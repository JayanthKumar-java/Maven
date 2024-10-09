package com.arteriatech.support;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
//import org.eclipse.persistence.oxm.json.JsonObjectBuilderResult;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

public class TestLogs extends HttpServlet{

	
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String oDataUrl="",userName="",password="",userPass="",executeURL="";
		JsonObject retrnRes=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		String id="";
		boolean debug=false;
		try {
			if(request.getParameter("ID")!=null){
			id=request.getParameter("ID");	
			}
			if(request.getParameter("debug")!=null){
				debug=true;
			}
			
			userName = commonUtils.getODataDestinationProperties("User", "PingOData");
			password = commonUtils.getODataDestinationProperties("Password", "PingOData");
			oDataUrl=commonUtils.getODataDestinationProperties("URL", "PingOData");
			userPass = userName + ":" + password;
			if(debug){
				response.getWriter().println("ApplicationLogs ID:"+id);
			}
			executeURL = oDataUrl + "ApplicationLogs('" + id + "')";
			if (debug)
				response.getWriter().println("ApplicationLogs.executeURL: " + executeURL);
			retrnRes =commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");

			if (debug) {
				response.getWriter().println("delete ApplicationLogs response " + retrnRes);
			}

			if (!retrnRes.get("ErrorCode").isJsonNull()
					&& retrnRes.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")) {
				// need to send the Success Response
				JsonObject retnObj = new JsonObject();
				retnObj.addProperty("ErrorCode", "");
				retnObj.addProperty("message", "Record Deleted Successfully " + id);
				retnObj.addProperty("Status", "000001");
				response.getWriter().print(retnObj);

			} else {
				// send the failure response
				JsonObject retnObj = new JsonObject();
				retnObj.addProperty("ErrorCode", "001");
				retnObj.addProperty("message", "Records not  Deleted " + id);
				retnObj.addProperty("Status", "000002");
				response.getWriter().print(retnObj);
			}

		} catch (Exception ex) {
			JsonObject responseObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "001");
			response.getWriter().println(responseObj);
			throw ex;

		}
		
	
		
	}

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName="",password="",oDataURL="",userPass="",executeURL="";
		CommonUtils commonUtils=new CommonUtils();
		boolean debug=false;
		String logId="";
		
		try{
		if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		if(request.getParameter("ID")!=null){
			logId=request.getParameter("ID");
		}
		userName = commonUtils.getODataDestinationProperties("User", "PingOData");
		password = commonUtils.getODataDestinationProperties("Password", "PingOData");
		oDataURL=commonUtils.getODataDestinationProperties("URL", "PingOData");
		userPass=userName+":"+password;
		if(debug){
			response.getWriter().println("ApplicationLogs Id:"+logId);
		}
		executeURL=oDataURL+ "ApplicationLogs?$filter=ID%20eq%20%27" + logId + "%27";
		if(debug){
			response.getWriter().println("Execute Url"+executeURL);
			response.getWriter().println("userName "+userName);
			response.getWriter().println("password "+password);
		}
		JsonObject appLogs = commonUtils.executeURL(executeURL, userPass, response);
		if(debug){
			response.getWriter().println("ApplicationLogs: "+appLogs);
		}
		response.getWriter().println(appLogs);
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			response.getWriter().println(buffer);
			
		}
	}

	


	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		String userName="",password="",oDataURL="",userPass="",payload="",loginID="";
		
		boolean debug=false;
		JsonParser jsonparser=new JsonParser();
		JsonObject jsonPayload=new JsonObject();
		try{
			payload=commonUtils.getGetBody(request, response);
			jsonPayload=(JsonObject)jsonparser.parse(payload);
			if(jsonPayload.has("debug")&& !jsonPayload.get("debug").isJsonNull()&&!jsonPayload.get("debug").getAsString().equalsIgnoreCase("")){
				debug=true;
			}
			if(debug){
				response.getWriter().println("Received Input Payload :"+payload);
			}
			userName = commonUtils.getODataDestinationProperties("User", "PingOData");
			password = commonUtils.getODataDestinationProperties("Password", "PingOData");
			oDataURL=commonUtils.getODataDestinationProperties("URL", "PingOData");
			if(debug){
				response.getWriter().println("User Name:"+userName);
				response.getWriter().println("Password:"+password);
				response.getWriter().println("OdataUrl:"+oDataURL);
			}
			userPass=userName+":"+password;
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			executeURL = oDataURL+"ApplicationLogs";
			if(debug){
				response.getWriter().println("executeURL:"+executeURL);
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				
			}
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("CreatedAt", createdAt);
			loginID=commonUtils.getLoginID(request, response, debug);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			if(jsonPayload.has("AggregatorID")){
				insertPayload.accumulate("AggregatorID", jsonPayload.get("AggregatorID").getAsString());
			}else{
				insertPayload.accumulate("AggregatorID", "");
			}
			
			if(jsonPayload.has("ExternalNumber")){
				insertPayload.accumulate("ExternalNumber", jsonPayload.get("ExternalNumber").getAsString());
			}else{
				insertPayload.accumulate("ExternalNumber","");
			}
			/*if(jsonPayload.has("TCode")){
				insertPayload.accumulate("TCode", jsonPayload.get("TCode").getAsString());
			}else{
				insertPayload.accumulate("TCode","");	
			}*/
			if(jsonPayload.has("LogSubObject")){
				insertPayload.accumulate("LogSubObject", jsonPayload.get("LogSubObject").getAsString());
			}else{
				insertPayload.accumulate("LogSubObject","");	
			}
			if(jsonPayload.has("Program")){
				insertPayload.accumulate("Program", jsonPayload.get("Program").getAsString());
			}else{
				insertPayload.accumulate("Program","");
			}
			if(jsonPayload.has("Process")){
				insertPayload.accumulate("Process", jsonPayload.get("Process").getAsString());
			}else{
				insertPayload.accumulate("Process","");
			}
			if(jsonPayload.has("ProblemClass")){
				insertPayload.accumulate("ProblemClass", jsonPayload.get("ProblemClass").getAsString());
			}else{
				insertPayload.accumulate("ProblemClass","");
			}
			if(jsonPayload.has("ProcessID")){
				insertPayload.accumulate("ProcessID", jsonPayload.get("ProcessID").getAsString());
			}else{
				insertPayload.accumulate("ProcessID","");
			}
			if(jsonPayload.has("ProcessRef1")){
				insertPayload.accumulate("ProcessRef1", jsonPayload.get("ProcessRef1").getAsString());
			}else{
				insertPayload.accumulate("ProcessRef1","");
			}
			if(jsonPayload.has("ProcessRef2")){
				insertPayload.accumulate("ProcessRef2", jsonPayload.get("ProcessRef2").getAsString());
			}else{
				insertPayload.accumulate("ProcessRef2","");
			}
			if(jsonPayload.has("ProcessRef3")){
				insertPayload.accumulate("ProcessRef3", jsonPayload.get("ProcessRef3").getAsString());
			}else{
				insertPayload.accumulate("ProcessRef3","");
			}
			
			if(jsonPayload.has("ProcessRef4")){
				insertPayload.accumulate("ProcessRef4", jsonPayload.get("ProcessRef4").getAsString());
			}else{
				insertPayload.accumulate("ProcessRef4","");
			}
			if(jsonPayload.has("ProcessRef5")){
				insertPayload.accumulate("ProcessRef5", jsonPayload.get("ProcessRef5").getAsString());
			}else{
				insertPayload.accumulate("ProcessRef5","");
			}
			if(jsonPayload.has("ProcessParams")){
				insertPayload.accumulate("ProcessParams", jsonPayload.get("ProcessParams").getAsString());
			}else{
				insertPayload.accumulate("ProcessParams","");
			}
			if(jsonPayload.has("CorrelationID")){
				insertPayload.accumulate("CorrelationID", jsonPayload.get("CorrelationID").getAsString());
			}else{
				insertPayload.accumulate("CorrelationID","");
			}
			if(jsonPayload.has("Source")){
				insertPayload.accumulate("Source", jsonPayload.get("Source").getAsString());
			}else{
				insertPayload.accumulate("Source","");
			}
			if(jsonPayload.has("SourceReferenceID")){
				insertPayload.accumulate("SourceReferenceID", jsonPayload.get("SourceReferenceID").getAsString());
			}else{
				insertPayload.accumulate("SourceReferenceID","");
			}
			if(debug){
				response.getWriter().println("Exceute Utl:"+executeURL);
				response.getWriter().println("Insert Object:"+insertPayload);
			}
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			response.getWriter().println(responseObject);
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<buffer.length();i++){
				buffer.append(stackTrace[i]);
			}
			response.getWriter().println(buffer);
			
		}
	}

	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", changedAt="", executeURL="", uniqueMsgID="";
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		long changedOn = 0;
		// CloseableHttpClient httpClient = null;
		JsonObject responseObject = new JsonObject();
		String userName="",password="",oDataURL="",userPass="",payload="",loginID="";
		boolean debug=false;
		JsonParser jsonparser=new JsonParser();
		JsonObject jsonPayload=new JsonObject();
		String ChangedBy="",logId="";
			try{
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination("PCGWHANA", options);
				HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
				HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

				payload=commonUtils.getGetBody(request, response);
				jsonPayload=(JsonObject)jsonparser.parse(payload);
				if(jsonPayload.has("debug")&& !jsonPayload.get("debug").isJsonNull()&&!jsonPayload.get("debug").getAsString().equalsIgnoreCase("")){
					debug=true;
				}
				if(debug){
					response.getWriter().println("Received Input Payload :"+payload);
				}
				userName = commonUtils.getODataDestinationProperties("User", "PingOData");
				password = commonUtils.getODataDestinationProperties("Password", "PingOData");
				oDataURL=commonUtils.getODataDestinationProperties("URL", "PingOData");
				userPass=userName+":"+password;
				if(debug){
					response.getWriter().println("UserName:"+userName);
					response.getWriter().println("Password:"+password);
					response.getWriter().println("OdataUrl:"+oDataURL);
				}
				ChangedBy=commonUtils.getLoginID(request, response, debug);
				insertPayload.accumulate("ChangedBy",ChangedBy);
				changedAt = commonUtils.getCreatedAtTime();
				insertPayload.accumulate("ChangedAt",changedAt);
				changedOn = commonUtils.getCreatedOnDate();
				insertPayload.accumulate("ChangedOn",  "/Date("+changedOn+")/");
				insertPayload.accumulate("LogTime",  changedAt);
				
				if(jsonPayload.has("ID") && !jsonPayload.get("ID").isJsonNull()){
					logId=jsonPayload.get("ID").getAsString();
					insertPayload.accumulate("ID", logId);
				}
				if(jsonPayload.has("AggregatorID") && !jsonPayload.get("AggregatorID").isJsonNull()){
					insertPayload.accumulate("AggregatorID", jsonPayload.get("AggregatorID").getAsString());
				}
				if(jsonPayload.has("LogObject") && !jsonPayload.get("LogObject").isJsonNull()){
					insertPayload.accumulate("LogObject", jsonPayload.get("LogObject").getAsString());
				}
				if(jsonPayload.has("LogSubObject") && !jsonPayload.get("LogSubObject").isJsonNull()){
					insertPayload.accumulate("LogSubObject", jsonPayload.get("LogSubObject").getAsString());
				}
				if(jsonPayload.has("ExternalNumber") && !jsonPayload.get("ExternalNumber").isJsonNull()){
					insertPayload.accumulate("ExternalNumber", jsonPayload.get("ExternalNumber").getAsString());
				}
				if(jsonPayload.has("LogDate") && !jsonPayload.get("LogDate").isJsonNull()){
					insertPayload.accumulate("LogDate", jsonPayload.get("LogDate").getAsString());
				}
				if(jsonPayload.has("LogUser") && !jsonPayload.get("LogUser").isJsonNull()){
					insertPayload.accumulate("LogUser", jsonPayload.get("LogUser").getAsString());
				}
				
				if(jsonPayload.has("TCode") && !jsonPayload.get("TCode").isJsonNull()){
					insertPayload.accumulate("TCode", jsonPayload.get("TCode").getAsString());
				}
				
				if(jsonPayload.has("Program") && !jsonPayload.get("Program").isJsonNull()){
					insertPayload.accumulate("Program", jsonPayload.get("Program").getAsString());
				}
				
				if(jsonPayload.has("ProblemClass") && !jsonPayload.get("ProblemClass").isJsonNull()){
					insertPayload.accumulate("ProblemClass", jsonPayload.get("ProblemClass").getAsString());
				}
				if(jsonPayload.has("Process") && !jsonPayload.get("Process").isJsonNull()){
					insertPayload.accumulate("Process", jsonPayload.get("Process").getAsString());
				}
				if(jsonPayload.has("ProcessID") && !jsonPayload.get("ProcessID").isJsonNull()){
					insertPayload.accumulate("ProcessID", jsonPayload.get("ProcessID").getAsString());
				}
				if(jsonPayload.has("ProcessRef1") && !jsonPayload.get("ProcessRef1").isJsonNull()){
					insertPayload.accumulate("ProcessRef1", jsonPayload.get("ProcessRef1").getAsString());
				}
				if(jsonPayload.has("ProcessRef2") && !jsonPayload.get("ProcessRef2").isJsonNull()){
					insertPayload.accumulate("ProcessRef2", jsonPayload.get("ProcessRef2").getAsString());
				}
				if(jsonPayload.has("ProcessRef3") && !jsonPayload.get("ProcessRef3").isJsonNull()){
					insertPayload.accumulate("ProcessRef3", jsonPayload.get("ProcessRef3").getAsString());
				}
				if(jsonPayload.has("ProcessRef4") && !jsonPayload.get("ProcessRef4").isJsonNull()){
					insertPayload.accumulate("ProcessRef4", jsonPayload.get("ProcessRef4").getAsString());
				}
				if(jsonPayload.has("ProcessRef5") && !jsonPayload.get("ProcessRef5").isJsonNull()){
					insertPayload.accumulate("ProcessRef5", jsonPayload.get("ProcessRef5").getAsString());
				}
				if(jsonPayload.has("ProcessParams") && !jsonPayload.get("ProcessParams").isJsonNull()){
					insertPayload.accumulate("ProcessParams", jsonPayload.get("ProcessParams").getAsString());
				}
				if(jsonPayload.has("CorrelationID") && !jsonPayload.get("CorrelationID").isJsonNull()){
					insertPayload.accumulate("CorrelationID", jsonPayload.get("CorrelationID").getAsString());
				}
				if(jsonPayload.has("CreatedBy") && !jsonPayload.get("CreatedBy").isJsonNull()){
					insertPayload.accumulate("CreatedBy", jsonPayload.get("CreatedBy").getAsString());
				}
				if(jsonPayload.has("CreatedAt") && !jsonPayload.get("CreatedAt").isJsonNull()){
					insertPayload.accumulate("CreatedAt", jsonPayload.get("CreatedAt").getAsString());
				}
				if(jsonPayload.has("CreatedOn") && !jsonPayload.get("CreatedOn").isJsonNull()){
					insertPayload.accumulate("CreatedOn", jsonPayload.get("CreatedOn").getAsString());
				}
				if(jsonPayload.has("Source") && !jsonPayload.get("Source").isJsonNull()){
					insertPayload.accumulate("Source", jsonPayload.get("Source").getAsString());
				}
				if(jsonPayload.has("SourceReferenceID") && !jsonPayload.get("SourceReferenceID").isJsonNull()){
					insertPayload.accumulate("SourceReferenceID", jsonPayload.get("SourceReferenceID").getAsString());
				}
				
                executeURL = oDataURL+"ApplicationLogs('"+logId+"')";
				
				if(debug){
					response.getWriter().println("executeUpdate-bpUpdatePayload: "+insertPayload);
				}
                requestEntity = new StringEntity(insertPayload.toString());
				String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
				if(debug){
					response.getWriter().println("executeUpdate.executeURL: "+ executeURL);
					response.getWriter().println("executeUpdate.userName: "+ userName);
			        response.getWriter().println("executeUpdate.password: "+ password);
			        response.getWriter().println("executeUpdate.basicAuth: "+ basicAuth);
				}
				
				/* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
		        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
		        
		        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build(); */
		        HttpPut updateRequest = new HttpPut(executeURL);
		        updateRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
		        updateRequest.setHeader("Content-Type", "application/json");
		        updateRequest.setHeader("Accept", "application/json");
		        updateRequest.setHeader("X-HTTP-Method", "PUT");
		        updateRequest.setEntity(requestEntity);
				
				// HttpResponse httpResponse = httpClient.execute(updateRequest);
				HttpResponse httpResponse = client.execute(updateRequest);
				responseEntity = httpResponse.getEntity();
				String returnVal="";
				
				
				if(debug){
					response.getWriter().println("executeUpdate.getStatusCode: "+httpResponse.getStatusLine().getStatusCode());
				}
				if(httpResponse.getStatusLine().getStatusCode()==204){
					JsonObject json=new JsonObject();
					json.addProperty("Message", "Updated Successfully");
					json.addProperty("Status", "000001");
					json.addProperty("ErrorCode", "");
					response.getWriter().println(json);
				}else{
				JSONObject responseJsonObject = new JSONObject();
				try{
					responseJsonObject = new JSONObject(EntityUtils.toString(responseEntity));
					
					if(responseJsonObject.has("error")){
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(responseJsonObject);
					}else{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
//						response.getWriter().println(responseJsonObject);
					}
				}catch (Exception ex) {
					StackTraceElement[] stackTrace = ex.getStackTrace();
					StringBuffer buffer=new StringBuffer();
					for(int i=0;i<stackTrace.length;i++){
						buffer.append(stackTrace[i]);
					}
					response.getWriter().println(buffer);
				}
				}	
//				if(httpResponse.getStatusLine().getStatusCode() > 204)
//					response.getWriter().println("Updated successfully");
			}catch(Exception ex){
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer=new StringBuffer();
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}
				response.getWriter().println(buffer);
				
			}
		finally{
			// httpClient.close();
		}
	
}
}

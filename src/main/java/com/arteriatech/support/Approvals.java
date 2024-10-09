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
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
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

public class Approvals  extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		
		boolean debug=false;
		JsonObject approvalReObj=new JsonObject();
		String  oDataUrl="", userName="", password="", userPass="", executeURL="";
		if(request.getParameter("debug")!=null && !request.getParameter("debug").equalsIgnoreCase("") && request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		try{
			String query = request.getQueryString();
			if(debug){
				response.getWriter().println("query:"+query);
			}
			
			if(query!=null && !query.equalsIgnoreCase("")){
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
				userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
				userPass = userName + ":" + password;
				executeURL=oDataUrl+"Approval?$filter="+query;
				if(debug){
					response.getWriter().println("executeURL:"+executeURL);
				}
				JsonObject approvalObjfrmDb = commonUtils.executeODataURL(executeURL, userPass, response, debug);
				if(debug){
					response.getWriter().println("executeODataURL:"+approvalObjfrmDb);
				}
				if(approvalObjfrmDb.get("Status").getAsString().equalsIgnoreCase("000001")){
					if(approvalObjfrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
						approvalReObj.add("Approval", approvalObjfrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
						approvalReObj.addProperty("Message", "Success");
						approvalReObj.addProperty("Status", "000001");
						approvalReObj.addProperty("ErrorCode", "");
						response.getWriter().println(approvalReObj);
					}else{
						approvalReObj.addProperty("Message", "Record not found");
						approvalReObj.addProperty("Status", "000002");
						approvalReObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(approvalReObj);
					}
				}else{
					response.getWriter().println(approvalObjfrmDb);
				}
			}else{
				approvalReObj.addProperty("Message", "Invalid input");
				approvalReObj.addProperty("Status", "000002");
				approvalReObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(approvalReObj);
			}

		} catch (Exception ex) {
			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String inputPayload = "";
		boolean debug = false;
		JSONObject parsePayload = null;
		CommonUtils commonUtils = new CommonUtils();
		try {
			inputPayload=commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				parsePayload = new JSONObject(inputPayload);
				if (parsePayload.has("debug") && !parsePayload.isNull("debug")
						&& !parsePayload.getString("debug").equalsIgnoreCase("")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println(" Input Payload Json :" + parsePayload);
				}
				JsonObject insertIntoApproval = commonUtils.insertIntoApproval(parsePayload, request, response, debug);
				if (debug)
					response.getWriter().println("insertBPResponse: " + insertIntoApproval);
				if (insertIntoApproval.has("ErrorCode")
						&& insertIntoApproval.get("ErrorCode").getAsString().equalsIgnoreCase("001")) {
					JsonObject result = new JsonObject();
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result.addProperty("ErrorCode", insertIntoApproval.get("ErrorCode").getAsString());
					result.addProperty("Status", "000002");
					result.addProperty("Message", insertIntoApproval.get("Message").getAsString());
					response.getWriter().println(new Gson().toJson(result));
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "");
					result.addProperty("Status", "000001");
					result.addProperty("Message", "Record Inserted Successfully");
					response.getWriter().println(result);
				}

			} else {
				// input payload as missing
				JsonObject retunObj = new JsonObject();
				retunObj.addProperty("Message", "Invalid Input  :" + inputPayload);
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);
			}

		}  catch (Exception ex) {
			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);
		}
	}

	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		// CloseableHttpClient httpClient = null;
		String executeURL="", payloadRequest = "", dataPayload="", corpID="", aggregatorID="", loginID="", oDataURL="", changedAt="";
		String apGuid="", changedBy="", userName="", password="", userPass="",inputPayload = "";
		long changedOnInMillis=0;
		JSONObject responseJsonObject = new JSONObject();
		JSONObject appUpdatedObj = new JSONObject();
		boolean debug = false;
		inputPayload = inputPayload.replaceAll("\\\\", "");
		JSONObject parsePayload = null;
		CommonUtils commonUtils = new CommonUtils();
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PCGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			inputPayload=commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				parsePayload = new JSONObject(inputPayload);
				if (parsePayload.has("debug") && !parsePayload.isNull("debug")
						&& !parsePayload.getString("debug").equalsIgnoreCase("")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println(" Input Payload Json :" + parsePayload);
				}
				if(parsePayload.has("ID") && !parsePayload.isNull("ID") &&parsePayload.getString("ID").length()>0){
					apGuid = parsePayload.getString("ID");
					changedBy = commonUtils.getUserPrincipal(request, "name", response);
					changedOnInMillis = commonUtils.getCreatedOnDate();
					changedAt = commonUtils.getCreatedAtTime();
					loginID = commonUtils.getUserPrincipal(request, "name", response);
					oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
					userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
					userPass = userName+":"+password;
					appUpdatedObj.accumulate("ID",apGuid);
					if (parsePayload.has("CreatedBy") && !parsePayload.isNull("CreatedBy")) {
						appUpdatedObj.accumulate("CreatedBy", parsePayload.getString("CreatedBy"));
					}else{
						appUpdatedObj.accumulate("CreatedBy", "");
					}

					if (parsePayload.has("CreatedAt") && !parsePayload.isNull("CreatedAt")) {
						appUpdatedObj.accumulate("CreatedAt", parsePayload.getString("CreatedAt"));
					}else{
						appUpdatedObj.accumulate("CreatedAt", JSONObject.NULL);
					}

					if (parsePayload.has("CreatedOn") && !parsePayload.isNull("CreatedOn")) {
						appUpdatedObj.accumulate("CreatedOn", parsePayload.getString("CreatedOn"));
					} else{
						appUpdatedObj.accumulate("CreatedOn", JSONObject.NULL);
					}

					if (parsePayload.has("AggregatorID") && !parsePayload.isNull("AggregatorID")) {
						appUpdatedObj.accumulate("AggregatorID", parsePayload.getString("AggregatorID"));
					} else {
						appUpdatedObj.accumulate("AggregatorID", "");
					}

					if (parsePayload.has("StatusID") &&! parsePayload.isNull("StatusID")) {
						appUpdatedObj.accumulate("StatusID", parsePayload.getString("StatusID"));
					} else {
						appUpdatedObj.accumulate("StatusID", "");

					}
					if (parsePayload.has("ProcessReference1") && !parsePayload.isNull("ProcessReference1")) {
						appUpdatedObj.accumulate("ProcessReference1", parsePayload.getString("ProcessReference1"));
					} else {
						appUpdatedObj.accumulate("ProcessReference1", "");

					}
					if (parsePayload.has("ProcessReference2") && !parsePayload.isNull("ProcessReference2")) {
						appUpdatedObj.accumulate("ProcessReference2", parsePayload.getString("ProcessReference2"));
					} else {
						appUpdatedObj.accumulate("ProcessReference2", "");

					}
					if (parsePayload.has("ProcessReference3") &&! parsePayload.isNull("ProcessReference3")) {
						appUpdatedObj.accumulate("ProcessReference3", parsePayload.getString("ProcessReference3"));
					} else {
						appUpdatedObj.accumulate("ProcessReference3", "");

					}
					if (parsePayload.has("ProcessReference4") &&! parsePayload.isNull("ProcessReference4")) {
						appUpdatedObj.accumulate("ProcessReference4", parsePayload.getString("ProcessReference4"));
					} else {
						appUpdatedObj.accumulate("ProcessReference4", "");

					}
					if (parsePayload.has("ProcessReference5") && !parsePayload.isNull("ProcessReference5")) {
						appUpdatedObj.accumulate("ProcessReference5", parsePayload.getString("ProcessReference5"));
					} else {
						appUpdatedObj.accumulate("ProcessReference5", "");

					}
					if (parsePayload.has("ProcessReference6") &&! parsePayload.isNull("ProcessReference6")) {
						appUpdatedObj.accumulate("ProcessReference6", parsePayload.getString("ProcessReference6"));
					} else {
						appUpdatedObj.accumulate("ProcessReference6", "");

					}
					if (parsePayload.has("ProcessReference7") &&! parsePayload.isNull("ProcessReference7")) {
						appUpdatedObj.accumulate("ProcessReference7", parsePayload.getString("ProcessReference7"));
					} else {
						appUpdatedObj.accumulate("ProcessReference7", "");

					}
					if (parsePayload.has("ProcessReference8") &&! parsePayload.isNull("ProcessReference8")) {
						appUpdatedObj.accumulate("ProcessReference8", parsePayload.getString("ProcessReference8"));
					} else {
						appUpdatedObj.accumulate("ProcessReference8", "");

					}
					if (parsePayload.has("ProcessReference9") &&! parsePayload.isNull("ProcessReference9")) {
						appUpdatedObj.accumulate("ProcessReference9", parsePayload.getString("ProcessReference9"));
					} else {
						appUpdatedObj.accumulate("ProcessReference9", "");

					}
					if (parsePayload.has("ProcessReference10") &&! parsePayload.isNull("ProcessReference10")) {
						appUpdatedObj.accumulate("ProcessReference10", parsePayload.getString("ProcessReference10"));
					} else {
						appUpdatedObj.accumulate("ProcessReference10", "");

					}
					if (parsePayload.has("Source") && !parsePayload.isNull("Source")) {
						appUpdatedObj.accumulate("Source", parsePayload.getString("Source"));
					} else {
						appUpdatedObj.accumulate("Source", "");

					}
					if (parsePayload.has("SourceReferenceID") &&! parsePayload.isNull("SourceReferenceID")) {
						appUpdatedObj.accumulate("SourceReferenceID", parsePayload.getString("SourceReferenceID"));
					} else {
						appUpdatedObj.accumulate("SourceReferenceID", "");

					}
					
					if (parsePayload.has("ProcessID") && !parsePayload.isNull("ProcessID")) {
						appUpdatedObj.accumulate("ProcessID", parsePayload.getString("ProcessID"));
					} else {
						appUpdatedObj.accumulate("ProcessID", "");

					}
					if (parsePayload.has("Remarks") &&! parsePayload.isNull("Remarks")) {
						String remarks = parsePayload.getString("Remarks");
						if (remarks.length() < 100) {
							appUpdatedObj.accumulate("Remarks", remarks);
						} else {
							appUpdatedObj.accumulate("Remarks", remarks.substring(0, 100));
						}
					} else {
						appUpdatedObj.accumulate("Remarks", "");
					}
					appUpdatedObj.accumulate("ChangedBy", changedBy);
					appUpdatedObj.accumulate("ChangedAt", changedAt);
					appUpdatedObj.accumulate("ChangedOn", "/Date(" + changedOnInMillis + ")/");
					executeURL = oDataURL+"Approval('"+apGuid+"')";
					if(debug){
						response.getWriter().println("executeUpdate-bpUpdatePayload: "+appUpdatedObj);
					}
					requestEntity = new StringEntity(appUpdatedObj.toString());
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
//			        updateRequest.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			        updateRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			        updateRequest.setHeader("Content-Type", "application/json");
			        updateRequest.setHeader("Accept", "application/json");
			        updateRequest.setHeader("X-HTTP-Method", "PUT");
			        updateRequest.setEntity(requestEntity);
//					updateRequest.setHeader("X-CSRF-Token", csrfToken);
					// HttpResponse httpResponse = httpClient.execute(updateRequest);
					HttpResponse httpResponse = client.execute(updateRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					if(debug){
						response.getWriter().println("statusCode:"+statusCode);
					}
					if(statusCode/100==2){
						JsonObject respoObj=new JsonObject();
						respoObj.addProperty("message", "Records Updated Successfully");
						respoObj.addProperty("ErrorCode", "");
						respoObj.addProperty("Status", "000001");
						response.getWriter().println(respoObj);
					} else {
						responseEntity = httpResponse.getEntity();
						if (debug) {
							response.getWriter().println(
									"executeUpdate.getStatusCode: " + httpResponse.getStatusLine().getStatusCode());
						}
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							JsonObject resObj=new JsonObject();
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							resObj.addProperty("Message", EntityUtils.toString(responseEntity));
							response.getWriter().println(resObj);
					}
				}else{
					//  no need to perform any task in this case because no apGuid is present
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "J001");
					result.addProperty("Status", "000002");
					result.addProperty("Message", "ID is Mandatory for updating");
					response.getWriter().println(new Gson().toJson(result));
				}
			} else {
				JsonObject retunObj = new JsonObject();
				retunObj.addProperty("Message", "Invalid Input  :" + inputPayload);
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);
			}
		}catch(JsonParseException ex){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", ex.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", ex.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
	}
}

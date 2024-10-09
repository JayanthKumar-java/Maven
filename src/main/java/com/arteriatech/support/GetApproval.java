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
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
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


public class GetApproval extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String apGuid = "";
		String inputPayload = "";
		JsonParser jsonParer = new JsonParser();
		JsonObject inputJson = new JsonObject();
		JsonObject approvalObj = new JsonObject();
		String executeURL = "", oDataUrl = "", userName = "", password = "", userPass = "";
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			inputJson = (JsonObject) jsonParer.parse(inputPayload);
			if (inputJson.has("debug") && !inputJson.get("debug").isJsonNull()
					&& inputJson.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;
			}
			if (!inputJson.get("ID").isJsonNull() && !inputJson.get("ID").getAsString().equalsIgnoreCase("")) {
				apGuid = inputJson.get("ID").getAsString();
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println("Odata URL " + oDataUrl);
					response.getWriter().println("User Name " + userName);
					response.getWriter().println("Password " + password);
				}
				executeURL=oDataUrl+"Approval('" + apGuid + "')";
					JsonObject approvalDelObj = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
					if(debug){
						response.getWriter().println("executeDelete Obj"+approvalDelObj);
					}
					
					if (!approvalDelObj.get("ErrorCode").isJsonNull()
							&& approvalDelObj.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")) {
						// need to send the Success Response
						JsonObject retnObj = new JsonObject();
						retnObj.addProperty("ErrorCode", "");
						retnObj.addProperty("message", "Record Deleted Successfully " + apGuid);
						retnObj.addProperty("Status", "000001");
						response.getWriter().print(retnObj);

					} else {
						// send the failure response
						JsonObject retnObj = new JsonObject();
						retnObj.addProperty("ErrorCode", "001");
						retnObj.addProperty("message", "Records not  Deleted " + apGuid);
						retnObj.addProperty("Status", "000002");
						response.getWriter().print(retnObj);
					}
					
					
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "J001");
					result.addProperty("Status", "000002");
					result.addProperty("Message", " ID Missing in the Input Payload");
					response.getWriter().println(new Gson().toJson(result));
				}

			} 
         catch (Exception e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			}
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", e.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
	
	}

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject approvalReObj=new JsonObject();
		boolean debug=false;
		String  oDataUrl="", userName="", password="", userPass="", executeURL="";
		String statusId="",cpGuid="",aggrId="",cpType="",processReference7="",processReference6="";
		StringBuilder queryBuilder=new StringBuilder();
		if(request.getParameter("debug")!=null && !request.getParameter("debug").equalsIgnoreCase("") && request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		try{	
			   oDataUrl = commonUtils.getODataDestinationProperties("URL","PCGWHANA"); 
			   userName =commonUtils.getODataDestinationProperties("User","PCGWHANA");
			   password = commonUtils.getODataDestinationProperties("Password","PCGWHANA");
			   userPass = userName + ":" + password;
			   if(request.getParameter("StatusID")!=null && !request.getParameter("StatusID").equalsIgnoreCase("")){
					statusId=request.getParameter("StatusID");
				}
				if(request.getParameter("AGGRID")!=null && !request.getParameter("AGGRID").equalsIgnoreCase("")){
					aggrId=request.getParameter("AGGRID");
				}
				 if(request.getParameter("CpType")!=null && !request.getParameter("CpType").equalsIgnoreCase("")){
					 cpType=request.getParameter("CpType");
					}
					if(request.getParameter("CpGuId")!=null && !request.getParameter("CpGuId").equalsIgnoreCase("")){
						cpGuid=request.getParameter("CpGuId");
					}
				if(request.getParameter("ProcessReference7")!=null && !request.getParameter("ProcessReference7").equalsIgnoreCase("")){
					processReference7=request.getParameter("ProcessReference7");
				}
				if(request.getParameter("ProcessReference6")!=null && !request.getParameter("ProcessReference6").equalsIgnoreCase("")){
					processReference6=request.getParameter("ProcessReference6");
				}
					
				if(statusId.equalsIgnoreCase("") &&aggrId.equalsIgnoreCase("") &&cpType.equalsIgnoreCase("")&&cpGuid.equalsIgnoreCase("")&&processReference7.equalsIgnoreCase("")&&processReference6.equalsIgnoreCase("")){
					executeURL = oDataUrl + "Approval";
				}else if(!statusId.equalsIgnoreCase("")&&!aggrId.equalsIgnoreCase("")&&!cpType.equalsIgnoreCase("")&&!cpGuid.equalsIgnoreCase("")&&!processReference7.equalsIgnoreCase("")&&!processReference6.equalsIgnoreCase("")){
					executeURL = oDataUrl + "Approval?$filter=StatusID%20eq%20%27" + statusId
							+ "%27%20and%20AggregatorID%20eq%20%27" + aggrId +"%27%20and%20ProcessReference3%20eq%20%27"+cpGuid+"%27%20and%20ProcessReference4%20eq%20%27"+cpType+"%27%20and%20ProcessReference7%20eq%20%27"+processReference7+"%27%20and%20ProcessReference6%20eq%20%27"+processReference6+"%27";
				}
				else{
					queryBuilder.append(oDataUrl).append("Approval?$filter=");
					if(!statusId.equalsIgnoreCase("")){
						queryBuilder.append("StatusID%20eq%20%27").append(statusId).append("%27");
					}
					if(!aggrId.equalsIgnoreCase("")){
						if(!statusId.equalsIgnoreCase("")){
							queryBuilder.append("%20and%20AggregatorID%20eq%20%27").append(aggrId).append("%27");
						}else{
							queryBuilder.append("AggregatorID%20eq%20%27").append(aggrId).append("%27");
						}
					}
					if(!cpGuid.equalsIgnoreCase("")){
					if(!aggrId.equalsIgnoreCase("")||!statusId.equalsIgnoreCase("")){
						queryBuilder.append("%20and%20ProcessReference3%20eq%20%27").append(cpGuid).append("%27");
					}else{
						queryBuilder.append("ProcessReference3%20eq%20%27").append(cpGuid).append("%27");
					}
				}
					if(!cpType.equalsIgnoreCase("")){
						if(!aggrId.equalsIgnoreCase("")||!statusId.equalsIgnoreCase("")||!cpGuid.equalsIgnoreCase("")){
							queryBuilder.append("%20and%20ProcessReference4%20eq%20%27").append(cpType).append("%27");
						}
					else{
						queryBuilder.append("ProcessReference4%20eq%20%27").append(cpType).append("%27");
					 }
					}
					if(!processReference7.equalsIgnoreCase("")){
						if(!aggrId.equalsIgnoreCase("")||!statusId.equalsIgnoreCase("")||!cpGuid.equalsIgnoreCase("")&&!cpType.equalsIgnoreCase("")){
							queryBuilder.append("%20and%20ProcessReference7%20eq%20%27").append(processReference7).append("%27");
					}else{
						queryBuilder.append("ProcessReference7%20eq%20%27").append(processReference7).append("%27");
					}
					
				 }
					if(!processReference6.equalsIgnoreCase("")){
						if(!processReference7.equalsIgnoreCase("")||!aggrId.equalsIgnoreCase("")||!statusId.equalsIgnoreCase("")||!cpGuid.equalsIgnoreCase("")&&!cpType.equalsIgnoreCase("")){
							queryBuilder.append("%20and%20ProcessReference6%20eq%20%27").append(processReference6).append("%27");
						}else{
							queryBuilder.append("ProcessReference6%20eq%20%27").append(processReference6).append("%27");
						}
					}
					
					executeURL =queryBuilder.toString();
				}		
				if (debug)
					response.getWriter().println("executeURL: " + executeURL);
				  approvalReObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("Approval Response :" + approvalReObj);
				}
				if (approvalReObj != null
						&& approvalReObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
			
				JSONObject resultObj=new JSONObject();
				resultObj.accumulate("Message",new Gson().toJson(approvalReObj.get("d").getAsJsonObject().get("results").getAsJsonArray()));
				resultObj.accumulate("Status","000001" );
				resultObj.accumulate("ErrorCode","" );
					response.getWriter()
							.print(resultObj);
				} else {
					JsonObject retunObj = new JsonObject();
					retunObj.addProperty("Message",
							"Records Not Exist");
					retunObj.addProperty("Status", "000002");
					retunObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(retunObj);
				}

			
		}
		catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug) {
				response.getWriter().println(buffer.toString());
			}
			JsonObject responseObject = new JsonObject();
			responseObject.addProperty("ErrorCode", "006");
			responseObject.addProperty("Message", "Exception: " + ex.getMessage() + ". Full Stacktrace: "
					+ ex.getLocalizedMessage() + "-" + buffer.toString());
			responseObject.addProperty("Status", "000002");
			response.getWriter().println(responseObject);
		}
		
		
	
		
	}

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
				JsonObject insertIntoApproval = insertApprovalRecord(parsePayload, request, response, debug);
				if (debug)
					response.getWriter().println("insertBPResponse: " + insertIntoApproval);
				if (insertIntoApproval.has("ErrorCode")
						&& insertIntoApproval.get("ErrorCode").getAsString().equalsIgnoreCase("001")) {
					JsonObject result = new JsonObject();
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
				retunObj.addProperty("Message", "InputPayload Is Empty  :" + inputPayload);
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);
			}

		} catch (JsonParseException ex) {
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);

		} catch (Exception ex) {
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);

		}
	
	}

	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		// CloseableHttpClient httpClient = null;
		String executeURL="", payloadRequest = "", dataPayload="", corpID="", aggregatorID="", loginID="", oDataURL="", changedAt="";
		String apGuid="", changedBy="", userName="", password="", userPass="",inputPayload = "";
		long changedOnInMillis=0;
		JSONObject responseJsonObject = new JSONObject();
		JSONObject appUpdatedObj = new JSONObject();
		boolean debug = false;
		CommonUtils commonUtils=new CommonUtils();
		JSONObject parsePayload = null;
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PCGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			inputPayload=commonUtils.getGetBody(request, response);
			inputPayload = inputPayload.replaceAll("\\\\", "");
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
					oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
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
					}else{
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
					if(httpResponse.getStatusLine().getStatusCode()==204){
						JsonObject respoObj=new JsonObject();
						respoObj.addProperty("Message", "Records Updated Successfully");
						respoObj.addProperty("ErrorCode", "");
						respoObj.addProperty("Status", "000001");
						response.getWriter().println(respoObj);
						
					} else {
						responseEntity = httpResponse.getEntity();
						if (debug) {
							response.getWriter().println(
									"executeUpdate.getStatusCode: " + httpResponse.getStatusLine().getStatusCode());
						}
						responseJsonObject = new JSONObject(EntityUtils.toString(responseEntity));
						if (responseJsonObject.has("error")) {
							JsonObject responseObj=new JsonObject();
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							if(!responseJsonObject.isNull("error")){
								JSONObject jsonObject = responseJsonObject.getJSONObject("error");
								if(jsonObject.has("message")&&!jsonObject.isNull("message")){
									JSONObject valueObj = jsonObject.getJSONObject("message");
								   if(valueObj.has("value")&&!valueObj.isNull("value")){
									   responseObj.addProperty("Message", valueObj.getString("value"));   
								   }else{
									   responseObj.addProperty("Message","Records Not Updated"); 
								   }
								}else{
									responseObj.addProperty("Message","Records Not Updated"); 
								}
							}else{
								responseObj.addProperty("Message","Records Not Updated"); 
							}
							responseObj.addProperty("ErrorCode", "J001");
							responseObj.addProperty("Status", "000002");
							response.getWriter().println(responseObj);
						} else {
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						}
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
	
	private JsonObject insertApprovalRecord(JSONObject inputPayload,HttpServletRequest request,HttpServletResponse response,boolean debug)throws IOException,Exception{
		String createdOn="", createdAt ="",createdBy="",  iD="", oDataUrl="", executeURL="", customerNo="",userName="", password="", userPass="";
		long createdOnInMillis=0;
		String id="",remarks="";
		JSONObject approvalObj = new JSONObject();
		JsonObject apprvlResponse=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
	try {
		  oDataUrl = commonUtils.getODataDestinationProperties("URL","PCGWHANA"); 
		   userName =commonUtils.getODataDestinationProperties("User","PCGWHANA");
		   password = commonUtils.getODataDestinationProperties("Password","PCGWHANA");
		   userPass = userName + ":" + password;
		 userPass = userName + ":" + password;
		executeURL = oDataUrl + "Approval";
		createdBy = commonUtils.getUserPrincipal(request, "name", response);
		createdAt = commonUtils.getCreatedAtTime();
		createdOnInMillis =commonUtils.getCreatedOnDate();
		id =commonUtils. generateGUID(36);
		if(inputPayload.has("ID")&&!inputPayload.isNull("ID")){
			approvalObj.accumulate("ID", inputPayload.getString("ID"));
		}else{
			approvalObj.accumulate("ID", id);
		}
		approvalObj.accumulate("CreatedBy", createdBy);
		approvalObj.accumulate("CreatedAt", createdAt);
		approvalObj.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
		if (inputPayload.has("AggregatorID") && !inputPayload.isNull("AggregatorID")) {
			approvalObj.accumulate("AggregatorID", inputPayload.getString("AggregatorID"));
		} else {
			approvalObj.accumulate("AggregatorID", "");
		}
		if (inputPayload.has("StatusID") && !inputPayload.isNull("StatusID")) {
			approvalObj.accumulate("StatusID", inputPayload.getString("StatusID"));
		} else {
			approvalObj.accumulate("StatusID", "");
		}
		if (inputPayload.has("ProcessReference3") && !inputPayload.isNull("ProcessReference3")) {
			approvalObj.accumulate("ProcessReference3", inputPayload.getString("ProcessReference3"));
		} else {
			approvalObj.accumulate("ProcessReference3", "");

		}
		if (inputPayload.has("ProcessReference4") && !inputPayload.isNull("ProcessReference4")) {
			approvalObj.accumulate("ProcessReference4", inputPayload.getString("ProcessReference4"));
		} else {
			approvalObj.accumulate("ProcessReference4", "");
		}
		if (inputPayload.has("ProcessReference7") && !inputPayload.isNull("ProcessReference7")) {
			approvalObj.accumulate("ProcessReference7", inputPayload.getString("ProcessReference7"));
		} else {
			approvalObj.accumulate("ProcessReference7", "");
		}
		if (inputPayload.has("ProcessID") && !inputPayload.isNull("ProcessID")) {
			approvalObj.accumulate("ProcessID", inputPayload.getString("ProcessID"));
		} else {
			approvalObj.accumulate("ProcessID", "");
		}
		if (inputPayload.has("ProcessReference1") && !inputPayload.isNull("ProcessReference1")) {
			approvalObj.accumulate("ProcessReference1", inputPayload.getString("ProcessReference1"));
		} else {
			approvalObj.accumulate("ProcessReference1", "");
		}
		if (inputPayload.has("ProcessReference2") && !inputPayload.isNull("ProcessReference2")) {
			approvalObj.accumulate("ProcessReference2", inputPayload.getString("ProcessReference2"));
		} else {
			approvalObj.accumulate("ProcessReference2", "");
		}
		if (inputPayload.has("ProcessReference5") && !inputPayload.isNull("ProcessReference5")) {
			approvalObj.accumulate("ProcessReference5", inputPayload.getString("ProcessReference5"));
		} else {
			approvalObj.accumulate("ProcessReference5", "");
		}
		if (inputPayload.has("ProcessReference6") && !inputPayload.isNull("ProcessReference6")) {
			approvalObj.accumulate("ProcessReference6", inputPayload.getString("ProcessReference6"));
		} else {
			approvalObj.accumulate("ProcessReference6", "");
		}
		if (inputPayload.has("ProcessReference8") && !inputPayload.isNull("ProcessReference8")) {
			approvalObj.accumulate("ProcessReference8", inputPayload.getString("ProcessReference8"));
		} else {
			approvalObj.accumulate("ProcessReference8", "");
		}
		
		if (inputPayload.has("ProcessReference9") && !inputPayload.isNull("ProcessReference9")) {
			approvalObj.accumulate("ProcessReference9", inputPayload.getString("ProcessReference9"));
		} else {
			approvalObj.accumulate("ProcessReference9", "");
		}
		
		if (inputPayload.has("ProcessReference10") && !inputPayload.isNull("ProcessReference10")) {
			approvalObj.accumulate("ProcessReference10", inputPayload.getString("ProcessReference10"));
		} else {
			approvalObj.accumulate("ProcessReference10", "");
		}
		
		if (inputPayload.has("Source") && !inputPayload.isNull("Source")) {
			approvalObj.accumulate("Source", inputPayload.getString("Source"));
		} else {
			approvalObj.accumulate("Source", "");
		}
		
		if (inputPayload.has("SourceReferenceID") && !inputPayload.isNull("SourceReferenceID")) {
			approvalObj.accumulate("SourceReferenceID", inputPayload.getString("SourceReferenceID"));
		} else {
			approvalObj.accumulate("SourceReferenceID", "");
		}
		
		if (inputPayload.has("Remarks") && !inputPayload.isNull("Remarks")) {
			approvalObj.accumulate("Remarks", inputPayload.getString("Remarks"));
		} else {
			approvalObj.accumulate("Remarks", "");
		}
		
		if (debug) {
			response.getWriter().print("Insert into Approval payload:" + approvalObj);
		}
		apprvlResponse =commonUtils.executePostURL(executeURL, userPass, response, approvalObj, request, debug, "PCGWHANA");
		if (debug)
			response.getWriter().println("Insert into Approval Obj" + apprvlResponse);
		if (apprvlResponse.has("error")) {
			apprvlResponse.addProperty("ErrorCode", "001");
			apprvlResponse.addProperty("Message", "Insertion Failed");
			return apprvlResponse;
		} else {
			apprvlResponse.addProperty("ErrorCode", "");
			apprvlResponse.addProperty("Message", "Success");
			return apprvlResponse;
		}

	} catch (Exception ex) {
		if (debug) {
			response.getWriter()
					.println("insertApprovalData.xception in Insering a Approval Table: " + ex.getMessage());
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println("insertBPHeaderData.Full Stack Trace: " + buffer.toString());
		}
		apprvlResponse.addProperty("ErrorCode", "001");
		apprvlResponse.addProperty("Message", "Insertion Failed");
		return apprvlResponse;

	}
		
	
	}
	
	private JsonObject getExceptionMessage(Exception ex){
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
         return retunObj;
		
	}
	
	

}

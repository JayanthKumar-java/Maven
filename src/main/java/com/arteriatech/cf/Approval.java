package com.arteriatech.cf;

import java.io.BufferedReader;
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
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

public class Approval extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ProcessReference7="",ProcessReference3="";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject approvalReObj=new JsonObject();
		boolean debug=false;
		String  oDataUrl="", userName="", password="", userPass="", executeURL="";
		ProcessReference7=request.getParameter("ProcessReference7");
		ProcessReference3=request.getParameter("ProcessReference3");
		if(request.getParameter("debug")!=null && !request.getParameter("debug").equalsIgnoreCase("") && request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		try{
			if (ProcessReference7 != null && !ProcessReference7.equalsIgnoreCase("")) {
				  oDataUrl = commonUtils.getODataDestinationProperties("URL","PCGWHANA"); 
				  userName =commonUtils.getODataDestinationProperties("User","PCGWHANA");
				  password = commonUtils.getODataDestinationProperties("Password","PCGWHANA");
				userPass = userName + ":" + password;
				if(ProcessReference3!=null && !ProcessReference3.equalsIgnoreCase("")){
					executeURL = oDataUrl + "Approval?$filter=ProcessReference7%20eq%20%27" + ProcessReference7
							+ "%27%20and%20ProcessReference3%20eq%20%27" + ProcessReference3 + "%27";
				}else{
				executeURL = oDataUrl + "Approval?$filter=ProcessReference7%20eq%20%27" + ProcessReference7 + "%27";
				}
				if (debug)
					response.getWriter().println("executeURL: " + executeURL);
				approvalReObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("Approval Response :" + approvalReObj);
				}
				if (approvalReObj != null
						&& approvalReObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					response.getWriter()
							.print(approvalReObj.get("d").getAsJsonObject().get("results").getAsJsonArray());
				} else {
					JsonObject retunObj = new JsonObject();
					retunObj.addProperty("Message",
							"No Record Exist for The Given ProcessReference7 :" + ProcessReference7);
					retunObj.addProperty("Status", "000002");
					retunObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(retunObj);
				}

			} else {
				JsonObject retunObj = new JsonObject();
				retunObj.addProperty("Message", "Invalid Input  :" + ProcessReference7);
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);
			}
		}catch(JsonParseException ex){
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);
		}
		catch(Exception ex){
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);
		}
		
		
	}

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String inputPayload = "";
		boolean debug = false;
		inputPayload=getGetBody(request, response);
		JSONObject parsePayload = null;
		CommonUtils commonUtils = new CommonUtils();
		try {
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
					result.addProperty("Message", "");
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
		/*inputPayload = request.getParameter("RenewalOD");*/
		inputPayload=getGetBody(request, response);
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

			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				parsePayload = new JSONObject(inputPayload);
				if (parsePayload.has("debug") && !parsePayload.isNull("debug")
						&& !parsePayload.getString("debug").equalsIgnoreCase("")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println(" Input Payload Json :" + parsePayload);
				}
				JsonObject getApprovalJsonObj = new  JsonObject();
				if(parsePayload.has("ID") && !parsePayload.isNull("ID") &&parsePayload.getString("ID").length()>0){
					apGuid = parsePayload.getString("ID");

					JsonObject getApproval = new JsonObject();
					getApprovalJsonObj = commonUtils.getApprovalByGuid(request, response, apGuid, debug);
					getApproval = getApprovalJsonObj.get("d").getAsJsonObject();
					if(debug){
						response.getWriter().println("getApproval: "+getApproval);
					}

					changedBy = commonUtils.getUserPrincipal(request, "name", response);
					changedOnInMillis = commonUtils.getCreatedOnDate();
					changedAt = commonUtils.getCreatedAtTime();
					loginID = commonUtils.getUserPrincipal(request, "name", response);
					oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");

					userPass = userName+":"+password;
					
					appUpdatedObj.accumulate("ID",apGuid);
					// getBP.get("CreatedOn").getAsString()
					if (getApproval.get("CreatedBy").getAsString() != null  && getApproval.get("CreatedBy").getAsString().trim().length() > 0) {
						appUpdatedObj.accumulate("CreatedBy", getApproval.get("CreatedBy").getAsString());
					}
					if (getApproval.get("CreatedAt").getAsString() != null  && getApproval.get("CreatedAt").getAsString().trim().length() > 0) {
						appUpdatedObj.accumulate("CreatedAt", getApproval.get("CreatedAt").getAsString());
					}

					if (getApproval.get("CreatedOn").getAsString() != null  && getApproval.get("CreatedOn").getAsString().trim().length() > 0) {
						appUpdatedObj.accumulate("CreatedOn", getApproval.get("CreatedOn").getAsString());
					} 
					if (parsePayload.has("AggregatorID") && !parsePayload.isNull("AggregatorID")) {
						appUpdatedObj.accumulate("AggregatorID", parsePayload.getString("AggregatorID"));
					} else {
						if(null != getApproval.get("AggregatorID").getAsString() && getApproval.get("AggregatorID").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("AggregatorID", getApproval.get("AggregatorID").getAsString());
						else
							appUpdatedObj.accumulate("AggregatorID", "");
					}
					if (parsePayload.has("StatusID") &&! parsePayload.isNull("StatusID")) {
						appUpdatedObj.accumulate("StatusID", parsePayload.getString("StatusID"));
					} else {
						if(null != getApproval.get("StatusID").getAsString() && getApproval.get("StatusID").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("StatusID", getApproval.get("StatusID").getAsString());
						else
							appUpdatedObj.accumulate("StatusID", "");
					}
					if (parsePayload.has("ProcessReference1") && !parsePayload.isNull("ProcessReference1")) {
						appUpdatedObj.accumulate("ProcessReference1", parsePayload.getString("ProcessReference1"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference1", "");
						if(null != getApproval.get("ProcessReference1").getAsString() && getApproval.get("ProcessReference1").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference1", getApproval.get("ProcessReference1").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference1", "");
					}
					if (parsePayload.has("ProcessReference2") && !parsePayload.isNull("ProcessReference2")) {
						appUpdatedObj.accumulate("ProcessReference2", parsePayload.getString("ProcessReference2"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference2", "");
						if(null != getApproval.get("ProcessReference2").getAsString() && getApproval.get("ProcessReference2").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference2", getApproval.get("ProcessReference2").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference2", "");
					}
					if (parsePayload.has("ProcessReference3") &&! parsePayload.isNull("ProcessReference3")) {
						appUpdatedObj.accumulate("ProcessReference3", parsePayload.getString("ProcessReference3"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference3", "");
						if(null != getApproval.get("ProcessReference3").getAsString() && getApproval.get("ProcessReference3").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference3", getApproval.get("ProcessReference3").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference3", "");
					}
					if (parsePayload.has("ProcessReference4") &&! parsePayload.isNull("ProcessReference4")) {
						appUpdatedObj.accumulate("ProcessReference4", parsePayload.getString("ProcessReference4"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference4", "");
						if(null != getApproval.get("ProcessReference4").getAsString() && getApproval.get("ProcessReference4").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference4", getApproval.get("ProcessReference4").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference4", "");
					}

					if (parsePayload.has("ProcessReference5") && !parsePayload.isNull("ProcessReference5")) {
						appUpdatedObj.accumulate("ProcessReference5", parsePayload.getString("ProcessReference5"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference5", "");
						if(null != getApproval.get("ProcessReference5").getAsString() && getApproval.get("ProcessReference5").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference5", getApproval.get("ProcessReference5").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference5", "");
					}
					
					if (parsePayload.has("ProcessReference6") &&! parsePayload.isNull("ProcessReference6")) {
						appUpdatedObj.accumulate("ProcessReference6", parsePayload.getString("ProcessReference6"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference6", "");
						if(null != getApproval.get("ProcessReference6").getAsString() && getApproval.get("ProcessReference6").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference6", getApproval.get("ProcessReference6").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference6", "");
					}
					if (parsePayload.has("ProcessReference7") &&! parsePayload.isNull("ProcessReference7")) {
						appUpdatedObj.accumulate("ProcessReference7", parsePayload.getString("ProcessReference7"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference7", "");
						if(null != getApproval.get("ProcessReference7").getAsString() && getApproval.get("ProcessReference7").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference7", getApproval.get("ProcessReference7").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference7", "");
					}
					if (parsePayload.has("ProcessReference8") &&! parsePayload.isNull("ProcessReference8")) {
						appUpdatedObj.accumulate("ProcessReference8", parsePayload.getString("ProcessReference8"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference8", "");
						if(null != getApproval.get("ProcessReference8").getAsString() && getApproval.get("ProcessReference8").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference8", getApproval.get("ProcessReference8").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference8", "");
					}

					if (parsePayload.has("ProcessReference9") &&! parsePayload.isNull("ProcessReference9")) {
						appUpdatedObj.accumulate("ProcessReference9", parsePayload.getString("ProcessReference9"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference9", "");
						if(null != getApproval.get("ProcessReference9").getAsString() && getApproval.get("ProcessReference9").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference9", getApproval.get("ProcessReference9").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference9", "");
					}

					if (parsePayload.has("ProcessReference10") &&! parsePayload.isNull("ProcessReference10")) {
						appUpdatedObj.accumulate("ProcessReference10", parsePayload.getString("ProcessReference10"));
					} else {
						// appUpdatedObj.accumulate("ProcessReference10", "");
						if(null != getApproval.get("ProcessReference10").getAsString() && getApproval.get("ProcessReference10").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessReference10", getApproval.get("ProcessReference10").getAsString());
						else
							appUpdatedObj.accumulate("ProcessReference10", "");
					}

					if (parsePayload.has("Source") && !parsePayload.isNull("Source")) {
						appUpdatedObj.accumulate("Source", parsePayload.getString("Source"));
					} else {
						// appUpdatedObj.accumulate("Source", "");
						if(null != getApproval.get("Source").getAsString() && getApproval.get("Source").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("Source", getApproval.get("Source").getAsString());
						else
							appUpdatedObj.accumulate("Source", "");
					}

					if (parsePayload.has("SourceReferenceID") &&! parsePayload.isNull("SourceReferenceID")) {
						appUpdatedObj.accumulate("SourceReferenceID", parsePayload.getString("SourceReferenceID"));
					} else {
						// appUpdatedObj.accumulate("SourceReferenceID", "");
						if(null != getApproval.get("SourceReferenceID").getAsString() && getApproval.get("SourceReferenceID").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("SourceReferenceID", getApproval.get("SourceReferenceID").getAsString());
						else
							appUpdatedObj.accumulate("SourceReferenceID", "");
					}
					
					if (parsePayload.has("ProcessID") && !parsePayload.isNull("ProcessID")) {
						appUpdatedObj.accumulate("ProcessID", parsePayload.getString("ProcessID"));
					} else {
						// appUpdatedObj.accumulate("ProcessID", "");
						if(null != getApproval.get("ProcessID").getAsString() && getApproval.get("ProcessID").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("ProcessID", getApproval.get("ProcessID").getAsString());
						else
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
						// appUpdatedObj.accumulate("Remarks", "");
						if(null != getApproval.get("Remarks").getAsString() && getApproval.get("Remarks").getAsString().trim().length() > 0)
							appUpdatedObj.accumulate("Remarks", getApproval.get("Remarks").getAsString());
						else
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
						responseJsonObject = new JSONObject(EntityUtils.toString(responseEntity));
						if (responseJsonObject.has("error")) {
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							response.getWriter().println(responseJsonObject);
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
	
	public String getGetBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		  String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }
		  body = jb.toString();
//		  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}
	
	
	
	

}

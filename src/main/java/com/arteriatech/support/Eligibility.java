package com.arteriatech.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Eligibility extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		String executeUrl="",userpass="",username="",password="",oDataUrl="";
		boolean debug=false;
		try{
			if(request.getParameter("debug")!=null &&request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;	
			}
			
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userpass=username+":"+password;
			if(debug){
				response.getWriter().println("oDataUrl:"+oDataUrl);
				response.getWriter().println("username:"+username);
			}
			if(request.getParameter("filter")!=null && !request.getParameter("filter").equalsIgnoreCase("")){
				if(debug){
					response.getWriter().println("filter passed from UI:"+request.getParameter("filter"));	
				}
				String filter = request.getParameter("filter");
				filter=filter.replaceAll(" ", "%20").replaceAll("'", "%27");
				if(debug){
					response.getWriter().println("Odata Filetr query:"+filter);
				}
				executeUrl=oDataUrl+"Eligibility?$filter="+filter;
			}else{
				executeUrl=oDataUrl+"Eligibility";
			}
			
			JsonObject eligibilityObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if(debug){
				response.getWriter().println("eligibilityObj:"+eligibilityObj);
			}
			if(eligibilityObj.get("Status").getAsString().equalsIgnoreCase("000001")){
				if(eligibilityObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
					JsonArray eligibilityArry = eligibilityObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
					resObj.add("Eligibility", eligibilityArry);
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);
				}else{
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Record doesn't exist");
					response.getWriter().println(resObj);
				}
				
			}else{
				response.getWriter().println(eligibilityObj);
			}
			
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ExceptionTrace", buffer.toString());
			response.getWriter().println(resObj);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		JsonObject resObj=new JsonObject();
		String executeUrl="",userpass="",username="",password="",oDataUrl="";
		JsonParser parser=new JsonParser();
		boolean debug=false;
		List<String> recordIds=new ArrayList<>();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {

				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userpass=username+":"+password;
				JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if(debug){
					response.getWriter().println("Received input Payload:"+jsonInput);
				}
				if(jsonInput.has("CorrelationID")&&!jsonInput.get("CorrelationID").isJsonNull()&&!jsonInput.get("CorrelationID").getAsString().equalsIgnoreCase("")){
					String correlationId = jsonInput.get("CorrelationID").getAsString();
					executeUrl=oDataUrl+"Eligibility?$filter=CorrelationID%20eq%20%27"+correlationId+"%27";
					if(debug){
						response.getWriter().println("executeUrl:"+executeUrl);
					}
					JsonObject eligibilityRec = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
					if(debug){
						response.getWriter().println("eligibilityRec:"+eligibilityRec);
					}
					if(eligibilityRec.get("Status").getAsString().equalsIgnoreCase("000001")){
						if(eligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
							JsonArray eligibilityArry = eligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
							for(int i=0;i<eligibilityArry.size();i++){
								JsonObject eligibleObg = eligibilityArry.get(i).getAsJsonObject();
								recordIds.add(eligibleObg.get("RecordID").getAsString());
							}
							
							executeUrl=oDataUrl+"BPHeader?$filter=ID%20eq%20%27"+correlationId+"%27";
							if(debug){
								response.getWriter().println("BPHeader execute URL:"+executeUrl);
							}
							JsonObject bpHeaderObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
							if(debug){
								response.getWriter().println("BPHeader Obj:"+bpHeaderObj);
							}
							if (bpHeaderObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								if (bpHeaderObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									bpHeaderObj = bpHeaderObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
									String cpGuid=bpHeaderObj.get("CPGuid").getAsString();
									String cpType=bpHeaderObj.get("CPType").getAsString();
									String aggregatorID=bpHeaderObj.get("AggregatorID").getAsString();
									executeUrl=oDataUrl+"SupplyChainFinanceEligibility?$filter=CPGUID%20eq%20%27"+cpGuid+"%27%20and%20CPTypeID%20eq%20%27"+cpType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20EligibilityTypeID%20eq%20%27"+"AML"+"%27";
									if(debug){
										response.getWriter().println("SupplyChainFinanceEligibility execute URL:"+executeUrl);
									}
									
									JsonObject scf1Obj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
									if(debug){
										response.getWriter().println("scf1Obj Obj:"+scf1Obj);
									}
									String scf1DeleteUrl="";
									if(scf1Obj.get("Status").getAsString().equalsIgnoreCase("000001")){
										if(scf1Obj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
											scf1Obj = scf1Obj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
											String scfGuid = scf1Obj.get("ID").getAsString();
											// Changes on 20231120
											// scf1DeleteUrl=oDataUrl+"SupplyChainFinanceEligibility('"+scfGuid+"')";
											scf1DeleteUrl="SupplyChainFinanceEligibility('"+scfGuid+"')";
											if(debug){
												response.getWriter().println("scfDeleteUrl:"+scf1DeleteUrl);
											}
										}
										JsonObject deletedResponse = deleteEligibilityRecords(oDataUrl, userpass, debug, response, recordIds,scf1DeleteUrl);
										response.getWriter().println(deletedResponse);
									}else{
										response.getWriter().println(scf1Obj);
									}
								} else {
									resObj.addProperty("Message", "BpHeader Record doesn't exist");
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									response.getWriter().println(resObj);
								}
							}else{
								response.getWriter().println(bpHeaderObj);
							}
						}else{
							resObj.addProperty("Message", "Record doesn't exist");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						}
						
					}else{
						response.getWriter().println(eligibilityRec);
					}
				}else{
					resObj.addProperty("Message", "CorrelationID Should not be empty or null Value");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}

			} else {
				resObj.addProperty("Message", "Empty input payload received");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {

			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", ex.getLocalizedMessage()+"");
			resObj.addProperty("ExceptionTrace", buffer.toString());
			response.getWriter().println(resObj);

		}
	}
	
	public JsonObject deleteEligibilityRecords(String odataUrl, String userpass, boolean debug, HttpServletResponse response, List<String> eligibilityIDs,String scf1DeleteUrl) {
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put("Content-Type", "application/json");
			changeSetHeaders.put("Accept", "application/json");
			changeSetHeaders.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			if(scf1DeleteUrl!=null && !scf1DeleteUrl.equalsIgnoreCase("")){
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(scf1DeleteUrl).headers(changeSetHeaders).build();
				changeSet.add(changeRequest);
			}
			for (int i = 0; i < eligibilityIDs.size(); i++) {
				String rocId =eligibilityIDs.get(i);
				// Changes on 20231120
				// String executeUrl = odataUrl + "Eligibility('" + rocId + "')";
				String executeUrl = "Eligibility('" + rocId + "')";
				if (debug) {
					response.getWriter().println("Delete eligibility record query: " + executeUrl);
				}
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
				changeSet.add(changeRequest);
			}
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			HttpResponse batchResponse = HttpClientBuilder.create().build().execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordDeleted = true;
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (!statusCode.equalsIgnoreCase("204")) {
					recordDeleted = false;
					if (debug) {
						response.getWriter().println("Error getting while deleting records:" + singleRes.getBody());
					}
					resObj.addProperty("Message", "Deleting Records Failed, error message:" + singleRes.getBody());
					break;
				}
			}
			if (recordDeleted) {
				resObj.addProperty("Message", "Record Deleted Successfully");
				resObj.addProperty("ErrorCode", "");
				resObj.addProperty("Status", "000001");

			} else {
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());

		}
		return resObj;
	}
	
	
	

}

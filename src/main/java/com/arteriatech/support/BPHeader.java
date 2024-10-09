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

public class BPHeader extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String oDataUrl = "", username = "", password = "", userpass = "", executeURL = "";
		boolean debug = false;
		JsonObject resObj = new JsonObject();
		JsonParser parser = new JsonParser();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			String inputPayload = commonUtils.getGetBody(request, response);
			if (debug) {
				response.getWriter().println("inputPayload:" + inputPayload);
			}
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
				if (jsonInput.has("ID") && !jsonInput.get("ID").getAsString().equalsIgnoreCase("")) {
					oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					userpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl:" + oDataUrl);
					}
					if (request.getServletPath().equalsIgnoreCase("/BPContactPersons")) {
						executeURL = oDataUrl + "BPContactPerson('" + jsonInput.get("ID").getAsString() + "')";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						JsonObject deletedResponse = commonUtils.executeODataDelete(executeURL, userpass, response, request, debug, "PYGWHANA");
						response.getWriter().println(deletedResponse);
					} else {
						executeURL = oDataUrl + "BPHeaders?$expand=BPContactPersons&$filter=BPGuid%20eq%20%27" + jsonInput.get("ID").getAsString() + "%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						JsonObject bpheaderRec = commonUtils.executeODataURL(executeURL, userpass, response, debug);
						if (debug) {
							response.getWriter().println("bpheaderRec:" + bpheaderRec);
						}
						if (bpheaderRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
							if (bpheaderRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								JsonObject bpheaders = bpheaderRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
								JsonObject deletedBpRec = deleteBPHeaderAndContractPerson(debug, response, userpass, oDataUrl, bpheaders);
								response.getWriter().println(deletedBpRec);
							} else {
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", "Record doesn't exist");
								response.getWriter().println(resObj);
							}
						} else {
							response.getWriter().println(bpheaderRec);
						}
					}
				} else {
					if (jsonInput.has("ID")) {
						resObj.addProperty("Message", "ID Should not be null or empty");
					} else {
						resObj.addProperty("Message", "Input payload doesn't contains a ID Property");
					}
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Empty input Payload received");
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
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ExceptionTrace", buffer.toString());
			response.getWriter().println(resObj);

		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String oDataUrl = "", username = "", password = "", userpass = "", executeUrl = "";
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		JsonObject resObj = new JsonObject();
		boolean cntPersonReq = false;
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (request.getParameter("ContractPersonReq") != null && request.getParameter("ContractPersonReq").equalsIgnoreCase("true")) {
				cntPersonReq = true;
			}
			if(debug){
				response.getWriter().println("Servlet Path:"+request.getServletPath());
			}
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userpass = username + ":" + password;
			if (debug) {
				response.getWriter().println("oDataUrl:" + oDataUrl);
			}
			if (request.getParameter("filter") != null && !request.getParameter("filter").equalsIgnoreCase("")) {
				if (debug) {
					response.getWriter().println("filter passed from UI:" + request.getParameter("filter"));
				}
				String filter = request.getParameter("filter");
				filter = filter.replaceAll(" ", "%20").replaceAll("'", "%27");
				if (debug) {
					response.getWriter().println("Odata Filetr query:" + filter);
				}
				if (request.getServletPath().equalsIgnoreCase("/BPContactPersons")) {
					executeUrl = oDataUrl + "BPContactPerson?$filter=" + filter;
				} else {
					if (cntPersonReq) {
						executeUrl = oDataUrl + "BPHeaders?$expand=BPContactPersons&$filter=" + filter;
					} else {
						executeUrl = oDataUrl + "BPHeaders?$filter=" + filter;
					}
				}
			} else {
				if (request.getServletPath().equalsIgnoreCase("/BPContactPersons")) {
					executeUrl = oDataUrl + "BPContactPerson";
				} else {
					if (cntPersonReq) {
						executeUrl = oDataUrl + "BPHeaders?$expand=BPContactPersons";
					} else {
						executeUrl = oDataUrl + "BPHeaders";
					}
				}
			}
			if (debug) {
				response.getWriter().println("executeUrl:" + executeUrl);
			}
			JsonObject bpHeaderObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("bpHeaderObj:" + bpHeaderObj);
			}
			if (bpHeaderObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (bpHeaderObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					if (request.getServletPath().equalsIgnoreCase("/BPContactPersons")){
						resObj.add("BPContactPerson", bpHeaderObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
					} else {
						resObj.add("BPRecord", bpHeaderObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
					}
					response.getWriter().println(resObj);
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Record doesn't exist");
					response.getWriter().println(resObj);
				}
			} else {
				response.getWriter().println(bpHeaderObj);
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

	public JsonObject deleteBPHeaderAndContractPerson(boolean debug, HttpServletResponse response, String userpass, String ODataUrl, JsonObject bpheaders) {
		String executeUrl = "";
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put("Content-Type", "application/json");
			changeSetHeaders.put("Accept", "application/json");
			changeSetHeaders.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			String bpGuid = bpheaders.get("BPGuid").getAsString();

			// Changes on 20231120
			// String executeURL = ODataUrl + "BPHeader('" + bpGuid + "')";
			String executeURL = "BPHeader('" + bpGuid + "')";
			if (debug) {
				response.getWriter().println("BPHeader delete executeURL:" + executeURL);
			}
			BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeURL).headers(changeSetHeaders).build();
			changeSet.add(changeRequest);
			if(bpheaders.get("BPContactPersons").getAsJsonObject().get("results").getAsJsonArray().size()>0){
				JsonArray bpcntpArr = bpheaders.get("BPContactPersons").getAsJsonObject().get("results").getAsJsonArray();
				for (int i = 0; i < bpcntpArr.size(); i++) {
					JsonObject bpcntpObj = bpcntpArr.get(i).getAsJsonObject();
					String bpCntpGuid = bpcntpObj.get("BPCntPrsnGuid").getAsString();
					// Changes on 20231120
					// executeUrl = ODataUrl + "BPContactPerson('" + bpCntpGuid + "')";
					executeUrl = "BPContactPerson('" + bpCntpGuid + "')";
					if(debug){
						response.getWriter().println("BPContactPerson delete Url:"+executeUrl);
					}
					BatchChangeSetPart bpCntpReqst = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
					changeSet.add(bpCntpReqst);
				}
			}
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(ODataUrl + "$batch"));
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
			String responsebOdy = "";
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
					if (singleRes.getBody() != null) {
						response.getWriter().println("BatchSingleResponse Body:" + singleRes.getBody());
					}
				}
				if (!statusCode.equalsIgnoreCase("204")) {
					recordDeleted = false;
					responsebOdy = singleRes.getBody();
					break;
				}
			}

			if (recordDeleted) {
				resObj.addProperty("Message", "Record Deleted Successfully");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
			} else {
				resObj.addProperty("Message", responsebOdy);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
			}
			return resObj;

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("Message", ex.getLocalizedMessage());
			}
			resObj.addProperty("ExceptionTrace", buffer.toString());
		}
		return resObj;
	}

}

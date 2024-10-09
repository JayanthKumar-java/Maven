package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MISScheduler extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String password = "", username = "", oDataUrl = "", executeURL = "", userpass = "";
		JsonObject resObj = new JsonObject();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userpass = username + ":" + password;
			if (debug) {
				response.getWriter().println("oDataUrl:" + oDataUrl);
			}
			if (request.getParameter("filter") != null && !request.getParameter("filter").equalsIgnoreCase("")) {
				String filters = request.getParameter("filter");
				if (debug) {
					response.getWriter().println("input paylaod filters:" + filters);
				}
				filters=filters.replaceAll(" ", "%20").replaceAll("'", "%27");
				if (debug) {
					response.getWriter().println("Odata filters:" + filters);
				}
				executeURL = oDataUrl+"MISScheduler?$filter=" + filters;
			} else {
				executeURL = oDataUrl +"MISScheduler";
			}
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject misSchedulerObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("misSchedulerObj:" + misSchedulerObj);
			}
			if (misSchedulerObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (misSchedulerObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.add("MISScheduler", misSchedulerObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);
				} else {
					resObj.addProperty("Message", "Record doesn't exist");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			} else {
				response.getWriter().println(misSchedulerObj);
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			}
			resObj.addProperty("Message", ex.getClass().getCanonicalName() + "---->" + buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject resObj=new JsonObject(); 
        CommonUtils commonUtils=new CommonUtils();
        String password="",username="",oDataUrl="",userpass="",executeUrl="";
        boolean debug=false;
        JsonParser parser=new JsonParser();
        JsonObject inserObj=new JsonObject();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
				userpass = username + ":" + password;
				JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("received input Payload:" + jsonInput);
				}
				String createdBy = commonUtils.getUserPrincipal(request, "name", response);
				String createdAt = commonUtils.getCreatedAtTime();
				long createdOnInMillis = commonUtils.getCreatedOnDate();
				String guid = commonUtils.generateGUID(36);
				if (debug) {
					response.getWriter().println("createdBy:" + createdBy);
					response.getWriter().println("createdAt:" + createdAt);
					response.getWriter().println("createdOnInMillis:" + createdOnInMillis);
					response.getWriter().println("guid:" + guid);
				}
				inserObj.addProperty("ID", guid);
				inserObj.addProperty("CreatedBy", createdBy);
				inserObj.addProperty("CreatedAt", createdAt);
				inserObj.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				
				if (jsonInput.has("AggregatorID") && !jsonInput.get("AggregatorID").isJsonNull()) {
					inserObj.addProperty("AggregatorID", jsonInput.get("AggregatorID").getAsString());
				} else {
					inserObj.addProperty("AggregatorID", "");
				}
				
				if (jsonInput.has("Report") && !jsonInput.get("Report").isJsonNull()) {
					inserObj.addProperty("Report", jsonInput.get("Report").getAsString());
				} else {
					inserObj.addProperty("Report", "");
				}

				if (jsonInput.has("VariantID") && !jsonInput.get("VariantID").isJsonNull()) {
					inserObj.addProperty("VariantID", jsonInput.get("VariantID").getAsString());
				} else {
					inserObj.addProperty("VariantID", "");
				}

				if (jsonInput.has("IsActive") && !jsonInput.get("IsActive").isJsonNull()) {
					inserObj.addProperty("IsActive", jsonInput.get("IsActive").getAsString());
				} else {
					inserObj.addProperty("IsActive", "");
				}
				if (jsonInput.has("Periodicity") && !jsonInput.get("Periodicity").isJsonNull()) {
					inserObj.addProperty("Periodicity", jsonInput.get("Periodicity").getAsString());
				} else {
					inserObj.addProperty("Periodicity", "");
				}

				if (jsonInput.has("QueueName") && !jsonInput.get("QueueName").isJsonNull()) {
					inserObj.addProperty("QueueName", jsonInput.get("QueueName").getAsString());
				} else {
					inserObj.addProperty("QueueName", "");
				}

				if (jsonInput.has("Monday") && !jsonInput.get("Monday").isJsonNull()) {
					inserObj.addProperty("Monday", jsonInput.get("Monday").getAsString());
				} else {
					inserObj.addProperty("Monday", "");
				}

				if (jsonInput.has("Tuesday") && !jsonInput.get("Tuesday").isJsonNull()) {
					inserObj.addProperty("Tuesday", jsonInput.get("Tuesday").getAsString());
				} else {
					inserObj.addProperty("Tuesday", "");
				}

				if (jsonInput.has("Wednesday") && !jsonInput.get("Wednesday").isJsonNull()) {
					inserObj.addProperty("Wednesday", jsonInput.get("Wednesday").getAsString());
				} else {
					inserObj.addProperty("Wednesday", "");
				}

				if (jsonInput.has("Thursday") && !jsonInput.get("Thursday").isJsonNull()) {
					inserObj.addProperty("Thursday", jsonInput.get("Thursday").getAsString());
				} else {
					inserObj.addProperty("Thursday", "");
				}

				if (jsonInput.has("Friday") && !jsonInput.get("Friday").isJsonNull()) {
					inserObj.addProperty("Friday", jsonInput.get("Friday").getAsString());
				} else {
					inserObj.addProperty("Friday", "");
				}

				if (jsonInput.has("Saturday") && !jsonInput.get("Saturday").isJsonNull()) {
					inserObj.addProperty("Saturday", jsonInput.get("Saturday").getAsString());
				} else {
					inserObj.addProperty("Saturday", "");
				}

				if (jsonInput.has("Sunday") && !jsonInput.get("Sunday").isJsonNull()) {
					inserObj.addProperty("Sunday", jsonInput.get("Sunday").getAsString());
				} else {
					inserObj.addProperty("Sunday", "");
				}

				

				if (jsonInput.has("T00_00") && !jsonInput.get("T00_00").isJsonNull()) {
					inserObj.addProperty("T00_00", jsonInput.get("T00_00").getAsString());
				} else {
					inserObj.addProperty("T00_00", "");
				}

				if (jsonInput.has("T00_30") && !jsonInput.get("T00_30").isJsonNull()) {
					inserObj.addProperty("T00_30", jsonInput.get("T00_30").getAsString());
				} else {
					inserObj.addProperty("T00_30", "");
				}
				
				if (jsonInput.has("T01_00") && !jsonInput.get("T01_00").isJsonNull()) {
					inserObj.addProperty("T01_00", jsonInput.get("T01_00").getAsString());
				} else {
					inserObj.addProperty("T01_00", "");
				}

				if (jsonInput.has("T01_30") && !jsonInput.get("T01_30").isJsonNull()) {
					inserObj.addProperty("T01_30", jsonInput.get("T01_30").getAsString());
				} else {
					inserObj.addProperty("T01_30", "");
				}
				
				if (jsonInput.has("T02_00") && !jsonInput.get("T02_00").isJsonNull()) {
					inserObj.addProperty("T02_00", jsonInput.get("T02_00").getAsString());
				} else {
					inserObj.addProperty("T02_00", "");
				}
				if (jsonInput.has("T02_30") && !jsonInput.get("T02_30").isJsonNull()) {
					inserObj.addProperty("T02_30", jsonInput.get("T02_30").getAsString());
				} else {
					inserObj.addProperty("T02_30", "");
				}
				
				if (jsonInput.has("T03_00") && !jsonInput.get("T03_00").isJsonNull()) {
					inserObj.addProperty("T03_00", jsonInput.get("T03_00").getAsString());
				} else {
					inserObj.addProperty("T03_00", "");
				}
				if (jsonInput.has("T03_30") && !jsonInput.get("T03_30").isJsonNull()) {
					inserObj.addProperty("T03_30", jsonInput.get("T03_30").getAsString());
				} else {
					inserObj.addProperty("T03_30", "");
				}
				
				if (jsonInput.has("T04_00") && !jsonInput.get("T04_00").isJsonNull()) {
					inserObj.addProperty("T04_00", jsonInput.get("T04_00").getAsString());
				} else {
					inserObj.addProperty("T04_00", "");
				}
				if (jsonInput.has("T04_30") && !jsonInput.get("T04_30").isJsonNull()) {
					inserObj.addProperty("T04_30", jsonInput.get("T04_30").getAsString());
				} else {
					inserObj.addProperty("T04_30", "");
				}
				
				if (jsonInput.has("T05_00") && !jsonInput.get("T05_00").isJsonNull()) {
					inserObj.addProperty("T05_00", jsonInput.get("T05_00").getAsString());
				} else {
					inserObj.addProperty("T05_00", "");
				}
				if (jsonInput.has("T05_30") && !jsonInput.get("T05_30").isJsonNull()) {
					inserObj.addProperty("T05_30", jsonInput.get("T05_30").getAsString());
				} else {
					inserObj.addProperty("T05_30", "");
				}
				
				if (jsonInput.has("T06_00") && !jsonInput.get("T06_00").isJsonNull()) {
					inserObj.addProperty("T06_00", jsonInput.get("T06_00").getAsString());
				} else {
					inserObj.addProperty("T06_00", "");
				}
				if (jsonInput.has("T06_30") && !jsonInput.get("T06_30").isJsonNull()) {
					inserObj.addProperty("T06_30", jsonInput.get("T06_30").getAsString());
				} else {
					inserObj.addProperty("T06_30", "");
				}
				
				if (jsonInput.has("T07_00") && !jsonInput.get("T07_00").isJsonNull()) {
					inserObj.addProperty("T07_00", jsonInput.get("T07_00").getAsString());
				} else {
					inserObj.addProperty("T07_00", "");
				}
				if (jsonInput.has("T07_30") && !jsonInput.get("T07_30").isJsonNull()) {
					inserObj.addProperty("T07_30", jsonInput.get("T07_30").getAsString());
				} else {
					inserObj.addProperty("T07_30", "");
				}
				
				if (jsonInput.has("T08_00") && !jsonInput.get("T08_00").isJsonNull()) {
					inserObj.addProperty("T08_00", jsonInput.get("T08_00").getAsString());
				} else {
					inserObj.addProperty("T08_00", "");
				}
				if (jsonInput.has("T08_30") && !jsonInput.get("T08_30").isJsonNull()) {
					inserObj.addProperty("T08_30", jsonInput.get("T08_30").getAsString());
				} else {
					inserObj.addProperty("T08_30", "");
				}
				
				if (jsonInput.has("T09_00") && !jsonInput.get("T09_00").isJsonNull()) {
					inserObj.addProperty("T09_00", jsonInput.get("T09_00").getAsString());
				} else {
					inserObj.addProperty("T09_00", "");
				}
				if (jsonInput.has("T09_30") && !jsonInput.get("T09_30").isJsonNull()) {
					inserObj.addProperty("T09_30", jsonInput.get("T09_30").getAsString());
				} else {
					inserObj.addProperty("T09_30", "");
				}
				
				if (jsonInput.has("T10_00") && !jsonInput.get("T10_00").isJsonNull()) {
					inserObj.addProperty("T10_00", jsonInput.get("T10_00").getAsString());
				} else {
					inserObj.addProperty("T10_00", "");
				}
				if (jsonInput.has("T10_30") && !jsonInput.get("T10_30").isJsonNull()) {
					inserObj.addProperty("T10_30", jsonInput.get("T10_30").getAsString());
				} else {
					inserObj.addProperty("T10_30", "");
				}
				
				if (jsonInput.has("T11_00") && !jsonInput.get("T11_00").isJsonNull()) {
					inserObj.addProperty("T11_00", jsonInput.get("T11_00").getAsString());
				} else {
					inserObj.addProperty("T11_00", "");
				}
				if (jsonInput.has("T11_30") && !jsonInput.get("T11_30").isJsonNull()) {
					inserObj.addProperty("T11_30", jsonInput.get("T11_30").getAsString());
				} else {
					inserObj.addProperty("T11_30", "");
				}
				
				if (jsonInput.has("T12_00") && !jsonInput.get("T12_00").isJsonNull()) {
					inserObj.addProperty("T12_00", jsonInput.get("T12_00").getAsString());
				} else {
					inserObj.addProperty("T12_00", "");
				}
				if (jsonInput.has("T12_30") && !jsonInput.get("T12_30").isJsonNull()) {
					inserObj.addProperty("T12_30", jsonInput.get("T12_30").getAsString());
				} else {
					inserObj.addProperty("T12_30", "");
				}
				
				if (jsonInput.has("T13_00") && !jsonInput.get("T13_00").isJsonNull()) {
					inserObj.addProperty("T13_00", jsonInput.get("T13_00").getAsString());
				} else {
					inserObj.addProperty("T13_00", "");
				}
				if (jsonInput.has("T13_30") && !jsonInput.get("T13_30").isJsonNull()) {
					inserObj.addProperty("T13_30", jsonInput.get("T13_30").getAsString());
				} else {
					inserObj.addProperty("T13_30", "");
				}
				
				
				if (jsonInput.has("T14_00") && !jsonInput.get("T14_00").isJsonNull()) {
					inserObj.addProperty("T14_00", jsonInput.get("T14_00").getAsString());
				} else {
					inserObj.addProperty("T14_00", "");
				}
				if (jsonInput.has("T14_30") && !jsonInput.get("T14_30").isJsonNull()) {
					inserObj.addProperty("T14_30", jsonInput.get("T14_30").getAsString());
				} else {
					inserObj.addProperty("T14_30", "");
				}
				
				if (jsonInput.has("T15_00") && !jsonInput.get("T15_00").isJsonNull()) {
					inserObj.addProperty("T15_00", jsonInput.get("T15_00").getAsString());
				} else {
					inserObj.addProperty("T15_00", "");
				}
				if (jsonInput.has("T15_30") && !jsonInput.get("T15_30").isJsonNull()) {
					inserObj.addProperty("T15_30", jsonInput.get("T15_30").getAsString());
				} else {
					inserObj.addProperty("T15_30", "");
				}
				
				
				if (jsonInput.has("T16_00") && !jsonInput.get("T16_00").isJsonNull()) {
					inserObj.addProperty("T16_00", jsonInput.get("T16_00").getAsString());
				} else {
					inserObj.addProperty("T16_00", "");
				}
				if (jsonInput.has("T16_30") && !jsonInput.get("T16_30").isJsonNull()) {
					inserObj.addProperty("T16_30", jsonInput.get("T16_30").getAsString());
				} else {
					inserObj.addProperty("T16_30", "");
				}
				
				if (jsonInput.has("T17_00") && !jsonInput.get("T17_00").isJsonNull()) {
					inserObj.addProperty("T17_00", jsonInput.get("T17_00").getAsString());
				} else {
					inserObj.addProperty("T17_00", "");
				}
				if (jsonInput.has("T17_30") && !jsonInput.get("T17_30").isJsonNull()) {
					inserObj.addProperty("T17_30", jsonInput.get("T17_30").getAsString());
				} else {
					inserObj.addProperty("T17_30", "");
				}
				
				if (jsonInput.has("T18_00") && !jsonInput.get("T18_00").isJsonNull()) {
					inserObj.addProperty("T18_00", jsonInput.get("T18_00").getAsString());
				} else {
					inserObj.addProperty("T18_00", "");
				}
				if (jsonInput.has("T18_30") && !jsonInput.get("T18_30").isJsonNull()) {
					inserObj.addProperty("T18_30", jsonInput.get("T18_30").getAsString());
				} else {
					inserObj.addProperty("T18_30", "");
				}
				
				if (jsonInput.has("T19_00") && !jsonInput.get("T19_00").isJsonNull()) {
					inserObj.addProperty("T19_00", jsonInput.get("T19_00").getAsString());
				} else {
					inserObj.addProperty("T19_00", "");
				}
				if (jsonInput.has("T19_30") && !jsonInput.get("T19_30").isJsonNull()) {
					inserObj.addProperty("T19_30", jsonInput.get("T19_30").getAsString());
				} else {
					inserObj.addProperty("T19_30", "");
				}
				
				if (jsonInput.has("T20_00") && !jsonInput.get("T20_00").isJsonNull()) {
					inserObj.addProperty("T20_00", jsonInput.get("T20_00").getAsString());
				} else {
					inserObj.addProperty("T20_00", "");
				}
				if (jsonInput.has("T20_30") && !jsonInput.get("T20_30").isJsonNull()) {
					inserObj.addProperty("T20_30", jsonInput.get("T20_30").getAsString());
				} else {
					inserObj.addProperty("T20_30", "");
				}
				
				if (jsonInput.has("T21_00") && !jsonInput.get("T22_00").isJsonNull()) {
					inserObj.addProperty("T21_00", jsonInput.get("T21_00").getAsString());
				} else {
					inserObj.addProperty("T21_00", "");
				}
				if (jsonInput.has("T21_30") && !jsonInput.get("T21_30").isJsonNull()) {
					inserObj.addProperty("T21_30", jsonInput.get("T21_30").getAsString());
				} else {
					inserObj.addProperty("T21_30", "");
				}
				
				if (jsonInput.has("T22_00") && !jsonInput.get("T22_00").isJsonNull()) {
					inserObj.addProperty("T22_00", jsonInput.get("T21_00").getAsString());
				} else {
					inserObj.addProperty("T22_00", "");
				}
				
				if (jsonInput.has("T22_30") && !jsonInput.get("T22_30").isJsonNull()) {
					inserObj.addProperty("T22_30", jsonInput.get("T22_30").getAsString());
				} else {
					inserObj.addProperty("T22_30", "");
				}
				
				if (jsonInput.has("T23_00") && !jsonInput.get("T23_00").isJsonNull()) {
					inserObj.addProperty("T23_00", jsonInput.get("T23_00").getAsString());
				} else {
					inserObj.addProperty("T23_00", "");
				}
				if (jsonInput.has("T23_30") && !jsonInput.get("T23_30").isJsonNull()) {
					inserObj.addProperty("T23_30", jsonInput.get("T23_30").getAsString());
				} else {
					inserObj.addProperty("T23_30", "");
				}
				
				if (jsonInput.has("Input") && !jsonInput.get("Input").isJsonNull()) {
					inserObj.addProperty("Input", jsonInput.get("Input").getAsString());
				} else {
					inserObj.addProperty("Input", "");
				}
				
				if (jsonInput.has("TargetURL") && !jsonInput.get("TargetURL").isJsonNull()) {
					inserObj.addProperty("TargetURL", jsonInput.get("TargetURL").getAsString());
				} else {
					inserObj.addProperty("TargetURL", "");
				}
				
				if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
					inserObj.addProperty("Source", jsonInput.get("Source").getAsString());
				} else {
					inserObj.addProperty("Source", "");
				}
				
				if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
					inserObj.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
				} else {
					inserObj.addProperty("SourceReferenceID", "");
				}
				
				executeUrl = oDataUrl + "MISScheduler";
				if (debug) {
					response.getWriter().println("executeUrl:" + executeUrl);
					response.getWriter().println("input payload:" + inserObj);
				}
				JsonObject insertedObj = commonUtils.executePostURL(executeUrl, userpass, response, inserObj, request, debug);
				response.getWriter().println(insertedObj);
			} else {
				resObj.addProperty("Message", "Empty input payload");
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
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			}
			resObj.addProperty("Message", ex.getClass().getCanonicalName() + "---->" + buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject resObj=new JsonObject(); 
        CommonUtils commonUtils=new CommonUtils();
        String password="",username="",oDataUrl="",userpass="",executeUrl="";
        boolean debug=false;
        JsonParser parser=new JsonParser();
        JsonObject insertObj=new JsonObject();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
				userpass = username + ":" + password;
				JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("received input Payload:" + jsonInput);
					response.getWriter().println("oDataUrl" + oDataUrl);
				}
				
				if (jsonInput.has("ID") && !jsonInput.get("ID").isJsonNull()) {
					insertObj.addProperty("ID", jsonInput.get("ID").getAsString());
					
					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					String changedAt = commonUtils.getCreatedAtTime();
					long changedOn = commonUtils.getCreatedOnDate();
					insertObj.addProperty("ChangedBy", changedBy);
					insertObj.addProperty("ChangedAt", changedAt);
					insertObj.addProperty("ChangedOn", "/Date("+changedOn+")/");
					if (jsonInput.has("CreatedBy") && !jsonInput.get("CreatedBy").isJsonNull()) {
						insertObj.addProperty("CreatedBy", jsonInput.get("CreatedBy").getAsString());
					} 

					if (jsonInput.has("CreatedAt") && !jsonInput.get("CreatedAt").isJsonNull()) {
						insertObj.addProperty("CreatedAt", jsonInput.get("CreatedAt").getAsString());
					} 

					if (jsonInput.has("CreatedOn") && !jsonInput.get("CreatedOn").isJsonNull()) {
						insertObj.addProperty("CreatedOn", jsonInput.get("CreatedOn").getAsString());
					} 

					if (jsonInput.has("AggregatorID") && !jsonInput.get("AggregatorID").isJsonNull()) {
						insertObj.addProperty("AggregatorID", jsonInput.get("AggregatorID").getAsString());
					} 
					
					if (jsonInput.has("Report") && !jsonInput.get("Report").isJsonNull()) {
						insertObj.addProperty("Report", jsonInput.get("Report").getAsString());
					} 

					if (jsonInput.has("VariantID") && !jsonInput.get("VariantID").isJsonNull()) {
						insertObj.addProperty("VariantID", jsonInput.get("VariantID").getAsString());
					} 

					if (jsonInput.has("IsActive") && !jsonInput.get("IsActive").isJsonNull()) {
						insertObj.addProperty("IsActive", jsonInput.get("IsActive").getAsString());
					} 
					if (jsonInput.has("Periodicity") && !jsonInput.get("Periodicity").isJsonNull()) {
						insertObj.addProperty("Periodicity", jsonInput.get("Periodicity").getAsString());
					} 

					if (jsonInput.has("QueueName") && !jsonInput.get("QueueName").isJsonNull()) {
						insertObj.addProperty("QueueName", jsonInput.get("QueueName").getAsString());
					} 

					if (jsonInput.has("Monday") && !jsonInput.get("Monday").isJsonNull()) {
						insertObj.addProperty("Monday", jsonInput.get("Monday").getAsString());
					}

					if (jsonInput.has("Tuesday") && !jsonInput.get("Tuesday").isJsonNull()) {
						insertObj.addProperty("Tuesday", jsonInput.get("Tuesday").getAsString());
					} 

					if (jsonInput.has("Wednesday") && !jsonInput.get("Wednesday").isJsonNull()) {
						insertObj.addProperty("Wednesday", jsonInput.get("Wednesday").getAsString());
					} 

					if (jsonInput.has("Thursday") && !jsonInput.get("Thursday").isJsonNull()) {
						insertObj.addProperty("Thursday", jsonInput.get("Thursday").getAsString());
					} 

					if (jsonInput.has("Friday") && !jsonInput.get("Friday").isJsonNull()) {
						insertObj.addProperty("Friday", jsonInput.get("Friday").getAsString());
					} 

					if (jsonInput.has("Saturday") && !jsonInput.get("Saturday").isJsonNull()) {
						insertObj.addProperty("Saturday", jsonInput.get("Saturday").getAsString());
					} 

					if (jsonInput.has("Sunday") && !jsonInput.get("Sunday").isJsonNull()) {
						insertObj.addProperty("Sunday", jsonInput.get("Sunday").getAsString());
					} 

					

					if (jsonInput.has("T00_00") && !jsonInput.get("T00_00").isJsonNull()) {
						insertObj.addProperty("T00_00", jsonInput.get("T00_00").getAsString());
					}

					if (jsonInput.has("T00_30") && !jsonInput.get("T00_30").isJsonNull()) {
						insertObj.addProperty("T00_30", jsonInput.get("T00_30").getAsString());
					}
					
					if (jsonInput.has("T01_00") && !jsonInput.get("T01_00").isJsonNull()) {
						insertObj.addProperty("T01_00", jsonInput.get("T01_00").getAsString());
					} 

					if (jsonInput.has("T01_30") && !jsonInput.get("T01_30").isJsonNull()) {
						insertObj.addProperty("T01_30", jsonInput.get("T01_30").getAsString());
					} 
					
					if (jsonInput.has("T02_00") && !jsonInput.get("T02_00").isJsonNull()) {
						insertObj.addProperty("T02_00", jsonInput.get("T02_00").getAsString());
					} 
					if (jsonInput.has("T02_30") && !jsonInput.get("T02_30").isJsonNull()) {
						insertObj.addProperty("T02_30", jsonInput.get("T02_30").getAsString());
					} 
					
					if (jsonInput.has("T03_00") && !jsonInput.get("T03_00").isJsonNull()) {
						insertObj.addProperty("T03_00", jsonInput.get("T03_00").getAsString());
					} 
					if (jsonInput.has("T03_30") && !jsonInput.get("T03_30").isJsonNull()) {
						insertObj.addProperty("T03_30", jsonInput.get("T03_30").getAsString());
					} 
					
					if (jsonInput.has("T04_00") && !jsonInput.get("T04_00").isJsonNull()) {
						insertObj.addProperty("T04_00", jsonInput.get("T04_00").getAsString());
					} 
					if (jsonInput.has("T04_30") && !jsonInput.get("T04_30").isJsonNull()) {
						insertObj.addProperty("T04_30", jsonInput.get("T04_30").getAsString());
					} 
					
					if (jsonInput.has("T05_00") && !jsonInput.get("T05_00").isJsonNull()) {
						insertObj.addProperty("T05_00", jsonInput.get("T05_00").getAsString());
					} 
					if (jsonInput.has("T05_30") && !jsonInput.get("T05_30").isJsonNull()) {
						insertObj.addProperty("T05_30", jsonInput.get("T05_30").getAsString());
					} 
					
					if (jsonInput.has("T06_00") && !jsonInput.get("T06_00").isJsonNull()) {
						insertObj.addProperty("T06_00", jsonInput.get("T06_00").getAsString());
					} 
					if (jsonInput.has("T06_30") && !jsonInput.get("T06_30").isJsonNull()) {
						insertObj.addProperty("T06_30", jsonInput.get("T06_30").getAsString());
					} 
					
					if (jsonInput.has("T07_00") && !jsonInput.get("T07_00").isJsonNull()) {
						insertObj.addProperty("T07_00", jsonInput.get("T07_00").getAsString());
					} 
					if (jsonInput.has("T07_30") && !jsonInput.get("T07_30").isJsonNull()) {
						insertObj.addProperty("T07_30", jsonInput.get("T07_30").getAsString());
					} 
					
					if (jsonInput.has("T08_00") && !jsonInput.get("T08_00").isJsonNull()) {
						insertObj.addProperty("T08_00", jsonInput.get("T08_00").getAsString());
					} 
					if (jsonInput.has("T08_30") && !jsonInput.get("T08_30").isJsonNull()) {
						insertObj.addProperty("T08_30", jsonInput.get("T08_30").getAsString());
					} 
					
					if (jsonInput.has("T09_00") && !jsonInput.get("T09_00").isJsonNull()) {
						insertObj.addProperty("T09_00", jsonInput.get("T09_00").getAsString());
					} 
					if (jsonInput.has("T09_30") && !jsonInput.get("T09_30").isJsonNull()) {
						insertObj.addProperty("T09_30", jsonInput.get("T09_30").getAsString());
					} 
					
					if (jsonInput.has("T10_00") && !jsonInput.get("T10_00").isJsonNull()) {
						insertObj.addProperty("T10_00", jsonInput.get("T10_00").getAsString());
					} 
					if (jsonInput.has("T10_30") && !jsonInput.get("T10_30").isJsonNull()) {
						insertObj.addProperty("T10_30", jsonInput.get("T10_30").getAsString());
					} 
					
					if (jsonInput.has("T11_00") && !jsonInput.get("T11_00").isJsonNull()) {
						insertObj.addProperty("T11_00", jsonInput.get("T11_00").getAsString());
					} 
					if (jsonInput.has("T11_30") && !jsonInput.get("T11_30").isJsonNull()) {
						insertObj.addProperty("T11_30", jsonInput.get("T11_30").getAsString());
					} 
					
					if (jsonInput.has("T12_00") && !jsonInput.get("T12_00").isJsonNull()) {
						insertObj.addProperty("T12_00", jsonInput.get("T12_00").getAsString());
					} 
					if (jsonInput.has("T12_30") && !jsonInput.get("T12_30").isJsonNull()) {
						insertObj.addProperty("T12_30", jsonInput.get("T12_30").getAsString());
					} 
					
					if (jsonInput.has("T13_00") && !jsonInput.get("T13_00").isJsonNull()) {
						insertObj.addProperty("T13_00", jsonInput.get("T13_00").getAsString());
					} 
					if (jsonInput.has("T13_30") && !jsonInput.get("T13_30").isJsonNull()) {
						insertObj.addProperty("T13_30", jsonInput.get("T13_30").getAsString());
					} 
					
					
					if (jsonInput.has("T14_00") && !jsonInput.get("T14_00").isJsonNull()) {
						insertObj.addProperty("T14_00", jsonInput.get("T14_00").getAsString());
					} 
					if (jsonInput.has("T14_30") && !jsonInput.get("T14_30").isJsonNull()) {
						insertObj.addProperty("T14_30", jsonInput.get("T14_30").getAsString());
					} 
					
					if (jsonInput.has("T15_00") && !jsonInput.get("T15_00").isJsonNull()) {
						insertObj.addProperty("T15_00", jsonInput.get("T15_00").getAsString());
					} 
					if (jsonInput.has("T15_30") && !jsonInput.get("T15_30").isJsonNull()) {
						insertObj.addProperty("T15_30", jsonInput.get("T15_30").getAsString());
					} 
					
					
					if (jsonInput.has("T16_00") && !jsonInput.get("T16_00").isJsonNull()) {
						insertObj.addProperty("T16_00", jsonInput.get("T16_00").getAsString());
					} 
					if (jsonInput.has("T16_30") && !jsonInput.get("T16_30").isJsonNull()) {
						insertObj.addProperty("T16_30", jsonInput.get("T16_30").getAsString());
					}
					
					if (jsonInput.has("T17_00") && !jsonInput.get("T17_00").isJsonNull()) {
						insertObj.addProperty("T17_00", jsonInput.get("T17_00").getAsString());
					} 
					if (jsonInput.has("T17_30") && !jsonInput.get("T17_30").isJsonNull()) {
						insertObj.addProperty("T17_30", jsonInput.get("T17_30").getAsString());
					} 
					
					if (jsonInput.has("T18_00") && !jsonInput.get("T18_00").isJsonNull()) {
						insertObj.addProperty("T18_00", jsonInput.get("T18_00").getAsString());
					} 
					if (jsonInput.has("T18_30") && !jsonInput.get("T18_30").isJsonNull()) {
						insertObj.addProperty("T18_30", jsonInput.get("T18_30").getAsString());
					} 
					
					if (jsonInput.has("T19_00") && !jsonInput.get("T19_00").isJsonNull()) {
						insertObj.addProperty("T19_00", jsonInput.get("T19_00").getAsString());
					} 
					if (jsonInput.has("T19_30") && !jsonInput.get("T19_30").isJsonNull()) {
						insertObj.addProperty("T19_30", jsonInput.get("T19_30").getAsString());
					} 
					
					if (jsonInput.has("T20_00") && !jsonInput.get("T20_00").isJsonNull()) {
						insertObj.addProperty("T20_00", jsonInput.get("T20_00").getAsString());
					} 
					if (jsonInput.has("T20_30") && !jsonInput.get("T20_30").isJsonNull()) {
						insertObj.addProperty("T20_30", jsonInput.get("T20_30").getAsString());
					} 
					
					if (jsonInput.has("T21_00") && !jsonInput.get("T22_00").isJsonNull()) {
						insertObj.addProperty("T21_00", jsonInput.get("T21_00").getAsString());
					} 
					if (jsonInput.has("T21_30") && !jsonInput.get("T21_30").isJsonNull()) {
						insertObj.addProperty("T21_30", jsonInput.get("T21_30").getAsString());
					} 
					
					if (jsonInput.has("T22_00") && !jsonInput.get("T22_00").isJsonNull()) {
						insertObj.addProperty("T22_00", jsonInput.get("T21_00").getAsString());
					} 
					
					if (jsonInput.has("T22_30") && !jsonInput.get("T22_30").isJsonNull()) {
						insertObj.addProperty("T22_30", jsonInput.get("T22_30").getAsString());
					} 
					
					if (jsonInput.has("T23_00") && !jsonInput.get("T23_00").isJsonNull()) {
						insertObj.addProperty("T23_00", jsonInput.get("T23_00").getAsString());
					} 
					if (jsonInput.has("T23_30") && !jsonInput.get("T23_30").isJsonNull()) {
						insertObj.addProperty("T23_30", jsonInput.get("T23_30").getAsString());
					} 
					
					if (jsonInput.has("Input") && !jsonInput.get("Input").isJsonNull()) {
						insertObj.addProperty("Input", jsonInput.get("Input").getAsString());
					} 
					
					if (jsonInput.has("TargetURL") && !jsonInput.get("TargetURL").isJsonNull()) {
						insertObj.addProperty("TargetURL", jsonInput.get("TargetURL").getAsString());
					} 
					
					if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
						insertObj.addProperty("Source", jsonInput.get("Source").getAsString());
					} 
					
					if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
						insertObj.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
					} 
					
					executeUrl = oDataUrl + "MISScheduler('"+jsonInput.get("ID").getAsString()+"')";
					if (debug) {
						response.getWriter().println("executeUrl:" + executeUrl);
						response.getWriter().println("input payload:" + insertObj);
					}
					JsonObject insertedObj=commonUtils.executeUpdate(executeUrl, userpass, response, insertObj, request, debug);
					response.getWriter().println(insertedObj);
				}else{
					resObj.addProperty("Message", "ID missing in the input payload");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);	
				}
			} else {
				resObj.addProperty("Message", "Empty input payload");
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
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			}
			resObj.addProperty("Message", ex.getClass().getCanonicalName() + "---->" + buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		}
	

	}
}
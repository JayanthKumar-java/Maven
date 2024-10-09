package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EventDestinations extends HttpServlet{
	

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
					response.getWriter().println("filters:" + filters);
				}
				filters=filters.replaceAll(" ", "%20").replaceAll("'", "%27");
				if (debug) {
					response.getWriter().println("Odata filters:" + filters);
				}
				executeURL = oDataUrl+"EventDestinations?$filter=" + filters;
			} else {
				executeURL = oDataUrl +"EventDestinations";
			}
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject eventDestObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("eventDestObj:" + eventDestObj);
			}
			if (eventDestObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (eventDestObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.add("EventDestinations", eventDestObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
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
				response.getWriter().println(eventDestObj);
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
				
				if (jsonInput.has("Object") && !jsonInput.get("Object").isJsonNull()) {
					inserObj.addProperty("Object", jsonInput.get("Object").getAsString());
				} else {
					inserObj.addProperty("Object", "");
				}
				
				if (jsonInput.has("Event") && !jsonInput.get("Event").isJsonNull()) {
					inserObj.addProperty("Event", jsonInput.get("Event").getAsString());
				} else {
					inserObj.addProperty("Event", "");
				}

				if (jsonInput.has("Destination") && !jsonInput.get("Destination").isJsonNull()) {
					inserObj.addProperty("Destination", jsonInput.get("Destination").getAsString());
				} else {
					inserObj.addProperty("Destination", "");
				}

				if (jsonInput.has("Description") && !jsonInput.get("Description").isJsonNull()) {
					inserObj.addProperty("Description", jsonInput.get("Description").getAsString());
				} else {
					inserObj.addProperty("Description", "");
				}
				if (jsonInput.has("DestinationType") && !jsonInput.get("DestinationType").isJsonNull()) {
					inserObj.addProperty("DestinationType", jsonInput.get("DestinationType").getAsString());
				} else {
					inserObj.addProperty("DestinationType", "");
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
				
				executeUrl = oDataUrl + "EventDestinations";
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

					if (jsonInput.has("Object") && !jsonInput.get("Object").isJsonNull()) {
						insertObj.addProperty("Object", jsonInput.get("Object").getAsString());
					} 
					
					if (jsonInput.has("Event") && !jsonInput.get("Event").isJsonNull()) {
						insertObj.addProperty("Event", jsonInput.get("Event").getAsString());
					} 

					if (jsonInput.has("Destination") && !jsonInput.get("Destination").isJsonNull()) {
						insertObj.addProperty("Destination", jsonInput.get("Destination").getAsString());
					}

					if (jsonInput.has("Description") && !jsonInput.get("Description").isJsonNull()) {
						insertObj.addProperty("Description", jsonInput.get("Description").getAsString());
					} 
					if (jsonInput.has("DestinationType") && !jsonInput.get("DestinationType").isJsonNull()) {
						insertObj.addProperty("DestinationType", jsonInput.get("DestinationType").getAsString());
					} 

					if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
						insertObj.addProperty("Source", jsonInput.get("Source").getAsString());
					} 
					
					if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
						insertObj.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
					}
					
					executeUrl = oDataUrl + "EventDestinations('"+jsonInput.get("ID").getAsString()+"')";
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

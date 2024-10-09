package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DFCNFG extends HttpServlet{

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
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
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
				executeURL = oDataUrl +"DFCNFG?$filter=" + filters;
			} else {
				executeURL = oDataUrl +"DFCNFG";
			}
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject rfcngObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("rfcngObj:" + rfcngObj);
			}
			if (rfcngObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (rfcngObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.add("DFCNFG", rfcngObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
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
				response.getWriter().println(rfcngObj);
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
        JsonObject dfcnfg=new JsonObject();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
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
				dfcnfg.addProperty("ID", guid);
				dfcnfg.addProperty("CreatedBy", createdBy);
				dfcnfg.addProperty("CreatedAt", createdAt);
				dfcnfg.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				
				if (jsonInput.has("AGGRID") && !jsonInput.get("AGGRID").isJsonNull()) {
					dfcnfg.addProperty("AGGRID", jsonInput.get("AGGRID").getAsString());
				} else {
					dfcnfg.addProperty("AGGRID", "");
				}
				
				if (jsonInput.has("CORPID") && !jsonInput.get("CORPID").isJsonNull()) {
					dfcnfg.addProperty("CORPID", jsonInput.get("CORPID").getAsString());
				} else {
					dfcnfg.addProperty("CORPID", "");
				}

				if (jsonInput.has("ProductCode") && !jsonInput.get("ProductCode").isJsonNull()) {
					dfcnfg.addProperty("ProductCode", jsonInput.get("ProductCode").getAsString());
				} else {
					dfcnfg.addProperty("ProductCode", "");
				}

				if (jsonInput.has("MessageSource") && !jsonInput.get("MessageSource").isJsonNull()) {
					dfcnfg.addProperty("MessageSource", jsonInput.get("MessageSource").getAsString());
				} else {
					dfcnfg.addProperty("MessageSource", "");
				}
				if (jsonInput.has("MapCode") && !jsonInput.get("MapCode").isJsonNull()) {
					dfcnfg.addProperty("MapCode", jsonInput.get("MapCode").getAsString());
				} else {
					dfcnfg.addProperty("MapCode", "");
				}

				if (jsonInput.has("UserData") && !jsonInput.get("UserData").isJsonNull()) {
					dfcnfg.addProperty("UserData", jsonInput.get("UserData").getAsString());
				} else {
					dfcnfg.addProperty("UserData", "");
				}

				if (jsonInput.has("ApprovalRequired") && !jsonInput.get("ApprovalRequired").isJsonNull()) {
					dfcnfg.addProperty("ApprovalRequired", jsonInput.get("ApprovalRequired").getAsString());
				} else {
					dfcnfg.addProperty("ApprovalRequired", "");
				}

				if (jsonInput.has("SystemID") && !jsonInput.get("SystemID").isJsonNull()) {
					dfcnfg.addProperty("SystemID", jsonInput.get("SystemID").getAsString());
				} else {
					dfcnfg.addProperty("SystemID", "");
				}

				if (jsonInput.has("AggregatorDivision") && !jsonInput.get("AggregatorDivision").isJsonNull()) {
					dfcnfg.addProperty("AggregatorDivision", jsonInput.get("AggregatorDivision").getAsString());
				} else {
					dfcnfg.addProperty("AggregatorDivision", "");
				}

				if (jsonInput.has("SettlementReqd") && !jsonInput.get("SettlementReqd").isJsonNull()) {
					dfcnfg.addProperty("SettlementReqd", jsonInput.get("SettlementReqd").getAsString());
				} else {
					dfcnfg.addProperty("SettlementReqd", "");
				}

				if (jsonInput.has("CORMandatoryFields") && !jsonInput.get("CORMandatoryFields").isJsonNull()) {
					dfcnfg.addProperty("CORMandatoryFields", jsonInput.get("CORMandatoryFields").getAsString());
				} else {
					dfcnfg.addProperty("CORMandatoryFields", "");
				}

				if (jsonInput.has("PayTo") && !jsonInput.get("PayTo").isJsonNull()) {
					dfcnfg.addProperty("PayTo", jsonInput.get("PayTo").getAsString());
				} else {
					dfcnfg.addProperty("PayTo", "");
				}

				if (jsonInput.has("ClientCode") && !jsonInput.get("ClientCode").isJsonNull()) {
					dfcnfg.addProperty("ClientCode", jsonInput.get("ClientCode").getAsString());
				} else {
					dfcnfg.addProperty("ClientCode", "");
				}

				if (jsonInput.has("ApprovalGroupID") && !jsonInput.get("ApprovalGroupID").isJsonNull()) {
					dfcnfg.addProperty("ApprovalGroupID", jsonInput.get("ApprovalGroupID").getAsString());
				} else {
					dfcnfg.addProperty("ApprovalGroupID", "");
				}

				if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
					dfcnfg.addProperty("Source", jsonInput.get("Source").getAsString());
				} else {
					dfcnfg.addProperty("Source", "");
				}

				if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
					dfcnfg.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
				} else {
					dfcnfg.addProperty("SourceReferenceID", "");
				}
				executeUrl = oDataUrl + "DFCNFG";
				if (debug) {
					response.getWriter().println("executeUrl:" + executeUrl);
					response.getWriter().println("input payload:" + dfcnfg);
				}
				JsonObject insertedObj = commonUtils.executePostURL(executeUrl, userpass, response, dfcnfg, request, debug);
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
        JsonObject dfcnfg=new JsonObject();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
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
					dfcnfg.addProperty("ID", jsonInput.get("ID").getAsString());
					
					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					String changedAt = commonUtils.getCreatedAtTime();
					long changedOn = commonUtils.getCreatedOnDate();
					dfcnfg.addProperty("ChangedBy", changedBy);
					dfcnfg.addProperty("ChangedAt", changedAt);
					dfcnfg.addProperty("ChangedOn", "/Date("+changedOn+")/");
					if (jsonInput.has("CreatedBy") && !jsonInput.get("CreatedBy").isJsonNull()) {
						dfcnfg.addProperty("CreatedBy", jsonInput.get("CreatedBy").getAsString());
					} 

					if (jsonInput.has("CreatedAt") && !jsonInput.get("CreatedAt").isJsonNull()) {
						dfcnfg.addProperty("CreatedAt", jsonInput.get("CreatedAt").getAsString());
					} 

					if (jsonInput.has("CreatedOn") && !jsonInput.get("CreatedOn").isJsonNull()) {
						dfcnfg.addProperty("CreatedOn", jsonInput.get("CreatedOn").getAsString());
					} 

					if (jsonInput.has("AGGRID") && !jsonInput.get("AGGRID").isJsonNull()) {
						dfcnfg.addProperty("AGGRID", jsonInput.get("AGGRID").getAsString());
					} 

					if (jsonInput.has("CORPID") && !jsonInput.get("CORPID").isJsonNull()) {
						dfcnfg.addProperty("CORPID", jsonInput.get("CORPID").getAsString());
					} 

					if (jsonInput.has("ProductCode") && !jsonInput.get("ProductCode").isJsonNull()) {
						dfcnfg.addProperty("ProductCode", jsonInput.get("ProductCode").getAsString());
					} 

					if (jsonInput.has("MessageSource") && !jsonInput.get("MessageSource").isJsonNull()) {
						dfcnfg.addProperty("MessageSource", jsonInput.get("MessageSource").getAsString());
					} 
					if (jsonInput.has("MapCode") && !jsonInput.get("MapCode").isJsonNull()) {
						dfcnfg.addProperty("MapCode", jsonInput.get("MapCode").getAsString());
					} 

					if (jsonInput.has("UserData") && !jsonInput.get("UserData").isJsonNull()) {
						dfcnfg.addProperty("UserData", jsonInput.get("UserData").getAsString());
					} 

					if (jsonInput.has("ApprovalRequired") && !jsonInput.get("ApprovalRequired").isJsonNull()) {
						dfcnfg.addProperty("ApprovalRequired", jsonInput.get("ApprovalRequired").getAsString());
					} 

					if (jsonInput.has("SystemID") && !jsonInput.get("SystemID").isJsonNull()) {
						dfcnfg.addProperty("SystemID", jsonInput.get("SystemID").getAsString());
					} 

					if (jsonInput.has("AggregatorDivision") && !jsonInput.get("AggregatorDivision").isJsonNull()) {
						dfcnfg.addProperty("AggregatorDivision", jsonInput.get("AggregatorDivision").getAsString());
					} 

					if (jsonInput.has("SettlementReqd") && !jsonInput.get("SettlementReqd").isJsonNull()) {
						dfcnfg.addProperty("SettlementReqd", jsonInput.get("SettlementReqd").getAsString());
					} 

					if (jsonInput.has("CORMandatoryFields") && !jsonInput.get("CORMandatoryFields").isJsonNull()) {
						dfcnfg.addProperty("CORMandatoryFields", jsonInput.get("CORMandatoryFields").getAsString());
					} 

					if (jsonInput.has("PayTo") && !jsonInput.get("PayTo").isJsonNull()) {
						dfcnfg.addProperty("PayTo", jsonInput.get("PayTo").getAsString());
					} 

					if (jsonInput.has("ClientCode") && !jsonInput.get("ClientCode").isJsonNull()) {
						dfcnfg.addProperty("ClientCode", jsonInput.get("ClientCode").getAsString());
					} 

					if (jsonInput.has("ApprovalGroupID") && !jsonInput.get("ApprovalGroupID").isJsonNull()) {
						dfcnfg.addProperty("ApprovalGroupID", jsonInput.get("ApprovalGroupID").getAsString());
					} 

					if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
						dfcnfg.addProperty("Source", jsonInput.get("Source").getAsString());
					} 

					if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
						dfcnfg.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
					} 
					executeUrl = oDataUrl + "DFCNFG('"+jsonInput.get("ID").getAsString()+"')";
					if (debug) {
						response.getWriter().println("executeUrl:" + executeUrl);
						response.getWriter().println("input payload:" + dfcnfg);
					}
					JsonObject insertedObj=commonUtils.executeUpdate(executeUrl, userpass, response, dfcnfg, request, debug);
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

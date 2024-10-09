package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class PFCNFG extends HttpServlet {

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
				executeURL = oDataUrl +"PFCNFG?$filter=" + filters;
			} else {
				executeURL = oDataUrl +"PFCNFG";
			}
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject pfcngObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("pfcngObj:" + pfcngObj);
			}
			if (pfcngObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (pfcngObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.add("PFCNFG", pfcngObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray());
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
				response.getWriter().println(pfcngObj);
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
        JsonObject pfcngf=new JsonObject();
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
				pfcngf.addProperty("ID", guid);
				pfcngf.addProperty("CreatedBy", createdBy);
				pfcngf.addProperty("CreatedAt", createdAt);
				pfcngf.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				pfcngf.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				
				if (jsonInput.has("AGGRID") && !jsonInput.get("AGGRID").isJsonNull()) {
					pfcngf.addProperty("AGGRID", jsonInput.get("AGGRID").getAsString());
				} else {
					pfcngf.addProperty("AGGRID", "");
				}
				
				if (jsonInput.has("CORPID") && !jsonInput.get("CORPID").isJsonNull()) {
					pfcngf.addProperty("CORPID", jsonInput.get("CORPID").getAsString());
				} else {
					pfcngf.addProperty("CORPID", "");
				}

				if (jsonInput.has("ProductCode") && !jsonInput.get("ProductCode").isJsonNull()) {
					pfcngf.addProperty("ProductCode", jsonInput.get("ProductCode").getAsString());
				} else {
					pfcngf.addProperty("ProductCode", "");
				}

				if (jsonInput.has("MessageSource") && !jsonInput.get("MessageSource").isJsonNull()) {
					pfcngf.addProperty("MessageSource", jsonInput.get("MessageSource").getAsString());
				} else {
					pfcngf.addProperty("MessageSource", "");
				}
				if (jsonInput.has("MapCode") && !jsonInput.get("MapCode").isJsonNull()) {
					pfcngf.addProperty("MapCode", jsonInput.get("MapCode").getAsString());
				} else {
					pfcngf.addProperty("MapCode", "");
				}

				if (jsonInput.has("UserData") && !jsonInput.get("UserData").isJsonNull()) {
					pfcngf.addProperty("UserData", jsonInput.get("UserData").getAsString());
				} else {
					pfcngf.addProperty("UserData", "");
				}

				if (jsonInput.has("ApprovalRequired") && !jsonInput.get("ApprovalRequired").isJsonNull()) {
					pfcngf.addProperty("ApprovalRequired", jsonInput.get("ApprovalRequired").getAsString());
				} else {
					pfcngf.addProperty("ApprovalRequired", "");
				}

				if (jsonInput.has("SystemID") && !jsonInput.get("SystemID").isJsonNull()) {
					pfcngf.addProperty("SystemID", jsonInput.get("SystemID").getAsString());
				} else {
					pfcngf.addProperty("SystemID", "");
				}

				if (jsonInput.has("AggregatorDivision") && !jsonInput.get("AggregatorDivision").isJsonNull()) {
					pfcngf.addProperty("AggregatorDivision", jsonInput.get("AggregatorDivision").getAsString());
				} else {
					pfcngf.addProperty("AggregatorDivision", "");
				}

				if (jsonInput.has("SettlementReqd") && !jsonInput.get("SettlementReqd").isJsonNull()) {
					pfcngf.addProperty("SettlementReqd", jsonInput.get("SettlementReqd").getAsString());
				} else {
					pfcngf.addProperty("SettlementReqd", "");
				}

				if (jsonInput.has("CORMandatoryFields") && !jsonInput.get("CORMandatoryFields").isJsonNull()) {
					pfcngf.addProperty("CORMandatoryFields", jsonInput.get("CORMandatoryFields").getAsString());
				} else {
					pfcngf.addProperty("CORMandatoryFields", "");
				}

				if (jsonInput.has("PayTo") && !jsonInput.get("PayTo").isJsonNull()) {
					pfcngf.addProperty("PayTo", jsonInput.get("PayTo").getAsString());
				} else {
					pfcngf.addProperty("PayTo", "");
				}

				if (jsonInput.has("ClientCode") && !jsonInput.get("ClientCode").isJsonNull()) {
					pfcngf.addProperty("ClientCode", jsonInput.get("ClientCode").getAsString());
				} else {
					pfcngf.addProperty("ClientCode", "");
				}

				if (jsonInput.has("ApprovalGroupID") && !jsonInput.get("ApprovalGroupID").isJsonNull()) {
					pfcngf.addProperty("ApprovalGroupID", jsonInput.get("ApprovalGroupID").getAsString());
				} else {
					pfcngf.addProperty("ApprovalGroupID", "");
				}

				if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
					pfcngf.addProperty("Source", jsonInput.get("Source").getAsString());
				} else {
					pfcngf.addProperty("Source", "");
				}

				if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
					pfcngf.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
				} else {
					pfcngf.addProperty("SourceReferenceID", "");
				}
				executeUrl = oDataUrl + "PFCNFG";
				if (debug) {
					response.getWriter().println("executeUrl:" + executeUrl);
					response.getWriter().println("input payload:" + pfcngf);
				}
				JsonObject insertedObj = commonUtils.executePostURL(executeUrl, userpass, response, pfcngf, request, debug);
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
        JsonObject pfcngf=new JsonObject();
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
					pfcngf.addProperty("ID", jsonInput.get("ID").getAsString());
					
					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					String changedAt = commonUtils.getCreatedAtTime();
					long changedOn = commonUtils.getCreatedOnDate();
					pfcngf.addProperty("ChangedBy", changedBy);
					pfcngf.addProperty("ChangedAt", changedAt);
					pfcngf.addProperty("ChangedOn", "/Date("+changedOn+")/");
					if (jsonInput.has("CreatedBy") && !jsonInput.get("CreatedBy").isJsonNull()) {
						pfcngf.addProperty("CreatedBy", jsonInput.get("CreatedBy").getAsString());
					}

					if (jsonInput.has("CreatedAt") && !jsonInput.get("CreatedAt").isJsonNull()) {
						pfcngf.addProperty("CreatedAt", jsonInput.get("CreatedAt").getAsString());
					}

					if (jsonInput.has("CreatedOn") && !jsonInput.get("CreatedOn").isJsonNull()) {
						pfcngf.addProperty("CreatedOn", jsonInput.get("CreatedOn").getAsString());
					} 

					if (jsonInput.has("AGGRID") && !jsonInput.get("AGGRID").isJsonNull()) {
						pfcngf.addProperty("AGGRID", jsonInput.get("AGGRID").getAsString());
					} 

					if (jsonInput.has("CORPID") && !jsonInput.get("CORPID").isJsonNull()) {
						pfcngf.addProperty("CORPID", jsonInput.get("CORPID").getAsString());
					} 

					if (jsonInput.has("ProductCode") && !jsonInput.get("ProductCode").isJsonNull()) {
						pfcngf.addProperty("ProductCode", jsonInput.get("ProductCode").getAsString());
					} 

					if (jsonInput.has("MessageSource") && !jsonInput.get("MessageSource").isJsonNull()) {
						pfcngf.addProperty("MessageSource", jsonInput.get("MessageSource").getAsString());
					} 
					if (jsonInput.has("MapCode") && !jsonInput.get("MapCode").isJsonNull()) {
						pfcngf.addProperty("MapCode", jsonInput.get("MapCode").getAsString());
					} 

					if (jsonInput.has("UserData") && !jsonInput.get("UserData").isJsonNull()) {
						pfcngf.addProperty("UserData", jsonInput.get("UserData").getAsString());
					} 

					if (jsonInput.has("ApprovalRequired") && !jsonInput.get("ApprovalRequired").isJsonNull()) {
						pfcngf.addProperty("ApprovalRequired", jsonInput.get("ApprovalRequired").getAsString());
					} 

					if (jsonInput.has("SystemID") && !jsonInput.get("SystemID").isJsonNull()) {
						pfcngf.addProperty("SystemID", jsonInput.get("SystemID").getAsString());
					} 

					if (jsonInput.has("AggregatorDivision") && !jsonInput.get("AggregatorDivision").isJsonNull()) {
						pfcngf.addProperty("AggregatorDivision", jsonInput.get("AggregatorDivision").getAsString());
					} 

					if (jsonInput.has("SettlementReqd") && !jsonInput.get("SettlementReqd").isJsonNull()) {
						pfcngf.addProperty("SettlementReqd", jsonInput.get("SettlementReqd").getAsString());
					} 

					if (jsonInput.has("CORMandatoryFields") && !jsonInput.get("CORMandatoryFields").isJsonNull()) {
						pfcngf.addProperty("CORMandatoryFields", jsonInput.get("CORMandatoryFields").getAsString());
					} 

					if (jsonInput.has("PayTo") && !jsonInput.get("PayTo").isJsonNull()) {
						pfcngf.addProperty("PayTo", jsonInput.get("PayTo").getAsString());
					} 

					if (jsonInput.has("ClientCode") && !jsonInput.get("ClientCode").isJsonNull()) {
						pfcngf.addProperty("ClientCode", jsonInput.get("ClientCode").getAsString());
					} 

					if (jsonInput.has("ApprovalGroupID") && !jsonInput.get("ApprovalGroupID").isJsonNull()) {
						pfcngf.addProperty("ApprovalGroupID", jsonInput.get("ApprovalGroupID").getAsString());
					}

					if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
						pfcngf.addProperty("Source", jsonInput.get("Source").getAsString());
					} 

					if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
						pfcngf.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
					} 
					executeUrl = oDataUrl + "PFCNFG('"+jsonInput.get("ID").getAsString()+"')";
					if (debug) {
						response.getWriter().println("executeUrl:" + executeUrl);
						response.getWriter().println("input payload:" + pfcngf);
					}
					JsonObject insertedObj=commonUtils.executeUpdate(executeUrl, userpass, response, pfcngf, request, debug);
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

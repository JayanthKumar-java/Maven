package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SupplyChainfinanceEligibility extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		JsonObject resObj = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String executeUrl = "", userpass = "", username = "", password = "", oDataUrl = "";
		boolean debug = false;
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userpass = username + ":" + password;
			if (debug) {
				response.getWriter().println("oDataUrl:" + oDataUrl);
				response.getWriter().println("username:" + username);
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
				executeUrl = oDataUrl + "SupplyChainFinanceEligibility?$filter=" + filter;
			} else {
				executeUrl = oDataUrl + "SupplyChainFinanceEligibility";
			}

			JsonObject scf1Obj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("scf1Obj:" + scf1Obj);
			}
			if (scf1Obj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (scf1Obj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					JsonArray scf1Array = scf1Obj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
					resObj.add("SupplyChainFinanceEligibility", scf1Array);
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Record doesn't exist");
					response.getWriter().println(resObj);
				}

			} else {
				response.getWriter().println(scf1Obj);
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
		JsonObject resObj = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String executeUrl = "", userpass = "", username = "", password = "", oDataUrl = "";
		boolean debug = false;
		JsonParser parser = new JsonParser();
		try {
			String inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && inputPayload.trim().equalsIgnoreCase("")) {
				JsonObject jsonPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonPayload.has("ID") && !jsonPayload.get("ID").isJsonNull() && !jsonPayload.get("ID").getAsString().equalsIgnoreCase("")) {
					username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					userpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl:" + oDataUrl);
						response.getWriter().println("username:" + username);
					}
					executeUrl = oDataUrl + "SupplyChainFinanceEligibility?$filter=ID%20eq%20%27" + jsonPayload.get("ID").getAsString() + "%27";
					if (debug) {
						response.getWriter().println("executeUrl:" + executeUrl);
					}
					JsonObject scf1DbObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
					if (debug) {
						response.getWriter().println("scf1DbObj:" + scf1DbObj);
					}
					if (scf1DbObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (scf1DbObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							scf1DbObj = scf1DbObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
							if (!scf1DbObj.get("EligibilityTypeID").isJsonNull() && scf1DbObj.get("EligibilityTypeID").getAsString().equalsIgnoreCase("AML")) {
								executeUrl = oDataUrl + "SupplyChainFinanceEligibility('" + jsonPayload.get("ID").getAsString() + "')";
								if (debug) {
									response.getWriter().println("delete SupplyChainFinanceEligibility Url:" + executeUrl);
								}
								JsonObject delObjRes = commonUtils.executeODataDelete(executeUrl, userpass, response, request, debug, "PYGWHANA");
								if(debug){
									response.getWriter().println("delObjRes:"+delObjRes);
								}
								response.getWriter().println(delObjRes);
							}else{
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", "Record doesn't exist");
								response.getWriter().println(resObj);
							}
						} else {
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							resObj.addProperty("Message", "Record doesn't exist");
							response.getWriter().println(resObj);

						}
					} else {
						response.getWriter().println(scf1DbObj);
					}
				} else {
					if (jsonPayload.has("ID")) {
						resObj.addProperty("Message", "ID Should not be empty or null");
					} else {
						resObj.addProperty("Message", "Input Payload doesn't contains a ID Property");
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

}

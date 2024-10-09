package com.arteriatech.support;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ConfigTypesets extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject configTypesetObj = new JsonObject();
		String aggregatorID = "", typeset = "", oDataUrl = "", userName = "", password = "", userPass = "",
				executeURL = "";
		boolean debug = false;
		try {

			if (request.getParameter("debug") != null) {
				debug = true;
			}

			if (request.getParameter("AggregatorID") != null
					&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
				aggregatorID = request.getParameter("AggregatorID");
			}

			if (request.getParameter("Typeset") != null && !request.getParameter("Typeset").equalsIgnoreCase("")) {
				typeset = request.getParameter("Typeset");
			}

			if (debug) {
				response.getWriter().println("Recived AggregatorID " + aggregatorID);
				response.getWriter().println("Recived Typeset " + typeset);
			}
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName + ":" + password;
			if (aggregatorID != null && !aggregatorID.equalsIgnoreCase("") && typeset != null
					&& !typeset.equalsIgnoreCase("")) {
				executeURL = oDataUrl + "ConfigTypesetTypes?$filter=AggregatorID%20eq%20%27" + aggregatorID
						+ "%27%20and%20Typeset%20eq%20%27" + typeset + "%27";
			} else if (aggregatorID != null && !aggregatorID.equalsIgnoreCase("")) {
				executeURL = oDataUrl + "ConfigTypesetTypes?$filter=AggregatorID%20eq%20%27" + aggregatorID + "%27";

			} else if (typeset != null && !typeset.equalsIgnoreCase("")) {
				executeURL = oDataUrl + "ConfigTypesetTypes?$filter=Typeset%20eq%20%27" + typeset + "%27";
			} else {
				executeURL = oDataUrl + "ConfigTypesetTypes";
			}
			if (debug) {
				response.getWriter().println("executeURL: " + executeURL);
			}
			configTypesetObj = commonUtils.executeURL(executeURL, userPass, response);
			if (debug) {
				response.getWriter().println("ConfigTypesets Response :" + configTypesetObj);
			}

			if (configTypesetObj != null
					&& configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
				JsonArray jsonArray = configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				response.getWriter().print(jsonArray);
			} else {
				JsonObject retunObj = new JsonObject();
				retunObj.addProperty("Message",
						"No Record Exist for The Given AggregatorID " + aggregatorID + " and Typeset  " + typeset);
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

	private JsonObject getExceptionMessage(Exception ex) {
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

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonInput = new JsonObject();
		JSONObject jsonPaylod = new JSONObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonInput = (JsonObject) jsonParser.parse(inputPayload);
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}

				if (debug) {
					response.getWriter().println(" Input Paylod " + jsonInput);
				}
				if (jsonInput.has("AggregatorID") && !jsonInput.get("AggregatorID").isJsonNull()
						&& !jsonInput.get("AggregatorID").getAsString().equalsIgnoreCase("")) {
					if (jsonInput.has("Types") && !jsonInput.get("Types").isJsonNull()
							&& !jsonInput.get("Types").getAsString().equalsIgnoreCase("")) {
						if (jsonInput.has("Language") && !jsonInput.get("Language").isJsonNull()
								&& !jsonInput.get("Language").getAsString().equalsIgnoreCase("")) {
							if (jsonInput.has("TypesName") && !jsonInput.get("TypesName").isJsonNull()
									&& !jsonInput.get("TypesName").getAsString().equalsIgnoreCase("")) {
								oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
								userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
								password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
								userPass = userName + ":" + password;
								executeURL = oDataUrl + "ConfigTypesetTypes";
								jsonPaylod.accumulate("AggregatorID", jsonInput.get("AggregatorID").getAsString());
								jsonPaylod.accumulate("Types", jsonInput.get("Types").getAsString());
								jsonPaylod.accumulate("Typeset", jsonInput.get("Typeset").getAsString());
								jsonPaylod.accumulate("Language", jsonInput.get("Language").getAsString());
								jsonPaylod.accumulate("TypesName", jsonInput.get("TypesName").getAsString());
								JsonObject executePostURL = commonUtils.executePostURL(executeURL, userPass, response,
										jsonPaylod, request, debug, "PCGWHANA");
								if (debug) {
									response.getWriter().println("insert ConfigTypesetTypes response");
								}
								response.getWriter().println(executePostURL);
							} else {
								JsonObject responseMessage = getResponseMessage("TypesName");
								response.getWriter().println(responseMessage);
							}
						} else {
							JsonObject responseMessage = getResponseMessage("Language");
							response.getWriter().println(responseMessage);
						}

					} else {
						JsonObject responseMessage = getResponseMessage("Types");
						response.getWriter().println(responseMessage);
					}

				} else {
					JsonObject responseMessage = getResponseMessage("Types");
					response.getWriter().println(responseMessage);
				}
			} else {
				JsonObject responseMessage = getResponseMessage(null);
				response.getWriter().println(responseMessage);

			}

		} catch (Exception ex) {
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);
		}

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	public JsonObject getResponseMessage(String field) {
		JsonObject retunObj = new JsonObject();
		if (field == null) {
			retunObj.addProperty("Message", " Input Paylaod is Empty ");
		} else {
			retunObj.addProperty("Message", field + " is empty in the Input Payload ");
		}
		retunObj.addProperty("Status", "000002");
		retunObj.addProperty("ErrorCode", "J002");
		return retunObj;
	}

}

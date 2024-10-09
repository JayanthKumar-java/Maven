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

public class ConfigTypsetTypeValues extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "",
				aggregatorID = "", typeSet = "";
		JsonParser parser = new JsonParser();
		JsonObject inputJson = new JsonObject();
		JsonObject configTypesetObj = new JsonObject();

		boolean debug = false;
		try {
			inputPayload = request.getParameter("ConfigTypsetTypeValues");
			inputJson = (JsonObject) parser.parse(inputPayload);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				if (inputJson.has("debug") && inputJson.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Input Payload " + inputPayload);
				}
				if (inputJson.has("AggregatorID") && !inputJson.get("AggregatorID").isJsonNull()
						&& !inputJson.get("AggregatorID").getAsString().equalsIgnoreCase("")) {
					if (inputJson.has("Typeset") && !inputJson.get("Typeset").isJsonNull()
							&& !inputJson.get("Typeset").getAsString().equalsIgnoreCase("")) {
						oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
						userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
						password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
						userPass = userName + ":" + password;
						aggregatorID = inputJson.get("AggregatorID").getAsString();
						typeSet = inputJson.get("Typeset").getAsString();
						executeURL = oDataUrl + "ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" + aggregatorID
								+ "%27%20and%20Typeset%20eq%20%27" + typeSet + "%27";
						if (debug) {
							response.getWriter().println("executeURL: " + executeURL);
						}
						configTypesetObj = commonUtils.executeURL(executeURL, userPass, response);
						if (debug) {
							response.getWriter().println("ConfigTypesetsType Response :" + configTypesetObj);
						}

						if (configTypesetObj != null && configTypesetObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray().size() > 0) {
							JsonArray jsonArray = configTypesetObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							response.getWriter().print(jsonArray);
						} else {
							JsonObject retunObj = new JsonObject();
							retunObj.addProperty("Message", "No Record Exist for The Given AggregatorID " + aggregatorID
									+ " and Typeset  " + typeSet);
							retunObj.addProperty("Status", "000002");
							retunObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(retunObj);
						}

					} else {
						JsonObject responseMessage = getResponseMessage("Typeset");
						response.getWriter().println(responseMessage);
					}

				} else {
					JsonObject responseMessage = getResponseMessage("AggregatorID");
					response.getWriter().println(responseMessage);
				}
			} else {
				JsonObject responseMessage = getResponseMessage(null);
				response.getWriter().println(responseMessage);
			}

		} catch (Exception ex) {

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
			response.getWriter().println(retunObj);

		}

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

package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Tset extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		JsonParser parser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		JSONObject jsonObj = new JSONObject();
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Recived Input Payload " + inputPayload);
				}
				if (jsonPayload.has("AGGRID") && !jsonPayload.get("AGGRID").isJsonNull()
						&& !jsonPayload.get("AGGRID").getAsString().equalsIgnoreCase("")) {
					if (jsonPayload.has("TYPESET") && !jsonPayload.get("TYPESET").isJsonNull()
							&& !jsonPayload.get("TYPESET").getAsString().equalsIgnoreCase("")) {
						oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
						userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
						password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
						userPass = userName + ":" + password;
						executeURL = oDataUrl + "TSET";
						jsonObj.accumulate("AGGRID", jsonPayload.get("AGGRID").getAsString());
						jsonObj.accumulate("TYPESET", jsonPayload.get("TYPESET").getAsString());
						if (debug) {
							response.getWriter().println(" Tset Payload " + jsonObj);
						}
						JsonObject executePostURL = commonUtils.executePostURL(executeURL, userPass, response, jsonObj,
								request, debug, "PCGWHANA");
						if (debug) {
							response.getWriter().println(" Tset Response " + executePostURL);
						}

						if (executePostURL.has("error")) {
							JsonObject retunObj = new JsonObject();
							retunObj.addProperty("Status", "000002");
							retunObj.addProperty("ErrorCode", "J002");
							retunObj.addProperty("Message", "Insertion Failed");
							response.getWriter().println(retunObj);
						} else {
							JsonObject retunObj = new JsonObject();
							retunObj.addProperty("Status", "000001");
							retunObj.addProperty("ErrorCode", "");
							retunObj.addProperty("Message", "Record Inserted Successfully");
							response.getWriter().println(retunObj);
						}

					} else {
						JsonObject validateFields = validateFields("TYPESET");
						response.getWriter().println(validateFields);

					}

				} else {
					JsonObject validateFields = validateFields("AGGRID");
					response.getWriter().println(validateFields);

				}

			} else {
				JsonObject validateFields = validateFields(null);
				response.getWriter().println(validateFields);
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

	public JsonObject validateFields(String field) {
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
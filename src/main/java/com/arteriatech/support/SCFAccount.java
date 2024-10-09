package com.arteriatech.support;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFAccount.SCFAccountClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SCFAccount extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPut(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		Properties properties = new Properties();
		JsonObject jsonInputPayLoad = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		String inputPayload = "";
		String dealerAccountNo="";
		JsonObject jsonRes=new JsonObject();
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonInputPayLoad = (JsonObject) jsonParser.parse(inputPayload);
				if (jsonInputPayLoad.has("debug")
						&& jsonInputPayLoad.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}

				if (debug) {
					response.getWriter().println(" Input Payload " + jsonInputPayLoad);
				}
				if (jsonInputPayLoad.has("DealerAccountNo") && !jsonInputPayLoad.get("DealerAccountNo").isJsonNull()) {
					dealerAccountNo = jsonInputPayLoad.get("DealerAccountNo").getAsString();
				}

				if (dealerAccountNo != null && !dealerAccountNo.equalsIgnoreCase("")) {
					SCFAccountClient scfAccountClient = new SCFAccountClient();

					Map<String, String> callSCFAccountClient = scfAccountClient.callSCFAccountClient(dealerAccountNo,
							debug);
					if (debug) {
						for (String key : callSCFAccountClient.keySet()) {
							response.getWriter()
									.println("SCF Account ObjMap: " + key + " - " + callSCFAccountClient.get(key));
						}
					}

					for (String key : callSCFAccountClient.keySet()) {
						jsonRes.addProperty(key, callSCFAccountClient.get(key));
					}

					response.getWriter().println(jsonRes);

				} else {
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "/ARTEC/J001");
					result.addProperty("Message", "dealerAccountNo Mandatory field is missing");
					response.getWriter().println(new Gson().toJson(result));

				}

			} else {
				if (debug)
					response.getWriter().println("  Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "No inputPayload is received in the request");
				response.getWriter().println(new Gson().toJson(result));

			}

		} catch (Exception ex) {

			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			JsonObject result = new JsonObject();
			String errorCode = "E173";
			String errorMsg = properties.getProperty(errorCode);
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg + ". " + ex.getClass() + ":" + ex.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}
	}

}

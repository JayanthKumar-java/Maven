package com.arteriatech.support;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFOffer.SCFOfferClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Scfoffer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonInput = new JsonObject();
		JsonObject result = new JsonObject();
		String corpId = "", dealerId = "";
		boolean debug = false;
		SCFOfferClient scfofferClinet = new SCFOfferClient();
		Properties properties = new Properties();
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonInput = (JsonObject) jsonParser.parse(inputPayload);
				corpId = jsonInput.get("CorpId").getAsString();
				dealerId = jsonInput.get("DealerId").getAsString();
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				Map callSCFOffersWebservice = scfofferClinet.callSCFOffersWebservice(request, response, corpId,
						dealerId, debug);
				if (debug) {
					response.getWriter().println("Response form CPI");
					for (Object key : callSCFOffersWebservice.keySet()) {
						response.getWriter().println("key " + key + " Value " + callSCFOffersWebservice.get(key));

					}
				}

				for (Object key : callSCFOffersWebservice.keySet()) {
					result.addProperty(key.toString(), callSCFOffersWebservice.get(key).toString());
				}

				response.getWriter().println(result);

			} else {
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject resp = new JsonObject();
				resp.addProperty("Status", "000002");
				resp.addProperty("ErrorCode", "/ARTEC/J001");
				resp.addProperty("Message", "No inputPayload is received in the request");
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
			JsonObject resp = new JsonObject();
			String errorCode = "E173";
			String errorMsg = properties.getProperty(errorCode);
			resp.addProperty("errorCode", errorCode);
			resp.addProperty("Message", errorMsg + ". " + ex.getClass() + ":" + ex.getMessage());
			resp.addProperty("Status", properties.getProperty("ErrorStatus"));
			resp.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));

		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}

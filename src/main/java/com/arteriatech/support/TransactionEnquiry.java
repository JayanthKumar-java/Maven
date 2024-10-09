package com.arteriatech.support;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TransactionEnquiry extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TransactionEnquiryClient txnEnqClient = new TransactionEnquiryClient();
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		JsonObject jsonInput = new JsonObject();
		String corporateID = "", userID = "", userRegistrationID = "", uniqueID = "", aggregatorID = "";
		boolean debug = false;
		JsonObject resObj = new JsonObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			jsonInput = (JsonObject) parser.parse(inputPayload);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonInput.has("corpId") && !jsonInput.get("corpId").isJsonNull()) {

					corporateID = jsonInput.get("corpId").getAsString();
				}
				if (jsonInput.has("aggregatorId") && !jsonInput.get("aggregatorId").isJsonNull()) {
					aggregatorID = jsonInput.get("aggregatorId").getAsString();
				}

				if (jsonInput.has("userId") && !jsonInput.get("userId").isJsonNull()) {
					userID = jsonInput.get("userId").getAsString();
				}
				if (jsonInput.has("userRegistrationID") && !jsonInput.get("userRegistrationID").isJsonNull()) {
					userRegistrationID = jsonInput.get("userRegistrationID").getAsString();
				}

				if (jsonInput.has("uniqueID") && !jsonInput.get("uniqueID").isJsonNull()) {
					uniqueID = jsonInput.get("uniqueID").getAsString();
				}

				Map<String, String> txnEnqResp = txnEnqClient.callTransactionEnquiryWebService(request, response,
						corporateID, userID, userRegistrationID, uniqueID, aggregatorID, debug);

				for (String key : txnEnqResp.keySet()) {
					resObj.addProperty(key, txnEnqResp.get(key));
				}
				if (debug) {
					response.getWriter().println("Cpi Response " + new Gson().toJson(resObj));
				}
				response.getWriter().print(new Gson().toJson(resObj));
			} else {
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "No inputPayload is received in the request");
				response.getWriter().println(new Gson().toJson(result));
			}

		} catch (Exception ex) {
			JsonObject res = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			res.addProperty("ExceptionTrace", buffer.toString());
			res.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			res.addProperty("Status", "000002");
			res.addProperty("ErrorCode", "J002");
			response.getWriter().println(res);

		}

	}

}

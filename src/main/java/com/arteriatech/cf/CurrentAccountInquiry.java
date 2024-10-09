package com.arteriatech.cf;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.CurrentAccountInquiry.CAInquiryClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CurrentAccountInquiry extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String accountNo = "";
		Map<String, String> caInquiryMap = null;
		boolean debug = false;
		CAInquiryClient CAInquiryClient = new CAInquiryClient();
		JsonObject retunObj = new JsonObject();
		try {
			accountNo = request.getParameter("AccountNo");
			if (accountNo != null && !accountNo.equalsIgnoreCase("")) {
				if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("AccountNo :" + accountNo);
				}

				caInquiryMap = CAInquiryClient.callCurrentAccountsWebservice(accountNo.trim(), request, response,
						debug);
				if (caInquiryMap != null && !caInquiryMap.isEmpty()) {
					if (debug) {
						for (String key : caInquiryMap.keySet()) {
							response.getWriter().println(
									"AccountBalance-accountBalanceObjMap: " + key + " - " + caInquiryMap.get(key));

						}
					}

					for (String key : caInquiryMap.keySet()) {
						retunObj.addProperty(key, caInquiryMap.get(key));
					}
					response.getWriter().println(retunObj);

				}

			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Mandatory field missing in the request: " + accountNo);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);

			}
		}

		catch (Exception ex) {
			JsonObject resObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		}

	}

}

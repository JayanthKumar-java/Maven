package com.arteriatech.support;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.CurrentAccountInquiry.CAInquiryClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AccountInquiry  extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String inputPayload="",accountNo="";
		Map<String, String> caInquiryMap=null;
		boolean debug=false;
		CAInquiryClient CAInquiryClient=new CAInquiryClient();
		JsonObject retunObj=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		JsonParser jsonParser=new JsonParser();
		JsonObject jsonInput=new JsonObject();
		try {
			inputPayload=commonUtils.getGetBody(request, response);
			jsonInput=(JsonObject)jsonParser.parse(inputPayload);
			accountNo=jsonInput.get("AccountNo").getAsString();
			if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;
			}
		if (accountNo != null && !accountNo.equalsIgnoreCase("")) {
				if (debug) {
					response.getWriter().println("AccountNo :" + accountNo);
				}
				caInquiryMap = CAInquiryClient.callCurrentAccountsWebservice(accountNo.trim(), request, response, debug);
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
		}else{

			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", "Mandatory field missing in the request: " + accountNo);
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		
			
		}


			} catch (Exception ex) {
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

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	

}

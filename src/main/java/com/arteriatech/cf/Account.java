package com.arteriatech.cf;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.arteriatech.bc.Account.AccountClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Account  extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 String accNumber="",  aggregatorID="", corpId="",  userId="", userRegId="",message="";
		boolean debug = false;
		JsonObject payload = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		CommonUtils commonUtils = new CommonUtils();
		JsonObject retunObj = new JsonObject();
		AccountClient accClient = new AccountClient();
		boolean isParamMissing=false;
		String inputPayload = "";
		Map<String, String> userRegMap = new HashMap<String, String>();
		Map<String, String> userAccountsEntry = new HashMap<String, String>();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				payload = (JsonObject) jsonParser.parse(inputPayload);
				if (payload.has("debug") && !payload.get("debug").getAsString().equalsIgnoreCase("")
						&& payload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;

				}
				if (payload.has("AccNumber") && !payload.get("AccNumber").isJsonNull()
						&& !payload.get("AccNumber").getAsString().equalsIgnoreCase("")) {
					accNumber = payload.get("AccNumber").getAsString();
				} else {
					isParamMissing = true;
					message = "AccNumber Mandatory field missing in the request: ";
				}
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
				if (aggregatorID == null || aggregatorID.equalsIgnoreCase("")) {
					isParamMissing = true;
					message = "AggregatorID Mandatory field missing in the request: ";
				}
				if (payload.has("CorpId") && !payload.get("CorpId").isJsonNull()
						&& !payload.get("CorpId").getAsString().equalsIgnoreCase("")) {
					corpId = payload.get("CorpId").getAsString();
				} else {
					isParamMissing = true;
					message = "CorpId Mandatory field missing in the request: ";
				}
				if (payload.has("UserId") && !payload.get("UserId").isJsonNull()
						&& !payload.get("UserId").getAsString().equalsIgnoreCase("")) {
					userId = payload.get("UserId").getAsString();
				} else {
					isParamMissing = true;
					message = "UserId Mandatory field missing in the request: ";
				}
				if (payload.has("UserRegId") && !payload.get("UserRegId").isJsonNull()
						&& !payload.get("UserRegId").getAsString().equalsIgnoreCase("")) {
					userRegId = payload.get("UserRegId").getAsString();
				} else {
					isParamMissing = true;
					message = "UserRegId Mandatory field missing in the request: ";
				}
				if (isParamMissing) {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", message);
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				} else {
					userAccountsEntry.put("BankAccntNo", accNumber);
					userRegMap.put("CorpId", corpId);
					userRegMap.put("UserId", userId);
					userRegMap.put("UserRegId", userRegId);
					Map<String, String> callAccountsWebservice = accClient.callAccountsWebservice(request, response,
							userAccountsEntry, userRegMap, aggregatorID, debug);
					if (!callAccountsWebservice.isEmpty()) {
						if (debug) {
							for (String key : callAccountsWebservice.keySet()) {

								response.getWriter().println("AccountBalance-accountBalanceObjMap: " + key + " - "
										+ callAccountsWebservice.get(key));

							}
						}
						for (String key : callAccountsWebservice.keySet()) {
							retunObj.addProperty(key, callAccountsWebservice.get(key));
						}
						response.getWriter().println(retunObj);
					}

				}

			}else{
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "input payload is empty");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		}  catch (JsonParseException ex) {
			JsonObject resObj = getExceptionMessage(ex);
			response.getWriter().println(resObj);

		} catch (Exception ex) {
			JsonObject resObj = getExceptionMessage(ex);
			response.getWriter().println(resObj);

		}
		
	}
	
	
	public JsonObject getExceptionMessage(Exception ex) {
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
		return resObj;
	}

	
	

}

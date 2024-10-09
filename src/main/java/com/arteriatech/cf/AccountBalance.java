package com.arteriatech.cf;

import java.io.IOException;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.AccountBalance.AccountBalanceClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class AccountBalance extends HttpServlet{

	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	   String accNumber="",  aggregatorID="", corpId="",  userId="", userRegId="";
	   boolean debug=false;
	   JsonObject payload=new JsonObject();
	   JsonParser jsonParser=new JsonParser();
	   CommonUtils commonUtils=new CommonUtils();
	   JsonObject retunObj =new JsonObject();
	   AccountBalanceClient accountBalanceClient=new AccountBalanceClient();
	   String inputPayload="";
	   String bcUser="", bcPass="", bcURL="", bcClientID="", bcclientSecret="";
		try {
			bcUser = commonUtils.getODataDestinationProperties("User","BankConnect");
			bcPass = commonUtils.getODataDestinationProperties("Password","BankConnect");
			bcURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			bcClientID = commonUtils.getODataDestinationProperties("clientId","BankConnect");
			bcclientSecret = commonUtils.getODataDestinationProperties("clientSecret","BankConnect");
			/* response.getWriter().println("bcUser: "+bcUser);
			response.getWriter().println("bcPass: "+bcPass);
			response.getWriter().println("bcURL: "+bcURL);
			response.getWriter().println("bcClientID: "+bcClientID);
			response.getWriter().println("bcclientSecret: "+bcclientSecret); */

			inputPayload = commonUtils.getGetBody(request, response);
			payload = (JsonObject) jsonParser.parse(inputPayload);
			if (payload.has("debug") && !payload.get("debug").getAsString().equalsIgnoreCase("")
					&& payload.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;

			}
			if (payload.has("AccNumber") && !payload.get("AccNumber").isJsonNull()
					&& !payload.get("AccNumber").getAsString().equalsIgnoreCase("")) {
				accNumber = payload.get("AccNumber").getAsString();
			}
			if (!accNumber.equalsIgnoreCase("")) {
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
				if (aggregatorID == null || aggregatorID.equalsIgnoreCase("")) {
					aggregatorID = "";
				} 
				if (payload.has("CorpId") && !payload.get("CorpId").isJsonNull()) {
					corpId = payload.get("CorpId").getAsString();
				} else {
					corpId = "";
				}
				if(payload.has("UserId") &&! payload.get("UserId").isJsonNull()){
					userId=payload.get("UserId").getAsString();
				}else{
					userId="";
				}
				if (payload.has("UserRegId") && !payload.get("UserRegId").isJsonNull()) {
					userRegId = payload.get("UserRegId").getAsString();
				} else {
					userRegId = "";
				}
				Map<String, String> callAccountBalanceMap = accountBalanceClient.callAccountBalance(accNumber, aggregatorID, corpId, userId, userRegId, debug);
				
				if (callAccountBalanceMap != null && !callAccountBalanceMap.isEmpty()) {
					if (debug) {
						for (String key : callAccountBalanceMap.keySet()) {
							response.getWriter().println("AccountBalance-accountBalanceObjMap: " + key + " - "
									+ callAccountBalanceMap.get(key));

						}
					}
					for (String key : callAccountBalanceMap.keySet()) {
						retunObj.addProperty(key, callAccountBalanceMap.get(key));
					}

					response.getWriter().println(retunObj);

				}

			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Mandatory field missing in the request: " + accNumber);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		} catch (JsonParseException ex) {
			JsonObject resObj = getExceptionMessage(ex);
			response.getWriter().println(resObj);

		} catch (Exception ex) {
			JsonObject resObj = getExceptionMessage(ex);
			response.getWriter().println(resObj);

		}
	   
	}
	
	public JsonObject getExceptionMessage(Exception ex){
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

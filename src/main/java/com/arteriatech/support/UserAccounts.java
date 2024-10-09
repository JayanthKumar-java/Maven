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

public class UserAccounts extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		JSONObject insertObj = new JSONObject();
		JsonObject resObj = new JsonObject();
		String loginId="";
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if(inputPayload!=null && !inputPayload.equalsIgnoreCase("")){
				JsonObject inputJsonObj = (JsonObject) parser.parse(inputPayload);
				if (inputJsonObj.has("debug") && inputJsonObj.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if(debug){
					response.getWriter().println("Received Input Payload:"+inputJsonObj);
				}
				String createdBy = commonUtils.getUserPrincipal(request, "name", response);
				String createdAt = commonUtils.getCreatedAtTime();
				loginId=commonUtils.getLoginID(request, response, debug);
				if(debug){
					response.getWriter().println("Login Id: "+loginId);
				}
				long createdOnInMillis = commonUtils.getCreatedOnDate();
				String id = commonUtils.generateGUID(36);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;
				insertObj.accumulate("UaccntGuid", id);
				insertObj.accumulate("LoginId", loginId);	
				if(!inputJsonObj.get("AggregatorID").isJsonNull()){
					insertObj.accumulate("AggregatorID", inputJsonObj.get("AggregatorID").getAsString());
				}else{
					insertObj.accumulate("AggregatorID", "");	
				}
				
				if(!inputJsonObj.get("Corpid").isJsonNull()){
					insertObj.accumulate("Corpid", inputJsonObj.get("Corpid").getAsString());
				}else{
					insertObj.accumulate("Corpid", "");	
				}
				
				if(!inputJsonObj.get("Userid").isJsonNull()){
					insertObj.accumulate("Userid", inputJsonObj.get("Userid").getAsString());
				}else{
					insertObj.accumulate("Userid", "");	
				}
				
				if(!inputJsonObj.get("BankCountry").isJsonNull()){
					insertObj.accumulate("BankCountry", inputJsonObj.get("BankCountry").getAsString());
				}else{
					insertObj.accumulate("BankCountry", "");	
				}
				
				if(!inputJsonObj.get("BankKey").isJsonNull()){
					insertObj.accumulate("BankKey", inputJsonObj.get("BankKey").getAsString());
				}else{
					insertObj.accumulate("BankKey", "");	
				}
				
				if(!inputJsonObj.get("BankAccntNo").isJsonNull()){
					insertObj.accumulate("BankAccntNo", inputJsonObj.get("BankAccntNo").getAsString());
				}else{
					insertObj.accumulate("BankAccntNo", "");	
				}
				
				if(!inputJsonObj.get("BankAccntType").isJsonNull()){
					insertObj.accumulate("BankAccntType", inputJsonObj.get("BankAccntType").getAsString());
				}else{
					insertObj.accumulate("BankAccntType", "");	
				}
				
				if(!inputJsonObj.get("BankAccntSts").isJsonNull()){
					insertObj.accumulate("BankAccntSts", inputJsonObj.get("BankAccntSts").getAsString());
				}else{
					insertObj.accumulate("BankAccntSts", "");	
				}
				
				if(!inputJsonObj.get("DDBActive").isJsonNull()){
					insertObj.accumulate("DDBActive", inputJsonObj.get("DDBActive").getAsString());
				}else{
					insertObj.accumulate("DDBActive", "");	
				}
				
				if(!inputJsonObj.get("Source").isJsonNull()){
					insertObj.accumulate("Source", inputJsonObj.get("Source").getAsString());
				}else{
					insertObj.accumulate("Source", "");	
				}
				
				if(!inputJsonObj.get("SourceReferenceID").isJsonNull()){
					insertObj.accumulate("SourceReferenceID", inputJsonObj.get("SourceReferenceID").getAsString());
				}else{
					insertObj.accumulate("SourceReferenceID", "");	
				}
				insertObj.accumulate("CreatedBy", createdBy);
				insertObj.accumulate("CreatedAt", createdAt);
				insertObj.accumulate("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				executeURL=oDataUrl+"UserAccounts";
				if(debug){
					response.getWriter().println("execute URL:"+executeURL);
					response.getWriter().println("insertObj: "+insertObj);
					response.getWriter().println("Userpass:"+userPass);
				}
				JsonObject insertObjRes = commonUtils.executePostURL(executeURL, userPass, response, insertObj, request, debug, "PYGWHANA");
				if(debug){
					response.getWriter().println("insertObjRes:"+insertObjRes);
				}
				if(!insertObjRes.has("error")){
					resObj.addProperty("Ststus", "000001");
					resObj.addProperty("ErrorCode", "");
					resObj.addProperty("Message", "Record Inserted Successfully");
				}else{
					resObj.addProperty("Ststus", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Record not Inserted");
				}
				
				
			}else{
				// empty input Payload Received
				resObj.addProperty("Ststus", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Empty input Payload Received in the Paylaod");
			}
			response.getWriter().println(resObj);
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Ststus", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());
			response.getWriter().println(resObj);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		String inputPayload="";
		JsonObject resObj = new JsonObject();
		JsonParser parser=new JsonParser();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject inputJsonObj = (JsonObject) parser.parse(inputPayload);
				if (inputJsonObj.has("debug") && inputJsonObj.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Received Input Payload:" + inputJsonObj);
				}
				if (inputJsonObj.has("UaccntGuid") && !inputJsonObj.get("UaccntGuid").isJsonNull()
						&& !inputJsonObj.get("UaccntGuid").getAsString().equalsIgnoreCase("")) {

					oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
					userPass = userName + ":" + password;
					executeURL = oDataUrl + "UserAccounts('" + inputJsonObj.get("UaccntGuid").getAsString() + "')";
					if (debug) {
						response.getWriter().println("executeURL:" + executeURL);
					}
					JsonObject userAccoutObjRes = commonUtils.executeDelete(executeURL, userPass, response, request,
							debug, "PYGWHANA");

					if (debug) {
						response.getWriter().println(userAccoutObjRes);
					}
					if (userAccoutObjRes.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
						resObj.addProperty("Ststus", "000001");
						resObj.addProperty("ErrorCode", "");
						resObj.addProperty("Message", "Record Deleted Successfully");
					} else {
						resObj.addProperty("Ststus", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.add("Message", userAccoutObjRes);
					}
				} else {
					resObj.addProperty("Ststus", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "UaccntGuid Field is Empty in the input Payload");
				}

			} else {
				// empty input Payload Received
				resObj.addProperty("Ststus", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Empty input Payload Received in the Paylaod");
			}

			response.getWriter().println(resObj);
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Ststus", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());
			response.getWriter().println(resObj);
		}
		
	}

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		String uaccntGuid = "", aggregatorId = "";
		JsonObject resObj = new JsonObject();
		try {
			if (request.getParameter("UaccntGuid") != null
					&& !request.getParameter("UaccntGuid").equalsIgnoreCase("")) {
				uaccntGuid = request.getParameter("UaccntGuid");
			}
			if (request.getParameter("AggregatorID") != null
					&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
				aggregatorId = request.getParameter("AggregatorID");
			}
			String errorMessage = validateInputPayload(uaccntGuid, aggregatorId);
			if (errorMessage.equalsIgnoreCase("")) {
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;

				if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
					debug = true;
				}

				if (debug) {
					response.getWriter().println("UaccntGuid:" + uaccntGuid);
					response.getWriter().println("AggregatorID:" + aggregatorId);
				}

				executeURL = oDataUrl + "UserAccounts?$filter=UaccntGuid%20eq%20%27" + uaccntGuid
						+ "%27%20and%20AggregatorID%20eq%20%27" + aggregatorId + "%27";
				if (debug) {
					response.getWriter().println("executeURL:" + executeURL);
				}
				JsonObject userAccountObj = commonUtils.executeURL(executeURL, userPass, response);
				if (userAccountObj.has("error")) {
					resObj.add("Message", userAccountObj);
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
				} else {
					if (userAccountObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						resObj.add("Message",
								userAccountObj.get("d").getAsJsonObject().get("results").getAsJsonArray());
						resObj.addProperty("ErrorCode", "");
						resObj.addProperty("Status", "000001");
					} else {
						resObj.addProperty("Message", "Records Not Exist");
						resObj.addProperty("ErrorCode", "");
						resObj.addProperty("Status", "000001");
					}
				}

			} else {
				// Id Missing in the input Payload.
				resObj.addProperty("Message", errorMessage);
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
			}
			response.getWriter().println(resObj);
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			response.getWriter().println(resObj);
		}
	}

	

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		JSONObject insertObj = new JSONObject();
		JsonObject resObj = new JsonObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject inputJsonObj = (JsonObject) parser.parse(inputPayload);
				if (inputJsonObj.has("debug") && inputJsonObj.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Received Input Payload:" + inputJsonObj);
				}
				if (inputJsonObj.has("UaccntGuid")
						&& !inputJsonObj.get("UaccntGuid").getAsString().equalsIgnoreCase("")) {
					insertObj.accumulate("UaccntGuid", inputJsonObj.get("UaccntGuid").getAsString());
					if (!inputJsonObj.get("AggregatorID").isJsonNull()) {
						insertObj.accumulate("AggregatorID", inputJsonObj.get("AggregatorID").getAsString());
					} else {
						insertObj.accumulate("AggregatorID", "");
					}
					if (!inputJsonObj.get("Corpid").isJsonNull()) {
						insertObj.accumulate("Corpid", inputJsonObj.get("Corpid").getAsString());
					} else {
						insertObj.accumulate("Corpid", "");
					}
					if (!inputJsonObj.get("Userid").isJsonNull()) {
						insertObj.accumulate("Userid", inputJsonObj.get("Userid").getAsString());
					} else {
						insertObj.accumulate("Userid", "");
					}

					if (!inputJsonObj.get("BankCountry").isJsonNull()) {
						insertObj.accumulate("BankCountry", inputJsonObj.get("BankCountry").getAsString());
					} else {
						insertObj.accumulate("BankCountry", "");
					}

					if (!inputJsonObj.get("BankKey").isJsonNull()) {
						insertObj.accumulate("BankKey", inputJsonObj.get("BankKey").getAsString());
					} else {
						insertObj.accumulate("BankKey", "");
					}

					if (!inputJsonObj.get("BankAccntNo").isJsonNull()) {
						insertObj.accumulate("BankAccntNo", inputJsonObj.get("BankAccntNo").getAsString());
					} else {
						insertObj.accumulate("BankAccntNo", "");
					}

					if (!inputJsonObj.get("BankAccntType").isJsonNull()) {
						insertObj.accumulate("BankAccntType", inputJsonObj.get("BankAccntType").getAsString());
					} else {
						insertObj.accumulate("BankAccntType", "");
					}

					if (!inputJsonObj.get("BankAccntSts").isJsonNull()) {
						insertObj.accumulate("BankAccntSts", inputJsonObj.get("BankAccntSts").getAsString());
					} else {
						insertObj.accumulate("BankAccntSts", "");
					}

					if (!inputJsonObj.get("DDBActive").isJsonNull()) {
						insertObj.accumulate("DDBActive", inputJsonObj.get("DDBActive").getAsString());
					} else {
						insertObj.accumulate("DDBActive", "");
					}

					if (!inputJsonObj.get("Source").isJsonNull()) {
						insertObj.accumulate("Source", inputJsonObj.get("Source").getAsString());
					} else {
						insertObj.accumulate("Source", "");
					}

					if (!inputJsonObj.get("SourceReferenceID").isJsonNull()) {
						insertObj.accumulate("SourceReferenceID", inputJsonObj.get("SourceReferenceID").getAsString());
					} else {
						insertObj.accumulate("SourceReferenceID", "");
					}

					if (!inputJsonObj.get("LoginId").isJsonNull()) {
						insertObj.accumulate("LoginId", inputJsonObj.get("LoginId").getAsString());
					} else {
						insertObj.accumulate("LoginId", "");
					}

					if (!inputJsonObj.get("CreatedBy").isJsonNull()) {
						insertObj.accumulate("CreatedBy", inputJsonObj.get("CreatedBy").getAsString());
					} else {
						insertObj.accumulate("CreatedBy", "");
					}

					if (!inputJsonObj.get("CreatedAt").isJsonNull()) {
						insertObj.accumulate("CreatedAt", inputJsonObj.get("CreatedAt").getAsString());
					} else {
						insertObj.accumulate("CreatedAt", JSONObject.NULL);
					}

					if (!inputJsonObj.get("CreatedOn").isJsonNull()) {
						insertObj.accumulate("CreatedOn", inputJsonObj.get("CreatedOn").getAsString());
					} else {
						insertObj.accumulate("CreatedOn", JSONObject.NULL);
					}

					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					String createdAt = commonUtils.getCreatedAtTime();
					long changedOnInMillis = commonUtils.getCreatedOnDate();
					insertObj.accumulate("ChangedOn", changedOnInMillis);
					insertObj.accumulate("ChangedBy", changedBy);
					insertObj.accumulate("ChangedAt", createdAt);
					if (debug) {
						response.getWriter().println("insertObj: " + insertObj);
					}
					oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
					userPass = userName + ":" + password;
					executeURL = oDataUrl + "UserAccounts('" + inputJsonObj.get("UaccntGuid").getAsString() + "')";
					if (debug) {
						response.getWriter().println("executeURL:" + executeURL);
					}
					JsonObject userAccountResObj = commonUtils.executeUpdate(executeURL, userPass, response, insertObj,
							request, debug, "PYGWHANA");
					if (userAccountResObj.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
						resObj.addProperty("Ststus", "000001");
						resObj.addProperty("ErrorCode", "");
						resObj.addProperty("Message", "Records Updated Successfully");
					} else {
						resObj.addProperty("Ststus", "000001");
						resObj.addProperty("ErrorCode", "");
						resObj.add("Message", userAccountResObj);
					}

				} else {
					resObj.addProperty("Ststus", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "UaccntGuid Field is Empty in the Input Payload");

				}

			} else {
				// empty input Payload Received
				resObj.addProperty("Ststus", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Empty input Payload Received in the Paylaod");
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Ststus", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());

		}

	}
	
	

	private String validateInputPayload(String uaccntGuid, String aggregatorId) {
		String errorMessage = "";
		if (uaccntGuid == null || uaccntGuid.equalsIgnoreCase("") || uaccntGuid.trim().length() < 0) {
			errorMessage = "UaccntGuid Field Is  Empty in the Input Payload ";
		}
		
		if (aggregatorId == null || aggregatorId.equalsIgnoreCase("") || aggregatorId.trim().length() < 0) {
			if (errorMessage.equalsIgnoreCase("")) {
				errorMessage = "AggregatorID Field Is  Empty in the Input Payload ";
			} else {
				errorMessage = errorMessage + ",AggregatorID Field Is  Empty in the Input Payload ";
			}
		}
		return errorMessage;
	}
}
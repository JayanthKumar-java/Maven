package com.arteriatech.axis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.Message;
import org.json.JSONObject;

import com.arteriatech.bc.Account.AccountClient;
import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AxisUtils {
	CommonUtils commonUtils=new CommonUtils();
	
	public JsonObject getUserAccountsInJsonForAxis(HttpServletRequest request, HttpServletResponse response, String loginID, 
			String oDataURL, String aggregatorID, String accountNo, Properties properties, boolean debug) throws IOException{
		String message="", userName="", passWord="", userPass="", executeURL="";
		JsonObject userAccountsObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		
		try{
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+passWord;
			
//			executeURL = oDataURL+"UserAccounts?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if (accountNo.trim().length() > 0) {
				executeURL = oDataURL+"UserAccounts?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20BankAccntNo%20eq%20%27"+accountNo+"%27";
			} else {
				executeURL = oDataURL+"UserAccounts?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			}
			
			if(debug){
//				response.getWriter().println("getUserAccountsInJson-userName: "+userName);
//				response.getWriter().println("getUserAccountsInJson-passWord: "+passWord);
				response.getWriter().println("getUserAccountsInJson-executeURL: "+executeURL);
			}
			
			userAccountsObj = commonUtils.executeURL(executeURL, userPass, response);
			userAccountsObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
			userAccountsObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
		}catch (Exception e) {
			message = "001";
			errorResponseObj.addProperty("ErrorCode", message);
			errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
			userAccountsObj.add("d", errorResponseObj);
		}
		if(debug){
			response.getWriter().println("getUserAccountsInJson-userAccountsObj: "+userAccountsObj);
		}
		
		return userAccountsObj;
	}
	
	public int getResultsSizeForAxis(HttpServletResponse response, JsonObject inputJsonObj, boolean debug) throws IOException{
		int dataSize=0;
		
		try{
			JsonObject results = inputJsonObj.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			
			dataSize=dresults.size();
			if(debug){
				response.getWriter().println("dataSize: "+dataSize);
			}
		}catch (Exception e) {
			dataSize=0;
		}
		return dataSize;
	}
	
	public Map<String, String> getODAUserAccountEntryForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject, 
			String loginID, Map<String, String> pyeactEntries, String oDataURL, String aggregatorID, boolean debug){
		String userName="", passWord="", userPass="", executeURL="", acccountType="";
		Map<String,String> userAccountsEntry = new HashMap<String,String>();
		JsonObject httpJsonResult = new JsonObject();
		
		try{
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+passWord;
			
			executeURL = oDataURL+"UserAccounts?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getUserAccounts-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			if(debug)
				response.getWriter().println("getUserAccounts-results: "+results);
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getUserAccounts-dresults: "+dresults);
			JsonObject userAccountsJsonObj = null;
			if(debug)
				response.getWriter().println("getUserAccounts-Size: "+dresults.size());
			
			if(dresults.size() == 0){
				userAccountsEntry.put("Error", "076");
			}else{
//				userAccountsEntry.put("Error", "076");
				boolean isValidAccType=false;
				for (int i = 0; i <= dresults.size() - 1; i++) {
					userAccountsJsonObj = (JsonObject) dresults.get(i);
					if(debug)
						response.getWriter().println("userAccountsJsonObj-get(i): "+userAccountsJsonObj);
					
					if(! userAccountsJsonObj.get("BankAccntType").isJsonNull()){
						acccountType = userAccountsJsonObj.get("BankAccntType").getAsString();
					}
					
					if(debug){
						response.getWriter().println("getUserAccounts-acccountType: "+acccountType);
					}
					
					if(null != acccountType && acccountType.equalsIgnoreCase("ODA")){
						isValidAccType=true;
						
						userAccountsEntry.put("Error", "");
						
						if(! userAccountsJsonObj.get("BankAccntType").isJsonNull())
							userAccountsEntry.put("BankAccntType", userAccountsJsonObj.get("BankAccntType").getAsString());
						else
							userAccountsEntry.put("BankAccntType", "");
						
						if(! userAccountsJsonObj.get("Corpid").isJsonNull())
							userAccountsEntry.put("Corpid", userAccountsJsonObj.get("Corpid").getAsString());
						else
							userAccountsEntry.put("Corpid", "");
						
						if(! userAccountsJsonObj.get("Userid").isJsonNull())
							userAccountsEntry.put("Userid", userAccountsJsonObj.get("Userid").getAsString());
						else
							userAccountsEntry.put("Userid", "");
						
						if(! userAccountsJsonObj.get("BankCountry").isJsonNull())
							userAccountsEntry.put("BankCountry", userAccountsJsonObj.get("BankCountry").getAsString());
						else
							userAccountsEntry.put("BankCountry", "");
						
						if(! userAccountsJsonObj.get("BankCountry").isJsonNull())
							userAccountsEntry.put("BankCountry", userAccountsJsonObj.get("BankCountry").getAsString());
						else
							userAccountsEntry.put("BankCountry", "");
						
						if(! userAccountsJsonObj.get("BankKey").isJsonNull())
							userAccountsEntry.put("BankKey", userAccountsJsonObj.get("BankKey").getAsString());
						else
							userAccountsEntry.put("BankKey", "");
						
						if(! userAccountsJsonObj.get("BankAccntNo").isJsonNull())
							userAccountsEntry.put("BankAccntNo", userAccountsJsonObj.get("BankAccntNo").getAsString());
						else
							userAccountsEntry.put("BankAccntNo", "");
						
						if(! userAccountsJsonObj.get("BankAccntSts").isJsonNull())
							userAccountsEntry.put("BankAccntSts", userAccountsJsonObj.get("BankAccntSts").getAsString());
						else
							userAccountsEntry.put("BankAccntSts", "");
						
						if(! userAccountsJsonObj.get("DDBActive").isJsonNull())
							userAccountsEntry.put("DDBActive", userAccountsJsonObj.get("DDBActive").getAsString());
						else
							userAccountsEntry.put("DDBActive", "");
						
						break;
					}
					
					if(isValidAccType){
						break;
					}
				}
				
				if(! isValidAccType){
					userAccountsEntry.put("Error", "999");
				}
			}
		}catch (Exception e) {
			userAccountsEntry.put("Error", "001");
		}
		return userAccountsEntry;
	}
	
	public Map<String, String> getUserRegDetailsForAxis(HttpServletRequest request, HttpServletResponse response, String loginID, 
			String aggregatorID, String oDataURL, boolean debug) throws IOException{
		String executeURL = "", userPass="", userName="", password="",registrationFor="";
		Map<String, String> userRegResponseMap = new HashMap<String, String>();
		JsonObject httpJsonResult = new JsonObject();
		try{
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			if(debug){
				response.getWriter().println("aggregatorID: "+aggregatorID);
				response.getWriter().println("loginID: "+loginID);
			}
			executeURL = oDataURL+"UserRegistrations?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginId%20eq%20%27"+loginID+"%27";
			
//			executeURL = oDataURL+"UserRegistrations?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginId%20eq%20%27"+loginID+"%27%20and%20RegistrationFor%20ne%20%27B2BIZ%27";
			if(debug)
				response.getWriter().println("getUserRegDetails-executeURL: "+executeURL);
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			if(debug)
				response.getWriter().println("getUserRegDetails-httpJsonResult: "+httpJsonResult);
			
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			if(debug)
				response.getWriter().println("getUserRegDetails-results: "+results);
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getUserRegDetails-dresults: "+dresults);
			JsonObject userRegistrationJsonObj = null;
			if(debug)
				response.getWriter().println("getUserRegDetails-Size: "+dresults.size());
			
			if(dresults.size() ==  0){
				userRegResponseMap.put("Error", "054");
			}else{
				boolean isCorrectStatus=true;
				for (int i = 0; i <= dresults.size() - 1; i++) {
					
					registrationFor ="";
					userRegistrationJsonObj = (JsonObject) dresults.get(i);
					if(debug)
						response.getWriter().println("userCustomersJsonObj-get(i): "+userRegistrationJsonObj);
					
					if ( ! userRegistrationJsonObj.get("RegistrationFor").isJsonNull())
						registrationFor = userRegistrationJsonObj.get("RegistrationFor").getAsString();
					
					if ( ! registrationFor.equalsIgnoreCase("B2BIZ")) {
						
						userRegResponseMap.put("Error", "");
						
						if(userRegistrationJsonObj.get("CorpId").isJsonNull())
							userRegResponseMap.put("CorpId", "");
						else
							userRegResponseMap.put("CorpId", userRegistrationJsonObj.get("CorpId").getAsString());
						
						if(userRegistrationJsonObj.get("UserId").isJsonNull())
							userRegResponseMap.put("UserId", "");
						else
							userRegResponseMap.put("UserId", userRegistrationJsonObj.get("UserId").getAsString());
						
						if(userRegistrationJsonObj.get("AliasID").isJsonNull())
							userRegResponseMap.put("AliasID", "");
						else
							userRegResponseMap.put("AliasID", userRegistrationJsonObj.get("AliasID").getAsString());
						
						if(userRegistrationJsonObj.get("UserRegId").isJsonNull())
							userRegResponseMap.put("UserRegId", "");
						else
							userRegResponseMap.put("UserRegId", userRegistrationJsonObj.get("UserRegId").getAsString());
						
						if(userRegistrationJsonObj.get("UserRegStatus").isJsonNull()){
							userRegResponseMap.put("UserRegStatus", "");
						}else{
							userRegResponseMap.put("UserRegStatus", userRegistrationJsonObj.get("UserRegStatus").getAsString());
							if(! userRegistrationJsonObj.get("UserRegStatus").getAsString().equalsIgnoreCase("000002")){
								isCorrectStatus = false;
//							break;
							}
						}
						
						if(userRegistrationJsonObj.get("LoginId").isJsonNull())
							userRegResponseMap.put("LoginId", "");
						else
							userRegResponseMap.put("LoginId", userRegistrationJsonObj.get("LoginId").getAsString());
						break;
						
					}else
					{
						userRegResponseMap.put("Error", "054");
					}
					
				}
//				if(! isCorrectStatus){
//					userRegResponseMap.clear();
//					userRegResponseMap.put("Error", "054");
//				}
			}
		}catch (Exception e) {
			userRegResponseMap.put("Error", "001");
		}
		if(debug){
			for (String key : userRegResponseMap.keySet()) {
				response.getWriter().println("getUserRegDetails-userRegResponseMap: "+key + " - " + userRegResponseMap.get(key));
			}
		}
		
		return userRegResponseMap;
	}
	
	public Map<String, String> getACNTYPEntriesForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject1, 
			String loginID, boolean debug){
		String message = "", executeURL="", aggregatorID = "", types="", userPass="", userName="", password="", oDataUrl="";
		Map<String,String> acntypData = new HashMap<String,String>();
		JsonObject httpJsonResult = new JsonObject();
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataUrl+"AttributeTypesetTypes?$filter=Typeset%20eq%20%27ACNTYP%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getACNTYPEntries-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getACNTYPEntries-dresults: "+dresults);
			
			if(dresults.size() == 0){
//				message = "119";
				acntypData.put("Error", "000");
			}else{
				acntypData.put("Error", "");
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject typeSetJsonObj = (JsonObject) dresults.get(i);
					acntypData.put(typeSetJsonObj.get("Type").getAsString(), typeSetJsonObj.get("Name").getAsString());
					if(debug){
						response.getWriter().println("getACNTYPEntries-Type: "+typeSetJsonObj.get("Type").getAsString());
						response.getWriter().println("getACNTYPEntries-Name: "+typeSetJsonObj.get("Name").getAsString());
					}
				}
			}
		}catch (Exception e) {
			acntypData.put("Error", "001");
		}
		return acntypData;
	}
	
	public String getAccountTypeDescForAxis(HttpServletRequest request, HttpServletResponse response, String aggregatorID, String accountTypeFromWS, boolean debug) throws IOException{
		String accountTypeDesc = "", executeURL="", userPass="", userName="", password="", oDataUrl="";
		JsonObject httpJsonResult = new JsonObject();
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName =commonUtils. getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName+":"+password;
			
			if(debug){
				response.getWriter().println("getAccountTypeDesc-oDataUrl: "+oDataUrl);
				response.getWriter().println("getAccountTypeDesc-aggregatorID: "+aggregatorID);
				response.getWriter().println("getAccountTypeDesc-userName: "+userName);
				response.getWriter().println("getAccountTypeDesc-password: "+password);
				response.getWriter().println("getAccountTypeDesc-userPass: "+userPass);
			}
			executeURL = oDataUrl+"ConfigTypesetTypes?$filter=Typeset%20eq%20%27PYACTY%27%20and%20Types%20eq%20%27"+accountTypeFromWS+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getAccountTypeDesc-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getAccountTypeDesc-dresults: "+dresults);
			if(dresults.size() == 0){
				accountTypeDesc = "Not Maintained";
			}else{
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject typeSetJsonObj = (JsonObject) dresults.get(i);
					accountTypeDesc = typeSetJsonObj.get("TypesName").getAsString();
					if(debug){
						response.getWriter().println("getAccountTypeDesc-Types: "+typeSetJsonObj.get("Types").getAsString());
						response.getWriter().println("getAccountTypeDesc-TypesName: "+typeSetJsonObj.get("TypesName").getAsString());
					}
				}
			}
		}catch (Exception e) {
			accountTypeDesc="Not Maintained";
		}
		return accountTypeDesc;
	}
	
	public String getAccountStatusCodeForAxis(HttpServletRequest request, HttpServletResponse response, String aggregatorID, String accountStatusFromWS, boolean debug) throws IOException{
		String accountStatusCode = "", executeURL="", userPass="", userName="", password="", oDataUrl="";
		JsonObject httpJsonResult = new JsonObject();
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName+":"+password;
			
			if(debug){
				response.getWriter().println("getAccountStatusCode-oDataUrl: "+oDataUrl);
				response.getWriter().println("getAccountStatusCode-aggregatorID: "+aggregatorID);
				response.getWriter().println("getAccountStatusCode-userName: "+userName);
				response.getWriter().println("getAccountStatusCode-password: "+password);
				response.getWriter().println("getAccountStatusCode-userPass: "+userPass);
			}
			executeURL = oDataUrl+"ConfigTypesetTypes?$filter=Typeset%20eq%20%27PYACST%27%20and%20TypesName%20eq%20%27"+accountStatusFromWS+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getAccountStatusCode-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getAccountStatusCode-dresults: "+dresults);
			if(dresults.size() == 0){
				accountStatusCode = "Not Maintained";
			}else{
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject typeSetJsonObj = (JsonObject) dresults.get(i);
					accountStatusCode = typeSetJsonObj.get("Types").getAsString();
					if(debug){
						response.getWriter().println("getAccountStatusCode-Types: "+typeSetJsonObj.get("Types").getAsString());
						response.getWriter().println("getAccountStatusCode-TypesName: "+typeSetJsonObj.get("TypesName").getAsString());
					}
				}
			}
		}catch (Exception e) {
			accountStatusCode="Not Maintained";
		}
		return accountStatusCode;
	}
	
	public Map<String, String> callAccountsWSForAxis(HttpServletRequest request, HttpServletResponse response, Map<String, String> userAccountsEntry, 
			String loginID, String aggregatorID, String cpGuid1, String oDataURL, JSONObject inputJsonObject,Properties properties, boolean debug) throws IOException{
		AccountClient accountClient = new AccountClient();
		Map<String, String> accountsResponseMap = new HashMap<String, String>();
		Map<String, String> userRegResponseMap = new HashMap<String, String>();
		Map<String, String> accountsWSResponseMap = new HashMap<String, String>();
		Map<String, String> accountTypesMap = new HashMap<String, String>();
		boolean hasUserRegistered = true, isValidAccType = false;
		String accountTypeFromWS = "", accountStatusFromWS="", accountStatusCode="", accountTypeDesc="";
		try{
//			1. Call UserRegistrations
			userRegResponseMap = getUserRegDetailsForAxis(request, response, loginID, aggregatorID, oDataURL, debug);
			for (String key : userRegResponseMap.keySet()) {
				/*if(debug)
					response.getWriter().println("callAccounts-userRegResponseMap: "+key + " - " + userRegResponseMap.get(key));*/
				if(key.equalsIgnoreCase("Error")){
					if(userRegResponseMap.get(key).equalsIgnoreCase("054") || userRegResponseMap.get(key).equalsIgnoreCase("001")){
						hasUserRegistered = false;
						break;
					}else{
						if(! userRegResponseMap.get("UserRegStatus").equalsIgnoreCase("000002")){
							hasUserRegistered = false;
							break;
						}
					}
				}
			}
			
			if(debug)
				response.getWriter().println("callAccounts-hasUserRegistered: "+hasUserRegistered);
			
			if(hasUserRegistered){
//				2. Call Accounts WS
				accountTypeFromWS = "";
				accountStatusFromWS="";
				
				Map<String, String> accNoFrmScfTable = getAccNoFrmScfTable(loginID, aggregatorID, response,properties,debug);
				
				if (accNoFrmScfTable.get("Status").equalsIgnoreCase("000001")) {

					accountsWSResponseMap = accountClient.callAccountsWebserviceForAxis(request, response, accNoFrmScfTable, userRegResponseMap, aggregatorID, debug);
					if (debug) {
						for (String key : accountsWSResponseMap.keySet()) {
							response.getWriter().println("callAccountsWS-accountsWSResponseMap:" + key + "--Value: " + accountsWSResponseMap.get(key));
						}
					}
					if (accountsWSResponseMap.get("Error").equalsIgnoreCase("059")) {
						accountsResponseMap.put("Error", "059");
						accountsResponseMap.put("Message", accountsWSResponseMap.get("Message"));
					} else {
						// 3. Account Type Matching and other validations
						accountTypesMap = getACNTYPEntriesForAxis(request, response, inputJsonObject, loginID, debug);
						accountTypeFromWS = accountsWSResponseMap.get("AccountType");

						if (null == accountTypeFromWS || accountTypeFromWS.trim().equalsIgnoreCase("") || accountTypeFromWS.trim().length() == 0)
							accountTypeFromWS = "";

						for (String key : accountTypesMap.keySet()) {
							if (key.equalsIgnoreCase(accountTypeFromWS)) {
								isValidAccType = true;
								break;
							}
						}

						if (!isValidAccType) {
							accountsResponseMap.put("Error", "067");
							accountsResponseMap.put("Message", "");
						} else {
							accountStatusFromWS = accountsWSResponseMap.get("AccountStatus");

							if (accountStatusFromWS.equalsIgnoreCase("Closed")) {
								accountsResponseMap.put("Error", "065");
								accountsResponseMap.put("Message", "");
							} else if (accountStatusFromWS.equalsIgnoreCase("Inactive")) {
								accountsResponseMap.put("Error", "066");
								accountsResponseMap.put("Message", "");
							} else if (accountStatusFromWS.equalsIgnoreCase("Dormant")) {
								accountsResponseMap.put("Error", "066");
								accountsResponseMap.put("Message", "");
							} else {
								accountsResponseMap.put("Error", "");
								accountsResponseMap.put("Message", "");
								// Taking descriptions
								accountStatusCode = "";
								accountTypeDesc = "";
								accountTypeDesc = getAccountTypeDescForAxis(request, response, aggregatorID, accountsWSResponseMap.get("AccountType"), debug);
								accountStatusCode = getAccountStatusCodeForAxis(request, response, aggregatorID, accountsWSResponseMap.get("AccountStatus"), debug);

								accountsResponseMap.put("AccountStatusCode", accountStatusCode);
								accountsResponseMap.put("AccountTypeDesc", accountTypeDesc);

								for (String key : accountsWSResponseMap.keySet()) {
									// response.getWriter().println("callAccountsWS-accountsWSResponseMap:"+key+"--Value: "+accountsWSResponseMap.get(key));
									accountsResponseMap.put(key, accountsWSResponseMap.get(key));
								}

								if (debug) {
									response.getWriter().println("callAccountsWS-accountStatusCode: " + accountStatusCode);
									response.getWriter().println("callAccountsWS-accountTypeDesc: " + accountTypeDesc);
								}
							}
						}
					}
				}else{
					String msg=accNoFrmScfTable.get("Message");
					accountsResponseMap.put("Error", "002");
					accountsResponseMap.put("Message", msg);
				}
			}else{
				accountsResponseMap.put("Error", "054");
				accountsResponseMap.put("Message", "");
			}
		}catch (Exception e) {
			accountsResponseMap.put("Error", "001");
			accountsResponseMap.put("Message", e.getMessage());
		}
		
		if(debug){
			for (String key : accountsResponseMap.keySet()) {
				response.getWriter().println("callAccounts-accountsResponseMap: "+key + " - " + accountsResponseMap.get(key));
			}
		}
		return accountsResponseMap;
	}
	
	public JsonObject setStandingInstruction1ForAxis(HttpServletRequest request, HttpServletResponse response, JsonObject userAccountsEntries, 
			Properties properties, String loginID, String aggregatorID, String oDataURL,boolean debug) throws IOException{

		JsonObject userAccountsResponseObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject(); 
		Map<String,String> pyeactEntries = new HashMap<String,String>();
		Map<String,String> userAccountsEntry = new HashMap<String,String>();
		Map<String,String> accountsWSResponse = new HashMap<String,String>();
		String message="";
		JSONObject inputJsonObject = new JSONObject();
		boolean odAccAvailableY = false;
		try{
//			pyeactEntries = getPYEACTEntries(request, response, inputJsonObject, loginID, debug); //PYEACT typeset values
			userAccountsEntry = getODAUserAccountEntryForAxis(request, response, inputJsonObject, loginID, pyeactEntries, oDataURL, aggregatorID, debug);
			// need to log the userAccountsEntry. if more then 1000 character truncate it ErrorMessage,UserMessage Filed.
			if(debug){
				for (String key : userAccountsEntry.keySet()) {
					response.getWriter().println("setStandingInstruction1-userAccountsEntry: "+key + " - " + userAccountsEntry.get(key));
				}
			}
			
			if(userAccountsEntry.get("Error").equalsIgnoreCase("")){
				//When ODA account is available in UserAccounts
				//Call Accounts WS
				accountsWSResponse = callAccountsWSForAxis(request, response, userAccountsEntry, loginID, aggregatorID, "", oDataURL, inputJsonObject,properties,debug);
		// need to log the accountsWSResponse
				if(debug){
					for (String key : accountsWSResponse.keySet()) {
						response.getWriter().println("setStandingInstruction1-accountsWSResponse: "+key + " - " + accountsWSResponse.get(key));
					}
				}
				
				if(accountsWSResponse.get("Error").equalsIgnoreCase("054")){
					message = "054";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("059")){
					message = "059";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", accountsWSResponse.get("Message"));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("067")){
					message = "067";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}
				else if(accountsWSResponse.get("Error").equalsIgnoreCase("065")){
					message = "065";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("066")){
					message = "066";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("001")){
					message = "001";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("001")){
					message = "002";
					String msg=accountsWSResponse.get("Message");
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", msg);
					userAccountsResponseObj.add("d", errorResponseObj);
				}else{
					userAccountsResponseObj = userAccountsEntries;
					boolean isDDBActive = false;
					String selectedAccNo = userAccountsEntry.get("BankAccntNo");
					// need to log the selectedAccNo
					if(debug){
						response.getWriter().println("selectedAccNo: "+selectedAccNo);
						response.getWriter().println("setStandingInstruction.IsCFSOD: "+accountsWSResponse.get("IsCFSOD"));
					}
					
					// need to the log the IsCFSOD Property value from accountsWSResponse JsonObjetc
					if(accountsWSResponse.get("IsCFSOD").equalsIgnoreCase("Y")){
						odAccAvailableY = true;
						
						JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
						JsonArray dresults = results.get("results").getAsJsonArray();
						for (int i = 0; i <= dresults.size() - 1; i++) {
							if(selectedAccNo.equalsIgnoreCase(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntNo").getAsString())){
								//Setting SI as X and CFS OD as Y when the account is CFSOD
								if(debug)
									response.getWriter().println("1");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "X");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "Y");
							}else{
								//All other accounts - setSI as ""
								if(debug)
									response.getWriter().println("2");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
						}
						// need to log the userAccountsResponseObj 
					}else{
						//When ODA account is not CFSOD
						userAccountsResponseObj = userAccountsEntries;
					
						JsonObject results1 = userAccountsResponseObj.get("d").getAsJsonObject();
						JsonArray dresults1 = results1.get("results").getAsJsonArray();
						for (int i = 0; i <= dresults1.size() - 1; i++) {
							if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")){
								//When  CFS OD account in "N"
								if(debug)
									response.getWriter().println("3");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}else{
								//If CFS OD is N, then CAA logic added
								if(debug)
									response.getWriter().println("4");
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
										&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
									if(debug)
										response.getWriter().println("5");
									isDDBActive = true;
								}
							}
						}
						// need to log the userAccountsResponseObj in the Message1 or Message2 Property put the Message if ODAccount CFSOD
						// log the isDDBActive
						if(isDDBActive){
							userAccountsResponseObj = userAccountsEntries;
							JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
							JsonArray dresults = results.get("results").getAsJsonArray();
							for (int i = 0; i <= dresults.size() - 1; i++) {
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
										&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
									//Set SI as O only when CAA is available and DDBActive is X
									if(debug)
										response.getWriter().println("6");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
								}else if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")
										&& userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("")){
									//Set SI as "" only when CAA is available and DDBActive is ""
									if(debug)
										response.getWriter().println("7");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}else{
									//All other Account Types other than CAA and ODA - set SI as ""
									if(debug)
										response.getWriter().println("8");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
							
							// need to log the userAccountsResponseObj if isDDBActive is true
						}else{
							userAccountsResponseObj = userAccountsEntries;
							JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
							JsonArray dresults = results.get("results").getAsJsonArray();
							for (int i = 0; i <= dresults.size() - 1; i++) {
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")){
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
									if(debug)
										response.getWriter().println("9");
								}else{
									if(debug)
										response.getWriter().println("10");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}
								
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
							
							// need to log the userAccountsResponseObj if isDDBActive is false
						}
					}
					
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("Status", "000001");
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
				}
				// need to log the userAccountsResponseObj
			}else{
				//No ODA account available in UserAccounts
				if(debug)
					response.getWriter().println("No ODA Account Available");
				boolean isDDBActive = false;
				userAccountsResponseObj = userAccountsEntries;
				JsonObject results1 = userAccountsResponseObj.get("d").getAsJsonObject();
				JsonArray dresults1 = results1.get("results").getAsJsonArray();
				for (int i = 0; i <= dresults1.size() - 1; i++) {
					if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
							&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
						if(debug)
							response.getWriter().println("11");
						isDDBActive = true;
					}
				}
				if(debug)
					response.getWriter().println("No ODA Account AvailableisDDBActive: "+isDDBActive);
				// need to the isDDBActive Message1 ODA Account not Available
				if(isDDBActive){
					userAccountsResponseObj = userAccountsEntries;
					JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
					JsonArray dresults = results.get("results").getAsJsonArray();
					for (int i = 0; i <= dresults.size() - 1; i++) {
						if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
								&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
							//Set SI as O only when CAA is available and DDBActive is X
							if(debug)
								response.getWriter().println("12");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
						}else if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")
								&& userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("")){
							//Set SI as "" only when CAA is available and DDBActive is ""
							if(debug)
								response.getWriter().println("13");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}else{
							//All other Account Types other than CAA and ODA - set SI as ""
							if(debug)
								response.getWriter().println("14");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}
						userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
					}
					// need to log the userAccountsResponseObj Message1=ODAccount Not Available
				}else{
					userAccountsResponseObj = userAccountsEntries;
					JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
					JsonArray dresults = results.get("results").getAsJsonArray();
					for (int i = 0; i <= dresults.size() - 1; i++) {
						if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")){
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
							if(debug)
								response.getWriter().println("15");
						}else{
							if(debug)
								response.getWriter().println("16");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}
						
						userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
					}
					// need to log the userAccountsResponseObj.
				}
				
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("Status", "000001");
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
				
			}
			
			if(debug)
				response.getWriter().println("setStandingInstruction1.userAccountsResponseObj before setting description: "+userAccountsResponseObj);
			//Setting Description Fields
			String accountStatusDesc="", accountTypeDesc="";
			
			if(! userAccountsResponseObj.get("d").getAsJsonObject().get("Status").getAsString().equalsIgnoreCase("000000")){
				JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
				JsonArray dresults = results.get("results").getAsJsonArray();
				for (int i = 0; i <= dresults.size() - 1; i++) {
					accountTypeDesc = getAccountTypeDescForAxis(request, response, aggregatorID, dresults.get(i).getAsJsonObject().get("BankAccntType").getAsString(), debug);
					accountStatusDesc = getAccountStatusDescForAxis(request, response, aggregatorID, dresults.get(i).getAsJsonObject().get("BankAccntSts").getAsString(), debug);
				
					userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("BankAccStsDs", accountStatusDesc);
					userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("BankAccTypeDs", accountTypeDesc);
				}
			}
			
		}catch (Exception e) {
			message = "001";
			errorResponseObj.addProperty("ErrorCode", message);
			errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
			userAccountsResponseObj.add("d", errorResponseObj);
			
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception-setStandingInstruction: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			}
		}
		if(debug)
			response.getWriter().println("setStandingInstruction.userAccountsResponseObj: "+userAccountsResponseObj);
		
		return userAccountsResponseObj;
	
	}
	
	public String getAccountStatusDescForAxis(HttpServletRequest request, HttpServletResponse response, String aggregatorID, String accountStatusFromWS, boolean debug) throws IOException{
		String accountStatusDesc = "", executeURL="", userPass="", userName="", password="", oDataUrl="";
		JsonObject httpJsonResult = new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		try{
			oDataUrl =commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName+":"+password;
			
			if(debug){
				response.getWriter().println("getAccountStatusDesc-oDataUrl: "+oDataUrl);
				response.getWriter().println("getAccountStatusDesc-aggregatorID: "+aggregatorID);
				response.getWriter().println("getAccountStatusDesc-userName: "+userName);
				response.getWriter().println("getAccountStatusDesc-password: "+password);
				response.getWriter().println("getAccountStatusDesc-userPass: "+userPass);
			}
			executeURL = oDataUrl+"ConfigTypesetTypes?$filter=Typeset%20eq%20%27PYACST%27%20and%20Types%20eq%20%27"+accountStatusFromWS+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getAccountStatusDesc-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("getAccountStatusDesc-dresults: "+dresults);
			if(dresults.size() == 0){
				accountStatusDesc = "Not Maintained";
			}else{
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject typeSetJsonObj = (JsonObject) dresults.get(i);
					accountStatusDesc = typeSetJsonObj.get("TypesName").getAsString();
					if(debug){
						response.getWriter().println("getAccountStatusDesc-Types: "+typeSetJsonObj.get("Types").getAsString());
						response.getWriter().println("getAccountStatusDesc-TypesName: "+typeSetJsonObj.get("TypesName").getAsString());
					}
				}
			}
		}catch (Exception e) {
			accountStatusDesc="Not Maintained";
		}
		return accountStatusDesc;
	}
	
	public JsonObject userAccountsCreateForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObj, String loginID, 
			String aggregatorID, String oDataURL, Properties properties,JsonArray appLogMessArray,String appLogID, boolean debug) throws IOException{
		JsonObject userAccCreateResponseToUI = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		JsonObject bankAccountEntriesJson = new JsonObject();
		JsonObject bankAccountEntriesJsonWithSI = new JsonObject();
		Map<String, String> userRegMap = new HashMap<String, String>();
		Map<String,String> accountsWSResponseMap = new HashMap<String,String>();
		Map<String,String> scfEntryMapForUser = new HashMap<String,String>();
		Map<String,String> pyeactEntries = new HashMap<String,String>();
		String message = "", testRun = "", accountTypeFromWS = "", isCFSOFFromWS="", accountNoFromUI = "", ddbActiveFromUI="";
		int userAccSize=0;
		boolean hasUserRegistered = true, validationSuccess = false, isOfflineODAccount = false;
		String setSIForResponse="";
		ODataLogs appLogs=new ODataLogs();
		AtomicInteger stepNo=new AtomicInteger(1);
		String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserAccounts(guid'USERACCOUNTGUID_VALUE')\",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserAccounts(guid'USERACCOUNTGUID_VALUE')\",\"type\":\"ARTEC.PYGW.UserAccount\"}"
				+ ",\"UaccntGuid\":\"USERACCOUNTGUID_VALUE\""
				+ ",\"LoginId\":\"LOGINID_VALUE\""
				+ ",\"Corpid\":\"\""
				+ ",\"Userid\":\"\""
				+ ",\"BankCountry\":\"\""
				+ ",\"BankKey\":\"\""
				+ ",\"BankAccntNo\":\"BANKACCOUNTNO_VALUE\""
				+ ",\"BankAccntType\":\"BANKACCOUNTTYPE_VALUE\""
				+ ",\"BankAccntSts\":\"BANKACCOUNTSTATUS_VALUE\""
				+ ",\"CreatedOn\":null"
				+ ",\"CreatedBy\":\"\""
				+ ",\"CreatedAt\":\"PT00H00M00S\""
				+ ",\"ChangedOn\":null"
				+ ",\"ChangedBy\":\"\""
				+ ",\"ChangedAt\":\"PT00H00M00S\""
				+ ",\"BankAccStsDs\":\"\""
				+ ",\"BankAccTypeDs\":\"\""
				+ ",\"CustomerID\":\"\""
				+ ",\"CustomerTitle\":\"\""
				+ ",\"CustomerName\":\"\""
				+ ",\"CustomerShortName\":\"\""
				+ ",\"ModeOfOperation\":\"\""
				+ ",\"IsAccountClosed\":\"\""
				+ ",\"AccountClosedDate\":null"
				+ ",\"DDBActive\":\"DDBACTIVE_VALUE\""
				+ ",\"Testrun\":\"TESTRUN_VALUE\""
				+ ",\"SetSI\":\"SETSI_VALUE\""
				+ ",\"IsCFSODA\":\"ISCFSOD_VALUE\"}}";
		
		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_MESSAGE\"}"
				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\""
				+ ",\"service_version\":\"0001\"},\"transactionid\":\"8A50D8E98D62F127A8EC001372667F53\",\"timestamp\":null"
				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\""
				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\""
				+ ",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"}"
				+ ",\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\""
				+ ",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\""
				+ ",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
		
		try{
			
			if(debug)
				response.getWriter().println("userAccountsCreate-inputJsonObj: "+inputJsonObj);
			
			message = validateAccountCreateInputForAxis(response, inputJsonObj, aggregatorID, debug);
			if(debug)
				response.getWriter().println("userAccountsCreate-validateAccountCreateInput.message: "+message);
			
			if(message != null && message.trim().length() > 0){
				if(message.equalsIgnoreCase("Technical Error : Missing value for mandatory field : UaccntGuid")){
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", "120");
					errorResponseObj.addProperty("ErrorMessage", message);
					userAccCreateResponseToUI.add("d", errorResponseObj);
				}else if(message.equalsIgnoreCase("Technical Error : Missing value for mandatory field : BankAccntNo")){
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", "120");
					errorResponseObj.addProperty("ErrorMessage", message);
					userAccCreateResponseToUI.add("d", errorResponseObj);
				}else if(message.equalsIgnoreCase("Given User Account Guid is already available")){
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", "160");
					errorResponseObj.addProperty("ErrorMessage", message);
					userAccCreateResponseToUI.add("d", errorResponseObj);
				}else{
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", "001");
					errorResponseObj.addProperty("ErrorMessage", message);
					userAccCreateResponseToUI.add("d", errorResponseObj);
				}
			}else{
				bankAccountEntriesJson = getUserAccountsInJsonForAxis(request, response, loginID, oDataURL, aggregatorID, "", properties, debug);
				if(! bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").isJsonNull() 
						&& bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().length() > 0){
//					userAccCreateResponseToUI = bankAccountEntriesJson;
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString());
					errorResponseObj.addProperty("ErrorMessage", bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorMessage").getAsString());
					userAccCreateResponseToUI.add("d", errorResponseObj);
//					return userAccCreateResponseToUI;
				}else{
					userAccSize = getResultsSizeForAxis(response, bankAccountEntriesJson, debug);
					if(userAccSize > 0){
						message = validateAccountForAxis(response, inputJsonObj, bankAccountEntriesJson, loginID, aggregatorID, oDataURL, debug);
					}
					if(message != null && message.trim().length() > 0){
						if(message.equalsIgnoreCase("151")){
							errorResponseObj.addProperty("Status", "000000");
							errorResponseObj.addProperty("ErrorCode", message);
							errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
							userAccCreateResponseToUI.add("d", errorResponseObj);
						}else{
							errorResponseObj.addProperty("Status", "000000");
							errorResponseObj.addProperty("ErrorCode", "001");
							errorResponseObj.addProperty("ErrorMessage", message);
							userAccCreateResponseToUI.add("d", errorResponseObj);
						}
					}else{
						//Proceed Further
						userRegMap = getUserRegDetailsForAxis(request, response, loginID, aggregatorID, oDataURL, debug);
						for (String key : userRegMap.keySet()) {
							if(debug)
								response.getWriter().println("userAccountsCreate-userRegResponseMap: "+key + " - " + userRegMap.get(key));
							if(key.equalsIgnoreCase("Error")){
								if(userRegMap.get(key).equalsIgnoreCase("054") || userRegMap.get(key).equalsIgnoreCase("001")){
									hasUserRegistered = false;
									break;
								}else{
									if(! userRegMap.get("UserRegStatus").equalsIgnoreCase("000002")){
										hasUserRegistered = false;
										break;
									}
								}
							}
						}
						
						if(hasUserRegistered){
							if(debug){
								response.getWriter().println("userAccountsCreate.loginID"+loginID);
								response.getWriter().println("userAccountsCreate.oDataURL"+oDataURL);
								response.getWriter().println("userAccountsCreate.aggregatorID"+aggregatorID);
							}
							//Call Accounts WS
							accountsWSResponseMap = getAccountDetailsForAxis(request, response, inputJsonObj, userRegMap, aggregatorID, debug);
							
							if(accountsWSResponseMap.get("Error").equalsIgnoreCase("059")){
								message = "059";
								errorResponseObj.addProperty("Status", "000000");
								errorResponseObj.addProperty("ErrorCode", message);
								errorResponseObj.addProperty("ErrorMessage", accountsWSResponseMap.get("Message"));
								userAccCreateResponseToUI.add("d", errorResponseObj);
							}else{
								bankAccountEntriesJsonWithSI = bankAccountEntriesJson;
								bankAccountEntriesJson = null;
								bankAccountEntriesJson = setStandingInstruction1ForAxis(request, response, bankAccountEntriesJsonWithSI, properties, loginID, aggregatorID, oDataURL, debug);
								try{
									testRun = inputJsonObj.getString("Testrun");
								}catch (Exception e) {
									if(e.getMessage().contains("JSONObject[\"Testrun\"] not found")){
										//When Testrun in ""
										testRun = "";
									}
								}
								accountNoFromUI = inputJsonObj.getString("BankAccntNo");
								ddbActiveFromUI = inputJsonObj.getString("DDBActive");
								accountTypeFromWS = accountsWSResponseMap.get("AccountType");
								isCFSOFFromWS = accountsWSResponseMap.get("IsCFSOD");
								if(debug){
									response.getWriter().println("userAccountsCreate.bankAccountEntriesJson: "+bankAccountEntriesJson);
									response.getWriter().println("userAccountsCreate.testRun: "+testRun);
									response.getWriter().println("userAccountsCreate.accountTypeFromWS: "+accountTypeFromWS);
									response.getWriter().println("userAccountsCreate.isCFSOFFromWS: "+isCFSOFFromWS);
								}
								
								String accountTypeFromUACTbl = "", setSIFromUACTbl = "";
								boolean isValidODA=true;
								if(testRun != null && testRun.trim().length() > 0){
									if(testRun.equalsIgnoreCase("X")){
										if(accountTypeFromWS != null && accountTypeFromWS.trim().length() > 0){
											if(accountTypeFromWS.equalsIgnoreCase("ODA") && isCFSOFFromWS.equalsIgnoreCase("Y")){
												JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
												JsonArray dresults = results.get("results").getAsJsonArray();
												for (int i = 0; i <= dresults.size() - 1; i++) {
													accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
													setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
													
													if(debug){
														response.getWriter().println("userAccountsCreate.ODA and 'Y'.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
														response.getWriter().println("userAccountsCreate.ODA and 'Y'.setSIFromUACTbl: "+setSIFromUACTbl);
													}
													
													if(accountTypeFromUACTbl.equalsIgnoreCase("ODA") && setSIFromUACTbl.equalsIgnoreCase("X")){
														isValidODA=false;
														break;
													}
												}
												
												if(! isValidODA){
													message = "153";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccCreateResponseToUI.add("d", errorResponseObj);
												}else{
													//Get SCF Entries
													scfEntryMapForUser = getSCFEntryForTheUserForAxis(response, inputJsonObj, aggregatorID, loginID, debug);
													
													if(debug){
														
														response.getWriter().println("userAccCreate.scfEntryMapForUser.ErrorCode: "+scfEntryMapForUser.get("ErrorCode"));
														response.getWriter().println("userAccCreate.scfEntryMapForUser.StatusID: "+scfEntryMapForUser.get("StatusID"));
														response.getWriter().println("userAccCreate.scfEntryMapForUser.AccountNo: "+scfEntryMapForUser.get("AccountNo"));
														response.getWriter().println("userAccCreate.scfEntryMapForUser.DDBActive: "+scfEntryMapForUser.get("DDBActive"));
														response.getWriter().println("userAccCreate.accountNoFromUI: "+accountNoFromUI);
													}
													
													if(scfEntryMapForUser.get("ErrorCode") != null && scfEntryMapForUser.get("ErrorCode").trim().length() == 0){
														if(scfEntryMapForUser.get("StatusID") != null && scfEntryMapForUser.get("StatusID").equalsIgnoreCase("000002")){
															if(! scfEntryMapForUser.get("AccountNo").equalsIgnoreCase(accountNoFromUI)){
																message = "185";
																errorResponseObj.addProperty("Status", "000000");
																errorResponseObj.addProperty("ErrorCode", message);
																errorResponseObj.addProperty("ErrorMessage", "OD account "+scfEntryMapForUser.get("AccountNo")+" is already active. Please link the same account");
																userAccCreateResponseToUI.add("d", errorResponseObj);
															}else{
																if(scfEntryMapForUser.get("DDBActive") != null && scfEntryMapForUser.get("DDBActive").equalsIgnoreCase("X")){
																	//setsi=""
																	setSIForResponse="";
																	validationSuccess = true;
																}else{
																	//setsi="X"
																	setSIForResponse="X";
																	validationSuccess = true;
																}
															}
														}else if(scfEntryMapForUser.get("StatusID") != null && scfEntryMapForUser.get("StatusID").equalsIgnoreCase("000001")){
															message = "185";
															errorResponseObj.addProperty("Status", "000000");
															errorResponseObj.addProperty("ErrorCode", message);
															errorResponseObj.addProperty("ErrorMessage", "OD account "+scfEntryMapForUser.get("AccountNo")+" is not activated. Linking not possible");
															userAccCreateResponseToUI.add("d", errorResponseObj);
														}else{
															//setsi = "X"
															setSIForResponse="X";
															validationSuccess = true;
														}
													}else{
														//Offline OD Account Scenario
														if(debug)
															response.getWriter().println("Offline ODA scenario.userAccCreateResponseToUI: "+userAccCreateResponseToUI);
														
														if (scfEntryMapForUser.get("ErrorCode").equalsIgnoreCase("000")) {
															setSIForResponse="X";
															isOfflineODAccount = true;
															validationSuccess = true;
														}else{
															message = "001";
															errorResponseObj.addProperty("Status", "000000");
															errorResponseObj.addProperty("ErrorCode", message);
															errorResponseObj.addProperty("ErrorMessage", scfEntryMapForUser.get("ErrorMessage"));
															userAccCreateResponseToUI.add("d", errorResponseObj);
														}
													}
												}
											}else if(accountTypeFromWS.equalsIgnoreCase("ODA") && isCFSOFFromWS.equalsIgnoreCase("N")){
												//setsi=""
												setSIForResponse="";
												validationSuccess = true;
											}else if(accountTypeFromWS.equalsIgnoreCase("CAA")){
												//If account type is CAA
												boolean doNothing = false;
												accountTypeFromUACTbl = ""; setSIFromUACTbl="";
												String ddbActive = "";
												JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
												JsonArray dresults = results.get("results").getAsJsonArray();
												for (int i = 0; i <= dresults.size() - 1; i++) {
													accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
													setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
													
													if(debug){
														response.getWriter().println("userAccountsCreate.ODA and SI is 'X'.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
														response.getWriter().println("userAccountsCreate.ODA and SI is 'X'.setSIFromUACTbl: "+setSIFromUACTbl);
													}
													if(accountTypeFromUACTbl.equalsIgnoreCase("ODA") && setSIFromUACTbl.equalsIgnoreCase("X")){
														doNothing = true;
													}
												}
												
												if(! doNothing){
													for (int i = 0; i <= dresults.size() - 1; i++) {
														accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
														setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
														
														if(bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull()){
															ddbActive = "";
														}else{
															ddbActive = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString();
														}
														
														if(debug){
															response.getWriter().println("userAccountsCreate.CAA and SI is 'O'.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
															response.getWriter().println("userAccountsCreate.CAA and SI is 'O'.setSIFromUACTbl: "+setSIFromUACTbl);
															response.getWriter().println("userAccountsCreate.CAA and SI is 'O'.setSIFromUACTbl: "+ddbActive);
														}
														if(accountTypeFromUACTbl.equalsIgnoreCase("CAA") && setSIFromUACTbl.equalsIgnoreCase("O") && ddbActive.equalsIgnoreCase("X")){
															doNothing = true;
														}
													}
													if(! doNothing){
														//setSI = "O"
														setSIForResponse="O";
														validationSuccess = true;
													}else{
														//setSI = ""
														setSIForResponse="";
														validationSuccess = true;
													}
												}else{
													//setSI = ""
													setSIForResponse="";
													validationSuccess = true;
												}
											}else if(!accountTypeFromWS.equalsIgnoreCase("CAA") && !accountTypeFromWS.equalsIgnoreCase("ODA")){
												//setSI = ""
//												setSIForResponse="";
//												validationSuccess = true;
												if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
													message = "146";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccCreateResponseToUI.add("d", errorResponseObj);
												}else{
													//setsi = "";
													setSIForResponse="";
													validationSuccess = true;
												}
											}
										}
									}
									
									
								}else{
									//TestRun is Blank
									if(accountTypeFromWS != null && accountTypeFromWS.trim().length() > 0){
										if(accountTypeFromWS.equalsIgnoreCase("ODA")){
											if(isCFSOFFromWS.equalsIgnoreCase("Y")){
												if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
													JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
													JsonArray dresults = results.get("results").getAsJsonArray();
													accountTypeFromUACTbl = ""; setSIFromUACTbl="";
													for (int i = 0; i <= dresults.size() - 1; i++) {
														accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
														setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
														
														if(debug){
															response.getWriter().println("userAccountsCreate.ODA and 'Y'.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
															response.getWriter().println("userAccountsCreate.ODA and 'Y'.setSIFromUACTbl: "+setSIFromUACTbl);
														}
														
														if(accountTypeFromUACTbl.equalsIgnoreCase("ODA") && setSIFromUACTbl.equalsIgnoreCase("X")){
															isValidODA=false;
															break;
														}
													}
													
													if(! isValidODA){
														message = "153";
														errorResponseObj.addProperty("Status", "000000");
														errorResponseObj.addProperty("ErrorCode", message);
														errorResponseObj.addProperty("ErrorMessage", "OD account "+scfEntryMapForUser.get("AccountNo")+" is not activated. Linking not possible");
														userAccCreateResponseToUI.add("d", errorResponseObj);
													}else{
														//Get SCF Entries
														scfEntryMapForUser = getSCFEntryForTheUserForAxis(response, inputJsonObj, aggregatorID, loginID, debug);
														
														if(debug){
															/*for (String key : scfEntryMapForUser.keySet()) {
																response.getWriter().println("userAccCreate.scfEntryMapForUser: "+key + " - " + scfEntryMapForUser.get(key));
															}*/
															response.getWriter().println("userAccCreate.scfEntryMapForUser.ErrorCode: "+scfEntryMapForUser.get("ErrorCode"));
															response.getWriter().println("userAccCreate.scfEntryMapForUser.StatusID: "+scfEntryMapForUser.get("StatusID"));
															response.getWriter().println("userAccCreate.scfEntryMapForUser.AccountNo: "+scfEntryMapForUser.get("AccountNo"));
															response.getWriter().println("userAccCreate.scfEntryMapForUser.DDBActive: "+scfEntryMapForUser.get("DDBActive"));
															response.getWriter().println("userAccCreate.accountNoFromUI: "+accountNoFromUI);
														}
														
														if(scfEntryMapForUser.get("ErrorCode") != null && scfEntryMapForUser.get("ErrorCode").trim().length() == 0){
															if(scfEntryMapForUser.get("StatusID") != null && scfEntryMapForUser.get("StatusID").equalsIgnoreCase("000002")){
																if(! scfEntryMapForUser.get("AccountNo").equalsIgnoreCase(accountNoFromUI)){
																	message = "185";
																	errorResponseObj.addProperty("Status", "000000");
																	errorResponseObj.addProperty("ErrorCode", message);
																	errorResponseObj.addProperty("ErrorMessage", "OD account "+scfEntryMapForUser.get("AccountNo")+" is already active. Please link the same account");
																	userAccCreateResponseToUI.add("d", errorResponseObj);
																}else{
																	if(scfEntryMapForUser.get("DDBActive") != null && scfEntryMapForUser.get("DDBActive").equalsIgnoreCase("X")){
																		//setsi=""
																		setSIForResponse="";
																		validationSuccess = true;
																	}else{
																		//setsi="X"
																		setSIForResponse="X";
																		validationSuccess = true;
																	}
																}
															}else if(scfEntryMapForUser.get("StatusID") != null && scfEntryMapForUser.get("StatusID").equalsIgnoreCase("000001")){
																message = "185";
																errorResponseObj.addProperty("Status", "000000");
																errorResponseObj.addProperty("ErrorCode", message);
																errorResponseObj.addProperty("ErrorMessage", "OD account "+scfEntryMapForUser.get("AccountNo")+" is not activated. Linking not possible");
																userAccCreateResponseToUI.add("d", errorResponseObj);
															}else{
																//setsi = "X"
																setSIForResponse="X";
																validationSuccess = true;
															}
														}else{
															//Offline OD Account scenario
															/*setSIForResponse="X";
															isOfflineODAccount = true;
															validationSuccess = true;*/
															if ( scfEntryMapForUser.get("ErrorCode").equalsIgnoreCase("000")) {

																setSIForResponse="X";
																validationSuccess = true;
																isOfflineODAccount = true;
															} else {
																message = "001";
																errorResponseObj.addProperty("Status", "000000");
																errorResponseObj.addProperty("ErrorCode", message);
																errorResponseObj.addProperty("ErrorMessage", scfEntryMapForUser.get("ErrorMessage"));
																userAccCreateResponseToUI.add("d", errorResponseObj);
															}
														}
													}
												}else if(ddbActiveFromUI == null || ddbActiveFromUI.trim().equalsIgnoreCase("")){
													message = "149";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccCreateResponseToUI.add("d", errorResponseObj);
												}
											}else if(isCFSOFFromWS.equalsIgnoreCase("N")){
												if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
													message = "146";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccCreateResponseToUI.add("d", errorResponseObj);
												}else{
													//setsi=""
													setSIForResponse="";
													validationSuccess = true;
												}	
											}
										}else if(accountTypeFromWS.equalsIgnoreCase("CAA")){
											boolean isCAAAlreadyAvailable  = false;
											JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
											JsonArray dresults = results.get("results").getAsJsonArray();
											accountTypeFromUACTbl = ""; setSIFromUACTbl="";
											for (int i = 0; i <= dresults.size() - 1; i++) {
												accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
												setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
												
												if(debug){
													response.getWriter().println("userAccountsCreate.CAA and 'Y'.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
													response.getWriter().println("userAccountsCreate.CAA and 'Y'.setSIFromUACTbl: "+setSIFromUACTbl);
												}
												
												if(accountTypeFromUACTbl.equalsIgnoreCase("ODA") && setSIFromUACTbl.equalsIgnoreCase("X")){
													isValidODA=false;
													break;
												}
											}
											
											if(! isValidODA){
												if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
													message = "148";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccCreateResponseToUI.add("d", errorResponseObj);
												}else{
													String ddbActiveFromUACTbl =  "";
													for (int i = 0; i <= dresults.size() - 1; i++) {
														accountTypeFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString();
														setSIFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString();
														
														if(bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull()){
															ddbActiveFromUACTbl = "";
														}else{
															ddbActiveFromUACTbl = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString();	
														}
														
														
														if(debug){
															response.getWriter().println("userAccountsCreate.CAA and 'O'  and DDBActive.accountTypeFromUACTbl: "+accountTypeFromUACTbl);
															response.getWriter().println("userAccountsCreate.CAA and 'O'  and DDBActive.setSIFromUACTbl: "+setSIFromUACTbl);
															response.getWriter().println("userAccountsCreate.CAA and 'O'  and DDBActive.ddbActiveFromUACTbl: "+ddbActiveFromUACTbl);
														}
														
														if(accountTypeFromUACTbl.equalsIgnoreCase("CAA") && setSIFromUACTbl.equalsIgnoreCase("O") && ddbActiveFromUACTbl.equalsIgnoreCase("X")){
															isCAAAlreadyAvailable  = true;
															break;
														}
														
													}
													
													if(isCAAAlreadyAvailable){
														if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
															message = "147";
															errorResponseObj.addProperty("Status", "000000");
															errorResponseObj.addProperty("ErrorCode", message);
															errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
															userAccCreateResponseToUI.add("d", errorResponseObj);
														}else{
															//setsi = "O";
															setSIForResponse="O";
															validationSuccess = true;
														}
													}else{
														//setsi = "O";
														
														setSIForResponse="O";
														validationSuccess = true;
														// log the setSIForResponse,validationSuccess
													}
												}
											}else{
												//setsi = "";
												setSIForResponse="";
												validationSuccess = true;
											}
										}else if(!accountTypeFromWS.equalsIgnoreCase("CAA") && !accountTypeFromWS.equalsIgnoreCase("ODA")){
											if(ddbActiveFromUI != null && ddbActiveFromUI.equalsIgnoreCase("X")){
												message = "146";
												errorResponseObj.addProperty("Status", "000000");
												errorResponseObj.addProperty("ErrorCode", message);
												errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
												userAccCreateResponseToUI.add("d", errorResponseObj);
											}else{
												//setsi = "";
												setSIForResponse="";
												validationSuccess = true;
											}
											// log the setSIForResponse,validationSuccess
										}
									}
								}
							}
							
						}else{
							message = "054";
							errorResponseObj.addProperty("Status", "000000");
							errorResponseObj.addProperty("ErrorCode", message);
							errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
							userAccCreateResponseToUI.add("d", errorResponseObj);
							
							accountsWSResponseMap.put("Error", "059");
							accountsWSResponseMap.put("Message", "User not registered");
							//set accounts ws response to error
						}
					}
					/*}else{
//						userAccCreateResponseToUI = bankAccountEntriesJson;
//						return userAccCreateResponseToUI;
					}*/
				}
			}
			
			if(debug){
				response.getWriter().println("userAccountsCreate.validationSuccess: "+validationSuccess);
				response.getWriter().println("userAccountsCreate.setSIForResponsefinal: "+setSIForResponse);
			}
			
			String responseUserAccGuid="", responseLoginID="", responseAccNo="", responseAccType="", responseAccStatus="", responseDDB="", responseTestRun="", responseIfCFSOD="";
			String responseErrorCode="", responseErrorMessage="";
			
			if(validationSuccess){
				//Success Response
				responseUserAccGuid = inputJsonObj.getString("UaccntGuid");
				responseLoginID = loginID;
				responseAccNo = inputJsonObj.getString("BankAccntNo");
				responseAccType = accountsWSResponseMap.get("AccountType");
				responseAccStatus = accountsWSResponseMap.get("AccountStatus"); //Have pass the status code
				responseDDB = inputJsonObj.getString("DDBActive");
				try{
					responseTestRun = inputJsonObj.getString("Testrun");
				}catch (Exception e) {
					if(e.getMessage().contains("JSONObject[\"Testrun\"] not found")){
						//When Testrun in ""
						responseTestRun = "";
					}
				}
//				responseTestRun = inputJsonObj.getString("Testrun");
				responseIfCFSOD = accountsWSResponseMap.get("IsCFSOD");
				
				successResponse = successResponse.replaceAll("USERACCOUNTGUID_VALUE", responseUserAccGuid);
				successResponse = successResponse.replaceAll("LOGINID_VALUE", responseLoginID);
				successResponse = successResponse.replaceAll("BANKACCOUNTNO_VALUE", responseAccNo);
				successResponse = successResponse.replaceAll("BANKACCOUNTTYPE_VALUE", responseAccType);
				successResponse = successResponse.replaceAll("BANKACCOUNTSTATUS_VALUE", responseAccStatus);
				successResponse = successResponse.replaceAll("DDBACTIVE_VALUE", responseDDB);
				successResponse = successResponse.replaceAll("TESTRUN_VALUE", responseTestRun);
				successResponse = successResponse.replaceAll("SETSI_VALUE", setSIForResponse);
				successResponse = successResponse.replaceAll("ISCFSOD_VALUE", responseIfCFSOD);
				
				userAccCreateResponseToUI = new JsonParser().parse(successResponse).getAsJsonObject();
				
				testRun = "";
				if(inputJsonObj.has("Testrun")&&!inputJsonObj.isNull("Testrun")){
					testRun=inputJsonObj.getString("Testrun");
				}
				
				if(debug)
					response.getWriter().println("userAccountsCreate.finalTestRun: "+testRun);
				if(testRun != null && testRun.equalsIgnoreCase("X")){
					
					response.getWriter().println(userAccCreateResponseToUI);
					return userAccCreateResponseToUI;
				}else{
					message = "";
					
					if(debug){
						response.getWriter().println("userAccountsCreate.bankAccountEntriesJson: "+bankAccountEntriesJson);
					}
					if(responseIfCFSOD != null && responseIfCFSOD.equalsIgnoreCase("Y")){
						//Check for existing entries with DDBActive in bankAccountEntriesJson
						String ddbActiveForUpdate = "";
						boolean insertFlag = false;
						JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
						JsonArray dresults = results.get("results").getAsJsonArray();
						JsonObject entryToUpdate = new JsonObject();
						for (int i = 0; i <= dresults.size() - 1; i++) {
							entryToUpdate = bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject();
							
							if(entryToUpdate.get("DDBActive").isJsonNull()){
								ddbActiveForUpdate = "";
							}else{
								ddbActiveForUpdate = entryToUpdate.get("DDBActive").getAsString();
							}
							
							if(debug)
								response.getWriter().println("userAccountsCreate.ddbActiveForUpdate: "+ddbActiveForUpdate);
							entryToUpdate.remove("DDBActive");
							entryToUpdate.addProperty("DDBActive", "");
							if(debug)
								response.getWriter().println("userAccountsCreate.entryToUpdate: "+entryToUpdate);
							
							if(ddbActiveForUpdate != null && ddbActiveForUpdate.equalsIgnoreCase("X")){
								//Call Update here
								message = updateUserAccountsForAxis(request, response, entryToUpdate, aggregatorID, debug);
							}
						}
						
						if(debug){
							response.getWriter().println("userAccountsCreate.update.message: "+message);
						}
						//Call Insert here
						if(message == null || (message != null && message.equalsIgnoreCase(""))){
							if(debug){
								response.getWriter().println("userAccountsCreate.CorpId: "+userRegMap.get("CorpId"));
								response.getWriter().println("userAccountsCreate.CorpId: "+userRegMap.get("CorpId"));
							}
							
							if(isOfflineODAccount){
								message = insertOfflineODAIntoSCF(request, response, inputJsonObj, userRegMap, setSIForResponse, accountsWSResponseMap, aggregatorID, loginID, appLogs,appLogMessArray,appLogID,stepNo,debug);
							}
							if(debug){
								response.getWriter().println("userAccountsCreate.insertOfflineODAIntoSCF.message: "+message);
							}
							
							if(message == null || (message != null && message.equalsIgnoreCase(""))){
								
								Map<String, String> accNoFrmScpBnkDataTble = getScpEntryForAxis(responseLoginID, aggregatorID, oDataURL, response, debug);
		                        if(debug){
									response.getWriter().println("accNoFrmScpBnkDataTble:"+accNoFrmScpBnkDataTble);
								}
		                        
								
		                        if (accNoFrmScpBnkDataTble.get("Status").equalsIgnoreCase("000001")) {
									String accNumber = accNoFrmScpBnkDataTble.get("BankAccntNo");
									if (inputJsonObj.has("BankAccntNo")) {
										inputJsonObj.remove("BankAccntNo");
									}
									
									inputJsonObj.accumulate("BankAccntNo", accNumber);
									// String successResponse = "sap-message: {\"code\":\"/ARTEC/PY/044\",\"message\":\"Account Linked Sucessfully\",\"severity\":\"info\",\"target\":\"\",\"details\":[]}";
									message = insertIntoUserAccountsForAxis(request, response, inputJsonObj, userRegMap, setSIForResponse, accountsWSResponseMap, aggregatorID, loginID, debug);
									if (debug) {
										response.getWriter().println("userAccountsCreate.directinsert.message: " + message);
									}
									JsonParser parse=new JsonParser();
									JsonObject insertedRes=(JsonObject)parse.parse(message);
									if(insertedRes.has("error")){
										String errorMsg="";
										errorMsg=insertedRes.get("message").getAsJsonObject().get("value").getAsString();
										responseErrorCode = "045";
										responseErrorMessage = errorMsg;
										errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
										errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
										response.getWriter().println(userAccCreateResponseToUI);
										return userAccCreateResponseToUI;
									}
								}else{
									responseErrorCode = "045";
									responseErrorMessage = accNoFrmScpBnkDataTble.get("Message");
									errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
									errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
									response.getWriter().println(userAccCreateResponseToUI);
									return userAccCreateResponseToUI;	
		                        }
								// log the Message value print like userAccountsCreate.insertOfflineODAIntoSCF.message:
								//appLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessArray, appLogID, "LineNumber=6371", "insertIntoUserAccounts()","message:"+message, stepNo.getAndIncrement()+"","", message);
							}else{
								
								if(message.equalsIgnoreCase("000") || message.equalsIgnoreCase("155")){
									responseErrorCode = "155";
									responseErrorMessage = "More than one partner is assigned for that login";
									
									errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
									errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
									//appLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessArray, appLogID, "LineNumber=6382", "Response to UI","userAccCreateResponseToUI=", stepNo.getAndIncrement()+"","", userAccCreateResponseToUI+"");
									response.getWriter().println(userAccCreateResponseToUI);
									return userAccCreateResponseToUI;
								}else{
									responseErrorCode = "001";
									responseErrorMessage = message;
									
									errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
									errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
									response.getWriter().println(userAccCreateResponseToUI);
									return userAccCreateResponseToUI;
								}
								
								
							}
						}else{
							responseErrorCode = "045";
							responseErrorMessage = "Account Linking Failed";
							
							errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
							errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
							//log the userAccCreateResponseToUI
							appLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessArray, appLogID, "LineNumber=6410", "Response to UI","userAccCreateResponseToUI=", stepNo.getAndIncrement()+"","", userAccCreateResponseToUI+"");
							response.getWriter().println(userAccCreateResponseToUI);
							return userAccCreateResponseToUI;
						}
					}else{
						
                             Map<String, String> accNoFrmScpBnkDataTble = getScpEntryForAxis(responseLoginID, aggregatorID, oDataURL, response, debug);						
						
                        if(debug){
							response.getWriter().println("accNoFrmScpBnkDataTble:"+accNoFrmScpBnkDataTble);
						}
                        
						if (accNoFrmScpBnkDataTble.get("Status").equalsIgnoreCase("000001")) {
							String accNumber = accNoFrmScpBnkDataTble.get("BankAccntNo");
							if (inputJsonObj.has("BankAccntNo")) {
								inputJsonObj.remove("BankAccntNo");
							}

							inputJsonObj.accumulate("BankAccntNo", accNumber);
							// String successResponse = "sap-message: {\"code\":\"/ARTEC/PY/044\",\"message\":\"Account Linked Sucessfully\",\"severity\":\"info\",\"target\":\"\",\"details\":[]}";
							message = insertIntoUserAccountsForAxis(request, response, inputJsonObj, userRegMap, setSIForResponse, accountsWSResponseMap, aggregatorID, loginID, debug);
							if (debug) {
								response.getWriter().println("userAccountsCreate.directinsert.message: " + message);
							}
							JsonParser parse=new JsonParser();
							JsonObject insertedRes=(JsonObject)parse.parse(message);
							if(insertedRes.has("error")){
								String errorMsg="";
								errorMsg=insertedRes.get("message").getAsJsonObject().get("value").getAsString();
								responseErrorCode = "045";
								responseErrorMessage = errorMsg;
								errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
								errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
								response.getWriter().println(userAccCreateResponseToUI);
								return userAccCreateResponseToUI;
							}
						}else{
							responseErrorCode = "045";
							responseErrorMessage = accNoFrmScpBnkDataTble.get("Message");
							errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
							errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
							response.getWriter().println(userAccCreateResponseToUI);
							return userAccCreateResponseToUI;	
                        }
					
					}
					
					if(message == null || message.trim().equalsIgnoreCase("")){
						responseErrorCode = "045";
						responseErrorMessage = "Account Linking Failed";
						
						errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
						errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
						response.getWriter().println(userAccCreateResponseToUI);
						return userAccCreateResponseToUI;
					}else{
						userAccCreateResponseToUI.get("d").getAsJsonObject().addProperty("Code", "044");
						userAccCreateResponseToUI.get("d").getAsJsonObject().addProperty("Message", "Account Linked Sucessfully");
						response.getWriter().println(userAccCreateResponseToUI);
						return userAccCreateResponseToUI;
					}
				}
			}else{
				//Failure Response
				responseErrorCode = userAccCreateResponseToUI.get("d").getAsJsonObject().get("ErrorCode").getAsString();
				responseErrorMessage = userAccCreateResponseToUI.get("d").getAsJsonObject().get("ErrorMessage").getAsString();
				
				errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
				errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
				
				userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(userAccCreateResponseToUI);
				if(debug)
					response.getWriter().println("userAccountsCreate.Failure Response.userAccCreateResponseToUI: "+userAccCreateResponseToUI);
				// log the userAccCreateResponseToUI
				return userAccCreateResponseToUI;
			}
		}catch (Exception e) {
//			String responseErrorCode="", responseErrorMessage="";
			
			message = "001";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug){
				response.getWriter().println("CommonUtils.userAccountsCreate --> "+e.getMessage()+". Full Stack Trace:"+buffer.toString());
			}
			
			if(debug){
				response.getWriter().println("CommonUtils.userAccountsCreate.userAccCreateResponseToUI: "+userAccCreateResponseToUI);
			}
			
			errorResponse = errorResponse.replaceAll("ERROR_CODE", message);
			errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", "CommonUtils.userAccountsCreate --> "+e.getMessage());
			
			userAccCreateResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(userAccCreateResponseToUI);
			// log the userAccCreateResponseToUI
			return userAccCreateResponseToUI;
		}
	}
	
	
	public String validateAccountCreateInputForAxis(HttpServletResponse response, JSONObject inputJsonObj, String aggregatorID, boolean debug) throws IOException{
		String returnMessage = "", accountGuid="", accountNo="";
		try{
			accountGuid = inputJsonObj.getString("UaccntGuid");
			accountNo = inputJsonObj.getString("BankAccntNo");
			if(debug){
				response.getWriter().println("validateAccountCreateInput.accountGuid: "+accountGuid);
				response.getWriter().println("validateAccountCreateInput.accountNo: "+accountNo);
			}
			
			if(accountGuid == null || accountGuid.trim().length() == 0){
				returnMessage = "Technical Error : Missing value for mandatory field : UaccntGuid";
			}
			if(debug){
				response.getWriter().println("validateAccountCreateInput.returnMessage1: "+returnMessage);
				response.getWriter().println("validateAccountCreateInput.returnMessage.trim().length(): "+returnMessage.trim().length());
				response.getWriter().println("validateAccountCreateInput.accountNo.trim().length(): "+accountNo.trim().length());
			}
				
			
			if(returnMessage.trim().length() == 0){
				if(accountNo == null || accountNo.trim().length() == 0){
					returnMessage = "Technical Error : Missing value for mandatory field : BankAccntNo";
				}
			}
			if(debug)
				response.getWriter().println("validateAccountCreateInput.returnMessage2: "+returnMessage);
			
			if(returnMessage.trim().length() == 0){
				returnMessage = checkUniqueIDInUserAccountsForAxis(response, accountGuid, aggregatorID, debug);
			}
		}catch (Exception e) {
			returnMessage = e.getMessage();
		}
		if(debug)
			response.getWriter().println("validateAccountCreateInput.returnMessageFinal: "+returnMessage);
		return returnMessage;
	}
	
	
	public String validateAccountForAxis(HttpServletResponse response, JSONObject inputJsonObj, JsonObject bankAccountEntriesJson, String loginID, 
			String aggregatorID, String oDataURL, boolean debug) throws IOException{
		String message = "", accountNoFromUI= "", accountNo = "", oDataUrl="", userName = "", password="", userPass="", executeURL="";
		boolean isAccountAvailable = false;
//		JsonObject httpJsonResult = new JsonObject();
		try{
			accountNoFromUI = inputJsonObj.getString("BankAccntNo");
			if(null != accountNoFromUI && accountNoFromUI.trim().length() > 0){
				JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
				JsonArray dresults = results.get("results").getAsJsonArray();
//				message = "119";
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject userAccJsonObj = (JsonObject) dresults.get(i);
					accountNo = userAccJsonObj.get("BankAccntNo").getAsString();
					if(debug)
						response.getWriter().println("validateAccount-accountNo: "+accountNo);
					if(accountNo.equalsIgnoreCase(accountNoFromUI)){
						isAccountAvailable = true;
						break;
					}
//					response.getWriter().println("value: "+legalStatusFromTSet);
				}
				
				if(isAccountAvailable){
					message = "151";
				}else{
					message = "";
				}
			}
		}catch (Exception e) {
			message = e.getLocalizedMessage();
		}
		if(debug)
			response.getWriter().println("validateAccount-message: "+message);
		
		return message;
	}
	
	
	public Map<String, String> getAccountDetailsForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObj, 
			Map<String, String> userRegMap, String aggregatorID, boolean debug) throws IOException{
		Map<String, String> accountsWSResponseMap = new HashMap<String, String>();
		Map<String, String> userAccountsEntry = new HashMap<String, String>();
		AccountClient accountClient = new AccountClient();
		
		try{
			userAccountsEntry.put("BankAccntNo", inputJsonObj.getString("BankAccntNo"));
			
			accountsWSResponseMap = accountClient.callAccountsWebserviceForAxis(request, response, userAccountsEntry, userRegMap, aggregatorID, debug);
			if(debug){
				for (String key : accountsWSResponseMap.keySet()) {
					response.getWriter().println("getAccountDetails-accountsWSResponseMap: "+key + " - " + accountsWSResponseMap.get(key));
				}
			}
			
		}catch (Exception e) {
			accountsWSResponseMap.put("Error", "059");
			accountsWSResponseMap.put("Message", e.getMessage());
		}
		
		return accountsWSResponseMap;
	}
	
	public Map<String, String> getSCFEntryForTheUserForAxis(HttpServletResponse response, JSONObject inputJsonObj, String aggregatorID, String loginID, boolean debug) throws IOException{
		Map<String, String> scfEntryMap = new HashMap<String, String>();
		String oDataUrl="", userName="", password="", userPass="", accountNo="", executeURL="", userPartnerTypes="";
		JsonObject scfJsonObject = new JsonObject();
		JsonObject userCustomerObj = new JsonObject();
		JsonObject userPartnerJsonResponse = new JsonObject();
		boolean isError = false;
		try{
			
			userPartnerJsonResponse = getUserPartnerTypesForAxis(response, loginID, aggregatorID, debug);
			JsonArray userPartnerJsonArray = userPartnerJsonResponse.getAsJsonObject("d").getAsJsonArray("results");
			JsonObject childUserPartnerJsonObj = new JsonObject();
			if(userPartnerJsonArray.size() > 0)
				childUserPartnerJsonObj = userPartnerJsonArray.get(0).getAsJsonObject();
			
			if ( childUserPartnerJsonObj.has("PartnerTypeID") &&  ! childUserPartnerJsonObj.get("PartnerTypeID").isJsonNull() )
				userPartnerTypes = childUserPartnerJsonObj.get("PartnerTypeID").getAsString();
			else
				userPartnerTypes = "";
			
			if (userPartnerTypes.equalsIgnoreCase("000002"))
			{
				userCustomerObj = getVendors(response, loginID, aggregatorID, debug);
			}
			else if (userPartnerTypes.equalsIgnoreCase("000003"))
			{
				userCustomerObj = getUserCustomersForAxis(response, loginID, aggregatorID, debug);
			}
			else {
				isError = true;
			}
			
			if(isError) {
				scfEntryMap.put("ErrorCode", "001");
				scfEntryMap.put("ErrorMessage", "Invalid Partner type maintained");
				
			}else{
				JsonObject userResults = userCustomerObj.get("d").getAsJsonObject();
				if(! userResults.get("ErrorCode").isJsonNull() && userResults.get("ErrorCode").getAsString().trim().length() > 0){
					scfEntryMap.put("ErrorCode", userResults.get("ErrorCode").getAsString());
					scfEntryMap.put("ErrorMessage", userResults.get("ErrorMessage").getAsString());
				}else{
					JsonArray userDResults = userResults.get("results").getAsJsonArray();
					String appendUrl = "";
					for(int i=0 ; i<userDResults.size() ; i++){
						if(i != userDResults.size()-1)
							appendUrl = appendUrl+"CPGUID%20eq%20%27"+userDResults.getAsJsonArray().get(i).getAsJsonObject().get("PartnerID").getAsString()+"%27%20or%20";
						else
							appendUrl = appendUrl+"CPGUID%20eq%20%27"+userDResults.getAsJsonArray().get(i).getAsJsonObject().get("PartnerID").getAsString()+"%27";
					}
					
					if(debug)
						response.getWriter().println("getSCFEntryForTheUser.appendUrl: "+appendUrl);
					
					oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					userPass = userName+":"+password;
					accountNo = inputJsonObj.getString("BankAccntNo");
					
//					executeURL = oDataUrl+"SCF?$filter=CP_GUID%20eq%20%27"+cpGuid+"%27%20and%20CP_TYPE%20eq%20%27"+cpType+"%27%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27%20and%20STATUS_ID%20eq%20%27000003%27";
					executeURL = oDataUrl+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20"+appendUrl;
					if(debug)
						response.getWriter().println("getSCFEntryForTheUser.executeURL: "+executeURL);
					
					scfJsonObject =commonUtils.executeURL(executeURL, userPass, response);
					
					if(debug)
						response.getWriter().println("getSCFEntryForTheUser.scfJsonObject: "+scfJsonObject);
//					scfJsonObject
					JsonObject results = scfJsonObject.get("d").getAsJsonObject();
					JsonArray dresults = results.get("results").getAsJsonArray();
					
					if(dresults.size() > 0){
						for (int i = 0; i <= dresults.size() - 1; i++) {
							JsonObject scfEntriesJsonObj = (JsonObject) dresults.get(i);
//							statusID = scfEntriesJsonObj.get("STATUS_ID").getAsString();
							scfEntryMap.put("ErrorCode", "");
							scfEntryMap.put("ErrorMessage", "");
							if(!scfEntriesJsonObj.get("CPGUID").isJsonNull())
								scfEntryMap.put("CPGUID", scfEntriesJsonObj.get("CPGUID").getAsString());
							else
								scfEntryMap.put("CPGUID", "");
							
							if(!scfEntriesJsonObj.get("CPTypeID").isJsonNull())
								scfEntryMap.put("CPTypeID", scfEntriesJsonObj.get("CPTypeID").getAsString());
							else
								scfEntryMap.put("CPTypeID", "");
							
							if(!scfEntriesJsonObj.get("OfferAmt").isJsonNull())
								scfEntryMap.put("OfferAmt", scfEntriesJsonObj.get("OfferAmt").getAsString());
							else
								scfEntryMap.put("OfferAmt", "");
							
							if(!scfEntriesJsonObj.get("OfferTenure").isJsonNull())
								scfEntryMap.put("OfferTenure", scfEntriesJsonObj.get("OfferTenure").getAsString());
							else
								scfEntryMap.put("OfferTenure", "");
							
							if(!scfEntriesJsonObj.get("Rate").isJsonNull())
								scfEntryMap.put("Rate", scfEntriesJsonObj.get("Rate").getAsString());
							else
								scfEntryMap.put("Rate", "");
							
							if(!scfEntriesJsonObj.get("AccountNo").isJsonNull())
								scfEntryMap.put("AccountNo", scfEntriesJsonObj.get("AccountNo").getAsString());
							else
								scfEntryMap.put("AccountNo", "");
							
							if(!scfEntriesJsonObj.get("NoOfChequeReturns").isJsonNull())
								scfEntryMap.put("NoOfChequeReturns", scfEntriesJsonObj.get("NoOfChequeReturns").getAsString());
							else
								scfEntryMap.put("NoOfChequeReturns", "");
							
							if(!scfEntriesJsonObj.get("PaymentDelayDays12Months").isJsonNull())
								scfEntryMap.put("PaymentDelayDays12Months", scfEntriesJsonObj.get("PaymentDelayDays12Months").getAsString());
							else
								scfEntryMap.put("PaymentDelayDays12Months", "");
							
							if(!scfEntriesJsonObj.get("BusinessVintageOfDealer").isJsonNull())
								scfEntryMap.put("BusinessVintageOfDealer", scfEntriesJsonObj.get("BusinessVintageOfDealer").getAsString());
							else
								scfEntryMap.put("BusinessVintageOfDealer", "");
							
							if(!scfEntriesJsonObj.get("PurchasesOf12Months").isJsonNull())
								scfEntryMap.put("PurchasesOf12Months", scfEntriesJsonObj.get("PurchasesOf12Months").getAsString());
							else
								scfEntryMap.put("PurchasesOf12Months", "");
							
							if(!scfEntriesJsonObj.get("DealersOverallScoreByCorp").isJsonNull())
								scfEntryMap.put("DealersOverallScoreByCorp", scfEntriesJsonObj.get("DealersOverallScoreByCorp").getAsString());
							else
								scfEntryMap.put("DealersOverallScoreByCorp", "");
							
							if(!scfEntriesJsonObj.get("CorpRating").isJsonNull())
								scfEntryMap.put("CorpRating", scfEntriesJsonObj.get("CorpRating").getAsString());
							else
								scfEntryMap.put("CorpRating", "");
							
							if(!scfEntriesJsonObj.get("DealerVendorFlag").isJsonNull())
								scfEntryMap.put("DealerVendorFlag", scfEntriesJsonObj.get("DealerVendorFlag").getAsString());
							else
								scfEntryMap.put("DealerVendorFlag", "");
							
							if(!scfEntriesJsonObj.get("ConstitutionType").isJsonNull())
								scfEntryMap.put("ConstitutionType", scfEntriesJsonObj.get("ConstitutionType").getAsString());
							else
								scfEntryMap.put("ConstitutionType", "");
							
							
							scfEntryMap.put("MaxLimitPerCorp", "");
							scfEntryMap.put("salesOf12Months", "");
							scfEntryMap.put("Currency", "");
							
							if(!scfEntriesJsonObj.get("StatusID").isJsonNull())
								scfEntryMap.put("StatusID", scfEntriesJsonObj.get("StatusID").getAsString());
							else
								scfEntryMap.put("StatusID", "");
							
							scfEntryMap.put("MCLR6Rate", "");
							scfEntryMap.put("InterestRateSpread", "");
							scfEntryMap.put("TenorOfPayment", "");
							scfEntryMap.put("ADDLNPRDINTRateSP", "");
							scfEntryMap.put("AddlnTenorOfPymt", "");
							scfEntryMap.put("DefIntSpread", "");
							scfEntryMap.put("ProcessingFee", "");
							
							if(!scfEntriesJsonObj.get("EContractID").isJsonNull())
								scfEntryMap.put("EContractID", scfEntriesJsonObj.get("EContractID").getAsString());
							else
								scfEntryMap.put("EContractID", "");
							
							if(!scfEntriesJsonObj.get("ECustomerID").isJsonNull())
								scfEntryMap.put("ECustomerID", scfEntriesJsonObj.get("ECustomerID").getAsString());
							else
								scfEntryMap.put("ECustomerID", "");
							
							if(!scfEntriesJsonObj.get("ApplicationNo").isJsonNull())
								scfEntryMap.put("ApplicationNo", scfEntriesJsonObj.get("ApplicationNo").getAsString());
							else
								scfEntryMap.put("ApplicationNo", "");
							
							if(!scfEntriesJsonObj.get("CallBackStatus").isJsonNull())
								scfEntryMap.put("CallBackStatus", scfEntriesJsonObj.get("CallBackStatus").getAsString());
							else
								scfEntryMap.put("CallBackStatus", "");
							
							scfEntryMap.put("ECompleteTime", "");
							scfEntryMap.put("ECompleteDate", "");
							
							if(!scfEntriesJsonObj.get("ApplicantID").isJsonNull())
								scfEntryMap.put("ApplicantID", scfEntriesJsonObj.get("ApplicantID").getAsString());
							else
								scfEntryMap.put("ApplicantID", "");
							
							scfEntryMap.put("LimitPrefix", "");
							scfEntryMap.put("InterestSpread", "");
							
							if(!scfEntriesJsonObj.get("DDBActive").isJsonNull())
								scfEntryMap.put("DDBActive", scfEntriesJsonObj.get("DDBActive").getAsString());
							else
								scfEntryMap.put("DDBActive", "");
							
							scfEntryMap.put("ProcessFeePerc", "");
							
							if(!scfEntriesJsonObj.get("ValidTo").isJsonNull())
								scfEntryMap.put("ValidTo", scfEntriesJsonObj.get("ValidTo").getAsString());
							else
								scfEntryMap.put("ValidTo", "");
						}
					}else{
						scfEntryMap.put("ErrorCode", "000");
						scfEntryMap.put("ErrorMessage", "No data found");
					}
				}
			}
		}catch (Exception e) {
			scfEntryMap.put("ErrorCode", "001");
			scfEntryMap.put("ErrorMessage", e.getMessage());
		}
		
		return scfEntryMap;
	}
	
	public JsonObject getUserPartnerTypesForAxis(HttpServletResponse response, String loginID, String aggregatorID, boolean debug) throws IOException{
		JsonObject userPartnerJsonrObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		String oDataUrl="", userName="", password="", userPass="", executeURL="";
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PUGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PUGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PUGWHANA);
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getUserPartnerTypes.oDataUrl: "+oDataUrl);
				response.getWriter().println("getUserPartnerTypes.userName: "+userName);
				response.getWriter().println("getUserPartnerTypes.password: "+password);
				response.getWriter().println("getUserPartnerTypes.userPass: "+userPass);
			}
			
			executeURL = oDataUrl+"UserPartners?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27";
			if(debug)
				response.getWriter().println("getUserPartnerTypes.executeURL: "+executeURL);
			
			userPartnerJsonrObj = commonUtils.executeURL(executeURL, userPass, response);
			if(debug){
				response.getWriter().println("getUserPartnerTypes.userPartnerJsonrObj: "+userPartnerJsonrObj);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+".CommonUtils.getUserPartnerTypes --> "+buffer.toString());
		}
		return userPartnerJsonrObj;
	}
	
	public JsonObject getVendors(HttpServletResponse response, String loginID, String aggregatorID, boolean debug) throws IOException{
		JsonObject vendorsJsonObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		String oDataUrl="", userName="", password="", userPass="", executeURL="";
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			if (debug) {
				response.getWriter().println("getVendorsDetails.oDataUrl: "+oDataUrl);
				response.getWriter().println("getVendorsDetails.userName: "+userName);
				response.getWriter().println("getVendorsDetails.password: "+password);
				response.getWriter().println("getVendorsDetails.userPass: "+userPass);
			}
			
			executeURL = oDataUrl+"Vendors?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27";
			if(debug)
				response.getWriter().println("getVendorsDetails.executeURL: "+executeURL);
			
			vendorsJsonObj = commonUtils.executeURL(executeURL, userPass, response);
			
			JsonObject results = vendorsJsonObj.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			
			if(dresults.size() > 0){
				vendorsJsonObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
				vendorsJsonObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
			}else{
				vendorsJsonObj.get("d").getAsJsonObject().addProperty("ErrorCode", "000");
				vendorsJsonObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "No Records Found in Entity Vendors for the Login ID");
			}
			
			if(debug){
				response.getWriter().println("getVendorsDetails.vendorsJsonObj: "+vendorsJsonObj);
			}
		}catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			errorResponseObj.addProperty("ErrorCode", "001");
			errorResponseObj.addProperty("ErrorMessage", e.getMessage()+".CommonUtils.getVendorsDetails --> "+buffer.toString());
			vendorsJsonObj.add("d", errorResponseObj);
		}
		
		return vendorsJsonObj;
	}
	
	public String updateUserAccountsForAxis(HttpServletRequest request, HttpServletResponse response, JsonObject entryToUpdate, 
			String aggregatorID, boolean debug) throws IOException{
		String message = "", executeURL="", oDataUrl="", userName="", password="", userPass="", accountGuid="", changedBy="", changedAt="";
		long changedOnInMillis=0;
		JsonObject updateResponse = new JsonObject();
		try{
			changedBy = commonUtils.getUserPrincipal(request, "name", response);
			changedOnInMillis = commonUtils.getCreatedOnDate();
			changedAt = commonUtils.getCreatedAtTime();
			
			JSONObject entryPayload = new JSONObject();
			
			accountGuid = entryToUpdate.get("UaccntGuid").getAsString();
			if(debug){
				response.getWriter().println("updateUserAccounts.entryToUpdate: "+entryToUpdate);
				response.getWriter().println("updateUserAccounts.accountGuid: "+accountGuid);
				response.getWriter().println("updateUserAccounts.entryPayload: "+entryPayload);
				response.getWriter().println("updateUserAccounts.changedBy: "+changedBy);
				response.getWriter().println("updateUserAccounts.changedOnInMillis: "+changedOnInMillis);
				response.getWriter().println("updateUserAccounts.changedAt: "+changedAt);
			}
			
			entryPayload.accumulate("UaccntGuid", entryToUpdate.get("UaccntGuid").getAsString());
			entryPayload.accumulate("AggregatorID", entryToUpdate.get("AggregatorID").getAsString());
			entryPayload.accumulate("LoginId", entryToUpdate.get("LoginId").getAsString());
			entryPayload.accumulate("Corpid", entryToUpdate.get("Corpid").getAsString());
			entryPayload.accumulate("Userid", entryToUpdate.get("Userid").getAsString());
			
			if(entryToUpdate.get("BankCountry").isJsonNull())
				entryPayload.accumulate("BankCountry", "");
			else
				entryPayload.accumulate("BankCountry", entryToUpdate.get("BankCountry").getAsString());
			
			if(entryToUpdate.get("BankKey").isJsonNull())
				entryPayload.accumulate("BankKey", "");
			else
				entryPayload.accumulate("BankKey", entryToUpdate.get("BankKey").getAsString());
			
			entryPayload.accumulate("BankAccntNo", entryToUpdate.get("BankAccntNo").getAsString());
			entryPayload.accumulate("BankAccntType", entryToUpdate.get("BankAccntType").getAsString());
			entryPayload.accumulate("BankAccntSts", entryToUpdate.get("BankAccntSts").getAsString());
			entryPayload.accumulate("CreatedBy", entryToUpdate.get("CreatedBy").getAsString());
			entryPayload.accumulate("CreatedAt", entryToUpdate.get("CreatedAt").getAsString());
			entryPayload.accumulate("CreatedOn", entryToUpdate.get("CreatedOn").getAsString());
			entryPayload.accumulate("ChangedBy", changedBy);
			entryPayload.accumulate("ChangedAt", changedAt);
			entryPayload.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
			
			if(entryToUpdate.get("Source").isJsonNull())
				entryPayload.accumulate("Source", "");
			else
				entryPayload.accumulate("Source", entryToUpdate.get("Source").getAsString());
			entryPayload.accumulate("DDBActive", "");
			
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password",DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataUrl+"UserAccounts('"+accountGuid+"')";
			if(debug)
				response.getWriter().println("updateUserAccounts.executeURL: "+executeURL);
			
			updateResponse = commonUtils.executeUpdate(executeURL, userPass, response, entryPayload, request, debug, "PYGWHANA");
			
			if(! updateResponse.get("ErrorCode").isJsonNull() && updateResponse.get("ErrorCode").getAsString().trim().length() > 0){
				message = updateResponse.toString();
			}
			if(debug){
				response.getWriter().println("updateUserAccounts.updateResponse: "+updateResponse);
			}
				
//			return message;
		}catch (Exception e) {
			message = e.getMessage();
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in updateUserAccounts.message: "+buffer.toString());
			}
//			return message;
		}
		if(debug){
			response.getWriter().println("updateUserAccounts.message: "+message);
		}
		return message;
	}
	
	public JsonObject getUserAccountEntryForUpdateForAxis(HttpServletResponse response, String accountGuid, boolean debug) throws IOException{
		JsonObject accountsEntry = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		String oDataURL="", aggregatorID="", userName="", password="", userPass="", executeURL="";
		try{
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataURL+"UserAccounts?$filter=UaccntGuid%20eq%20%27"+accountGuid+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("getUserAccountEntryForUpdate-executeURL: "+executeURL);
			
			accountsEntry = commonUtils.executeURL(executeURL, userPass, response);
			
			accountsEntry.addProperty("ErrorCode", "");
			accountsEntry.addProperty("ErrorMessage", "");
			
			if(debug)
				response.getWriter().println("getUserAccountEntryForUpdate-accountsEntry: "+accountsEntry);
			
			return accountsEntry;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			accountsEntry.addProperty("ErrorCode", "001");
			accountsEntry.addProperty("ErrorMessage", e.getMessage()+".CommonUtils.getUserAccountEntryForUpdate --> "+buffer.toString());
//			accountsEntry.add("d", errorResponseObj);
			
			return accountsEntry;
		}
	}
	
	public String insertOfflineODAIntoSCF(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObj, Map<String, String> userRegMap, 
			String setSIForResponse, Map<String, String> accountsWSResponseMap, String aggregatorID, String loginID,ODataLogs odataLogs,JsonArray appLogMsgArray,String appLogId,AtomicInteger stepNo, boolean debug) throws IOException{
		String message = "", createdBy="", createdAt="", userName="", password="", userPass="", executeURL="", oDataUrl="", userPartnerTypes="";
		JsonObject userCustomersObj = new JsonObject();
		JsonObject scfInsertResponseObj = new JsonObject();
		JSONObject scfInsertPayLoad = new JSONObject();
		long createdOnInMillis=0;
		JsonObject userPartnerJsonResponse = new JsonObject();
		
		try{
			createdBy = commonUtils.getUserPrincipal(request, "name", response);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			userPartnerJsonResponse = getUserPartnerTypesForAxis(response, loginID, aggregatorID, debug);
			JsonArray userPartnerJsonArray = userPartnerJsonResponse.getAsJsonObject("d").getAsJsonArray("results");
			JsonObject childUserPartnerJsonObj = new JsonObject();
			if(userPartnerJsonArray.size() > 0)
				childUserPartnerJsonObj = userPartnerJsonArray.get(0).getAsJsonObject();
			
			if ( childUserPartnerJsonObj.has("PartnerTypeID") &&  ! childUserPartnerJsonObj.get("PartnerTypeID").isJsonNull() )
				userPartnerTypes = childUserPartnerJsonObj.get("PartnerTypeID").getAsString();
			else
				userPartnerTypes = "";
			
			if (userPartnerTypes.equalsIgnoreCase("000002"))
			{
				userCustomersObj = getVendors(response, loginID, aggregatorID, debug);
			}
			else if (userPartnerTypes.equalsIgnoreCase("000003"))
			{
				userCustomersObj = getUserCustomersForAxis(response, loginID, aggregatorID, debug);
			}
			else {
				message = "Invalid Partner type maintained";
			}
			if(debug){
				response.getWriter().println("insertOfflineODAIntoSCF.userCustomersObj: "+userCustomersObj);
			}
			
			if (message != null && message.trim().length() > 0) {
				return message; 
			}else{
				if(! userCustomersObj.get("d").getAsJsonObject().get("ErrorCode").isJsonNull() && userCustomersObj.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().length() > 0){
					message = "000";
					return message;
				}else{
					JsonObject userCustomersResults = userCustomersObj.get("d").getAsJsonObject();
					JsonArray userCustomersResultsArray = userCustomersResults.get("results").getAsJsonArray();
					JsonObject assignedCustomer = new JsonObject();
					if(userCustomersResultsArray.size() == 1){
						assignedCustomer = userCustomersResultsArray.get(0).getAsJsonObject();
						if(debug){
							response.getWriter().println("insertOfflineODAIntoSCF.assignedCustomer: "+assignedCustomer);
						}
						
						scfInsertPayLoad.accumulate("ID", ""+commonUtils.generateGUID(36));
						scfInsertPayLoad.accumulate("CPGUID", assignedCustomer.get("PartnerID").getAsString());
//						scfInsertPayLoad.accumulate("CPTypeID", );
						if(userPartnerTypes.equalsIgnoreCase("000002"))
							scfInsertPayLoad.accumulate("CPTypeID", "60");
						else if(userPartnerTypes.equalsIgnoreCase("000003"))
							scfInsertPayLoad.accumulate("CPTypeID", "01");
						scfInsertPayLoad.accumulate("AggregatorID", aggregatorID);
						scfInsertPayLoad.accumulate("AccountNo", inputJsonObj.getString("BankAccntNo"));
						scfInsertPayLoad.accumulate("DDBActive", inputJsonObj.getString("DDBActive"));
						scfInsertPayLoad.accumulate("StatusID", "000002");
						scfInsertPayLoad.accumulate("CallBackStatus", "000080");
						scfInsertPayLoad.accumulate("CreatedBy", createdBy);
						scfInsertPayLoad.accumulate("CreatedAt", createdAt);
						scfInsertPayLoad.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
						//add the new fields ECompleteDate and ECompleteTime
						scfInsertPayLoad.accumulate("ECompleteDate", "/Date("+createdOnInMillis+")/");
						scfInsertPayLoad.accumulate("ECompleteTime", createdAt);
						

						oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
						password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
						userPass = userName+":"+password;
						executeURL = oDataUrl+"SupplyChainFinances";
			
						if(debug){
							response.getWriter().println("insertOfflineODAIntoSCF.executeURL: "+executeURL);
							response.getWriter().println("insertOfflineODAIntoSCF.oDataUrl: "+oDataUrl);
							response.getWriter().println("insertOfflineODAIntoSCF.userName: "+userName);
							response.getWriter().println("insertOfflineODAIntoSCF.scfInsertPayLoad: "+scfInsertPayLoad);
						}
						// log the scfInsertPayLoad Payload.
						scfInsertResponseObj = commonUtils.executePostURL(executeURL, userPass, response, scfInsertPayLoad, request, debug, "PYGWHANA");
						
						if(scfInsertResponseObj.has("error")){
							message = scfInsertResponseObj.get("error").getAsJsonObject().get("message").getAsJsonObject().get("value").getAsString();
						}
						return message;
					}else{
//						More than one partner is assigned for that login
						// log the below message value. print the message log More than one partner is assigned for that login
						message = "155";
						if(debug)
							response.getWriter().println("insertOfflineODAIntoSCF.message: "+message);
						return message;
					}
				}
			}
		}catch (Exception e) {
			// add the Logs if any exception occurred.
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){	
				response.getWriter().print("Exception in insertOfflineODAIntoSCF: "+e.getLocalizedMessage()+"-"+buffer.toString());
			}
			message = e.getLocalizedMessage()+"-"+buffer.toString();
			return message;
		}
	}
	
	public String insertIntoUserAccountsForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObj, Map<String, String> userRegMap, 
			String setSIForResponse, Map<String, String> accountsWSResponseMap, String aggregatorID, String loginID, boolean debug) throws IOException{
		String message = "", executeURL="", oDataUrl="", userName="", password="", userPass="", accountGuid="", createdBy="", createdAt="", accountStatusCode="";
		long createdOnInMillis=0;
		JsonObject insertResponse = new JsonObject();
		try{
			accountStatusCode = commonUtils.getAccountStatusCode(request, response, aggregatorID, accountsWSResponseMap.get("AccountStatus"), debug);
			createdBy =  commonUtils.getUserPrincipal(request, "name", response);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt =  commonUtils.getCreatedAtTime();
			
			JSONObject entryPayload = new JSONObject();
			
//			accountGuid = inputJsonObj.getString("UaccntGuid");
			if(debug){
				response.getWriter().println("insertIntoUserAccounts.inputJsonObj: "+inputJsonObj);
				response.getWriter().println("insertIntoUserAccounts.accountGuid: "+accountGuid);
				response.getWriter().println("insertIntoUserAccounts.entryPayload: "+entryPayload);
				response.getWriter().println("insertIntoUserAccounts.changedBy: "+createdBy);
				response.getWriter().println("insertIntoUserAccounts.changedOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoUserAccounts.changedAt: "+createdAt);
			}
			
			entryPayload.accumulate("UaccntGuid", inputJsonObj.getString("UaccntGuid"));
			entryPayload.accumulate("AggregatorID", aggregatorID);
			entryPayload.accumulate("LoginId", loginID);
			entryPayload.accumulate("Corpid", userRegMap.get("CorpId"));
			entryPayload.accumulate("Userid", userRegMap.get("UserId"));
			entryPayload.accumulate("BankCountry", "");
			entryPayload.accumulate("BankKey", "");
			entryPayload.accumulate("BankAccntNo", inputJsonObj.getString("BankAccntNo"));
			entryPayload.accumulate("BankAccntType", accountsWSResponseMap.get("AccountType"));
//			entryPayload.accumulate("BankAccntSts", accountsWSResponseMap.get("AccountStatus"));
			entryPayload.accumulate("BankAccntSts", accountStatusCode);
			entryPayload.accumulate("DDBActive", inputJsonObj.getString("DDBActive"));
			entryPayload.accumulate("CreatedBy", createdBy);
			entryPayload.accumulate("CreatedAt", createdAt);
			entryPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
			entryPayload.accumulate("ChangedBy", null);
			entryPayload.accumulate("ChangedAt", null);
			entryPayload.accumulate("ChangedOn", null);
			entryPayload.accumulate("Source", "");
			
			oDataUrl = commonUtils. getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils. getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils. getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataUrl+"UserAccounts";
			if(debug)
				response.getWriter().println("insertIntoUserAccounts.executeURL: "+executeURL);
			
			insertResponse =  commonUtils.executePostURL(executeURL, userPass, response, entryPayload, request, debug, "PYGWHANA");
			
			if(debug)
				response.getWriter().println("insertIntoUserAccounts.insertResponse: "+insertResponse);
			
			message = insertResponse.toString();
			
			return message;
		}catch (Exception e) {
//			message = e.getLocalizedMessage();
			message = "";
			return message;
		}
	}
	
	
	public String checkUniqueIDInUserAccountsForAxis(HttpServletResponse response, String accountGuid, String aggregatorID, boolean debug) throws IOException{
		String returnMessage = "", oDataUrl="", userName="", password="", userPass="", executeURL="";
		JsonObject scfJsonObject = new JsonObject();
		try{
			oDataUrl =commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName =commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
//			executeURL = oDataUrl+"SCF?$filter=CP_GUID%20eq%20%27"+cpGuid+"%27%20and%20CP_TYPE%20eq%20%27"+cpType+"%27%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27%20and%20STATUS_ID%20eq%20%27000003%27";
			executeURL = oDataUrl+"UserAccounts?$filter=UaccntGuid%20eq%20%27"+accountGuid+"%27";
			scfJsonObject = commonUtils.executeURL(executeURL, userPass, response);
			
			if(debug)
				response.getWriter().println("checkUniqueIDInUserAccounts.scfJsonObject: "+scfJsonObject);
//			scfJsonObject
			JsonObject results = scfJsonObject.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(dresults.size() > 0){
				returnMessage = "Given User Account Guid is already available";
			}
		}catch (Exception e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
	
	public JsonObject getUserCustomersForAxis(HttpServletResponse response, String loginID, String aggregatorID, boolean debug) throws IOException{
		JsonObject userCustomerObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		String oDataUrl="", userName="", password="", userPass="", executeURL="";
		try{
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataUrl+"UserCustomers?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27";
			userCustomerObj = commonUtils.executeURL(executeURL, userPass, response);
			
			JsonObject results = userCustomerObj.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			
			if(dresults.size() > 0){
				userCustomerObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
				userCustomerObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
			}else{
				userCustomerObj.get("d").getAsJsonObject().addProperty("ErrorCode", "000");
				userCustomerObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "No Records Found in Entity UserCustomers for the Login ID");
			}
			
			if(debug){
				response.getWriter().println("getUserCustomers.userCustomerObj: "+userCustomerObj);
			}
		}catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			errorResponseObj.addProperty("ErrorCode", "001");
			errorResponseObj.addProperty("ErrorMessage", e.getMessage()+".CommonUtils.getUserCustomers --> "+buffer.toString());
			userCustomerObj.add("d", errorResponseObj);
		}
		
		return userCustomerObj;
	}
	
	public JsonObject userAccountsChangeForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject, String loginID, 
			String aggregatorID, String oDataURL, Properties properties, boolean debug) throws IOException{
		JsonObject userAccChangeResponseToUI = new JsonObject();
		JsonObject bankAccountEntriesJson = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		JsonObject bankAccountEntriesJsonWithSI = new JsonObject();
		
		int userAccSize = 0;
		String message = "", accountNoFromUI="", ddbActiveFromUI="", accountTypeFromWS="", isCFSOFFromWS="";
		boolean hasUserRegistered = true, updateEntryFlag = false, isUpdateSuccess = false;
		Map<String, String> userRegMap = new HashMap<String, String>();
		Map<String,String> accountsWSResponseMap = new HashMap<String,String>();
		
		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_MESSAGE\"}"
				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\""
				+ ",\"service_version\":\"0001\"},\"transactionid\":\"8A50D8E98D62F127A8EC001372667F53\",\"timestamp\":null"
				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\""
				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\""
				+ ",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"}"
				+ ",\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\""
				+ ",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\""
				+ ",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
		try{
			if(debug){
				response.getWriter().println("userAccountsChange.inputJsonObject: "+inputJsonObject);
			}
			
			bankAccountEntriesJson = getUserAccountsInJsonForAxis(request, response, loginID, oDataURL, aggregatorID, "", properties, debug);
			if(debug){
				response.getWriter().println("userAccountsChange.bankAccountEntriesJson: "+bankAccountEntriesJson);
			}
			
			if(! bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").isJsonNull() 
					&& bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().length() > 0){
				/*errorResponseObj.addProperty("Status", "000000");
				errorResponseObj.addProperty("ErrorCode", bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString());
				errorResponseObj.addProperty("ErrorMessage", bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorMessage").getAsString());
				userAccChangeResponseToUI.add("d", errorResponseObj);*/
				String responseErrorCode = "", responseErrorMessage="";
				
				responseErrorCode = bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString();
				responseErrorMessage = bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorMessage").getAsString();
				
				errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
				errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
				
				userAccChangeResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(userAccChangeResponseToUI);
				if(debug)
					response.getWriter().println("userAccountsChange.Failure Response.userAccChangeResponseToUI: "+userAccChangeResponseToUI);
				
				return userAccChangeResponseToUI;
//				return userAccChangeResponseToUI;
			}else{
				userAccSize = getResultsSizeForAxis(response, bankAccountEntriesJson, debug);
				
				if(userAccSize > 0){
					message = validateAccountForChangeForAxis(response, inputJsonObject, bankAccountEntriesJson, loginID, aggregatorID, oDataURL, debug);
				}
				
				if(message != null && message.trim().length() > 0){
					if(message.equalsIgnoreCase("151")){
						errorResponseObj.addProperty("Status", "000000");
						errorResponseObj.addProperty("ErrorCode", message);
						errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
						userAccChangeResponseToUI.add("d", errorResponseObj);
					}else{
						errorResponseObj.addProperty("Status", "000000");
						errorResponseObj.addProperty("ErrorCode", "001");
						errorResponseObj.addProperty("ErrorMessage", message);
						userAccChangeResponseToUI.add("d", errorResponseObj);
					}
				}else{
					userRegMap = getUserRegDetailsForAxis(request, response, loginID, aggregatorID, oDataURL, debug);
					
					for (String key : userRegMap.keySet()) {
						if(debug)
							response.getWriter().println("userAccountsChange-userRegResponseMap: "+key + " - " + userRegMap.get(key));
						
						if(key.equalsIgnoreCase("Error")){
							if(userRegMap.get(key).equalsIgnoreCase("054") || userRegMap.get(key).equalsIgnoreCase("001")){
								hasUserRegistered = false;
								break;
							}else{
								if(! userRegMap.get("UserRegStatus").equalsIgnoreCase("000002")){
									hasUserRegistered = false;
									break;
								}
							}
						}
					}
					
					if(hasUserRegistered){
						if(debug){
							response.getWriter().println("userAccountsChange.loginID"+loginID);
							response.getWriter().println("userAccountsChange.oDataURL"+oDataURL);
							response.getWriter().println("userAccountsChange.aggregatorID"+aggregatorID);
						}
						//Call Accounts WS
						accountsWSResponseMap = getAccountDetailsForAxis(request, response, inputJsonObject, userRegMap, aggregatorID, debug);
						
						if(debug){
							for (String key : accountsWSResponseMap.keySet()) {
								response.getWriter().println("userAccountsChange-accountsWSResponseMap: "+key + " - " + accountsWSResponseMap.get(key));
							}
						}
						
						if(accountsWSResponseMap.get("Error").equalsIgnoreCase("059")){
							message = "059";
							errorResponseObj.addProperty("Status", "000000");
							errorResponseObj.addProperty("ErrorCode", message);
							errorResponseObj.addProperty("ErrorMessage", accountsWSResponseMap.get("Message"));
							userAccChangeResponseToUI.add("d", errorResponseObj);
						}else{
							bankAccountEntriesJsonWithSI = bankAccountEntriesJson;
							bankAccountEntriesJson = null;
							bankAccountEntriesJson = setStandingInstruction1ForAxis(request, response, bankAccountEntriesJsonWithSI, properties, loginID, aggregatorID, oDataURL, debug);
							if(debug)
								response.getWriter().println("userAccountsChange.bankAccountEntriesJsonWithSI: "+bankAccountEntriesJson);
							
							accountNoFromUI = inputJsonObject.getString("BankAccntNo");
							ddbActiveFromUI = inputJsonObject.getString("DDBActive");
							accountTypeFromWS = accountsWSResponseMap.get("AccountType");
							isCFSOFFromWS = accountsWSResponseMap.get("IsCFSOD");
							if(debug){
								response.getWriter().println("userAccountsChange.bankAccountEntriesJson: "+bankAccountEntriesJson);
								response.getWriter().println("userAccountsChange.accountTypeFromWS: "+accountTypeFromWS);
								response.getWriter().println("userAccountsChange.isCFSOFFromWS: "+isCFSOFFromWS);
							}
							
							if(accountTypeFromWS != null && accountTypeFromWS.trim().equalsIgnoreCase("ODA")){
								//When the Account Type is ODA
								if(isCFSOFFromWS != null && isCFSOFFromWS.trim().equalsIgnoreCase("Y")){
									//When the Account Type is ODA and ISCFSOD is 'Y'
									if(inputJsonObject.getString("DDBActive") != null && inputJsonObject.getString("DDBActive").trim().equalsIgnoreCase("")){
										message = "149";
										errorResponseObj.addProperty("Status", "000000");
										errorResponseObj.addProperty("ErrorCode", message);
										errorResponseObj.addProperty("ErrorMessage", accountsWSResponseMap.get("Message"));
										userAccChangeResponseToUI.add("d", errorResponseObj);
									}else{
										//Proceed to update
										if(debug)
											response.getWriter().println("update1");
										updateEntryFlag = true;
									}
								}else{
									//When the Account Type is ODA and ISCFSOD is 'N'
									if(inputJsonObject.getString("DDBActive") != null && inputJsonObject.getString("DDBActive").trim().equalsIgnoreCase("X")){
										message = "146";
										errorResponseObj.addProperty("Status", "000000");
										errorResponseObj.addProperty("ErrorCode", message);
										errorResponseObj.addProperty("ErrorMessage", accountsWSResponseMap.get("Message"));
										userAccChangeResponseToUI.add("d", errorResponseObj);
									}else{
										//Proceed to update
										if(debug)
											response.getWriter().println("update2");
										updateEntryFlag = true;
									}
								}
							}else if(accountTypeFromWS != null && accountTypeFromWS.trim().equalsIgnoreCase("CAA")){
								//When the Account Type is CAA
								JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
								JsonArray dresults = results.get("results").getAsJsonArray();
								accountNoFromUI = inputJsonObject.getString("BankAccntNo");
								ddbActiveFromUI = inputJsonObject.getString("DDBActive");
								boolean caaUpdValidation = true;
								for (int i = 0; i <= dresults.size() - 1; i++) {
									if(accountNoFromUI.equalsIgnoreCase(bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntNo").getAsString())){
										if(bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString().equalsIgnoreCase("O")){
											//Proceed to update
											if(debug)
												response.getWriter().println("update3");
											updateEntryFlag = true;
										}else{
											if(debug)
												response.getWriter().println("caaUpdValidation: "+caaUpdValidation);
											caaUpdValidation = false;
										}
									}
								}
								
								if(! caaUpdValidation){
									for (int i = 0; i <= dresults.size() - 1; i++) {
										if((bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA"))
											&& (bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("SetSI").getAsString().equalsIgnoreCase("X"))){
											if(ddbActiveFromUI.equalsIgnoreCase("X")){
												message = "148";
												errorResponseObj.addProperty("Status", "000000");
												errorResponseObj.addProperty("ErrorCode", message);
												errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
												userAccChangeResponseToUI.add("d", errorResponseObj);
											}
										}else{
											if((bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA"))
											&& (!bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))
											&& (! accountNoFromUI.equalsIgnoreCase(bankAccountEntriesJson.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntNo").getAsString()))){
												if(ddbActiveFromUI.equalsIgnoreCase("X")){
													message = "147";
													errorResponseObj.addProperty("Status", "000000");
													errorResponseObj.addProperty("ErrorCode", message);
													errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
													userAccChangeResponseToUI.add("d", errorResponseObj);
												}else{
													//Proceed to update
													if(debug)
														response.getWriter().println("update4");
													updateEntryFlag = true;
												}
											}else{
												//Proceed to update
												if(debug)
													response.getWriter().println("update5");
												updateEntryFlag = true;
											}
										}
									}
								}
							}else{
								//When the Account Type is other than ODA and CAA
								message = "146";
								errorResponseObj.addProperty("Status", "000000");
								errorResponseObj.addProperty("ErrorCode", message);
								errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
								userAccChangeResponseToUI.add("d", errorResponseObj);
							}
						}
					}
				}
				if(debug){
					response.getWriter().println("userAccountsChange.inputJsonObject: "+inputJsonObject);
					response.getWriter().println("userAccountsChange.bankAccountEntriesJson: "+bankAccountEntriesJson);
				}
				
				String responseErrorCode = "", responseErrorMessage="";
				if(updateEntryFlag){
					//Call Update
					userAccChangeResponseToUI = updateUserAccountsForAxis(request, response, inputJsonObject, bankAccountEntriesJson, debug);
					if(debug)
						response.getWriter().println("userAccountsChange.userAccChangeResponseToUI: "+userAccChangeResponseToUI);
					
					if(! userAccChangeResponseToUI.get("d").getAsJsonObject().isJsonNull()){
						if(! userAccChangeResponseToUI.get("d").getAsJsonObject().get("ErrorCode").isJsonNull()
							&& userAccChangeResponseToUI.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().equalsIgnoreCase("001")){
							//Update Failure
							responseErrorCode = "156";
							responseErrorMessage = properties.getProperty(responseErrorCode);
							
							errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
							errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
							
							userAccChangeResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							response.getWriter().println(userAccChangeResponseToUI);
							if(debug)
								response.getWriter().println("userAccountsChange.Failure Response.userAccChangeResponseToUI: "+userAccChangeResponseToUI);
							
							return userAccChangeResponseToUI;
						}else{
							//Update Success
							userAccChangeResponseToUI = new JsonObject();
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							response.getWriter().println(userAccChangeResponseToUI);
							return userAccChangeResponseToUI;
						}
					}else{
						//Update Success
						userAccChangeResponseToUI = new JsonObject();
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						response.getWriter().println(userAccChangeResponseToUI);
						return userAccChangeResponseToUI;
					}
				}else{
					//Error Response
					/*errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccChangeResponseToUI.add("d", errorResponseObj);*/
					
					responseErrorCode = message;
					responseErrorMessage = properties.getProperty(message);
					
					errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
					errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
					
					userAccChangeResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(userAccChangeResponseToUI);
					if(debug)
						response.getWriter().println("userAccountsChange.Failure Response.userAccChangeResponseToUI: "+userAccChangeResponseToUI);
					
					return userAccChangeResponseToUI;
				}
			}
		}catch (Exception e) {
			String responseErrorCode = "", responseErrorMessage="";
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in updateUserAccounts.message: "+buffer.toString());
			}
			
			/*message = "001";
			errorResponseObj.addProperty("Status", "000000");
			errorResponseObj.addProperty("ErrorCode", message);
			errorResponseObj.addProperty("ErrorMessage", e.getMessage());
			userAccChangeResponseToUI.add("d", errorResponseObj);*/
			
			
			responseErrorCode = message;
			responseErrorMessage = "Error in userAccountsChange: "+e.getMessage();
			
			errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
			errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
			
			userAccChangeResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(userAccChangeResponseToUI);
			if(debug)
				response.getWriter().println("userAccountsChange.Failure Response.userAccChangeResponseToUI: "+userAccChangeResponseToUI);
			
			return userAccChangeResponseToUI;
		}
		
	}
	
	public String validateAccountForChangeForAxis(HttpServletResponse response, JSONObject inputJsonObj, JsonObject bankAccountEntriesJson, String loginID, 
			String aggregatorID, String oDataURL, boolean debug) throws IOException{

		String message = "", accountNoFromUI= "", accountNo = "", oDataUrl="", userName = "", password="", userPass="", executeURL="";
		boolean isAccountAvailable = false;
//		JsonObject httpJsonResult = new JsonObject();
		try{
			accountNoFromUI = inputJsonObj.getString("BankAccntNo");
			if(null != accountNoFromUI && accountNoFromUI.trim().length() > 0){
				JsonObject results = bankAccountEntriesJson.get("d").getAsJsonObject();
				JsonArray dresults = results.get("results").getAsJsonArray();
//				message = "119";
				for (int i = 0; i <= dresults.size() - 1; i++) {
					JsonObject userAccJsonObj = (JsonObject) dresults.get(i);
					accountNo = userAccJsonObj.get("BankAccntNo").getAsString();
					if(debug)
						response.getWriter().println("validateAccount-accountNo: "+accountNo);
					if(accountNo.equalsIgnoreCase(accountNoFromUI)){
						isAccountAvailable = true;
						break;
					}
//					response.getWriter().println("value: "+legalStatusFromTSet);
				}
				
				if(! isAccountAvailable){
					message = "068";
				}else{
					message = "";
				}
			}
		}catch (Exception e) {
			message = e.getLocalizedMessage();
		}
		if(debug)
			response.getWriter().println("validateAccount-message: "+message);
		
		return message;
	
	}
	
	public JsonObject updateUserAccountsForAxis(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject, 
			JsonObject bankAccountEntriesJson, boolean debug) throws IOException{
		JsonObject updateReturnObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject();
		JsonObject entryToUpdate = new JsonObject();
		String message = "", accountGuidFromUI = "", changedBy="", changedAt="", oDataUrl="", userName="", password="", userPass="", executeURL="";
		long changedOnInMillis = 0;
		try{
			accountGuidFromUI = inputJsonObject.getString("UaccntGuid");
			entryToUpdate = getUserAccountEntryForUpdateForAxis(response, accountGuidFromUI, debug);
			if(debug)
				response.getWriter().println("updateUserAccounts.entryToUpdate: "+entryToUpdate);
			if(entryToUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")){
				changedBy =  commonUtils.getUserPrincipal(request, "name", response);
				changedOnInMillis =  commonUtils.getCreatedOnDate();
				changedAt =  commonUtils.getCreatedAtTime();
				
				JSONObject entryPayload = new JSONObject();
				
				if(debug){
					response.getWriter().println("updateUserAccounts.entryToUpdate: "+entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject());
					response.getWriter().println("updateUserAccounts.accountGuidFromUI: "+accountGuidFromUI);
					response.getWriter().println("updateUserAccounts.changedBy: "+changedBy);
					response.getWriter().println("updateUserAccounts.changedOnInMillis: "+changedOnInMillis);
					response.getWriter().println("updateUserAccounts.changedAt: "+changedAt);
				}
				
				entryPayload.accumulate("UaccntGuid", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("UaccntGuid").getAsString());
				entryPayload.accumulate("AggregatorID", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("AggregatorID").getAsString());
				entryPayload.accumulate("LoginId", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("LoginId").getAsString());
				entryPayload.accumulate("Corpid", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("Corpid").getAsString());
				entryPayload.accumulate("Userid", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("Userid").getAsString());
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankCountry").isJsonNull())
					entryPayload.accumulate("BankCountry", "");
				else
					entryPayload.accumulate("BankCountry", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankCountry").getAsString());
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankKey").isJsonNull())
					entryPayload.accumulate("BankKey", "");
				else
					entryPayload.accumulate("BankKey", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankKey").getAsString());
				
				entryPayload.accumulate("BankAccntNo", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankAccntNo").getAsString());
				entryPayload.accumulate("BankAccntType", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankAccntType").getAsString());
				entryPayload.accumulate("BankAccntSts", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("BankAccntSts").getAsString());
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedBy").isJsonNull()){
					entryPayload.accumulate("CreatedBy", "");
				}else{
					entryPayload.accumulate("CreatedBy", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedBy").getAsString());	
				}
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedAt").isJsonNull()){
					entryPayload.accumulate("CreatedAt", JSONObject.NULL);
				}else{
					entryPayload.accumulate("CreatedAt", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedAt").getAsString());
				}
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedOn").isJsonNull()){
					entryPayload.accumulate("CreatedOn", JSONObject.NULL);
				}else{
					entryPayload.accumulate("CreatedOn", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("CreatedOn").getAsString());	
				}
				
				entryPayload.accumulate("ChangedBy", changedBy);
				entryPayload.accumulate("ChangedAt", changedAt);
				entryPayload.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
				
				if(entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("Source").isJsonNull())
					entryPayload.accumulate("Source", "");
				else
					entryPayload.accumulate("Source", entryToUpdate.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("Source").getAsString());
			
				entryPayload.accumulate("DDBActive", inputJsonObject.getString("DDBActive"));
				
				oDataUrl =  commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userName =  commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				userPass = userName+":"+password;
				
				executeURL = oDataUrl+"UserAccounts('"+accountGuidFromUI+"')";
				if(debug)
					response.getWriter().println("updateUserAccounts.executeURL: "+executeURL);
				
				updateReturnObj =  commonUtils.executeUpdate(executeURL, userPass, response, entryPayload, request, debug, "PYGWHANA");
				if(debug){
					response.getWriter().println("updateUserAccounts.updateReturnObjBefore: "+updateReturnObj);
				}
				
				if(! updateReturnObj.get("ErrorCode").isJsonNull() && updateReturnObj.get("ErrorCode").getAsString().trim().length() > 0){
					message = "001";
					errorResponseObj.addProperty("Status", "000001");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", updateReturnObj.toString());
					updateReturnObj = new JsonObject();
					updateReturnObj.add("d", errorResponseObj);
				}else{
					message = "000";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", "Update Success");
					updateReturnObj = new JsonObject();
					updateReturnObj.add("d", errorResponseObj);
				}
				if(debug){
					response.getWriter().println("updateUserAccounts.updateReturnObj: "+updateReturnObj);
				}
			}else{
				message = entryToUpdate.get("d").getAsJsonObject().get("ErrorCode").getAsString();
				errorResponseObj.addProperty("Status", "000000");
				errorResponseObj.addProperty("ErrorCode", message);
				errorResponseObj.addProperty("ErrorMessage", entryToUpdate.get("d").getAsJsonObject().get("ErrorMessage").getAsString());
				updateReturnObj.add("d", errorResponseObj);
			}
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in updateUserAccounts.message: "+buffer.toString());
			}
			message = "001";
			errorResponseObj.addProperty("Status", "000000");
			errorResponseObj.addProperty("ErrorCode", message);
			errorResponseObj.addProperty("ErrorMessage", e.getMessage());
			updateReturnObj.add("d", errorResponseObj);
		}
		
		return updateReturnObj;
	}
	
	public JsonObject deleteAccountForAxis(HttpServletRequest request, HttpServletResponse response, String accountGuid, String loginID, 
			String aggregatorID, String oDataURL, Properties properties, boolean debug) throws IOException{
		JsonObject userAccDeleteResponseToUI = new JsonObject();
		String accountGuidFromUI="", oDataUrl="", userName="", password="", userPass="", executeURL="";
		String responseErrorCode = "", responseErrorMessage="";
		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_MESSAGE\"}"
				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\""
				+ ",\"service_version\":\"0001\"},\"transactionid\":\"8A50D8E98D62F127A8EC001372667F53\",\"timestamp\":null"
				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\""
				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\""
				+ ",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"}"
				+ ",\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\""
				+ ",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\""
				+ ",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\""
				+ ",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
		
		try{
			accountGuidFromUI = accountGuid;
			
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			executeURL = oDataUrl+"UserAccounts('"+accountGuidFromUI+"')";
			if(debug)
				response.getWriter().println("deleteAccount.executeURL: "+executeURL);
			
			userAccDeleteResponseToUI = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PYGWHANA");
			
			if(debug){
				response.getWriter().println("deleteAccount.userAccDeleteResponseToUI: "+userAccDeleteResponseToUI);
			}
			
			if(! userAccDeleteResponseToUI.get("ErrorCode").isJsonNull() 
					&& userAccDeleteResponseToUI.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
				String successResponse = "{\"code\":\"/ARTEC/PY/046\",\"message\":\"Account Deleted Sucessfully\",\"severity\":\"info\",\"target\":\"\",\"details\":[]}";
				userAccDeleteResponseToUI = new JsonParser().parse(successResponse).getAsJsonObject();
				return userAccDeleteResponseToUI;
			}else{
				//Update Failure
				responseErrorCode = "047";
				responseErrorMessage = properties.getProperty(responseErrorCode);
				
				errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
				errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
				
				userAccDeleteResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
//				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
				if(debug)
					response.getWriter().println("deleteAccount.Failure Response.userAccChangeResponseToUI: "+userAccDeleteResponseToUI);
				
//				response.getWriter().println(userAccDeleteResponseToUI);
				return userAccDeleteResponseToUI;
			}
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in deleteAccount.message: "+buffer.toString());
			}
			responseErrorCode = "047";
			responseErrorMessage = properties.getProperty(responseErrorCode);
			
			errorResponse = errorResponse.replaceAll("ERROR_CODE", responseErrorCode);
			errorResponse = errorResponse.replaceAll("ERROR_MESSAGE", responseErrorMessage);
			
			userAccDeleteResponseToUI = new JsonParser().parse(errorResponse).getAsJsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			if(debug)
				response.getWriter().println("deleteAccount.Failure Response.userAccChangeResponseToUI: "+userAccDeleteResponseToUI);
			
			response.getWriter().println(userAccDeleteResponseToUI);
			
			return userAccDeleteResponseToUI;
		}
	}
	
	public JsonObject setStandingInstruction1GetForAxis(HttpServletRequest request, HttpServletResponse response, JsonObject userAccountsEntries, 
			Properties properties, String loginID, String aggregatorID, String oDataURL, boolean debug) throws IOException{

		JsonObject userAccountsResponseObj = new JsonObject();
		JsonObject errorResponseObj = new JsonObject(); 
		Map<String,String> pyeactEntries = new HashMap<String,String>();
		//Map<String,String> userAccountsEntry = new HashMap<String,String>();
		Map<String,String> scpEntry = new HashMap<String,String>();
		Map<String,String> accountsWSResponse = new HashMap<String,String>();
		String message="";
		JSONObject inputJsonObject = new JSONObject();
		boolean odAccAvailableY = false;
		try{
//			pyeactEntries = getPYEACTEntries(request, response, inputJsonObject, loginID, debug); //PYEACT typeset values
			/*userAccountsEntry = getODAUserAccountEntryForAxis(request, response, inputJsonObject, loginID, pyeactEntries, oDataURL, aggregatorID, debug);
			// need to log the userAccountsEntry. if more then 1000 character truncate it ErrorMessage,UserMessage Filed.
			if(debug){
				for (String key : userAccountsEntry.keySet()) {
					response.getWriter().println("setStandingInstruction1-userAccountsEntry: "+key + " - " + userAccountsEntry.get(key));
				}
			}*/
			
			scpEntry=getScpEntryForAxis(loginID,aggregatorID,oDataURL,response,debug);
			if(debug){
				response.getWriter().println("scpEntry:"+scpEntry);
			}
			
			if(scpEntry.get("Error").equalsIgnoreCase("")){
				//When ODA account is available in UserAccounts
				//Call Accounts WS
				accountsWSResponse = callAccountsWSForAxis(request, response, scpEntry, loginID, aggregatorID, "", oDataURL, inputJsonObject,properties, debug);
		// need to log the accountsWSResponse
				if(debug){
					for (String key : accountsWSResponse.keySet()) {
						response.getWriter().println("setStandingInstruction1-accountsWSResponse: "+key + " - " + accountsWSResponse.get(key));
					}
				}
				
				if(accountsWSResponse.get("Error").equalsIgnoreCase("054")){
					message = "054";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("059")){
					message = "059";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", accountsWSResponse.get("Message"));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("067")){
					message = "067";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}
				else if(accountsWSResponse.get("Error").equalsIgnoreCase("065")){
					message = "065";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("066")){
					message = "066";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else if(accountsWSResponse.get("Error").equalsIgnoreCase("001")){
					message = "001";
					errorResponseObj.addProperty("Status", "000000");
					errorResponseObj.addProperty("ErrorCode", message);
					errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
					userAccountsResponseObj.add("d", errorResponseObj);
				}else{
					userAccountsResponseObj = userAccountsEntries;
					boolean isDDBActive = false;
					String selectedAccNo = scpEntry.get("BankAccntNo");
					// need to log the selectedAccNo
					if(debug){
						response.getWriter().println("selectedAccNo: "+selectedAccNo);
						response.getWriter().println("setStandingInstruction.IsCFSOD: "+accountsWSResponse.get("IsCFSOD"));
					}
					
					// need to the log the IsCFSOD Property value from accountsWSResponse JsonObjetc
					if(accountsWSResponse.get("IsCFSOD").equalsIgnoreCase("Y")){
						odAccAvailableY = true;
						
						JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
						JsonArray dresults = results.get("results").getAsJsonArray();
						for (int i = 0; i <= dresults.size() - 1; i++) {
							if(selectedAccNo.equalsIgnoreCase(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntNo").getAsString())){
								//Setting SI as X and CFS OD as Y when the account is CFSOD
								if(debug)
									response.getWriter().println("1");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "X");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "Y");
							}else{
								//All other accounts - setSI as ""
								if(debug)
									response.getWriter().println("2");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
						}
						// need to log the userAccountsResponseObj 
					}else{
						//When ODA account is not CFSOD
						userAccountsResponseObj = userAccountsEntries;
					
						JsonObject results1 = userAccountsResponseObj.get("d").getAsJsonObject();
						JsonArray dresults1 = results1.get("results").getAsJsonArray();
						for (int i = 0; i <= dresults1.size() - 1; i++) {
							if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")){
								//When  CFS OD account in "N"
								if(debug)
									response.getWriter().println("3");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}else{
								//If CFS OD is N, then CAA logic added
								if(debug)
									response.getWriter().println("4");
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
										&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
									if(debug)
										response.getWriter().println("5");
									isDDBActive = true;
								}
							}
						}
						// need to log the userAccountsResponseObj in the Message1 or Message2 Property put the Message if ODAccount CFSOD
						// log the isDDBActive
						if(isDDBActive){
							userAccountsResponseObj = userAccountsEntries;
							JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
							JsonArray dresults = results.get("results").getAsJsonArray();
							for (int i = 0; i <= dresults.size() - 1; i++) {
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
										&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
									//Set SI as O only when CAA is available and DDBActive is X
									if(debug)
										response.getWriter().println("6");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
								}else if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")
										&& userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("")){
									//Set SI as "" only when CAA is available and DDBActive is ""
									if(debug)
										response.getWriter().println("7");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}else{
									//All other Account Types other than CAA and ODA - set SI as ""
									if(debug)
										response.getWriter().println("8");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
							
							// need to log the userAccountsResponseObj if isDDBActive is true
						}else{
							userAccountsResponseObj = userAccountsEntries;
							JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
							JsonArray dresults = results.get("results").getAsJsonArray();
							for (int i = 0; i <= dresults.size() - 1; i++) {
								if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")){
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
									if(debug)
										response.getWriter().println("9");
								}else{
									if(debug)
										response.getWriter().println("10");
									userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
								}
								
								userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
							}
							
							// need to log the userAccountsResponseObj if isDDBActive is false
						}
					}
					
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("Status", "000001");
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
					userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
				}
				// need to log the userAccountsResponseObj
			}else{
				//No ODA account available in UserAccounts
				if(debug)
					response.getWriter().println("No ODA Account Available");
				boolean isDDBActive = false;
				userAccountsResponseObj = userAccountsEntries;
				JsonObject results1 = userAccountsResponseObj.get("d").getAsJsonObject();
				JsonArray dresults1 = results1.get("results").getAsJsonArray();
				for (int i = 0; i <= dresults1.size() - 1; i++) {
					if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
							&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
						if(debug)
							response.getWriter().println("11");
						isDDBActive = true;
					}
				}
				if(debug)
					response.getWriter().println("No ODA Account AvailableisDDBActive: "+isDDBActive);
				// need to the isDDBActive Message1 ODA Account not Available
				if(isDDBActive){
					userAccountsResponseObj = userAccountsEntries;
					JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
					JsonArray dresults = results.get("results").getAsJsonArray();
					for (int i = 0; i <= dresults.size() - 1; i++) {
						if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")
								&& (!userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").isJsonNull() && userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("X"))){
							//Set SI as O only when CAA is available and DDBActive is X
							if(debug)
								response.getWriter().println("12");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
						}else if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("ODA")
								&& userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("DDBActive").getAsString().equalsIgnoreCase("")){
							//Set SI as "" only when CAA is available and DDBActive is ""
							if(debug)
								response.getWriter().println("13");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}else{
							//All other Account Types other than CAA and ODA - set SI as ""
							if(debug)
								response.getWriter().println("14");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}
						userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
					}
					// need to log the userAccountsResponseObj Message1=ODAccount Not Available
				}else{
					userAccountsResponseObj = userAccountsEntries;
					JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
					JsonArray dresults = results.get("results").getAsJsonArray();
					for (int i = 0; i <= dresults.size() - 1; i++) {
						if(userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().get("BankAccntType").getAsString().equalsIgnoreCase("CAA")){
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "O");
							if(debug)
								response.getWriter().println("15");
						}else{
							if(debug)
								response.getWriter().println("16");
							userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("SetSI", "");
						}
						
						userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("IsCFSODA", "N");
					}
					// need to log the userAccountsResponseObj.
				}
				
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("Status", "000001");
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorCode", "");
				userAccountsResponseObj.get("d").getAsJsonObject().addProperty("ErrorMessage", "");
				
			}
			
			if(debug)
				response.getWriter().println("setStandingInstruction1.userAccountsResponseObj before setting description: "+userAccountsResponseObj);
			//Setting Description Fields
			String accountStatusDesc="", accountTypeDesc="";
			
			if(! userAccountsResponseObj.get("d").getAsJsonObject().get("Status").getAsString().equalsIgnoreCase("000000")){
				JsonObject results = userAccountsResponseObj.get("d").getAsJsonObject();
				JsonArray dresults = results.get("results").getAsJsonArray();
				for (int i = 0; i <= dresults.size() - 1; i++) {
					accountTypeDesc = getAccountTypeDescForAxis(request, response, aggregatorID, dresults.get(i).getAsJsonObject().get("BankAccntType").getAsString(), debug);
					accountStatusDesc = getAccountStatusDescForAxis(request, response, aggregatorID, dresults.get(i).getAsJsonObject().get("BankAccntSts").getAsString(), debug);
				
					userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("BankAccStsDs", accountStatusDesc);
					userAccountsResponseObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("BankAccTypeDs", accountTypeDesc);
				}
			}
		}catch (Exception e) {
			message = "001";
			errorResponseObj.addProperty("ErrorCode", message);
			errorResponseObj.addProperty("ErrorMessage", properties.getProperty(message));
			userAccountsResponseObj.add("d", errorResponseObj);
			
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception-setStandingInstruction: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			}
		}
		if(debug)
			response.getWriter().println("setStandingInstruction.userAccountsResponseObj: "+userAccountsResponseObj);
		
		return userAccountsResponseObj;
	
	}

	private Map<String, String> getScpEntryForAxis(String loginID, String aggregatorID, String oDataURL, HttpServletResponse response, boolean debug) {
		String userName="", passWord="", userPass="", executeURL="", acccountType="";
		Map<String,String> scpEntry = new HashMap<String,String>();
		JsonObject httpJsonResult = new JsonObject();
		try{
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+passWord;
			
			executeURL = oDataURL+"SupplyChainPartners?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("SupplyChainPartners-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			if(debug)
				response.getWriter().println("SupplyChainPartners-results: "+results);
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("SupplyChainPartners-dresults: "+dresults);
			if(debug)
				response.getWriter().println("SupplyChainPartners-Size: "+dresults.size());
			
			if(dresults.size() == 0){
				scpEntry.put("Message", "ODAccount Details not found");
				scpEntry.put("Status","000002");
			}else{
//				userAccountsEntry.put("Error", "076");
				//dresults.get(0).getAsJsonObject().get(memberName)
				String scpGuid=dresults.get(0).getAsJsonObject().get("ID").getAsString();
				String aggrId = dresults.get(0).getAsJsonObject().get("AggregatorId").getAsString();
				
				executeURL = oDataURL+"SupplyChainPartnerBankData?$filter=AggregatorID%20eq%20%27"+aggrId+"%27%20and%20ReferenceID%20eq%20%27"+scpGuid+"%27";
				
				if(debug)
					response.getWriter().println("SupplyChainPartnerBankData-executeURL: "+executeURL);
				
				httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
				
				  results = httpJsonResult.get("d").getAsJsonObject();
				if(debug){
					response.getWriter().println("SupplyChainPartnerBankData-results: "+results);
				}
				
				 dresults = results.get("results").getAsJsonArray();
				if (dresults.size() > 0) {
					JsonObject scpBankObj = dresults.get(0).getAsJsonObject();
					if (!scpBankObj.get("BankAccntNo").isJsonNull()) {
						scpEntry.put("BankAccntNo", scpBankObj.get("BankAccntNo").getAsString());
						scpEntry.put("Message", "");
						scpEntry.put("Status","000001");
					} else {
						scpEntry.put("Message", "ODAccount Details not found");
						scpEntry.put("Status","000002");
					}
				} else {
					scpEntry.put("Message", "ODAccount Details not found");
					scpEntry.put("Status","000002");
				}

				 
				
				/*boolean isValidAccType=false;
				for (int i = 0; i <= dresults.size() - 1; i++) {
					userAccountsJsonObj = (JsonObject) dresults.get(i);
					if(debug)
						response.getWriter().println("userAccountsJsonObj-get(i): "+userAccountsJsonObj);
					
					if(! userAccountsJsonObj.get("BankAccntType").isJsonNull()){
						acccountType = userAccountsJsonObj.get("BankAccntType").getAsString();
					}
					
					if(debug){
						response.getWriter().println("getUserAccounts-acccountType: "+acccountType);
					}
					
					if(null != acccountType && acccountType.equalsIgnoreCase("ODA")){
						isValidAccType=true;
						
						userAccountsEntry.put("Error", "");
						
						if(! userAccountsJsonObj.get("BankAccntType").isJsonNull())
							userAccountsEntry.put("BankAccntType", userAccountsJsonObj.get("BankAccntType").getAsString());
						else
							userAccountsEntry.put("BankAccntType", "");
						
						if(! userAccountsJsonObj.get("Corpid").isJsonNull())
							userAccountsEntry.put("Corpid", userAccountsJsonObj.get("Corpid").getAsString());
						else
							userAccountsEntry.put("Corpid", "");
						
						if(! userAccountsJsonObj.get("Userid").isJsonNull())
							userAccountsEntry.put("Userid", userAccountsJsonObj.get("Userid").getAsString());
						else
							userAccountsEntry.put("Userid", "");
						
						if(! userAccountsJsonObj.get("BankCountry").isJsonNull())
							userAccountsEntry.put("BankCountry", userAccountsJsonObj.get("BankCountry").getAsString());
						else
							userAccountsEntry.put("BankCountry", "");
						
						if(! userAccountsJsonObj.get("BankCountry").isJsonNull())
							userAccountsEntry.put("BankCountry", userAccountsJsonObj.get("BankCountry").getAsString());
						else
							userAccountsEntry.put("BankCountry", "");
						
						if(! userAccountsJsonObj.get("BankKey").isJsonNull())
							userAccountsEntry.put("BankKey", userAccountsJsonObj.get("BankKey").getAsString());
						else
							userAccountsEntry.put("BankKey", "");
						
						if(! userAccountsJsonObj.get("BankAccntNo").isJsonNull())
							userAccountsEntry.put("BankAccntNo", userAccountsJsonObj.get("BankAccntNo").getAsString());
						else
							userAccountsEntry.put("BankAccntNo", "");
						
						if(! userAccountsJsonObj.get("BankAccntSts").isJsonNull())
							userAccountsEntry.put("BankAccntSts", userAccountsJsonObj.get("BankAccntSts").getAsString());
						else
							userAccountsEntry.put("BankAccntSts", "");
						
						if(! userAccountsJsonObj.get("DDBActive").isJsonNull())
							userAccountsEntry.put("DDBActive", userAccountsJsonObj.get("DDBActive").getAsString());
						else
							userAccountsEntry.put("DDBActive", "");
						
						break;
					}
					
					if(isValidAccType){
						break;
					}
				}
				
				if(! isValidAccType){
					userAccountsEntry.put("Error", "999");
				}*/
			}
		}catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			StringBuffer bffer=new StringBuffer();
			if(e.getLocalizedMessage()!=null){
				bffer.append(e.getLocalizedMessage()+"-->");
			}
			for(int i=0;i<stackTrace.length;i++){
				bffer.append(stackTrace[i]);
			}
			scpEntry.put("Message", bffer.toString());
			scpEntry.put("Status","000002");
		}
		return scpEntry;
	}
	
	
	
	private Map<String, String> getAccNoFrmScfTable(String loginID, String aggregatorID,HttpServletResponse response,Properties properties, boolean debug) {
		String userName="", passWord="", userPass="", executeURL="", acccountType="";
		Map<String,String> scfEntry = new HashMap<String,String>();
		JsonObject httpJsonResult = new JsonObject();
		String message="";
		try{
			message=properties.getProperty("ODAccountLinkingFailureMsg");
			String odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			userPass=username+":"+password;
			if (debug) {
				response.getWriter().println("url:" + odataUrl);
				response.getWriter().println("password:" + password);
				response.getWriter().println("username:" + username);
			}
			
			executeURL = odataUrl+"SupplyChainPartners?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("SupplyChainPartners-executeURL: "+executeURL);
			
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			if(debug)
				response.getWriter().println("SupplyChainPartners-results: "+results);
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("SupplyChainPartners-dresults: "+dresults);
			if(debug)
				response.getWriter().println("SupplyChainPartners-Size: "+dresults.size());
			
			if(dresults.size() == 0){
				scfEntry.put("Message", message);
				scfEntry.put("Status","000002");
			}else{
//				userAccountsEntry.put("Error", "076");
				//dresults.get(0).getAsJsonObject().get(memberName)
				String cpGuid=dresults.get(0).getAsJsonObject().get("SCPGuid").getAsString();
				String cpType = dresults.get(0).getAsJsonObject().get("SCPType").getAsString();
				if(cpType.equals("000003")){
					cpType="01";
				}else{
					cpType="60";
				}
				if(debug){
					response.getWriter().println("cpType:"+cpType);
				}
				
				executeURL = odataUrl+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20StatusID%20eq%20%27"+"000002"+"%27%20and%20CPGUID%20eq%20%27"+cpGuid+"%27%20and%20CPTypeID%20eq%20%27"+cpType+"%27";
				
				if(debug)
					response.getWriter().println("SupplyChainPartnerBankData-executeURL: "+executeURL);
				
				httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
				
				  results = httpJsonResult.get("d").getAsJsonObject();
				if(debug){
					response.getWriter().println("SupplyChainPartnerBankData-results: "+results);
				}
				
				 dresults = results.get("results").getAsJsonArray();
				if (dresults.size() > 0) {
					JsonObject scfObj = dresults.get(0).getAsJsonObject();
					if (!scfObj.get("AccountNo").isJsonNull()) {
						scfEntry.put("BankAccntNo", scfObj.get("AccountNo").getAsString());
						scfEntry.put("Message", "");
						scfEntry.put("Status","000001");
					} else {
						scfEntry.put("Message", message);
						scfEntry.put("Status","000002");
					}
				} else {
					scfEntry.put("Message", message);
					scfEntry.put("Status","000002");
				}

				
			}
		}catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			StringBuffer bffer=new StringBuffer();
			if(e.getLocalizedMessage()!=null){
				bffer.append(e.getLocalizedMessage()+"-->");
			}
			for(int i=0;i<stackTrace.length;i++){
				bffer.append(stackTrace[i]);
			}
			scfEntry.put("Message", bffer.toString());
			scfEntry.put("Status","000002");
		}
		return scfEntry;
	}
	
	
	
	
}

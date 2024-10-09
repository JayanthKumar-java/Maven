package com.arteriatech.cf;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.arteriatech.support.DestinationUtils;

public class ChannelFinanceOps {

	public JsonObject insertPGPaymentCategories(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",pgCategoryID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonArray insertPGCategoryArray = new JsonArray();
		JsonObject insertPGCategoryJson = new JsonObject();
		JsonObject insertPGCategoryMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject pgPymntCategoryPOSTResponse = null;
		JsonObject pgPymntCategoryJson =null;
		successResonse ="{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PCGW/PGPaymentCategories"
				+ "(AggregatorID='',PGCategoryID='')\",\"uri\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PCGW/PGPaymentCategories"
				+ "(Application='',LoginID='',ERPSystemID='',PartnerID='')\",\"type\":\"ARTEC.PCGW.PGPaymentCategories\"},\"AggregatorID\":\"\",\"PGCategoryID\":\"\",\"CheckFinanceBlock\":\"\","
				+ "\"BDCPostingEnabled\":\"\",\"BankPaymentTransactionType\":\"\",\"NumberRangeObject\":\"\","
				+ "\"NumberRangeSubObject\":\"\",\"IsNRFiscalYearDependent\":\"\",\"TrackIDPrefix\":\"\",\"PaymProcessingSequence\":\"\","
				+ "\"ERPPostIndforPaymPending\":\"\",\"TestRun\":\"X\"}}";
		try{
			
			 if (inputPayload.has("PGPaymentCategories")) {
				 jsonArrayPayload = inputPayload.getJSONArray("PGPaymentCategories");
			 } else {
				 jsonArrayPayload.put(inputPayload);
			 }
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validatePGCategoriesInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug); //validatePGCategoriesInsert
			if(debug)
				response.getWriter().println("insertPGPaymentCategories.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					pgPymntCategoryPOSTResponse = new JsonObject();
					pgPymntCategoryJson = new JsonObject();
					executeURL =""; requestAggrID=""; pgCategoryID="";
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);

					requestAggrID = childObjPayload.getString("AggregatorID");
					pgCategoryID = childObjPayload.getString("PGCategoryID");
					
					executeURL = oDataUrl+"PGPaymentCategories?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20PGCategoryID%20eq%20%27"+pgCategoryID+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertPGPaymentCategories.executeURL: "+executeURL);
					
					pgPymntCategoryJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertPGPaymentCategories.pgPymntCategoryJson: "+pgPymntCategoryJson);
					
					JsonArray pgPymntCategoryArrayJson = pgPymntCategoryJson.getAsJsonObject("d").getAsJsonArray("results");
					
					if (pgPymntCategoryArrayJson.size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
//						    JsonObject childPGPymntCatJson = pgPymntCategoryArrayJson.get(0).getAsJsonObject();
							
							executeURL = oDataUrl+"PGPaymentCategories";

							insertPayload.accumulate("AggregatorID", requestAggrID);
							insertPayload.accumulate("PGCategoryID", pgCategoryID);
							
							if(childObjPayload.has("CheckFinanceBlock") && ! childObjPayload.isNull("CheckFinanceBlock"))
								insertPayload.accumulate("CheckFinanceBlock", childObjPayload.getString("CheckFinanceBlock"));
							else
								insertPayload.accumulate("CheckFinanceBlock", "");
							
							if(childObjPayload.has("BDCPostingEnabled") && ! childObjPayload.isNull("BDCPostingEnabled"))
								insertPayload.accumulate("BDCPostingEnabled", childObjPayload.getString("BDCPostingEnabled"));
							else
								insertPayload.accumulate("BDCPostingEnabled", "");
							
							if(childObjPayload.has("BankPaymentTransactionType") && ! childObjPayload.isNull("BankPaymentTransactionType"))
								insertPayload.accumulate("BankPaymentTransactionType", childObjPayload.getString("BankPaymentTransactionType"));
							else
								insertPayload.accumulate("BankPaymentTransactionType", "");
							
							if(childObjPayload.has("NumberRangeObject") && ! childObjPayload.isNull("NumberRangeObject"))
								insertPayload.accumulate("NumberRangeObject", childObjPayload.getString("NumberRangeObject"));
							else
								insertPayload.accumulate("NumberRangeObject", "");
							
							if(childObjPayload.has("NumberRangeSubObject") && ! childObjPayload.isNull("NumberRangeSubObject"))
								insertPayload.accumulate("NumberRangeSubObject", childObjPayload.getString("NumberRangeSubObject"));
							else
								insertPayload.accumulate("NumberRangeSubObject", "");
							
							if(childObjPayload.has("IsNRFiscalYearDependent") && ! childObjPayload.isNull("IsNRFiscalYearDependent"))
								insertPayload.accumulate("IsNRFiscalYearDependent", childObjPayload.getString("IsNRFiscalYearDependent"));
							else
								insertPayload.accumulate("IsNRFiscalYearDependent", "");
							
							if(childObjPayload.has("TrackIDPrefix") && ! childObjPayload.isNull("TrackIDPrefix"))
								insertPayload.accumulate("TrackIDPrefix", childObjPayload.getString("TrackIDPrefix"));
							else
								insertPayload.accumulate("TrackIDPrefix", "");
							
							if(childObjPayload.has("PaymProcessingSequence") && ! childObjPayload.isNull("PaymProcessingSequence"))
								insertPayload.accumulate("PaymProcessingSequence", childObjPayload.getString("PaymProcessingSequence"));
							else
								insertPayload.accumulate("PaymProcessingSequence", "");
							
							if(childObjPayload.has("ERPPostIndforPaymPending") && ! childObjPayload.isNull("ERPPostIndforPaymPending"))
								insertPayload.accumulate("ERPPostIndforPaymPending", childObjPayload.getString("ERPPostIndforPaymPending"));
							else
								insertPayload.accumulate("ERPPostIndforPaymPending", "");
							if(debug)
								response.getWriter().println("insertPGPaymentCategories.insertPayload "+(i)+": "+insertPayload);
							
							pgPymntCategoryPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertPGPaymentCategories.pgPymntCategoryPOSTResponse: "+pgPymntCategoryPOSTResponse);
							
							if (pgPymntCategoryPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertPGCategoryArray.add(pgPymntCategoryPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertPGCategoryMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertPGCategoryMasterJson.addProperty("ErrorCode", errorCode);
				insertPGCategoryMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertPGCategoryMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					insertPGCategoryMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertPGCategoryMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if (! insertPGCategoryMasterJson.has("d")){
					insertPGCategoryJson.add("results", insertPGCategoryArray);
					insertPGCategoryMasterJson.add("d", insertPGCategoryJson);
				}
				insertPGCategoryMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertPGCategoryMasterJson = new JsonObject();
			insertPGCategoryMasterJson.addProperty("ErrorCode", "E000");
			insertPGCategoryMasterJson.addProperty("ResponseCode", "000003");
			insertPGCategoryMasterJson.addProperty("Message", buffer.toString());
		}
		return insertPGCategoryMasterJson;
	}
	
	//added by kamlesh for request fields validation 29-05-2020
	public JsonObject validatePGCategoriesInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			 Properties properties, boolean debug) throws IOException
	{
		String executeURL ="", pgCatID="", errorMsg="", errorCOde="";
		boolean isSuccess = false;
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {

//			executeURL = oDataUrl+"ConfigTypesetTypes?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20Typeset%20eq%20%27PGCAT%27";
//			if(debug)
//				response.getWriter().println("validatePGCategoriesInsert.executeURL: "+executeURL);

			JsonObject getConfigTypesJson = getConfigTypesetByTypes(response, oDataUrl, userPass, aggregatorID, "PGCAT", debug);
			if(debug)
				response.getWriter().println("validatePGCategoriesInsert.getConfigTypesJson: "+getConfigTypesJson);
			
			JsonArray pgPymntCatJsonArray= getConfigTypesJson.getAsJsonObject("d").getAsJsonArray("results");
			if(debug)
				response.getWriter().println("validatePGCategoriesInsert.pgPymntCatJsonArray: "+pgPymntCatJsonArray);
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			if (pgPymntCatJsonArray.size() > 0) {
				
				for (int i = 0; i < inputArrayPayload.length(); i++) {
					
					isSuccess = false;
					errorMsg="";pgCatID="";
					inputPayload = inputArrayPayload.getJSONObject(i);
					
					if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
						errorMsg = "AggregatorID";
					
					if (inputPayload.isNull("PGCategoryID") || inputPayload.getString("PGCategoryID").trim().length() == 0){
						if(errorMsg.trim().length()> 0)
							errorMsg = errorMsg + ", PGCategoryID";
						else
							errorMsg = "PGCategoryID";
					}
					else
						pgCatID = inputPayload.getString("PGCategoryID");
					
					if(debug){
						response.getWriter().println("validatePGCategoriesInsert.pgCatID.i ("+i+"): "+pgCatID);
						response.getWriter().println("validatePGCategoriesInsert.errorMsg.i ("+i+"): "+errorMsg);
						response.getWriter().println("validatePGCategoriesInsert.isSuccess: "+isSuccess);
					}
					if (errorMsg.trim().length() == 0) {	
						JsonObject childConfigTypesetsJson = new JsonObject();
						for (int j = 0; j < pgPymntCatJsonArray.size(); j++) {
							
							childConfigTypesetsJson = pgPymntCatJsonArray.get(j).getAsJsonObject();
							if (pgCatID.equalsIgnoreCase(childConfigTypesetsJson.get("Types").getAsString())){
								isSuccess = true;
							}
						}
						if (! isSuccess) {
							errorCOde = "E005";
							errorResponse.addProperty("ErrorCode", errorCOde);
							errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
							break;
						}
					} else {
						errorCOde = "E001";
						errorResponse.addProperty("ErrorCode", errorCOde);
						errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
						break;
					}
				}
			} else {
				errorCOde = "E005";
				errorResponse.addProperty("ErrorCode", errorCOde);
				errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
//			isFieldMissing = true;
		}
		return errorResponse;
	}
	
	
	
	//added by kamlesh 29052020
	public JsonObject deletePGPaymentCategories(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",inputAGGRID="", pgCatID = "", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject errorResonse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		try {
			
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");

			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("AggregatorID"))
				inputAGGRID = getGuidResponse.get("AggregatorID").getAsString();
			
			if (getGuidResponse.has("PGCategoryID"))
				pgCatID = getGuidResponse.get("PGCategoryID").getAsString();
				
			if(debug){
				response.getWriter().println("deletePGPaymentCategories.pathInfo: "+pathInfo);
				response.getWriter().println("deletePGPaymentCategories.testRun: "+testRun);
				response.getWriter().println("deletePGPaymentCategories.inputAGGRID: "+inputAGGRID);
				response.getWriter().println("deletePGPaymentCategories.pgCatID: "+pgCatID);
			}
			
			if (! inputAGGRID.equalsIgnoreCase("") && ! pgCatID.equalsIgnoreCase("")) {
				executeURL = oDataURL+"PGPaymentCategories?$filter=AggregatorID%20eq%20%27"+inputAGGRID+"%27%20and%20PGCategoryID%20eq%20%27"+pgCatID+"%27";
				if(debug)
					response.getWriter().println("deletePGPaymentCategories.executeURL: "+executeURL);
				
				JsonObject pgPymntCategoryJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deletePGPaymentCategories.pgPymntCategoryJson: "+pgPymntCategoryJson);
				if (pgPymntCategoryJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
//						executeURL = oDataURL+"PGPaymentCategories(AggregatorID='"+inputAGGRID+"',PGCategoryID='"+pgCatID+"')";
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deletePGPaymentCategories.executeURL: "+executeURL);
						
						JsonObject pgPymntHttpDelete = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deletePGPaymentCategories.pgPymntHttpDelete: "+pgPymntHttpDelete);
						
						if( pgPymntHttpDelete.has("error") || (! pgPymntHttpDelete.get("ErrorCode").isJsonNull()
								&& ! pgPymntHttpDelete.get("ErrorCode").getAsString().trim().equalsIgnoreCase(""))){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else{
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						}
					} else {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			 if ( isValidationFailed) {
				 errorResonse = new JsonObject();
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 errorResonse.addProperty("ErrorCode", errorCode);
				 errorResonse.addProperty("ResponseCode", responseCode);
				 if(errorCode.equalsIgnoreCase("E003"))
					 errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				 else
					 errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
				 response.getWriter().println(errorResonse);
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
//			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
			errorResonse = new JsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
			response.getWriter().println(errorResonse);
		}
		return errorResonse;
	}
	
	public JsonObject getDeleteURLGuid(HttpServletResponse response, String pathInfo, boolean debug) throws IOException
	{
		String paramName="", paramValue="", guid="";
		JsonObject guidJsonResponse = new JsonObject();
		
		try {
//			entityInfo = pathInfo.substring(0, pathInfo.indexOf("(")).replace("/", "");
			guid = pathInfo.substring(pathInfo.indexOf("(")+1, pathInfo.length()-1).replaceAll("'", "");
			if(debug)
				response.getWriter().println("getDeleteURLGuid.guid: "+guid);
			if (guid.contains(","))
			{
				String[] reqQuery = guid.split(",");
				for (String string : reqQuery) {
					paramName = string.substring(0, string.indexOf("="));
					paramValue = string.substring(string.indexOf("=")+1,string.length());
					guidJsonResponse.addProperty(paramName, paramValue);
				}
			} else
			{
				guidJsonResponse.addProperty("GUID", guid);
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
		}
		return guidJsonResponse;
	}
	
	//added by kamlesh 29052020
	public JsonObject updatePGPaymentCategories(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",pgCategoryID="",errorMsg="";
		JsonObject pgPymntCategoryUpdateResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject pgPymntCategoryJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{

			 if (inputPayload.has("PGPaymentCategories")) {
				 jsonArrayPayload = inputPayload.getJSONArray("PGPaymentCategories");
			 } else {
				 jsonArrayPayload.put(inputPayload);
			 }
			
			 if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				 testRun = inputPayload.getString("TestRun");
			 
			 JsonObject validatePGPaymentUpdateJson = validatePGCategoriesInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			 if(debug)
					response.getWriter().println("updatePGPaymentCategories.validatePGPaymentUpdateJson: "+validatePGPaymentUpdateJson);
				
			 if (validatePGPaymentUpdateJson.has("ErrorCode") && validatePGPaymentUpdateJson.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				 for (int i = 0; i < jsonArrayPayload.length(); i++) {
					 
					 executeURL =""; requestAggrID=""; pgCategoryID="";errorMsg="";
					 updatePayLoad = new JSONObject();
					 pgPymntCategoryUpdateResponse = new JsonObject();
					 pgPymntCategoryJson = new JsonObject();
					 
					 childObjPayload = jsonArrayPayload.getJSONObject(i);
					 if(debug)
						 response.getWriter().println("updatePGPaymentCategories.childObjPayload ("+i+"): "+childObjPayload);
					 
					 pgCategoryID = childObjPayload.getString("PGCategoryID");
					 requestAggrID = childObjPayload.getString("AggregatorID");
					 
					 executeURL = oDataUrl+"PGPaymentCategories?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20PGCategoryID%20eq%20%27"+pgCategoryID+"%27";
					 executeURL = executeURL.replace(" ", "%20");
					 if(debug)
						 response.getWriter().println("updatePGPaymentCategories.executeURL: "+executeURL);
					 
					 pgPymntCategoryJson = commonUtils.executeURL(executeURL, userPass, response);
					 if(debug)
						 response.getWriter().println("updatePGPaymentCategories.pgPymntCategoryJson: "+pgPymntCategoryJson);
					 
					 JsonArray pgPymntCategoryArrayJson = pgPymntCategoryJson.getAsJsonObject("d").getAsJsonArray("results");
//					 if(debug)
//							 response.getWriter().println("updatePGPaymentCategories.pgPymntCategoryArrayJson: "+pgPymntCategoryArrayJson);
					 
					 if (pgPymntCategoryArrayJson.size() > 0) {
						 
						 if (testRun.equalsIgnoreCase("")) {
							 executeURL = "";
//							 	 JsonObject childPGPymntCatJson = pgPymntCategoryArrayJson.get(0).getAsJsonObject();
							 
							 updatePayLoad.accumulate("AggregatorID", requestAggrID);
							 updatePayLoad.accumulate("PGCategoryID", pgCategoryID);
							 
							 if(childObjPayload.isNull("CheckFinanceBlock"))
							 	updatePayLoad.accumulate("CheckFinanceBlock", "");
							 else
								 updatePayLoad.accumulate("CheckFinanceBlock", childObjPayload.getString("CheckFinanceBlock"));
							 
							 if(childObjPayload.isNull("BDCPostingEnabled"))
								 updatePayLoad.accumulate("BDCPostingEnabled", "");
							 else
								 updatePayLoad.accumulate("BDCPostingEnabled", childObjPayload.getString("BDCPostingEnabled"));
							 
							 if( childObjPayload.isNull("BankPaymentTransactionType"))
								 updatePayLoad.accumulate("BankPaymentTransactionType", "");
							 else
								 updatePayLoad.accumulate("BankPaymentTransactionType", childObjPayload.getString("BankPaymentTransactionType"));
							 
							 if( childObjPayload.isNull("NumberRangeObject"))
								 updatePayLoad.accumulate("NumberRangeObject", "");
							 else
								 updatePayLoad.accumulate("NumberRangeObject", childObjPayload.getString("NumberRangeObject"));
							 
							 if( childObjPayload.isNull("NumberRangeSubObject"))
								 updatePayLoad.accumulate("NumberRangeSubObject", "");
							 else
								 updatePayLoad.accumulate("NumberRangeSubObject", childObjPayload.getString("NumberRangeSubObject"));
							 
							 if( childObjPayload.isNull("IsNRFiscalYearDependent"))
								 updatePayLoad.accumulate("IsNRFiscalYearDependent", "");
							 else
								 updatePayLoad.accumulate("IsNRFiscalYearDependent", childObjPayload.getString("IsNRFiscalYearDependent"));
							 
							 if( childObjPayload.isNull("TrackIDPrefix"))
								 updatePayLoad.accumulate("TrackIDPrefix", "");
							 else
								 updatePayLoad.accumulate("TrackIDPrefix", childObjPayload.getString("TrackIDPrefix"));
							 
							 if( childObjPayload.isNull("PaymProcessingSequence"))
								 updatePayLoad.accumulate("PaymProcessingSequence", "");
							 else
								 updatePayLoad.accumulate("PaymProcessingSequence", childObjPayload.getString("PaymProcessingSequence"));
							 
							 if( childObjPayload.isNull("ERPPostIndforPaymPending"))
								 updatePayLoad.accumulate("ERPPostIndforPaymPending", "");
							 else
								 updatePayLoad.accumulate("ERPPostIndforPaymPending", childObjPayload.getString("ERPPostIndforPaymPending"));
							 
							 executeURL = oDataUrl+"PGPaymentCategories(AggregatorID='"+requestAggrID+"',PGCategoryID='"+pgCategoryID+"')";
							 executeURL = executeURL.replace(" ", "%20");
							 if(debug)
								 response.getWriter().println("updatePGPaymentCategories.executeURL.2: "+executeURL);
							 
							 pgPymntCategoryUpdateResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							 if(debug)
								 response.getWriter().println("updatePGPaymentCategories.pgPymntCategoryUpdateResponse: "+pgPymntCategoryUpdateResponse);
							 
							 if (pgPymntCategoryUpdateResponse.has("error") || pgPymntCategoryUpdateResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								 responseCode ="000004";
								 errorCode ="E003";
								 isValidationFailed = true;
								 break;
							 }else{
								 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							 }
						 } else {
							 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							 break;
						 }
					 } else {
						 responseCode ="000002";
						 errorCode ="E004";
						 isValidationFailed = true;
						 break;
					 }
				}
			 } else {
				 isValidationFailed = true;
				 responseCode ="000002";
				 errorCode = validatePGPaymentUpdateJson.get("ErrorCode").getAsString();
			 }
			 if ( isValidationFailed) {
				 pgPymntCategoryUpdateResponse = new JsonObject();
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 pgPymntCategoryUpdateResponse.addProperty("ErrorCode", errorCode);
				 pgPymntCategoryUpdateResponse.addProperty("ResponseCode", responseCode);
				 if(errorCode.equalsIgnoreCase("E003"))
					 errorMsg = properties.getProperty(errorCode).replace("&", "update");
				 else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					 errorMsg = validatePGPaymentUpdateJson.get("ErrorMsg").getAsString();
				 else
					 errorMsg = properties.getProperty(errorCode);
				 
				 pgPymntCategoryUpdateResponse.addProperty("Message", errorMsg);
				 response.getWriter().println(pgPymntCategoryUpdateResponse);
					 
			}
			 else
				 response.getWriter().println(pgPymntCategoryUpdateResponse);
			 
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
//			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
			pgPymntCategoryUpdateResponse = new JsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pgPymntCategoryUpdateResponse.addProperty("ErrorCode", "E000");
			pgPymntCategoryUpdateResponse.addProperty("ResponseCode", "000003");
			pgPymntCategoryUpdateResponse.addProperty("Message", buffer.toString());
			response.getWriter().println(pgPymntCategoryUpdateResponse);
		}
		return pgPymntCategoryUpdateResponse;	
	}
	
	public JsonObject getConfigTypesetByTypes(HttpServletResponse response, String oDataUrl,  String userPass, String aggregatorID, String typesetsTypes, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String executeURL="";
		JsonObject configTypesetTypesJson = new JsonObject();
		try {
			
			if (debug) {
				response.getWriter().println("getConfigTypesetByTypes.oDataUrl: "+oDataUrl);
				response.getWriter().println("getConfigTypesetByTypes.userPass: "+userPass);
				response.getWriter().println("getConfigTypesetByTypes.aggregatorID: "+aggregatorID);
			}
			executeURL = oDataUrl+"ConfigTypesetTypes?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20Typeset%20eq%20%27"+typesetsTypes+"%27";
			if(debug)
				response.getWriter().println("getConfigTypesetByTypes.executeURL "+executeURL);
			
			configTypesetTypesJson = commonUtils.executeURL(executeURL, userPass, response);
			if(debug)
				response.getWriter().println("getConfigTypesetByTypes.configTypesetTypesJson: "+configTypesetTypesJson);
			
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
		}
		return configTypesetTypesJson;
	}

	public JsonObject insertPGPaymentConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",pgCategoryID="",createdBy="", createdAt="", guid="", pgID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis =0;
		
		JsonArray insertPGConfigsArray = new JsonArray();
		JsonObject insertPGConfigJSon = new JsonObject();
		JsonObject insertPGConfigsMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();

		JsonObject pgPymntConfigsPOSTResponse = null;
		JsonObject pgPymntConfigsJson =null;
		successResonse ="{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.PGPaymentConfigsType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/PGPaymentConfigs('')\"},\"ConfigHeaderGUID\":\"\",\"AggregatorID\":\"\","
				+ "\"PGID\":\"\",\"PGCategoryID\":\"\",\"PGName\":\"\",\"MerchantCode\":null,\"SchemeCode\":null,\"PGPublicKey\":null,\"PGOwnPublickey\":null,"
				+ "\"PGOwnPrivatekey\":null,\"ClientCode\":\"ART\",\"BankKey\":\"\",\"PGProvider\":\"\",\"PGParameter1\":null,\"PGParameter2\":null,\"PGParameter3\":null,"
				+ "\"PGParameter4\":null,\"PGParameter5\":null,\"PGURL\":null,\"PymntFor\":null,\"UserRegURL\":null,\"AccStmtURL\":null,\"AccBalURL\":null,\"TopUpURL\":null,"
				+ "\"TxnStsURL\":null,\"CreatedBy\":null,\"CreatedAt\":null,\"CreatedOn\":null,\"ChangedBy\":null,\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			 if (inputPayload.has("PGPaymentConfigs")) {
				 jsonArrayPayload = inputPayload.getJSONArray("PGPaymentConfigs");
			 } else {
				 jsonArrayPayload.put(inputPayload);
			 }
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validatePGConfigsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertPGPaymentConfigs.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertPGPaymentConfigs - createdBy: "+createdBy);	
					response.getWriter().println("insertPGPaymentConfigs - createdAt: "+createdAt);	
					response.getWriter().println("insertPGPaymentConfigs - createdOnInMillis: "+createdOnInMillis);	
				}
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					pgPymntConfigsPOSTResponse = new JsonObject();
					pgPymntConfigsJson = new JsonObject();
					executeURL =""; requestAggrID=""; pgCategoryID="";guid="";
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigs.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					guid = childObjPayload.getString("ConfigHeaderGUID");
					
					executeURL = oDataUrl+"PGPaymentConfigs?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ConfigHeaderGUID%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertPGPaymentConfigs.executeURL: "+executeURL);
					
					pgPymntConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigs.pgPymntConfigsJson: "+pgPymntConfigsJson);
					
					JsonArray pgPymntCategoryArrayJson = pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results");
					
					if (pgPymntCategoryArrayJson.size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"PGPaymentConfigs";
							
							guid = commonUtils.generateGUID(36);
							pgCategoryID = childObjPayload.getString("PGCategoryID");

							insertPayload.accumulate("AggregatorID", requestAggrID);
							insertPayload.accumulate("PGCategoryID", pgCategoryID);
							insertPayload.accumulate("ConfigHeaderGUID",guid);
							 
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("PGName") && ! childObjPayload.isNull("PGName"))
								insertPayload.accumulate("PGName", childObjPayload.getString("PGName"));
							else
								insertPayload.accumulate("PGName", "");
							
							if(childObjPayload.has("PGID") && ! childObjPayload.isNull("PGID"))
								insertPayload.accumulate("PGID", childObjPayload.getString("PGID"));
							else
								insertPayload.accumulate("PGID", "");
							
							if(childObjPayload.has("MerchantCode") && ! childObjPayload.isNull("MerchantCode"))
								insertPayload.accumulate("MerchantCode", childObjPayload.getString("MerchantCode"));
							else
								insertPayload.accumulate("MerchantCode", "");
							
							if(childObjPayload.has("SchemeCode") && !childObjPayload.isNull("SchemeCode"))
								insertPayload.accumulate("SchemeCode", childObjPayload.getString("SchemeCode"));
							else
								insertPayload.accumulate("SchemeCode", "");
							
							if(childObjPayload.has("PGPublicKey") && !childObjPayload.isNull("PGPublicKey"))
								insertPayload.accumulate("PGPublicKey", childObjPayload.getString("PGPublicKey"));
							else
								insertPayload.accumulate("PGPublicKey", "");
							
							if(childObjPayload.has("PGOwnPublickey") && !childObjPayload.isNull("PGOwnPublickey"))
								insertPayload.accumulate("PGOwnPublickey", childObjPayload.getString("PGOwnPublickey"));
							else
								insertPayload.accumulate("PGOwnPublickey", "");
							
							if(childObjPayload.has("PGOwnPrivatekey") && !childObjPayload.isNull("PGOwnPrivatekey"))
								insertPayload.accumulate("PGOwnPrivatekey", childObjPayload.getString("PGOwnPrivatekey"));
							else
								insertPayload.accumulate("PGOwnPrivatekey", "");
							
							if(childObjPayload.has("ClientCode") && !childObjPayload.isNull("ClientCode"))
								insertPayload.accumulate("ClientCode", childObjPayload.getString("ClientCode"));
							else
								insertPayload.accumulate("ClientCode", "");
							
							if(childObjPayload.has("BankKey") && !childObjPayload.isNull("BankKey"))
								insertPayload.accumulate("BankKey", childObjPayload.getString("BankKey"));
							else
								insertPayload.accumulate("BankKey", "");
							
							if(childObjPayload.has("PGProvider") && !childObjPayload.isNull("PGProvider"))
								insertPayload.accumulate("PGProvider", childObjPayload.getString("PGProvider"));
							else
								insertPayload.accumulate("PGProvider", "");
							
							if(childObjPayload.has("PGParameter1") && !childObjPayload.isNull("PGParameter1"))
								insertPayload.accumulate("PGParameter1", childObjPayload.getString("PGParameter1"));
							else
								insertPayload.accumulate("PGParameter1", "");
							
							if(childObjPayload.has("PGParameter2") && !childObjPayload.isNull("PGParameter2"))
								insertPayload.accumulate("PGParameter2", childObjPayload.getString("PGParameter2"));
							else
								insertPayload.accumulate("PGParameter2", "");
							
							if(childObjPayload.has("PGParameter3") && !childObjPayload.isNull("PGParameter3"))
								insertPayload.accumulate("PGParameter3", childObjPayload.getString("PGParameter3"));
							else
								insertPayload.accumulate("PGParameter3", "");
							
							if(childObjPayload.has("PGParameter4") && !childObjPayload.isNull("PGParameter4"))
								insertPayload.accumulate("PGParameter4", childObjPayload.getString("PGParameter4"));
							else
								insertPayload.accumulate("PGParameter4", "");
							
							if(childObjPayload.has("PGParameter5") && ! childObjPayload.isNull("PGParameter5"))
								insertPayload.accumulate("PGParameter5", childObjPayload.getString("PGParameter5"));
							else
								insertPayload.accumulate("PGParameter5", "");
							
							if(childObjPayload.has("PGURL") && !childObjPayload.isNull("PGURL"))
								insertPayload.accumulate("PGURL", childObjPayload.getString("PGURL"));
							else
								insertPayload.accumulate("PGURL", "");
							
							if(childObjPayload.has("PymntFor") && !childObjPayload.isNull("PymntFor"))
								insertPayload.accumulate("PymntFor", childObjPayload.getString("PymntFor"));
							else
								insertPayload.accumulate("PymntFor", "");
							
							if(childObjPayload.has("UserRegURL") && !childObjPayload.isNull("UserRegURL"))
								insertPayload.accumulate("UserRegURL", childObjPayload.getString("UserRegURL"));
							else
								insertPayload.accumulate("UserRegURL", "");
							
							if(childObjPayload.has("AccStmtURL") && !childObjPayload.isNull("AccStmtURL"))
								insertPayload.accumulate("AccStmtURL", childObjPayload.getString("AccStmtURL"));
							else
								insertPayload.accumulate("AccStmtURL", "");
							
							if(childObjPayload.has("AccBalURL") && !childObjPayload.isNull("AccBalURL"))
								insertPayload.accumulate("AccBalURL", childObjPayload.getString("AccBalURL"));
							else
								insertPayload.accumulate("AccBalURL", "");
							
							if(childObjPayload.has("TopUpURL") && !childObjPayload.isNull("TopUpURL"))
								insertPayload.accumulate("TopUpURL", childObjPayload.getString("TopUpURL"));
							else
								insertPayload.accumulate("TopUpURL", "");
							
							if(childObjPayload.has("TxnStsURL") && !childObjPayload.isNull("TxnStsURL"))
								insertPayload.accumulate("TxnStsURL", childObjPayload.getString("TxnStsURL")); 
							else
								insertPayload.accumulate("TxnStsURL", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							pgPymntConfigsPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertPGPaymentConfigs.pgPymntConfigsPOSTResponse: "+pgPymntConfigsPOSTResponse);
							
							if (pgPymntConfigsPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertPGConfigsArray.add(pgPymntConfigsPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertPGConfigsMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertPGConfigsMasterJson.addProperty("ErrorCode", errorCode);
				insertPGConfigsMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertPGConfigsMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					insertPGConfigsMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertPGConfigsMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if (! insertPGConfigsMasterJson.has("d")){
					insertPGConfigJSon.add("results", insertPGConfigsArray);
					insertPGConfigsMasterJson.add("d", insertPGConfigJSon);
				}
				insertPGConfigsMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertPGConfigsMasterJson = new JsonObject();
			insertPGConfigsMasterJson.addProperty("ErrorCode", "E000");
			insertPGConfigsMasterJson.addProperty("ResponseCode", "000003");
			insertPGConfigsMasterJson.addProperty("Message", buffer.toString());
		}
		return insertPGConfigsMasterJson;
	}
	
	//added by kamlesh for request fields validation 30-05-2020
	public JsonObject validatePGConfigsInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String pgCatID="", errorMsg="", errorCOde="", pgID="", executeURL="", requestAggrID="";
		boolean isSuccess = false;
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {
			
			JsonObject getConfigTypesJson = getConfigTypesetByTypes(response, oDataUrl, userPass, aggregatorID, "PGCAT", debug);
			if(debug)
				response.getWriter().println("validatePGConfigsInsert.getConfigTypesJson: "+getConfigTypesJson);
			
			JsonArray pgPymntConfigJsonArray= getConfigTypesJson.getAsJsonObject("d").getAsJsonArray("results");
			if(debug)
				response.getWriter().println("validatePGConfigsInsert.pgPymntConfigJsonArray: "+pgPymntConfigJsonArray);
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			if(pgPymntConfigJsonArray.size() > 0)
			{
				for (int i = 0; i < inputArrayPayload.length(); i++) {
					
					isSuccess = false;
					errorMsg="";pgCatID="";requestAggrID="";
					inputPayload = inputArrayPayload.getJSONObject(i);
					
					if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
						errorMsg = "AggregatorID";
					
					if (inputPayload.isNull("PGCategoryID") || inputPayload.getString("PGCategoryID").trim().length() == 0){
						if(errorMsg.trim().length()> 0)
							errorMsg = errorMsg + ", PGCategoryID";
						else
							errorMsg = "PGCategoryID";
					}
					else
						pgCatID = inputPayload.getString("PGCategoryID");
					
					if ( ! inputPayload.isNull("PGID") || inputPayload.getString("PGID").trim().length() > 0)
						pgID = inputPayload.getString("PGID");
					
//					if (inputPayload.isNull("ConfigHeaderGUID") || inputPayload.getString("ConfigHeaderGUID").trim().length() == 0){
//						if(errorMsg.trim().length()> 0)
//							errorMsg = errorMsg + ", ConfigHeaderGUID";
//						else
//							errorMsg = "ConfigHeaderGUID";
//					}
					
					if(debug){
						response.getWriter().println("validatePGConfigsInsert.pgCatID.i ("+i+"): "+pgCatID);
						response.getWriter().println("validatePGConfigsInsert.errorMsg.i ("+i+"): "+errorMsg);
					}
					if (errorMsg.trim().length() == 0) {	
						JsonObject childConfigTypesetsJson = new JsonObject();
						for (int j = 0; j < pgPymntConfigJsonArray.size(); j++) {
							
							childConfigTypesetsJson = pgPymntConfigJsonArray.get(j).getAsJsonObject();
							if (pgCatID.equalsIgnoreCase(childConfigTypesetsJson.get("Types").getAsString())){
								isSuccess = true;
							}
						}
						if (! isSuccess) {
							errorCOde = "E005";
							errorResponse.addProperty("ErrorCode", errorCOde);
							errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
							break;
						}
					} else {
						errorCOde = "E001";
						errorResponse.addProperty("ErrorCode", errorCOde);
						errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
						break;
					}
				}
			}
			else{
				errorCOde = "E005";
				errorResponse.addProperty("ErrorCode", errorCOde);
				errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}
	
	//added by kamlesh 29052020
	public JsonObject deletePGPaymentConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",guid="", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject errorResonse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		try {
			
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("GUID"))
				guid = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deletePGPaymentConfigs.pathInfo: "+pathInfo);
				response.getWriter().println("deletePGPaymentConfigs.guid: "+guid);
			}
			
			if (! guid.equalsIgnoreCase("")) {
				executeURL = oDataURL+"PGPaymentConfigs?$filter=ConfigHeaderGUID%20eq%20%27"+guid+"%27";
				if(debug)
					response.getWriter().println("deletePGPaymentConfigs.executeURL: "+executeURL);
				
				JsonObject pgPymntConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deletePGPaymentConfigs.pgPymntConfigsJson: "+pgPymntConfigsJson);
				if (pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
//						executeURL = oDataURL+"PGPaymentCategories(AggregatorID='"+inputAGGRID+"',PGCategoryID='"+pgCatID+"')";
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deletePGPaymentConfigs.executeURL: "+executeURL);
						
						JsonObject pgPymntHttpDelete = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deletePGPaymentConfigs.pgPymntHttpDelete: "+pgPymntHttpDelete);
						
						if(! pgPymntHttpDelete.get("ErrorCode").isJsonNull() 
								&& ! pgPymntHttpDelete.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else{
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						}
					} else {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed) {
				errorResonse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
				response.getWriter().println(errorResonse);
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
//				response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
			errorResonse = new JsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
			response.getWriter().println(errorResonse);
		}
		return errorResonse;
	}
	//added by kamlesh 29052020
	public JsonObject updatePGPaymentConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",pgCategoryID="", guid="",changedBy="",changedAt="", pgID="";
		JsonObject pgPymntConfigsUpdateResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		long changedOnInMillis=0;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject pgPymntConfigsJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{

			 if (inputPayload.has("PGPaymentConfigs")) {
				 jsonArrayPayload = inputPayload.getJSONArray("PGPaymentConfigs");
			 } else {
				 jsonArrayPayload.put(inputPayload);
			 }
			
			 if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				 testRun = inputPayload.getString("TestRun");
			 
			 JsonObject validateErrorJsonResponse = validatePGConfigsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			 if(debug)
				 response.getWriter().println("updatePGPaymentConfigs.validateErrorJsonResponse: "+validateErrorJsonResponse);
			 
			 if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
			 
				 changedBy = commonUtils.getUserPrincipal(request, "name", response);
				 changedOnInMillis = commonUtils.getCreatedOnDate();
				 changedAt = commonUtils.getCreatedAtTime();
				 
				 for (int i = 0; i < jsonArrayPayload.length(); i++) {
					 
					 executeURL =""; requestAggrID=""; pgCategoryID="";guid=""; pgID="";
					 updatePayLoad = new JSONObject();
					 pgPymntConfigsUpdateResponse = new JsonObject();
					 pgPymntConfigsJson = new JsonObject();
					 
					 childObjPayload = jsonArrayPayload.getJSONObject(i);
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigs.childObjPayload ("+i+"): "+childObjPayload);
					 
					 guid = childObjPayload.getString("ConfigHeaderGUID");
					 requestAggrID = childObjPayload.getString("AggregatorID");
					 pgCategoryID = childObjPayload.getString("PGCategoryID");
					 if(debug){
						 response.getWriter().println("updatePGPaymentConfigs.guid: "+guid);
						 response.getWriter().println("updatePGPaymentConfigs.requestAggrID: "+requestAggrID);
						 response.getWriter().println("updatePGPaymentConfigs.pgCategoryID: "+pgCategoryID);
					 }

					 executeURL = oDataUrl+"PGPaymentConfigs?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ConfigHeaderGUID%20eq%20%27"+guid+"%27";
					 executeURL = executeURL.replace(" ", "%20");
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigs.executeURL: "+executeURL);
					 
					 pgPymntConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigs.pgPymntConfigsJson: "+pgPymntConfigsJson);
					 
					 if (pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						 
						 if (testRun.equalsIgnoreCase("")) {
							 executeURL = "";

							 JsonObject childJson = pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
							 
							 updatePayLoad.accumulate("ChangedBy", changedBy);
							 updatePayLoad.accumulate("ChangedAt", changedAt);
							 updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
							 
							 updatePayLoad.accumulate("AggregatorID", requestAggrID);
							 updatePayLoad.accumulate("PGCategoryID", pgCategoryID);
							 updatePayLoad.accumulate("ConfigHeaderGUID",guid);
							 
							 if(! childObjPayload.isNull("PGName"))
								 updatePayLoad.accumulate("PGName", childObjPayload.getString("PGName"));
							 else
								 updatePayLoad.accumulate("PGName", "");
							 
							 if(childObjPayload.has("PGID") && ! childObjPayload.isNull("PGID"))
								 updatePayLoad.accumulate("PGID", childObjPayload.getString("PGID"));
							 else
								 updatePayLoad.accumulate("PGID", "");
							 
							 if(childObjPayload.has("MerchantCode") && ! childObjPayload.isNull("MerchantCode"))
								 updatePayLoad.accumulate("MerchantCode", childObjPayload.getString("MerchantCode"));
							 else
								 updatePayLoad.accumulate("MerchantCode", "");
							 
							 if(childObjPayload.has("SchemeCode") && !childObjPayload.isNull("SchemeCode"))
								 updatePayLoad.accumulate("SchemeCode", childObjPayload.getString("SchemeCode"));
							 else
								 updatePayLoad.accumulate("SchemeCode", "");
							 
							 if( !childObjPayload.isNull("PGPublicKey"))
								 updatePayLoad.accumulate("PGPublicKey", childObjPayload.getString("PGPublicKey"));
							 else
								 updatePayLoad.accumulate("PGPublicKey", "");
							 
							 if( !childObjPayload.isNull("PGOwnPublickey"))
								 updatePayLoad.accumulate("PGOwnPublickey", childObjPayload.getString("PGOwnPublickey"));
							 else
								 updatePayLoad.accumulate("PGOwnPublickey", "");
							 
							 if( !childObjPayload.isNull("PGOwnPrivatekey"))
								 updatePayLoad.accumulate("PGOwnPrivatekey", childObjPayload.getString("PGOwnPrivatekey"));
							 else
								 updatePayLoad.accumulate("PGOwnPrivatekey", "");
							 
							 if( !childObjPayload.isNull("ClientCode"))
								 updatePayLoad.accumulate("ClientCode", childObjPayload.getString("ClientCode"));
							 else
								 updatePayLoad.accumulate("ClientCode", "");
							 
							 if(!childObjPayload.isNull("BankKey"))
								 updatePayLoad.accumulate("BankKey", childObjPayload.getString("BankKey"));
							 else
								 updatePayLoad.accumulate("BankKey", "");
							 
							 if(!childObjPayload.isNull("PGProvider"))
								 updatePayLoad.accumulate("PGProvider", childObjPayload.getString("PGProvider"));
							 else
								 updatePayLoad.accumulate("PGProvider", "");
							 
							 if(!childObjPayload.isNull("PGParameter1"))
								 updatePayLoad.accumulate("PGParameter1", childObjPayload.getString("PGParameter1"));
							 else
								 updatePayLoad.accumulate("PGParameter1", "");
							 
							 if( !childObjPayload.isNull("PGParameter2"))
								 updatePayLoad.accumulate("PGParameter2", childObjPayload.getString("PGParameter2"));
							 else
								 updatePayLoad.accumulate("PGParameter2", "");
							 
							 if(!childObjPayload.isNull("PGParameter3"))
								 updatePayLoad.accumulate("PGParameter3", childObjPayload.getString("PGParameter3"));
							 else
								 updatePayLoad.accumulate("PGParameter3", "");
							 
							 if( !childObjPayload.isNull("PGParameter4"))
								 updatePayLoad.accumulate("PGParameter4", childObjPayload.getString("PGParameter4"));
							 else
								 updatePayLoad.accumulate("PGParameter4", "");
							 
							 if(! childObjPayload.isNull("PGParameter5"))
								 updatePayLoad.accumulate("PGParameter5", childObjPayload.getString("PGParameter5"));
							 else
								 updatePayLoad.accumulate("PGParameter5", "");
							 
							 if(!childObjPayload.isNull("PGURL"))
								 updatePayLoad.accumulate("PGURL", childObjPayload.getString("PGURL"));
							 else
								 updatePayLoad.accumulate("PGURL", "");
							 
							 if(!childObjPayload.isNull("PymntFor"))
								 updatePayLoad.accumulate("PymntFor", childObjPayload.getString("PymntFor"));
							 else
								 updatePayLoad.accumulate("PymntFor", "");
							 
							 if(!childObjPayload.isNull("UserRegURL"))
								 updatePayLoad.accumulate("UserRegURL", childObjPayload.getString("UserRegURL"));
							 else
								 updatePayLoad.accumulate("UserRegURL", "");
							 
							 if(!childObjPayload.isNull("AccStmtURL"))
								 updatePayLoad.accumulate("AccStmtURL", childObjPayload.getString("AccStmtURL"));
							 else
								 updatePayLoad.accumulate("AccStmtURL", "");
							 
							 if(!childObjPayload.isNull("AccBalURL"))
								 updatePayLoad.accumulate("AccBalURL", childObjPayload.getString("AccBalURL"));
							 else
								 updatePayLoad.accumulate("AccBalURL", "");
							 
							 if(! childObjPayload.isNull("TopUpURL"))
								 updatePayLoad.accumulate("TopUpURL", childObjPayload.getString("TopUpURL"));
							 else
								 updatePayLoad.accumulate("TopUpURL", "");
							 
							 if(!childObjPayload.isNull("TxnStsURL"))
								 updatePayLoad.accumulate("TxnStsURL", childObjPayload.getString("TxnStsURL")); 
							 else
								 updatePayLoad.accumulate("TxnStsURL", "");
							 
							 if(!childObjPayload.isNull("Source"))
								 updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));	
							 else
								 updatePayLoad.accumulate("Source", "");
							 
							 if( !childObjPayload.isNull("SourceReferenceID"))
								 updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							 else
								 updatePayLoad.accumulate("SourceReferenceID", "");
							 
							 if ( ! childJson.get("CreatedBy").isJsonNull() )
								 updatePayLoad.accumulate("CreatedBy", childJson.get("CreatedBy").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedBy", "");
							 
							 if ( ! childJson.get("CreatedAt").isJsonNull() )
								 updatePayLoad.accumulate("CreatedAt", childJson.get("CreatedAt").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							 
							 if (  ! childJson.get("CreatedOn").isJsonNull() )
								 updatePayLoad.accumulate("CreatedOn", childJson.get("CreatedOn").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							 
							 executeURL = oDataUrl+"PGPaymentConfigs('"+guid+"')";
							 if(debug)
								 response.getWriter().println("updatePGPaymentConfigs.executeURL: "+executeURL);
							 
							 pgPymntConfigsUpdateResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							 if(debug)
								 response.getWriter().println("updatePGPaymentConfigs.pgPymntConfigsUpdateResponse: "+pgPymntConfigsUpdateResponse);
							 
							 if (pgPymntConfigsUpdateResponse.has("error")|| pgPymntConfigsUpdateResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								 responseCode ="000004";
								 errorCode ="E003";
								 isValidationFailed = true;
								 break;
							 }else{
								 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							 }
						 } else {
							 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							 break;
						 }
					 } else {
						 responseCode ="000002";
						 errorCode ="E004";
						 isValidationFailed = true;
						 break;
					 }
				}
			 }
			 else{
				 responseCode ="000002";
				 errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				 isValidationFailed = true;
			 }
			 if ( isValidationFailed) {
				 pgPymntConfigsUpdateResponse = new JsonObject();
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 pgPymntConfigsUpdateResponse.addProperty("ErrorCode", errorCode);
				 pgPymntConfigsUpdateResponse.addProperty("ResponseCode", responseCode);
				 if(errorCode.equalsIgnoreCase("E003"))
					 pgPymntConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				 else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					 pgPymntConfigsUpdateResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				 else
					 pgPymntConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode));
				 
				 response.getWriter().println(pgPymntConfigsUpdateResponse);
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
//			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
			pgPymntConfigsUpdateResponse = new JsonObject();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pgPymntConfigsUpdateResponse.addProperty("ErrorCode", "E000");
			pgPymntConfigsUpdateResponse.addProperty("ResponseCode", "000003");
			pgPymntConfigsUpdateResponse.addProperty("Message", buffer.toString());
			response.getWriter().println(pgPymntConfigsUpdateResponse);
		}
		return pgPymntConfigsUpdateResponse;	
	}
	
	//TODO: added by kamlesh on 02-06-2020 for PGPaymentConfigStats
	public JsonObject insertPGPaymentConfigStats(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",guid="",createdBy="", createdAt="", configGuid="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis =0;
		
		JsonArray insertPGConfigStatsArray = new JsonArray();
		JsonObject insertPGPymntStsJson = new JsonObject();
		JsonObject insertPGConfigStatsMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject pgPymntConfigsPOSTResponse = null;
		JsonObject pgPymntConfigsJson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.PGPaymentConfigStatsType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/PGPaymentConfigStats('')\"},\"CSGuid\":\"\","
				+ "\"ConfigHeaderGUID\":\"\",\"AggregatorID\":\"\",\"PGTxnStatus\":\"\",\"PymntStatus\":\"\",\"CreatedBy\":null,\"CreatedAt\":null,"
				+ "\"CreatedOn\":null,\"ChangedBy\":null,\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("PGPaymentConfigStats")) {
				jsonArrayPayload = inputPayload.getJSONArray("PGPaymentConfigStats");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validatePGConfigStatsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertPGPaymentConfigStats.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertPGPaymentConfigStats - createdBy: "+createdBy);	
					response.getWriter().println("insertPGPaymentConfigStats - createdAt: "+createdAt);	
					response.getWriter().println("insertPGPaymentConfigStats - createdOnInMillis: "+createdOnInMillis);	
				}
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					pgPymntConfigsPOSTResponse = new JsonObject();
					pgPymntConfigsJson = new JsonObject();
					executeURL =""; requestAggrID=""; guid="";configGuid="";
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					configGuid = childObjPayload.getString("ConfigHeaderGUID");
					guid = childObjPayload.getString("CSGuid");
					
					executeURL = oDataUrl+"PGPaymentConfigStats?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20CSGuid%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.executeURL: "+executeURL);
					
					pgPymntConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.pgPymntConfigsJson: "+pgPymntConfigsJson);
					
					if (pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"PGPaymentConfigStats";
							
							guid = commonUtils.generateGUID(36);
							
							insertPayload.accumulate("AggregatorID", requestAggrID);
							insertPayload.accumulate("ConfigHeaderGUID", configGuid);
							insertPayload.accumulate("CSGuid",guid);
							
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("PGTxnStatus") && ! childObjPayload.isNull("PGTxnStatus"))
								insertPayload.accumulate("PGTxnStatus", childObjPayload.getString("PGTxnStatus"));
							else
								insertPayload.accumulate("PGTxnStatus", "");
							
							if(childObjPayload.has("PymntStatus") && ! childObjPayload.isNull("PymntStatus"))
								insertPayload.accumulate("PymntStatus", childObjPayload.getString("PymntStatus"));
							else
								insertPayload.accumulate("PymntStatus", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							pgPymntConfigsPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertPGPaymentConfigStats.pgPymntConfigsPOSTResponse: "+pgPymntConfigsPOSTResponse);
							
							if (pgPymntConfigsPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertPGConfigStatsArray.add(pgPymntConfigsPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertPGConfigStatsMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertPGConfigStatsMasterJson.addProperty("ErrorCode", errorCode);
				insertPGConfigStatsMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertPGConfigStatsMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertPGConfigStatsMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertPGConfigStatsMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if ( ! insertPGConfigStatsMasterJson.has("d")){
					insertPGPymntStsJson.add("results", insertPGConfigStatsArray);
					insertPGConfigStatsMasterJson.add("d", insertPGPymntStsJson);
				}
				insertPGConfigStatsMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertPGConfigStatsMasterJson = new JsonObject();
			insertPGConfigStatsMasterJson.addProperty("ErrorCode", "E000");
			insertPGConfigStatsMasterJson.addProperty("ResponseCode", "000003");
			insertPGConfigStatsMasterJson.addProperty("Message", buffer.toString());
		}
		return insertPGConfigStatsMasterJson;
	}
	
	//added by kamlesh for request fields validation 02-06-2020
	public JsonObject validatePGConfigStatsInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String pgCatID="", errorMsg="", errorCOde="", configGuid="", executeURL="";;
		boolean isSuccess = false;
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			
			executeURL = oDataUrl+"PGPaymentConfigs?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("validatePGConfigStatsInsert.executeURL: "+executeURL);
			
			JsonObject getConfigTypesJson = commonUtils.executeURL(executeURL, userPass, response);
			if(debug)
				response.getWriter().println("validatePGConfigStatsInsert.getConfigTypesJson: "+getConfigTypesJson);
			
			JsonArray pgPymntConfigJsonArray= getConfigTypesJson.getAsJsonObject("d").getAsJsonArray("results");
//			if(debug)
//				response.getWriter().println("validatePGConfigStatsInsert.pgPymntConfigJsonArray: "+pgPymntConfigJsonArray);
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			if(pgPymntConfigJsonArray.size() > 0)
			{
				for (int i = 0; i < inputArrayPayload.length(); i++) {
					
					isSuccess = false;
					errorMsg="";configGuid="";
					inputPayload = inputArrayPayload.getJSONObject(i);
					
					if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
						errorMsg = "AggregatorID";
					
					if (inputPayload.isNull("ConfigHeaderGUID") || inputPayload.getString("ConfigHeaderGUID").trim().length() == 0){
						if(errorMsg.trim().length()> 0)
							errorMsg = errorMsg + ", ConfigHeaderGUID";
						else
							errorMsg = "ConfigHeaderGUID";
					}
					else
						configGuid = inputPayload.getString("ConfigHeaderGUID");
					
					if(debug){
						response.getWriter().println("validatePGConfigStatsInsert.configGuid.i ("+i+"): "+configGuid);
						response.getWriter().println("validatePGConfigStatsInsert.errorMsg.i ("+i+"): "+errorMsg);
					}
					if (errorMsg.trim().length() == 0) {
						JsonObject childConfigGuidJson = new JsonObject();
						for (int j = 0; j < pgPymntConfigJsonArray.size(); j++) {
							
							childConfigGuidJson = pgPymntConfigJsonArray.get(j).getAsJsonObject();
							if (configGuid.equalsIgnoreCase(childConfigGuidJson.get("ConfigHeaderGUID").getAsString())){
								isSuccess = true;
							}
						}
						if (! isSuccess) {
							errorCOde = "E006";
							errorResponse.addProperty("ErrorCode", errorCOde);
							errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
							break;
						}
					} else {
						errorCOde = "E001";
						errorResponse.addProperty("ErrorCode", errorCOde);
						errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
						break;
					}
				}
			}
			else{
				errorCOde = "E006";
				errorResponse.addProperty("ErrorCode", errorCOde);
				errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}
	
	//added by kamlesh 02-06-2020
	public JsonObject deletePGPaymentConfigStats(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",guid="", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		
		JsonObject pgPymntHttpDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("GUID"))
				guid = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deletePGPaymentConfigStats.pathInfo: "+pathInfo);
				response.getWriter().println("deletePGPaymentConfigStats.guid: "+guid);
			}
			
			if (! guid.equalsIgnoreCase("")) {
				executeURL = oDataURL+"PGPaymentConfigStats?$filter=CSGuid%20eq%20%27"+guid+"%27";
				if(debug)
					response.getWriter().println("deletePGPaymentConfigStats.executeURL: "+executeURL);
				
				JsonObject pgPymntConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deletePGPaymentConfigStats.pgPymntConfigsJson: "+pgPymntConfigsJson);
				if (pgPymntConfigsJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deletePGPaymentConfigStats.executeURL: "+executeURL);
						
						pgPymntHttpDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deletePGPaymentConfigStats.pgPymntHttpDeleteResponse: "+pgPymntHttpDeleteResponse);
						
						if(! pgPymntHttpDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! pgPymntHttpDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode", pgPymntHttpDeleteResponse.get("ErrorCode").getAsString());
							errorResonse.addProperty("Message", pgPymntHttpDeleteResponse.get("ErrorMessage").getAsString());
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "");
//						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}
	
	public JsonObject updatePGPaymentConfigStats(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",configGuid="",changedBy="",changedAt="", guid="";
		JsonObject pgPymntConfigsUpdateResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		long changedOnInMillis=0;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject pgPymntConfigStatsJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{

			 if (inputPayload.has("PGPaymentConfigStats")) {
				 jsonArrayPayload = inputPayload.getJSONArray("PGPaymentConfigStats");
			 } else {
				 jsonArrayPayload.put(inputPayload);
			 }
			
			 if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				 testRun = inputPayload.getString("TestRun");
			 
			 JsonObject validateErrorJsonResponse = validatePGConfigStatsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			 if(debug)
				 response.getWriter().println("updatePGPaymentConfigStats.validateErrorJsonResponse: "+validateErrorJsonResponse);
			 
			 if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
			 
				 changedBy = commonUtils.getUserPrincipal(request, "name", response);
				 changedOnInMillis = commonUtils.getCreatedOnDate();
				 changedAt = commonUtils.getCreatedAtTime();
				 
				 for (int i = 0; i < jsonArrayPayload.length(); i++) {
					 
					 executeURL =""; requestAggrID=""; configGuid="";guid="";
					 updatePayLoad = new JSONObject();
					 pgPymntConfigsUpdateResponse = new JsonObject();
					 pgPymntConfigStatsJson = new JsonObject();
					 
					 childObjPayload = jsonArrayPayload.getJSONObject(i);
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigStats.childObjPayload ("+i+"): "+childObjPayload);
					 
					 configGuid = childObjPayload.getString("ConfigHeaderGUID");
					 requestAggrID = childObjPayload.getString("AggregatorID");
					 guid = childObjPayload.getString("CSGuid");
					 if(debug){
						 response.getWriter().println("updatePGPaymentConfigStats.guid: "+guid);
						 response.getWriter().println("updatePGPaymentConfigStats.requestAggrID: "+requestAggrID);
						 response.getWriter().println("updatePGPaymentConfigStats.configGuid: "+configGuid);
					 }

					 executeURL = oDataUrl+"PGPaymentConfigStats?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20CSGuid%20eq%20%27"+guid+"%27";
					 executeURL = executeURL.replace(" ", "%20");
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigStats.executeURL: "+executeURL);
					 
					 pgPymntConfigStatsJson = commonUtils.executeURL(executeURL, userPass, response);
					 if(debug)
						 response.getWriter().println("updatePGPaymentConfigStats.pgPymntConfigsJson: "+pgPymntConfigStatsJson);
					 
					 if (pgPymntConfigStatsJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						 
						 if (testRun.equalsIgnoreCase("")) {
							 executeURL = "";

							 JsonObject childJson = pgPymntConfigStatsJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
							 
							 updatePayLoad.accumulate("ChangedBy", changedBy);
							 updatePayLoad.accumulate("ChangedAt", changedAt);
							 updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
							 
							 updatePayLoad.accumulate("AggregatorID", requestAggrID);
							 updatePayLoad.accumulate("CSGuid", guid);
							 updatePayLoad.accumulate("ConfigHeaderGUID",configGuid);
							 
							 if(childObjPayload.has("PGTxnStatus") && ! childObjPayload.isNull("PGTxnStatus"))
								 updatePayLoad.accumulate("PGTxnStatus", childObjPayload.getString("PGTxnStatus"));
							 else
								 updatePayLoad.accumulate("PGTxnStatus", "");
							 
							 if(childObjPayload.has("PymntStatus") && ! childObjPayload.isNull("PymntStatus"))
								 updatePayLoad.accumulate("PymntStatus", childObjPayload.getString("PymntStatus"));
							 else
								 updatePayLoad.accumulate("PymntStatus", "");
							 
							 if ( ! childJson.get("CreatedBy").isJsonNull() )
								 updatePayLoad.accumulate("CreatedBy", childJson.get("CreatedBy").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedBy", "");
							 
							 if ( ! childJson.get("CreatedAt").isJsonNull() )
								 updatePayLoad.accumulate("CreatedAt", childJson.get("CreatedAt").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							 
							 if (  ! childJson.get("CreatedOn").isJsonNull() )
								 updatePayLoad.accumulate("CreatedOn", childJson.get("CreatedOn").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							 
							 if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								 updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));	
							 else
								 updatePayLoad.accumulate("Source", "");
							 
							 if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								 updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							 else
								 updatePayLoad.accumulate("SourceReferenceID", "");
							 
							 executeURL = oDataUrl+"PGPaymentConfigStats('"+guid+"')";
							 if(debug)
								 response.getWriter().println("updatePGPaymentConfigStats.executeURL: "+executeURL);
							 
							 pgPymntConfigsUpdateResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							 if(debug)
								 response.getWriter().println("updatePGPaymentConfigStats.pgPymntConfigsUpdateResponse: "+pgPymntConfigsUpdateResponse);
							 
							 if (pgPymntConfigsUpdateResponse.has("error")|| pgPymntConfigsUpdateResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								 responseCode ="000004";
								 errorCode ="E003";
								 isValidationFailed = true;
								 break;
							 }
						 } else {
							 break;
						 }
					 } else {
						 responseCode ="000002";
						 errorCode ="E004";
						 isValidationFailed = true;
						 break;
					 }
				}
			 }
			 else{
				 responseCode ="000002";
				 errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				 isValidationFailed = true;
			 }
			 if ( isValidationFailed) {
				 pgPymntConfigsUpdateResponse = new JsonObject();
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 pgPymntConfigsUpdateResponse.addProperty("ErrorCode", errorCode);
				 pgPymntConfigsUpdateResponse.addProperty("ResponseCode", responseCode);
				 if(errorCode.equalsIgnoreCase("E003"))
					 pgPymntConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				 else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					 pgPymntConfigsUpdateResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				 else
					 pgPymntConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			 else{
				 pgPymntConfigsUpdateResponse.addProperty("Message", "Success");
				 pgPymntConfigsUpdateResponse.addProperty("ErrorCode", "");
				 pgPymntConfigsUpdateResponse.addProperty("ResponseCode", "000001");
			 }
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			pgPymntConfigsUpdateResponse = new JsonObject();
			pgPymntConfigsUpdateResponse.addProperty("ErrorCode", "E000");
			pgPymntConfigsUpdateResponse.addProperty("ResponseCode", "000003");
			pgPymntConfigsUpdateResponse.addProperty("Message", buffer.toString());
		}
		return pgPymntConfigsUpdateResponse;	
	}
	
	//TODO: added by kamlesh on 03-06-2020 for ValueHelps
	public JsonObject insertValueHelpsInsert(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",modelID="", entityType="",propName="",parentID="", ID="", partnerNo="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonArray insertValueHelpsArray = new JsonArray();
		JsonObject insertValueHelpsJson = new JsonObject();
		JsonObject insertValueHelpsMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject valueHelpsPOSTResponse = null;
		JsonObject valueHelpsJsson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.ValueHelpsType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/ValueHelps(AggregatorID='',ModelID='',EntityType='',PropName='',ParentID='',ID='',PartnerNo='')\"},"
				+ "\"AggregatorID\":\"\",\"ModelID\":\"PYGW\",\"EntityType\":\"UserRegistration\",\"PropName\":\"\",\"ParentID\":\"\","
				+ "\"ID\":\"\",\"PartnerNo\":\"ALL\",\"LoginID\":null,\"Description\":null,\"PartnerName\":null,\"IsDefault\":null,\"DepPropDefID\":null,\"DepPropName\":null,"
				+ "\"LabelParentID\":null,\"LabelID\":null,\"LabelDepPropDefID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("ValueHelps")) {
				jsonArrayPayload = inputPayload.getJSONArray("ValueHelps");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validateValueHelpsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertValueHelpsInsert.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					valueHelpsPOSTResponse = new JsonObject();
					valueHelpsJsson = new JsonObject();
					executeURL =""; requestAggrID=""; modelID=""; entityType=""; propName=""; parentID=""; ID=""; partnerNo=""; 
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertValueHelpsInsert.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					modelID = childObjPayload.getString("ModelID");
					entityType = childObjPayload.getString("EntityType");
					propName = childObjPayload.getString("PropName");
					parentID = childObjPayload.getString("ParentID");
					ID = childObjPayload.getString("ID");
					partnerNo = childObjPayload.getString("PartnerNo");
					
					executeURL = oDataUrl+"ValueHelps?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ModelID%20eq%20%27"+modelID+"%27%20and%20EntityType%20eq%20%27"+entityType+"%27"
							+ "%20and%20PropName%20eq%20%27"+propName+"%27%20and%20ParentID%20eq%20%27"+parentID+"%27%20and%20ID%20eq%20%27"+ID+"%27%20and%20PartnerNo%20eq%20%27"+partnerNo+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertValueHelpsInsert.executeURL: "+executeURL);
					
					valueHelpsJsson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertValueHelpsInsert.valueHelpsJsson: "+valueHelpsJsson);
					
					if (valueHelpsJsson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"ValueHelps";
							
							insertPayload.accumulate("AggregatorID", requestAggrID);
							insertPayload.accumulate("ModelID", modelID);
							insertPayload.accumulate("EntityType",entityType);
							insertPayload.accumulate("PropName", propName);
							insertPayload.accumulate("ParentID", parentID);
							insertPayload.accumulate("ID",ID);
							insertPayload.accumulate("PartnerNo", partnerNo);
							
							if(childObjPayload.has("LoginID") && ! childObjPayload.isNull("LoginID"))
								insertPayload.accumulate("LoginID", childObjPayload.getString("LoginID"));
							else
								insertPayload.accumulate("LoginID", "");
							
							if(childObjPayload.has("Description") && ! childObjPayload.isNull("Description"))
								insertPayload.accumulate("Description", childObjPayload.getString("Description"));
							else
								insertPayload.accumulate("Description", "");
							
							if(childObjPayload.has("PartnerName") && !childObjPayload.isNull("PartnerName"))
								insertPayload.accumulate("PartnerName", childObjPayload.getString("PartnerName"));	
							else
								insertPayload.accumulate("PartnerName", "");
							
							if(childObjPayload.has("IsDefault") && !childObjPayload.isNull("IsDefault"))
								insertPayload.accumulate("IsDefault", childObjPayload.getString("IsDefault"));	
							else
								insertPayload.accumulate("IsDefault", "");
							
							if(childObjPayload.has("DepPropDefID") && !childObjPayload.isNull("DepPropDefID"))
								insertPayload.accumulate("DepPropDefID", childObjPayload.getString("DepPropDefID"));	
							else
								insertPayload.accumulate("DepPropDefID", "");
							
							if(childObjPayload.has("DepPropName") && !childObjPayload.isNull("DepPropName"))
								insertPayload.accumulate("DepPropName", childObjPayload.getString("DepPropName"));	
							else
								insertPayload.accumulate("DepPropName", "");
							
							if(childObjPayload.has("LabelParentID") && !childObjPayload.isNull("LabelParentID"))
								insertPayload.accumulate("LabelParentID", childObjPayload.getString("LabelParentID"));	
							else
								insertPayload.accumulate("LabelParentID", "");
							
							if(childObjPayload.has("LabelID") && !childObjPayload.isNull("LabelID"))
								insertPayload.accumulate("LabelID", childObjPayload.getString("LabelID"));	
							else
								insertPayload.accumulate("LabelID", "");
							
							if(childObjPayload.has("LabelDepPropDefID") && !childObjPayload.isNull("LabelDepPropDefID"))
								insertPayload.accumulate("LabelDepPropDefID", childObjPayload.getString("LabelDepPropDefID"));	
							else
								insertPayload.accumulate("LabelDepPropDefID", "");
							
							valueHelpsPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertValueHelpsInsert.valueHelpsPOSTResponse: "+valueHelpsPOSTResponse);
							
							if (valueHelpsPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertValueHelpsArray.add(valueHelpsPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertValueHelpsMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertValueHelpsMasterJson.addProperty("ErrorCode", errorCode);
				insertValueHelpsMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertValueHelpsMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertValueHelpsMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertValueHelpsMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if ( ! insertValueHelpsMasterJson.has("d")){
					insertValueHelpsJson.add("results", insertValueHelpsArray);
					insertValueHelpsMasterJson.add("d", insertValueHelpsJson);
				}
				insertValueHelpsMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertValueHelpsMasterJson = new JsonObject();
			insertValueHelpsMasterJson.addProperty("ErrorCode", "E000");
			insertValueHelpsMasterJson.addProperty("ResponseCode", "000003");
			insertValueHelpsMasterJson.addProperty("Message", buffer.toString());
		}
		return insertValueHelpsMasterJson;
	}
	
	//added by kamlesh for request fields validation 02-06-2020
	public JsonObject validateValueHelpsInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String errorMsg="", errorCOde="";
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			for (int i = 0; i < inputArrayPayload.length(); i++) {
				
				
				inputPayload = inputArrayPayload.getJSONObject(i);
				if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
					errorMsg = "AggregatorID";
				
				if (inputPayload.isNull("ModelID") || inputPayload.getString("ModelID").trim().length() == 0){
					if(errorMsg.trim().length() > 0)
						errorMsg = errorMsg + ", ModelID";
					else
						errorMsg = "ModelID";
				}
				
				if (inputPayload.isNull("EntityType") || inputPayload.getString("EntityType").trim().length() == 0){
					if(errorMsg.trim().length() > 0)
						errorMsg = errorMsg + ", EntityType";
					else
						errorMsg = "EntityType";
				}
				
				if (inputPayload.isNull("PropName") || inputPayload.getString("PropName").trim().length() == 0){
					if(errorMsg.trim().length()> 0)
						errorMsg = errorMsg + ", PropName";
					else
						errorMsg = "PropName";
				}
				
				if (inputPayload.isNull("ParentID") || inputPayload.getString("ParentID").trim().length() == 0){
					if(errorMsg.trim().length()> 0)
						errorMsg = errorMsg + ", ParentID";
					else
						errorMsg = "ParentID";
				}
				
				if (inputPayload.isNull("ID") || inputPayload.getString("ID").trim().length() == 0){
					if(errorMsg.trim().length()> 0)
						errorMsg = errorMsg + ", ID";
					else
						errorMsg = "ID";
				}
				
				if (inputPayload.isNull("PartnerNo") || inputPayload.getString("PartnerNo").trim().length() == 0){
					if(errorMsg.trim().length()> 0)
						errorMsg = errorMsg + ", PartnerNo";
					else
						errorMsg = "PartnerNo";
				}
				
				if(debug)
					response.getWriter().println("validateValueHelpsInsert.errorMsg.i ("+i+"): "+errorMsg);
				if ( errorMsg.trim().length() > 0) {
					errorCOde = "E001";
					errorResponse.addProperty("ErrorCode", errorCOde);
					errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
					break;
				}
				else
					errorMsg="";
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}
	
	//added by kamlesh 03-06-2020 ValueHelps table
	public JsonObject deleteValueHelps(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="", testRun="", requestAggrID="",modelID="", entityType="",propName="",parentID="", ID="", partnerNo="";;
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		
		JsonObject valueHelpsHttpDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			requestAggrID = getGuidResponse.get("AggregatorID").getAsString();
			modelID = getGuidResponse.get("ModelID").getAsString();
			entityType = getGuidResponse.get("EntityType").getAsString();
			propName =getGuidResponse.get("PropName").getAsString();
			parentID = getGuidResponse.get("ParentID").getAsString();
			ID = getGuidResponse.get("ID").getAsString();
			partnerNo = getGuidResponse.get("PartnerNo").getAsString();
			
			if(debug){
				response.getWriter().println("deleteValueHelps.pathInfo: "+pathInfo);
				response.getWriter().println("deleteValueHelps.requestAggrID: "+requestAggrID);
				response.getWriter().println("deleteValueHelps.modelID: "+modelID);
				response.getWriter().println("deleteValueHelps.entityType: "+entityType);
				response.getWriter().println("deleteValueHelps.propName: "+propName);
				response.getWriter().println("deleteValueHelps.parentID: "+parentID);
				response.getWriter().println("deleteValueHelps.ID: "+ID);
				response.getWriter().println("deleteValueHelps.partnerNo: "+partnerNo);
			}
			if ( requestAggrID.trim().length() ==0 || modelID.trim().length() ==0 || entityType.trim().length() ==0 ||
					propName.trim().length() ==0 || parentID.trim().length() ==0 || ID.trim().length() ==0 || partnerNo.trim().length() ==0) {
				isValidationFailed = true;
			}
			
			if ( ! isValidationFailed) {
				
				executeURL = oDataURL+"ValueHelps?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ModelID%20eq%20%27"+modelID+"%27%20and%20EntityType%20eq%20%27"+entityType+"%27"
						+ "%20and%20PropName%20eq%20%27"+propName+"%27%20and%20ParentID%20eq%20%27"+parentID+"%27%20and%20ID%20eq%20%27"+ID+"%27%20and%20PartnerNo%20eq%20%27"+partnerNo+"%27";
				executeURL = executeURL.replace(" ", "%20");
				if(debug)
					response.getWriter().println("deleteValueHelps.executeURL: "+executeURL);
				
				JsonObject valueHelpsGETJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deleteValueHelps.valueHelpsGETJson: "+valueHelpsGETJson);
				if (valueHelpsGETJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deleteValueHelps.executeURL: "+executeURL);
						
						valueHelpsHttpDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deleteValueHelps.valueHelpsHttpDeleteResponse: "+valueHelpsHttpDeleteResponse);
						
						if(! valueHelpsHttpDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! valueHelpsHttpDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode","");
							errorResonse.addProperty("Message", "Success");
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "Success");
//						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}
	
	//added by kamlesh 03-06-2020 updateValueHelps
	public JsonObject updateValueHelps(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",modelID="", entityType="",propName="",parentID="", ID="", partnerNo="";
		JsonObject valueHelpsUpdateResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject valueHelpsGETJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{
			
			if (inputPayload.has("ValueHelps")) {
				jsonArrayPayload = inputPayload.getJSONArray("ValueHelps");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			JsonObject validateErrorJsonResponse = validateValueHelpsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("updateValueHelps.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					executeURL =""; requestAggrID="";modelID=""; entityType="";propName="";parentID=""; ID=""; partnerNo="";
					updatePayLoad = new JSONObject();
					valueHelpsUpdateResponse = new JsonObject();
					valueHelpsGETJson = new JsonObject();
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("updateValueHelps.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					modelID = childObjPayload.getString("ModelID");
					entityType = childObjPayload.getString("EntityType");
					propName = childObjPayload.getString("PropName");
					parentID = childObjPayload.getString("ParentID");
					ID = childObjPayload.getString("ID");
					partnerNo = childObjPayload.getString("PartnerNo");
					
					executeURL = oDataUrl+"ValueHelps?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ModelID%20eq%20%27"+modelID+"%27%20and%20EntityType%20eq%20%27"+entityType+"%27"
							+ "%20and%20PropName%20eq%20%27"+propName+"%27%20and%20ParentID%20eq%20%27"+parentID+"%27%20and%20ID%20eq%20%27"+ID+"%27%20and%20PartnerNo%20eq%20%27"+partnerNo+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("updateValueHelps.executeURL.1: "+executeURL);
					
					valueHelpsGETJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("updateValueHelps.valueHelpsGETJson: "+valueHelpsGETJson);
					
					if (valueHelpsGETJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							updatePayLoad.accumulate("AggregatorID", requestAggrID);
							updatePayLoad.accumulate("ModelID", modelID);
							updatePayLoad.accumulate("EntityType",entityType);
							updatePayLoad.accumulate("PropName", propName);
							updatePayLoad.accumulate("ParentID", parentID);
							updatePayLoad.accumulate("ID",ID);
							updatePayLoad.accumulate("PartnerNo", partnerNo);
							
							if(childObjPayload.has("LoginID") && ! childObjPayload.isNull("LoginID"))
								updatePayLoad.accumulate("LoginID", childObjPayload.getString("LoginID"));
							else
								updatePayLoad.accumulate("LoginID", "");
							
							if(childObjPayload.has("Description") && ! childObjPayload.isNull("Description"))
								updatePayLoad.accumulate("Description", childObjPayload.getString("Description"));
							else
								updatePayLoad.accumulate("Description", "");
							
							if(childObjPayload.has("PartnerName") && !childObjPayload.isNull("PartnerName"))
								updatePayLoad.accumulate("PartnerName", childObjPayload.getString("PartnerName"));	
							else
								updatePayLoad.accumulate("PartnerName", "");
							
							if(childObjPayload.has("IsDefault") && !childObjPayload.isNull("IsDefault"))
								updatePayLoad.accumulate("IsDefault", childObjPayload.getString("IsDefault"));	
							else
								updatePayLoad.accumulate("IsDefault", "");
							
							if(childObjPayload.has("DepPropDefID") && !childObjPayload.isNull("DepPropDefID"))
								updatePayLoad.accumulate("DepPropDefID", childObjPayload.getString("DepPropDefID"));	
							else
								updatePayLoad.accumulate("DepPropDefID", "");
							
							if(childObjPayload.has("DepPropName") && !childObjPayload.isNull("DepPropName"))
								updatePayLoad.accumulate("DepPropName", childObjPayload.getString("DepPropName"));	
							else
								updatePayLoad.accumulate("DepPropName", "");
							
							if(childObjPayload.has("LabelParentID") && !childObjPayload.isNull("LabelParentID"))
								updatePayLoad.accumulate("LabelParentID", childObjPayload.getString("LabelParentID"));	
							else
								updatePayLoad.accumulate("LabelParentID", "");
							
							if(childObjPayload.has("LabelID") && !childObjPayload.isNull("LabelID"))
								updatePayLoad.accumulate("LabelID", childObjPayload.getString("LabelID"));	
							else
								updatePayLoad.accumulate("LabelID", "");
							
							if(childObjPayload.has("LabelDepPropDefID") && !childObjPayload.isNull("LabelDepPropDefID"))
								updatePayLoad.accumulate("LabelDepPropDefID", childObjPayload.getString("LabelDepPropDefID"));	
							else
								updatePayLoad.accumulate("LabelDepPropDefID", "");
							
							executeURL = oDataUrl+"ValueHelps(AggregatorID='"+requestAggrID+"',ModelID='"+modelID+"',EntityType='"+entityType+"',PropName='"+propName+"',ParentID='"+parentID+"',ID='"+ID+"',PartnerNo='"+partnerNo+"')";
							executeURL = executeURL.replace(" ", "%20");
							if(debug)
								response.getWriter().println("updateValueHelps.executeURL.2: "+executeURL);
							
							valueHelpsUpdateResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("updateValueHelps.valueHelpsUpdateResponse: "+valueHelpsUpdateResponse);
							
							if (valueHelpsUpdateResponse.has("error")|| valueHelpsUpdateResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}
						} else {
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E004";
						isValidationFailed = true;
						break;
					}
				}
			}
			else{
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				isValidationFailed = true;
			}
			if ( isValidationFailed) {
				valueHelpsUpdateResponse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				valueHelpsUpdateResponse.addProperty("ErrorCode", errorCode);
				valueHelpsUpdateResponse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					valueHelpsUpdateResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					valueHelpsUpdateResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					valueHelpsUpdateResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			else{
				valueHelpsUpdateResponse.addProperty("Message", "Success");
				valueHelpsUpdateResponse.addProperty("ErrorCode", "");
				valueHelpsUpdateResponse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			valueHelpsUpdateResponse = new JsonObject();
			valueHelpsUpdateResponse.addProperty("ErrorCode", "E000");
			valueHelpsUpdateResponse.addProperty("ResponseCode", "000003");
			valueHelpsUpdateResponse.addProperty("Message", buffer.toString());
		}
		return valueHelpsUpdateResponse;	
	}
	
	//TODO: added by kamlesh on 04-06-2020 for DocumentRepConfigs
	public JsonObject insertDocumentRepConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",createdBy="", createdAt="", ID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis = 0;
		
		JsonArray insertdocumentRepConfigsArray = new JsonArray();
		JsonObject insertdocumentRepConfigsJson = new JsonObject();
		JsonObject insertdocumentRepConfigsMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject documentRepConfigsPOSTResponse = null;
		JsonObject documentRepConfigsJson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.DocumentRepConfigsType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/DocumentRepConfigs('')\"},\"ID\":\"\","
				+ "\"AggregatorID\":\"\",\"RepositoryName\":\"\",\"RepositoryKey\":\"\",\"TenantID\":\"\",\"SourceID\":\"\",\"RepositoryType\":\"\",\"CreatedBy\":null,"
				+ "\"CreatedAt\":null,\"CreatedOn\":null,\"ChangedBy\":null,\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("DocumentRepConfigs")) {
				jsonArrayPayload = inputPayload.getJSONArray("DocumentRepConfigs");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validateDocumentRepConfigsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertDocumentRepConfigs.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
			
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertDocumentRepConfigs - createdBy: "+createdBy);	
					response.getWriter().println("insertDocumentRepConfigs - createdAt: "+createdAt);	
					response.getWriter().println("insertDocumentRepConfigs - createdOnInMillis: "+createdOnInMillis);	
				}
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					documentRepConfigsPOSTResponse = new JsonObject();
					documentRepConfigsJson = new JsonObject();
					executeURL =""; requestAggrID=""; ID=""; 
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertDocumentRepConfigs.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					ID = childObjPayload.getString("ID");
					
					executeURL = oDataUrl+"DocumentRepConfigs?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ID%20eq%20%27"+ID+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertDocumentRepConfigs.executeURL: "+executeURL);
					
					documentRepConfigsJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertDocumentRepConfigs.documentRepConfigsJson: "+documentRepConfigsJson);
					
					if (documentRepConfigsJson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"DocumentRepConfigs";
							
							ID = commonUtils.generateGUID(36);
							insertPayload.accumulate("ID", ID);
							insertPayload.accumulate("AggregatorID", requestAggrID);
							
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("RepositoryName") && ! childObjPayload.isNull("RepositoryName"))
								insertPayload.accumulate("RepositoryName", childObjPayload.getString("RepositoryName"));
							else
								insertPayload.accumulate("RepositoryName", "");
							
							if(childObjPayload.has("RepositoryKey") && ! childObjPayload.isNull("RepositoryKey"))
								insertPayload.accumulate("RepositoryKey", childObjPayload.getString("RepositoryKey"));
							else
								insertPayload.accumulate("RepositoryKey", "");
							
							if(childObjPayload.has("TenantID") && !childObjPayload.isNull("TenantID"))
								insertPayload.accumulate("TenantID", childObjPayload.getString("TenantID"));	
							else
								insertPayload.accumulate("TenantID", "");
							
							if(childObjPayload.has("SourceID") && !childObjPayload.isNull("SourceID"))
								insertPayload.accumulate("SourceID", childObjPayload.getString("SourceID"));	
							else
								insertPayload.accumulate("SourceID", "");
							
							if(childObjPayload.has("RepositoryType") && !childObjPayload.isNull("RepositoryType"))
								insertPayload.accumulate("RepositoryType", childObjPayload.getString("RepositoryType"));	
							else
								insertPayload.accumulate("RepositoryType", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							documentRepConfigsPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertDocumentRepConfigs.documentRepConfigsPOSTResponse: "+documentRepConfigsPOSTResponse);
							
							if (documentRepConfigsPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertdocumentRepConfigsArray.add(documentRepConfigsPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertdocumentRepConfigsMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertdocumentRepConfigsMasterJson.addProperty("ErrorCode", errorCode);
				insertdocumentRepConfigsMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertdocumentRepConfigsMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertdocumentRepConfigsMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertdocumentRepConfigsMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if ( ! insertdocumentRepConfigsMasterJson.has("d")){
					insertdocumentRepConfigsJson.add("results", insertdocumentRepConfigsArray);
					insertdocumentRepConfigsMasterJson.add("d", insertdocumentRepConfigsJson);
				}
				insertdocumentRepConfigsMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertdocumentRepConfigsMasterJson = new JsonObject();
			insertdocumentRepConfigsMasterJson.addProperty("ErrorCode", "E000");
			insertdocumentRepConfigsMasterJson.addProperty("ResponseCode", "000003");
			insertdocumentRepConfigsMasterJson.addProperty("Message", buffer.toString());
		}
		return insertdocumentRepConfigsMasterJson;
	}
	
	//added by kamlesh for request fields validation 02-06-2020
	public JsonObject validateDocumentRepConfigsInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String errorMsg="", errorCOde="";
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			for (int i = 0; i < inputArrayPayload.length(); i++) {
				
				inputPayload = inputArrayPayload.getJSONObject(i);
				if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
					errorMsg = "AggregatorID";
				
				if ( errorMsg.trim().length() > 0) {
					errorCOde = "E001";
					errorResponse.addProperty("ErrorCode", errorCOde);
					errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
					break;
				}
				else
					errorMsg="";
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}
	
	//added by kamlesh 03-06-2020 updateValueHelps
	public JsonObject updateDocumentRepConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",ID="", changedBy="", changedAt="";
		JsonObject documentRepConfigsUpdateResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		long changedOnInMillis = 0;
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject documentRepConfigsGETJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{
			
			if (inputPayload.has("DocumentRepConfigs")) {
				jsonArrayPayload = inputPayload.getJSONArray("DocumentRepConfigs");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			JsonObject validateErrorJsonResponse = validateDocumentRepConfigsInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("updatedocumentRepConfigs.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				 changedBy = commonUtils.getUserPrincipal(request, "name", response);
				 changedOnInMillis = commonUtils.getCreatedOnDate();
				 changedAt = commonUtils.getCreatedAtTime();
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					executeURL =""; requestAggrID="";ID="";
					updatePayLoad = new JSONObject();
					documentRepConfigsUpdateResponse = new JsonObject();
					documentRepConfigsGETJson = new JsonObject();
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("updatedocumentRepConfigs.childObjPayload ("+i+"): "+childObjPayload);
					
					ID = childObjPayload.getString("ID");
					
					executeURL = oDataUrl+"DocumentRepConfigs?$filter=ID%20eq%20%27"+ID+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("updatedocumentRepConfigs.executeURL: "+executeURL);
					
					documentRepConfigsGETJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("updatedocumentRepConfigs.documentRepConfigsGETJson: "+documentRepConfigsGETJson);
					
					if (documentRepConfigsGETJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							JsonObject childJson = documentRepConfigsGETJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();

							requestAggrID = childObjPayload.getString("AggregatorID");
							updatePayLoad.accumulate("ID", ID);
							updatePayLoad.accumulate("AggregatorID", requestAggrID);
							
							 updatePayLoad.accumulate("ChangedBy", changedBy);
							 updatePayLoad.accumulate("ChangedAt", changedAt);
							 updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
							
							if(! childObjPayload.isNull("RepositoryName"))
								updatePayLoad.accumulate("RepositoryName", childObjPayload.getString("RepositoryName"));
							else
								updatePayLoad.accumulate("RepositoryName", "");
							
							if(! childObjPayload.isNull("RepositoryKey"))
								updatePayLoad.accumulate("RepositoryKey", childObjPayload.getString("RepositoryKey"));
							else
								updatePayLoad.accumulate("RepositoryKey", "");
							
							if(!childObjPayload.isNull("TenantID"))
								updatePayLoad.accumulate("TenantID", childObjPayload.getString("TenantID"));	
							else
								updatePayLoad.accumulate("TenantID", "");
							
							if( !childObjPayload.isNull("SourceID"))
								updatePayLoad.accumulate("SourceID", childObjPayload.getString("SourceID"));	
							else
								updatePayLoad.accumulate("SourceID", "");
							
							if(!childObjPayload.isNull("RepositoryType"))
								updatePayLoad.accumulate("RepositoryType", childObjPayload.getString("RepositoryType"));	
							else
								updatePayLoad.accumulate("RepositoryType", "");
							
							if(! childObjPayload.isNull("Source"))
								updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));	
							else
								updatePayLoad.accumulate("Source", "");
							
							if(! childObjPayload.isNull("SourceReferenceID"))
								updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								updatePayLoad.accumulate("SourceReferenceID", "");
							
							if ( ! childJson.get("CreatedBy").isJsonNull() )
								updatePayLoad.accumulate("CreatedBy", childJson.get("CreatedBy").getAsString());
							else
								updatePayLoad.accumulate("CreatedBy", "");
							
							if ( ! childJson.get("CreatedAt").isJsonNull() )
								updatePayLoad.accumulate("CreatedAt", childJson.get("CreatedAt").getAsString());
							else
								updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							
							if (  ! childJson.get("CreatedOn").isJsonNull() )
								updatePayLoad.accumulate("CreatedOn", childJson.get("CreatedOn").getAsString());
							else
								updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							
							executeURL = oDataUrl+"DocumentRepConfigs('"+ID+"')";
							executeURL = executeURL.replace(" ", "%20");
							if(debug)
								response.getWriter().println("updatedocumentRepConfigs.executeURL.2: "+executeURL);
							
							documentRepConfigsUpdateResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("updatedocumentRepConfigs.documentRepConfigsUpdateResponse: "+documentRepConfigsUpdateResponse);
							
							if (documentRepConfigsUpdateResponse.has("error")|| documentRepConfigsUpdateResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}
						} else {
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E004";
						isValidationFailed = true;
						break;
					}
				}
			}
			else{
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				isValidationFailed = true;
			}
			if ( isValidationFailed) {
				documentRepConfigsUpdateResponse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				documentRepConfigsUpdateResponse.addProperty("ErrorCode", errorCode);
				documentRepConfigsUpdateResponse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					documentRepConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					documentRepConfigsUpdateResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					documentRepConfigsUpdateResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			else{
				documentRepConfigsUpdateResponse.addProperty("Message", "Success");
				documentRepConfigsUpdateResponse.addProperty("ErrorCode", "");
				documentRepConfigsUpdateResponse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			documentRepConfigsUpdateResponse = new JsonObject();
			documentRepConfigsUpdateResponse.addProperty("ErrorCode", "E000");
			documentRepConfigsUpdateResponse.addProperty("ResponseCode", "000003");
			documentRepConfigsUpdateResponse.addProperty("Message", buffer.toString());
		}
		return documentRepConfigsUpdateResponse;	
	}
	
	//added by kamlesh 04-06-2020 DocumentRepConfigs table
	public JsonObject deletDocumentRepConfigs(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="", testRun="", ID="";
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		boolean isValidationFailed = false;
		
		JsonObject documentRepConfigsHttpDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			ID = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deletDocumentRepConfigs.pathInfo: "+pathInfo);
				response.getWriter().println("deletDocumentRepConfigs.ID: "+ID);
			}
			
			if ( ID.trim().length() > 0) {
				
				executeURL = oDataURL+"DocumentRepConfigs?$filter=ID%20eq%20%27"+ID+"%27";
				executeURL = executeURL.replace(" ", "%20");
				if(debug)
					response.getWriter().println("deletDocumentRepConfigs.executeURL: "+executeURL);
				
				JsonObject documentRefConfigsGETJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deletDocumentRepConfigs.documentRefConfigsGETJson: "+documentRefConfigsGETJson);
				if (documentRefConfigsGETJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deletDocumentRepConfigs.executeURL: "+executeURL);
						
						documentRepConfigsHttpDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deletDocumentRepConfigs.documentRepConfigsHttpDeleteResponse: "+documentRepConfigsHttpDeleteResponse);
						
						if(! documentRepConfigsHttpDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! documentRepConfigsHttpDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode","");
							errorResonse.addProperty("Message", "Success");
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "Success");
//							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}
	
	//TODO: added by kamlesh on 05-06-2020 for DocumentRepository
	public JsonObject insertDocumentRepository(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",guid="",createdBy="", createdAt="", repositoryConfigID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis =0;
		
		JsonArray insertDocRepositoryArray = new JsonArray();
		JsonObject insertDocRepositoryJson = new JsonObject();
		JsonObject insertDocRepositoryMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject documentRepositoryPOSTResponse = null;
		JsonObject documentRepositoryJson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.DocumentRepositoryType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/DocumentRepository('')\"},\"ID\":\"\",\"RepositoryConfigID\":\"\","
				+ "\"AggregatorID\":\"\",\"FolderID\":\"\",\"Description\":null,\"CreatedBy\":null,\"CreatedAt\":null,"
				+ "\"CreatedOn\":null,\"ChangedBy\":null,\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("DocumentRepository")) {
				jsonArrayPayload = inputPayload.getJSONArray("DocumentRepository");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validateDocumentRepository(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertDocumentRepository.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertDocumentRepository - createdBy: "+createdBy);	
					response.getWriter().println("insertDocumentRepository - createdAt: "+createdAt);	
					response.getWriter().println("insertDocumentRepository - createdOnInMillis: "+createdOnInMillis);	
				}
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					documentRepositoryPOSTResponse = new JsonObject();
					documentRepositoryJson = new JsonObject();
					executeURL =""; requestAggrID=""; guid="";repositoryConfigID="";
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					guid = childObjPayload.getString("ID");
					
					executeURL = oDataUrl+"DocumentRepository?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ID%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.executeURL: "+executeURL);
					
					documentRepositoryJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertPGPaymentConfigStats.documentRepositoryJson: "+documentRepositoryJson);
					
					if (documentRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"DocumentRepository";
							
							guid = commonUtils.generateGUID(36);
							
							insertPayload.accumulate("AggregatorID", requestAggrID);
							insertPayload.accumulate("RepositoryConfigID", childObjPayload.getString("RepositoryConfigID"));
							insertPayload.accumulate("ID",guid);
							
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("FolderID") && ! childObjPayload.isNull("FolderID"))
								insertPayload.accumulate("FolderID", childObjPayload.getString("FolderID"));
							else
								insertPayload.accumulate("FolderID", "");
							
							if(childObjPayload.has("Description") && ! childObjPayload.isNull("Description"))
								insertPayload.accumulate("Description", childObjPayload.getString("Description"));
							else
								insertPayload.accumulate("Description", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							documentRepositoryPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertPGPaymentConfigStats.documentRepositoryPOSTResponse: "+documentRepositoryPOSTResponse);
							
							if (documentRepositoryPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertDocRepositoryArray.add(documentRepositoryPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertDocRepositoryMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertDocRepositoryMasterJson.addProperty("ErrorCode", errorCode);
				insertDocRepositoryMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertDocRepositoryMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertDocRepositoryMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertDocRepositoryMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if ( ! insertDocRepositoryMasterJson.has("d")){
					insertDocRepositoryJson.add("results", insertDocRepositoryArray);
					insertDocRepositoryMasterJson.add("d", insertDocRepositoryJson);
				}
				insertDocRepositoryMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertDocRepositoryMasterJson = new JsonObject();
			insertDocRepositoryMasterJson.addProperty("ErrorCode", "E000");
			insertDocRepositoryMasterJson.addProperty("ResponseCode", "000003");
			insertDocRepositoryMasterJson.addProperty("Message", buffer.toString());
		}
		return insertDocRepositoryMasterJson;
	}
	
	//added by kamlesh for request fields validation 08-06-2020
	public JsonObject validateDocumentRepository(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String errorMsg="", errorCOde="", repositoryConfigGuid="", executeURL="", requestAGGRID="" ;
		boolean isSuccess = false;
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			
//			executeURL = oDataUrl+"DocumentRepConfigs?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
//			if(debug)
//				response.getWriter().println("validateDocumentRepository.executeURL: "+executeURL);
//			
//			JsonObject getConfigTypesJson = commonUtils.executeURL(executeURL, userPass, response);
//			if(debug)
//				response.getWriter().println("validateDocumentRepository.getConfigTypesJson: "+getConfigTypesJson);
			
//			JsonArray pgPymntConfigJsonArray= getConfigTypesJson.getAsJsonObject("d").getAsJsonArray("results");
//				if(debug)
//					response.getWriter().println("validatePGConfigStatsInsert.pgPymntConfigJsonArray: "+pgPymntConfigJsonArray);
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			for (int i = 0; i < inputArrayPayload.length(); i++) {
				
				executeURL ="";
				isSuccess = false;
				errorMsg=""; repositoryConfigGuid=""; requestAGGRID="";
				inputPayload = inputArrayPayload.getJSONObject(i);
				
				if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
					errorMsg = "AggregatorID";
				else
					requestAGGRID = inputPayload.getString("AggregatorID");
				
				if (inputPayload.isNull("RepositoryConfigID") || inputPayload.getString("RepositoryConfigID").trim().length() == 0){
					if(errorMsg.trim().length()> 0)
						errorMsg = errorMsg + ", RepositoryConfigID";
					else
						errorMsg = "RepositoryConfigID";
				}
				else
					repositoryConfigGuid = inputPayload.getString("RepositoryConfigID");
				
				if(debug){
					response.getWriter().println("validateDocumentRepository.repositoryConfigGuid.i ("+i+"): "+repositoryConfigGuid);
					response.getWriter().println("validateDocumentRepository.errorMsg.i ("+i+"): "+errorMsg);
				}
				if (errorMsg.trim().length() == 0) {
					
					executeURL = oDataUrl+"DocumentRepConfigs?$filter=AggregatorID%20eq%20%27"+requestAGGRID+"%27%20and%20ID%20eq%20%27"+repositoryConfigGuid+"%27";
					if(debug)
						response.getWriter().println("validateDocumentRepository.executeURL: "+executeURL);
					
					JsonObject getDocRepositoryJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("validateDocumentRepository.getDocRepositoryJson: "+getDocRepositoryJson);
					
					if(getDocRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
					{
						JsonObject childConfigGuidJson = new JsonObject();
						childConfigGuidJson = getDocRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
						if (repositoryConfigGuid.equalsIgnoreCase(childConfigGuidJson.get("ID").getAsString())){
							isSuccess = true;
						}
					}
					else{
						isSuccess = false;
					}
					if (! isSuccess) {
						errorCOde = "E007";
						errorResponse.addProperty("ErrorCode", errorCOde);
						errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde));
						break;
					}
				} else {
					errorCOde = "E001";
					errorResponse.addProperty("ErrorCode", errorCOde);
					errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
					break;
				}
			}
			
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}
	
	//added by kamlesh 08-06-2020
	public JsonObject deleteDocumentRepository(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",guid="", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		
		JsonObject docRepsitoryHttpDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("GUID"))
				guid = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deleteDocumentRepository.pathInfo: "+pathInfo);
				response.getWriter().println("deleteDocumentRepository.guid: "+guid);
			}
			
			if (! guid.equalsIgnoreCase("")) {
				executeURL = oDataURL+"DocumentRepository?$filter=ID%20eq%20%27"+guid+"%27";
				if(debug)
					response.getWriter().println("deleteDocumentRepository.executeURL: "+executeURL);
				
				JsonObject documentRepositoryJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deleteDocumentRepository.documentRepositoryJson: "+documentRepositoryJson);
				if (documentRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deleteDocumentRepository.executeURL: "+executeURL);
						
						docRepsitoryHttpDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deleteDocumentRepository.docRepsitoryHttpDeleteResponse: "+docRepsitoryHttpDeleteResponse);
						
						if(! docRepsitoryHttpDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! docRepsitoryHttpDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode", docRepsitoryHttpDeleteResponse.get("ErrorCode").getAsString());
							errorResonse.addProperty("Message", docRepsitoryHttpDeleteResponse.get("ErrorMessage").getAsString());
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "");
//								response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}
	
	//added by kamlesh 08-06-2020
	public JsonObject updateDocumentRepository(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",repositoryConfigGuid="",changedBy="",changedAt="", guid="";
		JsonObject UpdateDocumentRepositoryResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		long changedOnInMillis=0;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject documentRepositoryJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{
			
			if (inputPayload.has("DocumentRepository")) {
				jsonArrayPayload = inputPayload.getJSONArray("DocumentRepository");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			JsonObject validateErrorJsonResponse = validateDocumentRepository(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("updateDocumentRepository.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				changedBy = commonUtils.getUserPrincipal(request, "name", response);
				changedOnInMillis = commonUtils.getCreatedOnDate();
				changedAt = commonUtils.getCreatedAtTime();
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					executeURL =""; requestAggrID=""; repositoryConfigGuid="";guid="";
					updatePayLoad = new JSONObject();
					UpdateDocumentRepositoryResponse = new JsonObject();
					documentRepositoryJson = new JsonObject();
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("updateDocumentRepository.childObjPayload ("+i+"): "+childObjPayload);
					
					repositoryConfigGuid = childObjPayload.getString("RepositoryConfigID");
					requestAggrID = childObjPayload.getString("AggregatorID");
					guid = childObjPayload.getString("ID");
					if(debug){
						response.getWriter().println("updateDocumentRepository.guid: "+guid);
						response.getWriter().println("updateDocumentRepository.requestAggrID: "+requestAggrID);
						response.getWriter().println("updateDocumentRepository.repositoryConfigGuid: "+repositoryConfigGuid);
					}
					
					executeURL = oDataUrl+"DocumentRepository?$filter=ID%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("updatePGPaymentConfigStats.executeURL: "+executeURL);
					
					documentRepositoryJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("updatePGPaymentConfigStats.documentRepositoryJson: "+documentRepositoryJson);
					
					if (documentRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							
							JsonObject childJson = documentRepositoryJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
							
							updatePayLoad.accumulate("ChangedBy", changedBy);
							updatePayLoad.accumulate("ChangedAt", changedAt);
							updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
							
							updatePayLoad.accumulate("AggregatorID", requestAggrID);
							updatePayLoad.accumulate("ID", guid);
							updatePayLoad.accumulate("RepositoryConfigID",repositoryConfigGuid);
							
							if(childObjPayload.has("FolderID") && ! childObjPayload.isNull("FolderID"))
								updatePayLoad.accumulate("FolderID", childObjPayload.getString("FolderID"));
							else
								updatePayLoad.accumulate("FolderID", "");
							
							if(childObjPayload.has("Description") && ! childObjPayload.isNull("Description"))
								updatePayLoad.accumulate("Description", childObjPayload.getString("Description"));
							else
								updatePayLoad.accumulate("Description", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));	
							else
								updatePayLoad.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								updatePayLoad.accumulate("SourceReferenceID", "");
							
							 if ( ! childJson.get("CreatedBy").isJsonNull() )
								 updatePayLoad.accumulate("CreatedBy", childJson.get("CreatedBy").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedBy", "");
							 
							 if ( ! childJson.get("CreatedAt").isJsonNull() )
								 updatePayLoad.accumulate("CreatedAt", childJson.get("CreatedAt").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							 
							 if (  ! childJson.get("CreatedOn").isJsonNull() )
								 updatePayLoad.accumulate("CreatedOn", childJson.get("CreatedOn").getAsString());
							 else
								 updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							
							executeURL = oDataUrl+"DocumentRepository('"+guid+"')";
							if(debug)
								response.getWriter().println("updateDocumentRepository.executeURL: "+executeURL);
							
							UpdateDocumentRepositoryResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("updateDocumentRepository.UpdateDocumentRepositoryResponse: "+UpdateDocumentRepositoryResponse);
							
							if (UpdateDocumentRepositoryResponse.has("error")|| UpdateDocumentRepositoryResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}
						} else {
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E004";
						isValidationFailed = true;
						break;
					}
				}
			}
			else{
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				isValidationFailed = true;
			}
			if ( isValidationFailed) {
				UpdateDocumentRepositoryResponse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				UpdateDocumentRepositoryResponse.addProperty("ErrorCode", errorCode);
				UpdateDocumentRepositoryResponse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					UpdateDocumentRepositoryResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					UpdateDocumentRepositoryResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					UpdateDocumentRepositoryResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			else{
				UpdateDocumentRepositoryResponse.addProperty("Message", "Success");
				UpdateDocumentRepositoryResponse.addProperty("ErrorCode", "");
				UpdateDocumentRepositoryResponse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			UpdateDocumentRepositoryResponse = new JsonObject();
			UpdateDocumentRepositoryResponse.addProperty("ErrorCode", "E000");
			UpdateDocumentRepositoryResponse.addProperty("ResponseCode", "000003");
			UpdateDocumentRepositoryResponse.addProperty("Message", buffer.toString());
		}
		return UpdateDocumentRepositoryResponse;	
	}
	
	//TODO: added by kamlesh on 12-06-2020 for MISScheduler
	public JsonObject insertMISScheduler(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",createdBy="", createdAt="", ID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis = 0;
		
		JsonArray insertMISSchedulerArray = new JsonArray();
		JsonObject insertMISSchedulerJson = new JsonObject();
		JsonObject insertMISSchedulerMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject misSchedulerPOSTResponse = null;
		JsonObject misSchedulerJson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PCGW.service.MISSchedulerType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PCGW/service.xsodata/MISScheduler('')\"},\"ID\":\"\",\"AggregatorID\":\"\",\"Report\":\"\","
				+ "\"VariantID\":\"\",\"IsActive\":\"\",\"Periodicity\":\"\",\"QueueName\":null,\"Monday\":null,\"Tuesday\":null,\"Wednesday\":null,\"Thursday\":null,"
				+ "\"Friday\":null,\"Saturday\":null,\"Sunday\":null,\"T00_00\":null,\"T00_30\":null,\"T01_00\":null,\"T01_30\":null,\"T02_00\":null,\"T02_30\":null,"
				+ "\"T03_00\":\"X\",\"T03_30\":null,\"T04_00\":null,\"T04_30\":null,\"T05_00\":null,\"T05_30\":null,\"T06_00\":\"X\",\"T06_30\":null,\"T07_00\":null,\"T07_30\":null,"
				+ "\"T08_00\":null,\"T08_30\":null,\"T09_00\":null,\"T09_30\":null,\"T10_00\":\"X\",\"T10_30\":null,\"T11_00\":null,\"T11_30\":null,\"T12_00\":null,"
				+ "\"T12_30\":null,\"T13_00\":\"X\",\"T13_30\":null,\"T14_00\":\"X\",\"T14_30\":null,\"T15_00\":null,\"T15_30\":null,\"T16_00\":null,\"T16_30\":null,\"T17_00\":null,"
				+ "\"T17_30\":null,\"T18_00\":\"X\",\"T18_30\":null,\"T19_00\":null,\"T19_30\":null,\"T20_00\":null,\"T20_30\":null,\"T21_00\":null,\"T21_30\":null,\"T22_00\":null,"
				+ "\"T22_30\":null,\"T23_00\":null,\"T23_30\":null,\"Input\":null,\"TargetURL\":null,\"CreatedBy\":null,\"CreatedAt\":null,\"CreatedOn\":null,\"ChangedBy\":null,"
				+ "\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("MISScheduler")) {
				jsonArrayPayload = inputPayload.getJSONArray("MISScheduler");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validateMISSchedulerInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertMISScheduler.validateMISSchedulerInsert: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertMISScheduler - createdBy: "+createdBy);	
					response.getWriter().println("insertMISScheduler - createdAt: "+createdAt);	
					response.getWriter().println("insertMISScheduler - createdOnInMillis: "+createdOnInMillis);	
				}
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					misSchedulerPOSTResponse = new JsonObject();
					misSchedulerJson = new JsonObject();
					executeURL =""; requestAggrID=""; ID=""; 
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertMISScheduler.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					ID = childObjPayload.getString("ID");
					
					executeURL = oDataUrl+"MISScheduler?$filter=AggregatorID%20eq%20%27"+requestAggrID+"%27%20and%20ID%20eq%20%27"+ID+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertMISScheduler.executeURL: "+executeURL);
					
					misSchedulerJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertMISScheduler.misSchedulerJson: "+misSchedulerJson);
					
					if (misSchedulerJson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"MISScheduler";
							
							ID = commonUtils.generateGUID(16);
							insertPayload.accumulate("ID", ID);
							insertPayload.accumulate("AggregatorID", requestAggrID);
							
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("Report") && ! childObjPayload.isNull("Report"))
								insertPayload.accumulate("Report", childObjPayload.getString("Report"));
							else
								insertPayload.accumulate("Report", "");
							
							if(childObjPayload.has("VariantID") && ! childObjPayload.isNull("VariantID"))
								insertPayload.accumulate("VariantID", childObjPayload.getString("VariantID"));
							else
								insertPayload.accumulate("VariantID", "");
							
							if(childObjPayload.has("IsActive") && !childObjPayload.isNull("IsActive"))
								insertPayload.accumulate("IsActive", childObjPayload.getString("IsActive"));	
							else
								insertPayload.accumulate("IsActive", "");
							
							if(childObjPayload.has("Periodicity") && !childObjPayload.isNull("Periodicity"))
								insertPayload.accumulate("Periodicity", childObjPayload.getString("Periodicity"));	
							else
								insertPayload.accumulate("Periodicity", "");
							
							if(childObjPayload.has("QueueName") && !childObjPayload.isNull("QueueName"))
								insertPayload.accumulate("QueueName", childObjPayload.getString("QueueName"));	
							else
								insertPayload.accumulate("QueueName", "");
							
							if(childObjPayload.has("Monday") && !childObjPayload.isNull("Monday"))
								insertPayload.accumulate("Monday", childObjPayload.getString("Monday"));	
							else
								insertPayload.accumulate("Monday", "");
							
							if(childObjPayload.has("Tuesday") && !childObjPayload.isNull("Tuesday"))
								insertPayload.accumulate("Tuesday", childObjPayload.getString("Tuesday"));	
							else
								insertPayload.accumulate("Tuesday", "");
							
							if(childObjPayload.has("Wednesday") && !childObjPayload.isNull("Wednesday"))
								insertPayload.accumulate("Wednesday", childObjPayload.getString("Wednesday"));	
							else
								insertPayload.accumulate("Wednesday", "");
							
							if(childObjPayload.has("Thursday") && !childObjPayload.isNull("Thursday"))
								insertPayload.accumulate("Thursday", childObjPayload.getString("Thursday"));	
							else
								insertPayload.accumulate("Thursday", "");
							
							if(childObjPayload.has("Friday") && !childObjPayload.isNull("Friday"))
								insertPayload.accumulate("Friday", childObjPayload.getString("Friday"));	
							else
								insertPayload.accumulate("Friday", "");
							
							if(childObjPayload.has("Saturday") && !childObjPayload.isNull("Saturday"))
								insertPayload.accumulate("Saturday", childObjPayload.getString("Saturday"));	
							else
								insertPayload.accumulate("Saturday", "");
							
							if(childObjPayload.has("Sunday") && !childObjPayload.isNull("Sunday"))
								insertPayload.accumulate("Sunday", childObjPayload.getString("Sunday"));	
							else
								insertPayload.accumulate("Sunday", "");
							
							if(childObjPayload.has("T00_00") && !childObjPayload.isNull("T00_00"))
								insertPayload.accumulate("T00_00", childObjPayload.getString("T00_00"));	
							else
								insertPayload.accumulate("T00_00", "");
							
							if(childObjPayload.has("T00_30") && !childObjPayload.isNull("T00_30"))
								insertPayload.accumulate("T00_30", childObjPayload.getString("T00_30"));	
							else
								insertPayload.accumulate("T00_30", "");
							
							insertPayload.accumulate("T01_00", "");
							insertPayload.accumulate("T01_30", "");
							insertPayload.accumulate("T02_00", "");
							insertPayload.accumulate("T02_30", "");
							insertPayload.accumulate("T03_00", "");
							insertPayload.accumulate("T03_30", "");
							insertPayload.accumulate("T04_00", "");
							insertPayload.accumulate("T04_30", "");
							insertPayload.accumulate("T05_00", "");
							insertPayload.accumulate("T05_30", "");
							insertPayload.accumulate("T06_00", "");
							insertPayload.accumulate("T06_30", "");
							insertPayload.accumulate("T07_00", "");
							insertPayload.accumulate("T07_30", "");
							
//							insertPayload.accumulate("T08_00", "");
//							insertPayload.accumulate("T08_30", "");
//							insertPayload.accumulate("T09_00", "");
//							insertPayload.accumulate("T09_30", "");
//							insertPayload.accumulate("T10_00", "");
//							insertPayload.accumulate("T10_30", "");
//							insertPayload.accumulate("T11_00", "");
//							insertPayload.accumulate("T11_30", "");
//							insertPayload.accumulate("T12_00", "");
//							insertPayload.accumulate("T12_30", "");
//							insertPayload.accumulate("T13_00", "");
//							insertPayload.accumulate("T13_30", "");
//							insertPayload.accumulate("T14_00", "");
//							insertPayload.accumulate("T14_30", "");
//							insertPayload.accumulate("T15_00", "");
//							insertPayload.accumulate("T15_30", "");
//							insertPayload.accumulate("T16_00", "");
//							insertPayload.accumulate("T16_30", "");
//							insertPayload.accumulate("T17_00", "");
//							insertPayload.accumulate("T17_30", "");
//							insertPayload.accumulate("T18_00", "");
//							insertPayload.accumulate("T18_30", "");
//							insertPayload.accumulate("T19_00", "");
//							insertPayload.accumulate("T19_30", "");
//							insertPayload.accumulate("T20_00", "");
//							insertPayload.accumulate("T20_30", "");
//							insertPayload.accumulate("T21_00", "");
//							insertPayload.accumulate("T21_30", "");
							

							if(childObjPayload.has("Input") && !childObjPayload.isNull("Input"))
								insertPayload.accumulate("Input", childObjPayload.getString("Input"));	
							else
								insertPayload.accumulate("Input", "");
							
							if(childObjPayload.has("TargetURL") && !childObjPayload.isNull("TargetURL"))
								insertPayload.accumulate("TargetURL", childObjPayload.getString("TargetURL"));	
							else
								insertPayload.accumulate("TargetURL", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							misSchedulerPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
							if(debug)
								response.getWriter().println("insertMISScheduler.misSchedulerPOSTResponse: "+misSchedulerPOSTResponse);
							
							if (misSchedulerPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertMISSchedulerArray.add(misSchedulerPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertMISSchedulerMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertMISSchedulerMasterJson.addProperty("ErrorCode", errorCode);
				insertMISSchedulerMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertMISSchedulerMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertMISSchedulerMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertMISSchedulerMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if ( ! insertMISSchedulerMasterJson.has("d")){
					insertMISSchedulerJson.add("results", insertMISSchedulerArray);
					insertMISSchedulerMasterJson.add("d", insertMISSchedulerJson);
				}
				insertMISSchedulerMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertMISSchedulerMasterJson = new JsonObject();
			insertMISSchedulerMasterJson.addProperty("ErrorCode", "E000");
			insertMISSchedulerMasterJson.addProperty("ResponseCode", "000003");
			insertMISSchedulerMasterJson.addProperty("Message", buffer.toString());
		}
		return insertMISSchedulerMasterJson;
	}
	
	//added by kamlesh for request fields validation 12-06-2020
	public JsonObject validateMISSchedulerInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String errorMsg="", errorCOde="";
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			for (int i = 0; i < inputArrayPayload.length(); i++) {
				
				inputPayload = inputArrayPayload.getJSONObject(i);
				if (inputPayload.isNull("AggregatorID") || inputPayload.getString("AggregatorID").trim().length() == 0)
					errorMsg = "AggregatorID";
				
				if ( errorMsg.trim().length() > 0) {
					errorCOde = "E001";
					errorResponse.addProperty("ErrorCode", errorCOde);
					errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
					break;
				}
				else
					errorMsg="";
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}

	public JsonObject updateMISScheduler(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",changedBy="",changedAt="", guid="";
		JsonObject UpdateMISSchedulerResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		long changedOnInMillis=0;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject getMisSchedulerJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{
			
			if (inputPayload.has("MISScheduler")) {
				jsonArrayPayload = inputPayload.getJSONArray("MISScheduler");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			JsonObject validateErrorJsonResponse = validateMISSchedulerInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("updateMISScheduler.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				changedBy = commonUtils.getUserPrincipal(request, "name", response);
				changedOnInMillis = commonUtils.getCreatedOnDate();
				changedAt = commonUtils.getCreatedAtTime();
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					executeURL =""; requestAggrID="";guid="";
					updatePayLoad = new JSONObject();
					UpdateMISSchedulerResponse = new JsonObject();
					getMisSchedulerJson = new JsonObject();
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("updateMISScheduler.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AggregatorID");
					guid = childObjPayload.getString("ID");
					if(debug){
						response.getWriter().println("updateMISScheduler.guid: "+guid);
						response.getWriter().println("updateMISScheduler.requestAggrID: "+requestAggrID);
					}
					
					// guid = childObjPayload.getString("ID");
					executeURL = oDataUrl+"MISScheduler?$filter=ID%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("updateMISScheduler.executeURL: "+executeURL);
					
					// getMisSchedulerJson = commonUtils.executeURL(executeURL, userPass, response);
					getMisSchedulerJson = commonUtils.doGetForPut(request, response, guid,DestinationUtils.PCGWHANA, "MISScheduler", debug);
					if(debug)
						response.getWriter().println("updateMISScheduler.getMisSchedulerJson: "+getMisSchedulerJson);
					
					if(getMisSchedulerJson.get("Status").getAsString().trim().equalsIgnoreCase("000001")){
						JsonObject finalGet = getMisSchedulerJson.getAsJsonObject("d");
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							
							// JsonObject childJson = misSchedulerJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
							
							updatePayLoad.accumulate("ChangedBy", changedBy);
							updatePayLoad.accumulate("ChangedAt", changedAt);
							updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");

							updatePayLoad.accumulate("ID", guid);
							updatePayLoad.accumulate("AggregatorID", requestAggrID);
							
							if(childObjPayload.has("Report") && ! childObjPayload.isNull("Report"))
								updatePayLoad.accumulate("Report", childObjPayload.getString("Report"));
							else{
								if(! finalGet.get("Report").isJsonNull())
									updatePayLoad.accumulate("Report", finalGet.get("Report").getAsString());
								else
									updatePayLoad.accumulate("Report", "");
							}
								
							
							if(childObjPayload.has("VariantID") && ! childObjPayload.isNull("VariantID"))
								updatePayLoad.accumulate("VariantID", childObjPayload.getString("VariantID"));
							else{
								if(! finalGet.get("VariantID").isJsonNull())
									updatePayLoad.accumulate("VariantID", finalGet.get("VariantID").getAsString());
								else
									updatePayLoad.accumulate("VariantID", "");
							}
							
							if(childObjPayload.has("IsActive") && !childObjPayload.isNull("IsActive"))
								updatePayLoad.accumulate("IsActive", childObjPayload.getString("IsActive"));	
							else{
								if(! finalGet.get("IsActive").isJsonNull())
									updatePayLoad.accumulate("IsActive", finalGet.get("IsActive").getAsString());
								else
									updatePayLoad.accumulate("IsActive", "");
							}
							
							if(childObjPayload.has("Periodicity") && !childObjPayload.isNull("Periodicity"))
								updatePayLoad.accumulate("Periodicity", childObjPayload.getString("Periodicity"));	
							else{
								if(! finalGet.get("Periodicity").isJsonNull())
									updatePayLoad.accumulate("Periodicity", finalGet.get("Periodicity").getAsString());
								else
									updatePayLoad.accumulate("Periodicity", "");
							}
							
							if(childObjPayload.has("QueueName") && !childObjPayload.isNull("QueueName"))
								updatePayLoad.accumulate("QueueName", childObjPayload.getString("QueueName"));	
							else{
								if(! finalGet.get("QueueName").isJsonNull())
									updatePayLoad.accumulate("QueueName", finalGet.get("QueueName").getAsString());
								else
									updatePayLoad.accumulate("QueueName", "");
							}
							
							if(childObjPayload.has("Monday") && !childObjPayload.isNull("Monday"))
								updatePayLoad.accumulate("Monday", childObjPayload.getString("Monday"));	
							else{
								if(! finalGet.get("Monday").isJsonNull())
									updatePayLoad.accumulate("Monday", finalGet.get("Monday").getAsString());
								else
									updatePayLoad.accumulate("Monday", "");
							}
							
							if(childObjPayload.has("Tuesday") && !childObjPayload.isNull("Tuesday"))
								updatePayLoad.accumulate("Tuesday", childObjPayload.getString("Tuesday"));	
							else{
								if(! finalGet.get("Tuesday").isJsonNull())
									updatePayLoad.accumulate("Tuesday", finalGet.get("Tuesday").getAsString());
								else
									updatePayLoad.accumulate("Tuesday", "");
							}
							
							if(childObjPayload.has("Wednesday") && !childObjPayload.isNull("Wednesday"))
								updatePayLoad.accumulate("Wednesday", childObjPayload.getString("Wednesday"));	
							else{
								if(! finalGet.get("Wednesday").isJsonNull())
									updatePayLoad.accumulate("Wednesday", finalGet.get("Wednesday").getAsString());
								else
									updatePayLoad.accumulate("Wednesday", "");
							}
							
							if(childObjPayload.has("Thursday") && !childObjPayload.isNull("Thursday"))
								updatePayLoad.accumulate("Thursday", childObjPayload.getString("Thursday"));	
							else{
								if(! finalGet.get("Thursday").isJsonNull())
									updatePayLoad.accumulate("Thursday", finalGet.get("Thursday").getAsString());
								else
									updatePayLoad.accumulate("Thursday", "");
							}
							
							if(childObjPayload.has("Friday") && !childObjPayload.isNull("Friday"))
								updatePayLoad.accumulate("Friday", childObjPayload.getString("Friday"));	
							else{
								if(! finalGet.get("Friday").isJsonNull())
									updatePayLoad.accumulate("Friday", finalGet.get("Friday").getAsString());
								else
									updatePayLoad.accumulate("Friday", "");
							}
							
							if(childObjPayload.has("Saturday") && !childObjPayload.isNull("Saturday"))
								updatePayLoad.accumulate("Saturday", childObjPayload.getString("Saturday"));	
							else{
								if(!finalGet.get("Saturday").isJsonNull())
									updatePayLoad.accumulate("Saturday", finalGet.get("Saturday").getAsString());
								else
									updatePayLoad.accumulate("Saturday", "");
							}
							
							if(childObjPayload.has("Sunday") && !childObjPayload.isNull("Sunday"))
								updatePayLoad.accumulate("Sunday", childObjPayload.getString("Sunday"));	
							else{
								if(! finalGet.get("Sunday").isJsonNull())
									updatePayLoad.accumulate("Sunday", finalGet.get("Sunday").getAsString());
								else
									updatePayLoad.accumulate("Sunday", "");
							}
							
							if(childObjPayload.has("T00_00") && !childObjPayload.isNull("T00_00"))
								updatePayLoad.accumulate("T00_00", childObjPayload.getString("T00_00"));	
							else{
								if(! finalGet.get("T00_00").isJsonNull())
									updatePayLoad.accumulate("T00_00", finalGet.get("T00_00").getAsString());
								else
									updatePayLoad.accumulate("T00_00", "");
							}
							
							if(childObjPayload.has("T00_30") && !childObjPayload.isNull("T00_30"))
								updatePayLoad.accumulate("T00_30", childObjPayload.getString("T00_30"));	
							else{
								if(! finalGet.get("T00_30").isJsonNull())
									updatePayLoad.accumulate("T00_30", finalGet.get("T00_30").getAsString());
								else
									updatePayLoad.accumulate("T00_30", "");
							}

							//Start Here
							if(childObjPayload.has("T01_00") && !childObjPayload.isNull("T01_00"))
								updatePayLoad.accumulate("T01_00", childObjPayload.getString("T01_00"));	
							else{
								if(! finalGet.get("T01_00").isJsonNull())
									updatePayLoad.accumulate("T01_00", finalGet.get("T01_00").getAsString());
								else
									updatePayLoad.accumulate("T01_00", "");
							}

							if(childObjPayload.has("T01_30") && !childObjPayload.isNull("T01_30"))
								updatePayLoad.accumulate("T01_30", childObjPayload.getString("T01_30"));	
							else{
								if(! finalGet.get("T01_30").isJsonNull())
									updatePayLoad.accumulate("T01_30", finalGet.get("T01_30").getAsString());
								else
									updatePayLoad.accumulate("T01_30", "");
							}

							if(childObjPayload.has("T02_00") && !childObjPayload.isNull("T02_00"))
								updatePayLoad.accumulate("T02_00", childObjPayload.getString("T02_00"));	
							else{
								if(! finalGet.get("T02_00").isJsonNull())
									updatePayLoad.accumulate("T02_00", finalGet.get("T02_00").getAsString());
								else
									updatePayLoad.accumulate("T02_00", "");
							}

							if(childObjPayload.has("T02_30") && !childObjPayload.isNull("T02_30"))
								updatePayLoad.accumulate("T02_30", childObjPayload.getString("T02_30"));	
							else{
								if(! finalGet.get("T02_30").isJsonNull())
									updatePayLoad.accumulate("T02_30", finalGet.get("T02_30").getAsString());
								else
									updatePayLoad.accumulate("T02_30", "");
							}

							if(childObjPayload.has("T03_00") && !childObjPayload.isNull("T03_00"))
								updatePayLoad.accumulate("T03_00", childObjPayload.getString("T03_00"));	
							else{
								if(! finalGet.get("T03_00").isJsonNull())
									updatePayLoad.accumulate("T03_00", finalGet.get("T03_00").getAsString());
								else
									updatePayLoad.accumulate("T03_00", "");
							}

							if(childObjPayload.has("T03_30") && !childObjPayload.isNull("T03_30"))
								updatePayLoad.accumulate("T03_30", childObjPayload.getString("T03_30"));	
							else{
								if(! finalGet.get("T03_30").isJsonNull())
									updatePayLoad.accumulate("T03_30", finalGet.get("T03_30").getAsString());
								else
									updatePayLoad.accumulate("T03_30", "");
							}

							if(childObjPayload.has("T04_00") && !childObjPayload.isNull("T04_00"))
								updatePayLoad.accumulate("T04_00", childObjPayload.getString("T04_00"));	
							else{
								if(! finalGet.get("T04_00").isJsonNull())
									updatePayLoad.accumulate("T04_00", finalGet.get("T04_00").getAsString());
								else
									updatePayLoad.accumulate("T04_00", "");
							}

							if(childObjPayload.has("T04_30") && !childObjPayload.isNull("T04_30"))
								updatePayLoad.accumulate("T04_30", childObjPayload.getString("T04_30"));	
							else{
								if(! finalGet.get("T04_30").isJsonNull())
									updatePayLoad.accumulate("T04_30", finalGet.get("T04_30").getAsString());
								else
									updatePayLoad.accumulate("T04_30", "");
							}

							if(childObjPayload.has("T05_00") && !childObjPayload.isNull("T05_00"))
								updatePayLoad.accumulate("T05_00", childObjPayload.getString("T05_00"));	
							else{
								if(! finalGet.get("T05_00").isJsonNull())
									updatePayLoad.accumulate("T05_00", finalGet.get("T05_00").getAsString());
								else
									updatePayLoad.accumulate("T05_00", "");
							}

							if(childObjPayload.has("T05_30") && !childObjPayload.isNull("T05_30"))
								updatePayLoad.accumulate("T05_30", childObjPayload.getString("T05_30"));	
							else{
								if(! finalGet.get("T05_30").isJsonNull())
									updatePayLoad.accumulate("T05_30", finalGet.get("T05_30").getAsString());
								else
									updatePayLoad.accumulate("T05_30", "");
							}

							if(childObjPayload.has("T06_00") && !childObjPayload.isNull("T06_00"))
								updatePayLoad.accumulate("T06_00", childObjPayload.getString("T06_00"));	
							else{
								if(! finalGet.get("T06_00").isJsonNull())
									updatePayLoad.accumulate("T06_00", finalGet.get("T06_00").getAsString());
								else
									updatePayLoad.accumulate("T06_00", "");
							}

							if(childObjPayload.has("T06_30") && !childObjPayload.isNull("T06_30"))
								updatePayLoad.accumulate("T06_30", childObjPayload.getString("T06_30"));	
							else{
								if(! finalGet.get("T06_30").isJsonNull())
									updatePayLoad.accumulate("T06_30", finalGet.get("T06_30").getAsString());
								else
									updatePayLoad.accumulate("T06_30", "");
							}

							if(childObjPayload.has("T07_00") && !childObjPayload.isNull("T07_00"))
								updatePayLoad.accumulate("T07_00", childObjPayload.getString("T07_00"));	
							else{
								if(! finalGet.get("T07_00").isJsonNull())
									updatePayLoad.accumulate("T07_00", finalGet.get("T07_00").getAsString());
								else
									updatePayLoad.accumulate("T07_00", "");
							}

							if(childObjPayload.has("T07_30") && !childObjPayload.isNull("T07_30"))
								updatePayLoad.accumulate("T07_30", childObjPayload.getString("T07_30"));	
							else{
								if(! finalGet.get("T07_30").isJsonNull())
									updatePayLoad.accumulate("T07_30", finalGet.get("T07_30").getAsString());
								else
									updatePayLoad.accumulate("T07_30", "");
							}

							if(childObjPayload.has("T08_00") && !childObjPayload.isNull("T08_00"))
								updatePayLoad.accumulate("T08_00", childObjPayload.getString("T08_00"));	
							else{
								if(! finalGet.get("T08_00").isJsonNull())
									updatePayLoad.accumulate("T08_00", finalGet.get("T08_00").getAsString());
								else
									updatePayLoad.accumulate("T08_00", "");
							}

							if(childObjPayload.has("T08_30") && !childObjPayload.isNull("T08_30"))
								updatePayLoad.accumulate("T08_30", childObjPayload.getString("T08_30"));	
							else{
								if(! finalGet.get("T08_30").isJsonNull())
									updatePayLoad.accumulate("T08_30", finalGet.get("T08_30").getAsString());
								else
									updatePayLoad.accumulate("T08_30", "");
							}

							if(childObjPayload.has("T09_00") && !childObjPayload.isNull("T09_00"))
								updatePayLoad.accumulate("T09_00", childObjPayload.getString("T09_00"));	
							else{
								if(! finalGet.get("T09_00").isJsonNull())
									updatePayLoad.accumulate("T09_00", finalGet.get("T09_00").getAsString());
								else
									updatePayLoad.accumulate("T09_00", "");
							}

							if(childObjPayload.has("T09_30") && !childObjPayload.isNull("T09_30"))
								updatePayLoad.accumulate("T09_30", childObjPayload.getString("T09_30"));	
							else{
								if(! finalGet.get("T09_30").isJsonNull())
									updatePayLoad.accumulate("T09_30", finalGet.get("T09_30").getAsString());
								else
									updatePayLoad.accumulate("T09_30", "");
							}

							if(childObjPayload.has("T10_00") && !childObjPayload.isNull("T10_00"))
								updatePayLoad.accumulate("T10_00", childObjPayload.getString("T10_00"));	
							else{
								if(! finalGet.get("T10_00").isJsonNull())
									updatePayLoad.accumulate("T10_00", finalGet.get("T10_00").getAsString());
								else
									updatePayLoad.accumulate("T10_00", "");
							}

							if(childObjPayload.has("T10_30") && !childObjPayload.isNull("T10_30"))
								updatePayLoad.accumulate("T10_30", childObjPayload.getString("T10_30"));	
							else{
								if(! finalGet.get("T10_30").isJsonNull())
									updatePayLoad.accumulate("T10_30", finalGet.get("T10_30").getAsString());
								else
									updatePayLoad.accumulate("T10_30", "");
							}

							if(childObjPayload.has("T11_00") && !childObjPayload.isNull("T11_00"))
								updatePayLoad.accumulate("T11_00", childObjPayload.getString("T11_00"));	
							else{
								if(! finalGet.get("T11_00").isJsonNull())
									updatePayLoad.accumulate("T11_00", finalGet.get("T11_00").getAsString());
								else
									updatePayLoad.accumulate("T11_00", "");
							}

							if(childObjPayload.has("T11_30") && !childObjPayload.isNull("T11_30"))
								updatePayLoad.accumulate("T11_30", childObjPayload.getString("T11_30"));	
							else{
								if(! finalGet.get("T11_30").isJsonNull())
									updatePayLoad.accumulate("T11_30", finalGet.get("T11_30").getAsString());
								else
									updatePayLoad.accumulate("T11_30", "");
							}

							if(childObjPayload.has("T12_00") && !childObjPayload.isNull("T12_00"))
								updatePayLoad.accumulate("T12_00", childObjPayload.getString("T12_00"));	
							else{
								if(! finalGet.get("T12_00").isJsonNull())
									updatePayLoad.accumulate("T12_00", finalGet.get("T12_00").getAsString());
								else
									updatePayLoad.accumulate("T12_00", "");
							}

							if(childObjPayload.has("T12_30") && !childObjPayload.isNull("T12_30"))
								updatePayLoad.accumulate("T12_30", childObjPayload.getString("T12_30"));	
							else{
								if(! finalGet.get("T12_30").isJsonNull())
									updatePayLoad.accumulate("T12_30", finalGet.get("T12_30").getAsString());
								else
									updatePayLoad.accumulate("T12_30", "");
							}

							if(childObjPayload.has("T13_00") && !childObjPayload.isNull("T13_00"))
								updatePayLoad.accumulate("T13_00", childObjPayload.getString("T13_00"));	
							else{
								if(! finalGet.get("T13_00").isJsonNull())
									updatePayLoad.accumulate("T13_00", finalGet.get("T13_00").getAsString());
								else
									updatePayLoad.accumulate("T13_00", "");
							}

							if(childObjPayload.has("T13_30") && !childObjPayload.isNull("T13_30"))
								updatePayLoad.accumulate("T13_30", childObjPayload.getString("T13_30"));	
							else{
								if(! finalGet.get("T13_30").isJsonNull())
									updatePayLoad.accumulate("T13_30", finalGet.get("T13_30").getAsString());
								else
									updatePayLoad.accumulate("T13_30", "");
							}

							if(childObjPayload.has("T14_00") && !childObjPayload.isNull("T14_00"))
								updatePayLoad.accumulate("T14_00", childObjPayload.getString("T14_00"));	
							else{
								if(! finalGet.get("T14_00").isJsonNull())
									updatePayLoad.accumulate("T14_00", finalGet.get("T14_00").getAsString());
								else
									updatePayLoad.accumulate("T14_00", "");
							}

							if(childObjPayload.has("T14_30") && !childObjPayload.isNull("T14_30"))
								updatePayLoad.accumulate("T14_30", childObjPayload.getString("T14_30"));	
							else{
								if(! finalGet.get("T14_30").isJsonNull())
									updatePayLoad.accumulate("T14_30", finalGet.get("T14_30").getAsString());
								else
									updatePayLoad.accumulate("T14_30", "");
							}

							if(childObjPayload.has("T15_00") && !childObjPayload.isNull("T00T15_00_30"))
								updatePayLoad.accumulate("T15_00", childObjPayload.getString("T15_00"));	
							else{
								if(! finalGet.get("T15_00").isJsonNull())
									updatePayLoad.accumulate("T15_00", finalGet.get("T15_00").getAsString());
								else
									updatePayLoad.accumulate("T15_00", "");
							}

							if(childObjPayload.has("T15_30") && !childObjPayload.isNull("T15_30"))
								updatePayLoad.accumulate("T15_30", childObjPayload.getString("T15_30"));	
							else{
								if(! finalGet.get("T15_30").isJsonNull())
									updatePayLoad.accumulate("T15_30", finalGet.get("T15_30").getAsString());
								else
									updatePayLoad.accumulate("T15_30", "");
							}

							if(childObjPayload.has("T16_00") && !childObjPayload.isNull("T16_00"))
								updatePayLoad.accumulate("T16_00", childObjPayload.getString("T16_00"));	
							else{
								if(! finalGet.get("T16_00").isJsonNull())
									updatePayLoad.accumulate("T16_00", finalGet.get("T16_00").getAsString());
								else
									updatePayLoad.accumulate("T16_00", "");
							}

							if(childObjPayload.has("T16_30") && !childObjPayload.isNull("T16_30"))
								updatePayLoad.accumulate("T16_30", childObjPayload.getString("T16_30"));	
							else{
								if(! finalGet.get("T16_30").isJsonNull())
									updatePayLoad.accumulate("T16_30", finalGet.get("T16_30").getAsString());
								else
									updatePayLoad.accumulate("T16_30", "");
							}

							if(childObjPayload.has("T17_00") && !childObjPayload.isNull("T17_00"))
								updatePayLoad.accumulate("T17_00", childObjPayload.getString("T17_00"));	
							else{
								if(! finalGet.get("T17_00").isJsonNull())
									updatePayLoad.accumulate("T17_00", finalGet.get("T17_00").getAsString());
								else
									updatePayLoad.accumulate("T17_00", "");
							}

							if(childObjPayload.has("T17_30") && !childObjPayload.isNull("T17_30"))
								updatePayLoad.accumulate("T17_30", childObjPayload.getString("T17_30"));	
							else{
								if(! finalGet.get("T17_30").isJsonNull())
									updatePayLoad.accumulate("T17_30", finalGet.get("T17_30").getAsString());
								else
									updatePayLoad.accumulate("T17_30", "");
							}

							if(childObjPayload.has("T18_00") && !childObjPayload.isNull("T18_00"))
								updatePayLoad.accumulate("T18_00", childObjPayload.getString("T18_00"));	
							else{
								if(! finalGet.get("T18_00").isJsonNull())
									updatePayLoad.accumulate("T18_00", finalGet.get("T18_00").getAsString());
								else
									updatePayLoad.accumulate("T18_00", "");
							}

							if(childObjPayload.has("T18_30") && !childObjPayload.isNull("T18_30"))
								updatePayLoad.accumulate("T18_30", childObjPayload.getString("T18_30"));	
							else{
								if(! finalGet.get("T18_30").isJsonNull())
									updatePayLoad.accumulate("T18_30", finalGet.get("T18_30").getAsString());
								else
									updatePayLoad.accumulate("T18_30", "");
							}

							if(childObjPayload.has("T19_00") && !childObjPayload.isNull("T19_00"))
								updatePayLoad.accumulate("T19_00", childObjPayload.getString("T19_00"));	
							else{
								if(! finalGet.get("T19_00").isJsonNull())
									updatePayLoad.accumulate("T19_00", finalGet.get("T19_00").getAsString());
								else
									updatePayLoad.accumulate("T19_00", "");
							}

							if(childObjPayload.has("T19_30") && !childObjPayload.isNull("T19_30"))
								updatePayLoad.accumulate("T19_30", childObjPayload.getString("T19_30"));	
							else{
								if(! finalGet.get("T19_30").isJsonNull())
									updatePayLoad.accumulate("T19_30", finalGet.get("T19_30").getAsString());
								else
									updatePayLoad.accumulate("T19_30", "");
							}

							if(childObjPayload.has("T20_00") && !childObjPayload.isNull("T20_00"))
								updatePayLoad.accumulate("T20_00", childObjPayload.getString("T20_00"));	
							else{
								if(! finalGet.get("T20_00").isJsonNull())
									updatePayLoad.accumulate("T20_00", finalGet.get("T20_00").getAsString());
								else
									updatePayLoad.accumulate("T20_00", "");
							}

							if(childObjPayload.has("T20_30") && !childObjPayload.isNull("T20_30"))
								updatePayLoad.accumulate("T20_30", childObjPayload.getString("T20_30"));	
							else{
								if(! finalGet.get("T20_30").isJsonNull())
									updatePayLoad.accumulate("T20_30", finalGet.get("T20_30").getAsString());
								else
									updatePayLoad.accumulate("T20_30", "");
							}

							if(childObjPayload.has("T21_00") && !childObjPayload.isNull("T21_00"))
								updatePayLoad.accumulate("T21_00", childObjPayload.getString("T21_00"));	
							else{
								if(! finalGet.get("T21_00").isJsonNull())
									updatePayLoad.accumulate("T21_00", finalGet.get("T21_00").getAsString());
								else
									updatePayLoad.accumulate("T21_00", "");
							}

							if(childObjPayload.has("T21_30") && !childObjPayload.isNull("T21_30"))
								updatePayLoad.accumulate("T21_30", childObjPayload.getString("T21_30"));	
							else{
								if(! finalGet.get("T21_30").isJsonNull())
									updatePayLoad.accumulate("T21_30", finalGet.get("T21_30").getAsString());
								else
									updatePayLoad.accumulate("T21_30", "");
							}

							if(childObjPayload.has("T22_00") && !childObjPayload.isNull("T22_00"))
								updatePayLoad.accumulate("T22_00", childObjPayload.getString("T22_00"));	
							else{
								if(! finalGet.get("T22_00").isJsonNull())
									updatePayLoad.accumulate("T22_00", finalGet.get("T22_00").getAsString());
								else
									updatePayLoad.accumulate("T22_00", "");
							}

							if(childObjPayload.has("T22_30") && !childObjPayload.isNull("T22_30"))
								updatePayLoad.accumulate("T22_30", childObjPayload.getString("T22_30"));	
							else{
								if(! finalGet.get("T22_30").isJsonNull())
									updatePayLoad.accumulate("T22_30", finalGet.get("T22_30").getAsString());
								else
									updatePayLoad.accumulate("T22_30", "");
							}

							if(childObjPayload.has("T23_00") && !childObjPayload.isNull("T23_00"))
								updatePayLoad.accumulate("T23_00", childObjPayload.getString("T23_00"));	
							else{
								if(! finalGet.get("T23_00").isJsonNull())
									updatePayLoad.accumulate("T23_00", finalGet.get("T23_00").getAsString());
								else
									updatePayLoad.accumulate("T23_00", "");
							}

							if(childObjPayload.has("T23_30") && !childObjPayload.isNull("T23_30"))
								updatePayLoad.accumulate("T23_30", childObjPayload.getString("T23_30"));	
							else{
								if(! finalGet.get("T23_30").isJsonNull())
									updatePayLoad.accumulate("T23_30", finalGet.get("T23_30").getAsString());
								else
									updatePayLoad.accumulate("T23_30", "");
							}

							if(childObjPayload.has("Input") && !childObjPayload.isNull("Input"))
								updatePayLoad.accumulate("Input", childObjPayload.getString("Input"));	
							else{
								if(! finalGet.get("Input").isJsonNull())
									updatePayLoad.accumulate("Input", finalGet.get("Input").getAsString());
								else
									updatePayLoad.accumulate("Input", "");
							}
							
							if(childObjPayload.has("TargetURL") && !childObjPayload.isNull("TargetURL"))
								updatePayLoad.accumulate("TargetURL", childObjPayload.getString("TargetURL"));	
							else{
								if(! finalGet.get("TargetURL").isJsonNull())
									updatePayLoad.accumulate("TargetURL", finalGet.get("TargetURL").getAsString());
								else
									updatePayLoad.accumulate("TargetURL", "");
							}
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));
							else{
								if(! finalGet.get("Source").isJsonNull())
									updatePayLoad.accumulate("Source", finalGet.get("Source").getAsString());
								else
									updatePayLoad.accumulate("Source", "");
							}
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else{
								if(! finalGet.get("SourceReferenceID").isJsonNull())
									updatePayLoad.accumulate("SourceReferenceID", finalGet.get("SourceReferenceID").getAsString());
								else
									updatePayLoad.accumulate("SourceReferenceID", "");
							}

							if(! finalGet.get("CreatedBy").isJsonNull())
								updatePayLoad.accumulate("CreatedBy", finalGet.get("CreatedBy").getAsString());
							else
								updatePayLoad.accumulate("CreatedBy", "");
							
							if(! finalGet.get("CreatedAt").isJsonNull())
								updatePayLoad.accumulate("CreatedAt", finalGet.get("CreatedAt").getAsString());
							else
								updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							
							if(! finalGet.get("CreatedOn").isJsonNull())
								updatePayLoad.accumulate("CreatedOn", finalGet.get("CreatedOn").getAsString());
							else
								updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							
							JsonParser jsonParser = new JsonParser();
		    				JsonObject updGsonObject = (JsonObject)jsonParser.parse(updatePayLoad.toString());
							if(debug)
								response.getWriter().println("updGsonObject: "+updGsonObject);

							JsonObject putPayload = new JsonObject();
							JSONObject finalPutPayload = new JSONObject();

							putPayload = commonUtils.createPutPayload(request, response, getMisSchedulerJson, updGsonObject, debug);
							if(debug)
								response.getWriter().println("putPayload: "+putPayload);
							
							finalPutPayload = new JSONObject(putPayload.toString());

							executeURL = oDataUrl+"MISScheduler('"+guid+"')";
							if(debug)
								response.getWriter().println("updateMISScheduler.executeURL: "+executeURL);
							
							UpdateMISSchedulerResponse = commonUtils.executeUpdate(executeURL, userPass, response, finalPutPayload, request, debug, "PCGWHANA");
							
							if(debug)
								response.getWriter().println("updateMISScheduler.UpdateMISSchedulerResponse: "+UpdateMISSchedulerResponse);
							
							if (UpdateMISSchedulerResponse.has("error")|| UpdateMISSchedulerResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}
						} else {
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E004";
						isValidationFailed = true;
						break;
					}
				}
			}
			else{
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				isValidationFailed = true;
			}
			if ( isValidationFailed) {
				UpdateMISSchedulerResponse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				UpdateMISSchedulerResponse.addProperty("ErrorCode", errorCode);
				UpdateMISSchedulerResponse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					UpdateMISSchedulerResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					UpdateMISSchedulerResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					UpdateMISSchedulerResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			else{
				UpdateMISSchedulerResponse.addProperty("Message", "Success");
				UpdateMISSchedulerResponse.addProperty("ErrorCode", "");
				UpdateMISSchedulerResponse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			UpdateMISSchedulerResponse = new JsonObject();
			UpdateMISSchedulerResponse.addProperty("ErrorCode", "E000");
			UpdateMISSchedulerResponse.addProperty("ResponseCode", "000003");
			UpdateMISSchedulerResponse.addProperty("Message", buffer.toString());
		}
		return UpdateMISSchedulerResponse;	
	}

	public JsonObject deleteMISScheduler(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",guid="", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		
		JsonObject misSchedulerDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("GUID"))
				guid = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deleteMISScheduler.pathInfo: "+pathInfo);
				response.getWriter().println("deleteMISScheduler.guid: "+guid);
			}
			
			if (! guid.equalsIgnoreCase("")) {
				executeURL = oDataURL+"MISScheduler?$filter=ID%20eq%20%27"+guid+"%27";
				if(debug)
					response.getWriter().println("deleteMISScheduler.executeURL: "+executeURL);
				
				JsonObject misSchedulerJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deleteMISScheduler.misSchedulerJson: "+misSchedulerJson);
				if (misSchedulerJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deleteMISScheduler.executeURL: "+executeURL);
						
						misSchedulerDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PCGWHANA");
						if(debug)
							response.getWriter().println("deleteMISScheduler.misSchedulerDeleteResponse: "+misSchedulerDeleteResponse);
						
						if(! misSchedulerDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! misSchedulerDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode", misSchedulerDeleteResponse.get("ErrorCode").getAsString());
							errorResonse.addProperty("Message", misSchedulerDeleteResponse.get("ErrorMessage").getAsString());
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "");
//						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}

	//TODO: added by kamlesh on 16-06-2020 for DCCNFG
	public JsonObject insertDCCNFG(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="", successResonse="",requestAggrID="",createdBy="", createdAt="", ID="";
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		CommonUtils commonUtils = new CommonUtils();
		long createdOnInMillis = 0;
		
		JsonArray insertDCCNFGArray = new JsonArray();
		JsonObject insertDCCNFGJson = new JsonObject();
		JsonObject insertDCCNFGMasterJson = new JsonObject();
		
		JsonObject validateErrorJsonResponse = new JsonObject();
		JSONObject childObjPayload = new JSONObject();
		JSONObject insertPayload = null;
		JSONArray jsonArrayPayload = new JSONArray();
		
		JsonObject DCCNFGPOSTResponse = null;
		JsonObject DCCNFGJson =null;
		successResonse = "{\"d\":{\"__metadata\": {\"type\":\"ARTEC.PYGW.service.DCCNFGType\","
				+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PYGW/service.xsodata/DCCNFG('')\"},\"ID\":\"\",\"AGGRID\":\"\","
				+ "\"OWN_BANK_COUNTRY\":\"\",\"OWN_BANK_KEY\":\"\",\"OWN_BANK_ACCNTNO\":\"\",\"OWN_BANK_ACCNTNM\":\"\",\"CreatedBy\":null,\"CreatedAt\":null,"
				+ "\"CreatedOn\":null,\"ChangedBy\":null,\"ChangedAt\":null,\"ChangedOn\":null,\"Source\":null,"
				+ "\"SourceReferenceID\":null,\"TestRun\":\"X\"}}";
		try{
			
			if (inputPayload.has("DCCNFG")) {
				jsonArrayPayload = inputPayload.getJSONArray("DCCNFG");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			validateErrorJsonResponse = validateDCCNFGInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("insertDCCNFG.validateMISSchedulerInsert: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				if(debug){
					response.getWriter().println("insertDCCNFG - createdBy: "+createdBy);	
					response.getWriter().println("insertDCCNFG - createdAt: "+createdAt);	
					response.getWriter().println("insertDCCNFG - createdOnInMillis: "+createdOnInMillis);	
				}
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					insertPayload = new JSONObject();
					DCCNFGPOSTResponse = new JsonObject();
					DCCNFGJson = new JsonObject();
					executeURL =""; requestAggrID=""; ID=""; 
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("insertDCCNFG.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AGGRID");
					ID = childObjPayload.getString("ID");
					
					executeURL = oDataUrl+"DCCNFG?$filter=AGGRID%20eq%20%27"+requestAggrID+"%27%20and%20ID%20eq%20%27"+ID+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("insertDCCNFG.executeURL: "+executeURL);
					
					DCCNFGJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("insertDCCNFG.DCCNFGJson: "+DCCNFGJson);
					
					if (DCCNFGJson.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							executeURL = oDataUrl+"DCCNFG";
							
							ID = commonUtils.generateGUID(36);
							insertPayload.accumulate("ID", ID);
							insertPayload.accumulate("AGGRID", requestAggrID);
							
							insertPayload.accumulate("CreatedBy", createdBy);
							insertPayload.accumulate("CreatedAt", createdAt);
							insertPayload.accumulate("CreatedOn", "/Date("+createdOnInMillis+")/");
							
							if(childObjPayload.has("OWN_BANK_COUNTRY") && ! childObjPayload.isNull("OWN_BANK_COUNTRY"))
								insertPayload.accumulate("OWN_BANK_COUNTRY", childObjPayload.getString("OWN_BANK_COUNTRY"));
							else
								insertPayload.accumulate("OWN_BANK_COUNTRY", "");
							
							if(childObjPayload.has("OWN_BANK_KEY") && ! childObjPayload.isNull("OWN_BANK_KEY"))
								insertPayload.accumulate("OWN_BANK_KEY", childObjPayload.getString("OWN_BANK_KEY"));
							else
								insertPayload.accumulate("OWN_BANK_KEY", "");
							
							if(childObjPayload.has("OWN_BANK_ACCNTNO") && !childObjPayload.isNull("OWN_BANK_ACCNTNO"))
								insertPayload.accumulate("OWN_BANK_ACCNTNO", childObjPayload.getString("OWN_BANK_ACCNTNO"));	
							else
								insertPayload.accumulate("OWN_BANK_ACCNTNO", "");
							
							if(childObjPayload.has("OWN_BANK_ACCNTNM") && !childObjPayload.isNull("OWN_BANK_ACCNTNM"))
								insertPayload.accumulate("OWN_BANK_ACCNTNM", childObjPayload.getString("OWN_BANK_ACCNTNM"));	
							else
								insertPayload.accumulate("OWN_BANK_ACCNTNM", "");
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								insertPayload.accumulate("Source", childObjPayload.getString("Source"));	
							else
								insertPayload.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								insertPayload.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								insertPayload.accumulate("SourceReferenceID", "");
							
							DCCNFGPOSTResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PYGWHANA");
							if(debug)
								response.getWriter().println("insertDCCNFG.DCCNFGPOSTResponse: "+DCCNFGPOSTResponse);
							
							if (DCCNFGPOSTResponse.has("error")) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}else{
								insertDCCNFGArray.add(DCCNFGPOSTResponse.getAsJsonObject("d"));
							}
						} else {
							Gson gson = new Gson();
							insertDCCNFGMasterJson = gson.fromJson(successResonse, JsonObject.class);
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E002";
						isValidationFailed = true;
						break;
					}
				}
			} else {
				isValidationFailed = true;
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();
			}
			if (isValidationFailed)
			{
				insertDCCNFGMasterJson.addProperty("ErrorCode", errorCode);
				insertDCCNFGMasterJson.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					insertDCCNFGMasterJson.addProperty("Message", properties.getProperty(errorCode).replace("&", "insert"));
				else if(errorCode.equalsIgnoreCase("E001") || errorCode.equalsIgnoreCase("E000"))
					insertDCCNFGMasterJson.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					insertDCCNFGMasterJson.addProperty("Message", properties.getProperty(errorCode));
			}else
			{
				if (! insertDCCNFGMasterJson.has("d")){
					insertDCCNFGJson.add("results", insertDCCNFGArray);
					insertDCCNFGMasterJson.add("d", insertDCCNFGJson);
				}
				insertDCCNFGMasterJson.getAsJsonObject("d").addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			insertDCCNFGMasterJson = new JsonObject();
			insertDCCNFGMasterJson.addProperty("ErrorCode", "E000");
			insertDCCNFGMasterJson.addProperty("ResponseCode", "000003");
			insertDCCNFGMasterJson.addProperty("Message", buffer.toString());
		}
		return insertDCCNFGMasterJson;
	}
	
	//added by kamlesh for request fields validation 12-06-2020
	public JsonObject validateDCCNFGInsert(HttpServletResponse response, JSONArray inputArrayPayload, String userPass, String oDataUrl, String aggregatorID, 
			Properties properties, boolean debug) throws IOException
	{
		String errorMsg="", errorCOde="";
		JSONObject inputPayload = new JSONObject();
		JsonObject errorResponse = new JsonObject();
		try {
			
			errorResponse.addProperty("ErrorCode", "");
			errorResponse.addProperty("ErrorMsg", "");
			
			for (int i = 0; i < inputArrayPayload.length(); i++) {
				
				errorMsg ="";
				inputPayload = inputArrayPayload.getJSONObject(i);
				if (inputPayload.isNull("AGGRID") || inputPayload.getString("AGGRID").trim().length() == 0)
					errorMsg = "AGGRID";
				
				if ( errorMsg.trim().length() > 0) {
					errorCOde = "E001";
					errorResponse.addProperty("ErrorCode", errorCOde);
					errorResponse.addProperty("ErrorMsg", properties.getProperty(errorCOde)+": "+ errorMsg);
					break;
				}
				else
					errorMsg="";
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorCOde = "E000";
			errorResponse = new JsonObject();
			errorResponse.addProperty("ErrorCode", errorCOde);
			errorResponse.addProperty("ErrorMsg", buffer.toString());
		}
		return errorResponse;
	}

	public JsonObject updateDCCNFG(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataUrl, String pathInfo, String aggregatorID, Properties properties, boolean debug) throws IOException
	{
		String executeURL="",requestAggrID="",changedBy="",changedAt="", guid="";
		JsonObject UpdateDCCNFGResponse = new JsonObject();
		String testRun="", responseCode="", errorCode="";
		boolean isValidationFailed = false;
		long changedOnInMillis=0;
		CommonUtils commonUtils = new CommonUtils();
		
		JSONObject updatePayLoad = new JSONObject();
		JsonObject DCCNFGJson = new JsonObject();
		JSONArray jsonArrayPayload = new JSONArray();
		JSONObject childObjPayload = new JSONObject();
		try{
			
			if (inputPayload.has("DCCNFG")) {
				jsonArrayPayload = inputPayload.getJSONArray("DCCNFG");
			} else {
				jsonArrayPayload.put(inputPayload);
			}
			
			if (inputPayload.has("TestRun") && ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			JsonObject validateErrorJsonResponse = validateDCCNFGInsert(response, jsonArrayPayload, userPass, oDataUrl, aggregatorID, properties, debug);
			if(debug)
				response.getWriter().println("updateDCCNFG.validateErrorJsonResponse: "+validateErrorJsonResponse);
			
			if (validateErrorJsonResponse.has("ErrorCode") && validateErrorJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				
				changedBy = commonUtils.getUserPrincipal(request, "name", response);
				changedOnInMillis = commonUtils.getCreatedOnDate();
				changedAt = commonUtils.getCreatedAtTime();
				
				for (int i = 0; i < jsonArrayPayload.length(); i++) {
					
					executeURL =""; requestAggrID="";guid="";
					updatePayLoad = new JSONObject();
					UpdateDCCNFGResponse = new JsonObject();
					DCCNFGJson = new JsonObject();
					
					childObjPayload = jsonArrayPayload.getJSONObject(i);
					if(debug)
						response.getWriter().println("updateDCCNFG.childObjPayload ("+i+"): "+childObjPayload);
					
					requestAggrID = childObjPayload.getString("AGGRID");
					guid = childObjPayload.getString("ID");
					if(debug){
						response.getWriter().println("updateDCCNFG.guid: "+guid);
						response.getWriter().println("updateDCCNFG.requestAggrID: "+requestAggrID);
					}

					executeURL = oDataUrl+"DCCNFG?$filter=ID%20eq%20%27"+guid+"%27";
					executeURL = executeURL.replace(" ", "%20");
					if(debug)
						response.getWriter().println("updateDCCNFG.executeURL: "+executeURL);
					
					DCCNFGJson = commonUtils.executeURL(executeURL, userPass, response);
					if(debug)
						response.getWriter().println("updateDCCNFG.DCCNFGJson: "+DCCNFGJson);
					
					if (DCCNFGJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
						
						if (testRun.equalsIgnoreCase("")) {
							executeURL = "";
							
							JsonObject childJson = DCCNFGJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
							
							updatePayLoad.accumulate("ChangedBy", changedBy);
							updatePayLoad.accumulate("ChangedAt", changedAt);
							updatePayLoad.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");

							updatePayLoad.accumulate("ID", guid);
							updatePayLoad.accumulate("AGGRID", requestAggrID);
							
							if(childObjPayload.has("OWN_BANK_COUNTRY") && ! childObjPayload.isNull("OWN_BANK_COUNTRY"))
								updatePayLoad.accumulate("OWN_BANK_COUNTRY", childObjPayload.getString("OWN_BANK_COUNTRY"));
							else
								updatePayLoad.accumulate("OWN_BANK_COUNTRY", "");
							
							if(childObjPayload.has("OWN_BANK_KEY") && ! childObjPayload.isNull("OWN_BANK_KEY"))
								updatePayLoad.accumulate("OWN_BANK_KEY", childObjPayload.getString("OWN_BANK_KEY"));
							else
								updatePayLoad.accumulate("OWN_BANK_KEY", "");
							
							if(childObjPayload.has("OWN_BANK_ACCNTNO") && ! childObjPayload.isNull("OWN_BANK_ACCNTNO"))
								updatePayLoad.accumulate("OWN_BANK_ACCNTNO", childObjPayload.getString("OWN_BANK_ACCNTNO"));
							else
								updatePayLoad.accumulate("OWN_BANK_ACCNTNO", "");
							
							if(childObjPayload.has("OWN_BANK_ACCNTNM") && ! childObjPayload.isNull("OWN_BANK_ACCNTNM"))
								updatePayLoad.accumulate("OWN_BANK_ACCNTNM", childObjPayload.getString("OWN_BANK_ACCNTNM"));
							else
								updatePayLoad.accumulate("OWN_BANK_ACCNTNM", "");
							
							if ( ! childJson.get("CreatedBy").isJsonNull())
								updatePayLoad.accumulate("CreatedBy", childJson.get("CreatedBy").getAsString());
							else
								updatePayLoad.accumulate("CreatedBy", "");
							
							if ( ! childJson.get("CreatedAt").isJsonNull())
								updatePayLoad.accumulate("CreatedAt", childJson.get("CreatedAt").getAsString());
							else
								updatePayLoad.accumulate("CreatedAt", JSONObject.NULL);
							
							if (  ! childJson.get("CreatedOn").isJsonNull())
								updatePayLoad.accumulate("CreatedOn", childJson.get("CreatedOn").getAsString());
							else
								updatePayLoad.accumulate("CreatedOn", JSONObject.NULL);
							
							if(childObjPayload.has("Source") && !childObjPayload.isNull("Source"))
								updatePayLoad.accumulate("Source", childObjPayload.getString("Source"));
							else
								updatePayLoad.accumulate("Source", "");
							
							if(childObjPayload.has("SourceReferenceID") && !childObjPayload.isNull("SourceReferenceID"))
								updatePayLoad.accumulate("SourceReferenceID", childObjPayload.getString("SourceReferenceID"));	
							else
								updatePayLoad.accumulate("SourceReferenceID", "");
							
							executeURL = oDataUrl+"DCCNFG('"+guid+"')";
							if(debug)
								response.getWriter().println("updateDCCNFG.executeURL: "+executeURL);
							
							UpdateDCCNFGResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayLoad, request, debug, "PYGWHANA");
							if(debug)
								response.getWriter().println("updateDCCNFG.UpdateDCCNFGResponse: "+UpdateDCCNFGResponse);
							
							if (UpdateDCCNFGResponse.has("error")|| UpdateDCCNFGResponse.get("ErrorCode").getAsString().trim().length() > 0) {
								responseCode ="000004";
								errorCode ="E003";
								isValidationFailed = true;
								break;
							}
						} else {
							break;
						}
					} else {
						responseCode ="000002";
						errorCode ="E004";
						isValidationFailed = true;
						break;
					}
				}
			}
			else{
				responseCode ="000002";
				errorCode = validateErrorJsonResponse.get("ErrorCode").getAsString();;
				isValidationFailed = true;
			}
			if ( isValidationFailed) {
				UpdateDCCNFGResponse = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				UpdateDCCNFGResponse.addProperty("ErrorCode", errorCode);
				UpdateDCCNFGResponse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					UpdateDCCNFGResponse.addProperty("Message", properties.getProperty(errorCode).replace("&", "update"));
				else if(errorCode.equalsIgnoreCase("E001")  || errorCode.equalsIgnoreCase("E000"))
					UpdateDCCNFGResponse.addProperty("Message", validateErrorJsonResponse.get("ErrorMsg").getAsString());
				else
					UpdateDCCNFGResponse.addProperty("Message", properties.getProperty(errorCode));
			}
			else{
				UpdateDCCNFGResponse.addProperty("Message", "Success");
				UpdateDCCNFGResponse.addProperty("ErrorCode", "");
				UpdateDCCNFGResponse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			UpdateDCCNFGResponse = new JsonObject();
			UpdateDCCNFGResponse.addProperty("ErrorCode", "E000");
			UpdateDCCNFGResponse.addProperty("ResponseCode", "000003");
			UpdateDCCNFGResponse.addProperty("Message", buffer.toString());
		}
		return UpdateDCCNFGResponse;	
	}

	public JsonObject deleteDCCNFG(HttpServletResponse response, HttpServletRequest request, JSONObject inputPayload, String userPass, String oDataURL, String pathInfo, 
			Properties properties, boolean debug) throws IOException
	{
		String executeURL="",guid="", testRun="";
		boolean isValidationFailed = false;
		String responseCode="", errorCode="";
		JsonObject errorResonse = new JsonObject();
		
		JsonObject DCCNFGDeleteResponse = new JsonObject();
		JsonObject getGuidResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (inputPayload.has("TestRun")&& ! inputPayload.isNull("TestRun"))
				testRun = inputPayload.getString("TestRun");
			
			getGuidResponse = getDeleteURLGuid(response, pathInfo, debug);
			
			if (getGuidResponse.has("GUID"))
				guid = getGuidResponse.get("GUID").getAsString();
			
			if(debug){
				response.getWriter().println("deleteDCCNFG.pathInfo: "+pathInfo);
				response.getWriter().println("deleteDCCNFG.guid: "+guid);
			}
			
			if (! guid.equalsIgnoreCase("")) {
				executeURL = oDataURL+"DCCNFG?$filter=ID%20eq%20%27"+guid+"%27";
				if(debug)
					response.getWriter().println("deleteDCCNFG.executeURL: "+executeURL);
				
				JsonObject DCCNFGJson = commonUtils.executeURL(executeURL, userPass, response);
				if(debug)
					response.getWriter().println("deleteDCCNFG.DCCNFGJson: "+DCCNFGJson);
				if (DCCNFGJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
				{
					if (testRun.equalsIgnoreCase("")) {
						//delete url
						executeURL ="";
						pathInfo = pathInfo.replace("/", "");
						executeURL = oDataURL+pathInfo;
						if(debug)
							response.getWriter().println("deleteDCCNFG.executeURL: "+executeURL);
						
						DCCNFGDeleteResponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PYGWHANA");
						if(debug)
							response.getWriter().println("deleteDCCNFG.DCCNFGDeleteResponse: "+DCCNFGDeleteResponse);
						
						if(! DCCNFGDeleteResponse.get("ErrorCode").isJsonNull() 
								&& ! DCCNFGDeleteResponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
							
							responseCode ="000004";
							errorCode ="E003";
							isValidationFailed = true;
						}else
						{
							errorResonse.addProperty("ErrorCode", DCCNFGDeleteResponse.get("ErrorCode").getAsString());
							errorResonse.addProperty("Message", DCCNFGDeleteResponse.get("ErrorMessage").getAsString());
						}
					} else {
						errorResonse.addProperty("ErrorCode", "");
						errorResonse.addProperty("Message", "");
//						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					responseCode ="000002";
					errorCode ="E004";
					isValidationFailed = true;
				}
			} else {
				responseCode ="000002";
				errorCode ="E001";
				isValidationFailed = true;
				
			}
			if ( isValidationFailed)
			{
				errorResonse = new JsonObject();
				errorResonse.addProperty("ErrorCode", errorCode);
				errorResonse.addProperty("ResponseCode", responseCode);
				if(errorCode.equalsIgnoreCase("E003"))
					errorResonse.addProperty("Message", properties.getProperty(errorCode).replace("&", "delete"));
				else
					errorResonse.addProperty("Message", properties.getProperty(errorCode));
				
			}else
			{
				errorResonse.addProperty("ResponseCode", "000001");
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			errorResonse = new JsonObject();
			errorResonse.addProperty("ErrorCode", "E000");
			errorResonse.addProperty("ResponseCode", "000003");
			errorResonse.addProperty("Message", buffer.toString());
		}
		return errorResonse;
	}
	
	public String getAccountNoFromDCCNFG(HttpServletRequest request, HttpServletResponse response, boolean debug) throws IOException{
		String oDataURL="",executeURL = "", userPass="", userName="", passWord="", aggregatorID="";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject httpJsonResult = new JsonObject();
		String accountNo = "";
		try{
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			passWord = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			userPass = userName+":"+passWord;
			
			executeURL = oDataURL+"DCCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
			if(debug)
				response.getWriter().println("executeURL: "+executeURL);
			httpJsonResult = commonUtils.executeURL(executeURL, userPass, response);
			if(debug)
				response.getWriter().println("httpJsonResult: "+httpJsonResult);
			
			JsonObject results = httpJsonResult.get("d").getAsJsonObject();
			if(debug)
				response.getWriter().println("AccountBalances-results: "+results);
			JsonArray dresults = results.get("results").getAsJsonArray();
			if(debug)
				response.getWriter().println("AccountBalances-dresults: "+dresults);
			
			if(debug)
				response.getWriter().println("AccountBalances-Size: "+dresults.size());
			JsonObject dccnfgJsonObj = new JsonObject();
			
			if(dresults.size() ==  0){
				accountNo="No Records Found";
			}else{
				for (int i = 0; i <= dresults.size() - 1; i++) {
					dccnfgJsonObj = (JsonObject) dresults.get(i);

					accountNo = dccnfgJsonObj.get("OWN_BANK_ACCNTNO").getAsString();
				}
			}
			return accountNo;
		}catch (Exception e) {
			accountNo="Error";
			return accountNo;
		}
	}
}
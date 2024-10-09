package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ATYP extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		boolean debug = false;
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		JSONObject tsetTObj = new JSONObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonPayload = (JsonObject) jsonParser.parse(inputPayload);
				if (jsonPayload.has("debug") && !jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonPayload.has("AGGRID") && !jsonPayload.get("AGGRID").isJsonNull()
						&& !jsonPayload.get("AGGRID").getAsString().equalsIgnoreCase("")) {
					if (jsonPayload.has("TYPESET") && !jsonPayload.get("TYPESET").isJsonNull()
							&& !jsonPayload.get("TYPESET").getAsString().equalsIgnoreCase("")) {

						if (jsonPayload.has("TYPES") && !jsonPayload.get("TYPES").isJsonNull()
								&& !jsonPayload.get("TYPES").getAsString().equalsIgnoreCase("")) {
							oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
							userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
							password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
							userPass = userName + ":" + password;
							executeURL = oDataUrl + "ATYP";
							tsetTObj.accumulate("AGGRID", jsonPayload.get("AGGRID").getAsString());
							tsetTObj.accumulate("TYPESET", jsonPayload.get("TYPESET").getAsString());
							tsetTObj.accumulate("TYPES", jsonPayload.get("TYPES").getAsString());
							if (debug) {
								response.getWriter().println(" TsetT Payload " + tsetTObj);
							}
							JsonObject executePostURL = commonUtils.executePostURL(executeURL, userPass, response,
									tsetTObj, request, debug, "PCGWHANA");
							if (debug) {
								response.getWriter().println(" TsetT Response " + executePostURL);
							}

							if (executePostURL.has("error")) {
								JsonObject retunObj = new JsonObject();
								retunObj.addProperty("Status", "000002");
								retunObj.addProperty("ErrorCode", "J002");
								retunObj.addProperty("Message", "Insertion Failed");
								response.getWriter().println(retunObj);
							} else {
								JsonObject retunObj = new JsonObject();
								retunObj.addProperty("Status", "000001");
								retunObj.addProperty("ErrorCode", "");
								retunObj.addProperty("Message", "Record Inserted Successfully");
								response.getWriter().println(retunObj);
							}
						} else {
							JsonObject validateFields = validateFields("TYPES");
							response.getWriter().println(validateFields);

						}
					} else {
						JsonObject validateFields = validateFields("TYPESET");
						response.getWriter().println(validateFields);

					}

				} else {
					JsonObject validateFields = validateFields("AGGRID");
					response.getWriter().println(validateFields);

				}

			} else {
				JsonObject validateFields = validateFields(null);
				response.getWriter().println(validateFields);

			}
		} catch (Exception ex) {
			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);
		}

	}

	public JsonObject validateFields(String field) {
		JsonObject retunObj = new JsonObject();
		if (field == null) {
			retunObj.addProperty("Message", " Input Paylaod is Empty ");
		} else {
			retunObj.addProperty("Message", field + " Is empty In the Input Payload ");
		}
		retunObj.addProperty("Status", "000002");
		retunObj.addProperty("ErrorCode", "J002");
		return retunObj;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String aggregatorID = "", typeset = "", oDataUrl = "", userName = "", password = "", userPass = "",
				executeURL = "";
		boolean debug = false;
		try {
			if (request.getParameter("debug") != null) {
				debug = true;
			}
			if(request.getParameter("AGGRID")!=null && !request.getParameter("AGGRID").equalsIgnoreCase("")){
				aggregatorID=request.getParameter("AGGRID");
			}
			if(request.getParameter("TYPESET")!=null && !request.getParameter("TYPESET").equalsIgnoreCase("")){
				typeset=request.getParameter("TYPESET");
			}
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName + ":" + password;
			if(aggregatorID!=null && !aggregatorID.equalsIgnoreCase("")&&typeset!=null && !typeset.equalsIgnoreCase("")){
				executeURL = oDataUrl + "ATYP?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27%20and%20TYPESET%20eq%20%27"+typeset+"%27";	
			}
			else if(aggregatorID!=null && !aggregatorID.equalsIgnoreCase("")){
				executeURL = oDataUrl + "ATYP?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
			}else if(typeset!=null && !typeset.equalsIgnoreCase("")){
				executeURL = oDataUrl + "ATYP?$filter=TYPESET%20eq%20%27"+typeset+"%27";
			}else{
				executeURL = oDataUrl + "ATYP";
			}
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName + ":" + password;
			
			if (debug) {
				response.getWriter().println("executeURL :" + executeURL);
			}
			JsonObject aggObj = commonUtils.executeURL(executeURL, userPass, response);
			if (debug) {
				response.getWriter().println("Response ATYP table " + aggObj);
			}

			if (aggObj != null && aggObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
				JsonArray asJsonArray = aggObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				response.getWriter().print(asJsonArray);
			} else {
				JsonObject retunObj = new JsonObject();
				retunObj.addProperty("Message", "No Record Exist");
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);
			}

		} catch (JsonParseException ex) {
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);

		} catch (Exception ex) {
			JsonObject exceptionMessage = getExceptionMessage(ex);
			response.getWriter().println(exceptionMessage);

		}

	}

	private JsonObject getExceptionMessage(Exception ex) {
		JsonObject retunObj = new JsonObject();
		StackTraceElement element[] = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < element.length; i++) {
			buffer.append(element[i]);
		}
		retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
		retunObj.addProperty("Message",
				ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
		retunObj.addProperty("Status", "000002");
		retunObj.addProperty("ErrorCode", "J002");
		return retunObj;

	}

}

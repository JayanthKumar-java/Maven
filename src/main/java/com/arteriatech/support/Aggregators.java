package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Aggregators extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String aggregatorID = "", typeset = "", oDataUrl = "", userName = "", password = "", userPass = "",
				executeURL = "";
		boolean debug = false;
		try {
			   if(request.getParameter("debug")!=null){
				   debug=true;
			   }
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;
				executeURL = oDataUrl + "Aggregators";
				if (debug) {
					response.getWriter().println("executeURL :" + executeURL);
				}
				JsonObject aggObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("Response from Aggregtor ID  " + aggObj);
				}

				if (aggObj != null && aggObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					 JsonArray asJsonArray = aggObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					response.getWriter().print(asJsonArray);
				} else {
					JsonObject retunObj = new JsonObject();
					retunObj.addProperty("Message",
							"No Record Exist for The Given AggregatorID " + aggregatorID + " and Typeset  " + typeset);
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

	public JsonObject getResponseMessage(String field) {
		JsonObject retunObj = new JsonObject();
		if (field == null) {
			retunObj.addProperty("Message", " Input Paylaod is Empty ");
		} else {
			retunObj.addProperty("Message", field + " is empty in the Input Payload ");
		}
		retunObj.addProperty("Status", "000002");
		retunObj.addProperty("ErrorCode", "J002");
		return retunObj;
	}

}

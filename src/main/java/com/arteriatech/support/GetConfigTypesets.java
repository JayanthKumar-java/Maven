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

public class GetConfigTypesets extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject configTypesetObj = new JsonObject();
		String aggregatorID = "", typeset = "", oDataUrl = "", userName = "", password = "", userPass = "",
				executeURL = "",typeSet="";
		boolean debug = false;
		try {
			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
	        	debug=true;
	        }
          if(request.getParameter("Typeset")!=null && !request.getParameter("Typeset").equalsIgnoreCase("")){
        	  typeSet=request.getParameter("Typeset");
          }
          
           oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName + ":" + password;
          
          if(request.getParameter("AggregatorId")!=null && !request.getParameter("AggregatorId").equals("")){
        	  aggregatorID=request.getParameter("AggregatorId");
          }
			if (debug) {
				response.getWriter().println("Recived AggregatorID " + aggregatorID);
				response.getWriter().println("Recived Typeset " + typeSet);
			}
			if(aggregatorID!=null && !aggregatorID.equalsIgnoreCase("")&& typeSet!=null && !typeSet.equalsIgnoreCase("") ){
				executeURL = oDataUrl+"ConfigTypesets?$filter=AggregatorID%20eq%20%27" + aggregatorID
						+ "%27%20and%20Typeset%20eq%20%27" + typeSet + "%27";
				
			}else if(aggregatorID!=null && !aggregatorID.equalsIgnoreCase("")){
				executeURL = oDataUrl + "ConfigTypesets?$filter=AggregatorID%20eq%20%27" + aggregatorID+"%27";
				
			}else if(typeSet!=null && !typeSet.equalsIgnoreCase("")){
				executeURL = oDataUrl + "ConfigTypesets?$filter=Typeset%20eq%20%27" + typeSet+"%27";
			}else{
				executeURL = oDataUrl + "ConfigTypesets";
			}
				
				if (debug) {
					response.getWriter().println("executeURL: " + executeURL);
				}
				configTypesetObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("ConfigTypesets Response :" + configTypesetObj);
				}

				if (configTypesetObj != null
						&& configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					JsonArray jsonArray = configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					response.getWriter().print(jsonArray);
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

}

package com.arteriatech.aml;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class DummyExtendedPreEligibleCheck extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* @Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
		/* JsonObject resObj=new JsonObject();
		Enumeration<E>
		
			String pathInfo = request.getPathInfo();
			if(pathInfo.equalsIgnoreCase("/AML00011")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00011");
				response.getWriter().println(resObj);
			}else{
				resObj.addProperty("Message", "Updated Successfully");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
				response.getWriter().println(resObj);
			} 
		}catch(Exception ex){

			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
		}
	} */
}

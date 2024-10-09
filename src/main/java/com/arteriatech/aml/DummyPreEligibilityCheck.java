package com.arteriatech.aml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class DummyPreEligibilityCheck extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		try{
			String pathInfo = request.getPathInfo();
			if(pathInfo!=null &&pathInfo.equalsIgnoreCase("/AML00001")){
				resObj.addProperty("Message", "BP Not Registered");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00001");
				response.getWriter().println(resObj);
			}else if(pathInfo!=null &&pathInfo.equalsIgnoreCase("/AML00002")){
				resObj.addProperty("Message", "No records found");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00002");
				response.getWriter().println(resObj);
			}else if(pathInfo!=null &&pathInfo.equalsIgnoreCase("/AMLS001")){
				resObj.addProperty("Message", "Sanctioning under progress");
				resObj.addProperty("Status", "000003");
				resObj.addProperty("ErrorCode", "AMLS001");
				response.getWriter().println(resObj);
			}else if(pathInfo!=null &&pathInfo.equalsIgnoreCase("/AML00003")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00003");
				response.getWriter().println(resObj);
			}else{
				resObj.addProperty("Message", "AML Eligible");
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
	}

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		try{
			String pathInfo = request.getPathInfo();
			if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00004")){
				resObj.addProperty("Message", "Registration Not Complete");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00004");
				response.getWriter().println(resObj);
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AMLS001")){
				resObj.addProperty("Message", "Sanctioning under progress");
				resObj.addProperty("Status", "000003");
				resObj.addProperty("ErrorCode", "AMLS001");
				response.getWriter().println(resObj);
				
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00003")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00003");
				response.getWriter().println(resObj);
				
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00004")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00004");
				response.getWriter().println(resObj);
				
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00005")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00004");
				response.getWriter().println(resObj);
				
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00006")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00006");
				response.getWriter().println(resObj);
				
			}else if(pathInfo!=null && pathInfo.equalsIgnoreCase("/AML00007")){
				resObj.addProperty("Message", "Currently Facing Technical issues. Please try after sometime");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00007");
				response.getWriter().println(resObj);
			}else{
				resObj.addProperty("Message", "AML Eligible");
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
	}


	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		try{
			String servletPath = request.getPathInfo();
			if(servletPath!=null && servletPath.equalsIgnoreCase("/AML00008")){
				resObj.addProperty("Message", "Configurations Not Found");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00008");
				response.getWriter().println(resObj);
			}else if(servletPath!=null && servletPath.equalsIgnoreCase("/AML00009")){
				resObj.addProperty("Message", "Update Failed");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00009");
				response.getWriter().println(resObj);
			}else if(servletPath!=null && servletPath.equalsIgnoreCase("/AML00010")){
				resObj.addProperty("Message", "Unknown status");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "AML00010");
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
	}
	
	

}

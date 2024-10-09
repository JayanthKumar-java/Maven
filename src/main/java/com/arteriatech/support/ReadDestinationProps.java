package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;

public class ReadDestinationProps extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		String tenant="",userName="",password="";
		try{
			tenant = commonUtils.getODataDestinationProperties("Tenant","UploadDocs");
			userName = commonUtils.getODataDestinationProperties("User","UploadDocs");
			password = commonUtils.getODataDestinationProperties("Password","UploadDocs");
			resObj.addProperty("Tenant", tenant);
			resObj.addProperty("User", userName);
			resObj.addProperty("Password", password);
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Tenant", tenant);
			resObj.addProperty("User", userName);
			resObj.addProperty("Password", password);
			resObj.addProperty("Exception", buffer.toString());
		}finally{
			response.getWriter().println(resObj);
		}
	}
}
package com.arteriatech.support;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class GetResponse extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		try{

			TimeUnit.MINUTES.sleep(3);
			resObj.addProperty("Message", "Success");
			resObj.addProperty("ErrorCode", "");
			resObj.addProperty("Status", "000001");
			resp.getWriter().println(resObj);
		}catch(Exception ex){
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			resp.getWriter().println(resObj);
			
		}
	}
	
	
	

}

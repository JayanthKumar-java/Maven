package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;

public class InsertSCFRecord extends HttpServlet{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest requets, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		try{
			//commonUtils.getGetBody(request, response);
			
		}catch(Exception ex){
			
		}
	}
	
	

}

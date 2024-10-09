package com.arteriatech.support;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestIpAddress extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String header = headerNames.nextElement();
				response.getWriter().println(header + "------->" + request.getHeader(header));
			}

		}catch(Exception ex){
			response.getWriter().println(ex.getLocalizedMessage());
			response.getWriter().println(ex.getClass().getCanonicalName());
		}
	}
}
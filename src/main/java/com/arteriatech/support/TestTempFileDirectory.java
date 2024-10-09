package com.arteriatech.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

public class TestTempFileDirectory extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			File tmpDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
			File privFile = new File(tmpDir, "Invoice.xls");
			Date processEnd=new Date(System.currentTimeMillis());
			privFile.deleteOnExit();
		}catch(Exception ex){
			
		}
	}
}
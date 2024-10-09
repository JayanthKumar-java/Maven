package com.arteriatech.bankapis;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

/**
 * Servlet implementation class Dummy
 */
@WebServlet("/Dummy")
public class Dummy extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Dummy() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().println("GET Request Served at: "+request.getContextPath());
		String requestBody="", getResponse="", endPoint="", responseType="";
		JsonObject responseObj = new JsonObject();
		boolean debug = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		try{
			if(debug){
				requestBody = getGetBody(request, response);
				response.getWriter().println("requestBody: "+ requestBody);
				response.getWriter().println("endpoint: "+ request.getParameter("endpoint"));
				response.getWriter().println("responseType: "+ request.getParameter("responseType"));
			}
				
			if(null != request.getParameter("endpoint") && request.getParameter("endpoint").equalsIgnoreCase("Authorize")){
				if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Success")){
					getResponse = "{\"Status\":\"000001\",\"Message\":\"Success\",\"ErrorCode\":\"\",\"PF\":\"120\",\"Charges\":\"2000\",\"AdvEMIAmt\":\"13000\",\"TotalDownPymnt\":\"2312\",\"TransactionAmt\":\"250000\",\"Tenure\":\"10\",\"AdvEMITenure\":\"5\",\"FirstDueDate\":\"20-09-2021\",\"EmiPerMonth\":\"1300\",\"PGPaymentGUID\":\"12345678945867\",\"PGTransactionID\":\"35B4E44B-1A6C-4B9B-B29A-034085402828X02\"}";
				}else if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Failure")){
					getResponse = "{\"Status\":\"000002\",\"Message\":\"This is a dummy failure message\",\"ErrorCode\":\"E101010\",\"PF\":\"\",\"Charges\":\"\",\"AdvEMIAmt\":\"\",\"TotalDownPymnt\":\"\",\"TransactionAmt\":\"\",\"Tenure\":\"\",\"AdvEMITenure\":\"\",\"FirstDueDate\":\"\",\"EmiPerMonth\":\"\",\"PGPaymentGUID\":\"12345678945867\",\"PGTransactionID\":\"35B4E44B-1A6C-4B9B-B29A-034085402828X02\"}";
				}
				response.getWriter().println(getResponse);
			}else if(null != request.getParameter("endpoint") && request.getParameter("endpoint").equalsIgnoreCase("OTPGenerate")){
				if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Success")){
					getResponse = "{\"Status\":\"000001\",\"Message\":\"Success\",\"ErrorCode\":\"\",\"Last4Digits\":\"9921\",\"PGPaymentGUID\":\"12345678945867\",\"PGTransactionID\":\"35B4E44B-1A6C-4B9B-B29A-034085402828X02\"}";
				}else if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Failure")){
					getResponse = "{\"Status\":\"000002\",\"Message\":\"This is a dummy failure message\",\"ErrorCode\":\"E101010\",\"Last4Digits\":\"9921\",\"PGPaymentGUID\":\"12345678945867\",\"PGTransactionID\":\"35B4E44B-1A6C-4B9B-B29A-034085402828X02\"}";
				}
				response.getWriter().println(getResponse);
			}else{
				requestBody = getGetBody(request, response);
				response.getWriter().println(requestBody);
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
			}
			
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			responseObj.addProperty("ErrorCode", "JE001");
			response.getWriter().println(responseObj);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().println("GET Request Served at: "+request.getContextPath());
		String requestBody="", getResponse="", endPoint="", responseType="";
		JsonObject responseObj = new JsonObject();
		boolean debug = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		try{
			if(debug){
				requestBody = getGetBody(request, response);
				response.getWriter().println("requestBody: "+ requestBody);
				response.getWriter().println("endpoint: "+ request.getParameter("endpoint"));
				response.getWriter().println("responseType: "+ request.getParameter("responseType"));
			}
				
			if(null != request.getParameter("endpoint") && request.getParameter("endpoint").equalsIgnoreCase("Authorize")){
				if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Success")){
					getResponse = "{\"Status\":\"000001\",\"Message\":\"Success\",\"ErrorCode\":\"\",\"PF\":\"120\",\"Charges\":\"2000\",\"AdvEMIAmt\":\"13000\",\"TotalDownPymnt\":\"2312\",\"TransactionAmt\":\"250000\",\"Tenure\":\"10\",\"AdvEMITenure\":\"5\",\"FirstDueDate\":\"20-09-2021\",\"EmiPerMonth\":\"1300\",\"PGPaymentGUID\":\"12345678945867\"}";
				}else if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Failure")){
					getResponse = "{\"Status\":\"000002\",\"Message\":\"This is a dummy failure message\",\"ErrorCode\":\"E101010\",\"PF\":\"\",\"Charges\":\"\",\"AdvEMIAmt\":\"\",\"TotalDownPymnt\":\"\",\"TransactionAmt\":\"\",\"Tenure\":\"\",\"AdvEMITenure\":\"\",\"FirstDueDate\":\"\",\"EmiPerMonth\":\"\",\"PGPaymentGUID\":\"\"}";
				}
				response.getWriter().println(getResponse);
			}else if(null != request.getParameter("endpoint") && request.getParameter("endpoint").equalsIgnoreCase("OTPGenerate")){
				if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Success")){
					getResponse = "{\"Status\":\"000001\",\"Message\":\"Success\",\"ErrorCode\":\"\",\"Last4Digits\":\"9921\",\"PGPaymentGUID\":\"12345678945867\"}";
				}else if(null != request.getParameter("responseType") && request.getParameter("responseType").equalsIgnoreCase("Failure")){
					getResponse = "{\"Status\":\"000002\",\"Message\":\"This is a dummy failure message\",\"ErrorCode\":\"E101010\",\"Last4Digits\":\"9921\",\"PGPaymentGUID\":\"12345678945867\"}";
				}
				response.getWriter().println(getResponse);
			}else{
				requestBody = getGetBody(request, response);
				response.getWriter().println(requestBody);
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
			}
			
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			responseObj.addProperty("ErrorCode", "JE001");
			response.getWriter().println(responseObj);
		}
	}
	
	public String getGetBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {}
		body = jb.toString();
		return body;
	}
}
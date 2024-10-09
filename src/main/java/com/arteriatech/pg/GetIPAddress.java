package com.arteriatech.pg;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.logs.ODataLogs;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import com.sap.xsa.security.container.XSUserInfo;
import com.sap.xsa.security.container.XSUserInfoException;

//import com.isg.isgpay.ISGPayEncryption;

/**
 * Servlet implementation class GetIPAddress
 */
@WebServlet("/GetIPAddress")
public class GetIPAddress extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetIPAddress() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String ip = "";
//		Logger
		boolean debug=false;
		ODataLogs oDataLogs = new ODataLogs();
		CommonUtils commonUtils = new CommonUtils();
		String loginID="";
        try {

			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
        		debug=true;

			ip = request.getHeader("X-Forwarded-For");  
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("Proxy-Client-IP");

				if(debug)
			    	response.getWriter().println("Proxy-Client-IP: "+ip);
			}  
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("WL-Proxy-Client-IP");

				if(debug)
			    	response.getWriter().println("WL-Proxy-Client-IP: "+ip);
			}  
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("HTTP_CLIENT_IP");

				if(debug)
			    	response.getWriter().println("HTTP_CLIENT_IP: "+ip);
			}  
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("HTTP_X_FORWARDED_FOR");

				if(debug)
			    	response.getWriter().println("HTTP_X_FORWARDED_FOR: "+ip);
			}  
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getRemoteAddr();

				if(debug)
			    	response.getWriter().println("getRemoteAddr: "+ip);
			}  
			
//			ip = "103.57.135.122, 34.93.99.2, 155.56.216.33, 155.56.216.323";
			ip = ip.replaceAll("\\s", "");
			if(ip.length() > 40){
				String originalIP = ip;
				ip = ip.substring(0, 40);
				//insert into Application logs
				if(request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true"))
					debug  = true;
				
				if(debug)
					response.getWriter().println("IP length more than 40 chars");
				
				try{
					String oDataURL="", userName="", password="", userPass="", aggregatorID="";
					
					oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
					
					userPass = userName+":"+password;
					
					oDataLogs.insertApplicationLogs(request, response, "Java", "GetIPAddress", originalIP, "", "", oDataURL, userPass, aggregatorID, userName, debug);
				}catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("ip", ip);
			response.getWriter().print(new Gson().toJson(result));
		} catch (Exception e) {
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}

			response.getWriter().println("buffer: "+buffer.toString());
			
			String oDataURL="", userName="", password="", userPass="", aggregatorID="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "GetIPAddress", oDataURL, userPass, aggregatorID, userName, debug);
		}
		
		
//        response.getWriter().println("IP: "+ip);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

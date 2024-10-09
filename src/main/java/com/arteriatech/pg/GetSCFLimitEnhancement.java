package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class GetSCFLimitEnhancement
 */
@WebServlet("/GetSCFLimitEnhancement")
public class GetSCFLimitEnhancement extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSCFLimitEnhancement() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try {
			boolean debug = false, requestDebug=false;
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				requestDebug = true;
			}
			
			if(requestDebug)
			{
				response.getWriter().println("Inside doGet()");
			}
			String customerNo="", errorCode="", errorMsg="", sessionID="", merchantCode="", wsURL="";
			
			CommonUtils commonUtils = new CommonUtils();
			
			if(requestDebug)
				response.getWriter().println("Inside doGet(): "+request.getParameter("GetSCFLimitEnhancement"));
			
			JSONObject jsonobject1 = new JSONObject(request.getParameter("GetSCFLimitEnhancement"));
			
			if(requestDebug)
			{
				response.getWriter().println("jsonobject1: "+jsonobject1);
				response.getWriter().println("jsonobject1.toString: "+jsonobject1.toString());
			}
			
			String payLoad = request.getParameter("GetSCFLimitEnhancement");
			
			if(requestDebug)
				response.getWriter().println("payLoad: "+payLoad);
			
			if(null != payLoad && payLoad.trim().length() > 0 && payLoad != "")
			{
				JSONObject jsonobject = new JSONObject(payLoad);
				if(requestDebug)
					response.getWriter().println("jsonobject: "+jsonobject);
				
				try
				{
					if(null != jsonobject.getString("debug") && jsonobject.getString("debug").equalsIgnoreCase("true"))
					{
						 debug = true;
					}
				}
				catch (JSONException e) {
					if(e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found."))
					{
						debug = false;
					}
				}
				
//				response.getWriter().println("debug: "+debug);
				if(debug)
				{
					response.getWriter().println("payLoad: "+payLoad);
					response.getWriter().println("CustomerNo: "+jsonobject.getString("CustomerNo"));
				}
				
				if(null != jsonobject.getString("CustomerNo"))
				{
					customerNo = jsonobject.getString("CustomerNo");
				}
				else
				{
					errorCode = "E100";
					errorMsg = properties.getProperty(errorCode);
				}
				
				String loginID = commonUtils.getLoginID(request, response, debug);
				// if (request.getUserPrincipal() != null) 
				if(loginID != null) 
				{
					if(loginID == null)
					{
						errorCode = "E125";
					}
					else
					{
						String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
						if (debug)
							response.getWriter().println("authMethod:" + authMethod);
						if(authMethod.equalsIgnoreCase("BasicAuthentication")){
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug)
								response.getWriter().println("url:" + url);
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							if (debug)
								response.getWriter().println("Generating sessionID:" + sessionID);
							if (sessionID.contains(" ")) {
								errorCode = "S001";
								errorMsg = sessionID;

								if (debug)
									response.getWriter().println("Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
							}
						} else{
							sessionID ="";
						}
					}
				}
				else
				{
					errorCode = "E125";
					if(debug)
						response.getWriter().println("Generating sessionID - errorCode:" +errorCode);
				}
				
				if(debug)
				{
					response.getWriter().println("errorCode: "+errorCode);
					response.getWriter().println("errorMsg: "+errorMsg);
				}
					
				if(errorCode != null && (errorCode.trim().length() == 0 || errorCode.equalsIgnoreCase("S001")))
				{
					errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);
					if(debug)
					{
						response.getWriter().println("errorCode: "+errorCode);
						response.getWriter().println("errorMsg: "+errorMsg);
					}
					
					if(errorCode != null & errorCode.trim().length() == 0)
					{
						merchantCode = commonUtils.getODataDestinationProperties("AggregatorID", "CPIConnect");
						if(debug)
						{
							response.getWriter().println("merchantCode: "+merchantCode);
						}
						
						wsURL = commonUtils.getODataDestinationProperties("URL", "CPIConnect");
						
						
						if(debug)
						{
							response.getWriter().println("wsURL: "+wsURL);
						}
						
						String formattedStr = "";
						try{
							int number = Integer.parseInt(customerNo);
							formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
							customerNo = formattedStr;
						}catch (NumberFormatException e) {
//							formattedStr = customerNo;
						}
						
						JSONObject inputJson =  new JSONObject();
				        JSONObject root = new  JSONObject();
				        inputJson.put("DealerID", customerNo);
				        inputJson.put("AggregatorID", merchantCode);
				        root.put("Root", inputJson);
				        
				        byte[] postDataBytes = root.toString().getBytes("UTF-8");
				        
				        String userPass = "", userName="", authParam="";
						userName = commonUtils.getODataDestinationProperties("User", "CPIConnect");
						userPass = commonUtils.getODataDestinationProperties("Password", "CPIConnect");
						authParam = userName+":"+userPass;
				        
				        /*String system = commonUtils.readDestProperties("System");
						String userpass = "";
								
						if(system.equalsIgnoreCase("QAS"))
						{
							userpass = properties.getProperty("PeakLimitQASUsrPass");
						}
						else if(system.equalsIgnoreCase("PRD"))
						{
							userpass = properties.getProperty("PeakLimitPRDUsrPass");
						}
						else if (system.equalsIgnoreCase("DEV"))
						{
							userpass = properties.getProperty("PeakLimitDEVUsrPass");
						}
						else
						{
//							userpass = "E127";
							if (system.equalsIgnoreCase("E153"))
							{
								userpass = system;
							}
							else
							{
								userpass = "E127";
							}
						}*/
						
						if(wsURL != "E106" && merchantCode != "E112" && wsURL.trim().length() > 0)
						{
//							wsURL=wsURL+"/SCFLimitEnhancement";
							wsURL=wsURL+properties.getProperty("GetScenario");
							URL url = new URL(wsURL);
							HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
							
							con.setRequestMethod("POST");
							con.setRequestProperty("Content-Type", "application/json"); 
							con.setRequestProperty("charset", "utf-8");
							con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
							con.setRequestProperty("Accept", "application/json");
							con.setDoOutput(true);
							con.setDoInput(true);

							String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
							con.setRequestProperty("Authorization", basicAuth);
							con.connect();
							
							OutputStream os = con.getOutputStream();
							OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
							osw.write(root.toString());
							osw.flush();
							osw.close();

							StringBuffer sb = new StringBuffer();
							BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
							String line = null;
							while ((line = br.readLine()) != null) {
							    sb.append(line + "\n");
							}
							br.close();
							
							if(debug)
								response.getWriter().println("sb: "+sb.toString());
							
							String cpiResponse = sb.toString();
												
							if(debug)
							{
								JSONObject responseObj = new JSONObject(cpiResponse);
								response.getWriter().println("resSplitResult: "+responseObj.getString("EnhancementType"));
								response.getWriter().println("resSplitResult: "+responseObj.getString("ProposedLimit"));
								response.getWriter().println("resSplitResult: "+responseObj.getString("Status"));
							}
							
							if(cpiResponse != null && cpiResponse.trim().length() > 0)
							{
								response.getWriter().println(cpiResponse);
							}
							else
							{
								errorCode = "E107";
								errorMsg = properties.getProperty(errorCode);
								JsonObject finalResult = new JsonObject();
								JsonObject result = new JsonObject();
								result.addProperty("errorCode",errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								finalResult.add("SCFLimitEnhancement", result);
								response.getWriter().println(new Gson().toJson(finalResult));
							}
						}
						else
						{
							if(wsURL!= null && wsURL.equalsIgnoreCase("E106"))
							{
								errorCode = wsURL;
							}
							
							if(merchantCode!= null && merchantCode.equalsIgnoreCase("E112"))
							{
								errorCode = merchantCode;
							}
							
							/*if(userpass!= null && userpass.equalsIgnoreCase("E127"))
							{
								errorCode = userpass;
							}*/
							
							errorMsg = properties.getProperty(errorCode);
							JsonObject finalResult = new JsonObject();
							JsonObject result = new JsonObject();
							result.addProperty("errorCode",errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							finalResult.add("SCFLimitEnhancement", result);
							response.getWriter().println(new Gson().toJson(finalResult));
						}
					}
					else
					{
						errorMsg = properties.getProperty(errorCode);
						JsonObject finalResult = new JsonObject();
						JsonObject result = new JsonObject();
						result.addProperty("errorCode",errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						finalResult.add("SCFLimitEnhancement", result);
						response.getWriter().println(new Gson().toJson(finalResult));
					}
				}
				else
				{
					JsonObject finalResult = new JsonObject();
					JsonObject result = new JsonObject();
					result.addProperty("errorCode",errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					finalResult.add("SCFLimitEnhancement", result);
					response.getWriter().println(new Gson().toJson(finalResult));
				}
			}
			else
			{
				errorCode = "E118";
				errorMsg = properties.getProperty(errorCode);
				
				JsonObject finalResult = new JsonObject();
				JsonObject result = new JsonObject();
				result.addProperty("errorCode",errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				finalResult.add("SCFLimitEnhancement", result);
				response.getWriter().println(new Gson().toJson(finalResult));
			}
		} catch (URISyntaxException e) {
			JsonObject finalResult = new JsonObject();
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			finalResult.add("SCFLimitEnhancement", result);
			response.getWriter().println(new Gson().toJson(finalResult));
		}catch (Exception e) {
			JsonObject finalResult = new JsonObject();
			JsonObject result = new JsonObject();
			result.addProperty("Exception",""+e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			finalResult.add("SCFLimitEnhancement", result);
			response.getWriter().println(new Gson().toJson(finalResult));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
 * Servlet implementation class UpdSCFLimitEnhancement
 */
@WebServlet("/UpdSCFLimitEnhancement")
public class UpdSCFLimitEnhancement extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdSCFLimitEnhancement() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try
		{
			boolean debug = false;
			if(debug)
			{
				response.getWriter().println("Inside doGet()");
			}
			String customerNo="", partnerNames = "", dealerAddr1 = "", dealerAddr2 = "", dealerAddr3="", dealerAddr4="", city="", 
					constitutionType="",  dealerPAN="", sanctionLimit="", caAccount="", odAccount="", dealerName="", 
					proposedLimit="", errorCode="", errorMsg="", merchantCode="", acceptedAmt="", ipAddress="", status="", sessionID="", wsURL="";
			
			
			String payLoad = request.getParameter("UpdateSCFLimitEnhancement");
			if(debug)
			{
				response.getWriter().println("payLoad: "+payLoad);
			}
			JSONObject jsonobject = new JSONObject(payLoad);
			CommonUtils commonUtils = new CommonUtils();
			if(null != payLoad && payLoad.trim().length() > 0 && payLoad != "")
			{
				if(debug)
				{
					response.getWriter().println("jsonobject: "+jsonobject.toString());
				}
				
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
				
				if(null != jsonobject.getString("Status"))
				{
					status = jsonobject.getString("Status");
					boolean isValidStatus = false;
					if(status == "000001" || status == "000002" || status == "000003" || status == "000004" || status != "000005")
					{
						isValidStatus = true;
					}
					if(! isValidStatus)
					{
						errorCode = "E110";
						errorMsg = properties.getProperty(errorCode);
					}
				}
				else
				{
					errorCode = "E101";
					errorMsg = properties.getProperty(errorCode);
				}
				
				String loginID = commonUtils.getLoginID(request, response, debug);
				// if (request.getUserPrincipal() != null) 
				if (loginID != null) 
				{
					if(loginID == null)
					{
						errorCode = "E125";
						errorMsg = properties.getProperty(errorCode);
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
					errorMsg = properties.getProperty(errorCode);
					if(debug)
						response.getWriter().println("Generating sessionID - errorCode:" +errorCode);
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
				
//				merchantCode = commonUtils.readDestProperties("AggrID");
				merchantCode = commonUtils.getODataDestinationProperties("AggregatorID", "CPIConnect");
				
				
				if(merchantCode.equalsIgnoreCase("E112"))
				{
					errorCode = merchantCode;
					errorMsg = properties.getProperty(errorCode);
				}
				
//				if(errorCode != null & errorCode.trim().length() == 0)
				if(errorCode != null && (errorCode.trim().length() == 0 || errorCode.equalsIgnoreCase("S001")))
				{
					errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);
					
					if(errorCode != null & errorCode.trim().length() == 0)
					{
						if(errorCode != null & errorCode.trim().length() == 0)
						{
							errorCode = commonUtils.validateInboundReq(request, response, jsonobject, debug);
							
							if(errorCode == null || errorCode.trim().length() == 0 || errorCode.equalsIgnoreCase("103"))
							{
								try
								{
									if(debug)
										response.getWriter().println("jsonobject180: "+jsonobject);
									if(jsonobject.getString("Status").equalsIgnoreCase("000003"))
									{
										acceptedAmt = jsonobject.getString("AcceptedLimit");
										ipAddress = jsonobject.getString("IPAddress");
										status = jsonobject.getString("Status");
										constitutionType = jsonobject.getString("ConstitutionType");
										dealerPAN = jsonobject.getString("DealerPAN");
										sanctionLimit = jsonobject.getString("SanctionLimit");
										odAccount = jsonobject.getString("ODAccount");
										caAccount = jsonobject.getString("CAAccount");
										dealerName = jsonobject.getString("DealerName");
										proposedLimit = jsonobject.getString("ProposedLimit");
										partnerNames = jsonobject.getString("PartnerNames");
										dealerAddr1 = jsonobject.getString("DealerAddress1");
										dealerAddr2 = jsonobject.getString("DealerAddress2");
										dealerAddr3 = jsonobject.getString("DealerAddress3");
										dealerAddr4 = jsonobject.getString("DealerAddress4");
										city = jsonobject.getString("City");
									}
									else
									{
										acceptedAmt = "";
										ipAddress = jsonobject.getString("IPAddress");
										status = jsonobject.getString("Status");
										constitutionType = "";
										dealerPAN = "";
										sanctionLimit = "";
										odAccount = "";
										caAccount = "";
										dealerName = jsonobject.getString("DealerName");
										proposedLimit = "";
										partnerNames = "";
										dealerAddr1 = "";
										dealerAddr2 = "";
										dealerAddr3 = "";
										dealerAddr4 = "";
										city = "";
									}
								}
								catch (JSONException e) {
									JsonObject result = new JsonObject();
									result.addProperty("Exception",e.getClass().getCanonicalName());
									result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
									result.addProperty("Status", properties.getProperty("ErrorStatus"));
									result.addProperty("Valid", "false");
									response.getWriter().println(new Gson().toJson(result));
								}
								
								String formattedStr = "";
								try{
									int number = Integer.parseInt(customerNo);
									formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
									customerNo = formattedStr;
								}catch (NumberFormatException e) {
//									formattedStr = customerNo;
								}
								
								JSONObject inputJson =  new JSONObject();
						        JSONObject root = new  JSONObject();
						        inputJson.put("DealerID", customerNo);
						        inputJson.put("AggregatorID", merchantCode);
						        inputJson.put("AcceptedLimit", acceptedAmt);
						        inputJson.put("IPAddress", ipAddress);
						        inputJson.put("Status", status);
						        inputJson.put("DealerName", dealerName);
						        inputJson.put("CAAccount", caAccount);
						        inputJson.put("ODAccount", odAccount);
						        inputJson.put("SanctionLimit", sanctionLimit);
						        inputJson.put("DealerPAN", dealerPAN);
						        inputJson.put("ConstitutionType", constitutionType);
						        inputJson.put("ProposedLimit", proposedLimit);
						        inputJson.put("PartnerNames", partnerNames);
						        inputJson.put("DealerAddress1", dealerAddr1);
						        inputJson.put("DealerAddress2", dealerAddr2);
						        inputJson.put("DealerAddress3", dealerAddr3);
						        inputJson.put("DealerAddress3", dealerAddr3);
						        inputJson.put("DealerAddress4", dealerAddr4);
						        inputJson.put("City", city);
						        root.put("Root", inputJson);
								
								wsURL = commonUtils.getODataDestinationProperties("URL", "CPIConnect");
								
								if(debug)
								{
									response.getWriter().println("root: "+root.toString());
									response.getWriter().println("inputJson: "+inputJson.toString());
									response.getWriter().println("wsURL: "+wsURL);
								}
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
//									userpass = "E127";
									if (system.equalsIgnoreCase("E153"))
									{
										userpass = system;
									}
									else
									{
										userpass = "E127";
									}
								}*/
								
								String userHistory = "";
								
								if(wsURL != "E106"  && (status.equalsIgnoreCase("000003") || (status.equalsIgnoreCase("000004"))))
								{
//									wsURL=wsURL+"/SCFLimitEnhancementUpdate";
									userHistory = hasUserAlreadyAgreed(request, response, customerNo, merchantCode, wsURL, authParam, debug);
//									response.getWriter().println("userHistory: "+userHistory);
									
									if(userHistory.equalsIgnoreCase("000002"))
									{
										wsURL=wsURL+"/"+properties.getProperty("UpdateScenario");
										if(debug)
										{
											response.getWriter().println("wsURL: "+wsURL);
										}
										byte[] postDataBytes = root.toString().getBytes("UTF-8");
										
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
										
										int responseCode = con.getResponseCode();
										if(debug){
											response.getWriter().println("getResponseCode: "+con.getResponseCode());
//											response.getWriter().println("sb: "+sb.toString());
										}
											
										
//										String cpiResponse = sb.toString();
															
										/*if(debug)
										{
											JSONObject responseObj = new JSONObject(cpiResponse);
											response.getWriter().println("resSplitResult: "+responseObj.getString("Message"));
											response.getWriter().println("resSplitResult: "+responseObj.getString("Status"));
										}*/
										
//										if(cpiResponse != null && cpiResponse.trim().length() > 0)
										if(responseCode == 204)
										{
//											response.getWriter().println(cpiResponse);
											JsonObject result = new JsonObject();
											result.addProperty("Status","000001");
											result.addProperty("Message", "Success");
											response.getWriter().println(new Gson().toJson(result));
										}
										else if(responseCode != 204)
										{
											JsonObject result = new JsonObject();
											result.addProperty("Status","000002");
											result.addProperty("Message", "Update Failed");
											response.getWriter().println(new Gson().toJson(result));
										} else {
											errorCode = "E107";
											errorMsg = properties.getProperty(errorCode);
											
											JsonObject result = new JsonObject();
											result.addProperty("errorCode",errorCode);
											result.addProperty("Message", errorMsg);
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
										}
									}
									else
									{
										String[] messagePart = userHistory.split("\\|");
										if(debug)
										{
											response.getWriter().println("messagePart: "+messagePart.toString());
										}
										
										String errorCodeLcl = "";
										
										if(messagePart.length > 1)
										{
											String statusCode = messagePart[0];
											if(debug)
											{
												response.getWriter().println("statusCode-post: "+statusCode);
											}
											if(! statusCode.equalsIgnoreCase("000001") && ! statusCode.equalsIgnoreCase("000003") && ! statusCode.equalsIgnoreCase("000004"))
											{
												if(debug)
												{
													response.getWriter().println("Status not 000001, 000003 or 000004");
												}
												
												errorMsg = properties.getProperty(errorCode);
												errorCodeLcl = errorCode;
												
												if(debug)
												{
													response.getWriter().println("errorMsg-postif: "+errorMsg);
													response.getWriter().println("errorCodeLcl-postif: "+errorCodeLcl);
												}
											}
											else
											{
												if(debug)
												{
													response.getWriter().println("Status is 000005");
												}
												errorMsg = messagePart[1];
												errorCodeLcl = messagePart[0];
												if(debug)
												{
													response.getWriter().println("errorMsg-postelse: "+errorMsg);
													response.getWriter().println("errorCodeLcl-postelse: "+errorCodeLcl);
												}
												
												if(errorCodeLcl.equalsIgnoreCase("000003"))
												{
													errorMsg = "User has already accepted the offer";
												}
												else if(errorCodeLcl.equalsIgnoreCase("000004"))
												{
													errorMsg = "User has already declined the offer";
												}
												else if(errorCodeLcl.equalsIgnoreCase("000001"))
												{
													errorMsg = "User is not eligible for the offer";
												}
											}
										}
										else
										{
											errorCodeLcl = userHistory;
											errorMsg = "No Message received";
											if(debug)
											{
												response.getWriter().println("errorMsg-postelsem: "+errorMsg);
												response.getWriter().println("errorCodeLcl-postelsem: "+errorCodeLcl);
											}
										}
											
										JsonObject result = new JsonObject();
										result.addProperty("errorCode",errorCodeLcl);
										result.addProperty("Message", errorMsg);
										result.addProperty("Status", properties.getProperty("ErrorStatus"));
										result.addProperty("Valid", "false");
										response.getWriter().println(new Gson().toJson(result));
									}
								}
								else
								{
									if(wsURL == "E106")
										errorCode = wsURL;
//									else if(userpass == "E127")
//										errorCode = userpass;
									else if(status.equalsIgnoreCase("000002"))
										errorCode ="E131";
									else if(! status.equalsIgnoreCase("000002"))
										errorCode ="E131";
									
									errorMsg = properties.getProperty(errorCode);
									
									JsonObject result = new JsonObject();
									result.addProperty("errorCode",errorCode);
									result.addProperty("Message", errorMsg);
									result.addProperty("Status", properties.getProperty("ErrorStatus"));
									result.addProperty("Valid", "false");
									response.getWriter().println(new Gson().toJson(result));
								}
							}
							else
							{
								errorMsg = properties.getProperty(errorCode);
								
								JsonObject result = new JsonObject();
								result.addProperty("errorCode",errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							}
						}
						else
						{
							errorMsg = properties.getProperty(errorCode);
							
							JsonObject result = new JsonObject();
							result.addProperty("errorCode",errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						}
					}
					else
					{
						errorMsg = properties.getProperty(errorCode);
						
						JsonObject result = new JsonObject();
						result.addProperty("errorCode",errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().println(new Gson().toJson(result));
					}
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("errorCode",errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
			}
			else
			{
				errorCode = "E108";
				errorMsg = properties.getProperty(errorCode);
				
				JsonObject result = new JsonObject();
				result.addProperty("errorCode",errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		}
		catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	public String hasUserAlreadyAgreed(HttpServletRequest request, HttpServletResponse response, String customerNo, String merchantCode, String wsURL, String userpass, boolean debug) throws IOException
	{
		String statusCode = "";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		try
		{
			String errorMsg="";
			
			wsURL=wsURL+"/"+properties.getProperty("GetScenario");
			
			JSONObject inputJson =  new JSONObject();
	        JSONObject root = new  JSONObject();
	        inputJson.put("DealerID", customerNo);
	        inputJson.put("AggregatorID", merchantCode);
	        root.put("Root", inputJson);
	        
	        if(debug)
	        {
	        	response.getWriter().println("inputJson - hasUserAlreadyAgreed: "+inputJson);
	        	response.getWriter().println("Root - hasUserAlreadyAgreed: "+root);
	        	response.getWriter().println("wsURL - hasUserAlreadyAgreed: "+wsURL);
	        	response.getWriter().println("merchantCode - hasUserAlreadyAgreed: "+merchantCode);
	        }
	        
	        byte[] postDataBytes = root.toString().getBytes("UTF-8");
	        if(debug)
			{
				response.getWriter().println("wsURL - hasUserAlreadyAgreed: "+wsURL);
				response.getWriter().println("merchantCode - hasUserAlreadyAgreed: "+merchantCode);
			}
			if(wsURL != "E106" && merchantCode != "E112" && wsURL.trim().length() > 0)
			{
				URL url = new URL(wsURL);
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json"); 
				con.setRequestProperty("charset", "utf-8");
				con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);
				con.setDoInput(true);

				String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
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
					response.getWriter().println("sb1: "+sb.toString());
				
				String cpiResponse = sb.toString();
				JSONObject cpiResponseObj = new JSONObject(cpiResponse);
				JSONObject responseObj = new JSONObject(cpiResponseObj.get("SCFLimitEnhancement").toString());
//				JSONObject responseObj = new JSONObject(cpiResponse1);
				if(debug)
				{
					response.getWriter().println("resSplitResult-cpiResponse: "+cpiResponse);
					response.getWriter().println("resSplitResult-cpiResponseObj: "+cpiResponseObj);
					response.getWriter().println("resSplitResult-responseObj: "+responseObj);
					/*response.getWriter().println("resSplitResult: "+responseObj.getString("EnhancementType"));
					response.getWriter().println("resSplitResult: "+responseObj.getString("ProposedLimit"));*/
					response.getWriter().println("resSplitResult-Status: "+responseObj.getString("Status"));
					
				}
				
				if(cpiResponse != null && cpiResponse.trim().length() > 0)
				{
					if(! responseObj.getString("Status").equalsIgnoreCase("000002"))
					{
						statusCode = responseObj.getString("Status")+"|"+responseObj.getString("Message");
					}
					else
					{
						statusCode = responseObj.getString("Status");
					}
					
					if(debug)
						response.getWriter().println("statusCode - hasUserAlreadyAgreed: "+statusCode);
					
					
					return statusCode;
				}
				else
				{
					statusCode = "E107";
					errorMsg = properties.getProperty(statusCode);
					statusCode = "E107";
					/*JsonObject result = new JsonObject();
					result.addProperty("errorCode",statusCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));*/
					if(debug)
						response.getWriter().println("statusCode E107 - hasUserAlreadyAgreed: "+statusCode);
					return statusCode;
				}
			}
			else
			{
				if(wsURL.length() > 0)
				{
					statusCode = wsURL;
				}
				else if(merchantCode.length() > 0)
				{
					statusCode = merchantCode;
				}
				else
				{
					statusCode = "E129";
				}
					
				errorMsg = properties.getProperty(statusCode);
				
				JsonObject result = new JsonObject();
				result.addProperty("errorCode",statusCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				
				return statusCode;
			}
		} catch (MalformedURLException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (ProtocolException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (IOException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (JSONException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception",e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName()+"--->"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.api.WalletAPI;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class User
 */
@WebServlet("/User")
public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;

//	private TenantContext  tenantContext;
	/*private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";*/
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
    /* 
     * Default constructor. 
     */
    public User() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject userRegisGetResponse = new JsonObject();
		JsonObject userRegisPostResponse = new JsonObject();
		
		JsonObject userUpdateResponse = new JsonObject();
		
		try
		{
			String walletUserCode="", status="", remarks="", responseDateTime="", checksum="", errorStatus="", errorMessage="";
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", clientCode = "", merchantCode="", userRegistrationCall="", PGCategoryID="";
			String loginSessionID="", customerNo="", epID="" , message="";
			String oDataUrl ="", userName1 ="",password="", userPass="" , uniqueGuid="",adharID="", walletCode="",configurationValues="", sessionUrl="";
			 String activeUserSts ="000002", otherUserSts="000001";
			boolean debug = false;
			boolean isValidCustomer = false;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			userRegistrationCall = properties.getProperty("userRegistrationCall");
			
//			debug = true;
			//For debugging java app
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			if(debug)
				response.getWriter().println("doGet.userRegistrationCall: "+userRegistrationCall);
			
			if(null != request.getParameter("EPID") && request.getParameter("EPID").trim().length() > 0 )
				epID = request.getParameter("EPID");
			if(debug)
				response.getWriter().println("doGet.epID: "+epID);
			
			message = validateParam(request, response, properties, debug);
			
			if (message.equalsIgnoreCase("")){
				if(null != request.getParameter("PGCategoryID"))
					PGCategoryID = request.getParameter("PGCategoryID");
				else
					PGCategoryID = "000001";
				if(debug)
					response.getWriter().println("doGet.PGCategoryID: "+PGCategoryID);
				
				customerNo = request.getParameter("CustomerNo");
				if(debug)
					response.getWriter().println("doGet.customerNo: "+ customerNo);
				
//				loginSessionID = request.getParameter("sessionID");
				if(debug)
					response.getWriter().println("doGet.loginSessionID: "+ loginSessionID);
				String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
				String loginID =  commonUtils.getUserPrincipal(request, "name", response);
				if(debug)
					response.getWriter().println("doGet.loginID: "+loginID);
				if (debug)
					response.getWriter().println("authMethod:" + authMethod);
				String errorCode="", errorMsg="";
				if(authMethod.equalsIgnoreCase("BasicAuthentication")){
					sessionUrl = commonUtils.getDestinationURL(request, response, "URL");
					if (debug)
						response.getWriter().println("sessionUrl:" + sessionUrl);
					loginSessionID = commonUtils.createUserSession(request, response, sessionUrl, loginID, debug);
					if (debug)
						response.getWriter().println("Generating sessionID:" + loginSessionID);
					if (loginSessionID.contains(" ")) {
						errorCode = "S001";
						errorMsg = loginSessionID;

						if (debug)
							response.getWriter().println("Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
					}
				} else{
					loginSessionID ="";
				}
				
				if (debug)
					response.getWriter().println("doGet.validateCustomer:" + errorCode);
				
				if(errorCode.trim().length() > 0)
				{
					JsonObject result = new JsonObject();
					result.addProperty("errorStatus", "S001");
					result.addProperty("errorMessage", errorMsg);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().print(new Gson().toJson(result));
					/*errorJsonResponse.addProperty("errorStatus", errorCode);
					if(errorCode.equalsIgnoreCase("S001"))
						errorJsonResponse.addProperty("errorMessage", errorMsg);
					else
						errorJsonResponse.addProperty("errorMessage", properties.getProperty(errorCode));
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(errorJsonResponse);*/
				}else{
					isValidCustomer = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
					if(debug)
						response.getWriter().println("doGet.isValidCustomer: "+isValidCustomer);
					
					adharID = request.getParameter("adhar-card-id");
					if(debug)
						response.getWriter().println("doGet.adharID: "+ adharID);
						//Temporary line added below
		//				isValidCustomer = true;
					if(isValidCustomer){
							
						oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
						userName1 = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
						password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
						userPass = userName1+":"+password;
						uniqueGuid = commonUtils.generateGUID(36);
						if(debug)
						{
							response.getWriter().println("doGet.oDataUrl: "+ oDataUrl);
							response.getWriter().println("doGet.userName1: "+ userName1);
							response.getWriter().println("doGet.password: "+ password);
							response.getWriter().println("doGet.userPass: "+ userPass);
							response.getWriter().println("doGet.uniqueGuid: "+ uniqueGuid);
						}
						
						// TODO GET Call
						userRegisGetResponse = getUserRegByAliasID(request, response, oDataUrl, userPass, adharID, epID, debug);
						
//						condition-1
						if (userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
							if(userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("UserRegStatus").getAsString().equalsIgnoreCase("000002")){
								JsonObject result = new JsonObject();
								result.addProperty("errorStatus", "E195");
								result.addProperty("errorMessage", "Unique ID already used");
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.getWriter().print(new Gson().toJson(result));
							}else{
								configurationValues = getConstantValues(request, response, "", "B2BIZ");
								//Use this for update logic
								
								String uniqueID = "";
								JsonObject bankAPIJsonObj = new JsonObject();
								uniqueGuid = ""; 
								uniqueGuid = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("CommnGuid").getAsString();
								walletCode = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("UserRegId").getAsString();
								adharID = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("AliasID").getAsString();
								
								bankAPIJsonObj = getEscrowUserDetails(request, response, walletCode, epID, configurationValues, debug);
								if(debug)
									response.getWriter().println("doGet.else.bankAPIJsonObj2: "+ bankAPIJsonObj);
								
								if(debug){
									response.getWriter().println("doGet.else.uniqueGuid: "+ uniqueGuid);
									response.getWriter().println("doGet.else.walletCode: "+ walletCode);
								}
								
								if (bankAPIJsonObj.get("Valid").getAsString().equalsIgnoreCase("false")) {
//									if(message.equalsIgnoreCase("Invalid Usercode.")){
//									call userreg bank api
//										
//									on success, updateUserRegisDetails(request, response, userRegisGetResponse, "000002", uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									if(bankAPIJsonObj.has("errorMsg") && bankAPIJsonObj.get("errorMsg").getAsString().equalsIgnoreCase("Invalid Usercode.")){
										uniqueID = "";
										bankAPIJsonObj = new JsonObject();
//										bankAPIJsonObj = getEscrowUserDetails(request, response, walletCode, epID, configurationValues, debug);
										//Call escrow user reg here
										
										String wholeParamString="", paramName="", paramValue="", WSURL="";
										wholeParamString = configurationValues;
										String[] splitResult = wholeParamString.split("\\|");
										
										//For debugging java application
										if(debug)
											response.getWriter().println("doGetelse.wholeParamString: "+ wholeParamString);
										
										for(String s : splitResult)
										{
								        	paramName = s.substring(0, s.indexOf("="));
								        	paramValue = s.substring(s.indexOf("=")+1,s.length());
								        	
								        	if(paramName.equalsIgnoreCase("MerchantCode"))
								        	{
								        		merchantCode = paramValue;
								        	}
								        	if(paramName.equalsIgnoreCase("WSURL"))
								        	{
								        		WSURL = paramValue;
								        	}
								        	if(paramName.equalsIgnoreCase("clientCode"))
								        	{
								        		clientCode = paramValue;
								        	}
										}
										if(debug){
											response.getWriter().println("doGetelse.merchantCode: "+ merchantCode);
											response.getWriter().println("doGetelse.WSURL: "+ WSURL);
											response.getWriter().println("doGetelse.WSURL: "+ clientCode);
										}
										
										if(null != clientCode)
										{
											walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
											merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
											merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
											if(debug)
												response.getWriter().println("PRD Keys found");
											if(debug)
												response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
											if(debug)
												response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
											if(debug)
												response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
										}
										else
										{
											walletPublicKey = properties.getProperty("WalletPublicKey");
											merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
											merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
										}
										
										String userName = request.getParameter("user-name");
										
										userName = userName.replaceAll("%26", "AND");
										userName = userName.replaceAll("%20", " ");
										userName = userName.replaceAll("%2C", ",");
										userName = userName.replaceAll("%2F", "/");
										
										WalletParamMap inputParamMap = new WalletParamMap();
										
										inputParamMap.put("wallet-user-code", walletCode);
										inputParamMap.put("adhar-card-id", adharID);
										
										if(null !=request.getParameter("adhar-ref-no"))
											inputParamMap.put("adhar-ref-no", request.getParameter("adhar-ref-no"));
										
										if(null !=request.getParameter("user-name"))
											inputParamMap.put("user-name", userName);
										
										if(null !=request.getParameter("email-id"))
											inputParamMap.put("email-id", request.getParameter("email-id"));
										
										if(null !=request.getParameter("mobile-no"))
											inputParamMap.put("mobile-no", request.getParameter("mobile-no"));
					
										if(null !=request.getParameter("address-line1"))
											inputParamMap.put("address-line1", request.getParameter("address-line1"));
										
										//For debugging java application
										if(debug)
										{
											response.getWriter().println("doGetelse.wallet-user-code (mandatory): "+ request.getParameter("wallet-user-code"));
											response.getWriter().println("doGetelse.adhar-card-id: "+ request.getParameter("adhar-card-id"));
											response.getWriter().println("doGetelse.adhar-ref-no (mandatory): "+ request.getParameter("adhar-ref-no"));
											response.getWriter().println("doGetelse.user-name (mandatory): "+ request.getParameter("user-name"));
											response.getWriter().println("doGetelse.address-line1 (mandatory): "+ request.getParameter("address-line1"));
											response.getWriter().println("doGetelse.email-id (mandatory): "+ request.getParameter("email-id"));
											response.getWriter().println("doGetelse.mobile-no (mandatory): "+ request.getParameter("mobile-no"));
											
										}
										//TODO added code
										userRegisGetResponse = new JsonObject();
										WalletAPI getResponse = new WalletAPI();//"https://demo.b2biz.co.in/ws/walletAPI"
										WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, userRegistrationCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
										if(debug)
											response.getWriter().println("doGetelse.responseMap: "+ responseMap);
										
										userRegisGetResponse =	getUserRegByEpIDAndLogin(request, response, oDataUrl, userPass, epID, debug);
										if(debug)
											response.getWriter().println("doGetelse.userRegisGetResponse2: "+ userRegisGetResponse);
										uniqueGuid = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("CommnGuid").getAsString();
										uniqueID = request.getParameter("adhar-card-id");
										
										if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0)){
											userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
											//Success Response
											walletUserCode = responseMap.get("wallet-user-code");
											status = responseMap.get("status");
											remarks = responseMap.get("remarks");
											responseDateTime = responseMap.get("response-date-time");
											checksum = responseMap.get("checksum");
											
											JsonObject result = new JsonObject();
											result.addProperty("walletUserCode", walletUserCode);
											result.addProperty("responseDateTime", responseDateTime);
											result.addProperty("status", status);
											result.addProperty("remarks", remarks);
											result.addProperty("checksum", checksum);
											response.getWriter().print(result);
										}else{
											JsonObject result = new JsonObject();
											result.addProperty("errorStatus", responseMap.get("error_status"));
											result.addProperty("errorMessage", responseMap.get("error_message")); 
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											response.getWriter().print(new Gson().toJson(result));
										}
										
										if(debug)
											response.getWriter().println("doGet.else.userUpdateResponse2: "+ userUpdateResponse);
									}else{
										response.getWriter().println(bankAPIJsonObj);
									}
								} else {

									uniqueID = bankAPIJsonObj.get("UniqueID").getAsString();
									if(bankAPIJsonObj.get("status").getAsString().equalsIgnoreCase("Active")){
										userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									}else{
										userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, otherUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									}
									if(debug)
										response.getWriter().println("doGet.else.userUpdateResponse2: "+ userUpdateResponse);
									
									JsonObject result = new JsonObject();
									result.addProperty("walletUserCode", walletCode);
									result.addProperty("responseDateTime", "");//bankAPIJsonObj.get("responseDateTime").getAsString()
									result.addProperty("status", bankAPIJsonObj.get("status").getAsString());
									result.addProperty("remarks", "");
									result.addProperty("checksum", "");  //bankAPIJsonObj.get("checksum").getAsString()
									response.getWriter().print(new Gson().toJson(result));
								}
							
							}
						}else {
							configurationValues = getConstantValues(request, response, "", "B2BIZ");
							if(debug)
								response.getWriter().print("doGet.configurationValues: "+configurationValues);
							
							userRegisGetResponse =	getUserRegByEpIDAndLogin(request, response, oDataUrl, userPass, epID, debug);
							if(debug)
								response.getWriter().print("doGet.userRegisGetResponse: "+userRegisGetResponse);
							
							walletCode = request.getParameter("wallet-user-code");
							if(debug)
								response.getWriter().println("doGet.walletCode: "+ walletCode);
							
							//condition-2
							if (userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").size() == 0) {
								
								//TODO POST CALL
								userRegisPostResponse = insertUserRegisDetails(request, response, oDataUrl, userPass, walletCode, adharID, uniqueGuid, otherUserSts, epID,debug);
								if(debug)
									response.getWriter().println("doGet.userRegisPostResponse: "+ userRegisPostResponse);
								String wholeParamString="", paramName="", paramValue="", WSURL="";
								wholeParamString = configurationValues;
								String[] splitResult = wholeParamString.split("\\|");
								
								//For debugging java application
								if(debug)
									response.getWriter().println("doGet.wholeParamString: "+ wholeParamString);
								
								for(String s : splitResult)
								{
						        	paramName = s.substring(0, s.indexOf("="));
						        	paramValue = s.substring(s.indexOf("=")+1,s.length());
						        	
						        	if(paramName.equalsIgnoreCase("MerchantCode"))
						        	{
						        		merchantCode = paramValue;
						        	}
						        	if(paramName.equalsIgnoreCase("WSURL"))
						        	{
						        		WSURL = paramValue;
						        	}
						        	if(paramName.equalsIgnoreCase("clientCode"))
						        	{
						        		clientCode = paramValue;
						        	}
								}
								if(debug){
									response.getWriter().println("doGet.merchantCode: "+ merchantCode);
									response.getWriter().println("doGet.WSURL: "+ WSURL);
									response.getWriter().println("doGet.WSURL: "+ clientCode);
								}
								
								if(null != clientCode){
									walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
									merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
									merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
									if(debug)
										response.getWriter().println("PRD Keys found");
									if(debug)
										response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
									if(debug)
										response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
									if(debug)
										response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
								}else{
									walletPublicKey = properties.getProperty("WalletPublicKey");
									merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
									merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
								}
								
								String userName = request.getParameter("user-name");
								
								userName = userName.replaceAll("%26", "AND");
								userName = userName.replaceAll("%20", " ");
								userName = userName.replaceAll("%2C", ",");
								userName = userName.replaceAll("%2F", "/");
								
								WalletParamMap inputParamMap = new WalletParamMap();
								
								inputParamMap.put("wallet-user-code", walletCode);
								inputParamMap.put("adhar-card-id", adharID);
								
								if(null !=request.getParameter("adhar-ref-no"))
									inputParamMap.put("adhar-ref-no", request.getParameter("adhar-ref-no"));
								
								if(null !=request.getParameter("user-name"))
									inputParamMap.put("user-name", userName);
								
								if(null !=request.getParameter("email-id"))
									inputParamMap.put("email-id", request.getParameter("email-id"));
								
								if(null !=request.getParameter("mobile-no"))
									inputParamMap.put("mobile-no", request.getParameter("mobile-no"));
			
								if(null !=request.getParameter("address-line1"))
									inputParamMap.put("address-line1", request.getParameter("address-line1"));
								
								//For debugging java application
								if(debug)
								{
									response.getWriter().println("wallet-user-code (mandatory): "+ request.getParameter("wallet-user-code"));
									response.getWriter().println("adhar-card-id: "+ request.getParameter("adhar-card-id"));
									response.getWriter().println("adhar-ref-no (mandatory): "+ request.getParameter("adhar-ref-no"));
									response.getWriter().println("user-name (mandatory): "+ request.getParameter("user-name"));
									response.getWriter().println("address-line1 (mandatory): "+ request.getParameter("address-line1"));
									response.getWriter().println("email-id (mandatory): "+ request.getParameter("email-id"));
									response.getWriter().println("mobile-no (mandatory): "+ request.getParameter("mobile-no"));
									
								}
								//TODO added code
								userRegisGetResponse = new JsonObject();
								WalletAPI getResponse = new WalletAPI();//"https://demo.b2biz.co.in/ws/walletAPI"
								WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, userRegistrationCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
								if(debug)
									response.getWriter().println("doGet.responseMap: "+ responseMap);
								
								
								//Todo check userreg based on loginid, epid and aggregator
								userRegisGetResponse =	getUserRegByEpIDAndLogin(request, response, oDataUrl, userPass, epID, debug);
								if(debug)
									response.getWriter().println("doGet.userRegisGetResponse2: "+ userRegisGetResponse);
								
								if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
								{
									walletUserCode = responseMap.get("wallet-user-code");
									status = responseMap.get("status");
									remarks = responseMap.get("remarks");
									responseDateTime = responseMap.get("response-date-time");
									checksum = responseMap.get("checksum");
									
									//TODO Update UserRegistration Table
//									entry available in 'userRegisGetResponse'
									if(userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").size() > 0){
									
										userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, adharID, debug);
										if(debug)
											response.getWriter().println("doGet.userUpdateResponse: "+ userUpdateResponse);
									}else{
										uniqueGuid="";
										uniqueGuid = commonUtils.generateGUID(36);
										//TODO POST CALL
										insertUserRegisDetails(request, response, oDataUrl, userPass, walletCode, adharID, uniqueGuid, activeUserSts, epID,debug);
									}
									//Success Response
									JsonObject result = new JsonObject();
									result.addProperty("walletUserCode", walletUserCode);
									result.addProperty("responseDateTime", responseDateTime);
									result.addProperty("status", status);
									result.addProperty("remarks", remarks);
									result.addProperty("checksum", checksum);
									response.getWriter().print(result);
								}
								else
								{
									errorStatus = responseMap.get("error_status");
									errorMessage = responseMap.get("error_message");
									if(debug){
										response.getWriter().println("errorStatus: "+errorStatus);
										response.getWriter().println("errorMessage: "+errorMessage);
									}
									if(errorMessage != null && errorMessage.equalsIgnoreCase("Duplicate User Code Found")){

										String uniqueID = "";
										userRegisPostResponse = new JsonObject();
										
										JsonObject bankAPIJsonObj = new JsonObject();
										bankAPIJsonObj = getEscrowUserDetails(request, response, walletCode, epID, configurationValues, debug);
										if(debug)
											response.getWriter().println("doGet.bankAPIJsonObj2: "+ bankAPIJsonObj);
										
										if(debug)
											response.getWriter().println("doGet.uniqueID: "+ uniqueID);
										
										if (bankAPIJsonObj.get("Valid").getAsString().equalsIgnoreCase("false")) {
										
											JsonObject result = new JsonObject();
											result.addProperty("errorStatus", errorStatus);
											result.addProperty("errorMessage", errorMessage);
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											response.getWriter().print(new Gson().toJson(result));
											
										} else {
											uniqueID = bankAPIJsonObj.get("UniqueID").getAsString();
										
											if(userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").size() > 0){
												
												if(bankAPIJsonObj.get("status").getAsString().equalsIgnoreCase("Active")){
													userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
													if(debug)
														response.getWriter().println("doGet.userUpdateResponse1: "+ userUpdateResponse);
												}
												else{
													userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, otherUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
													if(debug)
														response.getWriter().println("doGet.userUpdateResponse1: "+ userUpdateResponse);
												}
											}else{
												uniqueGuid="";
												uniqueGuid = commonUtils.generateGUID(36);
												//TODO POST CALL
												insertUserRegisDetails(request, response, oDataUrl, userPass, walletCode, uniqueID, uniqueGuid, activeUserSts, epID,debug);
											}
											JsonObject result = new JsonObject();
											result.addProperty("walletUserCode", walletCode);
											result.addProperty("responseDateTime", responseDateTime); //bankAPIJsonObj.get("responseDateTime").getAsString()
											result.addProperty("status", bankAPIJsonObj.get("status").getAsString());
											result.addProperty("remarks", remarks);
											result.addProperty("checksum", "");  				//bankAPIJsonObj.get("checksum").getAsString()
											response.getWriter().print(new Gson().toJson(result));
										}
										
									}else{
										JsonObject result = new JsonObject();
										result.addProperty("errorStatus", errorStatus);
										result.addProperty("errorMessage", errorMessage);
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										response.getWriter().print(new Gson().toJson(result));
									}
								}
							} else {
								//Use this for update logic
								
								String uniqueID = "";
								JsonObject bankAPIJsonObj = new JsonObject();
								uniqueGuid = ""; 
								uniqueGuid = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("CommnGuid").getAsString();
								walletCode = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("UserRegId").getAsString();
								adharID = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("AliasID").getAsString();
								
								bankAPIJsonObj = getEscrowUserDetails(request, response, walletCode, epID, configurationValues, debug);
								if(debug)
									response.getWriter().println("doGet.else.bankAPIJsonObj2: "+ bankAPIJsonObj);
								
								if(debug){
									response.getWriter().println("doGet.else.uniqueGuid: "+ uniqueGuid);
									response.getWriter().println("doGet.else.walletCode: "+ walletCode);
								}
								
								if (bankAPIJsonObj.get("Valid").getAsString().equalsIgnoreCase("false")) {
//									if(message.equalsIgnoreCase("Invalid Usercode.")){
//									call userreg bank api
//										
//									on success, updateUserRegisDetails(request, response, userRegisGetResponse, "000002", uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									if(bankAPIJsonObj.has("errorMsg") && bankAPIJsonObj.get("errorMsg").getAsString().equalsIgnoreCase("Invalid Usercode.")){
										uniqueID = "";
										bankAPIJsonObj = new JsonObject();
//										bankAPIJsonObj = getEscrowUserDetails(request, response, walletCode, epID, configurationValues, debug);
										//Call escrow user reg here
										
										String wholeParamString="", paramName="", paramValue="", WSURL="";
										wholeParamString = configurationValues;
										String[] splitResult = wholeParamString.split("\\|");
										
										//For debugging java application
										if(debug)
											response.getWriter().println("doGetelse.wholeParamString: "+ wholeParamString);
										
										for(String s : splitResult)
										{
								        	paramName = s.substring(0, s.indexOf("="));
								        	paramValue = s.substring(s.indexOf("=")+1,s.length());
								        	
								        	if(paramName.equalsIgnoreCase("MerchantCode"))
								        	{
								        		merchantCode = paramValue;
								        	}
								        	if(paramName.equalsIgnoreCase("WSURL"))
								        	{
								        		WSURL = paramValue;
								        	}
								        	if(paramName.equalsIgnoreCase("clientCode"))
								        	{
								        		clientCode = paramValue;
								        	}
										}
										if(debug){
											response.getWriter().println("doGetelse.merchantCode: "+ merchantCode);
											response.getWriter().println("doGetelse.WSURL: "+ WSURL);
											response.getWriter().println("doGetelse.WSURL: "+ clientCode);
										}
										
										if(null != clientCode)
										{
											walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
											merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
											merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
											if(debug)
												response.getWriter().println("PRD Keys found");
											if(debug)
												response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
											if(debug)
												response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
											if(debug)
												response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
										}
										else
										{
											walletPublicKey = properties.getProperty("WalletPublicKey");
											merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
											merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
										}
										
										String userName = request.getParameter("user-name");
										
										userName = userName.replaceAll("%26", "AND");
										userName = userName.replaceAll("%20", " ");
										userName = userName.replaceAll("%2C", ",");
										userName = userName.replaceAll("%2F", "/");
										
										WalletParamMap inputParamMap = new WalletParamMap();
										
										inputParamMap.put("wallet-user-code", walletCode);
										inputParamMap.put("adhar-card-id", adharID);
										
										if(null !=request.getParameter("adhar-ref-no"))
											inputParamMap.put("adhar-ref-no", request.getParameter("adhar-ref-no"));
										
										if(null !=request.getParameter("user-name"))
											inputParamMap.put("user-name", userName);
										
										if(null !=request.getParameter("email-id"))
											inputParamMap.put("email-id", request.getParameter("email-id"));
										
										if(null !=request.getParameter("mobile-no"))
											inputParamMap.put("mobile-no", request.getParameter("mobile-no"));
					
										if(null !=request.getParameter("address-line1"))
											inputParamMap.put("address-line1", request.getParameter("address-line1"));
										
										//For debugging java application
										if(debug)
										{
											response.getWriter().println("doGetelse.wallet-user-code (mandatory): "+ request.getParameter("wallet-user-code"));
											response.getWriter().println("doGetelse.adhar-card-id: "+ request.getParameter("adhar-card-id"));
											response.getWriter().println("doGetelse.adhar-ref-no (mandatory): "+ request.getParameter("adhar-ref-no"));
											response.getWriter().println("doGetelse.user-name (mandatory): "+ request.getParameter("user-name"));
											response.getWriter().println("doGetelse.address-line1 (mandatory): "+ request.getParameter("address-line1"));
											response.getWriter().println("doGetelse.email-id (mandatory): "+ request.getParameter("email-id"));
											response.getWriter().println("doGetelse.mobile-no (mandatory): "+ request.getParameter("mobile-no"));
											
										}
										//TODO added code
										userRegisGetResponse = new JsonObject();
										WalletAPI getResponse = new WalletAPI();//"https://demo.b2biz.co.in/ws/walletAPI"
										WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, userRegistrationCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
										if(debug)
											response.getWriter().println("doGetelse.responseMap: "+ responseMap);
										
										userRegisGetResponse =	getUserRegByEpIDAndLogin(request, response, oDataUrl, userPass, epID, debug);
										if(debug)
											response.getWriter().println("doGetelse.userRegisGetResponse2: "+ userRegisGetResponse);
										uniqueGuid = userRegisGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("CommnGuid").getAsString();
										uniqueID = request.getParameter("adhar-card-id");
										
										if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0)){
											userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
											//Success Response
											walletUserCode = responseMap.get("wallet-user-code");
											status = responseMap.get("status");
											remarks = responseMap.get("remarks");
											responseDateTime = responseMap.get("response-date-time");
											checksum = responseMap.get("checksum");
											
											JsonObject result = new JsonObject();
											result.addProperty("walletUserCode", walletUserCode);
											result.addProperty("responseDateTime", responseDateTime);
											result.addProperty("status", status);
											result.addProperty("remarks", remarks);
											result.addProperty("checksum", checksum);
											response.getWriter().print(result);
										}else{
											JsonObject result = new JsonObject();
											result.addProperty("errorStatus", responseMap.get("error_status"));
											result.addProperty("errorMessage", responseMap.get("error_message")); 
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											response.getWriter().print(new Gson().toJson(result));
										}
										
										if(debug)
											response.getWriter().println("doGet.else.userUpdateResponse2: "+ userUpdateResponse);
									}
								} else {

									uniqueID = bankAPIJsonObj.get("UniqueID").getAsString();
									if(bankAPIJsonObj.get("status").getAsString().equalsIgnoreCase("Active")){
										userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, activeUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									}else{
										userUpdateResponse = updateUserRegisDetails(request, response, userRegisGetResponse, otherUserSts, uniqueGuid, oDataUrl, userPass, uniqueID, debug);
									}
									if(debug)
										response.getWriter().println("doGet.else.userUpdateResponse2: "+ userUpdateResponse);
									
									JsonObject result = new JsonObject();
									result.addProperty("walletUserCode", walletCode);
									result.addProperty("responseDateTime", "");//bankAPIJsonObj.get("responseDateTime").getAsString()
									result.addProperty("status", bankAPIJsonObj.get("status").getAsString());
									result.addProperty("remarks", "");
									result.addProperty("checksum", "");  //bankAPIJsonObj.get("checksum").getAsString()
									response.getWriter().print(new Gson().toJson(result));
								}
							}
						}
					}else
					{
						JsonObject result = new JsonObject();
						result.addProperty("errorStatus", "E195");
						result.addProperty("errorMessage", customerNo+" is not authorized to register.");
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().print(new Gson().toJson(result));
					}
				}
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", "E195");
				result.addProperty("errorMessage", message);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(new Gson().toJson(result));
			}
			
		}
		catch (Exception e)
		{ 	
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", "E195");
			result.addProperty("errorMessage", e.getLocalizedMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	public String validateParam(HttpServletRequest request, HttpServletResponse response, Properties properties, boolean debug) throws IOException
	{
		String  errorMessage="";
		
		try {
			if (null == request.getParameter("EPID") || request.getParameter("EPID").trim().length() == 0)
				errorMessage="EPID is mandatory";
			
			if (null == request.getParameter("CustomerNo") || request.getParameter("CustomerNo").trim().length() == 0)
				errorMessage= properties.getProperty("E100");
			
			if (null == request.getParameter("wallet-user-code") || request.getParameter("wallet-user-code").trim().length() == 0)
				errorMessage= "wallet-user-code is mandatory";
			
			if (null == request.getParameter("adhar-card-id") || request.getParameter("adhar-card-id").trim().length() == 0)
				errorMessage="adhar-card-id is mandatory";
			
			
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
			}
			errorMessage = properties.getProperty("E198");
			return errorMessage;
		}
		return errorMessage;
	}
	
	//TODO
	private JsonObject updateUserRegisDetails(HttpServletRequest request, HttpServletResponse response, JsonObject userRegisJson, String userRegisID, 
			String guid, String oDataUrl, String userPass, String uniqueID, boolean debug) throws IOException
	{
		
		String changedBy="",  changedAt="", executeURL="";
		long changedOnInMillis= 0;
		JSONObject updatePayload = new JSONObject();
		JsonObject updateUserResponse = new JsonObject();
		JsonObject userDetailsChildJson = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try
		{
			changedBy = commonUtils.getUserPrincipal(request, "name", response);
			changedOnInMillis = commonUtils.getCreatedOnDate();
			changedAt = commonUtils.getCreatedAtTime();
			if(debug){
				response.getWriter().println("updateUserRegisDetails.changedBy: "+ changedBy);
				response.getWriter().println("updateUserRegisDetails.changedOnInMillis: "+ changedOnInMillis);
				response.getWriter().println("updateUserRegisDetails.changedAt: "+ changedAt);
				response.getWriter().println("updateUserRegisDetails.userRegisID: "+ userRegisID);
				response.getWriter().println("updateUserRegisDetails.guid: "+ guid);
				response.getWriter().println("updateUserRegisDetails.uniqueID: "+ uniqueID);
				
			}
			if(debug)
				response.getWriter().println("updateUserRegisDetails.userRegisJson: "+ userRegisJson);
			
			userDetailsChildJson = userRegisJson.get("d").getAsJsonObject().getAsJsonArray("results").get(0).getAsJsonObject();// getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();
			if(debug)
				response.getWriter().println("updateUserRegisDetails.userDetailsChildJson: "+ userDetailsChildJson);
			
			updatePayload.accumulate("UserRegStatus", userRegisID);
			updatePayload.accumulate("AliasID", uniqueID);

			if (! userDetailsChildJson.get("CorpId").isJsonNull())
				updatePayload.accumulate("CorpId", userDetailsChildJson.get("CorpId").getAsString());
			else
				updatePayload.accumulate("CorpId", "");
			
			if (! userDetailsChildJson.get("UserId").isJsonNull())
				updatePayload.accumulate("UserId", userDetailsChildJson.get("UserId").getAsString());
			else
				updatePayload.accumulate("UserId", "");
			
			if (! userDetailsChildJson.get("AggregatorID").isJsonNull())
				updatePayload.accumulate("AggregatorID", userDetailsChildJson.get("AggregatorID").getAsString());
			else
				updatePayload.accumulate("AggregatorID", "");
			
			if (! userDetailsChildJson.get("UserRegId").isJsonNull())
				updatePayload.accumulate("UserRegId", userDetailsChildJson.get("UserRegId").getAsString());
			else
				updatePayload.accumulate("UserRegId", "");
			
			if (! userDetailsChildJson.get("LoginId").isJsonNull())
				updatePayload.accumulate("LoginId", userDetailsChildJson.get("LoginId").getAsString());
			else
				updatePayload.accumulate("LoginId", "");
			
			if (! userDetailsChildJson.get("RegistrationFor").isJsonNull())
				updatePayload.accumulate("RegistrationFor", userDetailsChildJson.get("RegistrationFor").getAsString());
			else
				updatePayload.accumulate("RegistrationFor", "");
			
			if (! userDetailsChildJson.get("CreatedBy").isJsonNull())
				updatePayload.accumulate("CreatedBy", userDetailsChildJson.get("CreatedBy").getAsString());
			else
				updatePayload.accumulate("CreatedBy", "");
			
			if (! userDetailsChildJson.get("CreatedAt").isJsonNull())
				updatePayload.accumulate("CreatedAt", userDetailsChildJson.get("CreatedAt").getAsString());
			else
				updatePayload.accumulate("CreatedAt", JSONObject.NULL);
			
			if (! userDetailsChildJson.get("CreatedOn").isJsonNull())
				updatePayload.accumulate("CreatedOn", userDetailsChildJson.get("CreatedOn").getAsString());
			else
				updatePayload.accumulate("CreatedOn", JSONObject.NULL);
			
			updatePayload.accumulate("ChangedBy", changedBy);
			updatePayload.accumulate("ChangedAt", changedAt);
			updatePayload.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
			if(debug)
				response.getWriter().print("updateUserRegisDetails.updatePayload: "+ updatePayload);
			 
			executeURL = oDataUrl+"UserRegistrations('"+guid+"')";
			if(debug)
				response.getWriter().println("updateUserRegisDetails.executeURL2: "+ executeURL);
			
			updateUserResponse = commonUtils.executeUpdate(executeURL, userPass, response, updatePayload, request, debug, "PYGWHANA");
			if(debug)
				response.getWriter().println("updateUserRegisDetails.updateUserResponse: "+ updateUserResponse);
			
			updateUserResponse.addProperty("ErrorCode", "");
			updateUserResponse.addProperty("ErrorMessage", "Update Success");
			
		}
		catch (Exception e) {
			// TODO: handle exception
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			updateUserResponse.addProperty("ErrorCode", "001");
			updateUserResponse.addProperty("ErrorMessage", e.getLocalizedMessage());
		}
		return updateUserResponse;
	}
	
	//TODO 
	private JsonObject getEscrowUserDetails(HttpServletRequest request, HttpServletResponse response,
			String walletCode, String epID, String ConfigValues, boolean debug) throws IOException, URISyntaxException
	{
		String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", EscrowUserDetailCall = "",
				clientCode = "", WSURL = "", merchantCode = "", wholeParamString="", paramName = "", paramValue = "";
		Properties properties = new Properties();
		
		try
		{
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			EscrowUserDetailCall = properties.getProperty("EscrowUserDetailCall");
			
			wholeParamString = ConfigValues;
			String[] splitResult = wholeParamString.split("\\|");
			
			//For debugging java application
			if(debug)
				response.getWriter().println("getEscrowUserDetails.wholeParamString: "+ wholeParamString);
			
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("ClientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
	        	if(paramName.equalsIgnoreCase("WSURL"))
	        	{
	        		WSURL = paramValue;
	        	}
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
			}
			if (debug) {
				response.getWriter().println("getEscrowUserDetails.clientCode: "+clientCode);
				response.getWriter().println("getEscrowUserDetails.WSURL: "+WSURL);
				response.getWriter().println("getEscrowUserDetails.merchantCode: "+merchantCode);
			}
			
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if (debug)
					response.getWriter().print("PRD Keys found ");
				if (debug)
					response.getWriter().println(clientCode+"WalletPublicKey"+ walletPublicKey);
				if (debug)
					response.getWriter().println(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
				if (debug)
					response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
			}
			else
			{
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}
		
			if(debug)
				response.getWriter().println("try ");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			inputParamMap.put("wallet-user-code", walletCode);
			if(debug){
				response.getWriter().println("wallet-user-code: " + walletCode);
				response.getWriter().println("inputParamMap: " + inputParamMap);
			}
		
			WalletAPI getResponse = new WalletAPI();
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, EscrowUserDetailCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			if(debug)
				response.getWriter().println("responseMap: " + responseMap);
			
			String errorStatus="", errorMsg="";
			if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
			{
				if(debug)
					response.getWriter().print("response found ");
				
				JsonObject result = new JsonObject();
				result.addProperty("walletusercode", responseMap.get("wallet-user-code"));
				result.addProperty("UniqueID", responseMap.get("adhar-card-id"));
				result.addProperty("remarks", responseMap.get("remarks"));
				result.addProperty("responseDateTime", responseMap.get("response-date-time"));
				result.addProperty("checksum", responseMap.get("checksum"));
				result.addProperty("status", responseMap.get("status"));
				result.addProperty("Valid", "true");
				return result;
			}
			else
			{
				if(debug)
					response.getWriter().println("response not found ");
				
				errorStatus = responseMap.get("error_status");
				errorMsg = responseMap.get("error_message");
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", errorStatus);
				result.addProperty("errorMsg", errorMsg);
				result.addProperty("Valid", "false");
//				response.getWriter().print(new Gson().toJson(result));
				return result;
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", "001");
			result.addProperty("errorMsg", e.getLocalizedMessage());
			result.addProperty("Valid", "false");
//			response.getWriter().println("Error: No Login ID found");
			return result;
		}
	}
	public JsonObject getUserRegByEpIDAndLogin(HttpServletRequest request, HttpServletResponse response, String oDataUrl, String userPass, String epId, boolean debug) throws IOException
	{
		String executeURL="", aggregatorID="", loginID="";
		JsonObject userRegisResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
	
		try {
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			if (debug) {
				response.getWriter().println("getEscrowUsers.loginID: "+loginID);
				response.getWriter().println("getEscrowUsers.oDataUrl: "+oDataUrl);
				response.getWriter().println("getEscrowUsers.aggregatorID: "+aggregatorID);
			}
			executeURL = oDataUrl+"UserRegistrations"+"?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20RegistrationFor%20eq%20%27"+epId+"%27";
			
			if (debug)
				response.getWriter().println("getUserRegByAliasAndLogin.executeURL: "+executeURL);
			
			userRegisResponse = commonUtils.executeURL(executeURL, userPass, response);
			if (debug)
				response.getWriter().println("getUserRegByAliasAndLogin.userRegisResponse: "+userRegisResponse);
		
		} catch (Exception e) {
			// TODO: handle exception
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			response.getWriter().print(e.getMessage());
		}
		return userRegisResponse;
	}
	public JsonObject getUserRegByAliasID(HttpServletRequest request, HttpServletResponse response, String oDataUrl, String userPass, String adharID, String epID, boolean debug) throws IOException
	{
		String executeURL="", aggregatorID="", loginID="";
		JsonObject userRegisResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
	
		try {
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			if (debug) {
				response.getWriter().println("getEscrowUsers.loginID: "+loginID);
				response.getWriter().println("getEscrowUsers.oDataUrl: "+oDataUrl);
				response.getWriter().println("getEscrowUsers.aggregatorID: "+aggregatorID);
			}
			executeURL = oDataUrl+"UserRegistrations"+"?$filter=AliasID%20eq%20%27"+adharID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
//			executeURL = oDataUrl+"UserRegistrations"+"?$filter=AliasID%20eq%20%27"+adharID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20UserRegStatus%20eq%20%27000002%27";
			/*if (walletCode.trim().length() > 0) {
				executeURL = oDataUrl+"UserRegistrations"+"?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20UserRegId%20eq%20%27"+walletCode+"%27";
			} else {*/
//			}
			if (debug)
				response.getWriter().println("getUserRegByAliasID.executeURL: "+executeURL);
			
			userRegisResponse = commonUtils.executeURL(executeURL, userPass, response);
			if (debug)
				response.getWriter().println("getUserRegByAliasID.userRegisResponse: "+userRegisResponse);
		
		} catch (Exception e) {
			// TODO: handle exception
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			response.getWriter().print(e.getMessage());
		}
		return userRegisResponse;
	}
	
	public JsonObject insertUserRegisDetails(HttpServletRequest request, HttpServletResponse response, String oDataUrl, String userPass, String walletCode, String uniqueKey, String uniqueGuid, String status, String epID, boolean debug) throws IOException
	{
		String executeURL="",loginId="", aggregatorID="", createdBy="",createdAt="";
		long createdInMillis = 0;
		JSONObject insertPayLoad = new JSONObject();
		JsonObject userRegisHttpResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			loginId = commonUtils.getUserPrincipal(request, "name", response);
			if (debug) {
				response.getWriter().println("insertUserRegisDetails.aggregatorID: "+aggregatorID);
				response.getWriter().println("insertUserRegisDetails.loginId: "+loginId);
			}
			createdBy= commonUtils.getUserPrincipal(request, "name", response);
			createdAt= commonUtils.getCreatedAtTime();
			createdInMillis= commonUtils.getCreatedOnDate();
			
			executeURL = oDataUrl+"UserRegistrations";
			if (debug)
				response.getWriter().println("insertUserRegisDetails.executeURL: "+executeURL);
			
			insertPayLoad.accumulate("CommnGuid", uniqueGuid);
			insertPayLoad.accumulate("AggregatorID", aggregatorID);
			insertPayLoad.accumulate("LoginId", loginId);
			insertPayLoad.accumulate("CorpId", "");
			insertPayLoad.accumulate("UserId", walletCode );
			insertPayLoad.accumulate("AliasID", uniqueKey );
			insertPayLoad.accumulate("UserRegId", walletCode );
			insertPayLoad.accumulate("UserRegStatus", status);
			insertPayLoad.accumulate("RegistrationFor", epID);
			insertPayLoad.accumulate("CreatedBy", createdBy);
			insertPayLoad.accumulate("CreatedAt", createdAt);
			insertPayLoad.accumulate("CreatedOn", "/Date("+createdInMillis+")/");
			if (debug)
				response.getWriter().println("insertUserRegisDetails.insertPayLoad: "+insertPayLoad);
			
			userRegisHttpResponse = commonUtils.executePostURL(executeURL, userPass, response, insertPayLoad, request, debug, "PYGWHANA");
			if (debug)
				response.getWriter().println("insertUserRegisDetails.userRegisHttpResponse: "+userRegisHttpResponse);
			
		} catch (Exception e) {
			// TODO: handle exception
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			response.getWriter().print(e.getMessage());
		}
		
		return userRegisHttpResponse;
	}
	
	public boolean registerWithDiffUniqueID(HttpServletRequest request, HttpServletResponse response, String newUniqueID, String configurationValues) throws  ServletException, IOException {
		String userRegistrationCall="", merchantCode="", clientCode="", walletPublicKey="", merchantPrivateKey="", merchantPublicKey="";
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			userRegistrationCall = properties.getProperty("userRegistrationCall");
			
			String wholeParamString="", paramName="", paramValue="", WSURL="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			
			//For debugging java application
			if(request.getParameter("debug") == "true")
				response.getWriter().println("registerWithDiffUniqueID.wholeParamString: "+ wholeParamString);
			
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL"))
	        	{
	        		WSURL = paramValue;
	        	}
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
			}
			
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("PRD Keys found");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
			}
			else
			{
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}
			
			String userName = request.getParameter("user-name");
			
			userName = userName.replaceAll("%26", "AND");
			userName = userName.replaceAll("%20", " ");
			userName = userName.replaceAll("%2C", ",");
			userName = userName.replaceAll("%2F", "/");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("WSURL: "+ WSURL);
			//For debugging java application
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("merchantCode: "+ merchantCode);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("WSURL: "+ WSURL);
			
			WalletParamMap inputParamMap = new WalletParamMap();
			if(null !=request.getParameter("wallet-user-code"))
				inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			
			inputParamMap.put("adhar-card-id", newUniqueID);
			
			if(null !=request.getParameter("adhar-ref-no"))
				inputParamMap.put("adhar-ref-no", request.getParameter("adhar-ref-no"));
			
			if(null !=request.getParameter("user-name"))
				inputParamMap.put("user-name", userName);
			
			if(null !=request.getParameter("address-line1"))
				inputParamMap.put("address-line1", request.getParameter("address-line1"));
			
			if(null !=request.getParameter("address-line2"))
				inputParamMap.put("address-line2", request.getParameter("address-line2"));
			
			if(null !=request.getParameter("address-line3"))
				inputParamMap.put("address-line3", request.getParameter("address-line3"));
			
			if(null !=request.getParameter("email-id"))
				inputParamMap.put("email-id", request.getParameter("email-id"));
			
			if(null !=request.getParameter("mobile-no"))
				inputParamMap.put("mobile-no", request.getParameter("mobile-no"));
			
			if(null !=request.getParameter("user-bank-name"))
				inputParamMap.put("user-bank-name", request.getParameter("user-bank-name"));
			
			if(null !=request.getParameter("user-bank-code"))
				inputParamMap.put("user-bank-code", request.getParameter("user-bank-code"));
			
			if(null !=request.getParameter("user-ifsc-code"))
				inputParamMap.put("user-ifsc-code", request.getParameter("user-ifsc-code"));
			
			if(null !=request.getParameter("user-bank-account"))
				inputParamMap.put("user-bank-account", request.getParameter("user-bank-account"));
			
			if(null !=request.getParameter("request-date-time"))
				inputParamMap.put("request-date-time", request.getParameter("request-date-time"));
			
			if(null !=request.getParameter("user-dob"))
				inputParamMap.put("user-dob", request.getParameter("user-dob"));
			
			//For debugging java application
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("wallet-user-code (mandatory): "+ request.getParameter("wallet-user-code"));
				response.getWriter().println("newUniqueID: "+ newUniqueID);
				response.getWriter().println("adhar-ref-no (mandatory): "+ request.getParameter("adhar-ref-no"));
				response.getWriter().println("user-name (mandatory): "+ request.getParameter("user-name"));
				response.getWriter().println("address-line1 (mandatory): "+ request.getParameter("address-line1"));
				response.getWriter().println("address-line2: "+ request.getParameter("address-line2"));
				response.getWriter().println("address-line3: "+ request.getParameter("address-line3"));
				response.getWriter().println("email-id (mandatory): "+ request.getParameter("email-id"));
				response.getWriter().println("mobile-no (mandatory): "+ request.getParameter("mobile-no"));
				response.getWriter().println("user-bank-name: "+ request.getParameter("user-bank-name"));
				response.getWriter().println("user-bank-code: "+ request.getParameter("user-bank-code"));
				response.getWriter().println("user-ifsc-code: "+ request.getParameter("user-ifsc-code"));
				response.getWriter().println("user-bank-account: "+ request.getParameter("user-bank-account"));
				response.getWriter().println("request-date-time: "+ request.getParameter("request-date-time"));
				response.getWriter().println("user-dob: "+ request.getParameter("user-dob"));
			}
			
			WalletAPI getResponse = new WalletAPI();//"https://demo.b2biz.co.in/ws/walletAPI"
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, userRegistrationCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			String walletUserCode="", status="", remarks="", responseDateTime="", checksum="", errorStatus="", errorMessage="";
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("1errorStatus: "+ errorStatus);
				response.getWriter().println("1errorMessage: "+ errorMessage);
				response.getWriter().println("1checksum: "+ responseMap.get("checksum"));
				response.getWriter().println("1wallet-user-code: "+ responseMap.get("wallet-user-code"));
			}
			
			if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
			{
				walletUserCode = responseMap.get("wallet-user-code");
				status = responseMap.get("status");
				remarks = responseMap.get("remarks");
				responseDateTime = responseMap.get("response-date-time");
				checksum = responseMap.get("checksum");
				
				JsonObject result = new JsonObject();
				result.addProperty("walletUserCode", walletUserCode);
				result.addProperty("responseDateTime", responseDateTime);
				result.addProperty("status", status);
				result.addProperty("remarks", remarks);
				result.addProperty("checksum", checksum);
				response.getWriter().print(new Gson().toJson(result));
				return true;
			}
			else
			{
				errorStatus = responseMap.get("error_status");
				errorMessage = responseMap.get("error_message");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("2errorStatus: "+ errorStatus);
					response.getWriter().println("2errorMessage: "+ errorMessage);
				}
				if(errorMessage != null && errorMessage.equalsIgnoreCase("Duplicate Unique ID found")){
					return false;
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("errorStatus", errorStatus);
					result.addProperty("errorMessage", errorMessage);
					response.getWriter().print(new Gson().toJson(result));
					return true;
				}
				
			}
		}catch (Exception e) {
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("error", e.getMessage());
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
			return true;
		}
	}

	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
				 
				 return "";
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000001";
			constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"' and PGCategoryID eq '"+pgCatID+"'";//PGCategoryID
			if(debug)
				response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
			
			constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
			if(sapclient != null)
			{
				constantValuesService =  destURL+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
			}
			else
			{
				constantValuesService =  destURL+constantValuesService+"?$filter="+constantValuesFilter;
			}

			if (debug){
				response.getWriter().println("pgPaymentConfigs.constantValuesService: "+constantValuesService);
				response.getWriter().println("pgPaymentConfigs.destURL: "+destURL);
			}
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			if(debug)
				response.getWriter().println("pgPaymentConfigs.proxyType: "+proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("pgPaymentConfigs.proxyHost: "+proxyHost);
			    response.getWriter().println("pgPaymentConfigs.proxyPort: "+proxyPort);
		    }
		    
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        configValuesGet = new HttpGet(constantValuesService);
	        // configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
			HttpResponse httpResponse = client.execute(configValuesGet);
	        if(debug){
		        int statusCode = httpResponse.getStatusLine().getStatusCode();
				response.getWriter().println("pgPaymentConfigs.statusCode: "+statusCode);
	        }
			
			configValuesEntity = httpResponse.getEntity();

			if(configValuesEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(configValuesEntity);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
	            NodeList WSURLList = document.getElementsByTagName("d:UserRegURL");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            
	            //For debugging java application
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				{
					response.getWriter().println("merchantCodeList: "+ merchantCodeList);
					response.getWriter().println("pgCategoryList: "+ pgCategoryList);
					response.getWriter().println("aWSURLList: "+ WSURLList);
					response.getWriter().println("pgIDList: "+ pgIDList);
				}
	            
	            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
	            {
	            	if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("pgIDList: "+pgIDList.item(i).getTextContent());
	            	if(pgIDList.item(i).getTextContent().equalsIgnoreCase("B2BIZ"))
	            	{
		            	if(PGID.equalsIgnoreCase(pgIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"
		            				+"PGID="+pgIDList.item(i).getTextContent()+"|"
		            				+"WSURL="+WSURLList.item(i).getTextContent()
		            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
//			            		configurableValues = merchantCodeList.item(i).getTextContent();
		            		break;
	        			}
	            	}
	            }
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("configurableValues: "+configurableValues);
		}
		catch (Exception e)
		{
			response.getWriter().println("Exception: "+e.getMessage());
		}
		/* finally
		{
			closableHttpClient.close();
		} */
		return configurableValues;
	}
	
	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = PUGW_DEST_NAME;
		}
		HttpDestination  destination = null;
		try {
			// look up the connectivity configuration API "DestinationFactory"
			Context ctxDestFact = new InitialContext();
			//Get HTTP destination 
			DestinationFactory destinationFactory = (DestinationFactory) ctxDestFact.lookup(DestinationFactory.JNDI_NAME);
			if(destinationFactory != null)
			{
				destination = (HttpDestination) destinationFactory.getDestination(destinationName);
			}
		} catch (NamingException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
		} 
		return destination;
	}*/
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	public String getUniqueIDForOEL(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
		String uniqueIDForOEL = "", uniqueIDFromRequest = "", walletUserCode="";
		boolean debug = false;
		if(request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		try{
			uniqueIDFromRequest = request.getParameter("adhar-card-id");
			walletUserCode = request.getParameter("wallet-user-code");
			
			if(debug){
				response.getWriter().println("uniqueIDFromRequest: "+uniqueIDFromRequest);
				response.getWriter().println("walletUserCode: "+walletUserCode);
			}
			
			
		}catch (Exception e) {
			uniqueIDForOEL = "";
		}
		return uniqueIDForOEL;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

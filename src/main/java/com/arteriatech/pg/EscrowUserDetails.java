package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.api.WalletAPI;
import com.wallet247.clientutil.bean.WalletParamMap;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import java.util.Enumeration;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.Principal;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import com.sap.cloud.sdk.cloudplatform.tenant.Tenant;
/**
 * Servlet implementation class EscrowUserDetails
 */
@WebServlet("/EscrowUserDetails")
public class EscrowUserDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EscrowUserDetails() {
        super();
        // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside doGet of EscrowUserDetails");
		String customerNo = "", epID="", loginSessionID="", beneficiaryName="", userRegID="", sSessionID="", regisFor="";
		
		boolean isValidCustomer = false, isError= false;
		boolean debug = false;
		String serveltPath="", loginID="", url="";
		
		List<Map<String, String>> configDataList = new ArrayList<>();
		Map< String, String> configDataMap = null ;
		JsonObject bankAPIObjResponse = null;
		JsonObject userRegisJsonResponse = new JsonObject();
		JsonArray bankAPIArrayResponse = new JsonArray();
		JsonObject bankAPIObjResultsResponse = new JsonObject();
		JsonObject bankAPIResponseUI = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();

		try {
			Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
			Try<Principal> currentPrincipal = PrincipalAccessor.tryGetCurrentPrincipal();
			System.out.println("currentTenant: "+currentTenant);
			System.out.println("currentPrincipal: "+currentPrincipal);
			// System.out.println("TenantID from request:"+request.getParameter("tenantId"));
			/* Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				response.getWriter().println("validateCustomer.ResponseHeader : " + header.getName()
						+ " || Value : " + header.getValue());
			} */

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				Enumeration enumr = request.getHeaderNames();  
				while (enumr.hasMoreElements()) {  
					String headerName = (String) enumr.nextElement();  
					String headerValue = request.getHeader(headerName);  
					response.getWriter().println("Header: "+headerName);  
					response.getWriter().println("Value: "+headerValue);
					// response.getWriter().println( + "<br>");  
				}  
			}

			// request.getHeader("x-arteria-source")
			serveltPath=request.getServletPath();
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
//			debug = true;
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			if (null != request.getParameter("EPID") && request.getParameter("EPID").trim().length()>0)
				epID = request.getParameter("EPID");
			else
				epID ="";
			if(debug)
				response.getWriter().println("doGet.epID: "+epID);
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			if(debug)
				response.getWriter().println("doGet.customerNo: "+customerNo);
			if(serveltPath.equalsIgnoreCase("/CorpEscrowUserDetails")){
				isValidCustomer = true;
			} else {
				/* if(debug){
					commonUtils.getUserPrincipal1(request, "name", response);
				} */
				loginID =  commonUtils.getUserPrincipal(request, "name", response);
				if(debug)
					response.getWriter().println("doGet.loginID: "+loginID);
				// if (null != request.getParameter("sessionID"))
				// 	loginSessionID = request.getParameter("sessionID");
				url = commonUtils.getDestinationURL(request, response, "URL");
				loginSessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				if(debug){
					response.getWriter().println("doGet.url: "+url);
					response.getWriter().println("doGet.loginSessionID: "+loginSessionID);
				}
				beneficiaryName = getCustomers(request, response, loginSessionID, customerNo);
				if (debug)
					response.getWriter().println("beneficiaryName: " + beneficiaryName);

				if (null != beneficiaryName && beneficiaryName.toString().trim().length() > 0) {
					isValidCustomer = true;
				}
			}	
			//TODO hardcode
//			isValidCustomer = true;
			
			if (isValidCustomer) {
					
				configDataList = getConstantValues(request, response, "", epID);
				if (debug) {
					response.getWriter().println("doGet.configDataList: " + configDataList);
					response.getWriter().println("doGet.configDataList.size(): " + configDataList.size());
				}
				
				if (configDataList.size() > 0) {

					if (debug) {
						for(int i=0; i < configDataList.size() ; i++){
							response.getWriter().println("doGet.MerchantCode ("+i+"): "+configDataList.get(i).get("MerchantCode"));
							response.getWriter().println("doGet.BankKey ("+i+"): "+configDataList.get(i).get("BankKey"));
							response.getWriter().println("doGet.ClientCode ("+i+"): "+configDataList.get(i).get("ClientCode"));
							response.getWriter().println("doGet.PGOwnPublickey ("+i+"): "+configDataList.get(i).get("PGOwnPublickey"));
							response.getWriter().println("doGet.WSURL ("+i+"): "+configDataList.get(i).get("WSURL"));
							response.getWriter().println("doGet.PGID ("+i+"): "+configDataList.get(i).get("PGID"));
						}
					}
					String appendUrl = "";
					for(int i=0 ; i<configDataList.size() ; i++){
						if(i != configDataList.size()-1)
							appendUrl = appendUrl+"RegistrationFor%20eq%20%27"+configDataList.get(i).get("PGID")+"%27%20or%20";
						else
							appendUrl = appendUrl+"RegistrationFor%20eq%20%27"+configDataList.get(i).get("PGID")+"%27";
					}
					if(debug)
						response.getWriter().println("appendUrl: " + appendUrl);
					// calling getUserRegisByPGId() for userRegiID
					JsonArray userRigsArray = new JsonArray();
					userRegisJsonResponse = getUserRegisByPGId(request, response, epID, appendUrl,serveltPath,customerNo,debug);
					userRigsArray = userRegisJsonResponse.getAsJsonObject("d").getAsJsonArray("results");
					
					if (userRigsArray.size() == 0) {
						
						if (epID.trim().length() >0)
						{
							JsonObject result = new JsonObject();
							result.addProperty("ErrorCode", "001");
							result.addProperty("UserAccountStatus", "Invalid Usercode");
							result.addProperty("EPID", epID);
							result.addProperty("Valid", "true");
							bankAPIArrayResponse.add(result);
						}else{
							for (int i = 0; i < configDataList.size(); i++) {
								
								JsonObject result = new JsonObject();
								result.addProperty("ErrorCode", "001");
								result.addProperty("UserAccountStatus", "Invalid Usercode");
								result.addProperty("Valid", "true");
								result.addProperty("EPID", configDataList.get(i).get("PGID"));
								bankAPIArrayResponse.add(result);
							}
						}
						bankAPIObjResultsResponse.add("results", bankAPIArrayResponse);
						bankAPIResponseUI.add("EscrowUserDetails", bankAPIObjResultsResponse);
						response.getWriter().println(bankAPIResponseUI);
					} else {
						if ( epID.trim().length() == 0 ) {
							
							if (debug) {
								response.getWriter().println("If part epid Not available");
							}
							for(int i=0; i < configDataList.size() ; i++)
							{
								for (int j = 0; j < userRigsArray.size(); j++) {
									
									bankAPIObjResponse = new JsonObject();
									configDataMap =  new HashMap<String, String>();
									
									if (! userRigsArray.get(j).getAsJsonObject().get("RegistrationFor").isJsonNull())
										regisFor = userRigsArray.get(j).getAsJsonObject().get("RegistrationFor").getAsString();
									if (debug)
										response.getWriter().println("doGet.regisFor: "+ regisFor);
									
									if (configDataList.get(i).get("PGID").equalsIgnoreCase(regisFor)) {
										
										if (configDataList.get(i).get("PGID").equalsIgnoreCase("B2BIZ")) {

											userRegID  =  userRigsArray.get(j).getAsJsonObject().get("UserRegId").getAsString();
											configDataMap.put("UserRegId", userRegID);
											configDataMap.put("WSURL", configDataList.get(i).get("WSURL"));
											configDataMap.put("ClientCode", configDataList.get(i).get("ClientCode"));
											configDataMap.put("MerchantCode", configDataList.get(i).get("MerchantCode"));
											configDataMap.put("EPID", configDataList.get(i).get("PGID"));
											
											bankAPIObjResponse = getEscrowUserDetails(request, response, sSessionID, configDataMap, customerNo, debug);
											if(debug)
												response.getWriter().println("doGet.bankAPIObjResponse("+i+"): "+ bankAPIObjResponse);
											
											if (bankAPIObjResponse.get("Valid").getAsString().equalsIgnoreCase("true"))
											{
												bankAPIArrayResponse.add(bankAPIObjResponse);
											}
											else
											{
												JsonObject result = new JsonObject();
												result.addProperty("ErrorCode", bankAPIObjResponse.get("errorStatus").getAsString());
												result.addProperty("ErrorMessage", bankAPIObjResponse.get("errorMsg").getAsString());
												result.addProperty("EPID", configDataList.get(i).get("PGID"));
												result.addProperty("Valid", "false");
												bankAPIArrayResponse.add(result);
//												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//												response.getWriter().println(new Gson().toJson(result));
											}
										}else{
											JsonObject result = new JsonObject();
											result.addProperty("ErrorCode", "001");
											result.addProperty("UserAccountStatus", "Invalid EPID");
											result.addProperty("EPID", configDataList.get(i).get("PGID"));
											result.addProperty("Valid", "false");
											bankAPIArrayResponse.add(result);
										}
									} 
								}
							}
							bankAPIObjResultsResponse.add("results", bankAPIArrayResponse);
							bankAPIResponseUI.add("EscrowUserDetails", bankAPIObjResultsResponse);
							response.getWriter().println(bankAPIResponseUI);
						}
						else
						{
							if (debug) {
								response.getWriter().println(" if part epid avail");
							}
							if (configDataList.get(0).get("PGID").equalsIgnoreCase(epID)) {

								if (configDataList.get(0).get("PGID").equalsIgnoreCase("B2BIZ")) {
								
									configDataMap =  new HashMap<String, String>();
									userRegID  =  userRigsArray.get(0).getAsJsonObject().get("UserRegId").getAsString();
									configDataMap.put("UserRegId", userRegID);
									configDataMap.put("WSURL", configDataList.get(0).get("WSURL"));
									configDataMap.put("ClientCode", configDataList.get(0).get("ClientCode"));
									configDataMap.put("MerchantCode", configDataList.get(0).get("MerchantCode"));
									configDataMap.put("EPID", configDataList.get(0).get("PGID"));
									
									bankAPIObjResponse = getEscrowUserDetails(request, response, sSessionID, configDataMap, customerNo, debug);
									if(debug)
										response.getWriter().println("doGet.bankAPIObjResponse: "+ bankAPIObjResponse);
									
									if (bankAPIObjResponse.get("Valid").getAsString().equalsIgnoreCase("true"))
									{
										bankAPIArrayResponse.add(bankAPIObjResponse);
									}
									else
									{
//										isError = true;
										JsonObject result = new JsonObject();
										result.addProperty("ErrorCode", bankAPIObjResponse.get("errorStatus").getAsString());
										result.addProperty("ErrorMessage", bankAPIObjResponse.get("errorMsg").getAsString());
										result.addProperty("EPID", configDataList.get(0).get("PGID"));
										result.addProperty("Valid", "false");
										bankAPIArrayResponse.add(result);
//										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//										response.getWriter().println(new Gson().toJson(result));
									}
								}
								else
								{
									JsonObject result = new JsonObject();
									result.addProperty("ErrorCode", "001");
									result.addProperty("UserAccountStatus", "Invalid EPID");
									result.addProperty("EPID", configDataList.get(0).get("PGID"));
									result.addProperty("Valid", "false");
									bankAPIArrayResponse.add(result);
								}
								
							}else
							{
								JsonObject result = new JsonObject();
								result.addProperty("ErrorCode", "001");
								result.addProperty("UserAccountStatus", "Invalid EPID");
								result.addProperty("EPID", configDataList.get(0).get("PGID"));
								result.addProperty("Valid", "false");
								bankAPIArrayResponse.add(result);
							}
							bankAPIObjResultsResponse.add("results", bankAPIArrayResponse);
							bankAPIResponseUI.add("EscrowUserDetails", bankAPIObjResultsResponse);
							response.getWriter().println(bankAPIResponseUI);
						}
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "E195");
					result.addProperty("ErrorMessage", "Configurations not maintained for the requested EPID: " + epID);
					result.addProperty("Valid", "false");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);;
					response.getWriter().println(new Gson().toJson(result));
				}
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "E100");
				result.addProperty("ErrorMessage", properties.getProperty("E100"));
				result.addProperty("Valid", "false");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);;
				response.getWriter().print(new Gson().toJson(result));
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "001");
			result.addProperty("ErrorMessage", e.getLocalizedMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);;
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	private JsonObject getEscrowUserDetails(HttpServletRequest request, HttpServletResponse response, String loginSessionID, Map<String, String> ConfigValues, String CustomerNo, boolean debug) throws IOException, URISyntaxException
	{
		String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", EscrowUserDetailCall = "", userRegId = "", epID="",
				clientCode = "", WSURL = "", merchantCode = "";
		Properties properties = new Properties();
		
		try
		{
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			EscrowUserDetailCall = properties.getProperty("EscrowUserDetailCall");
			
	        if (null != ConfigValues.get("ClientCode"))
	    		clientCode = (String)ConfigValues.get("ClientCode");
	        
	        if (null != ConfigValues.get("WSURL"))
	        	WSURL = (String)ConfigValues.get("WSURL");
	        
	        if (null != ConfigValues.get("MerchantCode"))
	        	merchantCode = (String)ConfigValues.get("MerchantCode");
	        
	        if (null != ConfigValues.get("UserRegId"))
	        	userRegId = (String)ConfigValues.get("UserRegId");
	        
	        if (null != ConfigValues.get("EPID"))
	        	epID = (String)ConfigValues.get("EPID");
	    
	        // using for-each loop for iteration over Map.entrySet() 
	        if (debug) {
	        	for (String key : ConfigValues.keySet())
	        		response.getWriter().println("getEscrowUserDetails: " +key+" : "+  ConfigValues.get(key));
	        }
			
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("PRD Keys found ");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode+"WalletPublicKey"+ walletPublicKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
			}
			else
			{
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}
		
			if(debug)
				response.getWriter().print("try ");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			inputParamMap.put("wallet-user-code", userRegId);
			if(debug){
				response.getWriter().println("wallet-user-code: " + userRegId);
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
				result.addProperty("KYCRefNo", responseMap.get("adhar-ref-no"));
				result.addProperty("UserName", responseMap.get("user-name"));
				result.addProperty("AddressLine1", responseMap.get("address-line1"));
				result.addProperty("AddressLine2", responseMap.get("address-line2"));
				result.addProperty("AddressLine3", responseMap.get("address-line3"));
				result.addProperty("EmailID", responseMap.get("email-id"));
				result.addProperty("MobileNo", responseMap.get("mobile-no"));
				result.addProperty("UserBankName", responseMap.get("user-bank-name"));
				result.addProperty("UserBankIFSCCode", responseMap.get("user-ifsc-code"));
				result.addProperty("UserBankAccountNo", responseMap.get("user-bank-account"));
				result.addProperty("RegistrationDateTime", responseMap.get("registration-date-time"));
				result.addProperty("DOB", responseMap.get("user-dob"));
				result.addProperty("VirtualAccountNumber", responseMap.get("user-van"));
				result.addProperty("UserAccountStatus", responseMap.get("status"));
				result.addProperty("EPID", epID);
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
	private JsonObject getUserRegisByPGId(HttpServletRequest request, HttpServletResponse response , String epID, String appendUrl,String servletPath,String customerNO,boolean debug) throws IOException
	{
		String loginID="", executeURL="",oDataUrl="",userName="",password="",userPass="",aggregatorID="";
		JsonObject userRegisJsonObjresponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();

		try {
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			if (debug) {
				response.getWriter().println("getUserRegisByPGId.loginID: "+loginID);
				response.getWriter().println("getUserRegisByPGId.oDataUrl: "+oDataUrl);
				response.getWriter().println("getUserRegisByPGId.userName: "+userName);
				response.getWriter().println("getUserRegisByPGId.password: "+password);
				response.getWriter().println("getUserRegisByPGId.userPass: "+userPass);
			}
			
			if (servletPath.equalsIgnoreCase("/CorpEscrowUserDetails")) {
				if (epID.trim().length() > 0)
					executeURL = oDataUrl + "UserRegistrations?$filter=UserRegId%20eq%20%27"+customerNO+"%27%20and%20UserRegStatus%20eq%20%27000002%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20RegistrationFor%20eq%20%27" + epID + "%27";
				else
					executeURL = oDataUrl + "UserRegistrations?$filter=UserRegId%20eq%20%27"+customerNO+"%27%20and%20UserRegStatus%20eq%20%27000002%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20(" + appendUrl + ")";

			} else {
				if (epID.trim().length() > 0)
					executeURL = oDataUrl + "UserRegistrations?$filter=LoginId%20eq%20%27" + loginID + "%27%20and%20UserRegStatus%20eq%20%27000002%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20RegistrationFor%20eq%20%27" + epID + "%27";
				else
					executeURL = oDataUrl + "UserRegistrations?$filter=LoginId%20eq%20%27" + loginID + "%27%20and%20UserRegStatus%20eq%20%27000002%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20(" + appendUrl + ")";
			}
			if(debug)
				response.getWriter().println("getUserRegisByPGId.executeURL: "+executeURL);
			
			userRegisJsonObjresponse = commonUtils.executeURL(executeURL, userPass, response);
			if(debug)
				response.getWriter().println("getUserRegisByPGId.userRegisJsonObjresponse: "+userRegisJsonObjresponse);
			
		} catch (Exception e) {
			if(debug){
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
		return userRegisJsonObjresponse;
	}
	private String getCustomers(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo) throws IOException, URISyntaxException
	{
		String destURL="", beneficiaryName="", userName="", password="", authParam="", authMethod="", customerService="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false, debug = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;

		try{
			Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			customerFilter = "LoginID eq '"+loginSessionID+"'";
			
			customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
			
			customerFilter = customerFilter.replaceAll("%26", "&");
			customerFilter = customerFilter.replaceAll("%3D", "=");
			
			// Context tenCtx = new InitialContext();
			// TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			// if(debug){
			// 	response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
			// 	response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
			// 	response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			// }
			
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGW_UTILS_OP);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			Destination destConfiguration = destinationAccessor.get();

			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", DestinationUtils.PCGW_UTILS_OP));
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			
			if(debug){
				response.getWriter().println("authMethod: "+authMethod);
				response.getWriter().println("destURL: "+destURL);
			}
			
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = java.util.Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();
			if(debug){
				response.getWriter().println("sapclient: "+sapclient);
			}
			// String service = destConfiguration.get("service").get().toString();
			String service = null;
			
			if(sapclient != null)
			{
//				CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
				customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
			}
			else
			{
				customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ customerFilter;
			}
			if(null != service && service.equalsIgnoreCase("SFGW")){
				if(sapclient != null)
				{
					customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
				}
				else
				{
					customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ customerFilter;
				}
			}else{
				if(sapclient != null)
				{
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
				}
				else
				{
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ customerFilter;
				}
			}
			if (debug){
				response.getWriter().println("CustomerService: "+customerService);
				response.getWriter().println("destURL: "+destURL);
			}
			
			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    // int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    /* if(debug){
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
			    if(! authMethod.equalsIgnoreCase("BasicAuthentication")){
			    	response.getWriter().println("principalPropagationHeader.getName(): "+principalPropagationHeader.getName());
			    	response.getWriter().println("principalPropagationHeader.getValue(): "+principalPropagationHeader.getValue());
			    }
		    } */
			
		    // HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        // DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        // closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        userCustomersGet = new HttpGet(customerService);
	        // userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        userCustomersGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);
	        
	        if(debug){
		        int statusCode = httpResponse.getStatusLine().getStatusCode();
		        response.getWriter().println("validateCustomer.statusCode: "+statusCode);
	        }
	        
	      //get all headers
	        if(debug){
		        Header[] headers = httpResponse.getAllHeaders();
		        for (Header header : headers) {
		        	response.getWriter().println("validateCustomer.ResponseHeader : " + header.getName()
		        	      + " || Value : " + header.getValue());
		        }
		        
		        response.getWriter().println("validateCustomer.Connection: "+httpResponse.getHeaders("Connection"));
		        response.getWriter().println("validateCustomer.Connection: "+httpResponse.getFirstHeader("Connection"));
//		        response.getWriter().println("validateCustomer.Connection: "+httpResponse.getFirstHeader("Connection").getValue());
		        response.getWriter().println("validateCustomer.Content-Encoding: "+httpResponse.getHeaders("Content-Encoding"));
		        response.getWriter().println("validateCustomer.Content-Type: "+httpResponse.getHeaders("Content-Type"));
		        response.getWriter().println("validateCustomer.dataserviceversion: "+httpResponse.getHeaders("dataserviceversion"));
		        response.getWriter().println("validateCustomer.Date: "+httpResponse.getHeaders("Date"));
		        response.getWriter().println("validateCustomer.Keep-Alive: "+httpResponse.getHeaders("Keep-Alive"));
		        response.getWriter().println("validateCustomer.Server: "+httpResponse.getHeaders("Server"));
		        response.getWriter().println("validateCustomer.Strict-Transport-Security: "+httpResponse.getHeaders("Strict-Transport-Security"));
		        response.getWriter().println("validateCustomer.Transfer-Encoding: "+httpResponse.getHeaders("Transfer-Encoding"));
		        response.getWriter().println("validateCustomer.vary: "+httpResponse.getHeaders("vary"));
		        
	        }
			
			customerEntity = httpResponse.getEntity();
			
			if(customerEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(customerEntity);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList customerCodeList = document.getElementsByTagName("d:CustomerNo");
	            NodeList customerNameList = document.getElementsByTagName("d:Name");
	            for(int i=0 ; i<customerCodeList.getLength() ; i++)
	            {
	            	if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	            		response.getWriter().println("nodeList Customer: "+customerCodeList.item(i).getTextContent());
	            	if(customerNo.equalsIgnoreCase(customerCodeList.item(i).getTextContent()))
        			{
//	            		response.getWriter().println("true");
	            		isValidCustomer = true;
	            		beneficiaryName = customerNameList.item(i).getTextContent();
//	            		return beneficiaryName;
//	            		break;
        			}
	            }
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("beneficiaryName: "+beneficiaryName);
			EntityUtils.consume(customerEntity);
			
			return beneficiaryName;
		}catch (RuntimeException e) {
//			customerRequest.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			System.out.println("RuntimeException in getCustomers: "+ e.getMessage());
			System.out.println("RuntimeException in getCustomers Full Stack Trace: "+ buffer.toString());

			return beneficiaryName;
		}catch (ParserConfigurationException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}

			if(debug){
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			System.out.println("RuntimeException in getCustomers: "+ e.getMessage());
			System.out.println("RuntimeException in getCustomers Full Stack Trace: "+ buffer.toString());

			return beneficiaryName;
		} catch (SAXException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug){
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			System.out.println("RuntimeException in getCustomers: "+ e.getMessage());
			System.out.println("RuntimeException in getCustomers Full Stack Trace: "+ buffer.toString());

			return beneficiaryName;
		} /*catch (NamingException e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}

			return beneficiaryName;
		}
		 finally
		{
			closableHttpClient.close();
		} */
	}
	private List<Map<String, String>> getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
		Map<String, String> configMap = null;
		List<Map<String, String>> configData = new ArrayList<>();
		try
		{
			// Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			// Context tenCtx = new InitialContext();
			// TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			// if(debug){
			// 	response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
			// 	response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
			// 	response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			// }
			
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGW_UTILS_OP);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", DestinationUtils.PCGW_UTILS_OP));
				 
				 return configData;
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000001";
			if (PGID.trim().length() > 0) {
				constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"' and PGCategoryID eq '"+pgCatID+"'";//PGCategoryID
			} else {
				constantValuesFilter = constantValuesFilter+"PGCategoryID eq '"+pgCatID+"'";
			}
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
			
			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// if(debug)
			// 	response.getWriter().println("pgPaymentConfigs.proxyType: "+proxyType);
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    // int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    // if(debug){
			//     response.getWriter().println("pgPaymentConfigs.proxyHost: "+proxyHost);
			//     response.getWriter().println("pgPaymentConfigs.proxyPort: "+proxyPort);
		    // }
		    
		    // HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        // DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        // closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
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
				if(debug){
					response.getWriter().println("Configvalues.retSrc: "+retSrc);
				}
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList bankKeyList = document.getElementsByTagName("d:BankKey");
	            NodeList pgOwnPublickeyList = document.getElementsByTagName("d:BankKey");
	            NodeList wsUrlList = document.getElementsByTagName("d:UserRegURL");
	            
	            if(debug){
	            	response.getWriter().println("pgIDList.getLength(): "+pgIDList.getLength());
	            	response.getWriter().println("pgCatID: "+pgCatID);
	            }
	            if(debug)
	            	response.getWriter().println("getConstantValues.if");
	            	 
	            for(int i=0 ; i<pgIDList.getLength() ; i++)
	            {
	            	configMap = new HashMap<String, String>();
        			configMap.put("MerchantCode", merchantCodeList.item(i).getTextContent());
        			configMap.put("BankKey", bankKeyList.item(i).getTextContent().toString());
        			configMap.put("ClientCode", clientCodeList.item(i).getTextContent());
        			configMap.put("PGOwnPublickey", pgOwnPublickeyList.item(i).getTextContent());
        			configMap.put("WSURL", wsUrlList.item(i).getTextContent());
        			configMap.put("PGID", pgIDList.item(i).getTextContent());
        			configData.add(i, configMap);
	            }
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("UserDetail.Exception:"+buffer.toString());
			}
		}
		finally
		{
			// closableHttpClient.close();
			return configData;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

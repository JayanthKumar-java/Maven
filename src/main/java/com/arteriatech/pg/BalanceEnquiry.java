package com.arteriatech.pg;

import java.io.IOException;
import java.util.ArrayList;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

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
import com.sap.security.um.user.UserProvider;
import com.wallet247.clientutil.api.WalletAPI;
import com.wallet247.clientutil.bean.WalletParamMap;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;

import io.vavr.control.Try;

/**
 * Servlet implementation class UserSessionServlet
 */
public class BalanceEnquiry extends HttpServlet {
//	private TenantContext  tenantContext;
	private static final long serialVersionUID = 1L;
//	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
//	private static final String SF_CUSTOMER_DEST_NAME =  "sfmst";
//	private static final String PCGW_DEST_NAME =  "pcgw";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	String servletPath="";
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.addHeader("Access-Control-Allow-Origin", "*");
		//response.setHeader("Access-Control-Allow-Methods", "GET");
		response.setHeader("Access-Control-Allow-Origin", "*");
		boolean debug=false;
		//set response type as plain text.
		response.setContentType("application/json");
		// Check for a logged in user
		LOGGER.info("1. doGet function starts");
//		CommonUtils commonUtils = new CommonUtils();
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			servletPath = request.getServletPath();
			/*
			 * Code snip for user roles
			 * */
			boolean isGroupAvailable = false;
			isGroupAvailable = readUserPrincipal(request,response);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug=true;
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", configurationValues="",merchantCode="", balanceEnquiryCall="";
			String customerNo="", application="", loginSessionID="", pgID = "", wsUrl = "", clientCode = "";
			String incomingApiKey="", arteriaApiKey="";
			boolean isValidUser = false;
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			else
				customerNo = request.getParameter("wallet-user-code");
			
			if(null != request.getParameter("Application"))
				application = request.getParameter("Application");
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
//			else
//				pgID = "B2BIZ";
			
			loginSessionID = request.getParameter("sessionID");
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("isGroupAvailable: "+isGroupAvailable);
				response.getWriter().println("servletPath: "+servletPath);
			}
			
			if(! isGroupAvailable)
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					debug = true;
					response.getWriter().println("Inside IF");
				}
				
				CommonUtils commonUtils = new CommonUtils();
				
				if(servletPath.equalsIgnoreCase("/EscrowBalanceEnquiry")){
//					isValidUser = true;
					incomingApiKey = request.getHeader("x-arteria-apikey");
					arteriaApiKey = properties.getProperty("EscrowBalance");
					/*Enumeration headerNames = request.getHeaderNames();
			        while (headerNames.hasMoreElements()) {
			            String key = (String) headerNames.nextElement();
			            String value = request.getHeader(key);
			            response.getWriter().print("Header key:   "+key);
			            response.getWriter().println("Header value: "+value);
			        }*/
					/*response.getWriter().println("incomingApiKey from header: "+incomingApiKey);*/
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
						response.getWriter().println("arteriaApiKey: "+arteriaApiKey);
						response.getWriter().println("incomingApiKey: "+incomingApiKey);
					}
					
					if(null != incomingApiKey && incomingApiKey.trim().length() > 0){
						if(incomingApiKey.equalsIgnoreCase(arteriaApiKey)){
							isValidUser = true;
						}else{
							isValidUser = false;
						}
					}else{
						isValidUser = false;
					}
					
				}else{
					String loginID = commonUtils.getLoginID(request, response, debug);
					if(debug)
						response.getWriter().println("loginID: "+loginID);

					if(loginID == null)
					{
						response.getWriter().println("No Login ID found");
					}
					else
					{
						/*
						String url = commonUtils.getDestinationURL(request, response, "URL");
						loginSessionID =  commonUtils.createUserSession(request, response, url, loginID, false);
						if(debug)
							response.getWriter().println("loginSessionID: "+loginSessionID);
						isValidUser = getCustomers(request, response, loginSessionID, customerNo);*/
						
						String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
						if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().println("authMethod:" + authMethod);
						if(authMethod.equalsIgnoreCase("BasicAuthentication")){
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("url:" + url);
							loginSessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("Generating sessionID:" + loginSessionID);
							/*if (loginSessionID.contains(" ")) {
								errorCode = "S001";
								errorMsg = sessionID;
	
								if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
									response.getWriter().println("Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
							}*/
						} else{
							loginSessionID ="";
						}
						
						isValidUser = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
					
						if(debug){
							response.getWriter().println("isValidUser: "+isValidUser);
						}
					}
				}
			}
			else
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("Inside ELSE");
				isValidUser = true;
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isGroupAvailable: "+isGroupAvailable);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isValidUser: "+isValidUser);
			
			if(isValidUser)
			{
				balanceEnquiryCall = properties.getProperty("balanceEnquiryCall");
				
				configurationValues = getConstantValues(request, response, "", pgID);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("configurationValues: "+configurationValues);
				
				String wholeParamString="", paramName="", paramValue="";
				wholeParamString = configurationValues;
				String[] splitResult = wholeParamString.split("\\|");
				for(String s : splitResult)
				{
		        	paramName = s.substring(0, s.indexOf("="));
		        	paramValue = s.substring(s.indexOf("=")+1,s.length());
		        	
		        	if(paramName.equalsIgnoreCase("MerchantCode"))
		        	{
		        		merchantCode = paramValue;
		        	}
		        	
		        	if(paramName.equalsIgnoreCase("PGID"))
		        	{
		        		pgID = paramValue;
		        	}
		        	
		        	if(paramName.equalsIgnoreCase("WSURL"))
		        	{
		        		wsUrl = paramValue;
		        	}
		        	if(paramName.equalsIgnoreCase("ClientCode"))
		        	{
		        		clientCode = paramValue.toString().trim();
		        	}
				}
				
//				walletPublicKey = properties.getProperty("walletPublicKey");
//				merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//				merchantPublicKey = properties.getProperty("merchantPublicKey");
				if(null != clientCode)
				{
					walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
					merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
					merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("PRD Keys found :");
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
				
				WalletParamMap inputParamMap = new WalletParamMap();
				if(null !=request.getParameter("wallet-user-code"))
					inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					debug=true;
				}
				if(debug){
					response.getWriter().println("Pay2corp Input Payload:"+inputParamMap);
					response.getWriter().println("wsUrl:"+wsUrl);
					response.getWriter().println("balanceEnquiryCall:"+balanceEnquiryCall);
					response.getWriter().println("walletPublicKey:"+walletPublicKey);
					response.getWriter().println("merchantCode:"+merchantCode);
					response.getWriter().println("merchantPrivateKey:"+merchantPrivateKey);
				}
				WalletAPI getResponse = new WalletAPI();
				WalletParamMap responseMap = getResponse.callWalletAPI(wsUrl, balanceEnquiryCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
				if(debug){
					response.getWriter().println("Response from Pay2Corp: "+responseMap);
				}
				String walletusercode="", userbalance="", checksum="", errorStatus="", errorMsg="";
				if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0)){
					walletusercode = responseMap.get("wallet-user-code");
					userbalance = responseMap.get("user-balance");
					checksum = responseMap.get("checksum");
					
					JsonObject result = new JsonObject();
					result.addProperty("walletusercode", walletusercode);
					result.addProperty("userbalance", userbalance);
					result.addProperty("checksum", checksum);
					result.addProperty("currency", "INR");
					result.addProperty("Valid", "true");
					response.getWriter().print(new Gson().toJson(result));
				}else{
//				response.getWriter().print("error");
					errorStatus = responseMap.get("error_status");
					errorMsg = responseMap.get("error_message");
					JsonObject result = new JsonObject();
					result.addProperty("errorStatus", errorStatus);
					result.addProperty("errorMsg", errorMsg);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				if(servletPath.equalsIgnoreCase("EscrowBalanceEnquiry")){
					JsonObject result = new JsonObject();
					result.addProperty("errorMsg", "Unauthorized request to view Escrow Account balance.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("errorMsg", customerNo+" is not authorized to view Wallet Account balance.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
		}catch (Exception e) { 
			response.getWriter().println("Error: No Login ID found");

			if(debug){
				response.getWriter().println("RuntimeException in doGet: "+e.getMessage());
				
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: "+buffer.toString());
			}
		}
//		LOGGER.info("doGet function end");	
	}
	
	private boolean readUserPrincipal(HttpServletRequest request, HttpServletResponse response)
	{
		boolean isGroupAvailable=false;
		String authTokenHeader="", authMtd="", authValue="";
		String jwtSplit[] = null;
		JsonObject jwtBody = new JsonObject();
		System.out.println( "inside readUserPrincipal for getCurrentToken:" +AuthTokenAccessor.getCurrentToken().getJwt());
		// System.out.println( "inside readUserPrincipal for getXsuaaServiceToken:" +AuthTokenAccessor.getXsuaaServiceToken().getJwt());
		System.out.println("inside readUserPrincipal for getCurrentPrincipal:"+PrincipalAccessor.getCurrentPrincipal());
		CommonUtils commonUtils = new CommonUtils();
		boolean debug= false;
		try
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;

			authTokenHeader = commonUtils.getAuthorization(request);
			if(debug)
				response.getWriter().println("readUserPrincipal.authTokenHeader: "+authTokenHeader);

			jwtSplit = authTokenHeader.split("\\s+");
		
			if(jwtSplit.length == 2){
				authMtd = jwtSplit[0];
				authValue = jwtSplit[1];
			}

			jwtBody = commonUtils.decodeJWTBody(request, response, authValue, debug);
			if(debug)
				response.getWriter().println("readUserPrincipal.jwtBody: "+jwtBody);

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("Inside readUserPrincipal");
			}
				
			JsonArray samlGroupsArray = jwtBody.get("xs.system.attributes").getAsJsonObject().get("xs.saml.groups").getAsJsonArray();
			List<String> samlGroupList = new ArrayList<String>();
			for(int i=0 ; i<samlGroupsArray.size(); i++){
				samlGroupList.add(samlGroupsArray.get(i).getAsString());
			}

			String[] samlStrArray = samlGroupList.toArray(new String[samlGroupList.size()]);
			for(String groupName : samlStrArray){
				if(groupName.equalsIgnoreCase("PY_ESCROW_SUPERUSER"))
				{
					isGroupAvailable = true;
					break;
				}
			}

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isGroupAvailable 1: "+isGroupAvailable);
		}
		catch (Exception e) {
			commonUtils.writeExceptionLogs(e, "readUserPrincipal");
		}
		finally
		{
			return isGroupAvailable;
		}
	}

	/* private boolean getCustomers(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo) throws IOException, URISyntaxException
	{
		String destURL="", userName="", password="", authParam="", authMethod="", customerService="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false, debug = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		CloseableHttpClient closableHttpClient = null;
		
		try{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			}
			
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			Destination destConfiguration = destinationAccessor.get();
			
			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				String encodedStr = new String(encodedByte);
				basicAuth = "Basic " + encodedStr;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();
			String service = destConfiguration.get("service").get().toString();
			
			if(sapclient != null)
			{
//					CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
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
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        userCustomersGet = new HttpGet(customerService);
	        userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        userCustomersGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        
	        if(debug)
	        	response.getWriter().println("validateCustomer.statusCode: "+statusCode);
			
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
	            document.getChildNodes().getLength();
	            NodeList nodeList = document.getElementsByTagName("d:CustomerNo");
//	            NodeList entryList = document.getElementsByTagName("entry");
	            for(int i=0 ; i<nodeList.getLength() ; i++)
	            {
	            	if(customerNo.equalsIgnoreCase(nodeList.item(i).getTextContent()))
        			{
	            		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	            		{
	            			response.getWriter().println("customerNo: "+customerNo);
	            			response.getWriter().println("nodeList.item(i).getTextContent(): "+nodeList.item(i).getTextContent());
	            			response.getWriter().println("true");
	            		}
	            		isValidCustomer = true;
//	            		break;
        			}
	            }
			}
			EntityUtils.consume(customerEntity);
			return isValidCustomer;
		}catch (RuntimeException e) {
//			customerRequest.abort();
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return isValidCustomer;
		}catch (ParserConfigurationException e) {
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return isValidCustomer;
		} catch (SAXException e) {
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return isValidCustomer;
		} catch (NamingException e) {
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return isValidCustomer;
		}
		finally
		{
			closableHttpClient.close();
//			return isValidCustomer;
		}
	} */
	private String getHTTPDestinationConfiguration(HttpServletRequest request, HttpServletResponse response, String destName, String property) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
			{
				destinationName = destName;
			}
		else if (destinationName == null) {
				destinationName = PCGW_UTIL_DEST_NAME;
			}
				
		String propertyValue="";
		// get all destination properties
//				Context ctxDestFact = new InitialContext();
//				ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
//				response.getWriter().println("ConnectivityConfiguration: "+ configuration);
//				DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
		Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
		Destination destConfiguration = destinationAccessor.get();
//				response.getWriter().println("DestinationConfiguration: "+ destConfiguration);
//				Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
					
		if(property.equalsIgnoreCase("sapclient")){
			//Reading SAP-Client from All Configuration.........................................................
			propertyValue = destConfiguration.get("sap-client").get().toString();
		} else if(property.equalsIgnoreCase("service")){
			propertyValue = destConfiguration.get("servicename").get().toString();
		}
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
		
		return propertyValue;
	}
	
	/*private HttpDestination getHTTPDestinationForCustomers(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = PCGW_UTIL_DEST_NAME;
		}
//		response.getWriter().println("destinationName: "+destinationName);
		HttpDestination  destination = null;
		try {
			// look up the connectivity configuration API "DestinationFactory"
//			response.getWriter().println("Inside try");
			Context ctxDestFact = new InitialContext();
//			response.getWriter().println("ctxDestFact: "+ctxDestFact.toString());
			//Get HTTP destination 
			DestinationFactory destinationFactory = (DestinationFactory) ctxDestFact.lookup(DestinationFactory.JNDI_NAME);
//			response.getWriter().println("destinationFactory: "+destinationFactory.toString());
			if(destinationFactory != null)
			{
				destination = (HttpDestination) destinationFactory.getDestination(destinationName);
			}
//			response.getWriter().println("destination: "+destination);
		} catch (NamingException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
			//response.getWriter().println("Error: " +  errorMessage);
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			//response.getWriter().println("Error: " +  errorMessage);
		} 
		return destination;
	}*/
	
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		//CloseableHttpClient closableHttpClient = null;
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
			}
 */			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			HttpDestination destConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(destConfiguration); 

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
				/* encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				String encodedStr = new String(encodedByte);
				basicAuth = "Basic " + encodedStr; */
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000002";
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
				response.getWriter().println("authMethod :"+authMethod);
			}
			
	        configValuesGet = new HttpGet(constantValuesService);
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				configValuesGet.addHeader("Authorization", "Bearer "+ authParam);
	        }else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }

			if (debug){
				response.getWriter().println("Before executing the client.execute ");
			}
	        
	        HttpResponse httpResponse = client.execute(configValuesGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("pgPaymentConfigs.statusCode: "+statusCode);
			
			configValuesEntity = httpResponse.getEntity();
			
			if(configValuesEntity != null)
			{
				configurableValues = "";
				if(PGID.equalsIgnoreCase("B2BIZ"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
					String retSrc = EntityUtils.toString(configValuesEntity);
					if(debug)
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList1 = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		           NodeList wsUrlList = document.getElementsByTagName("d:AccBalURL");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		           
		            
		           // NodeList wsUrlList = document.getElementsByTagName("d:UserRegURL");
		            
		            for(int i=0 ; i<pdIDList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            				+"|"+"WSURL="+wsUrlList.item(i).getTextContent()+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
			}
			if(debug)
				response.getWriter().println("configurableValues: "+configurableValues);
			
			return configurableValues;
			
			
			
			
			
			
			
			
			
			/*String constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			String constantValuesFilter = "";
			//PG ID 10 is for ICICI
			if(null != PGID && PGID.toString().length() > 0)
			{
//				if(PGCategoryID.equalsIgnoreCase("10"))
				{
					if(constantValuesFilter == null || constantValuesFilter == "")
					{
						//Uncomment middle line or last line when this service will be filterable . Login ID is not required for this.
//						constantValuesFilter = "LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"'";
					}
					else
					{
						//Uncomment middle line or last line when this service will be filterable. Login ID is not required for this.
//						constantValuesFilter = constantValuesFilter+" and LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"'";
					}
//					response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
					HttpDestination destination = getHTTPDestination(request, response, PCGW_UTIL_DEST_NAME);
					constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
					if(sapclient != null)
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
					}
					else
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?$filter="+constantValuesFilter;
					}
					//Uncomment the above code if filter is required. Below code is temporary one without any filters
//					constantValuesService = destination.getURI().getPath()+constantValuesService;
//					response.getWriter().println("URL: "+constantValuesService);
					HttpClient httpClient = destination.createHttpClient();
					constantRequest = new HttpGet(constantValuesService);
//					response.getWriter().println("constantRequest: "+constantRequest);
					HttpResponse constantResponse = httpClient.execute(constantRequest);
					
//					countResponse.getEntity().

					// Copy content from the incoming response to the outgoing response
					constantEntity = constantResponse.getEntity();

					if(constantEntity != null)
					{
						configurableValues = "";
						if(PGID.equalsIgnoreCase("B2BIZ"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
							String retSrc = EntityUtils.toString(constantEntity); 
	//						response.getWriter().println("retSrc: "+retSrc);
							
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList wsUrlList = document.getElementsByTagName("d:AccBalURL");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            
				            for(int i=0 ; i<pdIDList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            				+"|"+"WSURL="+wsUrlList.item(i).getTextContent()+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
					}
//					
//					response.getWriter().println("configurableValues: "+configurableValues);
//					response.getOutputStream().print(Integer.parseInt(EntityUtils.toString(constantEntity)));	
//					EntityUtils.consume(constantEntity);
//					response.getOutputStream().print
				}
			}*/
		}
		catch (Exception e)
		{
			if(debug)
				response.getWriter().println("Exception in getConfigValues: "+e.getLocalizedMessage());
			return configurableValues;
		}
		finally
		{
			//closableHttpClient.close();
//			return configurableValues;
		}
	}
	
	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != ""){
			destinationName = destName;
		} else if (destinationName == null) {
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
			//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
			//response.getWriter().println("Error: " +  errorMessage);
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			//response.getWriter().println("Error: " +  errorMessage);
		} 
		return destination;
	}*/
}

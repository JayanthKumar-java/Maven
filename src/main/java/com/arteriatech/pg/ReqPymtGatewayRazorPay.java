package com.arteriatech.pg;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.DirStateFactory.Result;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.isg.isgpay.ISGPayEncryption;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;

/**
 * Servlet implementation class ReqPymtGateway
 */
@WebServlet("/ReqPymtGatewayRazorPay")
public class ReqPymtGatewayRazorPay extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGatewayRazorPay.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReqPymtGatewayRazorPay() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.info("1. doGet function starts");
		response.setContentType("application/json");
		try
		{
			//Below for validating Customer
			String customerNo="", application="", loginSessionID="", PGCategoryID = "", configHdrGuid="", pgID="", environment="";
			boolean isValidCustomer = false;
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			if(null != request.getParameter("Application"))
				application = request.getParameter("Application");
			
			if(null != request.getParameter("PGCategoryID"))
				PGCategoryID = request.getParameter("PGCategoryID");
			else
				PGCategoryID = "000002";
			
			if(null != request.getParameter("ConfigHeaderGUID"))
				configHdrGuid = request.getParameter("ConfigHeaderGUID");
			else
				configHdrGuid = "";
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
			
			
			loginSessionID = request.getParameter("sessionID");
			
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(! pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
			{
				isValidCustomer = getCustomers(request, response, loginSessionID, customerNo, pgID);
			}
			else
			{
				isValidCustomer = true;
			}
//			isValidCustomer = true;
//			if(request.getParameter("debug").equalsIgnoreCase("true"))
//				response.getWriter().printlnln("isValidCustomer: "+isValidCustomer);
			
			if(isValidCustomer)
			{
				if(null != pgID && pgID.trim().length() > 0)
				{
					//Below code for consuming Gateway services
//					String configurationValues = getConstantValues(request, response, "", PGCategoryID);
					String configurationValues = "";
					if(! pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
					{
//						configurationValues = getConstantValues(request, response, "", pgID);
					}
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("configurationValues: "+configurationValues);
					
					if(null != request.getParameter("system"))
						environment = request.getParameter("system");
					//Below code for calling PG services
					
//					pgID = "YESPAYU";
					if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
					{
//						response.getWriter().printlnln("ICICIPGID: "+pgID);
						initiateICICIPaymentRequest(request, response, configurationValues);
					}
					//else-if added for Axis Bank PG
					else if(pgID.equalsIgnoreCase(properties.getProperty("AxisPGID")))
					{
//						response.getWriter().printlnln("AxisPGID: "+pgID);
						initiateAxisPGRequest(request, response, configurationValues);
					}
					else if(pgID.equalsIgnoreCase(properties.getProperty("YesPGID")))
					{
//						response.getWriter().printlnln("AxisPGID: "+pgID);
						initiateYesPayUPGRequest(request, response, configurationValues);
					}
					else if(pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
					{
						CommonUtils readConfig = new CommonUtils();
						Map<String, String> configMap = readConfig.readConfigValues(pgID, environment);
						
						initiateEazyPayRequest(request, response, configMap);
					}
					else if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")))
					{
						initiateRazorPayPGRequest(request, response, configurationValues);
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgReqMsg", "Invalid Payment Gateway Request.");
						result.addProperty("Valid", "false");
	//					result.addProperty("walletClientCode", customerNo);
						response.getWriter().println(new Gson().toJson(result));
					}
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", "Config Items not found");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", customerNo+" is not authorized to make a payment.");
				result.addProperty("walletClientCode", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().println(e.getMessage());
		}
	}
	
	private void initiateICICIPaymentRequest(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
			//Below code for Payment Gateway
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="",WSURL="", pgReqMsg="", pgRequestErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="", pgID="", clientCode = "";
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
//	        		response.getWriter().println("WSURL:" + WSURL);
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
					response.getWriter().println("PRD Keys found");
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
			
			
			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
			WalletParamMap payReqInputMap = new WalletParamMap();
			if(null != request.getParameter("txn-id"))
				payReqInputMap.put("txn-id", request.getParameter("txn-id"));
			if(null != request.getParameter("txn-session-id"))
				payReqInputMap.put("txn-session-id", request.getParameter("txn-session-id"));
			
			if(null != request.getParameter("wallet-user-code"))
				payReqInputMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			if(null != request.getParameter("txn-datetime"))
				payReqInputMap.put("txn-datetime", request.getParameter("txn-datetime"));
			if(null != request.getParameter("txn-amount"))
				payReqInputMap.put("txn-amount", request.getParameter("txn-amount"));
			if(null != request.getParameter("txn-for"))
				payReqInputMap.put("txn-for", request.getParameter("txn-for"));
			else
				payReqInputMap.put("txn-for", paymentRequestCall);
			
			if(null != request.getParameter("return-url"))
				payReqInputMap.put("return-url", request.getParameter("return-url"));
			if(null != request.getParameter("cancel-url"))
				payReqInputMap.put("cancel-url", request.getParameter("cancel-url"));
			if(null != request.getParameter("challan-expiry-date"))
				payReqInputMap.put("challan-expiry-date", request.getParameter("challan-expiry-date"));
			
			/*
			 * Additional parameters 1 to 4 are used for getting redirect url params based on which we'll form redirect url in the response servlet
			 * site-id : eg: SSLaunchpad
			 * application-id : eg: ssoutstanding1pg
			 * action-name: eg: Display
			 * nav-param: eg: Search
			 * Please note that these parameter values should not have length more than 25 characters
			 */
			
			if(null != request.getParameter("site-id"))
				payReqInputMap.put("additional-param1", request.getParameter("site-id"));
			else
				payReqInputMap.put("additional-param1", "SSLaunchpad");//Need to change it back to NA after UI5 changes are done
			
			if(null != request.getParameter("application-id"))
				payReqInputMap.put("additional-param2", request.getParameter("application-id"));
			else
				payReqInputMap.put("additional-param2", "sfoutstdnginvs1pg");//Need to change it back to NA after UI5 changes are done
			
			if(null != request.getParameter("action-name"))
				payReqInputMap.put("additional-param3", request.getParameter("action-name"));
			else
				payReqInputMap.put("additional-param3", "Display");//Need to change it back to NA after UI5 changes are done
			
			if(null != request.getParameter("nav-param"))
				payReqInputMap.put("additional-param4", request.getParameter("nav-param"));
			else
				payReqInputMap.put("additional-param4", "Search");//Need to change it back to NA after UI5 changes are done
			
			if(null != request.getParameter("additional-param5"))
				payReqInputMap.put("additional-param5", request.getParameter("additional-param5"));
			else
				payReqInputMap.put("additional-param5", "NA");
			if(null != request.getParameter("additional-param6"))
				payReqInputMap.put("additional-param6", request.getParameter("additional-param6"));
			else
				payReqInputMap.put("additional-param6", "NA");
			if(null != request.getParameter("additional-param7"))
				payReqInputMap.put("additional-param7", request.getParameter("additional-param7"));
			else
				payReqInputMap.put("additional-param7", "NA");
			if(null != request.getParameter("additional-param8"))
				payReqInputMap.put("additional-param8", request.getParameter("additional-param8"));
			else
				payReqInputMap.put("additional-param8", "NA");
			if(null != request.getParameter("additional-param9"))
				payReqInputMap.put("additional-param9", request.getParameter("additional-param9"));
			else;
				payReqInputMap.put("additional-param9", "NA");
			if(null != request.getParameter("pay-for"))
				payReqInputMap.put("additional-param10", request.getParameter("pay-for"));
			else
				payReqInputMap.put("additional-param10", "NA");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				System.out.println("payReqInputMap: "+payReqInputMap.toString());
			
			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setRequestMap(payReqInputMap);
			walletMessageBean.setWalletKey(walletPublicKey);
			
			walletMessageBean.setClientKey(merchantPrivateKey);
			//System.out.println(merchantPrivateKey);
//			response.getWriter().printlnln("code: "+request.getParameter("wallet-user-code")+"|||merchantCode: "+merchantCode);
			pgReqMsg = walletMessageBean.generateWalletRequestMessage();

			if(pgReqMsg != null && pgReqMsg.trim().length() > 0)
			{
//				response.getWriter().println("IF Condition");
				JsonObject result = new JsonObject();
				result.addProperty("walletRequestMessage", pgReqMsg);
				result.addProperty("RawPayload", payReqInputMap.toString());
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", WSURL);
//				result.addProperty("WSURL","https://demo.b2biz.co.in/ws/payment");
				result.addProperty("parameters","walletClientCode|walletRequestMessage");
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
//				response.getWriter().println("ELSE Condition");
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgRequestErrorMsg);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().print("initiateICICIPaymentRequest Error: "+e.getMessage());
		}
	}
	
	private boolean getCustomers(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo, String pgID) throws IOException, URISyntaxException
	{/*
		HttpGet customerRequest = null;
		HttpEntity customerEntity = null;
		boolean isValidCustomer = false;

		try{
			String CustomerService = "";
			String CustomerFilter = "";
				
			CustomerFilter = "LoginID eq '"+loginSessionID+"'";
			
			CustomerFilter = URLEncoder.encode(CustomerFilter, "UTF-8");
			
			CustomerFilter = CustomerFilter.replaceAll("%26", "&");
			CustomerFilter = CustomerFilter.replaceAll("%3D", "=");
//			response.getWriter().println("CustomerFilter: "+CustomerFilter);
			
			String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME, "sapclient");
			String service = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME, "service");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("service: "+service);
			
			HttpDestination destination = getHTTPDestinationForCustomers(request, response, PCGW_UTIL_DEST_NAME);
			if(pgID.equalsIgnoreCase("AXISPG"))
			{
//				response.getWriter().println("JKT");
				if(sapclient != null)
				{
					CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient;
				}
				else
				{
					CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers";
				}
			}
			else
			{
				if(null != service && service.equalsIgnoreCase("SFGW")){
					if(sapclient != null)
					{
						CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ CustomerFilter;
					}
					else
					{
						CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
					}
				}else{
					if(sapclient != null)
					{
						CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ CustomerFilter;
					}
					else
					{
						CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ CustomerFilter;
					}
				}
			}

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("CustomerService: "+CustomerService);
			HttpClient httpClient = destination.createHttpClient();
			customerRequest = new HttpGet(CustomerService);
			HttpResponse customerResponse = httpClient.execute(customerRequest);

			// Copy content from the incoming response to the outgoing response
			customerEntity = customerResponse.getEntity();
//			response.getWriter().println("customerEntity: "+customerEntity);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("customerEntity: "+customerEntity);
			
			if(customerEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(customerEntity);
//				response.getWriter().println("retSrc: "+retSrc);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: "+retSrc);
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList nodeList = document.getElementsByTagName("d:CustomerNo");
	            for(int i=0 ; i<nodeList.getLength() ; i++)
	            {
	            	if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	            		response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
//	            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(customerNo.equalsIgnoreCase(nodeList.item(i).getTextContent()))
        			{
	            		isValidCustomer = true;
	            		break;
        			}
	            }
			}
//			response.getWriter().println("isValidCustomer: "+isValidCustomer);
//			response.getWriter().println("customerFromService: "+customerFromService);
			EntityUtils.consume(customerEntity);
		}catch (RuntimeException e) {
			customerRequest.abort();
		} catch (DestinationException e) {
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			return isValidCustomer;
		}
	*/
		return true;	
	}
	
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
//				response.getWriter().println("DestinationConfiguration: "+ destConfiguration);
//				Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
		Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
		Destination destConfiguration = destinationAccessor.get();
					
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
	
//	For consuming gateway services
	private String getUsersession(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		//set response type as plain text.
//		response.getWriter().println("inside getUsersession method");
		response.setContentType( "text/plain" );
		String loginSessionID = "";
		// Check for a logged in user
		// response.getWriter().println("request.getUserPrincipal()"+request.getUserPrincipal());
		String loginID = getLoginID(request, response);
		// if (request.getUserPrincipal() != null) 
		if (loginID != null) 
		{
			response.getWriter().println("if");
			//call getLoginID to get loginID
			
			response.getWriter().println("loginID: "+loginID);
			//get destination URL
			String url = getDestinationURL(request, response);
			//create user session
//			loginSessionID = createUserSession(request, response, url, loginID);
		}else{
			// return error message if principal user is null
			response.getWriter().println("Error: No Login ID found");
		}
		return loginSessionID;
	}
	
	/*private String createUserSession(HttpServletRequest request, HttpServletResponse response, String url, String loginID) throws IOException{
		//get soap xml string
		String soapXML = getSoapXML(request, loginID);
		String loginSessionID = "";
		try{
			//Creates a StringEntity with the specified content and charset. The MIME type to UTF-8.
			StringEntity stringEntity = new StringEntity(soapXML, "UTF-8");
			stringEntity.setChunked(true);
			//get http destination
			HttpDestination  destination = getHTTPDestination(request, response, "");
			if(destination != null)
			{
				//Execute http post and get entity object
				loginSessionID = executeHttpPost(destination, stringEntity, url, response);
				//set httppost response to the servlet response
//				if(entity != null)
//				{
//					String retSrc = EntityUtils.toString(entity); 
//					response.getWriter().println("retSrc: "+retSrc);
//					if(retSrc.contains("<UserSession>")) {
//						loginSessionID = retSrc.split("<UserSession>")[1].split("</UserSession>")[0];
//					}
//				}
			}else {
				loginSessionID = "";
			}
		}catch(Exception e){
			// Handle errors 
			response.getWriter().println("createUserSession Error: " +  e.getMessage());
		}
		return loginSessionID;
	}*/
	
	private String getLoginID(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String loginID = null;
		try 
		{
			if (PrincipalAccessor.getCurrentPrincipal() != null){
				loginID = PrincipalAccessor.getCurrentPrincipal().getPrincipalId();
			}else{
				loginID = "";
			}
			response.getWriter().println("getLoginID: loginID: "+loginID);
		}catch (Exception e) { 
			response.getWriter().println("getLoginID Error: " +  e.getMessage());
		}
		return loginID;
	}
	
	private String getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if (destinationName == null) {
			destinationName = PUGW_DEST_NAME;
		}
		String url = null;
		// look up the connectivity configuration API "connectivityConfiguration"
//			Context ctxConnConfgn = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
		// get destination configuration for "myDestinationName"
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
		Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(destinationName, options);
		Destination destConfiguration = destinationAccessor.get();
		if(destConfiguration != null)
		{
			// Get the destination URL
			url = destConfiguration.get("URL").get().toString();
		} 
		return url;
	}

	private String getSoapXML(HttpServletRequest request, String loginID){
		//Get request parameters
		String app = request.getParameter("application");

		if(app == null){
			app = "PD";
		}
		String objectName = request.getParameter("Object");
		if(objectName  == null){
			objectName = "";
		}
		String methodType = request.getParameter("Method");
		if(methodType == null){
			methodType = "";
		}
		String fmName = request.getParameter("FmName");
		if(fmName  == null){
			fmName = "";
		}
		String isTestRun = request.getParameter("IsTestRun");
		if(isTestRun  == null){
			isTestRun = "";
		}
		//create soap xml
		String soapXML =  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:sap-com:document:sap:soap:functions:mc-style\">" 
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<urn:USRSSNCREATE>"
				+ "<Application>"+app+"</Application>"
				+ "<!--Optional:-->"
				+ "<IsTestRun>"+isTestRun+"</IsTestRun>"
				+ " <LoginID>"+loginID+"</LoginID>"
				+ "<!--Optional:-->"
				+ " <UsrSsnCrtDtl>"
				+ " <Object>"+objectName+"</Object>"
				+ " <Method>"+methodType+"</Method>"
				+ " <FmName>"+fmName+"</FmName>"
				+ " </UsrSsnCrtDtl>"
				+ "</urn:USRSSNCREATE>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		return soapXML;
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
	
	@SuppressWarnings("finally")
	/*private String executeHttpPost(HttpDestination destination, StringEntity stringEntity, String url, HttpServletResponse response) throws IOException{
		String loginSessionID = "";
		HttpPost httpPost = null;
		HttpEntity entity = null;
		try{
			// Create HTTP client
			HttpClient httpClient = destination.createHttpClient();
			httpPost = new HttpPost(url);
			httpPost.setEntity(stringEntity);
			httpPost.setHeader("content-type", "text/xml; charset=UTF-8");
//			httpPost.addHeader("Accept", "text/xml");
			// Execute HTTP request
			HttpResponse httpResponse = httpClient.execute(httpPost);
			// Copy content from the incoming response to the outgoing response
			entity = httpResponse.getEntity();
			if(entity != null)
			{
				String retSrc = EntityUtils.toString(entity); 
				if(retSrc.contains("<UserSession>")) {
					loginSessionID = retSrc.split("<UserSession>")[1].split("</UserSession>")[0];
				}
			}
			EntityUtils.consume(entity);
		}catch (RuntimeException e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpPost.abort();
			response.getWriter().println("executeHttpPost Error: " +  e.getMessage());
		} catch (DestinationException e) {
			response.getWriter().println("executeHttpPost Error: " +  e.getMessage());
		}
		
		return loginSessionID;
	}*/
	
	/*@SuppressWarnings("finally")
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGCategoryID) throws IOException, URISyntaxException
	{
		HttpGet constantRequest = null;
		HttpEntity constantEntity = null;
		String configurableValues = "", pgID="";
		pgID = PGCategoryID;
		String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME, "sapclient");
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			response.getWriter().println("pgID: "+pgID);
		try
		{
			String constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			String constantValuesFilter = "";
			if(null != PGCategoryID && PGCategoryID.toString().length() > 0)
			{
//				if(PGCategoryID.equalsIgnoreCase("10"))
				{
					if(constantValuesFilter == null || constantValuesFilter == "")
					{
						constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"'";
					}
					else
					{
						//Uncomment middle line or last line when this service will be filterable. Login ID is not required for this.
						constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"'";
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
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("constantValuesService: "+constantValuesService);

					HttpClient httpClient = destination.createHttpClient();
					constantRequest = new HttpGet(constantValuesService);
//					response.getWriter().println("constantRequest: "+constantRequest);
					HttpResponse constantResponse = httpClient.execute(constantRequest);
					
					// Copy content from the incoming response to the outgoing response
					constantEntity = constantResponse.getEntity();

					if(constantEntity != null)
					{
						configurableValues = "";
						if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						{
							response.getWriter().println("constantEntity is not null");
							response.getWriter().println("PGCategoryID: "+PGCategoryID);
						}
						
						if(PGCategoryID.equalsIgnoreCase("B2BIZ"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
				            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
				            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()
				            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("AXISPG"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
				            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
				            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
				            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
				            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
				            NodeList secretCode = document.getElementsByTagName("d:ClientCode");
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
				            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
				            								+"|"+"Type="+pgParameter2.item(i).getTextContent()
				            								+"|"+"RE1="+pgParameter3.item(i).getTextContent()
				            								+"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent()
				            								+"|"+"secretCode="+secretCode.item(i).getTextContent()
				            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("YESPAYU"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
				            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
				            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
				            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
				            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
				            NodeList pgParameter5 = document.getElementsByTagName("d:PGParameter5");
				            NodeList pgOwnPublicKey = document.getElementsByTagName("d:PGOwnPublickey");
				            NodeList pgOwnPrivateKey = document.getElementsByTagName("d:PGOwnPrivatekey");
				            NodeList bankKey = document.getElementsByTagName("d:BankKey");
				            NodeList clientCode = document.getElementsByTagName("d:ClientCode");
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
//				            	response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
				            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
				            								+"|"+"PassCode="+pgParameter2.item(i).getTextContent()
				            								+"|"+"MCC="+pgParameter3.item(i).getTextContent()
				            								+"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
				            								+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent()
				            								+"|"+"BankId="+bankKey.item(i).getTextContent()
				            								+"|"+"TerminalId="+clientCode.item(i).getTextContent()
				            								+"|"+"Currency="+pgParameter4.item(i).getTextContent()
				            								+"|"+"TxnType="+pgParameter5.item(i).getTextContent()
				            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("EAZYPAY"))
						{
							DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            NodeList secretKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
				            
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
				            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
				            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
				            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent()+
				            				"|"+"SecretKey="+secretKeyList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("RAZOR"))
						{
							DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder docBuilder;
							InputSource inputSource;
												       
												        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
							Document document = docBuilder.parse(inputSource);
							NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
							NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
							NodeList pdIDList = document.getElementsByTagName("d:PGID");
							NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
							NodeList clientCode = document.getElementsByTagName("d:ClientCode");
							NodeList pgOwnPrivateKey = document.getElementsByTagName("d:PGOwnPrivatekey");
											          
							for(int i=0 ; i<pgCategoryList.getLength() ; i++)
							{
//							response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
							if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
							{
								configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
											          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
											            	+"|"+"WSURL="+aWSURLList.item(i).getTextContent()
											            	+"|"+"d:PGCategoryID="+pgCategoryList.item(i).getTextContent()
											            	+"|"+"APIKey="+clientCode.item(i).getTextContent()
											            	+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent();
											            								
								break;
							}
							}
						}
					}
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("1 getConstantValues: "+configurableValues);
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		finally
		{
			return configurableValues;
		}
	}*/
	
	private void initiateAxisPGRequest(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{/*
//			request.getHeader(name)
			//Below code for Payment Gateway
			String encryptionKey = "", merchantCode="", coopParam="", pgType="", re1="", parameterData="", version="", axisCurrency="", keyValue="", sWSURL="";
			String referenceID="", customerRefNo="", payAmount="", returnURL="", ppiParameter="", retParameter2="", retParameter3="", retParameter4="", retParameter5="", checksum="";
			String encryptedData="";
			boolean isParamMissing = false;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="", pgID="", secretCode="";
			wholeParamString = configurationValues;
//			response.getWriter().println("configurationValues: "+configurationValues);
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
//	        		response.getWriter().println("merchantCode: "+merchantCode);
	        	}
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
//	        		response.getWriter().println("pgID: "+pgID);
	        	}
	        	if(paramName.equalsIgnoreCase("WSURL"))
	        	{
	        		sWSURL = paramValue;
//	        		response.getWriter().println("sWSURL: "+sWSURL);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Version"))
	        	{
	        		version = paramValue;
//	        		response.getWriter().println("version: "+version);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Type"))
	        	{
	        		pgType = paramValue;
//	        		response.getWriter().println("pgType: "+pgType);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("RE1"))
	        	{
	        		re1 = paramValue;
//	        		response.getWriter().println("re1: "+re1);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("secretCode"))
	        	{
	        		secretCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey"))
	        	{
//	        		encryptionKey = paramValue;
	        		if(pgType.equalsIgnoreCase("TEST"))
	        		{
	        			encryptionKey = properties.getProperty("axisEncryptionKeyUAT");
	        			keyValue = properties.getProperty("checkSumKeyUAT");
	        		}
	        		else
	        		{
	        			encryptionKey = properties.getProperty("axisEncryptionKeyPRD");
	        			keyValue = properties.getProperty("checkSumKeyPRD");
	        		}
	        	}
			}
//			response.getWriter().println("encryptionKey: "+encryptionKey);
//			response.getWriter().println("checksumkey: "+keyValue);
			axisCurrency = properties.getProperty("axisCurrency");
//			response.getWriter().println("Axis method configurationValues: "+configurationValues);
			
			if(null != request.getParameter("txn-id"))
			{
				referenceID = request.getParameter("txn-id");
//				referenceID = "123456789";
				long timeSeed = System.nanoTime(); // to get the current date time value

		        double randSeed = Math.random() * 1000; // random number generation

		        long midSeed = (long) (timeSeed * randSeed); 
		        String s = midSeed + "";
		        String subStr = s.substring(0, 9);

		        int finalSeed = Integer.parseInt(subStr);
		        referenceID = ""+finalSeed;
//		        referenceID = "850510583";
//		        response.getWriter().println("referenceID: "+referenceID);
			}
			else
			{
				isParamMissing = true;
			}
			
			if(null != request.getParameter("CustomerNo"))
			{
				customerRefNo = request.getParameter("CustomerNo");
			}
			else
			{
				isParamMissing = true;
			}
			
			if(null != request.getParameter("txn-amount"))
			{
				payAmount = request.getParameter("txn-amount");
			}
			else
			{
				isParamMissing = true;
			}
			
			if(null != request.getParameter("return-url"))
			{
				returnURL = request.getParameter("return-url");
			}
			else
			{
				isParamMissing = true;
			}
			
			if(! isParamMissing)
			{
				ppiParameter = referenceID+"|"+customerRefNo+"|"+payAmount;
			}
			
			if(null != request.getParameter("RE2"))
			{
				retParameter2 = request.getParameter("RE2");
			}
			else
			{
				retParameter2 = "";
			}
			
			if(null != request.getParameter("RE3"))
			{
				retParameter3 = request.getParameter("RE3");
			}
			else
			{
				retParameter3 = "";
			}
			
			if(null != request.getParameter("RE4"))
			{
				retParameter4 = request.getParameter("RE4");
			}
			else
			{
				retParameter4 = "";
			}
			
			if(null != request.getParameter("RE5"))
			{
				retParameter5 = request.getParameter("RE5");
			}
			else
			{
				retParameter5 = "";
			}
			
			if(! isParamMissing)
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("isParamMissing: "+isParamMissing);
					response.getWriter().println("secretCode: "+secretCode);
					response.getWriter().println("merchantCode: "+merchantCode);
				}
					
				
				coopParam = encryptDataForAxisPG(merchantCode, secretCode);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("coopParam: "+coopParam);
				
				String checkSumInput = merchantCode+referenceID+customerRefNo+payAmount+keyValue;
				checksum = generateCheckSum(checkSumInput);
//				response.getWriter().println("checksum: "+checksum);
				
				parameterData = parameterData+"CID="+merchantCode+"&RID="+referenceID+"&CRN="+customerRefNo+"&AMT="+payAmount+"&VER="+version+"&TYP="+pgType+"&CNY="+axisCurrency+"&RTU="+returnURL+"&PPI="+ppiParameter+"&RE1="+re1+"&RE2="+retParameter2+"&RE3="+retParameter3+"&RE4="+retParameter4+"&RE5="+retParameter5+"&CKS="+checksum;
//				response.getWriter().println("parameterData: "+parameterData);
				
				encryptedData = encryptDataForAxisPG(parameterData, encryptionKey);
//				response.getWriter().println("encryptedData: "+encryptedData);
				
				if(encryptedData != null && encryptedData.trim().length() > 0)
				{
					JsonObject result = new JsonObject();
					result.addProperty("i", encryptedData);
					result.addProperty("coop", coopParam);
					result.addProperty("Valid", "true");
//					result.addProperty("WSURL","https://uat-etendering.axisbank.co.in/easypay2.0/frontend/index.php/api/payment");
					result.addProperty("WSURL", sWSURL);
					result.addProperty("parameters","i|coop");
					result.addProperty("payLoad",parameterData);
					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("Valid", "false");
					result.addProperty("pgReqMsg", "Error in processing the request. Please contact system admin.");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("ParamsMissing", "Some mandatory parameters are missing");
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		*/}
		catch (Exception e)
		{
			response.getWriter().print("initiateAxisPGRequest Error: "+e.getMessage());
		}
	}
	
	/*public String encryptDataForAxisPG(String parameterData, String encryptionKey)
	{
		byte[] encrypted = null;
		try
		{
			SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			encrypted = cipher.doFinal(parameterData.getBytes());
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		finally
		{
			return new String(Base64.encode(encrypted));
		}
	}*/
	
	public String generateCheckSum(String checkSumInput) throws NoSuchAlgorithmException
	{
		StringBuffer hexString = null;
		try
		{
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(checkSumInput.getBytes());
	 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format method 2
	        hexString = new StringBuffer();
	    	for (int i=0;i<byteData.length;i++)
	    	{
	    		String hex=Integer.toHexString(0xff & byteData[i]);
	   	     	if(hex.length()==1) hexString.append('0');
	   	     		hexString.append(hex);
	    	}
//	    	System.out.println("Hex format : " + hexString.toString());
	    	return  hexString.toString();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally
		{
			return  hexString.toString();
		}
	}
	
	public String generateOrderID(HttpServletRequest request, HttpServletResponse response, boolean debug)  throws IOException, JSONException 
	{
		if(debug)
			response.getWriter().println("generateOrderID");
		String apiKey="", secret="", orderID="";
		try{
			apiKey = "rzp_test_UpMmJk4TXSwHgM";
			secret =  "UgVNyquPkxVG60Wx8wXt6HbU";
			if(debug){
				response.getWriter().println("apiKey: "+apiKey);
				response.getWriter().println("secret: "+secret);
			}
			
			RazorpayClient razorpayClient = new RazorpayClient(apiKey, secret);
//			RazorpayClient.
//			OrderClient client = new OrderClient();
			  
			JSONObject orderRequest = new JSONObject();
//			// amount in paise
			orderRequest.put("amount", 50000);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "RCPTID43");
			orderRequest.put("payment_capture", true);
			  
			Order order = razorpayClient.Orders.create(orderRequest);
				 
			if(order.has("id")){
				orderID = order.get("id").toString();
			}else {
				orderID = "";
			}
			
			if(debug){
				response.getWriter().println("order: "+order);
				response.getWriter().println("order.toJson: "+order.toJson());
				response.getWriter().println("order.toString: "+order.toString());
				  
				response.getWriter().println("amount:"+order.get("amount"));
				response.getWriter().println("currency:"+order.get("currenct"));
				response.getWriter().println("receipt:"+order.get("receipt")); 
				response.getWriter().println("payment_capture:"+order.get("payment_capture"));
				
				response.getWriter().println("orderID: "+orderID);
			}
		    
			/*Order order1 = razorpayClient.Orders.fetch("order_id");
			List<Order> orders = razorpayClient.Orders.fetchAll();
			List<Payment> payments = razorpayClient.Orders.fetchPayments("order_id");*/
			   
		}catch(RazorpayException e){
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("RazorpayException:"+buffer.toString());
		} catch (JSONException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("JSONException:"+buffer.toString());
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception:"+buffer.toString());
		}
		
		return orderID;
	}

	private void initiateRazorPayPGRequest(HttpServletRequest request, HttpServletResponse response,String configurationValues1) throws IOException, JSONException 
	{
		JSONObject orderJsonObj = new JSONObject();
		String keyID = "", cmpName = "", txnFor = "", orderID = "",returnURL = "", txnAmount = "", txnID = "";
		boolean isParamMissing = false, debug = false;
		
		try
		{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			debug = false;
			keyID = properties.getProperty("apiKey");
			cmpName = "Grasim";
			
			if(null != request.getParameter("txn-amount"))
			{
				txnAmount = request.getParameter("txn-amount");
			}
			else 
			{
				isParamMissing = true;
			}
			if(null != request.getParameter("txn-id"))
			{
				txnID = request.getParameter("txn-id");
			}
			else 
			{
				isParamMissing = true;
			}
			if(null != request.getParameter("return-url"))
			{
				returnURL = request.getParameter("return-url");
			}
			else 
			{
				isParamMissing = true;
			}
			if(null != request.getParameter("txn-for"))
			{
				txnFor = request.getParameter("txn-for");
			}
			else 
			{
				isParamMissing = true;
			}
			if(debug)
			{
				response.getWriter().println("apiKey: "+keyID);
				response.getWriter().println("returnURL: "+returnURL);
				response.getWriter().println("Description: "+txnFor);
				response.getWriter().println("CompanyName: "+cmpName);
				response.getWriter().println("isParamMissing: "+isParamMissing);
			}
			if(! isParamMissing)
			{
				orderID = generateOrderID(request,response, debug);
				
				if(debug)
					response.getWriter().println("orderJsonObj: "+orderJsonObj);
				
				if(orderID != null && orderID.trim().length() > 0)
				{
					String wholeParamString="", paramName="", paramValue="", pgID="",  WSURL = "",merchantCode="", clientCode = "";
					
					/*wholeParamString = configurationValues;
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
							WSURL = paramValue;
						}
						if(paramName.equalsIgnoreCase("clientCode"))
						{
							clientCode = paramValue;
						}
					}*/
					WSURL = "https://api.razorpay.com/v1/checkout/embedded";
					if(debug)
					{
						response.getWriter().println("merchantCode: "+merchantCode);
						response.getWriter().println("PGID: "+pgID);
						response.getWriter().println("WSURL: "+WSURL);
						response.getWriter().println("clientCode: "+clientCode);
					}
					String name="Sujai", contact="9611060612", email="sujaikareik@gmail.com";
					JsonObject result = new JsonObject();
					result.addProperty("key_id", keyID); 
					result.addProperty("name", cmpName);
					result.addProperty("description",txnFor);
					result.addProperty("order_id",orderID);
					result.addProperty("callback_url",returnURL);
					result.addProperty("prefill[name]",name);
					result.addProperty("prefill[contact]",contact);
					result.addProperty("prefill[email]",email);
					result.addProperty("WSURL",WSURL);
					result.addProperty("parameters","key_id|name|description|order_id|callback_url|prefill[name]|prefill[contact]|prefill[email]");
					result.addProperty("Valid",true);
					response.getWriter().print(new Gson().toJson(result));
				}
				else 
				{			
					JsonObject result = new JsonObject();
					result.addProperty("Error", "Unable to generate Order ID. Please try again later");
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("ParamsMissing", "Some mandatory parameters are missing");
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			orderJsonObj.accumulate("ErrorCode", "001");
			orderJsonObj.accumulate("ErrorMessage", e.getLocalizedMessage());
			response.getWriter().print(orderJsonObj);
		}
	}
	
	private void initiateYesPayUPGRequest(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
	        String sEncryptionKey="", sSecureSecret="", version="", sWSURL="", txnRefNo="", txnAmount="", passCode="", bankID="", terminalID="", merchantCode="", mcc="", currency="", txnType="", returnURL="", amountExponent="";
			version="1";
			
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
//			response.getWriter().println("configurationValues: "+configurationValues);
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
//	        		response.getWriter().println("merchantCode: "+merchantCode);
	        	}
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
//	        		response.getWriter().println("pgID: "+pgID);
	        	}
	        	if(paramName.equalsIgnoreCase("WSURL"))
	        	{
	        		sWSURL = paramValue;
//	        		response.getWriter().println("sWSURL: "+sWSURL);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Version"))
	        	{
	        		version = paramValue;
//	        		response.getWriter().println("version: "+version);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PassCode"))
	        	{
	        		passCode = paramValue;
//	        		response.getWriter().println("passCode: "+passCode);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("MCC"))
	        	{
	        		mcc = paramValue;
//	        		response.getWriter().println("mcc: "+mcc);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey"))
	        	{
	        		sEncryptionKey = paramValue;
//	        		response.getWriter().println("sEncryptionKey: "+sEncryptionKey);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecureSecret"))
	        	{
	        		sSecureSecret = paramValue;
//	        		response.getWriter().println("sSecureSecret: "+sSecureSecret);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("BankId"))
	        	{
	        		bankID = paramValue;
//	        		response.getWriter().println("bankID: "+bankID);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TerminalId"))
	        	{
	        		terminalID = paramValue;
//	        		response.getWriter().println("terminalID: "+terminalID);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Currency"))
	        	{
	        		currency = paramValue;
//	        		response.getWriter().println("currency: "+currency);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnType"))
	        	{
	        		txnType = paramValue;
//	        		response.getWriter().println("txnType: "+txnType);
	        	}
			}
			
	        amountExponent = "2";
	        
			if(null != request.getParameter("txn-id"))
				txnRefNo = request.getParameter("txn-id");
			
	        if(null != request.getParameter("txn-amount"))
	        	txnAmount = request.getParameter("txn-amount");
	        float transactionAmount = Float.parseFloat(txnAmount);
	        int withoutDecimal = (int)transactionAmount*100;
//	        response.getWriter().println("1 wihtoutDecimal: "+withoutDecimal);
	        txnAmount = withoutDecimal+"";

	        Hashtable<String, String> pgParams = new Hashtable<String, String>();

	        pgParams.put("Version",version);
	        pgParams.put("TxnRefNo",txnRefNo);
	        pgParams.put("Amount",txnAmount);
	        pgParams.put("PassCode",passCode);
	        pgParams.put("BankId",bankID);
	        pgParams.put("TerminalId",terminalID);
	        pgParams.put("MerchantId",merchantCode);
	        pgParams.put("MCC",mcc);
	        pgParams.put("Currency",currency);
	        pgParams.put("TxnType",txnType);
	        pgParams.put("ReturnURL",request.getParameter("return-url"));
	        
	        LinkedHashMap<String, String> hmReqFields = new LinkedHashMap<String, String>();
	        Enumeration e = pgParams.keys();

	        while(e.hasMoreElements()){
	        	String fieldName = (String) e.nextElement();
				String fieldValue = pgParams.get(fieldName);
//				response.getWriter().println("fieldName: "+fieldName);
//				response.getWriter().println("fieldValue: "+fieldValue);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
//					response.getWriter().println("3333");
					hmReqFields.put(fieldName, fieldValue);
				}
	        }
	        
	        /*sEncryptionKey="5EC4A697141C8CE45509EF485EE7D4B1"; //PGOWNPUBLIC
	        sSecureSecret = "E59CD2BF6F4D86B5FB3897A680E0DD3E";//PGOWNPRIVATE
*/	        
	        ISGPayEncryption encObj = new ISGPayEncryption();
	        encObj.encrypt(hmReqFields, sEncryptionKey, sSecureSecret);
	        
			if(encObj.getENC_DATA() != null && encObj.getENC_DATA().trim().length() > 0)
			{
				JsonObject result = new JsonObject();
				result.addProperty("MerchantId", encObj.getMERCHANT_ID());
				result.addProperty("TerminalId", encObj.getTERMINAL_ID());
				result.addProperty("BankId", encObj.getBANK_ID());
				result.addProperty("Version", encObj.getVERSION());
				result.addProperty("EncData", encObj.getENC_DATA());
				result.addProperty("txnRefNo", txnRefNo);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("WSURL",sWSURL);
				result.addProperty("parameters","MerchantId|TerminalId|BankId|Version|EncData");
//				result.addProperty("parameters","");
				result.addProperty("Valid",true);
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("Valid", "false");
				result.addProperty("pgReqMsg", "Error in processing the request. Please contact system admin.");
				response.getWriter().print(new Gson().toJson(result));
			}
			
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("ParamsMissing", "Some mandatory parameters are missing");
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void initiateEazyPayRequest(HttpServletRequest request, HttpServletResponse response, Map configMap) throws IOException
	{
		try
		{/*
//			ReadConfig
			
			//Below code for Payment Gateway
			String encryptionKey = "", merchantCode="", paymentRequestCall="", wsURL="", pgReqMsg="", pgRequestErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().print("\nconfigMap values:" + configMap.values());
				response.getWriter().print("\nconfigMap keySet:" + configMap.keySet());
			}
			String wholeParamString="", paramName="", paramValue="", pgID="", clientCode = "";
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
	        		WSURL = paramValue;
//	        		WSURL = "https://eazypayuat.icicibank.com/EazyPG?";
	        	}
	        	if(paramName.equalsIgnoreCase("SecretKey"))
	        	{
	        		encryptionKey = paramValue;
//	        		encryptionKey = "1234567891234567";
	        	}
	        }
			String key="", value="";
			Iterator<Map.Entry<String, String>> it = configMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<String, String> pair = it.next();
				
				key = pair.getKey();
				value =	pair.getValue();
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("\nMap Key:" + key+" | Value:"+value);
				
				if(key.equalsIgnoreCase("merchantCode"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nmerchant code");
					merchantCode = value;
				}
				else if(key.equalsIgnoreCase("pgUrl"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nURL");
					wsURL = value;
				}
				else if(key.equalsIgnoreCase("secretKey"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nKey");
					encryptionKey = value;
				}
			}

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().print("\nMerchantCode:" + merchantCode);
				response.getWriter().print("\nPGID:" + pgID);
				response.getWriter().print("\nWSURL:" + wsURL);
				response.getWriter().print("\nSecretKey:" + encryptionKey);
			}
			
			String mandatoryFields="", customerNo="", optionalFields="", subMerchantId="", transactionAmt="", paymentMode="", transactionID="", transactionDate="", returnURL="", finalEncrURL="", finalRawURL="";
			*//**
			 * Temporarily Hard Coded Values - Start
			 **//*
//			merchantCode = "1040"; transactionID = "19299"; transactionAmt="100";
//			mandatoryFields = merchantCode+"|"+transactionID+"|"+transactionAmt;
//			returnURL = "https://flpnwc-ac3edb22e.dispatcher.hana.ondemand.com/sap/fiori/pycomnpymtapp/sap/opu/odata/ARTEC/paymentgateway/PaymentGateway/PGPaymentTxnRes?PGParams=PGIDSEAZYPAY~PGGID938DB166-4585-4722-A347-FE178AC027BA~STEIDSSLaunchpad~APPIDpycomnpymtapp~ACTONAdvancePayment~NAVPMPGStatus~PymntFor000003PaymentCallTextAdvance";
			
//			if(null != request.getParameter("txn-datetime"))
//				transactionDate = request.getParameter("txn-datetime");
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
		    transactionDate = sdf.format(cal.getTime());
		    if(null != request.getParameter("customerNo"))
		    	customerNo = request.getParameter("customerNo");
		    
		    optionalFields = customerNo+"|"+transactionDate;
//			finalRawURL = WSURL+"?merchantid="+merchantCode+"&mandatoryfields="+mandatoryFields+"&optionalfields="+optionalFields+"&returnurl="+returnUrl+"&ReferenceNo="+transactionID+"&submerchantid="+subMerchantId+"&transactionamount="+transactionAmt+"&paymode="+paymentMode;
			*//**
			 * Temporarily Hard Coded Values - End
			 **//*
			if(null != request.getParameter("txn-id"))
				transactionID = request.getParameter("txn-id");
			if(null != request.getParameter("txn-amount"))
				transactionAmt = request.getParameter("txn-amount");
			if(null != request.getParameter("paymentMode"))
				paymentMode = request.getParameter("paymentMode");
			else
				paymentMode = "9";
			if(null != request.getParameter("subMerchantId"))
				subMerchantId = request.getParameter("subMerchantId");
			else
				subMerchantId = "0";
			if(null != request.getParameter("return-url"))
				returnURL = request.getParameter("return-url");
			
			mandatoryFields = transactionID+"|"+subMerchantId+"|"+transactionAmt;
			finalRawURL = wsURL+"?merchantid="+merchantCode+"&mandatoryfields="+mandatoryFields+"&optionalfields="+optionalFields+"&returnurl="+returnURL+"&ReferenceNo="+transactionID+"&submerchantid="+subMerchantId+"&transactionamount="+transactionAmt+"&paymode="+paymentMode;
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().print("\nMerchantCode:" + merchantCode);
				response.getWriter().print("\ntransactionID:" + transactionID);
				response.getWriter().print("\ntransactionAmt:" + transactionAmt);
				response.getWriter().print("\nsubMerchantId:" + subMerchantId);
				response.getWriter().print("\noptionalFields:" + optionalFields);
				response.getWriter().print("\nreturnURL:" + returnURL);
				response.getWriter().print("\nmandatoryFields:" + mandatoryFields);
				response.getWriter().print("\nfinalRawURL:" + finalRawURL);
			}
			
			mandatoryFields = encryptDataForEazyPay(mandatoryFields, encryptionKey);
			optionalFields = encryptDataForEazyPay(optionalFields, encryptionKey);
			transactionID = encryptDataForEazyPay(transactionID, encryptionKey);
			subMerchantId = encryptDataForEazyPay(subMerchantId, encryptionKey);
			transactionAmt = encryptDataForEazyPay(transactionAmt, encryptionKey);
			paymentMode = encryptDataForEazyPay(paymentMode, encryptionKey);
			returnURL = encryptDataForEazyPay(returnURL, encryptionKey);
			
			finalEncrURL = wsURL+"?merchantid="+merchantCode+"&mandatoryfields="+mandatoryFields+"&optionalfields="+optionalFields+"&returnurl="+returnURL+"&ReferenceNo="+transactionID+"&submerchantid="+subMerchantId+"&transactionamount="+transactionAmt+"&paymode="+paymentMode;
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("\nfinalEncrURL:" + finalEncrURL);
			

			if(mandatoryFields != null && mandatoryFields.trim().length() > 0)
			{
				JsonObject result = new JsonObject();
//				result.addProperty("walletRequestMessage", pgReqMsg);
				result.addProperty("RawPayload", finalRawURL);
				result.addProperty("merchantid", merchantCode);
				result.addProperty("mandatoryfields", mandatoryFields);
				result.addProperty("optionalfields", optionalFields);
				result.addProperty("returnurl", returnURL);
				result.addProperty("ReferenceNo", transactionID);
				result.addProperty("submerchantid", subMerchantId);
				result.addProperty("transactionamount", transactionAmt);
				result.addProperty("paymode", paymentMode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", wsURL);
				result.addProperty("parameters","merchantid|mandatoryfields|optionalfields|returnurl|ReferenceNo|submerchantid|transactionamount|paymode");
				
				result.addProperty("RawPayload", finalRawURL);
				result.addProperty("merchantid", merchantCode);
				result.addProperty("mandatory fields", mandatoryFields);
				result.addProperty("optional fields", optionalFields);
				result.addProperty("returnurl", returnURL);
				result.addProperty("Reference No", transactionID);
				result.addProperty("submerchantid", subMerchantId);
				result.addProperty("transaction amount", transactionAmt);
				result.addProperty("paymode", paymentMode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", wsURL);
				result.addProperty("parameters","merchantid|mandatory fields|optional fields|returnurl|Reference No|submerchantid|transaction amount|paymode");
				
				result.addProperty("walletRequestMessage", pgReqMsg);
				result.addProperty("RawPayload", finalURL);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", WSURL);
				result.addProperty("parameters","walletClientCode|walletRequestMessage");
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", "Mandatory fields are missing. Please check configuration.");
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		*/}
		catch (Exception e)
		{
//			response.getWriter().println("initiateICICIPaymentRequest Error: "+e.getMessage());
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", e.getMessage());
//			result.addProperty("walletClientCode", merchantCode);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	/*public String encryptDataForEazyPay(String parameterData, String encryptionKey)
	{
		String enryptedData = "";
		Base64 encoder = null;
		try
		{
			byte[] abyte2 = (byte[])null;
			byte[] abyte1 = encryptionKey.getBytes();
			SecretKeySpec secretkeyspec = new SecretKeySpec(abyte1, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(1, secretkeyspec);
			abyte2 = cipher.doFinal(parameterData.getBytes());
			enryptedData = encoder.encode(abyte2);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		finally
		{
			return enryptedData;
		}
	}*/
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class WalletDetails
 */
//@WebServlet("/WalletDetails")
public class WalletDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
//	private static final String SF_CUSTOMER_DEST_NAME =  "sfmst";
//	private static final String PCGW_DEST_NAME =  "pcgw";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
    /**
     * @see HttpServlet#HttpServlet()
     */
//    public WalletDetails() {
//        super();
//        // TODO Auto-generated constructor stub
//    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.setContentType("application/json");
		// Check for a logged in user
		LOGGER.info("1. doGet function starts");
		try
		{
			String customerNo = "", PGCategoryID="", clientCode="", loginSessionID="", beneficiaryName="", ifscCode = "", virtualAccountNo="";
			boolean isValidCustomer = false, isError = false;
			
//			Properties properties = new Properties();
//			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(null != request.getParameter("PGID"))
				PGCategoryID = request.getParameter("PGID");
//			else
//				PGCategoryID = "000002";
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			loginSessionID = request.getParameter("sessionID");
			
			beneficiaryName = getCustomers(request, response, loginSessionID, customerNo);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("beneficiaryName: "+beneficiaryName);
			
			if(null != beneficiaryName && beneficiaryName.toString().trim().length() > 0)
			{
				isValidCustomer = true;
			}
			
			if(isValidCustomer)
			{
//				clientCode = getConstantValues(request, response, "", PGCategoryID);
				String configurationValues = getConstantValues(request, response, "", PGCategoryID);
				String wholeParamString="", paramName="", paramValue="", pgID="", WSURL="";
				wholeParamString = configurationValues;
				String[] splitResult = wholeParamString.split("\\|");
				
				//For debugging java application
				if(null != request.getParameter("debug") && request.getParameter("Debug") == "true")
					response.getWriter().println("wholeParamString: "+ wholeParamString);
				
				for(String s : splitResult)
				{
		        	paramName = s.substring(0, s.indexOf("="));
		        	paramValue = s.substring(s.indexOf("=")+1,s.length());
		        	
		        	if(paramName.equalsIgnoreCase("BankKey"))
		        	{
		        		ifscCode = paramValue;
		        	}
		        	
		        	if(paramName.equalsIgnoreCase("ClientCode"))
		        	{
		        		clientCode = paramValue;
		        	}
				}
				
				if(null != request.getParameter("debug") && request.getParameter("Debug") == "true")
				{
					response.getWriter().println("clientCode: "+clientCode);
					response.getWriter().println("ifscCode: "+ifscCode);
				}
				
				if(null == clientCode || clientCode.toString().trim().length() == 0 || clientCode.equalsIgnoreCase(""))
				{
//					clientCode = "ART";
					isError = true;
//					isError = false;
				}
				
				if(null != clientCode && clientCode.toString().trim().length() > 0)
				{
					virtualAccountNo = clientCode+customerNo;
//					response.getWriter().println("virtualAccountNo: "+virtualAccountNo);
					/*if(clientCode.trim().length() == 3)
					{
						ifscValue = properties.getProperty("IFSC3DigitCode");
					}
					else if(clientCode.trim().length() == 4)
					{
						ifscValue = properties.getProperty("IFSC4DigitCode");
					}
					else
					{
						ifscValue = properties.getProperty("InvalidIFSCCode");
					}*/
//					response.getWriter().println("ifscValue: "+ifscValue);
					JsonObject result = new JsonObject();
					result.addProperty("ifscCode", ifscCode);
					result.addProperty("virtualAccountNo", virtualAccountNo);
					result.addProperty("beneficiaryName", beneficiaryName);
					result.addProperty("customerNo", customerNo);
					result.addProperty("Valid", "true");
					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("error", "Details not found");
//					result.addProperty("virtualAccountNo", "");
//					result.addProperty("beneficiaryName", "");
//					result.addProperty("customerNo", "");
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("error", "Customer:"+customerNo+" is not valid");
//				result.addProperty("virtualAccountNo", "");
//				result.addProperty("beneficiaryName", "");
				result.addProperty("customerNo", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("error", e.getMessage());
//			result.addProperty("virtualAccountNo", "");
//			result.addProperty("beneficiaryName", "");
//			result.addProperty("customerNo", "");
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
	}

	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
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
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
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
//						response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList bankKeyList = document.getElementsByTagName("d:BankKey");
	            
	            for(int i=0 ; i<pgIDList.getLength() ; i++)
	            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(PGID.equalsIgnoreCase(pgIDList.item(i).getTextContent()))
        			{
//			            		configurableValues = configurableValues+"MerchantCode:"+merchantCodeList.item(i).getTextContent();
//			            		configurableValues = clientCodeList.item(i).getTextContent();
	            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
	            				+"|"+"BankKey="+bankKeyList.item(i).getTextContent()
	            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
//			            		response.getWriter().println("clientCodeList: "+clientCodeList.item(i).getTextContent());
	            		/*
	            		 * configurableValues = configurableValues+"MerchantCode:"+merchantCodeList.item(i).getTextContent();
	            		 * configurableValues = configurableValues+"|ClientCode:"+clientCodeList.item(i).getTextContent();
	            		 * If any new parameters that need to be read, create a new line as above with pipe separation symbol as delimitter.
	            		 * Also add a new instance for NodeList class above the for loop for the new field.
	            		 * In the doGet method have to split these parameters and use as required.
	            		 * */
	            		break;
        			}
	            }
			}
		}
		catch (Exception e)
		{
			response.getWriter().println("Exception: "+e.getMessage());
		}
		finally
		{
			// closableHttpClient.close();
			return configurableValues;
		}
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
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			customerFilter = "LoginID eq '"+loginSessionID+"'";
			
			customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
			
			customerFilter = customerFilter.replaceAll("%26", "&");
			customerFilter = customerFilter.replaceAll("%3D", "=");
			
//			Context tenCtx = new InitialContext();
//			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
//			if(debug){
//				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}
			
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
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
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
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        userCustomersGet = new HttpGet(customerService);
//	        userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        userCustomersGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);
	        
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
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return beneficiaryName;
		}catch (ParserConfigurationException e) {
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return beneficiaryName;
		} catch (SAXException e) {
			if(debug)
				response.getWriter().println("RuntimeException in getCustomers: "+e.getMessage());
			return beneficiaryName;
		}
		/* finally
		{
			closableHttpClient.close();
		} */
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
		try {
				// get all destination properties
				Context ctxDestFact = new InitialContext();
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
				//Reading SAP-Client from All Configuration.........................................................
//				tempSapClient = allDestinationPropeties.get("sap-client");
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
				if(property.equalsIgnoreCase("sapclient")){
					propertyValue = destConfiguration.get("sap-client").get().toString();
				} else if(property.equalsIgnoreCase("service")){
					propertyValue = destConfiguration.get("servicename").get().toString();
				}
			}
			catch (NamingException e) {
				// Connectivity operation failed
				String errorMessage = "Connectivity operation failed with reason: "
							+ e.getMessage()
							+ ". See "
							+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
				LOGGER.error("Connectivity operation failed", e);
				//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
				//response.getWriter().println("Error: " +  errorMessage);
			}
		
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
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

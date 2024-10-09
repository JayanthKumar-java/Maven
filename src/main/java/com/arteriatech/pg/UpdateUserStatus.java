package com.arteriatech.pg;

import java.io.IOException;
import java.util.ArrayList;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;

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
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class UpdateUserStatus
 */
@WebServlet("/UpdateUserStatus")
public class UpdateUserStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateUserStatus() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		try
		{
			String walletPublicKey="", merchantPrivateKey="", merchantPublicKey="", merchantCode="", wsUrl="", customerNo = "", clientCode="", activationStatus="", reqDateTime="", loginSessionID="", updateStatusCall="", configurationValues="", pgID="", wholeParamString="", paramName="", paramValue="";
			boolean isGroupAvailable = false, isValidUser = false;
			isGroupAvailable = readUserPrincipal(request,response);
			
			if(null != request.getParameter("sessionID"))
				loginSessionID = request.getParameter("sessionID");
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isGroupAvailable: "+isGroupAvailable);
			
			if(! isGroupAvailable)
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("Inside IF");
				isValidUser = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
			}
			else
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("Inside ELSE");
				isValidUser = true;
			}
			
			//Temp
			isValidUser = true;
			
			if(isValidUser)
			{
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				updateStatusCall = properties.getProperty("updateStatusCall");
				
				configurationValues = getConstantValues(request, response, "", pgID);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+configurationValues);
				
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
				
				WalletParamMap inputParamMap = new WalletParamMap();
				if(null !=request.getParameter("wallet-user-code"))
					inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
				
				if(null !=request.getParameter("user-status"))
					inputParamMap.put("user-status", request.getParameter("user-status"));
//					inputParamMap.put("user-status", request.getParameter("5"));

				WalletAPI getResponse = new WalletAPI();
				WalletParamMap responseMap = getResponse.callWalletAPI(wsUrl, updateStatusCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
				
				String walletusercode="", status="", responseDateTime="", errorStatus="", errorMsg="", remarks="";
				if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
				{
					walletusercode = responseMap.get("wallet-user-code");
					status = responseMap.get("status");
					remarks = responseMap.get("remarks");
					responseDateTime = responseMap.get("responseDateTime");
							
					JsonObject result = new JsonObject();
					result.addProperty("walletusercode", walletusercode);
					result.addProperty("status", status);
					result.addProperty("remarks", remarks);
					result.addProperty("responseDateTime", responseDateTime);
					result.addProperty("Valid", "true");
					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
					errorStatus = "01";
					errorMsg = "Response not received";
					JsonObject result = new JsonObject();
					result.addProperty("errorStatus", errorStatus);
					result.addProperty("errorMsg", errorMsg);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
		}
		catch (Exception e) {
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
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
			String errorStatus = "02";
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMessage);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
			//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
			//response.getWriter().println("Error: " +  errorMessage);
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			String errorStatus = "02";
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMessage);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
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
				configurableValues = "";
				if(PGID.equalsIgnoreCase("B2BIZ"))
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
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList wsUrlList = document.getElementsByTagName("d:UserRegURL");
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
		}
		catch (Exception e)
		{
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
		finally
		{
			// closableHttpClient.close();
			return configurableValues;
		}
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
	
	/*private boolean getCustomers(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo) throws IOException, URISyntaxException
	{
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
			
			String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME, "sapclient");
			String service = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME, "service");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("service: "+service);
			
			HttpDestination destination = getHTTPDestinationForCustomers(request, response, PCGW_UTIL_DEST_NAME);
//			response.getWriter().println("destination getHost: "+destination.getURI().getHost());
//			response.getWriter().println("destination getPort: "+destination.getURI().getPort());
//			response.getWriter().println("destination getRawPath: "+destination.getURI().getRawPath());
//			response.getWriter().println("destination getName: "+destination.getName());
//			response.getWriter().println("destination getName: "+destination.);
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
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("CustomerService: "+CustomerService);
			HttpClient httpClient = destination.createHttpClient();
			customerRequest = new HttpGet(CustomerService);
			HttpResponse customerResponse = httpClient.execute(customerRequest);

			// Copy content from the incoming response to the outgoing response
			customerEntity = customerResponse.getEntity();
			
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
	            NodeList entryList = document.getElementsByTagName("entry");
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
	            		break;
        			}
	            }
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isValidCustomer: "+isValidCustomer);
			EntityUtils.consume(customerEntity);
		}catch (RuntimeException e) {
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
			
			customerRequest.abort();
		} catch (DestinationException e) {
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		} catch (ParserConfigurationException e) {
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		} catch (SAXException e) {
			String errorStatus = "02";
			String errorMsg = e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", errorStatus);
			result.addProperty("errorMsg", errorMsg);
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
		finally
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("Finally isValidCustomer: "+isValidCustomer);
			return isValidCustomer;
		}
	}*/
	
	private boolean readUserPrincipal(HttpServletRequest request, HttpServletResponse response)
	{
		boolean isGroupAvailable=false;
		String authTokenHeader="", authMtd="", authValue="";
		String jwtSplit[] = null;
		JsonObject jwtBody = new JsonObject();
		boolean debug= false;
		System.out.println( "inside readUserPrincipal for getCurrentToken:" +AuthTokenAccessor.getCurrentToken().getJwt());
		// System.out.println( "inside readUserPrincipal for getXsuaaServiceToken:" +AuthTokenAccessor.getXsuaaServiceToken().getJwt());
		System.out.println("inside readUserPrincipal for getCurrentPrincipal:"+PrincipalAccessor.getCurrentPrincipal());
		CommonUtils commonUtils = new CommonUtils();
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

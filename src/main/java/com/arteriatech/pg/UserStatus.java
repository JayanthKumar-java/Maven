package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
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
 * Servlet implementation class UserStatus
 */
@WebServlet("/UserStatus")
public class UserStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		CommonUtils commonUtils = new CommonUtils();
		try
		{
			String customerNo = "", PGCategoryID="", clientCode="", loginSessionID="", beneficiaryName="", ifscCode = "", sPGOwnPublickey = "", virtualAccountNo="";
			boolean isValidCustomer = false, isError = false, debug=false;
			
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(null != request.getParameter("PGID"))
				PGCategoryID = request.getParameter("PGID");
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			loginSessionID = request.getParameter("sessionID");
			
			isValidCustomer = commonUtils.getCustomers(request, response, loginSessionID, customerNo,debug);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isValidCustomer: "+isValidCustomer);
			
			
			if(isValidCustomer)
			{
				String configurationValues = getConstantValues(request, response, "", PGCategoryID);
				String wholeParamString="", sSessionID="", paramName="", paramValue="", pgID="", WSURL="";
				wholeParamString = configurationValues;
				String[] splitResult = wholeParamString.split("\\|");
				
				//For debugging java application
				if(null != request.getParameter("debug") && request.getParameter("debug") == "true")
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
		        	if(paramName.equalsIgnoreCase("PGOwnPublickey"))
		        	{
		        		sPGOwnPublickey = paramValue;
		        	}
		        	if(paramName.equalsIgnoreCase("SessionID"))
		        	{
		        		sSessionID = paramValue;
		        	}
				}
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				{
					response.getWriter().println("clientCode: "+clientCode);
					response.getWriter().println("ifscCode: "+ifscCode);
					response.getWriter().println("PGOwnPublickey: "+sPGOwnPublickey);
				}
				
				if(null == clientCode || clientCode.toString().trim().length() == 0 || clientCode.equalsIgnoreCase(""))
				{
					isError = true;
				}
				
				
				if(null != clientCode && clientCode.toString().trim().length() > 0)
				{
					getEscrowUserStatus(request, response,sSessionID, configurationValues, customerNo);
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("error", "Status not found");
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("error", "Customer:"+customerNo+" is not valid");
				result.addProperty("customerNo", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("error", e.getMessage());
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	private void getEscrowUserStatus(HttpServletRequest request, HttpServletResponse response, String loginSessionID2, String ConfigValues, String CustomerNo) throws IOException, URISyntaxException
	{
		String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", EscrowUserStatusCall = "", wholeParamString = "",
				paramName = "", paramValue = "", clientCode = "", WSURL = "", merchantCode = "";
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		EscrowUserStatusCall = properties.getProperty("EscrowUserStatusCall");
		
		wholeParamString = ConfigValues;
		String[] splitResult = wholeParamString.split("\\|");
		
		//For debugging java application
		if(null != request.getParameter("debug") && request.getParameter("debug") == "true")
			response.getWriter().println("wholeParamString: "+ wholeParamString);
		
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
        	if(paramName.equalsIgnoreCase("WSURL"))
        	{
        		WSURL = paramValue;
        	}
        	if(paramName.equalsIgnoreCase("MerchantCode"))
        	{
        		merchantCode = paramValue;
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
		
		try
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("try");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			if(null !=request.getParameter("wallet-user-code"))
				inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("wallet-user-code:" + request.getParameter("wallet-user-code"));
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("inputParamMap:" + inputParamMap);

			WalletAPI getResponse = new WalletAPI();
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, EscrowUserStatusCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("responseMap" + responseMap);
			
			String walletusercode="", userbalance="", checksum="", errorStatus="", errorMsg="";
			if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("response found");
				
				JsonObject result = new JsonObject();
				result.addProperty("walletusercode", responseMap.get("wallet-user-code"));
				result.addProperty("UpdationStatus", responseMap.get("status"));
				result.addProperty("Remarks", responseMap.get("remarks"));
				result.addProperty("Responsedatetime", responseMap.get("response-date-time"));
				result.addProperty("checksum", responseMap.get("checksum"));
				result.addProperty("checksum", checksum);
				result.addProperty("Valid", "true");
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("response not found");
				
				errorStatus = responseMap.get("error_status");
				errorMsg = responseMap.get("error_message");
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", errorStatus);
				result.addProperty("errorMsg", errorMsg);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().println("Error: No Login ID found");
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
//	        configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
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
				if(null != request.getParameter("debug") && request.getParameter("debug") == "true")
					response.getWriter().print("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            NodeList pgCategoryList = document.getElementsByTagName("d:PGID");
	            NodeList bankKeyList = document.getElementsByTagName("d:BankKey");
	            NodeList pgOwnPublickeyList = document.getElementsByTagName("d:BankKey");
	            NodeList wsUrlList = document.getElementsByTagName("d:UserRegURL");
	            
	            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
	            {
	            	if(PGID.equalsIgnoreCase(pgCategoryList.item(i).getTextContent()))
        			{
	            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
	            				+"|"+"BankKey="+bankKeyList.item(i).getTextContent()
	            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent()
	            				+"|"+"PGOwnPublickey="+pgOwnPublickeyList.item(i).getTextContent()
	            				+"|"+"WSURL="+wsUrlList.item(i).getTextContent();
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
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("sapclient: "+sapclient);
				response.getWriter().println("service: "+service);
			}
				
			
			HttpDestination destination = getHTTPDestinationForCustomers(request, response, PCGW_UTIL_DEST_NAME);
			
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
	            		break;
        			}
	            }
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isValidCustomer: "+isValidCustomer);
			EntityUtils.consume(customerEntity);
		}catch (RuntimeException e) {
			customerRequest.abort();
			response.getWriter().println("RuntimeException: "+e.getMessage());
		} catch (DestinationException e) {
			response.getWriter().println("DestinationException: "+e.getMessage());
		} catch (ParserConfigurationException e) {
			response.getWriter().println("ParserConfigurationException: "+e.getMessage());
		} catch (SAXException e) {
			response.getWriter().println("SAXException: "+e.getMessage());
		}
		finally
		{
			return isValidCustomer;
		}
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
//				DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
//				Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
		Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
		Destination destConfiguration = destinationAccessor.get();
		if(property.equalsIgnoreCase("sapclient")){
			propertyValue = destConfiguration.get("sap-client").get().toString();
		} else if(property.equalsIgnoreCase("service")){
			propertyValue = destConfiguration.get("servicename").get().toString();
		}
		
//		return tempSapClient;
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

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
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class EStatement
 */
@WebServlet("/EStatement")
public class EStatement extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
//	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
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
    public EStatement() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		boolean debug=false;
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", walletRequestMessage="", pgRequestErrorMsg="";
			boolean isValidCustomer = false;
			String configurationValues = "";
			String customerNo="", application="", loginSessionID="", PGID = "";
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			if(null != request.getParameter("Application"))
				application = request.getParameter("Application");
			
			if(null != request.getParameter("PGID"))
				PGID = request.getParameter("PGID");
//			else
//				PGID = "B2BIZ";
			
			loginSessionID = request.getParameter("sessionID");
			isValidCustomer = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
			
			if(isValidCustomer)
			{
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
//				walletPublicKey = properties.getProperty("walletPublicKey");
//				merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//				merchantPublicKey = properties.getProperty("merchantPublicKey");
//				pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
				
				//PG Config values.........................................................
				configurationValues = getConstantValues(request, response, "", PGID);
				
				WalletParamMap inputParamMap = new WalletParamMap();
				
//				if(null !=request.getParameter("wallet-user-code"))
//					inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
//				if(null !=request.getParameter("txn-datetime"))
//					inputParamMap.put("txn-datetime", request.getParameter("txn-datetime"));
//				
//				WalletMessageBean walletMessageBean = new WalletMessageBean();
//				walletMessageBean.setRequestMap(inputParamMap);
//				walletMessageBean.setWalletKey(walletPublicKey);
//				walletMessageBean.setClientKey(merchantPrivateKey);
//				walletRequestMessage = walletMessageBean.generateWalletRequestMessage();
				
				if(PGID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
				{
//					response.getWriter().print("Initiated:" + PGID);
					initiateEStatementRequest(request, response, configurationValues);
//				}
//				if(walletRequestMessage != null && walletRequestMessage.trim().length() > 0)
//				{
					//Old Code.............................................................
//					JsonObject result = new JsonObject();
//					result.addProperty("pgReqMsg", walletRequestMessage);
//					result.addProperty("walletClientCode", merchantCode);
//					result.addProperty("Valid", "true");
//					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
					
					//Old code..................................................................
//					JsonObject result = new JsonObject();
//					result.addProperty("pgReqMsg", pgRequestErrorMsg);
//					result.addProperty("walletClientCode", merchantCode);
//					result.addProperty("Valid", "false");
//					response.getWriter().print(new Gson().toJson(result));
					
					//Updated Code...............................................................
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", "Invalid Payment Gateway Request.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				//Old code........................................................................
//				JsonObject result = new JsonObject();
//				result.addProperty("pgReqMsg", pgRequestErrorMsg);
//				result.addProperty("walletClientCode", merchantCode);
//				result.addProperty("Valid", "false");
//				response.getWriter().print(new Gson().toJson(result));
				
				//Updated Code....................................................................
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", customerNo+" is not authorized to make a payment.");
				result.addProperty("walletClientCode", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().print(e.getMessage());
		}
	}
	private void initiateEStatementRequest(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", clientCode = "", merchantCode="", topupRequestCall="", pgReqMsg="" , WSURL="", pgRequestErrorMsg="";
			Properties properties = new Properties();
			String walletRequestMessage="";
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
//			response.getWriter().print("wholeParamString:" + wholeParamString);
			String[] splitResult = wholeParamString.split("\\|");
//			response.getWriter().print("splitResult:" + splitResult);
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
//	        	response.getWriter().print("paramValue:" + paramValue);
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
//	        		response.getWriter().print("merchantCode:" + merchantCode);
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
//	        		response.getWriter().print("pgID:" + pgID);
	        	}
	        	if(paramName.equalsIgnoreCase("WSURL"))
	        	{
//	        		response.getWriter().print("WSURL:" + WSURL);
	        		WSURL = paramValue;
	        	}
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
//	        		response.getWriter().print("WSURL:" + WSURL);
	        		clientCode = paramValue;
	        	}
			}
			
			//Stored Properties in local file.....................................................
//			walletPublicKey = properties.getProperty("walletPublicKey");
//			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//			merchantPublicKey = properties.getProperty("merchantPublicKey");
			pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
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
			
//			if()
//			response.getWriter().print("WSURL:" + WSURL);
			
			//Encryption process..................................................................
			WalletParamMap inputParamMap = new WalletParamMap();
			
			if(null !=request.getParameter("wallet-user-code"))
				inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			if(null !=request.getParameter("txn-datetime"))
				inputParamMap.put("txn-datetime", request.getParameter("txn-datetime"));
			
			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setRequestMap(inputParamMap);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			walletRequestMessage = walletMessageBean.generateWalletRequestMessage();
			
			
			if(walletRequestMessage != null && walletRequestMessage.trim().length() > 0)
			{
//				response.getWriter().print("walletRequestMessage:" + walletRequestMessage);
				JsonObject result = new JsonObject();
				//Correction code.................
				result.addProperty("walletRequestMessage", walletRequestMessage);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "true");
//				result.addProperty("WSURL", "https://demo.b2biz.co.in/ws/balanceInquiry");
				result.addProperty("WSURL", WSURL);
				result.addProperty("parameters","walletClientCode|walletRequestMessage");
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
//				response.getWriter().print("pgRequestErrorMsg:" + pgRequestErrorMsg);
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgRequestErrorMsg);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e) {
//			response.getWriter().print("Catch:");
			// TODO: handle exception
		}
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
//				response.getWriter().println("retSrc: "+retSrc);
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList nodeList = document.getElementsByTagName("d:CustomerNo");
	            for(int i=0 ; i<nodeList.getLength() ; i++)
	            {
//	            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(customerNo.equalsIgnoreCase(nodeList.item(i).getTextContent()))
        			{
	            		isValidCustomer = true;
	            		break;
        			}
	            }
//				if(retSrc.contains("<d:CustomerNo>")) {
//					customerFromService = retSrc.split("<d:CustomerNo>")[1].split("</d:CustomerNo>")[0];
//				}
			}
//			response.getWriter().println("isValidCustomer: "+isValidCustomer);
//			response.getWriter().println("customerFromService: "+customerFromService);
//			response.getOutputStream().print(Integer.parseInt(EntityUtils.toString(customerEntity)));
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
				
		String tempSapClient = null;
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
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
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
			
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			
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
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList aWSURLList = document.getElementsByTagName("d:AccStmtURL");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            
	            for(int i=0 ; i<pgIDList.getLength() ; i++)
	            {
	            	if(PGID.equalsIgnoreCase(pgIDList.item(i).getTextContent()))
        			{
	            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"+"PGID="+pgIDList.item(i).getTextContent()+"|"+"WSURL="+aWSURLList.item(i).getTextContent()
	            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
	            		break;
        			}
	            }
			}
			
			if(debug)
				response.getWriter().println("configurableValues: "+configurableValues);
		}
		catch (Exception e)
		{
			// TODO: handle exception
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
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

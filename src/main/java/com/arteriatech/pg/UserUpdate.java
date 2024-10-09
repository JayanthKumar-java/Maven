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
 * Servlet implementation class UserUpdate
 */
@WebServlet("/UserUpdate")
public class UserUpdate extends HttpServlet {
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
    public UserUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", clientCode = "", merchantCode="", userUpdateCall="", PGCategoryID="", configurationValues = "";
			String temp="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
//			merchantCode = properties.getProperty("merchantCode");
			userUpdateCall = properties.getProperty("userUpdateCall");
			
			if(null != request.getParameter("PGCategoryID"))
				PGCategoryID = request.getParameter("PGCategoryID");
			else
				PGCategoryID = "000002";
			
			configurationValues = getConstantValues(request, response, "", "B2BIZ");
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("configurationValues: "+configurationValues);
//			response.getWriter().println("configurationValues: "+merchantCode);
			
			
			
			String wholeParamString="", paramName="", paramValue="", pgID="", WSURL="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			
			//For debugging java application
			if(request.getParameter("debug") == "true")
				response.getWriter().println("wholeParamString: "+ wholeParamString);
			
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
			
//			walletPublicKey = properties.getProperty("walletPublicKey");
//			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//			merchantPublicKey = properties.getProperty("merchantPublicKey");
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
			
			
			if(request.getParameter("debug") == "true")
			{
				response.getWriter().println("merchantCode: "+ merchantCode);
				response.getWriter().println("WSURL: "+ WSURL);
			}
			
			String userName = request.getParameter("user-name");
			
			userName = userName.replaceAll("%26", "AND");
			userName = userName.replaceAll("%20", " ");
			userName = userName.replaceAll("%2C", ",");
			userName = userName.replaceAll("%2F", "/");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			if(null !=request.getParameter("wallet-user-code"))
				inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			
			if(null !=request.getParameter("adhar-card-id"))
				inputParamMap.put("adhar-card-id", request.getParameter("adhar-card-id"));
			
			if(null !=request.getParameter("adhar-ref-no"))
				inputParamMap.put("adhar-ref-no", request.getParameter("adhar-ref-no"));
			
			if(null !=request.getParameter("user-name"))
//				inputParamMap.put("user-name", request.getParameter("user-name"));
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
			
			WalletAPI getResponse = new WalletAPI();
//			"https://demo.b2biz.co.in/ws/walletAPI"
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, userUpdateCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			String walletUserCode="", status="", remarks="", responseDateTime="", checksum="", errorStatus="", errorMessage="";
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
			}
			else
			{
				errorStatus = responseMap.get("error_status");
				errorMessage = responseMap.get("error_message");
				
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", errorStatus);
				result.addProperty("errorMessage", errorMessage);
				response.getWriter().print(new Gson().toJson(result));
			}
			
//		response.getWriter().print("<br><br>Response:<br>");
//		Iterator it1 = responseMap.entrySet().iterator();
//	    while (it1.hasNext()) {
//	        Map.Entry pair = (Map.Entry)it1.next();
//	        response.getWriter().print("<br>"+pair.getKey() + " = " + pair.getValue());
//	    }
			
//		response.getWriter().print("<br>walletUserCode: "+responseMap.get("wallet-user-code")+"<br>status: "+responseMap.get("status")+"<br>remarks: "+responseMap.get("remarks")+"<br>responseDateTime: "+responseMap.get("response-date-time")+"<br>checksum: "+responseMap.get("checksum"));
		}
		catch (Exception e)
		{ 
			response.getWriter().print(e.getMessage());
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
				if(debug)
					response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
	            NodeList WSURLList = document.getElementsByTagName("d:UserRegURL");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            
	            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
	            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(PGID.equalsIgnoreCase(pgIDList.item(i).getTextContent()))
        			{
//			            		configurableValues = merchantCodeList.item(i).getTextContent();
	            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
	            				+"|"+"PGID="+pgIDList.item(i).getTextContent()
	            				+"|"+"WSURL="+WSURLList.item(i).getTextContent()
	            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
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
	
	private String getHTTPDestinationConfiguration(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
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
		//Reading SAP-Client from All Configuration.........................................................
		tempSapClient = destConfiguration.get("sap-client").get().toString();
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
		
		return tempSapClient;
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

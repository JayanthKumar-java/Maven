package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ccavenue.security.AesCryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.wallet247.clientutil.api.WalletAPI;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class TransactionStatus
 */
@WebServlet("/TransactionStatus")
public class TransactionStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private TenantContext  tenantContext;
//	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
//	private static final String SF_CUSTOMER_DEST_NAME =  "sfmst";
//	private static final String PCGW_DEST_NAME =  "pcgw";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	public static SSLSocketFactory s_sslSocketFactory = null;
	public static X509TrustManager s_x509TrustManager = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TransactionStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", transactionStatusCall="", PGCategoryID="", pgID="", constantValues="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
//			walletPublicKey = properties.getProperty("walletPublicKey");
//			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//			merchantPublicKey = properties.getProperty("merchantPublicKey");
//			merchantCode = properties.getProperty("merchantCode");
			transactionStatusCall = properties.getProperty("transactionStatusCall");
			
			if(null != request.getParameter("PGCategoryID"))
				PGCategoryID = request.getParameter("PGCategoryID");
			else
				PGCategoryID = "000002";
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("pgID: "+pgID);
//			response.getWriter().println("merchantCode: "+merchantCode);
			
			if(null != pgID && pgID.trim().length() > 0)
			{
				if(! pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
				{
					constantValues = getConstantValues(request, response, "", pgID);
				}
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+constantValues);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("constantValues: "+constantValues);
				
				if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
				{
//					merchantCode = getConstantValues(request, response, "", pgID);
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("ICICI method");
					getICICITransactionStatus(request, response, constantValues);
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("AxisPGID")))
				{
					getAxisBankTransactionStatus(request, response, constantValues);
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("YesPGID")))
				{
					getYesPayUTransactionStatus(request, response, constantValues);
				}else if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")) 
						|| pgID.equalsIgnoreCase("RZRPAY") 
						|| pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
					getRazorPayTxnStatus(request, response, constantValues);
				}else if(pgID.equalsIgnoreCase(properties.getProperty("CCAvenuePGID")) 
						|| pgID.equalsIgnoreCase(properties.getProperty("MobileCCPGID"))
						|| pgID.equalsIgnoreCase(properties.getProperty("CCAICICI"))
						|| pgID.equalsIgnoreCase(properties.getProperty("CCAPGID"))){
					getCCAvenueTransactionStatus(request, response, constantValues);
				}else if(pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID"))){
					CommonUtils readConfig = new CommonUtils();
					Map<String, String> configMap = readConfig.readConfigValues(pgID, "T");
					getEazyPayTransactionStatus(request, response, configMap);
				}else if(pgID.equalsIgnoreCase(properties.getProperty("BFL"))
						|| pgID.equalsIgnoreCase(properties.getProperty("BFL1"))){
					getBflTransactionStatus(request, response, constantValues);
				}else{
//					errorStatus = responseMap.get("error_status");
//					errorMsg = responseMap.get("error_message");
					JsonObject result = new JsonObject();
					result.addProperty("errorCode", "I");
					result.addProperty("errorMsg", "Invalid Request");
					result.addProperty("status", "Failure");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("errorCode", "I");
				result.addProperty("errorMsg", "PGID not found or null");
				result.addProperty("status", "Failure");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
//			response.getWriter().print(e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("errorCode", "I");
			result.addProperty("errorMsg", buffer.toString());
			result.addProperty("status", "Failure");
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
			if (debug)
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
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("1 configurableValues: "+configurableValues);
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
		            NodeList aWSURLList = document.getElementsByTagName("d:TxnStsURL");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()
		            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
				else if(PGID.equalsIgnoreCase("AXISPG"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
//							response.getWriter().println("retSrc: "+retSrc);
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
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
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
				else if(PGID.equalsIgnoreCase("YESPAYU"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
//							response.getWriter().println("retSrc: "+retSrc);
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
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
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
				else if(PGID.equalsIgnoreCase("EazyPayPGID"))
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
		            NodeList aWSURLList = document.getElementsByTagName("d:TxnStsURL");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		            NodeList secretKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
		            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent()+
		            				"|"+"SecretKey="+secretKeyList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}else if(PGID.equalsIgnoreCase("RAZORPAY")
					|| PGID.equalsIgnoreCase("RZRPYROUTE")
					|| PGID.equalsIgnoreCase("RZRPAY")){

					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
					NodeList apiKey = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList secretKey = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
									          
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
										          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
										            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
										            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"APIKey="+apiKey.item(i).getTextContent()
										            +"|"+"SecretKey="+secretKey.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("CCAVENUE") 
				|| PGID.equalsIgnoreCase("MOBILECCA")){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
									          
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
										          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
										            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
										            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("CCAICICI")
				|| PGID.equalsIgnoreCase("CCA")){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
										          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
										            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
										            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("BFLNCEMI")){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList pgNameList = document.getElementsByTagName("d:PGName");
					NodeList pgPublicKeyList = document.getElementsByTagName("d:PGPublicKey");
					NodeList pgOwnPublicKeyList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList pgOwnPrivateKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList pgProviderList = document.getElementsByTagName("d:PGProvider");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pgURLList = document.getElementsByTagName("d:PGURL");
					NodeList txnStatusList = document.getElementsByTagName("d:TxnStsURL");
					NodeList bankKey = document.getElementsByTagName("d:BankKey");
					
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"PGID="+pdIDList.item(i).getTextContent()
										          	+"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"PGName="+pgNameList.item(i).getTextContent()
										            +"|"+"IVText="+pgPublicKeyList.item(i).getTextContent()
										            +"|"+"RequeryKey="+pgOwnPublicKeyList.item(i).getTextContent()
										            +"|"+"TransactionKey="+pgOwnPrivateKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+pgProviderList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"TransactionURL="+pgURLList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnStatusList.item(i).getTextContent()
													+"|"+"AcquiringChannelID="+bankKey.item(i).getTextContent();
						
							break;
						}
					}
				}
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("1 configurableValues: "+configurableValues);
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
				tempSapClient = destConfiguration.get("sap-client").get().toString();
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
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
	
	private void getICICITransactionStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", clientCode = "", merchantCode="", transactionStatusCall="", PGCategoryID="", WSURL="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("splitResult: "+splitResult);
			}
			
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	        	{
					response.getWriter().println("paramName: "+paramName);
					response.getWriter().println("paramValue: "+paramValue);
	        	}
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
//			merchantCode = properties.getProperty("merchantCode");
//			merchantCode = configurationValues;
			transactionStatusCall = properties.getProperty("transactionStatusCall");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			if(null != request.getParameter("txnID"))
				inputParamMap.put("txn-id", request.getParameter("txnID"));
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("txn-id: "+request.getParameter("txnID"));

			WalletAPI getResponse = new WalletAPI();
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, transactionStatusCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			//"https://demo.b2biz.co.in/ws/walletAPI"
			String transactionID="", txnDateTime="", transactionAmount ="", checksum="", errorStatus="", errorMsg="", walletTrnsctnID="", walletTxnDateTime="", 
					walletTxnRemarks="", walletTxnStatus="", pymtMode="", brn="", status="", currency="";
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("checksum: "+responseMap.get("checksum"));
				response.getWriter().println("txn-id: "+responseMap.get("txn-id"));
				response.getWriter().println("txn-amount: "+responseMap.get("txn-amount"));
				response.getWriter().println("txn-datetime: "+responseMap.get("txn-datetime"));
				response.getWriter().println("wallet-txn-datetime: "+responseMap.get("wallet-txn-datetime"));
				response.getWriter().println("wallet-txn-id: "+responseMap.get("wallet-txn-id"));
				response.getWriter().println("wallet-txn-remarks: "+responseMap.get("wallet-txn-remarks"));
				response.getWriter().println("wallet-txn-status: "+responseMap.get("wallet-txn-status"));
				
			}
			if(null != responseMap.get("checksum") && ((null != responseMap.get("txn-id") && responseMap.get("txn-id").toString().trim().length() > 0)
					|| (null != responseMap.get("wallet-txn-id") && responseMap.get("wallet-txn-id").toString().trim().length() > 0)))
			{
				transactionID = responseMap.get("txn-id");
				transactionAmount = responseMap.get("txn-amount");
				checksum = responseMap.get("checksum");
				txnDateTime = responseMap.get("txn-datetime");
				walletTrnsctnID = responseMap.get("wallet-txn-id");
				walletTxnDateTime = responseMap.get("wallet-txn-datetime");
				walletTxnRemarks = responseMap.get("wallet-txn-remarks");
				walletTxnStatus = responseMap.get("wallet-txn-status");
				pymtMode = responseMap.get("pg-mode");
				brn = responseMap.get("wallet-bank-ref-id");
				merchantCode = responseMap.get("");
				currency = "INR";//responseMap.get("");
				status = "Success";
				
				JsonObject result = new JsonObject();
				result.addProperty("txnID", transactionID);
				result.addProperty("txnAmount", transactionAmount);
				result.addProperty("aggrTxnID", walletTrnsctnID);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("txnStatus", walletTxnStatus);
				result.addProperty("currency", currency); //No Parameters available from bank
				result.addProperty("remarks", walletTxnRemarks);
				result.addProperty("pymtMode", pymtMode);
				result.addProperty("brn", brn);
				result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
				result.addProperty("status", status);
				
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				status = "Failed";
//				response.getWriter().print("error");
				errorStatus = responseMap.get("error_status");
				errorMsg = responseMap.get("error_message");
				JsonObject result = new JsonObject();
				
				result.addProperty("txnID", transactionID);
				result.addProperty("txnAmount", transactionAmount);
				result.addProperty("aggrTxnID", walletTrnsctnID);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("txnStatus", walletTxnStatus);
				result.addProperty("currency", currency); //No Parameters available from bank
				result.addProperty("remarks", walletTxnRemarks);
				result.addProperty("pymtMode", pymtMode);
				result.addProperty("brn", brn);
				result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
				result.addProperty("status", status);
				
				result.addProperty("errorCode", errorStatus);
				result.addProperty("errorMsg", errorMsg);
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			
			result.addProperty("txnID", "");
			result.addProperty("txnAmount", "");
			result.addProperty("aggrTxnID", "");
			result.addProperty("txnDateTime", "");
			result.addProperty("txnStatus", "");
			result.addProperty("currency", ""); //No Parameters available from bank
			result.addProperty("remarks", "");
			result.addProperty("pymtMode", "");
			result.addProperty("brn", "");
			result.addProperty("merchantCode", "");//No Parameters available from bank
			result.addProperty("status", "Failed");
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
	
			result.addProperty("errorCode", "I");
			result.addProperty("errorMsg", buffer.toString());
			response.getWriter().print(new Gson().toJson(result));		
		}
	}
	
	private void getAxisBankTransactionStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
			//All hardcoded values- to be modified
			String requestURL="", coopParam="", re1="", decryptedData="", sWSURL="", keyValue="", version="", merchantCode="", pgType="", referenceId="", customerRefNo="", bankRefNo="", checkSumInput="", checksum="", parameterData="", encryptedData="", encryptionKey = "";
			boolean isParamMissing = false;
//			version = "1.0";
//			corporateID = "3621";
//			pgType = "TEST";
			referenceId = request.getParameter("txnID");
			customerRefNo = request.getParameter("cpNo");
			bankRefNo = request.getParameter("bankRefNo");
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			keyValue = properties.getProperty("checkSumKey");
			//////////////////////////////////////////
			
			String wholeParamString="", paramName="", paramValue="", pgID="", secretCode="";
			wholeParamString = configurationValues;
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("configurationValues jkt mtd: "+configurationValues);
			
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
	        			requestURL = "https://uat-etendering.axisbank.co.in/easypay2.0/frontend/index.php/api/enquiry?i=";
	        		}
	        		else
	        		{
	        			encryptionKey = properties.getProperty("axisEncryptionKeyPRD");
	        			keyValue = properties.getProperty("checkSumKeyPRD");
	        			requestURL = "https://easypay.axisbank.co.in/index.php/api/enquiry?i=";
	        		}
	        	}
			}
			
			if((null == version || version.equalsIgnoreCase(""))
				||(null == merchantCode || merchantCode.equalsIgnoreCase(""))
				||(null == pgType || pgType.equalsIgnoreCase(""))
				||(null == referenceId || referenceId.equalsIgnoreCase(""))
				||(null == customerRefNo || customerRefNo.equalsIgnoreCase("")))
			{
				isParamMissing = true;
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("secretCode: "+secretCode);
				response.getWriter().println("encryptionKey: "+encryptionKey);
			}
			if(! isParamMissing)
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("isParamMissing: "+isParamMissing);
				
				coopParam = encryptDataForAxisPG(merchantCode, secretCode);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("coopParam: "+coopParam);
				checkSumInput = merchantCode+referenceId+customerRefNo+keyValue;
				checksum = generateCheckSum(checkSumInput);

				parameterData = parameterData+"CID="+merchantCode+"&RID="+referenceId+"&CRN="+customerRefNo+"&VER="+version+"&TYP="+pgType+"&BRN="+bankRefNo+"&CKS="+checksum;

				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("raw payload: "+parameterData);

				encryptedData = encryptDataForAxisPG(parameterData, encryptionKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("encryptedData: "+encryptedData);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("requestURL: "+requestURL+encryptedData+"&coop="+coopParam);

				URL url = new URL(requestURL+encryptedData+"&coop="+coopParam);
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.setRequestMethod("POST");
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, new java.security.SecureRandom()); 
				con.setSSLSocketFactory(sc.getSocketFactory());

				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
				while ((input = br.readLine()) != null){
				    decryptedData = decryptDataForAxisPG(input, encryptionKey);
				    if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("decryptedData: "+decryptedData);
				    if(null == decryptedData || decryptedData.trim().length() == 0)
					{
				    	errorMsgInput = input;
					}
				}

				br.close();
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("errorMsgInput: "+errorMsgInput);
				
				if(errorMsgInput != null && errorMsgInput.trim().length() > 0)
				{
					String[] errorMsgSplit = errorMsgInput.split("\\&");
					for(String s : errorMsgSplit)
					{
						if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().println("s: "+s);
						errorParam = s.substring(0, s.indexOf("="));
						errorValue = s.substring(s.indexOf("=")+1,s.length());
						if(errorParam.equalsIgnoreCase("error"))
			        	{
							errorCode = errorValue;
			        	}
	
						if(errorParam.equalsIgnoreCase("message"))
			        	{
							errorMsg = errorValue;
			        	}
					}
				}

				if(errorCode == null || errorCode.trim().length() == 0)
				{
					String[] resSplitResult = decryptedData.split("\\&");
					String brn="", txnStatus="", remarks="", aggrTxnID="", txnDateTime="", pymtMode="", txnID="", txnSessionID="", versionNo="", resMerchantCode="", 
							resPGType="", customerNo="", currency="", txnAmount="", checkSum="", resParamName="", resParamValue="", status="";
					for(String s : resSplitResult)
					{
						resParamName = s.substring(0, s.indexOf("="));
						resParamValue = s.substring(s.indexOf("=")+1,s.length());

			        	if(resParamName.equalsIgnoreCase("BRN"))
			        	{
			        		brn = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("STC"))
			        	{
			        		txnStatus = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("RMK"))
			        	{
			        		remarks = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("TRN"))
			        	{
			        		aggrTxnID = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("TET"))
			        	{
			        		txnDateTime = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("PMD"))
			        	{
			        		pymtMode = resParamValue;
			        	}

			        	if(resParamName.equalsIgnoreCase("RID"))
			        	{
			        		txnID = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("VER"))
			        	{
			        		versionNo = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CID"))
			        	{
			        		resMerchantCode = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("TYP"))
			        	{
			        		resPGType = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CRN"))
			        	{
			        		customerNo = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CNY"))
			        	{
			        		currency = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("AMT"))
			        	{
			        		txnAmount = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CKS"))
			        	{
			        		checkSum = resParamValue;
			        	}
					}
					
					status = "Success";
					
					JsonObject result = new JsonObject();
					result.addProperty("txnID", txnID);
					result.addProperty("txnAmount", txnAmount);
					result.addProperty("aggrTxnID", aggrTxnID);
					result.addProperty("txnDateTime", txnDateTime);
					result.addProperty("txnStatus", txnStatus);
					result.addProperty("currency", currency);
					result.addProperty("remarks", remarks);
					result.addProperty("pymtMode", pymtMode);
					result.addProperty("brn", brn);
					result.addProperty("merchantCode", resMerchantCode);
					result.addProperty("status", status);
					
					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
					String status = "Failed";

					JsonObject result = new JsonObject();
					
					result.addProperty("txnID", "");
					result.addProperty("txnAmount", "");
					result.addProperty("aggrTxnID", "");
					result.addProperty("txnDateTime", "");
					result.addProperty("txnStatus", "");
					result.addProperty("currency", "");
					result.addProperty("remarks", "");
					result.addProperty("pymtMode", "");
					result.addProperty("brn", "");
					result.addProperty("merchantCode", "");
					result.addProperty("status", status);
					
					result.addProperty("errorCode", errorCode);
					result.addProperty("errorMsg", errorMsg);
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				
				result.addProperty("txnID", "");
				result.addProperty("txnAmount", "");
				result.addProperty("aggrTxnID", "");
				result.addProperty("txnDateTime", "");
				result.addProperty("txnStatus", "");
				result.addProperty("currency", "");
				result.addProperty("remarks", "");
				result.addProperty("pymtMode", "");
				result.addProperty("brn", "");
				result.addProperty("merchantCode", "");
				result.addProperty("status", "");
				
				result.addProperty("errorCode", "Internal");
				result.addProperty("errorMsg", "Mandatory parameter(s) are missing");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e) {
			String status = "Failed";

			JsonObject result = new JsonObject();
			
			result.addProperty("txnID", "");
			result.addProperty("txnAmount", "");
			result.addProperty("aggrTxnID", "");
			result.addProperty("txnDateTime", "");
			result.addProperty("txnStatus", "");
			result.addProperty("currency", "");
			result.addProperty("remarks", "");
			result.addProperty("pymtMode", "");
			result.addProperty("brn", "");
			result.addProperty("merchantCode", "");
			result.addProperty("status", status);
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
	
			result.addProperty("errorCode", "I");
			result.addProperty("errorMsg", buffer.toString());
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
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
//	    	response.getWriter().println("Hex format : " + hexString.toString());
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
	
	public String encryptDataForAxisPG(String parameterData, String encryptionKey)
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
			return new String(Base64.getEncoder().encodeToString(encrypted));
		}
	}
	
	public String decryptDataForAxisPG(String parameterData, String encryptionKey)
	{
		byte[] output = null;
		String resWholeParamString = "";
		try
		{
			SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    cipher.init(Cipher.DECRYPT_MODE, skey);
		    output = cipher.doFinal(Base64.getDecoder().decode(parameterData));
//		    response.getWriter().println("output: "+output);
		    resWholeParamString = new String(output);
//		    response.getWriter().println("resWholeParamString: "+resWholeParamString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
//			response.getWriter().println("String: "+new String(Base64.encode(output)));
			return resWholeParamString;
//			resWholeParamString = new String(output);
		}
	}
	
	private void getRazorPayTxnStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) 
			throws IOException{
		String apiKey="", trackID="", apiSecret="", txnStsURL="";
		boolean debug = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		try{
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
				paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("APIKey")){
	        		apiKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecretKey")){
	        		apiSecret = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStsURL = paramValue;
	        	}
			}
			
			if(debug){
				response.getWriter().println("apiKey: "+apiKey);
				response.getWriter().println("apiSecret: "+apiSecret);
				response.getWriter().println("txnStsURL: "+txnStsURL);
			}
			
			trackID = request.getParameter("txnID");
			
			if(debug){
				response.getWriter().println("orderID: "+trackID);
			}
			
			RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);
			Order orderObj = razorpayClient.Orders.fetch(trackID);
			
			if(debug)
				response.getWriter().println("orderObj.toString(): "+orderObj.toString());
			
			JsonParser ordParser = new JsonParser();
			JsonObject orderResponse = (JsonObject)ordParser.parse(orderObj.toString());
			
			if(debug){
				response.getWriter().println("orderResponse: "+orderResponse);
				response.getWriter().println(orderObj.toJson());
			}

			String txnID="", brn="", transactionAmount="", aggrTxnID="", txnDateTime="", currency="", remarks="", pymtMode="", merchantCode="", errorStatus="", 
		        	   errorMsg="", txnStatus="", pgTxnMessage="", pgTxnErrorCode="";
			BigDecimal txnAmt = new BigDecimal(0);
			String status="";
			if(! orderObj.has("error")){
				status = "success";
				
				List<Payment> payments = razorpayClient.Orders.fetchPayments(trackID);
//				JsonObject paymentJson = razorpayClient.Orders.fetchPayments(orderID);
				
				if(debug){
					response.getWriter().println("payments.size(): "+payments.size());
					response.getWriter().println("payments: "+payments);
				}
				
//				Gson gson = new Gson();
//				gson.toJson(payments);
				
				if(payments.size() > 0){
					StringBuilder sb = new StringBuilder();
					 
			        int j = 0;
			        JsonObject paymentsObj = new JsonObject();
			        JsonArray paymentsArray = new JsonArray();
			        while (j <= payments.size() - 1)
			        {
			        	sb.append(payments.get(j));
		        		JsonParser parser = new JsonParser();
		        		
		        		if(debug)
		        			response.getWriter().println("payments.get("+j+"): "+payments.get(j).toString());
		        		
			            paymentsObj = (JsonObject)parser.parse(payments.get(j).toString());
			            paymentsArray.add(paymentsObj);
			            j++;
			        }
			 
			        String paymentsResp = sb.toString();
			        if(debug)
			        	response.getWriter().println("Payments: "+paymentsResp);
			        
			        JsonObject finalPaymentObj = new JsonObject();
					finalPaymentObj.add("results", paymentsArray);
					
					if(debug)
						response.getWriter().println("finalPaymentObj: "+finalPaymentObj);
					
					JsonObject latestPymt = new JsonObject();
					for(int i=0 ; i<finalPaymentObj.get("results").getAsJsonArray().size() ; i++){
						latestPymt = null;
						if(debug)
							response.getWriter().println("finalPaymentObj: "+finalPaymentObj.get("results").getAsJsonArray().get(i).getAsJsonObject().get("status").getAsString());
						if(finalPaymentObj.get("results").getAsJsonArray().get(i).getAsJsonObject().get("status").getAsString().equalsIgnoreCase("captured")
							|| finalPaymentObj.get("results").getAsJsonArray().get(i).getAsJsonObject().get("status").getAsString().equalsIgnoreCase("authorized")){
							latestPymt = finalPaymentObj.get("results").getAsJsonArray().get(i).getAsJsonObject();
							
							break;
						}else{
							if(i==0){
								latestPymt = finalPaymentObj.get("results").getAsJsonArray().get(i).getAsJsonObject();
							}
						}
					}
			        
			        txnID = orderObj.get("receipt");
			        txnAmt = new BigDecimal(orderResponse.get("amount_paid").getAsBigInteger()).divide(new BigDecimal(100));
			        transactionAmount = ""+txnAmt;
			        aggrTxnID = latestPymt.get("id").getAsString();
			        txnDateTime = latestPymt.get("created_at").getAsString();
			        
			        /*if(latestPymt.get("status").getAsString().equalsIgnoreCase("paid"))
			        	txnStatus = "000200";
			        else*/ 
			        if(latestPymt.get("status").getAsString().equalsIgnoreCase("captured"))
			        	txnStatus = "000200";
			        else if(latestPymt.get("status").getAsString().equalsIgnoreCase("authorized"))
			        	txnStatus = "000200";
			        else if(latestPymt.get("status").getAsString().equalsIgnoreCase("created"))
			        	txnStatus = "000330";
			        else if(latestPymt.get("status").getAsString().equalsIgnoreCase("failed"))
			        	txnStatus = "000320";
			        else if(latestPymt.get("status").getAsString().equalsIgnoreCase("attempted"))
			        	txnStatus = "000320";
			        else if(latestPymt.get("status").getAsString().equalsIgnoreCase("created"))
			        	txnStatus = "000330";
			        
			        if(debug)
			        	response.getWriter().println("Converted txnStatus: "+txnStatus);
			        
			        currency = orderObj.get("currency");
			        pymtMode = latestPymt.get("method").getAsString();
			        brn = orderObj.get("id");
			        
			        if(! latestPymt.get("error_code").isJsonNull()){
				        pgTxnMessage = latestPymt.get("error_reason").getAsString()+"-"+latestPymt.get("error_description").getAsString();
				        pgTxnErrorCode = latestPymt.get("error_code").getAsString();
			        }
			        
			        JsonObject result = new JsonObject();
					
					result.addProperty("txnID", txnID);
					result.addProperty("txnAmount", transactionAmount);
					result.addProperty("aggrTxnID", aggrTxnID);
					result.addProperty("txnDateTime", txnDateTime);
					result.addProperty("txnStatus", txnStatus);
					result.addProperty("currency", currency); //No Parameters available from bank
					result.addProperty("remarks", remarks);
					result.addProperty("pymtMode", pymtMode);
					result.addProperty("brn", brn);
					result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
					result.addProperty("status", status);
					result.addProperty("pgTxnMessage", pgTxnMessage);
					result.addProperty("pgTxnErrorCode", pgTxnErrorCode);
					result.addProperty("errorCode", errorStatus);
					result.addProperty("errorMsg", errorMsg);
					
					response.setContentType("application/json");
					response.getWriter().print(new Gson().toJson(result));
				}else{
					txnID = orderObj.get("receipt");
			        txnAmt = new BigDecimal(orderResponse.get("amount_paid").getAsBigInteger()).divide(new BigDecimal(100));
			        transactionAmount = ""+txnAmt;
			        txnStatus = "000320";
			        pgTxnMessage = "Payment has not reached bank";
			        
					JsonObject result = new JsonObject();
					
					result.addProperty("txnID", txnID);
					result.addProperty("txnAmount", transactionAmount);
					result.addProperty("aggrTxnID", aggrTxnID);
					result.addProperty("txnDateTime", txnDateTime);
					result.addProperty("txnStatus", txnStatus);
					result.addProperty("currency", currency); //No Parameters available from bank
					result.addProperty("remarks", remarks);
					result.addProperty("pymtMode", pymtMode);
					result.addProperty("brn", brn);
					result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
					result.addProperty("status", status);
					result.addProperty("pgTxnMessage", pgTxnMessage);
					result.addProperty("pgTxnErrorCode", pgTxnErrorCode);
					result.addProperty("errorCode", errorStatus);
					result.addProperty("errorMsg", errorMsg);
					
					response.setContentType("application/json");
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				txnStatus = "000320";
				status = "failed";
				pgTxnErrorCode = "";
				pgTxnMessage = "Payment Failed";
				JSONObject errorObj = orderObj.get("error"); 
				if(debug)
					response.getWriter().println("errorObj: "+errorObj);
				
				JsonObject result = new JsonObject();
				
				result.addProperty("txnID", txnID);
				result.addProperty("txnAmount", transactionAmount);
				result.addProperty("aggrTxnID", errorObj.getJSONObject("metadata").getString("payment_id"));
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("txnStatus", txnStatus);
				result.addProperty("currency", currency); //No Parameters available from bank
				result.addProperty("remarks", remarks);
				result.addProperty("pymtMode", pymtMode);
				result.addProperty("brn", errorObj.getJSONObject("metadata").getString("order_id"));
				result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
				result.addProperty("status", status);
				result.addProperty("pgTxnMessage", pgTxnMessage);
				result.addProperty("pgTxnErrorCode", pgTxnErrorCode);
				result.addProperty("errorCode", errorObj.getString("code"));
				result.addProperty("errorMsg", errorObj.getString("reason")+"-"+errorObj.getString("description"));
				response.setContentType("application/json");
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			response.getWriter().println("Exception: "+buffer.toString());
			response.getWriter().println("getMessage: "+e.getMessage());
			response.getWriter().println("getLocalizedMessage: "+e.getLocalizedMessage());
			response.getWriter().println("getCause: "+e.getCause());
			response.getWriter().println("getClass: "+e.getClass());
		}
	}
	
	private void getCCAvenueTransactionStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException{
		String plainRequest="", trackID="", encRequest="", merchantCode="", schemeCode="", txnUrl="", txnStatusUrl="", workingKey="", statusURL="", accessCode="", schemeMode="";
		String statusCheckRequest="", responseFromCCAvenue="";
		try{
			
			trackID = request.getParameter("txnID");
			
			plainRequest="|"+trackID+"|";
			
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SchemeCode")){
	        		schemeCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WorkingKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Mode")){
	        		schemeMode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStatusUrl = paramValue;
	        	}
			}
			
			AesCryptUtil cryptoUtil = new AesCryptUtil(workingKey);
			encRequest = cryptoUtil.encrypt(plainRequest);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("plainRequest: "+plainRequest);
				response.getWriter().println("encRequest: "+encRequest);
			}
			
			statusCheckRequest = "enc_request="+encRequest+"&access_code="+accessCode+"&request_type=STRING&response_type=JSON&command=orderStatusTracker&order_no="+trackID+"&version=1.2";
			
			URL url = new URL(txnStatusUrl+"?"+statusCheckRequest);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, null, new java.security.SecureRandom()); 
			con.setSSLSocketFactory(sc.getSocketFactory());
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String input;
			String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
			while ((input = br.readLine()) != null){
				responseFromCCAvenue = input;
			}
			br.close();
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("responseFromCCAvenue: "+responseFromCCAvenue);
			
			String decryptedResponse="";
			
			String resParamName="", resParamValue="";
			String status="", encryptedResponse="";
			
			String[] splitResponseResult = responseFromCCAvenue.split("&");
			for(String s : splitResponseResult)
			{
				resParamName = s.substring(0, s.indexOf("="));
				resParamValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(resParamName.equalsIgnoreCase("status"))
	        		status = resParamValue;
	        	
	        	if(resParamName.equalsIgnoreCase("enc_response"))
	        		encryptedResponse = resParamValue;
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("status: "+status);
				response.getWriter().println("encryptedResponse: "+encryptedResponse);
			}
			
			String statusTxt="", responseCode="";
			
			if(status.equalsIgnoreCase("0")){
				AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
				decryptedResponse = aesUtil.decrypt(encryptedResponse);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("decryptedResponse: "+decryptedResponse);
				
				JsonObject decryptedObj = new JsonObject();
				JsonParser parser = new JsonParser();
				decryptedObj = (JsonObject)parser.parse(decryptedResponse);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("decryptedObj: "+decryptedObj);
				
				if(decryptedObj.get("status").getAsInt() == 0){
					statusTxt = "Success";
					
					if(decryptedObj.get("order_status").getAsString().length() > 6)
						responseCode = decryptedObj.get("order_status").getAsString().substring(0, 6);
					else
						responseCode = decryptedObj.get("order_status").getAsString();
					
					JsonObject result = new JsonObject();
					result.addProperty("txnID", decryptedObj.get("order_no").getAsString());
					result.addProperty("txnAmount", decryptedObj.get("order_amt").getAsBigDecimal());
					result.addProperty("aggrTxnID", decryptedObj.get("reference_no").getAsString());
					result.addProperty("txnDateTime", decryptedObj.get("order_date_time").getAsString());
					result.addProperty("txnStatus",responseCode.toUpperCase());//"":"Shipped"
					result.addProperty("currency", decryptedObj.get("order_currncy").getAsString());
					result.addProperty("remarks", decryptedObj.get("error_code").getAsString()+":"+decryptedObj.get("error_desc").getAsString());
					result.addProperty("pymtMode", "");
					result.addProperty("brn", decryptedObj.get("order_bank_ref_no").getAsString());
					result.addProperty("merchantCode", merchantCode);
					result.addProperty("status", statusTxt);
					
					response.getWriter().print(new Gson().toJson(result));
				}else{
					statusTxt = "Failed";
					JsonObject result = new JsonObject();
					
					result.addProperty("txnID", "");
					result.addProperty("txnAmount", "");
					result.addProperty("aggrTxnID", "");
					result.addProperty("txnDateTime", "");
					result.addProperty("txnStatus", "");
					result.addProperty("currency", "");
					result.addProperty("remarks", decryptedObj.get("error_code").getAsString()+":"+decryptedObj.get("error_desc").getAsString());
					result.addProperty("pymtMode", "");
					result.addProperty("brn", "");
					result.addProperty("merchantCode", "");
					result.addProperty("status", statusTxt);
					
					result.addProperty("errorCode", errorCode);
					result.addProperty("errorMsg", errorMsg);
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				statusTxt = "Failed";

				JsonObject result = new JsonObject();
				
				result.addProperty("txnID", "");
				result.addProperty("txnAmount", "");
				result.addProperty("aggrTxnID", "");
				result.addProperty("txnDateTime", "");
				result.addProperty("txnStatus", "");
				result.addProperty("currency", "");
				result.addProperty("remarks", "");
				result.addProperty("pymtMode", "");
				result.addProperty("brn", "");
				result.addProperty("merchantCode", "");
				result.addProperty("status", statusTxt);
				
				result.addProperty("errorCode", "");
				result.addProperty("errorMsg", responseFromCCAvenue);
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			response.getWriter().println("Exception: "+buffer.toString());
			response.getWriter().println("getMessage: "+e.getMessage());
			response.getWriter().println("getLocalizedMessage: "+e.getLocalizedMessage());
			response.getWriter().println("getCause: "+e.getCause());
			response.getWriter().println("getClass: "+e.getClass());
		}
	}
	
	private void getBflTransactionStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException{
		String paymentRRN="", requeryRRN="", trackID="", bflDealerCode="", dealerValKey="", statusTxt="";
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		
		JsonObject paymentTxnObj = new JsonObject();
		JsonObject cloudCPConfigObj = new JsonObject();
		JsonObject bflPayload = new JsonObject();
		JsonObject txnResponseObj = new JsonObject();
		try{
			trackID = request.getParameter("txnID");

			String wholeParamString="", paramName="", paramValue="";
			String pgName="", ivText="", requeryKey="", schemeCode="", txnStatusURL="", tenure="", pgID="", acqChannelID="", pgProvider="", inputCPGuid="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("PGName")){
	        		pgName = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("IVText")){
	        		ivText = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("RequeryKey")){
	        		requeryKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SchemeCode")){
	        		schemeCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStatusURL = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Tenure")){
	        		tenure = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AcquiringChannelID")){
	        		acqChannelID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGProvider")){
	        		pgProvider = paramValue;
	        	}
	        	
			}
			if(debug){
				response.getWriter().println("pgName: "+pgName);
				response.getWriter().println("ivText: "+ivText);
				response.getWriter().println("pgID: "+pgID);
				response.getWriter().println("requeryKey: "+requeryKey);
				response.getWriter().println("schemeCode: "+schemeCode);
				response.getWriter().println("txnStatusURL: "+txnStatusURL);
				response.getWriter().println("trackID: "+trackID);
				response.getWriter().println("pgProvider: "+pgProvider);
				response.getWriter().println("acqChannelID: "+acqChannelID);
			}
			
			paymentTxnObj = getPaymentTransaction(request, response, trackID, debug);
			if(debug){
				response.getWriter().println("paymentTxnObj: "+paymentTxnObj);
			}
			
			if(paymentTxnObj.get("Status").getAsString().equalsIgnoreCase("000001")){
				if(! paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").isJsonNull()
				&& paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").getAsString().trim().length() > 0){
					paymentRRN = paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").getAsString();
					requeryRRN = commonUtils.generateGUID(32);
					
					if(debug){
						response.getWriter().println("paymentRRN: "+paymentRRN);
						response.getWriter().println("requeryRRN: "+requeryRRN);
					}
					
					if(null != paymentTxnObj.get("Payment").getAsJsonObject().get("CPNo").getAsString() 
						&& paymentTxnObj.get("Payment").getAsJsonObject().get("CPNo").getAsString().trim().length() > 0){
					
						inputCPGuid = paymentTxnObj.get("Payment").getAsJsonObject().get("CPNo").getAsString();
						if(debug)
							response.getWriter().println("inputCPGuid: "+inputCPGuid);
						
						cloudCPConfigObj = commonUtils.getCPReferenceFromCloud(request, response, pgProvider, pgID, inputCPGuid, debug);
						if(debug){
							response.getWriter().println("cloudCPConfigObj: "+cloudCPConfigObj);
						}
						
						if(cloudCPConfigObj.get("Status").getAsString().equalsIgnoreCase("000001")){
							bflDealerCode = cloudCPConfigObj.get("CPReference").getAsJsonObject().get("Reference1").getAsString();
							dealerValKey = cloudCPConfigObj.get("CPReference").getAsJsonObject().get("Reference2").getAsString();
							
							if(debug){
								response.getWriter().println("bflDealerCode: "+bflDealerCode);
								response.getWriter().println("dealerValKey: "+dealerValKey);
							}
							
							bflPayload.addProperty("DEALERID", bflDealerCode);
							bflPayload.addProperty("REQID", requeryRRN);
							bflPayload.addProperty("VALKEY", dealerValKey);
							bflPayload.addProperty("REQUERYID", paymentRRN);
							bflPayload.addProperty("ACQCHNLID", acqChannelID);
							
							if(debug)
								response.getWriter().println("bflPayload for OTP Generate: "+bflPayload);
							
							String encodedValue="", sealValue="", encryptedResponse="", decodedValue="";
							encodedValue = CommonUtils.AES_Encode(bflPayload.toString(), requeryKey, ivText);
							if(debug)
								response.getWriter().println("encodedValue: "+encodedValue);
							sealValue = CommonUtils.getSealValue(encodedValue+requeryKey);
							if(debug)
								response.getWriter().println("sealValue: "+sealValue);
							
							try{
								Client client=Client.create();
								WebResource webResource = client.resource(txnStatusURL);
								encryptedResponse = webResource.header("SealValue", sealValue).type("application/json").post(String.class, "\""+encodedValue+"\"");
								if(debug)
									response.getWriter().println("encryptedResponse: "+encryptedResponse);
							}catch (Exception e) {
								StackTraceElement element[] = e.getStackTrace();
								StringBuffer buffer = new StringBuffer();
								for(int i=0;i<element.length;i++)
								{
									buffer.append(element[i]);
								}
								if(debug){
									response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
								}
								
								statusTxt = "Failed";
								
								txnResponseObj.addProperty("txnID", trackID);
								txnResponseObj.addProperty("txnAmount", "0");//field not available
								txnResponseObj.addProperty("aggrTxnID", paymentRRN);
								txnResponseObj.addProperty("txnDateTime", "");
								txnResponseObj.addProperty("txnStatus", "");
								txnResponseObj.addProperty("currency", "INR");
								txnResponseObj.addProperty("remarks", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
								txnResponseObj.addProperty("pymtMode", "");
								txnResponseObj.addProperty("brn", "");
								txnResponseObj.addProperty("merchantCode", "");
								txnResponseObj.addProperty("TransactionType", "");
								txnResponseObj.addProperty("Charges", "0");
								txnResponseObj.addProperty("AdvEMIAmt", "0");
								txnResponseObj.addProperty("TotalDownPymnt", "0");
								txnResponseObj.addProperty("EmiPerMonth", "0");
								txnResponseObj.addProperty("PF", "0");
								txnResponseObj.addProperty("Tenure", "0");
								txnResponseObj.addProperty("AdvEMITenure", "0");
								txnResponseObj.addProperty("FirstDueDate", "");
								txnResponseObj.addProperty("RateOfInterest", "");
								txnResponseObj.addProperty("CardNo", "");
								txnResponseObj.addProperty("NameOnCard", "");
								txnResponseObj.addProperty("ConvenienceFee", "0");
								txnResponseObj.addProperty("ExceptionTrace", buffer.toString());
								txnResponseObj.addProperty("status", statusTxt);
								response.getWriter().print(new Gson().toJson(txnResponseObj));
							}
							
							if(encryptedResponse != null){
								encryptedResponse = encryptedResponse.replace("\"", "");
								if(debug){
									response.getWriter().println("encryptedResponse after removing double quotes: "+encryptedResponse);
								    response.getWriter().println("input for decryption: "+encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")));
								}
					        	
					        	decodedValue = CommonUtils.AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),requeryKey, ivText);//Actual payload
//								decodedValue = OTPRequest.AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),txnKey);
								
					        	if(debug)
					        		response.getWriter().println("decodedValue after calling POST: "+decodedValue);
					        	
					        	JsonParser bflResponseParser = new JsonParser();
								JsonObject bflResponseJson = (JsonObject)bflResponseParser.parse(decodedValue);
								
								if(debug){
									response.getWriter().println("bflResponseJson: "+bflResponseJson);
									response.getWriter().println("bflResponseJson.RSPCODE: "+bflResponseJson.get("RESCODE").getAsString());
									response.getWriter().println("bflResponseJson.ERRDESC: "+bflResponseJson.get("ERRDESC").getAsString());
								}
								
//								statusTxt = "Failed";
//								statusTxt = "Success";
								if(bflResponseJson.get("RESCODE").getAsString().equalsIgnoreCase("00")
									|| bflResponseJson.get("RESCODE").getAsString().equalsIgnoreCase("0")){
									JsonObject enqRequestObject = bflResponseJson.get("ENQINFO").getAsJsonArray().get(0).getAsJsonObject();
									
									statusTxt = "Success";
									
									if(enqRequestObject.has("ORDERNO"))
										txnResponseObj.addProperty("txnID", enqRequestObject.get("ORDERNO").getAsString());
									else
										txnResponseObj.addProperty("txnID", trackID);
									
									if(enqRequestObject.has("LOANAMT"))
										txnResponseObj.addProperty("txnAmount", enqRequestObject.get("LOANAMT").getAsString());
									else
										txnResponseObj.addProperty("txnAmount", "0");
										
									txnResponseObj.addProperty("aggrTxnID", paymentRRN);
									
									if(enqRequestObject.has("RESDTT"))
										txnResponseObj.addProperty("txnDateTime", enqRequestObject.get("RESDTT").getAsString());
									else
										txnResponseObj.addProperty("txnDateTime", "");
									
									if(enqRequestObject.has("RSPCODE"))
										txnResponseObj.addProperty("txnStatus", enqRequestObject.get("RSPCODE").getAsString());
									else
										txnResponseObj.addProperty("txnStatus", "");
										
									txnResponseObj.addProperty("currency", "INR");
									
									if(enqRequestObject.has("ERRDESC"))
										txnResponseObj.addProperty("remarks", enqRequestObject.get("ERRDESC").getAsString());
									else
										txnResponseObj.addProperty("remarks", "");
									
									txnResponseObj.addProperty("pymtMode", "");
									
									if(enqRequestObject.has("DEALID"))
										txnResponseObj.addProperty("brn", enqRequestObject.get("DEALID").getAsString());
									else
										txnResponseObj.addProperty("brn", "");
									
									txnResponseObj.addProperty("merchantCode", "");
									
									if(enqRequestObject.has("TXNTYPE"))
										txnResponseObj.addProperty("TransactionType", enqRequestObject.get("TXNTYPE").getAsString());
									else
										txnResponseObj.addProperty("TransactionType", "");
									
									if(enqRequestObject.has("CHARGES"))
										txnResponseObj.addProperty("Charges", enqRequestObject.get("CHARGES").getAsString());
									else
										txnResponseObj.addProperty("Charges", "0");
									
									if(enqRequestObject.has("ADVEMIAMT"))
										txnResponseObj.addProperty("AdvEMIAmt", enqRequestObject.get("ADVEMIAMT").getAsString());
									else
										txnResponseObj.addProperty("AdvEMIAmt", "0");
									
									if(enqRequestObject.has("DOWNPAYMT"))
										txnResponseObj.addProperty("TotalDownPymnt", enqRequestObject.get("DOWNPAYMT").getAsString());
									else
										txnResponseObj.addProperty("TotalDownPymnt", "0");
									
									if(enqRequestObject.has("EMIPERMONTH")){
										txnResponseObj.addProperty("EmiPerMonth", enqRequestObject.get("EMIPERMONTH").getAsString());
									}else{
										if(enqRequestObject.has("EMIAMT")){
											txnResponseObj.addProperty("EmiPerMonth", enqRequestObject.get("EMIAMT").getAsString());
										}else{
											txnResponseObj.addProperty("EmiPerMonth", "0");
										}
									}

									if(enqRequestObject.has("PF"))
										txnResponseObj.addProperty("PF", enqRequestObject.get("PF").getAsString());
									else
										txnResponseObj.addProperty("PF", "0");
									
									if(enqRequestObject.has("TENURE"))
										txnResponseObj.addProperty("Tenure", enqRequestObject.get("TENURE").getAsString());
									else
										txnResponseObj.addProperty("Tenure", "0");
									
									if(enqRequestObject.has("ADVEMITENURE"))
										txnResponseObj.addProperty("AdvEMITenure", enqRequestObject.get("ADVEMITENURE").getAsString());
									else
										txnResponseObj.addProperty("AdvEMITenure", "0");
									
									if(enqRequestObject.has("FDD"))
										txnResponseObj.addProperty("FirstDueDate", enqRequestObject.get("FDD").getAsString());
									else
										txnResponseObj.addProperty("FirstDueDate", "");
									
									if(enqRequestObject.has("ROI"))
										txnResponseObj.addProperty("RateOfInterest", enqRequestObject.get("ROI").getAsString());
									else
										txnResponseObj.addProperty("RateOfInterest", "");
									
									if(enqRequestObject.has("CN"))
										txnResponseObj.addProperty("CardNo", enqRequestObject.get("CN").getAsString());
									else
										txnResponseObj.addProperty("CardNo", "");
									
									if(enqRequestObject.has("NAMEONCARD"))
										txnResponseObj.addProperty("NameOnCard", enqRequestObject.get("NAMEONCARD").getAsString());
									else
										txnResponseObj.addProperty("NameOnCard", "");
									
									if(enqRequestObject.has("PROCESSINGFEE"))
										txnResponseObj.addProperty("ConvenienceFee", enqRequestObject.get("PROCESSINGFEE").getAsString());
									else
										txnResponseObj.addProperty("ConvenienceFee", "0");
										
									txnResponseObj.addProperty("status", statusTxt);
									
									response.getWriter().print(new Gson().toJson(txnResponseObj));
								}else{
									//Error from api response
									statusTxt = "Failed";
									
									txnResponseObj.addProperty("txnID", trackID);
									txnResponseObj.addProperty("txnAmount", "0");
									txnResponseObj.addProperty("aggrTxnID", paymentRRN);
									txnResponseObj.addProperty("txnDateTime", "");
									
									if(bflResponseJson.has("RESCODE"))
										txnResponseObj.addProperty("txnStatus", bflResponseJson.get("RESCODE").getAsString());
									else
										txnResponseObj.addProperty("txnStatus", "");
									
									txnResponseObj.addProperty("currency", "INR");
									if(bflResponseJson.has("ERRDESC"))
										txnResponseObj.addProperty("remarks", bflResponseJson.get("ERRDESC").getAsString());
									else
										txnResponseObj.addProperty("remarks", "Unable to fetch response: "+bflResponseJson.toString());
									
									txnResponseObj.addProperty("pymtMode", "");
									txnResponseObj.addProperty("brn", "");
									txnResponseObj.addProperty("merchantCode", "");
									txnResponseObj.addProperty("TransactionType", "");
									txnResponseObj.addProperty("Charges", "0");
									txnResponseObj.addProperty("AdvEMIAmt", "0");
									txnResponseObj.addProperty("TotalDownPymnt", "0");
									txnResponseObj.addProperty("EmiPerMonth", "0");
									txnResponseObj.addProperty("PF", "0");
									txnResponseObj.addProperty("Tenure", "0");
									txnResponseObj.addProperty("AdvEMITenure", "0");
									txnResponseObj.addProperty("FirstDueDate", "");
									txnResponseObj.addProperty("RateOfInterest", "");
									txnResponseObj.addProperty("CardNo", "");
									txnResponseObj.addProperty("NameOnCard", "");
									txnResponseObj.addProperty("ConvenienceFee", "0");
									txnResponseObj.addProperty("status", statusTxt);
									
									response.getWriter().print(new Gson().toJson(txnResponseObj));
								}
							}else{
								//api response null error response
								statusTxt = "Failed";
								
								txnResponseObj.addProperty("txnID", trackID);
								txnResponseObj.addProperty("txnAmount", "0");
								txnResponseObj.addProperty("aggrTxnID", paymentRRN);
								txnResponseObj.addProperty("txnDateTime", "");
								txnResponseObj.addProperty("txnStatus", "");
								txnResponseObj.addProperty("currency", "INR");
								txnResponseObj.addProperty("remarks", "No response received from API");
								txnResponseObj.addProperty("pymtMode", "");
								txnResponseObj.addProperty("brn", "");
								txnResponseObj.addProperty("merchantCode", "");
								txnResponseObj.addProperty("TransactionType", "");
								txnResponseObj.addProperty("Charges", "0");
								txnResponseObj.addProperty("AdvEMIAmt", "0");
								txnResponseObj.addProperty("TotalDownPymnt", "0");
								txnResponseObj.addProperty("EmiPerMonth", "0");
								txnResponseObj.addProperty("PF", "0");
								txnResponseObj.addProperty("Tenure", "0");
								txnResponseObj.addProperty("AdvEMITenure", "0");
								txnResponseObj.addProperty("FirstDueDate", "");
								txnResponseObj.addProperty("RateOfInterest", "");
								txnResponseObj.addProperty("CardNo", "");
								txnResponseObj.addProperty("NameOnCard", "");
								txnResponseObj.addProperty("ConvenienceFee", "0");
								txnResponseObj.addProperty("status", statusTxt);
								
								response.getWriter().print(new Gson().toJson(txnResponseObj));
							}
						}else{
							//Dealer mapping not done error response
	//						response.getWriter().println(cloudCPConfigObj);
							statusTxt = "Failed";
							
							txnResponseObj.addProperty("txnID", trackID);
							txnResponseObj.addProperty("txnAmount", "0");//field not available
							txnResponseObj.addProperty("aggrTxnID", paymentRRN);
							txnResponseObj.addProperty("txnDateTime", "");
							txnResponseObj.addProperty("txnStatus", "");
							txnResponseObj.addProperty("currency", "INR");
							txnResponseObj.addProperty("remarks", "Dealer Mapping not done for the user");
							txnResponseObj.addProperty("pymtMode", "");
							txnResponseObj.addProperty("brn", "");
							txnResponseObj.addProperty("merchantCode", "");
							txnResponseObj.addProperty("TransactionType", "");
							txnResponseObj.addProperty("Charges", "0");
							txnResponseObj.addProperty("AdvEMIAmt", "0");
							txnResponseObj.addProperty("TotalDownPymnt", "0");
							txnResponseObj.addProperty("EmiPerMonth", "0");
							txnResponseObj.addProperty("PF", "0");
							txnResponseObj.addProperty("Tenure", "0");
							txnResponseObj.addProperty("AdvEMITenure", "0");
							txnResponseObj.addProperty("FirstDueDate", "");
							txnResponseObj.addProperty("RateOfInterest", "");
							txnResponseObj.addProperty("CardNo", "");
							txnResponseObj.addProperty("NameOnCard", "");
							txnResponseObj.addProperty("ConvenienceFee", "0");
							txnResponseObj.addProperty("status", statusTxt);
							
							response.getWriter().print(new Gson().toJson(txnResponseObj));
						}
					}else{
						//CPGuid not found from payment transaction error response
						statusTxt = "Failed";
						
						txnResponseObj.addProperty("txnID", trackID);
						txnResponseObj.addProperty("txnAmount", "0");//field not available
						txnResponseObj.addProperty("aggrTxnID", paymentRRN);
						txnResponseObj.addProperty("txnDateTime", "");
						txnResponseObj.addProperty("txnStatus", "");
						txnResponseObj.addProperty("currency", "INR");
						txnResponseObj.addProperty("remarks", "Dealer Code not associated with transaction");
						txnResponseObj.addProperty("pymtMode", "");
						txnResponseObj.addProperty("brn", "");
						txnResponseObj.addProperty("merchantCode", "");
						txnResponseObj.addProperty("TransactionType", "");
						txnResponseObj.addProperty("Charges", "0");
						txnResponseObj.addProperty("AdvEMIAmt", "0");
						txnResponseObj.addProperty("TotalDownPymnt", "0");
						txnResponseObj.addProperty("EmiPerMonth", "0");
						txnResponseObj.addProperty("PF", "0");
						txnResponseObj.addProperty("Tenure", "0");
						txnResponseObj.addProperty("AdvEMITenure", "0");
						txnResponseObj.addProperty("FirstDueDate", "");
						txnResponseObj.addProperty("RateOfInterest", "");
						txnResponseObj.addProperty("CardNo", "");
						txnResponseObj.addProperty("NameOnCard", "");
						txnResponseObj.addProperty("ConvenienceFee", "0");
						txnResponseObj.addProperty("status", statusTxt);
						
						response.getWriter().print(new Gson().toJson(txnResponseObj));
					}
				}else{
					//PGTrackID not found error response
					statusTxt = "Failed";
					
					txnResponseObj.addProperty("txnID", trackID);
					txnResponseObj.addProperty("txnAmount", "0");//field not available
					txnResponseObj.addProperty("aggrTxnID", paymentRRN);
					txnResponseObj.addProperty("txnDateTime", "");
					txnResponseObj.addProperty("txnStatus", "");
					txnResponseObj.addProperty("currency", "INR");
					txnResponseObj.addProperty("remarks", "Unable to get Requery ID associated with the transaction id: "+trackID);
					txnResponseObj.addProperty("pymtMode", "");
					txnResponseObj.addProperty("brn", "");
					txnResponseObj.addProperty("merchantCode", "");
					txnResponseObj.addProperty("TransactionType", "");
					txnResponseObj.addProperty("Charges", "0");
					txnResponseObj.addProperty("AdvEMIAmt", "0");
					txnResponseObj.addProperty("TotalDownPymnt", "0");
					txnResponseObj.addProperty("EmiPerMonth", "0");
					txnResponseObj.addProperty("PF", "0");
					txnResponseObj.addProperty("Tenure", "0");
					txnResponseObj.addProperty("AdvEMITenure", "0");
					txnResponseObj.addProperty("FirstDueDate", "");
					txnResponseObj.addProperty("RateOfInterest", "");
					txnResponseObj.addProperty("CardNo", "");
					txnResponseObj.addProperty("NameOnCard", "");
					txnResponseObj.addProperty("ConvenienceFee", "0");
					txnResponseObj.addProperty("status", statusTxt);
					
					response.getWriter().print(new Gson().toJson(txnResponseObj));
				}
			}else{
				//Transaction not found error response
				statusTxt = "Failed";
				
				txnResponseObj.addProperty("txnID", trackID);
				txnResponseObj.addProperty("txnAmount", "0");//field not available
				txnResponseObj.addProperty("aggrTxnID", paymentRRN);
				txnResponseObj.addProperty("txnDateTime", "");
				txnResponseObj.addProperty("txnStatus", "");
				txnResponseObj.addProperty("currency", "INR");
				txnResponseObj.addProperty("remarks", "No transaction found with the id: "+trackID);
				txnResponseObj.addProperty("pymtMode", "");
				txnResponseObj.addProperty("brn", "");
				txnResponseObj.addProperty("merchantCode", "");
				txnResponseObj.addProperty("TransactionType", "");
				txnResponseObj.addProperty("Charges", "0");
				txnResponseObj.addProperty("AdvEMIAmt", "0");
				txnResponseObj.addProperty("TotalDownPymnt", "0");
				txnResponseObj.addProperty("EmiPerMonth", "0");
				txnResponseObj.addProperty("PF", "0");
				txnResponseObj.addProperty("Tenure", "0");
				txnResponseObj.addProperty("AdvEMITenure", "0");
				txnResponseObj.addProperty("FirstDueDate", "");
				txnResponseObj.addProperty("RateOfInterest", "");
				txnResponseObj.addProperty("CardNo", "");
				txnResponseObj.addProperty("NameOnCard", "");
				txnResponseObj.addProperty("ConvenienceFee", "0");
				txnResponseObj.addProperty("status", statusTxt);
				
				response.getWriter().print(new Gson().toJson(txnResponseObj));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug){
				response.getWriter().println("Exception: "+buffer.toString());
				response.getWriter().println("getMessage: "+e.getMessage());
				response.getWriter().println("getLocalizedMessage: "+e.getLocalizedMessage());
				response.getWriter().println("getCause: "+e.getCause());
				response.getWriter().println("getClass: "+e.getClass());
			}
			
			statusTxt = "Failed";
			
			txnResponseObj.addProperty("txnID", trackID);
			txnResponseObj.addProperty("txnAmount", "0");//field not available
			txnResponseObj.addProperty("aggrTxnID", paymentRRN);
			txnResponseObj.addProperty("txnDateTime", "");
			txnResponseObj.addProperty("txnStatus", "");
			txnResponseObj.addProperty("currency", "INR");
			txnResponseObj.addProperty("remarks", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			txnResponseObj.addProperty("pymtMode", "");
			txnResponseObj.addProperty("brn", "");
			txnResponseObj.addProperty("merchantCode", "");
			txnResponseObj.addProperty("TransactionType", "");
			txnResponseObj.addProperty("Charges", "0");
			txnResponseObj.addProperty("AdvEMIAmt", "0");
			txnResponseObj.addProperty("TotalDownPymnt", "0");
			txnResponseObj.addProperty("EmiPerMonth", "0");
			txnResponseObj.addProperty("PF", "0");
			txnResponseObj.addProperty("Tenure", "0");
			txnResponseObj.addProperty("AdvEMITenure", "0");
			txnResponseObj.addProperty("FirstDueDate", "");
			txnResponseObj.addProperty("RateOfInterest", "");
			txnResponseObj.addProperty("CardNo", "");
			txnResponseObj.addProperty("NameOnCard", "");
			txnResponseObj.addProperty("ConvenienceFee", "0");
			txnResponseObj.addProperty("ExceptionTrace", buffer.toString());
			txnResponseObj.addProperty("status", statusTxt);
			
			response.getWriter().print(new Gson().toJson(txnResponseObj));
		}
	}
	
	private void getYesPayUTransactionStatus(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException
	{
		try
		{
			Map<String, String> mapResponse = new LinkedHashMap<String, String>();
			//UAT
//			String merchantCode="101000000000781", bankID="000004", passCode="SVPL4257", terminalID="10100781", txnType="Status", transactionID="",customerRefNo="", bankRefNo="";
//			String SECURE_SECRET= "E59CD2BF6F4D86B5FB3897A680E0DD3E";
//			String StatusURL = "https://sandbox.isgpay.com/ISGPay/Status";
//			response.getWriter().println("configurationValues: "+configurationValues);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("configurationValues: "+configurationValues);
			//LIVE
//			String merchantCode="120000000002151", bankID="000004", passCode="ZBGQ9324", terminalID="11002151", txnType="Status", transactionID="",customerRefNo="", bankRefNo="";
//			String SECURE_SECRET= "88DEC9861F2CC0AE69DE126D92C4EED2";
//			String StatusURL = "https://isgpay.com/ISGPay/Status";
			
			
			/////////////////////////////////////////////////////////
			
			String merchantCode="", statusURL="", sWSURL="", currency="", version="", mcc="", sEncryptionKey="", bankID="", passCode="", terminalID="", txnType="Status", transactionID="",customerRefNo="", bankRefNo="";
			String sSecureSecret= "";
			int portNumber = 0;
			
			
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
	        	
//	        	if(paramName.equalsIgnoreCase("TxnType"))
//	        	{
//	        		txnType = paramValue;
////	        		response.getWriter().println("txnType: "+txnType);
//	        	}
			}
			
			//Comment after debug
//			merchantCode="120000000002151"; bankID="000004"; passCode="ZBGQ9324"; terminalID="11002151"; txnType="Status";
//			sSecureSecret= "88DEC9861F2CC0AE69DE126D92C4EED2";
			/////////////////////////////////////////////////////////
			
			transactionID = request.getParameter("txnID");
			customerRefNo = request.getParameter("cpNo");
			bankRefNo = request.getParameter("bankRefNo");
			
			if (merchantCode == null || "".equals(merchantCode)) {
				throw new Exception("Merchant ID Not Found In Request!!!");
			}
			if (terminalID == null || "".equals(terminalID)) {
				throw new Exception("Terminal ID Not Found In Request!!!");
			}
			if (bankID == null || "".equals(bankID)) {
				throw new Exception("Bank ID Not Found In Request!!!");
			}

			if(merchantCode.equalsIgnoreCase("101000000000781"))
			{
				statusURL = "https://sandbox.isgpay.com/ISGPay/Status";
				portNumber = 8443;
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("URL: "+statusURL+"||Port:"+portNumber);
			}
			else
			{
				statusURL = "https://isgpay.com/ISGPay/Status";
				portNumber = 443;
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("URL: "+statusURL+"||Port:"+portNumber);
			}
			
			Hashtable<String, String> pgParams = new Hashtable<String, String>();

			pgParams.put("TxnRefNo",transactionID);
			pgParams.put("MerchantId",merchantCode);
			pgParams.put("BankId",bankID);
			pgParams.put("PassCode",passCode);
			pgParams.put("TerminalId",terminalID);
			pgParams.put("TxnType",txnType);
			Enumeration e = pgParams.keys();
			
			Map requestFields = new HashMap();
			while(e.hasMoreElements()){
				String fieldName = (String) e.nextElement();
				String fieldValue = pgParams.get(fieldName);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				{
					response.getWriter().println("Request:");
					response.getWriter().println(fieldName+"="+fieldValue);
				}
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					requestFields.put(fieldName, fieldValue);
				}
			}
			

//			response.re
			if (sSecureSecret != null && sSecureSecret.length() > 0) {
				String secureHash = hashAllFields(response, requestFields, sSecureSecret);

				if ("".equals(secureHash)) {
					throw new Exception("Problem during Hashing....");
				}
				requestFields.put("SecureHash", secureHash);
				String postData = createPostDataFromMap(requestFields);
				mapResponse = doPostRequest(response, statusURL, postData, portNumber);
				
//				response.getWriter().println("mapResponse: "+mapResponse);
				
				String brn="", txnStatus="", remarks="", aggrTxnID="", txnDateTime="", pymtMode="", txnID="", txnSessionID="", versionNo="", resMerchantCode="", 
						resPGType="", customerNo="", txnAmount="", checkSum="", resParamName="", resParamValue="", status="";
				Set<String> keys = mapResponse.keySet();
				for(String k:keys){
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					{
						response.getWriter().println("Response:");
						response.getWriter().println(k+"="+mapResponse.get(k));
					}
					if(k.equalsIgnoreCase("TxnRefNo"))
					{
						txnID = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("Message"))
					{
						remarks = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("ResponseCode"))
					{
						txnStatus = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("Amount"))
					{
						if(! mapResponse.get(k).equalsIgnoreCase("N/A")){
							txnAmount = ""+Integer.parseInt(mapResponse.get(k))/100;
						}
						else{
							txnAmount = "";
						}
					}
					if(k.equalsIgnoreCase("pgTxnId"))
					{
						brn = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("RetRefNo"))
					{
						aggrTxnID = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("MerchantId"))
					{
						resMerchantCode = mapResponse.get(k);
					}
					if(k.equalsIgnoreCase("AuthCode"))
					{
						versionNo = mapResponse.get(k);
					}
//		            response.getWriter().println(k+" -- "+mapResponse.get(k));
		        }
				currency = "INR";
				
				/*JsonObject result = new JsonObject();
				result.addProperty("brn", brn);
				result.addProperty("txnStatus", txnStatus);
				result.addProperty("remarks", remarks);
				result.addProperty("aggrTxnID", aggrTxnID);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("pymtMode", pymtMode);
				result.addProperty("txnID", txnID);
				result.addProperty("versionNo", versionNo);
				result.addProperty("resMerchantCode", resMerchantCode);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("currency", currency);
				response.getWriter().print(new Gson().toJson(result));*/
				
				JsonObject result = new JsonObject();
				result.addProperty("txnID", transactionID);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("aggrTxnID", aggrTxnID);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("txnStatus", txnStatus);
				result.addProperty("currency", currency); //No Parameters available from bank
				result.addProperty("remarks", remarks);
				result.addProperty("pymtMode", pymtMode);
				result.addProperty("brn", brn);
				result.addProperty("merchantCode", merchantCode);//No Parameters available from bank
				
				if(! txnStatus.equalsIgnoreCase("00"))
				{
					result.addProperty("errorCode", txnStatus);
					result.addProperty("errorMsg", remarks);
					result.addProperty("status", "Success");
				}
				else
				{
					result.addProperty("status", "Failed");
				}
				
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				String status = "Failed";

				JsonObject result = new JsonObject();
				
				result.addProperty("txnID", "");
				result.addProperty("txnAmount", "");
				result.addProperty("aggrTxnID", "");
				result.addProperty("txnDateTime", "");
				result.addProperty("txnStatus", "");
				result.addProperty("currency", "");
				result.addProperty("remarks", "");
				result.addProperty("pymtMode", "");
				result.addProperty("brn", "");
				result.addProperty("merchantCode", "");
				result.addProperty("status", status);
				
		
				result.addProperty("errorCode", "I");
				result.addProperty("errorMsg", "SecureSecret can not be null or empty!!!");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e) 
		{
			String status = "Failed";

			JsonObject result = new JsonObject();
			
			result.addProperty("txnID", "");
			result.addProperty("txnAmount", "");
			result.addProperty("aggrTxnID", "");
			result.addProperty("txnDateTime", "");
			result.addProperty("txnStatus", "");
			result.addProperty("currency", "");
			result.addProperty("remarks", "");
			result.addProperty("pymtMode", "");
			result.addProperty("brn", "");
			result.addProperty("merchantCode", "");
			result.addProperty("status", status);
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}

			result.addProperty("errorCode", "I");
			result.addProperty("errorMsg", buffer.toString());
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	String hashAllFields(HttpServletResponse response, Map fields,String SECURE_SECRET) {
		StringBuffer hexString = null;
		try
		{
			List fieldNames = new ArrayList(fields.keySet());
			Collections.sort(fieldNames);

			StringBuffer buf = new StringBuffer();
			buf.append(SECURE_SECRET);

			Iterator itr = fieldNames.iterator();

			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) fields.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {

					buf.append(fieldValue);
				}
			}
//			response.getWriter().println("client buf=" + buf);
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(buf.toString().getBytes("UTF-8"));
				hexString = new StringBuffer();
				for (int i = 0; i < hash.length; i++) {
					String hex = Integer.toHexString(0xff & hash[i]);
					if (hex.length() == 1)
						hexString.append('0');
					hexString.append(hex);
				}

				
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject result = new JsonObject();
			result.addProperty("error", buffer.toString());
			result.addProperty("method", "hashAllFields");
			response.getWriter().print(new Gson().toJson(result));
		}
        finally{
        	return hexString.toString();
        }        
    }
	
	private String createPostDataFromMap(Map fields) {
		StringBuffer buf = new StringBuffer();
		try
		{
			String ampersand = "";

			// append all fields in a data string
			for (Iterator i = fields.keySet().iterator(); i.hasNext(); ) {

				String key = (String)i.next();
				String value = (String)fields.get(key);
//				response.getWriter().println("Key: "+key+"---- Value:"+value);
				if ((value != null) && (value.length() > 0)) {
					// append the parameters
					buf.append(ampersand);
					buf.append(URLEncoder.encode(key));
					buf.append('=');
					buf.append(URLEncoder.encode(value));
				}
				ampersand = "&";
			}

			// return string 
//			response.getWriter().println("buf.toString(): "+buf.toString());
		}
		catch (Exception e) {
			return "Error in createPostDataFromMap method";
		}
		finally
		{
			return buf.toString();
		}
    }
	
	public static Map<String, String> doPostRequest(HttpServletResponse response, String vpc_Host, String data, int portNo) throws Exception
	{
		Map<String, String> responseFields = null;
		boolean useProxy = false;
		String proxyHost = "", proxyPort = "";
		int vpc_Port = portNo;
		try
		{
			InputStream is = null;
			OutputStream os = null;
			
			String fileName = "";
			boolean useSSL = false;
			String vpcHost = null;
			X509TrustManager s_x509TrustManager = null;
			SSLContext sc = null;
			SSLSocketFactory s_sslSocketFactory = null;
			SSLSocket ssl = null;
			
			TrustManager[] trustAllCerts = new TrustManager[]{
					   new X509TrustManager() {
					      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					         return null;
					      }
					      public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
					      public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
					   }
					};
					HostnameVerifier allHostsValid = new HostnameVerifier() {
					   public boolean verify(String hostname, SSLSession session) {
					      return true;
					   }
					};
				
			sc= SSLContext.getInstance("TLSv1.2");
			   sc.init(null, null, new java.security.SecureRandom());
			   HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			   HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			   s_sslSocketFactory = sc.getSocketFactory();
			
			if (vpc_Host.substring(0, 8).equalsIgnoreCase("https://")) {
				useSSL = true;
				vpcHost = vpc_Host.substring(8);
				fileName = vpcHost.substring(vpcHost.indexOf("/"), vpcHost.length());
				vpcHost = vpcHost.substring(0, vpcHost.indexOf("/"));
			}
//			response.getWriter().println("vpcHost::"+vpcHost);
//			response.getWriter().println("vpcHost::"+useProxy);
			if (useProxy) {

				Socket s = new Socket(proxyHost, Integer.parseInt(proxyPort));
//				response.getWriter().println("s===="+s);
				os = s.getOutputStream();
				is = s.getInputStream();
				if (useSSL) {
					//String msg = "CONNECT " + vpcHost + ":" + vpc_Port + " HTTP/1.0\r\n"
					String msg = "CONNECT " + vpcHost + ":" + vpc_Port + " HTTP/1.0\r\n"
							+ "User-Agent: HTTP Client\r\n\r\n";
//					response.getWriter().println("vpcHost===="+vpcHost);
//					response.getWriter().println("vpc_Port===="+vpc_Port);
					os.write(msg.getBytes());
					byte[] buf = new byte[4096];
					int len = is.read(buf);
					String res = new String(buf, 0, len);

					if (res.indexOf("200") < 0) {
						throw new IOException("Proxy would now allow connection - " + res);
					}

//					response.getWriter().println("HandShake1");
					try {
						//response.getWriter().println("");
						ssl = (SSLSocket) s_sslSocketFactory.createSocket(s, vpcHost, vpc_Port, true);

						ssl.startHandshake();
					} catch (Exception e) {
						e.printStackTrace();
					}

//					response.getWriter().println("HandShake");
					os = ssl.getOutputStream();
					is = ssl.getInputStream();

//					response.getWriter().println("1122");
				} else {
					fileName = vpcHost;
				}
			} else {
				try {
//					response.getWriter().println("2");
					if (useSSL) {
//						response.getWriter().println("8");
						SSLSocketFactory factory = (SSLSocketFactory) sc.getSocketFactory();
						SSLSocket s = (SSLSocket) factory.createSocket(vpcHost, vpc_Port);
						os = s.getOutputStream();
						is = s.getInputStream();
//						response.getWriter().println("10");
					} else {
//						response.getWriter().println("4");
						Socket s = new Socket(vpcHost, vpc_Port);
						os = s.getOutputStream();
						is = s.getInputStream();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String req = "POST " + fileName + " HTTP/1.0\r\n" + "User-Agent: HTTP Client\r\n"
					+ "Content-Type: application/x-www-form-urlencoded\r\n" + "Content-Length: " + data.length()
					+ "\r\n\r\n" + data;
//			response.getWriter().println("data==" + data);
//			response.getWriter().println("req==" + req);
			os.write(req.getBytes());
			String res = new String(readAll(is));
//			response.getWriter().println("res==" + res);
			if (res.indexOf("200") < 0) {
				throw new IOException("Connection Refused - " + res);
			}

			if (res.indexOf("404 Not Found") > 0) {
				throw new IOException("File Not Found Error - " + res);
			}

			int resIndex = res.indexOf("\r\n\r\n");
			String resQS = res.substring(resIndex + 4, res.length());
			responseFields = new LinkedHashMap<String, String>();
			responseFields = createMapFromResponse(response, resQS);
		}
		catch (Exception e)
		{
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject result = new JsonObject();
			result.addProperty("error", buffer.toString());
			result.addProperty("method", "doPostRequest");
			response.getWriter().print(new Gson().toJson(result));
		}
		finally
		{
			return responseFields;
		}
	}
	
	private static Map createMapFromResponse(HttpServletResponse response, String queryString) {
		Map map = new HashMap();
		try {
			
//		response.getWriter().println("createMapFromResponse queryString: "+queryString);
			StringTokenizer st = new StringTokenizer(queryString, "&");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int i = token.indexOf('=');
				if (i > 0) {
					try {
						String key = token.substring(0, i);
						String value = URLDecoder.decode(token.substring(i + 1, token.length()));
						map.put(key, value);
					} catch (Exception ex) {
						// Do Nothing and keep looping through data
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject result = new JsonObject();
			result.addProperty("error", buffer.toString());
			result.addProperty("method", "createMapFromResponse");
			response.getWriter().print(new Gson().toJson(result));
		}
		finally{
			return map; 
		}
	}
	
	private static String null2unknown(String in, Map responseFields) {
		if (in == null || in.length() == 0 || (String)responseFields.get(in) == null) {
			return "No Value Returned";
		} else {
			return (String)responseFields.get(in);
		}
	}
	
	private static byte[] readAll(InputStream is) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];

		while (true) {
			int len = is.read(buf);
			if (len < 0) {
				break;
			}
			baos.write(buf, 0, len);
		}
		return baos.toByteArray();
	}
	
	private void getEazyPayTransactionStatus(HttpServletRequest request, HttpServletResponse response, Map configMap) throws IOException
	{
		try
		{
			String encryptionKey = "", merchantCode="", paymentRequestCall="", wsURL="", pgReqMsg="", pgRequestErrorMsg="", secretKey="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
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
				else if(key.equalsIgnoreCase("statusPgUrl"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nURL");
					wsURL = value;
				}
				else if(key.equalsIgnoreCase("secretKey"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nKey");
					secretKey = value;
				}
			}
			
			String transactionID="", transactionAmt="", transactionDate="", requestURL="", responseFromEazyPay="";
			if(null != request.getParameter("txnID"))
				transactionID = request.getParameter("txnID");
			
			requestURL = wsURL+"?ezpaytranid=&amount=&paymentmode=&merchantid="+merchantCode+"&trandate=&pgreferenceno="+transactionID;

			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			{
				response.getWriter().println("txnID: "+request.getParameter("txnID"));
				response.getWriter().println("requestURL: "+requestURL);
			}
			URL url = new URL(requestURL);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
//			URLConnection con = url.openConnection();
			con.setRequestMethod("POST");
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, null, new java.security.SecureRandom()); 
			con.setSSLSocketFactory(sc.getSocketFactory());
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String input;
			String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
			while ((input = br.readLine()) != null){
				responseFromEazyPay = input;
			}
			br.close();
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("responseFromEazyPay: "+responseFromEazyPay);
			
			if(responseFromEazyPay != null && responseFromEazyPay.trim().length() > 0)
			{
				String[] resSplitResult = responseFromEazyPay.split("\\&");
				String settlementDate="", txnStatus="", baseAmount="", aggrTxnID="", txnDateTime="", processingFee="", txnID="", taxAmount="", paymentMode="", txnAmount="", resParamName="", resParamValue="", status="";
				for(String s : resSplitResult)
				{
					resParamName = s.substring(0, s.indexOf("="));
					resParamValue = s.substring(s.indexOf("=")+1,s.length());

		        	if(resParamName.equalsIgnoreCase("sdt"))
		        	{
		        		settlementDate = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("status"))
		        	{
		        		txnStatus = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("BA"))
		        	{
		        		baseAmount = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("ezpaytranid"))
		        	{
		        		aggrTxnID = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("trandate"))
		        	{
		        		txnDateTime = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("PF"))
		        	{
		        		processingFee = resParamValue;
		        	}

		        	if(resParamName.equalsIgnoreCase("pgreferenceno"))
		        	{
		        		txnID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("TAX"))
		        	{
		        		taxAmount = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("PaymentMode"))
		        	{
		        		paymentMode = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("amount"))
		        	{
		        		txnAmount = resParamValue;
		        	}
				}
				
//				status = "Success";
				
				JsonObject result = new JsonObject();
				result.addProperty("settlementDate", settlementDate);
				result.addProperty("txnStatus", txnStatus);
				result.addProperty("baseAmount", baseAmount);
				result.addProperty("aggrTxnID", aggrTxnID);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("processingFee", processingFee);
				result.addProperty("txnID", txnID);
				result.addProperty("taxAmount", taxAmount);
				result.addProperty("paymentMode", paymentMode);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("Valid", "true");
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("No Response received from bank");
				JsonObject result = new JsonObject();
				result.addProperty("errorCode", "I");
				result.addProperty("errorMsg", "No Response received from bank");
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));		
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			
			result.addProperty("txnID", "");
			result.addProperty("txnAmount", "");
			result.addProperty("aggrTxnID", "");
			result.addProperty("txnDateTime", "");
			result.addProperty("txnStatus", "");
			result.addProperty("currency", ""); //No Parameters available from bank
			result.addProperty("remarks", "");
			result.addProperty("pymtMode", "");
			result.addProperty("brn", "");
			result.addProperty("merchantCode", "");//No Parameters available from bank
			result.addProperty("status", "Failed");
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
	
			result.addProperty("errorCode", "I");
			result.addProperty("errorMsg", buffer.toString());
			result.addProperty("errorMsg1", e.getMessage());
			result.addProperty("LocalizedMessage", e.getLocalizedMessage());
			result.addProperty("ClassName", e.getClass().getName());
			result.addProperty("Cause", e.getCause().getMessage());
			response.getWriter().print(new Gson().toJson(result));		
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public JsonObject getPaymentTransaction(HttpServletRequest request, HttpServletResponse response, String transactionID, boolean debug) throws IOException{
		JsonObject returnJson = new JsonObject();
		String destURL="", pgID="", userName="", password="", authParam="", authMethod="", paymentGetService="", paymentGetFilter="", basicAuth="", 
				sapclient="", sessionID="", loginMethod="", txnRefNo="", txnAmount=""; 
		
		String returnMessage="";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpGet pgPymntTxnGet = null;
		HttpEntity pgPymntTxnGetEntity = null;
		// CloseableHttpClient closableHttpClient = null;
//		DestinationConfiguration destConfiguration = null;
		Destination destConfiguration = null;

		
		try{
			destConfiguration = commonUtils.getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			if(debug){
				response.getWriter().println("getPaymentTransaction.transactionID:" + transactionID);
			}
			
			String loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (debug){
				response.getWriter().println("getPaymentTransaction.sapclient:" + sapclient);
				response.getWriter().println("getPaymentTransaction.authMethod:" + authMethod);
				response.getWriter().println("getPaymentTransaction.destURL:" + destURL);
			}
			
			if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
				String url = commonUtils.getDestinationURL(request, response, "URL");
				if (debug)
					response.getWriter().println("getPaymentTransaction.url1:" + url);
				sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
			} else{
				loginMethod = destConfiguration.get("LoginMethod").get().toString();
				if(null != loginMethod && loginMethod.equalsIgnoreCase("Hybrid")){
					String url = commonUtils.getDestinationURL(request, response, "URL");
					if (debug){
						response.getWriter().println("url:" + url);
						response.getWriter().println("loginMethod:" + loginMethod);
					}
					sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				}else{
					sessionID ="";
				}
			}
			if (debug)
				response.getWriter().println("sessionID1:" + sessionID);
			
			if(sapclient != null){
				paymentGetService = destURL+"/sap/opu/odata/ARTEC/PCGW/PGPayments?$filter=TrackID%20eq%20%27"+transactionID+"%27&sap-client="+sapclient;
			}
			else{
				paymentGetService = destURL+"/sap/opu/odata/ARTEC/PCGW/PGPayments?$filter=TrackID%20eq%20%27"+transactionID+"%27";
			}
			
			if (debug)
				response.getWriter().println("getPaymentTransaction.paymentGetService 1: "+paymentGetService);
			
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getPaymentTransaction.getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getPaymentTransaction.getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getPaymentTransaction.getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
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
			
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("getPaymentTransaction.proxyType: "+proxyType);
			    response.getWriter().println("getPaymentTransaction.proxyHost: "+proxyHost);
			    response.getWriter().println("getPaymentTransaction.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        pgPymntTxnGet = new HttpGet(paymentGetService);
	        // pgPymntTxnGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        pgPymntTxnGet.setHeader("content-type", "application/json");
	        pgPymntTxnGet.setHeader("Accept", "application/json");
	        pgPymntTxnGet.setHeader("x-arteria-loginid", sessionID);
	        
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	pgPymntTxnGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	pgPymntTxnGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(pgPymntTxnGet);
			HttpResponse httpResponse = client.execute(pgPymntTxnGet);
	        
	        if(debug)
	        	response.getWriter().println("getPaymentTransaction.httpResponse: "+httpResponse);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("getPaymentTransaction.statusCode: "+statusCode);
	        
	        pgPymntTxnGetEntity = httpResponse.getEntity();
	        
	        if(pgPymntTxnGetEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		       
				String retSrc = EntityUtils.toString(pgPymntTxnGetEntity);
				if(debug)
		        	response.getWriter().println("getPaymentTransaction.retSrc: "+retSrc);
				
				JsonParser parser = new JsonParser();
				JsonObject pymntTxnObj = (JsonObject)parser.parse(retSrc);
				
				if(pymntTxnObj.has("d") && pymntTxnObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0){
					returnJson.add("Payment", pymntTxnObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject());
					returnJson.addProperty("Status", "000001");
					returnJson.addProperty("ErrorCode", "");
					returnJson.addProperty("Message", "Success");
					return returnJson;
				}else if(pymntTxnObj.has("d") && pymntTxnObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() == 0){
					returnMessage = "No Records Found for the Transaction ID";
//					returnJson.add("Payment", pymntTxnObj);
					returnJson.addProperty("Status", "000002");
					returnJson.addProperty("ErrorCode", "P001");
					returnJson.addProperty("Message", returnMessage);
					return returnJson;
				}else{
					returnMessage = "Error while fetching data";
//					returnJson.add("Payment", pymntTxnObj);
					returnJson.addProperty("Status", "000002");
					returnJson.addProperty("ErrorCode", "P001");
					returnJson.add("Message", pymntTxnObj);
					return returnJson;
				}
			}else{
				returnMessage = "PGPymntConfigStatsEntity returned null when trying to connect to the backend";
				returnJson.addProperty("Status", "000002");
				returnJson.addProperty("ErrorCode", "P001");
				returnJson.addProperty("Message", returnMessage);
				return returnJson;
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			returnJson.addProperty("Status", "000002");
			returnJson.addProperty("ErrorCode", "E001");
			returnJson.addProperty("Message", "Exception: "+e.getClass()+" - "+e.getCause()+". Message: "+e.getMessage());
			returnJson.addProperty("FullTrace", buffer.toString());
			return returnJson;
		}
	}

}

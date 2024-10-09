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

import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;

/**
 * Servlet implementation class ReqTopUp
 */
@WebServlet("/ReqTopUp")
public class ReqTopUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
//	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	//private static final String PUGW_DEST_NAME =  "pugw_utils_op";
//	private static final String SF_CUSTOMER_DEST_NAME =  "sfmst";
//	private static final String PCGW_DEST_NAME =  "pcgw";
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReqTopUp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		JsonObject applicationLog=new JsonObject();
		String aggrID="",txnId="",OdataUrl="",username="",password="",userpass="",executeUrl="";
		boolean debug = false;
		try
		{
			//Below for validating Customer
			String customerNo="", applicationName="", loginSessionID="", pgID = "";
			boolean isValidCustomer = false;
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
			
			if(null != request.getParameter("AppName"))
				applicationName = request.getParameter("AppName");
			else
				applicationName = "OS";
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
			loginSessionID = request.getParameter("sessionID");
			isValidCustomer = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
			aggrID=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			OdataUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userpass=username+":"+password;
			if(isValidCustomer)
			{
				String configurationValues = getConstantValues(request, response, "", pgID);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+configurationValues);
				
				if(null == configurationValues || configurationValues.toString().trim().length() == 0)
				{
					throw new NullPointerException("Configuration Values Not Found");
				}
//				response.getWriter().println("configurationValues: "+configurationValues);
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				if(null != request.getParameter("debug"))
				{
				if(request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("\n pgID:"+ pgID);
				if(request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("ICICIPGID:"+ properties.getProperty("ICICIPGID"));
				}
				if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
				{
					if(request.getParameter("txn-id")!=null){
						txnId=	request.getParameter("txn-id");
					}
					String appLogID = commonUtils.generateGUID(36);
					String ipAddress = commonUtils.getIPAddress(request);
					if(ipAddress.length()>100){
						ipAddress=ipAddress.substring(0, 100);
					}
					
					String createdBy = commonUtils.getUserPrincipal(request, "name", response);
					String createdAt =commonUtils. getCreatedAtTime();
					long createdOnInMillis = commonUtils.getCreatedOnDate();
					applicationLog.addProperty("ID",appLogID);
					applicationLog.addProperty("AggregatorID",aggrID);
					applicationLog.addProperty("LogObject","Java");
					applicationLog.addProperty("LogSubObject","Pay2CorpTopUp");
					applicationLog.addProperty("LogDate","/Date("+createdOnInMillis+")/");
					applicationLog.addProperty("LogTime",createdAt);
					applicationLog.addProperty("Program","ReqTopUp");
					applicationLog.addProperty("Process",request.getServletPath());
					applicationLog.addProperty("LogUser",ipAddress);
					applicationLog.addProperty("ProcessID",pgID);
					//applicationLog.addProperty("ProcessRef1",txnId);
					applicationLog.addProperty("CreatedBy",createdBy);
					applicationLog.addProperty("CreatedOn","/Date("+createdOnInMillis+")/");
					applicationLog.addProperty("CreatedAt",createdAt);
					if (request.getParameter("txn-id") != null) {
						applicationLog.addProperty("SourceReferenceID", request.getParameter("txn-id"));
					}
					executeUrl=OdataUrl+"ApplicationLogs";
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
						response.getWriter().println("appLogObj:"+applicationLog);
						response.getWriter().println("executeUrl:"+executeUrl);
					}
					JsonObject applogOb = commonUtils.executePostURL(executeUrl, userpass, response, applicationLog, request, false, "PCGWHANA");
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
						response.getWriter().println("applogOb:"+applogOb);
					}
					//String aggrId,String appLogId,JsonArray appLogMsgArry
					JsonArray appLogArr=new JsonArray();
					initiateEscrowTopupRequest(request, response, configurationValues,aggrID,appLogID,appLogArr);
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", "Invalid Payment Gateway Request.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", customerNo+" is not authorized to make a payment.");
				result.addProperty("walletClientCode", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().print("initiateTopupRequest Error: "+e.getMessage());
		}
	}

	private void initiateEscrowTopupRequest(HttpServletRequest request, HttpServletResponse response, String configurationValues,String aggrId,String appLogId,JsonArray appLogMsgArry) throws IOException
	{
		CommonUtils commonUtils=new CommonUtils();
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", clientCode = "", WSURL="", topupRequestCall="", pgReqMsg="", pgRequestErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if(null != request.getParameter("debug"))
			{
			if(request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("\n configurationValues:"+ configurationValues);
			}
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			if(null != request.getParameter("debug"))
			{
			if(request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("\n splitResult:"+ splitResult);
			}
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	if(null != request.getParameter("debug"))
				{
	        	if(request.getParameter("debug").equalsIgnoreCase("true"))
	        	{
		        	response.getWriter().print("\n paramName:"+ paramName);
		        	response.getWriter().print("paramValue:"+ paramValue);
	        	}
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
//	        		response.getWriter().print("WSURL:" + WSURL);
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
			
			topupRequestCall = properties.getProperty("topupRequestCall");
			pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
			WalletParamMap topupReqMap = new WalletParamMap();
			
			if(null != request.getParameter("txn-id"))
				topupReqMap.put("txn-id", request.getParameter("txn-id"));
			if(null != request.getParameter("txn-session-id"))
				topupReqMap.put("txn-session-id", request.getParameter("txn-session-id"));
			if(null != request.getParameter("txn-datetime"))
				topupReqMap.put("txn-datetime", request.getParameter("txn-datetime"));
			if(null != request.getParameter("txn-amount"))
				topupReqMap.put("txn-amount", request.getParameter("txn-amount"));
			if(null != request.getParameter("wallet-user-code"))
				topupReqMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			topupReqMap.put("txn-for", topupRequestCall);
			if(null != request.getParameter("return-url"))
				topupReqMap.put("return-url", request.getParameter("return-url"));
			if(null != request.getParameter("cancel-url"))
				topupReqMap.put("cancel-url", request.getParameter("cancel-url"));
			/*Additional Parameter1 is used to identify from which application topup request is triggered. 
			 * This parameter will return the same in the response based on which app will decide on the redirect url.*/
			/* Below condition commented for carrying out alternate solution. i.e., redirect URL itself will be read from the UI5 application
			 * if(null != request.getParameter("AppName"))
				topupReqMap.put("additional-param1", request.getParameter("AppName"));
			else
				topupReqMap.put("additional-param1", "OS");*/
			
			/*
			 * Additional parameters 1 to 4 are used for getting redirect url params based on which we'll form redirect url in the response servlet
			 * site-id : eg: SSLaunchpad
			 * application-id : eg: ssoutstanding1pg
			 * action-name: eg: Display
			 * nav-param: eg: Search
			 * Please note that these parameter values should not have length more than 25 characters
			 */
			
			if(null != request.getParameter("site-id"))
				topupReqMap.put("additional-param1", request.getParameter("site-id"));
			else
				topupReqMap.put("additional-param1", "NA");
			
			if(null != request.getParameter("application-id"))
				topupReqMap.put("additional-param2", request.getParameter("application-id"));
			else
				topupReqMap.put("additional-param2", "NA");
			
			if(null != request.getParameter("action-name"))
				topupReqMap.put("additional-param3", request.getParameter("action-name"));
			else
				topupReqMap.put("additional-param3", "NA");
			
			if(null != request.getParameter("nav-param"))
				topupReqMap.put("additional-param4", request.getParameter("nav-param"));
			else
				topupReqMap.put("additional-param4", "NA");
			
			if(null != request.getParameter("additional-param5"))
				topupReqMap.put("additional-param5", request.getParameter("additional-param5"));
			else
				topupReqMap.put("additional-param5", "NA");
			if(null != request.getParameter("additional-param6"))
				topupReqMap.put("additional-param6", request.getParameter("additional-param6"));
			else
				topupReqMap.put("additional-param6", "NA");
			if(null != request.getParameter("additional-param7"))
				topupReqMap.put("additional-param7", request.getParameter("additional-param7"));
			else
				topupReqMap.put("additional-param7", "NA");
			if(null != request.getParameter("additional-param8"))
				topupReqMap.put("additional-param8", request.getParameter("additional-param8"));
			else
				topupReqMap.put("additional-param8", "NA");
			if(null != request.getParameter("additional-param9"))
				topupReqMap.put("additional-param9", request.getParameter("additional-param9"));
			else
				topupReqMap.put("additional-param9", "NA");
			if(null != request.getParameter("pay-for"))
				topupReqMap.put("additional-param10", request.getParameter("pay-for"));
			else
				topupReqMap.put("additional-param10", "NA");
			
			JsonObject  reqObj=new JsonObject();
			
			topupReqMap.forEach((key,value)->{
				reqObj.addProperty(key, value);
			});
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), ""+request.getParameter("txn-id"), reqObj+"", appLogId,appLogMsgArry);
			
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("TopupReqInputMap: "+topupReqMap.toString());

			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setRequestMap(topupReqMap);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			pgReqMsg = walletMessageBean.generateWalletRequestMessage();
			
			if(pgReqMsg != null && pgReqMsg.trim().length() > 0)
			{
				JsonObject result = new JsonObject();
//				result.addProperty("pgReqMsg", pgReqMsg);
//				result.addProperty("walletClientCode", merchantCode);
//				result.addProperty("Valid", "true");
				
				//Correction code.................
				result.addProperty("walletRequestMessage", pgReqMsg);
//				result.addProperty("pgReqMsg", pgReqMsg);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "true");
//				result.addProperty("WSURL","https://demo.b2biz.co.in/ws/topup");
				result.addProperty("WSURL", WSURL);
				result.addProperty("parameters","walletClientCode|walletRequestMessage");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), ""+request.getParameter("txn-id"), result+"", appLogId,appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", aggrId);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgRequestErrorMsg);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), ""+request.getParameter("txn-id"), result+"", appLogId,appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", aggrId);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			}
			
		}
		catch (Exception e) {
			StringBuffer buffer = new StringBuffer(e.getClass().getName() + "-->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage());
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			JsonObject result = new JsonObject();
			result.addProperty("ExceptionMsg", buffer.toString());
			commonUtils.crtAppLogMsgsObj(request, response, request.getServletPath(),""+request.getParameter("txn-id"), result + "", appLogId, appLogMsgArry);
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", aggrId);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			response.getWriter().print("initiateEscrowTopupRequest Error: " + e.getMessage());
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
				if(null != request.getParameter("debug"))
				{
				if(request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("PGID: "+PGID);
				}
				if(PGID.equalsIgnoreCase("B2BIZ"))
				{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(configValuesEntity);
				if(null != request.getParameter("debug"))
				{
//				if(request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: "+retSrc);
				}
//						response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList aWSURLList = document.getElementsByTagName("d:TopUpURL");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            
	            if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("d:TopUpURL:");
	            
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            
	            for(int i=0 ; i<pgIDList.getLength() ; i++)
	            {
	            	if(null != request.getParameter("debug"))
					{
		            	if(request.getParameter("debug").equalsIgnoreCase("true"))
		            		response.getWriter().println("PGID: "+pgIDList.item(i).getTextContent());
		            	if(request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().println("aWSURLList: "+aWSURLList.item(i).getTextContent());
		            	if(request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().println("merchantCodeList: "+merchantCodeList.item(i).getTextContent());
					}
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(PGID.equalsIgnoreCase(pgIDList.item(i).getTextContent()))
        			{
//			            		configurableValues = merchantCodeList.item(i).getTextContent();
	            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"+"PGID="+pgIDList.item(i).getTextContent()
	            				+"|"+"WSURL="+aWSURLList.item(i).getTextContent()
	            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
	            		break;
        			}
	            }
//					
				if(null != request.getParameter("debug"))
				{
					if(request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("1 configurableValues: "+configurableValues);
					}
//					response.getOutputStream().print(Integer.parseInt(EntityUtils.toString(constantEntity)));	
//					EntityUtils.consume(constantEntity);
//					response.getOutputStream().print
				}
			}
		}
		catch (Exception e)
		{
			response.getWriter().println("GetConstantValues Error"+e.getMessage());
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
				destinationName = DestinationUtils.PCGW_UTILS_OP;
			}
				
		String tempSapClient = null;
		String propertyValue="";
		try {
				// get all destination properties
// 				Context ctxDestFact = new InitialContext();
// 				ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
// //				response.getWriter().println("ConnectivityConfiguration: "+ configuration);
// 				DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGW_UTILS_OP);
//				response.getWriter().println("DestinationConfiguration: "+ destConfiguration);
				// Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
				
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				Destination destConfiguration = destinationAccessor.get();

				if(property.equalsIgnoreCase("sapclient")){
					//Reading SAP-Client from All Configuration.........................................................
					propertyValue = destConfiguration.get("sap-client").get().toString();
				} else if(property.equalsIgnoreCase("service")){
					propertyValue = destConfiguration.get("servicename").get().toString();
				}
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
			}
			catch (Exception e) {
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
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

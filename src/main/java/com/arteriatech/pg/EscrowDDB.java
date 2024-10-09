package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
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

public class EscrowDDB extends HttpServlet{

	/**
	 * 
	 */	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean debug = false;
		try
		{/*
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			String customerNo="", pgID = "", pgCatID = "";
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;
			}
				if(null != request.getParameter("PGCategoryID"))
					pgCatID = request.getParameter("PGCategoryID");
				if(null != request.getParameter("PGID"))
					pgID = request.getParameter("PGID");
				if(null != request.getParameter("CustomerNo"))
					customerNo = request.getParameter("CustomerNo");
				
				String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", clientCode = "";

				String configurationValues = getConstantValues(request, response, pgCatID, pgID);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+configurationValues);
				
				if(null == configurationValues || configurationValues.toString().trim().length() == 0)
				{
//					response.getWriter().print("Config Error: Configuration Values Not Found");
//					throw new NullPointerException("Configuration Values Not Found");
				}
				
//				Properties properties = new Properties();
//				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				if(null != request.getParameter("debug"))
				{
					if(request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\n pgID:"+ pgID);
					if(request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("ICICIPGID:"+ properties.getProperty("ICICIPGID"));
				}
				
				
				//Encryption keys...
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("\n configurationValues:"+ configurationValues);
			
				String wholeParamString="", paramName="", paramValue="";
				wholeParamString = configurationValues;
				String[] splitResult = wholeParamString.split("\\|");
				if(debug){
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
				
				if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
				{
					initiatePullMoneyRequestRequest(request, response, configurationValues);
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", "Invalid Request.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			
		*/}
		catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", "Error");
			result.addProperty("errorMsg", e.getMessage());
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
//			response.getWriter().print("initiateTopupRequest Error: "+e.getMessage());
		}
	
	}
	
	@SuppressWarnings("finally")
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String PGCatID, String PGID) throws IOException, URISyntaxException
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
				basicAuth = "Basic " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000001";
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
				response.getWriter().println("configurableValues"+configurableValues);
			return configurableValues;
		}catch (Exception e)
		{
			if(debug)
				response.getWriter().println("Exception in getConfigValues: "+e.getLocalizedMessage());
			return configurableValues;
		}
		/* finally
		{
			closableHttpClient.close();
//			return configurableValues;
		} */
	}
	
	
	private void initiatePullMoneyRequestRequest(HttpServletRequest request, HttpServletResponse response, String ConfigValues) throws IOException, URISyntaxException
	{
		String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", EscrowUserDetailCall = "", wholeParamString = "",
				paramName = "", paramValue = "", clientCode = "", WSURL = "", merchantCode = "";
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		EscrowUserDetailCall = properties.getProperty("EscrowPullMoneyCall");
		
		wholeParamString = ConfigValues;
		String[] splitResult = wholeParamString.split("\\|");
		
		//For debugging java application
		if(null != request.getParameter("debug") && request.getParameter("Debug") == "true")
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
		
		if(null != clientCode){
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
		}else{
			walletPublicKey = properties.getProperty("WalletPublicKey");
			merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
			merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
		}
		
		try{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("try");
			
			//Request Part
			WalletParamMap map = new WalletParamMap();
			map.put("txn-id", request.getParameter("txnId"));
			map.put("wallet-user-code", request.getParameter("CustomerNo"));
			map.put("txn-datetime", request.getParameter("txndatetime"));
			map.put("txn-amount", request.getParameter("txnAmount"));
			map.put("txn-session-id", "NA");
//			map.put("txn-for", "paymentAPI");
			String txnFor = "paymentAPI";
			if(request.getParameter("txnFor") != null)
				txnFor = request.getParameter("txnFor");
			
			map.put("txn-for", "Auto pull");
			if(request.getParameter("additional-param1") != null)
				map.put("additional-param1", request.getParameter("additionalParam1"));
			else
				map.put("additional-param1", "NA");
			
			if(request.getParameter("additional-param2") != null)
				map.put("additional-param2", request.getParameter("additionalParam2"));
			else
				map.put("additional-param2", "NA");
			
			if(request.getParameter("additional-param3") != null)
				map.put("additional-param3", request.getParameter("additionalParam3"));
			else
				map.put("additional-param3", "NA");
			
			if(request.getParameter("additional-param4") != null)
				map.put("additional-param4", request.getParameter("additionalParam4"));
			else
				map.put("additional-param4", "NA");
			
			if(request.getParameter("additional-param5") != null)
				map.put("additional-param5", request.getParameter("additionalParam5"));
			else
				map.put("additional-param5", "NA");
			
			if(request.getParameter("additional-param6") != null)
				map.put("additional-param6", request.getParameter("additionalParam6"));
			else
				map.put("additional-param6", "NA");
			
			if(request.getParameter("additional-param7") != null)
				map.put("additional-param7", request.getParameter("additionalParam7"));
			else
				map.put("additional-param7", "NA");
			
			if(request.getParameter("additional-param8") != null)
				map.put("additional-param8", request.getParameter("additionalParam8"));
			else
				map.put("additional-param8", "NA");
			
			if(request.getParameter("additional-param9") != null)
				map.put("additional-param9", request.getParameter("additionalParam9"));
			else
				map.put("additional-param9", "NA");
			
			if(request.getParameter("additional-param10") != null)
				map.put("additional-param10", request.getParameter("additionalParam10"));
			else
				map.put("additional-param10", "NA");
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("inputParamMap:" + map);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			response.getWriter().print("merchantCode:" + merchantCode);

			WalletAPI walletAPI = new WalletAPI();
			WalletParamMap responseMap = walletAPI.callWalletAPI(WSURL,"paymentAPI",
					merchantCode, map, walletPublicKey, merchantPrivateKey);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("responseMap" + responseMap);
			
			String errorStatus="", errorMsg="";
			if((null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0)){
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("response found");
				
				JsonObject result = new JsonObject();
				result.addProperty("txnId", responseMap.get("txn-id"));
				result.addProperty("CustomerNo", responseMap.get("wallet-user-code"));
				result.addProperty("txndatetime", responseMap.get("txn-datetime"));
				result.addProperty("txnAmount", request.getParameter("txnAmount"));
				result.addProperty("txnFor", responseMap.get("txn-for"));
				result.addProperty("walletTxnId", responseMap.get("wallet-txn-id"));
				result.addProperty("walletTxndatetime", responseMap.get("wallet-txn-datetime"));
				result.addProperty("walletTxnStatus", responseMap.get("wallet-txn-status"));
				result.addProperty("additionalParam1", responseMap.get("additional-param1"));
				result.addProperty("additionalParam2", responseMap.get("additional-param2"));
				result.addProperty("additionalParam3", responseMap.get("additional-param3"));
				result.addProperty("additionalParam4", responseMap.get("additional-param4"));
				result.addProperty("additionalParam5", responseMap.get("additional-param5"));
				result.addProperty("additionalParam6", responseMap.get("additional-param6"));
				result.addProperty("additionalParam7", responseMap.get("additional-param7"));
				result.addProperty("additionalParam8", responseMap.get("additional-param8"));
				result.addProperty("additionalParam9", responseMap.get("additional-param9"));
				result.addProperty("additionalParam10", responseMap.get("additional-param10"));
				result.addProperty("errorStatus", "Success");
				result.addProperty("errorMsg", "");
				result.addProperty("Valid", "true");
				response.getWriter().print(new Gson().toJson(result));
			}else{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("response not found");
				
				JsonObject result = new JsonObject();
				result.addProperty("txnId", "");
				result.addProperty("CustomerNo", "");
				result.addProperty("txndatetime", "");
				result.addProperty("txnAmount", request.getParameter("txnAmount"));
				result.addProperty("txnFor", "");
				result.addProperty("walletTxnId", "");
				result.addProperty("walletTxndatetime", "");
				result.addProperty("walletTxnStatus", "");
				result.addProperty("additionalParam1", "");
				result.addProperty("additionalParam2", "");
				result.addProperty("additionalParam3", "");
				result.addProperty("additionalParam4", "");
				result.addProperty("additionalParam5", "");
				result.addProperty("additionalParam6", "");
				result.addProperty("additionalParam7", "");
				result.addProperty("additionalParam8", "");
				result.addProperty("additionalParam9", "");
				result.addProperty("additionalParam10", "");
				errorStatus = "Error";
				errorMsg = responseMap.get("error_message");
				result.addProperty("errorStatus", errorStatus);
				result.addProperty("errorMsg", errorMsg);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("txnId", "");
			result.addProperty("CustomerNo", "");
			result.addProperty("txndatetime", "");
			result.addProperty("txnAmount", request.getParameter("txnAmount"));
			result.addProperty("txnFor", "");
			result.addProperty("walletTxnId", "");
			result.addProperty("walletTxndatetime", "");
			result.addProperty("walletTxnStatus", "");
			result.addProperty("additionalParam1", "");
			result.addProperty("additionalParam2", "");
			result.addProperty("additionalParam3", "");
			result.addProperty("additionalParam4", "");
			result.addProperty("additionalParam5", "");
			result.addProperty("additionalParam6", "");
			result.addProperty("additionalParam7", "");
			result.addProperty("additionalParam8", "");
			result.addProperty("additionalParam9", "");
			result.addProperty("additionalParam10", "");
			result.addProperty("errorStatus", "Error");
			result.addProperty("errorMsg", e.getMessage());
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	

}

package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
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
import com.wallet247.clientutil.api.WalletAPI;
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
 * Servlet implementation class EscrowUserAccounts
 */
@WebServlet("/EscrowUserAccounts")
public class EscrowUserAccounts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EscrowUserAccounts() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		String customerNo ="",sessionID="",errorCode="", url="", loginID="", errorMsg=""; 
		
		boolean debug = false;
		
		JsonObject escrowUserResponse = new JsonObject();
		
		JsonObject errorJsonResponse = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String servletPath="";
		
		try {
			servletPath=request.getServletPath();
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			if (null != request.getParameter("CustomerNo")) 
				customerNo = request.getParameter("CustomerNo");
			if(debug){
				response.getWriter().println("doGet.customerNo: "+ customerNo);
				response.getWriter().println("doGet.ServletPath: "+ servletPath);
			}	
			
			loginID =  commonUtils.getUserPrincipal(request, "name", response);
			
			if(debug)
				response.getWriter().println("doGet.loginID: "+loginID);

			if (loginID == null) {
				errorCode = "E125";
			}
			else
			{
/*				url = commonUtils.getDestinationURL(request, response, "URL");
				if (debug)
					response.getWriter().println("doGet.url:" + url);
				
				sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				if (debug)
					response.getWriter().println("Generating sessionID:" + sessionID);*/
				
				if(servletPath.equalsIgnoreCase("/CorpEscrowAccountBalances")){
					escrowUserResponse = getEscrowUsers(request, response,servletPath,customerNo, debug);
					if(debug)
						response.getWriter().println("doGet.escrowUserResponse: "+escrowUserResponse);
					
					response.getWriter().println(escrowUserResponse);
				} else {
					String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
					if (debug)
						response.getWriter().println("authMethod:" + authMethod);
					if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
						url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("url:" + url);
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						if (debug)
							response.getWriter().println("Generating sessionID:" + sessionID);
						if (sessionID.contains(" ")) {
							errorCode = "S001";
							errorMsg = sessionID;
							if (debug)
								response.getWriter().println(
										"Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
						}
					} else {
						sessionID = "";
					}

					errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);
					if (debug)
						response.getWriter().println("doGet.validateCustomer:" + errorCode);

					if (errorCode.trim().length() > 0) {
						errorJsonResponse.addProperty("errorStatus", errorCode);
						if (errorCode.equalsIgnoreCase("S001"))
							errorJsonResponse.addProperty("errorMessage", errorMsg);
						else
							errorJsonResponse.addProperty("errorMessage", properties.getProperty(errorCode));
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(errorJsonResponse);
					} else {
						// TODO Calling getEscrowUsers()
						escrowUserResponse = getEscrowUsers(request, response,servletPath,customerNo, debug);
						if (debug)
							response.getWriter().println("doGet.escrowUserResponse: " + escrowUserResponse);

						response.getWriter().println(escrowUserResponse);
					}
				}		
	}	
			
		} catch (Exception e) {
			// TODO: handle exception
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			errorJsonResponse.addProperty("errorStatus", "001");
			errorJsonResponse.addProperty("errorMessage", e.getLocalizedMessage());
			response.getWriter().println(errorJsonResponse);
		}
	}
	
	private JsonObject getEscrowUsers(HttpServletRequest request, HttpServletResponse response,String servletPath,String inputCustomerNo,boolean debug) throws IOException, URISyntaxException
	{
		
		String customerNo = "", userRegID="", sSessionID="",regisFor="";
		
		Properties properties = new Properties();
		
		List<Map<String, String>> configDataList = new ArrayList<>();
		Map< String, String> configDataMap = null ;
		
		JsonObject bankAPIObjResponse = null;
		JsonObject userRegisJsonResponse = new JsonObject();
		JsonArray bankAPIArrayResponse = new JsonArray();
		JsonObject bankAPIObjResultsResponse = new JsonObject();
		JsonObject bankAPIResponseUI = new JsonObject();
		
		try
		{
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			configDataList = getConstantValues(request, response, "", "");
			if (configDataList.size() > 0) {

				if (debug) {
					for(int i=0; i < configDataList.size() ; i++){
						response.getWriter().println("doGet.MerchantCode ("+i+"): "+configDataList.get(i).get("MerchantCode"));
						response.getWriter().println("doGet.BankKey ("+i+"): "+configDataList.get(i).get("BankKey"));
						response.getWriter().println("doGet.ClientCode ("+i+"): "+configDataList.get(i).get("ClientCode"));
						response.getWriter().println("doGet.PGOwnPublickey ("+i+"): "+configDataList.get(i).get("PGOwnPublickey"));
						response.getWriter().println("doGet.WSURL ("+i+"): "+configDataList.get(i).get("WSURL"));
						response.getWriter().println("doGet.PGID ("+i+"): "+configDataList.get(i).get("PGID"));
					}
				}
				String appendUrl = "";
				for(int i=0 ; i<configDataList.size() ; i++){
					if(i != configDataList.size()-1)
						appendUrl = appendUrl+"RegistrationFor%20eq%20%27"+configDataList.get(i).get("PGID")+"%27%20or%20";
					else
						appendUrl = appendUrl+"RegistrationFor%20eq%20%27"+configDataList.get(i).get("PGID")+"%27";
				}
				if(debug)
					response.getWriter().println("appendUrl: " + appendUrl);	
				
				// calling getUserRegisByPGId() for userRegiID
				JsonArray userRigsArray = new JsonArray();
				userRegisJsonResponse = getUserRegisDetails(request, response, appendUrl,servletPath,inputCustomerNo,debug);
				userRigsArray = userRegisJsonResponse.getAsJsonObject("d").getAsJsonArray("results");
				
				if (userRigsArray.size() == 0) {
					
					for (int i = 0; i < configDataList.size(); i++) {
						
						JsonObject result = new JsonObject();
						result.addProperty("errorStatus", "001");
						result.addProperty("errorMessage", "Invalid Usercode");
						result.addProperty("EPID", configDataList.get(i).get("PGID"));
						bankAPIArrayResponse.add(result);
					}
					bankAPIObjResultsResponse.add("results", bankAPIArrayResponse);
					bankAPIResponseUI.add("EscrowAccountBalances", bankAPIObjResultsResponse);
					return bankAPIResponseUI;
				} else
				{
					
					for(int i=0; i < configDataList.size() ; i++)
					{
						for (int j = 0; j < userRigsArray.size(); j++) {
							
							bankAPIObjResponse = new JsonObject();
							configDataMap =  new HashMap<String, String>();
							
							if (! userRigsArray.get(j).getAsJsonObject().get("RegistrationFor").isJsonNull())
								regisFor = userRigsArray.get(j).getAsJsonObject().get("RegistrationFor").getAsString();
							if (debug)
								response.getWriter().println("doGet.regisFor: "+ regisFor);
							
							if (configDataList.get(i).get("PGID").equalsIgnoreCase(regisFor)) {
								
								if (configDataList.get(i).get("PGID").equalsIgnoreCase("B2BIZ")) {
										userRegID  =  userRigsArray.get(j).getAsJsonObject().get("UserRegId").getAsString();
										configDataMap.put("UserRegId", userRegID);
										configDataMap.put("WSURL", configDataList.get(i).get("WSURL"));
										configDataMap.put("ClientCode", configDataList.get(i).get("ClientCode"));
										configDataMap.put("MerchantCode", configDataList.get(i).get("MerchantCode"));
										configDataMap.put("EPID", configDataList.get(i).get("PGID"));

										bankAPIObjResponse = getEscrowUserDetails(request, response, sSessionID, configDataMap, customerNo, debug);
										if (debug)
											response.getWriter().println("doGet.bankAPIObjResponse(" + i + "): " + bankAPIObjResponse);

										if (bankAPIObjResponse.get("Valid").getAsString().equalsIgnoreCase("true")) {
											bankAPIObjResponse.addProperty("EPID", regisFor);
											bankAPIArrayResponse.add(bankAPIObjResponse);
										} else {
											JsonObject result = new JsonObject();

											result.addProperty("errorStatus", bankAPIObjResponse.get("errorStatus").getAsString());
											result.addProperty("errorMessage", bankAPIObjResponse.get("errorMsg").getAsString());
											result.addProperty("EPID", configDataList.get(i).get("PGID"));
											bankAPIArrayResponse.add(result);
											// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											// response.getWriter().println(new Gson().toJson(result));
										}
									
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("errorStatus", "001");
									result.addProperty("errorMessage", "Invalid EPID");
									result.addProperty("EPID", configDataList.get(i).get("PGID"));
									bankAPIArrayResponse.add(result);
								}
							} 
						}
					}
					bankAPIObjResultsResponse.add("results", bankAPIArrayResponse);
					bankAPIResponseUI.add("EscrowAccountBalances", bankAPIObjResultsResponse);
					return  bankAPIResponseUI;
				}
			}else
			{
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", "E195");
				result.addProperty("errorMessage", "Configurations not maintained for the requested EPID");
				result.addProperty("Valid", "false");
				return  result;
			}
		}catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", "001");
			result.addProperty("errorMessage", properties.getProperty("001"));
//			response.getWriter().println("Error: No Login ID found");
			return result;
		}
	}
	
	//TODO getConstants()
	private List<Map<String, String>> getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
		Map<String, String> configMap = null;
		List<Map<String, String>> configData = new ArrayList<>();
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
			HttpDestination destConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(destConfiguration);
			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", DestinationUtils.PCGW_UTILS_OP));
				 
				 return configData;
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				/* encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				String encodedStr = new String(encodedByte);
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());; */
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000001";
			constantValuesFilter = constantValuesFilter+"PGCategoryID eq '"+pgCatID+"'";
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
		    } */
		    
		    /* HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        configValuesGet = new HttpGet(constantValuesService);
	        //configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	configValuesGet.setHeader("Authorization", "Bearer "+ authParam);
	        }else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
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
				if(debug){
					response.getWriter().println("Configvalues.retSrc: "+retSrc);
				}
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
	            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
	            NodeList pgIDList = document.getElementsByTagName("d:PGID");
	            NodeList bankKeyList = document.getElementsByTagName("d:BankKey");
	            NodeList pgOwnPublickeyList = document.getElementsByTagName("d:BankKey");
	            NodeList wsUrlList = document.getElementsByTagName("d:UserRegURL");
	            
	            if(debug){
	            	response.getWriter().println("pgIDList.getLength(): "+pgIDList.getLength());
	            	response.getWriter().println("pgCatID: "+pgCatID);
	            }
	            if(debug)
	            	response.getWriter().println("getConstantValues.if");
	            	 
	            for(int i=0 ; i<pgIDList.getLength() ; i++)
	            {
	            	configMap = new HashMap<String, String>();
        			configMap.put("MerchantCode", merchantCodeList.item(i).getTextContent());
        			configMap.put("BankKey", bankKeyList.item(i).getTextContent().toString());
        			configMap.put("ClientCode", clientCodeList.item(i).getTextContent());
        			configMap.put("PGOwnPublickey", pgOwnPublickeyList.item(i).getTextContent());
        			configMap.put("WSURL", wsUrlList.item(i).getTextContent());
        			configMap.put("PGID", pgIDList.item(i).getTextContent());
        			configData.add(i, configMap);
	            }
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("UserDetail.Exception:"+buffer.toString());
			}
		}
		finally
		{
			//closableHttpClient.close();
			return configData;
		}
	}
		
	private JsonObject getEscrowUserDetails(HttpServletRequest request, HttpServletResponse response, String loginSessionID, Map<String, String> ConfigValues, String CustomerNo, boolean debug) throws IOException, URISyntaxException
	{
		String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", EscrowUserDetailCall = "", userRegId = "", epID="",balanceEnquiryCall="",
				clientCode = "", WSURL = "", merchantCode = "";
		Properties properties = new Properties();
		
		try
		{
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			balanceEnquiryCall = properties.getProperty("balanceEnquiryCall");
			
			
	        if (null != ConfigValues.get("ClientCode"))
	    		clientCode = (String)ConfigValues.get("ClientCode");
	        
	        if (null != ConfigValues.get("WSURL"))
	        	WSURL = (String)ConfigValues.get("WSURL");
	        
	        if (null != ConfigValues.get("MerchantCode"))
	        	merchantCode = (String)ConfigValues.get("MerchantCode");
	        
	        if (null != ConfigValues.get("UserRegId"))
	        	userRegId = (String)ConfigValues.get("UserRegId");
	        
	        if (null != ConfigValues.get("PGID"))
	        	epID = (String)ConfigValues.get("EPID");
	    
	        // using for-each loop for iteration over Map.entrySet() 
	        if (debug) {
	        	for (String key : ConfigValues.keySet())
	        		response.getWriter().println("getEscrowUserDetails: " +key+" : "+  ConfigValues.get(key));
	        }
			
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("PRD Keys found ");
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
		
			if(debug)
				response.getWriter().print("try ");
			
			WalletParamMap inputParamMap = new WalletParamMap();
			inputParamMap.put("wallet-user-code", userRegId);
			if(debug)
				response.getWriter().println("getEscrowUsers.inputParamMap: " + inputParamMap);
		
			WalletAPI getResponse = new WalletAPI();
			WalletParamMap responseMap = getResponse.callWalletAPI(WSURL, balanceEnquiryCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
			if(debug)
				response.getWriter().println("getEscrowUsers.responseMap: " + responseMap);
			
			String errorStatus="", errorMsg="";
			if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
			{
				if(debug)
					response.getWriter().println("response found ");
				
				//Success Responses
				JsonObject result = new JsonObject();
				result.addProperty("walletusercode", responseMap.get("wallet-user-code"));
				result.addProperty("userbalance", responseMap.get("user-balance"));
				result.addProperty("checksum", responseMap.get("checksum"));
				result.addProperty("currency", "INR");
				result.addProperty("Valid", "true");
				return result;
			}
			else
			{
				if(debug)
					response.getWriter().println("response not found ");
				
				errorStatus = responseMap.get("error_status");
				errorMsg = responseMap.get("error_message");
				JsonObject result = new JsonObject();
				result.addProperty("errorStatus", "E195");
				result.addProperty("errorMessage", errorMsg);
				result.addProperty("Valid", "false");
				return result;
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("errorStatus", "001");
			result.addProperty("errorMsg", e.getLocalizedMessage());
			result.addProperty("Valid", "false");
//			response.getWriter().println("Error: No Login ID found");
			return result;
		}
	}
	
	public JsonObject getUserRegisDetails(HttpServletRequest request, HttpServletResponse response, String appendUrl,String servletPath,String customerNo, boolean debug) throws IOException
	{
		String aggregatorID="", loginID ="",oDataUrl="", executeURL ="",password="",userPass="", userName="";
		JsonObject userRegisResponse = new JsonObject();
		
		CommonUtils commonUtils = new CommonUtils();
		try {
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password",DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			if (debug) {
				response.getWriter().println("getEscrowUsers.loginID: "+loginID);
				response.getWriter().println("getEscrowUsers.oDataUrl: "+oDataUrl);
				response.getWriter().println("getEscrowUsers.userName: "+userName);
			}
			if(servletPath.equalsIgnoreCase("/CorpEscrowAccountBalances")){
				executeURL = oDataUrl + "UserRegistrations?$filter=UserRegId%20eq%20%27"+customerNo+"%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20(" + appendUrl + ")";
			} else {
				executeURL = oDataUrl + "UserRegistrations?$filter=LoginId%20eq%20%27" + loginID + "%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20(" + appendUrl + ")";
			}
			//			executeURL = oDataUrl+"UserRegistrations"+"?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20RegistrationFor%20eq%20%27"+pgID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
			if (debug)
				response.getWriter().println("getEscrowUsers.executeURL: "+executeURL);
			
			userRegisResponse = commonUtils.executeURL(executeURL, userPass, response);
			if (debug)
				response.getWriter().println("getEscrowUsers.userRegisResponse: "+userRegisResponse);
			
		} catch (Exception e) {
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			}
			response.getWriter().print(e.getMessage());
		}
		return userRegisResponse;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

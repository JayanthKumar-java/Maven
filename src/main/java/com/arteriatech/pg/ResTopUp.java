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
import javax.servlet.RequestDispatcher;
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
 * Servlet implementation class ResTopUp
 */
@WebServlet("/ResTopUp")
public class ResTopUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private TenantContext  tenantContext;
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	
	//private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResTopUp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		CommonUtils commonUtils=new CommonUtils();
		String destAggr="",appLogId="";
		JsonArray appLogMsgArry=new JsonArray();
		try
		{
			destAggr=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", pgParams = "", pgID = "", loginID="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if(null != request.getParameter("PGParams"))
				pgParams = request.getParameter("PGParams");
			
			String[] resSplitResult = pgParams.split("\\~");
			String resParamName="", resParamValue="";
			for(String s : resSplitResult)
			{
				resParamName = s.substring(0, 5);
				resParamValue = s.substring(5,s.length());
	        	if(resParamName.equalsIgnoreCase("PGIDS"))
	        	{
	        		pgID = resParamValue;
	        	}
	        	if(resParamName.equalsIgnoreCase("LOGINIDS"))
	        	{
	        		loginID = resParamValue;
	        	}
			}
			String configurationValues = getConstantValues(request, response, loginID, pgID);
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("configurationValues: "+configurationValues);
			
			String wholeParamString="", paramName="", paramValue="",clientCode = "";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
//	        	if(paramName.equalsIgnoreCase("MerchantCode"))
//	        	{
//	        		merchantCode = paramValue;
//	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
			}
			
			
//			walletPublicKey = properties.getProperty("walletPublicKey");
//			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//			merchantPublicKey = properties.getProperty("merchantPublicKey");
			response.getWriter().println("clientCode: "+clientCode);
			
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("PRD Keys found");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
			}
			else
			{
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}
			response.getWriter().println("walletResponseMessage: "+request.getParameter("walletResponseMessage"));
//			merchantCode = properties.getProperty("merchantCode");
//			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgResponseErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
			pgRespMsg = request.getParameter("walletResponseMessage");
			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setWalletResponseMessage(pgRespMsg);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);

			String walletTxnStatus="", txnID="", walletUserCode="", txnSessionID="", walletTxnID="", walletTxnDateTime="", txnDateTime="", walletBankRefID="", txnFor="",
					txnAmount="", walletTxnRemarks="", adnlParam1="", adnlParam2="", adnlParam3="", adnlParam4="", adnlParam5="", adnlParam6="", adnlParam7="", adnlParam8="",
					adnlParam9="", adnlParam10="", pgMode="", pgName="";
					// response.getWriter().print("walletResponseMessage: "+request.getParameter("walletResponseMessage"));
			if(walletMessageBean.validateWalletResponseMessage()) 
			{
				response.getWriter().print("Inside validateWalletResponseMessage=true:");
				WalletParamMap responseMap = walletMessageBean.getResponseMap();
				walletTxnStatus = responseMap.get("wallet-txn-status");
				txnID = responseMap.get("txn-id");
				txnAmount = responseMap.get("txn-amount");
				walletUserCode = responseMap.get("wallet-user-code");
				txnSessionID = responseMap.get("txn-session-id");
				walletTxnID = responseMap.get("wallet-txn-id");
				walletTxnDateTime = responseMap.get("wallet-txn-datetime");
				txnDateTime = responseMap.get("txn-datetime");
				walletBankRefID = responseMap.get("wallet-bank-ref-id");
				txnFor = responseMap.get("txn-for");
				walletTxnRemarks = responseMap.get("wallet-txn-remarks");
				adnlParam1 = responseMap.get("additional-param1");
				adnlParam2 = responseMap.get("additional-param2");
				adnlParam3 = responseMap.get("additional-param3");
				adnlParam4 = responseMap.get("additional-param4");
				adnlParam5 = responseMap.get("additional-param5");
				adnlParam6 = responseMap.get("additional-param6");
				adnlParam7 = responseMap.get("additional-param7");
				adnlParam8 = responseMap.get("additional-param8");
				adnlParam9 = responseMap.get("additional-param9");
				adnlParam10 = responseMap.get("additional-param10");
				pgMode = responseMap.get("pg-mode");
				pgName = responseMap.get("pg-name");
				
				//response.getWriter().println("txnID:"+txnID);
				//response.getWriter().println("txnSessionID:"+txnSessionID);
				
				JsonObject apploGObj = getApplicationLogobj(txnID,response, destAggr, false);
				//response.getWriter().println("apploGObj:"+apploGObj);
				
				if(apploGObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					appLogId=apploGObj.get("ID").getAsString();
				}
				
				JsonObject result = new JsonObject();
				result.addProperty("walletTxnStatus", walletTxnStatus);
				result.addProperty("txnID", txnID);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("walletUserCode", walletUserCode);
				result.addProperty("txnSessionID", txnSessionID);
				result.addProperty("walletTxnID", walletTxnID);
				result.addProperty("walletTxnDateTime", walletTxnDateTime);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("walletBankRefID", walletBankRefID);
				result.addProperty("txnFor", txnFor);
				result.addProperty("walletTxnRemarks", walletTxnRemarks);
				result.addProperty("adnlParam1", adnlParam1);
				result.addProperty("adnlParam2", adnlParam2);
				result.addProperty("adnlParam3", adnlParam3);
				result.addProperty("adnlParam4", adnlParam4);
				result.addProperty("adnlParam5", adnlParam5);
				result.addProperty("adnlParam6", adnlParam6);
				result.addProperty("adnlParam7", adnlParam7);
				result.addProperty("adnlParam8", adnlParam8);
				result.addProperty("adnlParam9", adnlParam9);
				result.addProperty("adnlParam10", adnlParam10);
				result.addProperty("pgMode", pgMode);
				result.addProperty("pgName", pgName);
				
				//response.getWriter().println(result);
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, result+"", appLogId,appLogMsgArry);
				//response.getWriter().println("appLogMsgArry"+appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggr);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					JsonObject callOnObjectEventPublish = commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				   response.getWriter().println("callOnObjectEventPublish:"+callOnObjectEventPublish);
				}
				response.getWriter().print(new Gson().toJson(result));
				response.setContentType("text/html");
				if(null != walletTxnStatus)
					request.setAttribute("walletTxnStatus", walletTxnStatus);
				else
					request.setAttribute("walletTxnStatus", "Not Found");
				if(null != txnID)
					request.setAttribute("txnID", txnID);
				else
					request.setAttribute("txnID", "Not Found");
				if(null != txnAmount)
					request.setAttribute("txnAmount", txnAmount);
				else
					request.setAttribute("txnAmount", "0.00");
				String redirectURL = "";
				
				
				if(adnlParam4 != null && adnlParam4.toString().trim().length()>0)
				{
					if(adnlParam4.toString().trim().equalsIgnoreCase("NA"))
					{
						redirectURL = "/sites/"+adnlParam1+"#"+adnlParam2+"-"+adnlParam3+"?walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+",cpNo="+walletUserCode+",source=Top";
					}
					else
					{
						redirectURL = "/sites/"+adnlParam1+"#"+adnlParam2+"-"+adnlParam3+"&/"+adnlParam4+"/(walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+",cpNo="+walletUserCode+",source=Top"+")";
					}
					//Host hardcoded to check java app on CF and portal on Neo. Commend the below line when testing with CF portal
					response.sendRedirect("https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com"+redirectURL);
				}
				else
				{
					response.sendRedirect("/sites/SSLaunchpad");
				}
			}
			else
			{
				response.getWriter().print("Inside validateWalletResponseMessage=false:");
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgResponseErrorMsg);
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", result+"", appLogId,appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggr);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
				response.setContentType("text/html");
				request.setAttribute("pgReqMsg", pgResponseErrorMsg);
			}
		}
		catch (Exception e)
		{
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"-->");
			if(e.getLocalizedMessage()!=null){
			buffer.append(e.getLocalizedMessage());
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			JsonObject resObj=new JsonObject();
			resObj.addProperty("ExceptionMessage", buffer.toString());
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", resObj+"", appLogId,appLogMsgArry);
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggr);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			response.getWriter().print(e.getMessage());
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Error in processing Payment. Please contact portal administrator");
			response.getWriter().print(new Gson().toJson(result));
			response.setContentType("text/html");
			request.setAttribute("pgReqMsg", "Error in processing Payment. Please contact portal administrator");
			RequestDispatcher view = request.getRequestDispatcher("/OSTopup.jsp");
			view.forward(request,response);
		}
	}
	@SuppressWarnings("finally")
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
				if(PGID.equalsIgnoreCase("B2BIZ"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
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
				destinationName = DestinationUtils.PCGW_UTILS_OP;
			}
				
		String tempSapClient = null;
				
		try {
				// get all destination properties
				// Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
				// DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGW_UTILS_OP);
				// Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				Destination destConfiguration = destinationAccessor.get();

				//Reading SAP-Client from All Configuration.........................................................
				tempSapClient = destConfiguration.get("sap-client").get().toString();
			}
			catch (Exception e) {
				// Connectivity operation failed
				String errorMessage = "Connectivity operation failed with reason: "
							+ e.getMessage()
							+ ". See "
							+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
				LOGGER.error("Connectivity operation failed", e);
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
	
	private JsonObject getApplicationLogobj(String txnId,HttpServletResponse response,String destAggrID,boolean debug) throws Exception{
		CommonUtils commonUtils=new CommonUtils();
		String username="",password="",oDataUrl="",userpass="",executeUrl="";
		JsonObject resObj=new JsonObject();
		try{
			oDataUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userpass=username+":"+password;
			executeUrl=oDataUrl+"ApplicationLogs?$filter=AggregatorID%20eq%20%27"+destAggrID+"%27%20and%20SourceReferenceID%20eq%20%27"+txnId+"%27";
			
			//response.getWriter().println("executeUrl:"+executeUrl);
			
			JsonObject appLogObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			response.getWriter().println("appLogObj:"+appLogObj);
			if(appLogObj.get("Status").getAsString().equalsIgnoreCase("000001")){
				if(appLogObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
					String logId=appLogObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("ID").getAsString();
					resObj.addProperty("ID", logId);
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
				}else{
					resObj.addProperty("ID", "");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
				}
			}else{
				return appLogObj;
			}
			
			return resObj;
		}catch(Exception ex){
			throw ex;
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
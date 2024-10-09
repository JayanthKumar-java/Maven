package com.arteriatech.pg;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.log.SysoCounter;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.http.client.HttpClient;

public class EscrowReqRefund extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String txnID="",loginID="",PGID="",refundReqMessage="";
	boolean debug=false;
	JsonObject resObj=new JsonObject();
	CommonUtils commonUtils=new CommonUtils();
	Properties properties = new Properties();
	properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
	try{
		if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		if(request.getParameter("TxnID")!=null){
			txnID=request.getParameter("TxnID");
			if(debug){
				response.getWriter().println("TxnID:"+txnID);
			}
			/*if(request.getParameter("LoginID")!=null){
				loginID=request.getParameter("LoginID");
			}*/
			if(request.getParameter("PGID")!=null){
				PGID=request.getParameter("PGID");
			}
			
			loginID=commonUtils.getLoginID(request, response, debug);
			if(debug){
				response.getWriter().println("loginID:"+loginID);
			}
			/*JsonObject validatePaymentReq = validatePaymentReq(request, response, debug, txnID, loginID);
			if(debug){
				response.getWriter().println("validatePaymentReq:"+validatePaymentReq);
			}*/
			
			JsonObject configObj = getConstantValues(request, response,  PGID, loginID, debug);
			if(debug){
				response.getWriter().println("configObj:"+configObj);
			}
			
			if(!configObj.has("ErrorCode")){
				String walletPublicKey="",merchantPrivateKey="",merchantPublicKey="";
				if(!configObj.get("ClientCode").getAsString().equalsIgnoreCase("")){
					String clientCode = configObj.get("ClientCode").getAsString();
					walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
					merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				}else{
					walletPublicKey = properties.getProperty("WalletPublicKey");
					merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				}
				
				if(debug){
					response.getWriter().println("walletPublicKey: "+walletPublicKey);
					response.getWriter().println("merchantPrivateKey: "+merchantPrivateKey);
				}
				
				WalletParamMap refundReqMap = new WalletParamMap();
				String txn_id = commonUtils.generateGUID(36);
				if(debug){
					response.getWriter().println("txn_id:"+txn_id);
				}
				refundReqMap.put("txn-id",txn_id);
				refundReqMap.put("refund-client-txn-id",txnID);
				/*if(null != request.getParameter("txn-session-id"))
					refundReqMap.put("txn-session-id", request.getParameter("txn-session-id"));
				if(null != request.getParameter("txn-datetime"))
					refundReqMap.put("txn-datetime", request.getParameter("txn-datetime"));
				if(null != request.getParameter("txn-amount"))
					refundReqMap.put("txn-amount", request.getParameter("txn-amount"));
				if(null != request.getParameter("wallet-user-code"))
				     refundReqMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
				     refundReqMap.put("txn-for", "Payment for CSC");
				if(null != request.getParameter("return-url"))
					refundReqMap.put("return-url", request.getParameter("return-url"));
				if(null != request.getParameter("cancel-url"))
					refundReqMap.put("cancel-url", request.getParameter("cancel-url"));*/
				if(null != request.getParameter("site-id"))
					refundReqMap.put("additional-param1", request.getParameter("site-id"));
				else
					refundReqMap.put("additional-param1", "NA");
				
				if(null != request.getParameter("application-id"))
					refundReqMap.put("additional-param2", request.getParameter("application-id"));
				else
					refundReqMap.put("additional-param2", "NA");
				
				if(null != request.getParameter("action-name"))
					refundReqMap.put("additional-param3", request.getParameter("action-name"));
				else
					refundReqMap.put("additional-param3", "NA");
				
				if(null != request.getParameter("nav-param"))
					refundReqMap.put("additional-param4", request.getParameter("nav-param"));
				else
					refundReqMap.put("additional-param4", "NA");
				
				if(null != request.getParameter("additional-param5"))
					refundReqMap.put("additional-param5", request.getParameter("additional-param5"));
				else
					refundReqMap.put("additional-param5", "NA");
				if(null != request.getParameter("additional-param6"))
					refundReqMap.put("additional-param6", request.getParameter("additional-param6"));
				else
					refundReqMap.put("additional-param6", "NA");
				if(null != request.getParameter("additional-param7"))
					refundReqMap.put("additional-param7", request.getParameter("additional-param7"));
				else
					refundReqMap.put("additional-param7", "NA");
				if(null != request.getParameter("additional-param8"))
					refundReqMap.put("additional-param8", request.getParameter("additional-param8"));
				else
					refundReqMap.put("additional-param8", "NA");
				if(null != request.getParameter("additional-param9"))
					refundReqMap.put("additional-param9", request.getParameter("additional-param9"));
				else
					refundReqMap.put("additional-param9", "NA");
				if(null != request.getParameter("pay-for"))
					refundReqMap.put("additional-param10", request.getParameter("pay-for"));
				else
					refundReqMap.put("additional-param10", "NA");

				WalletMessageBean walletMessageBean = new WalletMessageBean();
				walletMessageBean.setRequestMap(refundReqMap);
				walletMessageBean.setWalletKey(walletPublicKey);
				walletMessageBean.setClientKey(merchantPrivateKey);
				refundReqMessage= walletMessageBean.generateWalletRequestMessage();
				if(debug){
					response.getWriter().println("refundReqMessage :"+refundReqMessage);
				}
				if(refundReqMessage != null && refundReqMessage.trim().length() > 0){
					JsonObject result = new JsonObject();
					result.addProperty("walletRequestMessage", refundReqMessage);
					result.addProperty("walletClientCode", configObj.get("MerchantCode").getAsString());
					result.addProperty("Valid", "true");
					result.addProperty("WSURL", "https://demo.b2biz.co.in/ws/refund");
					result.addProperty("parameters","walletClientCode|walletRequestMessage");
					response.getWriter().print(new Gson().toJson(result));
					
					WalletMessageBean walletResMessageBean = new WalletMessageBean(); 
					
					walletMessageBean.setWalletResponseMessage(refundReqMessage);
					walletMessageBean.setWalletKey(walletPublicKey);
					walletMessageBean.setClientKey(merchantPrivateKey);
					
					if(walletResMessageBean.validateWalletResponseMessage()) 
					{
						response.getWriter().println("walletResMessageBean:"+walletResMessageBean);
						WalletParamMap responseMap = walletResMessageBean.getResponseMap();
						
						response.getWriter().println("responseMap:"+responseMap);
						response.getWriter().println("walletResMessageBean.getClientKey():"+walletResMessageBean.getClientKey());
						response.getWriter().println("walletResMessageBean.getWalletKey():"+walletResMessageBean.getWalletKey());
	
						response.getWriter().println("walletResMessageBean.getWalletResponseMessage():"+walletResMessageBean.getWalletResponseMessage());
						
						if(!responseMap.isEmpty()){
							responseMap.forEach((key,value)->{
								try {
									response.getWriter().println("Key-->"+key+"Value--->"+value);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
						}else{
							response.getWriter().println("responseMap map is empty:"+responseMap);
						}
						
					}
					
				}else{
					String pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", pgRequestErrorMsg);
					result.addProperty("walletClientCode", configObj.get("MerchantCode").getAsString());
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				// unable to fetch the Records from PgConfig Table.
				response.getWriter().println(configObj);
			}
			
		}else{
			// Transaction Id missing in the payload.
			resObj.addProperty("Message", "TxnID Missing in the input payload");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
			
		}
		
		
		
	}catch(Exception ex){
		StringBuffer buffer=new StringBuffer();
		StackTraceElement[] stackTrace = ex.getStackTrace();
		for(int i=0;i<stackTrace.length;i++){
			buffer.append(stackTrace[i]);
		}
		
		resObj.addProperty("Exception Message", ex.getLocalizedMessage());
		resObj.addProperty("Exception Trace", buffer.toString());
		resObj.addProperty("Status", "000002");
		resObj.addProperty("ErrorCode", "J002");
		response.getWriter().println(resObj);	
	}
	}

	
	private JsonObject validatePaymentReq(HttpServletRequest request,HttpServletResponse response,boolean debug,String txnID,String loginID)
			throws IOException, URISyntaxException {
		String destURL = "", pgID = "", userName = "", password = "", authParam = "", authMethod = "",
				paymentService = "", paymentFilter = "", basicAuth = "", sapclient = "", sessionID = "",
				loginMethod = "";
		String returnMessage = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		HttpGet pgPaymentsGet = null;
		HttpEntity pgPaymentsEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;
		JsonObject resObj=new JsonObject();
		try {
			destConfiguration = getDestinationURL(request, response);
			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (debug) {
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}

			if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
				String url = commonUtils.getDestinationURL(request, response, "URL");
				if (debug)
					response.getWriter().println("url1:" + url);
				sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
			} else {
				loginMethod = destConfiguration.get("LoginMethod").get().toString();
				if (null != loginMethod && loginMethod.equalsIgnoreCase("Hybrid")) {
					String url = commonUtils.getDestinationURL(request, response, "URL");
					if (debug) {
						response.getWriter().println("url:" + url);
						response.getWriter().println("loginMethod:" + loginMethod);
					}
					sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				} else {
					sessionID = "";
				}
			}
			if (debug)
				response.getWriter().println("sessionID1:" + sessionID);

			paymentFilter = "LoginID eq '" + sessionID + "' and TrackID eq'" + txnID + "'"+" and PGCategoryID eq '000002'";
			if (debug)
				response.getWriter().println("paymentFilter:" + paymentFilter);

			paymentFilter = URLEncoder.encode(paymentFilter, "UTF-8");
			paymentFilter = paymentFilter.replaceAll("%26", "&");
			paymentFilter = paymentFilter.replaceAll("%3D", "=");

			if (debug)
				response.getWriter().println("paymentFilter: " + paymentFilter);

			if (sapclient != null) {
				paymentService = destURL + "/sap/opu/odata/ARTEC/PCGW/PGPayments?sap-client=" + sapclient + "&$filter="
						+ paymentFilter;
			} else {
				paymentService = destURL + "/sap/opu/odata/ARTEC/PCGW/PGPayments?$filter=" + paymentFilter;
			}

			if (debug)
				response.getWriter().println("paymentService 1: " + paymentService);

			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				String encodedStr = new String(encodedByte);
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if (debug) {
				response.getWriter().println("validateCustomer.proxyType: " + proxyType);
				response.getWriter().println("validateCustomer.proxyHost: " + proxyHost);
				response.getWriter().println("validateCustomer.proxyPort: " + proxyPort);
			}

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy); */
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
							.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			// closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
			pgPaymentsGet = new HttpGet(paymentService);
			// pgPaymentsGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			pgPaymentsGet.setHeader("content-type", "application/json");
			pgPaymentsGet.setHeader("Accept", "application/json");
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				pgPaymentsGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				pgPaymentsGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			if (debug)
				response.getWriter().println("pgPaymentsGet: " + pgPaymentsGet);
			HttpResponse httpResponse = client.execute(pgPaymentsGet);
			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("validatePaymentReq.statusCode: " + statusCode);
            if(statusCode==200){
			pgPaymentsEntity = httpResponse.getEntity();
			if (pgPaymentsEntity != null) {
				String retSrc = EntityUtils.toString(pgPaymentsEntity);
				if (debug)
					response.getWriter().println("validatePaymentReq.retSrc: " + retSrc);
				JsonParser parser = new JsonParser();
				JsonObject pymntTxnObject = (JsonObject) parser.parse(retSrc);
				if(debug){
					response.getWriter().println("pymntTxnObject:"+pymntTxnObject);
				}
				if(pymntTxnObject.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
					JsonArray pgPaymentsArray = pymntTxnObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
					for(int i=0;i<pgPaymentsArray.size();i++){
						JsonObject pgPaymentObj = pgPaymentsArray.get(i).getAsJsonObject();
						String pgTxnStatusID = pgPaymentObj.get("PGTxnStatusID").getAsString();
						String paymentStatusId = pgPaymentObj.get("PaymentStatusID").getAsString();
						if(pgTxnStatusID.equalsIgnoreCase("000200")&&paymentStatusId.equalsIgnoreCase("000200")){
							resObj.addProperty("Message", "Success Payment");
							resObj.addProperty("ErrorCode", "");
							resObj.addProperty("Status", "000001");
						}else{
							resObj.addProperty("Message", "Failure Payments");
							resObj.addProperty("ErrorCode", "J002");
							resObj.addProperty("Status", "000002");	
						}
					}
					
				}else{
					resObj.addProperty("Message", "Record not exist in the PGPayments Table for the given Transaction Id:"+txnID);
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
				}
				
			} else {
				resObj.addProperty("Message", "Record not exist in the PGPayments Table for the given Transaction Id:"+txnID);
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
			
			}
            }else if(statusCode==401){
            	response.getWriter().println("Unauthorized issue while accessing the Records from Back end table");
            }else{
            	response.getWriter().println("Unable to fetch the records from the backend table.");	
            }
		} catch (RuntimeException e) {
			// customerRequest.abort();
			if (debug) {
				response.getWriter().println("RuntimeException in validatePaymentReq: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}

			returnMessage = e.getMessage();
		} /*catch (NamingException e) {
			if (debug) {
				response.getWriter().println("NamingException in validatePaymentReq: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			returnMessage = e.getMessage();

		}  finally {
			closableHttpClient.close();
		} */
		
		return resObj;
	}
	
	
	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String destinationName = "";
		destinationName = DestinationUtils.PCGW_UTILS_OP;
		Destination destConfiguration = null;
		try {
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn
			// 		.lookup("java:comp/env/connectivityConfiguration");
			// destConfiguration = configuration.getConfiguration(destinationName);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format(
							"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
							DestinationUtils.PCGW_UTILS_OP));
		}
		return destConfiguration;
	}
	
	
	private JsonObject getConstantValues(HttpServletRequest request, HttpServletResponse response, String PGID,String loginID,boolean debug) throws IOException, URISyntaxException
	{
		String destURL = "",  userName = "", password = "", authParam = "", authMethod = "",
				 basicAuth = "", sapclient = "", sessionID = "",
				loginMethod = "";
		String returnMessage = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		HttpGet pgPaymentsGet = null;
		HttpEntity pgPaymentsEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;
		JsonObject resObj=new JsonObject();
		try {
			destConfiguration = getDestinationURL(request, response);
			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();

			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			if (debug) {
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}

			if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
				String url = commonUtils.getDestinationURL(request, response, "URL");
				if (debug)
					response.getWriter().println("url1:" + url);
				sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
			} else {
				loginMethod = destConfiguration.get("LoginMethod").get().toString();
				if (null != loginMethod && loginMethod.equalsIgnoreCase("Hybrid")) {
					String url = commonUtils.getDestinationURL(request, response, "URL");
					if (debug) {
						response.getWriter().println("url:" + url);
						response.getWriter().println("loginMethod:" + loginMethod);
					}
					sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				} else {
					sessionID = "";
				}
			}
			if (debug)
				response.getWriter().println("sessionID1:" + sessionID);

			String constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			String constantValuesFilter = "";
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
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			/* String proxyType = destConfiguration.getProperty("ProxyType");
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if (debug) {
				response.getWriter().println("validateCustomer.proxyType: " + proxyType);
				response.getWriter().println("validateCustomer.proxyHost: " + proxyHost);
				response.getWriter().println("validateCustomer.proxyPort: " + proxyPort);
			}

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			
			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
			pgPaymentsGet = new HttpGet(constantValuesService);
			// pgPaymentsGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			pgPaymentsGet.setHeader("content-type", "application/json");
			pgPaymentsGet.setHeader("Accept", "application/json");
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				pgPaymentsGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				pgPaymentsGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			if (debug)
				response.getWriter().println("pgPaymentsGet: " + pgPaymentsGet);
			HttpResponse httpResponse = client.execute(pgPaymentsGet);
			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("validatePaymentReq.statusCode: " + statusCode);
            if(statusCode==200){
			pgPaymentsEntity = httpResponse.getEntity();
			if (pgPaymentsEntity != null) {
				String retSrc = EntityUtils.toString(pgPaymentsEntity);
				if (debug)
					response.getWriter().println("PGConfig restr: " + retSrc);
				JsonParser parser = new JsonParser();
				JsonObject pgConfig = (JsonObject) parser.parse(retSrc);
				if(debug){
					response.getWriter().println("PGConfig Object:"+pgConfig);
				}
				if(pgConfig.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
					JsonArray pgConfigArray = pgConfig.get("d").getAsJsonObject().get("results").getAsJsonArray();
					for(int i=0;i<pgConfigArray.size();i++){
						JsonObject pgConfigObj = pgConfigArray.get(i).getAsJsonObject();
						if(!pgConfigObj.get("ClientCode").isJsonNull()&&!pgConfigObj.get("ClientCode").getAsString().equalsIgnoreCase("")){
						resObj.addProperty("ClientCode", pgConfigObj.get("ClientCode").getAsString());
						}else{
							resObj.addProperty("ClientCode","");	
						}
						if(!pgConfigObj.get("TxnStsURL").isJsonNull()&&!pgConfigObj.get("TxnStsURL").getAsString().equalsIgnoreCase("")){
							resObj.addProperty("WAPIURL", pgConfigObj.get("TxnStsURL").getAsString());
						}else{
							resObj.addProperty("WAPIURL", "");	
						}
						
						if(!pgConfigObj.get("MerchantCode").isJsonNull()&&!pgConfigObj.get("MerchantCode").getAsString().equalsIgnoreCase("")){
							resObj.addProperty("MerchantCode", pgConfigObj.get("MerchantCode").getAsString());
						}else{
							resObj.addProperty("MerchantCode", "");	
						}
					}
					return resObj;
					
				}else{
					resObj.addProperty("Message", "Record not exist in the PGPayments Table for the given Transaction Id:");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
				}
				
			} else {
				resObj.addProperty("Message", "Record not exist in the PGPayments Table for the given Transaction Id:");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
			
			}
            }else if(statusCode==401){
            	response.getWriter().println("Unauthorized issue while accessing the Records from Back end table");
            }else{
            	response.getWriter().println("Unable to fetch the records from the backend table.");	
            }
		} catch (RuntimeException e) {
			if (debug) {
				response.getWriter().println("RuntimeException in validatePaymentReq: " + e.getMessage());
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			returnMessage = e.getMessage();
		} /* catch (NamingException e) {
			if (debug) {
				response.getWriter().println("NamingException in validatePaymentReq: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			returnMessage = e.getMessage();

		} finally {
			closableHttpClient.close();
		} */
		
		return resObj;
	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	
	

}

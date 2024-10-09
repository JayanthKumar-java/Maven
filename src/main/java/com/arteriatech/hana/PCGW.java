package com.arteriatech.hana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.arteriatech.bc.TransactionOTPGenerate.OTPGeneratorClient;
import com.arteriatech.cf.ChannelFinanceOps;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.Principal;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import com.sap.cloud.sdk.cloudplatform.tenant.Tenant;
/**
 * Servlet implementation class PCGW
 */
@WebServlet("/PCGW")
public class PCGW extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PCGW() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String oDataURL = "", loginID="";
		CommonUtils utils = new CommonUtils();
		/* Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
		Try<Principal> currentPrincipal = PrincipalAccessor.tryGetCurrentPrincipal();
		System.out.println("currentTenant: "+currentTenant);
		System.out.println("currentPrincipal: "+currentPrincipal); */

		oDataURL = utils.getODataDestinationProperties("URL", "PCGWHANA");
		if(oDataURL != null && ! oDataURL.equalsIgnoreCase("E106") && ! oDataURL.contains("E173")){
			loginID = utils.getODataDestinationProperties("User", "PCGWHANA");
			if(loginID != null && ! loginID.equalsIgnoreCase("E106") && ! loginID.contains("E173")){
				executeODataCalls(request, response, oDataURL);
			}else{
				response.getWriter().println("Destination 'PCGWHANA' not maintained in sub account");
			}
		}else{
			response.getWriter().println("Destination 'PCGWHANA' not maintained in sub account");
		}
	}
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		ChannelFinanceOps channelFinanceOps = new ChannelFinanceOps();
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String payloadRequest = "", dataPayload="", aggregatorID="", loginID="", oDataURL="", pathInfo="",userName="", password="", userPass="";
		try{
			debug = false;
			JSONObject inputPayload = new JSONObject();
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName+":"+password;
			
			try {
				payloadRequest = getGetBody(request, response);
				inputPayload = new JSONObject(payloadRequest);
				if (inputPayload.has("debug") && ! inputPayload.isNull("debug") && inputPayload.getBoolean("debug") == true ) {
					debug = true;
					response.getWriter().println("doPut.PGPayments.: "+inputPayload); 
				}
			} catch (Exception e) {
				debug = false;
			}
			
			pathInfo = request.getPathInfo().toString();
			pathInfo = pathInfo.substring(1, pathInfo.indexOf('('));

			if(debug){
				response.getWriter().println("doPut.payloadRequest: "+payloadRequest);
				response.getWriter().println("doPut.aggregatorID: "+aggregatorID);
				response.getWriter().println("doPut.loginID: "+loginID);
				response.getWriter().println("doPut.oDataURL: "+oDataURL);
				response.getWriter().println("doPut.pathInfo: "+pathInfo);
				response.getWriter().println("doPut.getRequestURL: "+request.getRequestURL());
				response.getWriter().println("doPut.getPathInfo: "+request.getPathInfo());
			}
			
			if(pathInfo.equalsIgnoreCase("PGPayments")){
//				debug = true;
				if(debug)
					response.getWriter().println("doPut.PGPayments: ");
				//CALL POSTTXN ws
				commonUtils.postTransactionValidation(request, response, properties, inputPayload, aggregatorID, loginID, oDataURL, debug);
			}else if (pathInfo.equalsIgnoreCase("PGPaymentCategories")){
				if(debug)
					response.getWriter().println("doPut.PGPaymentCategories: ");
				channelFinanceOps.updatePGPaymentCategories(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
			}else if (pathInfo.equalsIgnoreCase("PGPaymentConfigs")){
				if(debug)
					response.getWriter().println("doPut.PGPaymentConfigs: ");
				channelFinanceOps.updatePGPaymentConfigs(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
			}else if(pathInfo.equalsIgnoreCase("PGPaymentConfigStats")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.updatePGPaymentConfigStats(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("ValueHelps")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.updateValueHelps(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("DocumentRepConfigs")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.updateDocumentRepConfigs(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("DocumentRepository")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.updateDocumentRepository(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("MISScheduler")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.updateMISScheduler(response, request, inputPayload, userPass, oDataURL, pathInfo, aggregatorID, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}
			else
			{
				response.getWriter().println("doPut.outside");
				response.getWriter().println("doPut.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doPut.getRequestURL: "+request.getRequestURL());
				response.getWriter().println("doPut.getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doPut.getContextPath: "+request.getContextPath());
				response.getWriter().println("doPut.getQueryString: "+request.getQueryString());
				response.getWriter().println("doPut.getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doPut.getServletContext: "+request.getServletContext());
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
		}
	}	
	public void executeODataCalls(HttpServletRequest request, HttpServletResponse response, String oDataURL) throws IOException{
		boolean debug = false;
		// CloseableHttpClient httpClient = null;
		String serviceURL="", aggregatorID="", loginID="";
		HttpGet readRequest = null;
		HttpEntity countEntity = null;
		CommonUtils commonUtils = new CommonUtils();
		String tokenURL = "", batchID="";
		String userName="", password="", authParam="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		ChannelFinanceOps channelFinanceOps = new ChannelFinanceOps();
		/*response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
		response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
		response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
		response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
		response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
		response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());*/
//		response.getWriter().println("loadMetadata oDataURL: "+oDataURL);
		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("PCGWHANA");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PCGWHANA", options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			
			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "PCGWHANA"));
            }
			debug = false;
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			
			if(debug){
		        response.getWriter().println("executeODataCalls.proxyType: "+ proxyType);
		        response.getWriter().println("executeODataCalls.userName: "+ userName);
		        response.getWriter().println("executeODataCalls.password: "+ password);
		        response.getWriter().println("executeODataCalls.authParam: "+ authParam);
		        response.getWriter().println("executeODataCalls.basicAuth: "+ basicAuth);
			}
			
			/*String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	        int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
	        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);*/
	        
	        /* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials); */
	        
			
//			serviceURL = oDataURL+"$metadata";
			serviceURL = request.getPathInfo();
			tokenURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			if(serviceURL.contains("$metadata")) {
				serviceURL= "";
				serviceURL = oDataURL+"$metadata";
			}else if(serviceURL.contains("$batch")) {
				serviceURL= "";
				serviceURL = oDataURL+"$batch";
			}
			if(debug)
				response.getWriter().println("executeODataCalls.serviceURL: "+ serviceURL);
			
			String payloadRequest = "";
//			HttpDestination destination = getHTTPDestination(request, response, "PCGWHANA");
//			response.getWriter().println("loadMetadata serviceURL1: "+serviceURL);
			if(!serviceURL.contains("$metadata")) {
//				String csrfToken = fetchCSRFToken(request, response, tokenURL);
//				response.getWriter().println("loadMetadata csrfToken: "+csrfToken);
//				httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
				// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
				serviceURL = serviceURL.replaceAll("%20", " ");
//				payloadRequest = getBody(request, response);
//				if(csrfToken != null && !csrfToken.equalsIgnoreCase("E174") && !csrfToken.equalsIgnoreCase("E175")){
					if(serviceURL.contains("$batch")){
						String replacedPayload = "";
						
						payloadRequest = getBody(request);
//						response.getWriter().println("loadMetadata received payload: "+payloadRequest);
						
						if(payloadRequest.contains("GET ValueHelps")){
							String appendStr = "", dataString="";
							dataString = payloadRequest.substring(payloadRequest.indexOf("GET ValueHelps"), payloadRequest.lastIndexOf(" HTTP/1.1"));
							appendStr = "%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							replacedPayload = payloadRequest.replace(dataString, dataString+appendStr);
							payloadRequest = replacedPayload;
//							response.getWriter().println("loadMetadata ValueHelps: "+payloadRequest);
						}
						
						batchID = request.getHeader("Content-Type");
//						response.getWriter().println("loadMetadata batchID: "+batchID);

						HttpEntity requestEntity = null;
//						requestEntity = new ByteArrayEntity(payloadRequest.getBytes("UTF-8"));
						requestEntity = new StringEntity(payloadRequest);
						
						HttpPost postRequest = new HttpPost(serviceURL);
						postRequest.setHeader("Content-Type", batchID);
						postRequest.setHeader("Accept", "multipart/mixed");
//						postRequest.setHeader("X-CSRF-Token", csrfToken);
						postRequest.setEntity(requestEntity);
						
						// HttpResponse httpPostResponse = httpClient.execute(postRequest);
						HttpResponse httpPostResponse = client.execute(postRequest);

						countEntity = httpPostResponse.getEntity();
						
						if(httpPostResponse.getEntity().getContentType() != null && httpPostResponse.getEntity().getContentType().toString() != "") {
							String contentType = httpPostResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
							if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
								response.setContentType(contentType);
								response.getOutputStream().print(EntityUtils.toString(countEntity));
							}else{
								response.setContentType(contentType);
								String Data = EntityUtils.toString(countEntity);
								response.getOutputStream().print(Data);	
							}
						}else{
							response.setContentType("application/pdf");
							response.getOutputStream().print(EntityUtils.toString(countEntity));
						}
						
						// httpClient.close();
					}
					else{
						 if(request.getPathInfo().equalsIgnoreCase("/PGPayments")){
//							 loginID ="";
							 String executeURL = "",oDataUserName="", oDatapassword="", userPass="";
							 debug = false;
							 String errorResFormatForPGPayments="";
							 String pygwODataURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
							 errorResFormatForPGPayments= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
										+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
										+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PCGW\",\"service_version\":\"0001\"},"
										+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
										+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
										+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
										+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
										+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
										+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
										+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
								
							 oDataUserName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
							 oDatapassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
							 userPass = oDataUserName+":"+oDatapassword;
							 if(debug){
								 response.getWriter().println("PGPayments.oDataUserName: "+oDataUserName);
								 response.getWriter().println("PGPayments.oDatapassword: "+oDatapassword);
								 response.getWriter().println("PGPayments.userPass: "+userPass);
							 }
							 
							 if(request.getMethod().equalsIgnoreCase("POST")){
									debug = false;
								String configHeaderGUIDInput="", configHeaderGuidTable="", commonGuid= "", trackId="", paymentTxnStatus="", paymentStatus="", pgPymntPostingSts="";	
								JsonObject pymntConfigJsonObj = new JsonObject();
								JsonArray pymntConfigJsonArray = new JsonArray();
								
								boolean isTxnTypeAvailable = false;
								JsonObject buildPostPymntResponse = new JsonObject();
								JsonObject insertPymntHeader = new JsonObject();
								Map<String, String> otpGenerateServiceMap = new HashMap<String, String>();

								payloadRequest ="";
								payloadRequest = getGetBody(request, response);
								JSONObject inputPayload = new JSONObject(payloadRequest);
								try {
									if ( inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											inputPayload.getBoolean("debug") == true) 
										debug = true;
											
								} catch (Exception e) {
									debug = false;
								}
								if(debug)
									response.getWriter().println("PGPayments.inputPayload: "+inputPayload);
								
								commonGuid =  inputPayload.getString("PGPaymentGUID");  //commonUtils.generateGUID(36);
								trackId = commonUtils.createRepaymentTrackID(request, response, properties, debug);
								
								if (debug) {
									response.getWriter().println("PGPayments.loginID: "+loginID);
									response.getWriter().println("PGPayments.commonGuid: "+commonGuid);
									response.getWriter().println("PGPayments.trackId: "+trackId);
									response.getWriter().println("PGPayments.pygwODataURL: "+pygwODataURL);
								}
								
								if(trackId != null && trackId.trim().length() > 0){
									configHeaderGUIDInput = inputPayload.getString("ConfigHeaderGUID");
									if (configHeaderGUIDInput.contains("-"))
										configHeaderGUIDInput = configHeaderGUIDInput.replace("-", "");
									if(debug)
										response.getWriter().println("PGPayments.configHeaderGUIDInput: "+configHeaderGUIDInput);
									
									//filtering PGPaymentConfig based on configHeaderGuid( portal )
									executeURL = tokenURL+ "PGPaymentConfigs?$filter=ConfigHeaderGUID%20eq%20%27"+configHeaderGUIDInput+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									if(debug)
										response.getWriter().println("POSTPGPayments.executeURL1: "+executeURL);
									
									pymntConfigJsonObj = commonUtils.executeURL(executeURL, userPass, response);
									if(debug)
										response.getWriter().println("POSTPGPayments.pymntConfigJsonObj: "+pymntConfigJsonObj);
									
									pymntConfigJsonArray = pymntConfigJsonObj.getAsJsonObject("d").getAsJsonArray("results");
									if (pymntConfigJsonArray.size() == 0) {
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", "Please Maintain Payment Configs");
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "116");
										response.getWriter().println(errorResFormatForPGPayments);
									}else{
										if (! pymntConfigJsonArray.get(0).getAsJsonObject().get("ConfigHeaderGUID").isJsonNull())
											configHeaderGuidTable = pymntConfigJsonArray.get(0).getAsJsonObject().get("ConfigHeaderGUID").getAsString();
										if(debug)
											response.getWriter().println("POSTPGPayments.configHeaderGuidTable: "+configHeaderGuidTable);	
										
										if (configHeaderGuidTable.equalsIgnoreCase(configHeaderGUIDInput)) {
											//TODO insert into PGPayment Header & PGPayment Items Table
											paymentTxnStatus ="000010";
											paymentStatus ="000100";
											pgPymntPostingSts = "000300";
											insertPymntHeader = commonUtils.insertIntoPaymentsHeader(request, response, aggregatorID, loginID, oDataURL, commonGuid, trackId, inputPayload, paymentTxnStatus, paymentStatus, pgPymntPostingSts, debug);
											if (debug)
												response.getWriter().println("POSTPGPayments.insertPymntHeader: "+insertPymntHeader);
											
											if(insertPymntHeader.get("ErrorCode").getAsString().trim().length() > 0 )
											{
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", insertPymntHeader.get("Message").getAsString());
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", insertPymntHeader.get("ErrorCode").getAsString());
												response.getWriter().println(errorResFormatForPGPayments);
												
											}else
											{
												String typeValue ="", OTPMaxLength="";
												executeURL ="";
												JsonObject cofigTypesetJson = new JsonObject();
												
												//TODO calling ConfigTypesetValues For Validation
												executeURL = tokenURL+"ConfigTypsetTypeValues"+"?$filter=Types%20eq%20%27"+"OTP"+"%27%20and%20Typeset%20eq%20%27"+"PY"+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
												if (debug)
													response.getWriter().println("POSTPGPayments.executeURL2: "+executeURL);
												
												cofigTypesetJson= commonUtils.executeURL(executeURL, userPass, response);
												if (debug)
													response.getWriter().println("POSTPGPayments.cofigTypesetJson: "+cofigTypesetJson);
												
												if (cofigTypesetJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0)
													typeValue = cofigTypesetJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("TypeValue").getAsString();
												if (debug)
													response.getWriter().println("POSTPGPayments.typeValue: "+typeValue);
												
												if(typeValue.equalsIgnoreCase("X") &&  typeValue.trim().length() > 0)
												{
													String corpId="", userId="", userRegId="", pgAmnt="", debitAcnNo="", txnType="", currency="", remarks="", pgCatId="";
													Map<String, String> userResgistrationMap = new HashMap<String, String>();
													
													//TODO Calling UserRegistration
													userResgistrationMap = commonUtils.getUserRegDetails(request, response, loginID, aggregatorID, pygwODataURL, debug);
													if (debug)
														response.getWriter().println("POSTPGPayments.userRegisJsonResponse: "+userResgistrationMap);
													
													if(userResgistrationMap.get("Error").equalsIgnoreCase("") &&  userResgistrationMap.get("Error").trim().length() == 0 &&
														userResgistrationMap.get("UserRegStatus").equalsIgnoreCase("000002")) {
														
														corpId = userResgistrationMap.get("CorpId");
														userId = userResgistrationMap.get("UserId");
														userRegId = userResgistrationMap.get("UserRegId");
														
														if(! inputPayload.isNull("CPAccountno")){
															debitAcnNo = inputPayload.getString("CPAccountno");
															debitAcnNo = replaceAccountNumber(debitAcnNo);
														}
														if(! inputPayload.isNull("PaymentAmount"))
															pgAmnt = inputPayload.getString("PaymentAmount");
														
														if(! inputPayload.isNull("Currency"))
															currency = inputPayload.getString("Currency");
														
														try {
															remarks = inputPayload.getString("Text");
														} catch (Exception e) {
															remarks = "";
														}
														//If Transaction Type will not Come
														JsonObject pgCatJsonResponse = new JsonObject();
														if (inputPayload.has("PGTransactionType") ) {
															if ( ! inputPayload.isNull("PGTransactionType") || inputPayload.getString("PGTransactionType").trim().length() > 0)
																isTxnTypeAvailable = true;
														}
														if(isTxnTypeAvailable)
															txnType = inputPayload.getString("PGTransactionType");
														else
														{
															executeURL ="";
															pgCatId = inputPayload.getString("PGCategoryID");
															executeURL = tokenURL+"PGPaymentCategories"+"?$filter=PGCategoryID%20eq%20%27"+pgCatId+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";//%20and%20CorpId%20eq%20%27"+corpId+"%27%20and%20userId%20eq%20%27"+userId+"%27";
															if (debug)
																response.getWriter().println("POSTPGPayments.executeURL4: "+executeURL);
															
															pgCatJsonResponse = commonUtils.executeURL(executeURL, userPass, response);
															if (debug)
																response.getWriter().println("POSTPGPayments.pgCatJsonResponse: "+pgCatJsonResponse);
															
															if( ! pgCatJsonResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("BankPaymentTransactionType").isJsonNull())
																txnType = pgCatJsonResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("BankPaymentTransactionType").getAsString();
														}
														
														if (debug){
															response.getWriter().println("POST.aggregatorID: "+aggregatorID);
															response.getWriter().println("POST.userRegId: "+userRegId);
															response.getWriter().println("POST.userId: "+userId);
															response.getWriter().println("POST.corpId: "+corpId);
															response.getWriter().println("POST.trackId: "+trackId);
															response.getWriter().println("POST.txnType: "+txnType);
															response.getWriter().println("POST.OTPDeliveryMode: "+"SMS");
															response.getWriter().println("POST.pgAmnt: "+pgAmnt);
															response.getWriter().println("POST.currency: "+currency);
															response.getWriter().println("POST.debitAcnNo: "+debitAcnNo);
															response.getWriter().println("POST.remarks: "+remarks);
														}
														
														//TODO calling OTP WebService
														OTPGeneratorClient otpGenerateClient = new OTPGeneratorClient();
														otpGenerateServiceMap = otpGenerateClient.callOTPGenerator(aggregatorID, userRegId, userId, corpId, trackId, txnType, "SMS", pgAmnt, currency, debitAcnNo, remarks);//OTPDeliveryMode
														if(debug)
															response.getWriter().println("PGPayments.otpGenerateServiceMap: "+otpGenerateServiceMap);
														
//														if ( status.equalsIgnoreCase("000001")) {
														if ( otpGenerateServiceMap.get("Status").equalsIgnoreCase("000001")) {
															
															response.setContentType("application/json");
															buildPostPymntResponse = commonUtils.buildPostPymntHeaderResponse(request, response, commonGuid, trackId, loginID, aggregatorID, insertPymntHeader, inputPayload, debug);
															response.getWriter().println(buildPostPymntResponse);
														} 
														else 
														{
															//TODO update PG_H table
															paymentTxnStatus =""; pgPymntPostingSts=""; paymentStatus="";
															String	pgTxnMsg="", pgBankRef="",pgTxnID="", pgTxnErrorCode="";
															JsonArray cofigTypesetJsonArray = new JsonArray();
															
															paymentTxnStatus = "000020";
															paymentStatus = "000110";//000110
															pgPymntPostingSts ="000300";
															
															cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PGTXST", paymentTxnStatus, oDataURL, aggregatorID, authParam, debug);
															 if(debug)
																 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgTxnStsDesc: "+cofigTypesetJsonArray);
															 
															 if (cofigTypesetJsonArray.size() >0)
																 pgTxnMsg = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
															 else 
																 pgTxnMsg ="";
															
															JsonObject updatePaymentHeader = new JsonObject();
															updatePaymentHeader = commonUtils.updatePaymentsHeader(request, response, aggregatorID, oDataURL, commonGuid, trackId, insertPymntHeader, paymentTxnStatus, paymentStatus, pgPymntPostingSts, pgTxnMsg, pgBankRef, pgTxnID, pgTxnErrorCode, debug);
															if (debug)
																response.getWriter().println("PGPayments.updatePaymentHeader: "+updatePaymentHeader);
															
															response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
															errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("079")+ otpGenerateServiceMap.get("Message").toString());
															errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "079");
															response.getWriter().println(errorResFormatForPGPayments);
														}
													} else
													{
														response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "054");
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("054"));//
														response.getWriter().println(errorResFormatForPGPayments);
													}
												} 
												else 
												{
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", "OTP not generated");
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "001");
													response.getWriter().println(errorResFormatForPGPayments);
												}
											}
										} else {

											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "145");
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("145"));
											response.getWriter().println(errorResFormatForPGPayments);
										}
									}
								}else{
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", "Unable to generate TrackID");
									errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "116");
									response.getWriter().println(errorResFormatForPGPayments);
								}
							 }else if (request.getMethod().equalsIgnoreCase("GET")) {
								 debug = false;
								 String queryString="", successResponse ="", pgHeaderGUID="",updatedQuery="";
								 executeURL="";

//								 need to maintain and append in the response.
								 String pymntStsDesc="", pgPymntPostingStsDesc="", advnceForDesc="", clrDocCatDesc="",pgCatgryDesc="", pymntForDesc="", invcTypeDesc="", pgTxnStsDesc="";
								 
								 if(debug){
									 response.getWriter().println("PGPyments GET");
									 response.getWriter().println("GET.loginID: "+loginID);
									 response.getWriter().println("GET.oDataURL: "+oDataURL);
									 response.getWriter().println("GET.aggregatorID: "+aggregatorID);
								 }
								 
//								 JsonObject pgPymntHeaderJsonObj = new JsonObject();
//								 JsonObject pgPymntHeaderJsonResponseUI = new JsonObject();
//								 Gson gson = new Gson();
								 JsonObject pymentHeaderObj = new JsonObject();
								 JsonObject httpGetResponse = new JsonObject();
								 queryString = request.getQueryString();
								 if (debug)
									 response.getWriter().println("GET.queryString: "+queryString);
								 //filter=PGPaymentGUID%20eq%20%27C74576C3-8A69-4C6D-B93D-5B400350C256%27
									 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"PGPayments?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a").replaceAll("-", "%2D");;
										 if (debug)
											 response.getWriter().println("filterQuery.1: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"PGPayments?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"PGPayments?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 
								 if(debug)
									 response.getWriter().println("GET.PGPayments.executeURL: "+executeURL);
								 
								 pymentHeaderObj = commonUtils.executeURL(executeURL, userPass, response);
								 if(debug){
									 response.getWriter().println("GET.PGPayments.pymentHeaderObj: "+pymentHeaderObj);
									 response.getWriter().println("GET.PGPayments.queryString: "+queryString.contains("PGPaymentGUID"));
									 response.getWriter().println("GET.PGPayments.pgHeaderGUID: "+pgHeaderGUID);
								 }
//								 if (! queryString.contains("PGPaymentGUID")) {
									 
//									 response.setContentType("application/json");
//									 response.getWriter().println(pymentHeaderObj);
//									 
//								 } else {
									 
								 if (pymentHeaderObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
									 
									 for (int i = 0; i < pymentHeaderObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
										
										 String pgTxnId="", pymntSts="", pgPymntPostingSts="", pgCat="";
										 JsonArray cofigTypesetJsonArray = new JsonArray();
										 
										 httpGetResponse = pymentHeaderObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
										 if(debug)
											 response.getWriter().println("GET.PGPayments.httpGetResponse: "+httpGetResponse);
										 
										 if  (!httpGetResponse.get("PGTxnStatusID").isJsonNull())
											 pgTxnId = httpGetResponse.get("PGTxnStatusID").getAsString();
										 
										 if  (!httpGetResponse.get("PaymentStatusID").isJsonNull())
											 pymntSts = httpGetResponse.get("PaymentStatusID").getAsString();
										 
										 if  (!httpGetResponse.get("PGPaymnetPostingStatusID").isJsonNull() )
											 pgPymntPostingSts = httpGetResponse.get("PGPaymnetPostingStatusID").getAsString();
										 
										 if  (!httpGetResponse.get("PGCategoryID").isJsonNull() )
											 pgCat = httpGetResponse.get("PGCategoryID").getAsString();
										 
										 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PGTXST", pgTxnId, oDataURL, aggregatorID, authParam, debug);
										 if(debug)
											 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgTxnStsDesc: "+cofigTypesetJsonArray);
										 
										 if (cofigTypesetJsonArray.size() >0)
											 pgTxnStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
										 else 
											 pgTxnStsDesc ="";
										 
										 cofigTypesetJsonArray = new JsonArray();
										 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYMTST", pymntSts, oDataURL, aggregatorID, authParam, debug);
										 if(debug)
											 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pymntStsDesc: "+cofigTypesetJsonArray);
										 
										 if (cofigTypesetJsonArray.size() >0)
											 pymntStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
										 else 
											 pymntStsDesc ="";
										 
										 cofigTypesetJsonArray = new JsonArray();
										 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYPSTS", pgPymntPostingSts, oDataURL, aggregatorID, authParam, debug);
										 if(debug)
											 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgPymntPostingStsDesc: "+cofigTypesetJsonArray);
										 
										 if (cofigTypesetJsonArray.size() >0)
											 pgPymntPostingStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
										 else 
											 pgPymntPostingStsDesc ="";
										 
										 cofigTypesetJsonArray = new JsonArray();
										 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PGCAT", pgCat, oDataURL, aggregatorID, authParam, debug);
										 if(debug)
											 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgCatgryDesc: "+cofigTypesetJsonArray);
										 
										 if (cofigTypesetJsonArray.size() >0)
											 pgCatgryDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
										 else 
											 pgCatgryDesc ="";
										 
										 httpGetResponse.addProperty("LoginID", loginID);
										 httpGetResponse.addProperty("ClearingText", "Posted Sucessfully with Document No "+httpGetResponse.get("ClearingDocNo").getAsString());
										 httpGetResponse.addProperty("CPName", "");
										 httpGetResponse.addProperty("PGCategoryID", pgCat);
										 httpGetResponse.addProperty("PGCategoryDesc", pgCatgryDesc);
										 httpGetResponse.addProperty("ClearingDocCompanyCodeDesc", "");
										 httpGetResponse.addProperty("PGTxnStatusDesc", pgTxnStsDesc);
										 httpGetResponse.addProperty("PaymentStatusID", pymntSts);
										 httpGetResponse.addProperty("PaymnetStatusDesc", pymntStsDesc);
										 httpGetResponse.addProperty("PGPaymnetPostingStatusID", pgPymntPostingSts);
										 httpGetResponse.addProperty("PGPaymentPostingStatusDesc", pgPymntPostingStsDesc);
										 httpGetResponse.addProperty("PGPaymentMethodDesc", "");
										 httpGetResponse.addProperty("PaymentTypeDesc", "");
										 httpGetResponse.addProperty("PGTxnStatusID", pgTxnId);
										 httpGetResponse.addProperty("PGRefID", "");
										 httpGetResponse.add("PGTxnTime", null);
										 httpGetResponse.addProperty("SourceDesc", "");
										 httpGetResponse.addProperty("AdvanceForDesc", "");
										 httpGetResponse.addProperty("OTPTransactionID", "");
										 httpGetResponse.addProperty("OTP", "");
										 httpGetResponse.addProperty("Text", "");
										 httpGetResponse.addProperty("PymntForDesc", "");
										 httpGetResponse.addProperty("ClrDocCatDesc", "");
										 httpGetResponse.addProperty("TestRun", "false");
										 httpGetResponse.addProperty("SourceID", "");
										 
									}
									 response.setContentType("application/json"); 
									 response.getWriter().println(pymentHeaderObj);
								 }
								 else
								 {
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", "Transaction not found");
									 errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "E112");
									 response.getWriter().println(errorResFormatForPGPayments);
								 }
//								 }
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/PGPaymentConfigs")) {
							 
							 debug= false; payloadRequest="";
								
							 // added for post
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("PGPaymentConfigs.inputPayload: "+inputPayload);
									 response.getWriter().println("PGPaymentConfigs.authParam: "+authParam);
									 response.getWriter().println("PGPaymentConfigs.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertPGPaymentConfigs(response, request, inputPayload, authParam, tokenURL, aggregatorID, properties, debug);
								 if(debug)
									 response.getWriter().println("PGPaymentConfigs.insertPGPymentResponse: "+insertPGPymentResponse);
								 
								 if (insertPGPymentResponse.has("ResponseCode") && ! insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {

									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertPGPymentResponse);
								} else {
									response.setContentType("application/json");
									response.getWriter().println(insertPGPymentResponse);
								}
							 }else{
								 String executeURL = "";
								 loginID = "";
								 loginID = commonUtils.getUserPrincipal(request, "name", response);
//								 response.getWriter().println("loginID: "+loginID);
//							   	 loginID = "P2000278306"; //Temporarily hard coded
								 
								 executeURL = oDataURL+"PGPaymentConfigs?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
//								 response.getOutputStream().println("executeURL: "+executeURL);
								 
								//  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//								 response.getWriter().println("loadMetadata httpClient: "+httpClient);
								 readRequest = new HttpGet(executeURL);
								 readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//								 response.getWriter().println("loadMetadata readRequest: "+readRequest);
								//  HttpResponse serviceResponse = httpClient.execute(readRequest);
								 HttpResponse serviceResponse = client.execute(readRequest);
								 countEntity = serviceResponse.getEntity();
//								 sresponse.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//								 response.getWriter().println("loadMetadata countEntity: "+countEntity);
//								 response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
								 
								 if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									 String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									 if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										 response.setContentType(contentType);
										 response.getOutputStream().print(EntityUtils.toString(countEntity));
									 }else{
										 response.setContentType(contentType);
										 String Data = EntityUtils.toString(countEntity);
										 response.getWriter().println(Data);
									 }
								 }else
								 {
									 response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									 response.getWriter().println("loadMetadata countEntity: "+countEntity);
								 }
							 }
						 } else if (request.getPathInfo().equalsIgnoreCase("/ConfigTypesetTypes")) {
							 
							 String executeURL = "", queryString="";
							 loginID = "";
							 loginID = commonUtils.getUserPrincipal(request, "name", response);
							 
							 try {
								 queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							 
							 if (queryString.trim().length() > 0)
								 executeURL = oDataURL+"ConfigTypesetTypes?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							else
								executeURL = oDataURL+"ConfigTypesetTypes?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27"; 
							
							//  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//							 response.getWriter().println("loadMetadata httpClient: "+httpClient);
							 readRequest = new HttpGet(executeURL);
							 readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//							 response.getWriter().println("loadMetadata readRequest: "+readRequest);
							//  HttpResponse serviceResponse = httpClient.execute(readRequest);
							HttpResponse serviceResponse = client.execute(readRequest);
							 countEntity = serviceResponse.getEntity();
//							 sresponse.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//							 response.getWriter().println("loadMetadata countEntity: "+countEntity);
//							 response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
							
							 if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
								 String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
								 if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
									 response.setContentType(contentType);
									 response.getOutputStream().print(EntityUtils.toString(countEntity));
								 }else{
									 response.setContentType(contentType);
									 String Data = EntityUtils.toString(countEntity);
									 response.getWriter().println(Data);
								 }
							 }else
							 {
								 response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
								 response.getWriter().println("loadMetadata countEntity: "+countEntity);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/ConfigTypsetTypeValues")) {
							 debug = false;
							 String executeURL = "", queryString="";
							 loginID = "";
							 loginID = commonUtils.getUserPrincipal(request, "name", response);
							 
							 try {
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 
							 if(debug)
								 response.getWriter().println("ConfigTypsetTypeValues.queryString: "+queryString);
							 
							 String encodedURL="", decodedURL="", reformedQuery="";
//							 encodedURL = URLEncoder.encode(queryString, "UTF-8");
							 decodedURL = URLDecoder.decode(queryString, "UTF-8");
							 if(debug){
								 response.getWriter().println("ConfigTypsetTypeValues.encodedURL: "+encodedURL);
								 response.getWriter().println("ConfigTypsetTypeValues.decodedURL: "+decodedURL);
							 }
							 
							 reformedQuery = decodedURL+" and AggregatorID eq '"+aggregatorID+"'";
							 encodedURL = URLEncoder.encode(reformedQuery, "UTF-8");
							 
							 if (queryString.trim().length() > 0)
								 executeURL = oDataURL+"ConfigTypsetTypeValues?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
//								 executeURL = oDataURL+"ConfigTypsetTypeValues?"+encodedURL;
							 else
								 executeURL = oDataURL+"ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27"; 
							
							 if(debug)
								 response.getWriter().println("ConfigTypsetTypeValues.executeURL: "+executeURL);
							
							//  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//							 response.getWriter().println("loadMetadata httpClient: "+httpClient);
							 readRequest = new HttpGet(executeURL);
							 readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//							 response.getWriter().println("loadMetadata readRequest: "+readRequest);
							//  HttpResponse serviceResponse = httpClient.execute(readRequest);
							 HttpResponse serviceResponse = client.execute(readRequest);
							 countEntity = serviceResponse.getEntity();
//							 sresponse.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//							 response.getWriter().println("loadMetadata countEntity: "+countEntity);
//							 response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
							
							 if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
								 String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
								 if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
									 response.setContentType(contentType);
									 response.getOutputStream().print(EntityUtils.toString(countEntity));
								 }else{
									 response.setContentType(contentType);
									 String Data = EntityUtils.toString(countEntity);
									 response.getWriter().println(Data);
								 }
							 }else
							 {
								 response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
								 response.getWriter().println("loadMetadata countEntity: "+countEntity);
							 }
						 }
						 else if ( request.getPathInfo().equalsIgnoreCase("/PG_H")) {
							 debug= false;
							 JsonObject httpGetResponse = new JsonObject();
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"PG_H?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"PG_H?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"PG_H?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if ( request.getPathInfo().equalsIgnoreCase("/PG_I")  ) {
							 debug= false;
							 JsonObject httpGetResponse = new JsonObject();
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"PG_I?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"PG_I?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"PG_I?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/PGPaymentCategories")) { // PGPaymentCategories
							 
							 debug= false; payloadRequest="";
								
							 // added for post by kamlesh on 23-05-2020
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("PGPaymentCategories.inputPayload: "+inputPayload);
									 response.getWriter().println("PGPaymentCategories.authParam: "+authParam);
									 response.getWriter().println("PGPaymentCategories.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertPGPaymentCategories(response, request, inputPayload, authParam, tokenURL, aggregatorID, properties, debug);
								 if (insertPGPymentResponse.has("ResponseCode") && insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									
									 response.setContentType("application/json");
									 response.getWriter().println(insertPGPymentResponse);
								} else {
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().println(insertPGPymentResponse);
								}
								 
							 }else if(request.getMethod().equalsIgnoreCase("GET")){
								 
								 JsonObject httpGetResponse = new JsonObject();
								 String queryString ="", executeURL="", updatedQuery="";
								 try {        
									 queryString = request.getQueryString();
								 } catch (Exception e) {
									 queryString ="";
								 }
								 if (debug)
									 response.getWriter().println("queryString: "+queryString);
								 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"PGPaymentCategories?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"PGPaymentCategories?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"PGPaymentCategories?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 //New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 }
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/PGPaymentConfigStats")) { // PGPaymentConfigStats
							 
							 debug= false; payloadRequest="";
								
							 // added for post by kamlesh on 23-05-2020
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("PGPaymentConfigStats.inputPayload: "+inputPayload);
									 response.getWriter().println("PGPaymentConfigStats.authParam: "+authParam);
									 response.getWriter().println("PGPaymentConfigStats.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertPGPaymentConfigStats(response, request, inputPayload, authParam, tokenURL, aggregatorID, properties, debug);
								 if (insertPGPymentResponse.has("ResponseCode") && ! insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									 //failure
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertPGPymentResponse);
									 
								 } else {
									 //success
									 response.setContentType("application/json");
									 response.getWriter().println(insertPGPymentResponse);
								 }
								 
							 }else if(request.getMethod().equalsIgnoreCase("GET")){

								 String queryString ="", executeURL="", updatedQuery="";
								 JsonObject httpGetResponse = new JsonObject();
								 try {        
									 queryString = request.getQueryString();
								 } catch (Exception e) {
									 queryString ="";
								 }
								 if (debug)
									 response.getWriter().println("queryString: "+queryString);
								 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"PGPaymentConfigStats?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"PGPaymentConfigStats?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"PGPaymentConfigStats?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 //New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 }
							 }							 
						 }else if (request.getPathInfo().equalsIgnoreCase("/PGPaymentItemDetails")) { // PGPaymentItemDetails
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"PGPaymentItemDetails?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"PGPaymentItemDetails?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"PGPaymentItemDetails?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) 
							 {
								 response.getWriter().println(httpGetResponse);
							 } else
							 {
								 String pymntSts="",pgPymntPostingSts="", pymntStsDesc="", pgPymntPostingStsDesc="";
								 JsonArray cofigTypesetJsonArray = new JsonArray();
								 JsonObject childPGPaymentsItems = new JsonObject();
								 
								 for (int i = 0; i < httpGetResponse.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
									 
									 childPGPaymentsItems = httpGetResponse.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
									 if  (!childPGPaymentsItems.get("PaymentStatusID").isJsonNull())
										 pymntSts = childPGPaymentsItems.get("PaymentStatusID").getAsString();
									 
									 if  (!childPGPaymentsItems.get("PaymentPostingStatusID").isJsonNull() )
										 pgPymntPostingSts = childPGPaymentsItems.get("PaymentPostingStatusID").getAsString();
									 
									 cofigTypesetJsonArray = new JsonArray();
									 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYMTST", pymntSts, oDataURL, aggregatorID, authParam, debug);
									 if(debug)
										 response.getWriter().println("PGPaymentItemDetails.cofigTypesetJsonArray: "+cofigTypesetJsonArray);
									 
									 if (cofigTypesetJsonArray.size() >0)
										 pymntStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
									 else 
										 pymntStsDesc ="";
									 
									 cofigTypesetJsonArray = new JsonArray();
									 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYPSTS", pgPymntPostingSts, oDataURL, aggregatorID, authParam, debug);
									 if(debug)
										 response.getWriter().println("PGPaymentItemDetails.cofigTypesetJsonArray"+cofigTypesetJsonArray);
									 
									 if (cofigTypesetJsonArray.size() >0)
										 pgPymntPostingStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
									 else 
										 pgPymntPostingStsDesc ="";
									 
									 childPGPaymentsItems.addProperty("PaymentStatusID", pymntSts);
									 childPGPaymentsItems.addProperty("PaymentStatusDesc", pymntStsDesc);
									 childPGPaymentsItems.addProperty("PaymentPostingStatusID", pgPymntPostingSts);
									 childPGPaymentsItems.addProperty("PaymentPostingStatusDesc", pgPymntPostingStsDesc);
									 childPGPaymentsItems.addProperty("Division", "");
									 childPGPaymentsItems.addProperty("DeductionReason", "");
									 childPGPaymentsItems.addProperty("DeductionAmount", "");
									 childPGPaymentsItems.addProperty("WCTAmount", "");
									 childPGPaymentsItems.addProperty("LoginID", loginID);
								 }
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/ALOG_H")) { // ALOG_H
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"ALOG_H?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"ALOG_H?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"ALOG_H?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/ApplicationLogs")) { // ApplicationLogs
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"ApplicationLogs?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"ApplicationLogs?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"ApplicationLogs?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/ALOG_M")) { // ALOG_M
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"ALOG_M";
							 } else {
								 executeURL = oDataURL+"ALOG_M?"+queryString;
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }
						 else if (request.getPathInfo().equalsIgnoreCase("/ApplicationLogMessages")) { // ApplicationLogMessages
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"ApplicationLogMessages"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"ApplicationLogMessages?"+queryString;
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/DOCREP_CNFG")) { // DOCREP_CNFG
							 
							 debug= false;
							 JsonObject httpGetResponse = new JsonObject();
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"DOCREP_CNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"DOCREP_CNFG?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"DOCREP_CNFG?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/DocumentRepConfigs")) { // DocumentRepConfigs
							 
							 debug= false; payloadRequest="";
								
							 // added for post by kamles
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("DocumentRepConfigs.inputPayload: "+inputPayload);
									 response.getWriter().println("DocumentRepConfigs.authParam: "+authParam);
									 response.getWriter().println("DocumentRepConfigs.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertDocumentRepConfigs(response, request, inputPayload, authParam, oDataURL, aggregatorID, properties, debug);
								 if (insertPGPymentResponse.has("ResponseCode") && ! insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertPGPymentResponse);
								} else {
									response.setContentType("application/json");
									response.getWriter().println(insertPGPymentResponse);
								}
							 }
							 else{
								 String queryString ="", executeURL="", updatedQuery="";
								 JsonObject httpGetResponse = new JsonObject();
								 try {        
									 queryString = request.getQueryString();
								 } catch (Exception e) {
									 queryString ="";
								 }
								 if (debug)
									 response.getWriter().println("queryString: "+queryString);
								 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"DocumentRepConfigs?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"DocumentRepConfigs?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"DocumentRepConfigs?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 } 
								 
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/DOCREP")) { // DOCREP
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"DOCREP?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"DOCREP?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"DOCREP?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/DocumentRepository")) { // DocumentRepository
							 
							 debug= false; payloadRequest="";
							 // added for post by kamlesh
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("DocumentRepConfigs.inputPayload: "+inputPayload);
									 response.getWriter().println("DocumentRepConfigs.authParam: "+authParam);
									 response.getWriter().println("DocumentRepConfigs.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertDocumentRepository(response, request, inputPayload, authParam, oDataURL, aggregatorID, properties, debug);
								 if (insertPGPymentResponse.has("ResponseCode") && ! insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									 
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertPGPymentResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(insertPGPymentResponse);
								 }
							 }
							 else{
								 String queryString ="", executeURL="", updatedQuery="";
								 JsonObject httpGetResponse = new JsonObject();
								 try {        
									 queryString = request.getQueryString();
								 } catch (Exception e) {
									 queryString ="";
								 }
								 if (debug)
									 response.getWriter().println("queryString: "+queryString);
								 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"DocumentRepository?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"DocumentRepository?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"DocumentRepository?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 }
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/EVENT_DEST")) { // EVENT_DEST
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"EVENT_DEST"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"EVENT_DEST?"+queryString;//+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/EventDestinations")) { // EventDestinations
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"EventDestinations"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"EventDestinations?"+queryString;//+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/MIS_SCHEDULER")) { // MIS_SCHEDULER
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"MIS_SCHEDULER?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 if (queryString.contains("$filter")) {
									 String filterQuery = request.getParameter("$filter");
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									 if (debug)
										 response.getWriter().println("filterQuery: "+filterQuery);
									 
									 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										 updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									 }
									 executeURL = oDataURL+"MIS_SCHEDULER?"+updatedQuery;
								 } else {
									 executeURL = oDataURL+"MIS_SCHEDULER?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								 } 
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }
						 else if (request.getPathInfo().equalsIgnoreCase("/MISScheduler")) { // MISScheduler
							 
							 debug = false;
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("MISScheduler.inputPayload: "+inputPayload);
									 response.getWriter().println("MISScheduler.authParam: "+authParam);
									 response.getWriter().println("MISScheduler.tokenURL: "+tokenURL);
								 }
								 JsonObject insertMISSchedulerResponse = new JsonObject();
								 insertMISSchedulerResponse = channelFinanceOps.insertMISScheduler(response, request, inputPayload, authParam, oDataURL, aggregatorID, properties, debug);
								 if(debug)
									 response.getWriter().println("MISScheduler.insertMISSchedulerResponse: "+insertMISSchedulerResponse);
									 
								 if (insertMISSchedulerResponse.has("ResponseCode") && ! insertMISSchedulerResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									 //failure
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertMISSchedulerResponse);
									 
								 } else {
									 //success
									 response.setContentType("application/json");
									 response.getWriter().println(insertMISSchedulerResponse);
								 }
							 }else{
								 
								 String queryString ="", executeURL="", updatedQuery="";
								 try {        
									 queryString = request.getQueryString();
								 } catch (Exception e) {
									 queryString ="";
								 }
								 if (debug)
									 response.getWriter().println("queryString: "+queryString);
								 
								 if ( queryString == null || queryString == "" ) {
									 executeURL = oDataURL+"MISScheduler?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 } else {
									 if (queryString.contains("$filter")) {
										 String filterQuery = request.getParameter("$filter");
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 
										 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
										 if (debug)
											 response.getWriter().println("filterQuery: "+filterQuery);
										 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
											 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } else {
											 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 }
										 executeURL = oDataURL+"MISScheduler?"+updatedQuery;
									 } else {
										 executeURL = oDataURL+"MISScheduler?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } 
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 // New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
								 JsonObject httpGetResponse = new JsonObject();
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 } 
							}
						 }else if (request.getPathInfo().equalsIgnoreCase("/H2H_ACF")) { // H2H_ACF
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 JsonObject httpGetResponse = new JsonObject();
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"H2H_ACF"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"H2H_ACF?"+queryString; //+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/H2HApplicationConfigs")) { // H2HApplicationConfigs
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"H2HApplicationConfigs"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"H2HApplicationConfigs?"+queryString; //+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 JsonObject httpGetResponse = new JsonObject();
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/H2H_ICF")) { // H2H_ICF
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"H2H_ICF"; //?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"H2H_ICF?"+queryString; //+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 // New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 JsonObject httpGetResponse = new JsonObject();
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/H2HInterfaceConfigs")) { // H2HInterfaceConfigs
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"H2HInterfaceConfigs"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"H2HInterfaceConfigs?"+queryString; //+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 //New Changes for Test Case 4 and 5 done by Arif Shaik on 24-04-2020
							 JsonObject httpGetResponse = new JsonObject();
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 } 
						 }else if (request.getPathInfo().equalsIgnoreCase("/ValueHelps")) {
							 
							 debug= false; payloadRequest="";
								
							 //add GET call along with POST - GET used in Repayments
							 // added for POST Http by kamlesh on 03-06-2020
							 if(request.getMethod().equalsIgnoreCase("POST")){
								 
								 JSONObject inputPayload = null;
								 payloadRequest = getGetBody(request, response);
								 try {
									 inputPayload = new JSONObject(payloadRequest);
									 if (inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
											 inputPayload.getBoolean("debug") == true) 
										 debug = true;
								 } catch (Exception e) {
									 debug = false;
								 }
								 if(debug){
									 response.getWriter().println("ValueHelps.inputPayload: "+inputPayload);
									 response.getWriter().println("ValueHelps.authParam: "+authParam);
									 response.getWriter().println("ValueHelps.tokenURL: "+tokenURL);
								 }
								 JsonObject insertPGPymentResponse = new JsonObject();
								 insertPGPymentResponse = channelFinanceOps.insertValueHelpsInsert(response, request, inputPayload, authParam, oDataURL, aggregatorID, properties, debug);
								 if (insertPGPymentResponse.has("ResponseCode") && ! insertPGPymentResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
									 //failure
									 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									 response.getWriter().println(insertPGPymentResponse);
									 
								 } else {
									 //success
									 response.setContentType("application/json");
									 response.getWriter().println(insertPGPymentResponse);
								 }
							 }else{
								 
								 //TODO GET pass through used in Repayments mandatory part 
								 String executeURL = "", queryString="";
								 JsonObject httpGetResponse = new JsonObject();
								 queryString = request.getQueryString();
//								 response.getWriter().println("Aggr: "+ aggregatorID);
								 
								 if(null != queryString && queryString.trim().length() > 0){
									 
									 queryString = queryString.replaceAll("%27and", "%27%20and");
									 queryString = queryString.replaceAll("%27AND", "%27%20AND");
									 queryString = queryString.replaceAll("%27OR", "%27%20OR");
									 
									 executeURL = oDataURL+"ValueHelps?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 }else{
									 executeURL = oDataURL+"ValueHelps?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								 }
								 if (debug)
									 response.getWriter().println("executeURL: "+executeURL);
								 // New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								 
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json");
									 response.getWriter().println(httpGetResponse);
								 }
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/MSG_LOG")) {
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"MSG_LOG"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"MSG_LOG?"+queryString; //+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 JsonObject httpGetResponse = new JsonObject();
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else if (request.getPathInfo().equalsIgnoreCase("/CPIMessageLogs")) {
							 
							 debug= false;
							 String queryString ="", executeURL="", updatedQuery="";
							 try {        
								 queryString = request.getQueryString();
							 } catch (Exception e) {
								 queryString ="";
							 }
							 if (debug)
								 response.getWriter().println("queryString: "+queryString);
							 
							 if ( queryString == null || queryString == "" ) {
								 executeURL = oDataURL+"CPIMessageLogs"; //?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 } else {
								 executeURL = oDataURL+"CPIMessageLogs?"+queryString; //+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							 }
							 if (debug)
								 response.getWriter().println("executeURL: "+executeURL);
							 JsonObject httpGetResponse = new JsonObject();
							 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getOutputStream().println("httpGetResponse: "+httpGetResponse);
							 if ( httpGetResponse.has("error") ) {
								 response.getWriter().println(httpGetResponse);
							 } else {
								 response.setContentType("application/json");
								 response.getWriter().println(httpGetResponse);
							 }
						 }else{
							 String pathInfo ="", executeURL ="", errorResponse ="" ;
							 JsonObject httpGetResponse = new JsonObject();
							 errorResponse = "{ \"error\": { \"code\": \"\", \"message\": { \"lang\": \"en-US\", \"value\": \"Resource not found.\"}}}";
							 pathInfo = request.getPathInfo(); 
//							 response.getWriter().println("pathInfo: "+pathInfo);
							 
							 if (pathInfo.contains("PG_H(")) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json");
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("PG_I(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json");
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("PGPayments(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 pathInfo = pathInfo.replace("/", "").replace("guid", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);

								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
									
											 String pymntStsDesc="", pgPymntPostingStsDesc="",pgCatgryDesc="", pgTxnStsDesc="", pymntForDesc="";
											 String pgTxnId="", pymntSts="", pgPymntPostingSts="", pgCat="", pymntFor="";
											 JsonArray cofigTypesetJsonArray = new JsonArray();
											 
											 if  (!httpGetResponse.getAsJsonObject("d").get("PGTxnStatusID").isJsonNull())
												 pgTxnId = httpGetResponse.getAsJsonObject("d").get("PGTxnStatusID").getAsString();
											 
											 if  (!httpGetResponse.getAsJsonObject("d").get("PaymentStatusID").isJsonNull())
												 pymntSts = httpGetResponse.getAsJsonObject("d").get("PaymentStatusID").getAsString();
											 
											 if  (!httpGetResponse.getAsJsonObject("d").get("PGPaymnetPostingStatusID").isJsonNull())
												 pgPymntPostingSts = httpGetResponse.getAsJsonObject("d").get("PGPaymnetPostingStatusID").getAsString();
											 
											 if  (!httpGetResponse.getAsJsonObject("d").get("PGCategoryID").isJsonNull())
												 pgCat = httpGetResponse.getAsJsonObject("d").get("PGCategoryID").getAsString();
											 
											 if  (!httpGetResponse.getAsJsonObject("d").get("PymntFor").isJsonNull())
												 pymntFor = httpGetResponse.getAsJsonObject("d").get("PymntFor").getAsString();
											 
											 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PGTXST", pgTxnId, oDataURL, aggregatorID, authParam, debug);
											 if(debug)
												 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgTxnStsDesc: "+cofigTypesetJsonArray);
											 
											 if (cofigTypesetJsonArray.size() >0)
												 pgTxnStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
											 else 
												 pgTxnStsDesc ="";
											 
											 cofigTypesetJsonArray = new JsonArray();
											 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYMTST", pymntSts, oDataURL, aggregatorID, authParam, debug);
											 if(debug)
												 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pymntStsDesc: "+cofigTypesetJsonArray);
											 
											 if (cofigTypesetJsonArray.size() >0)
												 pymntStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
											 else 
												 pymntStsDesc ="";
											 
											 cofigTypesetJsonArray = new JsonArray();
											 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PYPSTS", pgPymntPostingSts, oDataURL, aggregatorID, authParam, debug);
											 if(debug)
												 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgPymntPostingStsDesc: "+cofigTypesetJsonArray);
											 
											 if (cofigTypesetJsonArray.size() >0)
												 pgPymntPostingStsDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
											 else 
												 pgPymntPostingStsDesc ="";
											 
											 cofigTypesetJsonArray = new JsonArray();
											 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PGCAT", pgCat, oDataURL, aggregatorID, authParam, debug);
											 if(debug)
												 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pgCatgryDesc: "+cofigTypesetJsonArray);
											 
											 if (cofigTypesetJsonArray.size() >0)
												 pgCatgryDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
											 else 
												 pgCatgryDesc ="";
											 
											 cofigTypesetJsonArray = new JsonArray();
											 cofigTypesetJsonArray = commonUtils.getRepaymentsDescriptionByTypeset(request, response, "PAYFOR", pymntFor, oDataURL, aggregatorID, authParam, debug);
											 if(debug)
												 response.getWriter().println("GET.PGPayments.cofigTypesetJsonArray.pymntStsDesc: "+cofigTypesetJsonArray);
											 
											 if (cofigTypesetJsonArray.size() >0)
												 pymntForDesc = cofigTypesetJsonArray.get(0).getAsJsonObject().get("TypesName").getAsString();
											 else 
												 pymntForDesc ="";
											 
											 httpGetResponse.getAsJsonObject("d").addProperty("ClearingText", "Posted Sucessfully with Document No "+httpGetResponse.getAsJsonObject("d").get("ClearingDocNo").getAsString());
											 httpGetResponse.getAsJsonObject("d").addProperty("AdvanceForDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("CPName", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("PGCategoryID", pgCat);
											 httpGetResponse.getAsJsonObject("d").addProperty("PGCategoryDesc", pgCatgryDesc);
											 httpGetResponse.getAsJsonObject("d").addProperty("ClearingDocCompanyCodeDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("PGPaymentMethodDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("PaymentTypeDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("PGTxnStatusID", pgTxnId);
											 httpGetResponse.getAsJsonObject("d").addProperty("PGTxnStatusDesc", pgTxnStsDesc);
											 httpGetResponse.getAsJsonObject("d").addProperty("PaymentStatusID", pymntSts);
											 httpGetResponse.getAsJsonObject("d").addProperty("PaymnetStatusDesc", pymntStsDesc);
											 httpGetResponse.getAsJsonObject("d").addProperty("PGPaymnetPostingStatusID", pgPymntPostingSts);
											 httpGetResponse.getAsJsonObject("d").addProperty("PGPaymentPostingStatusDesc", pgPymntPostingStsDesc);
											 httpGetResponse.getAsJsonObject("d").addProperty("PGRefID", "");
											 httpGetResponse.getAsJsonObject("d").add("PGTxnTime", null);
											 httpGetResponse.getAsJsonObject("d").addProperty("SourceDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("OTPTransactionID", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("OTP", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("Text", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("PymntForDesc", pymntForDesc);
											 httpGetResponse.getAsJsonObject("d").addProperty("ClrDocCatDesc", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("TestRun", "false");
											 httpGetResponse.getAsJsonObject("d").addProperty("SourceID", "");
											 httpGetResponse.getAsJsonObject("d").addProperty("LoginID", loginID);
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("PGPaymentCategories(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("PGPaymentConfigStats(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("PGPaymentItemDetails(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("ALOG_H(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("ApplicationLogs(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("ALOG_M(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								}
							 }else if ( pathInfo.contains("ApplicationLogMessages(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								}  //ended on 23-04-2020
							 }else if ( pathInfo.contains("DOCREP_CNFG(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("DocumentRepConfigs(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("DOCREP(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("DocumentRepository(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("EVENT_DEST(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }
							 }else if ( pathInfo.contains("EventDestinations(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }
							 }else if ( pathInfo.contains("MIS_SCHEDULER(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("MISScheduler(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								    response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									 {
										 responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										 if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											 
											 response.setContentType("application/json"); 
											 response.getWriter().println(httpGetResponse);
										 }
										 else
											 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									 }
									 else
										 response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								 }
							 }else if ( pathInfo.contains("H2H_ACF(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }		// TODO: ended on 24-04-2020						
							 }
							 else if ( pathInfo.contains("H2HApplicationConfigs(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//			                        response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }
							 }else if ( pathInfo.contains("H2H_ICF(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								}
							 }else if ( pathInfo.contains("H2HInterfaceConfigs(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//									response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }		// TODO: ended on 24-04-2020						
							 }else if ( pathInfo.contains("MSG_LOG(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }					
							 }else if ( pathInfo.contains("CPIMessageLogs(") ) {
								 executeURL ="";
								 String responseAGGRID ="";
								 
								 pathInfo = pathInfo.replace("/", "");
								 executeURL = oDataURL+ pathInfo;
//								 response.getWriter().println("executeURL: "+executeURL);
								 httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								 response.getWriter().println("httpGetResponse: "+httpGetResponse);
								 if ( httpGetResponse.has("error") ) {
									 response.getWriter().println(httpGetResponse);
								 } else {
									 response.setContentType("application/json"); 
									 response.getWriter().println(httpGetResponse);
								 }						
							 }
							 //TODO else part
							 else {
								 String queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
								 queryString = queryString.replaceAll("%27and", "%27%20and");
								 queryString = queryString.replaceAll("%27AND", "%27%20AND");
								 queryString = queryString.replaceAll("%27OR", "%27%20OR");
//								response.getWriter().println("queryString: "+queryString);
//								response.getWriter().println("loadMetadata filter: "+request.getParameter("$filter"));
								 if(null != queryString && queryString.trim().length()>0){
									 String entityInfo  = request.getPathInfo().replace("/", "");
//									 response.getWriter().println("loadMetadata entityInfo: "+entityInfo);
									 if(null != entityInfo && entityInfo.trim().length()>0){
										 executeURL = oDataURL+entityInfo+"?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
//										 httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
										//  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//										 response.getWriter().println("loadMetadata httpClient: "+httpClient);
										 readRequest = new HttpGet(executeURL);
										 readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//										 response.getWriter().println("loadMetadata readRequest: "+readRequest);
										//  HttpResponse serviceResponse = httpClient.execute(readRequest);
										HttpResponse serviceResponse = client.execute(readRequest);
										 countEntity = serviceResponse.getEntity();
										 
										 if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
											 String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
											 if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
												 response.setContentType(contentType);
												 response.getOutputStream().print(EntityUtils.toString(countEntity));
											 }else{
												 response.setContentType(contentType);
												 String Data = EntityUtils.toString(countEntity);
												 response.getOutputStream().print(Data);	
											 }
										 }else{
											 response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
											 response.getWriter().println("loadMetadata countEntity: "+countEntity);
										 }
									 }else{
										 response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
										 response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
										 response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
										 response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
										 response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
										 response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
										 response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
										 response.getWriter().println("Unable to read the Entity Type");
									 }
								 }else{
									 response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
									 response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
									 response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
									 response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
									 response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
									 response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
									 response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
									 response.getWriter().println("No query received in the request");
								 }
							}
						 }
					}
			}else{
				
				/*URL urlObj = new URL(serviceURL);
				HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Authorization",basicAuth);
				connection.setDoInput(true);*/
				debug = false;
				
				serviceURL = serviceURL.replaceAll("%20", " ");
				if(debug)
					response.getWriter().println("serviceURL: "+serviceURL);
//				httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
				// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
				readRequest = new HttpGet(serviceURL);
				
				// HttpResponse serviceResponse = httpClient.execute(readRequest);
				HttpResponse serviceResponse = client.execute(readRequest);
				countEntity = serviceResponse.getEntity();
				
				if(debug)
					response.getWriter().println("getContentType: "+serviceResponse.getEntity().getContentType());
				
				if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
					String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
					if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
						response.setContentType(contentType);
						response.getOutputStream().print(EntityUtils.toString(countEntity));
					}else{
						response.setContentType(contentType);
						String Data = EntityUtils.toString(countEntity);
						response.getOutputStream().print(Data);	
					}
				}else{
					response.setContentType("application/pdf");
					response.getOutputStream().print(EntityUtils.toString(countEntity));
				}
			}
		}catch (Exception e) {
			response.getWriter().println("Exception: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Stack Trace: "+buffer.toString());
		}finally{
			// httpClient.close();
		}
	}
	//added method
	public String getGetBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }
		body = jb.toString();
//					  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}
	/*public String fetchCSRFToken(HttpServletRequest request, HttpServletResponse response, String serviceURL) {
		String csrfToken="";
		HttpGet readRequest = null;
		try{
			HttpDestination destination = getHTTPDestination(request, response, "PCGWHANA");
			HttpClient httpClient = destination.createHttpClient();
			
			readRequest = new HttpGet(serviceURL);
			readRequest.setHeader("X-CSRF-Token", "fetch");
			
			HttpResponse serviceResponse = httpClient.execute(readRequest);
			
			Header[] headers = serviceResponse.getAllHeaders();
			for (Header header : headers) {
				response.getWriter().println("fetchCSRFToken - Key : " + header.getName() 
				      + " ,Value : " + header.getValue());
				
				if(header.getName().equalsIgnoreCase("x-csrf-token")){
					csrfToken = header.getValue();
				}
			}
			
			if(null == csrfToken){
				csrfToken = "E174";
			}else if(csrfToken != null && csrfToken.trim().length() == 0){
				csrfToken = "E174";
			}
			
			countEntity = serviceResponse.getEntity();
			
			if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
				csrfToken = serviceResponse.getHeaders("x-csrf-token").toString();
			}else{
				csrfToken = "No response received from the HANA system";
			}
		}catch (Exception e) {
			csrfToken = "E175";
		}
		
		return csrfToken;
	}*/
	
	public String getBody(HttpServletRequest request) throws IOException {
		String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
	
	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		String destinationName = "";
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = "pygwmeta";
		}
		
		HttpDestination destination = null;
		try {
			//response.getWriter().println("destinationName: " +  destinationName);
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
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
		}
		return destination;
	}*/
	private String replaceAccountNumber(String AccountNo)
	{
		String modifiedAccnNo ="";
		try {
			if (AccountNo.length() > 5 )
			{
				modifiedAccnNo = AccountNo.substring ( 0, AccountNo.length() - 4 );
				for (int i = 1; i <= 4 ; i++){
					modifiedAccnNo = "*"+ modifiedAccnNo;
				}
			}
		} catch (Exception e) {
			return modifiedAccnNo;
		}
		return modifiedAccnNo;
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		CommonUtils commonUtils = new CommonUtils();
		ChannelFinanceOps channelFinanceOps = new ChannelFinanceOps();
		boolean debug = false;
		String payloadRequest = "", aggregatorID="", loginID="", oDataURL="", pathInfo="";
		
		try {
			JsonObject httpDeleteResponse = new JsonObject();
			JSONObject inputPayload = new JSONObject();
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			String userName="",password="",userPass="";

			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			userPass = userName+":"+password;
			try {
				payloadRequest = getGetBody(request, response);
				inputPayload = new JSONObject(payloadRequest);
				if (! inputPayload.isNull("debug") && inputPayload.getString("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
			} catch (Exception e) {
				debug = false;
//				debug = true;
			}
			
			pathInfo = request.getPathInfo().toString();
			pathInfo = pathInfo.substring(1, pathInfo.indexOf('('));
			if(debug){
				response.getWriter().println("doDelete.inputPayload: "+inputPayload);
				response.getWriter().println("doDelete.payloadRequest: "+payloadRequest);
				response.getWriter().println("doDelete.aggregatorID: "+aggregatorID);
				response.getWriter().println("doDelete.loginID: "+loginID);
				response.getWriter().println("doDelete.oDataURL: "+oDataURL);
				response.getWriter().println("doDelete.pathInfo: "+pathInfo);
				response.getWriter().println("doDelete.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doDelete.request.getPathInfo(): "+request.getPathInfo());
			}
			
			if (pathInfo.equalsIgnoreCase("PGPaymentCategories")) {
				
				channelFinanceOps.deletePGPaymentCategories(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				 
			}
			else if(pathInfo.equalsIgnoreCase("PGPaymentConfigs")) {
				
				channelFinanceOps.deletePGPaymentConfigs(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				 
			}else if(pathInfo.equalsIgnoreCase("PGPaymentConfigStats")) {
				
				httpDeleteResponse = channelFinanceOps.deletePGPaymentConfigStats(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				if (httpDeleteResponse.has("ResponseCode") && httpDeleteResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpDeleteResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpDeleteResponse);
				}
				 
			}else if(pathInfo.equalsIgnoreCase("ValueHelps")) {
				
				httpDeleteResponse = channelFinanceOps.deleteValueHelps(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				if (httpDeleteResponse.has("ResponseCode") && httpDeleteResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {

					//Success
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpDeleteResponse);
					
				} else
				{
					//failure
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpDeleteResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("DocumentRepConfigs")) {
				
				httpDeleteResponse = channelFinanceOps.deletDocumentRepConfigs(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				if (httpDeleteResponse.has("ResponseCode") && httpDeleteResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {

					//Success
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpDeleteResponse);
					
				} else
				{
					//failure
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpDeleteResponse);
				}
			}else if(pathInfo.equalsIgnoreCase("MISScheduler")) {
				
				JsonObject httpUpdateResponse = channelFinanceOps.deleteMISScheduler(response, request, inputPayload, userPass, oDataURL, pathInfo, properties, debug);
				if (httpUpdateResponse.has("ResponseCode") && httpUpdateResponse.get("ResponseCode").getAsString().equalsIgnoreCase("000001")) {
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(httpUpdateResponse);
					
				} else
				{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(httpUpdateResponse);
				}
			}else
			{
				response.getWriter().println("doDelete.outside");
				response.getWriter().println("doDelete.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doDelete.getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doDelete.getContextPath: "+request.getContextPath());
				response.getWriter().println("doDelete.getQueryString: "+request.getQueryString());
				response.getWriter().println("doDelete.getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doDelete.getServletContext: "+request.getServletContext());
			}
			
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doDelete. Full Stack Trace: "+buffer.toString());
		}
	}
}

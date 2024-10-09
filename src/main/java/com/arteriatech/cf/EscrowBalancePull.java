package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.api.WalletAPI;
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

public class EscrowBalancePull extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String PYGURL;
	private String PYGUSERNAME;
	private String PYGPASSWORD;
	private String PYGUSERPASS;
	private String PCGURL;
	private String PCGUSERNAME;
	private String PCGPASSWORD;
	private String PCGUSERPASS;
	
	@Override
	public void init() throws ServletException {
		CommonUtils commonUtils=new CommonUtils();
		try{
			PYGUSERNAME=commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			PYGPASSWORD=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			PYGURL=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			PYGUSERPASS=PYGUSERNAME+":"+PYGPASSWORD;
			
			PCGUSERNAME=commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			PCGPASSWORD=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			PCGURL=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			PCGUSERPASS=PCGUSERNAME+":"+PCGPASSWORD;
		}catch(Exception ex){
			throw ex;
		}
		
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		JsonObject resObj=new JsonObject();
		final boolean debug=false;
		Properties props=new Properties();
		ODataLogs oDataLogs = new ODataLogs();
		AtomicInteger stepNo=new AtomicInteger(1);
		try{
			props.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if(request.getHeader("x-arteria-aggrid")!=null&&!request.getHeader("x-arteria-aggrid").equalsIgnoreCase("")){
				if(request.getHeader("x-arteria-regfor")!=null&&!request.getHeader("x-arteria-regfor").equalsIgnoreCase("")){
					final String aggregatorId=request.getHeader("x-arteria-aggrid");
					final String registerFor=request.getHeader("x-arteria-regfor");
					final String loginID = commonUtils.getLoginID(request, response, debug);
					final String logID=oDataLogs.insertEscrowS2SAckApplicationLogs(request, response, "Java", "BalancePull", "Input Payload:{AggregatorID:"+aggregatorId+", RegisterFor:"+registerFor+"}", "BalancePull: Initiated", ""+stepNo.getAndIncrement(), "BalancePull ", PCGURL, PCGUSERPASS, aggregatorId, loginID, debug);
					 ExecutorService threadPool = Executors.newFixedThreadPool(5);
					 threadPool.execute(new Runnable() {
		                    public void run() {
		                    	CommonUtils commonUtils=new CommonUtils();
		                    	String executeURL=PYGURL+"UserRegistrations?$filter=AggregatorID%20eq%20%27"+aggregatorId+"%27%20and%20RegistrationFor%20eq%20%27"+registerFor+"%27";
		                    	try{
		                    	JsonObject userRegResults = commonUtils.executeODataURL(executeURL, PYGUSERPASS, response, debug);
		                    	
		                    	if(userRegResults.get("Status").getAsString().equalsIgnoreCase("000001")){
		                    		userRegResults= userRegResults.get("Message").getAsJsonObject();
		                    		if(userRegResults.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
		                    			JsonArray asJsonArray = userRegResults.get("d").getAsJsonObject().get("results").getAsJsonArray();
		                    			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Total Records Fetched from UserRegistrations Table:"+asJsonArray.size(), stepNo.getAndIncrement(), "Total Records Fetched from UserRegistrations", PCGURL, PCGUSERPASS, aggregatorId, debug);
		                    			for(int i=0;i<asJsonArray.size();i++){
		                    				JsonObject userRegObj = asJsonArray.get(i).getAsJsonObject();
		                    				if(!userRegObj.get("UserID").isJsonNull()&&!userRegObj.get("UserID").getAsString().equalsIgnoreCase("")){
		                    					String userId = userRegObj.get("UserID").getAsString();
		                    					JsonObject balanceResObj=callBalanceEnqueryApi(props,userId,response,debug);
		                    					oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", balanceResObj+"", stepNo.getAndIncrement(), "BalanceEnquery Api Response", PCGURL, PCGUSERPASS, aggregatorId, debug);
		                    					if(balanceResObj.get("Status").getAsString().equalsIgnoreCase("000001")){
		                    						String walletUserCode = balanceResObj.get("wallet-user-code").getAsString();
		                    						String userBalance = balanceResObj.get("user-balance").getAsString();
		                    						JsonObject escrowUserDetails=callEscrowUserDetailsApi(props,userId,response,debug);
		                    						oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", escrowUserDetails+"", stepNo.getAndIncrement(), "EscrowUserDetailsApi Response", PCGURL, PCGUSERPASS, aggregatorId, debug);
		                    						if(escrowUserDetails.get("Status").getAsString().equalsIgnoreCase("000001")){
		                    							String userVan=escrowUserDetails.get("user-van").getAsString();//CHGUID
		                    							String chGuid=escrowUserDetails.get("CHGUID").getAsString();
		                    							JsonObject pgPullFoundRes=callPGPullFoundIflow(props,userBalance,userId,walletUserCode,userVan,chGuid,aggregatorId);
		                    							// insert the response to application log table.
		                    							
		                    						}else{
		                    							
		                    						}
		                    						
		                    					}
		                    					
		                    				}
		                    			}
		                    			
		                    		}else{
		                    			// empty records fetched from table.
		                    			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Total Records Fetched from UserRegistrations Table:"+0, stepNo.getAndIncrement(), "Record Not Exist in the UserRegistrations Table", PCGURL, PCGUSERPASS, aggregatorId, debug);
		                    		}
		                    		
		                    	}else{
		                    		oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", userRegResults+"", stepNo.getAndIncrement(), "Fetching Records from UserRegistrations Table Failed", PCGURL, PCGUSERPASS, aggregatorId, debug);
		                    		
		                    	}
		                    	}catch(Exception ex){
		                    		StackTraceElement[] stackTrace = ex.getStackTrace();
		                    		StringBuffer buffer=new StringBuffer();
		                    		for(int i=0;i<stackTrace.length;i++){
		                    			buffer.append(stackTrace[i]);
		                    		}
		                    		String exceptionClass=ex.getClass().getCanonicalName();
		                    		String localizedMessage="Exception class:"+exceptionClass+",Message:"+ ex.getLocalizedMessage();
		                    		if(localizedMessage.length()>100){
		                    			localizedMessage=localizedMessage.substring(0, 100);
		                    		}
		                    		try {
										oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", buffer.toString(), stepNo.getAndIncrement(), localizedMessage, PCGURL, PCGUSERPASS, aggregatorId, debug);
									} catch (IOException e) {
										
									}
		                    	}
		                    	
		                    	
                           }

						private JsonObject callPGPullFoundIflow(Properties properties,String userBalance,String userId,String walletUserCode,
								String userVan,String chGuid,String aggregatorId) throws IOException {
							JsonObject resObj=new JsonObject();
							JsonObject cpiInputPayload=new JsonObject();
							CommonUtils commonUtils=new CommonUtils();
							try {
								// Context ctxDestFact = new InitialContext();
								// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
								// 		.lookup("java:comp/env/connectivityConfiguration");
								// DestinationConfiguration cpiConfig = configuration
								// 		.getConfiguration(DestinationUtils.CPI_CONNECT);
								DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
									.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
								Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
									.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
								Destination cpiConfig = destinationAccessor.get();
								HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();

								String wsURL = cpiConfig.get("URL").get().toString();
								String userName = cpiConfig.get("User").get().toString();
								String password = cpiConfig.get("Password").get().toString();
								String userpass = userName + ":" + password;
								String pullFundPay2crendoint = properties.getProperty("PGPullFund");
								wsURL = wsURL.concat(pullFundPay2crendoint);
								cpiInputPayload.addProperty("wt-code", walletUserCode);
								cpiInputPayload.addProperty("p2c-txn-id", "");
								cpiInputPayload.addProperty("van", userVan);
								cpiInputPayload.addProperty("user-code", userId);
								cpiInputPayload.addProperty("amount", userBalance);
								cpiInputPayload.addProperty("txn-dt", "");
								cpiInputPayload.addProperty("pay-mode", "2");
								cpiInputPayload.addProperty("utr-no", "");
								cpiInputPayload.addProperty("van-bal", userBalance);
								cpiInputPayload.addProperty("remarks", "Balance pull");
								cpiInputPayload.addProperty("checksum", "");
								cpiInputPayload.addProperty("CH_GUID ", chGuid);
								cpiInputPayload.addProperty("aggregatorID ", aggregatorId);
								cpiInputPayload.addProperty("ALogHID ", "need to add the application log id");
								String x_arteria_apikey = properties.getProperty("EscrowDirectDebit");
								cpiInputPayload.addProperty("x-arteria-apikey",x_arteria_apikey);
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", cpiInputPayload+"", stepNo.getAndIncrement(), "PGPullFound Iflow InputPayload", PCGURL, PCGUSERPASS, aggregatorId, debug);
								URL url = new URL(wsURL);
								HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
								byte[] bytes = cpiInputPayload.toString().getBytes("UTF-8");
								urlConnection.setRequestMethod("GET");
								urlConnection.setRequestProperty("Content-Type", "application/json");
								urlConnection.setRequestProperty("charset", "utf-8");
								urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
								urlConnection.setRequestProperty("Accept", "application/json");
								urlConnection.setRequestProperty("x-arteria-apikey", x_arteria_apikey);
								urlConnection.setDoOutput(true);
								urlConnection.setDoInput(true);
								String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
								urlConnection.setRequestProperty("Authorization", basicAuth);
								urlConnection.connect();
								OutputStream outputStream = urlConnection.getOutputStream();
								OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
								osw.write(cpiInputPayload.toString());
								osw.flush();
								osw.close();
								int resCode = urlConnection.getResponseCode();
								if ((resCode / 100) == 2 ||(resCode / 100)==3) {
									StringBuffer sb = new StringBuffer();
									BufferedReader br = new BufferedReader(
											new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
									String line = null;
									while ((line = br.readLine()) != null) {
										sb.append(line + "\n");
									}
									br.close();
									if (debug) {
										response.getWriter().println("cpi Response " + sb.toString());
									}
									String pullFundPay2creRes = sb.toString();
									JsonParser parse=new JsonParser();
									JsonObject cpiResponse=(JsonObject)parse.parse(pullFundPay2creRes);
									oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", cpiResponse+"", stepNo.getAndIncrement(), "PGPullFound iflow Response", PCGURL, PCGUSERPASS, aggregatorId, debug);
									// insert CPI Response to application logs.
								}else{
									StringBuffer sb = new StringBuffer();
									BufferedReader br = new BufferedReader(
											new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
									String line = null;
									while ((line = br.readLine()) != null) {
										sb.append(line + "\n");
									}
									br.close();
									
									String responseMessage = urlConnection.getResponseMessage();
									oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", sb.toString(), stepNo.getAndIncrement(), "PGPullFound Failure response:"+responseMessage, PCGURL, PCGUSERPASS, aggregatorId, debug);
									// insert the application
								}
								
								

							} catch (Exception ex) {
								StackTraceElement[] stackTrace = ex.getStackTrace();
								StringBuffer buffer=new StringBuffer();
								for(int i=0;i<stackTrace.length;i++){
									buffer.append(stackTrace[i]);
								}
								String str="Exception Occurred While Calling PGPullfound iflow:"+ex.getLocalizedMessage();
								if(str.length()>100){
									str=str.substring(0, 100);
								}
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", buffer.toString(), stepNo.getAndIncrement(), str, PCGURL, PCGUSERPASS, aggregatorId, debug);

							}
							return resObj;

						}

							private JsonObject callEscrowUserDetailsApi(Properties properties, String userId,
									HttpServletResponse response, boolean debug) {
								
								JsonObject resObj = new JsonObject();
								CommonUtils commonUtils = new CommonUtils();
								String walletPublicKey="",merchantPrivateKey="",merchantPublicKey="";
								try{
									JsonObject pgConfigRecords = commonUtils.getPgConfigRecords(request, response, "B2BIZ",
											"000001");
									
									if (pgConfigRecords.get("Status").getAsString().equalsIgnoreCase("000001")) {
										JsonObject pgConfigRecord = pgConfigRecords.get("Message").getAsJsonObject();
										String merchantCode = pgConfigRecord.get("MerchantCode").getAsString();
										String bankKey=pgConfigRecord.get("BankKey").getAsString();
										String pgOwnPublicKey = pgConfigRecord.get("BankKey").getAsString();
										String userRegUrl = pgConfigRecord.get("UserRegURL").getAsString();
										String clientCode = pgConfigRecord.get("ClientCode").getAsString();
										String chGuid=pgConfigRecord.get("ConfigHeaderGUID").getAsString();
										if(null != clientCode)
										{
											 walletPublicKey = properties.getProperty(clientCode+"WalletPublicKey");
											 merchantPrivateKey = properties.getProperty(clientCode+"ARTMerchantPrivateKey");
											 merchantPublicKey = properties.getProperty(clientCode+"ARTMerchantPublicKey");
										}
										else
										{
											walletPublicKey = properties.getProperty("WalletPublicKey");
											merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
											merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
										}
										String escrowUserDetailCall = properties.getProperty("EscrowUserDetailCall");
										WalletParamMap inputParamMap = new WalletParamMap();
										inputParamMap.put("wallet-user-code", userId);
										
										WalletAPI getResponse = new WalletAPI();
										WalletParamMap responseMap = getResponse.callWalletAPI(userRegUrl, escrowUserDetailCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
										if(debug)
											response.getWriter().println("responseMap: " + responseMap);
										if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0)){
											 String userVan = responseMap.get("user-van");
											 resObj.addProperty("Status", "000001");
											 resObj.addProperty("user-van", userVan);
											 resObj.addProperty("CHGUID", chGuid);
											return resObj;
										}else{
											String errorStatus = responseMap.get("error_status");
											String errorMsg = responseMap.get("error_message");
											 resObj.addProperty("Status", errorStatus);
											 resObj.addProperty("errorMessage", errorMsg);
											return resObj;
										}
										
										
									}else{
										//unable to fetch the records from the PGPaymentConfig Table.
										 return pgConfigRecords;
									}
								}catch(Exception ex){
									StackTraceElement[] stackTrace = ex.getStackTrace();
									StringBuffer buffer=new StringBuffer();
									for(int i=0;i<stackTrace.length;i++){
										buffer.append(stackTrace[i]);
									}
									resObj.addProperty("Message", ex.getLocalizedMessage());
									resObj.addProperty("ExceptionMessage", buffer.toString());
									resObj.addProperty("Status", "000002");
									
								}
								return resObj;
							}

							private JsonObject callBalanceEnqueryApi(Properties properties,String userId, HttpServletResponse response,
								boolean debug) {
							JsonObject resObj = new JsonObject();
							CommonUtils commonUtils = new CommonUtils();
							String walletPublicKey="",merchantPrivateKey="",merchantPublicKey="";
							try {
								
								JsonObject pgConfigRecords = commonUtils.getPgConfigRecords(request, response, "B2BIZ",
										"000002");
								if (pgConfigRecords.get("Status").getAsString().equalsIgnoreCase("000001")) {
									JsonObject pgConfigRecord = pgConfigRecords.get("Message").getAsJsonObject();
									String merchantCode = pgConfigRecord.get("MerchantCode").getAsString();
									String pgCatId=pgConfigRecord.get("PGCategoryID").getAsString();
									String pgID = pgConfigRecord.get("PGID").getAsString();
									String accBalUrl = pgConfigRecord.get("AccBalURL").getAsString();
									String clientCode = pgConfigRecord.get("ClientCode").getAsString();
									if(null != clientCode)
									{
										 walletPublicKey = properties.getProperty(clientCode+"WalletPublicKey");
										 merchantPrivateKey = properties.getProperty(clientCode+"ARTMerchantPrivateKey");
										 merchantPublicKey = properties.getProperty(clientCode+"ARTMerchantPublicKey");
									}
									else
									{
										walletPublicKey = properties.getProperty("WalletPublicKey");
										merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
										merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
									}
									String balanceEnquiryCall = properties.getProperty("balanceEnquiryCall");
									WalletParamMap inputParamMap = new WalletParamMap();
									inputParamMap.put("wallet-user-code",userId);
									WalletAPI getResponse = new WalletAPI();
									WalletParamMap responseMap = getResponse.callWalletAPI(accBalUrl, balanceEnquiryCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
									if(debug){
										response.getWriter().println("Response from Pay2Corp: "+responseMap);
									}
									
									if(null != responseMap.get("checksum") && null != responseMap.get("wallet-user-code")&&responseMap.get("wallet-user-code").toString().trim().length() >0){
										resObj.addProperty("Status", "000001");
										resObj.addProperty("wallet-user-code",responseMap.get("wallet-user-code"));
										resObj.addProperty("user-balance",responseMap.get("user-balance"));
									}else{
										String errorStatus = responseMap.get("error_status");
										String errorMsg = responseMap.get("error_message");
										resObj.addProperty("Status", "000002");
										resObj.addProperty("error_Status",errorStatus);
										resObj.addProperty("error_Message",errorMsg);
									}
									return resObj;
								} else {
									return pgConfigRecords;
								}
							} catch (Exception ex) {
								StackTraceElement[] stackTrace = ex.getStackTrace();
								StringBuffer buffer=new StringBuffer();
								for(int i=0;i<stackTrace.length;i++){
									buffer.append(stackTrace[i]);
								}
								resObj.addProperty("Status", "000002");
								resObj.addProperty("Message",ex.getLocalizedMessage());
								resObj.addProperty("ExceptionMessage",buffer.toString());
								return resObj;

							}
						}
		                });
					   resObj.addProperty("Message", "Process Initiated");
					   resObj.addProperty("Status", "000001");
					   resObj.addProperty("ErrorCode", "");
					   response.getWriter().println(resObj);
					 
				}else{
					resObj.addProperty("Message", "Mandatory header x-arteria-regfor is blank");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			}else{
				resObj.addProperty("Message", "Mandatory header x-arteria-aggrid is blank");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
				
			}
			
			
		}catch(Exception ex){
			
		}
	}
	

}

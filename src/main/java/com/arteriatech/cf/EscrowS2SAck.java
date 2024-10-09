package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import io.vavr.control.Try;

public class EscrowS2SAck extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final String CPI_CONNECTION_DESTINATION = "CPIConnect";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pgID = "", walletResponseMessage = "", arteriaApiKey = "", walletApiKey = "", p2c_txn_id = "",
				amount = "", clientCode = "";
		String oDataURL = "", userName = "", password = "", aggregatorID = "", userPass = "", loginID = "",
				servletPath = "", wsURL = "", userpass = "", pullFundPay2crendoint = "", pullFundPay2creRes = "",
				errorCode = "";
		String error_status="",error_message="",x_arteria_apikey="";
		CommonUtils commonUtils = new CommonUtils();
		Properties properties = new Properties();
		ODataLogs oDataLogs = new ODataLogs();
		JsonObject cpiInput = new JsonObject();
		int stepNo = 0;
		boolean debug = false;
		String logID = "",status="";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			servletPath = request.getServletPath();
			stepNo=stepNo+1;
			logID=oDataLogs.insertEscrowS2SAckApplicationLogs(request, response, "Java", "Intimation API", "Before Reading the Request Body", "EscrowS2SAck: Initiated", ""+stepNo, "EscrowS2SAck", oDataURL, userpass, aggregatorID, loginID, debug);
			stepNo=stepNo+1;
			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", servletPath, stepNo, "EscrowS2SAck: Hit received on this path", oDataURL, userpass, aggregatorID, debug);
			walletResponseMessage = commonUtils.getGetBody(request, response);
			stepNo = stepNo + 1;
			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", walletResponseMessage, stepNo, "EscrowS2SAck: Encrypted Request", oDataURL, userpass, aggregatorID, debug);
			
			if (request.getParameter("pgID") != null) {
				pgID = request.getParameter("pgID");
				pgID = pgID.replaceAll("'", "");
			}else{
			pgID=properties.getProperty("ICICIPGID");
			}
			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;
			}
			if (debug) {
				response.getWriter().println("Input Payload Recived PGID " + pgID);
			}
			
			
			if (walletResponseMessage != null && !walletResponseMessage.equalsIgnoreCase("")) {
				if (pgID != null && !pgID.equalsIgnoreCase("")) {
					Map<String, String> constantValues = commonUtils.getConstantValues(request, response, pgID);
					if (!constantValues.isEmpty() && constantValues.get("ClientCode") != null
							&& !constantValues.get("ClientCode").equalsIgnoreCase("")) {
						clientCode = constantValues.get("ClientCode");
						walletApiKey = properties.getProperty(clientCode.toString() + "WalletPublicKey");
						arteriaApiKey = properties.getProperty(clientCode.toString() + "ARTMerchantPrivateKey");
					} else {
						arteriaApiKey = properties.getProperty("ARTMerchantPrivateKey");
						walletApiKey = properties.getProperty("WalletPublicKey");
					}
					if(debug){
						response.getWriter().println("Client code "+clientCode);
						response.getWriter().println("WalletApiKey : "+walletApiKey);
						response.getWriter().println("ArteriaApiKey : "+arteriaApiKey);
					}
					if (pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID"))) {
						try {
							WalletMessageBean walletMessageBean = new WalletMessageBean();
							walletMessageBean.setWalletResponseMessage(walletResponseMessage);
							walletMessageBean.setWalletKey(walletApiKey);
							walletMessageBean.setClientKey(arteriaApiKey);
							if(debug){
								response.getWriter().println("Response Message "+walletResponseMessage);
							}
							if (walletMessageBean.validateWalletResponseMessage()) {
								if(debug){
									response.getWriter().println("inside the validateWalletResponseMessage is true ");
								}
								WalletParamMap resParamsMap = walletMessageBean.getResponseMap();
								if(debug){
									response.getWriter().println(" WalletParamMap "+resParamsMap);	
								}
								if (resParamsMap != null && !resParamsMap.isEmpty()) {
									for (String key : resParamsMap.keySet()) {
										cpiInput.addProperty(key, resParamsMap.get(key));
									}
									cpiInput.addProperty("CH_GUID", constantValues.get("CHGUID"));
									cpiInput.addProperty("ALogHID", logID);
									cpiInput.addProperty("aggregatorID", aggregatorID);
								}else{
									errorCode = "E301";
									stepNo = stepNo + 1;
									oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "WalletParamMap resParamsMap = walletMessageBean.getResponseMap(); resParamsMap map is null ", stepNo, "EscrowS2SAck:resParamsMap map is null", oDataURL, userpass, aggregatorID, debug);
								}
							} else {
								errorCode = "E100";
								stepNo = stepNo + 1;
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "walletMessageBean.validateWalletResponseMessage() unable to validate the Response Message", stepNo, "EscrowS2SAck:unable to validate the Response Message", oDataURL, userpass, aggregatorID, debug);
							}
						} catch (Exception ex) {
							errorCode = "E201";
							StackTraceElement element[] = ex.getStackTrace();
							StringBuffer buffer = new StringBuffer();
							for(int i=0;i<element.length;i++)
							{
								buffer.append(element[i]);
							}
							if(debug){
								response.getWriter().println("getLocalizedMessage  "+ex.getLocalizedMessage());
								response.getWriter().println("getMessage  "+ex.getMessage());
								response.getWriter().println("StackTrace  "+buffer.toString());
							}
							stepNo = stepNo + 1;
							oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response,  logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "EscrowS2SAck:Exception Occured While Decrypting A Pay2ToCorp Message", oDataURL, userpass, aggregatorID, debug);
							
						}
						if (errorCode.equalsIgnoreCase("")) {
							// Context ctxDestFact = new InitialContext();
							// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
							// 		.lookup("java:comp/env/connectivityConfiguration");
							// DestinationConfiguration cpiConfig = configuration
							// 		.getConfiguration(CPI_CONNECTION_DESTINATION);
							DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
									.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
							Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
									.tryGetDestination(CPI_CONNECTION_DESTINATION, options);
							Destination cpiConfig = destinationAccessor.get();

							wsURL = cpiConfig.get("URL").get().toString();
							userName = cpiConfig.get("User").get().toString();
							password = cpiConfig.get("Password").get().toString();
							userpass = userName + ":" + password;
							pullFundPay2crendoint = properties.getProperty("PGPullFund");
							wsURL = wsURL.concat(pullFundPay2crendoint);
							if (debug) {
								response.getWriter().println("PullFundsFromPay2Corp Url: " + wsURL);
								response.getWriter().println(" CPI input payload "+cpiInput);
							}
							x_arteria_apikey = properties.getProperty("EscrowDirectDebit");
							cpiInput.addProperty("x-arteria-apikey", x_arteria_apikey);
							stepNo = stepNo + 1;
							oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID,"I", "/ARTEC/PY", cpiInput.toString(), stepNo, "EscrowS2SAck: Input to CPI", oDataURL, userpass, aggregatorID, debug);
							URL url = new URL(wsURL);

							stepNo = stepNo + 1;
							oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", wsURL, stepNo, "EscrowS2SAck: CPI Request URL", oDataURL, userpass, aggregatorID, debug);
							HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
							byte[] bytes = cpiInput.toString().getBytes("UTF-8");
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
							osw.write(cpiInput.toString());
							osw.flush();
							osw.close();
							int resCode = urlConnection.getResponseCode();
							if (debug) {
								response.getWriter().println("responseCode: " + resCode);
							}
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
								pullFundPay2creRes = sb.toString();
								JsonParser jsonParser = new JsonParser();
								JsonObject responseJson = (JsonObject) jsonParser.parse(pullFundPay2creRes);
								stepNo = stepNo + 1;
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", responseJson.toString(), stepNo, "EscrowS2SAck: Response from CPI", oDataURL, userpass, aggregatorID, debug);
								status = responseJson.get("Status").getAsString();
								if(status.equalsIgnoreCase("000001")){
									status="100";
								}else{
									status="101";
								}
								if (cpiInput.has("p2c-txn-id")) {
									p2c_txn_id = cpiInput.get("p2c-txn-id").getAsString();
								}
								if (cpiInput.has("amount")) {
									amount = cpiInput.get("amount").getAsString();
								}
								WalletParamMap walletMap = new WalletParamMap();
								walletMap.put("status",status);
								walletMap.put("p2c-txn-id", p2c_txn_id);
								walletMap.put("amount", amount);
								stepNo = stepNo + 1;
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", ""+walletMap, stepNo, "EscrowS2SAck: Plain Response to Initmation API", oDataURL, userpass, aggregatorID, debug);
								WalletMessageBean walletMessageReqBean = new WalletMessageBean();
								walletMessageReqBean.setRequestMap(walletMap);
								walletMessageReqBean.setClientKey(arteriaApiKey);
								walletMessageReqBean.setWalletKey(walletApiKey);
								String walletRequestMessage = walletMessageReqBean.generateWalletRequestMessage();
								if(debug){
									response.getWriter().println("Response to Intimation API"+walletRequestMessage);
								}
								stepNo = stepNo + 1;
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", walletRequestMessage, stepNo, "EscrowS2SAck: Encrypted Response to Intimation API", oDataURL, userpass, aggregatorID, debug);
								response.setContentType("text/plain");
								response.getWriter().println(walletRequestMessage);
							} else {
								StringBuffer sb = new StringBuffer();
								BufferedReader br = new BufferedReader(
										new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
								String line = null;
								while ((line = br.readLine()) != null) {
									sb.append(line + "\n");
								}
								br.close();
								if (debug) {
									response.getWriter().println("getErrorStream: " + sb.toString());
								}
								pullFundPay2creRes = sb.toString();
								stepNo = stepNo + 1;
								oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", urlConnection.getResponseCode()+"|"+urlConnection.getResponseMessage(), stepNo, "EscrowS2SAck:Error Response", oDataURL, userpass, aggregatorID, debug);
								if (debug)
									response.getWriter().println("responseJson: " + pullFundPay2creRes);
								error_status=properties.getProperty("error_status");
								error_message=properties.getProperty("error_message");
								String message = "error_status="+error_status+"|error_message="+error_message;
								response.setContentType("text/plain");
								response.getWriter().println(message);

							}
						} else if (errorCode.equalsIgnoreCase("E100")) {
							error_status = properties.getProperty("error_status");
							error_message = properties.getProperty("error_message");
							String message = "error_status=" + error_status + "|error_message=" + error_message;
							response.setContentType("text/plain");
							response.getWriter().println(message);
						}else if(errorCode.equalsIgnoreCase("E301")){
							error_status = properties.getProperty("error_status");
							error_message = properties.getProperty("error_message");
							String message = "error_status=" + error_status + "|error_message=" + error_message;
							response.setContentType("text/plain");
							response.getWriter().println(message);
						}else {
							error_status = properties.getProperty("error_status");
							error_message = properties.getProperty("error_message");
							String message = "error_status=" + error_status + "|error_message=" + error_message;
							response.setContentType("text/plain");
							response.getWriter().println(message);

						}
					}else{
						response.setContentType("text/plain");
						if(debug){
							response.getWriter().println("Invalid PGID "+pgID+"Valid PGID Should Be B2BIZ");
						}
						stepNo = stepNo + 1;
						oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Invalid PGID "+pgID+" PGID should be B2BIZ ", stepNo, "EscrowS2SAck:Invalid PGID ", oDataURL, userpass, aggregatorID, debug);
						error_status=properties.getProperty("error_status");
						error_message=properties.getProperty("error_message");
						String message = "error_status="+error_status+"|error_message="+error_message;
						response.getWriter().println(message);	
					}
				} else {
					stepNo = stepNo + 1;
					oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request Param pgID is Empty", stepNo, "EscrowS2SAck:PGId is Empty", oDataURL, userpass, aggregatorID, debug);
					error_status=properties.getProperty("error_status");
					error_message=properties.getProperty("error_message");
					String message = "error_status="+error_status+"|error_message="+error_message;
					response.setContentType("text/plain");
					response.getWriter().println(message);
				}

			} else {
				stepNo = stepNo + 1;
				oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "input Payload  walletResponseMessage is empty", stepNo, "EscrowS2SAck:WalletResponseMessage is empty", oDataURL, userpass, aggregatorID, debug);
				error_status = properties.getProperty("error_status");
				error_message = properties.getProperty("error_message");
				String message = "error_status=" + error_status + "|error_message=" + error_message;
				response.setContentType("text/plain");
				response.getWriter().println(message);
			}
		} catch (NullPointerException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo = stepNo + 1;
			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "EscrowS2SAck:"+ ex.getLocalizedMessage(), oDataURL, userpass, aggregatorID, debug);
			error_status = properties.getProperty("error_status");
			error_message = properties.getProperty("error_message");
			String message = "error_status=" + error_status + "|error_message=" + error_message;
			response.setContentType("text/plain");
			response.getWriter().println(message);

		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo = stepNo + 1;
			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "EscrowS2SAck:"+ ex.getLocalizedMessage(), oDataURL, userpass, aggregatorID, debug);
			error_status = properties.getProperty("error_status");
			error_message = properties.getProperty("error_message");
			String message = "error_status=" + error_status + "|error_message=" + error_message;
			response.setContentType("text/plain");
			response.getWriter().println(message);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo = stepNo + 1;
			oDataLogs.insertEscrowS2SAckMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "EscrowS2SAck:"+ ex.getLocalizedMessage(), oDataURL, userpass, aggregatorID, debug);
			error_status = properties.getProperty("error_status");
			error_message = properties.getProperty("error_message");
			String message = "error_status=" + error_status + "|error_message=" + error_message;
			response.setContentType("text/plain");
			response.getWriter().println(message);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}

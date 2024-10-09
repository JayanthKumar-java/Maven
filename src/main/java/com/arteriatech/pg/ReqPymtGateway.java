package com.arteriatech.pg;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arteriatech.support.DestinationUtils;
import com.ccavenue.security.AesCryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.isg.isgpay.ISGPayEncryption;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.tp.pg.util.TransactionRequestBean;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;

/**
 * Servlet implementation class ReqPymtGateway
 */
@WebServlet("/ReqPymtGateway")
public class ReqPymtGateway extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// private TenantContext tenantContext;
	String destAggrID = "";

	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	//private static final String PCGW_UTIL_DEST_NAME = "pcgw_utils_op";
	private static final String NOAUTH_DEST_NAME = "pcgw_utils_noauth";
	private CookieStore globalCookieStore = null;

	private static final String PG_TXN_STATUS = "000010";

	private static final String PAYMENT_STATUS = "000100";

	public String customerName = "", customerMobile = "", customerEmail = "", pgGuid = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReqPymtGateway() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("1. ReqPymtGateway doGet function starts");
		response.setContentType("application/json");
		CommonUtils commonUtils = new CommonUtils();
		JsonArray appLogArry=new JsonArray();
		JsonObject appLogObj=new JsonObject();
		String pghGuid="",OdataUrl="",username="",password="",userpass="",executeUrl="", loginID="";
		AtomicInteger stepNumber=new AtomicInteger(1);
		JsonObject applicationLog=new JsonObject();
		boolean debug = false;
	
		try {
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug=true;
			}
			destAggrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			OdataUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userpass=username+":"+password;
			// Below for validating Customer
			String customerNo = "", application = "", loginSessionID = "", PGCategoryID = "", configHdrGuid = "",
					pgID = "", environment = "", url="";
			boolean isValidCustomer = false;
			String paymentTxnResponse = "";
			if (null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");

			if (null != request.getParameter("Application"))
				application = request.getParameter("Application");

			if (null != request.getParameter("PGCategoryID"))
				PGCategoryID = request.getParameter("PGCategoryID");
			else
				PGCategoryID = "000002";

			if (null != request.getParameter("ConfigHeaderGUID"))
				configHdrGuid = request.getParameter("ConfigHeaderGUID");
			else
				configHdrGuid = "";

			if (null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");

			//loginSessionID = request.getParameter("sessionID"); Commented in CF
			loginID = commonUtils.getLoginID(request, response, debug);
			url = commonUtils.getDestinationURL(request, response, "URL");
			if(debug)
				response.getWriter().println("loginID: "+loginID);

			loginSessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
			
			if(debug){
				response.getWriter().println("loginSessionID: "+loginSessionID);
			}

			String ipAddress = commonUtils.getIPAddress(request);
			if(ipAddress.length()>100){
				ipAddress=ipAddress.substring(0, 100);
				
			}
			
			String returnUrl = request.getParameter("return-url");
			//String returnUrl=removeSpecialCharacter(request.getParameter("return-url"));
			if (returnUrl != null && !returnUrl.equalsIgnoreCase("")) {
				String[] split = returnUrl.split("~");
				for (int i = 0; i < split.length; i++) {
					String params = split[i];
					if (params.startsWith("PGGID")) {
						int pghIndex = params.indexOf("D");
						pghGuid = params.substring(pghIndex+1, params.length());
						break;

					}
				}
			}
			
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().println("pghGuid:"+pghGuid);
			}
			
			String appLogID = commonUtils.generateGUID(36);
			
			String createdBy = commonUtils.getUserPrincipal(request, "name", response);
			String createdAt =commonUtils. getCreatedAtTime();
			long createdOnInMillis = commonUtils.getCreatedOnDate();
			applicationLog.addProperty("ID",appLogID);
			applicationLog.addProperty("AggregatorID",destAggrID);
			applicationLog.addProperty("LogObject","Java");
			applicationLog.addProperty("LogSubObject","PaymentGateway");
			applicationLog.addProperty("LogDate","/Date("+createdOnInMillis+")/");
			applicationLog.addProperty("LogTime",createdAt);
			applicationLog.addProperty("Program","ReqPymtGateway");
			applicationLog.addProperty("Process",request.getServletPath());
			applicationLog.addProperty("LogUser",ipAddress);
			applicationLog.addProperty("ProcessID",pgID);
			applicationLog.addProperty("ProcessRef1",pghGuid);
			applicationLog.addProperty("CreatedBy",createdBy);
			applicationLog.addProperty("CreatedOn","/Date("+createdOnInMillis+")/");
			applicationLog.addProperty("CreatedAt",createdAt);
			if (request.getParameter("txn-id") != null) {
				applicationLog.addProperty("SourceReferenceID", request.getParameter("txn-id"));
			}
			executeUrl=OdataUrl+"ApplicationLogs";
			// if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
			// 	// response.getWriter().println("appLogObj:"+applicationLog);
			// 	response.getWriter().println("executeUrl:"+executeUrl);
			// }
			JsonObject applogOb = commonUtils.executePostURL(executeUrl, userpass, response, applicationLog, request, false, "PCGWHANA");
			// if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
			// 	response.getWriter().println("applogOb:"+applogOb);
			// }
			// pgGuid = "74F0F387-CFD6-4CCE-BC26-DB223C4920D1";

			if (applogOb.get("Status").getAsString().equalsIgnoreCase("000001")) {
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

				if (!pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")) && !pgID.equalsIgnoreCase(properties.getProperty("CCAICICI")) && !pgID.equalsIgnoreCase(properties.getProperty("CCAPGID"))) {
					// isValidCustomer = commonUtils.getCustomers(request, response,
					// loginSessionID, customerNo);
					// isValidCustomer = getCustomers(request, response, loginSessionID, customerNo);
					isValidCustomer = commonUtils.getCustomers(request, response, loginSessionID, customerNo, debug);
				} else {
					isValidCustomer = true;
					// getPaymentTransaction(request, response, pgID,
					// request.getParameter("txn-id"));
				}
				// isValidCustomer = true;
				// if(request.getParameter("debug").equalsIgnoreCase("true"))
				// response.getWriter().printlnln("isValidCustomer:
				// "+isValidCustomer);

				if (isValidCustomer) {
					paymentTxnResponse = validatePaymentReq(request, response);

					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
						response.getWriter().println("destAggrID: " + destAggrID);
						response.getWriter().println("paymentTxnResponse: " + paymentTxnResponse);
					}

					if (paymentTxnResponse.equalsIgnoreCase("Success")) {
						if (null != pgID && pgID.trim().length() > 0) {
							// Below code for consuming Gateway services
							// String configurationValues =
							// getConstantValues(request, response, "",
							// PGCategoryID);
							String configurationValues = "";
							if (!pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID"))) {
								configurationValues = getConstantValues(request, response, "", pgID);
							}

							if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("configurationValues: " + configurationValues);

							if (null != request.getParameter("system"))
								environment = request.getParameter("system");
							// Below code for calling PG services

							// pgID = "YESPAYU";
							if (pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID"))) {
								initiateICICIPaymentRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("AxisPGID"))) {
								initiateAxisPGRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("YesPGID"))) {
								initiateYesPayUPGRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID"))) {
								CommonUtils readConfig = new CommonUtils();
								Map<String, String> configMap = readConfig.readConfigValues(pgID, environment);

								initiateEazyPayRequest(request, response, configMap, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")) || pgID.equalsIgnoreCase("RZRPAY")) {
								initiateRazorPayPGRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))) {
								initiateRazorPayPGRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else if (pgID.equalsIgnoreCase(properties.getProperty("CCAvenuePGID")) || pgID.equalsIgnoreCase(properties.getProperty("MobileCCPGID")) || pgID.equalsIgnoreCase(properties.getProperty("CCAICICI")) || pgID.equalsIgnoreCase(properties.getProperty("CCAPGID"))) {
								initiateCCAvenuePGRequest(request, response, configurationValues, applicationLog, appLogArry);
							}else if (pgID.equalsIgnoreCase(properties.getProperty("TechProcessPGID"))) {
								initiateTechProcessPGRequest(request, response, configurationValues, applicationLog, appLogArry);
							} else {
								JsonObject result = new JsonObject();
								result.addProperty("pgReqMsg", "Invalid Payment Gateway Request.");
								result.addProperty("Valid", "false");
								// result.addProperty("walletClientCode",
								// customerNo);

								commonUtils.crtAppLogMsgsObj(request, response, request.getServletPath(), request.getParameter("txn-id") + "", result + "", appLogObj.get("ID").getAsString(), appLogArry);
								if(appLogArry.size()>0){
									JsonObject appLogMsg=new JsonObject();
									JsonObject onObjEvent=new JsonObject();
									appLogMsg.addProperty("Object", "ApplicationLog");
									appLogMsg.addProperty("Event", "INSERT");
									appLogMsg.addProperty("AggregatorID", destAggrID);
									appLogMsg.addProperty("Identifier", "000002");
									appLogMsg.add("Message", appLogArry);
									onObjEvent.add("OnObjectEvent", appLogMsg);
									commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
								}
								response.getWriter().println(new Gson().toJson(result));
							}
						} else {
							JsonObject result = new JsonObject();
							result.addProperty("pgReqMsg", "Config Items not found");
							result.addProperty("walletClientCode", customerNo);
							result.addProperty("Valid", "false");

							commonUtils.crtAppLogMsgsObj(request, response, request.getServletPath(), request.getParameter("txn-id") + "", result + "", appLogObj.get("ID").getAsString(), appLogArry);
							if(appLogArry.size()>0){
								JsonObject appLogMsg=new JsonObject();
								JsonObject onObjEvent=new JsonObject();
								appLogMsg.addProperty("Object", "ApplicationLog");
								appLogMsg.addProperty("Event", "INSERT");
								appLogMsg.addProperty("AggregatorID", destAggrID);
								appLogMsg.addProperty("Identifier", "000002");
								appLogMsg.add("Message", appLogArry);
								onObjEvent.add("OnObjectEvent", appLogMsg);
								commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
							}
							response.getWriter().println(new Gson().toJson(result));
						}
					} else {
						JsonObject result = new JsonObject();
						result.addProperty("pgReqMsg", paymentTxnResponse);
						result.addProperty("walletClientCode", customerNo);
						result.addProperty("Valid", "false");
						commonUtils.crtAppLogMsgsObj(request, response, request.getServletPath(), request.getParameter("txn-id") + "", result + "", appLogObj.get("ID").getAsString(), appLogArry);
						if(appLogArry.size()>0){
							JsonObject appLogMsg=new JsonObject();
							JsonObject onObjEvent=new JsonObject();
							appLogMsg.addProperty("Object", "ApplicationLog");
							appLogMsg.addProperty("Event", "INSERT");
							appLogMsg.addProperty("AggregatorID", destAggrID);
							appLogMsg.addProperty("Identifier", "000002");
							appLogMsg.add("Message", appLogArry);
							onObjEvent.add("OnObjectEvent", appLogMsg);
							commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
						}
						response.getWriter().println(new Gson().toJson(result));
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("pgReqMsg", customerNo + " is not authorized to make a payment.");
					result.addProperty("walletClientCode", customerNo);
					result.addProperty("Valid", "false");
					commonUtils.crtAppLogMsgsObj(request, response, request.getServletPath(), request.getParameter("txn-id") + "", result + "", appLogObj.get("ID").getAsString(), appLogArry);
					if(appLogArry.size()>0){
						JsonObject appLogMsg=new JsonObject();
						JsonObject onObjEvent=new JsonObject();
						appLogMsg.addProperty("Object", "ApplicationLog");
						appLogMsg.addProperty("Event", "INSERT");
						appLogMsg.addProperty("AggregatorID", destAggrID);
						appLogMsg.addProperty("Identifier", "000002");
						appLogMsg.add("Message", appLogArry);
						onObjEvent.add("OnObjectEvent", appLogMsg);
						commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
					}
					response.getWriter().println(new Gson().toJson(result));
				}
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("Message", "Unable to create ApplicationLog");
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "J002");
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		} catch (Exception e) {
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"-->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"-->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id")+"", buffer.toString(), appLogObj.get("ID").getAsString(),  appLogArry);
			
			if(appLogArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			
			response.getWriter().println(e.getMessage());
		}
		
		
	}

	private void initiateICICIPaymentRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException {
		CommonUtils commonUtils=new CommonUtils();
		AtomicInteger stepNumber=new AtomicInteger(1);
		try {
			// Below code for Payment Gateway
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode = "",
					paymentRequestCall = "", WSURL = "", pgReqMsg = "", pgRequestErrorMsg = "";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

			String wholeParamString = "", paramName = "", paramValue = "", pgID = "", clientCode = "";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());

				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("PGID")) {
					pgID = paramValue;
				}
				if (paramName.equalsIgnoreCase("WSURL")) {
					// response.getWriter().println("WSURL:" + WSURL);
					WSURL = paramValue;
				}
				if (paramName.equalsIgnoreCase("clientCode")) {
					clientCode = paramValue;
				}
			}

			if (null != clientCode) {
				walletPublicKey = properties.getProperty(clientCode.toString() + "WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString() + "ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString() + "ARTMerchantPublicKey");
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("PRD Keys found");
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode + "WalletPublicKey" + walletPublicKey);
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode + "ARTMerchantPrivateKey" + merchantPrivateKey);
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println(clientCode + "ARTMerchantPublicKey" + merchantPublicKey);
			} else {
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}

			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
			WalletParamMap payReqInputMap = new WalletParamMap();
			if (null != request.getParameter("txn-id"))
				payReqInputMap.put("txn-id", request.getParameter("txn-id"));
			if (null != request.getParameter("txn-session-id"))
				payReqInputMap.put("txn-session-id", request.getParameter("txn-session-id"));

			if (null != request.getParameter("wallet-user-code"))
				payReqInputMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			if (null != request.getParameter("txn-datetime"))
				payReqInputMap.put("txn-datetime", request.getParameter("txn-datetime"));
			if (null != request.getParameter("txn-amount"))
				payReqInputMap.put("txn-amount", request.getParameter("txn-amount"));
			if (null != request.getParameter("txn-for"))
				payReqInputMap.put("txn-for", request.getParameter("txn-for"));
			else
				payReqInputMap.put("txn-for", paymentRequestCall);

			if (null != request.getParameter("return-url")){
				//String returnUrl = removeSpecialCharacter(request.getParameter("return-url"));
				payReqInputMap.put("return-url", request.getParameter("return-url"));
			}
			if (null != request.getParameter("cancel-url")){
				//String returnUrl = removeSpecialCharacter(request.getParameter("cancel-url"));
				payReqInputMap.put("cancel-url", request.getParameter("cancel-url"));
			}
			if (null != request.getParameter("challan-expiry-date"))
				payReqInputMap.put("challan-expiry-date", request.getParameter("challan-expiry-date"));

			/*
			 * Additional parameters 1 to 4 are used for getting redirect url
			 * params based on which we'll form redirect url in the response
			 * servlet site-id : eg: SSLaunchpad application-id : eg:
			 * ssoutstanding1pg action-name: eg: Display nav-param: eg: Search
			 * Please note that these parameter values should not have length
			 * more than 25 characters
			 */

			if (null != request.getParameter("site-id"))
				payReqInputMap.put("additional-param1", request.getParameter("site-id"));
			else
				payReqInputMap.put("additional-param1", "SSLaunchpad");

			if (null != request.getParameter("application-id"))
				payReqInputMap.put("additional-param2", request.getParameter("application-id"));
			else
				payReqInputMap.put("additional-param2", "sfoutstdnginvs1pg");

			if (null != request.getParameter("action-name"))
				payReqInputMap.put("additional-param3", request.getParameter("action-name"));
			else
				payReqInputMap.put("additional-param3", "Display");

			if (null != request.getParameter("nav-param"))
				payReqInputMap.put("additional-param4", request.getParameter("nav-param"));
			else
				payReqInputMap.put("additional-param4", "Search");

			if (null != request.getParameter("additional-param5"))
				payReqInputMap.put("additional-param5", request.getParameter("additional-param5"));
			else
				payReqInputMap.put("additional-param5", "NA");
			if (null != request.getParameter("additional-param6"))
				payReqInputMap.put("additional-param6", request.getParameter("additional-param6"));
			else
				payReqInputMap.put("additional-param6", "NA");
			if (null != request.getParameter("additional-param7"))
				payReqInputMap.put("additional-param7", request.getParameter("additional-param7"));
			else
				payReqInputMap.put("additional-param7", "NA");
			if (null != request.getParameter("additional-param8"))
				payReqInputMap.put("additional-param8", request.getParameter("additional-param8"));
			else
				payReqInputMap.put("additional-param8", "NA");
			if (null != request.getParameter("additional-param9"))
				payReqInputMap.put("additional-param9", request.getParameter("additional-param9"));
			else
				payReqInputMap.put("additional-param9", "NA");
			if (null != request.getParameter("pay-for"))
				payReqInputMap.put("additional-param10", request.getParameter("pay-for"));
			else
				payReqInputMap.put("additional-param10", "NA");

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				System.out.println("payReqInputMap: " + payReqInputMap.toString());
			
			JsonObject requestObj=new JsonObject();
			payReqInputMap.forEach((key, value) -> {
				if (!key.equalsIgnoreCase("return-url") || !key.equalsIgnoreCase("cancel-url")) {
					if (value != null) {
						requestObj.addProperty(key, value);
					} else {
						requestObj.addProperty(key, "");
					}
				}
			});
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), requestObj+"", appLogObj.get("ID").getAsString(),appLogMsgArry);
			
			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setRequestMap(payReqInputMap);
			walletMessageBean.setWalletKey(walletPublicKey);

			walletMessageBean.setClientKey(merchantPrivateKey);
			// System.out.println(merchantPrivateKey);
			// response.getWriter().printlnln("code:
			// "+request.getParameter("wallet-user-code")+"|||merchantCode:
			// "+merchantCode);
			pgReqMsg = walletMessageBean.generateWalletRequestMessage();

			if (pgReqMsg != null && pgReqMsg.trim().length() > 0) {
				// response.getWriter().println("IF Condition");
				JsonObject result = new JsonObject();
				result.addProperty("walletRequestMessage", pgReqMsg);
				result.addProperty("RawPayload", payReqInputMap.toString());
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", WSURL);
				// result.addProperty("WSURL","https://demo.b2biz.co.in/ws/payment");
				result.addProperty("parameters", "walletClientCode|walletRequestMessage");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), ""+request.getParameter("txn-id"), result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			} else {
				// response.getWriter().println("ELSE Condition");
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgRequestErrorMsg);
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			}
		} catch (Exception e) {
			
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "--->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage() + "--->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			response.getWriter().print("initiateICICIPaymentRequest Error: " + e.getMessage());
		}
	}

	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// get the destination name from the request
		String destinationName = "";
		// LOGGER.info("4. destination name from request: "+destinationName);
		// check destination null condition
		destinationName = DestinationUtils.PCGW_UTILS_OP;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API
			// "connectivityConfiguration"
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn
			// 		.lookup("java:comp/env/connectivityConfiguration");
			// destConfiguration = configuration.getConfiguration(destinationName);
			// LOGGER.info("5. destination configuration object created");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format(
							"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
							DestinationUtils.PCGW_UTILS_OP));
			// LOGGER.error("Lookup of destination failed", e);
			// response.getWriter().println(" " + errorMessage);
		}
		return destConfiguration;
	}

	private String validatePaymentReq(HttpServletRequest request, HttpServletResponse response)
			throws IOException, URISyntaxException {
		String destURL = "", pgID = "", userName = "", password = "", authParam = "", authMethod = "",
				paymentService = "", paymentFilter = "", basicAuth = "", sapclient = "", sessionID = "",
				loginMethod = "", txnRefNo = "", txnAmount = "";
		boolean debug = false, isParamMissing = false;
		String returnMessage = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();

		HttpGet pgPaymentsGet = null;
		HttpEntity pgPaymentsEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;

		try {
			destConfiguration = getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
			.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;

			// debug = true;

			if (null != request.getParameter("txn-id") && request.getParameter("txn-id").trim().length() > 0) {
				txnRefNo = request.getParameter("txn-id");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("txn-amount") && request.getParameter("txn-amount").trim().length() > 0) {
				txnAmount = request.getParameter("txn-amount");
			} else {
				isParamMissing = true;
			}

			if (debug) {
				response.getWriter().println("txnAmount:" + txnAmount);
				response.getWriter().println("txnRefNo:" + txnRefNo);
			}

			String loginID = commonUtils.getUserPrincipal(request, "name", response);

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

			paymentFilter = "LoginID eq '" + sessionID + "' and TrackID eq'" + txnRefNo + "'";
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

			// Context tenCtx = new InitialContext();
			// TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			// if (debug) {
			// 	response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
			// 	response.getWriter()
			// 			.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
			// 	response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			// }

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			// int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			// if (debug) {
			// 	response.getWriter().println("validateCustomer.proxyType: " + proxyType);
			// 	response.getWriter().println("validateCustomer.proxyHost: " + proxyHost);
			// 	response.getWriter().println("validateCustomer.proxyPort: " + proxyPort);
			// }

			// HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			// DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			// response.getWriter().println("routePlanner: "+routePlanner);

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

			// HttpResponse httpResponse = closableHttpClient.execute(pgPaymentsGet);
			HttpResponse httpResponse = client.execute(pgPaymentsGet);

			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("validatePaymentReq.statusCode: " + statusCode);

			pgPaymentsEntity = httpResponse.getEntity();

			if (pgPaymentsEntity != null) {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder;
				InputSource inputSource;

				String retSrc = EntityUtils.toString(pgPaymentsEntity);
				if (debug)
					response.getWriter().println("validatePaymentReq.retSrc: " + retSrc);

				JsonParser parser = new JsonParser();
				JsonObject pymntTxnObject = (JsonObject) parser.parse(retSrc);

				if (pymntTxnObject.has("d")) {
					if (pymntTxnObject.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						if (pymntTxnObject.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0)
								.getAsJsonObject().get("PGTxnAmount").getAsString().trim()
								.equalsIgnoreCase(txnAmount)) {
							returnMessage = "Success";
							return returnMessage;
						} else {
							returnMessage = "Payment Amount mismatch";
							return returnMessage;
						}
					} else {
						returnMessage = "Details not found for the transaction id";
						return returnMessage;
					}
				} else {
					return retSrc;
				}
			} else {
				returnMessage = "PGPaymentsEntity returned null when trying to connect to the backend";
				return returnMessage;
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
			return returnMessage;
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

			return returnMessage;
		} finally {
			closableHttpClient.close();
		} */
		// return isValidPayment;
	}

	private boolean getCustomers1(HttpServletRequest request, HttpServletResponse response, String loginSessionID,
			String customerNo) throws IOException, URISyntaxException {
		String destURL = "", pgID = "", userName = "", password = "", authParam = "", authMethod = "",
				customerService = "", customerFilter = "", basicAuth = "";
		boolean isValidCustomer = false, debug = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();

		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		// debug = true;
		try {

			if (null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;

			customerFilter = "LoginID eq '" + loginSessionID + "'";

			customerFilter = URLEncoder.encode(customerFilter, "UTF-8");

			customerFilter = customerFilter.replaceAll("%26", "&");
			customerFilter = customerFilter.replaceAll("%3D", "=");

			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */

			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
			// 		.lookup("java:comp/env/connectivityConfiguration");
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
						String.format(
								"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
								DestinationUtils.PCGW_UTILS_OP));
			}

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				// encodedByte = java.util.Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			String sapclient = destConfiguration.get("sap-client").get().toString();
			String service = destConfiguration.get("service").get().toString();

			if (pgID.equalsIgnoreCase("AXISPG")) {
				if (sapclient != null) {
					// CustomerService =
					// destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+
					// CustomerFilter;
					customerService = destURL + "/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client=" + sapclient
							+ "&$filter=" + customerFilter;
				} else {
					customerService = destURL + "/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter=" + customerFilter;
				}
			} else {
				if (null != service && service.equalsIgnoreCase("SFGW")) {
					if (sapclient != null) {
						customerService = destURL + "/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="
								+ sapclient + "&$filter=" + customerFilter;
					} else {
						customerService = destURL + "/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="
								+ customerFilter;
					}
				} else {
					if (sapclient != null) {
						customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client=" + sapclient
								+ "&$filter=" + customerFilter;
					} else {
						customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter=" + customerFilter;
					}
				}
			}

			if (debug) {
				response.getWriter().println("CustomerService: " + customerService);
				response.getWriter().println("destURL: " + destURL);
				response.getWriter().println("authMethod: " + authMethod);
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
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
			userCustomersGet = new HttpGet(customerService);
			// userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount",
			// 		tenantContext.getTenant().getAccount().getId());
			userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
			userCustomersGet.setHeader("Accept", "application/atom+xml");
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);

			int statusCode = httpResponse.getStatusLine().getStatusCode();

			if (debug)
				response.getWriter().println("validateCustomer.statusCode: " + statusCode);

			customerEntity = httpResponse.getEntity();

			if (customerEntity != null) {
				customerName = "";
				customerMobile = "";
				customerEmail = "";

				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder;
				InputSource inputSource;

				String retSrc = EntityUtils.toString(customerEntity);

				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: " + retSrc);

				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
				Document document = docBuilder.parse(inputSource);
				document.getChildNodes().getLength();
				NodeList customerNoList = document.getElementsByTagName("d:CustomerNo");
				NodeList mobileNoList = document.getElementsByTagName("d:Mobile1");
				NodeList emailIDList = document.getElementsByTagName("d:EmailID");
				NodeList customerNameList = document.getElementsByTagName("d:Name");
				// NodeList entryList = document.getElementsByTagName("entry");
				for (int i = 0; i < customerNoList.getLength(); i++) {
					if (customerNo.equalsIgnoreCase(customerNoList.item(i).getTextContent())) {
						customerName = customerNameList.item(i).getTextContent();
						customerMobile = mobileNoList.item(i).getTextContent();
						customerEmail = emailIDList.item(i).getTextContent();

						if (null != request.getParameter("debug")
								&& request.getParameter("debug").equalsIgnoreCase("true")) {
							response.getWriter().println("customerNo: " + customerNo);
							response.getWriter().println(
									"nodeList.item(i).getTextContent(): " + customerNoList.item(i).getTextContent());
							response.getWriter().println("true");

							response.getWriter().println("customerName: " + customerName);
							response.getWriter().println("customerMobile: " + customerMobile);
							response.getWriter().println("customerEmail: " + customerEmail);
						}
						isValidCustomer = true;
						// break;
					}
				}
			}
			EntityUtils.consume(customerEntity);
			return isValidCustomer;
		} catch (RuntimeException e) {
			// customerRequest.abort();
			if (debug) {
				response.getWriter().println("RuntimeException in getCustomers: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}

			return isValidCustomer;
		} catch (ParserConfigurationException e) {
			if (debug) {
				response.getWriter().println("ParserConfigurationException in getCustomers: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			return isValidCustomer;
		} catch (SAXException e) {
			if (debug) {
				response.getWriter().println("SAXException in getCustomers: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			return isValidCustomer;
		} /* catch (NamingException e) {
			if (debug) {
				response.getWriter().println("NamingException in getCustomers: " + e.getMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("Full Stack Trace: " + buffer.toString());
			}
			return isValidCustomer;
		} finally {
			closableHttpClient.close();
		} */
	}

	@SuppressWarnings("finally")
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID,
			String PGID) throws IOException, URISyntaxException {
		String configurableValues = "", basicAuth = "", authMethod = "", destURL = "", userName = "", password = "",
				authParam = "", constantValuesService = "", constantValuesFilter = "";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		// debug = true;
		try {
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;

			// Context tenCtx = new InitialContext();
			// TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			// if (debug) {
			// 	response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
			// 	response.getWriter()
			// 			.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
			// 	response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			// }

			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
			// 		.lookup("java:comp/env/connectivityConfiguration");
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
						String.format(
								"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
								DestinationUtils.PCGW_UTILS_OP));

				return "";
			}

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";

			String pgCatID = "000002";
			constantValuesFilter = constantValuesFilter + "PGID eq '" + PGID + "' and PGCategoryID eq '" + pgCatID
					+ "'";// PGCategoryID
			if (debug)
				response.getWriter().println("constantValuesFilter: " + constantValuesFilter);

			constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
			if (sapclient != null) {
				constantValuesService = destURL + constantValuesService + "?sap-client=" + sapclient + "&$filter="
						+ constantValuesFilter;
			} else {
				constantValuesService = destURL + constantValuesService + "?$filter=" + constantValuesFilter;
			}

			if (debug) {
				response.getWriter().println("pgPaymentConfigs.constantValuesService: " + constantValuesService);
				response.getWriter().println("pgPaymentConfigs.destURL: " + destURL);
			}

			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// if (debug)
			// 	response.getWriter().println("pgPaymentConfigs.proxyType: " + proxyType);
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			// int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			// if (debug) {
			// 	response.getWriter().println("pgPaymentConfigs.proxyHost: " + proxyHost);
			// 	response.getWriter().println("pgPaymentConfigs.proxyPort: " + proxyPort);
			// }

			// HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			// DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			// closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
			configValuesGet = new HttpGet(constantValuesService);
			// configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount",
			// 		tenantContext.getTenant().getAccount().getId());
			configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
			configValuesGet.setHeader("Accept", "application/atom+xml");
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
			HttpResponse httpResponse = client.execute(configValuesGet);

			if (debug) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				response.getWriter().println("pgPaymentConfigs.statusCode: " + statusCode);
			}

			configValuesEntity = httpResponse.getEntity();

			if (configValuesEntity != null) {
				configurableValues = "";
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
					response.getWriter().println("constantEntity is not null");
					response.getWriter().println("PGID: " + PGID);
				}

				if (PGID.equalsIgnoreCase("B2BIZ")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList Customer:
						// "+nodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "ClientCode="
									+ clientCodeList.item(i).getTextContent();
							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("AXISPG")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);
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
					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "Version="
									+ pgParameter1.item(i).getTextContent() + "|" + "Type="
									+ pgParameter2.item(i).getTextContent() + "|" + "RE1="
									+ pgParameter3.item(i).getTextContent() + "|" + "EncryptionKey="
									+ pgParameter4.item(i).getTextContent() + "|" + "secretCode="
									+ secretCode.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent();
							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("YESPAYU")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);
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
					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "Version="
									+ pgParameter1.item(i).getTextContent() + "|" + "PassCode="
									+ pgParameter2.item(i).getTextContent() + "|" + "MCC="
									+ pgParameter3.item(i).getTextContent() + "|" + "EncryptionKey="
									+ pgOwnPublicKey.item(i).getTextContent() + "|" + "SecureSecret="
									+ pgOwnPrivateKey.item(i).getTextContent() + "|" + "BankId="
									+ bankKey.item(i).getTextContent() + "|" + "TerminalId="
									+ clientCode.item(i).getTextContent() + "|" + "Currency="
									+ pgParameter4.item(i).getTextContent() + "|" + "TxnType="
									+ pgParameter5.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent();
							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("EAZYPAY")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
					NodeList secretKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList Customer:
						// "+nodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "ClientCode="
									+ clientCodeList.item(i).getTextContent() + "|" + "SecretKey="
									+ secretKeyList.item(i).getTextContent();
							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("RZRPYROUTE")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);

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

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "PGCategoryID="
									+ pgCategoryList.item(i).getTextContent() + "|" + "APIKey="
									+ apiKey.item(i).getTextContent() + "|" + "SecretKey="
									+ secretKey.item(i).getTextContent() + "|" + "PGProvider="
									+ providerList.item(i).getTextContent() + "|" + "TxnStsURL="
									+ txnURLList.item(i).getTextContent();

							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("RAZORPAY") || PGID.equalsIgnoreCase("RZRPAY")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);

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

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "PGCategoryID="
									+ pgCategoryList.item(i).getTextContent() + "|" + "APIKey="
									+ apiKey.item(i).getTextContent() + "|" + "SecretKey="
									+ secretKey.item(i).getTextContent() + "|" + "PGProvider="
									+ providerList.item(i).getTextContent() + "|" + "TxnStsURL="
									+ txnURLList.item(i).getTextContent();

							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("CCAICICI")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);

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

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "PGCategoryID="
									+ pgCategoryList.item(i).getTextContent() + "|" + "AccessCode="
									+ accessCodeList.item(i).getTextContent() + "|" + "WorkingKey="
									+ workingKeyList.item(i).getTextContent() + "|" + "PGProvider="
									+ providerList.item(i).getTextContent() + "|" + "SchemeCode="
									+ schemeCodeList.item(i).getTextContent() + "|" + "Mode="
									+ pymntModeList.item(i).getTextContent() + "|" + "TxnStsURL="
									+ txnURLList.item(i).getTextContent();

							break;
						}
					}
				} else if (PGID.equalsIgnoreCase("CCAVENUE") || PGID.equalsIgnoreCase("MOBILECCA")
						|| PGID.equalsIgnoreCase("CCA") || PGID.equalsIgnoreCase("CCAICICI")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);

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
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "PGCategoryID="
									+ pgCategoryList.item(i).getTextContent() + "|" + "AccessCode="
									+ accessCodeList.item(i).getTextContent() + "|" + "WorkingKey="
									+ workingKeyList.item(i).getTextContent() + "|" + "PGProvider="
									+ providerList.item(i).getTextContent() + "|" + "Mode="
									+ pymntModeList.item(i).getTextContent() + "|" + "SchemeCode="
									+ schemeCodeList.item(i).getTextContent() + "|" + "TxnStsURL="
									+ txnURLList.item(i).getTextContent();

							break;
						}
					}
				}else if (PGID.equalsIgnoreCase(properties.getProperty("TechProcessPGID"))) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: " + retSrc);

					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);

					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList encryptionIVList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList encryptionKey = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					NodeList txnTypeList = document.getElementsByTagName("d:ClientCode");

					for (int i = 0; i < pgCategoryList.getLength(); i++) {
						// response.getWriter().println("nodeList
						// merchantCodeList:
						// "+merchantCodeList.item(i).getTextContent());
						if (PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent())) {
							configurableValues = configurableValues + "MerchantCode="
									+ merchantCodeList.item(i).getTextContent() + "|" + "PGID="
									+ pdIDList.item(i).getTextContent() + "|" + "WSURL="
									+ aWSURLList.item(i).getTextContent() + "|" + "PGCategoryID="
									+ pgCategoryList.item(i).getTextContent() + "|" + "EncryptionIV="
									+ encryptionIVList.item(i).getTextContent() + "|" + "EncryptionKey="
									+ encryptionKey.item(i).getTextContent() + "|" + "PGProvider="
									+ providerList.item(i).getTextContent() + "|" + "SchemeCode="
									+ schemeCodeList.item(i).getTextContent() + "|" + "Mode="
									+ pymntModeList.item(i).getTextContent() + "|" + "TxnStsURL="
									+ txnURLList.item(i).getTextContent() + "|" + "TxnType="
											+ txnTypeList.item(i).getTextContent();

							break;
						}
					}
				}
			}
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("1 getConstantValues: " + configurableValues);
		} catch (Exception e) {
			if (debug)
				response.getWriter().println("Exception: " + e.getMessage());
		} finally {
			// closableHttpClient.close();
			return configurableValues;
		}
	}

	public String generateSession(HttpServletRequest request, HttpServletResponse response) {
		CommonUtils commonUtils = new CommonUtils();
		String returnValue = "";
		String loginSessionID = "", authMethod = "", loginID = "";
		boolean debug = true;
		try {
			loginID = commonUtils.getAnonymousDestProperties(request, response, NOAUTH_DEST_NAME, "User");
			if (debug)
				response.getWriter().println("loginID: " + loginID);

			authMethod = commonUtils.getAnonymousDestProperties(request, response, NOAUTH_DEST_NAME, "Authentication");
			if (debug)
				response.getWriter().println("authMethod: " + authMethod);

			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				String url = commonUtils.getAnonymousDestProperties(request, response, NOAUTH_DEST_NAME, "URL");
				if (debug)
					response.getWriter().println("url:" + url);

				loginSessionID = commonUtils.createUserSession(request, response, url, loginID, debug);

				if (debug)
					response.getWriter().println("Generating sessionID:" + loginSessionID);
			}

		} catch (Exception e) {
			returnValue = "";
		}

		return "";
	}

	private String getPaymentTransaction(HttpServletRequest request, HttpServletResponse response, String PGID,
			String pgTxnID) throws IOException, URISyntaxException {
		String configurableValues = "", basicAuth = "", sessionID = "", authMethod = "", destURL = "", userName = "",
				password = "", authParam = "", constantValuesService = "", constantValuesFilter = "";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;

		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		// debug = true;
		try {
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;

			sessionID = generateSession(request, response);

			if (debug)
				response.getWriter().println("sessionID: " + sessionID);

			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("getTenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("getTenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */

			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
			// 		.lookup("java:comp/env/connectivityConfiguration");
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
						String.format(
								"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
								DestinationUtils.PCGW_UTILS_OP));

				return "";
			}

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPayments";
			constantValuesFilter = "";

			// String pgCatID="000002";
			constantValuesFilter = constantValuesFilter + "PGID eq '" + PGID + "' and TrackID eq '" + pgTxnID + "'";// PGCategoryID
			if (debug)
				response.getWriter().println("getPaymentTransaction: " + constantValuesFilter);

			constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
			if (sapclient != null) {
				constantValuesService = destURL + constantValuesService + "?sap-client=" + sapclient + "&$filter="
						+ constantValuesFilter;
			} else {
				constantValuesService = destURL + constantValuesService + "?$filter=" + constantValuesFilter;
			}

			if (debug) {
				response.getWriter().println("getPaymentTransaction.constantValuesService: " + constantValuesService);
				response.getWriter().println("getPaymentTransaction.destURL: " + destURL);
			}

			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			if (debug)
				response.getWriter().println("getPaymentTransaction.proxyType: " + proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if (debug) {
				response.getWriter().println("getPaymentTransaction.proxyHost: " + proxyHost);
				response.getWriter().println("getPaymentTransaction.proxyPort: " + proxyPort);
			}

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
			configValuesGet = new HttpGet(constantValuesService);
			// configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount",
			// 		tenantContext.getTenant().getAccount().getId());
			configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
			configValuesGet.setHeader("Accept", "application/atom+xml");
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
			HttpResponse httpResponse = client.execute(configValuesGet);

			if (debug) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				response.getWriter().println("pgPaymentConfigs.statusCode: " + statusCode);
			}

			configValuesEntity = httpResponse.getEntity();

			if (configValuesEntity != null) {
				String retSrc = EntityUtils.toString(configValuesEntity);

				if (debug) {
					response.getWriter().println("pgPaymentConfigs.retSrc: " + retSrc);
				}

				/*
				 * configurableValues = ""; if(null !=
				 * request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true")) {
				 * response.getWriter().println("constantEntity is not null");
				 * response.getWriter().println("PGID: "+PGID); }
				 * 
				 * if(PGID.equalsIgnoreCase("B2BIZ")) { DocumentBuilderFactory
				 * docBuilderFactory = DocumentBuilderFactory.newInstance();
				 * DocumentBuilder docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc); docBuilder =
				 * docBuilderFactory.newDocumentBuilder(); inputSource = new
				 * InputSource(new StringReader(retSrc)); Document document =
				 * docBuilder.parse(inputSource); NodeList merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * pdIDList = document.getElementsByTagName("d:PGID"); NodeList
				 * clientCodeList =
				 * document.getElementsByTagName("d:ClientCode");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList Customer: "+nodeList.
				 * item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues =
				 * configurableValues+"MerchantCode="+merchantCodeList.item(i).
				 * getTextContent()+
				 * "|"+"PGID="+pdIDList.item(i).getTextContent()+
				 * "|"+"WSURL="+aWSURLList.item(i).getTextContent()
				 * +"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
				 * break; } } } else if(PGID.equalsIgnoreCase("AXISPG")) {
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc); docBuilder =
				 * docBuilderFactory.newDocumentBuilder(); inputSource = new
				 * InputSource(new StringReader(retSrc)); Document document =
				 * docBuilder.parse(inputSource); NodeList merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * pdIDList = document.getElementsByTagName("d:PGID"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList pgParameter1 =
				 * document.getElementsByTagName("d:PGParameter1"); NodeList
				 * pgParameter2 =
				 * document.getElementsByTagName("d:PGParameter2"); NodeList
				 * pgParameter3 =
				 * document.getElementsByTagName("d:PGParameter3"); NodeList
				 * pgParameter4 =
				 * document.getElementsByTagName("d:PGParameter4"); NodeList
				 * secretCode = document.getElementsByTagName("d:ClientCode");
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) {
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues =
				 * configurableValues+"MerchantCode="+merchantCodeList.item(i).
				 * getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"Version="+pgParameter1.item(i).getTextContent()
				 * +"|"+"Type="+pgParameter2.item(i).getTextContent()
				 * +"|"+"RE1="+pgParameter3.item(i).getTextContent()
				 * +"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent()
				 * +"|"+"secretCode="+secretCode.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent(); break; } }
				 * } else if(PGID.equalsIgnoreCase("YESPAYU")) {
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc); docBuilder =
				 * docBuilderFactory.newDocumentBuilder(); inputSource = new
				 * InputSource(new StringReader(retSrc)); Document document =
				 * docBuilder.parse(inputSource); NodeList merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * pdIDList = document.getElementsByTagName("d:PGID"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList pgParameter1 =
				 * document.getElementsByTagName("d:PGParameter1"); NodeList
				 * pgParameter2 =
				 * document.getElementsByTagName("d:PGParameter2"); NodeList
				 * pgParameter3 =
				 * document.getElementsByTagName("d:PGParameter3"); NodeList
				 * pgParameter4 =
				 * document.getElementsByTagName("d:PGParameter4"); NodeList
				 * pgParameter5 =
				 * document.getElementsByTagName("d:PGParameter5"); NodeList
				 * pgOwnPublicKey =
				 * document.getElementsByTagName("d:PGOwnPublickey"); NodeList
				 * pgOwnPrivateKey =
				 * document.getElementsByTagName("d:PGOwnPrivatekey"); NodeList
				 * bankKey = document.getElementsByTagName("d:BankKey");
				 * NodeList clientCode =
				 * document.getElementsByTagName("d:ClientCode"); for(int i=0 ;
				 * i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList merchantCodeList: "
				 * +merchantCodeList.item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues =
				 * configurableValues+"MerchantCode="+merchantCodeList.item(i).
				 * getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"Version="+pgParameter1.item(i).getTextContent()
				 * +"|"+"PassCode="+pgParameter2.item(i).getTextContent()
				 * +"|"+"MCC="+pgParameter3.item(i).getTextContent()
				 * +"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
				 * +"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent()
				 * +"|"+"BankId="+bankKey.item(i).getTextContent()
				 * +"|"+"TerminalId="+clientCode.item(i).getTextContent()
				 * +"|"+"Currency="+pgParameter4.item(i).getTextContent()
				 * +"|"+"TxnType="+pgParameter5.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent(); break; } }
				 * } else if(PGID.equalsIgnoreCase("EAZYPAY")) {
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc); docBuilder =
				 * docBuilderFactory.newDocumentBuilder(); inputSource = new
				 * InputSource(new StringReader(retSrc)); Document document =
				 * docBuilder.parse(inputSource); NodeList merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * pdIDList = document.getElementsByTagName("d:PGID"); NodeList
				 * clientCodeList =
				 * document.getElementsByTagName("d:ClientCode"); NodeList
				 * secretKeyList =
				 * document.getElementsByTagName("d:PGOwnPrivatekey");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList Customer: "+nodeList.
				 * item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues =
				 * configurableValues+"MerchantCode="+merchantCodeList.item(i).
				 * getTextContent()+
				 * "|"+"PGID="+pdIDList.item(i).getTextContent()+
				 * "|"+"WSURL="+aWSURLList.item(i).getTextContent()+
				 * "|"+"ClientCode="+clientCodeList.item(i).getTextContent()+
				 * "|"+"SecretKey="+secretKeyList.item(i).getTextContent();
				 * break; } } } else if(PGID.equalsIgnoreCase("RZRPYROUTE")) {
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc);
				 * 
				 * docBuilder = docBuilderFactory.newDocumentBuilder();
				 * inputSource = new InputSource(new StringReader(retSrc));
				 * Document document = docBuilder.parse(inputSource); NodeList
				 * pdIDList = document.getElementsByTagName("d:PGID"); NodeList
				 * pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * merchantCodeList = document.getElementsByTagName("d:PGName");
				 * NodeList apiKey =
				 * document.getElementsByTagName("d:PGOwnPublickey"); NodeList
				 * secretKey =
				 * document.getElementsByTagName("d:PGOwnPrivatekey"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList providerList =
				 * document.getElementsByTagName("d:PGProvider"); NodeList
				 * txnURLList = document.getElementsByTagName("d:TxnStsURL");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList merchantCodeList: "
				 * +merchantCodeList.item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues = configurableValues
				 * +"MerchantCode="+merchantCodeList.item(i).getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
				 * +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
				 * +"|"+"APIKey="+apiKey.item(i).getTextContent()
				 * +"|"+"SecretKey="+secretKey.item(i).getTextContent()
				 * +"|"+"PGProvider="+providerList.item(i).getTextContent()
				 * +"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
				 * 
				 * break; } } } else if(PGID.equalsIgnoreCase("RAZORPAY")) {
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc);
				 * 
				 * docBuilder = docBuilderFactory.newDocumentBuilder();
				 * inputSource = new InputSource(new StringReader(retSrc));
				 * Document document = docBuilder.parse(inputSource);
				 * 
				 * NodeList pdIDList = document.getElementsByTagName("d:PGID");
				 * NodeList pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * merchantCodeList = document.getElementsByTagName("d:PGName");
				 * NodeList apiKey =
				 * document.getElementsByTagName("d:PGOwnPublickey"); NodeList
				 * secretKey =
				 * document.getElementsByTagName("d:PGOwnPrivatekey"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList providerList =
				 * document.getElementsByTagName("d:PGProvider"); NodeList
				 * txnURLList = document.getElementsByTagName("d:TxnStsURL");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList merchantCodeList: "
				 * +merchantCodeList.item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues = configurableValues
				 * +"MerchantCode="+merchantCodeList.item(i).getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
				 * +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
				 * +"|"+"APIKey="+apiKey.item(i).getTextContent()
				 * +"|"+"SecretKey="+secretKey.item(i).getTextContent()
				 * +"|"+"PGProvider="+providerList.item(i).getTextContent()
				 * +"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
				 * 
				 * break; } } }else if(PGID.equalsIgnoreCase("CCAVENUE") ||
				 * PGID.equalsIgnoreCase("MOBILECCA")){ DocumentBuilderFactory
				 * docBuilderFactory = DocumentBuilderFactory.newInstance();
				 * DocumentBuilder docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc);
				 * 
				 * docBuilder = docBuilderFactory.newDocumentBuilder();
				 * inputSource = new InputSource(new StringReader(retSrc));
				 * Document document = docBuilder.parse(inputSource);
				 * 
				 * NodeList pdIDList = document.getElementsByTagName("d:PGID");
				 * NodeList pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * accessCodeList =
				 * document.getElementsByTagName("d:PGOwnPublickey"); NodeList
				 * workingKeyList =
				 * document.getElementsByTagName("d:PGOwnPrivatekey"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList providerList =
				 * document.getElementsByTagName("d:PGProvider"); NodeList
				 * pymntModeList =
				 * document.getElementsByTagName("d:PGParameter1"); NodeList
				 * txnURLList = document.getElementsByTagName("d:TxnStsURL");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList merchantCodeList: "
				 * +merchantCodeList.item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues = configurableValues
				 * +"MerchantCode="+merchantCodeList.item(i).getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
				 * +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
				 * +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
				 * +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
				 * +"|"+"PGProvider="+providerList.item(i).getTextContent()
				 * +"|"+"Mode="+pymntModeList.item(i).getTextContent()
				 * +"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
				 * 
				 * break; } } }else if(PGID.equalsIgnoreCase("CCAICICI")){
				 * DocumentBuilderFactory docBuilderFactory =
				 * DocumentBuilderFactory.newInstance(); DocumentBuilder
				 * docBuilder; InputSource inputSource;
				 * 
				 * String retSrc = EntityUtils.toString(configValuesEntity);
				 * if(null != request.getParameter("debug") &&
				 * request.getParameter("debug").equalsIgnoreCase("true"))
				 * response.getWriter().println("retSrc: "+retSrc);
				 * 
				 * docBuilder = docBuilderFactory.newDocumentBuilder();
				 * inputSource = new InputSource(new StringReader(retSrc));
				 * Document document = docBuilder.parse(inputSource);
				 * 
				 * NodeList pdIDList = document.getElementsByTagName("d:PGID");
				 * NodeList pgCategoryList =
				 * document.getElementsByTagName("d:PGCategoryID"); NodeList
				 * merchantCodeList =
				 * document.getElementsByTagName("d:MerchantCode"); NodeList
				 * accessCodeList =
				 * document.getElementsByTagName("d:PGOwnPublickey"); NodeList
				 * workingKeyList =
				 * document.getElementsByTagName("d:PGOwnPrivatekey"); NodeList
				 * aWSURLList = document.getElementsByTagName("d:PGURL");
				 * NodeList providerList =
				 * document.getElementsByTagName("d:PGProvider"); NodeList
				 * txnURLList = document.getElementsByTagName("d:TxnStsURL");
				 * NodeList schemeCodeList =
				 * document.getElementsByTagName("d:SchemeCode"); NodeList
				 * pymntModeList =
				 * document.getElementsByTagName("d:PGParameter1");
				 * 
				 * for(int i=0 ; i<pgCategoryList.getLength() ; i++) { //
				 * response.getWriter().println("nodeList merchantCodeList: "
				 * +merchantCodeList.item(i).getTextContent());
				 * if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
				 * { configurableValues = configurableValues
				 * +"MerchantCode="+merchantCodeList.item(i).getTextContent()
				 * +"|"+"PGID="+pdIDList.item(i).getTextContent()
				 * +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
				 * +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
				 * +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
				 * +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
				 * +"|"+"PGProvider="+providerList.item(i).getTextContent()
				 * +"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
				 * +"|"+"Mode="+pymntModeList.item(i).getTextContent()
				 * +"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
				 * 
				 * break; } } }
				 */}
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("1 getConstantValues: " + configurableValues);
		} catch (Exception e) {
			if (debug)
				response.getWriter().println("Exception: " + e.getMessage());
		} finally {
			// closableHttpClient.close();
			return configurableValues;
		}
	}

	private void initiateAxisPGRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException {
		CommonUtils commonUtils=new CommonUtils();
		AtomicInteger stepNumber=new AtomicInteger(1);
		try {
			String encryptionKey = "", merchantCode = "", coopParam = "", pgType = "", re1 = "", parameterData = "",
					version = "", axisCurrency = "", keyValue = "", sWSURL = "";
			String referenceID = "", customerRefNo = "", payAmount = "", returnURL = "", ppiParameter = "",
					retParameter2 = "", retParameter3 = "", retParameter4 = "", retParameter5 = "", checksum = "";
			String encryptedData = "";
			boolean isParamMissing = false;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

			String wholeParamString = "", paramName = "", paramValue = "", pgID = "", secretCode = "";
			wholeParamString = configurationValues;
			// response.getWriter().println("configurationValues:
			// "+configurationValues);
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());

				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
					// response.getWriter().println("merchantCode:
					// "+merchantCode);
				}
				if (paramName.equalsIgnoreCase("PGID")) {
					pgID = paramValue;
					// response.getWriter().println("pgID: "+pgID);
				}
				if (paramName.equalsIgnoreCase("WSURL")) {
					sWSURL = paramValue;
					// response.getWriter().println("sWSURL: "+sWSURL);
				}

				if (paramName.equalsIgnoreCase("Version")) {
					version = paramValue;
					// response.getWriter().println("version: "+version);
				}

				if (paramName.equalsIgnoreCase("Type")) {
					pgType = paramValue;
					// response.getWriter().println("pgType: "+pgType);
				}

				if (paramName.equalsIgnoreCase("RE1")) {
					re1 = paramValue;
					// response.getWriter().println("re1: "+re1);
				}

				if (paramName.equalsIgnoreCase("secretCode")) {
					secretCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("EncryptionKey")) {
					// encryptionKey = paramValue;
					if (pgType.equalsIgnoreCase("TEST")) {
						encryptionKey = properties.getProperty("axisEncryptionKeyUAT");
						keyValue = properties.getProperty("checkSumKeyUAT");
					} else {
						encryptionKey = properties.getProperty("axisEncryptionKeyPRD");
						keyValue = properties.getProperty("checkSumKeyPRD");
					}
				}
			}
			// response.getWriter().println("encryptionKey: "+encryptionKey);
			// response.getWriter().println("checksumkey: "+keyValue);
			axisCurrency = properties.getProperty("axisCurrency");
			// response.getWriter().println("Axis method configurationValues:
			// "+configurationValues);

			if (null != request.getParameter("txn-id")) {
				referenceID = request.getParameter("txn-id");
				// referenceID = "123456789";
				/*
				 * long timeSeed = System.nanoTime(); // to get the current date
				 * time value
				 * 
				 * double randSeed = Math.random() * 1000; // random number
				 * generation
				 * 
				 * long midSeed = (long) (timeSeed * randSeed); String s =
				 * midSeed + ""; String subStr = s.substring(0, 9);
				 * 
				 * int finalSeed = Integer.parseInt(subStr); referenceID =
				 * ""+finalSeed;
				 */
				// referenceID = "850510583";
				// response.getWriter().println("referenceID: "+referenceID);
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("CustomerNo")) {
				customerRefNo = request.getParameter("CustomerNo");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("txn-amount")) {
				payAmount = request.getParameter("txn-amount");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("return-url")) {
				//returnURL=removeSpecialCharacter(request.getParameter("return-url"));
				returnURL = request.getParameter("return-url");
			} else {
				isParamMissing = true;
			}

			if (!isParamMissing) {
				ppiParameter = referenceID + "|" + customerRefNo + "|" + payAmount;
			}

			if (null != request.getParameter("RE2")) {
				retParameter2 = request.getParameter("RE2");
			} else {
				retParameter2 = "";
			}

			if (null != request.getParameter("RE3")) {
				retParameter3 = request.getParameter("RE3");
			} else {
				retParameter3 = "";
			}

			if (null != request.getParameter("RE4")) {
				retParameter4 = request.getParameter("RE4");
			} else {
				retParameter4 = "";
			}

			if (null != request.getParameter("RE5")) {
				retParameter5 = request.getParameter("RE5");
			} else {
				retParameter5 = "";
			}

			if (!isParamMissing) {
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
					response.getWriter().println("isParamMissing: " + isParamMissing);
					response.getWriter().println("secretCode: " + secretCode);
					response.getWriter().println("merchantCode: " + merchantCode);
				}

				coopParam = encryptDataForAxisPG(merchantCode, secretCode);

				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("coopParam: " + coopParam);

				String checkSumInput = merchantCode + referenceID + customerRefNo + payAmount + keyValue;
				checksum = generateCheckSum(checkSumInput);
				// response.getWriter().println("checksum: "+checksum);

				parameterData = parameterData + "CID=" + merchantCode + "&RID=" + referenceID + "&CRN=" + customerRefNo
						+ "&AMT=" + payAmount + "&VER=" + version + "&TYP=" + pgType + "&CNY=" + axisCurrency + "&RTU="
						+ returnURL + "&PPI=" + ppiParameter + "&RE1=" + re1 + "&RE2=" + retParameter2 + "&RE3="
						+ retParameter3 + "&RE4=" + retParameter4 + "&RE5=" + retParameter5 + "&CKS=" + checksum;
				// response.getWriter().println("parameterData:
				// "+parameterData);
				JsonObject errorObj=new JsonObject();
				errorObj.addProperty("CID", merchantCode);
				errorObj.addProperty("RID", referenceID);
				errorObj.addProperty("CRN", customerRefNo);
				errorObj.addProperty("AMT", payAmount);
				errorObj.addProperty("VER", version);
				errorObj.addProperty("TYP", pgType);
				errorObj.addProperty("CNY", axisCurrency);
				errorObj.addProperty("RTU", returnURL);
				errorObj.addProperty("PPI", ppiParameter);
				errorObj.addProperty("RE1", re1);
				errorObj.addProperty("RE2", retParameter2);
				errorObj.addProperty("RE3", retParameter3);
				errorObj.addProperty("RE4", retParameter4);
				errorObj.addProperty("RE5", retParameter5);
				errorObj.addProperty("CKS", checksum);
				
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), referenceID, errorObj+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

				encryptedData = encryptDataForAxisPG(parameterData, encryptionKey);
				// response.getWriter().println("encryptedData:
				// "+encryptedData);

				if (encryptedData != null && encryptedData.trim().length() > 0) {
					JsonObject result = new JsonObject();
					result.addProperty("i", encryptedData);
					result.addProperty("coop", coopParam);
					result.addProperty("Valid", "true");
					// result.addProperty("WSURL","https://uat-etendering.axisbank.co.in/easypay2.0/frontend/index.php/api/payment");
					result.addProperty("WSURL", sWSURL);
					result.addProperty("parameters", "i|coop");
					result.addProperty("payLoad", parameterData);
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), referenceID, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
					if(appLogMsgArry.size()>0){
						JsonObject appLogMsg=new JsonObject();
						JsonObject onObjEvent=new JsonObject();
						appLogMsg.addProperty("Object", "ApplicationLog");
						appLogMsg.addProperty("Event", "INSERT");
						appLogMsg.addProperty("AggregatorID", destAggrID);
						appLogMsg.addProperty("Identifier", "000002");
						appLogMsg.add("Message", appLogMsgArry);
						onObjEvent.add("OnObjectEvent", appLogMsg);
						commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
					}
					response.getWriter().print(new Gson().toJson(result));
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("Valid", "false");
					result.addProperty("pgReqMsg", "Error in processing the request. Please contact system admin.");
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), referenceID, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
					if(appLogMsgArry.size()>0){
						JsonObject appLogMsg=new JsonObject();
						JsonObject onObjEvent=new JsonObject();
						appLogMsg.addProperty("Object", "ApplicationLog");
						appLogMsg.addProperty("Event", "INSERT");
						appLogMsg.addProperty("AggregatorID", destAggrID);
						appLogMsg.addProperty("Identifier", "000002");
						appLogMsg.add("Message", appLogMsgArry);
						onObjEvent.add("OnObjectEvent", appLogMsg);
						commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
					}
					response.getWriter().print(new Gson().toJson(result));
				}
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("ParamsMissing", "Some mandatory parameters are missing");
				result.addProperty("Valid", "false");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), referenceID, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			}
		} catch (Exception e) {
			
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "--->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage());
			}
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id") + "", buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);
			response.getWriter().print("initiateAxisPGRequest Error: " + e.getMessage());
		}
	}

	public String encryptDataForAxisPG(String parameterData, String encryptionKey) {
		byte[] encrypted = null;
		try {
			SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			encrypted = cipher.doFinal(parameterData.getBytes());
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			return new String(Base64.getEncoder().encodeToString(encrypted));
		}
	}

	public String generateCheckSum(String checkSumInput) throws NoSuchAlgorithmException {
		StringBuffer hexString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(checkSumInput.getBytes());

			byte byteData[] = md.digest();

			// convert the byte to hex format method 2
			hexString = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				String hex = Integer.toHexString(0xff & byteData[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			// System.out.println("Hex format : " + hexString.toString());
			return hexString.toString();
		} catch (Exception e) {
			// TODO: handle exception

		} finally {
			return hexString.toString();
		}
	}

	private void initiateRazorPayPGRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException, JSONException {

		// this code will go for Razor pay changes CA1-T2061 changes in razorpay payment mode in payment application shold not move this code for apollo razor pay response
		JsonObject responseObject = new JsonObject();
		String apiKey = "", merchantName = "", txnFor = "", orderID = "", returnURL = "", txnAmount = "", txnID = "",
				csrfToken = "", pgHdrGUID = "", companyCode = "", clearingDocCompanyCodeID = "", loginId = "";
		boolean isParamMissing = false, debug = false, updateSuccess = false;
		Properties properties = new Properties();
		CommonUtils commonUtils=new CommonUtils();
		AtomicInteger stepNumber=new AtomicInteger(1);
		if (request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")) {
			debug = true;
		}
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

			Order order = generateOrderId(request, response, configurationValues, properties,stepNumber,appLogObj,appLogMsgArry);
			if (order != null) {
				if (order.has("id") && order.get("id").toString().trim().length() > 0) {
					orderID = order.get("id").toString();
					companyCode = request.getParameter("CompanyCode");
					clearingDocCompanyCodeID = request.getParameter("ClearingDocCompanyCodeID");
					loginId = request.getParameter("LoginID");
					returnURL = request.getParameter("return-url");
					//returnURL=removeSpecialCharacter(request.getParameter("return-url"));
					if (returnURL != null) {
						String[] resSplitResult = returnURL.split("\\~");
						String resParamName = "", resParamValue = "";
						for (String s : resSplitResult) {
							if (debug) {
								response.getWriter().println("q: " + s);
							}
							resParamName = s.substring(0, 5);
							resParamValue = s.substring(5, s.length());
							if(debug){
							response.getWriter().println("resParamName: " + resParamName);
							response.getWriter().println("resParamValue: " + resParamValue);
							}
							if (resParamName.equalsIgnoreCase("PGGID")) {
								pgHdrGUID = resParamValue;
							}

						}
					}
					if (debug) {
						response.getWriter().println("pgHdrGUID " + pgHdrGUID);
					}
					csrfToken = generateCSRFTokenForPCGW(request, response, debug);
					if (debug) {
						response.getWriter().println("csrfToken " + csrfToken);
					}
					if (!csrfToken.equalsIgnoreCase("") && !pgHdrGUID.equalsIgnoreCase("")) {
						String updateTxnRes = updatePGTransaction(request, response, pgHdrGUID, orderID, PG_TXN_STATUS,
								PAYMENT_STATUS, csrfToken, debug);
						if(debug){
							response.getWriter().println("updateTxnRes:"+updateTxnRes);
						}
						if (updateTxnRes.equalsIgnoreCase("204")) {
							updateSuccess = true;
						} else {
							commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), updateTxnRes+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
							
							if(appLogMsgArry.size()>0){
								JsonObject appLogMsg=new JsonObject();
								JsonObject onObjEvent=new JsonObject();
								appLogMsg.addProperty("Object", "ApplicationLog");
								appLogMsg.addProperty("Event", "INSERT");
								appLogMsg.addProperty("AggregatorID", destAggrID);
								appLogMsg.addProperty("Identifier", "000002");
								appLogMsg.add("Message", appLogMsgArry);
								onObjEvent.add("OnObjectEvent", appLogMsg);
								commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
							}
							
							responseObject.addProperty("Message", "Update PG Transaction was Failed");
							responseObject.addProperty("Status", "000002");
							responseObject.addProperty("ErrorCode", "J002");
							response.getWriter().print(new Gson().toJson(responseObject));

						}

					} else {
						if (csrfToken.equalsIgnoreCase("")) {
							responseObject.addProperty("Message", "Unable to Generate csrfToken");
						}
						if (pgHdrGUID.equalsIgnoreCase("")) {
							responseObject.addProperty("Message", "pgHdrGUID value is null");
						}
						responseObject.addProperty("Status", "000002");
						responseObject.addProperty("ErrorCode", "J002");
						
						commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
					
						if(appLogMsgArry.size()>0){
							JsonObject appLogMsg=new JsonObject();
							JsonObject onObjEvent=new JsonObject();
							appLogMsg.addProperty("Object", "ApplicationLog");
							appLogMsg.addProperty("Event", "INSERT");
							appLogMsg.addProperty("AggregatorID", destAggrID);
							appLogMsg.addProperty("Identifier", "000002");
							appLogMsg.add("Message", appLogMsgArry);
							onObjEvent.add("OnObjectEvent", appLogMsg);
							commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
						}
						
						response.getWriter().print(new Gson().toJson(responseObject));

					}
				}
			} else {
				responseObject.addProperty("Message", "Unable to Generate order_id");
				responseObject.addProperty("Status", "000002");
				responseObject.addProperty("ErrorCode", "J002");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(responseObject));
			}

			if (updateSuccess) {
				if (null != request.getParameter("txn-id")) {
					txnID = request.getParameter("txn-id");
				} else {
					isParamMissing = true;
				}

				if (orderID.equals("")) {
					isParamMissing = true;
				}else{
					isParamMissing = false;
					
				}

				if (null != request.getParameter("return-url")) {
					returnURL = request.getParameter("return-url");
					//returnURL=removeSpecialCharacter(request.getParameter("return-url"));
					returnURL = returnURL + "&id=" + Base64.getEncoder().encodeToString(orderID.getBytes());
				} else {
					isParamMissing = true;
				}

				if (null != request.getParameter("txn-for")) {
					txnFor = request.getParameter("txn-for");
				} else {
					isParamMissing = true;
				}

				if (null != request.getParameter("txn-amount")) {
					txnAmount = request.getParameter("txn-amount");
				} else {
					isParamMissing = true;
				}

				if (debug) {
					response.getWriter().println("OrderID: " + orderID);
					response.getWriter().println("ReturnURL: " + returnURL);
					response.getWriter().println("TxnFor: " + txnFor);
					response.getWriter().println("TxnAmount: " + txnAmount);
					response.getWriter().println("TxnID: " + txnID);
					response.getWriter().println("isParamMissing: " + isParamMissing);
				}
				if (!isParamMissing) {
					String wholeParamString = "", paramName = "", paramValue = "", pgID = "", WSURL = "",
							pgCatogory = "", pgProvider = "";
					wholeParamString = configurationValues;
					String[] splitResult = wholeParamString.split("\\|");
					for (String s : splitResult) {
						paramName = s.substring(0, s.indexOf("="));
						paramValue = s.substring(s.indexOf("=") + 1, s.length());
						if (paramName.equalsIgnoreCase("MerchantCode")) {
							merchantName = paramValue;
						}
						if (paramName.equalsIgnoreCase("PGID")) {
							pgID = paramValue;
						}
						if (paramName.equalsIgnoreCase("WSURL")) {
							WSURL = paramValue;
						}
						if (paramName.equalsIgnoreCase("APIKey")) {
							apiKey = paramValue;
						}

						if (paramName.equalsIgnoreCase("PGCategoryID")) {
							pgCatogory = paramValue;
						}

						if (paramName.equalsIgnoreCase("PGProvider")) {
							pgProvider = paramValue;
						}

						if (paramName.equalsIgnoreCase("WSURL")) {
							WSURL = paramValue;
						}
						 
					}
					if (debug) {
						response.getWriter().println("pgCatogory: " + pgCatogory);
						response.getWriter().println("pgProvider: " + pgProvider);
						response.getWriter().println("merchantCode: " + merchantName);
						response.getWriter().println("PGID: " + pgID);
						response.getWriter().println("WSURL: " + WSURL);
						// response.getWriter().println("txnURL: "+txnURL);
						response.getWriter().println("merchantCode: " + merchantName);
					}
                     JsonObject returnObject=new JsonObject();
                     returnObject.addProperty("key_id", apiKey);
                     returnObject.addProperty("name", merchantName);
                     returnObject.addProperty("description", txnFor);
                     returnObject.addProperty("order_id", orderID);
                     returnObject.addProperty("prefill[name]", customerName);
                     returnObject.addProperty("prefill[contact]", customerMobile);
                     returnObject.addProperty("prefill[email]", customerEmail);
                     returnObject.addProperty("callback_url", returnURL);
                     returnObject.addProperty("WSURL", WSURL);
                     returnObject.addProperty("parameters",
							"key_id|name|description|order_id|callback_url|prefill[name]|prefill[contact]|prefill[email]");
                     returnObject.addProperty("Valid", true);
                     commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), returnObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
                    
                     if(appLogMsgArry.size()>0){
         				JsonObject appLogMsg=new JsonObject();
         				JsonObject onObjEvent=new JsonObject();
         				appLogMsg.addProperty("Object", "ApplicationLog");
         				appLogMsg.addProperty("Event", "INSERT");
         				appLogMsg.addProperty("AggregatorID", destAggrID);
         				appLogMsg.addProperty("Identifier", "000002");
         				appLogMsg.add("Message", appLogMsgArry);
         				onObjEvent.add("OnObjectEvent", appLogMsg);
         				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
         			}
                     
                     response.getWriter().print(new Gson().toJson(returnObject));
				} else {
					responseObject.addProperty("ParamsMissing", "Some mandatory parameters are missing");
					responseObject.addProperty("Valid", "false");
					
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
					
					if(appLogMsgArry.size()>0){
						JsonObject appLogMsg=new JsonObject();
						JsonObject onObjEvent=new JsonObject();
						appLogMsg.addProperty("Object", "ApplicationLog");
						appLogMsg.addProperty("Event", "INSERT");
						appLogMsg.addProperty("AggregatorID", destAggrID);
						appLogMsg.addProperty("Identifier", "000002");
						appLogMsg.add("Message", appLogMsgArry);
						onObjEvent.add("OnObjectEvent", appLogMsg);
						commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
					}
					
					response.getWriter().print(new Gson().toJson(responseObject));
				}
			}
			
		} catch (Exception e) {
			if (e instanceof RazorpayException) {
				responseObject.addProperty("Exception", e.getClass().getCanonicalName());
				responseObject.addProperty("ErrorCode", "001");
				responseObject.addProperty("Status", "000002");
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);

				}
				responseObject.addProperty("Message",
						e.getClass().getCanonicalName() + "--->" + e.getMessage() + "--->" + buffer.toString());
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().println(new Gson().toJson(responseObject));

			} else if (e instanceof JSONException) {
				responseObject.addProperty("Exception", e.getClass().getCanonicalName());
				responseObject.addProperty("ErrorCode", "001");
				responseObject.addProperty("Status", "000002");
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);

				}
				responseObject.addProperty("Message",
						e.getClass().getCanonicalName() + "--->" + e.getMessage() + "--->" + buffer.toString());
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().println(new Gson().toJson(responseObject));
			} else {
				responseObject.addProperty("ErrorCode", "001");
				responseObject.addProperty("ErrorMessage", e.getLocalizedMessage());
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				responseObject.addProperty("Status", "000002");
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);

				}
				responseObject.addProperty("Message",
						e.getClass().getCanonicalName() + "--->" + e.getMessage() + "--->" + buffer.toString());
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), responseObject+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(responseObject));
			}
		}
	}

	public String updatePGTransaction(HttpServletRequest request, HttpServletResponse response, String pgHdrGUID,
			String pgTxnRefNo, String pgTxnStatus, String paymentStatus, String csrfToken, boolean debug)
			throws IOException {
		JsonObject responseJsonObject = new JsonObject();
		String destURL = "", pgID = "", userName = "", password = "", authParam = "", authMethod = "",
				paymentConfigService = "", paymentUpdateQuery = "", basicAuth = "", sapclient = "", sessionID = "",
				loginMethod = "", txnAmount = "", cookie = "";
		String returnMessage = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();

		HttpPut pgPymntUpdate = null;
		HttpEntity pgPymntUpdateEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;

		try {

			destConfiguration = commonUtils.getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (debug) {
				response.getWriter().println("updatePGTransaction.pgHdrGUID:" + pgHdrGUID);
				response.getWriter().println("updatePGTransaction.pgTxnStatus:" + pgTxnStatus);
			}

			String loginID = commonUtils.getUserPrincipal(request, "name", response);

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

			// paymentUpdateQuery = destURL + "/sap/opu/odata/ARTEC/PCGW/PGPayments(guid'" + pgHdrGUID + "')";
			if(sapclient != null && sapclient.trim().length() > 0)
				paymentUpdateQuery = destURL + "/sap/opu/odata/ARTEC/PCGW/PGPayments(guid'" + pgHdrGUID + "')?sap-client="+sapclient;
			else
				paymentUpdateQuery = destURL + "/sap/opu/odata/ARTEC/PCGW/PGPayments(guid'" + pgHdrGUID + "')";
			
				// paymentConfigService =
			// destURL+"/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigStats?sap-client="+
			// sapclient +"&$filter="+ paymentConfigFilter;
			// paymentConfigFilter = "CHGuid eq guid'"+configHdrGuid+"' and
			// PGTxnStatus eq '"+pgTxnStatus+"'";
			if (debug)
				response.getWriter().println("paymentUpdateQuery:" + paymentUpdateQuery);

			/*
			 * paymentUpdateQuery = URLEncoder.encode(paymentUpdateQuery,
			 * "UTF-8");
			 * 
			 * paymentUpdateQuery = paymentUpdateQuery.replaceAll("%26", "&");
			 * paymentUpdateQuery = paymentUpdateQuery.replaceAll("%3D", "=");
			 */

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
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			// int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			// if (debug) {
			// 	response.getWriter().println("updatePGTransaction.proxyType: " + proxyType);
			// 	response.getWriter().println("updatePGTransaction.proxyHost: " + proxyHost);
			// 	response.getWriter().println("updatePGTransaction.proxyPort: " + proxyPort);
			// 	response.getWriter().println("updatePGTransaction.csrfToken: " + csrfToken);
			// 	// response.getWriter().println("updatePGTransaction.Cookie:
			// 	// "+request.getHeader("Cookie"));
			// }
			// cookie = request.getHeader("Cookie");
			// String[] splitResult = cookie.split(";");

			String key = "", value = "", prefix = "JTENANTSESSIONID_", compareKey = "", tenantSessionIDValue = "";
			/* Commented for CF
			compareKey = prefix + tenantContext.getTenant().getAccount().getId();
			for (String s : splitResult) {
				key = s.substring(0, s.indexOf("="));
				value = s.substring(s.indexOf("=") + 1, s.length());
				if(debug)
				response.getWriter().println("Cookie Key: " + key + "|Value: " + value);

				if (key.equalsIgnoreCase(compareKey)) {
					tenantSessionIDValue = value;
					if(debug)
					response.getWriter().println("Required value: " + tenantSessionIDValue);
				}
			}

			if (debug)
				response.getWriter().println("tenantSessionIDValue: " + prefix + tenantSessionIDValue); */

			// HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			// DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			// // response.getWriter().println("routePlanner: "+routePlanner);

			// closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();

			JSONObject updatePayLoad = new JSONObject();
			updatePayLoad.accumulate("PGPaymentGUID", pgHdrGUID);
			updatePayLoad.accumulate("PGTransactionID", pgTxnRefNo);
			updatePayLoad.accumulate("PGTxnStatusID", pgTxnStatus);
			updatePayLoad.accumulate("LoginID", sessionID);

			updatePayLoad.accumulate("TestRun", false);

			// prefix = "SAP_SESSIONID_GWQ_300="+tenantSessionIDValue+";
			// Path=/;";

			if (debug) {
				response.getWriter().println("updatePayLoad: " + updatePayLoad);
				// response.getWriter().println("prefix: "+prefix);
			}

			pgPymntUpdateEntity = new StringEntity(updatePayLoad.toString());

			pgPymntUpdate = new HttpPut(paymentUpdateQuery);

			// pgPymntUpdate.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			pgPymntUpdate.setHeader("Content-Type", "application/json; charset=utf-8");
			pgPymntUpdate.setHeader("Accept", "application/json");
			pgPymntUpdate.setHeader("x-csrf-token", csrfToken);
			pgPymntUpdate.setHeader("x-arteria-loginid", sessionID);
			// pgPymntUpdate.setHeader("X-Requested-With", "XMLHttpRequest");
			// pgPymntUpdate.setHeader("Cookie", prefix);
			// pgPymntUpdate.setHeader("Cookie", request.getHeader("Cookie"));
			// pgPymntUpdate.setHeader("Set-Cookie",
			// prefix+tenantSessionIDValue);

			// pgPymntUpdate.ad

			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				pgPymntUpdate.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				pgPymntUpdate.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			pgPymntUpdate.setEntity(pgPymntUpdateEntity);

			HttpClientContext httpClientContext = HttpClientContext.create();
			// CookieStore cookieStore = httpClientContext.getCookieStore();
			httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);
			// HttpResponse httpResponse = closableHttpClient.execute(pgPymntUpdate, httpClientContext);
			HttpResponse httpResponse = client.execute(pgPymntUpdate, httpClientContext);
			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("validatePaymentReq.statusCode: " + statusCode);

			pgPymntUpdateEntity = httpResponse.getEntity();

			if (statusCode != HttpServletResponse.SC_NO_CONTENT) {
				String retSrc = EntityUtils.toString(pgPymntUpdateEntity);
				if (debug)
					response.getWriter().println("updatePGTransaction.retSrc: " + retSrc);

				return retSrc;
			} else {
				return "" + statusCode;
			}
		} catch (Exception e) {
			responseJsonObject = new JsonObject();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug) {
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
				response.getWriter().println("updatePGTransaction.getMessage: " + e.getMessage());
				response.getWriter().println("updatePGTransaction.getClass: " + e.getClass());
				response.getWriter().println("updatePGTransaction.getCause: " + e.getCause());
			}
			responseJsonObject.addProperty("Status", "000002");
			responseJsonObject.addProperty("Message", e.getMessage() + ">" + e.getClass());
			responseJsonObject.addProperty("Full Trace", buffer.toString());
			responseJsonObject.addProperty("ErrorCode", "J002");
			returnMessage = "Exception: " + e.getMessage() + "|e.getClass()-" + e.getCause();
			return returnMessage;
		}
	}

	private String generateCSRFTokenForPCGW(HttpServletRequest request, HttpServletResponse response, boolean debug)
			throws IOException, Exception {
		JsonObject responseJsonObject = new JsonObject();
		String destURL = "", pgID = "", userName = "", password = "", authParam = "", authMethod = "",
				csrfTokenService = "", csrfTokenServiceURL = "", basicAuth = "", csrfToken = "", sapclient = "";

		boolean csrfFetchSuccess = false;

		String returnMessage = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();

		HttpGet csrfTokenGet = null;
		HttpEntity csrfFetchEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;
		try {

			destConfiguration = commonUtils.getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			String loginID = commonUtils.getUserPrincipal(request, "name", response);

			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (debug) {
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}

			// csrfTokenService = destURL + "/sap/opu/odata/ARTEC/PCGW";
			if(sapclient != null && sapclient.trim().length() > 0)
				csrfTokenService = destURL + "/sap/opu/odata/ARTEC/PCGW?sap-client="+sapclient;
			else
				csrfTokenService = destURL + "/sap/opu/odata/ARTEC/PCGW";

			if (debug)
				response.getWriter().println("csrfTokenService: " + csrfTokenService);

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
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				basicAuth = "Bearer " + authParam;
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
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */

			csrfTokenGet = new HttpGet(csrfTokenService);
			// csrfTokenGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			csrfTokenGet.setHeader("x-csrf-token", "Fetch");
			// csrfTokenGet.setHeader("Accept-Encoding", "gzip, deflate, br");
			// csrfTokenGet.setHeader("x-csrf-token", "Fetch");
			csrfTokenGet.setHeader("X-Requested-With", "XMLHttpRequest");

			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				csrfTokenGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				csrfTokenGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// if(debug)
			// response.getWriter().println("csrfTokenGet: "+csrfTokenGet);

			HttpClientContext httpClientContext = HttpClientContext.create();
			// HttpResponse httpResponse = closableHttpClient.execute(csrfTokenGet, httpClientContext);
			HttpResponse httpResponse = client.execute(csrfTokenGet, httpClientContext);

			globalCookieStore = httpClientContext.getCookieStore();
			httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);

			try {
				List<Cookie> cookies = globalCookieStore.getCookies();
				/*
				 * if (cookies.isEmpty()) {
				 * response.getWriter().println("No cookies"); } else { for (int
				 * i = 0; i < cookies.size(); i++) {
				 * response.getWriter().println("Got Cookies toString: " +
				 * cookies.get(i).toString());
				 * response.getWriter().println("Got Cookies getName: " +
				 * cookies.get(i).getName());
				 * response.getWriter().println("Got Cookies getValue: " +
				 * cookies.get(i).getValue()); } }
				 */
				// EntityUtils.consume(httpResponse.getEntity());
			} finally {
				// httpResponse.getClass()
			}

			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();

			Header[] headers = httpResponse.getAllHeaders();
			for (Header header : headers) {
				// response.getWriter().println("Key [" + header.getName() + "],
				// Value[" + header.getValue() + " ]");

				if (header.getName().trim().equalsIgnoreCase("x-csrf-token")) {
					csrfToken = header.getValue().toString();
					csrfFetchSuccess = true;
					// break;
				}
			}

			if (!csrfFetchSuccess) {
				csrfToken = "Failure";
			}

			if (debug) {
				// response.getWriter().println("csrfTokenGet.statusCode:
				// "+statusCode);
				response.getWriter().println("csrfTokenGet.csrfToken: " + csrfToken);
			}

			csrfFetchEntity = httpResponse.getEntity();

			if (csrfFetchEntity != null) {
				String retSrc = EntityUtils.toString(csrfFetchEntity);
				// if(debug)
				// response.getWriter().println("csrfFetchEntity.retSrc:
				// "+retSrc);
			} /*
				 * else { returnMessage =
				 * "PGPymntConfigStatsEntity returned null when trying to connect to the backend"
				 * ; return returnMessage; }
				 */

		} catch (Exception e) {
			throw e;

		}

		return csrfToken;
	}

	private Order generateOrderId(HttpServletRequest request, HttpServletResponse response, String configurationValues,
			Properties properties,AtomicInteger stepNumber,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException, JSONException, Exception {
		String apiKey = "", secret = "", orderID = "", wholeParamString = "", paramName = "", paramValue = "",
				txnAmount = "", txnID = "", accountID = "", currency = "";
		boolean debug = false;
		String pgID = "";
		Order order = null;
		accountID = request.getParameter("account-id");
		String debugParam = request.getParameter("debug");
		if (debugParam!=null  && debugParam.equalsIgnoreCase("true")) {
			debug = true;
		}
		CommonUtils commonUtils=new CommonUtils();
		try {
			if (null != request.getParameter("txn-id")) {
				txnID = request.getParameter("txn-id");
			}
			if (null != request.getParameter("pay-for")) {
				currency = request.getParameter("pay-for");
			}
			if (null != request.getParameter("PGID")) {
				pgID = request.getParameter("PGID");
			}
			if (null != request.getParameter("txn-amount")) {
				txnAmount = request.getParameter("txn-amount");
				BigDecimal txnAmt = new BigDecimal(txnAmount);
				txnAmt = txnAmt.multiply(new BigDecimal(100));
				txnAmount = txnAmt.toBigInteger().toString();
			}
			if (debug) {
				response.getWriter().println("generateRazorOrderID.txnAmount: " + txnAmount);
				response.getWriter().println("generateRazorOrderID.pgID: " + pgID);
				response.getWriter().println("generateRazorOrderID.currency: " + currency);
				response.getWriter().println("generateRazorOrderID.txnID: " + txnID);
			}
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());

				if (paramName.equalsIgnoreCase("APIKey"))
					apiKey = paramValue;

				if (paramName.equalsIgnoreCase("SecretKey"))
					secret = paramValue;
			}
			if (debug) {
				response.getWriter().println("generateRazorOrderID.apiKey: " + apiKey);
				response.getWriter().println("generateRazorOrderID.secret: " + secret);
			}
            JSONObject notes=new JSONObject();
            if(request.getParameter("wallet-user-code")!=null){
            	notes.put("CustomerCode", request.getParameter("wallet-user-code"));
            }else{
            	notes.put("CustomerCode","");
            }
            if(request.getParameter("CustomerNo")!=null){
            	notes.put("CustomerNo", request.getParameter("CustomerNo"));
            }else{
            	notes.put("CustomerNo","");
            }
            if(request.getParameter("txn-id")!=null){
            	notes.put("SAPTrackID", request.getParameter("txn-id"));
            }else{
            	notes.put("SAPTrackID","");
            }
            if(request.getParameter("SourceID")!=null){
            	notes.put("DeviceID", request.getParameter("SourceID"));
            }else{
            	notes.put("DeviceID","");
            }
			RazorpayClient razorpayClient = new RazorpayClient(apiKey, secret);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("notes", notes);
			orderRequest.put("amount", txnAmount);
			orderRequest.put("currency", currency);
			orderRequest.put("receipt", txnID);
			orderRequest.put("payment_capture", true);
			if(debug){
				response.getWriter().println("pgID: " + pgID);
			}
			if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
				if (debug)
					response.getWriter().println("generateRazorOrderID.accountID: " + accountID);
				JSONArray transfers = new JSONArray();
				JSONObject transfer = new JSONObject();
				transfer.put("amount", txnAmount);
				transfer.put("currency", currency);
			    transfer.put("account", accountID); 
			    transfers.put(transfer);
				orderRequest.put("transfers", transfers);
			}
			
			if(debug){
				response.getWriter().println("Order Object");
				response.getWriter().println(orderRequest);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, orderRequest+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

			order = razorpayClient.Orders.create(orderRequest);
			/*
			 * if (order.has("id") && order.get("id").toString().trim().length()
			 * > 0) { orderID = order.get("id").toString();
			 * razorPayResponse.addProperty("orderid", orderID);
			 * razorPayResponse.addProperty("Status", "000001");
			 * razorPayResponse.addProperty("Message", "Success");
			 * response.getWriter().println(razorPayResponse); } else { orderID
			 * = ""; razorPayResponse.addProperty("orderid", orderID);
			 * razorPayResponse.addProperty("Status", "000002");
			 * razorPayResponse.addProperty("Message",
			 * "Unable to generate order-id");
			 * response.getWriter().println(razorPayResponse); }
			 */
			if (debug) {
				response.getWriter().println("order.toJson: " + order.toJson());
				response.getWriter().println("order.amount: " + order.get("amount"));
				response.getWriter().println("order.currency: " + order.get("currency"));
				response.getWriter().println("order.receipt: " + order.get("receipt"));
				response.getWriter().println("order.transfers: " + order.get("transfers"));
				response.getWriter().println("order.orderID: " + order.get("id"));
			}
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, order.toJson()+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

		} catch (RazorpayException e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				
				
				response.getWriter().println("RazorpayException:" + buffer.toString());
			}
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName()+"---->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"---->");
			}
			
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, buffer.toString()+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
			throw e;
		} catch (JSONException e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("JSONException:" + buffer.toString());
			}
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName()+"---->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"---->");
			}
			
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, buffer.toString()+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
			
			throw e;
		} catch (Exception e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().println("generateRazorOrderID-Exception:" + buffer.toString());
			}
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName()+"---->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"---->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, buffer.toString()+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
			
			throw e;

		}
		return order;
	}

	private void initiateCCAvenuePGRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException, JSONException {
		String merchantCode = "", workingKey = "", accessCode = "", txnUrl = "", txnRefNo = "", txnAmount = "",
				currency = "", returnURL = "", customerCode = "", companyCode = "", customerInfo = "", schemeCode = "",
				schemeMode = "", consumerMobile = "";
		boolean debug = false;
		CommonUtils commonUtils=new CommonUtils();
		AtomicInteger stepNumber=new AtomicInteger(1);
		try {
			// debug = false;
			if (request.getParameter("debug") != null
					&& request.getParameter("debug").trim().equalsIgnoreCase("true")) {
				debug = true;
			}

			// if(null != request.getParameter("PGID"))
			// pgID = request.getParameter("PGID");

			String wholeParamString = "", paramName = "", paramValue = "", pgID = "";
			wholeParamString = configurationValues;

			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());

				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("SchemeCode")) {
					schemeCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("PGID")) {
					pgID = paramValue;
				}

				if (paramName.equalsIgnoreCase("WSURL")) {
					txnUrl = paramValue;
				}

				if (paramName.equalsIgnoreCase("WorkingKey")) {
					workingKey = paramValue;
				}

				if (paramName.equalsIgnoreCase("AccessCode")) {
					accessCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("Mode")) {
					schemeMode = paramValue;
				}
			}

			currency = "INR";
			boolean isParamMissing = false;
			// sujai
			// Validations
			if (null != request.getParameter("txn-id") && request.getParameter("txn-id").trim().length() > 0) {
				txnRefNo = request.getParameter("txn-id");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("txn-amount") && request.getParameter("txn-amount").trim().length() > 0) {
				txnAmount = request.getParameter("txn-amount");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("return-url") && request.getParameter("return-url").trim().length() > 0) {
				returnURL = request.getParameter("return-url");
				//returnURL=removeSpecialCharacter(request.getParameter("return-url"));
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("CompanyCode")
					&& request.getParameter("CompanyCode").trim().length() > 0) {
				companyCode = request.getParameter("CompanyCode");
			} else {
				if (!pgID.equalsIgnoreCase("CCAICICI") && !pgID.equalsIgnoreCase("CCA"))
					isParamMissing = true;
			}

			if (null != request.getParameter("CustomerNo") && request.getParameter("CustomerNo").trim().length() > 0) {
				customerCode = request.getParameter("CustomerNo");
			} else {
				isParamMissing = true;
			}

			if (null != request.getParameter("consumer-mobile")
					&& request.getParameter("consumer-mobile").trim().length() > 0)
				consumerMobile = request.getParameter("consumer-mobile");

			if (destAggrID.equalsIgnoreCase("AGGR0108"))
				pgGuid = returnURL.substring(returnURL.indexOf("~PGGID") + 6, returnURL.indexOf("~STEID"));

			if (debug) {
				response.getWriter().println("txnRefNo: " + txnRefNo);
				response.getWriter().println("txnAmount: " + txnAmount);
				response.getWriter().println("returnURL: " + returnURL);
				response.getWriter().println("companyCode: " + companyCode);
				response.getWriter().println("txnRefNo: " + txnRefNo);

				response.getWriter().println("schemeMode: " + schemeMode);
				if (destAggrID.equalsIgnoreCase("AGGR0108"))
					response.getWriter().println("pgGuid: " + pgGuid);
			}

			if (!isParamMissing) {
				customerInfo = companyCode + ":" + customerCode + ":" + customerName;
				if (debug)
					response.getWriter().println("customerInfo: " + customerInfo);

				String paymentRequest = "", encryptedRequest = "";
				JsonObject offerMode = new JsonObject();

				if (null != schemeMode && schemeMode.trim().length() > 0) {
					JsonParser cpiResponseParser = new JsonParser();

					try {
						offerMode = (JsonObject) cpiResponseParser.parse(schemeMode);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

				if (debug)
					response.getWriter().println("offerMode: " + offerMode);

				BigDecimal tidVal = new BigDecimal(0);
				boolean isTidNAN = true;
				String removeTextFromTxnID = "";
				try {
					BigDecimal decimalVal = new BigDecimal(txnRefNo);
					isTidNAN = false;
				} catch (NumberFormatException e) {
					removeTextFromTxnID = txnRefNo.replaceAll("[^0-9.]", "");
					isTidNAN = true;
					// removeTextFromTxnID = txnRefNo; //Added temporarily
				}

				paymentRequest = "";
				String formattedStr = "";
				try {
					int number = Integer.parseInt(customerCode);
					formattedStr = ("0000000000" + customerCode).substring(customerCode.length());
					customerCode = formattedStr;
				} catch (NumberFormatException e) {
					// formattedStr = customerNo;
				}

				if (offerMode.has("sa") && offerMode.get("sa").getAsString().equalsIgnoreCase("X")) {
					if (null != schemeCode && schemeCode.trim().length() > 0) {
						if (!isTidNAN)
							// paymentRequest =
							// "tid="+txnRefNo+"&sub_account_id="+customerCode+"&promo_code="+schemeCode+"&merchant_id="+merchantCode+"&order_id="+txnRefNo+"&currency="+currency+"&amount="+txnAmount+"&redirect_url="+returnURL+"&cancel_url="+returnURL+"&language=en&billing_tel="+consumerMobile;
							paymentRequest = "sub_account_id=" + customerCode + "&promo_code=" + schemeCode
									+ "&merchant_id=" + merchantCode + "&order_id=" + txnRefNo + "&currency=" + currency
									+ "&amount=" + txnAmount + "&redirect_url=" + returnURL + "&cancel_url=" + returnURL
									+ "&language=en&billing_tel=" + consumerMobile;
						else
							// paymentRequest =
							// "tid="+removeTextFromTxnID+"&sub_account_id="+customerCode+"&promo_code="+schemeCode+"&merchant_id="+merchantCode+"&order_id="+txnRefNo+"&currency="+currency+"&amount="+txnAmount+"&redirect_url="+returnURL+"&cancel_url="+returnURL+"&language=en&billing_tel="+consumerMobile;
							paymentRequest = "sub_account_id=" + customerCode + "&promo_code=" + schemeCode
									+ "&merchant_id=" + merchantCode + "&order_id=" + txnRefNo + "&currency=" + currency
									+ "&amount=" + txnAmount + "&redirect_url=" + returnURL + "&cancel_url=" + returnURL
									+ "&language=en&billing_tel=" + consumerMobile;
					} else {
						if (!isTidNAN)
							// paymentRequest =
							// "tid="+txnRefNo+"&sub_account_id="+customerCode+"&merchant_id="+merchantCode+"&order_id="+txnRefNo+"&currency="+currency+"&amount="+txnAmount+"&redirect_url="+returnURL+"&cancel_url="+returnURL+"&language=en&billing_tel="+consumerMobile;
							paymentRequest = "sub_account_id=" + customerCode + "&merchant_id=" + merchantCode
									+ "&order_id=" + txnRefNo + "&currency=" + currency + "&amount=" + txnAmount
									+ "&redirect_url=" + returnURL + "&cancel_url=" + returnURL
									+ "&language=en&billing_tel=" + consumerMobile;
						else
							// paymentRequest =
							// "tid="+removeTextFromTxnID+"&sub_account_id="+customerCode+"&merchant_id="+merchantCode+"&order_id="+txnRefNo+"&currency="+currency+"&amount="+txnAmount+"&redirect_url="+returnURL+"&cancel_url="+returnURL+"&language=en&billing_tel="+consumerMobile;
							paymentRequest = "sub_account_id=" + customerCode + "&merchant_id=" + merchantCode
									+ "&order_id=" + txnRefNo + "&currency=" + currency + "&amount=" + txnAmount
									+ "&redirect_url=" + returnURL + "&cancel_url=" + returnURL
									+ "&language=en&billing_tel=" + consumerMobile;
					}
				} else {
					if (null != schemeCode && schemeCode.trim().length() > 0) {
						if (!isTidNAN)
							paymentRequest = "promo_code=" + schemeCode + "&merchant_id=" + merchantCode + "&order_id="
									+ txnRefNo + "&currency=" + currency + "&amount=" + txnAmount + "&redirect_url="
									+ returnURL + "&cancel_url=" + returnURL + "&language=en&sub_account_id="
									+ companyCode + "&merchant_param1=" + companyCode + "&merchant_param2="
									+ customerCode + "&merchant_param3=" + customerName + "&billing_email="
									+ customerEmail + "&merchant_param4=" + pgGuid;
						else
							paymentRequest = "promo_code=" + schemeCode + "&merchant_id=" + merchantCode + "&order_id="
									+ txnRefNo + "&currency=" + currency + "&amount=" + txnAmount + "&redirect_url="
									+ returnURL + "&cancel_url=" + returnURL + "&language=en&sub_account_id="
									+ companyCode + "&merchant_param1=" + companyCode + "&merchant_param2="
									+ customerCode + "&merchant_param3=" + customerName + "&billing_email="
									+ customerEmail + "&merchant_param4=" + pgGuid;
					} else {
						if (!isTidNAN)
							// JK scenario
							paymentRequest = "tid=" + txnRefNo + "&merchant_id=" + merchantCode + "&order_id="
									+ txnRefNo + "&currency=" + currency + "&amount=" + txnAmount + "&redirect_url="
									+ returnURL + "&cancel_url=" + returnURL + "&language=en&sub_account_id="
									+ companyCode + "&merchant_param1=" + companyCode + "&merchant_param2="
									+ customerCode + "&merchant_param3=" + customerName + "&billing_email="
									+ customerEmail + "&merchant_param4=" + pgGuid;
						else
							paymentRequest = "merchant_id=" + merchantCode + "&order_id=" + txnRefNo + "&currency="
									+ currency + "&amount=" + txnAmount + "&redirect_url=" + returnURL + "&cancel_url="
									+ returnURL + "&language=en&sub_account_id=" + companyCode + "&merchant_param1="
									+ companyCode + "&merchant_param2=" + customerCode + "&merchant_param3="
									+ customerName + "&billing_email=" + customerEmail + "&merchant_param4=" + pgGuid;
					}
				}

				if (debug)
					response.getWriter().println("paymentRequest: " + paymentRequest);
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, paymentRequest, appLogObj.get("ID").getAsString(), appLogMsgArry);

				AesCryptUtil cryptoUtil = new AesCryptUtil(workingKey);
				encryptedRequest = cryptoUtil.encrypt(paymentRequest);

				if (debug)
					response.getWriter().println("encryptedRequest: " + encryptedRequest);

				JsonObject result = new JsonObject();
				result.addProperty("encRequest", encryptedRequest);
				result.addProperty("access_code", accessCode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", txnUrl);
				result.addProperty("parameters", "encRequest|access_code");
				result.addProperty("RawPayLoad", paymentRequest);
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));

			} else {
				JsonObject result = new JsonObject();
				result.addProperty("ParamsMissing", "Mandatory parameters are missing");
				result.addProperty("Valid", "false");
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));
			}

		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"---->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id")+"", buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);

			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getMessage());
			result.addProperty("Trace", buffer.toString());
			result.addProperty("Valid", "false");
			
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}

			
			response.getWriter().print(new Gson().toJson(result));
		}
	}

	private void initiateYesPayUPGRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray  appLogMsgArry) throws IOException {
		CommonUtils commonUtils=new CommonUtils();
		AtomicInteger stepNumber=new AtomicInteger(1);
		try {
			String sEncryptionKey = "", sSecureSecret = "", version = "", sWSURL = "", txnRefNo = "", txnAmount = "",
					passCode = "", bankID = "", terminalID = "", merchantCode = "", mcc = "", currency = "",
					txnType = "", returnURL = "", amountExponent = "";
			version = "1";

			String wholeParamString = "", paramName = "", paramValue = "", pgID = "";
			wholeParamString = configurationValues;
			// response.getWriter().println("configurationValues:
			// "+configurationValues);
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());

				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
					// response.getWriter().println("merchantCode:
					// "+merchantCode);
				}
				if (paramName.equalsIgnoreCase("PGID")) {
					pgID = paramValue;
					// response.getWriter().println("pgID: "+pgID);
				}
				if (paramName.equalsIgnoreCase("WSURL")) {
					sWSURL = paramValue;
					// response.getWriter().println("sWSURL: "+sWSURL);
				}

				if (paramName.equalsIgnoreCase("Version")) {
					version = paramValue;
					// response.getWriter().println("version: "+version);
				}

				if (paramName.equalsIgnoreCase("PassCode")) {
					passCode = paramValue;
					// response.getWriter().println("passCode: "+passCode);
				}

				if (paramName.equalsIgnoreCase("MCC")) {
					mcc = paramValue;
					// response.getWriter().println("mcc: "+mcc);
				}

				if (paramName.equalsIgnoreCase("EncryptionKey")) {
					sEncryptionKey = paramValue;
					// response.getWriter().println("sEncryptionKey:
					// "+sEncryptionKey);
				}

				if (paramName.equalsIgnoreCase("SecureSecret")) {
					sSecureSecret = paramValue;
					// response.getWriter().println("sSecureSecret:
					// "+sSecureSecret);
				}

				if (paramName.equalsIgnoreCase("BankId")) {
					bankID = paramValue;
					// response.getWriter().println("bankID: "+bankID);
				}

				if (paramName.equalsIgnoreCase("TerminalId")) {
					terminalID = paramValue;
					// response.getWriter().println("terminalID: "+terminalID);
				}

				if (paramName.equalsIgnoreCase("Currency")) {
					currency = paramValue;
					// response.getWriter().println("currency: "+currency);
				}

				if (paramName.equalsIgnoreCase("TxnType")) {
					txnType = paramValue;
					// response.getWriter().println("txnType: "+txnType);
				}
			}

			amountExponent = "2";

			if (null != request.getParameter("txn-id"))
				txnRefNo = request.getParameter("txn-id");

			if (null != request.getParameter("txn-amount"))
				txnAmount = request.getParameter("txn-amount");
			float transactionAmount = Float.parseFloat(txnAmount);
			int withoutDecimal = (int) transactionAmount * 100;
			// response.getWriter().println("1 wihtoutDecimal:
			// "+withoutDecimal);
			txnAmount = withoutDecimal + "";

			Hashtable<String, String> pgParams = new Hashtable<String, String>();

			pgParams.put("Version", version);
			pgParams.put("TxnRefNo", txnRefNo);
			pgParams.put("Amount", txnAmount);
			pgParams.put("PassCode", passCode);
			pgParams.put("BankId", bankID);
			pgParams.put("TerminalId", terminalID);
			pgParams.put("MerchantId", merchantCode);
			pgParams.put("MCC", mcc);
			pgParams.put("Currency", currency);
			pgParams.put("TxnType", txnType);
			pgParams.put("ReturnURL", request.getParameter("return-url"));
			//pgParams.put("ReturnURL", removeSpecialCharacter(request.getParameter("return-url")));
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), pgParams+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

			LinkedHashMap<String, String> hmReqFields = new LinkedHashMap<String, String>();
			Enumeration e = pgParams.keys();

			while (e.hasMoreElements()) {
				String fieldName = (String) e.nextElement();
				String fieldValue = pgParams.get(fieldName);
				// response.getWriter().println("fieldName: "+fieldName);
				// response.getWriter().println("fieldValue: "+fieldValue);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					// response.getWriter().println("3333");
					hmReqFields.put(fieldName, fieldValue);
				}
			}

			/*
			 * sEncryptionKey="5EC4A697141C8CE45509EF485EE7D4B1"; //PGOWNPUBLIC
			 * sSecureSecret = "E59CD2BF6F4D86B5FB3897A680E0DD3E";//PGOWNPRIVATE
			 */
			ISGPayEncryption encObj = new ISGPayEncryption();
			encObj.encrypt(hmReqFields, sEncryptionKey, sSecureSecret);

			if (encObj.getENC_DATA() != null && encObj.getENC_DATA().trim().length() > 0) {
				JsonObject result = new JsonObject();
				result.addProperty("MerchantId", encObj.getMERCHANT_ID());
				result.addProperty("TerminalId", encObj.getTERMINAL_ID());
				result.addProperty("BankId", encObj.getBANK_ID());
				result.addProperty("Version", encObj.getVERSION());
				result.addProperty("EncData", encObj.getENC_DATA());
				result.addProperty("txnRefNo", txnRefNo);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("WSURL", sWSURL);
				result.addProperty("parameters", "MerchantId|TerminalId|BankId|Version|EncData");
				// result.addProperty("parameters","");
				result.addProperty("Valid", true);
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("Valid", "false");
				result.addProperty("pgReqMsg", "Error in processing the request. Please contact system admin.");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			}

		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("ParamsMissing", "Some mandatory parameters are missing");
			result.addProperty("Valid", "false");
			StringBuffer buffer=new StringBuffer();
			buffer.append(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage());
				
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
	     	commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);
	     	if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
	     	
	     	response.getWriter().print(new Gson().toJson(result));
		}
	}

	private void initiateEazyPayRequest(HttpServletRequest request, HttpServletResponse response, Map configMap,JsonObject appLogObj,JsonArray appLogMsgArry)
			throws IOException {
		CommonUtils commonUtils=new CommonUtils();
		try {
			// ReadConfig

			// Below code for Payment Gateway
			String encryptionKey = "", merchantCode = "", paymentRequestCall = "", wsURL = "", pgReqMsg = "",
					pgRequestErrorMsg = "";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().print("\nconfigMap values:" + configMap.values());
				response.getWriter().print("\nconfigMap keySet:" + configMap.keySet());
			}
			String wholeParamString = "", paramName = "", paramValue = "", pgID = "", clientCode = "";
			/*
			 * wholeParamString = configurationValues; String[] splitResult =
			 * wholeParamString.split("\\|"); for(String s : splitResult) {
			 * paramName = s.substring(0, s.indexOf("=")); paramValue =
			 * s.substring(s.indexOf("=")+1,s.length());
			 * 
			 * if(paramName.equalsIgnoreCase("MerchantCode")) { merchantCode =
			 * paramValue; }
			 * 
			 * if(paramName.equalsIgnoreCase("PGID")) { pgID = paramValue; }
			 * if(paramName.equalsIgnoreCase("WSURL")) { WSURL = paramValue; //
			 * WSURL = "https://eazypayuat.icicibank.com/EazyPG?"; }
			 * if(paramName.equalsIgnoreCase("SecretKey")) { encryptionKey =
			 * paramValue; // encryptionKey = "1234567891234567"; } }
			 */
			String key = "", value = "";
			Iterator<Map.Entry<String, String>> it = configMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = it.next();

				key = pair.getKey();
				value = pair.getValue();
				if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("\nMap Key:" + key + " | Value:" + value);

				if (key.equalsIgnoreCase("merchantCode")) {
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nmerchant code");
					merchantCode = value;
				} else if (key.equalsIgnoreCase("pgUrl")) {
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nURL");
					wsURL = value;
				} else if (key.equalsIgnoreCase("secretKey")) {
					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nKey");
					encryptionKey = value;
				}
			}

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().print("\nMerchantCode:" + merchantCode);
				response.getWriter().print("\nPGID:" + pgID);
				response.getWriter().print("\nWSURL:" + wsURL);
				response.getWriter().print("\nSecretKey:" + encryptionKey);
			}

			String mandatoryFields = "", customerNo = "", optionalFields = "", subMerchantId = "", transactionAmt = "",
					paymentMode = "", transactionID = "", transactionDate = "", returnURL = "", finalEncrURL = "",
					finalRawURL = "";
			/**
			 * Temporarily Hard Coded Values - Start
			 **/
			// merchantCode = "1040"; transactionID = "19299";
			// transactionAmt="100";
			// mandatoryFields =
			// merchantCode+"|"+transactionID+"|"+transactionAmt;
			// returnURL =
			// "https://flpnwc-ac3edb22e.dispatcher.hana.ondemand.com/sap/fiori/pycomnpymtapp/sap/opu/odata/ARTEC/paymentgateway/PaymentGateway/PGPaymentTxnRes?PGParams=PGIDSEAZYPAY~PGGID938DB166-4585-4722-A347-FE178AC027BA~STEIDSSLaunchpad~APPIDpycomnpymtapp~ACTONAdvancePayment~NAVPMPGStatus~PymntFor000003PaymentCallTextAdvance";

			// if(null != request.getParameter("txn-datetime"))
			// transactionDate = request.getParameter("txn-datetime");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
			transactionDate = sdf.format(cal.getTime());
			if (null != request.getParameter("customerNo"))
				customerNo = request.getParameter("customerNo");

			optionalFields = customerNo + "|" + transactionDate;
			// finalRawURL =
			// WSURL+"?merchantid="+merchantCode+"&mandatoryfields="+mandatoryFields+"&optionalfields="+optionalFields+"&returnurl="+returnUrl+"&ReferenceNo="+transactionID+"&submerchantid="+subMerchantId+"&transactionamount="+transactionAmt+"&paymode="+paymentMode;
			/**
			 * Temporarily Hard Coded Values - End
			 **/
			if (null != request.getParameter("txn-id"))
				transactionID = request.getParameter("txn-id");
			if (null != request.getParameter("txn-amount"))
				transactionAmt = request.getParameter("txn-amount");
			if (null != request.getParameter("paymentMode"))
				paymentMode = request.getParameter("paymentMode");
			else
				paymentMode = "9";
			if (null != request.getParameter("subMerchantId"))
				subMerchantId = request.getParameter("subMerchantId");
			else
				subMerchantId = "0";
			if (null != request.getParameter("return-url")){
				//returnURL = removeSpecialCharacter(request.getParameter("return-url"));
				returnURL=request.getParameter("return-url");
			}

			mandatoryFields = transactionID + "|" + subMerchantId + "|" + transactionAmt;
			finalRawURL = wsURL + "?merchantid=" + merchantCode + "&mandatoryfields=" + mandatoryFields
					+ "&optionalfields=" + optionalFields + "&returnurl=" + returnURL + "&ReferenceNo=" + transactionID
					+ "&submerchantid=" + subMerchantId + "&transactionamount=" + transactionAmt + "&paymode="
					+ paymentMode;
			
			appLogObj.addProperty("SourceReferenceID", transactionID);
			JsonObject erroObj=new JsonObject();
			erroObj.addProperty("TrackID", transactionID);
			erroObj.addProperty("subMerchantId", subMerchantId);
			erroObj.addProperty("txn-amount", transactionAmt);
			erroObj.addProperty("merchantid", merchantCode);
			erroObj.addProperty("optionalfields", optionalFields);
			erroObj.addProperty("returnurl", returnURL);
			erroObj.addProperty("paymode", paymentMode);
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), transactionID, erroObj+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().print("\nMerchantCode:" + merchantCode);
				response.getWriter().print("\ntransactionID:" + transactionID);
				response.getWriter().print("\ntransactionAmt:" + transactionAmt);
				response.getWriter().print("\nsubMerchantId:" + subMerchantId);
				response.getWriter().print("\noptionalFields:" + optionalFields);
				response.getWriter().print("\nreturnURL:" + returnURL);
				response.getWriter().print("\nmandatoryFields:" + mandatoryFields);
				response.getWriter().print("\nfinalRawURL:" + finalRawURL);
			}

			mandatoryFields = encryptDataForEazyPay(mandatoryFields, encryptionKey);
			optionalFields = encryptDataForEazyPay(optionalFields, encryptionKey);
			transactionID = encryptDataForEazyPay(transactionID, encryptionKey);
			subMerchantId = encryptDataForEazyPay(subMerchantId, encryptionKey);
			transactionAmt = encryptDataForEazyPay(transactionAmt, encryptionKey);
			paymentMode = encryptDataForEazyPay(paymentMode, encryptionKey);
			returnURL = encryptDataForEazyPay(returnURL, encryptionKey);

			finalEncrURL = wsURL + "?merchantid=" + merchantCode + "&mandatoryfields=" + mandatoryFields
					+ "&optionalfields=" + optionalFields + "&returnurl=" + returnURL + "&ReferenceNo=" + transactionID
					+ "&submerchantid=" + subMerchantId + "&transactionamount=" + transactionAmt + "&paymode="
					+ paymentMode;
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().print("\nfinalEncrURL:" + finalEncrURL);

			if (mandatoryFields != null && mandatoryFields.trim().length() > 0) {
				JsonObject result = new JsonObject();
				// result.addProperty("walletRequestMessage", pgReqMsg);
				/*
				 * result.addProperty("RawPayload", finalRawURL);
				 * result.addProperty("merchantid", merchantCode);
				 * result.addProperty("mandatoryfields", mandatoryFields);
				 * result.addProperty("optionalfields", optionalFields);
				 * result.addProperty("returnurl", returnURL);
				 * result.addProperty("ReferenceNo", transactionID);
				 * result.addProperty("submerchantid", subMerchantId);
				 * result.addProperty("transactionamount", transactionAmt);
				 * result.addProperty("paymode", paymentMode);
				 * result.addProperty("Valid", "true");
				 * result.addProperty("WSURL", wsURL);
				 * result.addProperty("parameters",
				 * "merchantid|mandatoryfields|optionalfields|returnurl|ReferenceNo|submerchantid|transactionamount|paymode"
				 * );
				 */

				result.addProperty("RawPayload", finalRawURL);
				result.addProperty("merchantid", merchantCode);
				result.addProperty("mandatory fields", mandatoryFields);
				result.addProperty("optional fields", optionalFields);
				result.addProperty("returnurl", returnURL);
				result.addProperty("Reference No", transactionID);
				result.addProperty("submerchantid", subMerchantId);
				result.addProperty("transaction amount", transactionAmt);
				result.addProperty("paymode", paymentMode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", wsURL);
				result.addProperty("parameters",
						"merchantid|mandatory fields|optional fields|returnurl|Reference No|submerchantid|transaction amount|paymode");
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), transactionID, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);

				/*
				 * result.addProperty("walletRequestMessage", pgReqMsg);
				 * result.addProperty("RawPayload", finalURL);
				 * result.addProperty("walletClientCode", merchantCode);
				 * result.addProperty("Valid", "true");
				 * result.addProperty("WSURL", WSURL);
				 * result.addProperty("parameters",
				 * "walletClientCode|walletRequestMessage");
				 */
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.getWriter().print(new Gson().toJson(result));
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", "Mandatory fields are missing. Please check configuration.");
				result.addProperty("walletClientCode", merchantCode);
				result.addProperty("Valid", "false");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), transactionID, result+"", appLogObj.get("ID").getAsString(),  appLogMsgArry);
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));
			}
		} catch (Exception e) {
			// response.getWriter().println("initiateICICIPaymentRequest Error:
			// "+e.getMessage());
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", e.getMessage());
			// result.addProperty("walletClientCode", merchantCode);
			result.addProperty("Valid", "false");
			
			StringBuffer buffer=new StringBuffer();
			buffer.append(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage());
				
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
	     	commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id"), buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);
	     	
	     	if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
	     	
			response.getWriter().print(new Gson().toJson(result));
		}
	}

	public String encryptDataForEazyPay(String parameterData, String encryptionKey) {
		String enryptedData = "";
		Base64 encoder = null;
		try {
			byte[] abyte2 = (byte[]) null;
			byte[] abyte1 = encryptionKey.getBytes();
			SecretKeySpec secretkeyspec = new SecretKeySpec(abyte1, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(1, secretkeyspec);
			abyte2 = cipher.doFinal(parameterData.getBytes());
			enryptedData = encoder.getEncoder().encodeToString(abyte2);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} finally {
			return enryptedData;
		}
	}
	
	public String removeSpecialCharacter(String returnUrl){
		try{
			String[] split = returnUrl.split("cancel-url");
			StringBuffer buffer=new StringBuffer();
			if(split.length>0&&split.length==2) {
				String successUrl=split[0];
				String cancelUrl=split[1];
				String[] split2 = successUrl.split("PGParams=");
				buffer.append(split2[0]).append("PGParams=");
				String params=split2[1];
				params = params.replaceAll("\\s", "");
				params=params.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(""));
				params=params.replaceAll("//", "");
				buffer.append(params);
				buffer.append("cancel-url");
				String[] split3 = cancelUrl.split("PGParams=");
				buffer.append(split3[0]).append("PGParams=");
				params=split3[1];
				params = params.replaceAll("\\s", "");
				params=params.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(""));
				params=params.replaceAll("//", "");
				buffer.append(params);
				return buffer.toString();
			} else if (split.length > 0 && split.length == 1) {
				String successUrl = split[0];
				String[] split2 = successUrl.split("PGParams=");
				if (split2.length > 1) {
					buffer.append(split2[0]).append("PGParams=");
					String params = split2[1];
					params = params.replaceAll("\\s", "");
					params = params.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(""));
					params = params.replaceAll("//", "");
					buffer.append(params);
					return buffer.toString();
				} else {
					return returnUrl;
				}
			}else{
			return returnUrl;	
			}
		}catch(Exception ex){
			return returnUrl;
		}
	}

	private void initiateTechProcessPGRequest(HttpServletRequest request, HttpServletResponse response,
			String configurationValues,JsonObject appLogObj,JsonArray appLogMsgArry) throws IOException {
		CommonUtils commonUtils=new CommonUtils();
		String encryptionKey = "", merchantCode = "", schemeCode="", tokenURL="", sWSURL="", pgCategoryID="", encryptionIV="", 
				txnType="", txnRefNo="", txnAmount="", txnDate="", returnURL="", paymentRequest="";
		TransactionRequestBean objTransactionRequestBean = new TransactionRequestBean();
		boolean isParamMissing = false;
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		try{
			String wholeParamString = "", paramName = "", paramValue = "", pgID = "", secretCode = "";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());
				
				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("PGID")) {
					pgID = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("WSURL")) {
					sWSURL = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("PGCategoryID")) {
					pgCategoryID = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("EncryptionIV")) {
					encryptionIV = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("EncryptionKey")) {
					encryptionKey = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("SchemeCode")) {
					schemeCode = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("TxnType")) {
					txnType = paramValue;
				}
			}
			
			if (null != request.getParameter("txn-id") && request.getParameter("txn-id").trim().length() > 0) {
				txnRefNo = request.getParameter("txn-id");
			} else {
				isParamMissing = true;
			}
			
			if (null != request.getParameter("txn-amount") && request.getParameter("txn-amount").trim().length() > 0) {
				txnAmount = request.getParameter("txn-amount");
			} else {
				isParamMissing = true;
			}
			
			if (null != request.getParameter("txn-datetime")){
				txnDate = request.getParameter("txn-datetime");
			}else{
				isParamMissing = true;
			}
			
			if (null != request.getParameter("return-url") && request.getParameter("return-url").trim().length() > 0) {
				//returnURL = request.getParameter("return-url");
				returnURL=removeSpecialCharacter(request.getParameter("return-url"));
			} else {
				isParamMissing = true;
			}
			
			paymentRequest = "txnAmount:"+txnAmount+"|txnType:"+txnType+"merchantCode:"+merchantCode+"txnRefNo:"+txnRefNo+"txnDate:"+txnDate+"WebServiceLocator:"+sWSURL+"customerMobile:"+customerMobile+"customerName:"+customerName+"customerEmail:"+customerEmail;
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, paymentRequest, appLogObj.get("ID").getAsString(), appLogMsgArry);
			if(! isParamMissing){
				objTransactionRequestBean.setStrAmount(txnAmount);
				objTransactionRequestBean.setStrShoppingCartDetails(schemeCode+"_"+txnAmount+"_0.0");
				objTransactionRequestBean.setStrRequestType(txnType);
				objTransactionRequestBean.setStrMerchantCode(merchantCode);
				objTransactionRequestBean.setMerchantTxnRefNumber(txnRefNo);
				objTransactionRequestBean.setStrCurrencyCode(properties.getProperty("TPSLCurrency"));
				objTransactionRequestBean.setStrITC("");
				objTransactionRequestBean.setStrReturnURL(returnURL); 
				objTransactionRequestBean.setTxnDate(txnDate);
				objTransactionRequestBean.setWebServiceLocator(sWSURL);
				objTransactionRequestBean.setCustID("");
				objTransactionRequestBean.setStrTPSLTxnID("");
				objTransactionRequestBean.setStrMobileNumber(customerMobile);
				objTransactionRequestBean.setKey(encryptionKey.getBytes()); 
		        objTransactionRequestBean.setIv(encryptionIV.getBytes()); 
		        objTransactionRequestBean.setStrCustomerName(customerName);
		        objTransactionRequestBean.setStrEmail(customerEmail);
		        objTransactionRequestBean.setStrTimeOut("1000");
		        objTransactionRequestBean.setAccountNo("");
		        tokenURL = objTransactionRequestBean.getTransactionToken();
		        commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, tokenURL, appLogObj.get("ID").getAsString(), appLogMsgArry);
		        
		        if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
					response.getWriter().println("tokenURL:"+tokenURL);
				}
		        
		        JsonObject result = new JsonObject();
//				result.addProperty("encRequest", encryptedRequest);
//				result.addProperty("access_code", accessCode);
				result.addProperty("Valid", "true");
				result.addProperty("WSURL", tokenURL);
				result.addProperty("parameters", "");
				result.addProperty("RawPayLoad", paymentRequest);
				
				response.getWriter().print(new Gson().toJson(result));
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("ParamsMissing", "Mandatory parameters are missing");
				result.addProperty("Valid", "false");
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, result+"", appLogObj.get("ID").getAsString(), appLogMsgArry);
				
				if(appLogMsgArry.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArry);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));
			}
			
		}catch (Exception e) {
			
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "--->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage());
			}
			
			StackTraceElement[] stackTrace = e.getStackTrace();
			StringBuffer buffer1 = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().println(buffer1.toString());
			}
			
			if(appLogMsgArry.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArry);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), request.getParameter("txn-id") + "", buffer.toString(), appLogObj.get("ID").getAsString(), appLogMsgArry);
			response.getWriter().print("initiateTechProcessPGRequest Error: " + e.getMessage());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
package com.arteriatech.pg;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
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

import com.arteriatech.support.DestinationUtils;
import com.ccavenue.security.AesCryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.isg.isgpay.ISGPayDecryption;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.tp.pg.util.TransactionResponseBean;
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
import com.tp.pg.util.TransactionResponseBean;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;
/**
 * Servlet implementation class ResPymtGateway
 */
public class ResPymtGateway extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
	String destAggrID="";
	CookieStore globalCookieStore = null;
//	private TenantContext  tenantContext;
	
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	String servletPath="", loginID="";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResPymtGateway() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		boolean debug=false;
		String appLogID="";
		AtomicInteger messageNo=new AtomicInteger();
		JsonArray appLogMsgArr=new JsonArray();
		try
		{
			String siteID="", appID="", actionName="", navParam = "", localStorageRefNo = "", portalURL="", pgID="", responseMessage="", redirectURL="", pgHdrGUID="", pgParams="", paymentGtwyID="";
			String configurationValues = "";
			String aggregatorID = "", source="";
			servletPath = request.getServletPath();
//			response.getWriter().println("servletPath: "+servletPath);
			destAggrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			//			if(null != request.getParameter("PGID"))
//				pgID = request.getParameter("PGID");
			response.getWriter().println("destAggrID: "+destAggrID);
			response.getWriter().println("PGParams: "+request.getParameter("PGParams"));
			response.getWriter().println("Request received on: "+servletPath);
			if(destAggrID.equalsIgnoreCase("E106") || destAggrID.equalsIgnoreCase("E112") || destAggrID.contains("E173")){
				if(destAggrID.equalsIgnoreCase("E106")){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setContentType("text/plain");
					response.getWriter().println("Destination PCGWHANA not configured in cockpit");
				}else if(destAggrID.equalsIgnoreCase("E112")){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setContentType("text/plain");
					response.getWriter().println("Property 'AggregatorID' not maintained in destination PCGWHANA");
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setContentType("text/plain");
					response.getWriter().println("Exception when trying to read Destination PCGWHANA: "+destAggrID);
				}
			}else{
				if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
					if(null != request.getParameter("PGParams"))
						pgParams = request.getParameter("PGParams");

					String[] resSplitResult = pgParams.split("\\~");
					String resParamName="", resParamValue="";
					for(String s : resSplitResult){
//						response.getWriter().println("q: "+s);
						resParamName = s.substring(0, 5);
						resParamValue = s.substring(5,s.length());
						// response.getWriter().println("resParamName: "+resParamName);
						// response.getWriter().println("resParamValue: "+resParamValue);
			        	if(resParamName.equalsIgnoreCase("PGIDS")){
			        		pgID = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("PGGID")){
			        		pgHdrGUID = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("STEID")){
			        		siteID = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("APPID")){
			        		appID = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("ACTON")){
			        		actionName = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("NAVPM")){
			        		navParam = resParamValue;
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("REFNO")){
			        		localStorageRefNo = resParamValue;
			        	}

						if(resParamName.equalsIgnoreCase("USRID")){
			        		loginID = resParamValue;
			        	}
					}
					
					if(siteID.trim().length() == 32){
						String newSiteID = siteID.substring(0, 8) + "-" + siteID.substring(8,12)+"-"+siteID.substring(12,16)+"-"+siteID.substring(16,20)+"-"+siteID.substring(20);
						redirectURL = "/site?siteId="+newSiteID+"#"+appID+"-"+actionName+"&/"+navParam;
					}else{
						redirectURL = "/site/"+siteID+"#"+appID+"-"+actionName+"&/"+navParam;
					}
					if(request.getParameter("debug")!=null &&request.getParameter("debug").equalsIgnoreCase("true")){
						debug=true;
					}
					
				  JsonObject appLogObj= getApplicationLogobj(pgID,pgHdrGUID,response,debug);
				 if(appLogObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					 appLogID=appLogObj.get("ID").getAsString();
				 }
				 response.getWriter().println("appLogID:"+appLogID);	
				}else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
					pgID = request.getParameter("PGID");
					aggregatorID = request.getParameter("AGGRID");
					
					if(null != request.getParameter("source") && request.getParameter("source").trim().length() > 0){
						source = request.getParameter("source");
					}
				}
				response.getWriter().println("pgID: "+pgID);
				if(null != pgID && pgID.trim().length() > 0){
					Properties properties = new Properties();
					properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
					
					if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
						configurationValues = getConstantValues(request, response, "", pgID);
						if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
							response.getWriter().print("configurationValues.inside ID: "+configurationValues);
					}else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
						//Get keys from here and store in configurationValues variable here
						if(source.trim().equalsIgnoreCase("mobile")){
							configurationValues = getConstantValues(request, response, "", pgID);
						}else{
							configurationValues = getConstantValuesForDecryption(request, response, "", pgID);
						}
					}
					response.getWriter().println("configurationValues outside if: "+configurationValues);
					boolean isAggrEmpty = false;
					if(configurationValues.trim().length() > 0 && !configurationValues.equalsIgnoreCase("E106")){
						if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
							if(source.trim().equalsIgnoreCase("")){
								if(aggregatorID == null || aggregatorID.trim().length() == 0){
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.setContentType("text/plain");
									response.getWriter().println("AGGRID is mandatory");
									isAggrEmpty = true;
								}else{
									isAggrEmpty = false;
								}
							}
						}
						
						response.getWriter().println("isAggrEmpty: "+isAggrEmpty);
						if(!isAggrEmpty){
							if(pgID.equalsIgnoreCase(properties.getProperty("AxisPGID"))){
								if(null != request.getParameter("i"))
									responseMessage = request.getParameter("i");
								if(responseMessage != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectAxisMessageToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr,commonUtils);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID"))){
								if(null != request.getParameter("walletResponseMessage"))
									responseMessage = request.getParameter("walletResponseMessage");
								
								if(responseMessage != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectICICIMessageToPortal(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configurationValues, localStorageRefNo,appLogID,appLogMsgArr,commonUtils);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if(pgID.equalsIgnoreCase(properties.getProperty("YesPGID"))){
								if(request.getParameterNames() != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectPayUMessageToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr,commonUtils);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if(pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID"))){
								CommonUtils readConfig = new CommonUtils();
								Map<String, String> configMap = readConfig.readConfigValues(pgID, "T");
								
								if(request.getParameterNames() != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectEazyPayResponseToPortal(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configMap, localStorageRefNo,appLogID,appLogMsgArr,commonUtils);	
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")) || pgID.equalsIgnoreCase("RZRPAY")){
								if(request.getParameterNames() != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectRazorPayResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr,commonUtils);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
								if(request.getParameterNames() != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectRazorPayResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr,commonUtils);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
										response.getWriter().println("Work Under Progress");
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if (pgID.equalsIgnoreCase(properties.getProperty("CCAvenuePGID")) 
								|| pgID.equalsIgnoreCase(properties.getProperty("MobileCCPGID"))
								|| pgID.equalsIgnoreCase(properties.getProperty("CCAICICI"))
								|| pgID.equalsIgnoreCase(properties.getProperty("CCAPGID"))){
								if(null != request.getParameter("encResp")){
									responseMessage = request.getParameter("encResp");
								}else{
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.setContentType("text/plain");
									response.getWriter().println("Bank response is mandatory");
								}
								
								if(responseMessage != null){
									if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
										redirectCCAvenueResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr);
									else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
//										String respCC="order_id=202187000000686&tracking_id=309006142300&bank_ref_no=1592976553646&order_status=Success&failure_message=&payment_mode=Net Banking&card_name=AvenuesTest&status_code=null&status_message=Y&currency=INR&amount=33405.00&billing_name=&billing_address=&billing_city=&billing_state=&billing_zip=&billing_country=&billing_tel=&billing_email=&delivery_name=&delivery_address=&delivery_city=&delivery_state=&delivery_zip=&delivery_country=&delivery_tel=&merchant_param1=JKTIL&merchant_param2=1100644&merchant_param3=ARIHANT TYRES&merchant_param4=&merchant_param5=&vault=N&offer_type=null&offer_code=null&discount_value=0.0&mer_amount=33405.00&sub_account_id=JKTIL&eci_value=null&retry=N&response_code=0&billing_notes=&trans_date=24/06/2020 10:59:39&bin_country=";
										decryptCCAvenueResponse(request, response, responseMessage, configurationValues, pgID);
//										response.getWriter().println(respCC);
									}
								}else{
									JsonObject result = new JsonObject();
									result.addProperty("pgResMsg", "Response not found");
									result.addProperty("Valid", "false");
									response.getWriter().print(new Gson().toJson(result));
								}
							}else if(pgID.equalsIgnoreCase(properties.getProperty("Ingenico"))|| pgID.equalsIgnoreCase(properties.getProperty("TPSL"))){
								if(null != request.getParameter("msg")){
									responseMessage = request.getParameter("msg");
								}else{
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.setContentType("text/plain");
									response.getWriter().println("Bank response is mandatory");
								}
								
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
									commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", "Ingenico not avaibale on cloud", appLogID, appLogMsgArr);
									// response.getWriter().println("Ingenico not avaibale on cloud");
									decryptTechProcessPortalResponse(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam,appLogID,appLogMsgArr,commonUtils);
								}
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
									decryptTechprocessResponse(request, response, responseMessage, configurationValues, pgID);
								}
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "PGID Not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}
					}else{
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.setContentType("text/plain");
						response.getWriter().println("Configuration/Keys not found");
					}
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setContentType("text/plain");
					response.getWriter().println("PGID is mandatory");
				}
			}
		}
		catch (Exception e)
		{
			response.getWriter().println(displayErrorForWeb(e));
			if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
				StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"--->");
				if(e.getLocalizedMessage()!=null){
					buffer.append(e.getLocalizedMessage()+"-->");
				}
				StackTraceElement[] stackTrace = e.getStackTrace();
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}
					
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogID, appLogMsgArr);
			}
		}
		finally
		{
			if (appLogMsgArr.size() > 0) {
				JsonObject appLogMsg = new JsonObject();
				JsonObject onObjEvent = new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				JsonObject onObjRes = commonUtils.callOnObjectEventPublish(response, onObjEvent, debug);
				if (debug) {
					response.getWriter().println(onObjRes);
				}

			}
		}
		
		
	}
	
	

	private JsonObject getApplicationLogobj(String pgID, String pgHdrGUID,HttpServletResponse response,boolean debug) throws Exception{
		CommonUtils commonUtils=new CommonUtils();
		String username="",password="",oDataUrl="",userpass="",executeUrl="";
		JsonObject resObj=new JsonObject();
		try{
			oDataUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userpass=username+":"+password;
			executeUrl=oDataUrl+"ApplicationLogs?$filter=AggregatorID%20eq%20%27"+destAggrID+"%27%20and%20ProcessID%20eq%20%27"+pgID+"%27%20and%20ProcessRef1%20eq%20%27"+pgHdrGUID+"%27";
			if(debug){
			response.getWriter().println("executeUrl:"+executeUrl);
			}
			
			JsonObject appLogObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			// response.getWriter().println("appLogObj:"+appLogObj);
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

	public String displayErrorForWeb(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");
	}
	
	private String getConstantValuesForDecryption(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException{
		String configurableValues="", executeURL="", oDataUrl="", aggregatorID="", userName="", passWord="", userPass="", encryptionKey="", apiAccessKey="";
		boolean debug=false;
		
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject configJsonObject = new JsonObject();
		JsonObject resultObj = new JsonObject();
		try{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
//			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			aggregatorID = request.getParameter("AGGRID");
			
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName+":"+passWord;
			
			if(oDataUrl.trim().equalsIgnoreCase("E112") || oDataUrl.trim().equalsIgnoreCase("E106") || oDataUrl.trim().contains("E173")){
				configurableValues = "E106";
			}else{
				executeURL = oDataUrl+"H2HApplicationConfigs?$filter=ApplicationID%20eq%20%27"+PGID+"%27%20and%20SenderID%20eq%20%27"+aggregatorID+"%27";
				
				if(debug){
					response.getWriter().println("getConstantValuesForDecryption.oDataUrl: "+oDataUrl);
					response.getWriter().println("getConstantValuesForDecryption.aggregatorID: "+aggregatorID);
					response.getWriter().println("getConstantValuesForDecryption.userName: "+userName);
//					response.getWriter().println("getConstantValuesForDecryption.passWord: "+passWord);
					response.getWriter().println("getConstantValuesForDecryption.oDataUrl: "+executeURL);
					response.getWriter().println("getConstantValuesForDecryption.oDataUrl: "+executeURL);
				}
				
				configJsonObject = commonUtils.executeURL(executeURL, userPass, response);
				
				if(debug){
					response.getWriter().println("getConstantValuesForDecryption.configJsonObject: "+configJsonObject);
				}
				
				if(configJsonObject.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0)
				{
					resultObj = configJsonObject.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
					
					if(PGID.equalsIgnoreCase("INGENICO")){
						encryptionKey = resultObj.get("EncryptionKey").getAsString();
						apiAccessKey = resultObj.get("FTPPassword").getAsString();
						
						configurableValues = configurableValues+"EncryptionKey="+encryptionKey
								+"|"+"APIAccessKey="+apiAccessKey
								+"|"+"AggregatorID="+aggregatorID;
					}else if(PGID.equalsIgnoreCase("CCAVENUE") 
					|| PGID.equalsIgnoreCase("CCAICICI")
					|| PGID.equalsIgnoreCase("CCA")){
						encryptionKey = resultObj.get("EncryptionKey").getAsString();
//						apiAccessKey = resultObj.get("FTPPassword").getAsString();
						
						configurableValues = configurableValues+"EncryptionKey="+encryptionKey
								+"|"+"APIAccessKey="+apiAccessKey
								+"|"+"AggregatorID="+aggregatorID;
					}else if(PGID.equalsIgnoreCase("MOBILECCA")){
						encryptionKey = resultObj.get("EncryptionKey").getAsString();
//						apiAccessKey = resultObj.get("FTPPassword").getAsString();
						
						configurableValues = configurableValues+"EncryptionKey="+encryptionKey
								+"|"+"APIAccessKey="+apiAccessKey
								+"|"+"AggregatorID="+aggregatorID;
					}
				}
			}
		}catch (Exception e) {
//			response.getWriter().println("Exception: "+e.getMessage());
			if(debug){
				response.getWriter().println("getConstantValuesForDecryption.Exception: "+e.getLocalizedMessage());
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("getConstantValuesForDecryption.Full Stack Trace: "+buffer.toString());
			}
			configurableValues="";
		}
		
		if(debug){
			response.getWriter().println("getConstantValuesForDecryption.configurableValues: "+configurableValues);
		}
		return configurableValues;
	}
	
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

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
					if(debug)
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		            NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            				+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            				+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
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
					if(debug)
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
		            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
		            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
		            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
		            NodeList secretCode = document.getElementsByTagName("d:ClientCode");
		            NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
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
		            								+"|"+"secretCode="+secretCode.item(i).getTextContent()
		            								+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent();
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
					if(debug)
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList pgOwnPublicKey = document.getElementsByTagName("d:PGOwnPublickey");
		            NodeList pgOwnPrivateKey = document.getElementsByTagName("d:PGOwnPrivatekey");
		            NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
		            								+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
		            								+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent();
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
		            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		            NodeList secretKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
		            NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
		            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent()
		            				+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
		            				+"|"+"SecretKey="+secretKeyList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
				else if(PGID.equalsIgnoreCase("RZRPYROUTE"))
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
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
					NodeList apiKey = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList secretKey = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
									          
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
													+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
					
				}
				else if(PGID.equalsIgnoreCase("RAZORPAY") || PGID.equalsIgnoreCase("RZRPAY"))
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
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
					NodeList apiKey = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList secretKey = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
									          
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
													+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
					
				}
				else if(PGID.equalsIgnoreCase("CCAVENUE") 
				|| PGID.equalsIgnoreCase("MOBILECCA") 
				|| PGID.equalsIgnoreCase("CCAICICI")
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
					NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList chGuidList = document.getElementsByTagName("d:ConfigHeaderGUID");
									          
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
										            +"|"+"EncryptionKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"CHGUID="+chGuidList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if (PGID.equalsIgnoreCase(properties.getProperty("TechProcessPGID"))) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(configValuesEntity);
//					if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
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
					
					response.getWriter().println("configurableValues: " + configurableValues);
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
		else if (destinationName == null) 
		{
			destinationName = DestinationUtils.PCGW_UTILS_OP;
		}
		
		String tempSapClient = null;
				
		try {
				// get all destination properties
				// Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
				// DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGW_UTILS_OP);
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				Destination destConfiguration = destinationAccessor.get();

				// Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
							
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
	
	private void redirectICICIMessageToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String redirectURL, String pgID, String pgHdrGUID, String configurationValues, String localStorageRefNo,String appLogId,JsonArray appLogMsgArr,CommonUtils commonUtils) throws IOException
	{
		String portalURL="";
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="",clientCode = "";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
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
			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgResponseErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
//			pgRespMsg = request.getParameter("walletResponseMessage");
			pgRespMsg = responseMessage;

			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setWalletResponseMessage(pgRespMsg);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			
			String walletTxnStatus="", txnID="", walletUserCode="", txnSessionID="", walletTxnID="", walletTxnDateTime="", txnDateTime="", walletBankRefID="", txnFor="",
					txnAmount="", walletTxnRemarks="", adnlParam1="", adnlParam2="", adnlParam3="", adnlParam4="", adnlParam5="", adnlParam6="", adnlParam7="", adnlParam8="",
					adnlParam9="", adnlParam10="", pgMode="", pgName="", challanTxnID="", nachPaymentType="", nachPaymentDate="", nachPaymentDateAlt="", paymentMethod="",
					nachType="", nachSchDate="", yymmddDate="";
			if(walletMessageBean.validateWalletResponseMessage()) 
			{
				WalletParamMap map = walletMessageBean.getResponseMap();
				walletTxnStatus = map.get("wallet-txn-status");
				txnID = map.get("txn-id");
				walletUserCode = map.get("wallet-user-code");
				txnSessionID = map.get("txn-session-id");
				walletTxnID = map.get("wallet-txn-id");
				walletTxnDateTime = map.get("wallet-txn-datetime");
				txnDateTime = map.get("txn-datetime");
				walletBankRefID = map.get("wallet-bank-ref-id");
				txnFor = map.get("txn-for");
				txnAmount = map.get("txn-amount");
				walletTxnRemarks = map.get("wallet-txn-remarks");
				adnlParam1 = map.get("additional-param1");
				adnlParam2 = map.get("additional-param2");
				adnlParam3 = map.get("additional-param3");
				adnlParam4 = map.get("additional-param4");
				adnlParam5 = map.get("additional-param5");
				adnlParam6 = map.get("additional-param6");
				adnlParam7 = map.get("additional-param7");
				adnlParam8 = map.get("additional-param8");
				adnlParam9 = map.get("additional-param9");
				adnlParam10 = map.get("additional-param10");
				pgMode = map.get("pg-mode");
				pgName = map.get("pg-name");
				challanTxnID = map.get("challan-txn-id");
				nachPaymentType = map.get("nach-payment-type");
				nachPaymentDate = map.get("nach-scheduledpayment-date");
				
				if(nachPaymentDate != null)
				{
					nachPaymentDateAlt = nachPaymentDate.replaceAll("\\/", "");
					
					String year = nachPaymentDateAlt.substring(4, 8);
					String mm = nachPaymentDateAlt.substring(2, 4);
					String dd = nachPaymentDateAlt.substring(0, 2);
					yymmddDate =year+mm+dd;
					
				}
				else
				{
					yymmddDate = null;
				}
				
				if(pgMode == null && pgName==null && nachPaymentType==null)
				{
					paymentMethod = "000001";
				}
				else if(pgName != null && nachPaymentType==null)
				{
					paymentMethod = "000002";
				}
				else if(nachPaymentType != null)
				{
					paymentMethod = "000005";
				}
				
				JsonObject result = new JsonObject();
				result.addProperty("walletTxnStatus", walletTxnStatus);
				result.addProperty("txnID", txnID);
				result.addProperty("walletUserCode", walletUserCode);
				result.addProperty("txnSessionID", txnSessionID);
				result.addProperty("walletTxnID", walletTxnID);
				result.addProperty("walletTxnDateTime", walletTxnDateTime);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("walletBankRefID", walletBankRefID);
				result.addProperty("txnFor", txnFor);
				result.addProperty("txnAmount", txnAmount);
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
				result.addProperty("challanTxnID", challanTxnID);
				result.addProperty("nachPaymentType", nachPaymentType);
				result.addProperty("nachPaymentDate", nachPaymentDate);
				result.addProperty("paymentMethod", paymentMethod);
				result.addProperty("nachPaymentDateAlt", nachPaymentDateAlt);
//				pgID = request.getParameter("PGID");
				txnSessionID = request.getParameter("PGHdrGUID");
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, result+"", appLogId, appLogMsgArr);
				if(appLogMsgArr.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArr);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.getWriter().print(new Gson().toJson(result));
//				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";
				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",paymentMethod="+paymentMethod+",pgName="+pgName+",pgMode="+pgMode+",nachType="+nachPaymentType+",nachSchDate="+yymmddDate+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+",localStorageRefNo="+ localStorageRefNo +")";
				response.getWriter().println("portalURL: "+portalURL);
				//Host hardcoded to check java app on CF and portal on Neo. Commend the below line when testing with CF portal
				portalURL = "https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com"+portalURL;
				response.sendRedirect(portalURL);
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgResponseErrorMsg);
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, result+"", appLogId, appLogMsgArr);
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "--->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage() + "-->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);	
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void redirectAxisMessageToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam,String appLogId,JsonArray appLogMsgArr,CommonUtils commonUtils) throws IOException
	{
		byte[] output = null;
		String portalURL="";
		
		try
		{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String encryptionKey = "", merchantCode="", pgType="", re1="", parameterData="", version="", axisCurrency="", keyValue="", secretCode="";
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			response.getWriter().println("wholeParamString: "+wholeParamString);
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	// response.getWriter().println(paramName+" ---> "+paramValue);
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Version"))
	        	{
	        		version = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Type"))
	        	{
	        		pgType = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("RE1"))
	        	{
	        		re1 = paramValue;
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
//	        			encryptionKey = properties.getProperty("axisEncryptionKeyUAT");
	        			encryptionKey = secretCode;
	        		}
	        		else
	        		{
	        			encryptionKey = properties.getProperty("axisEncryptionKeyPRD");
	        		}
	        	}
			}
			
//			response.getWriter().println("secretCode: "+secretCode);
//			encryptionKey="testbank12345678";
			response.getWriter().println("responseMessage: "+responseMessage);
		    SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
//		    response.getWriter().println("skey-getFormat: "+skey.getFormat());
//		    response.getWriter().println("skey-getAlgorithm: "+skey.getAlgorithm());
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		    response.getWriter().println("cipher: "+cipher.getAlgorithm());
		    cipher.init(Cipher.DECRYPT_MODE, skey);
		    output = cipher.doFinal(Base64.getDecoder().decode(responseMessage));
		    
		    response.getWriter().println("output: "+output);
		    
			String resParamName="", resParamValue="", resWholeParamString="";
			resWholeParamString = new String(output);
//			response.getWriter().println("resWholeParamString: "+resWholeParamString);
			String[] resSplitResult = resWholeParamString.split("\\&");
			String bankRefNo="", txnStatus="", remarks="", aggrTxnID="", txnDateTime="", pymtMode="", txnID="", txnSessionID="", versionNo="", resMerchantCode="", 
					resPGType="", customerNo="", currency="", txnAmount="", checkSum="";
			
			response.getWriter().println("Response: ");
			for(String s : resSplitResult)
			{
				resParamName = s.substring(0, s.indexOf("="));
				resParamValue = s.substring(s.indexOf("=")+1,s.length());
	        	
				// response.getWriter().println(resParamName+" ---> "+resParamValue);
				
	        	if(resParamName.equalsIgnoreCase("BRN"))
	        	{
	        		bankRefNo = resParamValue;
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
			
			JsonObject errorObj=new JsonObject();
			errorObj.addProperty("txnStatus", txnStatus);
			errorObj.addProperty("txnID", txnID);
			errorObj.addProperty("bankRefNo", bankRefNo);
			errorObj.addProperty("txnSessionID", pgHdrGUID);
			errorObj.addProperty("txnSessionID", pgHdrGUID);
			errorObj.addProperty("txnAmount", txnAmount);
			errorObj.addProperty("source", "PG");
			errorObj.addProperty("PGID",pgID);
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, errorObj+"", appLogId, appLogMsgArr);
			if(appLogMsgArr.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			portalURL = redirectURL+"/(txnStatus="+txnStatus+",txnID="+txnID+",bankRefNo="+bankRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";

			request.setAttribute("portalURL",portalURL);
			response.setContentType("text/html");
			RequestDispatcher view = request.getRequestDispatcher("/TopUp.jsp");
			view.forward(request,response);
		}
		catch (Exception e)
		{
//			System.out.println(e.toString());
		    JsonObject result = new JsonObject();
			result.addProperty("ErrorMessage", "Unable to decrypt the response");
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"--->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			if(appLogMsgArr.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void redirectPayUMessageToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam,String appLogId,JsonArray appLogMsgArr,CommonUtils commonUtils) throws IOException
	{
		try
		{
			String portalURL="", sDecryptionKey="", sSecureSecret = "";
			String txnRefNo="", lastName="", addressZip="", hashValidated="", errorMessage="", merchantId="", amount="", terminalId="", responseCode="", message="", retRefNo="", batchNo="", authCode="", bankID="", issuerRefNo="", firstName="";
			
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantId = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey"))
	        	{
	        		sDecryptionKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecureSecret"))
	        	{
	        		sSecureSecret = paramValue;
	        	}
			}
			
			/*response.getWriter().println("merchantId: "+merchantId);
			response.getWriter().println("sDecryptionKey: "+sDecryptionKey);
			response.getWriter().println("sSecureSecret: "+sSecureSecret);*/
			
			
			ISGPayDecryption decObj = new ISGPayDecryption();
			LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();//decObj.decrypt(request, sDecryptionKey, sSecureSecret);
			Enumeration e = request.getParameterNames();

			while (e.hasMoreElements()) {

				String fieldName = (String) e.nextElement();
				String fieldValue = request.getParameter(fieldName);

				if(! fieldName.equalsIgnoreCase("PGParams"))
				{
					if ((fieldValue != null) && (fieldValue.length() > 0)) {
						hmDecryptedValue.put(fieldName, fieldValue);
					}
				}

			}
			
			decObj.decrypt(hmDecryptedValue, sDecryptionKey, sSecureSecret);
			//response.getWriter().println("decObj::::" +  hmDecryptedValue.get("SecureHash"));
			//response.getWriter().println("hmDecryptedValue:::"+hmDecryptedValue);
			
			txnRefNo = (String) hmDecryptedValue.get("TxnRefNo");
			merchantId = (String) hmDecryptedValue.get("MerchantId");
			amount = (String) hmDecryptedValue.get("Amount");
			terminalId = (String) hmDecryptedValue.get("TerminalId");
			responseCode = (String) hmDecryptedValue.get("ResponseCode");
			message = (String) hmDecryptedValue.get("Message");
			retRefNo = (String) hmDecryptedValue.get("RetRefNo");
			batchNo = (String) hmDecryptedValue.get("BatchNo");
			authCode = (String) hmDecryptedValue.get("AuthCode");
			bankID = (String) hmDecryptedValue.get("BankId");
			issuerRefNo = (String) hmDecryptedValue.get("issuerRefNo");
			firstName = (String) hmDecryptedValue.get("firstName");
			lastName = (String) hmDecryptedValue.get("lastName");
			addressZip = (String) hmDecryptedValue.get("addressZip");
			hashValidated = (String) hmDecryptedValue.get("hashValidated");
			errorMessage = (String) hmDecryptedValue.get("ErrorMessage");
			
			/*int intAmount = Integer.parseInt(amount)/100;
			amount= ""+intAmount;*/
			JsonObject errorObj=new JsonObject();
			errorObj.addProperty("txnRefNo", txnRefNo);
			errorObj.addProperty("nmerchantId", merchantId);
			errorObj.addProperty("namount", amount);
			errorObj.addProperty("terminalId", terminalId);
			errorObj.addProperty("responseCode", responseCode);
			errorObj.addProperty("message", message);
			errorObj.addProperty("retRefNo", retRefNo);
			errorObj.addProperty("batchNo", batchNo);
			errorObj.addProperty("authCode", authCode);
			errorObj.addProperty("bankID", bankID);
			errorObj.addProperty("issuerRefNo", issuerRefNo);
			errorObj.addProperty("firstName", firstName);
			errorObj.addProperty("lastName", lastName);
			errorObj.addProperty("addressZip", addressZip);
			errorObj.addProperty("hashValidated", hashValidated);
			errorObj.addProperty("errorMessage", errorMessage);
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, errorObj+"", appLogId, appLogMsgArr);
			
			response.getWriter().println("txnRefNo:"+txnRefNo+"\nmerchantId:"+merchantId+"\namount:"+amount+"\nterminalId:"+terminalId
					+"\nresponseCode:"+responseCode+"\nmessage:"+message+"\nretRefNo:"+retRefNo+"\nbatchNo:"+batchNo+"\nauthCode:"+authCode
					+"\nbankID:"+bankID+"\nissuerRefNo:"+issuerRefNo+"\nfirstName:"+firstName+"\nlastName:"+lastName+"\naddressZip:"
					+addressZip+"\nhashValidated:"+hashValidated+"\nerrorMessage:"+errorMessage);
			
			if(!"No Value Returned".equals(errorMessage)){
				errorMessage = errorMessage;
			}else{
				errorMessage = "";
			}
			
			if(appLogMsgArr.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			
			
			if(! "CORRECT".equals(hashValidated)){
				hashValidated = "INVALID HASH";
				JsonObject result = new JsonObject();
				result.addProperty("Error", hashValidated);
				result.addProperty("ErrorMessage", "Invalid Hash received. Data might be tampered with. Please contact system admin.");
				response.getWriter().print(new Gson().toJson(result));
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnRefNo+",bankRefNo="+retRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
				response.sendRedirect(portalURL);
			}
			else
			{
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnRefNo+",bankRefNo="+retRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
//			response.getWriter().println("portalURL: "+portalURL);
				response.sendRedirect(portalURL);
			}
			/*else{
				hashValidated = "<font color='#FF0066'><strong>INVALID HASH</strong></font>";
			}*/
		}
		catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("ErrorMessage", "Unable to decrypt the response");
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "-->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage() + "--->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void redirectEazyPayResponseToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String redirectURL, String pgID, String pgHdrGUID, Map configMap, String localStorageRefNo,String appLogId,JsonArray appLogMsgArr,CommonUtils commonUtils) throws IOException
	{
		String portalURL="";
		try
		{
			String walletPublicKey = "", secretKey="", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="",clientCode = "", encryptionKey="";
			/*wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecretKey"))
	        	{
	        		secretKey = paramValue;
	        	}
			}*/
			
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
				/*else if(key.equalsIgnoreCase("pgUrl"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nURL");
					wsURL = value;
				}*/
				else if(key.equalsIgnoreCase("secretKey"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nKey");
					secretKey = value;
				}
			}
			
			LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();//decObj.decrypt(request, sDecryptionKey, sSecureSecret);
			Enumeration e = request.getParameterNames();

			while (e.hasMoreElements()) {

				String fieldName = (String) e.nextElement();
				String fieldValue = request.getParameter(fieldName);

				if(! fieldName.equalsIgnoreCase("PGParams"))
				{
					if ((fieldValue != null) && (fieldValue.length() > 0)) {
						hmDecryptedValue.put(fieldName, fieldValue);
					}
				}

			}
			
			String txnID="", mandatoryfields="", optionalfields="", merchantId="", amount="", tps="", responseCode="", signature="", uniqueRefNo="", subMerchantId="", paymentMode="", tdr="", interchangeValue="", transactionDate="", totalAmount="", processingFeeAmount="", serviceTaxAmount="", errorMessage="";
			responseCode = (String) hmDecryptedValue.get("Response Code");
			uniqueRefNo = (String) hmDecryptedValue.get("Unique Ref Number");
			serviceTaxAmount = (String) hmDecryptedValue.get("Service Tax Amount");
			processingFeeAmount = (String) hmDecryptedValue.get("Processing Fee Amount");
			totalAmount = (String) hmDecryptedValue.get("Total Amount");
			amount = (String) hmDecryptedValue.get("Transaction Amount");
			transactionDate = (String) hmDecryptedValue.get("Transaction Date");
			interchangeValue = (String) hmDecryptedValue.get("Interchange Value");
			tdr = (String) hmDecryptedValue.get("TDR");
			paymentMode = (String) hmDecryptedValue.get("Payment Mode");
			subMerchantId = (String) hmDecryptedValue.get("SubMerchantId");
			txnID = (String) hmDecryptedValue.get("ReferenceNo");
			tps = (String) hmDecryptedValue.get("TPS");
			merchantId = (String) hmDecryptedValue.get("ID");
			signature = (String) hmDecryptedValue.get("RSV");
			mandatoryfields=(String) hmDecryptedValue.get("mandatory fields");
			optionalfields=(String) hmDecryptedValue.get("optional fields");
			boolean isValidResponse = true;
//			isValidResponse = verifySignature(signature, secretKey);
			
			if(isValidResponse)
			{
				JsonObject result = new JsonObject();
				result.addProperty("responseCode", responseCode);
				result.addProperty("BankRefNo", uniqueRefNo);
				result.addProperty("txnID", txnID);
				result.addProperty("serviceTaxAmount", serviceTaxAmount);
				result.addProperty("processingFeeAmount", processingFeeAmount);
				result.addProperty("totalAmount", totalAmount);
				result.addProperty("amount", amount);
				result.addProperty("transactionDate", transactionDate);
				result.addProperty("interchangeValue", interchangeValue);
				result.addProperty("tdr", tdr);
				result.addProperty("paymentMode", paymentMode);
				result.addProperty("subMerchantId", subMerchantId);
				result.addProperty("tps", tps);
				result.addProperty("merchantId", merchantId);
				result.addProperty("signature", signature);
				result.addProperty("mandatoryfields", mandatoryfields);
				result.addProperty("optionalfields", optionalfields);
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, result+"", appLogId, appLogMsgArr);
				
				String pgName="", pgMode="", nachPaymentType="", yymmddDate="", currency="INR";
				
				response.getWriter().print(new Gson().toJson(result));
//				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnID+",paymentMethod="+paymentMode+",pgName="+pgName+",pgMode="+pgMode+",nachType="+nachPaymentType+",nachSchDate="+yymmddDate+",currency="+currency+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+",localStorageRefNo="+ localStorageRefNo +")";
				response.getWriter().print("portalURL: "+portalURL);
				
				if(appLogMsgArr.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArr);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				
				response.sendRedirect(portalURL);
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("Response", "Signature not matched");
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, result+"", appLogId, appLogMsgArr);
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"--->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(),"" , buffer.toString(), appLogId, appLogMsgArr);
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void redirectRazorPayResponseToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam,String appLogId,JsonArray appLogMsgArr,CommonUtils commonUtils) throws IOException
	{
		byte[] decodedOrderId = null;
		String portalURL="";
		String  keyValue="", secretCode="", rzrpyAPIUrl="", userPass="", accountID="";
		String wholeParamString="", paramName="", paramValue="", txnID="", SparamValue="", errorCode="",  rzrpyPymntID="", rzrpySignature="", rzrpyCurrency="", expectedSignature="", expectedSignatureVal="", rzrpyTxnID ="" ,rzrpyTxnAmnt="", rzrpyTxnSts="";
		boolean isTxnValid = true;
		String txnMsg="",errorReason="",errorStep="";
		StringBuilder errorTxmessage=new StringBuilder();
		int rzpyAmount = 0;
		try
		{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			response.getWriter().println("wholeParamString: "+wholeParamString);
			String order_id = new String();
			
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	// response.getWriter().println(paramName+" ---> "+paramValue);
	        	
	        	if(paramName.equalsIgnoreCase("APIKey"))
	        	{
	        		keyValue = paramValue;
	        	}
	        	if(paramName.equalsIgnoreCase("SecretKey"))
	        	{
	        		secretCode = paramValue;
	        	}
			}
			
			response.getWriter().println("APIKey: "+ keyValue);
			response.getWriter().println("SecretKey: "+ secretCode);
			
			RazorpayClient razorpayClient = new RazorpayClient(keyValue, secretCode);
			CommonUtils utils = new CommonUtils();
			JsonObject apiResponse =  new JsonObject();
			Order order = null;
			
			Enumeration enumeration = request.getParameterNames();
	        Map<String, Object> modelMap = new HashMap<>();
	        while(enumeration.hasMoreElements()){
	            String parameterName = (String)enumeration.nextElement();
	            modelMap.put(parameterName, request.getParameter(parameterName));
	        }
	        for (String key : modelMap.keySet()) {
	        	response.getWriter().println("decryptRazorPay-modelMap: "+key + " - " + modelMap.get(key));
	        }
			
			if (null != modelMap.get("error[code]")) {
				errorCode = (String) modelMap.get("error[code]");
				if (null != modelMap.get("error[description]")) {
					 // need to check the payload															
					// need to check the Payload and added the Steps ,reason
					// code
					//txnMsg = (String) modelMap.get("error[description]");
					//errorTxmessage.append(txnMsg).append(",");
					errorTxmessage.append("Transaction failed with Reason: ");
					errorTxmessage.append(errorCode).append(" - ");
					if (null != modelMap.get("error[reason]")) {
						errorReason = (String) modelMap.get("error[reason]");
						errorTxmessage.append(errorReason).append(". ");
					}
					if (null != modelMap.get("error[step]")) {
						errorStep = (String) modelMap.get("error[step]");
						errorTxmessage.append("Step Failed:").append(errorStep);
					}

				}
			}
			
			response.getWriter().println("errorCode: "+ errorCode);
			
			if (null !=  errorCode && errorCode.trim().length() > 0) {
				if (null != request.getParameter("id") && request.getParameter("id").trim().length()> 0) {
					
					order_id =request.getParameter("id");
					decodedOrderId = Base64.getDecoder().decode(order_id);
					order_id = new String(decodedOrderId);
					
					response.getWriter().println("order_id: "+ order_id);
				}
				isTxnValid = false;
			}
			else
			{	
				if (null != modelMap.get("razorpay_order_id"))
					order_id = (String) modelMap.get("razorpay_order_id");

				response.getWriter().println("razorpay_order_id: "+ order_id);
				
				if (null != modelMap.get("razorpay_payment_id"))
					rzrpyPymntID = (String) modelMap.get("razorpay_payment_id");
				
				response.getWriter().println("razorpay_payment_id: "+ rzrpyPymntID);

				if (null != modelMap.get("razorpay_signature"))
					rzrpySignature = (String) modelMap.get("razorpay_signature");
				
				response.getWriter().println("razorpay_signature: "+ rzrpySignature);
				
				expectedSignature =  order_id + '|' + rzrpyPymntID;
				expectedSignatureVal =  generateRazorPaySignature(expectedSignature, secretCode);
				
				response.getWriter().println("expectedSignatureVal: "+ expectedSignatureVal);
				
				if (expectedSignatureVal.equals(rzrpySignature)) {
					rzrpyTxnSts = "00200";
					rzrpyTxnID = order_id;
				}else {
					isTxnValid = false;
				}
			}
			order = razorpayClient.Orders.fetch(order_id);
			response.getWriter().println("orderOBJ: "+ order);
			
//			rzrpyTxnAmnt = order.get("amount");
			rzpyAmount = order.get("amount");
			if (rzpyAmount > 0) {
				rzpyAmount = rzpyAmount/100;
			} else {
				rzpyAmount= 0;
			}
			
			rzrpyCurrency = order.get("currency");

			if (isTxnValid) {
				txnID = order.get("receipt");
				//txnMsg=
				if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))) {
					rzrpyAPIUrl = "https://api.razorpay.com/v1/orders/"+order_id+"/?expand[]=transfers";
					userPass = keyValue+":"+secretCode;
					response.getWriter().println("userPass: "+ userPass);
					
					apiResponse = utils.executeURL(rzrpyAPIUrl, userPass, response);
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, apiResponse+"", appLogId, appLogMsgArr);
					response.getWriter().println("apiResponse: "+ apiResponse);
					accountID = apiResponse.getAsJsonObject("transfers").getAsJsonArray("items").get(0).getAsJsonObject().get("recipient").getAsString();
					response.getWriter().println("apiResponse.accountID: "+ accountID);
				}else{
					accountID="";
					response.getWriter().println("else accountID: "+ accountID);
				}
			}
			else
			{
				txnID = order.get("receipt");
				rzrpyPymntID ="";
				accountID = "";
				rzrpyTxnID = order_id;
				rzrpyTxnSts = "000320";


			}
			rzrpyTxnAmnt =""+rzpyAmount;
			
			response.getWriter().println("redirectURL: "+ redirectURL);
			response.getWriter().println("rzrpyTxnSts: "+ rzrpyTxnSts);
			response.getWriter().println("accountID: "+ accountID);
			response.getWriter().println("rzrpyPymntID: "+ rzrpyPymntID);
			response.getWriter().println("rzrpyTxnID: "+ rzrpyTxnID);
			response.getWriter().println("rzrpyCurrency: "+ rzrpyCurrency);
			response.getWriter().println("pgHdrGUID: "+ pgHdrGUID);
			response.getWriter().println("rzrpyTxnAmnt: "+ rzrpyTxnAmnt);
//			String respTxnAmt = ""+rzrpyTxnAmnt;
			JsonObject errorObj=new JsonObject();
			errorObj.addProperty("redirectURL", redirectURL);
			errorObj.addProperty("txnStatus", rzrpyTxnSts);
			errorObj.addProperty("accountid", accountID);
			errorObj.addProperty("bankRefNo", rzrpyTxnID);
			errorObj.addProperty("txnID", txnID);
			errorObj.addProperty("pgTxnID", rzrpyPymntID);
			errorObj.addProperty("txnCurrency", rzrpyCurrency);
			errorObj.addProperty("txnSessionID", pgHdrGUID);
			errorObj.addProperty("txnAmount", rzrpyTxnAmnt);
			errorObj.addProperty("source", "PG");
			errorObj.addProperty("PGID", "pgID");
			
			
			
			if (null != modelMap.get("error[code]")) {
				portalURL = redirectURL + "/(txnStatus=" + rzrpyTxnSts + ",accountid=" + accountID + ",bankRefNo="
						+ rzrpyTxnID + ",txnID=" + txnID + ",pgTxnID=" + rzrpyPymntID + ",txnCurrency=" + rzrpyCurrency
						+ ",txnSessionID=" + pgHdrGUID + ",txnAmount=" + rzrpyTxnAmnt + ",source=PG,PGID=" + pgID
						+ ",txnMsg=" +errorTxmessage.toString() +")";
				errorObj.addProperty("txnMsg", errorTxmessage.toString());
			} else {
				portalURL = redirectURL + "/(txnStatus=" + rzrpyTxnSts + ",accountid=" + accountID + ",bankRefNo="
						+ rzrpyTxnID + ",txnID=" + txnID + ",pgTxnID=" + rzrpyPymntID + ",txnCurrency=" + rzrpyCurrency
						+ ",txnSessionID=" + pgHdrGUID + ",txnAmount=" + rzrpyTxnAmnt + ",source=PG,PGID=" + pgID +",txnMsg="+"success"+")";
				errorObj.addProperty("txnMsg", "success");
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnID, errorObj+"", appLogId, appLogMsgArr);
			response.getWriter().print("portalURL: "+portalURL);
			
			if(appLogMsgArr.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				JsonObject onObjRes = commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			  	response.getWriter().println("onObjRes:"+onObjRes);
			}

			response.getWriter().println("getServerName: "+request.getServerName());
			response.getWriter().println("getServerPort: "+request.getServerPort());
			response.getWriter().println("host: "+request.getHeader("host"));
			//Host hardcoded to check java app on CF and portal on Neo. Comment the below line when testing with CF portal
			// portalURL = "https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com"+portalURL;
			response.getWriter().println("final portalURL: "+portalURL);
			// response.sendRedirect(portalURL);
		}
		catch (Exception e)
		{
			response.getWriter().println(displayErrorForWeb(e));
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"--->");
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"-->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);	
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			response.getWriter().print(new Gson().toJson(result));
		}
	}

	private void decryptTechProcessPortalResponse(HttpServletRequest request, HttpServletResponse response, 
			String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, 
			String siteID, String appID, String actionName, String navParam, String appLogID, 
			JsonArray appLogMsgArr, CommonUtils commonUtils) throws IOException{
		String responsePayLoad = "", portalURL="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		String merchantCode="", encryptionIV="", encryptionKey="";
		String currency="", txnStatus="", txnMsg="", txnErrMsg="", txnID="", bankCode="", tpslTxnID="", txnAmnt="", clientReqMeta="", txnDate="", bankRefNo="";
    	try
    	{
    		String wholeParamString = "", paramName = "", paramValue = "", secretCode = "";
			wholeParamString = configurationValues;
			currency = properties.getProperty("TPSLCurrency");
			String[] splitResult = wholeParamString.split("\\|");
			for (String s : splitResult) {
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=") + 1, s.length());
				
				if (paramName.equalsIgnoreCase("MerchantCode")) {
					merchantCode = paramValue;
				}

				if (paramName.equalsIgnoreCase("EncryptionIV")) {
					encryptionIV = paramValue;
				}
				
				if (paramName.equalsIgnoreCase("EncryptionKey")) {
					encryptionKey = paramValue;
				}
			}
    		
    		
        	TransactionResponseBean transactionResponseBean = new TransactionResponseBean();
        	
        	transactionResponseBean.setKey(encryptionKey.getBytes());
        	transactionResponseBean.setIv(encryptionIV.getBytes());
        	transactionResponseBean.setResponsePayload(responseMessage);
        	
        	responsePayLoad = transactionResponseBean.getResponsePayload();
//        	if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				response.getWriter().println("responsePayLoad: "+responsePayLoad);
				response.getWriter().println("responseMessage: "+responseMessage);
//			}
        	
        	String[] result = responsePayLoad.split("\\|");
	        String resParamValue = "";
	        String resParamName = "";
	        
	        
	        for(String s : result)
	        {
	        	resParamValue = "";
	        	resParamName = "";
	        	
	        	if(! s.equalsIgnoreCase("ERROR037")){
	        		resParamName = s.substring(0, s.indexOf("="));
	        		resParamValue = s.substring(s.indexOf("=")+1,s.length());
	        		
	        		if(resParamName.equalsIgnoreCase("txn_status"))
		        	{
	        			txnStatus = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("txn_msg"))
		        	{
		        		txnMsg = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("txn_err_msg"))
		        	{
		        		txnErrMsg = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("clnt_txn_ref"))
		        	{
		        		txnID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("tpsl_bank_cd"))
		        	{
		        		bankCode = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("tpsl_txn_id"))
		        	{
		        		tpslTxnID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("txn_amt"))
		        	{
		        		txnAmnt = resParamValue;	
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("clnt_rqst_meta"))
		        	{
		        		clientReqMeta = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("tpsl_txn_time"))
		        	{
		        		txnDate = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("tpsl_rfnd_id"))
		        	{
		        		if(null != resParamValue && !resParamValue.equalsIgnoreCase("NA") && resParamValue.toString().trim().length() > 0)
		        		{
		        			bankRefNo = resParamValue;	
		        		}
		        		else
		        		{
		        			bankRefNo = "";
		        		}
		        	}
	        	}
	        }
        	
        	portalURL = redirectURL + "/(txnStatus=" + txnStatus + ",accountid=,bankRefNo="
					+ bankRefNo + ",txnID=" + txnID + ",pgTxnID=" + tpslTxnID + ",txnCurrency=" + currency
					+ ",txnSessionID=" + pgHdrGUID + ",txnAmount=" + txnAmnt + ",source=PG,PGID=" + pgID +",txnMsg="+"success"+",responsePayLoad=" + responsePayLoad + ",msg=" + responseMessage + ")";
    	
        	response.sendRedirect(portalURL);
    	}
    	catch (Exception e)
    	{
//    		e.printStackTrace();
    		JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			StringBuffer buffer = new StringBuffer(e.getClass().getCanonicalName() + "--->");
			if (e.getLocalizedMessage() != null) {
				buffer.append(e.getLocalizedMessage() + "-->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);	
			}
			response.getWriter().println(buffer.toString());
//			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void decryptTechprocessResponse(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String pgID) throws IOException{
		boolean debug=false;
		String workingKey="", accessCode="", decryptedResponse="";
		
		try{
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("APIAccessKey")){
	        		accessCode = paramValue;
	        	}
			}
			
			if(accessCode.trim().length() > 0 && workingKey.trim().length() > 0){
				TransactionResponseBean responseBean = new TransactionResponseBean();
				responseBean.setIv(accessCode.getBytes());
				responseBean.setKey(workingKey.getBytes());
				/*responseBean.setIv("1035230358GXHBMP".getBytes());
				responseBean.setKey("9514251011IHWJNW".getBytes());*/
				responseBean.setResponsePayload(responseMessage);
				
				decryptedResponse = responseBean.getResponsePayload();
				
				response.getWriter().println(decryptedResponse);
			}else{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Keys not found or wrong keys configured");
			}
		}catch (Exception e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("decryptTechprocessResponse.Exception: "+e.getLocalizedMessage());
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("decryptTechprocessResponse.Full Stack Trace: "+buffer.toString());
			}
		}
	}
	
	private void decryptCCAvenueResponse(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, 
			String pgID) throws IOException{
		boolean debug=false;
		String workingKey="", accessCode="", decryptedResponse="", aggregatorID="";
		try{
			
			aggregatorID = request.getParameter("AGGRID");
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			
			if(debug)
				response.getWriter().println("responseMessage: "+responseMessage);
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey")){
	        		workingKey = paramValue;
	        	}
			}
			
			Arrays.fill(splitResult, null);
			
			if(debug)
				response.getWriter().println("workingKey: "+workingKey);
			
			if(workingKey.trim().length() > 0){
				AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
				decryptedResponse = aesUtil.decrypt(responseMessage);
				if(debug)
					response.getWriter().println("1st decryptedResponse: "+decryptedResponse);
				
				if(null == decryptedResponse || decryptedResponse.trim().equalsIgnoreCase("null")){
					if(aggregatorID.equalsIgnoreCase("AGGR0108")){
						configurationValues = getConstantValuesForDecryption(request, response, "", "MOBILECCA");
						decryptedResponse = "";
						wholeParamString=""; paramName=""; paramValue="";
						wholeParamString = configurationValues;
						
						splitResult = wholeParamString.split("\\|");
						
						for(String s : splitResult)
						{
				        	paramName = s.substring(0, s.indexOf("="));
				        	paramValue = s.substring(s.indexOf("=")+1,s.length());
				        	
				        	if(paramName.equalsIgnoreCase("EncryptionKey")){
				        		workingKey = paramValue;
				        	}
						}
						
						if(debug)
							response.getWriter().println("workingKey.mobilecca: "+workingKey);
						
						if(workingKey.trim().length() > 0){
							AesCryptUtil aesUtilMobile=new AesCryptUtil(workingKey);
							decryptedResponse = aesUtilMobile.decrypt(responseMessage);
							
							response.getWriter().println(decryptedResponse);
						}else{
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							response.getWriter().println("Keys not found or wrong keys configured");
						}
					}
				}else{
					response.getWriter().println(decryptedResponse);
				}
			}else{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Keys not found or wrong keys configured");
			}
		}catch (Exception e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("decryptCCAvenueResponse.Exception: "+e.getLocalizedMessage());
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("decryptCCAvenueResponse.Full Stack Trace: "+buffer.toString());
			}
		}
	}
	
	private void redirectCCAvenueResponseToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, 
			String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam,String appLogId, JsonArray appLogMsgArr) throws IOException{
		String portalURL="", decryptedResponse="", csrfToken="", updateTxnRes="";
		String merchantCode="", txnUrl="", workingKey="", accessCode="", configHdrGuid="";
		boolean debug=true;
		String postTransaction = "", paymentStatus="";
		CommonUtils commonUtils = new CommonUtils();
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
//			debug = true;
//			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
//				debug = true;
//			}
			
			response.getWriter().println("responseMessage: "+responseMessage);
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("CHGUID")){
	        		configHdrGuid = paramValue;
	        	}
			}
			
			if(debug){
				response.getWriter().println("merchantCode: "+merchantCode);
				response.getWriter().println("pgID: "+pgID);
				response.getWriter().println("txnUrl: "+txnUrl);
				response.getWriter().println("workingKey: "+workingKey);
				response.getWriter().println("accessCode: "+accessCode);
				response.getWriter().println("redirectURL: "+redirectURL);
				response.getWriter().println("siteID: "+siteID);
				response.getWriter().println("responseMessage: "+responseMessage);
				response.getWriter().println("pgHdrGUID: "+pgHdrGUID);
			}
			
			AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
			decryptedResponse = aesUtil.decrypt(responseMessage);
			
			response.getWriter().println("decryptedResponse: "+decryptedResponse);
			
			String respParamName="", respParamVal="";
			String responseCode="", txnRefNo="", bankRefNo="", amount="", statusMsg="", encrPGGuid="";
			
			String[] splitDecrResult = decryptedResponse.split("\\&");
			for(String s : splitDecrResult){
				respParamName = s.substring(0, s.indexOf("="));
				respParamVal = s.substring(s.indexOf("=")+1,s.length());
				response.getWriter().println("Splitted Response: Parameter "+respParamName+"'s Value is: "+respParamVal);
				
				if(respParamName.equalsIgnoreCase("order_id"))
					txnRefNo = respParamVal;
				
				if(respParamName.equalsIgnoreCase("tracking_id"))
					bankRefNo = respParamVal;
				
				if(respParamName.equalsIgnoreCase("order_status")){
					if(respParamVal.length() > 6)
						responseCode = respParamVal.substring(0, 6);
					else
						responseCode = respParamVal;
				}
				
				if(respParamName.equalsIgnoreCase("status_message"))
					statusMsg = respParamVal;
				
				if(respParamName.equalsIgnoreCase("amount"))
					amount = respParamVal;
				
				if(respParamName.equalsIgnoreCase("merchant_param4")){
					if(respParamVal != null && respParamVal.trim().length() > 0)
						encrPGGuid = respParamVal;
				}
			}
			
			responseCode = responseCode.toUpperCase();
			
			response.getWriter().println("txnRefNo: "+txnRefNo);
			response.getWriter().println("bankRefNo: "+bankRefNo);
			response.getWriter().println("responseCode: "+responseCode);
			response.getWriter().println("statusMsg: "+statusMsg);
			response.getWriter().println("amount: "+amount);
			response.getWriter().println("encrPGGuid: "+encrPGGuid);
			
			JsonObject errorObj=new JsonObject();
			errorObj.addProperty("txnRefNo", txnRefNo);
			errorObj.addProperty("bankRefNo", bankRefNo);
			errorObj.addProperty("responseCode", responseCode);
			errorObj.addProperty("statusMsg", statusMsg);
			errorObj.addProperty("amount", amount);
			errorObj.addProperty("encrPGGuid", encrPGGuid);
			
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, errorObj+"", appLogId, appLogMsgArr);
			
			if(destAggrID.equalsIgnoreCase("AGGR0108")){
				String isPGTxnIDUsed="";
				if(pgHdrGUID.equalsIgnoreCase(encrPGGuid)){
					isPGTxnIDUsed = commonUtils.isBankTrackIDProcessed(request, response, encrPGGuid, bankRefNo, debug);
					response.getWriter().println("isPGTxnIDUsed: "+isPGTxnIDUsed);
					
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, isPGTxnIDUsed, appLogId, appLogMsgArr);
					
					if(isPGTxnIDUsed.equalsIgnoreCase("No")){
						paymentStatus = commonUtils.getConfigStatus(request, response, encrPGGuid, responseCode, configHdrGuid, debug);
						response.getWriter().println("paymentStatus: "+paymentStatus);
						if(paymentStatus.equalsIgnoreCase("Status configurations not maintained")
						  || paymentStatus.equalsIgnoreCase("PGPymntConfigStatsEntity returned null when trying to connect to the backend")
						  || paymentStatus.contains("Error") || paymentStatus.contains("Exception")){
							commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, paymentStatus+" Error while retrieving Payment Status from ERP", appLogId, appLogMsgArr);
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							response.setContentType("text/plain");
							response.getWriter().println("Error while retrieving Payment Status from ERP: "+paymentStatus);
						}else{
							csrfToken = generateCSRFTokenForPCGW(request, response, debug);
							
							if(csrfToken.equalsIgnoreCase("Failure") || csrfToken.contains("Exception")){
								commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, csrfToken+" Error while generating CSRF token", appLogId, appLogMsgArr);
								//Failure redirection
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.setContentType("text/plain");
								response.getWriter().println("Error while generating CSRF token "+csrfToken);
							}else{
								updateTxnRes = updatePGTransaction(request, response, encrPGGuid, txnRefNo, bankRefNo, responseCode, paymentStatus, configHdrGuid, csrfToken, statusMsg, debug);
								String errorCode="", errorMsg="";
								if(updateTxnRes.equalsIgnoreCase("204")){
									errorCode = errorCode.replaceAll("\\/", "\\|");
									portalURL = redirectURL+"/(id="+encrPGGuid+",errorCode="+errorCode+",errorMsg="+errorMsg+",source=PG)";
									if(debug)
										response.getWriter().println("portalURL: "+portalURL);
									if(appLogMsgArr.size()>0){
										JsonObject appLogMsg=new JsonObject();
										JsonObject onObjEvent=new JsonObject();
										appLogMsg.addProperty("Object", "ApplicationLog");
										appLogMsg.addProperty("Event", "INSERT");
										appLogMsg.addProperty("AggregatorID", destAggrID);
										appLogMsg.addProperty("Identifier", "000002");
										appLogMsg.add("Message", appLogMsgArr);
										onObjEvent.add("OnObjectEvent", appLogMsg);
										commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
									}
									
									response.sendRedirect(portalURL);//Temporarily
								}else{
									JsonObject responseJsonObject = new JsonObject();
									try{
										JsonParser cpiResponseParser = new JsonParser();
										responseJsonObject = (JsonObject)cpiResponseParser.parse(updateTxnRes);
										
										if(responseJsonObject.has("error")){
											errorCode = responseJsonObject.get("error").getAsJsonObject().get("code").getAsString();
											errorMsg = responseJsonObject.get("error").getAsJsonObject().get("message").getAsJsonObject().get("value").getAsString();
										}else{
											errorCode = "HTTP Error";
											errorMsg = updateTxnRes;
										}
										
										errorCode = errorCode.replaceAll("\\/", "\\|");
										
										commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, updateTxnRes, appLogId, appLogMsgArr);
										
										portalURL = redirectURL+"/(id="+encrPGGuid+",errorCode="+errorCode+",errorMsg="+errorMsg+",source=PG)";
										if(debug)
											response.getWriter().println("portalURL: "+portalURL);
										if(appLogMsgArr.size()>0){
											JsonObject appLogMsg=new JsonObject();
											JsonObject onObjEvent=new JsonObject();
											appLogMsg.addProperty("Object", "ApplicationLog");
											appLogMsg.addProperty("Event", "INSERT");
											appLogMsg.addProperty("AggregatorID", destAggrID);
											appLogMsg.addProperty("Identifier", "000002");
											appLogMsg.add("Message", appLogMsgArr);
											onObjEvent.add("OnObjectEvent", appLogMsg);
											commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
										}
										
										response.sendRedirect(portalURL);//Temporarily
									}catch (Exception e) {
										errorCode = e.getClass().getSimpleName();
										errorMsg = updateTxnRes;
										errorCode = errorCode.replaceAll("\\/", "\\|");
										portalURL = redirectURL+"/(id="+encrPGGuid+",errorCode="+errorCode+",errorMsg="+errorMsg+",source=PG)";
										if(debug)
											response.getWriter().println("portalURL: "+portalURL);
										
										StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName()+"--->");
										if(e.getLocalizedMessage()!=null){
											buffer.append(e.getLocalizedMessage()+"---->");
										}
										StackTraceElement[] stackTrace = e.getStackTrace();
										for(int i=0;i<stackTrace.length;i++){
											buffer.append(stackTrace[i]);
										}
										commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, buffer.toString(), appLogId, appLogMsgArr);
										if(appLogMsgArr.size()>0){
											JsonObject appLogMsg=new JsonObject();
											JsonObject onObjEvent=new JsonObject();
											appLogMsg.addProperty("Object", "ApplicationLog");
											appLogMsg.addProperty("Event", "INSERT");
											appLogMsg.addProperty("AggregatorID", destAggrID);
											appLogMsg.addProperty("Identifier", "000002");
											appLogMsg.add("Message", appLogMsgArr);
											onObjEvent.add("OnObjectEvent", appLogMsg);
											commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
										}
										response.sendRedirect(portalURL);//Temporarily
									}
								}
							}
						}
					}else{
						String errorCode="", errorMsg="";
						errorCode = "J101";
						errorMsg = isPGTxnIDUsed;
						errorCode = errorCode.replaceAll("\\/", "\\|");
						portalURL = redirectURL+"/(id="+encrPGGuid+",errorCode="+errorCode+",errorMsg="+errorMsg+",source=PG)";
						if(debug)
							response.getWriter().println("portalURL: "+portalURL);
						
						commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", errorMsg, appLogId, appLogMsgArr);
						if(appLogMsgArr.size()>0){
							JsonObject appLogMsg=new JsonObject();
							JsonObject onObjEvent=new JsonObject();
							appLogMsg.addProperty("Object", "ApplicationLog");
							appLogMsg.addProperty("Event", "INSERT");
							appLogMsg.addProperty("AggregatorID", destAggrID);
							appLogMsg.addProperty("Identifier", "000002");
							appLogMsg.add("Message", appLogMsgArr);
							onObjEvent.add("OnObjectEvent", appLogMsg);
							commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
						}
						response.sendRedirect(portalURL);//Temporarily
					}
				}else{
					String errorCode="", errorMsg="";
					errorCode = "J100";
					errorMsg = "Unable to process the response from PG. Attempt to process a record which is already processed";
					errorCode = errorCode.replaceAll("\\/", "\\|");
					portalURL = redirectURL+"/(id="+encrPGGuid+",errorCode="+errorCode+",errorMsg="+errorMsg+",source=PG)";
					if(debug)
						response.getWriter().println("portalURL: "+portalURL);
					
					commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", errorMsg, appLogId, appLogMsgArr);
					if(appLogMsgArr.size()>0){
						JsonObject appLogMsg=new JsonObject();
						JsonObject onObjEvent=new JsonObject();
						appLogMsg.addProperty("Object", "ApplicationLog");
						appLogMsg.addProperty("Event", "INSERT");
						appLogMsg.addProperty("AggregatorID", destAggrID);
						appLogMsg.addProperty("Identifier", "000002");
						appLogMsg.add("Message", appLogMsgArr);
						onObjEvent.add("OnObjectEvent", appLogMsg);
						commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
					}
					response.sendRedirect(portalURL);//Temporarily
				}
			}else{
				portalURL = redirectURL+"/(txnStatus="+responseCode+",statusMsg="+statusMsg+",txnID="+txnRefNo+",bankRefNo="+bankRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
				if(debug)
					response.getWriter().println("portalURL: "+portalURL);
				
				commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), txnRefNo, portalURL, appLogId, appLogMsgArr);
				if(appLogMsgArr.size()>0){
					JsonObject appLogMsg=new JsonObject();
					JsonObject onObjEvent=new JsonObject();
					appLogMsg.addProperty("Object", "ApplicationLog");
					appLogMsg.addProperty("Event", "INSERT");
					appLogMsg.addProperty("AggregatorID", destAggrID);
					appLogMsg.addProperty("Identifier", "000002");
					appLogMsg.add("Message", appLogMsgArr);
					onObjEvent.add("OnObjectEvent", appLogMsg);
					commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
				}
				response.sendRedirect(portalURL);
			}
		}catch (Exception e){
			response.getWriter().println(displayErrorForWeb(e));
			
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			StringBuffer buffer=new StringBuffer(e.getClass().getCanonicalName());
			if(e.getLocalizedMessage()!=null){
				buffer.append(e.getLocalizedMessage()+"--->");
			}
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			commonUtils.crtAppLogMsgsObj(request,response,request.getServletPath(), "", buffer.toString(), appLogId, appLogMsgArr);
			
			if(appLogMsgArr.size()>0){
				JsonObject appLogMsg=new JsonObject();
				JsonObject onObjEvent=new JsonObject();
				appLogMsg.addProperty("Object", "ApplicationLog");
				appLogMsg.addProperty("Event", "INSERT");
				appLogMsg.addProperty("AggregatorID", destAggrID);
				appLogMsg.addProperty("Identifier", "000002");
				appLogMsg.add("Message", appLogMsgArr);
				onObjEvent.add("OnObjectEvent", appLogMsg);
				commonUtils.callOnObjectEventPublish(response, onObjEvent, false);
			}
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	/* JKT RnD
	 * private void redirectCCAvenueResponseToPortal(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam) throws IOException{
		String portalURL="", decryptedResponse="";
		String merchantCode="", txnUrl="", workingKey="", accessCode="";
		boolean debug=true;
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
//			debug = true;
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			
			response.getWriter().println("responseMessage: "+responseMessage);
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
			}
			
			if(debug){
				response.getWriter().println("merchantCode: "+merchantCode);
				response.getWriter().println("pgID: "+pgID);
				response.getWriter().println("txnUrl: "+txnUrl);
				response.getWriter().println("workingKey: "+workingKey);
				response.getWriter().println("accessCode: "+accessCode);
				response.getWriter().println("redirectURL: "+redirectURL);
				response.getWriter().println("siteID: "+siteID);
				response.getWriter().println("responseMessage: "+responseMessage);
			}
			
			AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
			decryptedResponse = aesUtil.decrypt(responseMessage);
			
			response.getWriter().println("decryptedResponse: "+decryptedResponse);
			
			String respParamName="", respParamVal="";
			String responseCode="", txnRefNo="", bankRefNo="", amount="", statusMsg="";
			
			String[] splitDecrResult = decryptedResponse.split("\\&");
			for(String s : splitDecrResult){
				respParamName = s.substring(0, s.indexOf("="));
				respParamVal = s.substring(s.indexOf("=")+1,s.length());
				
				response.getWriter().println("Splitted Response: Parameter "+respParamName+"'s Value is: "+respParamVal);
				
				if(respParamName.equalsIgnoreCase("order_id"))
					txnRefNo = respParamVal;
				
				if(respParamName.equalsIgnoreCase("tracking_id"))
					bankRefNo = respParamVal;
				
				if(respParamName.equalsIgnoreCase("order_status")){
					if(respParamVal.length() > 6)
						responseCode = respParamVal.substring(0, 6);
					else
						responseCode = respParamVal;
				}
				
				if(respParamName.equalsIgnoreCase("status_message"))
					statusMsg = respParamVal;
				
				
				if(respParamName.equalsIgnoreCase("amount"))
					amount = respParamVal;
			}
			
			responseCode = responseCode.toUpperCase();
			
			response.getWriter().println("txnRefNo: "+txnRefNo);
			response.getWriter().println("bankRefNo: "+bankRefNo);
			response.getWriter().println("responseCode: "+responseCode);
			response.getWriter().println("statusMsg: "+statusMsg);
			response.getWriter().println("amount: "+amount);
			
			portalURL = redirectURL+"/(txnStatus="+responseCode+",statusMsg="+statusMsg+",txnID="+txnRefNo+",bankRefNo="+bankRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
			
			if(debug)
				response.getWriter().println("portalURL: "+portalURL);
			
			HttpSession session = request.getSession();
			session.setAttribute("txnStatus", responseCode);
			session.setAttribute("statusMsg", statusMsg);
			session.setAttribute("txnRefNo", txnRefNo);
			session.setAttribute("pgHdrGUID", pgHdrGUID);
			
			
			response.setHeader("txnStatus", responseCode);
			response.setHeader("statusMsg", statusMsg);
			response.setHeader("txnRefNo", txnRefNo);
			response.setHeader("pgHdrGUID", pgHdrGUID);
			
			request.setAttribute("txnStatus", responseCode);
			request.setAttribute("statusMsg", statusMsg);
			request.setAttribute("txnRefNo", txnRefNo);
			request.setAttribute("pgHdrGUID", pgHdrGUID);
			Cookie cookie = new Cookie("txnStatus", responseCode);
			cookie = new Cookie("statusMsg", statusMsg);
			cookie = new Cookie("txnRefNo", txnRefNo);
			cookie = new Cookie("pgHdrGUID", pgHdrGUID);
			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			response.setHeader("Location", portalURL);
			
//			response.addCookie(cookie);
			
//			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", portalURL);
			response.addHeader("txnStatus", responseCode);
			response.addHeader("statusMsg", statusMsg);
			response.addHeader("txnRefNo", txnRefNo);
			response.addHeader("pgHdrGUID", pgHdrGUID);
			response.sendRedirect(portalURL);
//			request.
//			request.getSession().getServletContext().s
			request.setAttribute("portalURL",portalURL);
			RequestDispatcher view = request.getRequestDispatcher("/TopUp.jsp");
//			RequestDispatcher view = request.getRequestDispatcher(portalURL); Returns 404 not found
			view.forward(request,response);
		}catch (Exception e){
			response.getWriter().println(displayErrorForWeb(e));
			
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			response.getWriter().print(new Gson().toJson(result));
		}
	}*/
	
	private String generateRazorPaySignature(String expectedSignature, String secret) throws SignatureException
	{
		String result="";
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA256_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
			mac.init(signingKey);
			 
			byte[] rawHmac = mac.doFinal(expectedSignature.getBytes());
			result = DatatypeConverter.printHexBinary(rawHmac).toLowerCase();
		} catch (Exception e) {
			result="";
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
	    }
		return result;
	}
	
	private boolean verifySignature(String responsePayLoadKey, String secretKey) throws IOException
	{
		boolean isValidResponse = false;
		try
		{
			String localHashString="", responseHashSignature="", localHashSignature=""; 
			
			responseHashSignature  = new ResPymtGateway().hashCal("SHA-512",responsePayLoadKey);
			
			localHashString = responsePayLoadKey;
			localHashString = localHashString.replaceAll(localHashString.substring(localHashString.lastIndexOf("|")+1, localHashString.length()), secretKey);
			
			localHashSignature = new ResPymtGateway().hashCal("SHA-512",localHashString);
			
			if(responseHashSignature.equals(localHashSignature))
			{
				isValidResponse = true;
			}
		}
		catch (Exception e) {
		}
		return isValidResponse;
	}
	
	public String hashCal(String type,String str)
	{
		byte[] hashseq=str.getBytes();
		StringBuffer hexString = new StringBuffer();
		try
		{
			MessageDigest algorithm = MessageDigest.getInstance(type);
			algorithm.reset();
			algorithm.update(hashseq);
			byte messageDigest[] = algorithm.digest();
			for (int i=0;i<messageDigest.length;i++) 
			{
				String hex=Integer.toHexString(0xFF & messageDigest[i]);
				if(hex.length()==1) hexString.append("0");
				hexString.append(hex);
			}
		}
		catch(NoSuchAlgorithmException nsae)
		{
			
		}
		return hexString.toString();
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		/*
		try
		{
			String siteID="", appID="", actionName="", navParam = "", localStorageRefNo = "", portalURL="", pgID="", responseMessage="", redirectURL="", pgHdrGUID="", pgParams="", paymentGtwyID="";
			String configurationValues = "";
			String aggregatorID = "", source="";
			servletPath = request.getServletPath();
//			response.getWriter().println("servletPath: "+servletPath);
			
			//			if(null != request.getParameter("PGID"))
//				pgID = request.getParameter("PGID");
			
			if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
				if(null != request.getParameter("PGParams"))
					pgParams = request.getParameter("PGParams");
				
				String[] resSplitResult = pgParams.split("\\~");
				String resParamName="", resParamValue="";
				for(String s : resSplitResult){
//					response.getWriter().println("q: "+s);
					resParamName = s.substring(0, 5);
					resParamValue = s.substring(5,s.length());
//					response.getWriter().println("resParamName: "+resParamName);
//					response.getWriter().println("resParamValue: "+resParamValue);
		        	if(resParamName.equalsIgnoreCase("PGIDS")){
		        		pgID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("PGGID")){
		        		pgHdrGUID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("STEID")){
		        		siteID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("APPID")){
		        		appID = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("ACTON")){
		        		actionName = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("NAVPM")){
		        		navParam = resParamValue;
		        	}
		        	
		        	if(resParamName.equalsIgnoreCase("REFNO")){
		        		localStorageRefNo = resParamValue;
		        	}
				}
				
				if(siteID.trim().length() == 32){
					String newSiteID = siteID.substring(0, 8) + "-" + siteID.substring(8,12)+"-"+siteID.substring(12,16)+"-"+siteID.substring(16,20)+"-"+siteID.substring(20);
					redirectURL = "/sites?siteId="+newSiteID+"#"+appID+"-"+actionName+"&/"+navParam;
				}else{
					redirectURL = "/sites/"+siteID+"#"+appID+"-"+actionName+"&/"+navParam;
				}
			}else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
				pgID = request.getParameter("PGID");
				aggregatorID = request.getParameter("AGGRID");
				
				if(null != request.getParameter("source") && request.getParameter("source").trim().length() > 0){
					source = request.getParameter("source");
				}
			}
			
			if(null != pgID && pgID.trim().length() > 0){
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				
				if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes")){
					configurationValues = getConstantValues(request, response, "", pgID);
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("configurationValues: "+configurationValues);
				}else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
					//Get keys from here and store in configurationValues variable here
					if(source.trim().equalsIgnoreCase("mobile")){
						configurationValues = getConstantValues(request, response, "", pgID);
					}else{
						configurationValues = getConstantValuesForDecryption(request, response, "", pgID);
					}
				}
				
				boolean isAggrEmpty = false;
				if(configurationValues.trim().length() > 0 && !configurationValues.equalsIgnoreCase("E106")){
					if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
						if(source.trim().equalsIgnoreCase("")){
							if(aggregatorID == null || aggregatorID.trim().length() == 0){
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.setContentType("text/plain");
								response.getWriter().println("AGGRID is mandatory");
								isAggrEmpty = true;
							}else{
								isAggrEmpty = false;
							}
						}
					}
					
					if(! isAggrEmpty){
						if(pgID.equalsIgnoreCase(properties.getProperty("AxisPGID"))){
							if(null != request.getParameter("i"))
								responseMessage = request.getParameter("i");
							if(responseMessage != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectAxisMessageToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID"))){
							if(null != request.getParameter("walletResponseMessage"))
								responseMessage = request.getParameter("walletResponseMessage");
							
							if(responseMessage != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectICICIMessageToPortal(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configurationValues, localStorageRefNo);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if(pgID.equalsIgnoreCase(properties.getProperty("YesPGID"))){
							if(request.getParameterNames() != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectPayUMessageToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if(pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID"))){
							CommonUtils readConfig = new CommonUtils();
							Map<String, String> configMap = readConfig.readConfigValues(pgID, "T");
							
							if(request.getParameterNames() != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectEazyPayResponseToPortal(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configMap, localStorageRefNo);	
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")) || pgID.equalsIgnoreCase("RZRPAY")){
							if(request.getParameterNames() != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectRazorPayResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
							if(request.getParameterNames() != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectRazorPayResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse"))
									response.getWriter().println("Work Under Progress");
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if (pgID.equalsIgnoreCase(properties.getProperty("CCAvenuePGID")) 
							|| pgID.equalsIgnoreCase(properties.getProperty("MobileCCPGID"))
							|| pgID.equalsIgnoreCase(properties.getProperty("CCAICICI"))){
							if(null != request.getParameter("encResp")){
								responseMessage = request.getParameter("encResp");
							}else{
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.setContentType("text/plain");
								response.getWriter().println("Bank response is mandatory");
							}
							
							if(responseMessage != null){
								if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
									redirectCCAvenueResponseToPortal(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
								else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
//									String respCC="order_id=202187000000686&tracking_id=309006142300&bank_ref_no=1592976553646&order_status=Success&failure_message=&payment_mode=Net Banking&card_name=AvenuesTest&status_code=null&status_message=Y&currency=INR&amount=33405.00&billing_name=&billing_address=&billing_city=&billing_state=&billing_zip=&billing_country=&billing_tel=&billing_email=&delivery_name=&delivery_address=&delivery_city=&delivery_state=&delivery_zip=&delivery_country=&delivery_tel=&merchant_param1=JKTIL&merchant_param2=1100644&merchant_param3=ARIHANT TYRES&merchant_param4=&merchant_param5=&vault=N&offer_type=null&offer_code=null&discount_value=0.0&mer_amount=33405.00&sub_account_id=JKTIL&eci_value=null&retry=N&response_code=0&billing_notes=&trans_date=24/06/2020 10:59:39&bin_country=";
									decryptCCAvenueResponse(request, response, responseMessage, configurationValues, pgID);
//									response.getWriter().println(respCC);
								}
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("pgResMsg", "Response not found");
								result.addProperty("Valid", "false");
								response.getWriter().print(new Gson().toJson(result));
							}
						}else if(pgID.equalsIgnoreCase(properties.getProperty("Ingenico"))|| pgID.equalsIgnoreCase(properties.getProperty("TPSL"))){
							if(null != request.getParameter("msg")){
								responseMessage = request.getParameter("msg");
							}else{
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.setContentType("text/plain");
								response.getWriter().println("Bank response is mandatory");
							}
							
							if(servletPath.equalsIgnoreCase("/PGPaymentTxnRes"))
								response.getWriter().println("Ingenico not avaibale on cloud");
							else if(servletPath.equalsIgnoreCase("/DecryptPGResponse")){
								decryptTechprocessResponse(request, response, responseMessage, configurationValues, pgID);
							}
						}else{
							JsonObject result = new JsonObject();
							result.addProperty("pgResMsg", "PGID Not found");
							result.addProperty("Valid", "false");
							response.getWriter().print(new Gson().toJson(result));
						}
					}
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setContentType("text/plain");
					response.getWriter().println("Configuration/Keys not found");
				}
			}else{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("text/plain");
				response.getWriter().println("PGID is mandatory");
			}
		}
		catch (Exception e)
		{
			response.getWriter().println(displayErrorForWeb(e));
		}
		finally
		{
			return;
		}
	*/}
	
	public String generateCSRFTokenForPCGW(HttpServletRequest request, HttpServletResponse response, boolean debug) throws IOException{
		JsonObject responseJsonObject = new JsonObject();
		String destURL="", pgID="", userName="", password="", authParam="", authMethod="", csrfTokenService="", csrfTokenServiceURL="", basicAuth="", 
			csrfToken="", sapclient=""; 
		
		boolean csrfFetchSuccess = false;
		
		String returnMessage="";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpGet csrfTokenGet = null;
		HttpEntity csrfFetchEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		Destination destConfiguration = null;
		
		try{
			destConfiguration = commonUtils.getDestinationURL(request, response);
			// String loginID = commonUtils.getUserPrincipal(request, "name", response);
			// String loginID = "JAVA";
			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (debug){
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			
			csrfTokenService = destURL+"/sap/opu/odata/ARTEC/PCGW";
			
			if (debug)
				response.getWriter().println("csrfTokenService: "+csrfTokenService);
			
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        
	        csrfTokenGet = new HttpGet(csrfTokenService);
	        // csrfTokenGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        csrfTokenGet.setHeader("x-csrf-token", "Fetch");
//	        csrfTokenGet.setHeader("Accept-Encoding", "gzip, deflate, br");
//	        csrfTokenGet.setHeader("x-csrf-token", "Fetch");
	        csrfTokenGet.setHeader("X-Requested-With", "XMLHttpRequest");
	        
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	csrfTokenGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	csrfTokenGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
//	        if(debug)
//	        	response.getWriter().println("csrfTokenGet: "+csrfTokenGet);
	        
	        HttpClientContext httpClientContext = HttpClientContext.create();
	        // HttpResponse httpResponse = closableHttpClient.execute(csrfTokenGet, httpClientContext);
	        HttpResponse httpResponse = client.execute(csrfTokenGet, httpClientContext);
	        globalCookieStore = httpClientContext.getCookieStore();
	        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);
	        
	        try {
	            List<Cookie> cookies = globalCookieStore.getCookies();
	            /*if (cookies.isEmpty()) {
	            	response.getWriter().println("No cookies");
	            } else {
	                for (int i = 0; i < cookies.size(); i++) {
	                	response.getWriter().println("Got Cookies toString: " + cookies.get(i).toString());
	                	response.getWriter().println("Got Cookies getName: " + cookies.get(i).getName());
	                	response.getWriter().println("Got Cookies getValue: " + cookies.get(i).getValue());
	                }
	            }*/
//	            EntityUtils.consume(httpResponse.getEntity());
	        } finally {
//	        	httpResponse.getClass()
	        }
	        
	        if(debug)
	        	response.getWriter().println("httpResponse: "+httpResponse);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        
	        Header[] headers = httpResponse.getAllHeaders();
            for (Header header: headers) {
            	//response.getWriter().println("Key [" + header.getName() + "], Value[" + header.getValue() + " ]");
            	
            	if(header.getName().trim().equalsIgnoreCase("x-csrf-token")){
	            	csrfToken = header.getValue().toString();
            		csrfFetchSuccess = true;
//            		break;
            	}
	        }
            
            if(! csrfFetchSuccess){
            	csrfToken = "Failure";
            }
	        
	        if(debug){
//	        	response.getWriter().println("csrfTokenGet.statusCode: "+statusCode);
	        	response.getWriter().println("csrfTokenGet.csrfToken: "+csrfToken);
	        }
			
	        csrfFetchEntity = httpResponse.getEntity();
	        
	        if(csrfFetchEntity != null)
			{
				String retSrc = EntityUtils.toString(csrfFetchEntity);
				
//				if(debug)
//		        	response.getWriter().println("csrfFetchEntity.retSrc: "+retSrc);
			}else{
				returnMessage = "PGPymntConfigStatsEntity returned null when trying to connect to the backend";
				return returnMessage;
			}
		
		}catch (Exception e) {
			responseJsonObject = new JsonObject();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			
			responseJsonObject.addProperty("Status", "000002");
			responseJsonObject.addProperty("Message", e.getMessage()+">"+e.getClass());
			responseJsonObject.addProperty("Full Trace", buffer.toString());
			responseJsonObject.addProperty("ErrorCode", "J002");
			
			returnMessage = "Exception: "+e.getMessage();
			return returnMessage;
		}
		
		return csrfToken;
	}

	
	public String updatePGTransaction(HttpServletRequest request, HttpServletResponse response, String pgHdrGUID, String txnRefNo, String bankRefNo,
			String pgTxnStatus, String paymentStatus, String configHdrGuid, String csrfToken, String bankMsg, boolean debug) throws IOException{
		JsonObject responseJsonObject = new JsonObject();
		String destURL="", pgID="", userName="", password="", authParam="", authMethod="", paymentConfigService="", paymentUpdateQuery="", basicAuth="", 
				sapclient="", sessionID="", loginMethod="", txnAmount="", cookie="";
		String returnMessage="";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpPut pgPymntUpdate = null;
		HttpEntity pgPymntUpdateEntity = null;
		// CloseableHttpClient closableHttpClient = null;
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
				response.getWriter().println("updatePGTransaction.pgHdrGUID:" + pgHdrGUID);
				response.getWriter().println("updatePGTransaction.pgTxnStatus:" + pgTxnStatus);
				response.getWriter().println("updatePGTransaction.configHdrGuid:" + configHdrGuid);
			}
			
			// String loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			sapclient = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();

			if (debug){
				
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}
			
			if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
				String url = commonUtils.getDestinationURL(request, response, "URL");
				if (debug)
					response.getWriter().println("url1:" + url);
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
			
			paymentUpdateQuery = destURL+"/sap/opu/odata/ARTEC/PCGW/PGPayments(guid'"+pgHdrGUID+"')";
//	        paymentConfigService = destURL+"/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigStats?sap-client="+ sapclient +"&$filter="+ paymentConfigFilter;
//	        paymentConfigFilter = "CHGuid eq guid'"+configHdrGuid+"' and PGTxnStatus eq '"+pgTxnStatus+"'";
			if (debug)
				response.getWriter().println("paymentUpdateQuery:" + paymentUpdateQuery);
			
			/*paymentUpdateQuery = URLEncoder.encode(paymentUpdateQuery, "UTF-8");
			
			paymentUpdateQuery = paymentUpdateQuery.replaceAll("%26", "&");
			paymentUpdateQuery = paymentUpdateQuery.replaceAll("%3D", "=");*/
			
			
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
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
			    response.getWriter().println("updatePGTransaction.proxyType: "+proxyType);
			    response.getWriter().println("updatePGTransaction.proxyHost: "+proxyHost);
			    response.getWriter().println("updatePGTransaction.proxyPort: "+proxyPort);
			    response.getWriter().println("updatePGTransaction.csrfToken: "+csrfToken);
//			    response.getWriter().println("updatePGTransaction.Cookie: "+request.getHeader("Cookie"));
		    } */
		    cookie = request.getHeader("Cookie");
		    String[] splitResult = cookie.split(";");
		    
		    String key="", value="", prefix="JTENANTSESSIONID_", compareKey="", tenantSessionIDValue="";
			/* Commented for CF
		    compareKey = prefix+tenantContext.getTenant().getAccount().getId();
			for(String s : splitResult){
				key = s.substring(0, s.indexOf("="));
	        	value = s.substring(s.indexOf("=")+1,s.length());
	        	response.getWriter().println("Cookie Key: "+key+"|Value: "+value);
	        	
	        	if(key.equalsIgnoreCase(compareKey)){
	        		tenantSessionIDValue = value;
	        		response.getWriter().println("Required value: "+tenantSessionIDValue);
	        	}
			}
			
			if(debug)
				response.getWriter().println("tenantSessionIDValue: "+prefix+tenantSessionIDValue);
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
//	        response.getWriter().println("routePlanner: "+routePlanner);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();*/
	        
	        JSONObject updatePayLoad = new JSONObject();
	        updatePayLoad.accumulate("PGPaymentGUID", pgHdrGUID);
	        updatePayLoad.accumulate("PGTransactionID", bankRefNo);
	        updatePayLoad.accumulate("PGTxnStatusID", pgTxnStatus);
	        updatePayLoad.accumulate("PGTxnMessage", bankMsg);
	        updatePayLoad.accumulate("TestRun", false);
	        
	        //prefix = "SAP_SESSIONID_GWQ_300="+tenantSessionIDValue+"; Path=/;";
	        
	        if(debug){
				response.getWriter().println("updatePayLoad: "+updatePayLoad);
//				response.getWriter().println("prefix: "+prefix);
	        }
	        
	        pgPymntUpdateEntity = new StringEntity(updatePayLoad.toString());
	        
	        pgPymntUpdate = new HttpPut(paymentUpdateQuery);
	        
	        // pgPymntUpdate.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        pgPymntUpdate.setHeader("Content-Type", "application/json; charset=utf-8");
	        pgPymntUpdate.setHeader("Accept", "application/json");
	        pgPymntUpdate.setHeader("x-csrf-token", csrfToken);
//	        pgPymntUpdate.setHeader("X-Requested-With", "XMLHttpRequest");
//	        pgPymntUpdate.setHeader("Cookie", prefix);
//	        pgPymntUpdate.setHeader("Cookie", request.getHeader("Cookie"));
//	        pgPymntUpdate.setHeader("Set-Cookie", prefix+tenantSessionIDValue);
	        
//	        pgPymntUpdate.ad
	        
	        
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	pgPymntUpdate.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	pgPymntUpdate.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        pgPymntUpdate.setEntity(pgPymntUpdateEntity);
	        
	        HttpClientContext httpClientContext = HttpClientContext.create();
//	        cookieStore = httpClientContext.getCookieStore();
	        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(pgPymntUpdate, httpClientContext);
			HttpResponse httpResponse = client.execute(pgPymntUpdate, httpClientContext);
	        
	        if(debug)
	        	response.getWriter().println("httpResponse: "+httpResponse);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("validatePaymentReq.statusCode: "+statusCode);
			
	        pgPymntUpdateEntity = httpResponse.getEntity();
			
	        if(statusCode != HttpServletResponse.SC_NO_CONTENT){
	        	String retSrc = EntityUtils.toString(pgPymntUpdateEntity);
				if(debug)
		        	response.getWriter().println("updatePGTransaction.retSrc: "+retSrc);
				return retSrc;
	        }else{
	        	return ""+statusCode;
	        }
		}catch (Exception e) {
			responseJsonObject = new JsonObject();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			
			response.getWriter().println("updatePGTransaction.getMessage: "+e.getMessage());
			response.getWriter().println("updatePGTransaction.getClass: "+e.getClass());
			response.getWriter().println("updatePGTransaction.getCause: "+e.getCause());
			
			responseJsonObject.addProperty("Status", "000002");
			responseJsonObject.addProperty("Message", e.getMessage()+">"+e.getClass());
			responseJsonObject.addProperty("Full Trace", buffer.toString());
			responseJsonObject.addProperty("ErrorCode", "J002");
			returnMessage = "Exception: "+e.getMessage()+"|e.getClass()-"+e.getCause();
			return returnMessage;
		}
	}
}
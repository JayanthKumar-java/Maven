package com.arteriatech.bankapis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
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

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class OTPGenerate
 */
@WebServlet("/OTPGenerate")
public class OTPGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String PCGW_HANA_DEST_NAME =  "PCGWHANA";
	CookieStore globalCookieStore = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OTPGenerate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("Unsupported Request Type");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject responseObj = new JsonObject();
		JsonObject cloudCPConfigObj = new JsonObject();
		JsonObject cloudPGPaymentConfigObj = new JsonObject();
		JsonObject paymentTxnObj = new JsonObject();
		JsonObject bflPayload = new JsonObject();
		boolean debug = false, mandatoryCheckPass = true, isSuccess = false;
		
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		
		String requestBody="", mandatoryCheckMsg="Mandatory inputs missing: ", validValueCheckMsg=" Received Invalid Values For: ";
		String idType="", idValue="", inputCPGuid="", cpType="", pgGuid="", pgProvider="", aggregatorID="", pgID="", configurationValues="";
		
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			requestBody = getGetBody(request, response);
			if(debug)
				response.getWriter().println("requestBody: "+requestBody);
			
			JsonParser parser = new JsonParser();
			JsonObject inputPayload = (JsonObject)parser.parse(requestBody);
			if(debug)
				response.getWriter().println("inputPayload: "+inputPayload);
			
			if(inputPayload.has("IDType") && inputPayload.get("IDType").getAsString().trim().length() > 0){
				idType = inputPayload.get("IDType").getAsString().trim();
				
				if(!idType.equalsIgnoreCase("MOBILE") && !idType.equalsIgnoreCase("CARD")){
					validValueCheckMsg = validValueCheckMsg+"IDType";
				}
			}else{
				mandatoryCheckMsg = mandatoryCheckMsg+"IDType";
			}
			
			if(inputPayload.has("IDValue") && inputPayload.get("IDValue").getAsString().trim().length() > 0){
				idValue = inputPayload.get("IDValue").getAsString();
				
				if(idType.equalsIgnoreCase("MOBILE") || idType.equalsIgnoreCase("CARD")){
					try{
						boolean lengthOk = true;
						if(idType.equalsIgnoreCase("MOBILE")){
							if(idValue.length() != 10){
								lengthOk = false;
							}
						}else if(idType.equalsIgnoreCase("CARD")){
							if(idValue.length() != 16){
								lengthOk = false;
							}
						}
						
						if(lengthOk){
							Long.parseLong(idValue);	
						}else{
							if(!validValueCheckMsg.equalsIgnoreCase("Received Invalid Values For: ")){
								validValueCheckMsg = validValueCheckMsg+", IDValue: Expected 10 digits for Mobile No, 16 digits for Card No";
							}else{
								validValueCheckMsg = validValueCheckMsg+"IDValue: Expected 10 digits for Mobile No, 16 digits for Card No";
							}
						}
					}catch (NumberFormatException e) {
						if(!validValueCheckMsg.equalsIgnoreCase("Received Invalid Values For: ")){
							validValueCheckMsg = validValueCheckMsg+", IDValue for IDType "+idType;
						}else{
							validValueCheckMsg = validValueCheckMsg+"IDValue for IDType "+idType;
						}
					}
				}
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", IDValue";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"IDValue";
				}
			}
			
			if(inputPayload.has("CPGuid") && inputPayload.get("CPGuid").getAsString().trim().length() > 0){
				inputCPGuid = inputPayload.get("CPGuid").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPGuid";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPGuid";
				}
			}
			
			if(inputPayload.has("CPType") && inputPayload.get("CPType").getAsString().trim().length() > 0){
				cpType = inputPayload.get("CPType").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPType";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPType";
				}
			}
			
			if(inputPayload.has("PGPaymentGUID") && inputPayload.get("PGPaymentGUID").getAsString().trim().length() > 0){
				pgGuid = inputPayload.get("PGPaymentGUID").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", PGPaymentGUID";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"PGPaymentGUID";
				}
			}
			
			if(inputPayload.has("PGProvider") && inputPayload.get("PGProvider").getAsString().trim().length() > 0){
				pgProvider = inputPayload.get("PGProvider").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", PGProvider";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"PGProvider";
				}
			}
			
			if(inputPayload.has("PGID") && inputPayload.get("PGID").getAsString().trim().length() > 0){
				pgID = inputPayload.get("PGID").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", PGID";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"PGID";
				}
			}
			
			if(debug){
				response.getWriter().println("mandatoryCheckMsg: "+mandatoryCheckMsg);
				response.getWriter().println("validValueCheckMsg: "+validValueCheckMsg);
			}
			
			if(!mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ") && !validValueCheckMsg.equalsIgnoreCase("Received Invalid Values For: ")){
				mandatoryCheckPass = false;
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("Message", mandatoryCheckMsg+"."+validValueCheckMsg);
				responseObj.addProperty("ErrorCode", "J001");
				response.getWriter().println(responseObj);
			}
			
			if(debug){
				response.getWriter().println("mandatoryCheckMsg: "+mandatoryCheckMsg);
				response.getWriter().println("mandatoryCheckPass: "+mandatoryCheckPass);
				response.getWriter().println("idType: "+idType);
				response.getWriter().println("idValue: "+idValue);
				response.getWriter().println("inputCPGuid: "+inputCPGuid);
				response.getWriter().println("cpType: "+cpType);
				response.getWriter().println("pgGuid: "+pgGuid);
				response.getWriter().println("pgProvider: "+pgProvider);
			}
			
			if(mandatoryCheckPass){
				String customerFilter ="", loginMethod="", destURL="", sessionID="", customerService="", service="";
				boolean isRequestFromCloud = false;
				
				Destination destConfiguration = null;
				destConfiguration = getDestinationURL(request, response);
				destURL = destConfiguration.get("URL").get().toString();
				
				if(debug){
					response.getWriter().println("doPost.destURL: "+ destURL);
				}
				
				if (destURL.contains("service.xsodata")){
					isRequestFromCloud = true;
				}else{
					isRequestFromCloud = false;
				}
				
				String loginID = commonUtils.getUserPrincipal(request, "name", response);
				
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", PCGW_HANA_DEST_NAME);
				
				if (debug){
					response.getWriter().println("loginID: "+loginID);
					response.getWriter().println("aggregatorID: "+aggregatorID);
				}
				
//				JsonObject userCustomerObj = new JsonObject();
				if(! isRequestFromCloud){/*
					service = destConfiguration.getProperty("service");
					if(debug){
						response.getWriter().println("doPost.service: "+ service);
					}
					
					String sapclient = destConfiguration.getProperty("sap-client");
					if (debug)
						response.getWriter().println("sapclient:" + sapclient);
					
					String authMethod = destConfiguration.getProperty("Authentication");
					if (debug)
						response.getWriter().println("authMethod:" + authMethod);
					
					if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("url1:" + url);
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					} else{
						loginMethod = destConfiguration.getProperty("LoginMethod");
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
					
					customerFilter = "LoginID eq '"+sessionID+"'";
					customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
					
					customerFilter = customerFilter.replaceAll("%26", "&");
					customerFilter = customerFilter.replaceAll("%3D", "=");
					if (debug)
						response.getWriter().println("customerFilter: "+customerFilter);
					
					if(null != service && service.equalsIgnoreCase("SFGW")){
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ customerFilter;
						}
					}else{
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ customerFilter;
						}
					}
					
					if (debug)
						response.getWriter().println("CustomerService 1: "+customerService);
					
					userCustomerObj = commonUtils.getUserCustomerDetails(request, response, customerService, destConfiguration, debug);
					if (debug)
						response.getWriter().println("op userCustomerObj: "+userCustomerObj);
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							String formattedStr = "", opCPGuid="";
							try{
								opCPGuid = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString();
								
								int number = Integer.parseInt(opCPGuid);
								formattedStr = ("0000000000" + opCPGuid).substring(opCPGuid.length());
								opCPGuid = formattedStr;
								if(debug)
									response.getWriter().println("opCPGuid: "+opCPGuid);
							}catch (NumberFormatException e) {
//								formattedStr = customerNo;
							}
//							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
							try{
								formattedStr="";
								int number = Integer.parseInt(inputCPGuid);
								formattedStr = ("0000000000" + inputCPGuid).substring(inputCPGuid.length());
								inputCPGuid = formattedStr;
							}catch (NumberFormatException e) {
//								formattedStr = customerNo;
							}
							if(opCPGuid.equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");d
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				*/}else{/*
					String formattedStr = "";
					try{
						int number = Integer.parseInt(inputCPGuid);
						formattedStr = ("0000000000" + inputCPGuid).substring(inputCPGuid.length());
						inputCPGuid = formattedStr;
					}catch (NumberFormatException e) {
//						formattedStr = customerNo;
					}
					
					String aggregatorIDFromDest="";
					aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
					
					customerService = destURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27";
					if (debug)
						response.getWriter().println("oc userCustomerObj.executeURL: "+customerService);
					
					userCustomerObj = commonUtils.getCustomerFromCloud(request, response, customerService, debug);
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				*/}
				
				isSuccess = true;
				boolean arePrechecksSuccess = false;
				
				if(isSuccess){
					if(! isRequestFromCloud){
						String formattedStr="";
						try{
							formattedStr="";
							int number = Integer.parseInt(inputCPGuid);
							formattedStr = ("0000000000" + inputCPGuid).substring(inputCPGuid.length());
							inputCPGuid = formattedStr;
						}catch (NumberFormatException e) {
//							formattedStr = customerNo;
						}
						
						if(debug)
							response.getWriter().println("formatted inputCPGuid: "+inputCPGuid);
						
						configurationValues = getPGPaymentConfigs(request, response, "", pgID);
						if(debug)
							response.getWriter().println("configurationValues: "+configurationValues);
						if(configurationValues.trim().length() > 0){
							cloudCPConfigObj = commonUtils.getCPReferenceFromCloud(request, response, pgProvider, pgID, inputCPGuid, debug);
							if(debug)
								response.getWriter().println("cloudCPConfigObj: "+cloudCPConfigObj);
							
							if(cloudCPConfigObj.get("Status").getAsString().equalsIgnoreCase("000001")){
								paymentTxnObj = commonUtils.getPaymentTransaction(request, response, pgGuid, debug);
								if(debug)
									response.getWriter().println("paymentTxnObj: "+paymentTxnObj);
								
								if(paymentTxnObj.get("Status").getAsString().equalsIgnoreCase("000001")){
									arePrechecksSuccess = true;
								}else{
									response.getWriter().println(paymentTxnObj);
								}
							}else{
								response.getWriter().println(cloudCPConfigObj);
							}
						}else{
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Configurations not maintained for: "+pgID);
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
						
						if(debug){
							response.getWriter().println("configurationValues: "+configurationValues);
							response.getWriter().println("cloudCPConfigObj: "+cloudCPConfigObj);
							response.getWriter().println("paymentTxnObj: "+paymentTxnObj);
						}
					}else{
						//Not needed to implement this block now - Required when cloud corporate comes in
						cloudPGPaymentConfigObj = commonUtils.getCloudPGPaymentConfigs(request, response, pgID, debug);
						cloudCPConfigObj = commonUtils.getCPReferenceFromCloud(request, response, pgProvider, pgID, inputCPGuid, debug);
						
						if(debug){
							response.getWriter().println("configurationValues: "+configurationValues);
							response.getWriter().println("cloudCPConfigObj: "+cloudCPConfigObj);
							response.getWriter().println("cloudPGPaymentConfigObj: "+cloudPGPaymentConfigObj);
						}
					}
					
					String transactionType="", saleType="", cardNo="", mobileNo="", lastReqRefNo="", newReqRefNo="", trackID="", bflDealerCode="", dealerValKey="", ipAddress="";
					if(! isRequestFromCloud){
						if(arePrechecksSuccess){
							ipAddress = "155.56.216.33";//commonUtils.getIPAddress(request);
							
//							transactionType = properties.getProperty("OTPTxnType");
							transactionType = "OTPREQ";
//							saleType = properties.getProperty("AuthSaleType");
							saleType = properties.getProperty("AuthSaleType");
							
							if(idType.equalsIgnoreCase("MOBILE")){
								mobileNo = idValue;
								cardNo = "";
							}else if(idType.equalsIgnoreCase("CARD")){
								mobileNo = "";
								cardNo = idValue;
							}
							
							if(null != paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").getAsString() 
								&& paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").getAsString().trim().length() > 0){
								lastReqRefNo = paymentTxnObj.get("Payment").getAsJsonObject().get("PGTransactionID").getAsString();
								if(debug)
									response.getWriter().println("lastReqRefNo: "+lastReqRefNo);
								
								String sub=lastReqRefNo.substring(lastReqRefNo.length()-3, lastReqRefNo.length());
								if(sub.startsWith("X")){
									String formattedStr="";
									try{
										int i = Integer.parseInt(sub.substring(sub.length()-2, sub.length()));
										
										i = i+1;
										formattedStr = ("00" + i).substring((""+i).length());
										formattedStr="X"+formattedStr;
										
										newReqRefNo = pgGuid+formattedStr;
									}catch (NumberFormatException e) {
										newReqRefNo = pgGuid+"X01";
									}
								}
							}else{
								lastReqRefNo = "";
								newReqRefNo = pgGuid+"X01";
							}
							
//							newReqRefNo = "sfvsfs8d788s98f8s7f89asdsdf";
							newReqRefNo = newReqRefNo.replaceAll("[^a-zA-Z0-9]", "");
							
							trackID = paymentTxnObj.get("Payment").getAsJsonObject().get("TrackID").getAsString();
							bflDealerCode = cloudCPConfigObj.get("CPReference").getAsJsonObject().get("Reference1").getAsString();
							dealerValKey = cloudCPConfigObj.get("CPReference").getAsJsonObject().get("Reference2").getAsString();
							
							if(debug){
								response.getWriter().println("ipAddress: "+ipAddress);
								response.getWriter().println("transactionType: "+transactionType);
								response.getWriter().println("saleType: "+saleType);
								response.getWriter().println("idType: "+idType);
								response.getWriter().println("mobileNo: "+mobileNo);
								response.getWriter().println("cardNo: "+cardNo);
								response.getWriter().println("trackID: "+trackID);
								response.getWriter().println("bflDealerCode: "+bflDealerCode);
								response.getWriter().println("dealerValKey: "+dealerValKey);
								response.getWriter().println("newReqRefNo: "+newReqRefNo);
							}
							
							bflPayload.addProperty("TXNTYPE", transactionType);
							bflPayload.addProperty("SALETYPE", saleType);
							bflPayload.addProperty("CN", cardNo);
							bflPayload.addProperty("MOBNO", mobileNo);
							bflPayload.addProperty("RRN", newReqRefNo);
							bflPayload.addProperty("ORDERNO", trackID);
							bflPayload.addProperty("DLRCODE", bflDealerCode);
							bflPayload.addProperty("DLRVKEY", dealerValKey);
							bflPayload.addProperty("IPADDR", ipAddress);
//							bflPayload.addProperty("REQFTXT1", "");
							bflPayload.addProperty("REQFTXT2", pgGuid);
							/*bflPayload.addProperty("REQFTXT3", "");
							bflPayload.addProperty("REQFTXT4", "");
							bflPayload.addProperty("REQFTXT5", "");
							bflPayload.addProperty("REQFTXT6", "");*/
							
							if(debug)
								response.getWriter().println("bflPayload for OTP Generate: "+bflPayload);
							
							//Pick Config Values
							String wholeParamString="", paramName="", paramValue="";
							String pgName="", ivText="", txnKey="", schemeCode="", txnURL="";
							wholeParamString = configurationValues;
							
							String[] splitResult = wholeParamString.split("\\|");
							for(String s : splitResult)
							{
					        	paramName = s.substring(0, s.indexOf("="));
					        	paramValue = s.substring(s.indexOf("=")+1,s.length());
					        	
					        	if(paramName.equalsIgnoreCase("PGName")){
					        		pgName = paramValue;
					        	}
					        	
					        	if(paramName.equalsIgnoreCase("IVText")){
					        		ivText = paramValue;
					        	}
					        	
					        	if(paramName.equalsIgnoreCase("PGID")){
					        		pgID = paramValue;
					        	}
					        	
					        	if(paramName.equalsIgnoreCase("TransactionKey")){
					        		txnKey = paramValue;
					        	}
					        	
					        	if(paramName.equalsIgnoreCase("SchemeCode")){
					        		schemeCode = paramValue;
					        	}
					        	
					        	if(paramName.equalsIgnoreCase("TransactionURL")){
					        		txnURL = paramValue;
					        	}
							}
							
							if(debug){
								response.getWriter().println("pgName: "+pgName);
								response.getWriter().println("ivText: "+ivText);
								response.getWriter().println("pgID: "+pgID);
								response.getWriter().println("txnKey: "+txnKey);
								response.getWriter().println("schemeCode: "+schemeCode);
								response.getWriter().println("txnURL: "+txnURL);
							}
							
							String encodedValue="", sealValue="", encryptedResponse="", decodedValue="";
							
							if(debug)
								response.getWriter().println("bflPayload.ToString: "+bflPayload.toString());
							
							
							
							//Temp code - Start
							/*String bflOtp="{\"TXNTYPE\":\"OTPREQ\",\"CN\":\"\",\"RRN\":\"Arvindpoghvbdfjo\",\"ORDERNO\":\"ArvindTestOrder100\",\"DLRCODE\":\"138851\",\"DLRVKEY\":\"8491843317222182\",\"MOBNO\":\"8959157646\",\"SALETYPE\":\"AUTH\"}";
							JsonParser jsonParser = new JsonParser();
							JsonObject bflObj = (JsonObject)jsonParser.parse(bflOtp);
							bflObj.remove("RRN");
							bflObj.addProperty("RRN", commonUtils.generateGUID(16));
							response.getWriter().println("Hardcoded request bflObj: "+bflObj);*/
							//Temp code - End
							
							encodedValue = CommonUtils.AES_Encode(bflPayload.toString(), txnKey, ivText);//Actual payload
//							encodedValue = OTPRequest.AES_Encode(bflObj.toString(), txnKey); //hardcoded payload
							
							if(debug)
								response.getWriter().println("encodedValue: "+encodedValue);
							sealValue = CommonUtils.getSealValue(encodedValue+txnKey);
							if(debug)
								response.getWriter().println("sealValue: "+sealValue);
							
							String csrfToken="", updateTxnRes="";
							csrfToken = generateCSRFTokenForPCGW(request, response, debug);
							if(csrfToken.equalsIgnoreCase("Failure") || csrfToken.contains("Exception")){
						    	responseObj.addProperty("Status", "000002");
								responseObj.addProperty("Message", "Error while generating CSRF token "+csrfToken);
								responseObj.addProperty("ErrorCode", "J001");
								responseObj.addProperty("PGTransactionID", newReqRefNo);
								response.getWriter().println(responseObj);
							}else{
								
								updateTxnRes = updatePGTransaction(request, response, pgGuid, newReqRefNo, properties.getProperty("Initiated"), csrfToken, debug);
								if(updateTxnRes.equalsIgnoreCase("204")){
									try{
										Client client=Client.create();
										WebResource webResource = client.resource(txnURL);
										encryptedResponse = webResource.header("SealValue", sealValue).type("application/json").post(String.class, "\""+encodedValue+"\"");
										if(debug)
											response.getWriter().println("encryptedResponse: "+encryptedResponse);
									}catch (Exception e) {
										StackTraceElement element[] = e.getStackTrace();
										StringBuffer buffer = new StringBuffer();
										for(int i=0;i<element.length;i++)
										{
											buffer.append(element[i]);
										}
										if(debug){
											response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
										}
										
										responseObj.addProperty("Status", "000002");
										responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
										responseObj.addProperty("ErrorCode", "JE001");
										responseObj.addProperty("PGTransactionID", newReqRefNo);
										response.getWriter().println(responseObj);
									}
									
									
									if(encryptedResponse != null){
										encryptedResponse = encryptedResponse.replace("\"", "");
										if(debug){
											response.getWriter().println("encryptedResponse after removing double quotes: "+encryptedResponse);
										    response.getWriter().println("input for decryption: "+encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")));
										}
							        	
							        	decodedValue = CommonUtils.AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),txnKey, ivText);//Actual payload
//										decodedValue = OTPRequest.AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),txnKey);
										
							        	if(debug)
							        		response.getWriter().println("decodedValue after calling POST: "+decodedValue);
							        	
							        	JsonParser bflResponseParser = new JsonParser();
										JsonObject bflResponseJson = (JsonObject)bflResponseParser.parse(decodedValue);
										
										if(debug){
											response.getWriter().println("bflResponseJson: "+bflResponseJson);
											response.getWriter().println("bflResponseJson.RSPCODE: "+bflResponseJson.get("RSPCODE").getAsString());
											response.getWriter().println("bflResponseJson.ERRDESC: "+bflResponseJson.get("ERRDESC").getAsString());
										}
										
										
										JsonObject responseObject = new JsonObject();
										if(bflResponseJson.get("RSPCODE").getAsString().equalsIgnoreCase("00")
											|| bflResponseJson.get("RSPCODE").getAsString().equalsIgnoreCase("0")){
											responseObject.addProperty("Status", "000001");
											responseObject.addProperty("Message", "Success");
											responseObject.addProperty("ErrorCode", "");
											responseObject.addProperty("TransactionStatus", bflResponseJson.get("RSPCODE").getAsString());
											responseObject.addProperty("Last4Digits", bflResponseJson.get("CRMN").getAsString());
											responseObject.addProperty("PGPaymentGUID", pgGuid);
											responseObject.addProperty("PGTransactionID", newReqRefNo);
										}else{
											responseObject.addProperty("Status", "000002");
											responseObject.addProperty("Message", bflResponseJson.get("ERRDESC").getAsString());
											responseObject.addProperty("ErrorCode", bflResponseJson.get("RSPCODE").getAsString());
											responseObject.addProperty("TransactionStatus", bflResponseJson.get("RSPCODE").getAsString());
											responseObject.addProperty("Last4Digits", "");
											responseObject.addProperty("PGPaymentGUID", pgGuid);
											responseObject.addProperty("PGTransactionID", newReqRefNo);
										}
										response.getWriter().println(responseObject);
								    }else{
								    	if(debug)
								    		response.getWriter().println("Getting Empty response from ATOS service");
								    	
								    	responseObj.addProperty("Status", "000002");
										responseObj.addProperty("Message", "Response not received from: "+txnURL);
										responseObj.addProperty("ErrorCode", "J001");
										responseObj.addProperty("PGTransactionID", newReqRefNo);
										response.getWriter().println(responseObj);
							        }
								}else{
									responseObj.addProperty("Status", "000002");
									responseObj.addProperty("Message", updateTxnRes);
									responseObj.addProperty("ErrorCode", "J001");
									responseObj.addProperty("PGTransactionID", newReqRefNo);
									response.getWriter().println(responseObj);
								}
							}
						}
					}else{
						responseObj.addProperty("Status", "000002");
						responseObj.addProperty("Message", "Process under development. Please check after sometime");
						responseObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(responseObj);
					}
				}
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
			}
			
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			responseObj.addProperty("ErrorCode", "JE001");
			response.getWriter().println(responseObj);
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("Unsupported Request Type");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("Unsupported Request Type");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public String getGetBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {}
		body = jb.toString();
		return body;
	}
	
	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//			LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = PCGW_UTIL_DEST_NAME;
		Destination destConfiguration = null;
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			destConfiguration = destinationAccessor.get();
//				LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
//				LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}
	
	private String getPGPaymentConfigs(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		try
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
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
				basicAuth = "Bearer " + basicAuth;
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
			if(sapclient != null){
				constantValuesService =  destURL+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
			}else{
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
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				{
					response.getWriter().println("constantEntity is not null");
					response.getWriter().println("PGID: "+PGID);
				}
				
				if(PGID.equalsIgnoreCase("B2BIZ")){
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
				}else if(PGID.equalsIgnoreCase("AXISPG")){
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
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
		            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
		            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
		            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
		            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
		            NodeList secretCode = document.getElementsByTagName("d:ClientCode");
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
		            								+"|"+"Type="+pgParameter2.item(i).getTextContent()
		            								+"|"+"RE1="+pgParameter3.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent()
		            								+"|"+"secretCode="+secretCode.item(i).getTextContent()
		            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}else if(PGID.equalsIgnoreCase("YESPAYU")){
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
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//				            	response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
		            								+"|"+"PassCode="+pgParameter2.item(i).getTextContent()
		            								+"|"+"MCC="+pgParameter3.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
		            								+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent()
		            								+"|"+"BankId="+bankKey.item(i).getTextContent()
		            								+"|"+"TerminalId="+clientCode.item(i).getTextContent()
		            								+"|"+"Currency="+pgParameter4.item(i).getTextContent()
		            								+"|"+"TxnType="+pgParameter5.item(i).getTextContent()
		            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}else if(PGID.equalsIgnoreCase("EAZYPAY")){
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
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
		            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent()+
		            				"|"+"SecretKey="+secretKeyList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}else if(PGID.equalsIgnoreCase("RZRPYROUTE")){
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
									+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("RAZORPAY") || PGID.equalsIgnoreCase("RZRPAY")){
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
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("CCAICICI")){
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
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					
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
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("CCAVENUE") 
				|| PGID.equalsIgnoreCase("MOBILECCA")
				|| PGID.equalsIgnoreCase("CCA")
				|| PGID.equalsIgnoreCase("CCAICICI")){
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
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
									          
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
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("BFLNCEMI") || PGID.equalsIgnoreCase("BFL")){
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
					NodeList pgNameList = document.getElementsByTagName("d:PGName");
					NodeList pgPublicKeyList = document.getElementsByTagName("d:PGPublicKey");
					NodeList pgOwnPublicKeyList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList pgOwnPrivateKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList pgProviderList = document.getElementsByTagName("d:PGProvider");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pgURLList = document.getElementsByTagName("d:PGURL");
					NodeList txnStatusList = document.getElementsByTagName("d:TxnStsURL");
					
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"PGID="+pdIDList.item(i).getTextContent()
										          	+"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"PGName="+pgNameList.item(i).getTextContent()
										            +"|"+"IVText="+pgPublicKeyList.item(i).getTextContent()
										            +"|"+"RequeryKey="+pgOwnPublicKeyList.item(i).getTextContent()
										            +"|"+"TransactionKey="+pgOwnPrivateKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+pgProviderList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"TransactionURL="+pgURLList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnStatusList.item(i).getTextContent();
						
							break;
						}
					}
				}
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("1 getConstantValues: "+configurableValues);
		}
		catch (Exception e)
		{
			if(debug)
				response.getWriter().println("Exception: "+e.getMessage());
		}
		finally
		{
			// closableHttpClient.close();
			return configurableValues;
		}
	}
	
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
			if (debug){
				response.getWriter().println("sapclient:" + sapclient);
				response.getWriter().println("authMethod:" + authMethod);
				response.getWriter().println("destURL:" + destURL);
			}
			
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
				// String encodedStr = new String(encodedByte);
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
	
	public String updatePGTransaction(HttpServletRequest request, HttpServletResponse response, String pgHdrGUID, String newReqRefNo,
			String pgTxnStatus, String csrfToken, boolean debug) throws IOException{
		JsonObject responseJsonObject = new JsonObject();
		String destURL="", pgID="", userName="", password="", authParam="", authMethod="", paymentConfigService="", paymentUpdateQuery="", basicAuth="", 
				sapclient="", sessionID="", loginMethod="", txnAmount="", cookie=""; 
		
		String returnMessage="";
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
				response.getWriter().println("updatePGTransaction.newReqRefNo:" + newReqRefNo);
			}
			
			String loginID = commonUtils.getUserPrincipal(request, "name", response);
			
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
			if (debug)
				response.getWriter().println("paymentUpdateQuery:" + paymentUpdateQuery);
			
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
		    } */
		    
		    cookie = request.getHeader("Cookie");
		    String[] splitResult = cookie.split(";");
		    
		    String key="", value="", prefix="JTENANTSESSIONID_", compareKey="", tenantSessionIDValue="";
		    /* compareKey = prefix+tenantContext.getTenant().getAccount().getId();
			for(String s : splitResult){
				key = s.substring(0, s.indexOf("="));
	        	value = s.substring(s.indexOf("=")+1,s.length());
	        	if(debug)
	        		response.getWriter().println("Cookie Key: "+key+"|Value: "+value);
	        	
	        	if(key.equalsIgnoreCase(compareKey)){
	        		tenantSessionIDValue = value;
	        		if(debug)
	        			response.getWriter().println("Required value: "+tenantSessionIDValue);
	        	}
			}
			
			if(debug)
				response.getWriter().println("tenantSessionIDValue: "+prefix+tenantSessionIDValue);
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        
	        JSONObject updatePayLoad = new JSONObject();
	        updatePayLoad.accumulate("PGPaymentGUID", pgHdrGUID);
	        updatePayLoad.accumulate("PGTransactionID", newReqRefNo);
	        updatePayLoad.accumulate("PGTxnStatusID", pgTxnStatus);
	        updatePayLoad.accumulate("LoginID", sessionID);
	        updatePayLoad.accumulate("TestRun", false);
	        
	        if(debug){
				response.getWriter().println("updatePayLoad: "+updatePayLoad);
	        }
	        
	        pgPymntUpdateEntity = new StringEntity(updatePayLoad.toString());
	        
	        pgPymntUpdate = new HttpPut(paymentUpdateQuery);
	        
	        // pgPymntUpdate.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        pgPymntUpdate.setHeader("Content-Type", "application/json; charset=utf-8");
	        pgPymntUpdate.setHeader("Accept", "application/json");
	        pgPymntUpdate.setHeader("x-csrf-token", csrfToken);
	        pgPymntUpdate.setHeader("x-arteria-loginid", sessionID);
	        
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	pgPymntUpdate.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	pgPymntUpdate.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        pgPymntUpdate.setEntity(pgPymntUpdateEntity);
	        
	        HttpClientContext httpClientContext = HttpClientContext.create();
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
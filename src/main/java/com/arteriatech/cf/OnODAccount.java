package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
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
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arteriatech.bc.Account.AccountClient;
import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import java.util.Enumeration;
/**
 * Servlet implementation class OnODAccount
 */
@WebServlet("/OnODAccount")
public class OnODAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	//private static final String CPI_CONNECT_DEST_NAME =  "CPIConnect";   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OnODAccount() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String accountNumber ="", event = "",payloadRequest="", destURL ="", ODataURL="", password="", userName="", userPass="", sessionID="",customerService="";
		boolean debug = false, isRequestFromCloud = false, mandParamVal=true;
		String dealerID="", dealerName="", corpID="",userID="", userRegId="", aggregatorID="";
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		boolean isInsertionSuccess=false;
		JsonObject finalPayload = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		Destination destConfiguration = null;
		Destination cpiDestConfig = null;
		//AccountClient accountClient = new AccountClient();
		AccountClient accountClient=new AccountClient();
		String vendorName="", cpTypeId="", vendorId="", lenderCode="";
		try {

			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
	            String headerName = headerNames.nextElement();	 
//	            String headerValue=request.getHeader(headerName);
//	            	response.getWriter().println(headerName+"  "+headerValue);
	            if ("x-arteria-cptypeid".equalsIgnoreCase(headerName)) {
	                 cpTypeId = request.getHeader(headerName);
	            }                             
	        }
			
			// JsonObject partnerObj = new JsonObject();
			JsonObject partnerObj = new JsonObject();
			JsonObject userRegistrationObj = new JsonObject();
			payloadRequest = getGetBody(request, response);
			
			JSONObject inputPayload = new JSONObject(payloadRequest);
//			response.getWriter().println("inputPayload: "+inputPayload);
			try {
				if (inputPayload.has("debug") && ! inputPayload.isNull("debug") && 
						inputPayload.getBoolean("debug") == true ) {
					debug = true;
					response.getWriter().println("doGet.STP.: "+inputPayload); 
				}
			} catch (Exception e) {
				debug = false;
			}
			//debug = true; // remove ita afetr
//			inputPayload.getJSONObject("Message").isNull(key)
			if ( ! inputPayload.getJSONObject("Message").isNull("AccountNo")){
				if(inputPayload.getJSONObject("Message").getString("AccountNo").equalsIgnoreCase("")){
					mandParamVal = false;
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "002");
					result.addProperty("Message", "'AccountNo' "+properties.getProperty("002"));
					response.getWriter().println(new Gson().toJson(result));
				}else{
					accountNumber = inputPayload.getJSONObject("Message").getString("AccountNo");
					finalPayload.addProperty("ODAccountNumber", accountNumber);
				}
			}else{
				//Error Response - Account No not found
				mandParamVal = false;
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "002");
				result.addProperty("Message", "'AccountNo' "+properties.getProperty("002"));
				response.getWriter().println(new Gson().toJson(result));
			}
			if(debug)
				response.getWriter().println("doGet.accountNumber.: "+ accountNumber);
			
			if (!inputPayload.isNull("Event")){
				if(inputPayload.getString("Event").equalsIgnoreCase("")){
					mandParamVal = false;
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "002");
					result.addProperty("Message", "'Event' "+properties.getProperty("002"));
					response.getWriter().println(new Gson().toJson(result));
				}else{
					event = inputPayload.getString("Event");
					finalPayload.addProperty("Event", inputPayload.getString("Event").toUpperCase());
				}
			}else{
				//Error Response - Event not found
				mandParamVal = false;
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "002");
				result.addProperty("Message", "'Event' "+properties.getProperty("002"));
				response.getWriter().println(new Gson().toJson(result));
			}
			if(debug)
				response.getWriter().println("doGet.event.: "+ event);
			
			if (mandParamVal) {
				// Destination
				cpiDestConfig = getCPIDestination(request, response);
				destConfiguration = getDestinationURL(request, response);
				destURL = destConfiguration.get("URL").get().toString();
				if (debug)
					response.getWriter().println("doGet.destURL.: " + destURL);

				if (destURL.contains("service.xsodata")) {
					isRequestFromCloud = true;
				} else {
					isRequestFromCloud = false;
				}

				String loginID = commonUtils.getUserPrincipal(request, "name", response);
				
				if (debug)
					response.getWriter().println("loginID: " + loginID);

				// String aggregatorID = "";
				String aggregatorIDFromDest = "";
				aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
				if (isRequestFromCloud) {
					// JsonObject = Call desturl;+"UserCustomers"
					String regisFor = "";
					JsonObject userRegistrationChildObj = new JsonObject();

					switch(cpTypeId)
					{
						case "01":
							customerService = destURL + "UserCustomers?$filter=LoginID%20eq%20%27" + loginID
									+ "%27%20and%20AggregatorID%20eq%20%27" + aggregatorIDFromDest + "%27";
							if (debug)
								response.getWriter().println("partnerObj.executeURL: " + customerService);

							partnerObj = getCustomerFromCloud(request, response, customerService, debug);
							if (debug) {
								response.getWriter().println("partnerObj: " + partnerObj);
							}
							if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode") && !partnerObj.has("error")
									&& partnerObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0
									&& !partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject()
											.get("PartnerID").isJsonNull())
								dealerID = partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0)
										.getAsJsonObject().get("PartnerID").getAsString();

							if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode")
									&& partnerObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0
									&& !partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject()
											.get("Name").isJsonNull())
								dealerName = partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0)
										.getAsJsonObject().get("Name").getAsString();

							if (debug) {
								response.getWriter().println("dealerID: " + dealerID);
								response.getWriter().println("dealerName: " + dealerName);
							}

							finalPayload.addProperty("DealerCode", dealerID);
							finalPayload.addProperty("DealerName", dealerName);
						break;
	
						case "60":
							customerService = destURL + "Vendors?$filter=LoginID%20eq%20%27" + loginID
								+ "%27%20and%20AggregatorID%20eq%20%27" + aggregatorIDFromDest + "%27";
								if (debug)
									response.getWriter().println("vendorObj.executeURL: " + customerService);
		
								partnerObj = getVendorsFromCloud(request, response, customerService, debug);
								if (debug) {
									response.getWriter().println("partnerObj: " + partnerObj);
								}
								if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode") && !partnerObj.has("error")
										&& partnerObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0
										&& !partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject()
										.get("PartnerID").isJsonNull())
									vendorId = partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0)
									.getAsJsonObject().get("PartnerID").getAsString();
		
								if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode")
										&& partnerObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0
										&& !partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject()
										.get("Name").isJsonNull())
									vendorName = partnerObj.getAsJsonObject("d").getAsJsonArray("results").get(0)
									.getAsJsonObject().get("Name").getAsString();
		
								if (debug) {
									response.getWriter().println("VendorId: " + vendorId);
									response.getWriter().println("VendorName: " + vendorName);
								}
		
								finalPayload.addProperty("VendorCode", vendorId);
								finalPayload.addProperty("VendorName", vendorName);
						break;
					}
					// JsonObject = Call desturl;+"UserRegistrations"
					customerService = "";
					customerService = destURL + "UserRegistrations?$filter=LoginId%20eq%20%27" + loginID
							+ "%27%20and%20AggregatorID%20eq%20%27" + aggregatorIDFromDest + "%27";
					if (debug)
						response.getWriter().println("userRegistration.executeURL: " + customerService);

					userRegistrationObj = getUserRegistrationFromCloud(response, request, customerService, debug);
					if (debug) {
						response.getWriter().println("userRegistrationObj: " + userRegistrationObj);
					}

					if (!userRegistrationObj.isJsonNull()&&!userRegistrationObj.has("ErrorCode") && !userRegistrationObj.has("error")
							&& userRegistrationObj.get("d").getAsJsonObject().get("results").getAsJsonArray()
									.size() > 0) {
						for (int i = 0; i < userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results")
								.size(); i++) {
							userRegistrationChildObj = userRegistrationObj.getAsJsonObject("d")
									.getAsJsonArray("results").get(i).getAsJsonObject();
							if (!userRegistrationChildObj.get("RegistrationFor").isJsonNull())
								regisFor = userRegistrationChildObj.get("RegistrationFor").getAsString();

							if (regisFor.trim().length() == 0 || regisFor.equalsIgnoreCase("")) {
								if (!userRegistrationChildObj.get("CorpId").isJsonNull())
									corpID = userRegistrationChildObj.get("CorpId").getAsString();

								if (!userRegistrationChildObj.get("UserId").isJsonNull())
									userID = userRegistrationChildObj.get("UserId").getAsString();

								if (!userRegistrationChildObj.get("UserRegId").isJsonNull())
									userRegId = userRegistrationChildObj.get("UserRegId").getAsString();

								if (!userRegistrationChildObj.get("AggregatorID").isJsonNull())
									aggregatorID = userRegistrationChildObj.get("AggregatorID").getAsString();

								break;
							}
						}
						if (debug) {
							response.getWriter().println("corpID: " + corpID);
							response.getWriter().println("userID: " + userID);
							response.getWriter().println("userRegId: " + userRegId);
							response.getWriter().println("aggregatorID: " + aggregatorID);
						}
						// Final payload
						finalPayload.addProperty("CorpID", corpID);
						finalPayload.addProperty("UserID", userID);
						finalPayload.addProperty("URN", userRegId);
						finalPayload.addProperty("AggregatorID", aggregatorID);
					}

				} else {
					// JsonObject = Call
					// desturl;+"/sap/opu/odata/ARTEC/PYGW/UserCustomers
					String customerFilter = "", queryString = "", loginMethod = "";

					String sapclient = destConfiguration.get("sap-client").get().toString();
					if (debug)
						response.getWriter().println("sapclient:" + sapclient);

					String authMethod = destConfiguration.get("Authentication").get().toString();
					if (debug)
						response.getWriter().println("authMethod:" + authMethod);

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

					customerFilter = "LoginID eq '" + sessionID + "'";
					customerFilter = URLEncoder.encode(customerFilter, "UTF-8");

					customerFilter = customerFilter.replaceAll("%26", "&");
					customerFilter = customerFilter.replaceAll("%3D", "=");
					if(debug)
						response.getWriter().println("doGet.cpTypeId.: "+ cpTypeId);
					
					switch(cpTypeId)
					{
						case "01":
							if (debug)
								response.getWriter().println("customerFilter: " + customerFilter);
							
							if (sapclient != null) {
								customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client=" + sapclient
										+ "&$filter=" + customerFilter;
							} else {
								customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter=" + customerFilter;
							}
							if (debug)
								response.getWriter().println("CustomerService 1: " + customerService);

							partnerObj = getUserCustomerDetails(request, response, customerService, destConfiguration,
									debug);
							if (debug)
								response.getWriter().println("partnerObj1: " + partnerObj);
							if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode")) {
								finalPayload.addProperty("DealerCode", partnerObj.get("DealerId").getAsString());
								finalPayload.addProperty("DealerName", partnerObj.get("DealerName").getAsString());
							}
						break;
	
						case "60":
	
							if (debug)
								response.getWriter().println("customerFilter: " + customerFilter);
							if (sapclient != null) {
								customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/Vendors?sap-client=" + sapclient
										+ "&$filter=" + customerFilter;
							} else {
								customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/Vendors?$filter=" + customerFilter;
							}
							if (debug)
								response.getWriter().println("CustomerService 1: " + customerService);
	
							partnerObj = getVendorDetails(request, response, customerService, destConfiguration,debug);
							if (debug)
								response.getWriter().println("partnerObj1: " + partnerObj);
							if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode")) {
								finalPayload.addProperty("VendorCode", partnerObj.get("VendorNo").getAsString());
								finalPayload.addProperty("VendorName", partnerObj.get("VendorName").getAsString());
							}
						break;
					}
					// JsonObject = Call
					// desturl;+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations
					sessionID = "";
					if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("url2:" + url);
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
						response.getWriter().println("sessionID2:" + sessionID);

					customerService = "";
					customerFilter = "";
					customerFilter = "LoginId eq '" + sessionID + "'";
					customerFilter = URLEncoder.encode(customerFilter, "UTF-8");

					customerFilter = customerFilter.replaceAll("%26", "&");
					customerFilter = customerFilter.replaceAll("%3D", "=");
					if (debug)
						response.getWriter().println("customerFilter: " + customerFilter);

					if (sapclient != null) {
						customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserRegistrations?sap-client="
								+ sapclient + "&$filter=" + customerFilter;
					} else {
						customerService = destURL + "/sap/opu/odata/ARTEC/PYGW/UserRegistrations?$filter="
								+ customerFilter;
					}
					if (debug)
						response.getWriter().println("CustomerService 2: " + customerService);

					userRegistrationObj = getUserRegistrationDetails(request, response, customerService,
							destConfiguration, debug);
					if (debug)
						response.getWriter().println("userRegistrationObj1: " + userRegistrationObj);
					if (!userRegistrationObj.isJsonNull()&&!userRegistrationObj.has("ErrorCode")) {
						finalPayload.addProperty("CorpID", userRegistrationObj.get("CorpId").getAsString());
						finalPayload.addProperty("UserID", userRegistrationObj.get("UserId").getAsString());
						finalPayload.addProperty("URN", userRegistrationObj.get("UserRegId").getAsString());
						finalPayload.addProperty("AggregatorID", userRegistrationObj.get("AggregatorID").getAsString());
					}
					// if (debug)
					// response.getWriter().println(finalPayload);
				}

				if (debug)
					response.getWriter().println("finalPayload: " + finalPayload);
				if (!partnerObj.isJsonNull()&&!partnerObj.has("ErrorCode")&&!partnerObj.has("error")) {
					if(debug)
						response.getWriter().println("IF partnerObj: " + partnerObj);
					if (!userRegistrationObj.isJsonNull()&&!userRegistrationObj.has("ErrorCode")&&!userRegistrationObj.has("error")){
						switch(cpTypeId)
						{
							case "01":
							// Check If Account No is isCFSOD or not
							Map<String, String> userAccountsEntry = new HashMap<String, String>();
							Map<String, String> userRegMap = new HashMap<String, String>();
							Map<String, String> accountsWSResponseMap = new HashMap<String, String>();
							JsonObject onODAccountObj = new JsonObject();
							String aggrID = "", isCFSOFFromWS = "", accountTypeFromWS = "";
							aggrID = finalPayload.get("AggregatorID").getAsString();

							userAccountsEntry.put("BankAccntNo", finalPayload.get("ODAccountNumber").getAsString());

							userRegMap.put("CorpId", finalPayload.get("CorpID").getAsString());
							userRegMap.put("UserId", finalPayload.get("UserID").getAsString());
							userRegMap.put("UserRegId", finalPayload.get("URN").getAsString());

							accountsWSResponseMap = accountClient.callAccountsWebservice(request, response,
									userAccountsEntry, userRegMap, aggrID, debug);
							for (String key : accountsWSResponseMap.keySet()) {
								if (debug)
									response.getWriter().println("callAccountsWebservice-accountsWSResponseMap: " + key
											+ " - " + accountsWSResponseMap.get(key));
							}
							if (accountsWSResponseMap.get("Error").equalsIgnoreCase("059")) {
								// Error Response - Error during webservice call
								JsonObject result = new JsonObject();
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", "059");
								result.addProperty("Message", "Unable to serve your request. Error From Webservice");
								response.getWriter().println(new Gson().toJson(result));
							} else {
								// Successful webservice call
								isCFSOFFromWS = accountsWSResponseMap.get("IsCFSOD");
								accountTypeFromWS = accountsWSResponseMap.get("AccountType");
								if (accountTypeFromWS != null && accountTypeFromWS.trim().length() > 0) {
									if (accountTypeFromWS.equalsIgnoreCase("ODA") && isCFSOFFromWS.equalsIgnoreCase("Y")) {
										switch (lenderCode) {
										case "AXISB":
											if (debug) {
												response.getWriter().println("AXISB Case");
											}
											callOnObjectEvent(response, finalPayload, cpiDestConfig, properties, debug);
											break;
										default:
											if (debug) {
												response.getWriter().println("default Case");
											}
											// here we need to check the record exist in the ARTEC_PC_MATERIAL Table
											JsonObject materialObj = checkRecordsExistInMaterialTbl(properties, loginID, destConfiguration, request, response, partnerObj, debug, aggregatorIDFromDest, accountNumber);
											if (debug) {
												response.getWriter().println("materialObj:" + materialObj);
											}

											if (materialObj.get("Status").getAsString().equalsIgnoreCase("000001")) 
											{
												if (debug) {
													response.getWriter().println("isRequestFromCloud:" + isRequestFromCloud);
												}
												
												if(!isRequestFromCloud)
												{
													String dealerId=finalPayload.get("DealerCode").getAsString();
													isInsertionSuccess=insertIntoSupplyChainFinance(request, response, accountNumber,dealerId, cpTypeId, debug);
												}
												else
												{
													isInsertionSuccess=true;
												}


												if(isInsertionSuccess)
												{
													callOnObjectEvent(response, finalPayload, cpiDestConfig, properties, debug);
												}
											} else {
												if (materialObj.has("ExceptionTrace")) {
													materialObj.remove("ExceptionTrace");
												}
												if (materialObj.has("error")) {
													materialObj.remove("error");
												}
												materialObj.addProperty("Message", "Current we are facing technical issues, please retry later");
												materialObj.addProperty("Status", "000002");
												materialObj.addProperty("ErrorCode", "PF001");
												response.getWriter().println(materialObj);
											}
										}

									}
									else {
										// Success Response - Account Type
										// may/maynot be
										// ODA, but it is not a CFSOD
										JsonObject result = new JsonObject();
										result.addProperty("Status", "000001");
										result.addProperty("ErrorCode", "");
										result.addProperty("Message", "Success");
										response.getWriter().println(new Gson().toJson(result));
									}
								} else {
									// Success Response - Account Type is blank from
									// WS
									JsonObject result = new JsonObject();
									result.addProperty("Status", "000001");
									result.addProperty("ErrorCode", "");
									result.addProperty("Message", "Success");
									response.getWriter().println(new Gson().toJson(result));
								}
								if (debug)
									response.getWriter().println("onODAccountObj: " + onODAccountObj);
							}
							break;
							case "60":
								if (debug)
									response.getWriter().println("For cptype 60");
								if(!isRequestFromCloud){
									String vendorId2=finalPayload.get("VendorCode").getAsString();
									isInsertionSuccess=insertIntoSupplyChainFinance(request, response, accountNumber,vendorId2, cpTypeId, debug);
								}else{
									isInsertionSuccess = true;
								}
								
								if(isInsertionSuccess)
								{
									JsonObject materialObj1=new JsonObject();
									
									materialObj1.addProperty("Message", "Succesfully inserted");
									materialObj1.addProperty("Status", "000001");
									materialObj1.addProperty("ErrorCode", "");
									response.getWriter().println(materialObj1);
								}else{
									JsonObject materialObj1=new JsonObject();
									
									materialObj1.addProperty("Message", "Current we are facing technical issues, please retry later");
									materialObj1.addProperty("Status", "000002");
									materialObj1.addProperty("ErrorCode", "J002");
									response.getWriter().println(materialObj1);
									
								}
							break;
						}
					}else{
						if(debug)
							response.getWriter().println("Else-nothing to do (not handled): ");
					}
				} else {
					response.getWriter().println(userRegistrationObj);
				}
			}else{
				//partnerObj
				response.getWriter().println(partnerObj);
			}
				
		}
		catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
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
//							  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}
	
	public Destination getCPIDestination(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//		LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = DestinationUtils.CPI_CONNECT;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
			// destConfiguration = configuration.getConfiguration(destinationName);
//			LOGGER.info("5. destination configuration object created");	
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", DestinationUtils.PCGW_UTILS_OP));
//			LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}	
	//added
	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//			LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = DestinationUtils.PCGW_UTILS_OP;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
			// destConfiguration = configuration.getConfiguration(destinationName);
//				LOGGER.info("5. destination configuration object created");	
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(destinationName, options);
				destConfiguration = destinationAccessor.get();
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", DestinationUtils.PCGW_UTILS_OP));
//				LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}
	
	private JsonObject getCustomerFromCloud(HttpServletRequest request, HttpServletResponse response,String executeURL, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject userCustomerResponse = new JsonObject();
		try {
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getCustomerFromCloud.userName: "+userName);
				response.getWriter().println("getCustomerFromCloud.password: "+password);
				response.getWriter().println("getCustomerFromCloud.userPass: "+userPass);
			}
			//userCustomerResponse = commonUtils.executeURL(executeURL, userPass, response);
			userCustomerResponse=commonUtils.executeAccountLinkingURL(executeURL, userPass, response, debug);
			if (debug)
				response.getWriter().println("getCustomerFromCloud.userRegisResponse: "+userCustomerResponse);
			
		}catch (Exception e) {
			/*StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));*/
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}
		return userCustomerResponse;
	}
	
	private JsonObject getUserRegistrationFromCloud(HttpServletResponse response, HttpServletRequest request, String executeURL, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject userRegistrationResponse = new JsonObject();
		try {
			
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getUserRegistrationFromCloud.userName: "+userName);
				response.getWriter().println("getUserRegistrationFromCloud.password: "+password);
				response.getWriter().println("getUserRegistrationFromCloud.userPass: "+userPass);
			}
			//userRegistrationResponse = commonUtils.executeURL(executeURL, userPass, response);
			userRegistrationResponse=commonUtils.executeAccountLinkingURL(executeURL, userPass, response, debug);
			if (debug)
				response.getWriter().println("getUserRegistrationFromCloud.userRegisResponse: "+userRegistrationResponse);
			
		} catch (Exception e) {
			/*StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));*/
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}
		return userRegistrationResponse;
	}
	
	private JsonObject getUserCustomerDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String customerNo = "";
		String destURL="", errorCode="", userName="", password="", authParam="", authMethod="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userCustomerJson = new JsonObject();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
		try{
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */

			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				// destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();

			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				// basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			// String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			// int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			// if(debug){
			// 	response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			// 	response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			// 	response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
			// }
			
			// HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			// DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			
			// closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
			userCustomersGet = new HttpGet(customerService);
			// userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
			userCustomersGet.setHeader("Accept", "application/atom+xml");
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+authParam);
			}else{
				userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}
			
			HttpResponse httpResponse = client.execute(userCustomersGet);
			
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
				response.getWriter().println("validateCustomer.statusCode: "+statusCode);
				
				Header headers[] = httpResponse.getAllHeaders();
                for(Header h:headers){
                	response.getWriter().println("Response Headers:" +h.getName() + ": " + h.getValue());
                }
			}
			
			if(statusCode==200){
				customerEntity = httpResponse.getEntity();
				if(customerEntity != null){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(customerEntity);
					
					if (debug)
						response.getWriter().println("retSrc: "+retSrc);
	//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	//					response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList idList = document.getElementsByTagName("d:CustomerNo");
					NodeList nameList = document.getElementsByTagName("d:Name");
	//	            NodeList name1List = document.getElementsByTagName("d:Name");SupplyChainPartnerType
					
					userCustomerJson.addProperty("DealerId", idList.item(0).getTextContent());
					userCustomerJson.addProperty("DealerName", nameList.item(0).getTextContent());
				}
			}else{
				customerEntity=httpResponse.getEntity();
				String retSrc = EntityUtils.toString(customerEntity);
				if(debug){
					response.getWriter().println("Http Response:"+retSrc);
				}
				String statusLine = httpResponse.getStatusLine().toString();
				StringBuffer buffer=new StringBuffer();
				if(statusLine!=null){
					String[] split = statusLine.split(" ");
					for(int i=0;i<split.length-1;i++){
						buffer.append(split[i]).append(" ");
					}
					}
					if(buffer==null || buffer.length()<0){
						buffer.append(statusCode);
					}
					String[] url = customerService.split("\\?");
					customerService=url[0];
				if(statusCode==401){
					userCustomerJson.addProperty("Message","Unauthorized Access with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}else if(statusCode==404){
					userCustomerJson.addProperty("Message","Resource not found with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}else{
					userCustomerJson.addProperty("Message","Unable to fetch Records with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}
			}
		}catch (RuntimeException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("RuntimeException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("ParserConfigurationException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} catch (SAXException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("SAXException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} /* catch (NamingException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("NamingException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result)); 
		}*/
		finally
		{
			// closableHttpClient.close();
			return userCustomerJson;
		}
	}	

	private JsonObject getUserRegistrationDetails(HttpServletRequest request, HttpServletResponse response, String serviceURL, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String customerNo = "";
		String destURL="", errorCode="", userName="", password="", authParam="", authMethod="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userCustomerJson = new JsonObject();
		
		HttpGet userRegistrationGet = null;
		HttpEntity customerEntity = null;
		//CloseableHttpClient closableHttpClient = null;
		try{
			
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				// destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();

			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				// basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
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
	        userRegistrationGet = new HttpGet(serviceURL);
	        // userRegistrationGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        userRegistrationGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        userRegistrationGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	userRegistrationGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+authParam);
	        }else{
	        	userRegistrationGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(userRegistrationGet);
			HttpResponse httpResponse = client.execute(userRegistrationGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("validateCustomer.statusCode: "+statusCode);
			if (statusCode == 200) {
				customerEntity = httpResponse.getEntity();
				if (customerEntity != null) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(customerEntity);

					if (debug)
						response.getWriter().println("retSrc: " + retSrc);
					// if(null != request.getParameter("debug") &&
					// request.getParameter("debug").equalsIgnoreCase("true"))
					// response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList corpList = document.getElementsByTagName("d:CorpId");
					NodeList userList = document.getElementsByTagName("d:UserId");
					NodeList userRegList = document.getElementsByTagName("d:UserRegId");
					NodeList aggrIDList = document.getElementsByTagName("d:AggrID");
					// for(int i=0 ; i < corpList.getLength() ; i++)
					// {
					userCustomerJson.addProperty("CorpId", corpList.item(0).getTextContent());
					userCustomerJson.addProperty("UserId", userList.item(0).getTextContent());
					userCustomerJson.addProperty("UserRegId", userRegList.item(0).getTextContent());
					userCustomerJson.addProperty("AggregatorID", aggrIDList.item(0).getTextContent());
					// break;
					// }
					if (debug) {
						if (debug)
							response.getWriter().println("retSrc: " + retSrc);
					}
				}
			} else {
				customerEntity = httpResponse.getEntity();
				String retSrc = EntityUtils.toString(customerEntity);
				if (debug) {
					response.getWriter().println("Http Response:" + retSrc);
				}
				String statusLine=httpResponse.getStatusLine().toString();
				StringBuffer buffer=new StringBuffer();
				if(statusLine!=null){
					String[] split = statusLine.split(" ");
					for(int i=0;i<split.length-1;i++){
						buffer.append(split[i]).append(" ");
					}
					}
					if(buffer==null || buffer.length()<0){
						buffer.append(statusCode);
					}
					String[] url = serviceURL.split("\\?");
					serviceURL=url[0];
				if (statusCode == 401) {
					userCustomerJson.addProperty("Message",
							"Unauthorized Access with Http Status Code:" + buffer+"for the service "+serviceURL);
					userCustomerJson.addProperty("ErrorCode", statusCode);
					userCustomerJson.addProperty("Status", "000002");
				} else if (statusCode == 404) {
					userCustomerJson.addProperty("Message",
							"Resource not found with Http Status Code:" + buffer+"for the service "+serviceURL);
					userCustomerJson.addProperty("ErrorCode", statusCode);
					userCustomerJson.addProperty("Status", "000002");
				} else {
					userCustomerJson.addProperty("Message",
							"Unable to fetch Records with Http Status Code:" + buffer+"for the service "+serviceURL);
					userCustomerJson.addProperty("ErrorCode", statusCode);
					userCustomerJson.addProperty("Status", "000002");
				}

			}
		}catch (RuntimeException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("RuntimeException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL",DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("ParserConfigurationException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} catch (SAXException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("SAXException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} /* catch (NamingException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("NamingException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} */
		finally{
			//closableHttpClient.close();
		}
		return userCustomerJson;
	}
	
	private JsonObject checkRecordsExistInMaterialTbl(Properties props,String loginID,Destination destinationConfig,HttpServletRequest request,HttpServletResponse response,JsonObject partnerObj,boolean debug,String aggregatorID,String accountNumber){
		CommonUtils commonUtils=new CommonUtils();
		String destUrl="",usename="",password="",userpass="",executeURL="",cpType="",cpNo="";
		JsonObject resObj=new JsonObject();
		try{
			if(debug){
				response.getWriter().println("checkRecordsExistInMaterialTbl partnerObj: "+partnerObj);
			}
			
			destUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			usename=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userpass=usename+":"+password;
			executeURL=destUrl+"ARTEC_PC_MATERIAL?$filter=AGGREGATORID%20eq%20%27"+aggregatorID+"%27";
			if(debug){
				response.getWriter().println("ARTEC_PC_MATERIAL Execute URL:"+executeURL);
			}
			JsonObject materialObjFromDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);
		      if(debug){
		    	  response.getWriter().println("materialObj FromDb:"+materialObjFromDb);
		      }
		      
		     if(materialObjFromDb.get("Status").getAsString().equalsIgnoreCase("000001")){
		    		// fetch the records from the SupplyChainFinance Table.
		    		 boolean isRequestFromCloud=false;
		    		 String destURL = destinationConfig.get("URL").get().toString();
						if (debug)
							response.getWriter().println("doGet.destURL.: " + destURL);

						if (destURL.contains("service.xsodata")) {
							isRequestFromCloud = true;
						} else {
							isRequestFromCloud = false;
							 
						}

						boolean recordsNotExist=false;
					if (isRequestFromCloud) {
						partnerObj=partnerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
						cpNo=partnerObj.get("CustomerNo").getAsString();
						cpType="01";
						if(debug){
							response.getWriter().println("cpNo:"+cpNo);
							response.getWriter().println("CPType:"+cpType);
						}
						String pygDestUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						String pygUsename = commonUtils.getODataDestinationProperties("User",
								DestinationUtils.PYGWHANA);
						String pygPssword = commonUtils.getODataDestinationProperties("Password",
								DestinationUtils.PYGWHANA);
						String pygUserpass = pygUsename + ":" + pygPssword;
						executeURL = pygDestUrl + "SupplyChainFinances?$filter=CPGUID%20eq%20%27" + cpNo
								+ "%27%20and%20CPTypeID%20eq%20%27" + cpType + "%27%20and%20AggregatorID%20eq%20%27"
								+ aggregatorID + "%27";
						if (debug) {
							response.getWriter().println("SupplyChainFinances executeUrl:" + executeURL);
						}
						JsonObject suplyFinanceObj = commonUtils.executeODataURL(executeURL, pygUserpass, response, debug);
						if (debug) {
							response.getWriter().println("response from suplyFinanceObj:" + suplyFinanceObj);
						}
						if (suplyFinanceObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
							if (suplyFinanceObj.get("Message").getAsJsonObject().get("d").getAsJsonObject()
									.get("results").getAsJsonArray().size() == 0) {
								recordsNotExist = true;
							}
						} else {
							return suplyFinanceObj;
						}
					} else {
						cpNo=partnerObj.get("DealerId").getAsString();
						cpType="01";
						
						if(cpNo.matches("[0-9]+")){
							int parseInt = Integer.parseInt(cpNo);
							cpNo=String.format("%010d", parseInt);
						}
						if(debug){
							response.getWriter().println("cpNo:"+cpNo);
							response.getWriter().println("CPType:"+cpType);
						}
						String sessionID="";
						String sapclient = destinationConfig.get("sap-client").get().toString();
						if (debug)
							response.getWriter().println("sapclient:" + sapclient);

						String authMethod = destinationConfig.get("Authentication").get().toString();
						if (debug)
							response.getWriter().println("authMethod:" + authMethod);

						sessionID ="";
						if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug)
								response.getWriter().println("url2:" + url);
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						} else{
							String loginMethod = destinationConfig.get("LoginMethod").get().toString();
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
							response.getWriter().println("sessionID2:" + sessionID);
						

							//String scfFilter ="LoginID eq '"+sessionID+"' CPTypeID eq '"+cpType+"' and CPGUID eq '"+cpNo+"'";
						String scfService="", scfFilter="";
						scfFilter = "LoginID eq '"+sessionID+"' and CPTypeID eq '"+cpType+"' and CPGUID eq '"+cpNo+"'";
						if (debug)
							response.getWriter().println("scfFilter1: "+scfFilter);
						
						scfFilter = URLEncoder.encode(scfFilter, "UTF-8");
						
						scfFilter = scfFilter.replaceAll("%26", "&");
						scfFilter = scfFilter.replaceAll("%3D", "=");
						scfFilter = scfFilter.replaceAll("%3D", "=");
						if (debug)
							response.getWriter().println("scfFilter: "+scfFilter);
							if(sapclient != null){
								scfService = destURL+"/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances?sap-client="+ sapclient +"&$filter="+ scfFilter;
							}
							else{
								scfService = destURL+"/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances?$filter="+ scfFilter;
							}
							if (debug)
								response.getWriter().println("scfService : "+scfService);
							
						JsonObject supplyChainFinanceObj = executeURL(destinationConfig, scfService, response, request, sessionID,debug);
                       if(debug){
                    	   response.getWriter().println("supplyChainFinanceObj:"+supplyChainFinanceObj);
                       }
						if(supplyChainFinanceObj.get("Status").getAsString().equalsIgnoreCase("000001")){
                        	if (supplyChainFinanceObj.get("Message").getAsJsonObject().get("d").getAsJsonObject()
									.get("results").getAsJsonArray().size() == 0) {
                        		recordsNotExist = true;
							}
                        }else{
                        	return supplyChainFinanceObj;
                        }
					}
		    	 
					if(debug){
						response.getWriter().println("recordsNotExist:"+recordsNotExist);
					}
					if(recordsNotExist){
					String aggr0008 = props.getProperty("AggregatorID");
					// Filter the PGPayments Table by AggregatorID and AccountNumber. if record exist send the Success response else insert a new Record.
					executeURL = destUrl + "PGPayments?$filter=AggregatorID%20eq%20%27"+aggr0008+"%27%20and%20CPAccountno%20eq%20%27"+accountNumber+"%27";
                    if(debug){
                    	response.getWriter().println("PGPayments execute URL:"+executeURL);
                    }
                   
                    JsonObject pgPaymentsRoc = commonUtils.executeODataURL(executeURL, userpass, response, debug);
                    if(debug){
                    	response.getWriter().println("PGPayments Record pgPaymentsRoc:"+pgPaymentsRoc);
                    }
                    
                    if(pgPaymentsRoc.get("Status").getAsString().equalsIgnoreCase("000001")){
                    	if(pgPaymentsRoc.get("Message").getAsJsonObject().get("d").getAsJsonObject()
								.get("results").getAsJsonArray().size() == 0){
						JsonObject pgPaymentsPayload = new JsonObject();
						String pgPaymentGuid = commonUtils.generateGUID(36);
						pgPaymentsPayload.addProperty("PGPaymentGUID", pgPaymentGuid);
						pgPaymentsPayload.addProperty("AggregatorID", aggr0008);//always be AGGR0008 Read from Properties file.
						pgPaymentsPayload.addProperty("CPNo", cpNo);
						pgPaymentsPayload.addProperty("CPTypeID", cpType);
						pgPaymentGuid=pgPaymentGuid.substring(0, 27);
						String trackID = "ART_" + pgPaymentGuid;
						pgPaymentsPayload.addProperty("TrackID", trackID);
						pgPaymentsPayload.addProperty("PGTransactionID", "");
						pgPaymentsPayload.addProperty("PGTxnStatusID", "000100");
						pgPaymentsPayload.addProperty("CPAccountno", accountNumber);
						pgPaymentsPayload.addProperty("PaymentAmount", "0.00");
						pgPaymentsPayload.addProperty("PaymentStatusID", "000200");
						pgPaymentsPayload.addProperty("PGPaymnetPostingStatusID", "000000");
						pgPaymentsPayload.addProperty("PGTxnMessage", "Offline Account");
						long createdOnDate = commonUtils.getCreatedOnDate();
						String createdAtTime = commonUtils.getCreatedAtTime();
						pgPaymentsPayload.addProperty("PGTxnDate", "/Date(" + createdOnDate + ")/");
						pgPaymentsPayload.addProperty("PGTxnTime", createdAtTime);
						pgPaymentsPayload.addProperty("CreatedOn", "/Date(" + createdOnDate + ")/");
						pgPaymentsPayload.addProperty("CreatedAt", createdAtTime);
						pgPaymentsPayload.addProperty("PymntFor", "SCFOFF");
						JsonObject arrgReg = new JsonObject();
						arrgReg.addProperty("AggregatorID", aggregatorID);
						arrgReg.addProperty("ProcessingFeePerc", "0.00");
						arrgReg.addProperty("OfferAmount", "0.00");
						pgPaymentsPayload.addProperty("AggregatorRef", arrgReg + "");
						executeURL = destUrl + "PGPayments";
						if (debug) {
							response.getWriter().println("PGPayments update executeURL:" + executeURL);
							response.getWriter().println("PGPayments Updated Payload:" + pgPaymentsPayload);
						}
						JsonObject pgPaymentPstRes = commonUtils.executePostURL(executeURL, userpass, response,
								pgPaymentsPayload, request, debug, "PCGWHANA");
						if (debug) {
							response.getWriter().println("pgPaymentPstRes:" + pgPaymentPstRes);
						}
						return pgPaymentPstRes;
					
                    }else{
                    	resObj.addProperty("Message", "Success");
			    		 resObj.addProperty("ErrorCode", "");
			    		 resObj.addProperty("Status", "000001");
			    		 return resObj;	
                    }
                    }else{
                    	return pgPaymentsRoc;
                    }
					}else{
						resObj.addProperty("Message", "Success");
			    		 resObj.addProperty("ErrorCode", "");
			    		 resObj.addProperty("Status", "000001");
			    		 return resObj;
					}
		     } else{
		    	 // unable to fetch the records from the Material Table.
		    	 return materialObjFromDb;
		     }
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			if(ex.getLocalizedMessage()!=null){
			 resObj.addProperty("Message", ex.getLocalizedMessage());
			}
			 resObj.addProperty("ExceptionTrace", buffer.toString());
    		 resObj.addProperty("ErrorCode", "J002");
    		 resObj.addProperty("Status", "000002");
    		 return resObj;
		}
	}
	
	
	public JsonObject executeURL(Destination destConfiguration,String executeURL, HttpServletResponse response, HttpServletRequest request, String sessionID,
			boolean debug) throws IOException {
		String userName="", password="", authParam="", authMethod="", basicAuth=""; 
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		JsonObject scfJson = new JsonObject();
		HttpGet scfGet = null;
		HttpEntity scfEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
			authMethod = destConfiguration.get("Authentication").get().toString();
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
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        scfGet = new HttpGet(executeURL);
	        // scfGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        scfGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        scfGet.setHeader("Accept", "application/json");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	scfGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	scfGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(scfGet);
			HttpResponse httpResponse = client.execute(scfGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("getScfDetails.statusCode: "+statusCode);
	        scfEntity = httpResponse.getEntity();
			String retSrc = EntityUtils.toString(scfEntity);
			if(debug){
				response.getWriter().println("retSrc:"+retSrc);
			}
	        if(statusCode/100==2){
				JsonParser parser = new JsonParser();
				JsonObject scfFromDb = (JsonObject)parser.parse(retSrc);
				if(debug)
					response.getWriter().println("scfFromDb: "+scfFromDb);
				scfJson.addProperty("Status", "000001");
				scfJson.addProperty("ErrorCode", "");
				scfJson.add("Message", scfFromDb);
				return scfJson;
			}else{
				scfJson.addProperty("Status", "000002");
				scfJson.addProperty("ErrorCode", "J002");
				scfJson.addProperty("Message", retSrc);
				return scfJson;
			}
		}catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			scfJson.addProperty("Status", "000002");
			scfJson.addProperty("ErrorCode", "J002");
			scfJson.addProperty("Message", e.getLocalizedMessage());
			scfJson.addProperty("ExceptionTrace", buffer.toString());
			return scfJson;
		} 
		finally
		{
			// closableHttpClient.close();
		}
	}
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private JsonObject getVendorsFromCloud(HttpServletRequest request, HttpServletResponse response,String executeURL, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject userCustomerResponse = new JsonObject();
		try {
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			userPass = userName+":"+password;

			if (debug) {
				response.getWriter().println("getVendorFromCloud.userName: "+userName);
				response.getWriter().println("getVendorFromCloud.password: "+password);
				response.getWriter().println("getVendorFromCloud.userPass: "+userPass);
			}
			//userCustomerResponse = commonUtils.executeURL(executeURL, userPass, response);
			userCustomerResponse=commonUtils.executeAccountLinkingURL(executeURL, userPass, response, debug);
			if (debug)
				response.getWriter().println("getVendorFromCloud.vendorResponse: "+userCustomerResponse);

		}catch (Exception e) {
			/*StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));*/
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());

			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="";

			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);

			userPass = userName+":"+password;

			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);

			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}
		return userCustomerResponse;
	}

	private JsonObject getVendorDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String customerNo = "";
		String destURL="", errorCode="", userName="", password="", authParam="", authMethod="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();

		JsonObject userCustomerJson = new JsonObject();

		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getVendorId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			// destConfiguration.get("URL").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password;
				/* encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				String encodedStr = new String(encodedByte);
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes()); */
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}

			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
				// destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();

			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if(debug){
				response.getWriter().println("getVendorDetails.proxyType: "+proxyType);
				response.getWriter().println("getVendorDetails.proxyHost: "+proxyHost);
				response.getWriter().println("getVendorDetails.proxyPort: "+proxyPort);
			}

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
			userCustomersGet = new HttpGet(customerService);
			// userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
			userCustomersGet.setHeader("Accept", "application/atom+xml");
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+authParam);
			}else{
				userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
				response.getWriter().println("validateVendor.statusCode: "+statusCode);

				Header headers[] = httpResponse.getAllHeaders();
				for(Header h:headers){
					response.getWriter().println("Response Headers:" +h.getName() + ": " + h.getValue());
				}
			}

			if(statusCode==200){
				customerEntity = httpResponse.getEntity();
				if(customerEntity != null){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(customerEntity);

					if (debug)
						response.getWriter().println("retSrc: "+retSrc);
					//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					//					response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList idList = document.getElementsByTagName("d:VendorNo");
					NodeList nameList = document.getElementsByTagName("d:VendorName1");
					//	            NodeList name1List = document.getElementsByTagName("d:Name");SupplyChainPartnerType

					userCustomerJson.addProperty("VendorNo", idList.item(0).getTextContent());
					userCustomerJson.addProperty("VendorName", nameList.item(0).getTextContent());
				}
			}else{
				customerEntity=httpResponse.getEntity();
				String retSrc = EntityUtils.toString(customerEntity);
				if(debug){
					response.getWriter().println("Http Response:"+retSrc);
				}
				String statusLine = httpResponse.getStatusLine().toString();
				StringBuffer buffer=new StringBuffer();
				if(statusLine!=null){
					String[] split = statusLine.split(" ");
					for(int i=0;i<split.length-1;i++){
						buffer.append(split[i]).append(" ");
					}
				}
				if(buffer==null || buffer.length()<0){
					buffer.append(statusCode);
				}
				String[] url = customerService.split("\\?");
				customerService=url[0];
				if(statusCode==401){
					userCustomerJson.addProperty("Message","Unauthorized Access with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}else if(statusCode==404){
					userCustomerJson.addProperty("Message","Resource not found with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}else{
					userCustomerJson.addProperty("Message","Unable to fetch Records with Http Status Code:"+buffer+"for the service "+customerService);
					userCustomerJson.addProperty("ErrorCode",statusCode);
					userCustomerJson.addProperty("Status","000002");
				}
			}
		}catch (RuntimeException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("RuntimeException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());

			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";

			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);

			userPass = userName+":"+password;

			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);

			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("ParserConfigurationException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());

			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";

			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);

			userPass = userName+":"+password;

			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);

			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} catch (SAXException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("SAXException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());

			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";

			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);

			userPass = userName+":"+password;

			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);

			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} /* catch (NamingException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("NamingException in validateCustomer: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());

			ODataLogs oDataLogs = new ODataLogs();
			String oDataURL="", loginID="", logID="", aggregatorID="", userPass="";

			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			loginID = commonUtils.getUserPrincipal(request, "name", response);

			userPass = userName+":"+password;

			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "OnODAccount", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);

			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", "Technical Issue Encountered: "+e.getClass()+"-"+e.getLocalizedMessage()+". Ref "+logID);
			result.addProperty("FullTrace", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		} */
		finally
		{
			// closableHttpClient.close();
			return userCustomerJson;
		}
	}

	public boolean insertIntoSupplyChainFinance(HttpServletRequest request,HttpServletResponse response,String accountNumber,String dealerID, String cpTypeID, boolean debug) throws IOException
	{
		boolean isInsertionSuccess=false;
		String executeURL="";
		String formattedStr="";
		String cpGuid1="";
		CommonUtils commonUtils=new CommonUtils();
		JsonObject supplyFinanceObj=null;
//		String cpType="01";
		String  createdAt ="",createdBy="",id="";
		long createdOnInMillis=0;
		JsonObject supplyChainFinanceEntry = new JsonObject();
		JsonObject supplyChainFinanceResponse = new JsonObject();
		try
		{
			String pygDestUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			String pygUsename = commonUtils.getODataDestinationProperties("User",DestinationUtils.PYGWHANA);
			String pygPssword = commonUtils.getODataDestinationProperties("Password",DestinationUtils.PYGWHANA);
			String aggID = commonUtils.getODataDestinationProperties("AggregatorID",DestinationUtils.PYGWHANA);

			String pygUserpass = pygUsename + ":" + pygPssword;
			
			try{
				int number = Integer.parseInt(dealerID);
				formattedStr = ("0000000000" + dealerID).substring(dealerID.length());
				dealerID = formattedStr;
			}catch (NumberFormatException e) {
//				formattedStr = customerNo;
			}
			
			if(debug)
				response.getWriter().println("insertIntoSupplyChainFinance formatted dealerID"+dealerID);

			executeURL = pygDestUrl + "SupplyChainFinances?$filter=CPGUID%20eq%20%27" + dealerID
					+ "%27%20and%20CPTypeID%20eq%20%27" + cpTypeID + "%27%20and%20AggregatorID%20eq%20%27"
					+ aggID +"%27%20and%20AccountNo%20eq%20%27" + accountNumber + "%27";
			if (debug) {
				response.getWriter().println("SupplyChainFinances executeUrl:" + executeURL);
			}
			supplyFinanceObj = commonUtils.executeODataURL(executeURL, pygUserpass, response, debug);
			if (debug) {
				response.getWriter().println("response from suplyFinanceObj:" + supplyFinanceObj);
			}
			if (supplyFinanceObj.get("Status").getAsString().equalsIgnoreCase("000001")) 
			{
				if (supplyFinanceObj.get("Message").getAsJsonObject().get("d").getAsJsonObject()
						.get("results").getAsJsonArray().size() > 0) 
				{
					isInsertionSuccess=true;
				}
				else    
				{		
					executeURL="";
					id = commonUtils.generateGUID(36);
					createdBy = commonUtils.getUserPrincipal(request, "name", response);
					createdAt = commonUtils.getCreatedAtTime();
					createdOnInMillis = commonUtils.getCreatedOnDate();
					supplyChainFinanceEntry.addProperty("ID", id);
					supplyChainFinanceEntry.addProperty("CreatedBy", createdBy);
					supplyChainFinanceEntry.addProperty("CreatedAt", createdAt);
					supplyChainFinanceEntry.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
					supplyChainFinanceEntry.addProperty("AccountNo", accountNumber);
					supplyChainFinanceEntry.addProperty("AggregatorID", aggID);
					supplyChainFinanceEntry.addProperty("CPGUID", dealerID);
					supplyChainFinanceEntry.addProperty("CPTypeID", cpTypeID);
					supplyChainFinanceEntry.addProperty("StatusID", "000002");
					supplyChainFinanceEntry.addProperty("CallBackStatus", "000080");
					supplyChainFinanceEntry.addProperty("ECompleteDate", "/Date("+createdOnInMillis+")/");
					supplyChainFinanceEntry.addProperty("ECompleteTime", createdAt);
					
					executeURL = pygDestUrl + "SupplyChainFinances";
					if(debug){
						response.getWriter().println("POST SupplyChainFinances executeURL: "+executeURL);
						response.getWriter().println("POST SupplyChainFinances supplyChainFinanceEntry: "+supplyChainFinanceEntry);
					}
					supplyChainFinanceResponse = commonUtils.executePostURL(executeURL, pygUserpass, response, supplyChainFinanceEntry, request, debug);	

					if(supplyChainFinanceResponse.has("d"))
					{
						isInsertionSuccess=true;
					}
					else
					{
						isInsertionSuccess=false;
						JsonObject error=new JsonObject();
						error.addProperty("Message", supplyChainFinanceResponse.get("Message").getAsString());
						error.addProperty("Status", "000002");
						error.addProperty("ErrorCode", "J001");
						response.getWriter().println(error);
					}
				}
			}
			else
			{
				isInsertionSuccess=false;
				JsonObject result = new JsonObject();
				result.addProperty("Status","000002");
				result.addProperty("ErrorCode", "J001");
				result.addProperty("Message",supplyFinanceObj.get("Message").getAsString());
				response.getWriter().println(result);			
			}
		}
		catch(NumberFormatException exception)
		{
			StackTraceElement element[] = exception.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject exceptions = new JsonObject();
			exceptions.addProperty("Status","000002");
			exceptions.addProperty("ErrorCode", "E002");
			exceptions.addProperty("Message", buffer.toString());
			response.getWriter().println(exceptions);

		}
		catch(Exception exception)
		{
			StackTraceElement element[] = exception.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject exception1 = new JsonObject();
			exception1.addProperty("Status","000002");
			exception1.addProperty("ErrorCode", "E002");
			exception1.addProperty("Message", buffer.toString());
			response.getWriter().println(exception1);
		}
		return isInsertionSuccess;
	}

	public void callOnObjectEvent(HttpServletResponse response,JsonObject finalPayload,Destination cpiDestConfig,Properties props, boolean debug){
		JsonObject eventObj=new JsonObject();
		JsonObject onODAccountObj=new JsonObject();
		JsonObject messageObj=new JsonObject();
		try{
			messageObj.addProperty("CorpID", finalPayload.get("CorpID").getAsString());
			// messageObj.addProperty("AggregatorID",
			// finalPayload.get("AggregatorID").getAsString());
			messageObj.addProperty("UserID", finalPayload.get("UserID").getAsString());
			messageObj.addProperty("URN", finalPayload.get("URN").getAsString());
			messageObj.addProperty("DealerName", finalPayload.get("DealerName").getAsString());
			messageObj.addProperty("DealerCode", finalPayload.get("DealerCode").getAsString());
			messageObj.addProperty("AccountNo",
					finalPayload.get("ODAccountNumber").getAsString());
			messageObj.addProperty("Report", "CFMIS");
			messageObj.addProperty("VariantID", "ONDEMAND");

			eventObj.addProperty("Event", "LINK");
			eventObj.addProperty("Object", "ODACCOUNT");
			eventObj.addProperty("AggregatorID",
					finalPayload.get("AggregatorID").getAsString());
			eventObj.add("Message", messageObj);

			onODAccountObj.add("OnObjectEvent", eventObj);

			if (debug)
				response.getWriter().println("Final onODAccountObj: " + onODAccountObj);
			byte[] postDataBytes = onODAccountObj.toString().getBytes("UTF-8");

			String cpiDestUrl = "", cpiUser = "", cpiPass = "", cpiUserPass = "",
					cpiResponse = "";
			cpiDestUrl = cpiDestConfig.get("URL").get().toString();
			cpiUser = cpiDestConfig.get("User").get().toString();
			cpiPass = cpiDestConfig.get("Password").get().toString();
			cpiUserPass = cpiUser + ":" + cpiPass;
			// cpiDestUrl+"OnODAccountEventPublish";
			cpiDestUrl = cpiDestUrl + props.getProperty("OnODAccountEvent");
			URL url = new URL(cpiDestUrl);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			String basicAuth = "Basic "
					+ Base64.getEncoder().encodeToString(cpiUserPass.getBytes());
			con.setRequestProperty("Authorization", basicAuth);
			con.connect();

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(onODAccountObj.toString());
			osw.flush();
			osw.close();
			int responseCode = con.getResponseCode();
			if(debug){
				response.getWriter().println("responseCode:"+responseCode);
			}
			if ((responseCode / 100) == 2 || (responseCode / 100) == 3) {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();

				if (debug)
					response.getWriter().println("sb: " + sb.toString());

				cpiResponse = sb.toString();
				if (debug)
					response.getWriter().println("cpiResponse: " + cpiResponse);
				JSONObject responseJsonObj = new JSONObject(cpiResponse);
				response.getWriter().println(responseJsonObj);
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				if (debug) {
					response.getWriter().println("getErrorStream: " + sb.toString());
				}
				sb.toString();
				if (debug) {
					response.getWriter().println("Response from CPI:" + sb.toString());
				}

				JsonObject result = new JsonObject();
				result.addProperty("Remarks", sb.toString());
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", responseCode);
				result.addProperty("Message", "getting failure  response from the  CPI Service" + cpiDestUrl);
				response.getWriter().println(new Gson().toJson(result));
				if (debug)
					response.getWriter().println("responseJson: " + result);
				// response.getWriter().println(result);
			}
		}catch(Exception ex){

		}
	}
}
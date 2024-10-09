package com.arteriatech.pg;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class SCFRenewal
 */
@WebServlet("/SCFRenewal")
public class SCFRenewal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String SCFLIMIT_DEST_NAME =  "SCFLimit";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SCFRenewal() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		String customerNo="", cpTypeID="", destURL="", errorCode="", errorMsg="", sessionID="", corpID="", aggrID="", wsURL="", renewalDBResponse="", scfOffersResponse="", renewalApplyResponse="";
		boolean isRequestFromCloud=false;
		JsonObject renewalApplyJsonResponse = null;
		JsonObject renewalDBResponseJson = new JsonObject();
		boolean debug = false,isFound=false;
		Destination destConfiguration = null;
		JSONObject scfOfferObj=null;
		try {
			CommonUtils commonUtils = new CommonUtils();
			
			destConfiguration = getDestinationURL(request, response);
			destURL = destConfiguration.get("URL").get().toString();
			if(debug)
				response.getWriter().println("doGet.destURL.: "+ destURL);
			
			if (destURL.contains("service.xsodata")){
				isRequestFromCloud = true;
			}else{
				isRequestFromCloud = false;
			}
			
			String payLoad = request.getParameter("RenewalApply");
			
			if (null != payLoad && payLoad.trim().length() > 0 && payLoad != ""){
				JSONObject jsonObject = new JSONObject(payLoad);
				try {
					if (null != jsonObject.getString("debug")
							&& jsonObject.getString("debug").equalsIgnoreCase("true")) {
						debug = true;
					}
				} catch (JSONException e) {
					if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
						debug = false;
					}
				}
//			debug=true;
				if (debug) {
					response.getWriter().println("doGet.destURL.: "+ destURL);
					response.getWriter().println("isRequestFromCloud.: "+ isRequestFromCloud);
					response.getWriter().println("payLoad: " + payLoad);
					response.getWriter().println("CustomerNo: " + jsonObject.getString("CustomerNo"));
				}

				if (null != jsonObject.getString("CustomerNo")) {
					customerNo = jsonObject.getString("CustomerNo");
				} else {
					errorCode = "E100";
					errorMsg = properties.getProperty(errorCode);
				}
				
				if (null != jsonObject.getString("CPTypeID")) {
					cpTypeID = jsonObject.getString("CPTypeID");
				} else {
					errorCode = "E201";
					errorMsg = properties.getProperty(errorCode);
				}
				
				if (debug){
					response.getWriter().println("customerNo:" + customerNo);
					response.getWriter().println("cpTypeID:" + cpTypeID);
				}
				
				if (errorCode != null & errorCode.trim().length() == 0) {
					
					String loginID = commonUtils.getLoginID(request, response, debug);
					if (loginID != null) {						
						if (debug)
							response.getWriter().println("loginID:" + loginID);
						if (loginID == null) {
							errorCode = "E125";
						} else {
							if(! isRequestFromCloud){
								String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
								if (debug)
									response.getWriter().println("authMethod:" + authMethod);
								if(authMethod.equalsIgnoreCase("BasicAuthentication")){
									String url = commonUtils.getDestinationURL(request, response, "URL");
									if (debug)
										response.getWriter().println("url:" + url);
									sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
									if (debug)
										response.getWriter().println("Generating sessionID:" + sessionID);
									if (sessionID.contains(" ")) {
										errorCode = "S001";
										errorMsg = sessionID;

										if (debug)
											response.getWriter().println("Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
									}
								} else{
									sessionID ="";
								}
							}
						}
					} else {
						errorCode = "E125";
						if (debug)
							response.getWriter().println("Generating sessionID - errorCode:" + errorCode);
					}
					
					if (debug) {
						response.getWriter().println("errorCode: " + errorCode);
						response.getWriter().println("errorMsg: " + errorMsg);
						response.getWriter().println("sessionID: " + sessionID);
					}
					
//					debug = true;
					
					if (errorCode != null & errorCode.trim().length() == 0) {
						if(! isRequestFromCloud){
							if(cpTypeID != null && cpTypeID.equalsIgnoreCase("01"))
								errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);
							else if(cpTypeID != null && cpTypeID.equalsIgnoreCase("60"))
								errorCode = commonUtils.validateVendor(request, response, sessionID, customerNo, debug);
						}else{
							errorCode = "";
						}
						
						if (debug) {
							response.getWriter().println("errorCode: " + errorCode);
							response.getWriter().println("errorMsg: " + errorMsg);
						}
						
						if (errorCode != null & errorCode.trim().length() == 0) {
							if(cpTypeID.equalsIgnoreCase("01")){
								errorCode = commonUtils.readDestProperties("CorpID");
							}else if(cpTypeID.equalsIgnoreCase("60")){
								errorCode = commonUtils.readDestProperties("VendorCorpID");
							}
							if (debug) {
								response.getWriter().println("corpID: " + corpID);
							}
							
							if (errorCode != null && errorCode.trim().length() > 0 && !errorCode.trim().equalsIgnoreCase("E152")) {
								corpID = errorCode;
								String name = SCFLIMIT_DEST_NAME;
								
								// Context ctxDestFact = new InitialContext();
								// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
								// DestinationConfiguration cpiDestConfig = configuration.getConfiguration(name);
								DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
										.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
								Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
										.tryGetDestination(name, options);
								Destination cpiDestConfig = destinationAccessor.get();

								wsURL = cpiDestConfig.get("URL").get().toString();

								if (debug)
									response.getWriter().println("wsURL: " + wsURL);
								
								if(wsURL != null && wsURL.trim().length() > 0){
//									String system = commonUtils.readDestProperties("System");
									String userName="", passWord="", userpass = "";
									
									userName = cpiDestConfig.get("User").get().toString();
									passWord = cpiDestConfig.get("Password").get().toString();
									userpass = userName+":"+passWord;

									if(debug){
										response.getWriter().println("Json TestRun:"+jsonObject.getString("TestRun"));
									}
									
									String testRun="";
									if(null != jsonObject.getString("TestRun")){
										testRun = jsonObject.getString("TestRun");
									}else{
										testRun="";
									}
									
									/*//Temp
									if(testRun.equalsIgnoreCase(""))
										debug=true;
									*/
									//debug=true;
									if(debug){
										response.getWriter().println("Var testRun:"+testRun);
									}
									
									if(userpass != null && (!userpass.trim().equalsIgnoreCase("E153") && !userpass.trim().equalsIgnoreCase("E127"))){
										try {
											if (null != testRun){
												aggrID = commonUtils.readDestProperties("AggrID");
												
												if(aggrID != "E112")
												{
													String formattedStr = "";
													try{
														int number = Integer.parseInt(customerNo);
														formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
														customerNo = formattedStr;
													}catch (NumberFormatException e) {
//														formattedStr = customerNo;
													}
//													customerNo = formattedStr;
													if(debug){
														response.getWriter().println("Formatted Customer: "+customerNo);
													}
													renewalDBResponse = callRenewalDBService(customerNo, aggrID, cpTypeID, wsURL, userpass, response, debug);
													
													if(debug){
														response.getWriter().println("renewalDBResponse: "+renewalDBResponse);
														response.getWriter().println("renewalDBResponse-length: "+renewalDBResponse.length());
														response.getWriter().println("renewalDBResponse-trim: "+renewalDBResponse.trim());
													}
													
													JsonParser parser = new JsonParser();
													renewalDBResponseJson = (JsonObject)parser.parse(renewalDBResponse);
										//			response.getWriter().println("renewalDBResponseJson: " + renewalDBResponseJson);
													if(debug)
														response.getWriter().println("renewalDBResponseJson: "+renewalDBResponseJson);
													
													JsonObject renewalUpdatedDBResponseJson = new JsonObject();
													
													if(isRequestFromCloud){
														if(renewalDBResponseJson.get("ResponseCode").getAsString().equalsIgnoreCase("200")){
															
															if(! renewalDBResponseJson.get("SupplyChainFinancesType").isJsonArray()){
																renewalUpdatedDBResponseJson.addProperty("SupplyChainFinancesType", "");
																renewalUpdatedDBResponseJson.addProperty("Message", "Success");
																renewalUpdatedDBResponseJson.addProperty("ResponseCode", "000002");
																renewalUpdatedDBResponseJson.addProperty("Status", "000001");
																
																if(debug)
																	response.getWriter().println("renewalUpdatedDBResponseJson: "+renewalUpdatedDBResponseJson);
															
																renewalDBResponseJson = renewalUpdatedDBResponseJson;
																if(debug)
																	response.getWriter().println("isRequestFromCloud.finalrenewalDBResponseJson: "+renewalDBResponseJson);
															}else{
																if(debug)
																	response.getWriter().println("SupplyChainFinancesType: "+renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonArray());
																JsonArray scfArray = renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonArray();
																JsonObject scfElement = new JsonObject();
																
																JsonArray updatedSCFArray = new JsonArray();
																JsonObject updatedScfEntry = new JsonObject();
																for(int i=0 ; i<scfArray.size() ; i++){
																	scfElement = scfArray.get(i).getAsJsonObject();
																	if(debug)
																		response.getWriter().println("scfElement: "+scfElement);
																	
																	if(scfElement.get("StatusID").isJsonNull() 
																	|| scfElement.get("StatusID").getAsString().equalsIgnoreCase("000002")
																	|| scfElement.get("StatusID").getAsString().equalsIgnoreCase("000001")
																	|| scfElement.get("StatusID").getAsString().equalsIgnoreCase("")){
																		if(scfElement.get("ApplicationNo").isJsonNull()
																		|| scfElement.get("ApplicationNo").getAsString().startsWith("PAR") 
																		|| scfElement.get("ApplicationNo").getAsString().startsWith("PQR") 
																		|| scfElement.get("ApplicationNo").getAsString().equalsIgnoreCase("")){
//																			renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonArray().get(i).
//																			renewalUpdatedDBResponseJson.
//																			updatedSCFArray.add(scfElement);
																			updatedScfEntry = scfElement;
																			updatedSCFArray.add(scfElement);
																		}
																	}
																}
																
																if(updatedSCFArray.size() > 0){
//																	renewalUpdatedDBResponseJson.add("SupplyChainFinancesType", updatedSCFArray);
																	renewalUpdatedDBResponseJson.add("SupplyChainFinancesType", updatedScfEntry);
																	
																	renewalUpdatedDBResponseJson.addProperty("Message", renewalDBResponseJson.get("Message").getAsString());
																	renewalUpdatedDBResponseJson.addProperty("ResponseCode", renewalDBResponseJson.get("ResponseCode").getAsString());
																	renewalUpdatedDBResponseJson.addProperty("Status", renewalDBResponseJson.get("Status").getAsString());
																}else{
//																	renewalUpdatedDBResponseJson.add("SupplyChainFinancesType", updatedScfEntry);
																	renewalUpdatedDBResponseJson.addProperty("SupplyChainFinancesType", "");
																	renewalUpdatedDBResponseJson.addProperty("Message", "Success");
																	renewalUpdatedDBResponseJson.addProperty("ResponseCode", "000002");
																	renewalUpdatedDBResponseJson.addProperty("Status", "000001");
																}
																if(debug)
																	response.getWriter().println("renewalUpdatedDBResponseJson: "+renewalUpdatedDBResponseJson);
															
																renewalDBResponseJson = renewalUpdatedDBResponseJson;
																if(debug)
																	response.getWriter().println("isRequestFromCloud.finalrenewalDBResponseJson: "+renewalDBResponseJson);
															}
														}
													}else{
														renewalUpdatedDBResponseJson.addProperty("Message", renewalDBResponseJson.get("Message").getAsString());
														renewalUpdatedDBResponseJson.addProperty("ResponseCode", renewalDBResponseJson.get("ResponseCode").getAsString());
														renewalUpdatedDBResponseJson.addProperty("Status", renewalDBResponseJson.get("Status").getAsString());
														
														if(! renewalDBResponseJson.get("SupplyChainFinancesType").isJsonArray()){
															renewalUpdatedDBResponseJson.addProperty("SupplyChainFinancesType", "");
															
															if(debug)
																response.getWriter().println("renewalUpdatedDBResponseJson: "+renewalUpdatedDBResponseJson);
														
															renewalDBResponseJson = renewalUpdatedDBResponseJson;
															if(debug)
																response.getWriter().println("if !isRequestFromCloud.finalrenewalDBResponseJson: "+renewalDBResponseJson);
														}else{
															if(debug)
																response.getWriter().println("SupplyChainFinancesType: "+renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonArray());
															JsonArray scfArray = renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonArray();
//															JsonObject scfElement = new JsonObject();
															
//															JsonArray updatedSCFArray = new JsonArray();
															JsonObject updatedScfEntry = new JsonObject();
															for(int i=0 ; i<scfArray.size() ; i++){
																updatedScfEntry = scfArray.get(i).getAsJsonObject();
															}
															renewalUpdatedDBResponseJson.add("SupplyChainFinancesType", updatedScfEntry);
															
															if(debug)
																response.getWriter().println("renewalUpdatedDBResponseJson: "+renewalUpdatedDBResponseJson);
														
															renewalDBResponseJson = renewalUpdatedDBResponseJson;
															if(debug)
																response.getWriter().println("else !isRequestFromCloud.finalrenewalDBResponseJson: "+renewalDBResponseJson);
														}
													}
													
													if(renewalDBResponseJson.get("Status").getAsString().equalsIgnoreCase("000001") && renewalDBResponseJson.get("ResponseCode").getAsString().equalsIgnoreCase("200")){
														if(renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonObject().get("StatusID").getAsString().equalsIgnoreCase("000001")){
															JsonObject result = new JsonObject();
															errorCode = "E188";
															if(renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonObject().get("CallBackStatus").getAsString() != null
																&& renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonObject().get("CallBackStatus").getAsString().trim().length() > 0){
//																errorMsg = "Your application is in process. Please contact your RM to know more"; commented on 24-11-20
																errorCode = "E195";
																errorMsg = properties.getProperty(errorCode);
															}else{
																errorMsg = properties.getProperty(errorCode);
															}
															result.addProperty("errorCode", errorCode);
															result.addProperty("Message", errorMsg);
															result.addProperty("Status", properties.getProperty("ErrorStatus"));
															result.addProperty("Valid", "false");
															response.getWriter().println(new Gson().toJson(result));
														}else {
															String msgDate="";
															String pattern = "dd/MM/yyyy";
															String eComplDate = renewalDBResponseJson.get("SupplyChainFinancesType").getAsJsonObject().get("ECompleteDate").getAsString();
															
															if(debug)
																response.getWriter().println("eComplDate: "+eComplDate);
															
															Date date1=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(eComplDate);
															
															if(debug)
																response.getWriter().println("date1: "+date1);
															
															DateFormat df = new SimpleDateFormat(pattern);
														    msgDate = df.format(date1);

														    if(debug)
																response.getWriter().println("msgDate: "+msgDate);
														    //1. Call Renewal offfer api here callSCFOffersService
														    //2. If Address5 field equals ignore case HULFDB, then print the response object
														    //3. Else print the below response with error code E159
														    scfOffersResponse = callSCFOffersService(customerNo, corpID, cpTypeID, wsURL, userpass, response, debug);
														    if(debug)
																response.getWriter().println("scfOffersResponse: "+scfOffersResponse);
														    if(scfOffersResponse!=null &&!scfOffersResponse.equalsIgnoreCase("")&& scfOffersResponse.trim().length()>0){
														    	scfOfferObj = new JSONObject(scfOffersResponse.toString());
														    	if(scfOfferObj!=null && scfOfferObj.has("Address5") && !scfOfferObj.isNull("Address5")&& scfOfferObj.getString("Address5").equalsIgnoreCase("HULFDB")){
														    		isFound=true;
														    	}else if(cpTypeID.equalsIgnoreCase("60")){
														    		isFound=true;
														    	}
														    }/*else{
														    	 if(debug)
														    	 {
														    		 response.getWriter().println("cpTypeID: "+cpTypeID); 
														    	 }
														    	
														    }*/
														    if(debug){
														    	response.getWriter().println("isFound: "+isFound);
														    }
														    if(isFound){
														    	if(testRun.equalsIgnoreCase("X")){
														    		response.getWriter().println(scfOffersResponse);
														    	}else{
														    		if (scfOffersResponse != null && scfOffersResponse.trim().length() > 0) {
														    			if (null != testRun && testRun.equalsIgnoreCase("")){
																			if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																					&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																				renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																				if(debug)
																					response.getWriter().println("PA Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																				/*if(debug)
																					response.getWriter().println("Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																				if(renewalApplyJsonResponse.get("Status").getAsString().equalsIgnoreCase("100000")){
																					response.getWriter().println(new Gson().toJson(renewalApplyJsonResponse));
																				}*/
																			} else if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																					&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																				//This Condition is added on 28-11-20 for allowing PQ case for renenwal
																				renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																				if(debug)
																					response.getWriter().println("PQ Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																				/*errorCode = "E154";
																				errorMsg = properties.getProperty(errorCode);

																				JsonObject result = new JsonObject();
																				result.addProperty("errorCode", errorCode);
																				result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																				result.addProperty("Status", properties.getProperty("ErrorStatus"));
																				result.addProperty("Valid", "false");
																				response.getWriter().println(new Gson().toJson(result));*/
																			}
																			else {
																				errorCode = "E154";
																				errorMsg = properties.getProperty(errorCode);

																				JsonObject result = new JsonObject();
																				result.addProperty("errorCode", errorCode);
																				result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																				result.addProperty("Status", properties.getProperty("ErrorStatus"));
																				result.addProperty("Valid", "false");
																				response.getWriter().println(new Gson().toJson(result));
																			}
																		} else {
																			errorCode = "E158";
																			errorMsg = properties.getProperty(errorCode);

																			JsonObject result = new JsonObject();
																			result.addProperty("errorCode", errorCode);
																			result.addProperty("Message", errorMsg);
																			result.addProperty("Status", properties.getProperty("ErrorStatus"));
																			result.addProperty("Valid", "false");
																			response.getWriter().println(new Gson().toJson(result));
																		}
														    		}
														    	}
														    	
														    }else{
														    	//Same typeset as in UI
														    	int validateDays=90;
														    	int diffDays=0;
														    	String pcgUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
																String pcgUser = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
																String pcgPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
																String aggrId=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
																String pcgUserPass = pcgUser + ":" + pcgPassword;
																String configUrl=pcgUrl+"ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" +"AGGRICICI"
																		+ "%27%20and%20Typeset%20eq%20%27" + "PYRNDY" + "%27%20and%20Types%20eq%20%27"+aggrId+"%27";
																JsonObject configTypesetObj = commonUtils.executeURL(configUrl, pcgUserPass, response);
																if(debug){
																	response.getWriter().println("configTypesetObj:"+configTypesetObj);
																}
																
																if(!configTypesetObj.has("error")&&configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
																	JsonObject configObj = configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
																	if(!configObj.get("TypeValue").isJsonNull()&&!configObj.get("TypeValue").getAsString().equalsIgnoreCase("")){
																		validateDays=configObj.get("TypeValue").getAsInt();
																	}
																}
																
																if(debug){
																	response.getWriter().println("validateDays:"+validateDays);
																}
																
																Date eCompletionDate = new Date();
																eCompletionDate.setTime(date1.getTime());
														        Date currentDate = new Date(System.currentTimeMillis());
														        
														        if(debug)
																	response.getWriter().println("currentDate in server: "+currentDate);
														        
														        currentDate = commonUtils.dateinTimeZone(currentDate, "dd MMM yyyy hh:mm:ss a", "IST");
										
														        if(debug){
																	response.getWriter().println("currentDate in IST: "+currentDate);
																		response.getWriter().println("timestamp: "+eCompletionDate);
																		response.getWriter().println("eCompletionDate: "+eCompletionDate);
														        }
																	
																diffDays = commonUtils.dateDifference(currentDate, eCompletionDate);
																if(debug)
																	response.getWriter().println("diffDays: "+diffDays);
																
																String isRenewal="", renewalDate="";
														        
														    	if(diffDays >= validateDays){
															    	if(testRun.equalsIgnoreCase("X")){
															    		response.getWriter().println(scfOffersResponse);
															    	}else{
																		
															    		if (scfOffersResponse != null && scfOffersResponse.trim().length() > 0) {
															    			if (null != testRun && testRun.equalsIgnoreCase("")){
															    				if(null != scfOfferObj.getString("IsRenewal") && scfOfferObj.getString("IsRenewal").trim().length() > 0)
															    					isRenewal = scfOfferObj.getString("IsRenewal");
															    				else
															    					isRenewal = "";
															    				
															    				if(null != scfOfferObj.getString("RenewalDate") && scfOfferObj.getString("RenewalDate").trim().length() > 0)
															    					renewalDate = scfOfferObj.getString("RenewalDate");
															    				else
															    					renewalDate = "Not found";
															    				if(debug)
														    						response.getWriter().println("renewalDate: "+renewalDate);
															    				
															    				if(! renewalDate.equalsIgnoreCase("Not found")){
															    					Date currentDateForRenew = new Date(System.currentTimeMillis());
															    					currentDateForRenew = commonUtils.dateinTimeZone(currentDateForRenew, "dd MMM yyyy hh:mm:ss a", "IST");
															    					if(debug)
															    						response.getWriter().println("currentDateForRenew: "+currentDateForRenew);
															    					
															    					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
															    					Date renewalDateInFormat = formatter.parse(renewalDate);
															    					renewalDateInFormat = commonUtils.dateinTimeZone(renewalDateInFormat, "dd MMM yyyy hh:mm:ss a", "IST");
															    					
															    					int diffDaysForRenew = commonUtils.dateDifference(renewalDateInFormat, currentDateForRenew);
															    					if(debug)
															    						response.getWriter().println("diffDaysForRenew: "+diffDaysForRenew);
															    					
															    					switch(isRenewal){
																	    				case "E":
	//																    					System.out.println("Allowed for enhancement");
																	    					if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																								renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																								if(debug)
																									response.getWriter().println("PA Enhancement Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																								
																							} else if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																								renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																								if(debug)
																									response.getWriter().println("PQ Enhancement Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																							}
																	    					
																	    				break;
																	    				
																	    				case "Y":
																	    					if(diffDaysForRenew > 60){
//																	    						System.out.println("Not allowed for renewal");
																	    						errorCode = "E202";
																								errorMsg = properties.getProperty(errorCode);

																								JsonObject result = new JsonObject();
																								result.addProperty("errorCode", errorCode);
																								result.addProperty("Message", errorMsg+renewalDate);
																								result.addProperty("Status", properties.getProperty("ErrorStatus"));
																								result.addProperty("Valid", "false");
																								response.getWriter().println(new Gson().toJson(result));
																	    					}else{
//																	    						System.out.println("Allowed for renewal");
																	    						if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																									renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																									if(debug)
																										response.getWriter().println("PA Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																									
																								} else if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																									renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																									if(debug)
																										response.getWriter().println("PQ Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																								}
																	    					}
																	    						
																	    				break;
																	    				
																	    				default:
//																	    					System.out.println("Not allowed for renewal/enhancement");
																	    					errorCode = "E154";
																							errorMsg = properties.getProperty(errorCode);

																							JsonObject result = new JsonObject();
																							result.addProperty("errorCode", errorCode);
																							result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																							result.addProperty("Status", properties.getProperty("ErrorStatus"));
																							result.addProperty("Valid", "false");
																							response.getWriter().println(new Gson().toJson(result));
																	    				break;
															    					}
															    				}
															    				
																				/*Existing logic commented for fixing 90 days renewal issue
																				 * if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																						&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																					renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																					if(debug)
																						response.getWriter().println("PA Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																					
																				} else if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																						&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																					renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																					if(debug)
																						response.getWriter().println("PQ Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																					
																				}
																				else {
																					errorCode = "E154";
																					errorMsg = properties.getProperty(errorCode);

																					JsonObject result = new JsonObject();
																					result.addProperty("errorCode", errorCode);
																					result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																					result.addProperty("Status", properties.getProperty("ErrorStatus"));
																					result.addProperty("Valid", "false");
																					response.getWriter().println(new Gson().toJson(result));
																				}*/
																			} else {
																				errorCode = "E158";
																				errorMsg = properties.getProperty(errorCode);
																				JsonObject result = new JsonObject();
																				result.addProperty("errorCode", errorCode);
																				result.addProperty("Message", errorMsg);
																				result.addProperty("Status", properties.getProperty("ErrorStatus"));
																				result.addProperty("Valid", "false");
																				response.getWriter().println(new Gson().toJson(result));
																			}
															    		}
															    	}
																} else {
																	
																	JsonObject result = new JsonObject();
																	errorCode = "E159";
																	errorMsg = properties.getProperty(errorCode);
																	result.addProperty("errorCode", errorCode);
																	// result.addProperty("Message",
																	// errorMsg);
																	result.addProperty("Message",
																			"You have already availed this facility on "
																					+ msgDate
																					+ ". Please contact your RM to know when you can re-apply for the same");
																	result.addProperty("Status",
																			properties.getProperty("ErrorStatus"));
																	result.addProperty("Valid", "false");
																	response.getWriter()
																			.println(new Gson().toJson(result));
																}
														    }	
														}
													}else if(renewalDBResponseJson.get("Status").getAsString().equalsIgnoreCase("000001")
													&& renewalDBResponseJson.get("ResponseCode").getAsString().equalsIgnoreCase("000002")){
//														if(debug)
//															response.getWriter().println("renewalDBResponse: "+renewalDBResponse);
													
//														JSONObject renewalDBObj1 = new JSONObject(renewalDBResponse);
														scfOffersResponse = callSCFOffersService(customerNo, corpID, cpTypeID, wsURL, userpass, response, debug);
													
														if(debug)
															response.getWriter().println("scfOffersResponse: "+scfOffersResponse);
														
														if (scfOffersResponse != null && scfOffersResponse.trim().length() > 0) {
															 scfOfferObj = new JSONObject(scfOffersResponse.toString());
															if (null != testRun && testRun.equalsIgnoreCase("X")){
																response.getWriter().println(scfOffersResponse);
															} else if (null != testRun && testRun.equalsIgnoreCase("")){
																
																String isRenewal="", renewalDate="";
																
																if(null != scfOfferObj.getString("IsRenewal") && scfOfferObj.getString("IsRenewal").trim().length() > 0)
											    					isRenewal = scfOfferObj.getString("IsRenewal");
											    				else
											    					isRenewal = "";
											    				
											    				if(null != scfOfferObj.getString("RenewalDate") && scfOfferObj.getString("RenewalDate").trim().length() > 0)
											    					renewalDate = scfOfferObj.getString("RenewalDate");
											    				else
											    					renewalDate = "Not found";
											    				if(debug)
										    						response.getWriter().println("renewalDate: "+renewalDate);
											    				
											    				if(! renewalDate.equalsIgnoreCase("Not found")){
											    					Date currentDateForRenew = new Date(System.currentTimeMillis());
											    					currentDateForRenew = commonUtils.dateinTimeZone(currentDateForRenew, "dd MMM yyyy hh:mm:ss a", "IST");
											    					if(debug)
											    						response.getWriter().println("currentDateForRenew: "+currentDateForRenew);
											    					
											    					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
											    					Date renewalDateInFormat = formatter.parse(renewalDate);
											    					renewalDateInFormat = commonUtils.dateinTimeZone(renewalDateInFormat, "dd MMM yyyy hh:mm:ss a", "IST");
											    					
											    					int diffDaysForRenew = commonUtils.dateDifference(renewalDateInFormat, currentDateForRenew);
											    					if(debug)
											    						response.getWriter().println("diffDaysForRenew: "+diffDaysForRenew);
											    					
											    					switch(isRenewal){
													    				case "E":
	//																    					System.out.println("Allowed for enhancement");
													    					if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																				renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																				if(debug)
																					response.getWriter().println("PA Enhancement Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																				
																			} else if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																				renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																				if(debug)
																					response.getWriter().println("PQ Enhancement Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																			}
													    					
													    				break;
													    				
													    				case "Y":
													    					if(diffDaysForRenew > 60){
	//												    						System.out.println("Not allowed for renewal");
													    						errorCode = "E202";
																				errorMsg = properties.getProperty(errorCode);
	
																				JsonObject result = new JsonObject();
																				result.addProperty("errorCode", errorCode);
																				result.addProperty("Message", errorMsg+renewalDate);
																				result.addProperty("Status", properties.getProperty("ErrorStatus"));
																				result.addProperty("Valid", "false");
																				response.getWriter().println(new Gson().toJson(result));
													    					}else{
	//												    						System.out.println("Allowed for renewal");
													    						if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																					renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																					if(debug)
																						response.getWriter().println("PA Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																					
																				} else if(scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																					renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																					if(debug)
																						response.getWriter().println("PQ Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																				}
													    					}
													    						
													    				break;
													    				
													    				default:
	//												    					System.out.println("Not allowed for renewal/enhancement");
													    					errorCode = "E154";
																			errorMsg = properties.getProperty(errorCode);
	
																			JsonObject result = new JsonObject();
																			result.addProperty("errorCode", errorCode);
																			result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																			result.addProperty("Status", properties.getProperty("ErrorStatus"));
																			result.addProperty("Valid", "false");
																			response.getWriter().println(new Gson().toJson(result));
													    				break;
											    					}
											    					
											    				}
																
																/*Existing logic commented for fixing 90 days renewal issue
																 * if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																		&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000002")) {
																	renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																	if(debug)
																		response.getWriter().println("PA Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																} else if(scfOfferObj.getString("IsRenewal").equalsIgnoreCase("Y") 
																		&& scfOfferObj.getString("EligibilityStatus").equalsIgnoreCase("000001")){
																	renewalApplyJsonResponse = callRenewalApplyService(customerNo, jsonObject, aggrID, cpTypeID, wsURL, userpass, response, debug);
																	if(debug)
																		response.getWriter().println("PQ Renewal Apply Response: "+new Gson().toJson(renewalApplyJsonResponse));
																}
																else {
																	errorCode = "E154";
																	errorMsg = properties.getProperty(errorCode);

																	JsonObject result = new JsonObject();
																	result.addProperty("errorCode", errorCode);
																	result.addProperty("Message", errorMsg+". Your eligibility status is: "+scfOfferObj.getString("EligibilityStatus"));
																	result.addProperty("Status", properties.getProperty("ErrorStatus"));
																	result.addProperty("Valid", "false");
																	response.getWriter().println(new Gson().toJson(result));
																}*/
															} else {
																errorCode = "E158";
																errorMsg = properties.getProperty(errorCode);

																JsonObject result = new JsonObject();
																result.addProperty("errorCode", errorCode);
																result.addProperty("Message", errorMsg);
																result.addProperty("Status", properties.getProperty("ErrorStatus"));
																result.addProperty("Valid", "false");
																response.getWriter().println(new Gson().toJson(result));
															}
														} else {
															errorCode = "E107";
															errorMsg = properties.getProperty(errorCode);

															JsonObject result = new JsonObject();
															result.addProperty("errorCode", errorCode);
															result.addProperty("Message", errorMsg);
															result.addProperty("Status", properties.getProperty("ErrorStatus"));
															result.addProperty("Valid", "false");
															response.getWriter().println(new Gson().toJson(result));
														}
													}else {
														errorCode = "E189";
														errorMsg = properties.getProperty(errorCode);

														JsonObject result = new JsonObject();
														result.addProperty("errorCode", errorCode);
														result.addProperty("Message", errorMsg);
														result.addProperty("Status", properties.getProperty("ErrorStatus"));
														result.addProperty("Valid", "false");
														response.getWriter().println(new Gson().toJson(result));
													}
												}else{
													JsonObject result = new JsonObject();
													errorCode = "E112";
													errorMsg = properties.getProperty(errorCode);
													
													result.addProperty("errorCode", errorCode);
													result.addProperty("Message", errorMsg);
													result.addProperty("Status", properties.getProperty("ErrorStatus"));
													result.addProperty("Valid", "false");
													response.getWriter().println(new Gson().toJson(result));
												}
											}
										} catch (JSONException e) {
											JsonObject result = new JsonObject();
											errorCode = "E157";
											errorMsg = properties.getProperty(errorCode);
											
											result.addProperty("errorCode", errorCode);
//											result.addProperty("Message", errorMsg);
											result.addProperty("Message", e.getLocalizedMessage());
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
											
											if(debug){
												StackTraceElement element[] = e.getStackTrace();
												StringBuffer buffer = new StringBuffer();
												for(int i=0;i<element.length;i++)
												{
													buffer.append(element[i]);
												}
												response.getWriter().println("SCFRenewal.Full Stack Trace: "+buffer.toString());
											}
										} catch (Exception e) {
											JsonObject result = new JsonObject();
											result.addProperty("Exception", e.getClass().getCanonicalName());
											result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
											
											if(debug){
												StackTraceElement element[] = e.getStackTrace();
												StringBuffer buffer = new StringBuffer();
												for(int i=0;i<element.length;i++)
												{
													buffer.append(element[i]);
												}
												response.getWriter().println("SCFRenewal.Full Stack Trace: "+buffer.toString());
											}
										}
									} else {
										JsonObject result = new JsonObject();
										errorCode = userpass;
										errorMsg = properties.getProperty(errorCode);
										
										result.addProperty("errorCode", errorCode);
										result.addProperty("Message", errorMsg);
										result.addProperty("Status", properties.getProperty("ErrorStatus"));
										result.addProperty("Valid", "false");
										response.getWriter().println(new Gson().toJson(result));
									}
								} else {
									JsonObject result = new JsonObject();
									errorCode = "E156";
									errorMsg = properties.getProperty(errorCode);
									
									result.addProperty("errorCode", errorCode);
									result.addProperty("Message", errorMsg);
									result.addProperty("Status", properties.getProperty("ErrorStatus"));
									result.addProperty("Valid", "false");
									response.getWriter().println(new Gson().toJson(result));
								}
							}else{
								JsonObject result = new JsonObject();
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							}
						}
						else{
							JsonObject result = new JsonObject();
							result.addProperty("errorCode", errorCode);
							result.addProperty("Message", properties.getProperty(errorCode));
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						}
					}
					else{
						JsonObject result = new JsonObject();
						result.addProperty("errorCode", errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().println(new Gson().toJson(result));
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
			} else {
				JsonObject result = new JsonObject();
				errorCode = "E108";
				errorMsg = properties.getProperty(errorCode);
				
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
//			debug=true;
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeURL.Full Stack Trace: "+buffer.toString());
			}
		}
	}
	
	public String callRenewalDBService(String customerNo, String aggrID, String cpTypeID, String wsURL, String userpass, HttpServletResponse response, boolean debug){
		String cpiResponse="";
		
		try {
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			JSONObject inputJson = new JSONObject();
			JSONObject root = new JSONObject();
		
			inputJson.put("CPGuid", customerNo);
			inputJson.put("AggregatorId", aggrID);
			inputJson.put("CPTypeID", cpTypeID);
			root.put("Root", inputJson);
			
			byte[] postDataBytes = root.toString().getBytes("UTF-8");
			wsURL = wsURL + "/" + properties.getProperty("RenewalDBScenario");
			
			if (debug) {
				response.getWriter().println("wsURL-RenewalDBScenario: " + wsURL);
				response.getWriter().println("SupplyChainFinances Input Payload: "+root);
			}

			URL url = new URL(wsURL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);

			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestProperty("Authorization", basicAuth);
			con.connect();

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(root.toString());
			osw.flush();
			osw.close();

			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), "utf-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			if (debug)
				response.getWriter().println("sb: " + sb.toString());

			cpiResponse = sb.toString();
			return cpiResponse;
		} catch (JSONException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		} catch (UnsupportedEncodingException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		} catch (IOException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		}
	}
	
	public String callSCFOffersService(String customerNo, String corpID, String cpTypeID, String wsURL, String userpass, HttpServletResponse response, boolean debug){
		String cpiResponse = "";
		
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			JSONObject inputJson = new JSONObject();
			JSONObject root = new JSONObject();
		
			inputJson.put("DealerId", customerNo);
			inputJson.put("CorpId", corpID);
			inputJson.put("CPTypeID", cpTypeID);
			root.put("Root", inputJson);
			
			byte[] postDataBytes = root.toString().getBytes("UTF-8");
			wsURL = wsURL + "/" + properties.getProperty("SCFOfferScenario");
			
			if (debug) {
				response.getWriter().println("wsURL-SCFOfferScenario: " + wsURL);
			}

			URL url = new URL(wsURL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);

			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestProperty("Authorization", basicAuth);
			con.connect();

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(root.toString());
			osw.flush();
			osw.close();

			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), "utf-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			if (debug)
				response.getWriter().println("sb: " + sb.toString());

			cpiResponse = sb.toString();
			return cpiResponse;
		} catch (JSONException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		} catch (UnsupportedEncodingException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		} catch (IOException e) {
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			return cpiResponse;
		}
	}
	
	public JsonObject callRenewalApplyService(String customerNo, JSONObject jsonObject, String aggrID, String cpType, String wsURL, String userpass, HttpServletResponse response, boolean debug) throws IOException{
		String cpiResponse="", constitutionType="", errorCode="", errorMsg="", corporateName="", odAccount="", ipAddress="", currentDate1="",
				currentTime="", partnerAccount="", dateOfIncorporation="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try
		{
			CommonUtils commonUtils = new CommonUtils();
			
			jsonObject.put("AggregatorId", aggrID);
			jsonObject.remove("CustomerNo");
			jsonObject.put("CustomerNo",customerNo);
			jsonObject.put("CPTypeID", cpType);
			
			if (debug) {
				response.getWriter().println("callRenewalApplyService jsonObject: " + jsonObject);
				response.getWriter().println("callRenewalApplyService ConstitutionType: " + jsonObject.getString("ConstitutionType"));
				response.getWriter().println("callRenewalApplyService ODAccountNumber: " + jsonObject.getString("ODAccountNumber"));
				response.getWriter().println("callRenewalApplyService CorporateName: " + jsonObject.getString("CorporateName"));
				response.getWriter().println("callRenewalApplyService IPAddress: " + jsonObject.getString("IPAddress"));
				response.getWriter().println("callRenewalApplyService PartnerAccount: " + jsonObject.getString("PartnerAccount"));
				response.getWriter().println("callRenewalApplyService DateOfIncorporation: " + jsonObject.getString("DateOfIncorporation"));
				response.getWriter().println("callRenewalApplyService AggregatorId: " + jsonObject.getString("AggregatorId"));
			}

			constitutionType = jsonObject.getString("ConstitutionType");
			if (debug) {
				response.getWriter().println("constitutionType var: "+constitutionType);
			}
			
			if (constitutionType != null && !(constitutionType.equalsIgnoreCase(properties.getProperty("ProprietorConstitutionType"))
					|| constitutionType.equalsIgnoreCase(properties.getProperty("PartnershipConstitutionType"))
					|| constitutionType.equalsIgnoreCase(properties.getProperty("CompaniesConstitutionType")))) {
				
				if (debug) {
					response.getWriter().println("constitutionType not equal to 03 or 04: "+constitutionType);
				}
				
				errorCode = "E142";
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				return result;
			} else {
				if (debug) {
					response.getWriter().println("constitutionType equal to 03 or 04: "+constitutionType);
				}
				
				corporateName = jsonObject.getString("CorporateName");
				if (corporateName != null && corporateName.trim().length() == 0
						&& corporateName.trim().equalsIgnoreCase("")) {
					errorCode = "E144";
				}
				if (debug) {
					response.getWriter().println("errorCode1: "+errorCode);
				}
				
				if (errorCode != null && errorCode.trim().length() == 0
						&& errorCode.trim().equalsIgnoreCase("")) {
					odAccount = jsonObject.getString("ODAccountNumber");
					if (odAccount != null && odAccount.trim().length() == 0
							&& odAccount.trim().equalsIgnoreCase("")) {
						errorCode = "E155";
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
					return result;
				}
				if (debug) {
					response.getWriter().println("errorCode2: "+errorCode);
				}
				
				if (errorCode != null && errorCode.trim().length() == 0
						&& errorCode.trim().equalsIgnoreCase("")) {
					ipAddress = jsonObject.getString("IPAddress");
					if (ipAddress != null && ipAddress.trim().length() == 0
							&& ipAddress.trim().equalsIgnoreCase("")) {
						errorCode = "E145";
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
					return result;
				}
				if (debug) {
					response.getWriter().println("errorCode3: "+errorCode);
				}
				
				if (errorCode != null && errorCode.trim().length() == 0
						&& errorCode.trim().equalsIgnoreCase("")) {
					partnerAccount = jsonObject.getString("PartnerAccount");
					if (partnerAccount != null && partnerAccount.trim().length() == 0
							&& partnerAccount.trim().equalsIgnoreCase("")) {
						errorCode = "E147";
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
					return result;
				}
				if (debug) {
					response.getWriter().println("errorCode4: "+errorCode);
				}
				
				if (errorCode != null && errorCode.trim().length() == 0
						&& errorCode.trim().equalsIgnoreCase("")) {
					dateOfIncorporation = jsonObject.getString("DateOfIncorporation");
					if (dateOfIncorporation != null && dateOfIncorporation.trim().length() == 0
							&& dateOfIncorporation.trim().equalsIgnoreCase("")) {
						errorCode = "E148";
					} else {
						errorCode = commonUtils.validateInput(response, "DateOfIncorporation", dateOfIncorporation, debug);
					}
				} else {
					errorMsg = properties.getProperty(errorCode);
					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
					return result;
				}
				
				if (debug) {
					response.getWriter().println("errorCode5: "+errorCode);
				}
				
				if (errorCode != null && errorCode.trim().length() == 0
						&& errorCode.trim().equalsIgnoreCase("")) {
					JSONArray arrayPartnerList = (JSONArray) jsonObject.getJSONArray("PartnerList");
					if (debug) {
						response.getWriter().println("arrayPartnerList: " + arrayPartnerList);
						response.getWriter().println("arrayPartnerList-length: " + arrayPartnerList.length());
					}
					
					String key = "", value="", childKey = "", childValue = "";
					for (int i = 0; i <= arrayPartnerList.length() - 1; i++) {
						JSONObject partnerListJsonObj = (JSONObject) arrayPartnerList.get(i);

						if (debug)
							response.getWriter().println("partnerListJsonObj---names: " + partnerListJsonObj.names().length());

						for (int j = 0; j < partnerListJsonObj.names().length(); j++) {
							key = partnerListJsonObj.names().get(j).toString();
							value = partnerListJsonObj.getString(key);
							if (debug)
								response.getWriter().println("Partner "+i+": "+key+"----"+value);
							
							errorCode = commonUtils.validateInput(response, key, value, debug);

							if (errorCode != null && errorCode.trim().length() > 0) {
								break;
							}
						}
					}
					
					if (errorCode != null && errorCode.equalsIgnoreCase("")) {
						if (errorCode != null && errorCode.equalsIgnoreCase("")) {
							if (debug) {
								response.getWriter().println("userHistory: " + aggrID);
								response.getWriter().println("wsURL" + wsURL);
								response.getWriter().println("userpass" + userpass);
							}
							JSONObject root = new JSONObject();
							
							root.put("RenewalApply", jsonObject);
							
							if(debug)
								response.getWriter().println(root);
							
							wsURL = wsURL + "/" + properties.getProperty("RenewalApplyScenario");
							if (debug)
								response.getWriter().println("wsURL" + wsURL);
							
							byte[] postDataBytes = root.toString().getBytes("UTF-8");

							URL url = new URL(wsURL);
							HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

							con.setRequestMethod("POST");
							con.setRequestProperty("Content-Type", "application/json");
							con.setRequestProperty("charset", "utf-8");
							con.setRequestProperty("Content-Length",
									String.valueOf(postDataBytes.length));
							con.setRequestProperty("Accept", "application/json");
							con.setDoOutput(true);
							con.setDoInput(true);

							String basicAuth = "Basic "
									+ Base64.getEncoder().encodeToString(userpass.getBytes());
							con.setRequestProperty("Authorization", basicAuth);
							con.connect();

							OutputStream os = con.getOutputStream();
							OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
							osw.write(root.toString());
							osw.flush();
							osw.close();

							StringBuffer sb = new StringBuffer();
							BufferedReader br = new BufferedReader(
									new InputStreamReader(con.getInputStream(), "utf-8"));
							String line = null;
							while ((line = br.readLine()) != null) {
								sb.append(line + "\n");
							}
							br.close();

							if (debug)
								response.getWriter().println("sb: " + sb.toString());

							cpiResponse = sb.toString();
							if (cpiResponse != null && cpiResponse.trim().length() > 0) {
								response.getWriter().println(cpiResponse);
								
								JsonObject result = new JsonObject();
								
								return result;
							} else {
								errorCode = "E107";
								errorMsg = properties.getProperty(errorCode);

								JsonObject result = new JsonObject();
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								
								response.getWriter().print(new Gson().toJson(result));
								return result;
							}
						} else {
							errorMsg = properties.getProperty(errorCode);

							JsonObject result = new JsonObject();
							result.addProperty("errorCode", errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().print(new Gson().toJson(result));
							return result;
						}
					} else {
						errorMsg = properties.getProperty(errorCode);

						JsonObject result = new JsonObject();
						result.addProperty("errorCode", errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
						return result;
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
					return result;
				}
			}
		} catch (Exception e) {
			errorCode="Exception";
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", cpiResponse);
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().print(new Gson().toJson(result));
			return result;
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//				LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = PCGW_UTIL_DEST_NAME;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
			// destConfiguration = configuration.getConfiguration(destinationName);
//					LOGGER.info("5. destination configuration object created");	
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
//					LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}
}

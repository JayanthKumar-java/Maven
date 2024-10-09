package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import com.google.gson.JsonElement;
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
/**
 * Servlet implementation class BPRenewal
 */
@WebServlet("/BPRenewal")
public class BPRenewal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String SCFLIMIT_DEST_NAME =  "SCFLimit";
//	String servletPath="";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BPRenewal() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		String customerNo="", cpType="", statusID="", reqSource="", bpGuid="", errorCode="", errorMsg="", sessionID="", corpID="", aggrID="", wsURL="", bpRenewalResponse="", scfOffersResponse="", renewalApplyResponse="";
		String renewalListScenario="";
		JsonObject bpRenewalJsonResponse = null;
		Destination destConfiguration = null;
		boolean debug = false, isRequestFromCloud=false;
		boolean hasBpApplied = false;
//		servletPath = request.getServletPath();
		String status = "", destURL="";
		JSONObject bpRenewalJson = new JSONObject();
		String payLoad = "";
		/*if(servletPath.equalsIgnoreCase("/BPDeclarations")){
			bpDeclarations(request, response, properties);
		}else{*/
			payLoad = request.getParameter("BPRenewal");
			JSONObject jsonObject = new JSONObject();
			try {
				bpRenewalJson = new JSONObject(payLoad);
				if (null != bpRenewalJson.getString("debug")
						&& bpRenewalJson.getString("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
			} catch (JSONException e) {
				if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
					debug = false;
				}
			}
			//debug=true; // delete this line afetr Tseting
			try {
				bpRenewalJson = new JSONObject(payLoad);
				CommonUtils commonUtils = new CommonUtils();
				
				//Changes done for Multi Renewal - Starts here
				destConfiguration = getDestinationURL(request, response);
				destURL = destConfiguration.get("URL").get().toString();
				if(debug)
					response.getWriter().println("doGet.destURL.: "+ destURL);
				
				if (destURL.contains("service.xsodata"))
				{
					isRequestFromCloud = true;
				} 
				else {
					isRequestFromCloud = false;
				}
				
				if(debug)
					response.getWriter().println("isRequestFromCloud: "+ isRequestFromCloud);
				
				//Changes done for Multi Renewal - Ends here
				
				if (null != payLoad && payLoad.trim().length() > 0 && payLoad != ""){
//					JSONObject jsonObject = new JSONObject(payLoad);
					try {
						if (null != bpRenewalJson.getString("debug")
								&& bpRenewalJson.getString("debug").equalsIgnoreCase("true")) {
							debug = true;
						}
					} catch (JSONException e) {
						if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
							debug = false;
//							debug = true;
						}
					}
					
					//debug=true;
					
					if (debug) {
						response.getWriter().println("payLoad: " + payLoad);
//						response.getWriter().println("BPGUID: " + jsonObject.getString("BPGUID"));
					}
					
					try {
						JSONObject jsonObject1 = new JSONObject(payLoad);
					
						if (null != jsonObject1.getString("Source")
								&& bpRenewalJson.getString("Source").trim().length() > 0) {
							reqSource = bpRenewalJson.getString("Source");
						}
					} catch (JSONException e) {
						if (e.getMessage().equalsIgnoreCase("JSONObject[\"Source\"] not found.")) {
							reqSource = "";
						}
					}
					/*if (null != jsonObject.getString("Source") && jsonObject.getString("Source").trim().length() > 0) {
						reqSource = jsonObject.getString("Source");
					}else{
						reqSource = "";
					}*/
					
					if (null != bpRenewalJson.getString("CustomerNo")) {
						customerNo = bpRenewalJson.getString("CustomerNo");
						cpType = bpRenewalJson.getString("CPType");
					} else {
						errorCode = "E100";
						errorMsg = properties.getProperty(errorCode);
					}
					
					if (null != bpRenewalJson.getString("CPType")) {
						cpType = bpRenewalJson.getString("CPType");
					} else {
						errorCode = "E201";
						errorMsg = properties.getProperty(errorCode);
					}
					
//					if(null != jsonObject.getString("STATUS"))
					
					statusID = "";
					
					if(bpRenewalJson.getString("TestRun").equalsIgnoreCase("X") && bpRenewalJson.getString("Source").equalsIgnoreCase("D")){
						try {
							statusID = bpRenewalJson.getString("STATUS");
						} catch (Exception e) {
							try{
								statusID = bpRenewalJson.getString("Status");
							}catch (Exception ex) {
								statusID = "";
							}
						}
					}else{
						statusID = "";
					}
					
					if (debug) {
						response.getWriter().println("customerNo: " + customerNo);
						response.getWriter().println("statusID: " + statusID);
						response.getWriter().println("cpType: " + cpType);
					}
					
					if(null != bpRenewalJson.getString("TestRun")){
						if(bpRenewalJson.getString("TestRun").equalsIgnoreCase("L")){
							renewalListScenario = bpRenewalJson.getString("TestRun");
						}else{
							renewalListScenario = "";
						}
					}
					
					if(!  renewalListScenario.equalsIgnoreCase("L")){
						if (null != bpRenewalJson.getString("BPGUID")) {
							bpGuid = bpRenewalJson.getString("BPGUID");
						} else {
							errorCode = "E169";
							errorMsg = properties.getProperty(errorCode);
						}
						
						if (errorCode != null & errorCode.trim().length() == 0) {
							String loginID = commonUtils.getLoginID(request, response, debug);
							
							if(debug)
								response.getWriter().println("loginID: "+loginID);
							// * if (request.getUserPrincipal() != null) { * //
								if(loginID!=null ) {
								if(! reqSource.equalsIgnoreCase("D")){
									// * String loginID = commonUtils.getLoginID(request, response, debug); * //
//									String loginID1 =  commonUtils.getUserPrincipal(request, "name", response);
									if(debug){
										response.getWriter().println("loginID: "+loginID);
//										response.getWriter().println("loginID1: "+loginID1);
									}
									
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
							
							if (errorCode != null & errorCode.trim().length() == 0) {
								if(! reqSource.equalsIgnoreCase("D")){
									if(! isRequestFromCloud){
										JsonObject userCustomerJson = new JsonObject();
										if(cpType != null && cpType.equalsIgnoreCase("01"))
											userCustomerJson = commonUtils.getUserCustomersFromERP(request, response, sessionID, customerNo, debug);
										else
											userCustomerJson = commonUtils.getVendorsFromERP(request, response, sessionID, customerNo, debug);
										if (debug){
											response.getWriter().println("BPRenewal-userCustomerJson.Status: " + userCustomerJson.get("Status").getAsString());
											response.getWriter().println("BPRenewal-userCustomerJson: " + userCustomerJson);
										}
										if (userCustomerJson.get("Status").getAsString().equalsIgnoreCase("000001")) {
											errorCode="";
											if (debug) {
												response.getWriter().println("BPRenewal-jsonObject.TestRun: " + bpRenewalJson.getString("TestRun"));
											}
											if(bpRenewalJson.getString("TestRun").equalsIgnoreCase("")){
//												debug=true;
												status = "";
												try {
													status = bpRenewalJson.getString("STATUS");
												} catch (Exception e) {
													try{
														status = bpRenewalJson.getString("Status");
													}catch (Exception ex) {
														status = "";
														if(debug){
															StackTraceElement element[] = e.getStackTrace();
															StringBuffer buffer = new StringBuffer();
															for(int i=0;i<element.length;i++)
															{
																buffer.append(element[i]);
															}
															response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
														}
													}
													
													if(debug){
														StackTraceElement element[] = e.getStackTrace();
														StringBuffer buffer = new StringBuffer();
														for(int i=0;i<element.length;i++)
														{
															buffer.append(element[i]);
														}
														response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
													}
												}
												if (debug) {
													response.getWriter().println("BPRenewal-jsonObject.Status: " + status);
												}
												if(status.equalsIgnoreCase("000020") || status.equalsIgnoreCase("000030")){
													
//													String payloadRequest = getBody(request, response);
													JSONObject inputJsonObject = null;
													if (debug)
														response.getWriter().println("BPRenewal-jsonObject: " + bpRenewalJson);
													
													inputJsonObject = new JSONObject(payLoad);
													aggrID = commonUtils.readDestProperties("AggrID");
													if (debug){
														response.getWriter().println("BPRenewal-inputJsonObject: " + inputJsonObject);
														response.getWriter().println("BPRenewal-aggrID: " + aggrID);
													}
													errorCode = commonUtils.createSupplyChainPartner(request, response, aggrID, inputJsonObject, userCustomerJson, debug);
													if (debug)
														response.getWriter().println("BPRenewal-createSupplyChainPartner.errorCode: " + errorCode);
													
													if (errorCode.equalsIgnoreCase("") && errorCode.trim().length() == 0 ) {
														if(status.equalsIgnoreCase("000020")){
															//Call insertCurrentBPData() here - To be done by Sujitha
															errorCode = commonUtils.insertCurrentBPData(aggrID, inputJsonObject, userCustomerJson, request, response, debug);
														}
													}
												}
											}else{
												if(debug)
													response.getWriter().println("Do none");
											}
										} else {
											errorCode ="E105";
										}
									}else{
										//Request from cloud source - insert into BP with status 000001
										JsonObject userCustomerJson = new JsonObject();
//										userCustomerJson = commonUtils.getUserCustomersFromERP(request, response, sessionID, customerNo, debug);
										//Get UserCustomers from oData
										userCustomerJson = commonUtils.getUserCustomersFromCloud(request, response, customerNo, debug);
										
										if(userCustomerJson.has("d")){
											if(userCustomerJson.get("d").getAsJsonObject().get("results").getAsJsonArray().size() == 0){
												errorCode ="E105";
											}else{
												if(bpRenewalJson.getString("TestRun").equalsIgnoreCase("")){
//													debug = false;
													status = "";
													try {
														status = bpRenewalJson.getString("STATUS");
													} catch (Exception e) {
														try{
															status = bpRenewalJson.getString("Status");
														}catch (Exception ex) {
															status = "";
														}
													}
													if (debug) {
														response.getWriter().println("BPRenewal-jsonObject.Status: " + status);
													}
													
													if(status.equalsIgnoreCase("000020")){
														JSONObject inputJsonObject = null;
														if (debug)
															response.getWriter().println("BPRenewal-jsonObject: " + bpRenewalJson);
														
														inputJsonObject = new JSONObject(payLoad);
														//Call insertCurrentBPData() here
														errorCode = commonUtils.insertCurrentCloudBPData(aggrID, inputJsonObject, userCustomerJson, request, response, debug);
													}
												}
											}
										}else{
											errorCode ="E105";
										}
										
									}
//									debug = false;
									String aggregatorID="", executeURL="", oDataUrl="", userName="", password="", userPass="";
									oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
									aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
									userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
									password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
									userPass = userName+":"+password;
									
									//Changes done for Multi Renewal - Starts here
									if(debug){
										response.getWriter().println("oDataUrl: "+oDataUrl);
										response.getWriter().println("aggregatorID: "+aggregatorID);
										response.getWriter().println("Json TestRun:"+bpRenewalJson.getString("TestRun"));
									}
									String testRun="";
									if(null != bpRenewalJson.getString("TestRun")){
										testRun = bpRenewalJson.getString("TestRun");
									}else{
										testRun="";
									}
									
									if (errorCode != null & errorCode.trim().length() == 0) {
										if(! isRequestFromCloud){
											if(debug){
												response.getWriter().println("Request not from cloud");
												response.getWriter().println("customerNo: "+customerNo);
												response.getWriter().println("aggregatorID: "+aggregatorID);
											}
											
											String formattedStr = "";
											try{
												int number = Integer.parseInt(customerNo);
												formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
												customerNo = formattedStr;
											}catch (NumberFormatException e) {
//													formattedStr = customerNo;
											}
											
											//Check if BP record is available with status '000030'
											JsonObject bpEntriesJson = new JsonObject();
											executeURL = oDataUrl+"BPHeader?$filter=CPGuid%20eq%20%27"+customerNo+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											bpEntriesJson = commonUtils.executeURL(executeURL, userPass, response);
											if(debug){
												response.getWriter().println("!isRequestFromCloud.BPHeader.executeURL: "+executeURL);
												response.getWriter().println("!isRequestFromCloud.BPHeader.bpEntriesJson: "+bpEntriesJson);
											}
											JsonObject bpResults = bpEntriesJson.get("d").getAsJsonObject();
											JsonArray bpDResults = bpResults.get("results").getAsJsonArray();
											JsonObject bpEntryToUpdate = new JsonObject();
											
											if(bpDResults.size() > 0){
												for (int i = 0; i <= bpDResults.size() - 1; i++) {
													JsonObject bpEntry = (JsonObject) bpDResults.get(i);
													
													if((!bpEntry.get("StatusID").isJsonNull()) && bpEntry.get("StatusID").getAsString().equalsIgnoreCase("000030")){
														bpEntryToUpdate = bpEntry;
														if(debug)
															response.getWriter().println("Already Applied");
														hasBpApplied = true;
														break;
													}
												}
											}
											
											if(debug)
												response.getWriter().println("!isRequestFromCloud.BPHeader.hasBpApplied: "+hasBpApplied);
											
											if(hasBpApplied){
												executeURL = "";
												JsonObject scfEntriesJson = new JsonObject();
												executeURL = oDataUrl+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPGUID%20eq%20%27"+customerNo+"%27%20and%20StatusID%20eq%20%27"+"000002"+"%27";
												scfEntriesJson = commonUtils.executeURL(executeURL, userPass, response);
												if(debug){
													response.getWriter().println("isRequestFromCloud.SupplyChainFinances.executeURL: "+executeURL);
													response.getWriter().println("isRequestFromCloud.SupplyChainFinances.scfEntriesJson: "+scfEntriesJson);
												}
												
												JsonObject scfResults = scfEntriesJson.get("d").getAsJsonObject();
												JsonArray scfDResults = scfResults.get("results").getAsJsonArray();
												JsonObject scfEntryToUpdate = new JsonObject();
												
												if(scfDResults.size() > 0){
													Pattern p = Pattern.compile("(?<=\\()(\\d+)(([-+])(\\d+))?(?=\\))");
													long timestamp = 0;
													
													Date eCompletionDate = new Date();
											        Date currentDate = new Date(System.currentTimeMillis());
											        
											        if(debug)
														response.getWriter().println("!isRequestFromCloud.currentDate in server: "+currentDate);
											        
											        currentDate = commonUtils.dateinTimeZone(currentDate, "dd MMM yyyy hh:mm:ss a", "IST");
											        
											        if(debug)
														response.getWriter().println("isRequestFromCloud.currentDate in IST: "+currentDate);
											        
											        int diffDays = 0;
											        String scfUniqueID="", bpUniqueID="";
											        JsonObject scfUpdateResponse = new JsonObject();
											        JsonObject bpUpdateResponse = new JsonObject();
											        
											        for (int i = 0; i <= scfDResults.size() - 1; i++) {
														JsonObject scfEntry = (JsonObject) scfDResults.get(i);
														
														if((!scfEntry.get("StatusID").isJsonNull()) && scfEntry.get("StatusID").getAsString().equalsIgnoreCase("000002")){
															if(debug){
																response.getWriter().println("!isRequestFromCloud.Already Applied");
																response.getWriter().println("!isRequestFromCloud.ECompleteDate: "+scfEntry.get("ECompleteDate").getAsString());
															}
															
															Matcher m = p.matcher(scfEntry.get("ECompleteDate").getAsString());
															if (m.find()) {
																timestamp = Long.parseLong(m.group(1));
																if(debug)
																	response.getWriter().println("!isRequestFromCloud.timestamp: "+timestamp);
																eCompletionDate.setTime(timestamp);
																if(debug)
																	response.getWriter().println("!isRequestFromCloud.eCompletionDate: "+eCompletionDate);
																
																diffDays = commonUtils.dateDifference(currentDate, eCompletionDate);
																if(debug)
																	response.getWriter().println("diffDays: "+diffDays);
																//check the day difference from TypeSets
																String pcgUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
																String pcgUser = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
																String pcgPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
																String aggrId=commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
																String pcgUserPass = pcgUser + ":" + pcgPassword;
																String configUrl=pcgUrl+"ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" +"AGGRICICI"
																		+ "%27%20and%20Typeset%20eq%20%27" + "PYRNDY" + "%27%20and%20TypeValue%20eq%20%27"+aggrId+"%27";
																
																JsonObject configTypesetObj = commonUtils.executeURL(configUrl, pcgUserPass, response);
																if(debug){
																	response.getWriter().println("configTypesetObj:"+configTypesetObj);
																}
																// if record Exist take the Types field value as a validation days
																int validateDays=90;
																if(!configTypesetObj.has("error")&&configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
																	JsonObject configObj = configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
																	if(!configObj.get("Types").isJsonNull()&&!configObj.get("Types").getAsString().equalsIgnoreCase("")){
																		validateDays=configObj.get("Types").getAsInt();
																	}
																	
																}
																if(debug){
																	response.getWriter().println("validateDays:"+validateDays);
																}
						                              
																if(diffDays >= validateDays){
																	scfEntryToUpdate = scfEntry;
																	scfUniqueID = scfEntryToUpdate.get("ID").getAsString();
																	if(debug)
																		response.getWriter().println("isRequestFromCloud.SCF ID: "+scfUniqueID);
																	scfEntryToUpdate.remove("__metadata");
																	scfEntryToUpdate.remove("StatusID");
																	scfEntryToUpdate.remove("ID");
																	scfEntryToUpdate.addProperty("StatusID", "000003");
																	if(debug){
																		response.getWriter().println("!isRequestFromCloud.scfEntryToUpdate: "+scfEntryToUpdate);
																		response.getWriter().println("!isRequestFromCloud.SCF StatusID: "+scfEntryToUpdate.get("StatusID").getAsString());
																	}
//																	if(testRun == null || testRun.trim().length() == 0 || testRun.equalsIgnoreCase("")){
																		executeURL = oDataUrl+"SupplyChainFinances('"+scfUniqueID+"')";
																		scfUpdateResponse = commonUtils.executeRenewalUpdate(executeURL, userPass, response, scfEntryToUpdate, request, debug, "PYGWHANA");
																		if(debug){
																			response.getWriter().println("!isRequestFromCloud.scfUpdateResponse: "+scfUpdateResponse);
																		}
//																	}
																	if(debug)
																		response.getWriter().println("!isRequestFromCloud.bpEntryToUpdate: "+bpEntryToUpdate);
																	bpUniqueID = bpEntryToUpdate.get("ID").getAsString();
																	bpEntryToUpdate.remove("__metadata");
																	bpEntryToUpdate.remove("ID");
																	bpEntryToUpdate.remove("BPContactPersons");
																	bpEntryToUpdate.remove("StatusID");
																	bpEntryToUpdate.addProperty("StatusID", "999999");
																	if(debug)
																		response.getWriter().println("!isRequestFromCloud.final bpEntryToUpdate: "+bpEntryToUpdate);
																	executeURL="";
																	executeURL = oDataUrl+"BPHeader('"+bpUniqueID+"')";
																	bpUpdateResponse = commonUtils.executeRenewalUpdate(executeURL, userPass, response, bpEntryToUpdate, request, debug, "PYGWHANA");
																	if(debug)
																		response.getWriter().println("!isRequestFromCloud.bpUpdateResponse: "+bpUpdateResponse);
																}else{
																	if(debug)
																		response.getWriter().println("!isRequestFromCloud.No need to do anything");
																}
																
															}
															hasBpApplied = true;
//																break;
														}
													}
												}
											}
										}else{
											//For cloud onboarded customers
											if(debug){
												response.getWriter().println("Request from cloud");
												response.getWriter().println("customerNo: "+customerNo);
												response.getWriter().println("aggregatorID: "+aggregatorID);
											}
											
											String formattedStr = "";
											try{
												int number = Integer.parseInt(customerNo);
												formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
												customerNo = formattedStr;
											}catch (NumberFormatException e) {
//													formattedStr = customerNo;
											}
											
											//Check if BP record is available with status '000030'
											JsonObject bpEntriesJson = new JsonObject();
											executeURL = oDataUrl+"BPHeader?$filter=CPGuid%20eq%20%27"+customerNo+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											bpEntriesJson = commonUtils.executeURL(executeURL, userPass, response);
											if(debug){
												response.getWriter().println("isRequestFromCloud.BPHeader.executeURL: "+executeURL);
												response.getWriter().println("isRequestFromCloud.BPHeader.bpEntriesJson: "+bpEntriesJson);
											}
											JsonObject bpResults = bpEntriesJson.get("d").getAsJsonObject();
											JsonArray bpDResults = bpResults.get("results").getAsJsonArray();
											JsonObject bpEntryToUpdate = new JsonObject();
											
											if(bpDResults.size() > 0){
												for (int i = 0; i <= bpDResults.size() - 1; i++) {
													JsonObject bpEntry = (JsonObject) bpDResults.get(i);
													
													if((!bpEntry.get("StatusID").isJsonNull()) && bpEntry.get("StatusID").getAsString().equalsIgnoreCase("000030")){
														bpEntryToUpdate = bpEntry;
														if(debug)
															response.getWriter().println("Already Applied");
														hasBpApplied = true;
														break;
													}
												}
											}
											
											if(debug)
												response.getWriter().println("isRequestFromCloud.BPHeader.hasBpApplied: "+hasBpApplied);
											
											if(hasBpApplied){
												executeURL = "";
												JsonObject scfEntriesJson = new JsonObject();
												executeURL = oDataUrl+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPGUID%20eq%20%27"+customerNo+"%27%20and%20StatusID%20eq%20%27"+"000002"+"%27";
												scfEntriesJson = commonUtils.executeURL(executeURL, userPass, response);
												if(debug){
													response.getWriter().println("isRequestFromCloud.SupplyChainFinances.executeURL: "+executeURL);
													response.getWriter().println("isRequestFromCloud.SupplyChainFinances.scfEntriesJson: "+scfEntriesJson);
												}
												
												JsonObject scfResults = scfEntriesJson.get("d").getAsJsonObject();
												JsonArray scfDResults = scfResults.get("results").getAsJsonArray();
												JsonObject scfEntryToUpdate = new JsonObject();
												
												if(scfDResults.size() > 0){
													Pattern p = Pattern.compile("(?<=\\()(\\d+)(([-+])(\\d+))?(?=\\))");
													long timestamp = 0;
													
													Date eCompletionDate = new Date();
											        Date currentDate = new Date(System.currentTimeMillis());
											        
											        if(debug)
														response.getWriter().println("isRequestFromCloud.currentDate in server: "+currentDate);
											        
											        currentDate = commonUtils.dateinTimeZone(currentDate, "dd MMM yyyy hh:mm:ss a", "IST");
											        
											        if(debug)
														response.getWriter().println("isRequestFromCloud.currentDate in IST: "+currentDate);
											        
											        int diffDays = 0;
											        String scfUniqueID="", bpUniqueID="";
											        JsonObject scfUpdateResponse = new JsonObject();
											        JsonObject bpUpdateResponse = new JsonObject();
											        
											        for (int i = 0; i <= scfDResults.size() - 1; i++) {
														JsonObject scfEntry = (JsonObject) scfDResults.get(i);
														if((!scfEntry.get("ApplicationNo").isJsonNull()) && scfEntry.get("ApplicationNo").getAsString().startsWith("PAR")
															||(!scfEntry.get("ApplicationNo").isJsonNull()) && scfEntry.get("ApplicationNo").getAsString().startsWith("PQR")){
															if((!scfEntry.get("StatusID").isJsonNull()) && scfEntry.get("StatusID").getAsString().equalsIgnoreCase("000002")){
																if(debug){
																	response.getWriter().println("isRequestFromCloud.Already Applied");
																	response.getWriter().println("isRequestFromCloud.ECompleteDate: "+scfEntry.get("ECompleteDate").getAsString());
																}
																
																Matcher m = p.matcher(scfEntry.get("ECompleteDate").getAsString());
																if (m.find()) {
																	timestamp = Long.parseLong(m.group(1));
																	if(debug)
																		response.getWriter().println("isRequestFromCloud.timestamp: "+timestamp);
																	eCompletionDate.setTime(timestamp);
																	if(debug)
																		response.getWriter().println("isRequestFromCloud.eCompletionDate: "+eCompletionDate);
																	
																	diffDays = commonUtils.dateDifference(currentDate, eCompletionDate);
																	if(debug)
																	response.getWriter().println("diffDays: "+diffDays);
																	
																	//check the day difference from TypeSets
																	String pcgUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
																	String pcgUser = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
																	String pcgPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
																	String aggrId=commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
																	String pcgUserPass = pcgUser + ":" + pcgPassword;
																	String configUrl=pcgUrl+"ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" +"AGGRICICI"
																			+ "%27%20and%20Typeset%20eq%20%27" + "PYRNDY" + "%27%20and%20TypeValue%20eq%20%27"+aggrId+"%27";
																	JsonObject configTypesetObj = commonUtils.executeURL(configUrl, pcgUserPass, response);
																	if(debug){
																		response.getWriter().println("configTypesetObj:"+configTypesetObj);
																	}
																	// if record Exist take the Types field value as a validation days
																	int validateDays=90;
																	if(!configTypesetObj.has("error")&&configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
																		JsonObject configObj = configTypesetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
																		if(!configObj.get("Types").isJsonNull()&&!configObj.get("Types").getAsString().equalsIgnoreCase("")){
																			validateDays=configObj.get("Types").getAsInt();
																		}
																		
																	}
																	if(debug){
																		response.getWriter().println("validateDays:"+validateDays);
																	}
																	
																	if(diffDays >= validateDays){
																		scfEntryToUpdate = scfEntry;
																		scfUniqueID = scfEntryToUpdate.get("ID").getAsString();
																		if(debug)
																			response.getWriter().println("isRequestFromCloud.SCF ID: "+scfUniqueID);
																		scfEntryToUpdate.remove("__metadata");
																		scfEntryToUpdate.remove("StatusID");
																		scfEntryToUpdate.remove("ID");
																		scfEntryToUpdate.addProperty("StatusID", "000003");
																		if(debug){
																			response.getWriter().println("isRequestFromCloud.scfEntryToUpdate: "+scfEntryToUpdate);
																			response.getWriter().println("isRequestFromCloud.SCF StatusID: "+scfEntryToUpdate.get("StatusID").getAsString());
																		}
																		executeURL = oDataUrl+"SupplyChainFinances('"+scfUniqueID+"')";
																		scfUpdateResponse = commonUtils.executeRenewalUpdate(executeURL, userPass, response, scfEntryToUpdate, request, debug, "PYGWHANA");
																		if(debug){
																			response.getWriter().println("isRequestFromCloud.scfUpdateResponse: "+scfUpdateResponse);
																			response.getWriter().println("isRequestFromCloud.bpEntryToUpdate: "+bpEntryToUpdate);
																		}
																		bpUniqueID = bpEntryToUpdate.get("ID").getAsString();
																		bpEntryToUpdate.remove("__metadata");
																		bpEntryToUpdate.remove("ID");
																		bpEntryToUpdate.remove("BPContactPersons");
																		bpEntryToUpdate.remove("StatusID");
																		bpEntryToUpdate.addProperty("StatusID", "999999");
																		if(debug)
																			response.getWriter().println("isRequestFromCloud.final bpEntryToUpdate: "+bpEntryToUpdate);
																		executeURL="";
																		executeURL = oDataUrl+"BPHeader('"+bpUniqueID+"')";
																		bpUpdateResponse = commonUtils.executeRenewalUpdate(executeURL, userPass, response, bpEntryToUpdate, request, debug, "PYGWHANA");
																		if(debug)
																			response.getWriter().println("isRequestFromCloud.bpUpdateResponse: "+bpUpdateResponse);
																	}else{
																		if(debug)
																			response.getWriter().println("isRequestFromCloud.No need to do anything");
																	}
																}
																hasBpApplied = true;
//																	break;
															}
														}
													}
												}
											}
										}
									}
									//Changes done for Multi Renewal - Ends here
								}
								
								if (debug) {
									response.getWriter().println("errorMsg: " + errorMsg);
								}
								
								if (errorCode != null & errorCode.trim().length() == 0) {
									errorCode = commonUtils.readDestProperties("CorpID");
									if (debug) {
										response.getWriter().println("corpID: " + corpID);
									}
									
									if (errorCode != null && errorCode.trim().length() > 0 && !errorCode.trim().equalsIgnoreCase("E152")) {
										corpID = errorCode;
										String name = SCFLIMIT_DEST_NAME;
//										wsURL = commonUtils.readDestProperties("URL",response);
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
											
//											String system = commonUtils.readDestProperties("System",response);
											String userName="", passWord="", userpass = "";
											
											
											userName = cpiDestConfig.get("User").get().toString();
											passWord = cpiDestConfig.get("Password").get().toString();
											userpass = userName+":"+passWord;
											
											if(debug){
												response.getWriter().println("Var userName:"+userName);
												response.getWriter().println("Var passWord:"+passWord);
												response.getWriter().println("Var userpass:"+userpass);
											}
											
											if(debug){
												response.getWriter().println("Json TestRun:"+bpRenewalJson.getString("TestRun"));
											}
											String testRun="";
											if(null != bpRenewalJson.getString("TestRun")){
												testRun = bpRenewalJson.getString("TestRun");
											}else{
												testRun="";
											}
											if(debug){
												response.getWriter().println("Var testRun:"+testRun);
											}
											if(userpass != null && (!userpass.trim().equalsIgnoreCase("E153") && !userpass.trim().equalsIgnoreCase("E127"))){
												try {
													if (null != testRun){
														//Check Source and TestRun - If 'D' and 'X' read from UI payload, otherwise read from destination
														aggrID ="";
														if(bpRenewalJson.getString("Source").equalsIgnoreCase("D")){
															try{
																aggrID = bpRenewalJson.getString("AggregatorID");
															}catch (Exception e) {
																aggrID ="E112";
															}
														}else{
															aggrID = commonUtils.readDestProperties("AggrID");
														}
														
														if(debug){
															response.getWriter().println("aggrID: "+aggrID);
														}
														
														if(aggrID != "E112")
														{
															String formattedStr = "";
															try{
																int number = Integer.parseInt(customerNo);
																formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
																customerNo = formattedStr;
															}catch (NumberFormatException e) {
//																formattedStr = customerNo;
															}
//															customerNo = formattedStr;
															if(debug){
																response.getWriter().println("Formatted Customer: "+customerNo);
															}
															bpRenewalResponse = callBPRenewalService(customerNo, aggrID, statusID, cpType, wsURL, userpass, response, debug);
															
															if(debug){
																response.getWriter().println("bpRenewalResponse: "+bpRenewalResponse);
																response.getWriter().println("bpRenewalResponse-length: "+bpRenewalResponse.length());
																response.getWriter().println("bpRenewalResponse-trim: "+bpRenewalResponse.trim());
															}
//															JSONObject bpRenewalReturn = new JSONObject(bpRenewalResponse);
															JsonObject bpRenewalReturn = new JsonObject();
															JsonParser parser = new JsonParser();
															bpRenewalReturn = (JsonObject)parser.parse(bpRenewalResponse);
															
//															debug = true;
															if(debug)
																response.getWriter().println("bpRenewalReturn: "+bpRenewalReturn);
//															JsonObj
															/*if(isRequestFromCloud){
																JsonArray rootArray = new JsonArray();
																JsonArray newRootArray = new JsonArray();
																boolean isRecordAvailable = false;
																try{
																	rootArray = bpRenewalReturn.get("Root").getAsJsonArray();
//																	newRootArray = 
																	if(debug){
																		response.getWriter().println("isRequestFromCloud.bpRenewalReturn.rootArray: "+rootArray);
																	}
																	
																	for(int i=0 ; i<rootArray.size() ; i++){
																		if(debug){
																			response.getWriter().println("isRequestFromCloud.bpRenewalReturn.STATUS: "+rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString());
																		}
																		
																		if(!rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("")){
																			if(debug)
																				response.getWriter().println("Other than blank status");
//																			statusOfRecord = rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString();
																			newRootArray.add(rootArray.get(i).getAsJsonObject());
																			isRecordAvailable = true;
//																			break;
																		}
																	}
																	
																	if(isRecordAvailable){
																		bpRenewalReturn = new JsonObject();
																		bpRenewalReturn.add("Root", newRootArray);
																	}
																	
																	if(debug){
																		response.getWriter().println("isRequestFromCloud.bpRenewalReturn.newRootArray: "+newRootArray);
																		response.getWriter().println("isRequestFromCloud.bpRenewalReturn.bpRenewalReturn: "+bpRenewalReturn);
																	}
																}catch (Exception e) {
																	if(debug){
																		StackTraceElement element[] = e.getStackTrace();
																		StringBuffer buffer = new StringBuffer();
																		for(int i=0;i<element.length;i++)
																		{
																			buffer.append(element[i]);
																		}
																		response.getWriter().println("bpRenewalReturn-statusOfRecordException Stack Trace: "+buffer.toString());
																	}
																}
															}*/
															
															if (null != testRun && testRun.equalsIgnoreCase("")){
																if (bpRenewalReturn.get("ResponseCode").getAsString().equalsIgnoreCase("00000")) {
																	//Call BPRenewalInsert here
																	bpRenewalJsonResponse = callBPRenewalInsertService(bpGuid, customerNo, bpRenewalJson, aggrID, wsURL, userpass, response, debug);
																	/*if(bpRenewalJsonResponse.get("ResponseCode").getAsString() != null 
																		&& bpRenewalJsonResponse.get("ResponseCode").getAsString().trim().length() > 0){
																		response.getWriter().println(new Gson().toJson(bpRenewalJsonResponse));
																	}*/
																	
																} else if(bpRenewalReturn.get("ResponseCode").getAsString().equalsIgnoreCase("200")) {
																	String statusOfRecord = "";
																	/*JSONObject rootObject = new JSONObject();
																	JSONArray rootArray = new JSONArray();
																	try{
																		rootObject = bpRenewalReturn.getJSONObject("Root");
																		
																		statusOfRecord = rootObject.getJSONObject("BPHeader").getString("STATUS");
																		if (debug) 
																			response.getWriter().println("bpRenewalReturn: "+statusOfRecord);
																		
																	}catch (JSONException e) {
																		if(e.getMessage().equalsIgnoreCase("JSONObject[\"Root\"] is not a JSONObject.")){
																			rootArray = bpRenewalReturn.getJSONArray("Root");
																			//loop through the array
																			for{
																				if(! bpheader.status.equals"000001"){
																					statusOfRecord = "";
																				}
																			}
																			statusOfRecord = "";
																		}
																	}*/
																	
																	JsonArray rootArray = new JsonArray();
																	boolean isFreshRenewal=true;
																	try{
																		rootArray = bpRenewalReturn.get("Root").getAsJsonArray();
																		if(debug){
																			response.getWriter().println("bpRenewalReturn.rootArray: "+rootArray);
																		}
																		
																		for(int i=0 ; i<rootArray.size() ; i++){
																			if(debug){
																				response.getWriter().println("bpRenewalReturn.STATUS: "+rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString());
																			}
																			
																			if(rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("000001")){
																				statusOfRecord = rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString();
//																				break;
																			}
																			
																			if(rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("999999")){
																				statusOfRecord = rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString();
																			}
																			
																			if(rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("000010")
																				|| rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("000020")
																				|| rootArray.get(i).getAsJsonObject().get("BPHeader").getAsJsonObject().get("STATUS").getAsString().equalsIgnoreCase("000030")){
																				isFreshRenewal = false;
																			}
																		}
																		
//																		statusOfRecord = rootArray.getJSONObject("BPHeader").getString("STATUS");
																		if (debug) 
																			response.getWriter().println("bpRenewalReturn.statusOfRecord: "+statusOfRecord);
																		
																	}catch (Exception e) {
																		statusOfRecord = "exception";
																		if(debug){
																			StackTraceElement element[] = e.getStackTrace();
																			StringBuffer buffer = new StringBuffer();
																			for(int i=0;i<element.length;i++)
																			{
																				buffer.append(element[i]);
																			}
																			response.getWriter().println("bpRenewalReturn-statusOfRecordException Stack Trace: "+buffer.toString());
																		}
																	}
																	
																	if(debug){
																		response.getWriter().println("bpRenewalReturn.statusOfRecord: "+statusOfRecord);
																		response.getWriter().println("bpRenewalReturn.isFreshRenewal: "+isFreshRenewal);
																	}
																	
																	if((statusOfRecord.equalsIgnoreCase("000001") || statusOfRecord.equalsIgnoreCase("999999")) && isFreshRenewal){
																		//Call BPRenewalInsert here
																		bpRenewalJsonResponse = callBPRenewalInsertService(bpGuid, customerNo, bpRenewalJson, aggrID, wsURL, userpass, response, debug);
																		/*if(bpRenewalJsonResponse.get("ResponseCode").getAsString() != null 
																			&& bpRenewalJsonResponse.get("ResponseCode").getAsString().trim().length() > 0){
																			response.getWriter().println(new Gson().toJson(bpRenewalJsonResponse));
																		}*/
																	}else{
																		// Call BPRenewalUpdate here
																		bpRenewalJsonResponse = callBPRenewalUpdateService(bpGuid, customerNo, bpRenewalJson, aggrID, wsURL, userpass, response, debug);
																		/*if(bpRenewalJsonResponse.get("ResponseCode").getAsString() != null 
																			&& bpRenewalJsonResponse.get("ResponseCode").getAsString().trim().length() > 0){
																			response.getWriter().println(new Gson().toJson(bpRenewalJsonResponse));
																		}*/
																	}
																} else {
																	response.getWriter().println(new Gson().toJson(bpRenewalResponse));
																}
															} else{
//																response.getWriter().println(bpRenewalResponse);
																response.getWriter().println(bpRenewalReturn);
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
												} /*catch (JSONException e) {
													JsonObject result = new JsonObject();
													errorCode = "E157";
													errorMsg = properties.getProperty(errorCode);
													
													result.addProperty("errorCode", errorCode);
													result.addProperty("Message", errorMsg);
													result.addProperty("Status", properties.getProperty("ErrorStatus"));
													result.addProperty("Valid", "false");
													response.getWriter().println(new Gson().toJson(result));
												}*/ catch (Exception e) {
													JsonObject result = new JsonObject();
													result.addProperty("Exception", e.getClass().getCanonicalName());
													result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
													result.addProperty("Status", properties.getProperty("ErrorStatus"));
													result.addProperty("Valid", "false");
													if(debug){
														StackTraceElement element[] = e.getStackTrace();
														StringBuffer buffer = new StringBuffer();
														for(int i=0;i<element.length;i++)
														{
															buffer.append(element[i]);
														}
														response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
													}
													
													response.getWriter().println(new Gson().toJson(result));
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
									}
									else{
										JsonObject result = new JsonObject();
										result.addProperty("errorCode", errorCode);
										result.addProperty("Message", errorMsg);
										result.addProperty("Status", properties.getProperty("ErrorStatus"));
										result.addProperty("Valid", "false");
										response.getWriter().println(new Gson().toJson(result));
									}
								}
								else{
									if(errorCode.equalsIgnoreCase("BPHeader Insertion failed")){
										errorMsg = errorCode;
										
										JsonObject result = new JsonObject();
										result.addProperty("errorCode", "E099");
										result.addProperty("Message", errorMsg);
										result.addProperty("Status", properties.getProperty("ErrorStatus"));
										result.addProperty("Valid", "false");
										response.getWriter().println(new Gson().toJson(result));
									}else{
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
					}else{
						//Validate and Call BP Renewal List here
						boolean statusAvailable = false, customerAvailable=false, aggrIDAvailable=false, validRequest=false;
						
						status = "";
						try {
							status = bpRenewalJson.getString("STATUS");
						} catch (Exception e) {
							try{
								status = bpRenewalJson.getString("Status");
							}catch (Exception ex) {
								status = null;
								
								if(debug){
									StackTraceElement element[] = e.getStackTrace();
									StringBuffer buffer = new StringBuffer();
									for(int i=0;i<element.length;i++)
									{
										buffer.append(element[i]);
									}
									response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
								}
							}
						}
						
						
						try {
							if (null != status) {
								statusAvailable = true;
							}else{
								statusAvailable = false;
							}
						} catch (Exception e){
//							if (e.getMessage().equalsIgnoreCase("JSONObject[\"Status\"] not found.")) {
								statusAvailable = false;
//							}
						}
						
						try {
							if (null != bpRenewalJson.getString("CustomerNo")) {
								customerAvailable = true;
							}
						} catch (JSONException e){
							if (e.getMessage().equalsIgnoreCase("JSONObject[\"CustomerNo\"] not found.")) {
								customerAvailable = false;
							}
						}
						
						try {
							if (null != bpRenewalJson.getString("AggregatorID")) {
								aggrIDAvailable = true;
							}
						} catch (JSONException e){
							if (e.getMessage().equalsIgnoreCase("JSONObject[\"AggregatorID\"] not found.")) {
								aggrIDAvailable = false;
							}
						}
						
						if(statusAvailable){
							/*if(! jsonObject.getString("Status").equalsIgnoreCase("000020")){
								errorCode = "E171";
								errorMsg = properties.getProperty(errorCode);
								
								JsonObject result = new JsonObject();
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							} else {*/
								if(customerAvailable){
									if(null != bpRenewalJson.getString("CustomerNo") && bpRenewalJson.getString("CustomerNo").trim().length() > 0){
										if(aggrIDAvailable){
											if(null != bpRenewalJson.getString("AggregatorID") && bpRenewalJson.getString("AggregatorID").trim().length() == 0){
												errorCode = "E172";
												errorMsg = properties.getProperty(errorCode);
												
												JsonObject result = new JsonObject();
												result.addProperty("errorCode", errorCode);
												result.addProperty("Message", errorMsg);
												result.addProperty("Status", properties.getProperty("ErrorStatus"));
												result.addProperty("Valid", "false");
												response.getWriter().println(new Gson().toJson(result));
											}else{
												validRequest = true;
											}
										}else{
											errorCode = "E172";
											errorMsg = properties.getProperty(errorCode);
											
											JsonObject result = new JsonObject();
											result.addProperty("errorCode", errorCode);
											result.addProperty("Message", errorMsg);
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
										}
									}else{
										validRequest = true;
									}
								}else{
									validRequest = true;
								}
								
								if(validRequest){
									String name = SCFLIMIT_DEST_NAME;
									wsURL  = "";
									// Context ctxDestFact = new InitialContext();
									// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
									// DestinationConfiguration cpiDestConfig = configuration.getConfiguration(name);
									DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
											.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
									Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
											.tryGetDestination(name, options);
									Destination cpiDestConfig = destinationAccessor.get();

									wsURL = cpiDestConfig.get("URL").get().toString();
									
									if(wsURL != null && wsURL.trim().length() > 0){
										String userName="", passWord="", userpass = "";
										
										userName = cpiDestConfig.get("User").get().toString();
										passWord = cpiDestConfig.get("Password").get().toString();
										userpass = userName+":"+passWord;
										
										/*String system = commonUtils.readDestProperties("System");
										String userpass = "";

										if (system.equalsIgnoreCase("QAS")) {
											userpass = properties.getProperty("PeakLimitQASUsrPass");
										} else if (system.equalsIgnoreCase("PRD")) {
											userpass = properties.getProperty("PeakLimitPRDUsrPass");
										} else if (system.equalsIgnoreCase("DEV")) {
											userpass = properties.getProperty("PeakLimitDEVUsrPass");
										} else {
											if (system.equalsIgnoreCase("E153")){
												userpass = system;
											}else{
												userpass = "E127";
											}
										}*/

										if(userpass != null && (!userpass.trim().equalsIgnoreCase("E153") && !userpass.trim().equalsIgnoreCase("E127"))){
											String formattedStr = "";
											JsonObject bpRenewalListResponse=null;
											customerNo = "";
											customerNo = bpRenewalJson.getString("CustomerNo");
											
											if(customerNo.trim().length() > 0 ){
												try{
													int number = Integer.parseInt(customerNo);
													formattedStr = ("0000000000" + customerNo).substring(customerNo.length());
													customerNo = formattedStr;
												}catch (NumberFormatException e) {
//													formattedStr = customerNo;
												}
											}else{
												customerNo = "";
											}
											
											bpRenewalListResponse = callBPRenewalList(request, response, bpRenewalJson, customerNo, cpType, wsURL, userpass, debug);
//											response.getWriter().println(new Gson().toJson(bpRenewalListResponse));
//											JSONObject bpRenewalReturn = new JSONObject(bpRenewalListResponse);
										}
									}
								}
//							}
						} else {
							errorCode = "E170";
							errorMsg = properties.getProperty(errorCode);
							
							JsonObject result = new JsonObject();
							result.addProperty("errorCode", errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						}
					}
				}else {
					JsonObject result = new JsonObject();
					errorCode = "E108";
					errorMsg = properties.getProperty(errorCode);
					
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
				
			}catch (Exception e){
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
					response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
				}
			}
//		}
	}
	
	public void bpDeclarations(HttpServletRequest request, HttpServletResponse response, Properties properties) throws IOException{
		response.getWriter().println("Hello!");
		String payLoad="", destURL="";
		boolean debug=false, isRequestFromCloud=false;
		payLoad = request.getParameter("BPDeclarations");
		JSONObject bpDeclarationsJson = new JSONObject();
		Destination destConfiguration = null;
		String customerNo="", errorCode="", errorMsg="";
		
		try {
			bpDeclarationsJson = new JSONObject(payLoad);
			if (null != bpDeclarationsJson.getString("debug")
					&& bpDeclarationsJson.getString("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
		} catch (JSONException e) {
			if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
				debug = false;
			}
		}
		
		try {
			bpDeclarationsJson = new JSONObject(payLoad);
			CommonUtils commonUtils = new CommonUtils();
			
			destConfiguration = getDestinationURL(request, response);
			destURL = destConfiguration.get("URL").get().toString();
			if(debug)
				response.getWriter().println("doGet.destURL.: "+ destURL);
			
			/*if (destURL.contains("service.xsodata"))
			{
				isRequestFromCloud = true;
			} 
			else {
				isRequestFromCloud = false;
			}
			
			if(debug)
				response.getWriter().println("isRequestFromCloud: "+ isRequestFromCloud);
			
			if (null != bpDeclarationsJson.getString("CustomerNo")) {
				customerNo = bpDeclarationsJson.getString("CustomerNo");
			} else {
				errorCode = "E100";
				errorMsg = properties.getProperty(errorCode);
			}
			.*/
		}catch (Exception e){
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
				response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
			}
		}
	}
	
	public String callBPRenewalService(String customerNo, String aggrID, String statusID, String cpType, String wsURL, String userpass, HttpServletResponse response, boolean debug) throws IOException{
		String cpiResponse="";
		
		try {
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			JSONObject inputJson = new JSONObject();
			JSONObject root = new JSONObject();
		
			inputJson.put("CustomerNo", customerNo);
			inputJson.put("AggregatorID", aggrID);
			inputJson.put("CPTypeID", cpType);
//			if(statusID.trim().length() > 0)
//				inputJson.put("Status", statusID);
			root.put("Root", inputJson);
			
			byte[] postDataBytes = root.toString().getBytes("UTF-8");
			wsURL = wsURL + "/" + properties.getProperty("BPRenewalScenario");
			
			if (debug) {
				response.getWriter().println("wsURL-BPRenewalScenario: " + wsURL);
				response.getWriter().println("callBPRenewalService CustomerNo: " + inputJson.getString("CustomerNo"));
				response.getWriter().println("wsURL-userpass: " + userpass);
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

//			String basicAuth = "Basic " + new String(Base64.getEncoder().encodeToString(userpass.getBytes()));
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
				response.getWriter().println("callBPRenewalService sb: " + sb.toString());
//			bpRenewalJsonResponse.get("ResponseCode").getAsString()
			cpiResponse = sb.toString();
			return cpiResponse;
		} catch (JSONException e) {
//			cpiResponse = "Error:"+e.getClass().getCanonicalName()+ "--->" + e.getMessage();
//			ResponseCode
			cpiResponse = "{\"ResponseCode\":\"000005\", \"Message\":\""+e.getMessage()+"\", \"Status\":\"000005\", \"Exception\":\""+e.getClass().getCanonicalName()+"\", \"Valid\":\"false\"}";
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			
			return cpiResponse;
		} catch (UnsupportedEncodingException e) {
//			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			cpiResponse = "{\"ResponseCode\":\"000005\", \"Message\":\""+e.getMessage()+"\", \"Status\":\"000005\", \"Exception\":\""+e.getClass().getCanonicalName()+"\", \"Valid\":\"false\"}";
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			
			return cpiResponse;
		} catch (IOException e) {
//			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			cpiResponse = "{\"ResponseCode\":\"000005\", \"Message\":\""+e.getMessage()+"\", \"Status\":\"000005\", \"Exception\":\""+e.getClass().getCanonicalName()+"\", \"Valid\":\"false\"}";
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			
			return cpiResponse;
		}
	}
	
	public JsonObject callBPRenewalUpdateService(String bpGuid, String customerNo, JSONObject jsonObject, String aggrID, String wsURL, String userpass, HttpServletResponse response, boolean debug) throws IOException{
		String cpiResponse="", errorCode="", errorMsg="";
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try
		{
			CommonUtils commonUtils = new CommonUtils();
			
			jsonObject.put("AggregatorId", aggrID);
			jsonObject.remove("BPGUID");
			jsonObject.put("BPGUID", bpGuid);
			jsonObject.remove("CustomerNo");
			jsonObject.put("CustomerNo", customerNo);
			
			
			if (debug) {
				response.getWriter().println("callBPRenewalUpdateService jsonObject: " + jsonObject);
				response.getWriter().println("callBPRenewalUpdateService BPGUID: " + jsonObject.getString("BPGUID"));
				response.getWriter().println("callBPRenewalUpdateService CustomerNo: " + jsonObject.getString("CustomerNo"));
				response.getWriter().println("callBPRenewalUpdateService INCORP_DATE: " + jsonObject.getString("INCORP_DATE"));
				response.getWriter().println("callBPRenewalUpdateService UTIL_DISTRICT: " + jsonObject.getString("UTIL_DISTRICT"));
				response.getWriter().println("callBPRenewalUpdateService LEGAL_STATUS: " + jsonObject.getString("LEGAL_STATUS"));
				response.getWriter().println("callBPRenewalUpdateService CITY: " + jsonObject.getString("CITY"));
				response.getWriter().println("callBPRenewalUpdateService STATE: " + jsonObject.getString("STATE"));
				response.getWriter().println("callBPRenewalUpdateService PINCODE: " + jsonObject.getString("PINCODE"));
				response.getWriter().println("callBPRenewalUpdateService AggregatorId: " + jsonObject.getString("AggregatorId"));
			}

			String incorporationDate="", utilDistrict="", legalStatus="", city="", state="", pincode="", status="";
			incorporationDate = jsonObject.getString("INCORP_DATE");
			if (incorporationDate != null && incorporationDate.trim().length() == 0
					&& incorporationDate.trim().equalsIgnoreCase("")) {
				errorCode = "E160";
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				utilDistrict = jsonObject.getString("UTIL_DISTRICT");
				if (utilDistrict != null && utilDistrict.trim().length() == 0
						&& utilDistrict.trim().equalsIgnoreCase("")) {
					errorCode = "E161";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				legalStatus = jsonObject.getString("LEGAL_STATUS");
				if (legalStatus != null && legalStatus.trim().length() == 0
						&& legalStatus.trim().equalsIgnoreCase("")) {
					errorCode = "E162";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				try{
					if((jsonObject.getString("ADDRESS_1") ==  null || jsonObject.getString("ADDRESS_1").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_2") ==  null || jsonObject.getString("ADDRESS_2").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_3") ==  null || jsonObject.getString("ADDRESS_3").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_4") ==  null || jsonObject.getString("ADDRESS_4").trim().length() == 0)){
						errorCode = "E163";
					}
				}catch (JSONException e) {
					e.getMessage();
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				city = jsonObject.getString("CITY");
				if (city != null && city.trim().length() == 0
						&& city.trim().equalsIgnoreCase("")) {
					errorCode = "E164";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				state = jsonObject.getString("STATE");
				if (state != null && state.trim().length() == 0
						&& state.trim().equalsIgnoreCase("")) {
					errorCode = "E165";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				pincode = jsonObject.getString("PINCODE");
				if (pincode != null && pincode.trim().length() == 0
						&& pincode.trim().equalsIgnoreCase("")) {
					errorCode = "E166";
				}else{
					errorCode = commonUtils.validateInput(response, "PINCODE", pincode, debug);
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
//				status = jsonObject.getString("Status");
				status = "";
				try {
					status = jsonObject.getString("STATUS");
				} catch (Exception e) {
					try{
						status = jsonObject.getString("Status");
					}catch (Exception ex) {
						status = "";
					}
				}
				
				if (status != null && status.trim().length() == 0
						&& status.trim().equalsIgnoreCase("")) {
					errorCode = "E168";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				JSONArray arrayContactPerson = (JSONArray) jsonObject.getJSONArray("ContactPerson");
				if (debug) {
					response.getWriter().println("arrayContactPerson: " + arrayContactPerson);
					response.getWriter().println("arrayContactPerson-length: " + arrayContactPerson.length());
				}
				
				String key = "", value="";
				for (int i = 0; i <= arrayContactPerson.length() - 1; i++) {
					JSONObject contactPersonJsonObj = (JSONObject) arrayContactPerson.get(i);

					if (debug)
						response.getWriter().println("contactPersonJsonObj---names: " + contactPersonJsonObj.names().length());

					for (int j = 0; j < contactPersonJsonObj.names().length(); j++) {
						key = contactPersonJsonObj.names().get(j).toString();
						value = contactPersonJsonObj.getString(key);
						if (debug)
							response.getWriter().println("Contact Person "+i+": "+key+"----"+value);
						
						errorCode = commonUtils.validateInput(response, key, value, debug);

						if (errorCode != null && errorCode.trim().length() > 0) {
							break;
						}
					}
				}
				
				if (errorCode != null && errorCode.equalsIgnoreCase("")) {
					if (errorCode != null && errorCode.equalsIgnoreCase("")) {
						if (debug) {
							response.getWriter().println("aggrID: " + aggrID);
							response.getWriter().println("wsURL" + wsURL);
							response.getWriter().println("userpass" + userpass);
						}
						JSONObject root = new JSONObject();
					
						root.put("BPRenewalUpdate", jsonObject);
						
						if(debug)
							response.getWriter().println(root);
						
						wsURL = wsURL + "/" + properties.getProperty("BPRenewalUpdateScenario");
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

						String basicAuth = "Basic "+ Base64.getEncoder().encodeToString(userpass.getBytes());
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
							response.getWriter().println(new Gson().toJson(result));
							return result;
						}
					} else {
						errorMsg = properties.getProperty(errorCode);

						JsonObject result = new JsonObject();
						result.addProperty("errorCode", errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().println(new Gson().toJson(result));
						return result;
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
					return result;
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
		} catch (Exception e) {
			errorCode="Exception";
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", cpiResponse);
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
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			
			return result;
		}
	}
	
	public JsonObject callBPRenewalInsertService(String bpGuid, String customerNo, JSONObject jsonObject, String aggrID, String wsURL, String userpass, HttpServletResponse response, boolean debug) throws IOException{
		String cpiResponse="", errorCode="", errorMsg="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try
		{
			CommonUtils commonUtils = new CommonUtils();
			
			jsonObject.put("AggregatorId", aggrID);
			jsonObject.remove("BPGUID");
			jsonObject.put("BPGUID", bpGuid);
			jsonObject.remove("CustomerNo");
			jsonObject.put("CustomerNo", customerNo);
			
			if (debug) {
				response.getWriter().println("callBPRenewalInsertService jsonObject: " + jsonObject);
				response.getWriter().println("callBPRenewalInsertService BPGUID: " + jsonObject.getString("BPGUID"));
				response.getWriter().println("callBPRenewalInsertService CustomerNo: " + jsonObject.getString("CustomerNo"));
				response.getWriter().println("callBPRenewalInsertService INCORP_DATE: " + jsonObject.getString("INCORP_DATE"));
				response.getWriter().println("callBPRenewalInsertService UTIL_DISTRICT: " + jsonObject.getString("UTIL_DISTRICT"));
				response.getWriter().println("callBPRenewalInsertService LEGAL_STATUS: " + jsonObject.getString("LEGAL_STATUS"));
				response.getWriter().println("callBPRenewalInsertService CITY: " + jsonObject.getString("CITY"));
				response.getWriter().println("callBPRenewalInsertService STATE: " + jsonObject.getString("STATE"));
				response.getWriter().println("callBPRenewalInsertService PINCODE: " + jsonObject.getString("PINCODE"));
				response.getWriter().println("callBPRenewalInsertService AggregatorId: " + jsonObject.getString("AggregatorId"));
			}

			String incorporationDate="", utilDistrict="", legalStatus="", city="", state="", pincode="", status="";
			incorporationDate = jsonObject.getString("INCORP_DATE");
			if (incorporationDate != null && incorporationDate.trim().length() == 0
					&& incorporationDate.trim().equalsIgnoreCase("")) {
				errorCode = "E160";
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				utilDistrict = jsonObject.getString("UTIL_DISTRICT");
				if (utilDistrict != null && utilDistrict.trim().length() == 0
						&& utilDistrict.trim().equalsIgnoreCase("")) {
					errorCode = "E161";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				
				response.getWriter().println(new Gson().toJson(result));
				
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				legalStatus = jsonObject.getString("LEGAL_STATUS");
				if (legalStatus != null && legalStatus.trim().length() == 0
						&& legalStatus.trim().equalsIgnoreCase("")) {
					errorCode = "E162";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				try{
					if((jsonObject.getString("ADDRESS_1") ==  null || jsonObject.getString("ADDRESS_1").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_2") ==  null || jsonObject.getString("ADDRESS_2").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_3") ==  null || jsonObject.getString("ADDRESS_3").trim().length() == 0)
						&& (jsonObject.getString("ADDRESS_4") ==  null || jsonObject.getString("ADDRESS_4").trim().length() == 0)){
						errorCode = "E163";
					}
				}catch (JSONException e) {
					e.getMessage();
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				city = jsonObject.getString("CITY");
				if (city != null && city.trim().length() == 0
						&& city.trim().equalsIgnoreCase("")) {
					errorCode = "E164";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				state = jsonObject.getString("STATE");
				if (state != null && state.trim().length() == 0
						&& state.trim().equalsIgnoreCase("")) {
					errorCode = "E165";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				pincode = jsonObject.getString("PINCODE");
				if (pincode != null && pincode.trim().length() == 0
						&& pincode.trim().equalsIgnoreCase("")) {
					errorCode = "E166";
				}else{
					errorCode = commonUtils.validateInput(response, "PINCODE", pincode, debug);
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
//				status = jsonObject.getString("Status");
				status = "";
				try {
					status = jsonObject.getString("STATUS");
				} catch (Exception e) {
					try{
						status = jsonObject.getString("Status");
					}catch (Exception ex) {
						status = "";
					}
				}
				
				if (status != null && status.trim().length() == 0
						&& status.trim().equalsIgnoreCase("")) {
					errorCode = "E168";
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
			
			if (errorCode != null && errorCode.trim().length() == 0
					&& errorCode.trim().equalsIgnoreCase("")) {
				JSONArray arrayContactPerson = (JSONArray) jsonObject.getJSONArray("ContactPerson");
				if (debug) {
					response.getWriter().println("arrayContactPerson: " + arrayContactPerson);
					response.getWriter().println("arrayContactPerson-length: " + arrayContactPerson.length());
				}
				
				String key = "", value="";
				for (int i = 0; i <= arrayContactPerson.length() - 1; i++) {
					JSONObject contactPersonJsonObj = (JSONObject) arrayContactPerson.get(i);

					if (debug)
						response.getWriter().println("contactPersonJsonObj---names: " + contactPersonJsonObj.names().length());

					for (int j = 0; j < contactPersonJsonObj.names().length(); j++) {
						key = contactPersonJsonObj.names().get(j).toString();
						value = contactPersonJsonObj.getString(key);
						if (debug)
							response.getWriter().println("Contact Person "+i+": "+key+"----"+value);
						
						errorCode = commonUtils.validateInput(response, key, value, debug);

						if (errorCode != null && errorCode.trim().length() > 0) {
							break;
						}
					}
				}
				
				if (errorCode != null && errorCode.equalsIgnoreCase("")) {
					if (errorCode != null && errorCode.equalsIgnoreCase("")) {
						if (debug) {
							response.getWriter().println("aggrID: " + aggrID);
							response.getWriter().println("wsURL: " + wsURL);
							response.getWriter().println("userpass: " + userpass);
						}
						JSONObject root = new JSONObject();
					
						root.put("BPRenewalInsert", jsonObject);
						
						if(debug)
							response.getWriter().println(root);
						
						wsURL = wsURL + "/" + properties.getProperty("BPRenewalInsertScenario");
						if (debug)
							response.getWriter().println("BPRenewalInsertScenario wsURL:" + wsURL);
						
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

						String basicAuth = "Basic "+ Base64.getEncoder().encodeToString(userpass.getBytes());
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

						if(debug)
							response.getWriter().println("renewal insert response sb: " + sb.toString());

						cpiResponse = sb.toString();
						
						if(debug)
							response.getWriter().println("renewal insert response cpiResponse: " + cpiResponse);
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
							response.getWriter().println(new Gson().toJson(result));
							return result;
						}
					} else {
						errorMsg = properties.getProperty(errorCode);

						JsonObject result = new JsonObject();
						result.addProperty("errorCode", errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().println(new Gson().toJson(result));
						return result;
					}
				} else {
					errorMsg = properties.getProperty(errorCode);

					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
					return result;
				}
			} else {
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
		} catch (Exception e) {
			errorCode="Exception";
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", cpiResponse);
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
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			return result;
		}
	}
	
	public JsonObject callBPRenewalList(HttpServletRequest request, HttpServletResponse response, JSONObject jsonObject, String customerNo, String cpType, String wsURL, String userpass, boolean debug) throws IOException{
		String cpiResponse="", errorCode="", errorMsg="", status="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		try{
			jsonObject.remove("CustomerNo");
			jsonObject.put("CustomerNo", customerNo);
			jsonObject.remove("CPType");
			jsonObject.put("CPTypeID", cpType);
			if(customerNo.trim().length() == 0){
				jsonObject.remove("AggregatorID");
				jsonObject.put("AggregatorID", "");
			}
			
			if (debug) {
				response.getWriter().println("callBPRenewalList jsonObject: " + jsonObject);
				response.getWriter().println("callBPRenewalList CustomerNo: " + jsonObject.getString("CustomerNo"));
				response.getWriter().println("callBPRenewalList AggregatorID: " + jsonObject.getString("AggregatorID"));
				
				status = "";
				try {
					status = jsonObject.getString("STATUS");
				} catch (Exception e) {
					try{
						status = jsonObject.getString("Status");
					}catch (Exception ex) {
						status = "";
					}
				}
//				jsonObject.getString("Status")
				response.getWriter().println("callBPRenewalList Status: " + status);
				response.getWriter().println("wsURL: " + wsURL);
				response.getWriter().println("userpass: " + userpass);
			}
			
			JSONObject root = new JSONObject();
			
			root.put("BPRenewalList", jsonObject);
			
			if(debug)
				response.getWriter().println(root);
			
			wsURL = wsURL + "/" + properties.getProperty("BPRenewalListScenario");
			if (debug)
				response.getWriter().println("BPRenewalListScenario wsURL:" + wsURL);
			
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

			String basicAuth = "Basic "+ Base64.getEncoder().encodeToString(userpass.getBytes());
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

			if(debug)
				response.getWriter().println("BPRenewalListScenario response sb: " + sb.toString());

			cpiResponse = sb.toString();
			
			if (cpiResponse != null && cpiResponse.trim().length() > 0) {
				response.getWriter().println(cpiResponse);
				
				JsonObject result = new JsonObject();
				result.addProperty("BPRenewalList", cpiResponse);
				return result;
			} else {
				errorCode = "E107";
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
				return result;
			}
		}catch (Exception e) {
			errorCode="Exception";
			cpiResponse = "Error:"+e.getClass().getCanonicalName() + "--->" + e.getMessage();
			JsonObject result = new JsonObject();
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", cpiResponse);
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
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			}
			return result;
		}
	}
	public String getBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		  String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }
		  body = jb.toString();
//		  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	//added
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

package com.arteriatech.support;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AccountVerification extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private final String CPI_CONNECTION_DESTINATION = "CPIConnect";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String inputPayload = "", cpName = "", loginId = "", panFrmBp = "";
		boolean debug = false;
		CommonUtils commonUtils = new CommonUtils();
		Properties properties = new Properties();
		JsonObject resObj = new JsonObject();
		try {
			loginId = commonUtils.getUserPrincipal(request, "name", response);
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			inputPayload = commonUtils.getGetBody(request, response);
			if (debug) {
				response.getWriter().println("Received Input Payload:" + inputPayload);
			}
			
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("") && inputPayload.trim().length() > 0) {
				JsonObject jsonPayload = (JsonObject) new JsonParser().parse(inputPayload);
				String message = validateInputPayload(jsonPayload);
				if (debug) {
					response.getWriter().println("Validate Message:" + message);
				}
				if (message.equalsIgnoreCase("")) {
					String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
					String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
					String oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
					final String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
					String pcgUserpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl:" + oDataUrl);
						response.getWriter().println("password:" + password);
						response.getWriter().println("username:" + username);
					}
					String accNum = jsonPayload.get("AccountNo").getAsString();
					String cpGuid = jsonPayload.get("CPGuid").getAsString();
					String cpType = jsonPayload.get("CPTypeID").getAsString();
					String accType = jsonPayload.get("AccountType").getAsString();

					String executeURL = oDataUrl + "Approval?$filter=ProcessID%20eq%20%27" + "VERIFYACCOUNT" + "%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20ProcessReference3%20eq%20%27" + accNum + "%27%20and%20StatusID%20ne%20%27" + "999999" + "%27";
					if (debug) {
						response.getWriter().println("executeURL:" + executeURL);
					}
					JsonObject approvalObj = commonUtils.executeODataURL(executeURL, pcgUserpass, response, debug);

					if (debug) {
						response.getWriter().println("approvalObj:" + approvalObj);
					}

					if (approvalObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							// if record exist Update the
							JsonObject updateApprovalObj = approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
							if (updateApprovalObj.has("__metadata")) {
								updateApprovalObj.remove("__metadata");
							}
							updateApprovalObj.remove("StatusID");
							updateApprovalObj.addProperty("StatusID", "000010");
							executeURL = oDataUrl + "Approval('" + updateApprovalObj.get("ID").getAsString() + "')";
							if (debug) {
								response.getWriter().println("update Approval Object executeURL :" + executeURL);
								response.getWriter().println("updateApprovalObj:" + updateApprovalObj);
							}
							JsonObject updateApprivalObj = commonUtils.executeUpdate(executeURL, pcgUserpass, response, updateApprovalObj, request, debug, "PCGWHANA");
							if (debug) {
								response.getWriter().println("updateApprivalObj:" + updateApprivalObj);
							}
							if (updateApprivalObj.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
								resObj.addProperty("Status", "000001");
								resObj.addProperty("ErrorCode", "");
								resObj.addProperty("Message", "Success");
								response.getWriter().println(resObj);
							} else {
								updateApprivalObj.addProperty("Status", "000002");
								String errorMsg = updateApprivalObj.get("ErrorMessage").getAsString();
								updateApprivalObj.remove("ErrorMessage");
								updateApprivalObj.addProperty("Message", errorMsg);
								response.getWriter().println(updateApprivalObj);
							}

						} else {
							// if record not exist insert a new Record to
							// Approval Table
							// call the AccountDetails Cpi Iflow.
							// call the configtypesets
							String aggrId=properties.getProperty("PYGSTN_AGGR");
							executeURL = oDataUrl + "ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" + aggrId + "%27%20and%20Typeset%20eq%20%27" + "PYGSTN" + "%27%20and%20TypeValue%20eq%20%27" + aggregatorID + "%27";
							if (debug) {
								response.getWriter().println("ConfigTypsetTypeValues execute Url:" + executeURL);
							}
							JsonObject configTypesetRes = commonUtils.executeODataURL(executeURL, pcgUserpass, response, debug);
							if (debug) {
								response.getWriter().println("configTypesetRes:" + configTypesetRes);
							}
							if (configTypesetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
								boolean typeSetFound = false;
								if (configTypesetRes.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									typeSetFound = true;
								}
								JsonObject accuntDetailsRes = commonUtils.callAccountDetailsAPI(response, properties, aggregatorID, accNum, debug);
								if (debug) {
									response.getWriter().println("accountDetailsResp:" + accuntDetailsRes);
								}
								
								if (accuntDetailsRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
									accuntDetailsRes = accuntDetailsRes.get("API_Response").getAsJsonObject();
									//accuntDetailsRes = commonUtils.checkSpecialChar(accuntDetailsRes);
									if(debug){
										response.getWriter().println("After removal of special character account api response:"+accuntDetailsRes);
									}
									if (!accuntDetailsRes.get("accountName").isJsonNull() && !accuntDetailsRes.get("accountName").getAsString().equalsIgnoreCase("")) {
										if (!accuntDetailsRes.get("panNum").isJsonNull() && !accuntDetailsRes.get("panNum").getAsString().equalsIgnoreCase("")) {

											String pygUsername = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
											String pygPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
											String pygODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
											String userpass = pygUsername + ":" + pygPassword;
											if (debug) {
												response.getWriter().println("oDataUrl:" + pygODataUrl);
												response.getWriter().println("password:" + password);
												response.getWriter().println("username:" + username);
											}
											executeURL = pygODataUrl + "BPHeader?$filter=AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20CPGuid%20eq%20%27" + cpGuid + "%27%20and%20CPType%20eq%20%27" + cpType + "%27";
											if (debug) {
												response.getWriter().println("BPHeader executeURL:" + executeURL);
											}
											JsonObject bpResponseFrmDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);
											if (debug) {
												response.getWriter().println("bpResponseFrmDb:" + bpResponseFrmDb);
											}
											if (bpResponseFrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
												if (bpResponseFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
													JsonArray bpArray = bpResponseFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
													if (bpArray.size() == 1) {
														if (!bpArray.get(0).getAsJsonObject().get("CPName").isJsonNull()) {
															cpName = bpArray.get(0).getAsJsonObject().get("CPName").getAsString();
														}
														if (!bpArray.get(0).getAsJsonObject().get("PAN").isJsonNull()) {
															panFrmBp = bpArray.get(0).getAsJsonObject().get("PAN").getAsString();
														}
													} else {
														// if more then 1
														// records find the
														// latest BpRecord.
														JsonObject bpObj = null;
														long createdOn = 0l;
														for (int i = 0; i < bpArray.size(); i++) {
															String createdOnStr = bpArray.get(i).getAsJsonObject().get("CreatedOn").getAsString();
															createdOnStr = createdOnStr.replaceAll("/Date", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("/", "");
															if (debug) {
																response.getWriter().println("createdOnStr:" + createdOnStr);
															}
															long createdOnfrmDb = Long.parseLong(createdOnStr);
															if (createdOnfrmDb > createdOn) {
																createdOn = createdOnfrmDb;
																bpObj = bpArray.get(i).getAsJsonObject();
															}
														}
														if (debug) {
															response.getWriter().println("bpObj:" + bpObj);
														}
														if (!bpObj.get("CPName").isJsonNull()) {
															cpName = bpObj.get("CPName").getAsString();
														}
														if (!bpObj.get("PAN").isJsonNull()) {
															panFrmBp = bpObj.get("PAN").getAsString();
														}
													}
													if (panFrmBp != null && !panFrmBp.equalsIgnoreCase("")) {
														if (!cpName.equalsIgnoreCase("")) {
															if (typeSetFound) {
																if (panFrmBp.equalsIgnoreCase(accuntDetailsRes.get("panNum").getAsString())) {
																	resObj.addProperty("Approval", "Non-GSTN");
																	resObj.addProperty("Status", "000003");
																	resObj.addProperty("ErrorCode", "");
																	resObj.addProperty("Message", "Success");
																	response.getWriter().println(resObj);
																} else {
																	resObj.addProperty("Approval", "Non-GSTN");
																	resObj.addProperty("Status", "000002");
																	resObj.addProperty("ErrorCode", "PAN0001");
																	resObj.addProperty("Message", "Error: PAN Number mismatch. Please contact your ICICI bank RM for further details");
																	response.getWriter().println(resObj);
																}

															} else {
																if (commonUtils.textMatch(request, response, accuntDetailsRes.get("accountName").getAsString(), cpName, debug) && panFrmBp.equalsIgnoreCase(accuntDetailsRes.get("panNum").getAsString())) {
																	resObj.addProperty("Status", "000001");
																	resObj.addProperty("ErrorCode", "");
																	resObj.addProperty("Message", "");
																	response.getWriter().println(resObj);
																} else if (!commonUtils.textMatch(request, response, accuntDetailsRes.get("accountName").getAsString(), cpName, debug) && !panFrmBp.equalsIgnoreCase(accuntDetailsRes.get("panNum").getAsString())) {
																	resObj.addProperty("Status", "000002");
																	resObj.addProperty("ErrorCode", "PAN0001");
																	resObj.addProperty("Message", "PAN Number mismatch. Please contact your ICICI bank RM for further details");
																	response.getWriter().println(resObj);
																} else if (!panFrmBp.equalsIgnoreCase(accuntDetailsRes.get("panNum").getAsString())) {
																	resObj.addProperty("Status", "000002");
																	resObj.addProperty("ErrorCode", "PAN0001");
																	resObj.addProperty("Message", "PAN Number mismatch. Please contact your ICICI bank RM for further details");
																	response.getWriter().println(resObj);
																} else if (!commonUtils.textMatch(request, response, accuntDetailsRes.get("accountName").getAsString(), cpName, debug)) {
																	JsonObject processRef1 = new JsonObject();
																	processRef1.addProperty("iCoreAccName", accuntDetailsRes.get("accountName").getAsString());
																	processRef1.addProperty("iCorePANNo", accuntDetailsRes.get("panNum").getAsString());
																	JsonObject processRef2 = new JsonObject();
																	processRef2.addProperty("CPName", cpName);
																	processRef2.addProperty("MasterPAN", panFrmBp);
																	String id = commonUtils.generateGUID(36);
																	String createdAt = commonUtils.getCreatedAtTime();
																	long createdOn = commonUtils.getCreatedOnDate();
																	JsonObject insrtAprvalObj = new JsonObject();
																	insrtAprvalObj.addProperty("ID", id);
																	insrtAprvalObj.addProperty("AggregatorID", aggregatorID);
																	insrtAprvalObj.addProperty("ProcessID", "VERIFYACCOUNT");
																	insrtAprvalObj.addProperty("Remarks", "");
																	insrtAprvalObj.addProperty("StatusID", "000010");
																	insrtAprvalObj.addProperty("ProcessReference1", processRef1 + "");
																	insrtAprvalObj.addProperty("ProcessReference2", processRef2 + "");
																	insrtAprvalObj.addProperty("ProcessReference3", accNum);
																	insrtAprvalObj.addProperty("ProcessReference4", cpGuid);
																	insrtAprvalObj.addProperty("ProcessReference5", cpName);
																	insrtAprvalObj.addProperty("ProcessReference6", cpType);
																	insrtAprvalObj.addProperty("ProcessReference7", accType);
																	insrtAprvalObj.addProperty("CreatedBy", loginId);
																	insrtAprvalObj.addProperty("CreatedAt", createdAt);
																	insrtAprvalObj.addProperty("CreatedOn", "/Date(" + createdOn + ")/");
																	if (debug) {
																		response.getWriter().println("insrtAprvalObj:" + insrtAprvalObj);
																	}
																	executeURL = oDataUrl + "Approval";
																	if (debug) {
																		response.getWriter().println("Approval Insert Query:" + executeURL);
																	}
																	JsonObject insrtAprovalObj = commonUtils.executePostURL(executeURL, pcgUserpass, response, insrtAprvalObj, request, debug, "PCGWHANA");
																	if (debug) {
																		response.getWriter().println("insrtAprovalObj:" + insrtAprovalObj);
																	}
																	if (insrtAprovalObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
																		resObj.addProperty("Status", "000002");
																		resObj.addProperty("ErrorCode", "000010");
																		resObj.addProperty("Message", "Your account details are pending for verification. Contact your ICICI Bank RM for further clarification");
																		response.getWriter().println(resObj);

																	} else {
																		response.getWriter().println(insrtAprovalObj);
																	}

																}
															}
														} else {
															// cpName Doesn't
															// exist
															resObj.addProperty("Status", "000002");
															resObj.addProperty("ErrorCode", "J002");
															resObj.addProperty("Message", "CPName  doesn't exist in the BPHeader Table");
															response.getWriter().println(resObj);
														}
													} else {
														resObj.addProperty("Status", "000002");
														resObj.addProperty("ErrorCode", "J002");
														resObj.addProperty("Message", "PAN  doesn't exist in the BPHeader Table");
														response.getWriter().println(resObj);
													}
												} else {
													// BpRecord not found
													resObj.addProperty("Status", "000002");
													resObj.addProperty("ErrorCode", "J002");
													resObj.addProperty("Message", "BPHeader record not exist");
													response.getWriter().println(resObj);
												}
											} else {
												response.getWriter().println(bpResponseFrmDb);
											}
										} else {
											// Pan Number doesn't exist
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											resObj.addProperty("Message", "panNum doesn't exist in the AccountDetails Api");
											response.getWriter().println(resObj);
										}
									} else {
										// accountName doesn't exist in the
										// details.
										resObj.addProperty("Status", "000002");
										resObj.addProperty("ErrorCode", "J002");
										resObj.addProperty("Message", "accountName doesn't exist in the AccountDetails Api");
										response.getWriter().println(resObj);
									}
								} else {
									response.getWriter().println(accuntDetailsRes);
								}
							} else {
								response.getWriter().println(configTypesetRes);
							}
						}
					} else {
						response.getWriter().println(approvalObj);
					}

				} else {
					resObj.addProperty("Message", message);
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Message", "Empty  Input Payload Received");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				response.getWriter().println(resObj);

			}
		} catch (Exception ex) {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			JsonObject result = new JsonObject();
			String errorCode = "E173";
			String errorMsg = properties.getProperty(errorCode);
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg + ". " + ex.getClass() + ":" + ex.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	private String validateInputPayload(JsonObject jsonPayload) throws Exception {
		String message = "";
		if (jsonPayload.has("AccountNo")) {
			if (jsonPayload.get("AccountNo").isJsonNull() || jsonPayload.get("AccountNo").getAsString().equalsIgnoreCase("")) {
				message = "AccountNo Should not be empty";
				return message;
			}
		} else {
			message = "AccountNo missing";
			return message;
		}
		if (jsonPayload.has("CPGuid")) {
			if (jsonPayload.get("CPGuid").isJsonNull() || jsonPayload.get("CPGuid").getAsString().equalsIgnoreCase("")) {
				message = "CPGuid Should not be empty";
				return message;
			}
		} else {
			message = "CPGuid missing";
			return message;
		}
		if (jsonPayload.has("CPTypeID")) {
			if (jsonPayload.get("CPTypeID").isJsonNull() || jsonPayload.get("CPTypeID").getAsString().equalsIgnoreCase("")) {
				message = "CPTypeID Should not be empty";
				return message;
			}

		} else {
			message = "CPTypeID missing";
			return message;
		}

		if (jsonPayload.has("AccountType")) {
			if (jsonPayload.get("AccountType").isJsonNull() || jsonPayload.get("AccountType").getAsString().equalsIgnoreCase("")) {
				message = "AccountType Should not be empty";
				return message;
			}

		} else {
			message = "AccountType missing";
			return message;
		}

		return message;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String executeUrl = "";
		Properties props = new Properties();
		JsonObject resObj = new JsonObject();
		JsonParser parse = new JsonParser();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (request.getParameter("AccountValidation") != null && !request.getParameter("AccountValidation").equalsIgnoreCase("")) {
				props.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				final String inputPaylaod = request.getParameter("AccountValidation");
				JsonObject jsonPayload = (JsonObject) parse.parse(inputPaylaod);
				if (debug) {
					response.getWriter().println("jsonPayload:" + jsonPayload);
				}
				if (jsonPayload.has("CPNo") && !jsonPayload.get("CPNo").isJsonNull() && !jsonPayload.get("CPNo").getAsString().equalsIgnoreCase("")) {
					if (jsonPayload.has("CPTypeID") && !jsonPayload.get("CPTypeID").isJsonNull() && !jsonPayload.get("CPTypeID").getAsString().equalsIgnoreCase("")) {
						final String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
						final String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
						final String oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
						final String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
						final String userpass = username + ":" + password;
						if (debug) {
							response.getWriter().println("AggregatorID:" + aggregatorID);
						}
						if (aggregatorID != null && !aggregatorID.equalsIgnoreCase("") && !aggregatorID.equalsIgnoreCase("E112") && !aggregatorID.equalsIgnoreCase("E106")) {
							executeUrl = oDataUrl + "Approval?$filter=ProcessID%20eq%20%27VERIFYACCOUNT%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20ProcessReference4%20eq%20%27" + jsonPayload.get("CPNo").getAsString() + "%27%20and%20ProcessReference6%20eq%20%27" + jsonPayload.get("CPTypeID").getAsString() + "%27";
							if (debug) {
								response.getWriter().println("executeUrl:" + executeUrl);
							}
							JsonObject approvalObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);

							if (debug) {
								response.getWriter().println("approvalObj:" + approvalObj);
							}

							if (approvalObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								if (approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									JsonArray approvalArary = approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
						             JsonArray resArry=new JsonArray();
									for(int i=0;i<approvalArary.size();i++){
										JsonObject obj = approvalArary.get(i).getAsJsonObject();
										if(obj.has("__metadata")){
											obj.remove("__metadata");
										}
										resArry.add(obj);
						            	 
						             }
									resObj.add("Approval", resArry);
									resObj.addProperty("Status", "000001");
									resObj.addProperty("ErrorCode", "");
									resObj.addProperty("Message", "Success");
									response.getWriter().println(resObj);
								} else {
									// record not exist in the Approval Table Case
									resObj.addProperty("Message", "Records not found");
									resObj.addProperty("ErrorCode", "J0001");
									resObj.addProperty("Status", "000002");
									response.getWriter().println(resObj);
								}
							} else {
								// Unable to Fetch The records from DB.
								response.getWriter().println(approvalObj);
							}
						} else {
							// AggregatorId Not Found or Destination not exist
							resObj.addProperty("Message", "AggregatorID not exist");
							resObj.addProperty("ErrorCode", "J0002");
							resObj.addProperty("Status", "000002");
							response.getWriter().println(resObj);
						}
					} else {
						resObj.addProperty("Message", "CPTypeID missing in the input");
						resObj.addProperty("ErrorCode", "J0003");
						resObj.addProperty("Status", "000002");
						response.getWriter().println(resObj);
					}
				} else {
					resObj.addProperty("Message", "CPNo missing in the input");
					resObj.addProperty("ErrorCode", "J0003");
					resObj.addProperty("Status", "000002");
					response.getWriter().println(resObj);
				}

			} else {
				resObj.addProperty("Message", "Input payload is empty");
				resObj.addProperty("ErrorCode", "J0003");
				resObj.addProperty("Status", "000002");
				response.getWriter().println(resObj);

			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ErrorCode", "J0004");
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Status", "000002");
			response.getWriter().println(resObj);
		}

	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		JsonParser parse = new JsonParser();
		boolean debug = false;
		String executeURL = "", cpGuid = "", cpType = "", statusId = "", accountType = "", accountNo = "";
		try {
			String inputPayload = commonUtils.getGetBody(request, response);

			if (inputPayload != null && !inputPayload.equalsIgnoreCase("") && inputPayload.trim().length() > 0) {
				JsonObject jsonPayload = (JsonObject) parse.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("input Payload:" + jsonPayload);
				}
				String message = validateInput(jsonPayload);
				if (message.equalsIgnoreCase("")) {
					accountNo = jsonPayload.get("AccountNo").getAsString();
					cpGuid = jsonPayload.get("CPGuid").getAsString();
					cpType = jsonPayload.get("CPType").getAsString();
					statusId = jsonPayload.get("StatusID").getAsString();
					accountType = jsonPayload.get("AccountType").getAsString();
					String processRef1=jsonPayload.get("ProcessReference1").getAsString();
					JsonObject processRefJsonObj1=null;
					String id=jsonPayload.get("ID").getAsString();
					try{
						processRefJsonObj1=(JsonObject)parse.parse(processRef1);
					}catch(Exception ex){
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "ProcessReference1 from input Payload not in JsonFormat");
						resObj.addProperty("Status", "000001");
						response.getWriter().println(resObj);
					}
					if (processRefJsonObj1 != null) {
						final String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
						final String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
						final String oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
						final String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
						final String userpass = username + ":" + password;
						if (debug) {
							response.getWriter().println("oDataUrl:" + oDataUrl);
							response.getWriter().println("aggregatorID:" + aggregatorID);
							response.getWriter().println("userpass:" + userpass);
						}
						executeURL = oDataUrl + "Approval?$filter=AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20ProcessReference3%20eq%20%27" + accountNo + "%27%20and%20ProcessReference4%20eq%20%27" + cpGuid + "%27%20and%20ProcessReference6%20eq%20%27" + cpType + "%27%20and%20ProcessReference7%20eq%20%27" + accountType + "%27%20and%20ID%20eq%20%27"+id+"%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						JsonObject approvalObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
						if (debug) {
							response.getWriter().println("approvalObj:" + approvalObj);
						}
						if (approvalObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
							if (approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								approvalObj = approvalObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
								approvalObj.remove("__metadata");
								approvalObj.remove("StatusID");
								approvalObj.addProperty("StatusID", statusId);
								approvalObj.addProperty("ProcessReference1", processRefJsonObj1+"");
								if (debug) {
									response.getWriter().println("updated Approval Object:" + approvalObj);
								}
								executeURL = oDataUrl + "Approval('" + approvalObj.get("ID").getAsString() + "')";
								if (debug) {
									response.getWriter().println("execute Approval Object:" + executeURL);
								}
								JsonObject updatedObj = commonUtils.executeUpdate(executeURL, userpass, response, approvalObj, request, debug, "PCGWHANA");
								if (debug) {
									response.getWriter().println("updatedObj:" + updatedObj);
								}
								if (updatedObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
									resObj.addProperty("ErrorCode", "");
									resObj.addProperty("Message", "");
									resObj.addProperty("Status", "000001");
									response.getWriter().println(resObj);
								} else {
									response.getWriter().println(updatedObj);
								}
							} else {
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", "No records found for the input");
								resObj.addProperty("Status", "000002");
								response.getWriter().println(resObj);
							}
						} else {
							response.getWriter().println(approvalObj);
						}
					}
				} else {
					resObj.addProperty("ErrorCode", "J001");
					resObj.addProperty("Message", message);
					resObj.addProperty("Status", "000002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("ErrorCode", "J001");
				resObj.addProperty("Message", "Empty Input Payload received");
				resObj.addProperty("Status", "000002");
				response.getWriter().println(resObj);
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ErrorCode", "J001");
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Status", "000002");
			response.getWriter().println(resObj);

		}

	}

	private String validateInput(JsonObject jsonPayload) {
		String message = "";
		if (jsonPayload.has("AccountNo")) {
			if (jsonPayload.get("AccountNo").isJsonNull() || jsonPayload.get("AccountNo").getAsString().equalsIgnoreCase("")) {
				message = "AccountNo Should not be null or empty";
				return message;
			}

		} else {
			message = "AccountNo missing in the input";
			return message;
		}

		if (jsonPayload.has("CPGuid")) {
			if (jsonPayload.get("CPGuid").isJsonNull() || jsonPayload.get("CPGuid").getAsString().equalsIgnoreCase("")) {
				message = "CPGuid Should not be null or empty";
				return message;
			}

		} else {
			message = "CPGuid missing in the input";
			return message;
		}

		if (jsonPayload.has("CPType")) {
			if (jsonPayload.get("CPType").isJsonNull() || jsonPayload.get("CPType").getAsString().equalsIgnoreCase("")) {
				message = "CPType Should not be null or empty";
				return message;
			}

		} else {
			message = "CPType missing in the input";
			return message;
		}

		if (jsonPayload.has("StatusID")) {
			if (jsonPayload.get("StatusID").isJsonNull() || jsonPayload.get("StatusID").getAsString().equalsIgnoreCase("")) {
				message = "StatusID Should not be null or empty";
				return message;
			}

		} else {
			message = "StatusID missing in the input";
			return message;
		}

		if (jsonPayload.has("AccountType")) {
			if (jsonPayload.get("AccountType").isJsonNull() || jsonPayload.get("AccountType").getAsString().equalsIgnoreCase("")) {
				message = "AccountType Should not be null or empty";
				return message;
			}
		} else {
			message = "AccountType missing in the input";
			return message;
		}
		
		if (jsonPayload.has("ProcessReference1")) {
			if (jsonPayload.get("ProcessReference1").isJsonNull() || jsonPayload.get("ProcessReference1").getAsString().equalsIgnoreCase("")) {
				message = "ProcessReference1 Should not be null or empty";
				return message;
			}
		} else {
			message = "ProcessReference1 missing in the input";
			return message;
		}
		
		if (jsonPayload.has("ID")) {
			if (jsonPayload.get("ID").isJsonNull() || jsonPayload.get("ID").getAsString().equalsIgnoreCase("")) {
				message = "ID Should not be null or empty";
				return message;
			}
		} else {
			message = "ID missing in the input";
			return message;
		}
		return message;
	}

	private boolean checkDataInJsonForm(String processRef1) {
		JsonParser parser = new JsonParser();
		try {
			parser.parse(processRef1);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}

}

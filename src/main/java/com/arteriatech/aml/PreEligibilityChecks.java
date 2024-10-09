package com.arteriatech.aml;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PreEligibilityChecks extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = "", password = "", userpass = "", oDataURL = "", executeURL = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		boolean debug = false;
		String cpGuid = "", cpType = "", aggregatorID = "";
		Properties properties = new Properties();
		AMLUtils amlUtils = new AMLUtils();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (request.getParameter("BPGuid") != null && !request.getParameter("BPGuid").equalsIgnoreCase("")) {
				String bpGUID = request.getParameter("BPGuid");
				if (debug) {
					response.getWriter().println("BpGUID Received from UI:" + bpGUID);
				}
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userpass = username + ":" + password;
				executeURL = oDataURL + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=BPID%20eq%20%27" + bpGUID + "%27";
				if (debug) {
					response.getWriter().println("executeURL:" + executeURL);
				}
				JsonObject odataResObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);

				if (debug) {
					response.getWriter().println("BPEligibility Response:" + odataResObj);
				}
				if (odataResObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
					JsonObject eligibleRecObj = odataResObj.get("Message").getAsJsonObject();
					JsonArray eligibleArray = eligibleRecObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					if (eligibleArray.size() > 0) {
						JsonObject bpEligibilObj = eligibleArray.get(0).getAsJsonObject();
						aggregatorID = bpEligibilObj.get("AggregatorID").getAsString();
						cpGuid = bpEligibilObj.get("CPGuid").getAsString();
						cpType = bpEligibilObj.get("CPType").getAsString();
						if (debug) {
							response.getWriter().println("AggregatorID:" + aggregatorID);
							response.getWriter().println("cpGuid:" + cpGuid);
							response.getWriter().println("cpType:" + cpType);
						}

						if (bpEligibilObj.get("CorrelationID").isJsonNull() || bpEligibilObj.get("CorrelationID").getAsString().equalsIgnoreCase("")) {
							resObj.addProperty("Message", "No Records found");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "AML00002");
							response.getWriter().println(resObj);
						} else {
							// if correlationID not null or not
							// Empty.
							JsonArray bpcntpArry = bpEligibilObj.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
							if (bpcntpArry.size() > 0) {
								JsonObject checkAMLEligibilitRes = amlUtils.checkAMLEligibility(response, debug, bpEligibilObj, bpcntpArry, properties);
								response.getWriter().println(checkAMLEligibilitRes);
							} else {
								resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(resObj);
							}
						}

					} else {
						resObj.addProperty("Message", "BP Not Registered");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "AML00001");
						response.getWriter().println(resObj);
					}

				} else {
					response.getWriter().println(odataResObj);
				}
			} else {
				// Input Payload doesn't Contains BPGuid.
				resObj.addProperty("Message", "BPGuid Missing in the Input Payload");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
		}
	}

	private JsonObject getSCCNFGRecords(String OdataUrl, String userpass, String aggregatorID, HttpServletResponse response, boolean debug) {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		try {
			String executeURL = OdataUrl + "SCCNFG?$filter=AGGRID%20eq%20%27" + aggregatorID + "%27";
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject sccnfgObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("sccnfgObj:" + sccnfgObj);
			}
			return sccnfgObj;
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			return resObj;
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonObject resObj = new JsonObject();
		JsonParser parser = new JsonParser();
		boolean debug = false;
		String executeURL = "", url = "", username = "", password = "", userpass = "", aggregatorID = "";
		AMLUtils amlUtils = new AMLUtils();
		Properties properties = new Properties();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonInputPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonInputPayload.has("debug") && jsonInputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				// debug=true;
				if (debug) {
					response.getWriter().println("Input Payload received from UI:" + jsonInputPayload);
				}
				if (!jsonInputPayload.get("BPGuid").isJsonNull() && !jsonInputPayload.get("BPGuid").getAsString().equalsIgnoreCase("")) {
					String bpGuid = jsonInputPayload.get("BPGuid").getAsString();
					url = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
					userpass = username + ":" + password;
					executeURL = url + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=BPID%20eq%20%27" + bpGuid + "%27";
					if (debug) {
						response.getWriter().println("executeURL:" + executeURL);
					}
					JsonObject odataResObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);

					if (debug) {
						response.getWriter().println("odataResObj:" + odataResObj);
					}
					if (odataResObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						JsonObject bpEligibileRec = odataResObj.get("Message").getAsJsonObject();
						if (bpEligibileRec.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							bpEligibileRec = bpEligibileRec.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
							if (debug) {
								response.getWriter().println("eligibilityRoc:" + bpEligibileRec);
							}
							if (!bpEligibileRec.get("CorrelationID").isJsonNull() && !bpEligibileRec.get("CorrelationID").getAsString().equalsIgnoreCase("")) {
								JsonArray bpcntpArry = bpEligibileRec.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
								if (bpcntpArry.size() > 0) {
									JsonObject amlEligibleRes = amlUtils.checkAMLEligibility(response, debug, bpEligibileRec, bpcntpArry, properties);
									amlEligibleRes.addProperty("AggregatorID", aggregatorID);
									response.getWriter().println(amlEligibleRes);
								} else {
									resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									resObj.addProperty("AggregatorID", aggregatorID);
									response.getWriter().println(resObj);
								}

							} else {
								// else block for if CorrelationID is empty or null
								if (debug) {
									response.getWriter().println("CorrelationID is empty in the Eligibility Records");
								}
								aggregatorID = bpEligibileRec.get("AggregatorID").getAsString();
								String cpGuid = bpEligibileRec.get("CPGuid").getAsString();
								String cpType = bpEligibileRec.get("CPType").getAsString();
								executeURL = url + "BPHeaders?$expand=BPContactPersons&$filter=CPGuid%20eq%20%27" + cpGuid + "%27%20and%20CPType%20eq%20%27" + cpType + "%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27";
								if (debug) {
									response.getWriter().println("BPHeaders executeURL: " + executeURL);
								}

								JsonObject BpHeaderDbObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
								if (debug) {
									response.getWriter().println("BP Records:" + BpHeaderDbObj);
								}

								if (BpHeaderDbObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
									JsonObject bpHeaderRecord = BpHeaderDbObj.get("Message").getAsJsonObject();
									JsonArray bpHeaderArray = bpHeaderRecord.get("d").getAsJsonObject().get("results").getAsJsonArray();
									if (bpHeaderArray.size() > 0) {
										JsonObject newBpEligibilityRec = null;
										JsonObject bpRecord = null;
										boolean bpELigibilityRecFound = false;
										for (int i = 0; i < bpHeaderArray.size(); i++) {
											bpRecord = bpHeaderArray.get(i).getAsJsonObject();
											String bpGUid = bpRecord.get("BPGuid").getAsString();
											if (debug) {
												response.getWriter().println("BpGuid:" + bpGUid);
											}
											executeURL = url + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=BPID%20eq%20%27" + bpGUid + "%27";
											if (debug) {
												response.getWriter().println("EligibilityRecords executeUrl:" + executeURL);
											}
											JsonObject bpEligibilityFrmDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);
											if (debug) {
												response.getWriter().println("eligibleRocFromDb:" + bpEligibilityFrmDb);
											}

											if (bpEligibilityFrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
												bpEligibilityFrmDb = bpEligibilityFrmDb.get("Message").getAsJsonObject();
												if (bpEligibilityFrmDb.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
													newBpEligibilityRec = bpEligibilityFrmDb.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
													if (!newBpEligibilityRec.get("CorrelationID").isJsonNull() && !newBpEligibilityRec.get("CorrelationID").getAsString().equalsIgnoreCase("")) {
														bpELigibilityRecFound = true;
														break;
													}
												}
											} else {
												throw new FailedToFetchRecordException(bpEligibilityFrmDb, "Unable to Fetch the records from the EligibilityRecords Table", "000002", "J002");
											}
										}
										if (debug) {
											response.getWriter().println("bpELigibilityRecFound :" + bpELigibilityRecFound);
											response.getWriter().println("bpRecord :" + bpRecord);
											response.getWriter().println("newBpEligibilityRec :" + newBpEligibilityRec);

										}
										JsonObject latestBPrecord = null;
										long createdOn = 0l;
										for (int i = 0; i < bpHeaderArray.size(); i++) {
											bpRecord = bpHeaderArray.get(i).getAsJsonObject();
											String strCreatedOn = bpRecord.get("CreatedOn").getAsString();
											strCreatedOn = strCreatedOn.replaceAll("/Date", "").replaceAll("/", "").replaceAll("\\(", "").replaceAll("\\)", "");
											long createdOnMilliSec = Long.parseLong(strCreatedOn);
											if (createdOnMilliSec > createdOn) {
												latestBPrecord = bpRecord;
												createdOn = createdOnMilliSec;
											}
										}

										if (debug) {
											response.getWriter().println("Latest BPRecord:" + latestBPrecord);
										}

										if (!bpELigibilityRecFound) {
											// Find the latest BPHeader and
											// BPContractPerson Record. insert a
											// new Record in to the eligibility
											// Table.
											JsonObject insertedEligibilityRec = amlUtils.insertEligibilityRecord(request, url, userpass, latestBPrecord, response, debug);
											if (debug) {
												response.getWriter().println("Insert in to eligibility Records Table response:" + insertedEligibilityRec);
											}

											// inserting to EligibilityRecords
											// Table success then try to insert
											// a records to SCF1 Table.
											if (insertedEligibilityRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
												JsonObject insertScfResponse = amlUtils.insertIntoSCF1Table(cpGuid, cpType, aggregatorID, url, userpass, debug, response, request);
												if (debug) {
													response.getWriter().println("insertScfResponse:" + insertScfResponse);
												}
												if (insertScfResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
													String newBpGuid = bpEligibileRec.get("BPID").getAsString();
													executeURL = url + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=BPID%20eq%20%27" + newBpGuid + "%27";
													if (debug) {
														response.getWriter().println("BPCNTPEligibilityRecords:" + executeURL);
													}
													JsonObject bpctpRec = commonUtils.executeODataURL(executeURL, userpass, response, debug);
													if (debug) {
														response.getWriter().println("bpctpRec:" + bpctpRec);
													}
													if (bpctpRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
														if (bpctpRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
															JsonObject bpEligibleRec = bpctpRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
															if (bpEligibleRec.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
																JsonArray bpCntpEligibleArr = bpEligibleRec.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
																JsonObject checkAMLEligibility = amlUtils.checkAMLEligibility(response, debug, bpEligibleRec, bpCntpEligibleArr, properties);
																if (debug) {
																	response.getWriter().println("checkAMLEligibility Response:" + checkAMLEligibility);
																}
																if (checkAMLEligibility.get("Status").getAsString().equalsIgnoreCase("000002") && checkAMLEligibility.get("ErrorCode").getAsString().equalsIgnoreCase("AML00003")) {
																	checkAMLEligibility.remove("ErrorCode");
																	checkAMLEligibility.addProperty("ErrorCode", "AML00006");
																	checkAMLEligibility.addProperty("AggregatorID", aggregatorID);
																	response.getWriter().println(checkAMLEligibility);
																} else {
																	checkAMLEligibility.addProperty("AggregatorID", aggregatorID);
																	response.getWriter().println(checkAMLEligibility);
																}
															} else {
																resObj.addProperty("AggregatorID", aggregatorID);
																resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
																resObj.addProperty("Status", "000002");
																resObj.addProperty("ErrorCode", "J002");
																response.getWriter().println(resObj);
															}

														} else {
															resObj.addProperty("AggregatorID", aggregatorID);
															resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
															resObj.addProperty("Status", "000002");
															resObj.addProperty("ErrorCode", "J002");
															response.getWriter().println(resObj);

														}
													} else {
														bpctpRec.addProperty("AggregatorID", aggregatorID);
														response.getWriter().println(bpctpRec);
													}
												} else {
													// delete the ELigibility
													// Records
													JsonArray eligibilityarray = insertedEligibilityRec.get("Message").getAsJsonArray();
													JsonObject deletedResponse = amlUtils.deleteEligibilityRecords(url, userpass, debug, response, eligibilityarray);
													if (debug) {
														response.getWriter().println("deletedResponse:" + deletedResponse);
													}
													if (deletedResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
														resObj.addProperty("Message", "Currently we are facing technical issues. Please try after sometime");
														resObj.addProperty("Status", "000002");
														resObj.addProperty("ErrorCode", "AML00005");
														resObj.addProperty("AggregatorID", aggregatorID);
														response.getWriter().println(resObj);
													} else {
														deletedResponse.addProperty("AggregatorID", aggregatorID);
														response.getWriter().println(deletedResponse);
													}
												}
											} else {
												resObj.addProperty("Message", "Currently we are facing technical issues. Please try after sometime");
												resObj.addProperty("Status", "000002");
												resObj.addProperty("ErrorCode", "AML00012");
												resObj.addProperty("AggregatorID", aggregatorID);
												response.getWriter().println(resObj);
											}

										} else {

											// check if record is already exist. if record exist delete the existing entry and insert a new record.

											String correlationId = newBpEligibilityRec.get("CorrelationID").getAsString();
											if (debug) {
												response.getWriter().println("correlationId:" + correlationId);
											}
											JsonObject deleteResponse = amlUtils.deleteEligibilityRecords(debug, response, correlationId, url, userpass, commonUtils);
											if (debug) {
												response.getWriter().println("deleteResponse:" + deleteResponse);
											}

											if (deleteResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
												JsonObject insertedEligibilityRec = amlUtils.insertEligibilityRecord(request, url, userpass, latestBPrecord, response, debug);
												if (debug) {
													response.getWriter().println("Insert in to eligibility Records Table response:" + insertedEligibilityRec);
												}
												if (insertedEligibilityRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
													JsonObject insertScfResponse = amlUtils.insertIntoSCF1Table(cpGuid, cpType, aggregatorID, url, userpass, debug, response, request);
													if (debug) {
														response.getWriter().println("insertScfResponse:" + insertScfResponse);
													}
													if (insertScfResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
														String updatedBPguid = latestBPrecord.get("BPGuid").getAsString();
														if (debug) {
															response.getWriter().println("updatedBPguid:" + updatedBPguid);
														}

														executeURL = url + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=BPID%20eq%20%27" + updatedBPguid + "%27";
														if (debug) {
															response.getWriter().println("executeURL:" + executeURL);
														}
														JsonObject updatedBpEligibilityRec = commonUtils.executeODataURL(executeURL, userpass, response, debug);

														if (debug) {
															response.getWriter().println("updatedBpEligibilityRec:" + updatedBpEligibilityRec);
														}
														if (updatedBpEligibilityRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
															if (updatedBpEligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
																updatedBpEligibilityRec = updatedBpEligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
																JsonArray bpcntpArr = updatedBpEligibilityRec.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
																if (bpcntpArr.size() > 0) {
																	JsonObject amlEligibleRes = amlUtils.checkAMLEligibility(response, debug, updatedBpEligibilityRec, bpcntpArr, properties);
																	if (debug) {
																		response.getWriter().println("amlEligibleRes:" + amlEligibleRes);
																	}
																	if (amlEligibleRes.get("Status").getAsString().equalsIgnoreCase("000002") && amlEligibleRes.get("ErrorCode").getAsString().equalsIgnoreCase("AML00003")) {
																		amlEligibleRes.remove("ErrorCode");
																		amlEligibleRes.addProperty("ErrorCode", "AML00008");
																	} else {
																		amlEligibleRes.addProperty("AggregatorID", aggregatorID);
																		response.getWriter().println(amlEligibleRes);
																	}
																} else {
																	resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
																	resObj.addProperty("Status", "000002");
																	resObj.addProperty("ErrorCode", "J002");
																	resObj.addProperty("AggregatorID", aggregatorID);
																	response.getWriter().println(resObj);
																}
															} else {
																resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
																resObj.addProperty("Status", "000002");
																resObj.addProperty("ErrorCode", "J002");
																resObj.addProperty("AggregatorID", aggregatorID);
																response.getWriter().println(resObj);
															}
														}
													} else {
														JsonArray eligibilityarray = insertedEligibilityRec.get("Message").getAsJsonArray();
														JsonObject deletedResponse = amlUtils.deleteEligibilityRecords(url, userpass, debug, response, eligibilityarray);
														if (debug) {
															response.getWriter().println("deletedResponse:" + deletedResponse);
														}
														if (deletedResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
															resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
															resObj.addProperty("Status", "000002");
															resObj.addProperty("ErrorCode", "AML00005");
															resObj.addProperty("AggregatorID", aggregatorID);
															response.getWriter().println(resObj);
														} else {
															deletedResponse.addProperty("AggregatorID", aggregatorID);
															response.getWriter().println(deletedResponse);
														}

													}
												} else {
													// insertedEligibilityRec
													response.getWriter().println(insertedEligibilityRec);
												}

											} else {
												response.getWriter().println(deleteResponse);
											}

										}
									} else {
										resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
										resObj.addProperty("Status", "000002");
										resObj.addProperty("ErrorCode", "J002");
										resObj.addProperty("AggregatorID", aggregatorID);
										response.getWriter().println(resObj);
									}

								} else {
									BpHeaderDbObj.addProperty("AggregatorID", aggregatorID);
									response.getWriter().println(BpHeaderDbObj);
								}
							}

						} else {
							resObj.addProperty("Message", "Registration Not Complete");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "AML00004");
							resObj.addProperty("AggregatorID", aggregatorID);
							response.getWriter().println(resObj);
						}

					} else {
						odataResObj.addProperty("AggregatorID", aggregatorID);
						response.getWriter().println(odataResObj);
					}

				} else {
					resObj.addProperty("Message", "Input Payload doesn't contains a BPGuid");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("AggregatorID", aggregatorID);
					response.getWriter().println(resObj);

				}

			} else {
				resObj.addProperty("Message", "Empty input Payload received from UI");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("AggregatorID", aggregatorID);
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			if (ex instanceof FailedToFetchRecordException) {
				FailedToFetchRecordException e = (FailedToFetchRecordException) ex;
				resObj.addProperty("Message", e.getMessage());
				resObj.addProperty("Status", e.getStatus());
				resObj.addProperty("ErrorCode", e.getErrorCode());
				resObj.addProperty("AggregatorID", aggregatorID);
				JsonObject errorObj = e.getErrorObj();
				if (errorObj.has("ExceptionMessage")) {
					resObj.addProperty("ErrorMessge", errorObj.get("ExceptionMessage").getAsString());
				} else {
					resObj.add("ErrorMessge", errorObj);
				}
				response.getWriter().println(resObj);
			} else {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("Message", ex.getLocalizedMessage());
				resObj.addProperty("ExceptionTrace", buffer.toString());
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("AggregatorID", aggregatorID);
				response.getWriter().println(resObj);
			}
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "", recordID = "", status = "";
		JsonObject resObj = new JsonObject();
		JsonParser parser = new JsonParser();
		boolean debug = false;
		String odataUrl = "", username = "", password = "", aggregatorID = "", userpass = "", executeURL = "";
		AMLUtils amlUtils = new AMLUtils();
		Properties properties = new Properties();
		try {
			String inputApiKey = "";
			if (request.getHeader("x-arteria-apikey") != null && !request.getHeader("x-arteria-apikey").equalsIgnoreCase("")) {
				inputApiKey = request.getHeader("x-arteria-apikey");
			}
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			String apiKeyfrmProps = properties.getProperty("ART_API_Key");
			if (inputApiKey.equalsIgnoreCase(apiKeyfrmProps)) {
				inputPayload = commonUtils.getGetBody(request, response);
				if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
					JsonObject jsonInputPayload = (JsonObject) parser.parse(inputPayload);
					if (jsonInputPayload.has("debug") && jsonInputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
						debug = true;
					}
					if (debug) {
						response.getWriter().println("Received input Payload:" + jsonInputPayload);
					}
					JsonObject validateInput = validateInputPayload(request, jsonInputPayload);
					if (debug) {
						response.getWriter().println("validateInput:" + validateInput);
					}
					if (validateInput.get("Status").getAsString().equalsIgnoreCase("000001")) {
						recordID = jsonInputPayload.get("RecordID").getAsString();
						status = jsonInputPayload.get("Status").getAsString();
						status = status.replaceAll("001", "").replaceAll("1ICICI", "");
						if (debug) {
							response.getWriter().println("status value after truncating:" + status);
						}
						//aggregatorID = request.getHeader("x-arteria-aggr");
						odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
						username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
						String destAggrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
						userpass = username + ":" + password;
						executeURL = odataUrl + "Eligibility?$filter=RecordID%20eq%20%27" + recordID + "%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						JsonObject eligibilityRec = commonUtils.executeODataURL(executeURL, userpass, response, debug);
						if (debug) {
							response.getWriter().println("eligibilityRec:" + eligibilityRec);
						}
						if (eligibilityRec.get("Status").getAsString().equalsIgnoreCase("000001")) {
							if (eligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								eligibilityRec = eligibilityRec.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
								String aggerID = eligibilityRec.get("AggregatorID").getAsString();
								JsonObject pgPaymentConfig = amlUtils.getAMLPGPaymentConfigs(response, debug);
								if (debug) {
									response.getWriter().println("pgPaymentConfig:" + pgPaymentConfig);
									response.getWriter().println("aggerID:" + aggerID);
								}
								if (pgPaymentConfig.get("Status").getAsString().equalsIgnoreCase("000001")) {
									if (pgPaymentConfig.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
										pgPaymentConfig = pgPaymentConfig.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
										String pgConfigGuid = pgPaymentConfig.get("ConfigHeaderGUID").getAsString();
										JsonObject paymntConfigSts = amlUtils.getAMLPGPaymentConfigStats(pgConfigGuid, status, response, debug);
										if (debug) {
											response.getWriter().println("aml pgPaymentConfigStats:" + paymntConfigSts);
										}
										JsonObject configObj = amlUtils.getValidToDate(response, commonUtils, debug);
										if (configObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
											if (configObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
												configObj = configObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
												String additionalDays = configObj.get("TypeValue").getAsString();
												if (debug) {
													response.getWriter().println("additionalDays:" + additionalDays);
												}
												if (paymntConfigSts.get("Status").getAsString().equalsIgnoreCase("000001")) {
													if (paymntConfigSts.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
														paymntConfigSts = paymntConfigSts.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
														String paymentStatus = paymntConfigSts.get("PymntStatus").getAsString();
														if (debug) {
															response.getWriter().println("paymentStatus:" + paymentStatus);
														}
														if (paymentStatus.equalsIgnoreCase("000050")) {
															JsonObject updateEligibilityRecords = amlUtils.updateEligibilityRecords(status, odataUrl, userpass, eligibilityRec, paymentStatus, response, request, "200030", "200020", aggerID, additionalDays, properties, debug);
															updateEligibilityRecords.addProperty("AggregatorID", aggerID);
															response.getWriter().println(updateEligibilityRecords);
														} else if (paymentStatus.equalsIgnoreCase("000060")) {
															JsonObject updateEligibilityRecords = amlUtils.updateEligibilityRecords(status, odataUrl, userpass, eligibilityRec, paymentStatus, response, request, "200040", "200020", aggerID, additionalDays, properties, debug);
															updateEligibilityRecords.addProperty("AggregatorID", aggerID);
															response.getWriter().println(updateEligibilityRecords);
														}
													} else {
														resObj.addProperty("AggregatorID", aggerID);
														resObj.addProperty("Message", "Unknown status");
														resObj.addProperty("Status", "000002");
														resObj.addProperty("ErrorCode", "AML00010");
														response.getWriter().println(resObj);
													}
												} else {
													paymntConfigSts.addProperty("AggregatorID", aggerID);
													response.getWriter().println(paymntConfigSts);
												}
											} else {
												// type set doesn't exist
												resObj.addProperty("Message", "Type set doesn't exist");
												resObj.addProperty("Status", "000002");
												resObj.addProperty("ErrorCode", "J002");
												resObj.addProperty("AggregatorID", aggerID);
												response.getWriter().println(resObj);
											}
										} else {
											configObj.addProperty("AggregatorID", aggerID);
											response.getWriter().println(configObj);
										}
									} else {
										resObj.addProperty("AggregatorID", aggerID);
										resObj.addProperty("Message", "Configurations Not Found");
										resObj.addProperty("Status", "000002");
										resObj.addProperty("ErrorCode", "AML00008");
										response.getWriter().println(resObj);
									}
								} else {
									response.getWriter().println(pgPaymentConfig);
								}
							} else {
								resObj.addProperty("Message", "EligibilityRecords doesn't exist for the given input RecordID:" + recordID);
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(resObj);

							}

						} else {
							response.getWriter().println(eligibilityRec);
						}

					} else {
						// input validation else block
						response.getWriter().println(validateInput);
					}
				} else {
					resObj.addProperty("Message", "Empty input Payload received");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Message", "invalid Api Key");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getCause().getMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
		}
	}

	private JsonObject validateInputPayload(HttpServletRequest request, JsonObject jsonInputPayload) {
		String message = "";
		JsonObject resObj = new JsonObject();
		try {
			if (request.getHeader("x-arteria-aggr") == null || request.getHeader("x-arteria-aggr").equalsIgnoreCase("")) {
				message = "Header doesn't contains a x-arteria-aggr Field";
			}
			if (jsonInputPayload.has("RecordID")) {
				if (jsonInputPayload.get("RecordID").isJsonNull() || jsonInputPayload.get("RecordID").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "RecordID property empty in the input Payload";
					} else {
						message = message + ",RecordID property empty in the input Payload";
					}
				}
			} else {
				if (message.equalsIgnoreCase("")) {
					message = "Input Payload doesn't contains a RecordID Property";
				} else {
					message = message + ",Input Payload doesn't contains a RecordID Property";
				}
			}
			if (jsonInputPayload.has("Status")) {
				if (jsonInputPayload.get("Status").isJsonNull() || jsonInputPayload.get("Status").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "Status Property empty in the input Payload";
					} else {
						message = message + ",Status Property empty in the input Payload";
					}
				}
			} else {
				if (message.equalsIgnoreCase("")) {
					message = "Input Payload doesn't contains a Status Property";
				} else {
					message = message + ",Input Payload doesn't contains a Status Property";
				}
			}
			if (message.equalsIgnoreCase("")) {
				resObj.addProperty("Message", "");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
			} else {
				resObj.addProperty("Message", message);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
			}
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionStackTrace", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;
		}
	}

}

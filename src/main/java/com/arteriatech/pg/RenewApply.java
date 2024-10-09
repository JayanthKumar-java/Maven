package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

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
import com.google.gson.JsonObject;

/**
 * Servlet implementation class RenewApply
 */
@WebServlet("/RenewApply")
public class RenewApply extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RenewApply() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try {
			boolean debug = false;
			if (debug) {
				response.getWriter().println("Inside doGet()");
			}
			/*
			 * String customerNo = "", partnerNames = "", dealerAddr1 = "",
			 * dealerAddr2 = "", dealerAddr3 = "", dealerAddr4 = "", city = "",
			 * constitutionType = "", dealerPAN = "", sanctionLimit = "",
			 * caAccount = "", odAccount = "", dealerName = "", proposedLimit =
			 * "", errorCode = "", errorMsg = "", merchantCode = "", acceptedAmt
			 * = "", ipAddress = "", status = "", sessionID = "", wsURL = "";
			 */
			String corpID = "", constitutionType = "", errorCode = "", errorMsg = "", customerNo = "", sessionID = "",
					corporateName = "", odAccount = "", ipAddress = "", currentDate = "", currentTime = "",
					partnerAccount = "", dateOfIncorporation = "", wsURL = "", system = "", userPass = "",
					userHistory = "";

			String payLoad = request.getParameter("RenewalApply");
			if (debug) {
				response.getWriter().println("payLoad: " + payLoad);
			}
			JSONObject jsonObject = new JSONObject(payLoad);
			CommonUtils commonUtils = new CommonUtils();
			if (null != payLoad && payLoad.trim().length() > 0 && payLoad != "") {
				String loginID = commonUtils.getLoginID(request, response, debug);
				// if (request.getUserPrincipal() != null) {
				if (loginID != null) {
					if (loginID == null) {
						errorCode = "E125";
					} else {
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
				} else {
					errorCode = "E125";
					if (debug)
						response.getWriter().println("Generating sessionID - errorCode:" + errorCode);
				}

				if (null != jsonObject.getString("CustomerNo")) {
					customerNo = jsonObject.getString("CustomerNo");
				} else {
					errorCode = "E100";
					errorMsg = properties.getProperty(errorCode);
				}

				/*
				 * corpID = commonUtils.readDestProperties("CorpCode");
				 * 
				 * if (corpID.equalsIgnoreCase("E112")) { errorCode = corpID; }
				 */

				if (errorCode != null & errorCode.trim().length() == 0) {
					errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);

					if (errorCode != null & errorCode.trim().length() == 0) {

						// Input validations here
						JSONObject searchRenewalApply = (JSONObject) jsonObject.get("RenewalApply");
						if (debug) {
							response.getWriter().println("searchRenewalApply: " + searchRenewalApply);
						}

						constitutionType = searchRenewalApply.getString("ConstitutionType");

						if (constitutionType != null && (!constitutionType.equalsIgnoreCase(properties.getProperty("ProprietorConstitutionType"))
								|| !constitutionType.equalsIgnoreCase(properties.getProperty("PartnershipConstitutionType")))) {
							errorCode = "E142";
							errorMsg = properties.getProperty(errorCode);

							JsonObject result = new JsonObject();
							result.addProperty("errorCode", errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						} else {
							corporateName = searchRenewalApply.getString("CorporateName");
							if (corporateName != null && corporateName.trim().length() == 0
									&& corporateName.trim().equalsIgnoreCase("")) {
								errorCode = "E144";
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								odAccount = searchRenewalApply.getString("ODAccount");
								if (odAccount != null && odAccount.trim().length() == 0
										&& odAccount.trim().equalsIgnoreCase("")) {
									errorCode = "E155";
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								ipAddress = searchRenewalApply.getString("IPAddress");
								if (ipAddress != null && ipAddress.trim().length() == 0
										&& ipAddress.trim().equalsIgnoreCase("")) {
									errorCode = "E145";
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								currentDate = searchRenewalApply.getString("CurrentDate");
								if (currentDate != null && currentDate.trim().length() == 0
										&& currentDate.trim().equalsIgnoreCase("")) {
									errorCode = "E146";
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								currentTime = searchRenewalApply.getString("CurrentTime");
								if (currentTime != null && currentTime.trim().length() == 0
										&& currentTime.trim().equalsIgnoreCase("")) {
									errorCode = "E151";
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								partnerAccount = searchRenewalApply.getString("PartnerAccount");
								if (partnerAccount != null && partnerAccount.trim().length() == 0
										&& partnerAccount.trim().equalsIgnoreCase("")) {
									errorCode = "E147";
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								dateOfIncorporation = searchRenewalApply.getString("DateOfIncorporation");
								if (dateOfIncorporation != null && dateOfIncorporation.trim().length() == 0
										&& dateOfIncorporation.trim().equalsIgnoreCase("")) {
									errorCode = "E148";
								} else {
									errorCode = commonUtils.validateInput(response, "DateOfIncorporation", dateOfIncorporation, debug);
								}
							}

							if (errorCode != null && errorCode.trim().length() == 0
									&& errorCode.trim().equalsIgnoreCase("")) {
								JSONArray arrayPartnerList = (JSONArray) searchRenewalApply.getJSONArray("PartnerList");
								JSONArray arraySignerList = (JSONArray) searchRenewalApply.getJSONArray("SignerDetails");
								if (debug) {
									response.getWriter().println("arrayPartnerList: " + arrayPartnerList);
									response.getWriter().println("arrayPartnerList-length: " + arrayPartnerList.length());

									response.getWriter().println("arraySignerList: " + arraySignerList);
									response.getWriter().println("arraySignerList-length: " + arraySignerList.length());
								}

								String masterField = "", childKey = "", childValue = "";
								for (int i = 0; i <= arrayPartnerList.length() - 1; i++) {
									JSONObject partnerListJsonObj = (JSONObject) arrayPartnerList.get(i);

									if (debug)
										response.getWriter().println("partnerListJsonObj---names: " + partnerListJsonObj.names().length());

									for (int j = 1; j <= partnerListJsonObj.names().length(); j++) {
										masterField = partnerListJsonObj.names().get(j).toString();
										JSONArray arrayPartnerListCount = (JSONArray) partnerListJsonObj.getJSONArray(masterField);
										if (debug)
											response.getWriter().println(j + " arrayPartnerListCount---length: "+ arrayPartnerListCount.length());
										for (int k = 0; k <= arrayPartnerListCount.length() - 1; k++) {
											JSONObject partnerListCountJsonObj = (JSONObject) arrayPartnerListCount.get(i);
											if (debug) {
												response.getWriter().println("partnerListCountJsonObj---names: "+ partnerListCountJsonObj.names());
												response.getWriter().println("partnerListCountJsonObj---names Length: "+ partnerListCountJsonObj.names().length());
											}

											for (int l = 0; l < partnerListCountJsonObj.names().length(); l++) {
												childKey = partnerListCountJsonObj.names().get(l).toString();
												childValue = partnerListCountJsonObj.getString(childKey);
												if (debug) {
													response.getWriter().println("Partner" + l + "----> childField Key--->: " + childKey);
													response.getWriter().println("Partner" + l + "----> childField Value--->: " + childValue);
												}

												errorCode = commonUtils.validateInput(response, childKey, childValue, debug);

												if (errorCode != null && errorCode.trim().length() > 0) {
													break;
												}
											}

											if (errorCode != null && errorCode.trim().length() > 0) {
												break;
											}
										}

										if (errorCode != null && errorCode.trim().length() > 0) {
											break;
										}
									}
								}

								if (errorCode != null && errorCode.equalsIgnoreCase("")) {
									masterField = "";
									childKey = "";
									childValue = "";
									for (int i = 0; i <= arraySignerList.length() - 1; i++) {
										JSONObject signerListJsonObj = (JSONObject) arraySignerList.get(i);
										// System.out.println("partnerListJsonObj---names:
										// " + partnerListJsonObj.names());
										if(debug)
											response.getWriter().println("signerListJsonObj---names Length: "+ signerListJsonObj.names().length());

										if (constitutionType.equalsIgnoreCase(properties.getProperty("ProprietorConstitutionType"))
												&& signerListJsonObj.names().length() > 1) {
											errorCode = "E143";
											errorMsg = properties.getProperty(errorCode);

											JsonObject result = new JsonObject();
											result.addProperty("errorCode", errorCode);
											result.addProperty("Message", errorMsg);
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
											
											break;
										} else {
											for (int j = 0; j < signerListJsonObj.names().length(); j++) {
												if (debug)
													response.getWriter().println(j + " signerListJsonObj---names-String: "+ signerListJsonObj.names().get(j).toString());

												masterField = signerListJsonObj.names().get(j).toString();
												JSONArray arraySignerListCount = (JSONArray) signerListJsonObj.getJSONArray(masterField);
												response.getWriter().println(j + " arraySignerListCount---length: "+ arraySignerListCount.length());
												for (int k = 0; k <= arraySignerListCount.length() - 1; k++) {
													JSONObject signerListCountJsonObj = (JSONObject) arraySignerListCount.get(i);

													if (debug) {
														response.getWriter().println("signerListCountJsonObj---names: "+ signerListCountJsonObj.names());
														response.getWriter().println("signerListCountJsonObj---names Length: "+ signerListCountJsonObj.names().length());
													}

													for (int l = 0; l < signerListCountJsonObj.names().length(); l++) {
														childKey = signerListCountJsonObj.names().get(l).toString();
														childValue = signerListCountJsonObj.getString(childKey);
														if (debug) {
															response.getWriter().println("Signer" + l + "----> childField Key--->: " + childKey);
															response.getWriter().println("Signer" + l + "----> childField Value--->: " + childValue);
														}

														errorCode = commonUtils.validateInput(response, childKey, childValue, debug);

														if (errorCode != null && errorCode.trim().length() > 0) {
															break;
														}
													}
													if (errorCode != null && errorCode.trim().length() > 0) {
														break;
													}
												}
												if (errorCode != null && errorCode.trim().length() > 0) {
													break;
												}
											}
										}
									}

									if (errorCode != null && errorCode.equalsIgnoreCase("")) {
										wsURL = commonUtils.readDestProperties("URL");

										system = commonUtils.readDestProperties("System");
										if (system.equalsIgnoreCase("QAS")) {
											userPass = properties.getProperty("PeakLimitQASUsrPass");
										} else if (system.equalsIgnoreCase("PRD")) {
											userPass = properties.getProperty("PeakLimitPRDUsrPass");
										} else {
											if (system.equalsIgnoreCase("E153")) {
												userPass = system;
											} else {
												userPass = "E127";
											}
										}

										corpID = commonUtils.readDestProperties("CorpID");

										if (wsURL != "E106" && corpID != "E152" && system != "E153"
												&& userPass != "E127" && wsURL.trim().length() > 0) {
											userHistory = hasUserAlreadyApplied(request, response, customerNo, corpID,
													wsURL, userPass, debug);
											if (debug) {
												response.getWriter().println("userHistory: " + userHistory);
												response.getWriter().println("wsURL" + wsURL);
											}
											if (userHistory != null && userHistory.trim().equalsIgnoreCase("Y")) {
												wsURL = wsURL + "/" + properties.getProperty("RenewalApplyScenario");
												if (debug)
													response.getWriter().println("wsURL" + wsURL);

												byte[] postDataBytes = jsonObject.toString().getBytes("UTF-8");

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
														+ Base64.getEncoder().encodeToString(userPass.getBytes());
												con.setRequestProperty("Authorization", basicAuth);
												con.connect();

												OutputStream os = con.getOutputStream();
												OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
												osw.write(jsonObject.toString());
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

												String cpiResponse = sb.toString();

												if (debug) {
													JSONObject responseObj = new JSONObject(cpiResponse);
													response.getWriter().println(
															"resSplitResult: " + responseObj.getString("Message"));
													response.getWriter().println(
															"resSplitResult: " + responseObj.getString("Status"));
												}

												if (cpiResponse != null && cpiResponse.trim().length() > 0) {
													response.getWriter().println(cpiResponse);
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
											} else {
												errorCode = "E154";
												errorMsg = properties.getProperty(errorCode);

												JsonObject result = new JsonObject();
												result.addProperty("errorCode", errorCode);
												result.addProperty("Message", errorMsg);
												result.addProperty("Status", properties.getProperty("ErrorStatus"));
												result.addProperty("Valid", "false");
												response.getWriter().println(new Gson().toJson(result));
											}
										} else {
											if (wsURL != null && wsURL.equalsIgnoreCase("E106")) {
												errorCode = wsURL;
											}

											if (corpID != null && corpID.equalsIgnoreCase("E152")) {
												errorCode = corpID;
											}

											if (userPass != null && userPass.equalsIgnoreCase("E127")) {
												errorCode = userPass;
											}

											if (system != null && system.equalsIgnoreCase("E153")) {
												errorCode = system;
											}

											errorMsg = properties.getProperty(errorCode);

											JsonObject result = new JsonObject();
											result.addProperty("errorCode", errorCode);
											result.addProperty("Message", errorMsg);
											result.addProperty("Status", properties.getProperty("ErrorStatus"));
											result.addProperty("Valid", "false");
											response.getWriter().println(new Gson().toJson(result));
										}
									} else {
										errorMsg = properties.getProperty(errorCode);

										JsonObject result = new JsonObject();
										result.addProperty("errorCode", errorCode);
										result.addProperty("Message", errorMsg);
										result.addProperty("Status", properties.getProperty("ErrorStatus"));
										result.addProperty("Valid", "false");
										response.getWriter().println(new Gson().toJson(result));
									}
								} else {
									errorMsg = properties.getProperty(errorCode);

									JsonObject result = new JsonObject();
									result.addProperty("errorCode", errorCode);
									result.addProperty("Message", errorMsg);
									result.addProperty("Status", properties.getProperty("ErrorStatus"));
									result.addProperty("Valid", "false");
									response.getWriter().println(new Gson().toJson(result));
								}
							} else {
								errorMsg = properties.getProperty(errorCode);

								JsonObject result = new JsonObject();
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							}
						}
					} else {
						errorMsg = properties.getProperty(errorCode);

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
				errorCode = "E108";
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		} catch (JSONException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}

	}

	public String hasUserAlreadyApplied1(HttpServletRequest request, HttpServletResponse response, String customerNo,
			String merchantCode, String wsURL, String userpass, boolean debug) throws IOException {
		String statusCode = "";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

		try {
			String errorMsg = "";

			wsURL = wsURL + "/" + properties.getProperty("GetScenario");

			JSONObject inputJson = new JSONObject();
			JSONObject root = new JSONObject();
			inputJson.put("DealerID", customerNo);
			inputJson.put("AggregatorID", merchantCode);
			root.put("Root", inputJson);

			if (debug) {
				response.getWriter().println("inputJson - hasUserAlreadyApplied: " + inputJson);
				response.getWriter().println("Root - hasUserAlreadyApplied: " + root);
				response.getWriter().println("wsURL - hasUserAlreadyApplied: " + wsURL);
				response.getWriter().println("merchantCode - hasUserAlreadyApplied: " + merchantCode);
			}

			byte[] postDataBytes = root.toString().getBytes("UTF-8");
			if (debug) {
				response.getWriter().println("wsURL - hasUserAlreadyApplied: " + wsURL);
				response.getWriter().println("merchantCode - hasUserAlreadyApplied: " + merchantCode);
			}
			if (wsURL != "E106" && merchantCode != "E112" && wsURL.trim().length() > 0) {
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
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();

				if (debug)
					response.getWriter().println("sb1: " + sb.toString());

				String cpiResponse = sb.toString();
				JSONObject responseObj = new JSONObject(cpiResponse);
				if (debug) {
					/*
					 * response.getWriter().println("resSplitResult: "
					 * +responseObj.getString("EnhancementType"));
					 * response.getWriter().println("resSplitResult: "
					 * +responseObj.getString("ProposedLimit"));
					 */
					response.getWriter().println("resSplitResult-Status: " + responseObj.getString("Status"));
					response.getWriter().println("resSplitResult-cpiResponse: " + cpiResponse);
					response.getWriter().println("resSplitResult-responseObj: " + responseObj);
				}

				if (cpiResponse != null && cpiResponse.trim().length() > 0) {
					if (!responseObj.getString("Status").equalsIgnoreCase("000002")) {
						statusCode = responseObj.getString("Status") + "|" + responseObj.getString("Message");
					} else {
						statusCode = responseObj.getString("Status");
					}

					if (debug)
						response.getWriter().println("statusCode - hasUserAlreadyApplied: " + statusCode);

					return statusCode;
				} else {
					statusCode = "E107";
					errorMsg = properties.getProperty(statusCode);
					statusCode = "E107";
					JsonObject result = new JsonObject();
					result.addProperty("errorCode", statusCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
					if (debug)
						response.getWriter().println("statusCode E107 - hasUserAlreadyApplied: " + statusCode);
					return statusCode;
				}
			} else {
				if (wsURL.length() > 0) {
					statusCode = wsURL;
				} else if (merchantCode.length() > 0) {
					statusCode = merchantCode;
				} else {
					statusCode = "E129";
				}

				errorMsg = properties.getProperty(statusCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", statusCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));

				return statusCode;
			}
		} catch (MalformedURLException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (ProtocolException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (IOException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (JSONException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// doGet(request, response);
	}

	public String hasUserAlreadyApplied(HttpServletRequest request, HttpServletResponse response, String customerNo,
			String corpID, String wsURL, String userpass, boolean debug) throws IOException {
		String statusCode = "";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

		try {
			wsURL = wsURL + "/" + properties.getProperty("SCFOfferScenario");

			JSONObject inputJson = new JSONObject();
			JSONObject root = new JSONObject();
			inputJson.put("DealerId", customerNo);
			inputJson.put("CorpId", corpID);
			root.put("Root", inputJson);

			if (debug) {
				response.getWriter().println("inputJson - hasUserAlreadyApplied: " + inputJson);
				response.getWriter().println("Root - hasUserAlreadyApplied: " + root);
				response.getWriter().println("wsURL - hasUserAlreadyApplied: " + wsURL);
				response.getWriter().println("corpID - hasUserAlreadyApplied: " + corpID);
			}

			byte[] postDataBytes = root.toString().getBytes("UTF-8");
			if (debug) {
				response.getWriter().println("wsURL - hasUserAlreadyApplied: " + wsURL);
				response.getWriter().println("merchantCode - hasUserAlreadyApplied: " + corpID);
			}
			if (wsURL != "E106" && corpID != "E152" && wsURL.trim().length() > 0) {
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
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();

				if (debug)
					response.getWriter().println("sb: " + sb.toString());

				String cpiResponse = sb.toString();
				JSONObject responseObj = new JSONObject(cpiResponse);
				if (debug) {

					response.getWriter().println("resSplitResult: " + responseObj.getString("EnhancementType"));
					response.getWriter().println("resSplitResult: " + responseObj.getString("ProposedLimit"));
					response.getWriter().println("resSplitResult: " + responseObj.getString("Status"));
				}

				if (cpiResponse != null && cpiResponse.trim().length() > 0) {
					response.getWriter().println(cpiResponse);
					if (!responseObj.getString("IsRenewal").equalsIgnoreCase("Y")) {
						statusCode = responseObj.getString("IsRenewal");
					} else {
						statusCode = "N";
					}
					if (debug)
						response.getWriter().println("statusCode - hasUserAlreadyApplied: " + statusCode);
					return statusCode;
				} else {
					statusCode = "E107";

					if (debug)
						response.getWriter().println("statusCode - hasUserAlreadyApplied: " + statusCode);

					return statusCode;
				}
			} else {
				if (wsURL.length() > 0) {
					statusCode = wsURL;
				} else if (corpID.length() > 0) {
					statusCode = corpID;
				} else {
					statusCode = "E129";
				}

				return statusCode;
			}
		} catch (MalformedURLException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (ProtocolException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (IOException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (JSONException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(result);
			return statusCode;
		}
	}
}

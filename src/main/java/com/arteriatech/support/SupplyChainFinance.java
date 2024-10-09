package com.arteriatech.support;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
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

public class SupplyChainFinance extends HttpServlet {

	private static final long serialVersionUID = 1L;
	// private final String CPI_CONNECTION_DESTINATION = "CPIConnect";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getHeader("x-arteria-source") != null && request.getHeader("x-arteria-source").equalsIgnoreCase("DataPurging")) {
			JsonObject resObj = new JsonObject();
			CommonUtils commonUtils = new CommonUtils();
			String executeUrl = "", userpass = "", username = "", password = "", oDataUrl = "";
			boolean debug = false;
			try {
				if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
					debug = true;
				}

				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userpass = username + ":" + password;
				if (debug) {
					response.getWriter().println("oDataUrl:" + oDataUrl);
					response.getWriter().println("username:" + username);
				}
				if (request.getParameter("filter") != null && !request.getParameter("filter").equalsIgnoreCase("")) {
					if (debug) {
						response.getWriter().println("filter passed from UI:" + request.getParameter("filter"));
					}
					String filter = request.getParameter("filter");
					filter = filter.replaceAll(" ", "%20").replaceAll("'", "%27");
					if (debug) {
						response.getWriter().println("Odata Filetr query:" + filter);
					}
					executeUrl = oDataUrl + "SupplyChainFinances?$filter=" + filter;
				} else {
					executeUrl = oDataUrl + "SupplyChainFinances";
				}

				JsonObject scfObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
				if (debug) {
					response.getWriter().println("scfObj:" + scfObj);
				}
				if (scfObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
					if (scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						JsonArray eligibilityArry = scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
						resObj.add("SupplyChainFinances", eligibilityArry);
						resObj.addProperty("Status", "000001");
						resObj.addProperty("ErrorCode", "");
						response.getWriter().println(resObj);
					} else {
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Record doesn't exist");
						response.getWriter().println(resObj);
					}

				} else {
					response.getWriter().println(scfObj);
				}

			} catch (Exception ex) {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", ex.getLocalizedMessage() + "");
				resObj.addProperty("ExceptionTrace", buffer.toString());
				response.getWriter().println(resObj);
			}
		} else if (request.getHeader("x-arteria-source") != null && request.getHeader("x-arteria-source").equalsIgnoreCase("RenewalSCFUpdate")) {
			JsonObject resObj = new JsonObject();
			CommonUtils commonUtils = new CommonUtils();
			String executeUrl = "", userpass = "", username = "", password = "", oDataUrl = "";
			boolean debug = false;
			String cpGuid = "", cpType = "", econtractID = "";
			boolean validRequest = true;
			try {
				if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
				if (request.getHeader("x-arteria-aggrid") != null && !request.getHeader("x-arteria-aggrid").equalsIgnoreCase("")) {
					username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					userpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl:" + oDataUrl);
						response.getWriter().println("username:" + username);
						response.getWriter().println("AggregatorID:" + request.getHeader("x-arteria-aggrid"));
					}
					if (request.getParameter("CPGuid") != null && !request.getParameter("CPGuid").equalsIgnoreCase("")) {
						cpGuid = request.getParameter("CPGuid");
						if (request.getParameter("CPType") != null && !request.getParameter("CPType").equalsIgnoreCase("")) {
							cpType = request.getParameter("CPType");
						} else {
							validRequest = false;
						}
					}

					if (debug) {
						response.getWriter().println("validRequest:" + validRequest);
						response.getWriter().println("CPType:" + cpType);
						response.getWriter().println("CPGuid:" + cpGuid);
					}
					if (validRequest) {
						if (request.getParameter("EcontractID") != null && !request.getParameter("EcontractID").equalsIgnoreCase("")) {
							econtractID = request.getParameter("EcontractID");
						}
						
						if (debug) {
							response.getWriter().println("econtractID:" + econtractID);
						}

						if (cpGuid != null && !cpGuid.equalsIgnoreCase("")) {
							if (econtractID != null && !econtractID.equalsIgnoreCase("")) {
								executeUrl = oDataUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + request.getHeader("x-arteria-aggrid") + "%27%20and%20CPGUID%20eq%20%27" + cpGuid + "%27%20and%20CPTypeID%20eq%20%27" + cpType + "%27%20and%20EContractID%20eq%20%27" + econtractID + "%27";
							} else {
								executeUrl = oDataUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + request.getHeader("x-arteria-aggrid") + "%27%20and%20CPGUID%20eq%20%27" + cpGuid + "%27%20and%20CPTypeID%20eq%20%27" + cpType + "%27";
							}
						} else {
							if (econtractID != null && !econtractID.equalsIgnoreCase("")) {
								executeUrl = oDataUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + request.getHeader("x-arteria-aggrid") + "%27%20and%20EContractID%20eq%20%27" + econtractID + "%27";
							} else {
								executeUrl = oDataUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + request.getHeader("x-arteria-aggrid") + "%27";
							}

						}
						if(debug){
							response.getWriter().println("executeUrl:"+executeUrl);
						}
						
						JsonObject scfObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
						if(debug){
							response.getWriter().println("scfObj:"+scfObj);
						}
						
						if(scfObj.get("Status").getAsString().equalsIgnoreCase("000001")){
							if(scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
								JsonArray scfArry = scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();								
								List<JsonObject> scfList = IntStream.range(0, scfArry.size()).mapToObj(k -> scfArry.get(k).getAsJsonObject()).filter(obj->!obj.get("ApplicationNo").isJsonNull()&&(obj.get("ApplicationNo").getAsString().startsWith("PAR")||obj.get("ApplicationNo").getAsString().startsWith("PQR"))).collect(Collectors.toList());
								JsonArray updateRecArr=new JsonArray();
								scfList.forEach(obj->updateRecArr.add(obj));
								JsonObject resultObj=new JsonObject();
								resultObj.add("results", updateRecArr);
								resObj.addProperty("Status", "000001");
								resObj.addProperty("ErrorCode", "");
								resObj.add("d", resultObj);
								response.getWriter().println(resObj);
							}else{
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", "Record doesn't exit");
								response.getWriter().println(resObj);
								
							}
							
						}else{
							response.getWriter().println(scfObj);	
						}
					} else {
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Mandatory Params missing the request");
						response.getWriter().println(resObj);

					}
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "AggregatorID Misiing in the request");
					response.getWriter().println(resObj);

				}
			} catch (Exception ex) {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", ex.getLocalizedMessage() + "");
				resObj.addProperty("ExceptionTrace", buffer.toString());
				response.getWriter().println(resObj);
			}
		}
		else {
			CommonUtils commonUtils = new CommonUtils();
			JsonParser parser = new JsonParser();
			String inputPayload = "", wsURL = "", userName = "", passWord = "", userpass = "", esignContractCalEndPoint = "", esignContractCalres = "";
			boolean debug = false;
			JsonObject inpJsonPayLoad = new JsonObject();
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			try {
				inputPayload = commonUtils.getGetBody(request, response);
				if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
					inpJsonPayLoad = (JsonObject) parser.parse(inputPayload);
					if (inpJsonPayLoad.has("debug") && !inpJsonPayLoad.get("debug").isJsonNull() && inpJsonPayLoad.get("debug").getAsString().equalsIgnoreCase("true")) {
						debug = true;
					}
					if (debug) {
						response.getWriter().println("Input Payload :" + inpJsonPayLoad);
						inpJsonPayLoad.remove("debug");
					}

					// Context ctxDestFact = new InitialContext();
					// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
					// DestinationConfiguration cpiConfig = configuration.getConfiguration(DestinationUtils.CPI_CONNECT);
					DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
							.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
					Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
							.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
					Destination cpiConfig = destinationAccessor.get();

					wsURL = cpiConfig.get("URL").get().toString();
					if (debug) {
						response.getWriter().println("WsURL :" + wsURL);
					}
					if (wsURL != null && !wsURL.equalsIgnoreCase("")) {
						userName = cpiConfig.get("User").get().toString();
						passWord = cpiConfig.get("Password").get().toString();
						userpass = userName + ":" + passWord;
						esignContractCalEndPoint = properties.getProperty("SCF_Delete");
						wsURL = wsURL.concat(esignContractCalEndPoint);
						if (debug) {
							response.getWriter().println("SupplyChainFinance cpi  Url: " + wsURL);
							response.getWriter().println("userName: " + userName);
							response.getWriter().println("Password: " + passWord);
							response.getWriter().println("Cpi Input " + inpJsonPayLoad);
						}
						URL url = new URL(wsURL);
						HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
						byte[] bytes = inpJsonPayLoad.toString().getBytes("UTF-8");
						urlConnection.setRequestMethod("GET");
						urlConnection.setRequestProperty("Content-Type", "application/json");
						urlConnection.setRequestProperty("charset", "utf-8");
						urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
						urlConnection.setRequestProperty("Accept", "application/json");
						urlConnection.setDoOutput(true);
						urlConnection.setDoInput(true);
						urlConnection.setUseCaches(false);
						String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
						urlConnection.setRequestProperty("Authorization", basicAuth);
						urlConnection.connect();
						OutputStream outputStream = urlConnection.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
						osw.write(inpJsonPayLoad.toString());
						osw.flush();
						osw.close();
						int resCode = urlConnection.getResponseCode();
						if (debug) {
							response.getWriter().println("responseCode: " + resCode);
						}
						if ((resCode / 100) == 2 || (resCode / 100) == 3) {
							StringBuffer sb = new StringBuffer();
							BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
							String line = null;
							while ((line = br.readLine()) != null) {
								sb.append(line + "\n");
							}
							br.close();
							if (debug) {
								response.getWriter().println("CPI Response : " + sb.toString());
							}
							esignContractCalres = sb.toString();
							JsonParser jsonParser = new JsonParser();
							JsonObject responseJson = (JsonObject) jsonParser.parse(esignContractCalres);
							responseJson.addProperty("Remarks", "");
							if (debug)
								response.getWriter().println("responseJson: " + responseJson);
							response.getWriter().println(responseJson);

						} else {
							StringBuffer sb = new StringBuffer();
							BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
							String line = null;
							while ((line = br.readLine()) != null) {
								sb.append(line + "\n");
							}
							br.close();
							if (debug) {
								response.getWriter().println("getErrorStream: " + sb.toString());
							}
							esignContractCalres = sb.toString();
							JsonParser jsonParser = new JsonParser();
							JsonObject responseJson = (JsonObject) jsonParser.parse(esignContractCalres);
							responseJson.addProperty("Remarks", "");
							if (debug)
								response.getWriter().println("responseJson: " + responseJson);
							response.getWriter().println(responseJson);

						}
					}

				} else {
					if (debug)
						response.getWriter().println("  Request");
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "/ARTEC/J001");
					result.addProperty("Message", "Empty InputPayload received in the request");
					response.getWriter().println(new Gson().toJson(result));
				}

			} catch (Exception ex) {
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
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getHeader("x-arteria-source") != null && request.getHeader("x-arteria-source").equalsIgnoreCase("StructuredTrade")) {
			String aggrid = "", oDataUrl = "", username = "", password = "", userpass = "", executeURL = "";
			boolean debug = false;
			JsonObject resObj = new JsonObject();
			CommonUtils commonUtils = new CommonUtils();
			JsonParser parser = new JsonParser();
			String inputPayload = commonUtils.getGetBody(request, response);
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("Received input Payload:" + inputPayload);
			}
			if (inputPayload != null && !inputPayload.trim().equalsIgnoreCase("") && inputPayload.trim().length() > 0) {
				try {
					if (request.getHeader("x-arteria-aggrid") != null && !request.getHeader("x-arteria-aggrid").equalsIgnoreCase("")) {
						aggrid = request.getHeader("x-arteria-aggrid");
					} else {
						aggrid = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
					}
					if (debug) {
						response.getWriter().println("aggrid:" + aggrid);
					}
					JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
					String message = validateInput(jsonInput);
					if (debug) {
						response.getWriter().println("message:" + message);
					}
					if (message.equalsIgnoreCase("")) {
						oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
						password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
						userpass = username + ":" + password;
						String cpGuid = jsonInput.get("CPGUID").getAsString();
						String cpType = jsonInput.get("CPTypeID").getAsString();
						String accNo = jsonInput.get("AccountNo").getAsString();
						String statusId = jsonInput.get("StatusID").getAsString();
						String callBackStatus = jsonInput.get("CallBackStatus").getAsString();
						if (debug) {
							response.getWriter().println("cpGuid:" + cpGuid);
							response.getWriter().println("CPType:" + cpType);
						}
						executeURL = oDataUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + aggrid + "%27%20and%20CPGUID%20eq%20%27" + cpGuid + "%27%20and%20CPTypeID%20eq%20%27" + cpType + "%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						
						JsonObject scfObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
						if (debug) {
							response.getWriter().println("scfObj:" + scfObj);
						}

						if (scfObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
							//
							if (scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								scfObj = scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
								if (scfObj.has("__metadata")) {
									scfObj.remove("__metadata");
								}
								scfObj.remove("ChangedBy");
								scfObj.remove("ChangedAt");
								scfObj.remove("ChangedOn");
								long changedOn = commonUtils.getCreatedOnDate();
								String changedAt = commonUtils.getCreatedAtTime();
								String changedBy = commonUtils.getUserPrincipal(request, "name", response);
								scfObj.addProperty("ChangedOn", "/Date(" + changedOn + ")/");
								scfObj.addProperty("ChangedAt", changedAt);
								scfObj.addProperty("ChangedBy", changedBy);
								scfObj.addProperty("AggregatorID", aggrid);
								scfObj.addProperty("CPGUID", cpGuid);
								scfObj.addProperty("CPTypeID", cpType);
								scfObj.addProperty("CallBackStatus", callBackStatus);
								scfObj.addProperty("StatusID", statusId);
								scfObj.addProperty("AccountNo", accNo);
								if (jsonInput.has("OfferAmt") && !jsonInput.get("OfferAmt").isJsonNull()) {
									scfObj.addProperty("OfferAmt", jsonInput.get("OfferAmt").getAsString());
								}

								if (jsonInput.has("OfferTenure") && !jsonInput.get("OfferTenure").isJsonNull()) {
									scfObj.addProperty("OfferTenure", jsonInput.get("OfferTenure").getAsString());
								}

								if (jsonInput.has("Rate") && !jsonInput.get("Rate").isJsonNull()) {
									scfObj.addProperty("Rate", jsonInput.get("Rate").getAsString());
								}
								if (jsonInput.has("NoOfChequeReturns") && !jsonInput.get("NoOfChequeReturns").isJsonNull()) {
									scfObj.addProperty("NoOfChequeReturns", jsonInput.get("NoOfChequeReturns").getAsString());
								}

								if (jsonInput.has("PaymentDelayDays12Months") && !jsonInput.get("PaymentDelayDays12Months").isJsonNull()) {
									scfObj.addProperty("PaymentDelayDays12Months", jsonInput.get("PaymentDelayDays12Months").getAsString());
								}

								if (jsonInput.has("BusinessVintageOfDealer") && !jsonInput.get("BusinessVintageOfDealer").isJsonNull()) {
									scfObj.addProperty("BusinessVintageOfDealer", jsonInput.get("BusinessVintageOfDealer").getAsString());
								}

								if (jsonInput.has("PurchasesOf12Months") && !jsonInput.get("PurchasesOf12Months").isJsonNull()) {
									scfObj.addProperty("PurchasesOf12Months", jsonInput.get("PurchasesOf12Months").getAsString());
								}

								if (jsonInput.has("DealersOverallScoreByCorp") && !jsonInput.get("DealersOverallScoreByCorp").isJsonNull()) {
									scfObj.addProperty("DealersOverallScoreByCorp", jsonInput.get("DealersOverallScoreByCorp").getAsString());
								}

								if (jsonInput.has("CorpRating") && !jsonInput.get("CorpRating").isJsonNull()) {
									scfObj.addProperty("CorpRating", jsonInput.get("CorpRating").getAsString());
								}

								if (jsonInput.has("DealerVendorFlag") && !jsonInput.get("DealerVendorFlag").isJsonNull()) {
									scfObj.addProperty("DealerVendorFlag", jsonInput.get("DealerVendorFlag").getAsString());
								}

								if (jsonInput.has("ConstitutionType") && !jsonInput.get("ConstitutionType").isJsonNull()) {
									scfObj.addProperty("ConstitutionType", jsonInput.get("ConstitutionType").getAsString());
								}

								if (jsonInput.has("MaxLimitPerCorp") && !jsonInput.get("MaxLimitPerCorp").isJsonNull()) {
									scfObj.addProperty("MaxLimitPerCorp", jsonInput.get("MaxLimitPerCorp").getAsString());
								}

								if (jsonInput.has("salesOf12Months") && !jsonInput.get("salesOf12Months").isJsonNull()) {
									scfObj.addProperty("salesOf12Months", jsonInput.get("salesOf12Months").getAsString());
								}

								if (jsonInput.has("Currency") && !jsonInput.get("Currency").isJsonNull()) {
									scfObj.addProperty("Currency", jsonInput.get("Currency").getAsString());
								}

								if (jsonInput.has("MCLR6Rate") && !jsonInput.get("MCLR6Rate").isJsonNull()) {
									scfObj.addProperty("MCLR6Rate", jsonInput.get("MCLR6Rate").getAsString());
								}

								if (jsonInput.has("InterestRateSpread") && !jsonInput.get("InterestRateSpread").isJsonNull()) {
									scfObj.addProperty("InterestRateSpread", jsonInput.get("InterestRateSpread").getAsString());
								}

								if (jsonInput.has("TenorOfPayment") && !jsonInput.get("TenorOfPayment").isJsonNull()) {
									scfObj.addProperty("TenorOfPayment", scfObj.get("TenorOfPayment").getAsString());
								}

								if (jsonInput.has("ADDLNPRDINTRateSP") && !jsonInput.get("ADDLNPRDINTRateSP").isJsonNull()) {
									scfObj.addProperty("ADDLNPRDINTRateSP", jsonInput.get("ADDLNPRDINTRateSP").getAsString());
								}

								if (jsonInput.has("AddlnTenorOfPymt") && !jsonInput.get("AddlnTenorOfPymt").isJsonNull()) {
									scfObj.addProperty("AddlnTenorOfPymt", jsonInput.get("AddlnTenorOfPymt").getAsString());
								}

								if (jsonInput.has("DefIntSpread") && !jsonInput.get("DefIntSpread").isJsonNull()) {
									scfObj.addProperty("DefIntSpread", jsonInput.get("DefIntSpread").getAsString());
								}

								if (jsonInput.has("ProcessingFee") && !jsonInput.get("ProcessingFee").isJsonNull()) {
									scfObj.addProperty("ProcessingFee", jsonInput.get("ProcessingFee").getAsString());
								}

								if (jsonInput.has("ECustomerID") && !jsonInput.get("ECustomerID").isJsonNull()) {
									scfObj.addProperty("ECustomerID", jsonInput.get("ECustomerID").getAsString());
								}

								if (jsonInput.has("EContractID") && !jsonInput.get("EContractID").isJsonNull()) {
									scfObj.addProperty("EContractID", jsonInput.get("EContractID").getAsString());
								}

								if (jsonInput.has("ApplicationNo") && !jsonInput.get("ApplicationNo").isJsonNull()) {
									scfObj.addProperty("ApplicationNo", jsonInput.get("ApplicationNo").getAsString());
								}

								if (jsonInput.has("ECompleteDate") && !jsonInput.get("ECompleteDate").isJsonNull()) {
									scfObj.addProperty("ECompleteDate", jsonInput.get("ECompleteDate").getAsString());
								}

								if (jsonInput.has("ECompleteTime") && !jsonInput.get("ECompleteTime").isJsonNull()) {
									scfObj.addProperty("ECompleteTime", jsonInput.get("ECompleteTime").getAsString());
								}

								if (jsonInput.has("ApplicantID") && !jsonInput.get("ApplicantID").isJsonNull()) {
									scfObj.addProperty("ApplicantID", jsonInput.get("ApplicantID").getAsString());
								}

								if (jsonInput.has("LimitPrefix") && !jsonInput.get("LimitPrefix").isJsonNull()) {
									scfObj.addProperty("LimitPrefix", jsonInput.get("LimitPrefix").getAsString());
								}

								if (jsonInput.has("InterestSpread") && !jsonInput.get("InterestSpread").isJsonNull()) {
									scfObj.addProperty("InterestSpread", jsonInput.get("InterestSpread").getAsString());
								}

								if (jsonInput.has("InterestSpread") && !jsonInput.get("InterestSpread").isJsonNull()) {
									scfObj.addProperty("InterestSpread", jsonInput.get("InterestSpread").getAsString());
								}

								if (jsonInput.has("DDBActive") && !jsonInput.get("DDBActive").isJsonNull()) {
									scfObj.addProperty("DDBActive", jsonInput.get("DDBActive").getAsString());
								}

								if (jsonInput.has("ProcessFeePerc") && !jsonInput.get("ProcessFeePerc").isJsonNull()) {
									scfObj.addProperty("ProcessFeePerc", jsonInput.get("ProcessFeePerc").getAsString());
								}

								if (jsonInput.has("ValidTo") && !jsonInput.get("ValidTo").isJsonNull()) {
									scfObj.addProperty("ValidTo", jsonInput.get("ValidTo").getAsString());
								}

								if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
									scfObj.addProperty("Source", jsonInput.get("Source").getAsString());
								}

								if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
									scfObj.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
								}

								executeURL = oDataUrl + "SupplyChainFinances('" + scfObj.get("ID").getAsString() + "')";
								if (debug) {
									response.getWriter().println("update SCF Url:" + executeURL);
									response.getWriter().println("Update scf Updated Payload:" + scfObj);
								}
								JsonObject executeUpdate = commonUtils.executeUpdate(executeURL, userpass, response, scfObj, request, debug, "PYGWHANA");
								response.getWriter().println(executeUpdate);
							} else {
								JsonObject insertObj = new JsonObject();
								String guid = commonUtils.generateGUID(36);
								long createdOn = commonUtils.getCreatedOnDate();
								String createdAt = commonUtils.getCreatedAtTime();
								String createdBy = commonUtils.getUserPrincipal(request, "name", response);
								long validTo = commonUtils.getDateAfterOneyearFrmCurDate();
								insertObj.addProperty("ID", guid);
								insertObj.addProperty("CreatedOn", "/Date(" + createdOn + ")/");
								insertObj.addProperty("CreatedAt", createdAt);
								insertObj.addProperty("CreatedBy", createdBy);
								insertObj.addProperty("AggregatorID", aggrid);
								insertObj.addProperty("CPGUID", cpGuid);
								insertObj.addProperty("CPTypeID", cpType);
								insertObj.addProperty("AccountNo", accNo);
								insertObj.addProperty("StatusID", statusId);
								insertObj.addProperty("CallBackStatus", callBackStatus);
								insertObj.addProperty("ValidTo", "/Date(" + validTo + ")/");
								executeURL = oDataUrl + "SupplyChainFinances";
								if (debug) {
									response.getWriter().println("executeURL:" + executeURL);
									response.getWriter().println("insertObj:" + insertObj);
								}
								JsonObject scfInsObj = commonUtils.executePostURL(executeURL, userpass, response, insertObj, request, debug, "PYGWHANA");
								response.getWriter().println(scfInsObj);
							}
						} else {
							response.getWriter().println(scfObj);
						}
					} else {
						resObj.addProperty("Message", message);
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
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", ex.getLocalizedMessage() + "");
					resObj.addProperty("ExceptionTrace", buffer.toString());
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Message", "Invalid request,Input Payload should not be empty");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}
	    }else if (request.getHeader("x-arteria-source") != null&& request.getHeader("x-arteria-source").equalsIgnoreCase("VBDOnboarding")) {
			boolean debug = false;
			String scfAggregatorId = "", pygwUrl = "", pygwusername = "", pygwpassword = "", pygUserpass = "",executeURL = "", executeUrl;
			CommonUtils commonUtils = new CommonUtils();
			String createdAt = "", createdBy = "", id = "";
			long createdOnInMillis = 0;
			JsonObject resObj = new JsonObject();
			JsonParser parser = new JsonParser();
			JsonObject supplyChainFinanceEntry = new JsonObject();
			JsonObject supplyChainFinanceResponse = new JsonObject();
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			String inputPayload = commonUtils.getGetBody(request, response);
			if (debug) {
				response.getWriter().println("Received input Payload:" + inputPayload);
			}
			if (inputPayload != null && !inputPayload.trim().equalsIgnoreCase("") && inputPayload.trim().length() > 0) {
				try {
					scfAggregatorId = commonUtils.getODataDestinationProperties("AggregatorID",DestinationUtils.PYGWHANA);
					if (debug) {
						response.getWriter().println("aggrid:" + scfAggregatorId);
					}
					JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
					String message = validateScfInput(jsonInput);
					if (debug) {
						response.getWriter().println("message:" + message);
					}

					if (message.equalsIgnoreCase("")) {
						pygwUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						pygwusername = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
						pygwpassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
						pygUserpass = pygwusername + ":" + pygwpassword;

						String cpGuid = jsonInput.get("CPGUID").getAsString();
						String cpType = jsonInput.get("CPTypeID").getAsString();
						String accNo = jsonInput.get("AccountNo").getAsString();
						String statusId = jsonInput.get("StatusID").getAsString();
						String DDBActive = jsonInput.get("DDBActive").getAsString();

						if (debug) {
							response.getWriter().println("cpGuid:" + cpGuid);
							response.getWriter().println("CPType:" + cpType);
						}

						executeURL = pygwUrl + "SupplyChainFinances?$filter=AggregatorID%20eq%20%27" + scfAggregatorId
									+ "%27%20and%20CPGUID%20eq%20%27" + cpGuid + "%27%20and%20CPTypeID%20eq%20%27" + cpType
									+ "%27%20and%20AccountNo%20eq%20%27"+ accNo + "%27%20and%20StatusID%20eq%20%27" + statusId + "%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}

						JsonObject scfObj = commonUtils.executeODataURL(executeURL, pygUserpass, response, debug);

						if (debug) {
							response.getWriter().println("scfObj:" + scfObj);
						}

						if (scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() == 0) {
							id = commonUtils.generateGUID(36);
							createdBy = commonUtils.getUserPrincipal(request, "name", response);
							createdAt = commonUtils.getCreatedAtTime();
							createdOnInMillis = commonUtils.getCreatedOnDate();
							supplyChainFinanceEntry.addProperty("ID", id);
							supplyChainFinanceEntry.addProperty("CreatedBy", createdBy);
							supplyChainFinanceEntry.addProperty("CreatedAt", createdAt);
							supplyChainFinanceEntry.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
							supplyChainFinanceEntry.addProperty("AccountNo", accNo);
							supplyChainFinanceEntry.addProperty("AggregatorID", scfAggregatorId);
							supplyChainFinanceEntry.addProperty("CPGUID", cpGuid);
							supplyChainFinanceEntry.addProperty("CPTypeID", cpType);
							supplyChainFinanceEntry.addProperty("StatusID", statusId);
							supplyChainFinanceEntry.addProperty("CallBackStatus", "000080");
							supplyChainFinanceEntry.addProperty("DDBActive", DDBActive);
							executeUrl = pygwUrl + "SupplyChainFinances";
							if (debug) {
								response.getWriter().println("POST SupplyChainFinances executeUrl: " + executeUrl);
								response.getWriter().println("POST SupplyChainFinances supplyChainFinanceEntry: " + supplyChainFinanceEntry);
							}
							supplyChainFinanceResponse = commonUtils.executePostURL(executeUrl, pygUserpass, response,supplyChainFinanceEntry, request, debug);

							if (supplyChainFinanceResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
								resObj.addProperty("Message", "Record Inserted Successfully");
								resObj.addProperty("Status", "000001");
								resObj.addProperty("ErrorCode", "");
								response.getWriter().println(resObj);
							} else {
								resObj.addProperty("Message", supplyChainFinanceResponse.toString());
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
							}
						} else {
							resObj.addProperty("Message", "Record Already Inserted");
							resObj.addProperty("Status", "000001");
							resObj.addProperty("ErrorCode", "");
							response.getWriter().println(resObj);
						}
					} else {
						resObj.addProperty("Message", message);
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(resObj);
					}
				} catch (Exception e) {
					resObj.addProperty("Message", e.getLocalizedMessage());
					resObj.addProperty("ExceptionTrace ", e.getStackTrace() +"");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Message", "Invalid request,Input Payload should not be empty");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}
	    } else {
			// exiting code Calling cpi iflow should work.
			doGet(request, response);
		}
	}

	private String validateScfInput(JsonObject jsonInput) {
		String message = "";
		try {
			if (jsonInput.has("CPGUID")) {
				if (jsonInput.get("CPGUID").isJsonNull()
						|| jsonInput.get("CPGUID").getAsString().equalsIgnoreCase("")) {
					message = "CPGUID is Mandatory";
					return message;
				}
			} else {
				message = "CPGUID is Mandatory";
				return message;
			}

			if (jsonInput.has("CPTypeID")) {
				if (jsonInput.get("CPTypeID").isJsonNull()
						|| jsonInput.get("CPTypeID").getAsString().equalsIgnoreCase("")) {
					message = "CPTypeID is Mandatory";
					return message;
				}else if (!jsonInput.get("CPTypeID").getAsString().equalsIgnoreCase("60")) {
					message = "CPTypeID Should be 60";
					return message;
				}
			} else {
				message = "CPTypeID is Mandatory";
				return message;
			}

			if (jsonInput.has("AccountNo")) {
				if (jsonInput.get("AccountNo").isJsonNull()
						|| jsonInput.get("AccountNo").getAsString().equalsIgnoreCase("")) {
					message = "AccountNo is Mandatory";
					return message;
				}
			} else {
				message = "AccountNo is Mandatory";
				return message;
			}

			if (jsonInput.has("StatusID")) {
				if (jsonInput.get("StatusID").isJsonNull()
						|| jsonInput.get("StatusID").getAsString().equalsIgnoreCase("")) {
					message = "StatusID is Mandatory";
					return message;
				}else if (!jsonInput.get("StatusID").getAsString().equalsIgnoreCase("000002")) {
					message = "StatusID Should be 000002";
					return message;
				}
			} else {
				message = "StatusID is Mandatory";
				return message;
			}

			if (jsonInput.has("DDBActive")) {
				if (jsonInput.get("DDBActive").isJsonNull()
						|| jsonInput.get("DDBActive").getAsString().equalsIgnoreCase("")) {
					message = "DDBActive is Mandatory";
					return message;
				}
			} else {
				message = "DDBActive is Mandatory";
				return message;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return message;
	}

	private String validateInput(JsonObject jsonInput) throws Exception {
		String message = "";
		try {
			if (jsonInput.has("CPGUID")) {
				if (jsonInput.get("CPGUID").isJsonNull() || jsonInput.get("CPGUID").getAsString().equalsIgnoreCase("")) {
					message = "Invalid request,CPGUID Should not be null or empty";
					return message;
				}
			} else {
				message = "Invalid request,CPGUID doesn't exist in the input Payload";
				return message;
			}
			if (jsonInput.has("CPTypeID")) {
				if (jsonInput.get("CPTypeID").isJsonNull() || jsonInput.get("CPTypeID").getAsString().equalsIgnoreCase("")) {
					message = "Invalid request,CPTypeID Should not be null or empty";
					return message;
				}
			} else {
				message = "Invalid request,CPTypeID doesn't exist in the input Payload";
				return message;
			}

			// if (jsonInput.has("AccountNo")) {
			// 	if (jsonInput.get("AccountNo").isJsonNull() || jsonInput.get("AccountNo").getAsString().equalsIgnoreCase("")) {
			// 		message = "Invalid request,AccountNo Should not be null or empty";
			// 		return message;
			// 	}
			// } else {
			// 	message = "Invalid request,AccountNo doesn't exist in the input Payload";
			// 	return message;
			// }

			if(jsonInput.has("ParentCPNo")){
				if (!jsonInput.get("ParentCPNo").isJsonNull() || !jsonInput.get("ParentCPNo").getAsString().trim().equalsIgnoreCase("")) {
					if (jsonInput.has("AccountNo")) {
						if(jsonInput.get("ParentCPNo").getAsString().trim().equalsIgnoreCase(jsonInput.get("CPGUID").getAsString())){
							if (jsonInput.get("AccountNo").isJsonNull() || jsonInput.get("AccountNo").getAsString().equalsIgnoreCase("")) {
								message = "Invalid request, AccountNo Should not be null or empty";
								return message;
							}
						}
					} else {
						message = "Invalid request, AccountNo doesn't exist in the input Payload";
						return message;
					}
				}
			}else{
				message = "Invalid request, ParentCPNo doesn't exist in the input Payload";
				return message;
			}

			if (jsonInput.has("StatusID")) {
				if (jsonInput.get("StatusID").isJsonNull() || jsonInput.get("StatusID").getAsString().equalsIgnoreCase("")) {
					message = "Invalid request,StatusID Should not be null or empty";
					return message;
				}
			} else {
				message = "Invalid request,StatusID doesn't exist in the input Payload";
				return message;
			}

		} catch (Exception ex) {
			throw ex;
		}

		return message;
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String executeUrl = "", userpass = "", username = "", password = "", oDataUrl = "";
		boolean debug = false;
		JsonParser parser = new JsonParser();
		String aggregatorId = "";
		username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
		password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
		oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
		userpass = username + ":" + password;
		if(request.getParameter("debug")!=null&&request.getParameter("debug").equalsIgnoreCase("true")){
			debug=true;
		}
		if (debug) {
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				String value = request.getHeader(key);
				response.getWriter().print("Header key:   " + key);
				response.getWriter().println("Header value: " + value);
			}
			response.getWriter().println("x-arteria-source from header: " + request.getHeader("x-arteria-source"));
		}
		if (request.getHeader("x-arteria-source") != null && request.getHeader("x-arteria-source").equalsIgnoreCase("DataPurging")) {
			try {
				if (request.getHeader("x-arteria-aggrid") != null && !request.getHeader("x-arteria-aggrid").equalsIgnoreCase("")) {
					aggregatorId = request.getHeader("x-arteria-aggrid");
					String inputPayload = commonUtils.getGetBody(request, response);
					if (inputPayload != null && inputPayload.trim().equalsIgnoreCase("")) {
						JsonObject jsonPayload = (JsonObject) parser.parse(inputPayload);
						if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
							debug = true;
						}
						if (jsonPayload.has("ID") && !jsonPayload.get("ID").isJsonNull() && !jsonPayload.get("ID").getAsString().equalsIgnoreCase("")) {
							
							if (debug) {
								response.getWriter().println("oDataUrl:" + oDataUrl);
								response.getWriter().println("username:" + username);
								response.getWriter().println("AggregatorID:" + aggregatorId);
							}
							executeUrl = oDataUrl + "SupplyChainFinances?$filter=ID%20eq%20%27" + jsonPayload.get("ID").getAsString() + "%27%20and%20CallBackStatus%20eq%20%27" + "000080" + "%27%20and%20AggregatorID%20eq%20%27" + aggregatorId + "%27";
							if (debug) {
								response.getWriter().println("executeUrl:" + executeUrl);
							}
							JsonObject scfDbObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
							if (debug) {
								response.getWriter().println("scfDbObj:" + scfDbObj);
							}
							if (scfDbObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								if (scfDbObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									scfDbObj = scfDbObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();

									executeUrl = oDataUrl + "SupplyChainFinances('" + jsonPayload.get("ID").getAsString() + "')";
									if (debug) {
										response.getWriter().println("Update SupplyChain Finance Record:" + executeUrl);
									}
									if (scfDbObj.has("__metadata")) {
										scfDbObj.remove("__metadata");
									}
									scfDbObj.remove("StatusID");
									scfDbObj.addProperty("StatusID", "000003");
									if (debug) {
										response.getWriter().println("updated ScfObj:" + scfDbObj);
									}
									JsonObject updateObj = commonUtils.executeUpdate(executeUrl, userpass, response, scfDbObj, request, debug, "PYGWHANA");
									response.getWriter().println(updateObj);
								}
							} else {
								response.getWriter().println(scfDbObj);
							}
						} else {
							if (jsonPayload.has("ID")) {
								resObj.addProperty("Message", "ID Should not be empty or null");
							} else {
								resObj.addProperty("Message", "Input Payload doesn't contains a ID Property");
							}
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						}
					} else {
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Empty input Payload received");
						response.getWriter().println(resObj);

					}
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "x-arteria-aggrid Header doesn't exist");
					response.getWriter().println(resObj);
				}

			} catch (Exception ex) {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", ex.getLocalizedMessage() + "");
				resObj.addProperty("ExceptionTrace", buffer.toString());
				response.getWriter().println(resObj);

			}
		} else if (request.getHeader("x-arteria-source") != null && request.getHeader("x-arteria-source").equalsIgnoreCase("RenewalSCFUpdate")) {
			try {
				String inputPayload = commonUtils.getGetBody(request, response);
				// JsonObject insertObj=new JsonObject();
				if (inputPayload != null && !inputPayload.equalsIgnoreCase("") && inputPayload.trim().length() > 0) {
					JsonObject jsonInput = (JsonObject) parser.parse(inputPayload);
					if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
						debug = true;
					}
					if (debug) {
						response.getWriter().println("Input Payload:" + jsonInput);
					}
					if (jsonInput.has("ID") && !jsonInput.get("ID").getAsString().equalsIgnoreCase("")) {

						executeUrl = oDataUrl + "SupplyChainFinances?$filter=ID%20eq%20%27" + jsonInput.get("ID").getAsString() + "%27";
						if (debug) {
							response.getWriter().println("oDataUrl:" + oDataUrl);
						}
						JsonObject scfObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
						if (debug) {
							response.getWriter().println("scfObj:" + scfObj);
						}
						if (scfObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
							if (scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								JsonObject insertObj = scfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
								
								if (insertObj.has("__metadata")) {
									insertObj.remove("__metadata");
								}
								if (jsonInput.has("CPGUID") && !jsonInput.get("CPGUID").isJsonNull()) {
									insertObj.addProperty("CPGUID", jsonInput.get("CPGUID").getAsString());
								}

								if (jsonInput.has("CPTypeID") && !jsonInput.get("CPTypeID").isJsonNull()) {
									insertObj.addProperty("CPTypeID", jsonInput.get("CPTypeID").getAsString());
								}

								if (jsonInput.has("AggregatorID") && !jsonInput.get("AggregatorID").isJsonNull()) {
									insertObj.addProperty("AggregatorID", jsonInput.get("AggregatorID").getAsString());
								}

								if (jsonInput.has("OfferAmt") && !jsonInput.get("OfferAmt").isJsonNull()) {
									insertObj.addProperty("OfferAmt", jsonInput.get("OfferAmt").getAsString());
								}

								if (jsonInput.has("OfferTenure") && !jsonInput.get("OfferTenure").isJsonNull()) {
									insertObj.addProperty("OfferTenure", jsonInput.get("OfferTenure").getAsString());
								}

								if (jsonInput.has("Rate") && !jsonInput.get("Rate").isJsonNull()) {
									insertObj.addProperty("Rate", jsonInput.get("Rate").getAsString());
								}

								if (jsonInput.has("AccountNo") && !jsonInput.get("AccountNo").isJsonNull()) {
									insertObj.addProperty("AccountNo", jsonInput.get("AccountNo").getAsString());
								}

								if (jsonInput.has("NoOfChequeReturns") && !jsonInput.get("NoOfChequeReturns").isJsonNull()) {
									insertObj.addProperty("NoOfChequeReturns", jsonInput.get("NoOfChequeReturns").getAsString());
								}

								if (jsonInput.has("PaymentDelayDays12Months") && !jsonInput.get("PaymentDelayDays12Months").isJsonNull()) {
									insertObj.addProperty("PaymentDelayDays12Months", jsonInput.get("PaymentDelayDays12Months").getAsString());
								}

								if (jsonInput.has("BusinessVintageOfDealer") && !jsonInput.get("BusinessVintageOfDealer").isJsonNull()) {
									insertObj.addProperty("BusinessVintageOfDealer", jsonInput.get("BusinessVintageOfDealer").getAsString());
								}

								if (jsonInput.has("PurchasesOf12Months") && !jsonInput.get("PurchasesOf12Months").isJsonNull()) {
									insertObj.addProperty("PurchasesOf12Months", jsonInput.get("PurchasesOf12Months").getAsString());
								}

								if (jsonInput.has("DealersOverallScoreByCorp") && !jsonInput.get("DealersOverallScoreByCorp").isJsonNull()) {
									insertObj.addProperty("DealersOverallScoreByCorp", jsonInput.get("DealersOverallScoreByCorp").getAsString());
								}

								if (jsonInput.has("CorpRating") && !jsonInput.get("CorpRating").isJsonNull()) {
									insertObj.addProperty("CorpRating", jsonInput.get("CorpRating").getAsString());
								}

								if (jsonInput.has("DealerVendorFlag") && !jsonInput.get("DealerVendorFlag").isJsonNull()) {
									insertObj.addProperty("DealerVendorFlag", jsonInput.get("DealerVendorFlag").getAsString());
								}

								if (jsonInput.has("ConstitutionType") && !jsonInput.get("ConstitutionType").isJsonNull()) {
									insertObj.addProperty("ConstitutionType", jsonInput.get("ConstitutionType").getAsString());
								}

								if (jsonInput.has("MaxLimitPerCorp") && !jsonInput.get("MaxLimitPerCorp").isJsonNull()) {
									insertObj.addProperty("MaxLimitPerCorp", jsonInput.get("MaxLimitPerCorp").getAsString());
								}

								if (jsonInput.has("salesOf12Months") && !jsonInput.get("salesOf12Months").isJsonNull()) {
									insertObj.addProperty("salesOf12Months", jsonInput.get("salesOf12Months").getAsString());
								}

								if (jsonInput.has("Currency") && !jsonInput.get("Currency").isJsonNull()) {
									insertObj.addProperty("Currency", jsonInput.get("Currency").getAsString());
								}

								if (jsonInput.has("StatusID") && !jsonInput.get("StatusID").isJsonNull()) {
									insertObj.addProperty("StatusID", jsonInput.get("StatusID").getAsString());
								}

								if (jsonInput.has("MCLR6Rate") && !jsonInput.get("MCLR6Rate").isJsonNull()) {
									insertObj.addProperty("MCLR6Rate", jsonInput.get("MCLR6Rate").getAsString());
								}

								if (jsonInput.has("InterestRateSpread") && !jsonInput.get("InterestRateSpread").isJsonNull()) {
									insertObj.addProperty("InterestRateSpread", jsonInput.get("InterestRateSpread").getAsString());
								}

								if (jsonInput.has("TenorOfPayment") && !jsonInput.get("TenorOfPayment").isJsonNull()) {
									insertObj.addProperty("TenorOfPayment", jsonInput.get("TenorOfPayment").getAsString());
								}

								if (jsonInput.has("ADDLNPRDINTRateSP") && !jsonInput.get("ADDLNPRDINTRateSP").isJsonNull()) {
									insertObj.addProperty("ADDLNPRDINTRateSP", jsonInput.get("ADDLNPRDINTRateSP").getAsString());
								}

								if (jsonInput.has("AddlnTenorOfPymt") && !jsonInput.get("AddlnTenorOfPymt").isJsonNull()) {
									insertObj.addProperty("AddlnTenorOfPymt", jsonInput.get("AddlnTenorOfPymt").getAsString());
								}

								if (jsonInput.has("DefIntSpread") && !jsonInput.get("DefIntSpread").isJsonNull()) {
									insertObj.addProperty("DefIntSpread", jsonInput.get("DefIntSpread").getAsString());
								}

								if (jsonInput.has("ProcessingFee") && !jsonInput.get("ProcessingFee").isJsonNull()) {
									insertObj.addProperty("ProcessingFee", jsonInput.get("ProcessingFee").getAsString());
								}

								if (jsonInput.has("ECustomerID") && !jsonInput.get("ECustomerID").isJsonNull()) {
									insertObj.addProperty("ECustomerID", jsonInput.get("ECustomerID").getAsString());
								}

								if (jsonInput.has("EContractID") && !jsonInput.get("EContractID").isJsonNull()) {
									insertObj.addProperty("EContractID", jsonInput.get("EContractID").getAsString());
								}

								if (jsonInput.has("ApplicationNo") && !jsonInput.get("ApplicationNo").isJsonNull()) {
									insertObj.addProperty("ApplicationNo", jsonInput.get("ApplicationNo").getAsString());
								}

								if (jsonInput.has("CallBackStatus") && !jsonInput.get("CallBackStatus").isJsonNull()) {
									insertObj.addProperty("CallBackStatus", jsonInput.get("CallBackStatus").getAsString());
								}

								if (jsonInput.has("ECompleteDate") && !jsonInput.get("ECompleteDate").isJsonNull()) {
									insertObj.addProperty("ECompleteDate", jsonInput.get("ECompleteDate").getAsString());
								}

								if (jsonInput.has("ECompleteTime") && !jsonInput.get("ECompleteTime").isJsonNull()) {
									insertObj.addProperty("ECompleteTime", jsonInput.get("ECompleteTime").getAsString());
								}

								if (jsonInput.has("ApplicantID") && !jsonInput.get("ApplicantID").isJsonNull()) {
									insertObj.addProperty("ApplicantID", jsonInput.get("ApplicantID").getAsString());
								}

								if (jsonInput.has("LimitPrefix") && !jsonInput.get("LimitPrefix").isJsonNull()) {
									insertObj.addProperty("LimitPrefix", jsonInput.get("LimitPrefix").getAsString());
								}

								if (jsonInput.has("InterestSpread") && !jsonInput.get("InterestSpread").isJsonNull()) {
									insertObj.addProperty("InterestSpread", jsonInput.get("InterestSpread").getAsString());
								}

								if (jsonInput.has("DDBActive") && !jsonInput.get("DDBActive").isJsonNull()) {
									insertObj.addProperty("DDBActive", jsonInput.get("DDBActive").getAsString());
								}

								if (jsonInput.has("ProcessFeePerc") && !jsonInput.get("ProcessFeePerc").isJsonNull()) {
									insertObj.addProperty("ProcessFeePerc", jsonInput.get("ProcessFeePerc").getAsString());
								}

								if (jsonInput.has("ValidTo") && !jsonInput.get("ValidTo").isJsonNull()) {
									insertObj.addProperty("ValidTo", jsonInput.get("ValidTo").getAsString());
								}

								if (jsonInput.has("CreatedBy") && !jsonInput.get("CreatedBy").isJsonNull()) {
									insertObj.addProperty("CreatedBy", jsonInput.get("CreatedBy").getAsString());
								}

								if (jsonInput.has("CreatedAt") && !jsonInput.get("CreatedAt").isJsonNull()) {
									insertObj.addProperty("CreatedAt", jsonInput.get("CreatedAt").getAsString());
								}

								if (jsonInput.has("CreatedOn") && !jsonInput.get("CreatedOn").isJsonNull()) {
									insertObj.addProperty("CreatedOn", jsonInput.get("CreatedOn").getAsString());
								}

								if (jsonInput.has("Source") && !jsonInput.get("Source").isJsonNull()) {
									insertObj.addProperty("Source", jsonInput.get("Source").getAsString());
								}

								if (jsonInput.has("SourceReferenceID") && !jsonInput.get("SourceReferenceID").isJsonNull()) {
									insertObj.addProperty("SourceReferenceID", jsonInput.get("SourceReferenceID").getAsString());
								}

								long changedOn = commonUtils.getCreatedOnDate();
								String changedAt = commonUtils.getCreatedAtTime();
								String changedBy = commonUtils.getUserPrincipal(request, "name", response);
								insertObj.addProperty("ChangedBy", changedBy);
								insertObj.addProperty("ChangedAt", changedAt);
								insertObj.addProperty("ChangedOn", "/Date(" + changedOn + ")/");
								executeUrl = oDataUrl + "SupplyChainFinances('" + jsonInput.get("ID").getAsString() + "')";
								if (debug) {
									response.getWriter().println("insertObj:" + insertObj);
									response.getWriter().println("executeUrl:" + executeUrl);
								}
								JsonObject updatedObj = commonUtils.executeUpdate(executeUrl, userpass, response, insertObj, request, debug, "PYGWHANA");
								if (debug) {
									response.getWriter().println("updatedObj:" + updatedObj);
								}
								response.getWriter().println(updatedObj);
							} else {

								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", "Record doesn't for the input ID");
								response.getWriter().println(resObj);

							}
						} else {
							response.getWriter().println(scfObj);
						}
					} else {
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Input Payload doesn't contains a ID Property");
						response.getWriter().println(resObj);
					}
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Empty input Payload received");
					response.getWriter().println(resObj);
				}
			} catch (Exception ex) {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer(ex.getClass().getCanonicalName() + "--->");
				if(ex.getLocalizedMessage()!=null){
					buffer.append(ex.getLocalizedMessage()+"--->");
				}
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message",buffer.toString());
				response.getWriter().println(resObj);
			}
		}
	}
}
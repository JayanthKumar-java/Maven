package com.arteriatech.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;

import com.arteriatech.logs.ODataLogs;
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
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

public class RenewalSCF1Reset extends HttpServlet {

	private static final long serialVersionUID = 1L;
	// private final String CPI_CONNECTION_DESTINATION = "CPIConnect";

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonParser parser = new JsonParser();
		String inputPayload = "", wsURL = "", userName = "", passWord = "", userpass = "", renewalSCF1Reset = "", renewalSCF1Resetres = "";
		boolean debug = false;
		JsonObject inpJsonPayLoad = new JsonObject();
		Properties properties = new Properties();
		ODataLogs odataLogs = new ODataLogs();
		String errorMessage = "";
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				inpJsonPayLoad = (JsonObject) parser.parse(inputPayload);
				if (inpJsonPayLoad.has("debug") && !inpJsonPayLoad.get("debug").isJsonNull() && inpJsonPayLoad.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
					inpJsonPayLoad.remove("debug");
				}
				if (debug) {
					response.getWriter().println("Input Payload :" + inpJsonPayLoad);

				}
				if (inpJsonPayLoad.has("SCF1GUID")) {
					if (inpJsonPayLoad.get("SCF1GUID").isJsonNull() || inpJsonPayLoad.get("SCF1GUID").getAsString().equalsIgnoreCase("")) {
						errorMessage = "SCF1GUID Missing in the input Payload";
					}
				} else {
					errorMessage = "SCF1GUID Missing in the input Payload";
				}
				if (errorMessage.equalsIgnoreCase("")) {
					// long changedOn = commonUtils.getCreatedOnDate();
					String changedOnDate = commonUtils.getCurrentDate("yyyy-MM-dd");
					String changedAt = commonUtils.getCurrentTime();
					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					inpJsonPayLoad.addProperty("ChangedOn", changedOnDate);
					inpJsonPayLoad.addProperty("ChangedAt", changedAt);
					inpJsonPayLoad.addProperty("ChangedBy", changedBy);
					// Context ctxDestFact = new InitialContext();
					// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
					// DestinationConfiguration cpiConfig = configuration.getConfiguration(DestinationUtils.CPI_CONNECT);
					DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
										.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
					Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
										.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
					Destination cpiConfig = destinationAccessor.get();
					HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
					HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

					wsURL = cpiConfig.get("URL").get().toString();
					if (debug) {
						response.getWriter().println("WsURL :" + wsURL);
					}
					userName = cpiConfig.get("User").get().toString();
					passWord = cpiConfig.get("Password").get().toString();
					userpass = userName + ":" + passWord;
					renewalSCF1Reset = properties.getProperty("RenewalSCF1Reset");
					wsURL = wsURL.concat(renewalSCF1Reset);
					if (debug) {
						response.getWriter().println("RenewalSCF1Reset  Url: " + wsURL);
						response.getWriter().println("input Payload: " + inpJsonPayLoad);
					}
					URL url = new URL(wsURL);
					HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
					byte[] bytes = inpJsonPayLoad.toString().getBytes("UTF-8");
					urlConnection.setRequestMethod("PUT");
					urlConnection.setRequestProperty("Content-Type", "application/json");
					urlConnection.setRequestProperty("charset", "utf-8");
					urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
					urlConnection.setRequestProperty("Accept", "application/json");
					urlConnection.setDoOutput(true);
					urlConnection.setDoInput(true);
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
							response.getWriter().println("Cpi Response: " + sb.toString());
						}
						renewalSCF1Resetres = sb.toString();
						JsonParser jsonParser = new JsonParser();
						JsonObject responseJson = (JsonObject) jsonParser.parse(renewalSCF1Resetres);
						if (responseJson.get("Status").getAsString().equalsIgnoreCase("000001")) {
							String aggrid = inpJsonPayLoad.get("AGGRID").getAsString();
							String cpType = inpJsonPayLoad.get("CPType").getAsString();
							String cpGuid = inpJsonPayLoad.get("CPGuid").getAsString();
							
							boolean isDigit = cpGuid.matches("[0-9]+");
							if (isDigit) {
								if (cpGuid.length() >=10) {
									try {
										cpGuid = Integer.valueOf(cpGuid).toString();
									} catch (NumberFormatException ex) {

									}
								}
							}
							
							if(debug){
								response.getWriter().println("cpGuid:"+cpGuid);
							}

							String odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
							String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
							String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
							userpass = username + ":" + password;
							String loginID = commonUtils.getLoginID(request, response, debug);
							String todayDate = formateDateIn();
							if(debug){
								response.getWriter().println("todayDate:"+todayDate);
							}
							String executeUrl = odataUrl + "ARTEC_PC_SUBSCRIPTION?$filter=AGGREGATORID%20eq%20%27" + aggrid + "%27%20and%20CP_GUID%20eq%20%27" + cpGuid + "%27%20and%20CP_TYPE%20eq%20%27" + cpType + "%27%20and%20VALID_FROM%20gt%20datetime%27"+todayDate+"T00:00:00%27%20and%20PAY_STATUS_ID%20eq%20%27"+"01"+"%27";
							if (debug) {
								response.getWriter().println("executeUrl:" + executeUrl);
							}
							JsonObject subscriptionObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
							if (debug) {
								response.getWriter().println("subscriptionObj:" + subscriptionObj);
							}
							List<String> subGuids=new ArrayList<>();
							
							if (subscriptionObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								JsonArray subscriptionLst = subscriptionObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
								
								if (subscriptionLst.size() > 0) {
									for(int i=0;i<subscriptionLst.size();i++){
										JsonObject deleteSubscrObj = subscriptionLst.get(i).getAsJsonObject();
										subGuids.add(deleteSubscrObj.get("SUBSCRIPTION_GUID").getAsString());
									}
									
									JsonObject executeDelResponse=deleteEligibilityRecords(odataUrl, userpass, debug, response, subGuids);
									
									int stepNo = 1;
									StringBuffer buffer=new StringBuffer("SubscriptionGuids:");
									subGuids.forEach(subscriptionGud->buffer.append(subscriptionGud).append(","));
									String aLogHID = odataLogs.insertApplicationLogs(request, response, "Java", "RenewalSCF1Reset", buffer.toString(), stepNo + "", request.getServletPath(), odataUrl, userpass, aggrid, loginID, debug);
									//JsonObject executeDelResponse = commonUtils.executeODataDelete(executeUrl, userpass, response, request, debug);
									if (debug) {
										response.getWriter().println("executeDelResponse:" + executeDelResponse);
									}
									if (executeDelResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
										stepNo++;
										odataLogs.insertDirectDebitMessageForAppLogs(request, response, aLogHID, "I", "/ARTEC/PY", buffer + "", "Record Deleted Successfully", odataUrl, userpass, aggrid, debug);
										response.getWriter().println(responseJson);
									} else {
										stepNo++;
										odataLogs.insertDirectDebitMessageForAppLogs(request, response, aLogHID, "I", "/ARTEC/PY", executeDelResponse + "", "Record not deleted", odataUrl, userpass, aggrid, debug);
										response.getWriter().println(executeDelResponse);
									}

								} else {
									// print cpi response
									response.getWriter().println(responseJson);
								}

							} else {
								response.getWriter().println(subscriptionObj);
							}
						} else {
							response.getWriter().println(responseJson);
						}

					} else {
						JsonObject responseJson = new JsonObject();
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
						renewalSCF1Resetres = sb.toString();
						responseJson.addProperty("Status", "000002");
						responseJson.addProperty("ErrorCode", "J001");
						responseJson.addProperty("Message", urlConnection.getResponseMessage());
						if (debug)
							response.getWriter().println("responseJson: " + responseJson);
						response.getWriter().println(responseJson);
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "/ARTEC/J001");
					result.addProperty("Message", errorMessage);
					response.getWriter().println(new Gson().toJson(result));
				}

			} else {
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "No inputPayload is received in the request");
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
	
	public String formateDateIn() throws Exception, IOException {
		try {
			 java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
			 return  date.toString();
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public JsonObject deleteEligibilityRecords(String odataUrl, String userpass, boolean debug, HttpServletResponse response, List<String> subscriptionGuids) {
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put("Content-Type", "application/json");
			changeSetHeaders.put("Accept", "application/json");
			changeSetHeaders.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			for (int i = 0; i < subscriptionGuids.size(); i++) {
				String subscriptionGuid =subscriptionGuids.get(i);
				// Changes on 20231120
				// String executeUrl = odataUrl + "ARTEC_PC_SUBSCRIPTION('" + subscriptionGuid + "')";
				String executeUrl = "ARTEC_PC_SUBSCRIPTION('" + subscriptionGuid + "')";
				if (debug) {
					response.getWriter().println("Delete Subscription Record query: " + executeUrl);
				}
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
				changeSet.add(changeRequest);
			}
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			HttpResponse batchResponse = HttpClientBuilder.create().build().execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordDeleted = true;
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (!statusCode.equalsIgnoreCase("204")) {
					recordDeleted = false;
					if (debug) {
						response.getWriter().println("Error getting while deleting records:" + singleRes.getBody());
					}
					resObj.addProperty("Message", "deleting Subscription Records Failed, error message:" + singleRes.getBody());
					break;
				}
			}
			if (recordDeleted) {
				resObj.addProperty("Message", "Record Deleted Successfully");
				resObj.addProperty("ErrorCode", "");
				resObj.addProperty("Status", "000001");

			} else {
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());

		}
		return resObj;
	}

}

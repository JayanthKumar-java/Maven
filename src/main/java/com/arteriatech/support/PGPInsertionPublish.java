package com.arteriatech.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
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

public class PGPInsertionPublish extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String CPI_CONNECTION_DESTINATION = "CPIConnect";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		String inputpayload="";
		JsonParser parser=new JsonParser();
		boolean debug=false;
		Properties properties = new Properties();
		String userName="",passWord="",userpass="";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			inputpayload = commonUtils.getGetBody(request, response);
			if (inputpayload != null && !inputpayload.equalsIgnoreCase("")) {
				JsonObject inputJson = (JsonObject) parser.parse(inputpayload);
				if (inputJson.has("debug") && inputJson.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
					inputJson.remove("debug");
				}
				if (debug) {
					response.getWriter().println("Received Input Payload:" + inputpayload);
				}
				String message = validateInputpayload(inputJson);
				if (message.equalsIgnoreCase("")) {
					// Context ctxDestFact = new InitialContext();
					// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
					// 		.lookup("java:comp/env/connectivityConfiguration");
					// DestinationConfiguration cpiConfig = configuration.getConfiguration(CPI_CONNECTION_DESTINATION);
					DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
							.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
					Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
							.tryGetDestination(CPI_CONNECTION_DESTINATION, options);
					Destination cpiConfig = destinationAccessor.get();

					String wsURL = cpiConfig.get("URL").get().toString();
					if (debug) {
						response.getWriter().println("WsURL :" + wsURL);
					}
					userName = cpiConfig.get("User").get().toString();
					passWord = cpiConfig.get("Password").get().toString();
					userpass = userName + ":" + passWord;
					String pgpInsertionPublish = properties.getProperty("PGPInsertionPublish");
					wsURL = wsURL.concat(pgpInsertionPublish);
					if (debug) {
						response.getWriter().println("PGPInsertionPublish Url: " + wsURL);
					}
					URL url = new URL(wsURL);
					HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
					byte[] bytes = inputJson.toString().getBytes("UTF-8");
					urlConnection.setRequestMethod("GET");
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
					osw.write(inputJson.toString());
					osw.flush();
					osw.close();
					int resCode = urlConnection.getResponseCode();
					if (debug) {
						response.getWriter().println("responseCode: " + resCode);
					}
					if ((resCode / 100) == 2 || (resCode / 100) == 3) {
						StringBuffer sb = new StringBuffer();
						BufferedReader br = new BufferedReader(
								new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
						String line = null;
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
						br.close();
						if (debug) {
							response.getWriter().println("sb: " + sb.toString());
						}
						
						JSONObject jsonRes = new JSONObject(sb.toString());
						jsonRes.put("Remarks", "");
						if (debug) {
							response.getWriter().println("responseJson: " + jsonRes);
						}
						response.getWriter().println(jsonRes);
					} else {
						JsonObject returnObj = new JsonObject();
						StringBuffer sb = new StringBuffer();
						BufferedReader br = new BufferedReader(
								new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
						String line = null;
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
						br.close();
						int responseCode = urlConnection.getResponseCode();
						String responseMessage = urlConnection.getResponseMessage();
						if (debug) {
							response.getWriter().println("getErrorStream: " + sb.toString());
						}
						// sb.toString();
						returnObj.addProperty("Status", "000002");
						returnObj.addProperty("ErrorCode", "J002");
						returnObj.addProperty("Remarks", "");
						returnObj.addProperty("message",
								"responseCode:" + responseCode + "ResponseMessage:" + responseMessage);
						if (debug)
							response.getWriter().println("responseJson: " + returnObj);
						response.getWriter().println(returnObj);
					}

				} else {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", message);
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
					response.getWriter().println(resObj);
				}
			} else {
				// empty input Payload received
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Empty Inputpayload received");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			JsonObject resObj = new JsonObject();
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			response.getWriter().println(resObj);
		}
	}

	private String validateInputpayload(JsonObject inputJson) {
		String message="";
		try{
			
			if (inputJson.has("FacilityType")) {
				if (inputJson.get("FacilityType").isJsonNull()
						|| inputJson.get("FacilityType").getAsString().equalsIgnoreCase("")) {
						message = "FacilityType missing in the input payload";
				}

			} else {
					message = "FacilityType missing in the input payload";
			}
			
			if (inputJson.has("CPGuid")) {
				if (inputJson.get("CPGuid").isJsonNull()
						|| inputJson.get("CPGuid").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "CPGuid missing in the input payload";
					} else {
						message = message + ",CPGuid missing in the input payload";
					}
				}

			} else {
				if (message.equalsIgnoreCase("")) {
					message = "CPGuid missing in the input payload";
				} else {
					message = message + ",CPGuid missing in the input payload";
				}
			}
			
			if (inputJson.has("CPType")) {
				if (inputJson.get("CPType").isJsonNull()
						|| inputJson.get("CPType").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "CPType missing in the input payload";
					} else {
						message = message + ",CPType missing in the input payload";
					}
				}

			} else {
				if (message.equalsIgnoreCase("")) {
					message = "CPType missing in the input payload";
				} else {
					message = message + ",CPType missing in the input payload";
				}
			}
			
			if (inputJson.has("CustID")) {
				if (inputJson.get("CustID").isJsonNull()
						|| inputJson.get("CustID").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "CustID missing in the input payload";
					} else {
						message = message + ",CustID missing in the input payload";
					}
				}

			} else {
				if (message.equalsIgnoreCase("")) {
					message = "CustID missing in the input payload";
				} else {
					message = message + ",CustID missing in the input payload";
				}
			}
			if (inputJson.has("AggregatorID")) {
				if (inputJson.get("AggregatorID").isJsonNull()
						|| inputJson.get("AggregatorID").getAsString().equalsIgnoreCase("")) {
					if (message.equalsIgnoreCase("")) {
						message = "AggregatorID missing in the input payload";
					} else {
						message = message + ",AggregatorID missing in the input payload";
					}
				}

			} else {
				if (message.equalsIgnoreCase("")) {
					message = "AggregatorID missing in the input payload";
				} else {
					message = message + ",AggregatorID missing in the input payload";
				}
			}

		return 	message;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			return buffer.toString();
		}
	}
}
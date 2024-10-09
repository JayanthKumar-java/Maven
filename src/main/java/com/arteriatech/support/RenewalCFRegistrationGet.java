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

import com.google.gson.Gson;
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

public class RenewalCFRegistrationGet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String CPI_CONNECTION_DESTINATION = "CPIConnect";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JsonParser parser = new JsonParser();
		String inputPayload = "", wsURL = "", userName = "", passWord = "", userpass = "",renewalCfGet = "",
				renewalCfGetres = "";
		boolean debug = false;
		JsonObject inpJsonPayLoad = new JsonObject();
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		String errorMessage="";
		try {
			inputPayload = request.getParameter("RenewalCFGet");
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				inpJsonPayLoad = (JsonObject) parser.parse(inputPayload);
				if (inpJsonPayLoad.has("debug") && !inpJsonPayLoad.get("debug").isJsonNull()
						&& inpJsonPayLoad.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
					inpJsonPayLoad.remove("debug");
				}
				if (debug) {
					response.getWriter().println("Input Payload from UI :" + inpJsonPayLoad);

				}
				if (inpJsonPayLoad.has("AGGRID")) {
					if (inpJsonPayLoad.get("AGGRID").isJsonNull()
							|| inpJsonPayLoad.get("AGGRID").getAsString().equalsIgnoreCase("")) {
						errorMessage = "AGGRID Missing in the payload";
					}
				} else {
					errorMessage = "AGGRID Missing in the payload";
				}
				if(errorMessage.equalsIgnoreCase("")){
				// Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
				// 		.lookup("java:comp/env/connectivityConfiguration");
				// DestinationConfiguration cpiConfig = configuration.getConfiguration(CPI_CONNECTION_DESTINATION);
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(CPI_CONNECTION_DESTINATION, options);
				Destination cpiConfig = destinationAccessor.get();

				wsURL = cpiConfig.get("URL").get().toString();
				if (debug) {
					response.getWriter().println("WsURL :" + wsURL);
				}

				userName = cpiConfig.get("User").get().toString();
				passWord = cpiConfig.get("Password").get().toString();
				userpass = userName + ":" + passWord;
				renewalCfGet = properties.getProperty("RenewalCFRegistrationGet");
				wsURL = wsURL.concat(renewalCfGet);
				if (debug) {
					response.getWriter().println("CPI Input Json Payload " + inpJsonPayLoad);
					response.getWriter().println("WsURL :" + wsURL);
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
					renewalCfGetres = sb.toString();
					JsonParser jsonParser = new JsonParser();
					JsonObject responseJson = (JsonObject) jsonParser.parse(renewalCfGetres);
					responseJson.addProperty("Remarks", "");
					if (debug)
						response.getWriter().println("responseJson: " + responseJson);
					response.getWriter().println(responseJson);

				} else {
					JsonObject responseJson = new JsonObject();
					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();
					if (debug) {
						response.getWriter().println("getErrorStream: " + sb.toString());
					}
					renewalCfGetres = sb.toString();
					responseJson.addProperty("Status", "000002");
					responseJson.addProperty("ErrorCode", "J001");
					responseJson.addProperty("Message", urlConnection.getResponseMessage());
					if (debug)
						response.getWriter().println("responseJson: " + responseJson);
					response.getWriter().println(responseJson);
				}
				}else{
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
				result.addProperty("Message", "Empty input Payload Received");
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
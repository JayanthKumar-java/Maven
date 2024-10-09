package com.arteriatech.cf;

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

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import io.vavr.control.Try;

public class LEIValidate extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String CPI_CONNECTION_DESTINATION = "CPIConnect";
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		boolean debug = false, mandatoryCheckPass = true;
		String requestedPayload = "", wsURL = "", errorCode = "", errorMsg = "";
		String userName = "", passWord = "", userpass = "", aggregatorId = "", LEIValidateEndPoint = "",
				LeiDestUrl = "", leiResponse = "";
		CommonUtils commonUtils=new CommonUtils();
				
		try {
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			String destName = CPI_CONNECTION_DESTINATION;
			 requestedPayload = getGetBody(request, response);
			//payloadRequest = request.getParameter("LEINumber");
			if (debug) {
				response.getWriter().println("payloadRequest: " + requestedPayload);
			}
			if (!requestedPayload.equals("") && requestedPayload.trim().length()>0) {
				JsonParser parser = new JsonParser();
				JsonObject inputPayload = (JsonObject) parser.parse(requestedPayload);
				if (debug) {
					response.getWriter().println("Proper Json input: " + inputPayload);
				}
				if (inputPayload.has("debug") && null != inputPayload.get("debug").getAsString()
						&& inputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
					response.getWriter().println("doGet.inputPayload: " + inputPayload);
				}
				if (mandatoryCheckPass) {
					// Context ctxDestFact = new InitialContext();
					// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
					// 		.lookup("java:comp/env/connectivityConfiguration");
					// DestinationConfiguration leiConfig = configuration.getConfiguration(destName);
					DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
							.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
					Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
							.tryGetDestination(destName, options);
					Destination leiConfig = destinationAccessor.get();
					wsURL = leiConfig.get("URL").get().toString();
					if (debug) {
						response.getWriter().println("wsURL: " + wsURL);
					}
					if (wsURL != null && wsURL.trim().length() > 0) {
						userName = leiConfig.get("User").get().toString();
						passWord = leiConfig.get("Password").get().toString();
						userpass = userName + ":" + passWord;
						aggregatorId = leiConfig.get("AggregatorID").get().toString();
						//agrgtrID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
						LEIValidateEndPoint = properties.getProperty("LEIValidate");
						LeiDestUrl = wsURL.concat(LEIValidateEndPoint);
						if (debug) {
							response.getWriter().println("aggregatorId: " + aggregatorId);
							response.getWriter().println("LeiDestUrl: " + LeiDestUrl);
						}

						if (aggregatorId != null && !aggregatorId.equals("")) {
							inputPayload.addProperty("AGGRID", aggregatorId);

						}
						
						if (debug) {
							response.getWriter().println("inputPayload: " + inputPayload);
						}
						URL url = new URL(LeiDestUrl);
						HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
						byte[] postDataBytes = inputPayload.toString().getBytes("UTF-8");
						urlConnection.setRequestMethod("GET");
						urlConnection.setRequestProperty("Content-Type", "application/json");
						urlConnection.setRequestProperty("charset", "utf-8");
						urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
						urlConnection.setRequestProperty("Accept", "application/json");
						urlConnection.setDoOutput(true);
						urlConnection.setDoInput(true);

						String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
						urlConnection.setRequestProperty("Authorization", basicAuth);
						urlConnection.connect();

						OutputStream os = urlConnection.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
						osw.write(inputPayload.toString());
						osw.flush();
						osw.close();
						
						int responseCode = urlConnection.getResponseCode();
						if (debug)
							response.getWriter().println("responseCode: " + responseCode);

						if((responseCode/100) == 2){
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
							leiResponse = sb.toString();
							JsonParser leiResponseParser = new JsonParser();
							JsonObject responseJson = (JsonObject) leiResponseParser.parse(leiResponse);
							responseJson.addProperty("Remarks", "");
							if (debug)
								response.getWriter().println("responseJson: " + responseJson);
							response.getWriter().println(responseJson);
						}else{
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
							leiResponse = sb.toString();
							JsonParser leiResponseParser = new JsonParser();
							JsonObject responseJson = (JsonObject) leiResponseParser.parse(leiResponse);
							responseJson.addProperty("Remarks", "");
							if (debug)
								response.getWriter().println("responseJson: " + responseJson);
							response.getWriter().println(responseJson);
						}
					}

				}

			} else {
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "No inputPayload is received in the request");
				result.addProperty("Remarks", "No inputPayload is received in the request");
				response.getWriter().println(new Gson().toJson(result));
			}

		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			JsonObject result = new JsonObject();
			errorCode = "E173";
			errorMsg = properties.getProperty(errorCode);
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg + ". " + e.getClass() + ":" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));

		}
	}
	
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
		return body;
	}

}
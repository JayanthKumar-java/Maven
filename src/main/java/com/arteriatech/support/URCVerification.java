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

import com.arteriatech.pg.CommonUtils;
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

public class URCVerification extends HttpServlet{
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private  final String CPI_CONNECTION_DESTINATION = "CPIConnect";
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName="",passWord="",userpass="",endPointURL="",inputPayload="",urcVerificationEndpoint="",wsURL="";
		boolean debug=false;
		CommonUtils commonUtils=new CommonUtils();
		Properties properties = new Properties();
		JsonObject cpiInput=new JsonObject();
		String urcVerificationRes="",aggregatorID="";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			inputPayload = commonUtils.getGetBody(request, response);
			if (debug) {
				response.getWriter().println("Received Input Payload:" + inputPayload);
			}
			String errorMessage = validateInputPayload(inputPayload);
			if (errorMessage.equalsIgnoreCase("")) {
				JsonObject jsonPayload = (JsonObject) new JsonParser().parse(inputPayload);
				String udyamNumber = jsonPayload.get("essentials").getAsJsonObject().get("udyamNumber").getAsString();
				if(jsonPayload.has("debug")&&jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")){
					debug=true;
				}
				// Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
				// 		.lookup("java:comp/env/connectivityConfiguration");
				// DestinationConfiguration cpiConfig = configuration.getConfiguration(DestinationUtils.CPI_CONNECT);
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
				Destination cpiConfig = destinationAccessor.get();

				userName = cpiConfig.get("User").get().toString();
				passWord = cpiConfig.get("Password").get().toString();
				wsURL = cpiConfig.get("URL").get().toString();
				userpass = userName + ":" + passWord;
				aggregatorID=cpiConfig.get("AggregatorID").get().toString();
				JsonObject udayNumberObj=new JsonObject();
				udayNumberObj.addProperty("udyamNumber", udyamNumber);
				udayNumberObj.addProperty("AggregatorID", aggregatorID);
				cpiInput.add("essentials", udayNumberObj);
				urcVerificationEndpoint = properties.getProperty("URCGetDetails");
				endPointURL = wsURL + urcVerificationEndpoint;
				if (debug) {
					response.getWriter().println("endPointURL:" + endPointURL);
					response.getWriter().println("UserName:" + userName);
					response.getWriter().println("password:" + passWord);
				}

				URL url = new URL(endPointURL);
				HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
				if (debug) {
					response.getWriter().println("CPI Input :" + cpiInput);
				}

				byte[] bytes = cpiInput.toString().getBytes("UTF-8");
				urlConnection.setRequestMethod("GET");
				urlConnection.setRequestProperty("Content-Type", "text/xml");
				urlConnection.setRequestProperty("charset", "utf-8");
				urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
				urlConnection.setRequestProperty("Accept", "text/xml");
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
				urlConnection.setRequestProperty("Authorization", basicAuth);
				urlConnection.connect();
				OutputStream outputStream = urlConnection.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
				osw.write(cpiInput.toString());
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
						response.getWriter().println("Cpi Response: " + sb.toString());
					}

					urcVerificationRes = sb.toString();
					JsonObject responseJson = (JsonObject) new JsonParser().parse(urcVerificationRes);
					response.getWriter().println(responseJson);
				} else {
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
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", sb.toString());
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
					response.getWriter().println(resObj);
				}
			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", errorMessage);
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				response.getWriter().println(resObj);

			}

		}catch(Exception ex){
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

	private String validateInputPayload(String inputPayload) {
		String errorMessage="";
		try{
			if(inputPayload!=null && !inputPayload.equalsIgnoreCase("")&&inputPayload.trim().length()>0){
				JsonObject inputJson=(JsonObject)new JsonParser().parse(inputPayload);
				if(inputJson.has("essentials")){
					if(inputJson.get("essentials").getAsJsonObject().has("udyamNumber")){
						JsonObject udayNumberObj = inputJson.get("essentials").getAsJsonObject();
						if(udayNumberObj.get("udyamNumber").isJsonNull() || udayNumberObj.get("udyamNumber").getAsString().equalsIgnoreCase("")){
							errorMessage="udyamNumber field is empty in the input payload";
						}
					}else{
						errorMessage="input Payload doesn't Contains a udyamNumber field";
					}
				}else{
					errorMessage="input Payload doesn't Contains a essentials field";
				}
				
			}else{
				errorMessage="Empty Input Payload Received";
			}
			return errorMessage;
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			return buffer.toString();
		}
		
	}
}
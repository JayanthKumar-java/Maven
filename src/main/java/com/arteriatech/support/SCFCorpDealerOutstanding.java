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
public class SCFCorpDealerOutstanding extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private final String CPI_CONNECTION_DESTINATION = "CPIConnect";
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String userName = "", passWord = "", userpass = "", system = "", endPointURL = "", xmlInputPayload = "";
		boolean debug = false;
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			xmlInputPayload = commonUtils.getGetBody(request, response);
			if (debug) {
				response.getWriter().println("Received Input Payload:" + xmlInputPayload);
			}
			if(xmlInputPayload!=null &&! xmlInputPayload.equalsIgnoreCase("")){
				String formateToSoapEnvelope = formateToSoapEnvelope(xmlInputPayload);
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			endPointURL = endPointURL + "SCFCorpDealerOutstanding";
			// Context ctxDestFact = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact
			// 		.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration cpiConfig = configuration.getConfiguration(CPI_CONNECTION_DESTINATION);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(CPI_CONNECTION_DESTINATION, options);
			Destination cpiConfig = destinationAccessor.get();

			userName = cpiConfig.get("User").get().toString();
			passWord = cpiConfig.get("Password").get().toString();
			userpass = userName + ":" + passWord;
			if (debug) {
				response.getWriter().println("endPointURL:" + endPointURL);
				response.getWriter().println("UserName:" + userName);
				response.getWriter().println("password:" + passWord);
			}
			URL url = new URL(endPointURL);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			if (debug) {
				response.getWriter().println("Input to CPI: " + xmlInputPayload);
			}
			byte[] bytes = formateToSoapEnvelope.getBytes("UTF-8");
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
			osw.write(formateToSoapEnvelope);
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
				response.getWriter().println(sb.toString());

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
				response.getWriter().println(sb.toString());
			}
			}else{
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "Empty Inputpayload Received");
				response.getWriter().println(new Gson().toJson(result));	
			}

		} catch (Exception e) {
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			StackTraceElement element[] = e.getStackTrace();
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
			result.addProperty("Message", errorMsg + ". " + e.getClass() + ":" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));

		}

	}
	public String formateToSoapEnvelope(String xmlPayload){
		String openEnvelope="<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		String openHeader="<soap:Header/>";
		String openBody="<soap:Body>";
		String closeBody="</soap:Body>";
		String closeEnvelope="</soap:Envelope>";
		String formatedPayload = openEnvelope.concat(openHeader).concat(openBody).concat(xmlPayload).concat(closeBody).concat(closeEnvelope);
		return formatedPayload;
	}

}

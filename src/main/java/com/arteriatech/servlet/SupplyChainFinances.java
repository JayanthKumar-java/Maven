package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.util.EntityUtils;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Configuration
public class SupplyChainFinances extends HttpServlet {

	JsonObject scfResponse=null;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		boolean debug = false;
		String url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/BPHeaders";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		JsonObject finObj = new JsonObject();
		scfResponse = executeURL(url, userpass, response, debug);
		
		JsonArray scfResponseArray = scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		response.getWriter().println("scfResponseArray  " + scfResponseArray);
		 finObj = new JsonObject();
		 debug=false;
		if (scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			for (int j = 0; j < scfResponseArray.size(); j++) {
				finObj = scfResponseArray.get(j).getAsJsonObject();
				if (finObj.get("AccountNo").isJsonNull()  && finObj.get("AccountNo").getAsString().equalsIgnoreCase("userpass")) {

					// response.getWriter().println("blank");
					// finObj.remove(finObj.get("ID").getAsString());
					String executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances('"+finObj.get("ID").getAsString()+"')";
					  executePostURL(response, executeURL, finObj, req, debug);
				}
			}
			// response.getWriter().println("finalObject  " + finalObject);
			response.getWriter().println("scfResponse  " + scfResponse);
		}
		
		// response.getWriter().println("scfResponseArray "+scfResponseArray);

		// this code remove the entry in jsonobject
		// Iterator<JsonElement> iterator = scfResponseArray.iterator();
		// while (iterator.hasNext()) {
		// JsonObject jsonObject = iterator.next().getAsJsonObject();
		// if(!jsonObject.get("AccountNo").isJsonNull()){
		// String accountNo = jsonObject.get("AccountNo").getAsString();
		// if (accountNo == null || accountNo.trim().isEmpty()) {
		// iterator.remove();
		// }
		// }
		// }
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		boolean debug = false;
		String url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		JsonObject finObj = new JsonObject();
		scfResponse = executeURL(url, userpass, response, debug);
		
		JsonArray scfResponseArray = scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		response.getWriter().println("scfResponseArray  " + scfResponseArray);
		 finObj = new JsonObject();
		 debug=false;
		if (scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			for (int j = 0; j < scfResponseArray.size(); j++) {
				finObj = scfResponseArray.get(j).getAsJsonObject();
				if (finObj.get("AccountNo").isJsonNull() ) {

					// response.getWriter().println("blank");
					// finObj.remove(finObj.get("ID").getAsString());
					String executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances('"+finObj.get("ID").getAsString()+"')";
					  executePostURL(response, executeURL, finObj, req, debug);
				}
			}
			// response.getWriter().println("finalObject  " + finalObject);
			response.getWriter().println("finObj  " + finObj);

		}

	}

	@SuppressWarnings("finally")
	public JsonObject executeURL(String executeURL, String userPass, HttpServletResponse response, boolean debug) {
		DataOutputStream dataOut = null;
		BufferedReader in = null;
		JsonObject jsonObj = null;
		try {

			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			connection.setDoInput(true);

			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String inputLine;
			StringBuffer responseStrBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseStrBuffer.append(inputLine);
			}

			if (debug) {
				response.getWriter().println("Direct Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
			JsonParser parser = new JsonParser();
			jsonObj = (JsonObject) parser.parse(responseStrBuffer.toString());

			if (debug) {
				response.getWriter().println("Json Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
		} catch (Exception e) {
			response.getWriter().println("executeURL.Exception: " + e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println("executeURL.Full Stack Trace: " + buffer.toString());
		} finally {
			return jsonObj;
		}
	}

	public JsonObject executePostURL(HttpServletResponse response,String executeURL, JsonObject approvalObj, HttpServletRequest request,
			boolean debug) throws IOException {
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		String userName = "", password = "", authParam = "";
		JsonObject jsonObj = new JsonObject();
		String data = "";
		// HttpPost postRequest = null;
		try {
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration)
			// ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration =
			// configuration.getConfiguration("PCGWHANA");
			// if (destConfiguration == null)
			// {
			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			// String.format("Destination %s is not found. Hint: Make sure to have the
			// destination configured.", "PYGWHANA"));
			// }

			// String proxyType = destConfiguration.getProperty("ProxyType");
			// String Url=destConfiguration.getProperty("URL");
			// String executeURL=Url+"ApplicationLogs";
			// userName = destConfiguration.getProperty("User");
			// password = destConfiguration.getProperty("Password");
			// authParam = userName + ":"+ password ;
			// String basicAuth = "Basic " +
			// Base64.getEncoder().encodeToString(authParam.getBytes());

			
			String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
			if (debug) {
				// response.getWriter().println("Reading destination properties");
				response.getWriter().println("executeHttpPost.executeURL: " + executeURL);
				// response.getWriter().println("executeHttpPost.proxyType: "+ proxyType);
				// response.getWriter().println("executeHttpPost.userName: "+ userName);
				// response.getWriter().println("executeHttpPost.password: "+ password);
				// response.getWriter().println("executeHttpPost.authParam: "+ authParam);
				// response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
			}

			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("P000000", "DevCFHNDBP@$$wdFeb2024");
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			if (debug) {
				response.getWriter().println("Created Values from insertIntoApproval()--->" + approvalObj);
			}
			// response.getWriter().println("Checking of responseEntity");
			requestEntity = new StringEntity(approvalObj.toString());

			// response.getWriter().println(requestEntity);

			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			// postRequest = new HttpPost(executeURL);
			HttpDelete postRequest = new HttpDelete(executeURL);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("X-HTTP-Method", "DELETE");
			// ((HttpResponse) postRequest).setEntity(requestEntity);
			HttpResponse httpPostResponse = httpClient.execute(postRequest);
			responseEntity = httpPostResponse.getEntity();
			// response.getWriter().println("Checking of responseentity");
			if (debug) {
				response.getWriter().println("Response :" + responseEntity.toString());
			}
			if (httpPostResponse.getEntity().getContentType() != null
					&& httpPostResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpPostResponse.getEntity().getContentType().toString()
						.replaceAll("content-type:", "").trim();
				if (contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					response.getWriter().println("Response :" + data);
					if (debug)
						response.getWriter().println(EntityUtils.toString(responseEntity));
				} else {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					response.getWriter().println(data);
				}
			} else {
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				if (debug)
					response.getWriter().println(EntityUtils.toString(responseEntity));
			}
			int statusCode = httpPostResponse.getStatusLine().getStatusCode();
			if ((statusCode / 100) == 2) {
				JsonParser parser = new JsonParser();
				jsonObj = (JsonObject) parser.parse(data);
				// jsonObj.addProperty("Status", "000001");
				// jsonObj.addProperty("ErrorCode", "");
				// jsonObj.addProperty("Message", "Record Inserted Successfully");
				// response.getWriter().println("Record Inserted Successfully");
			} else {
				// jsonObj.addProperty("Status", "000002");
				// jsonObj.addProperty("ErrorCode", statusCode);
				// jsonObj.addProperty("Message", data);
			}
			return jsonObj;
		} catch (Exception e) {
			// postRequest.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().println("executeUpdate-Exception Stack Trace: " + buffer.toString());

			if (e.getLocalizedMessage() != null) {
				jsonObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
			}
			// jsonObj.addProperty("ErrorCode", "J002");
			//
			// jsonObj.addProperty("Status", "000002");
			//
			// jsonObj.addProperty("Message", buffer.toString());

		} finally {
			httpClient.close();
		}
		return jsonObj;
	}

	@Bean
	public ServletRegistrationBean<SupplyChainFinances> SupplyChainFinancesBean() {
		ServletRegistrationBean<SupplyChainFinances> bean = new ServletRegistrationBean<>(new SupplyChainFinances(),
				"/SupplyChainFinances");
		return bean;
	}
}
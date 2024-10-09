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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;

/**
 * Servlet implementation class eSignContractCALCompanies
 */
@WebServlet("/eSignContractCALCompanies")
public class eSignContractCALCompanies extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CPI_CONNECTION_DESTINATION =  "CPIConnect";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public eSignContractCALCompanies() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String customerNo="", payloadRequest="", destURL="", errorCode="", errorMsg="", sessionID="", corpID="", aggrID="", wsURL="", renewalDBResponse="", scfOffersResponse="", renewalApplyResponse="";
		boolean isRequestFromCloud=false;
		JsonObject renewalApplyJsonResponse = null;
		JsonObject renewalDBResponseJson = new JsonObject();
		boolean debug = false, mandatoryCheckPass=true;
//		DestinationConfiguration destConfiguration = null;
		Destination destConfiguration = null;
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		String buyerorSellerCode="", mandatoryCheckMsg="Mandatory inputs missing: ";
		try{
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			
			String destName = CPI_CONNECTION_DESTINATION;
			payloadRequest = request.getParameter("eSignContractCALCompanies");//getGetBody(request, response);
			if(debug)
				response.getWriter().println("payloadRequest: "+payloadRequest);
			
			if(null != payloadRequest && payloadRequest.trim().length()> 0){
				JsonParser parser = new JsonParser();
				try{
					JsonObject inputPayload = (JsonObject)parser.parse(payloadRequest);
					
					if(debug)
						response.getWriter().println("Proper Json input: "+inputPayload);
					
					try{
						if (inputPayload.has("debug") && null != inputPayload.get("debug").getAsString() && 
								inputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
							debug = true;
							response.getWriter().println("doGet.inputPayload: "+inputPayload); 
						}
					}catch(Exception e){
						debug = false;
					}
					
					/*if(inputPayload.has("buyerorSellerCode") && inputPayload.get("buyerorSellerCode").getAsString().trim().length() > 0){
						buyerorSellerCode = inputPayload.get("buyerorSellerCode").getAsString();
						
						String formattedStr = "";
						try{
							int number = Integer.parseInt(buyerorSellerCode);
							formattedStr = ("0000000000" + buyerorSellerCode).substring(buyerorSellerCode.length());
							buyerorSellerCode = formattedStr;
						}catch (NumberFormatException e) {
//							formattedStr = customerNo;
						}
					}else{
						//Mandatory check fails
//						mandatoryCheckPass = false;
//						mandatoryCheckMsg = mandatoryCheckMsg+"BuyerorSellerCode";
						buyerorSellerCode="";
					}*/
					
					if(mandatoryCheckPass){
//						Context ctxDestFact = new InitialContext();
//						ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
//						DestinationConfiguration cpiDestConfig = configuration.getConfiguration(destName);
						DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
								.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
						Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
								.tryGetDestination(destName, options);
						Destination cpiDestConfig = destinationAccessor.get();
						wsURL = cpiDestConfig.get("URL").get().toString();
						
						if (debug)
							response.getWriter().println("wsURL: " + wsURL);
						
						if(wsURL != null && wsURL.trim().length() > 0){
							String userName="", passWord="", userpass = "", aggregatorId="", cpiEndPoint="", cpiDestUrl="", cpiResponse="";
							
							userName = cpiDestConfig.get("User").get().toString();
							passWord = cpiDestConfig.get("Password").get().toString();
							userpass = userName+":"+passWord;
							aggregatorId = cpiDestConfig.get("AggregatorID").get().toString();
							cpiEndPoint = properties.getProperty("eSignContractCALCompanies");
							
							cpiDestUrl = wsURL+cpiEndPoint;
							
							if(debug){
								response.getWriter().println("aggregatorId: "+aggregatorId);
								response.getWriter().println("cpiDestUrl: "+cpiDestUrl);
							}
//							JsonObject finalPayload = new JsonObject();
							inputPayload.addProperty("AggregatorId", aggregatorId);
//							finalPayload.addProperty("BuyerorSellerCode", buyerorSellerCode);
							
							try{
								URL url = new URL(cpiDestUrl);
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
								
								StringBuffer sb = new StringBuffer();
								BufferedReader br = new BufferedReader(
										new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
								String line = null;
								while ((line = br.readLine()) != null) {
									sb.append(line + "\n");
								}
								br.close();
								
								if (debug)
									response.getWriter().println("sb: " + sb.toString());
								
								cpiResponse = sb.toString();
								
								JsonParser cpiResponseParser = new JsonParser();
								JsonObject responseJson = (JsonObject)cpiResponseParser.parse(cpiResponse); 
								
								responseJson.addProperty("Remarks", "");
								if (debug)
									response.getWriter().println("responseJson: "+responseJson);
								response.getWriter().println(responseJson);
							}catch (Exception e) {
								StackTraceElement element[] = e.getStackTrace();
								StringBuffer buffer = new StringBuffer();
								for(int i=0;i<element.length;i++)
								{
									buffer.append(element[i]);
								}
								if(debug)
									response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
								
								JsonObject result = new JsonObject();
								errorCode = "E173";
								errorMsg = properties.getProperty(errorCode);
								
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg+". "+e.getClass()+":"+e.getMessage());
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							}
						}
					}
				}catch (Exception e) {
					StackTraceElement element[] = e.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for(int i=0;i<element.length;i++)
					{
						buffer.append(element[i]);
					}
					if(debug)
						response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
					
					JsonObject result = new JsonObject();
					errorCode = "E173";
					errorMsg = properties.getProperty(errorCode);
					
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg+". "+e.getClass()+":"+e.getMessage());
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
			}else{
				//Blank request
				if(debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "No input is received in the request");
				result.addProperty("Remarks", "No input is received in the request");
				response.getWriter().println(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			
			JsonObject result = new JsonObject();
			errorCode = "E173";
			errorMsg = properties.getProperty(errorCode);
			
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg+". "+e.getClass()+":"+e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

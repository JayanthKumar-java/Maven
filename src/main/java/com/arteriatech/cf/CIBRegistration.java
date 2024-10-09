package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
/**
 * Servlet implementation class CIBRegistration
 */
@WebServlet("/CIBRegistration")
public class CIBRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CIBRegistration() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("Invalid Request Method for this type");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		doGet(request, response);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.getWriter().println("Invalid Request Method for this type");
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		boolean debug = false, isRequestFromCloud = false;
		String destURL="", corpID="",userID="", userRegId="", aggregatorID="", loginID="";
		String oDataURL="", userName="", password="", userPass="";
		
		ODataLogs oDataLogs = new ODataLogs();
		CommonUtils commonUtils = new CommonUtils();
//		DestinationConfiguration cpiDestConfig = null;
//		DestinationConfiguration destConfiguration = null;
		Destination cpiDestConfig = null;
		Destination destConfiguration = null;
		
		String appLogID="";
		try{
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
				debug=true;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			if(debug)
				response.getWriter().println("loginID: "+loginID);
			
			cpiDestConfig = commonUtils.getCPIDestination(request, response);
			destConfiguration = commonUtils.getDestinationURL(request, response);
			destURL = destConfiguration.get("URL").get().toString();
			if(debug)
				response.getWriter().println("doGet.destURL.: "+ destURL);
		
			if (destURL.contains("service.xsodata")){
				isRequestFromCloud = true;
			}else{
				isRequestFromCloud = false;
			}
			
			String userRegService = "", sessionID="";
			JsonObject finalPayload = new JsonObject();
			JsonObject userRegistrationObj = new JsonObject();
			
			boolean userRegEmpty=false, userRegError=false;
			if(isRequestFromCloud){
				String aggregatorIDFromDest="";
				aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID",DestinationUtils.PYGWHANA);
				//JsonObject = Call desturl;+"UserCustomers"
				String regisFor ="";
				JsonObject userRegistrationChildObj = new JsonObject();
				
				//JsonObject = Call desturl;+"UserRegistrations"
				userRegService ="";
				userRegService = destURL+"UserRegistrations?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27";
				if (debug)
					response.getWriter().println("userRegistration.executeURL: "+userRegService);
				
				userRegistrationObj = getUserRegistrationFromCloud(response, request, userRegService, debug);
				if (debug) {
					response.getWriter().println("userRegistrationObj: "+userRegistrationObj);
				}
				
				if(userRegistrationObj==null||userRegistrationObj.has("ErrorCode")||userRegistrationObj.has("error")){
					userRegError = true;
					finalPayload = null;
					
					oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
					userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
					userPass = userName+":"+password;
					appLogID = oDataLogs.insertExceptionLogs(request, response, userRegistrationObj.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorIDFromDest, loginID, debug);
				}else if(userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results").size() == 0){
					userRegEmpty = true;
					finalPayload = null;
				}else if(! userRegistrationObj.isJsonNull()){
					for (int i = 0; i < userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
					{
						userRegistrationChildObj = userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
						if( ! userRegistrationChildObj.get("RegistrationFor").isJsonNull() )
							regisFor = userRegistrationChildObj.get("RegistrationFor").getAsString();
						else
							regisFor = "";
						
						if (regisFor.trim().length() == 0 || regisFor.equalsIgnoreCase(""))
						{
							if( ! userRegistrationChildObj.get("CorpId").isJsonNull() )
								corpID =  userRegistrationChildObj.get("CorpId").getAsString();
							
							if( ! userRegistrationChildObj.get("UserId").isJsonNull() )
								userID =  userRegistrationChildObj.get("UserId").getAsString();
							
							if( ! userRegistrationChildObj.get("UserRegId").isJsonNull() )
								userRegId =  userRegistrationChildObj.get("UserRegId").getAsString();
							
							if( ! userRegistrationChildObj.get("AggregatorID").isJsonNull() )
								aggregatorID =  userRegistrationChildObj.get("AggregatorID").getAsString();
							
							break;
						}
					}
					if (debug) {
						response.getWriter().println("corpID: "+corpID);
						response.getWriter().println("userID: "+userID);
						response.getWriter().println("userRegId: "+userRegId);
						response.getWriter().println("aggregatorID: "+aggregatorID);
					}
					
					//Final payload
					finalPayload.addProperty("CorporateID", corpID);
					finalPayload.addProperty("UserID", userID);
					finalPayload.addProperty("UserRegistrationID", userRegId);
					finalPayload.addProperty("AggregatorID", aggregatorID);
				}
			}else{
				String userRegFilter ="", loginMethod="";
				
				String sapclient = destConfiguration.get("sap-client").get().toString();
				if (debug)
					response.getWriter().println("sapclient:" + sapclient);
				
				String authMethod = destConfiguration.get("Authentication").get().toString();
				if (debug)
					response.getWriter().println("authMethod:" + authMethod);
				
				//JsonObject = Call desturl;+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations
				sessionID ="";
				if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
					String url = commonUtils.getDestinationURL(request, response, "URL");
					if (debug)
						response.getWriter().println("url2:" + url);
					sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				} else{
					loginMethod = destConfiguration.get("LoginMethod").get().toString();
					if(null != loginMethod && loginMethod.equalsIgnoreCase("Hybrid")){
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug){
							response.getWriter().println("url:" + url);
							response.getWriter().println("loginMethod:" + loginMethod);
						}
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					}else{
						sessionID ="";
					}
				}
				if (debug)
					response.getWriter().println("sessionID2:" + sessionID);
				
				userRegService=""; userRegFilter="";
				userRegFilter = "LoginId eq '"+sessionID+"'";
				userRegFilter = URLEncoder.encode(userRegFilter, "UTF-8");
				
				userRegFilter = userRegFilter.replaceAll("%26", "&");
				userRegFilter = userRegFilter.replaceAll("%3D", "=");
				if (debug)
					response.getWriter().println("userRegFilter: "+userRegFilter);
				
				
				if(sapclient != null){
					userRegService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations?sap-client="+ sapclient +"&$filter="+ userRegFilter;
				}
				else{
					userRegService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations?$filter="+ userRegFilter;
				}
				if (debug)
					response.getWriter().println("userRegService 2: "+userRegService);
				
				userRegistrationObj = getUserRegistrationDetails(request, response, userRegService, destConfiguration, debug);
				
				if (debug)
					response.getWriter().println("onprem.userRegistrationObj: "+userRegistrationObj);
				
				if(userRegistrationObj==null||userRegistrationObj.isJsonNull()){
					finalPayload = null;
					userRegEmpty = true;
				}else if(userRegistrationObj.has("ErrorCode")||userRegistrationObj.has("error")){
					userRegError = true;
					finalPayload = null;
					
					oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
					
					userPass = userName+":"+password;
					
					appLogID = oDataLogs.insertExceptionLogs(request, response, userRegistrationObj.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
				}else if(! userRegistrationObj.isJsonNull()){
					if (debug)
						response.getWriter().println("userRegistrationObj1: "+userRegistrationObj);
					
					finalPayload.addProperty("CorporateID", userRegistrationObj.get("CorpId").getAsString());
					finalPayload.addProperty("UserID", userRegistrationObj.get("UserId").getAsString());
					finalPayload.addProperty("UserRegistrationID", userRegistrationObj.get("UserRegId").getAsString());
					finalPayload.addProperty("AggregatorID", userRegistrationObj.get("AggregatorID").getAsString());
				}
//				if (debug)
//					response.getWriter().println(finalPayload);
			}
			
			if (debug)
				response.getWriter().println("finalPayload: "+finalPayload);
			
			if(userRegistrationObj.has("ErrorCode")){
				response.getWriter().println(userRegistrationObj);
			}
			else if(userRegEmpty ||userRegError){
			//Error response technical error response
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "Dear Customer, Currently we are facing technical issues in processing your request for account linking. Kindly try again later");
				result.addProperty("UserRegStatus", "000007");
				result.addProperty("Remarks", "Failed due to technical issues (Unable to fetch User Registration Details)");
				response.getWriter().println(new Gson().toJson(result));
			}else{
				if(! finalPayload.isJsonNull()){
//					JsonObject messageObj = new JsonObject();
					byte[] postDataBytes = finalPayload.toString().getBytes("UTF-8");
					
					String cpiDestUrl="", cpiUser="", cpiPass="", cpiUserPass="", cpiResponse="";
					cpiDestUrl = cpiDestConfig.get("URL").get().toString();
					cpiUser = cpiDestConfig.get("User").get().toString();
					cpiPass = cpiDestConfig.get("Password").get().toString();
					cpiUserPass = cpiUser+":"+cpiPass;
					cpiDestUrl = cpiDestUrl+properties.getProperty("UserRegistrationStatus");
					
					if(debug){
						response.getWriter().println("cpiUserPass: "+cpiUserPass);
						response.getWriter().println("cpiDestUrl: "+cpiDestUrl);
					}
					
					URL url = new URL(cpiDestUrl);
					HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
					
					urlConnection.setRequestMethod("PUT");
					urlConnection.setRequestProperty("Content-Type", "application/json");
					urlConnection.setRequestProperty("charset", "utf-8");
					urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
					urlConnection.setRequestProperty("Accept", "application/json");
					urlConnection.setDoOutput(true);
					urlConnection.setDoInput(true);
					
					String basicAuth = "Basic " + Base64.getEncoder().encodeToString(cpiUserPass.getBytes());
					urlConnection.setRequestProperty("Authorization", basicAuth);
					urlConnection.connect();
					
					OutputStream os = urlConnection.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
					osw.write(finalPayload.toString());
					osw.flush();
					osw.close();
					int responseCode = urlConnection.getResponseCode();
					if(debug){
						response.getWriter().println("responseCode:"+responseCode);
					}
					if (responseCode == 200 || responseCode == 204) {
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
						JsonParser parser = new JsonParser();
						JsonObject responseJson = (JsonObject) parser.parse(cpiResponse);
						if (debug)
							response.getWriter().println("responseJson: " + responseJson);
						if (responseJson.get("Status").getAsString().equalsIgnoreCase("000002")) {
							JsonObject result = new JsonObject();

							if (responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/04")
									|| responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/05")) {
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message",
										"Dear Customer, Currently we are facing technical issues in processing your request for account linking. Kindly try again later");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", "Failed due to technical issues ("
										+ responseJson.get("Message").getAsString() + ")");
							} else if (responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/06")) {
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message",
										"Dear Customer, Currently we are facing technical issues in processing your request for account linking. Kindly try again later");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", "Failed due to technical issues ("
										+ responseJson.get("Message").getAsString() + ")");
							}
							response.getWriter().println(new Gson().toJson(result));
						} else {
							JsonObject result = new JsonObject();

							if (responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/02")) {
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message",
										"Dear Customer, User Registration request has been rejected. Kindly contact your RM to know more");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", responseJson.get("Message").getAsString());
							} else if (responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/01")) {
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message",
										"Dear Customer, User Registration is pending for approval. Please approve the registration request and retry");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", responseJson.get("Message").getAsString());
							} else if (responseJson.get("ErrorCode").getAsString().equalsIgnoreCase("ART/CIB_REG/03")) {
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message",
										"Dear Customer, You are currently de-registered. Kindly contact your RM to know more");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", responseJson.get("Message").getAsString());
							} else {
								result.addProperty("Status", responseJson.get("Status").getAsString());
								result.addProperty("ErrorCode", responseJson.get("ErrorCode").getAsString());
								result.addProperty("Message", "");
								result.addProperty("UserRegStatus", responseJson.get("UserRegStatus").getAsString());
								result.addProperty("Remarks", "");
							}
							response.getWriter().println(new Gson().toJson(result));
						}
					}else{
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
						responseJson.addProperty("Status", "000002");
						responseJson.addProperty("ErrorCode", responseCode);
						responseJson.addProperty("UserRegStatus", "000007");
						responseJson.addProperty("Remarks", sb.toString());
						responseJson.addProperty("Message", "Unable to get the response from the  CPI Service "+cpiDestUrl);
						if (debug)
							response.getWriter().println("responseJson: " + responseJson);
						response.getWriter().println(responseJson);
						
					}
				}else{
					JsonObject result = new JsonObject();
					//Json is null-insert application logs and technical error response
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "/ARTEC/J002");
					result.addProperty("Message", "Dear Customer, Currently we are facing technical issues in processing your request for account linking. Kindly try again later");
					result.addProperty("UserRegStatus", "000007");
					result.addProperty("Remarks", "Failed due to technical issues (Error in fetching User Registration Details)");
					response.getWriter().println(new Gson().toJson(result));
				}
				
				//Temp output
//				JsonObject result = new JsonObject();
//				result.addProperty("Status", "000002");
//				result.addProperty("ErrorCode", "001");
//				result.addProperty("Message", "User Registration is pending for approval from the user.");
//				result.addProperty("UserRegStatus", "000001");
//				response.getWriter().println(new Gson().toJson(result));
			}
		}catch (Exception e) {
			String logID="";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
				debug=true;
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "UserRegistrationStatus", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "/ARTEC/J003");
			result.addProperty("Message", "Dear Customer, Currently we are facing technical issues in processing your request for account linking. Kindly try again later");
			result.addProperty("UserRegStatus", "000007");
			result.addProperty("Remarks", "Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+"')");
			
			response.getWriter().println(new Gson().toJson(result));
		}
	
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
//							  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}
	
	private JsonObject getUserRegistrationFromCloud(HttpServletResponse response, HttpServletRequest request, String executeURL, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject userRegistrationResponse = new JsonObject();
		try {
			
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getUserRegistrationFromCloud.userName: "+userName);
				response.getWriter().println("getUserRegistrationFromCloud.password: "+password);
				response.getWriter().println("getUserRegistrationFromCloud.userPass: "+userPass);
			}
			//userRegistrationResponse = commonUtils.executeURL(executeURL, userPass, response);
			userRegistrationResponse=commonUtils.executeAccountLinkingURL(executeURL, userPass, response, debug);
			if (debug)
				response.getWriter().println("getUserRegistrationFromCloud.userRegisResponse: "+userRegistrationResponse);
			
		} catch (Exception e) {
			userRegistrationResponse = null;
			/*StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));*/
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
		}
		return userRegistrationResponse;
	}
	
	private JsonObject getUserRegistrationDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String userName="", password="", authParam="", authMethod="", basicAuth=""; 
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userRegsitrationJson = new JsonObject();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			
//			Context tenCtx = new InitialContext();
//			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
//			if(debug){
//				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
			    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    }
			
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        userCustomersGet = new HttpGet(customerService);
//	        userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        userCustomersGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("getUserRegistrationDetails.statusCode: "+statusCode);
			if (statusCode == 200) {
				customerEntity = httpResponse.getEntity();
				if (customerEntity != null) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;

					String retSrc = EntityUtils.toString(customerEntity);

					if (debug)
						response.getWriter().println("retSrc: " + retSrc);
					// if(null != request.getParameter("debug") &&
					// request.getParameter("debug").equalsIgnoreCase("true"))
					// response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					NodeList corpList = document.getElementsByTagName("d:CorpId");
					NodeList userList = document.getElementsByTagName("d:UserId");
					NodeList userRegList = document.getElementsByTagName("d:UserRegId");
					NodeList aggrIDList = document.getElementsByTagName("d:AggrID");
					// for(int i=0 ; i < corpList.getLength() ; i++)
					// {
					userRegsitrationJson.addProperty("CorpId", corpList.item(0).getTextContent());
					userRegsitrationJson.addProperty("UserId", userList.item(0).getTextContent());
					userRegsitrationJson.addProperty("UserRegId", userRegList.item(0).getTextContent());
					userRegsitrationJson.addProperty("AggregatorID", aggrIDList.item(0).getTextContent());
					// break;
					// }
					if (debug) {
						if (debug)
							response.getWriter().println("retSrc: " + retSrc);
					}

				}
			}else{
				customerEntity = httpResponse.getEntity();
				String retSrc = EntityUtils.toString(customerEntity);
				if (debug) {
					response.getWriter().println("Http Response:" + retSrc);
				}
				String statusLine=httpResponse.getStatusLine().toString();
				StringBuffer buffer=new StringBuffer();
				if(statusLine!=null){
					String[] split = statusLine.split(" ");
					for(int i=0;i<split.length-1;i++){
						buffer.append(split[i]).append(" ");
					}
					}
					if(buffer==null || buffer.length()<0){
						buffer.append(statusCode);
					}
					String[] split = customerService.split("\\?");
					customerService=split[0];
				if (statusCode == 401) {
					userRegsitrationJson.addProperty("Message",
							"Unauthorized Access with Http Status Code:" + buffer+" for the Service"+customerService);
					userRegsitrationJson.addProperty("ErrorCode", statusCode);
					userRegsitrationJson.addProperty("Status", "000002");
					userRegsitrationJson.addProperty("Remarks", "");
				} else if (statusCode == 404) {
					userRegsitrationJson.addProperty("Message",
							"Resource not found with Http Status Code:" +buffer+" for the Service"+customerService);
					userRegsitrationJson.addProperty("ErrorCode", statusCode);
					userRegsitrationJson.addProperty("Status", "000002");
					userRegsitrationJson.addProperty("Remarks", "");
				} else {
					userRegsitrationJson.addProperty("Message",
							"Unable to fetch Records with Http Status Code:" + buffer+" for the Service"+customerService);
					userRegsitrationJson.addProperty("ErrorCode", statusCode);
					userRegsitrationJson.addProperty("Status", "000002");
					userRegsitrationJson.addProperty("Remarks", "");
				}
				
			}
		}catch (RuntimeException e) {
			userRegsitrationJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
		}catch (ParserConfigurationException e) {
			userRegsitrationJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			userPass = userName+":"+password;
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
		} catch (SAXException e) {
			userRegsitrationJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
			
		} catch (Exception e) {
			userRegsitrationJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
		}/* catch (Exception e) {
			userRegsitrationJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "UserRegistrationStatus", oDataURL, userPass, aggregatorID, loginID, debug);
		} */
		finally
		{
			// closableHttpClient.close();
		}
		return userRegsitrationJson;
	}

}

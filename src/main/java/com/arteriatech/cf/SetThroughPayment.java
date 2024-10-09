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
import java.util.HashMap;
import java.util.Map;
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

import org.apache.axis.encoding.ser.ElementSerializerFactory;
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

import com.arteriatech.bc.Account.AccountClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;

/**
 * Servlet implementation class SetThroughPayment
 */
@WebServlet("/SetThroughPayment")
public class SetThroughPayment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String CPI_CONNECT_DEST_NAME =  "CPIConnect";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetThroughPayment() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String accountNumber ="", event = "",payloadRequest="", destURL ="", ODataURL="", password="", userName="", userPass="", sessionID="",customerService="";
		boolean debug = false, isRequestFromCloud = false;
		String dealerID="", dealerName="", corpID="",userID="", userRegId="", aggregatorID="";
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		
		JsonObject finalPayload = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
//		DestinationConfiguration destConfiguration = null;
//		DestinationConfiguration cpiDestConfig = null;
		Destination destConfiguration = null;
		Destination cpiDestConfig = null;
		AccountClient accountClient = new AccountClient();
		try {
			
			JsonObject userCustomerObj = new JsonObject();
			JsonObject userRegistrationObj = new JsonObject();
			payloadRequest = getGetBody(request, response);
			
			JSONObject inputPayload = new JSONObject(payloadRequest);
//			response.getWriter().println("inputPayload: "+inputPayload);
			try {
				if (inputPayload.has("debug") && ! inputPayload.isNull("debug") && 
						inputPayload.getBoolean("debug") == true ) {
					debug = true;
					response.getWriter().println("doGet.STP.: "+inputPayload); 
				}
			} catch (Exception e) {
				debug = false;
			}
			
//			inputPayload.getJSONObject("Message").isNull(key)
			if ( ! inputPayload.getJSONObject("Message").isNull("AccountNo")){
				accountNumber = inputPayload.getJSONObject("Message").getString("AccountNo");
				finalPayload.addProperty("ODAccountNumber", accountNumber);
			}else{
				//Error Response - Account No not found
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "002");
				result.addProperty("Message", "'AccountNo' "+properties.getProperty("002"));
				response.getWriter().println(new Gson().toJson(result));
			}
			if(debug)
				response.getWriter().println("doGet.accountNumber.: "+ accountNumber);
			
			if ( ! inputPayload.isNull("Event")){
				event = inputPayload.getString("Event");
				finalPayload.addProperty("Event", inputPayload.getString("Event").toUpperCase());
			}else{
				//Error Response - Event not found
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "002");
				result.addProperty("Message", "'Event' "+properties.getProperty("002"));
				response.getWriter().println(new Gson().toJson(result));
			}
			if(debug)
				response.getWriter().println("doGet.event.: "+ event);
			
			//Destination
			cpiDestConfig = getCPIDestination(request, response);
			destConfiguration = getDestinationURL(request, response);
			destURL = destConfiguration.get("URL").get().toString();
			if(debug)
				response.getWriter().println("doGet.destURL.: "+ destURL);
			
			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						String.format("Destination %s is not found. Hint:"
								+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
			}
			else
			{
				if (destURL.contains("service.xsodata"))
				{
					isRequestFromCloud = true;
				} 
				else {
					isRequestFromCloud = false;
				}
			}
			String loginID = commonUtils.getUserPrincipal(request, "name", response);
			if (debug)
				response.getWriter().println("loginID: "+loginID);
				
			if(isRequestFromCloud)
			{
				//JsonObject = Call desturl;+"UserCustomers"
				String regisFor ="";
				JsonObject userRegistrationChildObj = new JsonObject();
				
				customerService = destURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27";
				if (debug)
					response.getWriter().println("userCustomerObj.executeURL: "+customerService);
				
				userCustomerObj = getCustomerFromCloud(request, response, customerService, debug);
				if (debug) {
					response.getWriter().println("userCustomerObj: "+userCustomerObj);
				}
				if (! userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("PartnerID").isJsonNull())
					dealerID = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("PartnerID").getAsString();
				
				if (! userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("Name").isJsonNull())
					dealerName = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("Name").getAsString();
				
				if (debug) {
					response.getWriter().println("dealerID: "+dealerID);
					response.getWriter().println("dealerName: "+dealerName);
				}

				finalPayload.addProperty("DealerCode", dealerID);
				finalPayload.addProperty("DealerName", dealerName);
				
				
				//JsonObject = Call desturl;+"UserRegistrations"
				customerService ="";
				customerService = destURL+"UserRegistrations?$filter=LoginId%20eq%20%27"+loginID+"%27";
				if (debug)
					response.getWriter().println("userRegistration.executeURL: "+customerService);
				
				userRegistrationObj = getUserRegistrationFromCloud(response, request, customerService, debug);
				if (debug) {
					response.getWriter().println("userRegistrationObj: "+userRegistrationObj);
				}
				
				for (int i = 0; i < userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
				{
					userRegistrationChildObj = userRegistrationObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
					if( ! userRegistrationChildObj.get("RegistrationFor").isJsonNull() )
						regisFor = userRegistrationChildObj.get("RegistrationFor").getAsString();
					
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
				finalPayload.addProperty("CorpID", corpID);
				finalPayload.addProperty("UserID", userID);
				finalPayload.addProperty("URN", userRegId);
				finalPayload.addProperty("AggregatorID", aggregatorID);
//				if (debug)
//					response.getWriter().println("finalPayload: "+finalPayload);
				
//				response.getWriter().println(finalPayload);
			}
			else
			{
				//JsonObject = Call desturl;+"/sap/opu/odata/ARTEC/PYGW/UserCustomers
				String customerFilter ="", queryString="";
				
				String sapclient = destConfiguration.get("sap-client").get().toString();
				if (debug)
					response.getWriter().println("sapclient:" + sapclient);
				
				String authMethod = destConfiguration.get("Authentication").get().toString();
				if (debug)
					response.getWriter().println("authMethod:" + authMethod);
				
				if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
				 	String url = commonUtils.getDestinationURL(request, response, "URL");
					if (debug)
						response.getWriter().println("url:" + url);
					sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
				} else{
					sessionID ="";
				}
				if (debug)
					response.getWriter().println("sessionID:" + sessionID);
				
				customerFilter = "LoginID eq '"+sessionID+"'";
				customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
				
				customerFilter = customerFilter.replaceAll("%26", "&");
				customerFilter = customerFilter.replaceAll("%3D", "=");
				if (debug)
					response.getWriter().println("customerFilter: "+customerFilter);
				
				
				if(sapclient != null){
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
				}
				else{
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ customerFilter;
				}
				if (debug)
					response.getWriter().println("CustomerService: "+customerService);
				
				userCustomerObj = getUserCustomerDetails(request, response, customerService, destConfiguration, debug);
				if (debug)
					response.getWriter().println("userCustomerObj1: "+userCustomerObj);
				
				finalPayload.addProperty("DealerCode", userCustomerObj.get("DealerId").getAsString());
				finalPayload.addProperty("DealerName", userCustomerObj.get("DealerName").getAsString());
				
				//JsonObject = Call desturl;+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations
				
				customerService=""; customerFilter="";
				customerFilter = "LoginId eq '"+sessionID+"'";
				customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
				
				customerFilter = customerFilter.replaceAll("%26", "&");
				customerFilter = customerFilter.replaceAll("%3D", "=");
				if (debug)
					response.getWriter().println("customerFilter: "+customerFilter);
				
				
				if(sapclient != null){
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations?sap-client="+ sapclient +"&$filter="+ customerFilter;
				}
				else{
					customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserRegistrations?$filter="+ customerFilter;
				}
				if (debug)
					response.getWriter().println("CustomerService: "+customerService);
				
				userRegistrationObj = getUserRegistrationDetails(request, response, customerService, destConfiguration, debug);
				if (debug)
					response.getWriter().println("userRegistrationObj1: "+userRegistrationObj);
				
				finalPayload.addProperty("CorpID", userRegistrationObj.get("CorpId").getAsString());
				finalPayload.addProperty("UserID", userRegistrationObj.get("UserId").getAsString());
				finalPayload.addProperty("URN", userRegistrationObj.get("UserRegId").getAsString());
				finalPayload.addProperty("AggregatorID", userRegistrationObj.get("AggregatorID").getAsString());
				
//				if (debug)
//					response.getWriter().println(finalPayload);
			}
			if (debug)
				response.getWriter().println("finalPayload: "+finalPayload);
			
			//Check If Account No is isCFSOD or not
			Map<String, String> userAccountsEntry = new HashMap<String, String>();
			Map<String, String> userRegMap = new HashMap<String, String>();
			Map<String, String> accountsWSResponseMap = new HashMap<String, String>();
			JsonObject onODAccountObj = new JsonObject();
			String aggrID = "", isCFSOFFromWS="", accountTypeFromWS="";
			aggrID = finalPayload.get("AggregatorID").getAsString();
			
			userAccountsEntry.put("BankAccntNo", finalPayload.get("ODAccountNumber").getAsString());
			
			userRegMap.put("CorpId", finalPayload.get("CorpID").getAsString());
			userRegMap.put("UserId", finalPayload.get("UserID").getAsString());
			userRegMap.put("UserRegId", finalPayload.get("URN").getAsString());
			
			accountsWSResponseMap = accountClient.callAccountsWebservice(request, response, userAccountsEntry, userRegMap, aggrID, debug);
			for (String key : accountsWSResponseMap.keySet()) {
				if(debug)
					response.getWriter().println("callAccountsWebservice-accountsWSResponseMap: "+key + " - " + accountsWSResponseMap.get(key));
			}
			if(accountsWSResponseMap.get("Error").equalsIgnoreCase("059")){
				//Error Response - Error during webservice call
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "059");
				result.addProperty("Message", "Unable to serve your request. Error From Webservice");
				response.getWriter().println(new Gson().toJson(result));
			}else{
				//Successful webservice call
				isCFSOFFromWS = accountsWSResponseMap.get("IsCFSOD");
				accountTypeFromWS = accountsWSResponseMap.get("AccountType");
				
				if(accountTypeFromWS != null && accountTypeFromWS.trim().length() > 0){
					if(accountTypeFromWS.equalsIgnoreCase("ODA") && isCFSOFFromWS.equalsIgnoreCase("Y")){
						//Form Payload for CPI
						JsonObject eventObj = new JsonObject();
						JsonObject messageObj = new JsonObject();
						
						messageObj.addProperty("CorpID", finalPayload.get("CorpID").getAsString());
						messageObj.addProperty("AggregatorID", finalPayload.get("AggregatorID").getAsString());
						messageObj.addProperty("UserID", finalPayload.get("UserID").getAsString());
						messageObj.addProperty("URN", finalPayload.get("URN").getAsString());
						messageObj.addProperty("DealerName", finalPayload.get("DealerName").getAsString());
						messageObj.addProperty("DealerCode", finalPayload.get("DealerCode").getAsString());
						messageObj.addProperty("ODAccountNumber", finalPayload.get("ODAccountNumber").getAsString());
						
						eventObj.addProperty("Event", "LINK");
						eventObj.add("Message", messageObj);
						
						onODAccountObj.add("OnODAccount", eventObj);

						if(debug)
							response.getWriter().println("Final onODAccountObj: "+onODAccountObj);
						byte[] postDataBytes = onODAccountObj.toString().getBytes("UTF-8");
						
						String cpiDestUrl="", cpiUser="", cpiPass="", cpiUserPass="", cpiResponse="";
						cpiDestUrl = cpiDestConfig.get("URL").get().toString();
						cpiUser = cpiDestConfig.get("User").get().toString();
						cpiPass = cpiDestConfig.get("Password").get().toString();
						cpiUserPass = cpiUser+":"+cpiPass;
						cpiDestUrl = cpiDestUrl+"OnODAccountEventPublish";
						URL url = new URL(cpiDestUrl);
						HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

						con.setRequestMethod("POST");
						con.setRequestProperty("Content-Type", "application/json");
						con.setRequestProperty("charset", "utf-8");
						con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
						con.setRequestProperty("Accept", "application/json");
						con.setDoOutput(true);
						con.setDoInput(true);
						
						String basicAuth = "Basic " + Base64.getEncoder().encodeToString(cpiUserPass.getBytes());
						con.setRequestProperty("Authorization", basicAuth);
						con.connect();

						OutputStream os = con.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
						osw.write(onODAccountObj.toString());
						osw.flush();
						osw.close();
						
						StringBuffer sb = new StringBuffer();
						BufferedReader br = new BufferedReader(
								new InputStreamReader(con.getInputStream(), "utf-8"));
						String line = null;
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
						br.close();

						if (debug)
							response.getWriter().println("sb: " + sb.toString());

						cpiResponse = sb.toString();
						
						response.getWriter().println("cpiResponse: "+cpiResponse);
						JSONObject responseJsonObj = new JSONObject(cpiResponse);
						response.getWriter().println(responseJsonObj);
						
//						Gson inputJsonObject = new Gson();
//						inputJsonObject.toJson(cpiResponse);
						
						//Success Response to UI after CPI is hit successfully
						/*JsonObject result = new JsonObject();
						result.addProperty("Status", "000001");
						result.addProperty("ErrorCode", "");
						result.addProperty("Message", "Success");
						response.getWriter().println(new Gson().toJson(result));*/
					}else{
						//Success Response - Account Type may/maynot be ODA, but it is not a CFSOD
						JsonObject result = new JsonObject();
						result.addProperty("Status", "000001");
						result.addProperty("ErrorCode", "");
						result.addProperty("Message", "Success");
						response.getWriter().println(new Gson().toJson(result));
					}
				}else{
					//Success Response - Account Type is blank from WS
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000001");
					result.addProperty("ErrorCode", "");
					result.addProperty("Message", "Success");
					response.getWriter().println(new Gson().toJson(result));
				}
			}
			if(debug)
				response.getWriter().println("onODAccountObj: "+onODAccountObj);
		}
		catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "E001");
			result.addProperty("Message", buffer.toString());
			response.getWriter().println(new Gson().toJson(result));
		}
	}
	//added
	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//		LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = PCGW_UTIL_DEST_NAME;
//		DestinationConfiguration destConfiguration = null;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
//			Context ctxConnConfgn = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
//			destConfiguration = configuration.getConfiguration(destinationName);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
//			LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
//			LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}
	
	public Destination getCPIDestination(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//		LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = CPI_CONNECT_DEST_NAME;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
//			Context ctxConnConfgn = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
//			destConfiguration = configuration.getConfiguration(destinationName);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
//			LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
//			LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}
	
//added
	private JsonObject getUserRegistrationDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String customerNo = "";
		String destURL="", errorCode="", userName="", password="", authParam="", authMethod="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userCustomerJson = new JsonObject();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
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
	        // userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
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
	        	response.getWriter().println("validateCustomer.statusCode: "+statusCode);
			
			customerEntity = httpResponse.getEntity();
			
			if(customerEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		       
				String retSrc = EntityUtils.toString(customerEntity);
				
				if (debug)
					response.getWriter().println("retSrc: "+retSrc);
//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
//					response.getWriter().println("retSrc: "+retSrc);
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList corpList = document.getElementsByTagName("d:CorpId");
	            NodeList userList = document.getElementsByTagName("d:UserId");
	            NodeList userRegList = document.getElementsByTagName("d:UserRegId");
	            NodeList aggrIDList = document.getElementsByTagName("d:AggrID");
//	            for(int i=0 ; i < corpList.getLength() ; i++)
//	            {
        		userCustomerJson.addProperty("CorpId", corpList.item(0).getTextContent());
        		userCustomerJson.addProperty("UserId", userList.item(0).getTextContent());
        		userCustomerJson.addProperty("UserRegId", userRegList.item(0).getTextContent());
        		userCustomerJson.addProperty("AggregatorID", aggrIDList.item(0).getTextContent());
//        		break;
//	            }
	            if (debug) {
	            	if (debug)
						response.getWriter().println("retSrc: "+retSrc);
				}
			}
		}catch (RuntimeException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("RuntimeException in validateCustomer: "+e.getMessage());
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("ParserConfigurationException in validateCustomer: "+e.getMessage());
		} catch (SAXException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("SAXException in validateCustomer: "+e.getMessage());
		} /* catch (NamingException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("NamingException in validateCustomer: "+e.getMessage());
		} */
		finally
		{
			// closableHttpClient.close();
			return userCustomerJson;
		}
	}	
	
	private JsonObject getCustomerFromCloud(HttpServletRequest request, HttpServletResponse response,String executeURL, boolean debug) throws IOException
	{
		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject userCustomerResponse = new JsonObject();
		try {
			
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getCustomerFromCloud.userName: "+userName);
				response.getWriter().println("getCustomerFromCloud.password: "+password);
				response.getWriter().println("getCustomerFromCloud.userPass: "+userPass);
			}
			userCustomerResponse = commonUtils.executeURL(executeURL, userPass, response);
			if (debug)
				response.getWriter().println("getCustomerFromCloud.userRegisResponse: "+userCustomerResponse);
			
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
		}
		return userCustomerResponse;
		
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
			userRegistrationResponse = commonUtils.executeURL(executeURL, userPass, response);
			if (debug)
				response.getWriter().println("getUserRegistrationFromCloud.userRegisResponse: "+userRegistrationResponse);
			
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
		}
		return userRegistrationResponse;
	}
	
	
	
	private JsonObject getUserCustomerDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String customerNo = "";
		String destURL="", errorCode="", userName="", password="", authParam="", authMethod="", customerFilter="", basicAuth=""; 
		boolean isValidCustomer = false;
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userCustomerJson = new JsonObject();
		
		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			/*
			Context ctx = new InitialContext();
			//ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			
			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
	        }*/
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
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
	        // userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
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
	        	response.getWriter().println("validateCustomer.statusCode: "+statusCode);
			
			customerEntity = httpResponse.getEntity();
			
			if(customerEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		       
				String retSrc = EntityUtils.toString(customerEntity);
				
				if (debug)
					response.getWriter().println("retSrc: "+retSrc);
//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
//					response.getWriter().println("retSrc: "+retSrc);
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList idList = document.getElementsByTagName("d:CustomerNo");
	            NodeList nameList = document.getElementsByTagName("d:Name");
//	            NodeList name1List = document.getElementsByTagName("d:Name");
	           
	            userCustomerJson.addProperty("DealerId", idList.item(0).getTextContent());
	            userCustomerJson.addProperty("DealerName", nameList.item(0).getTextContent());
			}
		}catch (RuntimeException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("RuntimeException in validateCustomer: "+e.getMessage());
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("ParserConfigurationException in validateCustomer: "+e.getMessage());
		} catch (SAXException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("SAXException in validateCustomer: "+e.getMessage());
		} /* catch (NamingException e) {
			errorCode = "E105";
			if(debug)
				response.getWriter().println("NamingException in validateCustomer: "+e.getMessage());
		} */
		finally
		{
			// closableHttpClient.close();
			return userCustomerJson;
		}
	}
	
	
	//added method
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
//						  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

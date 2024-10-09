package com.arteriatech.hana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.Principal;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import com.sap.cloud.sdk.cloudplatform.tenant.Tenant;

/**
 * Servlet implementation class PUGW
 */
@WebServlet("/PUGW")
public class PUGW extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PUGW() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String oDataURL = "", loginID="";
		CommonUtils utils = new CommonUtils();
		/* Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
		Try<Principal> currentPrincipal = PrincipalAccessor.tryGetCurrentPrincipal();
		System.out.println("currentTenant: "+currentTenant);
		System.out.println("currentPrincipal: "+currentPrincipal); */

		oDataURL = utils.getODataDestinationProperties("URL", "PUGWHANA");
		if(oDataURL != null && ! oDataURL.equalsIgnoreCase("E106") && ! oDataURL.contains("E173")){
			loginID = utils.getODataDestinationProperties("User", "PUGWHANA");
			if(loginID != null && ! loginID.equalsIgnoreCase("E106") && ! loginID.contains("E173")){
				executeODataCalls(request, response, oDataURL);
			}else{
				response.getWriter().println("Destination 'PCGWHANA' not maintained in sub account");
			}
		}else{
			response.getWriter().println("Destination 'PCGWHANA' not maintained in sub account");
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false, isValidationFailed = false;
		String payloadRequest = "", dataPayload="", aggregatorID="", loginID="", oDataURL="", url="",errorResponseFormat="", testRun="";
		
		errorResponseFormat= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
				+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
				+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PUGW\",\"service_version\":\"0001\"},"
				+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
				+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
				+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
				+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
				+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
				+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
				+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
		try{
			debug = false;
			JSONObject inputPayload = null;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PUGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PUGWHANA");

			try {
				payloadRequest = getGetBody(request, response);
				inputPayload = new JSONObject(payloadRequest);
				if (! inputPayload.isNull("debug") && inputPayload.getString("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
			} catch (Exception e) {
				debug = false;
			}
			if(debug){
				response.getWriter().println("doPut.payloadRequest: "+payloadRequest);
				response.getWriter().println("doPut.inputPayload: "+inputPayload);
				response.getWriter().println("doPut.aggregatorID: "+aggregatorID);
				response.getWriter().println("doPut.loginID: "+loginID);
				response.getWriter().println("doPut.oDataURL: "+oDataURL);
				response.getWriter().println("doPut.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doPut.getRequestURL: "+request.getRequestURL());
			}
			
			url = request.getRequestURL().toString();

			if (url.contains("UserLogins"))
			{
				loginID ="";
				if(debug)
					response.getWriter().println("doPut.UserLogins: ");
				try {
					if (inputPayload.getString("LoginID").trim().length() == 0)
						isValidationFailed = true;
					else
						loginID = inputPayload.getString("LoginID");
					
					if (inputPayload.getString("Application").trim().length() == 0) 
						isValidationFailed = true;

					if (inputPayload.getString("LoginName").trim().length() == 0)
						isValidationFailed = true;

					if (inputPayload.getString("ERPLoginID").trim().length() == 0)
						isValidationFailed = true;

					if (inputPayload.getString("RoleID").trim().length() == 0)
						isValidationFailed = true;
					
				} catch (Exception e)
				{
					isValidationFailed = true;
				}
				if ( ! isValidationFailed) {
					
					if ( ! inputPayload.isNull("TestRun"))
						testRun = inputPayload.getString("TestRun");
					
					//Based on TestRun It will give response and Update
					if (testRun.equalsIgnoreCase(""))
					{
						commonUtils.updateUserLogin(request, response, properties, testRun ,inputPayload, aggregatorID, loginID, oDataURL, errorResponseFormat, debug);
					}
					else
					{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_CODE", "024");
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_MESSAGE", properties.getProperty("024"));
					response.getWriter().println(errorResponseFormat);
				}
			} 
			else if (url.contains("UserPartners")) {
//				debug = true;
				if(debug){
					response.getWriter().println("doPut.Partner: ");
					response.getWriter().println("doPut.inputPayload: "+inputPayload);
				}
				loginID = "";
				String partnerID ="", applicationID="", eRPSystemID="", partnerName="";
				
				JSONObject childInputJSON = new JSONObject();
				JSONArray jsonPartnerArray = new JSONArray();
				try {
					if (inputPayload.has("UserPartners")) {
						jsonPartnerArray = inputPayload.getJSONArray("UserPartners");
					} else {
						jsonPartnerArray.put(inputPayload);
					}
					
					for (int i = 0; i < jsonPartnerArray.length(); i++) {
						
						childInputJSON = jsonPartnerArray.getJSONObject(i);
						if (! isValidationFailed) {
							
							if (childInputJSON.getString("PartnerID").trim().length() == 0)
								isValidationFailed = true;

							if (childInputJSON.getString("LoginID").trim().length() == 0)
								isValidationFailed = true;
							else
								loginID = childInputJSON.getString("LoginID");
							
							if ( childInputJSON.getString("Application").trim().length() == 0)
								isValidationFailed = true;
							else
								applicationID = childInputJSON.getString("Application");
							
							if (childInputJSON.getString("ERPSystemID").trim().length() == 0)
								isValidationFailed = true;
							
							if (childInputJSON.getString("PartnerName").trim().length() == 0)
								isValidationFailed = true;

						} else {
							break;
						}
					}
				} catch (Exception e) {
					isValidationFailed = true;
				}
				if (debug) {
					response.getWriter().println("UserPartners.LoginID: "+loginID);
					response.getWriter().println("UserPartners.Application: "+applicationID);
					response.getWriter().println("UserPartners.TestRun: "+ inputPayload.getString("TestRun"));
//					response.getWriter().println("UserPartners.PartnerName: "+partnerName);
//					response.getWriter().println("UserPartners.ERPSystemID: "+eRPSystemID);
//					response.getWriter().println("UserPartners.PartnerID: "+partnerID);
					
				}
				if ( ! isValidationFailed) {
					
					if ( ! inputPayload.isNull("TestRun"))
						testRun = inputPayload.getString("TestRun");
					
//					//Based on TestRun It will give response and Update
					if ( testRun.equalsIgnoreCase(""))
					{
						commonUtils.updateUserPartner(request, response, properties, testRun, jsonPartnerArray, aggregatorID, loginID, oDataURL, errorResponseFormat, debug);
					}
					else
					{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_CODE", "058");
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_MESSAGE", properties.getProperty("058"));//+ ""+partnerID);
					response.getWriter().println(errorResponseFormat);
				}
			}
			else if (url.contains("UserAuthSet")) {
//				debug = true;
				loginID ="";
				String authOrgValue="",authOrgTypeID="", eRPSystemID="",applicationID="";
				JSONArray jsonAuthSetArray = new JSONArray();
				try {
					JSONObject childInputJSON = new JSONObject();
					if (inputPayload.has("UserAuthSet")) {
						jsonAuthSetArray = inputPayload.getJSONArray("UserAuthSet");
					} else {
						jsonAuthSetArray.put(inputPayload);
					}
					
					for (int i = 0; i < jsonAuthSetArray.length(); i++) {
						
						childInputJSON = jsonAuthSetArray.getJSONObject(i);
						if (! isValidationFailed) {
							if (childInputJSON.getString("LoginID").trim().length()== 0)
								isValidationFailed = true;
							else
								loginID = childInputJSON.getString("LoginID");
							
							if (childInputJSON.getString("Application").trim().length() == 0) 
								isValidationFailed = true;
							else
								applicationID = childInputJSON.getString("Application");
							
							if (childInputJSON.getString("AuthOrgValue").trim().length() == 0)
								isValidationFailed = true;
							
							if (childInputJSON.getString("AuthOrgTypeID").trim().length() == 0)
								isValidationFailed = true;
							
							if ( childInputJSON.getString("AuthOrgValDsc").trim().length() == 0)
								isValidationFailed = true;

							if (childInputJSON.getString("ERPSystemID").trim().length() == 0)
								isValidationFailed = true;
						} else {
							break;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					isValidationFailed = true;
				}
				if (debug) {
					response.getWriter().println("UserAuthSet.LoginID: "+loginID);
					response.getWriter().println("UserAuthSet.Application: "+applicationID);
//					response.getWriter().println("UserAuthSet.AuthOrgValue: "+authOrgValue);
//					response.getWriter().println("UserAuthSet.AuthOrgTypeID: "+authOrgTypeID);
				}
				
				if ( ! isValidationFailed) {
					
					if ( ! inputPayload.isNull("TestRun"))
						testRun = inputPayload.getString("TestRun");
					//Based on TestRun It will give response and Update
					if (testRun.equalsIgnoreCase(""))
					{
						commonUtils.updateUserAuthorization(request, response, properties, testRun, jsonAuthSetArray, aggregatorID, loginID, applicationID, oDataURL, errorResponseFormat, debug);
					} 
					else
					{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_CODE", "058");
					errorResponseFormat = errorResponseFormat.replaceAll("ERROR_MESSAGE", properties.getProperty("058"));//+ ""+authOrgTypeID);
					response.getWriter().println(errorResponseFormat);
				}
			}
			else
			{
				response.getWriter().println("doPut.outside");
				response.getWriter().println("doPut.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doPut.getRequestURL: "+request.getRequestURL());
				response.getWriter().println("doPut.getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doPut.getContextPath: "+request.getContextPath());
				response.getWriter().println("doPut.getQueryString: "+request.getQueryString());
				response.getWriter().println("doPut.getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doPut.getServletContext: "+request.getServletContext());
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
		}
	}
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String payloadRequest = "", aggregatorID="", loginID="", oDataURL="", url="",errorResFormatResponse="", testRun="";

		errorResFormatResponse= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
				+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
				+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PUGW\",\"service_version\":\"0001\"},"
				+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
				+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
				+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
				+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
				+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
				+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
				+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
		
		try{
			JSONObject inputPayload = null;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			String executeURL= "",userName="",password="",userPass="";
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PUGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PUGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PUGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PUGWHANA");
			userPass = userName+":"+password;
			try {
				payloadRequest = getGetBody(request, response);
				inputPayload = new JSONObject(payloadRequest);
				if (! inputPayload.isNull("debug") && inputPayload.getString("debug").equalsIgnoreCase("true")) {
					debug = true;
				}
			} catch (Exception e) {
				debug = false;
//				debug = true;
			}
			if(debug){
				response.getWriter().println("doDelete.payloadRequest: "+payloadRequest);
				response.getWriter().println("doDelete.inputPayload: "+inputPayload);
				response.getWriter().println("doDelete.aggregatorID: "+aggregatorID);
				response.getWriter().println("doDelete.loginID: "+loginID);
				response.getWriter().println("doDelete.oDataURL: "+oDataURL);
				response.getWriter().println("doDelete.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doDelete.getRequestURL: "+request.getRequestURL());
				
			}
			url = request.getRequestURL().toString();

			if (url.contains("UserLogins"))
			{
				String application ="";
				loginID ="";

				if(debug)
					response.getWriter().println("doDelete.UserLogins: ");
				try {
					url = url.substring(url.indexOf("(")+1, url.length()-1).replaceAll("'", "");
				} catch (Exception e) {
					url ="";
				}
				String[] reqQwery = url.split(",");
				for (String string : reqQwery) {
					if (string.contains("Application"))
						application = string.substring( string.indexOf('=')+1,string.length());

					if (string.contains("LoginID"))
						loginID = string.substring( string.indexOf('=')+1,string.length());
				}
				if(debug){
					response.getWriter().println("doDelete.loginID: "+loginID);
					response.getWriter().println("doDelete.application: "+application);
				}
				commonUtils.deleteUserLogin(request, response, properties, application, inputPayload, aggregatorID, loginID, oDataURL, errorResFormatResponse, debug);
				
			}
			else if (url.contains("UserPartners")) {
				String updateUrl ="";
				JsonObject userPartnerDeleteresponse = new JsonObject();
				String successResponse = "{\"code\":\"/ARTEC/PC/027\",\"message\":\"User Partner mapping entry deleted successfully\",\"severity\":\"info\",\"target\":\"\",\"details\":[]}";
				if(debug)
					response.getWriter().println("doDelete.UserPartners: ");
				
				try {
					updateUrl = url.substring(url.indexOf("(") + 1, url.length()-1);
					updateUrl = "(AggregatorID='"+aggregatorID+"',"+ updateUrl+")"; 
				} catch (Exception e) {
					updateUrl ="";
				}
				
				///UserPartners(AggregatorID='AGGR0008',Application='CFA',LoginID='P099656454',PartnerID='5454',ERPSystemID='Oir94754')
				executeURL = oDataURL +"UserPartners"+updateUrl;
				
				userPartnerDeleteresponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PUGWHANA");
				if(debug){
					response.getWriter().println("executeURL.doDelete: "+userPartnerDeleteresponse);
					response.getWriter().println("UserPartners.doDelete: "+userPartnerDeleteresponse);
				}
				
				if(! userPartnerDeleteresponse.get("ErrorCode").isJsonNull() 
						&& ! userPartnerDeleteresponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
					//Update Failure
					errorResFormatResponse = errorResFormatResponse.replaceAll("ERROR_MESSAGE", properties.getProperty("035"));
					errorResFormatResponse = errorResFormatResponse.replaceAll("ERROR_CODE", "035");
					
					JsonObject userLoginResponseUI = new JsonParser().parse(errorResFormatResponse).getAsJsonObject();
					if(debug)
						response.getWriter().println("doDelete.UserPartners.Failure Response.: "+userLoginResponseUI);
					
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(userLoginResponseUI);
				}
				else
				{
					JsonObject userLoginResponseUI = new JsonParser().parse(successResponse).getAsJsonObject();
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(userLoginResponseUI);
				}
			}
			else if (url.contains("UserAuthSet")) {
				String updateUrl ="";
				JsonObject userAuthSetDeleteresponse = new JsonObject();
				String successResponse = "{\"code\":\"/ARTEC/PC/032\",\"message\":\"User Auth entry deleted successfully\",\"severity\":\"info\",\"target\":\"\",\"details\":[]}";
				if(debug)
					response.getWriter().println("doDelete.UserAuthSet: ");
				
				try {
					updateUrl = url.substring(url.indexOf("(") + 1, url.length()-1);
					updateUrl = "(AggregatorID='"+aggregatorID+"',"+ updateUrl+")"; 
				} catch (Exception e) {
					updateUrl ="";
				}
				
				///UserPartners(AggregatorID='AGGR0008',Application='CFA',LoginID='P099656454',PartnerID='5454',ERPSystemID='Oir94754')
				executeURL = oDataURL +"UserAuthSet"+updateUrl;
				
				userAuthSetDeleteresponse = commonUtils.executeDelete(executeURL, userPass, response, request, debug, "PUGWHANA");
				if(debug){
					response.getWriter().println("executeURL.doDelete: "+userAuthSetDeleteresponse);
					response.getWriter().println("UserAuthSet.doDelete: "+userAuthSetDeleteresponse);
				}
				
				if(! userAuthSetDeleteresponse.get("ErrorCode").isJsonNull() 
						&& ! userAuthSetDeleteresponse.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
					//Update Failure
					errorResFormatResponse = errorResFormatResponse.replaceAll("ERROR_MESSAGE", properties.getProperty("035"));
					errorResFormatResponse = errorResFormatResponse.replaceAll("ERROR_CODE", "035");
					
					JsonObject userLoginResponseUI = new JsonParser().parse(errorResFormatResponse).getAsJsonObject();
					if(debug)
						response.getWriter().println("doDelete.UserAuthSet.Failure Response.: "+userLoginResponseUI);
					
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(userLoginResponseUI);
				}
				else
				{
					JsonObject userLoginResponseUI = new JsonParser().parse(successResponse).getAsJsonObject();
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(userLoginResponseUI);
				}
			}
			else
			{
				response.getWriter().println("doDelete.outside");
				response.getWriter().println("doDelete.getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doDelete.getContextPath: "+request.getContextPath());
				response.getWriter().println("doDelete.getQueryString: "+request.getQueryString());
				response.getWriter().println("doDelete.getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doDelete.getServletContext: "+request.getServletContext());
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doPut. Full Stack Trace: "+buffer.toString());
		}
	}
	
	public void executeODataCalls(HttpServletRequest request, HttpServletResponse response, String oDataURL) throws IOException{
		// CloseableHttpClient httpClient = null;
		boolean debug=false;
		String serviceURL="", errorCode="", errorMsg="", aggregatorID = "", loginID="";
		HttpGet readRequest = null;
		HttpEntity countEntity = null;
		CommonUtils commonUtils = new CommonUtils();
		String tokenURL = "", batchID="";
		String userName="", password="", authParam="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
//		response.getWriter().println("loadMetadata oDataURL: "+oDataURL);
		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("PUGWHANA");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PUGWHANA", options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "PUGWHANA"));
            }
			
			// String proxyType = destConfiguration.get("ProxyType").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			if(debug){
		        // response.getWriter().println("executeODataCalls.proxyType: "+ proxyType);
		        response.getWriter().println("executeODataCalls.userName: "+ userName);
		        response.getWriter().println("executeODataCalls.password: "+ password);
		        response.getWriter().println("executeODataCalls.authParam: "+ authParam);
		        response.getWriter().println("executeODataCalls.basicAuth: "+ basicAuth);
			}
			
			/*String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	        int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
	        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);*/
	        
	        /*CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
			response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
			response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());*/
//			serviceURL = request.getRequestURI();
//			serviceURL = oDataURL+"$metadata";
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PUGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
//			response.getWriter().println("loadMetadata aggregatorID: "+aggregatorID);
			if(null == aggregatorID || aggregatorID.trim().length() == 0){
//				response.getWriter().println("");
				JsonObject result = new JsonObject();
				errorCode = "E176";
				errorMsg = properties.getProperty(errorCode);
				
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}else{
				serviceURL = request.getPathInfo();
				tokenURL = commonUtils.getODataDestinationProperties("URL", "PUGWHANA");
				
				if(serviceURL.contains("$metadata")) {
					serviceURL= "";
					serviceURL = oDataURL+"$metadata";
				}else if(serviceURL.contains("$batch")) {
					serviceURL= "";
					serviceURL = oDataURL+"$batch";
				}
				if(debug)
					response.getWriter().println("executeODataCalls.serviceURL: "+ serviceURL);
				
				String payloadRequest = "";
//				HttpDestination destination = getHTTPDestination(request, response, "PUGWHANA");
				
				if(!serviceURL.contains("$metadata")) {
//					String csrfToken = fetchCSRFToken(request, response, tokenURL);
//					response.getWriter().println("loadMetadata csrfToken: "+csrfToken);
					// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
					serviceURL = serviceURL.replaceAll("%20", " ");
//					if(csrfToken != null && !csrfToken.equalsIgnoreCase("E174") && !csrfToken.equalsIgnoreCase("E175")){
						if(serviceURL.contains("$batch")){
							String replacePayload = "";
							payloadRequest = getBody(request);
//							response.getWriter().println("executeODataCalls.payloadRequest: "+ payloadRequest);
//							response.getWriter().println("executeODataCalls.serviceURL: "+ serviceURL);
							
							if(payloadRequest.contains("GET UserProfiles(Application='PD')")){
//								replacePayload = payloadRequest.replace("GET UserProfiles(Application='PD')", "GET UserProfiles(Application='PD',AggregatorID='"+aggregatorID+"')");
								replacePayload = payloadRequest.replace("GET UserProfiles(Application='PD')", "GET UserProfiles?$filter=Application%20eq%20%27PD%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27");
							}
							else if (payloadRequest.contains("GET UserProfiles(Application='PS')")) {
								
								replacePayload = payloadRequest.replace("GET UserProfiles(Application='PS')", "GET UserProfiles?$filter=Application%20eq%20%27PS%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27");
							}
							
							batchID = request.getHeader("Content-Type");
//							response.getWriter().println("loadMetadata batchID: "+batchID);
							
							HttpEntity requestEntity = null;
							requestEntity = new StringEntity(replacePayload);
							
							HttpPost postRequest = new HttpPost(serviceURL);
							postRequest.setHeader("Content-Type", batchID);
							postRequest.setHeader("Accept", "multipart/mixed");
//								postRequest.setHeader("X-CSRF-Token", csrfToken);
							postRequest.setEntity(requestEntity);
							
							// HttpResponse httpPostResponse = httpClient.execute(postRequest);
							HttpResponse httpPostResponse = client.execute(postRequest);
							countEntity = httpPostResponse.getEntity();
							
							if(httpPostResponse.getEntity().getContentType() != null && httpPostResponse.getEntity().getContentType().toString() != "") {
								String contentType = httpPostResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
								if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
									response.setContentType(contentType);
									response.getOutputStream().print(EntityUtils.toString(countEntity));
								}else{
									response.setContentType(contentType);
									String Data = EntityUtils.toString(countEntity);
									response.getOutputStream().print(Data);	
								}
							}else{
								response.setContentType("application/pdf");
								response.getOutputStream().print(EntityUtils.toString(countEntity));
							}
							
							// httpClient.close();
//							}
						}else{
							if(request.getPathInfo().contains("/UserProfiles")){
//								replacePayload = payloadRequest.replace("GET UserProfiles(Application='PD')", "GET UserProfiles?$filter=Application%20eq%20%27PD%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27");
								String executeURL = "";
								executeURL = oDataURL+"UserProfiles?$filter=Application%20eq%20%27PD%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginID%20eq%20%27"+loginID+"%27";
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//								response.getWriter().println("loadMetadata executeURL: "+executeURL);
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//								response.getWriter().println("loadMetadata readRequest: "+readRequest);
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);
								countEntity = serviceResponse.getEntity();
								
								if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										response.setContentType(contentType);
										response.getOutputStream().print(EntityUtils.toString(countEntity));
									}else{
										response.setContentType(contentType);
										String Data = EntityUtils.toString(countEntity);
										response.getOutputStream().print(Data);	
									}
								}else{
									/*response.setContentType("application/pdf");
									response.getOutputStream().print(EntityUtils.toString(countEntity));*/
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}
							} else if (request.getPathInfo().equalsIgnoreCase("/PartnerRoles")) {
								
								String executeURL = "", appendMetaData="";

								executeURL = oDataURL+"PartnerRoles?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);
								
								countEntity = serviceResponse.getEntity();
								
								if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										response.setContentType(contentType);
										response.getOutputStream().print(EntityUtils.toString(countEntity));
									}else{
										JsonObject partnerRolesJsonObj = new JsonObject();
										response.setContentType(contentType);
										String Data = EntityUtils.toString(countEntity);
										
//										JsonParser parser = new JsonParser();
										partnerRolesJsonObj = (JsonObject)new JsonParser().parse(Data.toString());
										if (debug)
											response.getWriter().println("scfJsonObj: "+partnerRolesJsonObj);
										
										if (partnerRolesJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
											
											Gson gson = new Gson();
											JsonObject partnerRolesChildJsonObj = null;
											JsonObject buildPartnerRolesObj = null;
//											JsonArray partnerRoleJsonArray = new JsonArray();
//											JsonObject parterRoleResponseUI = new JsonObject();
											for (int i = 0; i < partnerRolesJsonObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
												
												partnerRolesChildJsonObj = new JsonObject();
												buildPartnerRolesObj= new JsonObject();
												partnerRolesChildJsonObj = partnerRolesJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
												
												appendMetaData = "{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/PartnerRoles('"+partnerRolesChildJsonObj.get("RoleID").getAsString()+"')\","
													+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/PartnerRoles('"+partnerRolesChildJsonObj.get("RoleID").getAsString()+"')\","
													+ "\"type\":\"ARTEC.PUGW.PartnerRole\"}";
												buildPartnerRolesObj = gson.fromJson(appendMetaData, JsonObject.class);
												partnerRolesChildJsonObj.add("__metadata", buildPartnerRolesObj);
											}
//											parterRoleResponseUI.add("d", new JsonObject());
//											parterRoleResponseUI.getAsJsonObject("d").add("results", partnerRoleJsonArray);
											response.getWriter().println(partnerRolesJsonObj);
											
										} else {
											response.getOutputStream().print(Data);
										}
									}
								}else{
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}
							}else if (request.getPathInfo().equalsIgnoreCase("/OrgTypes")) {
								
								String executeURL = "", appendSuccess="";
								executeURL = oDataURL+"OrgTypes?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);

								countEntity = serviceResponse.getEntity();
								
								if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										response.setContentType(contentType);
										response.getOutputStream().print(EntityUtils.toString(countEntity));
									}else{
										response.setContentType(contentType);
										String Data = EntityUtils.toString(countEntity);
//										JsonParser parser = new JsonParser();
										JsonObject orgTypeJsonObj = new JsonObject();
										orgTypeJsonObj = (JsonObject)new JsonParser().parse(Data.toString());
										if (debug)
											response.getWriter().println("orgTypeJsonObj: "+orgTypeJsonObj);
										
										if (orgTypeJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
											
											Gson gson = new Gson();
											JsonObject orgTypeChildJsonObj = null;
											JsonObject buildOrgTypeJsonObj = null;
											for (int i = 0; i < orgTypeJsonObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
											{
												orgTypeChildJsonObj = new JsonObject();
												buildOrgTypeJsonObj = new JsonObject();
												orgTypeChildJsonObj = orgTypeJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
												
												appendSuccess = "{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/OrgTypes('"+orgTypeChildJsonObj.get("OrgTypeID").getAsString()+"')\","
													+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/OrgTypes('"+orgTypeChildJsonObj.get("OrgTypeID").getAsString()+"')\","
													+ "\"type\":\"ARTEC.PUGW.OrgType\"}";
												buildOrgTypeJsonObj = gson.fromJson(appendSuccess, JsonObject.class);
												orgTypeChildJsonObj.add("__metadata", buildOrgTypeJsonObj);
											}
											
//											orgTypeResponseUI.add("d", new JsonObject());
//											orgTypeResponseUI.getAsJsonObject("d").add("results", orgTypeJsonArray);
											response.getWriter().println(orgTypeJsonObj);
											
										} else {
											response.getOutputStream().print(Data);
										}
									}
								}else{
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}
								
							}else if (request.getPathInfo().equalsIgnoreCase("/PartnerTypes")) {
								
								String executeURL = "", appendSuccess="";
								executeURL = oDataURL+"PartnerTypes?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);
								countEntity = serviceResponse.getEntity();
								
								if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										response.setContentType(contentType);
										response.getOutputStream().print(EntityUtils.toString(countEntity));
									}else{
										response.setContentType(contentType);
										String Data = EntityUtils.toString(countEntity);
										
										JsonObject partnerTypesJsonObj = new JsonObject();
										partnerTypesJsonObj = (JsonObject)new JsonParser().parse(Data.toString());
										if (debug)
											response.getWriter().println("scfJsonObj: "+partnerTypesJsonObj);
										
										if (partnerTypesJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
											
											Gson gson = new Gson();
											JsonObject partnerTypeChildJsonObj = null;
											JsonObject buildPartnerTypeJsonObj = null;
											for (int i = 0; i < partnerTypesJsonObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
											{
												partnerTypeChildJsonObj = new JsonObject();
												buildPartnerTypeJsonObj = new JsonObject();
												partnerTypeChildJsonObj = partnerTypesJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
												
												appendSuccess = "{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/PartnerTypes('"+partnerTypeChildJsonObj.get("PartnerTypeID").getAsString()+"')\","
													+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/PartnerTypes('"+partnerTypeChildJsonObj.get("PartnerTypeID").getAsString()+"')\","
													+ "\"type\":\"ARTEC.PUGW.PartnerType\"}";
												buildPartnerTypeJsonObj = gson.fromJson(appendSuccess, JsonObject.class);
												partnerTypeChildJsonObj.add("__metadata", buildPartnerTypeJsonObj);
												
											}
//											partnerTypeResponseUI.add("d", new JsonObject());
//											partnerTypeResponseUI.getAsJsonObject("d").add("results", partnerTypeJsonArray);
											response.getWriter().println(partnerTypesJsonObj);
											
										} else {
											response.getOutputStream().print(Data);
										}
									}
								}else{
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}
							
							}else if (request.getPathInfo().equalsIgnoreCase("/UserLogins")) {
								
								String errorResFormatForPGPayments="";
								//TODO remove debug
								debug = false;
								errorResFormatForPGPayments= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
										+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
										+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PUGW\",\"service_version\":\"0001\"},"
										+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
										+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
										+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
										+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
										+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
										+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
										+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
								
								if(request.getMethod().equalsIgnoreCase("POST")){
									
									payloadRequest =""; loginID="";
									String applicationID="", testRun="";
									boolean isValidationFailed = false;
									
									JSONArray userAuthpayLoadArray = null;
									JSONArray userPartnerpayLoadArray = null;
									payloadRequest = getGetBody(request, response);
									JSONObject inputPayload = new JSONObject(payloadRequest);
									try {
										if ( inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
												inputPayload.getBoolean("debug") == true) 
											debug = true;
										 
									} catch (Exception e) {
										debug = false;
									}
									
									if(debug)
										response.getWriter().println("UserLogins.inputPayload: "+inputPayload);
									
									try {
										if ( inputPayload.getString("LoginID").trim().length() > 0 )
											loginID = inputPayload.getString("LoginID");
										else
											isValidationFailed = true;
										
										if ( inputPayload.getString("Application").trim().length() > 0) 
											applicationID = inputPayload.getString("Application");
										else 
											isValidationFailed = true;

										if ( inputPayload.getString("LoginName").trim().length() == 0 )
											isValidationFailed = true;

										if ( inputPayload.getString("ERPLoginID").trim().length() == 0 )
											isValidationFailed = true;

										if ( inputPayload.getString("RoleID").trim().length() == 0 )
											isValidationFailed = true;
										
									} catch (Exception e)
									{
										isValidationFailed = true;
									}
									//TODO
									if ( ! isValidationFailed )
									{
										JsonObject validatePayloadResponse = new JsonObject();
										validatePayloadResponse = commonUtils.validateUserLoginFields(request, response, properties, inputPayload, loginID, debug);
										if (debug)
											response.getWriter().println("UserLogins.validatePayloadResponse: "+ validatePayloadResponse );
										
										if ( validatePayloadResponse.get("ErrorCode").getAsString().trim().length() == 0 )
										{
											Map<String, String> userLoginResponseMap = new HashMap<String, String>();
											userLoginResponseMap = commonUtils.getUserLoginsDetails(response, request, oDataURL, aggregatorID, loginID, applicationID, debug);
											if (debug) 
												response.getWriter().println("UserLogins.userLoginResponseMap: "+ userLoginResponseMap );
											
											testRun = inputPayload.getString("TestRun");
											if ( testRun.equalsIgnoreCase("X") ) {

												if ( ! userLoginResponseMap.get("ErrorCode").equalsIgnoreCase("") &&  userLoginResponseMap.get("ErrorCode").trim().length() > 0) {
													
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userLoginResponseMap.get("ErrorMsg"));
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userLoginResponseMap.get("ErrorCode"));
													response.getWriter().println( errorResFormatForPGPayments );
													
												} else {
													
													JsonObject masterObjectResponse = new JsonObject();
													JsonObject userAuthResponse = new JsonObject();
													JsonObject userPartnerResposne = new JsonObject();
													
													masterObjectResponse = commonUtils.buildUserLoginResponse(request, response, testRun, inputPayload, aggregatorID, loginID, applicationID, oDataURL, debug);
													if (debug)
														response.getWriter().println("UserLogins.masterObjectResponse: "+ masterObjectResponse );
													
													userAuthResponse = commonUtils.buildUserAuthsetResponse(request, response, inputPayload, aggregatorID, applicationID, loginID, oDataURL, debug);
													if (debug) 
														response.getWriter().println("UserLogins.userAuthResponse: "+ userAuthResponse );
													
													masterObjectResponse.getAsJsonObject("d").add("UserAuthSet", userAuthResponse);
													
													userPartnerResposne = commonUtils.buildUserPartnerResponse(request, response, inputPayload, aggregatorID, applicationID, loginID, oDataURL, debug);
													if (debug) 
														response.getWriter().println("UserLogins.userPartnerResposne: "+ userPartnerResposne );
													
													masterObjectResponse.getAsJsonObject("d").add("UserPartners", userPartnerResposne);
													if (debug) 
														response.getWriter().println("UserLogins.buildMasterJsonObj3: "+ masterObjectResponse );
													
													response.setContentType("application/json");
													response.getWriter().println(masterObjectResponse);
													
												}
												
											} else {
												if ( ! userLoginResponseMap.get("ErrorCode").equalsIgnoreCase("") &&  userLoginResponseMap.get("ErrorCode").trim().length() > 0 )
												{
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userLoginResponseMap.get("ErrorMsg"));
													errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userLoginResponseMap.get("ErrorCode"));
													response.getWriter().println( errorResFormatForPGPayments );
													
												} else
												{
													Map<String, String> userPartnerMap = new HashMap<String, String>();
													userPartnerpayLoadArray = new JSONArray();
													userPartnerpayLoadArray = inputPayload.getJSONArray("UserPartners");
													if (debug) {
														response.getWriter().println("UserLogins.userPartnerpayLoadArray: "+ userPartnerpayLoadArray );
													}
													userPartnerMap = commonUtils.getUserPartnersDetails(response, request, properties, userPartnerpayLoadArray, oDataURL, aggregatorID, loginID, applicationID, debug);
													if (debug) 
														response.getWriter().println("UserLogins.userPartnerMap: "+ userPartnerMap );
													
													if ( userPartnerMap.get("ErrorCode").trim().length() == 0 && userPartnerMap.get("ErrorMsg").trim().length() == 0) 
													{
														Map<String, String> userAuthsetMap = new HashMap<String, String>();
														userAuthpayLoadArray = new JSONArray();
														userAuthpayLoadArray = inputPayload.getJSONArray("UserAuthSet");
														if (debug) 
															response.getWriter().println("UserLogins.userAuthpayLoadArray: "+ userAuthpayLoadArray );
														
														userAuthsetMap = commonUtils.getUserAuthorizationDetails(response, request, properties, userAuthpayLoadArray, oDataURL, aggregatorID, loginID, applicationID, debug);
														if (debug) 
															response.getWriter().println("UserLogins.userAuthsetMap: "+ userAuthsetMap );
														
														if ( userAuthsetMap.get("ErrorCode").trim().length() > 0 && userAuthsetMap.get("ErrorMsg").trim().length() > 0) {
															
															response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
															errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userAuthsetMap.get("ErrorMsg"));
															errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userAuthsetMap.get("ErrorCode"));
															response.getWriter().println( errorResFormatForPGPayments );
															
														} else
														{
//															if ( ! testRun.equalsIgnoreCase("X")) {
															JsonObject userLoginHtppResponse = new JsonObject();
															userLoginHtppResponse = commonUtils.insertIntoUserLoginsDetails(response, request, properties, oDataURL, aggregatorID, inputPayload, debug);
															if(debug)
																response.getWriter().println("UserLogins.userLoginHtppResponse: "+ userLoginHtppResponse );
															if ( userLoginHtppResponse.has("ErrorCode") && userLoginHtppResponse.has("ErrorMsg") && 
																	userLoginHtppResponse.get("ErrorCode").getAsString().trim().length() > 0) {
																
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userLoginHtppResponse.get("ErrorMsg").getAsString());
																errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userLoginHtppResponse.get("ErrorCode").getAsString());
																response.getWriter().println( errorResFormatForPGPayments );
																
															} else {
																JsonObject userPartnerHtppResponse = new JsonObject();
																//Insert UserLogin
																userPartnerHtppResponse = commonUtils.insertIntoUserPartnerDetails(response, request, properties, oDataURL, aggregatorID, userPartnerpayLoadArray, debug);
																if(debug)
																	response.getWriter().println("UserLogins.userPartnerHtppResponse: "+ userPartnerHtppResponse );
																
																if (userPartnerHtppResponse.has("ErrorCode") && userPartnerHtppResponse.has("ErrorMsg")&& 
																		userPartnerHtppResponse.get("ErrorCode").getAsString().trim().length() > 0) {
																	
																	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																	errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userPartnerHtppResponse.get("ErrorMsg").getAsString());
																	errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userPartnerHtppResponse.get("ErrorCode").getAsString());
																	response.getWriter().println( errorResFormatForPGPayments );
																	
																}else
																{
																	JsonObject userAuthSetHtppResponse = new JsonObject();;
																	
																	//Insert userAuth MasterArray
																	userAuthSetHtppResponse = commonUtils.insertIntoUserAuthSetDetails(response, request, properties, oDataURL, aggregatorID, userAuthpayLoadArray, debug);
																	if(debug)
																		response.getWriter().println("UserLogins.userAuthSetHtppResponse: "+ userAuthSetHtppResponse );
																	
																	if ( userAuthSetHtppResponse.has("ErrorCode") && userAuthSetHtppResponse.has("ErrorMsg") && 
																			userAuthSetHtppResponse.get("ErrorCode").getAsString().trim().length() > 0) {
																		
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userAuthSetHtppResponse.get("ErrorMsg").getAsString());
																		errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userAuthSetHtppResponse.get("ErrorMsg").getAsString());
																		response.getWriter().println( errorResFormatForPGPayments );
																		
																		
																	} else {
																		JsonObject userAuthResponseUI = new JsonObject();
																		JsonObject userPartnerResponseUI = new JsonObject();
																		JsonObject buildMasterJsonObj = new JsonObject();
																		
																		JsonObject childUserLogin = userLoginHtppResponse.getAsJsonObject("d");
																		if(debug)
																			response.getWriter().println("UserLogins.childUserLogin: "+ childUserLogin );
																		
																		buildMasterJsonObj = commonUtils.buildUserLoginResponse(request, response, testRun, inputPayload, aggregatorID, loginID, applicationID, oDataURL, debug);
																		if(debug)
																			response.getWriter().println("UserLogins.buildMasterJsonObj: "+ buildMasterJsonObj );
																		
																		userAuthResponseUI= commonUtils.buildUserAuthsetResponse(request, response, inputPayload, aggregatorID, applicationID, loginID, oDataURL, debug);
																		if (debug) 
																			response.getWriter().println("UserLogins.userAuthResponseUI: "+ userAuthResponseUI );
																		
																		buildMasterJsonObj.getAsJsonObject("d").add("UserAuthSet", userAuthResponseUI);
																		if (debug) 
																			response.getWriter().println("UserLogins.buildMasterJsonObj2: "+ buildMasterJsonObj );
																		
																		userPartnerResponseUI = commonUtils.buildUserPartnerResponse(request, response, inputPayload, aggregatorID, applicationID, loginID, oDataURL, debug);
																		if (debug) 
																			response.getWriter().println("UserLogins.userPartnerResponseUI: "+ userPartnerResponseUI );
																		
																		buildMasterJsonObj.getAsJsonObject("d").add("UserPartners", userPartnerResponseUI);
																		if (debug) 
																			response.getWriter().println("UserLogins.buildMasterJsonObj3: "+ buildMasterJsonObj );
																		
																		response.setContentType("application/json");
																		response.getWriter().println(buildMasterJsonObj);
																	}
																}
															} 
														}
													} else
													{
														response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", userPartnerMap.get("ErrorMsg"));
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userPartnerMap.get("ErrorCode"));
														response.getWriter().println( errorResFormatForPGPayments );
													}
												}
											}
										}else
										{
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", validatePayloadResponse.get("ErrorMsg").getAsString());
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", validatePayloadResponse.get("ErrorCode").getAsString());
											response.getWriter().println( errorResFormatForPGPayments );
										}
									} else 
									{
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "024");
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("024"));
										response.getWriter().println( errorResFormatForPGPayments );
									}
								
								}else if (request.getMethod().equalsIgnoreCase("GET")) {
									
									debug = false;
									loginID ="";
									String executeURL = "", queryString="", userLoginMetaData="", applicationID="", userPartnerMetaData="",userAuthMetaData="",updatedQuery="";
									try {
										queryString = request.getQueryString();
									} catch (Exception e) {
										queryString ="";
									}
									if (debug)
										response.getWriter().println("queryString: "+queryString);
									
									
									if ( queryString == null || queryString == "" ) {
										executeURL = oDataURL+"UserLogins?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 if (queryString.contains("$filter")) {
											 String filterQuery = request.getParameter("$filter");
											 if (debug)
												 response.getWriter().println("filterQuery: "+filterQuery);
											 
											 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a").replaceAll("-", "%2D");;
											 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
												 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											 } else {
												 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											 }
											 executeURL = oDataURL+"UserLogins?"+updatedQuery;
										 } else {
											 executeURL = oDataURL+"UserLogins?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } 
									 }
									
									
									if (debug)
										response.getWriter().println("executeURL: "+executeURL);
									
									// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
									readRequest = new HttpGet(executeURL);
									readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
									// HttpResponse serviceResponse = httpClient.execute(readRequest);
									HttpResponse serviceResponse = client.execute(readRequest);
									countEntity = serviceResponse.getEntity();
									
									if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
										String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
										if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
											response.setContentType(contentType);
											response.getOutputStream().print(EntityUtils.toString(countEntity));
										}else{
											response.setContentType(contentType);
											String Data = EntityUtils.toString(countEntity);
											
											Gson gson = new Gson();
											JsonObject userLoginJson = new JsonObject();
											JsonObject childuserLoginObj = new JsonObject();;
											JsonObject buildItemJsonObj = null;
											
											userLoginJson = (JsonObject)new JsonParser().parse(Data.toString());
											if (debug)
												response.getWriter().println("userLoginJson: "+userLoginJson);
											

											if (userLoginJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
												
												executeURL ="";
												JsonArray userLoginDescArray = new JsonArray();
												userLoginDescArray = commonUtils.getUserLoginDescriptionArray(request, response, userLoginJson, oDataURL, aggregatorID, authParam, debug);

//												userLoginMetaData ="{\"id\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PUGW/service.xsodata/UserLogins(Application='APP_ID',LoginID='"+loginID+"')\","
//														+ "\"uri\":\"https://devci9yqyi812.hana.ondemand.com:443/ARTEC/PUGW/service.xsodata/UserLogins(Application='APP_ID',LoginID='"+loginID+"')\",\"type\":\"ARTEC.PUGW.UserLogin\"}";												
												
												for (int i = 0; i < userLoginJson.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
													
													buildItemJsonObj = new JsonObject();
													applicationID="";
													
													loginID = userLoginJson.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("LoginID").getAsString();

													userLoginMetaData ="{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserLogins(Application='APP_ID',LoginID='"+loginID+"')\","
															+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserLogins(Application='APP_ID',LoginID='"+loginID+"')\",\"type\":\"ARTEC.PUGW.UserLogin\"}";
													
													userAuthMetaData ="{\"__deferred\":{\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserLogins(Application='APP_ID',LoginID='"+loginID+"')/UserAuthSet\"}}";
													
													userPartnerMetaData ="{\"__deferred\":{\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserLogins(Application='APP_ID',LoginID='"+loginID+"')/UserPartners\"}}";

													childuserLoginObj = userLoginJson.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
													
													childuserLoginObj.remove("Source");
													childuserLoginObj.remove("AggregatorID");
													
													childuserLoginObj.addProperty("RoleDesc",userLoginDescArray.get(i).getAsJsonObject().get("RoleDesc").getAsString());
													childuserLoginObj.addProperty("RoleCatID",userLoginDescArray.get(i).getAsJsonObject().get("RoleCatID").getAsString());
													childuserLoginObj.addProperty("UserFunction1Desc", userLoginDescArray.get(i).getAsJsonObject().get("UserFunction1Desc").getAsString());
													childuserLoginObj.addProperty("UserFunction2Desc",userLoginDescArray.get(i).getAsJsonObject().get("UserFunction2Desc").getAsString());
													
													childuserLoginObj.addProperty("RoleCatDesc", "");
													
													if ( childuserLoginObj.get("CreatedBy").isJsonNull())
														childuserLoginObj.addProperty("CreatedBy", "");
													
													if ( childuserLoginObj.get("CreatedAt").isJsonNull())
														childuserLoginObj.addProperty("CreatedAt", "");
													
													if ( childuserLoginObj.get("CreatedOn").isJsonNull())
														childuserLoginObj.addProperty("CreatedOn", "");
													
													if ( childuserLoginObj.get("ChangedBy").isJsonNull())
														childuserLoginObj.addProperty("ChangedBy", "");
													
													if ( childuserLoginObj.get("ChangedAt").isJsonNull())
														childuserLoginObj.addProperty("ChangedAt", "");
													
													if ( childuserLoginObj.get("ChangedOn").isJsonNull())
														childuserLoginObj.addProperty("ChangedOn", "");
													
													if ( childuserLoginObj.get("SourceReferenceID").isJsonNull())
														childuserLoginObj.addProperty("SourceReferenceID", "");
													
													if (! childuserLoginObj.get("Application").isJsonNull() )
														applicationID = childuserLoginObj.get("Application").getAsString();
													
													userLoginMetaData =  userLoginMetaData.replaceAll("APP_ID", applicationID);
													userPartnerMetaData = userPartnerMetaData.replaceAll("APP_ID", applicationID);
													userAuthMetaData = userAuthMetaData.replaceAll("APP_ID", applicationID);
													
													buildItemJsonObj = gson.fromJson(userLoginMetaData, JsonObject.class);
													childuserLoginObj.add("__metadata", buildItemJsonObj);
													
													buildItemJsonObj = gson.fromJson(userAuthMetaData, JsonObject.class);
													childuserLoginObj.add("UserAuthSet", buildItemJsonObj);
													//userPartner MetaData
													buildItemJsonObj = gson.fromJson(userPartnerMetaData, JsonObject.class);
													childuserLoginObj.add("UserPartners", buildItemJsonObj);
													
												}
												response.getWriter().println(userLoginJson);
											}
											else{
												response.getWriter().println( userLoginJson );
											}
										}
									}else
									{
										response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
										response.getWriter().println("loadMetadata countEntity: "+countEntity);
									}
								}
								else
								{
									response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
									response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
									response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
									response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
									response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
									response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
									response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
									response.getWriter().println("Unable to read the Entity Type");
								}
								
							} else if (request.getPathInfo().equalsIgnoreCase("/UserPartners")) {
								String errorResFormatForPGPayments="";
								debug = false;
								payloadRequest ="";
								errorResFormatForPGPayments= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
										+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
										+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PUGW\",\"service_version\":\"0001\"},"
										+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
										+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
										+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
										+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
										+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
										+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
										+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
								
								if(debug)
									response.getWriter().println("PUGW.UserPartners.aggregatorID: "+aggregatorID);
								
								if ( request.getMethod().equalsIgnoreCase("GET")) {
									
									loginID ="";
									String executeURL = "", queryString="",  userPartnerMetaData="", applicationID="", partnerId="", erpSysID="", updatedQuery="";
									try {
										queryString = request.getQueryString();
									} catch (Exception e) {
										queryString ="";
									}
									
									if ( queryString == null || queryString == "" ) {
										executeURL = oDataURL+"UserPartners?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										if (queryString.contains("$filter")) {
											String filterQuery = request.getParameter("$filter");
											if (debug)
												response.getWriter().println("filterQuery: "+filterQuery);
											
											filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a").replaceAll("-", "%2D");;
											if (debug)
												response.getWriter().println("filterQuery.1: "+filterQuery);
											if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
												updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											} else {
												updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											}
											executeURL = oDataURL+"UserPartners?"+updatedQuery;
										} else {
											executeURL = oDataURL+"UserPartners?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										} 
									}
									
									
									if(debug)
										response.getWriter().println("PUGW.UserPartners.executeURL: "+executeURL);
									
									// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
									readRequest = new HttpGet(executeURL);
									readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
									// HttpResponse serviceResponse = httpClient.execute(readRequest);
									HttpResponse serviceResponse = client.execute(readRequest);

									countEntity = serviceResponse.getEntity();
									
									if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
										String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
										if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
											response.setContentType(contentType);
											response.getOutputStream().print(EntityUtils.toString(countEntity));
										}else{
											response.setContentType(contentType);
											String Data = EntityUtils.toString(countEntity);
											
											Gson gson = new Gson();
											JsonObject userPartnerJson = new JsonObject();
											JsonObject childUserPartnerJson = new JsonObject();
//											JsonArray userPartnerArray = new JsonArray();
//											JsonObject userPartnerResponseUI = new JsonObject();
											JsonObject buildItemJsonObj = new JsonObject();
											
											userPartnerJson = (JsonObject)new JsonParser().parse(Data.toString());
											if (debug)
												response.getWriter().println("userPartnerJson: "+userPartnerJson);
											
											if ( userPartnerJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0 ) {

												JsonArray userPartnerDescArray = new JsonArray();
												userPartnerDescArray = commonUtils.getUserPartnerDescriptionArray(request, response, userPartnerJson, oDataURL, aggregatorID, authParam, debug);
												if(debug)
													response.getWriter().println("PUGW.UserPartners.userPartnerDescArray: "+userPartnerDescArray);
												
												for (int i = 0; i < userPartnerJson.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
													
													applicationID=""; partnerId=""; erpSysID="";
													loginID = userPartnerJson.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("LoginID").getAsString();
													
													userPartnerMetaData ="{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserPartners(Application='APP_ID',"
															+ "LoginID='"+loginID+"',ERPSystemID='ERP_ID',PartnerID='PARTNER_ID')\","
															+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PUGW/UserPartners(Application='APP_ID',"
															+ "LoginID='"+loginID+"',ERPSystemID='ERP_ID',PartnerID='PARTNER_ID')\",\"type\":\"ARTEC.PUGW.UserPartner\"}";
													
													childUserPartnerJson = userPartnerJson.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();

													childUserPartnerJson.remove("Source");
													childUserPartnerJson.remove("AggregatorID");
													childUserPartnerJson.addProperty("PartnerTypeDesc", userPartnerDescArray.get(i).getAsJsonObject().get("PartnerTypeDesc").getAsString());
													childUserPartnerJson.addProperty("TestRun", "");
													
													if ( childUserPartnerJson.get("CreatedBy").isJsonNull())
														childUserPartnerJson.addProperty("CreatedBy", "");
													
													if ( childUserPartnerJson.get("CreatedAt").isJsonNull())
														childUserPartnerJson.addProperty("CreatedAt", "");
													
													if ( childUserPartnerJson.get("CreatedOn").isJsonNull())
														childUserPartnerJson.addProperty("CreatedOn", "");
													
													if ( childUserPartnerJson.get("ChangedBy").isJsonNull())
														childUserPartnerJson.addProperty("ChangedBy", "");
													
													if ( childUserPartnerJson.get("ChangedAt").isJsonNull())
														childUserPartnerJson.addProperty("ChangedAt", "");
													
													if ( childUserPartnerJson.get("ChangedOn").isJsonNull())
														childUserPartnerJson.addProperty("ChangedOn", "");
													
													if ( childUserPartnerJson.get("SourceReferenceID").isJsonNull())
														childUserPartnerJson.addProperty("SourceReferenceID", "");
													
													if (! childUserPartnerJson.get("Application").isJsonNull() )
														applicationID = childUserPartnerJson.get("Application").getAsString();
													
													if (! childUserPartnerJson.get("ERPSystemID").isJsonNull() )
														erpSysID = childUserPartnerJson.get("ERPSystemID").getAsString();
													
													if (! childUserPartnerJson.get("PartnerID").isJsonNull() )
														partnerId = childUserPartnerJson.get("PartnerID").getAsString();

													userPartnerMetaData = userPartnerMetaData.replaceAll("APP_ID", applicationID).replaceAll("ERP_ID", erpSysID).replaceAll("PARTNER_ID", partnerId);
													buildItemJsonObj = gson.fromJson(userPartnerMetaData, JsonObject.class);
													childUserPartnerJson.add("__metadata", buildItemJsonObj);
												}
												
												response.getWriter().println( userPartnerJson );
											}
											else {
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "044");
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("044"));
												response.getWriter().println( errorResFormatForPGPayments );
											}
										}
									}else
									{
										response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
										response.getWriter().println("loadMetadata countEntity: "+countEntity);
									}
										
								}else if (request.getMethod().equalsIgnoreCase("POST")) {
									payloadRequest ="";
									String applicationID="", testRun="", userPartnerReviewSuccess="";
									boolean isValidationFailed = false;
									loginID = "";
									
									userPartnerReviewSuccess = "{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PUGW/UserPartners(Application='',"
											+ "LoginID='',ERPSystemID='',PartnerID='')\","
											+ "\"uri\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PUGW/UserPartners(Application='',"
											+ "LoginID='',ERPSystemID='',PartnerID='')\",\"type\":\"ARTEC.PUGW.UserPartner\"},"
											+ "\"Application\":\"\",\"LoginID\":\"\","
											+ "\"ERPSystemID\":\"\",\"PartnerID\":\"\","
											+ "\"PartnerTypeID\":\"\",\"PartnerTypeDesc\":\"\",\"PartnerName\":\"\","
											+ "\"IsActive\":\"\",\"CreatedOn\":\"\",\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\",\"ChangedOn\":\"\","
											+ "\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\",\"TestRun\":\"\"}}";
									
									JSONArray userPartnerpayLoadArray = new JSONArray();
									payloadRequest = getGetBody(request, response);
									JSONObject inputPayload = new JSONObject(payloadRequest);
									try {
										if ( inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
												inputPayload.getString("debug").equalsIgnoreCase("true")) 
											debug = true;
												
									} catch (Exception e) {
										debug = false;
									}
									if(debug)
										response.getWriter().println("PGPayments.inputPayload: "+inputPayload);

									try {
										
										testRun = inputPayload.getString("TestRun");

										if ( inputPayload.getString("Application").trim().length() > 0) 
											applicationID = inputPayload.getString("Application");
										else
											isValidationFailed = true;
										
										if ( inputPayload.getString("LoginID").trim().length() == 0 )
											isValidationFailed = true;
										else
											loginID = inputPayload.getString("LoginID");
										
										if ( inputPayload.getString("PartnerID").trim().length() == 0 )
											isValidationFailed = true;
										
										if ( inputPayload.getString("ERPSystemID").trim().length() == 0 )
											isValidationFailed = true;
										
										if ( inputPayload.getString("PartnerName").trim().length() == 0 )
											isValidationFailed = true;
										
									} catch (Exception e) {
										isValidationFailed = true;
									}
									
									if (debug)
									{
										response.getWriter().println("UserPartners.applicationID: "+applicationID);
										response.getWriter().println("UserPartners.testRun: "+ testRun );
									}
									if (! isValidationFailed) 
									{
										Map<String, String> userLoginMap = new HashMap<String, String>();
										userLoginMap = commonUtils.getUserLoginsDetails(response, request, oDataURL, aggregatorID, loginID, applicationID, debug);
										
										if (userLoginMap.get("ErrorCode").equalsIgnoreCase("079")) {
											
											Map<String, String> userPartnerMap = new HashMap<String, String>();
											userPartnerpayLoadArray.put(inputPayload);
											
											userPartnerMap = commonUtils.getUserPartnersDetails(response, request, properties, userPartnerpayLoadArray, oDataURL, aggregatorID, loginID, applicationID, debug);
											if (debug) 
												response.getWriter().println("UserPartners.userPartnerMap: "+ userPartnerMap );
											
											if ( userPartnerMap.get("ErrorCode").equalsIgnoreCase("") && userPartnerMap.get("ErrorCode").trim().length() == 0)
											{
												if (testRun.equalsIgnoreCase("x")) {
													//Review Success
													response.setContentType("application/json");
													response.getWriter().println(userPartnerReviewSuccess);
												} else
												{
													JsonObject userPartnerHttpResponse = new JsonObject();
													userPartnerHttpResponse = commonUtils.insertIntoUserPartnerDetails(response, request, properties, oDataURL, aggregatorID, userPartnerpayLoadArray, debug);
													if (userPartnerHttpResponse.has("ErrorCode") ) {
														//failure
														response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userPartnerHttpResponse.get("ErrorCode").getAsString());
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",userPartnerHttpResponse.get("ErrorMsg").getAsString());
														response.getWriter().println( errorResFormatForPGPayments );
													}
													else
													{
														//Save Success
														response.setContentType("application/json");
														response.getWriter().println(userPartnerReviewSuccess);
													}
												}
											} 
											else
											{
												//failure
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userPartnerMap.get("ErrorCode"));
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",userPartnerMap.get("ErrorMsg"));
												response.getWriter().println( errorResFormatForPGPayments );
												
											}
										} else {
											//failure
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "001");
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE","LoginID not found");
											response.getWriter().println( errorResFormatForPGPayments );
										}
										
									}
									else
									{
										//Validation failure
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "058");
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",properties.getProperty("058"));// + loginID);
										response.getWriter().println( errorResFormatForPGPayments );
									}
								} else {
									response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
									response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
									response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
									response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
									response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
									response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
									response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
									response.getWriter().println("Unable to read the Entity Type");
								}
							}else if (request.getPathInfo().equalsIgnoreCase("/UserAuthSet")) {
								String errorResFormatForPGPayments="";
								debug = false;
								payloadRequest ="";
								errorResFormatForPGPayments= "{\"error\":{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":{\"lang\":\"en\","
										+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
										+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PUGW\",\"service_version\":\"0001\"},"
										+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
										+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
										+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
										+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
										+ "\"errordetails\":[{\"code\":\"/ARTEC/PC/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
										+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
										+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
								
								
								if ( request.getMethod().equalsIgnoreCase("GET")) {
									String executeURL = "", queryString="", updatedQuery="";
//											,  userAuthMetaData="", applicationID="",authOrgTypeID="", erpSysID="",authOrgValue="";
									try {
										queryString = request.getQueryString();
									} catch (Exception e) {
										queryString ="";
									}
									
									if(debug)
										response.getWriter().println("PUGW.UserAuthSet.aggregatorID: "+aggregatorID);
									
//									if ( queryString == null || queryString == "" ) {
//										executeURL = oDataURL+"UserAuthSet?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
//									} else {
//										executeURL = oDataURL+"UserAuthSet?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
//									}
									
									if ( queryString == null || queryString == "" ) {
										executeURL = oDataURL+"UserAuthSet?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									 } else {
										 if (queryString.contains("$filter")) {
											 String filterQuery = request.getParameter("$filter");
											 if (debug)
												 response.getWriter().println("filterQuery: "+filterQuery);
											 
											 filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a").replaceAll("-", "%2D");;
											 if (debug)
												 response.getWriter().println("filterQuery.1: "+filterQuery);
											 if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
												 updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											 } else {
												 updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
											 }
											 executeURL = oDataURL+"UserAuthSet?"+updatedQuery;
										 } else {
											 executeURL = oDataURL+"UserAuthSet?"+queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
										 } 
									 }

									
									
									if(debug)
										response.getWriter().println("PUGW.UserAuthSet.executeURL: "+executeURL);
									
//									if ( queryString.trim().length() > 0 )//$filter=LoginID%20eq%20%2754545%27
//									else
//										executeURL = oDataURL+"UserAuthSet?$filter="+"AggregatorID%20eq%20%27"+aggregatorID+"%27";
									
									// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
									readRequest = new HttpGet(executeURL);
									readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
									// HttpResponse serviceResponse = httpClient.execute(readRequest);
									HttpResponse serviceResponse = client.execute(readRequest);
									countEntity = serviceResponse.getEntity();
									
									if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
										String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
										if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
											response.setContentType(contentType);
											response.getOutputStream().print(EntityUtils.toString(countEntity));
										}else{
											response.setContentType(contentType);
											String Data = EntityUtils.toString(countEntity);
											
//											JsonObject userPartnerResponseUI = new JsonObject();
//											JsonObject buildItemJsonObj = new JsonObject();
											JsonObject userAuthJson = new JsonObject();
											JsonObject childUserAuthJson = new JsonObject();
											
											userAuthJson = (JsonObject)new JsonParser().parse(Data.toString());
											if (debug)
												response.getWriter().println("userAuthJson: "+userAuthJson);
											
											if ( userAuthJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0 ) {
											
												JsonArray userAuthDescArray = new JsonArray();
												userAuthDescArray =  commonUtils.getUserAuthDescriptionArray(request, response, userAuthJson, oDataURL, aggregatorID, authParam, debug);
												if(debug)
													response.getWriter().println("PUGW.userAuthSet.userAuthDescArray: "+userAuthDescArray);
												
												for (int i = 0; i < userAuthJson.getAsJsonObject("d").getAsJsonArray("results").size(); i++) {
													
													childUserAuthJson = userAuthJson.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
													
													if ( childUserAuthJson.get("CreatedBy").isJsonNull())
														childUserAuthJson.addProperty("CreatedBy", "");
													
													if ( childUserAuthJson.get("CreatedAt").isJsonNull())
														childUserAuthJson.addProperty("CreatedAt", "");
													
													if ( childUserAuthJson.get("CreatedOn").isJsonNull())
														childUserAuthJson.addProperty("CreatedOn", "");
													
													if ( childUserAuthJson.get("ChangedBy").isJsonNull())
														childUserAuthJson.addProperty("ChangedBy", "");
													
													if ( childUserAuthJson.get("ChangedAt").isJsonNull())
														childUserAuthJson.addProperty("ChangedAt", "");
													
													if ( childUserAuthJson.get("ChangedOn").isJsonNull())
														childUserAuthJson.addProperty("ChangedOn", "");
													
													if ( childUserAuthJson.get("SourceReferenceID").isJsonNull())
														childUserAuthJson.addProperty("SourceReferenceID", "");
													
//													applicationID=""; partnerId=""; erpSysID="";
													childUserAuthJson.remove("AggregatorID");
													childUserAuthJson.remove("Source");
													childUserAuthJson.addProperty("TestRun", "");
													childUserAuthJson.addProperty("AuthOrgTypeDesc", userAuthDescArray.get(i).getAsJsonObject().get("OrgTypeDesc").getAsString());
													
												}
												response.getWriter().println(userAuthJson);
											}
											else {
												response.getWriter().println(userAuthJson);
											}
										}
									}else
									{
										response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
										response.getWriter().println("loadMetadata countEntity: "+countEntity);
									}
								}
								else if (request.getMethod().equalsIgnoreCase("POST")) {
									payloadRequest ="";
									String applicationID="", testRun="", userAuthSuccess="",authOrgTypeID="" ;
									boolean isValidationFailed = false;
									loginID = "";
									
									userAuthSuccess =	"{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PUGW/UserAuthSet(Application='',"
											+ "LoginID='',ERPSystemID='',AuthOrgTypeID='',AuthOrgValue='')\","
											+ "\"uri\":\"https://flpnwc-z93y6qtneb.dispatcher.hana.ondemand.com/sap/fiori/ssflpplugin/sap/opu/odata/ARTEC/PUGW/UserAuthSet(Application='',"
											+ "LoginID='',ERPSystemID='',AuthOrgTypeID='',AuthOrgValue='')\",\"type\":\"ARTEC.PUGW.UserAuth\"},\"Application\":\"\",\"LoginID\":\"\",\"ERPSystemID\":\"\","
											+ "\"AuthOrgTypeID\":\"\",\"AuthOrgValue\":\"\",\"AuthOrgTypeDesc\":\"\",\"AuthOrgValDsc\":\"\","
											+ "\"CreatedOn\":\"\",\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\",\"ChangedOn\":\"\",\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\",\"TestRun\":\"\"}}";
									
									JSONArray userAuthpayLoadArray = new JSONArray();
									payloadRequest = getGetBody(request, response);
									JSONObject inputPayload = new JSONObject(payloadRequest);
									try {
										if ( inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
												inputPayload.getString("debug").equalsIgnoreCase("true")) 
											debug = true;
												
									} catch (Exception e) {
										debug = false;
									}
//									debug = true;
									if(debug)
										response.getWriter().println("POST.inputPayload: "+inputPayload);
									
									try {
										testRun = inputPayload.getString("TestRun");

										/*if(!testRun.equalsIgnoreCase("x")){
											debug=true;
										}*/
										
										if ( inputPayload.getString("Application").trim().length() > 0) 
											applicationID = inputPayload.getString("Application");
										else
											isValidationFailed = true;
										if ( inputPayload.getString("LoginID").trim().length()== 0 )
											isValidationFailed = true;
										else
											loginID = inputPayload.getString("LoginID");
										
										if ( inputPayload.getString("AuthOrgTypeID").trim().length() == 0 ){
											authOrgTypeID =inputPayload.getString("AuthOrgTypeID");
											isValidationFailed = true;
										}
										if ( inputPayload.getString("ERPSystemID").trim().length() == 0 )
											isValidationFailed = true;
										
										if ( inputPayload.getString("AuthOrgValue").trim().length() == 0 )
											isValidationFailed = true;
										
									} catch (Exception e) {
										isValidationFailed = true;
									}
									
									if(debug){
										response.getWriter().println("UserAuthSet.applicationID: "+applicationID);
										response.getWriter().println("UserAuthSet.testRun: "+ testRun );
										response.getWriter().println("UserAuthSet.authOrgTypeID: "+ authOrgTypeID);
									}
									
									if ( ! isValidationFailed ) 
									{
										Map<String, String> userLoginMap = new HashMap<String, String>();
										userLoginMap = commonUtils.getUserLoginsDetails(response, request, oDataURL, aggregatorID, loginID, applicationID, debug);
										
										if (userLoginMap.get("ErrorCode").equalsIgnoreCase("079")) {
											
											Map<String, String> userAuthMap = new HashMap<String, String>();
											userAuthpayLoadArray.put(inputPayload);
											if(debug)
												response.getWriter().println("UserAuthSet.userAuthpayLoadArray: "+ userAuthpayLoadArray );
											
											userAuthMap = commonUtils.getUserAuthorizationDetails(response, request, properties, userAuthpayLoadArray, oDataURL, aggregatorID, loginID, applicationID, debug);
											if (debug) 
												response.getWriter().println("UserAuthSet.userAuthMap: "+ userAuthMap );
											if ( userAuthMap.get("ErrorCode").equalsIgnoreCase("") && userAuthMap.get("ErrorCode").trim().length() == 0 ) {
												
												if (testRun.equalsIgnoreCase("x"))
												{
													//Review Success
													response.setContentType("application/json");
													response.getWriter().println(userAuthSuccess);
												} 
												else
												{
													JsonObject userPartnerHttpResponse = new JsonObject();
													userPartnerHttpResponse = commonUtils.insertIntoUserAuthSetDetails(response, request, properties, oDataURL, aggregatorID, userAuthpayLoadArray, debug);
													if (userPartnerHttpResponse.has("ErrorCode") )
													{
														//failure
														response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userPartnerHttpResponse.get("ErrorCode").getAsString());
														errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",userPartnerHttpResponse.get("ErrorMsg").getAsString());
														response.getWriter().println( errorResFormatForPGPayments );
													}
													else
													{
														//Save Success
														response.setContentType("application/json");
														response.getWriter().println(userAuthSuccess);
													}
												}
											} else {
												//failure
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", userAuthMap.get("ErrorCode"));
												errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",userAuthMap.get("ErrorMsg"));
												response.getWriter().println( errorResFormatForPGPayments );
											}
										} else {
											//failure
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "001");
											errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE","LoginID not found");
											response.getWriter().println( errorResFormatForPGPayments );
										}
									}
									else
									{
										//Validation failure
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "058");
										errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE",properties.getProperty("058"));// + authOrgTypeID);
										response.getWriter().println( errorResFormatForPGPayments );
									}
								}else
								{
									response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
									response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
									response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
									response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
									response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
									response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
									response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
									response.getWriter().println("Unable to read the Entity Type");
								}
							}
							else{
								String executeURL = "";
								String queryString = request.getQueryString();
								if(null != queryString && queryString.trim().length()>0){

									String entityInfo  = request.getPathInfo().replace("/", "");;
//									response.getWriter().println("loadMetadata entityInfo: "+entityInfo);
									if(null != entityInfo && entityInfo.trim().length()>0){
										executeURL = oDataURL+entityInfo+"?"+queryString;
										// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//										
										// response.getWriter().println("loadMetadata httpClient: "+httpClient);
										readRequest = new HttpGet(executeURL);
										readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//										response.getWriter().println("loadMetadata readRequest: "+readRequest);
										// HttpResponse serviceResponse = httpClient.execute(readRequest);
										HttpResponse serviceResponse = client.execute(readRequest);

										countEntity = serviceResponse.getEntity();
//										response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//										response.getWriter().println("loadMetadata countEntity: "+countEntity);
//										response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
										
										if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
											String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
											if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
												response.setContentType(contentType);
												response.getOutputStream().print(EntityUtils.toString(countEntity));
											}else{
												response.setContentType(contentType);
												String Data = EntityUtils.toString(countEntity);
												response.getOutputStream().print(Data);	
											}
										}else{
											/*response.setContentType("application/pdf");
											response.getOutputStream().print(EntityUtils.toString(countEntity));*/
											response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
											response.getWriter().println("loadMetadata countEntity: "+countEntity);
										}
									}else{
										response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
										response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
										response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
										response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
										response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
										response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
										response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
										response.getWriter().println("Unable to read the Entity Type");
									}
								}else{
									response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
									response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
									response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
									response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
									response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
									response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
									response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
									response.getWriter().println("No query received in the request");
								}
							}
						/*} else {
							JsonObject result = new JsonObject();
							result.addProperty("ErrorCode", csrfToken);
							result.addProperty("Message", properties.getProperty(csrfToken));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						}*/
						}
						
						
				}else{
					serviceURL = serviceURL.replaceAll("%20", " ");
//					response.getWriter().println("loadMetadata serviceURL: "+serviceURL);
//					destination = getHTTPDestination(request, response, "PUGWHANA");
					
					// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
					readRequest = new HttpGet(serviceURL);
//					readRequest.setParams(params);
					
					// HttpResponse serviceResponse = httpClient.execute(readRequest);
					HttpResponse serviceResponse = client.execute(readRequest);
					countEntity = serviceResponse.getEntity();
					
//					response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
					
					if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
						String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
						if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
							response.setContentType(contentType);
							response.getOutputStream().print(EntityUtils.toString(countEntity));
						}else{
							response.setContentType(contentType);
							String Data = EntityUtils.toString(countEntity);
							response.getOutputStream().print(Data);	
						}
					}else{
						response.setContentType("application/pdf");
						response.getOutputStream().print(EntityUtils.toString(countEntity));
					}
				}
			}
		}catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "E173");
			result.addProperty("Message", e.getMessage());
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
			if(debug)
				e.printStackTrace();
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().print(new Gson().toJson("Full Stack Trace:"+buffer.toString()));
			
		}finally{
			// httpClient.close();
		}
	}
		
	/*public String fetchCSRFToken(HttpServletRequest request, HttpServletResponse response, String serviceURL) {
		String csrfToken="";
		HttpGet readRequest = null;
		try{
			HttpDestination destination = getHTTPDestination(request, response, "PCGWHANA");
			HttpClient httpClient = destination.createHttpClient();
			
			readRequest = new HttpGet(serviceURL);
			readRequest.setHeader("X-CSRF-Token", "fetch");
			
			HttpResponse serviceResponse = httpClient.execute(readRequest);
			
			Header[] headers = serviceResponse.getAllHeaders();
			for (Header header : headers) {
				response.getWriter().println("fetchCSRFToken - Key : " + header.getName() 
				      + " ,Value : " + header.getValue());
				
				if(header.getName().equalsIgnoreCase("x-csrf-token")){
					csrfToken = header.getValue();
				}
			}
			
			if(null == csrfToken){
				csrfToken = "E174";
			}else if(csrfToken != null && csrfToken.trim().length() == 0){
				csrfToken = "E174";
			}
			
			countEntity = serviceResponse.getEntity();
			
			if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
				csrfToken = serviceResponse.getHeaders("x-csrf-token").toString();
			}else{
				csrfToken = "No response received from the HANA system";
			}
		}catch (Exception e) {
			csrfToken = "E175";
		}
		
		return csrfToken;
	}*/
	
	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = "pugwmeta";
		}
		
		HttpDestination destination = null;
		try {
			//response.getWriter().println("destinationName: " +  destinationName);
			// look up the connectivity configuration API "DestinationFactory"
			Context ctxDestFact = new InitialContext();
			//Get HTTP destination 
			DestinationFactory destinationFactory = (DestinationFactory) ctxDestFact.lookup(DestinationFactory.JNDI_NAME);
			if(destinationFactory != null)
			{
				destination = (HttpDestination) destinationFactory.getDestination(destinationName);
			}
		} catch (NamingException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
		}
		return destination;
	}*/
	
	public String getBody(HttpServletRequest request) throws IOException {
		String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
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
//							  response.getWriter().println("jb.toString(): "+jb.toString());
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

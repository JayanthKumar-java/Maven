package com.arteriatech.hana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

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
import org.json.JSONObject;

import com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutStandingClient;
import com.arteriatech.bc.SCFOffer.SCFOfferClient;
import com.arteriatech.bc.TransactionOTPGenerate.OTPGeneratorClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.Principal;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import com.sap.cloud.sdk.cloudplatform.tenant.Tenant;
/**
 * Servlet implementation class PYGW
 */
@WebServlet("/PYGW")
public class PYGW extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PYGW() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String oDataURL = "", loginID="";
		boolean debug=false;
		CommonUtils utils = new CommonUtils();
		/* Try<Tenant> currentTenant = TenantAccessor.tryGetCurrentTenant();
		Try<Principal> currentPrincipal = PrincipalAccessor.tryGetCurrentPrincipal();
		System.out.println("currentTenant: "+currentTenant);
		System.out.println("currentPrincipal: "+currentPrincipal); */

		oDataURL = utils.getODataDestinationProperties("URL", "PYGWHANA");
		if(oDataURL != null && ! oDataURL.equalsIgnoreCase("E106") && ! oDataURL.contains("E173")){
			loginID = utils.getODataDestinationProperties("User", "PYGWHANA");
			if(loginID != null && ! loginID.equalsIgnoreCase("E106") && ! loginID.contains("E173")){
				executeODataCalls(request, response, oDataURL, debug);
			}else{
				response.getWriter().println("Destination 'PYGWHANA' not maintained in sub account");
			}
		}else{
			response.getWriter().println("Destination 'PYGWHANA' not maintained in sub account");
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String payloadRequest = "", dataPayload="", corpID1="", aggregatorID="", loginID="", oDataURL="";
		try{
			debug = false;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			
			// corpID = commonUtils.getCorpID();
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			
			if(debug){
				response.getWriter().println("Inside doPut");
				response.getWriter().println("doPut.getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doPut.getRequestURL: "+request.getRequestURL());
				response.getWriter().println("doPut.getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doPut.getContextPath: "+request.getContextPath());
				response.getWriter().println("doPut.getQueryString: "+request.getQueryString());
				response.getWriter().println("doPut.getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doPut.getServletContext: "+request.getServletContext());
			}
			
			payloadRequest = getGetBody(request, response);
//			dataPayload = payloadRequest;
			dataPayload = payloadRequest.replaceAll("\\\\", "");
			JSONObject inputJsonObject = new JSONObject(payloadRequest);
				
			if(debug){
				response.getWriter().println("doPut.payloadRequest: "+payloadRequest);
				response.getWriter().println("doPut.dataPayload: "+dataPayload);
				// response.getWriter().println("doPut.corpID: "+corpID);
				response.getWriter().println("doPut.aggregatorID: "+aggregatorID);
				response.getWriter().println("doPut.loginID: "+loginID);
				response.getWriter().println("doPut.oDataURL: "+oDataURL);
				response.getWriter().println("doPut inputJsonObject: "+inputJsonObject);
			}
			commonUtils.userAccountsChange(request, response, inputJsonObject, loginID, aggregatorID, oDataURL, properties, debug);
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
		String pathInfo = "", dataPayload="", corpID="", aggregatorID="", loginID="", oDataURL="", accountGuid = "";
		JsonObject bankAccountDeleteJson = new JsonObject();
		try{
//			debug = true;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));	
			pathInfo = request.getPathInfo();
			accountGuid = pathInfo.substring((pathInfo.indexOf("'")+1), pathInfo.lastIndexOf("'"));
			
			if(debug){
				response.getWriter().println("Inside doDelete");
				response.getWriter().println("doDelete getRequestURI: "+request.getRequestURI());
				response.getWriter().println("doDelete getRequestURL: "+request.getRequestURL());
				response.getWriter().println("doDelete getPathInfo: "+request.getPathInfo());
				response.getWriter().println("doDelete getContextPath: "+request.getContextPath());
				response.getWriter().println("doDelete getQueryString: "+request.getQueryString());
				response.getWriter().println("doDelete getParameterMap: "+request.getParameterMap());
				response.getWriter().println("doDelete getServletContext: "+request.getServletContext());
				response.getWriter().println("doDelete pathInfo: "+pathInfo);
				response.getWriter().println("doDelete accountGuid: "+accountGuid);
			}
			
			
			/*payloadRequest = getGetBody(request, response);
//			dataPayload = payloadRequest;
			dataPayload = payloadRequest.replaceAll("\\\\", "");
			JSONObject inputJsonObject = new JSONObject(payloadRequest);*/
				
			if(debug){
				response.getWriter().println("doDelete.accountGuid: "+accountGuid);
				response.getWriter().println("doDelete.dataPayload: "+dataPayload);
				response.getWriter().println("doDelete.corpID: "+corpID);
				response.getWriter().println("doDelete.aggregatorID: "+aggregatorID);
				response.getWriter().println("doDelete.loginID: "+loginID);
				response.getWriter().println("doDelete.oDataURL: "+oDataURL);
				response.getWriter().println("doDelete pathInfo: "+pathInfo);
			}
			bankAccountDeleteJson = commonUtils.deleteAccount(request, response, accountGuid, loginID, aggregatorID, oDataURL, properties, debug);
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.getWriter().println(bankAccountDeleteJson);
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in doDelete. Full Stack Trace: "+buffer.toString());
		}
	}

	public void executeODataCalls(HttpServletRequest request, HttpServletResponse response, String oDataURL, boolean debug) throws IOException{
		// CloseableHttpClient httpClient = null;
		String serviceURL="", aggregatorID="", loginID="";
		String responseFromClient = "", corpID="";
		String userName="", password="", authParam="";
		HttpGet readRequest = null;
		HttpEntity countEntity = null;
		Map scfOfferResponseMap = null;
		CommonUtils commonUtils = new CommonUtils();
		SCFOfferClient offerClient = new SCFOfferClient();
		String tokenURL = "", batchID="";
		String payloadRequest = "", cpTypeID="", corpPayload="";
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));		
		try{
			serviceURL = request.getPathInfo();
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration("PYGWHANA");
			// response.getWriter().println("executeODataCalls Before: ");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			// response.getWriter().println("executeODataCalls After: ");
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			// response.getWriter().println("executeODataCalls destinationAccessor: ");
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			// response.getWriter().println("executeODataCalls client: ");
			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "PYGWHANA"));
            }
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug=true;

			String proxyType = destConfiguration.get("ProxyType").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			/* if(request.getPathInfo().equalsIgnoreCase("/SupplyChainFinances")){
				if(request.getMethod().equalsIgnoreCase("POST"))
					debug=true;
			} */

			// response.getWriter().println("executeODataCalls basicAuth: ");
			if(debug){
		        response.getWriter().println("executeODataCalls.proxyType: "+ proxyType);
		        response.getWriter().println("executeODataCalls.userName: "+ userName);
		        response.getWriter().println("executeODataCalls.password: "+ password);
		        response.getWriter().println("executeODataCalls.authParam: "+ authParam);
		        response.getWriter().println("executeODataCalls.basicAuth: "+ basicAuth);
			}
			
			/*String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	        int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
	        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);*/
	        
	        /* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials); */
	        // corpPayload = getBody(request, response);
			// if(debug)
			// 	response.getWriter().println("corpPayload: "+corpPayload);

			// payloadRequest = getBody(request, response);
			// if(debug)
			// 	response.getWriter().println("payloadRequest: "+payloadRequest);
			
			/* boolean validJsonBody = false;
			JSONObject payloadJson = new JSONObject();
			try{
				payloadJson = new JSONObject(corpPayload);
				validJsonBody = true;
			}catch(Exception e){
				validJsonBody = false;
			}
			
			if(validJsonBody){
				if(payloadJson.has("CPTypeID")){
					cpTypeID = payloadJson.getString("CPTypeID");
					
				}else{
					corpID = "";
				}
			} */
			corpID = commonUtils.getCorpID();
			if(debug)
				response.getWriter().println("executeODataCalls corpID: "+corpID);

			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			if(debug)
				response.getWriter().println("executeODataCalls aggregatorID: "+aggregatorID);
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			if(debug)
				response.getWriter().println("executeODataCalls loginID: "+loginID);
			
			/*response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
			response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
			response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
			response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
			response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
//			response.getWriter().println("loadMetadata getParameterMap: "+request.getParameterMap());
			response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());*/
			
//			response.getWriter().println("loadMetadata getParameterMap: "+request.);
//			serviceURL = request.getRequestURI();
			serviceURL = request.getPathInfo();
			tokenURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			// response.getWriter().println("executeODataCalls tokenURL: "+tokenURL);
//			serviceURL = oDataURL+"$metadata";
			if(serviceURL.contains("$metadata")) {
				serviceURL= "";
				serviceURL = oDataURL+"$metadata";
			}else if(serviceURL.contains("$batch")) {
				serviceURL= "";
				serviceURL = oDataURL+"$batch";
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("executeODataCalls.serviceURL: "+ serviceURL);
			
			
			String sampleSuccessResponse="", bpHdrErrResponseFormat="", sampleSuccessODataResponse="", sampleSuccessJsonResponse = "", bpHdrSuccessResponseFormat="", supplyChainSuccessFormat="", supplyChainErrorFormat="", dataPayload="";
//			HttpDestination destination = getHTTPDestination(request, response, "PYGWHANA");
			StringBuffer responseBuffer = new StringBuffer();
			
			if(! serviceURL.contains("$metadata")) {
				// debug = true;
//				String csrfToken = fetchCSRFToken(request, response, tokenURL);
//				response.getWriter().println("loadMetadata csrfToken: "+csrfToken);
				// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
				serviceURL = serviceURL.replaceAll("%20", " ");
//				if(csrfToken != null && !csrfToken.equalsIgnoreCase("E174") && !csrfToken.equalsIgnoreCase("E175")){
					if(serviceURL.contains("$batch")){
						payloadRequest = "";
						payloadRequest = getBody(request, response);
						dataPayload = payloadRequest;
//						response.getWriter().println("loadMetadata payloadRequest: "+payloadRequest);
						
						//Use  dataPayload variable for validations
						
						/*if(payloadRequest.contains("POST BPHeader")){
							bpHdrErrResponseFormat = "{\"error\":{\"code\":\"/ARTEC/PY/063\",\"message\":{\"lang\":\"en\",\"value\":\"Active\"},\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},\"transactionid\":\"7701B5E9D0DBF18FBC9CD067E5F9AB12\",\"timestamp\":\"20190802084234.6680000\",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},\"errordetails\":[{\"code\":\"/ARTEC/PY/063\",\"message\":\"Active\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
							bpHdrSuccessResponseFormat = "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/BPHeaders(guid'8ABA4CB5-ED86-4B94-90AA-507992E6C726')\",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/BPHeaders(guid'8ABA4CB5-ED86-4B94-90AA-507992E6C726')\",\"type\":\"ARTEC.PYGW.BPHeader\"},\"LoginID\":\"E9B50177304E4BF1BC9CD067E5F9AB12\",\"BPGuid\":\"8ABA4CB5-ED86-4B94-90AA-507992E6C726\",\"CPGuid\":\"394\",\"CPName\":\"\",\"CPType\":\"01\",\"CPTypeDesc\":\"\",\"IncorporationDate\":\"\\/Date(-2208988800000)\\/\",\"Testrun\":\"\",\"UtilDistrict\":\"MH607\",\"UtilDistrictDs\":\"MH-SINDHUDURG\",\"LegalStatus\":\"03\",\"LegalStatusDs\":\"\",\"CreatedOn\":null,\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\",\"ChangedOn\":null,\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\",\"BPContactPersons\":{\"results\":[{\"__metadata\":{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/BPContactPersons(guid'FA9F5515-A3F4-45EE-8337-007648258902')\",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/BPContactPersons(guid'FA9F5515-A3F4-45EE-8337-007648258902')\",\"type\":\"ARTEC.PYGW.BPContactPerson\"},\"BPCntPrsnGuid\":\"FA9F5515-A3F4-45EE-8337-007648258902\",\"LoginID\":\"E9B50177304E4BF1BC9CD067E5F9AB12\",\"BPGuid\":\"8ABA4CB5-ED86-4B94-90AA-507992E6C726\",\"BPType\":\"\",\"BPTypeDs\":\"\",\"Name1\":\"Subramani\",\"Name2\":\"Reddy\",\"DOB\":\"\\/Date(714182400000)\\/\",\"Mobile\":\"9999999999\",\"EmailID\":\"meghana.ramesh@arteriatech.com\",\"PanNo\":\"ESMPS9075R\",\"GenderID\":\"M\",\"GenderDesc\":\"\",\"SigningOrder\":\"00001\",\"PostalCode\":\"560066\",\"CreatedOn\":null,\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\",\"ChangedOn\":null,\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\",\"CpGuid\":\"\",\"CpType\":\"\",\"BPHeader\":{\"__deferred\":{\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/BPContactPersons(guid'FA9F5515-A3F4-45EE-8337-007648258902')/BPHeader\"}}}]}}}";
						}else if(payloadRequest.contains("POST BPContactPerson")){
							
						}else*/ 
						batchID = request.getHeader("Content-Type");
//							response.getWriter().println("loadMetadata batchID: "+batchID);
						HttpEntity requestEntity = null;
//							requestEntity = new ByteArrayEntity(payloadRequest.getBytes("UTF-8"));
						String filterValue = "", replacedPayload="";
	
						if(payloadRequest.contains("GET UserCustomers")){
//								payloadRequest = payloadRequest.replace("UserCustomers", "UserCustomers$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%"+aggregatorID+"%27");
								payloadRequest = payloadRequest.replace("UserCustomers", "UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27");
						}else if(payloadRequest.contains("POST BPHeader")){
							String lines[] = payloadRequest.split("\\r?\\n");
							String contentLengthSplit = "", reqContentLengthStr="", replaceContentLengthStr="", newRequestData="";
							int reqContentLength = 0, replaceContentLength = 0;;
							for(int i=0 ; i<=lines.length-1 ; i++){
								if(lines[i].contains("Content-Length")){
									contentLengthSplit = lines[i];
								}
							}
							reqContentLengthStr = contentLengthSplit.substring((contentLengthSplit.indexOf(": ")+2), contentLengthSplit.length());
							
							try{
								reqContentLength = Integer.parseInt(reqContentLengthStr);
							}catch (NumberFormatException e) {
								reqContentLength = 0;
							}
							
							String appendStr = "", dataString="";
							dataString = payloadRequest.substring(payloadRequest.indexOf("{"), payloadRequest.lastIndexOf("}"));
							appendStr = ",\"AggregatorID\":\""+aggregatorID+"\"";
							
							newRequestData = dataString+appendStr;
							replaceContentLength = (newRequestData.getBytes().length)+2;
							
							replacedPayload = payloadRequest.replace(dataString, dataString+appendStr);
							replacedPayload = replacedPayload.replace("Content-Length: "+reqContentLength, "Content-Length: "+replaceContentLength);
							payloadRequest = replacedPayload;
							
//								response.getWriter().println("payloadRequest: "+payloadRequest);
						}else if(payloadRequest.contains("GET UserRegistrations")){
							payloadRequest = payloadRequest.replace("UserRegistrations", "UserRegistrations?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27");
//							response.getWriter().println("payloadRequest: "+payloadRequest);
						}
//							response.getWriter().println("payloadRequest: "+payloadRequest);
						requestEntity = new StringEntity(payloadRequest);
						
						HttpPost postRequest = new HttpPost(serviceURL);
						postRequest.setHeader("Content-Type", batchID);
						postRequest.setHeader("Accept", "multipart/mixed");
//							postRequest.setHeader("X-CSRF-Token", csrfToken);
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
					}else{
						if(request.getPathInfo().equalsIgnoreCase("/SupplyChainFinances")){
							if(request.getMethod().equalsIgnoreCase("POST")){
								String testRunCase = "", errorResponseForSCF="";
								String errorResFormatForSCF = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\""
										+ ",\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
										+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},"
										+ "\"transactionid\":\"554CB8E9E259F12DBC9CD067E5F9AB12\",\"timestamp\":\"20190806131604.2890000\","
										+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and "
										+ "search for entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for "
										+ "error analysis (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\":"
										+ "[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
										+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
										+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
								
								payloadRequest = "";
								payloadRequest = getGetBody(request, response);
								// response.getWriter().println("SupplyChainFinances payloadRequest: "+payloadRequest);
//								dataPayload = payloadRequest;
								dataPayload = payloadRequest.replaceAll("\\\\", "");
								// response.getWriter().println("SupplyChainFinances dataPayload: "+dataPayload);
//								Gson inputJsonObject = new Gson();
//								inputJsonObject.fromJson(payloadRequest);
								JSONObject inputJsonObject = new JSONObject(payloadRequest);
								testRunCase = inputJsonObject.getString("TestRun").toUpperCase();
								String message = "";
								loginID = "";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
								
//								response.getWriter().println("testRunCase: "+testRunCase);
								if(inputJsonObject.has("debug")&& inputJsonObject.getString("debug").equalsIgnoreCase("true")){
									debug=true;
								}
								//debug=true;
								if(testRunCase !=  null && ! testRunCase.equalsIgnoreCase("E")){
									switch(testRunCase){
										case "O":
											//SCF Simulate(To get Offer)
											//response.getWriter().println("Inside O");
											if(! corpID.equalsIgnoreCase("E177")){
												message = commonUtils.validateLegalStatus(request, response, inputJsonObject, loginID, debug);
												if(debug)
													response.getWriter().println("loadMetadata validateLegalStatus message: "+message);
//												if(!message.equalsIgnoreCase("055") && !message.equalsIgnoreCase("118") && !message.equalsIgnoreCase("001")){
												if(!message.equalsIgnoreCase("055") && !message.equalsIgnoreCase("118") && !message.equalsIgnoreCase("001") && !message.equalsIgnoreCase("062")){
													message = "";
													
													message = commonUtils.validateSCF(request, response, inputJsonObject, loginID, debug);
													if(debug)
														response.getWriter().println("loadMetadata validateSCF message: "+message);
													
													if(message.equalsIgnoreCase("")){
														scfOfferResponseMap = commonUtils.simulateSCFOffers(inputJsonObject, request, response, corpID, debug);
//														scfOfferResponseMap = offerClient.callSCFOffersWebservice(request, response, corpID, inputJsonObject.getString("CPGUID"), debug);
														GsonBuilder gsonMapBuilder = new GsonBuilder();
														Gson gsonObject = gsonMapBuilder.create();
														String jsonObject = gsonObject.toJson(scfOfferResponseMap);
														JSONObject responseJsonObj = new JSONObject(jsonObject);
														response.setContentType("application/json");

														if(debug){
															response.getWriter().println("scfOfferResponseMap-responseJsonObj: "+responseJsonObj);
															response.getWriter().println("scfOfferResponseMap-EligibilityStatus: "+responseJsonObj.getString("EligibilityStatus"));
															response.getWriter().println("scfOfferResponseMap-StatusID: "+responseJsonObj.getString("StatusID"));
															response.getWriter().println("scfOfferResponseMap-OfferAmt: "+responseJsonObj.getString("OfferAmt"));
															response.getWriter().println("scfOfferResponseMap-Message: "+responseJsonObj.getString("Message"));
														}
														
														if(responseJsonObj.getString("EligibilityStatus").equalsIgnoreCase("999999")){
															//No Offer 056
															errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
															errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
															response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
															response.getWriter().println(errorResponseForSCF);
														}else{
															if(responseJsonObj.getString("StatusID").equalsIgnoreCase("000001")){
																if(commonUtils.ifOfferAmountGrtZero(responseJsonObj, debug)){
																	String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:"
																			+ "8000/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+inputJsonObject.getString("SCFGUID")+"')"
																			+ "\",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances"
																			+ "(guid'"+inputJsonObject.getString("SCFGUID")+"')\",\"type\":\"ARTEC.PYGW.SupplyChainFinance\"},"
																			+ "\"SCFGUID\":\""+inputJsonObject.getString("SCFGUID")+"\",\"CPGUID\":\""+inputJsonObject.getString("CPGUID")+"\",\"LoginID\""
																			+ ":\"\",\"CPTypeID\":\""+inputJsonObject.getString("CPTypeID")+"\",\"CPTypeDesc\":\"\",\"OfferAmt\":"
																			+ "\""+responseJsonObj.getString("OfferAmt")+"\",\"OfferTenure\":\""+responseJsonObj.getString("OfferTenure")+"\",\"Rate\":\""+responseJsonObj.getString("Rate")+"\",\"AccountNo\":\""+responseJsonObj.getString("AccountNo")+"\",\"NoOfChequeReturns\":"
																			+ "\""+responseJsonObj.getString("NoOfChequeReturns")+"\",\"PaymentDelayDays12Months\":\""+responseJsonObj.getString("PaymentDelayDays12Months")+"\",\"BusinessVintageOfDealer\":\""+responseJsonObj.getString("BusinessVintageOfDealer")+"\","
																			+ "\"PurchasesOf12Months\":\""+responseJsonObj.getString("PurchasesOf12Months")+"\",\"DealersOverallScoreByCorp\":\""+responseJsonObj.getString("DealersOverallScoreByCorp")+"\",\"CorpRating\":\""+responseJsonObj.getString("CorpRating")+"\","
																			+ "\"DealerVendorFlag\":\""+responseJsonObj.getString("DealerVendorFlag")+"\",\"ConstitutionType\":\""+responseJsonObj.getString("ConstitutionType")+"\",\"MaxLimitPerCorp\":\""+responseJsonObj.getString("MaxLimitPerCorp")+"\",\"salesOf12Months"
																			+ "\":\""+responseJsonObj.getString("salesOf12Months")+"\",\"Currency\":\""+responseJsonObj.getString("Currency")+"\",\"StatusID\":\""+responseJsonObj.getString("StatusID")+"\",\"StatusIDDesc\":\""+responseJsonObj.getString("Message")+"\",\"TestRun\":\""+inputJsonObject.getString("TestRun")+"\","
																			+ "\"TotalBalance\":\""+responseJsonObj.getString("TotalBalance")+"\",\"TenorIndays\":\""+responseJsonObj.getString("TenorIndays")+"\",\"PeakLimit\":\""+responseJsonObj.getString("PeakLimit")+"\",\"PeaklmtTenorindays\":\""+responseJsonObj.getString("PeaklmtTenorindays")+"\","
																			+ "\"OverdueBeyondCure\":\""+responseJsonObj.getString("OverdueBeyondCure")+"\",\"AsOnDate\":\""+responseJsonObj.getString("AsOnDate")+"\",\"ReportDate\":\""+responseJsonObj.getString("ReportDate")+"\",\"MCLR6Rate\":\""+responseJsonObj.getString("MCLR6Rate")+"\","
																			+ "\"InterestRateSpread\":\""+responseJsonObj.getString("InterestRateSpread")+"\",\"TenorOfPayment\":\""+responseJsonObj.getString("TenorOfPayment")+"\",\"ADDLNPRDINTRateSP\":"
																			+ "\""+responseJsonObj.getString("ADDLNPRDINTRateSP")+"\",\"AddlnTenorOfPymt\":\""+responseJsonObj.getString("AddlnTenorOfPymt")+"\",\"DefIntSpread\":\""+responseJsonObj.getString("DefIntSpread")+"\",\"ProcessingFee"
																			+ "\":\""+responseJsonObj.getString("ProcessingFee")+"\",\"OutstandingAmount\":\""+responseJsonObj.getString("OutstandingAmount")+"\",\"CreatedOn\":\"\",\"CreatedBy\":\"\",\"CreatedAt\":"
																			+ "\"\",\"ChangedOn\":\"\",\"ChangedBy\":\"\",\"ChangedAt\":\"\",\"EContractID\""
																			+ ":\""+responseJsonObj.getString("EContractID")+"\",\"ECustomerID\":\""+responseJsonObj.getString("ECustomerID")+"\",\"ApplicationNo\":\""+responseJsonObj.getString("ApplicationNo")+"\",\"CallBackStatus\":\""+responseJsonObj.getString("CallBackStatus")+"\",\"ECompleteTime\""
																			+ ":\""+responseJsonObj.getString("ECompleteTime")+"\",\"ECompleteDate\":\""+responseJsonObj.getString("ECompleteDate")+"\",\"ApplicantID\":\""+responseJsonObj.getString("ApplicantID")+"\",\"IsOverdue\":\""+responseJsonObj.getString("IsOverdue")+"\",\"BlockFinancing\""
																			+ ":\""+responseJsonObj.getString("BlockFinancing")+"\",\"OverdueBy\":\""+responseJsonObj.getString("OverdueBy")+"\",\"BlockOrder\":\""+responseJsonObj.getString("BlockOrder")+"\",\"BlockingReasonID\":\""+responseJsonObj.getString("BlockingReasonID")+"\",\"BlockingReasonDesc\""
																			+ ":\""+responseJsonObj.getString("BlockingReasonDesc")+"\",\"DDBActive\":\""+responseJsonObj.getString("DDBActive")+"\",\"ProcessFeePerc\":\""+responseJsonObj.getString("ProcessFeePerc")+"\",\"SupplyChainFinanceTxns\":\""+responseJsonObj.getString("SupplyChainFinanceTxns")+"\"}}";
																	response.getWriter().println(successResponse);
																}else{
																	//No Offer 056
																	errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
																	errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
																	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																	response.getWriter().println(errorResponseForSCF);
																}
															}else{
																if(responseJsonObj.getString("StatusID").equalsIgnoreCase("000002")){
																	if(responseJsonObj.getString("Message").equalsIgnoreCase("Exception raised without any specific error")){
																		errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseJsonObj.getString("Message"));
																		errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "022");
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		response.getWriter().println(errorResponseForSCF);
																	}else{
																		errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseJsonObj.getString("Message"));
																		errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "059");
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		response.getWriter().println(errorResponseForSCF);
																	}
																}else{
																	errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseJsonObj.getString("Message"));
																	errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "043");
																	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																	response.getWriter().println(errorResponseForSCF);
																}
															}
														}
													}else{
														errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
														errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
														response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
														response.getWriter().println(errorResponseForSCF);
													}
												}else{
													errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
													errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													response.getWriter().println(errorResponseForSCF);
												}
											}else{
												response.setContentType("application/json");
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//												response.getWriter().println(jsonObject);
												String errorResponse = "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:"
														+ "8000/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+inputJsonObject.getString("SCFGUID")+"')"
														+ "\",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances"
														+ "(guid'"+inputJsonObject.getString("SCFGUID")+"')\",\"type\":\"ARTEC.PYGW.SupplyChainFinance\"},"
														+ "\"SCFGUID\":\""+inputJsonObject.getString("SCFGUID")+"\",\"CPGUID\":\""+inputJsonObject.getString("CPGUID")+"\",\"LoginID\""
														+ ":\"\",\"CPTypeID\":\""+inputJsonObject.getString("CPTypeID")+"\",\"CPTypeDesc\":\"\",\"OfferAmt\":"
														+ "\"\",\"OfferTenure\":\"\",\"Rate\":\"\",\"AccountNo\":\"\",\"NoOfChequeReturns\":"
														+ "\"\",\"PaymentDelayDays12Months\":\"\",\"BusinessVintageOfDealer\":\"\","
														+ "\"PurchasesOf12Months\":\"\",\"DealersOverallScoreByCorp\":\"\",\"CorpRating\":\"\","
														+ "\"DealerVendorFlag\":\"\",\"ConstitutionType\":\"\",\"MaxLimitPerCorp\":\"\",\"salesOf12Months"
														+ "\":\"\",\"Currency\":\"\",\"StatusID\":\"000002\",\"StatusIDDesc\":\""+properties.getProperty(corpID)+"\",\"TestRun\":\""+inputJsonObject.getString("TestRun")+"\","
														+ "\"TotalBalance\":\"\",\"TenorIndays\":\"\",\"PeakLimit\":\"\",\"PeaklmtTenorindays\":\"\","
														+ "\"OverdueBeyondCure\":\"\",\"AsOnDate\":\"\",\"ReportDate\":\"\",\"MCLR6Rate\":\"\","
														+ "\"InterestRateSpread\":\"\",\"TenorOfPayment\":\"\",\"ADDLNPRDINTRateSP\":"
														+ "\"\",\"AddlnTenorOfPymt\":\"\",\"DefIntSpread\":\"\",\"ProcessingFee"
														+ "\":\"\",\"OutstandingAmount\":\"\",\"CreatedOn\":\"\",\"CreatedBy\":\"\",\"CreatedAt\":"
														+ "\"\",\"ChangedOn\":\"\",\"ChangedBy\":\"\",\"ChangedAt\":\"\",\"EContractID\""
														+ ":\"\",\"ECustomerID\":\"\",\"ApplicationNo\":\"\",\"CallBackStatus\":\"\",\"ECompleteTime\""
														+ ":\"\",\"ECompleteDate\":\"\",\"ApplicantID\":\"\",\"IsOverdue\":\"\",\"BlockFinancing\""
														+ ":\"\",\"OverdueBy\":\"\",\"BlockOrder\":\"\",\"BlockingReasonID\":\"\",\"BlockingReasonDesc\""
														+ ":\"\",\"DDBActive\":\"\",\"ProcessFeePerc\":\"\",\"SupplyChainFinanceTxns\":\"\"}}";
												response.getWriter().println(errorResponse);
											}
										break;
										
										case "A":
											//SCF Apply(To get Offer & Apply)
//											response.getWriter().println("Inside A");
											// debug = true;
											Map<String,String> scfOfferResponseMapSCFA = new HashMap<String,String>();
											JsonObject scfApplyResponse = new JsonObject();
											
											message = commonUtils.validateLegalStatus(request, response, inputJsonObject, loginID, debug);
											
											if(debug)
												response.getWriter().println("A validateLegalStatus message: "+message);
											if(!message.equalsIgnoreCase("055") && !message.equalsIgnoreCase("118") && !message.equalsIgnoreCase("001") && !message.equalsIgnoreCase("062")){
												message = "";
												
												message = commonUtils.validateSCF(request, response, inputJsonObject, loginID, debug);
												
												if(debug)
													response.getWriter().println("loadMetadata validateSCF message: "+message);
												
												if(message.equalsIgnoreCase("")){
//													commonUtils.simulateSCFOffers(inputJsonObject, request, response, inputJsonObject.getString("CPGUID"), debug);
													scfOfferResponseMapSCFA = commonUtils.simulateSCFOffers(inputJsonObject, request, response, corpID, debug);
													
													GsonBuilder gsonMapBuilder = new GsonBuilder();
													Gson gsonObject = gsonMapBuilder.create();
													String scfOfferJsonObject = gsonObject.toJson(scfOfferResponseMapSCFA);
													JSONObject responseScfOfferJsonObj = new JSONObject(scfOfferJsonObject);
													
													if(debug){
														response.getWriter().println("pygw-responseScfOffer-A: "+responseScfOfferJsonObj);
														response.getWriter().println("responseScfOfferJsonObj-EligStatus: "+responseScfOfferJsonObj.getString("EligibilityStatus"));
														response.getWriter().println("responseScfOfferJsonObj-ErrorCode: "+responseScfOfferJsonObj.getString("ErrorCode"));
														response.getWriter().println("responseScfOfferJsonObj-Message: "+responseScfOfferJsonObj.getString("Message"));
														response.getWriter().println("responseScfOfferJsonObj-StatusID: "+responseScfOfferJsonObj.getString("StatusID"));
														response.getWriter().println("responseScfOfferJsonObj-OfferAmt: "+responseScfOfferJsonObj.getString("OfferAmt"));
													}
													
													if(responseScfOfferJsonObj.getString("EligibilityStatus").equalsIgnoreCase("999999")){
														//No Offer 056
														errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
														errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
														response.getWriter().println(errorResponseForSCF);
													}else{
														if(responseScfOfferJsonObj.getString("StatusID").equalsIgnoreCase("000001")){
															if(commonUtils.ifOfferAmountGrtZero(responseScfOfferJsonObj, debug)){
																message = commonUtils.getContactPersonDetails(request, response, loginID, responseScfOfferJsonObj, inputJsonObject, debug);
																
																if(null != message && message.trim().length() > 0 && message.trim().equalsIgnoreCase("062")){
																	errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
																	errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
																	response.getWriter().println(errorResponseForSCF);
																}else{
																	scfApplyResponse = commonUtils.scfApply(response, request, inputJsonObject.getString("CPGUID"), aggregatorID, loginID, oDataURL, inputJsonObject, properties, scfOfferResponseMapSCFA, debug);
																	
																	if(debug)
																		response.getWriter().println("pygw-scfApplyResponse-A2: "+scfApplyResponse);
																	
																	JsonObject scfApplyResults = scfApplyResponse.get("d").getAsJsonObject();
																	
																	if(debug){
																		response.getWriter().println("scfInsertObj-results: "+scfApplyResults);
																		response.getWriter().println("scfInsertObj-EligibilityStatus: "+scfApplyResults.get("EligibilityStatus").getAsString());
																	}
																	
																	response.setContentType("application/json");
																	if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")
																			|| scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000002")){
																		
																		if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
																			String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z93y6qtneb.dispatcher.ha"
																					+ "na.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances"
																					+ "(guid'"+scfApplyResults.get("ID").getAsString()+"')\",\"uri\":\"https://flpnwc-z93y6qt"
																					+ "neb.dispatcher.hana.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ART"
																					+ "EC/PYGW/SupplyChainFinances(guid'"+scfApplyResults.get("ID").getAsString()+"')\",\"type\":\"ART"
																					+ "EC.PYGW.SupplyChainFinance\"}"
																					+ ",\"SCFGUID\":\""+scfApplyResults.get("ID").getAsString()+"\""
																					+ ",\"CPGUID\":\""+scfApplyResults.get("CPGUID").getAsString()+"\""
																					+ ",\"LoginID\":\"\""
																					+ ",\"CPTypeID\":\""+scfApplyResults.get("CPTypeID").getAsString()+"\""
																					+ ",\"CPTypeDesc\":\"\""
																					+ ",\"OfferAmt\":\""+responseScfOfferJsonObj.getString("OfferAmt")+"\""
																					+ ",\"OfferTenure\":\""+responseScfOfferJsonObj.getString("OfferTenure")+"\""
																					+ ",\"Rate\":\""+responseScfOfferJsonObj.getString("Rate")+"\""
																					+ ",\"AccountNo\":\"\""
																					+ ",\"NoOfChequeReturns\":\""+responseScfOfferJsonObj.getString("NoOfChequeReturns")+"\""
																					+ ",\"PaymentDelayDays12Months\":\""+responseScfOfferJsonObj.getString("PaymentDelayDays12Months")+"\""
																					+ ",\"BusinessVintageOfDealer\":\""+responseScfOfferJsonObj.getString("BusinessVintageOfDealer")+"\""
																					+ ",\"PurchasesOf12Months\":\""+responseScfOfferJsonObj.getString("PurchasesOf12Months")+"\""
																					+ ",\"DealersOverallScoreByCorp\":\""+responseScfOfferJsonObj.getString("DealersOverallScoreByCorp")+"\""
																					+ ",\"CorpRating\":\""+responseScfOfferJsonObj.getString("CorpRating")+"\""
																					+ ",\"DealerVendorFlag\":\""+responseScfOfferJsonObj.getString("DealerVendorFlag")+"\""
																					+ ",\"ConstitutionType\":\""+responseScfOfferJsonObj.getString("ConstitutionType")+"\""
																					+ ",\"MaxLimitPerCorp\":\""+responseScfOfferJsonObj.getString("MaxLimitPerCorp")+"\""
																					+ ",\"salesOf12Months\":\""+responseScfOfferJsonObj.getString("salesOf12Months")+"\""
																					+ ",\"Currency\":\"INR\""
																					+ ",\"StatusID\":\""+responseScfOfferJsonObj.getString("StatusID")+"\""
																					+ ",\"StatusIDDesc\":\""+responseScfOfferJsonObj.getString("Message")+"\""
																					+ ",\"TestRun\":\"A\""
																					+ ",\"TotalBalance\":\""+responseScfOfferJsonObj.getString("TotalBalance")+"\""
																					+ ",\"TenorIndays\":\""+responseScfOfferJsonObj.getString("TenorIndays")+"\""
																					+ ",\"PeakLimit\":\""+responseScfOfferJsonObj.getString("PeakLimit")+"\""
																					+ ",\"PeaklmtTenorindays\":\""+responseScfOfferJsonObj.getString("PeaklmtTenorindays")+"\""
																					+ ",\"OverdueBeyondCure\":\""+responseScfOfferJsonObj.getString("OverdueBeyondCure")+"\""
																					+ ",\"AsOnDate\":null"
																					+ ",\"ReportDate\":null"
																					+ ",\"MCLR6Rate\":\""+responseScfOfferJsonObj.getString("MCLR6Rate")+"\""
																					+ ",\"InterestRateSpread\":\""+responseScfOfferJsonObj.getString("InterestRateSpread")+"\""
																					+ ",\"TenorOfPayment\":\""+responseScfOfferJsonObj.getString("TenorOfPayment")+"\""
																					+ ",\"ADDLNPRDINTRateSP\":\""+responseScfOfferJsonObj.getString("ADDLNPRDINTRateSP")+"\""
																					+ ",\"AddlnTenorOfPymt\":\""+responseScfOfferJsonObj.getString("AddlnTenorOfPymt")+"\""
																					+ ",\"DefIntSpread\":\""+responseScfOfferJsonObj.getString("DefIntSpread")+"\""
																					+ ",\"ProcessingFee\":\""+responseScfOfferJsonObj.getString("ProcessingFee")+"\""
																					+ ",\"OutstandingAmount\":\""+responseScfOfferJsonObj.getString("OutstandingAmount")+"\""
																					+ ",\"CreatedOn\":null,\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\""
																					+ ",\"ChangedOn\":null,\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\""
																					+ ",\"EContractID\":\"\",\"ECustomerID\":\"\""
																					+ ",\"ApplicationNo\":\""+scfApplyResults.get("ApplicationNo").getAsString()+"\""
																					+ ",\"CallBackStatus\":\"\",\"ECompleteTime\":\"PT00H00M00S\",\"ECompleteDate\":null"
																					+ ",\"ApplicantID\":\"\""
																					+ ",\"IsOverdue\":\""+responseScfOfferJsonObj.getString("IsOverdue")+"\""
																					+ ",\"BlockFinancing\":\"\""
																					+ ",\"OverdueBy\":\""+responseScfOfferJsonObj.getString("OverdueBy")+"\""
																					+ ",\"BlockOrder\":\"\",\"BlockingReasonID\":\"\",\"BlockingReasonDesc\":\"\""
																					+ ",\"DDBActive\":\"\""
																					+ ",\"ProcessFeePerc\":\""+responseScfOfferJsonObj.getString("ProcessFeePerc")+"\",\"SupplyChainFinanceTxns\":null}}";
//																			response.getOutputStream().print(successResponse);
																			response.getWriter().println(successResponse);
																		}else{
//																			change here
																			if(debug)
																				response.getWriter().println("scfApplyResults.Eligibility Status Other than 000001: "+scfApplyResults);
//																			errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", "Customer is not eligible (Ref: "+scfApplyResults.get("ApplicationNo").getAsString()+" / ErrorCode: "+scfApplyResults.get("ErrorCode").getAsString()+")");
																			
																			if(!scfApplyResults.get("ErrorCode").isJsonNull() && (scfApplyResults.get("ErrorCode").getAsString().trim().equalsIgnoreCase("115") || scfApplyResults.get("ErrorCode").getAsString().trim().equalsIgnoreCase("-599"))){
																				String detailedMsg="";
																				errorResFormatForSCF = "{\"error\":{\"code\":\"/ARTEC/PY/109\",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_CODE\"},\"innererror\""
																						+ ":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\""
																						+ ":\"0001\"},\"transactionid\":\"9870C7EAEDC0F1BC80FBD067E5F9AB12\",\"timestamp\":\"20200716142830.9230000\""
																						+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for "
																						+ "entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (htt"
																						+ "ps://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\":[{\"code\":\"/ARTEC/PY/109\",\"message\":\"ERROR_CODE\""
																						+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/ARTEC/PY/059\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																						+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"seve"
																						+ "rity\":\"error\",\"target\":\"\"}]}}}";
//																				errorResponseForSCF = errorResFormatForSCF.replaceAll("INSERT_LEAD_ID_HERE", scfApplyResults.get("LeadID").getAsString());
//																				errorResponseForSCF = errorResFormatForSCF.replaceAll("INSERT_MSG_HERE", scfApplyResults.get("ErrorMessage").getAsString());
																				
																				errorResFormatForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", scfApplyResults.get("ErrorMsg").getAsString());
																				errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_CODE", scfApplyResults.get("ApplicationNo").getAsString());
																				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																				response.getWriter().println(errorResponseForSCF);
																			}else{
																				errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", scfApplyResults.get("ApplicationNo").getAsString());
																				errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "109");
																				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																				response.getWriter().println(errorResponseForSCF);
																			}
																		}
																	} else if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000000")){
																		if(debug){
																			response.getWriter().println("scfApplyResults.Eligibility Status 000000: "+scfApplyResults);
																			response.getWriter().println("scfApplyResults.Eligibility Status 000000LeadID: "+scfApplyResults.get("LeadID").getAsString());
																			response.getWriter().println("scfApplyResults.Eligibility Status 000000ErrorMessage: "+scfApplyResults.get("ErrorMessage").getAsString());
																		}
																		
																		if(scfApplyResults.get("ErrorCode").getAsString().equalsIgnoreCase("109")){
//																			change here
																			
																			errorResFormatForSCF = "{\"error\":{\"code\":\"/ARTEC/PY/109\",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_CODE\"},\"innererror\""
																					+ ":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\""
																					+ ":\"0001\"},\"transactionid\":\"9870C7EAEDC0F1BC80FBD067E5F9AB12\",\"timestamp\":\"20200716142830.9230000\""
																					+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for "
																					+ "entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (htt"
																					+ "ps://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\":[{\"code\":\"/ARTEC/PY/109\",\"message\":\"ERROR_CODE\""
																					+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/ARTEC/PY/059\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																					+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"seve"
																					+ "rity\":\"error\",\"target\":\"\"}]}}}";
//																			errorResponseForSCF = errorResFormatForSCF.replaceAll("INSERT_LEAD_ID_HERE", scfApplyResults.get("LeadID").getAsString());
//																			errorResponseForSCF = errorResFormatForSCF.replaceAll("INSERT_MSG_HERE", scfApplyResults.get("ErrorMessage").getAsString());
																			
																			errorResFormatForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", scfApplyResults.get("ErrorMessage").getAsString());
																			errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_CODE", scfApplyResults.get("LeadID").getAsString());
																			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																			response.getWriter().println(errorResponseForSCF);
																		}else if(scfApplyResults.get("ErrorCode").getAsString().equalsIgnoreCase("1091")){
//																			change needed here
																			errorResFormatForSCF = "{\"error\":{\"code\":\"/ARTEC/PY/109\",\"message\":{\"lang\":\"en\",\"value\":\"ERROR_CODE\"},\"innererror\""
																					+ ":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\""
																					+ ":\"0001\"},\"transactionid\":\"9870C7EAEDC0F1BC80FBD067E5F9AB12\",\"timestamp\":\"20200716142830.9230000\""
																					+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for "
																					+ "entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (htt"
																					+ "ps://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\":[{\"code\":\"/ARTEC/PY/109\",\"message\":\"ERROR_CODE\""
																					+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/ARTEC/PY/059\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																					+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\",\"propertyref\":\"\",\"seve"
																					+ "rity\":\"error\",\"target\":\"\"}]}}}";
																			
																			errorResFormatForSCF = errorResFormatForSCF.replaceAll("ERROR_CODE", scfApplyResults.get("LeadID").getAsString());
																			String encodedResStr = Matcher.quoteReplacement(scfApplyResults.get("ErrorMessage").getAsString());
																			
																			errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", encodedResStr);
																			
																			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																			response.getWriter().println(errorResponseForSCF);
																		}else{
																			String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/"+scfApplyResults.get("ErrorCode").getAsString()+"\",\"message\":{\"lang\""
																					+ ":\"en\",\"value\":\""+scfApplyResults.get("ErrorMessage").getAsString()+"\"}"
																					+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\""
																					+ ",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},\"transactionid\""
																					+ ":\"959AC4E9D2DBF1E4BC9CD067E5F9AB12\",\"timestamp\":\"20190822050630.0500000\""
																					+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG "
																					+ "on SAP Gateway hub system and search for entries with the timestamp above for more details\""
																					+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis"
																					+ " (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\""
																					+ ":[{\"code\":\"/ARTEC/PY/"+scfApplyResults.get("ErrorCode").getAsString()+"\",\"message\":\""+scfApplyResults.get("ErrorMessage").getAsString()+""
																					+ "\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																					+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred"
																					+ ".\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
//																			response.s
																			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																			response.getWriter().println(errorResponse);
																		}
																	} else {
																		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/113\",\"message\":{\"lang\""
																				+ ":\"en\",\"value\":\"Business Type in CF registration does not match with bank records\"}"
																				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\""
																				+ ",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},\"transactionid\""
																				+ ":\"959AC4E9D2DBF1E4BC9CD067E5F9AB12\",\"timestamp\":\"20190822050630.0500000\""
																				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG "
																				+ "on SAP Gateway hub system and search for entries with the timestamp above for more details\""
																				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis"
																				+ " (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\""
																				+ ":[{\"code\":\"/ARTEC/PY/113\",\"message\":\"Business Type in CF registration does "
																				+ "not match with bank records\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																				+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred"
																				+ ".\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		response.getWriter().println(errorResponse);
																	}
																}
															}else{
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}
														}else{
															if(responseScfOfferJsonObj.getString("StatusID").equalsIgnoreCase("000002")){
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseScfOfferJsonObj.getString("Message"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "059");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}else{
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseScfOfferJsonObj.getString("Message"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "043");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}
														}
													}
												}else{
													errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
													errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													response.getWriter().println(errorResponseForSCF);
												}
											}else{
												errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
												errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												response.getWriter().println(errorResponseForSCF);
											}
											
//											scfOfferResponseMapSCFA = commonUtils.simulateSCFOffers(inputJsonObject, request, response, corpID, debug);
//											response.getWriter().println(jsonObject);
											
										break;
										
										default :
											//SCF Create(To get Offer,apply,CAL Prop,Cal Partn)
//											response.getWriter().println("Inside A");
											//debug=true;
											String bpLegalStatus = "", applicantID="";
											Map<String,String> scfOfferResponseMapSCFC = new HashMap<String,String>();
											JsonObject scfCreateResponse = new JsonObject();
											message = commonUtils.validateLegalStatus(request, response, inputJsonObject, loginID, debug);
											
											if(debug)
												response.getWriter().println("loadMetadata validateLegalStatus message: "+message);
											
											if(debug)
												response.getWriter().println("loadMetadata validateLegalStatus message: "+message);
//											if(!message.equalsIgnoreCase("055") && !message.equalsIgnoreCase("118") && !message.equalsIgnoreCase("001")){
											if(!message.equalsIgnoreCase("055") && !message.equalsIgnoreCase("118") && !message.equalsIgnoreCase("001") && !message.equalsIgnoreCase("062")){
												message = "";
												
												message = commonUtils.validateSCF(request, response, inputJsonObject, loginID, debug);
												
												if(debug)
													response.getWriter().println("loadMetadata validateSCF message: "+message);
												
												if(message.equalsIgnoreCase("")){
//													commonUtils.simulateSCFOffers(inputJsonObject, request, response, inputJsonObject.getString("CPGUID"), debug);
													scfOfferResponseMapSCFC = commonUtils.simulateSCFOffers(inputJsonObject, request, response, corpID, debug);
													
													GsonBuilder gsonMapBuilder = new GsonBuilder();
													Gson gsonObject = gsonMapBuilder.create();
													String scfOfferJsonObject = gsonObject.toJson(scfOfferResponseMapSCFC);
													JSONObject responseScfOfferJsonObj = new JSONObject(scfOfferJsonObject);
													
													if(debug){
														response.getWriter().println("pygw-responseScfOffer-A: "+responseScfOfferJsonObj);
														response.getWriter().println("responseScfOfferJsonObj-EligStatus"+responseScfOfferJsonObj.getString("EligibilityStatus"));
														response.getWriter().println("responseScfOfferJsonObj-ErrorCode"+responseScfOfferJsonObj.getString("ErrorCode"));
														response.getWriter().println("responseScfOfferJsonObj-Message"+responseScfOfferJsonObj.getString("Message"));
														response.getWriter().println("responseScfOfferJsonObj-StatusID"+responseScfOfferJsonObj.getString("StatusID"));
														response.getWriter().println("responseScfOfferJsonObj-OfferAmt"+responseScfOfferJsonObj.getString("OfferAmt"));
													}
													
													if(responseScfOfferJsonObj.getString("EligibilityStatus").equalsIgnoreCase("999999")){
														//No Offer 056
														errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
														errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
														response.getWriter().println(errorResponseForSCF);
													}else{
														if(responseScfOfferJsonObj.getString("StatusID").equalsIgnoreCase("000001")){
															if(commonUtils.ifOfferAmountGrtZero(responseScfOfferJsonObj, debug)){
																message = commonUtils.getContactPersonDetails(request, response, loginID, responseScfOfferJsonObj, inputJsonObject, debug);
																
																if(null != message && message.trim().length() > 0 && message.trim().equalsIgnoreCase("062")){
																	errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
																	errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
																	response.getWriter().println(errorResponseForSCF);
																}else{
																	scfApplyResponse = commonUtils.scfApply(response, request, inputJsonObject.getString("CPGUID"), aggregatorID, loginID, oDataURL, inputJsonObject, properties, scfOfferResponseMapSCFC, debug);
																	
																	if(debug)
																		response.getWriter().println("pygw-scfApplyResponse-A1: "+scfApplyResponse);
																	
																	JsonObject scfApplyResults = scfApplyResponse.get("d").getAsJsonObject();
																	
																	if(debug){
																		response.getWriter().println("scfInsertObj-results: "+scfApplyResults);
																		response.getWriter().println("scfInsertObj-EligibilityStatus: "+scfApplyResults.get("EligibilityStatus").getAsString());
																	}
																	
																	response.setContentType("application/json");
																	if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")
																			|| scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000002")){
																		
																		if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
																			bpLegalStatus = commonUtils.getBPCPDetails(request, response, inputJsonObject, loginID, "LegalStatus", debug);
																			if(debug)
																				response.getWriter().println("loadMetadata bpLegalStatus: "+bpLegalStatus);
																			scfCreateResponse = null;
																			scfCreateResponse = new JsonObject();
																			if(bpLegalStatus.equalsIgnoreCase("03")){
																				//Proprietor
																				scfCreateResponse = commonUtils.callProprietor(request, response, inputJsonObject, aggregatorID, loginID, oDataURL, scfApplyResults, scfOfferResponseMapSCFC, bpLegalStatus, properties, debug);
																																						
																				if(debug)
																					response.getWriter().println("pygw.callProprietor-scfCreateResponse: "+scfCreateResponse);
																				
																				JsonObject scfCreateObj = scfCreateResponse.get("d").getAsJsonObject();
																				if(debug)
																					response.getWriter().println("pygw.scfCreateObj: "+scfCreateObj);
																				
																				if(scfCreateObj.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
																					String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+scfCreateObj.get("ID").getAsString()+"')\",\"uri\":\"https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+scfCreateObj.get("ID").getAsString()+"')\",\"type\":\"ARTEC.PYGW.SupplyChainFinance\"}"
																							+ ",\"SCFGUID\":\""+scfCreateObj.get("ID").getAsString()+"\""
																							+ ",\"CPGUID\":\""+scfCreateObj.get("CPGUID").getAsString()+"\""
																							+ ",\"LoginID\":\""+loginID+"\""
																							+ ",\"CPTypeID\":\""+scfCreateObj.get("CPTypeID").getAsString()+"\""
																							+ ",\"CPTypeDesc\":\"\""
																							+ ",\"OfferAmt\":\""+scfCreateObj.get("OfferAmt").getAsString()+"\""
																							+ ",\"OfferTenure\":\""+scfCreateObj.get("OfferTenure").getAsString()+"\""
																							+ ",\"Rate\":\""+scfCreateObj.get("Rate").getAsString()+"\""
																							+ ",\"AccountNo\":\"\""
																							+ ",\"NoOfChequeReturns\":\""+scfCreateObj.get("NoOfChequeReturns").getAsString()+"\""
																							+ ",\"PaymentDelayDays12Months\":\""+scfCreateObj.get("PaymentDelayDays12Months").getAsString()+"\""
																							+ ",\"BusinessVintageOfDealer\":\""+scfCreateObj.get("BusinessVintageOfDealer").getAsString()+"\""
																							+ ",\"PurchasesOf12Months\":\""+scfCreateObj.get("PurchasesOf12Months").getAsString()+"\""
																							+ ",\"DealersOverallScoreByCorp\":\""+scfCreateObj.get("DealersOverallScoreByCorp").getAsString()+"\""
																							+ ",\"CorpRating\":\""+scfCreateObj.get("CorpRating").getAsString()+"\""
																							+ ",\"DealerVendorFlag\":\"\""
																							+ ",\"ConstitutionType\":\""+scfCreateObj.get("ConstitutionType").getAsString()+"\""
																							+ ",\"MaxLimitPerCorp\":\""+scfCreateObj.get("MaxLimitPerCorp").getAsString()+"\""
																							+ ",\"salesOf12Months\":\""+scfCreateObj.get("salesOf12Months").getAsString()+"\""
																							+ ",\"Currency\":\""+scfCreateObj.get("Currency").getAsString()+"\""
																							+ ",\"StatusID\":\""+scfCreateObj.get("StatusID").getAsString()+"\""
																							+ ",\"StatusIDDesc\":\"\""
																							+ ",\"TestRun\":\"\""
																							+ ",\"TotalBalance\":\"0.00\""
																							+ ",\"TenorIndays\":\"\""
																							+ ",\"PeakLimit\":\"0.00\""
																							+ ",\"PeaklmtTenorindays\":\"\""
																							+ ",\"OverdueBeyondCure\":\"0.00\""
																							+ ",\"AsOnDate\":null"
																							+ ",\"ReportDate\":null"
																							+ ",\"MCLR6Rate\":\"8.15\""
																							+ ",\"InterestRateSpread\":\""+scfCreateObj.get("InterestRateSpread").getAsString()+"\""
																							+ ",\"TenorOfPayment\":\""+scfCreateObj.get("TenorOfPayment").getAsString()+"\""
																							+ ",\"ADDLNPRDINTRateSP\":\""+scfCreateObj.get("ADDLNPRDINTRateSP").getAsString()+"\""
																							+ ",\"AddlnTenorOfPymt\":\""+scfCreateObj.get("AddlnTenorOfPymt").getAsString()+"\""
																							+ ",\"DefIntSpread\":\""+scfCreateObj.get("DefIntSpread").getAsString()+"\""
																							+ ",\"ProcessingFee\":\""+scfCreateObj.get("ProcessingFee").getAsString()+"\""
																							+ ",\"OutstandingAmount\":\"\""
																							+ ",\"CreatedOn\":null"
																							+ ",\"CreatedBy\":\"\""
																							+ ",\"CreatedAt\":\"\""
																							+ ",\"ChangedOn\":null"
																							+ ",\"ChangedBy\":\"\""
																							+ ",\"ChangedAt\":\"\""
																							+ ",\"EContractID\":\""+scfCreateObj.get("EContractID").getAsString()+"\""
																							+ ",\"ECustomerID\":\""+scfCreateObj.get("ECustomerID").getAsString()+"\""
																							+ ",\"ApplicationNo\":\""+scfCreateObj.get("ApplicationNo").getAsString()+"\""
																							+ ",\"CallBackStatus\":\"\""
																							+ ",\"ECompleteTime\":\"PT00H00M00S\""
																							+ ",\"ECompleteDate\":null"
																							+ ",\"ApplicantID\":\""+scfCreateObj.get("ApplicantID").getAsString()+"\""
																							+ ",\"IsOverdue\":\"\""
																							+ ",\"BlockFinancing\":\"\""
																							+ ",\"OverdueBy\":\"\""
																							+ ",\"BlockOrder\":\"\""
																							+ ",\"BlockingReasonID\":\"\""
																							+ ",\"BlockingReasonDesc\":\"\""
																							+ ",\"DDBActive\":\"\""
																							+ ",\"ProcessFeePerc\":\""+scfCreateObj.get("ProcessFeePerc").getAsString()+"\""
																							+ ",\"SupplyChainFinanceTxns\":null}}";
//																					response.getOutputStream().print(successResponse);
																					response.getWriter().println(successResponse);
																				}else{
																					//Set Error Response
																					errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", scfCreateResponse.get("d").getAsJsonObject().get("ErrorMessage").getAsString());
																					errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", scfCreateResponse.get("d").getAsJsonObject().get("ErrorCode").getAsString());
																					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																					response.getWriter().println(errorResponseForSCF);
																				}
																			}else if(bpLegalStatus.equalsIgnoreCase("04")){
																				//Partner
																				scfCreateResponse = commonUtils.callPartnership(request, response, inputJsonObject, aggregatorID, loginID, oDataURL, scfApplyResults, scfOfferResponseMapSCFC, bpLegalStatus, properties, debug);
																				if(debug)
																					response.getWriter().println("pygw.callPartnership-scfCreateResponse: "+scfCreateResponse);
																				
																				JsonObject scfCreateObj = scfCreateResponse.get("d").getAsJsonObject();
																				if(debug)
																					response.getWriter().println("pygw.scfCreateObj: "+scfCreateObj);
																				
//																				if(scfCreateResponse.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
																				if(scfCreateObj.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
																					String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+scfCreateObj.get("ID").getAsString()+"')\",\"uri\":\"https://flpnwc-z7y4eawyfl.dispatcher.hana.ondemand.com/sap/fiori/pychfinancingapply/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances(guid'"+scfCreateObj.get("ID").getAsString()+"')\",\"type\":\"ARTEC.PYGW.SupplyChainFinance\"}"
																							+ ",\"SCFGUID\":\""+scfCreateObj.get("ID").getAsString()+"\""
																							+ ",\"CPGUID\":\""+scfCreateObj.get("CPGUID").getAsString()+"\""
																							+ ",\"LoginID\":\""+loginID+"\""
																							+ ",\"CPTypeID\":\""+scfCreateObj.get("CPTypeID").getAsString()+"\""
																							+ ",\"CPTypeDesc\":\"\""
																							+ ",\"OfferAmt\":\""+scfCreateObj.get("OfferAmt").getAsString()+"\""
																							+ ",\"OfferTenure\":\""+scfCreateObj.get("OfferTenure").getAsString()+"\""
																							+ ",\"Rate\":\""+scfCreateObj.get("Rate").getAsString()+"\""
																							+ ",\"AccountNo\":\"\""
																							+ ",\"NoOfChequeReturns\":\""+scfCreateObj.get("NoOfChequeReturns").getAsString()+"\""
																							+ ",\"PaymentDelayDays12Months\":\""+scfCreateObj.get("PaymentDelayDays12Months").getAsString()+"\""
																							+ ",\"BusinessVintageOfDealer\":\""+scfCreateObj.get("BusinessVintageOfDealer").getAsString()+"\""
																							+ ",\"PurchasesOf12Months\":\""+scfCreateObj.get("PurchasesOf12Months").getAsString()+"\""
																							+ ",\"DealersOverallScoreByCorp\":\""+scfCreateObj.get("DealersOverallScoreByCorp").getAsString()+"\""
																							+ ",\"CorpRating\":\""+scfCreateObj.get("CorpRating").getAsString()+"\""
																							+ ",\"DealerVendorFlag\":\"\""
																							+ ",\"ConstitutionType\":\""+scfCreateObj.get("ConstitutionType").getAsString()+"\""
																							+ ",\"MaxLimitPerCorp\":\""+scfCreateObj.get("MaxLimitPerCorp").getAsString()+"\""
																							+ ",\"salesOf12Months\":\""+scfCreateObj.get("salesOf12Months").getAsString()+"\""
																							+ ",\"Currency\":\""+scfCreateObj.get("Currency").getAsString()+"\""
																							+ ",\"StatusID\":\""+scfCreateObj.get("StatusID").getAsString()+"\""
																							+ ",\"StatusIDDesc\":\"\""
																							+ ",\"TestRun\":\"\""
																							+ ",\"TotalBalance\":\"0.00\""
																							+ ",\"TenorIndays\":\"\""
																							+ ",\"PeakLimit\":\"0.00\""
																							+ ",\"PeaklmtTenorindays\":\"\""
																							+ ",\"OverdueBeyondCure\":\"0.00\""
																							+ ",\"AsOnDate\":null"
																							+ ",\"ReportDate\":null"
																							+ ",\"MCLR6Rate\":\"8.15\""
																							+ ",\"InterestRateSpread\":\""+scfCreateObj.get("InterestRateSpread").getAsString()+"\""
																							+ ",\"TenorOfPayment\":\""+scfCreateObj.get("TenorOfPayment").getAsString()+"\""
																							+ ",\"ADDLNPRDINTRateSP\":\""+scfCreateObj.get("ADDLNPRDINTRateSP").getAsString()+"\""
																							+ ",\"AddlnTenorOfPymt\":\""+scfCreateObj.get("AddlnTenorOfPymt").getAsString()+"\""
																							+ ",\"DefIntSpread\":\""+scfCreateObj.get("DefIntSpread").getAsString()+"\""
																							+ ",\"ProcessingFee\":\""+scfCreateObj.get("ProcessingFee").getAsString()+"\""
																							+ ",\"OutstandingAmount\":\"\""
																							+ ",\"CreatedOn\":null"
																							+ ",\"CreatedBy\":\"\""
																							+ ",\"CreatedAt\":\"\""
																							+ ",\"ChangedOn\":null"
																							+ ",\"ChangedBy\":\"\""
																							+ ",\"ChangedAt\":\"\""
																							+ ",\"EContractID\":\""+scfCreateObj.get("EContractID").getAsString()+"\""
																							+ ",\"ECustomerID\":\""+scfCreateObj.get("ECustomerID").getAsString()+"\""
																							+ ",\"ApplicationNo\":\""+scfCreateObj.get("ApplicationNo").getAsString()+"\""
																							+ ",\"CallBackStatus\":\"\""
																							+ ",\"ECompleteTime\":\"PT00H00M00S\""
																							+ ",\"ECompleteDate\":null"
																							+ ",\"ApplicantID\":\""+scfCreateObj.get("ApplicantID").getAsString()+"\""
																							+ ",\"IsOverdue\":\"\""
																							+ ",\"BlockFinancing\":\"\""
																							+ ",\"OverdueBy\":\"\""
																							+ ",\"BlockOrder\":\"\""
																							+ ",\"BlockingReasonID\":\"\""
																							+ ",\"BlockingReasonDesc\":\"\""
																							+ ",\"DDBActive\":\"\""
																							+ ",\"ProcessFeePerc\":\""+scfCreateObj.get("ProcessFeePerc").getAsString()+"\""
																							+ ",\"SupplyChainFinanceTxns\":null}}";
//																					response.getOutputStream().print(successResponse);
																					response.getWriter().println(successResponse);
																				}else{
																					//Set Error Response
																					errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", scfCreateResponse.get("d").getAsJsonObject().get("ErrorMessage").getAsString());
																					errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", scfCreateResponse.get("d").getAsJsonObject().get("ErrorCode").getAsString());
																					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																					response.getWriter().println(errorResponseForSCF);
																				}
																			}else{
																				message = "113";
																				errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
																				errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
																				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																				response.getWriter().println(errorResponseForSCF);
																			}
																		}else{
																			errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", "Customer is not eligible (Ref: "+scfApplyResults.get("ApplicationNo").getAsString()+" / ErrorCode: "+scfApplyResults.get("ErrorCode").getAsString()+")");
																			errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "109");
																			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																			response.getWriter().println(errorResponseForSCF);
																		}
																		
																	} else if(scfApplyResults.get("EligibilityStatus").getAsString().equalsIgnoreCase("000000")){
																		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/"+scfApplyResults.get("ErrorCode").getAsString()+"\",\"message\":{\"lang\""
																				+ ":\"en\",\"value\":\""+scfApplyResults.get("ErrorMessage").getAsString()+"\"}"
																				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\""
																				+ ",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},\"transactionid\""
																				+ ":\"959AC4E9D2DBF1E4BC9CD067E5F9AB12\",\"timestamp\":\"20190822050630.0500000\""
																				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG "
																				+ "on SAP Gateway hub system and search for entries with the timestamp above for more details\""
																				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis"
																				+ " (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\""
																				+ ":[{\"code\":\"/ARTEC/PY/"+scfApplyResults.get("ErrorCode").getAsString()+"\",\"message\":\""+scfApplyResults.get("ErrorMessage").getAsString()+""
																				+ "\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																				+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred"
																				+ ".\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
//																		response.s
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		response.getWriter().println(errorResponse);
																	} else {
																		String errorResponse = "{\"error\":{\"code\":\"/ARTEC/PY/113\",\"message\":{\"lang\""
																				+ ":\"en\",\"value\":\"Business Type in CF registration does not match with bank records\"}"
																				+ ",\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\""
																				+ ",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},\"transactionid\""
																				+ ":\"959AC4E9D2DBF1E4BC9CD067E5F9AB12\",\"timestamp\":\"20190822050630.0500000\""
																				+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG "
																				+ "on SAP Gateway hub system and search for entries with the timestamp above for more details\""
																				+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis"
																				+ " (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\""
																				+ ":[{\"code\":\"/ARTEC/PY/113\",\"message\":\"Business Type in CF registration does "
																				+ "not match with bank records\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}"
																				+ ",{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred"
																				+ ".\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
																		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																		response.getWriter().println(errorResponse);
																	}
																}
															}else{
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty("056"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "056");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}
														}else{
															if(responseScfOfferJsonObj.getString("StatusID").equalsIgnoreCase("000002")){
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseScfOfferJsonObj.getString("Message"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "059");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}else{
																errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", responseScfOfferJsonObj.getString("Message"));
																errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", "043");
																response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
																response.getWriter().println(errorResponseForSCF);
															}
														}
													}
												}else{
													errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
													errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
													response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
													response.getWriter().println(errorResponseForSCF);
												}
											}else{
												errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
												errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												response.getWriter().println(errorResponseForSCF);
											}
										break;
									}
								}else{
									response.getOutputStream().print("No TestRun field received in the request");
								}
//								JsonObject inputJsonObject = new JsonParser().parse(payloadRequest).getAsJsonObject();
//								response.getWriter().println("loadMetadata jsonObject: "+inputJsonObject);
							}else{
								//debug = false;
								JsonObject scfJsonObj = new JsonObject();
								JsonObject scfDealerOutStandingResponse = new JsonObject();
								SCFDealerOutStandingClient scfDealerOutStanding = new SCFDealerOutStandingClient();
								BigDecimal totalBalAmt = null;
								BigDecimal outstandingAmt = new BigDecimal("0.00");
								BigDecimal offerAmt = null;
								
								String executeURL = "", dealerODAccountNo="", overDueCureBal="", outstandingBal="", offerBal="";
								String queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
								queryString = queryString.replaceAll("%27and", "%27%20and");
								queryString = queryString.replaceAll("%27AND", "%27%20AND");
								queryString = queryString.replaceAll("%27OR", "%27%20OR");
								loginID = "";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
								queryString = queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20StatusID%20ne%20%27000003%27";
								
								executeURL = oDataURL+"SupplyChainFinances?"+queryString;
								
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);

								countEntity = serviceResponse.getEntity();
								String scfStatus="";
								
								if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
									String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
									if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
										response.setContentType(contentType);
										response.getOutputStream().print(EntityUtils.toString(countEntity));
									}else{
										response.setContentType(contentType);
										String Data = EntityUtils.toString(countEntity);
										//added From Here
										JsonParser parser = new JsonParser();
										scfJsonObj = (JsonObject)parser.parse(Data.toString());
										if (debug)
											response.getWriter().println("scfJsonObj: "+scfJsonObj);
										
										JsonObject childSCFJsonOBj = null;
										if (scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
											if( ! scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("AccountNo").isJsonNull())
												dealerODAccountNo = scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("AccountNo").getAsString();
											else
												dealerODAccountNo = "";
											
											if( ! scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("StatusID").isJsonNull())
												scfStatus = scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("StatusID").getAsString();
											
											if (debug){
												response.getWriter().println("dealerODAccountNo: "+dealerODAccountNo);
												response.getWriter().println("scfStatus: "+scfStatus);
											}
											
											if(scfStatus != null && scfStatus.trim().equalsIgnoreCase("000002")){
												scfDealerOutStandingResponse = scfDealerOutStanding.callSCFDealerOutStandingClient(response, dealerODAccountNo, debug);
												if(debug){
													response.getWriter().println("buildSCFDealerOutstandingResponse.scfDealerWSJson: "+scfDealerOutStandingResponse);
													response.getWriter().println("WS-Status: "+scfDealerOutStandingResponse.get("Status").getAsString());
												}
												if (scfDealerOutStandingResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
													
													//Add those 7 new fields inside the loop
													JsonObject childSCFDealer = null;
													JsonArray scfDealerWSArray = new JsonArray();
													
													scfDealerWSArray = scfDealerOutStandingResponse.get("results").getAsJsonArray();
													if (debug)
														response.getWriter().println("supplyChainFinances.scfDealerWSArray: "+scfDealerWSArray);
													
													for (int i = 0; i < scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() ; i++)
													{
														childSCFJsonOBj = scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
														
														for (int j = 0; j < scfDealerWSArray.size(); j++) {
															
															childSCFDealer = scfDealerWSArray.get(j).getAsJsonObject();
															if ( childSCFDealer.get("TxnCategoryDescription").getAsString().equalsIgnoreCase("Totals")) {
																try
																{
																	outstandingBal = childSCFDealer.get("OutstandingAmount").getAsString();
																	outstandingBal = outstandingBal.substring(0, outstandingBal.lastIndexOf(".") + 3);
																	outstandingAmt = new BigDecimal(outstandingBal);
																	
																} catch (Exception e) {
																	outstandingAmt = new BigDecimal(outstandingBal);
																}
																break;
															}
														}
														childSCFJsonOBj.addProperty("OutstandingAmount", outstandingAmt.toString());
														try {
															//Removing trailing Zeros
															offerBal = scfDealerOutStandingResponse.get("SanctionLimit").getAsString();//offerAmt.toString();
														if (offerBal.contains(".")) {
															offerBal = offerBal.substring(0,
																	offerBal.lastIndexOf(".") + 3);
															offerAmt = new BigDecimal(offerBal);
														} else {
															offerAmt = new BigDecimal(offerBal);

														}
															
														} catch (Exception e) {
															offerAmt =new BigDecimal(offerBal);
														}
														
														try {
															overDueCureBal = scfDealerOutStandingResponse.get("OverdueBeyondCure").getAsString();
															overDueCureBal = overDueCureBal.substring(0, overDueCureBal.lastIndexOf(".") + 3);
														} catch (Exception e) {
															// TODO: handle exception
														}
														
														childSCFJsonOBj.addProperty("TenorIndays", scfDealerOutStandingResponse.get("TenorInDays").getAsString());
														childSCFJsonOBj.addProperty("PeakLimit", scfDealerOutStandingResponse.get("PeakLimit").getAsString());
														childSCFJsonOBj.addProperty("PeaklmtTenorindays", scfDealerOutStandingResponse.get("PeakLimitTenorInDays").getAsString());
														childSCFJsonOBj.addProperty("OverdueBeyondCure", overDueCureBal);
														childSCFJsonOBj.addProperty("OfferAmt", offerAmt.toString());
														childSCFJsonOBj.addProperty("AsOnDate", scfDealerOutStandingResponse.get("AsOnDate").getAsString());
														try {
															totalBalAmt = offerAmt.subtract(outstandingAmt);
														} catch (Exception e) {
															totalBalAmt = new BigDecimal(0.00);
														}
														childSCFJsonOBj.addProperty("TotalBalance", totalBalAmt.toString());
													}
													response.getWriter().println(scfJsonObj);
												}else{
													for (int i = 0; i < scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() ; i++)
													{
														childSCFJsonOBj = scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
														childSCFJsonOBj.addProperty("OutstandingAmount", outstandingAmt.toString());
														childSCFJsonOBj.addProperty("TenorIndays", "0");
														childSCFJsonOBj.addProperty("PeakLimit", "0.00");
														childSCFJsonOBj.addProperty("PeaklmtTenorindays", "0");
														childSCFJsonOBj.addProperty("OverdueBeyondCure", "0.00");
														childSCFJsonOBj.addProperty("OfferAmt", "0.00");
														childSCFJsonOBj.addProperty("TotalBalance", "0.00");
														childSCFJsonOBj.addProperty("AsOnDate", "");
													}
													response.getWriter().println(scfJsonObj);
//													JsonObject toUIResponse = new JsonObject();
//													toUIResponse.add("d", new JsonObject());
//													toUIResponse.getAsJsonObject("d").add("results", new JsonArray());
//													response.getWriter().println(toUIResponse);
												}
											}else{
												for (int i = 0; i < scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").size() ; i++)
												{
													childSCFJsonOBj = scfJsonObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
													childSCFJsonOBj.addProperty("OutstandingAmount", outstandingAmt.toString());
													childSCFJsonOBj.addProperty("TenorIndays", "0");
													childSCFJsonOBj.addProperty("PeakLimit", "0.00");
													childSCFJsonOBj.addProperty("PeaklmtTenorindays", "0");
													childSCFJsonOBj.addProperty("OverdueBeyondCure", "0.00");
													childSCFJsonOBj.addProperty("OfferAmt", "0.00");
													childSCFJsonOBj.addProperty("TotalBalance", "0.00");
													childSCFJsonOBj.addProperty("AsOnDate", "");
												}
												response.getWriter().println(scfJsonObj);
											}
											
										} else {
											response.getWriter().println(scfJsonObj);
										}
									}
								}else{
									/*response.setContentType("application/pdf");
									response.getOutputStream().print(EntityUtils.toString(countEntity));*/
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/UserAccounts")){
							debug = false;
							String errorResponseForSCF="", message = "";
							String errorResFormatForSCF = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\""
									+ ",\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
									+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},"
									+ "\"transactionid\":\"554CB8E9E259F12DBC9CD067E5F9AB12\",\"timestamp\":\"20190806131604.2890000\","
									+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and "
									+ "search for entries with the timestamp above for more details\",\"SAP_Note\":\"See SAP Note 1797736 for "
									+ "error analysis (https://service.sap.com/sap/support/notes/1797736)\"},\"errordetails\":"
									+ "[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
									+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
									+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
							
							
							int userAccSize=0;
							
							if(request.getMethod().equalsIgnoreCase("GET")){
								debug = false;
								if(debug){
									response.getWriter().println("payloadRequest: "+payloadRequest);
									response.getWriter().println("dataPayload: "+dataPayload);
									response.getWriter().println("loginID: "+loginID);
									response.getWriter().println("oDataURL: "+oDataURL);
									response.getWriter().println("aggregatorID: "+aggregatorID);
								}
								
								//String accountType = request.getParamerter("BankAccntType")
								//String operation = request.getParamerter("operation")
								//if(operation.eqigcase("G")){
//								}else if(operation.eqigcase("U")){
//								}else{}
//								queryStr = PY2CRP
//								if(BankAccntType.eq('PY2CRP')){
//									response.getWriter().print - 'if'
//								}else{
//									response.getWriter().print = "else"
//								}
								
								JsonObject bankAccountEntriesJson = new JsonObject();
								JsonObject bankAccountsListResponse = new JsonObject();
								
								bankAccountEntriesJson = commonUtils.getUserAccountsInJson(request, response, loginID, oDataURL, aggregatorID, "", properties, debug);
								
								response.setContentType("application/json");
								if(! bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").isJsonNull() 
										&& bankAccountEntriesJson.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().length() > 0){
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().println(bankAccountEntriesJson);
								}else{
									debug = false;
									userAccSize = commonUtils.getResultsSize(response, bankAccountEntriesJson, debug);
									if(debug)
										response.getWriter().println("pygw.userAccSize: "+userAccSize);
									bankAccountEntriesJson.get("d").getAsJsonObject().remove("ErrorCode");
									bankAccountEntriesJson.get("d").getAsJsonObject().remove("ErrorMessage");
									
									if(userAccSize > 0){
										
										bankAccountsListResponse = commonUtils.setStandingInstruction1(request, response, bankAccountEntriesJson, properties, loginID, aggregatorID, oDataURL, debug);
										if(debug)
											response.getWriter().println("pygw.bankAccountsListResponse: "+bankAccountsListResponse);
										
										if(! bankAccountsListResponse.get("d").getAsJsonObject().get("Status").isJsonNull()
											&& bankAccountsListResponse.get("d").getAsJsonObject().get("Status").getAsString().trim().length() > 0){
											
											if(bankAccountsListResponse.get("d").getAsJsonObject().get("Status").getAsString().trim().equalsIgnoreCase("000001")){
												//Success
												bankAccountsListResponse.get("d").getAsJsonObject().remove("Status");
												bankAccountsListResponse.get("d").getAsJsonObject().remove("ErrorCode");
												bankAccountsListResponse.get("d").getAsJsonObject().remove("ErrorMessage");
//												response.getWriter().println(bankAccountsListResponse);
												response.getWriter().println(new Gson().toJson(bankAccountsListResponse));
											}else{
												//Error
												message = bankAccountsListResponse.get("d").getAsJsonObject().get("ErrorCode").getAsString();
												errorResponseForSCF = errorResFormatForSCF.replaceAll("ERROR_MESSAGE", properties.getProperty(message));
												errorResponseForSCF = errorResponseForSCF.replaceAll("ERROR_CODE", message);
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												response.getWriter().println(errorResponseForSCF);
											}
										}
										
									}else{
										response.getWriter().println(bankAccountEntriesJson);
									}
								}
								
								/*String executeURL = "";
								loginID = "";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
								executeURL = oDataURL+"UserAccounts?$filter=LoginId%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								
								httpClient = destination.createHttpClient();
								readRequest = new HttpGet(executeURL);*/
								
								//readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
								
								/*HttpResponse serviceResponse = httpClient.execute(readRequest);
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
									response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
									response.getWriter().println("loadMetadata countEntity: "+countEntity);
								}*/
							}else if(request.getMethod().equalsIgnoreCase("POST")){
								//POST
								response.setContentType("application/json");
								JsonObject bankAccountEntriesJson = new JsonObject();
								payloadRequest = ""; dataPayload="";
								payloadRequest = getGetBody(request, response);
//								dataPayload = payloadRequest;
								dataPayload = payloadRequest.replaceAll("\\\\", "");
								
								if(debug){
									response.getWriter().println("loadMetadata payloadRequest: "+payloadRequest);
									response.getWriter().println("loadMetadata dataPayload: "+dataPayload);
									response.getWriter().println("loginID: "+loginID);
									response.getWriter().println("oDataURL: "+oDataURL);
									response.getWriter().println("aggregatorID: "+aggregatorID);
								}
									
//								Gson inputJsonObject = new Gson();
//								inputJsonObject.fromJson(payloadRequest);
								JSONObject inputJsonObject = new JSONObject(payloadRequest);
								if(debug)
									response.getWriter().println("loadMetadata inputJsonObject: "+inputJsonObject);
								
								try{
									if(inputJsonObject.has("debug")&&inputJsonObject.getString("debug").equalsIgnoreCase("true")){
										debug=true;
									}
									
									if(debug){
										response.getWriter().println("loadMetadata payloadRequest: "+payloadRequest);
										response.getWriter().println("loadMetadata dataPayload: "+dataPayload);
										response.getWriter().println("loginID: "+loginID);
										response.getWriter().println("oDataURL: "+oDataURL);
										response.getWriter().println("aggregatorID: "+aggregatorID);
									}
									/*if(inputJsonObject.getString("Testrun").equalsIgnoreCase("X")){
										debug = false;
									}else{
										debug = false;
									}*/
								}catch (Exception e) {
									if(e.getMessage().contains("JSONObject[\"Testrun\"] not found")){
										//When Testrun in ""
										// debug = false;
									}
								}
								JsonObject applicationLog=new JsonObject();
								String appLogID = commonUtils.generateGUID(36);
								applicationLog.addProperty("ID",appLogID);
								applicationLog.addProperty("AggregatorID",aggregatorID);
								applicationLog.addProperty("LogObject","Java");
								applicationLog.addProperty("LogSubObject","PYGW/UserAccounts");
								String logDate = commonUtils.getCurrentDate("yyyy-MM-dd");
								applicationLog.addProperty("LogDate",logDate);
								String logTime = commonUtils.getCurrentTime();
								commonUtils.getCreatedAtTime();
								applicationLog.addProperty("LogTime",logTime);
								applicationLog.addProperty("Program",request.getServletPath());
								applicationLog.addProperty("ProcessRef2","");
								applicationLog.addProperty("ProcessID","");
								applicationLog.addProperty("CorrelationID","");
								String createdBy = commonUtils.getLoginID(request, response, debug);
								applicationLog.addProperty("CreatedBy",createdBy);
								applicationLog.addProperty("CreatedOn",logDate);
								applicationLog.addProperty("CreatedAt",logTime);
								applicationLog.addProperty("SourceReferenceID","");
								JsonArray appLogMessages=new JsonArray();
								bankAccountEntriesJson = commonUtils.userAccountsCreate(request, response, inputJsonObject, loginID, aggregatorID, oDataURL, properties,appLogMessages,appLogID,debug);
								JsonObject insertIntoLogsOnEvent = commonUtils.insertIntoLogsOnEvent(response, aggregatorID, applicationLog, appLogMessages, debug);
								/* if(debug){
									response.getWriter().println("insertIntoLogsOnEvent:"+insertIntoLogsOnEvent);
								} */
//								response.getWriter().println(bankAccountEntriesJson);
							}else if(request.getMethod().equalsIgnoreCase("PUT")){
								response.getWriter().println("PUT Method");
							}else{
								response.getWriter().println("Request Method: "+request.getMethod());
							}
							
						} else if(request.getPathInfo().equalsIgnoreCase("/UserCustomers")){
//							debug=true;
							String executeURL = "", queryString="";
							queryString = request.getQueryString();
//							request.getQueryString().replaceAll("%27or", "%27%20or");
							loginID = "";
							loginID = commonUtils.getUserPrincipal(request, "name", response);
							if(debug){
								response.getWriter().println("loginID: "+loginID);
								response.getWriter().println("queryString: "+queryString);
							}
//							loginID = "P2000278306"; //Temporarily hard coded
							if(null != queryString && queryString.trim().length() > 0){
								/*response.getWriter().println("queryString: "+queryString);
								response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
								response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
								response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
								response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
								response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
								response.getWriter().println("loadMetadata serviceURL: "+serviceURL);*/
								
								executeURL = oDataURL+"UserCustomers?"+queryString+"%20and%20LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
//								response.getWriter().println("executeURL: "+executeURL);
//								executeURL = oDataURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							}else{
								executeURL = oDataURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							}
							if(debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//							response.getWriter().println("loadMetadata httpClient: "+httpClient);
							readRequest = new HttpGet(executeURL);
							readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//							response.getWriter().println("loadMetadata readRequest: "+readRequest);
							// HttpResponse serviceResponse = httpClient.execute(readRequest);
							HttpResponse serviceResponse = client.execute(readRequest);

							countEntity = serviceResponse.getEntity();
//							response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//							response.getWriter().println("loadMetadata countEntity: "+countEntity);
//							response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
							
							if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
								String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
								if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
									response.setContentType(contentType);
									response.getWriter().print(EntityUtils.toString(countEntity));
								}else{
									response.setContentType(contentType);
									String Data = EntityUtils.toString(countEntity);
									if(debug)
										response.getWriter().println("Data: "+Data);
									response.getWriter().print(Data);	
								}
							}else{
								/*response.setContentType("application/pdf");
								response.getOutputStream().print(EntityUtils.toString(countEntity));*/
								response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
								response.getWriter().println("loadMetadata countEntity: "+countEntity);
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/UserRegistrations")){
							debug = false;
							Map<String, String> userRegMap = new HashMap<String, String>();
							String message="", errorResponseForUserReg="";
														
							String errorResFormatForUserReg = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\""
									+ ",\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"}"
									+ ",\"transactionid\":\"0FCBD3E9D314F118A8EC001372667F53\",\"timestamp\":\"20190910130120.3870000\""
									+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\""
									+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\""
									+ ",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"}"
									+ ",\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\""
									+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\""
									+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
							
							payloadRequest="";
							
							if(request.getMethod().equalsIgnoreCase("POST")){
								payloadRequest = getGetBody(request, response);
//								dataPayload = payloadRequest;
								dataPayload = payloadRequest.replaceAll("\\\\", "");
//								response.getWriter().println("loadMetadata dataPayload: "+payloadRequest);
//								Gson inputJsonObject = new Gson();
//								inputJsonObject.fromJson(payloadRequest);
								JSONObject inputJsonObject = new JSONObject(payloadRequest);
								debug=false;
								if((inputJsonObject.getString("CorpId") != null && inputJsonObject.getString("CorpId").trim().length() > 0)
									&& (inputJsonObject.getString("UserId") != null && inputJsonObject.getString("UserId").trim().length() > 0)){
//									userRegMap = commonUtils.getUserRegDetails(request, response, loginID, aggregatorID, oDataURL, debug);
									userRegMap = commonUtils.getUserRegDetailsForReg(request, response, inputJsonObject, loginID, aggregatorID, oDataURL, debug);
									
									if(userRegMap.get("Error") != null && userRegMap.get("Error").trim().length() > 0){
										errorResponseForUserReg = errorResFormatForUserReg.replaceAll("ERROR_MESSAGE", properties.getProperty("012"));
										errorResponseForUserReg = errorResponseForUserReg.replaceAll("ERROR_CODE", "012");
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										response.getWriter().println(errorResponseForUserReg);
									}else{
										JsonObject userRegInsrObj = new JsonObject();
										JsonObject userRegInsrResponseToUI = new JsonObject();
										
										//Call WS
										userRegInsrObj = commonUtils.registerUser(request, response, inputJsonObject, loginID, aggregatorID, corpID, properties, debug);
										
										userRegInsrResponseToUI = userRegInsrObj.get("d").getAsJsonObject();
										if(debug)
											response.getWriter().println("pygw.userRegInsrResponseToUI: "+userRegInsrResponseToUI);
										
//										if(scfCreateResponse.get("EligibilityStatus").getAsString().equalsIgnoreCase("000001")){
										if(userRegInsrResponseToUI.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")){
											String successResponse = "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserRegistrations('31366')\""
													+ ",\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserRegistrations('"+userRegInsrResponseToUI.get("UserId").getAsString()+"')\""
													+ ",\"type\":\"ARTEC.PYGW.UserRegistration\"}"
													+ ",\"CorpId\":\""+userRegInsrResponseToUI.get("CorpId").getAsString()+"\""
													+ ",\"TransactionType\":\"\""
													+ ",\"Remarks\":\"\""
													+ ",\"UserId\":\""+userRegInsrResponseToUI.get("UserId").getAsString()+"\""
													+ ",\"UserRegId\":\""+userRegInsrResponseToUI.get("UserRegId").getAsString()+"\""
													+ ",\"LoginId\":\"\""
													+ ",\"CommnGuid\":\""+userRegInsrResponseToUI.get("CommnGuid").getAsString()+"\""
													+ ",\"UserRegStatus\":\""+userRegInsrResponseToUI.get("UserRegStatus").getAsString()+"\""
													+ ",\"Source\":\"\""
													+ ",\"CreatedOn\":null,\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\""
													+ ",\"ChangedOn\":null,\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\""
													+ ",\"OTP\":\"\""
													+ ",\"TranId\":\"\""
													+ ",\"Amount\":\"0.00\""
													+ ",\"AccNo\":\"\""
													+ ",\"Currency\":\"\""
													+ ",\"OtpDelMode\":\"\""
													+ ",\"TransactionDetail\":\"\""
													+ ",\"DeliveryAddress\":\"\""
													+ ",\"AggrID\":\""+userRegInsrResponseToUI.get("AggregatorID").getAsString()+"\""
													+ ",\"AliasID\":\""+userRegInsrResponseToUI.get("AliasID").getAsString()+"\"}}";
											
											response.getWriter().println(successResponse);
										}else{
											errorResponseForUserReg = errorResFormatForUserReg.replaceAll("ERROR_MESSAGE", userRegInsrResponseToUI.get("ErrorMessage").getAsString());
											errorResponseForUserReg = errorResponseForUserReg.replaceAll("ERROR_CODE", userRegInsrResponseToUI.get("ErrorCode").getAsString());
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											response.getWriter().println(errorResponseForUserReg);
										}
									}
								}else{
									errorResponseForUserReg = errorResFormatForUserReg.replaceAll("ERROR_MESSAGE", properties.getProperty("011"));
									errorResponseForUserReg = errorResponseForUserReg.replaceAll("ERROR_CODE", "011");
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().println(errorResponseForUserReg);
								}
							}else{
								String executeURL = "";
								String queryString = "";
								loginID = "";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
								
								if(null != request.getQueryString() && request.getQueryString().trim().length() > 0){
									queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
									queryString = queryString.replaceAll("%27and", "%27%20and");
									queryString = queryString.replaceAll("%27AND", "%27%20AND");
									queryString = queryString.replaceAll("%27OR", "%27%20OR");
									
									queryString = queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginId%20eq%20%27"+loginID+"%27";
								}else{
									queryString = "$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20LoginId%20eq%20%27"+loginID+"%27";
								}

//								response.getWriter().println("loginID: "+loginID);
//								response.getWriter().println("queryString: "+queryString);
								
								executeURL = oDataURL+"UserRegistrations?"+queryString;
//								response.getOutputStream().println("executeURL: "+executeURL);
								
								// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//								response.getWriter().println("loadMetadata httpClient: "+httpClient);
								readRequest = new HttpGet(executeURL);
								readRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//								response.getWriter().println("loadMetadata readRequest: "+readRequest);
								// HttpResponse serviceResponse = httpClient.execute(readRequest);
								HttpResponse serviceResponse = client.execute(readRequest);

								countEntity = serviceResponse.getEntity();
//								response.getWriter().println("loadMetadata serviceResponse: "+serviceResponse);
//								response.getWriter().println("loadMetadata countEntity: "+countEntity);
//								response.getWriter().println("loadMetadata getContentType: "+serviceResponse.getEntity().getContentType().toString());
								
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
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/EnhancementLimits")){
							debug = false;
							String message="";
							
							String errorResForEnhancementLimits = "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\""
									+ ",\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\",\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"}"
									+ ",\"transactionid\":\"0FCBD3E9D314F118A8EC001372667F53\",\"timestamp\":\"20190910130120.3870000\""
									+ ",\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\""
									+ ",\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\""
									+ ",\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"}"
									+ ",\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\""
									+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has occurred.\""
									+ ",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
							
							payloadRequest="";
							if(debug){
								response.getWriter().println("requestMethod: "+request.getMethod());
								response.getWriter().println("oDataURL: "+oDataURL);
							}
							
							if(request.getMethod().equalsIgnoreCase("POST")){
								payloadRequest = getGetBody(request, response);
//								dataPayload = payloadRequest;
								dataPayload = payloadRequest.replaceAll("\\\\", "");
//								response.getWriter().println("loadMetadata dataPayload: "+payloadRequest);
//								Gson inputJsonObject = new Gson();
//								inputJsonObject.fromJson(payloadRequest);
								JSONObject inputJsonObject = new JSONObject(payloadRequest);
								
								JsonObject enhancementPostResponse = new JsonObject();
								JsonObject validationsResponse = new JsonObject();
								
//								validationsResponse = commonUtils.validateEnhancementUpdate(request, response, inputJsonObject, aggregatorID, oDataURL, properties, debug);
								
								enhancementPostResponse = commonUtils.modifyEnhancementLimits(request, response, inputJsonObject, aggregatorID, oDataURL, debug);
							
								if(enhancementPostResponse.has("Error")){
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().println(enhancementPostResponse);
								}else{
									response.getWriter().println(enhancementPostResponse);
								}
							}else{
								/*response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
								response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
								response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
								response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
								response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
								response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
								response.getWriter().println("loadMetadata serviceURL: "+serviceURL);*/
								
								String executeURL = "";
								String queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
								queryString = queryString.replaceAll("%27and", "%27%20and");
								queryString = queryString.replaceAll("%27AND", "%27%20AND");
								queryString = queryString.replaceAll("%27OR", "%27%20OR");
								
//								response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
								if(request.getPathInfo().equalsIgnoreCase("/BPHeaders")){
									queryString=queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
//								response.getWriter().println("loadMetadata updated queryString: "+queryString);
//								response.getWriter().println("queryString: "+queryString);
//								response.getWriter().println("loadMetadata filter: "+request.getParameter("$filter"));
								if(null != queryString && queryString.trim().length()>0){
									String entityInfo  = request.getPathInfo().replace("/", "");;
//									response.getWriter().println("loadMetadata entityInfo: "+entityInfo);
									if(null != entityInfo && entityInfo.trim().length()>0){
										executeURL = oDataURL+entityInfo+"?"+queryString;
//										response.getWriter().println("loadMetadata updated executeURL: "+executeURL);
//										executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PCGW/service.xsodata/ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27SF%27%20or%20Typeset%20eq%20%27PY%27";
//										response.getWriter().println("loadMetadata executeURL: "+executeURL);
										
//										executeURL = executeURL.replaceAll("%20", " ");
//										executeURL = executeURL.replaceAll("%27", "'");
//										response.getWriter().println("loadMetadata executeURL1: "+executeURL);
										// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//										response.getWriter().println("loadMetadata httpClient: "+httpClient);
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
						}else if(request.getPathInfo().equalsIgnoreCase("/SCFEnhancementLimits")){
							debug = false;
							payloadRequest ="";
							JsonObject peakLimitEligResponse = new JsonObject();
							if(debug){
								response.getWriter().println("PYGW.SCFEnhancementLimits.aggregatorID: "+aggregatorID);
							}
							if (request.getMethod().equalsIgnoreCase("GET")){

								/*response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
								response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
								response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
								response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
								response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
								response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
								response.getWriter().println("loadMetadata serviceURL: "+serviceURL);*/
								
								String executeURL = "";
								String queryString = "";
								if(null != request.getQueryString() && request.getQueryString().trim().length() > 0){
									queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
									queryString = queryString.replaceAll("%27and", "%27%20and");
									queryString = queryString.replaceAll("%27AND", "%27%20AND");
									queryString = queryString.replaceAll("%27OR", "%27%20OR");
								}
								
								
//								response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
//								if(request.getPathInfo().equalsIgnoreCase("/BPHeaders")){
								if(null != queryString && queryString.trim().length()>0){
									queryString=queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}else{
									queryString=queryString+"$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
									
//								}
								
								if(debug){
									response.getWriter().println("SCFEnhancementLimits.queryString: "+queryString);
								}
//								response.getWriter().println("loadMetadata updated queryString: "+queryString);
//								response.getWriter().println("queryString: "+queryString);
//								response.getWriter().println("loadMetadata filter: "+request.getParameter("$filter"));
								if(null != queryString && queryString.trim().length()>0){
									String entityInfo  = request.getPathInfo().replace("/", "");;
//									response.getWriter().println("loadMetadata entityInfo: "+entityInfo);
									if(null != entityInfo && entityInfo.trim().length()>0){
										executeURL = oDataURL+entityInfo+"?"+queryString;
										if(debug){
											response.getWriter().println("SCFEnhancementLimits.executeURL: "+executeURL);
										}
//										response.getWriter().println("loadMetadata updated executeURL: "+executeURL);
//										executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PCGW/service.xsodata/ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27SF%27%20or%20Typeset%20eq%20%27PY%27";
//										response.getWriter().println("loadMetadata executeURL: "+executeURL);
										
//										executeURL = executeURL.replaceAll("%20", " ");
//										executeURL = executeURL.replaceAll("%27", "'");
//										response.getWriter().println("loadMetadata executeURL1: "+executeURL);
										// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//										response.getWriter().println("loadMetadata httpClient: "+httpClient);
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
												response.getWriter().println(EntityUtils.toString(countEntity));
											}else{
												response.setContentType(contentType);
												String Data = EntityUtils.toString(countEntity);
												response.getWriter().println(Data);	
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
							}else{
								//POST method
							}
							
						}else if(request.getPathInfo().equalsIgnoreCase("/PeakLimitEligibility")){
							debug = false;
							payloadRequest ="";
							JsonObject peakLimitEligResponse = new JsonObject();
							if (request.getMethod().equalsIgnoreCase("GET")){
								peakLimitEligResponse = commonUtils.getPeakLimitEligibility(request, response, aggregatorID, debug);
								response.getWriter().println(peakLimitEligResponse);
							}else{
								payloadRequest = getGetBody(request, response);
								JSONObject inputJson =null;
								
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/PrimaryLimit")){
							JsonObject primaryLimitDtlJsonObj = new JsonObject();
							if(request.getMethod().equalsIgnoreCase("POST")){
								
							//	commonUtils.insertPrimaryLimit(request,response,aggregatorID,debug);
							
							}
							else if(request.getMethod().equalsIgnoreCase("GET")){
								debug = false;
								String queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
								queryString = queryString.replaceAll("%27and", "%27%20and");
								queryString = queryString.replaceAll("%27AND", "%27%20AND");
								queryString = queryString.replaceAll("%27OR", "%27%20OR");
								
								primaryLimitDtlJsonObj = commonUtils.getPrimaryLimitDetail(request, response, aggregatorID, queryString, debug);
							}
						}
						else if (request.getPathInfo().equalsIgnoreCase("/SupplyChainFinanceTxns")) {
							
							String cpGuid="", cpGuidQuery="", formatCpGuid="";
							debug = false; 
							loginID = "";
							
							if (request.getMethod().equalsIgnoreCase("GET")) {
								String  queryString="",dealerODAccountNo="";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
//								try {
//									JSONObject inputJson = null;
//									payloadRequest = getGetBody(request, response);
//									inputJson = new JSONObject(payloadRequest);
//								} catch (Exception e) {
//								}
								if (debug) {
									response.getWriter().println("oDataURL: "+oDataURL);
									response.getWriter().println("aggregatorID: "+aggregatorID);
									response.getWriter().println("loginID: "+loginID);
								}
								JsonObject scfDealerOutstanding = new JsonObject();
								JsonObject scfDealerOutStandingResponse = new JsonObject();
								JsonObject scfByCPResonse = new JsonObject();
								JsonObject scfDealerItems = new JsonObject();
								SCFDealerOutStandingClient scfDealerOutStanding = new SCFDealerOutStandingClient();
								
								JsonObject scfDealerObjResponse = new JsonObject();
								JsonArray scfDealerArrayResponse = new JsonArray();
								JsonObject scfDealerresponseUI = new JsonObject();
								try {
									queryString = request.getQueryString();
									
									cpGuidQuery = queryString.substring(queryString.indexOf("CPGUID"), queryString.length());
									cpGuidQuery = cpGuidQuery.replace("%20", "").replace("%27", "").replace("and", "").replace("eq", ":").trim();
									cpGuid = cpGuidQuery.substring(cpGuidQuery.indexOf(":")+ 1, cpGuidQuery.length());
									
									// adding leading Zeros to cpGuid for filtering
									/*if (cpGuid.length() < 10){
										formatCpGuid = ("0000000000" + cpGuid).substring(cpGuid.length());
										queryString = queryString.replaceAll(cpGuid, formatCpGuid);
									}*/
									String formattedStr = "";
									try{
										int number = Integer.parseInt(cpGuid);
										formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
										cpGuid = formattedStr;
										queryString = queryString.replaceAll(cpGuid, formattedStr);
									}catch (NumberFormatException e) {
//										formattedStr = customerNo;
									}
									
									queryString = queryString.substring(queryString.lastIndexOf("CPTypeID"), queryString.length());
									queryString = queryString.replaceAll("eq%20", "eq%20%27");
									queryString = queryString.replaceAll("%20and", "%27%20and");
									queryString = queryString +"%27";
								  /*queryString = queryString.replaceAll("%27or", "%27%20or");
									queryString = queryString.replaceAll("%27and", "%27%20and");
									queryString = queryString.replaceAll("%27AND", "%27%20AND");
									queryString = queryString.replaceAll("%27OR", "%27%20OR");
									*/
								}
								catch (Exception e) {
									queryString = "";
								}
								if (debug)
									response.getWriter().println("queryString: "+queryString);
								
								response.setContentType("application/json");
								if ( ! queryString.equalsIgnoreCase("") &&  queryString.trim().length() > 0) {
									
									scfByCPResonse = commonUtils.getSCFByCP(request, response, queryString, aggregatorID, oDataURL, properties, debug);
									if(debug)
										response.getWriter().println("SupplyChainFinanceTxn.scfByCPResonse: "+scfByCPResonse);
									
									if ( scfByCPResonse.getAsJsonObject("d").getAsJsonArray("results").size() == 0 ) {
										
										response.getWriter().println(scfByCPResonse);
										if (debug)
											response.getWriter().println("scfByCPResonse: "+scfByCPResonse);
										
									} else {
										if (!scfByCPResonse.get("ErrorCode").isJsonNull() && (scfByCPResonse.get("ErrorCode").getAsString().trim().length() > 0 && scfByCPResonse.get("ErrorMessage").getAsString().trim().length() > 0 )) {
											scfDealerObjResponse.add("results", scfDealerArrayResponse);
											scfDealerresponseUI.add("d", scfDealerObjResponse);
											response.getWriter().println(scfDealerresponseUI);
											if (debug)
												response.getWriter().println("scfDealerresponseUI: "+scfDealerresponseUI);
										}
										else {
											dealerODAccountNo = scfByCPResonse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("AccountNo").getAsString();
											if (debug)
												response.getWriter().println("dealerODAccountNo: "+dealerODAccountNo);
											
											//TODO: calling WS
											scfDealerOutStandingResponse = scfDealerOutStanding.callSCFDealerOutStandingClient(response, dealerODAccountNo, debug);
											if(debug)
												response.getWriter().println("WS-Status: "+scfDealerOutStandingResponse.get("Status").getAsString());
											
											if(scfDealerOutStandingResponse.get("Status").getAsString().trim().equalsIgnoreCase("000002") ){
												
												scfDealerObjResponse.add("results", scfDealerArrayResponse);
												scfDealerresponseUI.add("d", scfDealerObjResponse);
												response.getWriter().println(scfDealerresponseUI);
												if (debug)
													response.getWriter().println("scfDealerresponseUI: "+scfDealerresponseUI);
											} else {
												if (scfDealerOutStandingResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
													
													scfDealerItems = scfByCPResonse.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
													scfDealerOutstanding = commonUtils.buildSCFDealerOutstandingResponse(request, response, loginID, scfDealerItems, scfDealerOutStandingResponse, properties, debug);
													if (debug)
														response.getWriter().println("scfDealerOutstanding: "+scfDealerOutstanding);
													
													if (scfDealerOutstanding.get("Error").getAsString().equalsIgnoreCase("") && scfDealerOutstanding.get("ErrorMessage").getAsString().equalsIgnoreCase("")) {
														
														//TODO success response
														scfDealerOutstanding.remove("Error");
														scfDealerOutstanding.remove("ErrorMessage");
														response.getWriter().println(scfDealerOutstanding);
														response.setStatus(HttpServletResponse.SC_OK);
												
													} else {
														
														scfDealerObjResponse.add("results", scfDealerArrayResponse);
														scfDealerresponseUI.add("d", scfDealerObjResponse);
														response.getWriter().println(scfDealerresponseUI);
														if (debug)
															response.getWriter().println("scfDealerresponseUI: "+scfDealerresponseUI);
													}
												}else
												{
													scfDealerObjResponse.add("results", scfDealerArrayResponse);
													scfDealerresponseUI.add("d", scfDealerObjResponse);
													response.getWriter().println(scfDealerresponseUI);
													if (debug)
														response.getWriter().println("scfDealerresponseUI: "+scfDealerresponseUI);
												}
											}
										}
									}
								}else
								{
									scfDealerObjResponse.add("results", scfDealerArrayResponse);
									scfDealerresponseUI.add("d", scfDealerObjResponse);
									response.getWriter().println(scfDealerresponseUI);
									if (debug)
										response.getWriter().println("scfDealerresponseUI: "+scfDealerresponseUI);
								}
							}
							else
							{
								response.getWriter().println("");
							}
						} else if(request.getPathInfo().equalsIgnoreCase("/AccountBalances")){
							
							String errorResFormatForAccnBal="";
							debug = false; 
							loginID = "";
							errorResFormatForAccnBal= "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\","
									+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
									+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},"
									+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
									+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
									+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
									+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
									+ "\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
									+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
									+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
							
							if (request.getMethod().equalsIgnoreCase("GET")) {
								if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
									debug = true;
									
								String queryString="", accountNO="";
								loginID = commonUtils.getUserPrincipal(request, "name", response);
								
								Map<String, String> userResgistrationMap = new HashMap<String, String>();
								JsonObject accnBalanceResponseUI = new JsonObject();
								JsonObject userAccountJson = new JsonObject();
								
								if (debug) {
									response.getWriter().println("oDataURL: "+oDataURL);
									response.getWriter().println("aggregatorID: "+aggregatorID);
								}
								try{
									queryString = request.getQueryString();
									queryString = queryString.substring(queryString.indexOf("AccountNo"), queryString.length());
									queryString = queryString.replace("%20", "").replace("%27", "").replace("and", "").replace("eq", ":").trim();
									accountNO = queryString.substring(queryString.indexOf(":")+ 1, queryString.length());
								}catch (Exception e) {
									queryString ="";
								}
								
								if (debug){
									response.getWriter().println("AccountBalances.queryString: "+queryString);
									response.getWriter().println("AccountBalances.accountNO: "+accountNO);
								}
								response.setContentType("application/json");
								userResgistrationMap = commonUtils.getUserRegDetails(request, response, loginID, aggregatorID, oDataURL, debug);
								if(debug)
									response.getWriter().println("AccountBalances.userResgistrationMap: "+userResgistrationMap);
								
								if (! userResgistrationMap.get("Error").equalsIgnoreCase("") &&  userResgistrationMap.get("Error").trim().length() > 0) {
									
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									userAccountJson.add("results", new JsonArray());
									accnBalanceResponseUI.add("d", userAccountJson);
									response.getWriter().println(accnBalanceResponseUI);
									
								} else { //1
									if (userResgistrationMap.get("UserRegStatus").equalsIgnoreCase("000002")) {
										
										if (accountNO.trim().length() > 0) {
											userAccountJson= commonUtils.getUserAccountsInJson(request, response, loginID, oDataURL, aggregatorID, accountNO, properties, debug);
										} else {
											userAccountJson= commonUtils.getUserAccountsInJson(request, response, loginID, oDataURL, aggregatorID, "", properties, debug);
										}
										if(debug){ 
											response.getWriter().println("AccountBalances.userAccountJson: "+userAccountJson);
											response.getWriter().println("AccountBalances.userAccountJson.ErrorCode: "+userAccountJson.get("d").getAsJsonObject().get("ErrorCode").getAsString());
										}
										if(userAccountJson.get("d").getAsJsonObject().get("ErrorCode").getAsString().trim().length() == 0 && userAccountJson.get("d").getAsJsonObject().get("ErrorMessage").getAsString().trim().length() == 0 
												&&	userAccountJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0){
											
											// TODO: calling method for Build Response for UI
											accnBalanceResponseUI = commonUtils.buildAccountBalanceResponse(request, response, aggregatorID, loginID, userResgistrationMap ,userAccountJson , debug);
											if(debug)
												response.getWriter().println("AccountBalances.accnBalanceResponseUI: "+accnBalanceResponseUI);
											
											if (accnBalanceResponseUI.get("ErrorMessage").getAsString().trim().length()> 0) {
												
												if( accnBalanceResponseUI.get("Error").getAsString().trim().length() > 0 )
													errorResFormatForAccnBal = errorResFormatForAccnBal.replaceAll("ERROR_CODE", accnBalanceResponseUI.get("Error").getAsString());
												else
													errorResFormatForAccnBal = errorResFormatForAccnBal.replaceAll("ERROR_CODE", "054");

												errorResFormatForAccnBal = errorResFormatForAccnBal.replaceAll("ERROR_MESSAGE", accnBalanceResponseUI.get("ErrorMessage").getAsString());//.replace("\n\t", "")
												response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
												response.getWriter().println(errorResFormatForAccnBal);
											}
											else {
												// TODO:Success Response
												accnBalanceResponseUI.remove("Error");
												accnBalanceResponseUI.remove("ErrorMessage");
												response.setContentType("application/json");
												response.getWriter().println(accnBalanceResponseUI);
											}
										}else{
											response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
											userAccountJson.getAsJsonObject("d").remove("ErrorCode");
											userAccountJson.getAsJsonObject("d").remove("ErrorMessage");
											userAccountJson.getAsJsonObject("d").add("results", new JsonArray());
											response.getWriter().println(userAccountJson);
										}
									} else {
										response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
										userAccountJson.add("results", new JsonArray());
										accnBalanceResponseUI.add("d", userAccountJson);
										response.getWriter().println(accnBalanceResponseUI);
									}
								}
							} else {
								response.getWriter().println("");
							}
						} 
						else if (request.getPathInfo().equalsIgnoreCase("/OTPGenerate")) {
							payloadRequest ="";
							String errorResFormatForPGPayments="", txnID= "",  txnType="", currency="", otpDeliveryMode ="", aggrID="", pymntAmnt="", debitAcnNo="", remarks="",executeURL="",pcgwURL="";
							debug = false;
							String oDataUserName="", oDatapassword="", userPass="", pgCatId="", SuccessResFormatForPGPayments="";
							JsonObject pymntHederJson = new JsonObject();
							JsonObject pgCatJsonResponse = new JsonObject();
							
							errorResFormatForPGPayments= "{\"error\":{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":{\"lang\":\"en\","
									+ "\"value\":\"ERROR_MESSAGE\"},\"innererror\":{\"application\":{\"component_id\":\"\","
									+ "\"service_namespace\":\"/ARTEC/\",\"service_id\":\"PYGW\",\"service_version\":\"0001\"},"
									+ "\"transactionid\":\"7A3B29EA9B65F18A86C6D067E5F9AB12\",\"timestamp\":\"20191228063013.7170000\","
									+ "\"Error_Resolution\":{\"SAP_Transaction\":\"Run transaction /IWFND/ERROR_LOG on SAP Gateway hub system and search for entries with the timestamp above for more details\","
									+ "\"SAP_Note\":\"See SAP Note 1797736 for error analysis (https://service.sap.com/sap/support/notes/1797736)\","
									+ "\"Batch_SAP_Note\":\"See SAP Note 1869434 for details about working with $batch (https://service.sap.com/sap/support/notes/1869434)\"},"
									+ "\"errordetails\":[{\"code\":\"/ARTEC/PY/ERROR_CODE\",\"message\":\"ERROR_MESSAGE\",\"propertyref\":\"\",\"severity\":\"error\","
									+ "\"target\":\"\"},{\"code\":\"/IWBEP/CX_SD_GEN_DPC_BUSINS\",\"message\":\"An application exception has"
									+ " occurred.\",\"propertyref\":\"\",\"severity\":\"error\",\"target\":\"\"}]}}}";
						
							executeURL ="";
							String corpId ="", userId="", userRegId="";
							Map<String, String> otpGenerateServiceMap = new HashMap<String, String>();
							
							aggregatorID = "";
							pcgwURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
							aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
							Map<String, String> userResgistrationMap = new HashMap<String, String>();
							
							Enumeration enumeration = request.getParameterNames();
					        Map<String, String> modelMap = new HashMap<>();
					        while(enumeration.hasMoreElements()){
					            String parameterName = (String)enumeration.nextElement();
					            modelMap.put(parameterName, request.getParameter(parameterName));
					        }
					       
					        if (null != modelMap.get("debug"))
								debug = Boolean.parseBoolean(modelMap.get("debug"));
					        
					        if (debug){
					        	for (String key : modelMap.keySet()) {
					        		response.getWriter().println("OTPGenerate-modelMap: "+key + " - " + modelMap.get(key));
					        	}
					        }
							oDataUserName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
							oDatapassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
							userPass = oDataUserName+":"+oDatapassword;
							
							if ( null != modelMap.get("OTPTransactionID") )
								txnID = (String) modelMap.get("OTPTransactionID").replace("'", "");
							
							if ( null != modelMap.get("Amount") )
								pymntAmnt = (String) modelMap.get("Amount").replace("'", "");
							
							if ( null != modelMap.get("AccNo") )
								debitAcnNo = (String) modelMap.get("AccNo").replace("'", "");
							
							if ( null != modelMap.get("Currency") )
								currency = (String) modelMap.get("Currency").replace("'", "");
							
							if ( null != modelMap.get("Remarks") )
								remarks = (String) modelMap.get("Remarks").replace("'", "");
							
							
							 executeURL ="";
							 executeURL = pcgwURL+"PGPayments?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20TrackID%20eq%20%27"+txnID+"%27%20and%20CPAccountno%20eq%20%27"+debitAcnNo+"%27";
							 if(debug)
								 response.getWriter().println("PGPayments.executeURL1: "+executeURL);
							 
							 pymntHederJson = commonUtils.executeURL(executeURL, userPass, response);
							 if(debug)
								 response.getWriter().println("PGPayments.pymntHederJson: "+pymntHederJson);
							
							 if (pymntHederJson.getAsJsonObject("d").getAsJsonArray("results").size() > 0) {
								
								executeURL ="";
								pgCatId = pymntHederJson.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("PGCategoryID").getAsString();
								executeURL = pcgwURL+"PGPaymentCategories"+"?$filter=PGCategoryID%20eq%20%27"+pgCatId+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";//%20and%20CorpId%20eq%20%27"+corpId+"%27%20and%20userId%20eq%20%27"+userId+"%27";
								if (debug)
									response.getWriter().println("PGPayments.executeURL4: "+executeURL);
								
								pgCatJsonResponse = commonUtils.executeURL(executeURL, userPass, response);
								if (debug)
									response.getWriter().println("PGPayments.pgCatJsonResponse: "+pgCatJsonResponse);
								
								txnType = pgCatJsonResponse.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject().get("BankPaymentTransactionType").getAsString();
							
								
								//TODO Calling UserRegistration
								String pygwODataURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
								userResgistrationMap = commonUtils.getUserRegDetails(request, response, loginID, aggregatorID, pygwODataURL, debug);
								if (debug)
									response.getWriter().println("PGPayments.userRegisJsonResponse: "+userResgistrationMap);
								
								corpId = userResgistrationMap.get("CorpId");
								userId = userResgistrationMap.get("UserId");
								userRegId = userResgistrationMap.get("UserRegId");
								
								if (debug){
									response.getWriter().println("PGPayments.aggregatorID: "+aggregatorID);
									response.getWriter().println("PGPayments.userRegId: "+userRegId);
									response.getWriter().println("PGPayments.userId: "+userId);
									response.getWriter().println("PGPayments.corpId: "+corpId);
									response.getWriter().println("PGPayments.trackId: "+txnID);
									response.getWriter().println("PGPayments.txnType: "+txnType);
									response.getWriter().println("PGPayments.OTPDeliveryMode: "+"SMS");
									response.getWriter().println("PGPayments.pymntAmnt: "+pymntAmnt);
									response.getWriter().println("PGPayments.currency: "+currency);
									response.getWriter().println("PGPayments.debitAcnNo: "+debitAcnNo);
									response.getWriter().println("PGPayments.remarks: "+remarks);
								}
								
								//TODO calling OTP WebService
								OTPGeneratorClient otpGenerateClient = new OTPGeneratorClient();
								otpGenerateServiceMap = otpGenerateClient.callOTPGenerator(aggregatorID, userRegId, userId, corpId, txnID, txnType, "SMS", pymntAmnt, currency, debitAcnNo, remarks);//OTPDeliveryMode
								if(debug)
									response.getWriter().println("PGPayments.otpGenerateServiceMap: "+otpGenerateServiceMap);
								
								if (otpGenerateServiceMap.get("Status").equalsIgnoreCase("000001"))
								{
									SuccessResFormatForPGPayments= "{\"d\":{\"__metadata\":{\"id\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserRegistrations('')\","
											+ "\"uri\":\"http://sserp:8000/sap/opu/odata/ARTEC/PYGW/UserRegistrations('')\","
											+ "\"type\":\"ARTEC.PYGW.UserRegistration\"},"
											+ "\"CorpId\":\"\","
											+ "\"TransactionType\":\"\","
											+ "\"Remarks\":\"\","
											+ "\"UserId\":\"\","
											+ "\"UserRegId\":\"\",\"LoginId\":\"\",\"CommnGuid\":\"00000000-0000-0000-0000-000000000000\","
											+ "\"UserRegStatus\":\"\",\"Source\":\"\","
											+ "\"CreatedOn\":null,\"CreatedBy\":\"\",\"CreatedAt\":\"PT00H00M00S\",\"ChangedOn\":null,\"ChangedBy\":\"\",\"ChangedAt\":\"PT00H00M00S\","
											+ "\"OTP\":\"\",\"TranId\":\"\",\"Amount\":\"0.00\","
											+ "\"AccNo\":\"\",\"Currency\":\"\",\"OtpDelMode\":\"\","
											+ "\"TransactionDetail\":\"\",\"DeliveryAddress\":\"\",\"AggrID\":\"\","
											+ "\"AliasID\":\"\"}}";
									
									response.setContentType("application/json");;
									response.getWriter().println(SuccessResFormatForPGPayments);
									
								} else
								{
									errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "079");
									errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", properties.getProperty("079")+ otpGenerateServiceMap.get("Message").toString());
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().println(errorResFormatForPGPayments);
								}
							 } else {
								 errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_CODE", "e112");
								 errorResFormatForPGPayments = errorResFormatForPGPayments.replaceAll("ERROR_MESSAGE", "Transaction Not Found");
								 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								 response.getWriter().println(errorResFormatForPGPayments);
							 }
						}else if ( request.getPathInfo().equalsIgnoreCase("/COR")) {
							debug = false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="", filterQuery ="", responseAGGRID="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"COR?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									filterQuery = request.getParameter("$filter");
//									response.getWriter().println("filterQuery: "+filterQuery);
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
//								    response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
//									response.getWriter().println("updatedQuery: "+updatedQuery);
									executeURL = oDataURL+"COR?"+updatedQuery;
									
								} else {
									executeURL = oDataURL+"COR?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							response.getWriter().println("httpGetResponse: "+ httpGetResponse);
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							
						}else if ( request.getPathInfo().equalsIgnoreCase("/CorporateOpinionReport")) {
							
							debug = false;
							String queryString ="", executeURL="", updatedQuery="", filterQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"CorporateOpinionReport?$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
							} else {
								
								if (queryString.contains("$filter")) {
									
									filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									}
//									response.getWriter().println("updatedQuery: "+updatedQuery);
									executeURL = oDataURL+"CorporateOpinionReport?"+updatedQuery;
									
								} else{
									executeURL = oDataURL+"CorporateOpinionReport?"+queryString+"&$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
								}
							}
//							response.getWriter().println("aggrID: "+ aggregatorID);
//							response.getWriter().println("executeURL: "+executeURL);
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							
						}else if ( request.getPathInfo().equalsIgnoreCase("/SCP")) {
							
							debug = false;
							String queryString ="", executeURL="",updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();

							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCP?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									
									executeURL = oDataURL+"SCP?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCP?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if ( request.getPathInfo().equalsIgnoreCase("/SupplyChainPartners")) { //SupplyChainPartners
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();

							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SupplyChainPartners?$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SupplyChainPartners?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SupplyChainPartners?"+queryString+"&$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							
						}else if ( request.getPathInfo().equalsIgnoreCase("/SCP_1")) {
							
							debug =false;
							String queryString ="", executeURL="", updatedQuery="";

							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCP_1?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
									response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCP_1?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCP_1?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							
						}else if ( request.getPathInfo().equalsIgnoreCase("/SupplyChainPartnerPrimarySales")) {
							
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
//							response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SupplyChainPartnerPrimarySales?$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
							} else {
								
								if (queryString.contains("$filter")) {
									
									queryString = "";
									String filterQuery = request.getParameter("$filter");
//									response.getWriter().println("filterQuery: "+filterQuery);
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
//									response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
									}
									queryString = updatedQuery;
								}
								else if (queryString.contains("SourceReferenceID")) {
									queryString = "$filter=SourceReferenceID%20eq%20%27"+request.getParameter("SourceReferenceID")+"%27%20and%20AggregatorId%20eq%20%27"+aggregatorID+"%27";
								}else{
									queryString = queryString+"&$filter=AggregatorId%20eq%20%27"+aggregatorID+"%27";
								}
								executeURL = oDataURL+"SupplyChainPartnerPrimarySales?"+queryString;
							}
//							response.getWriter().println("executeURL: "+executeURL);
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if ( request.getPathInfo().equalsIgnoreCase("/ODAccountSummary")) {
							
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
//							response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"ODAccountSummary?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									queryString ="";
									String filterQuery = request.getParameter("$filter");
//									response.getWriter().println("filterQuery: "+filterQuery);
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
//									response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									queryString = updatedQuery;
								} else {
									queryString = queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
								executeURL = oDataURL+"ODAccountSummary?"+queryString;
							}
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if ( request.getPathInfo().equalsIgnoreCase("/ODAccountOutstanding")) {
																			
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
//							response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"ODAccountOutstanding?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								
								if (queryString.contains("$filter")) {
									
									queryString ="";
									String filterQuery = request.getParameter("$filter");
//									response.getWriter().println("filterQuery: "+filterQuery);
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
//									response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									queryString = updatedQuery;
								} else {
									queryString = queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
								executeURL = oDataURL+"ODAccountOutstanding?"+queryString;
							}
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if ( request.getPathInfo().equalsIgnoreCase("/ENHLMT")) { // ENHLMT
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"ENHLMT?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug) 
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"ENHLMT?"+updatedQuery;
								} else {
									executeURL = oDataURL+"ENHLMT?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if ( request.getPathInfo().equalsIgnoreCase("/SCF")) { // SCF
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCF?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCF?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCF?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//  New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
					} else if (request.getPathInfo().equalsIgnoreCase("/SCF1")) { // SCF1
						debug = false;
						String queryString = "", executeURL = "", updatedQuery = "";

						try {
							queryString = request.getQueryString();
						} catch (Exception e) {
							queryString = "";
						}
						if (debug)
							response.getWriter().println("queryString: " + queryString);

						if (queryString == null || queryString == "") {
							executeURL = oDataURL + "SCF1?$filter=AGGRID%20eq%20%27" + aggregatorID + "%27";
						} else {
							if (queryString.contains("$filter")) {
								String filterQuery = request.getParameter("$filter");
								if (debug)
									response.getWriter().println("filterQuery: " + filterQuery);

								filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
								;
								if (debug)
									response.getWriter().println("filterQuery: " + filterQuery);

								if (!filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
									updatedQuery = "$filter=(" + filterQuery + ")%20and%20AGGRID%20eq%20%27" + aggregatorID + "%27";
								} else {
									updatedQuery = "$filter=" + filterQuery + "%20and%20AGGRID%20eq%20%27" + aggregatorID + "%27";
								}
								executeURL = oDataURL + "SCF1?" + updatedQuery;
							} else {
								executeURL = oDataURL + "SCF1?" + queryString + "&$filter=AGGRID%20eq%20%27" + aggregatorID + "%27";
							}
						}
						if (debug)
							response.getWriter().println("executeURL: " + executeURL);

						// New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
						JsonObject httpGetResponse = new JsonObject();
						httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);

						if (httpGetResponse.has("error")) {
							response.getWriter().println(httpGetResponse);
						} else {
							response.setContentType("application/json");
							JsonArray scf1Records = httpGetResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
							if (scf1Records.size() > 0) {
								JsonArray scf1Array = new JsonArray();
								for (int i = 0; i < scf1Records.size(); i++) {
									JsonObject scfObj = scf1Records.get(i).getAsJsonObject();
									if (scfObj.get("ELIG_TYPE_ID").isJsonNull() || !scfObj.get("ELIG_TYPE_ID").getAsString().equalsIgnoreCase("AML")) {
										scf1Array.add(scfObj);
									}
								}
								JsonObject resObj = new JsonObject();
								resObj.add("results", scf1Array);
								JsonObject dObj = new JsonObject();
								dObj.add("d", resObj);
								response.getWriter().println(dObj);
							} else {
								response.getWriter().println(httpGetResponse);
							}
						}
					} else if (request.getPathInfo().equalsIgnoreCase("/SupplyChainFinanceEligibility")) { // SupplyChainFinanceEligibility
						debug = false;
						String queryString = "", executeURL = "", updatedQuery = "";

						try {
							queryString = request.getQueryString();
						} catch (Exception e) {
							queryString = "";
						}
						if (debug)
							response.getWriter().println("queryString: " + queryString);
						if (queryString == null || queryString == "") {
							executeURL = oDataURL + "SupplyChainFinanceEligibility?$filter=AggregatorID%20eq%20%27" + aggregatorID + "%27";
						} else {
							if (queryString.contains("$filter")) {
								String filterQuery = request.getParameter("$filter");
								if (debug)
									response.getWriter().println("filterQuery: " + filterQuery);

								filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
								;
								if (debug)
									response.getWriter().println("filterQuery: " + filterQuery);

								if (!filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
									updatedQuery = "$filter=(" + filterQuery + ")%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27";
								} else {
									updatedQuery = "$filter=" + filterQuery + "%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27";
								}
								executeURL = oDataURL + "SupplyChainFinanceEligibility?" + updatedQuery;
							} else {
								executeURL = oDataURL + "SupplyChainFinanceEligibility?" + queryString + "&$filter=AggregatorID%20eq%20%27" + aggregatorID + "%27";
							}
						}
						if (debug)
							response.getWriter().println("executeURL: " + executeURL);
						// New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
						JsonObject httpGetResponse = new JsonObject();
						httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);

						if (httpGetResponse.has("error")) {
							response.getWriter().println(httpGetResponse);
						} else {
							response.setContentType("application/json");
							JsonArray scf1Array = httpGetResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
							JsonArray jsonArray = new JsonArray();
							if (scf1Array.size() > 0) {
								for (int i = 0; i < scf1Array.size(); i++) {
									JsonObject scf1Obj = scf1Array.get(i).getAsJsonObject();
									if (scf1Obj.get("EligibilityTypeID").isJsonNull() || !scf1Obj.get("EligibilityTypeID").getAsString().equalsIgnoreCase("AML")) {
										jsonArray.add(scf1Obj);
									}
								}
								JsonObject resultsObj = new JsonObject();
								resultsObj.add("results", jsonArray);
								JsonObject dObj = new JsonObject();
								dObj.add("d", resultsObj);
								response.getWriter().println(dObj);
							} else {
								response.getWriter().println(httpGetResponse);
							}
						}
					}else if ( request.getPathInfo().equalsIgnoreCase("/SCF2")) { // SCF2
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCF2?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCF2?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCF2?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/SupplyChainFinanceDiscInvoices")) { // SupplyChainFinanceDiscInvoices
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SupplyChainFinanceDiscInvoices?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SupplyChainFinanceDiscInvoices?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SupplyChainFinanceDiscInvoices?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/CF_MIS")) { // CF_MIS
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"CF_MIS?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"CF_MIS?"+updatedQuery;
								} else {
									executeURL = oDataURL+"CF_MIS?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							//Started on 21-04-2020
						}else if (request.getPathInfo().equalsIgnoreCase("/SCF_DLR_OS")) { // SCF_DLR_OS
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCF_DLR_OS?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {

									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCF_DLR_OS?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCF_DLR_OS?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BPCNTP")) { // BPCNTP
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"BPCNTP";//?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									updatedQuery = filterQuery;//+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									if (debug)
										response.getWriter().println("updatedQuery: "+updatedQuery);
									
									queryString = queryString.replaceAll(filterQuery , updatedQuery);
									executeURL = oDataURL+"BPCNTP?"+queryString;
								} else {
									executeURL = oDataURL+"BPCNTP?"+queryString;//+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								}
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/PAYM")) { // PAYM
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"PAYM?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"PAYM?"+updatedQuery;
								} else {
									executeURL = oDataURL+"PAYM?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/Payments")) { // Payments
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"Payments?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"Payments?"+updatedQuery;
								} else {
									executeURL = oDataURL+"Payments?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/PAYMI")) { // PAYMI
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"PAYMI";
							} else {
								executeURL = oDataURL+"PAYMI?"+queryString;
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/PaymentItemDetails")) { // PaymentItemDetails
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"PaymentItemDetails";
							} else {
								executeURL = oDataURL+"PaymentItemDetails?"+queryString;
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/UACCNT")) { // UACCNT
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"UACCNT?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"UACCNT?"+updatedQuery;
								} else {
									executeURL = oDataURL+"UACCNT?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}			
						}else if (request.getPathInfo().equalsIgnoreCase("/COMMN")) { // COMMN
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"COMMN?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"COMMN?"+updatedQuery;
								} else {
									executeURL = oDataURL+"COMMN?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/Customers")) { // Customers
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"Customers?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"Customers?"+updatedQuery;
								} else {
									executeURL = oDataURL+"Customers?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/Aggregators")) { // Aggregators
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"Aggregators?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"Aggregators?"+updatedQuery;
								} else {
									executeURL = oDataURL+"Aggregators?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/DCCNFG")) { // DCCNFG
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"DCCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"DCCNFG?"+updatedQuery;
								} else {
									executeURL = oDataURL+"DCCNFG?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/OPCNFG")) { // OPCNFG
							
							debug= false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"OPCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"OPCNFG?"+updatedQuery;
								} else {
									executeURL = oDataURL+"OPCNFG?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 23-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}	
							//Arif Shaik ************** 22-04-2020
						}else if (request.getPathInfo().equalsIgnoreCase("/SCCNFG")) { // SCCNFG
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCCNFG?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCCNFG?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}
						else if (request.getPathInfo().equalsIgnoreCase("/IDCNFG")) { // IDCNFG
							
							debug= false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"IDCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"IDCNFG?"+updatedQuery;
								} else {
									executeURL = oDataURL+"IDCNFG?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
//							response.getWriter().println("executeURL: "+executeURL);
							
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							response.getWriter().println("httpGetResponse: "+httpGetResponse);
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/UserAccountsByPartner")) { // UserAccountsByPartner
							
							debug= false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"UserAccountsByPartner?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"UserAccountsByPartner?"+updatedQuery;
								} else {
									executeURL = oDataURL+"UserAccountsByPartner?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/UserRegistrationByPartner")) { // UserRegistrationByPartner
							
							debug= false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"UserRegistrationByPartner?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"UserRegistrationByPartner?"+updatedQuery;
								} else {
									executeURL = oDataURL+"UserRegistrationByPartner?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							
						}else if (request.getPathInfo().equalsIgnoreCase("/eSignContractSigners")) { // eSignContractSigners
							// debug= true;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"eSignContractSigners?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								if (debug)
									response.getWriter().println("IF executeURL: "+executeURL);
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									if (debug)
										response.getWriter().println("elseif updatedQuery: "+updatedQuery);
									executeURL = oDataURL+"eSignContractSigners?"+updatedQuery;
									if (debug)
										response.getWriter().println("elseif executeURL: "+executeURL);
								} else {
									executeURL = oDataURL+"eSignContractSigners?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
									if (debug)
										response.getWriter().println("elseelse executeURL: "+executeURL);
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							if (debug)
								response.getWriter().println("httpGetResponse: "+httpGetResponse);

							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/eSignContracts")) { // eSignContracts
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"eSignContracts?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"eSignContracts?"+updatedQuery;
								} else {
									executeURL = oDataURL+"eSignContracts?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
//							response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							response.getWriter().println("httpGetResponse: "+httpGetResponse);
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BP")) { // BP
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"BP?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"BP?"+updatedQuery;
								} else {
									executeURL = oDataURL+"BP?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BPContactPersons")) { // BPContactPersons
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"BPContactPersons";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"BPContactPersons?"+updatedQuery;
								} else {
									executeURL = oDataURL+"BPContactPersons?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
//							response.getWriter().println("executeURL: "+executeURL);
							//New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							response.getWriter().println("httpGetResponse: "+httpGetResponse);
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if(request.getPathInfo().equalsIgnoreCase("/BPHeaders")) {
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"BPHeaders?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									updatedQuery = queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									executeURL = oDataURL+"BPHeaders?"+updatedQuery;
								} else {
									executeURL = oDataURL+"BPHeaders?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/EnhancementLimitView")) { // EnhancementLimitView
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"EnhancementLimitView?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"EnhancementLimitView?"+updatedQuery;
								} else {
									executeURL = oDataURL+"EnhancementLimitView?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BPRenewals")) { // BPRenewals
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"BPRenewals?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"BPRenewals?"+updatedQuery;
								} else {
									executeURL = oDataURL+"BPRenewals?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by Arif Shaik on 22-04-2020
							JsonObject httpGetResponse = new JsonObject();
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							// Changes end By Arif Shaik
						}else if ( request.getPathInfo().equalsIgnoreCase("/Vendors")) {
							
							String executeURL = "", queryString="";
							JsonObject httpGetResponse = new JsonObject();
							queryString = request.getQueryString();
							loginID = "";
							loginID = commonUtils.getUserPrincipal(request, "name", response);
//							response.getWriter().println("Aggr: "+ aggregatorID);
							
							if(null != queryString && queryString.trim().length() > 0){
								
								queryString = queryString.replaceAll("%27and", "%27%20and");
								queryString = queryString.replaceAll("%27AND", "%27%20AND");
								queryString = queryString.replaceAll("%27OR", "%27%20OR");

								executeURL = oDataURL+"Vendors?"+queryString+"%20and%20LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							}else{
								executeURL = oDataURL+"Vendors?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
							}
							
//							response.getWriter().println("executeURL: "+ executeURL);
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//							response.getWriter().println("httpGetResponse: "+ httpGetResponse);
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BPHeader")) {
							
							String executeURL = "", sessionID ="", CPGuid="";
							debug = false;

							if(request.getMethod().equalsIgnoreCase("POST")){
								payloadRequest =""; loginID="";
								
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
									response.getWriter().println("BPHeaders.inputPayload: "+inputPayload);
								
								CPGuid = inputPayload.getString("CPGuid");
								if (debug)
									response.getWriter().println("customerNo: " + CPGuid);
								
								JsonObject insertHttpResponse = new JsonObject();
								insertHttpResponse = commonUtils.insertIntoBPHeaders(aggregatorID, inputPayload,  request, response, debug);
								if (insertHttpResponse.has("ErrorCode") && insertHttpResponse.get("ErrorCode").getAsString().equalsIgnoreCase("001")) {
									JsonObject result = new JsonObject();
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									result.addProperty("errorCode", insertHttpResponse.get("ErrorCode").getAsString());
									result.addProperty("Message", insertHttpResponse.get("Message").getAsString());
									response.getWriter().println(new Gson().toJson(result));
								}else{
									//success
									response.setContentType("application/json");
									insertHttpResponse.remove("ErrorCode");
									insertHttpResponse.remove("Message");
									response.getWriter().println(insertHttpResponse);
								}
								
							}else if (request.getMethod().equalsIgnoreCase("GET")) {
								
								String  queryString="";

								queryString = request.getQueryString();
								
								if(null != queryString && queryString.trim().length() > 0){
									queryString = queryString.replaceAll("%27and", "%27%20and");
									queryString = queryString.replaceAll("%27AND", "%27%20AND");
									queryString = queryString.replaceAll("%27OR", "%27%20OR");
								}
								
								queryString=queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								
								executeURL = oDataURL+"BPHeaders"+"?"+queryString;
//								response.getOutputStream().println("executeURL: "+executeURL);
								JsonObject httpGetResponse = new JsonObject();
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									response.setContentType("application/json");
									response.getWriter().println(httpGetResponse);
								}
							}else
							{
								response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
								response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
								response.getWriter().println("No query received in the request");
							}
						}else if (request.getPathInfo().equalsIgnoreCase("/BPContactPerson")) {
							
							if (request.getMethod().equalsIgnoreCase("POST")) {
								
								payloadRequest =""; loginID="";
								JsonObject bpCNTPersonJsonResponse = new JsonObject();
								
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
									response.getWriter().println("BPContactPerson.inputPayload: "+inputPayload);
								
								bpCNTPersonJsonResponse = commonUtils.insertIntoBPContactPerson(inputPayload, request, response, debug);
								if (debug)
									response.getWriter().println("BPContactPerson.bpCNTPersonJsonResponse: "+bpCNTPersonJsonResponse);
								
								if ( bpCNTPersonJsonResponse.has("ErrorCode") && bpCNTPersonJsonResponse.get("ErrorCode").getAsString().equalsIgnoreCase("") ) {
									
									//success
									response.setContentType("application/json");
									bpCNTPersonJsonResponse.remove("ErrorCode");
									bpCNTPersonJsonResponse.remove("Message");
									response.getWriter().println(bpCNTPersonJsonResponse);
									
								} else {
									
									JsonObject result = new JsonObject();
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									result.addProperty("errorCode", bpCNTPersonJsonResponse.get("ErrorCode").getAsString());
									result.addProperty("Message", bpCNTPersonJsonResponse.get("Message").getAsString());
									response.getWriter().println(new Gson().toJson(result));
								}
								
							}else {

								response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
								response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
								response.getWriter().println("No query received in the request");
							}							
						}else if(request.getPathInfo().equals("/OpenItems")) {
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {   
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);

							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"OpenItems?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"OpenItems?"+updatedQuery;
								} else {
									executeURL = oDataURL+"OpenItems?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by kamlesh on 08-06-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							// Changes end By kamlesh
						}else if (request.getPathInfo().equals("/OPENITEMS")) {
							
							debug= false;
							JsonObject httpGetResponse = new JsonObject();
							String queryString ="", executeURL="", updatedQuery="";
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"OPENITEMS?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"OPENITEMS?"+updatedQuery;
								} else {
									executeURL = oDataURL+"OPENITEMS?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for kamlesh on 08-06-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							// Changes end By kamlesh
						}else if (request.getPathInfo().equalsIgnoreCase("/SCF3")) {
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SCF3?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AGGRID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SCF3?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SCF3?"+queryString+"&$filter=AGGRID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes for Test Case 4 and 5 done by kamlesh on 08-06-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
							// Changes end By kamlesh
						}else if (request.getPathInfo().equalsIgnoreCase("/SupplyChainFinanceControls")) {
							
							debug= false;
							String queryString ="", executeURL="", updatedQuery="";
							JsonObject httpGetResponse = new JsonObject();
							try {        
								queryString = request.getQueryString();
							} catch (Exception e) {
								queryString ="";
							}
							if (debug)
								response.getWriter().println("queryString: "+queryString);
							
							if ( queryString == null || queryString == "" ) {
								executeURL = oDataURL+"SupplyChainFinanceControls?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
							} else {
								if (queryString.contains("$filter")) {
									String filterQuery = request.getParameter("$filter");
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									
									filterQuery = filterQuery.replaceAll(" ", "%20").replaceAll("'", "%27").replaceAll(":", "%3a");;
									if (debug)
										response.getWriter().println("filterQuery: "+filterQuery);
									if ( ! filterQuery.contains("and") || filterQuery.contains("%20or%20")) {
										updatedQuery = "$filter=("+filterQuery+")%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									} else {
										updatedQuery = "$filter="+filterQuery+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
									}
									executeURL = oDataURL+"SupplyChainFinanceControls?"+updatedQuery;
								} else {
									executeURL = oDataURL+"SupplyChainFinanceControls?"+queryString+"&$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27";
								} 
							}
							if (debug)
								response.getWriter().println("executeURL: "+executeURL);
							// New Changes by kamleh 08-06-2020
							httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
							
							if ( httpGetResponse.has("error") ) {
								response.getWriter().println(httpGetResponse);
							} else {
								response.setContentType("application/json");
								response.getWriter().println(httpGetResponse);
							}
						}else{
							/*response.getWriter().println("loadMetadata getRequestURI: "+request.getRequestURI());
							response.getWriter().println("loadMetadata getRequestURL: "+request.getRequestURL());
							response.getWriter().println("loadMetadata getPathInfo: "+request.getPathInfo());
							response.getWriter().println("loadMetadata getContextPath: "+request.getContextPath());
							response.getWriter().println("loadMetadata getQueryString: "+request.getQueryString());
							response.getWriter().println("loadMetadata getServletContext: "+request.getServletContext());
							response.getWriter().println("loadMetadata serviceURL: "+serviceURL);*/
							
							String pathInfo ="", executeURL ="", errorResponse ="" ;
							JsonObject httpGetResponse = new JsonObject();
							errorResponse = "{ \"error\": { \"code\": \"\", \"message\": { \"lang\": \"en-US\", \"value\": \"Resource not found.\"}}}";
							pathInfo = request.getPathInfo(); 
							if(debug)
								response.getWriter().println("new pathInfo: "+pathInfo);
							if ( pathInfo.contains(""
									+ "")) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.substring(pathInfo.indexOf('/')+1, pathInfo.length());
								
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if(debug)								
									response.getWriter().println("else aggregatorID: "+aggregatorID);
								//sujai
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if((httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
										|| (httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
										|| (httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull()))
									{
										if(httpGetResponse.getAsJsonObject("d").has("AGGRID"))
											responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										else if(httpGetResponse.getAsJsonObject("d").has("AggregatorID"))
											responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										else if(httpGetResponse.getAsJsonObject("d").has("AggregatorId"))
											responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										
										if(debug)								
											response.getWriter().println("else responseAGGRID: "+responseAGGRID);
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("CorporateOpinionReport(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else{
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
										}
									}
									else{
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
								}
							}else if ( pathInfo.contains("SCP(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SupplyChainPartners(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SCP_1(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SupplyChainPartnerPrimarySales(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("ODAccountSummary(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("ODAccountOutstanding(") ) {
								executeURL ="";
								String responseAGGRID ="";
								debug = false;
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								if(debug)
									response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if(debug)
									response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("ENHLMT(") ) {
								
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SCF(") ) {
								
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SCF1(") ) {
								
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SupplyChainFinanceEligibility(") ) {
								
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SCF2(") ) {
								
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("SupplyChainFinanceDiscInvoices(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("CF_MIS(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
								//Added by Arif Shaik on 21-04-2020
							}else if ( pathInfo.contains("SCF_DLR_OS(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("BPCNTP(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									
									response.setContentType("application/json"); 
									response.getWriter().println(httpGetResponse);
								}
							}else if ( pathInfo.contains("PAYM(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("Payments(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("PAYMI(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									response.setContentType("application/json"); 
									response.getWriter().println(httpGetResponse);
								}
							}else if ( pathInfo.contains("PaymentItemDetails(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									response.setContentType("application/json"); 
									response.getWriter().println(httpGetResponse);
									}
							}else if ( pathInfo.contains("UACCNT(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if(pathInfo.contains("COMMN(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("Customers(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("Aggregators(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("DCCNFG(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("OPCNFG(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								} 
								// Started on 22-04-2020
							}else if ( pathInfo.contains("SCCNFG(") ) {
								//Chnages done by Arif Shaik for TestCase 4 and 5 on 22-04-2020
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);

								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("IDCNFG(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
//								response.getWriter().println("executeURL: "+executeURL);
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
//								response.getWriter().println("httpGetResponse: "+httpGetResponse);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								
								} // Shared yesterday 21-04-2020
							}else if ( pathInfo.contains("UserAccountsByPartner(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("UserRegistrationByPartner(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("eSignContractSigners(") ) {
								if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
									debug=true;
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								//sujai
								if(debug)
									response.getWriter().println("eSignContractSigners.aggregatorID: "+aggregatorID);

								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if(debug)
											response.getWriter().println("eSignContractSigners.responseAGGRID: "+responseAGGRID);
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("eSignContracts(")) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("BP(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("BPContactPersons(")) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									response.setContentType("application/json"); 
									response.getWriter().println(httpGetResponse);
								}
							}else if ( pathInfo.contains("BPHeaders(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("EnhancementLimitView(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("BPRenewals(") ) {
								executeURL ="";
								String responseAGGRID ="";
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorID") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json"); 
											response.getWriter().println(httpGetResponse);
										}
										else
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
									else
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
								}
							}else if ( pathInfo.contains("OPENITEMS(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else{
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
										}
									}
									else{
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
								}
							}else if ( pathInfo.contains("OpenItems(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else{
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
										}
									}
									else{
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
								}
							}else if ( pathInfo.contains("SCF3(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AGGRID") && ! httpGetResponse.getAsJsonObject("d").get("AGGRID").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AGGRID").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else{
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
										}
									}
									else{
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
								}
							}else if ( pathInfo.contains("SupplyChainFinanceControls(") ) {
								executeURL ="";
								String responseAGGRID ="";
								
								pathInfo = pathInfo.replace("/", "");
								executeURL = oDataURL+ pathInfo;
								httpGetResponse = commonUtils.executeODataGet(executeURL, authParam, response, debug);
								
								if ( httpGetResponse.has("error") ) {
									response.getWriter().println(httpGetResponse);
								} else {
									if ( httpGetResponse.getAsJsonObject("d").has("AggregatorId") && ! httpGetResponse.getAsJsonObject("d").get("AggregatorId").isJsonNull())
									{
										responseAGGRID = httpGetResponse.getAsJsonObject("d").get("AggregatorId").getAsString();
										if (responseAGGRID.equalsIgnoreCase(aggregatorID)) {
											
											response.setContentType("application/json");
											response.getWriter().println(httpGetResponse);
										}
										else{
											response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
										}
									}
									else{
										response.getWriter().println((JsonObject)new JsonParser().parse(errorResponse.toString()));
									}
								}
							}
							else{
								executeURL = "";
								String queryString = request.getQueryString().replaceAll("%27or", "%27%20or");
								queryString = queryString.replaceAll("%27and", "%27%20and");
								queryString = queryString.replaceAll("%27AND", "%27%20AND");
								queryString = queryString.replaceAll("%27OR", "%27%20OR");
								
								if(request.getPathInfo().equalsIgnoreCase("/BPHeaders")){
									queryString=queryString+"%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
								}
								if(null != queryString && queryString.trim().length()>0){
									String entityInfo  = request.getPathInfo().replace("/", "");;
//									response.getWriter().println("loadMetadata entityInfo: "+entityInfo);
									if(null != entityInfo && entityInfo.trim().length()>0){
										executeURL = oDataURL+entityInfo+"?"+queryString;
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
												response.getOutputStream().print(Data);	
											}
										}else{
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
						}
					}
			}else{
				serviceURL = serviceURL.replaceAll("%20", " ");
				
				// httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
				readRequest = new HttpGet(serviceURL);
				
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
					response.setContentType("application/pdf");
					response.getOutputStream().print(EntityUtils.toString(countEntity));
				}
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(e.getMessage()+"---> in executeODataCall. Full Stack Trace: "+buffer.toString());
		}finally{
			// httpClient.close();
		}
	}
	
	public String getBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;
//	    request.getre
	    try {
	        InputStream inputStream = request.getInputStream();
//	        response.getWriter().println("inputStream: "+inputStream);
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
//		  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().println("getMethod: "+request.getMethod());
		doGet(request, response);
	}

}
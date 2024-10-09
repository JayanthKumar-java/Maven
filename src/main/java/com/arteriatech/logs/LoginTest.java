package com.arteriatech.logs;

import java.io.IOException;
import java.util.Base64;
import com.arteriatech.pg.CommonUtils;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
// import com.sap.cloud.sdk.cloudplatform.servlet.RequestAccessorFilter;

import io.vavr.control.Try;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class LoginTest
 */
@WebServlet("/LoginTest")
public class LoginTest extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginTest.class);
	private static final String DEST_NAME =  "pugw_utils_op";
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginTest() {
		super();
		// TODO Auto-generated constructor stub
	}
//	Date RequestDate;
//	Date PostDate;
//	int RequestCount = 0;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//set response type as plain text.
		response.setContentType("application/json");
		
		// Check for a logged in user
		System.out.println("1. doGet function starts");
		System.out.println("2 Inside doGet for GetLoginID.getCurrentToken.getJwt: " +AuthTokenAccessor.getCurrentToken().getJwt());
		System.out.println("3 Inside doGet for GetLoginID.getCurrentPrincipal: "+PrincipalAccessor.getCurrentPrincipal());
		System.out.println("4 Inside doGet for GetLoginID.getFallbackPrincipal: "+PrincipalAccessor.getFallbackPrincipal());
//		boolean debug = false;
		try{
			CommonUtils utils = new CommonUtils();
			utils.getUserInfo(request, response);
			if (PrincipalAccessor.getCurrentPrincipal() != null)
			{
				String loginID = PrincipalAccessor.getCurrentPrincipal().getPrincipalId();
				System.out.println("5 LoginID:"+loginID);
				if(request.getParameter("debug") != null &&  request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("getCurrentToken: "+AuthTokenAccessor.getCurrentToken().getJwt());
					response.getWriter().println("getCurrentPrincipal: "+PrincipalAccessor.getCurrentPrincipal());
					response.getWriter().println("getFallbackPrincipal: "+PrincipalAccessor.getFallbackPrincipal());
					response.getWriter().println("loginID: "+loginID);
					response.getWriter().println("getAuthorizations: "+PrincipalAccessor.getCurrentPrincipal().getAuthorizations());
					// response.getWriter().println("getPrincipalId: "+PrincipalAccessor.getCurrentPrincipal().getPrincipalId());
					// response.getWriter().println("getPrincipalId: "+PrincipalAccessor.getCurrentPrincipal().getName());
					response.getWriter().println("getAttribute,name: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("name"));

					response.getWriter().println("User name: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("name"));
				    response.getWriter().println("User lastname: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("lastname"));
				    response.getWriter().println("User firstname: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("firstname"));
				    response.getWriter().println("User saml2AssertionGroups: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("saml2AssertionGroups"));
				    response.getWriter().println("User roleProvider: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("roleProvider"));
				    response.getWriter().println("User getAttribute.groups: "+PrincipalAccessor.getCurrentPrincipal().getAttribute("groups"));

				}
				
				if(loginID == null)
				{
					response.getWriter().println("Error: No Login ID found");
				}else {
					//get destination URL
					String url = getDestinationURL(request, response);
					if(request.getParameter("debug") != null &&  request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("url: "+url);
					//create user session
					createUserSession(request, response, url, loginID);
				}
			}else{
				// return error message if principal user is null
				response.getWriter().println("Error: No Login ID found");
			}
		}catch (Exception e) {
			response.getWriter().println("Error: No Login ID found");
		}
		LOGGER.info("doGet function end");
	}
	private String getLoginID(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String loginID = null;
		
		try
		{
				String prinicipalUser = request.getUserPrincipal().getName();
				if(prinicipalUser != null)
				{
					loginID = prinicipalUser;
					LOGGER.info("2. user object created");
				}
		}catch (Exception e) {
			LOGGER.error("getLoginId error: " +e.getMessage());
			response.getWriter().println("Error: No Login ID found");
		}
		return loginID;
	}
	private String getLoginID() throws IOException{
		return PrincipalAccessor.getCurrentPrincipal().getPrincipalId();
	}

	private String getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		LOGGER.info("4. destination name from request: "+destinationName);

		//check destination null condition
		if (destinationName == null) {
			destinationName = DEST_NAME;
		}
		if(request.getParameter("debug") != null &&  request.getParameter("debug").equalsIgnoreCase("true"))
			response.getWriter().println("destinationName: "+destinationName);

		String url = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
//			Context ctxConnConfgn = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
//			// get destination configuration for "myDestinationName"
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
            DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter.augmenter()
                    .tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_THEN_EXCHANGE)).build();
            Try<Destination> destinationTry = DestinationAccessor.getLoader().tryGetDestination(destinationName, options);
            Destination destination = destinationTry.get();
//			response.getWriter().println("destinationTry: "+destinationTry);
			LOGGER.info("5. destination configuration object created");
			// Get the destination URL
			if(destination != null)
			{
				url = destination.get("URL").get().toString();
			}
		} catch (Exception e) {
			// Lookup of destination failed
			response.getWriter().println("Exception in Destination: "+e);
			String errorMessage = "Lookup of destination failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have the destination "
					+ destinationName + " configured.";
			LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		}
		return url;
	}

	private void createUserSession(HttpServletRequest request, HttpServletResponse response, String url, String loginID) throws IOException{
		LOGGER.info("6.create user session function starts here");
		//get soap xml string
		String soapXMLMsg, stringEntityMsg="", destinationMsg="", entityMsg="", setGWResponseMsg="";
		String soapXML = getSoapXML(request, loginID);
		soapXMLMsg = "soapXMLMsg"+soapXML;
//		AuthenticationHeader principalPropagationHeader = null;
		boolean debug=false;
		// CloseableHttpClient closableHttpClient = null;

		String destinationName = request.getParameter("destname");
		LOGGER.info("4. destination name from request: "+destinationName);

		//check destination null condition
		if (destinationName == null) {
			destinationName = DEST_NAME;
		}

		if(request.getParameter("debug") != null &&  request.getParameter("debug").equalsIgnoreCase("true"))
			response.getWriter().println("createUserSession.destinationName: "+destinationName);

		try{
			//Creates a StringEntity with the specified content and charset. The MIME type to UTF-8.
			/*JsonObject result = new JsonObject();
			result.addProperty("Request", soapXML);
			response.getWriter().print(new Gson().toJson(result));*/
			if(request.getParameter("debug") != null &&  request.getParameter("debug").equalsIgnoreCase("true"))
				debug=true;

			StringEntity stringEntity = new StringEntity(soapXML, "UTF-8");
			stringEntityMsg = "stringEntityMsg"+stringEntity;
			stringEntity.setChunked(true);
			//get http destination
//			Context tenCtx = new InitialContext();
//			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
//			if(debug){
//				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}

//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
//			HttpDestination destConfiguration = DestinationAccessor.getDestination(destinationName).asHttp();
//			HttpClient client = HttpClientAccessor.getHttpClient(destConfiguration);
//			if (destConfiguration == null) {
//				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
//						 String.format("Destination %s is not found. Hint:"
//								 + " Make sure to have the destination configured.", destinationName));
//	        }
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
			String authMethod="", destURL="", userName="", password="",authParam="", basicAuth="";

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Bearer " + authParam;
			}else/* {
				principalPropagationHeader = getPrincipalPropagationAuthHdr(response, debug);
			} */

//			HttpDestination  destination = getHTTPDestination(request, response);
//			destinationMsg = "destinationMsg"+destination;
			/*if(destination != null)
			{*/
				//Execute http post and get entity object
				if(debug)
				{
//					response.getWriter().println("destination="+destination);
					response.getWriter().println("/n"+"stringEntity="+stringEntity);
					response.getWriter().println("/n"+"url="+url);
				}
//				HttpEntity entity = executeHttpPost(destination, stringEntity, url, response);
				HttpEntity entity = executeHttpPost(request, stringEntity, url, response, debug);
				entityMsg = "entityMsg"+entity;
				if(debug)
				{
//					response.getWriter().println("entityMsg="+entity.g);
					response.getWriter().println("entityMsg="+entityMsg);
//					response.getWriter().println("entity="+entity);
					response.getWriter().println("resStr="+EntityUtils.toString(entity));
				}
				//set httppost response to the servlet response
				setGWHttpResponse(entity, response, debug);
				setGWResponseMsg = "setGWResponseMsg";
				//R&D
//				RequestCount = 0;
			/*}else {
				setBlankHttpResponse(response);
			}*/
		}catch(Exception e){
			// Handle errors
			LOGGER.error("Error at user session create", e);
			LOGGER.error("Java Good Bye issue:"+soapXMLMsg+"\n"+stringEntityMsg+"\n"+destinationMsg+"\n"+entityMsg+"\n"+setGWResponseMsg);
			response.getWriter().println("Error at user session create: " +  e.getMessage());

			//R&D
//			RequestCount++;
//			if(RequestCount == 1)
//			{
//				RequestDate = Calendar.getInstance().getTime();
//				Calendar.getInstance().add(Calendar.SECOND, 30);
//				PostDate = Calendar.getInstance().getTime();
//			}
//
//			if(RequestDate != PostDate)
//			{
//				createUserSession(request, response, url, loginID);
//			}
//			else
//			{
//				response.getWriter().println("Error at user session create: " +  e.getMessage());
//				response.getWriter().println("Technical error encountered. Please retry.");
//			}
		}
	}

	public HttpEntity executeHttpPost(HttpServletRequest request, StringEntity stringEntity, String destUrl, HttpServletResponse response, boolean debug) throws IOException{
		HttpEntity entity = null;
		String userName="", password="", authParam="";
//		CloseableHttpClient httpClient = null;
		try{
			String destinationName = request.getParameter("destname");
			LOGGER.info("4. destination name from request: "+destinationName);

			//check destination null condition
			if (destinationName == null) {
				destinationName = DEST_NAME;
			}

//			Context tenCtx = new InitialContext();
//			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
//			if(debug){
//				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}

//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
			
			System.out.println("Inside executeHttpPost");
			DestinationOptions options = DestinationOptions
					.builder()
					.augmentBuilder(
							ScpCfDestinationOptionsAugmenter
									.augmenter()
									.tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_THEN_EXCHANGE))
					.build();
			Try<Destination> destinationTry = DestinationAccessor.getLoader().tryGetDestination(destinationName, options);
			// HttpClient client = HttpClientAccessor.getHttpClient(destinationTry.get().asHttp());
			Destination destConfiguration = destinationTry.get();
			HttpDestination httpDestConfiguration = destinationTry.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);
//			String proxyType = destConfiguration.getProperty("ProxyType");
			userName = destConfiguration.get("User").toString();
			password = destConfiguration.get("Password").toString();
			authParam = userName + ":"+ password ;
			String basicAuth = "Bearer " + authParam;
//
//			if(debug){
//				response.getWriter().println("executeHttpPost.destUrl: "+ destUrl);
//		        response.getWriter().println("executeHttpPost.proxyType: "+ proxyType);
//		        response.getWriter().println("executeHttpPost.userName: "+ userName);
//		        response.getWriter().println("executeHttpPost.password: "+ password);
//		        response.getWriter().println("executeHttpPost.authParam: "+ authParam);
//		        response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
//			}
//
//
//	        String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
//	        int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
//	        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
//	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
//
//	        CredentialsProvider provider = new BasicCredentialsProvider();
//	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
//	        provider.setCredentials(AuthScope.ANY, credentials);
//
//	        httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        HttpPost sessionRequest = new HttpPost(destUrl);
//			sessionRequest.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        sessionRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			sessionRequest.setHeader("content-type", "text/xml; charset=UTF-8");
			sessionRequest.setHeader("Accept", "text/xml");
			sessionRequest.setEntity(stringEntity);

//			HttpResponse httpResponse = httpClient.execute(sessionRequest);
			HttpResponse httpResponse = client.execute(sessionRequest);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(debug) {
				response.getWriter().println("statusCode: "+statusCode);
			}
			entity = httpResponse.getEntity();
//			if(debug){
//		        response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//		        response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//		        response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}
		}catch (RuntimeException e) {
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
			if(debug){
				response.getWriter().println("Error at posting xmlgetLocalizedMessage: " +  e.getLocalizedMessage());

				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeHttpPost.RuntimeException:"+buffer.toString());
			}
		} catch (Exception e) {
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
			if(debug){
				response.getWriter().println("Error at posting xmlgetLocalizedMessage: " +  e.getLocalizedMessage());
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeHttpPost.Exception:"+buffer.toString());
			}
		}
		return entity;
	}

	/*private HttpEntity executeHttpPost(HttpDestination destination, StringEntity stringEntity, String url, HttpServletResponse response) throws IOException{
		HttpPost httpPost = null;
		HttpEntity entity = null;
		try{

			LOGGER.info("9. execute http post");
			// Create HTTP client
			HttpClient httpClient = destination.createHttpClient();
			httpPost = new HttpPost(url);
			httpPost.setEntity(stringEntity);
			httpPost.setHeader("content-type", "text/xml; charset=UTF-8");
			httpPost.setHeader("Connection", "close");
//			httpPost.addHeader("Accept", "text/xml");
			// Execute HTTP request
			HttpResponse httpResponse = httpClient.execute(httpPost);
			LOGGER.info("10. get http response after excuting");
			// Copy content from the incoming response to the outgoing response
			entity = httpResponse.getEntity();
			LOGGER.info("11. get http entity from response");
		}catch (RuntimeException e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			LOGGER.error("Error at posting xml", e);
			httpPost.abort();
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
		} catch (DestinationException e) {
			LOGGER.error("Error at posting xml", e);
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
		}
		return entity;
	}*/

	private void setGWHttpResponse(HttpEntity entity,  HttpServletResponse response, boolean debug) throws IOException{
		// Copy content from the incoming response to the outgoing response
		if (entity != null) {
			try {
				//response entity converting string by using entity utils
				String retSrc = EntityUtils.toString(entity);
				LOGGER.info("12. convert entity response to string");
				if(debug)
					response.getWriter().println("retSrc: "+retSrc);
				//Declaring json object
				JsonObject result = new JsonObject();
				//checking the response string is soap xml or not
				if(retSrc.startsWith("<soap-env:Envelope")){
					//Check Type
					//result.addProperty("Error", "Test: "+ retSrc);
					if(retSrc.contains("<Type>")) {
					String responseType = retSrc.split("<Type>")[1].split("</Type>")[0];
						//checking the response is having usersession tag or not
						if(responseType != null && responseType == "E") {
							String errorId = "Error ID: "+retSrc.split("<Id>")[1].split("</Id>")[0] +"\n";
							String errorNumber = errorId+"Error Number: "+retSrc.split("<Number>")[1].split("</Number>")[0] +"\n";
							String errorMessage = errorNumber+"Error Message: "+retSrc.split("<Message>")[1].split("</Message>")[0];

							result.addProperty("Error", errorMessage);
							LOGGER.error("Error at fetching entity response", errorMessage);

						}else if(retSrc.contains("<UserSession>")){
							if(retSrc.contains("<UserSession>")) {
								//getting the usersession id value from usersession tags
								String loginSessionID = retSrc.split("<UserSession>")[1].split("</UserSession>")[0];
								if(loginSessionID.length()>=1){
									//push the key and value to json object
									result.addProperty("UserSession", loginSessionID);
									LOGGER.info("13. user session retrieved");
								}else{
									result.addProperty("Error", "No Usersession ID found (User not Maintained)");
									LOGGER.info("13. user session not found thrown error from BAPI");
								}
							}else {
								result.addProperty("Error", "No Usersession ID found (UserSession is missing)");
								LOGGER.info("13. user session not found thrown error");
							}
						}
					}else{
						result.addProperty("Error", "No Usersession ID found (Response type is missing)\n Backend Error: "+retSrc);
						LOGGER.info("13. user session not found thrown error from BAPI");
					}
				}else{
					result.addProperty("Error", "No Usersession ID found (No Envelope) ---"+retSrc);
					LOGGER.info("13. user session not found thrown error");
				}
				//print the json object
				response.getWriter().print(new Gson().toJson(result));
			}
			catch(Exception e){
				LOGGER.error("Error at fetching entity response", e);
				response.getWriter().println("Error at fetching entity response: " +  e.getMessage());
			}
		}

	}
	private void setHttpResponse(HttpEntity entity,  HttpServletResponse response) throws IOException{
		// Copy content from the incoming response to the outgoing response
		if (entity != null) {
			/*InputStream instream = entity.getContent();
        try {
            byte[] buffer = new byte[COPY_CONTENT_BUFFER_SIZE];
            int len;
            while ((len = instream.read(buffer)) != -1) {
            	response.getOutputStream().write(buffer, 0, len);
            }
            instream.close();
        } catch (IOException e) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically
        	response.getWriter().println("Error: " +  e.getMessage());
        }  finally {
            // Closing the input stream will trigger connection release
            try {
                instream.close();
            } catch (Exception e) {
            	response.getWriter().println("Error: " +  e.getMessage());
            }

        }*/

			try {
				//response entity converting string by using entity utils
				String retSrc = EntityUtils.toString(entity);
				LOGGER.info("12. convert entity response to string");
				//Declaring json object
				JsonObject result = new JsonObject();
				//checking the response string is soap xml or not
				if(retSrc.startsWith("<soap:Envelope")){
					//checking the response is having usersession tag or not
					if(retSrc.contains("<UserSession>")){
						//getting the usersession id value from usersession tags
						String loginSessionID = retSrc.split("<UserSession>")[1].split("</UserSession>")[0];
						if(loginSessionID.length()>=1){
							//push the key and value to json object
							result.addProperty("UserSession", loginSessionID);
							LOGGER.info("13. user session retrieved");
						}else{
							result.addProperty("Error", "No Usersession ID found (User not Maintained)");
							LOGGER.info("13. user session not found thrown error from BAPI");
						}
					}else if(retSrc.contains("<error_text>")){
						//handle error
						String errorMessage = retSrc.split("<error_text>")[1].split("</error_text>")[0];
						result.addProperty("Error", errorMessage);
						LOGGER.error("Error at fetching entity response", errorMessage);
					}

				}
				//checking the response string is html response or not
				else if(retSrc.startsWith("<html>")){
					result.addProperty("Error", retSrc);
					LOGGER.info("13. user session not found thrown error from HCI");
				}
				else{
					result.addProperty("Error", "No Usersession ID found (HCI)");
					LOGGER.info("13. user session not found thrown error");
				}
				//print the json object
				response.getWriter().print(new Gson().toJson(result));
			}
			catch(Exception e){
				LOGGER.error("Error at fetching entity response", e);
				response.getWriter().println("Error at fetching entity response: " +  e.getMessage());
			}
		}
	}

	private void setBlankHttpResponse(HttpServletResponse response) throws IOException{
		//Declaring json object
		JsonObject result = new JsonObject();
		//Add blank user UserSession
		result.addProperty("UserSession", "");
		//print the json object
		response.getWriter().print(new Gson().toJson(result));
	}

//	public AuthenticationHeader getPrincipalPropagationAuthHdr(HttpServletResponse response, boolean debug){
//		AuthenticationHeader principalPropagationHeader = null;
//		try {
//			Context ctxAuthHdr = new InitialContext();
//			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctxAuthHdr.lookup("java:comp/env/myAuthHeaderProvider");
//			principalPropagationHeader = authHeaderProvider.getPrincipalPropagationHeader();
//		} catch (NamingException e) {
//			return null;
//		}
//		return principalPropagationHeader;
//	}

	private String getSoapXML(HttpServletRequest request, String loginID){
		//Get request parameters
		String app = request.getParameter("Application");
		if(app == null){
			app = "";
		}
		String objectName = request.getParameter("Object");
		if(objectName  == null){
			objectName = "";
		}
		String methodType = request.getParameter("Method");
		if(methodType == null){
			methodType = "";
		}
		String fmName = request.getParameter("FmName");
		if(fmName  == null){
			fmName = "";
		}
		String isTestRun = request.getParameter("IsTestRun");
		if(isTestRun  == null){
			isTestRun = "";
		}
		//create soap xml
		String soapXML =  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:sap-com:document:sap:soap:functions:mc-style\">"
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<urn:USRSSNCREATE>"
				+ "<Application>"+app+"</Application>"
				+ "<IsTestRun>"+isTestRun+"</IsTestRun>"
				+ "<LoginID>"+loginID+"</LoginID>"
				+ "<UsrSsnCrtDtl>"
				+ "<Object>"+objectName+"</Object>"
				+ "<Method>"+methodType+"</Method>"
				+ "<FmName>"+fmName+"</FmName>"
				+ "</UsrSsnCrtDtl>"
				+ "</urn:USRSSNCREATE>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		LOGGER.info("7.soap xml created");
		return soapXML;
	}

	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if (destinationName == null) {
			destinationName = DEST_NAME;
		}
		HttpDestination  destination = null;
		try {
			// look up the connectivity configuration API "DestinationFactory"
			Context ctxDestFact = new InitialContext();
			//Get HTTP destination
			DestinationFactory destinationFactory = (DestinationFactory) ctxDestFact.lookup(DestinationFactory.JNDI_NAME);
			if(destinationFactory != null)
			{
				destination = (HttpDestination) destinationFactory.getDestination(destinationName);
				LOGGER.info("8. http destination created");
			}else {
				LOGGER.info("8.1. destinationFactory is null");
			}

		} catch (NamingException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			//        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
			//response.getWriter().println("Error: " +  errorMessage);
		} catch (DestinationNotFoundException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);

			//response.getWriter().println("Error: " +  errorMessage);
		}
		return destination;
	}*/
}

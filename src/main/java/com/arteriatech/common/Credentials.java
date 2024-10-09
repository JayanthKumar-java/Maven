package com.arteriatech.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

import com.arteriatech.pg.CommonUtils;
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
import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;

/**
 * Servlet implementation class Credentials
 */
@WebServlet("/Credentials")
public class Credentials extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op";
	private static final String ARTERIA_CPIDEST =  "ARTECCPI"; 
	CookieStore globalCookieStore = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Credentials() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String encBase64Str = "", aggregatorID = "", cpType="", cpName="", gstnNo="";
		String mandatoryCheckMsg="Mandatory inputs missing: ", userID="", eInvPassword="", inputCPGuid="", csrfToken="", createCredential="", updateCredential="";
		boolean debug = false, mandatoryCheckPass = true, isSuccess = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		
		CommonUtils commonUtils = new CommonUtils();
		JsonObject insertPayload = new JsonObject();
		JsonObject updatePayload = new  JsonObject();
		
		JsonObject responseObj = new JsonObject();
		String decBase64Str="";
		byte decByte[] = null;
		try{
//			debug = true;
			encBase64Str = getGetBody(request, response);
			if(debug)
				response.getWriter().println("payloadRequest: "+encBase64Str);
			decByte = Base64.getDecoder().decode(encBase64Str.getBytes());
			
			decBase64Str = new String(decByte);
			if(debug)
				response.getWriter().println("decBase64Str: "+decBase64Str);
			
			JsonParser parser = new JsonParser();
			JsonObject inputPayload = (JsonObject)parser.parse(decBase64Str);
			
			if(debug)
				response.getWriter().println("inputPayload: "+inputPayload);
			
			if(inputPayload.has("eInvoiceUser") && inputPayload.get("eInvoiceUser").getAsString().trim().length() > 0){
				userID = inputPayload.get("eInvoiceUser").getAsString().trim();
			}else{
				mandatoryCheckMsg = mandatoryCheckMsg+"eInvoiceUser";
			}
			
			if(inputPayload.has("eInvoicePassword") && inputPayload.get("eInvoicePassword").getAsString().trim().length() > 0){
				eInvPassword = inputPayload.get("eInvoicePassword").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", eInvoicePassword";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"eInvoicePassword";
				}
			}
			
			if(inputPayload.has("CPGuid") && inputPayload.get("CPGuid").getAsString().trim().length() > 0){
				inputCPGuid = inputPayload.get("CPGuid").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPGuid";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPGuid";
				}
			}
			
			if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
				mandatoryCheckPass = false;
			}
			
			if(inputPayload.has("CPType") && inputPayload.get("CPType").getAsString().trim().length() > 0){
				cpType = inputPayload.get("CPType").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPType";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPType";
				}
			}
			
			if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
				mandatoryCheckPass = false;
			}
			
			if(debug){
				response.getWriter().println("mandatoryCheckPass: "+mandatoryCheckPass);
				response.getWriter().println("userID: "+userID);
				response.getWriter().println("inputCPGuid: "+inputCPGuid);
				response.getWriter().println("cpType: "+cpType);
			}
			
			if(mandatoryCheckPass){
				if(debug)
					response.getWriter().println("mandatoryCheckMsg: "+mandatoryCheckMsg);
				
				String customerFilter ="", loginMethod="", destURL="", sessionID="", customerService="", service="";
				boolean isRequestFromCloud = false;
				
//				DestinationConfiguration destConfiguration = null;
				Destination destConfiguration = null;

				destConfiguration = getDestinationURL(request, response);
				destURL = destConfiguration.get("URL").get().toString();
				
				if(debug){
					response.getWriter().println("doPost.destURL: "+ destURL);
				}
				
				if (destURL.contains("service.xsodata")){
					isRequestFromCloud = true;
				}else{
					isRequestFromCloud = false;
				}
				
				String loginID = commonUtils.getUserPrincipal(request, "name", response);
				
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "ARTECCPI");
				
				if (debug){
					response.getWriter().println("loginID: "+loginID);
					response.getWriter().println("aggregatorID: "+aggregatorID);
				}
				JsonObject userCustomerObj = new JsonObject();
				if(! isRequestFromCloud){
					service = destConfiguration.get("service").get().toString();
					if(debug){
						response.getWriter().println("doPost.service: "+ service);
					}
					
					String sapclient = destConfiguration.get("sap-client").get().toString();
					if (debug)
						response.getWriter().println("sapclient:" + sapclient);
					
					String authMethod = destConfiguration.get("Authentication").get().toString();
					if (debug)
						response.getWriter().println("authMethod:" + authMethod);
					
					if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("url1:" + url);
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
						response.getWriter().println("sessionID1:" + sessionID);
					
					customerFilter = "LoginID eq '"+sessionID+"'";
					customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
					
					customerFilter = customerFilter.replaceAll("%26", "&");
					customerFilter = customerFilter.replaceAll("%3D", "=");
					if (debug)
						response.getWriter().println("customerFilter: "+customerFilter);
					
					if(null != service && service.equalsIgnoreCase("SFGW")){
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ customerFilter;
						}
					}else{
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ customerFilter;
						}
					}
					
					if (debug)
						response.getWriter().println("CustomerService 1: "+customerService);
					
					userCustomerObj = getUserCustomerDetails(request, response, customerService, destConfiguration, debug);
					if (debug)
						response.getWriter().println("op userCustomerObj: "+userCustomerObj);
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
								gstnNo = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("GSTIN").getAsString();
								cpName = aggregatorID+"|"+cpType+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString()
										+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
						
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				}else{
					String formattedStr = "";
					try{
						int number = Integer.parseInt(inputCPGuid);
						formattedStr = ("0000000000" + inputCPGuid).substring(inputCPGuid.length());
						inputCPGuid = formattedStr;
					}catch (NumberFormatException e) {
//						formattedStr = customerNo;
					}
					
					String aggregatorIDFromDest="";
					aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
					
					customerService = destURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27";
					if (debug)
						response.getWriter().println("oc userCustomerObj.executeURL: "+customerService);
					
					userCustomerObj = getCustomerFromCloud(request, response, customerService, debug);
//					if (debug) {
//						response.getWriter().println("oc userCustomerObj: "+userCustomerObj);
//					}
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
								gstnNo = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("GSTIN").getAsString();
//								cpName = aggregatorID+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
								cpName = aggregatorID+"|"+cpType+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString()
										+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				}
				
				if(debug) {
					response.getWriter().println("gstnNo: "+gstnNo);
					response.getWriter().println("cpName: "+cpName);
					response.getWriter().println("userCustomerObj: "+userCustomerObj);
				}
			
				if(isSuccess){
					csrfToken = generateCsrfTokenForCredStore(request, response, debug);
					if(debug)
						response.getWriter().println("csrfToken: "+csrfToken);
					
					if(csrfToken.contains("Exception") || csrfToken.equalsIgnoreCase("Unable to fetch CSRF Token")){
						responseObj.addProperty("Status", "000002");
						responseObj.addProperty("Message", csrfToken);
						responseObj.addProperty("ErrorCode", "J001");
						response.getWriter().println(responseObj);
					}else{
						insertPayload.addProperty("Name", gstnNo);
						insertPayload.addProperty("Kind", "default");
						insertPayload.addProperty("Description", cpName);
						insertPayload.addProperty("User", userID);
						insertPayload.addProperty("Password", eInvPassword);
						insertPayload.addProperty("CompanyId", "");
						
						createCredential = createCredential(request, response, csrfToken, insertPayload, debug);
						
						if(debug)
							response.getWriter().println("createCredential: "+createCredential);
						
						if(createCredential.equalsIgnoreCase("Credentials created successfully")){
							responseObj.addProperty("Status", "000001");
							responseObj.addProperty("Message", createCredential);
							responseObj.addProperty("ErrorCode", "");
							response.getWriter().println(responseObj);
						}else{
							if(createCredential.contains("Exception")){
								responseObj.addProperty("Status", "000002");
								responseObj.addProperty("Message", createCredential);
								responseObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(responseObj);
							}else{
								if(createCredential.trim().equalsIgnoreCase("409")){
									csrfToken = generateCsrfTokenForCredStore(request, response, debug);
									if(debug)
										response.getWriter().println("csrfToken: "+csrfToken);
									
									if(csrfToken.contains("Exception") || csrfToken.equalsIgnoreCase("Unable to fetch CSRF Token")){
										responseObj.addProperty("Status", "000002");
										responseObj.addProperty("Message", csrfToken);
										responseObj.addProperty("ErrorCode", "J001");
										response.getWriter().println(responseObj);
									}else{
										updatePayload.addProperty("Name", gstnNo);
										updatePayload.addProperty("Kind", "default");
										updatePayload.addProperty("Description", cpName);
										updatePayload.addProperty("User", userID);
										updatePayload.addProperty("Password", eInvPassword);
										updatePayload.addProperty("CompanyId", "");
										
										updateCredential = updateCredential(request, response, csrfToken, updatePayload, gstnNo, debug);
										if(debug)
											response.getWriter().println("updateCredential: "+updateCredential);
										
										if(updateCredential.equalsIgnoreCase("Credentials updated successfully")){
											responseObj.addProperty("Status", "000001");
											responseObj.addProperty("Message", updateCredential);
											responseObj.addProperty("ErrorCode", "");
											response.getWriter().println(responseObj);
										}else{
											if(updateCredential.contains("Exception")){
												responseObj.addProperty("Status", "000002");
												responseObj.addProperty("Message", updateCredential);
												responseObj.addProperty("ErrorCode", "J002");
												response.getWriter().println(responseObj);
											}else{
												responseObj.addProperty("Status", "000002");
//												responseObj.addProperty("Message", HttpStatus.getStatusText(Integer.parseInt(updateCredential)));
												responseObj.addProperty("Message", "Updation Failed with http status: "+updateCredential+"-"+HttpStatus.getStatusText(Integer.parseInt(updateCredential)));
												responseObj.addProperty("ErrorCode", "J002");
												response.getWriter().println(responseObj);
											}
										}
									}
								}else{
									responseObj.addProperty("Status", "000002");
									responseObj.addProperty("Message", "Creation Failed with http status: "+createCredential+"-"+HttpStatus.getStatusText(Integer.parseInt(createCredential)));
									responseObj.addProperty("ErrorCode", "J002");
									response.getWriter().println(responseObj);
								}
							}
						}
					}
				}
			}else{
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("Message", mandatoryCheckMsg);
				responseObj.addProperty("ErrorCode", "J001");
				response.getWriter().println(responseObj);
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doPost-Exception Stack Trace: "+buffer.toString());
			}
			
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			responseObj.addProperty("ErrorCode", "JE001");
			response.getWriter().println(responseObj);
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
		} catch (Exception e) {}
		body = jb.toString();
		return body;
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String encBase64Str = "", aggregatorID = "", cpName="", cpType="", gstnNo="";
		String mandatoryCheckMsg="Mandatory inputs missing: ", userID="", eInvPassword="", inputCPGuid="", csrfToken="", updateCredential="";
		boolean debug = false, mandatoryCheckPass = true, isSuccess = false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		
		CommonUtils commonUtils = new CommonUtils();
		JsonObject updatePayload = new JsonObject();
		
		JsonObject responseObj = new JsonObject();
		String decBase64Str="";
		byte decByte[] = null;
		try{
			encBase64Str = getGetBody(request, response);
			if(debug)
				response.getWriter().println("payloadRequest: "+encBase64Str);
			decByte = Base64.getDecoder().decode(encBase64Str.getBytes());
			
			decBase64Str = new String(decByte);
			if(debug)
				response.getWriter().println("decBase64Str: "+decBase64Str);
			
			JsonParser parser = new JsonParser();
			JsonObject inputPayload = (JsonObject)parser.parse(decBase64Str);
			
			if(debug)
				response.getWriter().println("inputPayload: "+inputPayload);
			
			if(inputPayload.has("eInvoiceUser") && inputPayload.get("eInvoiceUser").getAsString().trim().length() > 0){
				userID = inputPayload.get("eInvoiceUser").getAsString().trim();
			}else{
				mandatoryCheckMsg = mandatoryCheckMsg+"eInvoiceUser";
			}
			
			if(inputPayload.has("eInvoicePassword") && inputPayload.get("eInvoicePassword").getAsString().trim().length() > 0){
				eInvPassword = inputPayload.get("eInvoicePassword").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", eInvoicePassword";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"eInvoicePassword";
				}
			}
			
			if(inputPayload.has("CPGuid") && inputPayload.get("CPGuid").getAsString().trim().length() > 0){
				inputCPGuid = inputPayload.get("CPGuid").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPGuid";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPGuid";
				}
			}
			
			if(inputPayload.has("CPType") && inputPayload.get("CPType").getAsString().trim().length() > 0){
				cpType = inputPayload.get("CPType").getAsString();
			}else{
				if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
					mandatoryCheckMsg = mandatoryCheckMsg+", CPType";
				}else{
					mandatoryCheckMsg = mandatoryCheckMsg+"CPType";
				}
			}
			
			if(! mandatoryCheckMsg.equalsIgnoreCase("Mandatory inputs missing: ")){
				mandatoryCheckPass = false;
			}
			
			if(debug){
				response.getWriter().println("mandatoryCheckPass: "+mandatoryCheckPass);
				response.getWriter().println("userID: "+userID);
				response.getWriter().println("inputCPGuid: "+inputCPGuid);
				response.getWriter().println("cpType: "+cpType);
			}
			
			if(mandatoryCheckPass){
				if(debug)
					response.getWriter().println("mandatoryCheckMsg: "+mandatoryCheckMsg);
				
				String customerFilter ="", loginMethod="", destURL="", sessionID="", customerService="", service="";
				boolean isRequestFromCloud = false;
				
//				DestinationConfiguration destConfiguration = null;
				Destination destConfiguration = null;
				destConfiguration = getDestinationURL(request, response);
				destURL = destConfiguration.get("URL").get().toString();
				if(debug)
					response.getWriter().println("doGet.destURL.: "+ destURL);
				
				if (destURL.contains("service.xsodata")){
					isRequestFromCloud = true;
				}else{
					isRequestFromCloud = false;
				}
				
				String loginID = commonUtils.getUserPrincipal(request, "name", response);
				
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "ARTECCPI");
				
				if (debug){
					response.getWriter().println("loginID: "+loginID);
					response.getWriter().println("aggregatorID: "+aggregatorID);
				}
				
				if(! isRequestFromCloud){
					JsonObject userCustomerObj = new JsonObject();
					service = destConfiguration.get("service").get().toString();
					if (debug)
						response.getWriter().println("service:" + service);
					
					String sapclient = destConfiguration.get("sap-client").get().toString();
					if (debug)
						response.getWriter().println("sapclient:" + sapclient);
					
					String authMethod = destConfiguration.get("Authentication").get().toString();
					if (debug)
						response.getWriter().println("authMethod:" + authMethod);
					
					if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("url1:" + url);
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
						response.getWriter().println("sessionID1:" + sessionID);
					
					customerFilter = "LoginID eq '"+sessionID+"'";
					customerFilter = URLEncoder.encode(customerFilter, "UTF-8");
					
					customerFilter = customerFilter.replaceAll("%26", "&");
					customerFilter = customerFilter.replaceAll("%3D", "=");
					if (debug)
						response.getWriter().println("customerFilter: "+customerFilter);
					
					if(null != service && service.equalsIgnoreCase("SFGW")){
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ customerFilter;
						}
					}else{
						if(sapclient != null){
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ customerFilter;
						}
						else{
							customerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ customerFilter;
						}
					}
					
					if (debug)
						response.getWriter().println("CustomerService 1: "+customerService);
					
					userCustomerObj = getUserCustomerDetails(request, response, customerService, destConfiguration, debug);
					if (debug)
						response.getWriter().println("op userCustomerObj: "+userCustomerObj);
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
								gstnNo = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("GSTIN").getAsString();
//								cpName = aggregatorID+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
								cpName = aggregatorID+"|"+cpType+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString()
										+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
						
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				}else{
					String formattedStr = "";
					try{
						int number = Integer.parseInt(inputCPGuid);
						formattedStr = ("0000000000" + inputCPGuid).substring(inputCPGuid.length());
						inputCPGuid = formattedStr;
					}catch (NumberFormatException e) {
//						formattedStr = customerNo;
					}
					
					JsonObject userCustomerObj = new JsonObject();
					String aggregatorIDFromDest="";
					aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
					
					customerService = destURL+"UserCustomers?$filter=LoginID%20eq%20%27"+loginID+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27";
					if (debug)
						response.getWriter().println("oc userCustomerObj.executeURL: "+customerService);
					
					userCustomerObj = getCustomerFromCloud(request, response, customerService, debug);
					
					if(userCustomerObj.has("Status") && userCustomerObj.get("Status").getAsString().equalsIgnoreCase("000001")){
						for(int i=0 ; i<userCustomerObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() ; i++){
							if(userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString().equalsIgnoreCase(inputCPGuid)){
								isSuccess = true;
								gstnNo = userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("GSTIN").getAsString();
//								cpName = aggregatorID+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
								cpName = aggregatorID+"|"+cpType+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("CustomerNo").getAsString()
										+"|"+userCustomerObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject().get("Name").getAsString();
							}
						}
						
						if(! isSuccess){
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("Message", "Invalid CPGuid received in the request");
							responseObj.addProperty("ErrorCode", "J001");
							response.getWriter().println(responseObj);
						}
					}else{
						isSuccess = false;
						response.getWriter().println(userCustomerObj);
					}
				}
				
				if(debug) {
					response.getWriter().println("gstnNo: "+gstnNo);
					response.getWriter().println("cpName: "+cpName);
				}
			
				if(isSuccess){
					csrfToken = generateCsrfTokenForCredStore(request, response, debug);
					if(debug)
						response.getWriter().println("csrfToken: "+csrfToken);
					
					if(csrfToken.contains("Exception") || csrfToken.equalsIgnoreCase("Unable to fetch CSRF Token")){
						responseObj.addProperty("Status", "000002");
						responseObj.addProperty("Message", csrfToken);
						responseObj.addProperty("ErrorCode", "J001");
						response.getWriter().println(responseObj);
					}else{
						updatePayload.addProperty("Name", gstnNo);
						updatePayload.addProperty("Kind", "default");
						updatePayload.addProperty("Description", cpName);
						updatePayload.addProperty("User", userID);
						updatePayload.addProperty("Password", eInvPassword);
						updatePayload.addProperty("CompanyId", "");
						
						updateCredential = updateCredential(request, response, csrfToken, updatePayload, gstnNo, debug);
						
						if(debug)
							response.getWriter().println("updateCredential: "+updateCredential);
						
						if(updateCredential.equalsIgnoreCase("Success")){
							responseObj.addProperty("Status", "000001");
							responseObj.addProperty("Message", updateCredential);
							responseObj.addProperty("ErrorCode", "");
							response.getWriter().println(responseObj);
						}else{
							if(updateCredential.contains("Exception")){
								responseObj.addProperty("Status", "000002");
								responseObj.addProperty("Message", updateCredential);
								responseObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(responseObj);
							}else{
								responseObj.addProperty("Status", "000002");
//								responseObj.addProperty("Message", HttpStatus.getStatusText(Integer.parseInt(updateCredential)));
								responseObj.addProperty("Message", "Updation Failed with http status: "+updateCredential+"-"+HttpStatus.getStatusText(Integer.parseInt(updateCredential)));
								responseObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(responseObj);
							}
						}
					}
				}
//				HttpStatus.SC_ACCEPTED
			}else{
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("Message", mandatoryCheckMsg);
				responseObj.addProperty("ErrorCode", "J001");
				response.getWriter().println(responseObj);
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doPut-Exception Stack Trace: "+buffer.toString());
			}
			
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("Message", e.getClass()+"-"+e.getCause()+"-"+e.getMessage());
			responseObj.addProperty("ErrorCode", "JE001");
			response.getWriter().println(responseObj);
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//			LOGGER.info("4. destination name from request: "+destinationName);	
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
//				LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
//				LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
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
				response.getWriter().println("getCustomerFromCloud.userCustomerResponse: "+userCustomerResponse);
			
			if(userCustomerResponse.has("d")){
				if(userCustomerResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0){
					userCustomerResponse.addProperty("Status", "000001");
					userCustomerResponse.addProperty("ErrorCode", "");
					userCustomerResponse.addProperty("Message", "Success");
					return userCustomerResponse;
				}else{
					userCustomerResponse.addProperty("Status", "000002");
					userCustomerResponse.addProperty("ErrorCode", "J001");
					userCustomerResponse.addProperty("Message", "No Records Found in User Customers");
					return userCustomerResponse;
				}
			}else{
				userCustomerResponse.addProperty("Status", "000002");
				userCustomerResponse.addProperty("ErrorCode", "J002");
				userCustomerResponse.add("Message", userCustomerResponse);
				return userCustomerResponse;
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			userCustomerResponse.addProperty("Status", "000002");
			userCustomerResponse.addProperty("ErrorCode", "E001");
			userCustomerResponse.addProperty("Message", "Exception: "+e.getClass()+": "+e.getCause()+". Message "+e.getMessage());
			userCustomerResponse.addProperty("FullTrace", buffer.toString());
			return userCustomerResponse;
		}
	}
	
	private JsonObject getUserCustomerDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String userName="", password="", authParam="", authMethod="", basicAuth=""; 
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject userCustomerJson = new JsonObject();
		
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
//			userCustomersGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			userCustomersGet.setHeader("content-type", "text/xml; charset=UTF-8");
			userCustomersGet.setHeader("Accept", "application/json");
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userCustomersGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			}else{
				userCustomersGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}
			
			// HttpResponse httpResponse = closableHttpClient.execute(userCustomersGet);
			HttpResponse httpResponse = client.execute(userCustomersGet);
			
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
				response.getWriter().println("validateCustomer.statusCode: "+statusCode);
				
				Header headers[] = httpResponse.getAllHeaders();
                for(Header h:headers){
                	response.getWriter().println("Response Headers:" +h.getName() + ": " + h.getValue());
                }
//				response.getWriter().println("validateCustomer.statusCode: "+httpResponse.getv);
			}
			
			customerEntity = httpResponse.getEntity();
			
			if(customerEntity != null)
			{
				String retSrc = EntityUtils.toString(customerEntity);
				
//				if (debug)
//					response.getWriter().println("retSrc: "+retSrc);
				
				JsonParser parser = new JsonParser();
				userCustomerJson = (JsonObject)parser.parse(retSrc);

				if(userCustomerJson.has("d")){
					if(userCustomerJson.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0){
						userCustomerJson.addProperty("Status", "000001");
						userCustomerJson.addProperty("ErrorCode", "");
						userCustomerJson.addProperty("Message", "Success");
						return userCustomerJson;
					}else{
						userCustomerJson.addProperty("Status", "000002");
						userCustomerJson.addProperty("ErrorCode", "J001");
						userCustomerJson.addProperty("Message", "No Records Found in User Customers");
						return userCustomerJson;
					}
				}else{
					userCustomerJson.addProperty("Status", "000002");
					userCustomerJson.addProperty("ErrorCode", "J002");
					userCustomerJson.add("Message", userCustomerJson);
					return userCustomerJson;
				}
			}else{
				userCustomerJson.addProperty("Status", "000002");
				userCustomerJson.addProperty("ErrorCode", "J002");
				userCustomerJson.addProperty("Message", "customerEntity returned null while trying to fetch from UserCustomers");
				return userCustomerJson;
			}
		}catch (RuntimeException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			userCustomerJson.addProperty("Status", "000002");
			userCustomerJson.addProperty("ErrorCode", "E001");
			userCustomerJson.addProperty("Message", "Exception: "+e.getClass()+": "+e.getCause()+". Message "+e.getMessage());
			userCustomerJson.addProperty("FullTrace", buffer.toString());
			return userCustomerJson;
			
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			userCustomerJson.addProperty("Status", "000002");
			userCustomerJson.addProperty("ErrorCode", "E001");
			userCustomerJson.addProperty("Message", "Exception: "+e.getClass()+": "+e.getCause()+". Message "+e.getMessage());
			userCustomerJson.addProperty("FullTrace", buffer.toString());
			return userCustomerJson;
		}
		finally
		{
			// closableHttpClient.close();
		}
	}
	
	public String generateCsrfTokenForCredStore(HttpServletRequest request, HttpServletResponse response, boolean debug) throws IOException{
		String csrfToken="";
		String cpiUrl="", aggregatorID="", userName="", password="", userPass="", executeURL="", authMethod="", authParam="", basicAuth="";
		
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpGet csrfFetch = null;
		HttpEntity csrfFetchEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
		try{
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(ARTERIA_CPIDEST);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(ARTERIA_CPIDEST, options);
			Destination destConfiguration = destinationAccessor.get();

			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", ARTERIA_CPIDEST));
	        }
			// propertyValue = destConfiguration.get("sap-client").get().toString();
			authMethod = destConfiguration.get("Authentication").get().toString();
			cpiUrl = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			executeURL = cpiUrl+"api/v1";
//			executeURL="https://arteriatest.apimanagement.ap1.hana.ondemand.com/FetchCSRFToken";
			if(debug){
				response.getWriter().println("generateCsrfTokenForCredStore.cpiUrl: "+cpiUrl);
				response.getWriter().println("generateCsrfTokenForCredStore.executeURL: "+executeURL);
				response.getWriter().println("generateCsrfTokenForCredStore.userName: "+userName);
				response.getWriter().println("generateCsrfTokenForCredStore.password: "+password);
				response.getWriter().println("generateCsrfTokenForCredStore.authMethod: "+authMethod);
//				response.getWriter().println("generateCsrfTokenForCredStore.authParam: "+authParam);
				response.getWriter().println("generateCsrfTokenForCredStore.authParam: "+basicAuth);
			}
	        
	        HttpClient client = HttpClients.createDefault();
	        csrfFetch = new HttpGet(executeURL);
	        csrfFetch.setHeader("X-CSRF-Token", "Fetch");
	        csrfFetch.setHeader("Authorization",basicAuth);
	        
	        HttpClientContext httpClientContext = HttpClientContext.create();

	        HttpResponse httpResponse = client.execute(csrfFetch, httpClientContext);
	        
	        globalCookieStore = httpClientContext.getCookieStore();
	        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);
	        
			csrfFetchEntity = httpResponse.getEntity();
			
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
	        	response.getWriter().println("generateCsrfTokenForCredStore.httpResponse: "+httpResponse);
				response.getWriter().println("generateCsrfTokenForCredStore.responseCode: "+responseCode);
			}
			
			Header[] headers = httpResponse.getAllHeaders();
            for (Header header: headers) {
            	if(debug)
            		response.getWriter().println("generateCsrfTokenForCredStore Key [" + header.getName() + "], Value[" + header.getValue() + " ]");
            	if(header.getName().equalsIgnoreCase("X-CSRF-Token")){
            		csrfToken = header.getValue();
            	}
	        }
			
            if(csrfToken.equalsIgnoreCase("")){
            	csrfToken = "Unable to fetch CSRF Token";
            }
            
            if(debug){
	            try {
		            List<Cookie> cookies = globalCookieStore.getCookies();
		            if (cookies.isEmpty()) {
		            	response.getWriter().println("No cookies");
		            } else {
		                for (int i = 0; i < cookies.size(); i++) {
		                	response.getWriter().println("Got Cookies toString: " + cookies.get(i).toString());
		                	response.getWriter().println("Got Cookies getName: " + cookies.get(i).getName());
		                	response.getWriter().println("Got Cookies getValue: " + cookies.get(i).getValue());
		                }
		            }
	//	            EntityUtils.consume(httpResponse.getEntity());
		        }catch (Exception e) {
		        	if(debug){
						StackTraceElement element[] = e.getStackTrace();
						StringBuffer buffer = new StringBuffer();
						for(int i=0;i<element.length;i++)
						{
							buffer.append(element[i]);
						}
						response.getWriter().println("generateCsrfTokenForCredStore.cookies-Exception Stack Trace: "+buffer.toString());
						response.getWriter().println("generateCsrfTokenForCredStore.cookies-getMessage: "+e.getMessage());
						response.getWriter().println("generateCsrfTokenForCredStore.cookies-getCause: "+e.getCause());
						response.getWriter().println("generateCsrfTokenForCredStore.cookies-getClass: "+e.getClass());
						response.getWriter().println("generateCsrfTokenForCredStore.cookies-getLocalizedMessage: "+e.getLocalizedMessage());
					}
		        } finally {
	//	        	httpResponse.getClass()
		        }
            }
//			String retSrc = EntityUtils.toString(csrfFetchEntity);
//			response.getWriter().println("generateCsrfTokenForCredStore.retSrc: "+retSrc);
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("generateCsrfTokenForCredStore-Exception Stack Trace: "+buffer.toString());
				response.getWriter().println("generateCsrfTokenForCredStore-getMessage: "+e.getMessage());
				response.getWriter().println("generateCsrfTokenForCredStore-getCause: "+e.getCause());
				response.getWriter().println("generateCsrfTokenForCredStore-getClass: "+e.getClass());
				response.getWriter().println("generateCsrfTokenForCredStore-getLocalizedMessage: "+e.getLocalizedMessage());
			}
			
			csrfToken = "Exception when generating CSRF Token: "+e.getCause()+"::"+e.getMessage();
		}
		return csrfToken;
	}
	
	public String createCredential(HttpServletRequest request, HttpServletResponse response, String csrfToken, 
			JsonObject insertPayload, boolean debug) throws IOException{
		String cpiUrl="", userName="", password="", executeURL="", authMethod="", authParam="", basicAuth="";
		String createStatus="";
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpPost createCredentials = null;
		HttpEntity requestEntity = null;
		HttpEntity createCredentialsEntity = null;
		
		try{
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(ARTERIA_CPIDEST);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
								.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
								.tryGetDestination(ARTERIA_CPIDEST, options);
			Destination destConfiguration = destinationAccessor.get();

			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", ARTERIA_CPIDEST));
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			cpiUrl = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			executeURL = cpiUrl+"api/v1/UserCredentials";
//			executeURL = "https://arteriatest.apimanagement.ap1.hana.ondemand.com/CreateCredentials";
			if(debug){
				response.getWriter().println("createCredential.cpiUrl: "+cpiUrl);
				response.getWriter().println("createCredential.executeURL: "+executeURL);
				response.getWriter().println("createCredential.userName: "+userName);
				response.getWriter().println("createCredential.csrfToken: "+csrfToken);
				response.getWriter().println("createCredential.authMethod: "+authMethod);
				response.getWriter().println("createCredential.basicAuth: "+basicAuth);
			}
	        
	        HttpClient client = HttpClients.createDefault();
	        createCredentials = new HttpPost(executeURL);
	        createCredentials.setHeader("X-CSRF-Token", csrfToken);
	        createCredentials.setHeader("Authorization",basicAuth);
	        createCredentials.setHeader("Content-Type","application/json");
	        requestEntity = new StringEntity(insertPayload.toString());
	        createCredentials.setEntity(requestEntity);

	        HttpClientContext httpClientContext = HttpClientContext.create();
//	        cookieStore = httpClientContext.getCookieStore();
	        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);
	        
	        HttpResponse httpResponse = client.execute(createCredentials, httpClientContext);
	        createCredentialsEntity = httpResponse.getEntity();
			
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
				response.getWriter().println("createCredential.httpResponse: "+httpResponse);
				response.getWriter().println("createCredential.responseCode: "+responseCode);
				response.getWriter().println("createCredential.getStatusLine: "+httpResponse.getStatusLine());
				response.getWriter().println("createCredential.getContent: "+createCredentialsEntity.getContent());
			}
			
			if(debug){
				Header[] headers = httpResponse.getAllHeaders();
	            for (Header header: headers) {
	            	response.getWriter().println("createCredential Key [" + header.getName() + "], Value[" + header.getValue() + " ]");
		        }
			}
			
//            if(responseCode == 200 || responseCode == 201 || responseCode == 202 || responseCode == 203 || responseCode == 204){
            if((responseCode/100) == 2){
            	createStatus = "Credentials created successfully";
            }else{
            	createStatus = ""+responseCode;
            }
            
            String retSrc = EntityUtils.toString(createCredentialsEntity);
            if(debug)
            	response.getWriter().println("createCredentialsEntity.retSrc: "+retSrc);
           /* if(csrfToken.equalsIgnoreCase("")){
            	csrfToken = "Unable to fetch CSRF Token";
            }*/
//			String retSrc = EntityUtils.toString(csrfFetchEntity);
//			response.getWriter().println("generateCsrfTokenForCredStore.retSrc: "+retSrc);
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("createCredential-Exception Stack Trace: "+buffer.toString());
				response.getWriter().println("createCredential-getMessage: "+e.getMessage());
				response.getWriter().println("createCredential-getCause: "+e.getCause());
				response.getWriter().println("createCredential-getClass: "+e.getClass());
				response.getWriter().println("createCredential-getLocalizedMessage: "+e.getLocalizedMessage());
			}
			
			createStatus = "Exception when creating: "+e.getCause()+"::"+e.getMessage();
		}
		return createStatus;
	}
	
	public String updateCredential(HttpServletRequest request, HttpServletResponse response, String csrfToken, 
			JsonObject updatePayload, String gstnNo, boolean debug) throws IOException{
		String cpiUrl="", userName="", password="", executeURL="", authMethod="", authParam="", basicAuth="";
		String updateStatus="";
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpPut updateCredentials = null;
		HttpEntity requestEntity = null;
		HttpEntity updateCredentialsEntity = null;
		
		try{
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(ARTERIA_CPIDEST);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
								.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
								.tryGetDestination(ARTERIA_CPIDEST, options);
			Destination destConfiguration = destinationAccessor.get();
			
			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", ARTERIA_CPIDEST));
	        }
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			cpiUrl = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			executeURL = cpiUrl+"api/v1/UserCredentials('"+gstnNo+"')";
			if(debug){
				response.getWriter().println("updateCredential.executeURL: "+executeURL);
				response.getWriter().println("updateCredential.userName: "+userName);
				response.getWriter().println("updateCredential.csrfToken: "+csrfToken);
				response.getWriter().println("updateCredential.authMethod: "+authMethod);
				response.getWriter().println("updateCredential.basicAuth: "+basicAuth);
			}
	        
	        HttpClient client = HttpClients.createDefault();
	        updateCredentials = new HttpPut(executeURL);
	        updateCredentials.setHeader("X-CSRF-Token", csrfToken);
	        updateCredentials.setHeader("Authorization",basicAuth);
	        updateCredentials.setHeader("Content-Type","application/json");
	        requestEntity = new StringEntity(updatePayload.toString());
	        updateCredentials.setEntity(requestEntity);
	        
	        HttpClientContext httpClientContext = HttpClientContext.create();
//	        cookieStore = httpClientContext.getCookieStore();
	        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);

	        HttpResponse httpResponse = client.execute(updateCredentials, httpClientContext);
	        updateCredentialsEntity = httpResponse.getEntity();
			
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if(debug){
				response.getWriter().println("updateCredential.responseCode: "+responseCode);
				response.getWriter().println("updateCredential.getStatusLine: "+httpResponse.getStatusLine());
				response.getWriter().println("updateCredential.getContent: "+updateCredentialsEntity.getContent());
				response.getWriter().println("updateCredential.httpResponse: "+httpResponse);
			}
			
			if(debug){
				Header[] headers = httpResponse.getAllHeaders();
	            for (Header header: headers) {
	            	response.getWriter().println("updateCredential Key [" + header.getName() + "], Value[" + header.getValue() + " ]");
		        }
			}
			
            if((responseCode/100) == 2){
            	updateStatus = "Credentials updated successfully";
            }else{
            	updateStatus = ""+responseCode;
            }
            
            String retSrc = EntityUtils.toString(updateCredentialsEntity);
            if(debug)
            	response.getWriter().println("updateCredential.retSrc: "+retSrc);
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("updateCredential-Exception Stack Trace: "+buffer.toString());
				response.getWriter().println("updateCredential-getMessage: "+e.getMessage());
				response.getWriter().println("updateCredential-getCause: "+e.getCause());
				response.getWriter().println("updateCredential-getClass: "+e.getClass());
				response.getWriter().println("updateCredential-getLocalizedMessage: "+e.getLocalizedMessage());
			}
			
			updateStatus = "Exception when Updating: "+e.getCause()+"::"+e.getMessage();
		}
		return updateStatus;
	}
}

package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Base64;
import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

import io.vavr.control.Try;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet {

	//// TenantContex instance injection. It is used to get the consumer subaccount name.
	 @Resource
	 private TenantContext  tenantContext;
	 private static final long serialVersionUID = 1L;
	 private static final String ON_PREMISE_PROXY = "OnPremise";
	 private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	 
	 private static final Logger LOGGER = LoggerFactory.getLogger(TestServlet.class);
	 /**
	  * @see HttpServlet#HttpServlet()
	  */
	 public TestServlet() {
		 super();
		 // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String destURL="", aggregatorID="", executeURL="", userName="", password="", authParam="" , proxyType="", sessionID="";
		boolean debug = true;
		CommonUtils commonUtils = new CommonUtils();
		
		HttpURLConnection urlConnection = null;
		String destinationName = request.getParameter("destname");
		if (destinationName == null) {
			destinationName = "TestDest";
		}
		try {
//			 Context ctx = new InitialContext();
//			 ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			 DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
			 DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			 Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			 Destination destConfiguration = destinationAccessor.get();
			 if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", destinationName));
	                return;
	            }
		
			 // Get the destination Properties
			 destURL = destConfiguration.get("URL").get().toString();
//			 aggregatorID = destConfiguration.get("AggregatorID");
			 userName = destConfiguration.get("User").get().toString();
			 password = destConfiguration.get("Password").get().toString();
			 authParam = userName + ":"+ password ;
			
			if(debug) {
				 response.getWriter().println("destURL:-  "+ destURL);
//				 response.getWriter().println("aggregatorID:-  "+ aggregatorID);
				 response.getWriter().println("userName:-  " + userName);
				 response.getWriter().println("password:-  "+password);
			}
			// Show details of logged in user
			getUserAttributes(response, request.getUserPrincipal()); 
			
//			generateOrderID(request, response, debug);
			
			UserProvider userProvider = UserManagementAccessor.getUserProvider();
			User user = userProvider.getUser(request.getUserPrincipal().getName());
			
			String loginID = user.getName();
			response.getWriter().println("loginID: "+loginID);
			String sessionGenURL = commonUtils.getDestinationURL(request, response, "URL");
			sessionID = createUserSession(request, response, sessionGenURL, loginID, debug);
			
			validateCustomer(request, response, sessionID, "100186", debug);
			
			bpHeaderDollarExpand(response, request, "1000", sessionID, debug);
			pgPaymentConfigs(response, request, "B2BIZ", sessionID, debug);
			
			/*// Get the destination URL
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			 
			URL url = new URL(destURL);
			
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type","application/json");
			urlConnection.setRequestProperty("Accept","application/json");
			urlConnection.setRequestProperty("Authorization", basicAuth);
			urlConnection.setDoInput(true);

			// Copy content from the incoming response to the outgoing response
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer responseStrBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseStrBuffer.append(inputLine);
			}	
			
			response.getWriter().println("responseStrBuffer: "+responseStrBuffer.toString());
			JsonParser parser = new JsonParser();
			JsonObject	jsonObj = (JsonObject)parser.parse(responseStrBuffer.toString());
			 
			if (debug) 
				response.getWriter().println("jsonObj:-  " + jsonObj); */
			 
		} catch (Exception e) {
			 // Connectivity operation failed
            String errorMessage = "Connectivity operation failed with reason: "
                    + e.getMessage()
                    + ". See "
                    + "logs for details. Hint: Make sure to have an HTTP proxy configured in your "
                    + "local environment in case your environment uses "
                    + "an HTTP proxy for the outbound Internet "
                    + "communication.";
            LOGGER.error("Connectivity operation failed", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorMessage);
            
            StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("doGet.Exception: "+buffer.toString());
		}
	        
	}
	private String getUserAttributes(HttpServletResponse response, Principal principal) throws PersistenceException,UnsupportedUserAttributeException, IOException {
	
		try {
			// Get user from user storage based on principal name
			UserProvider userProvider = UserManagementAccessor.getUserProvider();
			User user = userProvider.getUser(principal.getName());
			
			// Extract and return user name and e-mail address if present
			String firstName = user.getAttribute("firstname");
			String lastName = user.getAttribute("lastname");
			String eMail = user.getAttribute("email");

			response.getWriter().println("getUserAttributes.firstName: "+firstName);
			response.getWriter().println("getUserAttributes.lastName: "+lastName);
			response.getWriter().println("getUserAttributes.eMail: "+eMail);
			response.getWriter().println("getUserAttributes.Name: "+user.getName());
			response.getWriter().println("getUserAttributes.getGroups: "+user.getGroups());
			response.getWriter().println("getUserAttributes.getGroups().size(): "+user.getGroups().size());
			response.getWriter().println("getUserAttributes.getGroups().toString(): "+user.getGroups().toString());
			response.getWriter().println("getUserAttributes.getRoles: "+user.getRoles());
			response.getWriter().println("getUserAttributes.getRoles.size(): "+user.getRoles().size());
			response.getWriter().println("getUserAttributes.getRoles.toString(): "+user.getRoles().toString());
			response.getWriter().println("getUserAttributes.getCountry: "+user.getLocale().getCountry());
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("getUserAttributes.Exception: "+buffer.toString());
		}
		return "";
	}
	
	public void generateOrderID(HttpServletRequest request, HttpServletResponse response, boolean debug)  throws IOException, JSONException 
	{
		// TODO Auto-generated method stub
		response.getWriter().println("doGet method");
		String apiKey="", secret="";
		try{
			apiKey = "rzp_test_UpMmJk4TXSwHgM";
			secret =  "UgVNyquPkxVG60Wx8wXt6HbU";
			response.getWriter().println("apiKey: "+apiKey);
			response.getWriter().println("secret: "+secret);
			RazorpayClient razorpayClient = new RazorpayClient(apiKey, secret);
//			RazorpayClient.
//			OrderClient client = new OrderClient();
			  
			JSONObject orderRequest = new JSONObject();
//			// amount in paise
			orderRequest.put("amount", 50000);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "RCPTID43");
			orderRequest.put("payment_capture", true);
			  
			Order order = razorpayClient.Orders.create(orderRequest);
				 
			if(order.has("id")){
				response.getWriter().println("true");
			}else {
				response.getWriter().println("false");
			}
			  
		    response.getWriter().println("order: "+order);
			response.getWriter().println("order.toJson: "+order.toJson());
			response.getWriter().println("order.toString: "+order.toString());
			  
			response.getWriter().println("amount:"+order.get("amount"));
			response.getWriter().println("currency:"+order.get("currenct"));
			response.getWriter().println("receipt:"+order.get("receipt")); 
			response.getWriter().println("payment_capture:"+order.get("payment_capture"));
			
			response.getWriter().println("123");
			String orderID = order.get("id").toString();
			response.getWriter().println("orderID: "+orderID);
			/*Order order1 = razorpayClient.Orders.fetch("order_id");
			List<Order> orders = razorpayClient.Orders.fetchAll();
			List<Payment> payments = razorpayClient.Orders.fetchPayments("order_id");*/
			   
		}catch(RazorpayException e){
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("RazorpayException:"+buffer.toString());
		} catch (JSONException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("JSONException:"+buffer.toString());
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception:"+buffer.toString());
		}
	}
	
	/* private Proxy getProxy(String proxyType) {
	        Proxy proxy = Proxy.NO_PROXY;
	        String proxyHost = null;
	        String proxyPort = null;

	        if (ON_PREMISE_PROXY.equals(proxyType)) {
	        	// Get proxy for on-premise destinations
	        	proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	            proxyPort = System.getenv("HC_OP_HTTP_PROXY_PORT");
	        } else {
	            // Get proxy for internet destinations
	            proxyHost = System.getProperty("https.proxyHost");
	            proxyPort = System.getProperty("https.proxyPort");
	        }

	        if (proxyPort != null && proxyHost != null) {
	        	int proxyPortNumber = Integer.parseInt(proxyPort);
	            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPortNumber));
	        }

	        return proxy;
	    }*/

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public JSONObject generateOrderID1(HttpServletRequest request, HttpServletResponse response, boolean debug)  throws IOException, JSONException 
	{
		JSONObject orderApiRes = new JSONObject();
		String txnAmount = "", txnID = "", apiKey="", secretKey=""; 
		try 
		{
			txnAmount = Integer.toString((int)(Float.parseFloat(request.getParameter("txn-amount"))*100));
			txnID = request.getParameter("txn-id");
	
//			if(debug){
				response.getWriter().println("GenerateID.txnAmount: "+txnAmount);
				response.getWriter().println("GenerateID.txnID: "+txnID);
//			}
				
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			apiKey = properties.getProperty("apiKey");
			secretKey = properties.getProperty("secretKey");
		
			JSONObject options = new JSONObject();
			options.put("amount", txnAmount);
			options.put("currency", "INR");
			options.put("receipt", txnID);
			options.put("payment_capture", "1");
			
//			if(debug){
				response.getWriter().println("options: "+options);
				response.getWriter().println("apiKey: "+apiKey);
				response.getWriter().println("secretKey: "+secretKey);
//			}
			String url = "https://api.razorpay.com/v1/orders";
			String credentials = Credentials.basic(apiKey, secretKey);
			
			MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			OkHttpClient client = new OkHttpClient();

			okhttp3.RequestBody body = RequestBody.create(JSON, options.toString());
			okhttp3.Request requestOK = new okhttp3.Request.Builder()
	    		                    .url(url)
	    		                    .addHeader("Authorization",credentials)
	    		                    .post(body)
	    		                    .build();
	    
			okhttp3.Response responseOK = client.newCall(requestOK).execute();

			String networkResp = responseOK.body().string();
			response.getWriter().println("networkResp: "+networkResp);
			orderApiRes = new JSONObject(networkResp.toString());
			
			orderApiRes.accumulate("ErrorCode", "");
			orderApiRes.accumulate("ErrorMessage", "");
			
//			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("orderApiRes: "+orderApiRes);
		} 
		catch (Exception e) 
		{	
//			e.printStackTrace();
			orderApiRes.accumulate("ErrorCode", "001");
			orderApiRes.accumulate("ErrorMessage", e.getLocalizedMessage());
			
				
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("genordid.Exception:"+buffer.toString());
		}		
		finally 
		{
			return  orderApiRes;
		}
	}
	
	
	public void validateCustomer(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo, boolean debug) throws IOException, URISyntaxException
	{
		String errorCode="", destURL="", userName="", password="", authParam="", authMethod=""; 

		HttpGet userCustomersGet = null;
		HttpEntity customerEntity = null;
		boolean isValidCustomer = false;

		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("pcgw_utils_op");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("pcgw_utils_op", options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", "pcgw_utils_op"));
	        }
			
			
			// Get the destination Properties
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
//			aggregatorID = destConfiguration.get("AggregatorID");
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Bearer " + authParam;
			
			
			if (debug) {
				 response.getWriter().println("destURL:-  "+ destURL);
//				 response.getWriter().println("aggregatorID:-  "+ aggregatorID);
				 response.getWriter().println("userName:-  " + userName);
				 response.getWriter().println("password:-  "+password);
				 response.getWriter().println("authMethod:-  "+authMethod);
			}
			
			String CustomerService = "";
			String CustomerFilter = "";
				
//			CustomerFilter = "LoginID eq '"+loginSessionID+"'";
			CustomerFilter = "LoginID eq '"+loginSessionID+"' and CustomerNo eq '"+customerNo+"'";
			
			CustomerFilter = URLEncoder.encode(CustomerFilter, "UTF-8");
			
			CustomerFilter = CustomerFilter.replaceAll("%26", "&");
			CustomerFilter = CustomerFilter.replaceAll("%3D", "=");
			if (debug){
				response.getWriter().println("CustomerFilter: "+CustomerFilter);
				response.getWriter().println("customerNo: "+customerNo);
				response.getWriter().println("loginSessionID: "+loginSessionID);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();
			String service = destConfiguration.get("service").get().toString();
			AuthenticationHeader principalPropagationHeader = null;
//			HttpDestination destination = getHTTPDestinationForCustomers(request, response, PCGW_UTIL_DEST_NAME);
			/* Context ctxAuthHdr = new InitialContext();
			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctxAuthHdr.lookup("java:comp/env/myAuthHeaderProvider");
			AuthenticationHeader principalPropagationHeader = authHeaderProvider.getPrincipalPropagationHeader(); */
			
			if(sapclient != null)
			{
				CustomerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ CustomerFilter;
			}
			else
			{
				CustomerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
			}

			if(null != service && service.equalsIgnoreCase("SFGW")){
				if(sapclient != null)
				{
					CustomerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client="+ sapclient +"&$filter="+ CustomerFilter;
				}
				else
				{
					CustomerService = destURL+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
				}
			}else{
				if(sapclient != null)
				{
					CustomerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?sap-client="+ sapclient +"&$filter="+ CustomerFilter;
				}
				else
				{
					CustomerService = destURL+"/sap/opu/odata/ARTEC/PYGW/UserCustomers?$filter="+ CustomerFilter;
				}
			}
//			response.getWriter().println("CustomerService: "+CustomerService);
			if (debug){
				response.getWriter().println("CustomerService: "+CustomerService);
				response.getWriter().println("destURL: "+destURL);
			}
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			response.getWriter().println("validateCustomer.proxyType: "+proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    response.getWriter().println("validateCustomer.proxyHost: "+proxyHost);
		    response.getWriter().println("validateCustomer.proxyPort: "+proxyPort);
		    
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        CloseableHttpClient closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        userCustomersGet = new HttpGet(CustomerService);
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
	            NodeList nodeList = document.getElementsByTagName("d:CustomerNo");
	            for(int i=0 ; i<nodeList.getLength() ; i++)
	            {
	            	if (debug)
	            		response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
//	            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
	            	if(customerNo.equalsIgnoreCase(nodeList.item(i).getTextContent()))
        			{
	            		isValidCustomer = true;
	            		break;
        			}
	            }
	            if(! isValidCustomer)
	            {
	            	errorCode = "E105";
	            }
			}

			response.getWriter().println("isValidCustomer: "+isValidCustomer);
			response.getWriter().println("errorCode: "+errorCode);
			
		}catch (RuntimeException e) {
			errorCode = "E105";
			response.getWriter().println("RuntimeException: "+e.getMessage());
//			userCustomersGet.abort();
			
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.RuntimeException:"+buffer.toString());
			
		}catch (ParserConfigurationException e) {
			errorCode = "E105";
			response.getWriter().println("ParserConfigurationException: "+e.getMessage());
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.ParserConfigurationException:"+buffer.toString());
		} catch (SAXException e) {
			errorCode = "E105";
			response.getWriter().println("SAXException: "+e.getMessage());
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.SAXException:"+buffer.toString());
		} /* catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.NamingException:"+buffer.toString());
		} */catch(Exception e){
			response.getWriter().println("Error at user session create: " +  e.getMessage());
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.Exception:"+buffer.toString());
		}
	}
	
	public String createUserSession(HttpServletRequest request, HttpServletResponse response, String url, String loginID, boolean debug) throws IOException{
		//get soap xml string
		String sessionID="";
		CommonUtils commonUtils = new CommonUtils();
		String soapXML = "";
		try{
			soapXML = commonUtils.getSoapXML(request, loginID);
			
			if(debug)
				response.getWriter().println("soapXML: "+soapXML);
			//Creates a StringEntity with the specified content and charset. The MIME type to UTF-8.
			if(debug){
				JsonObject result = new JsonObject();
				result.addProperty("Request", soapXML);
				response.getWriter().print(new Gson().toJson(result));
			}
			
			StringEntity stringEntity = new StringEntity(soapXML, "UTF-8");
			stringEntity.setChunked(true);
			//get http destination
			//Execute http post and get entity object
			HttpEntity entity = executeHttpPost(stringEntity, soapXML, url, response);
//			entityMsg = "entityMsg"+entity;
//			response.getWriter().println("entityMsg: "+entityMsg);
			//set httppost response to the servlet response
			sessionID = commonUtils.setGWHttpResponse(entity, response, debug);
			
			if(debug)
				response.getWriter().println("sessionID: "+sessionID);
		}catch(Exception e){
			
			response.getWriter().println("Error at user session create: " +  e.getMessage());
			
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("createUserSession.Exception:"+buffer.toString());
			}
		}
		
		return sessionID;
	}
	
	private Proxy getProxy(String proxyType, HttpServletResponse response) throws IOException{
        String proxyHost = "";
        int proxyPort=0;
        try {
        	response.getWriter().println("Inside getProxy");
			if (ON_PREMISE_PROXY.equals(proxyType)) {
			    // Get proxy for on-premise destinations
			    proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			    proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			} else {
			    // Get proxy for internet destinations
			    proxyHost = System.getProperty("http.proxyHost");
			    proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
			}
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
			response.getWriter().println("Error at posting xmlgetLocalizedMessage: " +  e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("getProxy.NumberFormatException:"+buffer.toString());
		}
        
        response.getWriter().println("proxyHost: "+proxyHost);
		response.getWriter().println("proxyPort: "+proxyPort);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }
	
	private void injectHeader(HttpURLConnection urlConnection, String proxyType, String basicAuth, HttpServletResponse response) throws IOException {
        if (ON_PREMISE_PROXY.equals(proxyType)) {
            // Insert header for on-premise connectivity with the consumer subaccount name
            urlConnection.setRequestProperty("SAP-Connectivity-ConsumerAccount", tenantContext.getAccountName());
            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestProperty("content-type", "text/xml; charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "text/xml");
        }
        
        response.getWriter().println("getAccountName: "+tenantContext.getAccountName());
        response.getWriter().println("getTenantId: "+tenantContext.getTenantId());
        response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
        response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
        response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
    }
	
	private void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[COPY_CONTENT_BUFFER_SIZE];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
    }
	
	public HttpEntity executeHttpPost(StringEntity stringEntity, String soapXML, String destUrl, HttpServletResponse response) throws IOException{
		HttpPost httpPost = null;
		HttpEntity entity = null;
		String userName="", password="", authParam="";
		HttpURLConnection urlConnection1 = null;
		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("pugw_utils_op");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("pugw_utils_op", options);
			Destination destConfiguration = destinationAccessor.get();
			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "pugw_utils_op"));
            }
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			
			response.getWriter().println("executeHttpPost.destUrl: "+ destUrl);
	        response.getWriter().println("executeHttpPost.proxyType: "+ proxyType);
	        response.getWriter().println("executeHttpPost.userName: "+ userName);
	        response.getWriter().println("executeHttpPost.password: "+ password);
	        response.getWriter().println("executeHttpPost.authParam: "+ authParam);
	        response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
	        
	        
	        String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	        int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
	        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        CredentialsProvider provider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        provider.setCredentials(AuthScope.ANY, credentials);
	        
	        CloseableHttpClient httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        HttpPost sessionRequest = new HttpPost(destUrl);
	        sessionRequest.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        sessionRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			sessionRequest.setHeader("content-type", "text/xml; charset=UTF-8");
			sessionRequest.setHeader("Accept", "text/xml");
			
			sessionRequest.setEntity(stringEntity);
			
			HttpResponse httpResponse = httpClient.execute(sessionRequest);
			
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			response.getWriter().println("statusCode: "+statusCode);
			entity = httpResponse.getEntity();
//			String retSrc = EntityUtils.toString(entity);
//			response.getWriter().println("retSrc: "+retSrc);
			response.getWriter().println("getAccountName: "+tenantContext.getAccountName());
	        response.getWriter().println("getTenantId: "+tenantContext.getTenantId());
	        response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
	        response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
	        response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			Proxy proxy1 = getProxy(proxyType, response);
		}catch (RuntimeException e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpPost.abort();
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
			response.getWriter().println("Error at posting xmlgetLocalizedMessage: " +  e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("executeHttpPost.RuntimeException:"+buffer.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			response.getWriter().println("Error at posting xml: " +  e.getMessage());
			response.getWriter().println("Error at posting xmlgetLocalizedMessage: " +  e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("executeHttpPost.Exception:"+buffer.toString());
		}
		
		return entity; 
	}
	
	public void bpHeaderDollarExpand(HttpServletResponse response, HttpServletRequest request, String customerNo, String loginSessionID, boolean debug) throws IOException{
		String authMethod="", destURL="", userName="", password="", authParam="", bpService="", bpFilter="";
		debug = true;
		HttpEntity bpEntity = null;
		HttpGet bpGet = null;
		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("pcgw_utils_op");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("pcgw_utils_op", options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", "pcgw_utils_op"));
	                return;
	        }
			
			
			// Get the destination Properties
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
//			aggregatorID = destConfiguration.get("AggregatorID");
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			
			if (debug) {
				 response.getWriter().println("destURL:-  "+ destURL);
//				 response.getWriter().println("aggregatorID:-  "+ aggregatorID);
				 response.getWriter().println("userName:-  " + userName);
				 response.getWriter().println("password:-  "+password);
				 response.getWriter().println("authMethod:-  "+authMethod);
			}
			
			String cpType="01";
			bpFilter = "LoginID eq '"+loginSessionID+"' and CPGuid eq '"+customerNo+"' and CPType eq '"+cpType+"'";
			bpFilter = URLEncoder.encode(bpFilter, "UTF-8");
			bpFilter = bpFilter.replaceAll("%26", "&");
			bpFilter = bpFilter.replaceAll("%3D", "=");
			if (debug){
				response.getWriter().println("bpHeaderDollarExpand.bpFilter: "+bpFilter);
				response.getWriter().println("bpHeaderDollarExpand.customerNo: "+customerNo);
				response.getWriter().println("bpHeaderDollarExpand.loginSessionID: "+loginSessionID);
			}
			
			String sapclient = destConfiguration.get("sapclient").get().toString();
			Context ctxAuthHdr = new InitialContext();
			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctxAuthHdr.lookup("java:comp/env/myAuthHeaderProvider");
			AuthenticationHeader principalPropagationHeader = authHeaderProvider.getPrincipalPropagationHeader();
			
			if(sapclient != null)
			{
				bpService = destURL+"/sap/opu/odata/ARTEC/PYGW/BPHeaders?$expand=BPContactPersons&sap-client="+ sapclient +"&$filter="+ bpFilter;
			}
			else
			{
				bpService = destURL+"/sap/opu/odata/ARTEC/PYGW/BPHeaders?$expand=BPContactPersons&$filter="+ bpFilter;
			}
			
			if (debug){
				response.getWriter().println("bpHeaderDollarExpand.bpService: "+bpService);
				response.getWriter().println("bpHeaderDollarExpand.destURL: "+destURL);
			}
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			response.getWriter().println("bpHeaderDollarExpand.proxyType: "+proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    response.getWriter().println("bpHeaderDollarExpand.proxyHost: "+proxyHost);
		    response.getWriter().println("bpHeaderDollarExpand.proxyPort: "+proxyPort);
		    
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        // CloseableHttpClient closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        bpGet = new HttpGet(bpService);
	        // bpGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        bpGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        bpGet.setHeader("Accept", "application/json");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	bpGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	bpGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(bpGet);
			HttpResponse httpResponse = client.execute(bpGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
			response.getWriter().println("bpHeaderDollarExpand.statusCode: "+statusCode);
			
			bpEntity = httpResponse.getEntity();
			
			if(bpEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(bpEntity);
				
				if (debug)
					response.getWriter().println("bpHeaderDollarExpand.retSrc: "+retSrc);
			}
		}catch (RuntimeException e) {
			response.getWriter().println("RuntimeException: "+e.getMessage());
			bpGet.abort();
			
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.RuntimeException:"+buffer.toString());
			
		}catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.NamingException:"+buffer.toString());
		}catch(Exception e){
			response.getWriter().println("Error at user session create: " +  e.getMessage());
			bpGet.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("validateCustomer.Exception:"+buffer.toString());
		}
	}
	
	public void pgPaymentConfigs(HttpServletResponse response, HttpServletRequest request, String pgID, String loginSessionID, boolean debug) throws IOException{
		String authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		debug = true;
		HttpEntity configValuesEntity = null;
		HttpGet configValuesGet = null;
		String configurableValues="";
		try{
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("pcgw_utils_op");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("pcgw_utils_op", options);
			Destination destConfiguration = destinationAccessor.get(); 
			if (destConfiguration == null) {
				 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						 String.format("Destination %s is not found. Hint:"
								 + " Make sure to have the destination configured.", "pcgw_utils_op"));
	                return;
	        }
			
			
			// Get the destination Properties
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
//			aggregatorID = destConfiguration.get("AggregatorID");
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + encodedStr;
			
			if (debug) {
				 response.getWriter().println("destURL:-  "+ destURL);
//				 response.getWriter().println("aggregatorID:-  "+ aggregatorID);
				 response.getWriter().println("userName:-  " + userName);
				 response.getWriter().println("password:-  "+password);
				 response.getWriter().println("authMethod:-  "+authMethod);
			}
			
			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			String sapclient = destConfiguration.get("sap-client").get().toString();
			
			Context ctxAuthHdr = new InitialContext();
			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctxAuthHdr.lookup("java:comp/env/myAuthHeaderProvider");
			AuthenticationHeader principalPropagationHeader = authHeaderProvider.getPrincipalPropagationHeader();
			// "LoginID eq '"+loginSessionID+"' and CPGuid eq '"+customerNo+"' and CPType eq '"+cpType+"'";
			String pgCatID="000002";
			constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"' and PGCategoryID eq '"+pgCatID+"'";//PGCategoryID
			response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
			
			constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
			if(sapclient != null)
			{
				constantValuesService =  destURL+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
			}
			else
			{
				constantValuesService =  destURL+constantValuesService+"?$filter="+constantValuesFilter;
			}

			if (debug){
				response.getWriter().println("pgPaymentConfigs.constantValuesService: "+constantValuesService);
				response.getWriter().println("pgPaymentConfigs.destURL: "+destURL);
			}
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			response.getWriter().println("pgPaymentConfigs.proxyType: "+proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    response.getWriter().println("pgPaymentConfigs.proxyHost: "+proxyHost);
		    response.getWriter().println("pgPaymentConfigs.proxyPort: "+proxyPort);
		    
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        CloseableHttpClient closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
	        configValuesGet = new HttpGet(constantValuesService);
	        configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
			response.getWriter().println("pgPaymentConfigs.statusCode: "+statusCode);
			
			configValuesEntity = httpResponse.getEntity();
			
			if(configValuesEntity != null)
			{
//		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//		        DocumentBuilder docBuilder;
//		        InputSource inputSource;
		        
//				String retSrc = EntityUtils.toString(configValuesEntity);
				
//				if (debug)
//					response.getWriter().println("pgPaymentConfigs.retSrc: "+retSrc);
		        if(pgID.equalsIgnoreCase("B2BIZ"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(debug)
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	response.getWriter().println("nodeList pdIDList: "+pdIDList.item(i).getTextContent());
		            	response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
		            	response.getWriter().println("nodeList aWSURLList: "+aWSURLList.item(i).getTextContent());
		            	response.getWriter().println("nodeList clientCodeList: "+clientCodeList.item(i).getTextContent());
		            	
		            	if(pgID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
		            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
		            		response.getWriter().println("configurableValues: "+configurableValues);
		            		break;
	        			}
		            }
				}
			}
			
		}catch (Exception e) {
			response.getWriter().println("Error at pgPaymentConfigs: " +  e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("pgPaymentConfigs.Exception:"+buffer.toString());
		}
	}

}

package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonObject;
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
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class PaymentGatewayUtils
 */
@WebServlet("/PaymentGatewayUtils")
public class PaymentGatewayUtils extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PaymentGatewayUtils() {
        super();
        // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String payLoadRequest ="" , pgID="", configurationValues="", message=""; 
		JSONObject inputJsonObject = new JSONObject();
		JsonObject razorJsonResponse = new JsonObject();	
		boolean debug = false;
		try {
//			inputJsonObject = new JSONObject(payLoadRequest);
			String payLoad = request.getParameter("OrderID");
			inputJsonObject = new JSONObject(payLoad);
			if(! inputJsonObject.isNull("debug") && inputJsonObject.getString("debug").equalsIgnoreCase("true")){
				debug = true;
			}
			if (debug) {
				response.getWriter().println("doGet.PaymentGatewayUtils_OrderID: "+ request.getPathInfo());
			}
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			payLoadRequest = getBody(request, response);
			
			
			if (! inputJsonObject.isNull("PGID"))
				pgID = inputJsonObject.getString("PGID");
		
			String required = inputJsonObject.getString("Required");
			if (debug)
				response.getWriter().println("doGet.PGID: "+ pgID);
			
			if(pgID != null && pgID.trim().length() > 0){
				
				if (debug)
					response.getWriter().println("doGet.message: "+message);
				
				if(required.equalsIgnoreCase("OrderID"))
				{
					message = validateParam(request, response, inputJsonObject, pgID, properties, debug);
					if (message.equalsIgnoreCase("") && message.trim().length() == 0){
						
						if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")) || pgID.equalsIgnoreCase("RZRPAY"))
						{
							configurationValues = getConstantValues(request, response, "", pgID, debug);
							if (debug)
								response.getWriter().println("doGet.configurationValues: "+configurationValues);
							if(configurationValues.trim().length() > 0 && configurationValues.contains("Status Code")){
								response.getWriter().println(configurationValues);
								
							} else {
								if (configurationValues.trim().length() > 0) {
									generateRazorOrderID(request, response, configurationValues, inputJsonObject, pgID,
											properties, debug);

								} else {
									razorJsonResponse.addProperty("order-id", "");
									razorJsonResponse.addProperty("Status", "000002");
									razorJsonResponse.addProperty("Message", "Configuration Values not found");
									response.getWriter().println(razorJsonResponse);
								}
							}	
								
						}
						else if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID")))
						{
							configurationValues = getConstantValues(request, response, "", pgID, debug);
							if (debug)
								response.getWriter().println("doGet.configurationValues: "+configurationValues);
							if (configurationValues.trim().length() > 0
									&& configurationValues.contains("Status Code")) {
								response.getWriter().println(configurationValues);
							} else {
								if (configurationValues.trim().length() > 0) {
									generateRazorOrderID(request, response, configurationValues, inputJsonObject, pgID,
											properties, debug);

								} else {
									razorJsonResponse.addProperty("order-id", "");
									razorJsonResponse.addProperty("Status", "000002");
									razorJsonResponse.addProperty("Message", "Configuration Values not found");
									response.getWriter().println(razorJsonResponse);
								}
							}	
						}else{
							razorJsonResponse.addProperty("order-id", "");
							razorJsonResponse.addProperty("Status", "000002");
							razorJsonResponse.addProperty("Message", "Invalid PGID received");
							response.getWriter().println(razorJsonResponse);
						}
					}
				}else {
					razorJsonResponse.addProperty("order-id", "");
					razorJsonResponse.addProperty("Status", "000002");
					razorJsonResponse.addProperty("Message", message);
					response.getWriter().println(razorJsonResponse);
				}
			}else{
				razorJsonResponse.addProperty("order-id", "");
				razorJsonResponse.addProperty("Status", "000002");
				razorJsonResponse.addProperty("Message", "Invalid PGID received");
				response.getWriter().println(razorJsonResponse);
			}
		} catch (JSONException e) {
			razorJsonResponse.addProperty("order-id", "");
			razorJsonResponse.addProperty("Status", "000002");
			razorJsonResponse.addProperty("Message", e.getLocalizedMessage());
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug){
				response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
			}
			razorJsonResponse.addProperty("order-id", "");
			razorJsonResponse.addProperty("Status", "000002");
			razorJsonResponse.addProperty("Message", buffer.toString());
			response.getWriter().println(razorJsonResponse);
		}
	}
	public String validateParam(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject, String pgID, Properties properties, boolean debug) throws IOException
	{
		String  errorMessage="";
		
		try {
//			if ( pgID.equalsIgnoreCase(""))
//				errorMessage = "pgID is missing in the request";
			
			/*if ( inputJsonObject.isNull("txn-id") ||  inputJsonObject.getString("txn-id").equalsIgnoreCase(""))
				errorMessage = "txn-id is missing in the request";*/
			
			if (inputJsonObject.isNull("currency") || ! StringUtils.isAlpha(inputJsonObject.getString("currency")))
				errorMessage = "Currency is missing in the request";

			try {
				BigDecimal txnAmt = new BigDecimal(inputJsonObject.getString("txn-amount"));
				//Check for >0
				if (txnAmt.compareTo(BigDecimal.ZERO) <= 0) 
					errorMessage = "Invalid amount received in the request";
			} catch (NumberFormatException | JSONException e){
				errorMessage = "Transaction Amount is missing in the request";
			}
			if ( errorMessage.isEmpty() && pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
				if ( inputJsonObject.isNull("account-id") || inputJsonObject.getString("account-id").equalsIgnoreCase(""))
					errorMessage = "Account ID is missing in the request";
					
				if (debug) 
					response.getWriter().println("validateParam.accountID: "+inputJsonObject.getString("account-id"));
			}
			
			if (debug){
				response.getWriter().println("validateParam.errorMessage: "+errorMessage);
				response.getWriter().println("validateParam.pgID: "+pgID);
				response.getWriter().println("validateParam.txnAmount: "+inputJsonObject.getString("txn-amount"));
//				response.getWriter().println("validateParam.txnID: "+inputJsonObject.getString("txn-id"));
				response.getWriter().println("validateParam.currency: "+inputJsonObject.getString("currency"));
			}
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("doGet-Exception Stack Trace: "+buffer.toString());
			}
			errorMessage = "Exception: "+e.getMessage();
			return errorMessage;
		}
		return errorMessage;
	}
	public void generateRazorOrderID(HttpServletRequest request, HttpServletResponse response, String configurationValues, JSONObject inputJsonObject, String pgID, Properties properties, boolean debug)  throws IOException, JSONException 
	{
		String apiKey="", secret="", orderID="", wholeParamString="",paramName="" ,paramValue="", txnAmount="", txnID="" ,accountID="", currency="";
		String deviceId="",cpGuid="";
		JsonObject razorPayResponse = new JsonObject();
		if(debug)
			response.getWriter().println("generateRazorOrderID()");
		try{
			txnAmount = inputJsonObject.getString("txn-amount");
			txnID = inputJsonObject.getString("txn-id");
			currency = inputJsonObject.getString("currency");
			pgID = inputJsonObject.getString("PGID");
			deviceId=inputJsonObject.has("Source")?inputJsonObject.getString("Source"):null;
			cpGuid=inputJsonObject.has("CustomerNo")?inputJsonObject.getString("CustomerNo"):null;
			 
			BigDecimal txnAmt = new BigDecimal(txnAmount);
			txnAmt = txnAmt.multiply(new BigDecimal(100));
			txnAmount =txnAmt.toBigInteger().toString();
			
			if (debug) {
				response.getWriter().println("generateRazorOrderID.txnAmount: "+txnAmount);
				response.getWriter().println("generateRazorOrderID.pgID: "+pgID);
				response.getWriter().println("generateRazorOrderID.currency: "+currency);
				response.getWriter().println("generateRazorOrderID.txnID: "+txnID);
				response.getWriter().println("generateRazorOrderID.deviceId"+deviceId);
				response.getWriter().println("generateRazorOrderID.cpGuid"+cpGuid);
				
			}
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
				paramName = s.substring(0, s.indexOf("="));
				paramValue = s.substring(s.indexOf("=")+1,s.length());
				
				if(paramName.equalsIgnoreCase("APIKey"))
					apiKey = paramValue;
				
				if (paramName.equalsIgnoreCase("SecretKey"))
					secret = paramValue;
			}
			if(debug){
				response.getWriter().println("generateRazorOrderID.apiKey: "+apiKey);
				response.getWriter().println("generateRazorOrderID.secret: "+secret);
			}
			RazorpayClient razorpayClient = new RazorpayClient(apiKey, secret);
			JSONObject notes=new JSONObject();
			if(cpGuid!=null){
				notes.put("CustomerNo", cpGuid);
			}
			if(deviceId!=null){
				notes.put("DeviceID", deviceId);
			}
			if(txnID!=null){
				notes.put("SAPTrackID", txnID);
			}
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("notes", notes);
			orderRequest.put("amount", txnAmount);
			orderRequest.put("currency", currency);
			orderRequest.put("receipt", txnID);
			orderRequest.put("payment_capture", true);
			if (pgID.equalsIgnoreCase(properties.getProperty("RazorPayRoutePGID"))){
				if (debug)
					response.getWriter().println("generateRazorOrderID.accountID: "+accountID);
					
				accountID = inputJsonObject.getString("account-id");
				JSONArray transfers = new JSONArray();
	//					// amount in paise
				JSONObject transfer = new JSONObject();
				transfer.put("amount", txnAmount);
				transfer.put("currency", currency);
				transfer.put("account", accountID);
				transfers.put(transfer);
				orderRequest.put("transfers", transfers);
			}
			
			Order order = razorpayClient.Orders.create(orderRequest);
			
			if(order.has("id") && order.get("id").toString().trim().length() > 0 ){
				orderID = order.get("id").toString();
				razorPayResponse.addProperty("orderid", orderID);
				razorPayResponse.addProperty("Status", "000001");
				razorPayResponse.addProperty("Message", "Success");
				response.getWriter().println(razorPayResponse);
			}else {
				orderID = "";
				razorPayResponse.addProperty("orderid", orderID);
				razorPayResponse.addProperty("Status", "000002");
				razorPayResponse.addProperty("Message", "Unable to generate order-id");
				response.getWriter().println(razorPayResponse);
			}
			
			if(debug){
				response.getWriter().println("order.toJson: "+order.toJson());
				response.getWriter().println("order.amount: "+order.get("amount"));
				response.getWriter().println("order.currency: "+order.get("currency"));
				response.getWriter().println("order.receipt: "+order.get("receipt")); 
				response.getWriter().println("order.transfers: "+order.get("transfers"));
				response.getWriter().println("order.orderID: "+order.get("id"));
			}
		}catch(RazorpayException e){
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("RazorpayException:"+buffer.toString());
			}
		} catch (JSONException e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("JSONException:"+buffer.toString());
			}
		} catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("generateRazorOrderID-Exception:"+buffer.toString());
			}
		}
	}
	public String getBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID, boolean debug) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
//		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
		try
		{
			/*if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;*/
			
//			Context tenCtx = new InitialContext();
//			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
//			if(debug){
//				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
//				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
//				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
//			}
			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			Destination destConfiguration = destinationAccessor.get();
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						String.format("Destination %s is not found. Hint:"
								+ " Make sure to have the destination configured.", PCGW_UTIL_DEST_NAME));
				 
				return "";
			}
			
			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if(authMethod.equalsIgnoreCase("BasicAuthentication")){
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":"+ password ;
				basicAuth = "Bearer " + authParam;
			}else{
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
			}
			
			String sapclient = destConfiguration.get("sap-client").get().toString();

			constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			constantValuesFilter = "";
			
			String pgCatID="000002";
			constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"' and PGCategoryID eq '"+pgCatID+"'";//PGCategoryID
			if(debug)
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
			
			/* String proxyType = destConfiguration.get("ProxyType").get().toString();
			if(debug)
				response.getWriter().println("pgPaymentConfigs.proxyType: "+proxyType);
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
		    int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
		    if(debug){
			    response.getWriter().println("pgPaymentConfigs.proxyHost: "+proxyHost);
			    response.getWriter().println("pgPaymentConfigs.proxyPort: "+proxyPort);
		    }
		    
		    HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
	        configValuesGet = new HttpGet(constantValuesService);
//	        configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
			HttpResponse httpResponse = client.execute(configValuesGet);
	        
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug){
				response.getWriter().println("pgPaymentConfigs.statusCode: "+statusCode);
	        }
			
			configValuesEntity = httpResponse.getEntity();
			if((statusCode/100)==2){
				if(configValuesEntity != null)
				{
					configurableValues = "";
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					{
						response.getWriter().println("constantEntity is not null");
						response.getWriter().println("PGID: "+PGID);
					}
					
					if(PGID.equalsIgnoreCase("RZRPYROUTE"))
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
						
						NodeList pdIDList = document.getElementsByTagName("d:PGID");
						NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
						NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
						NodeList apiKey = document.getElementsByTagName("d:PGOwnPublickey");
						NodeList secretKey = document.getElementsByTagName("d:PGOwnPrivatekey");
						NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
						NodeList providerList = document.getElementsByTagName("d:PGProvider");
						NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
										          
						for(int i=0 ; i<pgCategoryList.getLength() ; i++)
						{
//							response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
							if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
							{
								configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
											          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
											            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
											            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
											            +"|"+"APIKey="+apiKey.item(i).getTextContent()
											            +"|"+"SecretKey="+secretKey.item(i).getTextContent()
														+"|"+"PGProvider="+providerList.item(i).getTextContent()
														+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
							
								break;
							}
						}
					}
					else if (PGID.equalsIgnoreCase("RAZORPAY") || PGID.equalsIgnoreCase("RZRPAY")) {
						
						DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder;
						InputSource inputSource;
						
						String retSrc = EntityUtils.toString(configValuesEntity); 
						if(debug)
							response.getWriter().println("retSrc: "+retSrc);
						
						docBuilder = docBuilderFactory.newDocumentBuilder();
						inputSource = new InputSource(new StringReader(retSrc));
						Document document = docBuilder.parse(inputSource);
						
						NodeList pdIDList = document.getElementsByTagName("d:PGID");
						NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
						NodeList merchantCodeList = document.getElementsByTagName("d:PGName");
						NodeList apiKey = document.getElementsByTagName("d:PGOwnPublickey");
						NodeList secretKey = document.getElementsByTagName("d:PGOwnPrivatekey");
						NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
						NodeList providerList = document.getElementsByTagName("d:PGProvider");
						NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
										          
						for(int i=0 ; i<pgCategoryList.getLength() ; i++)
						{
//							response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
							if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
							{
								configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
											          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
											            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
											            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
											            +"|"+"APIKey="+apiKey.item(i).getTextContent()
											            +"|"+"SecretKey="+secretKey.item(i).getTextContent()
														+"|"+"PGProvider="+providerList.item(i).getTextContent()
														+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
							
								break;
							}
						}
					}
				}
				if(debug)
					response.getWriter().println("1 configurableValues: "+configurableValues);

				
			}else{
				String message=EntityUtils.toString(configValuesEntity); 
				if(debug)
				response.getWriter().println("Connection Failed with Status Code "+statusCode+" For the Host "+destURL+". "+message);
				
				configurableValues="Connection Failed with Status Code "+statusCode+" For the Host "+destURL+". "+message;
			}
					}
		catch (Exception e)
		{
			if(debug){
				response.getWriter().println("getConstantValues.Exception: "+e.getMessage());
				
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println(e.getMessage()+"---> in getConstantValues. Full Stack Trace: "+buffer.toString());
			}

		}
		finally
		{
			// closableHttpClient.close();
			return configurableValues;
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

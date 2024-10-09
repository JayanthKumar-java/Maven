package com.arteriatech.pg;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.isg.isgpay.ISGPayDecryption;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

import io.vavr.control.Try;

/**
 * Servlet implementation class ResPymtGateway
 */
@WebServlet("/ResPymtGatewayRazor")
public class ResPymtGatewayRazor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGatewayRazorPay.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResPymtGatewayRazor() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String siteID="", appID="", actionName="", navParam = "", localStorageRefNo = "", portalURL="", pgID="", responseMessage="", redirectURL="", pgHdrGUID="", pgParams="", paymentGtwyID="";
//			if(null != request.getParameter("PGID"))
//				pgID = request.getParameter("PGID");
			if(null != request.getParameter("PGParams"))
				pgParams = request.getParameter("PGParams");
			
			String[] resSplitResult = pgParams.split("\\~");
			String resParamName="", resParamValue="";
			for(String s : resSplitResult)
			{
//				response.getWriter().println("q: "+s);
				resParamName = s.substring(0, 5);
				resParamValue = s.substring(5,s.length());
//				response.getWriter().println("resParamName: "+resParamName);
//				response.getWriter().println("resParamValue: "+resParamValue);
	        	if(resParamName.equalsIgnoreCase("PGIDS"))
	        	{
	        		pgID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("PGGID"))
	        	{
	        		pgHdrGUID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("STEID"))
	        	{
	        		siteID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("APPID"))
	        	{
	        		appID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("ACTON"))
	        	{
	        		actionName = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("NAVPM"))
	        	{
	        		navParam = resParamValue;
	        	}
	        	if(resParamName.equalsIgnoreCase("REFNO"))
	        	{
	        		localStorageRefNo = resParamValue;
	        	}
			}
			
			if(siteID.trim().length() == 32)
			{
				String newSiteID = siteID.substring(0, 8) + "-" + siteID.substring(8,12)+"-"+siteID.substring(12,16)+"-"+siteID.substring(16,20)+"-"+siteID.substring(20);
				redirectURL = "/sites?siteId="+newSiteID+"#"+appID+"-"+actionName+"&/"+navParam;
			}
			else
			{
				redirectURL = "/sites/"+siteID+"#"+appID+"-"+actionName+"&/"+navParam;
			}
			
			if(null != pgID && pgID.trim().length() > 0)
			{
				String configurationValues = getConstantValues(request, response, "", pgID);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+configurationValues);
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				
				if(pgID.equalsIgnoreCase(properties.getProperty("AxisPGID")))
				{
					if(null != request.getParameter("i"))
						responseMessage = request.getParameter("i");
					if(responseMessage != null)
					{
						decryptAxisMessage(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgResMsg", "Response not found");
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
					}
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("ICICIPGID")))
				{
					if(null != request.getParameter("walletResponseMessage"))
						responseMessage = request.getParameter("walletResponseMessage");
					
					if(responseMessage != null)
					{
						decryptICICIMessage(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configurationValues, localStorageRefNo);	
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgResMsg", "Response not found");
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
					}
					
					
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("YesPGID")))
				{
					if(request.getParameterNames() != null)
					{
						decryptYesPayUMessage(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgResMsg", "Response not found");
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
					}
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
				{
					CommonUtils readConfig = new CommonUtils();
					Map<String, String> configMap = readConfig.readConfigValues(pgID, "T");
					
					if(request.getParameterNames() != null)
					{
						decryptEazyPayResponse(request, response, responseMessage, redirectURL, pgID, pgHdrGUID, configMap, localStorageRefNo);	
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgResMsg", "Response not found");
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
					}
					
					
				}
				else if(pgID.equalsIgnoreCase(properties.getProperty("RazorPayPGID")))
				{
					if(request.getParameterNames() != null)
					{
						decryptRazorPayResponse(request, response, responseMessage, configurationValues, redirectURL, pgID, pgHdrGUID, siteID, appID, actionName, navParam);
					}
					else
					{
						JsonObject result = new JsonObject();
						result.addProperty("pgResMsg", "Response not found");
						result.addProperty("Valid", "false");
						response.getWriter().print(new Gson().toJson(result));
					}
				}
				else
				{
					JsonObject result = new JsonObject();
					result.addProperty("pgResMsg", "PGID Not found");
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
		}
		catch (Exception e)
		{
			response.getWriter().println(displayErrorForWeb(e));
		}
		finally
		{
			return;
		}
	}
	
	public void decryptRazorPayResponse(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam) throws IOException{
		try {
			response.getWriter().println("In Response servlet-decryptRazorPayResponse method");
//			request.getAttributeNames();
			Enumeration enumeration = request.getParameterNames();
	        Map<String, Object> modelMap = new HashMap<>();
	        while(enumeration.hasMoreElements()){
	            String parameterName = (String)enumeration.nextElement();
	            modelMap.put(parameterName, request.getParameter(parameterName));
	        }
			
	        for (String key : modelMap.keySet()) {
	        	response.getWriter().println("decryptRazorPayResponse: "+key + " - " + modelMap.get(key));
	        }
		} catch (Exception e) {
			response.getWriter().println(displayErrorForWeb(e));
		}
	}
	
	public String displayErrorForWeb(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");
	}
	
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGCategoryID) throws IOException, URISyntaxException
	{/*
		HttpGet constantRequest = null;
		HttpEntity constantEntity = null;
		String configurableValues = "", pgID="";
		pgID = PGCategoryID;
		String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME);
		try
		{
			String constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			String constantValuesFilter = "";
			//PG ID 10 is for ICICI
			if(null != PGCategoryID && PGCategoryID.toString().length() > 0)
			{
//				if(PGCategoryID.equalsIgnoreCase("10"))
				{
					if(constantValuesFilter == null || constantValuesFilter == "")
					{
						//Uncomment middle line or last line when this service will be filterable . Login ID is not required for this.
//						constantValuesFilter = "LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
//						constantValuesFilter = constantValuesFilter+"PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"'";
					}
					else
					{
						//Uncomment middle line or last line when this service will be filterable. Login ID is not required for this.
//						constantValuesFilter = constantValuesFilter+" and LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
//						constantValuesFilter = constantValuesFilter+"PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"'";
					}
					
//					response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
					HttpDestination destination = getHTTPDestination(request, response, PCGW_UTIL_DEST_NAME);
					constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
					if(sapclient != null)
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
//						constantValuesFilter = constantValuesFilter+"PGID eq '"+pgID+"'";
					}
					else
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?$filter="+constantValuesFilter;
					}
					
					//Uncomment the above code if filter is required. Below code is temporary one without any filters
//					constantValuesService = destination.getURI().getPath()+constantValuesService;
//					response.getWriter().println("URL: "+constantValuesService);
//					response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
					HttpClient httpClient = destination.createHttpClient();
					constantRequest = new HttpGet(constantValuesService);
//					response.getWriter().println("constantRequest: "+constantRequest);
					HttpResponse constantResponse = httpClient.execute(constantRequest);
					
//					countResponse.getEntity().

					// Copy content from the incoming response to the outgoing response
					constantEntity = constantResponse.getEntity();

					if(constantEntity != null)
					{
						configurableValues = "";
						if(PGCategoryID.equalsIgnoreCase("B2BIZ"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
//							response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
				            				+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("AXISPG"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
//							response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
				            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
				            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
				            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
				            NodeList secretCode = document.getElementsByTagName("d:ClientCode");
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
				            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
				            								+"|"+"Type="+pgParameter2.item(i).getTextContent()
				            								+"|"+"RE1="+pgParameter3.item(i).getTextContent()
				            								+"|"+"secretCode="+secretCode.item(i).getTextContent()
				            								+"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("YESPAYU"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
//							response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList pgOwnPublicKey = document.getElementsByTagName("d:PGOwnPublickey");
				            NodeList pgOwnPrivateKey = document.getElementsByTagName("d:PGOwnPrivatekey");
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
				            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            								+"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
				            								+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						else if(PGCategoryID.equalsIgnoreCase("EazyPayPGID"))
						{
							DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
					        
							String retSrc = EntityUtils.toString(constantEntity); 
							if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
								response.getWriter().println("retSrc: "+retSrc);
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            NodeList secretKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
				            
				            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGCategoryID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
				            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
				            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()+
				            				"|"+"ClientCode="+clientCodeList.item(i).getTextContent()+
				            				"|"+"SecretKey="+secretKeyList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
					}
//					response.getWriter().println("configurableValues: "+configurableValues);
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		finally
		{
			return configurableValues;
		}
	*/
	return "";	
	}
	private String getHTTPDestinationConfiguration(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) 
		{
			destinationName = PCGW_UTIL_DEST_NAME;
		}
		
		String tempSapClient = null;
				
		// get all destination properties
//				Context ctxDestFact = new InitialContext();
//				ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
//				DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
//				Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
				.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
		Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
				.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
		Destination destConfiguration = destinationAccessor.get();
		//Reading SAP-Client from All Configuration.........................................................
		tempSapClient = destConfiguration.get("sap-client").get().toString();
		
		return tempSapClient;
	}
	/*private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = PUGW_DEST_NAME;
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
			}
		} catch (NamingException e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: "
					+ e.getMessage()
					+ ". See "
					+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
			LOGGER.error("Connectivity operation failed", e);
			//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
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
	
	private void decryptICICIMessage(HttpServletRequest request, HttpServletResponse response, String responseMessage, String redirectURL, String pgID, String pgHdrGUID, String configurationValues, String localStorageRefNo) throws IOException
	{
		String portalURL="";
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="",clientCode = "";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
			}
			
//			walletPublicKey = properties.getProperty("walletPublicKey");
//			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//			merchantPublicKey = properties.getProperty("merchantPublicKey");
			if(null != clientCode)
			{
				walletPublicKey = properties.getProperty(clientCode.toString()+"WalletPublicKey");
				merchantPrivateKey = properties.getProperty(clientCode.toString()+"ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty(clientCode.toString()+"ARTMerchantPublicKey");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("PRD Keys found");
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"WalletPublicKey"+ walletPublicKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"ARTMerchantPrivateKey"+ merchantPrivateKey);
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print(clientCode+"ARTMerchantPublicKey"+ merchantPublicKey);
			}
			else
			{
				walletPublicKey = properties.getProperty("WalletPublicKey");
				merchantPrivateKey = properties.getProperty("ARTMerchantPrivateKey");
				merchantPublicKey = properties.getProperty("ARTMerchantPublicKey");
			}
			
//			merchantCode = properties.getProperty("merchantCode");
			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgResponseErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
//			pgRespMsg = request.getParameter("walletResponseMessage");
			pgRespMsg = responseMessage;

			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setWalletResponseMessage(pgRespMsg);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			
			String walletTxnStatus="", txnID="", walletUserCode="", txnSessionID="", walletTxnID="", walletTxnDateTime="", txnDateTime="", walletBankRefID="", txnFor="",
					txnAmount="", walletTxnRemarks="", adnlParam1="", adnlParam2="", adnlParam3="", adnlParam4="", adnlParam5="", adnlParam6="", adnlParam7="", adnlParam8="",
					adnlParam9="", adnlParam10="", pgMode="", pgName="", challanTxnID="", nachPaymentType="", nachPaymentDate="", nachPaymentDateAlt="", paymentMethod="",
					nachType="", nachSchDate="", yymmddDate="";
			if(walletMessageBean.validateWalletResponseMessage()) 
			{
				WalletParamMap map = walletMessageBean.getResponseMap();
				walletTxnStatus = map.get("wallet-txn-status");
				txnID = map.get("txn-id");
				walletUserCode = map.get("wallet-user-code");
				txnSessionID = map.get("txn-session-id");
				walletTxnID = map.get("wallet-txn-id");
				walletTxnDateTime = map.get("wallet-txn-datetime");
				txnDateTime = map.get("txn-datetime");
				walletBankRefID = map.get("wallet-bank-ref-id");
				txnFor = map.get("txn-for");
				txnAmount = map.get("txn-amount");
				walletTxnRemarks = map.get("wallet-txn-remarks");
				adnlParam1 = map.get("additional-param1");
				adnlParam2 = map.get("additional-param2");
				adnlParam3 = map.get("additional-param3");
				adnlParam4 = map.get("additional-param4");
				adnlParam5 = map.get("additional-param5");
				adnlParam6 = map.get("additional-param6");
				adnlParam7 = map.get("additional-param7");
				adnlParam8 = map.get("additional-param8");
				adnlParam9 = map.get("additional-param9");
				adnlParam10 = map.get("additional-param10");
				pgMode = map.get("pg-mode");
				pgName = map.get("pg-name");
				challanTxnID = map.get("challan-txn-id");
				nachPaymentType = map.get("nach-payment-type");
				nachPaymentDate = map.get("nach-scheduledpayment-date");
				
				if(nachPaymentDate != null)
				{
					nachPaymentDateAlt = nachPaymentDate.replaceAll("\\/", "");
					
					String year = nachPaymentDateAlt.substring(4, 8);
					String mm = nachPaymentDateAlt.substring(2, 4);
					String dd = nachPaymentDateAlt.substring(0, 2);
					yymmddDate =year+mm+dd;
					
				}
				else
				{
					yymmddDate = null;
				}
				
				if(pgMode == null && pgName==null && nachPaymentType==null)
				{
					paymentMethod = "000001";
				}
				else if(pgName != null && nachPaymentType==null)
				{
					paymentMethod = "000002";
				}
				else if(nachPaymentType != null)
				{
					paymentMethod = "000005";
				}
				
				JsonObject result = new JsonObject();
				result.addProperty("walletTxnStatus", walletTxnStatus);
				result.addProperty("txnID", txnID);
				result.addProperty("walletUserCode", walletUserCode);
				result.addProperty("txnSessionID", txnSessionID);
				result.addProperty("walletTxnID", walletTxnID);
				result.addProperty("walletTxnDateTime", walletTxnDateTime);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("walletBankRefID", walletBankRefID);
				result.addProperty("txnFor", txnFor);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("walletTxnRemarks", walletTxnRemarks);
				result.addProperty("adnlParam1", adnlParam1);
				result.addProperty("adnlParam2", adnlParam2);
				result.addProperty("adnlParam3", adnlParam3);
				result.addProperty("adnlParam4", adnlParam4);
				result.addProperty("adnlParam5", adnlParam5);
				result.addProperty("adnlParam6", adnlParam6);
				result.addProperty("adnlParam7", adnlParam7);
				result.addProperty("adnlParam8", adnlParam8);
				result.addProperty("adnlParam9", adnlParam9);
				result.addProperty("adnlParam10", adnlParam10);
				result.addProperty("pgMode", pgMode);
				result.addProperty("pgName", pgName);
				result.addProperty("challanTxnID", challanTxnID);
				result.addProperty("nachPaymentType", nachPaymentType);
				result.addProperty("nachPaymentDate", nachPaymentDate);
				result.addProperty("paymentMethod", paymentMethod);
				result.addProperty("nachPaymentDateAlt", nachPaymentDateAlt);
//				pgID = request.getParameter("PGID");
				txnSessionID = request.getParameter("PGHdrGUID");
				
				
				response.getWriter().print(new Gson().toJson(result));
//				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";
				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",paymentMethod="+paymentMethod+",pgName="+pgName+",pgMode="+pgMode+",nachType="+nachPaymentType+",nachSchDate="+yymmddDate+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+",localStorageRefNo="+ localStorageRefNo +")";
				response.getWriter().println("portalURL: "+portalURL);
				response.sendRedirect(portalURL);
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgResponseErrorMsg);
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void decryptAxisMessage(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam) throws IOException
	{/*
		byte[] output = null;
		String portalURL="";
		try
		{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String encryptionKey = "", merchantCode="", pgType="", re1="", parameterData="", version="", axisCurrency="", keyValue="", secretCode="";
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			response.getWriter().println("wholeParamString: "+wholeParamString);
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	response.getWriter().println(paramName+" ---> "+paramValue);
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Version"))
	        	{
	        		version = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Type"))
	        	{
	        		pgType = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("RE1"))
	        	{
	        		re1 = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("secretCode"))
	        	{
	        		secretCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey"))
	        	{
//	        		encryptionKey = paramValue;
	        		if(pgType.equalsIgnoreCase("TEST"))
	        		{
//	        			encryptionKey = properties.getProperty("axisEncryptionKeyUAT");
	        			encryptionKey = secretCode;
	        		}
	        		else
	        		{
	        			encryptionKey = properties.getProperty("axisEncryptionKeyPRD");
	        		}
	        	}
			}
			
//			response.getWriter().println("secretCode: "+secretCode);
//			encryptionKey="testbank12345678";
			response.getWriter().println("responseMessage: "+responseMessage);
		    SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
//		    response.getWriter().println("skey-getFormat: "+skey.getFormat());
//		    response.getWriter().println("skey-getAlgorithm: "+skey.getAlgorithm());
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		    response.getWriter().println("cipher: "+cipher.getAlgorithm());
		    cipher.init(Cipher.DECRYPT_MODE, skey);
		    output = cipher.doFinal(Base64.decode(responseMessage));
		    
		    response.getWriter().println("output: "+output);
		    
			String resParamName="", resParamValue="", resWholeParamString="";
			resWholeParamString = new String(output);
//			response.getWriter().println("resWholeParamString: "+resWholeParamString);
			String[] resSplitResult = resWholeParamString.split("\\&");
			String bankRefNo="", txnStatus="", remarks="", aggrTxnID="", txnDateTime="", pymtMode="", txnID="", txnSessionID="", versionNo="", resMerchantCode="", 
					resPGType="", customerNo="", currency="", txnAmount="", checkSum="";
			
			response.getWriter().println("Response: ");
			for(String s : resSplitResult)
			{
				resParamName = s.substring(0, s.indexOf("="));
				resParamValue = s.substring(s.indexOf("=")+1,s.length());
	        	
				response.getWriter().println(resParamName+" ---> "+resParamValue);
				
	        	if(resParamName.equalsIgnoreCase("BRN"))
	        	{
	        		bankRefNo = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("STC"))
	        	{
	        		txnStatus = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("RMK"))
	        	{
	        		remarks = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("TRN"))
	        	{
	        		aggrTxnID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("TET"))
	        	{
	        		txnDateTime = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("PMD"))
	        	{
	        		pymtMode = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("RID"))
	        	{
	        		txnID = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("VER"))
	        	{
	        		versionNo = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("CID"))
	        	{
	        		resMerchantCode = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("TYP"))
	        	{
	        		resPGType = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("CRN"))
	        	{
	        		customerNo = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("CNY"))
	        	{
	        		currency = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("AMT"))
	        	{
	        		txnAmount = resParamValue;
	        	}
	        	
	        	if(resParamName.equalsIgnoreCase("CKS"))
	        	{
	        		checkSum = resParamValue;
	        	}
			}
			
			portalURL = redirectURL+"/(txnStatus="+txnStatus+",txnID="+txnID+",bankRefNo="+bankRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";

			request.setAttribute("portalURL",portalURL);
			response.setContentType("text/html");
			RequestDispatcher view = request.getRequestDispatcher("/TopUp.jsp");
			view.forward(request,response);
		}
		catch (Exception e)
		{
//			System.out.println(e.toString());
		    JsonObject result = new JsonObject();
			result.addProperty("ErrorMessage", "Unable to decrypt the response");
			response.getWriter().print(new Gson().toJson(result));
		}
	*/}
	
	private void decryptYesPayUMessage(HttpServletRequest request, HttpServletResponse response, String responseMessage, String configurationValues, String redirectURL, String pgID, String pgHdrGUID, String siteID, String appID, String actionName, String navParam) throws IOException
	{
		try
		{
			String portalURL="", sDecryptionKey="", sSecureSecret = "";
			String txnRefNo="", lastName="", addressZip="", hashValidated="", errorMessage="", merchantId="", amount="", terminalId="", responseCode="", message="", retRefNo="", batchNo="", authCode="", bankID="", issuerRefNo="", firstName="";
			
			
			String wholeParamString="", paramName="", paramValue="";
			wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantId = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("EncryptionKey"))
	        	{
	        		sDecryptionKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecureSecret"))
	        	{
	        		sSecureSecret = paramValue;
	        	}
			}
			
			/*response.getWriter().println("merchantId: "+merchantId);
			response.getWriter().println("sDecryptionKey: "+sDecryptionKey);
			response.getWriter().println("sSecureSecret: "+sSecureSecret);*/
			
			
			ISGPayDecryption decObj = new ISGPayDecryption();
			LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();//decObj.decrypt(request, sDecryptionKey, sSecureSecret);
			Enumeration e = request.getParameterNames();

			while (e.hasMoreElements()) {

				String fieldName = (String) e.nextElement();
				String fieldValue = request.getParameter(fieldName);

				if(! fieldName.equalsIgnoreCase("PGParams"))
				{
					if ((fieldValue != null) && (fieldValue.length() > 0)) {
						hmDecryptedValue.put(fieldName, fieldValue);
					}
				}

			}
			
			decObj.decrypt(hmDecryptedValue, sDecryptionKey, sSecureSecret);
			//response.getWriter().println("decObj::::" +  hmDecryptedValue.get("SecureHash"));
			//response.getWriter().println("hmDecryptedValue:::"+hmDecryptedValue);
			
			txnRefNo = (String) hmDecryptedValue.get("TxnRefNo");
			merchantId = (String) hmDecryptedValue.get("MerchantId");
			amount = (String) hmDecryptedValue.get("Amount");
			terminalId = (String) hmDecryptedValue.get("TerminalId");
			responseCode = (String) hmDecryptedValue.get("ResponseCode");
			message = (String) hmDecryptedValue.get("Message");
			retRefNo = (String) hmDecryptedValue.get("RetRefNo");
			batchNo = (String) hmDecryptedValue.get("BatchNo");
			authCode = (String) hmDecryptedValue.get("AuthCode");
			bankID = (String) hmDecryptedValue.get("BankId");
			issuerRefNo = (String) hmDecryptedValue.get("issuerRefNo");
			firstName = (String) hmDecryptedValue.get("firstName");
			lastName = (String) hmDecryptedValue.get("lastName");
			addressZip = (String) hmDecryptedValue.get("addressZip");
			hashValidated = (String) hmDecryptedValue.get("hashValidated");
			errorMessage = (String) hmDecryptedValue.get("ErrorMessage");
			
			/*int intAmount = Integer.parseInt(amount)/100;
			amount= ""+intAmount;*/
			
			response.getWriter().println("txnRefNo:"+txnRefNo+"\nmerchantId:"+merchantId+"\namount:"+amount+"\nterminalId:"+terminalId
					+"\nresponseCode:"+responseCode+"\nmessage:"+message+"\nretRefNo:"+retRefNo+"\nbatchNo:"+batchNo+"\nauthCode:"+authCode
					+"\nbankID:"+bankID+"\nissuerRefNo:"+issuerRefNo+"\nfirstName:"+firstName+"\nlastName:"+lastName+"\naddressZip:"
					+addressZip+"\nhashValidated:"+hashValidated+"\nerrorMessage:"+errorMessage);
			
			if(!"No Value Returned".equals(errorMessage)){
				errorMessage = errorMessage;
			}else{
				errorMessage = "";
			}
			
			
			
			if(! "CORRECT".equals(hashValidated)){
				hashValidated = "INVALID HASH";
				JsonObject result = new JsonObject();
				result.addProperty("Error", hashValidated);
				result.addProperty("ErrorMessage", "Invalid Hash received. Data might be tampered with. Please contact system admin.");
				response.getWriter().print(new Gson().toJson(result));
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnRefNo+",bankRefNo="+retRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
				response.sendRedirect(portalURL);
			}
			else
			{
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnRefNo+",bankRefNo="+retRefNo+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+")";
//			response.getWriter().println("portalURL: "+portalURL);
				response.sendRedirect(portalURL);
			}
			/*else{
				hashValidated = "<font color='#FF0066'><strong>INVALID HASH</strong></font>";
			}*/
		}
		catch (Exception e)
		{
		    JsonObject result = new JsonObject();
			result.addProperty("ErrorMessage", "Unable to decrypt the response");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void decryptEazyPayResponse(HttpServletRequest request, HttpServletResponse response, String responseMessage, String redirectURL, String pgID, String pgHdrGUID, Map configMap, String localStorageRefNo) throws IOException
	{
		String portalURL="";
		try
		{
			String walletPublicKey = "", secretKey="", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			String wholeParamString="", paramName="", paramValue="",clientCode = "", encryptionKey="";
			/*wholeParamString = configurationValues;
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode"))
	        	{
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID"))
	        	{
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("clientCode"))
	        	{
	        		clientCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SecretKey"))
	        	{
	        		secretKey = paramValue;
	        	}
			}*/
			
			String key="", value="";
			Iterator<Map.Entry<String, String>> it = configMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<String, String> pair = it.next();
				
				key = pair.getKey();
				value =	pair.getValue();
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("\nMap Key:" + key+" | Value:"+value);
				
				if(key.equalsIgnoreCase("merchantCode"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nmerchant code");
					merchantCode = value;
				}
				/*else if(key.equalsIgnoreCase("pgUrl"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nURL");
					wsURL = value;
				}*/
				else if(key.equalsIgnoreCase("secretKey"))
				{
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().print("\nKey");
					secretKey = value;
				}
			}
			
			LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();//decObj.decrypt(request, sDecryptionKey, sSecureSecret);
			Enumeration e = request.getParameterNames();

			while (e.hasMoreElements()) {

				String fieldName = (String) e.nextElement();
				String fieldValue = request.getParameter(fieldName);

				if(! fieldName.equalsIgnoreCase("PGParams"))
				{
					if ((fieldValue != null) && (fieldValue.length() > 0)) {
						hmDecryptedValue.put(fieldName, fieldValue);
					}
				}

			}
			
			String txnID="", mandatoryfields="", optionalfields="", merchantId="", amount="", tps="", responseCode="", signature="", uniqueRefNo="", subMerchantId="", paymentMode="", tdr="", interchangeValue="", transactionDate="", totalAmount="", processingFeeAmount="", serviceTaxAmount="", errorMessage="";
			responseCode = (String) hmDecryptedValue.get("Response Code");
			uniqueRefNo = (String) hmDecryptedValue.get("Unique Ref Number");
			serviceTaxAmount = (String) hmDecryptedValue.get("Service Tax Amount");
			processingFeeAmount = (String) hmDecryptedValue.get("Processing Fee Amount");
			totalAmount = (String) hmDecryptedValue.get("Total Amount");
			amount = (String) hmDecryptedValue.get("Transaction Amount");
			transactionDate = (String) hmDecryptedValue.get("Transaction Date");
			interchangeValue = (String) hmDecryptedValue.get("Interchange Value");
			tdr = (String) hmDecryptedValue.get("TDR");
			paymentMode = (String) hmDecryptedValue.get("Payment Mode");
			subMerchantId = (String) hmDecryptedValue.get("SubMerchantId");
			txnID = (String) hmDecryptedValue.get("ReferenceNo");
			tps = (String) hmDecryptedValue.get("TPS");
			merchantId = (String) hmDecryptedValue.get("ID");
			signature = (String) hmDecryptedValue.get("RSV");
			mandatoryfields=(String) hmDecryptedValue.get("mandatory fields");
			optionalfields=(String) hmDecryptedValue.get("optional fields");
			boolean isValidResponse = true;
//			isValidResponse = verifySignature(signature, secretKey);
			
			if(isValidResponse)
			{
				JsonObject result = new JsonObject();
				result.addProperty("responseCode", responseCode);
				result.addProperty("BankRefNo", uniqueRefNo);
				result.addProperty("txnID", txnID);
				result.addProperty("serviceTaxAmount", serviceTaxAmount);
				result.addProperty("processingFeeAmount", processingFeeAmount);
				result.addProperty("totalAmount", totalAmount);
				result.addProperty("amount", amount);
				result.addProperty("transactionDate", transactionDate);
				result.addProperty("interchangeValue", interchangeValue);
				result.addProperty("tdr", tdr);
				result.addProperty("paymentMode", paymentMode);
				result.addProperty("subMerchantId", subMerchantId);
				result.addProperty("tps", tps);
				result.addProperty("merchantId", merchantId);
				result.addProperty("signature", signature);
				result.addProperty("mandatoryfields", mandatoryfields);
				result.addProperty("optionalfields", optionalfields);
				
				String pgName="", pgMode="", nachPaymentType="", yymmddDate="", currency="INR";
				
				response.getWriter().print(new Gson().toJson(result));
//				portalURL = redirectURL+"/(txnStatus="+walletTxnStatus+",txnID="+txnID+",currency="+adnlParam10+",txnSessionID="+pgHdrGUID+",txnAmount="+txnAmount+",source=PG,PGID="+pgID+")";
				portalURL = redirectURL+"/(txnStatus="+responseCode+",txnID="+txnID+",paymentMethod="+paymentMode+",pgName="+pgName+",pgMode="+pgMode+",nachType="+nachPaymentType+",nachSchDate="+yymmddDate+",currency="+currency+",txnSessionID="+pgHdrGUID+",txnAmount="+amount+",source=PG,PGID="+pgID+",localStorageRefNo="+ localStorageRefNo +")";
				response.getWriter().print("portalURL: "+portalURL);
				response.sendRedirect(portalURL);
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("Response", "Signature not matched");
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			JsonObject result = new JsonObject();
			result.addProperty("pgReqMsg", "Exception in Decrypting message. Please contact administrator");
			result.addProperty("exception", e.getMessage());
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private boolean verifySignature(String responsePayLoadKey, String secretKey) throws IOException
	{
		boolean isValidResponse = false;
		try
		{
			String localHashString="", responseHashSignature="", localHashSignature=""; 
			
			responseHashSignature  = new ResPymtGatewayRazor().hashCal("SHA-512",responsePayLoadKey);
			
			localHashString = responsePayLoadKey;
			localHashString = localHashString.replaceAll(localHashString.substring(localHashString.lastIndexOf("|")+1, localHashString.length()), secretKey);
			
			localHashSignature = new ResPymtGatewayRazor().hashCal("SHA-512",localHashString);
			
			if(responseHashSignature.equals(localHashSignature))
			{
				isValidResponse = true;
			}
		}
		catch (Exception e) {
		}
		return isValidResponse;
	}
	
	public String hashCal(String type,String str)
	{
		byte[] hashseq=str.getBytes();
		StringBuffer hexString = new StringBuffer();
		try
		{
			MessageDigest algorithm = MessageDigest.getInstance(type);
			algorithm.reset();
			algorithm.update(hashseq);
			byte messageDigest[] = algorithm.digest();
			for (int i=0;i<messageDigest.length;i++) 
			{
				String hex=Integer.toHexString(0xFF & messageDigest[i]);
				if(hex.length()==1) hexString.append("0");
				hexString.append(hex);
			}
		}
		catch(NoSuchAlgorithmException nsae)
		{
			
		}
		return hexString.toString();
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

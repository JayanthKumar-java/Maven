package com.arteriatech.pg;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class PostManTest
 */
@WebServlet("/PostManTest")
public class PostManTest extends HttpServlet {/*
	private static final long serialVersionUID = 1L;
    
//	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
//	private static final String SF_CUSTOMER_DEST_NAME =  "sfmst";
//	private static final String PCGW_DEST_NAME =  "pcgw";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	public static SSLSocketFactory s_sslSocketFactory = null;
	public static X509TrustManager s_x509TrustManager = null;
    *//**
     * @see HttpServlet#HttpServlet()
     *//*
    public PostManTest() {
        super();
        // TODO Auto-generated constructor stub
    }

	*//**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//set response type as plain text.
		response.setContentType("application/json");
		// Check for a logged in user
		LOGGER.info("1. doGet function starts");
		try{
			
			 * Code snip for user roles
			 * 
			boolean isGroupAvailable = false;
			isGroupAvailable = readUserPrincipal(request,response);
			
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", configurationValues="",merchantCode="", balanceEnquiryCall="";
			String customerNo="", application="", loginSessionID="", pgID = "", wsUrl = "", clientCode = "";
			boolean isValidUser = false;
			
			if(null != request.getParameter("CustomerNo"))
				customerNo = request.getParameter("CustomerNo");
//			else
//				customerNo = "1000";
			if(null != request.getParameter("Application"))
				application = request.getParameter("Application");
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
//			else
//				pgID = "B2BIZ";
			
			loginSessionID = request.getParameter("sessionID");
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isGroupAvailable: "+isGroupAvailable);
			if(! isGroupAvailable)
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("Inside IF");
				isValidUser = getCustomers(request, response, loginSessionID, customerNo);
			}
			else
			{
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("Inside ELSE");
				isValidUser = true;
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isGroupAvailable: "+isGroupAvailable);
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("isValidUser: "+isValidUser);
			
			if(isValidUser)
			{
				Properties properties = new Properties();
				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				balanceEnquiryCall = properties.getProperty("balanceEnquiryCall");
				
				configurationValues = getConstantValues(request, response, "", pgID);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().print("configurationValues: "+configurationValues);
				
				String wholeParamString="", paramName="", paramValue="";
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
		        	
		        	if(paramName.equalsIgnoreCase("WSURL"))
		        	{
		        		wsUrl = paramValue;
		        	}
		        	if(paramName.equalsIgnoreCase("ClientCode"))
		        	{
		        		clientCode = paramValue.toString().trim();
		        	}
				}
				
//				walletPublicKey = properties.getProperty("walletPublicKey");
//				merchantPrivateKey = properties.getProperty("merchantPrivateKey");
//				merchantPublicKey = properties.getProperty("merchantPublicKey");
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
				
				WalletParamMap inputParamMap = new WalletParamMap();
				if(null !=request.getParameter("wallet-user-code"))
					inputParamMap.put("wallet-user-code", request.getParameter("wallet-user-code"));

				WalletAPI getResponse = new WalletAPI();
				WalletParamMap responseMap = getResponse.callWalletAPI(wsUrl, balanceEnquiryCall, merchantCode, inputParamMap, walletPublicKey, merchantPrivateKey);
				
				String walletusercode="", userbalance="", checksum="", errorStatus="", errorMsg="";
				if(null != responseMap.get("checksum") && (null != responseMap.get("wallet-user-code") && responseMap.get("wallet-user-code").toString().trim().length() > 0))
				{
					walletusercode = responseMap.get("wallet-user-code");
					userbalance = responseMap.get("user-balance");
					checksum = responseMap.get("checksum");
					
					JsonObject result = new JsonObject();
					result.addProperty("walletusercode", walletusercode);
					result.addProperty("userbalance", userbalance);
					result.addProperty("checksum", checksum);
					result.addProperty("Valid", "true");
					response.getWriter().print(new Gson().toJson(result));
				}
				else
				{
//				response.getWriter().print("error");
					errorStatus = responseMap.get("error_status");
					errorMsg = responseMap.get("error_message");
					JsonObject result = new JsonObject();
					result.addProperty("errorStatus", errorStatus);
					result.addProperty("errorMsg", errorMsg);
					result.addProperty("Valid", "false");
					response.getWriter().print(new Gson().toJson(result));
				}
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", customerNo+" is not authorized to view Wallet Account balance.");
				result.addProperty("walletClientCode", customerNo);
				result.addProperty("Valid", "false");
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) { 
			response.getWriter().println("Error: No Login ID found");
		}
//		LOGGER.info("doGet function end");	
	
	}
	private boolean readUserPrincipal(HttpServletRequest request, HttpServletResponse response)
	{

		
		com.sap.security.um.user.User user = null;
		boolean isGroupAvailable=false;
		try
		{
			response.getWriter().println("Inside readUserPrincipal");
			response.getWriter().println("getUserPrincipal: "+request.getUserPrincipal());
			InitialContext ctx = new InitialContext();
			UserProvider userProvider = (UserProvider) ctx.lookup("java:comp/env/user/Provider");
			response.getWriter().println("userProvider: "+userProvider);
			response.getWriter().println("userProvider.getUser: "+userProvider.getUser(request.getUserPrincipal().getName()));
			if (request.getUserPrincipal() != null) {
			     user = userProvider.getUser(request.getUserPrincipal().getName());
			     
			     if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			     {
				     response.getWriter().println("User Properties: "+user);
				     response.getWriter().println("User email: "+user.getAttribute("email"));
				     response.getWriter().println("User getGroups: "+user.getGroups());
				     response.getWriter().println("User getRoles: "+user.getRoles());
				     response.getWriter().println("User getName: "+user.getName());
				     response.getWriter().println("User getLocale: "+user.getLocale());
				     response.getWriter().println("User name: "+user.getAttribute("name"));
				     response.getWriter().println("User lastname: "+user.getAttribute("lastname"));
				     response.getWriter().println("User firstname: "+user.getAttribute("firstname"));
				     response.getWriter().println("User saml2AssertionGroups: "+user.getAttribute("saml2AssertionGroups"));
				     response.getWriter().println("User roleProvider: "+user.getAttribute("roleProvider"));
				     response.getWriter().println("User getAttribute.groups: "+Arrays.toString(user.getAttributeValues("groups")));
			     }
//			     List array = Arrays.asList(user.getAttributeValues("groups"));
			     
			     if(null != Arrays.toString(user.getAttributeValues("groups")))
			     {
				     for (String str : Arrays.asList(user.getAttributeValues("groups")))
				     {
				    	 if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				    		 response.getWriter().println("str: "+str);
				    	 if(str.equalsIgnoreCase("PY_ESCROW_SUPERUSER"))
				    	 {
				    		 isGroupAvailable = true;
				    		 break;
				    	 }
				     }
			     }
			     
			     if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
		    		 response.getWriter().println("isGroupAvailable 1: "+isGroupAvailable);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally
		{
			return isGroupAvailable;
		}
	
	}

	private boolean getCustomers(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String customerNo) throws IOException, URISyntaxException
	{
		HttpGet customerRequest = null;
		HttpEntity customerEntity = null;
		boolean isValidCustomer = false;

		try{
			String CustomerService = "";
			String CustomerFilter = "";
				
			CustomerFilter = "LoginID eq '"+loginSessionID+"'";
			
			CustomerFilter = URLEncoder.encode(CustomerFilter, "UTF-8");
			
			CustomerFilter = CustomerFilter.replaceAll("%26", "&");
			CustomerFilter = CustomerFilter.replaceAll("%3D", "=");
			
			String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME);
			
			HttpDestination destination = getHTTPDestinationForCustomers(request, response, PCGW_UTIL_DEST_NAME);
//			response.getWriter().println("destination getHost: "+destination.getURI().getHost());
//			response.getWriter().println("destination getPort: "+destination.getURI().getPort());
//			response.getWriter().println("destination getRawPath: "+destination.getURI().getRawPath());
//			response.getWriter().println("destination getName: "+destination.getName());
//			response.getWriter().println("destination getName: "+destination.);
			if(sapclient != null)
			{
				CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?sap-client=" + sapclient  + "&$filter="+ CustomerFilter;
			}
			else
			{
				CustomerService = destination.getURI().getPath()+"/sap/opu/odata/ARTEC/SFGW_MST/UserCustomers?$filter="+ CustomerFilter;
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("CustomerService: "+CustomerService);
			HttpClient httpClient = destination.createHttpClient();
			customerRequest = new HttpGet(CustomerService);
			HttpResponse customerResponse = httpClient.execute(customerRequest);

			// Copy content from the incoming response to the outgoing response
			customerEntity = customerResponse.getEntity();
			
			if(customerEntity != null)
			{
		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
		        InputSource inputSource;
		        
				String retSrc = EntityUtils.toString(customerEntity);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("retSrc: "+retSrc);
				
				docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            document.getChildNodes().getLength();
	            NodeList nodeList = document.getElementsByTagName("d:CustomerNo");
	            NodeList entryList = document.getElementsByTagName("entry");
	            for(int i=0 ; i<nodeList.getLength() ; i++)
	            {
	            	if(customerNo.equalsIgnoreCase(nodeList.item(i).getTextContent()))
        			{
	            		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
	            		{
	            			response.getWriter().println("customerNo: "+customerNo);
	            			response.getWriter().println("nodeList.item(i).getTextContent(): "+nodeList.item(i).getTextContent());
	            			response.getWriter().println("true");
	            		}
	            		isValidCustomer = true;
	            		break;
        			}
	            }
			}
			EntityUtils.consume(customerEntity);
		}catch (RuntimeException e) {
			customerRequest.abort();
		} catch (DestinationException e) {
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			return isValidCustomer;
		}
	}
	private String getHTTPDestinationConfiguration(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
			{
				destinationName = destName;
			}
		else if (destinationName == null) {
				destinationName = PCGW_UTIL_DEST_NAME;
			}
				
		String tempSapClient = null;
				
		try {
				// get all destination properties
				Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
//				response.getWriter().println("ConnectivityConfiguration: "+ configuration);
				DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
//				response.getWriter().println("DestinationConfiguration: "+ destConfiguration);
				Map<String, String> allDestinationPropeties = destConfiguration.getAllProperties();
							
				//Reading SAP-Client from All Configuration.........................................................
				tempSapClient = allDestinationPropeties.get("sap-client");
//				response.getWriter().println("tempSapClient: "+ tempSapClient);
			}
			catch (NamingException e) {
				// Connectivity operation failed
				String errorMessage = "Connectivity operation failed with reason: "
							+ e.getMessage()
							+ ". See "
							+ "logs for details. Hint: Make sure to have an HTTP proxy configured in your HCP";
				LOGGER.error("Connectivity operation failed", e);
				//	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
				//response.getWriter().println("Error: " +  errorMessage);
			}
		
		return tempSapClient;
	}
	
	private HttpDestination getHTTPDestinationForCustomers(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		//get the destination name from the request
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = PCGW_UTIL_DEST_NAME;
		}
//		response.getWriter().println("destinationName: "+destinationName);
		HttpDestination  destination = null;
		try {
			// look up the connectivity configuration API "DestinationFactory"
//			response.getWriter().println("Inside try");
			Context ctxDestFact = new InitialContext();
//			response.getWriter().println("ctxDestFact: "+ctxDestFact.toString());
			//Get HTTP destination 
			DestinationFactory destinationFactory = (DestinationFactory) ctxDestFact.lookup(DestinationFactory.JNDI_NAME);
//			response.getWriter().println("destinationFactory: "+destinationFactory.toString());
			if(destinationFactory != null)
			{
				destination = (HttpDestination) destinationFactory.getDestination(destinationName);
			}
//			response.getWriter().println("destination: "+destination);
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
	}
	
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		HttpGet constantRequest = null;
		HttpEntity constantEntity = null;
		String configurableValues = "", pgID="";
		pgID = PGID;
		String sapclient = getHTTPDestinationConfiguration(request, response, PCGW_UTIL_DEST_NAME);
		try
		{
			String constantValuesService = "/sap/opu/odata/ARTEC/PCGW/PGPaymentConfigs";
			String constantValuesFilter = "";
			//PG ID 10 is for ICICI
			if(null != PGID && PGID.toString().length() > 0)
			{
//				if(PGCategoryID.equalsIgnoreCase("10"))
				{
					if(constantValuesFilter == null || constantValuesFilter == "")
					{
						//Uncomment middle line or last line when this service will be filterable . Login ID is not required for this.
//						constantValuesFilter = "LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"'";
					}
					else
					{
						//Uncomment middle line or last line when this service will be filterable. Login ID is not required for this.
//						constantValuesFilter = constantValuesFilter+" and LoginID eq '"+loginSessionID+"'";
//						constantValuesFilter = constantValuesFilter+"LoginID eq '"+loginSessionID+"'and PGCategoryID eq '"+PGCategoryID+"'";
						constantValuesFilter = constantValuesFilter+"PGID eq '"+PGID+"'";
					}
//					response.getWriter().println("constantValuesFilter: "+constantValuesFilter);
					HttpDestination destination = getHTTPDestination(request, response, PCGW_UTIL_DEST_NAME);
					constantValuesFilter = URLEncoder.encode(constantValuesFilter, "UTF-8");
					if(sapclient != null)
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?sap-client="+ sapclient +"&$filter="+constantValuesFilter;
					}
					else
					{
						constantValuesService = destination.getURI().getPath()+constantValuesService+"?$filter="+constantValuesFilter;
					}
					//Uncomment the above code if filter is required. Below code is temporary one without any filters
//					constantValuesService = destination.getURI().getPath()+constantValuesService;
//					response.getWriter().println("URL: "+constantValuesService);
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
						if(PGID.equalsIgnoreCase("B2BIZ"))
						{
					        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					        DocumentBuilder docBuilder;
					        InputSource inputSource;
							String retSrc = EntityUtils.toString(constantEntity); 
	//						response.getWriter().println("retSrc: "+retSrc);
							
							docBuilder = docBuilderFactory.newDocumentBuilder();
							inputSource = new InputSource(new StringReader(retSrc));
				            Document document = docBuilder.parse(inputSource);
				            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
				            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
				            NodeList pdIDList = document.getElementsByTagName("d:PGID");
				            NodeList wsUrlList = document.getElementsByTagName("d:AccBalURL");
				            NodeList clientCodeList = document.getElementsByTagName("d:ClientCode");
				            
				            for(int i=0 ; i<pdIDList.getLength() ; i++)
				            {
	//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
				            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
			        			{
				            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+"|"+"PGID="+pdIDList.item(i).getTextContent()
				            				+"|"+"WSURL="+wsUrlList.item(i).getTextContent()+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
				            		break;
			        			}
				            }
						}
						if(retSrc.contains("<d:MerchantCode>")) {
							configurableValues = retSrc.split("<d:MerchantCode>")[1].split("</d:MerchantCode>")[0];
						}
					}
//					
//					response.getWriter().println("configurableValues: "+configurableValues);
//					response.getOutputStream().print(Integer.parseInt(EntityUtils.toString(constantEntity)));	
//					EntityUtils.consume(constantEntity);
//					response.getOutputStream().print
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
	}
	
	private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
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
	}
	
	*//**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

*/}

package com.arteriatech.pg;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class TestOData
 */
@WebServlet("/TestOData")
public class TestOData extends HttpServlet {/*
	private static final long serialVersionUID = 1L;
       
    *//**
     * @see HttpServlet#HttpServlet()
     *//*
    public TestOData() {
        super();
        // TODO Auto-generated constructor stub
    }

	*//**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String oDataURL = "", loginID="";
		CommonUtils utils = new CommonUtils();
//		response.getWriter().println("Inside doGet");
		oDataURL = utils.getODataDestinationProperties("URL", "PYGWHANA");
		if(oDataURL != null && ! oDataURL.equalsIgnoreCase("E106") && ! oDataURL.contains("E173")){
			loginID = utils.getODataDestinationProperties("User", "PYGWHANA");
			if(loginID != null && ! loginID.equalsIgnoreCase("E106") && ! loginID.contains("E173")){
				executeODataService(request, response, oDataURL);
			}
		}
	}
	
	public void executeODataService(HttpServletRequest request, HttpServletResponse response, String oDataURL){
		try{
			String serviceURL = "";
			String serviceName="UserCustomers";
			HttpGet readRequest = null;
			HttpEntity countEntity = null;
			try{
//				serviceURL = request.getPathInfo(); //request.getParameter("service");
				serviceURL = oDataURL+serviceName;
				serviceURL = serviceURL.replaceAll("%20", " ");
				String filters = "";
//				response.getWriter().println("serviceURL 1: "+serviceURL);
//				if(!serviceURL.contains("$metadata")) {
					response.getWriter().println("filters Length: "+ Service.split("&$filter").length);
					if(Service.split("$filter").length > 1) {
						filters = Service.split("$filter")[2];
						if(filters.contains("&")) {
							filters = filters.split("&")[1];
							filters = filters + "&";
						}
						filters = Service.replaceFirst(filters, "");
					}
					filters = request.getParameter("$filter");
					
//					response.getWriter().println("filters1: "+filters);
					if(filters == null || filters == "") {
						filters = "LoginID eq '"+loginSessionID+"'";
					}		
					else {
						filters = filters+" and LoginID eq '"+loginSessionID+"'";
					}
					filters = URLEncoder.encode(filters, "UTF-8");
					String filterKey = "?$filter=";
//					filters=filterKey.concat(filters);
					filters=filterKey+filters;
//					response.getWriter().println("filters2: "+filters);
					if(filters != null && filters != "") {
						serviceURL = serviceURL.concat(filters);
					}
					String select = request.getParameter("$select");
					if(select != null && select != "") {
						serviceURL = serviceURL.concat("&$select="+select);
					}
					String skip = request.getParameter("$top");
					if(skip != null && skip != "") {
						serviceURL = serviceURL.concat("&$skip="+skip);
					}
					String top = request.getParameter("$top");
					if(top != null && top != "") {
						serviceURL = serviceURL.concat("&$top="+top);
					}
//				}
				serviceURL = serviceURL.replaceAll("%20", " ");
//				response.getWriter().println("serviceURL 1: "+serviceURL);
				HttpDestination destination = getHTTPDestination(request, response, "PYGWHANA");
//				String sapclient = getHTTPDestinationConfiguration(request, response, PCGWUTILS_DEST_NAME);
//				if(sapclient != null)
//				{
//					Service = destination.getURI().getPath()+Service+"&sap-client="+sapclient;
//				}
//				else
//				{
//				serviceURL = destination.getURI().getPath()+serviceURL;
//				}

//				response.getWriter().println("serviceURL 2: "+serviceURL);
				
				HttpClient httpClient = destination.createHttpClient();
				readRequest = new HttpGet(serviceURL);
//				readRequest.addHeader("x-arteria-loginid", loginSessionID);
				HttpResponse serviceResponse = httpClient.execute(readRequest);
				countEntity = serviceResponse.getEntity();
//				response.getWriter().println("serviceResponse: "+serviceResponse);
				if(serviceResponse.getEntity().getContentType() != null && serviceResponse.getEntity().getContentType().toString() != "") {
					String contentType = serviceResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
//					response.getWriter().println("contentType: "+contentType);
					if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
						String sCountEntity = EntityUtils.toString(countEntity);
						String messageCode = sCountEntity.split("<code>")[1].split("</code>")[0];
//						response.getWriter().println("sCountEntity: "+sCountEntity);
//						response.getWriter().println("messageCode: "+messageCode);
//						if(messageCode.equalsIgnoreCase("ZSCM_SP_MSG/018")) {
//							response.getOutputStream().print("Message: No amendments available");
//						}else 
						{
							response.setContentType(contentType);
							response.getOutputStream().print(EntityUtils.toString(countEntity));	
						}
					}else {
						response.setContentType(contentType);
						String Data = EntityUtils.toString(countEntity);
						response.getOutputStream().print(Data);	
					}
				}else{
					response.setContentType("application/pdf");
					response.getOutputStream().print(EntityUtils.toString(countEntity));	
				}
			}catch (RuntimeException e) {
				if(readRequest != null){
					readRequest.abort();
				}
				response.getWriter().println("Error1: " +  e.getMessage());
			} catch (DestinationException e) {
				response.getWriter().println("Error2: " +  e.getMessage());
			}
		
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private HttpDestination getHTTPDestination(HttpServletRequest request, HttpServletResponse response, String destName) throws IOException{
		String destinationName = request.getParameter("destname");
		//check destination null condition
		if(destName != "")
		{
			destinationName = destName;
		}
		else if (destinationName == null) {
			destinationName = "PYGWHANA";
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
	}

	*//**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

*/}

package com.arteriatech.pg;

import java.io.IOException;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.arteriatech.logs.ODataLogs;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;

/**
 * Servlet implementation class PGPush
 */
@WebServlet("/PGPush")
public class PGPush extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CPI_CONNECT_DEST_NAME =  "CPIConnect";   
	String servletPath="";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PGPush() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		int stepNo=0;
		ODataLogs oDataLogs = new ODataLogs();
		boolean debug = false;
		CommonUtils commonUtils = new CommonUtils();
		debug = true;
		String oDataURL="", userName="", password="", userPass="", aggregatorID="", loginID="";
		String ccAvenueParam="", ingenicoParam="", logID="", responseFromCPI="";
		boolean failedResponse = false;
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			servletPath = request.getServletPath();
			
			if(debug){
				response.getWriter().println("servletPath: "+servletPath);
				response.getWriter().println("request.getPathInfo: "+request.getPathInfo());
			}
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			if(debug){
				response.getWriter().println("logID: "+logID);
				response.getWriter().println("oDataURL: "+oDataURL);
				response.getWriter().println("aggregatorID: "+aggregatorID);
				response.getWriter().println("userPass: "+userPass);
				if(loginID == null || loginID.trim().length() == 0)
					response.getWriter().println("Anonymous Access");
				else
					response.getWriter().println("Authenticated with loginID: "+loginID);
			}
			
			if(! servletPath.equalsIgnoreCase("/Razorpay")){
				stepNo = stepNo+1;
				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Process Started", ""+stepNo, servletPath, oDataURL, userPass, aggregatorID, loginID, debug);
				stepNo = stepNo+1;
				//if(debug)
//					response.getWriter().println("stepNo1: "+stepNo);
//					response.getWriter().println("logID: "+logID);
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Hit received on this path: "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
				stepNo = stepNo+1;
			}
			
			if(servletPath.equalsIgnoreCase("/CCAVENUE") || servletPath.equalsIgnoreCase("/CCAICICI")){
				ccAvenueParam = "";
				if(request.getPathInfo().equalsIgnoreCase("/STATUSECHO")){
					if(debug){
						response.getWriter().println("CCAVENUE/STATUSECHO");
						response.getWriter().println("referrer: "+request.getParameter("referer"));
						response.getWriter().println("getHeader.referrer: "+request.getHeader("referer"));
						response.getWriter().println("encResp: "+request.getParameter("encResp"));
						response.getWriter().println("getHeader.encResp: "+request.getHeader("encResp"));
					}
					
					if(request.getHeader("referer") != null && request.getHeader("referer").trim().length() > 0){
						ccAvenueParam = request.getHeader("referer");						
					}else if(request.getParameter("referer") != null && request.getParameter("referer").trim().length() > 0){
						ccAvenueParam = request.getParameter("referer");
					}else if(request.getParameter("encResp") != null && request.getParameter("encResp").trim().length() > 0){
						ccAvenueParam = request.getParameter("encResp");
					}else if(request.getHeader("encResp") != null && request.getHeader("encResp").trim().length() > 0){
						ccAvenueParam = request.getHeader("encResp");
					}
					
//					if(debug)
//						response.getWriter().println("ccAvenueParam: "+ccAvenueParam);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Bank: "+ccAvenueParam, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
					//if(debug)
//					response.getWriter().println("stepNo3: "+stepNo);
//					response.getWriter().println("logID3: "+logID);
					if(ccAvenueParam.trim().length() > 0){
						responseFromCPI = sendMessageToCPI(request, response, ccAvenueParam, loginID, logID, stepNo, debug);
					}else{
//						Parameter not received - insert into application logs
//						logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Parameter not Received from "+servletPath, oDataURL, userPass, aggregatorID, loginID, debug);
						oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
						stepNo = stepNo+1;
						responseFromCPI = "Blank request received";
						failedResponse = true;
						//if(debug)
//						response.getWriter().println("stepNo4: "+stepNo);
//						response.getWriter().println("logID4: "+logID);
					}
					if(debug)
						response.getWriter().println("logID: "+logID);
				}else if(request.getPathInfo().equalsIgnoreCase("/RECON")){
					if(debug){
						response.getWriter().println("RECON");
						response.getWriter().println("referrer: "+request.getParameter("referer"));
						response.getWriter().println("getHeader.referrer: "+request.getHeader("referer"));
						response.getWriter().println("encResp: "+request.getParameter("encResp"));
						response.getWriter().println("getHeader.encResp: "+request.getHeader("encResp"));
					}
//					order recon here
//					logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Invalid Request"+request.getPathInfo(), oDataURL, userPass, aggregatorID, loginID, debug);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
					responseFromCPI = "Unrecognized request received";
					failedResponse = true;
					//if(debug)
//					response.getWriter().println("stepNo5: "+stepNo);
//					response.getWriter().println("logID5: "+logID);
				}
				if(failedResponse)
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				else
					response.setStatus(HttpServletResponse.SC_OK);
				
				response.getWriter().println(responseFromCPI);
			}else if(servletPath.equalsIgnoreCase("/MOBILECCA")){
				ccAvenueParam = "";
				if(request.getPathInfo().equalsIgnoreCase("/STATUSECHO")){
					if(debug){
						response.getWriter().println("CCAVENUE/STATUSECHO");
						response.getWriter().println("referrer: "+request.getParameter("referer"));
						response.getWriter().println("getHeader.referrer: "+request.getHeader("referer"));
						response.getWriter().println("encResp: "+request.getParameter("encResp"));
						response.getWriter().println("getHeader.encResp: "+request.getHeader("encResp"));
					}
					
					if(request.getHeader("referer") != null && request.getHeader("referer").trim().length() > 0){
						ccAvenueParam = request.getHeader("referer");						
					}else if(request.getParameter("referer") != null && request.getParameter("referer").trim().length() > 0){
						ccAvenueParam = request.getParameter("referer");
					}else if(request.getParameter("encResp") != null && request.getParameter("encResp").trim().length() > 0){
						ccAvenueParam = request.getParameter("encResp");
					}else if(request.getHeader("encResp") != null && request.getHeader("encResp").trim().length() > 0){
						ccAvenueParam = request.getHeader("encResp");
					}
					
//					if(debug)
//						response.getWriter().println("ccAvenueParam: "+ccAvenueParam);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Bank: "+ccAvenueParam, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
					//if(debug)
//					response.getWriter().println("stepNo3: "+stepNo);
//					response.getWriter().println("logID3: "+logID);
					if(ccAvenueParam.trim().length() > 0){
						responseFromCPI = sendMessageToCPI(request, response, ccAvenueParam, loginID, logID, stepNo, debug);
					}else{
//						Parameter not received - insert into application logs
//						logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Parameter not Received from "+servletPath, oDataURL, userPass, aggregatorID, loginID, debug);
						oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
						stepNo = stepNo+1;
						responseFromCPI = "Blank request received";
						failedResponse = true;
						//if(debug)
//						response.getWriter().println("stepNo4: "+stepNo);
//						response.getWriter().println("logID4: "+logID);
					}
					if(debug)
						response.getWriter().println("logID: "+logID);
				}else if(request.getPathInfo().equalsIgnoreCase("/RECON")){
					if(debug){
						response.getWriter().println("RECON");
						response.getWriter().println("referrer: "+request.getParameter("referer"));
						response.getWriter().println("getHeader.referrer: "+request.getHeader("referer"));
						response.getWriter().println("encResp: "+request.getParameter("encResp"));
						response.getWriter().println("getHeader.encResp: "+request.getHeader("encResp"));
					}
//					order recon here
//					logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Invalid Request"+request.getPathInfo(), oDataURL, userPass, aggregatorID, loginID, debug);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
					responseFromCPI = "Unrecognized request received";
					failedResponse = true;
					//if(debug)
//					response.getWriter().println("stepNo5: "+stepNo);
//					response.getWriter().println("logID5: "+logID);
				}
				if(failedResponse)
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				else
					response.setStatus(HttpServletResponse.SC_OK);
				
				response.getWriter().println(responseFromCPI);
			}else if(servletPath.equalsIgnoreCase("/INGENICO")){
				ingenicoParam = "";
				
				if(debug){
					response.getWriter().println("INGENICO");
					response.getWriter().println("msg: "+request.getParameter("msg"));
					response.getWriter().println("getHeader.msg: "+request.getHeader("msg"));
				}
				
				if(request.getParameter("msg") != null && request.getParameter("msg").trim().length() > 0){
					ingenicoParam = request.getParameter("msg");
				}else if(request.getHeader("msg") != null && request.getHeader("msg").trim().length() > 0){
					ingenicoParam = request.getHeader("msg");
				}
				
				ingenicoParam = URLEncoder.encode(ingenicoParam,"UTF-8").replaceAll("\\+", "%20");
				if(debug)
					response.getWriter().println("ingenicoParam: "+ingenicoParam);
				
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Bank: "+ingenicoParam, stepNo, oDataURL, userPass, aggregatorID, debug);
				stepNo = stepNo+1;
				//if(debug)
//				response.getWriter().println("stepNo6: "+stepNo);
//				response.getWriter().println("logID6: "+logID);
				if(ingenicoParam.trim().length() > 0){
//					sendMessageToCPI(request, response, "msg="+ingenicoParam, loginID, logID, debug);
					responseFromCPI = sendIngenicoMessageToCPI(request, response, "msg="+ingenicoParam, loginID, logID, stepNo, debug);
				}else{
//					logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Parameter not Received from "+servletPath, oDataURL, userPass, aggregatorID, loginID, debug);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
					failedResponse = true;
					//if(debug)
//					response.getWriter().println("stepNo7: "+stepNo);
//					response.getWriter().println("logID7: "+logID);
					responseFromCPI = "Blank request received";
					if(debug)
						response.getWriter().println("logID: "+logID);
				}
				
				if(failedResponse)
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				else
					response.setStatus(HttpServletResponse.SC_OK);
				
				response.getWriter().println(responseFromCPI);
			}else if(servletPath.equalsIgnoreCase("/Razorpay")){
				stepNo = stepNo+1;
				String pathInfo="", requestBody="";
				String razorPaySignature="", webhookSecret="";
				pathInfo = request.getPathInfo();
				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Process Started", ""+stepNo, servletPath+"|"+(pathInfo.substring(1, pathInfo.length()-1)), oDataURL, userPass, aggregatorID, loginID, debug);
				stepNo = stepNo+1;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Hit received on this path: "+servletPath+"|"+(pathInfo.substring(1, pathInfo.length()-1)), stepNo, oDataURL, userPass, aggregatorID, debug);
				stepNo = stepNo+1;
				
				if(request.getPathInfo().equalsIgnoreCase("/AuthorizedPayments")){
					requestBody = commonUtils.getGetBody(request, response);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Razorpay: "+requestBody, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
				}else if(request.getPathInfo().equalsIgnoreCase("/FailedPayments")){
					requestBody = commonUtils.getGetBody(request, response);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Razorpay: "+requestBody, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
				}else if(request.getPathInfo().equalsIgnoreCase("/CapturedPayments")){
					requestBody = commonUtils.getGetBody(request, response);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Razorpay: "+requestBody, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
				}else if(request.getPathInfo().equalsIgnoreCase("/PaidOrders")){
					requestBody = commonUtils.getGetBody(request, response);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Request From Razorpay: "+requestBody, stepNo, oDataURL, userPass, aggregatorID, debug);
					stepNo = stepNo+1;
				}
				if(debug)
					response.getWriter().println("Razorpay requestBody: "+requestBody);
				
				
			}else{
//				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", "Invalid Request received from: "+servletPath, oDataURL, userPass, aggregatorID, loginID, debug);
				oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Parameter not Received from "+servletPath, stepNo, oDataURL, userPass, aggregatorID, debug);
				stepNo = stepNo+1;
				//if(debug)
//				response.getWriter().println("stepNo8: "+stepNo);
//				response.getWriter().println("logID8: "+logID);
				responseFromCPI = "Unrecognized request received";
				if(debug)
					response.getWriter().println("logID: "+logID);
			}
			
			oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "All Steps Completed", stepNo, oDataURL, userPass, aggregatorID, debug);
			response.getWriter().println(responseFromCPI);
			stepNo = 0;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			logID = oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "PushAPI", oDataURL, userPass, aggregatorID, loginID, debug);
			
			oDataLogs.insertExceptionMsg(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, oDataURL, userPass, aggregatorID, debug);
			stepNo = stepNo+1;
			//if(debug)
//			response.getWriter().println("stepNoe1: "+stepNo);
//			response.getWriter().println("logIDe1: "+logID);
			if(debug)
				response.getWriter().println("logID: "+logID);
		}
	}
	
	public String sendMessageToCPI(HttpServletRequest request, HttpServletResponse response, String payLoad, String loginID, String logID, int stepNo, boolean debug) throws IOException{
		String userName="", password="", authParam="", executeURL="", data="", aggregatorID="", pgID="";
		DestinationConfiguration cpiDestConfig = null;
		ODataLogs oDataLogs = new ODataLogs();
		CommonUtils commonUtils = new CommonUtils();
		
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		
		String returnResponse = "";
		try{
			String oDataURL="", oDataUserPass="", oDataUserName="", oDataPassword="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			oDataUserName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			oDataPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			oDataUserPass = oDataUserName+":"+oDataPassword;
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(CPI_CONNECT_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(CPI_CONNECT_DEST_NAME, options);
			Destination destConfiguration = destinationAccessor.get();
			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "PYGWHANA"));
            }
			
			pgID = servletPath.substring(1, servletPath.length());
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			executeURL = destConfiguration.get("URL").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			aggregatorID = destConfiguration.get("AggregatorID").get().toString();
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
//			debug = true;
			if(debug)
			{
				response.getWriter().println("sendMessageToCPI.executeURL from Dest: "+ executeURL);
		        response.getWriter().println("sendMessageToCPI.pgID: "+ pgID);
		        response.getWriter().println("sendMessageToCPI.proxyType: "+ proxyType);
		        response.getWriter().println("sendMessageToCPI.userName: "+ userName);
		        response.getWriter().println("sendMessageToCPI.password: "+ password);
		        response.getWriter().println("sendMessageToCPI.authParam: "+ authParam);
		        response.getWriter().println("sendMessageToCPI.basicAuth: "+ basicAuth);
		        response.getWriter().println("sendMessageToCPI.aggregatorID: "+ aggregatorID);
			}

			executeURL = executeURL+"PGPaymentStatusUpdate";
			
			if(debug)
				response.getWriter().println("PGPaymentStatusUpdate");
			
			oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Start of CPI Message PGPaymentStatusUpdate", stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
			stepNo = stepNo+1;
			if(debug)
				response.getWriter().println("Final executeURL: "+executeURL);
//			debug = false;
//			response.getWriter().println("stepNo9: "+stepNo);
//			response.getWriter().println("logID9: "+logID);
			
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			HttpPost postRequest = new HttpPost(executeURL);
			postRequest.setHeader("x-arteria-pgid", pgID);
			postRequest.setHeader("x-arteria-aggrid", aggregatorID);
			postRequest.setHeader("referer", payLoad);
			postRequest.setEntity(requestEntity);
			
			HttpResponse httpPostResponse = httpClient.execute(postRequest);
			responseEntity = httpPostResponse.getEntity();
			
			if(httpPostResponse.getEntity().getContentType() != null && httpPostResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpPostResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", contentType, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				//if(debug)
//					response.getWriter().println("stepNo10: "+stepNo);
//					response.getWriter().println("logID10: "+logID);
				if(debug)
					response.getWriter().println("contentType: "+contentType);
				
				if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					if(debug)
						response.getWriter().println("Response from CPI: "+data);
				}else{
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					if(debug)
						response.getWriter().println("Response from CPI: "+data);
				}
				
				if(data != null && data.trim().length() > 0){
					returnResponse = data;
				}else{
					returnResponse = "NA";
				}
				
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "CPI Response:"+data, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				//if(debug)
//				response.getWriter().println("stepNo11: "+stepNo);
//				response.getWriter().println("logID11: "+logID);
//				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", data, oDataURL, userPass, aggregatorID, loginID, debug);
				if(debug)
					response.getWriter().println("logID: "+logID);
			}else{
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				if(debug)
					response.getWriter().println("Response from CPI: "+data);
				
				/*oDataURL=""; userName=""; password=""; aggregatorID=""; userPass="";
				oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
				
				userPass = userName+":"+password;*/
				
				if(null != data || data.trim().length() > 0)
//				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "PUSHAPI", data, oDataURL, userPass, aggregatorID, loginID, debug);
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "CPI Response:"+data, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				else
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "No Response", stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				
				if(data != null && data.trim().length() > 0){
					returnResponse = data;
				}else{
					returnResponse = "NA";
				}
				//if(debug)
//				response.getWriter().println("stepNo12: "+stepNo);
//				response.getWriter().println("logID12: "+logID);
			}
			
			return returnResponse;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			returnResponse = e.getMessage();
			String oDataURL="", userPass="";
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
//			logID = oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "PushAPI", oDataURL, userPass, aggregatorID, loginID, debug);
//			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), oDataURL, userPass, aggregatorID, debug);
			oDataLogs.insertExceptionMsg(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, oDataURL, userPass, aggregatorID, debug);
			stepNo = stepNo+1;
			//if(debug)
//			response.getWriter().println("stepNoe2: "+stepNo);
//			response.getWriter().println("logIDe2: "+logID);
			if(debug)
				response.getWriter().println("logID: "+logID);
			
			return returnResponse;
		}
	}
	
	public String sendIngenicoMessageToCPI(HttpServletRequest request, HttpServletResponse response, String payLoad, String loginID, String logID, int stepNo, boolean debug) throws IOException{
		String userName="", password="", authParam="", executeURL="", data="", aggregatorID="", pgID="";
		ODataLogs oDataLogs = new ODataLogs();
		CommonUtils commonUtils = new CommonUtils();
		
		CloseableHttpClient httpClient = null;
		HttpEntity responseEntity = null;
		
		String returnResponse="";
				
		try{
			String oDataURL="", oDataUserPass="", oDataUserName="", oDataPassword="";
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			oDataUserName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			oDataPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			oDataUserPass = oDataUserName+":"+oDataPassword;
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(CPI_CONNECT_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(CPI_CONNECT_DEST_NAME, options);
			Destination destConfiguration = destinationAccessor.get();
			if (destConfiguration == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", "PYGWHANA"));
            }
			
			pgID = servletPath.substring(1, servletPath.length());
			
			String proxyType = destConfiguration.get("ProxyType").get().toString();
			executeURL = destConfiguration.get("URL").get().toString();
			userName = destConfiguration.get("User").get().toString();
			password = destConfiguration.get("Password").get().toString();
			authParam = userName + ":"+ password ;
			aggregatorID = destConfiguration.get("AggregatorID").get().toString();
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
//			debug = true;
			if(debug)
			{
				response.getWriter().println("sendIngenicoMessageToCPI.executeURL from Dest: "+ executeURL);
		        response.getWriter().println("sendIngenicoMessageToCPI.pgID: "+ pgID);
		        response.getWriter().println("sendIngenicoMessageToCPI.proxyType: "+ proxyType);
		        response.getWriter().println("sendIngenicoMessageToCPI"+ userName);
		        response.getWriter().println("sendIngenicoMessageToCPI.password: "+ password);
		        response.getWriter().println("sendIngenicoMessageToCPI.authParam: "+ authParam);
		        response.getWriter().println("sendIngenicoMessageToCPI.basicAuth: "+ basicAuth);
		        response.getWriter().println("sendIngenicoMessageToCPI.aggregatorID: "+ aggregatorID);
			}

			executeURL = executeURL+"PGPaymentStatusUpdate?"+payLoad;
			if(debug)
				response.getWriter().println("PGPaymentStatusUpdate");
			
			oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Start of CPI Message PGPaymentStatusUpdate", stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
			stepNo = stepNo+1;
			if(debug)
				response.getWriter().println("Final executeURL: "+executeURL);
			
//			executeURL = URLEncoder.encode(executeURL,"UTF-8");
			
			if(debug)
				response.getWriter().println("Final encoded executeURL: "+executeURL);
//			debug = false;
//			response.getWriter().println("stepNo9: "+stepNo);
//			response.getWriter().println("logID9: "+logID);
			
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			HttpGet getRequest = new HttpGet(executeURL);
			getRequest.setHeader("x-arteria-pgid", pgID);
			getRequest.setHeader("x-arteria-aggrid", aggregatorID);
			getRequest.setHeader("msg", payLoad);
//			getRequest.setc
			
			HttpResponse httpGetResponse = httpClient.execute(getRequest);
			responseEntity = httpGetResponse.getEntity();
			
			if(httpGetResponse.getEntity().getContentType() != null && httpGetResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpGetResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", contentType, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				//if(debug)
//					response.getWriter().println("stepNo10: "+stepNo);
//					response.getWriter().println("logID10: "+logID);
				if(debug)
					response.getWriter().println("contentType: "+contentType);
				
				if(contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					if(debug)
						response.getWriter().println("Response from CPI: "+data);
				}else{
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					if(debug)
						response.getWriter().println("Response from CPI: "+data);
				}
				if(data != null && data.trim().length() > 0){
					returnResponse = data;
				}else{
					returnResponse = "NA";
				}
				
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "CPI Response:"+data, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				if(debug)
					response.getWriter().println("logID: "+logID);
			}else{
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				if(debug)
					response.getWriter().println("Response from CPI: "+data);
				
				if(null != data || data.trim().length() > 0)
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "CPI Response:"+data, stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				else
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "No Response", stepNo, oDataURL, oDataUserPass, aggregatorID, debug);
				stepNo = stepNo+1;
				
				if(data != null && data.trim().length() > 0){
					returnResponse = data;
				}else{
					returnResponse = "NA";
				}
				//if(debug)
//				response.getWriter().println("stepNo12: "+stepNo);
//				response.getWriter().println("logID12: "+logID);
			}
			
			return returnResponse;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			returnResponse = e.getMessage();
			
			String oDataURL="", userPass="";
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
//			logID = oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "PushAPI", oDataURL, userPass, aggregatorID, loginID, debug);
//			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), oDataURL, userPass, aggregatorID, debug);
			oDataLogs.insertExceptionMsg(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, oDataURL, userPass, aggregatorID, debug);
			stepNo = stepNo+1;
			//if(debug)
//			response.getWriter().println("stepNoe2: "+stepNo);
//			response.getWriter().println("logIDe2: "+logID);
			if(debug)
				response.getWriter().println("logID: "+logID);
			
			return returnResponse;
		}
	}
	
	public Destination getCPIDestination(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//		LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = CPI_CONNECT_DEST_NAME;
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
					.tryGetDestination(CPI_CONNECT_DEST_NAME, options);
			destConfiguration = destinationAccessor.get();
//			LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", CPI_CONNECT_DEST_NAME));
//			LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

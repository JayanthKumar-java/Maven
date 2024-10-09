package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;

/**
 * Servlet implementation class ESignCALContractUpdate
 */
@WebServlet("/ESignCALContractUpdate")
public class ESignCALContractUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CNFG_TYPESET =  "PY";
	private static final String CNFG_DLRONB_TYPES =  "DLRONB";
	private static final String CNFG_VENONB_TYPES =  "VENONB";
	private static final String CNFG_DLRRNW_TYPES =  "DLRRNW";
	private static final String CNFG_VENRNW_TYPES =  "VENRNW";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ESignCALContractUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String servletPath="", logID="", aggregatorID="", cpiDestUrl = "", cpiUser = "", cpiPass = "", 
				cpiUserPass = "", cpiResponse = "", cpiEndPoint="", bodyPayload = "";
		boolean debug=false, proceed=false;
		ODataLogs oDataLogs = new ODataLogs();
		int stepNo=0;
		JsonObject configTypeSetObj = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		Destination cpiDestConfig = null;
		try{
			if(request.getParameter("debug")!= null && request.getParameter("debug").toString().trim().equalsIgnoreCase("true")){
				debug = true;
			}
			
			StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
		    String queryString = request.getQueryString();
		    
		    bodyPayload = commonUtils.getGetBody(request, response);
		    if(debug){
		    	response.getWriter().println("requestURL: "+requestURL);
		    	response.getWriter().println("queryString: "+queryString);
		    	response.getWriter().println("bodyPayload: "+bodyPayload);
		    }
			
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			servletPath = request.getServletPath();
			if(debug){
				response.getWriter().println("servletPath: "+servletPath);
			}
			
			cpiDestConfig = commonUtils.getCPIDestination(request, response);
			aggregatorID = cpiDestConfig.get("AggregatorID").get().toString();
			
			if(debug)
				response.getWriter().println("servletPath.aggregatorID: "+aggregatorID);
			
			if(servletPath.equalsIgnoreCase("/DealerOnboarding")){
				cpiEndPoint="";
				configTypeSetObj = commonUtils.getConfigTypeSetsOnTypesetAndTypes(request, response,CNFG_TYPESET, CNFG_DLRONB_TYPES, aggregatorID, debug);
				if(debug){
					response.getWriter().println("configTypeSetObj: "+configTypeSetObj);
				}
				
				if(configTypeSetObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					proceed = true;
					cpiEndPoint = configTypeSetObj.get("TypeValue").getAsString().trim();
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(configTypeSetObj);
				}
			}else if(servletPath.equalsIgnoreCase("/VendorOnboarding")){
				cpiEndPoint="";
				configTypeSetObj = commonUtils.getConfigTypeSetsOnTypesetAndTypes(request, response,CNFG_TYPESET, CNFG_VENONB_TYPES, aggregatorID, debug);
				if(debug){
					response.getWriter().println("configTypeSetObj: "+configTypeSetObj);
				}
				
				if(configTypeSetObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					proceed = true;
					cpiEndPoint = configTypeSetObj.get("TypeValue").getAsString().trim();
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(configTypeSetObj);
				}
			}else if(servletPath.equalsIgnoreCase("/DealerRenewal")){
				cpiEndPoint="";
				configTypeSetObj = commonUtils.getConfigTypeSetsOnTypesetAndTypes(request, response,CNFG_TYPESET, CNFG_DLRRNW_TYPES, aggregatorID, debug);
				if(debug){
					response.getWriter().println("configTypeSetObj: "+configTypeSetObj);
				}
				
				if(configTypeSetObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					proceed = true;
					cpiEndPoint = configTypeSetObj.get("TypeValue").getAsString().trim();
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(configTypeSetObj);
				}
			}else if(servletPath.equalsIgnoreCase("/VendorRenewal")){
				cpiEndPoint="";
				configTypeSetObj = commonUtils.getConfigTypeSetsOnTypesetAndTypes(request, response,CNFG_TYPESET, CNFG_VENRNW_TYPES, aggregatorID, debug);
				if(debug){
					response.getWriter().println("configTypeSetObj: "+configTypeSetObj);
				}
				
				if(configTypeSetObj.get("Status").getAsString().equalsIgnoreCase("000001")){
					proceed = true;
					cpiEndPoint = configTypeSetObj.get("TypeValue").getAsString().trim();
				}else{
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(configTypeSetObj);
				}
			}else{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Unknown request");
			}
			
			cpiDestUrl = cpiDestConfig.get("URL").get().toString();
			if(debug){
				response.getWriter().println("cpiDestUrl before updating: "+cpiDestUrl);
			}
			cpiUser = cpiDestConfig.get("User").get().toString();;
			cpiPass = cpiDestConfig.get("Password").get().toString();;
			cpiUserPass = cpiUser + ":" + cpiPass;
			cpiDestUrl = cpiDestUrl + cpiEndPoint+"?"+queryString;
			
			if(debug){
				response.getWriter().println("Final cpiDestUrl: "+cpiDestUrl);
				response.getWriter().println("cpiEndPoint: "+cpiEndPoint);
				response.getWriter().println("cpiUser: "+cpiUser);
				response.getWriter().println("cpiPass: "+cpiPass);
				response.getWriter().println("cpiUserPass: "+cpiUserPass);
			}
			
			URL url = new URL(cpiDestUrl);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			byte[] postDataBytes = bodyPayload.toString().getBytes("UTF-8");
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("charset", "utf-8");
			urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(cpiUserPass.getBytes());
			urlConnection.setRequestProperty("Authorization", basicAuth);
			urlConnection.connect();
			
			OutputStream os = urlConnection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(bodyPayload.toString());
			osw.flush();
			osw.close();
			
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			
			if (debug)
				response.getWriter().println("sb: " + sb.toString());
			
			cpiResponse = sb.toString();
			
			response.getWriter().println("cpiResponse: "+cpiResponse);
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println("Exception in ESignCALContractUpdate - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			/*logID = oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "PushAPI", oDataURL, userPass, aggregatorID, loginID, debug);
			
			oDataLogs.insertExceptionMsg(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, oDataURL, userPass, aggregatorID, debug);
			stepNo = stepNo+1;
			if(debug)
				response.getWriter().println("logID: "+logID);*/
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

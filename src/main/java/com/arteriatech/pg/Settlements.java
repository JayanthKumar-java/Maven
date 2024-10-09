package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ccavenue.security.AesCryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import org.apache.http.client.HttpClient;

import io.vavr.control.Try;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;

/**
 * Servlet implementation class Settlements
 */
@WebServlet("/Settlements")
public class Settlements extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReqPymtGateway.class);
	private static final String PUGW_DEST_NAME =  "pugw_utils_op";
	private static final String PCGW_UTIL_DEST_NAME =  "pcgw_utils_op"; 
	private static final String NOAUTH_DEST_NAME =  "pcgw_utils_noauth";
	
	String servletPath="";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Settlements() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		servletPath = request.getServletPath();
		String pgID="", configurationValues="";
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(null != request.getParameter("PGID"))
				pgID = request.getParameter("PGID");
			
			if(! pgID.equalsIgnoreCase(properties.getProperty("EazyPayPGID")))
			{
				configurationValues = getConstantValues(request, response, "", pgID);
			}

			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("configurationValues: "+configurationValues);
			}
			
			if(null != pgID && pgID.trim().length() > 0){
				if(pgID.equalsIgnoreCase(properties.getProperty("CCAvenuePGID")) 
				|| pgID.equalsIgnoreCase(properties.getProperty("MobileCCPGID"))
				|| pgID.equalsIgnoreCase(properties.getProperty("CCAICICI"))
				|| pgID.equalsIgnoreCase(properties.getProperty("CCAPGID"))){
					if(servletPath.equalsIgnoreCase("/PayoutSummary")){
						getCCAvenuePayoutSummary(request, response, configurationValues);
					}else if(servletPath.equalsIgnoreCase("/Payouts")){
//							response.getWriter().println("Under Construction");
						getCCAvenuePayoutDetails(request, response, configurationValues);
					}else if(servletPath.equalsIgnoreCase("/SettlementAccount")){
//						response.getWriter().println("Under Construction");
						getCCASettlementAccntDetails(request, response, configurationValues);
					}
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "J001");
					result.addProperty("ErrorMsg", "Wrong PGID passed in request");
					result.addProperty("Status", "000002");
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("ErrorMsg", "Mandatory Parameters are missing: PGID");
				result.addProperty("Status", "000002");
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("Exception Trace: "+buffer.toString());
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J002");
			result.addProperty("ErrorMsg", "Exception: Cause: "+e.getCause()+" | Class: "+e.getClass()+" | Message: "+e.getMessage());
			result.addProperty("Status", "000002");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void getCCASettlementAccntDetails(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException{
		String subAccID="", currency="", responseFromCCAvenue="", trackID="", encRequest="", merchantCode="", schemeCode="", txnUrl="", txnStatusUrl="", workingKey="", statusURL="", accessCode="", schemeMode="";
		boolean isMandtMissing = false;
		
		try{
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SchemeCode")){
	        		schemeCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WorkingKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Mode")){
	        		schemeMode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStatusUrl = paramValue;
	        	}
			}
			
			if(request.getParameter("subAccountID") != null && request.getParameter("subAccountID").trim().length() > 0)
				subAccID = request.getParameter("subAccountID");
			else
				isMandtMissing = true;
			
			if(request.getParameter("currency") != null && request.getParameter("currency").trim().length() > 0)
				currency = request.getParameter("currency");
			else
				isMandtMissing = true;
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("subAccID: "+subAccID);
				response.getWriter().println("currency: "+currency);
			}
			
			String statusCheckRequest="";
			if(! isMandtMissing){
				String formattedStr="";
				try{
					int number = Integer.parseInt(subAccID);
					formattedStr = ("0000000000" + subAccID).substring(subAccID.length());
					subAccID = formattedStr;
				}catch (NumberFormatException e) {
//						formattedStr = customerNo;
				}
				
				
				JsonObject plainReqJson = new JsonObject();
				plainReqJson.addProperty("sub_acc_id", subAccID);
				plainReqJson.addProperty("account_currency", currency);
				
				AesCryptUtil cryptoUtil = new AesCryptUtil(workingKey);
				encRequest = cryptoUtil.encrypt(plainReqJson+"");
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("plainReqJson: "+plainReqJson);
					response.getWriter().println("encRequest: "+encRequest);
				}
				
				statusCheckRequest = "enc_request="+encRequest+"&access_code="+accessCode+"&request_type=JSON&response_type=JSON&command=getStlmAccDetails&account_currency="+currency+"&sub_acc_id="+subAccID+"&version=1.1";
				
				URL url = new URL(txnStatusUrl+"?"+statusCheckRequest);
				
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.setRequestMethod("POST");
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, new java.security.SecureRandom()); 
				con.setSSLSocketFactory(sc.getSocketFactory());
				
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
				while ((input = br.readLine()) != null){
					responseFromCCAvenue = input;
				}
				br.close();
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("responseFromCCAvenue: "+responseFromCCAvenue);
				
				String decryptedResponse="";
				
				String resParamName="", resParamValue="";
				String status="", encryptedResponse="";
				
				String[] splitResponseResult = responseFromCCAvenue.split("&");
				for(String s : splitResponseResult)
				{
					resParamName = s.substring(0, s.indexOf("="));
					resParamValue = s.substring(s.indexOf("=")+1,s.length());
		        	
		        	if(resParamName.equalsIgnoreCase("status"))
		        		status = resParamValue;
		        	
		        	if(resParamName.equalsIgnoreCase("enc_response"))
		        		encryptedResponse = resParamValue;
				}
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("status: "+status);
					response.getWriter().println("encryptedResponse: "+encryptedResponse);
				}
				
				String statusTxt="", responseCode="";
				
				if(status.equalsIgnoreCase("0")){
					AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
					decryptedResponse = aesUtil.decrypt(encryptedResponse);
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("decryptedResponse: "+decryptedResponse);
					
					response.getWriter().println(decryptedResponse);
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "B001");
					result.addProperty("ErrorMsg", responseFromCCAvenue);
					result.addProperty("Status", "000002");
					
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("ErrorMsg", "Mandatory Parameters are missing");
				result.addProperty("Status", "000002");
				
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("Exception Trace: "+buffer.toString());
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J002");
			result.addProperty("ErrorMsg", "Exception: Cause: "+e.getCause()+" | Class: "+e.getClass()+" | Message: "+e.getMessage());
			result.addProperty("Status", "000002");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void getCCAvenuePayoutDetails(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException{
		String plainRequest="", responseFromCCAvenue="", trackID="", encRequest="", merchantCode="", schemeCode="", txnUrl="", txnStatusUrl="", workingKey="", statusURL="", accessCode="", schemeMode="";
		boolean isMandtMissing = false;
		
		try{
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SchemeCode")){
	        		schemeCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WorkingKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Mode")){
	        		schemeMode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStatusUrl = paramValue;
	        	}
			}
			
			if(request.getParameter("payid") != null && request.getParameter("payid").trim().length() > 0)
				plainRequest = request.getParameter("payid");
			else
				isMandtMissing = true;
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("plainRequest: "+plainRequest);
			}
			
			String statusCheckRequest="";
			
			if(! isMandtMissing){
				JsonObject plainReqJson = new JsonObject();
				plainReqJson.addProperty("pay_id", plainRequest);
				
				AesCryptUtil cryptoUtil = new AesCryptUtil(workingKey);
				encRequest = cryptoUtil.encrypt(plainReqJson+"");
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("encRequest: "+encRequest);
					response.getWriter().println("plainReqJson: "+plainReqJson);
				}
				
				statusCheckRequest = "enc_request="+encRequest+"&access_code="+accessCode+"&request_type=JSON&response_type=JSON&command=payIdDetails&pay_id="+plainRequest+"&version=1.2";
				
				URL url = new URL(txnStatusUrl+"?"+statusCheckRequest);
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
//				URLConnection con = url.openConnection();
				con.setRequestMethod("POST");
				/*con.addRequestProperty("enc_request", encRequest);
				con.addRequestProperty("access_code", accessCode);
				con.addRequestProperty("request_type", "JSON");
				con.addRequestProperty("response_type", "JSON");
				con.addRequestProperty("command", "payIdDetails");
				con.addRequestProperty("pay_id", plainRequest);
				con.addRequestProperty("version", "1.2");*/
				
//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
//					response.getWriter().println("con: "+con.);
//				}
//				con.
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, new java.security.SecureRandom()); 
				con.setSSLSocketFactory(sc.getSocketFactory());
				
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
				while ((input = br.readLine()) != null){
					responseFromCCAvenue = input;
				}
				br.close();
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("responseFromCCAvenue: "+responseFromCCAvenue);
				
				String decryptedResponse="";
				
				String resParamName="", resParamValue="";
				String status="", encryptedResponse="";
				
				String[] splitResponseResult = responseFromCCAvenue.split("&");
				for(String s : splitResponseResult)
				{
					resParamName = s.substring(0, s.indexOf("="));
					resParamValue = s.substring(s.indexOf("=")+1,s.length());
		        	
		        	if(resParamName.equalsIgnoreCase("status"))
		        		status = resParamValue;
		        	
		        	if(resParamName.equalsIgnoreCase("enc_response"))
		        		encryptedResponse = resParamValue;
				}
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("status: "+status);
					response.getWriter().println("encryptedResponse: "+encryptedResponse);
				}
				
				String statusTxt="", responseCode="";
				
				if(status.equalsIgnoreCase("0")){
					AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
					decryptedResponse = aesUtil.decrypt(encryptedResponse);
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("decryptedResponse: "+decryptedResponse);
					
					response.getWriter().println(decryptedResponse);
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "B001");
					result.addProperty("ErrorMsg", responseFromCCAvenue);
					result.addProperty("Status", "000002");
					
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("ErrorMsg", "Mandatory Parameters are missing: payid");
				result.addProperty("Status", "000002");
				
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("Exception Trace: "+buffer.toString());
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J002");
			result.addProperty("ErrorMsg", "Exception: Cause: "+e.getCause()+" | Class: "+e.getClass()+" | Message: "+e.getMessage());
			result.addProperty("Status", "000002");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private void getCCAvenuePayoutSummary(HttpServletRequest request, HttpServletResponse response, String configurationValues) throws IOException{
		String plainRequest="", responseFromCCAvenue="", trackID="", encRequest="", merchantCode="", schemeCode="", txnUrl="", txnStatusUrl="", workingKey="", statusURL="", accessCode="", schemeMode="";
		boolean isMandtMissing = false;
		try{
			String wholeParamString="", paramName="", paramValue="", pgID="";
			wholeParamString = configurationValues;
			
			String[] splitResult = wholeParamString.split("\\|");
			for(String s : splitResult)
			{
	        	paramName = s.substring(0, s.indexOf("="));
	        	paramValue = s.substring(s.indexOf("=")+1,s.length());
	        	
	        	if(paramName.equalsIgnoreCase("MerchantCode")){
	        		merchantCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("SchemeCode")){
	        		schemeCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("PGID")){
	        		pgID = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WSURL")){
	        		txnUrl = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("WorkingKey")){
	        		workingKey = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("AccessCode")){
	        		accessCode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("Mode")){
	        		schemeMode = paramValue;
	        	}
	        	
	        	if(paramName.equalsIgnoreCase("TxnStsURL")){
	        		txnStatusUrl = paramValue;
	        	}
			}
			
			if(request.getParameter("settlementdate") != null && request.getParameter("settlementdate").trim().length() > 0)
				plainRequest = request.getParameter("settlementdate");
			else
				isMandtMissing = true;
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("plainRequest: "+plainRequest);
			}
			
			String statusCheckRequest="";
			
			if(! isMandtMissing){
				JsonObject plainReqJson = new JsonObject();
				plainReqJson.addProperty("settlement_date", plainRequest);
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("plainReqJson: "+plainReqJson);
					response.getWriter().println("plainReqJson.getAsJsonObject: "+plainReqJson.getAsJsonObject());
				}
				
				AesCryptUtil cryptoUtil = new AesCryptUtil(workingKey);
				encRequest = cryptoUtil.encrypt(plainReqJson+"");
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("encRequest: "+encRequest);
				}
				
				statusCheckRequest = "enc_request="+encRequest+"&access_code="+accessCode+"&request_type=JSON&response_type=JSON&command=payoutSummary&settlement_date="+plainRequest+"&version=1.2";
				
				URL url = new URL(txnStatusUrl+"?"+statusCheckRequest);
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
//				URLConnection con = url.openConnection();
				con.setRequestMethod("POST");
				/*con.addRequestProperty("enc_request", encRequest);
				con.addRequestProperty("access_code", accessCode);
				con.addRequestProperty("request_type", "JSON");
				con.addRequestProperty("response_type", "JSON");
				con.addRequestProperty("command", "payoutSummary");
				con.addRequestProperty("settlement_date", plainRequest);
				con.addRequestProperty("version", "1.2");*/
				
//				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
//					response.getWriter().println("con: "+con.);
//				}
//				con.
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, new java.security.SecureRandom()); 
				con.setSSLSocketFactory(sc.getSocketFactory());
				
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				String errorMsgInput = "", errorParam="", errorValue="", errorCode="", errorMsg="";
				while ((input = br.readLine()) != null){
					responseFromCCAvenue = input;
				}
				br.close();
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
					response.getWriter().println("responseFromCCAvenue: "+responseFromCCAvenue);
				
				String decryptedResponse="";
				
				String resParamName="", resParamValue="";
				String status="", encryptedResponse="";
				
				String[] splitResponseResult = responseFromCCAvenue.split("&");
				for(String s : splitResponseResult)
				{
					resParamName = s.substring(0, s.indexOf("="));
					resParamValue = s.substring(s.indexOf("=")+1,s.length());
		        	
		        	if(resParamName.equalsIgnoreCase("status"))
		        		status = resParamValue;
		        	
		        	if(resParamName.equalsIgnoreCase("enc_response"))
		        		encryptedResponse = resParamValue;
				}
				
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
					response.getWriter().println("status: "+status);
					response.getWriter().println("encryptedResponse: "+encryptedResponse);
				}
				
				String statusTxt="", responseCode="";
				
				if(status.equalsIgnoreCase("0")){
					AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
					decryptedResponse = aesUtil.decrypt(encryptedResponse);
					
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("decryptedResponse: "+decryptedResponse);
					
					response.getWriter().println(decryptedResponse);
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "B001");
					result.addProperty("ErrorMsg", responseFromCCAvenue);
					result.addProperty("Status", "000002");
					
					response.getWriter().print(new Gson().toJson(result));
				}
			}else{
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("ErrorMsg", "Mandatory Parameters are missing: Settlement Date");
				result.addProperty("Status", "000002");
				
				response.getWriter().print(new Gson().toJson(result));
			}
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")){
				response.getWriter().println("Exception Trace: "+buffer.toString());
			}
			
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J002");
			result.addProperty("ErrorMsg", "Exception: Cause: "+e.getCause()+" | Class: "+e.getClass()+" | Message: "+e.getMessage());
			result.addProperty("Status", "000002");
			response.getWriter().print(new Gson().toJson(result));
		}
	}
	
	private String getConstantValues(HttpServletRequest request, HttpServletResponse response, String loginSessionID, String PGID) throws IOException, URISyntaxException
	{
		String configurableValues="", basicAuth="", authMethod="", destURL="", userName="", password="", authParam="", constantValuesService="", constantValuesFilter="";
		byte[] encodedByte = null;
		CommonUtils commonUtils = new CommonUtils();
		AuthenticationHeader principalPropagationHeader = null;
		boolean debug = false;
		
		HttpGet configValuesGet = null;
		HttpEntity configValuesEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		
//		debug = true;
		try
		{
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				debug = true;
			
			// Context tenCtx = new InitialContext();
			//TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			/* if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
			
//			Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
//			DestinationConfiguration destConfiguration = configuration.getConfiguration(PCGW_UTIL_DEST_NAME);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(PCGW_UTIL_DEST_NAME, options);
			HttpDestination destConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(destConfiguration);
			Destination destinationConf = destinationAccessor.get();
			if (destinationConf == null) {
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
			
			String sapclient = destinationConf.get("sap-client").get().toString();
			destURL=destinationConf.get("URL").get().toString();
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
		    } */
		    
		    /* HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
	        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	        
	        closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */

	        configValuesGet = new HttpGet(constantValuesService);
	        //configValuesGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        configValuesGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        configValuesGet.setHeader("Accept", "application/atom+xml");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	configValuesGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }/* else{
	        	configValuesGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        } */

			HttpResponse httpResponse = client.execute(configValuesGet);
			configValuesEntity = httpResponse.getEntity();
	        
	        //HttpResponse httpResponse = closableHttpClient.execute(configValuesGet);
	        
	        if(debug){
		        int statusCode = httpResponse.getStatusLine().getStatusCode();
				response.getWriter().println("pgPaymentConfigs.statusCode: "+statusCode);
	        }
			
			configValuesEntity = httpResponse.getEntity();

			if(configValuesEntity != null)
			{
				configurableValues = "";
				if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				{
					response.getWriter().println("constantEntity is not null");
					response.getWriter().println("PGID: "+PGID);
				}
				
				if(PGID.equalsIgnoreCase("B2BIZ"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
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
		            
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//			            	response.getWriter().println("nodeList Customer: "+nodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()+
		            				"|"+"PGID="+pdIDList.item(i).getTextContent()+
		            				"|"+"WSURL="+aWSURLList.item(i).getTextContent()
		            				+"|"+"ClientCode="+clientCodeList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
				else if(PGID.equalsIgnoreCase("AXISPG"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
		            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
		            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
		            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
		            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
		            NodeList secretCode = document.getElementsByTagName("d:ClientCode");
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
		            								+"|"+"Type="+pgParameter2.item(i).getTextContent()
		            								+"|"+"RE1="+pgParameter3.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgParameter4.item(i).getTextContent()
		            								+"|"+"secretCode="+secretCode.item(i).getTextContent()
		            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
				else if(PGID.equalsIgnoreCase("YESPAYU"))
				{
			        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
		            Document document = docBuilder.parse(inputSource);
		            NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
		            NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
		            NodeList pdIDList = document.getElementsByTagName("d:PGID");
		            NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
		            NodeList pgParameter1 = document.getElementsByTagName("d:PGParameter1");
		            NodeList pgParameter2 = document.getElementsByTagName("d:PGParameter2");
		            NodeList pgParameter3 = document.getElementsByTagName("d:PGParameter3");
		            NodeList pgParameter4 = document.getElementsByTagName("d:PGParameter4");
		            NodeList pgParameter5 = document.getElementsByTagName("d:PGParameter5");
		            NodeList pgOwnPublicKey = document.getElementsByTagName("d:PGOwnPublickey");
		            NodeList pgOwnPrivateKey = document.getElementsByTagName("d:PGOwnPrivatekey");
		            NodeList bankKey = document.getElementsByTagName("d:BankKey");
		            NodeList clientCode = document.getElementsByTagName("d:ClientCode");
		            for(int i=0 ; i<pgCategoryList.getLength() ; i++)
		            {
//				            	response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
	        			{
		            		configurableValues = configurableValues+"MerchantCode="+merchantCodeList.item(i).getTextContent()
		            								+"|"+"PGID="+pdIDList.item(i).getTextContent()
		            								+"|"+"Version="+pgParameter1.item(i).getTextContent()
		            								+"|"+"PassCode="+pgParameter2.item(i).getTextContent()
		            								+"|"+"MCC="+pgParameter3.item(i).getTextContent()
		            								+"|"+"EncryptionKey="+pgOwnPublicKey.item(i).getTextContent()
		            								+"|"+"SecureSecret="+pgOwnPrivateKey.item(i).getTextContent()
		            								+"|"+"BankId="+bankKey.item(i).getTextContent()
		            								+"|"+"TerminalId="+clientCode.item(i).getTextContent()
		            								+"|"+"Currency="+pgParameter4.item(i).getTextContent()
		            								+"|"+"TxnType="+pgParameter5.item(i).getTextContent()
		            								+"|"+"WSURL="+aWSURLList.item(i).getTextContent();
		            		break;
	        			}
		            }
				}
				else if(PGID.equalsIgnoreCase("EAZYPAY"))
				{
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder docBuilder;
			        InputSource inputSource;
			        
					String retSrc = EntityUtils.toString(configValuesEntity); 
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
		            	if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
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
				else if(PGID.equalsIgnoreCase("RZRPYROUTE"))
				{
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
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
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
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
				else if(PGID.equalsIgnoreCase("RAZORPAY"))
				{
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
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
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
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
				}else if(PGID.equalsIgnoreCase("CCAVENUE") || PGID.equalsIgnoreCase("MOBILECCA")){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
									          
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
										          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
										            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
										            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}else if(PGID.equalsIgnoreCase("CCAICICI") || PGID.equalsIgnoreCase("CCA")){
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					InputSource inputSource;
					
					String retSrc = EntityUtils.toString(configValuesEntity); 
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
						response.getWriter().println("retSrc: "+retSrc);
					
					docBuilder = docBuilderFactory.newDocumentBuilder();
					inputSource = new InputSource(new StringReader(retSrc));
					Document document = docBuilder.parse(inputSource);
					
					NodeList pdIDList = document.getElementsByTagName("d:PGID");
					NodeList pgCategoryList = document.getElementsByTagName("d:PGCategoryID");
					NodeList merchantCodeList = document.getElementsByTagName("d:MerchantCode");
					NodeList accessCodeList = document.getElementsByTagName("d:PGOwnPublickey");
					NodeList workingKeyList = document.getElementsByTagName("d:PGOwnPrivatekey");
					NodeList aWSURLList = document.getElementsByTagName("d:PGURL");
					NodeList providerList = document.getElementsByTagName("d:PGProvider");
					NodeList txnURLList = document.getElementsByTagName("d:TxnStsURL");
					NodeList schemeCodeList = document.getElementsByTagName("d:SchemeCode");
					NodeList pymntModeList = document.getElementsByTagName("d:PGParameter1");
					
					for(int i=0 ; i<pgCategoryList.getLength() ; i++)
					{
//						response.getWriter().println("nodeList merchantCodeList: "+merchantCodeList.item(i).getTextContent());
						if(PGID.equalsIgnoreCase(pdIDList.item(i).getTextContent()))
						{
							configurableValues = configurableValues	+"MerchantCode="+merchantCodeList.item(i).getTextContent()
										          	+"|"+"PGID="+pdIDList.item(i).getTextContent()
										            +"|"+"WSURL="+aWSURLList.item(i).getTextContent()
										            +"|"+"PGCategoryID="+pgCategoryList.item(i).getTextContent()
										            +"|"+"AccessCode="+accessCodeList.item(i).getTextContent()
										            +"|"+"WorkingKey="+workingKeyList.item(i).getTextContent()
													+"|"+"PGProvider="+providerList.item(i).getTextContent()
													+"|"+"SchemeCode="+schemeCodeList.item(i).getTextContent()
													+"|"+"Mode="+pymntModeList.item(i).getTextContent()
													+"|"+"TxnStsURL="+txnURLList.item(i).getTextContent();
						
							break;
						}
					}
				}
			}
			if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
				response.getWriter().println("1 getConstantValues: "+configurableValues);
		}
		catch (Exception e)
		{
			if(debug)
				response.getWriter().println("Exception: "+e.getMessage());
		}
		finally
		{
			//closableHttpClient.close();
			return configurableValues;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

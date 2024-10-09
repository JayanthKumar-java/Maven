package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
/**
 * Servlet implementation class ESignContractReminder
 */
@WebServlet("/ESignContractReminder")
public class ESignContractReminder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ESignContractReminder() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		boolean debug = false, isRequestFromCloud = false, mandatoryCheckPass = true;
		String destURL="", eContractID="",eCustomerID="", userRegId="", aggregatorID="", loginID="", appLogID="";
		String oDataURL="", userName="", password="", userPass="";
		String payloadRequest="", cpTypeID="", cpGuid="", renewalFlag="", mode="", mandatoryCheckMsg="Mandatory inputs missing: ";
		int mandatoryCheckMsgLen = mandatoryCheckMsg.length();
		
		ODataLogs oDataLogs = new ODataLogs();
		CommonUtils commonUtils = new CommonUtils();
		Destination cpiDestConfig = null;
		Destination destConfiguration = null;
		try{
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
				debug=true;
			
			//debug=true; // delete this line after Testing completed
			payloadRequest = getGetBody(request, response);
			if(debug)
				response.getWriter().println("payloadRequest: "+payloadRequest);
			
			if(null != payloadRequest && payloadRequest.trim().length()> 0){
				JsonParser parser = new JsonParser();
				try{
					JsonObject inputPayload = (JsonObject)parser.parse(payloadRequest);
					if(debug)
						response.getWriter().println("Proper Json input: "+inputPayload);
					
					try {
						if (inputPayload.has("debug") && null != inputPayload.get("debug").getAsString() && 
								inputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
							debug = true;
							response.getWriter().println("doGet.inputPayload: "+inputPayload); 
						}
					} catch (Exception e) {
						debug = false;
					}
					
					if(inputPayload.has("CPGUID") && inputPayload.get("CPGUID").getAsString().trim().length() > 0){
						cpGuid = inputPayload.get("CPGUID").getAsString();
					}else{
						//Mandatory check fails
						mandatoryCheckPass = false;
						mandatoryCheckMsg = mandatoryCheckMsg+"CPGUID";
						
						mandatoryCheckMsgLen = mandatoryCheckMsg.length();
					}
					
					if(inputPayload.has("CPTypeID") && inputPayload.get("CPTypeID").getAsString().trim().length() > 0){
						cpTypeID = inputPayload.get("CPTypeID").getAsString();
					}else{
						//Mandatory check fails
						if(debug)
							response.getWriter().println("mandatoryCheckMsgLen: "+mandatoryCheckMsgLen);
						
						mandatoryCheckPass = false;
						if(mandatoryCheckMsgLen > 26)
							mandatoryCheckMsg = mandatoryCheckMsg+", CPTypeID";
						else
							mandatoryCheckMsg = mandatoryCheckMsg+"CPTypeID";
						
						mandatoryCheckMsgLen = mandatoryCheckMsg.length();
					}
					
					//Mode: OC or OP
					if(inputPayload.has("Renewal") && inputPayload.get("Renewal").getAsString().trim().length() > 0){
						renewalFlag = inputPayload.get("Renewal").getAsString();
					}else{
						renewalFlag = "";
						//Mandatory check fails
						/*if(debug)
							response.getWriter().println("mandatoryCheckMsgLen: "+mandatoryCheckMsgLen);
						
						mandatoryCheckPass = false;
						if(mandatoryCheckMsgLen > 26)
							mandatoryCheckMsg = mandatoryCheckMsg+", CPTypeID";
						else
							mandatoryCheckMsg = mandatoryCheckMsg+"CPTypeID";
						
						mandatoryCheckMsgLen = mandatoryCheckMsg.length();*/
					}
					
					if(debug){
						response.getWriter().println("CpGuid: "+cpGuid);
						response.getWriter().println("cpTypeID: "+cpTypeID);
						response.getWriter().println("renewalFlag: "+renewalFlag);
					}
					
					String aggregatorFromDest="";
					if(mandatoryCheckPass){
						Properties properties = new Properties();
						properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
						
						loginID = commonUtils.getUserPrincipal(request, "name", response);
						
						if(debug)
							response.getWriter().println("loginID: "+loginID);
						
						cpiDestConfig = commonUtils.getCPIDestination(request, response);
						destConfiguration = commonUtils.getDestinationURL(request, response);
						destURL = destConfiguration.get("URL").get().toString();
						
						aggregatorFromDest = cpiDestConfig.get("AggregatorID").get().toString();
						if(debug)
							response.getWriter().println("doGet.destURL.: "+ destURL);
					
						if (destURL.contains("service.xsodata")){
							isRequestFromCloud = true;
						}else{
							isRequestFromCloud = false;
						}
						
						String scfService = "", sessionID="";
						JsonObject finalPayload = new JsonObject();
						JsonObject scfObj = new JsonObject();
						
						boolean scfEmpty = false, scfError = false;
						
						if(renewalFlag.equalsIgnoreCase("X")){
							destURL="";
							destURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
							String formattedStr = "";
							try{
								int number = Integer.parseInt(cpGuid);
								formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
								cpGuid = formattedStr;
							}catch (NumberFormatException e) {
//									formattedStr = customerNo;
							}
							
							String aggregatorIDFromDest="";
							aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
							
							JsonObject scfChildObj = new JsonObject();
							
							scfService = destURL+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27%20and%20CPGUID%20eq%20%27"+cpGuid+"%27%20and%20CPTypeID%20eq%20%27"+cpTypeID+"%27%20and%20StatusID%20eq%20%27000001%27";
							if (debug)
								response.getWriter().println("renewal.executeURL: "+scfService);
							
							scfObj = getSCFFromCloud(response, request, scfService, debug);
							
							if (debug) {
								response.getWriter().println("scfObj: "+scfObj);  // exception occured here
							}
							if(scfObj.has("error")){
								scfError = true;
								finalPayload = null;
								
								oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
								userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
								password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
								aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
								
								userPass = userName+":"+password;
								
								appLogID = oDataLogs.insertExceptionLogs(request, response, scfObj.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorIDFromDest, loginID, debug);
							}else if(scfObj.getAsJsonObject("d").getAsJsonArray("results").size() == 0){
								scfEmpty = true;
								finalPayload = null;
							}else if(! scfObj.isJsonNull()){
								for (int i = 0; i < scfObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
								{
									scfChildObj = scfObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
									
									if( ! scfChildObj.get("EContractID").isJsonNull() )
										eContractID =  scfChildObj.get("EContractID").getAsString();
									
									if( ! scfChildObj.get("ECustomerID").isJsonNull() )
										eCustomerID =  scfChildObj.get("ECustomerID").getAsString();
									
								}
								if (debug) {
									response.getWriter().println("eCustomerID: "+eCustomerID);
									response.getWriter().println("eContractID: "+eContractID);
								}
								
								//Final payload
								finalPayload.addProperty("CustomerId", eCustomerID);
								finalPayload.addProperty("ContractId", eContractID);
								finalPayload.addProperty("AggregatorID", aggregatorFromDest);
							}
						}else{
							if(isRequestFromCloud){
								String formattedStr = "";
								try{
									int number = Integer.parseInt(cpGuid);
									formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
									cpGuid = formattedStr;
								}catch (NumberFormatException e) {
	//									formattedStr = customerNo;
								}
								
								String aggregatorIDFromDest="";
								aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
								
								JsonObject scfChildObj = new JsonObject();
								
								scfService = destURL+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27%20and%20CPGUID%20eq%20%27"+cpGuid+"%27%20and%20CPTypeID%20eq%20%27"+cpTypeID+"%27";
								if (debug)
									response.getWriter().println("cloudscf.executeURL: "+scfService);
								
								scfObj = getSCFFromCloud(response, request, scfService, debug);
								
								if (debug) {
									response.getWriter().println("scfObj: "+scfObj);
								}
								if(scfObj.has("error")){
									scfError = true;
									finalPayload = null;
									
									oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
									userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
									password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
									aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
									
									userPass = userName+":"+password;
									
									appLogID = oDataLogs.insertExceptionLogs(request, response, scfObj.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorIDFromDest, loginID, debug);
								}else if(scfObj.getAsJsonObject("d").getAsJsonArray("results").size() == 0){
									scfEmpty = true;
									finalPayload = null;
								}else if(! scfObj.isJsonNull()){
									for (int i = 0; i < scfObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
									{
										scfChildObj = scfObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
										
										if( ! scfChildObj.get("EContractID").isJsonNull() )
											eContractID =  scfChildObj.get("EContractID").getAsString();
										
										if( ! scfChildObj.get("ECustomerID").isJsonNull() )
											eCustomerID =  scfChildObj.get("ECustomerID").getAsString();
										
									}
									if (debug) {
										response.getWriter().println("eCustomerID: "+eCustomerID);
										response.getWriter().println("eContractID: "+eContractID);
									}
									
									//Final payload
									finalPayload.addProperty("CustomerId", eCustomerID);
									finalPayload.addProperty("ContractId", eContractID);
									finalPayload.addProperty("AggregatorID", aggregatorFromDest);
								}
							}else{
								String aggregatorIDFromDest="";
								aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
								mode=inputPayload.get("Mode").getAsString();
								if(!mode.equalsIgnoreCase("") && mode.equalsIgnoreCase("OP")){
									JsonObject scfChildObj = new JsonObject();
									String scfFilter ="", loginMethod="";
									String sapclient = destConfiguration.get("sap-client").get().toString();
									if (debug)
										response.getWriter().println("sapclient:" + sapclient);	
									String authMethod = destConfiguration.get("Authentication").get().toString();
									if (debug)
										response.getWriter().println("authMethod:" + authMethod);
									
									//JsonObject = Call desturl;+"/sap/opu/odata/ARTEC/PYGW/SupplyChainFinances
									sessionID ="";
									if( null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")){
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug)
											response.getWriter().println("url2:" + url);
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
										response.getWriter().println("sessionID2:" + sessionID);
									
									String formattedStr = "";
									try{
										int number = Integer.parseInt(cpGuid);
										formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
										cpGuid = formattedStr;
									}catch (NumberFormatException e) {
//										formattedStr = customerNo;
									}
									
									scfService=""; scfFilter="";
									scfFilter = "LoginID eq '"+sessionID+"' and CPType eq '"+cpTypeID+"' and CPGUID eq '"+cpGuid+"'";
									if (debug)
										response.getWriter().println("scfFilter1: "+scfFilter);
									
									scfFilter = URLEncoder.encode(scfFilter, "UTF-8");
									
									scfFilter = scfFilter.replaceAll("%26", "&");
									scfFilter = scfFilter.replaceAll("%3D", "=");
									scfFilter = scfFilter.replaceAll("%3D", "=");
									if (debug)
										response.getWriter().println("scfFilter: "+scfFilter);
									
									if(sapclient != null){
										scfService = destURL+"/sap/opu/odata/ARTEC/PYGW/eSignContracts?sap-client="+ sapclient +"&$filter="+ scfFilter;
									}
									else{
										scfService = destURL+"/sap/opu/odata/ARTEC/PYGW/eSignContracts?$filter="+ scfFilter;
									}
									if (debug)
										response.getWriter().println("scfService 2: "+scfService);
									
									scfObj = getSCFDetails(request, response, scfService, destConfiguration, debug);
									
									if (debug)
										response.getWriter().println("onprem.scfObj: "+scfObj);
									
									if(scfObj.has("error")){
										scfError = true;
										finalPayload = null;
										oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
										userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
										password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
										aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
										
										userPass = userName+":"+password;
										
										appLogID = oDataLogs.insertExceptionLogs(request, response, scfObj.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorIDFromDest, loginID, debug);
									}else if(scfObj.getAsJsonObject("d").getAsJsonArray("results").size() == 0){
										scfEmpty = true;
										finalPayload = null;
									}else if(! scfObj.isJsonNull()){
										for (int i = 0; i < scfObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
										{
											scfChildObj = scfObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
											
											if( ! scfChildObj.get("ContractID").isJsonNull() )
												eContractID =  scfChildObj.get("ContractID").getAsString();
											else
												eContractID =  "";
											
											eCustomerID = "5b61dc0a4fc06a32c67b2c53";//DEV
											eCustomerID = "5aaa55461140a63021e05041";//PRD
										}
										
										if (debug) {
											response.getWriter().println("eCustomerID: "+eCustomerID);
											response.getWriter().println("eContractID: "+eContractID);
										}
										
										//Final payload
										finalPayload.addProperty("CustomerId", eCustomerID);
										finalPayload.addProperty("ContractId", eContractID);
										finalPayload.addProperty("AggregatorID", aggregatorFromDest);
									}
								}else{
									//OP scenario where for companies, custid SCF entry will not be available in ECC at this stage, so have to pick from cloud
									String formattedStr = "";
									destURL="";
									destURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
									
									try{
										int number = Integer.parseInt(cpGuid);
										formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
										cpGuid = formattedStr;
									}catch (NumberFormatException e) {
		//									formattedStr = customerNo;
									}
									
									aggregatorIDFromDest="";
									aggregatorIDFromDest = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
									
									JsonObject scfChildObj = new JsonObject();
									
									scfService = destURL+"SupplyChainFinances?$filter=AggregatorID%20eq%20%27"+aggregatorIDFromDest+"%27%20and%20CPGUID%20eq%20%27"+cpGuid+"%27%20and%20CPTypeID%20eq%20%27"+cpTypeID+"%27";
									if (debug)
										response.getWriter().println("onpremise.company/custid.executeURL: "+scfService);
									
									scfObj = getSCFFromCloud(response, request, scfService, debug);
									
									if (debug) {
										response.getWriter().println("scfObj: "+scfObj);
									}
									if(scfObj.has("error")){
										scfError = true;
										finalPayload = null;
										
										oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
										userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
										password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
										aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
										
										userPass = userName+":"+password;
										
										appLogID = oDataLogs.insertExceptionLogs(request, response, scfObj.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorIDFromDest, loginID, debug);
									}else if(scfObj.getAsJsonObject("d").getAsJsonArray("results").size() == 0){
										scfEmpty = true;
										finalPayload = null;
									}else if(! scfObj.isJsonNull()){
										for (int i = 0; i < scfObj.getAsJsonObject("d").getAsJsonArray("results").size(); i++)
										{
											scfChildObj = scfObj.getAsJsonObject("d").getAsJsonArray("results").get(i).getAsJsonObject();
											
											if( ! scfChildObj.get("EContractID").isJsonNull() )
												eContractID =  scfChildObj.get("EContractID").getAsString();
											
											if( ! scfChildObj.get("ECustomerID").isJsonNull() )
												eCustomerID =  scfChildObj.get("ECustomerID").getAsString();
											
										}
										if (debug) {
											response.getWriter().println("eCustomerID: "+eCustomerID);
											response.getWriter().println("eContractID: "+eContractID);
										}
										
										//Final payload
										finalPayload.addProperty("CustomerId", eCustomerID);
										finalPayload.addProperty("ContractId", eContractID);
										finalPayload.addProperty("AggregatorID", aggregatorFromDest);
									}
								
								}
								
							}
						}
						
						if(debug)
							response.getWriter().println("finalPayload: "+finalPayload);
						
						if(scfEmpty){
							JsonObject result = new JsonObject();
							result.addProperty("Status", "000002");
							result.addProperty("ErrorCode", "/ARTEC/J004");
							result.addProperty("Message", "Failed to resend. Failed due to technical issues (Unable to fetch CF Application Details)");
							result.addProperty("Remarks", "Failed due to technical issues (Unable to fetch CF Application Details)");
							response.getWriter().println(new Gson().toJson(result));
						}else{
							if(! finalPayload.isJsonNull()){
								byte[] postDataBytes = finalPayload.toString().getBytes("UTF-8");
								
								String cpiDestUrl="", cpiUser="", cpiPass="", cpiUserPass="", cpiResponse="";
								cpiDestUrl = cpiDestConfig.get("URL").get().toString();
								cpiUser = cpiDestConfig.get("User").get().toString();
								cpiPass = cpiDestConfig.get("Password").get().toString();
								cpiUserPass = cpiUser+":"+cpiPass;
								cpiDestUrl = cpiDestUrl+properties.getProperty("eSignContractReminder");
								
								if(debug){
									response.getWriter().println("cpiUserPass: "+cpiUserPass);
									response.getWriter().println("cpiDestUrl: "+cpiDestUrl);
								}
								
								try{
									URL url = new URL(cpiDestUrl);
									HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
									
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
									osw.write(finalPayload.toString());
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
									
									JsonParser cpiResponseParser = new JsonParser();
									JsonObject responseJson = (JsonObject)cpiResponseParser.parse(cpiResponse); 
									
									responseJson.addProperty("Remarks", "");
									if (debug)
										response.getWriter().println("responseJson: "+responseJson);
									response.getWriter().println(responseJson);
								}catch (Exception e) {
									String logID="";
									StackTraceElement element[] = e.getStackTrace();
									StringBuffer buffer = new StringBuffer();
									for(int i=0;i<element.length;i++)
									{
										buffer.append(element[i]);
									}
									if(debug)
										response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
									
									if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
										debug=true;
									
									oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
									userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
									password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
									aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
									loginID = commonUtils.getUserPrincipal(request, "name", response);
									
									userPass = userName+":"+password;
									
									logID = oDataLogs.insertApplicationLogs(request, response, "Java", "eSignContractReminder", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
									
									JsonObject result = new JsonObject();
									result.addProperty("Status", "000002");
									result.addProperty("ErrorCode", "/ARTEC/JE001");
									result.addProperty("Message", "Failed to resend. Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+")");
									result.addProperty("Remarks", "Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+")");
									
									response.getWriter().println(new Gson().toJson(result));
								}
							}else{
								JsonObject result = new JsonObject();
								//Json is null-insert application logs and technical error response
								result.addProperty("Status", "000002");
								result.addProperty("ErrorCode", "/ARTEC/J005");
								result.addProperty("Message", "Failed to resend. Failed due to technical issues (Unable to fetch CF Application Details)");
								result.addProperty("Remarks", "Failed due to technical issues (Unable to fetch CF Application Details)");
								response.getWriter().println(new Gson().toJson(result));
							}
						}
					}else{
						JsonObject result = new JsonObject();
						result.addProperty("Status", "000002");
						result.addProperty("ErrorCode", "/ARTEC/J003");
						result.addProperty("Message", "Failed to resend. "+mandatoryCheckMsg);
						result.addProperty("Remarks", mandatoryCheckMsg);
						response.getWriter().println(new Gson().toJson(result));
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
					
					oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
					userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
					password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
					loginID = commonUtils.getUserPrincipal(request, "name", response);
					
					userPass = userName+":"+password;
					String logID = "";
					logID = oDataLogs.insertApplicationLogs(request, response, "Java", "eSignContractReminder", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
					
					
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "/ARTEC/J001");
					result.addProperty("Message", "Failed to resend. Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+")");
					result.addProperty("Remarks", "Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+")");
					response.getWriter().println(new Gson().toJson(result));
				}
			}else{
				//Blank request
				if(debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J002");
				result.addProperty("Message", "Dear Customer, Currently we are facing technical issues in processing your request for resending contract email. Kindly try again later");
				result.addProperty("Remarks", "No input is received in the request");
				response.getWriter().println(new Gson().toJson(result));
			}
			/*JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "/ARTEC/J003");
			result.addProperty("Message", "Dear Customer, please re-check your registered email Id sandeep.singh@arteriatech.com or mobile number 8851153928 and e-sign the channel finance agreement");
			result.addProperty("Remarks", "This is a dummy output");
			response.getWriter().println(new Gson().toJson(result));*/
		}catch (Exception e) {
			String logID="";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true"))
				debug=true;
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			userPass = userName+":"+password;
			
			logID = oDataLogs.insertApplicationLogs(request, response, "Java", "eSignContractReminder", buffer.toString(), "", "", oDataURL, userPass, aggregatorID, loginID, debug);
			
			JsonObject result = new JsonObject();
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "/ARTEC/JE001");
			result.addProperty("Message", "Failed to resend. Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+"')");
			result.addProperty("Remarks", "Failed due to technical issues ("+e.getClass()+"-"+e.getLocalizedMessage()+"'"+logID+"')");
			
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	//added method
	public String getGetBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }
		body = jb.toString();
		return body;
	}
	
	public JsonObject getSCFFromCloud(HttpServletResponse response, HttpServletRequest request, String executeURL, boolean debug) throws IOException{

		CommonUtils commonUtils = new CommonUtils();
		String userName="", password="",userPass="";
		JsonObject scfResponse = new JsonObject();
		try {
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			
			if (debug) {
				response.getWriter().println("getSCFFromCloud.userName: "+userName);
				response.getWriter().println("getSCFFromCloud.password: "+password);
				response.getWriter().println("getSCFFromCloud.userPass: "+userPass);
			}
			scfResponse = commonUtils.executeURL(executeURL, userPass, response); // we are getting exception here
			if (debug)
				response.getWriter().println("getSCFFromCloud.scfResponse: "+scfResponse);
			
		} catch (Exception e) {
			scfResponse = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "eSignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
		}
		return scfResponse;
	}
	
	private JsonObject getSCFDetails(HttpServletRequest request, HttpServletResponse response, String customerService, Destination destConfiguration, boolean debug) throws IOException, URISyntaxException
	{
		String userName="", password="", authParam="", authMethod="", basicAuth=""; 
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject scfJson = new JsonObject();
		
		HttpGet scfGet = null;
		HttpEntity scfEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try{
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if(debug){
				response.getWriter().println("getTenant.getId: "+tenantContext.getTenant().getAccount().getId());
				response.getWriter().println("getTenant.getCustomerId: "+tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("getTenant.getName: "+tenantContext.getTenant().getAccount().getName());
			} */
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
	        scfGet = new HttpGet(customerService);
	        // scfGet.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
	        scfGet.setHeader("content-type", "text/xml; charset=UTF-8");
	        scfGet.setHeader("Accept", "application/json");
	        if(authMethod.equalsIgnoreCase("BasicAuthentication")){
	        	scfGet.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        }else{
	        	scfGet.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
	        }
	        
	        // HttpResponse httpResponse = closableHttpClient.execute(scfGet);
	        HttpResponse httpResponse = client.execute(scfGet);

	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if(debug)
	        	response.getWriter().println("getScfDetails.statusCode: "+statusCode);
			
			scfEntity = httpResponse.getEntity();
			
			if(scfEntity != null)
			{
				String retSrc = EntityUtils.toString(scfEntity);
				
				if (debug)
					response.getWriter().println("retSrc: "+retSrc);
				
				JsonParser parser = new JsonParser();
				scfJson = (JsonObject)parser.parse(retSrc);
				if(debug)
					response.getWriter().println("scfJson: "+scfJson);
				
				
				
				/*docBuilder = docBuilderFactory.newDocumentBuilder();
				inputSource = new InputSource(new StringReader(retSrc));
	            Document document = docBuilder.parse(inputSource);
	            NodeList corpList = document.getElementsByTagName("d:CorpId");
	            NodeList userList = document.getElementsByTagName("d:UserId");
	            NodeList userRegList = document.getElementsByTagName("d:UserRegId");
	            NodeList aggrIDList = document.getElementsByTagName("d:AggrID");
        		userCustomerJson.addProperty("CorpId", corpList.item(0).getTextContent());
        		userCustomerJson.addProperty("UserId", userList.item(0).getTextContent());
        		userCustomerJson.addProperty("UserRegId", userRegList.item(0).getTextContent());
        		userCustomerJson.addProperty("AggregatorID", aggrIDList.item(0).getTextContent());
	            if (debug) {
	            	if (debug)
						response.getWriter().println("retSrc: "+retSrc);
				}*/
			}
		}catch (RuntimeException e) {
			scfJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "eSignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
		}/*catch (ParserConfigurationException e) {
			scfJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
		} catch (SAXException e) {
			scfJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "ESignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
			
		}catch (NamingException e) {
			scfJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "eSignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
		}*/ catch (Exception e) {
			scfJson = null;
			ODataLogs oDataLogs = new ODataLogs();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println(e.getMessage()+"---> in doGet. Full Stack Trace: "+buffer.toString());
			String oDataURL="", aggregatorID="",loginID="", userPass="";
			
			loginID = commonUtils.getUserPrincipal(request, "name", response);
			
			oDataURL = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			userPass = userName+":"+password;
			
			oDataLogs.insertExceptionLogs(request, response, buffer.toString(), "eSignContractReminder", oDataURL, userPass, aggregatorID, loginID, debug);
		}
		finally
		{
			// closableHttpClient.close();
		}
		return scfJson;
	}
}
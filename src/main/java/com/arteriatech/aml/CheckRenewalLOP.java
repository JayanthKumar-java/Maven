package com.arteriatech.aml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;

public class CheckRenewalLOP extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cpGuid="",cpTypeID="";
		boolean debug=false;
		JsonObject resObj=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		Properties properties = new Properties();
		JsonObject inpJsonPayLoad=new JsonObject();
		JsonObject rootObj=new JsonObject();
		try{
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;
			}
			if(request.getParameter("CPGuid")!=null && !request.getParameter("CPGuid").equalsIgnoreCase("")){
				cpGuid=request.getParameter("CPGuid");
				if (request.getParameter("CPTypeID") != null && !request.getParameter("CPTypeID").equalsIgnoreCase("")) {
					cpTypeID=request.getParameter("CPTypeID");
					/*
					 * boolean isDigit = cpGuid.matches("[0-9]+"); if (isDigit) { if (cpGuid.length() < 10) { try { int cpGUIDNum = Integer.parseInt(cpGuid); cpGuid = String.format("%010d", cpGUIDNum); } catch (Exception ex) {
					 * 
					 * } } }
					 */

					if (debug) {
						response.getWriter().println("cpGuid:" + cpGuid);
						response.getWriter().println("cpTypeID:" + cpTypeID);
					}

					String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					String oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
					String userpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataURL:" + oDataURL);
						response.getWriter().println("aggregatorID:" + aggregatorID);
						response.getWriter().println("userpass:" + userpass);
					}
					JsonObject sccnfObj = getSCCNFGRecords(oDataURL, userpass, aggregatorID, response,cpTypeID,debug);
					if (sccnfObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (sccnfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							sccnfObj = sccnfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
							if (!sccnfObj.get("CORPID").isJsonNull() && !sccnfObj.get("CORPID").getAsString().equalsIgnoreCase("")) {
								String corpId = sccnfObj.get("CORPID").getAsString();
								rootObj.addProperty("CorpId", corpId);
								rootObj.addProperty("DealerId", cpGuid);
								inpJsonPayLoad.add("Root", rootObj);
								if (debug) {
									response.getWriter().println("inpJsonPayLoad:" + inpJsonPayLoad);
								}
								DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
										.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
								Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
										.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
								Destination cpiConfig = destinationAccessor.get();

								String wsURL = cpiConfig.get("URL").get().toString();
								if (debug) {
									response.getWriter().println("WsURL :" + wsURL);
								}
								String userName = cpiConfig.get("User").get().toString();
								String passWord = cpiConfig.get("Password").get().toString();
								userpass = userName + ":" + passWord;
								String scfOffer = properties.getProperty("SCFOfferScenario");
								wsURL = wsURL.concat(scfOffer);
								if (debug) {
									response.getWriter().println("SCFOffer Url: " + wsURL);
									response.getWriter().println("userpass: " + userpass);
								}
								URL url = new URL(wsURL);
								HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
								byte[] bytes = inpJsonPayLoad.toString().getBytes("UTF-8");
								urlConnection.setRequestMethod("GET");
								urlConnection.setRequestProperty("Content-Type", "application/json");
								urlConnection.setRequestProperty("charset", "utf-8");
								urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
								urlConnection.setRequestProperty("Accept", "application/json");
								urlConnection.setDoOutput(true);
								urlConnection.setDoInput(true);
								//String basicAuth = "Bearer " + userpass;

								String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());

								urlConnection.setRequestProperty("Authorization", basicAuth);
								urlConnection.connect();
								OutputStream outputStream = urlConnection.getOutputStream();
								OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
								osw.write(inpJsonPayLoad.toString());
								osw.flush();
								osw.close();
								int resCode = urlConnection.getResponseCode();
								if (debug) {
									response.getWriter().println("responseCode: " + resCode);
								}
								if ((resCode / 100) == 2 || (resCode / 100) == 3) {
									StringBuffer sb = new StringBuffer();
									BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
									String line = null;
									while ((line = br.readLine()) != null) {
										sb.append(line + "\n");
									}
									br.close();
									if (debug) {
										response.getWriter().println("sb: " + sb.toString());
									}
									JsonParser jsonParser = new JsonParser();
									JsonObject responseJson = (JsonObject) jsonParser.parse(sb.toString());
									if (debug)
										response.getWriter().println("responseJson: " + responseJson);
									response.getWriter().println(responseJson);
								} else {
									StringBuffer sb = new StringBuffer();
									BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
									String line = null;
									while ((line = br.readLine()) != null) {
										sb.append(line + "\n");
									}
									br.close();
									if (debug) {
										response.getWriter().println("getErrorStream: " + sb.toString());
									}
									JsonParser jsonParser = new JsonParser();
									JsonObject responseJson = (JsonObject) jsonParser.parse(sb.toString());
									responseJson.addProperty("Remarks", "");
									if (debug)
										response.getWriter().println("responseJson: " + responseJson);
									response.getWriter().println(responseJson);
								}
							} else {
								resObj.addProperty("Message", "CORPID doesn't exist for the AggregatorId:" + aggregatorID);
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(resObj);
							}
						} else {
							resObj.addProperty("Message", "Records doesn't exist in the SCCNFG table for the AggregatorId:" + aggregatorID);
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						}
					} else {
						response.getWriter().println(sccnfObj);
					}
				}else{
					resObj.addProperty("Message", "CPTypeID missing in the input payload");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			}else{
				resObj.addProperty("Message", "CPGuid missing in the input payload");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}
			
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message",ex.getLocalizedMessage()+"");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J003");
			resObj.addProperty("ExceptionTrace", buffer.toString());
			response.getWriter().println(resObj);
		}
	}
	
	private JsonObject getSCCNFGRecords(String OdataUrl,String userpass, String aggregatorID, HttpServletResponse response,String cpType, boolean debug) {
		CommonUtils commonUtils=new CommonUtils();
		JsonObject resObj=new JsonObject();
		try{
			String executeURL=OdataUrl+"SCCNFG?$filter=AGGRID%20eq%20%27"+aggregatorID+"%27%20and%20CP_TYPE%20eq%20%27"+cpType+"%27";
			if(debug){
				response.getWriter().println("executeURL:"+executeURL);
			}
			JsonObject sccnfgObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if(debug){
				response.getWriter().println("sccnfgObj:"+sccnfgObj);
			}
			return sccnfgObj;
		}catch(Exception ex){
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Message", ex.getLocalizedMessage()+"");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			return resObj;
		}
}

	
	

}

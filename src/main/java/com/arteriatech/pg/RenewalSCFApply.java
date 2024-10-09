package com.arteriatech.pg;

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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;


import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import io.vavr.control.Try;

/**
 * Servlet implementation class RenewalSCFApply
 */
@WebServlet("/RenewalSCFApply")
public class RenewalSCFApply extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SCFLIMIT_DEST_NAME =  "SCFLimit";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RenewalSCFApply() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		CommonUtils commonUtils = new CommonUtils();
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		String payLoad = "", corpID = "", cpType="", aggregatorID="", cpGuid="", wsURL="", cpiResponse="", userName="", passWord="", userpass="";
		boolean debug=false;
		try{
			payLoad = request.getParameter("RenewalSCFApply");
			if (null != payLoad && payLoad.trim().length() > 0 && payLoad != "") {
				JSONObject jsonObject = new JSONObject(payLoad);
				
				try {
					if (null != jsonObject.getString("debug")
							&& jsonObject.getString("debug").equalsIgnoreCase("true")) {
						debug = true;
					}
				} catch (JSONException e) {
					if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
						debug = false;
					}
				}
				
				cpGuid = jsonObject.getString("CPGuid");
				cpType = jsonObject.getString("CPTypeID");
				
				if(cpType.equalsIgnoreCase("01")){
					corpID = commonUtils.readDestProperties("CorpID");
				} else if(cpType.equalsIgnoreCase("60")){
					corpID = commonUtils.readDestProperties("VendorCorpID");
				}
				aggregatorID = commonUtils.readDestProperties("AggrID");
				
				
				if(debug){
					response.getWriter().println("corpID: "+corpID);
					response.getWriter().println("aggregatorID: "+aggregatorID);
					response.getWriter().println("cpGuid: "+cpGuid);
				}
				
				if (cpGuid != null && cpGuid.trim().length() >0) {
					String formattedStr = "";
					try{
						int number = Integer.parseInt(cpGuid);
						formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
						cpGuid = formattedStr;
					}catch (NumberFormatException e) {
//						formattedStr = customerNo;
					}
					
					String name = SCFLIMIT_DEST_NAME;
					
					DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(name, options);
			Destination cpiConfig= destinationAccessor.get();
					
			wsURL=cpiConfig.get("URL").get().toString();
			if (debug) {
				response.getWriter().println("WsURL :" + wsURL);
				}
					
				userName = cpiConfig.get("User").get().toString();
				passWord=cpiConfig.get("Password").get().toString();
					userpass = userName+":"+passWord;
					
					JSONObject inputJson = new JSONObject();
					JSONObject root = new JSONObject();
				
					inputJson.put("DealerId", cpGuid);
					inputJson.put("AggregatorId", aggregatorID);
					inputJson.put("CorporateID", corpID);
					root.put("Root", inputJson);
					
					byte[] postDataBytes = root.toString().getBytes("UTF-8");
					wsURL = wsURL + "/" + properties.getProperty("RenewalSCFApplyScenario");
					
					if (debug) {
						response.getWriter().println("wsURL-RenewalSCFApplyScenario: " + wsURL);
					}
					
					URL url = new URL(wsURL);
					HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json");
					con.setRequestProperty("charset", "utf-8");
					con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
					con.setRequestProperty("Accept", "application/json");
					con.setDoOutput(true);
					con.setDoInput(true);

					String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
					con.setRequestProperty("Authorization", basicAuth);
					con.connect();

					OutputStream os = con.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
					osw.write(root.toString());
					osw.flush();
					osw.close();

					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(con.getInputStream(), "utf-8"));
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();

					if (debug)
						response.getWriter().println("sb: " + sb.toString());

					cpiResponse = sb.toString();
					response.getWriter().println(cpiResponse);
				}else{
					JsonObject result = new JsonObject();
//					result.addProperty("Exception", e.getClass().getCanonicalName());
					result.addProperty("Message", "CPGuid is mandatory");
					result.addProperty("Status", "000002");
					result.addProperty("Valid", "false");
					result.addProperty("ErrorCode", "J002");
					response.getWriter().println(new Gson().toJson(result));
				}
				
				/*JsonObject result = new JsonObject();
//				result.addProperty("Exception", e.getClass().getCanonicalName());
				result.addProperty("Message", "Success");
				result.addProperty("Status", "000001");
				result.addProperty("Valid", "true");
				result.addProperty("ErrorCode", "");
				response.getWriter().println(new Gson().toJson(result));*/
			}else{
				JsonObject result = new JsonObject();
//				result.addProperty("Exception", e.getClass().getCanonicalName());
				result.addProperty("Message", "Empty Payload received in the request");
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				result.addProperty("ErrorCode", "J001");
				response.getWriter().println(new Gson().toJson(result));
			}
		}catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			result.addProperty("ErrorCode", "J001");
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

}

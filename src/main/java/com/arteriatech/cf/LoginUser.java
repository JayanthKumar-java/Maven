package com.arteriatech.cf;

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

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import io.vavr.control.Try;

/**
 * Servlet implementation class LoginUser
 */
@WebServlet("/LoginUser")
public class LoginUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SCFLIMIT_DEST_NAME =  "SCFLimit";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginUser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		doGet(request, response);
		CommonUtils commonUtils = new CommonUtils();
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		String payLoad = "", aggregatorID="", wsURL="", cpiResponse="", userName="", passWord="", userpass="";
		boolean debug=false;
		try{
//			payLoad = request.getParameter("LoginUser");
			payLoad = getGetBody(request, response);
			
			if (null != payLoad && payLoad.trim().length() > 0 && payLoad != "") {
				/*JSONObject jsonObject = new JSONObject(payLoad);
				
				try {
					if (null != jsonObject.getString("debug")
							&& jsonObject.getString("debug").equalsIgnoreCase("true")) {
						debug = true;
					}
				} catch (JSONException e) {
					if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
						debug = false;
					}
				}*/
				
//				aggregatorID = jsonObject.getString("AggregatorID");
				if(debug){
					response.getWriter().println("aggregatorID: "+aggregatorID);
				}
				
				String name = SCFLIMIT_DEST_NAME;
				
				// Context ctxDestFact = new InitialContext();
				// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxDestFact.lookup("java:comp/env/connectivityConfiguration");
				// DestinationConfiguration cpiDestConfig = configuration.getConfiguration(name);
				DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
						.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
						.tryGetDestination(name, options);
				Destination cpiDestConfig = destinationAccessor.get();

				wsURL = cpiDestConfig.get("URL").get().toString();
				
				userName = cpiDestConfig.get("User").get().toString();
				passWord = cpiDestConfig.get("Password").get().toString();
				userpass = userName+":"+passWord;
				
//				JSONObject inputJson = new JSONObject();
//				inputJson.accumulate("Root", value)
//				JSONObject root = new JSONObject();
//				inputJson.put("AggregatorID", aggregatorID);
//				root.put("Root", inputJson);
				
//				byte[] postDataBytes = inputJson.toString().getBytes("UTF-8");
				byte[] postDataBytes = payLoad.getBytes("UTF-8");
				wsURL = wsURL + "/" + properties.getProperty("LoginUserCreate");
				
				if (debug) {
					response.getWriter().println("wsURL-LoginUserCreate: " + wsURL);
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
				osw.write(payLoad.toString());
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
//		  response.getWriter().println("jb.toString(): "+jb.toString());
		return body;
	}

}

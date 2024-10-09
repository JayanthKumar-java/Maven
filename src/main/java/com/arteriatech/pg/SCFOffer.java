package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
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
 * Servlet implementation class SCFOffer
 */
@WebServlet("/SCFOffer")
public class SCFOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SCFLIMIT_DEST_NAME =  "SCFLimit";
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SCFOffer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		doGet(request, response);


		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try {
			boolean debug = false, requestDebug = false;

			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				requestDebug = true;
			}

			if (requestDebug) {
				response.getWriter().println("Inside doGet()");
			}
			String customerNo = "", errorCode = "", errorMsg = "", sessionID = "", merchantCode = "", wsURL = "";

			CommonUtils commonUtils = new CommonUtils();

			if (requestDebug)
				response.getWriter().println("Inside doGet(): " + request.getParameter("SCFOffer"));

			JSONObject jsonobject1 = new JSONObject(request.getParameter("SCFOffer"));

			if (requestDebug) {
				response.getWriter().println("jsonobject1: " + jsonobject1);
				response.getWriter().println("jsonobject1.toString: " + jsonobject1.toString());
			}

			String payLoad = request.getParameter("SCFOffer");

			if (requestDebug)
				response.getWriter().println("payLoad: " + payLoad);

			if (null != payLoad && payLoad.trim().length() > 0 && payLoad != "") {
				JSONObject jsonobject = new JSONObject(payLoad);
				if (requestDebug)
					response.getWriter().println("jsonobject: " + jsonobject);

				try {
					if (null != jsonobject.getString("debug")
							&& jsonobject.getString("debug").equalsIgnoreCase("true")) {
						debug = true;
					}
				} catch (JSONException e) {
					if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")) {
						debug = false;
					}
				}

				// response.getWriter().println("debug: "+debug);
				if (debug) {
					response.getWriter().println("payLoad: " + payLoad);
					response.getWriter().println("CustomerNo: " + jsonobject.getString("CustomerNo"));
				}

				if (null != jsonobject.getString("CustomerNo")) {
					customerNo = jsonobject.getString("CustomerNo");
				} else {
					errorCode = "E100";
					errorMsg = properties.getProperty(errorCode);
				}

				String loginID = commonUtils.getLoginID(request, response, debug);
				// if (request.getUserPrincipal() != null) {
				if (loginID != null) {		
					if (loginID == null) {
						errorCode = "E125";
					} else {
						String authMethod = commonUtils.getDestinationURL(request, response, "Authentication");
						if (debug)
							response.getWriter().println("authMethod:" + authMethod);
						if(authMethod.equalsIgnoreCase("BasicAuthentication")){
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug)
								response.getWriter().println("url:" + url);
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							if (debug)
								response.getWriter().println("Generating sessionID:" + sessionID);
							if (sessionID.contains(" ")) {
								errorCode = "S001";
								errorMsg = sessionID;

								if (debug)
									response.getWriter().println("Generating sessionID - contains errorCode:" + errorCode + " : " + errorMsg);
							}
						} else{
							sessionID ="";
						}
					}
				} else {
					errorCode = "E125";
					if (debug)
						response.getWriter().println("Generating sessionID - errorCode:" + errorCode);
				}

				if (debug) {
					response.getWriter().println("errorCode: " + errorCode);
					response.getWriter().println("errorMsg: " + errorMsg);
				}

				if (errorCode != null & errorCode.trim().length() == 0) {
					errorCode = commonUtils.validateCustomer(request, response, sessionID, customerNo, debug);
					if (debug) {
						response.getWriter().println("errorCode: " + errorCode);
						response.getWriter().println("errorMsg: " + errorMsg);
					}

					if (errorCode != null & errorCode.trim().length() == 0) {
						merchantCode = commonUtils.readDestProperties("CorpID");
						if (debug) {
							response.getWriter().println("merchantCode: " + merchantCode);
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

						if (debug) {
							response.getWriter().println("wsURL: " + wsURL);
						}

						JSONObject inputJson = new JSONObject();
						JSONObject root = new JSONObject();
						inputJson.put("DealerId", customerNo);
						inputJson.put("CorpId", merchantCode);
						root.put("Root", inputJson);

						byte[] postDataBytes = root.toString().getBytes("UTF-8");

						String userName="", passWord="", userpass = "";
						
						userName = cpiDestConfig.get("User").get().toString();
						passWord = cpiDestConfig.get("Password").get().toString();
						userpass = userName+":"+passWord;

						if (wsURL != "E106" && merchantCode != "E152" && userpass != "E127" && wsURL.trim().length() > 0) {
							// wsURL=wsURL+"/SCFLimitEnhancement";
							wsURL = wsURL + "/" + properties.getProperty("SCFOfferScenario");
							if (debug) {
								response.getWriter().println("wsURL-SCFOfferScenario: " + wsURL);
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

							String cpiResponse = sb.toString();

							if (debug) {
								response.getWriter().println(cpiResponse);
							}

							if (cpiResponse != null && cpiResponse.trim().length() > 0) {
								response.getWriter().println(cpiResponse);
							} else {
								errorCode = "E107";
								errorMsg = properties.getProperty(errorCode);

								JsonObject result = new JsonObject();
								result.addProperty("errorCode", errorCode);
								result.addProperty("Message", errorMsg);
								result.addProperty("Status", properties.getProperty("ErrorStatus"));
								result.addProperty("Valid", "false");
								response.getWriter().println(new Gson().toJson(result));
							}
						} else {
							if (debug) {
								response.getWriter().println("Error Scenario-wsURL: " + wsURL);
								response.getWriter().println("Error Scenario-merchantCode: " + merchantCode);
								response.getWriter().println("Error Scenario-userpass: " + userpass);
							}
							
							if (wsURL.length() > 0) {
								errorCode = wsURL;
							} else if (merchantCode.length() > 0) {
								errorCode = merchantCode;
							} else if (userpass == "E127") {
								errorCode = userpass;
							}

							errorMsg = properties.getProperty(errorCode);

							JsonObject result = new JsonObject();
							result.addProperty("errorCode", errorCode);
							result.addProperty("Message", errorMsg);
							result.addProperty("Status", properties.getProperty("ErrorStatus"));
							result.addProperty("Valid", "false");
							response.getWriter().println(new Gson().toJson(result));
						}
					} else {
						errorMsg = properties.getProperty(errorCode);

						JsonObject result = new JsonObject();
						result.addProperty("errorCode", errorCode);
						result.addProperty("Message", errorMsg);
						result.addProperty("Status", properties.getProperty("ErrorStatus"));
						result.addProperty("Valid", "false");
						response.getWriter().println(new Gson().toJson(result));
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("errorCode", errorCode);
					result.addProperty("Message", errorMsg);
					result.addProperty("Status", properties.getProperty("ErrorStatus"));
					result.addProperty("Valid", "false");
					response.getWriter().println(new Gson().toJson(result));
				}
			} else {
				errorCode = "E118";
				errorMsg = properties.getProperty(errorCode);

				JsonObject result = new JsonObject();
				result.addProperty("errorCode", errorCode);
				result.addProperty("Message", errorMsg);
				result.addProperty("Status", properties.getProperty("ErrorStatus"));
				result.addProperty("Valid", "false");
				response.getWriter().println(new Gson().toJson(result));
			}
		} catch (URISyntaxException e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		} catch (Exception e) {
			JsonObject result = new JsonObject();
			result.addProperty("Exception", "" + e.getClass().getCanonicalName());
			result.addProperty("Message", e.getClass().getCanonicalName() + "--->" + e.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}

	
	}

}

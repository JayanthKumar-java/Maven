package com.arteriatech.support;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TYPS_T extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		boolean debug = false;
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		JSONObject tsetTObj = new JSONObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonPayload = (JsonObject) jsonParser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonPayload.has("AGGRID") && !jsonPayload.get("AGGRID").isJsonNull()
						&& !jsonPayload.get("AGGRID").getAsString().equalsIgnoreCase("")) {
					if (jsonPayload.has("TYPESET") && !jsonPayload.get("TYPESET").isJsonNull()
							&& !jsonPayload.get("TYPESET").getAsString().equalsIgnoreCase("")) {
						if (jsonPayload.has("LANGUAGE") && !jsonPayload.get("LANGUAGE").isJsonNull()
								&& !jsonPayload.get("LANGUAGE").getAsString().equalsIgnoreCase("")) {
							if (jsonPayload.has("TYPESNAME") && !jsonPayload.get("TYPESNAME").isJsonNull()
									&& !jsonPayload.get("TYPESNAME").getAsString().equalsIgnoreCase("")) {

								if (jsonPayload.has("TYPES") && !jsonPayload.get("TYPES").isJsonNull()
										&& !jsonPayload.get("TYPES").getAsString().equalsIgnoreCase("")) {
									oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
									userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
									password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
									userPass = userName + ":" + password;
									executeURL = oDataUrl + "TYPS_T";
									//executeURL=oDataUrl + "TYPS_T('"+jsonPayload.get("TYPES").getAsString()+"')";
									tsetTObj.accumulate("AGGRID", jsonPayload.get("AGGRID").getAsString());
									tsetTObj.accumulate("TYPESET", jsonPayload.get("TYPESET").getAsString());
									tsetTObj.accumulate("LANGUAGE", jsonPayload.get("LANGUAGE").getAsString());
									tsetTObj.accumulate("TYPESNAME", jsonPayload.get("TYPESNAME").getAsString());
									tsetTObj.accumulate("TYPES", jsonPayload.get("TYPES").getAsString());
									if (debug) {
										response.getWriter().println(" TsetT Payload " + tsetTObj);
										response.getWriter().println("executeURL "+executeURL);
									}
									
									JsonObject executePostURL = commonUtils.executePostURL(executeURL, userPass,
											response, tsetTObj, request, debug, "PCGWHANA");
									if (debug) {
										response.getWriter().println(" TsetT Response " + executePostURL);
									}

									if (executePostURL.has("error")) {
										JsonObject retunObj = new JsonObject();
										retunObj.addProperty("Status", "000002");
										retunObj.addProperty("ErrorCode", "J002");
										retunObj.addProperty("Message", "Insertion Failed");
										response.getWriter().println(retunObj);
									} else {
										JsonObject retunObj = new JsonObject();
										retunObj.addProperty("Status", "000001");
										retunObj.addProperty("ErrorCode", "");
										retunObj.addProperty("Message", "Record Inserted Successfully");
										response.getWriter().println(retunObj);
									}
								} else {
									JsonObject validateFields = validateFields("TYPES");
									response.getWriter().println(validateFields);

								}

							} else {
								JsonObject validateFields = validateFields("TSETNAME");
								response.getWriter().println(validateFields);

							}

						} else {
							JsonObject validateFields = validateFields("LANGUAGE");
							response.getWriter().println(validateFields);

						}
					} else {
						JsonObject validateFields = validateFields("TYPESET");
						response.getWriter().println(validateFields);

					}

				} else {
					JsonObject validateFields = validateFields("AGGRID");
					response.getWriter().println(validateFields);

				}

			} else {
				JsonObject validateFields = validateFields(null);
				response.getWriter().println(validateFields);

			}
		} catch (Exception ex) {

			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);

		}

	}

	public JsonObject validateFields(String field) {
		JsonObject retunObj = new JsonObject();
		if (field == null) {
			retunObj.addProperty("Message", " Input Payload is Empty ");
		} else {
			retunObj.addProperty("Message", field + " Is empty In the Input Payload ");
		}
		retunObj.addProperty("Status", "000002");
		retunObj.addProperty("ErrorCode", "J002");
		return retunObj;
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//doPost(request, response);
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		boolean debug = false;
		
		String oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		JSONObject tsetTObj = new JSONObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonPayload = (JsonObject) jsonParser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonPayload.has("AGGRID") && !jsonPayload.get("AGGRID").isJsonNull()
						&& !jsonPayload.get("AGGRID").getAsString().equalsIgnoreCase("")) {
					if (jsonPayload.has("TYPESET") && !jsonPayload.get("TYPESET").isJsonNull()
							&& !jsonPayload.get("TYPESET").getAsString().equalsIgnoreCase("")) {
						if (jsonPayload.has("LANGUAGE") && !jsonPayload.get("LANGUAGE").isJsonNull()
								&& !jsonPayload.get("LANGUAGE").getAsString().equalsIgnoreCase("")) {
							if (jsonPayload.has("TYPESNAME") && !jsonPayload.get("TYPESNAME").isJsonNull()
									&& !jsonPayload.get("TYPESNAME").getAsString().equalsIgnoreCase("")) {

								if (jsonPayload.has("TYPES") && !jsonPayload.get("TYPES").isJsonNull()
										&& !jsonPayload.get("TYPES").getAsString().equalsIgnoreCase("")) {
									oDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
									userName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
									password = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
									userPass = userName + ":" + password;
									// executeURL = oDataUrl +
									// "TYPS_T('"+jsonPayload.get("TYPES").getAsString()+"')";
									
									String types = jsonPayload.get("TYPES").getAsString();
									/*if(types.contains("|")){
										types=types.replace("|","\\|");
									}*/
									
									executeURL = oDataUrl+"TYPS_T(AGGRID='"+jsonPayload.get("AGGRID").getAsString()+"',TYPESET='"+jsonPayload.get("TYPESET").getAsString()+"',TYPES='"+types+"',LANGUAGE='E')";
									//executeURL = oDataUrl + "TYPS_T";
									tsetTObj.accumulate("AGGRID", jsonPayload.get("AGGRID").getAsString());
									tsetTObj.accumulate("TYPESET", jsonPayload.get("TYPESET").getAsString());
									tsetTObj.accumulate("LANGUAGE", jsonPayload.get("LANGUAGE").getAsString());
									tsetTObj.accumulate("TYPESNAME", jsonPayload.get("TYPESNAME").getAsString());
									tsetTObj.accumulate("TYPES", types);
									if (debug) {
										response.getWriter().println("TsetT Payload " + tsetTObj);
										response.getWriter().println("Execute Url: " + executeURL);
									}
									//JsonObject executeUpdate = commonUtils.executeUpdate(executeURL, userPass, response, tsetTObj, request, debug);
									
									URL url = new URL(executeURL);
									HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
									byte[] bytes = tsetTObj.toString().getBytes("UTF-8");
									urlConnection.setRequestMethod("PUT");
									urlConnection.setRequestProperty("Content-Type", "application/json");
									urlConnection.setRequestProperty("charset", "utf-8");
									urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
									urlConnection.setRequestProperty("Accept", "application/json");
									urlConnection.setDoOutput(true);
									urlConnection.setDoInput(true);
									String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
									urlConnection.setRequestProperty("Authorization", basicAuth);
									urlConnection.connect();
									OutputStream outputStream = urlConnection.getOutputStream();
									OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
									osw.write(tsetTObj.toString());
									osw.flush();
									osw.close();
									int resCode = urlConnection.getResponseCode();
									if (debug) {
										response.getWriter().println("responseCode: " + resCode);
										response.getWriter().println("Error Message:"+urlConnection.getResponseMessage());
										response.getWriter().println("Error Response Code:"+urlConnection.getResponseCode());
									}
									if(resCode==204 || resCode==200){
										JsonObject retunObj = new JsonObject();
										retunObj.addProperty("Status", "000001");
										retunObj.addProperty("ErrorCode", "");
										retunObj.addProperty("Message", "Record Updated Successfully");
										response.getWriter().println(retunObj);
									}else{
										
										JsonObject retunObj = new JsonObject();
										retunObj.addProperty("Status", "000001");
										retunObj.addProperty("ErrorCode", "");
										retunObj.addProperty("Message", "Records Not Updated");
										response.getWriter().println(retunObj);
										
									}
									
								/*if(executeUpdate.has("ErrorCode")&& executeUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")){
									JsonObject retunObj = new JsonObject();
									retunObj.addProperty("Status", "000001");
									retunObj.addProperty("ErrorCode", "");
									retunObj.addProperty("Message", "Record Updated Successfully");
									response.getWriter().println(retunObj);
								}else{
									JsonObject retunObj = new JsonObject();
									retunObj.addProperty("Status", "000001");
									retunObj.addProperty("ErrorCode", "");
									retunObj.addProperty("Message", "Records Not Updated");
									response.getWriter().println(retunObj);
									
								}*/
									
							} else {
									JsonObject validateFields = validateFields("TYPES");
									response.getWriter().println(validateFields);

								}

							} else {
								JsonObject validateFields = validateFields("TSETNAME");
								response.getWriter().println(validateFields);

							}

						} else {
							JsonObject validateFields = validateFields("LANGUAGE");
							response.getWriter().println(validateFields);

						}
					} else {
						JsonObject validateFields = validateFields("TYPESET");
						response.getWriter().println(validateFields);

					}

				} else {
					JsonObject validateFields = validateFields("AGGRID");
					response.getWriter().println(validateFields);

				}

			} else {
				JsonObject validateFields = validateFields(null);
				response.getWriter().println(validateFields);

			}
		} catch (Exception ex) {

			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);
		}
	}
}
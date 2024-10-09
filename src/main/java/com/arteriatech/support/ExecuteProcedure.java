package com.arteriatech.support;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExecuteProcedure extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String inputPayload = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonParser parser = new JsonParser();
		JsonObject resObj = new JsonObject();
		String procName = "", ODataUrl = "", username = "", password = "", userPass = "", executeURL = "",
				aggregatorID = "", typeValue = "", dbName = "", postFixValue = "";
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonInputPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonInputPayload.has("debug")
						&& jsonInputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (jsonInputPayload.has("ProcName") && !jsonInputPayload.get("ProcName").isJsonNull()
						&& !jsonInputPayload.get("ProcName").getAsString().equalsIgnoreCase("")) {
					procName = jsonInputPayload.get("ProcName").getAsString();
					ODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWXCMNHANA);
					username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWXCMNHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWXCMNHANA);
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID",
							DestinationUtils.PCGWXCMNHANA);
					userPass = username + ":" + password;
					executeURL = ODataUrl + "ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" + aggregatorID
							+ "%27%20and%20Typeset%20eq%20%27PROC%27%20and%20(Types%20eq%20%27PRFX%27%20or%20Types%20eq%20%27DBNM%27%20or%20Types%20eq%20%27"
							+ procName + "%27)";
					
					if (debug) {
						response.getWriter().println("ODataUrl:" + ODataUrl);
						response.getWriter().println("Username:" + username);
						response.getWriter().println("password:" + password);
						response.getWriter().println("executeURL:" + executeURL);
					}
					JsonObject configObj = commonUtils.executeURL(executeURL, userPass, response);
					if (debug) {
						response.getWriter().println("configObj: " + configObj);
					}

					if (configObj != null && !configObj.has("error")) {
						if (!configObj.get("d").getAsJsonObject().get("results").isJsonNull()
								&& configObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							JsonArray configArray = configObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							for (int i = 0; i < configArray.size(); i++) {
								JsonObject confObj = configArray.get(i).getAsJsonObject();
								if (!confObj.get("Types").isJsonNull()
										&& !confObj.get("Types").getAsString().equalsIgnoreCase("")) {
									String types = confObj.get("Types").getAsString();
									if (types.equalsIgnoreCase("PRFX")) {
										if (!confObj.get("TypeValue").isJsonNull()
												&& !confObj.get("TypeValue").getAsString().equalsIgnoreCase("")) {
											typeValue = confObj.get("TypeValue").getAsString();
										}
									} else if (types.equalsIgnoreCase("DBNM")) {
										if (!confObj.get("TypeValue").isJsonNull()
												&& !confObj.get("TypeValue").getAsString().equalsIgnoreCase("")) {
											dbName = confObj.get("TypeValue").getAsString();
										}

									} else {
										if (!confObj.get("TypeValue").isJsonNull()
												&& !confObj.get("TypeValue").getAsString().equalsIgnoreCase("")) {
											postFixValue = confObj.get("TypeValue").getAsString();
										}
									}
								}

							}

							if (debug) {
								response.getWriter().println("typeValue:" + typeValue);
								response.getWriter().println("dbName:" + dbName);
								response.getWriter().println("postFixValue:" + postFixValue);
							}
							String errorMessage=validateFields(typeValue,dbName,postFixValue);
							if(debug){
								response.getWriter().println("errorMessage:" + errorMessage);
							}
							
							if (errorMessage.equalsIgnoreCase("")) {
								executeURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SDI);
								username = commonUtils.getODataDestinationProperties("User", DestinationUtils.SDI);
								password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SDI);
								userPass = username + ":" + password;
								executeURL = executeURL + "?dbName=" + dbName + "&procName=" + typeValue
										+ "::"+postFixValue;
								if (debug) {
									response.getWriter().println("execute Procedure URL:" + executeURL);
									response.getWriter().println("User Name:" + username);
									response.getWriter().println("Password:" + password);
								}

								JsonObject procedureObj = executeProcedureUrl(executeURL, userPass, response,debug);
								if (debug) {
									response.getWriter().println("procedureObj:" + procedureObj);
								}

								if (procedureObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
									resObj.addProperty("Status", "000001");
									resObj.addProperty("ErrorCode", "");
									resObj.addProperty("Message",
											procedureObj.get("Message").getAsString().replaceAll("\"", ""));
									response.getWriter().println(resObj);
								} else {
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									resObj.addProperty("Message",
											procedureObj.get("Message").getAsString().replaceAll("\"", ""));
									response.getWriter().println(resObj);
								}
							} else {
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", errorMessage.replaceAll("\"", ""));
								response.getWriter().println(resObj);
							}

						} else {
							// Record not exist
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							resObj.addProperty("Message", "Records Not Exist");
							response.getWriter().println(resObj);
						}
					} else {
						// unable to fetch the Records from the table.
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.add("Message", configObj);
						response.getWriter().println(resObj);
					}
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "ProcName field Empty in the input payload");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Empty input payload Received");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			String localizedMessage = ex.getLocalizedMessage();
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", localizedMessage);
			response.getWriter().println(resObj);

		}
	}

	private String validateFields(String typeValue, String dbName, String postFixValue) {
		String errorMessage="";
		if(typeValue.equalsIgnoreCase("")||typeValue.trim().length()<0){
			errorMessage="typeValue Field is Empty";
		}
		if(dbName.equalsIgnoreCase("")||dbName.trim().length()<0){
			if(errorMessage.equalsIgnoreCase("")){
				errorMessage="dbname Field is empty";
			}else{
				errorMessage=errorMessage+",dbname Field is empty";
			}
		}
		
		if(postFixValue.equalsIgnoreCase("")||postFixValue.trim().length()<0){
			if(errorMessage.equalsIgnoreCase("")){
				errorMessage="postFixValue Field is empty";
			}else{
				errorMessage=errorMessage+",postFixValue Field is empty";
			}
		}
		return errorMessage;
	}

	public JsonObject executeProcedureUrl(String executeURL, String userPass, HttpServletResponse response,boolean debug) {
		DataOutputStream dataOut = null;
		BufferedReader in = null;
		JsonObject jsonObj = new JsonObject();
		try {
			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			connection.setDoInput(true);
			int resCode = connection.getResponseCode();
			if(debug){
				response.getWriter().println("resCode:"+resCode);
			}
			StringBuffer responseStrBuffer = new StringBuffer();
			if (resCode / 100 == 2) {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					responseStrBuffer.append(inputLine);
				}
				if (debug) {
					response.getWriter().println("resCode:" + resCode);
					response.getWriter().println("inputLine:" + inputLine);
				}
			}
			
			if (resCode / 100 == 2) {
				jsonObj.addProperty("Message", responseStrBuffer.toString().replaceAll("\"", ""));
				jsonObj.addProperty("ErrorCode", "");
				jsonObj.addProperty("Status", "000001");
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				jsonObj.addProperty("Message", sb.toString());
				jsonObj.addProperty("ErrorCode", "J002");
				jsonObj.addProperty("Status", "000002");
			}

		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			jsonObj.addProperty("Message", buffer.toString());
			jsonObj.addProperty("ErrorCode", "J002");
			jsonObj.addProperty("Status", "000002");
		}
		return jsonObj;
	}

}

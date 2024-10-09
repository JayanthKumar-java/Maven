package com.arteriatech.support;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.DeRegistration.DeRegistrationClient;
import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeRegistration extends HttpServlet {

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

		DeRegistrationClient deRegClient = new DeRegistrationClient();
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";

		JsonParser parser = new JsonParser();
		JsonObject jsonInput = new JsonObject();
		String corpId = "", aggId = "", userId = "", urn = "", aggrName = "";
		boolean debug = false;
		JsonObject resObj = new JsonObject();
		ODataLogs appLogs=new ODataLogs();
		JsonArray appLogArrya=new JsonArray();
		int stepNo=1;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonInput = (JsonObject) parser.parse(inputPayload);
				if (jsonInput.has("debug") && jsonInput.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("received input Paylaod:" + jsonInput);
				}
				String message=validateInputPayload(jsonInput);
				if(debug){
					response.getWriter().println("message:"+message);
				}
				if (message.equalsIgnoreCase("")) {
					JsonObject appLog = commonUtils.createApplictaionLogPayload(request, response, jsonInput.get("aggregatorId").getAsString(), "User Deregistration", request.getPathInfo(), debug);
					if(debug){
						response.getWriter().println("appLog:"+appLog);
					}
					if (!appLog.has("Status")) {
						String appLogId=appLog.get("ID").getAsString();
						appLogArrya.add(appLog);
						if (jsonInput.has("corpId") && !jsonInput.get("corpId").isJsonNull()) {
							corpId = jsonInput.get("corpId").getAsString();
						}
						if (jsonInput.has("aggregatorId") && !jsonInput.get("aggregatorId").isJsonNull()) {
							aggId = jsonInput.get("aggregatorId").getAsString();
						}

						if (jsonInput.has("userId") && !jsonInput.get("userId").isJsonNull()) {
							userId = jsonInput.get("userId").getAsString();
						}
						if (jsonInput.has("URN") && !jsonInput.get("URN").isJsonNull()) {
							urn = jsonInput.get("URN").getAsString();
						}

						if (jsonInput.has("aggregatorName") && !jsonInput.get("aggregatorName").isJsonNull()) {
							aggrName = jsonInput.get("aggregatorName").getAsString();
						}
						Map<String, String> deRegResponse = deRegClient.callDeRegistarationWebservice(corpId, aggId,
								userId, urn, aggrName, response, debug);
						for (String key : deRegResponse.keySet()) {
							resObj.addProperty(key, deRegResponse.get(key));
						}
						if(jsonInput.has("URN")){
							jsonInput.remove("URN");
						}
						JsonObject appMsgLog = commonUtils.createApplictaionLogMsgPayload(request, response, appLogId, "I", "/ARTEC/PY", stepNo, corpId, aggId, userId, aggrName,"Input from Ui"+jsonInput,"Response from CPI:"+resObj, debug);
						
						if(debug){
							response.getWriter().println("appMsgLog:"+appMsgLog);
						}
						if(!appMsgLog.has("Status")){
							appLogArrya.add(appMsgLog);
						}
						if (debug) {
							response.getWriter().println("Cpi Response " + new Gson().toJson(resObj));
						}
						JsonObject insertApplicationLogs = appLogs.insertApplicationLogs(response, appLogArrya, debug);
						if(debug){
							response.getWriter().println("insertApplicationLogs:"+insertApplicationLogs);
						}
						response.getWriter().print(new Gson().toJson(resObj));
					}else{
						response.getWriter().println(appLog); 
					  }
				}else{
					JsonObject result = new JsonObject();
					result.addProperty("Status", "000002");
					result.addProperty("ErrorCode", "J001");
					result.addProperty("Message", message);
					response.getWriter().println(result);
					
				}
			} else {
				if (debug)
					response.getWriter().println("Blank Request");
				JsonObject result = new JsonObject();
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "/ARTEC/J001");
				result.addProperty("Message", "Empty input Payload received from UI");
				response.getWriter().println(new Gson().toJson(result));
			}

		} catch (Exception ex) {
			JsonObject res = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			res.addProperty("ExceptionTrace", buffer.toString());
			res.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			res.addProperty("Status", "000002");
			res.addProperty("ErrorCode", "J002");
			response.getWriter().println(res);

		}

	}

	private String validateInputPayload(JsonObject jsonInput) {
		String message="";
		try {
			if (jsonInput.has("corpId")) {
				if (jsonInput.get("corpId").isJsonNull()
						|| jsonInput.get("corpId").getAsString().equalsIgnoreCase("")) {
					message = "corpId Empty in the input Payload";
					return message;
				}
			} else {
				message = "Input Payload doesn't contains a corpId Property";
				return message;
			}

			if (jsonInput.has("aggregatorId")) {
				if (jsonInput.get("aggregatorId").isJsonNull()
						|| jsonInput.get("aggregatorId").getAsString().equalsIgnoreCase("")) {
					message = "aggregatorId empty in the input Payload";
					return message;
				}
			} else {
				message = "Input Payload doesn't contains a aggregatorId Property";
				return message;

			}

			if (jsonInput.has("userId")) {
				if (jsonInput.get("userId").isJsonNull()
						|| jsonInput.get("userId").getAsString().equalsIgnoreCase("")) {
					message = "userId empty in the input Payload";
					return message;
				}
			} else {
				message = "Input Payload doesn't contains a userId Property";
				return message;
			}

			if (jsonInput.has("URN")) {
				if (jsonInput.get("URN").isJsonNull() || jsonInput.get("userId").getAsString().equalsIgnoreCase("")) {
					message = "URN empty in the input Payload";
					return message;
				}
			} else {
				message = "URN Payload doesn't contains a userId Property";
				return message;
			}
			return message;

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			buffer.insert(0, ex.getLocalizedMessage() + "--->");
			return buffer.toString();

		}
	}
}
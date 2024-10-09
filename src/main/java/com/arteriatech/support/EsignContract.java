package com.arteriatech.support;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.bc.eSignContract.EsignContractClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EsignContract extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String contractId = "";
		boolean debug = false;
		JsonObject retunObj = new JsonObject();

		JSONObject json = new JSONObject();
		EsignContractClient esignContClient = new EsignContractClient();
		try {
			contractId = request.getParameter("contractId");
			if (request.getParameter("debug") != null && !request.getParameter("debug").equalsIgnoreCase("")) {
				debug = true;
			}
			contractId = contractId.replaceAll("^\"|\"$", "");
			if (debug) {
				response.getWriter().println("Recived Input Contract ID " + contractId);
			}
			if (contractId != null && !contractId.equalsIgnoreCase("")) {
				JSONObject result = esignContClient.callEsignContractWebservice(request, response, contractId, debug);
				if (debug) {
					response.getWriter().println("CPI Response");
					response.getWriter().println(result);
				}
				response.getWriter().println(result);
			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Mandatory field contractId missing in the request: " + contractId);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}

		} catch (Exception ex) {
			JsonObject resObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

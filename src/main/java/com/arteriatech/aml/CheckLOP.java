package com.arteriatech.aml;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFOffer.SCFOfferClient;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonObject;

public class CheckLOP extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cpGuid = "", cpType = "";
		JsonObject resObj = new JsonObject();
		boolean debug = false;
		CommonUtils commonUtils = new CommonUtils();
		SCFOfferClient scfOfferClient = new SCFOfferClient();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (request.getParameter("CPGuid") != null && !request.getParameter("CPGuid").equalsIgnoreCase("")) {
				cpGuid = request.getParameter("CPGuid");

				if (request.getParameter("CPTypeID") != null
						&& !request.getParameter("CPTypeID").equalsIgnoreCase("")) {
					cpType = request.getParameter("CPTypeID");

					/*
					 * boolean isDigit = cpGuid.matches("[0-9]+");
					 * if (isDigit) {
					 * if (cpGuid.length() < 10) {
					 * try {
					 * int cpGUIDNum = Integer.parseInt(cpGuid);
					 * cpGuid = String.format("%010d", cpGUIDNum);
					 * } catch (Exception ex) {
					 * 
					 * }
					 * }
					 * }
					 */

					if (debug) {
						response.getWriter().println("cpGuid:" + cpGuid);
						response.getWriter().println("CPType:" + cpType);
					}

					String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
					String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
					String oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
					String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID",
							DestinationUtils.PYGWHANA);
					String userpass = username + ":" + password;
					if (debug) {
						response.getWriter().println("oDataURL:" + oDataURL);
						response.getWriter().println("aggregatorID:" + aggregatorID);
					}
					JsonObject sccnfObj = getSCCNFGRecords(oDataURL, userpass, aggregatorID, response, cpType, debug);
					if (sccnfObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (sccnfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results")
								.getAsJsonArray().size() > 0) {
							sccnfObj = sccnfObj.get("Message").getAsJsonObject().get("d").getAsJsonObject()
									.get("results").getAsJsonArray().get(0).getAsJsonObject();
							if (!sccnfObj.get("CORPID").isJsonNull()
									&& !sccnfObj.get("CORPID").getAsString().equalsIgnoreCase("")) {
								String corpId = sccnfObj.get("CORPID").getAsString();
								Map<String, String> scfOfferMap = scfOfferClient.callSCFOffersWebservice(request,
										response, corpId, cpGuid, debug);
								JsonObject scfObj = new JsonObject();
								scfOfferMap.forEach((key, value) -> scfObj.addProperty((String) key, value.toString()));
								/*
								 * resObj.add("ScfofferResponse", scfObj); resObj.addProperty("Status",
								 * "000001"); resObj.addProperty("ErrorCode", "");
								 */
								response.getWriter().println(scfObj);
							} else {
								resObj.addProperty("Message",
										"CORPID doesn't exist for the AggregatorId:" + aggregatorID);
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								response.getWriter().println(resObj);
							}
						} else {
							resObj.addProperty("Message",
									"Records doesn't exist in the SCCNFG table for the AggregatorId:" + aggregatorID);
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						}
					} else {
						response.getWriter().println(sccnfObj);
					}
				} else {
					resObj.addProperty("Message", "CPTypeID missing in the input payload");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}
			} else {
				resObj.addProperty("Message", "CPGuid missing in the input payload");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J003");
			resObj.addProperty("ExceptionTrace", buffer.toString());
			response.getWriter().println(resObj);
		}
	}

	private JsonObject getSCCNFGRecords(String OdataUrl, String userpass, String aggregatorID,
			HttpServletResponse response, String cpType, boolean debug) {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		try {
			String executeURL = OdataUrl + "SCCNFG?$filter=AGGRID%20eq%20%27" + aggregatorID
					+ "%27%20and%20CP_TYPE%20eq%20%27" + cpType + "%27";
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			JsonObject sccnfgObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("sccnfgObj:" + sccnfgObj);
			}
			return sccnfgObj;
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Message", ex.getLocalizedMessage() + "");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			return resObj;
		}
	}
}
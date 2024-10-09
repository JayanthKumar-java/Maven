package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ApprovalRequest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String inputPayload = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonParser parser = new JsonParser();
		boolean debug = false;
		JSONObject appLogObj = new JSONObject();
		JSONObject appMessageLog = new JSONObject();
		String uniqueId = "";
		String errorMessage = "",cpGUID="";
		ODataLogs odataLogs = new ODataLogs();
		
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Received input payload:" + jsonPayload);
				}
				
				if (jsonPayload.has("ProcessReference1")) {
					if (jsonPayload.get("ProcessReference1").isJsonNull()
							|| jsonPayload.get("ProcessReference1").getAsString().equalsIgnoreCase("")) {
						errorMessage = "ProcessReference1 missing in the input payload";
					}
				} else {
						errorMessage = "ProcessReference1 missing in the input payload";
				}
				
				if (jsonPayload.has("ProcessReference7")) {
					if (jsonPayload.get("ProcessReference7").isJsonNull()
							|| jsonPayload.get("ProcessReference7").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference7 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference7 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference7 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference7 missing in the input payload";
				}

				if (jsonPayload.has("ProcessReference6")) {
					if (jsonPayload.get("ProcessReference6").isJsonNull()
							|| jsonPayload.get("ProcessReference6").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference6 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference6 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference6 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference6 missing in the input payload";
				}	

				if (jsonPayload.has("AggregatorID")) {
					if (jsonPayload.get("AggregatorID").isJsonNull()
							|| jsonPayload.get("AggregatorID").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "AggregatorID missing in the input payload";
						else
							errorMessage = errorMessage + ",AggregatorID missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "AggregatorID missing in the input payload";
					else
						errorMessage = errorMessage + ",AggregatorID missing in the input payload";
				}	
				
				/*if (jsonPayload.has("CPGUID")) {
					if (jsonPayload.get("CPGUID").isJsonNull()
							|| jsonPayload.get("CPGUID").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "CPGUID missing in the input payload";
						else
							errorMessage = errorMessage + ",CPGUID missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "CPGUID missing in the input payload";
					else
						errorMessage = errorMessage + ",CPGUID missing in the input payload";
				}	
				*/
				if (jsonPayload.has("ID")) {
					if (jsonPayload.get("ID").isJsonNull()
							|| jsonPayload.get("ID").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ID missing in the input payload";
						else
							errorMessage = errorMessage + ",ID missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ID missing in the input payload";
					else
						errorMessage = errorMessage + ",ID missing in the input payload";
				}	
				
				if (errorMessage.equalsIgnoreCase("")) {
					String oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
					String userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
					String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
					String userPass = userName + ":" + password;
					uniqueId = commonUtils.generateGUID(36);
					// String loginId = commonUtils.getLoginID(request,
					// response, debug);
					String loginId = commonUtils.getUserPrincipal(request, "name", response);
					if(jsonPayload.has("CPGUID") && !jsonPayload.get("CPGUID").isJsonNull()){
						cpGUID=jsonPayload.get("CPGUID").getAsString();
					}
					JsonObject applicationLog = odataLogs.insertApprovalRequestLogs(request, response, "Java",
							"Approval Request", "", "", request.getServletPath(), oDataUrl, userPass,
							jsonPayload.get("AggregatorID").getAsString(), loginId,
							jsonPayload.get("ProcessReference1").getAsString(),
							jsonPayload.get("ProcessReference6").getAsString(),
							jsonPayload.get("ProcessReference7").getAsString(), 
							cpGUID,
							debug);
					if (debug) {
						response.getWriter().println("applicationLog :" + applicationLog);
					}
					if (!applicationLog.has("error") && !applicationLog.has("ErrorCode")) {
						// delete the Record based on Id.
						String executeURL = oDataUrl + "Approval('" + jsonPayload.get("ID").getAsString() + "')";
						JsonObject deleteObj = commonUtils.executeDelete(executeURL, userPass, response, request,
								debug, "PCGWHANA");
						if (deleteObj.has("ErrorCode")
								&& deleteObj.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
							JsonObject resObj = new JsonObject();
							resObj.addProperty("Message", "Record deleted successfully");
							resObj.addProperty("Status", "000001");
							resObj.addProperty("ErrorCode", "");
							response.getWriter().println(resObj);
						} else {
							JsonObject resObj = new JsonObject();
							resObj.addProperty("Message", "Records not deleted");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						}
					} else {
						// delete the Application log entry
						JsonObject resObj = new JsonObject();
						resObj.addProperty("Message", "Unable to insert a Application log");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(resObj);
					}

				} else {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", errorMessage);
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}

			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Empty Input Payload Received");
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
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);

		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String processReference7 = "", processReference6 = "";
		String errorMessage = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		boolean debug = false;
		String StatusId = "",aggregatorId="",processNo3="";
		try {
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName + ":" + password;
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("Servlet Path:" + request.getServletPath());
			}
			if (request.getServletPath().equalsIgnoreCase("/ApprovalRequest")) {
				if (request.getParameter("ProcessReference7") != null
						&& !request.getParameter("ProcessReference7").equalsIgnoreCase("")) {
					processReference7 = request.getParameter("ProcessReference7");
				}
				if (request.getParameter("ProcessReference6") != null
						&& !request.getParameter("ProcessReference6").equalsIgnoreCase("")) {
					processReference6 = request.getParameter("ProcessReference6");
				}
				if (processReference7.equalsIgnoreCase("")) {
					errorMessage = "ProcessReference7 Field Empty in the Input Payload";
				}

				if (processReference6.equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "ProcessReference6 Field Empty in the Input Payload";
					} else {
						errorMessage = errorMessage + ",ProcessReference6 Field Empty in the Input Payload";
					}
				}

				if (debug) {
					response.getWriter()
							.println("Received Input Payload: " + processReference7 + "," + processReference6);
				}

			} else {
				if (request.getParameter("StatusID") != null
						&& !request.getParameter("StatusID").equalsIgnoreCase("")) {
					StatusId = request.getParameter("StatusID");
				}
				if (StatusId.equalsIgnoreCase("")) {
					errorMessage = "StatusID Missing in the input Payload";
				}
				if (request.getParameter("AggregatorID") != null
						&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
					aggregatorId=request.getParameter("AggregatorID");
				}
				
				if (request.getParameter("ProcessReference3") != null
						&& !request.getParameter("ProcessReference3").equalsIgnoreCase("")) {
					processNo3=request.getParameter("ProcessReference3");
				}
			}

			if (errorMessage.equalsIgnoreCase("")) {
				if (debug) {
					response.getWriter().println("Execute URL:" + executeURL);
				}
				if (request.getServletPath().equalsIgnoreCase("/ApprovalRequest")) {
					executeURL = oDataUrl + "Approval?$filter=ProcessReference7%20eq%20%27" + processReference7
							+ "%27%20and%20ProcessReference6%20eq%20%27" + processReference6 + "%27";
				} else {
					executeURL=oDataUrl + "Approval?$filter=StatusID%20eq%20%27" + StatusId+"%27";
					if(aggregatorId!=null && !aggregatorId.equalsIgnoreCase("")){
						executeURL=executeURL+"%20and%20AggregatorID%20eq%20%27"+aggregatorId+"%27";
					}
					if(processNo3!=null && !processNo3.equalsIgnoreCase("")){
						executeURL=executeURL+"%20and%20ProcessReference3%20eq%20%27"+processNo3+"%27";
					}
					
					executeURL = executeURL+"%20and%20(ProcessReference7%20eq%20%27" + "DELETE"
							+ "%27%20or%20ProcessReference7%20eq%20%27" + "RESET"
							+ "%27%20or%20ProcessReference7%20eq%20%27" + "DEREGISTER"
							+ "%27%20or%20ProcessReference7%20eq%20%27" + "UPDATE" + "%27)";
					/*executeURL = oDataUrl + "Approval?$filter=StatusID%20eq%20%27" + StatusId
							+ "%27%20and%20(ProcessReference7%20eq%20%27" + "DELETE"
							+ "%27%20or%20ProcessReference7%20eq%20%27" + "RESET"
							+ "%27%20or%20ProcessReference7%20eq%20%27" + "DEREGISTER" 
							+"%27%20or%20ProcessReference7%20eq%20%27" + "UPDATE"+ "%27)";*/
				}
				JsonObject approvalObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("approvalObj: " + approvalObj);
				}
				if (approvalObj != null && !approvalObj.isJsonNull()
						&& approvalObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					resObj.add("Message", approvalObj.get("d").getAsJsonObject().get("results").getAsJsonArray());
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");

				} else {
					// print no record exist
					resObj.addProperty("Message", "Records Not Exist");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
				}

			} else {
				// print validation error Message
				resObj.addProperty("Message", errorMessage);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");

		} finally {
			response.getWriter().println(resObj);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		String errorMessage = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		boolean debug = false;
		JSONObject insertObj = new JSONObject();
		JsonObject resObj = new JsonObject();
		try {
			inputPayload = commonUtils.getGetBody(request, response);

			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonPayload = (JsonObject) parser.parse(inputPayload);
				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					jsonPayload.remove("debug");
					debug = true;
				}
				if (debug) {
					response.getWriter().println("Received Input Payload: " + jsonPayload);
				}
				if (jsonPayload.has("AggregatorID")) {
					if (jsonPayload.get("AggregatorID").isJsonNull()
							|| jsonPayload.get("AggregatorID").getAsString().equalsIgnoreCase("")) {
						errorMessage = "AggregatorID missing in the input payload";
					}
				} else {
					errorMessage = "AggregatorID missing in the input payload";
				}
				if (jsonPayload.has("ProcessID")) {
					if (jsonPayload.get("ProcessID").isJsonNull()
							|| jsonPayload.get("ProcessID").getAsString().equalsIgnoreCase("")) {

						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessID missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessID missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessID missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessID missing in the input payload";

				}
				if (jsonPayload.has("ProcessReference3")) {
					if (jsonPayload.get("ProcessReference3").isJsonNull()
							|| jsonPayload.get("ProcessReference3").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference3 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference3 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference3 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference3 missing in the input payload";
				}
				if (jsonPayload.has("ProcessReference1")) {
					if (jsonPayload.get("ProcessReference1").isJsonNull()
							|| jsonPayload.get("ProcessReference1").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference1 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference1 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference1 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference1 missing in the input payload";
				}
				
				if (jsonPayload.has("ProcessReference4")) {
					if (jsonPayload.get("ProcessReference4").isJsonNull()
							|| jsonPayload.get("ProcessReference4").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference4 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference4 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference4 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference4 missing in the input payload";
				}
				
				if (jsonPayload.has("ProcessReference6")) {
					if (jsonPayload.get("ProcessReference6").isJsonNull()
							|| jsonPayload.get("ProcessReference6").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference6 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference6 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference6 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference6 missing in the input payload";
				}
				
				if (jsonPayload.has("ProcessReference7")) {
					if (jsonPayload.get("ProcessReference7").isJsonNull()
							|| jsonPayload.get("ProcessReference7").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "ProcessReference7 missing in the input payload";
						else
							errorMessage = errorMessage + ",ProcessReference7 missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "ProcessReference7 missing in the input payload";
					else
						errorMessage = errorMessage + ",ProcessReference7 missing in the input payload";
				}
				
				
				if (jsonPayload.has("StatusID")) {
					if (jsonPayload.get("StatusID").isJsonNull()
							|| jsonPayload.get("StatusID").getAsString().equalsIgnoreCase("")) {
						if (errorMessage.equalsIgnoreCase(""))
							errorMessage = "StatusID missing in the input payload";
						else
							errorMessage = errorMessage + ",StatusID missing in the input payload";
					}
				} else {
					if (errorMessage.equalsIgnoreCase(""))
						errorMessage = "StatusID missing in the input payload";
					else
						errorMessage = errorMessage + ",StatusID missing in the input payload";
				}
				if (errorMessage.equalsIgnoreCase("")) {
					String createdBy = commonUtils.getUserPrincipal(request, "name", response);
					String createdAt = commonUtils.getCreatedAtTime();
					long createdOnInMillis = commonUtils.getCreatedOnDate();
					String id = commonUtils.generateGUID(36);
					oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
					userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
					password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
					userPass = userName + ":" + password;

					insertObj.accumulate("ID", id);
					insertObj.accumulate("CreatedBy", createdBy);
					insertObj.accumulate("CreatedAt", createdAt);
					insertObj.accumulate("CreatedOn", "/Date(" + createdOnInMillis + ")/");
					insertObj.accumulate("AggregatorID", jsonPayload.get("AggregatorID").getAsString());
					insertObj.accumulate("ProcessID", jsonPayload.get("ProcessID").getAsString());
					insertObj.accumulate("ProcessReference3", jsonPayload.get("ProcessReference3").getAsString());
					insertObj.accumulate("ProcessReference4", jsonPayload.get("ProcessReference4").getAsString());
					insertObj.accumulate("StatusID", jsonPayload.get("StatusID").getAsString());
					insertObj.accumulate("ProcessReference7", jsonPayload.get("ProcessReference7").getAsString());
					insertObj.accumulate("ProcessReference6", jsonPayload.get("ProcessReference6").getAsString());
					insertObj.accumulate("ProcessReference1", jsonPayload.get("ProcessReference1").getAsString());

					if (jsonPayload.has("ProcessReference9") && !jsonPayload.get("ProcessReference9").isJsonNull()) {
						insertObj.accumulate("ProcessReference9", jsonPayload.get("ProcessReference9").getAsString());
					} else {
						insertObj.accumulate("ProcessReference9", "");
					}
					if (jsonPayload.has("ProcessReference5") && !jsonPayload.get("ProcessReference5").isJsonNull()) {
						insertObj.accumulate("ProcessReference5", jsonPayload.get("ProcessReference5").getAsString());
					} else {
						insertObj.accumulate("ProcessReference5", "");
					}
					
					if (jsonPayload.has("Remarks") && !jsonPayload.get("Remarks").isJsonNull()) {
						insertObj.accumulate("Remarks", jsonPayload.get("Remarks").getAsString());
					} else {
						insertObj.accumulate("Remarks", "");
					}
					if (jsonPayload.has("ProcessReference2") && !jsonPayload.get("ProcessReference2").isJsonNull()) {
						insertObj.accumulate("ProcessReference2", jsonPayload.get("ProcessReference2").getAsString());
					} else {
						insertObj.accumulate("ProcessReference2", "");
					}
				  
					if (jsonPayload.has("ProcessReference8") && !jsonPayload.get("ProcessReference8").isJsonNull()) {
						insertObj.accumulate("ProcessReference8", jsonPayload.get("ProcessReference8").getAsString());
					} else {
						insertObj.accumulate("ProcessReference8", "");
					}
						

					if (jsonPayload.has("ProcessReference10") && !jsonPayload.get("ProcessReference10").isJsonNull()) {
						insertObj.accumulate("ProcessReference10", jsonPayload.get("ProcessReference10").getAsString());
					} else {
						insertObj.accumulate("ProcessReference10", "");
					}

					if (jsonPayload.has("Source") && !jsonPayload.get("Source").isJsonNull()) {
						insertObj.accumulate("Source", jsonPayload.get("Source").getAsString());
					} else {
						insertObj.accumulate("Source", "");
					}
					if (jsonPayload.has("SourceReferenceID") && !jsonPayload.get("SourceReferenceID").isJsonNull()) {
						insertObj.accumulate("SourceReferenceID", jsonPayload.get("SourceReferenceID").getAsString());
					} else {
						insertObj.accumulate("SourceReferenceID", "");
					}
					executeURL = oDataUrl + "Approval";
					if (debug) {
						response.getWriter().println("Execute Url:" + executeURL);
						response.getWriter().println("Insert Payload:" + insertObj);
					}
					JsonObject apprvlResponse = commonUtils.executePostURL(executeURL, userPass, response, insertObj,
							request, debug, "PCGWHANA");
					if (debug)
						response.getWriter().println("Insert into Approval Obj Response" + apprvlResponse);
					if (apprvlResponse.has("error")) {
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Insertion Failed");
						resObj.addProperty("Status", "000002");

					} else {
						resObj.addProperty("ErrorCode", "");
						resObj.addProperty("Message", "Record Inserted Successfully");
						resObj.addProperty("Status", "000001");
					}
				} else {
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", errorMessage);
					resObj.addProperty("Status", "000002");
				}
			} else {
				// print input Payload not exist
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Message", "Input Payload is Empty");
				resObj.addProperty("Status", "000002");
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000001");

		} finally {
			response.getWriter().println(resObj);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		JSONObject insertObj = new JSONObject();
		String executeURL = "", oDataUrl = "", userName = "", password = "", userPass = "";
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			userPass = userName + ":" + password;
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				
				JsonObject jsonPayload = (JsonObject) parser.parse(inputPayload);
				if(jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")){
					debug=true;
				}
				if(debug){
					response.getWriter().println("Received Inputpayload:"+jsonPayload);
				}
				if (jsonPayload.has("ID") && !jsonPayload.get("ID").isJsonNull()
						&& !jsonPayload.get("ID").getAsString().equalsIgnoreCase("")) {
					insertObj.accumulate("ID", jsonPayload.get("ID").getAsString());
					if (jsonPayload.has("AggregatorID") && !jsonPayload.get("AggregatorID").isJsonNull()) {
						insertObj.accumulate("AggregatorID", jsonPayload.get("AggregatorID").getAsString());
					}
					if (jsonPayload.has("ProcessID") && !jsonPayload.get("ProcessID").isJsonNull()) {
						insertObj.accumulate("ProcessID", jsonPayload.get("ProcessID").getAsString());
					}

					if (jsonPayload.has("Remarks") && !jsonPayload.get("Remarks").isJsonNull()) {
						insertObj.accumulate("Remarks", jsonPayload.get("Remarks").getAsString());
					}

					if (jsonPayload.has("StatusID") && !jsonPayload.get("StatusID").isJsonNull()) {
						insertObj.accumulate("StatusID", jsonPayload.get("StatusID").getAsString());
					}

					if (jsonPayload.has("ProcessReference1") && !jsonPayload.get("ProcessReference1").isJsonNull()) {
						insertObj.accumulate("ProcessReference1", jsonPayload.get("ProcessReference1").getAsString());
					}

					if (jsonPayload.has("ProcessReference2") && !jsonPayload.get("ProcessReference2").isJsonNull()) {
						insertObj.accumulate("ProcessReference2", jsonPayload.get("ProcessReference2").getAsString());
					}

					if (jsonPayload.has("ProcessReference3") && !jsonPayload.get("ProcessReference3").isJsonNull()) {
						insertObj.accumulate("ProcessReference3", jsonPayload.get("ProcessReference3").getAsString());
					}

					if (jsonPayload.has("ProcessReference4") && !jsonPayload.get("ProcessReference4").isJsonNull()) {
						insertObj.accumulate("ProcessReference4", jsonPayload.get("ProcessReference4").getAsString());
					}
					if (jsonPayload.has("ProcessReference5") && !jsonPayload.get("ProcessReference5").isJsonNull()) {
						insertObj.accumulate("ProcessReference5", jsonPayload.get("ProcessReference5").getAsString());
					}

					if (jsonPayload.has("ProcessReference6") && !jsonPayload.get("ProcessReference6").isJsonNull()) {
						insertObj.accumulate("ProcessReference6", jsonPayload.get("ProcessReference6").getAsString());
					}

					if (jsonPayload.has("ProcessReference7") && !jsonPayload.get("ProcessReference7").isJsonNull()) {
						insertObj.accumulate("ProcessReference7", jsonPayload.get("ProcessReference7").getAsString());
					}
					if (jsonPayload.has("ProcessReference8") && !jsonPayload.get("ProcessReference8").isJsonNull()) {
						insertObj.accumulate("ProcessReference8", jsonPayload.get("ProcessReference8").getAsString());
					}

					if (jsonPayload.has("ProcessReference9") && !jsonPayload.get("ProcessReference9").isJsonNull()) {
						insertObj.accumulate("ProcessReference9", jsonPayload.get("ProcessReference9").getAsString());
					}

					if (jsonPayload.has("ProcessReference10") && !jsonPayload.get("ProcessReference10").isJsonNull()) {
						insertObj.accumulate("ProcessReference10", jsonPayload.get("ProcessReference10").getAsString());
					}

					if (jsonPayload.has("CreatedBy") && !jsonPayload.get("CreatedBy").isJsonNull()) {
						insertObj.accumulate("CreatedBy", jsonPayload.get("CreatedBy").getAsString());
					}

					if (jsonPayload.has("CreatedAt") && !jsonPayload.get("CreatedAt").isJsonNull()) {
						insertObj.accumulate("CreatedAt", jsonPayload.get("CreatedAt").getAsString());
					}

					if (jsonPayload.has("CreatedOn") && !jsonPayload.get("CreatedOn").isJsonNull()) {
						insertObj.accumulate("CreatedOn", jsonPayload.get("CreatedOn").getAsString());
					}
					if (jsonPayload.has("Source") && !jsonPayload.get("Source").isJsonNull()) {
						insertObj.accumulate("Source", jsonPayload.get("Source").getAsString());
					}
					if (jsonPayload.has("SourceReferenceID") && !jsonPayload.get("SourceReferenceID").isJsonNull()) {
						insertObj.accumulate("SourceReferenceID", jsonPayload.get("SourceReferenceID").getAsString());
					}
					long changedOn = commonUtils.getCreatedOnDate();
					String changedAt = commonUtils.getCreatedAtTime();
					String changedBy = commonUtils.getUserPrincipal(request, "name", response);
					insertObj.accumulate("ChangedBy", changedBy);
					insertObj.accumulate("ChangedAt", changedAt);
					insertObj.accumulate("ChangedOn", "/Date(" + changedOn + ")/");
					executeURL = oDataUrl + "Approval('" + jsonPayload.get("ID").getAsString() + "')";
					if (debug) {
						response.getWriter().println("Update URL:" + executeURL);
						response.getWriter().println("Inserted Payload:" + insertObj);
					}
					JsonObject executeUpdate = commonUtils.executeUpdate(executeURL, userPass, response, insertObj,
							request, debug, DestinationUtils.PCGWHANA);
					if(debug){
						response.getWriter().println("executeUpdate:"+executeUpdate);
					}
					if (executeUpdate != null && executeUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
						JsonObject resObj = new JsonObject();
						resObj.addProperty("Message", "Records Updated Successfully");
						resObj.addProperty("Status", "000001");
						resObj.addProperty("ErrorCode", "");
						response.getWriter().println(resObj);
					} else {
						JsonObject resObj = new JsonObject();
						resObj.addProperty("Message", "Records not Updated Successfully");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(resObj);
					}

				} else {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", "Mandatory Field Id Missing in the Payload ");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}

			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Empty Inputpayload Received");
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
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(resObj);
		}

	}

}

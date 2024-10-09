package com.arteriatech.logs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;

public class ODataLogs {

	/*public String insertIntoCPIMsgLogs(HttpServletRequest request, HttpServletResponse response, String oDataURL, String userPass, 
			String aggregatorID, String loginID, String servletPath, Properties properties, boolean debug) throws IOException{
		CommonUtils commonUtils = new CommonUtils();
		JSONObject insertPayload = new JSONObject();
		
		String uniqueID="";
		try{
			uniqueID = commonUtils.generateGUID(36);
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("SenderID", servletPath);
			insertPayload.accumulate("MessageType", "PGPaymentStsUpd");
			insertPayload.accumulate("RecordCount", "1");
		}catch (Exception e) {
			// TODO: handle exception
		}
	}*/
	
	/*public JsonObject preparePayloadForCPIMsgLogs(HttpServletRequest request, HttpServletResponse response, String oDataURL, String userPass, 
			String aggregatorID, Properties properties, String loginID, String servletPath, boolean debug) throws IOException{
		JsonObject returnPayload = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			returnPayload.addProperty("ID", uniqueID);
			returnPayload.addProperty("MessageType", "PGPaymentStsUpd");
			returnPayload.addProperty("SenderID", servletPath);
			returnPayload.addProperty("RecordCount", "1");
			
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertExceptionLogs(request, response, buffer.toString(), properties.getProperty("PushAPI"), oDataURL, userPass, aggregatorID, loginID, debug);
			
			returnPayload = null;
		}
	}*/
	
	public String insertExceptionLogs(HttpServletRequest request, HttpServletResponse response, String exceptionTrace, String subObject, 
			String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		JsonObject responseObject = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertExceptionInAppLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertExceptionInAppLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertExceptionInAppLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertExceptionInAppLogs createdAt: "+createdAt);
				response.getWriter().println("insertExceptionInAppLogs subObject: "+subObject);
				response.getWriter().println("insertExceptionInAppLogs executeURL: "+executeURL);
			}
						
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", "Java");
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate", "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertExceptionInAppLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String truncatedExceptionTrace="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(exceptionTrace.length() > 1000)
					truncatedExceptionTrace = exceptionTrace.substring(0, 999);
				else
					truncatedExceptionTrace = truncatedExceptionTrace+exceptionTrace;
				
				if(debug)
					response.getWriter().println("insertExceptionInAppLogs truncatedExceptionTrace: "+truncatedExceptionTrace);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "E");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", truncatedExceptionTrace);
				
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
		}catch (Exception e) {
			uniqueID = "";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception in ODataLogs - insertExceptionLogs: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
		}
		return uniqueID;
	}
	
	public String insertMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
		String msgID, String logMessage, int stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
		String errorMessage="";
		JsonObject responseObject = new JsonObject();
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="";
		long createdOnInMillis=0;
		
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", "");
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			String exceptionMsg="";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			exceptionMsg = buffer.toString();
			stepNo = stepNo+1;
			insertExceptionMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
			
			return "";
		}
	}
	
	public String insertExceptionMsg(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, int stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
		String errorMessage="";
		JsonObject responseObject = new JsonObject();
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="";
		long createdOnInMillis=0;
		
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
//			stepNo = stepNo+1;
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertExceptionMsg logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", "");
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			return "";
		}
	}
	
	public String insertApplicationLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			//responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug);
			
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertExceptionLogs(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			
			return "";
		}
	}
	
	public String insertDirectDebitMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, String message1, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
			String errorMessage="";
			JsonObject responseObject = new JsonObject();
			JSONObject insertPayload = new JSONObject();
			String executeURL = "", uniqueID="", createdAt="",createdBy="";
			long createdOnInMillis=0;
			
			CommonUtils commonUtils = new CommonUtils();
			try{
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				uniqueID = commonUtils.generateGUID(36);
				executeURL = oDataURL+"ApplicationLogMessages";
				if(logMessage.length() > 1000)
					errorMessage = logMessage.substring(0, 999);
				else
					errorMessage = logMessage;
				
				if(debug)
					response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
				
				insertPayload.accumulate("ID", uniqueID);
				insertPayload.accumulate("ALogHID", appLogID);
				insertPayload.accumulate("MessageType", msgType);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("MessageID", msgID);
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", createdBy);
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				return "";
			}catch (Exception e) {
				String exceptionMsg="";
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				
				if(debug)
					response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
				
				exceptionMsg = buffer.toString();
				int stepNo=0;
				insertExceptionMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
				
				return "";
			}
		}
	public String insertEscrowS2SAckApplicationLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad,String message1, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertExceptionLogs(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			
			return "";
		}
	}
	public String insertEscrowS2SAckMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage,int stepNo, String message1, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
			String errorMessage="";
			JsonObject responseObject = new JsonObject();
			JSONObject insertPayload = new JSONObject();
			String executeURL = "", uniqueID="", createdAt="",createdBy="";
			long createdOnInMillis=0;
			
			CommonUtils commonUtils = new CommonUtils();
			try{
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				uniqueID = commonUtils.generateGUID(36);
				executeURL = oDataURL+"ApplicationLogMessages";
				if(logMessage.length() > 1000)
					errorMessage = logMessage.substring(0, 999);
				else
					errorMessage = logMessage;
				
				if(debug)
					response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
				
				insertPayload.accumulate("ID", uniqueID);
				insertPayload.accumulate("ALogHID", appLogID);
				insertPayload.accumulate("MessageType", msgType);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("MessageID", msgID);
				insertPayload.accumulate("MessageNo", ""+stepNo);
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", createdBy);
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				return "";
			}catch (Exception e) {
				String exceptionMsg="";
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				
				if(debug)
					response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
				
				exceptionMsg = buffer.toString();
				insertExceptionMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
				return "";
			}
		}
	
	public String insertApplicationLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad,String message1, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertExceptionLogs(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			
			return "";
		}
	}
	
	public String insertMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage,int stepNo, String message1, String oDataURL, String userPass, String aggregatorID,String message2,String message3,String message4, boolean debug) throws IOException{
			String errorMessage="";
			JSONObject insertPayload = new JSONObject();
			String executeURL = "", uniqueID="", createdAt="",createdBy="";
			long createdOnInMillis=0;
			JsonObject responseObject=new JsonObject();
			CommonUtils commonUtils = new CommonUtils();
			try{
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				uniqueID = commonUtils.generateGUID(36);
				executeURL = oDataURL+"ApplicationLogMessages";
				if(logMessage.length() > 1000)
					errorMessage = logMessage.substring(0, 999);
				else
					errorMessage = logMessage;
				
				if(debug)
					response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
				
				insertPayload.accumulate("ID", uniqueID);
				insertPayload.accumulate("ALogHID", appLogID);
				insertPayload.accumulate("MessageType", msgType);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("Message2", message2);
				insertPayload.accumulate("Message3", message3);
				insertPayload.accumulate("Message4", message4);
				insertPayload.accumulate("MessageID", msgID);
				insertPayload.accumulate("MessageNo", ""+stepNo);
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", createdBy);
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				return "";
			}catch (Exception e) {
				String exceptionMsg="";
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				
				if(debug)
					response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
				
				exceptionMsg = buffer.toString();
				insertExceptionDownloadSalesMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
				return "";
			}
		}
	
	public String insertApplicationLogsDownLoadReport(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			//responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug);
			
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				//responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug);
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertExceptionDownloadsales(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			
			return "";
		}
	}
	
	
	public String insertExceptionDownloadsales(HttpServletRequest request, HttpServletResponse response, String exceptionTrace, String subObject, 
			String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		JsonObject responseObject = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertExceptionInAppLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertExceptionInAppLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertExceptionInAppLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertExceptionInAppLogs createdAt: "+createdAt);
				response.getWriter().println("insertExceptionInAppLogs subObject: "+subObject);
				response.getWriter().println("insertExceptionInAppLogs executeURL: "+executeURL);
			}
						
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", "Java");
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate", "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			//responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug);
			
			if(debug)
				response.getWriter().println("insertExceptionInAppLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String truncatedExceptionTrace="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(exceptionTrace.length() > 1000)
					truncatedExceptionTrace = exceptionTrace.substring(0, 999);
				else
					truncatedExceptionTrace = truncatedExceptionTrace+exceptionTrace;
				
				if(debug)
					response.getWriter().println("insertExceptionInAppLogs truncatedExceptionTrace: "+truncatedExceptionTrace);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "E");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", truncatedExceptionTrace);
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
		}catch (Exception e) {
			uniqueID = "";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception in ODataLogs - insertExceptionLogs: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
		}
		return uniqueID;
	}
	
	public String insertExceptionDownloadSalesMsg(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, int stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
		String errorMessage="";
		JsonObject responseObject = new JsonObject();
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="";
		long createdOnInMillis=0;
		
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
//			stepNo = stepNo+1;
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertExceptionMsg logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", "");
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			return "";
		}
	}
	
	public String insertSendSalesReportApplicationLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID,String spGuid, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			insertPayload.accumulate("ProcessRef1", spGuid);
			//responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug);
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug,"PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug,"PCGWHANA");
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertQuartelyReposrtExceptionLogs(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			//insertExceptionDownloadSalesMsg(request, response, appLogID, msgType, msgID, logMessage, stepNo, oDataURL, userPass, aggregatorID, debug)
			return "";
		}
	}
	
	public void updateCpiApplicationLog(HttpServletRequest request,HttpServletResponse response,String odataUrl,String userPass,String cpiLogId,String applogId,String errorMessage,String messageType,String aggrId,String accountId,boolean debug)throws IOException,Exception{
		String executeURL="";
		CommonUtils commonUtils=new CommonUtils();
		JSONObject updatePayLoad=new JSONObject();
		try{
			executeURL=odataUrl+"ApplicationLogMessages?$filter=ALogHID%20eq%20%27"+cpiLogId+"%27";
			if(debug){
				response.getWriter().println("updateCpiApplicationLog ApplicationLogMessages URl:"+executeURL);
			}
			JsonObject applicationLogObj = commonUtils.executeURL(executeURL, userPass, response);
			if(applicationLogObj!=null && !applicationLogObj.isJsonNull()&& applicationLogObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
				JsonObject appObj = applicationLogObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
				updatePayLoad.accumulate("ID",appObj.get("ID").getAsString());
				if(appObj.has("ALogHID") && !appObj.get("ALogHID").isJsonNull() &&!appObj.get("ALogHID").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("ALogHID",appObj.get("ALogHID").getAsString());
				}else{
					updatePayLoad.accumulate("ALogHID","");
				}

				if(appObj.has("MessageType") && !appObj.get("MessageType").isJsonNull() &&!appObj.get("MessageType").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("MessageType",appObj.get("MessageType").getAsString());
				}else{
					updatePayLoad.accumulate("MessageType","");
				}

				if(appObj.has("MessageID") && !appObj.get("MessageID").isJsonNull() &&!appObj.get("MessageID").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("MessageID",appObj.get("MessageID").getAsString());
				}else{
					updatePayLoad.accumulate("MessageID","");
				}

				if(appObj.has("MessageNo") && !appObj.get("MessageNo").isJsonNull() &&!appObj.get("MessageNo").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("MessageNo",appObj.get("MessageNo").getAsString());
				}else{
					updatePayLoad.accumulate("MessageNo","");
				}
				
				if(appObj.has("Message1") && !appObj.get("Message1").isJsonNull() &&!appObj.get("Message1").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("Message1",appObj.get("Message1").getAsString());
				}else{
					updatePayLoad.accumulate("Message1","");
				}
				
				if(appObj.has("Message2") && !appObj.get("Message2").isJsonNull() &&!appObj.get("Message2").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("Message2",appObj.get("Message2").getAsString());
				}else{
					updatePayLoad.accumulate("Message2","");
				}

				if(appObj.has("Message3") && !appObj.get("Message3").isJsonNull() &&!appObj.get("Message3").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("Message3",appObj.get("Message3").getAsString());
				}else{
					updatePayLoad.accumulate("Message3","");
				}

				if(appObj.has("Message4") && !appObj.get("Message4").isJsonNull() &&!appObj.get("Message4").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("Message4",appObj.get("Message4").getAsString());
				}else{
					updatePayLoad.accumulate("Message4","");
				}

				if(appObj.has("UserMessage") && !appObj.get("UserMessage").isJsonNull() &&!appObj.get("UserMessage").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("UserMessage",appObj.get("UserMessage").getAsString());
				}else{
					updatePayLoad.accumulate("UserMessage","");
				}
				
				if(appObj.has("ErrorCode") && !appObj.get("ErrorCode").isJsonNull() &&!appObj.get("ErrorCode").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("ErrorCode",appObj.get("ErrorCode").getAsString());
				}else{
					updatePayLoad.accumulate("ErrorCode","");
				}
				
				if(appObj.has("TimeStamp") && !appObj.get("TimeStamp").isJsonNull() &&!appObj.get("TimeStamp").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("TimeStamp",appObj.get("TimeStamp").getAsString());
				}else{
					updatePayLoad.accumulate("TimeStamp",JSONObject.NULL);
				}

				if(appObj.has("CreatedBy") && !appObj.get("CreatedBy").isJsonNull() &&!appObj.get("CreatedBy").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("CreatedBy",appObj.get("CreatedBy").getAsString());
				}else{
					updatePayLoad.accumulate("CreatedBy","");
				}

				if(appObj.has("CreatedAt") && !appObj.get("CreatedAt").isJsonNull() &&!appObj.get("CreatedAt").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("CreatedAt",appObj.get("CreatedAt").getAsString());
				}else{
					updatePayLoad.accumulate("CreatedAt",JSONObject.NULL);
				}

				if(appObj.has("CreatedOn") && !appObj.get("CreatedOn").isJsonNull() &&!appObj.get("CreatedOn").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("CreatedOn",appObj.get("CreatedOn").getAsString());
				}else{
					updatePayLoad.accumulate("CreatedOn",JSONObject.NULL);
				}

				if(appObj.has("Source") && !appObj.get("Source").isJsonNull() &&!appObj.get("Source").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("Source",appObj.get("Source").getAsString());
				}else{
					updatePayLoad.accumulate("Source","");
				}

				if(appObj.has("SourceReferenceID") && !appObj.get("SourceReferenceID").isJsonNull() &&!appObj.get("SourceReferenceID").getAsString().equalsIgnoreCase("")){
					updatePayLoad.accumulate("SourceReferenceID",appObj.get("SourceReferenceID").getAsString());
				}else{
					updatePayLoad.accumulate("SourceReferenceID","");
				}

				long createdOnInMillis = commonUtils.getCreatedOnDate();
				String createdAt = commonUtils.getCreatedAtTime();
				updatePayLoad.accumulate("ChangedAt",createdAt);
				updatePayLoad.accumulate("ChangedOn","/Date("+createdOnInMillis+")/");
				updatePayLoad.accumulate("ChangedBy",commonUtils.getUserPrincipal(request, "name", response));
				updatePayLoad.accumulate("MessageType",messageType);
				if(errorMessage!=null &&!errorMessage.equalsIgnoreCase("")){
					if(errorMessage.length() > 1000){
						errorMessage = errorMessage.substring(0, 999);
					}
					updatePayLoad.accumulate("ErrorMessage",errorMessage);
				}else{
					if(appObj.has("ErrorMessage") && !appObj.get("ErrorMessage").isJsonNull() &&!appObj.get("ErrorMessage").getAsString().equalsIgnoreCase("")){
						updatePayLoad.accumulate("ErrorMessage",appObj.get("ErrorMessage").getAsString());
					}else{
						updatePayLoad.accumulate("ErrorMessage","");
					}
				}
				if(debug){
					response.getWriter().println("ApplicationLogMessages Updated Paylaod:"+updatePayLoad);
				}
				executeURL=odataUrl+"ApplicationLogMessages('"+appObj.get("ID").getAsString()+"')";
				JsonObject executeUpdate = commonUtils.updateCPIApplicationLog(executeURL, userPass, response, updatePayLoad, request,accountId, debug, "PCGWHANA");
				if(executeUpdate.has("ErrorCode")&&!executeUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")){
					int stepNo=0;
					if (executeUpdate.has("ErrorMessage")) {
						String logMessage = executeUpdate.get("ErrorMessage").getAsString();
						if (logMessage.length() > 1000) {
							logMessage = logMessage.substring(0, 999);
						}
						insertSalesReportMessageForAppLogs(request, response, applogId, "I", "/ARTEC/PY", logMessage,
								stepNo, "Failed while Updating a CPI ApplicatioLogmessage Table", odataUrl, userPass, aggrId,
								"", "", "", debug);
						/*insertMessageForAppLogs(request, response, applogId, "E", "/ARTEC/PY", logMessage, "",
								odataUrl, userPass, aggrId, debug);*/
					}else{
						insertSalesReportMessageForAppLogs(request, response, applogId, "I", "/ARTEC/PY", "Updating a cpi application log failed",
								stepNo, "Failed while Updating a CPI ApplicatioLogmessage Table", odataUrl, userPass, aggrId,
								"", "", "", debug);
					}
					
				}
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			int stepNo = 1;
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			String logMessage = buffer.toString();
			if (logMessage.length() > 1000) {
				logMessage = logMessage.substring(0, 999);
			}
			insertSalesReportMessageForAppLogs(request, response, applogId, "E", "/ARTEC/PY", logMessage, stepNo,
					"Exception Occurred while Updating a CPI ApplicatioLogmessage Table", odataUrl, userPass, aggrId,
					"", "", "", debug);
		}
	}
	
	
	public String insertSalesReportMessageAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, String stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
			String errorMessage="";
			JsonObject responseObject = new JsonObject();
			JSONObject insertPayload = new JSONObject();
			String executeURL = "", uniqueID="", createdAt="";
			long createdOnInMillis=0;
			
			CommonUtils commonUtils = new CommonUtils();
			try{
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				uniqueID = commonUtils.generateGUID(36);
				executeURL = oDataURL+"ApplicationLogMessages";
				if(logMessage.length() > 1000)
					errorMessage = logMessage.substring(0, 999);
				else
					errorMessage = logMessage;
				
				if(debug)
					response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
				
				insertPayload.accumulate("ID", uniqueID);
				insertPayload.accumulate("ALogHID", appLogID);
				insertPayload.accumulate("MessageType", msgType);
				insertPayload.accumulate("MessageID", msgID);
				insertPayload.accumulate("MessageNo", ""+stepNo);
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				return "";
			}catch (Exception e) {
				String exceptionMsg="";
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				
				if(debug)
					response.getWriter().println("Exception in updateCpiApplicationLog - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
				
				exceptionMsg = buffer.toString();
				stepNo = stepNo+1;
				//insertExceptionMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
				//insertExceptionMsg(request, response, appLogID, msgType, msgID, logMessage, stepNo, oDataURL, userPass, aggregatorID, debug);
				return "";
			}
		}
	
	
	public String insertSalesReportApplicationLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			//responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug);
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("Message1", errorMessage);
				insertPayload.accumulate("MessageNo", stepNo);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", "");
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
			return uniqueID;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - doGet: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			insertSalesReportExceptionLogs(request, response, buffer.toString(), object, oDataURL, userPass, aggregatorID, loginID, debug);
			
			return "";
		}
	}
	
	public String insertSalesReportMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage,int stepNo, String message1, String oDataURL, String userPass, String aggregatorID,String message2,String message3,String message4, boolean debug) throws IOException{
			String errorMessage="";
			JSONObject insertPayload = new JSONObject();
			String executeURL = "", uniqueID="", createdAt="",createdBy="";
			long createdOnInMillis=0;
			JsonObject responseObject=new JsonObject();
			CommonUtils commonUtils = new CommonUtils();
			try{
				createdBy = commonUtils.getUserPrincipal(request, "name", response);
				createdOnInMillis = commonUtils.getCreatedOnDate();
				createdAt = commonUtils.getCreatedAtTime();
				uniqueID = commonUtils.generateGUID(36);
				executeURL = oDataURL+"ApplicationLogMessages";
				if(logMessage.length() > 1000)
					errorMessage = logMessage.substring(0, 999);
				else
					errorMessage = logMessage;
				
				if(debug)
					response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
				
				insertPayload.accumulate("ID", uniqueID);
				insertPayload.accumulate("ALogHID", appLogID);
				insertPayload.accumulate("MessageType", msgType);
				insertPayload.accumulate("Message1", message1);
				insertPayload.accumulate("Message2", message2);
				insertPayload.accumulate("Message3", message3);
				insertPayload.accumulate("Message4", message4);
				insertPayload.accumulate("MessageID", msgID);
				insertPayload.accumulate("MessageNo", ""+stepNo);
				insertPayload.accumulate("ErrorMessage", errorMessage);
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", createdBy);
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				//responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug);
				responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
				return "";
			}catch (Exception e) {
				String exceptionMsg="";
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				
				if(debug)
					response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
				
				exceptionMsg = buffer.toString();
				insertSalesReportExceptionMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
				return "";
			}
		}
	
	
	public String insertSalesReportExceptionMsg(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, int stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
		String errorMessage="";
		JsonObject responseObject = new JsonObject();
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="";
		long createdOnInMillis=0;
		
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
//			stepNo = stepNo+1;
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertExceptionMsg logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", "");
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			//responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug);
			responseObject=commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			return "";
		}
	}
	
	public String insertSalesReportExceptionLogs(HttpServletRequest request, HttpServletResponse response, String exceptionTrace, String subObject, 
			String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		JsonObject responseObject = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertExceptionInAppLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertExceptionInAppLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertExceptionInAppLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertExceptionInAppLogs createdAt: "+createdAt);
				response.getWriter().println("insertExceptionInAppLogs subObject: "+subObject);
				response.getWriter().println("insertExceptionInAppLogs executeURL: "+executeURL);
			}
						
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", "Java");
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate", "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertExceptionInAppLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String truncatedExceptionTrace="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(exceptionTrace.length() > 1000)
					truncatedExceptionTrace = exceptionTrace.substring(0, 999);
				else
					truncatedExceptionTrace = truncatedExceptionTrace+exceptionTrace;
				
				if(debug)
					response.getWriter().println("insertExceptionInAppLogs truncatedExceptionTrace: "+truncatedExceptionTrace);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "E");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", truncatedExceptionTrace);
				responseObject =commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
		}catch (Exception e) {
			uniqueID = "";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception in ODataLogs - insertExceptionLogs: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
		}
		return uniqueID;
	}
	
	public JsonObject insertApprovalRequestLogs(HttpServletRequest request, HttpServletResponse response, String object, String subObject, String payLoad, 
			String stepNo, String program, String oDataURL, String userPass, String aggregatorID, String loginID,String process1,String process6,String process7,String cpGUID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		CommonUtils commonUtils = new CommonUtils();
//		String returnVar1="";
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		JsonObject responseObject = new JsonObject();
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertIntoApplicationLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertIntoApplicationLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertIntoApplicationLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertIntoApplicationLogs createdAt: "+createdAt);
				response.getWriter().println("insertIntoApplicationLogs subObject: "+subObject);
				response.getWriter().println("insertIntoApplicationLogs executeURL: "+executeURL);
			}
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", object);
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate",  "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("Program", program);
			insertPayload.accumulate("ProcessRef1", cpGUID);
			responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertIntoApplicationLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String errorMessage="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(payLoad.length() > 1000)
					errorMessage = payLoad.substring(0, 999);
				else
					errorMessage = payLoad;
				
				if(debug)
					response.getWriter().println("insertIntoApplicationLogs payLoad: "+payLoad);
				if(process1.length()>1000){
					process1=process1.substring(0, 999);
				}
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "I");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", process1);
				insertPayload.accumulate("Message1", process6);
				insertPayload.accumulate("Message2", process7);
				insertPayload.accumulate("MessageNo", "");
				insertPayload.accumulate("CreatedAt", createdAt);
				insertPayload.accumulate("CreatedBy", loginID);
				insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
				responseObject = commonUtils.executePostURL(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
			return responseObject;
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			JsonObject resObj=new JsonObject();
			if(debug)
				response.getWriter().println("Exception in  inserting Application Log Message: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			resObj.addProperty("Message", "Exception occurred while  inserting Application Log Message Table: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			resObj.addProperty("Status","000002");
			resObj.addProperty("ErrorCode","J002");
			return resObj;
		}
	}
	
	
	
	public String insertQuartelyMessageForAppLogs(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage,int stepNo, String message1, String oDataURL, String userPass, String aggregatorID,String message2,String message3,String message4, boolean debug) throws IOException{
		String errorMessage="";
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="",createdBy="";
		long createdOnInMillis=0;
		JsonObject responseObject=new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdBy = commonUtils.getUserPrincipal(request, "name", response);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertMessageForAppLogs logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
		if (message1 != null) {
			if (message1.length() > 100) {
				message1 = message1.substring(0, 100);
			}
			insertPayload.accumulate("Message1", message1);
		}
		if (message2 != null) {
			if (message2.length() > 100) {
				message2 = message2.substring(0, 100);
			}
			insertPayload.accumulate("Message2", message2);
		}
		
		if (message3 != null) {
			if (message3.length() > 100) {
				message3 = message3.substring(0, 100);
			}
			insertPayload.accumulate("Message3", message3);
		}
		
		if (message4 != null) {
			if (message4.length() > 100) {
				message4 = message4.substring(0, 100);
			}
			insertPayload.accumulate("Message4", message4);
		}
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", createdBy);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			String exceptionMsg="";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			exceptionMsg = buffer.toString();
			insertExceptionQuartelySalesMsg(request, response, appLogID, "E", "/ARTEC/PY", exceptionMsg, stepNo, oDataURL, userPass, aggregatorID, debug);
			return "";
		}
	}
	
	
	public String insertQuartelyReposrtExceptionLogs(HttpServletRequest request, HttpServletResponse response, String exceptionTrace, String subObject, 
			String oDataURL, String userPass, String aggregatorID, String loginID, boolean debug) throws IOException{
		JSONObject insertPayload = new JSONObject();
		JsonObject responseObject = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String uniqueID="", createdAt="", executeURL="", uniqueMsgID="";
		long createdOnInMillis = 0;
		
		try{
			uniqueID = commonUtils.generateGUID(36);
			uniqueMsgID = commonUtils.generateGUID(36);
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			
			executeURL = oDataURL+"ApplicationLogs";
			
			if(debug){
				response.getWriter().println("insertExceptionInAppLogs uniqueID: "+uniqueID);
				response.getWriter().println("insertExceptionInAppLogs uniqueMsgID: "+uniqueMsgID);
				response.getWriter().println("insertExceptionInAppLogs createdOnInMillis: "+createdOnInMillis);
				response.getWriter().println("insertExceptionInAppLogs createdAt: "+createdAt);
				response.getWriter().println("insertExceptionInAppLogs subObject: "+subObject);
				response.getWriter().println("insertExceptionInAppLogs executeURL: "+executeURL);
			}
						
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("AggregatorID", aggregatorID);
			insertPayload.accumulate("LogObject", "Java");
			insertPayload.accumulate("LogSubObject", subObject);
			insertPayload.accumulate("LogUser", loginID);
			insertPayload.accumulate("LogDate", "/Date("+createdOnInMillis+")/");
			insertPayload.accumulate("LogTime", createdAt);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", loginID);
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			
			if(debug)
				response.getWriter().println("insertExceptionInAppLogs responseObject: "+responseObject);
			
			if(! responseObject.has("error")){
				String truncatedExceptionTrace="";
				responseObject = new JsonObject();
				insertPayload = new JSONObject();
				executeURL = "";
				
				executeURL = oDataURL+"ApplicationLogMessages";
				if(exceptionTrace.length() > 1000)
					truncatedExceptionTrace = exceptionTrace.substring(0, 999);
				else
					truncatedExceptionTrace = truncatedExceptionTrace+exceptionTrace;
				
				if(debug)
					response.getWriter().println("insertExceptionInAppLogs truncatedExceptionTrace: "+truncatedExceptionTrace);
				
				insertPayload.accumulate("ID", uniqueMsgID);
				insertPayload.accumulate("ALogHID", uniqueID);
				insertPayload.accumulate("MessageType", "E");
				insertPayload.accumulate("MessageID", "/ARTEC/PY");
				insertPayload.accumulate("ErrorMessage", truncatedExceptionTrace);
				
				responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			}
			
		}catch (Exception e) {
			uniqueID = "";
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Exception in ODataLogs - insertExceptionLogs: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
		}
		return uniqueID;
	}
	
	
	public String insertExceptionQuartelySalesMsg(HttpServletRequest request, HttpServletResponse response, String appLogID, String msgType, 
			String msgID, String logMessage, int stepNo, String oDataURL, String userPass, String aggregatorID, boolean debug) throws IOException{
		String errorMessage="";
		JsonObject responseObject = new JsonObject();
		JSONObject insertPayload = new JSONObject();
		String executeURL = "", uniqueID="", createdAt="";
		long createdOnInMillis=0;
		
		CommonUtils commonUtils = new CommonUtils();
		try{
			createdOnInMillis = commonUtils.getCreatedOnDate();
			createdAt = commonUtils.getCreatedAtTime();
			uniqueID = commonUtils.generateGUID(36);
//			stepNo = stepNo+1;
			executeURL = oDataURL+"ApplicationLogMessages";
			if(logMessage.length() > 1000)
				errorMessage = logMessage.substring(0, 999);
			else
				errorMessage = logMessage;
			
			if(debug)
				response.getWriter().println("insertExceptionMsg logMessage: "+logMessage);
			
			insertPayload.accumulate("ID", uniqueID);
			insertPayload.accumulate("ALogHID", appLogID);
			insertPayload.accumulate("MessageType", msgType);
			insertPayload.accumulate("MessageID", msgID);
			insertPayload.accumulate("MessageNo", ""+stepNo);
			insertPayload.accumulate("ErrorMessage", errorMessage);
			insertPayload.accumulate("CreatedAt", createdAt);
			insertPayload.accumulate("CreatedBy", "");
			insertPayload.accumulate("CreatedOn",  "/Date("+createdOnInMillis+")/");
			responseObject = commonUtils.executePostURLTemp(executeURL, userPass, response, insertPayload, request, debug, "PCGWHANA");
			return "";
		}catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
			if(debug)
				response.getWriter().println("Exception in PGPush - insertExceptionMsg: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			
			return "";
		}
	}
	
	
	public void createApplicationLogMsgOnEvent(HttpServletRequest request,HttpServletResponse response,boolean debug,JsonArray appLogMsgArray,String appLogId,String message1,String message2,String message3,String message4,String usermessage,String errorMessage)throws Exception{
		CommonUtils commonUtils=new CommonUtils();
		JsonObject appLogMsg=new JsonObject();
		try{
			String createdOn = commonUtils.getCurrentDate("yyyy-MM-dd");
			String createdAtTime = commonUtils.getCurrentTime();
			String createdBy = commonUtils.getLoginID(request, response, debug);
			String guid = commonUtils.generateGUID(36);
			appLogMsg.addProperty("ID", guid);
			appLogMsg.addProperty("ALogHID", appLogId);
			appLogMsg.addProperty("MessageType", "I");
			appLogMsg.addProperty("MessageID", "/ARTEC/PY");
			appLogMsg.addProperty("CreatedBy", createdBy);
			appLogMsg.addProperty("CreatedOn", createdOn);
			appLogMsg.addProperty("CreatedAt", createdAtTime);
			appLogMsg.addProperty("SourceReferenceID", "");
			if(message1.length()>100){
				message1=message1.substring(0,100);
			}
			appLogMsg.addProperty("Message1",message1);
			if(message2.length()>100){
				message2=message2.substring(0,100);
			}
			appLogMsg.addProperty("Message2",message2);
			if(message3.length()>100){
				message3=message3.substring(0,100);
			}
			appLogMsg.addProperty("Message3",message3);
			if(message4.length()>100){
				message4=message4.substring(0,100);
			}
			appLogMsg.addProperty("Message4",message4);
			if(usermessage.length()==0){
				if(errorMessage.length()>1000){
					if(errorMessage.length()<=2000){
					usermessage=errorMessage.substring(1000,errorMessage.length());
					}else{
						usermessage=errorMessage.substring(1000,2000);
					}
				}
			}else if(usermessage.length()>1000){
				usermessage=usermessage.substring(0, 1000);
			}
			if(errorMessage.length()>1000){
				errorMessage=errorMessage.substring(0,1000);
			}
			appLogMsg.addProperty("UserMessage",usermessage);
			appLogMsg.addProperty("ErrorMessage",errorMessage);
			appLogMsgArray.add(appLogMsg);
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			if(debug){
				response.getWriter().println(buffer.toString());
			}
		}
	}
	
	public JsonObject insertApplicationLogs(HttpServletResponse response,JsonArray appLogArray,boolean debug){
		CommonUtils commonUtils=new CommonUtils();
		String username="",password="",odataUrl="",executeUrl="";
		final String boundary = "batch_" + UUID.randomUUID().toString();
		final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
		final String APPLICATION_JSON = "application/json";
		final String AUTHORIZATION_HEADER = "Authorization";
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGWHANA, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			odataUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);

			// Changes on 20231120
			// executeUrl=odataUrl+"ApplicationLogs";
			executeUrl="ApplicationLogs";
			if(debug){
				response.getWriter().println("ApplicationLogs execute Url:"+executeUrl);
				response.getWriter().println("ApplicationLogs Input Payload :"+appLogArray.get(0).getAsJsonObject());
			}
			String userPass=username+":"+password;
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER,
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeUrl)
					.headers(changeSetHeaders).body(appLogArray.get(0).toString()).build();
			changeSet.add(changeRequest);
			// Changes on 20231120
			// executeUrl=odataUrl+"ApplicationLogMessages";
			executeUrl="ApplicationLogMessages";

			if(debug){
				response.getWriter().println("ApplicationLogMessages execute Url:"+executeUrl);
			}
			for(int i=1;i<appLogArray.size();i++){
				JsonObject asJsonObject = appLogArray.get(i).getAsJsonObject();
				if(debug){
					response.getWriter().println("ApplicationLogMessages payload:"+asJsonObject);	
				}
				BatchChangeSetPart changeRequest1 = BatchChangeSetPart.method("POST").uri(executeUrl)
						.headers(changeSetHeaders).body(asJsonObject.toString()).build();
				changeSet.add(changeRequest1);
			}
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body,"UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody,"UTF-8");
			List<BatchSingleResponse> responses = EntityProvider
					.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes,"UTF-8"), contentType);
			boolean recordUpdated=true;
			String responsebOdy=null;
			JsonArray resArray=new JsonArray();
			JsonParser parse=new JsonParser();
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if(debug){
					response.getWriter().println("BatchSingleResponse statusCode:"+statusCode);
				}
				if (statusCode.equalsIgnoreCase("201")) {
					responsebOdy= singleRes.getBody();
					JsonObject eligiblitilyRoc = (JsonObject)parse.parse(responsebOdy);
					resArray.add(eligiblitilyRoc);
				}else{
					recordUpdated=false;
					responsebOdy= singleRes.getBody();
					break;
				}
			}
			
			if(recordUpdated){
				JsonObject resObj=new JsonObject();
				resObj.addProperty("Message", "Record Inserted Successfully");
				resObj.addProperty("ErrorCode", "");
				resObj.addProperty("Status", "000001");
				return resObj;
			}else{
				JsonObject resObj=new JsonObject();
				resObj.addProperty("Message", "Applogs not inserted:"+responsebOdy);
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				return resObj;
			}
			
			
		}catch(Exception ex){
        JsonObject result=new JsonObject();
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			result.addProperty("Message", ex.getLocalizedMessage());
			result.addProperty("ExceptionStackTrace", buffer.toString());
			return result;
		}
	}
	
	/* private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	} */
}

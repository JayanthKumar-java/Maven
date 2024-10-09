package com.arteriatech.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * Servlet implementation class BPDeclarations
 */
@WebServlet("/BPDeclarations")
public class BPDeclarations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BPDeclarations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String payloadRequest ="", cpGuid="", aggregatorID="", cpType="";
		boolean debug=false;
		String statusId="999999";
		CommonUtils commonUtils = new CommonUtils();
		String errorCode="", errorMsg="", oDataUrl="", userName="", password="", userPass="", executeURL="";
		JSONObject bpRenewalJson = new JSONObject();
		JsonObject bpGetResponse = new JsonObject();
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			payloadRequest = request.getParameter("BPDeclarations");
			
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			
			try {
				bpRenewalJson = new JSONObject(payloadRequest);
				if (null != bpRenewalJson.getString("debug")
						&& bpRenewalJson.getString("debug").equalsIgnoreCase("true")){
					debug = true;
				}
			}catch (JSONException e){
				if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")){
					debug = false;
				}
			}
			if (null != bpRenewalJson.getString("CustomerNo")) {
				cpGuid = bpRenewalJson.getString("CustomerNo");
			} else {
				errorCode = "E100";
				errorMsg = properties.getProperty(errorCode);
			}
			
			if (null != bpRenewalJson.getString("CPType")) {
				cpType = bpRenewalJson.getString("CPType");
			} else {
				errorCode = "E201";
				if(errorMsg.trim().length()>0)
					errorMsg = errorMsg+", "+properties.getProperty(errorCode);
				else
					errorMsg = properties.getProperty(errorCode);
			}
			
			if(errorCode.trim().length() == 0){
				String formattedStr = "";
				try{
					int number = Integer.parseInt(cpGuid);
					formattedStr = ("0000000000" + cpGuid).substring(cpGuid.length());
					cpGuid = formattedStr;
				}catch (NumberFormatException e) {
//					formattedStr = customerNo;
				}
				
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				userPass = userName+":"+password;
				
//				executeURL = oDataUrl+"BPHeader?$filter=CPGuid%20eq%20%27"+cpGuid+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20StatusID%20eq%20%27%27";
				//executeURL = oDataUrl+"BPHeaders?$expand=BPContactPersons&$filter=CPGuid%20eq%20%27"+cpGuid+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20StatusID%20eq%20%27%27";
//				executeURL = oDataUrl+"BPHeaders?$expand=BPContactPersons&$filter=CPGuid%20eq%20%27"+cpGuid+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20%20StatusID%20eq%20%27"+statusId+"%27%20or%20StatusID%20eq%20%27%27";
				//executeURL = oDataUrl+"BPHeaders?$expand=BPContactPersons&$filter=CPGuid%20eq%20%27"+cpGuid+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20StatusID%20eq%20%27%27%20or%20StatusID%20eq%20%27"+statusId+"%27";
				executeURL=oDataUrl+"BPHeaders?$expand=BPContactPersons&$filter=(CPGuid%20eq%20%27"+cpGuid+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20(StatusID%20eq%20%27"+statusId+"%27%20or%20StatusID%20eq%20%27%27))%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27";
				if(debug){
					response.getWriter().println("executeURL: "+executeURL);
					response.getWriter().println("oDataUrl: "+oDataUrl);
					response.getWriter().println("userName: "+userName);
					response.getWriter().println("password: "+password);
				}
				
				bpGetResponse = commonUtils.executeURL(executeURL, userPass, response);
				
				bpGetResponse.addProperty("ErrorCode","");
				bpGetResponse.addProperty("Status", "000001");
				bpGetResponse.addProperty("Message","");
				response.getWriter().println(new Gson().toJson(bpGetResponse));
			}else{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J002");
				result.addProperty("Status", "000002");
				result.addProperty("Message", errorMsg);
				response.getWriter().println(new Gson().toJson(result));
			}
		}catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", e.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		doGet(request, response);
		String payloadRequest ="", cpGuid="", aggregatorID="", bpID="";
		boolean debug=false, bpcntpFail=false;
		CommonUtils commonUtils = new CommonUtils();
		boolean isInsertBpheader=false;
        int count=0;
		try{
			payloadRequest = getGetBody(request, response);
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			JSONObject inputPayload = new JSONObject(payloadRequest);
			try {
				if(inputPayload.has("debug") && ! inputPayload.isNull("debug") &&
						inputPayload.getBoolean("debug") == true) 
					debug = true;
			} catch (Exception e) {
				debug = false;
			}
			if(debug)
				response.getWriter().println("BPDeclarations.inputPayload: "+inputPayload);

			if(debug){
					response.getWriter().println("inside bpDeclaration post method");
			}
			
			cpGuid = inputPayload.getString("CPGuid");
			if (debug)
				response.getWriter().println("cpGuid: " + cpGuid);
			
			JsonObject insertBPResponse = new JsonObject();
			insertBPResponse = commonUtils.insertIntoBPHeaders(aggregatorID, inputPayload,  request, response, debug);
			
			if (debug)
				response.getWriter().println("insertBPResponse: " + insertBPResponse);
			
			if (insertBPResponse.has("ErrorCode") && insertBPResponse.get("ErrorCode").getAsString().equalsIgnoreCase("001")) {
				JsonObject result = new JsonObject();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				result.addProperty("ErrorCode", insertBPResponse.get("ErrorCode").getAsString());
				result.addProperty("Status", "000002");
				result.addProperty("Message", insertBPResponse.get("Message").getAsString());
				response.getWriter().println(new Gson().toJson(result));
			}else{
				//success
				isInsertBpheader=true;
				bpID = insertBPResponse.get("d").getAsJsonObject().get("ID").getAsString();
				if (debug)
					response.getWriter().println("bpID: " + bpID);
				
				JsonObject inputJsonObj = new JsonParser().parse(payloadRequest).getAsJsonObject();
				
				JsonArray bpcntpInputArray = new JsonArray();
				bpcntpInputArray = inputJsonObj.get("BPContactPersons").getAsJsonArray();
				JsonObject insertBPCNTPReqObj = new JsonObject();
				JSONObject bpcntpPayload = new JSONObject();
				
				if (debug)
					response.getWriter().println("bpcntpInputArray: " +bpcntpInputArray);
				
				JsonObject insertBPCNTPResponse = new JsonObject();
				
				for(int i=0 ; i<bpcntpInputArray.size() ; i++){
					insertBPCNTPResponse = new JsonObject();
					insertBPCNTPReqObj = bpcntpInputArray.get(i).getAsJsonObject();
					bpcntpPayload = new JSONObject();
					bpcntpPayload.accumulate("BPID", bpID);
					bpcntpPayload.accumulate("ID", insertBPCNTPReqObj.get("BPCntPrsnGuid").getAsString());
					bpcntpPayload.accumulate("Name1", insertBPCNTPReqObj.get("Name1").getAsString());
					bpcntpPayload.accumulate("Name2", insertBPCNTPReqObj.get("Name2").getAsString());
					bpcntpPayload.accumulate("DOB", insertBPCNTPReqObj.get("DOB").getAsString());
					bpcntpPayload.accumulate("Mobile", insertBPCNTPReqObj.get("Mobile").getAsString());
					bpcntpPayload.accumulate("EmailID", insertBPCNTPReqObj.get("EmailID").getAsString());
					bpcntpPayload.accumulate("PAN", insertBPCNTPReqObj.get("PanNo").getAsString());
					bpcntpPayload.accumulate("GenderID", insertBPCNTPReqObj.get("GenderID").getAsString());
					bpcntpPayload.accumulate("SigningOrder", insertBPCNTPReqObj.get("SigningOrder").getAsString());
					bpcntpPayload.accumulate("PostalCode", insertBPCNTPReqObj.get("PostalCode").getAsString());
					bpcntpPayload.accumulate("BPType", insertBPCNTPReqObj.get("BPType").getAsString());
					bpcntpPayload.accumulate("Designation", insertBPCNTPReqObj.get("Designation").getAsString());
					bpcntpPayload.accumulate("UniqueIdentifier1", insertBPCNTPReqObj.get("UniqueIdentifier1").getAsString());
					bpcntpPayload.accumulate("AuthorizedSignatory", insertBPCNTPReqObj.get("AuthorizedSignatory").getAsString());
					
					if(debug){
						response.getWriter().println("BPDeclarations.bpcntpPayload: "+bpcntpPayload);
					}
					
					insertBPCNTPResponse = commonUtils.insertIntoBPContactPerson(bpcntpPayload, request, response, debug);
					if(debug){
						response.getWriter().println("BPDeclarations.insertBPCNTPResponse: "+insertBPCNTPResponse);
					}
					
					if(insertBPCNTPResponse.has("ErrorCode") && insertBPCNTPResponse.get("ErrorCode").getAsString().trim().length() > 0){
						bpcntpFail = true;
						break;
					}
					count++;
				}
				
				if (bpcntpFail) {
					JsonObject result = new JsonObject();
					JsonObject deleteBpHeaderRecord = commonUtils.deleteBpHeaderRecord(bpID, response, request, debug);
					if (debug) {
						response.getWriter().println(" Deleted  BPheader Record Response " + deleteBpHeaderRecord);

					}
					if (count > 0) {
						JsonObject deleteBPContactPerson = commonUtils.deleteBPContactPerson(bpID, request, response,
								debug);
						if (debug) {
							response.getWriter()
									.println(" Deleted  BPContract Person Response" + deleteBPContactPerson);
						}
					}
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result.addProperty("ErrorCode", insertBPCNTPResponse.get("ErrorCode").getAsString());
					result.addProperty("Status", "000002");
					result.addProperty("Message", insertBPCNTPResponse.get("Message").getAsString());
					response.getWriter().println(new Gson().toJson(result));
				}else{
					response.setContentType("application/json");
					insertBPResponse.addProperty("ErrorCode","");
					insertBPResponse.addProperty("Status", "000001");
					insertBPResponse.addProperty("Message","");
					response.getWriter().println(insertBPResponse);
				}
			}
		} catch (Exception e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			}

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", e.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
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

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        //super.doDelete(request, response);
		CommonUtils commonUtils = new CommonUtils();
		String bpGuid = "";
		String inputPayload = "";
		JsonParser jsonParer = new JsonParser();
		JsonObject inputJson = new JsonObject();
		JsonObject bpHeaderRecord = new JsonObject();
		String executeURL = "", oDataUrl = "", userName = "", password = "", userPass = "";
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			inputJson = (JsonObject) jsonParer.parse(inputPayload);
			if (inputJson.has("debug") && !inputJson.get("debug").isJsonNull()
					&& inputJson.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;
			}
			if (!inputJson.get("BpGuid").isJsonNull() && !inputJson.get("BpGuid").getAsString().equalsIgnoreCase("")) {
				bpGuid = inputJson.get("BpGuid").getAsString();
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println(" Odata URL " + oDataUrl);
					response.getWriter().println(" User Name " + userName);
					response.getWriter().println("Password " + password);
				}
				
				executeURL = oDataUrl + "BPHeader?$filter=ID%20eq%20%27" + bpGuid + "%27";
				if (debug) {
					response.getWriter().println("executeURL " + executeURL);
				}
				bpHeaderRecord = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("BpHeader Record " + bpHeaderRecord);
				}
				if (bpHeaderRecord != null
						&& bpHeaderRecord.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					JsonObject deleteBpHeaderRecord = commonUtils.deleteBpHeaderRecord(bpGuid, response, request,
							debug);
					if (deleteBpHeaderRecord.get("ErrorCode").getAsString().trim().equalsIgnoreCase("")) {
						JsonObject deleteBPContactPerson = commonUtils.deleteBPContactPerson(bpGuid, request, response,
								debug);
						response.getWriter().println(deleteBpHeaderRecord);
						if(debug){
							response.getWriter().println(deleteBPContactPerson);
						}

					} else {

						response.getWriter().println(new Gson().toJson(deleteBpHeaderRecord));

					}

				} else {
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "J001");
					result.addProperty("Status", "000002");
					result.addProperty("Message", bpGuid + " BpGuid does not exists");
					response.getWriter().println(new Gson().toJson(result));
				}

			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("Status", "000002");
				result.addProperty("Message", "BpGuid is Mandatory for Deleting");
				response.getWriter().println(new Gson().toJson(result));

			}

		} catch (Exception e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			}
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", e.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		// CloseableHttpClient httpClient = null;
		CommonUtils commonUtils = new CommonUtils();
		boolean debug = false;
		String executeURL="", payloadRequest = "", dataPayload="", corpID="", aggregatorID="", loginID="", oDataURL="", changedAt="";
		JSONObject bpUpdateInputJson = new JSONObject();
		JSONObject bpUpdatePayload = new JSONObject();
		String mobileNo2="", landlineNo="", bpID="", changedBy="", userName="", password="", userPass="";
		long changedOnInMillis = 0;
		JsonObject getBPJsonObj = new  JsonObject();
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			debug = false;
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			payloadRequest = getGetBody(request, response);
			dataPayload = payloadRequest.replaceAll("\\\\", "");
			JSONObject inputJsonObject = new JSONObject(payloadRequest);
			changedBy = commonUtils.getUserPrincipal(request, "name", response);
			changedOnInMillis = commonUtils.getCreatedOnDate();
			changedAt = commonUtils.getCreatedAtTime();
			try {
				bpUpdateInputJson = new JSONObject(payloadRequest);
				if (null != bpUpdateInputJson.getString("debug")
						&& bpUpdateInputJson.getString("debug").equalsIgnoreCase("true")){
					debug = true;
				}
			}catch (JSONException e){
				if (e.getMessage().equalsIgnoreCase("JSONObject[\"debug\"] not found.")){
					debug = false;
				}
			}
			
			if(debug){
				response.getWriter().println("payloadRequest: "+payloadRequest);
				response.getWriter().println("inputJsonObject: "+inputJsonObject);
				response.getWriter().println("changedBy: "+changedBy);
				response.getWriter().println("changedOnInMillis: "+changedOnInMillis);
				response.getWriter().println("changedAt: "+changedAt);
			}
			
			if(inputJsonObject.has("ID")){
				bpID = inputJsonObject.getString("ID");
			}
			// JsonObject bpGetObj = new JsonObject();
			if(bpID.trim().length() > 0){
				JsonObject getBP = new JsonObject();
				getBPJsonObj = commonUtils.getBPByGuid(request, response, bpID, debug);
				getBP = getBPJsonObj.get("d").getAsJsonObject();
				if(debug){
					response.getWriter().println("getBP: "+getBP);
				}
				
				aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
				oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				userPass = userName+":"+password;
				
				if(inputJsonObject.has("Mobile2")){
					mobileNo2 = inputJsonObject.getString("Mobile2");
					bpUpdatePayload.accumulate("Mobile2", inputJsonObject.getString("Mobile2"));
				}else{
					//bpUpdatePayload.accumulate("Mobile2", getBP.get("Mobile2").getAsString());
					if(getBP.has("Mobile2") && !getBP.get("Mobile2").isJsonNull() && getBP.get("Mobile2").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Mobile2", getBP.get("Mobile2").getAsString());
					else
						bpUpdatePayload.accumulate("Mobile2", "");
				}
				
				if(inputJsonObject.has("LandLine1")){
					landlineNo = inputJsonObject.getString("LandLine1");
					bpUpdatePayload.accumulate("LandLine1", inputJsonObject.getString("LandLine1"));
				}else{
					// bpUpdatePayload.accumulate("LandLine1", getBP.get("LandLine1").getAsString());
					if(getBP.has("LandLine1") && !getBP.get("LandLine1").isJsonNull() && getBP.get("LandLine1").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("LandLine1", getBP.get("LandLine1").getAsString());
					else
						bpUpdatePayload.accumulate("LandLine1", "");
				}
				
				if(debug){
					response.getWriter().println("mobileNo2: "+mobileNo2);
					response.getWriter().println("landlineNo: "+landlineNo);
					response.getWriter().println("bpID: "+bpID);
				}
				
				if(inputJsonObject.has("CPGuid")){
					bpUpdatePayload.accumulate("CPGuid", inputJsonObject.getString("CPGuid"));
				}else{
					if(getBP.has("CPGuid") && !getBP.get("CPGuid").isJsonNull() && getBP.get("CPGuid").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CPGuid", getBP.get("CPGuid").getAsString());
					else
						bpUpdatePayload.accumulate("CPGuid", "");
				}
				
				if(inputJsonObject.has("CPType")){
					bpUpdatePayload.accumulate("CPType", inputJsonObject.getString("CPType"));
				}else{
					if(getBP.has("CPType") && !getBP.get("CPType").isJsonNull() && getBP.get("CPType").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CPType", getBP.get("CPType").getAsString());
					else
						bpUpdatePayload.accumulate("CPType", "");

					// bpUpdatePayload.accumulate("CPType", getBP.get("CPType").getAsString());
				}
				
				if(inputJsonObject.has("CPName")){
					bpUpdatePayload.accumulate("CPName", inputJsonObject.getString("CPName"));
				}else{
					// bpUpdatePayload.accumulate("CPName", getBP.get("CPName").getAsString());
					if(getBP.has("CPName") && !getBP.get("CPName").isJsonNull() && getBP.get("CPName").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CPName", getBP.get("CPName").getAsString());
					else
						bpUpdatePayload.accumulate("CPName", "");
				}
				
				if(inputJsonObject.has("AggregatorID")){
					bpUpdatePayload.accumulate("AggregatorID", inputJsonObject.getString("AggregatorID"));
				}else{
					aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
					bpUpdatePayload.accumulate("AggregatorID",aggregatorID);
				}
				
				if(inputJsonObject.has("IncorporationDate")){
					bpUpdatePayload.accumulate("IncorporationDate", inputJsonObject.getString("IncorporationDate"));
				}else{
					if(getBP.has("IncorporationDate") && !getBP.get("IncorporationDate").isJsonNull() && getBP.get("IncorporationDate").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("IncorporationDate", getBP.get("IncorporationDate").getAsString());
					else
						bpUpdatePayload.accumulate("IncorporationDate", JSONObject.NULL);
					// bpUpdatePayload.accumulate("IncorporationDate", getBP.get("IncorporationDate").getAsString());
				}
				
				if(inputJsonObject.has("UtilDistrict")){
					bpUpdatePayload.accumulate("UtilDistrict", inputJsonObject.getString("UtilDistrict"));
				}else{
					// bpUpdatePayload.accumulate("UtilDistrict", getBP.get("UtilDistrict").getAsString());
					if(getBP.has("UtilDistrict") && !getBP.get("UtilDistrict").isJsonNull() && getBP.get("UtilDistrict").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("UtilDistrict", getBP.get("UtilDistrict").getAsString());
					else
						bpUpdatePayload.accumulate("UtilDistrict", "");
				}
				
				if(inputJsonObject.has("LegalStatus")){
					bpUpdatePayload.accumulate("LegalStatus", inputJsonObject.getString("LegalStatus"));
				}else{
					// bpUpdatePayload.accumulate("LegalStatus", getBP.get("LegalStatus").getAsString());
					if(getBP.has("LegalStatus") && !getBP.get("LegalStatus").isJsonNull() && getBP.get("LegalStatus").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("LegalStatus", getBP.get("LegalStatus").getAsString());
					else
						bpUpdatePayload.accumulate("LegalStatus", "");
				}
				
				if(inputJsonObject.has("Address1")){
					bpUpdatePayload.accumulate("Address1", inputJsonObject.getString("Address1"));
				}else{
					// bpUpdatePayload.accumulate("Address1", getBP.get("Address1").getAsString());
					if(getBP.has("Address1") && !getBP.get("Address1").isJsonNull() && getBP.get("Address1").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Address1", getBP.get("Address1").getAsString());
					else
						bpUpdatePayload.accumulate("Address1", "");
				}
				
				if(inputJsonObject.has("Address2")){
					bpUpdatePayload.accumulate("Address2", inputJsonObject.getString("Address2"));
				}else{
					// bpUpdatePayload.accumulate("Address2", getBP.get("Address2").getAsString());
					if(getBP.has("Address2") && !getBP.get("Address2").isJsonNull() && getBP.get("Address2").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Address2", getBP.get("Address2").getAsString());
					else
						bpUpdatePayload.accumulate("Address2", "");
				}
				
				if(inputJsonObject.has("Address3")){
					bpUpdatePayload.accumulate("Address3", inputJsonObject.getString("Address3"));
				}else{
					// bpUpdatePayload.accumulate("Address3", getBP.get("Address3").getAsString());
					if(getBP.has("Address3") && !getBP.get("Address3").isJsonNull() && getBP.get("Address3").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Address3", getBP.get("Address3").getAsString());
					else
						bpUpdatePayload.accumulate("Address3", "");
				}
				
				if(inputJsonObject.has("Address4")){
					bpUpdatePayload.accumulate("Address4", inputJsonObject.getString("Address4"));
				}else{
					// bpUpdatePayload.accumulate("Address4", getBP.get("Address4").getAsString());
					if(getBP.has("Address4") && !getBP.get("Address4").isJsonNull() && getBP.get("Address4").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Address4", getBP.get("Address4").getAsString());
					else
						bpUpdatePayload.accumulate("Address4", "");
				}
				
				if(inputJsonObject.has("District")){
					bpUpdatePayload.accumulate("District", inputJsonObject.getString("District"));
				}else{
					// bpUpdatePayload.accumulate("District", getBP.get("District").getAsString());
					if(getBP.has("District") && !getBP.get("District").isJsonNull() && getBP.get("District").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("District", getBP.get("District").getAsString());
					else
						bpUpdatePayload.accumulate("District", "");
				}
				
				if(inputJsonObject.has("City")){
					bpUpdatePayload.accumulate("City", inputJsonObject.getString("City"));
				}else{
					// bpUpdatePayload.accumulate("City", getBP.get("City").getAsString());
					if(getBP.has("City") && !getBP.get("City").isJsonNull() && getBP.get("City").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("City", getBP.get("City").getAsString());
					else
						bpUpdatePayload.accumulate("City", "");
				}
				
				if(inputJsonObject.has("State")){
					bpUpdatePayload.accumulate("State", inputJsonObject.getString("State"));
				}else{
					// bpUpdatePayload.accumulate("State", getBP.get("State").getAsString());
					if(getBP.has("State") && !getBP.get("State").isJsonNull() && getBP.get("State").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("State", getBP.get("State").getAsString());
					else
						bpUpdatePayload.accumulate("State", "");
				}
				
				if(inputJsonObject.has("StateDesc")){
					bpUpdatePayload.accumulate("StateDesc", inputJsonObject.getString("StateDesc"));
				}else{
					// bpUpdatePayload.accumulate("StateDesc", getBP.get("StateDesc").getAsString());
					if(getBP.has("StateDesc") && !getBP.get("StateDesc").isJsonNull() && getBP.get("StateDesc").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("StateDesc", getBP.get("StateDesc").getAsString());
					else
						bpUpdatePayload.accumulate("StateDesc", "");
				}
				
				if(inputJsonObject.has("Country")){
					bpUpdatePayload.accumulate("Country", inputJsonObject.getString("Country"));
				}else{
					// bpUpdatePayload.accumulate("Country", getBP.get("Country").getAsString());
					if(getBP.has("Country") && !getBP.get("Country").isJsonNull() && getBP.get("Country").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Country", getBP.get("Country").getAsString());
					else
						bpUpdatePayload.accumulate("Country", "");
				}
				
				if(inputJsonObject.has("CountryDesc")){
					bpUpdatePayload.accumulate("CountryDesc", inputJsonObject.getString("CountryDesc"));
				}else{
					// bpUpdatePayload.accumulate("CountryDesc", getBP.get("CountryDesc").getAsString());
					if(getBP.has("CountryDesc") && !getBP.get("CountryDesc").isJsonNull() && getBP.get("CountryDesc").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CountryDesc", getBP.get("CountryDesc").getAsString());
					else
						bpUpdatePayload.accumulate("CountryDesc", "");
				}
				
				if(inputJsonObject.has("Pincode")){
					bpUpdatePayload.accumulate("Pincode", inputJsonObject.getString("Pincode"));
				}else{
					// bpUpdatePayload.accumulate("Pincode", getBP.get("Pincode").getAsString());
					if(getBP.has("Pincode") && !getBP.get("Pincode").isJsonNull() && getBP.get("Pincode").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Pincode", getBP.get("Pincode").getAsString());
					else
						bpUpdatePayload.accumulate("Pincode", "");
				}
				
				if(inputJsonObject.has("Mobile1")){
					bpUpdatePayload.accumulate("Mobile1", inputJsonObject.getString("Mobile1"));
				}else{
					// bpUpdatePayload.accumulate("Mobile1", getBP.get("Mobile1").getAsString());
					if(getBP.has("Mobile1") && !getBP.get("Mobile1").isJsonNull() && getBP.get("Mobile1").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Mobile1", getBP.get("Mobile1").getAsString());
					else
						bpUpdatePayload.accumulate("Mobile1", "");
				}
				
				if(inputJsonObject.has("EmailID")){
					bpUpdatePayload.accumulate("EmailID", inputJsonObject.getString("EmailID"));
				}else{
					// bpUpdatePayload.accumulate("EmailID", getBP.get("EmailID").getAsString());
					if(getBP.has("EmailID") && !getBP.get("EmailID").isJsonNull() && getBP.get("EmailID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("EmailID", getBP.get("EmailID").getAsString());
					else
						bpUpdatePayload.accumulate("EmailID", "");
				}
				
				if(inputJsonObject.has("PAN")){
					bpUpdatePayload.accumulate("PAN", inputJsonObject.getString("PAN"));
				}else{
					// bpUpdatePayload.accumulate("PAN", getBP.get("PAN").getAsString());
					if(getBP.has("PAN") && !getBP.get("PAN").isJsonNull() && getBP.get("PAN").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("PAN", getBP.get("PAN").getAsString());
					else
						bpUpdatePayload.accumulate("PAN", "");
				}
				
				if(inputJsonObject.has("GSTIN")){
					String gstn = inputJsonObject.getString("GSTIN");
					if(!gstn.equals("") || gstn!=null || !gstn.equalsIgnoreCase("null"))
						bpUpdatePayload.accumulate("GSTIN", inputJsonObject.getString("GSTIN"));
				}else{
					// bpUpdatePayload.accumulate("GSTIN", getBP.get("GSTIN").getAsString());
					if(getBP.has("GSTIN") && !getBP.get("GSTIN").isJsonNull() && getBP.get("GSTIN").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("GSTIN", getBP.get("GSTIN").getAsString());
					else
						bpUpdatePayload.accumulate("GSTIN", "");
				}
				
				if(inputJsonObject.has("StatusID")){
					bpUpdatePayload.accumulate("StatusID", inputJsonObject.getString("StatusID"));
				}else{
					// bpUpdatePayload.accumulate("StatusID", getBP.get("StatusID").getAsString());
					if(getBP.has("StatusID") && !getBP.get("StatusID").isJsonNull() && getBP.get("StatusID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("StatusID", getBP.get("StatusID").getAsString());
					else
						bpUpdatePayload.accumulate("StatusID", "");
				}
				
				if(inputJsonObject.has("ApproverRemarks")){
					bpUpdatePayload.accumulate("ApproverRemarks", inputJsonObject.getString("ApproverRemarks"));
				}else{
					// bpUpdatePayload.accumulate("ApproverRemarks", getBP.get("ApproverRemarks").getAsString());
					if(getBP.has("ApproverRemarks") && !getBP.get("ApproverRemarks").isJsonNull() && getBP.get("ApproverRemarks").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ApproverRemarks", getBP.get("ApproverRemarks").getAsString());
					else
						bpUpdatePayload.accumulate("ApproverRemarks", "");
				}
				
				if(inputJsonObject.has("MSME")){
					bpUpdatePayload.accumulate("MSME", inputJsonObject.getString("MSME"));
				}else{
					// bpUpdatePayload.accumulate("MSME", getBP.get("MSME").getAsString());
					if(getBP.has("MSME") && !getBP.get("MSME").isJsonNull() && getBP.get("MSME").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("MSME", getBP.get("MSME").getAsString());
					else
						bpUpdatePayload.accumulate("MSME", "");
				}
				
				if(inputJsonObject.has("UdyamRegNo")){
					bpUpdatePayload.accumulate("UdyamRegNo", inputJsonObject.getString("UdyamRegNo"));
				}else{
					// bpUpdatePayload.accumulate("UdyamRegNo", getBP.get("UdyamRegNo").getAsString());
					if(getBP.has("UdyamRegNo") && !getBP.get("UdyamRegNo").isJsonNull() && getBP.get("UdyamRegNo").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("UdyamRegNo", getBP.get("UdyamRegNo").getAsString());
					else
						bpUpdatePayload.accumulate("UdyamRegNo", "");
				}
				
				if(inputJsonObject.has("HgdFrgnCurrExposure")){
					if(debug)
						response.getWriter().println("IF HgdFrgnCurrExposure");
					// bpUpdatePayload.accumulate("HgdFrgnCurrExposure",inputJsonObject.getString("HgdFrgnCurrExposure"));
					if (!inputJsonObject.getString("HgdFrgnCurrExposure").equals("")
							&& inputJsonObject.getString("HgdFrgnCurrExposure") != null) {
						bpUpdatePayload.accumulate("HgdFrgnCurrExposure", inputJsonObject.getString("HgdFrgnCurrExposure"));
					}else{
						bpUpdatePayload.accumulate("HgdFrgnCurrExposure", JSONObject.NULL);
					}
				}else{
					if(debug)
						response.getWriter().println("Else HgdFrgnCurrExposure");
					// bpUpdatePayload.accumulate("HgdFrgnCurrExposure", getBP.get("HgdFrgnCurrExposure").getAsString());
					if(getBP.has("HgdFrgnCurrExposure") && !getBP.get("HgdFrgnCurrExposure").isJsonNull()){
						if(debug)
							response.getWriter().println("Else if HgdFrgnCurrExposure");
						bpUpdatePayload.accumulate("HgdFrgnCurrExposure", getBP.get("HgdFrgnCurrExposure").getAsString());
					}else{
						if(debug)
							response.getWriter().println("ElseElse HgdFrgnCurrExposure");
						bpUpdatePayload.accumulate("HgdFrgnCurrExposure", JSONObject.NULL);
					}
				}
				
				if(inputJsonObject.has("UnHgdFrgnCurrExposure")){
					// bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", inputJsonObject.getString("UnHgdFrgnCurrExposure"));
					if (!inputJsonObject.getString("UnHgdFrgnCurrExposure").equals("")
							&& inputJsonObject.getString("UnHgdFrgnCurrExposure") != null) {
						bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", inputJsonObject.getString("UnHgdFrgnCurrExposure"));
					}else{
						bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", JSONObject.NULL);
					}
				}else{
					// bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", getBP.get("UnHgdFrgnCurrExposure").getAsString());
					if(getBP.has("UnHgdFrgnCurrExposure") && !getBP.get("UnHgdFrgnCurrExposure").isJsonNull() && getBP.get("UnHgdFrgnCurrExposure").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", getBP.get("UnHgdFrgnCurrExposure").getAsString());
					else
						bpUpdatePayload.accumulate("UnHgdFrgnCurrExposure", JSONObject.NULL);
				}
				
				if(inputJsonObject.has("TotalFrgnCurrExposure")){
					// bpUpdatePayload.accumulate("TotalFrgnCurrExposure", inputJsonObject.getString("TotalFrgnCurrExposure"));
					if (!inputJsonObject.getString("TotalFrgnCurrExposure").equals("")
							&& inputJsonObject.getString("TotalFrgnCurrExposure") != null) {
						bpUpdatePayload.accumulate("TotalFrgnCurrExposure", inputJsonObject.getString("TotalFrgnCurrExposure"));
					}else{
						bpUpdatePayload.accumulate("TotalFrgnCurrExposure", JSONObject.NULL);
					}
				}else{
					// bpUpdatePayload.accumulate("TotalFrgnCurrExposure", getBP.get("TotalFrgnCurrExposure").getAsString());
					if(getBP.has("TotalFrgnCurrExposure") && !getBP.get("TotalFrgnCurrExposure").isJsonNull() && getBP.get("TotalFrgnCurrExposure").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("TotalFrgnCurrExposure", getBP.get("TotalFrgnCurrExposure").getAsString());
					else
						bpUpdatePayload.accumulate("TotalFrgnCurrExposure", JSONObject.NULL);
				}
				
				if(inputJsonObject.has("FundBasedExposure")){
					// bpUpdatePayload.accumulate("FundBasedExposure", inputJsonObject.getString("FundBasedExposure"));
					if (!inputJsonObject.getString("FundBasedExposure").equals("")
							&& inputJsonObject.getString("FundBasedExposure") != null) {
						bpUpdatePayload.accumulate("FundBasedExposure", inputJsonObject.getString("FundBasedExposure"));
					}else{
						bpUpdatePayload.accumulate("FundBasedExposure", JSONObject.NULL);
					}
				}else{
					// bpUpdatePayload.accumulate("FundBasedExposure", getBP.get("FundBasedExposure").getAsString());
					if(getBP.has("FundBasedExposure") && !getBP.get("FundBasedExposure").isJsonNull() && getBP.get("FundBasedExposure").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("FundBasedExposure", getBP.get("FundBasedExposure").getAsString());
					else
						bpUpdatePayload.accumulate("FundBasedExposure", JSONObject.NULL);
				}
				
				if(inputJsonObject.has("NonFundBasedExposure")){
					// bpUpdatePayload.accumulate("NonFundBasedExposure", inputJsonObject.getString("NonFundBasedExposure"));
					if (!inputJsonObject.getString("NonFundBasedExposure").equals("")
							&& inputJsonObject.getString("NonFundBasedExposure") != null) {
						bpUpdatePayload.accumulate("NonFundBasedExposure", inputJsonObject.getString("NonFundBasedExposure"));
					}else{
						bpUpdatePayload.accumulate("NonFundBasedExposure", JSONObject.NULL);
					}
				}else{
					// bpUpdatePayload.accumulate("NonFundBasedExposure", getBP.get("NonFundBasedExposure").getAsString());
					if(getBP.has("NonFundBasedExposure") && !getBP.get("NonFundBasedExposure").isJsonNull() && getBP.get("NonFundBasedExposure").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("NonFundBasedExposure", getBP.get("NonFundBasedExposure").getAsString());
					else
						bpUpdatePayload.accumulate("NonFundBasedExposure", JSONObject.NULL);
				}
				
				if(inputJsonObject.has("TotalBankingExposure")){
					// bpUpdatePayload.accumulate("TotalBankingExposure", inputJsonObject.getString("TotalBankingExposure"));
					if (!inputJsonObject.getString("TotalBankingExposure").equals("")
							&& inputJsonObject.getString("TotalBankingExposure") != null) {
						bpUpdatePayload.accumulate("TotalBankingExposure", inputJsonObject.getString("TotalBankingExposure"));
					}else{
						bpUpdatePayload.accumulate("TotalBankingExposure", JSONObject.NULL);
					}
				}else{
					// bpUpdatePayload.accumulate("TotalBankingExposure", getBP.get("TotalBankingExposure").getAsString());
					if(getBP.has("TotalBankingExposure") && !getBP.get("TotalBankingExposure").isJsonNull() && getBP.get("TotalBankingExposure").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("TotalBankingExposure", getBP.get("TotalBankingExposure").getAsString());
					else
						bpUpdatePayload.accumulate("TotalBankingExposure", JSONObject.NULL);
				}
				
				if(inputJsonObject.has("CorporateIdentificationNo")){
					bpUpdatePayload.accumulate("CorporateIdentificationNo", inputJsonObject.getString("CorporateIdentificationNo"));
				}else{
					// bpUpdatePayload.accumulate("CorporateIdentificationNo", getBP.get("CorporateIdentificationNo").getAsString());
					if(getBP.has("CorporateIdentificationNo") && !getBP.get("CorporateIdentificationNo").isJsonNull() && getBP.get("CorporateIdentificationNo").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CorporateIdentificationNo", getBP.get("CorporateIdentificationNo").getAsString());
					else
						bpUpdatePayload.accumulate("CorporateIdentificationNo", "");
				}
				
				/* if(inputJsonObject.has("CreatedOn")){
					bpUpdatePayload.accumulate("CreatedOn", inputJsonObject.getString("CreatedOn"));
				}else{ */
					// bpUpdatePayload.accumulate("CreatedOn", getBP.get("CreatedOn").getAsString());
					if(getBP.has("CreatedOn") && !getBP.get("CreatedOn").isJsonNull() && getBP.get("CreatedOn").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CreatedOn", getBP.get("CreatedOn").getAsString());
					else
						bpUpdatePayload.accumulate("CreatedOn", JSONObject.NULL);
				/* } */
				
				/* if(inputJsonObject.has("CreatedAt")){
					bpUpdatePayload.accumulate("CreatedAt", inputJsonObject.getString("CreatedAt"));
				}else{ */
					// bpUpdatePayload.accumulate("CreatedAt", getBP.get("CreatedAt").getAsString());
					if(getBP.has("CreatedAt") && !getBP.get("CreatedAt").isJsonNull() && getBP.get("CreatedAt").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CreatedAt", getBP.get("CreatedAt").getAsString());
					else
						bpUpdatePayload.accumulate("CreatedAt", JSONObject.NULL);
				/* } */
				
				/* if(inputJsonObject.has("CreatedBy")){
					bpUpdatePayload.accumulate("CreatedBy", inputJsonObject.getString("CreatedBy"));
				}else{ */
					// bpUpdatePayload.accumulate("CreatedBy", getBP.get("CreatedBy").getAsString());
					if(getBP.has("CreatedBy") && !getBP.get("CreatedBy").isJsonNull() && getBP.get("CreatedBy").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("CreatedBy", getBP.get("CreatedBy").getAsString());
					else
						bpUpdatePayload.accumulate("CreatedBy", "");
				/* } */

				if (inputJsonObject.has("FacilityType")) {
					if (!inputJsonObject.getString("FacilityType").equals("")
							&& inputJsonObject.getString("FacilityType") != null) {
						bpUpdatePayload.accumulate("FacilityType", inputJsonObject.getString("FacilityType"));
					}else{
						bpUpdatePayload.accumulate("FacilityType", "");
					}
				}else{
					// bpUpdatePayload.accumulate("FacilityType", getBP.get("FacilityType").getAsString());
					if(getBP.has("FacilityType") && !getBP.get("FacilityType").isJsonNull() && getBP.get("FacilityType").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("FacilityType", getBP.get("FacilityType").getAsString());
					else
						bpUpdatePayload.accumulate("FacilityType", "");
				}

				if (inputJsonObject.has("BPRejectionRemarks")) {
					if (!inputJsonObject.getString("BPRejectionRemarks").equals("")
							&& inputJsonObject.getString("BPRejectionRemarks") != null) {
						bpUpdatePayload.accumulate("BPRejectionRemarks", inputJsonObject.getString("BPRejectionRemarks"));
					}else{
						bpUpdatePayload.accumulate("BPRejectionRemarks", "");
					}
				}else{
					// bpUpdatePayload.accumulate("BPRejectionRemarks", getBP.get("BPRejectionRemarks").getAsString());
					if(getBP.has("BPRejectionRemarks") && !getBP.get("BPRejectionRemarks").isJsonNull() && getBP.get("BPRejectionRemarks").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("BPRejectionRemarks", getBP.get("BPRejectionRemarks").getAsString());
					else
						bpUpdatePayload.accumulate("BPRejectionRemarks", "");
				}

				if (inputJsonObject.has("LEINumber")) {
					if (!inputJsonObject.getString("LEINumber").equals("")
							&& inputJsonObject.getString("LEINumber") != null) {
						bpUpdatePayload.accumulate("LEINumber", inputJsonObject.getString("LEINumber"));
					}else{
						bpUpdatePayload.accumulate("LEINumber", "");
					}
				}else{
					// bpUpdatePayload.accumulate("LEINumber", getBP.get("LEINumber").getAsString());
					if(getBP.has("LEINumber") && !getBP.get("LEINumber").isJsonNull() && getBP.get("LEINumber").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("LEINumber", getBP.get("LEINumber").getAsString());
					else
						bpUpdatePayload.accumulate("LEINumber", "");
				}

				if (inputJsonObject.has("EntityID")) {
					if (!inputJsonObject.getString("EntityID").equals("")
							&& inputJsonObject.getString("EntityID") != null) {
						bpUpdatePayload.accumulate("EntityID", inputJsonObject.getString("EntityID"));
					}else{
						bpUpdatePayload.accumulate("EntityID", "");
					}
				}else{
					// bpUpdatePayload.accumulate("EntityID", getBP.get("EntityID").getAsString());
					if(getBP.has("EntityID") && !getBP.get("EntityID").isJsonNull() && getBP.get("EntityID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("EntityID", getBP.get("EntityID").getAsString());
					else
						bpUpdatePayload.accumulate("EntityID", "");
				}
				
				bpUpdatePayload.accumulate("ChangedBy", changedBy);
				bpUpdatePayload.accumulate("ChangedOn", "/Date("+changedOnInMillis+")/");
				bpUpdatePayload.accumulate("ChangedAt", changedAt);
				
				if (inputJsonObject.has("ParentNo") && !inputJsonObject.isNull("ParentNo")) {
					bpUpdatePayload.accumulate("ParentNo", inputJsonObject.getString("ParentNo"));
				}else{
					// bpUpdatePayload.accumulate("ParentNo", getBP.get("ParentNo").getAsString());
					if(getBP.has("ParentNo") && !getBP.get("ParentNo").isJsonNull() && getBP.get("ParentNo").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ParentNo", getBP.get("ParentNo").getAsString());
					else
						bpUpdatePayload.accumulate("ParentNo", "");
				}

				if (inputJsonObject.has("ParentTypeID") && !inputJsonObject.isNull("ParentTypeID")) {
					bpUpdatePayload.accumulate("ParentTypeID", inputJsonObject.getString("ParentTypeID"));
				}else{
					// bpUpdatePayload.accumulate("ParentTypeID", getBP.get("ParentTypeID").getAsString());
					if(getBP.has("ParentTypeID") && !getBP.get("ParentTypeID").isJsonNull() && getBP.get("ParentTypeID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ParentTypeID", getBP.get("ParentTypeID").getAsString());
					else
						bpUpdatePayload.accumulate("ParentTypeID", "");
				}

				if (inputJsonObject.has("ParentName") && !inputJsonObject.isNull("ParentName")) {
					bpUpdatePayload.accumulate("ParentName", inputJsonObject.getString("ParentName"));
				}else{
					// bpUpdatePayload.accumulate("ParentName", getBP.get("ParentName").getAsString());
					if(getBP.has("ParentName") && !getBP.get("ParentName").isJsonNull() && getBP.get("ParentName").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ParentName", getBP.get("ParentName").getAsString());
					else
						bpUpdatePayload.accumulate("ParentName", "");
				}
				
				if(inputJsonObject.has("ERP_CPName") && !inputJsonObject.isNull("ERP_CPName")) {
					bpUpdatePayload.accumulate("ERP_CPName", inputJsonObject.getString("ERP_CPName"));
				}else{
					// bpUpdatePayload.accumulate("ERP_CPName", getBP.get("ERP_CPName").getAsString());
					if(getBP.has("ERP_CPName") && !getBP.get("ERP_CPName").isJsonNull() && getBP.get("ERP_CPName").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ERP_CPName", getBP.get("ERP_CPName").getAsString());
					else
						bpUpdatePayload.accumulate("ERP_CPName", "");
				}
				
				if(inputJsonObject.has("URCEntityType") && !inputJsonObject.isNull("URCEntityType")) {
					bpUpdatePayload.accumulate("URCEntityType", inputJsonObject.getString("URCEntityType"));
				}else{
					// bpUpdatePayload.accumulate("URCEntityType", getBP.get("URCEntityType").getAsString());
					if(getBP.has("URCEntityType") && !getBP.get("URCEntityType").isJsonNull() && getBP.get("URCEntityType").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCEntityType", getBP.get("URCEntityType").getAsString());
					else
						bpUpdatePayload.accumulate("URCEntityType", "");
				}
				
				if (inputJsonObject.has("URCActivityType") && !inputJsonObject.isNull("URCActivityType")) {
					bpUpdatePayload.accumulate("URCActivityType", inputJsonObject.getString("URCActivityType"));
				}else{
					// bpUpdatePayload.accumulate("URCActivityType", getBP.get("URCActivityType").getAsString());
					if(getBP.has("URCActivityType") && !getBP.get("URCActivityType").isJsonNull() && getBP.get("URCActivityType").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCActivityType", getBP.get("URCActivityType").getAsString());
					else
						bpUpdatePayload.accumulate("URCActivityType", "");
				}
				
				if (inputJsonObject.has("URCSectorCode") && !inputJsonObject.isNull("URCSectorCode")) {
					bpUpdatePayload.accumulate("URCSectorCode", inputJsonObject.getString("URCSectorCode"));
				}else{
					// bpUpdatePayload.accumulate("URCSectorCode", getBP.get("URCSectorCode").getAsString());
					if(getBP.has("URCSectorCode") && !getBP.get("URCSectorCode").isJsonNull() && getBP.get("URCSectorCode").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCSectorCode", getBP.get("URCSectorCode").getAsString());
					else
						bpUpdatePayload.accumulate("URCSectorCode", "");
				}
				
				if (inputJsonObject.has("URCSubSectorCode") && !inputJsonObject.isNull("URCSubSectorCode")) {
					bpUpdatePayload.accumulate("URCSubSectorCode", inputJsonObject.getString("URCSubSectorCode"));
				}else{
					// bpUpdatePayload.accumulate("URCSubSectorCode", getBP.get("URCSubSectorCode").getAsString());
					if(getBP.has("URCSubSectorCode") && !getBP.get("URCSubSectorCode").isJsonNull() && getBP.get("URCSubSectorCode").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCSubSectorCode", getBP.get("URCSubSectorCode").getAsString());
					else
						bpUpdatePayload.accumulate("URCSubSectorCode", "");
				}
				
				if (inputJsonObject.has("URCDocURL") && !inputJsonObject.isNull("URCDocURL")) {
					bpUpdatePayload.accumulate("URCDocURL", inputJsonObject.getString("URCDocURL"));
				}else{
					// bpUpdatePayload.accumulate("URCDocURL", getBP.get("URCDocURL").getAsString());
					if(getBP.has("URCDocURL") && !getBP.get("URCDocURL").isJsonNull() && getBP.get("URCDocURL").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCDocURL", getBP.get("URCDocURL").getAsString());
					else
						bpUpdatePayload.accumulate("URCDocURL", "");
				}
				
				if (inputJsonObject.has("ODAccountNo") && !inputJsonObject.isNull("ODAccountNo")) {
					bpUpdatePayload.accumulate("ODAccountNo", inputJsonObject.getString("ODAccountNo"));
				}else{
					// bpUpdatePayload.accumulate("ODAccountNo", getBP.get("ODAccountNo").getAsString());
					if(getBP.has("ODAccountNo") && !getBP.get("ODAccountNo").isJsonNull() && getBP.get("ODAccountNo").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("ODAccountNo", getBP.get("ODAccountNo").getAsString());
					else
						bpUpdatePayload.accumulate("ODAccountNo", "");
				}
				
				if (inputJsonObject.has("URCRegistrationDate") && !inputJsonObject.isNull("URCRegistrationDate")) {
					if(debug)
						response.getWriter().println("URCRegistrationDate IF");
					bpUpdatePayload.accumulate("URCRegistrationDate", inputJsonObject.getString("URCRegistrationDate"));
				}else{
					if(debug)
						response.getWriter().println("URCRegistrationDate ELSE");
					// bpUpdatePayload.accumulate("URCRegistrationDate", getBP.get("URCRegistrationDate").getAsString());
					if(getBP.has("URCRegistrationDate") && !getBP.get("URCRegistrationDate").isJsonNull() && getBP.get("URCRegistrationDate").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("URCRegistrationDate", getBP.get("URCRegistrationDate").getAsString());
					else
						bpUpdatePayload.accumulate("URCRegistrationDate", "");
				}

				if (inputJsonObject.has("EntityType")) {
					if (!inputJsonObject.getString("EntityType").equals("")
							&& inputJsonObject.getString("EntityType") != null) {
						bpUpdatePayload.accumulate("EntityType", inputJsonObject.getString("EntityType"));
					}else{
						bpUpdatePayload.accumulate("EntityType", "");
					}
				}else{
					// bpUpdatePayload.accumulate("EntityType", getBP.get("EntityType").getAsString());
					if(getBP.has("EntityType") && !getBP.get("EntityType").isJsonNull() && getBP.get("EntityType").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("EntityType", getBP.get("EntityType").getAsString());
					else
						bpUpdatePayload.accumulate("EntityType", "");
				}

				if (inputJsonObject.has("Source")) {
					if (!inputJsonObject.getString("Source").equals("")
							&& inputJsonObject.getString("Source") != null) {
						bpUpdatePayload.accumulate("Source", inputJsonObject.getString("Source"));
					}else{
						bpUpdatePayload.accumulate("Source", "");
					}
				}else{
					/* if(null != getBP.get("Source").getAsString() && getBP.get("Source").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Source", getBP.get("Source").getAsString());
					else
						bpUpdatePayload.accumulate("Source", ""); */
					
					if(getBP.has("Source") && !getBP.get("Source").isJsonNull() && getBP.get("Source").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("Source", getBP.get("Source").getAsString());
					else
						bpUpdatePayload.accumulate("Source", "");
				}

				if (inputJsonObject.has("SourceReferenceID")) {
					if (!inputJsonObject.getString("SourceReferenceID").equals("")
							&& inputJsonObject.getString("SourceReferenceID") != null) {
						bpUpdatePayload.accumulate("SourceReferenceID", inputJsonObject.getString("SourceReferenceID"));
					}else{
						bpUpdatePayload.accumulate("SourceReferenceID", "");
					}
				}else{
					/* if(null != getBP.get("SourceReferenceID").getAsString() && getBP.get("SourceReferenceID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("SourceReferenceID", getBP.get("Source").getAsString());
					else
						bpUpdatePayload.accumulate("SourceReferenceID", ""); */
					if(getBP.has("SourceReferenceID") && !getBP.get("SourceReferenceID").isJsonNull() && getBP.get("SourceReferenceID").getAsString().trim().length() > 0)
						bpUpdatePayload.accumulate("SourceReferenceID", getBP.get("SourceReferenceID").getAsString());
					else
						bpUpdatePayload.accumulate("SourceReferenceID", "");
				}

				executeURL = oDataURL+"BPHeader('"+bpID+"')";
				
				if(debug){
					response.getWriter().println("executeUpdate-bpUpdatePayload: "+bpUpdatePayload);
				}
				
				requestEntity = new StringEntity(bpUpdatePayload.toString());
				
				String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
				if(debug){
					response.getWriter().println("executeUpdate.executeURL: "+ executeURL);
					response.getWriter().println("executeUpdate.userName: "+ userName);
					response.getWriter().println("executeUpdate.password: "+ password);
					response.getWriter().println("executeUpdate.basicAuth: "+ basicAuth);
				}
				
				/* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
				
				httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build(); */
				HttpPut updateRequest = new HttpPut(executeURL);
				updateRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
				updateRequest.setHeader("Content-Type", "application/json");
				updateRequest.setHeader("Accept", "application/json");
				updateRequest.setHeader("X-HTTP-Method", "PUT");
				updateRequest.setEntity(requestEntity);
				
				// HttpResponse httpResponse = httpClient.execute(updateRequest);
				HttpResponse httpResponse = client.execute(updateRequest);
				responseEntity = httpResponse.getEntity();
				String returnVal="";
				
				if(debug){
					response.getWriter().println("executeUpdate.getStatusCode: "+httpResponse.getStatusLine().getStatusCode());
				}
				
				JSONObject responseJsonObject = new JSONObject();
				if(debug)
					response.getWriter().println("responseJsonObject: "+responseJsonObject);
				try{
					responseJsonObject = new JSONObject(EntityUtils.toString(responseEntity));
					
					if(responseJsonObject.has("error")){
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(responseJsonObject);
					}else{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						response.getWriter().println(responseJsonObject);
						// response.getWriter().println("responseJsonObject: "+responseJsonObject);
					}
				}catch (Exception e) {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().println(responseJsonObject);
					/* response.getWriter().println("responseJsonObject: "+responseJsonObject);
					if (debug) {
						StackTraceElement element[] = e.getStackTrace();
						StringBuffer buffer = new StringBuffer();
						for (int i = 0; i < element.length; i++) {
							buffer.append(element[i]);
						}
						response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
					} */
				}
			}else{
				/* if (debug) {
					StackTraceElement element[] = e.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < element.length; i++) {
						buffer.append(element[i]);
					}
					response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
				} */

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("Status", "000002");
				result.addProperty("Message", "ID is Mandatory for updating");
				response.getWriter().println(new Gson().toJson(result));
			}
		}catch (Exception e) {
			if (debug) {
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
				System.out.println("Full Stack Trace:" + buffer.toString());
			}

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonObject result = new JsonObject();
			result.addProperty("ErrorCode", "J001");
			result.addProperty("Status", "000002");
			result.addProperty("Message", e.getMessage());
			response.getWriter().println(new Gson().toJson(result));
		}finally{
			// httpClient.close();
		}
	}
}


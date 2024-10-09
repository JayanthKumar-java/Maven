package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

@Configuration
public class AccountsMIS2 extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		boolean debug = false;

		//	--------------------------------  change document ---------------------------------- //


		// String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/"+"ChangeDocumentItems"+"?$filter=ObjectClass%20eq%20%27" +""+ "%27%20and%20ObjectValue%20eq%20%27"+ "2034-9474-jfhf-9484f-9844" +
        //           "%27%20and%20DocChangeNo%20eq%20%27"+"1000000335"+"%27%20and%20TableName%20eq%20%27"+"Surveys"+"%27%20and%20ChangedTablekey%20eq%20%27"+"2034-9474-jfhf-9484f-9844"+
		// 		  "%27%20and%20FieldName%20eq%20%27"+"ActivityType"+"%27%20and%20ChangeInd%20eq%20%27"+"U"+"%27";

		// 	executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/"+"ChangeDocumentHeader"+"?$filter=ObjectClass%20eq%20%27"
		// 	+""+"%27%20and%20ObjectValue%20eq%20%27"+"09FD72D4-525C-4B42-A10E-D567F6291A2A"+"%27%20and%20DocChangeNo%20eq%20%27"+"1000000167"+"%27";
		// 		  String userpass1 = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";
        // JsonObject emailResponse1 = executeURL(executeUrl, userpass1, resp, debug);
		// resp.getWriter().println(emailResponse1);

		// String tcdobUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentHeader" + "?$select=ObjectClass" + "&$filter=DocChangeNo%20eq%20%27" + "0682" + "%27%20and%20AggregatorID%20eq%20%27" + "AGGRNEL" + "%27%20and%20ObjectValue%20eq%20%27" + "e21f8975-3ce4-42a6-acfb-51ce49669e95" + "%27";

		// JsonObject tcdobObj = executeURL(tcdobUrl, "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM", resp, debug);
		// resp.getWriter().println(tcdobObj);

		// JsonArray tcdobObjArray = tcdobObj.get("d").getAsJsonObject().get("results")
		// 				.getAsJsonArray();

		// 				for (JsonElement element : tcdobObjArray) {
		// 								JsonObject jsonObject = element.getAsJsonObject();
		// 								String ObjectClass = jsonObject.get("ObjectClass").getAsString();
		// 								resp.getWriter().println("ObjectClass "+ObjectClass);
		// 				}


		// String url1 = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentItems";
		// String userpass1 = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";

		// String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com:443/SCHARTEC/ARTEC/PCGW/service.xsodata/"+"ChangeDocumentItems"+"?$filter=ObjectClass%20eq%20%27" + ""+ "%27%20and%20ObjectValue%20eq%20%27"+ "2034-9474-jfhf-9484f-9844" +
        //           "%27%20and%20DocChangeNo%20eq%20%27"+"1000000335"+"%27%20and%20TableName%20eq%20%27"+"Surveys"+"%27%20and%20ChangedTablekey%20eq%20%27"+"2034-9474-jfhf-9484f-9844"+
		// 		  "%27%20and%20FieldName%20eq%20%27"+"ActivityRefID"+"%27%20and%20ChangeInd%20eq%20"+"U"+"%27";
		// JsonObject emailResponse1 = executeURL(executeUrl, userpass1, resp, debug);
		// resp.getWriter().println(emailResponse1);

	// 	if(emailResponse1!=null){
	// 		JsonArray emailResponse1Array = emailResponse1.get("d").getAsJsonObject().get("results")
	// 					.getAsJsonArray();
	// 		for (JsonElement element : emailResponse1Array) {
	// 			JsonObject jsonObject = element.getAsJsonObject();
	// 		String ObjectClass = jsonObject.get("ObjectClass").getAsString();
	// 		String ObjectValue = jsonObject.get("ObjectValue").getAsString();
	// 		String DocChangeNo = jsonObject.get("DocChangeNo").getAsString();
	// 		String TableName = jsonObject.get("TableName").getAsString();
	// 		String ChangedTablekey = jsonObject.get("ChangedTablekey").getAsString();
	// 		String FieldName = jsonObject.get("FieldName").getAsString();
	// 		String ChangeInd = jsonObject.get("ChangeInd").getAsString();
	// 		resp.getWriter().println("ObjectClass "+ObjectClass);
	// 		resp.getWriter().println("ObjectValue "+ObjectValue);
	// 		resp.getWriter().println("DocChangeNo "+DocChangeNo);
	// 		resp.getWriter().println("TableName "+TableName);
	// 		resp.getWriter().println("ChangedTablekey "+ChangedTablekey);
	// 		resp.getWriter().println("FieldName "+FieldName);
	// 		resp.getWriter().println("ChangeInd "+ChangeInd);
			
	// 		if(ObjectClass!=null || ObjectValue!=null || DocChangeNo!=null){
	// 			resp.getWriter().println("deleting");
	// 	// String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com:443/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentItems(ObjectClass='"+ObjectClass+"',ObjectValue='"+ObjectValue+"',DocChangeNo='"+DocChangeNo+"',TableName='"+TableName+"',ChangedTablekey='"+ChangedTablekey+"',FieldName='"+FieldName+"',ChangeInd='"+ChangeInd+"'))";
	// 	String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com:443/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentItems("
	// 	+ "ObjectClass='" + ObjectClass + "',"
	// 	+ "ObjectValue='" + ObjectValue + "',"
	// 	+ "DocChangeNo='" + DocChangeNo + "',"
	// 	+ "TableName='" + TableName + "',"
	// 	+ "ChangedTablekey='" + ChangedTablekey + "',"
	// 	+ "FieldName='" + FieldName + "',"
	// 	+ "ChangeInd='" + ChangeInd + "')";
	// 	String userpasses = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";
	// 	String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpasses.getBytes());

	// 	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	// 	UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("USR_SCHARTEC_SRVUSR",
	// 			"DevCFHNDBP7ONq2XoBvkNL7uzMwdFeb2024");
	// 	credentialsProvider.setCredentials(AuthScope.ANY, credentials);
	// 	resp.getWriter().println("credentialsProvider "+credentialsProvider);
	// 	CloseableHttpClient  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
	// 	resp.getWriter().println("httpClient "+httpClient);
	// 	HttpDelete deleteRequest = new HttpDelete(executeUrl);
	// 	deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	// 	deleteRequest.setHeader("Content-Type", "application/json");
	// 	deleteRequest.setHeader("Accept", "application/json");
	// 	deleteRequest.setHeader("X-HTTP-Method", "DELETE");
	// 	resp.getWriter().println("deleteRequest");
	// 	HttpResponse httpResponse = httpClient.execute(deleteRequest);
	// 	resp.getWriter().println("httpResponse.getStatusLine().getStatusCode() "+httpResponse.getStatusLine().getStatusCode());
	// 	if (httpResponse.getStatusLine().getStatusCode() == 204) {
	// 		resp.getWriter().println("deleted");

	// 	} else if (httpResponse.getStatusLine().getStatusCode() == 404) {
	// 		resp.getWriter().println("Resources not found for this:");
	// 	} else {
	// 		// responseEntity = httpResponse.getEntity();
	// 	}
	// }
	// }
	// }


	// String tdposUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/NR/service.xsodata/ChangeDocumentNumberRange";
	// JsonObject tdposUrlResponse1 = executeURL(tdposUrl, userpass1, resp, debug);
	// resp.getWriter().println("tdposUrlResponse1 :"+tdposUrlResponse1);

	// if(tdposUrlResponse1!=null){
	// 	JsonArray tdposUrlResponse1Array = tdposUrlResponse1.get("d").getAsJsonObject().get("results")
	// 				.getAsJsonArray();
	// 				for (JsonElement element : tdposUrlResponse1Array) {
	// 					JsonObject jsonObject = element.getAsJsonObject();
	// 				String ChangeDocumentNumberRange = jsonObject.get("ChangeDocumentNumberRange").getAsString();
	// 				resp.getWriter().println("ChangeDocumentNumberRange :"+ChangeDocumentNumberRange);
	// 				}
	// }

	// String tcdobUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/"+"ChangeDocCreateObjects" + "?$select=ObjectClass" + "&$filter=AggregatorID%20eq%20%27" + "Aggr008" + "%27%20and%20TableName%20eq%20%27" + "CDPOS" + "%27";
	// JsonObject tcdobUrlResponse1 = executeURL(tcdobUrl, userpass1, resp, debug);
	// resp.getWriter().println("tcdobUrlResponse1 :"+tcdobUrlResponse1);

	// if(tcdobUrlResponse1!=null){
	// 	JsonArray tcdobResponse1Array = tcdobUrlResponse1.get("d").getAsJsonObject().get("results")
	// 				.getAsJsonArray();
	// 				for (JsonElement element : tcdobResponse1Array) {
	// 					JsonObject jsonObject = element.getAsJsonObject();
	// 				String ObjectClass = jsonObject.get("ObjectClass").getAsString();
	// 				resp.getWriter().println("ObjectClass :"+ObjectClass);
	// 				}
	// }

	// String urlMetadata = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/$metadata";
	// JsonObject metadatResponse = executeURL(urlMetadata, userpass1, resp, debug);
	// resp.getWriter().println("metadatResponse :"+metadatResponse);
	

	// String url2 = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentHeader";
	// String userpass2 = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";
	// JsonObject emailResponse2 = executeURL(url2, userpass2, resp, debug);

	// if(emailResponse2!=null){
	// 	JsonArray emailResponse1Array = emailResponse2.get("d").getAsJsonObject().get("results")
	// 				.getAsJsonArray();
	// 	for (JsonElement element : emailResponse1Array) {
	// 		JsonObject jsonObject = element.getAsJsonObject();
	// 	String ObjectClass = jsonObject.get("ObjectClass").getAsString();
	// 	String ObjectValue = jsonObject.get("ObjectValue").getAsString();
	// 	String DocChangeNo = jsonObject.get("DocChangeNo").getAsString();
		
	// 	resp.getWriter().println("ObjectClass "+ObjectClass);
	// 	resp.getWriter().println("ObjectValue "+ObjectValue);
	// 	resp.getWriter().println("DocChangeNo "+DocChangeNo);
		
	// 	if(ObjectClass!=null || ObjectValue!=null || DocChangeNo!=null){
	// 		resp.getWriter().println("deleting");
	// // String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com:443/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentItems(ObjectClass='"+ObjectClass+"',ObjectValue='"+ObjectValue+"',DocChangeNo='"+DocChangeNo+"',TableName='"+TableName+"',ChangedTablekey='"+ChangedTablekey+"',FieldName='"+FieldName+"',ChangeInd='"+ChangeInd+"'))";
	// String executeUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com:443/SCHARTEC/ARTEC/PCGW/service.xsodata/ChangeDocumentHeader("
	// + "ObjectClass='" + ObjectClass + "',"
	// + "ObjectValue='" + ObjectValue + "',"
	// + "DocChangeNo='" + DocChangeNo + "',";
	// String userpasses = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";
	// 	String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpasses.getBytes());

	// 	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	// 	UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("USR_SCHARTEC_SRVUSR",
	// 			"DevCFHNDBP7ONq2XoBvkNL7uzMwdFeb2024");
	// 	credentialsProvider.setCredentials(AuthScope.ANY, credentials);
	// 	resp.getWriter().println("credentialsProvider "+credentialsProvider);
	// 	CloseableHttpClient  httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
	// 	resp.getWriter().println("httpClient "+httpClient);
	// 	HttpDelete deleteRequest = new HttpDelete(executeUrl);
	// 	deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	// 	deleteRequest.setHeader("Content-Type", "application/json");
	// 	deleteRequest.setHeader("Accept", "application/json");
	// 	deleteRequest.setHeader("X-HTTP-Method", "DELETE");
	// 	resp.getWriter().println("deleteRequest");
	// 	HttpResponse httpResponse = httpClient.execute(deleteRequest);
	// 	resp.getWriter().println("httpResponse.getStatusLine().getStatusCode() "+httpResponse.getStatusLine().getStatusCode());
	// 	if (httpResponse.getStatusLine().getStatusCode() == 204) {
	// 		resp.getWriter().println("deleted");

	// 	} else if (httpResponse.getStatusLine().getStatusCode() == 404) {
	// 		resp.getWriter().println("Resources not found for this:");
	// 	} else {
	// 		// responseEntity = httpResponse.getEntity();
	// 	}
	// }
	// }
	// }

	// try {
	// 	String key = getAllKeyProperty(resp , metadataString, "ENHLMTType" , debug);
	// 	resp.getWriter().println("metadataString key : " + key);
	// 	String payload = "[\n" +
	// 	"    {\n" +
	// 	"        \"aggrID\": \"AGGRNEL\",\n" +
	// 	"        \"Service\": \"PCGW\",\n" +
	// 	"        \"entityName\": \"DeliveryHdrs\",\n" +
	// 	"        \"oldPayload\": \"{ \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"DeliveryNo\\\": \\\"1110\\\", \\\"Items\\\":[ { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F629123A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000010\\\", \\\"Quantity\\\": \\\"10\\\" }, { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291345A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000020\\\", \\\"Quantity\\\": \\\"20\\\" } ] }\",\n" +
	// 	"        \"newPayload\": \"{ \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2B\\\", \\\"AggregatorID\\\": \\\"AGGRNES\\\", \\\"DeliveryNo\\\": \\\"11101\\\", \\\"Items\\\":[ { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F629123A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000010\\\", \\\"Quantity\\\": \\\"10\\\" }, { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291345A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000020\\\", \\\"Quantity\\\": \\\"20\\\" } ] }\"\n" +
	// 	"    },\n" +
	// 	"    {\n" +
	// 	"        \"aggrID\": \"AGGRNEL\",\n" +
	// 	"        \"Service\": \"PCGW\",\n" +
	// 	"        \"entityName\": \"DeliveryHdrs\",\n" +
	// 	"        \"oldPayload\": \"{ \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"DeliveryNo\\\": \\\"1110\\\", \\\"Items\\\":[ { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F629123A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000010\\\", \\\"Quantity\\\": \\\"10\\\" }, { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291345A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000020\\\", \\\"Quantity\\\": \\\"20\\\" } ] }\",\n" +
	// 	"        \"newPayload\": \"{ \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"DeliveryNo\\\": \\\"1110\\\", \\\"Items\\\":[ { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F629123A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGRNEL\\\", \\\"ItemNo\\\": \\\"000010\\\", \\\"Quantity\\\": \\\"10\\\" }, { \\\"DeliveryITEMGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291345A\\\", \\\"DeliveryGUID\\\": \\\"09FD72D4-525C-4B42-A10E-D567F6291A2A\\\", \\\"AggregatorID\\\": \\\"AGGR008\\\", \\\"ItemNo\\\": \\\"000020\\\", \\\"Quantity\\\": \\\"20\\\" } ] }\"\n" +
	// 	"    }\n" +
	// 	"]";
	// 	JsonArray jsonArray = JsonParser.parseString(payload).getAsJsonArray();

	// 	Iterate through each JsonObject in the JsonArray
	// 	for (JsonElement element : jsonArray) {
	// 		JsonObject jsonObject = element.getAsJsonObject();

	// 		if(jsonObject.has("oldPayload") && !jsonObject.get("oldPayload").isJsonNull() && !jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")
	// 					&& jsonObject.has("newPayload") && !jsonObject.get("newPayload").isJsonNull() && !jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase("")){
	// 				String oldPayloadStr = jsonObject.get("oldPayload").getAsString();
	// 				String newPayloadStr = jsonObject.get("newPayload").getAsString();

	// 				JsonObject oldPayload = parseJsonString(oldPayloadStr);
	// 				JsonObject newPayload = parseJsonString(newPayloadStr);

	// 				postToCDHdr(resp, jsonObject.get("aggrID").getAsString(),cdhdrUrl,cdHdrUserPass ,oldPayload, newPayload ,request ,debug);

	// 			}else if (( (jsonObject.has("oldPayload")) &&  (jsonObject.get("oldPayload").isJsonNull() ||  jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")))
	// 					&& jsonObject.has("newPayload") && !jsonObject.get("newPayload").isJsonNull() && !jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase("")) {
							
	// 				resp.getWriter().println("2");
					
	// 			}else if(( (jsonObject.has("oldPayload")) &&  (!jsonObject.get("oldPayload").isJsonNull() &&  !jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")))
	// 					&& jsonObject.has("newPayload") && (jsonObject.get("newPayload").isJsonNull() || jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase("")) ){
	// 						resp.getWriter().println("3");
	// 					}
	// 	}

	// } catch (Exception e) {
	// 	// TODO Auto-generated catch block
	// 	e.printStackTrace();
	// }
	//	--------------------------------  change document ---------------------------------- //



      //	--------------------------------  SendPortalURL ---------------------------------- //
	
		// if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
		// 	debug = true;
		// }
		

		String url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/D0017/ARTEC/PSGW/service.xsodata/VendorOnBoardingList?$filter=BusinessPartnerGUID%20eq%20%279CCE22D6-7CB5-4110-8C9E-BFE54F768821%27";
		String userpass = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";

		// url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PSGW/service.xsodata/VendorOnBoardingList?$filter=BusinessPartnerGUID%20eq%20%2700EDF32D-76D5-460D-8BB0-E26802324851%27";
		
		JsonObject VendorOnBoardingListResponse = executeURL(url, userpass, resp, debug);
		resp.getWriter().println("VendorOnBoardingListResponse "+VendorOnBoardingListResponse);

		JsonArray VendorOnBoardingListResponseArray = VendorOnBoardingListResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		// resp.getWriter().println("VendorOnBoardingListResponseArray "+VendorOnBoardingListResponseArray);

		JsonObject vendorObject = VendorOnBoardingListResponseArray.get(0).getAsJsonObject();
		resp.getWriter().println("vendorObject "+vendorObject);
		vendorObject.addProperty("PortalURL", "portalUrl");

		String aggrId = vendorObject.get("AggregatorID").getAsString();
		String reqType = vendorObject.get("RequestType").getAsString();
		String companyCode = vendorObject.get("CompanyCode").getAsString();
		String vendorCategory = vendorObject.get("VendorCategory").getAsString();
		String vendorType = vendorObject.get("VendorType").getAsString();
		resp.getWriter().println("aggrId "+aggrId);
		resp.getWriter().println("reqType "+reqType);
		resp.getWriter().println("companyCode "+companyCode);
		resp.getWriter().println("vendorCategory "+vendorCategory);
		resp.getWriter().println("vendorType "+vendorType);

		url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/D0017/ARTEC/PCGW/service.xsodata/WFStrategy?$filter=AggregatorID%20eq%20%27"+aggrId+"%27%20and%20Application%20eq%20%27"+"PS"+"%27%20and%20EntityType%20eq%20%27"+"VOBLINKMAIL"+"%27%20and%20Attribute1%20eq%20%27"+reqType+"%27%20and%20Attribute2%20eq%20%27"+companyCode+"%27%20and%20Attribute3%20eq%20%27"+vendorCategory+"%27%20and%20Attribute4%20eq%20%27"+vendorType+"%27";
		userpass = "USR_D0017_SRVUSR:Sb7cGHqCHn2NtKfV";
		JsonObject WFStrategyResponse = executeURL(url, userpass, resp, debug);
		resp.getWriter().println("WFStrategyResponse "+WFStrategyResponse);

		JsonArray WFStrategyResponseArray = WFStrategyResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		resp.getWriter().println("WFStrategyResponseArray "+WFStrategyResponseArray);

		JsonObject WFStrategyResponseObject = WFStrategyResponseArray.get(0).getAsJsonObject();
		resp.getWriter().println("WFStrategyResponseObject "+WFStrategyResponseObject);

		String strategy = WFStrategyResponseObject.get("Strategy").getAsString();
		resp.getWriter().println("strategy "+strategy);

		url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/D0017/ARTEC/PCGW/service.xsodata/EntityAttributes?$filter=AggregatorID%20eq%20%27"+aggrId+"%27%20and%20EntityType%20eq%20%27"+"VOBLINKMAIL"+"%27%20and%20Attribute1%20eq%20%27"+strategy+"%27";
		JsonObject EntityAttributesResponse = executeURL(url, userpass, resp, debug);
		resp.getWriter().println("EntityAttributesResponse "+EntityAttributesResponse);

		JsonArray EntityAttributesResponseArray = EntityAttributesResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		
			resp.getWriter().println("EntityAttributesResponseArray "+EntityAttributesResponseArray);
		
		JsonObject EntityAttributesObject = EntityAttributesResponseArray.get(0).getAsJsonObject();
		
			resp.getWriter().println("EntityAttributesObject "+EntityAttributesObject);
		
		String template = EntityAttributesObject.get("Value").getAsString();
		
			resp.getWriter().println("template "+template);
		


		url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/D0017/ARTEC/PCGW/service.xsodata/ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27"+aggrId+"%27%20and%20Typeset%20eq%20%27VNDMIL%27";
		JsonObject configResponse = executeURL(url, userpass, resp, debug);
		resp.getWriter().println("configResponse "+configResponse);

		JsonArray configResponseArray = configResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		resp.getWriter().println("WFStrategyResponseArray "+WFStrategyResponseArray);

		int j=1;
		for(int i=0;i<=configResponseArray.size()-1;i++){
			
		JsonObject configResponseObject = configResponseArray.get(i).getAsJsonObject();
		resp.getWriter().println("configResponseObject "+configResponseObject);
		String types = configResponseObject.get("Types").getAsString();
		resp.getWriter().println("types "+types);

		resp.getWriter().println("types value"+configResponseObject.get("TypeValue").getAsString());
			if(template.contains("&"+types)){
				String typeValue = configResponseObject.get("TypeValue").getAsString();
				if(j==1){
				template =template.replace("&"+types, vendorObject.get(typeValue).getAsString());
				}else if(j==2){
					template = template.replace("&"+types, vendorObject.get(typeValue).getAsString());
				}else if(j==3){
					template = template.replace("&"+types, vendorObject.get(typeValue).getAsString());
				}
			}
			j++;
			resp.getWriter().println("template "+template);
		}
		resp.getWriter().println("template "+template);
		


		//  url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PSGW/service.xsodata/"+"VendorRequest?$filter=BusinessPartnerGUID%20eq%20%27" +"48fdb968-b03f-4379-8bde-0f09a6e44d64" + "%27";
		//  userpass = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";
		//  JsonObject psgwResponse = executeURL(url, userpass, resp, debug);
		// resp.getWriter().println("psgwResponse "+psgwResponse);

		String pygwUrl = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/";

		String userPass = "P000000:DevCFHNDBP@$$wdFeb2024";

		// url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/$metadata";
		// StringBuffer metadataString = executeMetadata(debug ,url, userPass, resp);
		// resp.getWriter().println("metadataString " + metadataString);

		// String cdhdrUrl = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/";
		// String cdHdrUserPass = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";





        //	--------------------------------  SendPortalURL ---------------------------------- //




		//	--------------------------------  AccountMIS ---------------------------------- //

		// JsonObject sccnfgEntries = getSccNfgEntries(resp, pygwUrl , userPass ,debug);
		// // resp.getWriter().println("sccnfgEntries "+sccnfgEntries);

		// JsonObject aggregatorsEntries = getAggregatorsEntries(request, resp, debug);
		// // resp.getWriter().println("aggregatorsEntries "+aggregatorsEntries);

		// HashMap<String, String> inputForScfCorpDealerMap = new HashMap<>();
		// inputForScfCorpDealerMap = getMapForInputToScfCorpDealer(request, resp, sccnfgEntries, aggregatorsEntries,debug);
		// // resp.getWriter().println("inputsForScfCorpDealer "+inputForScfCorpDealerMap);

        // JsonArray scfCorpDealerResponse=callScfCorpDealer(request, resp, inputForScfCorpDealerMap, debug);
		// // resp.getWriter().println("scfCorpDealerResponse "+scfCorpDealerResponse);

		// JsonObject scfEntries = getScfEntries(request, resp, debug);
		// // resp.getWriter().println("scfEntries "+scfEntries);

		// JsonObject bpEntries = getBpEntries(request, resp, debug);
		// // resp.getWriter().println("bpEntries "+bpEntries);

		// JsonObject scfEntriesAfterConcatination = concatinateFieldsinScf(request, resp, scfEntries, debug);
		// // resp.getWriter().println("scfEntriesAfterConcatination"+scfEntriesAfterConcatination);

		// JsonObject bpEntriesAfterConcatination = concatinateFieldsinBP(request, resp, bpEntries, debug);
		// // resp.getWriter().println("bpEntriesAfterConcatination "+bpEntriesAfterConcatination);

		// JsonArray OnboardedOtherFacilitiesEntries = pushMatchedRecordsToOnoboardOtherfacility(request, resp ,scfEntriesAfterConcatination, bpEntriesAfterConcatination);
		// // resp.getWriter().println("OnboardedOtherFacilitiesEntries "+OnboardedOtherFacilitiesEntries);

		// JsonObject  scfObjectAfterRemovingonoboardfacilities = removeScfEntriesAfterinsertToOnboardOtherFacilities(request, resp, OnboardedOtherFacilitiesEntries,scfEntriesAfterConcatination ,debug);
		// // resp.getWriter().println("scfObjectAfterRemovingonoboardfacilities  : "+scfObjectAfterRemovingonoboardfacilities);

		// JsonObject scfResponseAfterRemovingingByCreatedOn = removeEntriesByCreateOn(resp,scfObjectAfterRemovingonoboardfacilities ,debug);
		// //  resp.getWriter().println("scfResponseAfterRemovingingByCreatedOn " +scfResponseAfterRemovingingByCreatedOn);

		// JsonArray sortedScfEntryByCreatedOn =sortByCreatedOn(scfResponseAfterRemovingingByCreatedOn);
		// //  resp.getWriter().println("sortedScfEntryByCreatedOn "+sortedScfEntryByCreatedOn);

		// JsonArray scfEntriesWithUniqueAccNo = removeDuplicateAccNo(sortedScfEntryByCreatedOn);
		// // resp.getWriter().println("scfEntriesWithUniqueAccNo " +scfEntriesWithUniqueAccNo);

		// try {
		// // Create a new workbook
		// Workbook workbook = new XSSFWorkbook();
		// // Create a blank sheet

		// createXlSheetOfScfCorpDealers(resp ,workbook, scfCorpDealerResponse);
		// createBpSheet(workbook, bpEntriesAfterConcatination);
		// createScfSheet(workbook , scfEntriesWithUniqueAccNo);
		// createOnboardotherfacilitiesSheet(workbook ,OnboardedOtherFacilitiesEntries);
		// createSCFVendorAccountsSheet(workbook, scfEntriesWithUniqueAccNo);
		// createSCFDealerAccountsSheet(workbook, scfEntriesWithUniqueAccNo);

		// resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		// resp.setHeader("Content-Disposition", "attachment;filename=sample.xlsx");
		// // Write the workbook content to the response stream
		// workbook.write(resp.getOutputStream());
		// workbook.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		//	--------------------------------  AccountMIS ---------------------------------- //
	}

	private static JsonObject parseJsonString(String jsonString) {
		JsonObject jsonObject = null;
		try (StringReader reader = new StringReader(jsonString);
				JsonReader jsonReader = new JsonReader(reader)) {
			jsonReader.setLenient(true);
			JsonParser parser = new JsonParser();
			jsonObject = parser.parse(jsonReader).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	private static void postToCDHdr(HttpServletResponse resp, String aggrID ,String cdhdrUrl , String cdHdrUserPass ,JsonObject oldPayload, JsonObject newPayload , HttpServletRequest req , boolean debug) throws IOException {
		oldPayload.remove("Items");
		newPayload.remove("Items");
		JsonObject insertPayLoad =new JsonObject();
		String exceuteURL = "";
		resp.getWriter().println("oldPayload : "+oldPayload);
		resp.getWriter().println("newPayload : "+newPayload);

		for (Map.Entry<String, JsonElement> entry : oldPayload.entrySet()) {
			String key = entry.getKey();
			JsonElement oldValue = entry.getValue();
			JsonElement newValue = newPayload.get(key);

			if (newValue != null && !oldValue.equals(newValue)) {
				resp.getWriter().println("Field: " + key);
				resp.getWriter().println("Old Value: " + oldValue);
				resp.getWriter().println("New Value: " + newValue);

			     exceuteURL = cdhdrUrl + "CDHDR"; 
				//  String createdBy = getUserPrincipal(req, "name", resp);
				 String createdAt = getCreatedAtTime();
				 long createdOnInMillis = getCreatedOnDate();
				 insertPayLoad.addProperty("AGGRID", "AGGR008");
				 insertPayLoad.addProperty("OBJECTCLAS", "CDINV");
				 insertPayLoad.addProperty("OBJECTID", "2CB3B47CE1454BAFA2239D964D4A427A");
				 insertPayLoad.addProperty("CHANGENR", "0682863");
				 insertPayLoad.addProperty("USERNAME", "P000008");
	 //			insertPayLoad.addProperty("UDATE", createdOnInMillis);
				 insertPayLoad.add("UDATE", JsonNull.INSTANCE);
	 //			insertPayLoad.addProperty("UTIME", createdAt);
				 insertPayLoad.add("UTIME", JsonNull.INSTANCE);
				 insertPayLoad.addProperty("TCODE", "203.153");
				 insertPayLoad.addProperty("PLANCHNGNR", "PLANCH");
				 insertPayLoad.addProperty("ACT_CHNGNO", "ACT_CO");
				 insertPayLoad.addProperty("WAS_PLANND", "");
				 insertPayLoad.addProperty("CHANGE_IND", "I");
				 insertPayLoad.addProperty("LANGU", "E");
				 insertPayLoad.addProperty("VERSION", "");
				 
				 insertPayLoad.addProperty("CHANGE_HISTORY.CREATEDBY", "createdBy");
				 insertPayLoad.add("CHANGE_HISTORY.CREATEDAT", JsonNull.INSTANCE);
				 insertPayLoad.add("CHANGE_HISTORY.CREATEDON", JsonNull.INSTANCE);
				 insertPayLoad.addProperty("CHANGE_HISTORY.CHANGEDBY", "");
				 insertPayLoad.add("CHANGE_HISTORY.CHANGEDAT", JsonNull.INSTANCE);
				 insertPayLoad.add("CHANGE_HISTORY.CHANGEDON", JsonNull.INSTANCE);
				 insertPayLoad.addProperty("CHANGE_HISTORY.SOURCE", "SOURCE");
				 insertPayLoad.addProperty("CHANGE_HISTORY.SOURCE_REF_ID", "SOURCE_REF_ID");
			}
		}
		executePostURL( resp  ,insertPayLoad, req , debug);
	}

	public static String getCreatedAtTime(){
		String createdAt="";
		 try {
			 SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			 sdf.setTimeZone(TimeZone.getTimeZone("IST"));
			 createdAt = sdf.format(new Date());
			 createdAt = "PT"+createdAt.substring(11, 13) +"H"+createdAt.substring(14, 16)+"M"+createdAt.substring(17, createdAt.length())+"S";
			 
		} catch (Exception e) {
			createdAt="PT00H00M00S";
		}
		 return createdAt;
	}
	
	//createOn
	public static long getCreatedOnDate(){
		long createdOn=0;
		 try {
			 SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			 SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			 sdf1.setTimeZone(TimeZone.getTimeZone("IST"));
			 Date createdAtDate = sdf2.parse(sdf1.format(new Date()));
			 createdOn = createdAtDate.getTime();
			 
		} catch (Exception e) {
			createdOn=0;
		}
		 return createdOn;
	}

	public static JsonObject executePostURL( HttpServletResponse response, JsonObject approvalObj, HttpServletRequest request, boolean debug) throws IOException{
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		String userName="", password="", authParam="";
		JsonObject jsonObj = new JsonObject();
		String data = "";
		HttpPost postRequest=null;
		try{
			
			
			String executeURL = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/PCGW/service.xsodata/CDHDR";
			
			authParam = "USR_SCHARTEC_SRVUSR" + ":"+ "7ONq2XoBvkNL7uzM" ;
		
			//String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
			
			if(debug){
				response.getWriter().println("Reading destination properties");
				response.getWriter().println("executeHttpPost.executeURL: "+ executeURL);
		      
		      //  response.getWriter().println("executeHttpPost.userName: "+ userName);
		       // response.getWriter().println("executeHttpPost.password: "+ password);
		       // response.getWriter().println("executeHttpPost.authParam: "+ authParam);
		        //response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
			}
			
		
	        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("USR_SCHARTEC_SRVUSR", "7ONq2XoBvkNL7uzM");
	        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
	        
	        if(debug){
	        	response.getWriter().println("Created Values from insertIntoApproval()--->"+approvalObj);
	        }
			
	       //response.getWriter().println("Checking of responseEntity");
			requestEntity = new StringEntity(approvalObj.toString());
			//response.getWriter().println(requestEntity);
			
			if(debug){
	        	response.getWriter().println("requestEntity"+requestEntity);
	        }
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			postRequest = new HttpPost(executeURL);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			postRequest.setEntity(requestEntity);

			if(debug){
	        	response.getWriter().println("postRequest"+postRequest);
	        }
			HttpResponse httpPostResponse = httpClient.execute(postRequest);
			responseEntity = httpPostResponse.getEntity();
			
			if(debug){
	        	response.getWriter().println("httpPostResponse"+httpPostResponse);
	        }
			//response.getWriter().println("Checking of responseentity");
			//response.getWriter().println("Response :"+responseEntity.toString());
			
			if (httpPostResponse.getEntity().getContentType() != null
					&& httpPostResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpPostResponse.getEntity().getContentType().toString()
						.replaceAll("content-type:", "").trim();
				if (contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					
					//response.getWriter().println("Response :"+data);
					if (debug)
						response.getWriter().println(EntityUtils.toString(responseEntity));
				} else {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
                      if(debug)
                      {
						response.getWriter().println(data);
                      }
				}
			} else {
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				if (debug)
					response.getWriter().println(EntityUtils.toString(responseEntity));
			}
			int statusCode = httpPostResponse.getStatusLine().getStatusCode();
				if ((statusCode/100)==2) {
					JsonParser parser = new JsonParser();
					jsonObj = (JsonObject) parser.parse(data);
					//jsonObj.addProperty("Status", "000001");
					//jsonObj.addProperty("ErrorCode", "");
					//jsonObj.addProperty("Message", "Record Inserted Successfully");
	                // response.getWriter().println("Record Inserted Successfully");				
				} else {
					jsonObj.addProperty("Status", "000002");
					jsonObj.addProperty("ErrorCode", statusCode);
					jsonObj.addProperty("Message", data);
				}
				return jsonObj;
		}catch(Exception e) {
			postRequest.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
				response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
			if(e.getLocalizedMessage()!=null){
				jsonObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
			}
			jsonObj.addProperty("ErrorCode", "J002");
			jsonObj.addProperty("Status", "000002");
			jsonObj.addProperty("Message", buffer.toString());
		}finally{
			httpClient.close();
			
		}
		return jsonObj;
	}

	public static JsonObject executePostURL1(String executeURL, String userPass, HttpServletResponse response, JsonObject insertPayLoad, 
		HttpServletRequest request, boolean debug) throws IOException{
	CloseableHttpClient httpClient = null;
	HttpEntity requestEntity = null;
	HttpEntity responseEntity = null;
	String userName="", password="", authParam="";
	JsonObject jsonObj = new JsonObject();
	String data = "";
	HttpPost postRequest=null;
	try{
		if(debug){
			response.getWriter().println("executePostURL-insertPayLoad: "+insertPayLoad);
		}
		
		if(debug)
			response.getWriter().println("executePostURL-executeURL: "+executeURL);
		
		// Context ctx = new InitialContext();
		// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
		// DestinationConfiguration destConfiguration = configuration.getConfiguration(DestinationUtils.PCGWHANA);
		
		// if (destConfiguration == null) {
        //     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        //             String.format("Destination %s is not found. Hint: Make sure to have the destination configured.", DestinationUtils.PYGWHANA));
        // }
		// String proxyType = destConfiguration.getProperty("ProxyType");
		// userName = destConfiguration.getProperty("User");
		// password = destConfiguration.getProperty("Password");
		authParam = "USR_SCHARTEC_SRVUSR" + ":"+ "7ONq2XoBvkNL7uzM" ;
	
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
		
		if(debug){
			response.getWriter().println("executeHttpPost.executeURL: "+ executeURL);
	        // response.getWriter().println("executeHttpPost.proxyType: "+ proxyType);
	        response.getWriter().println("executeHttpPost.userName: "+ userName);
	        response.getWriter().println("executeHttpPost.password: "+ password);
	        response.getWriter().println("executeHttpPost.authParam: "+ authParam);
	        response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
		}
		
	
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("USR_SCHARTEC_SRVUSR", "7ONq2XoBvkNL7uzM");
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        
        if(debug){
        	response.getWriter().println("insertPayLoad.toString(): "+insertPayLoad.toString());
        }
		
		requestEntity = new StringEntity(insertPayLoad.toString());
		
		httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
		 postRequest = new HttpPost(executeURL);
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setHeader("Accept", "application/json");
		postRequest.setHeader("Authorization",basicAuth);
		postRequest.setEntity(requestEntity);
		HttpResponse httpPostResponse = httpClient.execute(postRequest);
		responseEntity = httpPostResponse.getEntity();
		if (httpPostResponse.getEntity().getContentType() != null
				&& httpPostResponse.getEntity().getContentType().toString() != "") {
			String contentType = httpPostResponse.getEntity().getContentType().toString()
					.replaceAll("content-type:", "").trim();
			if (contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
				response.setContentType(contentType);
				data = EntityUtils.toString(responseEntity);
				if (debug)
					response.getWriter().println(EntityUtils.toString(responseEntity));
			} else {
				response.setContentType(contentType);
				data = EntityUtils.toString(responseEntity);
				if (debug)
					response.getWriter().println(data);
			}
		} else {
			response.setContentType("application/pdf");
			data = EntityUtils.toString(responseEntity);
			if (debug)
				response.getWriter().println(EntityUtils.toString(responseEntity));
		}
		int statusCode = httpPostResponse.getStatusLine().getStatusCode();
			if ((statusCode/100)==2) {
				JsonParser parser = new JsonParser();
				jsonObj = (JsonObject) parser.parse(data);
				jsonObj.addProperty("Status", "000001");
				jsonObj.addProperty("ErrorCode", "");
				jsonObj.addProperty("Message", "Record Inserted Successfully");
			} else {
				jsonObj.addProperty("Status", "000002");
				jsonObj.addProperty("ErrorCode", statusCode);
				jsonObj.addProperty("Message", data);
			}
			return jsonObj;
	}catch (Exception e) {
		postRequest.abort();
		StackTraceElement element[] = e.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<element.length;i++)
		{
			buffer.append(element[i]);
		}
		if(debug)
			response.getWriter().println("executeUpdate-Exception Stack Trace: "+buffer.toString());
		if(e.getLocalizedMessage()!=null){
			jsonObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
		}
		jsonObj.addProperty("ErrorCode", "J002");
		jsonObj.addProperty("Status", "000002");
		jsonObj.addProperty("Message", buffer.toString());
	}finally{
		httpClient.close();
		
	}
	return jsonObj;
}

	public static String getKeyProperty(HttpServletResponse resp,StringBuffer metadataXml, String entityName , boolean debug) throws Exception {
		String propertyName ="";
		try {
            // Your XML content in a StringBuffer
        
 
            // EntityType name to search for
            String targetEntityTypeName = entityName; // Replace with the actual EntityType name
 
            // Convert StringBuffer to String
            String xmlString = metadataXml.toString();
 
            // Convert the String to an InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
 
            // Parse the InputStream using DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
 
            // Normalize the XML structure
            doc.getDocumentElement().normalize();
 
            // Get all EntityType elements
            NodeList entityTypeList = doc.getElementsByTagName("EntityType");
 
            // Iterate over each EntityType
            for (int i = 0; i < entityTypeList.getLength(); i++) {
                Node entityTypeNode = entityTypeList.item(i);
                
                if (entityTypeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element entityTypeElement = (Element) entityTypeNode;
 
                    // Get the name of the EntityType
                    String entityTypeName = entityTypeElement.getAttribute("Name");
 
                    // Check if this is the target EntityType
                    if (entityTypeName.equals(targetEntityTypeName)) {
                        System.out.println("EntityType: " + entityTypeName);
 
                        // Get all Key elements within this EntityType
                        NodeList keyList = entityTypeElement.getElementsByTagName("Key");
 
                        // Iterate over each Key element
                        for (int j = 0; j < keyList.getLength(); j++) {
                            Node keyNode = keyList.item(j);
 
                            if (keyNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element keyElement = (Element) keyNode;
 
                                // Get all PropertyRef elements within this Key
                                NodeList propertyRefList = keyElement.getElementsByTagName("PropertyRef");
 
                                // Iterate over each PropertyRef and print the Name attribute
                                for (int k = 0; k < propertyRefList.getLength(); k++) {
                                    Element propertyRefElement = (Element) propertyRefList.item(k);
                                     propertyName = propertyRefElement.getAttribute("Name");
                                    System.out.println("    Key Property Name: " + propertyName);
                                }
                            }
                        }
                        // Exit the loop since we've found and processed the target EntityType
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return propertyName;
    }

	public static String getAllKeyProperty(HttpServletResponse resp,StringBuffer metadataXml, String entityName , boolean debug) throws Exception {
		String propertyName ="";
		try {
            String xmlString = metadataXml.toString();
		
 
            // Convert the String to an InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
 
            // Parse the InputStream using DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			resp.getWriter().println("factory: " + factory);
            DocumentBuilder builder = factory.newDocumentBuilder();
			resp.getWriter().println("builder: " + builder);
            Document doc = builder.parse(inputStream);
			resp.getWriter().println("doc: " + doc);
 
            // Normalize the XML structure
            doc.getDocumentElement().normalize();
			resp.getWriter().println("doc1: " + doc);

            // Get all EntityType elements
            NodeList entityTypeList = doc.getElementsByTagName("EntityType");
			resp.getWriter().println("entityTypeList: " + entityTypeList);
 
            // Iterate over each EntityType
            for (int i = 0; i < entityTypeList.getLength(); i++) {
                Node entityTypeNode = entityTypeList.item(i);
                
                if (entityTypeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element entityTypeElement = (Element) entityTypeNode;
 
                    // Get the name of the EntityType
                     String entityTypeName = entityTypeElement.getAttribute("Name");
                    resp.getWriter().println("EntityType: " + entityTypeName);
 
					if (entityTypeName.equals(entityName)) {
                    // Get all Key elements within this EntityType
                    NodeList keyList = entityTypeElement.getElementsByTagName("Key");
 
                    // Iterate over each Key element
                    for (int j = 0; j < keyList.getLength(); j++) {
                        Node keyNode = keyList.item(j);
 
                        if (keyNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element keyElement = (Element) keyNode;
 
                            // Get all PropertyRef elements within this Key
                            NodeList propertyRefList = keyElement.getElementsByTagName("PropertyRef");
 
                            // Iterate over each PropertyRef and print the Name attribute
                            for (int k = 0; k < propertyRefList.getLength(); k++) {
                                Element propertyRefElement = (Element) propertyRefList.item(k);
                                propertyName = propertyRefElement.getAttribute("Name");
                                resp.getWriter().println("    Key Property Name: " + propertyName);
                            }
                        }
                    }
				break;
				}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return propertyName;
    }

	private JsonArray callScfCorpDealer(HttpServletRequest req, HttpServletResponse resp,
            HashMap<String, String> inputForScfCorpDealerMap, boolean debug) throws IOException {
        String aggregatorName = "";
		String prntLmtid = "DLR~BLUESTAR";
        String prntLmtPrx = "DLR~BLUESTAR";
		// String prntLmtid = "";
		// String prntLmtPrx = "";
        JsonObject scfCorpDealerResponse = new JsonObject();
        JsonArray scfCorpDealerOustStandingResponse = new JsonArray();
        SCFCorpDealerOutstandingClient client = new SCFCorpDealerOutstandingClient();
        try {
            for (String key : inputForScfCorpDealerMap.keySet()) {
                String prntLimitsvalue = inputForScfCorpDealerMap.get(key);
                int countofPipes = countPipeSymbols(prntLimitsvalue);
				aggregatorName = extractPrntLimits(resp ,prntLimitsvalue, 3);
                if (countofPipes == 1) {
                    // prntLmtid = extractPrntLimits(resp ,prntLimitsvalue, countofPipes);
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key, countofPipes , aggregatorName ,prntLmtid, debug);
					scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
				} else if (countofPipes == 2) {
                    // prntLmtid = extractPrntLimits(resp ,prntLimitsvalue, 1 );
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key , 1 , aggregatorName ,prntLmtid, debug);
                    scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
					// prntLmtPrx = extractPrntLimits(resp ,prntLimitsvalue, countofPipes);
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key ,countofPipes,aggregatorName, prntLmtPrx, debug);
                    scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
				}
            }
			// resp.getWriter().println("scfCorpDealerOustStandingResponse "+scfCorpDealerOustStandingResponse);
            return scfCorpDealerOustStandingResponse;
        } catch (Exception exception) {
            JsonObject result = new JsonObject();
            result.addProperty("Exception", exception.getClass().getCanonicalName());
            result.addProperty("Message", exception.getClass().getCanonicalName() + "--->" + exception.getMessage());

            if (debug) {
                StackTraceElement element[] = exception.getStackTrace();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < element.length; i++) {
                    buffer.append(element[i]);
                }
                resp.getWriter().println(new Gson().toJson(result));
                resp.getWriter().println("getAggregatorsEntries-Exception Stack Trace: " + buffer.toString());
            }
            scfCorpDealerOustStandingResponse.add(result);
            return scfCorpDealerOustStandingResponse;
        }
    }

	public static String extractPrntLimits(HttpServletResponse resp , String input, int pipeCount) {
		try {
			int firstPipeIndex = input.indexOf('|');
			if (pipeCount == 1) {
				if (firstPipeIndex != -1) {
					return input.substring(0, firstPipeIndex);
				} else {
					return input;
				}
			} else if (pipeCount == 2) {
				int secondPipeIndex = input.indexOf('|', firstPipeIndex + 1);

				if (firstPipeIndex != -1 && secondPipeIndex != -1) {
					return input.substring(firstPipeIndex + 1, secondPipeIndex);
				} else {
					return "";
				}
			}else if(pipeCount == 3){
					String[] elements = input.split("\\|");
					String lastElement = elements[elements.length - 1];
					return lastElement;
			}else{
				return "";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

    private static int countPipeSymbols(String value) {
		int count = 0;
		for (char c : value.toCharArray()) {
			if (c == '|') {
				count++;
			}
		}
		return count;
	}
	public JsonObject getSccNfgEntries(HttpServletResponse resp, String pygwUrl, String userPass ,boolean debug) {
		String url = pygwUrl+"SCCNFG?$filter=CP_TYPE%20eq%20%2701%27";
		JsonObject sccnfgResponse = executeURL(url, userPass, resp, debug);
		return sccnfgResponse;
	}

	public JsonObject getAggregatorsEntries(HttpServletRequest req, HttpServletResponse resp, boolean debug) {
		String url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/Aggregators";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		JsonObject aggrResponse = executeURL(url, userpass, resp, debug);
		return aggrResponse;
	}

	public HashMap getMapForInputToScfCorpDealer(HttpServletRequest req, HttpServletResponse resp, JsonObject sccnfgObj,
			JsonObject aggregatorsobj, boolean debug) throws IOException {

		HashMap<String, String> inputForScfCorpDealerMap = new HashMap<>();
		boolean isAggregatorIDmatched = false;

		String AggregatorName = "";
		if (sccnfgObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			if (aggregatorsobj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

				JsonArray sccnfgResponseArray = sccnfgObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				JsonArray aggregatorsResponseArray = aggregatorsobj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();

				for (int j = 0; j < sccnfgResponseArray.size(); j++) {

					JsonObject finObj = sccnfgResponseArray.get(j).getAsJsonObject();
					if (!finObj.get("AGGRID").isJsonNull()
							&& !finObj.get("AGGRID").getAsString().equalsIgnoreCase("")) {

						for (int i = 0; i < aggregatorsResponseArray.size(); i++) {
							JsonObject aggregatorObj = aggregatorsResponseArray.get(i).getAsJsonObject();

							if (!aggregatorObj.get("AggregatorID").isJsonNull()
									&& !aggregatorObj.get("AggregatorID").getAsString().equalsIgnoreCase("")) {

								if (finObj.get("AGGRID").getAsString()
										.equalsIgnoreCase(aggregatorObj.get("AggregatorID").getAsString())) {
									AggregatorName = aggregatorObj.get("AggregatorName").getAsString();
									isAggregatorIDmatched = true;
								} else {
									isAggregatorIDmatched = false;
								}

								if (isAggregatorIDmatched) {
									if (!finObj.get("PRNTLIMTIDPREFIX").isJsonNull()
											&& !finObj.get("PRNTLIMTIDPREFIX").getAsString().equalsIgnoreCase("")) {

										if (!finObj.get("ParentLimitPrefixHistory").isJsonNull()
												&& !finObj.get("ParentLimitPrefixHistory").getAsString()
														.equalsIgnoreCase("")) {

											inputForScfCorpDealerMap.put(finObj.get("AGGRID").getAsString(),
													finObj.get("PRNTLIMTIDPREFIX").getAsString() + "|"
															+ finObj.get("ParentLimitPrefixHistory").getAsString() + "|"
															+ AggregatorName);
										} else {
											inputForScfCorpDealerMap.put(finObj.get("AGGRID").getAsString(),
													finObj.get("PRNTLIMTIDPREFIX").getAsString() + "|"
															+ AggregatorName);
										}
									}
								}
							}
						}
					}else{
						if(debug)
						resp.getWriter().println("Empty");
					}
				}
			}
		}
		return inputForScfCorpDealerMap;
	}

	public JsonObject getScfEntries(HttpServletRequest req, HttpServletResponse response, boolean debug)
			throws IOException {
		String url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		JsonObject scfResponse = executeURL(url, userpass, response, debug);

		if (scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray scfResponseArray = scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
				Iterator<JsonElement> iterator = scfResponseArray.iterator();
           while (iterator.hasNext()) {
            JsonObject scfEntry = iterator.next().getAsJsonObject();
        
				if ((scfEntry.has("AccountNo") && scfEntry.get("AccountNo").isJsonNull())
						|| scfEntry.get("AccountNo").getAsString().equalsIgnoreCase("") 
						|| scfEntry.get("AccountNo").getAsString().contains("-")||
						(scfEntry.has("CPGUID") && !scfEntry.get("CPGUID").isJsonNull()
								&& scfEntry.get("CPGUID").getAsString().contains("-"))
						||
						(scfEntry.has("CPTypeID") && !scfEntry.get("CPTypeID").isJsonNull()
								&& scfEntry.get("CPTypeID").getAsString().contains("-"))
						||
						(scfEntry.has("AggregatorID") && !scfEntry.get("AggregatorID").isJsonNull()
								&& scfEntry.get("AggregatorID").getAsString().contains("-"))) {
									iterator.remove();
				}
			}
		}
		return scfResponse;
	}

	public JsonObject getBpEntries(HttpServletRequest req, HttpServletResponse response, boolean debug)
			throws ClientProtocolException, IOException {
		JsonArray finalBp = new JsonArray();

		String executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/BPHeaders?$filter=CPType%20eq%20%2760%27";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";

		JsonObject bpResponse = executeURL(executeURL, userpass, response, debug);

		if (bpResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
			JsonArray bpResponseArray = bpResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();

			Iterator<JsonElement> iterator = bpResponseArray.iterator();
			while (iterator.hasNext()) {
				JsonObject bpObj = iterator.next().getAsJsonObject();

				if ((bpObj.has("FacilityType") && bpObj.get("FacilityType").isJsonNull())
						|| bpObj.get("FacilityType").getAsString().equalsIgnoreCase("") ||
						(bpObj.has("CPGuid")
								&& !bpObj.get("CPGuid").isJsonNull() && bpObj.get("CPGuid").getAsString().contains("-"))
						||
						(bpObj.has("CPType") && !bpObj.get("CPType").isJsonNull()
								&& bpObj.get("CPType").getAsString().contains("-"))
						||
						(bpObj.has("AggregatorID") && !bpObj.get("AggregatorID").isJsonNull()
								&& bpObj.get("AggregatorID").getAsString().contains("-"))) {
							// response.getWriter().println("removed");
					iterator.remove();
				} else {
					finalBp.add(bpObj);
				}
			}
		}
		return bpResponse;
	}

	public JsonObject concatinateFieldsinScf(HttpServletRequest httpServletRequest, HttpServletResponse resp,
			JsonObject scfEntries, boolean debug) {

		JsonObject scfEntry = new JsonObject();
		if (scfEntries.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray scfResponseArray = scfEntries.get("d").getAsJsonObject().get("results").getAsJsonArray();
			for (int j = 0; j < scfResponseArray.size(); j++) {
				scfEntry = scfResponseArray.get(j).getAsJsonObject();

				String aggregatorID = scfEntry.has("AggregatorID") && !scfEntry.get("AggregatorID").isJsonNull()
						? scfEntry.get("AggregatorID").getAsString()
						: "";
				String cpGUID = scfEntry.has("CPGUID") && !scfEntry.get("CPGUID").isJsonNull()
						? scfEntry.get("CPGUID").getAsString()
						: "";
				String cpTypeID = scfEntry.has("CPTypeID") && !scfEntry.get("CPTypeID").isJsonNull()
						? scfEntry.get("CPTypeID").getAsString()
						: "";
				String ConcatinatedId = aggregatorID + cpGUID + cpTypeID;

				scfEntry.addProperty("ConcatinatedValues", ConcatinatedId);
			}
		}
		return scfEntries;
	}

	public JsonObject concatinateFieldsinBP(HttpServletRequest httpServletRequest, HttpServletResponse resp,JsonObject bpEntries, boolean debug) {

		JsonObject bpEntry = new JsonObject();
		if (bpEntries.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray bpResponseArray = bpEntries.get("d").getAsJsonObject().get("results").getAsJsonArray();
			for (int j = 0; j < bpResponseArray.size(); j++) {
				bpEntry = bpResponseArray.get(j).getAsJsonObject();

				String aggregatorID = bpEntry.has("AggregatorID") && !bpEntry.get("AggregatorID").isJsonNull()
						? bpEntry.get("AggregatorID").getAsString()
						: "";
				String cpGUID = bpEntry.has("CPGuid") && !bpEntry.get("CPGuid").isJsonNull()
						? bpEntry.get("CPGuid").getAsString()
						: "";
				String cpTypeID = bpEntry.has("CPType") && !bpEntry.get("CPType").isJsonNull()
						? bpEntry.get("CPType").getAsString()
						: "";
				String ConcatinatedId = aggregatorID + cpGUID + cpTypeID;

				bpEntry.addProperty("ConcatinatedValues", ConcatinatedId);
			}
		}
		return bpEntries;
	}

	public JsonArray pushMatchedRecordsToOnoboardOtherfacility(HttpServletRequest req, HttpServletResponse resp , JsonObject scfRecords, JsonObject bpRecords) {
		try{
		JsonArray OnboardedOtherFacilitiesEntries = new JsonArray();
		JsonObject scfEntry = new JsonObject();
		JsonObject bpEntry = new JsonObject();
		if (scfRecords.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray scfResponseArray = scfRecords.get("d").getAsJsonObject().get("results").getAsJsonArray();

			if (bpRecords.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

				JsonArray bpResponseArray = bpRecords.get("d").getAsJsonObject().get("results").getAsJsonArray();
				
				for (int i = 0; i < bpResponseArray.size(); i++) {
					bpEntry = bpResponseArray.get(i).getAsJsonObject();

					for (int j = 0; j < scfResponseArray.size(); j++) {
						scfEntry = scfResponseArray.get(j).getAsJsonObject();
						if (bpEntry.get("ConcatinatedValues").getAsString()
								.equalsIgnoreCase(scfEntry.get("ConcatinatedValues").getAsString())) {
							OnboardedOtherFacilitiesEntries.add(scfEntry);
						}
					}
				}
			}
		}
		return OnboardedOtherFacilitiesEntries;
	    }catch(Exception exception){
			return null;
		}
	}

	public JsonObject removeScfEntriesAfterinsertToOnboardOtherFacilities(HttpServletRequest req, HttpServletResponse resp,
			JsonArray OnboardedOtherFacilitiesEntries, JsonObject scfObject, boolean debug)
			throws ClientProtocolException, IOException {
				try{
					JsonArray scfArray = scfObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
					if(OnboardedOtherFacilitiesEntries.size()>0  && scfArray.size()>0 ){
					Set<String> idsToRemove = new HashSet<>();
					for (JsonElement element : OnboardedOtherFacilitiesEntries) {
						JsonObject obj = element.getAsJsonObject();
						idsToRemove.add(obj.get("ID").getAsString());
					}

					Iterator<JsonElement> iterator = scfArray.iterator();
					while (iterator.hasNext()) {
						JsonObject arrayObject = iterator.next().getAsJsonObject();
						String id = arrayObject.get("ID").getAsString();
						if (idsToRemove.contains(id)) {
							iterator.remove();
						}
					}
				}
		// if (OnboardedOtherFacilitiesEntries.size() > 0 && scfObject.size() > 0) {

		// 	JsonArray scfResponseArray = scfObject.get("d").getAsJsonObject().get("results").getAsJsonArray();

		// 	for (int i = 0; i <= OnboardedOtherFacilitiesEntries.size() - 1; i++) {
		// 		JsonObject OnboardedOtherFacilitiesEntriesObj = new JsonObject();
		// 		Iterator<JsonElement> iterator = scfResponseArray.iterator();
		// 		while (iterator.hasNext()) {
		// 			JsonObject scfObj = iterator.next().getAsJsonObject();
		// 			if (OnboardedOtherFacilitiesEntriesObj.get("ID").getAsString()
		// 					.equalsIgnoreCase(scfObj.get("ID").getAsString())) {
		// 				iterator.remove();

		// 			}
		// 		}
		// 	}
		// }

		return scfObject;
	}catch(Exception exception){
		scfObject.addProperty("Exception", exception.getLocalizedMessage());
		return scfObject;
	}
	}

	public JsonObject removeEntriesByCreateOn(HttpServletResponse resp, JsonObject scfEntriesAfterConcatination,
			boolean debug) throws ClientProtocolException, IOException {

				try{
					if(scfEntriesAfterConcatination.size()>0){
					JsonArray scfArray = scfEntriesAfterConcatination.get("d").getAsJsonObject().get("results").getAsJsonArray();;

					long cutoffDate = 1712294400000L;
					Iterator<JsonElement> iterator = scfArray.iterator();
					while (iterator.hasNext()) {
						JsonObject scfObj = iterator.next().getAsJsonObject();

						if (scfObj.has("CreatedOn") && !scfObj.get("CreatedOn").isJsonNull() && scfObj.has("CreatedBy") && !scfObj.get("CreatedBy").isJsonNull()) {
							String createdOn = scfObj.get("CreatedOn").getAsString();
							String createdBy = scfObj.get("CreatedBy").getAsString();
		
							long createdOnAfterRemovingTimeStamp = Long.parseLong(createdOn.replaceAll("[^\\d]", ""));
							
							if (createdOnAfterRemovingTimeStamp < cutoffDate|| (createdOnAfterRemovingTimeStamp > cutoffDate  && ( createdBy.equalsIgnoreCase("P000024")  || createdBy.equalsIgnoreCase("P000159")))){
						
							}else{
								iterator.remove();
							}

							// if (!(createdOnAfterRemovingTimeStamp < cutoffDate) && !(createdBy.equalsIgnoreCase("P000024") || createdBy.equalsIgnoreCase("P000159"))) {
							// 	iterator.remove();
							// }
						}
					}
				}
				return scfEntriesAfterConcatination;
				}catch(Exception exception){
					JsonObject result =new JsonObject();
					result.addProperty("Exception", exception.getLocalizedMessage());
					return result;
				}
	}

	public JsonArray sortByCreatedOn(JsonObject scfEntry) {

		JsonArray jsonArray = new JsonArray();
		if (scfEntry.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
			JsonArray bpResponseArray = scfEntry.get("d").getAsJsonObject().get("results")
					.getAsJsonArray();
			List<JsonObject> jsonObjectList = new ArrayList<>();
			for (JsonElement element : bpResponseArray) {
				jsonObjectList.add(element.getAsJsonObject());
			}

			// Sort the list based on CreatedOn field (from newest to oldest)
			jsonObjectList.sort((o1, o2) -> {

				JsonElement createdOnElement1 = o1.get("CreatedOn");
                JsonElement createdOnElement2 = o2.get("CreatedOn");
 
                // Handle null cases
                if (createdOnElement1 == null || createdOnElement1.isJsonNull()) {
                    return 1; // Place o1 after o2
                }
                if (createdOnElement2 == null || createdOnElement2.isJsonNull()) {
                    return -1; // Place o2 after o1
                }
 
                long createdOn1 = Long.parseLong(createdOnElement1.getAsString().replaceAll("[^\\d]", ""));
                long createdOn2 = Long.parseLong(createdOnElement2.getAsString().replaceAll("[^\\d]", ""));

				// long createdOn1 = Long.parseLong(o1.get("CreatedOn").getAsString().replaceAll("[^\\d]", ""));
				// long createdOn2 = Long.parseLong(o2.get("CreatedOn").getAsString().replaceAll("[^\\d]", ""));
				return Long.compare(createdOn2, createdOn1);
			});

			for (JsonObject jsonObject : jsonObjectList) {
				jsonArray.add(jsonObject);
			}
		}
		return jsonArray;
	}

	public JsonArray removeDuplicateAccNo(JsonArray sortedScfEntryByCreatedOn)
			throws ClientProtocolException, IOException {
		
		Set<String> seenAccountNos = new HashSet<>();

		Iterator<JsonElement> iterator = sortedScfEntryByCreatedOn.iterator();
					while (iterator.hasNext()) {
						JsonObject scfObj = iterator.next().getAsJsonObject();
			
			String accountNo = scfObj.get("AccountNo").getAsString();

			if (!seenAccountNos.contains(accountNo)) {
				seenAccountNos.add(accountNo);
			}else{
				iterator.remove();
			}
		}
		return sortedScfEntryByCreatedOn;
	}

	private static Date parseDate(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date format", e);
		}
	}

	private static Date parseDateFromJson(String jsonDateStr) {
		// Extract timestamp from the /Date(...) format
		long timestamp = Long.parseLong(jsonDateStr.replace("/Date(", "").replace(")/", ""));
		return new Date(timestamp);
	}

	private void createOnboardotherfacilitiesSheet(Workbook workbook, JsonArray onboardedOtherFacilitiesEntries) {

		Sheet sheet = workbook.createSheet("OnboardedOtherFacilities Sheet");

		Row headerRow = sheet.createRow(0);

		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		int i=0;
		for (String field : fields) {
		Cell headerCells = headerRow.createCell(i);
				headerCells.setCellValue(field);
				i++;
		}
		if (onboardedOtherFacilitiesEntries.size() > 0) {

			JsonArray onboardArray = onboardedOtherFacilitiesEntries;

			JsonObject firstElement = onboardArray.get(0).getAsJsonObject();
			firstElement.remove("__metadata");
			String arr[] = getHeaders(firstElement);

			//  for (int i = 0; i <= arr.length - 1; i++) {
			//  	Cell headerCells = headerRow.createCell(i);
			//  	headerCells.setCellValue(arr[i]);
			//  }

			int rowNum = 1;

			for (int j = 0; j <= onboardArray.size() - 1; j++) {
				Row valuesOfBp = sheet.createRow(rowNum);
				JsonObject onboardObject = onboardArray.get(j).getAsJsonObject();
				Cell rowCells = null;
				int cellNumber = 0;

				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			}
		}
	}

	public void createBpSheet(Workbook workbook, JsonObject jsonObject) {

		Sheet sheet = workbook.createSheet("BPSheet");

		String[] keys = {
		"Address1", "Address2", "Address3", "Address4", "AggregatorID", "ChangedAt", "ChangedBy", "ChangedOn",
		"CreatedAt", "CreatedBy", "CreatedOn", "Source", "City", "Country", "CountryDesc", "CPGuid", "CPName",
		"CPType", "District", "EmailID", "GSTIN", "BPGuid", "IncorporationDate", "LandLine1", "LegalStatus",
		"Mobile1", "Mobile2", "PAN", "Pincode", "State", "StateDesc", "StatusID", "UtilDistrict", "ApproverRemarks",
		"SourceReferenceID", "FundBasedExposure", "HgdFrgnCurrExposure", "MSME", "NonFundBasedExposure",
		"TotalBankingExposure", "TotalFrgnCurrExposure", "UnHgdFrgnCurrExposure", "UdyamRegNo",
		"CorporateIdentificationNo", "BPRejectionRemarks", "FacilityType", "EntityID", "EntityType", "LEINumber",
		"ParentName", "ParentNo", "ParentType", "URCActivityType", "URCDocURL", "URCEntityType", "URCSectorCode",
		"URCSubSectorCode", "ODAccountNo", "URCRegistrationDate", "ERPCPName", "Testrun", "UtilDistrictDs",
		"LegalStatusDs", "LoginID"
         };
		Row headerRow = sheet.createRow(0);

		if (jsonObject.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray bpArray = jsonObject.get("d").getAsJsonObject().get("results").getAsJsonArray();

			JsonObject firstElement = bpArray.get(0).getAsJsonObject();
			firstElement.remove("__metadata");
			String arr[] = getHeaders(firstElement);

			for (int i = 0; i <= arr.length - 1; i++) {
				Cell headerCells = headerRow.createCell(i);
				if(!arr[i].equalsIgnoreCase("BPContactPersons")){
				headerCells.setCellValue(arr[i]);
				}
			}

			int rowNum = 1;

			for (int j = 0; j <= bpArray.size() - 1; j++) {
				Row valuesOfBp = sheet.createRow(rowNum);
				JsonObject bpObject = bpArray.get(j).getAsJsonObject();
				Cell rowCells = null;
				int cellNumber = 0;

				for (int i = 0; i <= keys.length - 1; i++) {
				if (bpObject.has(keys[i]) && !bpObject.get(keys[i]).isJsonNull()) {
					rowCells = valuesOfBp.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(bpObject.get(keys[i]).getAsString());
				} else {
					rowCells = valuesOfBp.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}

			rowNum++;

				// if (bpObject.has("Address2") && !bpObject.get("Address2").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Address2").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Address3") && !bpObject.get("Address3").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Address3").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Address4") && !bpObject.get("Address4").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Address4").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("AggregatorID") && !bpObject.get("AggregatorID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("AggregatorID").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ChangedAt") && !bpObject.get("ChangedAt").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ChangedAt").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ChangedBy") && !bpObject.get("ChangedBy").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ChangedBy").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ChangedOn") && !bpObject.get("ChangedOn").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ChangedOn").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CreatedAt") && !bpObject.get("CreatedAt").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CreatedAt").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CreatedBy") && !bpObject.get("CreatedBy").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CreatedBy").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CreatedOn") && !bpObject.get("CreatedOn").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CreatedOn").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Source") && !bpObject.get("Source").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Source").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("City") && !bpObject.get("City").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("City").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Country") && !bpObject.get("Country").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Country").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CountryDesc") && !bpObject.get("CountryDesc").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CountryDesc").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CPGuid") && !bpObject.get("CPGuid").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CPGuid").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CPName") && !bpObject.get("CPName").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CPName").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CPType") && !bpObject.get("CPType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CPType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("District") && !bpObject.get("District").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("District").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("EmailID") && !bpObject.get("EmailID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("EmailID").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("GSTIN") && !bpObject.get("GSTIN").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("GSTIN").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("BPGuid") && !bpObject.get("BPGuid").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("BPGuid").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("IncorporationDate") && !bpObject.get("IncorporationDate").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("IncorporationDate").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("LandLine1") && !bpObject.get("LandLine1").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("LandLine1").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("LegalStatus") && !bpObject.get("LegalStatus").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("LegalStatus").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Mobile1") && !bpObject.get("Mobile1").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Mobile1").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Mobile2") && !bpObject.get("Mobile2").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Mobile2").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("PAN") && !bpObject.get("PAN").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("PAN").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Pincode") && !bpObject.get("Pincode").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Pincode").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("State") && !bpObject.get("State").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("State").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("StateDesc") && !bpObject.get("StateDesc").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("StateDesc").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("StatusID") && !bpObject.get("StatusID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("StatusID").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("UtilDistrict") && !bpObject.get("UtilDistrict").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("UtilDistrict").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ApproverRemarks") && !bpObject.get("ApproverRemarks").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ApproverRemarks").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("SourceReferenceID") && !bpObject.get("SourceReferenceID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("SourceReferenceID").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("FundBasedExposure") && !bpObject.get("FundBasedExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("FundBasedExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("HgdFrgnCurrExposure") && !bpObject.get("HgdFrgnCurrExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("HgdFrgnCurrExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("MSME") && !bpObject.get("MSME").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("MSME").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("NonFundBasedExposure") && !bpObject.get("NonFundBasedExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("NonFundBasedExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("TotalBankingExposure") && !bpObject.get("TotalBankingExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("TotalBankingExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("TotalFrgnCurrExposure") && !bpObject.get("TotalFrgnCurrExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("TotalFrgnCurrExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("UnHgdFrgnCurrExposure") && !bpObject.get("UnHgdFrgnCurrExposure").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("UnHgdFrgnCurrExposure").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("UdyamRegNo") && !bpObject.get("UdyamRegNo").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("UdyamRegNo").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("CorporateIdentificationNo")
				// 		&& !bpObject.get("CorporateIdentificationNo").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("CorporateIdentificationNo").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("BPRejectionRemarks") && !bpObject.get("BPRejectionRemarks").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("BPRejectionRemarks").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("FacilityType") && !bpObject.get("FacilityType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("FacilityType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("EntityID") && !bpObject.get("EntityID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("EntityID").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("EntityType") && !bpObject.get("EntityType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("EntityType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("LEINumber") && !bpObject.get("LEINumber").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("LEINumber").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ParentName") && !bpObject.get("ParentName").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ParentName").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ParentNo") && !bpObject.get("ParentNo").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ParentNo").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ParentType") && !bpObject.get("ParentType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ParentType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCActivityType") && !bpObject.get("URCActivityType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCActivityType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCDocURL") && !bpObject.get("URCDocURL").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCDocURL").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCEntityType") && !bpObject.get("URCEntityType").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCEntityType").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCSectorCode") && !bpObject.get("URCSectorCode").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCSectorCode").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCSubSectorCode") && !bpObject.get("URCSubSectorCode").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCSubSectorCode").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ODAccountNo") && !bpObject.get("ODAccountNo").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ODAccountNo").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("URCRegistrationDate") && !bpObject.get("URCRegistrationDate").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("URCRegistrationDate").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("ERPCPName") && !bpObject.get("ERPCPName").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("ERPCPName").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("Testrun") && !bpObject.get("Testrun").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("Testrun").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("UtilDistrictDs") && !bpObject.get("UtilDistrictDs").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("UtilDistrictDs").getAsString());
				// } else {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("LegalStatusDs") && !bpObject.get("LegalStatusDs").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("LegalStatusDs").getAsString());
				// } else {
				// 	rowCells.setCellValue("");
				// }

				// if (bpObject.has("LoginID") && !bpObject.get("LoginID").isJsonNull()) {
				// 	rowCells = valuesOfBp.createCell(cellNumber);
				// 	cellNumber++;
				// 	rowCells.setCellValue(bpObject.get("LoginID").getAsString());
				// } else {
				// 	rowCells.setCellValue("");
				// }
				
			}

		}

	}

	private String[] getHeaders(JsonObject jsonObject) {
		return jsonObject.keySet().toArray(new String[0]);
	}

	public void createXlSheetOfScfCorpDealers(HttpServletResponse resp,Workbook workbook, JsonArray scfCorpDealerResponse) throws IOException{
		
		Sheet sheet = workbook.createSheet("SCFCorpDealerOutstandingSheet");
		JsonArray onBoardOtherFacilitiesArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {"ODAccountNumber","CustomerID","DealerName","DealerLimit","DealerExpiryDate",
							"PeakLimit", "TODLimit", "NoOfDisbursement", "TotalDisbursementExcludingInterest",
							"RepaymentAmount", "BalanceOutstanding", "UnutilizedLimit", "NormalDues",
							"OverdueWithinCure", "OverdueBeyondCure", "OverdueBetween0To7Days",
							"OverdueBetween7To14Days", "OverdueBetween14To21Days", "OverdueBetween21To28Days",
							"OverdueBetween28To60Days","OverdueBeyond60Days","City","IsAccountFrozen",
							"DealerCode", "Currency"};
             
			onBoardOtherFacilitiesArray = scfCorpDealerResponse;

			JsonObject firstElement = onBoardOtherFacilitiesArray.get(0).getAsJsonObject();
			JsonArray firstArray = firstElement.getAsJsonArray("results");
		
			firstElement = firstArray.get(0).getAsJsonObject();
			String arr[] = getHeaders(firstElement);

			for(int i=0;i<=3;i++){
				Cell headerCells = headerRow.createCell(i);
				if(i==0){
				headerCells.setCellValue("AggregatorID");
				}else if(i==1){
					headerCells.setCellValue("AggregatorName");
					}else if(i==2){
						headerCells.setCellValue("ParentLimitID");
						}else if(i==3){
							headerCells.setCellValue("ParentLimitPrefixHistory");
							}
			}

			//with status , msg , respons i =0 
			// without i = 3
			int q = 4;
			for (int i = 3; i <= arr.length - 5; i++) {
				Cell headerCells = headerRow.createCell(q);
				headerCells.setCellValue(arr[i]);
				q++;
			}	

		int rowNum = 1;
		
		for (int z = 0; z <= onBoardOtherFacilitiesArray.size() - 1; z++) {
			firstElement = onBoardOtherFacilitiesArray.get(z).getAsJsonObject();
			 firstArray = firstElement.getAsJsonArray("results");
		for (int j = 0; j <= firstArray.size() - 1; j++) {
			Row valuesOfScf = sheet.createRow(rowNum);
			JsonObject scfObject = firstArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			// with status , msg , respons cellNumber =7 
			// without cellNumber = 4
			int cellNumber = 4;

			if (scfObject.has("AggregatorID") && !scfObject.get("AggregatorID").isJsonNull()) {
				rowCells = valuesOfScf.createCell(0);
				rowCells.setCellValue(scfObject.get("AggregatorID").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(0);
				rowCells.setCellValue("");
			}

			if (scfObject.has("AggregatorName") && !scfObject.get("AggregatorName").isJsonNull()) {
				rowCells = valuesOfScf.createCell(1);
				rowCells.setCellValue(scfObject.get("AggregatorName").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(1);
				rowCells.setCellValue("");
			}

			if (scfObject.has("ParentLimitID") && !scfObject.get("ParentLimitID").isJsonNull()) {
				rowCells = valuesOfScf.createCell(2);
				rowCells.setCellValue(scfObject.get("ParentLimitID").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(2);
				rowCells.setCellValue("");
			}

			if (scfObject.has("ParentLimitPrefixHistory") && !scfObject.get("ParentLimitPrefixHistory").isJsonNull()) {
				rowCells = valuesOfScf.createCell(3);
				rowCells.setCellValue(scfObject.get("ParentLimitPrefixHistory").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(3);
				rowCells.setCellValue("");
			}

			// if (scfObject.has("Status") && !scfObject.get("Status").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(4);
			// 	rowCells.setCellValue(scfObject.get("Status").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(4);
			// 	rowCells.setCellValue("");
			// }

			// if (scfObject.has("ResponseCode") && !scfObject.get("ResponseCode").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(5);
			// 	rowCells.setCellValue(scfObject.get("ResponseCode").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(5);
			// 	rowCells.setCellValue("");
			// }

			// if (scfObject.has("Message") && !scfObject.get("Message").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(6);
			// 	rowCells.setCellValue(scfObject.get("Message").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(6);
			// 	rowCells.setCellValue("");
			// }

			for (String field : fields) {
				if (scfObject.has(field) && !scfObject.get(field).isJsonNull()) {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(scfObject.get(field).getAsString());
				} else {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}
			rowNum++;
		}
	}
	}

	public void createScfSheet(Workbook workbook, JsonArray scfObjectWithUniqueAccNo) {

		Sheet sheet = workbook.createSheet("ScfSheet");
		JsonArray scfArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		scfArray = scfObjectWithUniqueAccNo;

		JsonObject firstElement = scfArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 1; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= scfArray.size() - 1; j++) {
			Row valuesOfScf = sheet.createRow(rowNum);
			JsonObject scfObject = scfArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			for (String field : fields) {
				if (scfObject.has(field) && !scfObject.get(field).isJsonNull()) {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(scfObject.get(field).getAsString());
				} else {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}
			rowNum++;
		}
	}

	public void createSCFVendorAccountsSheet(Workbook workbook, JsonArray jsonObject) {

		Sheet sheet = workbook.createSheet("VBDSheet");
		JsonArray bpArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		bpArray = jsonObject;

		JsonObject firstElement = bpArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 1; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= bpArray.size() - 1; j++) {
			Row valuesOfBp = sheet.createRow(rowNum);
			JsonObject onboardObject = bpArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			if (onboardObject.has("CPTypeID") && !onboardObject.get("CPTypeID").isJsonNull()
					&& onboardObject.get("CPTypeID").getAsString().equalsIgnoreCase("60")) {
				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			}
		}
	}

	public void createSCFDealerAccountsSheet(Workbook workbook, JsonArray jsonObject)
			throws ClientProtocolException, IOException {

		CloseableHttpClient httpClient = null;
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		Sheet sheet = workbook.createSheet("SCFDealerAccounts");
		JsonArray bpArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		bpArray = jsonObject;

		JsonObject firstElement = bpArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 1; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= bpArray.size() - 1; j++) {
			Row valuesOfBp = sheet.createRow(rowNum);
			JsonObject onboardObject = bpArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			if ((onboardObject.has("CPTypeID") && !onboardObject.get("CPTypeID").isJsonNull()
					&& onboardObject.get("CPTypeID").getAsString().equalsIgnoreCase("01"))
					&& (onboardObject.get("AccountNo").getAsString().startsWith("010205") ||
							onboardObject.get("AccountNo").getAsString().startsWith("10205"))) {

				if (onboardObject.get("AccountNo").getAsString().startsWith("10205")) {
					String accountNo = onboardObject.get("AccountNo").getAsString();
					accountNo = "0" + accountNo;
					onboardObject.addProperty("AccountNo", accountNo);
				}

				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			} 
			// else if (!onboardObject.get("AccountNo").getAsString().startsWith("010205")) {
			// 	String executeUrl = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances('"
			// 			+ onboardObject.get("ID").getAsString() + "')";
			// 	// commonUtils.executeDelete(executeURL, userpass, response, req,
			// 	// debug,executeUrl);

			// 	// executeURL = oDataURL+"Approval('"+apGuid+"')";

			// 	// requestEntity = new StringEntity(appUpdatedObj.toString());
			// 	String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());

			// 	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			// 	UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("P000000",
			// 			"DevCFHNDBP@$$wdFeb2024");
			// 	credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			// 	httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

			// 	HttpDelete deleteRequest = new HttpDelete(executeUrl);
			// 	deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			// 	deleteRequest.setHeader("Content-Type", "application/json");
			// 	deleteRequest.setHeader("Accept", "application/json");
			// 	deleteRequest.setHeader("X-HTTP-Method", "DELETE");

			// 	HttpResponse httpResponse = httpClient.execute(deleteRequest);

			// 	if (httpResponse.getStatusLine().getStatusCode() == 204) {
			// 		// response.getWriter().println("deleted");

			// 	} else if (httpResponse.getStatusLine().getStatusCode() == 404) {
			// 		// response.getWriter().println("Resources not found for this:");
			// 	} else {
			// 		// responseEntity = httpResponse.getEntity();

			// 	}
			// }
		}
	}

	@SuppressWarnings("finally")
	public JsonObject executeURL(String executeURL, String userPass, HttpServletResponse response, boolean debug) {
		DataOutputStream dataOut = null;
		BufferedReader in = null;
		JsonObject jsonObj = null;
		try {

			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			connection.setDoInput(true);

			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String inputLine;
			StringBuffer responseStrBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseStrBuffer.append(inputLine);
			}

			if (debug) {
				response.getWriter().println("Direct Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
			JsonParser parser = new JsonParser();
			jsonObj = (JsonObject) parser.parse(responseStrBuffer.toString());

			if (debug) {
				response.getWriter().println("Json Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
		} catch (Exception e) {
			response.getWriter().println("executeURL.Exception: " + e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println("executeURL.Full Stack Trace: " + buffer.toString());
		} finally {
			return jsonObj;
		}
	}

	@SuppressWarnings("finally")
	public StringBuffer  executeMetadata(boolean debug , String executeURL, String userPass, HttpServletResponse response) {
		DataOutputStream dataOut = null;
		BufferedReader in = null;
		JsonObject jsonObj = null;
		StringBuffer responseStrBuffer = new StringBuffer();
		try {
			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type","application/xml");
			connection.setRequestProperty("Accept","application/xml");
			connection.setRequestProperty("Authorization","Basic "+Base64.getEncoder().encodeToString(userPass.getBytes()));
			connection.setDoInput(true);
			int responseCode = connection.getResponseCode();
			
			if ((responseCode / 100) == 2) {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					responseStrBuffer.append(inputLine);
				}
			}
			return	responseStrBuffer;
		} catch (Exception e) {
			// response.getWriter().println("executeURL.Exception: " + e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			// response.getWriter().println("executeURL.Full Stack Trace: " + buffer.toString());
		} finally {
			return responseStrBuffer;
		}
	}

	@Bean
	public ServletRegistrationBean<AccountsMIS2> AccountsMIS2ServletBean() {
		ServletRegistrationBean<AccountsMIS2> bean = new ServletRegistrationBean<>(new AccountsMIS2(), "/SCFCorpDealerOutstandings");
		return bean;
	}
}

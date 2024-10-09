package com.arteriatech.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class DownlaodReport extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String FILE_NAME = null;
	private boolean emailSent = true;
	private boolean xlFileCreated = true;
	private int top = 5000;
	private File file=null;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		CommonUtils commonUtiles = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		String oDataUrl = "", password = "", userName = "", userPass = "";
		String executeUrl = "";
		String sumSecSales = "", secSales = "", userSyncSubmissionReport = "";// secSales
		boolean debug = false;
		Properties properties = new Properties();
		Workbook workbook = null;
		JsonObject emailRes = new JsonObject();
		JsonObject responseObj = new JsonObject();
		Set<String> ccEmails = new HashSet<>();
		JsonObject createdSheetRes = new JsonObject();
		String cpSpStkItems = "", cpSpStkItemNos = "", logID = "", agrgtrID = "", pcgoDataUrl = "", pcgUserName = "",
				pcgPassword = "", pcgUserPass = "";
		ODataLogs oDataLogs = new ODataLogs();
		String salesSummary = "", invoices = "", stockSummary = "", stockDetails = "", dataSubmission = "";
		int stepNo = 0;
		String scpGUID = "";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			salesSummary = properties.getProperty("SalesSummary");
			invoices = properties.getProperty("Invoices");
			stockSummary = properties.getProperty("StockSummary");
			stockDetails = properties.getProperty("StockDetails");
			dataSubmission = properties.getProperty("DataSubmission");
			inputPayload = commonUtiles.getGetBody(request, response);
			if (request.getParameter("AggregatorID") != null
					&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
				agrgtrID = request.getParameter("agrgtrID");
			}

			if (request.getParameter("SCPGUID") != null && !request.getParameter("SCPGUID").equalsIgnoreCase("")) {
				scpGUID = request.getParameter("SCPGUID");
			}
			if (request.getParameter("debug") != null && !request.getParameter("debug").equalsIgnoreCase("")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("Received Input Payload:" + jsonPayload);
			}
			
			userPass = userName + ":" + password;
			executeUrl=oDataUrl+"$metadata";
			JSONObject metadata = commonUtiles.executeMetadatURL(oDataUrl, userPass, response);
			userPass = userName + ":" + password;
			JSONArray entityArray = metadata.getJSONObject("edmx:Edmx").getJSONObject("edmx:DataServices").getJSONObject("Schema").getJSONArray("EntityType");
			Map<String,String> salesSummaryMetadata=new HashMap<>();
			Map<String,String> invoiceMeta=new HashMap<>();
			Map<String,String> dataSubmissionMetadata=new HashMap<>();
			for(int i=0;i<entityArray.length();i++) {
				JSONObject entity = entityArray.getJSONObject(i);
				if(entity.getString("Name").equalsIgnoreCase("V_SSCPSP_T-7_SUM_SECSALESType")) {
					JSONArray propertyArray = entity.getJSONArray("Property");
					for(int j=0;j<propertyArray.length();j++) {
						JSONObject jsonObject = propertyArray.getJSONObject(j);
						String dataType = jsonObject.getString("Type");
						String fieldName=jsonObject.getString("Name");
						if(fieldName.equalsIgnoreCase("SCPGuid"))
							salesSummaryMetadata.put(fieldName, dataType);
						if(fieldName.equalsIgnoreCase("SCPName1"))
							salesSummaryMetadata.put(fieldName, dataType);
						if(fieldName.equalsIgnoreCase("DmsDivision_I"))
							salesSummaryMetadata.put(fieldName, dataType);
						if(fieldName.equalsIgnoreCase("ASPGROSSAMT"))
							salesSummaryMetadata.put(fieldName, dataType);
						if(fieldName.equalsIgnoreCase("QUANTITYINBASEUOM"))
							salesSummaryMetadata.put(fieldName, dataType);
					}
				}
				
				if(entity.getString("Name").equalsIgnoreCase("V_SSCPSP_T-7_SECSALESType")){
					JSONArray propertyArray = entity.getJSONArray("Property");
					for(int j=0;j<propertyArray.length();j++) {
						JSONObject jsonObject = propertyArray.getJSONObject(j);
						String dataType = jsonObject.getString("Type");
						String fieldName=jsonObject.getString("Name");
						if(fieldName.equalsIgnoreCase("DMSOrg2"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DmsDivision_I"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DMSOrg3"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DMSOrg1"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("FromCPGuid"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("FromCPName"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("CountryID"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("InvoiceTypeDesc"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("InvoiceNo"))
							invoiceMeta.put(fieldName, dataType);
						
						else if(fieldName.equalsIgnoreCase("InvoiceDate"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("SoldToName"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("SoldToBPID"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ExternalSoldToCPName"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ItemNo"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("MaterialNo"))
							invoiceMeta.put(fieldName, dataType);
						
						else if(fieldName.equalsIgnoreCase("MaterialDesc"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ExternalMaterialDesc"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("SerialNo"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("Batch"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("InvoiceQty"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("UOM_I"))
							invoiceMeta.put(fieldName, dataType);
						
						else if(fieldName.equalsIgnoreCase("ItemUnitPrice"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("GrossAmount"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DiscountPerc"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ItemTotalDiscAmount"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("AssessableValue"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ItemTaxValue"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ItemNetAmount"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ItemNetAmountinRC"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ASPGrossAmount"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("InvoiceQtyASPUOM"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ASPUOM"))
							invoiceMeta.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("InvoiceStatusDesc"))
							invoiceMeta.put(fieldName, dataType);
					}
				
				}
				
				if(entity.getString("Name").equalsIgnoreCase("UserSyncSubmissionReportType")){
					JSONArray propertyArray = entity.getJSONArray("Property");
					for(int j=0;j<propertyArray.length();j++) {
						JSONObject jsonObject = propertyArray.getJSONObject(j);
						String dataType = jsonObject.getString("Type");
						String fieldName=jsonObject.getString("Name");
						if(fieldName.equalsIgnoreCase("PartnerID"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("CPName"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("ERPSoftware"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DaysLastStockSync"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DaysLastSalesSync"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("DaysLastGRSync"))
							dataSubmissionMetadata.put(fieldName, dataType);
						else if(fieldName.equalsIgnoreCase("LastInvDate"))
							dataSubmissionMetadata.put(fieldName, dataType);	
					}
				}
			}
			userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
			oDataUrl="https://hqas1c8c5b055e.ap1.hana.ondemand.com/AGGRMDT/ARTEC/SSGW_MIS/service.xsodata/";
			executeUrl = oDataUrl + userSyncSubmissionReport + "?$select=PartnerID,CPName,ERPSoftware,DaysLastStockSync,DaysLastSalesSync,DaysLastGRSync,LastInvDate"+"&$filter=SPGUID%20eq%20%27" + scpGUID
					+ "%27%20and%20AggregatorID%20eq%20%27" + "AGGRMDT" + "%27";
			if (debug) {
				response.getWriter().println("Data Submission execute Url:" + executeUrl);
			}
			JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
			if (debug) {
				response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
			}
			JsonArray dataSubmissingArr = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
					.get("results").getAsJsonArray();
			List<String>headers=new ArrayList<>();
			headers.add("Distributor Code");
			headers.add("Distributor Name");
			headers.add("ERP Software");
			headers.add("Days Last Stock Sync");
			headers.add("Days Last Sales Sync");
			headers.add("Days Last GR Sync");
			headers.add("Latest Invoice Date");
			headers.add("Distributor Code");
			workbook = new XSSFWorkbook();
				createdSheetRes = createXlSheet(dataSubmissingArr, workbook, dataSubmission, debug,
						response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,dataSubmissionMetadata, headers);
			pcgoDataUrl = commonUtiles.getODataDestinationProperties("URL", "PCGWHANA");
			pcgUserName = commonUtiles.getODataDestinationProperties("User", "PCGWHANA");
			pcgPassword = commonUtiles.getODataDestinationProperties("Password", "PCGWHANA");
			agrgtrID = commonUtiles.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			//pcgUserPass = pcgUserName + ":" + pcgPassword;
			if (agrgtrID != null && !agrgtrID.equalsIgnoreCase("") && scpGUID != null
					&& !scpGUID.equalsIgnoreCase("")) {
				String loginUser = commonUtiles.getUserPrincipal(request, "name", response);
				stepNo++;
				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "SendSalesReport", "Process Started",
						"" + stepNo, request.getServletPath(), pcgoDataUrl, pcgUserPass, agrgtrID, loginUser, debug);
				if (debug) {
					response.getWriter().println("ApplicationLogs logID:" + logID);
				}

				executeUrl = pcgoDataUrl + "ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27DMSADM"
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("ConfigTypsetTypeValues Url:" + executeUrl);
				}
				JsonObject ccEmailObj = commonUtiles.executeURL(executeUrl, pcgUserPass, response);
				if (debug) {
					response.getWriter()
							.println("Reterieved CC EmailIds from  ConfigTypsetTypeValues table:" + ccEmailObj);
				}
				JsonArray ccemailArray = ccEmailObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				if (ccemailArray != null && !ccemailArray.isJsonNull() && ccemailArray.size() > 0) {
					for (int i = 0; i < ccemailArray.size(); i++) {
						JsonObject ccEmailObjItem = ccemailArray.get(i).getAsJsonObject();
						if (!ccEmailObjItem.get("Types").isJsonNull()
								&& !ccEmailObjItem.get("Types").getAsString().equalsIgnoreCase("")) {
							String mailAddress = ccEmailObjItem.get("Types").getAsString();
							if (mailAddress.startsWith("WSRAUTML")) {
								if (!ccEmailObjItem.get("TypeValue").isJsonNull()
										&& !ccEmailObjItem.get("TypeValue").getAsString().equalsIgnoreCase("")) {
									ccEmails.add(ccEmailObjItem.get("TypeValue").getAsString());
								}
							}
						}
					}
				}
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", ccEmails.toString(),
						stepNo, "CC Email Address", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

				if (debug) {
					response.getWriter().println("List of CC Emaild Address:" + ccEmails);
				}

				workbook = new XSSFWorkbook();
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSMISHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSMISHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSMISHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println("oDataUrl  Url:" + oDataUrl);
				}
				sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
				executeUrl = oDataUrl + sumSecSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Sales Summary executeUrl:" + executeUrl);
				}
				JsonObject sumSecsalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Sales Summary Records from Db:" + sumSecsalesObj);
				}
				JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						sumSecSalesArray.size() + "", stepNo, "Total Sales Summary Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull() && sumSecSalesArray.size() > 0) {
					JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
					if (debug) {
						response.getWriter().println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
					}
					createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Sales Summary xl Sheet Created Response:" + createdSheetRes);
				}
				secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
				executeUrl = oDataUrl + secSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Invoices execute Url:" + executeUrl);
				}
				JsonObject secSalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Invoices Records from Db:" + secSalesObj);
				}
				JsonArray secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						secSalesObjArray.size() + "", stepNo, "Total Invoices Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (secSalesObjArray != null && !secSalesObjArray.isJsonNull() && secSalesObjArray.size() > 0) {
					JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(secSalesObjArray);
					if (debug) {
						response.getWriter().println("Invoices Filtered Records:" + filteredSecSalesRecords);
					}

					createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, invoices, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);

				}
				if (debug) {
					response.getWriter().println("Invoices xl Sheet Created Response:" + createdSheetRes);
				}

				userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
				executeUrl = oDataUrl + userSyncSubmissionReport + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Data Submission execute Url:" + executeUrl);
				}
				//JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
				}
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSGWHANA");
				userPass = userName + ":" + password;
				cpSpStkItems = properties.getProperty("V_SSCPSP_STKITMS");
				executeUrl = oDataUrl + cpSpStkItems + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Summary execute Url:" + executeUrl);
				}
				JsonObject cpSpStockItemsObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Stock Summary Records from Db:" + cpSpStockItemsObj);
				}
				JsonArray cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStockItemsObjArray.size() + "", stepNo, "Total Stock Summary Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
						&& cpSpStockItemsObjArray.size() > 0) {
					JsonArray filteredSTKITMSRecords = getFilteredSTKITMSRecords(cpSpStockItemsObjArray);
					if (debug) {
						response.getWriter().println("Stock Summary Filtered Records:" + filteredSTKITMSRecords);
					}

					createdSheetRes = createXlSheet(filteredSTKITMSRecords, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {
					createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}

				if (debug) {
					response.getWriter().println("Stock Summary Sheet Created Response:" + createdSheetRes);
				}
				cpSpStkItemNos = properties.getProperty("V_SSCPSP_STKITMSNOS");
				executeUrl = oDataUrl + cpSpStkItemNos + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Details execute Url:" + executeUrl);
				}
				JsonObject cpSpStkItemNosObj = commonUtiles.executeURL(executeUrl, userPass, response);

				if (debug) {
					response.getWriter().println("Stock Details Records from Db:" + cpSpStkItemNosObj);
				}
				JsonArray cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStkItemNosObjArray.size() + "", stepNo, "Total Stock Details Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
						&& cpSpStkItemNosObjArray.size() > 0) {
					JsonArray filteredStkItemNosRecords = getFiletredSTKITMSNOSRecords(cpSpStkItemNosObjArray);
					if (debug) {
						response.getWriter().println("Stock Details Filtered Records:" + filteredStkItemNosRecords);
						response.getWriter().println("Total Stock Details Records:" + filteredStkItemNosRecords.size());
						response.getWriter().println("Total Stock Details Record SP_GUID:" + scpGUID);
					}
					createdSheetRes = createXlSheet(filteredStkItemNosRecords, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Stock Details Sheet Created Response:" + createdSheetRes);
				}
				JsonArray usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
						.get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						usrSynSubmissionReportObjArray.size() + "", stepNo,
						"Total Data Submission Records for the SPGUID", pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "",
						"", debug);
				if (usrSynSubmissionReportObjArray != null && !usrSynSubmissionReportObjArray.isJsonNull()
						&& usrSynSubmissionReportObjArray.size() > 0) {
					JsonArray filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
							usrSynSubmissionReportObjArray);
					if (debug) {
						response.getWriter().println("Data Submission Filtered Records:" + filteredSubmissionReportRoc);
					}

					createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(usrSynSubmissionReportObjArray, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Data Submission Sheet Created Response:" + createdSheetRes);
				}
				if (xlFileCreated) {
					response.setContentType("application/vnd.ms-excel");
		            response.setHeader("Content-Disposition", "attachment; filename=SampleExcel.xls");
		            workbook.write(response.getOutputStream());
					//downloadFile(request, response);
				}else{
					responseObj.addProperty("Message", "xl file Not Created");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);
				}
			} else {
				responseObj.addProperty("Message", "Input Payload does not contain AggregatorID");
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);

			}

		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}

			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);

			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred", pcgoDataUrl, pcgUserPass, agrgtrID, ex.getLocalizedMessage(),
					ex.getClass().getCanonicalName(), "", debug);
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		}
	
	
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtiles = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		String oDataUrl = "", password = "", userName = "", userPass = "";
		String executeUrl = "";
		String sumSecSales = "", secSales = "", userSyncSubmissionReport = "";// secSales
		boolean debug = false;
		Properties properties = new Properties();
		Workbook workbook = null;
		JsonObject emailRes = new JsonObject();
		JsonObject responseObj = new JsonObject();
		Set<String> ccEmails = new HashSet<>();
		JsonObject createdSheetRes = new JsonObject();
		String cpSpStkItems = "", cpSpStkItemNos = "", logID = "", agrgtrID = "", pcgoDataUrl = "", pcgUserName = "",
				pcgPassword = "", pcgUserPass = "";
		ODataLogs oDataLogs = new ODataLogs();
		String salesSummary = "", invoices = "", stockSummary = "", stockDetails = "", dataSubmission = "";
		int stepNo = 0;
		String scpGUID = "";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			salesSummary = properties.getProperty("SalesSummary");
			invoices = properties.getProperty("Invoices");
			stockSummary = properties.getProperty("StockSummary");
			stockDetails = properties.getProperty("StockDetails");
			dataSubmission = properties.getProperty("DataSubmission");
			inputPayload = commonUtiles.getGetBody(request, response);
			if (request.getParameter("AggregatorID") != null
					&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
				agrgtrID = request.getParameter("agrgtrID");
			}

			if (request.getParameter("SCPGUID") != null && !request.getParameter("SCPGUID").equalsIgnoreCase("")) {
				scpGUID = request.getParameter("SCPGUID");
			}
			if (request.getParameter("debug") != null && !request.getParameter("debug").equalsIgnoreCase("")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("Received Input Payload:" + jsonPayload);
			}

			pcgoDataUrl = commonUtiles.getODataDestinationProperties("URL", "PCGWHANA");
			pcgUserName = commonUtiles.getODataDestinationProperties("User", "PCGWHANA");
			pcgPassword = commonUtiles.getODataDestinationProperties("Password", "PCGWHANA");
			agrgtrID = commonUtiles.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			if (agrgtrID != null && !agrgtrID.equalsIgnoreCase("") && scpGUID != null
					&& !scpGUID.equalsIgnoreCase("")) {
				String loginUser = commonUtiles.getUserPrincipal(request, "name", response);
				stepNo++;
				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "SendSalesReport", "Process Started",
						"" + stepNo, request.getServletPath(), pcgoDataUrl, pcgUserPass, agrgtrID, loginUser, debug);
				if (debug) {
					response.getWriter().println("ApplicationLogs logID:" + logID);
				}

				executeUrl = pcgoDataUrl + "ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27DMSADM"
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("ConfigTypsetTypeValues Url:" + executeUrl);
				}
				JsonObject ccEmailObj = commonUtiles.executeURL(executeUrl, pcgUserPass, response);
				if (debug) {
					response.getWriter()
							.println("Reterieved CC EmailIds from  ConfigTypsetTypeValues table:" + ccEmailObj);
				}
				JsonArray ccemailArray = ccEmailObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				if (ccemailArray != null && !ccemailArray.isJsonNull() && ccemailArray.size() > 0) {
					for (int i = 0; i < ccemailArray.size(); i++) {
						JsonObject ccEmailObjItem = ccemailArray.get(i).getAsJsonObject();
						if (!ccEmailObjItem.get("Types").isJsonNull()
								&& !ccEmailObjItem.get("Types").getAsString().equalsIgnoreCase("")) {
							String mailAddress = ccEmailObjItem.get("Types").getAsString();
							if (mailAddress.startsWith("WSRAUTML")) {
								if (!ccEmailObjItem.get("TypeValue").isJsonNull()
										&& !ccEmailObjItem.get("TypeValue").getAsString().equalsIgnoreCase("")) {
									ccEmails.add(ccEmailObjItem.get("TypeValue").getAsString());
								}
							}
						}
					}
				}
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", ccEmails.toString(),
						stepNo, "CC Email Address", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

				if (debug) {
					response.getWriter().println("List of CC Emaild Address:" + ccEmails);
				}

				workbook = new XSSFWorkbook();
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSMISHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSMISHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSMISHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println("oDataUrl  Url:" + oDataUrl);
				}
				sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
				executeUrl = oDataUrl + sumSecSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Sales Summary executeUrl:" + executeUrl);
				}
				JsonObject sumSecsalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Sales Summary Records from Db:" + sumSecsalesObj);
				}
				JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						sumSecSalesArray.size() + "", stepNo, "Total Sales Summary Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull() && sumSecSalesArray.size() > 0) {
					JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
					if (debug) {
						response.getWriter().println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
					}
					createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Sales Summary xl Sheet Created Response:" + createdSheetRes);
				}
				secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
				executeUrl = oDataUrl + secSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Invoices execute Url:" + executeUrl);
				}
				JsonObject secSalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Invoices Records from Db:" + secSalesObj);
				}
				JsonArray secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						secSalesObjArray.size() + "", stepNo, "Total Invoices Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (secSalesObjArray != null && !secSalesObjArray.isJsonNull() && secSalesObjArray.size() > 0) {
					JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(secSalesObjArray);
					if (debug) {
						response.getWriter().println("Invoices Filtered Records:" + filteredSecSalesRecords);
					}

					createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, invoices, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);

				}
				if (debug) {
					response.getWriter().println("Invoices xl Sheet Created Response:" + createdSheetRes);
				}

				userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
				executeUrl = oDataUrl + userSyncSubmissionReport + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Data Submission execute Url:" + executeUrl);
				}
				JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
				}
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSGWHANA");
				userPass = userName + ":" + password;
				cpSpStkItems = properties.getProperty("V_SSCPSP_STKITMS");
				executeUrl = oDataUrl + cpSpStkItems + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Summary execute Url:" + executeUrl);
				}
				JsonObject cpSpStockItemsObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Stock Summary Records from Db:" + cpSpStockItemsObj);
				}
				JsonArray cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStockItemsObjArray.size() + "", stepNo, "Total Stock Summary Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
						&& cpSpStockItemsObjArray.size() > 0) {
					JsonArray filteredSTKITMSRecords = getFilteredSTKITMSRecords(cpSpStockItemsObjArray);
					if (debug) {
						response.getWriter().println("Stock Summary Filtered Records:" + filteredSTKITMSRecords);
					}

					createdSheetRes = createXlSheet(filteredSTKITMSRecords, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {
					createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}

				if (debug) {
					response.getWriter().println("Stock Summary Sheet Created Response:" + createdSheetRes);
				}
				cpSpStkItemNos = properties.getProperty("V_SSCPSP_STKITMSNOS");
				executeUrl = oDataUrl + cpSpStkItemNos + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Details execute Url:" + executeUrl);
				}
				JsonObject cpSpStkItemNosObj = commonUtiles.executeURL(executeUrl, userPass, response);

				if (debug) {
					response.getWriter().println("Stock Details Records from Db:" + cpSpStkItemNosObj);
				}
				JsonArray cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStkItemNosObjArray.size() + "", stepNo, "Total Stock Details Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
						&& cpSpStkItemNosObjArray.size() > 0) {
					JsonArray filteredStkItemNosRecords = getFiletredSTKITMSNOSRecords(cpSpStkItemNosObjArray);
					if (debug) {
						response.getWriter().println("Stock Details Filtered Records:" + filteredStkItemNosRecords);
						response.getWriter().println("Total Stock Details Records:" + filteredStkItemNosRecords.size());
						response.getWriter().println("Total Stock Details Record SP_GUID:" + scpGUID);
					}
					createdSheetRes = createXlSheet(filteredStkItemNosRecords, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Stock Details Sheet Created Response:" + createdSheetRes);
				}
				JsonArray usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
						.get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						usrSynSubmissionReportObjArray.size() + "", stepNo,
						"Total Data Submission Records for the SPGUID", pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "",
						"", debug);
				if (usrSynSubmissionReportObjArray != null && !usrSynSubmissionReportObjArray.isJsonNull()
						&& usrSynSubmissionReportObjArray.size() > 0) {
					JsonArray filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
							usrSynSubmissionReportObjArray);
					if (debug) {
						response.getWriter().println("Data Submission Filtered Records:" + filteredSubmissionReportRoc);
					}

					createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(usrSynSubmissionReportObjArray, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Data Submission Sheet Created Response:" + createdSheetRes);
				}
				if (xlFileCreated) {
					downloadFile(request, response);
				}else{
					responseObj.addProperty("Message", "xl file Not Created");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);
				}
			} else {
				responseObj.addProperty("Message", "Input Payload does not contain AggregatorID");
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);

			}

		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}

			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);

			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred", pcgoDataUrl, pcgUserPass, agrgtrID, ex.getLocalizedMessage(),
					ex.getClass().getCanonicalName(), "", debug);
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		}
	}

	public JsonObject createXlSheet(JsonArray array, Workbook workbook, String sheetName, boolean debug,
			HttpServletResponse response, HttpServletRequest request, String logID, int stepNo, String oDataUrl,
			String userPass, String agrgtrID, ODataLogs oDataLogs,boolean isHearRequired) throws Exception {
		JsonObject resultjsonObj = new JsonObject();

		try {
			Sheet sheet1 = workbook.createSheet(sheetName);
			sheet1.setColumnWidth(0, 6000);
			sheet1.setColumnWidth(1, 4000);
			Row header = sheet1.createRow(0);
			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			AtomicInteger keyNum = new AtomicInteger(0);
			if (array != null && array.size() > 0 && !array.isJsonNull()) {
				JsonObject asJsonObject = array.get(0).getAsJsonObject();
				Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
				CellStyle style = workbook.createCellStyle();
				style.setWrapText(true);
					entrySet.forEach(json -> {
						String key = json.getKey();
						if (!key.equalsIgnoreCase("__metadata")) {
							Cell createCell = header.createCell(keyNum.getAndIncrement());
							if (key.equalsIgnoreCase("Invoice Status") && sheetName.equalsIgnoreCase("Invoices")) {
								createCell.setCellStyle(style);
							}
							if (sheetName.equalsIgnoreCase("Data Submission")
									&& (key.equalsIgnoreCase("Latest Invoice Date")
											|| key.equalsIgnoreCase("Days Last Stock Sync")
											|| key.equalsIgnoreCase("Days Last GR Sync"))) {
								createCell.setCellStyle(style);
							}
							if (sheetName.equalsIgnoreCase("Stock Details")
									&& (key.equalsIgnoreCase("Business Unit") || key.equalsIgnoreCase("Material No"))) {
								createCell.setCellStyle(style);
							}
							if (sheetName.equalsIgnoreCase("Stock Summary") && key.equalsIgnoreCase("Business Unit")) {
								createCell.setCellStyle(style);
							}
							if (sheetName.equalsIgnoreCase("Sales Summary")
									&& key.equalsIgnoreCase("Distributor Name")) {
								createCell.setCellStyle(style);
							}
							createCell.setCellValue(key);
						}
					});
					
				int rowNum = 1;
				//int rowNum=sheet1.getLastRowNum();
				List<Double> grossAmt = new ArrayList<>();
				List<Integer> totalQty = new ArrayList<>();
				for (int i = 0; i < array.size(); i++, rowNum++) {
					JsonObject asJsonObject2 = array.get(i).getAsJsonObject();
					Row row = sheet1.createRow(rowNum);
					AtomicInteger cellNum = new AtomicInteger(0);
					Set<Entry<String, JsonElement>> entrySet2 = asJsonObject2.entrySet();
					entrySet2.forEach(jsonObj -> {
						String key = jsonObj.getKey();
						if (sheetName.equalsIgnoreCase("Sales Summary") && key.equalsIgnoreCase("ASP Gross Amount")) {
							if (!asJsonObject2.get(key).getAsString().equalsIgnoreCase("")) {
								double convertStrtoDouble = convertStrtoDouble(asJsonObject2.get(key).getAsString());
								grossAmt.add(convertStrtoDouble);
							}
						}
						if (sheetName.equalsIgnoreCase("Sales Summary")
								&& key.equalsIgnoreCase("Quantity in Base UOM")) {
							if (!asJsonObject2.get(key).getAsString().equalsIgnoreCase("")) {
								int convertStrToInt = convertStrToInt(asJsonObject2.get(key).getAsString());
								totalQty.add(convertStrToInt);
							}
						}
						if (!key.equalsIgnoreCase("__metadata")) {
							if (!asJsonObject2.get(key).isJsonNull()) {
								String value = asJsonObject2.get(key).getAsString();
								Cell cell = row.createCell(cellNum.getAndIncrement());
								if (key.equalsIgnoreCase("Distributor Name") || key.equalsIgnoreCase("Invoice Status")
										|| key.equalsIgnoreCase("Business Unit") || key.equalsIgnoreCase("Material No")
										|| key.equalsIgnoreCase("Latest Invoice Date")
										|| key.equalsIgnoreCase("Days Last Stock Sync")
										|| key.equalsIgnoreCase("Days Last Sales Sync")
										|| key.equalsIgnoreCase("Days Last GR Sync")) {
									cell.setCellStyle(style);
								}
								if (value.startsWith("/Date")) {
									Date convertLongToDate = convertLongToDate(value);
									if (convertLongToDate != null) {
										cell.setCellValue(convertLongToDate);
									} else {
										cell.setCellValue(value);
									}
									/* cell.setCellStyle(style); */
								} else {
									boolean isNumber = NumberUtils.isNumber(value);
									if (isNumber) {
										if (value.contains(".")) {
											try {
												cell.setCellValue(Double.parseDouble(value));
											} catch (NumberFormatException ex) {
												cell.setCellValue(value);
											} catch (Exception ex) {
												cell.setCellValue(value);
											}
										} else {
											try {
												cell.setCellValue(Integer.parseInt(value));
											} catch (NumberFormatException ex) {
												cell.setCellValue(value);
											} catch (Exception ex) {
												cell.setCellValue(value);
											}
										}

									} else {
										cell.setCellValue(value);
									}

									/* cell.setCellStyle(style); */
								}
							} else {
								Cell cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
								/* cell.setCellStyle(style); */

							}

						}

					});
					sheet1.autoSizeColumn(i);
				}
				if (sheetName.equalsIgnoreCase("Sales Summary")) {
					Row grandTotal = sheet1.createRow(rowNum);
					Cell grossCell = grandTotal.createCell(1);
					grossCell.setCellValue("Grand Total");
					if (grossAmt != null && !grossAmt.isEmpty()) {
						Double totalAmt = grossAmt.stream().collect(Collectors.summingDouble(Double::doubleValue));
						Cell cellGrossAmt = grandTotal.createCell(3);
						style.setAlignment(HorizontalAlignment.LEFT);
						cellGrossAmt.setCellValue(totalAmt);
						cellGrossAmt.setCellStyle(style);
					}
					if (totalQty != null && !totalQty.isEmpty()) {
						Integer totaqt = totalQty.stream().collect(Collectors.summingInt(Integer::intValue));
						Cell cellGrossAmt = grandTotal.createCell(4);
						style.setAlignment(HorizontalAlignment.LEFT);
						cellGrossAmt.setCellValue(totaqt);
						cellGrossAmt.setCellStyle(style);
					}
				}
				if (debug) {
					response.getWriter().println("File Created in this Path :" + FILE_NAME);
				}
			}
			if (FILE_NAME == null) {
				getFilePath(request);
			}
			if (debug) {
				response.getWriter().println("File Created in the Path:" + FILE_NAME);
			}
		/*	FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
			workbook.write(outputStream);
			// call the Send Email Method
			outputStream.flush();*/
			resultjsonObj.addProperty("Status", "000001");
			resultjsonObj.addProperty("ErrorCode", "");
			resultjsonObj.addProperty("Message", sheetName + "Created Successfully");
			return resultjsonObj;

		} catch (IOException ex) {
			xlFileCreated = false;
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			if (debug)
				response.getWriter().println(buffer.toString());
			
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
			 
			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		} catch (Exception ex) {
			xlFileCreated = false;
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug)
				response.getWriter().println(buffer.toString());
			
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
			 
			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		}
		return resultjsonObj;

	}

	public Date convertLongToDate(String dateString) {
		String returnValue = "", longDateValue = "";
		long dateValue = 0;
		Date updatedDate = null;
		try {
			longDateValue = dateString.substring((dateString.indexOf("(") + 1), dateString.lastIndexOf(")"));
			dateValue = Long.parseLong(longDateValue);
			Date date = new Date(dateValue);
			SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
			returnValue = df2.format(date);
			updatedDate = df2.parse(returnValue);
		} catch (NumberFormatException ex) {
			return updatedDate;
		} catch (Exception e) {
			return updatedDate;
		}
		return updatedDate;

	}

	public void getFilePath(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		/*String rootPath = System.getProperty("catalina.home");
		ServletContext ctx = getServletContext();
		String relativePath = ctx.getInitParameter("tempfile.dir");
		String path = rootPath + File.separator + relativePath + File.separator + ";
*/		
		String filePath = getServletContext().getRealPath("/Resources/XlSheet/");
		FILE_NAME = filePath + "Sales & Stock Report.csv";
		
		File file=new File(FILE_NAME);
		if(file.exists())
			file.delete();
		

	}

	private JsonObject sendEmail(String emailId, boolean debug, HttpServletResponse response, String filePath,
			Set<String> ccAddressList, JsonObject mailContent, ODataLogs oDataLogs, int stepNo,
			HttpServletRequest request, String oDataUrl, String logID, String spGUID, String userPass, String agrgtrID)
			throws Exception {
		Properties properties = new Properties();
		JsonObject retunObj = new JsonObject();
		try {

			File file = new File(filePath);
			CommonUtils commonUtils = new CommonUtils();
			final String userName = commonUtils.getODataDestinationProperties("emailid", "PlatformEmail");
			final String passWord = commonUtils.getODataDestinationProperties("emailpass", "PlatformEmail");
			/*
			 * final String userName="saikiran.indla@arteriatech.com"; final
			 * String passWord="Sai@85499";
			 */
			String emailSubject = "Reg: Weekly Sales Report & Stock Report";
			if (debug) {
				response.getWriter().println("Email " + userName);
				response.getWriter().println("Password " + passWord);
				response.getWriter().println("Email Subject:" + emailSubject);
			}
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (debug) {
				response.getWriter().println("file path :" + filePath);
			}
			if (file.exists()) {
				if (debug) {
					response.getWriter().println("Recipient Email address :" + emailId);
				}
				Properties emailProperties = getEmailProperties();
				Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
					protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(userName, passWord);
					}
				});
				MimeMessage msg = new MimeMessage(session);
				try {
					msg.setFrom(new InternetAddress(userName));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailId));
					if (!ccAddressList.isEmpty() && ccAddressList.size() > 0) {
						Iterator<String> iterator = ccAddressList.iterator();
						while (iterator.hasNext()) {
							String email = iterator.next();
							InternetAddress ccAddress = new InternetAddress(email);
							msg.addRecipient(Message.RecipientType.CC, ccAddress);
						}
					}
					msg.setSubject(emailSubject);
					Multipart emailContent = new MimeMultipart();
					MimeBodyPart textBodyPart = new MimeBodyPart();
					if (!mailContent.get("FirstName").isJsonNull()
							&& !mailContent.get("FirstName").getAsString().equalsIgnoreCase("")) {
						textBodyPart.setText("Dear " + mailContent.get("FirstName").getAsString() + ","
								+ "\nPlease find the enclosed Weekly Sales Report & Stock Report.\n\nThanks & Regards\nMedtronic DMS Admin Team");
					} else {
						textBodyPart.setText(
								"Dear Sir/Madam,\nPlease find the enclosed Weekly Sales Report & Stock Report.\n\nThanks & Regards\nMedtronic DMS Admin Team");
					}
					MimeBodyPart pdfAttachment = new MimeBodyPart();
					/* pdfAttachment.attachFile(file.getPath()); */
					pdfAttachment.attachFile(filePath);
					if (debug) {
						response.getWriter().println("file attached successfully");
					}
					emailContent.addBodyPart(textBodyPart);
					emailContent.addBodyPart(pdfAttachment);
					msg.setContent(emailContent);
					long startTime = System.currentTimeMillis();
					Transport.send(msg);
					long endTime = System.currentTimeMillis();
					stepNo++;
					Path path = Paths.get(filePath);
					long fileSize = Files.size(path);
					if (debug) {
						response.getWriter()
								.println("Attached File Size:" + String.format(String.format("%,d bytes", fileSize)));
					}
					oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
							"Attached File Size: " + String.format("%,d bytes", fileSize), stepNo,
							"Time taken to delivery an email:" + (endTime - startTime) + " Milli Seconds", oDataUrl,
							userPass, agrgtrID, "", "Recipient Email ID: " + emailId, " SPGUID:" + spGUID, debug);
					retunObj.addProperty("Message", "Mail Sent Successfully");
					retunObj.addProperty("ErrorCode", "");
					retunObj.addProperty("Status", "000001");
					file.delete();
					FILE_NAME = null;
					if (debug) {
						response.getWriter().println("File Deleted Successfully");
					}
					return retunObj;

				} catch (SendFailedException ex) {
					emailSent = false;

					StackTraceElement element[] = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < element.length; i++) {
						buffer.append(element[i]);
					}
					stepNo++;
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
							stepNo, "Exception Occurred While Sending an Email", oDataUrl, userPass, agrgtrID,
							ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
					retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
					retunObj.addProperty("Message",
							ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
					retunObj.addProperty("Status", "000002");
					retunObj.addProperty("ErrorCode", "J002");

					return retunObj;
				} catch (MessagingException ex) {
					emailSent = false;
					StackTraceElement element[] = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < element.length; i++) {
						buffer.append(element[i]);
					}
					stepNo++;
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
							stepNo, "Exception Occurred While Sending an Email", oDataUrl, userPass, agrgtrID,
							ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
					retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
					retunObj.addProperty("Message",
							ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
					retunObj.addProperty("Status", "000002");
					retunObj.addProperty("ErrorCode", "J002");
					return retunObj;
				} catch (IOException ex) {
					emailSent = false;
					StackTraceElement element[] = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < element.length; i++) {
						buffer.append(element[i]);
					}
					stepNo++;
					oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
							stepNo, "Exception Occurred while Sending an Email", oDataUrl, userPass, agrgtrID,
							ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
					retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
					retunObj.addProperty("Message",
							ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
					retunObj.addProperty("Status", "000002");
					retunObj.addProperty("ErrorCode", "J002");
					return retunObj;

				}

			}

		} catch (Exception ex) {
			emailSent = false;
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred", oDataUrl, userPass, agrgtrID, ex.getLocalizedMessage(),
					ex.getClass().getCanonicalName(), "", debug);
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			return retunObj;

		}
		return retunObj;
	}

	public Properties getEmailProperties() throws Exception {
		Properties properties = new Properties();
		Properties emailProps = new Properties();
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			emailProps.put("mail.smtp.auth", properties.getProperty("mail.smtp.auth"));
			emailProps.put("mail.smtp.starttls.enable", properties.getProperty("mail.smtp.starttls.enable"));
			/* properties.put("mail.smtp.host", "74.208.5.2"); */
			emailProps.put("mail.smtp.host", properties.getProperty("mail.smtp.host"));
			emailProps.put("mail.smtp.port", properties.getProperty("mail.smtp.port"));

		} catch (Exception ex) {
			throw ex;
		}
		return emailProps;

	}

	

	private JsonArray getSumSecsalesFilteredColumns(JsonArray array) throws Exception {
		JsonArray updatedArray = new JsonArray();
		try {
			for (int i = 0; i < array.size(); i++) {
				JsonObject asJsonObject = array.get(i).getAsJsonObject();
				JsonObject newJson = new JsonObject();
				if (asJsonObject.has("SCPGuid") && !asJsonObject.get("SCPGuid").isJsonNull()) {
					newJson.addProperty("Distributor Code", asJsonObject.get("SCPGuid").getAsString());
				} else {
					newJson.addProperty("Distributor Code", "");
				}
				if (asJsonObject.has("SCPName1") && !asJsonObject.get("SCPName1").isJsonNull()) {
					newJson.addProperty("Distributor Name", asJsonObject.get("SCPName1").getAsString());
				} else {
					newJson.addProperty("Distributor Name", "");
				}

				if (asJsonObject.has("DmsDivision_I") && !asJsonObject.get("DmsDivision_I").isJsonNull()) {
					newJson.addProperty("Operating Unit", asJsonObject.get("DmsDivision_I").getAsString());
				} else {
					newJson.addProperty("Operating Unit", "");
				}
				if (asJsonObject.has("ASPGROSSAMT") && !asJsonObject.get("ASPGROSSAMT").isJsonNull()) {
					newJson.addProperty("ASP Gross Amount", asJsonObject.get("ASPGROSSAMT").getAsString());
				} else {
					newJson.addProperty("ASP Gross Amount", "");
				}
				if (asJsonObject.has("QUANTITYINBASEUOM") && !asJsonObject.get("QUANTITYINBASEUOM").isJsonNull()) {
					newJson.addProperty("Quantity in Base UOM", asJsonObject.get("QUANTITYINBASEUOM").getAsString());

				} else {
					newJson.addProperty("Quantity in Base UOM", "");
				}
				updatedArray.add(newJson);
			}
			return updatedArray;
		} catch (Exception ex) {
			throw ex;

		}
	}

	private JsonArray getFilteredSECSALESRecords(JsonArray array) throws Exception {
		JsonArray updatedArray = new JsonArray();
		try {
			for (int i = 0; i < array.size(); i++) {
				JsonObject asJsonObject = array.get(i).getAsJsonObject();
				JsonObject updatedObj = new JsonObject();

				if (asJsonObject.has("DMSOrg2") && !asJsonObject.get("DMSOrg2").isJsonNull()) {
					updatedObj.addProperty("Portfolio", asJsonObject.get("DMSOrg2").getAsString());
				} else {
					updatedObj.addProperty("Portfolio", "");
				}
				if (asJsonObject.has("DmsDivision_I") && !asJsonObject.get("DmsDivision_I").isJsonNull()) {
					updatedObj.addProperty("Operating Unit", asJsonObject.get("DmsDivision_I").getAsString());
				} else {
					updatedObj.addProperty("Operating Unit", "");
				}
				if (asJsonObject.has("DMSOrg3") && !asJsonObject.get("DMSOrg3").isJsonNull()) {
					updatedObj.addProperty("Business Unit", asJsonObject.get("DMSOrg3").getAsString());
				} else {
					updatedObj.addProperty("Business Unit", "");
				}

				if (asJsonObject.has("DMSOrg1") && !asJsonObject.get("DMSOrg1").isJsonNull()) {
					updatedObj.addProperty("Division", asJsonObject.get("DMSOrg1").getAsString());
				} else {
					updatedObj.addProperty("Division", "");
				}
				if (asJsonObject.has("FromCPGuid") && !asJsonObject.get("FromCPGuid").isJsonNull()) {
					updatedObj.addProperty("Distributor Code", asJsonObject.get("FromCPGuid").getAsString());
				} else {
					updatedObj.addProperty("Distributor Code", "");
				}

				if (asJsonObject.has("FromCPName") && !asJsonObject.get("FromCPName").isJsonNull()) {
					updatedObj.addProperty("Distributor Name", asJsonObject.get("FromCPName").getAsString());
				} else {
					updatedObj.addProperty("Distributor Name", "");
				}

				if (asJsonObject.has("CountryID") && !asJsonObject.get("CountryID").isJsonNull()) {
					updatedObj.addProperty("Country", asJsonObject.get("CountryID").getAsString());
				} else {
					updatedObj.addProperty("Country", "");
				}

				if (asJsonObject.has("InvoiceTypeDesc") && !asJsonObject.get("InvoiceTypeDesc").isJsonNull()) {
					updatedObj.addProperty("Invoice Type Desc", asJsonObject.get("InvoiceTypeDesc").getAsString());
				} else {
					updatedObj.addProperty("Invoice Type Desc", "");
				}

				if (asJsonObject.has("InvoiceNo") && !asJsonObject.get("InvoiceNo").isJsonNull()) {
					updatedObj.addProperty("Invoice No", asJsonObject.get("InvoiceNo").getAsString());
				} else {
					updatedObj.addProperty("Invoice No", "");
				}

				if (asJsonObject.has("InvoiceDate") && !asJsonObject.get("InvoiceDate").isJsonNull()) {
					updatedObj.addProperty("Invoice Date", asJsonObject.get("InvoiceDate").getAsString());
				} else {
					updatedObj.addProperty("Invoice Date", "");
				}

				if (asJsonObject.has("SoldToName") && !asJsonObject.get("SoldToName").isJsonNull()) {
					updatedObj.addProperty("Sold To Party Name", asJsonObject.get("SoldToName").getAsString());
				} else {
					updatedObj.addProperty("Sold To Party Name", "");
				}

				if (asJsonObject.has("SoldToBPID") && !asJsonObject.get("SoldToBPID").isJsonNull()) {
					updatedObj.addProperty("Sold To Party Code", asJsonObject.get("SoldToBPID").getAsString());
				} else {
					updatedObj.addProperty("Sold To Party Code", "");
				}

				if (asJsonObject.has("ExternalSoldToCPName")
						&& !asJsonObject.get("ExternalSoldToCPName").isJsonNull()) {
					updatedObj.addProperty("Source Sold To Party Name",
							asJsonObject.get("ExternalSoldToCPName").getAsString());
				} else {
					updatedObj.addProperty("Source Sold To Party Name", "");
				}
				if (asJsonObject.has("ItemNo") && !asJsonObject.get("ItemNo").isJsonNull()) {
					updatedObj.addProperty("Item No", asJsonObject.get("ItemNo").getAsString());
				} else {
					updatedObj.addProperty("Item No", "");
				}
				if (asJsonObject.has("MaterialNo") && !asJsonObject.get("MaterialNo").isJsonNull()) {
					updatedObj.addProperty("Material No", asJsonObject.get("MaterialNo").getAsString());
				} else {
					updatedObj.addProperty("Material No", "");
				}
				if (asJsonObject.has("MaterialDesc") && !asJsonObject.get("MaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Material Description", asJsonObject.get("MaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Material Description", "");
				}

				if (asJsonObject.has("ExternalMaterialDesc")
						&& !asJsonObject.get("ExternalMaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Source Material Description",
							asJsonObject.get("ExternalMaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Source Material Description", "");
				}

				if (asJsonObject.has("SerialNo") && !asJsonObject.get("SerialNo").isJsonNull()) {
					updatedObj.addProperty("SerialNo", asJsonObject.get("SerialNo").getAsString());
				} else {
					updatedObj.addProperty("SerialNo", "");
				}

				if (asJsonObject.has("Batch") && !asJsonObject.get("Batch").isJsonNull()) {
					updatedObj.addProperty("Batch", asJsonObject.get("Batch").getAsString());
				} else {
					updatedObj.addProperty("Batch", "");
				}
				if (asJsonObject.has("InvoiceQty") && !asJsonObject.get("InvoiceQty").isJsonNull()) {
					updatedObj.addProperty("Quantity", asJsonObject.get("InvoiceQty").getAsString());
				} else {
					updatedObj.addProperty("Quantity", "");
				}
				if (asJsonObject.has("UOM_I") && !asJsonObject.get("UOM_I").isJsonNull()) {
					updatedObj.addProperty("UOM", asJsonObject.get("UOM_I").getAsString());
				} else {
					updatedObj.addProperty("UOM", "");
				}

				if (asJsonObject.has("ItemUnitPrice") && !asJsonObject.get("ItemUnitPrice").isJsonNull()) {
					updatedObj.addProperty("Unit Price", asJsonObject.get("ItemUnitPrice").getAsString());
				} else {
					updatedObj.addProperty("Unit Price", "");
				}
				if (asJsonObject.has("GrossAmount") && !asJsonObject.get("GrossAmount").isJsonNull()) {
					updatedObj.addProperty("Gross Amount", asJsonObject.get("GrossAmount").getAsString());
				} else {
					updatedObj.addProperty("Gross Amount", "");
				}

				if (asJsonObject.has("DiscountPerc") && !asJsonObject.get("DiscountPerc").isJsonNull()) {
					updatedObj.addProperty("Discount %", asJsonObject.get("DiscountPerc").getAsString());
				} else {
					updatedObj.addProperty("Discount %", "");
				}

				if (asJsonObject.has("ItemTotalDiscAmount") && !asJsonObject.get("ItemTotalDiscAmount").isJsonNull()) {
					updatedObj.addProperty("Discount Amount", asJsonObject.get("ItemTotalDiscAmount").getAsString());
				} else {
					updatedObj.addProperty("Discount Amount", "");
				}

				if (asJsonObject.has("AssessableValue") && !asJsonObject.get("AssessableValue").isJsonNull()) {
					updatedObj.addProperty("Taxable Amount", asJsonObject.get("AssessableValue").getAsString());
				} else {
					updatedObj.addProperty("Taxable Amount", "");
				}

				if (asJsonObject.has("ItemTaxValue") && !asJsonObject.get("ItemTaxValue").isJsonNull()) {
					updatedObj.addProperty("Tax Amount", asJsonObject.get("ItemTaxValue").getAsString());
				} else {
					updatedObj.addProperty("Tax Amount", "");
				}
				if (asJsonObject.has("ItemNetAmount") && !asJsonObject.get("ItemNetAmount").isJsonNull()) {
					updatedObj.addProperty("Net Amount", asJsonObject.get("ItemNetAmount").getAsString());
				} else {
					updatedObj.addProperty("Net Amount", "");
				}

				if (asJsonObject.has("ItemNetAmountinRC") && !asJsonObject.get("ItemNetAmountinRC").isJsonNull()) {
					updatedObj.addProperty("Net Amount in USD", asJsonObject.get("ItemNetAmountinRC").getAsString());
				} else {
					updatedObj.addProperty("Net Amount in USD", "");
				}

				if (asJsonObject.has("ASPGrossAmount") && !asJsonObject.get("ASPGrossAmount").isJsonNull()) {
					updatedObj.addProperty("ASP in USD", asJsonObject.get("ASPGrossAmount").getAsString());
				} else {
					updatedObj.addProperty("ASP in USD", "");
				}

				if (asJsonObject.has("InvoiceQtyASPUOM") && !asJsonObject.get("InvoiceQtyASPUOM").isJsonNull()) {
					updatedObj.addProperty("Quantity in Base UOM", asJsonObject.get("InvoiceQtyASPUOM").getAsString());
				} else {
					updatedObj.addProperty("Quantity in Base UOM", "");
				}

				if (asJsonObject.has("ASPUOM") && !asJsonObject.get("ASPUOM").isJsonNull()) {
					updatedObj.addProperty("ASP UOM", asJsonObject.get("ASPUOM").getAsString());
				} else {
					updatedObj.addProperty("ASP UOM", "");
				}

				if (asJsonObject.has("InvoiceStatusDesc") && !asJsonObject.get("InvoiceStatusDesc").isJsonNull()) {
					updatedObj.addProperty("Invoice Status", asJsonObject.get("InvoiceStatusDesc").getAsString());
				} else {
					updatedObj.addProperty("Invoice Status", "");
				}
				updatedArray.add(updatedObj);
			}
			return updatedArray;
		} catch (Exception ex) {
			throw ex;

		}

	}

	private JsonArray getFilteredSTKITMSRecords(JsonArray array) {
		JsonArray updatedArray = new JsonArray();
		try {
			for (int i = 0; i < array.size(); i++) {
				JsonObject asJsonObject = array.get(i).getAsJsonObject();

				JsonObject updatedObj = new JsonObject();

				if (asJsonObject.has("DMSOrg2") && !asJsonObject.get("DMSOrg2").isJsonNull()) {
					updatedObj.addProperty("Portfolio", asJsonObject.get("DMSOrg2").getAsString());
				} else {
					updatedObj.addProperty("Portfolio", "");
				}
				if (asJsonObject.has("DMSDivision") && !asJsonObject.get("DMSDivision").isJsonNull()) {
					updatedObj.addProperty("Operating Unit", asJsonObject.get("DMSDivision").getAsString());
				} else {
					updatedObj.addProperty("Operating Unit", "");
				}

				if (asJsonObject.has("DMSOrg3") && !asJsonObject.get("DMSOrg3").isJsonNull()) {
					updatedObj.addProperty("Business Unit", asJsonObject.get("DMSOrg3").getAsString());
				} else {
					updatedObj.addProperty("Business Unit", "");
				}

				if (asJsonObject.has("DMSOrg1") && !asJsonObject.get("DMSOrg1").isJsonNull()) {
					updatedObj.addProperty("Division", asJsonObject.get("DMSOrg1").getAsString());
				} else {
					updatedObj.addProperty("Division", "");
				}

				if (asJsonObject.has("StockOwnerID") && !asJsonObject.get("StockOwnerID").isJsonNull()) {
					updatedObj.addProperty("Distributor Code", asJsonObject.get("StockOwnerID").getAsString());
				} else {
					updatedObj.addProperty("Distributor Code", "");
				}

				if (asJsonObject.has("StockOwnerName") && !asJsonObject.get("StockOwnerName").isJsonNull()) {
					updatedObj.addProperty("Distributor Name", asJsonObject.get("StockOwnerName").getAsString());
				} else {
					updatedObj.addProperty("Distributor Name", "");
				}

				if (asJsonObject.has("CountryID") && !asJsonObject.get("CountryID").isJsonNull()) {
					updatedObj.addProperty("Country", asJsonObject.get("CountryID").getAsString());
				} else {
					updatedObj.addProperty("Country", "");
				}
				if (asJsonObject.has("MaterialNo") && !asJsonObject.get("MaterialNo").isJsonNull()) {
					updatedObj.addProperty("Material No", asJsonObject.get("MaterialNo").getAsString());
				} else {
					updatedObj.addProperty("Material No", "");
				}
				if (asJsonObject.has("MaterialDesc") && !asJsonObject.get("MaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Material Description", asJsonObject.get("MaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Material Description", "");
				}
				if (asJsonObject.has("ExternalMaterialDesc")
						&& !asJsonObject.get("ExternalMaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Source Material Description",
							asJsonObject.get("ExternalMaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Source Material Description", "");
				}

				if (asJsonObject.has("StorageLocation") && !asJsonObject.get("StorageLocation").isJsonNull()) {
					updatedObj.addProperty("Storage Location", asJsonObject.get("StorageLocation").getAsString());
				} else {
					updatedObj.addProperty("Storage Location", "");
				}
				if (asJsonObject.has("AsOnDate") && !asJsonObject.get("AsOnDate").isJsonNull()) {
					updatedObj.addProperty("As On Date", asJsonObject.get("AsOnDate").getAsString());
				} else {
					updatedObj.addProperty("As On Date", "");
				}

				if (asJsonObject.has("UnrestrictedQty") && !asJsonObject.get("UnrestrictedQty").isJsonNull()) {
					updatedObj.addProperty("Quantity", asJsonObject.get("UnrestrictedQty").getAsString());
				} else {
					updatedObj.addProperty("Quantity", "");
				}
				if (asJsonObject.has("BlockedQty") && !asJsonObject.get("BlockedQty").isJsonNull()) {
					updatedObj.addProperty("Blocked Qty", asJsonObject.get("BlockedQty").getAsString());
				} else {
					updatedObj.addProperty("Blocked Qty", "");
				}

				if (asJsonObject.has("ExpiredQty") && !asJsonObject.get("ExpiredQty").isJsonNull()) {
					updatedObj.addProperty("Expired Qty", asJsonObject.get("ExpiredQty").getAsString());
				} else {
					updatedObj.addProperty("Expired Qty", "");
				}

				if (asJsonObject.has("AvailableQty") && !asJsonObject.get("AvailableQty").isJsonNull()) {
					updatedObj.addProperty("Available Qty", asJsonObject.get("AvailableQty").getAsString());
				} else {
					updatedObj.addProperty("Available Qty", "");
				}

				if (asJsonObject.has("UOM") && !asJsonObject.get("UOM").isJsonNull()) {
					updatedObj.addProperty("UOM", asJsonObject.get("UOM").getAsString());
				} else {
					updatedObj.addProperty("UOM", "");
				}

				if (asJsonObject.has("AvailableQtyBaseUOM") && !asJsonObject.get("AvailableQtyBaseUOM").isJsonNull()) {
					updatedObj.addProperty("Available Qty in Base UOM",
							asJsonObject.get("AvailableQtyBaseUOM").getAsString());
				} else {
					updatedObj.addProperty("Available Qty in Base UOM", "");
				}

				if (asJsonObject.has("BaseUOM") && !asJsonObject.get("BaseUOM").isJsonNull()) {
					updatedObj.addProperty("ASP UOM", asJsonObject.get("BaseUOM").getAsString());
				} else {
					updatedObj.addProperty("ASP UOM", "");
				}

				if (asJsonObject.has("StockValue") && !asJsonObject.get("StockValue").isJsonNull()) {
					updatedObj.addProperty("Stock Value", asJsonObject.get("StockValue").getAsString());
				} else {
					updatedObj.addProperty("Stock Value", "");
				}

				if (asJsonObject.has("StockValueInRC") && !asJsonObject.get("StockValueInRC").isJsonNull()) {
					updatedObj.addProperty("Stock Value in USD", asJsonObject.get("StockValueInRC").getAsString());
				} else {
					updatedObj.addProperty("Stock Value in USD", "");
				}
				updatedArray.add(updatedObj);

			}
			return updatedArray;
		} catch (Exception ex) {
			throw ex;

		}
	}

	private JsonArray getFiletredSTKITMSNOSRecords(JsonArray array) {

		JsonArray updatedArray = new JsonArray();
		try {
			for (int i = 0; i < array.size(); i++) {
				JsonObject asJsonObject = array.get(i).getAsJsonObject();
				JsonObject updatedObj = new JsonObject();

				if (asJsonObject.has("DMSOrg2") && !asJsonObject.get("DMSOrg2").isJsonNull()) {
					updatedObj.addProperty("Portfolio", asJsonObject.get("DMSOrg2").getAsString());
				} else {
					updatedObj.addProperty("Portfolio", "");
				}
				if (asJsonObject.has("DMSDivision") && !asJsonObject.get("DMSDivision").isJsonNull()) {
					updatedObj.addProperty("Operating Unit", asJsonObject.get("DMSDivision").getAsString());
				} else {
					updatedObj.addProperty("Operating Unit", "");
				}
				if (asJsonObject.has("DMSOrg3") && !asJsonObject.get("DMSOrg3").isJsonNull()) {
					updatedObj.addProperty("Business Unit", asJsonObject.get("DMSOrg3").getAsString());
				} else {
					updatedObj.addProperty("Business Unit", "");
				}

				if (asJsonObject.has("DMSOrg1") && !asJsonObject.get("DMSOrg1").isJsonNull()) {
					updatedObj.addProperty("Division", asJsonObject.get("DMSOrg1").getAsString());
				} else {
					updatedObj.addProperty("Division", "");
				}

				if (asJsonObject.has("StockOwnerID") && !asJsonObject.get("StockOwnerID").isJsonNull()) {
					updatedObj.addProperty("Distributor Code", asJsonObject.get("StockOwnerID").getAsString());
				} else {
					updatedObj.addProperty("Distributor Code", "");
				}

				if (asJsonObject.has("StockOwnerName") && !asJsonObject.get("StockOwnerName").isJsonNull()) {
					updatedObj.addProperty("Distributor Name", asJsonObject.get("StockOwnerName").getAsString());
				} else {
					updatedObj.addProperty("Distributor Name", "");
				}

				if (asJsonObject.has("MaterialNo") && !asJsonObject.get("MaterialNo").isJsonNull()) {
					updatedObj.addProperty("Marterial No", asJsonObject.get("MaterialNo").getAsString());
				} else {
					updatedObj.addProperty("Marterial No", "");
				}

				if (asJsonObject.has("MaterialDesc") && !asJsonObject.get("MaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Material Description", asJsonObject.get("MaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Material Description", "");
				}

				if (asJsonObject.has("ExternalMaterialDesc")
						&& !asJsonObject.get("ExternalMaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Source Material Description",
							asJsonObject.get("ExternalMaterialDesc").getAsString());
				} else {
					updatedObj.addProperty("Source Material Description", "");
				}

				if (asJsonObject.has("StorageLocation") && !asJsonObject.get("StorageLocation").isJsonNull()) {
					updatedObj.addProperty("Storage Location", asJsonObject.get("StorageLocation").getAsString());
				} else {
					updatedObj.addProperty("Storage Location", "");
				}

				if (asJsonObject.has("AsOnDate") && !asJsonObject.get("AsOnDate").isJsonNull()) {
					updatedObj.addProperty("As On Date", asJsonObject.get("AsOnDate").getAsString());
				} else {
					updatedObj.addProperty("As On Date", "");
				}

				if (asJsonObject.has("SerialNoHigh") && !asJsonObject.get("SerialNoHigh").isJsonNull()) {
					updatedObj.addProperty("Serial No", asJsonObject.get("SerialNoHigh").getAsString());
				} else {
					updatedObj.addProperty("Serial No", "");
				}
				if (asJsonObject.has("BatchNo") && !asJsonObject.get("BatchNo").isJsonNull()) {
					updatedObj.addProperty("Batch No", asJsonObject.get("BatchNo").getAsString());
				} else {
					updatedObj.addProperty("Batch No", "");
				}
				if (asJsonObject.has("ExpiryDate") && !asJsonObject.get("ExpiryDate").isJsonNull()) {
					updatedObj.addProperty("Expiry Date", asJsonObject.get("ExpiryDate").getAsString());
				} else {
					updatedObj.addProperty("Expiry Date", "");
				}

				if (asJsonObject.has("StockTypeDesc") && !asJsonObject.get("StockTypeDesc").isJsonNull()) {
					updatedObj.addProperty("Stock Type Description", asJsonObject.get("StockTypeDesc").getAsString());
				} else {
					updatedObj.addProperty("Stock Type Description", "");
				}

				if (asJsonObject.has("Quantity") && !asJsonObject.get("Quantity").isJsonNull()) {
					updatedObj.addProperty("Quantity", asJsonObject.get("Quantity").getAsString());
				} else {
					updatedObj.addProperty("Quantity", "");
				}

				if (asJsonObject.has("ExpiredQty") && !asJsonObject.get("ExpiredQty").isJsonNull()) {
					updatedObj.addProperty("Expired Qty", asJsonObject.get("ExpiredQty").getAsString());
				} else {
					updatedObj.addProperty("Expired Qty", "");
				}

				if (asJsonObject.has("AvailableQty") && !asJsonObject.get("AvailableQty").isJsonNull()) {
					updatedObj.addProperty("Available Qty", asJsonObject.get("AvailableQty").getAsString());
				} else {
					updatedObj.addProperty("Available Qty", "");
				}

				if (asJsonObject.has("UOM") && !asJsonObject.get("UOM").isJsonNull()) {
					updatedObj.addProperty("UOM", asJsonObject.get("UOM").getAsString());
				} else {
					updatedObj.addProperty("UOM", "");
				}

				if (asJsonObject.has("UnitPrice") && !asJsonObject.get("UnitPrice").isJsonNull()) {
					updatedObj.addProperty("Unit Price", asJsonObject.get("UnitPrice").getAsString());
				} else {
					updatedObj.addProperty("Unit Price", "");
				}
				if (asJsonObject.has("StockValue") && !asJsonObject.get("StockValue").isJsonNull()) {
					updatedObj.addProperty("Stock Value", asJsonObject.get("StockValue").getAsString());
				} else {
					updatedObj.addProperty("Stock Value", "");
				}
				updatedArray.add(updatedObj);

			}
			return updatedArray;
		} catch (Exception ex) {
			throw ex;

		}

	}

	private JsonArray getFilteredSubmissionReportRoc(JsonArray array) {
		JsonArray updatedArray = new JsonArray();
		try {
			for (int i = 0; i < array.size(); i++) {
				JsonObject asJsonObject = array.get(i).getAsJsonObject();
				JsonObject updatedObj = new JsonObject();
				if (asJsonObject.has("PartnerID") && !asJsonObject.get("PartnerID").isJsonNull()) {
					updatedObj.addProperty("Distributor Code", asJsonObject.get("PartnerID").getAsString());
				} else {
					updatedObj.addProperty("Distributor Code", "");
				}
				if (asJsonObject.has("CPName") && !asJsonObject.get("CPName").isJsonNull()) {
					updatedObj.addProperty("Distributor Name", asJsonObject.get("CPName").getAsString());
				} else {
					updatedObj.addProperty("Distributor Name", "");
				}

				if (asJsonObject.has("ERPSoftware") && !asJsonObject.get("ERPSoftware").isJsonNull()) {
					updatedObj.addProperty("ERP Software", asJsonObject.get("ERPSoftware").getAsString());
				} else {
					updatedObj.addProperty("ERP Software", "");
				}

				if (asJsonObject.has("DaysLastStockSync") && !asJsonObject.get("DaysLastStockSync").isJsonNull()) {
					updatedObj.addProperty("Days Last Stock Sync", asJsonObject.get("DaysLastStockSync").getAsString());
				} else {
					updatedObj.addProperty("Days Last Stock Sync", "");
				}
				if (asJsonObject.has("DaysLastSalesSync") && !asJsonObject.get("DaysLastSalesSync").isJsonNull()) {
					updatedObj.addProperty("Days Last Sales Sync", asJsonObject.get("DaysLastSalesSync").getAsString());
				} else {
					updatedObj.addProperty("Days Last Sales Sync", "");
				}
				if (asJsonObject.has("DaysLastGRSync") && !asJsonObject.get("DaysLastGRSync").isJsonNull()) {
					updatedObj.addProperty("Days Last GR Sync", asJsonObject.get("DaysLastGRSync").getAsString());
				} else {
					updatedObj.addProperty("Days Last GR Sync", "");
				}
				if (asJsonObject.has("LastInvDate") && !asJsonObject.get("LastInvDate").isJsonNull()) {
					updatedObj.addProperty("Latest Invoice Date", asJsonObject.get("LastInvDate").getAsString());
				} else {
					updatedObj.addProperty("Latest Invoice Date", "");
				}
				updatedArray.add(updatedObj);

			}
			return updatedArray;
		} catch (Exception ex) {
			throw ex;

		}

	}

	private double convertStrtoDouble(String doubleValue) {
		try {
			return Double.parseDouble(doubleValue);
		} catch (NumberFormatException ex) {
			return 0.0;

		} catch (Exception ex) {

			return 0.0;
		}
	}

	private int convertStrToInt(String intValue) {

		try {
			return Integer.parseInt(intValue);
		} catch (NumberFormatException ex) {
			return 0;

		} catch (Exception ex) {

			return 0;
		}

	}

	private void downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
		try {
			if (FILE_NAME != null) {
				File file=new File(FILE_NAME);
				Path path = Paths.get(FILE_NAME);
				byte[] data = Files.readAllBytes(path);
				response.setHeader("Content-disposition", "attachment;filename=" + "Sales & Stock Report.csv");
				response.setContentType("text/csv");
				response.setContentLength(data.length);
				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outputStream = null;
				try {
					outputStream = response.getOutputStream();
					outputStream.write(data, 0, data.length);
					outputStream.flush();
					outputStream.close();
					response.flushBuffer();
					if(file.exists())
						file.delete();
				} catch (RuntimeException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				JsonObject responseJson = new JsonObject();
				responseJson.addProperty("Message", "File Not Exist " + FILE_NAME);
				responseJson.addProperty("ErrorCode", "J002");
				responseJson.addProperty("Status", "000002");
				response.getWriter().println("File Not Exist " + FILE_NAME);
			}

		} catch (InvalidPathException ex) {
			throw ex;

		} catch (Exception ex) {
			throw ex;
		}

	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		CommonUtils commonUtils = new CommonUtils();
		String oDataUrl = "", password = "", userName = "", userPass = "";
		String executeUrl = "";
		String sumSecSales = "", secSales = "", userSyncSubmissionReport = "";// secSales
		boolean debug = false;
		Properties properties = new Properties();
		Workbook workbook = null;
		JsonObject responseObj = new JsonObject();
		Set<String> ccEmails = new HashSet<>();
		JsonObject createdSheetRes = new JsonObject();
		String cpSpStkItems = "", cpSpStkItemNos = "", logID = "", agrgtrID = "", pcgoDataUrl = "", pcgUserName = "",
				pcgPassword = "", pcgUserPass = "";
		ODataLogs oDataLogs = new ODataLogs();
		String salesSummary = "", invoices = "", stockSummary = "", stockDetails = "", dataSubmission = "";
		int stepNo = 0;
		String scpGUID = "";
		int maximValue = 0;
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			salesSummary = properties.getProperty("SalesSummary");
			invoices = properties.getProperty("Invoices");
			stockSummary = properties.getProperty("StockSummary");
			stockDetails = properties.getProperty("StockDetails");
			dataSubmission = properties.getProperty("DataSubmission");

			if (request.getParameter("debug") != null) {
				debug = true;
			}
			if (request.getParameter("SCPGUID") != null && !request.getParameter("SCPGUID").equalsIgnoreCase("")) {
				scpGUID = request.getParameter("SCPGUID");
			}
			if (scpGUID != null && !scpGUID.equalsIgnoreCase("")) {
				if (debug) {
					response.getWriter().println("Received SCPGUID " + scpGUID);
				}
				// https://hprd1c8c5b055e.ap1.hana.ondemand.com/AGGRMDT/ARTEC/SSGW/service.xsodata/V_SSCPSP_STKITMSNOS?$filter=SPGUID
				// eq 'DE4E1707-6502-4CCA-A00E-5721B4884FFA' and AggregatorID eq
				// 'AGGRMDT'&$skip=0&$top=2&$inlinecount=allpages
				/*
				 * pcgoDataUrl =
				 * commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
				 * pcgUserName =
				 * commonUtils.getODataDestinationProperties("User",
				 * "PCGWHANA"); pcgPassword =
				 * commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
				 */
				pcgoDataUrl = commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
				pcgUserName = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
				pcgPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
				pcgUserPass = pcgUserName + ":" + pcgPassword;
				agrgtrID = commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
				if (agrgtrID != null && !agrgtrID.equalsIgnoreCase("")) {
					executeUrl = pcgoDataUrl + "ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27DMSADM"
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
					if (debug) {
						response.getWriter().println("ConfigTypsetTypeValues Url:" + executeUrl);
					}
					JsonObject ccEmailObj = commonUtils.executeURL(executeUrl, pcgUserPass, response);
					if (debug) {
						response.getWriter()
								.println("Reterieved CC EmailIds from  ConfigTypsetTypeValues table:" + ccEmailObj);
					}
					JsonArray ccemailArray = ccEmailObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					if (ccemailArray != null && !ccemailArray.isJsonNull() && ccemailArray.size() > 0) {
						for (int i = 0; i < ccemailArray.size(); i++) {
							JsonObject ccEmailObjItem = ccemailArray.get(i).getAsJsonObject();
							if (!ccEmailObjItem.get("Types").isJsonNull()
									&& !ccEmailObjItem.get("Types").getAsString().equalsIgnoreCase("")) {
								String mailAddress = ccEmailObjItem.get("Types").getAsString();
								if (mailAddress.startsWith("WSRAUTML")) {
									if (!ccEmailObjItem.get("TypeValue").isJsonNull()
											&& !ccEmailObjItem.get("TypeValue").getAsString().equalsIgnoreCase("")) {
										ccEmails.add(ccEmailObjItem.get("TypeValue").getAsString());
									}
								}
							}
						}
					}
					if (debug) {
						response.getWriter().println("List of CC Emaild Address:" + ccEmails);
					}
					workbook = new XSSFWorkbook();
					if (debug) {
						response.getWriter().println("XSSFWorkbook created Successfully");
					}
					
					oDataUrl = commonUtils.getODataDestinationProperties("URL", "SSMISHANA");
					userName = commonUtils.getODataDestinationProperties("User","SSMISHANA");
					password = commonUtils.getODataDestinationProperties("Password", "SSMISHANA");
					userPass = userName + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl  Url:" + oDataUrl);
					}
					sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
					executeUrl = oDataUrl + sumSecSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + 0 + "&$top=" + top
							+ "&$inlinecount=allpages";
					if (debug) {
						response.getWriter().println("Sales Summary executeUrl:" + executeUrl);
					}
					JsonObject sumSecsalesObj = commonUtils.executeURL(executeUrl, userPass, response);
					if (debug) {
						response.getWriter().println("Sales Summary Records from Db:" + sumSecsalesObj);
					}
					if (sumSecsalesObj != null && sumSecsalesObj.has("d")
							&& !sumSecsalesObj.get("d").getAsJsonObject().isJsonNull()) {
						if (sumSecsalesObj.get("d").getAsJsonObject().has("__count")
								&& !sumSecsalesObj.get("d").getAsJsonObject().get("__count").isJsonNull()) {
							maximValue = sumSecsalesObj.get("d").getAsJsonObject().get("__count").getAsInt();
						}

					}
					if (debug) {
						response.getWriter().println("Total sales Summary Record:" + maximValue);
					}
					JsonArray sumSecSalesArray = null;
					if (maximValue == 0) {
						sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response,
								request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
					} else if (maximValue <= top) {
						sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull() && sumSecSalesArray.size() > 0) {
							JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
							if (debug) {
								response.getWriter()
										.println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
							}
							createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}
					} else {
						sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull() && sumSecSalesArray.size() > 0) {
							JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
							if (debug) {
								response.getWriter()
										.println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
							}
							createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
							sumSecsalesFilteredColumns = null;
						}
						int count = maximValue / top;
						int skipCount = 0;
						int topCount = 5000;
						for (int i = 1; i <= count; i++) {
							skipCount = topCount;
							topCount = (5000) * (i + 1);
							executeUrl = oDataUrl + sumSecSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
									+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + skipCount
									+ "&$top=" + topCount + "&$inlinecount=allpages";
							sumSecsalesObj = commonUtils.executeURL(executeUrl, userPass, response);
							sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull()
									&& sumSecSalesArray.size() > 0) {
								JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
								if (debug) {
									response.getWriter()
											.println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
								}
								createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary,
										debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
										oDataLogs,false);
							}
						}

					}
					if (debug) {
						response.getWriter().println("Sales Summary xl Sheet Created Response:" + createdSheetRes);
					}
					secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
					executeUrl = oDataUrl + secSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + 0 + "&$top=" + top
							+ "&$inlinecount=allpages";
					if (debug) {
						response.getWriter().println("Invoices execute Url:" + executeUrl);
					}

					JsonObject secSalesObj = commonUtils.executeURL(executeUrl, userPass, response);
					if (secSalesObj != null && secSalesObj.has("d")
							&& !secSalesObj.get("d").getAsJsonObject().isJsonNull()) {
						if (secSalesObj.get("d").getAsJsonObject().has("__count")
								&& !secSalesObj.get("d").getAsJsonObject().get("__count").isJsonNull()) {
							maximValue = secSalesObj.get("d").getAsJsonObject().get("__count").getAsInt();
						}

					}
					if (debug) {
						response.getWriter().println("Total Invoices Record:" + maximValue);
					}
					if (debug) {
						response.getWriter().println("Invoices Records from Db:" + secSalesObj);
					}
					JsonArray secSalesObjArray = null;
					if (maximValue == 0) {
						secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request,
								logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
					} else if (maximValue <= top) {
						secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						if (secSalesObjArray != null && !secSalesObjArray.isJsonNull() && secSalesObjArray.size() > 0) {
							JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(secSalesObjArray);
							if (debug) {
								response.getWriter().println("Invoices Filtered Records:" + filteredSecSalesRecords);
							}
							createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, invoices, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}

					} else {
						secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
						if (secSalesObjArray != null && !secSalesObjArray.isJsonNull() && secSalesObjArray.size() > 0) {
							JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(secSalesObjArray);
							if (debug) {
								response.getWriter().println("Invoices Filtered Records:" + filteredSecSalesRecords);
							}
							createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, invoices, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}

						int count = maximValue / top;
						int skipCount = 0;
						int top = 500;
						for (int i = 1; i <= count; i++) {
							skipCount = top;
							top = 5000 * (i + 1);
							executeUrl = oDataUrl + secSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
									+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + skipCount
									+ "&$top=" + top + "&$inlinecount=allpages";
							sumSecsalesObj = commonUtils.executeURL(executeUrl, userPass, response);
							sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull()
									&& sumSecSalesArray.size() > 0) {
								JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
								if (debug) {
									response.getWriter()
											.println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
								}
								createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary,
										debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
										oDataLogs,false);
							}
						}

					}

					if (debug) {
						response.getWriter().println("Invoices xl Sheet Created Response:" + createdSheetRes);
					}

					/*
					 * oDataUrl = commonUtils.getODataDestinationProperties("URL", "SSGWHANA"); 
					 * userName = commonUtils.getODataDestinationProperties("User",  "SSGWHANA"); 
					 * password = commonUtils.getODataDestinationProperties("Password",
					 * "SSGWHANA");
					 */
					oDataUrl = commonUtils.getODataDestinationProperties("URL", "SSGWHANA"); 
					userName = commonUtils.getODataDestinationProperties("User",  "SSGWHANA"); 
					password = commonUtils.getODataDestinationProperties("Password","SSGWHANA"); 
					userPass = userName + ":" + password;
					cpSpStkItems = properties.getProperty("V_SSCPSP_STKITMS");
					executeUrl = oDataUrl + cpSpStkItems + "?$filter=SPGUID%20eq%20%27" + scpGUID
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + 0 + "&$top=" + top
							+ "&$inlinecount=allpages";
					if (debug) {
						response.getWriter().println("Stock Summary execute Url:" + executeUrl);
					}
					JsonObject cpSpStockItemsObj = commonUtils.executeURL(executeUrl, userPass, response);
					if (cpSpStockItemsObj != null && cpSpStockItemsObj.has("d")
							&& !cpSpStockItemsObj.get("d").getAsJsonObject().isJsonNull()) {
						if (cpSpStockItemsObj.get("d").getAsJsonObject().has("__count")
								&& !cpSpStockItemsObj.get("d").getAsJsonObject().get("__count").isJsonNull()) {
							maximValue = cpSpStockItemsObj.get("d").getAsJsonObject().get("__count").getAsInt();
						}

					}
					if (debug) {
						response.getWriter().println("Total Stock Summary Record:" + maximValue);
					}
					if (debug) {
						response.getWriter().println("Stock Summary Records from Db:" + cpSpStockItemsObj);
					}
					JsonArray cpSpStockItemsObjArray = null;
					if (maximValue == 0) {
						cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
								request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
					} else if (maximValue <= top) {
						cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
								&& cpSpStockItemsObjArray.size() > 0) {
							JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(cpSpStockItemsObjArray);
							if (debug) {
								response.getWriter()
										.println("Stock Summary Filtered Records:" + filteredSecSalesRecords);
							}
							createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, stockSummary, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}

					} else {
						cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
								&& cpSpStockItemsObjArray.size() > 0) {
							JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(cpSpStockItemsObjArray);
							if (debug) {
								response.getWriter()
										.println("Stock Summary Filtered Records:" + filteredSecSalesRecords);
							}
							createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, stockSummary, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}
						int count = maximValue / top;
						int skipCount = 0;
						int top = 500;
						for (int i = 1; i <= count; i++) {
							skipCount = top;
							top = 5000 * (i + 1);
							executeUrl = oDataUrl + cpSpStkItems + "?$filter=SPGUID%20eq%20%27" + scpGUID
									+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + skipCount
									+ "&$top=" + top + "&$inlinecount=allpages";
							cpSpStockItemsObj = commonUtils.executeURL(executeUrl, userPass, response);
							cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
									&& cpSpStockItemsObjArray.size() > 0) {
								JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(
										cpSpStockItemsObjArray);
								if (debug) {
									response.getWriter()
											.println("Stock Summary Filtered Records:" + sumSecsalesFilteredColumns);
								}
								createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, stockSummary,
										debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
										oDataLogs,false);
							}
						}

					}
					if (debug) {
						response.getWriter().println("Stock Summary Sheet Created Response:" + createdSheetRes);
					}

					cpSpStkItemNos = properties.getProperty("V_SSCPSP_STKITMSNOS");
					executeUrl = oDataUrl + cpSpStkItemNos + "?$filter=SPGUID%20eq%20%27" + scpGUID
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + 0 + "&$top=" + top
							+ "&$inlinecount=allpages";
					if (debug) {
						response.getWriter().println("Stock Details execute Url:" + executeUrl);
					}
					JsonObject cpSpStkItemNosObj = commonUtils.executeURL(executeUrl, userPass, response);

					if (debug) {
						response.getWriter().println("Stock Details Records from Db:" + cpSpStkItemNosObj);
					}

					if (cpSpStkItemNosObj != null && cpSpStkItemNosObj.has("d")
							&& !cpSpStkItemNosObj.get("d").getAsJsonObject().isJsonNull()) {
						if (cpSpStkItemNosObj.get("d").getAsJsonObject().has("__count")
								&& !cpSpStkItemNosObj.get("d").getAsJsonObject().get("__count").isJsonNull()) {
							maximValue = cpSpStkItemNosObj.get("d").getAsJsonObject().get("__count").getAsInt();
						}

					}
					JsonArray cpSpStkItemNosObjArray = null;
					if (maximValue == 0) {
						cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
								request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
					} else if (maximValue <= top) {
						cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
								&& cpSpStkItemNosObjArray.size() > 0) {
							JsonArray filteredStkItemNosRecords = getFiletredSTKITMSNOSRecords(cpSpStkItemNosObjArray);
							if (debug) {
								response.getWriter()
										.println("Stock Details Filtered Records:" + filteredStkItemNosRecords);
								response.getWriter()
										.println("Total Stock Details Records:" + filteredStkItemNosRecords.size());
								response.getWriter().println("Total Stock Details Record SP_GUID:" + scpGUID);
							}
							createdSheetRes = createXlSheet(filteredStkItemNosRecords, workbook, stockDetails, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}
					} else {
						cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
								&& cpSpStkItemNosObjArray.size() > 0) {
							JsonArray filteredStkItemNosRecords = getFiletredSTKITMSNOSRecords(cpSpStkItemNosObjArray);
							if (debug) {
								response.getWriter()
										.println("Stock Details Filtered Records:" + filteredStkItemNosRecords);
								response.getWriter()
										.println("Total Stock Details Records:" + filteredStkItemNosRecords.size());
								response.getWriter().println("Total Stock Details Record SP_GUID:" + scpGUID);
							}
							createdSheetRes = createXlSheet(filteredStkItemNosRecords, workbook, stockDetails, debug,
									response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);
						}

						int count = maximValue / top;
						int skipCount = 0;
						int top = 500;
						for (int i = 1; i <= count; i++) {
							skipCount = top;
							top = 5000 * (i + 1);
							executeUrl = oDataUrl + cpSpStkItemNos + "?$filter=SPGUID%20eq%20%27" + scpGUID
									+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + skipCount
									+ "&$top=" + top + "&$inlinecount=allpages";
							cpSpStkItemNosObj = commonUtils.executeURL(executeUrl, userPass, response);
							cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
									.getAsJsonArray();
							if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
									&& cpSpStkItemNosObjArray.size() > 0) {
								JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(
										cpSpStkItemNosObjArray);
								if (debug) {
									response.getWriter()
											.println("Stock Details Filtered Records:" + sumSecsalesFilteredColumns);
								}
								createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, stockDetails,
										debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
										oDataLogs,false);
							}
						}
					}
					if (debug) {
						response.getWriter().println("Stock Details Sheet Created Response:" + createdSheetRes);
					}
					CommonUtils commonUtiles = new CommonUtils();
					oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSMISHANA");
					userName = commonUtiles.getODataDestinationProperties("User", "SSMISHANA");
					password = commonUtiles.getODataDestinationProperties("Password", "SSMISHANA");
					userPass = userName + ":" + password;
					userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
					executeUrl = oDataUrl + userSyncSubmissionReport + "?$filter=SPGUID%20eq%20%27" + scpGUID
							+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip=" + 0 + "&$top=" + top
							+ "&$inlinecount=allpages";
					if (debug) {
						response.getWriter().println("Data Submission execute Url:" + executeUrl);
					}
					JsonObject UserSyncSubmissionReportObj = commonUtils.executeURL(executeUrl, userPass, response);
					if (debug) {
						response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
					}

					JsonArray usrSynSubmissionReportObjArray = null;
					if (UserSyncSubmissionReportObj != null && UserSyncSubmissionReportObj.has("d")
							&& !UserSyncSubmissionReportObj.get("d").getAsJsonObject().isJsonNull()) {
						if (UserSyncSubmissionReportObj.get("d").getAsJsonObject().has("__count")
								&& !UserSyncSubmissionReportObj.get("d").getAsJsonObject().get("__count")
										.isJsonNull()) {
							maximValue = UserSyncSubmissionReportObj.get("d").getAsJsonObject().get("__count")
									.getAsInt();
						}

					}

					if (maximValue == 0) {
						usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
								.get("results").getAsJsonArray();
						createdSheetRes = createXlSheet(usrSynSubmissionReportObjArray, workbook, dataSubmission, debug,
								response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,true);

					} else if (maximValue <= top) {
						usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
								.get("results").getAsJsonArray();
						if (usrSynSubmissionReportObjArray != null && !usrSynSubmissionReportObjArray.isJsonNull()
								&& usrSynSubmissionReportObjArray.size() > 0) {
							JsonArray filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
									usrSynSubmissionReportObjArray);
							if (debug) {
								response.getWriter()
										.println("Data Submission Filtered Records:" + filteredSubmissionReportRoc);
							}
							createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, dataSubmission,
									debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
									oDataLogs,true);
						}
					} else {
						usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
								.get("results").getAsJsonArray();
						if (usrSynSubmissionReportObjArray != null && !usrSynSubmissionReportObjArray.isJsonNull()
								&& usrSynSubmissionReportObjArray.size() > 0) {
							JsonArray filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
									usrSynSubmissionReportObjArray);
							if (debug) {
								response.getWriter()
										.println("Data Submission Filtered Records:" + filteredSubmissionReportRoc);
							}
							createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, dataSubmission,
									debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
									oDataLogs,true);

							int count = maximValue / top;
							int skipCount = 0;
							int top = 500;
							for (int i = 1; i <= count; i++) {
								skipCount = top;
								top = 5000 * (i + 1);
								executeUrl = oDataUrl + userSyncSubmissionReport + "?$filter=SPGUID%20eq%20%27"
										+ scpGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27" + "&$skip="
										+ skipCount + "&$top=" + top + "&$inlinecount=allpages";
								UserSyncSubmissionReportObj = commonUtils.executeURL(executeUrl, userPass, response);
								usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
										.get("results").getAsJsonArray();
								if (usrSynSubmissionReportObjArray != null
										&& !usrSynSubmissionReportObjArray.isJsonNull()
										&& usrSynSubmissionReportObjArray.size() > 0) {
									 filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
											usrSynSubmissionReportObjArray);
									if (debug) {
										response.getWriter().println(
												"Data Submission Filtered Records:" + filteredSubmissionReportRoc);
									}
									createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, stockDetails,
											debug, response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID,
											oDataLogs,false);
								}
							}

						}
					}

					if (debug) {
						response.getWriter().println("Data Submission Sheet Created Response:" + createdSheetRes);
					}

					if (xlFileCreated) {
						downloadFile(request, response);
					}
					 else {
						responseObj.addProperty("Message", "xl file Not Created");
						responseObj.addProperty("Status", "000002");
						responseObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(responseObj);
						
					}
				} else {
					responseObj.addProperty("Message", "Input Payload does not contain SPGUID");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);

				}
			} else {
				responseObj.addProperty("Message", "Input Payload does not contain AggregatorID");
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);

			}
		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			/*
			 * stepNo++; oDataLogs.insertMessageForAppLogs(request, response,
			 * logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
			 * "Exception Occurred While Parsing Input Paylaod", pcgoDataUrl,
			 * pcgUserPass,
			 * agrgtrID,ex.getLocalizedMessage(),ex.getClass().getCanonicalName(
			 * ),"", debug);
			 */
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			/*
			 * stepNo++; oDataLogs.insertMessageForAppLogs(request, response,
			 * logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
			 * "Exception Occurred", pcgoDataUrl, pcgUserPass,
			 * agrgtrID,ex.getLocalizedMessage(),ex.getClass().getCanonicalName(
			 * ),"", debug);
			 */
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		}

		
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		CommonUtils commonUtiles = new CommonUtils();
		String inputPayload = "";
		JsonParser parser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		String oDataUrl = "", password = "", userName = "", userPass = "";
		String executeUrl = "";
		String sumSecSales = "", secSales = "", userSyncSubmissionReport = "";// secSales
		boolean debug = false;
		Properties properties = new Properties();
		Workbook workbook = null;
		JsonObject emailRes = new JsonObject();
		JsonObject responseObj = new JsonObject();
		Set<String> ccEmails = new HashSet<>();
		JsonObject createdSheetRes = new JsonObject();
		String cpSpStkItems = "", cpSpStkItemNos = "", logID = "", agrgtrID = "", pcgoDataUrl = "", pcgUserName = "",
				pcgPassword = "", pcgUserPass = "";
		ODataLogs oDataLogs = new ODataLogs();
		String salesSummary = "", invoices = "", stockSummary = "", stockDetails = "", dataSubmission = "";
		int stepNo = 0;
		String scpGUID = "";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			salesSummary = properties.getProperty("SalesSummary");
			invoices = properties.getProperty("Invoices");
			stockSummary = properties.getProperty("StockSummary");
			stockDetails = properties.getProperty("StockDetails");
			dataSubmission = properties.getProperty("DataSubmission");
			inputPayload = commonUtiles.getGetBody(request, response);
			if (request.getParameter("AggregatorID") != null
					&& !request.getParameter("AggregatorID").equalsIgnoreCase("")) {
				agrgtrID = request.getParameter("agrgtrID");
			}

			if (request.getParameter("SCPGUID") != null && !request.getParameter("SCPGUID").equalsIgnoreCase("")) {
				scpGUID = request.getParameter("SCPGUID");
			}
			if (request.getParameter("debug") != null && !request.getParameter("debug").equalsIgnoreCase("")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("Received Input Payload:" + jsonPayload);
			}

			pcgoDataUrl = commonUtiles.getODataDestinationProperties("URL", "PCGWHANA");
			pcgUserName = commonUtiles.getODataDestinationProperties("User", "PCGWHANA");
			pcgPassword = commonUtiles.getODataDestinationProperties("Password", "PCGWHANA");
			agrgtrID = commonUtiles.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			//pcgUserPass = pcgUserName + ":" + pcgPassword;
			if (agrgtrID != null && !agrgtrID.equalsIgnoreCase("") && scpGUID != null
					&& !scpGUID.equalsIgnoreCase("")) {
				String loginUser = commonUtiles.getUserPrincipal(request, "name", response);
				stepNo++;
				logID = oDataLogs.insertApplicationLogs(request, response, "Java", "SendSalesReport", "Process Started",
						"" + stepNo, request.getServletPath(), pcgoDataUrl, pcgUserPass, agrgtrID, loginUser, debug);
				if (debug) {
					response.getWriter().println("ApplicationLogs logID:" + logID);
				}

				executeUrl = pcgoDataUrl + "ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27DMSADM"
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("ConfigTypsetTypeValues Url:" + executeUrl);
				}
				JsonObject ccEmailObj = commonUtiles.executeURL(executeUrl, pcgUserPass, response);
				if (debug) {
					response.getWriter()
							.println("Reterieved CC EmailIds from  ConfigTypsetTypeValues table:" + ccEmailObj);
				}
				JsonArray ccemailArray = ccEmailObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				if (ccemailArray != null && !ccemailArray.isJsonNull() && ccemailArray.size() > 0) {
					for (int i = 0; i < ccemailArray.size(); i++) {
						JsonObject ccEmailObjItem = ccemailArray.get(i).getAsJsonObject();
						if (!ccEmailObjItem.get("Types").isJsonNull()
								&& !ccEmailObjItem.get("Types").getAsString().equalsIgnoreCase("")) {
							String mailAddress = ccEmailObjItem.get("Types").getAsString();
							if (mailAddress.startsWith("WSRAUTML")) {
								if (!ccEmailObjItem.get("TypeValue").isJsonNull()
										&& !ccEmailObjItem.get("TypeValue").getAsString().equalsIgnoreCase("")) {
									ccEmails.add(ccEmailObjItem.get("TypeValue").getAsString());
								}
							}
						}
					}
				}
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", ccEmails.toString(),
						stepNo, "CC Email Address", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

				if (debug) {
					response.getWriter().println("List of CC Emaild Address:" + ccEmails);
				}

				workbook = new XSSFWorkbook();
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSMISHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSMISHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSMISHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println("oDataUrl  Url:" + oDataUrl);
				}
				sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
				executeUrl = oDataUrl + sumSecSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Sales Summary executeUrl:" + executeUrl);
				}
				JsonObject sumSecsalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Sales Summary Records from Db:" + sumSecsalesObj);
				}
				JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						sumSecSalesArray.size() + "", stepNo, "Total Sales Summary Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (sumSecSalesArray != null && !sumSecSalesArray.isJsonNull() && sumSecSalesArray.size() > 0) {
					JsonArray sumSecsalesFilteredColumns = getSumSecsalesFilteredColumns(sumSecSalesArray);
					if (debug) {
						response.getWriter().println("Sales Summary Filtered Records:" + sumSecsalesFilteredColumns);
					}
					createdSheetRes = createXlSheet(sumSecsalesFilteredColumns, workbook, salesSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Sales Summary xl Sheet Created Response:" + createdSheetRes);
				}
				secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
				executeUrl = oDataUrl + secSales + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Invoices execute Url:" + executeUrl);
				}
				JsonObject secSalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Invoices Records from Db:" + secSalesObj);
				}
				JsonArray secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						secSalesObjArray.size() + "", stepNo, "Total Invoices Records for the SPGUID", pcgoDataUrl,
						pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (secSalesObjArray != null && !secSalesObjArray.isJsonNull() && secSalesObjArray.size() > 0) {
					JsonArray filteredSecSalesRecords = getFilteredSECSALESRecords(secSalesObjArray);
					if (debug) {
						response.getWriter().println("Invoices Filtered Records:" + filteredSecSalesRecords);
					}

					createdSheetRes = createXlSheet(filteredSecSalesRecords, workbook, invoices, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);

				}
				if (debug) {
					response.getWriter().println("Invoices xl Sheet Created Response:" + createdSheetRes);
				}

				userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
				executeUrl = oDataUrl + userSyncSubmissionReport + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Data Submission execute Url:" + executeUrl);
				}
				JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
				}
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSGWHANA");
				userPass = userName + ":" + password;
				cpSpStkItems = properties.getProperty("V_SSCPSP_STKITMS");
				executeUrl = oDataUrl + cpSpStkItems + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Summary execute Url:" + executeUrl);
				}
				JsonObject cpSpStockItemsObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if (debug) {
					response.getWriter().println("Stock Summary Records from Db:" + cpSpStockItemsObj);
				}
				JsonArray cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStockItemsObjArray.size() + "", stepNo, "Total Stock Summary Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStockItemsObjArray != null && !cpSpStockItemsObjArray.isJsonNull()
						&& cpSpStockItemsObjArray.size() > 0) {
					JsonArray filteredSTKITMSRecords = getFilteredSTKITMSRecords(cpSpStockItemsObjArray);
					if (debug) {
						response.getWriter().println("Stock Summary Filtered Records:" + filteredSTKITMSRecords);
					}

					createdSheetRes = createXlSheet(filteredSTKITMSRecords, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {
					createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}

				if (debug) {
					response.getWriter().println("Stock Summary Sheet Created Response:" + createdSheetRes);
				}
				cpSpStkItemNos = properties.getProperty("V_SSCPSP_STKITMSNOS");
				executeUrl = oDataUrl + cpSpStkItemNos + "?$filter=SPGUID%20eq%20%27" + scpGUID
						+ "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
				if (debug) {
					response.getWriter().println("Stock Details execute Url:" + executeUrl);
				}
				JsonObject cpSpStkItemNosObj = commonUtiles.executeURL(executeUrl, userPass, response);

				if (debug) {
					response.getWriter().println("Stock Details Records from Db:" + cpSpStkItemNosObj);
				}
				JsonArray cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						cpSpStkItemNosObjArray.size() + "", stepNo, "Total Stock Details Records for the SPGUID",
						pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "", "", debug);
				if (cpSpStkItemNosObjArray != null && !cpSpStkItemNosObjArray.isJsonNull()
						&& cpSpStkItemNosObjArray.size() > 0) {
					JsonArray filteredStkItemNosRecords = getFiletredSTKITMSNOSRecords(cpSpStkItemNosObjArray);
					if (debug) {
						response.getWriter().println("Stock Details Filtered Records:" + filteredStkItemNosRecords);
						response.getWriter().println("Total Stock Details Records:" + filteredStkItemNosRecords.size());
						response.getWriter().println("Total Stock Details Record SP_GUID:" + scpGUID);
					}
					createdSheetRes = createXlSheet(filteredStkItemNosRecords, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Stock Details Sheet Created Response:" + createdSheetRes);
				}
				JsonArray usrSynSubmissionReportObjArray = UserSyncSubmissionReportObj.get("d").getAsJsonObject()
						.get("results").getAsJsonArray();
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						usrSynSubmissionReportObjArray.size() + "", stepNo,
						"Total Data Submission Records for the SPGUID", pcgoDataUrl, pcgUserPass, agrgtrID, scpGUID, "",
						"", debug);
				if (usrSynSubmissionReportObjArray != null && !usrSynSubmissionReportObjArray.isJsonNull()
						&& usrSynSubmissionReportObjArray.size() > 0) {
					JsonArray filteredSubmissionReportRoc = getFilteredSubmissionReportRoc(
							usrSynSubmissionReportObjArray);
					if (debug) {
						response.getWriter().println("Data Submission Filtered Records:" + filteredSubmissionReportRoc);
					}

					createdSheetRes = createXlSheet(filteredSubmissionReportRoc, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				} else {

					createdSheetRes = createXlSheet(usrSynSubmissionReportObjArray, workbook, dataSubmission, debug,
							response, request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, true);
				}
				if (debug) {
					response.getWriter().println("Data Submission Sheet Created Response:" + createdSheetRes);
				}
				if (xlFileCreated) {
					downloadFile(request, response);
				}else{
					responseObj.addProperty("Message", "xl file Not Created");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);
				}
			} else {
				responseObj.addProperty("Message", "Input Payload does not contain AggregatorID");
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);

			}

		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}

			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);

			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred", pcgoDataUrl, pcgUserPass, agrgtrID, ex.getLocalizedMessage(),
					ex.getClass().getCanonicalName(), "", debug);
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		}
	
	
		
	}
	
	public JsonObject createXlSheet(JsonArray array, Workbook workbook, String sheetName, boolean debug,
			HttpServletResponse response, HttpServletRequest request, String logID, int stepNo, String oDataUrl,
			String userPass, String agrgtrID, ODataLogs oDataLogs,Map<String, String> metadata,List<String>headers) throws Exception {
		JsonObject resultjsonObj = new JsonObject();

		try {
			Sheet sheet1 = workbook.createSheet(sheetName);
			sheet1.setColumnWidth(0, 6000);
			sheet1.setColumnWidth(1, 4000);
			Row header = sheet1.createRow(0);
			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);
			AtomicInteger keyNum = new AtomicInteger(0);
			CellStyle style = workbook.createCellStyle();
			if (array != null && array.size() > 0 && !array.isJsonNull()) {
				style.setWrapText(true);
				if (sheetName.equalsIgnoreCase("Sales Summary")) {
					 Cell createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("Distributor Code");
					createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("Distributor Name");
					createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("Operating Unit");
					createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("ASP Gross Amount");
					createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("Quantity in Base UOM");
					int rowNum = 1;
					Double grandTotal = new Double(0.0);
					int totalQuantity=0;
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject salesSummary = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("SCPGuid") && !salesSummary.get("SCPGuid").isJsonNull()) {
							String scpGuidType = metadata.get("SCPGuid");
							if (scpGuidType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(salesSummary.get("SCPGuid").getAsString());
							else if (scpGuidType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(salesSummary.get("SCPGuid").getAsDouble());
							else if (scpGuidType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("SCPGuid").getAsString());
								cell.setCellValue(date);
							} else if (scpGuidType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(salesSummary.get("SCPGuid").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						if (salesSummary.has("SCPName1") && !salesSummary.get("SCPName1").isJsonNull()) {
							String dataType = metadata.get("SCPName1");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(salesSummary.get("SCPName1").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(salesSummary.get("SCPName1").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("SCPName1").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(salesSummary.get("SCPName1").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (salesSummary.has("DmsDivision_I") && !salesSummary.get("DmsDivision_I").isJsonNull()) {
							String dataType = metadata.get("DmsDivision_I");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(salesSummary.get("DmsDivision_I").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(salesSummary.get("DmsDivision_I").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("DmsDivision_I").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(salesSummary.get("DmsDivision_I").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (salesSummary.has("ASPGROSSAMT") && !salesSummary.get("ASPGROSSAMT").isJsonNull()) {
							String dataType = metadata.get("ASPGROSSAMT");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(salesSummary.get("ASPGROSSAMT").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
								cell.setCellValue(salesSummary.get("ASPGROSSAMT").getAsDouble());
								grandTotal = grandTotal + salesSummary.get("ASPGROSSAMT").getAsDouble();
							} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("ASPGROSSAMT").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(salesSummary.get("ASPGROSSAMT").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (salesSummary.has("QUANTITYINBASEUOM")
								&& !salesSummary.get("QUANTITYINBASEUOM").isJsonNull()) {
							String dataType = metadata.get("QUANTITYINBASEUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("QUANTITYINBASEUOM").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								totalQuantity=+salesSummary.get("QUANTITYINBASEUOM").getAsInt();
								cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						sheet1.autoSizeColumn(i);
					}
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(1);
					grossCell.setCellValue("Grand Total");
					Cell cellGrossAmt = total.createCell(3);
					cellGrossAmt.setCellValue(grandTotal);
					Cell totalQty = total.createCell(4);
					totalQty.setCellValue(totalQuantity);
				} else if (sheetName.equalsIgnoreCase("Invoices")) {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 1;
					
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject invoiceObj = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("DMSOrg2") && !invoiceObj.get("DMSOrg2").isJsonNull()) {
							String dataType = metadata.get("DMSOrg2");
						      cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DMSOrg2").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DMSOrg2").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DMSOrg2").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("DMSOrg2").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("DmsDivision_I") && !invoiceObj.get("DmsDivision_I").isJsonNull()) {
							String dataType = metadata.get("DmsDivision_I");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DmsDivision_I").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DmsDivision_I").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DmsDivision_I").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("DmsDivision_I").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("DMSOrg3") && !invoiceObj.get("DMSOrg3").isJsonNull()) {
							String dataType = metadata.get("DMSOrg3");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DMSOrg3").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DMSOrg3").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DMSOrg3").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("DMSOrg3").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("DMSOrg1") && !invoiceObj.get("DMSOrg1").isJsonNull()) {
							String dataType = metadata.get("DMSOrg1");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DMSOrg1").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DMSOrg1").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DMSOrg1").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("DMSOrg1").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("FromCPGuid") && !invoiceObj.get("FromCPGuid").isJsonNull()) {
							String dataType = metadata.get("FromCPGuid");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("FromCPGuid").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("FromCPGuid").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("FromCPGuid").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FromCPGuid").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("FromCPName") && !invoiceObj.get("FromCPName").isJsonNull()) {
							String dataType = metadata.get("FromCPName");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("FromCPName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("FromCPName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("FromCPName").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FromCPName").getAsInt());
							}
						}
						if (invoiceObj.has("CountryID") && !invoiceObj.get("CountryID").isJsonNull()) {
							String dataType = metadata.get("CountryID");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("CountryID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("CountryID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("CountryID").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("CountryID").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceTypeDesc") && !invoiceObj.get("InvoiceTypeDesc").isJsonNull()) {
							String dataType = metadata.get("InvoiceTypeDesc");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceTypeDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceTypeDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceTypeDesc").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceTypeDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceNo") && !invoiceObj.get("InvoiceNo").isJsonNull()) {
							String dataType = metadata.get("InvoiceNo");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceNo").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceDate") && !invoiceObj.get("InvoiceDate").isJsonNull()) {
							String dataType = metadata.get("InvoiceDate");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceDate").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceDate").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceDate").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("SoldToName") && !invoiceObj.get("SoldToName").isJsonNull()) {
							String dataType = metadata.get("SoldToName");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("SoldToName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("SoldToName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("SoldToName").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("SoldToName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("SoldToBPID") && !invoiceObj.get("SoldToBPID").isJsonNull()) {
							String dataType = metadata.get("SoldToBPID");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("SoldToBPID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("SoldToBPID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("SoldToBPID").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("SoldToBPID").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ExternalSoldToCPName")
								&& !invoiceObj.get("ExternalSoldToCPName").isJsonNull()) {
							String dataType = metadata.get("ExternalSoldToCPName");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ExternalSoldToCPName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ExternalSoldToCPName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ExternalSoldToCPName").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ExternalSoldToCPName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemNo") && !invoiceObj.get("ItemNo").isJsonNull()) {
							String dataType = metadata.get("ItemNo");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemNo").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("MaterialNo") && !invoiceObj.get("MaterialNo").isJsonNull()) {
							String dataType = metadata.get("MaterialNo");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("MaterialNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("MaterialNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("MaterialNo").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("MaterialNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("MaterialDesc") && !invoiceObj.get("MaterialDesc").isJsonNull()) {
							String dataType = metadata.get("MaterialDesc");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("MaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("MaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("MaterialDesc").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("MaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ExternalMaterialDesc")
								&& !invoiceObj.get("ExternalMaterialDesc").isJsonNull()) {
							String dataType = metadata.get("ExternalMaterialDesc");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ExternalMaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ExternalMaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ExternalMaterialDesc").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ExternalMaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("SerialNo") && !invoiceObj.get("SerialNo").isJsonNull()) {
							String dataType = metadata.get("SerialNo");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("SerialNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("SerialNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("SerialNo").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("SerialNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("Batch") && !invoiceObj.get("Batch").isJsonNull()) {
							String dataType = metadata.get("Batch");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("Batch").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("Batch").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("Batch").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("Batch").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceQty") && !invoiceObj.get("InvoiceQty").isJsonNull()) {
							String dataType = metadata.get("InvoiceQty");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceQty").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("UOM_I") && !invoiceObj.get("UOM_I").isJsonNull()) {
							String dataType = metadata.get("UOM_I");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("UOM_I").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("UOM_I").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("UOM_I").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("UOM_I").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemUnitPrice") && !invoiceObj.get("ItemUnitPrice").isJsonNull()) {
							String dataType = metadata.get("ItemUnitPrice");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemUnitPrice").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("GrossAmount") && !invoiceObj.get("GrossAmount").isJsonNull()) {
							String dataType = metadata.get("GrossAmount");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("GrossAmount").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							 cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("DiscountPerc") && !invoiceObj.get("DiscountPerc").isJsonNull()) {
							String dataType = metadata.get("DiscountPerc");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DiscountPerc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DiscountPerc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DiscountPerc").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("DiscountPerc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemTotalDiscAmount")
								&& !invoiceObj.get("ItemTotalDiscAmount").isJsonNull()) {
							String dataType = metadata.get("ItemTotalDiscAmount");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemTotalDiscAmount").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("AssessableValue") && !invoiceObj.get("AssessableValue").isJsonNull()) {
							String dataType = metadata.get("AssessableValue");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("AssessableValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("AssessableValue").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("AssessableValue").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("AssessableValue").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemTaxValue") && !invoiceObj.get("ItemTaxValue").isJsonNull()) {
							String dataType = metadata.get("ItemTaxValue");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemTaxValue").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemNetAmount") && !invoiceObj.get("ItemNetAmount").isJsonNull()) {
							String dataType = metadata.get("ItemNetAmount");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemNetAmount").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ItemNetAmountinRC") && !invoiceObj.get("ItemNetAmountinRC").isJsonNull()) {
							String dataType = metadata.get("ItemNetAmountinRC");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemNetAmountinRC").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ASPGrossAmount") && !invoiceObj.get("ASPGrossAmount").isJsonNull()) {
							String dataType = metadata.get("ASPGrossAmount");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ASPGrossAmount").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceQtyASPUOM") && !invoiceObj.get("InvoiceQtyASPUOM").isJsonNull()) {
							String dataType = metadata.get("InvoiceQtyASPUOM");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceQtyASPUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceQtyASPUOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceQtyASPUOM").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceQtyASPUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("ASPUOM") && !invoiceObj.get("ASPUOM").isJsonNull()) {
							String dataType = metadata.get("ASPUOM");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ASPUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ASPUOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ASPUOM").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ASPUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (invoiceObj.has("InvoiceStatusDesc") && !invoiceObj.get("InvoiceStatusDesc").isJsonNull()) {
							String dataType = metadata.get("InvoiceStatusDesc");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("InvoiceStatusDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("InvoiceStatusDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("InvoiceStatusDesc").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceStatusDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						sheet1.autoSizeColumn(i);
					}

				} else if (sheetName.equalsIgnoreCase("Data Submission")) {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 1;
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject dataSubmission = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("PartnerID") && !dataSubmission.get("PartnerID").isJsonNull()) {
							String dataType = metadata.get("PartnerID");
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("PartnerID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("PartnerID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("PartnerID").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("PartnerID").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						if (dataSubmission.has("CPName") && !dataSubmission.get("CPName").isJsonNull()) {
							String dataType = metadata.get("CPName");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("CPName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("CPName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("CPName").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("CPName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (dataSubmission.has("ERPSoftware") && !dataSubmission.get("ERPSoftware").isJsonNull()) {
							String dataType = metadata.get("ERPSoftware");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("ERPSoftware").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("ERPSoftware").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("ERPSoftware").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("ERPSoftware").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (dataSubmission.has("DaysLastStockSync")
								&& !dataSubmission.get("DaysLastStockSync").isJsonNull()) {
							String dataType = metadata.get("DaysLastStockSync");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("DaysLastStockSync").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("DaysLastStockSync").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("DaysLastStockSync").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("DaysLastStockSync").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (dataSubmission.has("DaysLastSalesSync")
								&& !dataSubmission.get("DaysLastSalesSync").isJsonNull()) {
							String dataType = metadata.get("DaysLastSalesSync");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("DaysLastSalesSync").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("DaysLastSalesSync").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("DaysLastSalesSync").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("DaysLastSalesSync").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (dataSubmission.has("DaysLastGRSync")
								&& !dataSubmission.get("DaysLastGRSync").isJsonNull()) {
							String dataType = metadata.get("DaysLastGRSync");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("DaysLastGRSync").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("DaysLastGRSync").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("DaysLastGRSync").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("DaysLastGRSync").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (dataSubmission.has("LastInvDate") && !dataSubmission.get("LastInvDate").isJsonNull()) {
							String dataType = metadata.get("LastInvDate");
							 cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(dataSubmission.get("LastInvDate").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(dataSubmission.get("LastInvDate").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(dataSubmission.get("LastInvDate").getAsString());
								cell.setCellValue(date);
							}
							else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("LastInvDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						sheet1.autoSizeColumn(i);
					}
				} else if (sheetName.equalsIgnoreCase("Stock Summary")) {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 1;
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject stockSummaryObj = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("DMSOrg2") && !stockSummaryObj.get("DMSOrg2").isJsonNull()) {
							String dataType = metadata.get("DMSOrg2");
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("DMSOrg2").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("DMSDivision") && !stockSummaryObj.get("DMSDivision").isJsonNull()) {
							String dataType = metadata.get("DMSDivision");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("DMSDivision").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("DMSDivision").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("DMSDivision").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSDivision").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("DMSOrg3") && !stockSummaryObj.get("DMSOrg3").isJsonNull()) {
							String dataType = metadata.get("DMSOrg3");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg3").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg3").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("DMSOrg3").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSOrg3").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("DMSOrg1") && !stockSummaryObj.get("DMSOrg1").isJsonNull()) {
							String dataType = metadata.get("DMSOrg1");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg1").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg1").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("DMSOrg1").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSOrg1").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("StockOwnerID") && !stockSummaryObj.get("StockOwnerID").isJsonNull()) {
							String dataType = metadata.get("StockOwnerID");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("StockOwnerID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("StockOwnerID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("StockOwnerID").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("StockOwnerID").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("StockOwnerName")
								&& !stockSummaryObj.get("StockOwnerName").isJsonNull()) {
							String dataType = metadata.get("StockOwnerName");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("StockOwnerName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("StockOwnerName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("StockOwnerName").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("StockOwnerName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("CountryID") && !stockSummaryObj.get("CountryID").isJsonNull()) {
							String dataType = metadata.get("CountryID");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("CountryID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("CountryID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("CountryID").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("CountryID").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("MaterialNo") && !stockSummaryObj.get("MaterialNo").isJsonNull()) {
							String dataType = metadata.get("MaterialNo");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("MaterialNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("MaterialNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("MaterialNo").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("MaterialNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("MaterialDesc") && !stockSummaryObj.get("MaterialDesc").isJsonNull()) {
							String dataType = metadata.get("MaterialDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("MaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("MaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("MaterialDesc").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("MaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("ExternalMaterialDesc")
								&& !stockSummaryObj.get("ExternalMaterialDesc").isJsonNull()) {
							String dataType = metadata.get("ExternalMaterialDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(
										stockSummaryObj.get("ExternalMaterialDesc").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("StorageLocation")
								&& !stockSummaryObj.get("StorageLocation").isJsonNull()) {
							String dataType = metadata.get("StorageLocation");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("StorageLocation").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("StorageLocation").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("StorageLocation").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("StorageLocation").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("AsOnDate") && !stockSummaryObj.get("AsOnDate").isJsonNull()) {
							String dataType = metadata.get("AsOnDate");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("AsOnDate").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("AsOnDate").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("AsOnDate").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("AsOnDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("UnrestrictedQty")
								&& !stockSummaryObj.get("UnrestrictedQty").isJsonNull()) {
							String dataType = metadata.get("UnrestrictedQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("UnrestrictedQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("UnrestrictedQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("UnrestrictedQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("UnrestrictedQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("BlockedQty") && !stockSummaryObj.get("BlockedQty").isJsonNull()) {
							String dataType = metadata.get("BlockedQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("BlockedQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("BlockedQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("BlockedQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("BlockedQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("ExpiredQty") && !stockSummaryObj.get("ExpiredQty").isJsonNull()) {
							String dataType = metadata.get("ExpiredQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("ExpiredQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("ExpiredQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("ExpiredQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("ExpiredQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("AvailableQty") && !stockSummaryObj.get("AvailableQty").isJsonNull()) {
							String dataType = metadata.get("AvailableQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("AvailableQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("AvailableQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("AvailableQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("AvailableQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("UOM") && !stockSummaryObj.get("UOM").isJsonNull()) {
							String dataType = metadata.get("UOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("UOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("UOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("UOM").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("UOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("AvailableQtyBaseUOM")
								&& !stockSummaryObj.get("AvailableQtyBaseUOM").isJsonNull()) {
							String dataType = metadata.get("AvailableQtyBaseUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("AvailableQtyBaseUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("AvailableQtyBaseUOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("AvailableQtyBaseUOM").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("AvailableQtyBaseUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("BaseUOM") && !stockSummaryObj.get("BaseUOM").isJsonNull()) {
							String dataType = metadata.get("BaseUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("BaseUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("BaseUOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("BaseUOM").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("BaseUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("StockValue") && !stockSummaryObj.get("StockValue").isJsonNull()) {
							String dataType = metadata.get("StockValue");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("StockValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("StockValue").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("StockValue").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("StockValue").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("StockValueInRC")
								&& !stockSummaryObj.get("StockValueInRC").isJsonNull()) {
							String dataType = metadata.get("StockValueInRC");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("StockValueInRC").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("StockValueInRC").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("StockValueInRC").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("StockValueInRC").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						sheet1.autoSizeColumn(i);
					}
				} else {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 1;
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject stockDetailsObj = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("DMSOrg2") && !stockDetailsObj.get("DMSOrg2").isJsonNull()) {
							String dataType = metadata.get("DMSOrg2");
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("DMSOrg2").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						if (stockDetailsObj.has("DMSDivision") && !stockDetailsObj.get("DMSDivision").isJsonNull()) {
							String dataType = metadata.get("DMSDivision");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("DMSDivision").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("DMSOrg3") && !stockDetailsObj.get("DMSOrg3").isJsonNull()) {
							String dataType = metadata.get("DMSOrg3");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg3").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg3").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("DMSOrg3").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("DMSOrg3").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("DMSOrg1") && !stockDetailsObj.get("DMSOrg1").isJsonNull()) {
							String dataType = metadata.get("DMSOrg1");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg1").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("DMSOrg1").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("DMSOrg1").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("DMSOrg1").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("StockOwnerID") && !stockDetailsObj.get("StockOwnerID").isJsonNull()) {
							String dataType = metadata.get("StockOwnerID");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("StockOwnerID").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("StockOwnerName")
								&& !stockDetailsObj.get("StockOwnerName").isJsonNull()) {
							String dataType = metadata.get("StockOwnerName");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("StockOwnerName").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("StockOwnerName").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("StockOwnerName").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("StockOwnerName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("MaterialNo") && !stockDetailsObj.get("MaterialNo").isJsonNull()) {
							String dataType = metadata.get("MaterialNo");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("MaterialNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("MaterialNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("MaterialNo").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("MaterialNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("MaterialDesc") && !stockDetailsObj.get("MaterialDesc").isJsonNull()) {
							String dataType = metadata.get("MaterialDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("MaterialDesc").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("ExternalMaterialDesc")
								&& !stockDetailsObj.get("ExternalMaterialDesc").isJsonNull()) {
							String dataType = metadata.get("ExternalMaterialDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(
										stockDetailsObj.get("ExternalMaterialDesc").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("StorageLocation")
								&& !stockDetailsObj.get("StorageLocation").isJsonNull()) {
							String dataType = metadata.get("StorageLocation");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("StorageLocation").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("StorageLocation").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("StorageLocation").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("StorageLocation").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("AsOnDate") && !stockDetailsObj.get("AsOnDate").isJsonNull()) {
							String dataType = metadata.get("AsOnDate");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("AsOnDate").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("AsOnDate").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("AsOnDate").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("AsOnDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("SerialNoHigh") && !stockDetailsObj.get("SerialNoHigh").isJsonNull()) {
							String dataType = metadata.get("SerialNoHigh");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("SerialNoHigh").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("BatchNo") && !stockDetailsObj.get("BatchNo").isJsonNull()) {
							String dataType = metadata.get("BatchNo");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("BatchNo").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("BatchNo").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("BatchNo").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("BatchNo").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("ExpiryDate") && !stockDetailsObj.get("ExpiryDate").isJsonNull()) {
							String dataType = metadata.get("ExpiryDate");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("ExpiryDate").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("ExpiryDate").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("ExpiryDate").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("ExpiryDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("StockTypeDesc")
								&& !stockDetailsObj.get("StockTypeDesc").isJsonNull()) {
							String dataType = metadata.get("StockTypeDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("StockTypeDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("StockTypeDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("StockTypeDesc").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("StockTypeDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("Quantity") && !stockDetailsObj.get("Quantity").isJsonNull()) {
							String dataType = metadata.get("Quantity");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("Quantity").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("Quantity").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("Quantity").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("Quantity").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("ExpiredQty") && !stockDetailsObj.get("ExpiredQty").isJsonNull()) {
							String dataType = metadata.get("ExpiredQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("ExpiredQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("ExpiredQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("ExpiredQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("ExpiredQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("AvailableQty") && !stockDetailsObj.get("AvailableQty").isJsonNull()) {
							String dataType = metadata.get("AvailableQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("AvailableQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("AvailableQty").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("AvailableQty").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("AvailableQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("UOM") && !stockDetailsObj.get("UOM").isJsonNull()) {
							String dataType = metadata.get("UOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("UOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("UOM").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("UOM").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("UOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("UnitPrice") && !stockDetailsObj.get("UnitPrice").isJsonNull()) {
							String dataType = metadata.get("UnitPrice");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("UnitPrice").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}

						if (stockDetailsObj.has("StockValue") && !stockDetailsObj.get("StockValue").isJsonNull()) {
							String dataType = metadata.get("StockValue");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockDetailsObj.get("StockValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockDetailsObj.get("StockValue").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockDetailsObj.get("StockValue").getAsString());
								cell.setCellValue(date);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockDetailsObj.get("StockValue").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						sheet1.autoSizeColumn(i);
					}

				}
		} 
			if (FILE_NAME == null) {
				getFilePath(request);
			}
			if (debug) {
				response.getWriter().println("File Created in this Path :" + FILE_NAME);
			}
			if (debug) {
				response.getWriter().println("File Created in the Path:" + FILE_NAME);
			}
			/*FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
			workbook.write(outputStream);
			// call the Send Email Method
			outputStream.flush();*/
			resultjsonObj.addProperty("Status", "000001");
			resultjsonObj.addProperty("ErrorCode", "");
			resultjsonObj.addProperty("Message", sheetName + "Created Successfully");
			return resultjsonObj;
		}	catch (IOException ex) {
			xlFileCreated = false;
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			if (debug)
				response.getWriter().println(buffer.toString());
			
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
			 
			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		} catch (Exception ex) {
			xlFileCreated = false;
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug)
				response.getWriter().println(buffer.toString());
			
			stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);
			 
			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		}
		return resultjsonObj;
	}
}
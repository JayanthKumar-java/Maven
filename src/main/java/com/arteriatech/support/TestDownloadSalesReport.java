package com.arteriatech.support;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.itextpdf.text.log.SysoCounter;

public class TestDownloadSalesReport extends HttpServlet{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		CommonUtils commonUtiles = new CommonUtils();
		JsonObject jsonPayload = new JsonObject();
		String oDataUrl = "", password = "", userName = "", userPass = "";
		String executeUrl = "";
		String sumSecSales = "", secSales = "", userSyncSubmissionReport = "";// secSales
		boolean debug = false;
		Properties properties = new Properties();
		SXSSFWorkbook workbook = null;
		JsonObject responseObj = new JsonObject();
		JsonObject createdSheetRes = new JsonObject();
		String cpSpStkItems = "", cpSpStkItemNos = "", logID = "", agrgtrID = "", pcgoDataUrl = "", pcgUserName = "",
				pcgPassword = "", pcgUserPass = "";
		ODataLogs oDataLogs = new ODataLogs();
		String salesSummary = "", invoices = "", stockSummary = "", stockDetails = "", dataSubmission = "";
		int stepNo = 0;
		String spGUID = "";
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			salesSummary = properties.getProperty("SalesSummary");
			invoices = properties.getProperty("Invoices");
			stockSummary = properties.getProperty("StockSummary");
			stockDetails = properties.getProperty("StockDetails");
			dataSubmission = properties.getProperty("DataSubmission");

			if (request.getParameter("SPGUID") != null && !request.getParameter("SPGUID").equalsIgnoreCase("")) {
				spGUID = request.getParameter("SPGUID");
			}
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println("SPGUID: " + spGUID);
			}

			pcgoDataUrl = commonUtiles.getODataDestinationProperties("URL", "PCGWHANA");
			pcgUserName = commonUtiles.getODataDestinationProperties("User", "PCGWHANA");
			pcgPassword = commonUtiles.getODataDestinationProperties("Password", "PCGWHANA");
			agrgtrID = commonUtiles.getODataDestinationProperties("AggregatorID", "PCGWHANA");
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			if (spGUID != null && !spGUID.equalsIgnoreCase("")) {
				/*String loginUser = commonUtiles.getUserPrincipal(request, "name", response);
				stepNo++;
				logID = oDataLogs.insertApplicationLogsDownLoadReport(request, response, "Java", "SendSalesReport", "Process Started",
						"" + stepNo, request.getServletPath(), pcgoDataUrl, pcgUserPass, agrgtrID, loginUser, debug);
				if (debug) {
					response.getWriter().println("ApplicationLogs logID:" + logID);
				}*/

//				  workbook = new XSSFWorkbook;
				   workbook = new SXSSFWorkbook(); 
				   workbook.setCompressTempFiles(true);
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSMISHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSMISHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSMISHANA");
				
				userPass = userName + ":" + password;
				sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
//                long startTime = System.currentTimeMillis();
				
				executeUrl = oDataUrl + sumSecSales
						+ "?$select=SCPGuid,SCPName1,DmsDivision_I,ASPGROSSAMT,QUANTITYINBASEUOM"
						+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
						+ "%27";

				/*if (debug) {
					response.getWriter().println("Sales Summary executeUrl:" + executeUrl);
				}*/
				if(debug){
					response.getWriter().println("Time before fetch is started for V_SSCPSP_T-7_SUM_SECSALES: "+new Date(System.currentTimeMillis()));
				}
				JsonObject sumSecsalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
			
				if(debug){
					response.getWriter().println("Time after fetch is completed for V_SSCPSP_T-7_SUM_SECSALES: "+new Date(System.currentTimeMillis()));
				}
				//String loginUser = commonUtiles.getUserPrincipal(request, "name", response);
				/*stepNo++;
				logID = oDataLogs.insertApplicationLogsDownLoadReport(request, response, "Java", "SendSalesReport", "Process Started",
						"" + stepNo, request.getServletPath(), pcgoDataUrl, pcgUserPass, agrgtrID, loginUser, debug);
				stepNo++;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", (endTime-startTime)+"Milli Seconds",
						stepNo, "", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);*/
						
				/*if (debug) {
					response.getWriter().println("Sales Summary Records from Db:" + sumSecsalesObj);
				}*/
				List<String> headers = new java.util.LinkedList<>();
				headers.add("Distributor Code");
				headers.add("Distributor Name");
				headers.add("Operating Unit");
				headers.add("ASP Gross Amount");
				headers.add("Quantity in Base UOM");
				JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
				if(debug){
					response.getWriter().println("Total records retrieved from V_SSCPSP_T-7_SUM_SECSALES Table:"+sumSecSalesArray.size());
				}
				if(debug){
					response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_T-7_SUM_SECSALES records:"+new Date(System.currentTimeMillis()));
				}
				
				createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request,
						logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,headers);
				if(debug){
					response.getWriter().println("After creating XL Sheet for the V_SSCPSP_T-7_SUM_SECSALES records:"+new Date(System.currentTimeMillis()));
				}
				
				/*if (debug) {
					response.getWriter().println("Sales Summary xl Sheet Created Response:" + createdSheetRes);
				}*/
				secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
				executeUrl = oDataUrl + secSales
						+ "?$select=DMSOrg2,DmsDivision_I,DMSOrg3,DMSOrg1,FromCPGuid,FromCPName,CountryID,InvoiceTypeDesc,InvoiceNo,InvoiceDate,SoldToName,SoldToBPID,ExternalSoldToCPName,ItemNo,MaterialNo,MaterialDesc,ExternalMaterialDesc,SerialNo,Batch,InvoiceQty,UOM_I,ItemUnitPrice,GrossAmount,DiscountPerc,ItemTotalDiscAmount,AssessableValue,ItemTaxValue,ItemNetAmount,ItemNetAmountinRC,ASPGrossAmount,InvoiceQtyASPUOM,ASPUOM,InvoiceStatusDesc"
						+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
						+ "%27";
				/*if (debug) {
					response.getWriter().println("Invoices execute Url:" + executeUrl);
				}*/
				if(debug){
					response.getWriter().println("Time before fetch is started for V_SSCPSP_T-7_SECSALES: "+new Date(System.currentTimeMillis()));
				}
				JsonObject secSalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if(debug){
					response.getWriter().println("Time after fetch is completed for V_SSCPSP_T-7_SECSALES: "+new Date(System.currentTimeMillis()));
				}
				/*if (debug) {
					response.getWriter().println("Invoices Records from Db:" + secSalesObj);
				}*/
				
				JsonArray secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
               if(debug){
            	   response.getWriter().println("Total records retrieved from V_SSCPSP_T-7_SECSALES Table: "+secSalesObjArray.size());
               }
               if(debug){
					response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_T-7_SECSALES records:"+new Date(System.currentTimeMillis()));
				}
               headers.clear();
				headers.add("Portfolio");
				headers.add("Operating Unit");
				headers.add("Business Unit");
				headers.add("Division");
				headers.add("Distributor Code");
				headers.add("Distributor Name");
				headers.add("Country");
				headers.add("Invoice Type Desc");
				headers.add("Invoice No");
				headers.add("Invoice Date");
				headers.add("Sold To Party Name");
				headers.add("Sold To Party Code");
				headers.add("Source Sold To Party Name");
				headers.add("Item No");
				headers.add("Material No");
				headers.add("Material Description");
				headers.add("Source Material Description");
				headers.add("SerialNo");
				headers.add("Batch");
				headers.add("Quantity");
				headers.add("UOM");
				headers.add("Unit Price");
				headers.add("Gross Amount");
				headers.add("Discount %");
				headers.add("Discount Amount");
				headers.add("Taxable Amount");
				headers.add("Tax Amount");
				headers.add("Net Amount");
				headers.add("Net Amount in USD");
				headers.add("ASP in USD");
				headers.add("Quantity in Base UOM");
				headers.add("ASP UOM");
				headers.add("Invoice Status");
				createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request, logID,
						stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,headers);
				if(debug){
					response.getWriter().println("After creating XL Sheet for the V_SSCPSP_T-7_SECSALES records:"+new Date(System.currentTimeMillis()));
				}

				/*if (debug) {
					response.getWriter().println("Invoices xl Sheet Created Response:" + createdSheetRes);
				}*/

				userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
				
				executeUrl = oDataUrl + userSyncSubmissionReport
						+ "?$select=PartnerID,CPName,ERPSoftware,DaysLastStockSync,DaysLastSalesSync,DaysLastGRSync,LastInvDate"
						+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
						+ "%27";
				/*if (debug) {
					response.getWriter().println("Data Submission execute Url:" + executeUrl);
				}*/
				if(debug){
					response.getWriter().println("Time before fetch is started for UserSyncSubmissionReport: "+new Date(System.currentTimeMillis()));
				}
				JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if(debug){
					response.getWriter().println("Time after fetch is completed for UserSyncSubmissionReport: "+new Date(System.currentTimeMillis()));
				}
				/*if (debug) {
					response.getWriter().println("Data Submission Records from Db:" + UserSyncSubmissionReportObj);
				}*/
				
				
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtiles.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtiles.getODataDestinationProperties("Password", "SSGWHANA");
				userPass = userName + ":" + password;
				cpSpStkItems = properties.getProperty("V_SSCPSP_STKITMS");

						executeUrl = oDataUrl + cpSpStkItems
						+ "?$select=DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,StockOwnerID,StockOwnerName,CountryID,MaterialNo,MaterialDesc,ExternalMaterialDesc,StorageLocation,AsOnDate,UnrestrictedQty,BlockedQty,ExpiredQty,AvailableQty,UOM,AvailableQtyBaseUOM,BaseUOM,StockValue,StockValueInRC"
						+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
						+ "%27";
						
				/*if (debug) {
					response.getWriter().println("Stock Summary execute Url:" + executeUrl);
				}*/
				if(debug){
					response.getWriter().println("Time before fetch is started for V_SSCPSP_STKITMS: "+new Date(System.currentTimeMillis()));
				}
				JsonObject cpSpStockItemsObj = commonUtiles.executeURL(executeUrl, userPass, response);
				if(debug){
					response.getWriter().println("Time after fetch is completed for V_SSCPSP_STKITMS: "+new Date(System.currentTimeMillis()));
				}
				/*if (debug) {
					response.getWriter().println("Stock Summary Records from Db:" + cpSpStockItemsObj);
				}*/
				
				JsonArray cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				if(debug){
					response.getWriter().println("Total records retrieved from V_SSCPSP_STKITMS Table:"+cpSpStockItemsObjArray.size());
				}
				 if(debug){
						response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_STKITMS records:"+new Date(System.currentTimeMillis()));
					}
				    headers.clear();
					headers.add("Portfolio");
					headers.add("Operating Unit");
					headers.add("Business Unit");
					headers.add("Division");
					headers.add("Distributor Code");
					headers.add("Distributor Name");
					headers.add("Country");
					headers.add("Material No");
					headers.add("Material Description");
					headers.add("Source Material Description");
					headers.add("Storage Location");
					headers.add("As On Date");
					headers.add("Quantity");
					headers.add("Blocked Qty");
					headers.add("Expired Qty");
					headers.add("Available Qty");
					headers.add("UOM");
					headers.add("Available Qty in Base UOM");
					headers.add("ASP UOM");
					headers.add("Stock Value");
					headers.add("Stock Value in USD");
					createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
					request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,headers);
					 if(debug){
							response.getWriter().println("After creating XL Sheet for the V_SSCPSP_STKITMS records:"+new Date(System.currentTimeMillis()));
						}

				/*if (debug) {
					response.getWriter().println("Stock Summary Sheet Created Response:" + createdSheetRes);
				}*/
				cpSpStkItemNos = properties.getProperty("V_SSCPSP_STKITMSNOS");
				executeUrl = oDataUrl + cpSpStkItemNos
						+ "?$select=DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,StockOwnerID,StockOwnerName,MaterialNo,MaterialDesc,ExternalMaterialDesc,StorageLocation,AsOnDate,SerialNoHigh,BatchNo,ExpiryDate,StockTypeDesc,Quantity,ExpiredQty,AvailableQty,UOM,UnitPrice,StockValue"
						+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
						+ "%27";
				/*if (debug) {
					response.getWriter().println("Stock Details execute Url:" + executeUrl);
				}*/
				
				if(debug){
					response.getWriter().println("Time before fetch is started for V_SSCPSP_STKITMSNOS: "+new Date(System.currentTimeMillis()));
				}
				JsonObject cpSpStkItemNosObj = commonUtiles.executeURL(executeUrl, userPass, response);
				
				if(debug){
					response.getWriter().println("Time after fetch is completed for V_SSCPSP_STKITMSNOS: "+new Date(System.currentTimeMillis()));
				}
				
				/*if (debug) {
					response.getWriter().println("Stock Details Records from Db:" + cpSpStkItemNosObj);
				}*/
				
				JsonArray cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				if(debug){
					response.getWriter().println("Total records retrieved from V_SSCPSP_STKITMSNOS Table:"+cpSpStkItemNosObjArray.size());
				}
				if(debug){
					response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_STKITMSNOS records:"+new Date(System.currentTimeMillis()));
				}
				headers.clear();
				headers.add("Portfolio");
				headers.add("Operating Unit");
				headers.add("Business Unit");
				headers.add("Division");
				headers.add("Distributor Code");
				headers.add("Distributor Name");
				headers.add("Marterial No");
				headers.add("Material Description");
				headers.add("Source Material Description");
				headers.add("Storage Location");
				headers.add("As On Date");
				headers.add("Serial No");
				headers.add("Batch No");
				headers.add("Expiry Date");
				headers.add("Stock Type Description");
				headers.add("Quantity");
				headers.add("Expired Qty");
				headers.add("Available Qty");
				headers.add("UOM");
				headers.add("Unit Price");
				headers.add("Stock Value");
				createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
						request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,headers);
				if(debug){
					response.getWriter().println("After creating XL Sheet for the V_SSCPSP_STKITMSNOS records:"+new Date(System.currentTimeMillis()));
				}
				/*if (debug) {
					response.getWriter().println("Stock Details Sheet Created Response:" + createdSheetRes);
				}*/
				JsonArray dataSubmissingArr = UserSyncSubmissionReportObj.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				
				if(debug){
					response.getWriter().println("Total records retrieved from UserSyncSubmissionReport Table:"+dataSubmissingArr.size());
				}
				if(debug){
					response.getWriter().println("Before creating XL Sheet for the UserSyncSubmissionReport records:"+new Date(System.currentTimeMillis()));
				}
				headers.clear();
				headers.add("Distributor Code");
				headers.add("Distributor Name");
				headers.add("ERP Software");
				headers.add("Days Last Stock Sync");
				headers.add("Days Last Sales Sync");
				headers.add("Days Last GR Sync");
				headers.add("Latest Invoice Date");
				createdSheetRes = createXlSheet(dataSubmissingArr, workbook, dataSubmission, debug, response, request,
						logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,headers);
				if(debug){
					response.getWriter().println("After creating XL Sheet for the UserSyncSubmissionReport records:"+new Date(System.currentTimeMillis()));
				}
				if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
					if(!debug){
					response.setContentType("application/vnd.ms-excel");
					response.setHeader("Content-Disposition", "attachment; filename=Sales & Stock Report.xls");
					workbook.write(response.getOutputStream());
					response.flushBuffer();
					}
				} else {
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

		} catch (JsonParseException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}

			/*stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);*/

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
		
	}

	public JsonObject createXlSheet(JsonArray array, SXSSFWorkbook workbook, String sheetName, boolean debug,
			HttpServletResponse response, HttpServletRequest request, String logID, int stepNo, String oDataUrl,
			String userPass, String agrgtrID, ODataLogs oDataLogs, List<String> headers)
			throws Exception {
		JsonObject resultjsonObj = new JsonObject();

		try {
			//Sheet sheet1 = workbook.createSheet(sheetName);
			SXSSFSheet sheet1 = (SXSSFSheet) workbook.createSheet(sheetName);
			sheet1.setRandomAccessWindowSize(1000);
			sheet1.setColumnWidth(0, 6000);
			sheet1.setColumnWidth(1, 4000);
			Row header = sheet1.createRow(0);
			/*XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);*/
			AtomicInteger keyNum = new AtomicInteger(0);
			CellStyle style = workbook.createCellStyle();
			if (array != null && array.size() > 0 && !array.isJsonNull()) {
				style.setWrapText(true);
				if (sheetName.equalsIgnoreCase("Sales Summary")) {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 1;
					for (int i = 0; i < array.size(); i++, rowNum++) {
						AtomicInteger cellNum = new AtomicInteger(0);
						JsonObject salesSummary = array.get(i).getAsJsonObject();
						Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("SCPGuid") && !salesSummary.get("SCPGuid").isJsonNull()) {
							cell.setCellValue(salesSummary.get("SCPGuid").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("SCPName1") && !salesSummary.get("SCPName1").isJsonNull()) {
							cell.setCellValue(salesSummary.get("SCPName1").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("DmsDivision_I") && !salesSummary.get("DmsDivision_I").isJsonNull()) {
							cell.setCellValue(salesSummary.get("DmsDivision_I").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("ASPGROSSAMT") && !salesSummary.get("ASPGROSSAMT").isJsonNull()) {
							cell.setCellValue(salesSummary.get("ASPGROSSAMT").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (salesSummary.has("QUANTITYINBASEUOM")
								&& !salesSummary.get("QUANTITYINBASEUOM").isJsonNull()) {
							cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsString());
						} else {
							cell.setCellValue("");
						}
					}
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
							cell.setCellValue(invoiceObj.get("DMSOrg2").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("DmsDivision_I") && !invoiceObj.get("DmsDivision_I").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("DmsDivision_I").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("DMSOrg3") && !invoiceObj.get("DMSOrg3").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("DMSOrg3").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("DMSOrg1") && !invoiceObj.get("DMSOrg1").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("DMSOrg1").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("FromCPGuid") && !invoiceObj.get("FromCPGuid").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("FromCPGuid").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("FromCPName") && !invoiceObj.get("FromCPName").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("FromCPName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("CountryID") && !invoiceObj.get("CountryID").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("CountryID").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceTypeDesc") && !invoiceObj.get("InvoiceTypeDesc").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceTypeDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceNo") && !invoiceObj.get("InvoiceNo").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceNo").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceDate") && !invoiceObj.get("InvoiceDate").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceDate").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("SoldToName") && !invoiceObj.get("SoldToName").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("SoldToName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("SoldToBPID") && !invoiceObj.get("SoldToBPID").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("SoldToBPID").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ExternalSoldToCPName")
								&& !invoiceObj.get("ExternalSoldToCPName").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ExternalSoldToCPName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemNo") && !invoiceObj.get("ItemNo").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("MaterialNo") && !invoiceObj.get("MaterialNo").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("MaterialNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("MaterialDesc") && !invoiceObj.get("MaterialDesc").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("MaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ExternalMaterialDesc")
								&& !invoiceObj.get("ExternalMaterialDesc").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ExternalMaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("SerialNo") && !invoiceObj.get("SerialNo").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("SerialNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("Batch") && !invoiceObj.get("Batch").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("Batch").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceQty") && !invoiceObj.get("InvoiceQty").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("UOM_I") && !invoiceObj.get("UOM_I").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("UOM_I").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemUnitPrice") && !invoiceObj.get("ItemUnitPrice").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("GrossAmount") && !invoiceObj.get("GrossAmount").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("GrossAmount").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("DiscountPerc") && !invoiceObj.get("DiscountPerc").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("DiscountPerc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemTotalDiscAmount")
								&& !invoiceObj.get("ItemTotalDiscAmount").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("AssessableValue") && !invoiceObj.get("AssessableValue").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("AssessableValue").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemTaxValue") && !invoiceObj.get("ItemTaxValue").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemNetAmount") && !invoiceObj.get("ItemNetAmount").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ItemNetAmountinRC") && !invoiceObj.get("ItemNetAmountinRC").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ASPGrossAmount") && !invoiceObj.get("ASPGrossAmount").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceQtyASPUOM") && !invoiceObj.get("InvoiceQtyASPUOM").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceQtyASPUOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("ASPUOM") && !invoiceObj.get("ASPUOM").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("ASPUOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (invoiceObj.has("InvoiceStatusDesc") && !invoiceObj.get("InvoiceStatusDesc").isJsonNull()) {
							cell.setCellValue(invoiceObj.get("InvoiceStatusDesc").getAsString());
						} else {
							cell.setCellValue("");
						}
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
							cell.setCellValue(dataSubmission.get("PartnerID").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("CPName") && !dataSubmission.get("CPName").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("CPName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("ERPSoftware") && !dataSubmission.get("ERPSoftware").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("ERPSoftware").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("DaysLastStockSync")
								&& !dataSubmission.get("DaysLastStockSync").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("DaysLastStockSync").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("DaysLastSalesSync")
								&& !dataSubmission.get("DaysLastSalesSync").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("DaysLastSalesSync").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("DaysLastGRSync")
								&& !dataSubmission.get("DaysLastGRSync").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("DaysLastGRSync").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (dataSubmission.has("LastInvDate") && !dataSubmission.get("LastInvDate").isJsonNull()) {
							cell.setCellValue(dataSubmission.get("LastInvDate").getAsString());
						} else {
							cell.setCellValue("");
						}
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
							cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("DMSDivision") && !stockSummaryObj.get("DMSDivision").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("DMSDivision").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("DMSOrg3") && !stockSummaryObj.get("DMSOrg3").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("DMSOrg3").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StockOwnerID") && !stockSummaryObj.get("StockOwnerID").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StockOwnerID").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StockOwnerName")
								&& !stockSummaryObj.get("StockOwnerName").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StockOwnerName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("CountryID") && !stockSummaryObj.get("CountryID").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("CountryID").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("MaterialNo") && !stockSummaryObj.get("MaterialNo").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("MaterialNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("MaterialDesc") && !stockSummaryObj.get("MaterialDesc").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("MaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("ExternalMaterialDesc")
								&& !stockSummaryObj.get("ExternalMaterialDesc").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StorageLocation")
								&& !stockSummaryObj.get("StorageLocation").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StorageLocation").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("AsOnDate") && !stockSummaryObj.get("AsOnDate").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("AsOnDate").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("UnrestrictedQty")
								&& !stockSummaryObj.get("UnrestrictedQty").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("UnrestrictedQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("BlockedQty") && !stockSummaryObj.get("BlockedQty").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("BlockedQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("ExpiredQty") && !stockSummaryObj.get("ExpiredQty").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("ExpiredQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("AvailableQty") && !stockSummaryObj.get("AvailableQty").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("AvailableQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("UOM") && !stockSummaryObj.get("UOM").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("UOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("AvailableQtyBaseUOM")
								&& !stockSummaryObj.get("AvailableQtyBaseUOM").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("AvailableQtyBaseUOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("BaseUOM") && !stockSummaryObj.get("BaseUOM").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("BaseUOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StockValue") && !stockSummaryObj.get("StockValue").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StockValue").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StockValueInRC")
								&& !stockSummaryObj.get("StockValueInRC").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StockValueInRC").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("StockValueInRC")
								&& !stockSummaryObj.get("StockValueInRC").isJsonNull()) {
							cell.setCellValue(stockSummaryObj.get("StockValueInRC").getAsString());
						} else {
							cell.setCellValue("");
						}

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
							cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("DMSDivision") && !stockDetailsObj.get("DMSDivision").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("DMSOrg3") && !stockDetailsObj.get("DMSOrg3").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("DMSOrg3").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("DMSOrg1") && !stockDetailsObj.get("DMSOrg1").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("DMSOrg1").getAsString());
						} else {
							cell.setCellValue("");
						}
						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("StockOwnerID") && !stockDetailsObj.get("StockOwnerID").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("StockOwnerName")
								&& !stockDetailsObj.get("StockOwnerName").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("StockOwnerName").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("MaterialNo") && !stockDetailsObj.get("MaterialNo").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("MaterialNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("MaterialDesc") && !stockDetailsObj.get("MaterialDesc").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("ExternalMaterialDesc")
								&& !stockDetailsObj.get("ExternalMaterialDesc").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("StorageLocation")
								&& !stockDetailsObj.get("StorageLocation").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("StorageLocation").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("AsOnDate") && !stockDetailsObj.get("AsOnDate").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("AsOnDate").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("SerialNoHigh") && !stockDetailsObj.get("SerialNoHigh").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("BatchNo") && !stockDetailsObj.get("BatchNo").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("BatchNo").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("ExpiryDate") && !stockDetailsObj.get("ExpiryDate").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("ExpiryDate").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("StockTypeDesc")
								&& !stockDetailsObj.get("StockTypeDesc").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("StockTypeDesc").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("Quantity") && !stockDetailsObj.get("Quantity").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("Quantity").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("ExpiredQty") && !stockDetailsObj.get("ExpiredQty").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("ExpiredQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("AvailableQty") && !stockDetailsObj.get("AvailableQty").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("AvailableQty").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("UOM") && !stockDetailsObj.get("UOM").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("UOM").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("UnitPrice") && !stockDetailsObj.get("UnitPrice").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsString());
						} else {
							cell.setCellValue("");
						}

						cell = row.createCell(cellNum.getAndIncrement());
						if (stockDetailsObj.has("StockValue") && !stockDetailsObj.get("StockValue").isJsonNull()) {
							cell.setCellValue(stockDetailsObj.get("StockValue").getAsString());
						} else {
							cell.setCellValue("");
						}

					}
				}
			}
			
			resultjsonObj.addProperty("Status", "000001");
			resultjsonObj.addProperty("ErrorCode", "");
			resultjsonObj.addProperty("Message", sheetName + "Created Successfully");
			return resultjsonObj;
		} catch (JsonParseException ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			if (debug)
				response.getWriter().println(buffer.toString());

			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug)
				response.getWriter().println(buffer.toString());

			stepNo++;
			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", ex.getLocalizedMessage());
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		}
		return resultjsonObj;

	}

	
	

	
	

	
	
	

	
	

	

	


	

}

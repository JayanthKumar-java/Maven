package com.arteriatech.support;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
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
import org.apache.poi.hpsf.Decimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import com.graphbuilder.struc.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DownloadSalesReport extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String FILE_NAME = null;
	private boolean xlFileCreated = true;
	private File file = null;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}
	
	public JsonObject createXlSheet(JsonArray array, SXSSFWorkbook workbook, String sheetName, boolean debug,
			HttpServletResponse response, HttpServletRequest request, String logID, int stepNo, String oDataUrl,
			String userPass, String agrgtrID, ODataLogs oDataLogs, Map<String, String> metadata, List<String> headers,boolean showFileds)
			throws Exception {
		JsonObject resultjsonObj = new JsonObject();

		try {
			SXSSFSheet sheet1 = (SXSSFSheet) workbook.createSheet(sheetName);
			sheet1.setRandomAccessWindowSize(100);
			sheet1.setColumnWidth(0, 6000);
			sheet1.setColumnWidth(1, 4000);
			Row header = sheet1.createRow(0);
			/*XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 16);
			font.setBold(true);*/
			AtomicInteger keyNum = new AtomicInteger(0);
			CellStyle style = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
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
					createCell.setCellValue("Gross Value");
					createCell = header.createCell(keyNum.getAndIncrement());
					createCell.setCellValue("Quantity in ASP UOM");
					int rowNum = 1;
					Double grandTotal = new Double(0.0);
					Double totalQuantity = new Double(0.0);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								Date date = convertLongToDate(salesSummary.get("SCPGuid").getAsString());
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
							else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
								totalQuantity += salesSummary.get("QUANTITYINBASEUOM").getAsDouble();
								cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsDouble());
							} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(salesSummary.get("QUANTITYINBASEUOM").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(salesSummary.get("QUANTITYINBASEUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						// sheet1.autoSizeColumn(i);
					}
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell cellGrossAmt = total.createCell(3);
					cellGrossAmt.setCellValue(grandTotal);
					Cell totalQty = total.createCell(4);
					totalQty.setCellValue(totalQuantity);
				} else if (sheetName.equalsIgnoreCase("Invoices")) {
					Double totalGrossAmt=new Double(0);
					Double totalDisccountAmt=new Double(0);
					Double totalTaxableAmt=new Double(0);
					Double totalTaxAmt=new Double(0);
					Double totalNetAmt=new Double(0);
					Double totalNetAmtInUsd=new Double(0);
					Double totalGrossValue=new Double(0);
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
						if (invoiceObj.has("ProductCategoryDesc") && !invoiceObj.get("ProductCategoryDesc").isJsonNull()) {
							String dataType = metadata.get("ProductCategoryDesc");
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ProductCategoryDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ProductCategoryDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ProductCategoryDesc").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ProductCategoryDesc").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						
						if (invoiceObj.has("ProductGroupDesc") && !invoiceObj.get("ProductGroupDesc").isJsonNull()) {
							String dataType = metadata.get("ProductGroupDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ProductGroupDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("ProductGroupDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ProductGroupDesc").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ProductGroupDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (invoiceObj.has("DMSOrg2") && !invoiceObj.get("DMSOrg2").isJsonNull()) {
							String dataType = metadata.get("DMSOrg2");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("DMSOrg2").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("DMSOrg2").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("DMSOrg2").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FromCPName").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (invoiceObj.has("FinancialWeekCode") && !invoiceObj.get("FinancialWeekCode").isJsonNull()) {
							String dataType = metadata.get("FinancialWeekCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("FinancialWeekCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("FinancialWeekCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("FinancialWeekCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FinancialWeekCode").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (invoiceObj.has("FinancialQuarterCode") && !invoiceObj.get("FinancialQuarterCode").isJsonNull()) {
							String dataType = metadata.get("FinancialQuarterCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("FinancialQuarterCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("FinancialQuarterCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("FinancialQuarterCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FinancialQuarterCode").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (invoiceObj.has("FinancialYearCode") && !invoiceObj.get("FinancialYearCode").isJsonNull()) {
							String dataType = metadata.get("FinancialYearCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("FinancialYearCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("FinancialYearCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("FinancialYearCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("FinancialYearCode").getAsInt());
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ExternalMaterialDesc").getAsInt());
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceQty").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						//21
						if (invoiceObj.has("UOM_I") && !invoiceObj.get("UOM_I").isJsonNull()) {
							String dataType = metadata.get("UOM_I");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("UOM_I").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(invoiceObj.get("UOM_I").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("UOM_I").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("UOM_I").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						//22
						if(showFileds){
						if (invoiceObj.has("ItemUnitPrice") && !invoiceObj.get("ItemUnitPrice").isJsonNull()) {
							String dataType = metadata.get("ItemUnitPrice");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsDouble());
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemUnitPrice").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
					}
						//23
						if(showFileds){
						if (invoiceObj.has("GrossAmount") && !invoiceObj.get("GrossAmount").isJsonNull()) {
							String dataType = metadata.get("GrossAmount");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsDouble());
								totalGrossAmt+=invoiceObj.get("GrossAmount").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("GrossAmount").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("GrossAmount").getAsInt());
								totalGrossAmt+=invoiceObj.get("GrossAmount").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
					}	
						//24
						if (showFileds) {
							if (invoiceObj.has("DiscountPerc") && !invoiceObj.get("DiscountPerc").isJsonNull()) {
								String dataType = metadata.get("DiscountPerc");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(invoiceObj.get("DiscountPerc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(invoiceObj.get("DiscountPerc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(invoiceObj.get("DiscountPerc").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(invoiceObj.get("DiscountPerc").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}
						//25
						if(showFileds){
						if (invoiceObj.has("ItemTotalDiscAmount")
								&& !invoiceObj.get("ItemTotalDiscAmount").isJsonNull()) {
							String dataType = metadata.get("ItemTotalDiscAmount");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsDouble());
								totalDisccountAmt+=invoiceObj.get("ItemTotalDiscAmount").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemTotalDiscAmount").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsInt());
								totalDisccountAmt+=invoiceObj.get("ItemTotalDiscAmount").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
					}	
						//26
						if (showFileds) {
							if (invoiceObj.has("AssessableValue") && !invoiceObj.get("AssessableValue").isJsonNull()) {
								String dataType = metadata.get("AssessableValue");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(invoiceObj.get("AssessableValue").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(invoiceObj.get("AssessableValue").getAsDouble());
									totalTaxableAmt += invoiceObj.get("AssessableValue").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(invoiceObj.get("AssessableValue").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(invoiceObj.get("AssessableValue").getAsInt());
									totalTaxableAmt += invoiceObj.get("AssessableValue").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}	
						//27
						if(showFileds){
						if (invoiceObj.has("ItemTaxValue") && !invoiceObj.get("ItemTaxValue").isJsonNull()) {
							String dataType = metadata.get("ItemTaxValue");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsDouble());
								totalTaxAmt+=invoiceObj.get("ItemTaxValue").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ItemTaxValue").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsInt());
								totalTaxAmt+=invoiceObj.get("ItemTaxValue").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						 }
						}
						//28
						if (showFileds) {
							if (invoiceObj.has("ItemNetAmount") && !invoiceObj.get("ItemNetAmount").isJsonNull()) {
								String dataType = metadata.get("ItemNetAmount");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsDouble());
									totalNetAmt += invoiceObj.get("ItemNetAmount").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(invoiceObj.get("ItemNetAmount").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(invoiceObj.get("ItemNetAmount").getAsInt());
									totalNetAmt += invoiceObj.get("ItemNetAmount").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}
						//29
						if (showFileds) {
							if (invoiceObj.has("ItemNetAmountinRC")
									&& !invoiceObj.get("ItemNetAmountinRC").isJsonNull()) {
								String dataType = metadata.get("ItemNetAmountinRC");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsDouble());
									totalNetAmtInUsd += invoiceObj.get("ItemNetAmountinRC").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(invoiceObj.get("ItemNetAmountinRC").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(invoiceObj.get("ItemNetAmountinRC").getAsInt());
									totalNetAmtInUsd += invoiceObj.get("ItemNetAmountinRC").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}
						//30
						if (invoiceObj.has("ASPGrossAmount") && !invoiceObj.get("ASPGrossAmount").isJsonNull()) {
							String dataType = metadata.get("ASPGrossAmount");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsDouble());
								totalGrossValue+=invoiceObj.get("ASPGrossAmount").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(invoiceObj.get("ASPGrossAmount").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsInt());
								totalGrossValue+=invoiceObj.get("ASPGrossAmount").getAsInt();
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(invoiceObj.get("InvoiceStatusDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						// sheet1.autoSizeColumn(i);
						
						
					}
					
					// Printing gross amount
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell totalGrossValueCell=null;
					if (showFileds) {
						Cell toatlGrossamt = total.createCell(26);
						toatlGrossamt.setCellValue(totalGrossAmt);
						Cell toatlDiscountCell = total.createCell(28);
						toatlDiscountCell.setCellValue(totalDisccountAmt);
						Cell totalTaxableAmtCell = total.createCell(29);
						totalTaxableAmtCell.setCellValue(totalTaxableAmt);
						Cell totalTaxAmtCell = total.createCell(30);
						totalTaxAmtCell.setCellValue(totalTaxAmt);

						Cell totalNetAmtCell = total.createCell(31);
						totalNetAmtCell.setCellValue(totalNetAmt);

						Cell totalNetAmtInUsdCell = total.createCell(32);
						totalNetAmtInUsdCell.setCellValue(totalNetAmtInUsd);
						totalGrossValueCell=total.createCell(33);
					}else{
						totalGrossValueCell=total.createCell(25);
					}
					totalGrossValueCell.setCellValue(totalGrossValue);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(dataSubmission.get("LastInvDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						// sheet1.autoSizeColumn(i);
					}
				} else if (sheetName.equalsIgnoreCase("Stock Summary")) {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 0;
					Double totalQty=new Double(0);
					Double totalBlockedQty=new Double(0);
					Double totalExprQty=new Double(0);
					Double totalAvailableQty=new Double(0);
					Double totalAvlqtyInbaseUm=new Double(0);
					Double totalStackValue=new Double(0);
					Double totalStackValueInUsd=new Double(0);
					Double totalAspValue=new Double(0);
					Double totalConsignmentQtyValue=new Double(0);
					for (int i = 0; i < array.size(); i++) {
						JsonObject stockSummaryObj = array.get(i).getAsJsonObject();
						if(stockSummaryObj.has("MaterialNo") &&!stockSummaryObj.get("MaterialNo").isJsonNull()&& !stockSummaryObj.get("MaterialNo").getAsString().equalsIgnoreCase("Non_MDT")&&!stockSummaryObj.get("MaterialNo").getAsString().equalsIgnoreCase("Discontinued")){
							rowNum++;
							AtomicInteger cellNum = new AtomicInteger(0);
							Row row = sheet1.createRow(rowNum);
						Cell cell = row.createCell(cellNum.getAndIncrement());
						if (stockSummaryObj.has("ProductCategoryDesc") && !stockSummaryObj.get("ProductCategoryDesc").isJsonNull()) {
							String dataType = metadata.get("ProductCategoryDesc");
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("ProductCategoryDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("ProductCategoryDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("ProductCategoryDesc").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("ProductCategoryDesc").getAsInt());
							}
						} else {
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("ProductGroupDesc") && !stockSummaryObj.get("ProductGroupDesc").isJsonNull()) {
							String dataType = metadata.get("ProductGroupDesc");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("ProductGroupDesc").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("ProductGroupDesc").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("ProductGroupDesc").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("ProductGroupDesc").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("DMSOrg2") && !stockSummaryObj.get("DMSOrg2").isJsonNull()) {
							String dataType = metadata.get("DMSOrg2");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("DMSOrg2").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSOrg2").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("DMSOrg1").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("FinancialWeekCode") && !stockSummaryObj.get("FinancialWeekCode").isJsonNull()) {
							String dataType = metadata.get("FinancialWeekCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("FinancialWeekCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("FinancialWeekCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("FinancialWeekCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("FinancialWeekCode").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("FinancialQuarterCode") && !stockSummaryObj.get("FinancialQuarterCode").isJsonNull()) {
							String dataType = metadata.get("FinancialQuarterCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("FinancialQuarterCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("FinancialQuarterCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("FinancialQuarterCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("FinancialQuarterCode").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("FinancialYearCode") && !stockSummaryObj.get("FinancialYearCode").isJsonNull()) {
							String dataType = metadata.get("FinancialYearCode");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("FinancialYearCode").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal"))
								cell.setCellValue(stockSummaryObj.get("FinancialYearCode").getAsDouble());
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("FinancialYearCode").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("FinancialYearCode").getAsInt());
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("AsOnDate").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						/*if (stockSummaryObj.has("SNoUnRestrictedQty")
								&& !stockSummaryObj.get("SNoUnRestrictedQty").isJsonNull()) {
							String dataType = metadata.get("SNoUnRestrictedQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("SNoUnRestrictedQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("SNoUnRestrictedQty").getAsDouble());
								totalQty+=stockSummaryObj.get("SNoUnRestrictedQty").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("SNoUnRestrictedQty").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("SNoUnRestrictedQty").getAsInt());
								totalQty+=stockSummaryObj.get("SNoUnRestrictedQty").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}*/
						if (stockSummaryObj.has("SNoBlockedQty") && !stockSummaryObj.get("SNoBlockedQty").isJsonNull()) {
							String dataType = metadata.get("SNoBlockedQty");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsDouble());
								totalBlockedQty+=stockSummaryObj.get("SNoBlockedQty").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("SNoBlockedQty").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsInt());
								totalBlockedQty+=stockSummaryObj.get("SNoBlockedQty").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("ExpiredInSourceUOM") && !stockSummaryObj.get("ExpiredInSourceUOM").isJsonNull()) {
							String dataType = metadata.get("ExpiredInSourceUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("ExpiredInSourceUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("ExpiredInSourceUOM").getAsDouble());
								totalExprQty+=stockSummaryObj.get("ExpiredInSourceUOM").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("ExpiredInSourceUOM").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("ExpiredInSourceUOM").getAsInt());
								totalExprQty+=stockSummaryObj.get("ExpiredInSourceUOM").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("SNOConsignment")
								&& !stockSummaryObj.get("SNOConsignment").isJsonNull()) {
							String dataType = metadata.get("SNOConsignment");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
								cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsDouble());
								totalConsignmentQtyValue += stockSummaryObj.get("SNOConsignment").getAsDouble();
							} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("SNOConsignment").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsInt());
								totalConsignmentQtyValue += stockSummaryObj.get("SNOConsignment").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						
						if (stockSummaryObj.has("UnrestrictedInSourceUOM") && !stockSummaryObj.get("UnrestrictedInSourceUOM").isJsonNull()) {
							String dataType = metadata.get("UnrestrictedInSourceUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsDouble());
								totalAvailableQty+=stockSummaryObj.get("UnrestrictedInSourceUOM").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsInt());
								totalAvailableQty+=stockSummaryObj.get("UnrestrictedInSourceUOM").getAsInt();
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("UOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (stockSummaryObj.has("UnrestrictedInBaseUOM")
								&& !stockSummaryObj.get("UnrestrictedInBaseUOM").isJsonNull()) {
							String dataType = metadata.get("UnrestrictedInBaseUOM");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble());
								totalAvlqtyInbaseUm+=stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsInt());
								totalAvlqtyInbaseUm+=stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble();
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
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("BaseUOM").getAsInt());
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						if (showFileds) {
							if (stockSummaryObj.has("StockValue") && !stockSummaryObj.get("StockValue").isJsonNull()) {
								String dataType = metadata.get("StockValue");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("StockValue").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("StockValue").getAsDouble());
									totalStackValue += stockSummaryObj.get("StockValue").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("StockValue").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("StockValue").getAsInt());
									totalStackValue += stockSummaryObj.get("StockValue").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}
						if (showFileds) {
							if (stockSummaryObj.has("SNOStockValueInRC")
									&& !stockSummaryObj.get("SNOStockValueInRC").isJsonNull()) {
								String dataType = metadata.get("SNOStockValueInRC");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("SNOStockValueInRC").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("SNOStockValueInRC").getAsDouble());
									totalStackValueInUsd += stockSummaryObj.get("SNOStockValueInRC").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("SNOStockValueInRC").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("SNOStockValueInRC").getAsInt());
									totalStackValueInUsd += stockSummaryObj.get("SNOStockValueInRC").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
						}
						if (stockSummaryObj.has("SNOASPValue")
								&& !stockSummaryObj.get("SNOASPValue").isJsonNull()) {
							String dataType = metadata.get("SNOASPValue");
							cell = row.createCell(cellNum.getAndIncrement());
							if (dataType.equalsIgnoreCase("Edm.String"))
								cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsString());
							else if (dataType.equalsIgnoreCase("Edm.Decimal")){
								cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsDouble());
								totalAspValue+=stockSummaryObj.get("SNOASPValue").getAsDouble();
							}
							else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
								Date date = convertLongToDate(stockSummaryObj.get("SNOASPValue").getAsString());
								style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
								cell.setCellValue(date);
								cell.setCellStyle(style);
							} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
								cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsInt());
								totalAspValue+=stockSummaryObj.get("SNOASPValue").getAsInt();
							}
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue("");
						}
						// sheet1.autoSizeColumn(i);
						
					}
					}		
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					/*Cell totalQtyCell = total.createCell(12);
					totalQtyCell.setCellValue(totalQty);*/
					//totalBlockedQty
					Cell totalBlockedQtyCell = total.createCell(17);
					totalBlockedQtyCell.setCellValue(totalBlockedQty);
					Cell totalExprQtyCell = total.createCell(18);
					totalExprQtyCell.setCellValue(totalExprQty);
					
					Cell totalAvailableQtyCell = total.createCell(20);
					totalAvailableQtyCell.setCellValue(totalAvailableQty);
				//	totalConsignmentQtyValue
					Cell totalConsignmentQtyValueCell = total.createCell(19);
					totalConsignmentQtyValueCell.setCellValue(totalConsignmentQtyValue);
					Cell totalAvlqtyInbaseUmCell = total.createCell(22);
					totalAvlqtyInbaseUmCell.setCellValue(totalAvlqtyInbaseUm);
					Cell totalAspValueCell=null;
					if (showFileds) {
						Cell totalStackValueCell = total.createCell(24);
						totalStackValueCell.setCellValue(totalStackValue);
						Cell totalStackValueInUsdCell = total.createCell(25);
						totalStackValueInUsdCell.setCellValue(totalStackValueInUsd);
						totalAspValueCell=total.createCell(26);
					}else{
						short lastCellNum = total.getLastCellNum();
						totalAspValueCell= total.createCell(lastCellNum+1);
					}
					totalAspValueCell.setCellValue(totalAspValue);
					
				} else {
					headers.forEach(cellName -> {
						Cell createCell = header.createCell(keyNum.getAndIncrement());
						createCell.setCellValue(cellName);
					});
					int rowNum = 0;
					Double totalQty=new Double(0);
					Double totalExprqty=new Double(0);
					Double totalAvblQty=new Double(0);
					Double totalStockVale=new Double(0);
					for (int i = 0; i < array.size(); i++) {
						
						JsonObject stockDetailsObj = array.get(i).getAsJsonObject();
						if (stockDetailsObj.has("MaterialNo") && !stockDetailsObj.get("MaterialNo").isJsonNull()
								&& !stockDetailsObj.get("MaterialNo").getAsString().equalsIgnoreCase("Non_MDT")
								&& !stockDetailsObj.get("MaterialNo").getAsString().equalsIgnoreCase("Discontinued")) {
							 rowNum++;
							AtomicInteger cellNum = new AtomicInteger(0);
							Row row = sheet1.createRow(rowNum);
							Cell cell = row.createCell(cellNum.getAndIncrement());
							if (stockDetailsObj.has("ProductCategoryDesc") && !stockDetailsObj.get("ProductCategoryDesc").isJsonNull()) {
								String dataType = metadata.get("ProductCategoryDesc");
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("ProductCategoryDesc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("ProductCategoryDesc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("ProductCategoryDesc").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("ProductCategoryDesc").getAsInt());
								}
							} else {
								cell.setCellValue("");
							}
							if (stockDetailsObj.has("ProductGroupDesc")
									&& !stockDetailsObj.get("ProductGroupDesc").isJsonNull()) {
								String dataType = metadata.get("ProductGroupDesc");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("ProductGroupDesc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("ProductGroupDesc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("ProductGroupDesc").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("ProductGroupDesc").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("DMSOrg2") && !stockDetailsObj.get("DMSOrg2").isJsonNull()) {
								String dataType = metadata.get("DMSOrg2");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("DMSOrg2").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("DMSOrg2").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("DMSDivision")
									&& !stockDetailsObj.get("DMSDivision").isJsonNull()) {
								String dataType = metadata.get("DMSDivision");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("DMSDivision").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("DMSDivision").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("DMSOrg1").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("FinancialWeekCode") && !stockDetailsObj.get("FinancialWeekCode").isJsonNull()) {
								String dataType = metadata.get("FinancialWeekCode");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("FinancialWeekCode").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("FinancialWeekCode").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("FinancialWeekCode").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("FinancialWeekCode").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("FinancialQuarterCode") && !stockDetailsObj.get("FinancialQuarterCode").isJsonNull()) {
								String dataType = metadata.get("FinancialQuarterCode");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("FinancialQuarterCode").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("FinancialQuarterCode").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("FinancialQuarterCode").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("FinancialQuarterCode").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("FinancialYearCode") && !stockDetailsObj.get("FinancialYearCode").isJsonNull()) {
								String dataType = metadata.get("FinancialYearCode");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("FinancialYearCode").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("FinancialYearCode").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("FinancialYearCode").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("FinancialYearCode").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							

							if (stockDetailsObj.has("StockOwnerID")
									&& !stockDetailsObj.get("StockOwnerID").isJsonNull()) {
								String dataType = metadata.get("StockOwnerID");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String")) {
									cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsString());
								} else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("StockOwnerID").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("MaterialNo").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

							if (stockDetailsObj.has("MaterialDesc")
									&& !stockDetailsObj.get("MaterialDesc").isJsonNull()) {
								String dataType = metadata.get("MaterialDesc");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("MaterialDesc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("MaterialDesc").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("AsOnDate").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

							/*if (stockDetailsObj.has("SerialNoHigh")
									&& !stockDetailsObj.get("SerialNoHigh").isJsonNull()) {
								String dataType = metadata.get("SerialNoHigh");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("SerialNoHigh").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("SerialNoHigh").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}*/

							if (stockDetailsObj.has("BatchNo") && !stockDetailsObj.get("BatchNo").isJsonNull()) {
								String dataType = metadata.get("BatchNo");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("BatchNo").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("BatchNo").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("BatchNo").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
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
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockDetailsObj.get("Quantity").getAsDouble());
									totalQty += stockDetailsObj.get("Quantity").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("Quantity").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("Quantity").getAsInt());
									totalQty += stockDetailsObj.get("Quantity").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

							/*if (stockDetailsObj.has("ExpiredInSourceUOM")
									&& !stockDetailsObj.get("ExpiredInSourceUOM").isJsonNull()) {
								String dataType = metadata.get("ExpiredInSourceUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("ExpiredInSourceUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockDetailsObj.get("ExpiredInSourceUOM").getAsDouble());
									totalExprqty += stockDetailsObj.get("ExpiredInSourceUOM").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(
											stockDetailsObj.get("ExpiredInSourceUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("ExpiredInSourceUOM").getAsInt());
									totalExprqty += stockDetailsObj.get("ExpiredInSourceUOM").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}*/

							/*if (stockDetailsObj.has("UnrestrictedInSourceUOM")
									&& !stockDetailsObj.get("UnrestrictedInSourceUOM").isJsonNull()) {
								String dataType = metadata.get("UnrestrictedInSourceUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInSourceUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInSourceUOM").getAsDouble());
									totalAvblQty += stockDetailsObj.get("UnrestrictedInSourceUOM").getAsDouble();
								}

								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(
											stockDetailsObj.get("UnrestrictedInSourceUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInSourceUOM").getAsInt());
									totalAvblQty += stockDetailsObj.get("UnrestrictedInSourceUOM").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}*/
							
							if (stockDetailsObj.has("UOM") && !stockDetailsObj.get("UOM").isJsonNull()) {
								String dataType = metadata.get("UOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("UOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("UOM").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("UOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("UOM").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							
							if (stockDetailsObj.has("UnrestrictedInBaseUOM")
									&& !stockDetailsObj.get("UnrestrictedInBaseUOM").isJsonNull()) {
								String dataType = metadata.get("UnrestrictedInBaseUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsDouble());
									// totalAvblQty +=
									// stockDetailsObj.get("UnrestrictedInBaseUOM").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(
											stockDetailsObj.get("UnrestrictedInBaseUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsInt());
									// totalAvblQty +=
									// stockDetailsObj.get("UnrestrictedInBaseUOM").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}


							if (stockDetailsObj.has("BaseUOM") && !stockDetailsObj.get("BaseUOM").isJsonNull()) {
								String dataType = metadata.get("BaseUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("BaseUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("BaseUOM").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("BaseUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("BaseUOM").getAsInt());
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

							if (showFileds) {
								if (stockDetailsObj.has("UnitPrice")
										&& !stockDetailsObj.get("UnitPrice").isJsonNull()) {
									String dataType = metadata.get("UnitPrice");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal"))
										cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsDouble());
									else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
										Date date = convertLongToDate(stockDetailsObj.get("UnitPrice").getAsString());
										style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
										cell.setCellValue(date);
										cell.setCellStyle(style);
									} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
										cell.setCellValue(stockDetailsObj.get("UnitPrice").getAsInt());
									}
								} else {
									cell = row.createCell(cellNum.getAndIncrement());
									cell.setCellValue("");
								}
							}

							if (showFileds) {
								if (stockDetailsObj.has("StockValue")
										&& !stockDetailsObj.get("StockValue").isJsonNull()) {
									String dataType = metadata.get("StockValue");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(stockDetailsObj.get("StockValue").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
										cell.setCellValue(stockDetailsObj.get("StockValue").getAsDouble());
										totalStockVale += stockDetailsObj.get("StockValue").getAsDouble();
									} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
										Date date = convertLongToDate(stockDetailsObj.get("StockValue").getAsString());
										style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
										cell.setCellValue(date);
										cell.setCellStyle(style);
									} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
										cell.setCellValue(stockDetailsObj.get("StockValue").getAsInt());
										totalStockVale += stockDetailsObj.get("StockValue").getAsInt();
									}
								} else {
									cell = row.createCell(cellNum.getAndIncrement());
									cell.setCellValue("");
								}
							}

							
						
							// sheet1.autoSizeColumn(i);
						}
					}	
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell totalQutyCell = total.createCell(19);
					totalQutyCell.setCellValue(totalQty);
					/*Cell totalExprCell = total.createCell(16);
					totalExprCell.setCellValue(totalExprqty);*/
					/*Cell totalAvblQtyCell = total.createCell(15);
					totalAvblQtyCell.setCellValue(totalAvblQty);*/
					if (showFileds) {
						Cell totalStockValeCell = total.createCell(24);
						totalStockValeCell.setCellValue(totalStockVale);
					}

				}
			}
			if (FILE_NAME == null) {
				getFilePath(request);
			}
			/*if (debug) {
				response.getWriter().println("File Created in this Path :" + FILE_NAME);
			}
			if (debug) {
				response.getWriter().println("File Created in the Path:" + FILE_NAME);
			}*/
			/*
			 * FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
			 * workbook.write(outputStream); // call the Send Email Method
			 * outputStream.flush();
			 */
			resultjsonObj.addProperty("Status", "000001");
			resultjsonObj.addProperty("ErrorCode", "");
			resultjsonObj.addProperty("Message", sheetName + "Created Successfully");
			return resultjsonObj;
		} catch (JsonParseException ex) {
			xlFileCreated = false;
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			if (debug)
				response.getWriter().println(buffer.toString());

			/*stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);*/

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

			/*stepNo++;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID,
					ex.getLocalizedMessage(), ex.getClass().getCanonicalName(), "", debug);*/

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
		/*
		 * String rootPath = System.getProperty("catalina.home"); ServletContext
		 * ctx = getServletContext(); String relativePath =
		 * ctx.getInitParameter("tempfile.dir"); String path = rootPath +
		 * File.separator + relativePath + File.separator + ";
		 */
		String filePath = getServletContext().getRealPath("/Resources/XlSheet/");
		FILE_NAME = filePath + "Sales & Stock Report.xlsx";
		File file = new File(FILE_NAME);
		if (file.exists())
			file.delete();
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

	private String getTodayDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date date = new Date();
		return formatter.format(date);
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

	

	
	

	

	

	private void downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

		try {
			if (FILE_NAME != null) {
				Path path = Paths.get(FILE_NAME);
				byte[] data = Files.readAllBytes(path);
				response.setHeader("Content-disposition", "attachment;filename=" + "Sales & Stock Report.xlsx");
				response.setHeader("charset", "iso-8859-1");
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setContentLength(data.length);
				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outputStream = null;
				try {
					outputStream = response.getOutputStream();
					outputStream.write(data, 0, data.length);
					outputStream.flush();
					outputStream.close();
					response.flushBuffer();
				} catch (RuntimeException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				response.getWriter().println("File Not Exist " + FILE_NAME);
			}

		} catch (InvalidPathException ex) {
			throw ex;

		} catch (Exception ex) {
			throw ex;
		}

	}

	private void downloadFile1(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
		try {
			if (FILE_NAME != null) {
				// File file=new File(FILE_NAME);
				Path path = Paths.get(FILE_NAME);
				byte[] data = Files.readAllBytes(path);
				/*
				 * response.setHeader("Content-disposition",
				 * "attachment;filename=" + "Sales & Stock Report.csv");
				 * response.setContentType("text/csv");
				 */
				response.setHeader("Content-disposition", "attachment;filename=" + "Sales & Stock Report.xls");
				response.setHeader("charset", "iso-8859-1");
				response.setContentType("application/vnd.ms-excel");
				/*
				 * response.setHeader("charset", "iso-8859-1");
				 * response.setContentType(
				 * "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
				 * );
				 */
				response.setContentLength(data.length);
				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outputStream = null;
				try {
					outputStream = response.getOutputStream();
					outputStream.write(data, 0, data.length);
					outputStream.flush();
					outputStream.close();
					response.flushBuffer();
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
		SXSSFWorkbook workbook = null;
		JsonObject emailRes = new JsonObject();
		JsonObject responseObj = new JsonObject();
		Set<String> ccEmails = new HashSet<>();
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
			//inputPayload = commonUtiles.getGetBody(request, response);
			String loginID = "",authOrgTypeID="",authOrgValue="";
			boolean shiwAllFileds=false;
			
       
			if (request.getParameter("SPGUID") != null && !request.getParameter("SPGUID").equalsIgnoreCase("")) {
				spGUID = request.getParameter("SPGUID");
			}
			if (request.getParameter("debug") != null && !request.getParameter("debug").equalsIgnoreCase("")) {
				debug = true;
			}
			
			
			
			/*if (debug) {
				response.getWriter().println("Received Input Payload:" + jsonPayload);
			}*/

			pcgoDataUrl = commonUtiles.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			pcgUserName = commonUtiles.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			pcgPassword = commonUtiles.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			agrgtrID = commonUtiles.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			if (spGUID != null && !spGUID.equalsIgnoreCase("")) {
				oDataUrl = commonUtiles.getODataDestinationProperties("URL", DestinationUtils.PUGWHANA);
				userName = commonUtiles.getODataDestinationProperties("User", DestinationUtils.PUGWHANA);
				password = commonUtiles.getODataDestinationProperties("Password", DestinationUtils.PUGWHANA);
				userPass = userName + ":" + password;
				/*
				 * executeUrl=oDataUrl+
				 * "UserPartners?$filter=PartnerID%20eq%20%27" + spGUID + "%27";
				 * JsonObject userPartnersObj =
				 * commonUtiles.executeURL(executeUrl, userPass, response);
				 * if(debug){
				 * response.getWriter().println("UserPartners Table Response:"
				 * +userPartnersObj); }
				 */

				// loginID=commonUtiles.getLoginID(request, response, debug);

				if (request.getParameter("LoginID") != null && !request.getParameter("LoginID").equalsIgnoreCase("")) {
					loginID = request.getParameter("LoginID");
				}
				
				if (debug) {
					response.getWriter().println("LoggedIn UserId:" + loginID);
				}
				if (loginID != null && !loginID.equalsIgnoreCase("")) {
					

					if (loginID != null && !loginID.equalsIgnoreCase("")) {
						executeUrl = oDataUrl + "UserAuthSet?$filter=LoginID%20eq%20%27" + loginID
								+ "%27%20and%20AuthOrgValue%20eq%20%27" + "SS_VIEW_DB_PRICE"
								+ "%27%20and%20AuthOrgTypeID%20eq%20%27" + "000011" + "%27";
						// executeUrl = oDataUrl +
						// "UserAuthSet?$filter=LoginID%20eq%20%27" + loginID +
						// "%27";
						if (debug) {
							response.getWriter().println("UserAuthSet execute Url:" + executeUrl);
						}
						JsonObject userAuthsetObj = commonUtiles.executeURL(executeUrl, userPass, response);
						if (userAuthsetObj != null
								&& !userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray()
										.isJsonNull()
								&& userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray()
										.size() > 0) {
							shiwAllFileds = true;
						}
					}
					if (debug) {
						response.getWriter().println("shiwAllFileds:" + shiwAllFileds);
					}
					workbook = new SXSSFWorkbook();
					workbook.setCompressTempFiles(true);
					oDataUrl = commonUtiles.getODataDestinationProperties("URL", DestinationUtils.SSMISHANA);
					userName = commonUtiles.getODataDestinationProperties("User", DestinationUtils.SSMISHANA);
					password = commonUtiles.getODataDestinationProperties("Password", DestinationUtils.SSMISHANA);
					userPass = userName + ":" + password;
					executeUrl = oDataUrl + "$metadata";
					if (debug) {
						response.getWriter()
								.println("Time before fetching metadata: " + new Date(System.currentTimeMillis()));
					}
					JSONObject metadata = commonUtiles.executeMetadatURL(executeUrl, userPass, response);
					if (debug) {
						response.getWriter()
								.println("Time after fetching metadata: " + new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) { response.getWriter().println("Metada :" +
					 * metadata); }
					 */

					JSONArray entityArray = metadata.getJSONObject("edmx:Edmx").getJSONObject("edmx:DataServices")
							.getJSONObject("Schema").getJSONArray("EntityType");
					Map<String, String> salesSummaryMetadata = new HashMap<>();
					Map<String, String> invoiceMeta = new HashMap<>();
					Map<String, String> dataSubmissionMetadata = new HashMap<>();
					if (debug) {
						response.getWriter().println("Time before Taking the DataTypes from metadata: "
								+ new Date(System.currentTimeMillis()));
					}
					for (int i = 0; i < entityArray.length(); i++) {
						JSONObject entity = entityArray.getJSONObject(i);
						if (entity.getString("Name").equalsIgnoreCase("V_SSCPSP_T-7_SUM_SECSALESType")) {
							JSONArray propertyArray = entity.getJSONArray("Property");
							for (int j = 0; j < propertyArray.length(); j++) {
								JSONObject jsonObject = propertyArray.getJSONObject(j);
								String dataType = jsonObject.getString("Type");
								String fieldName = jsonObject.getString("Name");
								if (fieldName.equalsIgnoreCase("SCPGuid"))
									salesSummaryMetadata.put(fieldName, dataType);
								if (fieldName.equalsIgnoreCase("SCPName1"))
									salesSummaryMetadata.put(fieldName, dataType);
								if (fieldName.equalsIgnoreCase("DmsDivision_I"))
									salesSummaryMetadata.put(fieldName, dataType);
								if (fieldName.equalsIgnoreCase("ASPGROSSAMT"))
									salesSummaryMetadata.put(fieldName, dataType);
								if (fieldName.equalsIgnoreCase("QUANTITYINBASEUOM"))
									salesSummaryMetadata.put(fieldName, dataType);
							}
						}

						if (entity.getString("Name").equalsIgnoreCase("V_SSCPSP_T-7_SECSALESType")) {
							JSONArray propertyArray = entity.getJSONArray("Property");
							for (int j = 0; j < propertyArray.length(); j++) {
								JSONObject jsonObject = propertyArray.getJSONObject(j);
								String dataType = jsonObject.getString("Type");
								String fieldName = jsonObject.getString("Name");
								if (fieldName.equalsIgnoreCase("DMSOrg2"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DmsDivision_I"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg3"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg1"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FromCPGuid"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FromCPName"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("CountryID"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("InvoiceTypeDesc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("InvoiceNo"))
									invoiceMeta.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("InvoiceDate"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("SoldToName"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("SoldToBPID"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ExternalSoldToCPName"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ItemNo"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("MaterialNo"))
									invoiceMeta.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("MaterialDesc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ExternalMaterialDesc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("Batch"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("InvoiceQty"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UOM_I"))
									invoiceMeta.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("ItemUnitPrice"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("GrossAmount"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DiscountPerc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ItemTotalDiscAmount"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("AssessableValue"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ItemTaxValue"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ItemNetAmount"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ItemNetAmountinRC"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ASPGrossAmount"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("InvoiceQtyASPUOM"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ASPUOM"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("InvoiceStatusDesc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialYearCode"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialQuarterCode"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialWeekCode"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductGroupDesc"))
									invoiceMeta.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductCategoryDesc"))
									invoiceMeta.put(fieldName, dataType);
							}

						}

						if (entity.getString("Name").equalsIgnoreCase("UserSyncSubmissionReportType")) {
							JSONArray propertyArray = entity.getJSONArray("Property");
							for (int j = 0; j < propertyArray.length(); j++) {
								JSONObject jsonObject = propertyArray.getJSONObject(j);
								String dataType = jsonObject.getString("Type");
								String fieldName = jsonObject.getString("Name");
								if (fieldName.equalsIgnoreCase("PartnerID"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("CPName"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ERPSoftware"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DaysLastStockSync"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DaysLastSalesSync"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DaysLastGRSync"))
									dataSubmissionMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("LastInvDate"))
									dataSubmissionMetadata.put(fieldName, dataType);
							}
						}
					}
					if (debug) {
						response.getWriter().println("Time Afetr Taking the DataTypes from metadata: "
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) {
					 * response.getWriter().println("oDataUrl  Url:" +
					 * oDataUrl); }
					 */
					List<String> headers = new java.util.LinkedList<>();
					headers.add("Distributor Code");
					headers.add("Distributor Name");
					headers.add("Operating Unit");
					headers.add("ASP Gross Amount");
					headers.add("Quantity in ASP UOM");

					sumSecSales = properties.getProperty("V_SSCPSP_T-7_SUM_SECSALES");
					/*
					 * executeUrl = oDataUrl + sumSecSales +
					 * "?$filter=SPGUID%20eq%20%27" + scpGUID +
					 * "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
					 */

					executeUrl = oDataUrl + sumSecSales
							+ "?$select=SCPGuid,SCPName1,DmsDivision_I,ASPGROSSAMT,QUANTITYINBASEUOM"
							+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
							+ "%27";

					/*
					 * if (debug) {
					 * response.getWriter().println("Sales Summary executeUrl:"
					 * + executeUrl); }
					 */
					if (debug) {
						response.getWriter().println("Time before fetch is started for V_SSCPSP_T-7_SUM_SECSALES: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonObject sumSecsalesObj = commonUtiles.executeURL(executeUrl, userPass, response);
					/*
					 * if (debug) { response.getWriter().
					 * println("Sales Summary Records from Db:" +
					 * sumSecsalesObj); }
					 */
					if (debug) {
						response.getWriter().println("Time after fetch is completed for V_SSCPSP_T-7_SUM_SECSALES: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results")
							.getAsJsonArray();

					if (debug) {
						response.getWriter().println("Total records retrieved from V_SSCPSP_T-7_SUM_SECSALES Table:"
								+ sumSecSalesArray.size());
					}
					if (debug) {
						response.getWriter()
								.println("Before creating XL Sheet for the V_SSCPSP_T-7_SUM_SECSALES records:"
										+ new Date(System.currentTimeMillis()));
					}
					/*
					 * stepNo++; oDataLogs.insertMessageForAppLogs(request,
					 * response, logID, "I", "/ARTEC/PY",
					 * sumSecSalesArray.size() + "", stepNo,
					 * "Total Sales Summary Records for the SPGUID",
					 * pcgoDataUrl, pcgUserPass, agrgtrID, spGUID, "", "",
					 * debug);
					 */
					createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, salesSummaryMetadata, headers,
							shiwAllFileds);
					if (debug) {
						response.getWriter()
								.println("After creating XL Sheet for the V_SSCPSP_T-7_SUM_SECSALES records:"
										+ new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) { response.getWriter().
					 * println("Sales Summary xl Sheet Created Response:" +
					 * createdSheetRes); }
					 */
					secSales = properties.getProperty("V_SSCPSP_T-7_SECSALES");
					/*
					 * executeUrl = oDataUrl + secSales +
					 * "?$filter=SPGUID%20eq%20%27" + scpGUID +
					 * "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
					 */
					executeUrl = oDataUrl + secSales
							+ "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DmsDivision_I,DMSOrg3,DMSOrg1,FromCPGuid,FromCPName,CountryID,InvoiceTypeDesc,InvoiceNo,InvoiceDate,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,SoldToName,SoldToBPID,ExternalSoldToCPName,ItemNo,MaterialNo,MaterialDesc,ExternalMaterialDesc,Batch,InvoiceQty,UOM_I,ItemUnitPrice,GrossAmount,DiscountPerc,ItemTotalDiscAmount,AssessableValue,ItemTaxValue,ItemNetAmount,ItemNetAmountinRC,ASPGrossAmount,InvoiceQtyASPUOM,ASPUOM,InvoiceStatusDesc"
							+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
							+ "%27";

					headers.clear();
					headers.add("Category"); //0
					headers.add("Product Group");
					headers.add("Portfolio");
					headers.add("Operating Unit");
					headers.add("Business Unit");
					// change label Division to Product Family
					headers.add("Product Family");
					headers.add("Distributor Code");
					headers.add("Distributor Name");
					headers.add("Country");
					headers.add("Invoice Type Desc");
					headers.add("Invoice No");
					headers.add("Invoice Date");
					headers.add("Week");
					headers.add("Quarter");
					headers.add("Fiscal Year");
					headers.add("Sold To Party Name");
					headers.add("Sold To Party Code");
					headers.add("Source Sold To Party Name");
					headers.add("Item No");
					headers.add("Material No");
					headers.add("Material Description");
					headers.add("Source Material Description");
					headers.add("Batch");
					headers.add("Quantity");
					headers.add("DB UOM");
					if (shiwAllFileds) {
						headers.add("Unit Price");
						headers.add("Gross Amount");
						headers.add("Discount %");
						headers.add("Discount Amount");
						headers.add("Taxable Amount");
						headers.add("Tax Amount");
						headers.add("Net Amount");
						headers.add("Net Amount in USD");
					}
					headers.add("Gross Value");
					headers.add("Quantity in ASP UOM");
					headers.add("ASP UOM");
					headers.add("Invoice Status");
					
					
					/*
					 * if (debug) {
					 * response.getWriter().println("Invoices execute Url:" +
					 * executeUrl); }
					 */
					if (debug) {
						response.getWriter().println("Time before fetch is started for V_SSCPSP_T-7_SECSALES: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonObject secSalesObj = commonUtiles.executeURL(executeUrl, userPass, response);

					if (debug) {
						response.getWriter().println("Time after fetch is completed for V_SSCPSP_T-7_SECSALES: "
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) {
					 * response.getWriter().println("Invoices Records from Db:"
					 * + secSalesObj); }
					 */
					JsonArray secSalesObjArray = secSalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					if (debug) {
						response.getWriter().println(
								"Total records retrieved from V_SSCPSP_T-7_SECSALES Table: " + secSalesObjArray.size());
					}
					if (debug) {
						response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_T-7_SECSALES records:"
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * stepNo++; oDataLogs.insertMessageForAppLogs(request,
					 * response, logID, "I", "/ARTEC/PY",
					 * secSalesObjArray.size() + "", stepNo,
					 * "Total Invoices Records for the SPGUID", pcgoDataUrl,
					 * pcgUserPass, agrgtrID, spGUID, "", "", debug);
					 */
					createdSheetRes = createXlSheet(secSalesObjArray, workbook, invoices, debug, response, request,
							logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, invoiceMeta, headers,
							shiwAllFileds);
					/*
					 * if (debug) { response.getWriter().
					 * println("Invoices xl Sheet Created Response:" +
					 * createdSheetRes); }
					 */
					if (debug) {
						response.getWriter().println("After creating XL Sheet for the V_SSCPSP_T-7_SECSALES records:"
								+ new Date(System.currentTimeMillis()));
					}

					userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");
					/*
					 * executeUrl = oDataUrl + userSyncSubmissionReport +
					 * "?$filter=SPGUID%20eq%20%27" + scpGUID +
					 * "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
					 */

					executeUrl = oDataUrl + userSyncSubmissionReport
							+ "?$select=PartnerID,CPName,ERPSoftware,DaysLastStockSync,DaysLastSalesSync,DaysLastGRSync,LastInvDate"
							+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
							+ "%27";
					/*
					 * if (debug) { response.getWriter().
					 * println("Data Submission execute Url:" + executeUrl); }
					 */
					if (debug) {
						response.getWriter().println("Time before fetch is started for UserSyncSubmissionReport: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonObject UserSyncSubmissionReportObj = commonUtiles.executeURL(executeUrl, userPass, response);
					if (debug) {
						response.getWriter().println("Time after fetch is completed for UserSyncSubmissionReport: "
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) { response.getWriter().
					 * println("Data Submission Records from Db:" +
					 * UserSyncSubmissionReportObj); }
					 */
					oDataUrl = commonUtiles.getODataDestinationProperties("URL", DestinationUtils.SSGWHANA);
					userName = commonUtiles.getODataDestinationProperties("User", DestinationUtils.SSGWHANA);
					password = commonUtiles.getODataDestinationProperties("Password", DestinationUtils.SSGWHANA);
					userPass = userName + ":" + password;
					cpSpStkItems = properties.getProperty("SSStockItemsSNosBySPCube");

					Map<String, String> stockSummaryMetadata = new HashMap<>();
					Map<String, String> stockDetailsMetadata = new HashMap<>();
					executeUrl = oDataUrl + "$metadata";
					if (debug) {
						response.getWriter().println(
								"Time before fetching metadata for V_SSCPSP_STKITMS and V_SSCPSP_STKITMSNOS Table.  : "
										+ new Date(System.currentTimeMillis()));
					}
					metadata = commonUtiles.executeMetadatURL(executeUrl, userPass, response);
					if (debug) {
						response.getWriter().println(
								"Time After fetching metadata for V_SSCPSP_STKITMS and V_SSCPSP_STKITMSNOS Table.  : "
										+ new Date(System.currentTimeMillis()));
					}
					entityArray = metadata.getJSONObject("edmx:Edmx").getJSONObject("edmx:DataServices")
							.getJSONObject("Schema").getJSONArray("EntityType");
					if (debug) {
						response.getWriter().println(
								"Time before Taking the column Datatypes from Metadata of  V_SSCPSP_STKITMS and V_SSCPSP_STKITMSNOS Table.  : "
										+ new Date(System.currentTimeMillis()));
					}
					for (int i = 0; i < entityArray.length(); i++) {
						JSONObject entity = entityArray.getJSONObject(i);
						if (entity.getString("Name").equalsIgnoreCase("SSStockItemsSNosBySPCubeType")) {
							JSONArray propertyArray = entity.getJSONArray("Property");
							for (int j = 0; j < propertyArray.length(); j++) {
								JSONObject jsonObject = propertyArray.getJSONObject(j);
								String dataType = jsonObject.getString("Type");
								String fieldName = jsonObject.getString("Name");
								if (fieldName.equalsIgnoreCase("DMSOrg2"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSDivision"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg3"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg1"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockOwnerID"))
									stockSummaryMetadata.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("StockOwnerName"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("CountryID"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("MaterialNo"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("MaterialDesc"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ExternalMaterialDesc"))
									stockSummaryMetadata.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("StorageLocation"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("AsOnDate"))
									stockSummaryMetadata.put(fieldName, dataType);
								/*
								 * else if (fieldName.equalsIgnoreCase(
								 * "SNoUnRestrictedQty"))
								 * stockSummaryMetadata.put(fieldName,
								 * dataType);
								 */
								else if (fieldName.equalsIgnoreCase("SNoBlockedQty"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ExpiredInSourceUOM"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("SNOConsignment"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UnrestrictedInSourceUOM"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UOM"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UnrestrictedInBaseUOM"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("BaseUOM"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockValue"))
									stockSummaryMetadata.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("SNOStockValueInRC"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("SNOASPValue"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductCategoryDesc"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductGroupDesc"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialWeekCode"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialQuarterCode"))
									stockSummaryMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialYearCode"))
									stockSummaryMetadata.put(fieldName, dataType);
							}
						}
					}
					for (int i = 0; i < entityArray.length(); i++) {
						JSONObject entity = entityArray.getJSONObject(i);
						if (entity.getString("Name").equalsIgnoreCase("SSStockItemsSNosBySPCubeType")) {
							JSONArray propertyArray = entity.getJSONArray("Property");
							for (int j = 0; j < propertyArray.length(); j++) {
								JSONObject jsonObject = propertyArray.getJSONObject(j);
								String dataType = jsonObject.getString("Type");
								String fieldName = jsonObject.getString("Name");
								if (fieldName.equalsIgnoreCase("DMSOrg2"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSDivision"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg3"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("DMSOrg1"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockOwnerID"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockOwnerName"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("MaterialNo"))
									stockDetailsMetadata.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("MaterialDesc"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ExternalMaterialDesc"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StorageLocation"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("AsOnDate"))
									stockDetailsMetadata.put(fieldName, dataType);
								/*else if (fieldName.equalsIgnoreCase("SerialNoHigh"))
									stockDetailsMetadata.put(fieldName, dataType);*/
								else if (fieldName.equalsIgnoreCase("BatchNo"))
									stockDetailsMetadata.put(fieldName, dataType);

								else if (fieldName.equalsIgnoreCase("ExpiryDate"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockTypeDesc"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("Quantity"))
									stockDetailsMetadata.put(fieldName, dataType);
								/*
								 * else if
								 * (fieldName.equalsIgnoreCase("ExpiredQty"))
								 * stockDetailsMetadata.put(fieldName,
								 * dataType); else if
								 * (fieldName.equalsIgnoreCase("AvailableQty"))
								 * stockDetailsMetadata.put(fieldName,
								 * dataType);
								 */
								/*
								 * else if (fieldName.equalsIgnoreCase(
								 * "ExpiredInSourceUOM"))
								 * stockDetailsMetadata.put(fieldName,
								 * dataType);
								 */
								/*else if (fieldName.equalsIgnoreCase("UnrestrictedInSourceUOM"))
									stockDetailsMetadata.put(fieldName, dataType);*/
								else if (fieldName.equalsIgnoreCase("UOM"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UnrestrictedInBaseUOM"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("BaseUOM"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("UnitPrice"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("StockValue"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductCategoryDesc"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("ProductGroupDesc"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialWeekCode"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialQuarterCode"))
									stockDetailsMetadata.put(fieldName, dataType);
								else if (fieldName.equalsIgnoreCase("FinancialYearCode"))
									stockDetailsMetadata.put(fieldName, dataType);

							}
						}
					}
					if (debug) {
						response.getWriter().println(
								"Time After Taking the column Datatypes from Metadata of  V_SSCPSP_STKITMS and V_SSCPSP_STKITMSNOS Table.  : "
										+ new Date(System.currentTimeMillis()));
					}
					executeUrl = oDataUrl + cpSpStkItems
							+ "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,StockOwnerID,StockOwnerName,CountryID,MaterialNo,MaterialDesc,ExternalMaterialDesc,StorageLocation,AsOnDate,SNoBlockedQty,ExpiredInSourceUOM,SNOConsignment,UnrestrictedInSourceUOM,UOM,UnrestrictedInBaseUOM,BaseUOM,StockValue,SNOStockValueInRC,SNOASPValue"
							+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
							+ "%27%20and%20LoginID%20eq%20%27" + loginID + "%27";

					if (debug) {
						response.getWriter().println("Time before fetch is started for V_SSCPSP_STKITMS: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonObject cpSpStockItemsObj = commonUtiles.executeURL(executeUrl, userPass, response);

					if (debug) {
						response.getWriter().println("Time after fetch is completed for V_SSCPSP_STKITMS: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonArray cpSpStockItemsObjArray = cpSpStockItemsObj.get("d").getAsJsonObject().get("results")
							.getAsJsonArray();
					if (debug) {
						response.getWriter().println(
								"Total records retrieved from V_SSCPSP_STKITMS Table:" + cpSpStockItemsObjArray.size());
					}
					if (debug) {
						response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_STKITMS records:"
								+ new Date(System.currentTimeMillis()));
					}
					headers.clear();
					headers.add("Category");
					headers.add("Product Group");
					headers.add("Portfolio"); // 0
					headers.add("Operating Unit");// 1
					headers.add("Business Unit");// 2
					// change the label Division to Product Family
					headers.add("Product Family");// 3
					
					headers.add("Week");
					headers.add("Quarter");
					headers.add("Fiscal Year");
					
					headers.add("Distributor Code");// 4
					headers.add("Distributor Name");// 5
					headers.add("Country");// 6
					headers.add("Material No");// 7
					headers.add("Material Description");// 8
					headers.add("Source Material Description");// 9
					headers.add("Storage Location");// 10
					headers.add("As On Date");// 11
					// headers.add("Quantity");
					headers.add("Blocked Qty");// 12
					headers.add("Expired Qty");// 13
					headers.add("Consignment Qty");// 14
					headers.add("Saleable Qty");// 15
					headers.add("DB UOM");// 16
					headers.add("Saleable Qty in ASP UOM");// 17
					headers.add("ASP UOM");// 18
					if (shiwAllFileds) {
						headers.add("Stock Value");// 19
						headers.add("Stock Value in USD");// 20
					}
					headers.add("Gross Value");// 21
					createdSheetRes = createXlSheet(cpSpStockItemsObjArray, workbook, stockSummary, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, stockSummaryMetadata,
							headers, shiwAllFileds);
					if (debug) {
						response.getWriter().println("After creating XL Sheet for the V_SSCPSP_STKITMS records:"
								+ new Date(System.currentTimeMillis()));
					}

					cpSpStkItemNos = properties.getProperty("SSStockItemsSNosBySPCube");

					executeUrl = oDataUrl + cpSpStkItemNos
							+ "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,StockOwnerID,StockOwnerName,MaterialNo,MaterialDesc,ExternalMaterialDesc,StorageLocation,AsOnDate,BatchNo,ExpiryDate,StockTypeDesc,Quantity,UOM,UnrestrictedInBaseUOM,BaseUOM,UnitPrice,StockValue"
							+ "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID
							+ "%27%20and%20LoginID%20eq%20%27" + loginID + "%27%20and%20IsQtyPositive%20eq%20%27" + "X"
							+ "%27";

					if (debug) {
						response.getWriter().println("Time before fetch is started for V_SSCPSP_STKITMSNOS: "
								+ new Date(System.currentTimeMillis()));
					}
					JsonObject cpSpStkItemNosObj = commonUtiles.executeURL(executeUrl, userPass, response);

					if (debug) {
						response.getWriter().println("Time after fetch is completed for V_SSCPSP_STKITMSNOS: "
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * if (debug) { response.getWriter().
					 * println("Stock Details Records from Db:" +
					 * cpSpStkItemNosObj); }
					 */
					JsonArray cpSpStkItemNosObjArray = cpSpStkItemNosObj.get("d").getAsJsonObject().get("results")
							.getAsJsonArray();
					if (debug) {
						response.getWriter().println("Total records retrieved from V_SSCPSP_STKITMSNOS Table:"
								+ cpSpStkItemNosObjArray.size());
					}
					if (debug) {
						response.getWriter().println("Before creating XL Sheet for the V_SSCPSP_STKITMSNOS records:"
								+ new Date(System.currentTimeMillis()));
					}
					/*
					 * stepNo++; oDataLogs.insertMessageForAppLogs(request,
					 * response, logID, "I", "/ARTEC/PY",
					 * cpSpStkItemNosObjArray.size() + "", stepNo,
					 * "Total Stock Details Records for the SPGUID",
					 * pcgoDataUrl, pcgUserPass, agrgtrID, spGUID, "", "",
					 * debug);
					 */
					headers.clear();
					headers.add("Category");
					headers.add("Product Group");
					headers.add("Portfolio");
					headers.add("Operating Unit");
					headers.add("Business Unit");
					// change the Label to Product Family
					headers.add("Product Family");
					
					headers.add("Week");
					headers.add("Quarter");
					headers.add("Fiscal Year");
					
					headers.add("Distributor Code");
					headers.add("Distributor Name");
					headers.add("Marterial No");
					headers.add("Material Description");
					headers.add("Source Material Description");
					headers.add("Storage Location");
					headers.add("As On Date");
				//	headers.add("Serial No");
					headers.add("Batch No");
					headers.add("Expiry Date");
					headers.add("Stock Type Description");
					headers.add("Quantity");
					// headers.add("Expired Qty");
				//	headers.add("Available Qty");
					headers.add("DB UOM");
					headers.add("Saleable Qty in ASP UOM");
					headers.add("ASP UOM");
					if (shiwAllFileds) {
						headers.add("Unit Price");
						headers.add("Stock Value");
					}
					
					
					createdSheetRes = createXlSheet(cpSpStkItemNosObjArray, workbook, stockDetails, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, stockDetailsMetadata,
							headers, shiwAllFileds);

					if (debug) {
						response.getWriter().println("After creating XL Sheet for the V_SSCPSP_STKITMSNOS records:"
								+ new Date(System.currentTimeMillis()));
					}
					headers.clear();
					/*
					 * if (debug) { response.getWriter().
					 * println("Stock Details Sheet Created Response:" +
					 * createdSheetRes); }
					 */
					JsonArray dataSubmissingArr = UserSyncSubmissionReportObj.get("d").getAsJsonObject().get("results")
							.getAsJsonArray();
					/*
					 * stepNo++; oDataLogs.insertMessageForAppLogs(request,
					 * response, logID, "I", "/ARTEC/PY",
					 * dataSubmissingArr.size() + "", stepNo,
					 * "Total Data Submission Records for the SPGUID",
					 * pcgoDataUrl, pcgUserPass, agrgtrID, spGUID, "", "",
					 * debug);
					 */
					headers.clear();
					headers.add("Distributor Code");
					headers.add("Distributor Name");
					headers.add("ERP Software");
					headers.add("Days Last Stock Sync");
					headers.add("Days Last Sales Sync");
					headers.add("Days Last GR Sync");
					headers.add("Latest Invoice Date");
					if (debug) {
						response.getWriter().println("Total records retrieved from UserSyncSubmissionReport Table:"
								+ dataSubmissingArr.size());
					}
					if (debug) {
						response.getWriter()
								.println("Before creating XL Sheet for the UserSyncSubmissionReport records:"
										+ new Date(System.currentTimeMillis()));
					}
					createdSheetRes = createXlSheet(dataSubmissingArr, workbook, dataSubmission, debug, response,
							request, logID, stepNo, pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs,
							dataSubmissionMetadata, headers, shiwAllFileds);

					if (debug) {
						response.getWriter().println("After creating XL Sheet for the UserSyncSubmissionReport records:"
								+ new Date(System.currentTimeMillis()));
					}

					if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
						// downloadFile(request, response);
						if (!debug) {
							response.setContentType("application/vnd.ms-excel");
							response.setHeader("Content-Disposition", "attachment; filename=Sales & Stock Report.xls");
							// response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
					// login id not exist
					responseObj.addProperty("Message", "Login Id Missing in the payload");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);
				}
			}else {
				
				responseObj.addProperty("Message", "Input Payload doesn't contain SPGUID");
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
			/*oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
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
			/*oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred", pcgoDataUrl, pcgUserPass, agrgtrID, ex.getLocalizedMessage(),
					ex.getClass().getCanonicalName(), "", debug);*/
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
		}
		
	}

	

	private static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
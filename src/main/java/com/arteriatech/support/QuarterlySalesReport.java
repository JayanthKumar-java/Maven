package com.arteriatech.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sun.mail.smtp.SMTPSendFailedException;

public class QuarterlySalesReport extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String FILE_NAME = null;
	private String pugOdataUrl = "";
	private String pugUseruseName = "";
	private String pugUserpassword = "";
	private String pugUserPass = "";
	private String pcgoDataUrl = "";
	private String pcgUserName = "";
	private String pcgPassword = "";
	private String agrgtrID = "";
	private String pcgUserPass = "";
	private String ssgwODataUrl = "";
	private String ssgwUserName = "";
	private String ssgwPassword = "";
	private String ssgwUserPass = "";
	private String ssmisODataUrl = "";
	private String ssmisUserName = "";
	private String ssmisPassword = "";
	private String ssmisUserPass = "";
	private String accountID = "";
	private String userName = "";
	private String passWord = "";

	@Override
	public void init() throws ServletException {/*
		CommonUtils commonUtils = new CommonUtils();
		try {
			pugOdataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PUGWHANA);
			pugUseruseName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PUGWHANA);
			pugUserpassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PUGWHANA);
			pugUserPass = pugUseruseName + ":" + pugUserpassword;
			pcgoDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			pcgUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			pcgPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			agrgtrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			ssgwODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSGWHANA);
			ssgwUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSGWHANA);
			ssgwPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSGWHANA);
			ssgwUserPass = ssgwUserName + ":" + ssgwPassword;

			ssmisODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSMISHANA);
			ssmisUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSMISHANA);
			ssmisPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSMISHANA);
			ssmisUserPass = ssmisUserName + ":" + ssmisPassword;
			userName = commonUtils.getODataDestinationProperties("emailid", DestinationUtils.PLATFORM_EMAIL);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PLATFORM_EMAIL);
			Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			accountID = tenantContext.getTenant().getAccount().getId();
		} catch (NamingException e) {
			// TODO Auto-generated catch block

			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			throw new RuntimeException(resObj.toString());
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
			throw new RuntimeException(resObj.toString());
		}
	*/}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		doPost(req, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		JsonParser parser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		boolean debug = false;
		try {
			final String  inputPayload = commonUtils.getGetBody(request, response);
			String servletPath="";
			if(request.getServletPath()!=null){
			   servletPath =request.getServletPath();
			}
			final String  path=servletPath;
			
			pugOdataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PUGWHANA);
			pugUseruseName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PUGWHANA);
			pugUserpassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PUGWHANA);
			pugUserPass = pugUseruseName + ":" + pugUserpassword;
			pcgoDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			pcgUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			pcgPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			agrgtrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			pcgUserPass = pcgUserName + ":" + pcgPassword;
			ssgwODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSGWHANA);
			ssgwUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSGWHANA);
			ssgwPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSGWHANA);
			ssgwUserPass = ssgwUserName + ":" + ssgwPassword;
			ssmisODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSMISHANA);
			ssmisUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSMISHANA);
			ssmisPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSMISHANA);
			ssmisUserPass = ssmisUserName + ":" + ssmisPassword;
			userName = commonUtils.getODataDestinationProperties("emailid", DestinationUtils.PLATFORM_EMAIL);
			passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PLATFORM_EMAIL);
			Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			accountID = tenantContext.getTenant().getAccount().getId();
			
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				jsonPayload = (JsonObject) parser.parse(inputPayload);

				if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
					debug = true;
				}
				
				/*if(debug){
					response.getWriter().println("pugOdataUrl:"+pugOdataUrl);
					response.getWriter().println("pugUseruseName:"+pugUseruseName);
					response.getWriter().println("pugUserpassword:"+pugUserpassword);
					
					response.getWriter().println("pcgoDataUrl:"+pcgoDataUrl);
					response.getWriter().println("pcgUserName:"+pcgUserName);
					response.getWriter().println("pcgPassword:"+pcgPassword);
					
					response.getWriter().println("ssgwODataUrl:"+ssgwODataUrl);
					response.getWriter().println("ssgwUserName:"+ssgwUserName);
					response.getWriter().println("ssgwPassword:"+ssgwPassword);
					
					response.getWriter().println("ssmisODataUrl:"+ssmisODataUrl);
					response.getWriter().println("ssmisUserName:"+ssmisUserName);
					response.getWriter().println("ssmisPassword:"+ssmisPassword);
					
					response.getWriter().println("userName:"+userName);
					response.getWriter().println("passWord:"+passWord);
					response.getWriter().println("agrgtrID:"+agrgtrID);
				}*/
				
				if (jsonPayload.get("d").getAsJsonObject().has("LogID") && !jsonPayload.get("d").getAsJsonObject().get("LogID").isJsonNull() && !jsonPayload.get("d").getAsJsonObject().get("LogID").getAsString().equalsIgnoreCase("")) {
					
					final AsyncContext asyncContext = request.startAsync(request, response);
					asyncContext.addListener(new AsyncListner());
				    asyncContext.setTimeout(3600000);
					asyncContext.start(new Runnable() {
						@Override
						public void run() {
							boolean debug = false;
							JsonParser parser = new JsonParser();
							JsonObject jsonPayload = new JsonObject();
							jsonPayload = (JsonObject) parser.parse(inputPayload);
							String executeUrl = "";
							String salesSummaryEntity = "", invoiceEntity = "", userSyncSubmissionReport = "";// secSales
							Properties properties = new Properties();
							
							JsonObject emailRes = new JsonObject();
							JsonObject responseObj = new JsonObject();
							Set<String> ccEmails = new HashSet<>();
							JsonObject createdSheetRes = new JsonObject();
							String cpSpStkItems = "", cpSpStkItemNos = "", logID = "";
							ODataLogs oDataLogs = new ODataLogs();
							String salesSummary = "", invoices = "", stockSummary = "", dataSubmission = "";
							//int stepNo = 0;
							AtomicInteger stepNo=new AtomicInteger(1);
							boolean showAllFields = false;
							String cpiLogId = "";

							cpiLogId = jsonPayload.get("d").getAsJsonObject().get("LogID").getAsString();
							try {

								if (agrgtrID != null && !agrgtrID.equalsIgnoreCase("")) {
									properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
									salesSummary = properties.getProperty("SalesSummary");
									invoices = properties.getProperty("Invoices");
									stockSummary = properties.getProperty("StockSummary");
									dataSubmission = properties.getProperty("DataSubmission");
									String loginUser = commonUtils.getUserPrincipal(request, "name", response);
									String spGuid="";
									//logID = oDataLogs.insertSendSalesReportApplicationLogs(request, response, "Java", "QuarterlySalesReport", "Process Started", "" + stepNo, path, pcgoDataUrl, pcgUserPass, agrgtrID, loginUser,spGuid, debug);
									if (jsonPayload.has("d") && !jsonPayload.get("d").getAsJsonObject().isJsonNull() && jsonPayload.get("d").getAsJsonObject().has("results") && !jsonPayload.get("d").getAsJsonObject().get("results").isJsonNull() && jsonPayload.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
										JsonArray inputArry=jsonPayload.get("d").getAsJsonObject().get("results").getAsJsonArray();
										if (inputArry.get(0).getAsJsonObject().has("SPGUID") && !inputArry.get(0).getAsJsonObject().get("SPGUID").isJsonNull()){
											spGuid=inputArry.get(0).getAsJsonObject().get("SPGUID").getAsString();
									   }
									}
									
									logID = oDataLogs.insertSendSalesReportApplicationLogs(request, response, "Java", "QuarterlySalesReport", "Process Started", "" + stepNo.getAndIncrement(), path, pcgoDataUrl, pcgUserPass, agrgtrID, loginUser,spGuid, debug);
									
									if (jsonPayload.has("d") && !jsonPayload.get("d").getAsJsonObject().isJsonNull() && jsonPayload.get("d").getAsJsonObject().has("results") && !jsonPayload.get("d").getAsJsonObject().get("results").isJsonNull() && jsonPayload.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
										JsonArray asJsonArray = jsonPayload.get("d").getAsJsonObject().get("results").getAsJsonArray();
										executeUrl = pcgoDataUrl + "ConfigTypsetTypeValues?$filter=Typeset%20eq%20%27DMSADM" + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";

										JsonObject ccEmailObj = commonUtils.executeURL(executeUrl, pcgUserPass, response);

										if (ccEmailObj != null && ccEmailObj.has("d")) {
											getCCEmails(ccEmailObj, ccEmails);

											String ccEmailMsg = "";
											if (!ccEmails.isEmpty() && ccEmails.size() > 0) {
												ccEmailMsg = ccEmails.toString();
											} else {
												ccEmailMsg = "CC Emails not maintained";
											}

											oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", ccEmailMsg, stepNo.getAndIncrement(), "CC Email Address", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

											executeUrl = ssmisODataUrl + "$metadata";

											JSONObject metadata = commonUtils.executeMetadatURL(executeUrl, ssmisUserPass, response);

											Map<String, String> salesSummaryMetadata = new HashMap<>();
											Map<String, String> invoiceMeta = new HashMap<>();
											Map<String, String> dataSubmissionMetadata = new HashMap<>();

											if (!metadata.has("Status")) {
												readMetaData(metadata, salesSummaryMetadata, invoiceMeta, dataSubmissionMetadata);

												Map<String, String> stockSummaryMetadata = new HashMap<>();
												executeUrl = ssgwODataUrl + "$metadata";
												metadata = commonUtils.executeMetadatURL(executeUrl, ssgwUserPass, response);
												if (!metadata.has("Status")) {
													readStockSummaryMetadat(metadata, stockSummaryMetadata);

													List<String> salesSummaryHeaders = new java.util.LinkedList<>();
													List<String> invoiceHeaders = new java.util.LinkedList<>();
													List<String> stockSummaryHeaders = new java.util.LinkedList<>();
													List<String> dataSubmissionHeaders = new java.util.LinkedList<>();
													// Setting Headers.
													setHeader(salesSummaryHeaders, invoiceHeaders, stockSummaryHeaders, dataSubmissionHeaders);

													for (int i = 0; i < asJsonArray.size(); i++) {
														JsonObject inputPayload = new JsonObject();
														if (asJsonArray.get(i).getAsJsonObject().has("SPGUID") && !asJsonArray.get(i).getAsJsonObject().get("SPGUID").isJsonNull()) {
															inputPayload.addProperty("SPGUID", asJsonArray.get(i).getAsJsonObject().get("SPGUID").getAsString());
														}

														if (asJsonArray.get(i).getAsJsonObject().has("AggregatorID") && !asJsonArray.get(i).getAsJsonObject().get("AggregatorID").isJsonNull()) {
															inputPayload.addProperty("AggregatorID", asJsonArray.get(i).getAsJsonObject().get("AggregatorID").getAsString());
														}

														if (asJsonArray.get(i).getAsJsonObject().has("SPNo") && !asJsonArray.get(i).getAsJsonObject().get("SPNo").isJsonNull()) {
															inputPayload.addProperty("SPNo", asJsonArray.get(i).getAsJsonObject().get("SPNo").getAsString());
														}

														if (asJsonArray.get(i).getAsJsonObject().has("EmailID") && !asJsonArray.get(i).getAsJsonObject().get("EmailID").isJsonNull()) {
															inputPayload.addProperty("EmailID", asJsonArray.get(i).getAsJsonObject().get("EmailID").getAsString());
														}

														if (asJsonArray.get(i).getAsJsonObject().has("MobileNo") && !asJsonArray.get(i).getAsJsonObject().get("MobileNo").isJsonNull()) {
															inputPayload.addProperty("MobileNo", asJsonArray.get(i).getAsJsonObject().get("MobileNo").getAsString());
														}

														if (asJsonArray.get(i).getAsJsonObject().has("LoginID") && !asJsonArray.get(i).getAsJsonObject().get("LoginID").isJsonNull()) {
															inputPayload.addProperty("LoginID", asJsonArray.get(i).getAsJsonObject().get("LoginID").getAsString());
														} else {
															inputPayload.addProperty("LoginID", "");
														}
														
														inputPayload.addProperty("CPILogID", cpiLogId);

														oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", inputPayload + "", stepNo.getAndIncrement(), "Input Payload", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

														// remove this line afetr testing
														//asJsonArray.get(i).getAsJsonObject().addProperty("EmailID", "vinodrajkj@wavelabs.ai");
														
														if (asJsonArray.get(i).getAsJsonObject().has("SPGUID") && !asJsonArray.get(i).getAsJsonObject().get("SPGUID").isJsonNull() && !asJsonArray.get(i).getAsJsonObject().get("SPGUID").getAsString().equalsIgnoreCase("") && asJsonArray.get(i).getAsJsonObject().has("EmailID") && !asJsonArray.get(i).getAsJsonObject().get("EmailID").isJsonNull() && !asJsonArray.get(i).getAsJsonObject().get("EmailID").getAsString().equalsIgnoreCase("")) {
															String spGUID = asJsonArray.get(i).getAsJsonObject().get("SPGUID").getAsString();
															String loginId = "";
															if (asJsonArray.get(i).getAsJsonObject().has("LoginID") && !asJsonArray.get(i).getAsJsonObject().get("LoginID").isJsonNull() && !asJsonArray.get(i).getAsJsonObject().get("LoginID").getAsString().equalsIgnoreCase("")) {
																loginId = asJsonArray.get(i).getAsJsonObject().get("LoginID").getAsString();
																executeUrl = pugOdataUrl + "UserAuthSet?$filter=LoginID%20eq%20%27" + loginId + "%27%20and%20AuthOrgValue%20eq%20%27" + "SS_VIEW_DB_PRICE" + "%27%20and%20AuthOrgTypeID%20eq%20%27" + "000011" + "%27";

																JsonObject userAuthsetObj = commonUtils.executeURL(executeUrl, pugUserPass, response);

																if (userAuthsetObj != null && !userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().isJsonNull() && userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
																	showAllFields = true;
																}
															}

															List<String> dmsDivFilter = filterDmsDivisionRecord(loginId, response);

															StringBuffer userAuthBuff = new StringBuffer();
															if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() == 1) {
																userAuthBuff.append("%20and%20DmsDivision_I%20eq%20%27").append(dmsDivFilter.get(0)).append("%27");
															} else if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() > 1) {
																userAuthBuff.append("%20and%20(");
																for (int j = 0; j < dmsDivFilter.size(); j++) {
																	userAuthBuff.append("DmsDivision_I%20eq%20%27" + dmsDivFilter.get(j) + "%27%20or%20");
																}
																userAuthBuff = new StringBuffer(userAuthBuff.substring(0, userAuthBuff.length() - 8));
																userAuthBuff.append(")");

															}

															salesSummaryEntity = "V_SSCPSP_SUM_SECSALES";
															invoiceEntity = "V_SSCPSP_SECSALES";
															cpSpStkItems = properties.getProperty("SSStockItemsSNosBySPCube");
															userSyncSubmissionReport = properties.getProperty("UserSyncSubmissionReport");

															Set<String> quarterlySet = new HashSet<>();
															JsonObject quarterObj = asJsonArray.get(i).getAsJsonObject();
															if (quarterObj.has("QuarterYear") && !quarterObj.get("QuarterYear").getAsJsonArray().isJsonNull() && quarterObj.get("QuarterYear").getAsJsonArray().size() > 0) {
																JsonArray inputQuarterArray = quarterObj.get("QuarterYear").getAsJsonArray();
																boolean sendEmail = true;
																String errorMsg = "";
																for (int j = 0; j < inputQuarterArray.size(); j++) {
																	if (!inputQuarterArray.get(j).getAsJsonObject().isJsonNull()) {
																		try {
																			final SXSSFWorkbook workbook = new SXSSFWorkbook();
																			workbook.setCompressTempFiles(true);
																			quarterlySet = new HashSet<>();
																			String quarter = inputQuarterArray.get(j).getAsJsonObject().get("ID").getAsString();
																			quarterlySet.add(quarter);
																			executeUrl = ssmisODataUrl + salesSummaryEntity + "?$select=SCPGuid,SCPName1,DmsDivision_I,ASPGROSSAMT,QUANTITYINBASEUOM" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarter + "%27";
																			if (userAuthBuff != null && userAuthBuff.length() > 0) {
																				executeUrl = executeUrl + userAuthBuff.toString();
																			}
																			JsonObject sumSecsalesObj = commonUtils.executeURL(executeUrl, ssmisUserPass, response, debug, "SSMISHANA");
																			JsonArray sumSecSalesArray = sumSecsalesObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
																			oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", sumSecSalesArray.size() + "", stepNo.getAndIncrement(), "Total Sales Summary Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "FinancialQuarterCode:" + quarter, debug);
																			createdSheetRes = createXlSheet(sumSecSalesArray, workbook, salesSummary, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, salesSummaryMetadata, salesSummaryHeaders, showAllFields);

																			if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																				executeUrl = ssmisODataUrl + invoiceEntity + "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DmsDivision_I,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,FromCPGuid,FromCPName,CountryID,InvoiceTypeDesc,InvoiceNo,InvoiceDate,SoldToName,SoldToBPID,ExternalSoldToCPName,ItemNo,MaterialNo,MaterialDesc,ExternalMaterialDesc,Batch,InvoiceQty,UOM_I,InvoiceQtyASPUOM,ASPUOM,ItemUnitPrice,GrossAmount,DiscountPerc,ItemTotalDiscAmount,AssessableValue,ItemTaxValue,ItemNetAmount,ItemNetAmountinRC,ASPGrossAmount" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarter + "%27%20and%20InvoiceTypeID%20ne%20%2706%27";
																				if (userAuthBuff != null && userAuthBuff.length() > 0) {
																					executeUrl = executeUrl + userAuthBuff.toString();
																				}
																				
																				executeUrl=executeUrl+"%20and%20(toupper(MaterialNo)%20ne%20%27NON_MDT%27%20and%20toupper(MaterialNo)%20ne%20%27DISCONTINUED%27)";

																				JsonObject invoiceObj = commonUtils.executeURL(executeUrl, ssmisUserPass, response, debug, "SSMISHANA");

																				JsonArray invoiceSheetArry = invoiceObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
																				oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", invoiceSheetArry.size() + "", stepNo.getAndIncrement(), "Total Invoices Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "FinancialQuarterCode:" + quarter, debug);
																				createdSheetRes = createXlSheet(invoiceSheetArry, workbook, invoices, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, invoiceMeta, invoiceHeaders, showAllFields);
																				if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {

																					executeUrl = ssmisODataUrl + userSyncSubmissionReport + "?$select=PartnerID,CPName,ERPSoftware,DaysLastStockSync,DaysLastSalesSync,DaysLastGRSync,LastInvDate" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";

																					userAuthBuff = new StringBuffer();
																					if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() == 1) {
																						userAuthBuff.append("%20and%20DMSDivisionID%20eq%20%27").append(dmsDivFilter.get(0)).append("%27");
																					} else if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() > 1) {
																						userAuthBuff.append("%20and%20(");
																						for (int k = 0; k < dmsDivFilter.size(); k++) {
																							userAuthBuff.append("DMSDivisionID%20eq%20%27" + dmsDivFilter.get(k) + "%27%20or%20");
																						}
																						userAuthBuff = new StringBuffer(userAuthBuff.substring(0, userAuthBuff.length() - 8));
																						userAuthBuff.append(")");
																					}
																					
																					if (userAuthBuff != null && userAuthBuff.length() > 0) {
																						executeUrl = executeUrl + userAuthBuff.toString();
																					}
																					JsonObject dataSubmissionObj = commonUtils.executeURL(executeUrl, ssmisUserPass, response, debug, "SSMISHANA");

																					if (loginId.equalsIgnoreCase("")) {
																						executeUrl = ssgwODataUrl + cpSpStkItems + "?$select=FirstName,LastName,ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,AsOnDate,FinancialYearCode,StockOwnerID,MaterialNo,ExternalMaterialDesc,StorageLocation,SNoBlockedQty,ExpiredInSourceUOM,SNOConsignment,UnrestrictedInSourceUOM,UnrestrictedInBaseUOM,SNOASPValue,StockOwnerName,CPOrg1,MaterialDesc,SNoUnRestrictedQty,UOM,BaseUOM,Currency,StockValue,ReportingCurrency,SNOStockValueInRC,ASPCurrency,ASPUnitPrice" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarter + "%27";
																					} else {
																						executeUrl = ssgwODataUrl + cpSpStkItems + "?$select=FirstName,LastName,ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,AsOnDate,FinancialYearCode,StockOwnerID,MaterialNo,ExternalMaterialDesc,StorageLocation,SNoBlockedQty,ExpiredInSourceUOM,SNOConsignment,UnrestrictedInSourceUOM,UnrestrictedInBaseUOM,SNOASPValue,StockOwnerName,CPOrg1,MaterialDesc,SNoUnRestrictedQty,UOM,BaseUOM,Currency,StockValue,ReportingCurrency,SNOStockValueInRC,ASPCurrency,ASPUnitPrice" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20LoginID%20eq%20%27" + loginId + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarter + "%27";
																					}
																					
																					userAuthBuff = new StringBuffer();
																					if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() == 1) {
																						userAuthBuff.append("%20and%20DMSDivision%20eq%20%27").append(dmsDivFilter.get(0)).append("%27");
																					} else if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() > 1) {
																						userAuthBuff.append("%20and%20(");
																						for (int k = 0; k < dmsDivFilter.size(); k++) {
																							userAuthBuff.append("DMSDivision%20eq%20%27" + dmsDivFilter.get(k) + "%27%20or%20");
																						}
																						userAuthBuff = new StringBuffer(userAuthBuff.substring(0, userAuthBuff.length() - 8));
																						userAuthBuff.append(")");

																					}

																					if (userAuthBuff != null && userAuthBuff.length() > 0) {
																						executeUrl = executeUrl + userAuthBuff.toString();
																					}
																					
																					executeUrl=executeUrl+"%20and%20(toupper(MaterialNo)%20ne%20%27NON_MDT%27%20and%20toupper(MaterialNo)%20ne%20%27DISCONTINUED%27)";
																					final JsonObject stockSummaryObj = commonUtils.executeURL(executeUrl, ssgwUserPass, response, debug, DestinationUtils.SSGWHANA);

																					final JsonArray stockSummaryArry = stockSummaryObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
																					oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", stockSummaryArry.size() + "", stepNo.getAndIncrement(), "Total Stock Summary Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "FinancialQuarterCode:" + quarter, debug);
																					createdSheetRes = createXlSheet(stockSummaryArry, workbook, stockSummary, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, stockSummaryMetadata, stockSummaryHeaders, showAllFields);
																					if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																						JsonArray dataSubmissingArr = dataSubmissionObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
																						oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", dataSubmissingArr.size() + "", stepNo.getAndIncrement(), "Total Data Submission Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "FinancialQuarterCode:" + quarter, debug);
																						createdSheetRes = createXlSheet(dataSubmissingArr, workbook, dataSubmission, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, dataSubmissionMetadata, dataSubmissionHeaders, showAllFields);
																						if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																							String email = asJsonArray.get(i).getAsJsonObject().get("EmailID").getAsString();
																							emailRes = sendEmail(quarterlySet, email, debug, response, ccEmails, asJsonArray.get(i).getAsJsonObject(), oDataLogs, stepNo, request, pcgoDataUrl, logID, spGUID, pcgUserPass, agrgtrID, cpiLogId, workbook);
																							if (emailRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																								oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Mail Send Successfully", stepNo.getAndIncrement(), spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "MailID:" + email, "", "FinancialQuarterCode:" + quarter, debug);
																								// oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, "", "S",agrgtrID,accountID, debug);
																							} else {
																								errorMsg = emailRes.get("Message").getAsString();
																								sendEmail = false;
																								oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Sending Email Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "MailID:" + email, spGUID, "FinancialQuarterCode:" + quarter, debug);

																							}

																						} else {
																							sendEmail = false;
																							// insert the Application Log.
																							errorMsg = createdSheetRes.get("Message").getAsString();

																							oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Data Submission", spGUID, "FinancialQuarterCode:" + quarter, debug);
																						}

																					} else {
																						sendEmail = false;
																						// insert the Application Log.
																						errorMsg = createdSheetRes.get("Message").getAsString();
																						oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Stock Summary", spGUID, "FinancialQuarterCode:" + quarter, debug);
																					}

																				} else {
																					sendEmail = false;
																					// insert the Application Log.
																					errorMsg = createdSheetRes.get("Message").getAsString();

																					oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Invoice Sheet", spGUID, "FinancialQuarterCode:" + quarter, debug);
																				}
																			} else {
																				sendEmail = false;
																				// insert the Application Logs

																				errorMsg = createdSheetRes.get("Message").getAsString();

																				oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Sales Summary", spGUID, "FinancialQuarterCode:" + quarter, debug);
																			}
																		} catch (Exception ex) {
																			sendEmail = false;
																			StringBuffer buffer = new StringBuffer();
																			buffer.append(ex.getClass().getCanonicalName() + "--->");
																			if (ex.getLocalizedMessage() != null) {
																				buffer.append(ex.getLocalizedMessage()).append("----->");
																			}
																			errorMsg = buffer.toString();
																			oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer + "", stepNo.getAndIncrement(), "Exception Occurred", pcgoDataUrl, pcgUserPass, agrgtrID, "", spGUID, "", debug);

																		}

																	}

																}

																if (sendEmail) {
																	oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, "", "S", agrgtrID, accountID, debug);
																} else {
																	oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);
																}

															} else {

																final SXSSFWorkbook workbook = new SXSSFWorkbook();
																workbook.setCompressTempFiles(true);

																JsonObject financialObj = getFinancialQuarterCode(debug, response);
																if (financialObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
																	JsonArray financeArray = financialObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
																	for (int j = 0; j < financeArray.size(); j++) {
																		JsonObject finObj = financeArray.get(j).getAsJsonObject();
																		if (!finObj.get("FinancialQuarterCode").isJsonNull() && !finObj.get("FinancialQuarterCode").getAsString().equalsIgnoreCase("")) {
																			quarterlySet.add(finObj.get("FinancialQuarterCode").getAsString());
																		}

																	}

																}

																if (quarterlySet.size() == 1) {
																	executeUrl = ssmisODataUrl + salesSummaryEntity + "?$select=SCPGuid,SCPName1,DmsDivision_I,ASPGROSSAMT,QUANTITYINBASEUOM" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarterlySet.iterator().next() + "%27";
																} else {
																	executeUrl = ssmisODataUrl + salesSummaryEntity + "?$select=SCPGuid,SCPName1,DmsDivision_I,ASPGROSSAMT,QUANTITYINBASEUOM" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20(";
																	StringBuffer financialQuaterCode = new StringBuffer();
																	Iterator<String> quartelyIterator = quarterlySet.iterator();
																	while (quartelyIterator.hasNext()) {
																		financialQuaterCode.append("FinancialQuarterCode%20eq%20%27" + quartelyIterator.next()).append("%27").append("%20or%20");
																	}
																	int lastIndexOf = financialQuaterCode.lastIndexOf("or");
																	String code = financialQuaterCode.substring(0, lastIndexOf - 3);
																	StringBuffer buffer = new StringBuffer(code);
																	buffer.append(")");
																	executeUrl = executeUrl + buffer.toString();
																}

																if (userAuthBuff != null && userAuthBuff.length() > 0) {
																	executeUrl = executeUrl + userAuthBuff.toString();
																}

																final JsonObject salesSummaryObj = commonUtils.executeODataURL(executeUrl, ssmisUserPass, response, debug);
																if (salesSummaryObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
																	final JsonArray salesSummaryArry = salesSummaryObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
																	oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", salesSummaryArry.size() + "", stepNo.getAndIncrement(), "Total Sales Summary Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
																	createdSheetRes = createXlSheet(salesSummaryArry, workbook, salesSummary, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, salesSummaryMetadata, salesSummaryHeaders, showAllFields);
																	if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																		if (quarterlySet.size() == 1) {
																			executeUrl = ssmisODataUrl + invoiceEntity + "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DmsDivision_I,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,FromCPGuid,FromCPName,CountryID,InvoiceTypeDesc,InvoiceNo,InvoiceDate,SoldToName,SoldToBPID,ExternalSoldToCPName,ItemNo,MaterialNo,MaterialDesc,ExternalMaterialDesc,Batch,InvoiceQty,UOM_I,InvoiceQtyASPUOM,ASPUOM,ItemUnitPrice,GrossAmount,DiscountPerc,ItemTotalDiscAmount,AssessableValue,ItemTaxValue,ItemNetAmount,ItemNetAmountinRC,ASPGrossAmount" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20FinancialQuarterCode%20eq%20%27" + quarterlySet.iterator().next() + "%27%20and%20InvoiceTypeID%20ne%20%2706%27";
																		} else {
																			executeUrl = ssmisODataUrl + invoiceEntity + "?$select=ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DmsDivision_I,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,FinancialYearCode,FromCPGuid,FromCPName,CountryID,InvoiceTypeDesc,InvoiceNo,InvoiceDate,SoldToName,SoldToBPID,ExternalSoldToCPName,ItemNo,MaterialNo,MaterialDesc,ExternalMaterialDesc,Batch,InvoiceQty,UOM_I,InvoiceQtyASPUOM,ASPUOM,ItemUnitPrice,GrossAmount,DiscountPerc,ItemTotalDiscAmount,AssessableValue,ItemTaxValue,ItemNetAmount,ItemNetAmountinRC,ASPGrossAmount" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20(";
																			StringBuffer financialQuaterCode = new StringBuffer();
																			Iterator<String> iterator = quarterlySet.iterator();
																			while (iterator.hasNext()) {
																				financialQuaterCode.append("FinancialQuarterCode%20eq%20%27" + iterator.next()).append("%27").append("%20or%20");
																			}

																			int lastIndexOf = financialQuaterCode.lastIndexOf("or");
																			String code = financialQuaterCode.substring(0, lastIndexOf - 3);
																			StringBuffer buffer = new StringBuffer(code);
																			buffer.append(")");
																			buffer.append("%20and%20InvoiceTypeID%20ne%20%2706%27");
																			executeUrl = executeUrl + buffer.toString();
																		}

																		if (userAuthBuff != null && userAuthBuff.length() > 0) {
																			executeUrl = executeUrl + userAuthBuff.toString();
																		}
																		//executeUrl=executeUrl+"&$top=1&$inlinecount=allpages";
																		executeUrl=executeUrl+"%20and%20(toupper(MaterialNo)%20ne%20%27NON_MDT%27%20and%20toupper(MaterialNo)%20ne%20%27DISCONTINUED%27)";
																		
																		String countQuery=executeUrl+"&$top=1&$inlinecount=allpages";
																		
																		final JsonObject invoiceObj = commonUtils.executeODataURL(countQuery, ssmisUserPass, response, debug);

																		if(invoiceObj.get("Status").getAsString().equalsIgnoreCase("000001")){
																		int totalRecords = invoiceObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("__count").getAsInt();
																		int batchSize=10000;
																		int loopCount=0;
																		if(totalRecords>0){
																		loopCount=totalRecords/batchSize;
																		int rem=totalRecords%batchSize;
																		if(rem!=0){
																			loopCount++;
																		}
																		}else{
																			loopCount++;
																		}
																		int skipCount=0;
																		List<String> queries=new ArrayList<>();
																		String filetrQuery=executeUrl;
																		for(int k=0;k<loopCount;k++){
																			skipCount=k*batchSize;
																			executeUrl=filetrQuery+"&$skip=" + skipCount + "&$top=" + batchSize;
																			queries.add(executeUrl);
																		}
																		
																		final JsonObject invoiceRecords= executeBatchCalls(queries,ssmisUserPass,ssmisODataUrl);
																		
																		if (invoiceRecords.get("Status").getAsString().equalsIgnoreCase("000001")){
																			//invoiceRecords.get
																		//final JsonArray invoiceArry = invoiceObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
																				final JsonArray invoiceArry = invoiceRecords.get("Message").getAsJsonArray();
																		oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", invoiceArry.size() + "", stepNo.getAndIncrement(), "Total Invoices Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
																		createdSheetRes = createXlSheet(invoiceArry, workbook, invoices, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, invoiceMeta, invoiceHeaders, showAllFields);
																		if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																			executeUrl = ssmisODataUrl + userSyncSubmissionReport + "?$select=PartnerID,CPName,ERPSoftware,DaysLastStockSync,DaysLastSalesSync,DaysLastGRSync,LastInvDate" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
																				userAuthBuff = new StringBuffer();
																				if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() == 1) {
																					userAuthBuff.append("%20and%20DMSDivisionID%20eq%20%27").append(dmsDivFilter.get(0)).append("%27");
																				} else if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() > 1) {
																					userAuthBuff.append("%20and%20(");
																					for (int j = 0; j < dmsDivFilter.size(); j++) {
																						userAuthBuff.append("DMSDivisionID%20eq%20%27" + dmsDivFilter.get(j) + "%27%20or%20");
																					}
																					userAuthBuff = new StringBuffer(userAuthBuff.substring(0, userAuthBuff.length() - 8));
																					userAuthBuff.append(")");
																				}
																				
																				if (userAuthBuff != null && userAuthBuff.length() > 0) {
																					executeUrl = executeUrl + userAuthBuff.toString();
																				}
								
																			final JsonObject dataSubmissionObj = commonUtils.executeODataURL(executeUrl, ssmisUserPass, response, debug);

																			if(dataSubmissionObj.get("Status").getAsString().equalsIgnoreCase("000001")){
																			if (loginId.equalsIgnoreCase("")) {
																				executeUrl = ssgwODataUrl + cpSpStkItems + "?$select=FirstName,LastName,ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,AsOnDate,FinancialYearCode,StockOwnerID,MaterialNo,ExternalMaterialDesc,StorageLocation,SNoBlockedQty,ExpiredInSourceUOM,SNOConsignment,UnrestrictedInSourceUOM,UnrestrictedInBaseUOM,SNOASPValue,StockOwnerName,CPOrg1,MaterialDesc,SNoUnRestrictedQty,UOM,BaseUOM,Currency,StockValue,ReportingCurrency,SNOStockValueInRC,ASPCurrency,ASPUnitPrice" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27";
																			} else {
																				executeUrl = ssgwODataUrl + cpSpStkItems + "?$select=FirstName,LastName,ProductCategoryDesc,ProductGroupDesc,DMSOrg2,DMSDivision,DMSOrg3,DMSOrg1,FinancialWeekCode,FinancialQuarterCode,AsOnDate,FinancialYearCode,StockOwnerID,MaterialNo,ExternalMaterialDesc,StorageLocation,SNoBlockedQty,ExpiredInSourceUOM,SNOConsignment,UnrestrictedInSourceUOM,UnrestrictedInBaseUOM,SNOASPValue,StockOwnerName,CPOrg1,MaterialDesc,SNoUnRestrictedQty,UOM,BaseUOM,Currency,StockValue,ReportingCurrency,SNOStockValueInRC,ASPCurrency,ASPUnitPrice" + "&$filter=SPGUID%20eq%20%27" + spGUID + "%27%20and%20AggregatorID%20eq%20%27" + agrgtrID + "%27%20and%20LoginID%20eq%20%27" + loginId + "%27";
																			}

																			if (quarterlySet.size() == 1) {
																				executeUrl = executeUrl + "%20and%20FinancialQuarterCode%20eq%20%27" + quarterlySet.iterator().next() + "%27";
																			} else {
																				StringBuffer financialQuaterCode = new StringBuffer("%20and%20(");
																				Iterator<String> iterator = quarterlySet.iterator();
																				while (iterator.hasNext()) {
																					financialQuaterCode.append("FinancialQuarterCode%20eq%20%27" + iterator.next()).append("%27").append("%20or%20");
																				}

																				int lastIndexOf = financialQuaterCode.lastIndexOf("or");
																				String code = financialQuaterCode.substring(0, lastIndexOf - 3);
																				StringBuffer buffer = new StringBuffer(code);
																				buffer.append(")");
																				executeUrl = executeUrl + buffer.toString();
																			}

																			userAuthBuff = new StringBuffer();
																			if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() == 1) {
																				userAuthBuff.append("%20and%20DMSDivision%20eq%20%27").append(dmsDivFilter.get(0)).append("%27");
																			} else if (!dmsDivFilter.isEmpty() && dmsDivFilter.size() > 1) {
																				userAuthBuff.append("%20and%20(");
																				for (int j = 0; j < dmsDivFilter.size(); j++) {
																					userAuthBuff.append("DMSDivision%20eq%20%27" + dmsDivFilter.get(j) + "%27%20or%20");
																				}
																				userAuthBuff = new StringBuffer(userAuthBuff.substring(0, userAuthBuff.length() - 8));
																				userAuthBuff.append(")");

																			}
																			
																			if (userAuthBuff != null && userAuthBuff.length() > 0) {
																				executeUrl = executeUrl + userAuthBuff.toString();
																			}
																			
																			executeUrl=executeUrl+"%20and%20(toupper(MaterialNo)%20ne%20%27NON_MDT%27%20and%20toupper(MaterialNo)%20ne%20%27DISCONTINUED%27)";
																			
																					String stockSummarycount=executeUrl+"&$top=1&$inlinecount=allpages";
																			
																					final JsonObject stockSummaryObj = commonUtils.executeODataURL(stockSummarycount, ssgwUserPass, response, debug);
																						if (stockSummaryObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
																							final int stockCount = stockSummaryObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("__count").getAsInt();
																							batchSize = 10000;
																							int stockLoop=0;
																							if(stockCount>0){
																							stockLoop = stockCount / batchSize;
																							int stockRem = stockCount % batchSize;
																							if (stockRem != 0) {
																								stockLoop++;
																							}
																							}else{
																								stockLoop++;	
																							}

																							int stockSkipCount = 0;
																							List<String> stockSummaryquires = new ArrayList<>();
																							String filetrQry=executeUrl;
																							for (int k = 0; k < stockLoop; k++) {
																								stockSkipCount = k * batchSize;
																								executeUrl = filetrQry + "&$skip=" + stockSkipCount + "&$top=" + batchSize;
																								stockSummaryquires.add(executeUrl);
																							}

																							final JsonObject stockObj = executeBatchCalls(stockSummaryquires, ssgwUserPass, ssgwODataUrl);

																							if (stockObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
																								final JsonArray stockSummaryArry=stockObj.get("Message").getAsJsonArray();

																								//final JsonArray stockSummaryArry = stockSummaryObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
																								oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", stockSummaryArry.size() + "", stepNo.getAndIncrement(), "Total Stock Summary Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
																								createdSheetRes = createXlSheet(stockSummaryArry, workbook, stockSummary, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, stockSummaryMetadata, stockSummaryHeaders, showAllFields);
																								if (createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																									final JsonArray dataSubmissionArry = dataSubmissionObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
																									oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", dataSubmissionArry.size() + "", stepNo.getAndIncrement(), "Total Data Submission Records for the SPGUID:" + spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
																									createdSheetRes = createXlSheet(dataSubmissionArry, workbook, dataSubmission, debug, response, request, logID, stepNo.get(), pcgoDataUrl, pcgUserPass, agrgtrID, oDataLogs, dataSubmissionMetadata, dataSubmissionHeaders, showAllFields);
																									if (createdSheetRes.has("Status") && createdSheetRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																										final String email = asJsonArray.get(i).getAsJsonObject().get("EmailID").getAsString();

																										emailRes = sendEmail(quarterlySet, email, debug, response, ccEmails, asJsonArray.get(i).getAsJsonObject(), oDataLogs, stepNo, request, pcgoDataUrl, logID, spGUID, pcgUserPass, agrgtrID, cpiLogId, workbook);
																										if (emailRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
																											oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "Mail Send Successfully", stepNo.getAndIncrement(), spGUID, pcgoDataUrl, pcgUserPass, agrgtrID, "MailID:" + email, "", "", debug);
																											oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, "", "S", agrgtrID, accountID, debug);
																										} else {
																											String errorMsg = emailRes.get("Message").getAsString();
																											oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg, stepNo.getAndIncrement(), "Sending Email Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "MailID:" + email, spGUID, "", debug);
																											oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);

																										}
																									} else {
																										// insert the Application Log.
																										String errorMsg = createdSheetRes.get("Message").getAsString();
																										oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg, stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Data Submission", spGUID, "", debug);
																										oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);
																									}
																								} else {

																									String errorMsg = createdSheetRes.get("Message").getAsString();
																									oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg, stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Stock Summary", spGUID, "", debug);
																									oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);

																								}
																							} else {
																								// fetching records Stock Summary Failed.
																								String erorMsg = stockObj.get("Message").getAsString();

																								oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", stockSummaryObj + "", stepNo.getAndIncrement(), "Fetching records from Stock Summary Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Stock Summary", spGUID, "", debug);
																								oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																							}
																						}else{
																							
																							String erorMsg = stockSummaryObj.get("Message").getAsString();

																							oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", stockSummaryObj + "", stepNo.getAndIncrement(), "Fetching records from Stock Summary Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Stock Summary", spGUID, "", debug);
																							oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																							
																						}
																			}else{
																				// fetching records from Data Submission Table Failed.
																				String erorMsg=dataSubmissionObj.get("Message").getAsString();
																				
																				oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", dataSubmissionObj+"", stepNo.getAndIncrement(), "Fetching records from Data Submission Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Data Submission", spGUID, "", debug);
																				oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																			}
																		} else {
																			// insert the application Log for creating a excel sheet failure.
																			String errorMsg = createdSheetRes.get("Message").getAsString();
																			oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg, stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Invoice Sheet", spGUID, "", debug);
																			oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);
																		}
																		}else{
																			// fetching records from Table. Failed
																			String erorMsg=invoiceRecords.get("Message").getAsString();
																			oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", invoiceObj+"", stepNo.getAndIncrement(), "Fetching records from Invoice Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Invoice Sheet", spGUID, "", debug);
																			oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																		}
																		}else{
																			// fetching records from Table. Failed
																			String erorMsg=invoiceObj.get("Message").getAsString();
																			oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", invoiceObj+"", stepNo.getAndIncrement(), "Fetching records from Invoice Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Invoice Sheet", spGUID, "", debug);
																			oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																		}
																	} else {
																		// insert the Application Logs
																		String errorMsg = createdSheetRes.get("Message").getAsString();
																		oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", errorMsg + "", stepNo.getAndIncrement(), "Excel Sheet Creation Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Sales Summary", spGUID, "", debug);
																		oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, errorMsg, "E", agrgtrID, accountID, debug);

																	}
																}else{
																	// Fetching records from Sales Summary Failed
																	String erorMsg=salesSummaryObj.get("Message").getAsString();
																	oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", salesSummaryObj+"", stepNo.getAndIncrement(), "Fetching records from Sales Summary Sheet Failed", pcgoDataUrl, pcgUserPass, agrgtrID, "Sheet Name:Sales Summary", spGUID, "", debug);
																	oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, erorMsg, "E", agrgtrID, accountID, debug);
																}
															}

														} else {
															JsonObject res = new JsonObject();
															res.addProperty("Message", "Input Payload doesn't contains a Sender email address");
															res.addProperty("Status", "000002");
															res.addProperty("ErrorCode", "J002");
															oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", res + "", stepNo.getAndIncrement(), "Input Payload does not contains an Email Id or SPGUID Field", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
														}

													}
													oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", emailRes + "", stepNo.getAndIncrement(), "Process Completed", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
												} else {
													oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", metadata + "Reading MetaData Failed", stepNo.getAndIncrement(), "Reading Metadata Failed for the destination:", pcgoDataUrl, pcgUserPass, agrgtrID, "SSGW", "", "", debug);

												}
											} else {
												oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", metadata + "Reading MetaData Failed", stepNo.getAndIncrement(), "Reading Metadata Failed for the destination:", pcgoDataUrl, pcgUserPass, agrgtrID, "SSMIS", "", "", debug);

											}
										} else {
											oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", ccEmailObj + "Fetching CCEmails Failed", stepNo.getAndIncrement(), "Exception Occurred While Fetching CC Emails:", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
										}
									} else {
										oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, "Input Payload does not contains SP Data", "E", agrgtrID, accountID, debug);

										oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Input Payload does not contains SP Data", stepNo.getAndIncrement(), "", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);
									}
								} else {
									oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, "Input Payload does not contains AggregatorID", "E", agrgtrID, accountID, debug);

									oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", "Input Payload does not contains AggregatorID", stepNo.getAndIncrement(), "", pcgoDataUrl, pcgUserPass, agrgtrID, "", "", "", debug);

								}
							} catch (JsonParseException ex) {
								try {
									StackTraceElement element[] = ex.getStackTrace();
									StringBuffer buffer = new StringBuffer();
									for (int i = 0; i < element.length; i++) {
										buffer.append(element[i]);
									}

									String localizedMsg = "";
									if (ex.getLocalizedMessage() != null) {
										localizedMsg = ex.getLocalizedMessage();
									}

									oDataLogs.insertSalesReportMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo.getAndIncrement(), "Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID, localizedMsg, ex.getClass().getCanonicalName(), "", debug);

									responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
									responseObj.addProperty("Message", ex.getClass().getCanonicalName() + buffer.toString());
									responseObj.addProperty("Status", "000002");
									responseObj.addProperty("ErrorCode", "J002");
									oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, buffer.toString(), "E", agrgtrID, accountID, debug);
								} catch (IOException ioex) {
									throw new RuntimeException(ioex);
								} catch (Exception ioex) {
									throw new RuntimeException(ioex);
								}
							} catch (Exception ex) {
								try {
									StackTraceElement element[] = ex.getStackTrace();
									StringBuffer buffer = new StringBuffer();
									for (int i = 0; i < element.length; i++) {
										buffer.append(element[i]);
									}
									String localizedMsg = "";
									if (ex.getLocalizedMessage() != null) {
										localizedMsg = ex.getLocalizedMessage();
									}
									oDataLogs.insertSalesReportMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo.getAndIncrement(), "Exception Occurred While Parsing Input Paylaod", pcgoDataUrl, pcgUserPass, agrgtrID, localizedMsg, ex.getClass().getCanonicalName(), "", debug);
									responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
									responseObj.addProperty("Message", ex.getClass().getCanonicalName() + buffer.toString());
									responseObj.addProperty("Status", "000002");
									responseObj.addProperty("ErrorCode", "J002");
									oDataLogs.updateCpiApplicationLog(request, response, pcgoDataUrl, pcgUserPass, cpiLogId, logID, buffer.toString(), "E", agrgtrID, accountID, debug);
								} catch (Exception e) {
									throw new RuntimeException(e);
								}

							}

						}

						private JsonObject executeBatchCalls(List<String> queries, String ssmisUserPass, String ssmisODataUrl) {
							JsonObject resObj = new JsonObject();
							final String boundary = "batch_" + UUID.randomUUID().toString();
							try {
								Map<String, String> changeSetHeaders = new HashMap<String, String>();
								changeSetHeaders.put("Content-Type", "application/json");
								changeSetHeaders.put("Accept", "application/json");
								changeSetHeaders.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(ssmisUserPass.getBytes()));
								List<BatchPart> batchParts = new ArrayList<BatchPart>();
								//BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
								
								for (int i = 0; i < queries.size(); i++) {
									String executeUrl =queries.get(i);
									BatchQueryPart query = BatchQueryPart.method("GET").uri(executeUrl).headers(changeSetHeaders).build();
									batchParts.add(query);
								}
								//batchParts.add(changeSet);
								InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
								String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
								final HttpPost post = new HttpPost(URI.create(ssmisODataUrl + "$batch"));
								post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
								post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(ssmisUserPass.getBytes()));
								HttpEntity entity = new StringEntity(payload);
								post.setEntity(entity);
								HttpResponse batchResponse = HttpClientBuilder.create().build().execute(post);
								InputStream responseBody = batchResponse.getEntity().getContent();
								String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
								String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
								List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
								boolean executeSuccess= true;
								JsonArray recArry=new JsonArray();
								JsonParser parser=new JsonParser();
								for (BatchSingleResponse singleRes : responses) {
									String statusCode = singleRes.getStatusCode();
									if (statusCode.equalsIgnoreCase("200")) {
										String records = singleRes.getBody();
										JsonObject obj =(JsonObject)parser.parse(records);
										if(obj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
											JsonArray asJsonArray = obj.get("d").getAsJsonObject().get("results").getAsJsonArray();
											recArry.addAll(asJsonArray);
										}
									}else{
										executeSuccess = false;
										resObj.addProperty("Message", "Fetching records from Table Failed" + singleRes.getBody());
										break;
									}
								}
								if (executeSuccess) {
									resObj.add("Message", recArry);
									resObj.addProperty("ErrorCode", "");
									resObj.addProperty("Status", "000001");
								} else {
									resObj.addProperty("ErrorCode", "J002");
									resObj.addProperty("Status", "000002");
								}
							} catch (Exception ex) {
								StackTraceElement[] stackTrace = ex.getStackTrace();
								StringBuffer buffer = new StringBuffer();
								for (int i = 0; i < stackTrace.length; i++) {
									buffer.append(stackTrace[i]);
								}
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Message", ex.getLocalizedMessage());
								resObj.addProperty("ExceptionTrace", buffer.toString());

							}
							return resObj;
						}

						private void setHeader(List<String> salesSummaryHeaders, List<String> invoiceHeaders, List<String> stockSummaryHeaders, List<String> dataSubmissionHeaders) {
							// Slaes Summary Headers
							salesSummaryHeaders.add("Distributor Code");
							salesSummaryHeaders.add("Distributor Name");
							salesSummaryHeaders.add("Operating Unit");
							salesSummaryHeaders.add("ASP Gross Amount");
							salesSummaryHeaders.add("Quantity in ASP UOM");
							// Invoice Headers
							invoiceHeaders.add("Category");
							invoiceHeaders.add("Product Group");
							invoiceHeaders.add("Portfolio");
							invoiceHeaders.add("Operating Unit");
							invoiceHeaders.add("Business Unit");
							invoiceHeaders.add("Product Family");
							invoiceHeaders.add("Week");
							invoiceHeaders.add("Quarter");
							invoiceHeaders.add("Fiscal Year");
							invoiceHeaders.add("Distributor Code");
							invoiceHeaders.add("Distributor Name");
							invoiceHeaders.add("Country");
							invoiceHeaders.add("Invoice Type Desc");
							invoiceHeaders.add("Invoice No");
							invoiceHeaders.add("Invoice Date");
							invoiceHeaders.add("Sold To Party Name");
							invoiceHeaders.add("Sold To Party Code");
							invoiceHeaders.add("Source Sold To Party Name");
							invoiceHeaders.add("Item No");
							invoiceHeaders.add("Material No");
							invoiceHeaders.add("Material Description");
							invoiceHeaders.add("Source Material Description");
							invoiceHeaders.add("Batch");
							invoiceHeaders.add("DB Quantity");
							invoiceHeaders.add("DB UOM");

							invoiceHeaders.add("Quantity in ASP UOM");
							invoiceHeaders.add("ASP UOM");

							invoiceHeaders.add("Unit Price");
							invoiceHeaders.add("Gross Amount");
							invoiceHeaders.add("Discount %");
							invoiceHeaders.add("Discount Amount");
							invoiceHeaders.add("Taxable Amount");
							invoiceHeaders.add("Tax Amount");
							invoiceHeaders.add("Net Amount");
							invoiceHeaders.add("Net Amount in USD");
							invoiceHeaders.add("Gross Value");

							// Stock Summary Headers
							stockSummaryHeaders.add("Category");
							stockSummaryHeaders.add("Product Group");
							stockSummaryHeaders.add("Portfolio");
							stockSummaryHeaders.add("Operating Unit");
							stockSummaryHeaders.add("Business Unit");
							stockSummaryHeaders.add("Product Family");

							stockSummaryHeaders.add("Week");
							stockSummaryHeaders.add("Quarter");
							stockSummaryHeaders.add("Fiscal Year");

							stockSummaryHeaders.add("Distributor Code");
							stockSummaryHeaders.add("Distributor Name");
							stockSummaryHeaders.add("Country");
							stockSummaryHeaders.add("Material No");
							stockSummaryHeaders.add("Material Description");
							stockSummaryHeaders.add("Source Material Description");
							stockSummaryHeaders.add("Storage Location");
							stockSummaryHeaders.add("As On Date");
							stockSummaryHeaders.add("Blocked Qty");
							stockSummaryHeaders.add("Expired Qty");
							stockSummaryHeaders.add("Consignment Qty");
							stockSummaryHeaders.add("Saleable Qty");
							stockSummaryHeaders.add("DB UOM");
							stockSummaryHeaders.add("Saleable Qty in ASP UOM");
							stockSummaryHeaders.add("ASP UOM");
							stockSummaryHeaders.add("Stock Value");
							stockSummaryHeaders.add("Stock Value in USD");
							stockSummaryHeaders.add("Gross Value");
							// Data Submission Sheet Headers
							dataSubmissionHeaders.add("Distributor Code");
							dataSubmissionHeaders.add("Distributor Name");
							dataSubmissionHeaders.add("ERP Software");
							dataSubmissionHeaders.add("Days Last Stock Sync");
							dataSubmissionHeaders.add("Days Last Sales Sync");
							dataSubmissionHeaders.add("Days Last GR Sync");
							dataSubmissionHeaders.add("Latest Invoice Date");
						}

						private void readStockSummaryMetadat(JSONObject metadata, Map<String, String> stockSummaryMetadata) throws Exception {
							try {
								JSONArray entityArray = metadata.getJSONObject("edmx:Edmx").getJSONObject("edmx:DataServices").getJSONObject("Schema").getJSONArray("EntityType");
								for (int j = 0; j < entityArray.length(); j++) {
									JSONObject entity = entityArray.getJSONObject(j);
									if (entity.getString("Name").equalsIgnoreCase("SSStockItemsSNosBySPCubeType")) {
										JSONArray propertyArray = entity.getJSONArray("Property");
										for (int k = 0; k < propertyArray.length(); k++) {
											JSONObject jsonObject = propertyArray.getJSONObject(k);
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

							} catch (Exception ex) {
								throw ex;

							}

						}

						private void readMetaData(JSONObject metadata, Map<String, String> salesSummaryMetadata, Map<String, String> invoiceMeta, Map<String, String> dataSubmissionMetadata) throws Exception {
							try {
								JSONArray entityArray = metadata.getJSONObject("edmx:Edmx").getJSONObject("edmx:DataServices").getJSONObject("Schema").getJSONArray("EntityType");

								for (int i = 0; i < entityArray.length(); i++) {
									JSONObject entity = entityArray.getJSONObject(i);
									if (entity.getString("Name").equalsIgnoreCase("V_SSCPSP_SUM_SECSALESType")) {
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

									if (entity.getString("Name").equalsIgnoreCase("V_SSCPSP_SECSALESType")) {
										JSONArray propertyArray = entity.getJSONArray("Property");
										for (int k = 0; k < propertyArray.length(); k++) {
											JSONObject jsonObject = propertyArray.getJSONObject(k);
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
											else if (fieldName.equalsIgnoreCase("ProductCategoryDesc"))
												invoiceMeta.put(fieldName, dataType);
											else if (fieldName.equalsIgnoreCase("ProductGroupDesc"))
												invoiceMeta.put(fieldName, dataType);
											else if (fieldName.equalsIgnoreCase("FinancialWeekCode"))
												invoiceMeta.put(fieldName, dataType);
											else if (fieldName.equalsIgnoreCase("FinancialQuarterCode"))
												invoiceMeta.put(fieldName, dataType);
											else if (fieldName.equalsIgnoreCase("FinancialYearCode"))
												invoiceMeta.put(fieldName, dataType);
										}

									}

									if (entity.getString("Name").equalsIgnoreCase("UserSyncSubmissionReportType")) {
										JSONArray propertyArray = entity.getJSONArray("Property");
										for (int k = 0; k < propertyArray.length(); k++) {
											JSONObject jsonObject = propertyArray.getJSONObject(k);
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

							} catch (Exception ex) {
								throw ex;
							}

						}

						private void getCCEmails(JsonObject ccEmailObj, Set<String> ccEmails) throws Exception {
							try {
								JsonArray ccemailArray = ccEmailObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
								if (ccemailArray != null && !ccemailArray.isJsonNull() && ccemailArray.size() > 0) {
									for (int i = 0; i < ccemailArray.size(); i++) {
										JsonObject ccEmailObjItem = ccemailArray.get(i).getAsJsonObject();
										if (!ccEmailObjItem.get("Types").isJsonNull() && !ccEmailObjItem.get("Types").getAsString().equalsIgnoreCase("")) {
											String mailAddress = ccEmailObjItem.get("Types").getAsString();
											if (mailAddress.startsWith("WSRAUTML")) {
												if (!ccEmailObjItem.get("TypeValue").isJsonNull() && !ccEmailObjItem.get("TypeValue").getAsString().equalsIgnoreCase("")) {
													ccEmails.add(ccEmailObjItem.get("TypeValue").getAsString().trim());
												}
											}
										}
									}
								}
							} catch (Exception ex) {
								throw ex;
							}

						}
					});
					
					asyncContext.complete();
					
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", "Input Payload Received");
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);
				} else {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Message", "LogID missing in the input Payload");
					resObj.addProperty("Status", "000001");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);
				}
			} else {
				// print input payload is empty
				JsonObject responseObj = new JsonObject();
				responseObj.addProperty("Message", "Empty Input Payload Received");
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			JsonObject responseObj = new JsonObject();
			responseObj.addProperty("Exception", ex.getClass().getCanonicalName());
			responseObj.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);

		}
	}

	public JsonObject createXlSheet(JsonArray array, SXSSFWorkbook workbook, String sheetName, boolean debug, HttpServletResponse response, HttpServletRequest request, String logID, int stepNo, String oDataUrl, String userPass, String agrgtrID, ODataLogs oDataLogs, Map<String, String> metadata, List<String> headers, boolean showFields) throws Exception {
		JsonObject resultjsonObj = new JsonObject();
		try {
			final SXSSFSheet sheet1 = (SXSSFSheet) workbook.createSheet(sheetName);
			sheet1.setRandomAccessWindowSize(100);
			sheet1.setColumnWidth(0, 6000);
			sheet1.setColumnWidth(1, 4000);
			Row header = sheet1.createRow(0);
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
						if (salesSummary.has("QUANTITYINBASEUOM") && !salesSummary.get("QUANTITYINBASEUOM").isJsonNull()) {
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
					rowNum=rowNum+1;
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell cellGrossAmt = total.createCell(3);
					cellGrossAmt.setCellValue(grandTotal);
					Cell totalQty = total.createCell(4);
					totalQty.setCellValue(totalQuantity);
				} else if (sheetName.equalsIgnoreCase("Invoices")) {
					Double totalGrossAmt = new Double(0);
					Double totalDisccountAmt = new Double(0);
					Double totalTaxableAmt = new Double(0);
					Double totalTaxAmt = new Double(0);
					Double totalNetAmt = new Double(0);
					Double totalNetAmtInUsd = new Double(0);
					Double totalGrossValue = new Double(0);

					headers.forEach(cellName -> {
						if (showFields) {
							Cell createCell = header.createCell(keyNum.getAndIncrement());
							createCell.setCellValue(cellName);
						} else {
							if (!(cellName.equalsIgnoreCase("Gross Amount") || cellName.equalsIgnoreCase("Discount %") || cellName.equalsIgnoreCase("Discount Amount") || cellName.equalsIgnoreCase("Taxable Amount") || cellName.equalsIgnoreCase("Tax Amount") || cellName.equalsIgnoreCase("Net Amount") || cellName.equalsIgnoreCase("Net Amount in USD") || cellName.equalsIgnoreCase("Unit Price"))) {
								Cell createCell = header.createCell(keyNum.getAndIncrement());
								createCell.setCellValue(cellName);
							}
						}
					});
					int rowNum = 0;

					for (int i = 0; i < array.size(); i++) {
						JsonObject invoiceObj = array.get(i).getAsJsonObject();
							rowNum++;
							AtomicInteger cellNum = new AtomicInteger(0);
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
								cell = row.createCell(cellNum.getAndIncrement());
								String dataType = metadata.get("DMSOrg2");
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
							if (invoiceObj.has("SoldToName") && !invoiceObj.get("SoldToName").isJsonNull()) {
								String dataType = metadata.get("SoldToName");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String")) {
									String soldName = invoiceObj.get("SoldToName").getAsString();
									/*
									 * byte[] bytes = soldName.getBytes(Charset.defaultCharset()); soldName=new String(bytes,Charset.defaultCharset());
									 */
									cell.setCellValue(soldName);
									// cell.setCellValue(soldName);
								} else if (dataType.equalsIgnoreCase("Edm.Decimal"))
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
							if (invoiceObj.has("ExternalSoldToCPName") && !invoiceObj.get("ExternalSoldToCPName").isJsonNull()) {
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
							if (invoiceObj.has("ExternalMaterialDesc") && !invoiceObj.get("ExternalMaterialDesc").isJsonNull()) {
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
							// 21
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
							// 22
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

							if (showFields) {
								if (invoiceObj.has("ItemUnitPrice") && !invoiceObj.get("ItemUnitPrice").isJsonNull()) {
									String dataType = metadata.get("ItemUnitPrice");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
										cell.setCellValue(invoiceObj.get("ItemUnitPrice").getAsDouble());
									} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
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
							// 23
							if (showFields) {
								if (invoiceObj.has("GrossAmount") && !invoiceObj.get("GrossAmount").isJsonNull()) {
									String dataType = metadata.get("GrossAmount");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(invoiceObj.get("GrossAmount").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
										cell.setCellValue(invoiceObj.get("GrossAmount").getAsDouble());
										totalGrossAmt += invoiceObj.get("GrossAmount").getAsDouble();
									} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
										Date date = convertLongToDate(invoiceObj.get("GrossAmount").getAsString());
										style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
										cell.setCellValue(date);
										cell.setCellStyle(style);
									} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
										cell.setCellValue(invoiceObj.get("GrossAmount").getAsInt());
										totalGrossAmt += invoiceObj.get("GrossAmount").getAsInt();
									}
								} else {
									cell = row.createCell(cellNum.getAndIncrement());
									cell.setCellValue("");
								}
							}
							// 24
							if (showFields) {
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
							// 25
							if (showFields) {
								if (invoiceObj.has("ItemTotalDiscAmount") && !invoiceObj.get("ItemTotalDiscAmount").isJsonNull()) {
									String dataType = metadata.get("ItemTotalDiscAmount");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
										cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsDouble());
										totalDisccountAmt += invoiceObj.get("ItemTotalDiscAmount").getAsDouble();
									} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
										Date date = convertLongToDate(invoiceObj.get("ItemTotalDiscAmount").getAsString());
										style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
										cell.setCellValue(date);
										cell.setCellStyle(style);
									} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
										cell.setCellValue(invoiceObj.get("ItemTotalDiscAmount").getAsInt());
										totalDisccountAmt += invoiceObj.get("ItemTotalDiscAmount").getAsInt();
									}
								} else {
									cell = row.createCell(cellNum.getAndIncrement());
									cell.setCellValue("");
								}
							}
							// 26
							if (showFields) {
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
							// 27
							if (showFields) {
								if (invoiceObj.has("ItemTaxValue") && !invoiceObj.get("ItemTaxValue").isJsonNull()) {
									String dataType = metadata.get("ItemTaxValue");
									cell = row.createCell(cellNum.getAndIncrement());
									if (dataType.equalsIgnoreCase("Edm.String"))
										cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsString());
									else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
										cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsDouble());
										totalTaxAmt += invoiceObj.get("ItemTaxValue").getAsDouble();
									} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
										Date date = convertLongToDate(invoiceObj.get("ItemTaxValue").getAsString());
										style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
										cell.setCellValue(date);
										cell.setCellStyle(style);
									} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
										cell.setCellValue(invoiceObj.get("ItemTaxValue").getAsInt());
										totalTaxAmt += invoiceObj.get("ItemTaxValue").getAsInt();
									}
								} else {
									cell = row.createCell(cellNum.getAndIncrement());
									cell.setCellValue("");
								}
							}
							// 28
							if (showFields) {
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
							// 29
							if (showFields) {
								if (invoiceObj.has("ItemNetAmountinRC") && !invoiceObj.get("ItemNetAmountinRC").isJsonNull()) {
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
							// 30
							if (invoiceObj.has("ASPGrossAmount") && !invoiceObj.get("ASPGrossAmount").isJsonNull()) {
								String dataType = metadata.get("ASPGrossAmount");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsDouble());
									totalGrossValue += invoiceObj.get("ASPGrossAmount").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(invoiceObj.get("ASPGrossAmount").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(invoiceObj.get("ASPGrossAmount").getAsInt());
									totalGrossValue += invoiceObj.get("ASPGrossAmount").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

						
					}

					// Printing gross amount
					rowNum=rowNum+1;
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell totalGrossValueCell = null;
					if (showFields) {
						Cell toatlGrossamt = total.createCell(28);
						toatlGrossamt.setCellValue(totalGrossAmt);
						Cell toatlDiscountCell = total.createCell(30);
						toatlDiscountCell.setCellValue(totalDisccountAmt);
						Cell totalTaxableAmtCell = total.createCell(31);
						totalTaxableAmtCell.setCellValue(totalTaxableAmt);
						Cell totalTaxAmtCell = total.createCell(32);
						totalTaxAmtCell.setCellValue(totalTaxAmt);
						Cell totalNetAmtCell = total.createCell(33);
						totalNetAmtCell.setCellValue(totalNetAmt);
						Cell totalNetAmtInUsdCell = total.createCell(34);
						totalNetAmtInUsdCell.setCellValue(totalNetAmtInUsd);
						totalGrossValueCell = total.createCell(35);
					} else {
						totalGrossValueCell = total.createCell(27);
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
						if (dataSubmission.has("DaysLastStockSync") && !dataSubmission.get("DaysLastStockSync").isJsonNull()) {
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
						if (dataSubmission.has("DaysLastSalesSync") && !dataSubmission.get("DaysLastSalesSync").isJsonNull()) {
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
						if (dataSubmission.has("DaysLastGRSync") && !dataSubmission.get("DaysLastGRSync").isJsonNull()) {
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
						if (showFields) {
							Cell createCell = header.createCell(keyNum.getAndIncrement());
							createCell.setCellValue(cellName);
						} else {
							if (!(cellName.equalsIgnoreCase("Stock Value") || cellName.equalsIgnoreCase("Stock Value in USD"))) {
								Cell createCell = header.createCell(keyNum.getAndIncrement());
								createCell.setCellValue(cellName);
							}
						}
					});
					int rowNum = 0;
					Double totalQty = new Double(0);
					Double totalBlockedQty = new Double(0);
					Double totalExprQty = new Double(0);
					Double totalAvailableQty = new Double(0);
					Double totalAvlqtyInbaseUm = new Double(0);
					Double totalStackValue = new Double(0);
					Double totalStackValueInUsd = new Double(0);
					Double totalAspValue = new Double(0);
					Double totalConsignmentQty = new Double(0);
					for (int i = 0; i < array.size(); i++) {
						JsonObject stockSummaryObj = array.get(i).getAsJsonObject();
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
							if (stockSummaryObj.has("StockOwnerName") && !stockSummaryObj.get("StockOwnerName").isJsonNull()) {
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
							if (stockSummaryObj.has("ExternalMaterialDesc") && !stockSummaryObj.get("ExternalMaterialDesc").isJsonNull()) {
								String dataType = metadata.get("ExternalMaterialDesc");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockSummaryObj.get("ExternalMaterialDesc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("ExternalMaterialDesc").getAsString());
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
							if (stockSummaryObj.has("StorageLocation") && !stockSummaryObj.get("StorageLocation").isJsonNull()) {
								String dataType = metadata.get("StorageLocation");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String")) {
									String stLocation = stockSummaryObj.get("StorageLocation").getAsString();
									/*
									 * byte[] bytes = stLocation.getBytes(StandardCharsets.UTF_8); stLocation=new String(bytes, StandardCharsets.UTF_8);
									 */
									cell.setCellValue(stLocation);
								} else if (dataType.equalsIgnoreCase("Edm.Decimal"))
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

							if (stockSummaryObj.has("SNoBlockedQty") && !stockSummaryObj.get("SNoBlockedQty").isJsonNull()) {
								String dataType = metadata.get("SNoBlockedQty");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsDouble());
									totalBlockedQty += stockSummaryObj.get("SNoBlockedQty").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("SNoBlockedQty").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("SNoBlockedQty").getAsInt());
									totalBlockedQty += stockSummaryObj.get("SNoBlockedQty").getAsInt();
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
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("ExpiredInSourceUOM").getAsDouble());
									totalExprQty += stockSummaryObj.get("ExpiredInSourceUOM").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("ExpiredInSourceUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("ExpiredInSourceUOM").getAsInt());
									totalExprQty += stockSummaryObj.get("ExpiredInSourceUOM").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}

							if (stockSummaryObj.has("SNOConsignment") && !stockSummaryObj.get("SNOConsignment").isJsonNull()) {
								String dataType = metadata.get("SNOConsignment");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsDouble());
									totalConsignmentQty += stockSummaryObj.get("SNOConsignment").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("SNOConsignment").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("SNOConsignment").getAsInt());
									totalConsignmentQty += stockSummaryObj.get("SNOConsignment").getAsInt();
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
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsDouble());
									totalAvailableQty += stockSummaryObj.get("UnrestrictedInSourceUOM").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("UnrestrictedInSourceUOM").getAsInt());
									totalAvailableQty += stockSummaryObj.get("UnrestrictedInSourceUOM").getAsInt();
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
							if (stockSummaryObj.has("UnrestrictedInBaseUOM") && !stockSummaryObj.get("UnrestrictedInBaseUOM").isJsonNull()) {
								String dataType = metadata.get("UnrestrictedInBaseUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble());
									totalAvlqtyInbaseUm += stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("UnrestrictedInBaseUOM").getAsInt());
									totalAvlqtyInbaseUm += stockSummaryObj.get("UnrestrictedInBaseUOM").getAsDouble();
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
							if (showFields) {
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
							if (showFields) {
								if (stockSummaryObj.has("SNOStockValueInRC") && !stockSummaryObj.get("SNOStockValueInRC").isJsonNull()) {
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
							if (stockSummaryObj.has("SNOASPValue") && !stockSummaryObj.get("SNOASPValue").isJsonNull()) {
								String dataType = metadata.get("SNOASPValue");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsDouble());
									totalAspValue += stockSummaryObj.get("SNOASPValue").getAsDouble();
								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockSummaryObj.get("SNOASPValue").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockSummaryObj.get("SNOASPValue").getAsInt());
									totalAspValue += stockSummaryObj.get("SNOASPValue").getAsInt();
								}
							} else {
								cell = row.createCell(cellNum.getAndIncrement());
								cell.setCellValue("");
							}
							// sheet1.autoSizeColumn(i);

						
					}
					rowNum=rowNum+1;
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");

					// totalBlockedQty
					Cell totalBlockedQtyCell = total.createCell(17);
					totalBlockedQtyCell.setCellValue(totalBlockedQty);
					Cell totalExprQtyCell = total.createCell(18);
					totalExprQtyCell.setCellValue(totalExprQty);

					Cell totalAvailableQtyCell = total.createCell(20);
					totalAvailableQtyCell.setCellValue(totalAvailableQty);
					Cell totalConsignmentQtyCell = total.createCell(19);
					totalConsignmentQtyCell.setCellValue(totalConsignmentQty);
					Cell totalAvlqtyInbaseUmCell = total.createCell(22);
					totalAvlqtyInbaseUmCell.setCellValue(totalAvlqtyInbaseUm);
					Cell totalAspValueCell = null;
					if (showFields) {
						Cell totalStackValueCell = total.createCell(24);
						totalStackValueCell.setCellValue(totalStackValue);
						Cell totalStackValueInUsdCell = total.createCell(25);
						totalStackValueInUsdCell.setCellValue(totalStackValueInUsd);
						totalAspValueCell = total.createCell(26);
					} else {
						short lastCellNum = total.getLastCellNum();
						totalAspValueCell = total.createCell(lastCellNum + 1);
					}
					totalAspValueCell.setCellValue(totalAspValue);

				} else {
					headers.forEach(cellName -> {
						if (showFields) {
							Cell createCell = header.createCell(keyNum.getAndIncrement());
							createCell.setCellValue(cellName);
						} else {
							if (!(cellName.equalsIgnoreCase("Unit Price") || cellName.equalsIgnoreCase("Stock Value"))) {
								Cell createCell = header.createCell(keyNum.getAndIncrement());
								createCell.setCellValue(cellName);
							}
						}
					});
					int rowNum = 0;
					Double totalQty = new Double(0);
					Double totalExprqty = new Double(0);
					Double totalAvblQty = new Double(0);
					Double totalStockVale = new Double(0);
					for (int i = 0; i < array.size(); i++) {
						JsonObject stockDetailsObj = array.get(i).getAsJsonObject();
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

							if (stockDetailsObj.has("ProductGroupDesc") && !stockDetailsObj.get("ProductGroupDesc").isJsonNull()) {
								String dataType = metadata.get("ProductGroupDesc");
								// cell =
								// row.createCell(cellNum.getAndIncrement());
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
							if (stockDetailsObj.has("DMSDivision") && !stockDetailsObj.get("DMSDivision").isJsonNull()) {
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

							if (stockDetailsObj.has("StockOwnerID") && !stockDetailsObj.get("StockOwnerID").isJsonNull()) {
								String dataType = metadata.get("StockOwnerID");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("StockOwnerID").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
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

							if (stockDetailsObj.has("StockOwnerName") && !stockDetailsObj.get("StockOwnerName").isJsonNull()) {
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

							if (stockDetailsObj.has("MaterialDesc") && !stockDetailsObj.get("MaterialDesc").isJsonNull()) {
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

							if (stockDetailsObj.has("ExternalMaterialDesc") && !stockDetailsObj.get("ExternalMaterialDesc").isJsonNull()) {
								String dataType = metadata.get("ExternalMaterialDesc");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal"))
									cell.setCellValue(stockDetailsObj.get("ExternalMaterialDesc").getAsDouble());
								else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("ExternalMaterialDesc").getAsString());
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

							if (stockDetailsObj.has("StorageLocation") && !stockDetailsObj.get("StorageLocation").isJsonNull()) {
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

							if (stockDetailsObj.has("StockTypeDesc") && !stockDetailsObj.get("StockTypeDesc").isJsonNull()) {
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

							if (stockDetailsObj.has("UnrestrictedInBaseUOM") && !stockDetailsObj.get("UnrestrictedInBaseUOM").isJsonNull()) {
								String dataType = metadata.get("UnrestrictedInBaseUOM");
								cell = row.createCell(cellNum.getAndIncrement());
								if (dataType.equalsIgnoreCase("Edm.String"))
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsString());
								else if (dataType.equalsIgnoreCase("Edm.Decimal")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsDouble());

								} else if (dataType.equalsIgnoreCase("Edm.DateTime")) {
									Date date = convertLongToDate(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsString());
									style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
									cell.setCellValue(date);
									cell.setCellStyle(style);
								} else if (dataType.equalsIgnoreCase("Edm.Int32")) {
									cell.setCellValue(stockDetailsObj.get("UnrestrictedInBaseUOM").getAsInt());
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

							if (showFields) {
								if (stockDetailsObj.has("UnitPrice") && !stockDetailsObj.get("UnitPrice").isJsonNull()) {
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

							if (showFields) {
								if (stockDetailsObj.has("StockValue") && !stockDetailsObj.get("StockValue").isJsonNull()) {
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
					rowNum=rowNum+1;
					Row total = sheet1.createRow(rowNum);
					Cell grossCell = total.createCell(0);
					grossCell.setCellValue("Grand Total");
					Cell totalQutyCell = total.createCell(19);
					totalQutyCell.setCellValue(totalQty);

					if (showFields) {
						Cell totalStockValeCell = total.createCell(24);
						totalStockValeCell.setCellValue(totalStockVale);
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
			buffer.append(ex.getClass().getCanonicalName() + "---->");
			if (ex.getLocalizedMessage() != null) {
				buffer.append(ex.getLocalizedMessage() + "------>");
			}
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}

			/*
			 * String localizedMsg=""; if(ex.getLocalizedMessage()!=null){ localizedMsg=ex.getLocalizedMessage(); } stepNo++;
			 * 
			 * oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID, localizedMsg, ex.getClass().getCanonicalName(), "", debug);
			 */

			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", "J002");
			resultjsonObj.addProperty("Message", buffer.toString());
			resultjsonObj.addProperty("Remarks", sheetName + "Not Created");
		} catch (Exception ex) {

			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			buffer.append(ex.getClass().getCanonicalName() + "---->");
			if (ex.getLocalizedMessage() != null) {
				buffer.append(ex.getLocalizedMessage() + "------>");
			}
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			/*
			 * 
			 * stepNo++; oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "Exception Occurred While Creating a Xl Sheet", oDataUrl, userPass, agrgtrID, localizedMsg, ex.getClass().getCanonicalName(), "", debug);
			 */

			resultjsonObj.addProperty("Status", "000002");
			resultjsonObj.addProperty("ErrorCode", "J002");
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

	public void getFilePath() {
		String filePath = getServletContext().getRealPath("/Resources/XlSheet/");
		FILE_NAME = filePath + "Sales and Stock Report - QTD.xlsx";
	}

	private JsonObject sendEmail(final Set<String> quarters, String emailId, boolean debug, HttpServletResponse response, Set<String> ccEmails, final JsonObject mailContent, ODataLogs oDataLogs, AtomicInteger stepNo, HttpServletRequest request, final String oDataUrl, final String logID, final String spGUID, final String userPass,final String agrgtrID, final String cpiLogId, final SXSSFWorkbook workbook) throws Exception {
		Properties properties = new Properties();
		JsonObject retunObj = new JsonObject();
		String fileName="";
		CommonUtils commonUtils=new CommonUtils();
		boolean retryCount=true;
		try {
			
		  final File tmpDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
		  if (!mailContent.get("FirstName").isJsonNull() && !mailContent.get("FirstName").getAsString().equalsIgnoreCase("")) {
			  fileName=mailContent.get("FirstName").getAsString()+"_QTD Sales & Stock Report.xlsx";
		  }else{
			  fileName="QTD Sales & Stock Report.xlsx";
		  }
		  
		  	final String userName = this.userName;
			final String passWord = this.passWord;
			final StringBuffer quarterBuff = new StringBuffer();
			Iterator<String> quIterator = quarters.iterator();
			if (quarters.size() == 1) {
				StringBuffer forQtdBuf = new StringBuffer();
				String quarter = quIterator.next();
				forQtdBuf.append(quarter.substring(4, quarter.length())).append(" ").append("FY").append(quarter.substring(2, 4));
				quarterBuff.append(forQtdBuf);
			} else {
				while (quIterator.hasNext()) {
					StringBuffer forQtdBuf = new StringBuffer();
					String quarter = quIterator.next();
					forQtdBuf.append(quarter.substring(4, quarter.length())).append(" ").append("FY").append(quarter.substring(2, 4));
					quarterBuff.append(forQtdBuf).append(" and ");
				}
				quarterBuff.delete(quarterBuff.length() - 5, quarterBuff.length());
			}

			String emailSubject = "Reg: DMS QTD Sales & Stock Report";

			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

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
				if (!ccEmails.isEmpty() && ccEmails.size() > 0) {
					Iterator<String> iterator = ccEmails.iterator();
					while (iterator.hasNext()) {
						String email = iterator.next();
						InternetAddress ccAddress = new InternetAddress(email);
						msg.addRecipient(Message.RecipientType.CC, ccAddress);
					}
				}
				msg.setSubject(emailSubject);
				final Multipart emailContent = new MimeMultipart();
				final MimeBodyPart textBodyPart = new MimeBodyPart();
				if (!mailContent.get("FirstName").isJsonNull() && !mailContent.get("FirstName").getAsString().equalsIgnoreCase("")) {
					textBodyPart.setText("Dear " + mailContent.get("FirstName").getAsString() + "," + "\nPlease find the enclosed " + quarterBuff.toString() + " QTD Sales & Stock Report.\n\nThanks & Regards\nMedtronic DMS Admin Team");
				} else {
					textBodyPart.setText("Dear Sir/Madam,\nPlease find the enclosed " + quarterBuff.toString() + " QTD Sales & Stock Report.\n\nThanks & Regards\nMedtronic DMS Admin Team");
				}
				
				final MimeBodyPart pdfAttachment = new MimeBodyPart();
				final File attachmentFile = new File(tmpDir, fileName);
				final FileOutputStream outputStream = new FileOutputStream(attachmentFile);
				workbook.write(outputStream);
				outputStream.flush();
				outputStream.close();
					
				pdfAttachment.attachFile(attachmentFile);
				if (debug) {
					response.getWriter().println("file attached successfully");
				}
				emailContent.addBodyPart(textBodyPart);
				emailContent.addBodyPart(pdfAttachment);
				msg.setContent(emailContent);
				
				String absolutePath = attachmentFile.getAbsolutePath();
				Path path = Paths.get(attachmentFile.getAbsolutePath());
				long fileSize = Files.size(path);
				oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "" + "Attached File Size: " + String.format("%,d bytes", fileSize) + ";Sent To:" + emailId, stepNo.getAndIncrement(), "Email Details", oDataUrl, userPass, agrgtrID, "File Path:", absolutePath, "", debug);
				Transport.send(msg);
				
				boolean delete = attachmentFile.delete();
				retunObj.addProperty("Message", "Mail Sent Successfully");
				retunObj.addProperty("ErrorCode", "");
				retunObj.addProperty("Status", "000001");
				return retunObj;
				
			}
			catch (SMTPSendFailedException ex) {
				
				StringBuffer buffer = new StringBuffer();
				buffer.append(ex.getClass().getCanonicalName() + "--->");
				if (ex.getLocalizedMessage() != null) {
					buffer.append(ex.getLocalizedMessage()).append("----->");
				}
				if(retryCount){
					retryCount=false;
				sendEmail(quarters, emailId, debug, response, ccEmails, mailContent, oDataLogs, stepNo, request, oDataUrl, logID, spGUID, userPass, agrgtrID, cpiLogId, workbook);
				}else{
				commonUtils.sendQtdSalesExceptionMail(buffer.toString(), properties, spGUID,this.userName,this.passWord);
				}
				StackTraceElement[] stackTrace = ex.getStackTrace();
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}
				retunObj.addProperty("Message", buffer.toString());
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				return retunObj;
			}
			catch (SendFailedException ex) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(ex.getClass().getCanonicalName() + "--->");
				if (ex.getLocalizedMessage() != null) {
					buffer.append(ex.getLocalizedMessage()).append("----->");
				}
				
				StackTraceElement[] stackTrace = ex.getStackTrace();
				
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}

				/*
				 * oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "Exception Occurred While Sending an Email", oDataUrl, userPass, agrgtrID, "", "", "", debug);
				 */
				// oDataLogs.updateCpiApplicationLog(request, response, oDataUrl, userPass, cpiLogId, logID, buffer.toString(), "E",agrgtrID,accountID ,debug);
				retunObj.addProperty("Message", buffer.toString());
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				return retunObj;
			} catch (MessagingException ex) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(ex.getClass().getCanonicalName() + "--->");
				if (ex.getLocalizedMessage() != null) {
					buffer.append(ex.getLocalizedMessage()).append("----->");
				}
				
				StackTraceElement[] stackTrace = ex.getStackTrace();
				
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}

				/*
				 * buffer.append("StackTrace-->"+stackTraceBuf.toString());
				 * 
				 * 
				 * 
				 * oDataLogs.insertQuartelyMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo, "Exception Occurred While Sending an Email", oDataUrl, userPass, agrgtrID, localizedMessage, ex.getClass().getCanonicalName(), "", debug); retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
				 */
				retunObj.addProperty("Message", buffer.toString());
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				// oDataLogs.updateCpiApplicationLog(request, response, oDataUrl, userPass, cpiLogId, logID, buffer.toString(), "E",agrgtrID,accountID ,debug);
				return retunObj;
			} catch (IOException ex) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(ex.getClass().getCanonicalName() + "--->");
				if (ex.getLocalizedMessage() != null) {
					buffer.append(ex.getLocalizedMessage()).append("----->");
				}
				StackTraceElement[] stackTrace = ex.getStackTrace();
				
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}
				
				// oDataLogs.updateCpiApplicationLog(request, response, oDataUrl, userPass, cpiLogId, logID, buffer.toString(), "E",agrgtrID,accountID ,debug);
				retunObj.addProperty("Message", buffer.toString());
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				return retunObj;
			}
		} catch (Exception ex) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(ex.getClass().getCanonicalName() + "--->");
			if (ex.getLocalizedMessage() != null) {
				buffer.append(ex.getLocalizedMessage()).append("----->");
			}
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			
			retunObj.addProperty("Message", buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			return retunObj;

		}

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

				if (asJsonObject.has("ExternalSoldToCPName") && !asJsonObject.get("ExternalSoldToCPName").isJsonNull()) {
					updatedObj.addProperty("Source Sold To Party Name", asJsonObject.get("ExternalSoldToCPName").getAsString());
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

				if (asJsonObject.has("ExternalMaterialDesc") && !asJsonObject.get("ExternalMaterialDesc").isJsonNull()) {
					updatedObj.addProperty("Source Material Description", asJsonObject.get("ExternalMaterialDesc").getAsString());
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

	public void getFilePath(HttpServletRequest request) {
		String filePath = getServletContext().getRealPath("/Resources/XlSheet/");
		FILE_NAME = filePath + "Sales and Stock Report - QTD.xlsx";
		File file = new File(FILE_NAME);
		if (file.exists())
			file.delete();
	}

	public List<String> filterDmsDivisionRecord(String loginId, HttpServletResponse response) throws Exception {
		List<String> userAuthLst = new ArrayList<String>();
		String executeUrl = "";
		CommonUtils commonUtils = new CommonUtils();
		try {
			if (loginId != null && !loginId.equalsIgnoreCase("") && loginId.length() > 0) {
				executeUrl = pugOdataUrl + "UserAuthSet?$filter=LoginID%20eq%20%27" + loginId + "%27%20and%20AuthOrgTypeID%20eq%20%27" + "000013" + "%27%20and%20Application%20eq%20%27" + "PD" + "%27";
			} else {
				executeUrl = pugOdataUrl + "UserAuthSet?$filter=AuthOrgTypeID%20eq%20%27" + "000013" + "%27%20and%20Application%20eq%20%27" + "PD" + "%27";
			}

			JsonObject userAuthsetObj = commonUtils.executeURL(executeUrl, pugUserPass, response);
			if (userAuthsetObj != null && !userAuthsetObj.has("error")) {
				if (userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					boolean isDmsDivisionFilterReq = true;
					JsonArray userAuthset = userAuthsetObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
					for (int j = 0; j < userAuthset.size(); j++) {
						if (!userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").isJsonNull() && !userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").getAsString().equalsIgnoreCase("")) {
							if (userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").getAsString().equalsIgnoreCase("*")) {
								isDmsDivisionFilterReq = false;
								break;
							}

						} else {
							isDmsDivisionFilterReq = false;
						}

					}

					if (isDmsDivisionFilterReq) {
						for (int j = 0; j < userAuthset.size(); j++) {
							if (!userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").isJsonNull() && !userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").getAsString().equalsIgnoreCase("")) {
								userAuthLst.add(userAuthset.get(j).getAsJsonObject().get("AuthOrgValue").getAsString());

							}

						}

					}

				}

			}
		} catch (Exception ex) {
			throw ex;

		}
		return userAuthLst;

	}

	private JsonObject getFinancialQuarterCode(boolean debug, HttpServletResponse response) throws Exception {
		String executeUrl = "";
		CommonUtils commonUtils = new CommonUtils();
		try {
			Calendar currentDate = Calendar.getInstance();
			Timestamp currentTimeStamp = new Timestamp(currentDate.getTime().getTime());
			Calendar aCalendar = Calendar.getInstance();
			aCalendar.add(Calendar.MONTH, -1);
			aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DATE) - 3);
			Timestamp lastMonTimeStamp = new Timestamp(aCalendar.getTime().getTime());
			aCalendar.add(Calendar.DATE, 7);
			Timestamp previousTimeStamp = new Timestamp(aCalendar.getTime().getTime());
			String curDate = new SimpleDateFormat("YYYY-MM-dd").format(currentTimeStamp);
			String prvDate = new SimpleDateFormat("YYYY-MM-dd").format(lastMonTimeStamp);
			if (currentTimeStamp.equals(previousTimeStamp) || currentTimeStamp.before(previousTimeStamp)) {
				executeUrl = pcgoDataUrl + "FiscalYear?$filter=(DateOfYear%20eq%20datetime%27" + curDate + "T00:00:00%27%20or%20DateOfYear%20eq%20datetime%27" + prvDate + "T00:00:00%27)&$select=FinancialQuarterCode";
			} else {
				executeUrl = pcgoDataUrl + "FiscalYear?$filter=DateOfYear%20eq%20datetime%27" + curDate + "T00:00:00%27&$select=FinancialQuarterCode";
			}
			JsonObject financialObj = commonUtils.executeURL(executeUrl, pcgUserPass, response);
			return financialObj;
		} catch (Exception ex) {
			throw ex;
		}

	}
}
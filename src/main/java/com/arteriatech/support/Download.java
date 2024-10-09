package com.arteriatech.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import org.apache.http.client.HttpClient;
import com.arteriatech.support.DestinationUtils;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.vavr.control.Try;

public class Download extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static  int BATCH_SIZE=100;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String inputPayload = "", service = "", entity = "", oDataUrl = "", userName = "", password = "",
				sapclient = "", authMethod = "", destURL = "";
		JsonParser parser = new JsonParser();
		CommonUtils commonUtils = new CommonUtils();
		Properties props = new Properties();
		Destination destConfiguration = null;
		boolean debug = false,isCountQuery=false;
		String sessionID = "", loginMethod = "", customFilter = "", query = "",filters="";
		int skipCount=0,loopCount=0;
		SXSSFWorkbook workbook = null;
		String filetrs="",select="",loginID="";
		try {
			//inputPayload = commonUtils.getGetBody(request, response);
			
		    inputPayload=request.getParameter("download");
			props.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonInputPayload = (JsonObject) parser.parse(inputPayload);
				String errorMessage = validateInputpayload(jsonInputPayload, response);
				if (errorMessage.equalsIgnoreCase("")) {
					if (jsonInputPayload.has("debug")
							&& jsonInputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
						debug = true;
					}
					long startProcess = System.currentTimeMillis();
					Date processStartTime=new Date(startProcess);
					
					if(debug){
						response.getWriter().println("Process Strated :"+processStartTime);
					}

					JsonArray filterArray = jsonInputPayload.get("filter").getAsJsonArray();
					
					StringBuffer withPipes=new StringBuffer();
					StringBuffer withOutPipe=new StringBuffer();
					StringBuffer queryStr=new StringBuffer();
					for (int i = 0; i < filterArray.size(); i++) {
						JsonObject asJsonObject = filterArray.get(i).getAsJsonObject();
						Iterator<Entry<String, JsonElement>> iterator = asJsonObject.entrySet().iterator();
						while (iterator.hasNext()) {
							Entry<String, JsonElement> next = iterator.next();
							String key = next.getKey();
							if (!asJsonObject.get(key).isJsonNull()
									&& !asJsonObject.get(key).getAsString().equalsIgnoreCase("")) {
								String asString = asJsonObject.get(key).getAsString();
								if (!asString.contains("|") && !asString.startsWith("(")) {
									withOutPipe.append(key).append(" ").append("eq").append(" ").append("'")
											.append(asString).append("'").append(" ").append("and").append(" ");
								} else if (asString.contains("|")) {
									if(!withPipes.toString().isEmpty()||!withPipes.toString().equalsIgnoreCase("")) {
										int lastIndexOf2 = withPipes.lastIndexOf("'");
										String removalOfwithPipes=withPipes.substring(0, lastIndexOf2+1).toString();
										withPipes=new StringBuffer(removalOfwithPipes);
										withPipes.append(")");
										withPipes.append(" ").append("and").append(" ");
									}
									withPipes.append("(");
									String[] split = asString.split("\\|");
									for (int j = 0; j < split.length; j++) {
										withPipes.append(key).append(" ").append("eq").append("'").append(split[j])
												.append("'").append(" ").append("or").append(" ");
									}
									withPipes.append(")");
								} else if (asString.startsWith("(")) {
									if (!queryStr.toString().isEmpty() && queryStr.length() > 0) {
										queryStr.append(" ").append("and").append(" ").append(asString);
									} else {
										queryStr.append(asString);
									}
								}
							}
						}

					}
					
					if(jsonInputPayload.has("loginid")&& !jsonInputPayload.get("loginid").isJsonNull()&&!jsonInputPayload.get("loginid").getAsString().equalsIgnoreCase("")){
						loginID=jsonInputPayload.get("loginid").getAsString();
					}
					
					/*if(debug){
						response.getWriter().println("loginID:"+loginID);
					}*/
					if(loginID!=null && !loginID.equalsIgnoreCase("")){
					String finalwithOutPipe=null;
					StringBuffer finalWitWhiteSpace=null;
					if(!withOutPipe.toString().isEmpty()&&withOutPipe.toString().trim().length()>0) {
						int lastIndexOf = withOutPipe.lastIndexOf("'");
							finalwithOutPipe=withOutPipe.substring(0, lastIndexOf+1).toString();
						}
						if(!withPipes.toString().isEmpty()&&withPipes.toString().trim().length()>0) {
						int lastIndexOf2 = withPipes.lastIndexOf("'");
						String removalOfwithPipes=withPipes.substring(0, lastIndexOf2+1).toString();
						finalWitWhiteSpace=new StringBuffer(removalOfwithPipes);
						finalWitWhiteSpace.append(")");
					}
					
					//System.out.println("queryStr:"+queryStr);
					StringBuffer buffer=new StringBuffer();
					 if(finalwithOutPipe!=null && !finalwithOutPipe.equalsIgnoreCase("")&&finalwithOutPipe.length()>0) {
						 buffer.append(finalwithOutPipe);
					 }
					 
						if (finalWitWhiteSpace != null && !finalWitWhiteSpace.toString().equalsIgnoreCase("")
								&& finalWitWhiteSpace.toString().trim().length() > 0) {
							if (buffer != null && !buffer.toString().isEmpty() && buffer.toString().trim().length() > 0) {
								buffer.append(" " + "and" + " " + finalWitWhiteSpace);
							} else {
								buffer.append(finalWitWhiteSpace);
							}
						}
					
					 if(queryStr!=null && !queryStr.toString().isEmpty()&& queryStr.toString().length()>0) {
						 if(!buffer.toString().isEmpty()&& buffer.length()>0) {
							 buffer.append(" ").append("and").append(" ").append(queryStr);
						 }else {
							 buffer.append(queryStr);
						 }
					 }
					 
					filetrs=buffer.toString();
					if(debug){
						response.getWriter().println(filetrs);
					}
					select=jsonInputPayload.get("select").getAsString();
					service = jsonInputPayload.get("Service").getAsString();
					entity = jsonInputPayload.get("EntitySet").getAsString();
					destConfiguration = getDestinationURL(request, response);
					
					//String loginID = commonUtils.getUserPrincipal(request, "name", response);
					JsonArray summaryArray = jsonInputPayload.get("sum").getAsJsonArray();
					filetrs = URLEncoder.encode(filetrs, "UTF-8");
					filetrs = filetrs.replaceAll("%26", "&");
					filetrs = filetrs.replaceAll("%3D", "=");
					/*if(debug){
						response.getWriter().println("Updated Filters:"+filetrs);
					}*/
					
					/*if(debug){
						response.getWriter().println("x-arteria-count:"+request.getHeader("x-arteria-count"));
					}*/
					
					
					String totalCount=null;
					totalCount = request.getHeader("x-arteria-count");
					if(totalCount==null || totalCount.equalsIgnoreCase("")||totalCount.trim().length()<0){
						if(jsonInputPayload.has("count")&&!jsonInputPayload.get("count").isJsonNull()&&!jsonInputPayload.get("count").getAsString().equalsIgnoreCase("")){
							totalCount=jsonInputPayload.get("count").getAsString();
						}
					}
					if(debug){
						response.getWriter().println("totalCount:"+totalCount);
					}
					
					Map<String, BigDecimal> totalSummarymap = new HashMap<>();
					for (int i = 0; i < summaryArray.size(); i++) {
						JsonObject summaryFields = summaryArray.get(i).getAsJsonObject();
						Set<Entry<String, JsonElement>> entries = summaryFields.entrySet();
						for (Map.Entry<String, JsonElement> entry : entries) {
							String key = entry.getKey();
							totalSummarymap.put(key, new BigDecimal(0));
						}
					}
					
					/*if(jsonInputPayload.has("CPNo")&&!jsonInputPayload.get("CPNo").isJsonNull()&&!jsonInputPayload.get("CPNo").getAsString().equalsIgnoreCase("")){
						filters="CPNo"
					}*/
					
					/*if(debug){
						response.getWriter().println("Login ID:"+loginID);
					}*/

					sapclient = destConfiguration.get("sap-client").get().toString();
					authMethod = destConfiguration.get("Authentication").get().toString();
					destURL = destConfiguration.get("URL").get().toString();
					/*if (debug) {
						response.getWriter().println("sapclient:" + sapclient);
						response.getWriter().println("authMethod:" + authMethod);
						response.getWriter().println("destURL:" + destURL);
					}*/

					/*if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("URL:" + url);
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					} else {
						// loginMethod =
						// destConfiguration.getProperty("LoginMethod");

						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug) {
							response.getWriter().println("url: " + url);
							response.getWriter().println("Auth Method: " + authMethod);
						}
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					}

					if (debug)
						response.getWriter().println("sessionID:" + sessionID);

					//destURL = getDestinationURL(request, response, "URL");

					if (debug) {
						response.getWriter().println("ssfi Destination URL:" + destURL);
					}

					if (sapclient != null) {
						query = destURL + entity +"/$count"+"?sap-client=" + sapclient;
					} else {
						query = destURL + entity+"/$count";
					}

					JsonObject totalRoc = executeURL(query, response, request, sessionID,true,debug);*/
					
					//int totalRecords = totalRoc.get("Message").getAsInt();
					int totalRecords=0;
					if (totalCount != null) {
						try {
							totalRecords = Integer.parseInt(totalCount);
						} catch (NumberFormatException ex) {

						}catch (Exception ex) {

						}
					}
					
					/*if(debug){
						response.getWriter().println("Total Records:"+totalRecords);
					}*/
					
					/*if(debug){
						if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug)
								response.getWriter().println("Generating Session URL:" + url);
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						} else {
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug) {
								response.getWriter().println("url: " + url);
								response.getWriter().println("Auth Method: " + authMethod);
							}
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						}
						
						if (sapclient != null) {
							query = destURL+"$metadata" +"?sap-client=" + sapclient+"&sap-language=EN";
						} else {
							query = destURL + "$metadata";
						}
						
						if(debug){
							response.getWriter().println("Metadata URL:"+query);
						}
						//JsonObject metaDataObj = executeURL(query, response, request, sessionID, false, debug);
						JsonObject metaDataObj=executeMetadataURL(query, response, request, sessionID, debug);
						
						if(metaDataObj.get("Status").getAsString().equalsIgnoreCase("000001")){
							String asString = metaDataObj.get("Message").getAsString();
						    JSONObject jsonObject = XML.toJSONObject(asString);
						    response.getWriter().println("Metadat Object:"+jsonObject);
						}
						
					}*/
						/*if (debug) {
							response.getWriter().println("metaDataObj:" + metaDataObj);
						}*/
						
						
						
						/*List<String> headers=new ArrayList<>();
						headers.add("InvoiceNo");
						headers.add("InvoiceDate");
						headers.add("CPNo");
						headers.add("CPName");
						headers.add("BeatCode");
						headers.add("BeatDesc");
						headers.add("SoldToID");
						headers.add("SoldToName");
						headers.add("NetAmount");
						headers.add("StatusID");
						headers.add("PaymentStatusID");
						headers.add("Source");
						headers.add("InvoiceTypeID");
						headers.add("InvoiceTypeDesc");
						headers.add("GSTIN");
						headers.add("GrossAmount");
						headers.add("PriTradeDisc");
						headers.add("SecTradeDisc");
						headers.add("CashDiscAmount");
						headers.add("SPLDiscountAmt");
						headers.add("TCSPerc");
						headers.add("TCSAmount");
						headers.add("AdditionalDiscAmt");
						headers.add("TaxableAmount");
						headers.add("Tax1");
						headers.add("Tax2");
						headers.add("Tax3");
						headers.add("Tax");
						headers.add("CreditNote");
						headers.add("OnInvDis");
						headers.add("DebitNote");
						headers.add("NetPayAmt");
						headers.add("TotDiscAmt");
						headers.add("CESS");
						headers.add("CashDiscRsn");
						headers.add("ExternalRefID");
						headers.add("CreatedOn");
						headers.add("CreatedAt");
						headers.add("ChangedOn");
						headers.add("ChangedAt");
						headers.add("InvoiceGUID");
						headers.add("SoldToCPGUID");*/
					
						String[] split = select.split(",");
						List<String> headers = Arrays.asList(split);
					    List<String> sheetheaders = new ArrayList<>();
					    if(entity.equalsIgnoreCase("SSInvoices")){
					    sheetheaders.add("Customer No");
				    	sheetheaders.add("Customer Name");
				    	sheetheaders.add("Invoice No");
				    	sheetheaders.add("Invoice Date");
				    	sheetheaders.add("Sales Person");
				    	sheetheaders.add("Beat Desc");
				    	sheetheaders.add("Beat Code");
				    	sheetheaders.add("Sold To Party Name");
				    	sheetheaders.add("Sold To Party");
				    	sheetheaders.add("Net Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Invoice Status");
				    	sheetheaders.add("Payment Status");
				    	sheetheaders.add("Source");
				    	sheetheaders.add("Invoice Type Desc");
				    	sheetheaders.add("Invoice Type");
				    	sheetheaders.add("GST Number");
				    	sheetheaders.add("Gross Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Pri Trade Dis");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Sec Trade Disc");
				    	sheetheaders.add("Unit");
				    	
				    	sheetheaders.add("Spl Disc Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Cash Dis Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("TCS Percentage");
				    	sheetheaders.add("TCS Amount");
				    	
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Add.Dis Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Taxable amount");
				    	
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("CGST Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("SGST / UGST Amt");
				    	
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("IGST Amount");
				    	sheetheaders.add("Tax Amount");
				    	sheetheaders.add("Unit");
				    	
				    	sheetheaders.add("Credit Note");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Invoice Discount");
				    	sheetheaders.add("Unit");
				    	
				    	sheetheaders.add("Debit Note");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Net Pay Amt");
				    	sheetheaders.add("Unit");
				    	
				    	sheetheaders.add("TOT Discount Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("CESS");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Cash Dis Rsn De");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Ext Ref ID Desc");
				    	sheetheaders.add("Ext Ref ID");
				    	sheetheaders.add("Created On");
				    	sheetheaders.add("Created At");
				    	sheetheaders.add("Changed On");
				    	sheetheaders.add("Changed At");
				    	}else{
				    	sheetheaders.add("Customer No");
				    	sheetheaders.add("Customer Name");
				    	sheetheaders.add("Invoice No");
				    	sheetheaders.add("Item No");
				    	sheetheaders.add("Invoice Type Desc");
				    	sheetheaders.add("Invoice Type");
				    	sheetheaders.add("Invoice Date");
				    	sheetheaders.add("Sold To Party Name");
				    	sheetheaders.add("Sold To Party");
				    	sheetheaders.add("Material No Desc");
				    	sheetheaders.add("Material No");
				    	sheetheaders.add("Brand Desc");
				    	sheetheaders.add("Brand");
				    	sheetheaders.add("Banner Desc");
				    	sheetheaders.add("Banner");
				    	sheetheaders.add("SKU Desc");
				    	sheetheaders.add("SKU");
				    	sheetheaders.add("DMS Division Desc");
				    	sheetheaders.add("DMS Division");
				    	sheetheaders.add("Sales Person Name");
				    	sheetheaders.add("Sales Person");
				    	sheetheaders.add("Beat Desc");
				    	sheetheaders.add("Beat Code");
				    	sheetheaders.add("Invoice Qty");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Conversion Factor");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Conversion Factor");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Net weight");
				    	sheetheaders.add("MRP");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Unit Price");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Pri Trade Dis");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Pri Dis Per Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Pri Disc Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Sec Trade Disc");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Sec Dis Per Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Sec Trd Dis Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Spl Disc Perc");
				    	sheetheaders.add("Spl Disc Perc Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Spl Disc Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Gross Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Taxable amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Tax Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("Net Amount");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("TOT Discount Amt");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("CESS");
				    	sheetheaders.add("Unit");
				    	sheetheaders.add("CESS Percentage");
				    	sheetheaders.add("Status");
				    	sheetheaders.add("Payment Status");
				    	sheetheaders.add("Ext Ref ID");
				    	sheetheaders.add("Ref Doc No");
				    	sheetheaders.add("Ref Doc Date");
				    	}
						workbook = new SXSSFWorkbook();
						workbook.setCompressTempFiles(true);
						SXSSFSheet sheet = createXlSheet(workbook, entity, sheetheaders, response, debug);
						if (debug) {
							//response.getWriter().println("sheet:" + sheet);
							response.getWriter().println("sheet Name:" + sheet.getSheetName());
						}
						
						if (totalRecords > 0 && totalRecords <= BATCH_SIZE) {
							Date genSessionStart=new Date(System.currentTimeMillis());
							if(debug){
								response.getWriter().println("Session  Genaration Starated:"+genSessionStart);
							}
							if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
								String url = commonUtils.getDestinationURL(request, response, "URL");
								/*if (debug)
									response.getWriter().println("Generating Session URL:" + url);*/
								sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							} else {
								String url = commonUtils.getDestinationURL(request, response, "URL");
								/*if (debug) {
									response.getWriter().println("url: " + url);
									response.getWriter().println("Auth Method: " + authMethod);
								}*/
								sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							}
							
							Date genSessionend=new Date(System.currentTimeMillis());
							if(debug){
								response.getWriter().println("Session  Generation End:"+genSessionend);
							}

							if (sapclient != null) {
								/*query = destURL + entity + "?sap-client=" + sapclient
										+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
										+ skipCount + "&$top=" + BATCH_SIZE;*/
								query = destURL + entity + "?sap-client=" + sapclient
										+ "&$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
										+ skipCount + "&$top=" + BATCH_SIZE;
							} else {
								/*query = destURL + entity
										+ "?$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="+skipCount+"&$top="
										+ BATCH_SIZE;*/
								
								query = destURL + entity
										+ "?$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
										+ skipCount + "&$top=" + BATCH_SIZE;
							}
							if (debug) {
								response.getWriter().println("fetch Records from SSinvoices Table query: " + query);
							}
							
							Date fetchingRecordStart=new Date(System.currentTimeMillis());
							if(debug){
								response.getWriter().println("Fetching Records from DB Strat:"+fetchingRecordStart);
							}
							
							JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false, debug);
							Date fetchingRecordend=new Date(System.currentTimeMillis());
							if(debug){
								response.getWriter().println("Fetching Records from DB End:"+fetchingRecordend);
							}
							/*if (debug) {
								if(!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
								response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
							}*/
							if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
								//JsonArray ssinvArry = ssinvocieObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
								if (sheet != null) {
									Date exportToExcelSheet=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Export to excel Sheet Strat:"+exportToExcelSheet);
									}
									AtomicInteger rowNumber = new AtomicInteger(1);
									JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj, rowNumber,headers, totalSummarymap,debug);
									if(exportToXlSheet.get("Status").getAsString().equalsIgnoreCase("000001")){
									int lastRowNum = sheet.getLastRowNum();
									SXSSFRow summaryRow = sheet.createRow(lastRowNum+1);
									SXSSFCell grandTotal = summaryRow.createCell(0);
									grandTotal.setCellValue("Grand Total");
									Set<Entry<String, BigDecimal>> entrySet = totalSummarymap.entrySet();
									for(Map.Entry<String, BigDecimal> entry:entrySet){
										String key = entry.getKey();
										 if(headers.contains(key)){
											 int indexOf = headers.indexOf(key);
											 SXSSFCell createCell = summaryRow.createCell(indexOf);
											 BigDecimal bigDecimal = totalSummarymap.get(key);
											 bigDecimal=bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
											 createCell.setCellValue(bigDecimal.doubleValue());
											// CellUtil.setAlignment(createCell, HorizontalAlignment.RIGHT);
										 }
										
									}
									
									Date exportToExcelend=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Export to excel Sheet Completed:"+exportToExcelend);
									}
									if(!debug){
									response.setContentType("application/vnd.ms-excel");
									Date date=new Date(System.currentTimeMillis());
									String format2=null;
									try{
										SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
										Date parse = dateFormat.parse(date.toString());
										dateFormat = new SimpleDateFormat("yyyyddMMhhmmss");
										 format2 = dateFormat.format(parse);
									}catch(Exception ex){
										format2=date.toString();
									}
									response.setHeader("Content-Disposition", "attachment; filename="+entity+format2+".xls");
									ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
									workbook.write(byteArrayOutputStream);
									response.setHeader("Content-Length", "" + byteArrayOutputStream.size());
									workbook.write(response.getOutputStream());
									response.flushBuffer();
									}
									}else{
										response.getWriter().println(exportToXlSheet);
									}
								} else {
									//response.reset();
									response.setHeader("Content-type", "application/json");
									JsonObject resObj = new JsonObject();
									resObj.addProperty("Message", "Excel Sheet Not created");
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									response.getWriter().println(resObj);
								}
							} else {
								//response.reset();
								response.setHeader("Content-type", "application/json");
								response.getWriter().println(ssinvocieObj);
							}

						} else if(totalRecords > 0){
							// call batch wise
							if (totalRecords > 0 && totalRecords < 1000) {
								BATCH_SIZE = 100;
							} else if (totalRecords > 1000 && totalRecords < 10000) {
								BATCH_SIZE = 1000;
							} else {
								BATCH_SIZE = 10000;
							}
							
							loopCount = totalRecords / BATCH_SIZE;
							int rem = totalRecords % BATCH_SIZE;
							if (rem > 0) {
								loopCount++;
							}
							if(debug){
								response.getWriter().println("Batch Size:"+BATCH_SIZE);
								response.getWriter().println("Loop Count:"+loopCount);
							}
							AtomicInteger rowNumber = new AtomicInteger(1);
							/*File tmpDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
							File privFile = new File(tmpDir, "ssinvoices.csv");*/
							boolean exportedSuccess=true;
							for (int i = 1; i <= loopCount; i++) {
								if (debug) {
									response.getWriter().println("Batch No:" + i);
								}
								if (i == 1) {
									Date sessionStart=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Session Generatetion Strated Batch No:"+i+" Time: "+sessionStart);
									}
									if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug)
											response.getWriter().println("Generating Session URL:" + url);
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									} else {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										/*if (debug) {
											response.getWriter().println("url: " + url);
											response.getWriter().println("Auth Method: " + authMethod);
										}*/
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									}
									Date sessionend=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Session Generatetion completed Batch No:"+i+" Time: "+sessionend);
									}
									/*if(debug){
										response.getWriter().println("Session Id:"+sessionID);
									}*/

									if (sapclient != null) {
										/*query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;*/
										query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									} else {
										query = destURL + entity
												+ "?$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									}
									/*if (debug) {
										response.getWriter()
												.println("Fetch Records from SSinvoices Table Query:" + query);
									}*/
									
									Date excuteQueryStart=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Fetching Records strated for the Batch :"+i+" Time :"+excuteQueryStart);
									}

									JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false,
											debug);
									
									Date excuteQueryend=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Fetching Records completed  for the  Batch :"+i+" Time :"+excuteQueryend);
									}
									if (debug) {
										if (!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
											response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
									}

									if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
										ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
										if (sheet != null) {
											Date exportTOexcelStart=new Date(System.currentTimeMillis());
											if(debug){
												response.getWriter().println("Export to Excel Sheet strated  for the Batch :"+i+" Time :"+exportTOexcelStart);
											}
											JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj,
													rowNumber,headers,totalSummarymap,debug);
											Date exportTOexcelCompleted=new Date(System.currentTimeMillis());
											if(debug){
												response.getWriter().println("Export to Excel Sheet completed  for the Batch :"+i+" Time :"+exportTOexcelCompleted);
											}
											if (!exportToXlSheet.get("Status").getAsString()
													.equalsIgnoreCase("000001")) {
												// workbook.write(response.getOutputStream());
												exportedSuccess = false;
												//response.reset();
												response.setHeader("Content-type", "application/json");
												response.getWriter().println(exportToXlSheet);
												break;
											}

										} else {
											exportedSuccess = false;
											//response.reset();
											response.setHeader("Content-type", "application/json");
											JsonObject resObj = new JsonObject();
											resObj.addProperty("Message", "Excel Sheet Not created");
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											response.getWriter().println(resObj);
											break;
										}
									} else {
										exportedSuccess = false;
										response.reset();
										response.setHeader("Content-type", "application/json");
										response.getWriter().println(ssinvocieObj);
										break;
									}

								} else {
									skipCount = skipCount + BATCH_SIZE;
									if (debug) {
										response.getWriter().println("skipCount: " + skipCount);
									}

									if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
										Date sessionStart=new Date(System.currentTimeMillis());
										if(debug){
											response.getWriter().println("Session Generatetion Strated Batch No:"+i+" Time: "+sessionStart);
										}
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug)
											response.getWriter().println("Generating Session URL:" + url);
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									} else {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug) {
											response.getWriter().println("url: " + url);
											response.getWriter().println("Auth Method: " + authMethod);
										}
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									}
									
									Date sessionend=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Session Generatetion completed Batch No:"+i+" Time: "+sessionend);
									}
									
									/*if(debug){
										response.getWriter().println("Session Id:"+sessionID);
										response.getWriter().println("Loop Count:"+i);
									}*/

									if (sapclient != null) {
										/*query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;*/
										
										query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;

									} else {
										/*query = destURL + entity
												+ "?$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;*/
										
										query = destURL + entity
												+ "?$format=json&$filter="+filetrs+"&$select="+select+"&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									}
									/*if (debug) {
										response.getWriter().println("query:" + query);
									}*/
									
									Date excuteQueryStart=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Fetching Record Strated for the Batch :"+i+" Time :"+excuteQueryStart);
									}
									
									JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false,
											debug);
									
									Date excuteQueryend=new Date(System.currentTimeMillis());
									if(debug){
										response.getWriter().println("Fetching Records completed  for the  Batch :"+i+" Time :"+excuteQueryend);
									}

									if (debug) {
										if (!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
											response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
									}

									if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
										ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
										if (sheet != null) {
											Date exportTOexcelStart=new Date(System.currentTimeMillis());
											if(debug){
												response.getWriter().println("Export to Excel Sheet strated  for the Batch :"+i+" Time :"+exportTOexcelStart);
											}
											JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj,
													rowNumber,headers,totalSummarymap,debug);
											
											Date exportTOexcelCompleted=new Date(System.currentTimeMillis());
											if(debug){
												response.getWriter().println("Export to Excel Sheet completed  for the Batch :"+i+" Time :"+exportTOexcelCompleted);
											}
											
											if (!exportToXlSheet.get("Status").getAsString()
													.equalsIgnoreCase("000001")) {
												//workbook.write(response.getOutputStream())
												exportedSuccess = false;
												//response.reset();
												response.setHeader("Content-type", "application/json");
												response.getWriter().println(exportToXlSheet);
												break;
											
											}
										} else {
											exportedSuccess = false;
											//response.reset();
											response.setHeader("Content-type", "application/json");
											JsonObject resObj = new JsonObject();
											resObj.addProperty("Message", "Excel Sheet Not created");
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											response.getWriter().println(resObj);
											break;
										}
									} else {
										exportedSuccess = false;
										//response.reset();
										response.setHeader("Content-type", "application/json");
										response.getWriter().println(ssinvocieObj);
										break;
									}

								}

							}
							if (exportedSuccess) {
								int lastRowNum = sheet.getLastRowNum();
								SXSSFRow summaryRow = sheet.createRow(lastRowNum+1);
								SXSSFCell grandTotal = summaryRow.createCell(0);
								grandTotal.setCellValue("Grand Total");
								Set<Entry<String, BigDecimal>> entrySet = totalSummarymap.entrySet();
								for(Map.Entry<String, BigDecimal> entry:entrySet){
									String key = entry.getKey();
									 if(headers.contains(key)){
										 int indexOf = headers.indexOf(key);
										 SXSSFCell createCell = summaryRow.createCell(indexOf);
										 BigDecimal bigDecimal = totalSummarymap.get(key);
										 bigDecimal=bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
										 double doubleValue = bigDecimal.doubleValue();
										 createCell.setCellValue(doubleValue);
										 //CellUtil.setAlignment(createCell, HorizontalAlignment.RIGHT);
									 }
									
								}
								
								if(!debug){
								response.setContentType("application/vnd.ms-excel");
								Date date=new Date(System.currentTimeMillis());
								String format2=null;
								try{
									SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
									Date parse = dateFormat.parse(date.toString());
									dateFormat = new SimpleDateFormat("yyyyddMMhhmmss");
									 format2 = dateFormat.format(parse);
								}catch(Exception ex){
									format2=date.toString();
								}
								response.setHeader("Content-Disposition", "attachment; filename="+entity+format2+".xls");
								//response.setHeader("Content-Disposition", "attachment; filename=Invoice.xls");
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								workbook.write(byteArrayOutputStream);
								response.setHeader("Content-Length", "" + byteArrayOutputStream.size());
								workbook.write(response.getOutputStream());
								response.flushBuffer();
								}else{
									File tmpDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
									File privFile = new File(tmpDir, "Invoice.xls");
									FileOutputStream fileStream=new FileOutputStream(privFile);
									workbook.write(fileStream);
									fileStream.flush();
									response.getWriter().println("File Size in bytes :"+FileUtils.sizeOf(privFile));
									Date processEnd=new Date(System.currentTimeMillis());
									privFile.deleteOnExit();
									if(debug){
										response.getWriter().println("Process Ended: "+processEnd);
									}	
								}
							}
						}else{
		                 // validation failed
							//response.reset();
							response.setHeader("Content-type", "application/json");
							JsonObject resObj=new JsonObject();
							resObj.addProperty("Message", "Total Records from Header is null or Empty");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(resObj);
						
						}
					}else{
						JsonObject resObj=new JsonObject();
						resObj.addProperty("Message", "LoginId missing in the input payload");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						response.getWriter().println(resObj);
					}
				} else {
					// validation failed
					//response.reset();
					response.setHeader("Content-type", "application/json");
					JsonObject resObj=new JsonObject();
					resObj.addProperty("Message", errorMessage);
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}

			} else {
				// empty input paylaod received
				//response.reset();
				response.setHeader("Content-type", "application/json");
				JsonObject resObj=new JsonObject();
				resObj.addProperty("Message", "Empty input payload Received");
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
			//response.reset();
			response.setHeader("Content-type", "application/json");
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Exception StackTrace", buffer.toString());
			response.getWriter().println(resObj);

		}
	
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
		
		/*
		String inputPayload = "", service = "", entity = "", oDataUrl = "", userName = "", password = "",
				sapclient = "", authMethod = "", destURL = "";
		JsonParser parser = new JsonParser();
		CommonUtils commonUtils = new CommonUtils();
		Properties props = new Properties();
		DestinationConfiguration destConfiguration = null;
		boolean debug = false,isCountQuery=false;
		String sessionID = "", loginMethod = "", customFilter = "", query = "",filters="";
		int skipCount=0,loopCount=0;
		SXSSFWorkbook workbook = null;
		try {
			//inputPayload = commonUtils.getGetBody(request, response);
			inputPayload=request.getParameter("SSInvoice");
			props.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
				JsonObject jsonInputPayload = (JsonObject) parser.parse(inputPayload);
				String errorMessage = validateInputpayload(jsonInputPayload, response);
				if (errorMessage.equalsIgnoreCase("")) {
					if (jsonInputPayload.has("debug")
							&& jsonInputPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
						debug = true;
					}
					service = jsonInputPayload.get("Service").getAsString();
					entity = jsonInputPayload.get("EntitySet").getAsString();
					destConfiguration = getDestinationURL(request, response);
					String loginID = commonUtils.getUserPrincipal(request, "name", response);
					
					if(jsonInputPayload.has("CPNo")&&!jsonInputPayload.get("CPNo").isJsonNull()&&!jsonInputPayload.get("CPNo").getAsString().equalsIgnoreCase("")){
						filters="CPNo"
					}
					
					if(debug){
						response.getWriter().println("Login ID:"+loginID);
					}

					sapclient = destConfiguration.getProperty("sap-client");
					authMethod = destConfiguration.getProperty("Authentication");
					destURL = destConfiguration.getProperty("URL");
					if (debug) {
						response.getWriter().println("sapclient:" + sapclient);
						response.getWriter().println("authMethod:" + authMethod);
						response.getWriter().println("destURL:" + destURL);
					}

					if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug)
							response.getWriter().println("URL:" + url);
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					} else {
						// loginMethod =
						// destConfiguration.getProperty("LoginMethod");

						String url = commonUtils.getDestinationURL(request, response, "URL");
						if (debug) {
							response.getWriter().println("url: " + url);
							response.getWriter().println("Auth Method: " + authMethod);
						}
						sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
					}

					if (debug)
						response.getWriter().println("sessionID:" + sessionID);

					//destURL = getDestinationURL(request, response, "URL");

					if (debug) {
						response.getWriter().println("ssfi Destination URL:" + destURL);
					}

					if (sapclient != null) {
						query = destURL + entity +"/$count"+"?sap-client=" + sapclient;
					} else {
						query = destURL + entity+"/$count";
					}

					JsonObject totalRoc = executeURL(query, response, request, sessionID,true,debug);
					
					int totalRecords = totalRoc.get("Message").getAsInt();
					if(debug){
						response.getWriter().println("Total Records:"+totalRecords);
					}
					
					if (totalRoc.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug)
								response.getWriter().println("Generating Session URL:" + url);
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						} else {
							String url = commonUtils.getDestinationURL(request, response, "URL");
							if (debug) {
								response.getWriter().println("url: " + url);
								response.getWriter().println("Auth Method: " + authMethod);
							}
							sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
						}
						
						if (sapclient != null) {
							query = destURL +"?sap-client=" + sapclient+"&$metadata";
						} else {
							query = destURL + "?$metadata";
						}
						
						if(debug){
							response.getWriter().println("Metadata URL:"+query);
						}
						//JsonObject metaDataObj = executeURL(query, response, request, sessionID, false, debug);
						JsonObject metaDataObj=executeMetadataURL(query, response, request, sessionID, debug);
						if (debug) {
							response.getWriter().println("metaDataObj:" + metaDataObj);
						}
						
						List<String> headers=new ArrayList<>();
						headers.add("InvoiceNo");
						headers.add("InvoiceDate");
						headers.add("CPNo");
						headers.add("CPName");
						headers.add("BeatCode");
						headers.add("BeatDesc");
						headers.add("SoldToID");
						headers.add("SoldToName");
						headers.add("NetAmount");
						headers.add("StatusID");
						headers.add("PaymentStatusID");
						headers.add("Source");
						headers.add("InvoiceTypeID");
						headers.add("InvoiceTypeDesc");
						headers.add("GSTIN");
						headers.add("GrossAmount");
						headers.add("PriTradeDisc");
						headers.add("SecTradeDisc");
						headers.add("CashDiscAmount");
						headers.add("SPLDiscountAmt");
						headers.add("TCSPerc");
						headers.add("TCSAmount");
						headers.add("AdditionalDiscAmt");
						headers.add("TaxableAmount");
						headers.add("Tax1");
						headers.add("Tax2");
						headers.add("Tax3");
						headers.add("Tax");
						headers.add("CreditNote");
						headers.add("OnInvDis");
						headers.add("DebitNote");
						headers.add("NetPayAmt");
						headers.add("TotDiscAmt");
						headers.add("CESS");
						headers.add("CashDiscRsn");
						headers.add("ExternalRefID");
						headers.add("CreatedOn");
						headers.add("CreatedAt");
						headers.add("ChangedOn");
						headers.add("ChangedAt");
						headers.add("InvoiceGUID");
						headers.add("SoldToCPGUID");
						workbook = new SXSSFWorkbook();
						workbook.setCompressTempFiles(true);
						SXSSFSheet sheet = createXlSheet(workbook, entity, headers, response, debug);
						if (debug) {
							response.getWriter().println("sheet:" + sheet);
							response.getWriter().println("sheet:" + sheet.getSheetName());
						}
						
						if (totalRecords > 0 && totalRecords <= BATCH_SIZE) {
							if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
								String url = commonUtils.getDestinationURL(request, response, "URL");
								if (debug)
									response.getWriter().println("Generating Session URL:" + url);
								sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							} else {
								String url = commonUtils.getDestinationURL(request, response, "URL");
								if (debug) {
									response.getWriter().println("url: " + url);
									response.getWriter().println("Auth Method: " + authMethod);
								}
								sessionID = commonUtils.createUserSession(request, response, url, loginID, debug);
							}

							if (sapclient != null) {
								query = destURL + entity + "?sap-client=" + sapclient
										+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
										+ skipCount + "&$top=" + BATCH_SIZE;
							} else {
								query = destURL + entity
										+ "?$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="+skipCount+"&$top="
										+ BATCH_SIZE;
							}
							if (debug) {
								response.getWriter().println("fetch Records from SSinvoices Table query: " + query);
							}
							
							JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false, debug);
							if (debug) {
								if(!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
								response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
							}
							if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
								if (sheet != null) {
									AtomicInteger rowNumber = new AtomicInteger(1);
									JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj, rowNumber,summaryList,debug);
									if(exportToXlSheet.get("Status").getAsString().equalsIgnoreCase("000001")){
									response.setContentType("application/vnd.ms-excel");
									response.setHeader("Content-Disposition", "attachment; filename=ssinvoice.xls");
									workbook.write(response.getOutputStream());
									response.flushBuffer();
									}else{
										response.getWriter().println(exportToXlSheet);
									}
								} else {
									response.reset();
									response.setHeader("Content-type", "application/json");
									JsonObject resObj = new JsonObject();
									resObj.addProperty("Message", "Excel Sheet Not created");
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									response.getWriter().println(resObj);
								}
							} else {
								response.reset();
								response.setHeader("Content-type", "application/json");
								response.getWriter().println(ssinvocieObj);
							}

						} else {
							// call batch wise
							loopCount = totalRecords / BATCH_SIZE;
							int rem = totalRecords % BATCH_SIZE;
							if (rem > 0) {
								loopCount++;
							}
							AtomicInteger rowNumber = new AtomicInteger(1);
							File tmpDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
							File privFile = new File(tmpDir, "ssinvoices.csv");
							boolean exportedSuccess=true;
							for (int i = 1; i <= loopCount; i++) {
								if (debug) {
									response.getWriter().println("Batch No:" + i);
								}
								if (i == 1) {
									if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug)
											response.getWriter().println("Generating Session URL:" + url);
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									} else {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug) {
											response.getWriter().println("url: " + url);
											response.getWriter().println("Auth Method: " + authMethod);
										}
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									}

									if (sapclient != null) {
										query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									} else {
										query = destURL + entity
												+ "?$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									}
									if (debug) {
										response.getWriter()
												.println("Fetch Records from SSinvoices Table Query:" + query);
									}

									JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false,
											debug);
									if (debug) {
										if (!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
											response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
									}

									if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
										ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
										if (sheet != null) {
											JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj,
													rowNumber, debug);
											if (!exportToXlSheet.get("Status").getAsString()
													.equalsIgnoreCase("000001")) {
												// workbook.write(response.getOutputStream());
												exportedSuccess = false;
												response.reset();
												response.setHeader("Content-type", "application/json");
												response.getWriter().println(exportToXlSheet);
												break;
											}

										} else {
											exportedSuccess = false;
											response.reset();
											response.setHeader("Content-type", "application/json");
											JsonObject resObj = new JsonObject();
											resObj.addProperty("Message", "Excel Sheet Not created");
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											response.getWriter().println(resObj);
											break;
										}
									} else {
										exportedSuccess = false;
										response.reset();
										response.setHeader("Content-type", "application/json");
										response.getWriter().println(ssinvocieObj);
										break;
									}

								} else {
									skipCount = skipCount + BATCH_SIZE;
									if (debug) {
										response.getWriter().println("skipCount: " + skipCount);
									}

									if (null != authMethod && authMethod.equalsIgnoreCase("BasicAuthentication")) {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug)
											response.getWriter().println("Generating Session URL:" + url);
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									} else {
										String url = commonUtils.getDestinationURL(request, response, "URL");
										if (debug) {
											response.getWriter().println("url: " + url);
											response.getWriter().println("Auth Method: " + authMethod);
										}
										sessionID = commonUtils.createUserSession(request, response, url, loginID,
												debug);
									}

									if (sapclient != null) {
										query = destURL + entity + "?sap-client=" + sapclient
												+ "&$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;

									} else {
										query = destURL + entity
												+ "?$format=json&$select=InvoiceNo,InvoiceDate,CPNo,CPName,BeatCode,BeatDesc,SoldToID,SoldToName,NetAmount,StatusID,PaymentStatusID,Source,InvoiceTypeID,InvoiceTypeDesc,GSTIN,GrossAmount,PriTradeDisc,SecTradeDisc,CashDiscAmount,SPLDiscountAmt,TCSPerc,TCSAmount,AdditionalDiscAmt,TaxableAmount,Tax1,Tax2,Tax3,Tax,CreditNote,OnInvDis,DebitNote,NetPayAmt,TotDiscAmt,CESS,CashDiscRsn,ExternalRefID,CreatedOn,CreatedAt,ChangedOn,ChangedAt,InvoiceGUID,SoldToCPGUID&$skip="
												+ skipCount + "&$top=" + BATCH_SIZE;
									}
									if (debug) {
										response.getWriter().println("query:" + query);
									}
									JsonObject ssinvocieObj = executeURL(query, response, request, sessionID, false,
											debug);

									if (debug) {
										if (!ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001"))
											response.getWriter().println("ssinvocieObj:" + ssinvocieObj);
									}

									if (ssinvocieObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
										ssinvocieObj = ssinvocieObj.get("Message").getAsJsonObject();
										if (sheet != null) {
											JsonObject exportToXlSheet = exportToXlSheet(workbook, sheet, ssinvocieObj,
													rowNumber, debug);

											if (!exportToXlSheet.get("Status").getAsString()
													.equalsIgnoreCase("000001")) {
												//workbook.write(response.getOutputStream())
												exportedSuccess = false;
												response.reset();
												response.setHeader("Content-type", "application/json");
												response.getWriter().println(exportToXlSheet);
												break;
											
											}
										} else {
											exportedSuccess = false;
											response.reset();
											response.setHeader("Content-type", "application/json");
											JsonObject resObj = new JsonObject();
											resObj.addProperty("Message", "Excel Sheet Not created");
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											response.getWriter().println(resObj);
											break;
										}
									} else {
										exportedSuccess = false;
										response.reset();
										response.setHeader("Content-type", "application/json");
										response.getWriter().println(ssinvocieObj);
										break;
									}

								}

							}
							if (exportedSuccess && !debug) {
								response.setContentType("application/vnd.ms-excel");
								response.setHeader("Content-Disposition", "attachment; filename=ssinvoice.xls");
								workbook.write(response.getOutputStream());
								response.flushBuffer();
							}
						}
					}else{
						response.getWriter().println(totalRoc);
					}

					

				} else {
					// validation failed
					response.reset();
					response.setHeader("Content-type", "application/json");
					
					JsonObject resObj=new JsonObject();
					resObj.addProperty("Message", errorMessage);
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(resObj);
				}

			} else {
				// empty input paylaod received
				response.reset();
				response.setHeader("Content-type", "application/json");
				JsonObject resObj=new JsonObject();
				resObj.addProperty("Message", "Empty input payload Received");
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
			response.reset();
			response.setHeader("Content-type", "application/json");

			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Exception StackTrace", buffer.toString());
			response.getWriter().println(resObj);

		}
	
		
	*/}

	private String validateInputpayload(JsonObject jsonInputPayload, HttpServletResponse response) {
		String message = "";
		try {
			if (jsonInputPayload.has("Service")) {
				if (jsonInputPayload.get("Service").isJsonNull()
						|| jsonInputPayload.get("Service").getAsString().equalsIgnoreCase("")) {
					message = "Service Field Is Empty in the input Payload";
				}
			} else {
				message = "Input Payload doesn't contains Service field";
			}

			if (jsonInputPayload.has("EntitySet")) {
				if (jsonInputPayload.get("EntitySet").isJsonNull()
						|| jsonInputPayload.get("EntitySet").getAsString().equalsIgnoreCase("")) {
					message = "EntitySet Field Is Empty in the input Payload";
				}
			} else {
				message = "Input Payload doesn't contains EntitySet field";
			}

			return message;

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			return buffer.toString();

		}

	}

	public JsonObject executeURL(String executeURL, HttpServletResponse response, HttpServletRequest request, String sessionID,
			boolean isTotalRocQry,boolean debug) throws IOException {
		Destination destConfiguration = null;
		String authMethod = "", destURL = "", userName = "", password = "", authParam = "";
		String returnMessage = "", basicAuth = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		HttpGet getRecords = null;
		HttpEntity pgPymntTxnGetEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try {
			/*Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("Tenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("Tenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("Tenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */
			destConfiguration = getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
				/*if (debug) {
					response.getWriter()
							.println("principalPropagationHeader.getName(): " + principalPropagationHeader.getName());
					response.getWriter()
							.println("principalPropagationHeader.getValue(): " + principalPropagationHeader.getValue());
				}*/
			}

			/*String proxyType = destConfiguration.getProperty("ProxyType");
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if (debug) {
				response.getWriter().println("proxyType: " + proxyType);
				response.getWriter().println("proxyHost: " + proxyHost);
				response.getWriter().println("proxyPort: " + proxyPort);
				response.getWriter().println("execute URL: " + executeURL);
				response.getWriter().println("Session ID: " + sessionID);
			} 

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();*/

			getRecords = new HttpGet(executeURL);
			// getRecords.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			getRecords.setHeader("content-type", "application/json");
			getRecords.setHeader("Accept", "application/json");
			getRecords.setHeader("x-arteria-loginid", sessionID);

			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				getRecords.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				getRecords.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(getRecords);
			HttpResponse httpResponse = client.execute(getRecords);

			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("statusCode: " + statusCode);

			pgPymntTxnGetEntity = httpResponse.getEntity();

			String retSrc = EntityUtils.toString(pgPymntTxnGetEntity);

			if (debug&&statusCode!=200)
				response.getWriter().println("retSrc: " + retSrc);
			if (statusCode == 200) {
				if (isTotalRocQry) {
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("ErrorCode", "");
					jsonObject.addProperty("Message", retSrc);
					jsonObject.addProperty("Status", "000001");
					return jsonObject;
				} else {
					JsonParser parser = new JsonParser();
					JsonObject ssinvoiceObj = (JsonObject) parser.parse(retSrc);
					if (debug&& statusCode!=200) {
						response.getWriter().println("ssinvoiceObj:" + ssinvoiceObj);
					}
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("ErrorCode", "");
					jsonObject.add("Message", ssinvoiceObj);
					jsonObject.addProperty("Status", "000001");

					return jsonObject;
				}
			} else {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("ErrorCode", "J002");
				jsonObject.addProperty("Message", retSrc);
				jsonObject.addProperty("Status", "000002");
				return jsonObject;
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug) {
				response.getWriter().println(buffer.toString());
				response.getWriter().println(ex.getLocalizedMessage());
				response.getWriter().println(ex.getMessage());

			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("ErrorCode", "J002");
			jsonObject.addProperty("Message", buffer.toString());
			jsonObject.addProperty("Status", "000002");
			jsonObject.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			return jsonObject;
		}
	}

	public String getDestinationURL(HttpServletRequest request, HttpServletResponse response, String paramName)
			throws IOException {
		// get the destination name from the request
		// String destinationName = "";
		// LOGGER.info("4. destination name from request: "+destinationName);
		// check destination null condition
		String destinationName = "ssfi";
		String url = null;
		try {
			// look up the connectivity configuration API
			// "connectivityConfiguration"
			// Context ctxConnConfgn = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn
			// 		.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
			// LOGGER.info("5. destination configuration object created");
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
								.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
								.tryGetDestination(destinationName, options);
			Destination destConfiguration = destinationAccessor.get();
			// Get the destination URL
			if (destConfiguration != null) {
				if (paramName.equalsIgnoreCase("URL")) {
					url = destConfiguration.get("URL").get().toString();
				} else if (paramName.equalsIgnoreCase("Authentication")) {
					url = destConfiguration.get("Authentication").get().toString();
				}
			}
		} catch (Exception e) {
			// Lookup of destination failed
			String errorMessage = "Lookup of destination failed with reason: " + e.getMessage() + ". See "
					+ "logs for details. Hint: Make sure to have the destination " + destinationName + " configured.";
			// LOGGER.error("Lookup of destination failed", e);
			// response.getWriter().println(" " + errorMessage);
		}
		return url;
	}

	public Destination getDestinationURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get the destination name from the request
		String destinationName = "";
//			LOGGER.info("4. destination name from request: "+destinationName);	
		//check destination null condition
		destinationName = "ssfi";
//		DestinationConfiguration destConfiguration = null;
		Destination destConfiguration = null;
		try {
			// look up the connectivity configuration API "connectivityConfiguration"
//			Context ctxConnConfgn = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn.lookup("java:comp/env/connectivityConfiguration");
//			destConfiguration = configuration.getConfiguration(destinationName);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(destinationName, options);
			destConfiguration = destinationAccessor.get();
//				LOGGER.info("5. destination configuration object created");	
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format("Destination %s is not found. Hint:"
							+ " Make sure to have the destination configured.", destinationName));
//				LOGGER.error("Lookup of destination failed", e);
			//response.getWriter().println(" " +  errorMessage);
		} 
		return destConfiguration;
	}

	/* public DestinationConfiguration getDestinationURL(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// get the destination name from the request
		String destinationName = "";
		// LOGGER.info("4. destination name from request: "+destinationName);
		// check destination null condition
		destinationName = "ssfi";
		DestinationConfiguration destConfiguration = null;
		try {
			// look up the connectivity configuration API
			// "connectivityConfiguration"
			Context ctxConnConfgn = new InitialContext();
			//ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctxConnConfgn
					.lookup("java:comp/env/connectivityConfiguration");
			destConfiguration = configuration.getConfiguration(destinationName);
			// LOGGER.info("5. destination configuration object created");
		} catch (Exception e) {
			// Lookup of destination failed
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					String.format(
							"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
							DestinationUtils.PCGW_UTILS_OP));
			// LOGGER.error("Lookup of destination failed", e);
			// response.getWriter().println(" " + errorMessage);
		}
		return destConfiguration;
	} */
	
	public JsonObject exportToXlSheet(SXSSFWorkbook workbook,SXSSFSheet sheet,JsonObject ssinvoiceObj,AtomicInteger rowNumber,List<String> columnList,Map<String,BigDecimal> summaryFields,boolean debug) {
		JsonObject resObj=new JsonObject();
		try {
			//AtomicInteger keyNum = new AtomicInteger(0);
			CellStyle style = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			style.setWrapText(true);
			JsonArray ssinvArry = ssinvoiceObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
			Cell cell=null;
			for (int i = 0; i < ssinvArry.size(); i++) {
				JsonObject ssInvObj = ssinvArry.get(i).getAsJsonObject();
				AtomicInteger cellNum = new AtomicInteger(0);
				Row row = sheet.createRow(rowNumber.getAndIncrement());
				for(int j=0;j<columnList.size();j++){
					cell = row.createCell(cellNum.getAndIncrement());
					if(!ssInvObj.get(columnList.get(j)).isJsonNull()&&!ssInvObj.get(columnList.get(j)).getAsString().equalsIgnoreCase("")){
						//cell.setCellValue(ssInvObj.get(columnList.get(j)).getAsString());
						String value = ssInvObj.get(columnList.get(j)).getAsString();
						if(value.contains("/Date")){
							Date date = convertLongToDate(value);
							style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
							cell.setCellValue(date);
							cell.setCellStyle(style);
						}else{
							// check for numeric or String values
							if (value.matches("-?\\d+(\\.\\d+)?")) {
								if (value.contains(".")) {
									try {
										double parseDouble = Double.parseDouble(value);
										cell.setCellValue(parseDouble);
									} catch (Exception ex) {
										cell.setCellValue(value);
									}
								} else {
									try {
										int intValue = Integer.parseInt(value);
										cell.setCellValue(intValue);
									} catch (Exception ex) {
										cell.setCellValue(value);
									}

								}
							} else {
								cell.setCellValue(value);
							}
							
							
						}
						
						try{
							BigDecimal bigDecimal = new BigDecimal(value);
							BigDecimal totalValue = summaryFields.get(columnList.get(j));
							totalValue=totalValue.add(bigDecimal);
							summaryFields.put(columnList.get(j), totalValue);
						}catch(Exception ex){
							
						}
					}else{
						cell.setCellValue("");
					}
				}
				
				/*Iterator<Entry<String, JsonElement>> iterator = ssInvObj.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, JsonElement> next = iterator.next();
					String key = next.getKey();
					if (!key.equalsIgnoreCase("__metadata")) {
						if (ssInvObj.get(key).isJsonNull() && !ssInvObj.get(key).getAsString().equalsIgnoreCase("")) {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue(ssInvObj.get(key).getAsString());
						} else {
							cell = row.createCell(cellNum.getAndIncrement());
							cell.setCellValue(ssInvObj.get(key).getAsString());
						}
					}
				}*/
				
				/*if (ssInvObj.has("InvoiceNo") && !ssInvObj.get("InvoiceNo").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("InvoiceNo").getAsString());
					if(summaryColumns.contains("InvoiceNo")){
						String inv = ssInvObj.get("InvoiceNo").getAsString();
						try{
							double parseDouble = Double.parseDouble(inv);
							totalCount=totalCount.add(new BigDecimal(parseDouble));
						}catch(NumberFormatException ex){
							
						}
					}
				} else {
					cell.setCellValue("");
				}
				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("InvoiceDate") && !ssInvObj.get("InvoiceDate").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("InvoiceDate").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CPNo") && !ssInvObj.get("CPNo").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CPNo").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CPName") && !ssInvObj.get("CPName").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CPName").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("BeatCode") && !ssInvObj.get("BeatCode").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("BeatCode").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("BeatDesc") && !ssInvObj.get("BeatDesc").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("BeatDesc").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("SoldToID") && !ssInvObj.get("SoldToID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("SoldToID").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("SoldToName") && !ssInvObj.get("SoldToName").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("SoldToName").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("NetAmount") && !ssInvObj.get("NetAmount").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("NetAmount").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("StatusID") && !ssInvObj.get("StatusID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("StatusID").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("PaymentStatusID") && !ssInvObj.get("PaymentStatusID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("PaymentStatusID").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("Source") && !ssInvObj.get("Source").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("Source").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("InvoiceTypeID") && !ssInvObj.get("InvoiceTypeID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("InvoiceTypeID").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("InvoiceTypeDesc") && !ssInvObj.get("InvoiceTypeDesc").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("InvoiceTypeDesc").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("GSTIN") && !ssInvObj.get("GSTIN").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("GSTIN").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("GrossAmount") && !ssInvObj.get("GrossAmount").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("GrossAmount").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("PriTradeDisc") && !ssInvObj.get("PriTradeDisc").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("PriTradeDisc").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("SecTradeDisc") && !ssInvObj.get("SecTradeDisc").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("SecTradeDisc").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CashDiscAmount") && !ssInvObj.get("CashDiscAmount").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CashDiscAmount").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("SPLDiscountAmt") && !ssInvObj.get("SPLDiscountAmt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("SPLDiscountAmt").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("TCSPerc") && !ssInvObj.get("TCSPerc").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("TCSPerc").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("TCSAmount") && !ssInvObj.get("TCSAmount").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("TCSAmount").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("AdditionalDiscAmt") && !ssInvObj.get("AdditionalDiscAmt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("AdditionalDiscAmt").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("TaxableAmount") && !ssInvObj.get("TaxableAmount").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("TaxableAmount").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("Tax1") && !ssInvObj.get("Tax1").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("Tax1").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("Tax2") && !ssInvObj.get("Tax2").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("Tax2").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("Tax3") && !ssInvObj.get("Tax3").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("Tax3").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("Tax") && !ssInvObj.get("Tax").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("Tax").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CreditNote") && !ssInvObj.get("CreditNote").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CreditNote").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("OnInvDis") && !ssInvObj.get("OnInvDis").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("OnInvDis").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("DebitNote") && !ssInvObj.get("DebitNote").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("DebitNote").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("NetPayAmt") && !ssInvObj.get("NetPayAmt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("NetPayAmt").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("TotDiscAmt") && !ssInvObj.get("TotDiscAmt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("TotDiscAmt").getAsString());
				} else {
					cell.setCellValue("");
				}
				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CESS") && !ssInvObj.get("CESS").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CESS").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CashDiscRsn") && !ssInvObj.get("CashDiscRsn").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CashDiscRsn").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("ExternalRefID") && !ssInvObj.get("ExternalRefID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("ExternalRefID").getAsString());
				} else {
					cell.setCellValue("");
				}
				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CreatedOn") && !ssInvObj.get("CreatedOn").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CreatedOn").getAsString());
				} else {
					cell.setCellValue("");
				}
				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("CreatedAt") && !ssInvObj.get("CreatedAt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("CreatedAt").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("ChangedOn") && !ssInvObj.get("ChangedOn").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("ChangedOn").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("ChangedAt") && !ssInvObj.get("ChangedAt").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("ChangedAt").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.getAndIncrement());
				if (ssInvObj.has("InvoiceGUID") && !ssInvObj.get("InvoiceGUID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("InvoiceGUID").getAsString());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(cellNum.get());
				if (ssInvObj.has("SoldToCPGUID") && !ssInvObj.get("SoldToCPGUID").isJsonNull()) {
					cell.setCellValue(ssInvObj.get("SoldToCPGUID").getAsString());
				} else {
					cell.setCellValue("");
				}*/

			}
			resObj.addProperty("Message", "Sheet created Successfully");
			resObj.addProperty("ErrorCode", "");
			resObj.addProperty("Status", "000001");

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionStackTrace", buffer.toString());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
		}
		return resObj;
	}
	
	public SXSSFSheet createXlSheet(SXSSFWorkbook workbook,String sheetName,List<String> headers,HttpServletResponse response,boolean debug)throws IOException{
		SXSSFSheet sheet=null;
		try{
			sheet = (SXSSFSheet) workbook.createSheet(sheetName);
			sheet.setRandomAccessWindowSize(100);
			CellStyle cellStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			cellStyle.setFont(font);
			
			/*sheet.setColumnWidth(0, 6000); // cell width
			sheet.setColumnWidth(1, 4000);// cell height
*/			Row header = sheet.createRow(0);
			AtomicInteger keyNum = new AtomicInteger(0);
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);
			for(int i=0;i<headers.size();i++){
				Cell createCell = header.createCell(keyNum.getAndIncrement());
				createCell.setCellValue(headers.get(i));
				createCell.setCellStyle(cellStyle);
			}
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.size()-1));
		} catch (Exception ex) {
			if (debug) {
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				response.getWriter().println("Exception message: " + ex.getLocalizedMessage());
				response.getWriter().println("Exception StackTrace: " + buffer.toString());
			}
		}
		
		return sheet;
	}
	
	public JsonObject executeMetadataURL(String executeURL, HttpServletResponse response, HttpServletRequest request, String sessionID,boolean debug) throws IOException {
		Destination destConfiguration = null;
		String authMethod = "", destURL = "", userName = "", password = "", authParam = "";
		String returnMessage = "", basicAuth = "";
		byte[] encodedByte = null;
		AuthenticationHeader principalPropagationHeader = null;
		CommonUtils commonUtils = new CommonUtils();
		HttpGet getRecords = null;
		HttpEntity pgPymntTxnGetEntity = null;
		// CloseableHttpClient closableHttpClient = null;
		try {
			/* Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			if (debug) {
				response.getWriter().println("Tenant.getId: " + tenantContext.getTenant().getAccount().getId());
				response.getWriter()
						.println("Tenant.getCustomerId: " + tenantContext.getTenant().getAccount().getCustomerId());
				response.getWriter().println("Tenant.getName: " + tenantContext.getTenant().getAccount().getName());
			} */
			destConfiguration = getDestinationURL(request, response);
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination(DestinationUtils.PCGW_UTILS_OP, options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			authMethod = destConfiguration.get("Authentication").get().toString();
			destURL = destConfiguration.get("URL").get().toString();
			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				userName = destConfiguration.get("User").get().toString();
				password = destConfiguration.get("Password").get().toString();
				authParam = userName + ":" + password;
				// encodedByte = Base64.getEncoder().encode(authParam.getBytes());
				// String encodedStr = new String(encodedByte);
				basicAuth = "Bearer " + authParam;
			} else {
				principalPropagationHeader = commonUtils.getPrincipalPropagationAuthHdr(response, debug);
				if (debug) {
					response.getWriter()
							.println("principalPropagationHeader.getName(): " + principalPropagationHeader.getName());
					response.getWriter()
							.println("principalPropagationHeader.getValue(): " + principalPropagationHeader.getValue());
				}
			}

			/* String proxyType = destConfiguration.getProperty("ProxyType");
			String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
			int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
			if (debug) {
				response.getWriter().println("proxyType: " + proxyType);
				response.getWriter().println("proxyHost: " + proxyHost);
				response.getWriter().println("proxyPort: " + proxyPort);
				response.getWriter().println("execute URL: " + executeURL);
				response.getWriter().println("Session ID: " + sessionID);
			}

			HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			closableHttpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build(); */
			getRecords = new HttpGet(executeURL);
			// getRecords.setHeader("SAP-Connectivity-ConsumerAccount", tenantContext.getTenant().getAccount().getId());
			getRecords.setHeader("content-type", "application/xml");
			getRecords.setHeader("Accept", "application/xml");
			getRecords.setHeader("x-arteria-loginid", sessionID);

			if (authMethod.equalsIgnoreCase("BasicAuthentication")) {
				getRecords.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
			} else {
				getRecords.setHeader(principalPropagationHeader.getName(), principalPropagationHeader.getValue());
			}

			// HttpResponse httpResponse = closableHttpClient.execute(getRecords);
			HttpResponse httpResponse = client.execute(getRecords);

			if (debug)
				response.getWriter().println("httpResponse: " + httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (debug)
				response.getWriter().println("statusCode: " + statusCode);

			pgPymntTxnGetEntity = httpResponse.getEntity();

			String retSrc = EntityUtils.toString(pgPymntTxnGetEntity);

			if (debug)
				response.getWriter().println("retSrc: " + retSrc);
			if (statusCode == 200) {
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("ErrorCode", "");
					jsonObject.addProperty("Message", retSrc);
					jsonObject.addProperty("Status", "000001");
					return jsonObject;
			} else {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("ErrorCode", "J002");
				jsonObject.addProperty("Message", retSrc);
				jsonObject.addProperty("Status", "000002");
				return jsonObject;
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			if (debug) {
				response.getWriter().println(buffer.toString());
				response.getWriter().println(ex.getLocalizedMessage());
				response.getWriter().println(ex.getMessage());

			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("ErrorCode", "J002");
			jsonObject.addProperty("Message", buffer.toString());
			jsonObject.addProperty("Status", "000002");
			jsonObject.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			return jsonObject;
		}
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

}

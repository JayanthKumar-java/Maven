package com.arteriatech.cf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.mail.AuthenticationFailedException;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PaymentAdvice extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SERVLETPATH = "/SendPaymentAdvice";
	private String date = "";
	private String aggId = "";
	private String oDataUrl = "";
	private String userName = "";
	private String password = "";
	private String platformEmail = "";
	private String emailPassword = "";

	@Override
	public void init() throws ServletException {
		CommonUtils commonUtils = new CommonUtils();
		try {
			oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			emailPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PLATFORM_EMAIL);
			platformEmail = commonUtils.getODataDestinationProperties("emailid", DestinationUtils.PLATFORM_EMAIL);
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			throw new RuntimeException(resObj.toString());

		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject paymentInfo = new JsonObject();
		JsonObject paymentItemInfo = new JsonObject();
		String executeURL = "", id = "", cpGuid = "", servletPath = "";
		String paymentId = null, emailId = null;
		JsonObject paymentInfoObj = new JsonObject();
		boolean debug = false;
		CommonUtils commonUtils = new CommonUtils();
		Set<String> uniqueCpGuids = new HashSet<>();
		Map<String, JsonObject> mapCpguid = new HashMap<>();
		List<JsonObject> updatedPayments = new ArrayList<>();
		
		servletPath = request.getServletPath();
		final String path = servletPath;
		Properties properties = new Properties();

		if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
			debug = true;
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
		}
		
		date = request.getParameter("date");
		aggId = request.getParameter("AggregatorID");
		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		if (servletPath.equalsIgnoreCase(SERVLETPATH)) {
			try {
				if (aggId != null &&!aggId.equalsIgnoreCase("") &&date != null && !date.equalsIgnoreCase("")) {
					String userPass = userName + ":" + password;
					// String loginUser = commonUtils.getUserPrincipal(request, "name", response);
					String loginUser = "";
					String appLogId = commonUtils.generateGUID(36);
					AtomicInteger msgNo = new AtomicInteger(1);
					JsonObject applicationLog = new JsonObject();
					applicationLog.addProperty("ID", appLogId);
					applicationLog.addProperty("AggregatorID", aggId);
					applicationLog.addProperty("LogObject", "Java");
					applicationLog.addProperty("LogSubObject", "PaymentAdvice");
					String logDate = commonUtils.getCurrentDate("yyyy-MM-dd");
					applicationLog.addProperty("LogDate", logDate);
					String logTime = commonUtils.getCurrentTime();
					commonUtils.getCreatedAtTime();
					applicationLog.addProperty("LogTime", logTime);
					applicationLog.addProperty("Program", request.getServletPath());
					applicationLog.addProperty("ProcessRef2", "");
					applicationLog.addProperty("ProcessID", "");
					applicationLog.addProperty("CorrelationID", "");
					applicationLog.addProperty("CreatedBy", loginUser);
					applicationLog.addProperty("CreatedOn", logDate);
					applicationLog.addProperty("CreatedAt", logTime);
					applicationLog.addProperty("SourceReferenceID", "");
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							AtomicInteger stepNo = new AtomicInteger(1);
							ODataLogs oDataLogs = new ODataLogs();
							JsonArray appLogMessag = new JsonArray();
							try {
								boolean debug = false;
								date = formateDateIn(date);
								String executeURL = oDataUrl + "APPayments?$filter=AggregatorID%20eq%20%27" + aggId + "%27%20and%20ChangedOn%20eq%20datetime%27" + date + "T00:00:00%27";
								JsonObject paymentInfo = commonUtils.executeURL(executeURL, userPass, response);
								if (paymentInfo != null && paymentInfo.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									JsonArray appPaymentList = paymentInfo.get("d").getAsJsonObject().get("results").getAsJsonArray();
									oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "Input Payload", "AggregatorID=" + aggId + " Date=" + date, "Total APPayments Records :" + appPaymentList.size());
									// Take the CPGuids if UTRNo is not Null
									// and not empty String
									for (int i = 0; i < appPaymentList.size(); i++) {
										JsonObject appayment = appPaymentList.get(i).getAsJsonObject();
										if (!appayment.get("UTRNo").isJsonNull() && !appayment.get("UTRNo").getAsString().equalsIgnoreCase("") && !appayment.get("CPGuid").isJsonNull() && !appayment.get("CPGuid").getAsString().equalsIgnoreCase("")) {
											String cpGuid=appayment.get("CPGuid").getAsString();
											String cpType=appayment.get("CPType").getAsString();
											String uniqueCpGuidAndCptype=cpGuid+":"+cpType;
											uniqueCpGuids.add(uniqueCpGuidAndCptype);
											//uniqueCpGuids.add(new UniqueCpGuidsAndCpType(cpGuid, cpType));
											updatedPayments.add(appayment);
										}
									}
									oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "Total Unique CPguids:" + uniqueCpGuids.size(), "Total Valid APPayments Records :" + updatedPayments.size());

									// take the recipient email address from the
									// SupplyChainPartners

									Iterator<String> iterator = uniqueCpGuids.iterator();
									while (iterator.hasNext()) {
										String uniqueCpGudAndCpType = iterator.next();
										String[] split = uniqueCpGudAndCpType.split(":");
										String cpGuid=split[0];
										String cpType=split[1];
										if(cpType.equalsIgnoreCase("01")){
											cpType="000003";
										}else{
											cpType="000002";
										}
										
										executeURL = oDataUrl + "SupplyChainPartners?$select=SCPType,SCPGuid,EmailID,Name1,Name2,Name3,Name4&$filter=AggregatorId%20eq%20%27" + aggId + "%27%20and%20SCPGuid%20eq%20%27" + cpGuid + "%27%20and%20SCPType%20eq%20%27"+cpType+"%27";
										JsonObject scpObj = commonUtils.executeURL(executeURL, userPass, response);
										if (scpObj != null && !scpObj.has("error") && scpObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
											JsonObject scpJson = scpObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
											if (!scpJson.get("EmailID").isJsonNull() && !scpJson.get("EmailID").getAsString().equalsIgnoreCase("")) {
												if (mapCpguid.get(uniqueCpGudAndCpType) == null) {
													mapCpguid.put(uniqueCpGudAndCpType, scpJson);
												}
											} else {
												oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "SupplyChainPartners", "", "", "CPGUid:" + cpGuid, "SupplyChainPartners Table EmailId does not  exist for the cpGuid:" + cpGuid);
												JsonObject sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, "EmailId doesn't exist in the SupplyChainPartners Table", aggId, cpGuid, response, request, debug, properties);
												if (sendEmail != null && sendEmail.has("Status") && !sendEmail.get("Status").getAsString().equalsIgnoreCase("000001")) {
													oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, msgNo.getAndIncrement() + "", "", "", "", "SendPaymentAdvice Exception " + cpGuid, sendEmail + "");
												}
											}

										} else {
											JsonObject sendEmail = null;
											if (scpObj.has("error")) {
												oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "SupplyChainPartners", "", "", "CPGUid:" + cpGuid, scpObj + "");
												sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, scpObj + "", aggId, cpGuid, response, request, debug, properties);
											} else {
												oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "SupplyChainPartners", "", "", "CPGUid:" + cpGuid, "Records does not exist in the SupplyChainPartners Table");
												sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, "Record Not Exist in the SupplyChainPartners Table", aggId, cpGuid, response, request, debug, properties);
											}

											if (sendEmail != null && sendEmail.has("Status") && !sendEmail.get("Status").getAsString().equalsIgnoreCase("000001")) {
												oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "SupplyChainPartners", "", "", "SendPaymentAdvice Exception " + cpGuid, sendEmail + "");
											}
										}

									}
									
									oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "Total EmailIds:" + mapCpguid.size(), "");
									
									if (!updatedPayments.isEmpty()) {
										if (!mapCpguid.isEmpty()) {
											
											Map<String, List<JsonObject>> appaymentsMap = updatedPayments.stream().collect(Collectors.groupingBy(paymentObj->paymentObj.get("CPGuid").getAsString()+":"+paymentObj.get("CPType").getAsString()));
											appaymentsMap.forEach((key,appamentLst)->{
												/*try {
													for (int i = 0; i < appamentLst.size(); i++) {
														oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", i+"th appamentLst element", "", key, ""+appamentLst.get(i).getAsJsonObject(), "");	
													}
												} catch (Exception e1) {
													e1.printStackTrace();
												}*/
												final List<String> fileList=new ArrayList<>();
												JsonObject sendEmail = null;
												try {
												String uniqueKeys=key;
												String[] split = uniqueKeys.split(":");
													oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "Total Records for the  CPGuid "+split[0], appamentLst.size()+"", "", "", "");
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												JsonObject scpObj=null;
												String cpGuid=null;
												String cpType=null;
													for (int i = 0; i < appamentLst.size(); i++) {
														try {
															cpGuid = appamentLst.get(i).getAsJsonObject().get("CPGuid").getAsString();
															cpType = appamentLst.get(i).getAsJsonObject().get("CPType").getAsString();
															String uniqueCpGuidAndType = cpGuid + ":" + cpType;
															scpObj = mapCpguid.get(uniqueCpGuidAndType);
															if (scpObj != null) {
																// String id = updatedPayments.get(i).getAsJsonObject().get("ID").getAsString();
																String id = appamentLst.get(i).getAsJsonObject().get("ID").getAsString();
																String executeurl = oDataUrl + "APPaymentItemDetails?$filter=PaymentGUID%20eq%20%27" + id + "%27";
																JsonObject paymentItemInfo = commonUtils.executeURL(executeurl, userPass, response);
																if (paymentItemInfo != null && !paymentItemInfo.has("error") && paymentItemInfo.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
																	// printInvoice(paymentItemInfo.get("d").getAsJsonObject().get("results").getAsJsonArray(), updatedPayments.get(i).getAsJsonObject(), request, response, scfObj, path, debug, oDataLogs, msgNo, appLogId, appLogMessag, aggId);
																	/*try {
																		oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "Payments and SCPObject Used", "For ID: "+uniqueCpGuidAndType, "", ""+appamentLst.get(i).getAsJsonObject(), ""+scpObj);
																	} catch (Exception e) {
																		e.printStackTrace();
																	}*/
																	
																	JsonObject sendPaymentAdvice = sendPaymentAdvice(paymentItemInfo.get("d").getAsJsonObject().get("results").getAsJsonArray(), updatedPayments.get(i).getAsJsonObject(), request, response, scpObj,debug, oDataLogs, msgNo, appLogId, appLogMessag, aggId, fileList);
                                                                   if(sendPaymentAdvice.get("Status").getAsString().equalsIgnoreCase("000002")){
                                                                	   String message = sendPaymentAdvice.get("Message").getAsString(); 
                                                                	   oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "APPaymentItemDetails", "", "", "PaymentGUID:" + id, message + "");
																		sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, message + "", aggId, cpGuid, response, request, debug, properties);
                                                                   }
																} else {
																	if (paymentItemInfo.has("error")) {
																		oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "APPaymentItemDetails", "", "", "PaymentGUID:" + id, paymentItemInfo + "");
																		sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, paymentItemInfo + "", aggId, cpGuid, response, request, debug, properties);
																	} else {
																		oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "APPaymentItemDetails", "", "", "PaymentGUID:" + id, "Records does not exist in the APPaymentItemDetails Table");
																		sendEmail = commonUtils.sendEmail("SendPaymentAdvice Exception :" + cpGuid, userName, password, oDataUrl, platformEmail, emailPassword, "Record Not Exist in the APPaymentItemDetails Table", aggId, cpGuid, response, request, debug, properties);
																	}
																	if (!sendEmail.get("Status").getAsString().equalsIgnoreCase("000001")) {
																		oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "APPaymentItemDetails", "", "", "SendPaymentAdvice Exception " + cpGuid, sendEmail + "");
																	}
																}
															}
													} catch (Exception ex) {
														StringBuffer buffer = new StringBuffer(ex.getClass().getCanonicalName() + "--->");
														if (ex.getLocalizedMessage() != null) {
															buffer.append(ex.getLocalizedMessage() + "--->");
														}
														StackTraceElement[] stackTrace = ex.getStackTrace();
														for (int k = 0; k < stackTrace.length; k++) {
															buffer.append(stackTrace[i]);
														}
														try {
															oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "APPaymentItemDetails", "", "", "", buffer.toString());
														} catch (Exception e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}

													}
												}	
												if (fileList.size() > 0) {
													StringBuilder emailSub = new StringBuilder();
													emailSub.append("Reg: Payment Advice for ").append(cpGuid);
													try {
														oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "CpGuid: "+cpGuid, "CPType"+cpType, "Recipient EmailID: "+scpObj.get("EmailID").getAsString(), "", "Total Attachments files: "+fileList.size());
														sendEmail(scpObj, debug, response, fileList, emailSub.toString(), oDataLogs, stepNo, appLogId, appLogMessag, aggId, request);
													} catch (Exception e) {

													}
												}
											});	
											oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "", "Process Completed");

										} else {

											oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "Recipient EmailId Not Found", "Process Completed");

										}

									} else {
										oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "Valid APPayments Records Not Found", "Process Completed");
									}

								} else {
									oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", "", "", "Records Not Found In Appayments Table", "Process Completed");

								}
							} catch (Exception ex) {
								StackTraceElement[] stackTrace = ex.getStackTrace();
								StringBuffer buffer = new StringBuffer();
								for (int i = 0; i < stackTrace.length; i++) {
									buffer.append(stackTrace[i]);
								}
								try {
									oDataLogs.createApplicationLogMsgOnEvent(request, response, false, appLogMessag, appLogId, stepNo.getAndIncrement() + "", "", ex.getCause().getLocalizedMessage(), ex.getLocalizedMessage(), buffer.toString(), "Process Completed");
								} catch (Exception e) {
									// we need to capture

								}

							}
							JsonObject insertIntoLogsOnEvent = commonUtils.insertIntoLogsOnEvent(response, aggId, applicationLog, appLogMessag, false);
						}

					};

					Thread thread = new Thread(runnable);
					thread.start();
					JsonObject resObj = new JsonObject();
					resObj.addProperty("Status", "000001");
					resObj.addProperty("Message", "Input Payload Received");
					resObj.addProperty("ErrorCode", "");
					response.getWriter().println(resObj);

				} else {
					JsonObject resObj = new JsonObject();
					resObj.addProperty("message", "Invalid Input");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");

					response.getWriter().println(resObj);

				}
			} catch (Exception ex) {
				JsonObject retunObj = new JsonObject();
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
				retunObj.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
				retunObj.addProperty("Status", "000002");
				retunObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(retunObj);

			}
		} else {
			try {
				paymentId = request.getParameter("Id");
				if (paymentId != null && !paymentId.equalsIgnoreCase("")) {
					String userPass = userName + ":" + password;
					if (debug) {
						response.getWriter().println("oDataUrl: " + oDataUrl);
						response.getWriter().println("userName: " + userName);
						response.getWriter().println("password: " + password);
					}
					executeURL = oDataUrl + "APPayments?$filter=ID%20eq%20%27" + paymentId + "%27";
					if (debug) {
						response.getWriter().println("executeUrl :" + executeURL);
					}

					paymentInfo = commonUtils.executeURL(executeURL, userPass, response);
					if (debug) {
						response.getWriter().println("APPayments : " + paymentInfo);
					}
					if (paymentInfo != null && !paymentInfo.has("error") && paymentInfo.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						if (debug) {
							response.getWriter().println("paymentInfo payload :" + paymentInfo);
						}
						paymentInfoObj = paymentInfo.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
						id = paymentInfoObj.get("ID").getAsString();
						executeURL = oDataUrl + "APPaymentItemDetails?$filter=PaymentGUID%20eq%20%27" + id + "%27";
						if (debug) {
							response.getWriter().println("executeURL: " + executeURL);
						}
						paymentItemInfo = commonUtils.executeURL(executeURL, userPass, response);
						if (debug) {
							response.getWriter().println("payment item info  :" + paymentItemInfo);
						}

						if (paymentItemInfo != null && !paymentItemInfo.has("error")) {
							printInvoice(paymentItemInfo.get("d").getAsJsonObject().get("results").getAsJsonArray(), paymentInfoObj, request, response, null, servletPath, debug, null, null, null, null, aggId);
						} else {
							JsonObject result = new JsonObject();
							result.addProperty("ErrorCode", "J002");
							result.addProperty("Status", "000002");
							result.addProperty("Message", "APPaymentItemDetails Records Not Exist");
							response.getWriter().println(new Gson().toJson(result));
						}
					} else {
						JsonObject result = new JsonObject();
						result.addProperty("ErrorCode", "J002");
						result.addProperty("Status", "000002");
						result.addProperty("Message", "No records found");
						response.getWriter().println(new Gson().toJson(result));

					}
				} else {
					JsonObject responseObject = new JsonObject();
					responseObject.addProperty("ErrorCode", "J002");
					responseObject.addProperty("Status", "000002");
					responseObject.addProperty("Message", "Input Param Missing");
					response.getWriter().println(new Gson().toJson(responseObject));
				}
			} catch (Exception ex) {
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				JsonObject responseObject = new JsonObject();
				responseObject.addProperty("Status", "000002");
				responseObject.addProperty("ErrorCode", "J001");
				responseObject.addProperty("Message", ex.getMessage());
				responseObject.addProperty("Exception Trace", buffer.toString());
				response.getWriter().println(responseObject);
			}
		}

	}

	private void printInvoice(JsonArray paymentItem, JsonObject paymentHeader, HttpServletRequest request, HttpServletResponse response, JsonObject scfObj, String servletPath, boolean debug, ODataLogs oDataLogs, AtomicInteger stepNo, String logID, JsonArray appLogMsgArray, String aggrId) throws Exception {
		OutputStream os = null;
		CommonUtils commonUtils = new CommonUtils();
		String cpGuid = "", cpName = "";
		try {
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
			templateResolver.setTemplateMode(TemplateMode.HTML);
			templateResolver.setPrefix("/Resources/PaymentAdvice/");
			templateResolver.setSuffix(".html");
			templateResolver.setCacheTTLMs(3600000L);
			TemplateEngine templateEngine = new TemplateEngine();
			templateEngine.setTemplateResolver(templateResolver);
			Context webContext = new Context();
			if (paymentHeader.has("CPGuid") && !paymentHeader.get("CPGuid").isJsonNull() && !paymentHeader.get("CPGuid").getAsString().equalsIgnoreCase("")) {
				cpGuid = paymentHeader.get("CPGuid").getAsString();
			}

			if (paymentHeader.has("CPName") && !paymentHeader.get("CPName").isJsonNull() && !paymentHeader.get("CPName").getAsString().equalsIgnoreCase("")) {
				cpName = paymentHeader.get("CPName").getAsString();
			}

			
			webContext.setVariable("BeneficiaryCode", cpGuid);

			if (paymentHeader.has("TrackID") && !paymentHeader.get("TrackID").isJsonNull()) {
				webContext.setVariable("PaymentReferenceNumber", paymentHeader.get("TrackID").getAsString());
			} else {
				webContext.setVariable("PaymentReferenceNumber", "");
			}
			if (paymentHeader.has("RunDate") && !paymentHeader.get("RunDate").isJsonNull() && !paymentHeader.get("RunDate").getAsString().equalsIgnoreCase("")) {
				String runDate = paymentHeader.get("RunDate").getAsString();
				String formatedDate = commonUtils.convertLongDateToString(response, runDate, debug);
				String date = formateDate(formatedDate);
				webContext.setVariable("TxnDate", date);
			} else {
				webContext.setVariable("TxnDate", "");
			}

			if (paymentHeader.has("BenificiaryName") && !paymentHeader.get("BenificiaryName").isJsonNull()) {
				webContext.setVariable("BenificiaryName", paymentHeader.get("BenificiaryName").getAsString());
			} else {
				webContext.setVariable("BenificiaryName", "");
			}
			if (!paymentHeader.get("Source").isJsonNull()) {
				String paymentMethod = paymentHeader.get("Source").getAsString();
				if (paymentMethod.equalsIgnoreCase("IPS")) {
					webContext.setVariable("PaymentMethod", "Direct Payment");
					if (!paymentHeader.get("UTRNo").isJsonNull()) {
						webContext.setVariable("UTRNo", paymentHeader.get("UTRNo").getAsString());
					}
				} else if (paymentMethod.equalsIgnoreCase("FSCM")) {
					webContext.setVariable("PaymentMethod", "Early Payment");
					webContext.setVariable("UTRNo", null);

				}
			}
			if (!paymentHeader.get("BankReferenceNo").isJsonNull()) {
				webContext.setVariable("BankReferenceNo", paymentHeader.get("BankReferenceNo").getAsString());
			}
			if (paymentHeader.has("BeneficiaryAccNo") && !paymentHeader.get("BeneficiaryAccNo").isJsonNull()) {
				String accNo = paymentHeader.get("BeneficiaryAccNo").getAsString();
				if (!accNo.equalsIgnoreCase("") && accNo.length() > 4) {
					webContext.setVariable("lstdigAccNo", getLastDigits(accNo));
				} else {
					webContext.setVariable("lstdigAccNo", accNo);
				}

			}

			if (paymentHeader.has("IFSC") && !paymentHeader.get("IFSC").isJsonNull()) {
				webContext.setVariable("IFSC", paymentHeader.get("IFSC").getAsString());
			} else {
				webContext.setVariable("IFSC", "");
			}

			

			List<PaymentAdviceDto> paymentIntemInfo = getPaymentIntemInfo(paymentHeader, paymentItem, response, debug, webContext);
			if (!paymentIntemInfo.isEmpty() && paymentIntemInfo.size() > 0) {
				webContext.setVariable("itemList", paymentIntemInfo);
			}

			/*
			 * String image ="data:image/jpg;base64, "+loadImages(); webContext.setVariable("image",image);
			 */

			String html = templateEngine.process("paymentAdvice", webContext);
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(html);
			renderer.layout();
				if (!debug) {
					response.setContentType("application/pdf");
					response.setHeader("Cache-Control", "no-cache");
					os = response.getOutputStream();
					renderer.createPDF(os);
					os.flush();
				} else {
					response.setContentType("application/json");
					response.setHeader("Cache-Control", "no-cache");
				}
			
			
		} catch (Exception ex) {
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			
				JsonObject resObj = new JsonObject();
				resObj.addProperty("Message", "Exception occurred while generating a PDf file");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ExceptionTrace", buffer.toString());
				response.getWriter().println(resObj);

		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException ex) {
				throw ex;
			}
		}

	}

	private List<PaymentAdviceDto> getPaymentIntemInfo(JsonObject paymnetHeader, JsonArray paymentItem, HttpServletResponse response, boolean debug, Context webContext) throws Exception {
		List<PaymentAdviceDto> paymenetItemList = new ArrayList<>();
		CommonUtils commonUtils = new CommonUtils();
		NumberFormat formateAmt = NumberFormat.getInstance();
		formateAmt.setGroupingUsed(true);
		Double grossAmount = new Double(0.00);
		Double netAmt = new Double(0.00);
		double totalDeducation = 0.00d;
		double totalGross = 0.00d;
		double totalNetAmount = 0.00d;
		Double deduction = new Double(0.00);
		try {
			if (paymnetHeader.has("InterestAmount") && !paymnetHeader.get("InterestAmount").isJsonNull() && !paymnetHeader.get("InterestAmount").getAsString().equalsIgnoreCase("")) {
				deduction = paymnetHeader.get("InterestAmount").getAsDouble();
			}
			for (int i = 0; i < paymentItem.size(); i++) {
				JsonObject jsonResponse = paymentItem.get(i).getAsJsonObject();
				PaymentAdviceDto paymentAdvice = new PaymentAdviceDto();
				if (!jsonResponse.get("CPInvoiceNo").isJsonNull()) {
					paymentAdvice.setDocNumber(jsonResponse.get("CPInvoiceNo").getAsString());
				}

				if (!jsonResponse.get("ItemDate").isJsonNull()) {
					String iteamDate = jsonResponse.get("ItemDate").getAsString();
					String convertedString = commonUtils.convertLongDateToString(response, iteamDate, debug);
					String formateDate = formateDate(convertedString);
					paymentAdvice.setDocDate(formateDate);
				}

				if (!jsonResponse.get("ItemAmount").isJsonNull()) {
					grossAmount = jsonResponse.get("ItemAmount").getAsDouble();
					if (grossAmount >= 0) {
						// Document Type Set to Invoice
						if (deduction > 0) {
							totalDeducation += deduction.doubleValue();
							netAmt = grossAmount - deduction.doubleValue();
							paymentAdvice.setDeduction(getFormatedAmt(deduction.doubleValue()));
						} else {
							paymentAdvice.setDeduction(getFormatedAmt(deduction.doubleValue()));
							netAmt = grossAmount;
						}
						paymentAdvice.setDocType("Invoice");
						if (!jsonResponse.get("ItemDueDate").isJsonNull()) {
							String duDate = jsonResponse.get("ItemDueDate").getAsString();
							String convertDueDate = commonUtils.convertLongDateToString(response, duDate, debug);
							String formateDate = formateDate(convertDueDate);
							paymentAdvice.setDocDueDate(formateDate);
						}

					} else {
						// Document Type Set to Debit Note
						paymentAdvice.setDocDueDate("");
						paymentAdvice.setDeduction(null);
						paymentAdvice.setDocType("Debit Note");
						netAmt = grossAmount;

					}
					totalGross += grossAmount.doubleValue();
					paymentAdvice.setGrossAmount(getFormatedAmt(grossAmount));
					paymentAdvice.setNetAmount(getFormatedAmt(netAmt));
					totalNetAmount += netAmt;

				}

				paymenetItemList.add(paymentAdvice);
			}

			webContext.setVariable("totalGrossAmt", getFormatedAmt(totalGross));
			webContext.setVariable("totalNetAmt", getFormatedAmt(totalNetAmount));
			if (totalNetAmount > 0) {
				String convertAmtToWords = commonUtils.convertAmtToWords(totalNetAmount);
				webContext.setVariable("AmtInWords", convertAmtToWords);
			}

			webContext.setVariable("totalDeducationAmt", getFormatedAmt(totalDeducation));
			Collections.sort(paymenetItemList); // Assending Order
			Collections.reverse(paymenetItemList); // Descending Order
			return paymenetItemList;
		} catch (Exception ex) {
			throw ex;
		}
	}

	private String formateDate(String date) throws Exception {
		try {
			Date dateFormate = new SimpleDateFormat("yyyyMMdd").parse(date);
			DateFormat foramte = new SimpleDateFormat("dd/MM/yyyy");
			String format2 = foramte.format(dateFormate);
			return format2;
		} catch (Exception ex) {
			throw ex;
		}

	}

	private String getLastDigits(String accNo) throws Exception {
		StringBuilder strBuilder = new StringBuilder();
		try {
			String lstDigit = accNo.substring(accNo.length() - 4, accNo.length());
			String firstDigit = accNo.substring(0, accNo.length() - 4);
			for (int i = 0; i < firstDigit.length(); i++) {
				strBuilder.append("X");
			}
			return strBuilder.append(lstDigit).toString();

		} catch (Exception e) {
			throw e;
		}

	}

	private String getFormatedAmt(double amt) {
		try {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			decimalFormat.setGroupingUsed(true);
			decimalFormat.setGroupingSize(3);
			String format = decimalFormat.format(amt);
			return format;
		} catch (Exception e) {
			throw e;
		}
	}

	/*
	 * private String formateDateddMMyyyy(String date)throws Exception{ try{ SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy"); Date parsedate = formatter.parse(date); formatter = new SimpleDateFormat("E, dd MMM, yyyy"); String format = formatter.format(parsedate); return format; }catch(Exception ex){ throw ex; } }
	 */

	private void sendEmail(JsonObject scpObj, boolean debug, HttpServletResponse response, List<String> files, String emailSub, ODataLogs oDataLogs, AtomicInteger stepNo, String logID, JsonArray appLogMsgArray, String aggrId, HttpServletRequest request) throws Exception {
		Properties properties = new Properties();
		final String fromEmail = platformEmail;
		final String password = emailPassword;
		CommonUtils commonUtils = new CommonUtils();
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
				Properties emailProperties = getEmailProperties();
				StringBuffer buffer = new StringBuffer();
				if (!scpObj.get("Name1").isJsonNull()) {
					buffer.append(scpObj.get("Name1").getAsString());
				}
				if (scpObj.get("Name1").isJsonNull() && !scpObj.get("Name2").isJsonNull()) {
					if (buffer.toString().trim().length() > 0) {
						buffer.append(" ").append(scpObj.get("Name2").getAsString());
					}
				}
				if (scpObj.get("Name1").isJsonNull() && scpObj.get("Name2").isJsonNull() && !scpObj.get("Name3").isJsonNull()) {
					if (buffer.toString().trim().length() > 0) {
						buffer.append(" ").append(scpObj.get("Name3").getAsString());
					}
				}

				if (scpObj.get("Name1").isJsonNull() && scpObj.get("Name2").isJsonNull() && scpObj.get("Name3").isJsonNull() && !scpObj.get("Name4").isJsonNull()) {
					if (buffer.toString().trim().length() > 0) {
						buffer.append(" ").append(scpObj.get("Name4").getAsString());
					}
				}

				String url = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
				String pygUsername = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
				String pygPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
				
				String executeURL = url + "Aggregators?$filter=AggregatorID%20eq%20%27" + aggrId + "%27";
				String userpass = pygUsername + ":" + pygPassword;
				JsonObject aggrObj = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (aggrObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				aggrObj = aggrObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
				String aggrName = aggrObj.get("AggregatorName").getAsString();
				
				if (files.size() <= 100) {
					Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
						protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(fromEmail, password);
						}
					});
					MimeMessage msg = new MimeMessage(session);
					msg.setFrom(new InternetAddress(fromEmail));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(scpObj.get("EmailID").getAsString()));
					msg.setSubject(emailSub);
					Multipart emailContent = new MimeMultipart();
					MimeBodyPart textBodyPart = new MimeBodyPart();
					textBodyPart.setText("Dear " + buffer.toString() + ",\n\nPlease find the attached payment advice for the transaction done with " + aggrName + " (" + aggrId + ")." + "\nPlease contact your ICICI bank RM or write to icicibank.support@arteriatech.com in case of any clarifications\n\n\nThanks,\nFinessart Platform\n(This is an auto generated email. Please do not reply to this)");
					for (int i = 0; i < files.size(); i++) {
						MimeBodyPart pdfAttachment = new MimeBodyPart();
						String filePath = files.get(i);
						pdfAttachment.attachFile(filePath);
						emailContent.addBodyPart(pdfAttachment);
					}
					emailContent.addBodyPart(textBodyPart);
					msg.setContent(emailContent);
					Transport.send(msg);
					oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Mail Sent Successfully", scpObj.get("EmailID").getAsString(), "", "", "");
				} else {
					boolean mailSetSuccess=true;
					int mxmAttachments=100;
					int loopCount = files.size() / mxmAttachments;
					int rem = files.size() % mxmAttachments;
					if (rem != 0) {
						loopCount++;
					}
					int skipCount=0;
					for (int i = 0; i < loopCount; i++) {
						skipCount = mxmAttachments * i;
						final List<String> filePaths = files.stream().skip(skipCount).limit(mxmAttachments).collect(Collectors.toList());
						try {
							Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
								protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(fromEmail, password);
								}
							});
							MimeMessage msg = new MimeMessage(session);
							msg.setFrom(new InternetAddress(fromEmail));
							msg.addRecipient(Message.RecipientType.TO, new InternetAddress(scpObj.get("EmailID").getAsString()));
							msg.setSubject(emailSub);
							Multipart emailContent = new MimeMultipart();
							MimeBodyPart textBodyPart = new MimeBodyPart();
							textBodyPart.setText("Dear " + buffer.toString() + ",\n\nPlease find the attached payment advice for the transaction done with " + aggrName + " (" + aggrId + ")." + "\nPlease contact your ICICI bank RM or write to icicibank.support@arteriatech.com in case of any clarifications\n\n\nThanks,\nFinessart Platform\n(This is an auto generated email. Please do not reply to this)");
							for (int j = 0; j < filePaths.size(); j++) {
								MimeBodyPart pdfAttachment = new MimeBodyPart();
								String filePath = filePaths.get(j);
								pdfAttachment.attachFile(filePath);
								emailContent.addBodyPart(pdfAttachment);
							}
							emailContent.addBodyPart(textBodyPart);
							msg.setContent(emailContent);
							Transport.send(msg);
						} catch (Exception ex) {
							mailSetSuccess=false;
							StackTraceElement element[] = ex.getStackTrace();
							StringBuffer exceptionBuff = new StringBuffer(ex.getLocalizedMessage() + "-->");
							if (ex.getLocalizedMessage() != null) {
								exceptionBuff.append(ex.getLocalizedMessage() + "-->");
							}
							for (int j = 0; j < element.length; j++) {
								exceptionBuff.append(element[i]);
							}
							oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Exception occurred while Sending an email", scpObj.get("EmailID").getAsString(), ex.getClass().getCanonicalName(), "", exceptionBuff.toString());
						}

					}
					
					
					if(mailSetSuccess){
					oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Mail Sent Successfully", scpObj.get("EmailID").getAsString(), "", "", "");
					}else{
						oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Sending email failed", scpObj.get("EmailID").getAsString(), "", "", "");
					}
					}
			} else {
				oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "", "", "", "Unable to fetch the AggregatorName", aggrObj + "");

			}
			
		} catch (AuthenticationFailedException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer(ex.getLocalizedMessage()+"-->");
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "AuthenticationFailedException", "Exception occurred while Sending an email", ex.getClass().getCanonicalName(), "", "", buffer.toString());
		} catch (SendFailedException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer(ex.getLocalizedMessage()+"-->");
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Exception occurred while Sending an email", "SendFailedException", ex.getClass().getCanonicalName(), "", buffer.toString());
		} catch (MessagingException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer(ex.getLocalizedMessage()+"-->");
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Exception occurred while Sending an email", "MessagingException", ex.getClass().getCanonicalName(), "", buffer.toString());
		} catch (IOException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer(ex.getLocalizedMessage()+"-->");
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}

			oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Exception occurred while Sending an email", "IOException", ex.getClass().getCanonicalName(), "", buffer.toString());
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			
			StringBuffer buffer = new StringBuffer(ex.getLocalizedMessage()+"-->");
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "Exception occurred while Sending an email", "Exception", ex.getClass().getCanonicalName(), "", buffer.toString());
		} finally {
			if (files.size() > 0) {
				for (int i = 0; i < files.size(); i++) {
					File file = new File(files.get(i));
					if (file.exists()) {
						file.delete();
					}
				}
				
				oDataLogs.createApplicationLogMsgOnEvent(request, response, debug, appLogMsgArray, logID, stepNo.getAndIncrement() + "", "All Files Deleted Successfully", "", "", "", "");
			}
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

	private String getDateInYYYYMMDDFormate() throws Exception {
		try {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Date today = Calendar.getInstance().getTime();
			String reportDate = df.format(today);
			return reportDate;

		} catch (Exception ex) {
			throw ex;
		}

	}

	private String loadImages() throws Exception, IOException {
		InputStream resourceAsStream = getServletContext().getResourceAsStream("/Resources/images/arteria.jpg");
		try {
			BufferedImage bImage = ImageIO.read(resourceAsStream);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos);
			byte[] data = bos.toByteArray();
			return Base64.getEncoder().encodeToString(data);
		} catch (Exception ex) {
			throw ex;
		}

	}

	public String formateDateIn(String date) throws Exception, IOException {
		try {
			Date dateFormate = new SimpleDateFormat("yyyyMMdd").parse(date);
			DateFormat foramte = new SimpleDateFormat("yyyy-MM-dd");
			String format2 = foramte.format(dateFormate);
			return format2;
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	
	private JsonObject sendPaymentAdvice(JsonArray paymentItem, JsonObject paymentHeader, HttpServletRequest request, HttpServletResponse response, JsonObject scfObj, boolean debug, ODataLogs oDataLogs, AtomicInteger stepNo, String logID, JsonArray appLogMsgArray, String aggrId,List<String> fileLst) throws Exception {
		OutputStream os = null;
		CommonUtils commonUtils = new CommonUtils();
		String cpGuid = "", cpName = "";
		JsonObject resObj=new JsonObject();
		try {
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
			templateResolver.setTemplateMode(TemplateMode.HTML);
			templateResolver.setPrefix("/Resources/PaymentAdvice/");
			templateResolver.setSuffix(".html");
			templateResolver.setCacheTTLMs(3600000L);
			TemplateEngine templateEngine = new TemplateEngine();
			templateEngine.setTemplateResolver(templateResolver);
			Context webContext = new Context();
			if (paymentHeader.has("CPGuid") && !paymentHeader.get("CPGuid").isJsonNull() && !paymentHeader.get("CPGuid").getAsString().equalsIgnoreCase("")) {
				cpGuid = paymentHeader.get("CPGuid").getAsString();
			}

			if (paymentHeader.has("CPName") && !paymentHeader.get("CPName").isJsonNull() && !paymentHeader.get("CPName").getAsString().equalsIgnoreCase("")) {
				cpName = paymentHeader.get("CPName").getAsString();
			}

			/*
			 * String cpGuid=paymentHeader.get("CPGuid").getAsString(); String cpName=paymentHeader.get("CPName").getAsString();
			 */
			webContext.setVariable("BeneficiaryCode", cpGuid);

			if (paymentHeader.has("TrackID") && !paymentHeader.get("TrackID").isJsonNull()) {
				webContext.setVariable("PaymentReferenceNumber", paymentHeader.get("TrackID").getAsString());
			} else {
				webContext.setVariable("PaymentReferenceNumber", "");
			}
			if (paymentHeader.has("RunDate") && !paymentHeader.get("RunDate").isJsonNull() && !paymentHeader.get("RunDate").getAsString().equalsIgnoreCase("")) {
				String runDate = paymentHeader.get("RunDate").getAsString();
				String formatedDate = commonUtils.convertLongDateToString(response, runDate, debug);
				String date = formateDate(formatedDate);
				webContext.setVariable("TxnDate", date);
			} else {
				webContext.setVariable("TxnDate", "");
			}

			if (paymentHeader.has("BenificiaryName") && !paymentHeader.get("BenificiaryName").isJsonNull()) {
				webContext.setVariable("BenificiaryName", paymentHeader.get("BenificiaryName").getAsString());
			} else {
				webContext.setVariable("BenificiaryName", "");
			}
			if (!paymentHeader.get("Source").isJsonNull()) {
				String paymentMethod = paymentHeader.get("Source").getAsString();
				if (paymentMethod.equalsIgnoreCase("IPS")) {
					webContext.setVariable("PaymentMethod", "Direct Payment");
					if (!paymentHeader.get("UTRNo").isJsonNull()) {
						webContext.setVariable("UTRNo", paymentHeader.get("UTRNo").getAsString());
					}
				} else if (paymentMethod.equalsIgnoreCase("FSCM")) {
					webContext.setVariable("PaymentMethod", "Early Payment");
					webContext.setVariable("UTRNo", null);

				}
			}
			if (!paymentHeader.get("BankReferenceNo").isJsonNull()) {
				webContext.setVariable("BankReferenceNo", paymentHeader.get("BankReferenceNo").getAsString());
			}
			if (paymentHeader.has("BeneficiaryAccNo") && !paymentHeader.get("BeneficiaryAccNo").isJsonNull()) {
				String accNo = paymentHeader.get("BeneficiaryAccNo").getAsString();
				if (!accNo.equalsIgnoreCase("") && accNo.length() > 4) {
					webContext.setVariable("lstdigAccNo", getLastDigits(accNo));
				} else {
					webContext.setVariable("lstdigAccNo", accNo);
				}

			}

			if (paymentHeader.has("IFSC") && !paymentHeader.get("IFSC").isJsonNull()) {
				webContext.setVariable("IFSC", paymentHeader.get("IFSC").getAsString());
			} else {
				webContext.setVariable("IFSC", "");
			}

			/*
			 * if (!paymentHeader.get("Remarks").isJsonNull()) { webContext.setVariable("Remarks", paymentHeader.get("Remarks").getAsString()); }
			 */

			List<PaymentAdviceDto> paymentIntemInfo = getPaymentIntemInfo(paymentHeader, paymentItem, response, debug, webContext);
			if (!paymentIntemInfo.isEmpty() && paymentIntemInfo.size() > 0) {
				webContext.setVariable("itemList", paymentIntemInfo);
			}

			/*
			 * String image ="data:image/jpg;base64, "+loadImages(); webContext.setVariable("image",image);
			 */

			final String html = templateEngine.process("paymentAdvice", webContext);
			final ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(html);
			renderer.layout();
			// File name format: VendorCode<CPGuid>_Advice_YYYYMMDDHHmmss. Example: 1000_Advice_20211008155645.pdf
			
				response.setContentType("application/json");
				response.setHeader("Cache-Control", "no-cache");
				StringBuilder fileName = new StringBuilder();
				StringBuilder emailSub = new StringBuilder();
				emailSub.append("Reg: Payment Advice for ").append(cpGuid);
				// emailSub.append(cpName).append("_").append(cpGuid);
				final String currentDate = getDateInYYYYMMDDFormate();
				/* fileName.append("/Resources/PaymentAdvice/"); */
				final String filePath = getServletContext().getRealPath("/Resources/PaymentAdvice/");
				fileName.append(filePath);
				fileName.append(cpGuid).append("_Advice_").append(currentDate).append(".pdf");
				File file = new File(fileName.toString());
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				renderer.createPDF(fos);
				fos.flush();
				fos.close();
				fileLst.add(fileName.toString());
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
				resObj.addProperty("Message", "");
				//sendEmail(scfObj, debug, response, file, fileName.toString(), emailSub.toString(), oDataLogs, stepNo, logID, appLogMsgArray, aggrId, request);

		} catch (Exception ex) {
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer(ex.getClass().getCanonicalName());
			if(ex.getLocalizedMessage()!=null){
				buffer.append(ex.getLocalizedMessage()+"-->");
			}
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException ex) {
				throw ex;
			}
		}
		return resObj;
	}
}
package com.arteriatech.aml;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.net.URLEncoder;  

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
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

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
// import com.itextpdf.text.pdf.PdfDocument.Destination;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

public class AMLUtils {

	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AGGRICICI = "AGGRICICI";
	public static final String PGID = "AMLCHK";

	public JsonObject checkAMLEligibility(HttpServletResponse response, boolean debug, JsonObject bpELigibilityRec, JsonArray bpCntpArray, Properties props) {
		String statusID = "";
		JsonObject resObj = new JsonObject();
		boolean isEligible = false;
		try {

			if (bpELigibilityRec.has("ValidTo_AML") && !bpELigibilityRec.get("ValidTo_AML").isJsonNull() && !bpELigibilityRec.get("ValidTo_AML").getAsString().equalsIgnoreCase("")) {
				// check ODEligibl Record > CurrentDate.
				isEligible = checkODEligibleDate(response, bpELigibilityRec.get("ValidTo_AML").getAsString(), debug);

				if (bpELigibilityRec.has("EligibilityStatus_AML") && !bpELigibilityRec.get("EligibilityStatus_AML").isJsonNull() && !bpELigibilityRec.get("EligibilityStatus_AML").getAsString().equalsIgnoreCase("")) {
					statusID = bpELigibilityRec.get("EligibilityStatus_AML").getAsString();
				}
				if (debug) {
					response.getWriter().println("statusID:" + statusID);
					response.getWriter().println("isEligible:" + isEligible);
				}
			}
			if (statusID.equalsIgnoreCase("200040") && isEligible) {
				resObj.addProperty("Message", "AML Eligible");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
				return resObj;
			} else {
				// call the AntiMoneyLaunderingCheck CPI Iflow.SS
				JsonObject cpiResponseObj = antiMoneyLaunderingCheck(response, bpELigibilityRec, bpCntpArray, props, debug);
				if (debug) {
					response.getWriter().println("cpiResponseObj:" + cpiResponseObj);
				}
				if (cpiResponseObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
					resObj.addProperty("Message", "We are in the process of checking your eligibility for Channel Finance facility of ICICI Bank. Please check back in some time to know your sanction status");
					resObj.addProperty("Status", "000003");
					resObj.addProperty("ErrorCode", "AMLS001");
					return resObj;
				} else {
					// check the failure case here
					if (cpiResponseObj.get("Status").getAsString().equalsIgnoreCase("000002") && !cpiResponseObj.get("ErrorCode").getAsString().equalsIgnoreCase("J002")) {
						resObj.addProperty("Message", "Currently we are facing technical issues. Please try after sometime");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "AML00003");
						return resObj;
					} else {
						return cpiResponseObj;

					}
				}
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionMessage", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;

		}
	}

	public JsonObject antiMoneyLaunderingCheck(HttpServletResponse response, JsonObject bpEligibilityRec, JsonArray bpCntpArray, Properties properties, boolean debug) {
		CommonUtils commonUtils = new CommonUtils();
		
		JsonObject resObj = new JsonObject();
		String messageType = "", procCode = "", appCode = "", reqMessage1 = "", reqMessage4 = "", reqMessage5 = "", reqMessage6 = "", reqMessage2 = "", reqMessage3 = "";
		String incDate = "", gender = "", countryDesc = "", pan = "", corporateIdentificationNo = "", passport = "", address = "", city = "", state = "", district = "";
		String mobile1 = "", mobile2 = "", mobile3 = "", addtionalParam1 = "", additionalParam2 = "", additionalParam3 = "";
		String antiMonyLunCheckRes = "", recordId = "";
		try {
			JsonObject configObj = getCorpConnectEmail(response, commonUtils, debug);
			if (debug) {
				response.getWriter().println("configObj:" + configObj);
			}
			if (configObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (configObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					String corpConnectEamil = configObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("TypeValue").getAsString();
					if (debug) {
						response.getWriter().println("corpConnectEamil:" + corpConnectEamil);
					}
					JsonObject pgConfigDbObj = getAMLPGPaymentConfigs(response, debug);
					if (debug) {
						response.getWriter().println("pgConfigDbObj:" + pgConfigDbObj);
					}
					if (pgConfigDbObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
						JsonObject pgConfigObj = pgConfigDbObj.get("Message").getAsJsonObject();
						if (pgConfigObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							pgConfigObj = pgConfigObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();

							if (!pgConfigObj.get("ClientCode").isJsonNull()) {
								messageType = pgConfigObj.get("ClientCode").getAsString();
							}

							if (!pgConfigObj.get("SchemeCode").isJsonNull()) {
								procCode = pgConfigObj.get("SchemeCode").getAsString();
							}

							if (!pgConfigObj.get("MerchantCode").isJsonNull()) {
								appCode = pgConfigObj.get("MerchantCode").getAsString();
							}

							if (!pgConfigObj.get("PGParameter1").isJsonNull()) {
								reqMessage1 = pgConfigObj.get("PGParameter1").getAsString();
							}

							if (!pgConfigObj.get("PGParameter2").isJsonNull()) {
								reqMessage4 = pgConfigObj.get("PGParameter2").getAsString();
							}

							if (!pgConfigObj.get("PGParameter3").isJsonNull()) {
								reqMessage5 = pgConfigObj.get("PGParameter3").getAsString();
							}

							if (!pgConfigObj.get("PGParameter4").isJsonNull()) {
								reqMessage6 = pgConfigObj.get("PGParameter4").getAsString();
							}

							recordId = bpEligibilityRec.get("RecordID").getAsString();
							JsonArray amlRecordsArray = new JsonArray();
							JsonObject bpRecord = new JsonObject();
							bpRecord.addProperty("MessageType", messageType);
							bpRecord.addProperty("ProcCode", procCode);
							bpRecord.addProperty("AppCode", appCode);
							bpRecord.addProperty("TRN", recordId);
							long currentTimeMillis = System.currentTimeMillis();
							Date currentDate = new Date(currentTimeMillis);
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String date = format.format(currentDate);
							date = date.replaceFirst(" ", "T");
							bpRecord.addProperty("DTime", date);
							bpRecord.addProperty("RequestMessage1", reqMessage1);
							bpRecord.addProperty("RequestMessage2", recordId);
							bpRecord.addProperty("RequestMessage3", recordId);
							bpRecord.addProperty("RequestMessage4", reqMessage4);
							bpRecord.addProperty("RequestMessage5", reqMessage5);
							bpRecord.addProperty("RequestMessage6", reqMessage6);

							String cpGuid = bpEligibilityRec.get("CPGuid").getAsString();
							String cpType = bpEligibilityRec.get("CPType").getAsString();
							String aggregatorId = bpEligibilityRec.get("AggregatorID").getAsString();
							if (debug) {
								response.getWriter().println("before Updating cptype:" + cpType);
							}
							if (cpType.equalsIgnoreCase("01")) {
								cpType = "000003";
							} else if (cpType.equalsIgnoreCase("60")) {
								cpType = "000002";
							}

							if (debug) {
								response.getWriter().println("updated cpType:" + cpType);
								response.getWriter().println("cpGuid:" + cpGuid);
								response.getWriter().println("aggregatorId:" + aggregatorId);
							}
							JsonObject scpObj = getScpRecord(response, debug, cpType, cpGuid, aggregatorId);
							if (scpObj.get("Status").getAsString().equalsIgnoreCase("000001")) {
								if (scpObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
									scpObj = scpObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();

									if (!bpEligibilityRec.get("IncorporationDate").isJsonNull()) {
										String incroptionDate = bpEligibilityRec.get("IncorporationDate").getAsString();
										incDate = commonUtils.convertLongDateToString(response, incroptionDate, "dd-MM-yyyy", debug);
									}

									if (!scpObj.get("PANNo").isJsonNull()) {
										pan = scpObj.get("PANNo").getAsString();
									}

									if (!bpEligibilityRec.get("CorporateIdentificationNo").isJsonNull()) {
										corporateIdentificationNo = bpEligibilityRec.get("CorporateIdentificationNo").getAsString();
									}
									StringBuffer buffer = new StringBuffer();
									if (!scpObj.get("Address1").isJsonNull() && !scpObj.get("Address1").getAsString().equalsIgnoreCase("")) {
										
										buffer.append(scpObj.get("Address1").getAsString());
									}

									if (!scpObj.get("Address2").isJsonNull() && !scpObj.get("Address2").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("Address2").getAsString());
									}

									if (!scpObj.get("Address3").isJsonNull() && !scpObj.get("Address3").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("Address3").getAsString());
									}
									if (!scpObj.get("Address4").isJsonNull() && !scpObj.get("Address4").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("Address4").getAsString());
									}
									if (!scpObj.get("City").isJsonNull() && !scpObj.get("City").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("City").getAsString());
										city = scpObj.get("City").getAsString();
										
									}
									if (!scpObj.get("District").isJsonNull() && !scpObj.get("District").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("District").getAsString());
										district = scpObj.get("District").getAsString();
									}
									if (!scpObj.get("StateDesc").isJsonNull() && !scpObj.get("StateDesc").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("StateDesc").getAsString());
										state = scpObj.get("StateDesc").getAsString();
									}
									if (!scpObj.get("CountryDesc").isJsonNull() && !scpObj.get("CountryDesc").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("CountryDesc").getAsString());
										countryDesc = scpObj.get("CountryDesc").getAsString();
									}
									if (!scpObj.get("PostalCode").isJsonNull() && !scpObj.get("PostalCode").getAsString().equalsIgnoreCase("")) {
										buffer.append(",").append(scpObj.get("PostalCode").getAsString());
									}

									/*if (!bpEligibilityRec.get("Mobile1").isJsonNull() && !bpEligibilityRec.get("Mobile1").getAsString().equalsIgnoreCase("")) {
										mobile1 = bpEligibilityRec.get("Mobile1").getAsString();
									}*/
									
									if (!scpObj.get("MobileNo").isJsonNull() && !scpObj.get("MobileNo").getAsString().equalsIgnoreCase("")) {
										mobile1 = scpObj.get("MobileNo").getAsString();
									}
									
									if (!scpObj.get("TelephoneNo").isJsonNull() && !scpObj.get("TelephoneNo").getAsString().equalsIgnoreCase("")) {
										mobile2 = scpObj.get("TelephoneNo").getAsString();
									}
									String name="";
									if (!bpEligibilityRec.get("CPName").isJsonNull() && !bpEligibilityRec.get("CPName").getAsString().equalsIgnoreCase("")) {
										name = bpEligibilityRec.get("CPName").getAsString();
									}
									address = buffer.toString();
									if(address.contains("<")){
										address=address.replaceAll("<", " ");
									}
									if(address.contains(">")){
										address=address.replaceAll(">", " ");
									}
									
									if(address.contains("|")){
										address=address.replaceAll("\\|", " ");
									}
									
									if(address.contains("&")){
										address=address.replaceAll("&", "&amp;");
									}
									
									
									if(city.contains("<")){
										city=city.replaceAll("<", " ");
									}
									if(city.contains(">")){
										city=city.replaceAll(">", " ");
									}
									
									if(city.contains("|")){
										city=city.replaceAll("\\|", " ");
									}
									
									if(city.contains("&")){
										city=city.replaceAll("&", "&amp;");
									}
									
									
									if(district.contains("<")){
										district=district.replaceAll("<", " ");
									}
									if(district.contains(">")){
										district=district.replaceAll(">", " ");
									}
									
									if(district.contains("|")){
										district=district.replaceAll("\\|", " ");
									}
									
									if(district.contains("&")){
										district=district.replaceAll("&", "&amp;");
									}
									
									
									if(state.contains("<")){
										state=state.replaceAll("<", " ");
									}
									if(state.contains(">")){
										state=state.replaceAll(">", " ");
									}
									
									if(state.contains("|")){
										state=state.replaceAll("\\|", " ");
									}
									
									if(state.contains("&")){
										state=state.replaceAll("&", "&amp;");
									}
									
									
									if(countryDesc.contains("<")){
										countryDesc=countryDesc.replaceAll("<", " ");
									}
									if(countryDesc.contains(">")){
										countryDesc=countryDesc.replaceAll(">", " ");
									}
									
									if(countryDesc.contains("|")){
										countryDesc=countryDesc.replaceAll("\\|", " ");
									}
									
									if(countryDesc.contains("&")){
										countryDesc=countryDesc.replaceAll("&", "&amp;");
									}
									
									if(name.contains("<")){
										name=name.replaceAll("<", " ");
									}
									if(name.contains(">")){
										name=name.replaceAll(">", " ");
									}
									
									if(name.contains("|")){
										name=name.replaceAll("\\|", " ");
									}
									
									if(name.contains("&")){
										name=name.replaceAll("&", "&amp;");
									}
									
									
									if(debug){
										response.getWriter().println("address:"+address);
									}
									
									String requestMessage7 = recordId + "|" + "C" + "|" + name + "|" + incDate + "|" + gender + "|" + countryDesc + "|" + pan + "|" + corporateIdentificationNo + "|" + passport + "|" + address + "|" + city + "|" + state + "|" + countryDesc + "|" + address + "|" + city + "|" + state + "|" + countryDesc + "|" + mobile1 + "|" + mobile2 + "|" + mobile3 + "|" + addtionalParam1 + "|" + additionalParam2 + "|" + corpConnectEamil + "|";
									if (debug) {
										response.getWriter().println("requestMessage7" + requestMessage7);
									}
									bpRecord.addProperty("RequestMessage7", requestMessage7);
									amlRecordsArray.add(bpRecord);
									for (int i = 0; i < bpCntpArray.size(); i++) {
										JsonObject bpCOntractPerson = bpCntpArray.get(i).getAsJsonObject();
										String bpcntpRecId = bpCntpArray.get(i).getAsJsonObject().get("RecordID").getAsString();
										String dateOfBirth = "", country = "", nationalId2 = "", currentAddress = "", permanentAddress = "", permCity = "", permState = "", percmCountry = "";
										JsonObject bpContpersonCpiPayload = new JsonObject();
										bpContpersonCpiPayload.addProperty("MessageType", messageType);
										bpContpersonCpiPayload.addProperty("ProcCode", procCode);
										bpContpersonCpiPayload.addProperty("AppCode", appCode);
										bpContpersonCpiPayload.addProperty("TRN", bpcntpRecId);
										bpContpersonCpiPayload.addProperty("DTime", date);
										bpContpersonCpiPayload.addProperty("RequestMessage1", reqMessage1);
										bpContpersonCpiPayload.addProperty("RequestMessage2", bpcntpRecId);
										bpContpersonCpiPayload.addProperty("RequestMessage3", bpcntpRecId);
										bpContpersonCpiPayload.addProperty("RequestMessage4", reqMessage4);
										bpContpersonCpiPayload.addProperty("RequestMessage5", reqMessage5);
										bpContpersonCpiPayload.addProperty("RequestMessage6", reqMessage6);
										StringBuffer names = new StringBuffer("");
										if (!bpCOntractPerson.get("Name1").isJsonNull() && !bpCOntractPerson.get("Name1").getAsString().equalsIgnoreCase("")) {
											names.append(bpCOntractPerson.get("Name1").getAsString());
										}
										if (!bpCOntractPerson.get("Name2").isJsonNull() && !bpCOntractPerson.get("Name2").getAsString().equalsIgnoreCase("")) {
											names.append(" ").append(bpCOntractPerson.get("Name2").getAsString());
										}

										if (!bpCOntractPerson.get("DOB").isJsonNull() && !bpCOntractPerson.get("DOB").getAsString().equalsIgnoreCase("")) {
											dateOfBirth = bpCOntractPerson.get("DOB").getAsString();
											dateOfBirth = commonUtils.convertLongDateToString(response, dateOfBirth, "dd-MM-yyyy", debug);
										}

										if (!bpCOntractPerson.get("GenderID").isJsonNull() && !bpCOntractPerson.get("GenderID").getAsString().equalsIgnoreCase("")) {
											gender = bpCOntractPerson.get("GenderID").getAsString();
										}

										if (!bpCOntractPerson.get("PAN").isJsonNull() && !bpCOntractPerson.get("PAN").getAsString().equalsIgnoreCase("")) {
											pan = bpCOntractPerson.get("PAN").getAsString();
										} else {
											pan = "";
										}

										if (!bpCOntractPerson.get("Mobile").isJsonNull() && !bpCOntractPerson.get("Mobile").getAsString().equalsIgnoreCase("")) {
											mobile1 = bpCOntractPerson.get("Mobile").getAsString();
										} else {
											mobile1 = "";
										}
										city = "";
										state = "";
										countryDesc = "";
										mobile2 = "";
										mobile3 = "";
										String updatedName = names.toString();
										if(updatedName.contains("<")){
											updatedName=updatedName.replaceAll("<", " ");
										}
										if(updatedName.contains(">")){
											updatedName=updatedName.replaceAll(">", " ");
										}
										
										if(updatedName.contains("|")){
											updatedName=updatedName.replaceAll("\\|", " ");
										}
										
										if(updatedName.contains("&")){
											updatedName=updatedName.replaceAll("&", "&amp;");
										}
										
										
										requestMessage7 = bpcntpRecId + "|" + "I" + "|" + updatedName + "|" + dateOfBirth + "|" + gender + "|" + country + "|" + pan + "|" + nationalId2 + "|" + passport + "|" + currentAddress + "|" + city + "|" + state + "|" + countryDesc + "|" + permanentAddress + "|" + permCity + "|" + permState + "|" + percmCountry + "|" + mobile1 + "|" + mobile2 + "|" + mobile3 + "|" + addtionalParam1 + "|" + additionalParam2 + "|" + corpConnectEamil + "|";
										bpContpersonCpiPayload.addProperty("RequestMessage7", requestMessage7);
										amlRecordsArray.add(bpContpersonCpiPayload);

									}
									JsonObject cpiInputPayload = new JsonObject();
									cpiInputPayload.add("AMLRecords", amlRecordsArray);
									if (debug) {
										response.getWriter().println("Cpi Input Payload:" + cpiInputPayload);
									}
									DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
											.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
									Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
											.tryGetDestination(DestinationUtils.CPI_CONNECT, options);
									com.sap.cloud.sdk.cloudplatform.connectivity.Destination cpiConfig = destinationAccessor.get();

									String wsURL = cpiConfig.get("URL").get().toString();
									if (debug) {
										response.getWriter().println("WsURL :" + wsURL);
									}

									String username = cpiConfig.get("User").get().toString();
									String password = cpiConfig.get("Password").get().toString();
									String userpass = username + ":" + password;
									wsURL = wsURL + properties.getProperty("AntiMoneyLaunderingCheck");
									if (debug) {
										response.getWriter().println("CPI Input Payload:" + cpiInputPayload);
										response.getWriter().println("WsURL :" + wsURL);
									}

									URL url = new URL(wsURL);
									HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
									byte[] bytes = cpiInputPayload.toString().getBytes("UTF-8");
									urlConnection.setRequestMethod("GET");
									urlConnection.setRequestProperty("Content-Type", "application/json");
									urlConnection.setRequestProperty("charset", "utf-8");
									urlConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
									urlConnection.setRequestProperty("Accept", "application/json");
									urlConnection.setDoOutput(true);
									urlConnection.setDoInput(true);
									String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
									urlConnection.setRequestProperty("Authorization", basicAuth);
									urlConnection.connect();
									OutputStream outputStream = urlConnection.getOutputStream();
									OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
									osw.write(cpiInputPayload.toString());
									osw.flush();
									osw.close();
									int resCode = urlConnection.getResponseCode();
									if (debug) {
										response.getWriter().println("responseCode: " + resCode);
									}

									if ((resCode / 100) == 2 || (resCode / 100) == 3) {
										StringBuffer sb = new StringBuffer();
										BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
										String line = null;
										while ((line = br.readLine()) != null) {
											sb.append(line + "\n");
										}
										br.close();
										if (debug) {
											response.getWriter().println("Reponse from CPI: " + sb.toString());
										}
										antiMonyLunCheckRes = sb.toString();
										JsonParser jsonParser = new JsonParser();
										resObj = (JsonObject) jsonParser.parse(antiMonyLunCheckRes);
										resObj.addProperty("Remarks", "");
										return resObj;
									} else {
										StringBuffer sb = new StringBuffer();
										BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
										String line = null;
										while ((line = br.readLine()) != null) {
											sb.append(line + "\n");
										}
										br.close();
										if (debug) {
											response.getWriter().println("getErrorStream: " + sb.toString());
										}
										antiMonyLunCheckRes = sb.toString();
										resObj.addProperty("Status", "000002");
										resObj.addProperty("ErrorCode", resCode);
										resObj.addProperty("Message", sb.toString());
										if (debug)
											response.getWriter().println("Failure response from CPI:" + resObj);
										return resObj;
									}
								} else {
									resObj.addProperty("Status", "000002");
									resObj.addProperty("ErrorCode", "J002");
									resObj.addProperty("Message", "Record doesn't exist in the SupplyChainPartners");
									return resObj;
								}
							} else {
								return scpObj;
							}
						} else {
							// record not exist in the
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							resObj.addProperty("Message", "Record not exist in the PGPaymentConfigs for the AggregatorID :" + AGGRICICI + "and PGID:" + PGID);
							return resObj;
						}

					} else {
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Message", "Unable to fetch records from the  PGPaymentConfigs Table");
						return resObj;
					}
				} else {
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Message", "Typeset doesn't exist");
					return resObj;
				}

			} else {
				return configObj;
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			return resObj;
		}

	}

	private JsonObject getScpRecord(HttpServletResponse response, boolean debug, String cpType, String cpGuid, String aggregatorId) {
		JsonObject resObj = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try {
			String odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			if (debug) {
				response.getWriter().println("url:" + odataUrl);
				response.getWriter().println("password:" + password);
				response.getWriter().println("username:" + username);
			}
			String userpass = username + ":" + password;
			String executeUrl = odataUrl + "SupplyChainPartners?$filter=AggregatorId%20eq%20%27" + aggregatorId + "%27%20and%20SCPGuid%20eq%20%27" + cpGuid + "%27%20and%20SCPType%20eq%20%27" + cpType + "%27";
			if (debug) {
				response.getWriter().println("SupplyChainPartners executeUrl:" + executeUrl);
			}
			JsonObject scpObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("SupplyChainPartners Records:" + scpObj);
			}
			return scpObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);

			}
			resObj.addProperty("Message", "" + ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
		}
		return resObj;
	}

	public boolean checkODEligibleDate(HttpServletResponse response, String odEligibleDate, boolean debug) throws Exception {
		CommonUtils commonUtils = new CommonUtils();
		try {
			odEligibleDate = commonUtils.convertLongDateToString(response, odEligibleDate, debug);
			if (debug) {
				response.getWriter().println("checkODEligibleDate:" + odEligibleDate);
				response.getWriter().println("odEligibleDate:" + odEligibleDate);
			}
			String currentDate = commonUtils.getCurrentDateYYYYMMDDFormat(response, debug);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date currDate = dateFormat.parse(currentDate);
			Date odEleDate = dateFormat.parse(odEligibleDate);
			if (debug) {
				response.getWriter().println("currDate:" + currDate);
				response.getWriter().println("odEleDate:" + odEleDate);
			}
			if (odEleDate.after(currDate)) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			throw ex;
		}
	}

	public JsonObject insertEligibilityRecord(HttpServletRequest request, String odataUrl, String userpass, JsonObject bpRecord, HttpServletResponse response, boolean debug) {
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		JsonObject insertEligibileRoc = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			String createdBy = commonUtils.getUserPrincipal(request, "name", response);
			String createdAt = commonUtils.getCreatedAtTime();
			long createdOnInMillis = commonUtils.getCreatedOnDate();
			String aggrId = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
			String generateGUID = commonUtils.generateGUID(32);
			insertEligibileRoc.addProperty("RecordID", generateGUID);
			insertEligibileRoc.addProperty("ObjectType", bpRecord.get("BPGuid").getAsString());
			insertEligibileRoc.addProperty("ObjectTypeID", "BP");
			insertEligibileRoc.addProperty("CorrelationID", bpRecord.get("BPGuid").getAsString());
			insertEligibileRoc.addProperty("EligibilityStatusID", "000010");
			insertEligibileRoc.addProperty("EligibilityTypeID", "AML");
			insertEligibileRoc.addProperty("StatusID", "000010");
			insertEligibileRoc.addProperty("AggregatorID", aggrId);
			insertEligibileRoc.addProperty("CreatedBy", createdBy);
			insertEligibileRoc.addProperty("CreatedAt", createdAt);
			insertEligibileRoc.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");

			if (debug) {
				response.getWriter().println("insert eligibility Record:" + insertEligibileRoc);
			}
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			// Changes on 20231118
			//String executeUrl = odataUrl + "Eligibility";
			String executeUrl = "Eligibility";
		
			BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeUrl).headers(changeSetHeaders).body(insertEligibileRoc.toString()).build();
			changeSet.add(changeRequest);
			JsonArray bpContPersons = bpRecord.get("BPContactPersons").getAsJsonObject().get("results").getAsJsonArray();

			for (int i = 0; i < bpContPersons.size(); i++) {
				JsonObject bpContPerson = bpContPersons.get(i).getAsJsonObject();
				JsonObject insertBpContEligibility = new JsonObject();
				generateGUID = commonUtils.generateGUID(32);
				insertBpContEligibility.addProperty("RecordID", generateGUID);
				insertBpContEligibility.addProperty("ObjectType", bpContPerson.get("BPCntPrsnGuid").getAsString());// BPContactPersons GUID
				insertBpContEligibility.addProperty("ObjectTypeID", "BPCNTP");
				insertBpContEligibility.addProperty("CorrelationID", bpRecord.get("BPGuid").getAsString());
				insertBpContEligibility.addProperty("EligibilityStatusID", "000010");
				insertBpContEligibility.addProperty("EligibilityTypeID", "AML");
				insertBpContEligibility.addProperty("StatusID", "000010");
				insertBpContEligibility.addProperty("AggregatorID", aggrId);
				insertBpContEligibility.addProperty("CreatedBy", createdBy);
				insertBpContEligibility.addProperty("CreatedAt", createdAt);
				insertBpContEligibility.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				if (debug) {
					response.getWriter().println("insert BpContract Person eligibility Records:" + insertBpContEligibility);
				}
				BatchChangeSetPart changeRequest1 = BatchChangeSetPart.method("POST").uri(executeUrl).headers(changeSetHeaders).body(insertBpContEligibility.toString()).build();
				changeSet.add(changeRequest1);
			}
			batchParts.add(changeSet);

			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			if (debug)
				response.getWriter().println("payload :"+payload);
				
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordUpdated = true;
			String responsebOdy = null;
			JsonArray resArray = new JsonArray();
			JsonParser parse = new JsonParser();
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (statusCode.equalsIgnoreCase("201")) {
					responsebOdy = singleRes.getBody();
					JsonObject eligiblitilyRoc = (JsonObject) parse.parse(responsebOdy);
					resArray.add(eligiblitilyRoc);
				} else {
					recordUpdated = false;
					responsebOdy = singleRes.getBody();
					break;
				}
			}

			if (debug) {
				response.getWriter().println("responsebOdy:" + responsebOdy);
				response.getWriter().println("resArray:" + resArray);
			}

			if (recordUpdated) {
				resObj.add("Message", resArray);
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
			} else {
				resObj.addProperty("Message", responsebOdy);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Remarks", "Inserting Records to Eligibility Table Failed");
			}
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage() + "");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;

		}

	}

	/* private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	} */

	public JsonObject insertIntoSCF1Table(String cpGuid, String cpType, String aggregatorID, String odataUrl, String userpass, boolean debug, HttpServletResponse response, HttpServletRequest request) {
		CommonUtils commonUtils = new CommonUtils();
		String executeURL = "";
		JsonObject resObj = new JsonObject();
		JsonObject insertScfObj = new JsonObject();
		try {
			String createdBy = commonUtils.getUserPrincipal(request, "name", response);
			String createdAt = commonUtils.getCreatedAtTime();
			long createdOnInMillis = commonUtils.getCreatedOnDate();

			executeURL = odataUrl + "SupplyChainFinanceEligibility?$filter=CPGUID%20eq%20%27" + cpGuid + "%27%20and%20CPTypeID%20eq%20%27" + cpType + "%27%20and%20AggregatorID%20eq%20%27" + aggregatorID + "%27%20and%20EligibilityTypeID%20eq%20%27" + "AML" + "%27";
			if (debug) {
				response.getWriter().println("SupplyChainFinanceEligibility execute url:" + executeURL);
			}
			JsonObject scfResFrmDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("scfResFrmDb:" + scfResFrmDb);
			}
			if (scfResFrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
				String guid = commonUtils.generateGUID(36);
				insertScfObj.addProperty("ID", guid);
				insertScfObj.addProperty("EligibilityTypeID", "AML");
				insertScfObj.addProperty("EligibilityStatus", "200010");
				insertScfObj.addProperty("CPGUID", cpGuid);
				insertScfObj.addProperty("CPTypeID", cpType);
				insertScfObj.addProperty("AggregatorID", aggregatorID);
				insertScfObj.addProperty("CreatedBy", createdBy);
				insertScfObj.addProperty("CreatedAt", createdAt);
				insertScfObj.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				if (debug) {
					response.getWriter().println("SCf1 Table Insert paylaod insertScfObj: " + insertScfObj);
				}
				if (scfResFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					// if already record exist delete the exist record and insert a new Record.
					String scfGuid = scfResFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("ID").getAsString();
					executeURL = odataUrl + "SupplyChainFinanceEligibility('" + scfGuid + "')";
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility delete URL:" + executeURL);
					}
					JsonObject executeDelete = commonUtils.executeODataDelete(executeURL, userpass, response, request, debug, "PYGWHANA");
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility delete Response:" + executeDelete);
					}
					if (executeDelete.get("Status").getAsString().equalsIgnoreCase("000001")) {
						executeURL = odataUrl + "SupplyChainFinanceEligibility";
						if (debug) {
							response.getWriter().println("SupplyChainFinanceEligibility insert url:" + executeURL);
						}
						JsonObject insertScfRes = commonUtils.executePostURL(executeURL, userpass, response, insertScfObj, request, debug, "PYGWHANA");
						return insertScfRes;
					} else {
						return executeDelete;
					}

				} else {
					// insert a new record
					executeURL = odataUrl + "SupplyChainFinanceEligibility";
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility insert url:" + executeURL);
					}
					JsonObject insertScfRes = commonUtils.executePostURL(executeURL, userpass, response, insertScfObj, request, debug, "PYGWHANA");
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility insert Response: " + insertScfRes);
					}
					return insertScfRes;
				}

			} else {
				return scfResFrmDb;
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;

		}
	}

	public JsonObject getAMLPGPaymentConfigs(HttpServletResponse response, boolean debug) {
		String executeUrl = "", username = "", password = "", userpass = "", odataUrl = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		try {
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userpass = username + ":" + password;
			executeUrl = odataUrl + "PGPaymentConfigs?$filter=AggregatorID%20eq%20%27" + AGGRICICI + "%27%20and%20PGID%20eq%20%27" + PGID + "%27";
			if (debug) {
				response.getWriter().println("PGPaymentConfigs executeURL:" + executeUrl);
			}
			resObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;
		}
	}

	public JsonObject getAMLPGPaymentConfigStats(String configHeaderGuid, String pgTxnStatus, HttpServletResponse response, boolean debug) {
		String executeUrl = "", username = "", password = "", userpass = "", odataUrl = "";
		CommonUtils commonUtils = new CommonUtils();
		JsonObject resObj = new JsonObject();
		try {
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userpass = username + ":" + password;

			executeUrl = odataUrl + "PGPaymentConfigStats?$filter=ConfigHeaderGUID%20eq%20%27" + configHeaderGuid + "%27%20and%20PGTxnStatus%20eq%20%27" + pgTxnStatus + "%27";
			if (debug) {
				response.getWriter().println("PGPaymentConfigStats execute URL:" + executeUrl);
			}
			resObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("PGPaymentConfigStats resObj:" + resObj);
			}
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;
		}
	}

	public JsonObject updateEligibilityRecords(String statusFrmInputPayload, String odataUrl, String userpass, JsonObject eligibilityRec, String paymentStatus, HttpServletResponse response, HttpServletRequest request, String eligibilityStatus1, String eligibilityStatus2, String aggrId, String additionalDays, Properties props, boolean debug) {
		CommonUtils commonUtils = new CommonUtils();
		String executeUrl = "";
		JsonObject resObj = new JsonObject();
		Set<String> emails = new HashSet<>();
		try {
			String changedBy = commonUtils.getUserPrincipal(request, "name", response);
			String changedAt = commonUtils.getCreatedAtTime();
			long changedOn = commonUtils.getCreatedOnDate();

			String recordID = eligibilityRec.get("RecordID").getAsString();
			String correlationID = eligibilityRec.get("CorrelationID").getAsString();
			
			executeUrl = odataUrl + "Eligibility('" + recordID + "')";
			if (debug) {
				response.getWriter().println("Get Eligibility Record Execute URL:" + executeUrl);
			}
			JsonObject eligibilityRecFrmDb = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {

				response.getWriter().println("eligibilityRecFrmDb: " + eligibilityRecFrmDb);
				response.getWriter().println("statusFrmInputPayload: " + statusFrmInputPayload);
			}
			eligibilityRecFrmDb = eligibilityRecFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject();
			eligibilityRecFrmDb.remove("__metadata");
			eligibilityRecFrmDb.remove("EligibilityStatusID");
			eligibilityRecFrmDb.addProperty("EligibilityStatusID", paymentStatus);
			eligibilityRecFrmDb.addProperty("ServiceProviderRef", statusFrmInputPayload);
			eligibilityRecFrmDb.addProperty("AggregatorID", aggrId);
			eligibilityRecFrmDb.addProperty("ChangedBy", changedBy);
			eligibilityRecFrmDb.addProperty("ChangedAt", changedAt);
			eligibilityRecFrmDb.addProperty("ChangedOn", "/Date(" + changedOn + ")/");
			int extraDays = Integer.parseInt(additionalDays);
			if (statusFrmInputPayload.equalsIgnoreCase("FFF_OK") || statusFrmInputPayload.equalsIgnoreCase("FFFBAD")) {
				// pic from typeset
				Date currentDatePlus60 = commonUtils.getDate(extraDays);
				long validTo = currentDatePlus60.getTime();
				eligibilityRecFrmDb.addProperty("ValidTo", "/Date(" + validTo + ")/");
			}
			if (debug) {
				response.getWriter().println("Updated Eligibility Record Execute Url:" + executeUrl);
				response.getWriter().println("Updated Eligibility Record Payload:" + eligibilityRecFrmDb);
			}
			JsonObject executeUpdate = commonUtils.executeUpdate(executeUrl, userpass, response, eligibilityRecFrmDb, request, debug, "PYGWHANA");
			if (debug) {
				response.getWriter().println("Update eligibility Record Response:" + executeUpdate);
			}
			if (executeUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
				// fetch all the records from Eligibility Table Based on correlationID and Associated BPCNTY
				executeUrl = odataUrl + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=CorrelationID%20eq%20%27" + correlationID + "%27";
				if (debug) {
					response.getWriter().println("BPEligibilityRecords Execute URL:" + executeUrl);
				}
				eligibilityRecFrmDb = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
				if (debug) {
					response.getWriter().println("eligibilityRocFrmDb:" + eligibilityRecFrmDb);
				}
				if (eligibilityRecFrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
					JsonObject eligibilityObj = eligibilityRecFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
					String eligibilityStatus = eligibilityObj.get("EligibilityStatusID_SCF4").getAsString();
					String scfGuid = eligibilityObj.get("ID_AML").getAsString();
					String cpGuid=eligibilityObj.get("CPGuid").getAsString();
					String cpType=eligibilityObj.get("CPType").getAsString();
					if(debug){
						response.getWriter().println("scfGuid:"+scfGuid);
						response.getWriter().println("cpGuid:"+cpGuid);
						response.getWriter().println("cpType:"+cpType);
					}
					boolean isAllStatusIdSame = true;
					if (!paymentStatus.equalsIgnoreCase(eligibilityStatus)) {
						isAllStatusIdSame = false;
					}
					if (isAllStatusIdSame) {
						JsonArray bpCntpArry = eligibilityObj.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
						for (int i = 0; i < bpCntpArry.size(); i++) {
							JsonObject bpCntpObj = bpCntpArry.get(i).getAsJsonObject();
							
							if (!bpCntpObj.get("EligibilityStatusID_SCF4").getAsString().equalsIgnoreCase(paymentStatus)) {
								isAllStatusIdSame = false;
							}
						}
					}

					if (debug) {
						response.getWriter().println("isAllStatusIdSame:" + isAllStatusIdSame);
						//response.getWriter().println("allStatusOkorBad:" + allStatusOkorBad);
						response.getWriter().println("scfGuid:" + scfGuid);
					}
					executeUrl = odataUrl + "SupplyChainFinanceEligibility?$filter=ID%20eq%20%27" + scfGuid + "%27";
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility executeURL:" + executeUrl);
					}
					JsonObject scf1RecordFrmDb = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
					if (debug) {
						response.getWriter().println("SupplyChainFinanceEligibility Records scf1RecordFrmDb:" + scf1RecordFrmDb);
					}
					if (scf1RecordFrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
						if (scf1RecordFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							JsonObject scf1Record = scf1RecordFrmDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();

							if (isAllStatusIdSame) {
								if (paymentStatus.equalsIgnoreCase("000060")) {
									// pick from Same TypeSet.
									Date curDatePlus90Days = commonUtils.getDate(extraDays);
									if (debug) {
										response.getWriter().println("curDatePlus90Days:" + curDatePlus90Days);
									}
									long validTo = curDatePlus90Days.getTime();
									scf1Record.remove("ValidTo");
									scf1Record.addProperty("ValidTo", "/Date(" + validTo + ")/");
								} else {
									scf1Record.remove("ValidTo");
								}
								scf1Record.remove("EligibilityStatus");
								scf1Record.remove("__metadata");
								scf1Record.addProperty("EligibilityStatus", eligibilityStatus1);
								scf1Record.addProperty("EligibilityTypeID", "AML");
								scf1Record.addProperty("ChangedBy", changedBy);
								scf1Record.addProperty("ChangedAt", changedAt);
								scf1Record.addProperty("ChangedOn", "/Date(" + changedOn + ")/");
								if (debug) {
									response.getWriter().println("Updated SCF1 Payload:" + scf1Record);
								}
								executeUrl = odataUrl + "SupplyChainFinanceEligibility('" + scfGuid + "')";
								if (debug) {
									response.getWriter().println("SupplyChainFinanceEligibility Update URL:" + executeUrl);
								}
								JsonObject scfUpdatedRes = commonUtils.executeUpdate(executeUrl, userpass, response, scf1Record, request, debug, "PYGWHANA");
								if (debug) {
									response.getWriter().println("scfUpdatedRes:" + scfUpdatedRes);
								}
								if (scfUpdatedRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
										
										if(cpType.equalsIgnoreCase("60")){
											// fetch from VENDERCARTABLE  //EMAIL_ID
											executeUrl=odataUrl+"VENDORCOR?$filter=AGGRID%20eq%20%27"+aggrId+"%27%20and%20CP_TYPE%20eq%20%27"+cpType+"%27%20and%20CP_GUID%20eq%20%27"+cpGuid+"%27";
										}else{
											// fetch from CorpOpenionReport Table
											executeUrl=odataUrl+"CorporateOpinionReport?$filter=AggregatorId%20eq%20%27"+aggrId+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20CPGuid%20eq%20%27"+cpGuid+"%27";
										}
										if(debug){
											response.getWriter().println("executeUrl:"+executeUrl);
										}
										JsonObject dbFrmObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
										if(debug){
											response.getWriter().println("dbFrmObj:"+dbFrmObj);
										}
										if(dbFrmObj.get("Status").getAsString().equalsIgnoreCase("000001")){
											if(dbFrmObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
												JsonArray emailArray = dbFrmObj.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
											
											for (int i = 0; i < emailArray.size(); i++) {
												JsonObject emilObj = emailArray.get(i).getAsJsonObject();
												if (cpType.equalsIgnoreCase("60")) {
													if (!emilObj.get("EMAIL_ID").isJsonNull() && !emilObj.get("EMAIL_ID").getAsString().equalsIgnoreCase("")) {
														emails.add(emilObj.get("EMAIL_ID").getAsString());
													}
												} else {
													if (!emilObj.get("EmailID").isJsonNull() && !emilObj.get("EmailID").getAsString().equalsIgnoreCase("")) {
														emails.add(emilObj.get("EmailID").getAsString());
													}
												}

											}
											if(debug){
												response.getWriter().println("Total Valid EmailIds:"+emails.size());
											}
											if (emails.size() > 0) {
												resObj = sendEmails(emails, response, props, commonUtils, debug);
												return resObj;
											} else {
												resObj.addProperty("Status", "000002");
												resObj.addProperty("ErrorCode", "J002");
												if (cpType.equalsIgnoreCase("60")) {
													resObj.addProperty("Message", "Emailids doesn't exist in the  VENDORCOR Table");
												} else {
													resObj.addProperty("Message", "Emailids doesn't exist in the CorporateOpinionReport Table");
												}
												return resObj;
											}
										} else {
											if (cpType.equalsIgnoreCase("60")) {
												resObj.addProperty("Message", "Record doesn't exist in the VENDORCOR Table");
											} else {
												resObj.addProperty("Message", "Record doesn't exist in the CorporateOpinionReport Table");
											}
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "J002");
											return resObj;
										}
										}else{
											return dbFrmObj;
										}
								} else {
									resObj.addProperty("Message", "Update Failed");
									resObj.addProperty("Status", "000002");
									if (paymentStatus.equalsIgnoreCase("000060")) {
										resObj.addProperty("ErrorCode", "AML00009");
									} else {
										resObj.addProperty("ErrorCode", "AML00008");
									}
									return resObj;
								}
							} else {
								scf1Record.remove("EligibilityStatus");
								scf1Record.remove("__metadata");
								scf1Record.addProperty("EligibilityStatus", eligibilityStatus2);
								scf1Record.remove("ValidTo");
								scf1Record.addProperty("EligibilityTypeID", "AML");
								if (debug) {
									response.getWriter().println("Updated SCF1 Payload:" + scf1Record);
								}
								executeUrl = odataUrl + "SupplyChainFinanceEligibility('" + scfGuid + "')";
								if (debug) {
									response.getWriter().println("SupplyChainFinanceEligibility Update URL:" + executeUrl);
								}
								JsonObject scfUpdatedRes = commonUtils.executeUpdate(executeUrl, userpass, response, scf1Record, request, debug, "PYGWHANA");
								if (debug) {
									response.getWriter().println("scfUpdatedRes:" + scfUpdatedRes);
								}
								if (scfUpdatedRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
									resObj.addProperty("Message", "Updated Successfully");
									resObj.addProperty("Status", "000001");
									resObj.addProperty("ErrorCode", "");
									return resObj;
								} else {
									resObj.addProperty("Message", "Update Failed");
									resObj.addProperty("Status", "000002");
									if (paymentStatus.equalsIgnoreCase("000060")) {
										resObj.addProperty("ErrorCode", "AML00009");
									} else {
										resObj.addProperty("ErrorCode", "AML00008");
									}
									return resObj;
								}

							}

							// if all the records StatusProviderRef is FFF_OK or FFFBAD Send the email
						} else {
							resObj.addProperty("Message", "Record doesn't exist in the SupplyChainFinanceEligibility Table for The ID:" + scfGuid);
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
							return resObj;
						}
					} else {
						return scf1RecordFrmDb;
					}

				} else {
					// eligibility record not exist
					resObj.addProperty("Message", "Record not exist in the EligibilityRecords Table for the EligibilityTypeID is AML");
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
					return resObj;
				}

			} else {
				executeUpdate.addProperty("Status", "000002");
				return executeUpdate;
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ExceptionTrace", buffer.toString());
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;
		}

	}

	private JsonObject sendEmails(Set<String> emails, HttpServletResponse response, Properties props, CommonUtils commonUtils, boolean debug) {
		Properties emailProps = new Properties();
		JsonObject resObj = new JsonObject();
		try {
			String fromMailID = commonUtils.getODataDestinationProperties("emailid", DestinationUtils.PLATFORM_EMAIL);
			String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PLATFORM_EMAIL);
			String supportEmail=commonUtils.getODataDestinationProperties("SupportEmail", DestinationUtils.PLATFORM_EMAIL);
			if (debug) {
				response.getWriter().println("fromMailID:" + fromMailID);
				response.getWriter().println("supportEmail:" + supportEmail);
			}
			emailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth"));
			emailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable"));
			emailProps.put("mail.smtp.host", props.getProperty("mail.smtp.host"));
			emailProps.put("mail.smtp.port", props.getProperty("mail.smtp.port"));
			Session session = Session.getInstance(emailProps, new javax.mail.Authenticator() {
				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromMailID, password);
				}
			});
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(fromMailID));
			for (String email : emails) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			}
			
			if(supportEmail!=null && !supportEmail.equalsIgnoreCase("")){
				msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(supportEmail));
			}
			String subject = "CorpConnect Platform - Confirmation to check Sanction status for SCF OD Limit";
			if(debug){
				response.getWriter().println("subject:"+subject);
			}
			Multipart emailContent = new MimeMultipart();
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setText("Dear Customer,\n\nPlease login to the CorpConnect platform and check your sanction status for SCF OD Limit.\n\nDo reach out to your concerned RM in case of any issues.\n\n\n\nThanks and Regards,\nFinessart Platform");
			emailContent.addBodyPart(textBodyPart);
			msg.setSubject(subject);
			msg.setContent(emailContent);
			Transport.send(msg);
			resObj.addProperty("Status", "000001");
			resObj.addProperty("ErrorCode", "");
			resObj.addProperty("Message", "Mail sent successfully");
		} catch (Exception ex) {
			String name = ex.getClass().getName();
			StringBuffer buffer = new StringBuffer();
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("Message", name + "--->" + ex.getLocalizedMessage());
			resObj.addProperty("ErrorCode", "J0002");
			resObj.addProperty("ExceptionTrace", buffer.toString());
		}
		return resObj;
	}

	public JsonObject deleteEligibilityRecords(String odataUrl, String userpass, boolean debug, HttpServletResponse response, JsonArray eligibilityRocArray) {
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			for (int i = 0; i < eligibilityRocArray.size(); i++) {
				JsonObject eligibilityRecord = eligibilityRocArray.get(i).getAsJsonObject();
				String rocId = eligibilityRecord.get("RecordID").getAsString();
				// Changes on 20231118
				//String executeUrl = odataUrl + "Eligibility('" + rocId + "')";
				String executeUrl = "Eligibility('" + rocId + "')";

				if (debug) {
					response.getWriter().println("Delete eligibility record query: " + executeUrl);
				}
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
				changeSet.add(changeRequest);
			}
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);

			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordDeleted = true;
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (!statusCode.equalsIgnoreCase("204")) {
					recordDeleted = false;
					if (debug) {
						response.getWriter().println("Error getting while deleting records:" + singleRes.getBody());
					}
					resObj.addProperty("Message", "deleting Eligibility Records Failed, error message:" + singleRes.getBody());
					break;
				}
			}
			if (recordDeleted) {
				resObj.addProperty("Message", "Record Deleted Successfully");
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

	public JsonObject updateELigibilityRecords(JsonObject updateEligibilityRecerd, String inputBpGUID, String oDataURL, String userpass, HttpServletResponse response, boolean debug) throws IOException {
		JsonObject resObj = new JsonObject();
		String executeURL = "";
		CommonUtils commonUtils = new CommonUtils();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			// fetch the records from the BPHeader Table for The BpGuid.
			executeURL = oDataURL + "BPHeaders?$expand=BPContactPersons&$filter=BPGuid%20eq%20%27" + inputBpGUID + "%27";
			if (debug) {
				response.getWriter().println("BPHeaders execute URl:" + executeURL);
			}
			JsonObject bpObjFromDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);
			if (debug) {
				response.getWriter().println("BPHeaders Response Obj:" + bpObjFromDb);
			}
			if (bpObjFromDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (bpObjFromDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					JsonArray bpArray = bpObjFromDb.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
					JsonObject bpRecord = bpArray.get(0).getAsJsonObject();
					JsonArray bpContPersons = bpRecord.get("BPContactPersons").getAsJsonObject().get("results").getAsJsonArray();
					if (debug) {
						response.getWriter().println("bpContPersons Person Updating :" + bpContPersons);
					}

					if (debug) {
						response.getWriter().println("Update Eligibility Records :" + updateEligibilityRecerd);
					}
					JsonObject eligibilityRoc = updateEligibilityRecerd.get("Message").getAsJsonObject();
					if (eligibilityRoc.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						JsonArray eligibilityArray = eligibilityRoc.get("d").getAsJsonObject().get("results").getAsJsonArray();
						if (debug) {
							response.getWriter().println("eligibilityRoc:" + eligibilityRoc);
						}
						String bpGudFrmEligibilityRec = eligibilityArray.get(0).getAsJsonObject().get("BPGuid").getAsString();
						executeURL = oDataURL + "Eligibility?$filter=CorrelationID%20eq%20%27" + bpGudFrmEligibilityRec + "%27";
						if (debug) {
							response.getWriter().println("executeURL:" + executeURL);
						}
						JsonObject eligibilityRecfrmDb = commonUtils.executeODataURL(executeURL, userpass, response, debug);

						if (debug) {
							response.getWriter().println("Update Eligibility Records:" + eligibilityRecfrmDb);
						}
						if (eligibilityRecfrmDb.get("Status").getAsString().equalsIgnoreCase("000001")) {
							JsonObject eligibility = eligibilityRecfrmDb.get("Message").getAsJsonObject();
							if (eligibility.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								JsonArray eligibilityRocUpArray = eligibility.get("d").getAsJsonObject().get("results").getAsJsonArray();
								List<JsonObject> updateEligiBilityRecord = new ArrayList<>();
								List<JsonObject> deleteEligiBilityRecord = new ArrayList<>();
								List<JsonObject> insertEligibility = new ArrayList<>();
								for (int i = 0; i < bpContPersons.size(); i++) {
									JsonObject bpContactPerson = bpContPersons.get(i).getAsJsonObject();
									if (!bpContactPerson.get("PanNo").isJsonNull() && !bpContactPerson.get("PanNo").getAsString().equalsIgnoreCase("")) {
										String panNumber = bpContactPerson.get("PanNo").getAsString();
										if (debug) {
											response.getWriter().println("panNumber from BPContact Person Table :" + panNumber);
										}
										Stream<JsonElement> stream = StreamSupport.stream(eligibilityArray.spliterator(), true);
										stream.map(element -> (JsonObject) element).forEach(obj -> {
											JsonObject updateJson = new JsonObject();
											if (!obj.get("CNTPPAN").isJsonNull() && obj.get("CNTPPAN").getAsString().equalsIgnoreCase(panNumber)) {
												updateJson.addProperty("ObjectType", obj.get("ObjectType").getAsString());
												updateJson.addProperty("BPCntPrsnGuid", bpContactPerson.get("BPCntPrsnGuid").getAsString());
												updateEligiBilityRecord.add(updateJson);
											} else {
												if (!obj.get("CNTPPAN").isJsonNull()) {
													updateJson.addProperty("ObjectType", obj.get("ObjectType").getAsString());
													deleteEligiBilityRecord.add(updateJson);
												}
												if (updateJson.has("ObjectType")) {
													updateJson.remove("ObjectType");
												}
												updateJson.addProperty("BPCntPrsnGuid", bpContactPerson.get("BPCntPrsnGuid").getAsString());
												insertEligibility.add(updateJson);
											}
										});

									}

								}
								Map<String, String> changeSetHeaders = new HashMap<String, String>();
								changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
								changeSetHeaders.put("Accept", APPLICATION_JSON);
								changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
								List<BatchPart> batchParts = new ArrayList<BatchPart>();
								BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();

								Stream<JsonElement> stream = StreamSupport.stream(eligibilityRocUpArray.spliterator(), true);
								stream.map(jsonElement -> (JsonObject) jsonElement).forEach(updateObj -> {
									String objType = updateObj.get("ObjectType").getAsString();
									String objTypeId = updateObj.get("ObjectTypeID").getAsString();
									final String recordId = updateObj.get("RecordID").getAsString();
									if (objTypeId.equalsIgnoreCase("BP")) {
										updateObj.remove("__metadata");
										updateObj.remove("CorrelationID");
										updateObj.addProperty("CorrelationID", inputBpGUID);
										// Changes on 20231118
										// String executeUrl = oDataURL + "Eligibility('" + recordId + "')";
										String executeUrl = "Eligibility('" + recordId + "')";
										BatchChangeSetPart changeRequest = BatchChangeSetPart.method("PUT").uri(executeUrl).headers(changeSetHeaders).body(updateObj.toString()).build();
										changeSet.add(changeRequest);
									} else {
										Optional<JsonObject> findAny2 = updateEligiBilityRecord.stream().filter(jsonobj -> jsonobj.get("ObjectType").getAsString().equalsIgnoreCase(objType)).findAny();
										if (findAny2.isPresent()) {
											JsonObject jsonObject = findAny2.get();
											updateObj.remove("__metadata");
											updateObj.remove("CorrelationID");
											updateObj.remove("ObjectType");
											updateObj.addProperty("CorrelationID", inputBpGUID);
											updateObj.addProperty("ObjectType", jsonObject.get("BPCntPrsnGuid").getAsString());
											if (debug) {
												try {
													response.getWriter().println("Update Eligibility Records Payload:" + updateObj);
												} catch (IOException ex) {

												}
											}
											// Changes on 20231118
											// String executeUrl = oDataURL + "Eligibility('" + recordId + "')";
											String executeUrl = "Eligibility('" + recordId + "')";

											BatchChangeSetPart changeRequest = BatchChangeSetPart.method("PUT").uri(executeUrl).headers(changeSetHeaders).body(updateObj.toString()).build();
											changeSet.add(changeRequest);
										} else {
											Optional<JsonObject> findAny3 = deleteEligiBilityRecord.stream().filter(jsonobj -> jsonobj.get("ObjectType").getAsString().equalsIgnoreCase(objType)).findAny();
											if (findAny3.isPresent()) {
												// delete Eligibility Record
												// Changes on 20231118
												// String executeUrl = oDataURL + "Eligibility('" + recordId + "')";
												String executeUrl = "Eligibility('" + recordId + "')";
												if (debug) {
													try {
														response.getWriter().println("delete Eligibility Record executeUrl:" + executeUrl);
													} catch (Exception ex) {

													}
												}
												BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
												changeSet.add(changeRequest);
											} else {
												// insert the Eligibility
												// Records
												JsonObject insertBpContEligibility = new JsonObject();
												String guid = commonUtils.generateGUID(36);
												insertBpContEligibility.addProperty("RecordID", guid);
												insertBpContEligibility.addProperty("ObjectType", objType);// BPContactPersons GUID
												insertBpContEligibility.addProperty("ObjectTypeID", "BPCNTP");
												insertBpContEligibility.addProperty("CorrelationID", inputBpGUID);
												insertBpContEligibility.addProperty("EligibilityStatusID", "000010");
												insertBpContEligibility.addProperty("EligibilityTypeID", "AML");
												insertBpContEligibility.addProperty("StatusID", "000010");
												if (debug) {
													try {
														response.getWriter().println("insertBpContEligibility:" + insertBpContEligibility);
													} catch (Exception ex) {

													}
												}
												
												// Changes on 20231118
												// String executeUrl = oDataURL + "Eligibility";
												String executeUrl = "Eligibility";
												BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeUrl).headers(changeSetHeaders).body(insertBpContEligibility.toString()).build();
												changeSet.add(changeRequest);
											}

										}

									}
								});
								batchParts.add(changeSet);

								InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
								String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
								final HttpPost post = new HttpPost(URI.create(oDataURL + "$batch"));
								post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
								post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
								HttpEntity entity = new StringEntity(payload);
								post.setEntity(entity);
								// HttpResponse batchResponse = getHttpClient().execute(post);
								HttpResponse batchResponse = client.execute(post);

								InputStream responseBody = batchResponse.getEntity().getContent();
								String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
								String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
								List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
								boolean batchSuccess = true;
								String responsebOdy = null;
								JsonArray resArray = new JsonArray();
								JsonParser parse = new JsonParser();
								for (BatchSingleResponse singleRes : responses) {
									String statusCode = singleRes.getStatusCode();
									if (debug) {
										response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
									}
									int status = Integer.parseInt(statusCode);

									if ((status / 100) != 2) {
										batchSuccess = false;
										responsebOdy = singleRes.getBody();
										if (debug) {
											response.getWriter().println("Batch Response body responsebOdy:" + responsebOdy);
										}
										JsonObject eligiblitilyRoc = (JsonObject) parse.parse(responsebOdy);
										resArray.add(eligiblitilyRoc);
									}
								}

								if (debug) {
									response.getWriter().println("batchSuccess:" + batchSuccess);
								}
								if (batchSuccess) {
									resObj.addProperty("Message", "BPContactPersons Updared Successfully");
									resObj.addProperty("ErrorCode", "");
									resObj.addProperty("Status", "000001");
									return resObj;
								} else {
									resObj.add("Message", resArray);
									resObj.addProperty("ErrorCode", "J002");
									resObj.addProperty("Status", "000002");
									return resObj;
								}
							} else {
								resObj.addProperty("Message", "Eligibility Records  not Exist for the Given BPGuid:" + bpGudFrmEligibilityRec);
								resObj.addProperty("ErrorCode", "J002");
								resObj.addProperty("Status", "000002");
								return resObj;
							}
						} else {
							return eligibilityRecfrmDb;
						}
					} else {
						resObj.addProperty("Message", "Eligibility Records doesn't exist");
						resObj.addProperty("ErrorCode", "J002");
						resObj.addProperty("Status", "000002");
						return resObj;

					}

				} else {
					resObj.addProperty("Message", "BPRecord not Exist for the Given BPGuid:" + inputBpGUID);
					resObj.addProperty("ErrorCode", "J002");
					resObj.addProperty("Status", "000002");
					return resObj;

				}
			} else {
				return bpObjFromDb;
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ex.getLocalizedMessage());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ExceptionStackTrace", buffer.toString());

		}
		return resObj;
	}

	public JsonObject getValidToDate(HttpServletResponse response, CommonUtils commonUtils, boolean debug) {
		JsonObject resObj = new JsonObject();
		try {
			final String odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			final String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			final String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			String userpass = username + ":" + password;
			String executeUrl = odataUrl + "ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" + AGGRICICI + "%27%20and%20Typeset%20eq%20%27PY%27%20and%20Types%20eq%20%27" + "ELIGVLDTO" + "%27";
			if (debug) {
				response.getWriter().println("ConfigTypsetTypeValues Execute Url:" + executeUrl);
			}
			JsonObject typeSetObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("typeSetObj:" + typeSetObj);
			}
			return typeSetObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", "" + ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());

			return resObj;
		}
	}

	public JsonObject getEligibilityRecord(HttpServletRequest request, HttpServletResponse response, String odataUrl, String userpass, List<String> records, boolean debug) {
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		JsonArray eligibiliRecArry = new JsonArray();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			// BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();

			for (int i = 0; i < records.size(); i++) {
				String recordId = records.get(i);
			//String executeUrl = odataUrl + "Eligibility?$filter=RecordID%20eq%20%27" + recordId + "%27";
			String executeUrl = "Eligibility?$filter=RecordID%20eq%20%27" + recordId + "%27";
				if (debug) {
					response.getWriter().println("executeUrl:" + executeUrl);
				}
				BatchQueryPart query = BatchQueryPart.method("GET").uri(executeUrl).headers(changeSetHeaders).build();
				batchParts.add(query);
			}

			// batchParts.add(changeSet);

			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);

			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordUpdated = true;
			String responsebOdy = null;
			JsonParser parse = new JsonParser();
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (statusCode.equalsIgnoreCase("200")) {
					responsebOdy = singleRes.getBody();
					JsonObject eligiblitilyRoc = (JsonObject) parse.parse(responsebOdy);
					eligibiliRecArry.add(eligiblitilyRoc);
				} else {
					recordUpdated = false;
					responsebOdy = singleRes.getBody();
					break;
				}
			}

			if (debug) {
				response.getWriter().println("responsebOdy:" + responsebOdy);
				response.getWriter().println("resArray:" + eligibiliRecArry);
			}

			if (recordUpdated) {
				resObj.add("Message", eligibiliRecArry);
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
			} else {
				resObj.addProperty("Message", responsebOdy);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Remarks", "Getting  Records from Eligibility Table Failed");
			}
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage() + "");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;

		}

	}

	public JsonObject updateAllEligibility(HttpServletRequest request, HttpServletResponse response, String odataUrl, String userpass, JsonArray eligibilityRec, String validTo, boolean debug) {
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			for (int i = 0; i < eligibilityRec.size(); i++) {
				JsonObject eligibleRec = eligibilityRec.get(i).getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
				if (debug) {
					response.getWriter().println("ELigibility Record for update:" + eligibleRec);
				}
				if (eligibleRec.has("__metadata")) {
					eligibleRec.remove("__metadata");
				}
				if (eligibleRec.has("ValidTo")) {
					eligibleRec.remove("ValidTo");
				}
				eligibleRec.addProperty("ValidTo", validTo);
				// Changes on 20231118
				//String executeUrl = odataUrl + "Eligibility('" + eligibleRec.get("RecordID").getAsString() + "')";
				String executeUrl = "Eligibility('" + eligibleRec.get("RecordID").getAsString() + "')";
				if (debug) {
					response.getWriter().println("executeUrl:" + executeUrl);
				}
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("PUT").uri(executeUrl).headers(changeSetHeaders).body(eligibleRec.toString()).build();
				changeSet.add(changeRequest);
			}

			batchParts.add(changeSet);

			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
			final HttpPost post = new HttpPost(URI.create(odataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);

			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
			List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
			boolean recordUpdated = true;
			String responsebOdy = null;
			for (BatchSingleResponse singleRes : responses) {
				String statusCode = singleRes.getStatusCode();
				if (debug) {
					response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
				}
				if (statusCode.equalsIgnoreCase("204") || statusCode.equalsIgnoreCase("200")) {
					recordUpdated = true;
				} else {
					recordUpdated = false;
					responsebOdy = singleRes.getBody();
					break;
				}
			}

			if (debug) {
				response.getWriter().println("responsebOdy:" + responsebOdy);
			}

			if (recordUpdated) {
				resObj.addProperty("Message", "Updated SuucessFully");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");
			} else {
				resObj.addProperty("Message", responsebOdy);
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Remarks", "Updating eligibility Records Failed");
			}
			return resObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage() + "");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			return resObj;

		}

	}

	public JsonObject getCorpConnectEmail(HttpServletResponse response, CommonUtils commonUtils, boolean debug) {
		JsonObject resObj = new JsonObject();
		try {
			final String odataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			final String username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			final String password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			String userpass = username + ":" + password;
			String executeUrl = odataUrl + "ConfigTypsetTypeValues?$filter=AggregatorID%20eq%20%27" + AGGRICICI + "%27%20and%20Typeset%20eq%20%27PY%27%20and%20Types%20eq%20%27" + "CRPCNCTEML" + "%27";
			if (debug) {
				response.getWriter().println("ConfigTypsetTypeValues Execute Url:" + executeUrl);
			}
			JsonObject typeSetObj = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("typeSetObj:" + typeSetObj);
			}
			return typeSetObj;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", "" + ex.getLocalizedMessage());
			resObj.addProperty("ExceptionTrace", buffer.toString());

			return resObj;
		}
	}

	public JsonObject deleteEligibilityRecords(boolean debug, HttpServletResponse response, String correlationId, String ODataUrl, String userpass, CommonUtils commonUtils) {
		String executeUrl = "";
		JsonObject resObj = new JsonObject();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();

			executeUrl = ODataUrl + "Eligibility?$filter=CorrelationID%20eq%20%27" + correlationId + "%27";
			if (debug) {
				response.getWriter().println("execute Eligibility Record Url:" + executeUrl);
			}

			JsonObject odataResponse = commonUtils.executeODataURL(executeUrl, userpass, response, debug);
			if (debug) {
				response.getWriter().println("odataResponse:" + odataResponse);
			}
			if (odataResponse.get("Status").getAsString().equalsIgnoreCase("000001")) {
				if (odataResponse.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					JsonArray eligibilityArry = odataResponse.get("Message").getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();
					for (int i = 0; i < eligibilityArry.size(); i++) {
						String recordId = eligibilityArry.get(i).getAsJsonObject().get("RecordID").getAsString();
						// Changes on 20231118
						// executeUrl = ODataUrl + "Eligibility('" + recordId + "')";
						executeUrl = "Eligibility('" + recordId + "')";
						if (debug) {
							response.getWriter().println("executeUrl:" + executeUrl);
						}
						BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeUrl).headers(changeSetHeaders).build();
						changeSet.add(changeRequest);
					}
					batchParts.add(changeSet);
					
					InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
					String payload = org.apache.commons.io.IOUtils.toString(body, "UTF-8");
					final HttpPost post = new HttpPost(URI.create(ODataUrl + "$batch"));
					post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
					post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
					HttpEntity entity = new StringEntity(payload);
					post.setEntity(entity);
					// HttpResponse batchResponse = getHttpClient().execute(post);
					HttpResponse batchResponse = client.execute(post);
					InputStream responseBody = batchResponse.getEntity().getContent();
					String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
					String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody, "UTF-8");
					List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes, "UTF-8"), contentType);
					boolean recordDeleted = true;
					String responsebOdy="";
					for (BatchSingleResponse singleRes : responses) {
						String statusCode = singleRes.getStatusCode();
						if (debug) {
							response.getWriter().println("BatchSingleResponse statusCode:" + statusCode);
							if (singleRes.getBody() != null) {
								response.getWriter().println("BatchSingleResponse Body:" + singleRes.getBody());
							}
						}
						if (!statusCode.equalsIgnoreCase("204")) {
							recordDeleted = false;
							responsebOdy = singleRes.getBody();
							break;
						}
					}

					if (recordDeleted) {
						resObj.addProperty("Message", "Record Deleted Successfully");
						resObj.addProperty("Status", "000001");
						resObj.addProperty("ErrorCode", "");
					} else {
						resObj.addProperty("Message", responsebOdy);
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
					}
					return resObj;
				}
				resObj.addProperty("Message", "Record doesn't exist");
				resObj.addProperty("Status", "000001");
				resObj.addProperty("ErrorCode", "");

				return resObj;
			} else {
				return odataResponse;
			}

		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			if (ex.getLocalizedMessage() != null) {
				resObj.addProperty("Message", ex.getLocalizedMessage());
			}
			resObj.addProperty("ExceptionTrace", buffer.toString());

		}
		return resObj;

	}

}

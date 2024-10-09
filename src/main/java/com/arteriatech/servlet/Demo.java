package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Configuration
public class Demo extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String input = readRequestBody(request, response);
		try {
			if (input != null && !input.equalsIgnoreCase("")) {
				JSONObject input1 = new JSONObject(input);
				JsonObject insertIntoApproval = insertIntoApproval(input1, request, response);
			} else {
				response.getWriter().println("Invalid Input");
			}
		} catch (Exception ex) {
			StringBuffer buffer = new StringBuffer(ex.getClass().getCanonicalName() + "--->");
			if (ex.getLocalizedMessage() != null) {
				buffer.append(ex.getLocalizedMessage() + "--->");
			}
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (int k = 0; k < stackTrace.length; k++) {
				buffer.append(stackTrace[k]);
			}
			response.getWriter().println(buffer.toString());
		}
	}

	public String readRequestBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = "";
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
		}
		body = jb.toString();
		return body;
	}

	public JsonObject insertIntoApproval(JSONObject parseinput, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		JSONObject input1 = new JSONObject(parseinput);
		String createdAt = "", createdBy = "", id = "";
		long createdOnInMillis = 0;
		JsonObject approvalObj = new JsonObject();
		JsonObject apprvlResponse = new JsonObject();
		try {
			id = generateGUID(36);
			createdBy = "Sap";
			createdAt = getCreatedAtTime();
			createdOnInMillis = getCreatedOnDate();
			approvalObj.addProperty("ID", id);
			boolean run = false;
			approvalObj.addProperty("CreatedBy", createdBy);
			approvalObj.addProperty("CreatedAt", createdAt);
			approvalObj.addProperty("CreatedOn", "/Date(" + createdOnInMillis + ")/");
			approvalObj.addProperty("ChangedBy", createdBy);
			// }
			approvalObj.addProperty("ChangedAt", createdAt);
			approvalObj.addProperty("ChangedOn", "/Date(" + createdOnInMillis + ")/");
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration)
			// ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration =
			// configuration.getConfiguration("PCGWHANA");
			String aggid = "AGGR0008";
			approvalObj.addProperty("AggregatorID", aggid);

			if (parseinput.has("LogObject") && !parseinput.isNull("LogObject")) {
				approvalObj.addProperty("LogObject", parseinput.getString("LogObject"));
				if (parseinput.getString("LogObject").length() > 20) {
					run = true;
					response.getWriter().println("LogObject length is too Large");
				}
			} else {
				approvalObj.addProperty("LogObject", "");
			}

			if (parseinput.has("LogSubObject") && !parseinput.isNull("LogSubObject")) {
				approvalObj.addProperty("LogSubObject", parseinput.getString("LogSubObject"));
				if (parseinput.getString("LogSubObject").length() > 50) {
					run = true;
					response.getWriter().println("LogSubObject length is too Large");
				}
			} else {
				approvalObj.addProperty("LogSubObject", "");
			}

			if (parseinput.has("ExternalNumber") && !parseinput.isNull("ExternalNumber")) {
				approvalObj.addProperty("ExternalNumber", parseinput.getString("ExternalNumber"));
				if (parseinput.getString("ExternalNumber").length() > 100) {
					run = true;
					response.getWriter().println("ExternalNumber length is too Large");
				}
			} else {
				approvalObj.addProperty("ExternalNumber", "");
			}

			if (parseinput.has("LogDate") && !parseinput.isNull("LogDate")) {
				approvalObj.addProperty("LogDate", parseinput.getString("LogDate"));
			} else {
				approvalObj.addProperty("LogDate", "");
			}

			if (parseinput.has("LogUser") && !parseinput.isNull("LogUser")) {
				approvalObj.addProperty("LogUser", parseinput.getString("LogUser"));
				if (parseinput.getString("LogUser").length() > 100) {
					run = true;
					response.getWriter().println("LogUser length is too Large");
				}
			} else {
				approvalObj.addProperty("LogUser", "");
			}

			if (parseinput.has("LogTime") && !parseinput.isNull("LogTime")) {
				approvalObj.addProperty("LogTime", parseinput.getString("LogTime"));
			} else {
				approvalObj.addProperty("LogTime", "");
			}

			if (parseinput.has("TCode") && !parseinput.isNull("TCode")) {
				approvalObj.addProperty("TCode", parseinput.getString("TCode"));
				if (parseinput.getString("TCode").length() > 100) {
					run = true;
					response.getWriter().println("TCode length is too Large");
				}
			} else {
				approvalObj.addProperty("TCode", "");
			}

			if (parseinput.has("Program") && !parseinput.isNull("Program")) {
				approvalObj.addProperty("Program", parseinput.getString("Program"));
				if (parseinput.getString("Program").length() > 100) {
					run = true;
					response.getWriter().println("Program length is too Large");
				}
			} else {
				approvalObj.addProperty("Program", "");
			}

			if (parseinput.has("ProblemClass") && !parseinput.isNull("ProblemClass")) {
				approvalObj.addProperty("ProblemClass", parseinput.getString("ProblemClass"));
				if (parseinput.getString("ProblemClass").length() > 1) {
					run = true;
					response.getWriter().println("ProblemClass length is too Large");
				}
			} else {
				approvalObj.addProperty("ProblemClass", "");
			}

			if (parseinput.has("Process") && !parseinput.isNull("Process")) {
				approvalObj.addProperty("Process", parseinput.getString("Process"));
				if (parseinput.getString("Process").length() > 100) {
					run = true;
					response.getWriter().println("Process length is too Large");
				}
			} else {
				approvalObj.addProperty("Process", "");
			}

			if (parseinput.has("ProcessID") && !parseinput.isNull("ProcessID")) {
				approvalObj.addProperty("ProcessID", parseinput.getString("ProcessID"));
				if (parseinput.getString("ProcessID").length() > 100) {
					run = true;
					response.getWriter().println("ProcessID length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessID", "");
			}

			if (parseinput.has("ProcessRef1") && !parseinput.isNull("ProcessRef1")) {
				approvalObj.addProperty("ProcessRef1", parseinput.getString("ProcessRef1"));
				if (parseinput.getString("ProcessRef1").length() > 100) {
					run = true;
					response.getWriter().println("ProcessRef1 length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessRef1", "");
			}

			if (parseinput.has("ProcessRef2") && !parseinput.isNull("ProcessRef2")) {
				approvalObj.addProperty("ProcessRef2", parseinput.getString("ProcessRef2"));
				if (parseinput.getString("ProcessRef2").length() > 100) {
					run = true;
					response.getWriter().println("ProcessRef2 length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessRef2", "");
			}

			if (parseinput.has("ProcessRef3") && !parseinput.isNull("ProcessRef3")) {
				approvalObj.addProperty("ProcessRef3", parseinput.getString("ProcessRef3"));
				if (parseinput.getString("ProcessRef3").length() > 100) {
					run = true;
					response.getWriter().println("ProcessRef3 length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessRef3", "");
			}

			if (parseinput.has("ProcessRef4") && !parseinput.isNull("ProcessRef4")) {
				approvalObj.addProperty("ProcessRef4", parseinput.getString("ProcessRef4"));
				if (parseinput.getString("ProcessRef4").length() > 100) {
					run = true;
					response.getWriter().println("ProcessRef4 length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessRef4", "");
			}

			if (parseinput.has("ProcessRef5") && !parseinput.isNull("ProcessRef5")) {
				approvalObj.addProperty("ProcessRef5", parseinput.getString("ProcessRef5"));
				if (parseinput.getString("ProcessRef5").length() > 100) {
					run = true;
					response.getWriter().println("ProcessRef5 length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessRef5", "");
			}

			if (parseinput.has("ProcessParams") && !parseinput.isNull("ProcessParams")) {
				approvalObj.addProperty("ProcessParams", parseinput.getString("ProcessParams"));
				if (parseinput.getString("ProcessParams").length() > 1000) {
					run = true;
					response.getWriter().println("ProcessParams length is too Large");
				}
			} else {
				approvalObj.addProperty("ProcessParams", "");
			}

			if (parseinput.has("CorrelationID") && !parseinput.isNull("CorrelationID")) {
				approvalObj.addProperty("CorrelationID", parseinput.getString("CorrelationID"));
				if (parseinput.getString("CorrelationID").length() > 36) {
					run = true;
					response.getWriter().println("CorrelationID length is too Large");
				}
			} else {
				approvalObj.addProperty("CorrelationID", "");
			}

			if (parseinput.has("Source") && !parseinput.isNull("Source")) {
				approvalObj.addProperty("Source", parseinput.getString("Source"));
				if (parseinput.getString("Source").length() > 10) {
					run = true;
					response.getWriter().println("Source length is too Large");
				}
			} else {
				approvalObj.addProperty("Source", "");
			}

			if (parseinput.has("SourceReferenceID") && !parseinput.isNull("SourceReferenceID")) {
				approvalObj.addProperty("SourceReferenceID", parseinput.getString("SourceReferenceID"));
				if (parseinput.getString("SourceReferenceID").length() > 50) {
					run = true;
					response.getWriter().println("SourceReferenceID length is too Large");
				}
			} else {
				approvalObj.addProperty("SourceReferenceID", "");
			}

			boolean debug = Boolean.parseBoolean(request.getParameter("debug"));

			if (run == false) {
				apprvlResponse = executePostURL(response, approvalObj, request, debug);
			}

			if (debug) {
				response.getWriter().println("Insert into ApplicationLogs object" + apprvlResponse);
			}
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println(buffer.toString());
		}
		// apprvlResponse.addProperty("ErrorCode", "001");
		// apprvlResponse.addProperty("Message", "Insertion Failed");
		return apprvlResponse;
	}

	public String generateGUID(int fieldLength) {
		String guid = "";
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		if (fieldLength == 32) {
			guid = UUID.randomUUID().toString().replace("-", "");
		} else if (fieldLength == 36) {
			guid = UUID.randomUUID().toString();
		} else if (fieldLength == 16) {
			StringBuilder salt = new StringBuilder();
			Random rnd = new Random();
			while (salt.length() < fieldLength) {
				int index = (int) (rnd.nextFloat() * SALTCHARS.length());
				salt.append(SALTCHARS.charAt(index));
			}
			guid = salt.toString();
		} else if (fieldLength == 10) {
			long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
			guid = "" + number;
		} else {
			guid = "Unrecognized length request for a GUID";
		}
		return guid;
	}

	// public String getUserPrincipal(HttpServletRequest request, String
	// reqProperty, HttpServletResponse response)
	// {
	// String reqPropertyValue = "";
	// // com.sap.security.um.user.User user = null;
	// try
	// {
	// InitialContext ctx = new InitialContext();
	// UserProvider userProvider = (UserProvider)
	// ctx.lookup("java:comp/env/user/Provider");
	// if (request.getUserPrincipal() != null)
	// {
	// user = userProvider.getUser(request.getUserPrincipal().getName());

	// if(reqProperty.equalsIgnoreCase("name")){

	// reqPropertyValue = user.getAttribute("name");

	// } else if(reqProperty.equalsIgnoreCase("email")){

	// reqPropertyValue = user.getAttribute("email");

	// } else if(reqProperty.equalsIgnoreCase("DisplayName")){

	// reqPropertyValue = user.getAttribute("firstname")+"
	// "+user.getAttribute("lastname");

	// } else if(reqProperty.equalsIgnoreCase("user")){

	// reqPropertyValue = user.getName();

	// } else if(reqProperty.equalsIgnoreCase("All")){

	// reqPropertyValue =
	// "Name:"+user.getName()+user.getAttribute("Name")+"|Email:"+user.getAttribute("Email")+"|firstname:"+user.getAttribute("firstname")+"|lastname:"+user.getAttribute("lastname");

	// } else{

	// reqPropertyValue = "Unknown Property";

	// }

	// }

	// }

	// catch (Exception e) {

	// reqPropertyValue = e.getClass().getCanonicalName()+" - "+e.getMessage();

	// }

	// return reqPropertyValue;

	// }

	public String getCreatedAtTime() {

		String createdAt = "";

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			sdf.setTimeZone(TimeZone.getTimeZone("IST"));

			createdAt = sdf.format(new Date());

			createdAt = "PT" + createdAt.substring(11, 13) + "H" + createdAt.substring(14, 16) + "M"
					+ createdAt.substring(17, createdAt.length()) + "S";
		} catch (Exception e) {
			createdAt = "PT00H00M00S";
		}
		return createdAt;
	}

	public long getCreatedOnDate() {
		long createdOn = 0;
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			sdf1.setTimeZone(TimeZone.getTimeZone("IST"));
			Date createdAtDate = sdf2.parse(sdf1.format(new Date()));
			createdOn = createdAtDate.getTime();
		} catch (Exception e) {
			createdOn = 0;
		}
		return createdOn;
	}

	public JsonObject executePostURL(HttpServletResponse response, JsonObject approvalObj, HttpServletRequest request,
			boolean debug) throws IOException {
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		String userName = "", password = "", authParam = "";
		JsonObject jsonObj = new JsonObject();
		String data = "";
		HttpPost postRequest = null;
		try {
			// Context ctx = new InitialContext();
			// ConnectivityConfiguration configuration = (ConnectivityConfiguration)
			// ctx.lookup("java:comp/env/connectivityConfiguration");
			// DestinationConfiguration destConfiguration =
			// configuration.getConfiguration("PCGWHANA");
			// if (destConfiguration == null)
			// {
			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			// String.format("Destination %s is not found. Hint: Make sure to have the
			// destination configured.", "PYGWHANA"));
			// }

			// String proxyType = destConfiguration.getProperty("ProxyType");
			// String Url=destConfiguration.getProperty("URL");
			// String executeURL=Url+"ApplicationLogs";
			// userName = destConfiguration.getProperty("User");
			// password = destConfiguration.getProperty("Password");
			// authParam = userName + ":"+ password ;
			// String basicAuth = "Basic " +
			// Base64.getEncoder().encodeToString(authParam.getBytes());

			String executeURL = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PCGW/service.xsodata/";
			String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
			if (debug) {
				// response.getWriter().println("Reading destination properties");
				response.getWriter().println("executeHttpPost.executeURL: " + executeURL);
				// response.getWriter().println("executeHttpPost.proxyType: "+ proxyType);
				// response.getWriter().println("executeHttpPost.userName: "+ userName);
				// response.getWriter().println("executeHttpPost.password: "+ password);
				// response.getWriter().println("executeHttpPost.authParam: "+ authParam);
				// response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
			}

			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("P000000", "password");
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			if (debug) {
				response.getWriter().println("Created Values from insertIntoApproval()--->" + approvalObj);
			}
			// response.getWriter().println("Checking of responseEntity");
			requestEntity = new StringEntity(approvalObj.toString());

			// response.getWriter().println(requestEntity);

			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			// postRequest = new HttpPost(executeURL);
			HttpDelete delete = new HttpDelete(executeURL);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			postRequest.setEntity(requestEntity);
			HttpResponse httpPostResponse = httpClient.execute(postRequest);
			responseEntity = httpPostResponse.getEntity();
			// response.getWriter().println("Checking of responseentity");
			if (debug) {
				response.getWriter().println("Response :" + responseEntity.toString());
			}
			if (httpPostResponse.getEntity().getContentType() != null
					&& httpPostResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpPostResponse.getEntity().getContentType().toString()
						.replaceAll("content-type:", "").trim();
				if (contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					response.getWriter().println("Response :" + data);
					if (debug)
						response.getWriter().println(EntityUtils.toString(responseEntity));
				} else {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					response.getWriter().println(data);
				}
			} else {
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				if (debug)
					response.getWriter().println(EntityUtils.toString(responseEntity));
			}
			int statusCode = httpPostResponse.getStatusLine().getStatusCode();
			if ((statusCode / 100) == 2) {
				JsonParser parser = new JsonParser();
				jsonObj = (JsonObject) parser.parse(data);
				// jsonObj.addProperty("Status", "000001");
				// jsonObj.addProperty("ErrorCode", "");
				// jsonObj.addProperty("Message", "Record Inserted Successfully");
				// response.getWriter().println("Record Inserted Successfully");
			} else {
				// jsonObj.addProperty("Status", "000002");
				// jsonObj.addProperty("ErrorCode", statusCode);
				// jsonObj.addProperty("Message", data);
			}
			return jsonObj;
		} catch (Exception e) {
			postRequest.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().println("executeUpdate-Exception Stack Trace: " + buffer.toString());

			if (e.getLocalizedMessage() != null) {
				jsonObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
			}
			// jsonObj.addProperty("ErrorCode", "J002");
			//
			// jsonObj.addProperty("Status", "000002");
			//
			// jsonObj.addProperty("Message", buffer.toString());

		} finally {
			httpClient.close();
		}
		return jsonObj;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		// response.getWriter().println("Demo");

		boolean debug = false, isAggregatorIDmatched = false;
		String url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SCCNFG?filter=CP_TYPE%20eq%20%2701%27";
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		JsonObject sccnfgResponse = executeURL(url, userpass, response, debug);
		// response.getWriter().println("sccnfgResponse :" + sccnfgResponse);

		url = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/BPHeaders";
		JsonObject aggregatorsResponse = executeURL(url, userpass, response, debug);
		// response.getWriter().println("aggregatorsResponse : " + aggregatorsResponse);

		Map<String, String> inputForScfCorpDealerMap = new HashMap<>();
		

		String AggregatorName = "";
		if (sccnfgResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			if (aggregatorsResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

				JsonArray sccnfgResponseArray = sccnfgResponse.get("d").getAsJsonObject().get("results")
						.getAsJsonArray();
				JsonArray aggregatorsResponseArray = aggregatorsResponse.get("d").getAsJsonObject().get("results")
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

					}
				}
			}

		}

		// response.getWriter().println("inputForScfCorpDealerMap " + inputForScfCorpDealerMap);

		// Create a new workbook
		Workbook workbook=new XSSFWorkbook();
		// Create a blank sheet
		Sheet sheet = workbook.createSheet("Sample Excel Sheet");

		Row headerRow = sheet.createRow(0);

		Cell headerCell=headerRow.createCell(0);
		headerCell.setCellValue("AggregatorID");
		int i=1;
		for(String key :inputForScfCorpDealerMap.keySet()){
		String value=inputForScfCorpDealerMap.get(key);
		// response.getWriter().println("Key :"+key+" , "+"Value :"+value);

		Row rowValue=sheet.createRow(i);
		Cell rowCell=rowValue.createCell(0);
		rowCell.setCellValue(key);
		i++;
		}

		// int callCount = countPipeSymbols(value) + 1;

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment;filename=sample.xlsx");
		// response.getWriter().println("inputForScfCorpDealerMap : " +
		// inputForScfCorpDealerMap);
		String prntLmtid = "";
		String prntLmtPrx = "";
		for (String key : inputForScfCorpDealerMap.keySet()) {
			String prntLimitsvalue = inputForScfCorpDealerMap.get(key);
			int countofPipes = countPipeSymbols(prntLimitsvalue);
			if (countofPipes == 1) {
				prntLmtid = extractPrntLimits(prntLimitsvalue, countofPipes);
			} else if (countofPipes == 2) {
				prntLmtid = extractPrntLimits(prntLimitsvalue, 1);
				prntLmtPrx = extractPrntLimits(prntLimitsvalue, countofPipes);
			}
		}

		String pygwUrl = "";
		CommonUtils commonUtils = new CommonUtils();

		// delete
		// String executeURL=pygwUrl+"SupplyChainFinances";
		// JsonObject scfResponse = executeURL(executeURL, userpass, response,debug);

		// String executeURL =
		// "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances";
		// JsonObject scfResponse = executeURL(executeURL, userpass, response, debug);

		// response.getWriter().println("scfResponse "+scfResponse);

		// CloseableHttpClient httpClient = null;
		// executeURL=
		// "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/SupplyChainFinances%28%271bd84a6c-88a3-482c-a25d-d068fd370ae5%27%29";

		// String basicAuth = "Basic " +
		// Base64.getEncoder().encodeToString(userpass.getBytes());

		// CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		// UsernamePasswordCredentials credentials = new
		// UsernamePasswordCredentials("P000000", "DevCFHNDBP@$$wdFeb2024");
		// credentialsProvider.setCredentials(AuthScope.ANY, credentials);
		// httpClient =
		// HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

		// HttpDelete deleteRequest = new HttpDelete(executeURL);
		// deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
		// deleteRequest.setHeader("Content-Type", "application/json");
		// deleteRequest.setHeader("Accept", "application/json");
		// deleteRequest.setHeader("X-HTTP-Method", "DELETE");

		// HttpResponse httpResponse = httpClient.execute(deleteRequest);

		// if(httpResponse.getStatusLine().getStatusCode()==204)
		// {
		// response.getWriter().println("deleted");

		// }
		// else if(httpResponse.getStatusLine().getStatusCode()==404)
		// {
		// response.getWriter().println("Resources not found for this:");
		// }
		// else
		// {

		// if (debug)
		// {
		// response.getWriter().println("executeUpdate.getStatusCode: " +
		// httpResponse.getStatusLine().getStatusCode());
		// }
		// }

		// supllychain finance
		// JsonArray scfResponseArray =
		// scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();

		// JsonObject scfEntry = new JsonObject();
		// debug=false;
		// if
		// (scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size()
		// > 0) {
		// response.getWriter().println("scfResponseArray :"+scfResponseArray);
		// for (int j = 0; j < scfResponseArray.size(); j++) {
		// scfEntry = scfResponseArray.get(j).getAsJsonObject();

		// response.getWriter().println("scfEntry" +" "+j+" "+scfEntry);

		// if ( scfEntry.get("AccountNo").getAsString().equalsIgnoreCase("")
		// || scfEntry.get("AccountNo").isJsonNull() ||
		// scfEntry.get("AccountNo").getAsString().contains("-")
		// || scfEntry.get("CPGUID").getAsString().contains("-") ||
		// scfEntry.get("CPTypeID").getAsString().contains("-")
		// || scfEntry.get("AggregatorID").getAsString().contains("-")) {

		// String executeUrl =
		// pygwUrl+"SupplyChainFinances('"+scfEntry.get("ID").getAsString()+"')";
		// commonUtils.executeDelete(executeURL, userpass, response, req, debug,
		// executeUrl);
		// }
		// }
		// }

		// bpheaders
		// String executeURL = pygwUrl+"BPHeaders?filter=CPType%20eq%20%2760%27";
		// JsonObject bpResponse = executeURL(executeURL, userpass, response, debug);

		// if
		// (bpResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size()
		// > 0) {
		// JsonArray bpResponseArray =
		// bpResponse.get("d").getAsJsonObject().get("results")
		// .getAsJsonArray();
		// for (int j = 0; j < bpResponseArray.size(); j++) {
		// JsonObject bpObj = bpResponseArray.get(j).getAsJsonObject();

		// if (bpObj.get("FacilityType").getAsString().equalsIgnoreCase("")
		// || bpObj.get("FacilityType").isJsonNull() ||
		// bpObj.get("CPGUID").getAsString().contains("-")
		// || bpObj.get("CPTypeID").getAsString().contains("-")
		// || bpObj.get("AggregatorID").getAsString().contains("-")) {

		// String executeUrl =
		// pygwUrl+"SupplyChainFinances('"+bpObj.get("BPGuid").getAsString()+"')";
		// commonUtils.executeDelete(executeURL, userpass, response, req,
		// debug,executeUrl);
		// }
		// }
		// }

		// Write the workbook content to the response stream
		workbook.write(response.getOutputStream());
		workbook.close();

	}

	public static String extractPrntLimits(String input, int pipeCount) {
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
			} else {
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

	// public JsonObject executeURL(String executeURL, String userPass,
	// HttpServletResponse response) throws IOException {
	// DataOutputStream dataOut = null;
	// BufferedReader in = null;
	// JsonObject jsonObj = null;
	// try {
	// URL urlObj = new URL(executeURL);
	// HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
	// connection.setRequestMethod("GET");
	// connection.setRequestProperty("Content-Type", "application/xml");
	// connection.setRequestProperty("Accept", "application/xml");
	// connection.setRequestProperty("Authorization",
	// "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
	// connection.setDoInput(true);

	// in = new BufferedReader(new InputStreamReader(connection.getInputStream(),
	// StandardCharsets.UTF_8));
	// String inputLine;
	// StringBuffer responseStrBuffer = new StringBuffer();
	// while ((inputLine = in.readLine()) != null) {
	// responseStrBuffer.append(inputLine);
	// }

	// String xml = responseStrBuffer.toString();
	// org.json.JSONObject xmlJSONObj = XML.toJSONObject(xml);
	// String jsonString = xmlJSONObj.toString();

	// // Parse JSON string to JsonObject
	// JsonParser parser = new JsonParser();
	// jsonObj = parser.parse(jsonString).getAsJsonObject();

	// return jsonObj;
	// // JsonParser parser = new JsonParser();
	// // jsonObj = (JsonObject)parser.parse(responseStrBuffer.toString());
	// // response.getWriter().println(jsonObj.toString());
	// // change xml to json when need json data
	// // JsonParser parser = new JsonParser();
	// // jsonObj = (JsonObject)parser.parse(responseStrBuffer.toString());
	// } catch (Exception e) {
	// response.getWriter().println("executeURL.Exception: " +
	// e.getLocalizedMessage());
	// StackTraceElement element[] = e.getStackTrace();
	// StringBuffer buffer = new StringBuffer();
	// for (int i = 0; i < element.length; i++) {
	// buffer.append(element[i]);
	// }
	// response.getWriter().println("executeURL.Full Stack Trace: " +
	// buffer.toString());
	// } finally {
	// return jsonObj;
	// }
	// }

	@Bean
	public ServletRegistrationBean<Demo> myServletBean1() {
		ServletRegistrationBean<Demo> bean = new ServletRegistrationBean<>(new Demo(), "/Demo");
		return bean;
	}
}

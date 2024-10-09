package com.arteriatech.support;

import java.io.IOException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.synth.SynthSpinnerUI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicCommentHandler;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;


import javassist.bytecode.stackmap.BasicBlock.Catch;

public class UpdateIFSCRecords extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
	
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	private static final String HTTP_METHOD_DELETE = "DELETE";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	private String ODataUrl;
	private String username;
	private String password;
	private String appLogUrl;
	private String appLogUsername;
	private String appLogPassword;
	private String aggregatorID;
	private JSONObject jsonObj = new JSONObject();
	//private String inputPayload="";
	
	@Override
	public void init() throws ServletException {
		CommonUtils commonUtils=new CommonUtils();
		try{
			ODataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			username = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			appLogUrl=commonUtils.getODataDestinationProperties("URL", "PCGWHANA");
			appLogUsername = commonUtils.getODataDestinationProperties("User", "PCGWHANA");
			appLogPassword = commonUtils.getODataDestinationProperties("Password", "PCGWHANA");
			aggregatorID=commonUtils.getODataDestinationProperties("AggregatorID", "PCGWHANA");
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
		
	}


	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = commonUtils.getGetBody(request, response);
		final AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.addListener(new AsyncListner());
	    asyncContext.setTimeout(3000000);
		asyncContext.start(new Runnable() {
			@Override
			public void run() {	
				String logID = "";
				CommonUtils commonUtils = new CommonUtils();
				ODataLogs odataLogs = new ODataLogs();
				String executeURL ="";
				boolean debug = false;
				JsonObject res = new JsonObject();
				AtomicInteger stepNo = new AtomicInteger(0);
				String appLogUserpass = appLogUsername + ":" + appLogPassword;
				String userPass=username+ ":"+password;
			   
				try {
					//inputPayload = commonUtils.getGetBody(request, response);
					if (inputPayload != null && !inputPayload.equalsIgnoreCase("")) {
						JSONObject jsonObj = new JSONObject(inputPayload);
						if (jsonObj.has("debug") && jsonObj.getString("debug").equalsIgnoreCase("true")) {
							debug = true;
							jsonObj.remove("debug");
						}
						String logedInUser = commonUtils.getUserPrincipal(request, "name", response);
						stepNo.incrementAndGet();
						logID = odataLogs.insertApplicationLogs(request, response, "Java", "Inserting IFSC Records",
								"Process Strated", stepNo.intValue() + "", request.getServletPath(), appLogUrl,
								appLogUserpass, aggregatorID, logedInUser, debug);
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
								jsonObj.getJSONArray("results").length() + "", stepNo.intValue(),
								"Total Records from input payload", appLogUrl, appLogUserpass, aggregatorID, "", "", "",
								debug);

						if (jsonObj.has("results") && !jsonObj.isNull("results")
								&& jsonObj.getJSONArray("results").length() > 0) {

							final StringBuffer logBuffer = new StringBuffer(logID);
							List<JSONObject> totalJsonObj = IntStream.range(0, jsonObj.getJSONArray("results").length())
									.mapToObj(k -> {
										try {
											return jsonObj.getJSONArray("results").getJSONObject(k);
										} catch (JSONException e1) {
											StackTraceElement[] stackTrace = e1.getStackTrace();
											StringBuffer buf = new StringBuffer();
											for (int i = 0; i < stackTrace.length; i++) {
												buf.append(stackTrace[i]);
											}
											try {
												stepNo.incrementAndGet();
												odataLogs.insertMessageForAppLogs(request, response,
														logBuffer.toString(), "E", "/ARTEC/PY", buf.toString() + "",
														stepNo.intValue(),
														"Exception occurred while Parsing Json Array", appLogUrl,
														appLogUserpass, aggregatorID, "", "", "", false);
											} catch (Exception ex) {

											}
										}
										return jsonObj;
									}).collect(Collectors.toList());
							
							// filter duplicate IFSC Records in the input Payload
							
							
							List<JSONObject> jsonInputPayload = totalJsonObj.stream().filter(obj -> {
								try {
									return obj.has("IFSCCode") && !obj.isNull("IFSCCode")
											&& !obj.getString("IFSCCode").equalsIgnoreCase("");
								} catch (JSONException e3) {
									// TODO Auto-generated catch block
									e3.printStackTrace();
								}
								return false;
							}).filter(distinctByKey(Json -> {
								try {
									return Json.getString("IFSCCode");
								} catch (JSONException e2) {
									e2.printStackTrace();
								}
								return Json;
							})).collect(Collectors.toList());
							
							stepNo.incrementAndGet();
							odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(), "I",
									"/ARTEC/PY", jsonInputPayload.size() + "", stepNo.intValue(),
									"Total Valid Records in the Input Payload", appLogUrl, appLogUserpass,
									aggregatorID, "", "", "", false);
							
							List<JSONObject> updatedIFSCRecords = new ArrayList<>();
							synchronized (this) { // synchronization block
													// starated
								long start = System.currentTimeMillis();
								executeURL = ODataUrl + "IFSCCodes?$select=IFSCCode";
								// commonUtils.executeURL(executeURL, userPass,
								// response);
								JsonObject totalRecordFromDB = commonUtils.executeURL(executeURL, userPass, response);

								if (!totalRecordFromDB.has("error") && totalRecordFromDB.get("d").getAsJsonObject()
										.get("results").getAsJsonArray().size() > 0) {
									JsonArray jsonArray = totalRecordFromDB.get("d").getAsJsonObject().get("results")
											.getAsJsonArray();
									/*JSONArray dbArray = new JSONArray();
									for (int i = 0; i < jsonArray.size(); i++) {
										JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
										JSONObject json = new JSONObject(asJsonObject.toString());
										dbArray.put(json);
									}*/

									List<JSONObject> dbObjList = IntStream.range(0, jsonArray.size()).mapToObj(k -> {
										JSONObject json = null;
										try {
											JsonObject JsonObj = jsonArray.get(k).getAsJsonObject();
											json = new JSONObject(JsonObj.toString());
											return json;
										} catch (JSONException e1) {
											StackTraceElement[] stackTrace = e1.getStackTrace();
											StringBuffer buf = new StringBuffer();
											for (int i = 0; i < stackTrace.length; i++) {
												buf.append(stackTrace[i]);
											}
											try {
												stepNo.incrementAndGet();
												odataLogs.insertMessageForAppLogs(request, response,
														logBuffer.toString(), "E", "/ARTEC/PY", buf.toString() + "",
														stepNo.intValue(),
														"Exception occurred while Parsing Json Array", appLogUrl,
														appLogUserpass, aggregatorID, "", "", "", false);
											} catch (Exception ex) {

											}
										}
										return json;
									}).collect(Collectors.toList());

									
									/*updatedIFSCRecords = totalJsonObj.stream().filter(inputObj -> {
										Optional<JSONObject> findAny = dbObjList.stream().filter(obj -> {
											try {
												if (inputObj.has("IFSCCode") && !inputObj.isNull("IFSCCode")
														&& !inputObj.getString("IFSCCode").equalsIgnoreCase("")) {
													return obj.getString("IFSCCode")
															.equalsIgnoreCase(inputObj.getString("IFSCCode"));
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}
											return false;
										}).findAny();
										if (findAny.isPresent()) {
											return false;
										} else
											return true;
									}).collect(Collectors.toList());*/
									 

									updatedIFSCRecords = jsonInputPayload.stream().filter(inputObj -> {
										boolean result = dbObjList.stream().anyMatch(dbObj -> {
											try {
												return inputObj.has("IFSCCode") && !inputObj.isNull("IFSCCode")
														&& inputObj.getString("IFSCCode")
																.equalsIgnoreCase(dbObj.getString("IFSCCode"));
											} catch (JSONException e) {

												e.printStackTrace();
											}
											return false;
										});
										if (result) {
											return false;
										} else {
											return true;
										}
									}).collect(Collectors.toList());

								}

								long end = System.currentTimeMillis();
								stepNo.incrementAndGet();
								odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(), "I",
										"/ARTEC/PY", (end - start) + "ms", stepNo.intValue(),
										"Total Time Taken for checking Records Exist or Not", appLogUrl, appLogUserpass,
										aggregatorID, "", "", "", false);

								
								if (!updatedIFSCRecords.isEmpty() || updatedIFSCRecords.size() > 0) {
									int totalRecords = updatedIFSCRecords.size();
									int limit = 5000;
									int loppCount = totalRecords / limit;
									int rem = totalRecords % limit;
									int skipCount = 0;
									if (rem != 0) {
										loppCount++;
									}
									stepNo.incrementAndGet();
									odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(), "I",
											"/ARTEC/PY", updatedIFSCRecords.size() + "", stepNo.intValue(),
											"Total Valid Records for Updating to DB", appLogUrl, appLogUserpass,
											aggregatorID, "", "", "", false);
									
									for (int i = 0; i < loppCount; i++) {
										if (i == 0) {
											List<JSONObject> ifscList = updatedIFSCRecords.stream().skip(skipCount)
													.limit(limit).collect(Collectors.toList());

											processBatchRequest(request, response, ifscList, i, limit, skipCount,
													stepNo, appLogUserpass, logID, odataLogs);
											stepNo.incrementAndGet();
											odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(),
													"I", "/ARTEC/PY", "Batch No" + i, stepNo.intValue(),
													"Batch Completed", appLogUrl, appLogUserpass, aggregatorID, "", "",
													"", false);

										} else {
											skipCount = limit * i;
											List<JSONObject> ifscList = jsonInputPayload.stream().skip(skipCount)
													.limit(limit).collect(Collectors.toList());
											processBatchRequest(request, response, ifscList, i, limit, skipCount,
													stepNo, appLogUserpass, logID, odataLogs);
											stepNo.incrementAndGet();
											odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(),
													"I", "/ARTEC/PY", "Batch No" + i, stepNo.intValue(),
													"Batch Completed", appLogUrl, appLogUserpass, aggregatorID, "", "",
													"", false);
										}
									}

								} else {
									if (!totalRecordFromDB.has("error") && totalRecordFromDB.get("d").getAsJsonObject()
											.get("results").getAsJsonArray().size() == 0) {
										int totalRecords = jsonInputPayload.size();
										int limit = 5000;
										int lopCount = totalRecords / limit;
										int rem = totalRecords % limit;
										int skipCount = 0;
										if (rem != 0) {
											lopCount++;
										}
										stepNo.incrementAndGet();
										odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(), "I",
												"/ARTEC/PY", totalRecords + "", stepNo.intValue(),
												"Total Valid Records for Updating to DB", appLogUrl, appLogUserpass,
												aggregatorID, "", "", "", false);
										for (int i = 0; i < lopCount; i++) {
											if (i == 0) {
												List<JSONObject> ifscList = jsonInputPayload.stream().skip(skipCount)
														.limit(limit).collect(Collectors.toList());
												processBatchRequest(request, response, ifscList, i, limit, skipCount,
														stepNo, appLogUserpass, logID, odataLogs);

											} else {
												skipCount = limit * i;
												List<JSONObject> ifscList = jsonInputPayload.stream().skip(skipCount)
														.limit(limit).collect(Collectors.toList());
												processBatchRequest(request, response, ifscList, i, limit, skipCount,
														stepNo, appLogUserpass, logID, odataLogs);
											}
										}
									} else {
										stepNo.incrementAndGet();
										odataLogs.insertMessageForAppLogs(request, response, logBuffer.toString(), "I",
												"/ARTEC/PY",
												totalRecordFromDB.get("d").getAsJsonObject().get("results")
														.getAsJsonArray().size() + "",
												stepNo.intValue(), "Total Records Fetched from DB ", appLogUrl,
												appLogUserpass, aggregatorID, "", "", "", false);
									}

								}
							}  // synch  block
						/*	if (successResponse) {
								res.addProperty("Message", "Records Updated Successfully");
								res.addProperty("Status", "000001");
								res.addProperty("ErrorCode", "");
								stepNo.incrementAndGet();
								odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
										res.toString(), stepNo.intValue(), "All Records Updated Successfully",
										appLogUrl, appLogUserpass, aggregatorID, "", "", "", debug);
							}*/

							stepNo.incrementAndGet();
							odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
									"", stepNo.intValue(), "Process Completed", appLogUrl, appLogUserpass,
									aggregatorID, "", "", "", debug);
						}
					} else {
						res.addProperty("Message", "Empty input Payload Received");
						res.addProperty("Status", "000002");
						res.addProperty("ErrorCode", "J002");
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", res.toString(),
								stepNo.intValue(), "Empty input Payload Received", appLogUrl, appLogUserpass,
								aggregatorID, "", "", "", debug);
					}
				} catch (JSONException ex) {
					StackTraceElement[] stackTrace = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < stackTrace.length; i++) {
						buffer.append(stackTrace[i]);
					}
					try {
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
								stepNo.intValue(), "SocketTimeoutException Exception Occurred", appLogUrl,
								appLogUserpass, aggregatorID, ex.getLocalizedMessage(), ex.getClass().getName(), "",
								debug);
					} catch (Exception e) {

					}
				} catch (SocketTimeoutException ex) {
					StackTraceElement[] stackTrace = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < stackTrace.length; i++) {
						buffer.append(stackTrace[i]);
					}
					try {
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
								stepNo.intValue(), "SocketTimeoutException Exception Occurred", appLogUrl,
								appLogUserpass, aggregatorID, ex.getLocalizedMessage(), ex.getClass().getName(), "",
								debug);
					} catch (Exception e) {

					}
				} catch (Exception ex) {
					StackTraceElement[] stackTrace = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < stackTrace.length; i++) {
						buffer.append(stackTrace[i]);
					}
					try {
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
								stepNo.intValue(), "Exception Occurred", appLogUrl, appLogUserpass, aggregatorID,
								ex.getLocalizedMessage(), "", "", debug);
					} catch (Exception e) {

					}

				}

			}
		});
		asyncContext.complete();
		JsonObject resObj = new JsonObject();
		resObj.addProperty("Message", "Uploading Records Strated Please Check After Some Time");
		resObj.addProperty("Status", "000001");
		resObj.addProperty("ErrorCode", "");
		response.getWriter().println(resObj);
	}
	
	synchronized private  void processBatchRequest(HttpServletRequest request,HttpServletResponse response,List<JSONObject> jsonList,int batchNo,int limit,int skipCount,AtomicInteger stepNo,String appLogUserpass,String logID,ODataLogs odataLogs){
		boolean debug=false;
		boolean successResponse=true;
		CommonUtils commonUtils=new CommonUtils();
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			String userpass = username + ":" + password;
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER,
					"Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			// Changes on 20231120
			// String executeURL = ODataUrl + "IFSCCodes";
			String executeURL = "IFSCCodes";
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			/*for (int i = 0; i < jsonList.size(); i++) {
				JSONObject jsonObject = jsonList.get(i);
				if (jsonObject.has("IFSCCode") && !jsonObject.isNull("IFSCCode")
						&& !jsonObject.getString("IFSCCode").equalsIgnoreCase("")) {
					BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
							.headers(changeSetHeaders).body(jsonObject.toString()).build();
					changeSet.add(changeRequest);
				}
			}*/
			List<JSONObject> collect = jsonList.stream().filter(jsonObject -> {
				try {
					return jsonObject.has("IFSCCode") && !jsonObject.isNull("IFSCCode")
							&& !jsonObject.getString("IFSCCode").equalsIgnoreCase("");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return false;
			}).collect(Collectors.toList());
			
			collect.forEach(obj -> {
				String createdBy = commonUtils.getUserPrincipal(request, "name", response);
				String createdAt = commonUtils.getCreatedAtTime();
				long createdOnInMillis = commonUtils.getCreatedOnDate();
				try {
					obj.accumulate("CreatedBy", createdBy);
					obj.accumulate("CreatedAt", createdAt);
					obj.accumulate("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
						.headers(changeSetHeaders).body(obj.toString()).build();
				changeSet.add(changeRequest);
			});
			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body);
			// CloseableHttpClient build = HttpClientBuilder.create().build();
			final HttpPost post = new HttpPost(URI.create(ODataUrl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			// HttpResponse batchResponse = getHttpClient().execute(post);
			HttpResponse batchResponse = client.execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody);
			try {
				List<BatchSingleResponse> responses = EntityProvider
						.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes), contentType);
				for (BatchSingleResponse singleRes : responses) {
					String statusCode = singleRes.getStatusCode();
					if (!statusCode.equalsIgnoreCase("201")) {
						successResponse = false;
						stepNo.incrementAndGet();
						odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
								statusCode+":" + singleRes.getBody(), stepNo.intValue(), "Batch Execution failed", appLogUrl,
								appLogUserpass, aggregatorID, "Exception occurred while Parsing Batch Response","Batch No:"+batchNo, "Status Code:"+statusCode, debug);
					}
				}
			} catch (BatchException e) {
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int k = 0; k < stackTrace.length; k++) {
					buffer.append(stackTrace[k]);
				}
				successResponse = false;
				stepNo.incrementAndGet();
				odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo.intValue(),
						"Batch Parser Exception", appLogUrl, appLogUserpass,
						aggregatorID, e.getLocalizedMessage(), "Batch No:"+batchNo, "", debug);
			}
			if(successResponse){
				stepNo.incrementAndGet();
				odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", "All Records Updated Sucessfylly", stepNo.intValue(),
						"Batch Completed"+batchNo, appLogUrl, appLogUserpass,
						aggregatorID, "", "Batch No:"+batchNo, "", debug);
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int k = 0; k < stackTrace.length; k++) {
				buffer.append(stackTrace[k]);
			}
			successResponse = false;
			stepNo.incrementAndGet();
			try {
				odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo.intValue(),
						"Exception occurred while Processing the batch response", appLogUrl, appLogUserpass,
						aggregatorID, ex.getLocalizedMessage(), "Batch No:" + batchNo, "", debug);
			} catch (Exception e) {

			}

		}
	}
	
	/* private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	} */
	
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
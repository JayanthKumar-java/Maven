package com.arteriatech.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ODataBatchProcessing extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public static final String SEPARATOR = "/";
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	private static final String HTTP_METHOD_DELETE = "DELETE";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
	private final String boundary = "batch_" + UUID.randomUUID().toString();
	private static final int  BATCH_SIZE=5000;
	private String ODataUrl;
	private String username;
	private String password;
	
	private String appLogUrl;
	private String appLogUsername;
	private String appLogPassword;
	private String aggregatorID;

	@Override
	public void init() throws ServletException {
		CommonUtils commonUtils=new CommonUtils();
		try{
			ODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
			username = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
			appLogUrl=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			appLogUsername = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			appLogPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
			aggregatorID=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			JsonObject resObj=new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			throw new RuntimeException(resObj.toString());
		}
		
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.addListener(new AsyncListner());
		asyncContext.setTimeout(18000000);
		asyncContext.start(new Runnable() {
			@Override
			public void run() {
				try {
					ODataLogs odataLogs = new ODataLogs();
					String executeURL = "";
					String userpass = "";
					CommonUtils commonUtils = new CommonUtils();
					int totalRecords = 0;
					boolean debug = false;
					int stepNo = 0;
					String appLogUserpass = appLogUsername + ":" + appLogPassword;
					String logedInUser = commonUtils.getUserPrincipal(request, "name", response);
					stepNo++;
					String logID = odataLogs.insertApplicationLogs(request, response, "Java", "Deleting IFSC Records",
							"Process Strated", stepNo + "", request.getServletPath(), appLogUrl, appLogUserpass,
							aggregatorID, logedInUser, debug);
					userpass = username + ":" + password;
					executeURL = ODataUrl + "IFSCCodes?$select=IFSCCode&$top=" + 2 + "&$inlinecount=allpages";
					JsonObject getTotalRecordsCount = commonUtils.executeURL(executeURL, userpass, response);
					stepNo++;
					odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
							getTotalRecordsCount + "", stepNo, "Total Records in IFSCCodes Table", appLogUrl,
							appLogUserpass, aggregatorID, "", "", "", debug);
					if (debug) {
						response.getWriter().println("Total Records in IFSCCodes Table:" + getTotalRecordsCount);
					}
					if (getTotalRecordsCount != null && !getTotalRecordsCount.isJsonNull()
							&& getTotalRecordsCount.get("d").getAsJsonObject().has("__count")
							&& getTotalRecordsCount.get("d").getAsJsonObject().get("__count").getAsInt() > 0) {
						totalRecords = getTotalRecordsCount.get("d").getAsJsonObject().get("__count").getAsInt();
						if (debug) {
							response.getWriter().println("Total Records in IFSCCode Table: " + totalRecords);
						}
						if (totalRecords > BATCH_SIZE) {
							// do batch call
							int loopCount = totalRecords / BATCH_SIZE;
							int rem = totalRecords % BATCH_SIZE;
							if (rem != 0) {
								loopCount++;
							}
							for (int i = 0; i < loopCount; i++) {
								JsonObject ifcObj = getIFCSRecords(request, response, BATCH_SIZE, debug, logID,
										odataLogs);
								if (ifcObj != null && !ifcObj.isJsonNull() && ifcObj.get("d").getAsJsonObject()
										.get("results").getAsJsonArray().size() > 0) {
									JsonArray ifscrecords = ifcObj.get("d").getAsJsonObject().get("results")
											.getAsJsonArray();
									deleteIFCRecords(request, response, ifscrecords, debug, logID, odataLogs);
								} else {
									stepNo++;
									odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
											"Batch No: " + (i + 1), stepNo, "Records Not Fetched from IFSCCodes Table",
											appLogUrl, appLogUserpass, aggregatorID, "", "", "", debug);
								}
							}
						} else {
							JsonObject ifcObj = getIFCSRecords(request, response, BATCH_SIZE, debug, logID, odataLogs);
							JsonArray ifscrecords = ifcObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
							deleteIFCRecords(request, response, ifscrecords, debug, logID, odataLogs);
						}
					} 
					stepNo++;
					odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
							"", stepNo, "Process Completed",
							appLogUrl, appLogUserpass, aggregatorID, "", "", "", debug);
				} catch (Exception ex) {
					JsonObject res = new JsonObject();
					StackTraceElement[] stackTrace = ex.getStackTrace();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < stackTrace.length; i++) {
						buffer.append(stackTrace[i]);
					}
					res.addProperty("Message", buffer.toString());
					res.addProperty("Status", "000002");
					res.addProperty("ErrorCode", "J002");
				}
			}

		});
		asyncContext.complete();
		JsonObject resObj = new JsonObject();
		resObj.addProperty("Message", "Deleting Records Strated");
		resObj.addProperty("Status", "000002");
		resObj.addProperty("ErrorCode", "J002");
		response.getWriter().println(resObj);
	}			

	public HttpStatusCodes deleteEntry(String serviceUri, String entityName, String id) throws IOException {
		String absolutUri = createUri(serviceUri, entityName, id);
		HttpURLConnection connection = connect(absolutUri, APPLICATION_JSON, HTTP_METHOD_DELETE);
		return HttpStatusCodes.fromStatusCode(connection.getResponseCode());
	}

	private String createUri(String serviceUri, String entitySetName, String id) {
		return createUri(serviceUri, entitySetName, id, null);
	}

	private String createUri(String serviceUri, String entitySetName, String id, String expand) {
		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(entitySetName);
		if (id != null) {
			absolutUri.append("(").append(id).append(")");
		}
		if (expand != null) {
			absolutUri.append("/?$expand=").append(expand);
		}
		return absolutUri.toString();
	}

	private HttpURLConnection connect(String relativeUri, String contentType, String httpMethod) throws IOException {
		HttpURLConnection connection = initializeConnection(relativeUri, contentType, httpMethod);
		connection.connect();
		checkStatus(connection);

		return connection;
	}

	private HttpURLConnection initializeConnection(String absolutUri, String contentType, String httpMethod)
			throws MalformedURLException, IOException {
		String username = "P000000";
		String password = "2022Pa$$w0Rddev";
		String userpass = username + ":" + password;
		URL url = new URL(absolutUri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(httpMethod);
		connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
		connection.setRequestProperty("Authorization",
				"Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
		connection.setDoOutput(true);
		connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, contentType);
		return connection;
	}

	private HttpStatusCodes checkStatus(HttpURLConnection connection) throws IOException {
		HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
		if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
			throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " "
					+ httpStatusCode.toString());
		}
		return httpStatusCode;
	}

	
	private HttpResponse executeBatchCall(HttpServletRequest request,HttpServletResponse servletResponse,String ODataurl, final String body,String logID,ODataLogs oDataLogs,boolean debug)
			throws ClientProtocolException, IOException {
		HttpResponse response=null;
		try {
			String userpass = username + ":" + password;
			final HttpPost post = new HttpPost(URI.create(ODataurl + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			HttpEntity entity = new StringEntity(body);
			post.setEntity(entity);
			response = getHttpClient().execute(post);
			return response;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			int stepNo = 0;
			String userpass = appLogUsername + ":" + appLogPassword;
			oDataLogs.insertMessageForAppLogs(request, servletResponse, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
					"Exception Occurred in executeBatchCall Method ", appLogUrl, userpass, aggregatorID, "", "", "",
					debug);
		}
		return response;
	}
	
	private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	}
	
	private void deleteIFCRecords(HttpServletRequest request, HttpServletResponse response, JsonArray ifcrecords,
			boolean debug, String logID, ODataLogs oDataLogs) throws IOException {
		String executeURL = "";
		String userpass = username + ":" + password;
		try {
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER,
					"Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
			for (int i = 0; i < ifcrecords.size(); i++) {
				String ifscCode = ifcrecords.get(i).getAsJsonObject().get("IFSCCode").getAsString();
				// Changes on 20231120
				// executeURL = ODataUrl + "IFSCCodes('" + ifscCode + "')";
				executeURL = "IFSCCodes('" + ifscCode + "')";
				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("DELETE").uri(executeURL)
						.headers(changeSetHeaders).build();
				changeSet.add(changeRequest);
			}

			batchParts.add(changeSet);
			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body);
			
			userpass = appLogUsername + ":" + appLogPassword;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", payload, 0,
					"Batch Request Payload ", appLogUrl, userpass, aggregatorID, "", "", "",
					debug);
			if (debug) {
				response.getWriter().println("$batch request : ");
				response.getWriter().println(payload);
			}
			HttpResponse batchResponse = executeBatchCall(request,response,ODataUrl, payload,logID, oDataLogs,debug);
			if (debug) {
				int statusCode = batchResponse.getStatusLine().getStatusCode();
				response.getWriter().println("statusCode: " + statusCode);
			}
			if (debug) {
				for (Header h : batchResponse.getAllHeaders()) {
					response.getWriter().println(h.getName() + ":" + h.getValue());
				}
			}

			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody);
			userpass = appLogUsername + ":" + appLogPassword;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY", updatedRes, 0,
					"Batch Response Payload ", appLogUrl, userpass, aggregatorID, "", "", "",
					debug);
			try {
				List<BatchSingleResponse> responses = EntityProvider
						.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes), contentType);
			} catch (BatchException e) {
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				int stepNo = 0;
				userpass = appLogUsername + ":" + appLogPassword;
				oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(), stepNo,
						"Exception Occurred in deleteIFCRecords Method ", appLogUrl, userpass, aggregatorID, "", "", "",
						debug);
			}
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			int stepNo=0;
			userpass=appLogUsername + ":" + appLogPassword;
			oDataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
					stepNo, "Exception Occurred in deleteIFCRecords Method ", appLogUrl, userpass, aggregatorID, "", "", "", debug);
		}

	}
	
	/*private String getODataServiceUrl() {
		return "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata";
	}
	*/
	private JsonObject getIFCSRecords(HttpServletRequest request,HttpServletResponse response,int batch_size,boolean debug,String logID,ODataLogs odataLogs)throws IOException{
		String userpass="";
		String executeURL="";
		CommonUtils commonUtils=new CommonUtils();
		JsonObject jsonRes=null;
		try{
			
			executeURL=ODataUrl+"IFSCCodes?$select=IFSCCode&$top="+batch_size;
			userpass = username + ":" + password;
			jsonRes = commonUtils.executeURL(executeURL, userpass, response);
			return jsonRes;
		} catch (Exception ex) {
			JsonObject res = new JsonObject();
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			res.addProperty("Exception", buffer.toString());
			res.addProperty("Message", "Exception occurred while getting Records from IFSC Table");
			res.addProperty("Status", "000002");
			res.addProperty("ErrorCode", "J002");
			int stepNo=0;
			userpass=appLogUsername + ":" + appLogPassword;
			odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
					stepNo, "Exception Occurred in getIFCSRecords Method ", appLogUrl, userpass, aggregatorID, "", "", "", debug);
			
		}
		return jsonRes;
	}

}

package com.arteriatech.support;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.json.JSONException;
import org.json.JSONObject;

import com.arteriatech.logs.ODataLogs;
import com.arteriatech.pg.CommonUtils;
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


public class ProcessBatchRequest implements Runnable {

	private List<JSONObject> ifscList = null;
	private int batchNo;
	private int limit;
	private int skipCount;
	private AtomicInteger stepNo;
	private String appLogUserpass;
	private String logID;
	private ODataLogs odataLogs;
	private String ODataUrl;
	

	private String userpass;
	private String aggregatorID;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String appLogUrl;
	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	public ProcessBatchRequest(List<JSONObject> ifscList, int batchNo, int limit, int skipCount, AtomicInteger stepNo,
			String appLogUserpass, String logID, ODataLogs odataLogs, String oDataUrl, String userpass,
			String aggregatorID, HttpServletRequest request, HttpServletResponse response, String appLogUrl) {
		this.ifscList = ifscList;
		this.batchNo = batchNo;
		this.limit = limit;
		this.skipCount = skipCount;
		this.stepNo = stepNo;
		this.appLogUserpass = appLogUserpass;
		this.logID = logID;
		this.odataLogs = odataLogs;
		this.ODataUrl = oDataUrl;
		this.userpass = userpass;
		this.aggregatorID = aggregatorID;
		this.request = request;
		this.response = response;
		this.appLogUrl = appLogUrl;
	}

	@Override
	public void run() {
		boolean debug = false;
		boolean successResponse = true;
		CommonUtils commonUtils = new CommonUtils();
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			final String boundary = "batch_" + UUID.randomUUID().toString();
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
			List<JSONObject> collect = ifscList.stream().filter(jsonObject -> {
				try {
					return jsonObject.has("IFSCCode") && !jsonObject.isNull("IFSCCode")
							&& !jsonObject.getString("IFSCCode").equalsIgnoreCase("");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				return false;
			}).collect(Collectors.toList());

			collect.forEach(obj -> {
				String createdAt = commonUtils.getCreatedAtTime();
				long createdOnInMillis = commonUtils.getCreatedOnDate();
				try {
					obj.accumulate("CreatedAt", createdAt);
					obj.accumulate("CreatedOn", "/Date(" + createdOnInMillis + ")/");
				} catch (JSONException e) {
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
								statusCode + ":" + singleRes.getBody(), stepNo.intValue(), "Batch Execution failed",
								appLogUrl, appLogUserpass, aggregatorID,
								"Exception occurred while Parsing Batch Response", "Batch No:" + batchNo,
								"Status Code:" + statusCode, debug);
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
				odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
						stepNo.intValue(), "Batch Parser Exception", appLogUrl, appLogUserpass, aggregatorID,
						e.getLocalizedMessage(), "Batch No:" + batchNo, "", debug);
			}
			if (successResponse) {
				stepNo.incrementAndGet();
				odataLogs.insertMessageForAppLogs(request, response, logID, "I", "/ARTEC/PY",
						"All Records Updated Successfully", stepNo.intValue(), "Batch Completed" + batchNo, appLogUrl,
						appLogUserpass, aggregatorID, "", "Batch No:" + batchNo, "", debug);
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
				odataLogs.insertMessageForAppLogs(request, response, logID, "E", "/ARTEC/PY", buffer.toString(),
						stepNo.intValue(), "Exception occurred while Processing the batch response", appLogUrl,
						appLogUserpass, aggregatorID, ex.getLocalizedMessage(), "Batch No:" + batchNo, "", debug);
			} catch (Exception e) {

			}
		}
	}

	/* private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	} */
}
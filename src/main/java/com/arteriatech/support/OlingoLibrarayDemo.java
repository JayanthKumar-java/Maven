package com.arteriatech.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.ep.EntityProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OlingoLibrarayDemo extends HttpServlet {

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
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final boolean PRINT_RAW_CONTENT = true;
	public static final String METADATA = "$metadata";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String oDataURL = "", userName = "", password = "", userPass = "", aggregatorID = "", executeURL = "",
				pgid = "";
		JsonObject resObj = new JsonObject();
		boolean debug = false;
		final String boundary = "batch_" + UUID.randomUUID().toString();
		JsonParser parser = new JsonParser();
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			/*oDataURL = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
			userName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
			password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);*/
			//aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
			oDataURL="https://qasci9yqyi812.hana.ondemand.com/ARTEC/PCGW/service.xsodata/";
			userPass =  "P000000" + ":"+"2022Pa$$w0RdqaH@n@NovDb";
			if (request.getParameter("PGID") != null && !request.getParameter("PGID").equalsIgnoreCase("")) {
				pgid = request.getParameter("PGID");
			}
			if (debug) {
				response.getWriter().println("PGID:" + pgid);
			}

			executeURL = oDataURL + "ARTEC_PC_INV_H";
			if (debug) {
				response.getWriter().println("executeURL:" + executeURL);
			}

			Map<String, String> changeSetHeaders = new HashMap<String, String>();
			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
			changeSetHeaders.put("Accept", APPLICATION_JSON);
			changeSetHeaders.put(AUTHORIZATION_HEADER,
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			List<BatchPart> batchParts = new ArrayList<BatchPart>();
			BatchQueryPart batchRequest = BatchQueryPart.method("GET").headers(changeSetHeaders).uri(executeURL)
					.build();
			batchParts.add(batchRequest);

			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
			String payload = org.apache.commons.io.IOUtils.toString(body);

			final HttpPost post = new HttpPost(URI.create(oDataURL + "$batch"));
			post.setHeader("Content-Type", "multipart/mixed" + ";boundary=" + boundary);
			post.setHeader("Accept", APPLICATION_XML);
			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);
			HttpResponse batchResponse = getHttpClient().execute(post);
			InputStream responseBody = batchResponse.getEntity().getContent();
			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			try {
				List<BatchSingleResponse> parseBatchResponse = EntityProvider.parseBatchResponse(responseBody,
						contentType);
				for (BatchSingleResponse singleRes : parseBatchResponse) {
					String statusCode = singleRes.getStatusCode();
					if (debug) {
						response.getWriter().println("Status Code:" + statusCode);
						response.getWriter().println("Response Body:" + singleRes.getBody());
					}
					if (statusCode.equalsIgnoreCase("200")) {
						String data = singleRes.getBody();
						JsonObject openitemObj = (JsonObject) parser.parse(data);
						if (openitemObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
							resObj.add("Message", openitemObj);
							resObj.addProperty("Status", "000001");
							resObj.addProperty("ErrorCode", "");
						} else {
							resObj.addProperty("Message", "Records Not Exist");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
						}
					} else {
						String errorMessage = singleRes.getBody();
						if (debug) {
							response.getWriter().println("Error Message: " + errorMessage);
						}
						resObj.addProperty("Message", "Unable to Fetch the Records");
						resObj.addProperty("Status", "000002");
						resObj.addProperty("ErrorCode", "J002");
					}

				}
			} catch (Exception e) {
				StackTraceElement[] stackTrace = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < stackTrace.length; i++) {
					buffer.append(stackTrace[i]);
				}
				resObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
				resObj.addProperty("Message", buffer.toString());
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
			}
			response.getWriter().println(resObj);
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Status", "000002");
			resObj.addProperty("Message", buffer.toString());
			response.getWriter().println(resObj);
		}

	}

	private HttpClient getHttpClient() {
		HttpClient client = new DefaultHttpClient();
		return client;
	}

}

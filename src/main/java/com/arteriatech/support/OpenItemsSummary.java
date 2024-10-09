package com.arteriatech.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.exception.ODataException;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.HttpClient;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
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

public class OpenItemsSummary extends HttpServlet{

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		String customerNo="",invoiceNo="",ODataUrl="",username="",password="",aggregatorID="",userpass="",executeURL="";
		JsonObject resObj=new JsonObject();
		JsonParser parser=new JsonParser();
		boolean debug=false;
		final String boundary = "batch_" + UUID.randomUUID().toString();
		try{
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			if(request.getParameter("CPGUID")!=null&&!request.getParameter("CPGUID").equalsIgnoreCase("")){
				customerNo=request.getParameter("CPGUID");
				
				if(request.getParameter("debug")!=null&&request.getParameter("debug").equalsIgnoreCase("true")){
					debug=true;
				}
				if(request.getParameter("InvoiceNo")!=null&&!request.getParameter("InvoiceNo").equalsIgnoreCase("")){
					invoiceNo=request.getParameter("InvoiceNo");
				}
				
				if(debug){
					response.getWriter().println("CPGUID:"+customerNo);
					response.getWriter().println("InvoiceNo:"+invoiceNo);
				}
				
				ODataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				username = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password= commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				aggregatorID=commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
				userpass = username + ":" + password;
				executeURL=ODataUrl+"OpenItems?$filter=AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPGuid%20eq%20%27"+customerNo+"%27";
				if(!invoiceNo.equalsIgnoreCase("")){
					executeURL=executeURL+"%20and%20InvoiceNo%20eq%20%27"+invoiceNo+"%27";
				}
				
				if(debug){
					response.getWriter().println("username:"+username);
					response.getWriter().println("password:"+password);
					response.getWriter().println("executeURL:"+executeURL);
				}
				
				Map<String, String> changeSetHeaders = new HashMap<String, String>();
				changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
				changeSetHeaders.put("Accept", APPLICATION_JSON);
				changeSetHeaders.put(AUTHORIZATION_HEADER,
						"Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
				List<BatchPart> batchParts = new ArrayList<BatchPart>();
				BatchQueryPart batchRequest = BatchQueryPart.method("GET").headers(changeSetHeaders).uri(executeURL).build();
				batchParts.add(batchRequest);
				
				InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
				String payload = org.apache.commons.io.IOUtils.toString(body);
				if(debug){
					response.getWriter().println("request Payload  body:"+payload);
				}
				
				final HttpPost post = new HttpPost(URI.create(ODataUrl + "$batch"));
				post.setHeader("Content-Type", "multipart/mixed"+";boundary=" + boundary);
				post.setHeader("Accept", APPLICATION_XML);
				post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()));
				HttpEntity entity = new StringEntity(payload);
				post.setEntity(entity);
				// HttpResponse batchResponse = getHttpClient().execute(post);
				HttpResponse batchResponse = client.execute(post);
				InputStream responseBody = batchResponse.getEntity().getContent();
				String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
				try {
					List<BatchSingleResponse> parseBatchResponse = EntityProvider.parseBatchResponse(responseBody, contentType);
					for (BatchSingleResponse singleRes : parseBatchResponse) {
						String statusCode = singleRes.getStatusCode();
						if(debug){
							response.getWriter().println("Status Code:"+statusCode);
							response.getWriter().println("Response Body:"+singleRes.getBody());
						}
						if (statusCode.equalsIgnoreCase("200")) {
							String data = singleRes.getBody();
							JsonObject openitemObj = (JsonObject)parser.parse(data);
							if (openitemObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
								resObj.add("Message", openitemObj);
								resObj.addProperty("Status", "000001");
								resObj.addProperty("ErrorCode", "");
							} else {
								resObj.addProperty("Message", "Records Not Exist");
								resObj.addProperty("Status", "000002");
								resObj.addProperty("ErrorCode", "J002");
							}
						}else{
							String errorMessage = singleRes.getBody();
							if(debug){
								response.getWriter().println("Error Message: "+errorMessage);
							}
							resObj.addProperty("Message", "Unable to Fetch the Records");
							resObj.addProperty("Status", "000002");
							resObj.addProperty("ErrorCode", "J002");
						}
					}
				} catch (Exception e) {
					StackTraceElement[] stackTrace = e.getStackTrace();
					StringBuffer buffer=new StringBuffer();
					for(int i=0;i<stackTrace.length;i++){
						buffer.append(stackTrace[i]);
					}
					resObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
					resObj.addProperty("Message", buffer.toString());
					resObj.addProperty("Status", "000002");
					resObj.addProperty("ErrorCode", "J002");
				}
				response.getWriter().println(resObj);
			}else{
				resObj.addProperty("Message", "CPGUID Field is Missing in the input payload");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);
			}
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			response.getWriter().println(ex.getLocalizedMessage());
			response.getWriter().println(buffer.toString());
			
		}
	}
	
	public Edm readEdm(String serviceUrl,String userpass) throws IOException, ODataException {
		  InputStream content = execute(serviceUrl+ METADATA, APPLICATION_XML, HTTP_METHOD_GET,userpass);
		  return EntityProvider.readMetadata(content, false);
		}
	
	
	private InputStream execute(String relativeUri, String contentType, String httpMethod,String userpass) throws IOException {
	    HttpURLConnection connection = initializeConnection(relativeUri, contentType, httpMethod,userpass);
	    connection.connect();
	    HttpStatusCodes checkStatus = checkStatus(connection);

	    InputStream content = connection.getInputStream();
	    content = logRawContent(httpMethod + " request:\n  ", content, "\n");
	    return content;
	  }
	
	private HttpURLConnection initializeConnection(String absolutUri, String contentType, String httpMethod,String userpass)
		      throws MalformedURLException, IOException {
		    URL url = new URL(absolutUri);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		    connection.setRequestMethod(httpMethod);
		    connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
		    connection.setRequestProperty("Authorization","Basic "+Base64.getEncoder().encodeToString(userpass.getBytes()));
		    if(HTTP_METHOD_POST.equals(httpMethod) || HTTP_METHOD_PUT.equals(httpMethod)) {
		      connection.setDoOutput(true);
		      connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, contentType);
		    }

		    return connection;
		  }
	
	private HttpURLConnection connect(String relativeUri, String contentType, String httpMethod,String userpass) throws IOException {
	    HttpURLConnection connection = initializeConnection(relativeUri, contentType, httpMethod,userpass);

	    connection.connect();
	    checkStatus(connection);

	    return connection;
	  }
	
	private HttpStatusCodes checkStatus(HttpURLConnection connection) throws IOException {
	    HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
	    if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
	      throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " " + httpStatusCode.toString());
	    }
	    return httpStatusCode;
	  }
	
	private InputStream logRawContent(String prefix, InputStream content, String postfix) throws IOException {
	    if(PRINT_RAW_CONTENT) {
	      byte[] buffer = streamToArray(content);
	      //System.out.println(prefix + new String(buffer) + postfix);
	      content.close();
	      return new ByteArrayInputStream(buffer);
	    }
	    return content;
	  }
	
	private byte[] streamToArray(InputStream stream) throws IOException {
	    byte[] result = new byte[0];
	    byte[] tmp = new byte[8192];
	    int readCount = stream.read(tmp);
	    while(readCount >= 0) {
	      byte[] innerTmp = new byte[result.length + readCount];
	      System.arraycopy(result, 0, innerTmp, 0, result.length);
	      System.arraycopy(tmp, 0, innerTmp, result.length, readCount);
	      result = innerTmp;
	      readCount = stream.read(tmp);
	    }
	    return result;
	  }
	
	private String createUri(String serviceUri, String entitySetName, String id) {
	    final StringBuilder absolutUri = new StringBuilder(serviceUri).append(entitySetName);
	    if(id != null) {
	      absolutUri.append("(").append(id).append(")");
	    }
	    return absolutUri.toString();
	  }
	
	public ODataEntry readEntry(Edm edm, String serviceUri, String contentType, String entitySetName, String keyValue,String userpass)
		      throws IOException, ODataException {
		    EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
		    String absolutUri = createUri(serviceUri, entitySetName, keyValue);
		    
		    InputStream content = execute(absolutUri, contentType, HTTP_METHOD_GET,userpass);
		    return EntityProvider.readEntry(contentType,
		        entityContainer.getEntitySet(entitySetName),
		        content,
		        EntityProviderReadProperties.init().build());
		  }
	
	private static String prettyPrint(ODataEntry createdEntry) {
	    return prettyPrint(createdEntry.getProperties(), 0);
	  }
	
	private static String prettyPrint(Map<String, Object> properties, int level) {
	    StringBuilder b = new StringBuilder();
	    Set<Entry<String, Object>> entries = properties.entrySet();
	    
	    for (Entry<String, Object> entry : entries) {
	      intend(b, level);
	      b.append(entry.getKey()).append(": ");
	      Object value = entry.getValue();
	      if(value instanceof Map) {
	        value = prettyPrint((Map<String, Object>)value, level+1);
	        b.append(value).append("\n");
	      } else if(value instanceof Calendar) {
	        Calendar cal = (Calendar) value;
	        value = SimpleDateFormat.getInstance().format(cal.getTime());
	        b.append(value).append("\n");
	      } else if(value instanceof ODataDeltaFeed) {
	        ODataDeltaFeed feed = (ODataDeltaFeed) value;
	        List<ODataEntry> inlineEntries =  feed.getEntries();
	        b.append("{");
	        for (ODataEntry oDataEntry : inlineEntries) {
	          value = prettyPrint((Map<String, Object>)oDataEntry.getProperties(), level+1);
	          b.append("\n[\n").append(value).append("\n],");
	        }
	        b.deleteCharAt(b.length()-1);
	        intend(b, level);
	        b.append("}\n");
	      } else {
	        b.append(value).append("\n");
	      }
	    }
	    // remove last line break
	    b.deleteCharAt(b.length()-1);
	    return b.toString();
	  }
	
	
	private static void intend(StringBuilder builder, int intendLevel) {
	    for (int i = 0; i < intendLevel; i++) {
	      builder.append("  ");
	    }
	  }
	
	/* private CloseableHttpClient getHttpClient() {
		CloseableHttpClient build = HttpClientBuilder.create().build();
		return build;
	} */
	
	
	

}

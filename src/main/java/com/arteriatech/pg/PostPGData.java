package com.arteriatech.pg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class PostPGData
 */
@WebServlet("/PostPGData")
public class PostPGData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SERVICEURL = "https://af5a91eb208b.hana.ondemand.com/artodata/service.xsodata";
	private static final String METADATA = "$metadata";
	private static final String APPLICATION_XML = "application/xml; charset=utf-8";
//	private static final String APPLICATION_XML = "application/atom+xml";
	private static final String HTTP_METHOD_GET = "GET";
	private static final String USER = "S0016812987";
	private static final String PW = "Art@2cts1234567";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostPGData() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		try {/*
			response.getWriter().println("1");
//			ODataClient client = ODataClientFactory.getV4();
//			ODataServiceDocumentReques
			ODataClient client = ODataClientFactory.getClient();
			response.getWriter().println("2");
			String serviceRoot = "https://af5a91eb208b.hana.ondemand.com/artodata/service.xsodata";
			response.getWriter().println("3");
			ODataServiceDocumentRequest req = client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceRoot);
			response.getWriter().println("4");
//			req.setAccept(APPLICATION_XML);
			req.setContentType(APPLICATION_XML);			
			response.getWriter().println("5");
			ODataRetrieveResponse<ClientServiceDocument> res = req.execute();
			response.getWriter().println("getETag: "+res.getETag());
			response.getWriter().println("getHeaderNames: "+res.getHeaderNames());
			response.getWriter().println("getContentType: "+res.getContentType());
			response.getWriter().println("getStatusCode: "+res.getStatusCode());
			response.getWriter().println("getStatusMessage: "+res.getStatusMessage());
			response.getWriter().println("toString: "+res.toString());
			ClientServiceDocument serviceDocument = res.getBody();
			response.getWriter().println("7");
			Collection<String> entitySetNames = serviceDocument.getEntitySetNames();
			Map<String,URI> entitySets = serviceDocument.getEntitySets();
			Map<String,URI> singletons = serviceDocument.getSingletons();
			Map<String,URI> functionImports = serviceDocument.getFunctionImports();
			URI productsUri = serviceDocument.getEntitySetURI("Products");
			response.getWriter().println("8");
			response.getWriter().println("entitySetNames: "+entitySetNames);
			response.getWriter().println("entitySets: "+entitySets);
			response.getWriter().println("singletons: "+singletons);
			response.getWriter().println("functionImports: "+functionImports);
			response.getWriter().println("productsUri: "+productsUri);
			response.getWriter().println("9");
			Iterator<String> entitySetNamesIterator = entitySetNames.iterator();
			while (entitySetNamesIterator.hasNext())
			{
				String contentEntitySetNames = entitySetNamesIterator.next();
				response.getWriter().println("contentEntitySetNames: "+contentEntitySetNames);
			}
			
			for (Map.Entry<String, URI> entry : entitySets.entrySet()) {
				response.getWriter().println("entry key: "+entry.getKey()+" entry value: "+entry.getValue());
			}
			
			for (Map.Entry<String, URI> singletonsEntry : singletons.entrySet()) {
				response.getWriter().println("entry key: "+singletonsEntry.getKey()+" entry value: "+singletonsEntry.getValue());
			}
			
			for (Map.Entry<String, URI> functionImportsEntry : functionImports.entrySet()) {
				response.getWriter().println("entry key: "+functionImportsEntry.getKey()+" entry value: "+functionImportsEntry.getValue());
			}
			
			response.getWriter().println("productsUri: "+productsUri.getQuery());
			
//			Edm redHelixEdm = readEdm(SERVICEURL, request, response);
			
			ODataFeed feed = readFeed(redHelixEdm, SERVICEURL, "application/atom+xml", QueryParams.PERPERSONAL,
					QueryParams.PERSONNAV, QueryParams.EXTERNALIDCODE + "'" + personIdExt + "'");
			if (feed.getEntries().isEmpty()) {
				uiReference.unvalidUserIdError(personIdExt, attributeData);
				throw new WrongPersonIdException();
			}
			for (ODataEntry entry : feed.getEntries()) {
				parseData(entry);
			}
			
			response.getWriter().println("getEntitySets: "+redHelixEdm.getEntitySets());
			response.getWriter().println("getFunctionImports: "+redHelixEdm.getFunctionImports());
			response.getWriter().println("getServiceMetadata: "+redHelixEdm.getServiceMetadata());
		*/} catch (Exception e) {
//			e.printStackTrace();
//			response.getWriter().println(e.getLocalizedMessage());
			response.getWriter().println(e.getMessage());
			response.getWriter().println("getCause: "+e.getCause());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(buffer.toString());
		}
	}
	/*public Edm readEdm(String serviceUrl, HttpServletRequest request, HttpServletResponse response) throws IOException, ODataException {
		InputStream content = null;
		try {
			response.getWriter().println("Inside readEdm()");
			content = execute(serviceUrl + "/" + METADATA, APPLICATION_XML, HTTP_METHOD_GET, request, response);
			response.getWriter().println("content: "+content.read());
//			entit
//			response.getWriter().println("content readBinary: "+EntityProvider.readBinary(content));
//			response.getWriter().println("content readBinary: "+EntityProvider.readFeed(contentType, entitySet, content, properties));
//			response.getWriter().println("content readErrorDocument: "+EntityProvider.readErrorDocument(content, APPLICATION_XML));
			response.getWriter().println("content readMetadata: "+EntityProvider.readMetadata(content, false));
			
			return EntityProvider.readMetadata(content, false);
//			EntityProvider.readBinary(content);
		} catch (Exception e) {
//			e.printStackTrace();
			response.getWriter().println(e.getLocalizedMessage());
			response.getWriter().println(e.getMessage());
			response.getWriter().println("getCause: "+e.getCause());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(buffer.toString());
		}
		return EntityProvider.readMetadata(content, false);
	}*/
	
	/*public ODataFeed readFeed(Edm edm, String serviceUri, String contentType, String entitySetName, String expand,
			String filter) throws IOException, ODataException {
		EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
		String absolutUri = createUri(serviceUri, entitySetName, null, expand, filter);

		InputStream content = (InputStream) connect(absolutUri, contentType, "GET").getContent();
		return EntityProvider.readFeed(contentType, entityContainer.getEntitySet(entitySetName), content,
				EntityProviderReadProperties.init().build());
	}*/
	
	/*private String createUri(String serviceUri, String entitySetName, String id, String expand, String filter) {
		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(entitySetName);
		if (id != null) {
			absolutUri.append("(").append(id).append(")");
		}
		if (expand != null && filter != null) {
			absolutUri.append("/?$expand=").append(expand).append("&$filter=").append(filter);
		}

		return absolutUri.toString();
	}*/
	
	/*public InputStream execute(String relativeUri, String contentType, String httpMethod, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println("Inside execute() relativeUri: "+relativeUri);
		HttpURLConnection connection = initializeConnection(relativeUri, contentType, httpMethod, request, response);
		response.getWriter().println("Inside execute() after initializeConnection(): "+connection.getResponseCode());
		connection.connect();
		HttpStatusCodes httpStatusCode = null;
		httpStatusCode = checkStatus(connection, request, response);
		response.getWriter().println("Inside execute()- httpStatusCode: "+httpStatusCode);
		InputStream content = connection.getInputStream();
		content = logRawContent(httpMethod + " request:\n  ", content, "\n", request, response);
		response.getWriter().println("Inside execute()- content:"+content.toString());
		connection.disconnect();
		return content;
	}*/
	
	public HttpURLConnection initializeConnection(String absolutUri, String contentType, String httpMethod, HttpServletRequest request, HttpServletResponse response)
			throws MalformedURLException, IOException {
		response.getWriter().println("Inside initializeConnection()");
		URL url = new URL(absolutUri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod(httpMethod);
		connection.setRequestProperty("Accept", contentType);
		connection.setRequestProperty("Authorization", encodeBase64(request, response));
		response.getWriter().println("Inside initializeConnection() connection: "+connection.getResponseMessage());
		return connection;
	}
	
	public String encodeBase64(HttpServletRequest request, HttpServletResponse response) {
		String encoding = "";
		try {
			response.getWriter().println("Inside encodeBase64()");
		
			String userId = USER + ":" + PW;
			encoding = "Basic "+Base64.getEncoder().encodeToString(userId.getBytes());
//			encoding += new String(Base64.getEncoder().encode(userId.getBytes()));
			
			response.getWriter().println("Inside encodeBase64()-encoding"+encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encoding;
	}
	
	/*public HttpStatusCodes checkStatus(HttpURLConnection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println("Inside checkStatus()");
		HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
		response.getWriter().println("httpStatusCode: "+httpStatusCode.getStatusCode());
		if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
			throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " "
					+ httpStatusCode.toString());
		}
		return httpStatusCode;
	}*/
	
	public static InputStream logRawContent(String prefix, InputStream content, String postfix, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println("Inside logRawContent()");
		byte[] buffer = streamToArray(content, request, response);
		content.close();
		response.getWriter().println("buffer: "+buffer);
		return new ByteArrayInputStream(buffer);

	}
	
	public static byte[] streamToArray(InputStream stream, HttpServletRequest request, HttpServletResponse response) throws IOException {
		byte[] result = new byte[0];
		try {
			response.getWriter().println("Inside logRawContent()");
			byte[] tmp = new byte[8192];
			int readCount = stream.read(tmp);
			while (readCount >= 0) {
				byte[] innerTmp = new byte[result.length + readCount];
				System.arraycopy(result, 0, innerTmp, 0, result.length);
				System.arraycopy(tmp, 0, innerTmp, result.length, readCount);
				result = innerTmp;
				readCount = stream.read(tmp);
			}
			response.getWriter().println("Inside logRawContent()-readCount"+readCount);
			response.getWriter().println("Inside logRawContent()-result"+result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

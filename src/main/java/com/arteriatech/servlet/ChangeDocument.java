// package com.arteriatech.ppcutils.pc;

// import java.io.BufferedReader;
// import java.io.ByteArrayInputStream;
// import java.io.DataOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.StringReader;
// import java.net.HttpURLConnection;
// import java.net.URI;
// import java.net.URL;
// import java.nio.charset.StandardCharsets;
// import java.time.OffsetDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.HashMap;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Properties;
// import java.util.Random;
// import java.util.UUID;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// import javax.mail.Message;
// import javax.mail.MessagingException;
// import javax.mail.PasswordAuthentication;
// import javax.mail.SendFailedException;
// import javax.mail.Session;
// import javax.mail.Transport;
// import javax.mail.internet.InternetAddress;
// import javax.mail.internet.MimeBodyPart;
// import javax.mail.internet.MimeMessage;
// import javax.mail.internet.MimeMultipart;
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import javax.xml.parsers.DocumentBuilder;
// import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.parsers.ParserConfigurationException;

// import org.apache.http.Header;
// import org.apache.http.HttpEntity;
// import org.apache.http.HttpHeaders;
// import org.apache.http.HttpResponse;
// import org.apache.http.auth.AuthScope;
// import org.apache.http.auth.UsernamePasswordCredentials;
// import org.apache.http.client.ClientProtocolException;
// import org.apache.http.client.CredentialsProvider;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.entity.StringEntity;
// import org.apache.http.impl.client.BasicCredentialsProvider;
// import org.apache.http.impl.client.CloseableHttpClient;
// import org.apache.http.impl.client.HttpClientBuilder;
// import org.apache.http.util.EntityUtils;
// import org.apache.olingo.odata2.api.batch.BatchException;
// import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
// import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
// import org.apache.olingo.odata2.api.client.batch.BatchPart;
// import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
// import org.apache.olingo.odata2.api.ep.EntityProvider;
// import org.w3c.dom.Document;
// import org.w3c.dom.Element;
// import org.w3c.dom.NamedNodeMap;
// import org.w3c.dom.Node;
// import org.w3c.dom.NodeList;
// import org.xml.sax.InputSource;
// import org.xml.sax.SAXException;

// import com.arteriatech.ppcutils.download.CommonUtils;
// import com.arteriatech.ppcutils.download.DestinationUtils;
// import com.google.gson.Gson;
// import com.google.gson.JsonArray;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonNull;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import com.google.gson.internal.JsonReaderInternalAccess;

// public class ChangeDocument extends HttpServlet {

// 	CommonUtils commonUtils = new CommonUtils();
// 	private static final long serialVersionUID = 1L;
// 	private final static String boundary = "batch_" + UUID.randomUUID().toString();
// 	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
// 	public static final String APPLICATION_JSON = "application/json";
// 	public static final String AUTHORIZATION_HEADER = "Authorization";	
// 	public static String docHangeNo = "";
// 	boolean headerRecordMismatch = true , itemsRecordMismatch = false , deletionIndicatorEnabled =false ; 
// 	static String srcRefId ="";
// 	static String src ="";
// 	static String createdBy = "";
// 	static String createdAt =" ";
// 	static String objectValueMaxLength = "" ,maxLengthOfChangTabKey ="";
// 	static long createdOnInMillis = 0l;

// 	final String senderName = commonUtils.getDestinationProperties("emailid", DestinationUtils.PLATFORMEMAIL);
// 	final String senderPassWord = commonUtils.getDestinationProperties("Password", DestinationUtils.PLATFORMEMAIL);

// 	/**
// 	 * @see HttpServlet#HttpServlet()
// 	 */
// 	public ChangeDocument() {
// 		super();
// 		// TODO Auto-generated constructor stub
// 	}

// 	/**
// 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
// 	 */
// 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// 		// TODO Auto-generated method stub
// 		response.getWriter().append("Served at: ").append(request.getContextPath());
// 	}

// 	/**
// 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
// 	 */
// 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// 		boolean debug =false ;
// 		Properties properties  =new Properties();
// 		properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

// 		String reciverEmailId = properties.getProperty("EmailId");
// 		try{
// 			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
// 				debug = true;
// 			}

// 			String pcgwUrl = commonUtils.getDestinationProperties("URL", "PCGWHANA");
// 			String pcgwUser  = commonUtils.getDestinationProperties("User", "PCGWHANA");
// 			String pcgwPass    = commonUtils.getDestinationProperties("Password", "PCGWHANA");

// 			String dataSourceUrl = commonUtils.getDestinationProperties("URL", "DataSource");
// 			String dataSourceUser  = commonUtils.getDestinationProperties("User", "DataSource");
// 			String dataSourcePass    = commonUtils.getDestinationProperties("Password", "DataSource");

// 			createdBy = commonUtils.getUserPrincipal(request, "name", response);
// 			createdAt = commonUtils.getCreatedAtTime();
// 			createdOnInMillis = commonUtils.getCreatedOnDate();

// 			if(debug){
// 				response.getWriter().println("PCGWHANAurl :"+pcgwUrl);
// 				response.getWriter().println("PCGWHANAuser : "+ pcgwUser);
// 				//				response.getWriter().println("PCGWHANApass : "+ PCGWHANApass);
// 				response.getWriter().println("dataSourceUrl :"+dataSourceUrl);
// 				response.getWriter().println("dataSourceUser : "+ dataSourceUser);
// 				//				response.getWriter().println("dataSourcePass : "+ dataSourcePass);
// 				response.getWriter().println("createdBy: " + createdBy);
// 				response.getWriter().println("createdAt: " + createdAt);
// 				response.getWriter().println("createdOnInMillis: " + createdOnInMillis);
// 				response.getWriter().println("senderName: " + senderName);
// 				//			response.getWriter().println(" senderPassWord: " + senderPassWord);
// 			}

// 			JsonObject emailDetails = new JsonObject();
// 			emailDetails.addProperty("UserName", senderName);
// 			emailDetails.addProperty("Password", senderPassWord);

// 			String payload	= commonUtils.getGetBody(request, response);

// 			if(debug){
// 				response.getWriter().println("Payload : "+ payload);
// 			}

// 			if(!payload.isEmpty() && payload!=null){

// 				Runnable runnable = new Runnable() {
// 					@Override
// 					public void run() {
// 						try{
// 							boolean debug=false  ;
// 							JsonObject validatedHeaders = new JsonObject();
// 							String serviceName = "" , entityName = "" , aggregatorID = "" , ipAddress =""; 

// 							String pcgwUserPass =pcgwUser+":"+pcgwPass;
// 							String dataSourceUserPass = dataSourceUser +":"+dataSourcePass;

// 							Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 							changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 							changeSetHeaders.put("Accept", APPLICATION_JSON);
// 							changeSetHeaders.put(AUTHORIZATION_HEADER,"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));

// 							List<BatchPart> batchParts = new ArrayList<BatchPart>();
// 							JsonParser parser = new JsonParser();
// 							JsonArray inputJsonArray = parser.parse(payload).getAsJsonArray();

// 							if(debug){
// 								response.getWriter().println("Extracted JsonArray From Payload : "+ inputJsonArray);
// 							}

// 							getMaxLengthsByMetaData(response, dataSourceUrl, dataSourceUserPass, debug);

// 							for(JsonElement element : inputJsonArray) {
// 								JsonObject jsonObject = element.getAsJsonObject();

// 								if(debug){
// 									response.getWriter().println("JsonObject from jsonArray payload: "+ jsonObject);
// 								}

// 								validatedHeaders = validateAndGetHeaders(request, response, jsonObject, debug);
// 								if(debug){
// 									response.getWriter().println("result : "+ validatedHeaders);
// 								}

// 								if(validatedHeaders.get("Status").getAsString().equalsIgnoreCase("000001")){
// 									aggregatorID = validatedHeaders.get("AggregatorID").getAsString();
// 									serviceName = validatedHeaders.get("ServiceName").getAsString();
// 									entityName = validatedHeaders.get("EntityName").getAsString();
// 									ipAddress = validatedHeaders.get("IP").getAsString();

// 									if(jsonObject.has("Source") && !jsonObject.get("Source").isJsonNull() && !jsonObject.get("Source").getAsString().trim().equalsIgnoreCase("")){
// 										src = jsonObject.get("Source").getAsString();
// 									}

// 									if(	jsonObject.has("SourceReferenceID") && !jsonObject.get("SourceReferenceID").isJsonNull() && !jsonObject.get("SourceReferenceID").getAsString().trim().equalsIgnoreCase("")){
// 										srcRefId = jsonObject.get("SourceReferenceID").getAsString();
// 									}

// 									if(debug){
// 										response.getWriter().println("srcRefId : "+ srcRefId);
// 										response.getWriter().println("src : "+ src);
// 									}

// 									String objectClass = getObjectClass(request, response, pcgwUrl, pcgwUserPass,aggregatorID ,jsonObject.get("EntityName").getAsString(), debug);
// 									if(debug){
// 										response.getWriter().println("object : "+ objectClass);
// 									}

// 									String headerKeyProperties[]= getKeyPropertyTag(response, dataSourceUrl,dataSourceUserPass ,serviceName ,entityName ,  debug);
// 									if(debug){
// 										response.getWriter().println("keyProperty : "+ headerKeyProperties);
// 									}

// 									BatchChangeSet changeSetcdHdr = BatchChangeSet.newBuilder().build();
// 									BatchChangeSet changeSetcdPos = BatchChangeSet.newBuilder().build();

// 									if (((jsonObject.has("oldPayload")) &&  (jsonObject.get("oldPayload").isJsonNull() ||  jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")))
// 											&& jsonObject.has("newPayload") &&( jsonObject.get("newPayload").isJsonNull() || jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase(""))) {
// 										JsonObject responseObj = new JsonObject();
// 										responseObj.addProperty("Message", "Invalid Input Both Old Payload and New Payload is Empty");
// 										responseObj.addProperty("Status", "000002");
// 										responseObj.addProperty("ErrorCode", "J002");
// 										if(debug){
// 											response.getWriter().println(responseObj);
// 										}
// 									}else if(jsonObject.has("oldPayload") && !jsonObject.get("oldPayload").isJsonNull() && !jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")
// 											&& jsonObject.has("newPayload") && !jsonObject.get("newPayload").isJsonNull() && !jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase("")){

// 										String oldPayloadStr = jsonObject.get("oldPayload").getAsString();
// 										String newPayloadStr = jsonObject.get("newPayload").getAsString();

// 										JsonParser jsonParser= new JsonParser();
// 										JsonObject oldPayloadForCDHdr = jsonParser.parse(oldPayloadStr).getAsJsonObject();
// 										JsonObject newPayloadForCDHdr = jsonParser.parse(newPayloadStr).getAsJsonObject();

// 										JsonObject oldPayloadForCDPos = jsonParser.parse(oldPayloadStr).getAsJsonObject();
// 										JsonObject newPayloadForCDPos = jsonParser.parse(newPayloadStr).getAsJsonObject();

// 										JsonObject oldPayloadForHeader = jsonParser.parse(oldPayloadStr).getAsJsonObject();
// 										JsonObject newPayloadForHeader = jsonParser.parse(newPayloadStr).getAsJsonObject();

// 										if(debug){
// 											response.getWriter().println("Update");
// 											response.getWriter().println("Update oldPayload : "+ oldPayloadForCDHdr);
// 											response.getWriter().println("Update newPayload : "+ newPayloadForCDHdr);
// 										}

// 										JsonObject validatedkeyObject= validateKeyProperties(response, oldPayloadForCDHdr , newPayloadForCDHdr , headerKeyProperties ,debug);

// 										if(validatedkeyObject.get("Status").getAsString().equalsIgnoreCase("000001")){

// 											docHangeNo = generateDocChangeNo(request, response, dataSourceUrl, dataSourceUserPass , debug);
// 											if(debug){
// 												response.getWriter().println("docHangeNo : "+ docHangeNo);
// 											}
// 											response.getWriter().println("docHangeNo : "+ docHangeNo);

// 											String concatinatedObjectValue = "";
// 											for(String key : headerKeyProperties){
// 												String objectValue = newPayloadForCDHdr.get(key).getAsString();
// 												concatinatedObjectValue = concatinatedObjectValue.concat(objectValue);
// 											}
// 											if(debug){
// 												response.getWriter().println("concatinatedObjectValue " + concatinatedObjectValue);
// 											}

// 											changeSetcdPos = postHeadersToCdPos(request , response ,changeSetHeaders,pcgwUrl, oldPayloadForHeader, newPayloadForHeader,  concatinatedObjectValue, aggregatorID, objectClass, jsonObject.get("EntityName").getAsString(), debug);
// 											if(debug){
// 												response.getWriter().println("Update Header changeSetcdPos"+changeSetcdPos.getChangeSetParts().size());
// 											}
// 											if(changeSetcdPos!=null  && changeSetcdPos.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdPos);
// 											}
// 											changeSetcdPos = BatchChangeSet.newBuilder().build();

// 											changeSetcdPos = postToCDPos(request ,response , changeSetHeaders,oldPayloadForCDPos, newPayloadForCDPos ,dataSourceUrl, dataSourceUserPass, emailDetails,newPayloadForCDPos ,pcgwUrl ,aggregatorID,  "U" ,concatinatedObjectValue ,jsonObject.get("EntityName").getAsString() ,  objectClass , reciverEmailId ,debug);
// 											if(debug){
// 												response.getWriter().println("Update Item changeSetcdPos"+changeSetcdPos.getChangeSetParts().size());
// 											}
// 											if(changeSetcdPos!=null  && changeSetcdPos.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdPos);
// 											}
// 											changeSetcdPos = BatchChangeSet.newBuilder().build();

// 											if(deletionIndicatorEnabled){
// 												changeSetcdHdr = postToCDHdr(request,response , changeSetHeaders,oldPayloadForCDHdr, newPayloadForCDHdr ,pcgwUrl,   dataSourceUrl , dataSourceUserPass ,aggregatorID, "U"  ,jsonObject.get("EntityName").getAsString() , concatinatedObjectValue  , objectClass , ipAddress,debug);
// 											}else if(!deletionIndicatorEnabled || headerRecordMismatch){
// 												changeSetcdHdr = postToCDHdr(request , response , changeSetHeaders,oldPayloadForCDHdr, newPayloadForCDHdr ,pcgwUrl,   dataSourceUrl , dataSourceUserPass ,aggregatorID, "U"  ,jsonObject.get("EntityName").getAsString() , concatinatedObjectValue  , objectClass , ipAddress,debug);
// 											}
// 											if(debug){
// 												response.getWriter().println("Update changeSetcdHdr"+changeSetcdHdr.getChangeSetParts().size());
// 											}
// 											if(changeSetcdHdr!=null  &&changeSetcdHdr.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdHdr);
// 												deletionIndicatorEnabled = false ;
// 											}
// 										}else{
// 											if(debug){
// 												response.getWriter().println("validatedkeyObject: " + validatedkeyObject);
// 											}
// 										}
// 									}else if (((jsonObject.has("oldPayload")) &&  (jsonObject.get("oldPayload").isJsonNull() ||  jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")))
// 											&& jsonObject.has("newPayload") && !jsonObject.get("newPayload").isJsonNull() && !jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase(""))  {

// 										String newPayloadStr = jsonObject.get("newPayload").getAsString();
// 										JsonParser jsonParser= new JsonParser();

// 										JsonObject newPayloadForCDHdr = jsonParser.parse(newPayloadStr).getAsJsonObject();
// 										JsonObject newPayloadForCDPos = jsonParser.parse(newPayloadStr).getAsJsonObject();

// 										JsonObject validatedNewPayload = validateKeyProperties(response, null, newPayloadForCDHdr, headerKeyProperties, debug);

// 										if(validatedNewPayload.get("Status").getAsString().equalsIgnoreCase("000001")){
// 											docHangeNo = generateDocChangeNo(request, response, dataSourceUrl, dataSourceUserPass , debug);
// 											response.getWriter().println("docHangeNo : "+ docHangeNo);
// 											String concatinatedObjectValue = "";
// 											for(String key : headerKeyProperties){
// 												String objectValue = newPayloadForCDHdr.get(key).getAsString();
// 												concatinatedObjectValue = concatinatedObjectValue.concat(objectValue);
// 											}

// 											if(debug){
// 												response.getWriter().println("Insert");
// 												response.getWriter().println("concatinatedObjectValue " + concatinatedObjectValue);
// 												response.getWriter().println("Insert newPayloadForCDHdr : "+ newPayloadForCDHdr);
// 											}
// 											changeSetcdHdr = postToCDHdr(request, response,changeSetHeaders , null, newPayloadForCDHdr ,pcgwUrl , dataSourceUrl , dataSourceUserPass , aggregatorID, "I"   ,jsonObject.get("EntityName").getAsString() , concatinatedObjectValue , objectClass , ipAddress ,debug);
// 											if(debug){
// 												response.getWriter().println("Insert changeSetcdHdr size"+changeSetcdHdr.getChangeSetParts().size());
// 											}

// 											if(changeSetcdHdr!=null  && changeSetcdHdr.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdHdr);
// 											}

// 											if(debug){
// 												response.getWriter().println("Insert newPayloadForCDPos : "+ newPayloadForCDPos);
// 											}

// 											changeSetcdPos = postToCDPos(request ,response , changeSetHeaders,null,  newPayloadForCDPos , dataSourceUrl, dataSourceUserPass, emailDetails,newPayloadForCDPos ,pcgwUrl ,aggregatorID, "I" ,concatinatedObjectValue ,jsonObject.get("EntityName").getAsString() ,  objectClass , reciverEmailId,debug);
// 											if(debug){
// 												response.getWriter().println("Insert changeSetcdPos size"+changeSetcdPos.getChangeSetParts().size());
// 											}
// 											if(changeSetcdPos!=null  && changeSetcdPos.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdPos);
// 											}
// 										}else{
// 											if(debug){
// 												response.getWriter().println("validateNewPayload: " + validatedNewPayload);
// 											}
// 										}
// 									}else if (( (jsonObject.has("oldPayload")) &&  (!jsonObject.get("oldPayload").isJsonNull() ||  !jsonObject.get("oldPayload").getAsString().trim().equalsIgnoreCase("")))
// 											&& jsonObject.has("newPayload") && (jsonObject.get("newPayload").isJsonNull() || jsonObject.get("newPayload").getAsString().trim().equalsIgnoreCase(""))) {
// 										String oldPayloadStr = jsonObject.get("oldPayload").getAsString();

// 										JsonParser jsonParser= new JsonParser();
// 										JsonObject oldPayloadForCDHdr = jsonParser.parse(oldPayloadStr).getAsJsonObject();
// 										JsonObject oldPayloadForCDPos = jsonParser.parse(oldPayloadStr).getAsJsonObject();

// 										JsonObject validateOldPayload = validateKeyProperties(response, oldPayloadForCDHdr, null, headerKeyProperties, debug);

// 										if(validateOldPayload.get("Status").getAsString().equalsIgnoreCase("000001")){
// 											docHangeNo = generateDocChangeNo(request, response, dataSourceUrl, dataSourceUserPass , debug);
// 											response.getWriter().println("docHangeNo : "+ docHangeNo);
// 											String concatinatedObjectValue = "";
// 											for(String key : headerKeyProperties){
// 												String objectValue = oldPayloadForCDHdr.get(key).getAsString();
// 												concatinatedObjectValue = concatinatedObjectValue.concat(objectValue);
// 											}
// 											if(debug){
// 												response.getWriter().println("Delete concatinatedObjectValue " + concatinatedObjectValue);
// 											}
// 											changeSetcdHdr = postToCDHdr(request , response, changeSetHeaders, oldPayloadForCDHdr, null, pcgwUrl, dataSourceUrl , dataSourceUserPass, aggregatorID, "D",  entityName, concatinatedObjectValue, objectClass, ipAddress, debug);
// 											if(debug){
// 												response.getWriter().println("Delete changeSetcdHdr size"+changeSetcdHdr.getChangeSetParts().size());
// 											}

// 											if(changeSetcdHdr!=null  && changeSetcdHdr.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdHdr);
// 											}

// 											if(debug){
// 												response.getWriter().println("Delete oldPayloadForCDPos : "+ oldPayloadForCDPos);
// 											}

// 											changeSetcdPos = postToCDPos(request, response, changeSetHeaders,oldPayloadForCDPos, null, dataSourceUrl,dataSourceUserPass, emailDetails, null, pcgwUrl, aggregatorID, "D", concatinatedObjectValue, entityName, objectClass , reciverEmailId, debug);
// 											if(debug){
// 												response.getWriter().println("Delete changeSetcdPos size"+changeSetcdPos.getChangeSetParts().size());
// 											}
// 											if(changeSetcdPos!=null  && changeSetcdPos.getChangeSetParts().size()>0){
// 												batchParts.add(changeSetcdPos);
// 											}
// 										}else{
// 											if(debug){
// 												response.getWriter().println("validateOldPayload: " + validateOldPayload);
// 											}
// 										}
// 									}
// 								}else{
// 									if(debug){
// 										response.getWriter().println("Result :"+validatedHeaders);
// 									}
// 								}
// 							}
// 							if(debug){
// 								response.getWriter().println("batchParts.size"+batchParts.size());
// 								response.getWriter().println("batchParts"+batchParts);
// 							}

// 							if(batchParts.size()>0){
// 								doBatchCall(response, request, pcgwUrl,pcgwUserPass,emailDetails, batchParts, debug);
// 							}
// 						}catch(Exception ex){
// 							JsonObject result = new JsonObject();
// 							StackTraceElement element[] = ex.getStackTrace();
// 							StringBuffer buffer = new StringBuffer();
// 							for (int i = 0; i < element.length; i++) {
// 								buffer.append(element[i]);
// 							}
// 							result.addProperty("Exception", ex.getClass().getCanonicalName());
// 							result.addProperty("Message",
// 									ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 							result.addProperty("Status", "000002");
// 							result.addProperty("ErrorCode", "J002");
// 							try {
// 								response.getWriter().println(result);
// 							} catch (IOException e) {
// 								e.printStackTrace();
// 							}
// 						}
// 					}
// 				};
// 				Thread thread = new Thread(runnable);
// 				thread.start();
// 				thread.join();

// 				JsonObject responseObj = new JsonObject();
// 				responseObj.addProperty("Message", "Process Started");
// 				responseObj.addProperty("Status", "000001");
// 				responseObj.addProperty("ErrorCode", "");
// 				response.getWriter().println(responseObj);
// 			}else{
// 				JsonObject responseObj = new JsonObject();
// 				responseObj.addProperty("Message", "Input Payload is Empty");
// 				responseObj.addProperty("Status", "000002");
// 				responseObj.addProperty("ErrorCode", "J002");
// 				if(debug){
// 					response.getWriter().println(responseObj);
// 				}
// 				return;
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "doPost()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		}
// 	}

// 	private void getMaxLengthsByMetaData(HttpServletResponse resp ,String dataSourceUrl , String dataSourceUserPass , boolean debug) throws IOException, ParserConfigurationException, SAXException{
// 		try{
// 			String pcgwExecuteUrl  = dataSourceUrl+"ARTEC/"+"PCGW/"+"/service.xsodata/$metadata";
// 			StringBuffer pcgwMetadataXml=executeUrlForMetaData(resp , pcgwExecuteUrl, dataSourceUserPass,  debug);

// 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
// 			DocumentBuilder builder = factory.newDocumentBuilder();
// 			ByteArrayInputStream inputStream = new ByteArrayInputStream(pcgwMetadataXml.toString().getBytes());
// 			Document document = builder.parse(inputStream);

// 			NodeList entityTypes = document.getElementsByTagName("EntityType");
// 			for (int i = 0; i < entityTypes.getLength(); i++) {
// 				Node entityType = entityTypes.item(i);
// 				NamedNodeMap entityTypeAttributes = entityType.getAttributes();
// 				Node nameAttribute = entityTypeAttributes.getNamedItem("Name");
// 				if (nameAttribute != null && nameAttribute.getNodeValue().equals("ChangeDocumentItemsType")) {
// 					NodeList properties = entityType.getChildNodes();
// 					for (int j = 0; j < properties.getLength(); j++) {
// 						Node property = properties.item(j);
// 						if (property.getNodeName().equals("Property")) {
// 							NamedNodeMap propertyAttributes = property.getAttributes();
// 							Node propertyNameAttribute = propertyAttributes.getNamedItem("Name");
// 							if (propertyNameAttribute != null && propertyNameAttribute.getNodeValue().equals("ObjectValue")) {
// 								Node maxLengthAttribute = propertyAttributes.getNamedItem("MaxLength");
// 								if (maxLengthAttribute != null) {
// 									objectValueMaxLength = maxLengthAttribute.getNodeValue();
// 									if(debug){
// 										resp.getWriter().println("Max length of ObjectValue field: " + objectValueMaxLength);
// 									}
// 								}
// 							}
// 							propertyNameAttribute = propertyAttributes.getNamedItem("Name");
// 							if (propertyNameAttribute != null && propertyNameAttribute.getNodeValue().equals("ChangedTablekey")) {
// 								Node maxLengthAttribute = propertyAttributes.getNamedItem("MaxLength");
// 								if (maxLengthAttribute != null) {
// 									maxLengthOfChangTabKey = maxLengthAttribute.getNodeValue();
// 									if(debug){
// 										resp.getWriter().println("Max length of ChangedTablekey field: " + maxLengthOfChangTabKey);
// 									}
// 								}
// 							}
// 						}
// 					}
// 				}
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "getKeyPropertyTag()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				resp.getWriter().println(result);
// 			}
// 		}
// 	}

// 	public static StringBuffer executeUrlForMetaData(HttpServletResponse response, String executeURL , String userPass ,  boolean debug ){
// 		BufferedReader in = null;
// 		StringBuffer responseStrBuffer = new StringBuffer();
// 		try {
// 			URL urlObj = new URL(executeURL);
// 			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
// 			connection.setRequestMethod("GET");
// 			connection.setRequestProperty("Content-Type","application/xml");
// 			connection.setRequestProperty("Accept","application/xml");
// 			connection.setRequestProperty("Authorization","Basic "+Base64.getEncoder().encodeToString(userPass.getBytes()));
// 			connection.setDoInput(true);
// 			int responseCode = connection.getResponseCode();

// 			if(debug){
// 				response.getWriter().println("responseCode : "+ responseCode);
// 			}
// 			if ((responseCode / 100) == 2) {
// 				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
// 				String inputLine;
// 				while ((inputLine = in.readLine()) != null) {
// 					responseStrBuffer.append(inputLine);
// 				}
// 			}
// 			if(debug){
// 				response.getWriter().println("responseStrBuffer : "+ responseStrBuffer);
// 			}
// 			return	responseStrBuffer;
// 		} catch (Exception e) {
// 			StackTraceElement element[] = e.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			if(debug){
// 				response.getWriter().println("executeURL.Full Stack Trace: " + buffer.toString());
// 			}
// 		} finally {
// 			return responseStrBuffer;
// 		}
// 	}

// 	private JsonObject validateAndGetHeaders(HttpServletRequest request , HttpServletResponse response ,JsonObject jsonPayload , boolean debug) throws IOException{
// 		String aggregatorID= "" ,serviceName ="", entityName ="" ,ipAddress ="" ;
// 		JsonObject successObject = new JsonObject();
// 		JsonObject errorObject = new JsonObject();
// 		try{
// 			if(	jsonPayload.has("AggregatorID") && !jsonPayload.get("AggregatorID").isJsonNull() && !jsonPayload.get("AggregatorID").getAsString().trim().equalsIgnoreCase("")){
// 				aggregatorID = jsonPayload.get("AggregatorID").getAsString();
// 				successObject.addProperty("AggregatorID", aggregatorID);
// 			}else{
// 				errorObject.addProperty("Message", "AggregatorID Not found");
// 			}

// 			if(	jsonPayload.has("ServiceName") && !jsonPayload.get("ServiceName").isJsonNull() && !jsonPayload.get("ServiceName").getAsString().trim().equalsIgnoreCase("")){
// 				serviceName = jsonPayload.get("ServiceName").getAsString();
// 				successObject.addProperty("ServiceName", serviceName);
// 			}else{
// 				errorObject.addProperty("Message1", "ServiceName Not found");
// 			}

// 			if(	jsonPayload.has("EntityName") && !jsonPayload.get("EntityName").isJsonNull() && !jsonPayload.get("EntityName").getAsString().trim().equalsIgnoreCase("")){
// 				entityName = jsonPayload.get("EntityName").getAsString();
// 				entityName = entityName.concat("Type");
// 				successObject.addProperty("EntityName", entityName);
// 			}else{
// 				errorObject.addProperty("Message2", "EntityName Not found");
// 			}

// 			if(	jsonPayload.has("IP") && !jsonPayload.get("IP").isJsonNull() && !jsonPayload.get("IP").getAsString().trim().equalsIgnoreCase("")){
// 				ipAddress = jsonPayload.get("IP").getAsString();
// 				successObject.addProperty("IP", ipAddress);
// 			}else{
// 				errorObject.addProperty("Message3", "IP Not found");
// 			}
// 			if(debug){
// 				response.getWriter().println("entityName"+ entityName);
// 				response.getWriter().println("serviceName : "+ serviceName);
// 				response.getWriter().println("aggregatorID : "+ aggregatorID);
// 				response.getWriter().println("errorObject : "+ errorObject);
// 				response.getWriter().println("successObject : "+ successObject);
// 			}

// 			if(errorObject.entrySet().isEmpty()){
// 				successObject.addProperty("Status", "000001");
// 				return successObject;
// 			}else{
// 				errorObject.addProperty("Status", "000002");
// 				errorObject.addProperty("ErrorCode", "J002");
// 				return errorObject;
// 			}
// 		}catch(Exception ex){
// 			errorObject = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			errorObject.addProperty("Exception", ex.getClass().getCanonicalName());
// 			errorObject.addProperty("Method", "validateAndGetHeaders()");
// 			errorObject.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			errorObject.addProperty("Status", "000002");
// 			errorObject.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(errorObject);
// 			}
// 			return errorObject;
// 		}
// 	}

// 	private String getObjectClass(HttpServletRequest request , HttpServletResponse response , String pcgwUrl , String pcgwUserPass , String aggrId ,String entityName ,boolean debug) throws IOException{
// 		String ObjectClass = "";
// 		try{
// 			String tcdobUrl = pcgwUrl+"ChangeDocCreateObjects" + "?$select=ObjectClass" + "&$filter=AggregatorID%20eq%20%27" + aggrId + "%27%20and%20TableName%20eq%20%27" + entityName + "%27";

// 			tcdobUrl = tcdobUrl.replaceAll(" ", "");
// 			JsonObject tcdobObj = commonUtils.executeURL(tcdobUrl, pcgwUserPass, response);
// 			if(debug){
// 				response.getWriter().println("tcdobUrl "+tcdobUrl);
// 				response.getWriter().println("tcdobObj :"+tcdobObj);
// 			}

// 			JsonArray tcdobObjArray = tcdobObj.get("d").getAsJsonObject().get("results").getAsJsonArray();

// 			for (JsonElement tcdobElement : tcdobObjArray) {
// 				JsonObject tcdobObject = tcdobElement.getAsJsonObject();
// 				ObjectClass = tcdobObject.get("ObjectClass").getAsString();
// 				if(debug){
// 					response.getWriter().println("ObjectClass :"+ObjectClass);
// 				}
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "getObjectClass()");
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		}
// 		return ObjectClass;
// 	}

// 	public static String[]  getKeyPropertyTag(HttpServletResponse resp , String dataSourceUrl ,String dataSourceUserPass,String serviceName , String entityName ,boolean debug) throws IOException{
// 		List<String> propertyNames = new ArrayList<>();
// 		try{
// 			dataSourceUrl = dataSourceUrl.replaceAll(" ", "");
// 			//			String executeURL = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/SCHARTEC/ARTEC/" + serviceName + "/service.xsodata/$metadata";
// 			String executeURL  = dataSourceUrl+"ARTEC/"+serviceName+"/service.xsodata/$metadata";
// 			if(debug){
// 				resp.getWriter().println("dataSourceUrl "+dataSourceUrl);
// 				resp.getWriter().println("executeURL "+executeURL);
// 			}
// 			StringBuffer metadataXml=executeUrlForMetaData(resp , executeURL, dataSourceUserPass,  debug);

// 			String keyProperty[] = getKeyProperty(resp, metadataXml,  entityName, debug);

// 			return keyProperty;
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "getKeyPropertyTag()");
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			resp.getWriter().println(result);
// 			return propertyNames.toArray(new String[0]) ;
// 		}
// 	}

// 	public static String[] getKeyProperty(HttpServletResponse resp,StringBuffer metadataXml, String entityName , boolean debug) throws Exception {
// 		String propertyName ="";
// 		List<String> propertyNames = new ArrayList<>();
// 		try {
// 			String xmlString = metadataXml.toString();
// 			ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
// 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
// 			DocumentBuilder builder = factory.newDocumentBuilder();
// 			Document doc = builder.parse(inputStream);
// 			doc.getDocumentElement().normalize();
// 			// Get all EntityType elements
// 			NodeList entityTypeList = doc.getElementsByTagName("EntityType");
// 			if(debug){
// 				resp.getWriter().println("entityTypeList : "+ entityTypeList);
// 			}
// 			// Iterate over each EntityType
// 			for (int i = 0; i < entityTypeList.getLength(); i++) {
// 				Node entityTypeNode = entityTypeList.item(i);
// 				if (entityTypeNode.getNodeType() == Node.ELEMENT_NODE) {
// 					Element entityTypeElement = (Element) entityTypeNode;
// 					// Get the name of the EntityType
// 					String entityTypeName = entityTypeElement.getAttribute("Name");
// 					if(debug){
// 						resp.getWriter().println("EntityType: " + entityTypeName);
// 					}
// 					if (entityTypeName.equals(entityName)) {
// 						// Get all Key elements within this EntityType
// 						NodeList keyList = entityTypeElement.getElementsByTagName("Key");
// 						// Iterate over each Key element
// 						for (int j = 0; j < keyList.getLength(); j++) {
// 							Node keyNode = keyList.item(j);
// 							if (keyNode.getNodeType() == Node.ELEMENT_NODE) {
// 								Element keyElement = (Element) keyNode;
// 								// Get all PropertyRef elements within this Key
// 								NodeList propertyRefList = keyElement.getElementsByTagName("PropertyRef");
// 								// Iterate over each PropertyRef and print the Name attribute
// 								for (int k = 0; k < propertyRefList.getLength(); k++) {
// 									Element propertyRefElement = (Element) propertyRefList.item(k);
// 									propertyName = propertyRefElement.getAttribute("Name");
// 									propertyNames.add(propertyName);
// 									if(debug){
// 										resp.getWriter().println("Key Property Name: " + propertyName);
// 									}
// 								}
// 							}
// 						}
// 						break;
// 					}
// 				}
// 			}
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 		return propertyNames.toArray(new String[0]);
// 	}



// 	private static JsonObject validateKeyProperties(HttpServletResponse response , JsonObject oldPayload , JsonObject newPayload , String [] keyProperties,boolean debug) throws IOException{
// 		JsonObject missingKey = new JsonObject();
// 		try {
// 			for (String key : keyProperties) {
// 				// Check if both JSON objects have the key and the values are neither null nor empty
// 				if(oldPayload!=null && newPayload!=null){
// 					if (oldPayload.has(key) && newPayload.has(key)) {
// 						JsonElement value1 = oldPayload.get(key);
// 						JsonElement value2 = newPayload.get(key);

// 						// Ensure values are not null or empty
// 						if (isValidValue(value1) && isValidValue(value2)) {
// 							// Compare the values associated with the key in both JSON objects
// 							if (!value1.equals(value2)) {
// 								// If the values differ, add the difference to the differences JsonObject
// 								JsonObject mismatch = new JsonObject();
// 								missingKey.addProperty("Message", "Key Property Values are not equal in both payloads");
// 								mismatch.addProperty(key, oldPayload.get(key).getAsString());
// 								mismatch.addProperty(key, newPayload.get(key).getAsString());

// 								missingKey.addProperty("Status", "000002");
// 							}else{
// 								missingKey.addProperty("Status", "000001");
// 							}
// 						} else {
// 							// If either value is null or empty, capture this scenario as well
// 							if (!isValidValue(value1)) {
// 								missingKey.addProperty("Message", "value "+"is Empty or null in old payload");
// 								missingKey.addProperty("Status", "000002");
// 							}
// 							if (!isValidValue(value2)) {
// 								missingKey.addProperty("Message2", "value "+"is Empty or null in New  payload");
// 								missingKey.addProperty("Status", "000002");
// 							}
// 						}
// 					} else {
// 						// Capture if the key is missing in either JSON object
// 						if (!oldPayload.has(key)) {
// 							missingKey.addProperty("Message", key +" not found in  Old Payload ");
// 							missingKey.addProperty("Status", "000002");
// 						}

// 						if (!newPayload.has(key)) {
// 							missingKey.addProperty("Message1", key +" not found in  New Payload ");
// 							missingKey.addProperty("Status", "000002");
// 						}
// 					}
// 				}else if(newPayload!=null && oldPayload==null){
// 					if ( newPayload.has(key)) {
// 						JsonElement value1 = newPayload.get(key);
// 						if (isValidValue(value1)) {
// 							missingKey.addProperty("Status", "000001");
// 						}else{
// 							if (!isValidValue(value1)) {
// 								missingKey.addProperty("Message", "value "+"is Empty or null in New payload");
// 								missingKey.addProperty("Status", "000002");
// 							}
// 						}
// 					}else{
// 						if (!newPayload.has(key)) {
// 							missingKey.addProperty("Message", key +" not found in  New Payload ");
// 							missingKey.addProperty("Status", "000002");
// 						}
// 					}
// 				}else if(newPayload==null && oldPayload!=null){
// 					if ( oldPayload.has(key)) {
// 						JsonElement value1 = oldPayload.get(key);
// 						if (isValidValue(value1)) {
// 							missingKey.addProperty("Status", "000001");
// 						}else{
// 							if (!isValidValue(value1)) {
// 								missingKey.addProperty("Message", "value "+"is Empty or null in Old payload");
// 								missingKey.addProperty("Status", "000002");
// 							}
// 						}
// 					}else{
// 						if (!oldPayload.has(key)) {
// 							missingKey.addProperty("Message", key +" not found in  Old Payload ");
// 							missingKey.addProperty("Status", "000002");
// 						}
// 					}
// 				}
// 			}
// 			if(debug){
// 				response.getWriter().println("missingKey :"+missingKey);
// 			}
// 			return missingKey;
// 		} catch (Exception e) {
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = e.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", e.getClass().getCanonicalName());
// 			result.addProperty("Method", "validateKeyProperties()");
// 			result.addProperty("Message",
// 					e.getClass().getCanonicalName() + "--->" + e.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 			return result ;
// 		}
// 	}

// 	private static boolean isValidValue(JsonElement value) {
// 		return value != null && !value.isJsonNull() && !value.getAsString().trim().isEmpty();
// 	}

// 	private String generateDocChangeNo(HttpServletRequest request , HttpServletResponse response ,String dataSourceUrl ,String userPass , boolean debug) throws IOException{
// 		String ChangeDocumentNumberRange = "";
// 		try{
// 			String docChangeUrl = dataSourceUrl +"ARTEC/NR/service.xsodata/ChangeDocumentNumberRange" ;
// 			JsonObject tdposUrlResponse= commonUtils.executeURL(docChangeUrl, userPass, response);

// 			if(debug){
// 				response.getWriter().println("tdposUrlResponse :"+tdposUrlResponse);
// 			}

// 			if(tdposUrlResponse!=null){
// 				JsonArray tdposUrlResponse1Array = tdposUrlResponse.get("d").getAsJsonObject().get("results")
// 						.getAsJsonArray();
// 				for (JsonElement elements : tdposUrlResponse1Array) {
// 					JsonObject jsonObject = elements.getAsJsonObject();
// 					ChangeDocumentNumberRange = jsonObject.get("ChangeDocumentNumberRange").getAsString();
// 					if(debug){
// 						response.getWriter().println("ChangeDocumentNumberRange :"+ChangeDocumentNumberRange);
// 					}
// 				}
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		}
// 		return ChangeDocumentNumberRange;
// 	}

// 	public  BatchChangeSet postToCDHdr(HttpServletRequest request,HttpServletResponse resp , Map<String, String> changeSetHeaders,JsonObject oldPayload, JsonObject newPayload , String pcgwUrl ,String dataSourceUrl,String dataSrcuserPass,String aggregatorID,String flag  , String entityName , String concatinatedObjectValue , String objectClass , String ipAddress ,boolean debug) throws IOException {
// 		String executeURL = "" ; 
// 		BatchChangeSet changeSetForInsert = BatchChangeSet.newBuilder().build();
// 		BatchChangeSet changeSetForUpdate = BatchChangeSet.newBuilder().build();
// 		BatchChangeSet changeSetForDelete = BatchChangeSet.newBuilder().build();
// 		try{
// 			JsonObject insertPayLoad = new JsonObject();
// 			executeURL = pcgwUrl+"ChangeDocumentHeader";
// 			//			Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 			//			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 			//			changeSetHeaders.put("Accept", APPLICATION_JSON);
// 			//			changeSetHeaders.put(AUTHORIZATION_HEADER,"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));

// 			if(oldPayload!=null){
// 				oldPayload.remove("Items");
// 			}
// 			if(newPayload!=null){
// 				newPayload.remove("Items");
// 			}
// 			if(debug){
// 				resp.getWriter().println("postToCDHdr after removing items oldPayload: " + oldPayload);
// 				resp.getWriter().println("postToCDHdr after removing items newPayload: " + newPayload);
// 			}

// 			if(flag.equalsIgnoreCase("I")){

// 				if(debug){
// 					resp.getWriter().println("postToCDHdr flag: " + flag);
// 				}

// 				insertPayLoad = formPayloadForCDHdr(resp, aggregatorID, objectClass, concatinatedObjectValue, ipAddress, flag, debug);

// 				if(debug){
// 					resp.getWriter().println("Insert insertPayLoad: " + insertPayLoad);
// 					resp.getWriter().println("Insert executeURL: " + executeURL);
// 				}

// 				BatchChangeSetPart changeRequestForInsert = BatchChangeSetPart.method("POST").uri(executeURL)
// 						.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				changeSetForInsert.add(changeRequestForInsert);
// 			}else if(flag.equalsIgnoreCase("U")){
// 				if(debug){
// 					resp.getWriter().println("flag: " + flag);
// 					resp.getWriter().println("entityName: " + entityName);
// 				}

// 				insertPayLoad = formPayloadForCDHdr(resp, aggregatorID, objectClass, concatinatedObjectValue, ipAddress, flag, debug);

// 				if(debug){
// 					resp.getWriter().println("Update InsertPayload: " + insertPayLoad);
// 					resp.getWriter().println("Update executeURL: " + executeURL);
// 				}
// 				BatchChangeSetPart changeRequestForUpdate = BatchChangeSetPart.method("POST").uri(executeURL)
// 						.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				changeSetForUpdate.add(changeRequestForUpdate);
// 			}else if(flag.equalsIgnoreCase("D")){
// 				if(debug){
// 					resp.getWriter().println("postToCDHdr flag: " + flag);
// 				}

// 				insertPayLoad = formPayloadForCDHdr(resp, aggregatorID, objectClass, concatinatedObjectValue, ipAddress, flag, debug);

// 				if(debug){
// 					resp.getWriter().println("Delete insertPayLoad: " + insertPayLoad);
// 					resp.getWriter().println("Delete executeURL: " + executeURL);
// 				}

// 				BatchChangeSetPart changeRequestForDelete = BatchChangeSetPart.method("POST").uri(executeURL)
// 						.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				changeSetForDelete.add(changeRequestForDelete);
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "postToCDHdr()");
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			resp.getWriter().println(result);
// 		}
// 		if(flag.equalsIgnoreCase("I")){
// 			return changeSetForInsert;
// 		}else if(flag.equalsIgnoreCase("U")){
// 			return changeSetForUpdate;
// 		}else{
// 			return changeSetForDelete;
// 		}
// 	}

// 	private JsonObject formPayloadForCDHdr(HttpServletResponse response ,String aggregatorID , String objectClass , String concatinatedObjectValue ,String  ipAddress , String flag , boolean debug) throws IOException{
// 		JsonObject insertPayLoad = new JsonObject();
// 		try{
// 			insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 			insertPayLoad.addProperty("ObjectClass", objectClass);

// 			int maxLength = 0;
// 			try {
// 				maxLength = Integer.parseInt(objectValueMaxLength);
// 				if(concatinatedObjectValue.length()>=maxLength){
// 					insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 				}else{
// 					insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 				}
// 			} catch (NumberFormatException e) {
// 				// Handle the exception or log an error message
// 				response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 			}

// 			insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 			insertPayLoad.addProperty("ChangeDocChangeNo", docHangeNo);
// 			insertPayLoad.addProperty("ChangeDocCreatedBy", createdBy);
// 			insertPayLoad.addProperty("ChangeDocCreatedAt", createdAt);
// 			insertPayLoad.addProperty("ChangeDocCreatedOn", "/Date("+createdOnInMillis+")/");
// 			insertPayLoad.addProperty("Tcode", ipAddress);
// 			insertPayLoad.addProperty("PlannedChangeNo", "");
// 			insertPayLoad.addProperty("WasPlanned", "");
// 			insertPayLoad.addProperty("ChangeInd", flag);
// 			insertPayLoad.addProperty("Language", "E");
// 			insertPayLoad.addProperty("Version", "000");
// 			insertPayLoad.addProperty("CreatedBy", createdBy);
// 			insertPayLoad.addProperty("CreatedAt", createdAt);
// 			insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 			insertPayLoad.addProperty("Source", src);
// 			insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "formPayloadForCDHdr()");
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		}
// 		return insertPayLoad;
// 	}

// 	private BatchChangeSet postToCDPos(HttpServletRequest request,HttpServletResponse response, Map<String, String> changeSetHeaders , JsonObject oldPayloads, JsonObject newPayloadForCDHdr, String dataSrcUrl ,String dataSrcUserPass,JsonObject emailDetails ,JsonObject newPayloads, String pcgwUrl,String aggregatorID,String flag, String concatinatedObjectValue, String entityName   ,String object , String reciverEmailId, boolean debug) throws Exception {
// 		String executeURL = "";
// 		BatchChangeSet changeSetforInsert = BatchChangeSet.newBuilder().build();
// 		BatchChangeSet changeSetsForUpdate = BatchChangeSet.newBuilder().build();
// 		BatchChangeSet changeSetsForDelete = BatchChangeSet.newBuilder().build();
// 		try {
// 			//			Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 			//			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 			//			changeSetHeaders.put("Accept", APPLICATION_JSON);
// 			//			changeSetHeaders.put(AUTHORIZATION_HEADER,
// 			//					"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));

// 			if(debug){
// 				response.getWriter().println("url :"+pcgwUrl);
// 				response.getWriter().println("flag :"+flag);
// 				response.getWriter().println(" oldPayloads:"+oldPayloads);
// 				response.getWriter().println("newPayloads :"+newPayloads);
// 			}

// 			if(flag.equalsIgnoreCase("U")){
// 				JsonArray oldItemsArray = oldPayloads.has("Items") && oldPayloads.get("Items").isJsonArray()
// 						? oldPayloads.getAsJsonArray("Items")
// 								: null;

// 						JsonArray newItemsArray = newPayloads.has("Items") && newPayloads.get("Items").isJsonArray()
// 								? newPayloads.getAsJsonArray("Items")
// 										: null;

// 								String olditemKeyProperties [] = null;
// 								String olditemEntityName = "";
// 								String newitemKeyProperties [] = null;
// 								JsonObject validatedkeyObject = new JsonObject();
// 								String newitemEntityName = "" ,newPayloadServiceName ="" ,oldPayloaderviceName="";

// 								if (oldItemsArray != null && newItemsArray != null) {

// 									JsonObject insertPayLoad= new JsonObject();

// 									insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 									insertPayLoad.addProperty("ObjectClass", object);
// 									if (concatinatedObjectValue != null && objectValueMaxLength != null && !objectValueMaxLength.isEmpty()) {
// 										int maxLength = 0;
// 										try {
// 											maxLength = Integer.parseInt(objectValueMaxLength);
// 											if(concatinatedObjectValue.length()>=maxLength){
// 												insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 											}else{
// 												insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 											}
// 										} catch (NumberFormatException e) {
// 											// Handle the exception or log an error message
// 											response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 										}
// 									}
// 									insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 									insertPayLoad.addProperty("TableName", entityName);
// 									int maxLength = 0;
// 									try {
// 										maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 										if(concatinatedObjectValue.length()>=maxLength){
// 											insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 										}else{
// 											insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 										}
// 									} catch (NumberFormatException e) {
// 										// Handle the exception or log an error message
// 										response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 									}
// 									insertPayLoad.addProperty("FieldName", "KEY");
// 									insertPayLoad.addProperty("ChangeInd", flag);
// 									insertPayLoad.addProperty("TextChange", "");
// 									insertPayLoad.addProperty("OldUnit", "");
// 									insertPayLoad.addProperty("NewUnit", "");
// 									insertPayLoad.addProperty("OldCurrency", "");
// 									insertPayLoad.addProperty("NewCurrency", "");
// 									insertPayLoad.addProperty("NewValue", "");
// 									insertPayLoad.addProperty("CreatedBy", createdBy);
// 									insertPayLoad.addProperty("CreatedAt", createdAt);
// 									insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 									insertPayLoad.addProperty("Source", src);
// 									insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 									if(debug){
// 										response.getWriter().println("postToCDPos newPayload  for header: " + insertPayLoad);
// 									}
// 									executeURL = pcgwUrl+"ChangeDocumentItems";

// 									BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
// 											.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 									changeSetsForUpdate.add(changeRequest);

// 									Map<String, JsonObject> oldItemsMap = new HashMap<>();


// 									for (JsonElement oldElement : oldItemsArray) {
// 										JsonObject oldItemJson = oldElement.getAsJsonObject();
// 										if (debug) {
// 											response.getWriter().println("postToCDPos oldItemJson :" + oldItemJson);
// 										}

// 										olditemEntityName = oldItemJson.get("EntityName").getAsString();
// 										oldPayloaderviceName = oldItemJson.get("ServiceName").getAsString();

// 										for(int i=0;i<=newItemsArray.size()-1;i++){
// 											JsonObject newItemJson = newItemsArray.get(i).getAsJsonObject();
// 											if (debug) {
// 												response.getWriter().println("postToCDPos newItemJson :" + newItemJson);
// 											}

// 											newitemEntityName = newItemJson.get("EntityName").getAsString();
// 											newPayloadServiceName = newItemJson.get("ServiceName").getAsString();
// 											newitemEntityName = newitemEntityName.concat("Type");
// 											if(olditemEntityName.equalsIgnoreCase(newItemJson.get("EntityName").getAsString()) && oldPayloaderviceName.equalsIgnoreCase(newPayloadServiceName)){
// 												newitemKeyProperties = getKeyPropertyTag(response, dataSrcUrl,dataSrcUserPass,newPayloadServiceName ,newitemEntityName ,  debug);
// 												validatedkeyObject	= validateKeyProperties(response, oldItemJson, newItemJson, newitemKeyProperties, debug);
// 											}else{
// 												validatedkeyObject.addProperty("Status", "000002");
// 											}

// 											if(validatedkeyObject.get("Status").getAsString().equalsIgnoreCase("000001")){
// 												itemsRecordMismatch = true;
// 												String concatinatedTableKey = "";
// 												for(String key : newitemKeyProperties){
// 													String tableKey = newItemJson.get(key).getAsString();
// 													concatinatedTableKey = concatinatedTableKey.concat(tableKey);
// 												}
// 												if(debug){
// 													response.getWriter().println("concatinatedTableKey " + concatinatedTableKey);
// 												}
// 												compareAndInsertToCDPos(request,response, changeSetHeaders,oldItemJson, newItemJson, pcgwUrl, emailDetails,aggregatorID,flag, newItemJson.get("EntityName").getAsString()  ,  concatinatedObjectValue , concatinatedTableKey ,newitemKeyProperties  , object , changeSetsForUpdate ,debug);
// 												oldItemsMap.put(concatinatedTableKey, oldItemJson);
// 												break;
// 											}else if(i==newItemsArray.size()-1){
// 												itemsRecordMismatch = false ;
// 												if(debug){
// 													response.getWriter().println(validatedkeyObject);
// 												}
// 												olditemEntityName = olditemEntityName.concat("Type");
// 												olditemKeyProperties = getKeyPropertyTag(response, dataSrcUrl,dataSrcUserPass,oldPayloaderviceName ,olditemEntityName ,  debug);

// 												JsonObject validatedkeyObjOfOldPayload = validateKeyProperties(response, oldItemJson, null, olditemKeyProperties, debug);
// 												if(!itemsRecordMismatch && validatedkeyObjOfOldPayload.get("Status").getAsString().equalsIgnoreCase("000001")){
// 													String concatinatedTableKey = "";
// 													for(String key : olditemKeyProperties){
// 														String tableKey = oldItemJson.get(key).getAsString();
// 														concatinatedTableKey = concatinatedTableKey.concat(tableKey);
// 													}
// 													oldItemsMap.put(concatinatedTableKey, oldItemJson);
// 													if(debug){
// 														response.getWriter().println("concatinatedTableKey " + concatinatedTableKey);
// 													}
// 													insertPayLoad= new JsonObject();

// 													insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 													insertPayLoad.addProperty("ObjectClass", object);
// 													if (concatinatedObjectValue != null && objectValueMaxLength != null && !objectValueMaxLength.isEmpty()) {
// 														maxLength = 0;
// 														try {
// 															maxLength = Integer.parseInt(objectValueMaxLength);
// 															if(concatinatedObjectValue.length()>=maxLength){
// 																insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 															}else{
// 																insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 															}
// 														} catch (NumberFormatException e) {
// 															// Handle the exception or log an error message
// 															response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 														}
// 													}
// 													insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 													insertPayLoad.addProperty("TableName", oldItemJson.get("EntityName").getAsString());
// 													maxLength = 0;
// 													try {
// 														maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 														if(concatinatedTableKey.length()>=maxLength){
// 															insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 														}else{
// 															insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey);
// 														}
// 													} catch (NumberFormatException e) {
// 														// Handle the exception or log an error message
// 														response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 													}
// 													insertPayLoad.addProperty("FieldName", "KEY");
// 													insertPayLoad.addProperty("ChangeInd", "D");
// 													insertPayLoad.addProperty("TextChange", "");
// 													insertPayLoad.addProperty("OldUnit", "");
// 													insertPayLoad.addProperty("NewUnit", "");
// 													insertPayLoad.addProperty("OldCurrency", "");
// 													insertPayLoad.addProperty("NewCurrency", "");
// 													insertPayLoad.addProperty("NewValue", "");
// 													insertPayLoad.addProperty("CreatedBy", createdBy);
// 													insertPayLoad.addProperty("CreatedAt", createdAt);
// 													insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 													insertPayLoad.addProperty("Source", src);
// 													insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 													if(debug){
// 														response.getWriter().println("postToCDPos newPayload  for missing old item: " + insertPayLoad);
// 													}
// 													executeURL = pcgwUrl+"ChangeDocumentItems";

// 													BatchChangeSetPart changeRequests = BatchChangeSetPart.method("POST").uri(executeURL)
// 															.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 													changeSetsForUpdate.add(changeRequests);
// 													deletionIndicatorEnabled = true;
// 												}
// 											}
// 										}
// 									}
// 									createItemsMapForNewItems(response, changeSetHeaders,dataSrcUrl, dataSrcUserPass, pcgwUrl,aggregatorID,object,concatinatedObjectValue,newItemsArray, oldItemsMap, changeSetsForUpdate ,debug);
// 								}
// 			}else if (flag.equalsIgnoreCase("I")) {
// 				if (debug) {
// 					response.getWriter().println("postToCDPos flag: " + flag);
// 				}
// 				JsonObject insertPayLoad= new JsonObject();

// 				insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 				insertPayLoad.addProperty("ObjectClass", object);
// 				if (concatinatedObjectValue != null && objectValueMaxLength != null && !objectValueMaxLength.isEmpty()) {
// 					int maxLength = 0;
// 					try {
// 						maxLength = Integer.parseInt(objectValueMaxLength);
// 						if(concatinatedObjectValue.length()>=maxLength){
// 							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 						}else{
// 							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 						}
// 					} catch (NumberFormatException e) {
// 						// Handle the exception or log an error message
// 						response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 					}
// 				}
// 				insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 				insertPayLoad.addProperty("TableName", entityName);
// 				int maxLength = 0;
// 				try {
// 					maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 					if(concatinatedObjectValue.length()>=maxLength){
// 						insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 					}else{
// 						insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 					}
// 				} catch (NumberFormatException e) {
// 					// Handle the exception or log an error message
// 					response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 				}
// 				insertPayLoad.addProperty("FieldName", "KEY");
// 				insertPayLoad.addProperty("ChangeInd", flag);
// 				insertPayLoad.addProperty("TextChange", "");
// 				insertPayLoad.addProperty("OldUnit", "");
// 				insertPayLoad.addProperty("NewUnit", "");
// 				insertPayLoad.addProperty("OldCurrency", "");
// 				insertPayLoad.addProperty("NewCurrency", "");
// 				insertPayLoad.addProperty("NewValue", "");
// 				insertPayLoad.addProperty("CreatedBy", createdBy);
// 				insertPayLoad.addProperty("CreatedAt", createdAt);
// 				insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 				insertPayLoad.addProperty("Source", src);
// 				insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 				if(debug){
// 					response.getWriter().println("postToCDPos newPayload  for header: " + insertPayLoad);
// 				}
// 				executeURL = pcgwUrl+"ChangeDocumentItems";

// 				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
// 						.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				changeSetforInsert.add(changeRequest);

// 				if (newPayloads.has("Items") && newPayloads.get("Items").isJsonArray()) {
// 					// Get the Items array
// 					JsonArray itemsArray = newPayloads.get("Items").getAsJsonArray();
// 					String itemKeyProperty[] = null;

// 					for (JsonElement itemElement : itemsArray) {
// 						JsonObject newPayload = itemElement.getAsJsonObject(); 

// 						if (debug) {
// 							response.getWriter().println("postToCDPos newPayload: " + newPayload);
// 						}

// 						String itemEntityName = newPayload.get("EntityName").getAsString();
// 						String serviceName = newPayload.get("ServiceName").getAsString();
// 						itemEntityName = itemEntityName.concat("Type");
// 						itemKeyProperty = getKeyPropertyTag(response, dataSrcUrl,dataSrcUserPass,serviceName ,itemEntityName ,  debug);

// 						JsonObject validatedItemskeyObject = new JsonObject();

// 						validatedItemskeyObject = validateKeyProperties(response, null, newPayload, itemKeyProperty, debug);

// 						String concatinatedTableKey = "";
// 						for(String key : itemKeyProperty){
// 							String keyValue = newPayload.get(key).getAsString();
// 							concatinatedTableKey = concatinatedTableKey.concat(keyValue);
// 						}
// 						if(debug){
// 							response.getWriter().println("concatinatedTableKey " + concatinatedTableKey);
// 						}

// 						if(validatedItemskeyObject.get("Status").getAsString().equalsIgnoreCase("000001")){
// 							JsonObject insertToCdpos = new JsonObject();

// 							insertToCdpos.addProperty("AggregatorID", aggregatorID);
// 							insertToCdpos.addProperty("ObjectClass", object);
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertToCdpos.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertToCdpos.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}
// 							insertToCdpos.addProperty("DocChangeNo", docHangeNo);
// 							insertToCdpos.addProperty("TableName", newPayload.get("EntityName").getAsString());

// 							try {
// 								maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 								if(concatinatedTableKey.length()>=maxLength){
// 									insertToCdpos.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 								}else{
// 									insertToCdpos.addProperty("ChangedTablekey",concatinatedTableKey);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 							}
// 							insertToCdpos.addProperty("FieldName", "KEY");
// 							insertToCdpos.addProperty("ChangeInd", flag);
// 							insertToCdpos.addProperty("TextChange", "");
// 							insertToCdpos.addProperty("OldUnit", "");
// 							insertToCdpos.addProperty("NewUnit", "");
// 							insertToCdpos.addProperty("OldCurrency", "");
// 							insertToCdpos.addProperty("NewCurrency", "");
// 							insertToCdpos.addProperty("NewValue", "");
// 							insertToCdpos.addProperty("CreatedBy", createdBy);
// 							insertToCdpos.addProperty("CreatedAt", createdAt);
// 							insertToCdpos.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 							insertToCdpos.addProperty("Source", src);
// 							insertToCdpos.addProperty("SourceReferenceID", srcRefId);

// 							if (debug) {
// 								response.getWriter().println("postToCDPos newPayload executeURL: " + executeURL);
// 							}

// 							BatchChangeSetPart changeRequestItem= BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertToCdpos.toString()).build();
// 							changeSetforInsert.add(changeRequestItem);
// 						}else{

// 						}
// 					}
// 				}
// 			}else if (flag.equalsIgnoreCase("D")){
// 				if (debug) {
// 					response.getWriter().println("postToCDPos flag: " + flag);
// 				}
// 				JsonObject insertPayLoad= new JsonObject();

// 				insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 				insertPayLoad.addProperty("ObjectClass", object);
// 				if (concatinatedObjectValue != null && objectValueMaxLength != null && !objectValueMaxLength.isEmpty()) {
// 					int maxLength = 0;
// 					try {
// 						maxLength = Integer.parseInt(objectValueMaxLength);
// 						if(concatinatedObjectValue.length()>=maxLength){
// 							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 						}else{
// 							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 						}
// 					} catch (NumberFormatException e) {
// 						// Handle the exception or log an error message
// 						response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 					}
// 				}
// 				insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 				insertPayLoad.addProperty("TableName", entityName);
// 				int maxLength = 0;
// 				try {
// 					maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 					if(concatinatedObjectValue.length()>=maxLength){
// 						insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 					}else{
// 						insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 					}
// 				} catch (NumberFormatException e) {
// 					// Handle the exception or log an error message
// 					response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 				}
// 				insertPayLoad.addProperty("FieldName", "KEY");
// 				insertPayLoad.addProperty("ChangeInd", flag);
// 				insertPayLoad.addProperty("TextChange", "");
// 				insertPayLoad.addProperty("OldUnit", "");
// 				insertPayLoad.addProperty("NewUnit", "");
// 				insertPayLoad.addProperty("OldCurrency", "");
// 				insertPayLoad.addProperty("NewCurrency", "");
// 				insertPayLoad.addProperty("NewValue", "");
// 				insertPayLoad.addProperty("CreatedBy", createdBy);
// 				insertPayLoad.addProperty("CreatedAt", createdAt);
// 				insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 				insertPayLoad.addProperty("Source", src);
// 				insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 				if(debug){
// 					response.getWriter().println("postToCDPos newPayload  for header: " + insertPayLoad);
// 				}
// 				executeURL = pcgwUrl+"ChangeDocumentItems";

// 				BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
// 						.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				changeSetsForDelete.add(changeRequest);

// 				if (oldPayloads.has("Items") && oldPayloads.get("Items").isJsonArray()) {
// 					// Get the Items array
// 					JsonArray itemsArray = oldPayloads.get("Items").getAsJsonArray();
// 					String itemKeyProperty[] = null;
// 					for (JsonElement itemElement : itemsArray) {
// 						JsonObject oldPayloadItem = itemElement.getAsJsonObject(); 

// 						if (debug) {
// 							response.getWriter().println("postToCDPos oldPayloadItem: " + oldPayloadItem);
// 						}

// 						String itemEntityName = oldPayloadItem.get("EntityName").getAsString();
// 						String serviceName = oldPayloadItem.get("ServiceName").getAsString();
// 						itemEntityName = itemEntityName.concat("Type");
// 						itemKeyProperty = getKeyPropertyTag(response, dataSrcUrl,dataSrcUserPass,serviceName ,itemEntityName ,  debug);

// 						JsonObject validatedItemskeyObject = new JsonObject();

// 						validatedItemskeyObject = validateKeyProperties(response, null, oldPayloadItem, itemKeyProperty, debug);

// 						String concatinatedTableKey = "";
// 						for(String key : itemKeyProperty){
// 							String keyValue = oldPayloadItem.get(key).getAsString();
// 							concatinatedTableKey = concatinatedTableKey.concat(keyValue);
// 						}
// 						if(debug){
// 							response.getWriter().println("concatinatedTableKey " + concatinatedTableKey);
// 						}

// 						if(validatedItemskeyObject.get("Status").getAsString().equalsIgnoreCase("000001")){
// 							JsonObject insertToCdpos = new JsonObject();

// 							insertToCdpos.addProperty("AggregatorID", aggregatorID);
// 							insertToCdpos.addProperty("ObjectClass", object);
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertToCdpos.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertToCdpos.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								// Handle the exception or log an error message
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}

// 							insertToCdpos.addProperty("DocChangeNo", docHangeNo);
// 							insertToCdpos.addProperty("TableName", oldPayloadItem.get("EntityName").getAsString());

// 							try {
// 								maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 								if(concatinatedTableKey.length()>=maxLength){
// 									insertToCdpos.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 								}else{
// 									insertToCdpos.addProperty("ChangedTablekey",concatinatedTableKey);
// 								}
// 							} catch (NumberFormatException e) {
// 								// Handle the exception or log an error message
// 								response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 							}

// 							insertToCdpos.addProperty("FieldName", "KEY");
// 							insertToCdpos.addProperty("ChangeInd", flag);
// 							insertToCdpos.addProperty("TextChange", "");
// 							insertToCdpos.addProperty("OldUnit", "");
// 							insertToCdpos.addProperty("NewUnit", "");
// 							insertToCdpos.addProperty("OldCurrency", "");
// 							insertToCdpos.addProperty("NewCurrency", "");
// 							insertToCdpos.addProperty("NewValue", "");
// 							insertToCdpos.addProperty("CreatedBy", createdBy);
// 							insertToCdpos.addProperty("CreatedAt", createdAt);
// 							insertToCdpos.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 							insertToCdpos.addProperty("Source", src);
// 							insertToCdpos.addProperty("SourceReferenceID", srcRefId);

// 							if (debug) {
// 								response.getWriter().println("postToCDPos newPayload executeURL: " + executeURL);
// 							}

// 							executeURL = pcgwUrl+"ChangeDocumentItems";
// 							BatchChangeSetPart changeRequestItem= BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertToCdpos.toString()).build();
// 							changeSetsForDelete.add(changeRequestItem);
// 						}else{
// 							if(debug){
// 								response.getWriter().println("validatedItemskeyObject "+validatedItemskeyObject);
// 							}
// 						}
// 					}
// 				}
// 			}
// 		} catch (Exception ex) {
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "postToCDPos()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		}

// 		if (flag.equalsIgnoreCase("I") ){
// 			return changeSetforInsert;
// 		}else if(flag.equalsIgnoreCase("U")){
// 			return changeSetsForUpdate;
// 		}else{
// 			return changeSetsForDelete;
// 		}
// 	}

// 	private static BatchChangeSet createItemsMapForNewItems(HttpServletResponse response , Map<String, String> changeSetHeaders,String dataSrcUrl ,String dataSrcUserPass, String pcgwUrl ,String aggregatorID ,String object,String concatinatedObjectValue,JsonArray itemsArray , Map<String, JsonObject> oldItemsMap , BatchChangeSet changeSetsForUpdate ,boolean debug) throws IOException {
// 		String olditemKeyProperties [] = null;
// 		String olditemEntityName = "";
// 		String newitemKeyProperties [] = null;
// 		JsonObject validatedkeyObject = new JsonObject();
// 		String newitemEntityName = "" ,newPayloadServiceName ="" ,oldPayloaderviceName="";
// 		Map<String, JsonObject> newItemsMap = new HashMap<>();
// 		//		BatchChangeSet changeSetsForUpdate =  BatchChangeSet.newBuilder().build();
// 		try{
// 			//			Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 			//			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 			//			changeSetHeaders.put("Accept", APPLICATION_JSON);
// 			//			changeSetHeaders.put(AUTHORIZATION_HEADER,
// 			//					"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));
// 			for (JsonElement element : itemsArray) {
// 				JsonObject oldItemJson = element.getAsJsonObject();
// 				olditemEntityName = oldItemJson.get("EntityName").getAsString();
// 				oldPayloaderviceName = oldItemJson.get("ServiceName").getAsString();
// 				olditemEntityName = olditemEntityName.concat("Type");
// 				olditemKeyProperties = getKeyPropertyTag(response, dataSrcUrl,dataSrcUserPass,oldPayloaderviceName ,olditemEntityName ,  debug);

// 				JsonObject validatedkeyObjOfOldPayload = validateKeyProperties(response, null,oldItemJson , olditemKeyProperties, debug);

// 				if(validatedkeyObjOfOldPayload.get("Status").getAsString().equalsIgnoreCase("000001")){
// 					String concatinatedTableKey = "";
// 					for(String key : olditemKeyProperties){
// 						String keyValue = oldItemJson.get(key).getAsString();
// 						concatinatedTableKey = concatinatedTableKey.concat(keyValue);
// 					}
// 					newItemsMap.put(concatinatedTableKey, oldItemJson);
// 					if(debug){
// 						response.getWriter().println("createItemsMapForNewItems itemsMap :" + newItemsMap);
// 					}

// 					if (newItemsMap.containsKey(concatinatedTableKey) && oldItemsMap!=null && !oldItemsMap.containsKey(concatinatedTableKey) ) {
// 						JsonObject newItem = newItemsMap.get(concatinatedTableKey);
// 						JsonObject insertPayLoad= new JsonObject();

// 						insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 						insertPayLoad.addProperty("ObjectClass", object);
// 						if (concatinatedObjectValue != null && objectValueMaxLength != null && !objectValueMaxLength.isEmpty()) {
// 							int maxLength = 0;
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								// Handle the exception or log an error message
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}
// 						}
// 						insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 						insertPayLoad.addProperty("TableName", newItem.get("EntityName").getAsString());
// 						int maxLength = 0;
// 						try {
// 							maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 							if(concatinatedTableKey.length()>=maxLength){
// 								insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 							}else{
// 								insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey);
// 							}
// 						} catch (NumberFormatException e) {
// 							// Handle the exception or log an error message
// 							response.getWriter().println("Invalid number format for maxLengthOfChangTabKey: " + maxLengthOfChangTabKey);
// 						}
// 						insertPayLoad.addProperty("FieldName", "KEY");
// 						insertPayLoad.addProperty("ChangeInd", "I");
// 						insertPayLoad.addProperty("TextChange", "");
// 						insertPayLoad.addProperty("OldUnit", "");
// 						insertPayLoad.addProperty("NewUnit", "");
// 						insertPayLoad.addProperty("OldCurrency", "");
// 						insertPayLoad.addProperty("NewCurrency", "");
// 						insertPayLoad.addProperty("NewValue", "");
// 						insertPayLoad.addProperty("CreatedBy", createdBy);
// 						insertPayLoad.addProperty("CreatedAt", createdAt);
// 						insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 						insertPayLoad.addProperty("Source", src);
// 						insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 						if(debug){
// 							response.getWriter().println("postToCDPos newPayload  for missing old item: " + insertPayLoad);
// 						}
// 						String executeURL = pcgwUrl+"ChangeDocumentItems";

// 						BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
// 								.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 						changeSetsForUpdate.add(changeRequest);

// 					}
// 				}
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "postToCDPos()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		}
// 		return changeSetsForUpdate;
// 	}

// 	private BatchChangeSet postHeadersToCdPos(HttpServletRequest request , HttpServletResponse response ,  Map<String, String> changeSetHeaders,String pcgwUrl ,JsonObject oldPayloadsHdr ,JsonObject newPayloadsHdr  ,String concatinatedObjectValue ,String aggregatorID, String objectClass ,String entityName ,boolean debug) throws IOException{
// 		BatchChangeSet changeSetsForUpdate = BatchChangeSet.newBuilder().build();
// 		try{
// 			String executeURL = pcgwUrl+"ChangeDocumentItems";
// 			//			Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 			//			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 			//			changeSetHeaders.put("Accept", APPLICATION_JSON);
// 			//			changeSetHeaders.put(AUTHORIZATION_HEADER,"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));

// 			if(oldPayloadsHdr!=null ){
// 				oldPayloadsHdr.remove("Items");
// 			}
// 			if(newPayloadsHdr!=null ){
// 				newPayloadsHdr.remove("Items");
// 			}

// 			if (debug) {
// 				response.getWriter().println("postToCDPos oldPayload removed Items:" + oldPayloadsHdr);
// 				response.getWriter().println("postToCDPos newPayload removedItems:" + newPayloadsHdr);
// 				response.getWriter().println("concatinatedObjectValue " + concatinatedObjectValue);
// 			}

// 			JsonObject insertPayLoad =  new JsonObject();
// 			for (Map.Entry<String, JsonElement> entry : oldPayloadsHdr.entrySet()) {
// 				String key = entry.getKey();
// 				if(oldPayloadsHdr.has(key) && newPayloadsHdr.has(key) ){
// 					JsonElement oldValue = entry.getValue();
// 					JsonElement newValue = newPayloadsHdr.get(key);

// 					if (oldValue != null && !oldValue.isJsonNull() && newValue != null && !newValue.isJsonNull()) {

// 						headerRecordMismatch = true;
// 						if (!oldValue.toString().equalsIgnoreCase(newValue.toString())) {
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old Value: " + oldValue);
// 								response.getWriter().println("New Value: " + newValue);
// 								response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 							}

// 							insertPayLoad = formPayloadForHeaders(response, aggregatorID, objectClass, concatinatedObjectValue, entityName, key, debug);
// 							insertPayLoad.addProperty("NewValue", newValue.toString().substring(1,newValue.toString().length()-1));
// 							insertPayLoad.addProperty("OldValue",  oldValue.toString().substring(1,oldValue.toString().length()-1));

// 							BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequestForField);

// 							if(debug){
// 								response.getWriter().println("insertPayLoad for changed field: " + insertPayLoad);
// 							}
// 						}
// 					}else if( (oldValue.isJsonNull() || oldValue == null) && newValue != null && !newValue.isJsonNull()){
// 						if (newValue.toString().length()>=0) {
// 							headerRecordMismatch = true;
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old Value: " + oldValue.getAsString());
// 								response.getWriter().println("New Value: " + newValue.getAsString());
// 								response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 							}

// 							insertPayLoad = formPayloadForHeaders(response, aggregatorID, objectClass, concatinatedObjectValue, entityName, key, debug);
// 							insertPayLoad.addProperty("NewValue", newValue.toString().substring(1,newValue.toString().length()-1));
// 							insertPayLoad.addProperty("OldValue", JsonNull.INSTANCE.toString());

// 							BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequestForField);
// 						}
// 					}else if((newValue == null || newValue.isJsonNull())  && oldValue != null && !oldValue.isJsonNull()){
// 						if (oldValue.toString().length()>=0) {
// 							headerRecordMismatch = true;
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old Value: " + oldValue.getAsString());
// 								response.getWriter().println("New Value: " + newValue.getAsString());
// 								response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 							}

// 							insertPayLoad = formPayloadForHeaders(response, aggregatorID, objectClass, concatinatedObjectValue, entityName, key, debug);
// 							insertPayLoad.addProperty("NewValue", JsonNull.INSTANCE.toString());
// 							insertPayLoad.addProperty("OldValue",  oldValue.toString().substring(1,oldValue.toString().length()-1));

// 							BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequestForField);
// 						}
// 					}
// 				}

// 				// ----------------- if old payload header has key and new payload header is not has key still that scenerio didnt occur ---//

// 				//				else if(oldPayloadsHdr.has(key) && !newPayloadsHdr.has(key)){
// 				//					JsonElement oldValue = entry.getValue();
// 				//
// 				//					//					if (oldValue != null && !oldValue.isJsonNull()) {
// 				//					insertPayLoad =  new JsonObject();
// 				//					headerRecordMismatch = true;
// 				//					if(debug){
// 				//						response.getWriter().println("Field: " + key);
// 				//						response.getWriter().println("Old Value: " + oldValue);
// 				//						response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 				//					}
// 				//
// 				//					insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 				//					insertPayLoad.addProperty("ObjectClass", object);
// 				//					int maxLength =0;
// 				//					try {
// 				//						maxLength = Integer.parseInt(objectValueMaxLength);
// 				//						if(concatinatedObjectValue.length()>=maxLength){
// 				//							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 				//						}else{
// 				//							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 				//						}
// 				//					} catch (NumberFormatException e) {
// 				//						// Handle the exception or log an error message
// 				//						response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 				//					}
// 				//					insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 				//					insertPayLoad.addProperty("TableName", entityName);
// 				//
// 				//					try {
// 				//						maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 				//						if(concatinatedObjectValue.length()>=maxLength){
// 				//							insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 				//						}else{
// 				//							insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 				//						}
// 				//					} catch (NumberFormatException e) {
// 				//						// Handle the exception or log an error message
// 				//						response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 				//					}
// 				//
// 				//					insertPayLoad.addProperty("FieldName", key);
// 				//					insertPayLoad.addProperty("ChangeInd", "D");
// 				//					insertPayLoad.addProperty("TextChange", "");
// 				//					insertPayLoad.addProperty("OldUnit", "");
// 				//					insertPayLoad.addProperty("NewUnit", "");
// 				//					insertPayLoad.addProperty("OldCurrency", "");
// 				//					insertPayLoad.addProperty("NewCurrency", "");
// 				//					insertPayLoad.addProperty("NewValue", "");
// 				//					insertPayLoad.addProperty("OldValue",  "");
// 				//					insertPayLoad.addProperty("CreatedBy", createdBy);
// 				//					insertPayLoad.addProperty("CreatedAt", createdAt);
// 				//					insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 				//					insertPayLoad.addProperty("Source", src);
// 				//					insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 				//
// 				//					String executeURL = pcgwUrl+"ChangeDocumentItems";
// 				//
// 				//					BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 				//							.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 				//					changeSetsForUpdate.add(changeRequestForField);
// 				//
// 				//					if(debug){
// 				//						response.getWriter().println("insertPayLoad for changed field: " + insertPayLoad);
// 				//					}
// 				//					//					}
// 				//				} 
// 			}



// 			// ----------------- if old payload header not has key and new payload header is has key still that scenerio didnt occur ---//

// 			//			for (Map.Entry<String, JsonElement> entry : newPayloadsHdr.entrySet()) {
// 			//				String key = entry.getKey();
// 			//
// 			//				if(!oldPayloadsHdr.has(key) && newPayloadsHdr.has(key)){
// 			//					JsonElement newValue = newPayloadsHdr.get(key);
// 			//
// 			//					//					if (newValue != null && !newValue.isJsonNull()) {
// 			//					insertPayLoad =  new JsonObject();
// 			//					headerRecordMismatch = true;
// 			//					if(debug){
// 			//						response.getWriter().println("Field: " + key);
// 			//						response.getWriter().println(" newValue: " + newValue);
// 			//						response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 			//					}
// 			//
// 			//					insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 			//					insertPayLoad.addProperty("ObjectClass", object);
// 			//					int maxLength =0;
// 			//					try {
// 			//						maxLength = Integer.parseInt(objectValueMaxLength);
// 			//						if(concatinatedObjectValue.length()>=maxLength){
// 			//							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 			//						}else{
// 			//							insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 			//						}
// 			//					} catch (NumberFormatException e) {
// 			//						// Handle the exception or log an error message
// 			//						response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 			//					}
// 			//					insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 			//					insertPayLoad.addProperty("TableName", entityName);
// 			//
// 			//					try {
// 			//						maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 			//						if(concatinatedObjectValue.length()>=maxLength){
// 			//							insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 			//						}else{
// 			//							insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 			//						}
// 			//					} catch (NumberFormatException e) {
// 			//						// Handle the exception or log an error message
// 			//						response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 			//					}
// 			//
// 			//					insertPayLoad.addProperty("FieldName", key);
// 			//					insertPayLoad.addProperty("ChangeInd", "I");
// 			//					insertPayLoad.addProperty("TextChange", "");
// 			//					insertPayLoad.addProperty("OldUnit", "");
// 			//					insertPayLoad.addProperty("NewUnit", "");
// 			//					insertPayLoad.addProperty("OldCurrency", "");
// 			//					insertPayLoad.addProperty("NewCurrency", "");
// 			//					insertPayLoad.addProperty("NewValue", "");
// 			//					insertPayLoad.addProperty("OldValue",  "");
// 			//					insertPayLoad.addProperty("CreatedBy", createdBy);
// 			//					insertPayLoad.addProperty("CreatedAt", createdAt);
// 			//					insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 			//					insertPayLoad.addProperty("Source", src);
// 			//					insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 			//
// 			//					String executeURL = pcgwUrl+"ChangeDocumentItems";
// 			//
// 			//					BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 			//							.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 			//					changeSetsForUpdate.add(changeRequestForField);
// 			//
// 			//					if(debug){
// 			//						response.getWriter().println("insertPayLoad for changed field: " + insertPayLoad);
// 			//					}
// 			//					//					}
// 			//				}
// 			//			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "postHeadersToCdPos()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		}
// 		return changeSetsForUpdate;
// 	}

// 	private JsonObject formPayloadForHeaders(HttpServletResponse response ,String aggregatorID ,String objectClass,String concatinatedObjectValue ,String entityName,String key, boolean debug) throws IOException{

// 		JsonObject insertPayLoad =  new JsonObject();
// 		try{
// 			insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 			insertPayLoad.addProperty("ObjectClass", objectClass);
// 			int maxLength =0;
// 			try {
// 				maxLength = Integer.parseInt(objectValueMaxLength);
// 				if(concatinatedObjectValue.length()>=maxLength){
// 					insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 				}else{
// 					insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 				}
// 			} catch (NumberFormatException e) {
// 				// Handle the exception or log an error message
// 				response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 			}
// 			insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 			insertPayLoad.addProperty("TableName", entityName);

// 			try {
// 				maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 				if(concatinatedObjectValue.length()>=maxLength){
// 					insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 				}else{
// 					insertPayLoad.addProperty("ChangedTablekey",concatinatedObjectValue);
// 				}
// 			} catch (NumberFormatException e) {
// 				// Handle the exception or log an error message
// 				response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 			}

// 			insertPayLoad.addProperty("FieldName", key);
// 			insertPayLoad.addProperty("ChangeInd", "U");
// 			insertPayLoad.addProperty("TextChange", "");
// 			insertPayLoad.addProperty("OldUnit", "");
// 			insertPayLoad.addProperty("NewUnit", "");
// 			insertPayLoad.addProperty("OldCurrency", "");
// 			insertPayLoad.addProperty("NewCurrency", "");
// 			insertPayLoad.addProperty("CreatedBy", createdBy);
// 			insertPayLoad.addProperty("CreatedAt", createdAt);
// 			insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 			insertPayLoad.addProperty("Source", src);
// 			insertPayLoad.addProperty("SourceReferenceID", srcRefId);
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "formPayloadForHeaders()");
// 			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		}
// 		return insertPayLoad;
// 	}

// 	private  BatchChangeSet compareAndInsertToCDPos(HttpServletRequest request,HttpServletResponse response , Map<String, String> changeSetHeaders,JsonObject oldItem, JsonObject newItem ,String pcgwUrl, JsonObject emailDetails,String aggregatorID, String flag, String entityName , String concatinatedObjectValue , String concatinatedTableKey,String[] keyProperties  ,String object , BatchChangeSet changeSetsForUpdate , boolean debug) throws IOException {
// 		String executeURL = "" ; 
// 		try{

// 			//			Map<String, String> changeSetHeaders = new HashMap<String, String>();
// 			//			changeSetHeaders.put(HTTP_HEADER_CONTENT_TYPE, "application/json");
// 			//			changeSetHeaders.put("Accept", APPLICATION_JSON);
// 			//			changeSetHeaders.put(AUTHORIZATION_HEADER,
// 			//					"Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));

// 			JsonObject insertPayLoad = new JsonObject();
// 			for (Map.Entry<String, JsonElement> entry : oldItem.entrySet()) {
// 				String key = entry.getKey();

// 				if(oldItem.has(key) && newItem.has(key) ){
// 					JsonElement oldValue = entry.getValue();
// 					JsonElement newValue = newItem.get(key);
// 					if(debug){
// 						response.getWriter().println("compareJsonObjects key :"+key);
// 						response.getWriter().println("compareJsonObjects oldValue :"+oldValue.toString());
// 						response.getWriter().println("compareJsonObjects newValue :"+newValue.toString());
// 					}

// 					if (oldValue != null && !oldValue.isJsonNull() && newValue != null && !newValue.isJsonNull()) {
// 						if (!oldValue.toString().equalsIgnoreCase(newValue.toString())) {
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old value: " + oldValue);
// 								response.getWriter().println("New value: " + newValue);
// 								response.getWriter().println();
// 							}
// 							insertPayLoad = new JsonObject();
// 							insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 							insertPayLoad.addProperty("ObjectClass", object);
// 							int maxLength =0;
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								// Handle the exception or log an error message
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}
// 							insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 							insertPayLoad.addProperty("TableName", entityName);

// 							try {
// 								maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 								if(concatinatedTableKey.length()>=maxLength){
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 								}else{
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey);
// 								}
// 							} catch (NumberFormatException e) {
// 								// Handle the exception or log an error message
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 							}
// 							insertPayLoad.addProperty("FieldName", key);
// 							insertPayLoad.addProperty("ChangeInd", flag);
// 							insertPayLoad.addProperty("TextChange", "");
// 							insertPayLoad.addProperty("OldUnit", "");
// 							insertPayLoad.addProperty("NewUnit", "");
// 							insertPayLoad.addProperty("OldCurrency", "");
// 							insertPayLoad.addProperty("NewCurrency", "");
// 							insertPayLoad.addProperty("NewValue", newValue.toString().substring(1,newValue.toString().length()-1));
// 							insertPayLoad.addProperty("OldValue", oldValue.toString().substring(1,oldValue.toString().length()-1));
// 							insertPayLoad.addProperty("CreatedBy", createdBy);
// 							insertPayLoad.addProperty("CreatedAt", createdAt);
// 							insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 							insertPayLoad.addProperty("Source", src);
// 							insertPayLoad.addProperty("SourceReferenceID", srcRefId);

// 							executeURL = pcgwUrl+"ChangeDocumentItems";
// 							BatchChangeSetPart changeRequest = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequest);
// 						}
// 					}else if((oldValue.isJsonNull() || oldValue == null) && newValue != null && !newValue.isJsonNull()){
// 						if (newValue.toString().length()>=0) {
// 							insertPayLoad = new JsonObject();
// 							headerRecordMismatch = true;
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old Value: " + oldValue.getAsString());
// 								response.getWriter().println("New Value: " + newValue.getAsString());
// 								response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 							}

// 							insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 							insertPayLoad.addProperty("ObjectClass", object);
// 							int maxLength =0;
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}
// 							insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 							insertPayLoad.addProperty("TableName", entityName);

// 							try {
// 								maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 								if(concatinatedTableKey.length()>=maxLength){
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 								}else{
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 							}

// 							insertPayLoad.addProperty("FieldName", key);
// 							insertPayLoad.addProperty("ChangeInd", "U");
// 							insertPayLoad.addProperty("TextChange", "");
// 							insertPayLoad.addProperty("OldUnit", "");
// 							insertPayLoad.addProperty("NewUnit", "");
// 							insertPayLoad.addProperty("OldCurrency", "");
// 							insertPayLoad.addProperty("NewCurrency", "");
// 							insertPayLoad.addProperty("NewValue", newValue.toString().substring(1,newValue.toString().length()-1));
// 							insertPayLoad.addProperty("OldValue", JsonNull.INSTANCE.toString());
// 							insertPayLoad.addProperty("CreatedBy", createdBy);
// 							insertPayLoad.addProperty("CreatedAt", createdAt);
// 							insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 							insertPayLoad.addProperty("Source", src);
// 							insertPayLoad.addProperty("SourceReferenceID", srcRefId);

// 							executeURL = pcgwUrl+"ChangeDocumentItems";

// 							BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequestForField);
// 						}
// 					}else if((newValue == null || newValue.isJsonNull())  && oldValue != null && !oldValue.isJsonNull()){
// 						if (oldValue.toString().length()>=0) {
// 							insertPayLoad = new JsonObject();
// 							headerRecordMismatch = true;
// 							if(debug){
// 								response.getWriter().println("Field: " + key);
// 								response.getWriter().println("Old Value: " + oldValue.getAsString());
// 								response.getWriter().println("New Value: " + newValue.getAsString());
// 								response.getWriter().println("concatinatedObjectValue "+concatinatedObjectValue);
// 							}

// 							insertPayLoad.addProperty("AggregatorID", aggregatorID);
// 							insertPayLoad.addProperty("ObjectClass", object);
// 							int maxLength =0;
// 							try {
// 								maxLength = Integer.parseInt(objectValueMaxLength);
// 								if(concatinatedObjectValue.length()>=maxLength){
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue.substring(0, Integer.parseInt(objectValueMaxLength)-1));
// 								}else{
// 									insertPayLoad.addProperty("ObjectValue",concatinatedObjectValue);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + objectValueMaxLength);
// 							}
// 							insertPayLoad.addProperty("DocChangeNo", docHangeNo);
// 							insertPayLoad.addProperty("TableName", entityName);

// 							try {
// 								maxLength = Integer.parseInt(maxLengthOfChangTabKey);
// 								if(concatinatedTableKey.length()>=maxLength){
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey.substring(0, Integer.parseInt(maxLengthOfChangTabKey)-1));
// 								}else{
// 									insertPayLoad.addProperty("ChangedTablekey",concatinatedTableKey);
// 								}
// 							} catch (NumberFormatException e) {
// 								response.getWriter().println("Invalid number format for objectValueMaxLength: " + maxLengthOfChangTabKey);
// 							}

// 							insertPayLoad.addProperty("FieldName", key);
// 							insertPayLoad.addProperty("ChangeInd", "U");
// 							insertPayLoad.addProperty("TextChange", "");
// 							insertPayLoad.addProperty("OldUnit", "");
// 							insertPayLoad.addProperty("NewUnit", "");
// 							insertPayLoad.addProperty("OldCurrency", "");
// 							insertPayLoad.addProperty("NewCurrency", "");
// 							insertPayLoad.addProperty("NewValue", JsonNull.INSTANCE.toString());
// 							insertPayLoad.addProperty("OldValue", oldValue.toString().substring(1,oldValue.toString().length()-1));
// 							insertPayLoad.addProperty("CreatedBy", createdBy);
// 							insertPayLoad.addProperty("CreatedAt", createdAt);
// 							insertPayLoad.addProperty("CreatedOn", "/Date("+createdOnInMillis+")/");
// 							insertPayLoad.addProperty("Source", src);
// 							insertPayLoad.addProperty("SourceReferenceID", srcRefId);

// 							executeURL = pcgwUrl+"ChangeDocumentItems";

// 							BatchChangeSetPart changeRequestForField = BatchChangeSetPart.method("POST").uri(executeURL)
// 									.headers(changeSetHeaders).body(insertPayLoad.toString()).build();
// 							changeSetsForUpdate.add(changeRequestForField);
// 						}
// 					}
// 				}
// 			}
// 		}catch(Exception ex){
// 			JsonObject result = new JsonObject();
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Method", "compareAndInsertToCDPos()");
// 			result.addProperty("Message",ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		}
// 		return changeSetsForUpdate;
// 	}


// 	public static String generateGUID(int fieldLength) {
// 		String guid = "";
// 		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
// 		if (fieldLength == 32) {
// 			guid = UUID.randomUUID().toString().replace("-", "");
// 		} else if (fieldLength == 36) {
// 			guid = UUID.randomUUID().toString();
// 		} else if (fieldLength == 16) {
// 			StringBuilder salt = new StringBuilder();
// 			Random rnd = new Random();
// 			while (salt.length() < fieldLength) {
// 				int index = (int) (rnd.nextFloat() * SALTCHARS.length());
// 				salt.append(SALTCHARS.charAt(index));
// 			}
// 			guid = salt.toString();
// 		} else if (fieldLength == 10) {
// 			long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
// 			guid = "" + number;
// 		} else {
// 			guid = "Unrecognized length request for a GUID";
// 		}
// 		return guid;
// 	}

// 	@SuppressWarnings("unused")
// 	private static void sendEmail(boolean debug, String recipientEmailId, String payLoad, String Header, String aggregatorName ,
// 			HttpServletResponse response ,String senderName , String senderPassWord) throws IOException, Exception {
// 		JsonObject result = new JsonObject();
// 		String emailSubject = "Reg : Exception While Insering Records to ChangeDocumentHeader and ChangeDocumentItems";
// 		try {
// 			if(debug){
// 				response.getWriter().println("PLATFORMEMAILuserName :"+senderName);
// 			}
// 			Properties emailProperties = getEmailProperties();
// 			Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
// 				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
// 					return new PasswordAuthentication(senderName, senderPassWord);
// 				}
// 			});
// 			MimeMessage msg = new MimeMessage(session);
// 			msg.setFrom(new InternetAddress(senderName));
// 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmailId));
// 			msg.setSubject(emailSubject);
// 			MimeMultipart emailContent = new MimeMultipart();
// 			MimeBodyPart textBodyPart = new MimeBodyPart();
// 			textBodyPart.setText("Dear " + aggregatorName + " ,"
// 					+ "\n\n"+Header+"\n\n"
// 					+ "\n\n"+payLoad+"\n\n");
// 			emailContent.addBodyPart(textBodyPart);
// 			msg.setContent(emailContent);
// 			Transport.send(msg);
// 			result.addProperty("Message", "Mail Sent Successfully");
// 			result.addProperty("ErrorCode", "");
// 			result.addProperty("Status", "000001");
// 			if(debug){
// 				response.getWriter().println(result);
// 			}
// 		} catch (SendFailedException ex) {
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Message",
// 					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		} catch (MessagingException ex) {
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Message",
// 					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		} catch (IOException ex) {
// 			StackTraceElement element[] = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < element.length; i++) {
// 				buffer.append(element[i]);
// 			}
// 			result.addProperty("Exception", ex.getClass().getCanonicalName());
// 			result.addProperty("Message",
// 					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
// 			result.addProperty("Status", "000002");
// 			result.addProperty("ErrorCode", "J002");
// 			response.getWriter().println(result);
// 		}
// 	}

// 	public static Properties getEmailProperties() throws Exception {
// 		Properties emailProps = new Properties();
// 		try {
// 			emailProps.put("mail.smtp.auth", "true");
// 			emailProps.put("mail.smtp.starttls.enable","true");
// 			emailProps.put("mail.smtp.host", "smtp.office365.com");
// 			emailProps.put("mail.smtp.port", "587");
// 		} catch (Exception ex) {
// 			throw ex;
// 		}
// 		return emailProps;
// 	}

// 	private static HttpResponse executeBatchCall(HttpServletRequest request,HttpServletResponse servletResponse,String ODataurl, String pcgwUserPass , final String body,boolean debug)
// 			throws ClientProtocolException, IOException {
// 		HttpResponse response=null;
// 		try {
// 			//			String userpass = "USR_SCHARTEC_SRVUSR" + ":" + "7ONq2XoBvkNL7uzM";
// 			//			CloseableHttpClient build = HttpClientBuilder.create().build();
// 			final HttpPost post = new HttpPost(URI.create(ODataurl + "$batch"));
// 			post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);
// 			post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(pcgwUserPass.getBytes()));
// 			HttpEntity entity = new StringEntity(body);
// 			post.setEntity(entity);
// 			response = getHttpClient().execute(post);
// 			return response;
// 		} catch (Exception ex) {
// 			StackTraceElement[] stackTrace = ex.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < stackTrace.length; i++) {
// 				buffer.append(stackTrace[i]);
// 			}
// 		}
// 		return response;
// 	}

// 	private static CloseableHttpClient getHttpClient() {
// 		CloseableHttpClient build = HttpClientBuilder.create().build();
// 		return build;
// 	}

// 	private LinkedList<String> extractErrorResponse(HttpServletResponse response ,String individualResponseBody,boolean debug) throws IOException{
// 		LinkedList<String> list = new LinkedList<>();
// 		try{
// 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
// 			DocumentBuilder builder = factory.newDocumentBuilder();
// 			InputSource is = new InputSource(new StringReader(individualResponseBody));
// 			Document doc = builder.parse(is);
// 			Element root = doc.getDocumentElement();


// 			NodeList innerErrorList = root.getElementsByTagName("innererror");
// 			if (innerErrorList != null && innerErrorList.getLength() > 0) {
// 				Node innerError = innerErrorList.item(0);
// 				NodeList exceptionList = innerError.getChildNodes();
// 				if (exceptionList != null) {
// 					for (int i = 0; i < exceptionList.getLength(); i++) {
// 						Node exception = exceptionList.item(i);
// 						if (exception.getNodeType() == Node.ELEMENT_NODE) {
// 							String exceptionText = exception.getTextContent();
// 							if (exceptionText != null) {
// 								String[] parts = exceptionText.split("key: ");
// 								if (parts.length > 1) {
// 									String keyPart = parts[1].split("already")[0]; 
// 									String[] inputArray = keyPart.split(";");

// 									for (String input1 : inputArray) {
// 										if (input1.contains("=")) {
// 											String[] keyValue = input1.split("=");
// 											if (keyValue.length == 2) {
// 												String value = keyValue[1];
// 												if (!value.isEmpty()) {
// 													String[] values = value.split(",");
// 													//													list.add(values[0] + ": " + (values.length > 1 ? values[1] : ""));
// 													list.add( (values.length > 1 ? values[1] : ""));
// 												}
// 											}
// 										} else {
// 											String[] values = input1.split(",");
// 											//											list.add(values[0] + ": " + (values.length > 1 ? values[1] : ""));
// 											list.add( (values.length > 1 ? values[1] : ""));
// 										}
// 									}
// 									for (int j=0;j<list.size();j++) {
// 										if(debug){
// 											response.getWriter().println("Keys "+list.get(j));
// 										}
// 									}
// 								}
// 							}
// 						}
// 					}
// 				}
// 			}else{
// 				response.getWriter().println("innererror tag not found");
// 			}
// 		}catch(Exception e){
// 			StackTraceElement[] stackTrace = e.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < stackTrace.length; i++) {
// 				buffer.append(stackTrace[i]);
// 			}
// 			if(debug){
// 				response.getWriter().println("Exception "+e.getLocalizedMessage());
// 			}
// 		}
// 		return list;
// 	}

// 	private void doBatchCall(HttpServletResponse response ,HttpServletRequest request , String pcgwUrl,String pcgwUserPass , JsonObject emailDetails,List<BatchPart> batchParts , boolean debug) throws Exception{
// 		String executeUrl = "";
// 		JsonObject emailObjResponse = new JsonObject();
// 		try{
// 			Properties properties  =new Properties();
// 			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));

// 			String reciverEmailId = properties.getProperty("EmailId");
// 			String senderName = emailDetails.get("UserName").getAsString();
// 			String senderPassWord = emailDetails.get("Password").getAsString();

// 			InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);
// 			String payload = org.apache.commons.io.IOUtils.toString(body);
// 			HttpResponse batchResponse = executeBatchCall(request,response,pcgwUrl, pcgwUserPass,payload, debug);
// 			int statusCode = batchResponse.getStatusLine().getStatusCode();
// 			if(debug){
// 				response.getWriter().println("statusCode: :" + statusCode);
// 			}
// 			for (Header h : batchResponse.getAllHeaders()) {
// 				if(debug){
// 					response.getWriter().println("Name: :"+h.getName());
// 					response.getWriter().println("Value: :"+h.getValue());
// 				}
// 			}

// 			InputStream responseBody = batchResponse.getEntity().getContent();
// 			String contentType = batchResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
// 			String updatedRes = org.apache.commons.io.IOUtils.toString(responseBody);
// 			response.getWriter().println("updatedRes: :"+updatedRes);
// 			if(debug){
// 				response.getWriter().println("responseBody: :" + responseBody);
// 				response.getWriter().println("contentType: :"+contentType);
// 				response.getWriter().println("updatedRes: :"+updatedRes);
// 			}
// 			try {
// 				List<BatchSingleResponse> responses = EntityProvider
// 						.parseBatchResponse(org.apache.commons.io.IOUtils.toInputStream(updatedRes), contentType);
// 				for (BatchSingleResponse singleRes : responses) {
// 					String statusCodess = singleRes.getStatusCode();
// 					String individualResponseBody = singleRes.getBody();
// 					if(debug){
// 						response.getWriter().println("statusCodess: "+statusCodess);
// 						response.getWriter().println("individualResponseBody: "+individualResponseBody);
// 						response.getWriter().println("statusCodess: "+statusCodess);
// 						response.getWriter().println("getContentId: "+singleRes.getContentId());
// 						response.getWriter().println("getStatusInfo: "+singleRes.getStatusInfo());
// 						response.getWriter().println("getHeaderNames: "+singleRes.getHeaderNames());
// 						response.getWriter().println("getHeaders: "+singleRes.getHeaders());
// 					}

// 					if(statusCodess.equalsIgnoreCase("500")){
// 						LinkedList<String> list = extractErrorResponse(response, individualResponseBody, debug);
// 						if(debug){
// 							response.getWriter().println("list: "+list);
// 						}
// 						if(list.size() == 3){
// 							executeUrl = pcgwUrl+ "ChangeDocumentHeader"+"?$filter=ObjectClass%20eq%20%27"
// 									+""+"%27%20and%20ObjectValue%20eq%20%27"+"09FD72D4-525C-4B42-A10E-D567F6291A2A"+"%27%20and%20DocChangeNo%20eq%20%27"+"1000000167"+"%27";
// 						}else if(list.size() == 5){
// 							executeUrl = pcgwUrl+"ChangeDocumentItems"+"?$filter=ObjectClass%20eq%20%27" +""+ "%27%20and%20ObjectValue%20eq%20%27"+ "2034-9474-jfhf-9484f-9844" +
// 									"%27%20and%20DocChangeNo%20eq%20%27"+"1000000335"+"%27%20and%20TableName%20eq%20%27"+"Surveys"+"%27%20and%20ChangedTablekey%20eq%20%27"+"2034-9474-jfhf-9484f-9844"+
// 									"%27%20and%20FieldName%20eq%20%27"+"ActivityType"+"%27%20and%20ChangeInd%20eq%20%27"+"U"+"%27";
// 						}

// 						if(!executeUrl.equalsIgnoreCase("")){
// 							emailObjResponse= commonUtils.executeURL(executeUrl, pcgwUserPass, response);
// 						}
// 						sendEmail(debug, reciverEmailId, emailObjResponse.toString(), "", "Arteria Technologies Private Limited", response ,senderName, senderPassWord);

// 					}else if(!statusCodess.equalsIgnoreCase("201")){
// 						if(debug){
// 							response.getWriter().println(individualResponseBody);
// 						}
// 						sendEmail(debug, reciverEmailId, individualResponseBody, "", "Arteria Technologies Private Limited", response ,senderName, senderPassWord);
// 					}
// 					if(debug){
// 						response.getWriter().println("statusCodess: "+statusCodess);
// 						response.getWriter().println("individualResponseBody: "+individualResponseBody);
// 					}
// 				}
// 			} catch (BatchException e) {
// 				StackTraceElement[] stackTrace = e.getStackTrace();
// 				StringBuffer buffer = new StringBuffer();
// 				for (int i = 0; i < stackTrace.length; i++) {
// 					buffer.append(stackTrace[i]);
// 				}
// 				response.getWriter().println("BatchException "+e.getLocalizedMessage());
// 			}
// 		}catch (BatchException e) {
// 			StackTraceElement[] stackTrace = e.getStackTrace();
// 			StringBuffer buffer = new StringBuffer();
// 			for (int i = 0; i < stackTrace.length; i++) {
// 				buffer.append(stackTrace[i]);
// 			}
// 			response.getWriter().println("BatchException "+e.getLocalizedMessage());
// 		}
// 	}
// }

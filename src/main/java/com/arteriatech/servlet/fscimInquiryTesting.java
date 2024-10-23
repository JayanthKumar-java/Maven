package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.naming.NamingException;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.TimeZone;

@Configuration
public class fscimInquiryTesting extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
		if(null!= req.getParameter("date") && !req.getParameter("date").equalsIgnoreCase("")){
			String inputDate = req.getParameter("date");
			
            resp.getWriter().println("inputDate :"+inputDate);
		
			JsonObject validatedResponse =validateInput(inputDate, resp);
			if(validatedResponse.get("Status").getAsString().equalsIgnoreCase("000001")){
				resp.getWriter().println("validatedResponse :"+validatedResponse);
				resp.getWriter().println("inputDate :"+inputDate);
			}else{
				resp.getWriter().println("validatedResponse :"+validatedResponse);
			}
		}

        String cpiInputDate ="2024-10-16";
        String pygwUrl = "https://finessart-corpconnect-dev-py-finessart-pc-artec-xsjs-service.cfapps.eu10-004.hana.ondemand.com/ARTEC/PYGW/service.xsodata/";
		String userPass = "sb-na-814b1a7c-118e-4bb3-9eb1-757dd8785a22!t159545:u4657457467";
        boolean debug =false;


        // String executeUrl = pygwUrl+ "SupplyChainFinanceDiscInvoices?$filter=CreatedOn%20eq%20datetime%27"+cpiInputDate+"%27";
//    String executeUrl = pygwUrl+ "SupplyChainFinanceDiscInvoices";
//         JsonObject scfResponse = executeURL(executeUrl, userPass, resp, debug);
//         resp.getWriter().println("scfResponse: " + scfResponse);
		
		// if(scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
		// JsonArray scfArray = scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		// resp.getWriter().println("scfArray: " + scfArray);
		// JsonObject scfObject = new JsonObject();
		// for(int i=0;i<=scfArray.size()-1;i++){
		// 	 scfObject = scfArray.get(i).getAsJsonObject();
		// 	String invNo = "";
		// 	String productCode = "";
		// 	String clientCode = "";
		// 	if( scfObject.get("LoanNumber").isJsonNull() || scfObject.get("LoanNumber").getAsString().equalsIgnoreCase("") ){
		// 		invNo = scfObject.get("InvoiceNo").getAsString();
		// 		resp.getWriter().println("invNo: " + invNo);
		// 	}

		// 	executeUrl = pygwUrl + "PFCNFG?$filter=ProductCode%20eq%20%27TATABUSINESSHUB%27";
		// 	JsonObject pfcNfgResponse = executeURL(executeUrl, userPass, resp, debug);
			 
		// 	JsonArray pfcNfgArray= pfcNfgResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
		// 	JsonObject pfcNfgObject = pfcNfgArray.get(0).getAsJsonObject();
		// 	productCode = pfcNfgObject.get("ProductCode").getAsString();
		// 	clientCode = pfcNfgObject.get("ClientCode").getAsString();
		// 	resp.getWriter().println("productCode: " + productCode);
		// 	resp.getWriter().println("clientCode: " + clientCode);

		// 	executeUrl = pygwUrl + "PFCNFG?$filter=ClientCode%20eq%20%27TATABUSINESSHUB%27";
		// 	pfcNfgResponse = executeURL(executeUrl, userPass, resp, debug);
			

		// 	 JsonObject cpiInputPayload = new JsonObject();
		// 	 cpiInputPayload.addProperty("InvoiceNumber", invNo);
		// 	 cpiInputPayload.addProperty("ProductCode", productCode);
		// 	 cpiInputPayload.addProperty("ClientCode", clientCode);

		// 	 resp.getWriter().println("cpiInputPayload: " + cpiInputPayload);
		// }

		// resp.getWriter().println("scfObject: " + scfObject);

		// if(!scfObject.get("CPAccountNo").isJsonNull() && !scfObject.get("CPAccountNo").getAsString().equalsIgnoreCase("") ){
		// 	resp.getWriter().println("CPAccountNo: " + scfObject.get("CPAccountNo").getAsString());                       
		// }

		// long changedOnInMillis = getCreatedOnDate();
		// 	JsonObject updatePayload =  new JsonObject();
		

		// 	updatePayload.addProperty("AggregatorID", "ahfhff");
		// 	updatePayload.addProperty("InvCategoryID", "null");
		// 	updatePayload.addProperty("InvoiceNo", "null");
		// 	updatePayload.addProperty("InvFiscalYear", "null");
		// 	updatePayload.addProperty("InvAccntDocCategoryID", "null");
		// 	updatePayload.addProperty("InvAccntDocumentNo", "null");
		// 	updatePayload.addProperty("InvAccntDocFiscalYear", "null");
		// 	updatePayload.addProperty("CompanyCode", "null");
		// 	updatePayload.addProperty("PymntDocCategoryID", "null");
		// 	updatePayload.addProperty("PymntDocNumber", "null");
		// 	updatePayload.addProperty("PymntDocFiscalYear", "null");
		// 	updatePayload.addProperty("PymntDocCompanyCode", "null");
		// 	updatePayload.addProperty("BankReferenceNo", "null");
		// 	// updatePayload.addProperty("LoanNumber", "null");
		// 	updatePayload.addProperty("LoanAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("LoanDueDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("LoanRepayAmount", new BigDecimal("1500.00").toString());
		// 	// updatePayload.addProperty("LoanRepayAmount", new BigDecimal(scfObject.get("LoanRepayAmount").getAsString()).toString());
		// 	updatePayload.addProperty("LoanOutstandingAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("IntOutstandingAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("Currency", "null");
		// 	updatePayload.addProperty("LoanStatusID", "null");
		// 	updatePayload.addProperty("InvoiceStatusID", "null");
		// 	updatePayload.addProperty("ErrorCodeID", "null");
		// 	updatePayload.addProperty("ErrorDescription", "null");
		// 	updatePayload.addProperty("SourceOfEntry", "null");
		// 	updatePayload.addProperty("SenderID", "null");
		// 	updatePayload.addProperty("ReceiverID", "null");
		// 	updatePayload.addProperty("SellerGSTN", "null");
		// 	updatePayload.addProperty("SellerPAN", "null");
		// 	updatePayload.addProperty("BuyerGSTN", "null");
		// 	updatePayload.addProperty("BuyerPAN", "null");
		// 	updatePayload.addProperty("LoanDisbursementDate",  "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("FinancierName", "null");
		// 	updatePayload.addProperty("InterestPaidBy", "null");
		// 	updatePayload.addProperty("ReasonForRejection", "null");
		// 	updatePayload.addProperty("Remarks", "null");
		// 	updatePayload.addProperty("DeletionStatus", "null");
		// 	updatePayload.addProperty("LoanRepaymentDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("VendorInvoiceNo", "null");
		// 	updatePayload.addProperty("UniqueRefNo", "null");
		// 	updatePayload.addProperty("VendorNo", "null");
		// 	updatePayload.addProperty("VendorName", "null");
		// 	updatePayload.addProperty("InvoiceAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("InvoiceDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("VendorInvDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("VendorInvDueDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("PaymentMethod", "7");
		// 	updatePayload.addProperty("InvoiceInternalRefNo", "null");
		// 	updatePayload.addProperty("InterestRate", new BigDecimal("150.00").toString());
		// 	updatePayload.addProperty("InterestBorneBy", "null");
		// 	updatePayload.addProperty("InterestPymntMode", "null");
		// 	updatePayload.addProperty("InterestAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("InterestType", "null");
		// 	updatePayload.addProperty("ClientCode", "null");
		// 	updatePayload.addProperty("CounterPartyCode", "null");
		// 	updatePayload.addProperty("CounterPartyName", "null");
		// 	// updatePayload.addProperty("LoanType", "null");
		// 	// updatePayload.addProperty("CPAccountNo", "null");
		// 	updatePayload.addProperty("NewLoanDueDate", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("ProductCode", "null");
		// 	// updatePayload.addProperty("RebateAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("RebateAmount", scfObject.get("RebateAmount").getAsBigDecimal().toString());
		// 	updatePayload.addProperty("ERPDocumentNo", "null");
		// 	// updatePayload.addProperty("ProcessedAmount", new BigDecimal("1500.00").toString());
		// 	updatePayload.addProperty("CreatedBy", "null");
		// 	updatePayload.addProperty("CreatedAt", getCreatedAtTime());
		// 	updatePayload.addProperty("CreatedOn", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("ChangedBy", "null");
		// 	updatePayload.addProperty("ChangedAt", getCreatedAtTime());
		// 	updatePayload.addProperty("ChangedOn", "/Date(" + changedOnInMillis + ")/");
		// 	updatePayload.addProperty("Source", "null");
		// 	updatePayload.addProperty("SourceReferenceID", "null");
		// 	String Id = "4947585855";
		// 	String executeURL = pygwUrl+"SupplyChainFinanceDiscInvoices('"+Id+"')";
		// 	executeUpdate(executeURL, userPass, resp, updatePayload, req, debug);

		// 	executeURL = pygwUrl+"SupplyChainFinanceDiscInvoices";
		// 	updatePayload.addProperty("ID", "455745njfjhfnfhf");
		// 	updatePayload.addProperty("CreatedOn ", "/Date(" + changedOnInMillis + ")/");
        // executePostJsonURL(executeURL, userPass, resp, updatePayload, req, true);
	// }else{
	// 	resp.getWriter().println("size 0");
	// }
	String executeURL = pygwUrl+"PYConfigs";
	
	JsonObject updatePayload =  new JsonObject();
	updatePayload.addProperty("ID", "JDHHJFJF");
			executePostJsonURL(executeURL, userPass, resp, updatePayload, req, true);
	}

	public JsonObject validateInput(String inputDate,HttpServletResponse response) throws IOException{
        String format = "dd-MM-yyyy";
        JsonObject validatedResult = new JsonObject();
        
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // to prevent parsing of invalid dates

        try {
            java.util.Date date = sdf.parse(inputDate);
            String formattedDate = sdf.format(date);
            if (inputDate.equals(formattedDate)) {
				validatedResult.addProperty("Status", "000001");
            } else {
                validatedResult.addProperty("ErrorCode", "J002");
			validatedResult.addProperty("Message", "Date received in invalid format. Please send in dd-MM-yyyy");
			validatedResult.addProperty("Status", "000002");
            }
        } catch (ParseException e) {
            System.out.println("The date is not in the correct format.");
        }
		response.getWriter().println("validatedResult "+ validatedResult);
        return validatedResult;
    }

    public static boolean isValidDate(String dateString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // to prevent parsing of invalid dates

        try {
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

	public JsonObject executeUpdate(String executeURL, String userPass, HttpServletResponse response, JsonObject updatePayLoad, 
			HttpServletRequest request, boolean debug) throws IOException{
		HttpPost httpPost = null;
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		String userName="", password="", authParam="";
		JsonObject jsonObj = new JsonObject();
		try{
			
			requestEntity = new StringEntity(updatePayLoad.toString());
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
			response.getWriter().println("updatePayLoad "+ updatePayLoad);
			if(debug){
				response.getWriter().println("executeHttpPost.executeURL: "+ executeURL);
		        
		        response.getWriter().println("executeHttpPost.userName: "+ userName);
		        response.getWriter().println("executeHttpPost.password: "+ password);
		        response.getWriter().println("executeHttpPost.authParam: "+ authParam);
		        response.getWriter().println("executeHttpPost.basicAuth: "+ basicAuth);
			}
			
	       		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("P000000", "DevCFHNDBP@$$wdFeb2024");
				
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
				
				httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
				
				HttpPut updateRequest = new HttpPut(executeURL);    
				      		 
				updateRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
				
				updateRequest.setHeader("Content-Type", "application/json");
				
				updateRequest.setHeader("Accept", "application/json");
				
				updateRequest.setHeader("X-HTTP-Method", "PUT");
				
				updateRequest.setEntity(requestEntity);
				
				HttpResponse httpResponse = httpClient.execute(updateRequest);
			
				response.getWriter().println(httpResponse.getEntity());
				
				
				if(httpResponse.getStatusLine().getStatusCode()==204)
				{
					JsonObject respoObj=new JsonObject();
					respoObj.addProperty("message", "Records Updated Successfully");
					response.getWriter().println(respoObj);
					response.getWriter().println("URl to check the Table"+ executeURL);
				}
				else 
				{
					responseEntity = httpResponse.getEntity();
					response.getWriter().println("Error " + httpResponse.getStatusLine().getStatusCode());
				}
		}catch (RuntimeException e) {
			httpPost.abort();
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
			response.getWriter().println("executeHttpPost.RuntimeException:"+buffer.toString());
			if(e.getLocalizedMessage()!=null){
				jsonObj.addProperty("ExceptionMessage", e.getLocalizedMessage());
			}
			jsonObj.addProperty("ErrorCode", "J002");
			jsonObj.addProperty("Message", buffer.toString());
			jsonObj.addProperty("Status", "000002");
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			httpPost.abort();
			
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			if(debug)
			response.getWriter().println("executeHttpPost.Exception:"+buffer.toString());
			if(e.getLocalizedMessage()!=null){
				jsonObj.addProperty("ExceptionMessage",e.getLocalizedMessage());
			}
			jsonObj.addProperty("ErrorCode", "J002");
			jsonObj.addProperty("Message", buffer.toString());
			jsonObj.addProperty("Status", "000002");
		}finally{
			httpClient.close();
		}
		return jsonObj;
	}

	
	public JsonObject executePostJsonURL(String executeURL, String userPass, HttpServletResponse response, JsonObject insertPayLoad, HttpServletRequest request, boolean debug) throws IOException {
		CloseableHttpClient httpClient = null;
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		String userName = "", password = "", authParam = "";
		JsonObject jsonObj = new JsonObject();
		String data = "";
		try {
			if (debug) {
				response.getWriter().println("executePostURL-insertPayLoad: " + insertPayLoad);
			}

			if (debug)
				response.getWriter().println("executePostURL-executeURL: " + executeURL);

			

			// byte[] encodedByte = Base64.getEncoder().encode(authParam.getBytes());
			// String encodedStr = new String(encodedByte);
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());

			if (debug) {
				response.getWriter().println("executeHttpPost.executeURL: " + executeURL);
				
				response.getWriter().println("executeHttpPost.userName: " + userName);
				response.getWriter().println("executeHttpPost.password: " + password);
				response.getWriter().println("executeHttpPost.authParam: " + authParam);
				response.getWriter().println("executeHttpPost.basicAuth: " + basicAuth);
			}


	//  * String proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST"); int proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT")); HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http"); DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);


			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("P000000", "DevCFHNDBP@$$wdFeb2024");
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);

			if (debug) {
				response.getWriter().println("insertPayLoad.toString(): " + insertPayLoad.toString());
			}

			requestEntity = new StringEntity(insertPayLoad.toString());

			// httpClient = HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
			HttpPost postRequest = new HttpPost(executeURL);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			// postRequest.setHeader("X-CSRF-Token", csrfToken);
			postRequest.setEntity(requestEntity);

			HttpResponse httpPostResponse = httpClient.execute(postRequest);
			responseEntity = httpPostResponse.getEntity();

			if (httpPostResponse.getEntity().getContentType() != null && httpPostResponse.getEntity().getContentType().toString() != "") {
				String contentType = httpPostResponse.getEntity().getContentType().toString().replaceAll("content-type:", "").trim();
				if (contentType.equalsIgnoreCase("application/xml;charset=utf-8")) {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					// response.getOutputStream().print(EntityUtils.toString(countEntity));
					if (debug)
						response.getWriter().println(EntityUtils.toString(responseEntity));
				} else {
					response.setContentType(contentType);
					data = EntityUtils.toString(responseEntity);
					// response.getOutputStream().print(Data);
					if (debug)
						response.getWriter().println(data);
				}
			} else {
				response.setContentType("application/pdf");
				data = EntityUtils.toString(responseEntity);
				// response.getOutputStream().print(EntityUtils.toString(countEntity));
				if (debug)
					response.getWriter().println(EntityUtils.toString(responseEntity));
			}
			JsonParser parser = new JsonParser();
			jsonObj = (JsonObject) parser.parse(data);

		} catch (Exception e) {
			response.getWriter().println("Data: " + data);
			response.getWriter().println("Exception: " + e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println("executePostURL-Exception Stack Trace: " + buffer.toString());
		} finally {
			httpClient.close();

		}
		return jsonObj;
	}

	public String getCreatedAtTime(){
		String createdAt="";
		 try {
			 SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			 sdf.setTimeZone(TimeZone.getTimeZone("IST"));
			 createdAt = sdf.format(new Date());
			 createdAt = "PT"+createdAt.substring(11, 13) +"H"+createdAt.substring(14, 16)+"M"+createdAt.substring(17, createdAt.length())+"S";
			/*Calendar cal = Calendar.getInstance();
			createdAt = "PT"+cal.get(Calendar.HOUR_OF_DAY)+"H"+cal.get(Calendar.MINUTE)+"M"+cal.get(Calendar.SECOND)+"S";*/
		} catch (Exception e) {
			createdAt="PT00H00M00S";
		}
		 return createdAt;
	}

	public long getCreatedOnDate(){
		long createdOn=0;
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			sdf1.setTimeZone(TimeZone.getTimeZone("IST"));
			Date createdAtDate = sdf2.parse(sdf1.format(new Date()));
			createdOn = createdAtDate.getTime();
		} 
		catch (Exception e){
			createdOn=0;
		}
		return createdOn;
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

    
    @Bean
	public ServletRegistrationBean<fscimInquiryTesting> fscimInquiryTestingBean() {
		ServletRegistrationBean<fscimInquiryTesting> bean = new ServletRegistrationBean<>(new fscimInquiryTesting(), "/fscimInquiryTesting");
		return bean;
	}
}

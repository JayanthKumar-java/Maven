package com.arteriatech.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
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

@Configuration
@WebServlet("/SCFUpdate")
public class SCFUpdate extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "";
		HttpEntity requestEntity = null;
		HttpEntity responseEntity = null;
		// CloseableHttpClient httpClient = null;
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = new JsonObject();
		boolean debug = false;
		String changedAt = "", changedBy = "";
		long changedOnInMillis = 0;
		JSONObject updatedJson = new JSONObject();
		String id = "", userName = "", password = "", userPass = "", oDataURL = "", executeURL = "";
		Properties properties = new Properties();
		String absolutePath = "/home/user/projects/demo/src/main/webapp/Resources/KeyProperties.properties";
        
        File file = new File(absolutePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + absolutePath);
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            
            // Now you can use the properties object
            // Example: String value = properties.getProperty("someKey");
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception appropriately
        }

		response.getWriter().println(properties);
		// properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
		try {
			DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
					.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
			Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
					.tryGetDestination("PYGWHANA", options);
			HttpDestination httpDestConfiguration = destinationAccessor.get().asHttp();
			HttpClient client = HttpClientAccessor.getHttpClient(httpDestConfiguration);

			inputPayload = commonUtils.getGetBody(request, response);
			jsonPayload = (JsonObject) jsonParser.parse(inputPayload);
			if (jsonPayload.has("debug") && jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;
			}
			if (debug) {
				response.getWriter().println(" Input Payload Json " + jsonPayload);
			}
			if (jsonPayload.has("ID") && jsonPayload.get("ID").getAsString() != null) {
				id = jsonPayload.get("ID").getAsString();
			}
			if (!id.equalsIgnoreCase("") && id.length() > 0) {
				changedBy = commonUtils.getUserPrincipal(request, "name", response);
				changedOnInMillis = commonUtils.getCreatedOnDate();
				changedAt = commonUtils.getCreatedAtTime();
				updatedJson.accumulate("ID", id);
				updatedJson.accumulate("ChangedBy", changedBy);
				updatedJson.accumulate("ChangedOn", "/Date(" + changedOnInMillis + ")/");
				updatedJson.accumulate("ChangedAt", changedAt);
				if (jsonPayload.has("CPGUID") && !jsonPayload.get("CPGUID").isJsonNull()) {
					updatedJson.accumulate("CPGUID", jsonPayload.get("CPGUID").getAsString());
				}else{
					updatedJson.accumulate("CPGUID", "");
				}

				if (jsonPayload.has("CPTypeID") && !jsonPayload.get("CPTypeID").isJsonNull()) {
					updatedJson.accumulate("CPTypeID", jsonPayload.get("CPTypeID").getAsString());
				}else{
					updatedJson.accumulate("CPTypeID", "");
				}

				if (jsonPayload.has("AggregatorID") && !jsonPayload.get("AggregatorID").isJsonNull()) {
					updatedJson.accumulate("AggregatorID", jsonPayload.get("AggregatorID").getAsString());
				}else{
					updatedJson.accumulate("AggregatorID", "");
				}

				if (jsonPayload.has("OfferAmt") && !jsonPayload.get("OfferAmt").isJsonNull()) {
					updatedJson.accumulate("OfferAmt", jsonPayload.get("OfferAmt").getAsString());
				}else{
					updatedJson.accumulate("OfferAmt", JSONObject.NULL);
				}

				if (jsonPayload.has("OfferTenure") && !jsonPayload.get("OfferTenure").isJsonNull()) {
					updatedJson.accumulate("OfferTenure", jsonPayload.get("OfferTenure").getAsString());
				}else{
					updatedJson.accumulate("OfferTenure", "");
				}

				if (jsonPayload.has("Rate") && !jsonPayload.get("Rate").isJsonNull()) {
					updatedJson.accumulate("Rate", jsonPayload.get("Rate").getAsString());
				}else{
					updatedJson.accumulate("Rate", "");
				}

				if (jsonPayload.has("AccountNo") && !jsonPayload.get("AccountNo").isJsonNull()) {
					updatedJson.accumulate("AccountNo", jsonPayload.get("AccountNo").getAsString());
				}else{
					updatedJson.accumulate("AccountNo", "");
				}

				if (jsonPayload.has("NoOfChequeReturns") && !jsonPayload.get("NoOfChequeReturns").isJsonNull()) {
					updatedJson.accumulate("NoOfChequeReturns", jsonPayload.get("NoOfChequeReturns").getAsString());
				}else{
					updatedJson.accumulate("NoOfChequeReturns", "");
				}

				if (jsonPayload.has("PaymentDelayDays12Months")
						&& !jsonPayload.get("PaymentDelayDays12Months").isJsonNull()) {
					updatedJson.accumulate("PaymentDelayDays12Months",
							jsonPayload.get("PaymentDelayDays12Months").getAsString());
				}else{
					updatedJson.accumulate("PaymentDelayDays12Months", JSONObject.NULL);
				}

				if (jsonPayload.has("BusinessVintageOfDealer")
						&& !jsonPayload.get("BusinessVintageOfDealer").isJsonNull()) {
					updatedJson.accumulate("BusinessVintageOfDealer",
							jsonPayload.get("BusinessVintageOfDealer").getAsString());
				}else{
					updatedJson.accumulate("BusinessVintageOfDealer", "");
				}

				if (jsonPayload.has("PurchasesOf12Months") && !jsonPayload.get("PurchasesOf12Months").isJsonNull()) {
					updatedJson.accumulate("PurchasesOf12Months", jsonPayload.get("PurchasesOf12Months").getAsString());
				}else{
					updatedJson.accumulate("PurchasesOf12Months", JSONObject.NULL);
				}

				if (jsonPayload.has("DealersOverallScoreByCorp")
						&& !jsonPayload.get("DealersOverallScoreByCorp").isJsonNull()) {
					updatedJson.accumulate("DealersOverallScoreByCorp",
							jsonPayload.get("DealersOverallScoreByCorp").getAsString());
				}else{
					updatedJson.accumulate("DealersOverallScoreByCorp", "");
				}

				if (jsonPayload.has("CorpRating") && !jsonPayload.get("CorpRating").isJsonNull()) {
					updatedJson.accumulate("CorpRating", jsonPayload.get("CorpRating").getAsString());
				}else{
					updatedJson.accumulate("CorpRating", "");
				}

				if (jsonPayload.has("DealerVendorFlag") && !jsonPayload.get("DealerVendorFlag").isJsonNull()) {
					updatedJson.accumulate("DealerVendorFlag", jsonPayload.get("DealerVendorFlag").getAsString());
				}else{
					updatedJson.accumulate("DealerVendorFlag", "");
				}

				if (jsonPayload.has("ConstitutionType") && !jsonPayload.get("ConstitutionType").isJsonNull()) {
					updatedJson.accumulate("ConstitutionType", jsonPayload.get("ConstitutionType").getAsString());
				}else{
					updatedJson.accumulate("ConstitutionType", "");
				}

				if (jsonPayload.has("MaxLimitPerCorp") && !jsonPayload.get("MaxLimitPerCorp").isJsonNull()) {
					updatedJson.accumulate("MaxLimitPerCorp", jsonPayload.get("MaxLimitPerCorp").getAsString());
				}else{
					updatedJson.accumulate("MaxLimitPerCorp", JSONObject.NULL);
				}

				if (jsonPayload.has("salesOf12Months") && !jsonPayload.get("salesOf12Months").isJsonNull()) {
					updatedJson.accumulate("salesOf12Months", jsonPayload.get("salesOf12Months").getAsString());
				}else{
					updatedJson.accumulate("salesOf12Months", JSONObject.NULL);
				}

				if (jsonPayload.has("Currency") && !jsonPayload.get("Currency").isJsonNull()) {
					updatedJson.accumulate("Currency", jsonPayload.get("Currency").getAsString());
				}else{
					updatedJson.accumulate("Currency", "");
				}

				if (jsonPayload.has("StatusID") && !jsonPayload.get("StatusID").isJsonNull()) {
					updatedJson.accumulate("StatusID", jsonPayload.get("StatusID").getAsString());
				}else{
					updatedJson.accumulate("StatusID", "");
				}

				if (jsonPayload.has("MCLR6Rate") && !jsonPayload.get("MCLR6Rate").isJsonNull()) {
					updatedJson.accumulate("MCLR6Rate", jsonPayload.get("MCLR6Rate").getAsString());
				}else{
					updatedJson.accumulate("MCLR6Rate", JSONObject.NULL);
				}

				if (jsonPayload.has("InterestRateSpread") && !jsonPayload.get("InterestRateSpread").isJsonNull()) {
					updatedJson.accumulate("InterestRateSpread", jsonPayload.get("InterestRateSpread").getAsString());
				}else{
					updatedJson.accumulate("InterestRateSpread", "");
				}

				if (jsonPayload.has("TenorOfPayment") && !jsonPayload.get("TenorOfPayment").isJsonNull()) {
					updatedJson.accumulate("TenorOfPayment", jsonPayload.get("TenorOfPayment").getAsString());
				}else{
					updatedJson.accumulate("TenorOfPayment", "");
				}

				if (jsonPayload.has("ADDLNPRDINTRateSP") && !jsonPayload.get("ADDLNPRDINTRateSP").isJsonNull()) {
					updatedJson.accumulate("ADDLNPRDINTRateSP", jsonPayload.get("ADDLNPRDINTRateSP").getAsString());
				}else{
					updatedJson.accumulate("ADDLNPRDINTRateSP", "");
				}

				if (jsonPayload.has("AddlnTenorOfPymt") && !jsonPayload.get("AddlnTenorOfPymt").isJsonNull()) {
					updatedJson.accumulate("AddlnTenorOfPymt", jsonPayload.get("AddlnTenorOfPymt").getAsString());
				}else{
					updatedJson.accumulate("AddlnTenorOfPymt", "");
				}

				if (jsonPayload.has("DefIntSpread") && !jsonPayload.get("DefIntSpread").isJsonNull()) {
					updatedJson.accumulate("DefIntSpread", jsonPayload.get("DefIntSpread").getAsString());
				}else{
					updatedJson.accumulate("DefIntSpread", "");
				}

				if (jsonPayload.has("ProcessingFee") && !jsonPayload.get("ProcessingFee").isJsonNull()) {
					updatedJson.accumulate("ProcessingFee", jsonPayload.get("ProcessingFee").getAsString());
				}else{
					updatedJson.accumulate("ProcessingFee", JSONObject.NULL);
				}

				if (jsonPayload.has("ECustomerID") && !jsonPayload.get("ECustomerID").isJsonNull()) {
					updatedJson.accumulate("ECustomerID", jsonPayload.get("ECustomerID").getAsString());
				}else{
					updatedJson.accumulate("ECustomerID", "");
				}

				if (jsonPayload.has("EContractID") && !jsonPayload.get("EContractID").isJsonNull()) {
					updatedJson.accumulate("EContractID", jsonPayload.get("EContractID").getAsString());
				}else{
					updatedJson.accumulate("EContractID", "");
				}

				if (jsonPayload.has("ApplicationNo") && !jsonPayload.get("ApplicationNo").isJsonNull()) {
					updatedJson.accumulate("ApplicationNo", jsonPayload.get("ApplicationNo").getAsString());
				}else{
					updatedJson.accumulate("ApplicationNo", "");
				}

				if (jsonPayload.has("CallBackStatus") && !jsonPayload.get("CallBackStatus").isJsonNull()) {
					updatedJson.accumulate("CallBackStatus", jsonPayload.get("CallBackStatus").getAsString());
				}else{
					updatedJson.accumulate("CallBackStatus", "");
				}

				if (jsonPayload.has("ECompleteDate") && !jsonPayload.get("ECompleteDate").isJsonNull()) {
					updatedJson.accumulate("ECompleteDate", jsonPayload.get("ECompleteDate").getAsString());
				}else{
					updatedJson.accumulate("ECompleteDate", JSONObject.NULL);
				}

				if (jsonPayload.has("ECompleteTime") && !jsonPayload.get("ECompleteTime").isJsonNull()) {
					updatedJson.accumulate("ECompleteTime", jsonPayload.get("ECompleteTime").getAsString());
				}else{
					updatedJson.accumulate("ECompleteTime", JSONObject.NULL);
				}

				if (jsonPayload.has("ApplicantID") && !jsonPayload.get("ApplicantID").isJsonNull()) {
					updatedJson.accumulate("ApplicantID", jsonPayload.get("ApplicantID").getAsString());
				}else{
					updatedJson.accumulate("ApplicantID", "");
				}

				if (jsonPayload.has("LimitPrefix") && !jsonPayload.get("LimitPrefix").isJsonNull()) {
					updatedJson.accumulate("LimitPrefix", jsonPayload.get("LimitPrefix").getAsString());
				}else{
					updatedJson.accumulate("LimitPrefix", "");
				}

				if (jsonPayload.has("InterestSpread") && !jsonPayload.get("InterestSpread").isJsonNull()) {
					updatedJson.accumulate("InterestSpread", jsonPayload.get("InterestSpread").getAsString());
				}else{
					updatedJson.accumulate("InterestSpread", JSONObject.NULL);
				}

				if (jsonPayload.has("DDBActive") && !jsonPayload.get("DDBActive").isJsonNull()) {
					updatedJson.accumulate("DDBActive", jsonPayload.get("DDBActive").getAsString());
				}else{
					updatedJson.accumulate("DDBActive", "");
				}

				if (jsonPayload.has("ProcessFeePerc") && !jsonPayload.get("ProcessFeePerc").isJsonNull()) {
					updatedJson.accumulate("ProcessFeePerc", jsonPayload.get("ProcessFeePerc").getAsString());
				}else{
					updatedJson.accumulate("ProcessFeePerc", JSONObject.NULL);
				}

				if (jsonPayload.has("ValidTo") && !jsonPayload.get("ValidTo").isJsonNull()) {
					updatedJson.accumulate("ValidTo", jsonPayload.get("ValidTo").getAsString());
				}else{
					updatedJson.accumulate("ValidTo", JSONObject.NULL);
				}

				if (jsonPayload.has("CreatedBy") && !jsonPayload.get("CreatedBy").isJsonNull()) {
					updatedJson.accumulate("CreatedBy", jsonPayload.get("CreatedBy").getAsString());
				}else{
					updatedJson.accumulate("CreatedBy", "");
				}

				if (jsonPayload.has("CreatedAt") && !jsonPayload.get("CreatedAt").isJsonNull()) {
					updatedJson.accumulate("CreatedAt", jsonPayload.get("CreatedAt").getAsString());
				}else{
					updatedJson.accumulate("CreatedAt", JSONObject.NULL);
				}

				if (jsonPayload.has("CreatedOn") && !jsonPayload.get("CreatedOn").isJsonNull()) {
					updatedJson.accumulate("CreatedOn", jsonPayload.get("CreatedOn").getAsString());
				}else{
					updatedJson.accumulate("CreatedOn", JSONObject.NULL);
				}

				if (jsonPayload.has("Source") && !jsonPayload.get("Source").isJsonNull()) {
					updatedJson.accumulate("Source", jsonPayload.get("Source").getAsString());
				}else{
					updatedJson.accumulate("Source", "");
				}

				if (jsonPayload.has("SourceReferenceID") && !jsonPayload.get("SourceReferenceID").isJsonNull()) {
					updatedJson.accumulate("SourceReferenceID", jsonPayload.get("SourceReferenceID").getAsString());
				}else{
					updatedJson.accumulate("SourceReferenceID", "");
				}

				oDataURL = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;
				executeURL = oDataURL + "SupplyChainFinances('" + id + "')";
				if (debug) {
					response.getWriter().println(" SupplyChainFinances update  executeURL :" + executeURL);
					response.getWriter().println(" SupplyChainFinances Updated Payload " + updatedJson);
				}
				requestEntity = new StringEntity(updatedJson.toString());
				String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
				if (debug) {
					response.getWriter().println("executeUpdate.executeURL: " + executeURL);
					response.getWriter().println("executeUpdate.userName: " + userName);
					response.getWriter().println("executeUpdate.password: " + password);
					response.getWriter().println("executeUpdate.basicAuth: " + basicAuth);
				}
				/* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
				httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build(); */
				HttpPut updateRequest = new HttpPut(executeURL);
				updateRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
				updateRequest.setHeader("Content-Type", "application/json");
				updateRequest.setHeader("Accept", "application/json");
				updateRequest.setHeader("X-HTTP-Method", "PUT");
				updateRequest.setEntity(requestEntity);
				// HttpResponse httpResponse = httpClient.execute(updateRequest);
				HttpResponse httpResponse = client.execute(updateRequest);
				if (debug) {
					response.getWriter()
							.println("httpResponse status code :" + httpResponse.getStatusLine().getStatusCode());
				}
				if (httpResponse.getStatusLine().getStatusCode() == 204) {
					JsonObject respoObj = new JsonObject();
					respoObj.addProperty("message", "Records Updated Successfully");
					respoObj.addProperty("ErrorCode", "");
					respoObj.addProperty("Status", "000001");
					response.getWriter().println(respoObj);

				} else {
					JSONObject responseJsonObject = new JSONObject();
					responseEntity = httpResponse.getEntity();
					if (debug) {
						response.getWriter().println(
								"executeUpdate.getStatusCode: " + httpResponse.getStatusLine().getStatusCode());
					}
					responseJsonObject = new JSONObject(EntityUtils.toString(responseEntity));
					if (responseJsonObject.has("error")) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(responseJsonObject);
					} else {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					}

				}

			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("Status", "000002");
				result.addProperty("Message", "ID is Mandatory for updating");
				response.getWriter().println(new Gson().toJson(result));
			}
		} catch (Exception ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			JsonObject result = new JsonObject();
			String errorCode = "E173";
			String errorMsg = properties.getProperty(errorCode);
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg + ". " + ex.getClass() + ":" + ex.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtils commonUtils = new CommonUtils();
		String inputPayload = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "", id = "";
		JsonObject jsoninput = new JsonObject();
		JsonParser parser = new JsonParser();
		JsonObject scfObj = new JsonObject();
		boolean debug = false;
		try {
			inputPayload = commonUtils.getGetBody(request, response);
			jsoninput = (JsonObject) parser.parse(inputPayload);
			if (jsoninput.has("debug") && jsoninput.get("debug").getAsString().equalsIgnoreCase("true")) {
				debug = true;
			}

			if (debug) {
				response.getWriter().println("Input Payload " + jsoninput);
			}
			if (jsoninput.has("ID") && !jsoninput.get("ID").getAsString().equalsIgnoreCase("")) {
				id = jsoninput.get("ID").getAsString();
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userPass = userName + ":" + password;
				if (debug) {
					response.getWriter().println("Odata URL " + oDataUrl);
					response.getWriter().println("User Name " + userName);
					response.getWriter().println("Password " + password);
				}
				executeURL = oDataUrl + "SupplyChainFinances?$filter=ID%20eq%20%27" + id + "%27";
				if (debug) {
					response.getWriter().println("executeURL " + executeURL);
				}
				scfObj = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("SCF Delete " + executeURL);
				}
				if (scfObj != null && scfObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
					executeURL = oDataUrl + "SupplyChainFinances('" + id + "')";
					JsonObject scfDeleteRecord = commonUtils.executeDelete(executeURL, userPass, response, request,
							debug, "PYGWHANA");
					if (debug) {
						response.getWriter().println("Deleted SCF Record Response  " + scfDeleteRecord);
					}
					if (scfDeleteRecord.has("ErrorCode")
							&& scfDeleteRecord.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
						JsonObject res = new JsonObject();
						res.addProperty("Message", "Record Deleted Successfully");
						res.addProperty("ErrorCode", "");
						res.addProperty("Status", "000001");
						response.getWriter().println(res);
					} else {
						response.getWriter().println(scfDeleteRecord);
					}

				}else{
					JsonObject result = new JsonObject();
					result.addProperty("ErrorCode", "J001");
					result.addProperty("Status", "000002");
					result.addProperty("Message", id + " SCF ID is Not Exist");
					response.getWriter().println(new Gson().toJson(result));
				}

			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonObject result = new JsonObject();
				result.addProperty("ErrorCode", "J001");
				result.addProperty("Status", "000002");
				result.addProperty("Message", "ID is Mandatory for Deleting");
				response.getWriter().println(new Gson().toJson(result));

			}

		} catch (Exception ex) {
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			if (debug)
				response.getWriter().print(new Gson().toJson("Full Stack Trace:" + buffer.toString()));
			JsonObject result = new JsonObject();
			String errorCode = "E173";
			String errorMsg = properties.getProperty(errorCode);
			result.addProperty("errorCode", errorCode);
			result.addProperty("Message", errorMsg + ". " + ex.getClass() + ":" + ex.getMessage());
			result.addProperty("Status", properties.getProperty("ErrorStatus"));
			result.addProperty("Valid", "false");
			response.getWriter().println(new Gson().toJson(result));
		}

	}

	@Bean
	public ServletRegistrationBean<SCFUpdate> SCFUpdateServletBean() {
		ServletRegistrationBean<SCFUpdate> bean = new ServletRegistrationBean<>(new SCFUpdate(), "/SCFUpdate");
		return bean;
	}
}

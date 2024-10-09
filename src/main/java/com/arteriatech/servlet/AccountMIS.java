package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingClient;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationTokenExchangeStrategy;

import io.vavr.control.Try;

@Configuration
public class AccountMIS extends HttpServlet {

    CommonUtils commonUtils = new CommonUtils();
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        boolean debug = false;
        HashMap<String, String> inputForScfCorpDealerMap = new HashMap<>();
		// ArrayListMultimap<String, String> inputForScfCorpDealerMap = ArrayListMultimap.create();	
        String pygwUrl ="", pygwUserName ="", pygwPassWord ="",pygwUserPass ="";
        try {

            if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
                debug = true;
            }

            //  pygwUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
            //  pygwUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
            //  pygwPassWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
            //  pygwUserPass = pygwUserName + pygwPassWord;

            pygwUrl = "https://devci9yqyi812.hana.ondemand.com/ARTEC/PYGW/service.xsodata/";
            pygwUserPass = "P000000:DevCFHNDBP@$$wdFeb2024";

            JsonObject sccnfgEntries = getSccNfgEntries(resp, pygwUrl , pygwUserPass , debug);
            if (debug) {
                resp.getWriter().println("sccnfgEntries  :" + sccnfgEntries);
            }

            JsonObject aggregatorsEntries = getAggregatorsEntries(resp, pygwUrl , pygwUserPass , debug);
            if (debug) {
                resp.getWriter().println("aggregatorsEntries  :" + aggregatorsEntries);
            }

            inputForScfCorpDealerMap = getMapForInputToScfCorpDealer(resp, sccnfgEntries, aggregatorsEntries,debug);
            if (debug) {
                resp.getWriter().println("inputsForScfCorpDealer  :" + inputForScfCorpDealerMap);
            }

            JsonArray scfCorpDealerResponse = callScfCorpDealer(resp, inputForScfCorpDealerMap, debug);
            if (debug) {
                resp.getWriter().println("scfCorpDealerResponse  :" + scfCorpDealerResponse);
            }

            JsonObject scfEntriesAfterRemovingInvalidEntries = getScfEntriesByremovingInvalidEntries(resp, pygwUrl, pygwUserPass, debug);
            if (debug) {
                resp.getWriter().println("scfEntriesAfterRemovingInvalidEntries " + scfEntriesAfterRemovingInvalidEntries);
            }

            JsonObject bpEntriesAfterRemovingInvalidEntries = getBpEntriesAfterRemovingInvalidEntries(resp, pygwUrl, pygwUserPass, debug);
            if (debug) {
                resp.getWriter().println("bpEntriesAfterRemovingInvalidEntries " + bpEntriesAfterRemovingInvalidEntries);
            }

            JsonObject scfEntriesAfterConcatination = concatinateFieldsinScf(resp, scfEntriesAfterRemovingInvalidEntries, debug);
            if (debug) {
            resp.getWriter().println("scfEntriesAfterConcatination"+scfEntriesAfterConcatination);
            }

            JsonObject bpEntriesAfterConcatination = concatinateFieldsinBP(resp, bpEntriesAfterRemovingInvalidEntries, debug);
            if (debug) { 
            resp.getWriter().println("bpEntriesAfterConcatination "+bpEntriesAfterConcatination);
            }

            JsonArray OnboardedOtherFacilitiesEntries = pushMatchedRecordsToOnoboardOtherfacility(resp ,scfEntriesAfterConcatination, bpEntriesAfterConcatination ,debug);
            if (debug) { 
            resp.getWriter().println("OnboardedOtherFacilitiesEntries "+OnboardedOtherFacilitiesEntries);
            }

            JsonObject  scfObjectAfterRemovingonoboardfacilities = removeScfEntriesAfterinsertToOnboardOtherFacilities(resp, OnboardedOtherFacilitiesEntries, scfEntriesAfterConcatination, debug);
            if (debug) { 
            resp.getWriter().println("scfObjectAfterRemovingonoboardfacilities  : "+scfObjectAfterRemovingonoboardfacilities);
            }

            JsonObject scfResponseAfterRemovingingByCreatedOn = removeEntriesByCreateOn(resp, scfObjectAfterRemovingonoboardfacilities , debug);
            if (debug) { 
            resp.getWriter().println("scfResponseAfterRemovingingByCreatedOn " +scfResponseAfterRemovingingByCreatedOn);
            } 

            JsonArray sortedScfEntryByCreatedOn = sortByCreatedOn(resp, scfEntriesAfterConcatination , debug);
            if (debug) { 
            resp.getWriter().println("sortedScfEntryByCreatedOn "+sortedScfEntryByCreatedOn);
            }

            JsonArray scfEntriesWithUniqueAccNo = removeDuplicateAccNo(resp, sortedScfEntryByCreatedOn , debug);
            if (debug) { 
                resp.getWriter().println("scfEntriesWithUniqueAccNo "+scfEntriesWithUniqueAccNo);
            }

            Workbook workbook = new XSSFWorkbook();
		// // Create a blank sheet

		createXlSheetOfScfCorpDealers(resp ,workbook, scfCorpDealerResponse , debug);
        createBpSheet(resp , workbook, bpEntriesAfterConcatination , debug);
		createScfSheet(resp , workbook , scfEntriesWithUniqueAccNo , debug);
		createOnboardotherfacilitiesSheet(resp , workbook ,OnboardedOtherFacilitiesEntries , debug);
		createSCFVendorAccountsSheet(resp ,workbook, scfEntriesWithUniqueAccNo, debug);
		createSCFDealerAccountsSheet(resp ,workbook, scfEntriesWithUniqueAccNo, debug);
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		resp.setHeader("Content-Disposition", "attachment;filename=sample.xlsx");
		// Write the workbook content to the response stream
		workbook.write(resp.getOutputStream());
		workbook.close();
        }catch(Exception exception) {
            JsonObject result = new JsonObject();
            result.addProperty("Exception", exception.getClass().getCanonicalName());
            result.addProperty("Message", exception.getClass().getCanonicalName() + "--->" + exception.getMessage());

            if (debug) {
                StackTraceElement element[] = exception.getStackTrace();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < element.length; i++) {
                    buffer.append(element[i]);
                }
                resp.getWriter().println(new Gson().toJson(result));
                resp.getWriter().println("doGet-Exception Stack Trace: " + buffer.toString());
            }
        }
    }

	public void createSCFVendorAccountsSheet(HttpServletResponse resp ,Workbook workbook, JsonArray jsonObject, boolean debug) throws IOException {
     try{
		Sheet sheet = workbook.createSheet("VBDSheet");
		JsonArray bpArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		bpArray = jsonObject;

		JsonObject firstElement = bpArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 1; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= bpArray.size() - 1; j++) {
			Row valuesOfBp = sheet.createRow(rowNum);
			JsonObject onboardObject = bpArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			if (onboardObject.has("CPTypeID") && !onboardObject.get("CPTypeID").isJsonNull()
					&& onboardObject.get("CPTypeID").getAsString().equalsIgnoreCase("60")) {
				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			}
		}
	}catch(Exception ex){
        JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "createSCFVendorAccountsSheet()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
	}
	}

	public void createSCFDealerAccountsSheet(HttpServletResponse resp ,Workbook workbook, JsonArray jsonObject, boolean debug)
			throws ClientProtocolException, IOException {
    try{
		CloseableHttpClient httpClient = null;
		String userpass = "P000000:DevCFHNDBP@$$wdFeb2024";
		Sheet sheet = workbook.createSheet("SCFDealerAccounts");
		JsonArray bpArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		bpArray = jsonObject;

		JsonObject firstElement = bpArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 1; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= bpArray.size() - 1; j++) {
			Row valuesOfBp = sheet.createRow(rowNum);
			JsonObject onboardObject = bpArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			if ((onboardObject.has("CPTypeID") && !onboardObject.get("CPTypeID").isJsonNull()
					&& onboardObject.get("CPTypeID").getAsString().equalsIgnoreCase("01"))
					&& (onboardObject.get("AccountNo").getAsString().startsWith("010205") ||
							onboardObject.get("AccountNo").getAsString().startsWith("10205"))) {

				if (onboardObject.get("AccountNo").getAsString().startsWith("10205")) {
					String accountNo = onboardObject.get("AccountNo").getAsString();
					accountNo = "0" + accountNo;
					onboardObject.addProperty("AccountNo", accountNo);
				}

				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			} 
		}
	}catch(Exception ex){
        JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "createSCFDealerAccountsSheet()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
	}
	}

	private void createOnboardotherfacilitiesSheet(HttpServletResponse resp ,Workbook workbook, JsonArray onboardedOtherFacilitiesEntries, boolean debug) throws IOException {
    try{
		Sheet sheet = workbook.createSheet("OnboardedOtherFacilities Sheet");

		Row headerRow = sheet.createRow(0);

		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		int i=0;
		for (String field : fields) {
		Cell headerCells = headerRow.createCell(i);
				headerCells.setCellValue(field);
				i++;
		}
		if (onboardedOtherFacilitiesEntries.size() > 0) {

			JsonArray onboardArray = onboardedOtherFacilitiesEntries;

			JsonObject firstElement = onboardArray.get(0).getAsJsonObject();
			firstElement.remove("__metadata");
			String arr[] = getHeaders(firstElement);

			//  for (int i = 0; i <= arr.length - 1; i++) {
			//  	Cell headerCells = headerRow.createCell(i);
			//  	headerCells.setCellValue(arr[i]);
			//  }

			int rowNum = 1;

			for (int j = 0; j <= onboardArray.size() - 1; j++) {
				Row valuesOfBp = sheet.createRow(rowNum);
				JsonObject onboardObject = onboardArray.get(j).getAsJsonObject();
				Cell rowCells = null;
				int cellNumber = 0;

				for (String field : fields) {
					if (onboardObject.has(field) && !onboardObject.get(field).isJsonNull()) {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue(onboardObject.get(field).getAsString());
					} else {
						rowCells = valuesOfBp.createCell(cellNumber);
						cellNumber++;
						rowCells.setCellValue("");
					}
				}
				rowNum++;
			}
		}
	 }catch(Exception ex){
        JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "createScfSheet()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
	}
	}

	public void createScfSheet(HttpServletResponse resp , Workbook workbook, JsonArray scfObjectWithUniqueAccNo , boolean debug) throws IOException {
		try{
		Sheet sheet = workbook.createSheet("ScfSheet");
		JsonArray scfArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {
				"ID", "CPGUID", "CPTypeID", "AggregatorID", "OfferAmt", "OfferTenure",
				"Rate", "AccountNo", "NoOfChequeReturns", "PaymentDelayDays12Months",
				"BusinessVintageOfDealer", "PurchasesOf12Months", "DealersOverallScoreByCorp",
				"CorpRating", "DealerVendorFlag", "ConstitutionType", "MaxLimitPerCorp",
				"salesOf12Months", "Currency", "StatusID", "MCLR6Rate", "InterestRateSpread",
				"TenorOfPayment", "ADDLNPRDINTRateSP", "AddlnTenorOfPymt", "DefIntSpread",
				"ProcessingFee", "ECustomerID", "EContractID", "ApplicationNo",
				"CallBackStatus", "ECompleteDate", "ECompleteTime", "ApplicantID",
				"LimitPrefix", "InterestSpread", "DDBActive", "ProcessFeePerc", "ValidTo",
				"FacilityType", "CreatedBy", "CreatedAt", "CreatedOn", "ChangedBy",
				"ChangedAt", "ChangedOn", "Source", "SourceReferenceID"
		};

		scfArray = scfObjectWithUniqueAccNo;

		JsonObject firstElement = scfArray.get(0).getAsJsonObject();
		firstElement.remove("__metadata");
		String arr[] = getHeaders(firstElement);

		for (int i = 0; i <= arr.length - 2; i++) {
			Cell headerCells = headerRow.createCell(i);
			headerCells.setCellValue(arr[i]);
		}

		int rowNum = 1;

		for (int j = 0; j <= scfArray.size() - 1; j++) {
			Row valuesOfScf = sheet.createRow(rowNum);
			JsonObject scfObject = scfArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			int cellNumber = 0;

			for (String field : fields) {
				if (scfObject.has(field) && !scfObject.get(field).isJsonNull()) {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(scfObject.get(field).getAsString());
				} else {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}
			rowNum++;
		}
	}catch(Exception ex){
		JsonObject result = new JsonObject();
		StackTraceElement element[] = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < element.length; i++) {
			buffer.append(element[i]);
		}
		result.addProperty("Exception", ex.getClass().getCanonicalName());
		result.addProperty("Method", "createScfSheet()");
		result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
		result.addProperty("Status", "000002");
		result.addProperty("ErrorCode", "J002");
		resp.getWriter().println(result);
	}
	}

    public void createBpSheet(HttpServletResponse resp , Workbook workbook, JsonObject bpEntriesAfterConcatination , boolean debug) throws IOException {

        try{
		Sheet sheet = workbook.createSheet("BPSheet");

		String[] keys = {
		"Address1", "Address2", "Address3", "Address4", "AggregatorID", "ChangedAt", "ChangedBy", "ChangedOn",
		"CreatedAt", "CreatedBy", "CreatedOn", "Source", "City", "Country", "CountryDesc", "CPGuid", "CPName",
		"CPType", "District", "EmailID", "GSTIN", "BPGuid", "IncorporationDate", "LandLine1", "LegalStatus",
		"Mobile1", "Mobile2", "PAN", "Pincode", "State", "StateDesc", "StatusID", "UtilDistrict", "ApproverRemarks",
		"SourceReferenceID", "FundBasedExposure", "HgdFrgnCurrExposure", "MSME", "NonFundBasedExposure",
		"TotalBankingExposure", "TotalFrgnCurrExposure", "UnHgdFrgnCurrExposure", "UdyamRegNo",
		"CorporateIdentificationNo", "BPRejectionRemarks", "FacilityType", "EntityID", "EntityType", "LEINumber",
		"ParentName", "ParentNo", "ParentType", "URCActivityType", "URCDocURL", "URCEntityType", "URCSectorCode",
		"URCSubSectorCode", "ODAccountNo", "URCRegistrationDate", "ERPCPName", "Testrun", "UtilDistrictDs",
		"LegalStatusDs", "LoginID" 
		// ,"ConcatinatedValues"
         };
		Row headerRow = sheet.createRow(0);

		if (bpEntriesAfterConcatination.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

			JsonArray bpArray = bpEntriesAfterConcatination.get("d").getAsJsonObject().get("results").getAsJsonArray();

			JsonObject firstElement = bpArray.get(0).getAsJsonObject();
			firstElement.remove("__metadata");
			String arr[] = getHeaders(firstElement);

			for (int i = 0; i <= arr.length - 2; i++) {
				Cell headerCells = headerRow.createCell(i);
				if(!arr[i].equalsIgnoreCase("BPContactPersons")){
				headerCells.setCellValue(arr[i]);
				}
			}

			int rowNum = 1;

			for (int j = 0; j <= bpArray.size() - 1; j++) {
				Row valuesOfBp = sheet.createRow(rowNum);
				JsonObject bpObject = bpArray.get(j).getAsJsonObject();
				Cell rowCells = null;
				int cellNumber = 0;

				for (int i = 0; i <= keys.length - 1; i++) {
				if (bpObject.has(keys[i]) && !bpObject.get(keys[i]).isJsonNull()) {
					rowCells = valuesOfBp.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(bpObject.get(keys[i]).getAsString());
				} else {
					rowCells = valuesOfBp.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}
			rowNum++;
			}
		}
      }catch(Exception ex){
        JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "createBpSheet()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
	}
	}

    public void createXlSheetOfScfCorpDealers(HttpServletResponse resp,Workbook workbook, JsonArray scfCorpDealerResponse , boolean debug) throws IOException{
		try{
		Sheet sheet = workbook.createSheet("SCFCorpDealerOutstandingSheet");
		JsonArray onBoardOtherFacilitiesArray = new JsonArray();
		Row headerRow = sheet.createRow(0);
		String[] fields = {"ODAccountNumber","CustomerID","DealerName","DealerLimit","DealerExpiryDate",
							"PeakLimit", "TODLimit", "NoOfDisbursement", "TotalDisbursementExcludingInterest",
							"RepaymentAmount", "BalanceOutstanding", "UnutilizedLimit", "NormalDues",
							"OverdueWithinCure", "OverdueBeyondCure", "OverdueBetween0To7Days",
							"OverdueBetween7To14Days", "OverdueBetween14To21Days", "OverdueBetween21To28Days",
							"OverdueBetween28To60Days","OverdueBeyond60Days","City","IsAccountFrozen",
							"DealerCode", "Currency"};
             
			onBoardOtherFacilitiesArray = scfCorpDealerResponse;

			JsonObject firstElement = onBoardOtherFacilitiesArray.get(0).getAsJsonObject();
			JsonArray firstArray = firstElement.getAsJsonArray("results");
		
			firstElement = firstArray.get(0).getAsJsonObject();
			String arr[] = getHeaders(firstElement);

			for(int i=0;i<=3;i++){
				Cell headerCells = headerRow.createCell(i);
				if(i==0){
				headerCells.setCellValue("AggregatorID");
				}else if(i==1){
					headerCells.setCellValue("AggregatorName");
					}else if(i==2){
						headerCells.setCellValue("ParentLimitID");
						}else if(i==3){
							headerCells.setCellValue("ParentLimitPrefixHistory");
							}
			}

			//with status , msg , respons i =0 
			// without i = 3
			int q = 4;
			for (int i = 3; i <= arr.length - 5; i++) {
				Cell headerCells = headerRow.createCell(q);
				headerCells.setCellValue(arr[i]);
				q++;
			}	

		int rowNum = 1;
		
		for (int z = 0; z <= onBoardOtherFacilitiesArray.size() - 1; z++) {
			firstElement = onBoardOtherFacilitiesArray.get(z).getAsJsonObject();
			 firstArray = firstElement.getAsJsonArray("results");
		for (int j = 0; j <= firstArray.size() - 1; j++) {
			Row valuesOfScf = sheet.createRow(rowNum);
			JsonObject scfObject = firstArray.get(j).getAsJsonObject();
			Cell rowCells = null;
			// with status , msg , respons cellNumber =7 
			// without cellNumber = 4
			int cellNumber = 4;

			if (scfObject.has("AggregatorID") && !scfObject.get("AggregatorID").isJsonNull()) {
				rowCells = valuesOfScf.createCell(0);
				rowCells.setCellValue(scfObject.get("AggregatorID").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(0);
				rowCells.setCellValue("");
			}

			if (scfObject.has("AggregatorName") && !scfObject.get("AggregatorName").isJsonNull()) {
				rowCells = valuesOfScf.createCell(1);
				rowCells.setCellValue(scfObject.get("AggregatorName").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(1);
				rowCells.setCellValue("");
			}

			if (scfObject.has("ParentLimitID") && !scfObject.get("ParentLimitID").isJsonNull()) {
				rowCells = valuesOfScf.createCell(2);
				rowCells.setCellValue(scfObject.get("ParentLimitID").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(2);
				rowCells.setCellValue("");
			}

			if (scfObject.has("ParentLimitPrefixHistory") && !scfObject.get("ParentLimitPrefixHistory").isJsonNull()) {
				rowCells = valuesOfScf.createCell(3);
				rowCells.setCellValue(scfObject.get("ParentLimitPrefixHistory").getAsString());
			} else {
				rowCells = valuesOfScf.createCell(3);
				rowCells.setCellValue("");
			}

			// if (scfObject.has("Status") && !scfObject.get("Status").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(4);
			// 	rowCells.setCellValue(scfObject.get("Status").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(4);
			// 	rowCells.setCellValue("");
			// }

			// if (scfObject.has("ResponseCode") && !scfObject.get("ResponseCode").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(5);
			// 	rowCells.setCellValue(scfObject.get("ResponseCode").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(5);
			// 	rowCells.setCellValue("");
			// }

			// if (scfObject.has("Message") && !scfObject.get("Message").isJsonNull()) {
			// 	rowCells = valuesOfScf.createCell(6);
			// 	rowCells.setCellValue(scfObject.get("Message").getAsString());
			// } else {
			// 	rowCells = valuesOfScf.createCell(6);
			// 	rowCells.setCellValue("");
			// }

			for (String field : fields) {
				if (scfObject.has(field) && !scfObject.get(field).isJsonNull()) {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue(scfObject.get(field).getAsString());
				} else {
					rowCells = valuesOfScf.createCell(cellNumber);
					cellNumber++;
					rowCells.setCellValue("");
				}
			}
			rowNum++;
            }
        }
        }catch(Exception ex){
			JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "createXlSheetOfScfCorpDealers()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
        }
	}

    public JsonArray removeDuplicateAccNo(HttpServletResponse response ,JsonArray sortedScfEntryByCreatedOn , boolean debug)
			throws ClientProtocolException, IOException {
		try{
		Set<String> seenAccountNos = new HashSet<>();

		Iterator<JsonElement> iterator = sortedScfEntryByCreatedOn.iterator();
					while (iterator.hasNext()) {
						JsonObject scfObj = iterator.next().getAsJsonObject();
			
			String accountNo = scfObj.get("AccountNo").getAsString();

			if (!seenAccountNos.contains(accountNo)) {
				seenAccountNos.add(accountNo);
			}else{
				iterator.remove();
			}
		}
    }catch(Exception ex){
        JsonObject result = new JsonObject();
		StackTraceElement element[] = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < element.length; i++) {
			buffer.append(element[i]);
		}
		result.addProperty("Exception", ex.getClass().getCanonicalName());
		result.addProperty("Method", "removeDuplicateAccNo()");
		result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
		result.addProperty("Status", "000002");
		result.addProperty("ErrorCode", "J002");
		response.getWriter().println(result);
	}
    return sortedScfEntryByCreatedOn;
}

    public JsonArray sortByCreatedOn(HttpServletResponse response , JsonObject scfEntry , boolean debug) throws IOException {
        JsonArray jsonArray = new JsonArray();
        try{
		if (scfEntry.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
			JsonArray bpResponseArray = scfEntry.get("d").getAsJsonObject().get("results")
					.getAsJsonArray();
			List<JsonObject> jsonObjectList = new ArrayList<>();
			for (JsonElement element : bpResponseArray) {
				jsonObjectList.add(element.getAsJsonObject());
			}

			// Sort the list based on CreatedOn field (from newest to oldest)
			jsonObjectList.sort((o1, o2) -> {

				JsonElement createdOnElement1 = o1.get("CreatedOn");
                JsonElement createdOnElement2 = o2.get("CreatedOn");
 
                // Handle null cases
                if (createdOnElement1 == null || createdOnElement1.isJsonNull()) {
                    return 1; // Place o1 after o2
                }
                if (createdOnElement2 == null || createdOnElement2.isJsonNull()) {
                    return -1; // Place o2 after o1
                }
 
                long createdOn1 = Long.parseLong(createdOnElement1.getAsString().replaceAll("[^\\d]", ""));
                long createdOn2 = Long.parseLong(createdOnElement2.getAsString().replaceAll("[^\\d]", ""));

				// long createdOn1 = Long.parseLong(o1.get("CreatedOn").getAsString().replaceAll("[^\\d]", ""));
				// long createdOn2 = Long.parseLong(o2.get("CreatedOn").getAsString().replaceAll("[^\\d]", ""));
				return Long.compare(createdOn2, createdOn1);
			});

			for (JsonObject jsonObject : jsonObjectList) {
				jsonArray.add(jsonObject);
			}
		}
    }catch(Exception ex){
		JsonObject result = new JsonObject();
		StackTraceElement element[] = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < element.length; i++) {
			buffer.append(element[i]);
		}
		result.addProperty("Exception", ex.getClass().getCanonicalName());
		result.addProperty("Method", "sortByCreatedOn()");
		result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
		result.addProperty("Status", "000002");
		result.addProperty("ErrorCode", "J002");
		response.getWriter().println(result);
    }
    return jsonArray;
	}

    public JsonObject removeEntriesByCreateOn(HttpServletResponse resp, JsonObject scfEntriesAfterConcatination,
			boolean debug) throws ClientProtocolException, IOException {
				try{
						if(scfEntriesAfterConcatination.size()>0){
						JsonArray scfArray = scfEntriesAfterConcatination.get("d").getAsJsonObject().get("results").getAsJsonArray();;

						long cutoffDate = 1712294400000L;
						Iterator<JsonElement> iterator = scfArray.iterator();
						while (iterator.hasNext()) {
							JsonObject scfObj = iterator.next().getAsJsonObject();

							if (scfObj.has("CreatedOn") && !scfObj.get("CreatedOn").isJsonNull() && scfObj.has("CreatedBy") && !scfObj.get("CreatedBy").isJsonNull()) {
								String createdOn = scfObj.get("CreatedOn").getAsString();
								String createdBy = scfObj.get("CreatedBy").getAsString();
			
								long createdOnAfterRemovingTimeStamp = Long.parseLong(createdOn.replaceAll("[^\\d]", ""));
								
								if (createdOnAfterRemovingTimeStamp < cutoffDate|| (createdOnAfterRemovingTimeStamp > cutoffDate  && ( createdBy.equalsIgnoreCase("P000024")  || createdBy.equalsIgnoreCase("P000159")))){
							
								}else{
									iterator.remove();
								}

								// if (!(createdOnAfterRemovingTimeStamp < cutoffDate) && !(createdBy.equalsIgnoreCase("P000024") || createdBy.equalsIgnoreCase("P000159"))) {
								// 	iterator.remove();
								// }
							}
						}
					}
				}catch(Exception ex){
					JsonObject result = new JsonObject();
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				result.addProperty("Exception", ex.getClass().getCanonicalName());
				result.addProperty("Method", "removeEntriesByCreateOn()");
				result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "J002");
				resp.getWriter().println(result);
				}
				return scfEntriesAfterConcatination;
	}

    private String[] getHeaders(JsonObject jsonObject) {
		return jsonObject.keySet().toArray(new String[0]);
	}

    public JsonArray pushMatchedRecordsToOnoboardOtherfacility(HttpServletResponse resp , JsonObject scfRecords, JsonObject bpRecords ,boolean debug)throws IOException {
        JsonArray OnboardedOtherFacilitiesEntries = new JsonArray();
		try{
			JsonObject scfEntry = new JsonObject();
			JsonObject bpEntry = new JsonObject();
			if (scfRecords.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

				JsonArray scfResponseArray = scfRecords.get("d").getAsJsonObject().get("results").getAsJsonArray();

				if (bpRecords.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

					JsonArray bpResponseArray = bpRecords.get("d").getAsJsonObject().get("results").getAsJsonArray();
					for (int i = 0; i < bpResponseArray.size(); i++) {
						bpEntry = bpResponseArray.get(i).getAsJsonObject();

						for (int j = 0; j < scfResponseArray.size(); j++) {
							scfEntry = scfResponseArray.get(j).getAsJsonObject();
							if (bpEntry.get("ConcatinatedValues").getAsString()
									.equalsIgnoreCase(scfEntry.get("ConcatinatedValues").getAsString())) {
								OnboardedOtherFacilitiesEntries.add(scfEntry);
							}
						}
					}
				}
			}
			if (debug) {
				resp.getWriter().println("OnboardedOtherFacilitiesEntries :"+OnboardedOtherFacilitiesEntries);
			}
        } catch(Exception ex){
            JsonObject result = new JsonObject();
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				result.addProperty("Exception", ex.getClass().getCanonicalName());
				result.addProperty("Method", "pushMatchedRecordsToOnoboardOtherfacility()");
				result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "J002");
				resp.getWriter().println(result);
        }
		return OnboardedOtherFacilitiesEntries;
	}

    public JsonObject concatinateFieldsinBP(HttpServletResponse resp,JsonObject bpEntries, boolean debug)throws IOException {
        try{
            JsonObject bpEntry = new JsonObject();
            if (bpEntries.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
    
                JsonArray bpResponseArray = bpEntries.get("d").getAsJsonObject().get("results").getAsJsonArray();
                for (int j = 0; j < bpResponseArray.size(); j++) {
                    bpEntry = bpResponseArray.get(j).getAsJsonObject();
    
                    String aggregatorID = bpEntry.has("AggregatorID") && !bpEntry.get("AggregatorID").isJsonNull()
                            ? bpEntry.get("AggregatorID").getAsString()
                            : "";
                    String cpGUID = bpEntry.has("CPGuid") && !bpEntry.get("CPGuid").isJsonNull()
                            ? bpEntry.get("CPGuid").getAsString()
                            : "";
                    String cpTypeID = bpEntry.has("CPType") && !bpEntry.get("CPType").isJsonNull()
                            ? bpEntry.get("CPType").getAsString()
                            : "";
                    String ConcatinatedId = aggregatorID + cpGUID + cpTypeID;
    
                    bpEntry.addProperty("ConcatinatedValues", ConcatinatedId);
                }
            }
        }catch(Exception ex){
            JsonObject result = new JsonObject();
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				result.addProperty("Exception", ex.getClass().getCanonicalName());
				result.addProperty("Method", "concatinateFieldsinBP()");
				result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "J002");
				resp.getWriter().println(result);
        }
		return bpEntries;
	}

    public JsonObject concatinateFieldsinScf(HttpServletResponse resp, JsonObject scfEntries, boolean debug) throws IOException {  
            try{
                JsonObject scfEntry = new JsonObject();
                if (scfEntries.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
        
                    JsonArray scfResponseArray = scfEntries.get("d").getAsJsonObject().get("results").getAsJsonArray();
                    for (int j = 0; j < scfResponseArray.size(); j++) {
                        scfEntry = scfResponseArray.get(j).getAsJsonObject();
        
                        String aggregatorID = scfEntry.has("AggregatorID") && !scfEntry.get("AggregatorID").isJsonNull()
                                ? scfEntry.get("AggregatorID").getAsString()
                                : "";
                        String cpGUID = scfEntry.has("CPGUID") && !scfEntry.get("CPGUID").isJsonNull()
                                ? scfEntry.get("CPGUID").getAsString()
                                : "";
                        String cpTypeID = scfEntry.has("CPTypeID") && !scfEntry.get("CPTypeID").isJsonNull()
                                ? scfEntry.get("CPTypeID").getAsString()
                                : "";
                        String ConcatinatedId = aggregatorID + cpGUID + cpTypeID;
        
                        scfEntry.addProperty("ConcatinatedValues", ConcatinatedId);
                    }
                }
            }catch(Exception ex){
				JsonObject result = new JsonObject();
				StackTraceElement element[] = ex.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < element.length; i++) {
					buffer.append(element[i]);
				}
				result.addProperty("Exception", ex.getClass().getCanonicalName());
				result.addProperty("Method", "concatinateFieldsinScf()");
				result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
				result.addProperty("Status", "000002");
				result.addProperty("ErrorCode", "J002");
				resp.getWriter().println(result);
            }
			return scfEntries;
	}

    public JsonObject removeScfEntriesAfterinsertToOnboardOtherFacilities(HttpServletResponse resp,
    JsonArray OnboardedOtherFacilitiesEntries, JsonObject scfObject, boolean debug) throws ClientProtocolException, IOException{
        try {
            JsonArray scfArray = scfObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
            if(OnboardedOtherFacilitiesEntries.size()>0  && scfArray.size()>0 ){
            Set<String> idsToRemove = new HashSet<>();
            for (JsonElement element : OnboardedOtherFacilitiesEntries) {
                JsonObject obj = element.getAsJsonObject();
                idsToRemove.add(obj.get("ID").getAsString());
            }

            Iterator<JsonElement> iterator = scfArray.iterator();
            while (iterator.hasNext()) {
                JsonObject arrayObject = iterator.next().getAsJsonObject();
                String id = arrayObject.get("ID").getAsString();
                if (idsToRemove.contains(id)) {
                    iterator.remove();
                }
            }
        }
        // if (OnboardedOtherFacilitiesEntries.size() > 0 && scfObject.size() > 0) {

        // 	JsonArray scfResponseArray = scfObject.get("d").getAsJsonObject().get("results").getAsJsonArray();

        // 	for (int i = 0; i <= OnboardedOtherFacilitiesEntries.size() - 1; i++) {
        // 		JsonObject OnboardedOtherFacilitiesEntriesObj = new JsonObject();
        // 		Iterator<JsonElement> iterator = scfResponseArray.iterator();
        // 		while (iterator.hasNext()) {
        // 			JsonObject scfObj = iterator.next().getAsJsonObject();
        // 			if (OnboardedOtherFacilitiesEntriesObj.get("ID").getAsString()
        // 					.equalsIgnoreCase(scfObj.get("ID").getAsString())) {
        // 				iterator.remove();

        // 			}
        // 		}
        // 	}
        // }
        }catch (Exception ex) {
			JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "removeScfEntriesAfterinsertToOnboardOtherFacilities()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
        }
		return scfObject;
	}

    public JsonObject getBpEntriesAfterRemovingInvalidEntries(HttpServletResponse response,String pygwUrl ,String pygwUserPass , boolean debug)
            throws ClientProtocolException, IOException {
			JsonObject bpResponse = new JsonObject();
		try {
            pygwUrl = pygwUrl + "BPHeaders?$filter=CPType%20eq%20%2760%27";
            bpResponse = executeURL(pygwUrl, pygwUserPass, response, debug);
    
            if (bpResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
                JsonArray bpResponseArray = bpResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
    
                Iterator<JsonElement> iterator = bpResponseArray.iterator();
                while (iterator.hasNext()) {
                    JsonObject bpObj = iterator.next().getAsJsonObject();
    
                    if ((bpObj.has("FacilityType") && bpObj.get("FacilityType").isJsonNull())
                            || bpObj.get("FacilityType").getAsString().equalsIgnoreCase("") ||
                            (bpObj.has("CPGuid")
                                    && !bpObj.get("CPGuid").isJsonNull() && bpObj.get("CPGuid").getAsString().contains("-"))
                            ||
                            (bpObj.has("CPType") && !bpObj.get("CPType").isJsonNull()
                                    && bpObj.get("CPType").getAsString().contains("-"))
                            ||
                            (bpObj.has("AggregatorID") && !bpObj.get("AggregatorID").isJsonNull()
                                    && bpObj.get("AggregatorID").getAsString().contains("-"))) {
                                // response.getWriter().println("removed");
                        iterator.remove();
                    } 
                }
            }
        } catch (Exception ex) {
            JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "getBpEntriesAfterRemovingInvalidEntries()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
        }
		return bpResponse;
    }

    public JsonObject getScfEntriesByremovingInvalidEntries(HttpServletResponse response, String pygwUrl ,String pygwUserPass ,boolean debug)
            throws IOException {
				String executeUrl = "";
				JsonObject scfResponse = new JsonObject();
        try {
            executeUrl = pygwUrl + "SupplyChainFinances";
            scfResponse = executeURL(executeUrl, pygwUserPass, response, debug);
            
			if (scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

				JsonArray scfResponseArray = scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray();
					Iterator<JsonElement> iterator = scfResponseArray.iterator();
				while (iterator.hasNext()) {
				JsonObject scfEntry = iterator.next().getAsJsonObject();
			
					if ((scfEntry.has("AccountNo") && scfEntry.get("AccountNo").isJsonNull())
							|| scfEntry.get("AccountNo").getAsString().equalsIgnoreCase("") 
							|| scfEntry.get("AccountNo").getAsString().contains("-")||
							(scfEntry.has("CPGUID") && !scfEntry.get("CPGUID").isJsonNull()
									&& scfEntry.get("CPGUID").getAsString().contains("-"))
							||
							(scfEntry.has("CPTypeID") && !scfEntry.get("CPTypeID").isJsonNull()
									&& scfEntry.get("CPTypeID").getAsString().contains("-"))
							||
							(scfEntry.has("AggregatorID") && !scfEntry.get("AggregatorID").isJsonNull()
									&& scfEntry.get("AggregatorID").getAsString().contains("-"))) {
										iterator.remove();
					}
				}
			}
        } catch (Exception ex) {
			JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "getScfEntriesByremovingInvalidEntries()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
        }
		return scfResponse;
    }

    public static String extractPrntLimits(HttpServletResponse resp , String input, int pipeCount) {
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
			}else if(pipeCount == 3){
					String[] elements = input.split("\\|");
					String lastElement = elements[elements.length - 1];
					return lastElement;
			}else{
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

    private JsonArray callScfCorpDealer( HttpServletResponse resp,
            HashMap<String, String> inputForScfCorpDealerMap, boolean debug) throws IOException {
        String aggregatorName = "";
		String prntLmtid = "DLR~BLUESTAR";
        String prntLmtPrx = "DLR~BLUESTAR";
		// String prntLmtid = "";
		// String prntLmtPrx = "";
        JsonObject scfCorpDealerResponse = new JsonObject();
        JsonArray scfCorpDealerOustStandingResponse = new JsonArray();
        SCFCorpDealerOutstandingClient client = new SCFCorpDealerOutstandingClient();
        try {
            for (String key : inputForScfCorpDealerMap.keySet()) {
                String prntLimitsvalue = inputForScfCorpDealerMap.get(key);
                int countofPipes = countPipeSymbols(prntLimitsvalue);
				aggregatorName = extractPrntLimits(resp ,prntLimitsvalue, 3);
                if (countofPipes == 1) {
                    // prntLmtid = extractPrntLimits(resp ,prntLimitsvalue, countofPipes);
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key, countofPipes , aggregatorName ,prntLmtid, debug);
					scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
				} else if (countofPipes == 2) {
                    // prntLmtid = extractPrntLimits(resp ,prntLimitsvalue, 1 );
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key , 1 , aggregatorName ,prntLmtid, debug);
                    scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
					// prntLmtPrx = extractPrntLimits(resp ,prntLimitsvalue, countofPipes);
                    scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, key ,countofPipes,aggregatorName, prntLmtPrx, debug);
                    scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
				}
            }
        } catch (Exception ex) {
		JsonObject result = new JsonObject();
		StackTraceElement element[] = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < element.length; i++) {
			buffer.append(element[i]);
		}
		result.addProperty("Exception", ex.getClass().getCanonicalName());
		result.addProperty("Method", "callScfCorpDealer()");
		result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
		result.addProperty("Status", "000002");
		result.addProperty("ErrorCode", "J002");
		resp.getWriter().println(result);
        }
	return scfCorpDealerOustStandingResponse;
    }

	// for arraylistmultimap ..
	// private JsonArray callScfCorpDealer(HttpServletResponse resp,ArrayListMultimap<String, String> inputForScfCorpDealerMap, boolean debug) throws IOException {
    //     String prntLmtid = "DLR~ARTTEST3";
    //     String prntLmtPrx = "DLR~ARTTEST3";
	// 	String aggregatorName = "";
	// 	// String prntLmtid = "";
	// 	// String prntLmtPrx = "";
    //     JsonObject scfCorpDealerResponse = new JsonObject();
    //     JsonArray scfCorpDealerOustStandingResponse = new JsonArray();
    //     SCFCorpDealerOutstandingClient client = new SCFCorpDealerOutstandingClient();
    //     try {
    //         for (Map.Entry<String, Collection<String>> entry : inputForScfCorpDealerMap.asMap().entrySet()) {
	// 			String keyFieldString = entry.getKey();
	// 			for (String value : entry.getValue()) {
    //             String prntLimitsvalue = value;
    //             int countofPipes = countPipeSymbols(prntLimitsvalue);
	// 			aggregatorName = extractPrntLimits(resp ,prntLimitsvalue, 3);
    //             if (countofPipes == 1) {
    //                 // prntLmtid = extractPrntLimits(prntLimitsvalue, countofPipes);
    //                 scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, value, countofPipes , aggregatorName ,prntLmtid, debug);
    //                 scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
	// 				// resp.getWriter().println("scfCorpDealerOustStandingResponse  "+scfCorpDealerOustStandingResponse);
    //             } else if (countofPipes == 2) {
    //                 // prntLmtid = extractPrntLimits(prntLimitsvalue, 1);
    //                 scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, value , 1 , aggregatorName ,prntLmtid, debug);
    //                 scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
    //                 // prntLmtPrx = extractPrntLimits(prntLimitsvalue, countofPipes);
    //                 scfCorpDealerResponse = client.getSCFCorpDealerOutstandingClient(resp, value ,countofPipes,aggregatorName, prntLmtPrx, debug);
    //                 scfCorpDealerOustStandingResponse.add(scfCorpDealerResponse);
	// 				// resp.getWriter().println("scfCorpDealerOustStandingResponse  "+scfCorpDealerOustStandingResponse);
    //             }
	// 		}
    //         }
    //     } catch (Exception ex) {
	// 		JsonObject result = new JsonObject();
	// 		StackTraceElement element[] = ex.getStackTrace();
	// 		StringBuffer buffer = new StringBuffer();
	// 		for (int i = 0; i < element.length; i++) {
	// 			buffer.append(element[i]);
	// 		}
	// 		result.addProperty("Exception", ex.getClass().getCanonicalName());
	// 		result.addProperty("Method", "callScfCorpDealer()");
	// 		result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
	// 		result.addProperty("Status", "000002");
	// 		result.addProperty("ErrorCode", "J002");
	// 		resp.getWriter().println(result);
    //     }
	// 	return scfCorpDealerOustStandingResponse;
    // }

    @SuppressWarnings("rawtypes")
    public HashMap getMapForInputToScfCorpDealer(HttpServletResponse resp, JsonObject sccnfgObj,JsonObject aggregatorsobj, boolean debug) throws IOException {
        HashMap<String, String> inputForScfCorpDealerMap = new HashMap<>();
        boolean isAggregatorIDmatched = false;
        String AggregatorName = "";
        try {
            if (sccnfgObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

                if (aggregatorsobj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

                    JsonArray sccnfgResponseArray = sccnfgObj.get("d").getAsJsonObject().get("results")
                            .getAsJsonArray();

                    JsonArray aggregatorsResponseArray = aggregatorsobj.get("d").getAsJsonObject().get("results")
                            .getAsJsonArray();

                    if (debug) {
                        resp.getWriter().println("sccnfgResponseArray :" + sccnfgResponseArray);
                        resp.getWriter().println("aggregatorsResponseArray :" + aggregatorsResponseArray);
                    }

                    for (int j = 0; j < sccnfgResponseArray.size(); j++) {
                        JsonObject sccNfgObj = sccnfgResponseArray.get(j).getAsJsonObject();

                        if (!sccNfgObj.get("AGGRID").isJsonNull()
                                && !sccNfgObj.get("AGGRID").getAsString().equalsIgnoreCase("")) {

                            for (int i = 0; i < aggregatorsResponseArray.size(); i++) {
                                JsonObject aggregatorObj = aggregatorsResponseArray.get(i).getAsJsonObject();

                                if (!aggregatorObj.get("AggregatorID").isJsonNull()
                                        && !aggregatorObj.get("AggregatorID").getAsString().equalsIgnoreCase("")) {

                                    if (sccNfgObj.get("AGGRID").getAsString()
                                            .equalsIgnoreCase(aggregatorObj.get("AggregatorID").getAsString())) {
                                        AggregatorName = aggregatorObj.get("AggregatorName").getAsString();
                                        isAggregatorIDmatched = true;
                                    } else {
                                        isAggregatorIDmatched = false;
                                    }

                                    if(isAggregatorIDmatched) {
                                        if (!sccNfgObj.get("PRNTLIMTIDPREFIX").isJsonNull()
                                                && !sccNfgObj.get("PRNTLIMTIDPREFIX").getAsString().equalsIgnoreCase("")) {

                                            if (!sccNfgObj.get("ParentLimitPrefixHistory").isJsonNull()
                                                    && !sccNfgObj.get("ParentLimitPrefixHistory").getAsString()
                                                            .equalsIgnoreCase("")) {

                                                inputForScfCorpDealerMap.put(sccNfgObj.get("AGGRID").getAsString(),
                                                        sccNfgObj.get("PRNTLIMTIDPREFIX").getAsString() + "|"
                                                                + sccNfgObj.get("ParentLimitPrefixHistory").getAsString()
                                                                + "|"
                                                                + AggregatorName);
                                            } else {
                                                inputForScfCorpDealerMap.put(sccNfgObj.get("AGGRID").getAsString(),
                                                        sccNfgObj.get("PRNTLIMTIDPREFIX").getAsString() + "|"
                                                                + AggregatorName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if(debug){
                    resp.getWriter().println("Aggregators Table dont have any records");
                    }
                }
            } else {
                if(debug){
                resp.getWriter().println("SCCNFG Table dont have any records based on CPType 60");
                }
            }
        } catch (Exception ex) {
            JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "getMapForInputToScfCorpDealer()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
        }
        if (debug) {
            resp.getWriter().println("inputForScfCorpDealerMap :" + inputForScfCorpDealerMap);
        }
        return inputForScfCorpDealerMap;
    }

	// for ArrayListMultimap
	// public ArrayListMultimap<String, String> getMapForInputToScfCorpDealer(HttpServletResponse resp, JsonObject sccnfgObj,JsonObject aggregatorsobj, boolean debug) throws IOException {
	// 	ArrayListMultimap<String, String> inputForScfCorpDealerMap = ArrayListMultimap.create();	
	// 	try{	
	// 	boolean isAggregatorIDmatched = false;

	// 	String AggregatorName = "";
	// 	if (sccnfgObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

	// 		if (aggregatorsobj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {

	// 			JsonArray sccnfgResponseArray = sccnfgObj.get("d").getAsJsonObject().get("results")
	// 					.getAsJsonArray();
	// 			JsonArray aggregatorsResponseArray = aggregatorsobj.get("d").getAsJsonObject().get("results")
	// 					.getAsJsonArray();

	// 			for (int j = 0; j < sccnfgResponseArray.size(); j++) {

	// 				JsonObject sccnfgObject = sccnfgResponseArray.get(j).getAsJsonObject();
	// 				if (!sccnfgObject.get("AGGRID").isJsonNull()
	// 						&& !sccnfgObject.get("AGGRID").getAsString().equalsIgnoreCase("")) {

	// 					for (int i = 0; i < aggregatorsResponseArray.size(); i++) {
	// 						JsonObject aggregatorObj = aggregatorsResponseArray.get(i).getAsJsonObject();

	// 						if (!aggregatorObj.get("AggregatorID").isJsonNull()
	// 								&& !aggregatorObj.get("AggregatorID").getAsString().equalsIgnoreCase("")) {

	// 							if (sccnfgObject.get("AGGRID").getAsString()
	// 									.equalsIgnoreCase(aggregatorObj.get("AggregatorID").getAsString())) {
	// 								AggregatorName = aggregatorObj.get("AggregatorName").getAsString();
	// 								isAggregatorIDmatched = true;
	// 							} else {
	// 								isAggregatorIDmatched = false;
	// 							}

	// 							if (isAggregatorIDmatched) {
	// 								if (!sccnfgObject.get("PRNTLIMTIDPREFIX").isJsonNull()
	// 										&& !sccnfgObject.get("PRNTLIMTIDPREFIX").getAsString().equalsIgnoreCase("")) {

	// 									if (!sccnfgObject.get("ParentLimitPrefixHistory").isJsonNull()
	// 											&& !sccnfgObject.get("ParentLimitPrefixHistory").getAsString()
	// 													.equalsIgnoreCase("")) {

	// 										inputForScfCorpDealerMap.put(sccnfgObject.get("AGGRID").getAsString(),
	// 												sccnfgObject.get("PRNTLIMTIDPREFIX").getAsString() + "|"
	// 														+ sccnfgObject.get("ParentLimitPrefixHistory").getAsString() + "|"
	// 														+ AggregatorName);
	// 									} else {
	// 										inputForScfCorpDealerMap.put(sccnfgObject.get("AGGRID").getAsString(),
	// 												sccnfgObject.get("PRNTLIMTIDPREFIX").getAsString() + "|"
	// 														+ AggregatorName);
	// 									}
	// 								}
	// 							}
	// 						}
	// 					}
	// 				}
	// 			}
	// 		} else {
	// 			if(debug){
	// 			resp.getWriter().println("Aggregators Table dont have any records");
	// 			}
	// 		}
	// 	} else {
	// 		if(debug){
	// 		resp.getWriter().println("SCCNFG Table dont have any records based on CPType 60");
	// 		}
	// 	}
	// }catch (Exception ex) {
	// 	JsonObject result = new JsonObject();
	// 	StackTraceElement element[] = ex.getStackTrace();
	// 	StringBuffer buffer = new StringBuffer();
	// 	for (int i = 0; i < element.length; i++) {
	// 		buffer.append(element[i]);
	// 	}
	// 	result.addProperty("Exception", ex.getClass().getCanonicalName());
	// 	result.addProperty("Method", "getMapForInputToScfCorpDealer()");
	// 	result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
	// 	result.addProperty("Status", "000002");
	// 	result.addProperty("ErrorCode", "J002");
	// 	resp.getWriter().println(result);
	// }
	// return inputForScfCorpDealerMap;
	// }

    public JsonObject getAggregatorsEntries(HttpServletResponse resp, String pygwUrl, String pygwUserPass,  boolean debug) throws IOException {
        JsonObject aggregatorsResponse = new JsonObject();
		try {
            String executeUrl = pygwUrl + "Aggregators";
            if (debug) {
                resp.getWriter().println("getAggregatorsEntries() executeUrl :" + executeUrl);
            }
            aggregatorsResponse = executeURL(executeUrl, pygwUserPass, resp, debug);
        } catch (Exception ex) {
            JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "getAggregatorsEntries()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
        }
		return aggregatorsResponse;
    }

    public JsonObject getSccNfgEntries(HttpServletResponse resp, String pygwUrl, String pygwUserPass,  boolean debug) throws IOException {
        JsonObject sccnfgResponse = new JsonObject();
		try {
                String executeUrl = pygwUrl + "SCCNFG?$filter=CP_TYPE%20eq%20%2701%27";
                if (debug) {
                    resp.getWriter().println("getSccNfgEntries() executeUrl :" + executeUrl);
                }
                sccnfgResponse = executeURL(executeUrl, pygwUserPass, resp, debug);
        } catch (Exception ex) {
			JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Method", "getSccNfgEntries()");
			result.addProperty("Message", ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			resp.getWriter().println(result);
		}
		return sccnfgResponse;
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
	public ServletRegistrationBean<AccountMIS> AccountsMISServletBean() {
		ServletRegistrationBean<AccountMIS> bean = new ServletRegistrationBean<>(new AccountMIS(), "/AccountMIs");
		return bean;
	}
}
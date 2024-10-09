package com.arteriatech.bc.SCFDealerOutstanding;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SCFDealerOutStandingClient {

	
	public JsonObject callSCFDealerOutStandingClient(HttpServletResponse response, String dealerODAccountNo, boolean debug) throws IOException
	{
		String  endPointURL = "" , dealerODAccn="",message="",system="";
		
		JsonObject scfDealerOutStandingObject = new JsonObject();
		JsonArray scfDealerDetailsArray = new JsonArray();
		JsonObject scfdealerDetailsChildObj = new JsonObject();
		
		CommonUtils commonUtils = new CommonUtils();
		
		try {
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"SCFDealerOutstanding";
			
			if(debug)
				response.getWriter().println("callSCFDealerOutStandingClient.dealerODAccountNo: "+dealerODAccountNo);
			
			SCFDealerOutstanding_Request scfDealerOutstandingRequest = new SCFDealerOutstanding_Request();
			scfDealerOutstandingRequest.setDealerODAccountNo(dealerODAccountNo);
			
			SCFDealerOutstandingServiceLocator scfDealerLocator = new SCFDealerOutstandingServiceLocator();
			scfDealerLocator.setEndpointAddress("SCFDealerOutstandingPort", endPointURL);
			
			SCFDealerOutstanding service = scfDealerLocator.getSCFDealerOutstandingPort();
			SCFDealerOutstanding_Response scfDealerResponse = service.SCFDealerOutstanding(scfDealerOutstandingRequest);
			
			SCFDealerOutstanding_ResponseDetail[] scfDetails = scfDealerResponse.getDetail();
			
			if(debug){
				response.getWriter().println("SCFDealerOutstanding.getStatus: "+scfDealerResponse.getStatus().getStatus());
				response.getWriter().println("SCFDealerOutstanding.getResponseCode: "+scfDealerResponse.getStatus().getResponseCode());
				response.getWriter().println("SCFDealerOutstanding.getMessage: "+scfDealerResponse.getStatus().getMessage());
				response.getWriter().println("SCFDealerOutstanding.getOutstandingAmount: "+scfDealerResponse.getHeader().getOutstandingAmount());
			}
			
			if (null != scfDealerResponse.getStatus().getStatus() && scfDealerResponse.getStatus().getStatus().trim().length() > 0) {
				if (scfDealerResponse.getStatus().getStatus().equalsIgnoreCase("000001")) {
					
					scfDealerOutStandingObject.addProperty("Status", scfDealerResponse.getStatus().getStatus());
					
					if (null != scfDealerResponse.getStatus().getResponseCode() && scfDealerResponse.getStatus().getResponseCode().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("ResponseCode", scfDealerResponse.getStatus().getResponseCode());
					else
						scfDealerOutStandingObject.addProperty("ResponseCode", "" );
					
					if (null != scfDealerResponse.getStatus().getMessage() && scfDealerResponse.getStatus().getMessage().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("Message", scfDealerResponse.getStatus().getMessage() );
					else
						scfDealerOutStandingObject.addProperty("Message", "" );
					
					// TODO: Dealer Header 
					if (null == scfDealerResponse.getHeader().getCorporateName() || scfDealerResponse.getHeader().getCorporateName().trim().length() == 0)
						scfDealerOutStandingObject.addProperty("CorporateName", "" );
					else
						scfDealerOutStandingObject.addProperty("CorporateName", scfDealerResponse.getHeader().getCorporateName() );
					
					if (null != scfDealerResponse.getHeader().getDealerName() && scfDealerResponse.getHeader().getDealerName().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("DealerName", scfDealerResponse.getHeader().getDealerName());
					else
						scfDealerOutStandingObject.addProperty("DealerName", "" );
					
					if (null != scfDealerResponse.getHeader().getCustomerId() && scfDealerResponse.getHeader().getCustomerId().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("CustomerId", scfDealerResponse.getHeader().getCustomerId() );
					else
						scfDealerOutStandingObject.addProperty("CustomerId", "" );
					
					if (null != scfDealerResponse.getHeader().getSanctionLimit() && scfDealerResponse.getHeader().getSanctionLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("SanctionLimit", scfDealerResponse.getHeader().getSanctionLimit() );
					else
						scfDealerOutStandingObject.addProperty("SanctionLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getSanctionLimitCurrency() && scfDealerResponse.getHeader().getSanctionLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", scfDealerResponse.getHeader().getSanctionLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getTenorInDays() && scfDealerResponse.getHeader().getTenorInDays().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("TenorInDays", scfDealerResponse.getHeader().getTenorInDays());
					else
						scfDealerOutStandingObject.addProperty("TenorInDays", "0" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimit() && scfDealerResponse.getHeader().getPeakLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimit", scfDealerResponse.getHeader().getPeakLimit() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimitCurrency() && scfDealerResponse.getHeader().getPeakLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimitCurrency", scfDealerResponse.getHeader().getPeakLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimitTenorInDays() && scfDealerResponse.getHeader().getPeakLimitTenorInDays().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", scfDealerResponse.getHeader().getPeakLimitTenorInDays() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", "0" );
					
					if (null != scfDealerResponse.getHeader().getOutstandingAmount() && scfDealerResponse.getHeader().getOutstandingAmount().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OutstandingAmount", scfDealerResponse.getHeader().getOutstandingAmount() );
					else
						scfDealerOutStandingObject.addProperty("OutstandingAmount", "" );
					
					if (null != scfDealerResponse.getHeader().getOutstandingAmountCurrency() && scfDealerResponse.getHeader().getOutstandingAmountCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", scfDealerResponse.getHeader().getOutstandingAmountCurrency() );
					else
						scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getOverdueBeyondCure() && scfDealerResponse.getHeader().getOverdueBeyondCure().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OverdueBeyondCure", scfDealerResponse.getHeader().getOverdueBeyondCure() );
					else
						scfDealerOutStandingObject.addProperty("OverdueBeyondCure", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getOverdueBeyondCureCurrency() && scfDealerResponse.getHeader().getOverdueBeyondCureCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", scfDealerResponse.getHeader().getOverdueBeyondCureCurrency() );
					else
						scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getAvailableLimit() && scfDealerResponse.getHeader().getAvailableLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AvailableLimit", scfDealerResponse.getHeader().getAvailableLimit() );
					else
						scfDealerOutStandingObject.addProperty("AvailableLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getAvailableLimitCurrency() && scfDealerResponse.getHeader().getAvailableLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", scfDealerResponse.getHeader().getAvailableLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getAsOnDate() && scfDealerResponse.getHeader().getAsOnDate().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AsOnDate", scfDealerResponse.getHeader().getAsOnDate() );
					else
						scfDealerOutStandingObject.addProperty("AsOnDate", "" );
					
					if (null != scfDealerResponse.getHeader().getIsDealerBlocked() && scfDealerResponse.getHeader().getIsDealerBlocked().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("IsDealerBlocked", scfDealerResponse.getHeader().getIsDealerBlocked() );
					else
						scfDealerOutStandingObject.addProperty("IsDealerBlocked", "" );
					
					if (null != scfDealerResponse.getHeader().getBlockingReasonId() && scfDealerResponse.getHeader().getBlockingReasonId().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("BlockingReasonId", scfDealerResponse.getHeader().getBlockingReasonId() );
					else
						scfDealerOutStandingObject.addProperty("BlockingReasonId", "" );
					
					if (null != scfDealerResponse.getHeader().getBlockingReasonDesc() && scfDealerResponse.getHeader().getBlockingReasonDesc().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("BlockingReasonDesc", scfDealerResponse.getHeader().getBlockingReasonDesc() );
					else
						scfDealerOutStandingObject.addProperty("BlockingReasonDesc", "" );
					
					// TODO:Calling  for Dealer Details
					for (int i = 0; i < scfDetails.length; i++) {
						
						scfdealerDetailsChildObj = new JsonObject();
						
						if (null != scfDetails[i].getTxnCategoryId() && scfDetails[i].getTxnCategoryId().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TxnCategoryId", scfDetails[i].getTxnCategoryId() );
						else
							scfdealerDetailsChildObj.addProperty("TxnCategoryId", "" );
						
						if (null != scfDetails[i].getTxnCategoryDescription() && scfDetails[i].getTxnCategoryDescription().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", scfDetails[i].getTxnCategoryDescription() );
						else
							scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", "" );
						
						if (null != scfDetails[i].getTransactionId() && scfDetails[i].getTransactionId().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionId", scfDetails[i].getTransactionId() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionId", "" );
						
						if (null != scfDetails[i].getTransactionDate() && scfDetails[i].getTransactionDate().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionDate", scfDetails[i].getTransactionDate() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionDate", "" );
						
						if (null != scfDetails[i].getTransactionAmount() && scfDetails[i].getTransactionAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionAmount", scfDetails[i].getTransactionAmount() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionAmount", "0.00" );
						
						if (null != scfDetails[i].getOutstandingAmount() && scfDetails[i].getOutstandingAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OutstandingAmount", scfDetails[i].getOutstandingAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OutstandingAmount", "0.00" );
						
						if (null != scfDetails[i].getDueDate() && scfDetails[i].getDueDate().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("DueDate", scfDetails[i].getDueDate() );
						else
							scfdealerDetailsChildObj.addProperty("DueDate", "" );
						
						if (null != scfDetails[i].getOverdueWithinCureAmount() && scfDetails[i].getOverdueWithinCureAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", scfDetails[i].getOverdueWithinCureAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", "0.00" );
						
						if (null != scfDetails[i].getOverdueWithinCureCurrency() && scfDetails[i].getOverdueWithinCureCurrency().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", scfDetails[i].getOverdueWithinCureCurrency() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", "" );
						
						if (null != scfDetails[i].getOverdueWithinCureDays() && scfDetails[i].getOverdueWithinCureDays().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", scfDetails[i].getOverdueWithinCureDays() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", "0" );
						
						if (null != scfDetails[i].getOverdueBeyondCureAmount() && scfDetails[i].getOverdueBeyondCureAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", scfDetails[i].getOverdueBeyondCureAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", "0.00" );
						
						if (null != scfDetails[i].getOverdueBeyondCureCurrency() && scfDetails[i].getOverdueBeyondCureCurrency().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", scfDetails[i].getOverdueBeyondCureCurrency() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", "" );
						
						if (null != scfDetails[i].getOverdueBeyondCureDays() && scfDetails[i].getOverdueBeyondCureDays().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", scfDetails[i].getOverdueBeyondCureDays() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", "0" );
						
						if (null != scfDetails[i].getIsOverdue() && scfDetails[i].getIsOverdue().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("IsOverdue", scfDetails[i].getIsOverdue() );
						else
							scfdealerDetailsChildObj.addProperty("IsOverdue", "" );
						
						if (null != scfDetails[i].getOrderBlocked() && scfDetails[i].getOrderBlocked().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OrderBlocked", scfDetails[i].getOrderBlocked());
						else
							scfdealerDetailsChildObj.addProperty("OrderBlocked", "" );
						
						if (null != scfDetails[i].getPaymentBlocked() && scfDetails[i].getPaymentBlocked().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("PaymentBlocked", scfDetails[i].getPaymentBlocked() );
						else
							scfdealerDetailsChildObj.addProperty("PaymentBlocked", "" );
						
						scfDealerDetailsArray.add(scfdealerDetailsChildObj);
					}
					
					scfDealerOutStandingObject.add("results", scfDealerDetailsArray);
					
				} else {
					scfDealerOutStandingObject.addProperty("Status", scfDealerResponse.getStatus().getStatus());
					
					if (null != scfDealerResponse.getStatus().getResponseCode() && scfDealerResponse.getStatus().getResponseCode().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("ResponseCode", scfDealerResponse.getStatus().getResponseCode() );
					else
						scfDealerOutStandingObject.addProperty("ResponseCode", "" );
					
					if (null != scfDealerResponse.getStatus().getMessage() && scfDealerResponse.getStatus().getMessage().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("Message", scfDealerResponse.getStatus().getMessage() );
					else
						scfDealerOutStandingObject.addProperty("Message", "" );
					
					// TODO: Header Response
					/*if (null == scfDealerResponse.getHeader().getCorporateName() || scfDealerResponse.getHeader().getCorporateName().trim().length() == 0)
						scfDealerOutStandingObject.addProperty("CorporateName", "" );
					else
						scfDealerOutStandingObject.addProperty("CorporateName", scfDealerResponse.getHeader().getCorporateName() );
					
					if (null != scfDealerResponse.getHeader().getDealerName() && scfDealerResponse.getHeader().getDealerName().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("DealerName", scfDealerResponse.getHeader().getDealerName());
					else
						scfDealerOutStandingObject.addProperty("DealerName", "" );
					
					if (null != scfDealerResponse.getHeader().getCustomerId() && scfDealerResponse.getHeader().getCustomerId().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("CustomerId", scfDealerResponse.getHeader().getCustomerId() );
					else
						scfDealerOutStandingObject.addProperty("CustomerId", "" );
					
					if (null != scfDealerResponse.getHeader().getSanctionLimit() && scfDealerResponse.getHeader().getSanctionLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("SanctionLimit", scfDealerResponse.getHeader().getSanctionLimit() );
					else
						scfDealerOutStandingObject.addProperty("SanctionLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getSanctionLimitCurrency() && scfDealerResponse.getHeader().getSanctionLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", scfDealerResponse.getHeader().getSanctionLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getTenorInDays() && scfDealerResponse.getHeader().getTenorInDays().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("TenorInDays", scfDealerResponse.getHeader().getTenorInDays());
					else
						scfDealerOutStandingObject.addProperty("TenorInDays", "0" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimit() && scfDealerResponse.getHeader().getPeakLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimit", scfDealerResponse.getHeader().getPeakLimit() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimitCurrency() && scfDealerResponse.getHeader().getPeakLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimitCurrency", scfDealerResponse.getHeader().getPeakLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getPeakLimitTenorInDays() && scfDealerResponse.getHeader().getPeakLimitTenorInDays().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", scfDealerResponse.getHeader().getPeakLimitTenorInDays() );
					else
						scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", "0" );
					
					if (null != scfDealerResponse.getHeader().getOutstandingAmount() && scfDealerResponse.getHeader().getOutstandingAmount().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OutstandingAmount", scfDealerResponse.getHeader().getOutstandingAmount() );
					else
						scfDealerOutStandingObject.addProperty("OutstandingAmount", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getOutstandingAmountCurrency() && scfDealerResponse.getHeader().getOutstandingAmountCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", scfDealerResponse.getHeader().getOutstandingAmountCurrency() );
					else
						scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getOverdueBeyondCure() && scfDealerResponse.getHeader().getOverdueBeyondCure().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OverdueBeyondCure", scfDealerResponse.getHeader().getOverdueBeyondCure() );
					else
						scfDealerOutStandingObject.addProperty("OverdueBeyondCure", "" );
					
					if (null != scfDealerResponse.getHeader().getOverdueBeyondCureCurrency() && scfDealerResponse.getHeader().getOverdueBeyondCureCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", scfDealerResponse.getHeader().getOverdueBeyondCureCurrency() );
					else
						scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getAvailableLimit() && scfDealerResponse.getHeader().getAvailableLimit().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AvailableLimit", scfDealerResponse.getHeader().getAvailableLimit() );
					else
						scfDealerOutStandingObject.addProperty("AvailableLimit", "0.00" );
					
					if (null != scfDealerResponse.getHeader().getAvailableLimitCurrency() && scfDealerResponse.getHeader().getAvailableLimitCurrency().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", scfDealerResponse.getHeader().getAvailableLimitCurrency() );
					else
						scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", "" );
					
					if (null != scfDealerResponse.getHeader().getAsOnDate() && scfDealerResponse.getHeader().getAsOnDate().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("AsOnDate", scfDealerResponse.getHeader().getAsOnDate() );
					else
						scfDealerOutStandingObject.addProperty("AsOnDate", "" );
					
					if (null != scfDealerResponse.getHeader().getIsDealerBlocked() && scfDealerResponse.getHeader().getIsDealerBlocked().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("IsDealerBlocked", scfDealerResponse.getHeader().getIsDealerBlocked() );
					else
						scfDealerOutStandingObject.addProperty("IsDealerBlocked", "" );
					
					if (null != scfDealerResponse.getHeader().getBlockingReasonId() && scfDealerResponse.getHeader().getBlockingReasonId().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("BlockingReasonId", scfDealerResponse.getHeader().getBlockingReasonId() );
					else
						scfDealerOutStandingObject.addProperty("BlockingReasonId", "" );
					
					if (null != scfDealerResponse.getHeader().getBlockingReasonDesc() && scfDealerResponse.getHeader().getBlockingReasonDesc().trim().length() > 0)
						scfDealerOutStandingObject.addProperty("BlockingReasonDesc", scfDealerResponse.getHeader().getBlockingReasonDesc() );
					else
						scfDealerOutStandingObject.addProperty("BlockingReasonDesc", "" );
					
					// TODO: Calling1 dealerDetails() for Dealer Details
					for (int i = 0; i < scfDetails.length; i++) {
						
						scfdealerDetailsChildObj = new JsonObject();
						
						if (null != scfDetails[i].getTxnCategoryId() && scfDetails[i].getTxnCategoryId().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TxnCategoryId", scfDetails[i].getTxnCategoryId() );
						else
							scfdealerDetailsChildObj.addProperty("TxnCategoryId", "" );
						
						if (null != scfDetails[i].getTxnCategoryDescription() && scfDetails[i].getTxnCategoryDescription().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", scfDetails[i].getTxnCategoryDescription() );
						else
							scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", "" );
						
						if (null != scfDetails[i].getTransactionId() && scfDetails[i].getTransactionId().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionId", scfDetails[i].getTransactionId() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionId", "" );
						
						if (null != scfDetails[i].getTransactionDate() && scfDetails[i].getTransactionDate().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionDate", scfDetails[i].getTransactionDate() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionDate", "" );
						
						if (null != scfDetails[i].getTransactionAmount() && scfDetails[i].getTransactionAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("TransactionAmount", scfDetails[i].getTransactionAmount() );
						else
							scfdealerDetailsChildObj.addProperty("TransactionAmount", "0.00" );
						
						if (null != scfDetails[i].getOutstandingAmount() && scfDetails[i].getOutstandingAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OutstandingAmount", scfDetails[i].getOutstandingAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OutstandingAmount", "0.00" );
						
						if (null != scfDetails[i].getDueDate() && scfDetails[i].getDueDate().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("DueDate", scfDetails[i].getDueDate() );
						else
							scfdealerDetailsChildObj.addProperty("DueDate", "" );
						
						if (null != scfDetails[i].getOverdueWithinCureAmount() && scfDetails[i].getOverdueWithinCureAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", scfDetails[i].getOverdueWithinCureAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", "0.00" );
						
						if (null != scfDetails[i].getOverdueWithinCureCurrency() && scfDetails[i].getOverdueWithinCureCurrency().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", scfDetails[i].getOverdueWithinCureCurrency() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", "" );
						
						if (null != scfDetails[i].getOverdueWithinCureDays() && scfDetails[i].getOverdueWithinCureDays().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", scfDetails[i].getOverdueWithinCureDays() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", "0" );
						
						if (null != scfDetails[i].getOverdueBeyondCureAmount() && scfDetails[i].getOverdueBeyondCureAmount().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", scfDetails[i].getOverdueBeyondCureAmount() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", "0.00" );
						
						if (null != scfDetails[i].getOverdueBeyondCureCurrency() && scfDetails[i].getOverdueBeyondCureCurrency().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", scfDetails[i].getOverdueBeyondCureCurrency() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", "" );
						
						if (null != scfDetails[i].getOverdueBeyondCureDays() && scfDetails[i].getOverdueBeyondCureDays().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", scfDetails[i].getOverdueBeyondCureCurrency() );
						else
							scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", "0" );
						
						if (null != scfDetails[i].getIsOverdue() && scfDetails[i].getIsOverdue().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("IsOverdue", scfDetails[i].getIsOverdue() );
						else
							scfdealerDetailsChildObj.addProperty("IsOverdue", "" );
						
						if (null != scfDetails[i].getOrderBlocked() && scfDetails[i].getOrderBlocked().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("OrderBlocked", scfDetails[i].getOrderBlocked());
						else
							scfdealerDetailsChildObj.addProperty("OrderBlocked", "" );
						
						if (null != scfDetails[i].getPaymentBlocked() && scfDetails[i].getPaymentBlocked().trim().length() > 0)
							scfdealerDetailsChildObj.addProperty("PaymentBlocked", scfDetails[i].getPaymentBlocked() );
						else
							scfdealerDetailsChildObj.addProperty("PaymentBlocked", "" );
						
						scfDealerDetailsArray.add(scfdealerDetailsChildObj);
					}*/
					
					// TODO: Header Response
					scfDealerOutStandingObject.addProperty("CorporateName", "");
					scfDealerOutStandingObject.addProperty("DealerName", "");
					scfDealerOutStandingObject.addProperty("CustomerId", "");
					scfDealerOutStandingObject.addProperty("SanctionLimit", "0.00");
					scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", "");
					scfDealerOutStandingObject.addProperty("TenorInDays", "0");
					scfDealerOutStandingObject.addProperty("PeakLimit", "0.00");
					scfDealerOutStandingObject.addProperty("PeakLimitCurrency", "");
					scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", "0");
					scfDealerOutStandingObject.addProperty("OutstandingAmount", "0.00");
					scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", "");
					scfDealerOutStandingObject.addProperty("OverdueBeyondCure", "");
					scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", "");
					scfDealerOutStandingObject.addProperty("AvailableLimit", "0.00");
					scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", "");
					scfDealerOutStandingObject.addProperty("AsOnDate", "");
					scfDealerOutStandingObject.addProperty("IsDealerBlocked", "");
					scfDealerOutStandingObject.addProperty("BlockingReasonId", "");
					scfDealerOutStandingObject.addProperty("BlockingReasonDesc", "");
					
					// TODO: Calling2 dealerDetails() for Dealer Details
					scfdealerDetailsChildObj.addProperty("TxnCategoryId", "");
					scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", "");
					scfdealerDetailsChildObj.addProperty("TransactionId", "");
					scfdealerDetailsChildObj.addProperty("TransactionDate", "");
					scfdealerDetailsChildObj.addProperty("TransactionAmount", "0.00");
					scfdealerDetailsChildObj.addProperty("OutstandingAmount", "0.00");
					scfdealerDetailsChildObj.addProperty("DueDate", "");
					scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", "0.00");
					scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", "");
					scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", "0");
					scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", "0.00");
					scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", "");
					scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", "0");
					scfdealerDetailsChildObj.addProperty("IsOverdue", "");
					scfdealerDetailsChildObj.addProperty("OrderBlocked", "");
					scfdealerDetailsChildObj.addProperty("PaymentBlocked", "");
						
					scfDealerDetailsArray.add(scfdealerDetailsChildObj);
					scfDealerOutStandingObject.add("results", scfDealerDetailsArray);
				}
			}else
			{
				scfDealerOutStandingObject.addProperty("Status", "000002" );
				
				if (null != scfDealerResponse.getStatus().getResponseCode() && scfDealerResponse.getStatus().getResponseCode().trim().length() > 0)
					scfDealerOutStandingObject.addProperty("ResponseCode", scfDealerResponse.getStatus().getResponseCode() );
				else
					scfDealerOutStandingObject.addProperty("ResponseCode", "" );
				
				if (null != scfDealerResponse.getStatus().getMessage() && scfDealerResponse.getStatus().getMessage().trim().length() > 0)
					scfDealerOutStandingObject.addProperty("Message", scfDealerResponse.getStatus().getMessage() );
				else
					scfDealerOutStandingObject.addProperty("Message", "" );
				
				// TODO: Header Response
				scfDealerOutStandingObject.addProperty("CorporateName", "");
				scfDealerOutStandingObject.addProperty("DealerName", "");
				scfDealerOutStandingObject.addProperty("CustomerId", "");
				scfDealerOutStandingObject.addProperty("SanctionLimit", "0.00");
				scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", "");
				scfDealerOutStandingObject.addProperty("TenorInDays", "0");
				scfDealerOutStandingObject.addProperty("PeakLimit", "0.00");
				scfDealerOutStandingObject.addProperty("PeakLimitCurrency", "");
				scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", "0");
				scfDealerOutStandingObject.addProperty("OutstandingAmount", "0.00");
				scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", "");
				scfDealerOutStandingObject.addProperty("OverdueBeyondCure", "");
				scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", "");
				scfDealerOutStandingObject.addProperty("AvailableLimit", "0.00");
				scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", "");
				scfDealerOutStandingObject.addProperty("AsOnDate", "");
				scfDealerOutStandingObject.addProperty("IsDealerBlocked", "");
				scfDealerOutStandingObject.addProperty("BlockingReasonId", "");
				scfDealerOutStandingObject.addProperty("BlockingReasonDesc", "");
				
				// TODO: Calling2 dealerDetails() for Dealer Details
				scfdealerDetailsChildObj.addProperty("TxnCategoryId", "");
				scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", "");
				scfdealerDetailsChildObj.addProperty("TransactionId", "");
				scfdealerDetailsChildObj.addProperty("TransactionDate", "");
				scfdealerDetailsChildObj.addProperty("TransactionAmount", "0.00");
				scfdealerDetailsChildObj.addProperty("OutstandingAmount", "0.00");
				scfdealerDetailsChildObj.addProperty("DueDate", "");
				scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", "0.00");
				scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", "");
				scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", "0");
				scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", "0.00");
				scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", "");
				scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", "0");
				scfdealerDetailsChildObj.addProperty("IsOverdue", "");
				scfdealerDetailsChildObj.addProperty("OrderBlocked", "");
				scfdealerDetailsChildObj.addProperty("PaymentBlocked", "");
					
				scfDealerDetailsArray.add(scfdealerDetailsChildObj);
				scfDealerOutStandingObject.add("results", scfDealerDetailsArray);
			}
		} catch (Exception e) {
			
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeHttpPost.NamingException:"+buffer.toString());
			}
			
			// TODO: handle exception
			scfDealerOutStandingObject.addProperty("ResponseCode", "001");
			scfDealerOutStandingObject.addProperty("Message", e.getLocalizedMessage());
			scfDealerOutStandingObject.addProperty("Status", "000002");
			
			scfDealerOutStandingObject.addProperty("CorporateName", "");
			scfDealerOutStandingObject.addProperty("DealerName", "");
			scfDealerOutStandingObject.addProperty("CustomerId", "");
			scfDealerOutStandingObject.addProperty("SanctionLimit", "0.00");
			scfDealerOutStandingObject.addProperty("SanctionLimitCurrency", "");
			scfDealerOutStandingObject.addProperty("TenorInDays", "0");
			scfDealerOutStandingObject.addProperty("PeakLimit", "0.00");
			scfDealerOutStandingObject.addProperty("PeakLimitCurrency", "");
			scfDealerOutStandingObject.addProperty("PeakLimitTenorInDays", "0");
			scfDealerOutStandingObject.addProperty("OutstandingAmount", "0.00");
			scfDealerOutStandingObject.addProperty("OutstandingAmountCurrency", "");
			scfDealerOutStandingObject.addProperty("OverdueBeyondCure", "");
			scfDealerOutStandingObject.addProperty("OverdueBeyondCureCurrency", "");
			scfDealerOutStandingObject.addProperty("AvailableLimit", "0.00");
			scfDealerOutStandingObject.addProperty("AvailableLimitCurrency", "");
			scfDealerOutStandingObject.addProperty("AsOnDate", "");
			scfDealerOutStandingObject.addProperty("IsDealerBlocked", "");
			scfDealerOutStandingObject.addProperty("BlockingReasonId", "");
			scfDealerOutStandingObject.addProperty("BlockingReasonDesc", "");
			
			scfdealerDetailsChildObj.addProperty("TxnCategoryId", "");
			scfdealerDetailsChildObj.addProperty("TxnCategoryDescription", "");
			scfdealerDetailsChildObj.addProperty("TransactionId", "");
			scfdealerDetailsChildObj.addProperty("TransactionDate", "");
			scfdealerDetailsChildObj.addProperty("TransactionAmount", "0.00");
			scfdealerDetailsChildObj.addProperty("OutstandingAmount", "0.00");
			scfdealerDetailsChildObj.addProperty("DueDate", "");
			scfdealerDetailsChildObj.addProperty("OverdueWithinCureAmount", "0.00");
			scfdealerDetailsChildObj.addProperty("OverdueWithinCureCurrency", "");
			scfdealerDetailsChildObj.addProperty("OverdueWithinCureDays", "0");
			scfdealerDetailsChildObj.addProperty("OverdueBeyondCureAmount", "0.00");
			scfdealerDetailsChildObj.addProperty("OverdueBeyondCureCurrency", "");
			scfdealerDetailsChildObj.addProperty("OverdueBeyondCureDays", "0");
			scfdealerDetailsChildObj.addProperty("IsOverdue", "");
			scfdealerDetailsChildObj.addProperty("OrderBlocked", "");
			scfdealerDetailsChildObj.addProperty("PaymentBlocked", "");
			
			scfDealerDetailsArray.add(scfdealerDetailsChildObj);
			scfDealerOutStandingObject.add("results", scfDealerDetailsArray);
		}
		return scfDealerOutStandingObject;
	}

}

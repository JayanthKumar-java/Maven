package com.arteriatech.bc.SCFOffer;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import com.arteriatech.pg.CommonUtils;

public class SCFOfferClient {
	public Map callSCFOffersWebservice(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String corpID, String dealerID, boolean debug){
		String responseValue = "", endPointURL="", system="";
		Map<String,String> scfOfferResponseMap = new HashMap<String,String>();
		SCFOfferServiceLocator locator = new SCFOfferServiceLocator();
		CommonUtils commonUtils = new CommonUtils();
		try {
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"SCFOffer";
			/*if(system.equalsIgnoreCase("PRD")){
				endPointURL = "https://l20321-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFOffer";
			} else if(system.equalsIgnoreCase("QAS")){
				endPointURL = "https://l20320-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFOffer";
			} else{
				endPointURL = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFOffer";
			}*/
//			corpID = servletRequest.getParameter("corpID");
//			servletResponse.getWriter().println("callWebservice - corpID: "+corpID);
			SCFOffer_Request offerRequest = new SCFOffer_Request();
			offerRequest.setCorpId(corpID);
//			offerRequest.setCorpId("CIBNEXT");
			offerRequest.setDealerId(dealerID);
			
			SCFOfferServiceLocator asLocator = new SCFOfferServiceLocator();
			asLocator.setEndpointAddress("SCFOfferPort", endPointURL);
			SCFOffer service = asLocator.getSCFOfferPort();
			
			SCFOffer_Response response = service.SCFOffer(offerRequest);
			
			if(null == response.getStatus() || response.getStatus().trim().length() == 0 
					|| response.getStatus().equalsIgnoreCase("000002") || response.getStatus().trim().equalsIgnoreCase("")){
				scfOfferResponseMap.put("ADDLNPRDINTRateSP", "");
				scfOfferResponseMap.put("AddlnTenorOfPymt", "");
				scfOfferResponseMap.put("Address1", "");
				scfOfferResponseMap.put("Address2", "");
				scfOfferResponseMap.put("Address3", "");
				scfOfferResponseMap.put("Address4", "");
				scfOfferResponseMap.put("Address5", "");
				scfOfferResponseMap.put("BusinessVintageOfDealer", "");
				scfOfferResponseMap.put("CIN", "");
				scfOfferResponseMap.put("City", "");
				scfOfferResponseMap.put("ConstitutionType", "");
				scfOfferResponseMap.put("CorporateID", "");
				scfOfferResponseMap.put("CorpRating", "");
				scfOfferResponseMap.put("DealerAssociationWithCorporate", "");
				scfOfferResponseMap.put("DealersOverallScoreByCorp", "");
				scfOfferResponseMap.put("DefIntSpread", "");
				
//				scfOfferResponseMap.put("EligibilityStatus", "");
				if(null != response.getEligibilityStatus() && response.getEligibilityStatus().trim().length() > 0)
					scfOfferResponseMap.put("EligibilityStatus", response.getEligibilityStatus());
				else
					scfOfferResponseMap.put("EligibilityStatus", "");
				
				scfOfferResponseMap.put("EntityType", "");
				scfOfferResponseMap.put("ErrorCode", "");
				scfOfferResponseMap.put("GSTIN", "");
				scfOfferResponseMap.put("InterestSpread", "0.00");
				scfOfferResponseMap.put("InterestRateSpread", "0.00");
				scfOfferResponseMap.put("MaxLimitPerCorp", "");
				scfOfferResponseMap.put("MCLR6Rate", "");
				if(null == response.getMessage() || response.getMessage().trim().length() == 0){
					scfOfferResponseMap.put("Message", "Exception raised without any specific error");
				}else{
					scfOfferResponseMap.put("Message", response.getMessage());
				}
				
				scfOfferResponseMap.put("MobileNo", "");
				scfOfferResponseMap.put("NoOfChequeReturns", "");
				scfOfferResponseMap.put("OfferAmt", "");
				scfOfferResponseMap.put("Currency", "");
				scfOfferResponseMap.put("OfferTenure", "");
				scfOfferResponseMap.put("PAN", "");
				scfOfferResponseMap.put("PaymentDelayDays12Months", "");
				scfOfferResponseMap.put("Pincode", "");
				scfOfferResponseMap.put("ProcessingFee", "");
				scfOfferResponseMap.put("ProcessFeePerc", "");
				scfOfferResponseMap.put("PurchasesOf12Months", "");
				scfOfferResponseMap.put("Rate", "");
				scfOfferResponseMap.put("salesOf12Months", "");
				scfOfferResponseMap.put("State", "");
				if(null == response.getStatus() || response.getStatus().trim().length() == 0 || response.getStatus().trim().equalsIgnoreCase(""))
					scfOfferResponseMap.put("StatusID", "000002");
				else
					scfOfferResponseMap.put("StatusID", response.getStatus());
				scfOfferResponseMap.put("TenorOfPayment", "");
				scfOfferResponseMap.put("TIN", "");
				scfOfferResponseMap.put("ValidTo", "");
				
				//Fields that are not available in websevices
				scfOfferResponseMap.put("AccountNo", "");
				scfOfferResponseMap.put("TotalBalance", "");
				scfOfferResponseMap.put("TenorIndays", "");
				scfOfferResponseMap.put("PeakLimit", "");
				scfOfferResponseMap.put("PeaklmtTenorindays", "");
				scfOfferResponseMap.put("OverdueBeyondCure", "");
				scfOfferResponseMap.put("AsOnDate", "");
				scfOfferResponseMap.put("ReportDate", "");
				scfOfferResponseMap.put("OutstandingAmount", "");
				scfOfferResponseMap.put("EContractID", "");
				scfOfferResponseMap.put("ECustomerID", "");
				scfOfferResponseMap.put("ApplicationNo", "");
				scfOfferResponseMap.put("CallBackStatus", "");
				scfOfferResponseMap.put("ECompleteTime", "");
				scfOfferResponseMap.put("ECompleteDate", "");
				scfOfferResponseMap.put("ApplicantID", "");
				scfOfferResponseMap.put("IsOverdue", "");
				scfOfferResponseMap.put("BlockFinancing", "");
				scfOfferResponseMap.put("OverdueBy", "");
				scfOfferResponseMap.put("BlockOrder", "");
				scfOfferResponseMap.put("BlockingReasonID", "");
				scfOfferResponseMap.put("BlockingReasonDesc", "");
				scfOfferResponseMap.put("DDBActive", "");
				scfOfferResponseMap.put("SupplyChainFinanceTxns", "");
				scfOfferResponseMap.put("DealerVendorFlag", "");
			}else{
				if(null != response.getAddlnPeriodInterestRateSpread() && response.getAddlnPeriodInterestRateSpread().trim().length() > 0)
					scfOfferResponseMap.put("ADDLNPRDINTRateSP", response.getAddlnPeriodInterestRateSpread());
				else
					scfOfferResponseMap.put("ADDLNPRDINTRateSP", "0.00");
				
				if(null != response.getAddlnTenorOfPayment() && response.getAddlnTenorOfPayment().trim().length() > 0)
					scfOfferResponseMap.put("AddlnTenorOfPymt", response.getAddlnTenorOfPayment());
				else
					scfOfferResponseMap.put("AddlnTenorOfPymt", "0");
				
				if(null != response.getAddress1() && response.getAddress1().trim().length() > 0)
					scfOfferResponseMap.put("Address1", response.getAddress1());
				else
					scfOfferResponseMap.put("Address1", "");
				
				if(null != response.getAddress2() && response.getAddress2().trim().length() > 0)
					scfOfferResponseMap.put("Address2", response.getAddress2());
				else
					scfOfferResponseMap.put("Address2", "");
				
				if(null != response.getAddress3() && response.getAddress3().trim().length() > 0)
					scfOfferResponseMap.put("Address3", response.getAddress3());
				else
					scfOfferResponseMap.put("Address3", "");
				
				if(null != response.getAddress4() && response.getAddress4().trim().length() > 0)
					scfOfferResponseMap.put("Address4", response.getAddress4());
				else
					scfOfferResponseMap.put("Address4", "");
				
				if(null != response.getAddress5() && response.getAddress5().trim().length() > 0)
					scfOfferResponseMap.put("Address5", response.getAddress5());
				else
					scfOfferResponseMap.put("Address5", "");
				
				if(null != response.getBusinessVintageOfDealer() && response.getBusinessVintageOfDealer().trim().length() > 0)
					scfOfferResponseMap.put("BusinessVintageOfDealer", response.getBusinessVintageOfDealer());
				else
					scfOfferResponseMap.put("BusinessVintageOfDealer", "");
				
				if(null != response.getCIN() && response.getCIN().trim().length() > 0)
					scfOfferResponseMap.put("CIN", response.getCIN());
				else
					scfOfferResponseMap.put("CIN", "");
				
				if(null != response.getCity() && response.getCity().trim().length() > 0)
					scfOfferResponseMap.put("City", response.getCity());
				else
					scfOfferResponseMap.put("City", "");
				
				if(null != response.getConstitutionType() && response.getConstitutionType().trim().length() > 0)
					scfOfferResponseMap.put("ConstitutionType", response.getConstitutionType());
				else
					scfOfferResponseMap.put("ConstitutionType", "");
				
				if(null != response.getCorporateID() && response.getCorporateID().trim().length() > 0)
					scfOfferResponseMap.put("CorporateID", response.getCorporateID());
				else
					scfOfferResponseMap.put("CorporateID", "");
				
				if(null != response.getCorpRating() && response.getCorpRating().trim().length() > 0)
					scfOfferResponseMap.put("CorpRating", response.getCorpRating());
				else
					scfOfferResponseMap.put("CorpRating", "");
				
				if(null != response.getDealerAssociationWithCorporate() && response.getDealerAssociationWithCorporate().trim().length() > 0)
					scfOfferResponseMap.put("DealerAssociationWithCorporate", response.getDealerAssociationWithCorporate());
				else
					scfOfferResponseMap.put("DealerAssociationWithCorporate", "");
				
				if(null != response.getDealersOverallScoreByCorp() && response.getDealersOverallScoreByCorp().trim().length() > 0)
					scfOfferResponseMap.put("DealersOverallScoreByCorp", response.getDealersOverallScoreByCorp());
				else
					scfOfferResponseMap.put("DealersOverallScoreByCorp", "0");
				
				if(null != response.getDefaultInterestSpread() && response.getDefaultInterestSpread().trim().length() > 0)
					scfOfferResponseMap.put("DefIntSpread", response.getDefaultInterestSpread());
				else
					scfOfferResponseMap.put("DefIntSpread", "0");
				
				if(null != response.getEligibilityStatus() && response.getEligibilityStatus().trim().length() > 0)
					scfOfferResponseMap.put("EligibilityStatus", response.getEligibilityStatus());
				else
					scfOfferResponseMap.put("EligibilityStatus", "");
				
				if(null != response.getEntityType() && response.getEntityType().trim().length() > 0)
					scfOfferResponseMap.put("EntityType", response.getEntityType());
				else
					scfOfferResponseMap.put("EntityType", "");
				
				if(null != response.getErrorCode() && response.getErrorCode().trim().length() > 0)
					scfOfferResponseMap.put("ErrorCode", response.getErrorCode());
				else
					scfOfferResponseMap.put("ErrorCode", "");
				
				if(null != response.getGSTIN() && response.getGSTIN().trim().length() > 0)
					scfOfferResponseMap.put("GSTIN", response.getGSTIN());
				else
					scfOfferResponseMap.put("GSTIN", "");
				
				if(null != response.getInterestRateSpread() && response.getInterestRateSpread().trim().length() > 0)
					scfOfferResponseMap.put("InterestRateSpread", response.getInterestRateSpread());
				else
					scfOfferResponseMap.put("InterestRateSpread", "0.00");
				
				if(null != response.getInterestSpread() && response.getInterestSpread().trim().length() > 0)
					scfOfferResponseMap.put("InterestSpread", response.getInterestSpread());
				else
					scfOfferResponseMap.put("InterestSpread", "0.00");
				
				if(null != response.getMaxLimitPerCorporate() && response.getMaxLimitPerCorporate().trim().length() > 0)
					scfOfferResponseMap.put("MaxLimitPerCorp", response.getMaxLimitPerCorporate());
				else
					scfOfferResponseMap.put("MaxLimitPerCorp", "0.00");
				
				if(null != response.getMCLR6MRate() && response.getMCLR6MRate().trim().length() > 0)
					scfOfferResponseMap.put("MCLR6Rate", response.getMCLR6MRate());
				else
					scfOfferResponseMap.put("MCLR6Rate", "0.00");
//				scfOfferResponseMap.put("Message", "");
				if(null == response.getMessage() || response.getMessage().trim().length() == 0){
					scfOfferResponseMap.put("Message", "");
				}else{
					scfOfferResponseMap.put("Message", response.getMessage());
				}
				
				if(null != response.getMobileNo() && response.getMobileNo().trim().length() > 0)
					scfOfferResponseMap.put("MobileNo", response.getMobileNo());
				else
					scfOfferResponseMap.put("MobileNo", "");
				
				if(null != response.getNoOfChequeReturns() && response.getNoOfChequeReturns().trim().length() > 0)
					scfOfferResponseMap.put("NoOfChequeReturns", response.getNoOfChequeReturns());
				else
					scfOfferResponseMap.put("NoOfChequeReturns", "");
				
				if(null != response.getOfferAmount() && response.getOfferAmount().trim().length() > 0)
					scfOfferResponseMap.put("OfferAmt", response.getOfferAmount());
				else
					scfOfferResponseMap.put("OfferAmt", "0.00");
				
				if(null != response.getOfferCurrency() && response.getOfferCurrency().trim().length() > 0)
					scfOfferResponseMap.put("Currency", response.getOfferCurrency());
				else
					scfOfferResponseMap.put("Currency", "INR");
				
				if(null != response.getOfferTenure() && response.getOfferTenure().trim().length() > 0)
					scfOfferResponseMap.put("OfferTenure", response.getOfferTenure());
				else
					scfOfferResponseMap.put("OfferTenure", "0");
				
				if(null != response.getPAN() && response.getPAN().trim().length() > 0)
					scfOfferResponseMap.put("PAN", response.getPAN());
				else
					scfOfferResponseMap.put("PAN", "");
				
				if(null != response.getPaymentDelayDays12Months() && response.getPaymentDelayDays12Months().trim().length() > 0)
					scfOfferResponseMap.put("PaymentDelayDays12Months", response.getPaymentDelayDays12Months());
				else
					scfOfferResponseMap.put("PaymentDelayDays12Months", "0.00");
				
				if(null != response.getPincode() && response.getPincode().trim().length() > 0)
					scfOfferResponseMap.put("Pincode", response.getPincode());
				else
					scfOfferResponseMap.put("Pincode", "");
				
				if(null != response.getProcessingFee() && response.getProcessingFee().trim().length() > 0)
					scfOfferResponseMap.put("ProcessingFee", response.getProcessingFee());
				else
					scfOfferResponseMap.put("ProcessingFee", "0.00");
				
				if(null != response.getProcessingFeePercent() && response.getProcessingFeePercent().trim().length() > 0)
					scfOfferResponseMap.put("ProcessFeePerc", response.getProcessingFeePercent());
				else
					scfOfferResponseMap.put("ProcessFeePerc", "0.00");
				
				if(null != response.getPurchasesOf12Months() && response.getPurchasesOf12Months().trim().length() > 0)
					scfOfferResponseMap.put("PurchasesOf12Months", response.getPurchasesOf12Months());
				else
					scfOfferResponseMap.put("PurchasesOf12Months", "0.00");
				
				if(null != response.getRate() && response.getRate().trim().length() > 0)
					scfOfferResponseMap.put("Rate", response.getRate());
				else
					scfOfferResponseMap.put("Rate", "0.00");
				
				if(null != response.getSalesOf12Months() && response.getSalesOf12Months().trim().length() > 0)
					scfOfferResponseMap.put("salesOf12Months", response.getSalesOf12Months());
				else
					scfOfferResponseMap.put("salesOf12Months", "0.00");
				
				if(null != response.getState() && response.getState().trim().length() > 0)
					scfOfferResponseMap.put("State", response.getState());
				else
					scfOfferResponseMap.put("State", "");
				
				if(null != response.getStatus() && response.getStatus().trim().length() > 0)
					scfOfferResponseMap.put("StatusID", response.getStatus());
				else
					scfOfferResponseMap.put("StatusID", "");
				
				if(null != response.getTenorOfPayment() && response.getTenorOfPayment().trim().length() > 0)
					scfOfferResponseMap.put("TenorOfPayment", response.getTenorOfPayment());
				else
					scfOfferResponseMap.put("TenorOfPayment", "0");
				
				if(null != response.getTIN() && response.getTIN().trim().length() > 0)
					scfOfferResponseMap.put("TIN", response.getTIN());
				else
					scfOfferResponseMap.put("TIN", "");
				
				if(null != response.getValidTo() && response.getValidTo().trim().length() > 0)
					scfOfferResponseMap.put("ValidTo", response.getValidTo());
				else
					scfOfferResponseMap.put("ValidTo", "");
				
				//Fields that are not available in websevices
				scfOfferResponseMap.put("AccountNo", "");
				scfOfferResponseMap.put("TotalBalance", "0.00");
				scfOfferResponseMap.put("TenorIndays", "");
				scfOfferResponseMap.put("PeakLimit", "0.00");
				scfOfferResponseMap.put("PeaklmtTenorindays", "");
				scfOfferResponseMap.put("OverdueBeyondCure", "0.00");
				scfOfferResponseMap.put("AsOnDate", "");
				scfOfferResponseMap.put("ReportDate", "");
				scfOfferResponseMap.put("OutstandingAmount", "0.00");
				scfOfferResponseMap.put("EContractID", "");
				scfOfferResponseMap.put("ECustomerID", "");
				scfOfferResponseMap.put("ApplicationNo", "");
				scfOfferResponseMap.put("CallBackStatus", "");
				scfOfferResponseMap.put("ECompleteTime", "");
				scfOfferResponseMap.put("ECompleteDate", "");
				scfOfferResponseMap.put("ApplicantID", "");
				scfOfferResponseMap.put("IsOverdue", "");
				scfOfferResponseMap.put("BlockFinancing", "");
				scfOfferResponseMap.put("OverdueBy", "");
				scfOfferResponseMap.put("BlockOrder", "");
				scfOfferResponseMap.put("BlockingReasonID", "");
				scfOfferResponseMap.put("BlockingReasonDesc", "");
				scfOfferResponseMap.put("DDBActive", "");
				scfOfferResponseMap.put("SupplyChainFinanceTxns", "");
				scfOfferResponseMap.put("DealerVendorFlag", "");
			}
			
			/*for (String key : scfOfferResponseMap.keySet()) {
			    response.getWriter().println(key + " - " + scfOfferResponseMap.get(key));
			}*/
			
//			responseValue = "Status:"+response.getStatus()+"|Message: "+response.getMessage()+"|OfferTenure:"+response.getOfferTenure();
			
		} catch (RemoteException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
//			responseValue = e.getMessage()+"---> Full Stack Trace: "+buffer.toString();
			scfOfferResponseMap.put("StatusID", "000003");
			scfOfferResponseMap.put("Message", e.getMessage());
			
			scfOfferResponseMap.put("ADDLNPRDINTRateSP", "");
			scfOfferResponseMap.put("AddlnTenorOfPymt", "");
			scfOfferResponseMap.put("Address1", "");
			scfOfferResponseMap.put("Address2", "");
			scfOfferResponseMap.put("Address3", "");
			scfOfferResponseMap.put("Address4", "");
			scfOfferResponseMap.put("Address5", "");
			scfOfferResponseMap.put("BusinessVintageOfDealer", "");
			scfOfferResponseMap.put("CIN", "");
			scfOfferResponseMap.put("City", "");
			scfOfferResponseMap.put("ConstitutionType", "");
			scfOfferResponseMap.put("CorporateID", "");
			scfOfferResponseMap.put("CorpRating", "");
			scfOfferResponseMap.put("DealerAssociationWithCorporate", "");
			scfOfferResponseMap.put("DealersOverallScoreByCorp", "");
			scfOfferResponseMap.put("DefIntSpread", "");
			scfOfferResponseMap.put("EligibilityStatus", "");
			scfOfferResponseMap.put("EntityType", "");
			scfOfferResponseMap.put("ErrorCode", "");
			scfOfferResponseMap.put("GSTIN", "");
			scfOfferResponseMap.put("InterestSpread", "");
			scfOfferResponseMap.put("InterestRateSpread", "");
			scfOfferResponseMap.put("MaxLimitPerCorp", "");
			scfOfferResponseMap.put("MCLR6Rate", "");
			scfOfferResponseMap.put("MobileNo", "");
			scfOfferResponseMap.put("NoOfChequeReturns", "");
			scfOfferResponseMap.put("OfferAmt", "");
			scfOfferResponseMap.put("Currency", "");
			scfOfferResponseMap.put("OfferTenure", "");
			scfOfferResponseMap.put("PAN", "");
			scfOfferResponseMap.put("PaymentDelayDays12Months", "");
			scfOfferResponseMap.put("Pincode", "");
			scfOfferResponseMap.put("ProcessingFee", "");
			scfOfferResponseMap.put("ProcessFeePerc", "");
			scfOfferResponseMap.put("PurchasesOf12Months", "");
			scfOfferResponseMap.put("Rate", "");
			scfOfferResponseMap.put("salesOf12Months", "");
			scfOfferResponseMap.put("State", "");
			scfOfferResponseMap.put("TenorOfPayment", "");
			scfOfferResponseMap.put("TIN", "");
			scfOfferResponseMap.put("ValidTo", "");
			//Fields that are not available in websevices
			scfOfferResponseMap.put("AccountNo", "");
			scfOfferResponseMap.put("TotalBalance", "");
			scfOfferResponseMap.put("TenorIndays", "");
			scfOfferResponseMap.put("PeakLimit", "");
			scfOfferResponseMap.put("PeaklmtTenorindays", "");
			scfOfferResponseMap.put("OverdueBeyondCure", "");
			scfOfferResponseMap.put("AsOnDate", "");
			scfOfferResponseMap.put("ReportDate", "");
			scfOfferResponseMap.put("OutstandingAmount", "");
			scfOfferResponseMap.put("EContractID", "");
			scfOfferResponseMap.put("ECustomerID", "");
			scfOfferResponseMap.put("ApplicationNo", "");
			scfOfferResponseMap.put("CallBackStatus", "");
			scfOfferResponseMap.put("ECompleteTime", "");
			scfOfferResponseMap.put("ECompleteDate", "");
			scfOfferResponseMap.put("ApplicantID", "");
			scfOfferResponseMap.put("IsOverdue", "");
			scfOfferResponseMap.put("BlockFinancing", "");
			scfOfferResponseMap.put("OverdueBy", "");
			scfOfferResponseMap.put("BlockOrder", "");
			scfOfferResponseMap.put("BlockingReasonID", "");
			scfOfferResponseMap.put("BlockingReasonDesc", "");
			scfOfferResponseMap.put("DDBActive", "");
			scfOfferResponseMap.put("SupplyChainFinanceTxns", "");
			scfOfferResponseMap.put("DealerVendorFlag", "");
			
		} catch (ServiceException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
//			responseValue = e.getMessage()+"---> Full Stack Trace: "+buffer.toString();
			scfOfferResponseMap.put("Status", "000002");
			scfOfferResponseMap.put("Message", e.getMessage());
			
			scfOfferResponseMap.put("ADDLNPRDINTRateSP", "");
			scfOfferResponseMap.put("AddlnTenorOfPymt", "");
			scfOfferResponseMap.put("Address1", "");
			scfOfferResponseMap.put("Address2", "");
			scfOfferResponseMap.put("Address3", "");
			scfOfferResponseMap.put("Address4", "");
			scfOfferResponseMap.put("Address5", "");
			scfOfferResponseMap.put("BusinessVintageOfDealer", "");
			scfOfferResponseMap.put("CIN", "");
			scfOfferResponseMap.put("City", "");
			scfOfferResponseMap.put("ConstitutionType", "");
			scfOfferResponseMap.put("CorporateID", "");
			scfOfferResponseMap.put("CorpRating", "");
			scfOfferResponseMap.put("DealerAssociationWithCorporate", "");
			scfOfferResponseMap.put("DealersOverallScoreByCorp", "");
			scfOfferResponseMap.put("DefIntSpread", "");
			scfOfferResponseMap.put("EligibilityStatus", "");
			scfOfferResponseMap.put("EntityType", "");
			scfOfferResponseMap.put("ErrorCode", "");
			scfOfferResponseMap.put("GSTIN", "");
			scfOfferResponseMap.put("InterestRateSpread", "");
			scfOfferResponseMap.put("MaxLimitPerCorp", "");
			scfOfferResponseMap.put("MCLR6Rate", "");
			scfOfferResponseMap.put("MobileNo", "");
			scfOfferResponseMap.put("NoOfChequeReturns", "");
			scfOfferResponseMap.put("OfferAmt", "");
			scfOfferResponseMap.put("Currency", "");
			scfOfferResponseMap.put("OfferTenure", "");
			scfOfferResponseMap.put("PAN", "");
			scfOfferResponseMap.put("PaymentDelayDays12Months", "");
			scfOfferResponseMap.put("Pincode", "");
			scfOfferResponseMap.put("ProcessingFee", "");
			scfOfferResponseMap.put("ProcessFeePerc", "");
			scfOfferResponseMap.put("PurchasesOf12Months", "");
			scfOfferResponseMap.put("Rate", "");
			scfOfferResponseMap.put("salesOf12Months", "");
			scfOfferResponseMap.put("State", "");
			scfOfferResponseMap.put("TenorOfPayment", "");
			scfOfferResponseMap.put("TIN", "");
			scfOfferResponseMap.put("ValidTo", "");
			//Fields that are not available in websevices
			scfOfferResponseMap.put("AccountNo", "");
			scfOfferResponseMap.put("TotalBalance", "");
			scfOfferResponseMap.put("TenorIndays", "");
			scfOfferResponseMap.put("PeakLimit", "");
			scfOfferResponseMap.put("PeaklmtTenorindays", "");
			scfOfferResponseMap.put("OverdueBeyondCure", "");
			scfOfferResponseMap.put("AsOnDate", "");
			scfOfferResponseMap.put("ReportDate", "");
			scfOfferResponseMap.put("OutstandingAmount", "");
			scfOfferResponseMap.put("EContractID", "");
			scfOfferResponseMap.put("ECustomerID", "");
			scfOfferResponseMap.put("ApplicationNo", "");
			scfOfferResponseMap.put("CallBackStatus", "");
			scfOfferResponseMap.put("ECompleteTime", "");
			scfOfferResponseMap.put("ECompleteDate", "");
			scfOfferResponseMap.put("ApplicantID", "");
			scfOfferResponseMap.put("IsOverdue", "");
			scfOfferResponseMap.put("BlockFinancing", "");
			scfOfferResponseMap.put("OverdueBy", "");
			scfOfferResponseMap.put("BlockOrder", "");
			scfOfferResponseMap.put("BlockingReasonID", "");
			scfOfferResponseMap.put("BlockingReasonDesc", "");
			scfOfferResponseMap.put("DDBActive", "");
			scfOfferResponseMap.put("SupplyChainFinanceTxns", "");
			scfOfferResponseMap.put("DealerVendorFlag", "");
		} catch(Exception e){
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			
//			responseValue = e.getMessage()+"---> Full Stack Trace: "+buffer.toString();
			scfOfferResponseMap.put("Status", "000002");
			scfOfferResponseMap.put("Message", e.getMessage());
			
			scfOfferResponseMap.put("ADDLNPRDINTRateSP", "");
			scfOfferResponseMap.put("AddlnTenorOfPymt", "");
			scfOfferResponseMap.put("Address1", "");
			scfOfferResponseMap.put("Address2", "");
			scfOfferResponseMap.put("Address3", "");
			scfOfferResponseMap.put("Address4", "");
			scfOfferResponseMap.put("Address5", "");
			scfOfferResponseMap.put("BusinessVintageOfDealer", "");
			scfOfferResponseMap.put("CIN", "");
			scfOfferResponseMap.put("City", "");
			scfOfferResponseMap.put("ConstitutionType", "");
			scfOfferResponseMap.put("CorporateID", "");
			scfOfferResponseMap.put("CorpRating", "");
			scfOfferResponseMap.put("DealerAssociationWithCorporate", "");
			scfOfferResponseMap.put("DealersOverallScoreByCorp", "");
			scfOfferResponseMap.put("DefIntSpread", "");
			scfOfferResponseMap.put("EligibilityStatus", "");
			scfOfferResponseMap.put("EntityType", "");
			scfOfferResponseMap.put("ErrorCode", "");
			scfOfferResponseMap.put("GSTIN", "");
			scfOfferResponseMap.put("InterestRateSpread", "");
			scfOfferResponseMap.put("MaxLimitPerCorp", "");
			scfOfferResponseMap.put("MCLR6Rate", "");
			scfOfferResponseMap.put("MobileNo", "");
			scfOfferResponseMap.put("NoOfChequeReturns", "");
			scfOfferResponseMap.put("OfferAmt", "");
			scfOfferResponseMap.put("Currency", "");
			scfOfferResponseMap.put("OfferTenure", "");
			scfOfferResponseMap.put("PAN", "");
			scfOfferResponseMap.put("PaymentDelayDays12Months", "");
			scfOfferResponseMap.put("Pincode", "");
			scfOfferResponseMap.put("ProcessingFee", "");
			scfOfferResponseMap.put("ProcessFeePerc", "");
			scfOfferResponseMap.put("PurchasesOf12Months", "");
			scfOfferResponseMap.put("Rate", "");
			scfOfferResponseMap.put("salesOf12Months", "");
			scfOfferResponseMap.put("State", "");
			scfOfferResponseMap.put("TenorOfPayment", "");
			scfOfferResponseMap.put("TIN", "");
			scfOfferResponseMap.put("ValidTo", "");
			//Fields that are not available in websevices
			scfOfferResponseMap.put("AccountNo", "");
			scfOfferResponseMap.put("TotalBalance", "");
			scfOfferResponseMap.put("TenorIndays", "");
			scfOfferResponseMap.put("PeakLimit", "");
			scfOfferResponseMap.put("PeaklmtTenorindays", "");
			scfOfferResponseMap.put("OverdueBeyondCure", "");
			scfOfferResponseMap.put("AsOnDate", "");
			scfOfferResponseMap.put("ReportDate", "");
			scfOfferResponseMap.put("OutstandingAmount", "");
			scfOfferResponseMap.put("EContractID", "");
			scfOfferResponseMap.put("ECustomerID", "");
			scfOfferResponseMap.put("ApplicationNo", "");
			scfOfferResponseMap.put("CallBackStatus", "");
			scfOfferResponseMap.put("ECompleteTime", "");
			scfOfferResponseMap.put("ECompleteDate", "");
			scfOfferResponseMap.put("ApplicantID", "");
			scfOfferResponseMap.put("IsOverdue", "");
			scfOfferResponseMap.put("BlockFinancing", "");
			scfOfferResponseMap.put("OverdueBy", "");
			scfOfferResponseMap.put("BlockOrder", "");
			scfOfferResponseMap.put("BlockingReasonID", "");
			scfOfferResponseMap.put("BlockingReasonDesc", "");
			scfOfferResponseMap.put("DDBActive", "");
			scfOfferResponseMap.put("SupplyChainFinanceTxns", "");
			scfOfferResponseMap.put("DealerVendorFlag", "");
		}
		
		return scfOfferResponseMap;
	}
}

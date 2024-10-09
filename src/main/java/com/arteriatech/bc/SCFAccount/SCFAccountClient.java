package com.arteriatech.bc.SCFAccount;

import java.util.HashMap;
import java.util.Map;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;

public class SCFAccountClient {

	public Map<String, String> callSCFAccountClient(String accNo, boolean debug) {
		
		String message="";
		Map<String, String> scfAccountResponseMap = new HashMap<String, String>();
		SCFAccount_Request scfAccountRequest = new SCFAccount_Request();
		CommonUtils commonUtils = new CommonUtils();
		
		String endPointURL = "", system="";
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"SCFAccount";
		
			scfAccountRequest.setDealerAccountNo(accNo);
			
			SCFAccountServiceLocator scfLocator = new SCFAccountServiceLocator();
			scfLocator.setEndpointAddress("SCFAccountPort", endPointURL);
			SCFAccount service = scfLocator.getSCFAccountPort();
			
			SCFAccount_Response scfResponse = service.SCFAccount(scfAccountRequest);
			if(null != scfResponse.getStatus().getStatus() && scfResponse.getStatus().getStatus().trim().length() > 0){
				if(scfResponse.getStatus().getStatus().trim().equalsIgnoreCase("000001")){
					if(null != scfResponse.getStatus().getStatus() && scfResponse.getStatus().getStatus().trim().length() > 0)
						scfAccountResponseMap.put("Status", scfResponse.getStatus().getStatus());
					else
						scfAccountResponseMap.put("Status", "");
					if(null != scfResponse.getStatus().getResponseCode() && scfResponse.getStatus().getResponseCode().trim().length() > 0)
						scfAccountResponseMap.put("ResponseCode", scfResponse.getStatus().getResponseCode());
					else
						scfAccountResponseMap.put("ResponseCode", "");

					if(null != scfResponse.getStatus().getMessage() && scfResponse.getStatus().getMessage().trim().length() > 0)
						scfAccountResponseMap.put("Message", scfResponse.getStatus().getMessage());
					else
						scfAccountResponseMap.put("Message", "");

					if(null != scfResponse.getStatement().getDueDays() && scfResponse.getStatement().getDueDays().trim().length() > 0)
						scfAccountResponseMap.put("DueDays", scfResponse.getStatement().getDueDays());
					else
						scfAccountResponseMap.put("DueDays", "");

					if(null != scfResponse.getStatement().getFeeOverdueAmount() && scfResponse.getStatement().getFeeOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("FeeOverdueAmount", scfResponse.getStatement().getFeeOverdueAmount());
					else
						scfAccountResponseMap.put("FeeOverdueAmount", "");

					if(null != scfResponse.getStatement().getFreezeFlag() && scfResponse.getStatement().getFreezeFlag().trim().length() > 0)
						scfAccountResponseMap.put("FreezeFlag", scfResponse.getStatement().getFreezeFlag());
					else
						scfAccountResponseMap.put("FreezeFlag", "");

					if(null != scfResponse.getStatement().getInterestOverdueAmount() && scfResponse.getStatement().getInterestOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("InterestOverdueAmount", scfResponse.getStatement().getInterestOverdueAmount());
					else
						scfAccountResponseMap.put("InterestOverdueAmount", "");

					if(null != scfResponse.getStatement().getIsPrincipalOverdue() && scfResponse.getStatement().getIsPrincipalOverdue().trim().length() > 0)
						scfAccountResponseMap.put("IsPrincipalOverdue", scfResponse.getStatement().getIsPrincipalOverdue());
					else
						scfAccountResponseMap.put("IsPrincipalOverdue", "");

					if(null != scfResponse.getStatement().getOverdueDays() && scfResponse.getStatement().getOverdueDays().trim().length() > 0)
						scfAccountResponseMap.put("OverdueDays", scfResponse.getStatement().getOverdueDays());
					else
						scfAccountResponseMap.put("OverdueDays", "");

					if(null != scfResponse.getStatement().getPrincipalOverdueAmount() && scfResponse.getStatement().getPrincipalOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("PrincipalOverdueAmount", scfResponse.getStatement().getPrincipalOverdueAmount());
					else
						scfAccountResponseMap.put("PrincipalOverdueAmount", "");

					if(null != scfResponse.getStatement().getReportDate() && scfResponse.getStatement().getReportDate().trim().length() > 0)
						scfAccountResponseMap.put("ReportDate", scfResponse.getStatement().getReportDate());
					else
						scfAccountResponseMap.put("ReportDate", "");

					if(null != scfResponse.getStatement().getSanctionLimit() && scfResponse.getStatement().getSanctionLimit().trim().length() > 0)
						scfAccountResponseMap.put("SanctionLimit", scfResponse.getStatement().getSanctionLimit());
					else
						scfAccountResponseMap.put("SanctionLimit", "");

					if(null != scfResponse.getStatement().getTotalOverdueBeyondCureAmount() && scfResponse.getStatement().getTotalOverdueBeyondCureAmount().trim().length() > 0)
						scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", scfResponse.getStatement().getTotalOverdueBeyondCureAmount());
					else
						scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", "");

					if(null != scfResponse.getStatement().getTotalOverdueBeyondCureDays() && scfResponse.getStatement().getTotalOverdueBeyondCureDays().trim().length() > 0)
						scfAccountResponseMap.put("TotalOverdueBeyondCureDays", scfResponse.getStatement().getTotalOverdueBeyondCureDays());
					else
						scfAccountResponseMap.put("TotalOverdueBeyondCureDays", "");
				}else{
					if(null != scfResponse.getStatus().getStatus() && scfResponse.getStatus().getStatus().trim().length() > 0)
						scfAccountResponseMap.put("Status", scfResponse.getStatus().getStatus());
					else
						scfAccountResponseMap.put("Status", "");

					if(null != scfResponse.getStatus().getResponseCode() && scfResponse.getStatus().getResponseCode().trim().length() > 0)
						scfAccountResponseMap.put("ResponseCode", scfResponse.getStatus().getResponseCode());
					else
						scfAccountResponseMap.put("ResponseCode", "");

					if(null != scfResponse.getStatus().getMessage() && scfResponse.getStatus().getMessage().trim().length() > 0)
						scfAccountResponseMap.put("Message", scfResponse.getStatus().getMessage());
					else
						scfAccountResponseMap.put("Message", "");

					if(null != scfResponse.getStatement().getDueDays() && scfResponse.getStatement().getDueDays().trim().length() > 0)
						scfAccountResponseMap.put("DueDays", scfResponse.getStatement().getDueDays());
					else
						scfAccountResponseMap.put("DueDays", "");

					if(null != scfResponse.getStatement().getFeeOverdueAmount() && scfResponse.getStatement().getFeeOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("FeeOverdueAmount", scfResponse.getStatement().getFeeOverdueAmount());
					else
						scfAccountResponseMap.put("FeeOverdueAmount", "");

					if(null != scfResponse.getStatement().getFreezeFlag() && scfResponse.getStatement().getFreezeFlag().trim().length() > 0)
						scfAccountResponseMap.put("FreezeFlag", scfResponse.getStatement().getFreezeFlag());
					else
						scfAccountResponseMap.put("FreezeFlag", "");

					if(null != scfResponse.getStatement().getInterestOverdueAmount() && scfResponse.getStatement().getInterestOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("InterestOverdueAmount", scfResponse.getStatement().getInterestOverdueAmount());
					else
						scfAccountResponseMap.put("InterestOverdueAmount", "");

					if(null != scfResponse.getStatement().getIsPrincipalOverdue() && scfResponse.getStatement().getIsPrincipalOverdue().trim().length() > 0)
						scfAccountResponseMap.put("IsPrincipalOverdue", scfResponse.getStatement().getIsPrincipalOverdue());
					else
						scfAccountResponseMap.put("IsPrincipalOverdue", "");

					if(null != scfResponse.getStatement().getOverdueDays() && scfResponse.getStatement().getOverdueDays().trim().length() > 0)
						scfAccountResponseMap.put("OverdueDays", scfResponse.getStatement().getOverdueDays());
					else
						scfAccountResponseMap.put("OverdueDays", "");

					if(null != scfResponse.getStatement().getPrincipalOverdueAmount() && scfResponse.getStatement().getPrincipalOverdueAmount().trim().length() > 0)
						scfAccountResponseMap.put("PrincipalOverdueAmount", scfResponse.getStatement().getPrincipalOverdueAmount());
					else
						scfAccountResponseMap.put("PrincipalOverdueAmount", "");

					if(null != scfResponse.getStatement().getReportDate() && scfResponse.getStatement().getReportDate().trim().length() > 0)
						scfAccountResponseMap.put("ReportDate", scfResponse.getStatement().getReportDate());
					else
						scfAccountResponseMap.put("ReportDate", "");

					if(null != scfResponse.getStatement().getSanctionLimit() && scfResponse.getStatement().getSanctionLimit().trim().length() > 0)
						scfAccountResponseMap.put("SanctionLimit", scfResponse.getStatement().getSanctionLimit());
					else
						scfAccountResponseMap.put("SanctionLimit", "");
					
					if(null != scfResponse.getStatement().getTotalOverdueBeyondCureAmount() && scfResponse.getStatement().getTotalOverdueBeyondCureAmount().trim().length() > 0)
						scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", scfResponse.getStatement().getTotalOverdueBeyondCureAmount());
					else
						scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", "");

					if(null != scfResponse.getStatement().getTotalOverdueBeyondCureDays() && scfResponse.getStatement().getTotalOverdueBeyondCureDays().trim().length() > 0)
						scfAccountResponseMap.put("TotalOverdueBeyondCureDays", scfResponse.getStatement().getTotalOverdueBeyondCureDays());
					else
						scfAccountResponseMap.put("TotalOverdueBeyondCureDays", "");
				}
			} else {
				scfAccountResponseMap.put("Status", "000002");

				if(null != scfResponse.getStatus().getResponseCode() && scfResponse.getStatus().getResponseCode().trim().length() > 0)
					scfAccountResponseMap.put("ResponseCode", scfResponse.getStatus().getResponseCode());
				else
					scfAccountResponseMap.put("ResponseCode", "");
				
				if(null != scfResponse.getStatus().getMessage() && scfResponse.getStatus().getMessage().trim().length() > 0)
					scfAccountResponseMap.put("Message", scfResponse.getStatus().getMessage());
				else
					scfAccountResponseMap.put("Message", "");

				if(null != scfResponse.getStatement().getDueDays() && scfResponse.getStatement().getDueDays().trim().length() > 0)
					scfAccountResponseMap.put("DueDays", scfResponse.getStatement().getDueDays());
				else
					scfAccountResponseMap.put("DueDays", "");

				if(null != scfResponse.getStatement().getFeeOverdueAmount() && scfResponse.getStatement().getFeeOverdueAmount().trim().length() > 0)
					scfAccountResponseMap.put("FeeOverdueAmount", scfResponse.getStatement().getFeeOverdueAmount());
				else
					scfAccountResponseMap.put("FeeOverdueAmount", "");

				if(null != scfResponse.getStatement().getFreezeFlag() && scfResponse.getStatement().getFreezeFlag().trim().length() > 0)
					scfAccountResponseMap.put("FreezeFlag", scfResponse.getStatement().getFreezeFlag());
				else
					scfAccountResponseMap.put("FreezeFlag", "");

				if(null != scfResponse.getStatement().getInterestOverdueAmount() && scfResponse.getStatement().getInterestOverdueAmount().trim().length() > 0)
					scfAccountResponseMap.put("InterestOverdueAmount", scfResponse.getStatement().getInterestOverdueAmount());
				else
					scfAccountResponseMap.put("InterestOverdueAmount", "");
				
				if(null != scfResponse.getStatement().getIsPrincipalOverdue() && scfResponse.getStatement().getIsPrincipalOverdue().trim().length() > 0)
					scfAccountResponseMap.put("IsPrincipalOverdue", scfResponse.getStatement().getIsPrincipalOverdue());
				else
					scfAccountResponseMap.put("IsPrincipalOverdue", "");

				if(null != scfResponse.getStatement().getOverdueDays() && scfResponse.getStatement().getOverdueDays().trim().length() > 0)
					scfAccountResponseMap.put("OverdueDays", scfResponse.getStatement().getOverdueDays());
				else
					scfAccountResponseMap.put("OverdueDays", "");

				if(null != scfResponse.getStatement().getPrincipalOverdueAmount() && scfResponse.getStatement().getPrincipalOverdueAmount().trim().length() > 0)
					scfAccountResponseMap.put("PrincipalOverdueAmount", scfResponse.getStatement().getPrincipalOverdueAmount());
				else
					scfAccountResponseMap.put("PrincipalOverdueAmount", "");

				if(null != scfResponse.getStatement().getReportDate() && scfResponse.getStatement().getReportDate().trim().length() > 0)
					scfAccountResponseMap.put("ReportDate", scfResponse.getStatement().getReportDate());
				else
					scfAccountResponseMap.put("ReportDate", "");

				if(null != scfResponse.getStatement().getSanctionLimit() && scfResponse.getStatement().getSanctionLimit().trim().length() > 0)
					scfAccountResponseMap.put("SanctionLimit", scfResponse.getStatement().getSanctionLimit());
				else
					scfAccountResponseMap.put("SanctionLimit", "");

				if(null != scfResponse.getStatement().getTotalOverdueBeyondCureAmount() && scfResponse.getStatement().getTotalOverdueBeyondCureAmount().trim().length() > 0)
					scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", scfResponse.getStatement().getTotalOverdueBeyondCureAmount());
				else
					scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", "");
				
				if(null != scfResponse.getStatement().getTotalOverdueBeyondCureDays() && scfResponse.getStatement().getTotalOverdueBeyondCureDays().trim().length() > 0)
					scfAccountResponseMap.put("TotalOverdueBeyondCureDays", scfResponse.getStatement().getTotalOverdueBeyondCureDays());
				else
					scfAccountResponseMap.put("TotalOverdueBeyondCureDays", "");
			}
		}catch (Exception e) {
			message="001";
			scfAccountResponseMap.put("ResponseCode", message);
			scfAccountResponseMap.put("Message", e.getLocalizedMessage());
			scfAccountResponseMap.put("Status", "000002");
			scfAccountResponseMap.put("TotalOverdueBeyondCureDays", "");
			scfAccountResponseMap.put("TotalOverdueBeyondCureAmount", "");
			scfAccountResponseMap.put("SanctionLimit", "");
			scfAccountResponseMap.put("ReportDate", "");
			scfAccountResponseMap.put("PrincipalOverdueAmount", "");
			scfAccountResponseMap.put("OverdueDays", "");
			scfAccountResponseMap.put("InterestOverdueAmount", "");
			scfAccountResponseMap.put("FreezeFlag", "");
			scfAccountResponseMap.put("IsPrincipalOverdue", "");
			scfAccountResponseMap.put("DueDays", "");
			scfAccountResponseMap.put("FeeOverdueAmount", "");
		}
		return scfAccountResponseMap;
	}

}

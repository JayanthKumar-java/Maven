package com.arteriatech.bc.CurrentAccount;
import java.util.HashMap;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFOffer.SCFOfferServiceLocator;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;

public class AccountClient {
	public Map<String, String> callAccountsWebservice(HttpServletRequest servletRequest, HttpServletResponse response, 
			Map<String, String> userAccountsEntry, Map<String, String> userRegResponseMap, String aggregatorID, boolean debug){
		String responseValue = "", endPointURL="", system="";
		Map<String,String> accountsResponseMap = new HashMap<String,String>();
		AccountRequest accountRequest = new AccountRequest();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			system = commonUtils.getODataDestinationProperties("System",DestinationUtils.BANK_CONNECT);
			endPointURL = commonUtils.getODataDestinationProperties("URL",DestinationUtils.BANK_CONNECT);
			String lenderCode = commonUtils.getODataDestinationProperties("LenderCode", DestinationUtils.PYGWHANA);
			if(debug){
			response.getWriter().println("lenderCode:"+lenderCode);
			}
			endPointURL = endPointURL+"LinkedAccount/CurrentAccount";
			accountRequest.setAccountNumber(userAccountsEntry.get("BankAccntNo"));
			accountRequest.setAggregatorID(aggregatorID);
			accountRequest.setCorporateID(userRegResponseMap.get("CorpId"));
			accountRequest.setUserID(userRegResponseMap.get("UserId"));
			accountRequest.setUserRegistrationID(userRegResponseMap.get("UserRegId"));
			if(lenderCode!=null && !lenderCode.equalsIgnoreCase("")&&!lenderCode.equalsIgnoreCase("E106")){
				accountRequest.setLenderCode(lenderCode);
			}else{
				accountRequest.setLenderCode("");
			}
			if(debug){
				response.getWriter().println("callAccountsWebservice-endPointURL: "+endPointURL);
				response.getWriter().println("callAccountsWebservice-BankAccntNo: "+userAccountsEntry.get("BankAccntNo"));
				response.getWriter().println("callAccountsWebservice-aggregatorID: "+aggregatorID);
				response.getWriter().println("callAccountsWebservice-UserId: "+userRegResponseMap.get("UserId"));
				response.getWriter().println("callAccountsWebservice-UserRegId: "+userRegResponseMap.get("UserRegId"));
				response.getWriter().println("callAccountsWebservice-CorpId: "+userRegResponseMap.get("CorpId"));
			}
			
			AccountServiceLocator asLocator = new AccountServiceLocator();
			asLocator.setEndpointAddress("AccountPort", endPointURL);
			Account accountService = asLocator.getAccountPort();
			
			com.arteriatech.bc.CurrentAccount.AccountResponse accountResponse = accountService.account(accountRequest);
			if(debug){
				response.getWriter().println("Status CPI Response:"+accountResponse.getStatus());
			}
			if(null == accountResponse.getStatus() || accountResponse.getStatus().trim().length() == 0 
					|| accountResponse.getStatus().equalsIgnoreCase("000002") || accountResponse.getStatus().trim().equalsIgnoreCase("")){
				accountsResponseMap.put("Error", "059");
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
		
					accountsResponseMap.put("ErrorDesc", "");
					accountsResponseMap.put("ErrorSource", "");
					accountsResponseMap.put("ErrorType", "");
					accountsResponseMap.put("AccountClosedDate", "");
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
			
					accountsResponseMap.put("CustomerId", "");
				
					accountsResponseMap.put("CustomerName", "");
				
					accountsResponseMap.put("CustomerShortName", "");
				
					accountsResponseMap.put("CustomerTitle", "");
				
					accountsResponseMap.put("IsAccountClosed", "");
					
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				
					accountsResponseMap.put("IsCFSOD", "");
				
				
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
		
					accountsResponseMap.put("ModeOfOperation", "");
				
				
				if(null == accountResponse.getStatus())
					accountsResponseMap.put("Status", "");
				else
					accountsResponseMap.put("Status", accountResponse.getStatus());
			}else{
				accountsResponseMap.put("Error", "");
				/*accountsResponseMap.put("AccountClosedDate", accountResponse.getAccountClosedDate());
				accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				accountsResponseMap.put("CustomerId", accountResponse.getCustomerId());
				accountsResponseMap.put("CustomerName", accountResponse.getCustomerName());
				accountsResponseMap.put("CustomerShortName", accountResponse.getCustomerShortName());
				accountsResponseMap.put("CustomerTitle", accountResponse.getCustomerTitle());
				accountsResponseMap.put("IsAccountClosed", accountResponse.getIsAccountClosed());
				accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				accountsResponseMap.put("IsCFSOD", accountResponse.getIsCFSOD());
				accountsResponseMap.put("Message", accountResponse.getMessage());
				accountsResponseMap.put("ModeOfOperation", accountResponse.getModeOfOperation());
				accountsResponseMap.put("Status", accountResponse.getStatus());*/
				
				
					accountsResponseMap.put("AccountClosedDate", "");
				
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				
					accountsResponseMap.put("CustomerId", "");
					accountsResponseMap.put("CustomerName", "");
					accountsResponseMap.put("CustomerShortName", "");
					accountsResponseMap.put("CustomerTitle", "");
					accountsResponseMap.put("IsAccountClosed", "");
				
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
					accountsResponseMap.put("IsCFSOD", "");
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
					accountsResponseMap.put("ModeOfOperation", "");
				
				if(null == accountResponse.getStatus())
					accountsResponseMap.put("Status", "");
				else
					accountsResponseMap.put("Status", accountResponse.getStatus());
				
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
					accountsResponseMap.put("ErrorDesc", "");
					accountsResponseMap.put("ErrorSource", "");
					accountsResponseMap.put("ErrorType", "");
				
			}
		}catch (Exception e) {
			accountsResponseMap.put("Error", "059");
			accountsResponseMap.put("Status", "000002");
			accountsResponseMap.put("Message", e.getMessage());
			accountsResponseMap.put("AccountClosedDate", "");
			accountsResponseMap.put("AccountStatus", "");
			accountsResponseMap.put("AccountType", "");
			accountsResponseMap.put("CustomerId", "");
			accountsResponseMap.put("CustomerName", "");
			accountsResponseMap.put("CustomerShortName", "");
			accountsResponseMap.put("CustomerTitle", "");
			accountsResponseMap.put("IsAccountClosed", "");
			accountsResponseMap.put("IsAccountLinked", "");
			accountsResponseMap.put("IsCFSOD", "");
			accountsResponseMap.put("ModeOfOperation", "");
			accountsResponseMap.put("ErrorType", "");
			accountsResponseMap.put("ErrorSource", "");
			accountsResponseMap.put("ErrorDesc", "");
			accountsResponseMap.put("ErrorCode", "001");
		}
		
		return accountsResponseMap;
	}
}


package com.arteriatech.bc.Account;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFOffer.SCFOfferServiceLocator;
import com.arteriatech.pg.CommonUtils;
import com.arteriatech.bc.Account.AccountRequest;
import com.arteriatech.support.DestinationUtils;

public class AccountClient {
	public Map<String, String> callAccountsWebservice(HttpServletRequest servletRequest, HttpServletResponse servletResponse, 
			Map<String, String> userAccountsEntry, Map<String, String> userRegResponseMap, String aggregatorID, boolean debug){
		String responseValue = "", endPointURL="", system="";
		Map<String,String> accountsResponseMap = new HashMap<String,String>();
		AccountRequest accountRequest = new AccountRequest();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"LinkedAccount/CurrentAccount";
			accountRequest.setAccountNumber(userAccountsEntry.get("BankAccntNo"));
			accountRequest.setAggregatorID(aggregatorID);
			accountRequest.setCorporateID(userRegResponseMap.get("CorpId"));
			accountRequest.setUserID(userRegResponseMap.get("UserId"));
			accountRequest.setUserRegistrationID(userRegResponseMap.get("UserRegId"));
			
			if(debug){
				servletResponse.getWriter().println("callAccountsWebservice-endPointURL: "+endPointURL);
				servletResponse.getWriter().println("callAccountsWebservice-BankAccntNo: "+userAccountsEntry.get("BankAccntNo"));
				servletResponse.getWriter().println("callAccountsWebservice-aggregatorID: "+aggregatorID);
				servletResponse.getWriter().println("callAccountsWebservice-UserId: "+userRegResponseMap.get("UserId"));
				servletResponse.getWriter().println("callAccountsWebservice-UserRegId: "+userRegResponseMap.get("UserRegId"));
				servletResponse.getWriter().println("callAccountsWebservice-CorpId: "+userRegResponseMap.get("CorpId"));
			}
			
			AccountServiceLocator asLocator = new AccountServiceLocator();
			asLocator.setEndpointAddress("AccountPort", endPointURL);
			Account accountService = asLocator.getAccountPort();
			
			LinkedAccount_CurrentAccount_2_Portal accountResponse = accountService.account(accountRequest);
			
			if(null == accountResponse.getStatus() || accountResponse.getStatus().trim().length() == 0 
					|| accountResponse.getStatus().equalsIgnoreCase("000002") || accountResponse.getStatus().trim().equalsIgnoreCase("")){
				accountsResponseMap.put("Error", "059");
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
				
				if(null == accountResponse.getErrorDesc())
					accountsResponseMap.put("ErrorDesc", "");
				else
					accountsResponseMap.put("ErrorDesc", accountResponse.getErrorDesc());
				
				if(null == accountResponse.getErrorSource())
					accountsResponseMap.put("ErrorSource", "");
				else
					accountsResponseMap.put("ErrorSource", accountResponse.getErrorSource());
				
				if(null == accountResponse.getErrorType())
					accountsResponseMap.put("ErrorType", "");
				else
					accountsResponseMap.put("ErrorType", accountResponse.getErrorType());
				
				if(null == accountResponse.getAccountClosedDate())
					accountsResponseMap.put("AccountClosedDate", "");
				else
					accountsResponseMap.put("AccountClosedDate", accountResponse.getAccountClosedDate());
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				
				if(null == accountResponse.getCustomerId())
					accountsResponseMap.put("CustomerId", "");
				else
					accountsResponseMap.put("CustomerId", accountResponse.getCustomerId());
				
				if(null == accountResponse.getCustomerName())
					accountsResponseMap.put("CustomerName", "");
				else
					accountsResponseMap.put("CustomerName", accountResponse.getCustomerName());
				
				if(null == accountResponse.getCustomerShortName())
					accountsResponseMap.put("CustomerShortName", "");
				else
					accountsResponseMap.put("CustomerShortName", accountResponse.getCustomerShortName());
				
				if(null == accountResponse.getCustomerTitle())
					accountsResponseMap.put("CustomerTitle", "");
				else
					accountsResponseMap.put("CustomerTitle", accountResponse.getCustomerTitle());
				
				if(null == accountResponse.getIsAccountClosed())
					accountsResponseMap.put("IsAccountClosed", "");
				else
					accountsResponseMap.put("IsAccountClosed", accountResponse.getIsAccountClosed());
				
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				
				if(null == accountResponse.getIsCFSOD())
					accountsResponseMap.put("IsCFSOD", "");
				else
					accountsResponseMap.put("IsCFSOD", accountResponse.getIsCFSOD());
				
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
				
				if(null == accountResponse.getModeOfOperation())
					accountsResponseMap.put("ModeOfOperation", "");
				else
					accountsResponseMap.put("ModeOfOperation", accountResponse.getModeOfOperation());
				
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
				
				if(null == accountResponse.getAccountClosedDate())
					accountsResponseMap.put("AccountClosedDate", "");
				else
					accountsResponseMap.put("AccountClosedDate", accountResponse.getAccountClosedDate());
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				
				if(null == accountResponse.getCustomerId())
					accountsResponseMap.put("CustomerId", "");
				else
					accountsResponseMap.put("CustomerId", accountResponse.getCustomerId());
				
				if(null == accountResponse.getCustomerName())
					accountsResponseMap.put("CustomerName", "");
				else
					accountsResponseMap.put("CustomerName", accountResponse.getCustomerName());
				
				if(null == accountResponse.getCustomerShortName())
					accountsResponseMap.put("CustomerShortName", "");
				else
					accountsResponseMap.put("CustomerShortName", accountResponse.getCustomerShortName());
				
				if(null == accountResponse.getCustomerTitle())
					accountsResponseMap.put("CustomerTitle", "");
				else
					accountsResponseMap.put("CustomerTitle", accountResponse.getCustomerTitle());
				
				if(null == accountResponse.getIsAccountClosed())
					accountsResponseMap.put("IsAccountClosed", "");
				else
					accountsResponseMap.put("IsAccountClosed", accountResponse.getIsAccountClosed());
				
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				
				if(null == accountResponse.getIsCFSOD())
					accountsResponseMap.put("IsCFSOD", "");
				else
					accountsResponseMap.put("IsCFSOD", accountResponse.getIsCFSOD());
				
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
				
				if(null == accountResponse.getModeOfOperation())
					accountsResponseMap.put("ModeOfOperation", "");
				else
					accountsResponseMap.put("ModeOfOperation", accountResponse.getModeOfOperation());
				
				if(null == accountResponse.getStatus())
					accountsResponseMap.put("Status", "");
				else
					accountsResponseMap.put("Status", accountResponse.getStatus());
				
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
				
				if(null == accountResponse.getErrorDesc())
					accountsResponseMap.put("ErrorDesc", "");
				else
					accountsResponseMap.put("ErrorDesc", accountResponse.getErrorDesc());
				
				if(null == accountResponse.getErrorSource())
					accountsResponseMap.put("ErrorSource", "");
				else
					accountsResponseMap.put("ErrorSource", accountResponse.getErrorSource());
				
				if(null == accountResponse.getErrorType())
					accountsResponseMap.put("ErrorType", "");
				else
					accountsResponseMap.put("ErrorType", accountResponse.getErrorType());
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


	public Map<String, String> callAccountsWebserviceForAxis(HttpServletRequest servletRequest, HttpServletResponse servletResponse, 
			Map<String, String> userAccountsEntry, Map<String, String> userRegResponseMap, String aggregatorID, boolean debug){
		String responseValue = "", endPointURL="", system="";
		Map<String,String> accountsResponseMap = new HashMap<String,String>();
		AccountRequest accountRequest = new AccountRequest();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			system = commonUtils.getODataDestinationProperties("System",DestinationUtils.BANK_CONNECT);
			endPointURL = commonUtils.getODataDestinationProperties("URL",DestinationUtils.BANK_CONNECT);
			endPointURL = endPointURL+"LinkedAccount/CurrentAccount";
			accountRequest.setAccountNumber(userAccountsEntry.get("BankAccntNo"));
			accountRequest.setAggregatorID(aggregatorID);
			accountRequest.setCorporateID(userRegResponseMap.get("CorpId"));
			accountRequest.setUserID(userRegResponseMap.get("UserId"));
			accountRequest.setUserRegistrationID(userRegResponseMap.get("UserRegId"));
			String lenderCode = commonUtils.getODataDestinationProperties("LenderCode", DestinationUtils.PYGWHANA);
			if (lenderCode != null && !lenderCode.equalsIgnoreCase("")&&!lenderCode.equalsIgnoreCase("E106")) {
				accountRequest.setLenderCode(lenderCode);
			}
			if(debug){
				servletResponse.getWriter().println("callAccountsWebservice-endPointURL: "+endPointURL);
				servletResponse.getWriter().println("callAccountsWebservice-BankAccntNo: "+userAccountsEntry.get("BankAccntNo"));
				servletResponse.getWriter().println("callAccountsWebservice-aggregatorID: "+aggregatorID);
				servletResponse.getWriter().println("callAccountsWebservice-UserId: "+userRegResponseMap.get("UserId"));
				servletResponse.getWriter().println("callAccountsWebservice-UserRegId: "+userRegResponseMap.get("UserRegId"));
				servletResponse.getWriter().println("callAccountsWebservice-CorpId: "+userRegResponseMap.get("CorpId"));
				servletResponse.getWriter().println("callAccountsWebservice-LenderCode: "+lenderCode);
			}
			AccountServiceLocator asLocator = new AccountServiceLocator();
			asLocator.setEndpointAddress("AccountPort", endPointURL);
			Account accountService = asLocator.getAccountPort();
			
			LinkedAccount_CurrentAccount_2_Portal accountResponse = accountService.account(accountRequest);
			
			if(null == accountResponse.getStatus() || accountResponse.getStatus().trim().length() == 0 
					|| accountResponse.getStatus().equalsIgnoreCase("000002") || accountResponse.getStatus().trim().equalsIgnoreCase("")){
				accountsResponseMap.put("Error", "059");
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
				
				if(null == accountResponse.getErrorDesc())
					accountsResponseMap.put("ErrorDesc", "");
				else
					accountsResponseMap.put("ErrorDesc", accountResponse.getErrorDesc());
				
				if(null == accountResponse.getErrorSource())
					accountsResponseMap.put("ErrorSource", "");
				else
					accountsResponseMap.put("ErrorSource", accountResponse.getErrorSource());
				
				if(null == accountResponse.getErrorType())
					accountsResponseMap.put("ErrorType", "");
				else
					accountsResponseMap.put("ErrorType", accountResponse.getErrorType());
				
				if(null == accountResponse.getAccountClosedDate())
					accountsResponseMap.put("AccountClosedDate", "");
				else
					accountsResponseMap.put("AccountClosedDate", accountResponse.getAccountClosedDate());
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				
				if(null == accountResponse.getCustomerId())
					accountsResponseMap.put("CustomerId", "");
				else
					accountsResponseMap.put("CustomerId", accountResponse.getCustomerId());
				
				if(null == accountResponse.getCustomerName())
					accountsResponseMap.put("CustomerName", "");
				else
					accountsResponseMap.put("CustomerName", accountResponse.getCustomerName());
				
				if(null == accountResponse.getCustomerShortName())
					accountsResponseMap.put("CustomerShortName", "");
				else
					accountsResponseMap.put("CustomerShortName", accountResponse.getCustomerShortName());
				
				if(null == accountResponse.getCustomerTitle())
					accountsResponseMap.put("CustomerTitle", "");
				else
					accountsResponseMap.put("CustomerTitle", accountResponse.getCustomerTitle());
				
				if(null == accountResponse.getIsAccountClosed())
					accountsResponseMap.put("IsAccountClosed", "");
				else
					accountsResponseMap.put("IsAccountClosed", accountResponse.getIsAccountClosed());
				
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				
				if(null == accountResponse.getIsCFSOD())
					accountsResponseMap.put("IsCFSOD", "");
				else
					accountsResponseMap.put("IsCFSOD", accountResponse.getIsCFSOD());
				
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
				
				if(null == accountResponse.getModeOfOperation())
					accountsResponseMap.put("ModeOfOperation", "");
				else
					accountsResponseMap.put("ModeOfOperation", accountResponse.getModeOfOperation());
				
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
				
				if(null == accountResponse.getAccountClosedDate())
					accountsResponseMap.put("AccountClosedDate", "");
				else
					accountsResponseMap.put("AccountClosedDate", accountResponse.getAccountClosedDate());
				
				if(null == accountResponse.getAccountStatus())
					accountsResponseMap.put("AccountStatus", "");
				else
					accountsResponseMap.put("AccountStatus", accountResponse.getAccountStatus());
				
				if(null == accountResponse.getAccountType())
					accountsResponseMap.put("AccountType", "");
				else
					accountsResponseMap.put("AccountType", accountResponse.getAccountType());
				
				if(null == accountResponse.getCustomerId())
					accountsResponseMap.put("CustomerId", "");
				else
					accountsResponseMap.put("CustomerId", accountResponse.getCustomerId());
				
				if(null == accountResponse.getCustomerName())
					accountsResponseMap.put("CustomerName", "");
				else
					accountsResponseMap.put("CustomerName", accountResponse.getCustomerName());
				
				if(null == accountResponse.getCustomerShortName())
					accountsResponseMap.put("CustomerShortName", "");
				else
					accountsResponseMap.put("CustomerShortName", accountResponse.getCustomerShortName());
				
				if(null == accountResponse.getCustomerTitle())
					accountsResponseMap.put("CustomerTitle", "");
				else
					accountsResponseMap.put("CustomerTitle", accountResponse.getCustomerTitle());
				
				if(null == accountResponse.getIsAccountClosed())
					accountsResponseMap.put("IsAccountClosed", "");
				else
					accountsResponseMap.put("IsAccountClosed", accountResponse.getIsAccountClosed());
				
				if(null == accountResponse.getIsAccountLinked())
					accountsResponseMap.put("IsAccountLinked", "");
				else
					accountsResponseMap.put("IsAccountLinked", accountResponse.getIsAccountLinked());
				
				if(null == accountResponse.getIsCFSOD())
					accountsResponseMap.put("IsCFSOD", "");
				else
					accountsResponseMap.put("IsCFSOD", accountResponse.getIsCFSOD());
				
				if(null == accountResponse.getMessage())
					accountsResponseMap.put("Message", "");
				else
					accountsResponseMap.put("Message", accountResponse.getMessage());
				
				if(null == accountResponse.getModeOfOperation())
					accountsResponseMap.put("ModeOfOperation", "");
				else
					accountsResponseMap.put("ModeOfOperation", accountResponse.getModeOfOperation());
				
				if(null == accountResponse.getStatus())
					accountsResponseMap.put("Status", "");
				else
					accountsResponseMap.put("Status", accountResponse.getStatus());
				
				if(null == accountResponse.getErrorCode())
					accountsResponseMap.put("ErrorCode", "");
				else
					accountsResponseMap.put("ErrorCode", accountResponse.getErrorCode());
				
				if(null == accountResponse.getErrorDesc())
					accountsResponseMap.put("ErrorDesc", "");
				else
					accountsResponseMap.put("ErrorDesc", accountResponse.getErrorDesc());
				
				if(null == accountResponse.getErrorSource())
					accountsResponseMap.put("ErrorSource", "");
				else
					accountsResponseMap.put("ErrorSource", accountResponse.getErrorSource());
				
				if(null == accountResponse.getErrorType())
					accountsResponseMap.put("ErrorType", "");
				else
					accountsResponseMap.put("ErrorType", accountResponse.getErrorType());
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

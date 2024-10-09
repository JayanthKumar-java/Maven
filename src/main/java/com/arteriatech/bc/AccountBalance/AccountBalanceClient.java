package com.arteriatech.bc.AccountBalance;

import java.util.HashMap;
import java.util.Map;

import com.arteriatech.pg.CommonUtils;

public class AccountBalanceClient {
	public Map<String, String> callAccountBalance(String accNumber, String aggregatorID, String corpId, String userId, String userRegId, boolean debug)
	{
		Map<String, String> accnBalanceResponseMap = new HashMap<String, String>();
		AccountBalanceRequest accBalRequest = new AccountBalanceRequest();
		CommonUtils commonUtils = new CommonUtils();
		
		String system="", endPointURL="";
		try {
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			
			if(endPointURL != null && endPointURL.equalsIgnoreCase("E106")){
				accnBalanceResponseMap.put("ErrorCode", "E106");
				accnBalanceResponseMap.put("Message", "Destination not found or required property not maintained in the destination");
				accnBalanceResponseMap.put("Status", "000002");
				accnBalanceResponseMap.put("FreezeStatus", "");
				accnBalanceResponseMap.put("Currency", "");
				accnBalanceResponseMap.put("Amount", "");
				accnBalanceResponseMap.put("AsOn", "");
				accnBalanceResponseMap.put("ExpiryDate", "");
			}else{
				endPointURL = endPointURL+"AccountBalance";
				accBalRequest.setAccountNumber(accNumber);
				accBalRequest.setAggregatorID(aggregatorID);
				accBalRequest.setCorporateID(corpId);
				accBalRequest.setUserID(userId);
				accBalRequest.setUserRegistrationID(userRegId);
				
				AccountBalanceServiceLocator accBalanceLocator = new AccountBalanceServiceLocator();
				accBalanceLocator.setEndpointAddress("AccountBalancePort", endPointURL);
				AccountBalance service = accBalanceLocator.getAccountBalancePort();
				
				AccountBalanceResponse accBalResponse = service.accountBalance(accBalRequest);
				if (null != accBalResponse.getStatus() && accBalResponse.getStatus().trim().length() >0 ){
					if (accBalResponse.getStatus().trim().equalsIgnoreCase("000001")){
						accnBalanceResponseMap.put("Status", accBalResponse.getStatus());
						
						if (null != accBalResponse.getAsOn() && accBalResponse.getAsOn().trim().length()>0) {	
							accnBalanceResponseMap.put("AsOn", accBalResponse.getAsOn());
						} else {
							accnBalanceResponseMap.put("AsOn", "");
						}
						if (null != accBalResponse.getAmount() && accBalResponse.getAmount().trim().length()>0) {
							accnBalanceResponseMap.put("Amount", accBalResponse.getAmount());
						} else {
							accnBalanceResponseMap.put("Amount", "");
						}
						if (null != accBalResponse.getCurrency() && accBalResponse.getCurrency().trim().length()>0) {
							accnBalanceResponseMap.put("Currency", accBalResponse.getCurrency());
						} else {
							accnBalanceResponseMap.put("Currency", "");
						}
						if (null != accBalResponse.getErrorCode() && accBalResponse.getErrorCode().trim().length()>0) {
							accnBalanceResponseMap.put("ErrorCode", accBalResponse.getErrorCode());
						} else {
							accnBalanceResponseMap.put("ErrorCode", "");
						}
						if (null != accBalResponse.getMessage() && accBalResponse.getMessage().trim().length() > 0) {
							accnBalanceResponseMap.put("Message", accBalResponse.getMessage());
						} else {
							accnBalanceResponseMap.put("Message", "");
						}
						if (null != accBalResponse.getFreezeStatus() && accBalResponse.getFreezeStatus().trim().length() > 0) {
							accnBalanceResponseMap.put("FreezeStatus", accBalResponse.getFreezeStatus());
						} else {
							accnBalanceResponseMap.put("FreezeStatus", "");
						}
						
						if(null != accBalResponse.getExpiryDate() && accBalResponse.getExpiryDate().trim().length() > 0){
							accnBalanceResponseMap.put("ExpiryDate", accBalResponse.getExpiryDate());
						}else{
							accnBalanceResponseMap.put("ExpiryDate", "");
						}
					} else {
						if (null != accBalResponse.getStatus() && accBalResponse.getStatus().trim().length()>0) {	
							accnBalanceResponseMap.put("Status", accBalResponse.getStatus());
						} else {
							accnBalanceResponseMap.put("Status", "");
						}
						if (null != accBalResponse.getAsOn() && accBalResponse.getAsOn().trim().length()>0) {	
							accnBalanceResponseMap.put("AsOn", accBalResponse.getAsOn());
						} else {
							accnBalanceResponseMap.put("AsOn", "");
						}
						if (null != accBalResponse.getAmount() && accBalResponse.getAmount().trim().length()>0) {
							accnBalanceResponseMap.put("Amount", accBalResponse.getAmount());
						} else {
							accnBalanceResponseMap.put("Amount", "");
						}
						if (null != accBalResponse.getCurrency() && accBalResponse.getCurrency().trim().length()>0) {
							accnBalanceResponseMap.put("Currency", accBalResponse.getCurrency());
						} else {
							accnBalanceResponseMap.put("Currency", "");
						}
						if (null != accBalResponse.getErrorCode() && accBalResponse.getErrorCode().trim().length()>0) {
							accnBalanceResponseMap.put("ErrorCode", accBalResponse.getErrorCode());
						} else {
							accnBalanceResponseMap.put("ErrorCode", "054");
						}
						if (null != accBalResponse.getMessage() && accBalResponse.getMessage().trim().length() > 0) {
							accnBalanceResponseMap.put("Message", accBalResponse.getMessage());
						} else {
							accnBalanceResponseMap.put("Message", "");
						}
						if (null != accBalResponse.getFreezeStatus() && accBalResponse.getFreezeStatus().trim().length() > 0) {
							accnBalanceResponseMap.put("FreezeStatus", accBalResponse.getFreezeStatus());
						} else {
							accnBalanceResponseMap.put("FreezeStatus", "");
						}
						
						if(null != accBalResponse.getExpiryDate() && accBalResponse.getExpiryDate().trim().length() > 0){
							accnBalanceResponseMap.put("ExpiryDate", accBalResponse.getExpiryDate());
						}else{
							accnBalanceResponseMap.put("ExpiryDate", "");
						}
					}
				}else{	
					accnBalanceResponseMap.put("Status", "000002");
//					accnBalanceResponseMap.put("ErrorCode", "054");
					
					if (null != accBalResponse.getAsOn() && accBalResponse.getAsOn().trim().length()>0) {	
						accnBalanceResponseMap.put("AsOn", accBalResponse.getAsOn());
					} else {
						accnBalanceResponseMap.put("AsOn", "");
					}
					if (null != accBalResponse.getAmount() && accBalResponse.getAmount().trim().length()>0) {
						accnBalanceResponseMap.put("Amount", accBalResponse.getAmount());
					} else {
						accnBalanceResponseMap.put("Amount", "");
					}
					if (null != accBalResponse.getCurrency() && accBalResponse.getCurrency().trim().length()>0) {
						accnBalanceResponseMap.put("Currency", accBalResponse.getCurrency());
					} else {
						accnBalanceResponseMap.put("Currency", "");
					}
					if (null != accBalResponse.getErrorCode() && accBalResponse.getErrorCode().trim().length()>0) {
						accnBalanceResponseMap.put("ErrorCode", accBalResponse.getErrorCode());
					} else {
						accnBalanceResponseMap.put("ErrorCode", "");
					}
					if (null != accBalResponse.getMessage() && accBalResponse.getMessage().trim().length() > 0) {
						accnBalanceResponseMap.put("Message", accBalResponse.getMessage());
					} else {
						accnBalanceResponseMap.put("Message", "Status not received from webservice");
					}
					if (null != accBalResponse.getFreezeStatus() && accBalResponse.getFreezeStatus().trim().length() > 0) {
						accnBalanceResponseMap.put("FreezeStatus", accBalResponse.getFreezeStatus());
					} else {
						accnBalanceResponseMap.put("FreezeStatus", "");
					}
					
					if(null != accBalResponse.getExpiryDate() && accBalResponse.getExpiryDate().trim().length() > 0){
						accnBalanceResponseMap.put("ExpiryDate", accBalResponse.getExpiryDate());
					}else{
						accnBalanceResponseMap.put("ExpiryDate", "");
					}
				}
			}
		} catch (Exception e) {
			accnBalanceResponseMap.put("ErrorCode", "054");
			accnBalanceResponseMap.put("Message", e.getLocalizedMessage());
			accnBalanceResponseMap.put("Status", "000002");
			accnBalanceResponseMap.put("FreezeStatus", "");
			accnBalanceResponseMap.put("Currency", "");
			accnBalanceResponseMap.put("Amount", "");
			accnBalanceResponseMap.put("AsOn", "");
			accnBalanceResponseMap.put("ExpiryDate", "");
		}		
		return accnBalanceResponseMap;		
	}
}

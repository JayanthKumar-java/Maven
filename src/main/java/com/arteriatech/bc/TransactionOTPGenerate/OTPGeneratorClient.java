package com.arteriatech.bc.TransactionOTPGenerate;

import java.util.HashMap;
import java.util.Map;

import com.arteriatech.pg.CommonUtils;

public class OTPGeneratorClient {
	public Map<String, String> callOTPGenerator(String aggregatorID, String userRegId, String userId, String corpId, String trackId, String txnType, String OTPDeliveryMode, 
			String pgAmnt,String currency, String debitAcnNo, String remarks)
	{
		String  endPointURL = "" ,system="";
		
		Map<String, String> otpGeneratorResponseMap = new HashMap<String, String>();
		TransactionOTPGenerate_Request otpRequest = new TransactionOTPGenerate_Request();
		CommonUtils commonUtils = new CommonUtils();
		try {
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"TransactionOTPGenerate";
			
			otpRequest.setAggregatorID(aggregatorID);
			otpRequest.setUserRegistrationID(userRegId);
			otpRequest.setUserID(userId);
			otpRequest.setCorporateID(corpId);
			otpRequest.setTrackID(trackId);
			otpRequest.setTransactionType(txnType);
			otpRequest.setOTPDeliveryMode(OTPDeliveryMode);
			otpRequest.setAmount(pgAmnt);
			otpRequest.setCurrency(currency);
			otpRequest.setDebitAccountNumber(debitAcnNo);
			otpRequest.setRemarks(remarks);
			
			TransactionOTPGenerateServiceLocator otpGeneratorLocator = new TransactionOTPGenerateServiceLocator();
			otpGeneratorLocator.setEndpointAddress("TransactionOTPGeneratePort", endPointURL);
			
			TransactionOTPGenerate service = otpGeneratorLocator.getTransactionOTPGeneratePort();
			
			TransactionOTPGenerate_Response otpResponse = service.transactionOTPGenerate(otpRequest);
			
			if(null != otpResponse.getStatus() && otpResponse.getStatus().trim().length() > 0){
				if(otpResponse.getStatus().trim().equalsIgnoreCase("000001")){

					otpGeneratorResponseMap.put("Status", otpResponse.getStatus());
					
					if(null != otpResponse.getResponse() && otpResponse.getResponse().trim().length() > 0)
						otpGeneratorResponseMap.put("Response", otpResponse.getResponse());
					else
						otpGeneratorResponseMap.put("Response", "");

					if(null != otpResponse.getMessage() && otpResponse.getMessage().trim().length() > 0)
						otpGeneratorResponseMap.put("Message", otpResponse.getMessage());
					else
						otpGeneratorResponseMap.put("Message", "");
				}
				else {
					otpGeneratorResponseMap.put("Status", otpResponse.getStatus());
					
					if(null != otpResponse.getResponse() && otpResponse.getResponse().trim().length() > 0)
						otpGeneratorResponseMap.put("Response", otpResponse.getResponse());
					else
						otpGeneratorResponseMap.put("Response", "");

					if(null != otpResponse.getMessage() && otpResponse.getMessage().trim().length() > 0)
						otpGeneratorResponseMap.put("Message", otpResponse.getMessage());
					else
						otpGeneratorResponseMap.put("Message", "");
				}
			}else
			{
				otpGeneratorResponseMap.put("Status", otpResponse.getStatus());
				
				if(null != otpResponse.getResponse() && otpResponse.getResponse().trim().length() > 0)
					otpGeneratorResponseMap.put("Response", otpResponse.getResponse());
				else
					otpGeneratorResponseMap.put("Response", "");
				
				if(null != otpResponse.getMessage() && otpResponse.getMessage().trim().length() > 0)
					otpGeneratorResponseMap.put("Message", otpResponse.getMessage());
				else
					otpGeneratorResponseMap.put("Message", "");
			}
		} catch (Exception e) {
			otpGeneratorResponseMap.put("Status", "001");
			otpGeneratorResponseMap.put("Response", "");
			otpGeneratorResponseMap.put("Message", e.getLocalizedMessage());
		}
		return otpGeneratorResponseMap;
	}
}

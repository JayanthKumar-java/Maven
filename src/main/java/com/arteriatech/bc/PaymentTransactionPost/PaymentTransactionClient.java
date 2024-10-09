package com.arteriatech.bc.PaymentTransactionPost;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerateServiceLocator;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;

public class PaymentTransactionClient {

	public Map<String, String> callpymntTxnPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String aggregatorID , Map<String, String> pymntTxnPayMap, boolean debug)
	{
		String  endPointURL = "" ,system="";
		Map<String, String> pymntPostResponseMap = new HashMap<String, String>();
		PaymentTransactionPost_Request pymntRequest = new PaymentTransactionPost_Request();
		CommonUtils commonUtils = new CommonUtils();
		try {
			system = commonUtils.getODataDestinationProperties("System","PaymentTransactionPost ");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"PaymentTransactionPost";
			
			if (debug) {
				servletResponse.getWriter().println("callpymntTxnPost: " +aggregatorID);
				for (String key : pymntTxnPayMap.keySet())
					servletResponse.getWriter().println("callpymntTxnPost: " +key+" : "+  pymntTxnPayMap.get(key));
			}
				
			pymntRequest.setAggregatorID(aggregatorID);
			pymntRequest.setOTP((String) pymntTxnPayMap.get("OTP"));

			pymntRequest.setAmount( (String) pymntTxnPayMap.get("Amount"));
			pymntRequest.setCorporateID((String) pymntTxnPayMap.get("CorporateID"));
			pymntRequest.setCreditAccountNumber( (String)pymntTxnPayMap.get("CreditAccountNumber"));
			pymntRequest.setCurrency( (String) pymntTxnPayMap.get("Currency"));
			pymntRequest.setDebitAccountNumber( (String) pymntTxnPayMap.get("DebitAccountNumber"));
			pymntRequest.setIFSCCode( (String) pymntTxnPayMap.get("IFSCCode"));
			pymntRequest.setPayeeName( (String) pymntTxnPayMap.get("PayeeName"));
			pymntRequest.setPGCategoryID( (String) pymntTxnPayMap.get("PGCategoryID"));
			pymntRequest.setPGID( (String) pymntTxnPayMap.get("PGID"));
			pymntRequest.setRemarks( (String) pymntTxnPayMap.get("Remarks"));
			pymntRequest.setTrackID( (String) pymntTxnPayMap.get("TrackId"));
			pymntRequest.setTransactionDate( (String) pymntTxnPayMap.get("TransactionDate"));
			pymntRequest.setTransactionTime( (String) pymntTxnPayMap.get("TransactionTime"));
			pymntRequest.setTransactionType( (String) pymntTxnPayMap.get("TransactionType"));
			pymntRequest.setUserID( (String) pymntTxnPayMap.get("UserID"));
			pymntRequest.setUserRegistrationID( (String) pymntTxnPayMap.get("UserRegId"));
			
			
			PaymentTransactionPostServiceLocator pymntTxnLocator = new PaymentTransactionPostServiceLocator();
			pymntTxnLocator.setEndpointAddress("PaymentTransactionPostPort", endPointURL);
			
			PaymentTransactionPost service = pymntTxnLocator.getPaymentTransactionPostPort();
			
			PaymentTransactionPost_Response pymntPostResponse = service.paymentTransactionPost(pymntRequest);
			
			if(null != pymntPostResponse.getStatus() && pymntPostResponse.getStatus().trim().length() > 0){
				if(pymntPostResponse.getStatus().trim().equalsIgnoreCase("000001")) {
					
					pymntPostResponseMap.put("Status", pymntPostResponse.getStatus());
					
					if(null != pymntPostResponse.getResponse() && pymntPostResponse.getResponse().trim().length() > 0)
						pymntPostResponseMap.put("Response", pymntPostResponse.getResponse());
					else
						pymntPostResponseMap.put("Response", "");
					
					if(null != pymntPostResponse.getMessage() && pymntPostResponse.getMessage().trim().length() > 0)
						pymntPostResponseMap.put("Message", pymntPostResponse.getMessage());
					else
						pymntPostResponseMap.put("Message", "");
					
					if(null != pymntPostResponse.getPGTransactionID() && pymntPostResponse.getPGTransactionID().trim().length() > 0)
						pymntPostResponseMap.put("PGTransactionID", pymntPostResponse.getPGTransactionID());
					else
						pymntPostResponseMap.put("PGTransactionID", "");
					
					if(null != pymntPostResponse.getPGBankRefID() && pymntPostResponse.getPGBankRefID().trim().length() > 0)
						pymntPostResponseMap.put("PGBankRefID", pymntPostResponse.getPGBankRefID());
					else
						pymntPostResponseMap.put("PGBankRefID", "");
					
					if(null != pymntPostResponse.getPGTxnErrorCode() && pymntPostResponse.getPGTxnErrorCode().trim().length() > 0)
						pymntPostResponseMap.put("PGTxnErrorCode", pymntPostResponse.getPGTxnErrorCode());
					else
						pymntPostResponseMap.put("PGTxnErrorCode", "");
				
				}else {
					pymntPostResponseMap.put("Status", pymntPostResponse.getStatus());
					
					if(null != pymntPostResponse.getResponse() && pymntPostResponse.getResponse().trim().length() > 0)
						pymntPostResponseMap.put("Response", pymntPostResponse.getResponse());
					else
						pymntPostResponseMap.put("Response", "");

					if(null != pymntPostResponse.getMessage() && pymntPostResponse.getMessage().trim().length() > 0)
						pymntPostResponseMap.put("Message", pymntPostResponse.getMessage());
					else
						pymntPostResponseMap.put("Message", "");
					
					if(null != pymntPostResponse.getPGTransactionID() && pymntPostResponse.getPGTransactionID().trim().length() > 0)
						pymntPostResponseMap.put("PGTransactionID", pymntPostResponse.getPGTransactionID());
					else
						pymntPostResponseMap.put("PGTransactionID", "");
					
					if(null != pymntPostResponse.getPGBankRefID() && pymntPostResponse.getPGBankRefID().trim().length() > 0)
						pymntPostResponseMap.put("PGBankRefID", pymntPostResponse.getPGBankRefID());
					else
						pymntPostResponseMap.put("PGBankRefID", "");
					
					if(null != pymntPostResponse.getPGTxnErrorCode() && pymntPostResponse.getPGTxnErrorCode().trim().length() > 0)
						pymntPostResponseMap.put("PGTxnErrorCode", pymntPostResponse.getPGTxnErrorCode());
					else
						pymntPostResponseMap.put("PGTxnErrorCode", "");
				}
			}else
			{
				pymntPostResponseMap.put("Status", pymntPostResponse.getStatus());
				
				if(null != pymntPostResponse.getResponse() && pymntPostResponse.getResponse().trim().length() > 0)
					pymntPostResponseMap.put("Response", pymntPostResponse.getResponse());
				else
					pymntPostResponseMap.put("Response", "");

				if(null != pymntPostResponse.getMessage() && pymntPostResponse.getMessage().trim().length() > 0)
					pymntPostResponseMap.put("Message", pymntPostResponse.getMessage());
				else
					pymntPostResponseMap.put("Message", "");
				
				if(null != pymntPostResponse.getPGTransactionID() && pymntPostResponse.getPGTransactionID().trim().length() > 0)
					pymntPostResponseMap.put("PGTransactionID", pymntPostResponse.getPGTransactionID());
				else
					pymntPostResponseMap.put("PGTransactionID", "");
				
				if(null != pymntPostResponse.getPGBankRefID() && pymntPostResponse.getPGBankRefID().trim().length() > 0)
					pymntPostResponseMap.put("PGBankRefID", pymntPostResponse.getPGBankRefID());
				else
					pymntPostResponseMap.put("PGBankRefID", "");
				
				if(null != pymntPostResponse.getPGTxnErrorCode() && pymntPostResponse.getPGTxnErrorCode().trim().length() > 0)
					pymntPostResponseMap.put("PGTxnErrorCode", pymntPostResponse.getPGTxnErrorCode());
				else
					pymntPostResponseMap.put("PGTxnErrorCode", "");
			}
		}
		catch (Exception e) {
			pymntPostResponseMap.put("Status", "054");
			pymntPostResponseMap.put("Response", "");
			pymntPostResponseMap.put("Message", "Please Contact Bank With Screenshot Error");
			pymntPostResponseMap.put("PGTransactionID", "");
			pymntPostResponseMap.put("PGBankRefID", "");
			pymntPostResponseMap.put("PGTxnErrorCode", "");
		}
		return pymntPostResponseMap;	
	}
}

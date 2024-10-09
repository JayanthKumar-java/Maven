package com.arteriatech.bc.TransactionEnquiry;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;

public class TransactionEnquiryClient {

	public Map<String, String> callTransactionEnquiryWebService(HttpServletRequest request,
			HttpServletResponse response, String corporateID, String userID, String userRegistrationID, String uniqueID,
			String aggregatorID, boolean debug) {

		String endPointURL = "", system = "",userName="",password="";
		CommonUtils commonUtils = new CommonUtils();
		Map<String, String> deRegMap = new HashMap<String, String>();
		TransactionEnquiryRequest txnEnqReq = new TransactionEnquiryRequest();
		try {
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			userName = commonUtils.getODataDestinationProperties("User","BankConnect");
    		password = commonUtils.getODataDestinationProperties("Password","BankConnect");
			endPointURL = endPointURL + "TransactionEnquiry";
			if(debug){
				response.getWriter().println("endpoint url "+endPointURL);	
				response.getWriter().println("password "+password);
				response.getWriter().println("username "+userName);
			}
			txnEnqReq.setAggregatorID(aggregatorID);
			txnEnqReq.setCorporateID(corporateID);
			txnEnqReq.setUniqueID(uniqueID);
			txnEnqReq.setUserID(userID);
			txnEnqReq.setUserRegistrationID(userRegistrationID);
			if (debug) {
				response.getWriter().println("TransactionEnquiryWebservice-endPointURL: " + endPointURL);
				response.getWriter().println("TransactionEnquiryWebservice-AggregatorId: " + aggregatorID);
				response.getWriter().println("TransactionEnquiryWebservice-userRegistrationID: " + userRegistrationID);
				response.getWriter().println("TransactionEnquiryWebservice-CorpId: " + corporateID);
				response.getWriter().println("TransactionEnquiryWebservice-uniqueID: " + uniqueID);
				response.getWriter().println("TransactionEnquiryWebservice-UserId: " + userID);

			}

			TransactionEnquiryServiceLocator tranEnqLoc = new TransactionEnquiryServiceLocator();
			tranEnqLoc.setEndpointAddress("TransactionEnquiryPort", endPointURL);

			TransactionEnquiry transactionEnquiryPort = tranEnqLoc.getTransactionEnquiryPort();

			TransactionEnquiryResponse txnEnqRes = transactionEnquiryPort.transactionEnquiry(txnEnqReq);
			

			if (debug) {
				response.getWriter().println("Response from CPI :" + new Gson().toJson(txnEnqRes));
			}
			if (null == txnEnqRes || txnEnqRes.getStatus().trim().length() == 0
					|| txnEnqRes.getStatus().equalsIgnoreCase("000002")
					|| txnEnqRes.getStatus().trim().equalsIgnoreCase("")) {
				deRegMap.put("Error", "059");
				if (txnEnqRes.getStatus() == null
						|| txnEnqRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "000002");
				} else {
					deRegMap.put("Status", txnEnqRes.getStatus());
				}

				if (txnEnqRes.getMessage() == null
						|| txnEnqRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", txnEnqRes.getMessage());

				}

				if (txnEnqRes.getPGTxnErrorCode() == null
						|| txnEnqRes.getPGTxnErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("PGTxnErrorCode", "");
				} else {
					deRegMap.put("PGTxnErrorCode", txnEnqRes.getPGTxnErrorCode());

				}

				if (txnEnqRes.getUTRNumber() == null
						|| txnEnqRes.getUTRNumber().equalsIgnoreCase("")) {
					deRegMap.put("UTRNumber", "");
				} else {
					deRegMap.put("UTRNumber", txnEnqRes.getUTRNumber());

				}
			} else if (null != txnEnqRes.getStatus()
					|| txnEnqRes.getStatus().equalsIgnoreCase("000001")) {
				deRegMap.put("Error", "");
				if (txnEnqRes.getStatus() == null
						|| txnEnqRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "");
				} else {
					deRegMap.put("Status", txnEnqRes.getStatus());
				}

				if (txnEnqRes.getMessage() == null
						|| txnEnqRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", txnEnqRes.getMessage());

				}

				if (txnEnqRes.getPGTxnErrorCode() == null
						|| txnEnqRes.getPGTxnErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("PGTxnErrorCode", "");
				} else {
					deRegMap.put("PGTxnErrorCode", txnEnqRes.getPGTxnErrorCode());

				}

				if (txnEnqRes.getUTRNumber() == null
						|| txnEnqRes.getUTRNumber().equalsIgnoreCase("")) {
					deRegMap.put("UTRNumber", "");
				} else {
					deRegMap.put("UTRNumber", txnEnqRes.getUTRNumber());

				}
			} else {
				deRegMap.put("Error", "");
				if (txnEnqRes.getStatus() == null
						|| txnEnqRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "");
				} else {
					deRegMap.put("Status", txnEnqRes.getStatus());
				}

				if (txnEnqRes.getMessage() == null
						|| txnEnqRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", txnEnqRes.getMessage());

				}

				if (txnEnqRes.getPGTxnErrorCode() == null
						|| txnEnqRes.getPGTxnErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("PGTxnErrorCode", "");
				} else {
					deRegMap.put("PGTxnErrorCode", txnEnqRes.getPGTxnErrorCode());

				}

				if (txnEnqRes.getUTRNumber() == null
						|| txnEnqRes.getUTRNumber().equalsIgnoreCase("")) {
					deRegMap.put("UTRNumber", "");
				} else {
					deRegMap.put("UTRNumber", txnEnqRes.getUTRNumber());

				}

			}
		} catch (Exception ex) {
			deRegMap.put("Error", "059");
			deRegMap.put("Status", "000002");
			deRegMap.put("errorCode", "");
			deRegMap.put("UTRNumber", "");
			deRegMap.put("PGTxnErrorCode", "");
			deRegMap.put("Message", "Unable to serve your request. Error From Webservice");
			StringBuffer buffer = new StringBuffer();
			StackTraceElement element[] = ex.getStackTrace();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			deRegMap.put("ExceptionTrace", buffer.toString());
			
		}
		return deRegMap;
	}

}

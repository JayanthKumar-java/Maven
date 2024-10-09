package com.arteriatech.bc.CurrentAccountInquiry;

import java.util.HashMap;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;

public class CAInquiryClient {
	
	public Map<String,String> callCurrentAccountsWebservice(String accountNo,HttpServletRequest request,HttpServletResponse response,boolean debug){
	 String  endPointURL="", system="";
	 CommonUtils commonUtils = new CommonUtils();
	 Map<String,String> caResMap = new HashMap<String,String>();
	 CurrentAccountInquiryRequest caInqObj=new CurrentAccountInquiryRequest();
		try {
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			endPointURL = endPointURL + "AccountInquiry";
			caInqObj.setAccount_No(accountNo);
			if(debug){
				response.getWriter().println("callCurrentAccountsWebservice-endPointURL: "+endPointURL);
				response.getWriter().println("callCurrentAccountsWebservice-AccntNo: "+accountNo);
				
			}
			CurrentAccountInquiryServiceLocator caInqLoc=new CurrentAccountInquiryServiceLocator();
			caInqLoc.setEndpointAddress("currentAccountInquiryPort", endPointURL);
			CurrentAccountInquiry currentAccountInquiry = caInqLoc.getcurrentAccountInquiryPort();
			CurrentAccountInquiryResponsePortal caInqResponse = currentAccountInquiry.currentAccountInquiry(caInqObj);
			if(debug){
				response.getWriter().println("Response from CPI :"+new Gson().toJson(caInqResponse));		
			}
			if(null == caInqResponse.getStatus() || caInqResponse.getStatus().trim().length() == 0 
					|| caInqResponse.getStatus().equalsIgnoreCase("000002") || caInqResponse.getStatus().trim().equalsIgnoreCase("")){
				caResMap.put("Error", "059");
				if(caInqResponse.getStatus()==null || caInqResponse.getStatus().equalsIgnoreCase("")){
					caResMap.put("Status", "000002");
				}else{
					caResMap.put("Status",caInqResponse.getStatus());
				}
				if(caInqResponse.getAccountClosedDate()==null ||caInqResponse.getAccountClosedDate().equalsIgnoreCase("") ){
					caResMap.put("AccountClosedDate", "");
				}else{
					caResMap.put("AccountClosedDate", caInqResponse.getAccountClosedDate());
				}
				if (caInqResponse.getCustomerId() == null || caInqResponse.getCustomerId().equalsIgnoreCase("")) {
					caResMap.put("CustomerId", "");
				} else {
					caResMap.put("CustomerId", caInqResponse.getCustomerId());

				}
				if(caInqResponse.getCustomerName()==null || caInqResponse.getCustomerName().equalsIgnoreCase("")){
					caResMap.put("CustomerName","");
				}else{
					caResMap.put("CustomerName", caInqResponse.getCustomerName());
				}
				if(caInqResponse.getCustomerShortName()==null ||caInqResponse.getCustomerShortName().equalsIgnoreCase("") ){
					caResMap.put("CustomerShortName","");
				}else{
					caResMap.put("CustomerShortName", caInqResponse.getCustomerShortName());
				}
				if(caInqResponse.getCustomerTitle()==null || caInqResponse.getCustomerTitle().equalsIgnoreCase("")){
					caResMap.put("CustomerTitle","");
				}else{
					caResMap.put("CustomerTitle", caInqResponse.getCustomerTitle());
				}
				
				if (caInqResponse.getEffectiveBalance() == null || caInqResponse.getEffectiveBalance().equalsIgnoreCase("")) {
					caResMap.put("EffectiveBalance", "");
				} else {
					caResMap.put("EffectiveBalance", caInqResponse.getEffectiveBalance());
				}
				if (caInqResponse.getErrorCode() == null || caInqResponse.getErrorCode().equalsIgnoreCase("")) {
					caResMap.put("ErrorCode", "");
				} else {
					caResMap.put("ErrorCode", caInqResponse.getErrorCode());
				}
				if (caInqResponse.getErrorDesc() == null || caInqResponse.getErrorDesc().equalsIgnoreCase("")) {
					caResMap.put("ErrorDesc", "");
				} else {
					caResMap.put("ErrorDesc", caInqResponse.getErrorDesc());
				}
				if (caInqResponse.getErrorSource() == null || caInqResponse.getErrorSource().equalsIgnoreCase("")) {
					caResMap.put("ErrorSource", "");
				} else {
					caResMap.put("ErrorSource", caInqResponse.getErrorSource());
				}
				if (caInqResponse.getErrorType() == null || caInqResponse.getErrorType().equalsIgnoreCase("")) {
					caResMap.put("ErrorType", "");
				} else {
					caResMap.put("ErrorType", caInqResponse.getErrorType());
				}
				if (caInqResponse.getExpiryDate() == null || caInqResponse.getExpiryDate().equalsIgnoreCase("")) {
					caResMap.put("ExpiryDate", "");
				} else {
					caResMap.put("ExpiryDate", caInqResponse.getExpiryDate());
				}
				if (caInqResponse.getFreezeStatus() == null ||caInqResponse.getFreezeStatus().equalsIgnoreCase("") ) {
					caResMap.put("FreezeStatus", "");
				} else {
					caResMap.put("FreezeStatus", caInqResponse.getFreezeStatus());
				}
				if (caInqResponse.getIsAccountClosed() == null || caInqResponse.getIsAccountClosed().equalsIgnoreCase("")) {
					caResMap.put("IsAccountClosed", "");
				} else {
					caResMap.put("IsAccountClosed", caInqResponse.getIsAccountClosed());
				}
				if (caInqResponse.getLimitPrefix()== null || caInqResponse.getLimitPrefix().equalsIgnoreCase("")) {
					caResMap.put("LimitPrefix", "");
				} else {
					caResMap.put("LimitPrefix", caInqResponse.getLimitPrefix());
				}
				if (caInqResponse.getLimitSuffix()== null || caInqResponse.getLimitSuffix().equalsIgnoreCase("")) {
					caResMap.put("LimitSuffix", "");
				} else {
					caResMap.put("LimitSuffix", caInqResponse.getLimitSuffix());
				}
				if (caInqResponse.getModeOfOperation()== null || caInqResponse.getModeOfOperation().equalsIgnoreCase("")) {
					caResMap.put("ModeOfOperation", "");
				} else {
					caResMap.put("ModeOfOperation", caInqResponse.getModeOfOperation());
				}
				if (caInqResponse.getSoldID()== null ||caInqResponse.getSoldID().equalsIgnoreCase("")) {
					caResMap.put("SoldID", "");
				} else {
					caResMap.put("SoldID", caInqResponse.getSoldID());
				}
				caResMap.put("Message", "Unable to serve your request. Error From Webservice");
				
			}else if(null != caInqResponse.getStatus() || caInqResponse.getStatus().equalsIgnoreCase("000001")){
				caResMap.put("Error", "");
				if(caInqResponse.getStatus()==null || caInqResponse.getStatus().equalsIgnoreCase("")){
					caResMap.put("Status", "");
				}else{
					caResMap.put("Status",caInqResponse.getStatus());
				}
				if(caInqResponse.getAccountClosedDate()==null || caInqResponse.getAccountClosedDate().equalsIgnoreCase("") ){
					caResMap.put("AccountClosedDate", "");
				}else{
					caResMap.put("AccountClosedDate", caInqResponse.getAccountClosedDate());
				}
				if (caInqResponse.getCustomerId() == null || caInqResponse.getCustomerId().equalsIgnoreCase("")) {
					caResMap.put("CustomerId", "");
				} else {
					caResMap.put("CustomerId", caInqResponse.getCustomerId());

				}
				if(caInqResponse.getCustomerName()==null ||caInqResponse.getCustomerName().equalsIgnoreCase("") ){
					caResMap.put("CustomerName","");
				}else{
					caResMap.put("CustomerName", caInqResponse.getCustomerName());
				}
				if(caInqResponse.getCustomerShortName()==null ||caInqResponse.getCustomerShortName().equalsIgnoreCase("") ){
					caResMap.put("CustomerShortName","");
				}else{
					caResMap.put("CustomerShortName", caInqResponse.getCustomerShortName());
				}
				if(caInqResponse.getCustomerTitle()==null || caInqResponse.getCustomerTitle().equalsIgnoreCase("") ){
					caResMap.put("CustomerTitle","");
				}else{
					caResMap.put("CustomerTitle", caInqResponse.getCustomerTitle());
				}
				
				if (caInqResponse.getEffectiveBalance() == null || caInqResponse.getEffectiveBalance().equalsIgnoreCase("") ) {
					caResMap.put("EffectiveBalance", "");
				} else {
					caResMap.put("EffectiveBalance", caInqResponse.getEffectiveBalance());
				}
				if (caInqResponse.getErrorCode() == null || caInqResponse.getErrorCode().equalsIgnoreCase("")) {
					caResMap.put("ErrorCode", "");
				} else {
					caResMap.put("ErrorCode", caInqResponse.getErrorCode());
				}
				if (caInqResponse.getErrorDesc() == null || caInqResponse.getErrorDesc().equalsIgnoreCase("") ) {
					caResMap.put("ErrorDesc", "");
				} else {
					caResMap.put("ErrorDesc", caInqResponse.getErrorDesc());
				}
				if (caInqResponse.getErrorSource() == null || caInqResponse.getErrorSource().equalsIgnoreCase("") ) {
					caResMap.put("ErrorSource", "");
				} else {
					caResMap.put("ErrorSource", caInqResponse.getErrorSource());
				}
				if (caInqResponse.getErrorType() == null ||caInqResponse.getErrorType().equalsIgnoreCase("") ) {
					caResMap.put("ErrorType", "");
				} else {
					caResMap.put("ErrorType", caInqResponse.getErrorType());
				}
				if (caInqResponse.getExpiryDate() == null || caInqResponse.getExpiryDate().equalsIgnoreCase("")) {
					caResMap.put("ExpiryDate", "");
				} else {
					caResMap.put("ExpiryDate", caInqResponse.getExpiryDate());
				}
				if (caInqResponse.getFreezeStatus() == null ||caInqResponse.getFreezeStatus().equalsIgnoreCase("") ) {
					caResMap.put("FreezeStatus", "");
				} else {
					caResMap.put("FreezeStatus", caInqResponse.getFreezeStatus());
				}
				if (caInqResponse.getIsAccountClosed() == null || caInqResponse.getIsAccountClosed().equalsIgnoreCase("")) {
					caResMap.put("IsAccountClosed", "");
				} else {
					caResMap.put("IsAccountClosed", caInqResponse.getIsAccountClosed());
				}
				if (caInqResponse.getLimitPrefix()== null || caInqResponse.getLimitPrefix().equalsIgnoreCase("")) {
					caResMap.put("LimitPrefix", "");
				} else {
					caResMap.put("LimitPrefix", caInqResponse.getLimitPrefix());
				}
				if (caInqResponse.getLimitSuffix()== null || caInqResponse.getLimitSuffix().equalsIgnoreCase("") ) {
					caResMap.put("LimitSuffix", "");
				} else {
					caResMap.put("LimitSuffix", caInqResponse.getLimitSuffix());
				}
				if (caInqResponse.getModeOfOperation()== null || caInqResponse.getModeOfOperation().equalsIgnoreCase("")) {
					caResMap.put("ModeOfOperation", "");
				} else {
					caResMap.put("ModeOfOperation", caInqResponse.getModeOfOperation());
				}
				if (caInqResponse.getSoldID()== null || caInqResponse.getSoldID().equalsIgnoreCase("")) {
					caResMap.put("SoldID", "");
				} else {
					caResMap.put("SoldID", caInqResponse.getSoldID());
				}
			} else {
				caResMap.put("Error", "");
				if (caInqResponse.getStatus() == null || caInqResponse.getStatus().equalsIgnoreCase("")) {
					caResMap.put("Status", "");
				} else {
					caResMap.put("Status", caInqResponse.getStatus());
				}
				if (caInqResponse.getAccountClosedDate() == null
						|| caInqResponse.getAccountClosedDate().equalsIgnoreCase("")) {
					caResMap.put("AccountClosedDate", "");
				} else {
					caResMap.put("AccountClosedDate", caInqResponse.getAccountClosedDate());
				}
				if (caInqResponse.getCustomerId() == null || caInqResponse.getCustomerId().equalsIgnoreCase("")) {
					caResMap.put("CustomerId", "");
				} else {
					caResMap.put("CustomerId", caInqResponse.getCustomerId());

				}
				if (caInqResponse.getCustomerName() == null || caInqResponse.getCustomerName().equalsIgnoreCase("")) {
					caResMap.put("CustomerName", "");
				} else {
					caResMap.put("CustomerName", caInqResponse.getCustomerName());
				}
				if (caInqResponse.getCustomerShortName() == null
						|| caInqResponse.getCustomerShortName().equalsIgnoreCase("")) {
					caResMap.put("CustomerShortName", "");
				} else {
					caResMap.put("CustomerShortName", caInqResponse.getCustomerShortName());
				}
				if (caInqResponse.getCustomerTitle() == null || caInqResponse.getCustomerTitle().equalsIgnoreCase("")) {
					caResMap.put("CustomerTitle", "");
				} else {
					caResMap.put("CustomerTitle", caInqResponse.getCustomerTitle());
				}

				if (caInqResponse.getEffectiveBalance() == null
						|| caInqResponse.getEffectiveBalance().equalsIgnoreCase("")) {
					caResMap.put("EffectiveBalance", "");
				} else {
					caResMap.put("EffectiveBalance", caInqResponse.getEffectiveBalance());
				}
				if (caInqResponse.getErrorCode() == null || caInqResponse.getErrorCode().equalsIgnoreCase("")) {
					caResMap.put("ErrorCode", "");
				} else {
					caResMap.put("ErrorCode", caInqResponse.getErrorCode());
				}
				if (caInqResponse.getErrorDesc() == null || caInqResponse.getErrorDesc().equalsIgnoreCase("")) {
					caResMap.put("ErrorDesc", "");
				} else {
					caResMap.put("ErrorDesc", caInqResponse.getErrorDesc());
				}
				if (caInqResponse.getErrorSource() == null || caInqResponse.getErrorSource().equalsIgnoreCase("")) {
					caResMap.put("ErrorSource", "");
				} else {
					caResMap.put("ErrorSource", caInqResponse.getErrorSource());
				}
				if (caInqResponse.getErrorType() == null || caInqResponse.getErrorType().equalsIgnoreCase("")) {
					caResMap.put("ErrorType", "");
				} else {
					caResMap.put("ErrorType", caInqResponse.getErrorType());
				}
				if (caInqResponse.getExpiryDate() == null || caInqResponse.getExpiryDate().equalsIgnoreCase("")) {
					caResMap.put("ExpiryDate", "");
				} else {
					caResMap.put("ExpiryDate", caInqResponse.getExpiryDate());
				}
				if (caInqResponse.getFreezeStatus() == null || caInqResponse.getFreezeStatus().equalsIgnoreCase("")) {
					caResMap.put("FreezeStatus", "");
				} else {
					caResMap.put("FreezeStatus", caInqResponse.getFreezeStatus());
				}
				if (caInqResponse.getIsAccountClosed() == null
						|| caInqResponse.getIsAccountClosed().equalsIgnoreCase("")) {
					caResMap.put("IsAccountClosed", "");
				} else {
					caResMap.put("IsAccountClosed", caInqResponse.getIsAccountClosed());
				}
				if (caInqResponse.getLimitPrefix() == null || caInqResponse.getLimitPrefix().equalsIgnoreCase("")) {
					caResMap.put("LimitPrefix", "");
				} else {
					caResMap.put("LimitPrefix", caInqResponse.getLimitPrefix());
				}
				if (caInqResponse.getLimitSuffix() == null || caInqResponse.getLimitSuffix().equalsIgnoreCase("")) {
					caResMap.put("LimitSuffix", "");
				} else {
					caResMap.put("LimitSuffix", caInqResponse.getLimitSuffix());
				}
				if (caInqResponse.getModeOfOperation() == null
						|| caInqResponse.getModeOfOperation().equalsIgnoreCase("")) {
					caResMap.put("ModeOfOperation", "");
				} else {
					caResMap.put("ModeOfOperation", caInqResponse.getModeOfOperation());
				}
				if (caInqResponse.getSoldID() == null || caInqResponse.getSoldID().equalsIgnoreCase("")) {
					caResMap.put("SoldID", "");
				} else {
					caResMap.put("SoldID", caInqResponse.getSoldID());
				}
			}
		} catch (Exception ex) {
			caResMap.put("Error", "059");
			caResMap.put("Status", "000002");
			caResMap.put("AccountClosedDate", "");
			caResMap.put("CustomerId", "");
			caResMap.put("CustomerName", "");
			caResMap.put("CustomerShortName", "");
			caResMap.put("CustomerTitle", "");
			caResMap.put("EffectiveBalance", "");
			caResMap.put("ErrorCode", "");
			caResMap.put("ErrorDesc", "");
			caResMap.put("ErrorSource", "");
			caResMap.put("ErrorType", "");
			caResMap.put("ExpiryDate", "");
			caResMap.put("FreezeStatus", "");
			caResMap.put("IsAccountClosed", "");
			caResMap.put("LimitPrefix", "");
			caResMap.put("ModeOfOperation", "");
			caResMap.put("SoldID", "");
			caResMap.put("Message", "Unable to serve your request. Error From Webservice");
			StringBuffer buffer = new StringBuffer();
			StackTraceElement element[] = ex.getStackTrace();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			caResMap.put("ExceptionTrace", buffer.toString());
		}
		return caResMap;
	}
}

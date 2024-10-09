package com.arteriatech.bc.eSignContractCALProprietorship;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ProprietorESignClient {
	public JsonObject proprietorESignContract(HttpServletRequest servletRequest, HttpServletResponse servletResponse, 
			Map<String, String> wsPayloadData, String aggregatorID, boolean debug) throws IOException{
		JsonObject returnJsonObject = new JsonObject();
		ESignContractCALProprietorship_Request propRequest = new ESignContractCALProprietorship_Request();
		CommonUtils commonUtils = new CommonUtils();
		String system="", endPointURL="";
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"eSignContractCALProprietorship";
			
			//Set the headers
			propRequest.setAadharNumber("");
			propRequest.setAddlnPeriodInterestRateSpread(wsPayloadData.get("AddlnPeriodInterestRateSpread"));
			propRequest.setAddlnTenorOfPayment(wsPayloadData.get("AddlnTenorOfPayment"));
			propRequest.setConstitution(wsPayloadData.get("Constitution"));
			propRequest.setCorporateName(wsPayloadData.get("CorporateName"));
			propRequest.setCPType(wsPayloadData.get("CPType"));
			propRequest.setCurrentDate(wsPayloadData.get("CurrentDate"));
			propRequest.setCurrentTime(wsPayloadData.get("CurrentTime"));
			propRequest.setDateOfBirth(wsPayloadData.get("DateOfBirth"));
			propRequest.setDateOfIncorporation(wsPayloadData.get("DateOfIncorporation"));
			propRequest.setDealerName(wsPayloadData.get("DealerName"));
			propRequest.setDefaultInterestSpread(wsPayloadData.get("DefaultInterestSpread"));
			propRequest.setEmailAddress(wsPayloadData.get("EmailAddress"));
			propRequest.setExpiryDate(wsPayloadData.get("ExpiryDate"));
			propRequest.setFacilityAmount(wsPayloadData.get("FacilityAmount"));
			propRequest.setInterestRateSpread(wsPayloadData.get("InterestRateSpread"));
			propRequest.setIPAddress(wsPayloadData.get("IPAddress"));
			propRequest.setLeadId(wsPayloadData.get("LeadId"));
			propRequest.setMCLR6MRate(wsPayloadData.get("MCLR6MRate"));
			propRequest.setMobileNumber(wsPayloadData.get("MobileNumber"));
			propRequest.setPAN(wsPayloadData.get("PAN"));
			propRequest.setProcessingFee(wsPayloadData.get("ProcessingFee"));
			propRequest.setProprietorAccount(wsPayloadData.get("ProprietorAccount"));
			propRequest.setProprietorName(wsPayloadData.get("ProprietorName"));
			propRequest.setTenorOfPayment(wsPayloadData.get("TenorOfPayment"));
			propRequest.setTestRun(wsPayloadData.get("TestRun"));
			
			ESignContractCALProprietorship_RequestInitiateRequest initiateRequest1 = new ESignContractCALProprietorship_RequestInitiateRequest();
			ESignContractCALProprietorship_RequestInitiateRequestRequest initiateReqRequest1 = new ESignContractCALProprietorship_RequestInitiateRequestRequest();
//			
			ESignContractCALProprietorship_RequestInitiateRequestRequestSIgnerDetail[] signerDetailArray = new ESignContractCALProprietorship_RequestInitiateRequestRequestSIgnerDetail[1];
			ESignContractCALProprietorship_RequestInitiateRequestRequestSIgnerDetail signerDetail = new ESignContractCALProprietorship_RequestInitiateRequestRequestSIgnerDetail();
			
			initiateReqRequest1.setAggregatorId(aggregatorID);
			initiateReqRequest1.setSIgnerDetail(signerDetailArray);
			
			initiateReqRequest1.setSIgnerDetail(signerDetailArray);
			signerDetail.setAadharNumber(""); 
			signerDetail.setSignerEmailId(wsPayloadData.get("SignerEmailId"));
			signerDetail.setSignerName(wsPayloadData.get("SignerName"));
			signerDetail.setSignOrder(wsPayloadData.get("SignOrder"));
			initiateReqRequest1.setSIgnerDetail(0, signerDetail);
			
			initiateRequest1.setRequest(initiateReqRequest1);
			propRequest.setInitiateRequest(initiateRequest1);
			
			ESignContractCALProprietorshipServiceLocator propServiceLocator = new ESignContractCALProprietorshipServiceLocator();
			propServiceLocator.setEndpointAddress("eSignContractCALProprietorshipPort", endPointURL);
			ESignContractCALProprietorship eSignCalPropService = propServiceLocator.geteSignContractCALProprietorshipPort();
			
			ESignContractCALProprietorship_Response propResponse = new ESignContractCALProprietorship_Response();
			propResponse = eSignCalPropService.eSignContractCALProprietorship(propRequest);
			
			if(null != propResponse.getStatus() && propResponse.getStatus().trim().length() > 0){
				if(propResponse.getStatus().equalsIgnoreCase("000001")){
					returnJsonObject.addProperty("Status", propResponse.getStatus());
					if(null == propResponse.getMessage()){
						returnJsonObject.addProperty("Message", "");
					}else{
						returnJsonObject.addProperty("Message", propResponse.getMessage());
					}
					
					returnJsonObject.addProperty("ContractId", propResponse.getResponse().getContractId());
					returnJsonObject.addProperty("CustomerId", propResponse.getResponse().getCustomerId());
					returnJsonObject.addProperty("ResponseCode", propResponse.getResponseCode());
					returnJsonObject.addProperty("SignerName", propResponse.getResponse().getSignerDetail(0).getSignerName());
					returnJsonObject.addProperty("SignOrder", propResponse.getResponse().getSignerDetail(0).getSignOrder());
				}else{
					returnJsonObject.addProperty("Status", propResponse.getStatus());
					
					if(null == propResponse.getMessage())
						returnJsonObject.addProperty("Message", "");
					else
						returnJsonObject.addProperty("Message", propResponse.getMessage());
					
					if(null == propResponse.getResponse().getContractId())
						returnJsonObject.addProperty("ContractId", "");
					else
						returnJsonObject.addProperty("ContractId", propResponse.getResponse().getContractId());
					
					if(null == propResponse.getResponse().getCustomerId())
						returnJsonObject.addProperty("CustomerId", "");
					else
						returnJsonObject.addProperty("CustomerId", propResponse.getResponse().getCustomerId());
					
					if(null == propResponse.getResponseCode())
						returnJsonObject.addProperty("ResponseCode", "");
					else
						returnJsonObject.addProperty("ResponseCode", propResponse.getResponseCode());
					
					if(null == propResponse.getResponse().getSignerDetail(0).getSignerName())
						returnJsonObject.addProperty("SignerName", "");
					else
						returnJsonObject.addProperty("SignerName", propResponse.getResponse().getSignerDetail(0).getSignerName());
					
					if(null == propResponse.getResponse().getSignerDetail(0).getSignOrder())
						returnJsonObject.addProperty("SignOrder", "");
					else
						returnJsonObject.addProperty("SignOrder", propResponse.getResponse().getSignerDetail(0).getSignOrder());
				}
			}else{
				returnJsonObject.addProperty("Status", "000002");
				if(null == propResponse.getMessage())
					returnJsonObject.addProperty("Message", "");
				else
					returnJsonObject.addProperty("Message", propResponse.getMessage());
				
				if(null == propResponse.getResponse().getContractId())
					returnJsonObject.addProperty("ContractId", "");
				else
					returnJsonObject.addProperty("ContractId", propResponse.getResponse().getContractId());
				
				if(null == propResponse.getResponse().getCustomerId())
					returnJsonObject.addProperty("CustomerId", "");
				else
					returnJsonObject.addProperty("CustomerId", propResponse.getResponse().getCustomerId());
				
				if(null == propResponse.getResponseCode())
					returnJsonObject.addProperty("ResponseCode", "");
				else
					returnJsonObject.addProperty("ResponseCode", propResponse.getResponseCode());
				
				if(null == propResponse.getResponse().getSignerDetail(0).getSignerName())
					returnJsonObject.addProperty("SignerName", "");
				else
					returnJsonObject.addProperty("SignerName", propResponse.getResponse().getSignerDetail(0).getSignerName());
				
				if(null == propResponse.getResponse().getSignerDetail(0).getSignOrder())
					returnJsonObject.addProperty("SignOrder", "");
				else
					returnJsonObject.addProperty("SignOrder", propResponse.getResponse().getSignerDetail(0).getSignOrder());
			}
			

		}catch (Exception e) {
			returnJsonObject.addProperty("Status", "000002");
			returnJsonObject.addProperty("Message", e.getMessage());
			returnJsonObject.addProperty("ContractId", "");
			returnJsonObject.addProperty("CustomerId", "");
			returnJsonObject.addProperty("ResponseCode", "");
			returnJsonObject.addProperty("SignerName", "");
			returnJsonObject.addProperty("SignOrder", "");
		}
		
		if(debug)
			servletResponse.getWriter().println("proprietorESignContract-response: "+new Gson().toJson(returnJsonObject));
		
		return returnJsonObject;
	}
}

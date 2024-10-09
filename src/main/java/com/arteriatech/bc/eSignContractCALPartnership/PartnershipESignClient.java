package com.arteriatech.bc.eSignContractCALPartnership;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorshipServiceLocator;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_Request;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_RequestInitiateRequest;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_RequestInitiateRequestRequest;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_RequestInitiateRequestRequestSIgnerDetail;
import com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_Response;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PartnershipESignClient {
	public JsonObject partnershipESignContract(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Map<String, String> wsPayloadData, 
			String aggregatorID, List<Map<String, String>> partnerData, List<Map<String, String>> signerData, boolean debug) throws IOException{
		JsonObject returnJsonObject = new JsonObject();
		
		ESignContractCALPartnership_Request partnerRequest = new ESignContractCALPartnership_Request();
		ESignContractCALPartnership_RequestRequest partnerReqRequest = new ESignContractCALPartnership_RequestRequest();
		ESignContractCALPartnership_RequestTemplateVariables templateVariables = new ESignContractCALPartnership_RequestTemplateVariables();
		
		CommonUtils commonUtils = new CommonUtils();
		String system="", endPointURL="";
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"eSignContractCALPartnership";
			
			String[][] partnerNameArray = new String[1][partnerData.size()];
			String[][] aadharNumberArray = new String[1][partnerData.size()];
			String[][] dateOfBirthArray = new String[1][partnerData.size()];
			String[][] emailAddressArray = new String[1][partnerData.size()];
			String[][] mobileNumberArray = new String[1][partnerData.size()];
			String[][] panNumberArray = new String[1][partnerData.size()];
			
			templateVariables.setAadharNumberList(aadharNumberArray);
			templateVariables.setDateOfBirthList(dateOfBirthArray);
			templateVariables.setEmailAddressList(emailAddressArray);
			templateVariables.setMobileNumberList(mobileNumberArray);
			templateVariables.setPANList(panNumberArray);
			templateVariables.setPartnerNameList(partnerNameArray);			
			
			for(int i=0 ; i<partnerData.size() ; i++){
				partnerNameArray[0][i] = partnerData.get(i).get("PartnerName");
				aadharNumberArray[0][i] = partnerData.get(i).get("AadharNumber");
				dateOfBirthArray[0][i] = partnerData.get(i).get("DateOfBirth");
				emailAddressArray[0][i] = partnerData.get(i).get("EmailAddress");
				mobileNumberArray[0][i] = partnerData.get(i).get("MobileNumber");
				panNumberArray[0][i] = partnerData.get(i).get("PANNumber");
			}
			templateVariables.setAadharNumberList(aadharNumberArray);
			templateVariables.setDateOfBirthList(dateOfBirthArray);
			templateVariables.setEmailAddressList(emailAddressArray);
			templateVariables.setMobileNumberList(mobileNumberArray);
			templateVariables.setPANList(panNumberArray);
			templateVariables.setPartnerNameList(partnerNameArray);
			
			
			//Header Data
			templateVariables.setApplicationId(wsPayloadData.get("LeadId"));
			templateVariables.setConstitution(wsPayloadData.get("Constitution"));
			templateVariables.setCorporateName(wsPayloadData.get("CorporateName"));
			templateVariables.setCurrentDate(wsPayloadData.get("CurrentDate"));
			templateVariables.setCurrentTime(wsPayloadData.get("CurrentTime"));
			templateVariables.setDateOfIncorporation(wsPayloadData.get("DateOfIncorporation"));
			templateVariables.setDealerName(wsPayloadData.get("DealerName"));
			templateVariables.setDefaultSpread(wsPayloadData.get("DefaultInterestSpread"));
			templateVariables.setExpiryDate(wsPayloadData.get("ExpiryDate"));
			templateVariables.setExtraSpread(wsPayloadData.get("AddlnPeriodInterestRateSpread"));
			templateVariables.setExtraTenorPayment(wsPayloadData.get("AddlnTenorOfPayment"));
			templateVariables.setFacilityAmount(wsPayloadData.get("FacilityAmount"));
			templateVariables.setInterestRate(wsPayloadData.get("MCLR6MRate"));
			templateVariables.setIPAddress(wsPayloadData.get("IPAddress"));
			templateVariables.setPartnerAccount(wsPayloadData.get("PartnerAccount"));
			templateVariables.setPaymentTenor(wsPayloadData.get("TenorOfPayment"));
			templateVariables.setPFAmount(wsPayloadData.get("ProcessingFee"));
			templateVariables.setSpreadRate(wsPayloadData.get("InterestRateSpread"));
			
			partnerRequest.setCPType(wsPayloadData.get("CPType"));
			partnerRequest.setTestRun(wsPayloadData.get("TestRun"));
			partnerRequest.setTemplateVariables(templateVariables);
			
			partnerReqRequest.setAggregatorId(aggregatorID);
//			1
			
			ESignContractCALPartnership_RequestRequestSIgnerDetail signerDetailArray[] = new ESignContractCALPartnership_RequestRequestSIgnerDetail[signerData.size()];
			partnerReqRequest.setSIgnerDetail(signerDetailArray);
			
			for(int i=0 ; i<signerData.size() ; i++){
				ESignContractCALPartnership_RequestRequestSIgnerDetail signerDetail = new ESignContractCALPartnership_RequestRequestSIgnerDetail();
				/*signerDetailArray[i].setAadharNumber(signerData.get(i).get("SignerAadharNumber"));
				signerDetailArray[i].setSignerEmailId(signerData.get(i).get("SignerEmailId"));
				signerDetailArray[i].setSignerName(signerData.get(i).get("SignerName"));
				signerDetailArray[i].setSignOrder(signerData.get(i).get("SignOrder"));*/
				
				signerDetail.setAadharNumber(signerData.get(i).get("SignerAadharNumber"));
				signerDetail.setSignerEmailId(signerData.get(i).get("SignerEmailId"));
				signerDetail.setSignerName(signerData.get(i).get("SignerName"));
				signerDetail.setSignOrder(signerData.get(i).get("SignOrder"));
				if(debug){
					servletResponse.getWriter().println("partnershipESignContract.SignerEmailId ("+i+"): "+signerData.get(i).get("SignerEmailId"));
					servletResponse.getWriter().println("partnershipESignContract.SignerName ("+i+"): "+signerData.get(i).get("SignerName"));
					servletResponse.getWriter().println("partnershipESignContract.SignOrder ("+i+"): "+signerData.get(i).get("SignOrder"));
				}
//				partnerReqRequest.setSIgnerDetail(i, signerDetailArray[i]);
				partnerReqRequest.setSIgnerDetail(i, signerDetail);
				
			}
//			partnerReqRequest.setSIgnerDetail(signerDetailArray);
			partnerRequest.setRequest(partnerReqRequest);
			
			if(debug){
//				String userName = commonUtils.getODataDestinationProperties("User","BankConnect");
//				String password = commonUtils.getODataDestinationProperties("Password","BankConnect");
				servletResponse.getWriter().println("partnershipESignContract.endPointURL: "+endPointURL);
//				servletResponse.getWriter().println("partnershipESignContract.userName: "+userName);
//				servletResponse.getWriter().println("partnershipESignContract.password: "+password);
			}
			
			ESignContractCALPartnershipServiceLocator partnerServiceLocator = new ESignContractCALPartnershipServiceLocator();
			partnerServiceLocator.setEndpointAddress("eSignContractCALPartnershipPort", endPointURL);
			ESignContractCALPartnership eSignCalPartnerService = partnerServiceLocator.geteSignContractCALPartnershipPort();
			
			ESignContractCALPartnership_Response partnerResponse = new ESignContractCALPartnership_Response();
			partnerResponse = eSignCalPartnerService.eSignContractCALPartnership(partnerRequest);
			
			
			if(null != partnerResponse.getStatus() && partnerResponse.getStatus().trim().length() > 0){
				if(partnerResponse.getStatus().equalsIgnoreCase("000001")){
					returnJsonObject.addProperty("Status", partnerResponse.getStatus());
					if(null == partnerResponse.getMessage()){
						returnJsonObject.addProperty("Message", "");
					}else{
						returnJsonObject.addProperty("Message", partnerResponse.getMessage());
					}
					
					returnJsonObject.addProperty("ContractId", partnerResponse.getResponse().getContractId());
					returnJsonObject.addProperty("CustomerId", partnerResponse.getResponse().getCustomerId());
					returnJsonObject.addProperty("ResponseCode", partnerResponse.getResponseCode());
					returnJsonObject.addProperty("SignerName", partnerResponse.getResponse().getSignerDetail(0).getSignerName());
					returnJsonObject.addProperty("SignOrder", partnerResponse.getResponse().getSignerDetail(0).getSignOrder());
				}else{
					returnJsonObject.addProperty("Status", partnerResponse.getStatus());
					
					if(null == partnerResponse.getMessage())
						returnJsonObject.addProperty("Message", "");
					else
						returnJsonObject.addProperty("Message", partnerResponse.getMessage());
					
					if(null == partnerResponse.getResponse().getContractId())
						returnJsonObject.addProperty("ContractId", "");
					else
						returnJsonObject.addProperty("ContractId", partnerResponse.getResponse().getContractId());
					
					if(null == partnerResponse.getResponse().getCustomerId())
						returnJsonObject.addProperty("CustomerId", "");
					else
						returnJsonObject.addProperty("CustomerId", partnerResponse.getResponse().getCustomerId());
					
					if(null == partnerResponse.getResponseCode())
						returnJsonObject.addProperty("ResponseCode", "");
					else
						returnJsonObject.addProperty("ResponseCode", partnerResponse.getResponseCode());
					
					if(null == partnerResponse.getResponse().getSignerDetail(0).getSignerName())
						returnJsonObject.addProperty("SignerName", "");
					else
						returnJsonObject.addProperty("SignerName", partnerResponse.getResponse().getSignerDetail(0).getSignerName());
					
					if(null == partnerResponse.getResponse().getSignerDetail(0).getSignOrder())
						returnJsonObject.addProperty("SignOrder", "");
					else
						returnJsonObject.addProperty("SignOrder", partnerResponse.getResponse().getSignerDetail(0).getSignOrder());
				}
			}else{
				returnJsonObject.addProperty("Status", "000002");
				if(null == partnerResponse.getMessage())
					returnJsonObject.addProperty("Message", "");
				else
					returnJsonObject.addProperty("Message", partnerResponse.getMessage());
				
				if(null == partnerResponse.getResponse().getContractId())
					returnJsonObject.addProperty("ContractId", "");
				else
					returnJsonObject.addProperty("ContractId", partnerResponse.getResponse().getContractId());
				
				if(null == partnerResponse.getResponse().getCustomerId())
					returnJsonObject.addProperty("CustomerId", "");
				else
					returnJsonObject.addProperty("CustomerId", partnerResponse.getResponse().getCustomerId());
				
				if(null == partnerResponse.getResponseCode())
					returnJsonObject.addProperty("ResponseCode", "");
				else
					returnJsonObject.addProperty("ResponseCode", partnerResponse.getResponseCode());
				
				if(null == partnerResponse.getResponse().getSignerDetail(0).getSignerName())
					returnJsonObject.addProperty("SignerName", "");
				else
					returnJsonObject.addProperty("SignerName", partnerResponse.getResponse().getSignerDetail(0).getSignerName());
				
				if(null == partnerResponse.getResponse().getSignerDetail(0).getSignOrder())
					returnJsonObject.addProperty("SignOrder", "");
				else
					returnJsonObject.addProperty("SignOrder", partnerResponse.getResponse().getSignerDetail(0).getSignOrder());
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
			servletResponse.getWriter().println("partnershipESignContract-response: "+new Gson().toJson(returnJsonObject));
		
		return returnJsonObject;
	}
}

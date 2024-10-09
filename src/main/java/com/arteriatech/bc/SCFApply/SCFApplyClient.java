package com.arteriatech.bc.SCFApply;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFOffer.SCFOffer;
import com.arteriatech.bc.SCFOffer.SCFOfferServiceLocator;
import com.arteriatech.bc.SCFOffer.SCFOffer_Response;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SCFApplyClient {
	public Map<String,String> scfApply(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String corpID, Map wsHeaderData, JsonObject promoterDataObj, boolean debug) throws IOException{
		String responseValue = "", endPointURL="", system="";
		Map<String,String> scfApplyResponseMap = new HashMap<String,String>();
		Map<String,String> scfApplyhdrMap = new HashMap<String,String>();
		SCFOfferServiceLocator locator = new SCFOfferServiceLocator();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"SCFApply";
			
			scfApplyhdrMap = wsHeaderData;
			
			SCFApplyRequest applyHdrRequest = new SCFApplyRequest();
			
			for (String key : scfApplyhdrMap.keySet()) {
				if(debug)
					servletResponse.getWriter().println("ws-scfApplyhdrMap-->"+key + " - " + scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("PAN"))
					applyHdrRequest.setPAN(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("corporateID"))
					applyHdrRequest.setCorporateID(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("constitutionType"))
					applyHdrRequest.setConstitutionType(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dateOfIncorporation"))
					applyHdrRequest.setDateOfIncorporation("19000101");
//					applyHdrRequest.setDateOfIncorporation(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerAddress1"))
					applyHdrRequest.setDealerAddress1(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerAddress2"))
					applyHdrRequest.setDealerAddress2(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerAddress3"))
					applyHdrRequest.setDealerAddress3(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerAddress4"))
					applyHdrRequest.setDealerAddress4(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerAddress5"))
					applyHdrRequest.setDealerAddress5(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerCity"))
					applyHdrRequest.setDealerCity(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerID"))
					applyHdrRequest.setDealerID(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerName"))
					applyHdrRequest.setDealerName(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerPincode"))
					applyHdrRequest.setDealerPincode(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("dealerState"))
					applyHdrRequest.setDealerState(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("GSTIN"))
					applyHdrRequest.setGSTIN(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("isEligible"))
					applyHdrRequest.setIsEligible(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("TIN"))
					applyHdrRequest.setTIN(scfApplyhdrMap.get(key));
				if(key.equalsIgnoreCase("CIN"))
					applyHdrRequest.setCIN(scfApplyhdrMap.get(key));
			}
			
			String promoterAddress1="", promoterAddress2="", promoterAddress3="", promoterAddress4="", promoterAddress5="", promoterCity="", 
					promoterPinCode="", promoterState="";
			for (String key : scfApplyhdrMap.keySet()) {
				if(key.equalsIgnoreCase("dealerAddress1"))
					promoterAddress1=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerAddress2"))
					promoterAddress2=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerAddress3"))
					promoterAddress3=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerAddress4"))
					promoterAddress4=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerAddress5"))
					promoterAddress5=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerCity"))
					promoterCity=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerPincode"))
					promoterPinCode=scfApplyhdrMap.get(key);
				if(key.equalsIgnoreCase("dealerState"))
					promoterState=scfApplyhdrMap.get(key);
			}
			
			JsonObject results = promoterDataObj.get("d").getAsJsonObject();
			JsonArray dresults = results.get("results").getAsJsonArray();
			
			JsonObject contactPersonsJsonObj = null;
			JsonArray bpCPArray = null;
			JsonObject bpCPJsonObj = null;
			for (int i = 0; i <= dresults.size() - 1; i++) {
				contactPersonsJsonObj = (JsonObject) dresults.get(i);
				bpCPJsonObj = (JsonObject)contactPersonsJsonObj.get("BPContactPersons").getAsJsonObject();
			}
			bpCPArray = bpCPJsonObj.get("results").getAsJsonArray();
			
			SCFApplyRequestPromoter[] promoterData = new SCFApplyRequestPromoter[bpCPArray.size()];
			String promoterDataDOBStr="";
			for(int i=0; i<bpCPArray.size() ; i++){
				promoterData[i] = new SCFApplyRequestPromoter();
				promoterDataDOBStr="";
				promoterData[i].setPromoterAadhar("");
				promoterData[i].setPromoterAddress1(promoterAddress1);
				promoterData[i].setPromoterAddress2(promoterAddress2);
				promoterData[i].setPromoterAddress3(promoterAddress3);
				promoterData[i].setPromoterAddress4(promoterAddress4);
				promoterData[i].setPromoterAddress5(promoterAddress5);
				promoterData[i].setPromoterCity(promoterCity);
				promoterDataDOBStr = commonUtils.convertLongDateToString(servletResponse, bpCPArray.get(i).getAsJsonObject().get("DOB").getAsString(), debug);
				promoterData[i].setPromoterDOB(promoterDataDOBStr);
//				promoterData[i].setPromoterDOB(bpCPArray.get(i).getAsJsonObject().get("DOB").getAsString());
//				promoterData[i].setPromoterDOB("19851215");
				
				promoterData[i].setPromoterFirstName(bpCPArray.get(i).getAsJsonObject().get("Name1").getAsString());
				if(bpCPArray.get(i).getAsJsonObject().get("GenderID").getAsString().equalsIgnoreCase("M"))
					promoterData[i].setPromoterGender("Male");
				else if(bpCPArray.get(i).getAsJsonObject().get("GenderID").getAsString().equalsIgnoreCase("F"))
					promoterData[i].setPromoterGender("Female");
				else
					promoterData[i].setPromoterGender("Others");
				promoterData[i].setPromoterLastName(bpCPArray.get(i).getAsJsonObject().get("Name2").getAsString());
				promoterData[i].setPromoterMobileNumber(bpCPArray.get(i).getAsJsonObject().get("Mobile").getAsString());
				promoterData[i].setPromoterPAN(bpCPArray.get(i).getAsJsonObject().get("PanNo").getAsString());
				promoterData[i].setPromoterPassport("");
				promoterData[i].setPromoterPincode(promoterPinCode);
				promoterData[i].setPromoterState(promoterState);
				promoterData[i].setPromoterVoterID("");
				
				applyHdrRequest.setPromoter(promoterData);
				
				if(debug){
					servletResponse.getWriter().println("ws-DOB-->"+bpCPArray.get(i).getAsJsonObject().get("DOB").getAsString());
					servletResponse.getWriter().println("ws-Name1-->"+bpCPArray.get(i).getAsJsonObject().get("Name1").getAsString());
					servletResponse.getWriter().println("ws-GenderID-->"+bpCPArray.get(i).getAsJsonObject().get("GenderID").getAsString());
					servletResponse.getWriter().println("ws-Name2-->"+bpCPArray.get(i).getAsJsonObject().get("Name2").getAsString());
					servletResponse.getWriter().println("ws-Mobile-->"+bpCPArray.get(i).getAsJsonObject().get("Mobile").getAsString());
					servletResponse.getWriter().println("ws-PanNo-->"+bpCPArray.get(i).getAsJsonObject().get("PanNo").getAsString());
				}
			}
			
			SCFApplyServiceLocator asLocator = new SCFApplyServiceLocator();
			asLocator.setEndpointAddress("SCFApplyPort", endPointURL);
			SCFApply service = asLocator.getSCFApplyPort();
			
			SCFApplyResponse response = service.SCFApply(applyHdrRequest);
			
//			servletResponse.getWriter().println(response.);
			if(debug){
				servletResponse.getWriter().println("SCFApply.DirectResponse.Status"+response.getStatus());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getAccountNo"+response.getAccountNo());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getAmount"+response.getAmount());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getErrorCode"+response.getErrorCode());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getLeadID"+response.getLeadID());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getMessage"+response.getMessage());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getRate"+response.getRate());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getTenure"+response.getTenure());
				servletResponse.getWriter().println("SCFApply.DirectResponse.getValidTo"+response.getValidTo());
			}
			
			if(response.getStatus().equalsIgnoreCase("000002")){
				scfApplyResponseMap.put("AccountNo", "");
				scfApplyResponseMap.put("Amount", "0.00");
				if(null == response.getErrorCode() || response.getErrorCode().trim().length() == 0)
					scfApplyResponseMap.put("ErrorCode", "059");
				else
					scfApplyResponseMap.put("ErrorCode", response.getErrorCode());
				
				if(null != response.getLeadID() && response.getLeadID().trim().length() > 0)
					scfApplyResponseMap.put("LeadID", response.getLeadID());
				else
					scfApplyResponseMap.put("LeadID", "");
				
				if(response.getMessage() == null){
					scfApplyResponseMap.put("Message", "");
				}else{
					scfApplyResponseMap.put("Message", response.getMessage());
				}
				
				scfApplyResponseMap.put("Rate", "");
				scfApplyResponseMap.put("Status", response.getStatus());
				scfApplyResponseMap.put("Tenure", "0");
				
				if(null != response.getValidTo() && response.getValidTo().trim().length() > 0)
					scfApplyResponseMap.put("ValidTo", response.getValidTo());
				else
					scfApplyResponseMap.put("ValidTo", "");
			}else{
				scfApplyResponseMap.put("AccountNo", response.getAccountNo());
				scfApplyResponseMap.put("Amount", response.getAmount());
				scfApplyResponseMap.put("ErrorCode", "");
				scfApplyResponseMap.put("LeadID", response.getLeadID());
				scfApplyResponseMap.put("Message", "");
				scfApplyResponseMap.put("Rate", response.getRate());
				scfApplyResponseMap.put("Status", response.getStatus());
				scfApplyResponseMap.put("Tenure", response.getTenure());
				scfApplyResponseMap.put("ValidTo", response.getValidTo());
			}
		}catch (Exception e) {
			scfApplyResponseMap.put("AccountNo", "");
			scfApplyResponseMap.put("Amount", "0.00");
			scfApplyResponseMap.put("ErrorCode", "001");
			scfApplyResponseMap.put("LeadID", "");
			scfApplyResponseMap.put("Message", e.getMessage());
			scfApplyResponseMap.put("Rate", "");
			scfApplyResponseMap.put("Status", "000002");
			scfApplyResponseMap.put("Tenure", "0");
			scfApplyResponseMap.put("ValidTo", "");
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				servletResponse.getWriter().println("SCFApply.Exception: "+e.getMessage()+". Full Stack Trace --> "+buffer.toString());
			}
		}
		
		if(debug){
			for (String key : scfApplyResponseMap.keySet()) {
				servletResponse.getWriter().println("WSClass.scfApplyResponseMap: "+key + " - " + scfApplyResponseMap.get(key));
			}
		}
		return scfApplyResponseMap;
	}
	
}

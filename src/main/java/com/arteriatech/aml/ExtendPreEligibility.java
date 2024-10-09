package com.arteriatech.aml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExtendPreEligibility extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonUtils commonUtils=new CommonUtils();
		String inputPayload="",cpType="",cpNo="";
		JsonParser parse=new JsonParser();
		JsonObject resObj=new JsonObject();
		AMLUtils amlUtils=new AMLUtils();
				
		boolean debug=false;
		String url="",executeURL="",username="",password="",userpass="",aggregatorId="";
		try{
			inputPayload=commonUtils.getGetBody(request, response);
			if(inputPayload!=null && !inputPayload.equalsIgnoreCase("")){
				JsonObject jsonPayload=(JsonObject)parse.parse(inputPayload);
				if(jsonPayload.has("debug")&& jsonPayload.get("debug").getAsString().equalsIgnoreCase("true")){
					debug=true;
				}
				if(debug){
					response.getWriter().println("Received input Payload:"+jsonPayload);
				}
				if(jsonPayload.has("CPTYPE")&&!jsonPayload.get("CPTYPE").isJsonNull()&&!jsonPayload.get("CPTYPE").getAsString().equalsIgnoreCase("")){
					cpType=jsonPayload.get("CPTYPE").getAsString();
				}
				
				if(jsonPayload.has("CPNO")&&!jsonPayload.get("CPNO").isJsonNull()&&!jsonPayload.get("CPNO").getAsString().equalsIgnoreCase("")){
					cpNo=jsonPayload.get("CPNO").getAsString();
				}
				if (cpType != null && !cpType.equalsIgnoreCase("")) {
					if (cpNo != null && !cpNo.equalsIgnoreCase("")) {
						url=commonUtils.getODataDestinationProperties("URL", DestinationUtils.PYGWHANA);
						password=commonUtils.getODataDestinationProperties("Password", DestinationUtils.PYGWHANA);
						username=commonUtils.getODataDestinationProperties("User", DestinationUtils.PYGWHANA);
						aggregatorId=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PYGWHANA);
						userpass=username+":"+password;
						executeURL=url+"SupplyChainFinanceEligibility?$filter=CPGUID%20eq%20%27"+cpNo+"%27%20and%20CPTypeID%20eq%20%27"+cpType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorId+"%27";
						if(debug){
							response.getWriter().println("SupplyChainFinanceEligibility execute URL:"+executeURL);
						}
						JsonObject scfResponse = commonUtils.executeODataURL(executeURL, userpass, response, debug);
                       if(debug){
                    	   response.getWriter().println("scfResponse:"+scfResponse);
                       }
						if(scfResponse.get("Status").getAsString().equalsIgnoreCase("000001")){
                        	scfResponse= scfResponse.get("Message").getAsJsonObject();
                        	JsonObject updatedScfRec=null;
                        	if(scfResponse.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
                        		executeURL = url + "BPEligibilityRecords?$expand=BPCNTPEligibilityRecords&$filter=AggregatorID%20eq%20%27"
                						+ aggregatorId + "%27%20and%20CPGuid%20eq%20%27"+cpNo+"%27%20and%20CPType%20eq%20%27"+cpType+"%27%20and%20CorrelationID%20ne%20%27null%27";
                				if (debug) {
                					response.getWriter().println("executeURL:" + executeURL);
                				}
                				
                				JsonObject bpEligibilityRes = commonUtils.executeODataURL(executeURL, userpass, response, debug);
                                if(debug){
                             	   response.getWriter().println("bpEligibilityRes:"+bpEligibilityRes);
                                }
                				if(bpEligibilityRes.get("Status").getAsString().equalsIgnoreCase("000001")){
                					bpEligibilityRes= bpEligibilityRes.get("Message").getAsJsonObject();
									if (bpEligibilityRes.get("d").getAsJsonObject().get("results").getAsJsonArray()
											.size() > 0) {
										List<String> recordIds=new ArrayList<>();
										JsonObject bpEligibilityObj = bpEligibilityRes.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
										String recId = bpEligibilityObj.get("RecordID").getAsString();
										recordIds.add(recId);
										JsonArray bpcntpEligibleArry = bpEligibilityObj.get("BPCNTPEligibilityRecords").getAsJsonObject().get("results").getAsJsonArray();
										if(bpcntpEligibleArry.size()>0){
											for(int i=0;i<bpcntpEligibleArry.size();i++){
												recId=bpcntpEligibleArry.get(i).getAsJsonObject().get("RecordID").getAsString();
												recordIds.add(recId);
											}
										JsonArray asJsonArray = scfResponse.get("d").getAsJsonObject().get("results")
												.getAsJsonArray();
										String validTo = "";
										for (int i = 0; i < asJsonArray.size(); i++) {
											JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
											if (asJsonObject.get("EligibilityTypeID").isJsonNull() || asJsonObject
													.get("EligibilityTypeID").getAsString().equalsIgnoreCase("")) {
												// updatedScfRoc=asJsonObject;
												if (!asJsonObject.get("ValidTo").isJsonNull()) {
													validTo = asJsonObject.get("ValidTo").getAsString();
												}
											} else if (!asJsonObject.get("EligibilityTypeID").isJsonNull()
													&& asJsonObject.get("EligibilityTypeID").getAsString()
															.equalsIgnoreCase("AML")) {
												updatedScfRec = asJsonObject;
											}
										}
										if (debug) {
											response.getWriter().println("updatedScfRoc:" + updatedScfRec);
											response.getWriter().println("ValidTo:" + validTo);
										}
										if (updatedScfRec != null && !validTo.equalsIgnoreCase("")) {
											updatedScfRec.remove("__metadata");
											updatedScfRec.remove("ValidTo");
											updatedScfRec.addProperty("ValidTo", validTo);
											if (debug) {
												response.getWriter().println("Updated scf1 Record:" + updatedScfRec);
											}
											executeURL = url + "SupplyChainFinanceEligibility('"
													+ updatedScfRec.get("ID").getAsString() + "')";
													com.google.gson.JsonObject scfUpdateRespose = commonUtils.executeUpdate(executeURL,
													userpass, response, updatedScfRec, request, debug, "PYGWHANA");
											if (debug) {
												response.getWriter().println("scfUpdateRespose:" + scfUpdateRespose);
											}
											if (scfUpdateRespose.get("ErrorCode").getAsString().equalsIgnoreCase("")) {
												JsonObject eligibilityRecord = amlUtils.getEligibilityRecord(request, response, url, userpass, recordIds,debug);
												if(eligibilityRecord.get("Status").getAsString().equalsIgnoreCase("000001")){
													
												JsonArray eligibilityRecArry = eligibilityRecord.get("Message").getAsJsonArray();
												JsonObject updateEligibleRec = amlUtils.updateAllEligibility(request, response, url, userpass, eligibilityRecArry, validTo, debug);
												if(debug){
													response.getWriter().println(updateEligibleRec);
												}
												if(updateEligibleRec.get("Status").getAsString().equalsIgnoreCase("000001")){
												resObj.addProperty("Message", "Updated Successfully");
												resObj.addProperty("Status", "000001");
												resObj.addProperty("ErrorCode", "");
												response.getWriter().println(resObj);
												}else{
													response.getWriter().println(updateEligibleRec);
												}
												}else{
													response.getWriter().println(eligibilityRecord);
												}
											} else {
												resObj.addProperty("Message",
														"we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
												resObj.addProperty("Status", "000002");
												resObj.addProperty("ErrorCode", "AML00011");
												response.getWriter().println(resObj);

											}

										} else {
											resObj.addProperty("Message",
													"we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
											resObj.addProperty("Status", "000002");
											resObj.addProperty("ErrorCode", "AML00012");
											response.getWriter().println(resObj);
										}
										}else{
											resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
			                    			resObj.addProperty("Status", "000002");
			                    			resObj.addProperty("ErrorCode", "J002");
			                    			response.getWriter().println(resObj);
							
										}
									}else{
										resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
		                    			resObj.addProperty("Status", "000002");
		                    			resObj.addProperty("ErrorCode", "J002");
		                    			response.getWriter().println(resObj);
                						
                					}
                        	}else{
                        		response.getWriter().println(bpEligibilityRes);
                        	}
                        	}else{
                        		// Records not exist in the Table.
                        		resObj.addProperty("Message", "we are facing some technical issues in checking your sanction eligibility. Please check back in sometime");
                    			resObj.addProperty("Status", "000002");
                    			resObj.addProperty("ErrorCode", "J002");
                    			response.getWriter().println(resObj);
                        	}
                        	
                        }else{
                        	response.getWriter().println(scfResponse);
                        }
					} else {
						// input Payload doesnt conatins a cpNo.
						resObj.addProperty("Message", "Input Payload doesn't contains a CPNO");
            			resObj.addProperty("Status", "000002");
            			resObj.addProperty("ErrorCode", "J002");
            			response.getWriter().println(resObj);
					}
				}else{
					// input Payload doesnt conatins a cptype.
					resObj.addProperty("Message", "Input Payload doesn't contains a CPTYPE");
        			resObj.addProperty("Status", "000002");
        			resObj.addProperty("ErrorCode", "J002");
        			response.getWriter().println(resObj);
				}
			}else{
				// empty input Payload received from UI.
				resObj.addProperty("Message", "Empty input Payload received from the UI");
    			resObj.addProperty("Status", "000002");
    			resObj.addProperty("ErrorCode", "J002");
    			response.getWriter().println(resObj);
			}
			
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("Message", buffer.toString());
            response.getWriter().println(resObj);
		}
	}
	
	

}

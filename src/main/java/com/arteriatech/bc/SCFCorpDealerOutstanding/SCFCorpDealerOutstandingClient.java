package com.arteriatech.bc.SCFCorpDealerOutstanding;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SCFCorpDealerOutstandingClient {

	public JsonObject getSCFCorpDealerOutstandingClient(HttpServletResponse response, String aggregatorID , int countOfPipes, String aggregatorName,String parentLimitPrefixId , boolean debug) throws IOException{

		String  endPointURL = "", system="";
		JsonArray onBoardOtherFacilitiesArray = new JsonArray();
		JsonObject scfCorpDealerOutStandingResponse = new JsonObject();
		JsonObject scfCorpDealerOutStandingObject = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();

		try {
			// system = commonUtils.getODataDestinationProperties("System",DestinationUtils.BANK_CONNECT);
              system = "DEV" ;
			// endPointURL = commonUtils.getODataDestinationProperties("URL",DestinationUtils.BANK_CONNECT);
			// endPointURL  = endPointURL + "SCFCorpDealerOutstanding";
			endPointURL = "https://cpi-dev-py-finessart.it-cpi018-rt.cfapps.eu10-003.hana.ondemand.com/cxf/ARTEC/BC/SCFCorpDealerOutstanding";

			SCFCorpDealerOutstanding_Request dealerOutstanding_Request=new SCFCorpDealerOutstanding_Request();
			dealerOutstanding_Request.setParentLimitPrefixId(parentLimitPrefixId);

			if(debug){
				response.getWriter().println("dealerOutstanding_Request"+dealerOutstanding_Request);
			}
			
			SCFCorpDealerOutstandingServiceLocator locator=new SCFCorpDealerOutstandingServiceLocator();
			locator.setEndpointAddress("SCFCorpDealerOutstandingPort", endPointURL);

			if(debug){
				response.getWriter().println("locator"+locator);
			}
			
			SCFCorpDealerOutstanding dealerOutstanding=locator.getSCFCorpDealerOutstandingPort();
			if(debug){
				response.getWriter().println("dealerOutstanding"+dealerOutstanding);
			}
			
			SCFCorpDealerOutstanding_PortalResponse scfCorpDealerResponse=dealerOutstanding.SCFCorpDealerOutstanding(dealerOutstanding_Request);
			if(debug){
				response.getWriter().println("scfCorpDealerResponse"+scfCorpDealerResponse);
			}
			
			SCFCorpDealerOutstanding_PortalResponseReport [] outstanding_PortalResponseReport = scfCorpDealerResponse.getReport();

			if(debug){
				response.getWriter().println("SCFDealerOutstanding.getStatus: "+scfCorpDealerResponse.getStatus().getStatus());
				response.getWriter().println("SCFDealerOutstanding.getResponseCode: "+scfCorpDealerResponse.getStatus().getResponseCode());
				response.getWriter().println("SCFDealerOutstanding.getMessage: "+scfCorpDealerResponse.getStatus().getMessage());
				response.getWriter().println("SCFDealerOutstanding.getOutstandingAmount: "+scfCorpDealerResponse.getReport());
			}
			
			if (null != scfCorpDealerResponse.getStatus().getStatus() && scfCorpDealerResponse.getStatus().getStatus().trim().length() > 0) {
				if (scfCorpDealerResponse.getStatus().getStatus().equalsIgnoreCase("000001")) {
						
					for(int i=0 ; i<=outstanding_PortalResponseReport.length-1;i++){
						
						scfCorpDealerOutStandingObject = new JsonObject();
						scfCorpDealerOutStandingObject.addProperty("Status", scfCorpDealerResponse.getStatus().getStatus());

						if (null != scfCorpDealerResponse.getStatus().getResponseCode() && scfCorpDealerResponse.getStatus().getResponseCode().trim().length() > 0)
							scfCorpDealerOutStandingObject.addProperty("ResponseCode", scfCorpDealerResponse.getStatus().getResponseCode());
						else
							scfCorpDealerOutStandingObject.addProperty("ResponseCode", "" );

						if (null != scfCorpDealerResponse.getStatus().getMessage() && scfCorpDealerResponse.getStatus().getMessage().trim().length() > 0)
							scfCorpDealerOutStandingObject.addProperty("Message", scfCorpDealerResponse.getStatus().getMessage() );
						else
							scfCorpDealerOutStandingObject.addProperty("Message", "" );
							
						if (null!=scfCorpDealerResponse.getReport(i).getODAccountNumber() && scfCorpDealerResponse.getReport(i).getODAccountNumber().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("ODAccountNumber", scfCorpDealerResponse.getReport(i).getODAccountNumber());
						}else{
							scfCorpDealerOutStandingObject.addProperty("ODAccountNumber", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getCustomerID() && scfCorpDealerResponse.getReport(i).getCustomerID().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("CustomerID", scfCorpDealerResponse.getReport(i).getCustomerID());
						}else{
							scfCorpDealerOutStandingObject.addProperty("CustomerID", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getDealerName() && scfCorpDealerResponse.getReport(i).getDealerName().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("DealerName", scfCorpDealerResponse.getReport(i).getDealerName());
						}else{
							scfCorpDealerOutStandingObject.addProperty("DealerName", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getDealerLimit() && scfCorpDealerResponse.getReport(i).getDealerLimit().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("DealerLimit", scfCorpDealerResponse.getReport(i).getDealerLimit());
						}else{
							scfCorpDealerOutStandingObject.addProperty("DealerLimit", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getDealerExpiryDate() && scfCorpDealerResponse.getReport(i).getDealerExpiryDate().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("DealerExpiryDate", scfCorpDealerResponse.getReport(i).getDealerExpiryDate());
						}else{
							scfCorpDealerOutStandingObject.addProperty("DealerExpiryDate", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getPeakLimit() && scfCorpDealerResponse.getReport(i).getPeakLimit().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("PeakLimit", scfCorpDealerResponse.getReport(i).getPeakLimit());
						}else{
							scfCorpDealerOutStandingObject.addProperty("PeakLimit", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getTODLimit() && scfCorpDealerResponse.getReport(i).getTODLimit().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("TODLimit", scfCorpDealerResponse.getReport(i).getTODLimit());
						}else{
							scfCorpDealerOutStandingObject.addProperty("TODLimit", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getNoOfDisbursement() && scfCorpDealerResponse.getReport(i).getNoOfDisbursement().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("NoOfDisbursement", scfCorpDealerResponse.getReport(i).getNoOfDisbursement());
						}else{
							scfCorpDealerOutStandingObject.addProperty("NoOfDisbursement", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getTotalDisbursementExcludingInterest() && scfCorpDealerResponse.getReport(i).getTotalDisbursementExcludingInterest().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("TotalDisbursementExcludingInterest", scfCorpDealerResponse.getReport(i).getTotalDisbursementExcludingInterest());
						}else{
							scfCorpDealerOutStandingObject.addProperty("TotalDisbursementExcludingInterest", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getRepaymentAmount() && scfCorpDealerResponse.getReport(i).getRepaymentAmount().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("RepaymentAmount", scfCorpDealerResponse.getReport(i).getRepaymentAmount());
						}else{
							scfCorpDealerOutStandingObject.addProperty("RepaymentAmount", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getBalanceOutstanding() && scfCorpDealerResponse.getReport(i).getBalanceOutstanding().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("BalanceOutstanding", scfCorpDealerResponse.getReport(i).getBalanceOutstanding());
						}else{
							scfCorpDealerOutStandingObject.addProperty("BalanceOutstanding", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getUnutilizedLimit() && scfCorpDealerResponse.getReport(i).getUnutilizedLimit().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("UnutilizedLimit", scfCorpDealerResponse.getReport(i).getUnutilizedLimit());
						}else{
							scfCorpDealerOutStandingObject.addProperty("UnutilizedLimit", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getNormalDues() && scfCorpDealerResponse.getReport(i).getNormalDues().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("NormalDues", scfCorpDealerResponse.getReport(i).getNormalDues());
						}else{
							scfCorpDealerOutStandingObject.addProperty("NormalDues", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueWithinCure() && scfCorpDealerResponse.getReport(i).getOverdueWithinCure().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueWithinCure", scfCorpDealerResponse.getReport(i).getOverdueWithinCure());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueWithinCure", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBeyondCure() && scfCorpDealerResponse.getReport(i).getOverdueBeyondCure().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBeyondCure", scfCorpDealerResponse.getReport(i).getOverdueBeyondCure());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBeyondCure", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBetween0To7Days() && scfCorpDealerResponse.getReport(i).getOverdueBetween0To7Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween0To7Days", scfCorpDealerResponse.getReport(i).getOverdueBetween0To7Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween0To7Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBetween7To14Days() && scfCorpDealerResponse.getReport(i).getOverdueBetween7To14Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween7To14Days", scfCorpDealerResponse.getReport(i).getOverdueBetween7To14Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween7To14Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBetween14To21Days() && scfCorpDealerResponse.getReport(i).getOverdueBetween14To21Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween14To21Days", scfCorpDealerResponse.getReport(i).getOverdueBetween14To21Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween14To21Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBetween21To28Days() && scfCorpDealerResponse.getReport(i).getOverdueBetween21To28Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween21To28Days", scfCorpDealerResponse.getReport(i).getOverdueBetween21To28Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween21To28Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBetween28To60Days() && scfCorpDealerResponse.getReport(i).getOverdueBetween28To60Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween28To60Days", scfCorpDealerResponse.getReport(i).getOverdueBetween28To60Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBetween28To60Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getOverdueBeyond60Days() && scfCorpDealerResponse.getReport(i).getOverdueBeyond60Days().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("OverdueBeyond60Days", scfCorpDealerResponse.getReport(i).getOverdueBeyond60Days());
						}else{
							scfCorpDealerOutStandingObject.addProperty("OverdueBeyond60Days", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getCity() && scfCorpDealerResponse.getReport(i).getCity().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("City", scfCorpDealerResponse.getReport(i).getCity());
						}else{
							scfCorpDealerOutStandingObject.addProperty("City", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getIsAccountFrozen() && scfCorpDealerResponse.getReport(i).getIsAccountFrozen().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("IsAccountFrozen", scfCorpDealerResponse.getReport(i).getIsAccountFrozen());
						}else{
							scfCorpDealerOutStandingObject.addProperty("IsAccountFrozen", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getDealerCode() && scfCorpDealerResponse.getReport(i).getDealerCode().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("DealerCode", scfCorpDealerResponse.getReport(i).getDealerCode());
						}else{
							scfCorpDealerOutStandingObject.addProperty("DealerCode", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getCurrency() && scfCorpDealerResponse.getReport(i).getCurrency().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("Currency", scfCorpDealerResponse.getReport(i).getCurrency());
						}else{
							scfCorpDealerOutStandingObject.addProperty("Currency", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getCurrency() && scfCorpDealerResponse.getReport(i).getCurrency().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("Currency", scfCorpDealerResponse.getReport(i).getCurrency());
						}else{
							scfCorpDealerOutStandingObject.addProperty("Currency", "");
						}

						if (null!=scfCorpDealerResponse.getReport(i).getCurrency() && scfCorpDealerResponse.getReport(i).getCurrency().trim().length() > 0){
							scfCorpDealerOutStandingObject.addProperty("Currency", scfCorpDealerResponse.getReport(i).getCurrency());
						}else{
							scfCorpDealerOutStandingObject.addProperty("Currency", "");
						}
						
						scfCorpDealerOutStandingObject.addProperty("AggregatorID", aggregatorID);
						scfCorpDealerOutStandingObject.addProperty("AggregatorName", aggregatorName);
						if(countOfPipes == 1){
							scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
							scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", "");
						}else if(countOfPipes == 2){
							scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
							scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", parentLimitPrefixId);
						}
						
						onBoardOtherFacilitiesArray.add(scfCorpDealerOutStandingObject);
					}
					scfCorpDealerOutStandingResponse.add("results", onBoardOtherFacilitiesArray);
					return scfCorpDealerOutStandingResponse;
				}else{
					scfCorpDealerOutStandingObject.addProperty("Status", scfCorpDealerResponse.getStatus().getStatus() );

					if (null != scfCorpDealerResponse.getStatus().getResponseCode() && scfCorpDealerResponse.getStatus().getResponseCode().trim().length() > 0)
						scfCorpDealerOutStandingObject.addProperty("ResponseCode", scfCorpDealerResponse.getStatus().getResponseCode() );
					else
						scfCorpDealerOutStandingObject.addProperty("ResponseCode", "" );

					if (null != scfCorpDealerResponse.getStatus().getMessage() && scfCorpDealerResponse.getStatus().getMessage().trim().length() > 0)
						scfCorpDealerOutStandingObject.addProperty("Message", scfCorpDealerResponse.getStatus().getMessage() );
					else
						scfCorpDealerOutStandingObject.addProperty("Message", "" );

					scfCorpDealerOutStandingObject.addProperty("ODAccountNumber", "");
					scfCorpDealerOutStandingObject.addProperty("CustomerID", "");
					scfCorpDealerOutStandingObject.addProperty("DealerName", "");
					scfCorpDealerOutStandingObject.addProperty("DealerLimit", "0.00");
					scfCorpDealerOutStandingObject.addProperty("DealerExpiryDate", "");
					scfCorpDealerOutStandingObject.addProperty("PeakLimit", "0.00");
					scfCorpDealerOutStandingObject.addProperty("TODLimit", "0.00");
					scfCorpDealerOutStandingObject.addProperty("NoOfDisbursement", "0.00");
					scfCorpDealerOutStandingObject.addProperty("TotalDisbursementExcludingInterest", "0.00");
					scfCorpDealerOutStandingObject.addProperty("RepaymentAmount", "0.00");
					scfCorpDealerOutStandingObject.addProperty("BalanceOutstanding", "0.00");
					scfCorpDealerOutStandingObject.addProperty("UnutilizedLimit", "0.00");
					scfCorpDealerOutStandingObject.addProperty("NormalDues", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueWithinCure", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueBeyondCure", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueBetween0To7Days", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueBetween7To14Days", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueBetween14To21Days", "0.00");
					scfCorpDealerOutStandingObject.addProperty("OverdueBetween21To28Days", "0.00");	
					scfCorpDealerOutStandingObject.addProperty("OverdueBetween28To60Days", "0.00");	
					scfCorpDealerOutStandingObject.addProperty("OverdueBeyond60Days", "0.00");	
					scfCorpDealerOutStandingObject.addProperty("City", "");	
					scfCorpDealerOutStandingObject.addProperty("IsAccountFrozen", "000001");	
					scfCorpDealerOutStandingObject.addProperty("DealerCode", "");	
					scfCorpDealerOutStandingObject.addProperty("Currency", "");	
					scfCorpDealerOutStandingObject.addProperty("AggregatorID", aggregatorID);
					scfCorpDealerOutStandingObject.addProperty("AggregatorName", aggregatorName);
					if(countOfPipes == 1){
						scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
						scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", "");
					}else if(countOfPipes == 2){
						scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
						scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", parentLimitPrefixId);
					}
					onBoardOtherFacilitiesArray.add(scfCorpDealerOutStandingObject);
					scfCorpDealerOutStandingResponse.add("results", onBoardOtherFacilitiesArray);
				}
			}else{
				scfCorpDealerOutStandingObject.addProperty("Status", "000002" );

				if (null != scfCorpDealerResponse.getStatus().getResponseCode() && scfCorpDealerResponse.getStatus().getResponseCode().trim().length() > 0)
					scfCorpDealerOutStandingObject.addProperty("ResponseCode", scfCorpDealerResponse.getStatus().getResponseCode() );
				else
					scfCorpDealerOutStandingObject.addProperty("ResponseCode", "" );

				if (null != scfCorpDealerResponse.getStatus().getMessage() && scfCorpDealerResponse.getStatus().getMessage().trim().length() > 0)
					scfCorpDealerOutStandingObject.addProperty("Message", scfCorpDealerResponse.getStatus().getMessage() );
				else
					scfCorpDealerOutStandingObject.addProperty("Message", "" );

				scfCorpDealerOutStandingObject.addProperty("ODAccountNumber", "");
				scfCorpDealerOutStandingObject.addProperty("CustomerID", "");
				scfCorpDealerOutStandingObject.addProperty("DealerName", "");
				scfCorpDealerOutStandingObject.addProperty("DealerLimit", "0.00");
				scfCorpDealerOutStandingObject.addProperty("DealerExpiryDate", "");
				scfCorpDealerOutStandingObject.addProperty("PeakLimit", "0.00");
				scfCorpDealerOutStandingObject.addProperty("TODLimit", "0.00");
				scfCorpDealerOutStandingObject.addProperty("NoOfDisbursement", "0.00");
				scfCorpDealerOutStandingObject.addProperty("TotalDisbursementExcludingInterest", "0.00");
				scfCorpDealerOutStandingObject.addProperty("RepaymentAmount", "0.00");
				scfCorpDealerOutStandingObject.addProperty("BalanceOutstanding", "0.00");
				scfCorpDealerOutStandingObject.addProperty("UnutilizedLimit", "0.00");
				scfCorpDealerOutStandingObject.addProperty("NormalDues", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueWithinCure", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueBeyondCure", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueBetween0To7Days", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueBetween7To14Days", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueBetween14To21Days", "0.00");
				scfCorpDealerOutStandingObject.addProperty("OverdueBetween21To28Days", "0.00");	
				scfCorpDealerOutStandingObject.addProperty("OverdueBetween28To60Days", "0.00");	
				scfCorpDealerOutStandingObject.addProperty("OverdueBeyond60Days", "0.00");	
				scfCorpDealerOutStandingObject.addProperty("City", "");	
				scfCorpDealerOutStandingObject.addProperty("IsAccountFrozen", "000001");	
				scfCorpDealerOutStandingObject.addProperty("DealerCode", "");	
				scfCorpDealerOutStandingObject.addProperty("Currency", "");	
				scfCorpDealerOutStandingObject.addProperty("AggregatorID", aggregatorID);
				scfCorpDealerOutStandingObject.addProperty("AggregatorName", aggregatorName);
				if(countOfPipes == 1){
					scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
					scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", "");
				}else if(countOfPipes == 2){
					scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
					scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", parentLimitPrefixId);
				}

				onBoardOtherFacilitiesArray.add(scfCorpDealerOutStandingObject);
				scfCorpDealerOutStandingResponse.add("results", onBoardOtherFacilitiesArray);
			}

		}catch(Exception exception){

			if(debug){
				StackTraceElement element[] = exception.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("executeHttpPost.NamingException:"+buffer.toString());
			}

			// TODO: handle exception
			scfCorpDealerOutStandingObject.addProperty("ResponseCode", "001");
			scfCorpDealerOutStandingObject.addProperty("Message", exception.getLocalizedMessage());
			scfCorpDealerOutStandingObject.addProperty("Status", "000002");

			scfCorpDealerOutStandingObject.addProperty("ODAccountNumber", "");
			scfCorpDealerOutStandingObject.addProperty("CustomerID", "");
			scfCorpDealerOutStandingObject.addProperty("DealerName", "");
			scfCorpDealerOutStandingObject.addProperty("DealerLimit", "0.00");
			scfCorpDealerOutStandingObject.addProperty("DealerExpiryDate", "");
			scfCorpDealerOutStandingObject.addProperty("PeakLimit", "0.00");
			scfCorpDealerOutStandingObject.addProperty("TODLimit", "0.00");
			scfCorpDealerOutStandingObject.addProperty("NoOfDisbursement", "0.00");
			scfCorpDealerOutStandingObject.addProperty("TotalDisbursementExcludingInterest", "0.00");
			scfCorpDealerOutStandingObject.addProperty("RepaymentAmount", "0.00");
			scfCorpDealerOutStandingObject.addProperty("BalanceOutstanding", "0.00");
			scfCorpDealerOutStandingObject.addProperty("UnutilizedLimit", "0.00");
			scfCorpDealerOutStandingObject.addProperty("NormalDues", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueWithinCure", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueBeyondCure", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueBetween0To7Days", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueBetween7To14Days", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueBetween14To21Days", "0.00");
			scfCorpDealerOutStandingObject.addProperty("OverdueBetween21To28Days", "0.00");	
			scfCorpDealerOutStandingObject.addProperty("OverdueBetween28To60Days", "0.00");	
			scfCorpDealerOutStandingObject.addProperty("OverdueBeyond60Days", "0.00");	
			scfCorpDealerOutStandingObject.addProperty("City", "");	
			scfCorpDealerOutStandingObject.addProperty("IsAccountFrozen", "000001");	
			scfCorpDealerOutStandingObject.addProperty("DealerCode", "");	
			scfCorpDealerOutStandingObject.addProperty("Currency", "");	
			
			scfCorpDealerOutStandingObject.addProperty("AggregatorID", aggregatorID);
			scfCorpDealerOutStandingObject.addProperty("AggregatorName", aggregatorName);
			if(countOfPipes == 1){
				scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
				scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", "");
			}else if(countOfPipes == 2){
				scfCorpDealerOutStandingObject.addProperty("ParentLimitID", parentLimitPrefixId);
				scfCorpDealerOutStandingObject.addProperty("ParentLimitPrefixHistory", parentLimitPrefixId);
			}

			onBoardOtherFacilitiesArray.add(scfCorpDealerOutStandingObject);
			scfCorpDealerOutStandingResponse.add("results", onBoardOtherFacilitiesArray);

		}
		return scfCorpDealerOutStandingResponse;
	}
}

package com.arteriatech.bc.ODCorpAccountMISPublish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;

public class MISPublishOnDemandClient {

	public Map<String, String> callMISPublishOnDemandWebservice(String aggregatorID, String input, String report,
			String variantID,HttpServletResponse response,boolean debug) {

		String endPointURL = "", system = "";
		CommonUtils commonUtils = new CommonUtils();
		Map<String, String> misPubMap = new HashMap<String, String>();
		MISPublishOnDemand_RequestRoot misPubObj = new MISPublishOnDemand_RequestRoot();
		SI_MISPublishOnDemand_RequestServiceLocator siMIspLocator=new SI_MISPublishOnDemand_RequestServiceLocator();
		List<MISPublishOnDemand_RequestRoot> root=new ArrayList<>();
		MISPublishOnDemand_RequestRoot[] rootArray=new MISPublishOnDemand_RequestRoot[10];
		try {
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			endPointURL = endPointURL + "MISPublishOnDemand_Request";
			misPubObj.setAggregatorID(aggregatorID);
			misPubObj.setInput(input);
			misPubObj.setReport(report);
			misPubObj.setVariantID(variantID);
			rootArray[0]=misPubObj;
			if (debug) {
				response.getWriter().println("MISPublishOnDemandWebservice-endPointURL: " + endPointURL);
				response.getWriter().println("MISPublishOnDemandWebservice-aggregatorID: " + aggregatorID);
				response.getWriter().println("MISPublishOnDemandWebservice-input: " + input);
				response.getWriter().println("MISPublishOnDemandWebservice-report: " + report);
				response.getWriter().println("MISPublishOnDemandWebservice-report: " + variantID);
			}
			siMIspLocator.setEndpointAddress("SI_MISPublishOnDemand_RequestPort", endPointURL);
			SI_MISPublishOnDemand_Request MISPublishOnDemand_RequestPort = siMIspLocator.getSI_MISPublishOnDemand_RequestPort();
			
			MISPublishOnDemand_RequestPort.SI_MISPublishOnDemand_Request(rootArray);
		} catch (Exception ex) {

		}

		return null;

	}

}

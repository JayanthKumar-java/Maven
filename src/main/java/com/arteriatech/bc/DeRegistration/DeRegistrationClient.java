package com.arteriatech.bc.DeRegistration;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;

public class DeRegistrationClient {

	public Map<String, String> callDeRegistarationWebservice(String corpId, String aggId, String userId, String urn,
			String aggrName, HttpServletResponse response, boolean debug) {

		String endPointURL = "", system = "";
		CommonUtils commonUtils = new CommonUtils();
		Map<String, String> deRegMap = new HashMap<String, String>();
		DT_DeRegistration_Request dtRegReq = new DT_DeRegistration_Request();
		try {
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			endPointURL = endPointURL + "DeRegistration";
			dtRegReq.setAggregatorId(aggId);
			dtRegReq.setAggregatorName(aggrName);
			dtRegReq.setCorpId(corpId);
			dtRegReq.setURN(urn);
			dtRegReq.setUserId(userId);
			if (debug) {
				response.getWriter().println("DeRegistarationWebservice-endPointURL: " + endPointURL);
				response.getWriter().println("DeRegistarationWebservice-AggregatorId: " + aggId);
				response.getWriter().println("DeRegistarationWebservice-AggregatorName: " + aggrName);
				response.getWriter().println("DeRegistarationWebservice-CorpId: " + corpId);
				response.getWriter().println("DeRegistarationWebservice-URN: " + urn);
				response.getWriter().println("DeRegistarationWebservice-UserId: " + userId);

			}

			DeRegistrationServiceLocator deRegLoca = new DeRegistrationServiceLocator();
			deRegLoca.setEndpointAddress("DeRegistrationPort", endPointURL);
			DeRegistration deRegistrationPort = deRegLoca.getDeRegistrationPort();
			DT_DeRegistration_Response deRegcpiRes = deRegistrationPort.deRegistration(dtRegReq);
			if (debug) {
				response.getWriter().println("Response from CPI :" + new Gson().toJson(deRegcpiRes));
			}
			if (null == deRegcpiRes.getStatus() || deRegcpiRes.getStatus().trim().length() == 0
					|| deRegcpiRes.getStatus().equalsIgnoreCase("000002")
					|| deRegcpiRes.getStatus().trim().equalsIgnoreCase("")) {
				deRegMap.put("Error", "059");
				if (deRegcpiRes.getStatus() == null || deRegcpiRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "000002");
				} else {
					deRegMap.put("Status", deRegcpiRes.getStatus());
				}

				if (deRegcpiRes.getAGGRID() == null || deRegcpiRes.getAGGRID().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorId", "");
				} else {
					deRegMap.put("AggregatorId", deRegcpiRes.getAGGRID());

				}

				if (deRegcpiRes.getAGGRNAME() == null || deRegcpiRes.getAGGRNAME().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorName", "");
				} else {
					deRegMap.put("AggregatorName", deRegcpiRes.getAGGRNAME());

				}

				if (deRegcpiRes.getCORPID() == null || deRegcpiRes.getCORPID().equalsIgnoreCase("")) {
					deRegMap.put("cropId", "");
				} else {
					deRegMap.put("cropId", deRegcpiRes.getCORPID());

				}

				if (deRegcpiRes.getURN() == null || deRegcpiRes.getURN().equalsIgnoreCase("")) {
					deRegMap.put("URN", "");
				} else {
					deRegMap.put("URN", deRegcpiRes.getURN());

				}

				if (deRegcpiRes.getUSERID() == null || deRegcpiRes.getUSERID().equalsIgnoreCase("")) {
					deRegMap.put("userId", "");
				} else {
					deRegMap.put("userId", deRegcpiRes.getUSERID());

				}

				if (deRegcpiRes.getErrorCode() == null || deRegcpiRes.getErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("errorCode", "");
				} else {
					deRegMap.put("errorCode", deRegcpiRes.getErrorCode());

				}

				if (deRegcpiRes.getMessage() == null || deRegcpiRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", deRegcpiRes.getMessage());

				}
			} else if (null != deRegcpiRes.getStatus() || deRegcpiRes.getStatus().equalsIgnoreCase("000001")) {
				deRegMap.put("Error", "");
				if (deRegcpiRes.getStatus() == null || deRegcpiRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "");
				} else {
					deRegMap.put("Status", deRegcpiRes.getStatus());
				}

				if (deRegcpiRes.getAGGRID() == null || deRegcpiRes.getAGGRID().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorId", "");
				} else {
					deRegMap.put("AggregatorId", deRegcpiRes.getAGGRID());

				}

				if (deRegcpiRes.getAGGRNAME() == null || deRegcpiRes.getAGGRNAME().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorName", "");
				} else {
					deRegMap.put("AggregatorName", deRegcpiRes.getAGGRNAME());

				}

				if (deRegcpiRes.getCORPID() == null || deRegcpiRes.getCORPID().equalsIgnoreCase("")) {
					deRegMap.put("cropId", "");
				} else {
					deRegMap.put("cropId", deRegcpiRes.getCORPID());

				}

				if (deRegcpiRes.getURN() == null || deRegcpiRes.getURN().equalsIgnoreCase("")) {
					deRegMap.put("URN", "");
				} else {
					deRegMap.put("URN", deRegcpiRes.getURN());

				}

				if (deRegcpiRes.getUSERID() == null || deRegcpiRes.getUSERID().equalsIgnoreCase("")) {
					deRegMap.put("userId", "");
				} else {
					deRegMap.put("userId", deRegcpiRes.getUSERID());

				}

				if (deRegcpiRes.getErrorCode() == null || deRegcpiRes.getErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("errorCode", "");
				} else {
					deRegMap.put("errorCode", deRegcpiRes.getErrorCode());

				}

				if (deRegcpiRes.getMessage() == null || deRegcpiRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", deRegcpiRes.getMessage());
				}
			} else {
				deRegMap.put("Error", "");
				if (deRegcpiRes.getStatus() == null || deRegcpiRes.getStatus().equalsIgnoreCase("")) {
					deRegMap.put("Status", "");
				} else {
					deRegMap.put("Status", deRegcpiRes.getStatus());
				}

				if (deRegcpiRes.getAGGRID() == null || deRegcpiRes.getAGGRID().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorId", "");
				} else {
					deRegMap.put("AggregatorId", deRegcpiRes.getAGGRID());

				}

				if (deRegcpiRes.getAGGRNAME() == null || deRegcpiRes.getAGGRNAME().equalsIgnoreCase("")) {
					deRegMap.put("AggregatorName", "");
				} else {
					deRegMap.put("AggregatorName", deRegcpiRes.getAGGRNAME());

				}

				if (deRegcpiRes.getCORPID() == null || deRegcpiRes.getCORPID().equalsIgnoreCase("")) {
					deRegMap.put("cropId", "");
				} else {
					deRegMap.put("cropId", deRegcpiRes.getCORPID());

				}

				if (deRegcpiRes.getURN() == null || deRegcpiRes.getURN().equalsIgnoreCase("")) {
					deRegMap.put("URN", "");
				} else {
					deRegMap.put("URN", deRegcpiRes.getURN());

				}

				if (deRegcpiRes.getUSERID() == null || deRegcpiRes.getUSERID().equalsIgnoreCase("")) {
					deRegMap.put("userId", "");
				} else {
					deRegMap.put("userId", deRegcpiRes.getUSERID());

				}

				if (deRegcpiRes.getErrorCode() == null || deRegcpiRes.getErrorCode().equalsIgnoreCase("")) {
					deRegMap.put("errorCode", "");
				} else {
					deRegMap.put("errorCode", deRegcpiRes.getErrorCode());

				}

				if (deRegcpiRes.getMessage() == null || deRegcpiRes.getMessage().equalsIgnoreCase("")) {
					deRegMap.put("message", "");
				} else {
					deRegMap.put("message", deRegcpiRes.getMessage());
				}

			}
		} catch (Exception ex) {
			deRegMap.put("Error", "059");
			deRegMap.put("Status", "000002");
			deRegMap.put("errorCode", "");
			deRegMap.put("userId", "");
			deRegMap.put("URN", "");
			deRegMap.put("cropId", "");
			deRegMap.put("AggregatorName", "");
			deRegMap.put("AggregatorId", "");
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

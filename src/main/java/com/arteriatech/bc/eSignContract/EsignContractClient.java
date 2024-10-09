package com.arteriatech.bc.eSignContract;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class EsignContractClient {

	public JSONObject callEsignContractWebservice(HttpServletRequest request, HttpServletResponse response,
			String contractId, boolean debug) {
		String endPointURL = "", system = "";
		CommonUtils commonUtils = new CommonUtils();
		ESignContractRequestRequest esignCOntractClient = new ESignContractRequestRequest();
		ESignContractRequest client = new ESignContractRequest();
		Map<Object, Object> resMap = new HashMap<>();

		JSONObject result = new JSONObject();

		try {
			system = commonUtils.getODataDestinationProperties("System", "BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL", "BankConnect");
			endPointURL = endPointURL + "eSignContract";
			esignCOntractClient.setContractId(contractId);
			client.setRequest(esignCOntractClient);

			if (debug) {
				response.getWriter().println("CPI URL " + endPointURL);
			}
			ESignContract_ServiceLocator serviceLocator = new ESignContract_ServiceLocator();
			serviceLocator.setEndpointAddress("eSignContract", endPointURL);
			ESignContract_PortType esignContractType = serviceLocator.geteSignContract();
			ESignContractResponse eSignContract = esignContractType.eSignContract(client);
			ESignContractResponseResponse cpiResponse = eSignContract.getResponse();
			if (debug) {
				response.getWriter().println("Response from CPI :" + new Gson().toJson(cpiResponse));
			}

			ESignContractResponseResponseError status = cpiResponse.getError();
			ESignContractResponseResponseSignerDetail[] signerDetail = cpiResponse.getSignerDetail();
			String[] customerSupportEmailId = cpiResponse.getCustomerSupportEmailId();
			if (debug) {
				response.getWriter().println("EsignContract webservice Status " + status.getStatus());
				response.getWriter().println("EsignContract webservice Response Code " + status.getResponseCode());
				response.getWriter().println("EsignContract webservice Message " + status.getMessage());
			}
			if (null == status.getStatus() || status.getStatus().trim().length() == 0
					|| status.getStatus().equalsIgnoreCase("000002")
					|| status.getStatus().trim().equalsIgnoreCase("")) {
				result.accumulate("Error", "059");
				if (status.getStatus() == null || status.getStatus().equalsIgnoreCase("")) {
					result.accumulate("Status", "000002");
				} else {
					result.accumulate("Status", status.getStatus());
				}
				if (signerDetail != null && signerDetail.length > 0) {
					result.accumulate("SignerDetails", signerDetail);
				} else {
					result.accumulate("SignerDetails", "");
				}
				if (customerSupportEmailId != null && customerSupportEmailId.length > 0) {
					result.accumulate("CustomerSupportEmailId", customerSupportEmailId);
				} else {
					result.accumulate("CustomerSupportEmailId", "");

				}

				if (cpiResponse.getUserReminderTime() != null
						&& !cpiResponse.getUserReminderTime().equalsIgnoreCase("")) {
					result.accumulate("UserReminderTime", cpiResponse.getUserReminderTime());
				} else {
					result.accumulate("UserReminderTime", "");

				}

				if (cpiResponse.getMaximumValidityTime() != null
						&& !cpiResponse.getMaximumValidityTime().equalsIgnoreCase("")) {
					result.accumulate("MaximumValidityTime", cpiResponse.getMaximumValidityTime());
				} else {
					result.accumulate("MaximumValidityTime", "");

				}

				if (cpiResponse.getAdminReminderTime() != null
						&& !cpiResponse.getAdminReminderTime().equalsIgnoreCase("")) {
					result.accumulate("AdminReminderTime", cpiResponse.getAdminReminderTime());
				} else {
					result.accumulate("AdminReminderTime", "");

				}

				if (cpiResponse.getContractCreatedOn() != null
						&& !cpiResponse.getContractCreatedOn().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedOn", cpiResponse.getContractCreatedOn());
				} else {
					result.accumulate("ContractCreatedOn", "");

				}

				if (cpiResponse.getContractCreatedAt() != null
						&& !cpiResponse.getContractCreatedAt().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedAt", cpiResponse.getContractCreatedAt());
				} else {
					result.accumulate("ContractCreatedAt", "");

				}

				if (cpiResponse.getContractCreatedAt() != null
						&& !cpiResponse.getContractCreatedAt().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedAt", cpiResponse.getContractCreatedAt());
				} else {
					result.accumulate("ContractCreatedAt", "");

				}

				if (cpiResponse.getContractCompletionTime() != null
						&& !cpiResponse.getContractCompletionTime().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime", cpiResponse.getContractCompletionTime());
				} else {
					result.accumulate("ContractCompletionTime", "");

				}

				if (cpiResponse.getCallbackURL() != null && !cpiResponse.getCallbackURL().equalsIgnoreCase("")) {
					result.accumulate("CallbackURL", cpiResponse.getCallbackURL());
				} else {
					result.accumulate("CallbackURL", "");

				}

				if (cpiResponse.getInitialContractFile() != null
						&& !cpiResponse.getInitialContractFile().equalsIgnoreCase("")) {
					result.accumulate("InitialContractFile", cpiResponse.getInitialContractFile());
				} else {
					result.accumulate("InitialContractFile", "");

				}

				if (cpiResponse.getIsCompleted() != null && !cpiResponse.getIsCompleted().equalsIgnoreCase("")) {
					result.accumulate("IsCompleted", cpiResponse.getIsCompleted());
				} else {
					result.accumulate("IsCompleted", "");

				}

				if (cpiResponse.getContractCompletionTime2() != null
						&& !cpiResponse.getContractCompletionTime2().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime2", cpiResponse.getContractCompletionTime2());
				} else {
					result.accumulate("ContractCompletionTime2", "");

				}

				if (cpiResponse.getFinalSignedContractFile() != null
						&& !cpiResponse.getFinalSignedContractFile().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractFile", cpiResponse.getFinalSignedContractFile());
				} else {
					result.accumulate("FinalSignedContractFile", "");

				}

				if (cpiResponse.getFinalSignedContractURL() != null
						&& !cpiResponse.getFinalSignedContractURL().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractURL", cpiResponse.getFinalSignedContractURL());
				} else {
					result.accumulate("FinalSignedContractURL", "");

				}

				if (cpiResponse.getAuditCertificate() != null
						&& !cpiResponse.getAuditCertificate().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificate", cpiResponse.getAuditCertificate());
				} else {
					result.accumulate("AuditCertificate", "");

				}

				if (cpiResponse.getAuditCertificateURL() != null
						&& !cpiResponse.getAuditCertificateURL().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificateURL", cpiResponse.getAuditCertificateURL());
				} else {
					result.accumulate("AuditCertificateURL", "");

				}

				if (cpiResponse.getEmailAcceptance() != null
						&& !cpiResponse.getEmailAcceptance().equalsIgnoreCase("")) {
					result.accumulate("EmailAcceptance", cpiResponse.getEmailAcceptance());
				} else {
					result.accumulate("EmailAcceptance", "");

				}

				if (cpiResponse.getMergedUserConsentURL() != null
						&& !cpiResponse.getMergedUserConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserConsentURL", cpiResponse.getMergedUserConsentURL());
				} else {
					result.accumulate("MergedUserConsentURL", "");
				}

				if (cpiResponse.getMergedUserSignedConsentURL() != null
						&& !cpiResponse.getMergedUserSignedConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserSignedConsentURL", cpiResponse.getMergedUserSignedConsentURL());
				} else {
					result.accumulate("MergedUserSignedConsentURL", "");
				}

			} else if (null != status.getStatus() || status.getStatus().equalsIgnoreCase("000001")) {
				result.accumulate("Error", "");
				if (status.getStatus() == null || status.getStatus().equalsIgnoreCase("")) {
					result.accumulate("Status", "");
				} else {
					result.accumulate("Status", status.getStatus());
				}

				if (cpiResponse.getUserReminderTime() != null
						&& !cpiResponse.getUserReminderTime().equalsIgnoreCase("")) {
					result.accumulate("UserReminderTime", cpiResponse.getUserReminderTime());
				} else {
					result.accumulate("UserReminderTime", "");

				}

				if (cpiResponse.getMaximumValidityTime() != null
						&& !cpiResponse.getMaximumValidityTime().equalsIgnoreCase("")) {
					result.accumulate("MaximumValidityTime", cpiResponse.getMaximumValidityTime());
				} else {
					result.accumulate("MaximumValidityTime", "");

				}

				if (cpiResponse.getAdminReminderTime() != null
						&& !cpiResponse.getAdminReminderTime().equalsIgnoreCase("")) {
					result.accumulate("AdminReminderTime", cpiResponse.getAdminReminderTime());
				} else {
					result.accumulate("AdminReminderTime", "");

				}

				if (cpiResponse.getContractCreatedOn() != null
						&& !cpiResponse.getContractCreatedOn().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedOn", cpiResponse.getContractCreatedOn());
				} else {
					result.accumulate("ContractCreatedOn", "");

				}

				if (customerSupportEmailId != null && customerSupportEmailId.length > 0) {
					result.accumulate("CustomerSupportEmailId", customerSupportEmailId);
				} else {
					result.accumulate("CustomerSupportEmailId", "");

				}

				if (signerDetail != null && signerDetail.length > 0) {
					result.accumulate("SignerDetails", signerDetail);
				} else {
					result.accumulate("SignerDetails", "");
				}

				if (cpiResponse.getContractCreatedAt() != null
						&& !cpiResponse.getContractCreatedAt().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedAt", cpiResponse.getContractCreatedAt());
				} else {
					result.accumulate("ContractCreatedAt", "");

				}

				if (cpiResponse.getContractCompletionTime() != null
						&& !cpiResponse.getContractCompletionTime().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime", cpiResponse.getContractCompletionTime());
				} else {
					result.accumulate("ContractCompletionTime", "");

				}

				if (cpiResponse.getCallbackURL() != null && !cpiResponse.getCallbackURL().equalsIgnoreCase("")) {
					result.accumulate("CallbackURL", cpiResponse.getCallbackURL());
				} else {
					result.accumulate("CallbackURL", "");

				}

				if (cpiResponse.getInitialContractFile() != null
						&& !cpiResponse.getInitialContractFile().equalsIgnoreCase("")) {
					result.accumulate("InitialContractFile", cpiResponse.getInitialContractFile());
				} else {
					result.accumulate("InitialContractFile", "");

				}

				if (cpiResponse.getIsCompleted() != null && !cpiResponse.getIsCompleted().equalsIgnoreCase("")) {
					result.accumulate("IsCompleted", cpiResponse.getIsCompleted());
				} else {
					result.accumulate("IsCompleted", "");

				}

				if (cpiResponse.getContractCompletionTime2() != null
						&& !cpiResponse.getContractCompletionTime2().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime2", cpiResponse.getContractCompletionTime2());
				} else {
					result.accumulate("ContractCompletionTime2", "");

				}

				if (cpiResponse.getFinalSignedContractFile() != null
						&& !cpiResponse.getFinalSignedContractFile().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractFile", cpiResponse.getFinalSignedContractFile());
				} else {
					result.accumulate("FinalSignedContractFile", "");

				}

				if (cpiResponse.getFinalSignedContractURL() != null
						&& !cpiResponse.getFinalSignedContractURL().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractURL", cpiResponse.getFinalSignedContractURL());
				} else {
					result.accumulate("FinalSignedContractURL", "");

				}

				if (cpiResponse.getAuditCertificate() != null
						&& !cpiResponse.getAuditCertificate().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificate", cpiResponse.getAuditCertificate());
				} else {
					result.accumulate("AuditCertificate", "");

				}

				if (cpiResponse.getAuditCertificateURL() != null
						&& !cpiResponse.getAuditCertificateURL().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificateURL", cpiResponse.getAuditCertificateURL());
				} else {
					result.accumulate("AuditCertificateURL", "");

				}

				if (cpiResponse.getEmailAcceptance() != null
						&& !cpiResponse.getEmailAcceptance().equalsIgnoreCase("")) {
					result.accumulate("EmailAcceptance", cpiResponse.getEmailAcceptance());
				} else {
					result.accumulate("EmailAcceptance", "");

				}

				if (cpiResponse.getMergedUserConsentURL() != null
						&& !cpiResponse.getMergedUserConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserConsentURL", cpiResponse.getMergedUserConsentURL());
				} else {
					result.accumulate("MergedUserConsentURL", "");
				}

				if (cpiResponse.getMergedUserSignedConsentURL() != null
						&& !cpiResponse.getMergedUserSignedConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserSignedConsentURL", cpiResponse.getMergedUserSignedConsentURL());
				} else {
					result.accumulate("MergedUserSignedConsentURL", "");
				}

			} else {
				result.accumulate("Error", "");
				if (status.getStatus() == null || status.getStatus().equalsIgnoreCase("")) {
					result.accumulate("Status", "");
				} else {
					result.accumulate("Status", status.getStatus());
				}

				if (cpiResponse.getUserReminderTime() != null
						&& !cpiResponse.getUserReminderTime().equalsIgnoreCase("")) {
					result.accumulate("UserReminderTime", cpiResponse.getUserReminderTime());
				} else {
					result.accumulate("UserReminderTime", "");

				}

				if (cpiResponse.getMaximumValidityTime() != null
						&& !cpiResponse.getMaximumValidityTime().equalsIgnoreCase("")) {
					result.accumulate("MaximumValidityTime", cpiResponse.getMaximumValidityTime());
				} else {
					result.accumulate("MaximumValidityTime", "");

				}

				if (cpiResponse.getAdminReminderTime() != null
						&& !cpiResponse.getAdminReminderTime().equalsIgnoreCase("")) {
					result.accumulate("AdminReminderTime", cpiResponse.getAdminReminderTime());
				} else {
					result.accumulate("AdminReminderTime", "");

				}

				if (cpiResponse.getContractCreatedOn() != null
						&& !cpiResponse.getContractCreatedOn().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedOn", cpiResponse.getContractCreatedOn());
				} else {
					result.accumulate("ContractCreatedOn", "");

				}

				if (cpiResponse.getContractCreatedAt() != null
						&& !cpiResponse.getContractCreatedAt().equalsIgnoreCase("")) {
					result.accumulate("ContractCreatedAt", cpiResponse.getContractCreatedAt());
				} else {
					result.accumulate("ContractCreatedAt", "");

				}

				if (customerSupportEmailId != null && customerSupportEmailId.length > 0) {
					result.accumulate("CustomerSupportEmailId", customerSupportEmailId);
				} else {
					result.accumulate("CustomerSupportEmailId", "");

				}

				if (signerDetail != null && signerDetail.length > 0) {
					result.accumulate("SignerDetails", signerDetail);
				} else {
					result.accumulate("SignerDetails", "");
				}

				if (cpiResponse.getContractCompletionTime() != null
						&& !cpiResponse.getContractCompletionTime().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime", cpiResponse.getContractCompletionTime());
				} else {
					result.accumulate("ContractCompletionTime", "");

				}

				if (cpiResponse.getCallbackURL() != null && !cpiResponse.getCallbackURL().equalsIgnoreCase("")) {
					result.accumulate("CallbackURL", cpiResponse.getCallbackURL());
				} else {
					result.accumulate("CallbackURL", "");

				}

				if (cpiResponse.getInitialContractFile() != null
						&& !cpiResponse.getInitialContractFile().equalsIgnoreCase("")) {
					result.accumulate("InitialContractFile", cpiResponse.getInitialContractFile());
				} else {
					result.accumulate("InitialContractFile", "");

				}

				if (cpiResponse.getIsCompleted() != null && !cpiResponse.getIsCompleted().equalsIgnoreCase("")) {
					result.accumulate("IsCompleted", cpiResponse.getIsCompleted());
				} else {
					result.accumulate("IsCompleted", "");

				}

				if (cpiResponse.getContractCompletionTime2() != null
						&& !cpiResponse.getContractCompletionTime2().equalsIgnoreCase("")) {
					result.accumulate("ContractCompletionTime2", cpiResponse.getContractCompletionTime2());
				} else {
					result.accumulate("ContractCompletionTime2", "");

				}

				if (cpiResponse.getFinalSignedContractFile() != null
						&& !cpiResponse.getFinalSignedContractFile().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractFile", cpiResponse.getFinalSignedContractFile());
				} else {
					result.accumulate("FinalSignedContractFile", "");

				}

				if (cpiResponse.getFinalSignedContractURL() != null
						&& !cpiResponse.getFinalSignedContractURL().equalsIgnoreCase("")) {
					result.accumulate("FinalSignedContractURL", cpiResponse.getFinalSignedContractURL());
				} else {
					result.accumulate("FinalSignedContractURL", "");

				}

				if (cpiResponse.getAuditCertificate() != null
						&& !cpiResponse.getAuditCertificate().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificate", cpiResponse.getAuditCertificate());
				} else {
					result.accumulate("AuditCertificate", "");

				}

				if (cpiResponse.getAuditCertificateURL() != null
						&& !cpiResponse.getAuditCertificateURL().equalsIgnoreCase("")) {
					result.accumulate("AuditCertificateURL", cpiResponse.getAuditCertificateURL());
				} else {
					result.accumulate("AuditCertificateURL", "");

				}

				if (cpiResponse.getEmailAcceptance() != null
						&& !cpiResponse.getEmailAcceptance().equalsIgnoreCase("")) {
					result.accumulate("EmailAcceptance", cpiResponse.getEmailAcceptance());
				} else {
					result.accumulate("EmailAcceptance", "");

				}

				if (cpiResponse.getMergedUserConsentURL() != null
						&& !cpiResponse.getMergedUserConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserConsentURL", cpiResponse.getMergedUserConsentURL());
				} else {
					result.accumulate("MergedUserConsentURL", "");
				}

				if (cpiResponse.getMergedUserSignedConsentURL() != null
						&& !cpiResponse.getMergedUserSignedConsentURL().equalsIgnoreCase("")) {
					result.accumulate("MergedUserSignedConsentURL", cpiResponse.getMergedUserSignedConsentURL());
				} else {
					result.accumulate("MergedUserSignedConsentURL", "");
				}

			}

		} catch (Exception ex) {
			try {
				result.accumulate("Error", "059");
				result.accumulate("Status", "000002");
				result.accumulate("UserReminderTime", "");
				result.accumulate("MaximumValidityTime", "");
				result.accumulate("AdminReminderTime", "");
				result.accumulate("ContractCreatedOn", "");
				result.accumulate("ContractCreatedAt", "");
				result.accumulate("MergedUserConsentURL", "");
				result.accumulate("ContractCompletionTime", "");
				result.accumulate("CallbackURL", "");
				result.accumulate("InitialContractFile", "");
				result.accumulate("IsCompleted", "");
				result.accumulate("ContractCompletionTime2", "");
				result.accumulate("FinalSignedContractFile", "");
				result.accumulate("FinalSignedContractURL", "");
				result.accumulate("AuditCertificate", "");
				result.accumulate("AuditCertificateURL", "");
				result.accumulate("EmailAcceptance", "");
				result.accumulate("MergedUserConsentURL", "");
				result.accumulate("MergedUserSignedConsentURL", "");
				result.accumulate("CustomerSupportEmailId", "");
				result.accumulate("SignerDetails", "");
			} catch (JSONException exc) {
				return result;

			}

		}

		return result;

	}

}

package com.arteriatech.bc.UserRegistration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;

public class UserRegistrationClient {
	public Map<String, String> callUserRegWS(HttpServletRequest request, HttpServletResponse response, JSONObject inputJsonObject, String userRegID, String corpID, String aggregatorID, boolean debug) throws IOException{
		Map<String, String> userRegResponseMap = new HashMap<String, String>();
		UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest();
		CommonUtils commonUtils = new CommonUtils();
		
		String system = "", endPointURL = "";
		try{
			system = commonUtils.getODataDestinationProperties("System","BankConnect");
			endPointURL = commonUtils.getODataDestinationProperties("URL","BankConnect");
			endPointURL = endPointURL+"UserRegistration";
			
			if(debug){
				response.getWriter().println("callUserRegWS.aggregatorID: "+aggregatorID);
				response.getWriter().println("callUserRegWS.AliasID: "+inputJsonObject.getString("AliasID"));
				response.getWriter().println("callUserRegWS.CorpId: "+inputJsonObject.getString("CorpId"));
				response.getWriter().println("callUserRegWS.UserId: "+inputJsonObject.getString("UserId"));
			}
			
			userRegistrationRequest.setAggregatorID(aggregatorID);
			
			if((inputJsonObject.getString("AliasID") != null && inputJsonObject.getString("AliasID").trim().length() > 0)){
				userRegistrationRequest.setAliasID(inputJsonObject.getString("AliasID"));
			}else{
				userRegistrationRequest.setAliasID("");
			}
			
			userRegistrationRequest.setCorporateID(inputJsonObject.getString("CorpId"));
			userRegistrationRequest.setUserID(inputJsonObject.getString("UserId"));
			userRegistrationRequest.setUserRegistrationID(userRegID);
			
			UserRegistrationServiceLocator userRegLocator = new UserRegistrationServiceLocator();
			userRegLocator.setEndpointAddress("UserRegistrationPort", endPointURL);
			UserRegistration service = userRegLocator.getUserRegistrationPort();
			
			UserRegistrationResponse userRegResponse = service.userRegistration(userRegistrationRequest);
			
			if(debug){
				response.getWriter().println("callUserRegWS.getMessage: "+userRegResponse.getMessage());
				response.getWriter().println("callUserRegWS.getErrorCode: "+userRegResponse.getErrorCode());
				response.getWriter().println("callUserRegWS.getRequestID: "+userRegResponse.getRequestID());
				response.getWriter().println("callUserRegWS.getStatus: "+userRegResponse.getStatus());
			}
			
			if(null != userRegResponse.getStatus() && userRegResponse.getStatus().trim().length() > 0){
				if(userRegResponse.getStatus().trim().equalsIgnoreCase("000001")){
					if(null != userRegResponse.getErrorCode() && userRegResponse.getErrorCode().trim().length() > 0)
						userRegResponseMap.put("ErrorCode", userRegResponse.getErrorCode());
					else
						userRegResponseMap.put("ErrorCode", "");
					
					if(null != userRegResponse.getMessage() && userRegResponse.getMessage().trim().length() > 0)
						userRegResponseMap.put("Message", userRegResponse.getMessage());
					else
						userRegResponseMap.put("Message", "");
					
					if(null != userRegResponse.getRequestID() && userRegResponse.getRequestID().trim().length() > 0)
						userRegResponseMap.put("RequestID", userRegResponse.getRequestID());
					else
						userRegResponseMap.put("RequestID", "");
					
					if(null != userRegResponse.getStatus() && userRegResponse.getStatus().trim().length() > 0)
						userRegResponseMap.put("Status", userRegResponse.getStatus());
					else
						userRegResponseMap.put("Status", "");
				}else{
					if(null != userRegResponse.getErrorCode() && userRegResponse.getErrorCode().trim().length() > 0)
						userRegResponseMap.put("ErrorCode", userRegResponse.getErrorCode());
					else
						userRegResponseMap.put("ErrorCode", "");
					
					if(null != userRegResponse.getMessage() && userRegResponse.getMessage().trim().length() > 0)
						userRegResponseMap.put("Message", userRegResponse.getMessage());
					else
						userRegResponseMap.put("Message", "");
					
					if(null != userRegResponse.getRequestID() && userRegResponse.getRequestID().trim().length() > 0)
						userRegResponseMap.put("RequestID", userRegResponse.getRequestID());
					else
						userRegResponseMap.put("RequestID", "");
					
					if(null != userRegResponse.getStatus() && userRegResponse.getStatus().trim().length() > 0)
						userRegResponseMap.put("Status", userRegResponse.getStatus());
					else
						userRegResponseMap.put("Status", "");
				}
			}
		}catch (Exception e) {
			userRegResponseMap.put("ErrorCode", "027");
			userRegResponseMap.put("Message", e.getLocalizedMessage());
			userRegResponseMap.put("RequestID", "");
			userRegResponseMap.put("Status", "000002");
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception-UserRegistrationClient: "+e.getMessage()+"---> Full Stack Trace: "+buffer.toString());
			}
		}
		
		if(debug){
			for (String key : userRegResponseMap.keySet()) {
				response.getWriter().println("UserRegistrationClient-userRegWSMap: "+key + " - " + userRegResponseMap.get(key));
			}
		}
		
		return userRegResponseMap;
	}
}

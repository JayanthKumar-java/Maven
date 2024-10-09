package com.arteriatech.support;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;

public class AccountDetails extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject resObj=new JsonObject();
		CommonUtils commonUtils=new CommonUtils();
		boolean debug=false;
		Properties properties=new Properties();
		try{
			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;
			}
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			if(request.getParameter("AccountNumber")!=null && !request.getParameter("AccountNumber").equalsIgnoreCase("")){
				String accNo=request.getParameter("AccountNumber");
				final String aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID",
						DestinationUtils.PYGWHANA);
				if(debug){
					response.getWriter().println("input Account number:"+accNo);
					response.getWriter().println("AggregatorID:"+aggregatorID);
				}
				
				JsonObject accuntDetailsRes = commonUtils.callAccountDetailsAPI(response, properties,
						aggregatorID, accNo, debug);
				if(debug){
					response.getWriter().println("accuntDetailsRes:"+accuntDetailsRes);
				}
				if (accuntDetailsRes.get("Status").getAsString().equalsIgnoreCase("000001")) {
					//accuntDetailsRes = accuntDetailsRes.get("API_Response").getAsJsonObject();
					//accuntDetailsRes = commonUtils.checkSpecialChar(accuntDetailsRes);
					if(debug){
						response.getWriter().println("After removal of special character account api response:"+accuntDetailsRes);
					}
					response.getWriter().println(accuntDetailsRes);
				}else{
				response.getWriter().println(accuntDetailsRes);
				}
			}else{
				resObj.addProperty("Message", "AccountNumber missing in the input Payload");
				resObj.addProperty("ErrorCode", "J002");
				resObj.addProperty("Status","000002");
				response.getWriter().println(resObj);
			}
		
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			resObj.addProperty("Message", ""+ex.getLocalizedMessage());
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("ExceptionTrace",buffer.toString());
			resObj.addProperty("Status","000002");
			
			response.getWriter().println(resObj);
			
			
		}
	}
	
	

}

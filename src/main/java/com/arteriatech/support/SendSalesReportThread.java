package com.arteriatech.support;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.sap.cloud.account.TenantContext;

public class SendSalesReportThread implements Runnable {
	String pugOdataUrl = "";
	String pugUseruseName = "";
	String pugUserpassword = "";
	String pugUserPass = "";
	String pcgoDataUrl = "";
	String pcgUserName = "";
	String pcgPassword = "";
	String agrgtrID = "";
	String pcgUserPass = "";
	String ssgwODataUrl = "";
	String ssgwUserName = "";
	String ssgwPassword = "";
	String ssgwUserPass = "";
	String ssmisODataUrl = "";
	String ssmisUserName = "";
	String ssmisPassword = "";
	String ssmisUserPass = "";
	String accountID = "";
	String userName = "";
	String passWord = "";
	Properties properties=new Properties();
	JsonObject jsonPayload=new JsonObject();
	

	public SendSalesReportThread(Properties properties,JsonObject jsonPayload) {
		CommonUtils commonUtils = new CommonUtils();
		pugOdataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PUGWHANA);
		pugUseruseName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PUGWHANA);
		pugUserpassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PUGWHANA);
		pugUserPass = pugUseruseName + ":" + pugUserpassword;
		pcgoDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.PCGWHANA);
		pcgUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.PCGWHANA);
		pcgPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PCGWHANA);
		agrgtrID = commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
		pcgUserPass = pcgUserName + ":" + pcgPassword;
		ssgwODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSGWHANA);
		ssgwUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSGWHANA);
		ssgwPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSGWHANA);
		ssgwUserPass = ssgwUserName + ":" + ssgwPassword;
		ssmisODataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SSMISHANA);
		ssmisUserName = commonUtils.getODataDestinationProperties("User", DestinationUtils.SSMISHANA);
		ssmisPassword = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SSMISHANA);
		ssmisUserPass = ssmisUserName + ":" + ssmisPassword;
		userName = commonUtils.getODataDestinationProperties("emailid", DestinationUtils.PLATFORM_EMAIL);
		passWord = commonUtils.getODataDestinationProperties("Password", DestinationUtils.PLATFORM_EMAIL);
		this.properties=properties;
		this.jsonPayload=jsonPayload;
		try {
			Context tenCtx = new InitialContext();
			TenantContext tenantContext = (TenantContext) tenCtx.lookup("java:comp/env/TenantContext");
			accountID = tenantContext.getTenant().getAccount().getId();
		} catch (NamingException e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			throw new RuntimeException(resObj.toString());
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", buffer.toString());
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			throw new RuntimeException(resObj.toString());
		}
	}

	@Override
	public void run() {
		
		
	}

}

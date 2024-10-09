package com.arteriatech.hana;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.AccountBalance.AccountBalanceClient;
import com.arteriatech.bc.SCFAccount.SCFAccountClient;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class AccountBalance
 */
@WebServlet("/AccountBalance")
public class AccountBalance extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountBalance() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String accnNo ="", message="", scfSanctionLimit="";
		boolean debug = false;
		JsonObject rootJson = new JsonObject();
		JsonObject accountBalanceJsonObj = new JsonObject();
		Map<String, String> accountBalanceObjMap = new HashMap<String, String>();
		AccountBalanceClient accountBalance = new AccountBalanceClient();
		BigDecimal availableBalance = new BigDecimal(0.00);
		BigDecimal sanctionLimitAmt = new BigDecimal(0.00);
		
		SCFAccountClient scfAccountClient = new SCFAccountClient();
		Map<String, String> scfAccountObjMap = new HashMap<String, String>();
		
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			accnNo = request.getParameter("AccountNo");
			if (null != accnNo && accnNo.trim().length()>0){
//				accountBalanceObjMap = accountBalance.callAccountBalance(request, response, accnNo, debug);
				if(debug){
					response.getWriter().println("accnNo: "+accnNo+".");
					response.getWriter().println("accnNo: "+accnNo.trim()+".");
				}
				
				accountBalanceObjMap = accountBalance.callAccountBalance(accnNo.trim(), "", "", "", "", debug);
				//Pass expiry date from here and send in output
				if(debug){
					for (String key : accountBalanceObjMap.keySet()) {
						if(debug)
							response.getWriter().println("AccountBalance-accountBalanceObjMap: "+key + " - " + accountBalanceObjMap.get(key));
					}
				}
				
				//Call SCFAccounts to get Sanction Limit
				scfAccountObjMap = scfAccountClient.callSCFAccountClient(accnNo.trim(), debug);
				if(debug){
					for (String key : scfAccountObjMap.keySet()) {
						if(debug)
							response.getWriter().println("SCFAccounts-scfAccountObjMap: "+key + " - " + scfAccountObjMap.get(key));
					}
				}
				if (null != scfAccountObjMap.get("Status") && scfAccountObjMap.get("Status").equalsIgnoreCase("000001")) {
					try {
						sanctionLimitAmt = new BigDecimal(scfAccountObjMap.get("SanctionLimit"));
						scfSanctionLimit = sanctionLimitAmt.toString();
					} catch (Exception e) {
						scfSanctionLimit ="0.00";
					}
				}else{
					try {
						sanctionLimitAmt = new BigDecimal(scfAccountObjMap.get("SanctionLimit"));
						scfSanctionLimit = sanctionLimitAmt.toString();
					} catch (Exception e) {
						scfSanctionLimit ="0.00";
					}
				}
				
				
				if(accountBalanceObjMap.get("Status").equalsIgnoreCase("000001")){
//					availableBalance = new BigDecimal(1000000.00);
					
					accountBalanceJsonObj.addProperty("ErrorCode", accountBalanceObjMap.get("ErrorCode"));
					accountBalanceJsonObj.addProperty("Message", accountBalanceObjMap.get("Message"));
					accountBalanceJsonObj.addProperty("Status", accountBalanceObjMap.get("Status"));
					accountBalanceJsonObj.addProperty("AsOn", accountBalanceObjMap.get("AsOn"));
					accountBalanceJsonObj.addProperty("Amount", accountBalanceObjMap.get("Amount"));
					accountBalanceJsonObj.addProperty("Currency", accountBalanceObjMap.get("Currency"));
					accountBalanceJsonObj.addProperty("FreezeStatus", accountBalanceObjMap.get("FreezeStatus"));
					accountBalanceJsonObj.addProperty("ExpiryDate", accountBalanceObjMap.get("ExpiryDate"));
					accountBalanceJsonObj.addProperty("SanctionLimit", scfSanctionLimit);
					
//					accountBalanceResponse.addProperty("ActualBalance", availableBalance);
					message = getAvailableBalance(request, response, accountBalanceJsonObj, accnNo.trim(), scfSanctionLimit, debug);
					if(debug){
						response.getWriter().println("message: "+message);
					}
					if(! message.equalsIgnoreCase("001") && !message.equalsIgnoreCase("E179")){
						accountBalanceJsonObj.addProperty("ActualBalance", message);
						
					}else{
//						String errorCode = message;
//						message = "";
						if(debug){
							response.getWriter().println("message1: "+message);
							response.getWriter().println("messagevalue: "+properties.getProperty(message));
						}
						accountBalanceJsonObj.addProperty("ErrorCode", message);
						accountBalanceJsonObj.addProperty("Status", "000002");
						accountBalanceJsonObj.addProperty("Message", properties.getProperty(message));
						accountBalanceJsonObj.addProperty("ActualBalance", ""+availableBalance);
//						rootJson.add("Root", accountBalanceJsonObj);
//						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
					rootJson.add("Root", accountBalanceJsonObj);
					response.setStatus(HttpServletResponse.SC_OK);
					response.getWriter().println(rootJson);
				}else{
					accountBalanceJsonObj.addProperty("ErrorCode", accountBalanceObjMap.get("ErrorCode"));
					accountBalanceJsonObj.addProperty("Message", accountBalanceObjMap.get("Message"));
					accountBalanceJsonObj.addProperty("Status", accountBalanceObjMap.get("Status"));
					accountBalanceJsonObj.addProperty("AsOn", accountBalanceObjMap.get("AsOn"));
					accountBalanceJsonObj.addProperty("Amount", accountBalanceObjMap.get("Amount"));
					accountBalanceJsonObj.addProperty("Currency", accountBalanceObjMap.get("Currency"));
					accountBalanceJsonObj.addProperty("FreezeStatus", accountBalanceObjMap.get("FreezeStatus"));
					accountBalanceJsonObj.addProperty("ExpiryDate", accountBalanceObjMap.get("ExpiryDate"));
					accountBalanceJsonObj.addProperty("ActualBalance", ""+availableBalance);
					accountBalanceJsonObj.addProperty("SanctionLimit", scfSanctionLimit);
					rootJson.add("Root", accountBalanceJsonObj);
					response.setStatus(HttpServletResponse.SC_OK);
					response.getWriter().println(rootJson);
				}
			} else {
				message ="Account Number is missing in the request";
				accountBalanceJsonObj.addProperty("ErrorCode", "002");
				accountBalanceJsonObj.addProperty("Status", "000002");
				accountBalanceJsonObj.addProperty("Message", message);
				accountBalanceJsonObj.addProperty("FreezeStatus", "");
				accountBalanceJsonObj.addProperty("ExpiryDate", "");
				accountBalanceJsonObj.addProperty("Currency", "");
				accountBalanceJsonObj.addProperty("Amount", "");
				accountBalanceJsonObj.addProperty("AsOn", "");
				accountBalanceJsonObj.addProperty("ActualBalance", ""+availableBalance);
				accountBalanceJsonObj.addProperty("SanctionLimit", scfSanctionLimit);
				rootJson.add("Root", accountBalanceJsonObj);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(rootJson);
			}
		}catch (Exception e) {
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in doGet: "+buffer.toString());
			}
			message = e.getLocalizedMessage();
			accountBalanceJsonObj.addProperty("Message", message);
			accountBalanceJsonObj.addProperty("ErrorCode", "001");
			accountBalanceJsonObj.addProperty("Status", "000002");
			accountBalanceJsonObj.addProperty("FreezeStatus", "");
			accountBalanceJsonObj.addProperty("Currency", accountBalanceObjMap.get("Currency"));
			accountBalanceJsonObj.addProperty("Amount", "");
			accountBalanceJsonObj.addProperty("AsOn", "");
			accountBalanceJsonObj.addProperty("ActualBalance", ""+availableBalance);
			accountBalanceJsonObj.addProperty("SanctionLimit", scfSanctionLimit);
			rootJson.add("Root", accountBalanceJsonObj);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(rootJson);
		}
	}
	
	public String getAvailableBalance(HttpServletRequest request, HttpServletResponse response, JsonObject accountBalanceJsonObj, 
			String accnNo, String scfSanctionLimit, boolean debug) throws IOException{
		CommonUtils commonUtils = new CommonUtils();
		BigDecimal availableBalance = new BigDecimal(0.00);
		String returnMessage = "", executeURL = "", oDataUrl="", userName="", password="", userPass="", aggregatorID="", statusID="", peakType="", primaryType="";
		int scfSize = 0, peakLimitSize = 0, primaryLimitSize=0;
		String enhPeakLimit="", enhPrimaryLimit="", peakLimitValidFrom="", peakLimitValidTo="";
		long validFromMillis=0, validToMillis=0;
		
		JsonObject scfObj = new JsonObject();
		JsonArray scfArray = new JsonArray();
		
		JsonObject peakLimitObject = new JsonObject();
		JsonArray peakLimitArray = new JsonArray();
		
		JsonObject primaryLimitObject = new JsonObject();
		JsonArray primaryLimitArray = new JsonArray();
		
		JsonObject calculationObject = new JsonObject();
		try{
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			statusID = "000002";
			peakType = "000020";
			primaryType = "000010";
			
			//Get SupplyChainFinances record for the Account Number to get the SanctionLimit (Offer Amount)
			executeURL = oDataUrl+"SupplyChainFinances?$filter=AccountNo%20eq%20%27"+accnNo+"%27%20and%20StatusID%20eq%20%27"+statusID+"%27";
			if(debug)
				response.getWriter().println("getAvailableBalance.executeURL: "+executeURL);
			
			scfObj = commonUtils.executeURL(executeURL, userPass, response);
			scfArray = scfObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
			scfSize = scfArray.size();
			
			if(debug){
				response.getWriter().println("getAvailableBalance.scfObj: "+scfObj);
				response.getWriter().println("getAvailableBalance.scfArray: "+scfArray);
				response.getWriter().println("getAvailableBalance.scfSize: "+scfSize);
			}
			
			if(scfSize == 0){
				returnMessage = "E179";
				if(debug)
					response.getWriter().println("getAvailableBalance.returnMessage1: "+returnMessage);
				return returnMessage;
			}else{
				
				/*scfAccountWSMap = scfAccountClient.callSCFAccountClient(accnNo, debug);
				scfSanctionLimit = new BigDecimal(scfAccountObjMap.get("SanctionLimit"));*/
				
				if(debug)
					response.getWriter().println("sanctionLimit: "+scfSanctionLimit);

				//Check Whether Active Peak Limit is available or not
				executeURL = "";
				executeURL = oDataUrl+"SCFEnhancementLimits?$filter=AccountNo%20eq%20%27"+accnNo+"%27%20and%20EnhancementType%20eq%20%27"+peakType+"%27";
				if(debug)
					response.getWriter().println("getAvailableBalance.enhPeakLimit (Active Peak Available)executeURL: "+executeURL);
				
				peakLimitObject = commonUtils.executeURL(executeURL, userPass, response);
				peakLimitArray = peakLimitObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
				peakLimitSize = peakLimitArray.size();
				
				if(debug){
					response.getWriter().println("getAvailableBalance.peakLimitObject: "+peakLimitObject);
					response.getWriter().println("getAvailableBalance.peakLimitArray: "+peakLimitArray);
					response.getWriter().println("getAvailableBalance.peakLimitSize: "+peakLimitSize);
				}
				
				if (peakLimitSize > 0 ) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					
					Calendar calender = Calendar.getInstance();
					Date currentDate = calender.getTime();
					String currentDateSt = sdf.format(currentDate);
					currentDate = sdf.parse(currentDateSt);
					if(debug)
						response.getWriter().println("getAvailableBalance.CurrentDate: " +currentDate);
					
					boolean isactivePeakLimitAvailable = false;
					for (int i = 0; i < peakLimitSize; i++){
						JsonObject peakLimitItem = peakLimitArray.get(i).getAsJsonObject();
						peakLimitValidFrom = peakLimitItem.get("ValidFrom").getAsString();
						peakLimitValidTo = peakLimitItem.get("ValidTo").getAsString();
						
						peakLimitValidFrom = peakLimitValidFrom.substring(peakLimitValidFrom.indexOf('(')+1, peakLimitValidFrom.lastIndexOf(")"));
						peakLimitValidTo = peakLimitValidTo.substring(peakLimitValidTo.indexOf('(')+1, peakLimitValidTo.lastIndexOf(")"));
						
						validFromMillis = Long.parseLong(peakLimitValidFrom);
						validToMillis = Long.parseLong(peakLimitValidTo);
						
						calender.setTimeInMillis(validFromMillis);
						Date peakLimitValidFromDate = calender.getTime();
						peakLimitValidFrom = sdf.format(peakLimitValidFromDate);
						peakLimitValidFromDate = sdf.parse(peakLimitValidFrom);
						
						calender.setTimeInMillis(validToMillis);
						Date peakLimitValidToDate  = calender.getTime();
						peakLimitValidTo =sdf.format(peakLimitValidToDate);
						peakLimitValidToDate = sdf.parse(peakLimitValidTo);
						
						if(peakLimitValidFromDate.compareTo(currentDate)<= 0 && peakLimitValidToDate.compareTo(currentDate)>= 0) {
							if(debug)
								response.getWriter().println("getAvailableBalance.peakLimitItem: "+peakLimitItem);
							isactivePeakLimitAvailable = true;
							if(debug){
								response.getWriter().println("peakLimitValidFromDate: " +peakLimitValidFromDate);
								response.getWriter().println("peakLimitValidToDate: " +peakLimitValidToDate);
							}
							
							try{
								enhPeakLimit = peakLimitItem.get("ProposedLimit").getAsString();
							} catch (Exception e) {
								if(debug){
									StackTraceElement element[] = e.getStackTrace();
									StringBuffer buffer = new StringBuffer();
									for(int j=0;j<element.length;j++)
									{
										buffer.append(element[j]);
									}
									response.getWriter().println("getAvailableBalance.-Exception Stack Trace: "+buffer.toString());
								}
								
								enhPeakLimit ="0.00";
							}
							break;
						}
					}
					
					if(debug)
						response.getWriter().println("getAvailableBalance.enhPeakLimit (Active Peak): "+enhPeakLimit);
					
					executeURL = "";
					executeURL = oDataUrl+"SCFEnhancementLimits?$filter=AccountNo%20eq%20%27"+accnNo+"%27%20and%20EnhancementType%20eq%20%27"+primaryType+"%27";
					if(debug)
						response.getWriter().println("getAvailableBalance.enhPrimaryLimit (Active Peak Available)executeURL: "+executeURL);
					
					primaryLimitObject = commonUtils.executeURL(executeURL, userPass, response);
					primaryLimitArray = primaryLimitObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
					primaryLimitSize = primaryLimitArray.size();
					
					if(debug){
						response.getWriter().println("getAvailableBalance.primaryLimitObject: "+primaryLimitObject);
						response.getWriter().println("getAvailableBalance.primaryLimitArray: "+primaryLimitArray);
						response.getWriter().println("getAvailableBalance.primaryLimitSize: "+primaryLimitSize);
					}
					
					if(primaryLimitSize > 0){
						JsonObject primaryItem = primaryLimitArray.get(0).getAsJsonObject();
						if(debug)
							response.getWriter().println("getAvailableBalance.primaryItem: "+primaryItem);
						try {
							enhPrimaryLimit = primaryItem.get("ProposedLimit").getAsString();
						} catch (Exception e) {
							enhPrimaryLimit ="0.00";
						}
						
						if(debug)
							response.getWriter().println("getAvailableBalance.enhPrimaryLimit (Active Peak Available): "+enhPrimaryLimit);
					}
					
					if (! isactivePeakLimitAvailable) {
						enhPeakLimit = "0.00";
					}
				}else{
					//When Peak Limit is not available
					enhPeakLimit = "0.00";
					
					executeURL = "";
					executeURL = oDataUrl+"SCFEnhancementLimits?$filter=AccountNo%20eq%20%27"+accnNo+"%27%20and%20EnhancementType%20eq%20%27"+primaryType+"%27";
					if(debug)
						response.getWriter().println("getAvailableBalance.enhPrimaryLimit (Active Peak Available)executeURL: "+executeURL);
					
					primaryLimitObject = commonUtils.executeURL(executeURL, userPass, response);
					primaryLimitArray = primaryLimitObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
					primaryLimitSize = primaryLimitArray.size();
					
					if(debug){
						response.getWriter().println("getAvailableBalance.primaryLimitObject (Peak Not Available): "+primaryLimitObject);
						response.getWriter().println("getAvailableBalance.primaryLimitArray (Peak Not Available): "+primaryLimitArray);
						response.getWriter().println("getAvailableBalance.primaryLimitSize (Peak Not Available): "+primaryLimitSize);
					}
					
					if(primaryLimitSize > 0){
						//Primary available and Peak not available
						JsonObject primaryItem = primaryLimitArray.get(0).getAsJsonObject();
						if(debug)
							response.getWriter().println("getAvailableBalance.primaryItem: "+primaryItem);
						try {
							enhPrimaryLimit = primaryItem.get("ProposedLimit").getAsString();
						} catch (Exception e) {
							enhPrimaryLimit ="0.00";
						}
						
						if(debug)
							response.getWriter().println("getAvailableBalance.enhPrimaryLimit (Peak Not Available, But Primary Available): "+enhPrimaryLimit);
					}else{
						//Primary and Peak both not available
						JsonObject scfChildObj = scfArray.get(0).getAsJsonObject();
						try {
//							enhPrimaryLimit = scfChildObj.get("OfferAmt").getAsString();
							enhPrimaryLimit = scfSanctionLimit;
						} catch (Exception e) {
							enhPrimaryLimit="0.00";
						}
						if(debug)
							response.getWriter().println("getAvailableBalance.enhPrimaryLimit (Primary and Peak both not available): "+enhPrimaryLimit);
					}
					
					if(debug)
						response.getWriter().println("getAvailableBalance.enhPeakLimit (Peak not available): "+enhPeakLimit);
				}
				
				if(debug){
					response.getWriter().println("getAvailableBalance.Final.AccountNo: "+accnNo);
					response.getWriter().println("getAvailableBalance.Final.SanctionLimit: "+scfSanctionLimit);
					response.getWriter().println("getAvailableBalance.Final.PrimaryLimit: "+enhPrimaryLimit);
					response.getWriter().println("getAvailableBalance.Final.PeakLimit: "+enhPeakLimit);
					response.getWriter().println("getAvailableBalance.Final.Amount: "+accountBalanceJsonObj.get("Amount").getAsString());
				}
				
				calculationObject = commonUtils.calculateAmount(response, scfSanctionLimit, enhPrimaryLimit, enhPeakLimit, accountBalanceJsonObj.get("Amount").getAsString(), debug);
				
				String calcMsg = "";
				calcMsg = calculationObject.get("Message").getAsString();
				if (calcMsg.equalsIgnoreCase("")) {
					returnMessage = calculationObject.get("AvailableBalance").getAsString();
					return returnMessage;
				}else{
					returnMessage = "001";
					return returnMessage;
				}
			}
		}catch (Exception e) {
//			availableBalance = new BigDecimal(0.00);
			if(debug){
				StackTraceElement element[] = e.getStackTrace();
				StringBuffer buffer = new StringBuffer();
				for(int i=0;i<element.length;i++)
				{
					buffer.append(element[i]);
				}
				response.getWriter().println("Exception in getAvailableBalance: "+buffer.toString());
			}
			returnMessage = "001";
			return returnMessage;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

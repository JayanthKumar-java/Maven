package com.arteriatech.cf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.AccountBalance.AccountBalanceClient;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class AccountBalances
 */
@WebServlet("/AccountBalances")
public class AccountBalances extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountBalances() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		boolean debug = false;
		ChannelFinanceOps cfOps = new ChannelFinanceOps();
		String accountNo="";
		JsonObject responseJson = new JsonObject();
		Map<String, String> accountBalanceObjMap = new HashMap<String, String>();
		AccountBalanceClient accountBalance = new AccountBalanceClient();
		
		try{
			if (null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			
			accountNo = cfOps.getAccountNoFromDCCNFG(request, response, debug);
			if(debug)
				response.getWriter().println("accountNo: "+ accountNo);
			if(accountNo.equalsIgnoreCase("No Records Found") || accountNo.equalsIgnoreCase("Error")){
				responseJson.addProperty("AccountNo", "");
				responseJson.addProperty("Status", "000002");
				responseJson.addProperty("AsOn", "");
				responseJson.addProperty("Amount", "0.00");
				responseJson.addProperty("Currency", "INR");
				responseJson.addProperty("ErrorCode", "001");
				responseJson.addProperty("Message", accountNo);
				responseJson.addProperty("LoginId", "");
				responseJson.addProperty("FreezeStatus", "");
				responseJson.addProperty("FreezeStatusDesc", "");
				responseJson.addProperty("ExpiryDate", "");
			}else{
				accountBalanceObjMap = accountBalance.callAccountBalance(accountNo, "", "", "", "", debug);
				//Pass expiry date from here and send in output
				if(debug){
					for (String key : accountBalanceObjMap.keySet()) {
						if(debug)
							response.getWriter().println("AccountBalance-accountBalanceObjMap: "+key + " - " + accountBalanceObjMap.get(key));
					}
				}
				responseJson.addProperty("AccountNo", "");
				responseJson.addProperty("Status", accountBalanceObjMap.get("Status"));
				responseJson.addProperty("AsOn", accountBalanceObjMap.get("AsOn"));
				responseJson.addProperty("Amount", accountBalanceObjMap.get("Amount"));
				responseJson.addProperty("Currency", accountBalanceObjMap.get("Currency"));
				responseJson.addProperty("ErrorCode", accountBalanceObjMap.get("ErrorCode"));
				responseJson.addProperty("Message", accountNo);
				responseJson.addProperty("LoginId", "");
				responseJson.addProperty("FreezeStatus", accountBalanceObjMap.get("FreezeStatus"));
				responseJson.addProperty("FreezeStatusDesc", "");
				responseJson.addProperty("ExpiryDate", accountBalanceObjMap.get("ExpiryDate"));
			}
			
			response.getWriter().println(responseJson);
		}catch (Exception e) {
			responseJson.addProperty("AccountNo", "");
			responseJson.addProperty("Status", "000002");
			responseJson.addProperty("AsOn", "");
			responseJson.addProperty("Amount", "0.00");
			responseJson.addProperty("Currency", "INR");
			responseJson.addProperty("ErrorCode", "002");
			responseJson.addProperty("Message", e.getMessage());
			responseJson.addProperty("LoginId", "");
			responseJson.addProperty("FreezeStatus", "");
			responseJson.addProperty("FreezeStatusDesc", "");
			responseJson.addProperty("ExpiryDate", "");
			response.getWriter().println(responseJson);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}

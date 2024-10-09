package com.arteriatech.pg;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

/**
 * Servlet implementation class ResWalletPymt
 */
@WebServlet("/ResWalletPymt")
public class ResWalletPymt extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResWalletPymt() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			walletPublicKey = properties.getProperty("walletPublicKey");
			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
			merchantPublicKey = properties.getProperty("merchantPublicKey");
			merchantCode = properties.getProperty("merchantCode");
			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgResponseErrorMsg = properties.getProperty("pgResponseErrorMsg");
			
			pgRespMsg = request.getParameter("walletResponseMessage");

			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setWalletResponseMessage(pgRespMsg);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			
			String walletTxnStatus="", txnID="", walletUserCode="", txnSessionID="", walletTxnID="", walletTxnDateTime="", txnDateTime="", walletBankRefID="", txnFor="",
					txnAmount="", walletTxnRemarks="", adnlParam1="", adnlParam2="", adnlParam3="", adnlParam4="", adnlParam5="", adnlParam6="", adnlParam7="", adnlParam8="",
					adnlParam9="", adnlParam10="";
			if(walletMessageBean.validateWalletResponseMessage()) 
			{
				WalletParamMap map = walletMessageBean.getResponseMap();
				walletTxnStatus = map.get("wallet-txn-status");
				txnID = map.get("txn-id");
				walletUserCode = map.get("wallet-user-code");
//				txnSessionID = map.get("txn-session-id");
				txnSessionID = map.get("KSJ9");
				walletTxnID = map.get("wallet-txn-id");
				walletTxnDateTime = map.get("wallet-txn-datetime");
				txnDateTime = map.get("txn-datetime");
				walletBankRefID = map.get("wallet-bank-ref-id");
				txnFor = map.get("txn-for");
				txnAmount = map.get("txn-amount");
				walletTxnRemarks = map.get("wallet-txn-remarks");
				adnlParam1 = map.get("additional-param1");
				adnlParam2 = map.get("additional-param2");
				adnlParam3 = map.get("additional-param3");
				adnlParam4 = map.get("additional-param4");
				adnlParam5 = map.get("additional-param5");
				adnlParam6 = map.get("additional-param6");
				adnlParam7 = map.get("additional-param7");
				adnlParam8 = map.get("additional-param8");
				adnlParam9 = map.get("additional-param9");
				adnlParam10 = map.get("additional-param10");
				
				JsonObject result = new JsonObject();
				result.addProperty("walletTxnStatus", walletTxnStatus);
				result.addProperty("txnID", txnID);
				result.addProperty("walletUserCode", walletUserCode);
				result.addProperty("txnSessionID", txnSessionID);
				result.addProperty("walletTxnID", walletTxnID);
				result.addProperty("walletTxnDateTime", walletTxnDateTime);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("walletBankRefID", walletBankRefID);
				result.addProperty("txnFor", txnFor);
				result.addProperty("txnAmount", txnAmount);
				result.addProperty("walletTxnRemarks", walletTxnRemarks);
				result.addProperty("adnlParam1", adnlParam1);
				result.addProperty("adnlParam2", adnlParam2);
				result.addProperty("adnlParam3", adnlParam3);
				result.addProperty("adnlParam4", adnlParam4);
				result.addProperty("adnlParam5", adnlParam5);
				result.addProperty("adnlParam6", adnlParam6);
				result.addProperty("adnlParam7", adnlParam7);
				result.addProperty("adnlParam8", adnlParam8);
				result.addProperty("adnlParam9", adnlParam9);
				result.addProperty("adnlParam10", adnlParam10);
				
//				response.getWriter().print(new Gson().toJson(result));
				
				request.setAttribute("walletTxnStatus", walletTxnStatus);
				request.setAttribute("txnID", txnID);
				request.setAttribute("txnAmount", txnAmount);
				 response.setContentType("text/html");  
					RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");  
				 view.forward(request,response);
			}
			else
			{
				JsonObject result = new JsonObject();
//				result.addProperty("pgReqMsg", pgResponseErrorMsg);
//				response.getWriter().print(new Gson().toJson(result));
				request.setAttribute("walletTxnStatus", "404");
				request.setAttribute("txnID", "XXX");
				request.setAttribute("txnAmount", "0");
				response.setContentType("text/html");  
					RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");  
					 view.forward(request,response);
			}
		}
		catch (Exception e)
		{
//			response.getWriter().print(e.getMessage());
			request.setAttribute("walletTxnStatus", "405");
			request.setAttribute("txnID", "XXX");
			request.setAttribute("txnAmount", "0");
			 response.setContentType("text/html");  
				RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");  
				 view.forward(request,response);
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

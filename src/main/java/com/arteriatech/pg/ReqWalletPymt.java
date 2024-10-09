package com.arteriatech.pg;

import java.io.IOException;
import java.util.Properties;

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
 * Servlet implementation class ReqWalletPymt
 */
@WebServlet("/ReqWalletPymt")
public class ReqWalletPymt extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReqWalletPymt() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String walletPublicKey = "", merchantPrivateKey = "", merchantPublicKey = "", merchantCode="", walletPymtRequestCall="", pgReqMsg="", pgRequestErrorMsg="";
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			walletPublicKey = properties.getProperty("walletPublicKey");
			merchantPrivateKey = properties.getProperty("merchantPrivateKey");
			merchantPublicKey = properties.getProperty("merchantPublicKey");
			merchantCode = properties.getProperty("merchantCode");
			walletPymtRequestCall = properties.getProperty("walletPymtRequestCall");
			pgRequestErrorMsg = properties.getProperty("pgRequestErrorMsg");
			
			WalletParamMap payReqInputMap = new WalletParamMap();
			if(null != request.getParameter("txn-id"))
				payReqInputMap.put("txn-id", request.getParameter("txn-id"));
			if(null != request.getParameter("txn-session-id"))
				payReqInputMap.put("txn-session-id", request.getParameter("txn-session-id"));
			if(null != request.getParameter("wallet-user-code"))
				payReqInputMap.put("wallet-user-code", request.getParameter("wallet-user-code"));
			if(null != request.getParameter("txn-datetime"))
				payReqInputMap.put("txn-datetime", request.getParameter("txn-datetime"));
			if(null != request.getParameter("txn-amount"))
				payReqInputMap.put("txn-amount", request.getParameter("txn-amount"));
			payReqInputMap.put("txn-for", walletPymtRequestCall);
			if(null != request.getParameter("return-url"))
				payReqInputMap.put("return-url", request.getParameter("return-url"));
			if(null != request.getParameter("cancel-url"))
				payReqInputMap.put("cancel-url", request.getParameter("cancel-url"));
			if(null != request.getParameter("additional-param1"))
				payReqInputMap.put("additional-param1", request.getParameter("additional-param1"));
			else
				payReqInputMap.put("additional-param1", "NA");
			if(null != request.getParameter("additional-param2"))
				payReqInputMap.put("additional-param2", request.getParameter("additional-param2"));
			else
				payReqInputMap.put("additional-param2", "NA");
			if(null != request.getParameter("additional-param3"))
				payReqInputMap.put("additional-param3", request.getParameter("additional-param3"));
			else
				payReqInputMap.put("additional-param3", "NA");
			if(null != request.getParameter("additional-param4"))
				payReqInputMap.put("additional-param4", request.getParameter("additional-param4"));
			else
				payReqInputMap.put("additional-param4", "NA");
			if(null != request.getParameter("additional-param5"))
				payReqInputMap.put("additional-param5", request.getParameter("additional-param5"));
			else
				payReqInputMap.put("additional-param5", "NA");
			if(null != request.getParameter("additional-param6"))
				payReqInputMap.put("additional-param6", request.getParameter("additional-param6"));
			else
				payReqInputMap.put("additional-param6", "NA");
			if(null != request.getParameter("additional-param7"))
				payReqInputMap.put("additional-param7", request.getParameter("additional-param7"));
			else
				payReqInputMap.put("additional-param7", "NA");
			if(null != request.getParameter("additional-param8"))
				payReqInputMap.put("additional-param8", request.getParameter("additional-param8"));
			else
				payReqInputMap.put("additional-param8", "NA");
			if(null != request.getParameter("additional-param9"))
				payReqInputMap.put("additional-param9", request.getParameter("additional-param9"));
			else
				payReqInputMap.put("additional-param9", "NA");
			if(null != request.getParameter("additional-param10"))
				payReqInputMap.put("additional-param10", request.getParameter("additional-param10"));
			else
				payReqInputMap.put("additional-param10", "NA");
			
			WalletMessageBean walletMessageBean = new WalletMessageBean();
			walletMessageBean.setRequestMap(payReqInputMap);
			walletMessageBean.setWalletKey(walletPublicKey);
			walletMessageBean.setClientKey(merchantPrivateKey);
			pgReqMsg = walletMessageBean.generateWalletRequestMessage();

			if(pgReqMsg != null && pgReqMsg.trim().length() > 0)
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgReqMsg);
				result.addProperty("walletClientCode", merchantCode);
				response.getWriter().print(new Gson().toJson(result));
			}
			else
			{
				JsonObject result = new JsonObject();
				result.addProperty("pgReqMsg", pgRequestErrorMsg);
				result.addProperty("walletClientCode", merchantCode);
				response.getWriter().print(new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			response.getWriter().print(e.getMessage());
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

package com.arteriatech.pg;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class ResPymtGatewayAxis
 */
@WebServlet("/ResPymtGatewayAxis")
public class ResPymtGatewayAxis extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResPymtGatewayAxis() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		try
		{
			String encryptionKey = "";
			
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			encryptionKey = properties.getProperty("axisEncryptionKey");
			
			String merchantCode="", paymentRequestCall="", pgRespMsg="", pgResponseErrorMsg="";

			merchantCode = properties.getProperty("merchantCode");
			paymentRequestCall = properties.getProperty("paymentRequestCall");
			pgResponseErrorMsg = properties.getProperty("pgRequestErrorMsg");
			byte[] outputParams = null;
			pgRespMsg = request.getParameter("walletResponseMessage");

			
			String txnStatus="", txnID="", bankRefNo="", statusMsg="", txnDateTime="", pmd="", txnReferenceID="", pgVersion="", corporateID="", pgType="", customerNo="", currency="", payAmount="", checkSum="";
			if(null != pgRespMsg && pgRespMsg.trim().length() > 0) 
			{
				
				SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, skey);
				outputParams = cipher.doFinal(Base64.getDecoder().decode(pgRespMsg));
				response.getWriter().print(new String(outputParams));
				
				/*
				 * Need to segregate parameters based on the delimiter
				 * 
				 * */
			      
				JsonObject result = new JsonObject();
				result.addProperty("txnStatus", txnStatus);
				result.addProperty("txnID", txnID);
				result.addProperty("bankRefNo", bankRefNo);
				result.addProperty("statusMsg", statusMsg);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("pmd", pmd);
				result.addProperty("txnDateTime", txnDateTime);
				result.addProperty("txnReferenceID", txnReferenceID);
				result.addProperty("pgVersion", pgVersion);
				result.addProperty("corporateID", corporateID);
				result.addProperty("pgType", pgType);
				result.addProperty("customerNo", customerNo);
				result.addProperty("currency", currency);
				result.addProperty("payAmount", payAmount);
				result.addProperty("checkSum", checkSum);
				/*result.addProperty("adnlParam5", adnlParam5);
				result.addProperty("adnlParam6", adnlParam6);
				result.addProperty("adnlParam7", adnlParam7);
				result.addProperty("adnlParam8", adnlParam8);
				result.addProperty("adnlParam9", adnlParam9);
				result.addProperty("adnlParam10", adnlParam10);*/

				/*
				 * Additional parameters 1 to 4 are used for getting redirect url params based on which we'll form redirect url below. 
				 * These parameters are sent during Request Initiation servlet call.
				 * site-id : eg: SSLaunchpad
				 * application-id : eg: ssoutstanding1pg
				 * action-name: eg: Display
				 * nav-param: eg: Search
				 * Please note that these parameter values should not have length more than 25 characters
				 */
				response.getWriter().print(new Gson().toJson(result));
				if(null != txnStatus)
					request.setAttribute("txnStatus", txnStatus);
				else
					request.setAttribute("txnStatus", "Not Found");
				
				if(null != txnID)
					request.setAttribute("txnID", txnID);
				else
					request.setAttribute("txnID", "Not Found");
				
				if(null != payAmount)
					request.setAttribute("payAmount", payAmount);
				else
					request.setAttribute("payAmount", "0.00");
				
				if(null != txnReferenceID)
					request.setAttribute("txnReferenceID", txnReferenceID);
				else
					request.setAttribute("txnReferenceID", "Not Found");
				
				response.setContentType("text/html");
//				String redirectURL = "/sites/"+adnlParam1+"#"+adnlParam2+"-"+adnlParam3+"&/"+adnlParam4+"/(walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+")";
				String redirectURL = "";
				response.sendRedirect("");
//				response.sendRedirect("/sites/SSLaunchpad#sfoutstdnginvs1pg-Display&/Search/(CustomerNo=1000,InvoiceNo=,InvoiceDate=%20,InvoiceFromDate=null,InvoiceToDate=null,walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnAmount="+txnAmount+")");
//				response.sendRedirect("/sites/SSLaunchpad#sfoutstdnginvs1pg-Display&/Search/(CustomerNo=1000,InvoiceNo=,InvoiceDate=%20,InvoiceFromDate=null,InvoiceToDate=null,walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+")");
				/*if(adnlParam4 != null && adnlParam4.toString().trim().length()>0)
				{
					if(adnlParam4.toString().trim().equalsIgnoreCase("NA"))
					{
						redirectURL = "/sites/"+adnlParam1+"#"+adnlParam2+"-"+adnlParam3+"?walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+",source=PG";
					}
					else
					{
						redirectURL = "/sites/"+adnlParam1+"#"+adnlParam2+"-"+adnlParam3+"&/"+adnlParam4+"/(walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+",source=PG"+")";
					}
					response.sendRedirect(redirectURL);
				}
				else
				{
					response.sendRedirect("/sites/SSLaunchpad");
				}*/
//				RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");  
//				view.forward(request,response);
			}
			else
			{
				JsonObject result = new JsonObject();
//				result.addProperty("pgReqMsg", pgResponseErrorMsg);
//				response.getWriter().print(new Gson().toJson(result));
				request.setAttribute("walletTxnStatus", "error");
				request.setAttribute("txnID", "NA");
				request.setAttribute("txnAmount", "0");
//				request.setAttribute("txnSessionID", txnSessionID);
				 response.setContentType("text/html");  
//					RequestDispatcher view = request.getRequestDispatcher("/TopUp.jsp");
//				 response.sendRedirect("/sites/SSLaunchpad#sfoutstdnginvs1pg-Display&/Search/(CustomerNo=1000,InvoiceNo=,InvoiceDate=%20,InvoiceFromDate=null,InvoiceToDate=null,walletTxnStatus="+walletTxnStatus+",txnID="+txnID+",txnSessionID="+txnSessionID+",txnAmount="+txnAmount+")");
//				 RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");
//					 view.forward(request,response);
			}
		}
		catch (Exception e)
		{
			/*request.setAttribute("walletTxnStatus", "405");
			request.setAttribute("txnID", "");
			request.setAttribute("txnAmount", "0");
			response.setContentType("text/html");  
			RequestDispatcher view = request.getRequestDispatcher("/Wallet.jsp");  
			view.forward(request,response);*/
			
			response.getWriter().println(displayErrorForWeb(e));
				
		}
	}

	public String displayErrorForWeb(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

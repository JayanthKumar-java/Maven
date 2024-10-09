package com.arteriatech.cf;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.wallet247.clientutil.bean.WalletMessageBean;
import com.wallet247.clientutil.bean.WalletParamMap;

public class InvokeIntimationApi extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Properties properties = new Properties();
		String arteriaApiKey = "", walletApiKey = "";
		try {
			JSONObject inputPayload = getInputPayload(req);
			String jsonInpu = inputPayload.toString();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			arteriaApiKey = properties.getProperty("ARTMerchantPrivateKey");
			walletApiKey = properties.getProperty("WalletPublicKey");
			WalletParamMap walletParamMap = new WalletParamMap();
			walletParamMap.put("wt-code", "WT-1111");
			walletParamMap.put("p2c-txn-id", "tx-id-123456789012");
			walletParamMap.put("van", "abcdtest1234557");
			walletParamMap.put("user-code", "sampleUsercode");
			walletParamMap.put("amount", "105.20");
			walletParamMap.put("txn-dt", "2021-12-08 12:6:36");
			walletParamMap.put("pay-mode", "1");
			walletParamMap.put("van-bal", "1000.20");
			walletParamMap.put("utr-no", "ECOLL ");
			walletParamMap.put("remarks", "transaction was successfull");
			WalletMessageBean walletMessageReqBean = new WalletMessageBean();
			walletMessageReqBean.setRequestMap(walletParamMap);
			walletMessageReqBean.setClientKey(arteriaApiKey);
			walletMessageReqBean.setWalletKey(walletApiKey);
			String walletRequestMessage = walletMessageReqBean.generateWalletRequestMessage();
		    resp.getWriter().println(" encrypted message "+walletRequestMessage);
			/*req.setAttribute("WalletParamMap", walletRequestMessage);
			RequestDispatcher rd = req.getRequestDispatcher("EscrowS2SAck");
			rd.forward(req, resp);*/

		} catch (Exception ex) {
			resp.getWriter().println(ex.getLocalizedMessage());
			resp.getWriter().println(" encrypted message ");

		}
	}
	
	private JSONObject getInputPayload(HttpServletRequest request) throws IOException,Exception {
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();
		try {
			Enumeration<String> parameterNames = request.getParameterNames();
			Map<String, String[]> parameterMap = request.getParameterMap();
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				String[] paramValues = request.getParameterValues(paramName);
				jsonObj.accumulate(paramName, paramValues);
			}
			for(String key:parameterMap.keySet()){
				jsonObj1.accumulate(key, parameterMap.get(key));
			}

		}  catch(Exception ex){
			jsonObj.accumulate("cause", ex.getCause());
			jsonObj.accumulate("message", ex.getMessage());
			jsonObj.accumulate("class", ex.getClass());
		    	 
         }
		return jsonObj1;
	}

}

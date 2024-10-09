package com.arteriatech.cf;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutStandingClient;
import com.google.gson.JsonObject;

public class SCFDealerOutStanding extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String dealerODAccountNo = "";
		boolean debug = false;
		SCFDealerOutStandingClient scfDealerOutStanding = new SCFDealerOutStandingClient();
		JsonObject scfDealerOutStandingResponse = new JsonObject();
		dealerODAccountNo = request.getParameter("AccountNo");
		if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
			debug = true;
		}
		try {
			if (dealerODAccountNo != null && !dealerODAccountNo.equalsIgnoreCase("")) {
				scfDealerOutStandingResponse = scfDealerOutStanding.callSCFDealerOutStandingClient(response,
						dealerODAccountNo, debug);
				response.getWriter().println(scfDealerOutStandingResponse);
			} else {
				JsonObject resObj = new JsonObject();
				resObj.addProperty("message", "Invalid Input");
				resObj.addProperty("Status", "000002");
				resObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(resObj);

			}
		} catch (Exception ex) {
			JsonObject retunObj = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			retunObj.addProperty("Exception", ex.getClass().getCanonicalName());
			retunObj.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			retunObj.addProperty("Status", "000002");
			retunObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(retunObj);
		}

	}

}

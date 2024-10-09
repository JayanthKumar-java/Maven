package com.arteriatech.cf;

import javax.servlet.annotation.WebServlet;

import com.sap.ecm.api.AbstractCmisProxyServlet;


/**
 * Servlet implementation class DocumentServiceServlet
 */
@WebServlet("/DocumentService")
public class DocumentServiceServlet extends AbstractCmisProxyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getRepositoryUniqueName() {
//		return "Documents";
		return "FirstRepository";
	}

	
	@Override
	protected String getRepositoryKey() {
//		return "welcome@12";
		return "Arteria@123";
	}
}


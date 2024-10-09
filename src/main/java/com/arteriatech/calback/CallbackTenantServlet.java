package com.arteriatech.calback;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/callback/v1.0/tenants/")
public class CallbackTenantServlet extends HttpServlet {

    public CallbackTenantServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Inside doGet of Tenant servlet");
        super.doGet(req, resp);
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Inside doPost of Tenant servlet");
        super.doPost(req, resp);
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tenantId = req.getParameter("tenantId");
        String jsonString = IOUtils.toString(req.getReader());
        System.out.println("Inside doPut of Tenant servlet");
        System.out.println("tenantId = "+tenantId);
        System.out.println("jsonString = " +jsonString);
        JSONObject json = new JSONObject(jsonString);
        String subdomain = json.getString("subscribedSubdomain");
        System.out.println("subdomain = " +subdomain);
        resp.setContentType("application/json");
        // resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("https://"+subdomain+"-paymentgateway.cfapps.eu10-004.hana.ondemand.com");
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("done");
    }

    /**
     *
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
    }
}

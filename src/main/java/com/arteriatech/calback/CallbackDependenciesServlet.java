package com.arteriatech.calback;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import static java.nio.charset.StandardCharsets.*;
@WebServlet("/callback/v1.0/dependencies/")
public class CallbackDependenciesServlet extends HttpServlet {
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CallbackDependenciesServlet() {
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
        // String message = "[{\"xsappname\" : \"paymentgateway\"}]"; //Added by Sujai
        // String message = "[{\"xsappname\" : \"clonebf595c286dec4e0a89966ad2f13a4c91!b241723|destination-xsappname!b404\"},{\"xsappname\" : \"clonee9b0a1bce8ab4d1488f540f1b3d38f5a!b241723|connectivity!b114511\"}]"; //Added by Sujai
        String message = "[{\"xsappname\" : \"cloneefa4d8d7a02d4830aaaecd6679891757!b451819|destination-xsappname!b404\"},{\"xsappname\" : \"clonec1eae72e91a747078a80496e13605783!b451819|connectivity!b114511\"}]"; //Added by Sujai
        byte[] bytes = message.getBytes(ISO_8859_1); 
        String jsonStr = new String(bytes, UTF_8);
        // String message = "[]";
        // resp.setContentType("application/json");
        System.out.println("Inside doGet of Dependencies servlet");
        System.out.println("Inside doGet: jsonStr: "+jsonStr);
        // Actual logic goes here.
        resp.setContentType("application/json");
        // resp.setStatus(HttpServletResponse.SC_OK); //written by Sujai
        // resp.getWriter().println(message); //Written by Sujai

        PrintWriter out = resp.getWriter();
        out.println(jsonStr);
    }



    /**
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        System.out.println("Inside init of Dependencies servlet");
        super.init();
    }
}

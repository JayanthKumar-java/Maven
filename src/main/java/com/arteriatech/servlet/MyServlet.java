package com.arteriatech.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arteriatech.pg.CommonUtils;
import com.arteriatech.support.DestinationUtils;
import com.google.gson.JsonObject;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestinationProperties;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationProperty;

@Configuration
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String id = request.getParameter("id");
			String fileId = id;
			String Url = "https://drive.google.com/uc";
			String imageUrl = Url + "?id=" + fileId;
			getImage(request, response, imageUrl);
		} catch (Exception exception) {
			StackTraceElement[] stackTrace = exception.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			response.setHeader("Content-type", "application/json");
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", exception.getLocalizedMessage());
			resObj.addProperty("Exception StackTrace", buffer.toString());
			response.getWriter().println(resObj);
		}
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	response.getWriter().println("doPut ");
	}

	private void getImage(HttpServletRequest request, HttpServletResponse response, String imageUrl)
			throws IOException {
		URL url = new URL(imageUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		// Set response headers based on the fetched image
		response.setContentType(connection.getContentType());
		response.setContentLength(connection.getContentLength());

		try (InputStream inputStream = connection.getInputStream();
				OutputStream outputStream = response.getOutputStream()) {
			// Proxy the image data from Google Drive to the servlet response
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception exception) {
			StackTraceElement[] stackTrace = exception.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			response.setHeader("Content-type", "application/json");
			JsonObject resObj = new JsonObject();
			resObj.addProperty("Message", exception.getLocalizedMessage());
			resObj.addProperty("Exception StackTrace", buffer.toString());
			response.getWriter().println(resObj);
		} finally {
			connection.disconnect();
		}
	}

	@Bean
	public ServletRegistrationBean<MyServlet> myServletBean() {
		ServletRegistrationBean<MyServlet> bean = new ServletRegistrationBean<>(new MyServlet(), "/hello");
		return bean;
	}
}
package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import org.thymeleaf.context.Context;
import java.io.OutputStream;

import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.RectangleReadOnly;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class Trent extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// OutputStream os = null;
 
		try {

			// ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
			// templateResolver.setTemplateMode(TemplateMode.HTML);
			// templateResolver.setPrefix("/Resources/Trents/");
			// templateResolver.setSuffix(".html");
			// templateResolver.setCacheTTLMs(3600000L);
			// TemplateEngine templateEngine = new TemplateEngine();
			// templateEngine.setTemplateResolver(templateResolver);
			// Context webContext = new Context();
 
			// webContext.setVariable("PaymentReferenceNumber", "JAVA");
			// String html = templateEngine.process("demo", webContext);
			// ITextRenderer renderer = new ITextRenderer();
			// renderer.setDocumentFromString(html);
			// renderer.layout();
			// response.setContentType("application/pdf");
			// response.setHeader("Cache-Control", "no-cache");
			// os = response.getOutputStream();
			// renderer.createPDF(os);
			// os.flush();
 
            // Initialize Thymeleaf template engine
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/Resources/Trents/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);  // Cache time-to-live in milliseconds

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        // Create web context and add variables
        Context webContext = new Context();
        webContext.setVariable("PaymentReferenceNumber", "JAVA");
        webContext.setVariable("VendorAddress1", "Ve rAghhhhhhhhhhhhhhhhhhn1:");
        webContext.setVariable("Mobile", "9108041039");
        webContext.setVariable("VendorAddress2", "#13/6 jfh 8th mai nroad pbengalore 560987");
        webContext.setVariable("ContactPersonName", "harshith rana nayaka");

        // Process the template to generate HTML content
        String html = templateEngine.process("demo", webContext);

        // Create the ITextRenderer instance
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);  // Pass the generated HTML to the renderer

        // Layout the PDF content
        renderer.layout();

        // Set response headers and content type
        response.setContentType("application/pdf");
        response.setHeader("Cache-Control", "no-cache");

        // Get the output stream and write the PDF
        OutputStream os = response.getOutputStream();
        renderer.createPDF(os);  // Create the PDF
        os.flush();  // Ensure all data is written to the output stream
        os.close();  // Close the output stream
            
		} catch (Exception e) {
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(".PrintStackTrace");
			e.printStackTrace();
			response.getWriter().println(".StackTrace :"+buffer.toString());
			response.getWriter().println(".getMessage: "+e.getMessage());
			response.getWriter().println(".getCause: "+e.getCause());
			response.getWriter().println(".getClass: "+e.getClass());
			response.getWriter().println(".getLocalizedMessage: "+e.getLocalizedMessage());
		}
	}

    @Bean
	public ServletRegistrationBean<Trent> TrentBean() {
		ServletRegistrationBean<Trent> bean = new ServletRegistrationBean<>(new Trent(), "/Trent");
		return bean;
	}
}

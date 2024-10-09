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
public class PDF extends HttpServlet{

@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// TODO Auto-generated method stub

	InputStream inputStream = getServletContext().getResourceAsStream("/index.html");
        String htmlTemplate = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
 
        // Replace placeholders with dynamic values
        String name = "John Doe";
        String age = "30";
        String dynamicHtml = htmlTemplate
                .replace("{{name}}", name)
                .replace("{{age}}", age);
 
               // Set response content type to HTML
        // resp.setContentType("text/html");
        
        // // Write the dynamic HTML content to the response
        // resp.getWriter().write(dynamicHtml);

      // Convert the dynamic HTML to PDF using iText
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4.rotate());
    try {
       
    PdfWriter writer = PdfWriter.getInstance(document, bos);
    document.open();
    XMLWorkerHelper.getInstance().parseXHtml(writer, document, new ByteArrayInputStream(dynamicHtml.getBytes()));
    document.close();
        } catch (DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
 
    // Set response content type to PDF
    resp.setContentType("application/pdf");
 
    // Set the response header to prompt the user to save the PDF
    // resp.setHeader("Content-Disposition", "attachment; filename=\"dynamic_pdf.pdf\"");
 
    // Write the PDF content to the response
    resp.getOutputStream().write(bos.toByteArray());

  }
    
    @Bean
	public ServletRegistrationBean<PDF> PDFBean() {
		ServletRegistrationBean<PDF> bean = new ServletRegistrationBean<>(new PDF(), "/PrintPDF");
		return bean;
	}
}

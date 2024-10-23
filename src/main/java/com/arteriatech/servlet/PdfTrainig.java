package com.arteriatech.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
// import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class PdfTrainig extends HttpServlet{


    private static final long serialVersionUID = 1L;
    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    		try 
    		{
    			//			String a=request.getParameter("range");
    			Document document = new Document();   //Creating Document
    			PdfWriter writer=PdfWriter.getInstance(document, response.getOutputStream());
    			//          just to CreatePDF
    
    
    						// PdfWriter writer=PdfWriter.getInstance(document,baos);
    			//			To Sending Email
    
    			document.open();//open the document
    
    			PdfContentByte border=createBorder(writer);
    			border.stroke();
    			//Tocreate border
    
    			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
    			DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
    
    			// x axis months   
    			String [] months={"April","May","june","july","august","sept"};
    			//connecting points
    			double []connectinPoints={72.94,89.68,72.34,62.68,72.94,53.24};
    			double []connectinPoints1={23.94,39.68,42.94,62.68,52.94,63.24};
    			double []connectinPoints2={82.94,69.68,42.44,32.68,42.94,33.24};
    
    			//adding months and connecting points 
    			for(int i=0;i<=months.length-1;i++)
    			{
    				dataset.addValue(connectinPoints[i], "", months[i]);
    				dataset1.addValue(connectinPoints1[i], "", months[i]);
    				dataset2.addValue(connectinPoints2[i], "", months[i]);
    			}
    
    			//Creating Graph as a chart
    			// 1st parameter is heading of the chart 
    			// 2nd paramter is we have to put any String Label in x axis 
    			//3rd parameter is we have to put any String Label in y axis
    			//4th paramterer data set for graph
    			//5th orientation or position of the chart
    			//6th legend ,information about the  each data series
    			//7th tooltips small piece information
    			//8th url in html
    			JFreeChart chart = ChartFactory.createLineChart(
    					"", "", "", dataset ,PlotOrientation.VERTICAL, false, false, false);
    			JFreeChart chart1 = ChartFactory.createLineChart(
    					"", "", "", dataset1 ,PlotOrientation.VERTICAL, false, false, false);
    			JFreeChart chart2 = ChartFactory.createLineChart(
    					"", "", "", dataset2 ,PlotOrientation.VERTICAL, false, false, false);
    
    			//inside that chart we are plotting thevalues 
    			CategoryPlot plot = chart.getCategoryPlot();
    			CategoryPlot plot1 = chart1.getCategoryPlot();
    			CategoryPlot plot2 = chart2.getCategoryPlot();
    
    			//xaxis taking as domainaxis
    			// domain axis for labels position
    			CategoryAxis xAxis = plot.getDomainAxis();
    			xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
    
    			CategoryAxis xAxis1 = plot1.getDomainAxis();
    			xAxis1.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
    
    			CategoryAxis xAxis2 = plot2.getDomainAxis();
    			xAxis2.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
    
    			//yaxis taking as rangeaxis
    			//number axis  for 
    			NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
    			yAxis.setRange(0,100);
    
    			NumberAxis yAxis1 = (NumberAxis) plot1.getRangeAxis();
    			yAxis1.setRange(0,100);
     
    			NumberAxis yAxis2 = (NumberAxis) plot2.getRangeAxis();
    			yAxis2.setRange(0,100);
    
    			//for line
    			LineAndShapeRenderer renderer = new LineAndShapeRenderer();
    			renderer.setSeriesShapesVisible(0, true);
    			renderer.setSeriesLinesVisible(0, true);
    			renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
    			plot.setBackgroundPaint(Color.WHITE);
    			plot1.setBackgroundPaint(Color.WHITE);
    			plot2.setBackgroundPaint(Color.WHITE);
    			plot.setRenderer(renderer);
    			plot1.setRenderer(renderer);
    			plot2.setRenderer(renderer);
    			
    			plot.setDomainGridlinesVisible(true);
    	        plot.setRangeGridlinesVisible(true);
    	 
    	        // Adjust gridline colors as needed
    	        plot.setDomainGridlinePaint(Color.BLACK);
    	        plot.setRangeGridlinePaint(Color.BLACK);
    	        plot.setOutlineStroke(new BasicStroke(0.0f));
    	        
    	        plot1.setDomainGridlinesVisible(true);
    	        plot1.setRangeGridlinesVisible(true);
    	 
    	        // Adjust gridline colors as needed
    	        plot1.setDomainGridlinePaint(Color.BLACK);
    	        plot1.setRangeGridlinePaint(Color.BLACK);
    	        plot1.setOutlineStroke(new BasicStroke(0.0f));
    	        
    	        plot2.setDomainGridlinesVisible(true);
    	        plot2.setRangeGridlinesVisible(true);
    	        plot2.setOutlineStroke(new BasicStroke(0.0f));
    	 
    	        // Adjust gridline colors as needed
    	        plot2.setDomainGridlinePaint(Color.BLACK);
    	        plot2.setRangeGridlinePaint(Color.BLACK);
    
    
    
    			//			for bar graph 
    			//			BarRenderer renderer = new BarRenderer()
    			//			renderer.setBarPainter(new StandardBarPainter());
    			//					plot.setRenderer(renderer);
    
    			// save the chart as image
    			// ChartUtilities.saveChartAsPNG(new File("chart.png"), chart, 500, 100);
    			// ChartUtilities.saveChartAsPNG(new File("chart1.png"), chart1, 500, 100);
    			// ChartUtilities.saveChartAsPNG(new File("chart2.png"), chart2, 500, 100);
    
    			// com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance("chart.png");
    			// com.itextpdf.text.Image image1 = com.itextpdf.text.Image.getInstance("chart1.png");
    			// com.itextpdf.text.Image image2 = com.itextpdf.text.Image.getInstance("chart2.png");
    
    
    			Paragraph paragraph=new Paragraph("Supplier Performance Rating FY 2023-24",FontFactory.getFont(FontFactory.HELVETICA_BOLD));
    			paragraph.setAlignment(Element.ALIGN_CENTER);
    			document.add(paragraph);
    
    			document.add(Chunk.NEWLINE);
    
    
    			// Arteria logo
    			Image img = Image.getInstance(new URL("https://3.imimg.com/data3/PH/FQ/IMFCP-1114011/data2-wy-mg-imfcp-1114011-logo-90x90.gif"));
    			img .scaleAbsolute(100f, 25f);
    			img.setAbsolutePosition(document.getPageSize().getWidth() - 550, document.getPageSize().getHeight() - 60);
    			document.add(img);
    
    			Image img1 = Image.getInstance(new URL("https://3.imimg.com/data3/PH/FQ/IMFCP-1114011/data2-wy-mg-imfcp-1114011-logo-90x90.gif"));
    			img1 .scaleAbsolute(100f, 25f);
    			img1.setAbsolutePosition(document.getPageSize().getWidth() - 150, document.getPageSize().getHeight() - 60);
    			document.add(img1);
    
    			//			PdfContentByte content = writer.getDirectContent();
    			//			content.moveTo(document.left(), 20); // Y-coordinate of the line
    			//			content.lineTo(document.right(), 20); // X-coordinate of the line
    			//			content.stroke();
    
    			//2nd table
    			PdfPTable table2 = new PdfPTable(6); // Number of columns in the table
    
    			table2.setWidthPercentage(100);
    
    			String []  headers={"Supplier Code","500024","Supplier Name","ANAND INDUSTRIES","Month","Jan'23"};
    			// Add table header
    
    			PdfPCell headerCell=new PdfPCell(); 
    			for(int i=0;i<6;i++)
    			{
    				headerCell = new PdfPCell(new Phrase(headers[i],getSmallFont3()));
    
    				headerCell.setFixedHeight(20f);
    				headerCell.setNoWrap(true); // the data doesn't wrap
    				headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    				headerCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    
    				table2.addCell(headerCell);
    
    			}
    
    			String []  rows={"Lumax Plant Code","1300","Lumax Plant Name","LIL Dharuhera(1300)","Date","31/01/2024"};
    
    
    			PdfPCell rowCell=new PdfPCell(); 
    			for(int i=0;i<6;i++)
    			{
    				rowCell = new PdfPCell(new Phrase(rows[i],getSmallFont()));
    
    				rowCell.setFixedHeight(20f);
    				rowCell.setNoWrap(true); // the data doesn't wrap
    				rowCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    				rowCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    				table2.addCell(rowCell);
    			}
    
    			document.add(table2);
    
    			document.add(new Paragraph());
    			document.add(new Paragraph("Kind Attention :-",getSmallFont2()));
    			document.add(new Paragraph(" Dear Supplier partner,",getSmallFont()));
    			document.add(new Paragraph(" Thank you for your continuous support.",getSmallFont()));
    			document.add(new Paragraph(" Following is the Quality & Delivery performance report of your organisation for current financial year.",getSmallFont()));
    
    			//			PdfContentByte content3 = writer.getDirectContent();
    			//			content3.moveTo(document.left(), 130); // Y-coordinate of the line
    			//			content3.lineTo(document.right(), 130); // X-coordinate of the line
    			//			content3.stroke();
    
    			Paragraph para=new Paragraph("Overall Supplier Rating Score Trend - Max Score 100%",getSmallFont1());
    			para.setAlignment(Element.ALIGN_CENTER);
    			document.add(para);
    			// document.add(image);
    
    			Paragraph para1=new Paragraph("Quality Score Trend - Max Score 70 (QAV)/Max Score 50(w/o QAV)",getSmallFont1());
    			para1.setAlignment(Element.ALIGN_CENTER);
    			document.add(para1);
    			// document.add(image1);
    
    			Paragraph para2=new Paragraph("Delivery Score Trend - Max Score 30",getSmallFont1());
    			para2.setAlignment(Element.ALIGN_CENTER);
    			document.add(para2);
    			// document.add(image2);
    
    			//			PdfContentByte content2 = writer.getDirectContent();
    			//			content2.moveTo(document.left(), 730); // Y-coordinate of the line
    			//			content2.lineTo(document.right(), 730); // X-coordinate of the line
    			//			content2.stroke();
    
    			PdfPTable table1 = new PdfPTable(12);
    
    			float[] columnWidths = {33f, 15f, 15f,15f,15f,15f,15f,15f,15f,15f,15f,15f}; 
               table1.setWidths(columnWidths);
    			table1.setWidthPercentage(100);
    
    			String []  headers1={"Description","Apr-23","May-23","Jun-23","Jul-23","Aug-23","Oct-23","Nov-23","Dec-23","Jan-24","Feb-24","Mar-24"};
    
    			PdfPCell headerCell1=new PdfPCell(); 
    			for(int i=0;i<12;i++)
    			{
    				headerCell1 = new PdfPCell(new Phrase(headers1[i],getSmallFont3()));
    
    				headerCell1.setFixedHeight(20f);
    				headerCell1.setNoWrap(true); // the data doesn't wrap
    				headerCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
    				headerCell1.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    				
    
    				table1.addCell(headerCell1);
    
    			}
    
    			String  []rows1={"QAV(20)","12.22","22.33","22.2","22.33","12.22","22.33","23.33","34.55","53.55","67.78","22.23","22.32"};
    			String  []rows2={"GR Processing(15)","Line/Stock Rejection(15)","QAV(20)","SQIR(10)","Response Time(10) ","Schedule vs Supply(25)","Pre. Freight Score(5)","Total Del. Score(30)","34.55","53.55","67.78","22.23","22.32"};
    			PdfPCell rowCell1=new PdfPCell(); 
    			int k=0;
    			//			Integer.parseInt(a);
    			for(int j=1;j<=10;j++)
    			{
    				for(int i=0;i<12;i++)
    				{
    					if(i==0)
    					{
    						rowCell1 = new PdfPCell(new Phrase(rows2[k],getSmallFont2()));
    						k++;
    					}
    					else
    					{
    						rowCell1 = new PdfPCell(new Phrase(rows1[i],getSmallFont3()));
    					}
    
    					rowCell1.setFixedHeight(15f);
    					rowCell1.setNoWrap(true); // the data doesn't wrap
    					rowCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
    					rowCell1.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    
    					table1.addCell(rowCell1);
    				}
    			}
    
    			document.add(table1);
    			document.add(new Paragraph(" Organisations scoring less than 90% need to submit the action plan on areas which have low score. ",getSmallFont1() ));
    			document.add(Chunk.NEWLINE);
    			document.add(new Paragraph(" Best Regards,",getSmallFont1() ));
    			document.add(new Paragraph(" Team Group Supplier Development",getSmallFont1() ));
    
    			PdfPTable table3 = new PdfPTable(3);
    
    			// Add table header
    			PdfPCell cell=new PdfPCell();
    
    			
    			String [] values={"Red (R)","Yellow (Y)","Green (G)","< 80%","≥ 80% to < 90%","≥ 90% to 100%"};
    			int m=0;
    			for(int j=1;j<=2;j++)
    			{
    				for(int i=0;i<3;i++)
    				{
    					cell = new PdfPCell(new Phrase(values[m],getSmallFont3()));
    
    					cell.setFixedHeight(20f);
    					cell.setNoWrap(true); // the data doesn't wrap
    					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    					cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    
    					table3.addCell(cell);
    					m++;
    				}
    			}
    
    			float totalTableWidth = 300f; // Adjust the width as needed
    			table3.setTotalWidth(totalTableWidth);
    			table3.setLockedWidth(true);
    
    			// Get the total height of the table
    			float totalTableHeight = table3.getTotalHeight();
    
    			// Calculate the position to align the table to the bottom-left
    			float xPosition = document.right()/2-30;
    			//			float yPosition = document.bottom()+60;
    
    			//			// Set the absolute position of the table
    			table3.writeSelectedRows(0, -1, xPosition, table2.getTotalHeight()+80 + totalTableHeight, writer.getDirectContent());
    
    			//				        document.(table3);
    
    			document.add(Chunk.NEWLINE);
    			document.add(Chunk.NEWLINE);
    
    			Paragraph paragraph2=new Paragraph("  This is the SAP Generated Report                          "+"                                                                         Grade (X) means no supply",getSmallFont1() );
    			paragraph2.setAlignment(Element.ALIGN_LEFT);
    			paragraph2.setSpacingAfter(20f);
    			document.add(paragraph2);
    
    			//	        Paragraph paragraph1=new Paragraph("Grade (X) means no supply");
    			//			paragraph1.setAlignment(Element.ALIGN_RIGHT);
    			//			document.add(paragraph1);
    
    
    			float lineWidth1 = 1f; // Adjust the line width as needed
    			float lineYPosition1 = document.bottom() + 38f; // Adjust the Y position as needed
    			float lineStartX1 = document.left();
    			float lineEndX1 = document.right();
    
    			PdfContentByte content1 = writer.getDirectContent();
    			content1.setLineWidth(lineWidth1);
    			content1.moveTo(lineStartX1, lineYPosition1);
    			content1.lineTo(lineEndX1, lineYPosition1);
    			content1.stroke();
    
    			float lineWidth2 = 1f; // Adjust the line width as needed
    			float lineYPosition2 = document.bottom() + 740f; // Adjust the Y position as needed
    			float lineStartX2 = document.left();
    			float lineEndX2 = document.right();
    
    			PdfContentByte content2 = writer.getDirectContent();
    			content2.setLineWidth(lineWidth2);
    			content2.moveTo(lineStartX2, lineYPosition2);
    			content2.lineTo(lineEndX2, lineYPosition2);
    			content2.stroke();
    
    			float lineWidth = 1f; // Adjust the line width as needed
    			float lineYPosition = document.bottom() + 390f; // Adjust the Y position as needed
    			float lineStartX = document.left();
    			float lineEndX = document.right();
    
    			PdfContentByte content = writer.getDirectContent();
    			content.setLineWidth(lineWidth);
    			content.moveTo(lineStartX, lineYPosition);
    			content.lineTo(lineEndX, lineYPosition);
    			content.stroke();
    
    			float lineWidth3 = 1f; // Adjust the line width as needed
    			float lineYPosition3 = document.bottom() + 513f; // Adjust the Y position as needed
    			float lineStartX3 = document.left();
    			float lineEndX3 = document.right();
    
    			PdfContentByte content3 = writer.getDirectContent();
    			content3.setLineWidth(lineWidth3);
    			content3.moveTo(lineStartX3, lineYPosition3);
    			content3.lineTo(lineEndX3, lineYPosition3);
    			content3.stroke();
    
    			//			float lineWidth4 = 1f; // Adjust the line width as needed
    			//			float lineYPosition4 = document.bottom() + 480f; // Adjust the Y position as needed
    			//			float lineStartX4 = document.left();
    			//			float lineEndX4 = document.right();
    			//
    			//			PdfContentByte content4 = writer.getDirectContent();
    			//			content4.setLineWidth(lineWidth4);
    			//			content4.moveTo(lineStartX4, lineYPosition4);
    			//			content4.lineTo(lineEndX4, lineYPosition4);
    			//			content4.stroke();
    
    			float lineWidth5 = 1f; // Adjust the line width as needed
    			float lineYPosition5 = document.bottom() + 635f; // Adjust the Y position as needed
    			float lineStartX5 = document.left();
    			float lineEndX5 = document.right();
    
    			PdfContentByte content5 = writer.getDirectContent();
    			content5.setLineWidth(lineWidth5);
    			content5.moveTo(lineStartX5, lineYPosition5);
    			content5.lineTo(lineEndX5, lineYPosition5);
    			content5.stroke();
    
    //			float lineWidth6 = 1f; // Adjust the line width as needed
    //			float lineYPosition6 = document.bottom() + 276f; // Adjust the Y position as needed
    //			float lineStartX6 = document.left();
    //			float lineEndX6 = document.right();
    //
    //			PdfContentByte content6 = writer.getDirectContent();
    //			content6.setLineWidth(lineWidth6);
    //			content6.moveTo(lineStartX6, lineYPosition6);
    //			content6.lineTo(lineEndX6, lineYPosition6);
    //			content6.stroke();
    
    			//			float lineWidth7 = 1f; // Adjust the line width as needed
    			//			float lineYPosition7 = document.bottom() + 220f; // Adjust the Y position as needed
    			//			float lineStartX7 = document.left();
    			//			float lineEndX7 = document.right();
    			//
    			//			PdfContentByte content7 = writer.getDirectContent();
    			//			content7.setLineWidth(lineWidth7);
    			//			content7.moveTo(lineStartX7, lineYPosition7);
    			//			content7.lineTo(lineEndX7, lineYPosition7);
    			//			content7.stroke();
    
    			document.close();  //close the document
    		}
    		catch (DocumentException | IOException e)
    		{
    			e.printStackTrace();
    		}
    
    		
    		
    //		Paragraph paragraph = new Paragraph();
    //		 
    //		            // Add text aligned to the left
    //		            paragraph.add(new Chunk("Left Text", new Font(Font.FontFamily.TIMES_ROMAN, 12)));
    //		 
    //		            // Add some spacing between left and right text
    //		            paragraph.add(Chunk.createWhitespace(30));
    //		 
    //		            // Add text aligned to the right
    //		            paragraph.add(new Chunk("Right Text", new Font(Font.FontFamily.TIMES_ROMAN, 12)));
    //		            paragraph.setAlignment(Element.ALIGN_LEFT); // Align the whole paragraph to the left
    
    //				to Send email
    	// 	try {
        //    	CommonUtils1 commonUtils = new CommonUtils1();
        //    	String Username = CommonUtils1.getODataDestinationProperties("User","TestEmail");
    	// 		String Password = CommonUtils1.getODataDestinationProperties("Password","TestEmail");
    	// 		Properties emailProperties = getEmailProperties();
    	// 		Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
    	// 				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
    	// 					return new PasswordAuthentication(Username,Password);
    	// 				}
    	// 			});
        //        MimeMessage message = new MimeMessage(session);
        //        message.setFrom(new InternetAddress(Username));
        //        message.addRecipient(Message.RecipientType.TO, new InternetAddress("mohanraj.v@arterialabs.com"));
        //        message.setSubject("PDF Attachment");
        //        MimeBodyPart attachmentPart = new MimeBodyPart();
        //        attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(baos.toByteArray(), "application/pdf")));
        //        attachmentPart.setFileName("Arteria.pdf");
        //        Multipart multipart = new MimeMultipart();
        //        multipart.addBodyPart(attachmentPart);
        //        message.setContent(multipart);
        //        Transport.send(message);
        //        response.getWriter().println("Email sent successfully with PDF attachment.");
        //    } 
    	// 	catch (Exception e) 
    	// 	{
        //        e.printStackTrace();
        //        response.getWriter().println("Failed to send email: " + e.getMessage());
        //    }
    		
    			}
    		
    		public Properties getEmailProperties() throws Exception {
    			Properties properties = new Properties();
    			Properties emailProps = new Properties();
    			try {
    				properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
    				emailProps.put("mail.smtp.auth", properties.getProperty("mail.smtp.auth"));
    				emailProps.put("mail.smtp.starttls.enable", properties.getProperty("mail.smtp.starttls.enable"));
    				/* properties.put("mail.smtp.host", "74.208.5.2"); */
    				emailProps.put("mail.smtp.host", properties.getProperty("mail.smtp.host"));
    				emailProps.put("mail.smtp.port", properties.getProperty("mail.smtp.port"));
    	
    			} catch (Exception ex) {
    				throw ex;
    			}
    			return emailProps;
    	}
    
    	private static Font getSmallFont() 
    	{
    		return new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL, BaseColor.BLACK);
    	}
    
    	private static Font getSmallFont3() 
    	{
    		return new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD, BaseColor.BLACK);
    	}
    
    	private static Font getSmallFont2() 
    	{
    		return new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD, BaseColor.BLACK);
    	}
    
    	private static Font getSmallFont1() 
    	{
    		return new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLACK);
    	}
    
    	private PdfContentByte createBorder(PdfWriter writer)
    	{
    		PdfContentByte border = writer.getDirectContent();
    		//1st parameter left side 
    		//2nd parameter bottom side
    		//3rd paramter right side
    		//4th paramter top side
    		border.setLineWidth(2f);
    		border.rectangle(35, 46, PageSize.A4.getWidth() - 70, PageSize.A4.getHeight() - 59);
    		return border;
    	}
    @Bean
	public ServletRegistrationBean<PdfTrainig> PdfTrainigBean() {
		ServletRegistrationBean<PdfTrainig> bean = new ServletRegistrationBean<>(new PdfTrainig(), "/PdfTrainig");
		return bean;
	}
}

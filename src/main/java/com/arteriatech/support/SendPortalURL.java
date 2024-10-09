package com.arteriatech.support;

import java.io.IOException;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SendPortalURL extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PLATFORM_EMAIL = "PlatformEmail";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cpGuid = "", oDataUrl = "", userName = "", password = "", userPass = "", executeURL = "";
		CommonUtils commonUtils = new CommonUtils();
		String prospectName = "", recipientEmailId = "";
		boolean debug = false;
		String portalUrl = "";
		try {
			if (request.getParameter("debug") != null && request.getParameter("debug").equalsIgnoreCase("true")) {
				debug = true;
			}
			if (request.getParameter("CPGUID") != null && !request.getParameter("CPGUID").equalsIgnoreCase("")) {
				cpGuid = request.getParameter("CPGUID");
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "SSGWHANA");
				portalUrl = commonUtils.getODataDestinationProperties("PortalURL", "SSGWHANA");
				if (debug) {
					response.getWriter().println("Odata Url:" + oDataUrl);
					response.getWriter().println("Received CPGUID:" + cpGuid);
					response.getWriter().println("User Name:" + userName);
				}
				userPass = userName + ":" + password;
				executeURL = oDataUrl + "SS_CP?$filter=CP_GUID%20eq%20%27" + cpGuid + "%27";
				if (debug) {
					response.getWriter().println("execute Url:" + executeURL);
				}
				JsonObject sscpObject = commonUtils.executeURL(executeURL, userPass, response);
				if (debug) {
					response.getWriter().println("SS_CP Object:" + sscpObject);
				}
				if (sscpObject != null && !sscpObject.isJsonNull()) {
					JsonArray ssObjArray = sscpObject.get("d").getAsJsonObject().get("results").getAsJsonArray();
					if (!ssObjArray.isJsonNull() && ssObjArray.size() > 0) {
						JsonObject ssObj = ssObjArray.get(0).getAsJsonObject();
						if (ssObj.has("NAME") && !ssObj.get("NAME").isJsonNull()
								&& !ssObj.get("NAME").getAsString().equalsIgnoreCase("")) {
							prospectName = ssObj.get("NAME").getAsString();
						}
						if (ssObj.has("EMAILID") && !ssObj.get("EMAILID").isJsonNull()
								&& !ssObj.get("EMAILID").getAsString().equalsIgnoreCase("")) {
							recipientEmailId = ssObj.get("EMAILID").getAsString();
						}
						if (!prospectName.equalsIgnoreCase("")) {
							if (!recipientEmailId.equalsIgnoreCase("")) {
								portalUrl=portalUrl+cpGuid;
								sendEmail(debug, recipientEmailId, prospectName, portalUrl, response);
							} else {
								JsonObject result = new JsonObject();
								result.addProperty("Message", "RecipientEmailId Not Found");
								result.addProperty("ErrorCode", "J002");
								result.addProperty("Status", "000002");
								response.getWriter().println(result);
							}
						} else {
							JsonObject result = new JsonObject();
							result.addProperty("Message", "ProspectName Not Found");
							result.addProperty("ErrorCode", "J002");
							result.addProperty("Status", "000002");
							response.getWriter().println(result);
						}
					} else {
						JsonObject result = new JsonObject();
						result.addProperty("Message", "Record  Not Exist for The CPGUID:" + cpGuid);
						result.addProperty("ErrorCode", "J002");
						result.addProperty("Status", "000002");
						response.getWriter().println(result);
					}
				} else {
					JsonObject result = new JsonObject();
					result.addProperty("Message", "Record  Not Exist for The CPGUID:" + cpGuid);
					result.addProperty("ErrorCode", "J002");
					result.addProperty("Status", "000002");
					response.getWriter().println(result);
				}

			} else {
				JsonObject result = new JsonObject();
				result.addProperty("Message", "CPGUID Missing in the Input Payload");
				result.addProperty("ErrorCode", "J002");
				result.addProperty("Status", "000002");
				response.getWriter().println(result);
			}

		} catch (Exception ex) {
			JsonObject result = new JsonObject();
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
		}

	}

	private void sendEmail(boolean debug, String recipientEmailId, String prospectName, String portalUrl,
			HttpServletResponse response) throws IOException, Exception {
		JsonObject result = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		String emailSubject = "Reg : RSPL On Boarding URL Link";
		try {
			final String userName = commonUtils.getODataDestinationProperties("emailid", PLATFORM_EMAIL);
			final String passWord = commonUtils.getODataDestinationProperties("Password", PLATFORM_EMAIL);
			Properties emailProperties = getEmailProperties();
			Session session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, passWord);
				}
			});
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(userName));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmailId));
			msg.setSubject(emailSubject);
			Multipart emailContent = new MimeMultipart();
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setText("Dear " + prospectName + " ,"
					+ "\n\nYou are requested to provide your own details on our \"Dealer Portal\" for on boarding into RSPL.\n\nKindly use the following URL.\n\n"
					+ portalUrl
					+ "\n\nFor any query, kindly connect with our sales team.\n\nThank You \n\nRSPL Team\n\nNote : This is an auto generated email. Please do not reply.");
			emailContent.addBodyPart(textBodyPart);
			msg.setContent(emailContent);
			Transport.send(msg);
			result.addProperty("Message", "Mail Sent Successfully");
			result.addProperty("ErrorCode", "");
			result.addProperty("Status", "000001");
			response.getWriter().println(result);
		} catch (SendFailedException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
		} catch (MessagingException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
		} catch (IOException ex) {
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			result.addProperty("Exception", ex.getClass().getCanonicalName());
			result.addProperty("Message",
					ex.getClass().getCanonicalName() + "--->" + ex.getMessage() + "--->" + buffer.toString());
			result.addProperty("Status", "000002");
			result.addProperty("ErrorCode", "J002");
			response.getWriter().println(result);
		}
	}

	public Properties getEmailProperties() throws Exception {
		Properties properties = new Properties();
		Properties emailProps = new Properties();
		try {
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			emailProps.put("mail.smtp.auth", properties.getProperty("mail.smtp.auth"));
			emailProps.put("mail.smtp.starttls.enable", properties.getProperty("mail.smtp.starttls.enable"));
			emailProps.put("mail.smtp.host", properties.getProperty("mail.smtp.host"));
			emailProps.put("mail.smtp.port", properties.getProperty("mail.smtp.port"));
		} catch (Exception ex) {
			throw ex;
		}
		return emailProps;
	}

}

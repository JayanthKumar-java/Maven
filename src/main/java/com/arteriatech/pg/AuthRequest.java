package com.arteriatech.pg;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Servlet implementation class AuthRequest
 */
@WebServlet("/AuthRequest")
public class AuthRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String secretKey = "cBnfZX3atf4PQRggFAGQjzCrZ9XeFgAa";
	public static byte[] ivBytes = "1234567887654321".getBytes();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
//		String authRequest = "{\"TXNTYPE\": \"AUTREQ\",\"DLRCODE\": \"138851\",\"ORDERNO\": \"ArvindTestOrder100\",\"LOANAMT\": \"10000\",\"TNC\": \"Y\",\"CN\": \"\",\"MOBNO\": \"8959157646\",\"OTP\": \"999999\",\"TENURE\": \"5\",\"PINCODE\": \"411012\",\"RRN\": \"Arvindjxdrtyujdrtojh\",\"DLRVKEY\": \"8491843317222182\",\"SCHEMID\":\"\"}";
		String authRequest = "", mandatoryCheckMsg="";
		String encodedValue = "", sealValue="", decodedValue="", encryptedResponse="";
		String txnType="", authReq="", dealerCode="", orderNo="", loanAmt="", tnc="", cn="", mobileNo="", otp="", tenure="", pincode="", rrn="", dlrvKey="", schemeID="";
		CommonUtils commonUtils = new CommonUtils();
		boolean debug=false;
		if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			debug = true;
		
		try{
			authRequest = commonUtils.getGetBody(request, response);
			JsonParser jsonParser = new JsonParser();
			JsonObject bflObj = (JsonObject)jsonParser.parse(authRequest);
			encodedValue = commonUtils.aesEncode(bflObj.toString(), secretKey, ivBytes);
			if(debug)
				response.getWriter().println("encodedValue: "+encodedValue);
			
			sealValue = commonUtils.getSealValue(encodedValue+secretKey);
			if(debug)
				response.getWriter().println("sealValue: "+sealValue);
			
			/*if(inputPayload.has("UserID") && inputPayload.get("UserID").getAsString().trim().length() > 0){
				userID = inputPayload.get("UserID").getAsString().trim();
			}else{
				mandatoryCheckMsg = mandatoryCheckMsg+"UserID";
			}*/
			
			
			
			
//			decodedValue = AES_Decode(encodedValue, secretKey);
//			response.getWriter().println("decodedValue before calling POST: "+decodedValue);
			
			Client client=Client.create();
			WebResource webResource = client.resource("https://bfl2.in.worldline.com/worldlineinterfaceecom/WorldlineInterfaceEcom.svc/MPOSRequest");
			encryptedResponse = webResource.header("SealValue", sealValue).type("application/json").post(String.class, "\""+encodedValue+"\"");
			
			if(response!=null)
	        {
				response.getWriter().println("encryptedResponse: "+encryptedResponse);
				encryptedResponse = encryptedResponse.replace("\"", "");
				response.getWriter().println("encryptedResponse after removing double quotes: "+encryptedResponse);
			    response.getWriter().println("input for decryption: "+encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")));
	        	
	        	decodedValue=AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),secretKey);
	        	response.getWriter().println("decodedValue after calling POST: "+decodedValue);
		    }
		}catch(Exception e) {
			e.printStackTrace();
//			System.out.println(e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
	    	response.getWriter().println("Trace: "+buffer.toString());
			response.getWriter().println("getMessage: "+e.getMessage());
			response.getWriter().println("getClass: "+e.getClass().getSimpleName());
			response.getWriter().println("getCause: "+e.getCause());
		}
	}
	
	public static String AES_Decode(String encryptedText, String key)
			throws java.io.UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException 
	{
//		System.out.println(key);
		byte[] textBytes = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedText);
//		byte[] textBytes = Base64.getDecoder().decode(encryptedText);
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
//		return new String(cipher.doFinal(textBytes), "UTF-8");
		return new String(cipher.doFinal(textBytes), "UTF-8");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
}

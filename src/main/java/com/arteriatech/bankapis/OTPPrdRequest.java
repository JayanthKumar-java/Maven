package com.arteriatech.bankapis;

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

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Servlet implementation class OTPPrdRequest
 */
@WebServlet("/OTPPrdRequest")
public class OTPPrdRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String secretKey = "cBnfZX3atf4PQRggFAGQjzCrZ9XeFgAa";
	public static byte[] ivBytes = "1234567887654321".getBytes();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OTPPrdRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		String bflOtp="{\"TXNTYPE\":\"OTPREQ\",\"CN\":\"\",\"RRN\":\"Arvindpoghvbdfjo\",\"ORDERNO\":\"ArvindTestOrder100\",\"DLRCODE\":\"138851\",\"DLRVKEY\":\"8491843317222182\",\"MOBNO\":\"8959157646\",\"SALETYPE\":\"AUTH\"}";
		String encodedValue = "", sealValue="", decodedValue="", encryptedResponse="";
		CommonUtils commonUtils = new CommonUtils();
		try{
			JsonParser jsonParser = new JsonParser();
			JsonObject bflObj = (JsonObject)jsonParser.parse(bflOtp);
			bflObj.remove("RRN");
			bflObj.addProperty("RRN", commonUtils.generateGUID(16));
			response.getWriter().println("bflObj: "+bflObj);
			
			encodedValue = AES_Encode(bflObj.toString(), secretKey);
//			encodedValue = AES_Encode(cookie, secretKey);
			response.getWriter().println("encodedValue: "+encodedValue);
			sealValue = getSealValue(encodedValue+secretKey);
			response.getWriter().println("sealValue: "+sealValue);
			
//			decodedValue = AES_Decode(finalEncrReq.substring(0, finalEncrReq.lastIndexOf("|")), secretKey);
			decodedValue = AES_Decode(encodedValue, secretKey);
			response.getWriter().println("decodedValue before calling POST: "+decodedValue);
			
			Client client=Client.create();
			WebResource webResource = client.resource("https://bfl2.in.worldline.com/worldlineinterfaceecom/WorldlineInterfaceEcom.svc/MPOSRequest");
			encryptedResponse = webResource.header("SealValue", sealValue).type("application/json").post(String.class, "\""+encodedValue+"\"");
			
			if(encryptedResponse!=null)
	        {
				response.getWriter().println("encryptedResponse: "+encryptedResponse);
				encryptedResponse = encryptedResponse.replace("\"", "");
				response.getWriter().println("encryptedResponse after removing double quotes: "+encryptedResponse);
			    response.getWriter().println("input for decryption: "+encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")));
	        	
	        	decodedValue=AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),secretKey);
	        	response.getWriter().println("decodedValue after calling POST: "+decodedValue);
		    }
	        else
	        {
	        	response.getWriter().println("Getting Empty response from ATOS service");
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public static String AES_Encode(String jsonText, String key)
			throws java.io.UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException 
	{
		byte[] textBytes = jsonText.getBytes("UTF-8");
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
//		return Base64.encodeBase64String(cipher.doFinal(textBytes));
		return Base64.getEncoder().encodeToString(cipher.doFinal(textBytes));
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
	
	public static String getSealValue(String jsonEncAndSecKey)
	{
		MessageDigest md;
		String hashText = null;
		try 
		{
			md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(jsonEncAndSecKey.getBytes());
			hashText = String.format("%032x", new Object[] { new BigInteger(1,
					md.digest()) });
		}
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return hashText;
	}
}

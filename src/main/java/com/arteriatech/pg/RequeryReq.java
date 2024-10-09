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
 * Servlet implementation class RequeryReq
 */
@WebServlet("/RequeryReq")
public class RequeryReq extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String reqSecretKey = "sOnfGB3atf4UYZggYGQQjzCrZ9XeUgNn";
	public static byte[] ivBytes = "1234567887654321".getBytes();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RequeryReq() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		String requeryPayload = "{\"DEALERID\":\"138851\",\"REQID\":\"ArvindAUTHRequery1\",\"VALKEY\":\"8491843317222182\",\"REQUERYID\":\"Arvindjxdrtyujdrtojh\",\"ACQCHNLID\":\"10\"}";
		String encodedValue = "", sealValue="", decodedValue="", encryptedResponse="";
		try{
			JsonParser jsonParser = new JsonParser();
			JsonObject bflObj = (JsonObject)jsonParser.parse(requeryPayload);
			CommonUtils commonUtils = new CommonUtils(); 
			bflObj.remove("REQID");
			bflObj.addProperty("REQID", commonUtils.generateGUID(16));
			
			encodedValue = AES_Encode(bflObj.toString(), reqSecretKey);
//			encodedValue = AES_Encode(cookie, secretKey);
			response.getWriter().println("encodedValue: "+encodedValue);
			sealValue = getSealValue(encodedValue+reqSecretKey);
			response.getWriter().println("sealValue: "+sealValue);
			
//			decodedValue = AES_Decode(finalEncrReq.substring(0, finalEncrReq.lastIndexOf("|")), secretKey);
			decodedValue = AES_Decode(encodedValue, reqSecretKey);
			response.getWriter().println("decodedValue before calling POST: "+decodedValue);
			
			Client client=Client.create();
			WebResource webResource = client.resource("https://bfl2.in.worldline.com/WorldlineInterfaceEnqRequery/WorldlineInterfaceEnhanceRequery.svc/ENQRequest");
			encryptedResponse = webResource.header("SealValue", sealValue).type("application/json").post(String.class, "\""+encodedValue+"\"");
			
			if(response!=null)
	        {
				response.getWriter().println("encryptedResponse: "+encryptedResponse);
				encryptedResponse = encryptedResponse.replace("\"", "");
				response.getWriter().println("encryptedResponse after removing double quotes: "+encryptedResponse);
			    response.getWriter().println("input for decryption: "+encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")));
	        	
	        	decodedValue=AES_Decode(encryptedResponse.substring(0,encryptedResponse.lastIndexOf("|")),reqSecretKey);
	        	response.getWriter().println("decodedValue after calling POST: "+decodedValue);
	        	
	        	JsonParser bflResponseParser = new JsonParser();
				JsonObject bflResponseJson = (JsonObject)bflResponseParser.parse(decodedValue);
				response.getWriter().println("bflResponseJson: "+bflResponseJson);
				
				response.getWriter().println("bflResponseJson: "+bflResponseJson.get("ENQINFO").getAsJsonArray().get(0).getAsJsonObject());
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

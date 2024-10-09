package com.arteriatech.pg;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class StatusCall extends HttpServlet{
	public static SSLSocketFactory s_sslSocketFactory = null;
	public static X509TrustManager s_x509TrustManager = null;
	private static String StatusURL = "/ISGPay/Status";
	private static String rcvURL = "/ISGPay/RCV";
	private static boolean useProxy = false;
	private static String proxyHost = "";
	private static String proxyPort = "";
	private static int vpc_Port = 8443;//UAT
//	private static int vpc_Port = 443;//Live
	
	public static void main(String[] args)
	{
		Map<String, String> mapResponse = new LinkedHashMap<String, String>();
		String merchantCode="101000000000781", bankID="000004", passCode="SVPL4257", terminalID="10100781", txnType="Status"; // UAT
//		String merchantCode="120000000002151", bankID="000004", passCode="ZBGQ9324", terminalID="11002151", txnType="Status"; // Live
		StatusURL = "https://sandbox.isgpay.com" + StatusURL; // UAT
		rcvURL = "https://sandbox.isgpay.com" + rcvURL; // UAT
//		StatusURL = "https://isgpay.com" + StatusURL; // LIVE
//		rcvURL = "https://isgpay.com" + rcvURL; // LIVE
		try
		{
			//Yes Bank New Code - 27-Aug
//			if (request != null) 
			{

				/*if (request.getParameter("MerchantId") == null || "".equals(request.getParameter("MerchantId"))) {
					throw new Exception("Merchant ID Not Found In Request!!!");
				}
				if (request.getParameter("TerminalId") == null || "".equals(request.getParameter("TerminalId"))) {
					throw new Exception("Terminal ID Not Found In Request!!!");
				}
				if (request.getParameter("BankId") == null || "".equals(request.getParameter("BankId"))) {
					throw new Exception("Bank ID Not Found In Request!!!");
				}*/
				
				/*if (merchantCode == null || "".equals(merchantCode)) {
					throw new Exception("Merchant ID Not Found In Request!!!");
				}
				if (terminalID == null || "".equals(terminalID)) {
					throw new Exception("Terminal ID Not Found In Request!!!");
				}
				if (bankID == null || "".equals(bankID)) {
					throw new Exception("Bank ID Not Found In Request!!!");
				}

				Hashtable<String, String> pgParams = new Hashtable<String, String>();

				pgParams.put("TxnRefNo","000000000001402");
				pgParams.put("MerchantId",merchantCode);
				pgParams.put("BankId",bankID);
				pgParams.put("PassCode",passCode);
				pgParams.put("TerminalId",terminalID);
				pgParams.put("TxnType",txnType);
				Enumeration e = pgParams.keys();
				
				Map requestFields = new HashMap();
				while(e.hasMoreElements()){
					String fieldName = (String) e.nextElement();
					String fieldValue = pgParams.get(fieldName);
					System.out.println(fieldName+"="+fieldValue);
					if ((fieldValue != null) && (fieldValue.length() > 0)) {
						requestFields.put(fieldName, fieldValue);
					}
				}
				
				System.out.println("requestFields doStatusQuery==="+requestFields);
				requestFields.remove("Title");
				requestFields.remove("SubButL");
//				String SECURE_SECRET= "88DEC9861F2CC0AE69DE126D92C4EED2"; // LIVE
				String SECURE_SECRET= "E59CD2BF6F4D86B5FB3897A680E0DD3E"; // UAT

				if (SECURE_SECRET != null && SECURE_SECRET.length() > 0) {
					String secureHash = hashAllFields(requestFields, SECURE_SECRET);

					if ("".equals(secureHash)) {
						throw new Exception("Problem during Hashing....");
					}
					requestFields.put("SecureHash", secureHash);
					String postData = createPostDataFromMap(requestFields);
					mapResponse = doPostRequest(StatusURL, postData);
					
					System.out.println("mapResponse: "+mapResponse);
					
					Set<String> keys = mapResponse.keySet();
					for(String k:keys){
			            System.out.println(k+" -- "+mapResponse.get(k));
			        }
				} else {
					throw new Exception("SecureSecret can not be null or empty!!!");
				}*/
			} 

			//Yes Bank New Code - 24-Aug
			/*String merchantCode="101000000000781", bankID="000004", passCode="SVPL4257", terminalID="10100781", txnType="Status";
			StatusURL = "https://sandbox.isgpay.com" + StatusURL;
			rcvURL = "https://sandbox.isgpay.com" + rcvURL;
				

			if (merchantCode == null || "".equals(merchantCode)) {
				throw new Exception("Merchant ID Not Found In Request!!!");
			}
			if (terminalID == null || "".equals(terminalID)) {
				throw new Exception("Terminal ID Not Found In Request!!!");
			}
			if (bankID == null || "".equals(bankID)) {
				throw new Exception("Bank ID Not Found In Request!!!");
			}

			Hashtable<String, String> pgParams = new Hashtable<String, String>();

			pgParams.put("TxnRefNo","000000000000192");
			pgParams.put("MerchantId",merchantCode);
			pgParams.put("BankId",bankID);
			pgParams.put("PassCode",passCode);
			pgParams.put("TerminalId",terminalID);
			pgParams.put("TxnType",txnType);
			Enumeration e = pgParams.keys();

			Map requestFields = new HashMap();
			while(e.hasMoreElements()){
				String fieldName = (String) e.nextElement();
				String fieldValue = pgParams.get(fieldName);
				System.out.println(fieldName+"="+fieldValue);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					requestFields.put(fieldName, fieldValue);
				}
			}

			System.out.println("requestFields doStatusQuery==="+requestFields);
			requestFields.remove("Title");
			requestFields.remove("SubButL");
			String SECURE_SECRET= "E59CD2BF6F4D86B5FB3897A680E0DD3E";

			if (SECURE_SECRET != null && SECURE_SECRET.length() > 0) {
				String secureHash = hashAllFields(requestFields, SECURE_SECRET);

				if ("".equals(secureHash)) {
					throw new Exception("Problem during Hashing....");
				}
				requestFields.put("SecureHash", secureHash);
				String postData = createPostDataFromMap(requestFields);
				mapResponse = doPostRequest(StatusURL, postData);

				//										Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				for (Map.Entry<String, String> entry : mapResponse.entrySet()) {
					System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
				}
			} else {
				throw new Exception("SecureSecret can not be null or empty!!!");

			}*/
							
//				else {
//								throw new Exception("Error Parsing Request!!!");
//									}
//				} 
//		catch (Exception ex) {
//								System.out.println("Exception Occured in StatusResponse.doStatusQuery(HttpServletRequest)::" + ex);
//					}
//			return mapResponse;
			
			
			//Yes Bank Old Code
			/*String SECURE_SECRET= "E59CD2BF6F4D86B5FB3897A680E0DD3E";//Add SALT here
			String vpcURL = "https://sandbox.isgpay.com:8443/ISGPay/Status";//Add url here
			String merchantCode="101000000000781", bankID="000004", passCode="SVPL4257", terminalID="10100781", txnType="Status";
			boolean useProxy = false;
		    String proxyHost = "";
		    int proxyPort = 80;
			Hashtable<String, String> pgParams = new Hashtable<String, String>();

			pgParams.put("TxnRefNo","000000000000192");
			pgParams.put("MerchantId",merchantCode);
			pgParams.put("BankId",bankID);
			pgParams.put("PassCode",passCode);
			pgParams.put("TerminalId",terminalID);
			pgParams.put("TxnType",txnType);
			Enumeration e = pgParams.keys();
	        
			Map requestFields = new HashMap();
			while(e.hasMoreElements()){
				String fieldName = (String) e.nextElement();
				String fieldValue = pgParams.get(fieldName);
				System.out.println(fieldName+"="+fieldValue);
		        if ((fieldValue != null) && (fieldValue.length() > 0)) {
		            requestFields.put(fieldName, fieldValue);
		        }
			}

			System.out.println("SECURE_SECRET= "+SECURE_SECRET);
		    if (SECURE_SECRET != null && SECURE_SECRET.length() > 0) {
		        String secureHash = hashAllFields(requestFields,SECURE_SECRET);
		        requestFields.put("SecureHash", secureHash);
		    }
		    
		    String postData = createPostDataFromMap(requestFields);
		    System.out.println("postData:"+postData);
		    String resQS = "";
		    String message = "";

		    try {
		        // create a URL connection to the ePP Payment Client
		    	System.out.println("vpcURL: "+vpcURL);
		    	System.out.println("postData: "+postData);
		    	System.out.println("useProxy: "+useProxy);
		    	System.out.println("proxyHost: "+proxyHost);
		    	System.out.println("proxyPort: "+proxyPort);
		        resQS = doPostRequest(vpcURL, postData, useProxy, proxyHost, proxyPort);
		        System.out.println("resQS: "+resQS);
		    } catch (Exception ex) {
		        // The response is an error message so generate an Error Page
		        message = ex.toString();
		    } //try-catch

		    
		    Map responseFields = createMapFromResponse(resQS);
		 // Standard Receipt Data
		    String TxnRefNo          = null2unknown("TxnRefNo", responseFields);
		    String Amount          = null2unknown("Amount", responseFields);
		    String TerminalId         = null2unknown("TerminalId", responseFields);
		    String MerchantId         = null2unknown("MerchantId", responseFields);
		    String BankId         = null2unknown("BankId", responseFields);
		    String TxnType        = null2unknown("TxnType", responseFields);
		    String RetRefNo       = null2unknown("RetRefNo", responseFields);
		    String AuthCode      = null2unknown("AuthCode", responseFields);
		    String Message   = null2unknown("Message", responseFields);
		    String ResponseCode   = null2unknown("ResponseCode", responseFields);
		    String RefundedAmount= null2unknown("RefundedAmount", responseFields);
		    System.out.println("RefundedAmount===== "+RefundedAmount);
		    System.out.println("TxnRefNo===== "+TxnRefNo);
		    System.out.println("Amount===== "+Amount);
		    System.out.println("TerminalId===== "+TerminalId);
		    System.out.println("MerchantId===== "+MerchantId);
		    System.out.println("BankId===== "+BankId);
		    System.out.println("TxnType===== "+TxnType);
		    System.out.println("RetRefNo===== "+RetRefNo);
		    System.out.println("AuthCode===== "+AuthCode);
		    System.out.println("Message===== "+Message);
		    System.out.println("ResponseCode===== "+ResponseCode);*/
			
			/*
			 * Axis Bank
			 * 
			 * */
			String decryptedData="", keyValue="", version="", corporateID="", type="", referenceId="", customerRefNo="", bankRefNo="", checkSumInput="", checksum="", parameterData="", encryptedData="", encryptionKey = "";
			boolean isParamMissing = false;
			version = "1.0";
			corporateID = "3621";
			type = "TEST";
			referenceId = "000000000000411";
			customerRefNo = "1100196";
			bankRefNo = "";
			
			if(type.equalsIgnoreCase("TEST"))
			{
				encryptionKey = "axisbank12345678";
				keyValue = "axis";
			}
			else
			{
				encryptionKey = "J3j@KkQ4#Tr5b!uz";
				keyValue = "y#Ct";
			}
			System.out.println("encryptionKey: "+encryptionKey);
			System.out.println("keyValue: "+keyValue);
			System.out.println("referenceId: "+referenceId);
			System.out.println("customerRefNo: "+customerRefNo);
			
			if((null == version || version.equalsIgnoreCase(""))
				||(null == corporateID || corporateID.equalsIgnoreCase(""))
				||(null == type || type.equalsIgnoreCase(""))
				||(null == referenceId || referenceId.equalsIgnoreCase(""))
				||(null == customerRefNo || customerRefNo.equalsIgnoreCase("")))
			{
				isParamMissing = true;
			}
			
//			System.out.println("bankRefNo: "+bankRefNo);
			String strUrl = "https://uat-etendering.axisbank.co.in/easypay2.0/frontend/index.php/api/enquiry?i=";
			if(! isParamMissing)
			{
//				System.out.println("isParamMissing: "+isParamMissing);
				checkSumInput = corporateID+referenceId+customerRefNo+keyValue;
				checksum = generateCheckSum(checkSumInput);
				System.out.println("checksum: "+checksum);
				
				parameterData = parameterData+"CID="+corporateID+"&RID="+referenceId+"&CRN="+customerRefNo+"&VER="+version+"&TYP="+type+"&BRN="+bankRefNo+"&CKS="+checksum;
				System.out.println("payLoad: "+parameterData);
				
				encryptedData = encryptDataForAxisPG(parameterData, encryptionKey);
				System.out.println("encryptedData: "+encryptedData);
			
				URL url = new URL(strUrl+encryptedData);
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.setRequestMethod("POST");
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, new java.security.SecureRandom()); 
				con.setSSLSocketFactory(sc.getSocketFactory());

				System.out.println("getRequestMethod: "+con.getRequestMethod());

				System.out.println("\n\n****** Response of the URL ********");			
				   BufferedReader br = 
					new BufferedReader(
						new InputStreamReader(con.getInputStream()));
							
				   String input;
							
				   while ((input = br.readLine()) != null){
				      System.out.println("Response: "+input);
				      decryptedData = decryptDataForAxisPG(input, encryptionKey);
						System.out.println("decryptedData: "+decryptedData);
				   }
				   
				   br.close();
				
				   
				   String[] resSplitResult = decryptedData.split("\\&");
					String brn="", txnStatus="", remarks="", aggrTxnID="", txnDateTime="", pymtMode="", txnID="", txnSessionID="", versionNo="", resMerchantCode="", 
							resPGType="", customerNo="", currency="", txnAmount="", checkSum="", resParamName="", resParamValue="";
					for(String s : resSplitResult)
					{
						resParamName = s.substring(0, s.indexOf("="));
						resParamValue = s.substring(s.indexOf("=")+1,s.length());
			        	
			        	if(resParamName.equalsIgnoreCase("BRN"))
			        	{
			        		brn = resParamValue;
			        		System.out.println("bankRefNo: "+brn);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("STC"))
			        	{
			        		txnStatus = resParamValue;
			        		System.out.println("txnStatus: "+txnStatus);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("RMK"))
			        	{
			        		remarks = resParamValue;
			        		System.out.println("remarks: "+remarks);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("TRN"))
			        	{
			        		aggrTxnID = resParamValue;
			        		System.out.println("aggrTxnID: "+aggrTxnID);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("TET"))
			        	{
			        		txnDateTime = resParamValue;
			        		System.out.println("txnDateTime: "+txnDateTime);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("PMD"))
			        	{
			        		pymtMode = resParamValue;
			        		System.out.println("pymtMode: "+pymtMode);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("RID"))
			        	{
			        		txnID = resParamValue;
			        		System.out.println("txnID: "+txnID);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("VER"))
			        	{
			        		versionNo = resParamValue;
			        		System.out.println("versionNo: "+versionNo);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CID"))
			        	{
			        		resMerchantCode = resParamValue;
			        		System.out.println("resMerchantCode: "+resMerchantCode);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("TYP"))
			        	{
			        		resPGType = resParamValue;
			        		System.out.println("resPGType: "+resPGType);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CRN"))
			        	{
			        		customerNo = resParamValue;
			        		System.out.println("customerNo: "+customerNo);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CNY"))
			        	{
			        		currency = resParamValue;
			        		System.out.println("currency: "+currency);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("AMT"))
			        	{
			        		txnAmount = resParamValue;
			        		System.out.println("txnAmount: "+txnAmount);
			        	}
			        	
			        	if(resParamName.equalsIgnoreCase("CKS"))
			        	{
			        		checkSum = resParamValue;
			        		System.out.println("checkSum: "+checkSum);
			        	}
					}
					
					JsonObject result = new JsonObject();
					result.addProperty("brn", brn);
					result.addProperty("txnStatus", txnStatus);
					result.addProperty("remarks", remarks);
					result.addProperty("aggrTxnID", aggrTxnID);
					result.addProperty("txnDateTime", txnDateTime);
					result.addProperty("pymtMode", pymtMode);
					result.addProperty("txnID", txnID);
					result.addProperty("versionNo", versionNo);
					result.addProperty("resMerchantCode", resMerchantCode);
					result.addProperty("resPGType", resPGType);
					result.addProperty("txnAmount", txnAmount);
					result.addProperty("currency", currency);
					result.addProperty("customerNo", customerNo);
					System.out.println(new Gson().toJson(result));
				   
			}
		}
		catch (Exception e) {
//			System.out.println(e.printStackTrace());
			e.printStackTrace();
		}
	}
	
	public static String generateCheckSum(String checkSumInput) throws NoSuchAlgorithmException
	{
		StringBuffer hexString = null;
		try
		{
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(checkSumInput.getBytes());
	 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format method 2
	        hexString = new StringBuffer();
	    	for (int i=0;i<byteData.length;i++)
	    	{
	    		String hex=Integer.toHexString(0xff & byteData[i]);
	   	     	if(hex.length()==1) hexString.append('0');
	   	     		hexString.append(hex);
	    	}
//	    	System.out.println("Hex format : " + hexString.toString());
	    	return  hexString.toString();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally
		{
			return  hexString.toString();
		}
	}
	
	public static String encryptDataForAxisPG(String parameterData, String encryptionKey)
	{
		byte[] encrypted = null;
		try
		{
			SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			encrypted = cipher.doFinal(parameterData.getBytes());
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		finally
		{
			return new String(Base64.getEncoder().encodeToString(encrypted));
//			return new String(Base64.getEncoder().encodeToString(encrypted));
		}
	}
	
	public static String decryptDataForAxisPG(String parameterData, String encryptionKey)
	{
		byte[] output = null;
		String resWholeParamString = "";
		try
		{
			SecretKeySpec skey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    cipher.init(Cipher.DECRYPT_MODE, skey);
		    output = cipher.doFinal(Base64.getDecoder().decode(parameterData));
		    System.out.println("output: "+output);
		    resWholeParamString = new String(output);
		    System.out.println("resWholeParamString: "+resWholeParamString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
//			System.out.println("String: "+new String(Base64.encode(output)));
			return resWholeParamString;
//			resWholeParamString = new String(output);
		}
	}

	public static String hashAllFields(Map fields,String SECURE_SECRET) {
		StringBuffer hexString = null;
		try
		{
			List fieldNames = new ArrayList(fields.keySet());
			Collections.sort(fieldNames);

			StringBuffer buf = new StringBuffer();
			buf.append(SECURE_SECRET);

			Iterator itr = fieldNames.iterator();

			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) fields.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {

					buf.append(fieldValue);

				}

			}
			System.out.println("client buf=" + buf);
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(buf.toString().getBytes("UTF-8"));
				hexString = new StringBuffer();
				for (int i = 0; i < hash.length; i++) {
					String hex = Integer.toHexString(0xff & hash[i]);
					if (hex.length() == 1)
						hexString.append('0');
					hexString.append(hex);
				}

				
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
        finally{
        	return hexString.toString();
        }
    }
	
	private static String createPostDataFromMap(Map fields) {
        StringBuffer buf = new StringBuffer();

        String ampersand = "";

        // append all fields in a data string
        for (Iterator i = fields.keySet().iterator(); i.hasNext(); ) {
            
            String key = (String)i.next();
            String value = (String)fields.get(key);
            System.out.println("Key: "+key+"---- Value:"+value);
            if ((value != null) && (value.length() > 0)) {
                // append the parameters
                buf.append(ampersand);
                buf.append(URLEncoder.encode(key));
                buf.append('=');
                buf.append(URLEncoder.encode(value));
            }
            ampersand = "&";
        }

        // return string 
        System.out.println("buf.toString(): "+buf.toString());
        return buf.toString();    
    }
	
	public static Map<String, String> doPostRequest(String vpc_Host, String data) throws Exception
//	public static String doPostRequest(String vpc_Host , String data, boolean useProxy, String proxyHost, int proxyPort) throws IOException
	{
		// New code commented
		Map<String, String> responseFields = null;
		
		//Old Code
		String body="";
		try
		{
			//Old Code
			/*InputStream is;
	        OutputStream os;
//	        int vpc_Port = 443; 
	        int vpc_Port = 8443; 
	        String fileName = "";
	        boolean useSSL = false;
	        String vpcHost=null;
		
	        // determine if SSL encryption is being used
	       if(vpc_Host.substring(0,8).equalsIgnoreCase("https://")){
	            useSSL=true;
	            //remove https:// from url
	            vpcHost=vpc_Host.substring(8);
	            //get File Name
	            fileName=vpcHost.substring(vpcHost.indexOf("/"),vpcHost.length());
	            //Get Host
	            vpcHost=vpcHost.substring(0,vpcHost.indexOf("/"));
	        }
	        System.out.println("vpcHost====="+vpcHost);
	         System.out.println("vpc_Port======="+vpc_Port);
	        // use the next block of code if using a proxy server
	        if (useProxy) {
	            Socket s = new Socket(proxyHost, proxyPort);
	            os = s.getOutputStream();
	            is = s.getInputStream();
	            // use next block of code if using SSL encryption
	            if (useSSL) {
	                String msg = "CONNECT " + vpcHost + ":" + vpc_Port + " HTTP/1.0\r\n" + "User-Agent: HTTP Client\r\n\r\n";
	                os.write(msg.getBytes());
	                byte[] buf = new byte[4096];
	                int len = is.read(buf);
	                String res = new String(buf, 0, len);

	                // check if a successful HTTP connection
	                if (res.indexOf("200") < 0) {
	                    throw new IOException("Proxy would now allow connection - " + res);
	                }
	                
	                // write output to VPC
	                SSLSocket ssl = (SSLSocket)s_sslSocketFactory.createSocket(s, vpcHost, vpc_Port, true);
	                ssl.startHandshake();
	                os = ssl.getOutputStream();
	                // get response data from VPC
	                is = ssl.getInputStream();
	            // use the next block of code if NOT using SSL encryption
	            } else {
	                fileName = vpcHost;
	            }
	        // use the next block of code if NOT using a proxy server
	        } else {
	            // use next block of code if using SSL encryption
	            if (useSSL) {
	                Socket s = s_sslSocketFactory.createSocket("192.168.21.156", 8443);
	                os = s.getOutputStream();
	                is = s.getInputStream();
	            // use next block of code if NOT using SSL encryption
	            } else {
	                Socket s = new Socket(vpcHost, vpc_Port);
	                os = s.getOutputStream();
	                is = s.getInputStream();
	            }
	        }
	        
	        String req = "POST " + fileName + " HTTP/1.0\r\n"
	                             + "User-Agent: HTTP Client\r\n"
	                             + "Content-Type: application/x-www-form-urlencoded\r\n"
	                             + "Content-Length: " + data.length() + "\r\n\r\n"
	                             + data;

	        os.write(req.getBytes());
	        String res = new String(readAll(is));

	        // check if a successful connection
	        if (res.indexOf("200") < 0) {
	            throw new IOException("Connection Refused - " + res);
	        }
	        
	        if (res.indexOf("404 Not Found") > 0) {
	            throw new IOException("File Not Found Error - " + res);
	        }
	        
	        int resIndex = res.indexOf("\r\n\r\n");
	        body = res.substring(resIndex + 4, res.length());*/
			
			// New Code:
			InputStream is = null;
			OutputStream os = null;
			
			String fileName = "";
			boolean useSSL = false;
			String vpcHost = null;
			X509TrustManager s_x509TrustManager = null;
			SSLContext sc = null;
			SSLSocketFactory s_sslSocketFactory = null;
			SSLSocket ssl = null;
			
			TrustManager[] trustAllCerts = new TrustManager[]{
					   new X509TrustManager() {
					      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					         return null;
					      }
					      public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
					      public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
					   }
					};
					HostnameVerifier allHostsValid = new HostnameVerifier() {
					   public boolean verify(String hostname, SSLSession session) {
					      return true;
					   }
					};
				
			sc= SSLContext.getInstance("TLSv1.2");
			   sc.init(null, null, new java.security.SecureRandom());
			   HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			   HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			   s_sslSocketFactory = sc.getSocketFactory();
			
			if (vpc_Host.substring(0, 8).equalsIgnoreCase("https://")) {
				useSSL = true;
				vpcHost = vpc_Host.substring(8);
				fileName = vpcHost.substring(vpcHost.indexOf("/"), vpcHost.length());
				vpcHost = vpcHost.substring(0, vpcHost.indexOf("/"));
			}
			System.out.println("vpcHost::"+vpcHost);
			System.out.println("vpcHost::"+useProxy);
			if (useProxy) {

				Socket s = new Socket(proxyHost, Integer.parseInt(proxyPort));
				System.out.println("s===="+s);
				os = s.getOutputStream();
				is = s.getInputStream();
				if (useSSL) {
					//String msg = "CONNECT " + vpcHost + ":" + vpc_Port + " HTTP/1.0\r\n"
					String msg = "CONNECT " + vpcHost + ":" + vpc_Port + " HTTP/1.0\r\n"
							+ "User-Agent: HTTP Client\r\n\r\n";
					System.out.println("vpcHost===="+vpcHost);
					System.out.println("vpc_Port===="+vpc_Port);
					os.write(msg.getBytes());
					byte[] buf = new byte[4096];
					int len = is.read(buf);
					String res = new String(buf, 0, len);

					if (res.indexOf("200") < 0) {
						throw new IOException("Proxy would now allow connection - " + res);
					}

					System.out.println("HandShake1");
					try {
						//System.out.println("");
						ssl = (SSLSocket) s_sslSocketFactory.createSocket(s, vpcHost, vpc_Port, true);

						ssl.startHandshake();
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println("HandShake");
					os = ssl.getOutputStream();
					is = ssl.getInputStream();

					System.out.println("1122");
				} else {
					fileName = vpcHost;
				}
			} else {
				try {
					System.out.println("2");
					if (useSSL) {
						System.out.println("8");
						SSLSocketFactory factory = (SSLSocketFactory) sc.getSocketFactory();
						SSLSocket s = (SSLSocket) factory.createSocket(vpcHost, vpc_Port);
						os = s.getOutputStream();
						is = s.getInputStream();
						System.out.println("10");
					} else {
						System.out.println("4");
						Socket s = new Socket(vpcHost, vpc_Port);
						os = s.getOutputStream();
						is = s.getInputStream();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String req = "POST " + fileName + " HTTP/1.0\r\n" + "User-Agent: HTTP Client\r\n"
					+ "Content-Type: application/x-www-form-urlencoded\r\n" + "Content-Length: " + data.length()
					+ "\r\n\r\n" + data;
			System.out.println("data==" + data);
			System.out.println("req==" + req);
			os.write(req.getBytes());
			String res = new String(readAll(is));
			System.out.println("res==" + res);
			if (res.indexOf("200") < 0) {
				throw new IOException("Connection Refused - " + res);
			}

			if (res.indexOf("404 Not Found") > 0) {
				throw new IOException("File Not Found Error - " + res);
			}

			int resIndex = res.indexOf("\r\n\r\n");
			String resQS = res.substring(resIndex + 4, res.length());
			responseFields = new LinkedHashMap<String, String>();
			responseFields = createMapFromResponse(resQS);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			//New Code commented
			return responseFields;
			
			//Old Code
//			return body;
		}
	}
	
	private static byte[] readAll(InputStream is) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];

		while (true) {
			int len = is.read(buf);
			if (len < 0) {
				break;
			}
			baos.write(buf, 0, len);
		}
		System.out.println("readAll: "+baos.toByteArray());
		return baos.toByteArray();
	}
	
	private static Map createMapFromResponse(String queryString) {
		Map map = new HashMap();
		System.out.println("createMapFromResponse queryString: "+queryString);
		StringTokenizer st = new StringTokenizer(queryString, "&");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int i = token.indexOf('=');
			if (i > 0) {
				try {
					String key = token.substring(0, i);
					String value = URLDecoder.decode(token.substring(i + 1, token.length()));
					map.put(key, value);
				} catch (Exception ex) {
					// Do Nothing and keep looping through data
				}
			}
		}
		return map;
	}
	
	private static String null2unknown(String in, Map responseFields) {
		if (in == null || in.length() == 0 || (String)responseFields.get(in) == null) {
			return "No Value Returned";
		} else {
			return (String)responseFields.get(in);
		}
	}
}

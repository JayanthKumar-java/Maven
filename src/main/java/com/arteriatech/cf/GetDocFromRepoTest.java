package com.arteriatech.cf;

import java.io.IOException;
import java.io.InputStream;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

/**
 * Servlet implementation class GetDocFromRepoTest
 */
@WebServlet("/GetDocFromRepoTest")
public class GetDocFromRepoTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDocFromRepoTest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
//		String docUrl
		String uniqueName = "FirstRepository";
    	// Use a secret key only known to your application (min. 10 chars)
    	String secretKey = "Arteria@123";
    	Session openCmisSession = null;
//    	CloseableHttpClient httpClient = HttpClients.createDefault();
    	CloseableHttpClient httpClient = HttpClients.createDefault();
//    	closa
//    	closa
    	try{
    		InitialContext ctx = new InitialContext();
            String lookupName = "java:comp/env/" + "EcmService";
            EcmService ecmSvc = (EcmService) ctx.lookup(lookupName);
            try {
            // connect to my repository
            	openCmisSession = ecmSvc.connect(uniqueName, secretKey);
            }catch (CmisObjectNotFoundException e) {
    	        // repository does not exist, so try to create it
    	        RepositoryOptions options = new RepositoryOptions();
    	        options.setUniqueName(uniqueName);
    	        options.setRepositoryKey(secretKey);
    	        options.setVisibility(Visibility.PROTECTED);
    	        ecmSvc.createRepository(options);
    	        // should be created now, so connect to it
    	        openCmisSession = ecmSvc.connect(uniqueName, secretKey);
            }catch(Exception e){
        	  	response.getWriter().println("Exception4: "+e.getMessage());
    			StackTraceElement element[] = e.getStackTrace();
    			StringBuffer buffer = new StringBuffer();
    			for(int i=0;i<element.length;i++)
    			{
    				buffer.append(element[i]);
    			}
    			response.getWriter().println("Full Stack Trace: "+buffer.toString());
            }
            response.getWriter().println(
            "<h3>You are now connected to the Repository with Id "
            + openCmisSession.getRepositoryInfo().getId()
            + "</h3>");
            
//            -----------------------------------------------------------
            Folder root = openCmisSession.getRootFolder();
            ItemIterable<CmisObject> children1 = root.getChildren();
	        response.getWriter().println("The root folder of the repository with id " + root.getId()
	                                 + " contains the following objects:<ul>");
	        for (CmisObject o : children1) {
	        	response.getWriter().print("<li>" + o.getName());
	        	if (o instanceof Folder) {
	        		response.getWriter().println(" createdBy: " + o.getCreatedBy() + "</li>");
	        		response.getWriter().println("ObjectID: "+o.getId());
	        	} else {
	        		Document doc = (Document) o;
	        		/*response.getWriter().println(" createdBy: " + o.getCreatedBy() + " filesize: "
	                                     + doc.getContentStreamLength() + " bytes"
	                                     + "</li>");*/
	        		response.getWriter().println("ObjectID: "+o.getId());
	        		response.getWriter().println("URL to access this object: "+doc.getContentUrl());
	        		
	        		if(o.getId().equalsIgnoreCase("M--j2_AWyG55_QfEH5-E5w2pZ0gfOmMqmurSfUsNE-0")){
//	        			response.getWriter().println("URL to access this object: "+doc.getContentUrl());
//	        			String docUrl = doc.getContentUrl();
	        			String docUrl = "https://paymentgatewaytestz93y6qtneb.hana.ondemand.com/PaymentGateway/docORD/json/f594742fbd0fc4333981afd5/root?objectId=M--j2_AWyG55_QfEH5-E5w2pZ0gfOmMqmurSfUsNE-0&cmisselector=content";
	        			String authParam="S0020543510:0638Mm1149!@#";
//	        			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authParam.getBytes());
	        			String basicAuth = "Basic " + Base64.encodeBase64String(authParam.getBytes());
	        			
	        			HttpGet getRequest = new HttpGet(docUrl);
	        			//Start
//	        			Credentials credentials = new UsernamePasswordCredentials("S0020543510", "0638Mm1149!@#");
//	        			httpClient.getConnectionManager().setCredentials(AuthScope.ANY, credentials);
//	        			httpClient.
//	        			getRequest.addHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        			//End
	        			
	        			getRequest.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
	        			CloseableHttpResponse getResponse = httpClient.execute(getRequest);
	        			
	        			try{
	        				response.getWriter().println("getProtocolVersion: "+getResponse.getProtocolVersion());              // HTTP/1.1
	        				response.getWriter().println("getStatusCode: "+getResponse.getStatusLine().getStatusCode());   // 200
	        				response.getWriter().println("getReasonPhrase: "+getResponse.getStatusLine().getReasonPhrase()); // OK
	        				response.getWriter().println("getStatusLine.toString: "+getResponse.getStatusLine().toString());        // HTTP/1.1 200 OK
	        				
	        				HttpEntity entity = getResponse.getEntity();
	        				response.getWriter().println("getContentLength: "+entity.getContentLength());
	        				response.getWriter().println("getContentType: "+entity.getContentType());
	        				response.getWriter().println("isChunked: "+entity.isChunked());
	        				response.getWriter().println("isStreaming: "+entity.isStreaming());
//	        				response.getWriter().println("isStreaming: "+entity.);
	        				
//	        				response.getWriter().println("isStreaming: "+entity.);
	        	            if (entity != null) {
	        	                // return it as a String
	        	            	InputStream stream = entity.getContent();
	        	            	byte[] bytes = IOUtils.toByteArray(stream);
	        	            	response.getWriter().println("encodeBase64String: "+Base64.encodeBase64String(bytes));
	        	            	
//	        	                String result = EntityUtils.toString(entity);
//	        	                response.getWriter().println("Base64: "+new String(Base64.getEncoder().encode(result.getBytes("utf-8"))));
	        	                //Content-Type:application/xml
//	        	                response.setCharacterEncoding("UTF-8");
//	        	                response.setHeader("Content-Type", "application/xml");
//	        	                response.setLocale(Locale.ENGLISH);
//	        	                response.getWriter().println(result);
	        	            }
	        			}catch (Exception e) {
	        				response.getWriter().println("Exception: "+e.getLocalizedMessage());
	        				response.getWriter().println("Exception: "+e.getMessage());
	        				StackTraceElement element[] = e.getStackTrace();
	        				StringBuffer buffer = new StringBuffer();
	        				for(int i=0;i<element.length;i++)
	        				{
	        					buffer.append(element[i]);
	        				}
	        				response.getWriter().println("Full Stack Trace: "+buffer.toString());
						}finally{
							getResponse.close();
						}
	        			
	        			/*ContentStream contentStream = doc.getContentStream();
	        			InputStream stream = contentStream.getStream();*/
	        			
	        			/*long len = contentStream.get
	        			byte[] data = new byte[len];
	    		        int offset = 0;
	    		        while (offset < len) {
	    		            int read = is.read(data, offset, data.length - offset);
	    		            if (read < 0) {
	    		                break;
	    		            }
	    		          offset += read;
	    		        }
	    		        if (offset < len) {
	    		            throw new IOException(
	    		                String.format("Read %d bytes; expected %d", offset, len));
	    		        }*/
	        			
	        			/*ContentStream contentStream = doc.getContentStream();
	        			InputStream stream = contentStream.getStream();
	        			
	        			response.getWriter().println("getLength: "+contentStream.getLength());
	        			response.getWriter().println("getMimeType: "+contentStream.getMimeType());
	        			response.getWriter().println("getFileName: "+contentStream.getFileName());
//	        			response.getWriter().println("getFileName: "+doc.);
//	        			response.getWriter().println("getFileName: "+contentStream.);
	        			org.apache.chemistry.opencmis.commons.impl.Base64.InputStream base64Stream 
	        			= new org.apache.chemistry.opencmis.commons.impl.Base64.InputStream
	        			(stream, org.apache.chemistry.opencmis.commons.impl.Base64.ENCODE);
	        			
	        			response.getWriter().println("toByteArray: "+IOUtils.toByteArray(stream));
//	        			base64Stream.re
//	        			response.getWriter().println("base64Stream: "+base64Stream);
	        			byte[] bytes = IOUtils.toByteArray(stream);
//	        			byte[] encoded = Base64.getEncoder().encode("Hello".getBytes());
	        			Base64.encodeBase64String(bytes);
	        			response.getWriter().println("encodeBase64String: "+new String(Base64.encodeBase64String(bytes)));*/
//	        			bytes  = stream.read;
//	        			base64Stream.
	        			
	        			/*StringBuffer sb1 = new StringBuffer();
	        			BufferedReader br1 = new BufferedReader(
	    						new InputStreamReader(base64Stream, "utf-8"));
	        			String line1 = null;
	    				while ((line1 = br1.readLine()) != null) {
	    					sb1.append(line1 + "\n");
	    				}
	    				br1.close();
	    				System.out.println("sb1: "+sb1.toString());
	    				response.getWriter().println("Base64: "+Base64.encodeBase64String(sb1.toString().getBytes()));*/
	        					
	    				/*StringBuffer sb = new StringBuffer();
	    				BufferedReader br = new BufferedReader(
	    						new InputStreamReader(stream, "utf-8"));
	    				String line = null;
	    				while ((line = br.readLine()) != null) {
	    					sb.append(line + "\n");
	    				}
	    				br.close();
	    				
	    				response.getWriter().println("sb: "+sb.toString());
	    				response.getWriter().println("bytes: "+sb.toString().getBytes());
	    				response.getWriter().println("Base64: "+Base64.encodeBase64String(sb.toString().getBytes()));*/
//	    				System.out.println("Base64: "+Base64.getEncoder().encodeToString(sb.toString().getBytes()));
	        		}
	        	}
	        }
	        response.getWriter().println("</ul>");
	        
//	        Document document = root.get 
    	}catch (Exception e) {
    		response.getWriter().println("Exception: "+e.getLocalizedMessage());
			response.getWriter().println("Exception: "+e.getMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println("Full Stack Trace: "+buffer.toString());
		}finally{
			httpClient.close();
//			httpClient.
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

package com.arteriatech.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Configuration
public class Trent extends HttpServlet {

    @SuppressWarnings("static-access")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// OutputStream os = null;
		boolean debug =false;
		try{
			String udyamReString = "";
			String msmeCommenceDate = "";
			String msmeValidTo = "";
			String msmeType = "";
			String stcd5 = "";
			String isMsMeReg = "";
			String trainStation = "";
			String vendorType = "";
			String vendorCategory = "";
			String vendorClass = "";
			String title = "";
			String name1 = "";
			String city = "";
			String district = "";
			String country = "";
			String postalCode = "";
			String currency = "";
			String contactPersonName = "";
			String telfx = "";
			String telf1 = "";
			String msmeTradeString = "";

		String url = "https://hdev1c8c5b055e.ap1.hana.ondemand.com/D0017/ARTEC/PSGW/service.xsodata/VendorRequestList(BusinessPartnerGUID='2428E6E2-6544-42FD-AD35-46E2C73D272C')?$expand=TelephoneNumbersList,EMailAddressesList,AddressesList,VendorGeneralList,VendorCompanyCodeList,VendorBankList,VendorPurchasingOrgDataList,VendorPurchasingDataList,VendorArbitrationList,VendorCertificateList,VendorContactList,VendorFactoryList,VendorFinanceList,VendorProductSpecializationList,VendorProductList,VendorReferenceList,VendorTransporterList,VendorVerticalList";
		String userpass = "USR_SCHARTEC_SRVUSR:7ONq2XoBvkNL7uzM";

		JsonObject vobFormFieldResponse = executeURL(url, userpass, response, false);
		if(debug){
		response.getWriter().println("vobFormFieldResponse: "+vobFormFieldResponse);
		}
		JsonObject vobGrpCompaniesObject = vobFormFieldResponse.get("d").getAsJsonObject().get("VendorFactoryList").getAsJsonObject();
		if(debug){
		response.getWriter().println("vobGrpCompaniesObject: "+vobGrpCompaniesObject);
		}
		JsonArray  vobGrpCompaniesArray= vobGrpCompaniesObject.get("results").getAsJsonArray();
		if(debug){
			response.getWriter().println("vobGrpCompaniesArray :"+vobGrpCompaniesArray);
		}
		Map<Integer, String[]> vobGrpCompaniesvalues = new LinkedHashMap<Integer, String[]>();
		for(int i=0;i<=vobGrpCompaniesArray.size()-1;i++){
			JsonObject vobGrpCompanyObject =  vobGrpCompaniesArray.get(i).getAsJsonObject();
			if(debug){
				response.getWriter().println("vobGrpCompanyObject :"+vobGrpCompanyObject);
			}
			
			String factoryName = vobGrpCompanyObject.get("FactoryName").getAsString();
			String vendorAddress1 = vobGrpCompanyObject.get("Address1").getAsString();
			String street = vobGrpCompanyObject.get("Street").getAsString();
			String citi = vobGrpCompanyObject.get("City").getAsString();
			String districts = vobGrpCompanyObject.get("District").getAsString();
			String postalCodes = vobGrpCompanyObject.get("PostalCode").getAsString();
			String countrys = vobGrpCompanyObject.get("Country").getAsString();
       if(debug){
			response.getWriter().println("factoryName "+factoryName);
			response.getWriter().println("vendorAddress1 "+vendorAddress1);
			response.getWriter().println("street "+street);
			response.getWriter().println("city "+citi);
			response.getWriter().println("district "+districts);
			response.getWriter().println("postalCodes "+postalCodes);
			response.getWriter().println("country "+countrys);
        } 
		  vobGrpCompaniesvalues.put(i+1, new String[]{factoryName, vendorAddress1, street, citi, districts, postalCodes, countrys});
		}

			// Map<String, String[]> values = new LinkedHashMap<>();
			// values.put("1", new String[]{"Company A", "Address 1", "Street 1", "City 1", "District 1", "12345", "Country A"});
			// values.put("2", new String[]{"Company B", "Address 2", "Street 2", "City 2", "District 2", "54321", "Country B"});
			// values.put("3", new String[]{"Company C", "Address 3", "Street 3", "City 3", "District 3", "67890", "Country C"});
			// values.put("4", new String[]{"Company D", "Address 4", "Street 4", "City 4", "District 4", "98765", "Country D"});
			// values.put("5", new String[]{"Company E", "Address 5", "Street 5", "City 5", "District 5", "11111", "Country E"});

	
			JsonObject vobGnrlDetailsObject = vobFormFieldResponse.get("d").getAsJsonObject().get("VendorGeneralList").getAsJsonObject();
			if(debug){
				response.getWriter().println("vobGnrlDetailsObject :"+vobGnrlDetailsObject);
			}
			JsonArray  vobGnrlDetailsArray= vobGnrlDetailsObject.get("results").getAsJsonArray();
			if(debug){
				response.getWriter().println("vobGnrlDetailsArray :"+vobGnrlDetailsArray);
			}
			JsonObject  vobGnrlDetailObject= vobGnrlDetailsArray.get(0).getAsJsonObject();
			if(debug){
				response.getWriter().println("vobGnrlDetailObject :"+vobGnrlDetailObject);
			}

			if(!vobGnrlDetailObject.get("TrainStation").isJsonNull()){
				trainStation = vobGnrlDetailObject.get("TrainStation").getAsString();
			}
			if(!vobGnrlDetailObject.get("VendorType").isJsonNull()){
				vendorType = vobGnrlDetailObject.get("VendorType").getAsString();
			}
			if(!vobGnrlDetailObject.get("VendorCategory").isJsonNull()){
				vendorCategory = vobGnrlDetailObject.get("VendorCategory").getAsString();
			}
			if(!vobGnrlDetailObject.get("VendorClass").isJsonNull()){
				vendorClass = vobGnrlDetailObject.get("VendorClass").getAsString();
			}
			if(!vobGnrlDetailObject.get("Title").isJsonNull()){
				title = vobGnrlDetailObject.get("Title").getAsString();
			}
			if(!vobGnrlDetailObject.get("Name1").isJsonNull()){
				name1 = vobGnrlDetailObject.get("Name1").getAsString();
			}
			if(!vobGnrlDetailObject.get("City").isJsonNull()){
				city = vobGnrlDetailObject.get("City").getAsString();
			}
			if(!vobGnrlDetailObject.get("District").isJsonNull()){
				district = vobGnrlDetailObject.get("District").getAsString();
			}
			if(!vobGnrlDetailObject.get("Coutnry").isJsonNull()){
				country = vobGnrlDetailObject.get("Coutnry").getAsString();
			}
			if(!vobGnrlDetailObject.get("PostalCode").isJsonNull()){
				postalCode = vobGnrlDetailObject.get("PostalCode").getAsString();
			}
			if(!vobGnrlDetailObject.get("J_SC_CURRENCY").isJsonNull()){
				currency = vobGnrlDetailObject.get("J_SC_CURRENCY").getAsString();
			}
			if(!vobGnrlDetailObject.get("ContactPersonName").isJsonNull()){
				contactPersonName = vobGnrlDetailObject.get("ContactPersonName").getAsString();
			}
			if(!vobGnrlDetailObject.get("TELFX").isJsonNull()){
				telfx = vobGnrlDetailObject.get("TELFX").getAsString();
			}
			if(!vobGnrlDetailObject.get("TELF1").isJsonNull()){
				telf1 = vobGnrlDetailObject.get("TELF1").getAsString();
			}
			if(!vobGnrlDetailObject.get("MSMETradingStatusID").isJsonNull()){
				msmeTradeString = vobGnrlDetailObject.get("MSMETradingStatusID").getAsString();
			}
			if(!vobGnrlDetailObject.get("UDYAMRegistrationDate").isJsonNull()){
				udyamReString = vobGnrlDetailObject.get("UDYAMRegistrationDate").getAsString();
			}
			if(!vobGnrlDetailObject.get("MSMECommenceDate").isJsonNull()){
				msmeCommenceDate = vobGnrlDetailObject.get("MSMECommenceDate").getAsString();
			}
			if(!vobGnrlDetailObject.get("MSMEValidTo").isJsonNull()){
				msmeValidTo = vobGnrlDetailObject.get("MSMEValidTo").getAsString();
			}
			if(!vobGnrlDetailObject.get("MSMEType").isJsonNull()){
				msmeType = vobGnrlDetailObject.get("MSMEType").getAsString();
			}
			if(!vobGnrlDetailObject.get("STCD5").isJsonNull()){
				stcd5 = vobGnrlDetailObject.get("STCD5").getAsString();
			}
			if(!vobGnrlDetailObject.get("ISMSMERegitered").isJsonNull()){
				isMsMeReg = vobGnrlDetailObject.get("ISMSMERegitered").getAsString();
			}



			JsonObject addrsListsObjects = vobFormFieldResponse.get("d").getAsJsonObject().get("AddressesList").getAsJsonObject();
			if(debug){
				response.getWriter().println("addrsListObject :"+addrsListsObjects);
			}
			JsonArray  addrsListsArray= addrsListsObjects.get("results").getAsJsonArray();
			if(debug){
				response.getWriter().println("addrsListsArray :"+addrsListsArray);
			}
			JsonObject  addrsListObject= addrsListsArray.get(0).getAsJsonObject();
			if(debug){
				response.getWriter().println("addrsListObject :"+addrsListObject);
			}

			String strSuppl1 = "";
			String street = "";
			String strSuppl2 = "";
			if(!addrsListObject.get("STRSUPPL1").isJsonNull()){
				strSuppl1 = addrsListObject.get("STRSUPPL1").getAsString();
			}
			if(!addrsListObject.get("STREET").isJsonNull()){
				street = addrsListObject.get("STREET").getAsString();
			}
			
			// String strSuppl2 = vobGnrlDetailObject.get("STR_SUPPL2").getAsString();
			
			// String street = vobGnrlDetailObject.get("STREET").getAsString();
			
			// if(debug){
			// 	response.getWriter().println("trainStation :"+trainStation);
			// 	response.getWriter().println("vendorType :"+vendorType);
			// 	response.getWriter().println("vendorCategory :"+vendorCategory);
			// 	response.getWriter().println("vendorClass :"+vendorClass);
			// 	response.getWriter().println("title :"+title);
			// 	response.getWriter().println("name1 :"+name1);
			// 	response.getWriter().println("strSuppl1 :"+strSuppl1);
			// 	response.getWriter().println("telPhnCode :"+telPhnCode);
			// 	response.getWriter().println("strSuppl2 :"+strSuppl2);
			// 	response.getWriter().println("contactPersonName :"+contactPersonName);
			// 	response.getWriter().println("street :"+street);
			// 	response.getWriter().println("telf1 :"+telf1);
			// 	response.getWriter().println("city :"+city);
			// 	response.getWriter().println("district :"+district);
			// 	response.getWriter().println("country :"+country);
			// 	response.getWriter().println("postalCode :"+postalCode);
			// }

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
    
		webContext.setVariable("values", vobGrpCompaniesvalues); // Pass group details to HTML
		
		webContext.setVariable("HomeCare", trainStation);
        webContext.setVariable("VendorAddress1", strSuppl1);  
        // webContext.setVariable("Mobile", telPhnCode);
        webContext.setVariable("VendorType", vendorType);
		webContext.setVariable("VendorAddress2", strSuppl2);
        webContext.setVariable("ContactPersonName", contactPersonName);
		// webContext.setVariable("PaymentTerms", "#13");
		webContext.setVariable("Street", street);
		webContext.setVariable("TelephoneNo", telf1);
		webContext.setVariable("CategoryofVendor", vendorCategory);
		webContext.setVariable("City", city);
		webContext.setVariable("Fax", telfx);
		webContext.setVariable("vendorClass", vendorClass);
		webContext.setVariable("District", district);
		// webContext.setVariable("Email1", emailId1);
		// webContext.setVariable("TaxDeclaration", "Taxd Declared");
		webContext.setVariable("Country", country);
		// webContext.setVariable("Email2", emailId2);
		webContext.setVariable("Title", title);
		// webContext.setVariable("Region", "EU 2");
		// webContext.setVariable("Email3", emailId3);
		webContext.setVariable("Name", name1);
		webContext.setVariable("PostalCode", postalCode);
		// webContext.setVariable("GSTR", "GSTR1975hu86k");
		webContext.setVariable("MSMENumber", stcd5);
		webContext.setVariable("Currency", currency);
		// webContext.setVariable("Lastreturn", "29/10/2024");
		webContext.setVariable("MSMEType", msmeType);
		webContext.setVariable("MSMERegistered", isMsMeReg);
		webContext.setVariable("MSMEValidDate", msmeValidTo);
		webContext.setVariable("MSMETrading", msmeTradeString);
		// webContext.setVariable("billingaddress", "NO");
		webContext.setVariable("CommencementDate", msmeCommenceDate);
		webContext.setVariable("UdyamReg", udyamReString);
		// webContext.setVariable("RegisteredAddress1", "#13/6 jayanga jdhfn");
		// webContext.setVariable("RegisteredDistrict", "Maharastra");
		// webContext.setVariable("RegisteredContactName", "");
		// webContext.setVariable("RegisteredAddress2", "#13/6 jayanga jdhf");
		// webContext.setVariable("RegisteredRegion", "#13/6 jayanjf hfbfnf hfbf");
		// webContext.setVariable("RegisteredTelephoneNo", "080-25977255");
		// webContext.setVariable("RegisteredStreet", "");
		// webContext.setVariable("RegisteredPostalCode", "#13/6 fjf hfbfnf hfbf");
		// webContext.setVariable("RegisteredFaxNo", "Pune");
		// webContext.setVariable("RegisteredCity", "Gokul");
		// webContext.setVariable("RegisteredMobileNo", "910802344");
		// webContext.setVariable("RegisteredEmailID", "#13/6 fjf hfbfnf hfbf");
		// webContext.setVariable("serialNo", "#139487447");
		// webContext.setVariable("name", "#139487447");
		// webContext.setVariable("vendorAddress1", "##13/6 jayanga jdhfnf  jayangar rr nagar vs fjfbfnfnnhf  fhbfjf hf");
		// webContext.setVariable("street", "church street");
		// webContext.setVariable("city", "#bengalore");
		// webContext.setVariable("district", "#Karantaka");
		// webContext.setVariable("postalCode", "#139487447");
		// webContext.setVariable("country", "#India");
		// webContext.setVariable("cinNo", "CIN008jh9725");
		// webContext.setVariable("gstn_number", "GSTN20856742I");
		// webContext.setVariable("pan_number", "#CKPPJ8464E");
		// webContext.setVariable("Constitution", "#Indian");
		// webContext.setVariable("Business", "Expoting");

	
		
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

	private List<String> getHeadersFromJavaSide() {
        List<String> headers = new ArrayList<>();
        headers.add("SerialNo");
        headers.add("Certificate Type");
        headers.add("Certificate No");
        // ...
        return headers;
    }

    private int getNumRowsFromJavaSide() {
        return 5; // example number of rows
    }

    private List<String> getCellsForRowFromJavaSide(int row) {
        List<String> cells = new ArrayList<>();
        cells.add("Row " + row + " Cell 1");
        cells.add("Row " + row + " Cell 2");
        // ...
        return cells;
    }


	@SuppressWarnings("finally")
	public JsonObject executeURL(String executeURL, String userPass, HttpServletResponse response, boolean debug) {
		DataOutputStream dataOut = null;
		BufferedReader in = null;
		JsonObject jsonObj = null;
		try {

			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString(userPass.getBytes()));
			connection.setDoInput(true);

			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String inputLine;
			StringBuffer responseStrBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseStrBuffer.append(inputLine);
			}

			if (debug) {
				response.getWriter().println("Direct Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
			JsonParser parser = new JsonParser();
			jsonObj = (JsonObject) parser.parse(responseStrBuffer.toString());

			if (debug) {
				response.getWriter().println("Json Response");
				response.getWriter().println(responseStrBuffer.toString());
			}
		} catch (Exception e) {
			response.getWriter().println("executeURL.Exception: " + e.getLocalizedMessage());
			StackTraceElement element[] = e.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < element.length; i++) {
				buffer.append(element[i]);
			}
			response.getWriter().println("executeURL.Full Stack Trace: " + buffer.toString());
		} finally {
			return jsonObj;
		}
	}

	
    @Bean
	public ServletRegistrationBean<Trent> TrentBean() {
		ServletRegistrationBean<Trent> bean = new ServletRegistrationBean<>(new Trent(), "/Trent");
		return bean;
	}
}
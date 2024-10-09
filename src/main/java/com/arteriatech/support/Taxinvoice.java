package com.arteriatech.support;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.HashedMap;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Taxinvoice extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fromCpGuid="",invGUID="",errorMessage="",oDataUrl="",userName="",password="",userPass="";
		CommonUtils commonUtils=new CommonUtils();
		String executeUrl="";
		boolean debug=false;
		
		Double totalItemTaxValue=new Double(0);
		Integer totalInvQty=new Integer(0);
		Double netAmt=new Double(0.00);
		Double totalAssValue=new Double(0.00);
		Double totalCgst=new Double(0.00);
		Double totalSgst=new Double(0.00);
		TreeSet<String> uomSets=new TreeSet<>();
		
		try{
			if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
				debug=true;
			}
			if(request.getParameter("FROM_CP_GUID")!=null && !request.getParameter("FROM_CP_GUID").equalsIgnoreCase("")){
				fromCpGuid=request.getParameter("FROM_CP_GUID");
			}
			
			if(request.getParameter("INV_GUID")!=null && !request.getParameter("INV_GUID").equalsIgnoreCase("")){
				invGUID=request.getParameter("INV_GUID");
			}
			if(fromCpGuid.equalsIgnoreCase("")){
				errorMessage="FROM_CP_GUID Field Missing in the input Payload";
			}
			if(invGUID.equalsIgnoreCase("")){
				if(errorMessage.equalsIgnoreCase("")){
					errorMessage="INV_NO Field Missing in the input payload";
				}else{
					errorMessage=errorMessage+",INV_NO Field Missing in the input payload";
				}
			}
			if(errorMessage.equalsIgnoreCase("")){
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "SSGWHANA");
				userName = commonUtils.getODataDestinationProperties("User", "SSGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "SSGWHANA");
				userPass = userName + ":" + password;
				executeUrl=oDataUrl+"SS_INV_H?$filter=INV_GUID%20eq%20%27"+invGUID+"%27%20and%20FROM_CP_GUID%20eq%20%27"+fromCpGuid+"%27";
				if(debug){
					response.getWriter().println("SS_INV_H Execute Url: "+executeUrl);
				}
				JsonObject SShHeaderObj = commonUtils.executeURL(executeUrl, userPass, response);
				if(debug){
					response.getWriter().println("SS_INV_H Records:"+SShHeaderObj);
				}
				executeUrl=oDataUrl+"SS_INV_I?$filter=INV_GUID%20eq%20%27"+invGUID+"%27";
				if(debug){
					response.getWriter().println("Invoice item URL:"+executeUrl);
				}
				JsonObject invItems = commonUtils.executeURL(executeUrl, userPass, response);
				if(debug){
					response.getWriter().println("Invoice items:"+invItems);
				}
			    
			     
				if(!SShHeaderObj.isJsonNull()&&SShHeaderObj.entrySet().size()>0&&SShHeaderObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
					ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
					templateResolver.setTemplateMode(TemplateMode.HTML);
					templateResolver.setPrefix("/Resources/Taxinvoice/");
					templateResolver.setSuffix(".html");
					templateResolver.setCacheTTLMs(3600000L);
					TemplateEngine templateEngine = new TemplateEngine();
					templateEngine.setTemplateResolver(templateResolver);
					WebContext webContext = new WebContext(request, response, getServletConfig().getServletContext(),
							request.getLocale());
					JsonObject sshObj = SShHeaderObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
					if(!sshObj.get("FROM_CP_GUID").isJsonNull()&&!sshObj.get("FROM_CP_GUID").getAsString().equalsIgnoreCase("")){
						String cpGuid=sshObj.get("FROM_CP_GUID").getAsString();
						
						if(cpGuid.length()<10){
							try{
							cpGuid=String.format("%010d", Integer.parseInt(cpGuid));
							}catch(Exception ex){
								
							}
						}
						if(debug){
							response.getWriter().println("FROM_CP_GUID:"+cpGuid);
						}
						oDataUrl = commonUtils.getODataDestinationProperties("URL", "SFGWHANA");
						userName = commonUtils.getODataDestinationProperties("User", "SFGWHANA");
						password = commonUtils.getODataDestinationProperties("Password", "SFGWHANA");
						userPass = userName + ":" + password;
						executeUrl=oDataUrl+"SF_CP?$filter=KUNNR%20eq%20%27"+cpGuid+"%27";
						if(debug){
							response.getWriter().println("executeUrl:"+executeUrl);
						}
						JsonObject sfCpObj = commonUtils.executeURL(executeUrl, userPass, response);
						if(debug){
							response.getWriter().println("sfCpObj: "+sfCpObj);
						}
						
						if(sfCpObj!=null && sfCpObj.entrySet().size()>0&&!sfCpObj.isJsonNull()&&sfCpObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
							
							 sfCpObj = sfCpObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject();
							
							if(!sfCpObj.get("NAME1").isJsonNull()){
								webContext.setVariable("NAME1",sfCpObj.get("NAME1").getAsString());	
							}
							
							if(!sfCpObj.get("ADDRESS1").isJsonNull()){
								webContext.setVariable("ADDRESS1",sfCpObj.get("ADDRESS1").getAsString());	
							}
							if(!sfCpObj.get("ADDRESS2").isJsonNull()){
								webContext.setVariable("ADDRESS2",sfCpObj.get("ADDRESS2").getAsString());	
							}
							
							if(!sfCpObj.get("ADDRESS3").isJsonNull()){
								webContext.setVariable("ADDRESS3",sfCpObj.get("ADDRESS3").getAsString());	
							}
							
							if(!sfCpObj.get("ADDRESS4").isJsonNull()){
								webContext.setVariable("ADDRESS4",sfCpObj.get("ADDRESS4").getAsString());	
							}
							if(!sfCpObj.get("CITY_NAME").isJsonNull()){
								webContext.setVariable("CITY_NAME",sfCpObj.get("CITY_NAME").getAsString());	
							}
							
							if(!sfCpObj.get("STATEID").isJsonNull()){
								webContext.setVariable("STATEID",sfCpObj.get("STATEID").getAsString());	
							}
							
							if(!sfCpObj.get("POSTAL_CODE").isJsonNull()){
								webContext.setVariable("POSTAL_CODE",sfCpObj.get("POSTAL_CODE").getAsString());	
							}
							
							if(!sfCpObj.get("MOBILE1").isJsonNull()){
								webContext.setVariable("MOBILE1",sfCpObj.get("MOBILE1").getAsString());	
							}
						}
						
					}
					if(!sshObj.get("EXT_SOLD_TO_CPNAME").isJsonNull()){
					webContext.setVariable("EXT_SOLD_TO_CPNAME", sshObj.get("EXT_SOLD_TO_CPNAME").getAsString());
					}else{
						webContext.setVariable("EXT_SOLD_TO_CPNAME", "");
					}if(!sshObj.get("SOLD_TO_ADDRESS1").isJsonNull()){
					webContext.setVariable("SOLD_TO_ADDRESS1", sshObj.get("SOLD_TO_ADDRESS1").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_ADDRESS1","");
					}if(!sshObj.get("SOLD_TO_ADDRESS2").isJsonNull()){
					webContext.setVariable("SOLD_TO_ADDRESS2", sshObj.get("SOLD_TO_ADDRESS2").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_ADDRESS2","");
					}
					if(!sshObj.get("SOLD_TO_ADDRESS3").isJsonNull()){
						
						webContext.setVariable("SOLD_TO_ADDRESS3", sshObj.get("SOLD_TO_ADDRESS3").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_ADDRESS3","");
					}
					if(!sshObj.get("SOLD_TO_ADDRESS4").isJsonNull()){
						
						webContext.setVariable("SOLD_TO_ADDRESS4", sshObj.get("SOLD_TO_ADDRESS4").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_ADDRESS4","");	
					}
					if(!sshObj.get("SOLD_TO_CITY_NAME").isJsonNull()){
						webContext.setVariable("SOLD_TO_CITY_NAME", sshObj.get("SOLD_TO_CITY_NAME").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_CITY_NAME","");
					}
					if(!sshObj.get("SOLD_TO_STATE").isJsonNull()){
						
						webContext.setVariable("SOLD_TO_STATE", sshObj.get("SOLD_TO_STATE").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_STATE","");	
					}
					if(!sshObj.get("SOLD_TO_COUNTRY_NAME").isJsonNull()){
						
						webContext.setVariable("SOLD_TO_COUNTRY_NAME", sshObj.get("SOLD_TO_COUNTRY_NAME").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_COUNTRY_NAME","");	
					}
					if(!sshObj.get("SOLD_TO_MOB_NO").isJsonNull()){
					webContext.setVariable("SOLD_TO_MOB_NO", sshObj.get("SOLD_TO_MOB_NO").getAsString());
					}else{
						webContext.setVariable("SOLD_TO_MOB_NO","");
					}
					
					if (!sshObj.get("INV_NO").isJsonNull()) {
						webContext.setVariable("INV_NO", sshObj.get("INV_NO").getAsString());
					} else {
						webContext.setVariable("INV_NO", "");
					}

					if (!sshObj.get("INV_DATE").isJsonNull()&& !sshObj.get("INV_DATE").getAsString().equalsIgnoreCase("")) {
						String lonDatetoStr = commonUtils.convertLongDateToString(response, sshObj.get("INV_DATE").getAsString(), debug);
						 String  date = formateDate(lonDatetoStr);
						 webContext.setVariable("INV_DATE",date);
					} else {
						webContext.setVariable("INV_DATE", "");
					}
					int slNo=1;
					String setUom=null;
					double taxAmt=0;
				List<TaxinvoiceItemDto> invoiceItems=new ArrayList<>();
					 if(invItems!=null &&!invItems.isJsonNull() && invItems.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
						 JsonArray invItemsArray = invItems.get("d").getAsJsonObject().get("results").getAsJsonArray();
						 for(int i=0;i<invItemsArray.size();i++,slNo++){
							 TaxinvoiceItemDto invDto=new TaxinvoiceItemDto();
							   JsonObject invItem = invItemsArray.get(i).getAsJsonObject();
							   invDto.setSlNo(slNo);
							 if(!invItem.get("MATERIAL_DESC").isJsonNull()){
								 invDto.setMaterialDesc(invItem.get("MATERIAL_DESC").getAsString());
							 }else{
								 invDto.setMaterialDesc("");	 
							 }
							 if(!invItem.get("MATERIAL_NO").isJsonNull()){
								 invDto.setMaterialNo(invItem.get("MATERIAL_NO").getAsString());
							 }else{
								 invDto.setMaterialNo("");	 
							 }
							 if(!invItem.get("BATCH").isJsonNull()){
								 invDto.setBatch(invItem.get("BATCH").getAsString());
							 }else{
								 invDto.setBatch("");	 
							 }
							 if(!invItem.get("EXPIRY_DATE").isJsonNull()){
								// invDto.setExpiryDate(invItem.get("EXPIRY_DATE").getAsString());
								 String convertLongDateToString = commonUtils.convertLongDateToString(response, invItem.get("EXPIRY_DATE").getAsString(), debug);
								 String  date = formateDate(convertLongDateToString);
								 invDto.setExpiryDate(date);
							 }else{
								 invDto.setExpiryDate("");	 
							 }
							 
							 if(!invItem.get("HSN_CODE").isJsonNull()){
								 invDto.setHsnCode(invItem.get("HSN_CODE").getAsString());
							 }else{
								 invDto.setHsnCode("");	 
							 }
							 if(!invItem.get("INV_QTY").isJsonNull()){
								 totalInvQty+=invItem.get("INV_QTY").getAsInt();
								 invDto.setInvQty(invItem.get("INV_QTY").getAsString());
							 }else{
								 invDto.setInvQty("");	 
							 }
							 
							 if(!invItem.get("UOM").isJsonNull()){
								 setUom=invItem.get("UOM").getAsString();
								 invDto.setPer(invItem.get("UOM").getAsString());
								 invDto.setUom(invItem.get("UOM").getAsString());
								 uomSets.add(setUom);
							 }else{
								 invDto.setUom("");
								 invDto.setPer("");
							 }
							 
							 
							 if(!invItem.get("ITM_BASIC_VALUE").isJsonNull()){
								 String formatedAmt = getFormatedAmt(invItem.get("ITM_BASIC_VALUE").getAsDouble());
								 invDto.setItemBasicValue(formatedAmt);
							 }else{
								 invDto.setItemBasicValue("");	 
							 }
							 
							 if(!invItem.get("DISCOUNT_PER").isJsonNull()){
								 invDto.setDiscountPer(invItem.get("DISCOUNT_PER").getAsString());
							 }else{
								 invDto.setDiscountPer("");	 
							 }
							 
							 if(!invItem.get("DISCOUNT_I").isJsonNull()){
								 String formatedAmt = getFormatedAmt(invItem.get("DISCOUNT_I").getAsDouble());
								 invDto.setDiscountI(formatedAmt);
							 }else{
								 invDto.setDiscountI("");	 
							 }
							 
							 if(!invItem.get("ASS_VALUE").isJsonNull()){
								 netAmt+=invItem.get("ASS_VALUE").getAsDouble();
								 String formatedAmt = getFormatedAmt(invItem.get("ASS_VALUE").getAsDouble());
								 invDto.setItemTaxableValue(formatedAmt);
							 }else{
								 invDto.setItemTaxableValue("");	 
							 }
							 if(!invItem.get("ITEM_TAX_VALUE").isJsonNull()){
								if (invItem.get("ITEM_TAX_VALUE").getAsDouble() > 0) {
									taxAmt = invItem.get("ITEM_TAX_VALUE").getAsDouble();
									totalCgst+=(taxAmt/2);
									totalSgst+=(taxAmt/2);
									invDto.setCgstAmt(totalCgst.toString());
									invDto.setSgstAmt(totalSgst.toString());
									
								} else {
									invDto.setCgstAmt(getFormatedAmt(taxAmt));
									invDto.setSgstAmt(getFormatedAmt(taxAmt));
								}
								 totalItemTaxValue+=invItem.get("ITEM_TAX_VALUE").getAsDouble();
								 invDto.setItemTaxValue(invItem.get("ITEM_TAX_VALUE").getAsString());
							 }else{
								 invDto.setItemTaxValue("");	 
							 }
							 if(!invItem.get("ASS_VALUE").isJsonNull()){
								 totalAssValue+=invItem.get("ASS_VALUE").getAsDouble();
								 invDto.setAssValue(invItem.get("ASS_VALUE").getAsString());
							 }else{
								 invDto.setAssValue("");	 
							 }
							 
							 invoiceItems.add(invDto);
							 
							 
						 }
						 Map<String,TaxinvoiceItemDto> HsnCalculatedAmt=new HashMap<>();
						 Map<String, List<TaxinvoiceItemDto>> totalHsnValues = invoiceItems.stream().collect(Collectors.groupingBy(TaxinvoiceItemDto::getHsnCode));
						// Set<Entry<String, List<TaxinvoiceItemDto>>> entrySet = totalHsnValues.entrySet();
						 Set<String> keySet = totalHsnValues.keySet();
						 keySet.forEach(key->{
							 TaxinvoiceItemDto updatedDto=new TaxinvoiceItemDto();
							 Double totalAssvale=new Double(0.0);
							 Double totalTaxAmt=new Double(0.0);
							 Double totalCgStAmt=new Double(0.0);
							 Double totalSGSTAmt=new Double(0.0);
							 List<TaxinvoiceItemDto> list = totalHsnValues.get(key);
							 for(TaxinvoiceItemDto dto:list){
								 if(dto.getAssValue()!=null&& !dto.getAssValue().equalsIgnoreCase(""))
								 totalAssvale+=Double.parseDouble(dto.getAssValue());
								 if(dto.getItemTaxValue()!=null&& !dto.getItemTaxValue().equalsIgnoreCase("")){
								 totalTaxAmt+=Double.parseDouble(dto.getItemTaxValue());
								 if(Double.parseDouble(dto.getItemTaxValue())>0){
								 totalCgStAmt+=Double.parseDouble(dto.getItemTaxValue())/2;
								 totalSGSTAmt+=Double.parseDouble(dto.getItemTaxValue())/2;
								 }
								 }
							 }
							 updatedDto.setAssValue(getFormatedAmt(totalAssvale));
							 updatedDto.setItemTaxValue(getFormatedAmt(totalTaxAmt));
							 updatedDto.setCgstAmt(getFormatedAmt(totalCgStAmt));
							 updatedDto.setSgstAmt(getFormatedAmt(totalSGSTAmt));
							 HsnCalculatedAmt.put(key, updatedDto);
						 });
						 
						 webContext.setVariable("hsnGroup",HsnCalculatedAmt);
						 
						 if (!sshObj.get("TOTAL_QTY").isJsonNull()) {
							 webContext.setVariable("totalQty",sshObj.get("TOTAL_QTY").getAsString());
						 }else{
							 
							 webContext.setVariable("totalQty","");
						 }
						 netAmt=netAmt+totalCgst+totalSgst;
						// String formatedNetAmt = getFormatedAmt(netAmt);
						 String formatedTotalTaxAmt = getFormatedAmt(totalItemTaxValue);
						// String totalAmtAmtInToWords = commonUtils.convertAmtToWords(netAmt);
						 String totalItemTaxInword = commonUtils.convertAmtToWords(totalItemTaxValue);
						 webContext.setVariable("TotalItemTaxAmtInWord", totalItemTaxInword);
						 
						 webContext.setVariable("TotalCGST", getFormatedAmt(totalCgst));
						 webContext.setVariable("TotalSGST", getFormatedAmt(totalSgst));
						 webContext.setVariable("TotalAssValue", getFormatedAmt(totalAssValue));
						 webContext.setVariable("TotalItemTaxValue", formatedTotalTaxAmt);
						 webContext.setVariable("TotalInvQty", totalInvQty);
						// webContext.setVariable("TotalAmount", formatedNetAmt);
						/* netAmt=1000.41;*/
						 
						 BigDecimal amountForRoundOff=new BigDecimal(netAmt);
						 amountForRoundOff=amountForRoundOff.setScale(2,RoundingMode.DOWN);
						 String fractionAmt = amountForRoundOff.toString().substring(amountForRoundOff.toString().indexOf("."));
						 double parsedAmt = Double.parseDouble(fractionAmt);
						 BigDecimal decFraction=new BigDecimal(parsedAmt);
						 decFraction.setScale(2,RoundingMode.DOWN);
						 BigDecimal offSet=new BigDecimal(1.00);
						 offSet=offSet.setScale(2,RoundingMode.DOWN);
						 BigDecimal roundOffValue = offSet.subtract(decFraction);
						if(parsedAmt>=.5){
							amountForRoundOff=amountForRoundOff.add(roundOffValue);
							 String totalAmtAmtInToWords = commonUtils.convertAmtToWords(amountForRoundOff.doubleValue());
							 webContext.setVariable("TotalAmount",  getFormatedAmt(amountForRoundOff.doubleValue()));
							 roundOffValue= roundOffValue.setScale(2,RoundingMode.HALF_DOWN);
							 webContext.setVariable("RoundOffValue","+ 0"+ roundOffValue);
							 webContext.setVariable("TotalAmtInWord", totalAmtAmtInToWords);
						}else{
							Double totalAmt=new Double(amountForRoundOff.intValue());
							 String totalAmtAmtInToWords= commonUtils.convertAmtToWords(totalAmt);
							 webContext.setVariable("TotalAmount", getFormatedAmt(totalAmt));
							 webContext.setVariable("RoundOffValue","- 0"+ fractionAmt.substring(0,2));
							 webContext.setVariable("TotalAmtInWord", totalAmtAmtInToWords);
						}
						 double cgst=0;
						 double sgst=0;
						if(totalItemTaxValue>0){
							cgst=totalItemTaxValue/2;
							sgst=totalItemTaxValue/2;
						}
						
						webContext.setVariable("CGST", getFormatedAmt(cgst));
						webContext.setVariable("SGST", getFormatedAmt(sgst));
						uomSets.remove("");
						if(uomSets.size()>0 &&uomSets.size()==1){
							webContext.setVariable("UOMUnits", uomSets.first());
						}else{
							webContext.setVariable("UOMUnits","");
						}
				     }
					
					 webContext.setVariable("invItems",invoiceItems);
					String html = templateEngine.process("Taxinvoices", webContext);
					html=html.replaceAll("\'", "&apos");
					ITextRenderer renderer = new ITextRenderer();
					renderer.setDocumentFromString(html);
					renderer.layout();
					if(debug){
						response.setContentType("application/json");
					}else{
					response.setContentType("application/pdf");
					response.setHeader("Cache-Control", "no-cache");
					ServletOutputStream outputStream = response.getOutputStream();
					renderer.createPDF(outputStream);
					outputStream.flush();
					}
					}else{
					// print header information not exist
						response.setContentType("application/json");
						JsonObject returnObj=new JsonObject();
						returnObj.addProperty("Message", "Record not exist");
						returnObj.addProperty("ErrorCode", "J002");
						returnObj.addProperty("Status", "000002");
						response.getWriter().println(returnObj);
				}
			}else{
				response.setContentType("application/json");
				JsonObject returnObj=new JsonObject();
				returnObj.addProperty("Message", errorMessage);
				returnObj.addProperty("ErrorCode", "J002");
				returnObj.addProperty("Status", "000002");
				response.getWriter().println(returnObj);
			}
			
			
		}catch(Exception ex){
			response.setContentType("application/json");
			StackTraceElement element[] = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<element.length;i++)
			{
				buffer.append(element[i]);
			}
			response.getWriter().println(ex.getClass().getCanonicalName() + "--->" + ex.getMessage()+"--->"+buffer.toString());	
		}
	}


	private String formateDate(String lonDatetoStr)throws Exception {
		try {
			Date dateFormate = new SimpleDateFormat("yyyyMMdd").parse(lonDatetoStr);
			DateFormat foramte = new SimpleDateFormat("dd/MM/yyyy");
			String format2 = foramte.format(dateFormate);
			return format2;
		} catch (Exception ex) {
			throw ex;
		}

	}
	
	private String getFormatedAmt(double amt){
		try{
		/*DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        String format = decimalFormat.format(amt);*/
			if(amt<10){
				 return format("0.00", amt);
			}
			else if(amt<100){
				 return format("00.00", amt);
			}
			else if(amt < 1000) {
		        return format("000.00", amt);
		    } else {
		        double hundreds = amt % 1000;
		        int other = (int) (amt / 1000);
		        return format(",##", other) + ',' + format("000.00", hundreds);
		    }
			/*NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
			String moneyString = formatter.format(amt);*/
			
          /* return moneyString;*/
		}catch (Exception e) {
			throw e;
		}
	}
	
	private static String format(String pattern, Object value) {
	    return new DecimalFormat(pattern).format(value);
	}
}
package com.arteriatech.hana;

import java.io.IOException;
import java.text.ParseException;
//import java.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class CPWiseMIS
 */
@WebServlet("/CPWiseMIS")
public class CPWiseMIS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CPWiseMIS() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String cpGuid = "", fromDate = "", toDate = "",validFromDate = "", validToDate = "";
		boolean debug = false;
		JsonObject monthlyMisJsonObj = new JsonObject();	
		try{
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			cpGuid = request.getParameter("CPGuid");
			fromDate = request.getParameter("FromDate");
			toDate = request.getParameter("ToDate");
			if(cpGuid != null && !cpGuid.isEmpty()){
				if (null != fromDate && ! fromDate.trim().equals("") ){
					if(	null != toDate && ! toDate.trim().equals("")){
					
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						sdf.setLenient(false);
						Date startDate = sdf.parse(fromDate);
						Date endDate = sdf.parse(toDate);
						
						if (startDate.compareTo(endDate) < 0)
						{
							validFromDate = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(startDate);
							if(debug)
								response.getWriter().println("FromDate.validFromDate : "+validFromDate);
							
							validToDate = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(endDate);
							if(debug)
								response.getWriter().println("ToDate.validToDate: "+validToDate);
							
							monthlyMisJsonObj = getPeakLimitByCP(cpGuid, validFromDate, validToDate,response,debug);
							response.getWriter().println(monthlyMisJsonObj);
//								response.getWriter().println("monthlyMisJsonObj: "+monthlyMisJsonObj);
						} else
						{
							monthlyMisJsonObj.addProperty("StatusCode", "000002");
							monthlyMisJsonObj.addProperty("Message", properties.getProperty("E190"));
							response.getWriter().println(monthlyMisJsonObj);
						}
					} catch (ParseException e) {
						monthlyMisJsonObj.addProperty("StatusCode", "000002");
						monthlyMisJsonObj.addProperty("Message", properties.getProperty("E191"));
						response.getWriter().println(monthlyMisJsonObj);
					}
					} else {
						monthlyMisJsonObj.addProperty("StatusCode", "000002");
						monthlyMisJsonObj.addProperty("Message", properties.getProperty("E192"));
						response.getWriter().println(monthlyMisJsonObj);
					}	
				} else {
					monthlyMisJsonObj.addProperty("StatusCode", "000002");
					monthlyMisJsonObj.addProperty("Message", properties.getProperty("E193"));
					response.getWriter().println(monthlyMisJsonObj);
				}	
			}else{
				monthlyMisJsonObj.addProperty("StatusCode", "000002");
				monthlyMisJsonObj.addProperty("Message", properties.getProperty("E194"));
				response.getWriter().println(monthlyMisJsonObj);
			}
		}catch (Exception e) {
			monthlyMisJsonObj.addProperty("StatusCode", "000002");
			monthlyMisJsonObj.addProperty("Message", "Exception: "+e.getLocalizedMessage());
			response.getWriter().println(monthlyMisJsonObj);
		}
	
	}
	private JsonObject getPeakLimitByCP(String cpGuid, String validFromDate, String validToDate, HttpServletResponse response,boolean debug) {
		String aggregatorID = "", enhancementType = "", executeUrl = "",oDataUrl = "",userName = "",password = "",userPass = "", cpType = "", currency = "", cpName = "",proposedLimit = "",validFrom = "",validTo = "";
		int itemNo = 0,totalAmount = 0,proposedLimitAmt = 0,peakLimitDays = 0,peakDays = 0;
		JsonObject enhancementLimitObj = new JsonObject();
		JsonObject monthlyMisJsonObj = new JsonObject();
		JsonObject lclMisJsonObj = new JsonObject();
		long peakLimitDaysMilli=0;
		JsonArray misJsonArray = new JsonArray();
		JsonObject lclJsonObj = new JsonObject();
		JsonObject childELimit = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			enhancementType = "000020";
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
			executeUrl = oDataUrl+"EnhancementLimits?$filter=%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPGuid%20eq%20%27"+cpGuid+"%27%20and%20EnhancementType%20eq%20%27"+enhancementType+"%27%20and%20(ValidFrom%20le%20datetime%27"+validToDate+"T00:00:00%27%20and%20ValidTo%20ge%20datetime%27"+validFromDate+"T00:00:00%27)";
			if(debug)
				response.getWriter().println("getPeakLimitByCP.executeUrl: "+executeUrl);
			enhancementLimitObj = commonUtils.executeURL(executeUrl, userPass, response);
			if(debug)
				response.getWriter().println("getPeakLimitByCP().enhancementLimitsObj: "+enhancementLimitObj);
			
			int eLimitObjSize = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size();
			if(debug)
				response.getWriter().println("getPeakLimitByCP().eLimitObjSize: "+eLimitObjSize);
			
			//changed
		/*	if(eLimitObjSize > 0){
				
			}else{
//				{"StatusCode":"000002","Message":"Year is mandatory"}
			}*/
			SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			for(int i=0; i<eLimitObjSize; i++){
				lclJsonObj = new JsonObject();
				totalAmount = 0;
				peakLimitDaysMilli=0;
				childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject();
				try{
					validFrom = childELimit.get("ValidFrom").getAsString();
				}catch (Exception e) {
					validFrom = "";
				}
				try{
					validTo = childELimit.get("ValidTo").getAsString();
				}catch (Exception e) {
					validTo = "";
				}
				if (debug) {
					response.getWriter().println("eLimitValidFrom: "+validFrom);
					response.getWriter().println("eLimitValidTo: "+validTo);
				}
				Date eLimitValidFromDate = simpleFormat.parse(simpleFormat.format(new Date(Long.parseLong(validFrom.substring(validFrom.indexOf("(")+1, validFrom.lastIndexOf(")"))))));
				Date eLimitValidToDate = simpleFormat.parse(simpleFormat.format(new Date(Long.parseLong(validTo.substring(validTo.indexOf("(")+1, validTo.lastIndexOf(")"))))));
				Date fromDate = simpleFormat.parse(validFromDate);
				Date toDate = simpleFormat.parse(validToDate);
				if(debug){
					response.getWriter().println("childELimit.validFromDate: "+validFromDate);
					response.getWriter().println("childELimit.validToDate: "+validToDate);
					response.getWriter().println("startDate: "+fromDate);
					response.getWriter().println("endDate: "+toDate);
				}
				
				if (!((eLimitValidFromDate.compareTo(fromDate) < 0 && eLimitValidToDate.compareTo(fromDate)<0 )  && (eLimitValidFromDate.compareTo(fromDate) > 0 && eLimitValidToDate.compareTo(fromDate)> 0))) {
					try{
						proposedLimit = childELimit.get("ProposedLimit").getAsString();
					}catch (Exception e) {
						proposedLimit = "";
					}
					try{
						currency = childELimit.get("Currency").getAsString();
					}catch (Exception e) {
						currency = "";
					}
					try{
						cpType = childELimit.get("CPType").getAsString();
					}catch (Exception e) {
						cpType = "";
					}
					try{
						cpName = childELimit.get("CPName").getAsString();
					}catch (Exception e) {
						cpName = "";
					}
					if(debug){
						response.getWriter().println("currency: "+currency);
						response.getWriter().println("proposedLimit: "+proposedLimit);
						response.getWriter().println("cpType: "+cpType);
						response.getWriter().println("cpName: "+cpName);
						response.getWriter().println("validFrom: "+validFrom);
						response.getWriter().println("validTo: "+validTo);
					}
					if (eLimitValidToDate.compareTo(fromDate) >= 0 && eLimitValidFromDate.compareTo(fromDate) <= 0) {
						peakLimitDaysMilli = (eLimitValidToDate.getTime()-fromDate.getTime());
						if (debug)
							response.getWriter().println("eLimitValidToDate.getTime(): "+ eLimitValidToDate.getTime() +"- fromDate.getTime(): "+fromDate.getTime());
					}
					else if (eLimitValidToDate.compareTo(fromDate)>=0 && eLimitValidFromDate.compareTo(fromDate)>=0 && toDate.compareTo(eLimitValidToDate) >= 0) {
						peakLimitDaysMilli = (eLimitValidToDate.getTime()-eLimitValidFromDate.getTime());
						if (debug)
							response.getWriter().println("eLimitValidToDate.getTime(): "+ eLimitValidToDate.getTime() +"- eLimitValidFromDate.getTime(): "+eLimitValidFromDate.getTime());
					}
					else if (eLimitValidToDate.compareTo(fromDate)>=0 && eLimitValidFromDate.compareTo(fromDate)>=0 && toDate.compareTo(eLimitValidToDate) <= 0) {
						peakLimitDaysMilli = (toDate.getTime() -eLimitValidFromDate.getTime());
						if (debug)
							response.getWriter().println("toDate.getTime(): "+ toDate.getTime() +" - eLimitValidFromDate.getTime(): "+eLimitValidFromDate.getTime());
					}
					else if (eLimitValidFromDate.compareTo(fromDate)< 0 && eLimitValidToDate.compareTo(toDate)>0) {
						peakLimitDaysMilli = (toDate.getTime()- fromDate.getTime());
						if (debug)
							response.getWriter().println("toDate.getTime(): "+ toDate.getTime() +"- fromDate.getTime(): "+fromDate.getTime());
					}
					peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
//					peakLimitDays = (int) (peakLimitDaysMilli / (1000*60*60*24))+1;
					try	{
					    if(proposedLimit != null)
					    	proposedLimitAmt = Integer.parseInt(proposedLimit);
					}catch (NumberFormatException e){
						proposedLimitAmt = 0;
					}
					itemNo++;
					 totalAmount = totalAmount+proposedLimitAmt;	
					 if(debug){
						 response.getWriter().println("itemNo: "+Integer.toString(itemNo));
						 response.getWriter().println("totalAmount: "+Integer.toString(totalAmount));
						 response.getWriter().println("peakLimitDays: "+Integer.toString(peakLimitDays));
					 }
					 lclJsonObj.addProperty("ItemNo",Integer.toString(itemNo));;
					 lclJsonObj.addProperty("ValidFrom",format.format(eLimitValidFromDate));
					 lclJsonObj.addProperty("ValidTo",format.format(eLimitValidToDate));
					 lclJsonObj.addProperty("PeakLimitDays",Integer.toString(peakLimitDays));
					 lclJsonObj.addProperty("TotalAmount",Integer.toString(totalAmount));
					 lclJsonObj.addProperty("Currency", currency);
					 if(debug)
						 response.getWriter().println("lclJsonObj: "+lclJsonObj);
					 misJsonArray.add(lclJsonObj);
				}
			}
			if(debug)
				response.getWriter().println("getPeakLimitByCP().misJsonArray: "+misJsonArray);
			
			lclMisJsonObj.addProperty("CPGuid",childELimit.get("CPGuid").getAsString());
			lclMisJsonObj.addProperty("CPType",childELimit.get("CPType").getAsString());
			lclMisJsonObj.addProperty("CPName",childELimit.get("CPName").getAsString());
			lclMisJsonObj.add("Items", misJsonArray);
			if(debug)
				response.getWriter().println("getPeakLimitByCP().lclMisJsonObj: "+lclMisJsonObj);
			monthlyMisJsonObj.add("PeakLimitByCP", lclMisJsonObj);
			if(debug)
				response.getWriter().println("getPeakLimitByCP().monthlyMisJsonObj: "+monthlyMisJsonObj);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return monthlyMisJsonObj ;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}





/*package com.arteriatech.hana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
//import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

*//**
 * Servlet implementation class CPWiseMIS
 *//*
@WebServlet("/CPWiseMIS")
public class CPWiseMIS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    *//**
     * @see HttpServlet#HttpServlet()
     *//*
    public CPWiseMIS() {
        super();
        // TODO Auto-generated constructor stub
    }

	*//**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String cpGuid = "", cpName="", month = "", years = "", aggregatorId = "",monthStartDate = "", monthEndDate = "",monthDesc = "";
		int monthCode = 0, firstDay = 0,lastDay = 0, year = 0;
		boolean debug = false;
		JsonObject monthlyMisJsonObj = new JsonObject();	
		try{
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			cpGuid = request.getParameter("CPGuid");
			month = request.getParameter("Month");
			years = request.getParameter("Year");
			if(cpGuid != null && !cpGuid.isEmpty()){
				if(((month != null && !month.isEmpty()) && StringUtils.isNumeric(month)) && (Integer.parseInt(month) >=0 && Integer.parseInt(month)<=11)){
					try{
						if((month != null && !month.isEmpty()) && (years != null && !years.isEmpty())){
							monthCode = Integer.parseInt(month);
							year = Integer.parseInt(years); 
						}
					}catch (Exception e) {
						monthCode = 0;
						year = 0;
					}	
					switch (monthCode) {
					case 0:	monthDesc = "January";
							lastDay = 31;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 1: if(monthCode == 1 && (year%4 == 0 || year%400 == 0)){
								monthDesc = "February";
								lastDay = 29; 
								if(debug)
									response.getWriter().println("monthDesc: "+monthDesc);
							}else if(monthCode == 1 && (year%4 != 0 || year%400 != 0)){
								monthDesc = "February";
								lastDay = 28;  
								if(debug)
									response.getWriter().println("monthDesc: "+monthDesc);
							}
							break;
					case 2:	monthDesc = "March";
							lastDay = 31; 
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 3:	monthDesc = "April";
							lastDay = 30;  
							response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 4:	monthDesc = "May";
							lastDay = 31;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 5:	monthDesc = "June";
							lastDay = 30;
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 6:	monthDesc = "July";
							lastDay = 31;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 7:	monthDesc = "August";
							lastDay = 31;
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 8:	monthDesc = "September";
							lastDay = 30;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 9:	monthDesc = "October";
							lastDay = 31;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 10:monthDesc = "November";
							lastDay = 30;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;
					case 11:monthDesc = "December";
							lastDay = 31;  
							if(debug)
								response.getWriter().println("monthDesc: "+monthDesc);
							break;					
					}
					if((years != null && !years.isEmpty()) && (StringUtils.isNumeric(years) && years.length() == 4)){
						firstDay = 01;    													
						Calendar calendar = Calendar.getInstance();
						calendar.set(year, monthCode, firstDay, 0, 0, 0);
						monthStartDate = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(calendar.getTime());
						if(debug)
							response.getWriter().println("StartDate of month: "+monthStartDate); 						
						calendar.set(year, monthCode, lastDay, 23, 59, 59);
						monthEndDate = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(calendar.getTime());
						if(debug)
							response.getWriter().println("Last Date of month: "+monthEndDate);
						monthlyMisJsonObj = getPeakLimitByCP(cpGuid, monthStartDate,monthEndDate,monthCode,monthDesc,years,response,debug);
						response.getWriter().println(monthlyMisJsonObj);
//						response.getWriter().println("monthlyMisJsonObj: "+monthlyMisJsonObj);
					}else{
						monthlyMisJsonObj.addProperty("StatusCode", "000002");
						monthlyMisJsonObj.addProperty("Message", "Year is mandatory");
						response.getWriter().println(monthlyMisJsonObj);
					}
				}else{
					monthlyMisJsonObj.addProperty("StatusCode", "000002");
					monthlyMisJsonObj.addProperty("Message", "Month is mandatory");
					response.getWriter().println(monthlyMisJsonObj);
				}
			}else{
				monthlyMisJsonObj.addProperty("StatusCode", "000002");
				monthlyMisJsonObj.addProperty("Message", "CPGuid is mandatory");
				response.getWriter().println(monthlyMisJsonObj);
			}
		}catch (Exception e) {
			monthlyMisJsonObj.addProperty("StatusCode", "000002");
			monthlyMisJsonObj.addProperty("Message", "Exception");
			response.getWriter().println(monthlyMisJsonObj);
		}
	
	}
	
	private JsonObject getPeakLimitByCP(String cpGuid, String monthStartDate, String monthEndDate, int monthCode, String monthDesc,String years,HttpServletResponse response,boolean debug) {
		String aggregatorID = "", enhancementType = "", executeUrl = "",oDataUrl = "",userName = "",password = "",userPass = "", cpType = "", currency = "", cpName = "",proposedLimit = "",validFrom = "",validTo = "";
		int itemNo = 0,totalAmount = 0,proposedLimitAmt = 0,peakLimitDays = 0,peakDays = 0;
		JsonObject enhancementLimitObj = new JsonObject();
		JsonObject monthlyMisJsonObj = new JsonObject();
		JsonObject lclMisJsonObj = new JsonObject();
		JsonArray misJsonArray = new JsonArray();
		JsonObject lclJsonObj = new JsonObject();
		JsonObject childELimit = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		
		try{
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			enhancementType = "000020"; 
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");		
			executeUrl = oDataUrl+"EnhancementLimits?$filter=%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20CPGuid%20eq%20%27"+cpGuid+"%27%20and%20EnhancementType%20eq%20%27"+enhancementType+"%27%20and%20(ValidFrom%20le%20datetime%27"+monthEndDate+"%27%20and%20ValidTo%20ge%20datetime%27"+monthStartDate+"%27)";
			if(debug)
				response.getWriter().println("executeUrl: "+executeUrl);
			enhancementLimitObj = commonUtils.executeURL(executeUrl, userPass, response);
			if(debug)
				response.getWriter().println("enhancementLimitsObj: "+enhancementLimitObj);
			
			int eLimitObjSize = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size();
			if(debug)
				response.getWriter().println("eLimitObjSize: "+eLimitObjSize);
			
			if(eLimitObjSize > 0){
				
			}else{
//				{"StatusCode":"000002","Message":"Year is mandatory"}
			}
			
			for(int i=0; i<eLimitObjSize; i++){
				lclJsonObj = new JsonObject();
				totalAmount = 0;
				childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject();
				try{
					proposedLimit = childELimit.get("ProposedLimit").getAsString();
				}catch (Exception e) {
					proposedLimit = "";
				}
				try{
					currency = childELimit.get("Currency").getAsString();
				}catch (Exception e) {
					currency = "";
				}
				try{
					cpType = childELimit.get("CPType").getAsString();
				}catch (Exception e) {
					cpType = "";
				}
				try{
					cpName = childELimit.get("CPName").getAsString();
				}catch (Exception e) {
					cpName = "";
				}
				try{
					validFrom = childELimit.get("ValidFrom").getAsString();
				}catch (Exception e) {
					validFrom = "";
				}
				try{
					validTo = childELimit.get("ValidTo").getAsString();	
				}catch (Exception e) {
					validTo = "";
				}
				if(debug){
					response.getWriter().println("currency: "+currency);
					response.getWriter().println("proposedLimit: "+proposedLimit);
					response.getWriter().println("cpType: "+cpType);
					response.getWriter().println("cpName: "+cpName);
					response.getWriter().println("validFrom: "+validFrom);
					response.getWriter().println("validTo: "+validTo);
				}
				SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date validFromDate = simpleFormat.parse(simpleFormat.format(new Date(Long.parseLong(validFrom.substring(validFrom.indexOf("(")+1, validFrom.lastIndexOf(")"))))));
				Date validToDate = simpleFormat.parse(simpleFormat.format(new Date(Long.parseLong(validTo.substring(validTo.indexOf("(")+1, validTo.lastIndexOf(")"))))));
				Date startDate = simpleFormat.parse(monthStartDate);
				Date endDate = simpleFormat.parse(monthEndDate);
				if(debug){
					response.getWriter().println("validFromDate: "+validFromDate);
					response.getWriter().println("validToDate: "+validToDate);
					response.getWriter().println("startDate: "+startDate);
					response.getWriter().println("endDate: "+endDate);
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(validFromDate);
				int validFromMonth = calendar.get(Calendar.MONTH);
				calendar.setTime(validToDate);
				int validToMonth = calendar.get(Calendar.MONTH);
				int validToYear = calendar.get(Calendar.YEAR);
				int year = Integer.parseInt(years);
				if(validFromMonth < monthCode && validToMonth == monthCode){
					long peakLimitDaysMilli = (validToDate.getTime()-startDate.getTime());
					peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
					if(debug)
						response.getWriter().println("peakLimitDays: "+peakLimitDays);
				}else if(validFromMonth == monthCode &&  validToMonth == monthCode){
					long peakLimitDaysMilli = (validToDate.getTime()-validFromDate.getTime());
					peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
					if(debug)
						response.getWriter().println("peakLimitDays: "+peakLimitDays);
				}else if((validFromMonth == monthCode) && (validToMonth > monthCode && validToYear >=year)){
					long peakLimitDaysMilli = (endDate.getTime()-validFromDate.getTime());
					peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
					if(debug)
						response.getWriter().println("peakLimitDays: "+peakLimitDays);
				}else if((validFromMonth < monthCode) && (validToMonth > monthCode  && validToYear >=year)){
					long peakLimitDaysMilli = (endDate.getTime()-startDate.getTime());
					peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
					if(debug)
						response.getWriter().println("peakLimitDays: "+peakLimitDays);
				}
				try	{
				    if(proposedLimit != null)
				    	proposedLimitAmt = Integer.parseInt(proposedLimit);
				}catch (NumberFormatException e){
					proposedLimitAmt = 0;
				}
				itemNo++;					
				totalAmount = totalAmount+proposedLimitAmt;	
				if(debug){
					response.getWriter().println("itemNo: "+Integer.toString(itemNo));
					response.getWriter().println("totalAmount: "+Integer.toString(totalAmount));
					response.getWriter().println("peakLimitDays: "+Integer.toString(peakLimitDays));
				}
				lclJsonObj.addProperty("ItemNo",Integer.toString(itemNo));;
				lclJsonObj.addProperty("ValidFrom",format.format(validFromDate));
				lclJsonObj.addProperty("ValidTo",format.format(validToDate));
				lclJsonObj.addProperty("PeakLimitDays",Integer.toString(peakLimitDays));
				lclJsonObj.addProperty("TotalAmount",Integer.toString(totalAmount));
				lclJsonObj.addProperty("Currency", currency);
				if(debug)
					response.getWriter().println("lclJsonObj: "+lclJsonObj);
				misJsonArray.add(lclJsonObj);
			}
			if(debug)
				response.getWriter().println("misJsonArray: "+misJsonArray);
			lclMisJsonObj.addProperty("CPGuid",childELimit.get("CPGuid").getAsString());
			lclMisJsonObj.addProperty("CPType",childELimit.get("CPType").getAsString());
			lclMisJsonObj.addProperty("CPName",childELimit.get("CPName").getAsString());
			lclMisJsonObj.addProperty("MonthCode",Integer.toString(monthCode));
			lclMisJsonObj.addProperty("MonthDesc",monthDesc);
			lclMisJsonObj.addProperty("Year",years);
			lclMisJsonObj.add("Items", misJsonArray);
			if(debug)
				response.getWriter().println("lclMisJsonObj: "+lclMisJsonObj);
			monthlyMisJsonObj.add("PeakLimitByCP", lclMisJsonObj);
			if(debug)
				response.getWriter().println("monthlyMisJsonObj: "+monthlyMisJsonObj);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return monthlyMisJsonObj ;
	}

	*//**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
*/
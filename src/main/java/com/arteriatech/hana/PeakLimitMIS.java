package com.arteriatech.hana;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.Base64;
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
 * Servlet implementation class PeakLimitMIS
 */
@WebServlet("/PeakLimitMIS")
public class PeakLimitMIS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PeakLimitMIS() {
        super();
        // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String fromDate = "", toDate = "",validFromDate="",validToDate="";
		boolean debug = false;
		JsonObject peakLimitMISObj = new JsonObject();
		try {
			Properties properties = new Properties();
			properties.load(getServletContext().getResourceAsStream("/Resources/KeyProperties.properties"));
			
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			fromDate = request.getParameter("FromDate");
			toDate = request.getParameter("ToDate");
			if(debug){
				response.getWriter().println("fromDate: "+fromDate);
				response.getWriter().println("toDate: "+toDate);
			}
			if ((null != fromDate && ! fromDate.trim().equals(""))) {
				if (null != toDate && ! toDate.trim().equals("")) {
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
							
							peakLimitMISObj = getPeakLimitMIS(validFromDate, validToDate, debug, response);
							response.getWriter().println(peakLimitMISObj);
							
						} else
						{
							peakLimitMISObj.addProperty("StatusCode", "000002");
							peakLimitMISObj.addProperty("Message", properties.getProperty("E190"));
							response.getWriter().println(peakLimitMISObj);
						}
					} catch (ParseException e) {
						peakLimitMISObj.addProperty("StatusCode", "000002");
						peakLimitMISObj.addProperty("Message", properties.getProperty("E191"));
						response.getWriter().println(peakLimitMISObj);
					}
					
				} else {
					peakLimitMISObj.addProperty("StatusCode", "000002");
					peakLimitMISObj.addProperty("Message", properties.getProperty("E192"));
					response.getWriter().println(peakLimitMISObj);
				}
			} else {
				peakLimitMISObj.addProperty("StatusCode", "000002");
				peakLimitMISObj.addProperty("Message", properties.getProperty("E193"));
				response.getWriter().println(peakLimitMISObj);
			}
		} catch (Exception e) {
			peakLimitMISObj.addProperty("StatusCode", "000002");
			peakLimitMISObj.addProperty("Message", "Exception");
			response.getWriter().println(peakLimitMISObj);
		}
	}
	
	private JsonObject getPeakLimitMIS(String validFromDate, String validToDate, boolean debug, HttpServletResponse response) {
		String aggregatorID = "", cpGuid = "", enhancementType = "", executeUrl = "",oDataUrl = "",userName = "",password = "", cpName="", userPass = "", cpType = "",currency = "",proposedLimit = "",validFrom = "",validTo = "";
		int peakLimitCount = 0,totalAmount = 0,proposedLimitAmt = 0,peakLimitDays = 0,peakDays = 0;
		JsonObject enhancementLimitObj = new JsonObject();
		long peakLimitDaysMilli=0;
		JsonObject misJsonObj = new JsonObject();
		JsonArray misJsonArray = new JsonArray();
		JsonObject childELimit = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try{
			
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			enhancementType = "000020";
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");

			executeUrl = oDataUrl+"EnhancementLimits?$filter=%20EnhancementType%20eq%20%27"+enhancementType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20(ValidFrom%20le%20datetime%27"+validToDate+"T00:00:00%27%20and%20ValidTo%20ge%20datetime%27"+validFromDate+"T00:00:00%27)";
			if(debug)
				response.getWriter().println("getPeakLimitMIS.executeUrl: "+executeUrl);
			enhancementLimitObj = commonUtils.executeURL(executeUrl, userPass, response);
			if(debug)
				response.getWriter().println("getPeakLimitMIS().enhancementLimitsObj: "+enhancementLimitObj);
			int eLimitObjSize = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size();
			if(debug)
				response.getWriter().println("getPeakLimitMIS().eLimitObjSize: "+eLimitObjSize);
			
			int misJsonArraySize = 0;
			if(debug)
				response.getWriter().println("getPeakLimitMIS().misJsonObjSize: "+misJsonArraySize);
			if(eLimitObjSize > 0){
				JsonObject lclJsonObj = new JsonObject();
				peakLimitDaysMilli=0;
				for(int i=0; i<eLimitObjSize; i++){
					childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject();
					misJsonArraySize = misJsonArray.size();
					cpGuid = childELimit.get("CPGuid").getAsString();
					cpType = childELimit.get("CPType").getAsString();
					cpName = childELimit.get("CPName").getAsString();
					currency = childELimit.get("Currency").getAsString();
					if(debug)
						response.getWriter().println("getPeakLimitMIS().cpGuid: "+cpGuid);
					if(misJsonArraySize == 0){
					
						lclJsonObj.addProperty("CPGuid", cpGuid);
						lclJsonObj.addProperty("CPTypeID", cpType);
						lclJsonObj.addProperty("CPName", cpName);
						lclJsonObj.addProperty("PeakLimitCount", "");
						lclJsonObj.addProperty("TotalAmount", "");
						lclJsonObj.addProperty("PeakLimitDays", "");
						misJsonArray.add(lclJsonObj);
						if(debug)
							response.getWriter().println("getPeakLimitMIS().misJsonArray.size(): "+misJsonArray.size());
					}else if(misJsonArray.size() == 1){
						if(debug){
							response.getWriter().println("getPeakLimitMIS().misJsonArray.size(): "+misJsonArray.size());
							response.getWriter().println("getPeakLimitMIS().misJsonArray.cpGuid: "+misJsonArray.get(0).getAsJsonObject().get("CPGuid").getAsString());
						}
						if(! misJsonArray.get(0).getAsJsonObject().get("CPGuid").getAsString().equalsIgnoreCase(cpGuid)){
							lclJsonObj = new JsonObject();
							lclJsonObj.addProperty("CPGuid", cpGuid);
							lclJsonObj.addProperty("CPTypeID", cpType);
							lclJsonObj.addProperty("CPName", cpName);
							lclJsonObj.addProperty("PeakLimitCount", "");
							lclJsonObj.addProperty("TotalAmount", "");
							lclJsonObj.addProperty("PeakLimitDays", "");
							misJsonArray.add(lclJsonObj);
						}
					}
				}
				if(debug)
					response.getWriter().println("getPeakLimitMIS().misJsonArray: "+misJsonArray);
				misJsonObj.add("results", misJsonArray);
				/*if(debug)
					response.getWriter().println("misJsonObj: "+misJsonObj);*/
				int misJsonSize = misJsonObj.get("results").getAsJsonArray().size();
				if(debug)
					response.getWriter().println("getPeakLimitMIS().misJsonSize: "+misJsonSize);
				
				SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
				for(int i=0; i < misJsonSize; i++){
					peakLimitCount = 0;
					peakDays = 0;
					totalAmount = 0;
					for(int j = 0; j < eLimitObjSize; j++){
						childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(j).getAsJsonObject();
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
							response.getWriter().println("childELimit.elimtValidFromDate: "+eLimitValidFromDate);
							response.getWriter().println("childELimit.elimtValidToDate: "+eLimitValidToDate);
							response.getWriter().println("fromDate: "+fromDate);
							response.getWriter().println("toDate: "+toDate);
						}
						if (!((eLimitValidFromDate.compareTo(fromDate) < 0 && eLimitValidToDate.compareTo(fromDate)<0 )  
								&& (eLimitValidFromDate.compareTo(fromDate) > 0 && eLimitValidToDate.compareTo(fromDate)> 0))) {
							try{
								currency = childELimit.get("Currency").getAsString();
							}catch (Exception e) {
								currency = "";
							}
							try{
								proposedLimit = childELimit.get("ProposedLimit").getAsString();
							}catch (Exception e) {
								proposedLimit = "";
							}
							if (debug) {
								response.getWriter().println("currency: "+currency);
								response.getWriter().println("proposedLimit: "+proposedLimit);
							}
							if(misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().get("CPGuid").getAsString().equalsIgnoreCase(childELimit.get("CPGuid").getAsString())){		
							
								if (eLimitValidToDate.compareTo(fromDate) >= 0 && eLimitValidFromDate.compareTo(fromDate) <= 0) {
									peakLimitDaysMilli = (eLimitValidToDate.getTime()-fromDate.getTime());
									if (debug)
										response.getWriter().println("eLimitValidToDate.getTime(): "+ eLimitValidToDate.getTime() +" - fromDate.getTime(): "+fromDate.getTime());
								}
								else if (eLimitValidToDate.compareTo(fromDate)>=0 && eLimitValidFromDate.compareTo(fromDate)>=0 && toDate.compareTo(eLimitValidToDate) >= 0) {
									peakLimitDaysMilli = (eLimitValidToDate.getTime()-eLimitValidFromDate.getTime());
									if (debug)
										response.getWriter().println("eLimitValidToDate.getTime(): "+ eLimitValidToDate.getTime() +" - eLimitValidFromDate.getTime(): "+eLimitValidFromDate.getTime());
								}
								else if (eLimitValidToDate.compareTo(fromDate)>=0 && eLimitValidFromDate.compareTo(fromDate)>=0 && toDate.compareTo(eLimitValidToDate) <= 0) {
									peakLimitDaysMilli = (toDate.getTime() -eLimitValidFromDate.getTime());
									if (debug)
										response.getWriter().println("toDate.getTime(): "+ toDate.getTime() +" - eLimitValidFromDate.getTime(): "+eLimitValidFromDate.getTime());
								}
								else if (eLimitValidFromDate.compareTo(fromDate)< 0 && eLimitValidToDate.compareTo(toDate)>0) {
									peakLimitDaysMilli = (toDate.getTime()- fromDate.getTime());
									if (debug)
										response.getWriter().println("toDate.getTime(): "+ toDate.getTime() +" - fromDate.getTime(): "+fromDate.getTime());
								}
								peakLimitDays = ((int) TimeUnit.MILLISECONDS.toDays(peakLimitDaysMilli))+1;
//								peakLimitDays = (int) (peakLimitDaysMilli / (1000*60*60*24))+1;
								try	{
									if(proposedLimit != null)
										proposedLimitAmt = Integer.parseInt(proposedLimit);
								}catch (NumberFormatException e){
									proposedLimitAmt = 0;
								}
								peakLimitCount++;
								totalAmount = totalAmount+proposedLimitAmt;
								peakDays = peakDays+peakLimitDays;
								if(debug){
									response.getWriter().println("peakLimitCount: "+peakLimitCount);
									response.getWriter().println("totalAmount: "+totalAmount);
									response.getWriter().println("peakLimitDays: "+peakDays);
								}
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("Currency", currency);
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("PeakLimitCount", Integer.toString(peakLimitCount));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("TotalAmount", Integer.toString(totalAmount));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("PeakLimitDays", Integer.toString(peakDays));
							misJsonObj.addProperty("StatusCode", "000001");
							misJsonObj.addProperty("Message", "Success");
							}
						}/* else {
							misJsonObj.addProperty("StatusCode", "000002");
							misJsonObj.addProperty("Message", "No records found");
							break;
						}*/
					}
				}
			}else{
//				{"StatusCode":"000002","Message":"Year is mandatory"}
				misJsonObj.addProperty("StatusCode", "000002");
				misJsonObj.addProperty("Message", "No records found");
			}
			if(debug)
				response.getWriter().println("getPeakLimitMIS().misJsonObj: "+misJsonObj);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return misJsonObj;
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
 * Servlet implementation class PeakLimitMIS
 *//*
@WebServlet("/PeakLimitMIS")
public class PeakLimitMIS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    *//**
     * @see HttpServlet#HttpServlet()
     *//*
    public PeakLimitMIS() {
        super();
        // TODO Auto-generated constructor stub
    }

	*//**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 *//*
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String month = "", years = "",monthStartDate = "", monthEndDate = "";
		int monthCode = 0,firstDay = 0,lastDay = 0,year = 0;
		boolean debug = false;
		JsonObject peakLimitMISObj = new JsonObject();
		try {
			if(request.getParameter("debug") != null && request.getParameter("debug").trim().equalsIgnoreCase("true")){
				debug = true;
			}
			month = request.getParameter("Month");
			years = request.getParameter("Year");
			if(debug){
				response.getWriter().println("month: "+month);
				response.getWriter().println("years: "+years);
			}
			if(((month != null && !month.isEmpty())&& (years != null && !years.isEmpty()))){
				try{
					if(StringUtils.isNumeric(month) && StringUtils.isNumeric(years)){
						monthCode = Integer.parseInt(month);
						year = Integer.parseInt(years); 
					}
				}catch (Exception e) {	
					monthCode = 0;
					year = 0;
				}
				if((!StringUtils.isAlpha(month) && StringUtils.isNumeric(month)) && monthCode >=0 && monthCode <=11){			
					if((! StringUtils.isAlpha(years) && StringUtils.isNumeric(years)) && (years.length() == 4)){
						peakLimitMISObj = getPeakLimitMIS(monthCode,year,debug,response);
						response.getWriter().println(peakLimitMISObj);
					}else {
						peakLimitMISObj.addProperty("StatusCode", "000002");
						peakLimitMISObj.addProperty("Message", "Invalid Year received in the request");
						response.getWriter().println(peakLimitMISObj);
					}
				}else {		
					peakLimitMISObj.addProperty("StatusCode", "000002");
					peakLimitMISObj.addProperty("Message", "Invalid Month received in the request");
					response.getWriter().println(peakLimitMISObj);
				}
			}else {		
				peakLimitMISObj.addProperty("StatusCode", "000002");
				peakLimitMISObj.addProperty("Message", "Mandatory Parmeters are Missing");
				response.getWriter().println(peakLimitMISObj);
			}
		} catch (Exception e) {
			peakLimitMISObj.addProperty("StatusCode", "000002");
			peakLimitMISObj.addProperty("Message", "Exception");
			response.getWriter().println(peakLimitMISObj);
		}
	
	}
	
	private JsonObject getPeakLimitMIS(int monthCode, int year, boolean debug,HttpServletResponse response) {
		String aggregatorID = "", cpGuid = "", enhancementType = "", executeUrl = "",oDataUrl = "",userName = "",password = "", cpName="", userPass = "", cpType = "",currency = "",proposedLimit = "",validFrom = "",validTo = "",monthStartDate ="",monthEndDate ="",monthDesc = "";
		int peakLimitCount = 0,totalAmount = 0,proposedLimitAmt = 0,peakLimitDays = 0,peakDays = 0,firstDay = 0,lastDay = 0;
		JsonObject enhancementLimitObj = new JsonObject();
		JsonObject misJsonObj = new JsonObject();
		JsonArray misJsonArray = new JsonArray();
		JsonObject childELimit = new JsonObject();
		CommonUtils commonUtils = new CommonUtils();
		try{
			switch (monthCode) {
			case 0:	monthDesc = "January";
					lastDay = 31;  
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 1: if(monthCode == 1 && (year%4 == 0 ||  year%400 == 0)){
						monthDesc = "February";
						lastDay = 29;  
						if(debug)
							response.getWriter().println("month: "+monthDesc);
					}else if(monthCode == 1 && (year%4 != 0 || year%400 != 0)){
						monthDesc = "February";
						lastDay = 28;  
						if(debug)
							response.getWriter().println("month: "+monthDesc);
					}
					break;
			case 2:	monthDesc = "March";
					lastDay = 31;  
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 3:	monthDesc = "April";
					lastDay = 30;  
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 4:	monthDesc = "May";
					lastDay = 31;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 5:	monthDesc = "June";
					lastDay = 30;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 6:	monthDesc = "July";
					lastDay = 31;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 7:	monthDesc = "August";
					lastDay = 31;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 8:	monthDesc = "September";
					lastDay = 30;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 9:	monthDesc = "October";
					lastDay = 31;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 10:monthDesc = "November";
					lastDay = 30;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;
			case 11:monthDesc = "December";
					lastDay = 31;
					if(debug)
						response.getWriter().println("month: "+monthDesc);
					break;					
			}
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
			
			userName = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
			password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
			userPass = userName+":"+password;
			enhancementType = "000020"; 
			aggregatorID = commonUtils.getODataDestinationProperties("AggregatorID", "PYGWHANA");
			oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");

			executeUrl = oDataUrl+"EnhancementLimits?$filter=%20EnhancementType%20eq%20%27"+enhancementType+"%27%20and%20AggregatorID%20eq%20%27"+aggregatorID+"%27%20and%20(ValidFrom%20le%20datetime%27"+monthEndDate+"%27%20and%20ValidTo%20ge%20datetime%27"+monthStartDate+"%27)";
			if(debug)
				response.getWriter().println("executeUrl: "+executeUrl);
			enhancementLimitObj = commonUtils.executeURL(executeUrl, userPass, response);
			if(debug)
				response.getWriter().println("enhancementLimitsObj: "+enhancementLimitObj);
			int eLimitObjSize = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size();
			if(debug)
				response.getWriter().println("eLimitObjSize: "+eLimitObjSize);
			
			int misJsonArraySize = 0;
			if(debug)
				response.getWriter().println("misJsonObjSize: "+misJsonArraySize);
			if(eLimitObjSize > 0){
				JsonObject lclJsonObj = new JsonObject();
				for(int i=0; i<eLimitObjSize; i++){
					childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(i).getAsJsonObject();
					misJsonArraySize = misJsonArray.size();
					cpGuid = childELimit.get("CPGuid").getAsString();
					cpType = childELimit.get("CPType").getAsString();
					cpName = childELimit.get("CPName").getAsString();
					currency = childELimit.get("Currency").getAsString();
					if(debug)
						response.getWriter().println("cpGuid: "+cpGuid);
					if(misJsonArraySize == 0){
						lclJsonObj.addProperty("CPGuid", cpGuid);
						lclJsonObj.addProperty("CPTypeID", cpType);
						lclJsonObj.addProperty("CPName", cpName);
						lclJsonObj.addProperty("MonthCode", "");
						lclJsonObj.addProperty("MonthDesc", "");
						lclJsonObj.addProperty("Year", "");
						lclJsonObj.addProperty("PeakLimitCount", "");
						lclJsonObj.addProperty("TotalAmount", "");
						lclJsonObj.addProperty("PeakLimitDays", "");
						misJsonArray.add(lclJsonObj);
						if(debug)
							response.getWriter().println("misJsonArray.size(): "+misJsonArray.size());
					}else if(misJsonArray.size() == 1){
						if(debug){
							response.getWriter().println("misJsonArray.size(): "+misJsonArray.size());
							response.getWriter().println("misJsonArray.cpGuid: "+misJsonArray.get(0).getAsJsonObject().get("CPGuid").getAsString());
						}
						if(! misJsonArray.get(0).getAsJsonObject().get("CPGuid").getAsString().equalsIgnoreCase(cpGuid)){
							lclJsonObj = new JsonObject();
							lclJsonObj.addProperty("CPGuid", cpGuid);
							lclJsonObj.addProperty("CPTypeID", cpType);
							lclJsonObj.addProperty("CPName", cpName);
							lclJsonObj.addProperty("MonthCode", "");
							lclJsonObj.addProperty("MonthDesc", "");
							lclJsonObj.addProperty("Year", "");
							lclJsonObj.addProperty("PeakLimitCount", "");
							lclJsonObj.addProperty("TotalAmount", "");
							lclJsonObj.addProperty("PeakLimitDays", "");
							misJsonArray.add(lclJsonObj);
						}
					}
					
				}
				if(debug)
					response.getWriter().println("misJsonArray: "+misJsonArray);
				misJsonObj.add("results", misJsonArray);
				if(debug)
					response.getWriter().println("misJsonObj: "+misJsonObj);
				int misJsonSize = misJsonObj.get("results").getAsJsonArray().size();
				if(debug)
					response.getWriter().println("misJsonSize: "+misJsonSize);
				for(int i=0; i < misJsonSize; i++){
					peakLimitCount = 0;
					peakDays = 0;
					totalAmount = 0;
					for(int j = 0; j < eLimitObjSize; j++){
						childELimit = enhancementLimitObj.get("d").getAsJsonObject().get("results").getAsJsonArray().get(j).getAsJsonObject();
						try{
							currency = childELimit.get("Currency").getAsString();
						}catch (Exception e) {
							currency = "";
						}
						try{
							proposedLimit = childELimit.get("ProposedLimit").getAsString();
						}catch (Exception e) {
							proposedLimit = "";
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
						if(misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().get("CPGuid").getAsString().equalsIgnoreCase(childELimit.get("CPGuid").getAsString())){					
							SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
							calendar.setTime(validFromDate);
							int validFromMonth = calendar.get(Calendar.MONTH);
							calendar.setTime(validToDate);
							int validToMonth = calendar.get(Calendar.MONTH);
							int validToYear = calendar.get(Calendar.YEAR);
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
							peakLimitCount++;					
							totalAmount = totalAmount+proposedLimitAmt;	
							peakDays = peakDays+peakLimitDays;
							if(debug){
								response.getWriter().println("currency: "+currency);
								response.getWriter().println("proposedLimit: "+proposedLimit);
								response.getWriter().println("peakLimitCount: "+peakLimitCount);
								response.getWriter().println("totalAmount: "+totalAmount);
								response.getWriter().println("peakLimitDays: "+peakDays);
							}
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("MonthCode", Integer.toString(monthCode));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("MonthDesc", monthDesc);
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("Year", Integer.toString(year));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("Currency", currency);
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("PeakLimitCount", Integer.toString(peakLimitCount));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("TotalAmount", Integer.toString(totalAmount));
							misJsonObj.get("results").getAsJsonArray().get(i).getAsJsonObject().addProperty("PeakLimitDays", Integer.toString(peakDays));
							misJsonObj.addProperty("StatusCode", "000001");
							misJsonObj.addProperty("Message", "Success");
						}
					}
				}
			}else{
//				{"StatusCode":"000002","Message":"Year is mandatory"}
				misJsonObj.addProperty("StatusCode", "000002");
				misJsonObj.addProperty("Message", "No records found");
			}
			
			if(debug)
				response.getWriter().println("misJsonObj: "+misJsonObj);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return misJsonObj;
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
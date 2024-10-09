package com.arteriatech.support;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SPDistance extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String inputPayload="";
		CommonUtils commonUtils=new CommonUtils();
		JsonParser parser=new JsonParser();
		String username="",password="",oDataUrl="",executeURL="";
		String spguid="",userpass="";
		try {
			inputPayload=commonUtils.getGetBody(request, response);
		    JsonObject 	jsonInput=(JsonObject)parser.parse(inputPayload);
		   String errorMessage= validateInputPayload(jsonInput);
			if (errorMessage.equalsIgnoreCase("")) {
				oDataUrl = commonUtils.getODataDestinationProperties("URL", "PYGWHANA");
				username = commonUtils.getODataDestinationProperties("User", "PYGWHANA");
				password = commonUtils.getODataDestinationProperties("Password", "PYGWHANA");
				userpass=username+":"+password;
				spguid=jsonInput.get("SPGUID").getAsString();
				String distanceTime = jsonInput.get("DistanceTime").getAsString();
				String distanceDate = formatDate(jsonInput.get("DistanceDate").getAsString());
				executeURL=oDataUrl+"SPDistance?$filter=SPGUID%20eq%20%27"+spguid+"%27%20and%20DistanceDate%20eq%20datetime%27"+distanceDate+"T00:00:00%27";
			    JsonObject spdistanceObj = commonUtils.executeURL(executeURL, userpass, response);
			    if(spdistanceObj!=null && !spdistanceObj.has("error") && !spdistanceObj.get("d").getAsJsonObject().isJsonNull()&& !spdistanceObj.get("d").getAsJsonObject().get("results").isJsonNull()&&spdistanceObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size()>0){
			    	String inputTime = jsonInput.get("Time").getAsString();
			    	JsonArray spDistanceArray = spdistanceObj.get("d").getAsJsonObject().get("results").getAsJsonArray();
			    	for(int i=0;i<spDistanceArray.size();i++){
			    		JsonObject distanceObj = spDistanceArray.get(i).getAsJsonObject();
			    		    String dbTime = distanceObj.get("DistanceTime").getAsString();
			    		    Duration inputDuration = Duration.parse(inputTime);
			    		    Duration dbDuration = Duration.parse(dbTime);
			    		    int totalDuration = inputDuration.compareTo(dbDuration);
			    		    if(totalDuration==1){
			    		    	JSONObject inputPaylaod=new JSONObject();
			    		    	inputPaylaod.accumulate("SnappedCoordinates",jsonInput.get("Coordinates"));
			    		    	inputPaylaod.accumulate("DistanceDate","");
			    		    	inputPaylaod.accumulate("DistanceDate","");
			    		    }else{
			    		    	
			    		    }
			    		
			    	}
			    	


			    	
			    }else{
			    	// spdistanceObj distance object doesn't exist
			    }
			}else{
			   
		   }
			
		} catch (Exception ex) {

		}
	}


	private String validateInputPayload(JsonObject jsonInput) {
		String errorMessage="";
		try {
			if (jsonInput.has("SPGUID")) {
				if (jsonInput.get("SPGUID").isJsonNull()
						|| jsonInput.get("SPGUID").getAsString().equalsIgnoreCase("")) {
					errorMessage = "SPGUID Field is Empty in the input payload";
				}
			} else {
				errorMessage = "SPGUID Field Missing on the payload";
			}

			if (jsonInput.has("DistanceDate")) {
				if (jsonInput.get("DistanceDate").isJsonNull()
						|| jsonInput.get("DistanceDate").getAsString().equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "DistanceDate Field is Empty in the input payload";
					} else {
						errorMessage = errorMessage + ",DistanceDate Field is Empty in the input payload";
					}
				}
			} else {
				if (errorMessage.equalsIgnoreCase("")) {
					errorMessage = "DistanceDate Field is Empty in the input payload";
				} else {
					errorMessage = errorMessage + ",DistanceDate Field is Empty in the input payload";
				}
			}

			if (jsonInput.has("DistanceTime")) {
				if (jsonInput.get("DistanceTime").isJsonNull()
						|| jsonInput.get("DistanceTime").getAsString().equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "DistanceTime Field is Empty in the input payload";
					} else {
						errorMessage = errorMessage + ",DistanceTime Field is Empty in the input payload";
					}
				}
			} else {
				if (errorMessage.equalsIgnoreCase("")) {
					errorMessage = "DistanceTime Field is Empty in the input payload";
				} else {
					errorMessage = errorMessage + ",DistanceTime Field is Empty in the input payload";
				}
			}

			return errorMessage;
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < stackTrace.length; i++) {
				buffer.append(stackTrace[i]);
			}
			return buffer.toString();

		}
	}
	
	
	public String formatDate(String date) throws Exception, IOException {
		try {
			Date dateFormate = new SimpleDateFormat("dd-MM-yyyy").parse(date);
			DateFormat foramte = new SimpleDateFormat("yyyy-MM-dd");
			String format2 = foramte.format(dateFormate);
			return format2;
		} catch (Exception ex) {
			throw ex;
		}
	}
}
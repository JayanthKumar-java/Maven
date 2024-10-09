package com.arteriatech.support;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.arteriatech.pg.CommonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.arteriatech.support.DestinationUtils;

public class SnapToRoad extends HttpServlet{

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
		String spguid="",userpass="",aggregatorId="";
		JsonObject responseObj=new JsonObject();
		boolean debug=false;
		try {
			inputPayload=commonUtils.getGetBody(request, response);
		    JsonObject 	jsonInput=(JsonObject)parser.parse(inputPayload);
		   String errorMessage= validateInputPayload(jsonInput);
			if (errorMessage.equalsIgnoreCase("")) {
				oDataUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SPGWHANA);
				username = commonUtils.getODataDestinationProperties("User", DestinationUtils.SPGWHANA);
				password = commonUtils.getODataDestinationProperties("Password", DestinationUtils.SPGWHANA);
				aggregatorId=commonUtils.getODataDestinationProperties("AggregatorID", DestinationUtils.PCGWHANA);
				userpass=username+":"+password;
				String snapRoadAPiUrl = commonUtils.getODataDestinationProperties("URL", DestinationUtils.SNAPROAD_API);
				String apiKey = commonUtils.getODataDestinationProperties("API_KEY", DestinationUtils.SNAPROAD_API);
				if (snapRoadAPiUrl != null && !snapRoadAPiUrl.equalsIgnoreCase("E106")
						&& !snapRoadAPiUrl.startsWith("E173")) {
					String latitude = jsonInput.get("latitude").getAsString();
					String longitude = jsonInput.get("longitude").getAsString();
					String pts = latitude + "," + longitude;
					String time = jsonInput.get("time").getAsString();
					String roadMapUrl = snapRoadAPiUrl + apiKey + "/snapToRoad?pts=" + pts + "&timestamps="
							+ time;
					spguid = jsonInput.get("SPGUID").getAsString();
					String distanceDate = formatDate(jsonInput.get("DistanceDate").getAsString());
					executeURL = oDataUrl + "SPDistance?$filter=SPGUID%20eq%20%27" + spguid
							+ "%27%20and%20DistanceDate%20eq%20datetime%27" + distanceDate + "%27";
					JsonObject spdistanceObj = commonUtils.executeURL(executeURL, userpass, response);
					if (spdistanceObj != null && !spdistanceObj.has("error")
							&& !spdistanceObj.get("d").getAsJsonObject().isJsonNull()
							&& !spdistanceObj.get("d").getAsJsonObject().get("results").isJsonNull()
							&& spdistanceObj.get("d").getAsJsonObject().get("results").getAsJsonArray().size() > 0) {
						String mobileTime = jsonInput.get("Time").getAsString();
						JsonArray spDistanceArray = spdistanceObj.get("d").getAsJsonObject().get("results")
								.getAsJsonArray();
						for (int i = 0; i < spDistanceArray.size(); i++) {
							JsonObject distanceObj = spDistanceArray.get(i).getAsJsonObject();
							String dbTime = distanceObj.get("DistanceTime").getAsString();
							Duration mobileDuration = Duration.parse(mobileTime);
							Duration dbDuration = Duration.parse(dbTime);
							int timediff = mobileDuration.compareTo(dbDuration);
							if (timediff == 1) {
								JsonObject roadMapApiRes = executeRoadMapApi(roadMapUrl);
								if (!roadMapApiRes.has("ErrorCode")) {
									if (roadMapApiRes.get("responsecode").getAsString().equalsIgnoreCase("200")) {
										// calculate the total distance and
										// Update the payload
										JsonObject roadMapObj = roadMapApiRes.get("results").getAsJsonObject();
										if(roadMapObj!=null && roadMapObj.isJsonNull()){
											JsonArray distanceArray = roadMapObj.get("matchings").getAsJsonArray();
											JsonArray coordinateArray = roadMapObj.get("snappedPoints").getAsJsonArray();
											if (coordinateArray != null && !coordinateArray.isJsonNull()
													&& coordinateArray.size() > 0) {
												if (!distanceArray.isJsonNull()
														&& distanceArray.getAsJsonArray().size() > 0) {
													JsonObject geometryObj = distanceArray.get(0).getAsJsonObject();
													if (geometryObj != null
															&& !geometryObj.get("geometry").isJsonNull()) {
														String encryptedDistance = geometryObj.get("geometry")
																.getAsString();
														JsonObject decryPtDistance = calculateDistance(
																encryptedDistance, debug);
														if (decryPtDistance != null && decryPtDistance.get("Status")
																.getAsString().equalsIgnoreCase("000001")) {
															String distance = decryPtDistance.get("Message")
																	.getAsString();
															StringBuffer buffer = new StringBuffer();
															for(int j=0;j<coordinateArray.size();j++){
																JsonObject coordinateObjects = coordinateArray.get(j).getAsJsonObject();
																if(coordinateObjects.get("location").getAsJsonArray().size()>0){
																	StringBuffer coordObj=new StringBuffer();
																	JsonArray jsonArray = coordinateObjects.get("location").getAsJsonArray();
																	String lat = jsonArray.get(0).getAsString();
																	String  longt=jsonArray.get(1).getAsString();
																	coordObj.append("{").append(lat).append(",").append(longt).append("}");
																	buffer.append(coordObj).append(",");
																}
																
															
															}
															buffer.substring(0, buffer.length()-1);
															JSONObject updatePayLoad=new JSONObject();
															updatePayLoad.accumulate("SnappedCoordinates", buffer);
															updatePayLoad.accumulate("Distance", distance);
															String SPDistGUID = distanceObj.get("SPDistGUID").getAsString();
															executeURL=oDataUrl+"SPDistance('"+SPDistGUID+"')";
															JsonObject executeUpdate = commonUtils.executeUpdate(executeURL, username,password, response, updatePayLoad, request, debug);
															if(!executeUpdate.get("ErrorCode").getAsString().equalsIgnoreCase("")){
																response.getWriter().println(executeUpdate);
																break;
															}else{
																responseObj.addProperty("ErrorCode", "");
																responseObj.addProperty("Status", "000001");
																responseObj.addProperty("Message", "Record updated successfully");
																response.getWriter().println(responseObj);
															}
															
														} else {
															response.getWriter().println(decryPtDistance);
															break;
														}

													}else{
														responseObj.addProperty("ErrorCode", "J002");
														responseObj.addProperty("Status", "000002");
														responseObj.addProperty("Message", "Snaproad api doesn't contains a geometry fileds");
														response.getWriter().println(responseObj);
														break;
													}
												} else {
													// distance array is empty
													responseObj.addProperty("ErrorCode", "J002");
													responseObj.addProperty("Status", "000002");
													responseObj.addProperty("Message", "Snaproad api doesn't contains a matchings fileds");
													response.getWriter().println(responseObj);
													break;
												}
											}else{
												// snap road coordinate array is empty
												
												responseObj.addProperty("ErrorCode", "J002");
												responseObj.addProperty("Status", "000002");
												responseObj.addProperty("Message", "Snaproad api doesn't contains a snappedPoints");
												response.getWriter().println(responseObj);
												break;
											}
										}else{
											// roadm map api is empty
											responseObj.addProperty("ErrorCode", "J002");
											responseObj.addProperty("Status", "000002");
											responseObj.addProperty("Message", "Snaproad api doesn't contains results filed or empty results object received");
											response.getWriter().println(responseObj);
											break;
										}

									} else {
										responseObj.addProperty("ErrorCode", "J002");
										responseObj.addProperty("Status", "000002");
										responseObj.add("Message", roadMapApiRes);
										response.getWriter().println(responseObj);
										break;
									}
								} else {
									response.getWriter().println(roadMapApiRes);
									break;
								}

							} else {
								// no need to call snap road api
								responseObj.addProperty("ErrorCode", "");
								responseObj.addProperty("Status", "000001");
								responseObj.addProperty("Message", "Already record exist for the give time: "+time);
								response.getWriter().println(responseObj);

							}

						}
					} else {
						//  if record not exist in the table . call the snap to road api and insert the new record
						if (spdistanceObj != null && !spdistanceObj.has("error") && spdistanceObj.get("d")
								.getAsJsonObject().get("results").getAsJsonArray().size() == 0) {
							JsonObject roadMapApiRes = executeRoadMapApi(roadMapUrl);
							if (!roadMapApiRes.has("ErrorCode")) {
								if (roadMapApiRes.get("responsecode").getAsString().equalsIgnoreCase("200")) {
									JsonObject roadMapObj = roadMapApiRes.get("results").getAsJsonObject();
									if(roadMapObj!=null && roadMapObj.isJsonNull()){
										JsonArray distanceArray = roadMapObj.get("matchings").getAsJsonArray();
										JsonArray coordinateArray = roadMapObj.get("snappedPoints").getAsJsonArray();
										if (coordinateArray != null && !coordinateArray.isJsonNull()
												&& coordinateArray.size() > 0) {
											if (!distanceArray.isJsonNull()
													&& distanceArray.getAsJsonArray().size() > 0) {
												JsonObject geometryObj = distanceArray.get(0).getAsJsonObject();
												if (geometryObj != null
														&& !geometryObj.get("geometry").isJsonNull()) {
													String encryptedDistance = geometryObj.get("geometry")
															.getAsString();
													JsonObject decryPtDistance = calculateDistance(
															encryptedDistance, debug);
													if (decryPtDistance != null && decryPtDistance.get("Status")
															.getAsString().equalsIgnoreCase("000001")) {
														String distance = decryPtDistance.get("Message")
																.getAsString();
														StringBuffer buffer = new StringBuffer();
														for(int j=0;j<coordinateArray.size();j++){
															JsonObject coordinateObjects = coordinateArray.get(j).getAsJsonObject();
															if(coordinateObjects.get("location").getAsJsonArray().size()>0){
																StringBuffer coordObj=new StringBuffer();
																JsonArray jsonArray = coordinateObjects.get("location").getAsJsonArray();
																String lat = jsonArray.get(0).getAsString();
																String  longt=jsonArray.get(1).getAsString();
																coordObj.append("{").append(lat).append(",").append(longt).append("}");
																buffer.append(coordObj).append(",");
															}
															
														
														}
														buffer.substring(0, buffer.length()-1);
														JSONObject inserObject=new JSONObject();
														String spdistGuid = commonUtils.generateGUID(36);
														inserObject.accumulate("SPDistGUID", spdistGuid);
														inserObject.accumulate("SPGUID", spguid);
														inserObject.accumulate("Distance", distance);
														inserObject.accumulate("SnappedCoordinates", buffer);
														inserObject.accumulate("AggregatorID", aggregatorId);
														inserObject.accumulate("DistanceDate", aggregatorId);
														JsonObject resObj = commonUtils.executePostURLTemp(executeURL, userpass, response, inserObject, request, debug, "SPGWHANA");
														if (resObj == null || resObj.has("error")) {
															responseObj.addProperty("ErrorCode", "J002");
															responseObj.addProperty("Status", "000002");
															if (resObj != null) {
																responseObj.add("Message", resObj);
															} else {
																responseObj.addProperty("Message",
																		"Record not inserted");
															}
															response.getWriter().println(responseObj);
														}else{
															responseObj.addProperty("ErrorCode", "J002");
															responseObj.addProperty("Status", "000002");
															responseObj.addProperty("Message", "Record inserted successfully");
															response.getWriter().println(responseObj);
														}
													} else {
														response.getWriter().println(decryPtDistance);
													
													}

												
													
													
												}else{
													// response from roadmap spi geometry object is empty
													responseObj.addProperty("ErrorCode", "J002");
													responseObj.addProperty("Status", "000002");
													responseObj.addProperty("Message", "Snaproad api response doesn't contains a geometry fileds");
													response.getWriter().println(responseObj);
												}
												
												
											}else{
												// distance object not exist
												responseObj.addProperty("ErrorCode", "J002");
												responseObj.addProperty("Status", "000002");
												responseObj.addProperty("Message", "Snaproad api response doesn't contains a matchings fileds");
												response.getWriter().println(responseObj);
											}
											
										}else{
											// empty snapped array
											
											responseObj.addProperty("ErrorCode", "J002");
											responseObj.addProperty("Status", "000002");
											responseObj.addProperty("Message", "Snaproad api response doesn't contains a snappedPoints");
											response.getWriter().println(responseObj);
										}
										
									}else{
										
										// empty coordinates received
										responseObj.addProperty("ErrorCode", "J002");
										responseObj.addProperty("Status", "000002");
										responseObj.addProperty("Message", "Snaproad api response doesn't contains a results fields");
										response.getWriter().println(responseObj);
									}
									
									
								}else{
									responseObj.addProperty("ErrorCode", "J002");
									responseObj.addProperty("Status", "000002");
									responseObj.add("Message", roadMapApiRes);
									response.getWriter().println(responseObj);
								}
							}else{
								response.getWriter().println(roadMapApiRes);
							}
						} else {
							responseObj.add("Message", spdistanceObj);
							responseObj.addProperty("Status", "000002");
							responseObj.addProperty("ErrorCode", "J002");
							response.getWriter().println(responseObj);
						}

					}

				}else{
			    	// snap road api doesn't exist  response object
					responseObj.addProperty("Message", "SNAPROADAPI Destination not exist");
					responseObj.addProperty("Status", "000002");
					responseObj.addProperty("ErrorCode", "J002");
					response.getWriter().println(responseObj);
			    }
			}else{
				responseObj.addProperty("Message", errorMessage);
				responseObj.addProperty("Status", "000002");
				responseObj.addProperty("ErrorCode", "J002");
				response.getWriter().println(responseObj);
			}
			
		} catch (Exception ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			responseObj.addProperty("Exception StackTrace", buffer.toString());
			responseObj.addProperty("Message", ex.getLocalizedMessage());
			responseObj.addProperty("Status", "000002");
			responseObj.addProperty("ErrorCode", "J002");
			response.getWriter().println(responseObj);
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
			if (jsonInput.has("Time")) {
				if (jsonInput.get("Time").isJsonNull()
						|| jsonInput.get("Time").getAsString().equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "Time Field is Empty in the input payload";
					} else {
						errorMessage = errorMessage + ",Time Field is Empty in the input payload";
					}
				}
			} else {
				if (errorMessage.equalsIgnoreCase("")) {
					errorMessage = "Time Field is Empty in the input payload";
				} else {
					errorMessage = errorMessage + ",Time Field is Empty in the input payload";
				}
			}
			
			if (jsonInput.has("latitude")) {
				if (jsonInput.get("latitude").isJsonNull()
						|| jsonInput.get("latitude").getAsString().equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "latitude Field is Empty in the input payload";
					} else {
						errorMessage = errorMessage + ",latitude Field is Empty in the input payload";
					}
				}
			} else {
				if (errorMessage.equalsIgnoreCase("")) {
					errorMessage = "latitude Field is Empty in the input payload";
				} else {
					errorMessage = errorMessage + ",latitude Field is Empty in the input payload";
				}
			}
			
			if (jsonInput.has("longitude")) {
				if (jsonInput.get("longitude").isJsonNull()
						|| jsonInput.get("longitude").getAsString().equalsIgnoreCase("")) {
					if (errorMessage.equalsIgnoreCase("")) {
						errorMessage = "longitude Field is Empty in the input payload";
					} else {
						errorMessage = errorMessage + ",longitude Field is Empty in the input payload";
					}
				}
			} else {
				if (errorMessage.equalsIgnoreCase("")) {
					errorMessage = "longitude Field is Empty in the input payload";
				} else {
					errorMessage = errorMessage + ",longitude Field is Empty in the input payload";
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
	
	public JsonObject executeRoadMapApi(String executeURL){
		JsonObject resObj=null;
		BufferedReader in =null;
		try{
			URL urlObj = new URL(executeURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type","application/json");
			connection.setRequestProperty("Accept","application/json");
			connection.setDoInput(true);
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8));
			String inputLine;
			StringBuffer responseStrBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseStrBuffer.append(inputLine);
			}
			JsonParser parser = new JsonParser();
			resObj = (JsonObject)parser.parse(responseStrBuffer.toString());
			return resObj;
		}catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			resObj=new JsonObject();
			resObj.addProperty("Status", "000002");
			resObj.addProperty("ErrorCode", "J002");
			resObj.addProperty("ExceptionMessage", ex.getLocalizedMessage());
			resObj.addProperty("Excpetion StackTrace", buffer.toString());
			return resObj;
		}
	}
	
	
	public JsonObject calculateDistance(String encryptedTxt,boolean debug){
		JsonObject resObj=new JsonObject();
        try{
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		if(engine!=null){
			try{
			 String filePath = getServletContext().getRealPath("/Resources/script/distancecalscript.js");
			  engine.eval(new FileReader(filePath));
			  Invocable invocable = (Invocable)engine; 
			  Object decryptObj = invocable.invokeFunction("decryptDistance", encryptedTxt);
			  resObj.addProperty("Message", decryptObj.toString());
			  resObj.addProperty("Status", "000001");
			  resObj.addProperty("ErrorCode", "");
			}catch(Exception ex){
				StackTraceElement[] stackTrace = ex.getStackTrace();
				StringBuffer buffer=new StringBuffer();
				for(int i=0;i<stackTrace.length;i++){
					buffer.append(stackTrace[i]);
				}
				  resObj.addProperty("Exception Message", ex.getLocalizedMessage());
				  resObj.addProperty("Exception StackTrace", buffer.toString());
				  resObj.addProperty("Status", "000002");
				  resObj.addProperty("ErrorCode", "J002");
			}
			
		}else{
			  resObj.addProperty("Message", "Unable to create a nashorn Java script engine");
			  resObj.addProperty("Status", "000002");
			  resObj.addProperty("ErrorCode", "J002");
		}
		
        }catch(Exception ex){
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<stackTrace.length;i++){
				buffer.append(stackTrace[i]);
			}
			  resObj.addProperty("Exception Message", ex.getLocalizedMessage());
			  resObj.addProperty("Exception StackTrace", buffer.toString());
			  resObj.addProperty("Status", "000002");
			  resObj.addProperty("ErrorCode", "J002");
        }
       return resObj;
	}
	
	

}

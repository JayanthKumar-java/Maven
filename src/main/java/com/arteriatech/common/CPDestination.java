package com.arteriatech.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import io.vavr.control.Try;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.cloud.sdk.cloudplatform.connectivity.*;
/**
 * Servlet implementation class CPDestination
 */
@WebServlet("/CPDestination")
public class CPDestination extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CPDestination() {
        super();
        // TODO Auto-generated constructor stub
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String payload="";
		AtomicBoolean debug=new AtomicBoolean(false);
		 JSONObject retJsonObj = new JSONObject();
		 JSONArray retJSONArray = null;
		 JSONObject retMainJsonObj = new JSONObject();
		JsonObject resObj=new JsonObject();
		
		if(request.getParameter("debug")!=null && request.getParameter("debug").equalsIgnoreCase("true")){
			
			debug.set(true);
		}
		if(request.getParameter("destination")!=null && !request.getParameter("destination").equalsIgnoreCase("")){
			payload=request.getParameter("destination");
		}

		if(debug.get()){
			response.getWriter().println("inpuy Payload:"+payload);
		}

		if(payload!=null && !payload.equalsIgnoreCase("")){
		JSONArray jsonArray = new JSONArray(payload);
		try{
		DestinationOptions options = DestinationOptions.builder().augmentBuilder(ScpCfDestinationOptionsAugmenter
							.augmenter().tokenExchangeStrategy(ScpCfDestinationTokenExchangeStrategy.LOOKUP_ONLY)).build();
				Map<String, String> data = new HashMap<String, String>();
				JSONObject jsonobject = null;
			for(int i=0;i<jsonArray.length();i++){
					jsonobject = jsonArray.getJSONObject(i);
					String destName = jsonobject.getString("destinationName");
					if(null != request.getParameter("debug") && request.getParameter("debug").equalsIgnoreCase("true"))
			   			response.getWriter().println("destinationName: "+destName);

					Try<Destination> destinationAccessor = DestinationAccessor.getLoader()
							.tryGetDestination(destName, options);
					Destination destination = destinationAccessor.get();
					if(destination!=null){
							retJSONArray = new JSONArray();

							String authentication=destination.get("Authentication").get().toString();
							if(debug.get()){
								response.getWriter().println("authentication:"+authentication);
							}
							Iterable<String> iterable=destination.getPropertyNames();
							iterable.forEach(name->{
								try{
									if(debug.get()){
										response.getWriter().println("property: "+name);
									}

									if(authentication.equalsIgnoreCase("BasicAuthentication")){
										if(!name.equalsIgnoreCase("Password")){
											data.put(name,destination.get(name).get().toString().toString());
										}
									}else if(authentication.equalsIgnoreCase("OAuth2ClientCredentials")){
										if(!name.equalsIgnoreCase("Token Service Password")&&!name.equalsIgnoreCase("clientSecret"))
										data.put(name,destination.get(name).get().toString().toString());
									}else if(authentication.equalsIgnoreCase("OAuth2JWTBearer")){
										if(!name.equalsIgnoreCase("clientSecret"))
										data.put(name,destination.get(name).get().toString().toString());

									}else if(authentication.equalsIgnoreCase("OAuth2Password")){
										if(!name.equalsIgnoreCase("clientSecret")&&!name.equalsIgnoreCase("Password"))
										data.put(name,destination.get(name).get().toString().toString());
									}else if(authentication.equalsIgnoreCase("OAuth2RefreshToken")){
										if(!name.equalsIgnoreCase("clientSecret"))
										data.put(name,destination.get(name).get().toString().toString());
									}else if(authentication.equalsIgnoreCase("OAuth2SAMLBearerAssertion")){
										if(!name.equalsIgnoreCase("clientSecret")&&!name.equalsIgnoreCase("Key Store Password")&&!name.equalsIgnoreCase("Token Service Password")){
											data.put(name,destination.get(name).get().toString().toString());
										}
									}else if(authentication.equalsIgnoreCase("OAuth2UserTokenExchange")){
										if(!name.equalsIgnoreCase("clientSecret")){
											data.put(name,destination.get(name).get().toString().toString());
										}
									}else{
										data.put(name,destination.get(name).get().toString().toString());	
									}
								}catch(Exception ex){
									data.put(name,ex.getLocalizedMessage());
								}
						});
						retJsonObj.put(destName, data);
						retJSONArray.put(retJsonObj);
						retMainJsonObj.put("destination", retJSONArray);
						if(debug.get()){
							response.getWriter().println(retMainJsonObj);
						}
							
					}else{
						if(debug.get()){
							response.getWriter().println("Destination Not Found: "+destName);
						}
						data.put("ErrorCode", "001");
						data.put("ErrorMsg", "Destination Not Found");
						retJsonObj.put(destName, data);
						retJSONArray.put(retJsonObj);
						retMainJsonObj.put("destination", retJSONArray);
						if(debug.get()){
							response.getWriter().println(retMainJsonObj);
						}
					}
			}
			response.getWriter().println(retMainJsonObj);
		}catch(Exception ex){
			response.getWriter().println("getLocalizedMessage: "+ex.getLocalizedMessage());
			response.getWriter().println("getMessage: "+ex.getMessage());
		}
	}else{
		JsonObject result = new JsonObject();
		if(null == payload)
			{
					result.addProperty("ErrorCode", "002");
					result.addProperty("ErrorMsg", "Parameter 'destination' not found in request");
			}
			else
			{
					result.addProperty("ErrorCode", "003");
					result.addProperty("ErrorMsg", "Parameter 'destination' is empty");
			}
			response.getWriter().println(result);
	}
}
}
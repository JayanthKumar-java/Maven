package com.arteriatech.aml;

import com.google.gson.JsonObject;

public class AMLEligibilityResponse extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JsonObject resObj;
	
	public AMLEligibilityResponse(JsonObject resObj) {
		super();
		this.resObj = resObj;
	}

	public JsonObject getResObj() {
		return resObj;
	}
	
	public void setResObj(JsonObject resObj) {
		this.resObj = resObj;
	}
}
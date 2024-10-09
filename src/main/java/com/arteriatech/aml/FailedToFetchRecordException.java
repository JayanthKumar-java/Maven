package com.arteriatech.aml;

import com.google.gson.JsonObject;

public class FailedToFetchRecordException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JsonObject errorObj;
	private String message;
	private String status;
	private String errorCode;
	public FailedToFetchRecordException(JsonObject errorObj, String message, String status, String errorCode) {
		this.errorObj = errorObj;
		this.message = message;
		this.status = status;
		this.errorCode = errorCode;
	}
	/**
	 * @return the errorObj
	 */
	public JsonObject getErrorObj() {
		return errorObj;
	}
	/**
	 * @param errorObj the errorObj to set
	 */
	public void setErrorObj(JsonObject errorObj) {
		this.errorObj = errorObj;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}
	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	
	
	

}

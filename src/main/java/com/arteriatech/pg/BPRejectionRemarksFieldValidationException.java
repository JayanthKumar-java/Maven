package com.arteriatech.pg;

public class BPRejectionRemarksFieldValidationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public BPRejectionRemarksFieldValidationException(String message) {
		this.message=message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}

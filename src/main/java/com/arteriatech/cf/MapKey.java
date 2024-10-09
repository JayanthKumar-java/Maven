package com.arteriatech.cf;

import java.util.Objects;

public class MapKey {
	
	private String cpGuid;
	private String cpType;
	private String emailID;
	
	
	public MapKey(String cpGuid, String cpType, String emailID) {
		this.cpGuid = cpGuid;
		this.cpType = cpType;
		this.emailID = emailID;
	}
	
	
	
	
	/**
	 * @return the cpGuid
	 */
	public String getCpGuid() {
		return cpGuid;
	}
	/**
	 * @param cpGuid the cpGuid to set
	 */
	public void setCpGuid(String cpGuid) {
		this.cpGuid = cpGuid;
	}
	/**
	 * @return the cpType
	 */
	public String getCpType() {
		return cpType;
	}
	/**
	 * @param cpType the cpType to set
	 */
	public void setCpType(String cpType) {
		this.cpType = cpType;
	}
	/**
	 * @return the emailID
	 */
	public String getEmailID() {
		return emailID;
	}
	/**
	 * @param emailID the emailID to set
	 */
	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(cpGuid,cpType,emailID);
	  }
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		if (emailID != null) {
			MapKey mapKey = (MapKey) o;
			return Objects.equals(cpGuid, mapKey.getCpGuid()) && Objects.equals(cpType, mapKey.getCpType()) && emailID.equalsIgnoreCase(mapKey.getEmailID());
		} else {
			return false;
		}
	}
	
	
	
	
	

}

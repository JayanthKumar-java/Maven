package com.arteriatech.cf;

public class UniqueCpGuidsAndCpType{
	
	private String cpGuid;
	private String cpType;
	
	public UniqueCpGuidsAndCpType(String cpGuid, String cpType) {
		this.cpGuid = cpGuid;
		this.cpType = cpType;
	}
	
	public String getCpGuid() {
		return cpGuid;
	}

	public void setCpGuid(String cpGuid) {
		this.cpGuid = cpGuid;
	}

	public String getCpType() {
		return cpType;
	}

	public void setCpType(String cpType) {
		this.cpType = cpType;
	}

	
	@Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cpGuid == null) ? 0 : cpGuid.hashCode());
      return result;
    }
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null)
          return false;
        if (getClass() != obj.getClass())
          return false;
        UniqueCpGuidsAndCpType cpGuids = (UniqueCpGuidsAndCpType) obj;
		if (this.cpGuid != null && this.cpType != null && cpGuids.getCpGuid() != null && cpGuids.getCpType() != null) {
			if (this.cpGuid.equals(cpGuids.getCpGuid()) && this.cpType.equals(cpGuids.getCpType())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
       
      }
	
	

}

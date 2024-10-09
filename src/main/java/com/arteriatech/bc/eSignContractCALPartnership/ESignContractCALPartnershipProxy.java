package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnershipProxy implements com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership {
  private String _endpoint = null;
  private com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership eSignContractCALPartnership = null;
  
  public ESignContractCALPartnershipProxy() {
    _initESignContractCALPartnershipProxy();
  }
  
  public ESignContractCALPartnershipProxy(String endpoint) {
    _endpoint = endpoint;
    _initESignContractCALPartnershipProxy();
  }
  
  private void _initESignContractCALPartnershipProxy() {
    try {
      eSignContractCALPartnership = (new com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipServiceLocator()).geteSignContractCALPartnershipPort();
      if (eSignContractCALPartnership != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)eSignContractCALPartnership)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)eSignContractCALPartnership)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (eSignContractCALPartnership != null)
      ((javax.xml.rpc.Stub)eSignContractCALPartnership)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership getESignContractCALPartnership() {
    if (eSignContractCALPartnership == null)
      _initESignContractCALPartnershipProxy();
    return eSignContractCALPartnership;
  }
  
  public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_Response eSignContractCALPartnership(com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_Request eSignContractCALPartnership_Request) throws java.rmi.RemoteException{
    if (eSignContractCALPartnership == null)
      _initESignContractCALPartnershipProxy();
    return eSignContractCALPartnership.eSignContractCALPartnership(eSignContractCALPartnership_Request);
  }
  
  
}
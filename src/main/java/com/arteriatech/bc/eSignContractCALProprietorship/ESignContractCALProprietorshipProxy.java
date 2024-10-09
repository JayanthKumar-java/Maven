package com.arteriatech.bc.eSignContractCALProprietorship;

public class ESignContractCALProprietorshipProxy implements com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship {
  private String _endpoint = null;
  private com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship eSignContractCALProprietorship = null;
  
  public ESignContractCALProprietorshipProxy() {
    _initESignContractCALProprietorshipProxy();
  }
  
  public ESignContractCALProprietorshipProxy(String endpoint) {
    _endpoint = endpoint;
    _initESignContractCALProprietorshipProxy();
  }
  
  private void _initESignContractCALProprietorshipProxy() {
    try {
      eSignContractCALProprietorship = (new com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorshipServiceLocator()).geteSignContractCALProprietorshipPort();
      if (eSignContractCALProprietorship != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)eSignContractCALProprietorship)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)eSignContractCALProprietorship)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (eSignContractCALProprietorship != null)
      ((javax.xml.rpc.Stub)eSignContractCALProprietorship)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship getESignContractCALProprietorship() {
    if (eSignContractCALProprietorship == null)
      _initESignContractCALProprietorshipProxy();
    return eSignContractCALProprietorship;
  }
  
  public com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_Response eSignContractCALProprietorship(com.arteriatech.bc.eSignContractCALProprietorship.ESignContractCALProprietorship_Request eSignContractCALProprietorship_Request) throws java.rmi.RemoteException{
    if (eSignContractCALProprietorship == null)
      _initESignContractCALProprietorshipProxy();
    return eSignContractCALProprietorship.eSignContractCALProprietorship(eSignContractCALProprietorship_Request);
  }
  
  
}
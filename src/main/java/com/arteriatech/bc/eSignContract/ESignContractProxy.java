package com.arteriatech.bc.eSignContract;

public class ESignContractProxy implements com.arteriatech.bc.eSignContract.ESignContract_PortType {
  private String _endpoint = null;
  private com.arteriatech.bc.eSignContract.ESignContract_PortType eSignContract_PortType = null;
  
  public ESignContractProxy() {
    _initESignContractProxy();
  }
  
  public ESignContractProxy(String endpoint) {
    _endpoint = endpoint;
    _initESignContractProxy();
  }
  
  private void _initESignContractProxy() {
    try {
      eSignContract_PortType = (new com.arteriatech.bc.eSignContract.ESignContract_ServiceLocator()).geteSignContract();
      if (eSignContract_PortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)eSignContract_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)eSignContract_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (eSignContract_PortType != null)
      ((javax.xml.rpc.Stub)eSignContract_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.eSignContract.ESignContract_PortType getESignContract_PortType() {
    if (eSignContract_PortType == null)
      _initESignContractProxy();
    return eSignContract_PortType;
  }
  
  public com.arteriatech.bc.eSignContract.ESignContractResponse eSignContract(com.arteriatech.bc.eSignContract.ESignContractRequest eSignContractRequest) throws java.rmi.RemoteException{
    if (eSignContract_PortType == null)
      _initESignContractProxy();
    return eSignContract_PortType.eSignContract(eSignContractRequest);
  }
  
  
}
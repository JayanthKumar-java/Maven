package com.arteriatech.bc.SCFAccount;

public class SCFAccountProxy implements com.arteriatech.bc.SCFAccount.SCFAccount {
  private String _endpoint = null;
  private com.arteriatech.bc.SCFAccount.SCFAccount sCFAccount = null;
  
  public SCFAccountProxy() {
    _initSCFAccountProxy();
  }
  
  public SCFAccountProxy(String endpoint) {
    _endpoint = endpoint;
    _initSCFAccountProxy();
  }
  
  private void _initSCFAccountProxy() {
    try {
      sCFAccount = (new com.arteriatech.bc.SCFAccount.SCFAccountServiceLocator()).getSCFAccountPort();
      if (sCFAccount != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sCFAccount)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sCFAccount)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sCFAccount != null)
      ((javax.xml.rpc.Stub)sCFAccount)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.SCFAccount.SCFAccount getSCFAccount() {
    if (sCFAccount == null)
      _initSCFAccountProxy();
    return sCFAccount;
  }
  
  public com.arteriatech.bc.SCFAccount.SCFAccount_Response SCFAccount(com.arteriatech.bc.SCFAccount.SCFAccount_Request SCFAccount_Request) throws java.rmi.RemoteException{
    if (sCFAccount == null)
      _initSCFAccountProxy();
    return sCFAccount.SCFAccount(SCFAccount_Request);
  }
  
  
}
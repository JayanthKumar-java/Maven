package com.arteriatech.bc.SCFApply;

public class SCFApplyProxy implements com.arteriatech.bc.SCFApply.SCFApply {
  private String _endpoint = null;
  private com.arteriatech.bc.SCFApply.SCFApply sCFApply = null;
  
  public SCFApplyProxy() {
    _initSCFApplyProxy();
  }
  
  public SCFApplyProxy(String endpoint) {
    _endpoint = endpoint;
    _initSCFApplyProxy();
  }
  
  private void _initSCFApplyProxy() {
    try {
      sCFApply = (new com.arteriatech.bc.SCFApply.SCFApplyServiceLocator()).getSCFApplyPort();
      if (sCFApply != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sCFApply)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sCFApply)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sCFApply != null)
      ((javax.xml.rpc.Stub)sCFApply)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.SCFApply.SCFApply getSCFApply() {
    if (sCFApply == null)
      _initSCFApplyProxy();
    return sCFApply;
  }
  
  public com.arteriatech.bc.SCFApply.SCFApplyResponse SCFApply(com.arteriatech.bc.SCFApply.SCFApplyRequest SCFApplyRequest) throws java.rmi.RemoteException{
    if (sCFApply == null)
      _initSCFApplyProxy();
    return sCFApply.SCFApply(SCFApplyRequest);
  }
  
  
}
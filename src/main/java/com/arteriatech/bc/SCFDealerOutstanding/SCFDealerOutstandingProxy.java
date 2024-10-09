package com.arteriatech.bc.SCFDealerOutstanding;

public class SCFDealerOutstandingProxy implements com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding {
  private String _endpoint = null;
  private com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding sCFDealerOutstanding = null;
  
  public SCFDealerOutstandingProxy() {
    _initSCFDealerOutstandingProxy();
  }
  
  public SCFDealerOutstandingProxy(String endpoint) {
    _endpoint = endpoint;
    _initSCFDealerOutstandingProxy();
  }
  
  private void _initSCFDealerOutstandingProxy() {
    try {
      sCFDealerOutstanding = (new com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingServiceLocator()).getSCFDealerOutstandingPort();
      if (sCFDealerOutstanding != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sCFDealerOutstanding)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sCFDealerOutstanding)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sCFDealerOutstanding != null)
      ((javax.xml.rpc.Stub)sCFDealerOutstanding)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding getSCFDealerOutstanding() {
    if (sCFDealerOutstanding == null)
      _initSCFDealerOutstandingProxy();
    return sCFDealerOutstanding;
  }
  
  public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_Response SCFDealerOutstanding(com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_Request SCFDealerOutstanding_Request) throws java.rmi.RemoteException{
    if (sCFDealerOutstanding == null)
      _initSCFDealerOutstandingProxy();
    return sCFDealerOutstanding.SCFDealerOutstanding(SCFDealerOutstanding_Request);
  }
  
  
}
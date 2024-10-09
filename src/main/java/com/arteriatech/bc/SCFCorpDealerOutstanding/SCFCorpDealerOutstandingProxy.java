package com.arteriatech.bc.SCFCorpDealerOutstanding;

public class SCFCorpDealerOutstandingProxy implements com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding {
  private String _endpoint = null;
  private com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding sCFCorpDealerOutstanding = null;
  
  public SCFCorpDealerOutstandingProxy() {
    _initSCFCorpDealerOutstandingProxy();
  }
  
  public SCFCorpDealerOutstandingProxy(String endpoint) {
    _endpoint = endpoint;
    _initSCFCorpDealerOutstandingProxy();
  }
  
  private void _initSCFCorpDealerOutstandingProxy() {
    try {
      sCFCorpDealerOutstanding = (new com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingServiceLocator()).getSCFCorpDealerOutstandingPort();
      if (sCFCorpDealerOutstanding != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sCFCorpDealerOutstanding)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sCFCorpDealerOutstanding)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sCFCorpDealerOutstanding != null)
      ((javax.xml.rpc.Stub)sCFCorpDealerOutstanding)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding getSCFCorpDealerOutstanding() {
    if (sCFCorpDealerOutstanding == null)
      _initSCFCorpDealerOutstandingProxy();
    return sCFCorpDealerOutstanding;
  }
  
  public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponse SCFCorpDealerOutstanding(com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_Request SCFCorpDealerOutstanding_Request) throws java.rmi.RemoteException{
    if (sCFCorpDealerOutstanding == null)
      _initSCFCorpDealerOutstandingProxy();
    return sCFCorpDealerOutstanding.SCFCorpDealerOutstanding(SCFCorpDealerOutstanding_Request);
  }
  
  
}
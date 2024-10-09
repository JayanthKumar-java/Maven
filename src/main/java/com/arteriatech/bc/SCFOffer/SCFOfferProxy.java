package com.arteriatech.bc.SCFOffer;

public class SCFOfferProxy implements com.arteriatech.bc.SCFOffer.SCFOffer {
  private String _endpoint = null;
  private com.arteriatech.bc.SCFOffer.SCFOffer sCFOffer = null;
  
  public SCFOfferProxy() {
    _initSCFOfferProxy();
  }
  
  public SCFOfferProxy(String endpoint) {
    _endpoint = endpoint;
    _initSCFOfferProxy();
  }
  
  private void _initSCFOfferProxy() {
    try {
      sCFOffer = (new com.arteriatech.bc.SCFOffer.SCFOfferServiceLocator()).getSCFOfferPort();
      if (sCFOffer != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sCFOffer)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sCFOffer)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sCFOffer != null)
      ((javax.xml.rpc.Stub)sCFOffer)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.SCFOffer.SCFOffer getSCFOffer() {
    if (sCFOffer == null)
      _initSCFOfferProxy();
    return sCFOffer;
  }
  
  public com.arteriatech.bc.SCFOffer.SCFOffer_Response SCFOffer(com.arteriatech.bc.SCFOffer.SCFOffer_Request SCFOffer_Request) throws java.rmi.RemoteException{
    if (sCFOffer == null)
      _initSCFOfferProxy();
    return sCFOffer.SCFOffer(SCFOffer_Request);
  }
  
  
}
package com.arteriatech.bc.ODCorpAccountMISPublish;

public class SI_MISPublishOnDemand_RequestProxy implements com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request {
  private String _endpoint = null;
  private com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request sI_MISPublishOnDemand_Request = null;
  
  public SI_MISPublishOnDemand_RequestProxy() {
    _initSI_MISPublishOnDemand_RequestProxy();
  }
  
  public SI_MISPublishOnDemand_RequestProxy(String endpoint) {
    _endpoint = endpoint;
    _initSI_MISPublishOnDemand_RequestProxy();
  }
  
  private void _initSI_MISPublishOnDemand_RequestProxy() {
    try {
      sI_MISPublishOnDemand_Request = (new com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestServiceLocator()).getSI_MISPublishOnDemand_RequestPort();
      if (sI_MISPublishOnDemand_Request != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sI_MISPublishOnDemand_Request)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sI_MISPublishOnDemand_Request)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sI_MISPublishOnDemand_Request != null)
      ((javax.xml.rpc.Stub)sI_MISPublishOnDemand_Request)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request getSI_MISPublishOnDemand_Request() {
    if (sI_MISPublishOnDemand_Request == null)
      _initSI_MISPublishOnDemand_RequestProxy();
    return sI_MISPublishOnDemand_Request;
  }
  
  public void SI_MISPublishOnDemand_Request(com.arteriatech.bc.ODCorpAccountMISPublish.MISPublishOnDemand_RequestRoot[] MT_MISPublishOnDemand_Request) throws java.rmi.RemoteException{
    if (sI_MISPublishOnDemand_Request == null)
      _initSI_MISPublishOnDemand_RequestProxy();
    sI_MISPublishOnDemand_Request.SI_MISPublishOnDemand_Request(MT_MISPublishOnDemand_Request);
  }
  
  
}
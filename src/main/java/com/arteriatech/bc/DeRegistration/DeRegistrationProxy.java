package com.arteriatech.bc.DeRegistration;

public class DeRegistrationProxy implements com.arteriatech.bc.DeRegistration.DeRegistration {
  private String _endpoint = null;
  private com.arteriatech.bc.DeRegistration.DeRegistration deRegistration = null;
  
  public DeRegistrationProxy() {
    _initDeRegistrationProxy();
  }
  
  public DeRegistrationProxy(String endpoint) {
    _endpoint = endpoint;
    _initDeRegistrationProxy();
  }
  
  private void _initDeRegistrationProxy() {
    try {
      deRegistration = (new com.arteriatech.bc.DeRegistration.DeRegistrationServiceLocator()).getDeRegistrationPort();
      if (deRegistration != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)deRegistration)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)deRegistration)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (deRegistration != null)
      ((javax.xml.rpc.Stub)deRegistration)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.DeRegistration.DeRegistration getDeRegistration() {
    if (deRegistration == null)
      _initDeRegistrationProxy();
    return deRegistration;
  }
  
  public com.arteriatech.bc.DeRegistration.DT_DeRegistration_Response deRegistration(com.arteriatech.bc.DeRegistration.DT_DeRegistration_Request DT_DeRegistration_Request) throws java.rmi.RemoteException{
    if (deRegistration == null)
      _initDeRegistrationProxy();
    return deRegistration.deRegistration(DT_DeRegistration_Request);
  }
  
  
}
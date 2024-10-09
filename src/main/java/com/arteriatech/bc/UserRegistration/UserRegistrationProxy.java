package com.arteriatech.bc.UserRegistration;

public class UserRegistrationProxy implements com.arteriatech.bc.UserRegistration.UserRegistration {
  private String _endpoint = null;
  private com.arteriatech.bc.UserRegistration.UserRegistration userRegistration = null;
  
  public UserRegistrationProxy() {
    _initUserRegistrationProxy();
  }
  
  public UserRegistrationProxy(String endpoint) {
    _endpoint = endpoint;
    _initUserRegistrationProxy();
  }
  
  private void _initUserRegistrationProxy() {
    try {
      userRegistration = (new com.arteriatech.bc.UserRegistration.UserRegistrationServiceLocator()).getUserRegistrationPort();
      if (userRegistration != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)userRegistration)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)userRegistration)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (userRegistration != null)
      ((javax.xml.rpc.Stub)userRegistration)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.UserRegistration.UserRegistration getUserRegistration() {
    if (userRegistration == null)
      _initUserRegistrationProxy();
    return userRegistration;
  }
  
  public com.arteriatech.bc.UserRegistration.UserRegistrationResponse userRegistration(com.arteriatech.bc.UserRegistration.UserRegistrationRequest userRegistrationRequest) throws java.rmi.RemoteException{
    if (userRegistration == null)
      _initUserRegistrationProxy();
    return userRegistration.userRegistration(userRegistrationRequest);
  }
  
  
}
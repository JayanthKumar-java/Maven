package com.arteriatech.bc.CurrentAccountInquiry;

public class CurrentAccountInquiryProxy implements com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiry {
  private String _endpoint = null;
  private com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiry currentAccountInquiry = null;
  
  public CurrentAccountInquiryProxy() {
    _initCurrentAccountInquiryProxy();
  }
  
  public CurrentAccountInquiryProxy(String endpoint) {
    _endpoint = endpoint;
    _initCurrentAccountInquiryProxy();
  }
  
  private void _initCurrentAccountInquiryProxy() {
    try {
      currentAccountInquiry = (new com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiryServiceLocator()).getcurrentAccountInquiryPort();
      if (currentAccountInquiry != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)currentAccountInquiry)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)currentAccountInquiry)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (currentAccountInquiry != null)
      ((javax.xml.rpc.Stub)currentAccountInquiry)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiry getCurrentAccountInquiry() {
    if (currentAccountInquiry == null)
      _initCurrentAccountInquiryProxy();
    return currentAccountInquiry;
  }
  
  public com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiryResponsePortal currentAccountInquiry(com.arteriatech.bc.CurrentAccountInquiry.CurrentAccountInquiryRequest currentAccountInquiryRequest) throws java.rmi.RemoteException{
    if (currentAccountInquiry == null)
      _initCurrentAccountInquiryProxy();
    return currentAccountInquiry.currentAccountInquiry(currentAccountInquiryRequest);
  }
  
  
}
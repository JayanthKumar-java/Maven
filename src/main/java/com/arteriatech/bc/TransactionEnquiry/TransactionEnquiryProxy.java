package com.arteriatech.bc.TransactionEnquiry;

public class TransactionEnquiryProxy implements com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry {
  private String _endpoint = null;
  private com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry transactionEnquiry = null;
  
  public TransactionEnquiryProxy() {
    _initTransactionEnquiryProxy();
  }
  
  public TransactionEnquiryProxy(String endpoint) {
    _endpoint = endpoint;
    _initTransactionEnquiryProxy();
  }
  
  private void _initTransactionEnquiryProxy() {
    try {
      transactionEnquiry = (new com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryServiceLocator()).getTransactionEnquiryPort();
      if (transactionEnquiry != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)transactionEnquiry)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)transactionEnquiry)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (transactionEnquiry != null)
      ((javax.xml.rpc.Stub)transactionEnquiry)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry getTransactionEnquiry() {
    if (transactionEnquiry == null)
      _initTransactionEnquiryProxy();
    return transactionEnquiry;
  }
  
  public com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryResponse transactionEnquiry(com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryRequest transactionEnquiryRequest) throws java.rmi.RemoteException{
    if (transactionEnquiry == null)
      _initTransactionEnquiryProxy();
    return transactionEnquiry.transactionEnquiry(transactionEnquiryRequest);
  }
  
  
}
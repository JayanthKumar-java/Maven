package com.arteriatech.bc.PaymentTransactionPost;

public class PaymentTransactionPostProxy implements com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost {
  private String _endpoint = null;
  private com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost paymentTransactionPost = null;
  
  public PaymentTransactionPostProxy() {
    _initPaymentTransactionPostProxy();
  }
  
  public PaymentTransactionPostProxy(String endpoint) {
    _endpoint = endpoint;
    _initPaymentTransactionPostProxy();
  }
  
  private void _initPaymentTransactionPostProxy() {
    try {
      paymentTransactionPost = (new com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostServiceLocator()).getPaymentTransactionPostPort();
      if (paymentTransactionPost != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)paymentTransactionPost)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)paymentTransactionPost)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (paymentTransactionPost != null)
      ((javax.xml.rpc.Stub)paymentTransactionPost)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost getPaymentTransactionPost() {
    if (paymentTransactionPost == null)
      _initPaymentTransactionPostProxy();
    return paymentTransactionPost;
  }
  
  public com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost_Response paymentTransactionPost(com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost_Request paymentTransactionPost_Request) throws java.rmi.RemoteException{
    if (paymentTransactionPost == null)
      _initPaymentTransactionPostProxy();
    return paymentTransactionPost.paymentTransactionPost(paymentTransactionPost_Request);
  }
  
  
}
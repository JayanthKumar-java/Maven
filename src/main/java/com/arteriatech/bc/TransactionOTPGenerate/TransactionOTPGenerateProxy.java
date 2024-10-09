package com.arteriatech.bc.TransactionOTPGenerate;

public class TransactionOTPGenerateProxy implements com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerate {
  private String _endpoint = null;
  private com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerate transactionOTPGenerate = null;
  
  public TransactionOTPGenerateProxy() {
    _initTransactionOTPGenerateProxy();
  }
  
  public TransactionOTPGenerateProxy(String endpoint) {
    _endpoint = endpoint;
    _initTransactionOTPGenerateProxy();
  }
  
  private void _initTransactionOTPGenerateProxy() {
    try {
      transactionOTPGenerate = (new com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerateServiceLocator()).getTransactionOTPGeneratePort();
      if (transactionOTPGenerate != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)transactionOTPGenerate)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)transactionOTPGenerate)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (transactionOTPGenerate != null)
      ((javax.xml.rpc.Stub)transactionOTPGenerate)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerate getTransactionOTPGenerate() {
    if (transactionOTPGenerate == null)
      _initTransactionOTPGenerateProxy();
    return transactionOTPGenerate;
  }
  
  public com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerate_Response transactionOTPGenerate(com.arteriatech.bc.TransactionOTPGenerate.TransactionOTPGenerate_Request transactionOTPGenerate_Request) throws java.rmi.RemoteException{
    if (transactionOTPGenerate == null)
      _initTransactionOTPGenerateProxy();
    return transactionOTPGenerate.transactionOTPGenerate(transactionOTPGenerate_Request);
  }
  
  
}
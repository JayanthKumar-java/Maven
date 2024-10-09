package com.arteriatech.bc.AccountBalance;

public class AccountBalanceProxy implements com.arteriatech.bc.AccountBalance.AccountBalance {
  private String _endpoint = null;
  private com.arteriatech.bc.AccountBalance.AccountBalance accountBalance = null;
  
  public AccountBalanceProxy() {
    _initAccountBalanceProxy();
  }
  
  public AccountBalanceProxy(String endpoint) {
    _endpoint = endpoint;
    _initAccountBalanceProxy();
  }
  
  private void _initAccountBalanceProxy() {
    try {
      accountBalance = (new com.arteriatech.bc.AccountBalance.AccountBalanceServiceLocator()).getAccountBalancePort();
      if (accountBalance != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)accountBalance)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)accountBalance)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (accountBalance != null)
      ((javax.xml.rpc.Stub)accountBalance)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.AccountBalance.AccountBalance getAccountBalance() {
    if (accountBalance == null)
      _initAccountBalanceProxy();
    return accountBalance;
  }
  
  public com.arteriatech.bc.AccountBalance.AccountBalanceResponse accountBalance(com.arteriatech.bc.AccountBalance.AccountBalanceRequest accountBalanceRequest) throws java.rmi.RemoteException{
    if (accountBalance == null)
      _initAccountBalanceProxy();
    return accountBalance.accountBalance(accountBalanceRequest);
  }
  
  
}
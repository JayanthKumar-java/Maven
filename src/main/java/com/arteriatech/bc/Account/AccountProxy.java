package com.arteriatech.bc.Account;

public class AccountProxy implements com.arteriatech.bc.Account.Account {
  private String _endpoint = null;
  private com.arteriatech.bc.Account.Account account = null;
  
  public AccountProxy() {
    _initAccountProxy();
  }
  
  public AccountProxy(String endpoint) {
    _endpoint = endpoint;
    _initAccountProxy();
  }
  
  private void _initAccountProxy() {
    try {
      account = (new com.arteriatech.bc.Account.AccountServiceLocator()).getAccountPort();
      if (account != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)account)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)account)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (account != null)
      ((javax.xml.rpc.Stub)account)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.arteriatech.bc.Account.Account getAccount() {
    if (account == null)
      _initAccountProxy();
    return account;
  }
  
  public com.arteriatech.bc.Account.LinkedAccount_CurrentAccount_2_Portal account(com.arteriatech.bc.Account.AccountRequest accountRequest) throws java.rmi.RemoteException{
    if (account == null)
      _initAccountProxy();
    return account.account(accountRequest);
  }
  
  
}
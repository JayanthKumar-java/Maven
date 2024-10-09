package com.arteriatech.bc.CurrentAccount;

public class AccountProxy implements com.arteriatech.bc.CurrentAccount.Account {
  private String _endpoint = null;
  private com.arteriatech.bc.CurrentAccount.Account account = null;
  
  public AccountProxy() {
    _initAccountProxy();
  }
  
  public AccountProxy(String endpoint) {
    _endpoint = endpoint;
    _initAccountProxy();
  }
  
  private void _initAccountProxy() {
    try {
      account = (new com.arteriatech.bc.CurrentAccount.AccountServiceLocator()).getAccountPort();
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
  
  public com.arteriatech.bc.CurrentAccount.Account getAccount() {
    if (account == null)
      _initAccountProxy();
    return account;
  }
  
  public com.arteriatech.bc.CurrentAccount.AccountResponse account(com.arteriatech.bc.CurrentAccount.AccountRequest accountRequest) throws java.rmi.RemoteException{
    if (account == null)
      _initAccountProxy();
    return account.account(accountRequest);
  }
  
  
}
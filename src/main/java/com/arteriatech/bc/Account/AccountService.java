/**
 * AccountService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.Account;

public interface AccountService extends javax.xml.rpc.Service {
    public java.lang.String getAccountPortAddress();

    public com.arteriatech.bc.Account.Account getAccountPort() throws javax.xml.rpc.ServiceException;

    public com.arteriatech.bc.Account.Account getAccountPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

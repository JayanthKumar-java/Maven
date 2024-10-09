/**
 * AccountBalanceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.AccountBalance;

public class AccountBalanceServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.AccountBalance.AccountBalanceService {

    public AccountBalanceServiceLocator() {
    }


    public AccountBalanceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AccountBalanceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AccountBalancePort
    private java.lang.String AccountBalancePort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/AccountBalance";

    public java.lang.String getAccountBalancePortAddress() {
        return AccountBalancePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AccountBalancePortWSDDServiceName = "AccountBalancePort";

    public java.lang.String getAccountBalancePortWSDDServiceName() {
        return AccountBalancePortWSDDServiceName;
    }

    public void setAccountBalancePortWSDDServiceName(java.lang.String name) {
        AccountBalancePortWSDDServiceName = name;
    }

    public com.arteriatech.bc.AccountBalance.AccountBalance getAccountBalancePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AccountBalancePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAccountBalancePort(endpoint);
    }

    public com.arteriatech.bc.AccountBalance.AccountBalance getAccountBalancePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.AccountBalance.AccountBalanceBindingStub _stub = new com.arteriatech.bc.AccountBalance.AccountBalanceBindingStub(portAddress, this);
            _stub.setPortName(getAccountBalancePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAccountBalancePortEndpointAddress(java.lang.String address) {
        AccountBalancePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.AccountBalance.AccountBalance.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.AccountBalance.AccountBalanceBindingStub _stub = new com.arteriatech.bc.AccountBalance.AccountBalanceBindingStub(new java.net.URL(AccountBalancePort_address), this);
                _stub.setPortName(getAccountBalancePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AccountBalancePort".equals(inputPortName)) {
            return getAccountBalancePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/AccountBalance", "AccountBalanceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/AccountBalance", "AccountBalancePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AccountBalancePort".equals(portName)) {
            setAccountBalancePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

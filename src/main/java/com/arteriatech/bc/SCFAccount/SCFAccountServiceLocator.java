/**
 * SCFAccountServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFAccount;

public class SCFAccountServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.SCFAccount.SCFAccountService {

    public SCFAccountServiceLocator() {
    }


    public SCFAccountServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SCFAccountServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SCFAccountPort
    private java.lang.String SCFAccountPort_address = "https://l20320-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFAccount";

    public java.lang.String getSCFAccountPortAddress() {
        return SCFAccountPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SCFAccountPortWSDDServiceName = "SCFAccountPort";

    public java.lang.String getSCFAccountPortWSDDServiceName() {
        return SCFAccountPortWSDDServiceName;
    }

    public void setSCFAccountPortWSDDServiceName(java.lang.String name) {
        SCFAccountPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.SCFAccount.SCFAccount getSCFAccountPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SCFAccountPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSCFAccountPort(endpoint);
    }

    public com.arteriatech.bc.SCFAccount.SCFAccount getSCFAccountPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.SCFAccount.SCFAccountBindingStub _stub = new com.arteriatech.bc.SCFAccount.SCFAccountBindingStub(portAddress, this);
            _stub.setPortName(getSCFAccountPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSCFAccountPortEndpointAddress(java.lang.String address) {
        SCFAccountPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.SCFAccount.SCFAccount.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.SCFAccount.SCFAccountBindingStub _stub = new com.arteriatech.bc.SCFAccount.SCFAccountBindingStub(new java.net.URL(SCFAccountPort_address), this);
                _stub.setPortName(getSCFAccountPortWSDDServiceName());
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
        if ("SCFAccountPort".equals(inputPortName)) {
            return getSCFAccountPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", "SCFAccountService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", "SCFAccountPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SCFAccountPort".equals(portName)) {
            setSCFAccountPortEndpointAddress(address);
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

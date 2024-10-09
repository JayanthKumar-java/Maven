/**
 * TransactionEnquiryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.TransactionEnquiry;

public class TransactionEnquiryServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryService {

    public TransactionEnquiryServiceLocator() {
    }


    public TransactionEnquiryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TransactionEnquiryServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TransactionEnquiryPort
    private java.lang.String TransactionEnquiryPort_address = "https://l20320-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/TransactionEnquiry";

    public java.lang.String getTransactionEnquiryPortAddress() {
        return TransactionEnquiryPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TransactionEnquiryPortWSDDServiceName = "TransactionEnquiryPort";

    public java.lang.String getTransactionEnquiryPortWSDDServiceName() {
        return TransactionEnquiryPortWSDDServiceName;
    }

    public void setTransactionEnquiryPortWSDDServiceName(java.lang.String name) {
        TransactionEnquiryPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry getTransactionEnquiryPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TransactionEnquiryPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTransactionEnquiryPort(endpoint);
    }

    public com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry getTransactionEnquiryPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryBindingStub _stub = new com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryBindingStub(portAddress, this);
            _stub.setPortName(getTransactionEnquiryPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTransactionEnquiryPortEndpointAddress(java.lang.String address) {
        TransactionEnquiryPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.TransactionEnquiry.TransactionEnquiry.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryBindingStub _stub = new com.arteriatech.bc.TransactionEnquiry.TransactionEnquiryBindingStub(new java.net.URL(TransactionEnquiryPort_address), this);
                _stub.setPortName(getTransactionEnquiryPortWSDDServiceName());
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
        if ("TransactionEnquiryPort".equals(inputPortName)) {
            return getTransactionEnquiryPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/TransactionEnquiry", "TransactionEnquiryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/TransactionEnquiry", "TransactionEnquiryPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TransactionEnquiryPort".equals(portName)) {
            setTransactionEnquiryPortEndpointAddress(address);
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

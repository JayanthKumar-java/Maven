/**
 * PaymentTransactionPostServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.PaymentTransactionPost;

public class PaymentTransactionPostServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostService {

    public PaymentTransactionPostServiceLocator() {
    }


    public PaymentTransactionPostServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public PaymentTransactionPostServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for PaymentTransactionPostPort
    private java.lang.String PaymentTransactionPostPort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/PaymentTransactionPost";

    public java.lang.String getPaymentTransactionPostPortAddress() {
        return PaymentTransactionPostPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PaymentTransactionPostPortWSDDServiceName = "PaymentTransactionPostPort";

    public java.lang.String getPaymentTransactionPostPortWSDDServiceName() {
        return PaymentTransactionPostPortWSDDServiceName;
    }

    public void setPaymentTransactionPostPortWSDDServiceName(java.lang.String name) {
        PaymentTransactionPostPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost getPaymentTransactionPostPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(PaymentTransactionPostPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPaymentTransactionPostPort(endpoint);
    }

    public com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost getPaymentTransactionPostPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostBindingStub _stub = new com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostBindingStub(portAddress, this);
            _stub.setPortName(getPaymentTransactionPostPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setPaymentTransactionPostPortEndpointAddress(java.lang.String address) {
        PaymentTransactionPostPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPost.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostBindingStub _stub = new com.arteriatech.bc.PaymentTransactionPost.PaymentTransactionPostBindingStub(new java.net.URL(PaymentTransactionPostPort_address), this);
                _stub.setPortName(getPaymentTransactionPostPortWSDDServiceName());
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
        if ("PaymentTransactionPostPort".equals(inputPortName)) {
            return getPaymentTransactionPostPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/PaymentTransactionPost", "PaymentTransactionPostService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/PaymentTransactionPost", "PaymentTransactionPostPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("PaymentTransactionPostPort".equals(portName)) {
            setPaymentTransactionPostPortEndpointAddress(address);
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

/**
 * SCFOfferServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFOffer;

public class SCFOfferServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.SCFOffer.SCFOfferService {

    public SCFOfferServiceLocator() {
    }


    public SCFOfferServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SCFOfferServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SCFOfferPort
    private java.lang.String SCFOfferPort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFOffer";

    public java.lang.String getSCFOfferPortAddress() {
        return SCFOfferPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SCFOfferPortWSDDServiceName = "SCFOfferPort";

    public java.lang.String getSCFOfferPortWSDDServiceName() {
        return SCFOfferPortWSDDServiceName;
    }

    public void setSCFOfferPortWSDDServiceName(java.lang.String name) {
        SCFOfferPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.SCFOffer.SCFOffer getSCFOfferPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SCFOfferPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSCFOfferPort(endpoint);
    }

    public com.arteriatech.bc.SCFOffer.SCFOffer getSCFOfferPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.SCFOffer.SCFOfferBindingStub _stub = new com.arteriatech.bc.SCFOffer.SCFOfferBindingStub(portAddress, this);
            _stub.setPortName(getSCFOfferPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSCFOfferPortEndpointAddress(java.lang.String address) {
        SCFOfferPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.SCFOffer.SCFOffer.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.SCFOffer.SCFOfferBindingStub _stub = new com.arteriatech.bc.SCFOffer.SCFOfferBindingStub(new java.net.URL(SCFOfferPort_address), this);
                _stub.setPortName(getSCFOfferPortWSDDServiceName());
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
        if ("SCFOfferPort".equals(inputPortName)) {
            return getSCFOfferPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFOffer", "SCFOfferService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFOffer", "SCFOfferPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SCFOfferPort".equals(portName)) {
            setSCFOfferPortEndpointAddress(address);
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

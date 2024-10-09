/**
 * SCFApplyServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFApply;

public class SCFApplyServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.SCFApply.SCFApplyService {

    public SCFApplyServiceLocator() {
    }


    public SCFApplyServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SCFApplyServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SCFApplyPort
    private java.lang.String SCFApplyPort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFApply";

    public java.lang.String getSCFApplyPortAddress() {
        return SCFApplyPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SCFApplyPortWSDDServiceName = "SCFApplyPort";

    public java.lang.String getSCFApplyPortWSDDServiceName() {
        return SCFApplyPortWSDDServiceName;
    }

    public void setSCFApplyPortWSDDServiceName(java.lang.String name) {
        SCFApplyPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.SCFApply.SCFApply getSCFApplyPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SCFApplyPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSCFApplyPort(endpoint);
    }

    public com.arteriatech.bc.SCFApply.SCFApply getSCFApplyPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.SCFApply.SCFApplyBindingStub _stub = new com.arteriatech.bc.SCFApply.SCFApplyBindingStub(portAddress, this);
            _stub.setPortName(getSCFApplyPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSCFApplyPortEndpointAddress(java.lang.String address) {
        SCFApplyPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.SCFApply.SCFApply.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.SCFApply.SCFApplyBindingStub _stub = new com.arteriatech.bc.SCFApply.SCFApplyBindingStub(new java.net.URL(SCFApplyPort_address), this);
                _stub.setPortName(getSCFApplyPortWSDDServiceName());
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
        if ("SCFApplyPort".equals(inputPortName)) {
            return getSCFApplyPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFApply", "SCFApplyService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFApply", "SCFApplyPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SCFApplyPort".equals(portName)) {
            setSCFApplyPortEndpointAddress(address);
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

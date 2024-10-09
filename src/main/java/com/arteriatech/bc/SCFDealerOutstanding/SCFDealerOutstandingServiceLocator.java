/**
 * SCFDealerOutstandingServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFDealerOutstanding;

public class SCFDealerOutstandingServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingService {

    public SCFDealerOutstandingServiceLocator() {
    }


    public SCFDealerOutstandingServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SCFDealerOutstandingServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SCFDealerOutstandingPort
    private java.lang.String SCFDealerOutstandingPort_address = "https://l20321-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFDealerOutstanding";

    public java.lang.String getSCFDealerOutstandingPortAddress() {
        return SCFDealerOutstandingPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SCFDealerOutstandingPortWSDDServiceName = "SCFDealerOutstandingPort";

    public java.lang.String getSCFDealerOutstandingPortWSDDServiceName() {
        return SCFDealerOutstandingPortWSDDServiceName;
    }

    public void setSCFDealerOutstandingPortWSDDServiceName(java.lang.String name) {
        SCFDealerOutstandingPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding getSCFDealerOutstandingPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SCFDealerOutstandingPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSCFDealerOutstandingPort(endpoint);
    }

    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding getSCFDealerOutstandingPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingBindingStub _stub = new com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingBindingStub(portAddress, this);
            _stub.setPortName(getSCFDealerOutstandingPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSCFDealerOutstandingPortEndpointAddress(java.lang.String address) {
        SCFDealerOutstandingPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingBindingStub _stub = new com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstandingBindingStub(new java.net.URL(SCFDealerOutstandingPort_address), this);
                _stub.setPortName(getSCFDealerOutstandingPortWSDDServiceName());
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
        if ("SCFDealerOutstandingPort".equals(inputPortName)) {
            return getSCFDealerOutstandingPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", "SCFDealerOutstandingService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", "SCFDealerOutstandingPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SCFDealerOutstandingPort".equals(portName)) {
            setSCFDealerOutstandingPortEndpointAddress(address);
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

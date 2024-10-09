/**
 * UserRegistrationServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.UserRegistration;

public class UserRegistrationServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.UserRegistration.UserRegistrationService {

    public UserRegistrationServiceLocator() {
    }


    public UserRegistrationServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UserRegistrationServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for UserRegistrationPort
    private java.lang.String UserRegistrationPort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/ARTEC/BC/UserRegistration";

    public java.lang.String getUserRegistrationPortAddress() {
        return UserRegistrationPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UserRegistrationPortWSDDServiceName = "UserRegistrationPort";

    public java.lang.String getUserRegistrationPortWSDDServiceName() {
        return UserRegistrationPortWSDDServiceName;
    }

    public void setUserRegistrationPortWSDDServiceName(java.lang.String name) {
        UserRegistrationPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.UserRegistration.UserRegistration getUserRegistrationPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UserRegistrationPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUserRegistrationPort(endpoint);
    }

    public com.arteriatech.bc.UserRegistration.UserRegistration getUserRegistrationPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.UserRegistration.UserRegistrationBindingStub _stub = new com.arteriatech.bc.UserRegistration.UserRegistrationBindingStub(portAddress, this);
            _stub.setPortName(getUserRegistrationPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUserRegistrationPortEndpointAddress(java.lang.String address) {
        UserRegistrationPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.UserRegistration.UserRegistration.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.UserRegistration.UserRegistrationBindingStub _stub = new com.arteriatech.bc.UserRegistration.UserRegistrationBindingStub(new java.net.URL(UserRegistrationPort_address), this);
                _stub.setPortName(getUserRegistrationPortWSDDServiceName());
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
        if ("UserRegistrationPort".equals(inputPortName)) {
            return getUserRegistrationPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/UserRegistration", "UserRegistrationService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/UserRegistration", "UserRegistrationPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("UserRegistrationPort".equals(portName)) {
            setUserRegistrationPortEndpointAddress(address);
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

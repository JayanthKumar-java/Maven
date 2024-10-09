/**
 * ESignContractCALPartnershipServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnershipServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipService {

    public ESignContractCALPartnershipServiceLocator() {
    }


    public ESignContractCALPartnershipServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ESignContractCALPartnershipServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for eSignContractCALPartnershipPort
    private java.lang.String eSignContractCALPartnershipPort_address = "https://l20320-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/eSignContractCALPartnership";

    public java.lang.String geteSignContractCALPartnershipPortAddress() {
        return eSignContractCALPartnershipPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String eSignContractCALPartnershipPortWSDDServiceName = "eSignContractCALPartnershipPort";

    public java.lang.String geteSignContractCALPartnershipPortWSDDServiceName() {
        return eSignContractCALPartnershipPortWSDDServiceName;
    }

    public void seteSignContractCALPartnershipPortWSDDServiceName(java.lang.String name) {
        eSignContractCALPartnershipPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership geteSignContractCALPartnershipPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(eSignContractCALPartnershipPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return geteSignContractCALPartnershipPort(endpoint);
    }

    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership geteSignContractCALPartnershipPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipBindingStub _stub = new com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipBindingStub(portAddress, this);
            _stub.setPortName(geteSignContractCALPartnershipPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void seteSignContractCALPartnershipPortEndpointAddress(java.lang.String address) {
        eSignContractCALPartnershipPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipBindingStub _stub = new com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnershipBindingStub(new java.net.URL(eSignContractCALPartnershipPort_address), this);
                _stub.setPortName(geteSignContractCALPartnershipPortWSDDServiceName());
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
        if ("eSignContractCALPartnershipPort".equals(inputPortName)) {
            return geteSignContractCALPartnershipPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", "eSignContractCALPartnershipService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", "eSignContractCALPartnershipPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("eSignContractCALPartnershipPort".equals(portName)) {
            seteSignContractCALPartnershipPortEndpointAddress(address);
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

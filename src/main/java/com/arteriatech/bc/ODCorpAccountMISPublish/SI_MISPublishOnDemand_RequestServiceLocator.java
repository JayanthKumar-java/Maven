/**
 * SI_MISPublishOnDemand_RequestServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.ODCorpAccountMISPublish;

public class SI_MISPublishOnDemand_RequestServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestService {

    public SI_MISPublishOnDemand_RequestServiceLocator() {
    }


    public SI_MISPublishOnDemand_RequestServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SI_MISPublishOnDemand_RequestServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SI_MISPublishOnDemand_RequestPort
    private java.lang.String SI_MISPublishOnDemand_RequestPort_address = "https://e1044-iflmap.hcisbt.ap1.hana.ondemand.com:443/cxf/MISPublishOnDemand_Request";

    public java.lang.String getSI_MISPublishOnDemand_RequestPortAddress() {
        return SI_MISPublishOnDemand_RequestPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SI_MISPublishOnDemand_RequestPortWSDDServiceName = "SI_MISPublishOnDemand_RequestPort";

    public java.lang.String getSI_MISPublishOnDemand_RequestPortWSDDServiceName() {
        return SI_MISPublishOnDemand_RequestPortWSDDServiceName;
    }

    public void setSI_MISPublishOnDemand_RequestPortWSDDServiceName(java.lang.String name) {
        SI_MISPublishOnDemand_RequestPortWSDDServiceName = name;
    }

    public com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request getSI_MISPublishOnDemand_RequestPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SI_MISPublishOnDemand_RequestPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSI_MISPublishOnDemand_RequestPort(endpoint);
    }

    public com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request getSI_MISPublishOnDemand_RequestPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestBindingStub _stub = new com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestBindingStub(portAddress, this);
            _stub.setPortName(getSI_MISPublishOnDemand_RequestPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSI_MISPublishOnDemand_RequestPortEndpointAddress(java.lang.String address) {
        SI_MISPublishOnDemand_RequestPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_Request.class.isAssignableFrom(serviceEndpointInterface)) {
                com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestBindingStub _stub = new com.arteriatech.bc.ODCorpAccountMISPublish.SI_MISPublishOnDemand_RequestBindingStub(new java.net.URL(SI_MISPublishOnDemand_RequestPort_address), this);
                _stub.setPortName(getSI_MISPublishOnDemand_RequestPortWSDDServiceName());
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
        if ("SI_MISPublishOnDemand_RequestPort".equals(inputPortName)) {
            return getSI_MISPublishOnDemand_RequestPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://bc.arteriatech.com/ODCorpAccountMISPublish", "SI_MISPublishOnDemand_RequestService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/ODCorpAccountMISPublish", "SI_MISPublishOnDemand_RequestPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SI_MISPublishOnDemand_RequestPort".equals(portName)) {
            setSI_MISPublishOnDemand_RequestPortEndpointAddress(address);
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

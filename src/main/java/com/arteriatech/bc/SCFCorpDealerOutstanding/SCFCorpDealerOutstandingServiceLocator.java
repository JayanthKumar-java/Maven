/**
 * SCFCorpDealerOutstandingServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

 package com.arteriatech.bc.SCFCorpDealerOutstanding;

 public class SCFCorpDealerOutstandingServiceLocator extends org.apache.axis.client.Service implements com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingService {
 
     public SCFCorpDealerOutstandingServiceLocator() {
     }
 
     public SCFCorpDealerOutstandingServiceLocator(org.apache.axis.EngineConfiguration config) {
         super(config);
     }
 
     public SCFCorpDealerOutstandingServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
         super(wsdlLoc, sName);
     }
 
     // Use to get a proxy class for SCFCorpDealerOutstandingPort
     private java.lang.String SCFCorpDealerOutstandingPort_address = "https://l20320-iflmap.hcisbp.eu1.hana.ondemand.com:443/cxf/ARTEC/BC/SCFCorpDealerOutstanding";
 
     public java.lang.String getSCFCorpDealerOutstandingPortAddress() {
         return SCFCorpDealerOutstandingPort_address;
     }
 
     // The WSDD service name defaults to the port name.
     private java.lang.String SCFCorpDealerOutstandingPortWSDDServiceName = "SCFCorpDealerOutstandingPort";
 
     public java.lang.String getSCFCorpDealerOutstandingPortWSDDServiceName() {
         return SCFCorpDealerOutstandingPortWSDDServiceName;
     }
 
     public void setSCFCorpDealerOutstandingPortWSDDServiceName(java.lang.String name) {
         SCFCorpDealerOutstandingPortWSDDServiceName = name;
     }
 
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding getSCFCorpDealerOutstandingPort() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
         try {
             endpoint = new java.net.URL(SCFCorpDealerOutstandingPort_address);
         }
         catch (java.net.MalformedURLException e) {
             throw new javax.xml.rpc.ServiceException(e);
         }
         return getSCFCorpDealerOutstandingPort(endpoint);
     }
 
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding getSCFCorpDealerOutstandingPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
         try {
             com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingBindingStub _stub = new com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingBindingStub(portAddress, this);
             _stub.setPortName(getSCFCorpDealerOutstandingPortWSDDServiceName());
             _stub.setUsername(SCFCorpDealerOutstandingPortWSDDServiceName);
             _stub.setPassword(SCFCorpDealerOutstandingPortWSDDServiceName);
             return _stub;
         }
         catch (org.apache.axis.AxisFault e) {
             return null;
         }
     }
 
     public void setSCFCorpDealerOutstandingPortEndpointAddress(java.lang.String address) {
         SCFCorpDealerOutstandingPort_address = address;
     }
 
     /**
      * For the given interface, get the stub implementation.
      * If this service has no port for the given interface,
      * then ServiceException is thrown.
      */
     public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
         try {
             if (com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding.class.isAssignableFrom(serviceEndpointInterface)) {
                 com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingBindingStub _stub = new com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingBindingStub(new java.net.URL(SCFCorpDealerOutstandingPort_address), this);
                 _stub.setPortName(getSCFCorpDealerOutstandingPortWSDDServiceName());
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
         if ("SCFCorpDealerOutstandingPort".equals(inputPortName)) {
             return getSCFCorpDealerOutstandingPort();
         }
         else  {
             java.rmi.Remote _stub = getPort(serviceEndpointInterface);
             ((org.apache.axis.client.Stub) _stub).setPortName(portName);
             return _stub;
         }
     }
 
     public javax.xml.namespace.QName getServiceName() {
         return new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstandingService");
     }
 
     private java.util.HashSet ports = null;
 
     public java.util.Iterator getPorts() {
         if (ports == null) {
             ports = new java.util.HashSet();
             ports.add(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstandingPort"));
         }
         return ports.iterator();
     }
 
     /**
     * Set the endpoint address for the specified port name.
     */
     public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
         
 if ("SCFCorpDealerOutstandingPort".equals(portName)) {
             setSCFCorpDealerOutstandingPortEndpointAddress(address);
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
 
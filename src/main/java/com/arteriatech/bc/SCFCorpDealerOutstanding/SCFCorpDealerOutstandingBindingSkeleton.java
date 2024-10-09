/**
 * SCFCorpDealerOutstandingBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

 package com.arteriatech.bc.SCFCorpDealerOutstanding;

 public class SCFCorpDealerOutstandingBindingSkeleton implements com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding, org.apache.axis.wsdl.Skeleton {
     private com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding impl;
     private static java.util.Map _myOperations = new java.util.Hashtable();
     private static java.util.Collection _myOperationsList = new java.util.ArrayList();
 
     /**
     * Returns List of OperationDesc objects with this name
     */
     public static java.util.List getOperationDescByName(java.lang.String methodName) {
         return (java.util.List)_myOperations.get(methodName);
     }
 
     /**
     * Returns Collection of OperationDescs
     */
     public static java.util.Collection getOperationDescs() {
         return _myOperationsList;
     }
 
     static {
         org.apache.axis.description.OperationDesc _oper;
         org.apache.axis.description.FaultDesc _fault;
         org.apache.axis.description.ParameterDesc [] _params;
         _params = new org.apache.axis.description.ParameterDesc [] {
             new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_Request"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_Request"), com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_Request.class, false, false), 
         };
         _oper = new org.apache.axis.description.OperationDesc("SCFCorpDealerOutstanding", _params, new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_PortalResponse"));
         _oper.setReturnType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_PortalResponse"));
         _oper.setElementQName(new javax.xml.namespace.QName("", "SCFCorpDealerOutstanding"));
         _oper.setSoapAction("http://sap.com/xi/WebService/soap1.1");
         _myOperationsList.add(_oper);
         if (_myOperations.get("SCFCorpDealerOutstanding") == null) {
             _myOperations.put("SCFCorpDealerOutstanding", new java.util.ArrayList());
         }
         ((java.util.List)_myOperations.get("SCFCorpDealerOutstanding")).add(_oper);
     }
 
     public SCFCorpDealerOutstandingBindingSkeleton() {
         this.impl = new com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstandingBindingImpl();
     }
 
     public SCFCorpDealerOutstandingBindingSkeleton(com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding impl) {
         this.impl = impl;
     }
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponse SCFCorpDealerOutstanding(com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_Request SCFCorpDealerOutstanding_Request) throws java.rmi.RemoteException
     {
         com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponse ret = impl.SCFCorpDealerOutstanding(SCFCorpDealerOutstanding_Request);
         return ret;
     }
 
 }
 
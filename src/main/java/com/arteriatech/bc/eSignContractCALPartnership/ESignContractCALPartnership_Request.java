/**
 * ESignContractCALPartnership_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnership_Request  implements java.io.Serializable {
    private com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestTemplateVariables templateVariables;

    private java.lang.String testRun;

    private com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestRequest request;

    private java.lang.String CPType;

    public ESignContractCALPartnership_Request() {
    }

    public ESignContractCALPartnership_Request(
           com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestTemplateVariables templateVariables,
           java.lang.String testRun,
           com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestRequest request,
           java.lang.String CPType) {
           this.templateVariables = templateVariables;
           this.testRun = testRun;
           this.request = request;
           this.CPType = CPType;
    }


    /**
     * Gets the templateVariables value for this ESignContractCALPartnership_Request.
     * 
     * @return templateVariables
     */
    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestTemplateVariables getTemplateVariables() {
        return templateVariables;
    }


    /**
     * Sets the templateVariables value for this ESignContractCALPartnership_Request.
     * 
     * @param templateVariables
     */
    public void setTemplateVariables(com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestTemplateVariables templateVariables) {
        this.templateVariables = templateVariables;
    }


    /**
     * Gets the testRun value for this ESignContractCALPartnership_Request.
     * 
     * @return testRun
     */
    public java.lang.String getTestRun() {
        return testRun;
    }


    /**
     * Sets the testRun value for this ESignContractCALPartnership_Request.
     * 
     * @param testRun
     */
    public void setTestRun(java.lang.String testRun) {
        this.testRun = testRun;
    }


    /**
     * Gets the request value for this ESignContractCALPartnership_Request.
     * 
     * @return request
     */
    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestRequest getRequest() {
        return request;
    }


    /**
     * Sets the request value for this ESignContractCALPartnership_Request.
     * 
     * @param request
     */
    public void setRequest(com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_RequestRequest request) {
        this.request = request;
    }


    /**
     * Gets the CPType value for this ESignContractCALPartnership_Request.
     * 
     * @return CPType
     */
    public java.lang.String getCPType() {
        return CPType;
    }


    /**
     * Sets the CPType value for this ESignContractCALPartnership_Request.
     * 
     * @param CPType
     */
    public void setCPType(java.lang.String CPType) {
        this.CPType = CPType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractCALPartnership_Request)) return false;
        ESignContractCALPartnership_Request other = (ESignContractCALPartnership_Request) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.templateVariables==null && other.getTemplateVariables()==null) || 
             (this.templateVariables!=null &&
              this.templateVariables.equals(other.getTemplateVariables()))) &&
            ((this.testRun==null && other.getTestRun()==null) || 
             (this.testRun!=null &&
              this.testRun.equals(other.getTestRun()))) &&
            ((this.request==null && other.getRequest()==null) || 
             (this.request!=null &&
              this.request.equals(other.getRequest()))) &&
            ((this.CPType==null && other.getCPType()==null) || 
             (this.CPType!=null &&
              this.CPType.equals(other.getCPType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTemplateVariables() != null) {
            _hashCode += getTemplateVariables().hashCode();
        }
        if (getTestRun() != null) {
            _hashCode += getTestRun().hashCode();
        }
        if (getRequest() != null) {
            _hashCode += getRequest().hashCode();
        }
        if (getCPType() != null) {
            _hashCode += getCPType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractCALPartnership_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", "eSignContractCALPartnership_Request"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templateVariables");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TemplateVariables"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">eSignContractCALPartnership_Request>TemplateVariables"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("testRun");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TestRun"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("request");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Request"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">eSignContractCALPartnership_Request>Request"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CPType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

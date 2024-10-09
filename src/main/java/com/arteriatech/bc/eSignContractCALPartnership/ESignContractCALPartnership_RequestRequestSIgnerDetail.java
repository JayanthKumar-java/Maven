/**
 * ESignContractCALPartnership_RequestRequestSIgnerDetail.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnership_RequestRequestSIgnerDetail  implements java.io.Serializable {
    private java.lang.String signerName;

    private java.lang.String signerEmailId;

    private java.lang.String signOrder;

    private java.lang.String aadharNumber;

    public ESignContractCALPartnership_RequestRequestSIgnerDetail() {
    }

    public ESignContractCALPartnership_RequestRequestSIgnerDetail(
           java.lang.String signerName,
           java.lang.String signerEmailId,
           java.lang.String signOrder,
           java.lang.String aadharNumber) {
           this.signerName = signerName;
           this.signerEmailId = signerEmailId;
           this.signOrder = signOrder;
           this.aadharNumber = aadharNumber;
    }


    /**
     * Gets the signerName value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @return signerName
     */
    public java.lang.String getSignerName() {
        return signerName;
    }


    /**
     * Sets the signerName value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @param signerName
     */
    public void setSignerName(java.lang.String signerName) {
        this.signerName = signerName;
    }


    /**
     * Gets the signerEmailId value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @return signerEmailId
     */
    public java.lang.String getSignerEmailId() {
        return signerEmailId;
    }


    /**
     * Sets the signerEmailId value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @param signerEmailId
     */
    public void setSignerEmailId(java.lang.String signerEmailId) {
        this.signerEmailId = signerEmailId;
    }


    /**
     * Gets the signOrder value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @return signOrder
     */
    public java.lang.String getSignOrder() {
        return signOrder;
    }


    /**
     * Sets the signOrder value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @param signOrder
     */
    public void setSignOrder(java.lang.String signOrder) {
        this.signOrder = signOrder;
    }


    /**
     * Gets the aadharNumber value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @return aadharNumber
     */
    public java.lang.String getAadharNumber() {
        return aadharNumber;
    }


    /**
     * Sets the aadharNumber value for this ESignContractCALPartnership_RequestRequestSIgnerDetail.
     * 
     * @param aadharNumber
     */
    public void setAadharNumber(java.lang.String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractCALPartnership_RequestRequestSIgnerDetail)) return false;
        ESignContractCALPartnership_RequestRequestSIgnerDetail other = (ESignContractCALPartnership_RequestRequestSIgnerDetail) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.signerName==null && other.getSignerName()==null) || 
             (this.signerName!=null &&
              this.signerName.equals(other.getSignerName()))) &&
            ((this.signerEmailId==null && other.getSignerEmailId()==null) || 
             (this.signerEmailId!=null &&
              this.signerEmailId.equals(other.getSignerEmailId()))) &&
            ((this.signOrder==null && other.getSignOrder()==null) || 
             (this.signOrder!=null &&
              this.signOrder.equals(other.getSignOrder()))) &&
            ((this.aadharNumber==null && other.getAadharNumber()==null) || 
             (this.aadharNumber!=null &&
              this.aadharNumber.equals(other.getAadharNumber())));
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
        if (getSignerName() != null) {
            _hashCode += getSignerName().hashCode();
        }
        if (getSignerEmailId() != null) {
            _hashCode += getSignerEmailId().hashCode();
        }
        if (getSignOrder() != null) {
            _hashCode += getSignOrder().hashCode();
        }
        if (getAadharNumber() != null) {
            _hashCode += getAadharNumber().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractCALPartnership_RequestRequestSIgnerDetail.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">>eSignContractCALPartnership_Request>Request>SIgnerDetail"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerEmailId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerEmailId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signOrder");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignOrder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aadharNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AadharNumber"));
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

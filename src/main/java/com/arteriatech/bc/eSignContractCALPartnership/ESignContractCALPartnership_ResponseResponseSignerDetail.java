/**
 * ESignContractCALPartnership_ResponseResponseSignerDetail.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnership_ResponseResponseSignerDetail  implements java.io.Serializable {
    private java.lang.String signerName;

    private java.lang.String signOrder;

    public ESignContractCALPartnership_ResponseResponseSignerDetail() {
    }

    public ESignContractCALPartnership_ResponseResponseSignerDetail(
           java.lang.String signerName,
           java.lang.String signOrder) {
           this.signerName = signerName;
           this.signOrder = signOrder;
    }


    /**
     * Gets the signerName value for this ESignContractCALPartnership_ResponseResponseSignerDetail.
     * 
     * @return signerName
     */
    public java.lang.String getSignerName() {
        return signerName;
    }


    /**
     * Sets the signerName value for this ESignContractCALPartnership_ResponseResponseSignerDetail.
     * 
     * @param signerName
     */
    public void setSignerName(java.lang.String signerName) {
        this.signerName = signerName;
    }


    /**
     * Gets the signOrder value for this ESignContractCALPartnership_ResponseResponseSignerDetail.
     * 
     * @return signOrder
     */
    public java.lang.String getSignOrder() {
        return signOrder;
    }


    /**
     * Sets the signOrder value for this ESignContractCALPartnership_ResponseResponseSignerDetail.
     * 
     * @param signOrder
     */
    public void setSignOrder(java.lang.String signOrder) {
        this.signOrder = signOrder;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractCALPartnership_ResponseResponseSignerDetail)) return false;
        ESignContractCALPartnership_ResponseResponseSignerDetail other = (ESignContractCALPartnership_ResponseResponseSignerDetail) obj;
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
            ((this.signOrder==null && other.getSignOrder()==null) || 
             (this.signOrder!=null &&
              this.signOrder.equals(other.getSignOrder())));
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
        if (getSignOrder() != null) {
            _hashCode += getSignOrder().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractCALPartnership_ResponseResponseSignerDetail.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">>eSignContractCALPartnership_Response>Response>SignerDetail"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerName"));
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

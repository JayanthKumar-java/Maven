/**
 * SCFOffer_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFOffer;

public class SCFOffer_Request  implements java.io.Serializable {
    private java.lang.String corpId;

    private java.lang.String dealerId;

    public SCFOffer_Request() {
    }

    public SCFOffer_Request(
           java.lang.String corpId,
           java.lang.String dealerId) {
           this.corpId = corpId;
           this.dealerId = dealerId;
    }


    /**
     * Gets the corpId value for this SCFOffer_Request.
     * 
     * @return corpId
     */
    public java.lang.String getCorpId() {
        return corpId;
    }


    /**
     * Sets the corpId value for this SCFOffer_Request.
     * 
     * @param corpId
     */
    public void setCorpId(java.lang.String corpId) {
        this.corpId = corpId;
    }


    /**
     * Gets the dealerId value for this SCFOffer_Request.
     * 
     * @return dealerId
     */
    public java.lang.String getDealerId() {
        return dealerId;
    }


    /**
     * Sets the dealerId value for this SCFOffer_Request.
     * 
     * @param dealerId
     */
    public void setDealerId(java.lang.String dealerId) {
        this.dealerId = dealerId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFOffer_Request)) return false;
        SCFOffer_Request other = (SCFOffer_Request) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.corpId==null && other.getCorpId()==null) || 
             (this.corpId!=null &&
              this.corpId.equals(other.getCorpId()))) &&
            ((this.dealerId==null && other.getDealerId()==null) || 
             (this.dealerId!=null &&
              this.dealerId.equals(other.getDealerId())));
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
        if (getCorpId() != null) {
            _hashCode += getCorpId().hashCode();
        }
        if (getDealerId() != null) {
            _hashCode += getDealerId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFOffer_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFOffer", "SCFOffer_Request"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corpId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorpId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerId"));
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

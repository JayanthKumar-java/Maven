/**
 * SCFAccount_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFAccount;

public class SCFAccount_Request  implements java.io.Serializable {
    private java.lang.String dealerAccountNo;

    public SCFAccount_Request() {
    }

    public SCFAccount_Request(
           java.lang.String dealerAccountNo) {
           this.dealerAccountNo = dealerAccountNo;
    }


    /**
     * Gets the dealerAccountNo value for this SCFAccount_Request.
     * 
     * @return dealerAccountNo
     */
    public java.lang.String getDealerAccountNo() {
        return dealerAccountNo;
    }


    /**
     * Sets the dealerAccountNo value for this SCFAccount_Request.
     * 
     * @param dealerAccountNo
     */
    public void setDealerAccountNo(java.lang.String dealerAccountNo) {
        this.dealerAccountNo = dealerAccountNo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFAccount_Request)) return false;
        SCFAccount_Request other = (SCFAccount_Request) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dealerAccountNo==null && other.getDealerAccountNo()==null) || 
             (this.dealerAccountNo!=null &&
              this.dealerAccountNo.equals(other.getDealerAccountNo())));
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
        if (getDealerAccountNo() != null) {
            _hashCode += getDealerAccountNo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFAccount_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", "SCFAccount_Request"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAccountNo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAccountNo"));
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

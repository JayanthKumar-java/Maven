/**
 * CurrentAccountInquiryRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.CurrentAccountInquiry;

public class CurrentAccountInquiryRequest  implements java.io.Serializable {
    private java.lang.String account_No;

    public CurrentAccountInquiryRequest() {
    }

    public CurrentAccountInquiryRequest(
           java.lang.String account_No) {
           this.account_No = account_No;
    }


    /**
     * Gets the account_No value for this CurrentAccountInquiryRequest.
     * 
     * @return account_No
     */
    public java.lang.String getAccount_No() {
        return account_No;
    }


    /**
     * Sets the account_No value for this CurrentAccountInquiryRequest.
     * 
     * @param account_No
     */
    public void setAccount_No(java.lang.String account_No) {
        this.account_No = account_No;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentAccountInquiryRequest)) return false;
        CurrentAccountInquiryRequest other = (CurrentAccountInquiryRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.account_No==null && other.getAccount_No()==null) || 
             (this.account_No!=null &&
              this.account_No.equals(other.getAccount_No())));
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
        if (getAccount_No() != null) {
            _hashCode += getAccount_No().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CurrentAccountInquiryRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/CurrentAccountInquiry", "CurrentAccountInquiryRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("account_No");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Account_No"));
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

/**
 * ESignContractResponseResponseSignerDetailResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContract;

public class ESignContractResponseResponseSignerDetailResult  implements java.io.Serializable {
    private java.lang.String eSignedFile;

    public ESignContractResponseResponseSignerDetailResult() {
    }

    public ESignContractResponseResponseSignerDetailResult(
           java.lang.String eSignedFile) {
           this.eSignedFile = eSignedFile;
    }


    /**
     * Gets the eSignedFile value for this ESignContractResponseResponseSignerDetailResult.
     * 
     * @return eSignedFile
     */
    public java.lang.String getESignedFile() {
        return eSignedFile;
    }


    /**
     * Sets the eSignedFile value for this ESignContractResponseResponseSignerDetailResult.
     * 
     * @param eSignedFile
     */
    public void setESignedFile(java.lang.String eSignedFile) {
        this.eSignedFile = eSignedFile;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractResponseResponseSignerDetailResult)) return false;
        ESignContractResponseResponseSignerDetailResult other = (ESignContractResponseResponseSignerDetailResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.eSignedFile==null && other.getESignedFile()==null) || 
             (this.eSignedFile!=null &&
              this.eSignedFile.equals(other.getESignedFile())));
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
        if (getESignedFile() != null) {
            _hashCode += getESignedFile().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractResponseResponseSignerDetailResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>>eSignContractResponse>Response>SignerDetail>result"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ESignedFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eSignedFile"));
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

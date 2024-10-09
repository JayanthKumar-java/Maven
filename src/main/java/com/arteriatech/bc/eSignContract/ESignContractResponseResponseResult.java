/**
 * ESignContractResponseResponseResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContract;

public class ESignContractResponseResponseResult  implements java.io.Serializable {
    private java.lang.String esignedOutput;

    public ESignContractResponseResponseResult() {
    }

    public ESignContractResponseResponseResult(
           java.lang.String esignedOutput) {
           this.esignedOutput = esignedOutput;
    }


    /**
     * Gets the esignedOutput value for this ESignContractResponseResponseResult.
     * 
     * @return esignedOutput
     */
    public java.lang.String getEsignedOutput() {
        return esignedOutput;
    }


    /**
     * Sets the esignedOutput value for this ESignContractResponseResponseResult.
     * 
     * @param esignedOutput
     */
    public void setEsignedOutput(java.lang.String esignedOutput) {
        this.esignedOutput = esignedOutput;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractResponseResponseResult)) return false;
        ESignContractResponseResponseResult other = (ESignContractResponseResponseResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.esignedOutput==null && other.getEsignedOutput()==null) || 
             (this.esignedOutput!=null &&
              this.esignedOutput.equals(other.getEsignedOutput())));
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
        if (getEsignedOutput() != null) {
            _hashCode += getEsignedOutput().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractResponseResponseResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>eSignContractResponse>Response>Result"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("esignedOutput");
        elemField.setXmlName(new javax.xml.namespace.QName("", "esignedOutput"));
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

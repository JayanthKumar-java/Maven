/**
 * SCFAccount_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFAccount;

public class SCFAccount_Response  implements java.io.Serializable {
    private com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatement statement;

    private com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatus status;

    public SCFAccount_Response() {
    }

    public SCFAccount_Response(
           com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatement statement,
           com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatus status) {
           this.statement = statement;
           this.status = status;
    }


    /**
     * Gets the statement value for this SCFAccount_Response.
     * 
     * @return statement
     */
    public com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatement getStatement() {
        return statement;
    }


    /**
     * Sets the statement value for this SCFAccount_Response.
     * 
     * @param statement
     */
    public void setStatement(com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatement statement) {
        this.statement = statement;
    }


    /**
     * Gets the status value for this SCFAccount_Response.
     * 
     * @return status
     */
    public com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this SCFAccount_Response.
     * 
     * @param status
     */
    public void setStatus(com.arteriatech.bc.SCFAccount.SCFAccount_ResponseStatus status) {
        this.status = status;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFAccount_Response)) return false;
        SCFAccount_Response other = (SCFAccount_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.statement==null && other.getStatement()==null) || 
             (this.statement!=null &&
              this.statement.equals(other.getStatement()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus())));
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
        if (getStatement() != null) {
            _hashCode += getStatement().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFAccount_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", "SCFAccount_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statement");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Statement"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", ">SCFAccount_Response>Statement"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", ">SCFAccount_Response>Status"));
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

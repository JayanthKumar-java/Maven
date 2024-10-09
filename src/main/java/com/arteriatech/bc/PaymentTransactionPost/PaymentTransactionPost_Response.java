/**
 * PaymentTransactionPost_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.PaymentTransactionPost;

public class PaymentTransactionPost_Response  implements java.io.Serializable {
    private java.lang.String response;

    private java.lang.String status;

    private java.lang.String message;

    private java.lang.String PGTransactionID;

    private java.lang.String PGBankRefID;

    private java.lang.String PGTxnErrorCode;

    public PaymentTransactionPost_Response() {
    }

    public PaymentTransactionPost_Response(
           java.lang.String response,
           java.lang.String status,
           java.lang.String message,
           java.lang.String PGTransactionID,
           java.lang.String PGBankRefID,
           java.lang.String PGTxnErrorCode) {
           this.response = response;
           this.status = status;
           this.message = message;
           this.PGTransactionID = PGTransactionID;
           this.PGBankRefID = PGBankRefID;
           this.PGTxnErrorCode = PGTxnErrorCode;
    }


    /**
     * Gets the response value for this PaymentTransactionPost_Response.
     * 
     * @return response
     */
    public java.lang.String getResponse() {
        return response;
    }


    /**
     * Sets the response value for this PaymentTransactionPost_Response.
     * 
     * @param response
     */
    public void setResponse(java.lang.String response) {
        this.response = response;
    }


    /**
     * Gets the status value for this PaymentTransactionPost_Response.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this PaymentTransactionPost_Response.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the message value for this PaymentTransactionPost_Response.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this PaymentTransactionPost_Response.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the PGTransactionID value for this PaymentTransactionPost_Response.
     * 
     * @return PGTransactionID
     */
    public java.lang.String getPGTransactionID() {
        return PGTransactionID;
    }


    /**
     * Sets the PGTransactionID value for this PaymentTransactionPost_Response.
     * 
     * @param PGTransactionID
     */
    public void setPGTransactionID(java.lang.String PGTransactionID) {
        this.PGTransactionID = PGTransactionID;
    }


    /**
     * Gets the PGBankRefID value for this PaymentTransactionPost_Response.
     * 
     * @return PGBankRefID
     */
    public java.lang.String getPGBankRefID() {
        return PGBankRefID;
    }


    /**
     * Sets the PGBankRefID value for this PaymentTransactionPost_Response.
     * 
     * @param PGBankRefID
     */
    public void setPGBankRefID(java.lang.String PGBankRefID) {
        this.PGBankRefID = PGBankRefID;
    }


    /**
     * Gets the PGTxnErrorCode value for this PaymentTransactionPost_Response.
     * 
     * @return PGTxnErrorCode
     */
    public java.lang.String getPGTxnErrorCode() {
        return PGTxnErrorCode;
    }


    /**
     * Sets the PGTxnErrorCode value for this PaymentTransactionPost_Response.
     * 
     * @param PGTxnErrorCode
     */
    public void setPGTxnErrorCode(java.lang.String PGTxnErrorCode) {
        this.PGTxnErrorCode = PGTxnErrorCode;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PaymentTransactionPost_Response)) return false;
        PaymentTransactionPost_Response other = (PaymentTransactionPost_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.response==null && other.getResponse()==null) || 
             (this.response!=null &&
              this.response.equals(other.getResponse()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.PGTransactionID==null && other.getPGTransactionID()==null) || 
             (this.PGTransactionID!=null &&
              this.PGTransactionID.equals(other.getPGTransactionID()))) &&
            ((this.PGBankRefID==null && other.getPGBankRefID()==null) || 
             (this.PGBankRefID!=null &&
              this.PGBankRefID.equals(other.getPGBankRefID()))) &&
            ((this.PGTxnErrorCode==null && other.getPGTxnErrorCode()==null) || 
             (this.PGTxnErrorCode!=null &&
              this.PGTxnErrorCode.equals(other.getPGTxnErrorCode())));
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
        if (getResponse() != null) {
            _hashCode += getResponse().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getPGTransactionID() != null) {
            _hashCode += getPGTransactionID().hashCode();
        }
        if (getPGBankRefID() != null) {
            _hashCode += getPGBankRefID().hashCode();
        }
        if (getPGTxnErrorCode() != null) {
            _hashCode += getPGTxnErrorCode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PaymentTransactionPost_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/PaymentTransactionPost", "PaymentTransactionPost_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("response");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Response"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PGTransactionID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PGTransactionID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PGBankRefID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PGBankRefID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PGTxnErrorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PGTxnErrorCode"));
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

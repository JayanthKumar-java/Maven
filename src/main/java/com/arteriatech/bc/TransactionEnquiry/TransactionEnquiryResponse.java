/**
 * TransactionEnquiryResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.TransactionEnquiry;

public class TransactionEnquiryResponse  implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private java.lang.String status;

    private java.lang.String message;

    private java.lang.String PGTxnErrorCode;

    private java.lang.String UTRNumber;

    public TransactionEnquiryResponse() {
    }

    public TransactionEnquiryResponse(
            java.lang.String status,
            java.lang.String message,
            java.lang.String PGTxnErrorCode,
            java.lang.String UTRNumber) {
            this.status = status;
            this.message = message;
            this.PGTxnErrorCode = PGTxnErrorCode;
            this.UTRNumber = UTRNumber;
     }

    /**
     * Gets the status value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the message value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the PGTxnErrorCode value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @return PGTxnErrorCode
     */
    public java.lang.String getPGTxnErrorCode() {
        return PGTxnErrorCode;
    }


    /**
     * Sets the PGTxnErrorCode value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @param PGTxnErrorCode
     */
    public void setPGTxnErrorCode(java.lang.String PGTxnErrorCode) {
        this.PGTxnErrorCode = PGTxnErrorCode;
    }


    /**
     * Gets the UTRNumber value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @return UTRNumber
     */
    public java.lang.String getUTRNumber() {
        return UTRNumber;
    }


    /**
     * Sets the UTRNumber value for this TransactionEnquiryResponseTransactionEnquiryResponse.
     * 
     * @param UTRNumber
     */
    public void setUTRNumber(java.lang.String UTRNumber) {
        this.UTRNumber = UTRNumber;
    }

    

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TransactionEnquiryResponse)) return false;
        TransactionEnquiryResponse other = (TransactionEnquiryResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.PGTxnErrorCode==null && other.getPGTxnErrorCode()==null) || 
             (this.PGTxnErrorCode!=null &&
              this.PGTxnErrorCode.equals(other.getPGTxnErrorCode()))) &&
            ((this.UTRNumber==null && other.getUTRNumber()==null) || 
             (this.UTRNumber!=null &&
              this.UTRNumber.equals(other.getUTRNumber())));
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
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getPGTxnErrorCode() != null) {
            _hashCode += getPGTxnErrorCode().hashCode();
        }
        if (getUTRNumber() != null) {
            _hashCode += getUTRNumber().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TransactionEnquiryResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/TransactionEnquiry", "TransactionEnquiryResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("PGTxnErrorCode");
       //elemField.setXmlName(new javax.xml.namespace.QName("", "PGTxnErrorCode"));
        elemField.setXmlName(new javax.xml.namespace.QName("", "ErrorCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("UTRNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UTRNumber"));
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

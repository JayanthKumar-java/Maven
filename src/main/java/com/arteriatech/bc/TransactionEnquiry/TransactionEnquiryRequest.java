/**
 * TransactionEnquiryRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.TransactionEnquiry;

public class TransactionEnquiryRequest  implements java.io.Serializable {
    private java.lang.String corporateID;

    private java.lang.String userID;

    private java.lang.String userRegistrationID;

    private java.lang.String uniqueID;

    private java.lang.String aggregatorID;

    public TransactionEnquiryRequest() {
    }

    public TransactionEnquiryRequest(
           java.lang.String corporateID,
           java.lang.String userID,
           java.lang.String userRegistrationID,
           java.lang.String uniqueID,
           java.lang.String aggregatorID) {
           this.corporateID = corporateID;
           this.userID = userID;
           this.userRegistrationID = userRegistrationID;
           this.uniqueID = uniqueID;
           this.aggregatorID = aggregatorID;
    }


    /**
     * Gets the corporateID value for this TransactionEnquiryRequest.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this TransactionEnquiryRequest.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the userID value for this TransactionEnquiryRequest.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this TransactionEnquiryRequest.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }


    /**
     * Gets the userRegistrationID value for this TransactionEnquiryRequest.
     * 
     * @return userRegistrationID
     */
    public java.lang.String getUserRegistrationID() {
        return userRegistrationID;
    }


    /**
     * Sets the userRegistrationID value for this TransactionEnquiryRequest.
     * 
     * @param userRegistrationID
     */
    public void setUserRegistrationID(java.lang.String userRegistrationID) {
        this.userRegistrationID = userRegistrationID;
    }


    /**
     * Gets the uniqueID value for this TransactionEnquiryRequest.
     * 
     * @return uniqueID
     */
    public java.lang.String getUniqueID() {
        return uniqueID;
    }


    /**
     * Sets the uniqueID value for this TransactionEnquiryRequest.
     * 
     * @param uniqueID
     */
    public void setUniqueID(java.lang.String uniqueID) {
        this.uniqueID = uniqueID;
    }


    /**
     * Gets the aggregatorID value for this TransactionEnquiryRequest.
     * 
     * @return aggregatorID
     */
    public java.lang.String getAggregatorID() {
        return aggregatorID;
    }


    /**
     * Sets the aggregatorID value for this TransactionEnquiryRequest.
     * 
     * @param aggregatorID
     */
    public void setAggregatorID(java.lang.String aggregatorID) {
        this.aggregatorID = aggregatorID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TransactionEnquiryRequest)) return false;
        TransactionEnquiryRequest other = (TransactionEnquiryRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.corporateID==null && other.getCorporateID()==null) || 
             (this.corporateID!=null &&
              this.corporateID.equals(other.getCorporateID()))) &&
            ((this.userID==null && other.getUserID()==null) || 
             (this.userID!=null &&
              this.userID.equals(other.getUserID()))) &&
            ((this.userRegistrationID==null && other.getUserRegistrationID()==null) || 
             (this.userRegistrationID!=null &&
              this.userRegistrationID.equals(other.getUserRegistrationID()))) &&
            ((this.uniqueID==null && other.getUniqueID()==null) || 
             (this.uniqueID!=null &&
              this.uniqueID.equals(other.getUniqueID()))) &&
            ((this.aggregatorID==null && other.getAggregatorID()==null) || 
             (this.aggregatorID!=null &&
              this.aggregatorID.equals(other.getAggregatorID())));
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
        if (getCorporateID() != null) {
            _hashCode += getCorporateID().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        if (getUserRegistrationID() != null) {
            _hashCode += getUserRegistrationID().hashCode();
        }
        if (getUniqueID() != null) {
            _hashCode += getUniqueID().hashCode();
        }
        if (getAggregatorID() != null) {
            _hashCode += getAggregatorID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TransactionEnquiryRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/TransactionEnquiry", "TransactionEnquiryRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corporateID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorporateID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userRegistrationID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserRegistrationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uniqueID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UniqueID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregatorID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AggregatorID"));
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

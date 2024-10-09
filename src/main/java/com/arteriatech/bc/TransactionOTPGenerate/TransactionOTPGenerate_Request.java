/**
 * TransactionOTPGenerate_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.TransactionOTPGenerate;

public class TransactionOTPGenerate_Request  implements java.io.Serializable {
    private java.lang.String remarks;

    private java.lang.String debitAccountNumber;

    private java.lang.String currency;

    private java.lang.String amount;

    private java.lang.String OTPDeliveryMode;

    private java.lang.String transactionType;

    private java.lang.String trackID;

    private java.lang.String corporateID;

    private java.lang.String userID;

    private java.lang.String userRegistrationID;

    private java.lang.String aggregatorID;

    public TransactionOTPGenerate_Request() {
    }

    public TransactionOTPGenerate_Request(
           java.lang.String remarks,
           java.lang.String debitAccountNumber,
           java.lang.String currency,
           java.lang.String amount,
           java.lang.String OTPDeliveryMode,
           java.lang.String transactionType,
           java.lang.String trackID,
           java.lang.String corporateID,
           java.lang.String userID,
           java.lang.String userRegistrationID,
           java.lang.String aggregatorID) {
           this.remarks = remarks;
           this.debitAccountNumber = debitAccountNumber;
           this.currency = currency;
           this.amount = amount;
           this.OTPDeliveryMode = OTPDeliveryMode;
           this.transactionType = transactionType;
           this.trackID = trackID;
           this.corporateID = corporateID;
           this.userID = userID;
           this.userRegistrationID = userRegistrationID;
           this.aggregatorID = aggregatorID;
    }


    /**
     * Gets the remarks value for this TransactionOTPGenerate_Request.
     * 
     * @return remarks
     */
    public java.lang.String getRemarks() {
        return remarks;
    }


    /**
     * Sets the remarks value for this TransactionOTPGenerate_Request.
     * 
     * @param remarks
     */
    public void setRemarks(java.lang.String remarks) {
        this.remarks = remarks;
    }


    /**
     * Gets the debitAccountNumber value for this TransactionOTPGenerate_Request.
     * 
     * @return debitAccountNumber
     */
    public java.lang.String getDebitAccountNumber() {
        return debitAccountNumber;
    }


    /**
     * Sets the debitAccountNumber value for this TransactionOTPGenerate_Request.
     * 
     * @param debitAccountNumber
     */
    public void setDebitAccountNumber(java.lang.String debitAccountNumber) {
        this.debitAccountNumber = debitAccountNumber;
    }


    /**
     * Gets the currency value for this TransactionOTPGenerate_Request.
     * 
     * @return currency
     */
    public java.lang.String getCurrency() {
        return currency;
    }


    /**
     * Sets the currency value for this TransactionOTPGenerate_Request.
     * 
     * @param currency
     */
    public void setCurrency(java.lang.String currency) {
        this.currency = currency;
    }


    /**
     * Gets the amount value for this TransactionOTPGenerate_Request.
     * 
     * @return amount
     */
    public java.lang.String getAmount() {
        return amount;
    }


    /**
     * Sets the amount value for this TransactionOTPGenerate_Request.
     * 
     * @param amount
     */
    public void setAmount(java.lang.String amount) {
        this.amount = amount;
    }


    /**
     * Gets the OTPDeliveryMode value for this TransactionOTPGenerate_Request.
     * 
     * @return OTPDeliveryMode
     */
    public java.lang.String getOTPDeliveryMode() {
        return OTPDeliveryMode;
    }


    /**
     * Sets the OTPDeliveryMode value for this TransactionOTPGenerate_Request.
     * 
     * @param OTPDeliveryMode
     */
    public void setOTPDeliveryMode(java.lang.String OTPDeliveryMode) {
        this.OTPDeliveryMode = OTPDeliveryMode;
    }


    /**
     * Gets the transactionType value for this TransactionOTPGenerate_Request.
     * 
     * @return transactionType
     */
    public java.lang.String getTransactionType() {
        return transactionType;
    }


    /**
     * Sets the transactionType value for this TransactionOTPGenerate_Request.
     * 
     * @param transactionType
     */
    public void setTransactionType(java.lang.String transactionType) {
        this.transactionType = transactionType;
    }


    /**
     * Gets the trackID value for this TransactionOTPGenerate_Request.
     * 
     * @return trackID
     */
    public java.lang.String getTrackID() {
        return trackID;
    }


    /**
     * Sets the trackID value for this TransactionOTPGenerate_Request.
     * 
     * @param trackID
     */
    public void setTrackID(java.lang.String trackID) {
        this.trackID = trackID;
    }


    /**
     * Gets the corporateID value for this TransactionOTPGenerate_Request.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this TransactionOTPGenerate_Request.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the userID value for this TransactionOTPGenerate_Request.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this TransactionOTPGenerate_Request.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }


    /**
     * Gets the userRegistrationID value for this TransactionOTPGenerate_Request.
     * 
     * @return userRegistrationID
     */
    public java.lang.String getUserRegistrationID() {
        return userRegistrationID;
    }


    /**
     * Sets the userRegistrationID value for this TransactionOTPGenerate_Request.
     * 
     * @param userRegistrationID
     */
    public void setUserRegistrationID(java.lang.String userRegistrationID) {
        this.userRegistrationID = userRegistrationID;
    }


    /**
     * Gets the aggregatorID value for this TransactionOTPGenerate_Request.
     * 
     * @return aggregatorID
     */
    public java.lang.String getAggregatorID() {
        return aggregatorID;
    }


    /**
     * Sets the aggregatorID value for this TransactionOTPGenerate_Request.
     * 
     * @param aggregatorID
     */
    public void setAggregatorID(java.lang.String aggregatorID) {
        this.aggregatorID = aggregatorID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TransactionOTPGenerate_Request)) return false;
        TransactionOTPGenerate_Request other = (TransactionOTPGenerate_Request) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.remarks==null && other.getRemarks()==null) || 
             (this.remarks!=null &&
              this.remarks.equals(other.getRemarks()))) &&
            ((this.debitAccountNumber==null && other.getDebitAccountNumber()==null) || 
             (this.debitAccountNumber!=null &&
              this.debitAccountNumber.equals(other.getDebitAccountNumber()))) &&
            ((this.currency==null && other.getCurrency()==null) || 
             (this.currency!=null &&
              this.currency.equals(other.getCurrency()))) &&
            ((this.amount==null && other.getAmount()==null) || 
             (this.amount!=null &&
              this.amount.equals(other.getAmount()))) &&
            ((this.OTPDeliveryMode==null && other.getOTPDeliveryMode()==null) || 
             (this.OTPDeliveryMode!=null &&
              this.OTPDeliveryMode.equals(other.getOTPDeliveryMode()))) &&
            ((this.transactionType==null && other.getTransactionType()==null) || 
             (this.transactionType!=null &&
              this.transactionType.equals(other.getTransactionType()))) &&
            ((this.trackID==null && other.getTrackID()==null) || 
             (this.trackID!=null &&
              this.trackID.equals(other.getTrackID()))) &&
            ((this.corporateID==null && other.getCorporateID()==null) || 
             (this.corporateID!=null &&
              this.corporateID.equals(other.getCorporateID()))) &&
            ((this.userID==null && other.getUserID()==null) || 
             (this.userID!=null &&
              this.userID.equals(other.getUserID()))) &&
            ((this.userRegistrationID==null && other.getUserRegistrationID()==null) || 
             (this.userRegistrationID!=null &&
              this.userRegistrationID.equals(other.getUserRegistrationID()))) &&
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
        if (getRemarks() != null) {
            _hashCode += getRemarks().hashCode();
        }
        if (getDebitAccountNumber() != null) {
            _hashCode += getDebitAccountNumber().hashCode();
        }
        if (getCurrency() != null) {
            _hashCode += getCurrency().hashCode();
        }
        if (getAmount() != null) {
            _hashCode += getAmount().hashCode();
        }
        if (getOTPDeliveryMode() != null) {
            _hashCode += getOTPDeliveryMode().hashCode();
        }
        if (getTransactionType() != null) {
            _hashCode += getTransactionType().hashCode();
        }
        if (getTrackID() != null) {
            _hashCode += getTrackID().hashCode();
        }
        if (getCorporateID() != null) {
            _hashCode += getCorporateID().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        if (getUserRegistrationID() != null) {
            _hashCode += getUserRegistrationID().hashCode();
        }
        if (getAggregatorID() != null) {
            _hashCode += getAggregatorID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TransactionOTPGenerate_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/TransactionOTPGenerate", "TransactionOTPGenerate_Request"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remarks");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Remarks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("debitAccountNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DebitAccountNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currency");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Currency"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("amount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Amount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("OTPDeliveryMode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OTPDeliveryMode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TransactionType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trackID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TrackID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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

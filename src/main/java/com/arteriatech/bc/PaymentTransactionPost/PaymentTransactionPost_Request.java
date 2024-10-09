/**
 * PaymentTransactionPost_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.PaymentTransactionPost;

public class PaymentTransactionPost_Request  implements java.io.Serializable {
    private java.lang.String corporateID;

    private java.lang.String userID;

    private java.lang.String userRegistrationID;

    private java.lang.String trackID;

    private java.lang.String debitAccountNumber;

    private java.lang.String IFSCCode;

    private java.lang.String creditAccountNumber;

    private java.lang.String transactionType;

    private java.lang.String payeeName;

    private java.lang.String amount;

    private java.lang.String currency;

    private java.lang.String remarks;

    private java.lang.String aggregatorID;

    private java.lang.String OTP;

    private java.lang.String PGID;

    private java.lang.String PGCategoryID;

    private java.lang.String transactionDate;

    private java.lang.String transactionTime;

    public PaymentTransactionPost_Request() {
    }

    public PaymentTransactionPost_Request(
           java.lang.String corporateID,
           java.lang.String userID,
           java.lang.String userRegistrationID,
           java.lang.String trackID,
           java.lang.String debitAccountNumber,
           java.lang.String IFSCCode,
           java.lang.String creditAccountNumber,
           java.lang.String transactionType,
           java.lang.String payeeName,
           java.lang.String amount,
           java.lang.String currency,
           java.lang.String remarks,
           java.lang.String aggregatorID,
           java.lang.String OTP,
           java.lang.String PGID,
           java.lang.String PGCategoryID,
           java.lang.String transactionDate,
           java.lang.String transactionTime) {
           this.corporateID = corporateID;
           this.userID = userID;
           this.userRegistrationID = userRegistrationID;
           this.trackID = trackID;
           this.debitAccountNumber = debitAccountNumber;
           this.IFSCCode = IFSCCode;
           this.creditAccountNumber = creditAccountNumber;
           this.transactionType = transactionType;
           this.payeeName = payeeName;
           this.amount = amount;
           this.currency = currency;
           this.remarks = remarks;
           this.aggregatorID = aggregatorID;
           this.OTP = OTP;
           this.PGID = PGID;
           this.PGCategoryID = PGCategoryID;
           this.transactionDate = transactionDate;
           this.transactionTime = transactionTime;
    }


    /**
     * Gets the corporateID value for this PaymentTransactionPost_Request.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this PaymentTransactionPost_Request.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the userID value for this PaymentTransactionPost_Request.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this PaymentTransactionPost_Request.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }


    /**
     * Gets the userRegistrationID value for this PaymentTransactionPost_Request.
     * 
     * @return userRegistrationID
     */
    public java.lang.String getUserRegistrationID() {
        return userRegistrationID;
    }


    /**
     * Sets the userRegistrationID value for this PaymentTransactionPost_Request.
     * 
     * @param userRegistrationID
     */
    public void setUserRegistrationID(java.lang.String userRegistrationID) {
        this.userRegistrationID = userRegistrationID;
    }


    /**
     * Gets the trackID value for this PaymentTransactionPost_Request.
     * 
     * @return trackID
     */
    public java.lang.String getTrackID() {
        return trackID;
    }


    /**
     * Sets the trackID value for this PaymentTransactionPost_Request.
     * 
     * @param trackID
     */
    public void setTrackID(java.lang.String trackID) {
        this.trackID = trackID;
    }


    /**
     * Gets the debitAccountNumber value for this PaymentTransactionPost_Request.
     * 
     * @return debitAccountNumber
     */
    public java.lang.String getDebitAccountNumber() {
        return debitAccountNumber;
    }


    /**
     * Sets the debitAccountNumber value for this PaymentTransactionPost_Request.
     * 
     * @param debitAccountNumber
     */
    public void setDebitAccountNumber(java.lang.String debitAccountNumber) {
        this.debitAccountNumber = debitAccountNumber;
    }


    /**
     * Gets the IFSCCode value for this PaymentTransactionPost_Request.
     * 
     * @return IFSCCode
     */
    public java.lang.String getIFSCCode() {
        return IFSCCode;
    }


    /**
     * Sets the IFSCCode value for this PaymentTransactionPost_Request.
     * 
     * @param IFSCCode
     */
    public void setIFSCCode(java.lang.String IFSCCode) {
        this.IFSCCode = IFSCCode;
    }


    /**
     * Gets the creditAccountNumber value for this PaymentTransactionPost_Request.
     * 
     * @return creditAccountNumber
     */
    public java.lang.String getCreditAccountNumber() {
        return creditAccountNumber;
    }


    /**
     * Sets the creditAccountNumber value for this PaymentTransactionPost_Request.
     * 
     * @param creditAccountNumber
     */
    public void setCreditAccountNumber(java.lang.String creditAccountNumber) {
        this.creditAccountNumber = creditAccountNumber;
    }


    /**
     * Gets the transactionType value for this PaymentTransactionPost_Request.
     * 
     * @return transactionType
     */
    public java.lang.String getTransactionType() {
        return transactionType;
    }


    /**
     * Sets the transactionType value for this PaymentTransactionPost_Request.
     * 
     * @param transactionType
     */
    public void setTransactionType(java.lang.String transactionType) {
        this.transactionType = transactionType;
    }


    /**
     * Gets the payeeName value for this PaymentTransactionPost_Request.
     * 
     * @return payeeName
     */
    public java.lang.String getPayeeName() {
        return payeeName;
    }


    /**
     * Sets the payeeName value for this PaymentTransactionPost_Request.
     * 
     * @param payeeName
     */
    public void setPayeeName(java.lang.String payeeName) {
        this.payeeName = payeeName;
    }


    /**
     * Gets the amount value for this PaymentTransactionPost_Request.
     * 
     * @return amount
     */
    public java.lang.String getAmount() {
        return amount;
    }


    /**
     * Sets the amount value for this PaymentTransactionPost_Request.
     * 
     * @param amount
     */
    public void setAmount(java.lang.String amount) {
        this.amount = amount;
    }


    /**
     * Gets the currency value for this PaymentTransactionPost_Request.
     * 
     * @return currency
     */
    public java.lang.String getCurrency() {
        return currency;
    }


    /**
     * Sets the currency value for this PaymentTransactionPost_Request.
     * 
     * @param currency
     */
    public void setCurrency(java.lang.String currency) {
        this.currency = currency;
    }


    /**
     * Gets the remarks value for this PaymentTransactionPost_Request.
     * 
     * @return remarks
     */
    public java.lang.String getRemarks() {
        return remarks;
    }


    /**
     * Sets the remarks value for this PaymentTransactionPost_Request.
     * 
     * @param remarks
     */
    public void setRemarks(java.lang.String remarks) {
        this.remarks = remarks;
    }


    /**
     * Gets the aggregatorID value for this PaymentTransactionPost_Request.
     * 
     * @return aggregatorID
     */
    public java.lang.String getAggregatorID() {
        return aggregatorID;
    }


    /**
     * Sets the aggregatorID value for this PaymentTransactionPost_Request.
     * 
     * @param aggregatorID
     */
    public void setAggregatorID(java.lang.String aggregatorID) {
        this.aggregatorID = aggregatorID;
    }


    /**
     * Gets the OTP value for this PaymentTransactionPost_Request.
     * 
     * @return OTP
     */
    public java.lang.String getOTP() {
        return OTP;
    }


    /**
     * Sets the OTP value for this PaymentTransactionPost_Request.
     * 
     * @param OTP
     */
    public void setOTP(java.lang.String OTP) {
        this.OTP = OTP;
    }


    /**
     * Gets the PGID value for this PaymentTransactionPost_Request.
     * 
     * @return PGID
     */
    public java.lang.String getPGID() {
        return PGID;
    }


    /**
     * Sets the PGID value for this PaymentTransactionPost_Request.
     * 
     * @param PGID
     */
    public void setPGID(java.lang.String PGID) {
        this.PGID = PGID;
    }


    /**
     * Gets the PGCategoryID value for this PaymentTransactionPost_Request.
     * 
     * @return PGCategoryID
     */
    public java.lang.String getPGCategoryID() {
        return PGCategoryID;
    }


    /**
     * Sets the PGCategoryID value for this PaymentTransactionPost_Request.
     * 
     * @param PGCategoryID
     */
    public void setPGCategoryID(java.lang.String PGCategoryID) {
        this.PGCategoryID = PGCategoryID;
    }


    /**
     * Gets the transactionDate value for this PaymentTransactionPost_Request.
     * 
     * @return transactionDate
     */
    public java.lang.String getTransactionDate() {
        return transactionDate;
    }


    /**
     * Sets the transactionDate value for this PaymentTransactionPost_Request.
     * 
     * @param transactionDate
     */
    public void setTransactionDate(java.lang.String transactionDate) {
        this.transactionDate = transactionDate;
    }


    /**
     * Gets the transactionTime value for this PaymentTransactionPost_Request.
     * 
     * @return transactionTime
     */
    public java.lang.String getTransactionTime() {
        return transactionTime;
    }


    /**
     * Sets the transactionTime value for this PaymentTransactionPost_Request.
     * 
     * @param transactionTime
     */
    public void setTransactionTime(java.lang.String transactionTime) {
        this.transactionTime = transactionTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PaymentTransactionPost_Request)) return false;
        PaymentTransactionPost_Request other = (PaymentTransactionPost_Request) obj;
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
            ((this.trackID==null && other.getTrackID()==null) || 
             (this.trackID!=null &&
              this.trackID.equals(other.getTrackID()))) &&
            ((this.debitAccountNumber==null && other.getDebitAccountNumber()==null) || 
             (this.debitAccountNumber!=null &&
              this.debitAccountNumber.equals(other.getDebitAccountNumber()))) &&
            ((this.IFSCCode==null && other.getIFSCCode()==null) || 
             (this.IFSCCode!=null &&
              this.IFSCCode.equals(other.getIFSCCode()))) &&
            ((this.creditAccountNumber==null && other.getCreditAccountNumber()==null) || 
             (this.creditAccountNumber!=null &&
              this.creditAccountNumber.equals(other.getCreditAccountNumber()))) &&
            ((this.transactionType==null && other.getTransactionType()==null) || 
             (this.transactionType!=null &&
              this.transactionType.equals(other.getTransactionType()))) &&
            ((this.payeeName==null && other.getPayeeName()==null) || 
             (this.payeeName!=null &&
              this.payeeName.equals(other.getPayeeName()))) &&
            ((this.amount==null && other.getAmount()==null) || 
             (this.amount!=null &&
              this.amount.equals(other.getAmount()))) &&
            ((this.currency==null && other.getCurrency()==null) || 
             (this.currency!=null &&
              this.currency.equals(other.getCurrency()))) &&
            ((this.remarks==null && other.getRemarks()==null) || 
             (this.remarks!=null &&
              this.remarks.equals(other.getRemarks()))) &&
            ((this.aggregatorID==null && other.getAggregatorID()==null) || 
             (this.aggregatorID!=null &&
              this.aggregatorID.equals(other.getAggregatorID()))) &&
            ((this.OTP==null && other.getOTP()==null) || 
             (this.OTP!=null &&
              this.OTP.equals(other.getOTP()))) &&
            ((this.PGID==null && other.getPGID()==null) || 
             (this.PGID!=null &&
              this.PGID.equals(other.getPGID()))) &&
            ((this.PGCategoryID==null && other.getPGCategoryID()==null) || 
             (this.PGCategoryID!=null &&
              this.PGCategoryID.equals(other.getPGCategoryID()))) &&
            ((this.transactionDate==null && other.getTransactionDate()==null) || 
             (this.transactionDate!=null &&
              this.transactionDate.equals(other.getTransactionDate()))) &&
            ((this.transactionTime==null && other.getTransactionTime()==null) || 
             (this.transactionTime!=null &&
              this.transactionTime.equals(other.getTransactionTime())));
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
        if (getTrackID() != null) {
            _hashCode += getTrackID().hashCode();
        }
        if (getDebitAccountNumber() != null) {
            _hashCode += getDebitAccountNumber().hashCode();
        }
        if (getIFSCCode() != null) {
            _hashCode += getIFSCCode().hashCode();
        }
        if (getCreditAccountNumber() != null) {
            _hashCode += getCreditAccountNumber().hashCode();
        }
        if (getTransactionType() != null) {
            _hashCode += getTransactionType().hashCode();
        }
        if (getPayeeName() != null) {
            _hashCode += getPayeeName().hashCode();
        }
        if (getAmount() != null) {
            _hashCode += getAmount().hashCode();
        }
        if (getCurrency() != null) {
            _hashCode += getCurrency().hashCode();
        }
        if (getRemarks() != null) {
            _hashCode += getRemarks().hashCode();
        }
        if (getAggregatorID() != null) {
            _hashCode += getAggregatorID().hashCode();
        }
        if (getOTP() != null) {
            _hashCode += getOTP().hashCode();
        }
        if (getPGID() != null) {
            _hashCode += getPGID().hashCode();
        }
        if (getPGCategoryID() != null) {
            _hashCode += getPGCategoryID().hashCode();
        }
        if (getTransactionDate() != null) {
            _hashCode += getTransactionDate().hashCode();
        }
        if (getTransactionTime() != null) {
            _hashCode += getTransactionTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PaymentTransactionPost_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/PaymentTransactionPost", "PaymentTransactionPost_Request"));
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
        elemField.setFieldName("trackID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TrackID"));
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
        elemField.setFieldName("IFSCCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IFSCCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creditAccountNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CreditAccountNumber"));
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
        elemField.setFieldName("payeeName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PayeeName"));
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
        elemField.setFieldName("currency");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Currency"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remarks");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Remarks"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("OTP");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OTP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PGID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PGID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PGCategoryID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PGCategoryID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionDate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TransactionDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TransactionTime"));
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

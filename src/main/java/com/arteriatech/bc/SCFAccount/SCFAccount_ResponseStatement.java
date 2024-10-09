/**
 * SCFAccount_ResponseStatement.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFAccount;

public class SCFAccount_ResponseStatement  implements java.io.Serializable {
    private java.lang.String freezeFlag;

    private java.lang.String dueDays;

    private java.lang.String overdueDays;

    private java.lang.String totalOverdueBeyondCureDays;

    private java.lang.String totalOverdueBeyondCureAmount;

    private java.lang.String isPrincipalOverdue;

    private java.lang.String interestOverdueAmount;

    private java.lang.String feeOverdueAmount;

    private java.lang.String principalOverdueAmount;

    private java.lang.String reportDate;

    private java.lang.String sanctionLimit;

    public SCFAccount_ResponseStatement() {
    }

    public SCFAccount_ResponseStatement(
           java.lang.String freezeFlag,
           java.lang.String dueDays,
           java.lang.String overdueDays,
           java.lang.String totalOverdueBeyondCureDays,
           java.lang.String totalOverdueBeyondCureAmount,
           java.lang.String isPrincipalOverdue,
           java.lang.String interestOverdueAmount,
           java.lang.String feeOverdueAmount,
           java.lang.String principalOverdueAmount,
           java.lang.String reportDate,
           java.lang.String sanctionLimit) {
           this.freezeFlag = freezeFlag;
           this.dueDays = dueDays;
           this.overdueDays = overdueDays;
           this.totalOverdueBeyondCureDays = totalOverdueBeyondCureDays;
           this.totalOverdueBeyondCureAmount = totalOverdueBeyondCureAmount;
           this.isPrincipalOverdue = isPrincipalOverdue;
           this.interestOverdueAmount = interestOverdueAmount;
           this.feeOverdueAmount = feeOverdueAmount;
           this.principalOverdueAmount = principalOverdueAmount;
           this.reportDate = reportDate;
           this.sanctionLimit = sanctionLimit;
    }


    /**
     * Gets the freezeFlag value for this SCFAccount_ResponseStatement.
     * 
     * @return freezeFlag
     */
    public java.lang.String getFreezeFlag() {
        return freezeFlag;
    }


    /**
     * Sets the freezeFlag value for this SCFAccount_ResponseStatement.
     * 
     * @param freezeFlag
     */
    public void setFreezeFlag(java.lang.String freezeFlag) {
        this.freezeFlag = freezeFlag;
    }


    /**
     * Gets the dueDays value for this SCFAccount_ResponseStatement.
     * 
     * @return dueDays
     */
    public java.lang.String getDueDays() {
        return dueDays;
    }


    /**
     * Sets the dueDays value for this SCFAccount_ResponseStatement.
     * 
     * @param dueDays
     */
    public void setDueDays(java.lang.String dueDays) {
        this.dueDays = dueDays;
    }


    /**
     * Gets the overdueDays value for this SCFAccount_ResponseStatement.
     * 
     * @return overdueDays
     */
    public java.lang.String getOverdueDays() {
        return overdueDays;
    }


    /**
     * Sets the overdueDays value for this SCFAccount_ResponseStatement.
     * 
     * @param overdueDays
     */
    public void setOverdueDays(java.lang.String overdueDays) {
        this.overdueDays = overdueDays;
    }


    /**
     * Gets the totalOverdueBeyondCureDays value for this SCFAccount_ResponseStatement.
     * 
     * @return totalOverdueBeyondCureDays
     */
    public java.lang.String getTotalOverdueBeyondCureDays() {
        return totalOverdueBeyondCureDays;
    }


    /**
     * Sets the totalOverdueBeyondCureDays value for this SCFAccount_ResponseStatement.
     * 
     * @param totalOverdueBeyondCureDays
     */
    public void setTotalOverdueBeyondCureDays(java.lang.String totalOverdueBeyondCureDays) {
        this.totalOverdueBeyondCureDays = totalOverdueBeyondCureDays;
    }


    /**
     * Gets the totalOverdueBeyondCureAmount value for this SCFAccount_ResponseStatement.
     * 
     * @return totalOverdueBeyondCureAmount
     */
    public java.lang.String getTotalOverdueBeyondCureAmount() {
        return totalOverdueBeyondCureAmount;
    }


    /**
     * Sets the totalOverdueBeyondCureAmount value for this SCFAccount_ResponseStatement.
     * 
     * @param totalOverdueBeyondCureAmount
     */
    public void setTotalOverdueBeyondCureAmount(java.lang.String totalOverdueBeyondCureAmount) {
        this.totalOverdueBeyondCureAmount = totalOverdueBeyondCureAmount;
    }


    /**
     * Gets the isPrincipalOverdue value for this SCFAccount_ResponseStatement.
     * 
     * @return isPrincipalOverdue
     */
    public java.lang.String getIsPrincipalOverdue() {
        return isPrincipalOverdue;
    }


    /**
     * Sets the isPrincipalOverdue value for this SCFAccount_ResponseStatement.
     * 
     * @param isPrincipalOverdue
     */
    public void setIsPrincipalOverdue(java.lang.String isPrincipalOverdue) {
        this.isPrincipalOverdue = isPrincipalOverdue;
    }


    /**
     * Gets the interestOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @return interestOverdueAmount
     */
    public java.lang.String getInterestOverdueAmount() {
        return interestOverdueAmount;
    }


    /**
     * Sets the interestOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @param interestOverdueAmount
     */
    public void setInterestOverdueAmount(java.lang.String interestOverdueAmount) {
        this.interestOverdueAmount = interestOverdueAmount;
    }


    /**
     * Gets the feeOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @return feeOverdueAmount
     */
    public java.lang.String getFeeOverdueAmount() {
        return feeOverdueAmount;
    }


    /**
     * Sets the feeOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @param feeOverdueAmount
     */
    public void setFeeOverdueAmount(java.lang.String feeOverdueAmount) {
        this.feeOverdueAmount = feeOverdueAmount;
    }


    /**
     * Gets the principalOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @return principalOverdueAmount
     */
    public java.lang.String getPrincipalOverdueAmount() {
        return principalOverdueAmount;
    }


    /**
     * Sets the principalOverdueAmount value for this SCFAccount_ResponseStatement.
     * 
     * @param principalOverdueAmount
     */
    public void setPrincipalOverdueAmount(java.lang.String principalOverdueAmount) {
        this.principalOverdueAmount = principalOverdueAmount;
    }


    /**
     * Gets the reportDate value for this SCFAccount_ResponseStatement.
     * 
     * @return reportDate
     */
    public java.lang.String getReportDate() {
        return reportDate;
    }


    /**
     * Sets the reportDate value for this SCFAccount_ResponseStatement.
     * 
     * @param reportDate
     */
    public void setReportDate(java.lang.String reportDate) {
        this.reportDate = reportDate;
    }


    /**
     * Gets the sanctionLimit value for this SCFAccount_ResponseStatement.
     * 
     * @return sanctionLimit
     */
    public java.lang.String getSanctionLimit() {
        return sanctionLimit;
    }


    /**
     * Sets the sanctionLimit value for this SCFAccount_ResponseStatement.
     * 
     * @param sanctionLimit
     */
    public void setSanctionLimit(java.lang.String sanctionLimit) {
        this.sanctionLimit = sanctionLimit;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFAccount_ResponseStatement)) return false;
        SCFAccount_ResponseStatement other = (SCFAccount_ResponseStatement) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.freezeFlag==null && other.getFreezeFlag()==null) || 
             (this.freezeFlag!=null &&
              this.freezeFlag.equals(other.getFreezeFlag()))) &&
            ((this.dueDays==null && other.getDueDays()==null) || 
             (this.dueDays!=null &&
              this.dueDays.equals(other.getDueDays()))) &&
            ((this.overdueDays==null && other.getOverdueDays()==null) || 
             (this.overdueDays!=null &&
              this.overdueDays.equals(other.getOverdueDays()))) &&
            ((this.totalOverdueBeyondCureDays==null && other.getTotalOverdueBeyondCureDays()==null) || 
             (this.totalOverdueBeyondCureDays!=null &&
              this.totalOverdueBeyondCureDays.equals(other.getTotalOverdueBeyondCureDays()))) &&
            ((this.totalOverdueBeyondCureAmount==null && other.getTotalOverdueBeyondCureAmount()==null) || 
             (this.totalOverdueBeyondCureAmount!=null &&
              this.totalOverdueBeyondCureAmount.equals(other.getTotalOverdueBeyondCureAmount()))) &&
            ((this.isPrincipalOverdue==null && other.getIsPrincipalOverdue()==null) || 
             (this.isPrincipalOverdue!=null &&
              this.isPrincipalOverdue.equals(other.getIsPrincipalOverdue()))) &&
            ((this.interestOverdueAmount==null && other.getInterestOverdueAmount()==null) || 
             (this.interestOverdueAmount!=null &&
              this.interestOverdueAmount.equals(other.getInterestOverdueAmount()))) &&
            ((this.feeOverdueAmount==null && other.getFeeOverdueAmount()==null) || 
             (this.feeOverdueAmount!=null &&
              this.feeOverdueAmount.equals(other.getFeeOverdueAmount()))) &&
            ((this.principalOverdueAmount==null && other.getPrincipalOverdueAmount()==null) || 
             (this.principalOverdueAmount!=null &&
              this.principalOverdueAmount.equals(other.getPrincipalOverdueAmount()))) &&
            ((this.reportDate==null && other.getReportDate()==null) || 
             (this.reportDate!=null &&
              this.reportDate.equals(other.getReportDate()))) &&
            ((this.sanctionLimit==null && other.getSanctionLimit()==null) || 
             (this.sanctionLimit!=null &&
              this.sanctionLimit.equals(other.getSanctionLimit())));
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
        if (getFreezeFlag() != null) {
            _hashCode += getFreezeFlag().hashCode();
        }
        if (getDueDays() != null) {
            _hashCode += getDueDays().hashCode();
        }
        if (getOverdueDays() != null) {
            _hashCode += getOverdueDays().hashCode();
        }
        if (getTotalOverdueBeyondCureDays() != null) {
            _hashCode += getTotalOverdueBeyondCureDays().hashCode();
        }
        if (getTotalOverdueBeyondCureAmount() != null) {
            _hashCode += getTotalOverdueBeyondCureAmount().hashCode();
        }
        if (getIsPrincipalOverdue() != null) {
            _hashCode += getIsPrincipalOverdue().hashCode();
        }
        if (getInterestOverdueAmount() != null) {
            _hashCode += getInterestOverdueAmount().hashCode();
        }
        if (getFeeOverdueAmount() != null) {
            _hashCode += getFeeOverdueAmount().hashCode();
        }
        if (getPrincipalOverdueAmount() != null) {
            _hashCode += getPrincipalOverdueAmount().hashCode();
        }
        if (getReportDate() != null) {
            _hashCode += getReportDate().hashCode();
        }
        if (getSanctionLimit() != null) {
            _hashCode += getSanctionLimit().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFAccount_ResponseStatement.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFAccount", ">SCFAccount_Response>Statement"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("freezeFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FreezeFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dueDays");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DueDays"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("overdueDays");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OverdueDays"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalOverdueBeyondCureDays");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TotalOverdueBeyondCureDays"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalOverdueBeyondCureAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TotalOverdueBeyondCureAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isPrincipalOverdue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IsPrincipalOverdue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("interestOverdueAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "InterestOverdueAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("feeOverdueAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FeeOverdueAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("principalOverdueAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PrincipalOverdueAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reportDate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ReportDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sanctionLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SanctionLimit"));
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

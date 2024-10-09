/**
 * ESignContractResponseResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContract;

public class ESignContractResponseResponse  implements java.io.Serializable {
    private java.lang.String userReminderTime;

    private java.lang.String maximumValidityTime;

    private java.lang.String adminReminderTime;

    private java.lang.String contractCreatedOn;

    private java.lang.String contractCreatedAt;

    private java.lang.String contractCompletionTime;

    private java.lang.String callbackURL;

    private java.lang.String initialContractFile;

    private java.lang.String isCompleted;

    private java.lang.String[] customerSupportEmailId;

    private java.lang.String contractCompletionTime2;

    private java.lang.String finalSignedContractFile;

    private java.lang.String finalSignedContractURL;

    private java.lang.String auditCertificate;

    private java.lang.String auditCertificateURL;

    private java.lang.String emailAcceptance;

    private java.lang.String mergedUserConsentURL;

    private java.lang.String mergedUserSignedConsentURL;

    private com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail[] signerDetail;

    private com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult[] result;

    private com.arteriatech.bc.eSignContract.ESignContractResponseResponseError error;

    public ESignContractResponseResponse() {
    }

    public ESignContractResponseResponse(
           java.lang.String userReminderTime,
           java.lang.String maximumValidityTime,
           java.lang.String adminReminderTime,
           java.lang.String contractCreatedOn,
           java.lang.String contractCreatedAt,
           java.lang.String contractCompletionTime,
           java.lang.String callbackURL,
           java.lang.String initialContractFile,
           java.lang.String isCompleted,
           java.lang.String[] customerSupportEmailId,
           java.lang.String contractCompletionTime2,
           java.lang.String finalSignedContractFile,
           java.lang.String finalSignedContractURL,
           java.lang.String auditCertificate,
           java.lang.String auditCertificateURL,
           java.lang.String emailAcceptance,
           java.lang.String mergedUserConsentURL,
           java.lang.String mergedUserSignedConsentURL,
           com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail[] signerDetail,
           com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult[] result,
           com.arteriatech.bc.eSignContract.ESignContractResponseResponseError error) {
           this.userReminderTime = userReminderTime;
           this.maximumValidityTime = maximumValidityTime;
           this.adminReminderTime = adminReminderTime;
           this.contractCreatedOn = contractCreatedOn;
           this.contractCreatedAt = contractCreatedAt;
           this.contractCompletionTime = contractCompletionTime;
           this.callbackURL = callbackURL;
           this.initialContractFile = initialContractFile;
           this.isCompleted = isCompleted;
           this.customerSupportEmailId = customerSupportEmailId;
           this.contractCompletionTime2 = contractCompletionTime2;
           this.finalSignedContractFile = finalSignedContractFile;
           this.finalSignedContractURL = finalSignedContractURL;
           this.auditCertificate = auditCertificate;
           this.auditCertificateURL = auditCertificateURL;
           this.emailAcceptance = emailAcceptance;
           this.mergedUserConsentURL = mergedUserConsentURL;
           this.mergedUserSignedConsentURL = mergedUserSignedConsentURL;
           this.signerDetail = signerDetail;
           this.result = result;
           this.error = error;
    }


    /**
     * Gets the userReminderTime value for this ESignContractResponseResponse.
     * 
     * @return userReminderTime
     */
    public java.lang.String getUserReminderTime() {
        return userReminderTime;
    }


    /**
     * Sets the userReminderTime value for this ESignContractResponseResponse.
     * 
     * @param userReminderTime
     */
    public void setUserReminderTime(java.lang.String userReminderTime) {
        this.userReminderTime = userReminderTime;
    }


    /**
     * Gets the maximumValidityTime value for this ESignContractResponseResponse.
     * 
     * @return maximumValidityTime
     */
    public java.lang.String getMaximumValidityTime() {
        return maximumValidityTime;
    }


    /**
     * Sets the maximumValidityTime value for this ESignContractResponseResponse.
     * 
     * @param maximumValidityTime
     */
    public void setMaximumValidityTime(java.lang.String maximumValidityTime) {
        this.maximumValidityTime = maximumValidityTime;
    }


    /**
     * Gets the adminReminderTime value for this ESignContractResponseResponse.
     * 
     * @return adminReminderTime
     */
    public java.lang.String getAdminReminderTime() {
        return adminReminderTime;
    }


    /**
     * Sets the adminReminderTime value for this ESignContractResponseResponse.
     * 
     * @param adminReminderTime
     */
    public void setAdminReminderTime(java.lang.String adminReminderTime) {
        this.adminReminderTime = adminReminderTime;
    }


    /**
     * Gets the contractCreatedOn value for this ESignContractResponseResponse.
     * 
     * @return contractCreatedOn
     */
    public java.lang.String getContractCreatedOn() {
        return contractCreatedOn;
    }


    /**
     * Sets the contractCreatedOn value for this ESignContractResponseResponse.
     * 
     * @param contractCreatedOn
     */
    public void setContractCreatedOn(java.lang.String contractCreatedOn) {
        this.contractCreatedOn = contractCreatedOn;
    }


    /**
     * Gets the contractCreatedAt value for this ESignContractResponseResponse.
     * 
     * @return contractCreatedAt
     */
    public java.lang.String getContractCreatedAt() {
        return contractCreatedAt;
    }


    /**
     * Sets the contractCreatedAt value for this ESignContractResponseResponse.
     * 
     * @param contractCreatedAt
     */
    public void setContractCreatedAt(java.lang.String contractCreatedAt) {
        this.contractCreatedAt = contractCreatedAt;
    }


    /**
     * Gets the contractCompletionTime value for this ESignContractResponseResponse.
     * 
     * @return contractCompletionTime
     */
    public java.lang.String getContractCompletionTime() {
        return contractCompletionTime;
    }


    /**
     * Sets the contractCompletionTime value for this ESignContractResponseResponse.
     * 
     * @param contractCompletionTime
     */
    public void setContractCompletionTime(java.lang.String contractCompletionTime) {
        this.contractCompletionTime = contractCompletionTime;
    }


    /**
     * Gets the callbackURL value for this ESignContractResponseResponse.
     * 
     * @return callbackURL
     */
    public java.lang.String getCallbackURL() {
        return callbackURL;
    }


    /**
     * Sets the callbackURL value for this ESignContractResponseResponse.
     * 
     * @param callbackURL
     */
    public void setCallbackURL(java.lang.String callbackURL) {
        this.callbackURL = callbackURL;
    }


    /**
     * Gets the initialContractFile value for this ESignContractResponseResponse.
     * 
     * @return initialContractFile
     */
    public java.lang.String getInitialContractFile() {
        return initialContractFile;
    }


    /**
     * Sets the initialContractFile value for this ESignContractResponseResponse.
     * 
     * @param initialContractFile
     */
    public void setInitialContractFile(java.lang.String initialContractFile) {
        this.initialContractFile = initialContractFile;
    }


    /**
     * Gets the isCompleted value for this ESignContractResponseResponse.
     * 
     * @return isCompleted
     */
    public java.lang.String getIsCompleted() {
        return isCompleted;
    }


    /**
     * Sets the isCompleted value for this ESignContractResponseResponse.
     * 
     * @param isCompleted
     */
    public void setIsCompleted(java.lang.String isCompleted) {
        this.isCompleted = isCompleted;
    }


    /**
     * Gets the customerSupportEmailId value for this ESignContractResponseResponse.
     * 
     * @return customerSupportEmailId
     */
    public java.lang.String[] getCustomerSupportEmailId() {
        return customerSupportEmailId;
    }


    /**
     * Sets the customerSupportEmailId value for this ESignContractResponseResponse.
     * 
     * @param customerSupportEmailId
     */
    public void setCustomerSupportEmailId(java.lang.String[] customerSupportEmailId) {
        this.customerSupportEmailId = customerSupportEmailId;
    }

    public java.lang.String getCustomerSupportEmailId(int i) {
        return this.customerSupportEmailId[i];
    }

    public void setCustomerSupportEmailId(int i, java.lang.String _value) {
        this.customerSupportEmailId[i] = _value;
    }


    /**
     * Gets the contractCompletionTime2 value for this ESignContractResponseResponse.
     * 
     * @return contractCompletionTime2
     */
    public java.lang.String getContractCompletionTime2() {
        return contractCompletionTime2;
    }


    /**
     * Sets the contractCompletionTime2 value for this ESignContractResponseResponse.
     * 
     * @param contractCompletionTime2
     */
    public void setContractCompletionTime2(java.lang.String contractCompletionTime2) {
        this.contractCompletionTime2 = contractCompletionTime2;
    }


    /**
     * Gets the finalSignedContractFile value for this ESignContractResponseResponse.
     * 
     * @return finalSignedContractFile
     */
    public java.lang.String getFinalSignedContractFile() {
        return finalSignedContractFile;
    }


    /**
     * Sets the finalSignedContractFile value for this ESignContractResponseResponse.
     * 
     * @param finalSignedContractFile
     */
    public void setFinalSignedContractFile(java.lang.String finalSignedContractFile) {
        this.finalSignedContractFile = finalSignedContractFile;
    }


    /**
     * Gets the finalSignedContractURL value for this ESignContractResponseResponse.
     * 
     * @return finalSignedContractURL
     */
    public java.lang.String getFinalSignedContractURL() {
        return finalSignedContractURL;
    }


    /**
     * Sets the finalSignedContractURL value for this ESignContractResponseResponse.
     * 
     * @param finalSignedContractURL
     */
    public void setFinalSignedContractURL(java.lang.String finalSignedContractURL) {
        this.finalSignedContractURL = finalSignedContractURL;
    }


    /**
     * Gets the auditCertificate value for this ESignContractResponseResponse.
     * 
     * @return auditCertificate
     */
    public java.lang.String getAuditCertificate() {
        return auditCertificate;
    }


    /**
     * Sets the auditCertificate value for this ESignContractResponseResponse.
     * 
     * @param auditCertificate
     */
    public void setAuditCertificate(java.lang.String auditCertificate) {
        this.auditCertificate = auditCertificate;
    }


    /**
     * Gets the auditCertificateURL value for this ESignContractResponseResponse.
     * 
     * @return auditCertificateURL
     */
    public java.lang.String getAuditCertificateURL() {
        return auditCertificateURL;
    }


    /**
     * Sets the auditCertificateURL value for this ESignContractResponseResponse.
     * 
     * @param auditCertificateURL
     */
    public void setAuditCertificateURL(java.lang.String auditCertificateURL) {
        this.auditCertificateURL = auditCertificateURL;
    }


    /**
     * Gets the emailAcceptance value for this ESignContractResponseResponse.
     * 
     * @return emailAcceptance
     */
    public java.lang.String getEmailAcceptance() {
        return emailAcceptance;
    }


    /**
     * Sets the emailAcceptance value for this ESignContractResponseResponse.
     * 
     * @param emailAcceptance
     */
    public void setEmailAcceptance(java.lang.String emailAcceptance) {
        this.emailAcceptance = emailAcceptance;
    }


    /**
     * Gets the mergedUserConsentURL value for this ESignContractResponseResponse.
     * 
     * @return mergedUserConsentURL
     */
    public java.lang.String getMergedUserConsentURL() {
        return mergedUserConsentURL;
    }


    /**
     * Sets the mergedUserConsentURL value for this ESignContractResponseResponse.
     * 
     * @param mergedUserConsentURL
     */
    public void setMergedUserConsentURL(java.lang.String mergedUserConsentURL) {
        this.mergedUserConsentURL = mergedUserConsentURL;
    }


    /**
     * Gets the mergedUserSignedConsentURL value for this ESignContractResponseResponse.
     * 
     * @return mergedUserSignedConsentURL
     */
    public java.lang.String getMergedUserSignedConsentURL() {
        return mergedUserSignedConsentURL;
    }


    /**
     * Sets the mergedUserSignedConsentURL value for this ESignContractResponseResponse.
     * 
     * @param mergedUserSignedConsentURL
     */
    public void setMergedUserSignedConsentURL(java.lang.String mergedUserSignedConsentURL) {
        this.mergedUserSignedConsentURL = mergedUserSignedConsentURL;
    }


    /**
     * Gets the signerDetail value for this ESignContractResponseResponse.
     * 
     * @return signerDetail
     */
    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail[] getSignerDetail() {
        return signerDetail;
    }


    /**
     * Sets the signerDetail value for this ESignContractResponseResponse.
     * 
     * @param signerDetail
     */
    public void setSignerDetail(com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail[] signerDetail) {
        this.signerDetail = signerDetail;
    }

    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail getSignerDetail(int i) {
        return this.signerDetail[i];
    }

    public void setSignerDetail(int i, com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetail _value) {
        this.signerDetail[i] = _value;
    }


    /**
     * Gets the result value for this ESignContractResponseResponse.
     * 
     * @return result
     */
    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult[] getResult() {
        return result;
    }


    /**
     * Sets the result value for this ESignContractResponseResponse.
     * 
     * @param result
     */
    public void setResult(com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult[] result) {
        this.result = result;
    }

    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult getResult(int i) {
        return this.result[i];
    }

    public void setResult(int i, com.arteriatech.bc.eSignContract.ESignContractResponseResponseResult _value) {
        this.result[i] = _value;
    }


    /**
     * Gets the error value for this ESignContractResponseResponse.
     * 
     * @return error
     */
    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseError getError() {
        return error;
    }


    /**
     * Sets the error value for this ESignContractResponseResponse.
     * 
     * @param error
     */
    public void setError(com.arteriatech.bc.eSignContract.ESignContractResponseResponseError error) {
        this.error = error;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractResponseResponse)) return false;
        ESignContractResponseResponse other = (ESignContractResponseResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.userReminderTime==null && other.getUserReminderTime()==null) || 
             (this.userReminderTime!=null &&
              this.userReminderTime.equals(other.getUserReminderTime()))) &&
            ((this.maximumValidityTime==null && other.getMaximumValidityTime()==null) || 
             (this.maximumValidityTime!=null &&
              this.maximumValidityTime.equals(other.getMaximumValidityTime()))) &&
            ((this.adminReminderTime==null && other.getAdminReminderTime()==null) || 
             (this.adminReminderTime!=null &&
              this.adminReminderTime.equals(other.getAdminReminderTime()))) &&
            ((this.contractCreatedOn==null && other.getContractCreatedOn()==null) || 
             (this.contractCreatedOn!=null &&
              this.contractCreatedOn.equals(other.getContractCreatedOn()))) &&
            ((this.contractCreatedAt==null && other.getContractCreatedAt()==null) || 
             (this.contractCreatedAt!=null &&
              this.contractCreatedAt.equals(other.getContractCreatedAt()))) &&
            ((this.contractCompletionTime==null && other.getContractCompletionTime()==null) || 
             (this.contractCompletionTime!=null &&
              this.contractCompletionTime.equals(other.getContractCompletionTime()))) &&
            ((this.callbackURL==null && other.getCallbackURL()==null) || 
             (this.callbackURL!=null &&
              this.callbackURL.equals(other.getCallbackURL()))) &&
            ((this.initialContractFile==null && other.getInitialContractFile()==null) || 
             (this.initialContractFile!=null &&
              this.initialContractFile.equals(other.getInitialContractFile()))) &&
            ((this.isCompleted==null && other.getIsCompleted()==null) || 
             (this.isCompleted!=null &&
              this.isCompleted.equals(other.getIsCompleted()))) &&
            ((this.customerSupportEmailId==null && other.getCustomerSupportEmailId()==null) || 
             (this.customerSupportEmailId!=null &&
              java.util.Arrays.equals(this.customerSupportEmailId, other.getCustomerSupportEmailId()))) &&
            ((this.contractCompletionTime2==null && other.getContractCompletionTime2()==null) || 
             (this.contractCompletionTime2!=null &&
              this.contractCompletionTime2.equals(other.getContractCompletionTime2()))) &&
            ((this.finalSignedContractFile==null && other.getFinalSignedContractFile()==null) || 
             (this.finalSignedContractFile!=null &&
              this.finalSignedContractFile.equals(other.getFinalSignedContractFile()))) &&
            ((this.finalSignedContractURL==null && other.getFinalSignedContractURL()==null) || 
             (this.finalSignedContractURL!=null &&
              this.finalSignedContractURL.equals(other.getFinalSignedContractURL()))) &&
            ((this.auditCertificate==null && other.getAuditCertificate()==null) || 
             (this.auditCertificate!=null &&
              this.auditCertificate.equals(other.getAuditCertificate()))) &&
            ((this.auditCertificateURL==null && other.getAuditCertificateURL()==null) || 
             (this.auditCertificateURL!=null &&
              this.auditCertificateURL.equals(other.getAuditCertificateURL()))) &&
            ((this.emailAcceptance==null && other.getEmailAcceptance()==null) || 
             (this.emailAcceptance!=null &&
              this.emailAcceptance.equals(other.getEmailAcceptance()))) &&
            ((this.mergedUserConsentURL==null && other.getMergedUserConsentURL()==null) || 
             (this.mergedUserConsentURL!=null &&
              this.mergedUserConsentURL.equals(other.getMergedUserConsentURL()))) &&
            ((this.mergedUserSignedConsentURL==null && other.getMergedUserSignedConsentURL()==null) || 
             (this.mergedUserSignedConsentURL!=null &&
              this.mergedUserSignedConsentURL.equals(other.getMergedUserSignedConsentURL()))) &&
            ((this.signerDetail==null && other.getSignerDetail()==null) || 
             (this.signerDetail!=null &&
              java.util.Arrays.equals(this.signerDetail, other.getSignerDetail()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              java.util.Arrays.equals(this.result, other.getResult()))) &&
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError())));
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
        if (getUserReminderTime() != null) {
            _hashCode += getUserReminderTime().hashCode();
        }
        if (getMaximumValidityTime() != null) {
            _hashCode += getMaximumValidityTime().hashCode();
        }
        if (getAdminReminderTime() != null) {
            _hashCode += getAdminReminderTime().hashCode();
        }
        if (getContractCreatedOn() != null) {
            _hashCode += getContractCreatedOn().hashCode();
        }
        if (getContractCreatedAt() != null) {
            _hashCode += getContractCreatedAt().hashCode();
        }
        if (getContractCompletionTime() != null) {
            _hashCode += getContractCompletionTime().hashCode();
        }
        if (getCallbackURL() != null) {
            _hashCode += getCallbackURL().hashCode();
        }
        if (getInitialContractFile() != null) {
            _hashCode += getInitialContractFile().hashCode();
        }
        if (getIsCompleted() != null) {
            _hashCode += getIsCompleted().hashCode();
        }
        if (getCustomerSupportEmailId() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomerSupportEmailId());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomerSupportEmailId(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getContractCompletionTime2() != null) {
            _hashCode += getContractCompletionTime2().hashCode();
        }
        if (getFinalSignedContractFile() != null) {
            _hashCode += getFinalSignedContractFile().hashCode();
        }
        if (getFinalSignedContractURL() != null) {
            _hashCode += getFinalSignedContractURL().hashCode();
        }
        if (getAuditCertificate() != null) {
            _hashCode += getAuditCertificate().hashCode();
        }
        if (getAuditCertificateURL() != null) {
            _hashCode += getAuditCertificateURL().hashCode();
        }
        if (getEmailAcceptance() != null) {
            _hashCode += getEmailAcceptance().hashCode();
        }
        if (getMergedUserConsentURL() != null) {
            _hashCode += getMergedUserConsentURL().hashCode();
        }
        if (getMergedUserSignedConsentURL() != null) {
            _hashCode += getMergedUserSignedConsentURL().hashCode();
        }
        if (getSignerDetail() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSignerDetail());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSignerDetail(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getResult() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResult());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResult(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractResponseResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">eSignContractResponse>Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userReminderTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserReminderTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maximumValidityTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MaximumValidityTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("adminReminderTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AdminReminderTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractCreatedOn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractCreatedOn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractCreatedAt");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractCreatedAt"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractCompletionTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contractCompletionTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("callbackURL");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CallbackURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("initialContractFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "InitialContractFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isCompleted");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IsCompleted"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerSupportEmailId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CustomerSupportEmailId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractCompletionTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractCompletionTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("finalSignedContractFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FinalSignedContractFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("finalSignedContractURL");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FinalSignedContractURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("auditCertificate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AuditCertificate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("auditCertificateURL");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AuditCertificateURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("emailAcceptance");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EmailAcceptance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mergedUserConsentURL");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MergedUserConsentURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mergedUserSignedConsentURL");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MergedUserSignedConsentURL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerDetail");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerDetail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>eSignContractResponse>Response>SignerDetail"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>eSignContractResponse>Response>Result"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Error"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>eSignContractResponse>Response>Error"));
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

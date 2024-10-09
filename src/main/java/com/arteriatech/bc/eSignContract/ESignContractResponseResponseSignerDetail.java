/**
 * ESignContractResponseResponseSignerDetail.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContract;

public class ESignContractResponseResponseSignerDetail  implements java.io.Serializable {
    private java.lang.String signerName;

    private java.lang.String signerEmail;

    private java.lang.String signerOtpMobile;

    private java.lang.String signOrder;

    private java.lang.String nameMatchScore;

    private java.lang.String yobMatchScore;

    private java.lang.String signaturePosition;

    private java.lang.String signatureStatus;

    private java.lang.String contractSendTime;

    private java.lang.String esignUrl;

    private java.lang.String contractUrl;

    private java.lang.String contractSignTime;

    private com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetailResult result;

    public ESignContractResponseResponseSignerDetail() {
    }

    public ESignContractResponseResponseSignerDetail(
           java.lang.String signerName,
           java.lang.String signerEmail,
           java.lang.String signerOtpMobile,
           java.lang.String signOrder,
           java.lang.String nameMatchScore,
           java.lang.String yobMatchScore,
           java.lang.String signaturePosition,
           java.lang.String signatureStatus,
           java.lang.String contractSendTime,
           java.lang.String esignUrl,
           java.lang.String contractUrl,
           java.lang.String contractSignTime,
           com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetailResult result) {
           this.signerName = signerName;
           this.signerEmail = signerEmail;
           this.signerOtpMobile = signerOtpMobile;
           this.signOrder = signOrder;
           this.nameMatchScore = nameMatchScore;
           this.yobMatchScore = yobMatchScore;
           this.signaturePosition = signaturePosition;
           this.signatureStatus = signatureStatus;
           this.contractSendTime = contractSendTime;
           this.esignUrl = esignUrl;
           this.contractUrl = contractUrl;
           this.contractSignTime = contractSignTime;
           this.result = result;
    }


    /**
     * Gets the signerName value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signerName
     */
    public java.lang.String getSignerName() {
        return signerName;
    }


    /**
     * Sets the signerName value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signerName
     */
    public void setSignerName(java.lang.String signerName) {
        this.signerName = signerName;
    }


    /**
     * Gets the signerEmail value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signerEmail
     */
    public java.lang.String getSignerEmail() {
        return signerEmail;
    }


    /**
     * Sets the signerEmail value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signerEmail
     */
    public void setSignerEmail(java.lang.String signerEmail) {
        this.signerEmail = signerEmail;
    }


    /**
     * Gets the signerOtpMobile value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signerOtpMobile
     */
    public java.lang.String getSignerOtpMobile() {
        return signerOtpMobile;
    }


    /**
     * Sets the signerOtpMobile value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signerOtpMobile
     */
    public void setSignerOtpMobile(java.lang.String signerOtpMobile) {
        this.signerOtpMobile = signerOtpMobile;
    }


    /**
     * Gets the signOrder value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signOrder
     */
    public java.lang.String getSignOrder() {
        return signOrder;
    }


    /**
     * Sets the signOrder value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signOrder
     */
    public void setSignOrder(java.lang.String signOrder) {
        this.signOrder = signOrder;
    }


    /**
     * Gets the nameMatchScore value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return nameMatchScore
     */
    public java.lang.String getNameMatchScore() {
        return nameMatchScore;
    }


    /**
     * Sets the nameMatchScore value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param nameMatchScore
     */
    public void setNameMatchScore(java.lang.String nameMatchScore) {
        this.nameMatchScore = nameMatchScore;
    }


    /**
     * Gets the yobMatchScore value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return yobMatchScore
     */
    public java.lang.String getYobMatchScore() {
        return yobMatchScore;
    }


    /**
     * Sets the yobMatchScore value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param yobMatchScore
     */
    public void setYobMatchScore(java.lang.String yobMatchScore) {
        this.yobMatchScore = yobMatchScore;
    }


    /**
     * Gets the signaturePosition value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signaturePosition
     */
    public java.lang.String getSignaturePosition() {
        return signaturePosition;
    }


    /**
     * Sets the signaturePosition value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signaturePosition
     */
    public void setSignaturePosition(java.lang.String signaturePosition) {
        this.signaturePosition = signaturePosition;
    }


    /**
     * Gets the signatureStatus value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return signatureStatus
     */
    public java.lang.String getSignatureStatus() {
        return signatureStatus;
    }


    /**
     * Sets the signatureStatus value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param signatureStatus
     */
    public void setSignatureStatus(java.lang.String signatureStatus) {
        this.signatureStatus = signatureStatus;
    }


    /**
     * Gets the contractSendTime value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return contractSendTime
     */
    public java.lang.String getContractSendTime() {
        return contractSendTime;
    }


    /**
     * Sets the contractSendTime value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param contractSendTime
     */
    public void setContractSendTime(java.lang.String contractSendTime) {
        this.contractSendTime = contractSendTime;
    }


    /**
     * Gets the esignUrl value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return esignUrl
     */
    public java.lang.String getEsignUrl() {
        return esignUrl;
    }


    /**
     * Sets the esignUrl value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param esignUrl
     */
    public void setEsignUrl(java.lang.String esignUrl) {
        this.esignUrl = esignUrl;
    }


    /**
     * Gets the contractUrl value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return contractUrl
     */
    public java.lang.String getContractUrl() {
        return contractUrl;
    }


    /**
     * Sets the contractUrl value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param contractUrl
     */
    public void setContractUrl(java.lang.String contractUrl) {
        this.contractUrl = contractUrl;
    }


    /**
     * Gets the contractSignTime value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return contractSignTime
     */
    public java.lang.String getContractSignTime() {
        return contractSignTime;
    }


    /**
     * Sets the contractSignTime value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param contractSignTime
     */
    public void setContractSignTime(java.lang.String contractSignTime) {
        this.contractSignTime = contractSignTime;
    }


    /**
     * Gets the result value for this ESignContractResponseResponseSignerDetail.
     * 
     * @return result
     */
    public com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetailResult getResult() {
        return result;
    }


    /**
     * Sets the result value for this ESignContractResponseResponseSignerDetail.
     * 
     * @param result
     */
    public void setResult(com.arteriatech.bc.eSignContract.ESignContractResponseResponseSignerDetailResult result) {
        this.result = result;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractResponseResponseSignerDetail)) return false;
        ESignContractResponseResponseSignerDetail other = (ESignContractResponseResponseSignerDetail) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.signerName==null && other.getSignerName()==null) || 
             (this.signerName!=null &&
              this.signerName.equals(other.getSignerName()))) &&
            ((this.signerEmail==null && other.getSignerEmail()==null) || 
             (this.signerEmail!=null &&
              this.signerEmail.equals(other.getSignerEmail()))) &&
            ((this.signerOtpMobile==null && other.getSignerOtpMobile()==null) || 
             (this.signerOtpMobile!=null &&
              this.signerOtpMobile.equals(other.getSignerOtpMobile()))) &&
            ((this.signOrder==null && other.getSignOrder()==null) || 
             (this.signOrder!=null &&
              this.signOrder.equals(other.getSignOrder()))) &&
            ((this.nameMatchScore==null && other.getNameMatchScore()==null) || 
             (this.nameMatchScore!=null &&
              this.nameMatchScore.equals(other.getNameMatchScore()))) &&
            ((this.yobMatchScore==null && other.getYobMatchScore()==null) || 
             (this.yobMatchScore!=null &&
              this.yobMatchScore.equals(other.getYobMatchScore()))) &&
            ((this.signaturePosition==null && other.getSignaturePosition()==null) || 
             (this.signaturePosition!=null &&
              this.signaturePosition.equals(other.getSignaturePosition()))) &&
            ((this.signatureStatus==null && other.getSignatureStatus()==null) || 
             (this.signatureStatus!=null &&
              this.signatureStatus.equals(other.getSignatureStatus()))) &&
            ((this.contractSendTime==null && other.getContractSendTime()==null) || 
             (this.contractSendTime!=null &&
              this.contractSendTime.equals(other.getContractSendTime()))) &&
            ((this.esignUrl==null && other.getEsignUrl()==null) || 
             (this.esignUrl!=null &&
              this.esignUrl.equals(other.getEsignUrl()))) &&
            ((this.contractUrl==null && other.getContractUrl()==null) || 
             (this.contractUrl!=null &&
              this.contractUrl.equals(other.getContractUrl()))) &&
            ((this.contractSignTime==null && other.getContractSignTime()==null) || 
             (this.contractSignTime!=null &&
              this.contractSignTime.equals(other.getContractSignTime()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult())));
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
        if (getSignerName() != null) {
            _hashCode += getSignerName().hashCode();
        }
        if (getSignerEmail() != null) {
            _hashCode += getSignerEmail().hashCode();
        }
        if (getSignerOtpMobile() != null) {
            _hashCode += getSignerOtpMobile().hashCode();
        }
        if (getSignOrder() != null) {
            _hashCode += getSignOrder().hashCode();
        }
        if (getNameMatchScore() != null) {
            _hashCode += getNameMatchScore().hashCode();
        }
        if (getYobMatchScore() != null) {
            _hashCode += getYobMatchScore().hashCode();
        }
        if (getSignaturePosition() != null) {
            _hashCode += getSignaturePosition().hashCode();
        }
        if (getSignatureStatus() != null) {
            _hashCode += getSignatureStatus().hashCode();
        }
        if (getContractSendTime() != null) {
            _hashCode += getContractSendTime().hashCode();
        }
        if (getEsignUrl() != null) {
            _hashCode += getEsignUrl().hashCode();
        }
        if (getContractUrl() != null) {
            _hashCode += getContractUrl().hashCode();
        }
        if (getContractSignTime() != null) {
            _hashCode += getContractSignTime().hashCode();
        }
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractResponseResponseSignerDetail.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>eSignContractResponse>Response>SignerDetail"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerEmail");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerEmail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerOtpMobile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "signerOtpMobile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signOrder");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignOrder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nameMatchScore");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nameMatchScore"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("yobMatchScore");
        elemField.setXmlName(new javax.xml.namespace.QName("", "yobMatchScore"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signaturePosition");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignaturePosition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signatureStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignatureStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractSendTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractSendTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("esignUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "esignUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractSignTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractSignTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("", "result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContract", ">>>eSignContractResponse>Response>SignerDetail>result"));
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[signerName=" + signerName + ", signerEmail=" + signerEmail
				+ ", signerOtpMobile=" + signerOtpMobile + ", signOrder=" + signOrder + ", nameMatchScore="
				+ nameMatchScore + ", yobMatchScore=" + yobMatchScore + ", signaturePosition=" + signaturePosition
				+ ", signatureStatus=" + signatureStatus + ", contractSendTime=" + contractSendTime + ", esignUrl="
				+ esignUrl + ", contractUrl=" + contractUrl + ", contractSignTime=" + contractSignTime + ", result="
				+ result + "]";
	}
    
    

}

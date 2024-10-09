/**
 * ESignContractCALPartnership_ResponseResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnership_ResponseResponse  implements java.io.Serializable {
    private java.lang.String customerId;

    private java.lang.String contractId;

    private com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail[] signerDetail;

    public ESignContractCALPartnership_ResponseResponse() {
    }

    public ESignContractCALPartnership_ResponseResponse(
           java.lang.String customerId,
           java.lang.String contractId,
           com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail[] signerDetail) {
           this.customerId = customerId;
           this.contractId = contractId;
           this.signerDetail = signerDetail;
    }


    /**
     * Gets the customerId value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @return customerId
     */
    public java.lang.String getCustomerId() {
        return customerId;
    }


    /**
     * Sets the customerId value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @param customerId
     */
    public void setCustomerId(java.lang.String customerId) {
        this.customerId = customerId;
    }


    /**
     * Gets the contractId value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @return contractId
     */
    public java.lang.String getContractId() {
        return contractId;
    }


    /**
     * Sets the contractId value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @param contractId
     */
    public void setContractId(java.lang.String contractId) {
        this.contractId = contractId;
    }


    /**
     * Gets the signerDetail value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @return signerDetail
     */
    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail[] getSignerDetail() {
        return signerDetail;
    }


    /**
     * Sets the signerDetail value for this ESignContractCALPartnership_ResponseResponse.
     * 
     * @param signerDetail
     */
    public void setSignerDetail(com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail[] signerDetail) {
        this.signerDetail = signerDetail;
    }

    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail getSignerDetail(int i) {
        return this.signerDetail[i];
    }

    public void setSignerDetail(int i, com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponseSignerDetail _value) {
        this.signerDetail[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractCALPartnership_ResponseResponse)) return false;
        ESignContractCALPartnership_ResponseResponse other = (ESignContractCALPartnership_ResponseResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.customerId==null && other.getCustomerId()==null) || 
             (this.customerId!=null &&
              this.customerId.equals(other.getCustomerId()))) &&
            ((this.contractId==null && other.getContractId()==null) || 
             (this.contractId!=null &&
              this.contractId.equals(other.getContractId()))) &&
            ((this.signerDetail==null && other.getSignerDetail()==null) || 
             (this.signerDetail!=null &&
              java.util.Arrays.equals(this.signerDetail, other.getSignerDetail())));
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
        if (getCustomerId() != null) {
            _hashCode += getCustomerId().hashCode();
        }
        if (getContractId() != null) {
            _hashCode += getContractId().hashCode();
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
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractCALPartnership_ResponseResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">eSignContractCALPartnership_Response>Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CustomerId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signerDetail");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SignerDetail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">>eSignContractCALPartnership_Response>Response>SignerDetail"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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

/**
 * DT_DeRegistration_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.DeRegistration;

public class DT_DeRegistration_Request  implements java.io.Serializable {
    private java.lang.String corpId;

    private java.lang.String aggregatorId;

    private java.lang.String userId;

    private java.lang.String URN;

    private java.lang.String aggregatorName;

    public DT_DeRegistration_Request() {
    }

    public DT_DeRegistration_Request(
           java.lang.String corpId,
           java.lang.String aggregatorId,
           java.lang.String userId,
           java.lang.String URN,
           java.lang.String aggregatorName) {
           this.corpId = corpId;
           this.aggregatorId = aggregatorId;
           this.userId = userId;
           this.URN = URN;
           this.aggregatorName = aggregatorName;
    }


    /**
     * Gets the corpId value for this DT_DeRegistration_Request.
     * 
     * @return corpId
     */
    public java.lang.String getCorpId() {
        return corpId;
    }


    /**
     * Sets the corpId value for this DT_DeRegistration_Request.
     * 
     * @param corpId
     */
    public void setCorpId(java.lang.String corpId) {
        this.corpId = corpId;
    }


    /**
     * Gets the aggregatorId value for this DT_DeRegistration_Request.
     * 
     * @return aggregatorId
     */
    public java.lang.String getAggregatorId() {
        return aggregatorId;
    }


    /**
     * Sets the aggregatorId value for this DT_DeRegistration_Request.
     * 
     * @param aggregatorId
     */
    public void setAggregatorId(java.lang.String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }


    /**
     * Gets the userId value for this DT_DeRegistration_Request.
     * 
     * @return userId
     */
    public java.lang.String getUserId() {
        return userId;
    }


    /**
     * Sets the userId value for this DT_DeRegistration_Request.
     * 
     * @param userId
     */
    public void setUserId(java.lang.String userId) {
        this.userId = userId;
    }


    /**
     * Gets the URN value for this DT_DeRegistration_Request.
     * 
     * @return URN
     */
    public java.lang.String getURN() {
        return URN;
    }


    /**
     * Sets the URN value for this DT_DeRegistration_Request.
     * 
     * @param URN
     */
    public void setURN(java.lang.String URN) {
        this.URN = URN;
    }


    /**
     * Gets the aggregatorName value for this DT_DeRegistration_Request.
     * 
     * @return aggregatorName
     */
    public java.lang.String getAggregatorName() {
        return aggregatorName;
    }


    /**
     * Sets the aggregatorName value for this DT_DeRegistration_Request.
     * 
     * @param aggregatorName
     */
    public void setAggregatorName(java.lang.String aggregatorName) {
        this.aggregatorName = aggregatorName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DT_DeRegistration_Request)) return false;
        DT_DeRegistration_Request other = (DT_DeRegistration_Request) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.corpId==null && other.getCorpId()==null) || 
             (this.corpId!=null &&
              this.corpId.equals(other.getCorpId()))) &&
            ((this.aggregatorId==null && other.getAggregatorId()==null) || 
             (this.aggregatorId!=null &&
              this.aggregatorId.equals(other.getAggregatorId()))) &&
            ((this.userId==null && other.getUserId()==null) || 
             (this.userId!=null &&
              this.userId.equals(other.getUserId()))) &&
            ((this.URN==null && other.getURN()==null) || 
             (this.URN!=null &&
              this.URN.equals(other.getURN()))) &&
            ((this.aggregatorName==null && other.getAggregatorName()==null) || 
             (this.aggregatorName!=null &&
              this.aggregatorName.equals(other.getAggregatorName())));
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
        if (getCorpId() != null) {
            _hashCode += getCorpId().hashCode();
        }
        if (getAggregatorId() != null) {
            _hashCode += getAggregatorId().hashCode();
        }
        if (getUserId() != null) {
            _hashCode += getUserId().hashCode();
        }
        if (getURN() != null) {
            _hashCode += getURN().hashCode();
        }
        if (getAggregatorName() != null) {
            _hashCode += getAggregatorName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DT_DeRegistration_Request.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/DeRegistration", "DT_DeRegistration_Request"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corpId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorpId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregatorId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AggregatorId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("URN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "URN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregatorName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AggregatorName"));
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

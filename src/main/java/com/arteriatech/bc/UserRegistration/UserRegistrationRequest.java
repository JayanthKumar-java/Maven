/**
 * UserRegistrationRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.UserRegistration;

public class UserRegistrationRequest  implements java.io.Serializable {
    private java.lang.String corporateID;

    private java.lang.String userID;

    private java.lang.String userRegistrationID;

    private java.lang.String aggregatorID;

    private java.lang.String aliasID;

    public UserRegistrationRequest() {
    }

    public UserRegistrationRequest(
           java.lang.String corporateID,
           java.lang.String userID,
           java.lang.String userRegistrationID,
           java.lang.String aggregatorID,
           java.lang.String aliasID) {
           this.corporateID = corporateID;
           this.userID = userID;
           this.userRegistrationID = userRegistrationID;
           this.aggregatorID = aggregatorID;
           this.aliasID = aliasID;
    }


    /**
     * Gets the corporateID value for this UserRegistrationRequest.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this UserRegistrationRequest.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the userID value for this UserRegistrationRequest.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this UserRegistrationRequest.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }


    /**
     * Gets the userRegistrationID value for this UserRegistrationRequest.
     * 
     * @return userRegistrationID
     */
    public java.lang.String getUserRegistrationID() {
        return userRegistrationID;
    }


    /**
     * Sets the userRegistrationID value for this UserRegistrationRequest.
     * 
     * @param userRegistrationID
     */
    public void setUserRegistrationID(java.lang.String userRegistrationID) {
        this.userRegistrationID = userRegistrationID;
    }


    /**
     * Gets the aggregatorID value for this UserRegistrationRequest.
     * 
     * @return aggregatorID
     */
    public java.lang.String getAggregatorID() {
        return aggregatorID;
    }


    /**
     * Sets the aggregatorID value for this UserRegistrationRequest.
     * 
     * @param aggregatorID
     */
    public void setAggregatorID(java.lang.String aggregatorID) {
        this.aggregatorID = aggregatorID;
    }


    /**
     * Gets the aliasID value for this UserRegistrationRequest.
     * 
     * @return aliasID
     */
    public java.lang.String getAliasID() {
        return aliasID;
    }


    /**
     * Sets the aliasID value for this UserRegistrationRequest.
     * 
     * @param aliasID
     */
    public void setAliasID(java.lang.String aliasID) {
        this.aliasID = aliasID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UserRegistrationRequest)) return false;
        UserRegistrationRequest other = (UserRegistrationRequest) obj;
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
            ((this.aggregatorID==null && other.getAggregatorID()==null) || 
             (this.aggregatorID!=null &&
              this.aggregatorID.equals(other.getAggregatorID()))) &&
            ((this.aliasID==null && other.getAliasID()==null) || 
             (this.aliasID!=null &&
              this.aliasID.equals(other.getAliasID())));
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
        if (getAggregatorID() != null) {
            _hashCode += getAggregatorID().hashCode();
        }
        if (getAliasID() != null) {
            _hashCode += getAliasID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UserRegistrationRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/UserRegistration", "UserRegistrationRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corporateID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorporateID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userRegistrationID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "UserRegistrationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregatorID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AggregatorID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aliasID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AliasID"));
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

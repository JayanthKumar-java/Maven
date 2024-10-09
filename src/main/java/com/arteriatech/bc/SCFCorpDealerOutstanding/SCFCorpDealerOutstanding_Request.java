/**
 * SCFCorpDealerOutstanding_Request.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

 package com.arteriatech.bc.SCFCorpDealerOutstanding;

 public class SCFCorpDealerOutstanding_Request  implements java.io.Serializable {
     private java.lang.String parentLimitPrefixId;
 
     public SCFCorpDealerOutstanding_Request() {
     }
 
     public SCFCorpDealerOutstanding_Request(
            java.lang.String parentLimitPrefixId) {
            this.parentLimitPrefixId = parentLimitPrefixId;
     }
 
 
     /**
      * Gets the parentLimitPrefixId value for this SCFCorpDealerOutstanding_Request.
      * 
      * @return parentLimitPrefixId
      */
     public java.lang.String getParentLimitPrefixId() {
         return parentLimitPrefixId;
     }
 
 
     /**
      * Sets the parentLimitPrefixId value for this SCFCorpDealerOutstanding_Request.
      * 
      * @param parentLimitPrefixId
      */
     public void setParentLimitPrefixId(java.lang.String parentLimitPrefixId) {
         this.parentLimitPrefixId = parentLimitPrefixId;
     }
 
     private java.lang.Object __equalsCalc = null;
     public synchronized boolean equals(java.lang.Object obj) {
         if (!(obj instanceof SCFCorpDealerOutstanding_Request)) return false;
         SCFCorpDealerOutstanding_Request other = (SCFCorpDealerOutstanding_Request) obj;
         if (obj == null) return false;
         if (this == obj) return true;
         if (__equalsCalc != null) {
             return (__equalsCalc == obj);
         }
         __equalsCalc = obj;
         boolean _equals;
         _equals = true && 
             ((this.parentLimitPrefixId==null && other.getParentLimitPrefixId()==null) || 
              (this.parentLimitPrefixId!=null &&
               this.parentLimitPrefixId.equals(other.getParentLimitPrefixId())));
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
         if (getParentLimitPrefixId() != null) {
             _hashCode += getParentLimitPrefixId().hashCode();
         }
         __hashCodeCalc = false;
         return _hashCode;
     }
 
     // Type metadata
     private static org.apache.axis.description.TypeDesc typeDesc =
         new org.apache.axis.description.TypeDesc(SCFCorpDealerOutstanding_Request.class, true);
 
     static {
         typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_Request"));
         org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
         elemField.setFieldName("parentLimitPrefixId");
         elemField.setXmlName(new javax.xml.namespace.QName("", "ParentLimitPrefixId"));
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
 
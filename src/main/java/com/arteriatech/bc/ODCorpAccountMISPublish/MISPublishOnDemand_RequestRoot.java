/**
 * MISPublishOnDemand_RequestRoot.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.ODCorpAccountMISPublish;

public class MISPublishOnDemand_RequestRoot  implements java.io.Serializable {
    private java.lang.String aggregatorID;

    private java.lang.String report;

    private java.lang.String variantID;

    private java.lang.String input;

    public MISPublishOnDemand_RequestRoot() {
    }

    public MISPublishOnDemand_RequestRoot(
           java.lang.String aggregatorID,
           java.lang.String report,
           java.lang.String variantID,
           java.lang.String input) {
           this.aggregatorID = aggregatorID;
           this.report = report;
           this.variantID = variantID;
           this.input = input;
    }


    /**
     * Gets the aggregatorID value for this MISPublishOnDemand_RequestRoot.
     * 
     * @return aggregatorID
     */
    public java.lang.String getAggregatorID() {
        return aggregatorID;
    }


    /**
     * Sets the aggregatorID value for this MISPublishOnDemand_RequestRoot.
     * 
     * @param aggregatorID
     */
    public void setAggregatorID(java.lang.String aggregatorID) {
        this.aggregatorID = aggregatorID;
    }


    /**
     * Gets the report value for this MISPublishOnDemand_RequestRoot.
     * 
     * @return report
     */
    public java.lang.String getReport() {
        return report;
    }


    /**
     * Sets the report value for this MISPublishOnDemand_RequestRoot.
     * 
     * @param report
     */
    public void setReport(java.lang.String report) {
        this.report = report;
    }


    /**
     * Gets the variantID value for this MISPublishOnDemand_RequestRoot.
     * 
     * @return variantID
     */
    public java.lang.String getVariantID() {
        return variantID;
    }


    /**
     * Sets the variantID value for this MISPublishOnDemand_RequestRoot.
     * 
     * @param variantID
     */
    public void setVariantID(java.lang.String variantID) {
        this.variantID = variantID;
    }


    /**
     * Gets the input value for this MISPublishOnDemand_RequestRoot.
     * 
     * @return input
     */
    public java.lang.String getInput() {
        return input;
    }


    /**
     * Sets the input value for this MISPublishOnDemand_RequestRoot.
     * 
     * @param input
     */
    public void setInput(java.lang.String input) {
        this.input = input;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MISPublishOnDemand_RequestRoot)) return false;
        MISPublishOnDemand_RequestRoot other = (MISPublishOnDemand_RequestRoot) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.aggregatorID==null && other.getAggregatorID()==null) || 
             (this.aggregatorID!=null &&
              this.aggregatorID.equals(other.getAggregatorID()))) &&
            ((this.report==null && other.getReport()==null) || 
             (this.report!=null &&
              this.report.equals(other.getReport()))) &&
            ((this.variantID==null && other.getVariantID()==null) || 
             (this.variantID!=null &&
              this.variantID.equals(other.getVariantID()))) &&
            ((this.input==null && other.getInput()==null) || 
             (this.input!=null &&
              this.input.equals(other.getInput())));
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
        if (getAggregatorID() != null) {
            _hashCode += getAggregatorID().hashCode();
        }
        if (getReport() != null) {
            _hashCode += getReport().hashCode();
        }
        if (getVariantID() != null) {
            _hashCode += getVariantID().hashCode();
        }
        if (getInput() != null) {
            _hashCode += getInput().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MISPublishOnDemand_RequestRoot.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/ODCorpAccountMISPublish", ">MISPublishOnDemand_Request>Root"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregatorID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AggregatorID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("report");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Report"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("variantID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "VariantID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("input");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Input"));
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

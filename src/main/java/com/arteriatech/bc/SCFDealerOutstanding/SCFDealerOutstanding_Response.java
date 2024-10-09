/**
 * SCFDealerOutstanding_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFDealerOutstanding;

public class SCFDealerOutstanding_Response  implements java.io.Serializable {
    private com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseHeader header;

    private com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail[] detail;

    private com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseStatus status;

    public SCFDealerOutstanding_Response() {
    }

    public SCFDealerOutstanding_Response(
           com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseHeader header,
           com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail[] detail,
           com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseStatus status) {
           this.header = header;
           this.detail = detail;
           this.status = status;
    }


    /**
     * Gets the header value for this SCFDealerOutstanding_Response.
     * 
     * @return header
     */
    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseHeader getHeader() {
        return header;
    }


    /**
     * Sets the header value for this SCFDealerOutstanding_Response.
     * 
     * @param header
     */
    public void setHeader(com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseHeader header) {
        this.header = header;
    }


    /**
     * Gets the detail value for this SCFDealerOutstanding_Response.
     * 
     * @return detail
     */
    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail[] getDetail() {
        return detail;
    }


    /**
     * Sets the detail value for this SCFDealerOutstanding_Response.
     * 
     * @param detail
     */
    public void setDetail(com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail[] detail) {
        this.detail = detail;
    }

    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail getDetail(int i) {
        return this.detail[i];
    }

    public void setDetail(int i, com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseDetail _value) {
        this.detail[i] = _value;
    }


    /**
     * Gets the status value for this SCFDealerOutstanding_Response.
     * 
     * @return status
     */
    public com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this SCFDealerOutstanding_Response.
     * 
     * @param status
     */
    public void setStatus(com.arteriatech.bc.SCFDealerOutstanding.SCFDealerOutstanding_ResponseStatus status) {
        this.status = status;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFDealerOutstanding_Response)) return false;
        SCFDealerOutstanding_Response other = (SCFDealerOutstanding_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.header==null && other.getHeader()==null) || 
             (this.header!=null &&
              this.header.equals(other.getHeader()))) &&
            ((this.detail==null && other.getDetail()==null) || 
             (this.detail!=null &&
              java.util.Arrays.equals(this.detail, other.getDetail()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus())));
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
        if (getHeader() != null) {
            _hashCode += getHeader().hashCode();
        }
        if (getDetail() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDetail());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDetail(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFDealerOutstanding_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", "SCFDealerOutstanding_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("header");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Header"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", ">SCFDealerOutstanding_Response>Header"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("detail");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Detail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", ">SCFDealerOutstanding_Response>Detail"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFDealerOutstanding", ">SCFDealerOutstanding_Response>Status"));
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

/**
 * ESignContractCALPartnership_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.eSignContractCALPartnership;

public class ESignContractCALPartnership_Response  implements java.io.Serializable {
    private com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponse response;

    private java.lang.String finalTemplate;

    private java.lang.String status;

    private java.lang.String responseCode;

    private java.lang.String message;

    public ESignContractCALPartnership_Response() {
    }

    public ESignContractCALPartnership_Response(
           com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponse response,
           java.lang.String finalTemplate,
           java.lang.String status,
           java.lang.String responseCode,
           java.lang.String message) {
           this.response = response;
           this.finalTemplate = finalTemplate;
           this.status = status;
           this.responseCode = responseCode;
           this.message = message;
    }


    /**
     * Gets the response value for this ESignContractCALPartnership_Response.
     * 
     * @return response
     */
    public com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponse getResponse() {
        return response;
    }


    /**
     * Sets the response value for this ESignContractCALPartnership_Response.
     * 
     * @param response
     */
    public void setResponse(com.arteriatech.bc.eSignContractCALPartnership.ESignContractCALPartnership_ResponseResponse response) {
        this.response = response;
    }


    /**
     * Gets the finalTemplate value for this ESignContractCALPartnership_Response.
     * 
     * @return finalTemplate
     */
    public java.lang.String getFinalTemplate() {
        return finalTemplate;
    }


    /**
     * Sets the finalTemplate value for this ESignContractCALPartnership_Response.
     * 
     * @param finalTemplate
     */
    public void setFinalTemplate(java.lang.String finalTemplate) {
        this.finalTemplate = finalTemplate;
    }


    /**
     * Gets the status value for this ESignContractCALPartnership_Response.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this ESignContractCALPartnership_Response.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the responseCode value for this ESignContractCALPartnership_Response.
     * 
     * @return responseCode
     */
    public java.lang.String getResponseCode() {
        return responseCode;
    }


    /**
     * Sets the responseCode value for this ESignContractCALPartnership_Response.
     * 
     * @param responseCode
     */
    public void setResponseCode(java.lang.String responseCode) {
        this.responseCode = responseCode;
    }


    /**
     * Gets the message value for this ESignContractCALPartnership_Response.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this ESignContractCALPartnership_Response.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ESignContractCALPartnership_Response)) return false;
        ESignContractCALPartnership_Response other = (ESignContractCALPartnership_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.response==null && other.getResponse()==null) || 
             (this.response!=null &&
              this.response.equals(other.getResponse()))) &&
            ((this.finalTemplate==null && other.getFinalTemplate()==null) || 
             (this.finalTemplate!=null &&
              this.finalTemplate.equals(other.getFinalTemplate()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.responseCode==null && other.getResponseCode()==null) || 
             (this.responseCode!=null &&
              this.responseCode.equals(other.getResponseCode()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage())));
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
        if (getResponse() != null) {
            _hashCode += getResponse().hashCode();
        }
        if (getFinalTemplate() != null) {
            _hashCode += getFinalTemplate().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getResponseCode() != null) {
            _hashCode += getResponseCode().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ESignContractCALPartnership_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", "eSignContractCALPartnership_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("response");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Response"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/eSignContractCALPartnership", ">eSignContractCALPartnership_Response>Response"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("finalTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FinalTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responseCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ResponseCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Message"));
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

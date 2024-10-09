/**
 * DT_DeRegistration_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.DeRegistration;

public class DT_DeRegistration_Response  implements java.io.Serializable {
    private java.lang.String status;

    private java.lang.String errorCode;

    private java.lang.String message;

    private java.lang.String URN;

    private java.lang.String AGGRNAME;

    private java.lang.String AGGRID;

    private java.lang.String CORPID;

    private java.lang.String USERID;

    public DT_DeRegistration_Response() {
    }

    public DT_DeRegistration_Response(
           java.lang.String status,
           java.lang.String errorCode,
           java.lang.String message,
           java.lang.String URN,
           java.lang.String AGGRNAME,
           java.lang.String AGGRID,
           java.lang.String CORPID,
           java.lang.String USERID) {
           this.status = status;
           this.errorCode = errorCode;
           this.message = message;
           this.URN = URN;
           this.AGGRNAME = AGGRNAME;
           this.AGGRID = AGGRID;
           this.CORPID = CORPID;
           this.USERID = USERID;
    }


    /**
     * Gets the status value for this DT_DeRegistration_Response.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this DT_DeRegistration_Response.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the errorCode value for this DT_DeRegistration_Response.
     * 
     * @return errorCode
     */
    public java.lang.String getErrorCode() {
        return errorCode;
    }


    /**
     * Sets the errorCode value for this DT_DeRegistration_Response.
     * 
     * @param errorCode
     */
    public void setErrorCode(java.lang.String errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Gets the message value for this DT_DeRegistration_Response.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this DT_DeRegistration_Response.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the URN value for this DT_DeRegistration_Response.
     * 
     * @return URN
     */
    public java.lang.String getURN() {
        return URN;
    }


    /**
     * Sets the URN value for this DT_DeRegistration_Response.
     * 
     * @param URN
     */
    public void setURN(java.lang.String URN) {
        this.URN = URN;
    }


    /**
     * Gets the AGGRNAME value for this DT_DeRegistration_Response.
     * 
     * @return AGGRNAME
     */
    public java.lang.String getAGGRNAME() {
        return AGGRNAME;
    }


    /**
     * Sets the AGGRNAME value for this DT_DeRegistration_Response.
     * 
     * @param AGGRNAME
     */
    public void setAGGRNAME(java.lang.String AGGRNAME) {
        this.AGGRNAME = AGGRNAME;
    }


    /**
     * Gets the AGGRID value for this DT_DeRegistration_Response.
     * 
     * @return AGGRID
     */
    public java.lang.String getAGGRID() {
        return AGGRID;
    }


    /**
     * Sets the AGGRID value for this DT_DeRegistration_Response.
     * 
     * @param AGGRID
     */
    public void setAGGRID(java.lang.String AGGRID) {
        this.AGGRID = AGGRID;
    }


    /**
     * Gets the CORPID value for this DT_DeRegistration_Response.
     * 
     * @return CORPID
     */
    public java.lang.String getCORPID() {
        return CORPID;
    }


    /**
     * Sets the CORPID value for this DT_DeRegistration_Response.
     * 
     * @param CORPID
     */
    public void setCORPID(java.lang.String CORPID) {
        this.CORPID = CORPID;
    }


    /**
     * Gets the USERID value for this DT_DeRegistration_Response.
     * 
     * @return USERID
     */
    public java.lang.String getUSERID() {
        return USERID;
    }


    /**
     * Sets the USERID value for this DT_DeRegistration_Response.
     * 
     * @param USERID
     */
    public void setUSERID(java.lang.String USERID) {
        this.USERID = USERID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DT_DeRegistration_Response)) return false;
        DT_DeRegistration_Response other = (DT_DeRegistration_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.errorCode==null && other.getErrorCode()==null) || 
             (this.errorCode!=null &&
              this.errorCode.equals(other.getErrorCode()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.URN==null && other.getURN()==null) || 
             (this.URN!=null &&
              this.URN.equals(other.getURN()))) &&
            ((this.AGGRNAME==null && other.getAGGRNAME()==null) || 
             (this.AGGRNAME!=null &&
              this.AGGRNAME.equals(other.getAGGRNAME()))) &&
            ((this.AGGRID==null && other.getAGGRID()==null) || 
             (this.AGGRID!=null &&
              this.AGGRID.equals(other.getAGGRID()))) &&
            ((this.CORPID==null && other.getCORPID()==null) || 
             (this.CORPID!=null &&
              this.CORPID.equals(other.getCORPID()))) &&
            ((this.USERID==null && other.getUSERID()==null) || 
             (this.USERID!=null &&
              this.USERID.equals(other.getUSERID())));
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
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getErrorCode() != null) {
            _hashCode += getErrorCode().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getURN() != null) {
            _hashCode += getURN().hashCode();
        }
        if (getAGGRNAME() != null) {
            _hashCode += getAGGRNAME().hashCode();
        }
        if (getAGGRID() != null) {
            _hashCode += getAGGRID().hashCode();
        }
        if (getCORPID() != null) {
            _hashCode += getCORPID().hashCode();
        }
        if (getUSERID() != null) {
            _hashCode += getUSERID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DT_DeRegistration_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/DeRegistration", "DT_DeRegistration_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ErrorCode"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("URN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "URN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("AGGRNAME");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AGGRNAME"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("AGGRID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AGGRID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CORPID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CORPID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("USERID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "USERID"));
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

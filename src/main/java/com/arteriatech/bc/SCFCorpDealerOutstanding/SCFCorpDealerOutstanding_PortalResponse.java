package com.arteriatech.bc.SCFCorpDealerOutstanding;

 public class SCFCorpDealerOutstanding_PortalResponse  implements java.io.Serializable {
     private com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport[] report;
 
     private com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseStatus status;
 
     public SCFCorpDealerOutstanding_PortalResponse() {
     }
 
     public SCFCorpDealerOutstanding_PortalResponse(
            com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport[] report,
            com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseStatus status) {
            this.report = report;
            this.status = status;
     }
 
 
     /**
      * Gets the report value for this SCFCorpDealerOutstanding_PortalResponse.
      * 
      * @return report
      */
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport[] getReport() {
         return report;
     }
 
 
     /**
      * Sets the report value for this SCFCorpDealerOutstanding_PortalResponse.
      * 
      * @param report
      */
     public void setReport(com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport[] report) {
         this.report = report;
     }
 
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport getReport(int i) {
         return this.report[i];
     }
 
     public void setReport(int i, com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseReport _value) {
         this.report[i] = _value;
     }
 
 
     /**
      * Gets the status value for this SCFCorpDealerOutstanding_PortalResponse.
      * 
      * @return status
      */
     public com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseStatus getStatus() {
         return status;
     }
 
 
     /**
      * Sets the status value for this SCFCorpDealerOutstanding_PortalResponse.
      * 
      * @param status
      */
     public void setStatus(com.arteriatech.bc.SCFCorpDealerOutstanding.SCFCorpDealerOutstanding_PortalResponseStatus status) {
         this.status = status;
     }
 
     private java.lang.Object __equalsCalc = null;
     public synchronized boolean equals(java.lang.Object obj) {
         if (!(obj instanceof SCFCorpDealerOutstanding_PortalResponse)) return false;
         SCFCorpDealerOutstanding_PortalResponse other = (SCFCorpDealerOutstanding_PortalResponse) obj;
         if (obj == null) return false;
         if (this == obj) return true;
         if (__equalsCalc != null) {
             return (__equalsCalc == obj);
         }
         __equalsCalc = obj;
         boolean _equals;
         _equals = true && 
             ((this.report==null && other.getReport()==null) || 
              (this.report!=null &&
               java.util.Arrays.equals(this.report, other.getReport()))) &&
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
         if (getReport() != null) {
             for (int i=0;
                  i<java.lang.reflect.Array.getLength(getReport());
                  i++) {
                 java.lang.Object obj = java.lang.reflect.Array.get(getReport(), i);
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
         new org.apache.axis.description.TypeDesc(SCFCorpDealerOutstanding_PortalResponse.class, true);
 
     static {
         typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", "SCFCorpDealerOutstanding_PortalResponse"));
         org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
         elemField.setFieldName("report");
         elemField.setXmlName(new javax.xml.namespace.QName("", "Report"));
         elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", ">SCFCorpDealerOutstanding_PortalResponse>Report"));
         elemField.setMinOccurs(0);
         elemField.setNillable(false);
         elemField.setMaxOccursUnbounded(true);
         typeDesc.addFieldDesc(elemField);
         elemField = new org.apache.axis.description.ElementDesc();
         elemField.setFieldName("status");
         elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
         elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFCorpDealerOutstanding", ">SCFCorpDealerOutstanding_PortalResponse>Status"));
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
 
/**
 * SCFApplyRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFApply;

public class SCFApplyRequest  implements java.io.Serializable {
    private java.lang.String dealerName;

    private java.lang.String dateOfIncorporation;

    private java.lang.String PAN;

    private java.lang.String TIN;

    private java.lang.String CIN;

    private java.lang.String GSTIN;

    private java.lang.String dealerID;

    private java.lang.String corporateID;

    private java.lang.String constitutionType;

    private java.lang.String dealerAddress1;

    private java.lang.String dealerAddress2;

    private java.lang.String dealerAddress3;

    private java.lang.String dealerAddress4;

    private java.lang.String dealerAddress5;

    private java.lang.String dealerCity;

    private java.lang.String dealerState;

    private java.lang.String dealerPincode;

    private com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter[] promoter;

    private java.lang.String isEligible;

    public SCFApplyRequest() {
    }

    public SCFApplyRequest(
           java.lang.String dealerName,
           java.lang.String dateOfIncorporation,
           java.lang.String PAN,
           java.lang.String TIN,
           java.lang.String CIN,
           java.lang.String GSTIN,
           java.lang.String dealerID,
           java.lang.String corporateID,
           java.lang.String constitutionType,
           java.lang.String dealerAddress1,
           java.lang.String dealerAddress2,
           java.lang.String dealerAddress3,
           java.lang.String dealerAddress4,
           java.lang.String dealerAddress5,
           java.lang.String dealerCity,
           java.lang.String dealerState,
           java.lang.String dealerPincode,
           com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter[] promoter,
           java.lang.String isEligible) {
           this.dealerName = dealerName;
           this.dateOfIncorporation = dateOfIncorporation;
           this.PAN = PAN;
           this.TIN = TIN;
           this.CIN = CIN;
           this.GSTIN = GSTIN;
           this.dealerID = dealerID;
           this.corporateID = corporateID;
           this.constitutionType = constitutionType;
           this.dealerAddress1 = dealerAddress1;
           this.dealerAddress2 = dealerAddress2;
           this.dealerAddress3 = dealerAddress3;
           this.dealerAddress4 = dealerAddress4;
           this.dealerAddress5 = dealerAddress5;
           this.dealerCity = dealerCity;
           this.dealerState = dealerState;
           this.dealerPincode = dealerPincode;
           this.promoter = promoter;
           this.isEligible = isEligible;
    }


    /**
     * Gets the dealerName value for this SCFApplyRequest.
     * 
     * @return dealerName
     */
    public java.lang.String getDealerName() {
        return dealerName;
    }


    /**
     * Sets the dealerName value for this SCFApplyRequest.
     * 
     * @param dealerName
     */
    public void setDealerName(java.lang.String dealerName) {
        this.dealerName = dealerName;
    }


    /**
     * Gets the dateOfIncorporation value for this SCFApplyRequest.
     * 
     * @return dateOfIncorporation
     */
    public java.lang.String getDateOfIncorporation() {
        return dateOfIncorporation;
    }


    /**
     * Sets the dateOfIncorporation value for this SCFApplyRequest.
     * 
     * @param dateOfIncorporation
     */
    public void setDateOfIncorporation(java.lang.String dateOfIncorporation) {
        this.dateOfIncorporation = dateOfIncorporation;
    }


    /**
     * Gets the PAN value for this SCFApplyRequest.
     * 
     * @return PAN
     */
    public java.lang.String getPAN() {
        return PAN;
    }


    /**
     * Sets the PAN value for this SCFApplyRequest.
     * 
     * @param PAN
     */
    public void setPAN(java.lang.String PAN) {
        this.PAN = PAN;
    }


    /**
     * Gets the TIN value for this SCFApplyRequest.
     * 
     * @return TIN
     */
    public java.lang.String getTIN() {
        return TIN;
    }


    /**
     * Sets the TIN value for this SCFApplyRequest.
     * 
     * @param TIN
     */
    public void setTIN(java.lang.String TIN) {
        this.TIN = TIN;
    }


    /**
     * Gets the CIN value for this SCFApplyRequest.
     * 
     * @return CIN
     */
    public java.lang.String getCIN() {
        return CIN;
    }


    /**
     * Sets the CIN value for this SCFApplyRequest.
     * 
     * @param CIN
     */
    public void setCIN(java.lang.String CIN) {
        this.CIN = CIN;
    }


    /**
     * Gets the GSTIN value for this SCFApplyRequest.
     * 
     * @return GSTIN
     */
    public java.lang.String getGSTIN() {
        return GSTIN;
    }


    /**
     * Sets the GSTIN value for this SCFApplyRequest.
     * 
     * @param GSTIN
     */
    public void setGSTIN(java.lang.String GSTIN) {
        this.GSTIN = GSTIN;
    }


    /**
     * Gets the dealerID value for this SCFApplyRequest.
     * 
     * @return dealerID
     */
    public java.lang.String getDealerID() {
        return dealerID;
    }


    /**
     * Sets the dealerID value for this SCFApplyRequest.
     * 
     * @param dealerID
     */
    public void setDealerID(java.lang.String dealerID) {
        this.dealerID = dealerID;
    }


    /**
     * Gets the corporateID value for this SCFApplyRequest.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this SCFApplyRequest.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the constitutionType value for this SCFApplyRequest.
     * 
     * @return constitutionType
     */
    public java.lang.String getConstitutionType() {
        return constitutionType;
    }


    /**
     * Sets the constitutionType value for this SCFApplyRequest.
     * 
     * @param constitutionType
     */
    public void setConstitutionType(java.lang.String constitutionType) {
        this.constitutionType = constitutionType;
    }


    /**
     * Gets the dealerAddress1 value for this SCFApplyRequest.
     * 
     * @return dealerAddress1
     */
    public java.lang.String getDealerAddress1() {
        return dealerAddress1;
    }


    /**
     * Sets the dealerAddress1 value for this SCFApplyRequest.
     * 
     * @param dealerAddress1
     */
    public void setDealerAddress1(java.lang.String dealerAddress1) {
        this.dealerAddress1 = dealerAddress1;
    }


    /**
     * Gets the dealerAddress2 value for this SCFApplyRequest.
     * 
     * @return dealerAddress2
     */
    public java.lang.String getDealerAddress2() {
        return dealerAddress2;
    }


    /**
     * Sets the dealerAddress2 value for this SCFApplyRequest.
     * 
     * @param dealerAddress2
     */
    public void setDealerAddress2(java.lang.String dealerAddress2) {
        this.dealerAddress2 = dealerAddress2;
    }


    /**
     * Gets the dealerAddress3 value for this SCFApplyRequest.
     * 
     * @return dealerAddress3
     */
    public java.lang.String getDealerAddress3() {
        return dealerAddress3;
    }


    /**
     * Sets the dealerAddress3 value for this SCFApplyRequest.
     * 
     * @param dealerAddress3
     */
    public void setDealerAddress3(java.lang.String dealerAddress3) {
        this.dealerAddress3 = dealerAddress3;
    }


    /**
     * Gets the dealerAddress4 value for this SCFApplyRequest.
     * 
     * @return dealerAddress4
     */
    public java.lang.String getDealerAddress4() {
        return dealerAddress4;
    }


    /**
     * Sets the dealerAddress4 value for this SCFApplyRequest.
     * 
     * @param dealerAddress4
     */
    public void setDealerAddress4(java.lang.String dealerAddress4) {
        this.dealerAddress4 = dealerAddress4;
    }


    /**
     * Gets the dealerAddress5 value for this SCFApplyRequest.
     * 
     * @return dealerAddress5
     */
    public java.lang.String getDealerAddress5() {
        return dealerAddress5;
    }


    /**
     * Sets the dealerAddress5 value for this SCFApplyRequest.
     * 
     * @param dealerAddress5
     */
    public void setDealerAddress5(java.lang.String dealerAddress5) {
        this.dealerAddress5 = dealerAddress5;
    }


    /**
     * Gets the dealerCity value for this SCFApplyRequest.
     * 
     * @return dealerCity
     */
    public java.lang.String getDealerCity() {
        return dealerCity;
    }


    /**
     * Sets the dealerCity value for this SCFApplyRequest.
     * 
     * @param dealerCity
     */
    public void setDealerCity(java.lang.String dealerCity) {
        this.dealerCity = dealerCity;
    }


    /**
     * Gets the dealerState value for this SCFApplyRequest.
     * 
     * @return dealerState
     */
    public java.lang.String getDealerState() {
        return dealerState;
    }


    /**
     * Sets the dealerState value for this SCFApplyRequest.
     * 
     * @param dealerState
     */
    public void setDealerState(java.lang.String dealerState) {
        this.dealerState = dealerState;
    }


    /**
     * Gets the dealerPincode value for this SCFApplyRequest.
     * 
     * @return dealerPincode
     */
    public java.lang.String getDealerPincode() {
        return dealerPincode;
    }


    /**
     * Sets the dealerPincode value for this SCFApplyRequest.
     * 
     * @param dealerPincode
     */
    public void setDealerPincode(java.lang.String dealerPincode) {
        this.dealerPincode = dealerPincode;
    }


    /**
     * Gets the promoter value for this SCFApplyRequest.
     * 
     * @return promoter
     */
    public com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter[] getPromoter() {
        return promoter;
    }


    /**
     * Sets the promoter value for this SCFApplyRequest.
     * 
     * @param promoter
     */
    public void setPromoter(com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter[] promoter) {
        this.promoter = promoter;
    }

    public com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter getPromoter(int i) {
        return this.promoter[i];
    }

    public void setPromoter(int i, com.arteriatech.bc.SCFApply.SCFApplyRequestPromoter _value) {
        this.promoter[i] = _value;
    }


    /**
     * Gets the isEligible value for this SCFApplyRequest.
     * 
     * @return isEligible
     */
    public java.lang.String getIsEligible() {
        return isEligible;
    }


    /**
     * Sets the isEligible value for this SCFApplyRequest.
     * 
     * @param isEligible
     */
    public void setIsEligible(java.lang.String isEligible) {
        this.isEligible = isEligible;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFApplyRequest)) return false;
        SCFApplyRequest other = (SCFApplyRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dealerName==null && other.getDealerName()==null) || 
             (this.dealerName!=null &&
              this.dealerName.equals(other.getDealerName()))) &&
            ((this.dateOfIncorporation==null && other.getDateOfIncorporation()==null) || 
             (this.dateOfIncorporation!=null &&
              this.dateOfIncorporation.equals(other.getDateOfIncorporation()))) &&
            ((this.PAN==null && other.getPAN()==null) || 
             (this.PAN!=null &&
              this.PAN.equals(other.getPAN()))) &&
            ((this.TIN==null && other.getTIN()==null) || 
             (this.TIN!=null &&
              this.TIN.equals(other.getTIN()))) &&
            ((this.CIN==null && other.getCIN()==null) || 
             (this.CIN!=null &&
              this.CIN.equals(other.getCIN()))) &&
            ((this.GSTIN==null && other.getGSTIN()==null) || 
             (this.GSTIN!=null &&
              this.GSTIN.equals(other.getGSTIN()))) &&
            ((this.dealerID==null && other.getDealerID()==null) || 
             (this.dealerID!=null &&
              this.dealerID.equals(other.getDealerID()))) &&
            ((this.corporateID==null && other.getCorporateID()==null) || 
             (this.corporateID!=null &&
              this.corporateID.equals(other.getCorporateID()))) &&
            ((this.constitutionType==null && other.getConstitutionType()==null) || 
             (this.constitutionType!=null &&
              this.constitutionType.equals(other.getConstitutionType()))) &&
            ((this.dealerAddress1==null && other.getDealerAddress1()==null) || 
             (this.dealerAddress1!=null &&
              this.dealerAddress1.equals(other.getDealerAddress1()))) &&
            ((this.dealerAddress2==null && other.getDealerAddress2()==null) || 
             (this.dealerAddress2!=null &&
              this.dealerAddress2.equals(other.getDealerAddress2()))) &&
            ((this.dealerAddress3==null && other.getDealerAddress3()==null) || 
             (this.dealerAddress3!=null &&
              this.dealerAddress3.equals(other.getDealerAddress3()))) &&
            ((this.dealerAddress4==null && other.getDealerAddress4()==null) || 
             (this.dealerAddress4!=null &&
              this.dealerAddress4.equals(other.getDealerAddress4()))) &&
            ((this.dealerAddress5==null && other.getDealerAddress5()==null) || 
             (this.dealerAddress5!=null &&
              this.dealerAddress5.equals(other.getDealerAddress5()))) &&
            ((this.dealerCity==null && other.getDealerCity()==null) || 
             (this.dealerCity!=null &&
              this.dealerCity.equals(other.getDealerCity()))) &&
            ((this.dealerState==null && other.getDealerState()==null) || 
             (this.dealerState!=null &&
              this.dealerState.equals(other.getDealerState()))) &&
            ((this.dealerPincode==null && other.getDealerPincode()==null) || 
             (this.dealerPincode!=null &&
              this.dealerPincode.equals(other.getDealerPincode()))) &&
            ((this.promoter==null && other.getPromoter()==null) || 
             (this.promoter!=null &&
              java.util.Arrays.equals(this.promoter, other.getPromoter()))) &&
            ((this.isEligible==null && other.getIsEligible()==null) || 
             (this.isEligible!=null &&
              this.isEligible.equals(other.getIsEligible())));
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
        if (getDealerName() != null) {
            _hashCode += getDealerName().hashCode();
        }
        if (getDateOfIncorporation() != null) {
            _hashCode += getDateOfIncorporation().hashCode();
        }
        if (getPAN() != null) {
            _hashCode += getPAN().hashCode();
        }
        if (getTIN() != null) {
            _hashCode += getTIN().hashCode();
        }
        if (getCIN() != null) {
            _hashCode += getCIN().hashCode();
        }
        if (getGSTIN() != null) {
            _hashCode += getGSTIN().hashCode();
        }
        if (getDealerID() != null) {
            _hashCode += getDealerID().hashCode();
        }
        if (getCorporateID() != null) {
            _hashCode += getCorporateID().hashCode();
        }
        if (getConstitutionType() != null) {
            _hashCode += getConstitutionType().hashCode();
        }
        if (getDealerAddress1() != null) {
            _hashCode += getDealerAddress1().hashCode();
        }
        if (getDealerAddress2() != null) {
            _hashCode += getDealerAddress2().hashCode();
        }
        if (getDealerAddress3() != null) {
            _hashCode += getDealerAddress3().hashCode();
        }
        if (getDealerAddress4() != null) {
            _hashCode += getDealerAddress4().hashCode();
        }
        if (getDealerAddress5() != null) {
            _hashCode += getDealerAddress5().hashCode();
        }
        if (getDealerCity() != null) {
            _hashCode += getDealerCity().hashCode();
        }
        if (getDealerState() != null) {
            _hashCode += getDealerState().hashCode();
        }
        if (getDealerPincode() != null) {
            _hashCode += getDealerPincode().hashCode();
        }
        if (getPromoter() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPromoter());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPromoter(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getIsEligible() != null) {
            _hashCode += getIsEligible().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFApplyRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFApply", "SCFApplyRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dateOfIncorporation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DateOfIncorporation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PAN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PAN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("TIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GSTIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "GSTIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corporateID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorporateID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("constitutionType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ConstitutionType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAddress1");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAddress1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAddress2");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAddress2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAddress3");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAddress3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAddress4");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAddress4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAddress5");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAddress5"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerCity");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerCity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerState");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerPincode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerPincode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("promoter");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Promoter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFApply", ">SCFApplyRequest>Promoter"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isEligible");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IsEligible"));
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

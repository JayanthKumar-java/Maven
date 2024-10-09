/**
 * SCFOffer_Response.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.arteriatech.bc.SCFOffer;

public class SCFOffer_Response  implements java.io.Serializable {
    private java.lang.String entityType;

    private java.lang.String corporateID;

    private java.lang.String PAN;

    private java.lang.String TIN;

    private java.lang.String CIN;

    private java.lang.String GSTIN;

    private java.lang.String constitutionType;

    private java.lang.String address1;

    private java.lang.String address2;

    private java.lang.String address3;

    private java.lang.String address4;

    private java.lang.String address5;

    private java.lang.String city;

    private java.lang.String state;

    private java.lang.String pincode;

    private java.lang.String mobileNo;

    private java.lang.String offerAmount;

    private java.lang.String offerCurrency;

    private java.lang.String offerTenure;

    private java.lang.String rate;

    private java.lang.String maxLimitPerCorporate;

    private java.lang.String dealerAssociationWithCorporate;

    private java.lang.String noOfChequeReturns;

    private java.lang.String paymentDelayDays12Months;

    private java.lang.String businessVintageOfDealer;

    private java.lang.String purchasesOf12Months;

    private java.lang.String dealersOverallScoreByCorp;

    private java.lang.String corpRating;

    private java.lang.String salesOf12Months;

    private java.lang.String MCLR6MRate;

    private java.lang.String interestRateSpread;

    private java.lang.String tenorOfPayment;

    private java.lang.String addlnPeriodInterestRateSpread;

    private java.lang.String addlnTenorOfPayment;

    private java.lang.String defaultInterestSpread;

    private java.lang.String processingFee;

    private java.lang.String status;

    private java.lang.String errorCode;

    private java.lang.String message;

    private java.lang.String interestSpread;

    private java.lang.String eligibilityStatus;

    private java.lang.String validTo;

    private java.lang.String processingFeePercent;

    public SCFOffer_Response() {
    }

    public SCFOffer_Response(
           java.lang.String entityType,
           java.lang.String corporateID,
           java.lang.String PAN,
           java.lang.String TIN,
           java.lang.String CIN,
           java.lang.String GSTIN,
           java.lang.String constitutionType,
           java.lang.String address1,
           java.lang.String address2,
           java.lang.String address3,
           java.lang.String address4,
           java.lang.String address5,
           java.lang.String city,
           java.lang.String state,
           java.lang.String pincode,
           java.lang.String mobileNo,
           java.lang.String offerAmount,
           java.lang.String offerCurrency,
           java.lang.String offerTenure,
           java.lang.String rate,
           java.lang.String maxLimitPerCorporate,
           java.lang.String dealerAssociationWithCorporate,
           java.lang.String noOfChequeReturns,
           java.lang.String paymentDelayDays12Months,
           java.lang.String businessVintageOfDealer,
           java.lang.String purchasesOf12Months,
           java.lang.String dealersOverallScoreByCorp,
           java.lang.String corpRating,
           java.lang.String salesOf12Months,
           java.lang.String MCLR6MRate,
           java.lang.String interestRateSpread,
           java.lang.String tenorOfPayment,
           java.lang.String addlnPeriodInterestRateSpread,
           java.lang.String addlnTenorOfPayment,
           java.lang.String defaultInterestSpread,
           java.lang.String processingFee,
           java.lang.String status,
           java.lang.String errorCode,
           java.lang.String message,
           java.lang.String interestSpread,
           java.lang.String eligibilityStatus,
           java.lang.String validTo,
           java.lang.String processingFeePercent) {
           this.entityType = entityType;
           this.corporateID = corporateID;
           this.PAN = PAN;
           this.TIN = TIN;
           this.CIN = CIN;
           this.GSTIN = GSTIN;
           this.constitutionType = constitutionType;
           this.address1 = address1;
           this.address2 = address2;
           this.address3 = address3;
           this.address4 = address4;
           this.address5 = address5;
           this.city = city;
           this.state = state;
           this.pincode = pincode;
           this.mobileNo = mobileNo;
           this.offerAmount = offerAmount;
           this.offerCurrency = offerCurrency;
           this.offerTenure = offerTenure;
           this.rate = rate;
           this.maxLimitPerCorporate = maxLimitPerCorporate;
           this.dealerAssociationWithCorporate = dealerAssociationWithCorporate;
           this.noOfChequeReturns = noOfChequeReturns;
           this.paymentDelayDays12Months = paymentDelayDays12Months;
           this.businessVintageOfDealer = businessVintageOfDealer;
           this.purchasesOf12Months = purchasesOf12Months;
           this.dealersOverallScoreByCorp = dealersOverallScoreByCorp;
           this.corpRating = corpRating;
           this.salesOf12Months = salesOf12Months;
           this.MCLR6MRate = MCLR6MRate;
           this.interestRateSpread = interestRateSpread;
           this.tenorOfPayment = tenorOfPayment;
           this.addlnPeriodInterestRateSpread = addlnPeriodInterestRateSpread;
           this.addlnTenorOfPayment = addlnTenorOfPayment;
           this.defaultInterestSpread = defaultInterestSpread;
           this.processingFee = processingFee;
           this.status = status;
           this.errorCode = errorCode;
           this.message = message;
           this.interestSpread = interestSpread;
           this.eligibilityStatus = eligibilityStatus;
           this.validTo = validTo;
           this.processingFeePercent = processingFeePercent;
    }


    /**
     * Gets the entityType value for this SCFOffer_Response.
     * 
     * @return entityType
     */
    public java.lang.String getEntityType() {
        return entityType;
    }


    /**
     * Sets the entityType value for this SCFOffer_Response.
     * 
     * @param entityType
     */
    public void setEntityType(java.lang.String entityType) {
        this.entityType = entityType;
    }


    /**
     * Gets the corporateID value for this SCFOffer_Response.
     * 
     * @return corporateID
     */
    public java.lang.String getCorporateID() {
        return corporateID;
    }


    /**
     * Sets the corporateID value for this SCFOffer_Response.
     * 
     * @param corporateID
     */
    public void setCorporateID(java.lang.String corporateID) {
        this.corporateID = corporateID;
    }


    /**
     * Gets the PAN value for this SCFOffer_Response.
     * 
     * @return PAN
     */
    public java.lang.String getPAN() {
        return PAN;
    }


    /**
     * Sets the PAN value for this SCFOffer_Response.
     * 
     * @param PAN
     */
    public void setPAN(java.lang.String PAN) {
        this.PAN = PAN;
    }


    /**
     * Gets the TIN value for this SCFOffer_Response.
     * 
     * @return TIN
     */
    public java.lang.String getTIN() {
        return TIN;
    }


    /**
     * Sets the TIN value for this SCFOffer_Response.
     * 
     * @param TIN
     */
    public void setTIN(java.lang.String TIN) {
        this.TIN = TIN;
    }


    /**
     * Gets the CIN value for this SCFOffer_Response.
     * 
     * @return CIN
     */
    public java.lang.String getCIN() {
        return CIN;
    }


    /**
     * Sets the CIN value for this SCFOffer_Response.
     * 
     * @param CIN
     */
    public void setCIN(java.lang.String CIN) {
        this.CIN = CIN;
    }


    /**
     * Gets the GSTIN value for this SCFOffer_Response.
     * 
     * @return GSTIN
     */
    public java.lang.String getGSTIN() {
        return GSTIN;
    }


    /**
     * Sets the GSTIN value for this SCFOffer_Response.
     * 
     * @param GSTIN
     */
    public void setGSTIN(java.lang.String GSTIN) {
        this.GSTIN = GSTIN;
    }


    /**
     * Gets the constitutionType value for this SCFOffer_Response.
     * 
     * @return constitutionType
     */
    public java.lang.String getConstitutionType() {
        return constitutionType;
    }


    /**
     * Sets the constitutionType value for this SCFOffer_Response.
     * 
     * @param constitutionType
     */
    public void setConstitutionType(java.lang.String constitutionType) {
        this.constitutionType = constitutionType;
    }


    /**
     * Gets the address1 value for this SCFOffer_Response.
     * 
     * @return address1
     */
    public java.lang.String getAddress1() {
        return address1;
    }


    /**
     * Sets the address1 value for this SCFOffer_Response.
     * 
     * @param address1
     */
    public void setAddress1(java.lang.String address1) {
        this.address1 = address1;
    }


    /**
     * Gets the address2 value for this SCFOffer_Response.
     * 
     * @return address2
     */
    public java.lang.String getAddress2() {
        return address2;
    }


    /**
     * Sets the address2 value for this SCFOffer_Response.
     * 
     * @param address2
     */
    public void setAddress2(java.lang.String address2) {
        this.address2 = address2;
    }


    /**
     * Gets the address3 value for this SCFOffer_Response.
     * 
     * @return address3
     */
    public java.lang.String getAddress3() {
        return address3;
    }


    /**
     * Sets the address3 value for this SCFOffer_Response.
     * 
     * @param address3
     */
    public void setAddress3(java.lang.String address3) {
        this.address3 = address3;
    }


    /**
     * Gets the address4 value for this SCFOffer_Response.
     * 
     * @return address4
     */
    public java.lang.String getAddress4() {
        return address4;
    }


    /**
     * Sets the address4 value for this SCFOffer_Response.
     * 
     * @param address4
     */
    public void setAddress4(java.lang.String address4) {
        this.address4 = address4;
    }


    /**
     * Gets the address5 value for this SCFOffer_Response.
     * 
     * @return address5
     */
    public java.lang.String getAddress5() {
        return address5;
    }


    /**
     * Sets the address5 value for this SCFOffer_Response.
     * 
     * @param address5
     */
    public void setAddress5(java.lang.String address5) {
        this.address5 = address5;
    }


    /**
     * Gets the city value for this SCFOffer_Response.
     * 
     * @return city
     */
    public java.lang.String getCity() {
        return city;
    }


    /**
     * Sets the city value for this SCFOffer_Response.
     * 
     * @param city
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }


    /**
     * Gets the state value for this SCFOffer_Response.
     * 
     * @return state
     */
    public java.lang.String getState() {
        return state;
    }


    /**
     * Sets the state value for this SCFOffer_Response.
     * 
     * @param state
     */
    public void setState(java.lang.String state) {
        this.state = state;
    }


    /**
     * Gets the pincode value for this SCFOffer_Response.
     * 
     * @return pincode
     */
    public java.lang.String getPincode() {
        return pincode;
    }


    /**
     * Sets the pincode value for this SCFOffer_Response.
     * 
     * @param pincode
     */
    public void setPincode(java.lang.String pincode) {
        this.pincode = pincode;
    }


    /**
     * Gets the mobileNo value for this SCFOffer_Response.
     * 
     * @return mobileNo
     */
    public java.lang.String getMobileNo() {
        return mobileNo;
    }


    /**
     * Sets the mobileNo value for this SCFOffer_Response.
     * 
     * @param mobileNo
     */
    public void setMobileNo(java.lang.String mobileNo) {
        this.mobileNo = mobileNo;
    }


    /**
     * Gets the offerAmount value for this SCFOffer_Response.
     * 
     * @return offerAmount
     */
    public java.lang.String getOfferAmount() {
        return offerAmount;
    }


    /**
     * Sets the offerAmount value for this SCFOffer_Response.
     * 
     * @param offerAmount
     */
    public void setOfferAmount(java.lang.String offerAmount) {
        this.offerAmount = offerAmount;
    }


    /**
     * Gets the offerCurrency value for this SCFOffer_Response.
     * 
     * @return offerCurrency
     */
    public java.lang.String getOfferCurrency() {
        return offerCurrency;
    }


    /**
     * Sets the offerCurrency value for this SCFOffer_Response.
     * 
     * @param offerCurrency
     */
    public void setOfferCurrency(java.lang.String offerCurrency) {
        this.offerCurrency = offerCurrency;
    }


    /**
     * Gets the offerTenure value for this SCFOffer_Response.
     * 
     * @return offerTenure
     */
    public java.lang.String getOfferTenure() {
        return offerTenure;
    }


    /**
     * Sets the offerTenure value for this SCFOffer_Response.
     * 
     * @param offerTenure
     */
    public void setOfferTenure(java.lang.String offerTenure) {
        this.offerTenure = offerTenure;
    }


    /**
     * Gets the rate value for this SCFOffer_Response.
     * 
     * @return rate
     */
    public java.lang.String getRate() {
        return rate;
    }


    /**
     * Sets the rate value for this SCFOffer_Response.
     * 
     * @param rate
     */
    public void setRate(java.lang.String rate) {
        this.rate = rate;
    }


    /**
     * Gets the maxLimitPerCorporate value for this SCFOffer_Response.
     * 
     * @return maxLimitPerCorporate
     */
    public java.lang.String getMaxLimitPerCorporate() {
        return maxLimitPerCorporate;
    }


    /**
     * Sets the maxLimitPerCorporate value for this SCFOffer_Response.
     * 
     * @param maxLimitPerCorporate
     */
    public void setMaxLimitPerCorporate(java.lang.String maxLimitPerCorporate) {
        this.maxLimitPerCorporate = maxLimitPerCorporate;
    }


    /**
     * Gets the dealerAssociationWithCorporate value for this SCFOffer_Response.
     * 
     * @return dealerAssociationWithCorporate
     */
    public java.lang.String getDealerAssociationWithCorporate() {
        return dealerAssociationWithCorporate;
    }


    /**
     * Sets the dealerAssociationWithCorporate value for this SCFOffer_Response.
     * 
     * @param dealerAssociationWithCorporate
     */
    public void setDealerAssociationWithCorporate(java.lang.String dealerAssociationWithCorporate) {
        this.dealerAssociationWithCorporate = dealerAssociationWithCorporate;
    }


    /**
     * Gets the noOfChequeReturns value for this SCFOffer_Response.
     * 
     * @return noOfChequeReturns
     */
    public java.lang.String getNoOfChequeReturns() {
        return noOfChequeReturns;
    }


    /**
     * Sets the noOfChequeReturns value for this SCFOffer_Response.
     * 
     * @param noOfChequeReturns
     */
    public void setNoOfChequeReturns(java.lang.String noOfChequeReturns) {
        this.noOfChequeReturns = noOfChequeReturns;
    }


    /**
     * Gets the paymentDelayDays12Months value for this SCFOffer_Response.
     * 
     * @return paymentDelayDays12Months
     */
    public java.lang.String getPaymentDelayDays12Months() {
        return paymentDelayDays12Months;
    }


    /**
     * Sets the paymentDelayDays12Months value for this SCFOffer_Response.
     * 
     * @param paymentDelayDays12Months
     */
    public void setPaymentDelayDays12Months(java.lang.String paymentDelayDays12Months) {
        this.paymentDelayDays12Months = paymentDelayDays12Months;
    }


    /**
     * Gets the businessVintageOfDealer value for this SCFOffer_Response.
     * 
     * @return businessVintageOfDealer
     */
    public java.lang.String getBusinessVintageOfDealer() {
        return businessVintageOfDealer;
    }


    /**
     * Sets the businessVintageOfDealer value for this SCFOffer_Response.
     * 
     * @param businessVintageOfDealer
     */
    public void setBusinessVintageOfDealer(java.lang.String businessVintageOfDealer) {
        this.businessVintageOfDealer = businessVintageOfDealer;
    }


    /**
     * Gets the purchasesOf12Months value for this SCFOffer_Response.
     * 
     * @return purchasesOf12Months
     */
    public java.lang.String getPurchasesOf12Months() {
        return purchasesOf12Months;
    }


    /**
     * Sets the purchasesOf12Months value for this SCFOffer_Response.
     * 
     * @param purchasesOf12Months
     */
    public void setPurchasesOf12Months(java.lang.String purchasesOf12Months) {
        this.purchasesOf12Months = purchasesOf12Months;
    }


    /**
     * Gets the dealersOverallScoreByCorp value for this SCFOffer_Response.
     * 
     * @return dealersOverallScoreByCorp
     */
    public java.lang.String getDealersOverallScoreByCorp() {
        return dealersOverallScoreByCorp;
    }


    /**
     * Sets the dealersOverallScoreByCorp value for this SCFOffer_Response.
     * 
     * @param dealersOverallScoreByCorp
     */
    public void setDealersOverallScoreByCorp(java.lang.String dealersOverallScoreByCorp) {
        this.dealersOverallScoreByCorp = dealersOverallScoreByCorp;
    }


    /**
     * Gets the corpRating value for this SCFOffer_Response.
     * 
     * @return corpRating
     */
    public java.lang.String getCorpRating() {
        return corpRating;
    }


    /**
     * Sets the corpRating value for this SCFOffer_Response.
     * 
     * @param corpRating
     */
    public void setCorpRating(java.lang.String corpRating) {
        this.corpRating = corpRating;
    }


    /**
     * Gets the salesOf12Months value for this SCFOffer_Response.
     * 
     * @return salesOf12Months
     */
    public java.lang.String getSalesOf12Months() {
        return salesOf12Months;
    }


    /**
     * Sets the salesOf12Months value for this SCFOffer_Response.
     * 
     * @param salesOf12Months
     */
    public void setSalesOf12Months(java.lang.String salesOf12Months) {
        this.salesOf12Months = salesOf12Months;
    }


    /**
     * Gets the MCLR6MRate value for this SCFOffer_Response.
     * 
     * @return MCLR6MRate
     */
    public java.lang.String getMCLR6MRate() {
        return MCLR6MRate;
    }


    /**
     * Sets the MCLR6MRate value for this SCFOffer_Response.
     * 
     * @param MCLR6MRate
     */
    public void setMCLR6MRate(java.lang.String MCLR6MRate) {
        this.MCLR6MRate = MCLR6MRate;
    }


    /**
     * Gets the interestRateSpread value for this SCFOffer_Response.
     * 
     * @return interestRateSpread
     */
    public java.lang.String getInterestRateSpread() {
        return interestRateSpread;
    }


    /**
     * Sets the interestRateSpread value for this SCFOffer_Response.
     * 
     * @param interestRateSpread
     */
    public void setInterestRateSpread(java.lang.String interestRateSpread) {
        this.interestRateSpread = interestRateSpread;
    }


    /**
     * Gets the tenorOfPayment value for this SCFOffer_Response.
     * 
     * @return tenorOfPayment
     */
    public java.lang.String getTenorOfPayment() {
        return tenorOfPayment;
    }


    /**
     * Sets the tenorOfPayment value for this SCFOffer_Response.
     * 
     * @param tenorOfPayment
     */
    public void setTenorOfPayment(java.lang.String tenorOfPayment) {
        this.tenorOfPayment = tenorOfPayment;
    }


    /**
     * Gets the addlnPeriodInterestRateSpread value for this SCFOffer_Response.
     * 
     * @return addlnPeriodInterestRateSpread
     */
    public java.lang.String getAddlnPeriodInterestRateSpread() {
        return addlnPeriodInterestRateSpread;
    }


    /**
     * Sets the addlnPeriodInterestRateSpread value for this SCFOffer_Response.
     * 
     * @param addlnPeriodInterestRateSpread
     */
    public void setAddlnPeriodInterestRateSpread(java.lang.String addlnPeriodInterestRateSpread) {
        this.addlnPeriodInterestRateSpread = addlnPeriodInterestRateSpread;
    }


    /**
     * Gets the addlnTenorOfPayment value for this SCFOffer_Response.
     * 
     * @return addlnTenorOfPayment
     */
    public java.lang.String getAddlnTenorOfPayment() {
        return addlnTenorOfPayment;
    }


    /**
     * Sets the addlnTenorOfPayment value for this SCFOffer_Response.
     * 
     * @param addlnTenorOfPayment
     */
    public void setAddlnTenorOfPayment(java.lang.String addlnTenorOfPayment) {
        this.addlnTenorOfPayment = addlnTenorOfPayment;
    }


    /**
     * Gets the defaultInterestSpread value for this SCFOffer_Response.
     * 
     * @return defaultInterestSpread
     */
    public java.lang.String getDefaultInterestSpread() {
        return defaultInterestSpread;
    }


    /**
     * Sets the defaultInterestSpread value for this SCFOffer_Response.
     * 
     * @param defaultInterestSpread
     */
    public void setDefaultInterestSpread(java.lang.String defaultInterestSpread) {
        this.defaultInterestSpread = defaultInterestSpread;
    }


    /**
     * Gets the processingFee value for this SCFOffer_Response.
     * 
     * @return processingFee
     */
    public java.lang.String getProcessingFee() {
        return processingFee;
    }


    /**
     * Sets the processingFee value for this SCFOffer_Response.
     * 
     * @param processingFee
     */
    public void setProcessingFee(java.lang.String processingFee) {
        this.processingFee = processingFee;
    }


    /**
     * Gets the status value for this SCFOffer_Response.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this SCFOffer_Response.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the errorCode value for this SCFOffer_Response.
     * 
     * @return errorCode
     */
    public java.lang.String getErrorCode() {
        return errorCode;
    }


    /**
     * Sets the errorCode value for this SCFOffer_Response.
     * 
     * @param errorCode
     */
    public void setErrorCode(java.lang.String errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Gets the message value for this SCFOffer_Response.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this SCFOffer_Response.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the interestSpread value for this SCFOffer_Response.
     * 
     * @return interestSpread
     */
    public java.lang.String getInterestSpread() {
        return interestSpread;
    }


    /**
     * Sets the interestSpread value for this SCFOffer_Response.
     * 
     * @param interestSpread
     */
    public void setInterestSpread(java.lang.String interestSpread) {
        this.interestSpread = interestSpread;
    }


    /**
     * Gets the eligibilityStatus value for this SCFOffer_Response.
     * 
     * @return eligibilityStatus
     */
    public java.lang.String getEligibilityStatus() {
        return eligibilityStatus;
    }


    /**
     * Sets the eligibilityStatus value for this SCFOffer_Response.
     * 
     * @param eligibilityStatus
     */
    public void setEligibilityStatus(java.lang.String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }


    /**
     * Gets the validTo value for this SCFOffer_Response.
     * 
     * @return validTo
     */
    public java.lang.String getValidTo() {
        return validTo;
    }


    /**
     * Sets the validTo value for this SCFOffer_Response.
     * 
     * @param validTo
     */
    public void setValidTo(java.lang.String validTo) {
        this.validTo = validTo;
    }


    /**
     * Gets the processingFeePercent value for this SCFOffer_Response.
     * 
     * @return processingFeePercent
     */
    public java.lang.String getProcessingFeePercent() {
        return processingFeePercent;
    }


    /**
     * Sets the processingFeePercent value for this SCFOffer_Response.
     * 
     * @param processingFeePercent
     */
    public void setProcessingFeePercent(java.lang.String processingFeePercent) {
        this.processingFeePercent = processingFeePercent;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SCFOffer_Response)) return false;
        SCFOffer_Response other = (SCFOffer_Response) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.entityType==null && other.getEntityType()==null) || 
             (this.entityType!=null &&
              this.entityType.equals(other.getEntityType()))) &&
            ((this.corporateID==null && other.getCorporateID()==null) || 
             (this.corporateID!=null &&
              this.corporateID.equals(other.getCorporateID()))) &&
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
            ((this.constitutionType==null && other.getConstitutionType()==null) || 
             (this.constitutionType!=null &&
              this.constitutionType.equals(other.getConstitutionType()))) &&
            ((this.address1==null && other.getAddress1()==null) || 
             (this.address1!=null &&
              this.address1.equals(other.getAddress1()))) &&
            ((this.address2==null && other.getAddress2()==null) || 
             (this.address2!=null &&
              this.address2.equals(other.getAddress2()))) &&
            ((this.address3==null && other.getAddress3()==null) || 
             (this.address3!=null &&
              this.address3.equals(other.getAddress3()))) &&
            ((this.address4==null && other.getAddress4()==null) || 
             (this.address4!=null &&
              this.address4.equals(other.getAddress4()))) &&
            ((this.address5==null && other.getAddress5()==null) || 
             (this.address5!=null &&
              this.address5.equals(other.getAddress5()))) &&
            ((this.city==null && other.getCity()==null) || 
             (this.city!=null &&
              this.city.equals(other.getCity()))) &&
            ((this.state==null && other.getState()==null) || 
             (this.state!=null &&
              this.state.equals(other.getState()))) &&
            ((this.pincode==null && other.getPincode()==null) || 
             (this.pincode!=null &&
              this.pincode.equals(other.getPincode()))) &&
            ((this.mobileNo==null && other.getMobileNo()==null) || 
             (this.mobileNo!=null &&
              this.mobileNo.equals(other.getMobileNo()))) &&
            ((this.offerAmount==null && other.getOfferAmount()==null) || 
             (this.offerAmount!=null &&
              this.offerAmount.equals(other.getOfferAmount()))) &&
            ((this.offerCurrency==null && other.getOfferCurrency()==null) || 
             (this.offerCurrency!=null &&
              this.offerCurrency.equals(other.getOfferCurrency()))) &&
            ((this.offerTenure==null && other.getOfferTenure()==null) || 
             (this.offerTenure!=null &&
              this.offerTenure.equals(other.getOfferTenure()))) &&
            ((this.rate==null && other.getRate()==null) || 
             (this.rate!=null &&
              this.rate.equals(other.getRate()))) &&
            ((this.maxLimitPerCorporate==null && other.getMaxLimitPerCorporate()==null) || 
             (this.maxLimitPerCorporate!=null &&
              this.maxLimitPerCorporate.equals(other.getMaxLimitPerCorporate()))) &&
            ((this.dealerAssociationWithCorporate==null && other.getDealerAssociationWithCorporate()==null) || 
             (this.dealerAssociationWithCorporate!=null &&
              this.dealerAssociationWithCorporate.equals(other.getDealerAssociationWithCorporate()))) &&
            ((this.noOfChequeReturns==null && other.getNoOfChequeReturns()==null) || 
             (this.noOfChequeReturns!=null &&
              this.noOfChequeReturns.equals(other.getNoOfChequeReturns()))) &&
            ((this.paymentDelayDays12Months==null && other.getPaymentDelayDays12Months()==null) || 
             (this.paymentDelayDays12Months!=null &&
              this.paymentDelayDays12Months.equals(other.getPaymentDelayDays12Months()))) &&
            ((this.businessVintageOfDealer==null && other.getBusinessVintageOfDealer()==null) || 
             (this.businessVintageOfDealer!=null &&
              this.businessVintageOfDealer.equals(other.getBusinessVintageOfDealer()))) &&
            ((this.purchasesOf12Months==null && other.getPurchasesOf12Months()==null) || 
             (this.purchasesOf12Months!=null &&
              this.purchasesOf12Months.equals(other.getPurchasesOf12Months()))) &&
            ((this.dealersOverallScoreByCorp==null && other.getDealersOverallScoreByCorp()==null) || 
             (this.dealersOverallScoreByCorp!=null &&
              this.dealersOverallScoreByCorp.equals(other.getDealersOverallScoreByCorp()))) &&
            ((this.corpRating==null && other.getCorpRating()==null) || 
             (this.corpRating!=null &&
              this.corpRating.equals(other.getCorpRating()))) &&
            ((this.salesOf12Months==null && other.getSalesOf12Months()==null) || 
             (this.salesOf12Months!=null &&
              this.salesOf12Months.equals(other.getSalesOf12Months()))) &&
            ((this.MCLR6MRate==null && other.getMCLR6MRate()==null) || 
             (this.MCLR6MRate!=null &&
              this.MCLR6MRate.equals(other.getMCLR6MRate()))) &&
            ((this.interestRateSpread==null && other.getInterestRateSpread()==null) || 
             (this.interestRateSpread!=null &&
              this.interestRateSpread.equals(other.getInterestRateSpread()))) &&
            ((this.tenorOfPayment==null && other.getTenorOfPayment()==null) || 
             (this.tenorOfPayment!=null &&
              this.tenorOfPayment.equals(other.getTenorOfPayment()))) &&
            ((this.addlnPeriodInterestRateSpread==null && other.getAddlnPeriodInterestRateSpread()==null) || 
             (this.addlnPeriodInterestRateSpread!=null &&
              this.addlnPeriodInterestRateSpread.equals(other.getAddlnPeriodInterestRateSpread()))) &&
            ((this.addlnTenorOfPayment==null && other.getAddlnTenorOfPayment()==null) || 
             (this.addlnTenorOfPayment!=null &&
              this.addlnTenorOfPayment.equals(other.getAddlnTenorOfPayment()))) &&
            ((this.defaultInterestSpread==null && other.getDefaultInterestSpread()==null) || 
             (this.defaultInterestSpread!=null &&
              this.defaultInterestSpread.equals(other.getDefaultInterestSpread()))) &&
            ((this.processingFee==null && other.getProcessingFee()==null) || 
             (this.processingFee!=null &&
              this.processingFee.equals(other.getProcessingFee()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.errorCode==null && other.getErrorCode()==null) || 
             (this.errorCode!=null &&
              this.errorCode.equals(other.getErrorCode()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.interestSpread==null && other.getInterestSpread()==null) || 
             (this.interestSpread!=null &&
              this.interestSpread.equals(other.getInterestSpread()))) &&
            ((this.eligibilityStatus==null && other.getEligibilityStatus()==null) || 
             (this.eligibilityStatus!=null &&
              this.eligibilityStatus.equals(other.getEligibilityStatus()))) &&
            ((this.validTo==null && other.getValidTo()==null) || 
             (this.validTo!=null &&
              this.validTo.equals(other.getValidTo()))) &&
            ((this.processingFeePercent==null && other.getProcessingFeePercent()==null) || 
             (this.processingFeePercent!=null &&
              this.processingFeePercent.equals(other.getProcessingFeePercent())));
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
        if (getEntityType() != null) {
            _hashCode += getEntityType().hashCode();
        }
        if (getCorporateID() != null) {
            _hashCode += getCorporateID().hashCode();
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
        if (getConstitutionType() != null) {
            _hashCode += getConstitutionType().hashCode();
        }
        if (getAddress1() != null) {
            _hashCode += getAddress1().hashCode();
        }
        if (getAddress2() != null) {
            _hashCode += getAddress2().hashCode();
        }
        if (getAddress3() != null) {
            _hashCode += getAddress3().hashCode();
        }
        if (getAddress4() != null) {
            _hashCode += getAddress4().hashCode();
        }
        if (getAddress5() != null) {
            _hashCode += getAddress5().hashCode();
        }
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getState() != null) {
            _hashCode += getState().hashCode();
        }
        if (getPincode() != null) {
            _hashCode += getPincode().hashCode();
        }
        if (getMobileNo() != null) {
            _hashCode += getMobileNo().hashCode();
        }
        if (getOfferAmount() != null) {
            _hashCode += getOfferAmount().hashCode();
        }
        if (getOfferCurrency() != null) {
            _hashCode += getOfferCurrency().hashCode();
        }
        if (getOfferTenure() != null) {
            _hashCode += getOfferTenure().hashCode();
        }
        if (getRate() != null) {
            _hashCode += getRate().hashCode();
        }
        if (getMaxLimitPerCorporate() != null) {
            _hashCode += getMaxLimitPerCorporate().hashCode();
        }
        if (getDealerAssociationWithCorporate() != null) {
            _hashCode += getDealerAssociationWithCorporate().hashCode();
        }
        if (getNoOfChequeReturns() != null) {
            _hashCode += getNoOfChequeReturns().hashCode();
        }
        if (getPaymentDelayDays12Months() != null) {
            _hashCode += getPaymentDelayDays12Months().hashCode();
        }
        if (getBusinessVintageOfDealer() != null) {
            _hashCode += getBusinessVintageOfDealer().hashCode();
        }
        if (getPurchasesOf12Months() != null) {
            _hashCode += getPurchasesOf12Months().hashCode();
        }
        if (getDealersOverallScoreByCorp() != null) {
            _hashCode += getDealersOverallScoreByCorp().hashCode();
        }
        if (getCorpRating() != null) {
            _hashCode += getCorpRating().hashCode();
        }
        if (getSalesOf12Months() != null) {
            _hashCode += getSalesOf12Months().hashCode();
        }
        if (getMCLR6MRate() != null) {
            _hashCode += getMCLR6MRate().hashCode();
        }
        if (getInterestRateSpread() != null) {
            _hashCode += getInterestRateSpread().hashCode();
        }
        if (getTenorOfPayment() != null) {
            _hashCode += getTenorOfPayment().hashCode();
        }
        if (getAddlnPeriodInterestRateSpread() != null) {
            _hashCode += getAddlnPeriodInterestRateSpread().hashCode();
        }
        if (getAddlnTenorOfPayment() != null) {
            _hashCode += getAddlnTenorOfPayment().hashCode();
        }
        if (getDefaultInterestSpread() != null) {
            _hashCode += getDefaultInterestSpread().hashCode();
        }
        if (getProcessingFee() != null) {
            _hashCode += getProcessingFee().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getErrorCode() != null) {
            _hashCode += getErrorCode().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getInterestSpread() != null) {
            _hashCode += getInterestSpread().hashCode();
        }
        if (getEligibilityStatus() != null) {
            _hashCode += getEligibilityStatus().hashCode();
        }
        if (getValidTo() != null) {
            _hashCode += getValidTo().hashCode();
        }
        if (getProcessingFeePercent() != null) {
            _hashCode += getProcessingFeePercent().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SCFOffer_Response.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bc.arteriatech.com/SCFOffer", "SCFOffer_Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EntityType"));
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
        elemField.setFieldName("constitutionType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ConstitutionType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address1");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Address1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address2");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Address2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address3");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Address3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address4");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Address4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address5");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Address5"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("city");
        elemField.setXmlName(new javax.xml.namespace.QName("", "City"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("state");
        elemField.setXmlName(new javax.xml.namespace.QName("", "State"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pincode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Pincode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mobileNo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MobileNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offerAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OfferAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offerCurrency");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OfferCurrency"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offerTenure");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OfferTenure"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Rate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maxLimitPerCorporate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MaxLimitPerCorporate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealerAssociationWithCorporate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealerAssociationWithCorporate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("noOfChequeReturns");
        elemField.setXmlName(new javax.xml.namespace.QName("", "NoOfChequeReturns"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paymentDelayDays12Months");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PaymentDelayDays12Months"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("businessVintageOfDealer");
        elemField.setXmlName(new javax.xml.namespace.QName("", "BusinessVintageOfDealer"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("purchasesOf12Months");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PurchasesOf12Months"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dealersOverallScoreByCorp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DealersOverallScoreByCorp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("corpRating");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CorpRating"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("salesOf12Months");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SalesOf12Months"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MCLR6MRate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MCLR6MRate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("interestRateSpread");
        elemField.setXmlName(new javax.xml.namespace.QName("", "InterestRateSpread"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tenorOfPayment");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TenorOfPayment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addlnPeriodInterestRateSpread");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AddlnPeriodInterestRateSpread"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addlnTenorOfPayment");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AddlnTenorOfPayment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("defaultInterestSpread");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DefaultInterestSpread"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processingFee");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ProcessingFee"));
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
        elemField.setFieldName("interestSpread");
        elemField.setXmlName(new javax.xml.namespace.QName("", "InterestSpread"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eligibilityStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EligibilityStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validTo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ValidTo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processingFeePercent");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ProcessingFeePercent"));
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

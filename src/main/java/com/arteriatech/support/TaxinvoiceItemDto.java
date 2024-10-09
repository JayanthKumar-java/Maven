package com.arteriatech.support;


public class TaxinvoiceItemDto {
	
	private String materialDesc;
	private String materialNo;
	private String batch;
	private String expiryDate;
	private String hsnCode;
	private String invQty;
	private String uom;
	private String itemBasicValue;
	private String discountPer;
	private String discountI;
	private String itemTaxableValue;
	private String itemTaxValue;
	private int slNo;
	
	private String per;
	private String assValue;
	
	private String cgstAmt;
	private String sgstAmt;
	private String netAmt;
	
	
	
	
	
	
	/**
	 * @return the netAmt
	 */
	public String getNetAmt() {
		return netAmt;
	}


	/**
	 * @param netAmt the netAmt to set
	 */
	public void setNetAmt(String netAmt) {
		this.netAmt = netAmt;
	}


	/**
	 * @return the cgstAmt
	 */
	public String getCgstAmt() {
		return cgstAmt;
	}


	/**
	 * @param cgstAmt the cgstAmt to set
	 */
	public void setCgstAmt(String cgstAmt) {
		this.cgstAmt = cgstAmt;
	}


	/**
	 * @return the sgstAmt
	 */
	public String getSgstAmt() {
		return sgstAmt;
	}


	/**
	 * @param sgstAmt the sgstAmt to set
	 */
	public void setSgstAmt(String sgstAmt) {
		this.sgstAmt = sgstAmt;
	}


	/**
	 * @return the assValue
	 */
	public String getAssValue() {
		return assValue;
	}


	/**
	 * @param assValue the assValue to set
	 */
	public void setAssValue(String assValue) {
		this.assValue = assValue;
	}


	/**
	 * @return the slNo
	 */
	public int getSlNo() {
		return slNo;
	}


	/**
	 * @param slNo the slNo to set
	 */
	public void setSlNo(int slNo) {
		this.slNo = slNo;
	}


	public TaxinvoiceItemDto() {
	}
	

	/**
	 * @return the per
	 */
	public String getPer() {
		return per;
	}


	/**
	 * @param per the per to set
	 */
	public void setPer(String per) {
		this.per = per;
	}
	

	/**
	 * @return the itemTaxValue
	 */
	public String getItemTaxValue() {
		return itemTaxValue;
	}


	/**
	 * @param itemTaxValue the itemTaxValue to set
	 */
	public void setItemTaxValue(String itemTaxValue) {
		this.itemTaxValue = itemTaxValue;
	}


	/**
	 * @return the materialDesc
	 */
	public String getMaterialDesc() {
		return materialDesc;
	}


	/**
	 * @param materialDesc the materialDesc to set
	 */
	public void setMaterialDesc(String materialDesc) {
		this.materialDesc = materialDesc;
	}


	/**
	 * @return the materialNo
	 */
	public String getMaterialNo() {
		return materialNo;
	}


	/**
	 * @param materialNo the materialNo to set
	 */
	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}


	/**
	 * @return the batch
	 */
	public String getBatch() {
		return batch;
	}


	/**
	 * @param batch the batch to set
	 */
	public void setBatch(String batch) {
		this.batch = batch;
	}


	/**
	 * @return the expiryDate
	 */
	public String getExpiryDate() {
		return expiryDate;
	}


	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}


	/**
	 * @return the hsnCode
	 */
	public String getHsnCode() {
		return hsnCode;
	}


	/**
	 * @param hsnCode the hsnCode to set
	 */
	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
	}


	/**
	 * @return the invQty
	 */
	public String getInvQty() {
		return invQty;
	}


	/**
	 * @param invQty the invQty to set
	 */
	public void setInvQty(String invQty) {
		this.invQty = invQty;
	}


	/**
	 * @return the uom
	 */
	public String getUom() {
		return uom;
	}


	/**
	 * @param uom the uom to set
	 */
	public void setUom(String uom) {
		this.uom = uom;
	}


	/**
	 * @return the itemBasicValue
	 */
	public String getItemBasicValue() {
		return itemBasicValue;
	}


	/**
	 * @param itemBasicValue the itemBasicValue to set
	 */
	public void setItemBasicValue(String itemBasicValue) {
		this.itemBasicValue = itemBasicValue;
	}


	/**
	 * @return the discountPer
	 */
	public String getDiscountPer() {
		return discountPer;
	}


	/**
	 * @param discountPer the discountPer to set
	 */
	public void setDiscountPer(String discountPer) {
		this.discountPer = discountPer;
	}


	/**
	 * @return the discountI
	 */
	public String getDiscountI() {
		return discountI;
	}


	/**
	 * @param discountI the discountI to set
	 */
	public void setDiscountI(String discountI) {
		this.discountI = discountI;
	}


	/**
	 * @return the itemTaxableValue
	 */
	public String getItemTaxableValue() {
		return itemTaxableValue;
	}


	/**
	 * @param itemTaxableValue the itemTaxableValue to set
	 */
	public void setItemTaxableValue(String itemTaxableValue) {
		this.itemTaxableValue = itemTaxableValue;
	}
}
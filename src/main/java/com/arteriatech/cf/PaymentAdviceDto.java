package com.arteriatech.cf;

import java.math.BigDecimal;
import java.sql.Date;

// this class is used for Mapping paymnet info to paymentAdvice pdf file
public class PaymentAdviceDto implements Comparable<PaymentAdviceDto> {

	private String docNumber;
	private String docType;
	private String docDate;
	private String docDueDate;
	private String grossAmount;
	private String deduction;
	private String netAmount;
	

	public PaymentAdviceDto(String docNumber, String docType, String docDate, String docDueDate, String grossAmount,
			String deduction, String netAmount) {
		this.docNumber = docNumber;
		this.docType = docType;
		this.docDate = docDate;
		this.docDueDate = docDueDate;
		this.grossAmount = grossAmount;
		this.deduction = deduction;
		this.netAmount = netAmount;
	}
	public PaymentAdviceDto() {
	}



	/**
	 * @return the docNumber
	 */
	public String getDocNumber() {
		return docNumber;
	}

	/**
	 * @param docNumber
	 *            the docNumber to set
	 */
	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	/**
	 * @return the docType
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * @param docType
	 *            the docType to set
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}

	/**
	 * @return the docDate
	 */
	public String getDocDate() {
		return docDate;
	}

	/**
	 * @param docDate
	 *            the docDate to set
	 */
	public void setDocDate(String docDate) {
		this.docDate = docDate;
	}

	/**
	 * @return the docDueDate
	 */
	public String getDocDueDate() {
		return docDueDate;
	}

	/**
	 * @param docDueDate
	 *            the docDueDate to set
	 */
	public void setDocDueDate(String docDueDate) {
		this.docDueDate = docDueDate;
	}

	/**
	 * @return the grossAmount
	 */
	public String getGrossAmount() {
		return grossAmount;
	}

	/**
	 * @param grossAmount
	 *            the grossAmount to set
	 */
	public void setGrossAmount(String grossAmount) {
		this.grossAmount = grossAmount;
	}

	/**
	 * @return the deduction
	 */
	public String getDeduction() {
		return deduction;
	}

	/**
	 * @param deduction
	 *            the deduction to set
	 */
	public void setDeduction(String deduction) {
		this.deduction = deduction;
	}

	/**
	 * @return the netAmount
	 */
	public String getNetAmount() {
		return netAmount;
	}

	/**
	 * @param netAmount
	 *            the netAmount to set
	 */
	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}

	@Override
	public int compareTo(PaymentAdviceDto obj) {

		if (this.docType != null && obj.getDocType() != null) {
			return this.docType.compareTo(obj.getDocType());
		}
		return 0;

	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PaymentAdviceDto [docNumber=" + docNumber + ", docType=" + docType + ", docDate=" + docDate
				+ ", docDueDate=" + docDueDate + ", grossAmount=" + grossAmount + ", deduction=" + deduction
				+ ", netAmount=" + netAmount + "]";
	}
	
	
	
	

}

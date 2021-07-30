package com.insta.hms.payments;

import java.math.BigDecimal;

public class PaymentsDTO {


	private String voucherNo;
	private String type;
	private BigDecimal amount = new BigDecimal(0);
	private String taxType;
	private BigDecimal taxAmount = new BigDecimal(0);
	private java.sql.Timestamp paymentDtTime;
	private java.sql.Date date;
	private String counter;
	private String username;
	private int paymentModeId;
	private int cardTypeId;
	private String bank;
	private String referenceNo;
	private String payeeName;
	private String remarks;
	private BigDecimal tdsAmount = new BigDecimal(0);

	private String description;
	private String voucherCategory;
	private String paymentType;

	private String directPayment;

	private BigDecimal roundOff = new BigDecimal(0);

	public String getVoucherNo() { return voucherNo; }
	public void setVoucherNo(String v) { voucherNo = v; }

	public String getType() { return type; }
	public void setType(String v) { type = v; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal v) { amount = v; }

	public String getTaxType() { return taxType; }
	public void setTaxType(String v) { taxType = v; }

	public BigDecimal getTaxAmount() { return taxAmount; }
	public void setTaxAmount(BigDecimal v) { taxAmount = v; }

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }

	public String getUsername() { return username; }
	public void setUsername(String v) { username = v; }

	public int getPaymentModeId() { return paymentModeId; }
	public void setPaymentModeId(int v) { paymentModeId = v; }

	public int getCardTypeId() { return cardTypeId; }
	public void setCardTypeId(int v) { cardTypeId = v; }

	public String getBank() { return bank; }
	public void setBank(String v) { bank = v; }

	public String getReferenceNo() { return referenceNo; }
	public void setReferenceNo(String v) { referenceNo = v; }

	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getRemarks() { return remarks; }
	public void setRemarks(String v) { remarks = v; }

	public BigDecimal getTdsAmount() { return tdsAmount; }
	public void setTdsAmount(BigDecimal v) { tdsAmount = v; }

	public java.sql.Timestamp getPaymentDtTime() { return paymentDtTime; }
	public void setPaymentDtTime(java.sql.Timestamp v) { paymentDtTime = v; }

	public java.sql.Date getDate() { return date; }
	public void setDate(java.sql.Date v) { date = v; }

	public String getDescription() { return description; }
	public void setDescription(String v) { description = v; }

	public String getVoucherCategory() { return voucherCategory; }
	public void setVoucherCategory(String v) { voucherCategory = v; }

	public String getPaymentType() { return paymentType; }
	public void setPaymentType(String v) { paymentType = v; }

	public String getDirectPayment() {
		return directPayment;
	}
	public void setDirectPayment(String directPayment) {
		this.directPayment = directPayment;
	}

	public BigDecimal getRoundOff() { return roundOff; }
	public void setRoundOff(BigDecimal v) { roundOff = v; }

}



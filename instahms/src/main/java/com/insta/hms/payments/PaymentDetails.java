package com.insta.hms.payments;


import java.math.BigDecimal;

public class PaymentDetails {


	private String paymentId;
	private String type;
	private String voucherNo;
	private String counterNo;
	private String description;
	private String category;
	private String userName;
	private String payeeName;
	private String taxType;
	private int paymentModeId;
	private int cardTypeId;
	private String bankName;
	private String refNo;
	private String remarks;
	private String billNo;
	private String chargeHead;
	private String chargeGroup;
	private String mrno;
	private String chargeId;
	private String payType;
	private String payeeId;
	private String patientName;
	private String lastName;
	private String tpaId;

	private String paymentMode;
	private String cardType;
	/**
	 * Some usefull filed for displaying as tootip
	 */

	private String billType;
	private String status;
	private String visitType;


	private BigDecimal doctorAmount = new BigDecimal(0);
	private BigDecimal paidAmount = new BigDecimal(0);
	private BigDecimal amount= new BigDecimal(0);
	private BigDecimal tdsAmount = new BigDecimal(0);
	private BigDecimal taxAmount = new BigDecimal(0);
	private BigDecimal netAmount = new BigDecimal(0);
	private BigDecimal doctorPaidAmount = new BigDecimal(0);
	private BigDecimal refDoctorAmount = new BigDecimal(0);
	private BigDecimal ohPayment = new BigDecimal(0);
	private BigDecimal prescribingDrAmount = new BigDecimal(0);

	private java.sql.Date postedDate;
	private java.sql.Date date;

	private java.sql.Date dueDate;

	private String paymentType;

	// supplier form fields;

	private String poNo;
	private String grnNo;
	private String invoiceType;
	private String accountGroup; //For Tally Export
	private BigDecimal poAmount = new BigDecimal(0);


	public static final String PAYMENT_TYPE_CASH ="C";
	public static final String PAYMENT_TYPE_DOCTOR = "D";
	public static final String PAYMENT_TYPE_PRESCRIBING_DOCTOR = "P";
	public static final String PAYMENT_TYPE_REFERRAL = "R";
	public static final String PAYMENT_TYPE_OTHER_REFERRAL = "F";
	public static final String PAYMENT_TYPE_OT = "O";
	public static final String PAYMENT_TYPE_SUPPLIER = "S";

	public static final String PATIENT_TYPE_IP ="i";
	public static final String PATIENT_TYPE_OP ="o";
	public static final String PATIENT_TYPE_DIAG ="d";

	public static final String PAYMENT_STATUS_ACTIVE = "A";
	public static final String PAYMENT_STATUS_CANCELLED = "X";
	public static final String PAYMENT_STATUS_CLOSED = "C" ;

	public static final String PAYMENT_VOUCHER="P";
	public static final String REVERSAL_PAYMENT_VOUCHER = "R";

	public static final int NON_ELIGIBLE_PAYMENT = 0;

	public static final String INSURANCE_PATIENT="I";
	public static final String NON_INSURANCE_PATIENT="N";

	public String getAccountGroup() {
		return accountGroup;
	}
	public void setAccountGroup(String accountGroup) {
		this.accountGroup = accountGroup;
	}
	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String v) { paymentId = v; }

	public String getType() { return type; }
	public void setType(String v) { type = v; }

	public String getVoucherNo() { return voucherNo; }
	public void setVoucherNo(String v) { voucherNo = v; }

	public String getCounterNo() { return counterNo; }
	public void setCounterNo(String v) { counterNo = v; }

	public String getDescription() { return description; }
	public void setDescription(String v) { description = v; }

	public String getCategory() { return category; }
	public void setCategory(String v) { category = v; }

	public String getUserName() { return userName; }
	public void setUserName(String v) { userName = v; }

	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getTaxType() { return taxType; }
	public void setTaxType(String v) { taxType = v; }

	public int getPaymentModeId() { return paymentModeId; }
	public void setPaymentModeId(int v) { paymentModeId = v; }

	public int getCardTypeId() { return cardTypeId; }
	public void setCardTypeId(int v) { cardTypeId = v; }

	public String getBankName() { return bankName; }
	public void setBankName(String v) { bankName = v; }

	public String getRefNo() { return refNo; }
	public void setRefNo(String v) { refNo = v; }


	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal v) { amount = v; }

	public BigDecimal getTdsAmount() { return tdsAmount; }
	public void setTdsAmount(BigDecimal v) { tdsAmount = v; }

	public BigDecimal getTaxAmount() { return taxAmount; }
	public void setTaxAmount(BigDecimal v) { taxAmount = v; }

	public BigDecimal getDoctorPaidAmount() { return doctorPaidAmount; }
	public void setDoctorPaidAmount(BigDecimal v) { doctorPaidAmount = v; }

	public BigDecimal getRefDoctorAmount() { return refDoctorAmount; }
	public void setRefDoctorAmount(BigDecimal v) { refDoctorAmount = v; }


	public java.sql.Date getPostedDate() { return postedDate; }
	public void setPostedDate(java.sql.Date v) { postedDate = v; }

	public java.sql.Date getDate() { return date; }
	public void setDate(java.sql.Date v) { date = v; }


	public String getPaymentType() { return paymentType; }
	public void setPaymentType(String v) { paymentType = v; }

	public BigDecimal getNetAmount() { return netAmount; }
	public void setNetAmount(BigDecimal v) { netAmount = v; }

	public String getRemarks() { return remarks; }
	public void setRemarks(String v) { remarks = v; }

	public String getBillNo() { return billNo; }
	public void setBillNo(String v) { billNo = v; }

	public String getChargeHead() { return chargeHead; }
	public void setChargeHead(String v) { chargeHead = v; }

	public String getMrno() { return mrno; }
	public void setMrno(String v) { mrno = v; }

	public BigDecimal getDoctorAmount() { return doctorAmount; }
	public void setDoctorAmount(BigDecimal v) { doctorAmount = v; }


	public BigDecimal getPaidAmount() { return paidAmount; }
	public void setPaidAmount(BigDecimal v) { paidAmount = v; }

	public String getChargeId() { return chargeId; }
	public void setChargeId(String v) { chargeId = v; }

	public String getBillType() { return billType; }
	public void setBillType(String v) { billType = v; }

	public String getStatus() { return status; }
	public void setStatus(String v) { status = v; }

	public String getVisitType() { return visitType; }
	public void setVisitType(String v) { visitType = v; }

	public String getPayType() { return payType; }
	public void setPayType(String v) { payType = v; }

	public String getPoNo() { return poNo; }
	public void setPoNo(String v) { poNo = v; }

	public String getGrnNo() { return grnNo; }
	public void setGrnNo(String v) { grnNo = v; }

	public BigDecimal getPoAmount() { return poAmount; }
	public void setPoAmount(BigDecimal v) { poAmount = v; }

	public String getPayeeId() { return payeeId; }
	public void setPayeeId(String v) { payeeId = v; }

	public String getChargeGroup() { return chargeGroup; }
	public void setChargeGroup(String v) { chargeGroup = v; }

	public BigDecimal getOhPayment() { return ohPayment; }
	public void setOhPayment(BigDecimal v) { ohPayment = v; }

	public BigDecimal getPrescribingDrAmount() { return prescribingDrAmount; }
	public void setPrescribingDrAmount(BigDecimal v) { prescribingDrAmount = v; }

	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getTpaId() {
		return tpaId;
	}
	public void setTpaId(String tpaId) {
		this.tpaId = tpaId;
	}
	public String getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}
	public java.sql.Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(java.sql.Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getCardType() { return cardType; }
	public void setCardType(String v) { cardType = v; }

	public String getPaymentMode() { return paymentMode; }
	public void setPaymentMode(String v) { paymentMode = v; }
}

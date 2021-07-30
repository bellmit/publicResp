package com.insta.hms.payments;


import java.math.BigDecimal;

public class PaymentDetailsDTO {

	private String paymentId;
	private String paymentType;
	private String voucherNo;
	private BigDecimal amount = new BigDecimal(0);
	private String description;
	private String category;
	private java.sql.Date postedDate;
	private String username;
	private String payeeName;
	private String chargeId; //for doctor payment charges

	private String grnNo; //for supplier payments
	private String invoiceType; //For supplier payments
	private java.sql.Date invoiceDate;
	private String voucherCategory;

	private int accountHead;
	private int centerId;

	private String packagCharge;
	private String pkgActivityId;
	private String pkgActivityCode;

	private String consignmentStatus;
	private int issueId;

	private String accountGroup; //For Tally Export

	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String v) { paymentId = v; }

	public String getPaymentType() { return paymentType; }
	public void setPaymentType(String v) { paymentType = v; }

	public String getVoucherNo() { return voucherNo; }
	public void setVoucherNo(String v) { voucherNo = v; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal v) { amount = v; }

	public String getDescription() { return description; }
	public void setDescription(String v) { description = v; }

	public String getCategory() { return category; }
	public void setCategory(String v) { category = v; }

	public String getUsername() { return username; }
	public void setUsername(String v) { username = v; }

	public java.sql.Date getPostedDate() { return postedDate; }
	public void setPostedDate(java.sql.Date v) { postedDate = v; }

	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getChargeId() { return chargeId; }
	public void setChargeId(String v) { chargeId = v; }

	public String getGrnNo() { return grnNo; }
	public void setGrnNo(String v) { grnNo = v; }

	public String getVoucherCategory() { return voucherCategory; }
	public void setVoucherCategory(String v) { voucherCategory = v; }

	public int getAccountHead() { return accountHead; }
	public void setAccountHead(int v) { accountHead = v; }

	public String getInvoiceType() {return invoiceType;}
	public void setInvoiceType(String v) {invoiceType = v;}
	public java.sql.Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(java.sql.Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getPackagCharge() {
		return packagCharge;
	}
	public void setPackagCharge(String packagCharge) {
		this.packagCharge = packagCharge;
	}
	public String getPkgActivityId() {
		return pkgActivityId;
	}
	public void setPkgActivityId(String pkgActivityId) {
		this.pkgActivityId = pkgActivityId;
	}
	public String getPkgActivityCode() { return pkgActivityCode; }
	public void setPkgActivityCode(String v) { pkgActivityCode = v; }

	public String getAccountGroup() {
		return accountGroup;
	}
	public void setAccountGroup(String accountGroup) {
		this.accountGroup = accountGroup;
	}

	public String getConsignmentStatus() { return consignmentStatus; }
	public void setConsignmentStatus(String v) { consignmentStatus = v; }

	public int getIssueId() { return issueId; }
	public void setIssueId(int v) { issueId = v; }
	public int getCenterId() {
		return centerId;
	}
	public void setCenterId(int centerId) {
		this.centerId = centerId;
	}

}


package com.insta.hms.core.billing;

import java.math.BigDecimal;

public class BillOrReceipt {
	private String document_type;
	private String bill_no;
	private String receipt_no;
	private BigDecimal amount;
	private String generated_at;
	private String receipt_payment_type;
	private String bill_type;
	private String receipt_type;
	private String bill_payment_status;

	public String getDocument_type() {
		return document_type;
	}
	public void setDocument_type(String document_type) {
		this.document_type = document_type;
	}
	public String getBill_no() {
		return bill_no;
	}
	public void setBill_no(String bill_no) {
		this.bill_no = bill_no;
	}
	public String getReceipt_no() {
		return receipt_no;
	}
	public void setReceipt_no(String receipt_no) {
		this.receipt_no = receipt_no;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getGenerated_at() {
		return generated_at;
	}
	public void setGenerated_at(String generated_at) {
		this.generated_at = generated_at;
	}
	public String getReceipt_payment_type() {
		return receipt_payment_type;
	}
	public void setReceipt_payment_type(String receipt_payment_type) {
		this.receipt_payment_type = receipt_payment_type;
	}
	public String getBill_type() {
		return bill_type;
	}
	public void setBill_type(String bill_type) {
		this.bill_type = bill_type;
	}
	public String getReceipt_type() {
		return receipt_type;
	}
	public void setReceipt_type(String receipt_type) {
		this.receipt_type = receipt_type;
	}
	public String getBill_payment_status() {
		return bill_payment_status;
	}
	public void setBill_payment_status(String bill_payment_status) {
		this.bill_payment_status = bill_payment_status;
	}
}

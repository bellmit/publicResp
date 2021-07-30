/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

/**
 * @author krishnat
 *
 */
public class Bill {
	
	private String visit_id;
	private String bill_no;
	private String bill_status;
	private String payment_status;
	private String mr_no;

	public String getVisit_id() {
		return visit_id;
	}
	public void setVisit_id(String visit_id) {
		this.visit_id = visit_id;
	}
	public String getBill_no() {
		return bill_no;
	}
	public void setBill_no(String bill_no) {
		this.bill_no = bill_no;
	}
	public String getBill_status() {
		return bill_status;
	}
	public void setBill_status(String bill_status) {
		this.bill_status = bill_status;
	}
	public String getPayment_status() {
		return payment_status;
	}
	public void setPayment_status(String payment_status) {
		this.payment_status = payment_status;
	}
	
	public String getMr_no() {
		return mr_no;
	}
	public void setMr_no(String mr_no) {
		this.mr_no = mr_no;
	}
	@Override
	public int hashCode() {
		return this.bill_no.hashCode() * 20; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Bill) {
			return ((Bill) obj).getBill_no().equals(this.bill_no);
		}
		return false;
	}
	
}

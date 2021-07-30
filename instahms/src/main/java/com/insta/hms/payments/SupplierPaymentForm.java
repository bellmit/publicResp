package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;

public class SupplierPaymentForm extends ActionForm{


	private String supplier_id;
	private String _charges;
	private String directPayment;

	private String[] description;
	private String[] amount;

	private String _supplierName;

	private String[] _invoiceType;


	private String[] _accountGroup; // For Tally Export


// charge component

	private String[] _poNo;
	private String[] _invoice_no;
	private String[] _invoice_date;

	private String[] _sdescription;
	private String[] _grnDate;
	private String[] _poAmount;
	private String[] _grnAmount;
	private String[] _paidAmount;
	private String[] _pendingAmount;

	private String[] paymentCheckBox;
	private String[] consignment_status;
	private int[] _issueId;
	private int[] _centerId;

	private String _addCharge;
	private String _deleteCharge;


// Delete charge Component

	private String[] paidCheckBox;
	private String[] _delPoNo;
	private String[] _delGrnNo;
	private String[] _delGrnDate;
	private String[] _delDescription;
	private String[] _delPoAmount;
	private String[] _delpaidAmount;
	private String[] _delPendingAmount;
	private String[] _delPaymentId;
	private String[] _delinvoiceType;
	private String[] _delConsignment_status;
	private int[] _delIssueId;

	public String[] get_invoice_no() { return _invoice_no; }
	public void set_invoice_no(String[] v) { _invoice_no = v; }

	public String[] get_invoice_date() {
		return _invoice_date;
	}
	public void set_invoice_date(String[] _invoice_date) {
		this._invoice_date = _invoice_date;
	}
	public String get_addCharge() { return _addCharge; }
	public void set_addCharge(String v) { _addCharge = v; }

	public String get_deleteCharge() { return _deleteCharge; }
	public void set_deleteCharge(String v) { _deleteCharge = v; }

	public String getSupplier_id() { return supplier_id; }
	public void setSupplier_id(String v) { supplier_id = v; }

	public String get_charges() { return _charges; }
	public void set_charges(String v) { _charges = v; }

	public String getDirectPayment() { return directPayment; }
	public void setDirectPayment(String v) { directPayment = v; }

	public String[] getDescription() { return description; }
	public void setDescription(String[] v) { description = v; }

	public String[] getAmount() { return amount; }
	public void setAmount(String[] v) { amount = v; }

	public String get_supplierName() { return _supplierName; }
	public void set_supplierName(String v) { _supplierName = v; }

	public String[] get_invoiceType() { return _invoiceType; }
	public void set_invoiceType(String[] v) { _invoiceType = v; }

	public String[] get_accountGroup() { return _accountGroup; }
	public void set_accountGroup(String[] v) { _accountGroup = v; }

	public int[] get_issueId() { return _issueId; }
	public void set_issueId(int[] v) { _issueId = v; }


// charge component

	public String[] get_poNo() { return _poNo; }
	public void set_poNo(String[] v) { _poNo = v; }

	public String[] get_sdescription() { return _sdescription; }
	public void set_sdescription(String[] v) { _sdescription = v; }

	public String[] get_grnDate() { return _grnDate; }
	public void set_grnDate(String[] v) { _grnDate = v; }

	public String[] get_poAmount() { return _poAmount; }
	public void set_poAmount(String[] v) { _poAmount = v; }

	public String[] get_grnAmount() { return _grnAmount; }
	public void set_grnAmount(String[] v) { _grnAmount = v; }

	public String[] get_paidAmount() { return _paidAmount; }
	public void set_paidAmount(String[] v) { _paidAmount = v; }

	public String[] get_pendingAmount() { return _pendingAmount; }
	public void set_pendingAmount(String[] v) { _pendingAmount = v; }

	public String[] getPaymentCheckBox() { return paymentCheckBox; }
	public void setPaymentCheckBox(String[] v) { paymentCheckBox = v; }

	public String[] getConsignment_status() { return consignment_status; }
	public void setConsignment_status(String[] v) { consignment_status = v; }



// Delete charge Component

	public String[] getPaidCheckBox() { return paidCheckBox; }
	public void setPaidCheckBox(String[] v) { paidCheckBox = v; }

	public String[] get_delPoNo() { return _delPoNo; }
	public void set_delPoNo(String[] v) { _delPoNo = v; }

	public String[] get_delGrnNo() { return _delGrnNo; }
	public void set_delGrnNo(String[] v) { _delGrnNo = v; }

	public String[] get_delGrnDate() { return _delGrnDate; }
	public void set_delGrnDate(String[] v) { _delGrnDate = v; }

	public String[] get_delDescription() { return _delDescription; }
	public void set_delDescription(String[] v) { _delDescription = v; }

	public String[] get_delPoAmount() { return _delPoAmount; }
	public void set_delPoAmount(String[] v) { _delPoAmount = v; }

	public String[] get_delpaidAmount() { return _delpaidAmount; }
	public void set_delpaidAmount(String[] v) { _delpaidAmount = v; }

	public String[] get_delPendingAmount() { return _delPendingAmount; }
	public void set_delPendingAmount(String[] v) { _delPendingAmount = v; }

	public String[] get_delPaymentId() { return _delPaymentId; }
	public void set_delPaymentId(String[] v) { _delPaymentId = v; }

	public String[] get_delinvoiceType() { return _delinvoiceType; }
	public void set_delinvoiceType(String[] v) { _delinvoiceType = v; }

	public String[] get_delConsignment_status() { return _delConsignment_status; }
	public void set_delConsignment_status(String[] v) { _delConsignment_status = v; }

	public int[] get_delIssueId() { return _delIssueId; }
	public void set_delIssueId(int[] v) { _delIssueId = v; }
	public int[] get_centerId() {
		return _centerId;
	}
	public void set_centerId(int[] id) {
		_centerId = id;
	}

}

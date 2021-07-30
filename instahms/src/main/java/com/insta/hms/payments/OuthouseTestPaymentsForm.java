package com.insta.hms.payments;

import org.apache.struts.action.ActionForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OuthouseTestPaymentsForm extends ActionForm{

static Logger logger = LoggerFactory.getLogger(OuthouseTestPaymentsForm.class);

	private boolean _charges;

	private String mrno;

	private String outhouseName;
	private String outhouse_id;

	private String _noOfCharges;

	private String[] statusCheck;

	private String[] _billNo;
	private String[] _paymentId;
	private String[] _chargeId;
	private String[] _mrNo;
	private String[] _postedDate;
	private String[] _chargeHeadName;
	private String[] _actDescription;
	private String[] _amount;
	private String[] _ohPayment;
	private String[] _centerId;


	/*
	 *field for delete charge items
	 */

	private String _deleteRows;


	private String[] deleteCharge;

	private String[] _delbillNo;
	private String[] _delchargeId;
	private String[] _delPaymentId;
	private String[] _delmrNo;
	private String[] _delpostedDate;
	private String[] _delactDescription;
	private String[] _delahargeHeadName;
	private String[] _delamount;
	private String[] _delOhPayment;

	public String getOuthouse_id() { return outhouse_id; }
	public void setOuthouse_id(String v) { outhouse_id = v; }

	public boolean get_charges() { return _charges; }
	public void set_charges(boolean v) { _charges = v; }

	public String getMrno() { return mrno; }
	public void setMrno(String v) { mrno = v; }

	public String getOuthouseName() { return outhouseName; }
	public void setOuthouseName(String v) { outhouseName = v; }

	public String get_noOfCharges() { return _noOfCharges; }
	public void set_noOfCharges(String v) { _noOfCharges = v; }

	public String[] getStatusCheck() { return statusCheck; }
	public void setStatusCheck(String[] v) { statusCheck = v; }

	public String[] get_billNo() { return _billNo; }
	public void set_billNo(String[] v) { _billNo = v; }

	public String[] get_paymentId() { return _paymentId; }
	public void set_paymentId(String[] v) { _paymentId = v; }

	public String[] get_chargeId() { return _chargeId; }
	public void set_chargeId(String[] v) { _chargeId = v; }

	public String[] get_mrNo() { return _mrNo; }
	public void set_mrNo(String[] v) { _mrNo = v; }

	public String[] get_postedDate() { return _postedDate; }
	public void set_postedDate(String[] v) { _postedDate = v; }

	public String[] get_chargeHeadName() { return _chargeHeadName; }
	public void set_chargeHeadName(String[] v) { _chargeHeadName = v; }

	public String[] get_actDescription() { return _actDescription; }
	public void set_actDescription(String[] v) { _actDescription = v; }

	public String[] get_amount() { return _amount; }
	public void set_amount(String[] v) { _amount = v; }

	public String[] get_ohPayment() { return _ohPayment; }
	public void set_ohPayment(String[] v) { _ohPayment = v; }

	public String get_deleteRows() { return _deleteRows; }
	public void set_deleteRows(String v) { _deleteRows = v; }

	public String[] getDeleteCharge() { return deleteCharge; }
	public void setDeleteCharge(String[] v) { deleteCharge = v; }

	public String[] get_delbillNo() { return _delbillNo; }
	public void set_delbillNo(String[] v) { _delbillNo = v; }

	public String[] get_delchargeId() { return _delchargeId; }
	public void set_delchargeId(String[] v) { _delchargeId = v; }

	public String[] get_delPaymentId() { return _delPaymentId; }
	public void set_delPaymentId(String[] v) { _delPaymentId = v; }

	public String[] get_delmrNo() { return _delmrNo; }
	public void set_delmrNo(String[] v) { _delmrNo = v; }

	public String[] get_delpostedDate() { return _delpostedDate; }
	public void set_delpostedDate(String[] v) { _delpostedDate = v; }

	public String[] get_delactDescription() { return _delactDescription; }
	public void set_delactDescription(String[] v) { _delactDescription = v; }

	public String[] get_delahargeHeadName() { return _delahargeHeadName; }
	public void set_delahargeHeadName(String[] v) { _delahargeHeadName = v; }

	public String[] get_delamount() { return _delamount; }
	public void set_delamount(String[] v) { _delamount = v; }

	public String[] get_delOhPayment() { return _delOhPayment; }
	public void set_delOhPayment(String[] v) { _delOhPayment = v; }
	public String[] get_centerId() {
		return _centerId;
	}
	public void set_centerId(String[] id) {
		_centerId = id;
	}
}

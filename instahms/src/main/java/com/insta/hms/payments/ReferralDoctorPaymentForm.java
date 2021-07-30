package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;


public class ReferralDoctorPaymentForm extends ActionForm{


	private String mrno;
	private String counter;

	private String reference_docto_id;
	private String doctorName;

	private String _noOfCharges;

	private String[] _statusCheck;

	private String[] _billNo;
	private String[] _paymentId;
	private String[] _chargeId;

	private String[] _mrNo;
	private String[] _postedDate;
	private String[] _chargeHeadName;
	private String[] _actDescription;
	private String[] _amount;
	private String[] _paidAmount;
	private String[] _doctorFees;
	private String[] _centerId;



	private boolean refPayments;

	private String[] chargeGroups;

	private String[] _packageCharge; //For Package Charges

	private String _deleteRows;

	private String[] deleteCharge;
	private String[] _delbillNo;
	private String[] _delchargeId;
	private String[] _delPaymentId;
	private String[] _delmrNo;
	private String[] _delpostedDate;
	private String[] _delactDescription;
	private String[] _delchargeHeadName;
	private String[] _delamount;
	private String[] _delpaidAmount;
	private String[] _deldoctorFees;

	private String _chargeHead;
	private String _chargeName;
	private String _activityId;
	private String[] _delpackageCharge;


	public String getMrno() { return mrno; }
	public void setMrno(String v) { mrno = v; }

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }

	public String getReference_docto_id() { return reference_docto_id; }
	public void setReference_docto_id(String v) { reference_docto_id = v; }

	public String getDoctorName() { return doctorName; }
	public void setDoctorName(String v) { doctorName = v; }

	public String get_noOfCharges() { return _noOfCharges; }
	public void set_noOfCharges(String v) { _noOfCharges = v; }

	public String[] get_statusCheck() { return _statusCheck; }
	public void set_statusCheck(String[] v) { _statusCheck = v; }

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

	public String[] get_paidAmount() { return _paidAmount; }
	public void set_paidAmount(String[] v) { _paidAmount = v; }

	public String[] get_doctorFees() { return _doctorFees; }
	public void set_doctorFees(String[] v) { _doctorFees = v; }

	public boolean getRefPayments() { return refPayments; }
	public void setRefPayments(boolean v) { refPayments = v; }

	public String[] getChargeGroups() { return chargeGroups; }
	public void setChargeGroups(String[] v) { chargeGroups = v; }

	public String[] get_packageCharge() { return _packageCharge; }
	public void set_packageCharge(String[] v) { _packageCharge = v; }

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

	public String[] get_delchargeHeadName() { return _delchargeHeadName; }
	public void set_delchargeHeadName(String[] v) { _delchargeHeadName = v; }

	public String[] get_delamount() { return _delamount; }
	public void set_delamount(String[] v) { _delamount = v; }

	public String[] get_delpaidAmount() { return _delpaidAmount; }
	public void set_delpaidAmount(String[] v) { _delpaidAmount = v; }

	public String[] get_deldoctorFees() { return _deldoctorFees; }
	public void set_deldoctorFees(String[] v) { _deldoctorFees = v; }

	public String get_chargeHead() { return _chargeHead; }
	public void set_chargeHead(String v) { _chargeHead = v; }

	public String get_chargeName() { return _chargeName; }
	public void set_chargeName(String v) { _chargeName = v; }

	public String get_activityId() { return _activityId; }
	public void set_activityId(String v) { _activityId = v; }

	public String[] get_delpackageCharge() { return _delpackageCharge; }
	public void set_delpackageCharge(String[] v) { _delpackageCharge = v; }
	public String[] get_centerId() {
		return _centerId;
	}
	public void set_centerId(String[] id) {
		_centerId = id;
	}

}


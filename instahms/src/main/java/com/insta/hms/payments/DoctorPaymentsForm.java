package com.insta.hms.payments;

import org.apache.struts.action.ActionForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorPaymentsForm extends ActionForm{

static Logger logger = LoggerFactory.getLogger(DoctorPaymentsForm.class);

	private String chargeName;
	private String activityId;
	private String chargeHead;

	private String mrno;
	private String tpaId;

	private String counter;


	private String payee_doctor_id;
	private String doctorName;

	private String paymentPageNum;
	private String chargePageNum;
	private String pageSize;

	private String _noOfCharges;

	private String[] statusCheck;

	private String[] _billNo;
	private String[] _paymentId;
	private String[] _actDescription;
	private String[] _chargeId;
	private String[] _mrNo;
	private String[] _postedDate;
	private String[] _chargeHeadName;
	private String[] _amount;
	private String[] _paidAmount;
	private String[] _doctorFees;
	private String[] _packageCharge;
	private String[] _pkgActivityId;
	private String[] _pkgActivityCode;

	private String _deleteRows;
	private String[] _delpkgActivityId;
	private String[] _delpkgActivityCode;
	private String[] deleteCharge;
	private String[] _delbillNo;
	private String[] _delchargeId;
	private String[] _delPaymentId;
	private String[] _delmrNo;
	private String[] _delpostedDate;
	private String[] _delactDescription;
	private String[] _delahargeHeadName;
	private String[] _delamount;
	private String[] _delpaidAmount;
	private String[] _deldoctorFees;
	private String[] _delpackageCharge;

	private String[] chargeGroups;
	private String[] _centerId;
	private String userName;
	private String screen;



	public String getChargeName() { return chargeName; }
	public void setChargeName(String v) { chargeName = v; }

	public String getActivityId() { return activityId; }
	public void setActivityId(String v) { activityId = v; }

	public String getChargeHead() { return chargeHead; }
	public void setChargeHead(String v) { chargeHead = v; }

	public String getMrno() { return mrno; }
	public void setMrno(String v) { mrno = v; }

	public String getTpaId() { return tpaId; }
	public void setTpaId(String v) { tpaId = v; }

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }

	public String getPayee_doctor_id() { return payee_doctor_id; }
	public void setPayee_doctor_id(String v) { payee_doctor_id = v; }

	public String getDoctorName() { return doctorName; }
	public void setDoctorName(String v) { doctorName = v; }

	public String get_noOfCharges() { return _noOfCharges; }
	public void set_noOfCharges(String v) { _noOfCharges = v; }

	public String[] getStatusCheck() { return statusCheck; }
	public void setStatusCheck(String[] v) { statusCheck = v; }

	public String[] get_billNo() { return _billNo; }
	public void set_billNo(String[] v) { _billNo = v; }

	public String[] get_paymentId() { return _paymentId; }
	public void set_paymentId(String[] v) { _paymentId = v; }

	public String[] get_actDescription() { return _actDescription; }
	public void set_actDescription(String[] v) { _actDescription = v; }

	public String[] get_chargeId() { return _chargeId; }
	public void set_chargeId(String[] v) { _chargeId = v; }

	public String[] get_mrNo() { return _mrNo; }
	public void set_mrNo(String[] v) { _mrNo = v; }

	public String[] get_postedDate() { return _postedDate; }
	public void set_postedDate(String[] v) { _postedDate = v; }

	public String[] get_chargeHeadName() { return _chargeHeadName; }
	public void set_chargeHeadName(String[] v) { _chargeHeadName = v; }

	public String[] get_amount() { return _amount; }
	public void set_amount(String[] v) { _amount = v; }

	public String[] get_paidAmount() { return _paidAmount; }
	public void set_paidAmount(String[] v) { _paidAmount = v; }

	public String[] get_doctorFees() { return _doctorFees; }
	public void set_doctorFees(String[] v) { _doctorFees = v; }

	public String[] get_packageCharge() { return _packageCharge; }
	public void set_packageCharge(String[] v) { _packageCharge = v; }

	public String[] get_pkgActivityId() { return _pkgActivityId; }
	public void set_pkgActivityId(String[] v) { _pkgActivityId = v; }

	public String[] get_pkgActivityCode() { return _pkgActivityCode; }
	public void set_pkgActivityCode(String[] v) { _pkgActivityCode = v; }

	public String get_deleteRows() { return _deleteRows; }
	public void set_deleteRows(String v) { _deleteRows = v; }

	public String[] get_delpkgActivityId() { return _delpkgActivityId; }
	public void set_delpkgActivityId(String[] v) { _delpkgActivityId = v; }

	public String[] get_delpkgActivityCode() { return _delpkgActivityCode; }
	public void set_delpkgActivityCode(String[] v) { _delpkgActivityCode = v; }

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

	public String[] get_delpaidAmount() { return _delpaidAmount; }
	public void set_delpaidAmount(String[] v) { _delpaidAmount = v; }

	public String[] get_deldoctorFees() { return _deldoctorFees; }
	public void set_deldoctorFees(String[] v) { _deldoctorFees = v; }

	public String[] get_delpackageCharge() { return _delpackageCharge; }
	public void set_delpackageCharge(String[] v) { _delpackageCharge = v; }

	public String[] getChargeGroups() { return chargeGroups; }
	public void setChargeGroups(String[] v) { chargeGroups = v; }

	public String getUserName() { return userName; }
	public void setUserName(String v) { userName = v; }

	public String getScreen() { return screen; }
	public void setScreen(String v) { screen = v; }

	public String[] getDeleteCharge() { return deleteCharge; }
	public void setDeleteCharge(String[] v) { deleteCharge = v; }
	public String[] get_centerId() {
		return _centerId;
	}
	public void set_centerId(String[] id) {
		_centerId = id;
	}



}

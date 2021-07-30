package com.insta.hms.billing;
import java.sql.Timestamp;

public class BillActivityCharge {

	private String chargeId;
	private String activityCode;
	private String activityId;

	private String paymentChargeHead;
	private String chargeGroup;
	private String actDescriptionId;
	private String doctorId;

	private String activityConducted;
	private Timestamp conductedDateTime;

	//activity codes
	public static final String DIAG_ACTIVITY_CODE = "DIA";

	public BillActivityCharge(String chargeId, String activityCode, String paymentChargeHead,
			String activityId) {
		this.chargeId = chargeId;
		this.activityCode = activityCode;
		this.paymentChargeHead = paymentChargeHead;
		this.activityId = activityId;
	}

	public BillActivityCharge() { }

	public BillActivityCharge(String chargeId, String activityCode, String paymentChargeHead,
			String activityId, String actDescriptionId, String doctorId, String activityConducted,
			Timestamp conductedDateTime) {
		this.chargeId = chargeId;
		this.activityCode = activityCode;
		this.paymentChargeHead = paymentChargeHead;
		this.activityId = activityId;
		this.actDescriptionId = actDescriptionId;
		this.doctorId = doctorId;
		this.activityConducted = activityConducted;
		this.conductedDateTime = conductedDateTime;
	}

	public String getActivityCode() { return activityCode; }
	public void setActivityCode(String v) { activityCode = v; }

	public String getActivityId() { return activityId; }
	public void setActivityId(String v) { activityId = v; }
	public void setActivityId(int v) { activityId = String.valueOf(v); }

	public String getChargeId() {
		return chargeId;
	}
	public void setChargeId(String chargeId) {
		this.chargeId = chargeId;
	}

	public String getActDescriptionId() {
		return actDescriptionId;
	}

	public void setActDescriptionId(String actDescriptionId) {
		this.actDescriptionId = actDescriptionId;
	}

	public String getChargeGroup() {
		return chargeGroup;
	}

	public void setChargeGroup(String chargeGroup) {
		this.chargeGroup = chargeGroup;
	}

	public String getPaymentChargeHead() { return paymentChargeHead; }
	public void setPaymentChargeHead(String v) { paymentChargeHead = v; }

	public String getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}

	public String getActivityConducted() {
		return activityConducted;
	}

	public void setActivityConducted(String activityConducted) {
		this.activityConducted = activityConducted;
	}

	public Timestamp getConductedDateTime() { return conductedDateTime; }
	public void setConductedDateTime(Timestamp v) { conductedDateTime = v; }

}

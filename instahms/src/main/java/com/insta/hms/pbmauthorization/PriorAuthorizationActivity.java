/**
 *
 */
package com.insta.hms.pbmauthorization;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author lakshmi
 *
 */
public class PriorAuthorizationActivity {
	private String activityID;
	private String activityType;
	private String activityCode;
	private BigDecimal quantity;
	private BigDecimal net;
	private BigDecimal list;
	private BigDecimal patientShare;
	private BigDecimal paymentAmount;
	private String activityDenialCode;
	private String denialRemarks;

	private ArrayList<PriorActivityObservation> observations;

	public PriorAuthorizationActivity(){
		observations = new ArrayList<PriorActivityObservation>();
	}
	public ArrayList<PriorActivityObservation> getObservations() {
		return observations;
	}
	public void setObservations(ArrayList<PriorActivityObservation> observations) {
		this.observations = observations;
	}
	public void addObservation(PriorActivityObservation observation){
		observations.add(observation);
	}
	public String getActivityID() {
		return activityID;
	}

	public void setActivityID(String activityID) {
		this.activityID = activityID;
	}

	public String getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getActivityDenialCode() {
		return activityDenialCode;
	}

	public void setActivityDenialCode(String activityDenialCode) {
		this.activityDenialCode = activityDenialCode;
	}

	public BigDecimal getList() {
		return list;
	}

	public void setList(BigDecimal list) {
		this.list = list;
	}

	public BigDecimal getNet() {
		return net;
	}

	public void setNet(BigDecimal net) {
		this.net = net;
	}

	public BigDecimal getPatientShare() {
		return patientShare;
	}

	public void setPatientShare(BigDecimal patientShare) {
		this.patientShare = patientShare;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getDenialRemarks() {
		return denialRemarks;
	}

	public void setDenialRemarks(String denialRemarks) {
		this.denialRemarks = denialRemarks;
	}
}

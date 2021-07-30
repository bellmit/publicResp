package com.insta.hms.core.patient.outpatientlist;

import java.util.Date;
import java.util.List;

public class AdvancedFilterOptions {
	private List<Date> visitRange;
	private List<Date> appointmentRange;
	private List<String> visitStatuses;
	private List<String> billStatuses;
	private List<String> deptIds;		
	private List<String> doctorIds;		
	private List<String> priInsurance;		
	private List<String> secInsurance;		
	private int centerId;
	private String visitType;
	private String deptType;
	private boolean allBillStatuses;
	private boolean allDoctorIds;
	private boolean allPriInsurance;
	private boolean allSecInsurance;
	
	public List<Date> getVisitRange() {
		return visitRange;
	}
	public void setVisitRange(List<Date> visitRange) {
		this.visitRange = visitRange;
	}
	public List<Date> getAppointmentRange() {
		return appointmentRange;
	}
	public void setAppointmentRange(List<Date> appointmentRange) {
		this.appointmentRange = appointmentRange;
	}
	public List<String> getVisitStatuses() {
		return visitStatuses;
	}
	public void setVisitStatuses(List<String> visitStatuses) {
		this.visitStatuses = visitStatuses;
	}
	public List<String> getBillStatuses() {
		return billStatuses;
	}
	public void setBillStatuses(List<String> billStatuses) {
		this.billStatuses = billStatuses;
	}
	public List<String> getDeptIds() {
		return deptIds;
	}
	public void setDeptIds(List<String> deptIds) {
		this.deptIds = deptIds;
	}
	public List<String> getDoctorIds() {
		return doctorIds;
	}
	public void setDoctorIds(List<String> doctorIds) {
		this.doctorIds = doctorIds;
	}
	public List<String> getPriInsurance() {
		return priInsurance;
	}
	public void setPriInsurance(List<String> priInsurance) {
		this.priInsurance = priInsurance;
	}
	public List<String> getSecInsurance() {
		return secInsurance;
	}
	public void setSecInsurance(List<String> secInsurance) {
		this.secInsurance = secInsurance;
	}
	public int getCenterId() {
		return centerId;
	}
	public void setCenterId(int centerId) {
		this.centerId = centerId;
	}
	public String getVisitType() {
		return visitType;
	}
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	public String getDeptType() {
		return deptType;
	}
	public void setDeptType(String deptType) {
		this.deptType = deptType;
	}
	public boolean isAllBillStatuses() {
		return allBillStatuses;
	}
	public void setAllBillStatuses(boolean allBillStatuses) {
		this.allBillStatuses = allBillStatuses;
	}
	public boolean isAllDoctorIds() {
		return allDoctorIds;
	}
	public void setAllDoctorIds(boolean allDoctorIds) {
		this.allDoctorIds = allDoctorIds;
	}
	public boolean isAllPriInsurance() {
		return allPriInsurance;
	}
	public void setAllPriInsurance(boolean allPriInsurance) {
		this.allPriInsurance = allPriInsurance;
	}
	public boolean isAllSecInsurance() {
		return allSecInsurance;
	}
	public void setAllSecInsurance(boolean allSecInsurance) {
		this.allSecInsurance = allSecInsurance;
	}
}

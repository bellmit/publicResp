package com.insta.hms.diagnosticmodule.common;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class TestConducted {
	//this class represents the tests_conducted

	private String mrNo;
	private String visitId;
	private String testId;
	private Timestamp conductedDate;
	private String conductedBy;
	private String issuedBy;
	private String designation;
	private Time conductedTime;
	private String statisFactoryFlag;
	private int prescribedId;
	private String remarks;
	private String userName;
	private String testConducted;
	private String validatedBy;
	private String technician;
	private Date validatedDate;
	private int reportId;
	private String completedBy;

	public String getTechnician() {
		return technician;
	}
	public void setTechnician(String technician) {
		this.technician = technician;
	}
	public String getValidatedBy() {
		return validatedBy;
	}
	public void setValidatedBy(String validatedBy) {
		this.validatedBy = validatedBy;
	}
	public Date getValidatedDate() {
		return validatedDate;
	}
	public void setValidatedDate(Date validatedDate) {
		this.validatedDate = validatedDate;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getConductedBy() {
		return conductedBy;
	}
	public void setConductedBy(String conductedBy) {
		this.conductedBy = conductedBy;
	}
	public Timestamp getConductedDate() {
		return conductedDate;
	}
	public void setConductedDate(Timestamp conductedDate) {
		this.conductedDate = conductedDate;
	}

	public Time getConductedTime() {
		return conductedTime;
	}
	public void setConductedTime(Time conductedTime) {
		this.conductedTime = conductedTime;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getIssuedBy() {
		return issuedBy;
	}
	public void setIssuedBy(String issuedBy) {
		this.issuedBy = issuedBy;
	}
	public String getMrNo() {
		return mrNo;
	}
	public void setMrNo(String mrNo) {
		this.mrNo = mrNo;
	}
	public int getPrescribedId() {
		return prescribedId;
	}
	public void setPrescribedId(int prescribedId) {
		this.prescribedId = prescribedId;
	}
	public String getStatisFactoryFlag() {
		return statisFactoryFlag;
	}
	public void setStatisFactoryFlag(String statisFactoryFlag) {
		this.statisFactoryFlag = statisFactoryFlag;
	}
	public String getTestId() {
		return testId;
	}
	public void setTestId(String testId) {
		this.testId = testId;
	}
	public String getVisitId() {
		return visitId;
	}
	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}
	public String getTestConducted() {
		return testConducted;
	}
	public void setTestConducted(String testConducted) {
		this.testConducted = testConducted;
	}
	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getCompletedBy() {
		return completedBy;
	}
	public void setCompletedBy(String completedBy) {
		this.completedBy = completedBy;
	}
	

}

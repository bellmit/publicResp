package com.insta.hms.diagnosticmodule.common;

import java.sql.Date;
import java.sql.Time;

public class TestPrescribed {
	//this class represents the tests_prescribed table in database
	//(with out unnessary columns)

	private String mrNo;
	private String visitId;
	private String testId;
	private String ddeptId;
	private Date prescriptionDate;
	private Time presTime;
	private String prescriptionDoctor;
	private String testconductedFlag;
	private String cancelledBy;
	private Date cancelDate;
	private String priority;
	private String sampleCollectedFlag;
	private int prescribedId;
	private String userName;
	private String remarks;
	private int reportId;
	private String labno;
	private int outsourceDestPrescId;
	private int currLocationPrescId;

	public int getCurrLocationPrescId() {
		return currLocationPrescId;
	}
	public void setCurrLocationPrescId(int currLocationPrescId) {
		this.currLocationPrescId = currLocationPrescId;
	}
	//not part of table
	private String testName;



	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getTestconductedFlag() {
		return testconductedFlag;
	}
	public void setTestconductedFlag(String testconductedFlag) {
		this.testconductedFlag = testconductedFlag;
	}
	public Date getCancelDate() {
		return cancelDate;
	}
	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}
	public String getCancelledBy() {
		return cancelledBy;
	}
	public void setCancelledBy(String cancelledBy) {
		this.cancelledBy = cancelledBy;
	}
	public String getDdeptId() {
		return ddeptId;
	}
	public void setDdeptId(String ddeptId) {
		this.ddeptId = ddeptId;
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
	public Date getPrescriptionDate() {
		return prescriptionDate;
	}
	public void setPrescriptionDate(Date prescriptionDate) {
		this.prescriptionDate = prescriptionDate;
	}
	public String getPrescriptionDoctor() {
		return prescriptionDoctor;
	}
	public void setPrescriptionDoctor(String prescriptionDoctor) {
		this.prescriptionDoctor = prescriptionDoctor;
	}
	public Time getPresTime() {
		return presTime;
	}
	public void setPresTime(Time presTime) {
		this.presTime = presTime;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getSampleCollectedFlag() {
		return sampleCollectedFlag;
	}
	public void setSampleCollectedFlag(String sampleCollectedFlag) {
		this.sampleCollectedFlag = sampleCollectedFlag;
	}

	public String getTestId() {
		return testId;
	}
	public void setTestId(String testId) {
		this.testId = testId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getVisitId() {
		return visitId;
	}
	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public String getLabno() {
		return labno;
	}
	public void setLabno(String labno) {
		this.labno = labno;
	}
	public int getOutsourceDestPrescId() {
		return outsourceDestPrescId;
	}
	public void setOutsourceDestPrescId(int outsourceDestPrescId) {
		this.outsourceDestPrescId = outsourceDestPrescId;
	}

}

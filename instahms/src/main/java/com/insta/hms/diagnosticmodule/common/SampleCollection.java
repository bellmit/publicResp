package com.insta.hms.diagnosticmodule.common;

import java.sql.Date;
import java.sql.Timestamp;

public class SampleCollection {
	//this class represents the sample_collection table

	private String mrNo;
	private String visitId;
	private String testId;
	private String sampleNo;
	private String ddeptId;
	private int prescribedId;
	private Timestamp sampleDate;
	private Date cancelDate;
	private String userName;
	private int sampleTypeId;
	private String reqAutoGenId;
	private String sampleStatus;
	private int sampleSourceId;
	private String specimenCondition;
	private String sampleType;
	private String sampleSource;
	private int sampleSequence;
	private int sampleQty;
	private int sampleNoCounter;
	private String outSourceId;
	private String sampleTransferStatus;
	private int bedId;
	private String  wardId;



	public int getBedId() {
		return bedId;
	}
	public void setBedId(int bedId) {
		this.bedId = bedId;
	}
	public String getWardId() {
		return wardId;
	}
	public void setWardId(String wardId) {
		this.wardId = wardId;
	}
	public String getoutSourceId() {
		return outSourceId;
	}
	public void setoutSourceId(String outSourceId) {
		this.outSourceId = outSourceId;
	}
	public int getSampleNoCounter() {
		return sampleNoCounter;
	}
	public void setSampleNoCounter(int sampleNoCounter) {
		this.sampleNoCounter = sampleNoCounter;
	}
	public int getSampleQty() {
		return sampleQty;
	}
	public void setSampleQty(int sampleQty) {
		this.sampleQty = sampleQty;
	}
	public int getSampleSequence() {
		return sampleSequence;
	}
	public void setSampleSequence(int sampleSequence) {
		this.sampleSequence = sampleSequence;
	}
	public String getSpecimenCondition() {
		return specimenCondition;
	}
	public void setSpecimenCondition(String specimenCondition) {
		this.specimenCondition = specimenCondition;
	}
	public int getSampleSourceId() {
		return sampleSourceId;
	}
	public void setSampleSourceId(int sampleSourceId) {
		this.sampleSourceId = sampleSourceId;
	}
	public String getSampleStatus() {
		return sampleStatus;
	}
	public void setSampleStatus(String sampleStatus) {
		this.sampleStatus = sampleStatus;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Date getCancelDate() {
		return cancelDate;
	}
	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
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
	public Timestamp getSampleDate() {
		return sampleDate;
	}
	public void setSampleDate(Timestamp sampleDate) {
		this.sampleDate = sampleDate;
	}
	public String getSampleNo() {
		return sampleNo;
	}
	public void setSampleNo(String sampleNo) {
		this.sampleNo = sampleNo;
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
	public int getSampleTypeId() {
		return sampleTypeId;
	}
	public void setSampleTypeId(int sampleTypeId) {
		this.sampleTypeId = sampleTypeId;
	}
	public String getReqAutoGenId() {
		return reqAutoGenId;
	}
	public void setReqAutoGenId(String reqAutoGenId) {
		this.reqAutoGenId = reqAutoGenId;
	}
	public String getSampleSource() {
		return sampleSource;
	}
	public void setSampleSource(String sampleSource) {
		this.sampleSource = sampleSource;
	}
	public String getSampleType() {
		return sampleType;
	}
	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}
	public String getSampleTransferStatus() {
		return sampleTransferStatus;
	}
	public void setSampleTransferStatus(String sampleTransferStatus) {
		this.sampleTransferStatus = sampleTransferStatus;
	}

}

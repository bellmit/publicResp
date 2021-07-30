package com.insta.hms.diagnosticmodule.common;

public class OutHouseSampleDetails {

	private String visitId;
	private String outSourceId;
	private String sampleNo;
	private String testId;
	private int prescribedId;
	private String reqAutoGenId;
	private String sampleStatus;



	public String getSampleStatus() {
		return sampleStatus;
	}
	public void setSampleStatus(String sampleStatus) {
		this.sampleStatus = sampleStatus;
	}
	public String getoutSourceId() {
		return outSourceId;
	}
	public void setoutSourceId(String outSourceId) {
		this.outSourceId = outSourceId;
	}
	public int getPrescribedId() {
		return prescribedId;
	}
	public void setPrescribedId(int prescribedId) {
		this.prescribedId = prescribedId;
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
	public String getReqAutoGenId() {
		return reqAutoGenId;
	}
	public void setReqAutoGenId(String reqAutoGenId) {
		this.reqAutoGenId = reqAutoGenId;
	}





}

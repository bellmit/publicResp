package com.insta.hms.diagnosticmodule.laboratory;

import org.apache.struts.action.ActionForm;

public class TestPrescriptionCancellationForm  extends ActionForm{

	private String presMrno;
	private String patVisitId;
	private String cancelledBy;
	private String remarks[];
	private String category;
	private String toDate;
	private String presId[];
	private String cancelType[];
	private String testName[];
	private String sflag[];
	private String outsourceDestPrescId[];
	private String reportId[];
	private String testId[];
	private String collectionTestPrescribedID;
	private String incomingVisitIds[];
	private String currentLocationPrescId[];



	public String[] getCurrentLocationPrescId() {
		return currentLocationPrescId;
	}
	public void setCurrentLocationPrescId(String[] currentLocationPrescId) {
		this.currentLocationPrescId = currentLocationPrescId;
	}
	public String[] getIncomingVisitIds() {
		return incomingVisitIds;
	}
	public void setIncomingVisitIds(String[] incomingVisitIds) {
		this.incomingVisitIds = incomingVisitIds;
	}
	public String getCollectionTestPrescribedID() {
		return collectionTestPrescribedID;
	}
	public void setCollectionTestPrescribedID(String collectionTestPrescribedID) {
		this.collectionTestPrescribedID = collectionTestPrescribedID;
	}
	public String[] getReportId() {
		return reportId;
	}
	public void setReportId(String[] reportId) {
		this.reportId = reportId;
	}
	public String[] getOutsourceDestPrescId() {
		return outsourceDestPrescId;
	}
	public void setOutsourceDestPrescId(String[] outsourceDestPrescId) {
		this.outsourceDestPrescId = outsourceDestPrescId;
	}
	public String[] getTestName() {
		return testName;
	}
	public void setTestName(String[] testName) {
		this.testName = testName;
	}
	public String[] getCancelType() {
		return cancelType;
	}
	public void setCancelType(String[] cancelType) {
		this.cancelType = cancelType;
	}
	public String[] getPresId() {
		return presId;
	}
	public void setPresId(String[] presId) {
		this.presId = presId;
	}

	public String[] getRemarks() {
		return remarks;
	}
	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
	}
	public String getCancelledBy() {
		return cancelledBy;
	}
	public void setCancelledBy(String cancelledBy) {
		this.cancelledBy = cancelledBy;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public String getPatVisitId() {
		return patVisitId;
	}
	public void setPatVisitId(String patVisitId) {
		this.patVisitId = patVisitId;
	}

	public String getPresMrno() {
		return presMrno;
	}
	public void setPresMrno(String presMrno) {
		this.presMrno = presMrno;
	}

	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public String[] getSflag() {
		return sflag;
	}
	public void setSflag(String[] sflag) {
		this.sflag = sflag;
	}
	public String[] getTestId() {
		return testId;
	}
	public void setTestId(String[] testId) {
		this.testId = testId;
	}

}

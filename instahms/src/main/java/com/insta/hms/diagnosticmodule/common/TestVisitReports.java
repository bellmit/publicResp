package com.insta.hms.diagnosticmodule.common;


public class TestVisitReports {

	private int reportId;
	private String visitId;
	private String reportName;
	private String reportData;
	private String category;
	private String reportMode;
	private String userName;
	private Integer pheader_template_id;
	private int revisedReportId;
	private String reportState;
	private String reportResultsSeverityStatus;


	public String getReportResultsSeverityStatus() {
		return reportResultsSeverityStatus;
	}
	public void setReportResultsSeverityStatus(String reportResultsSeverityStatus) {
		this.reportResultsSeverityStatus = reportResultsSeverityStatus;
	}
	public String getReportState() {
		return reportState;
	}
	public void setReportState(String reportState) {
		this.reportState = reportState;
	}
	public int getRevisedReportId() {
		return revisedReportId;
	}
	public void setRevisedReportId(int revisedReportId) {
		this.revisedReportId = revisedReportId;
	}
	public void setUserName(String userName){this.userName = userName;}
    public String getUserName(){return userName;}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getReportData() {
		return reportData;
	}
	public void setReportData(String reportData) {
		this.reportData = reportData;
	}
	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public String getVisitId() {
		return visitId;
	}
	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	public String getReportMode() { return reportMode; }
	public void setReportMode(String v) { reportMode = v; }
	public Integer getPheader_template_id() {
		return pheader_template_id;
	}
	public void setPheader_template_id(Integer pheader_template_id) {
		this.pheader_template_id = pheader_template_id;
	}

}

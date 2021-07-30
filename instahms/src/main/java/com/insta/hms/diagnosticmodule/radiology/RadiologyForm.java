package com.insta.hms.diagnosticmodule.radiology;

import org.apache.struts.action.ActionForm;

public class RadiologyForm extends ActionForm{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String fromDate;
	private String toDate;
	private String[] filterDepartment;
	private String filterTestname;
	private boolean patientAll;
	private boolean patientIp;
	private boolean patientOp;
	private boolean patientDiag;
	private String mrno;
	private String pageNum;
	private String sortOrder;
	private boolean sortReverse;
	private String startPage;
	private String endPage;

	private String visitId;

	//variables corresponding to RadiologyCancellation
	private String presMrno;
	private String name;
	private String patientAge;
	private String patVisitId;
	private String sex;
	private String location;
	private String department;
	private String referredBy;
	private String testName;
	private String cancelledBy;
	private String presId;
	private String reportId[];
	private String printreportId;



	//varibles corresponding to RadiologyConductionScreen.jsp
	private String testid[];
	private String testname[];
	private String prescribedid[];
	private String completed[];
	private String resultlabel[];
	private String referenceRanges[];
	private String rtestId[];
	private String conductedinreportformat[];
	private String formatid[];
	private String reporttemplate[];
	private String withinNormal[];
	private String units[];
	private String rprescribedId[];
	private String doctor[];
	private String resultvalue[];
	private String remarks[];
	private String dateOfInvestigation[];
	private String dateOfReport[];
	private String sampleno[];//logically a labno
	private String testRemarks[];
	private String saveandprint;
	private String closePrescriptions[];


	public boolean getSortReverse() {
		return sortReverse;
	}
	public void setSortReverse(boolean sortReverse) {
		this.sortReverse = sortReverse;
	}
	public String[] getClosePrescriptions() {
		return closePrescriptions;
	}
	public void setClosePrescriptions(String[] closePrescriptions) {
		this.closePrescriptions = closePrescriptions;
	}
	public String getSaveandprint() {
		return saveandprint;
	}
	public void setSaveandprint(String saveandprint) {
		this.saveandprint = saveandprint;
	}
	public String[] getTestRemarks() {
		return testRemarks;
	}
	public void setTestRemarks(String[] testRemarks) {
		this.testRemarks = testRemarks;
	}
	public String[] getSampleno() {
		return sampleno;
	}
	public void setSampleno(String[] sampleno) {
		this.sampleno = sampleno;
	}
	public String[] getDateOfInvestigation() {
		return dateOfInvestigation;
	}
	public void setDateOfInvestigation(String[] dateOfInvestigation) {
		this.dateOfInvestigation = dateOfInvestigation;
	}
	public String[] getDateOfReport() {
		return dateOfReport;
	}
	public void setDateOfReport(String[] dateOfReport) {
		this.dateOfReport = dateOfReport;
	}
	public String[] getRemarks() {
		return remarks;
	}
	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
	}
	public String[] getResultvalue() {
		return resultvalue;
	}
	public void setResultvalue(String[] resultvalue) {
		this.resultvalue = resultvalue;
	}

	public String[] getDoctor() {
		return doctor;
	}
	public void setDoctor(String[] doctor) {
		this.doctor = doctor;
	}
	public String[] getCompleted() {
		return completed;
	}
	public void setCompleted(String[] completed) {
		this.completed = completed;
	}
	public String[] getConductedinreportformat() {
		return conductedinreportformat;
	}
	public void setConductedinreportformat(String[] conductedinreportformat) {
		this.conductedinreportformat = conductedinreportformat;
	}
	public String[] getFormatid() {
		return formatid;
	}
	public void setFormatid(String[] formatid) {
		this.formatid = formatid;
	}
	public String[] getPrescribedid() {
		return prescribedid;
	}
	public void setPrescribedid(String[] prescribedid) {
		this.prescribedid = prescribedid;
	}
	public String[] getReferenceRanges() {
		return referenceRanges;
	}
	public void setReferenceRanges(String[] referenceRanges) {
		this.referenceRanges = referenceRanges;
	}
	public String[] getReporttemplate() {
		return reporttemplate;
	}
	public void setReporttemplate(String[] reporttemplate) {
		this.reporttemplate = reporttemplate;
	}
	public String[] getResultlabel() {
		return resultlabel;
	}
	public void setResultlabel(String[] resultlabel) {
		this.resultlabel = resultlabel;
	}
	public String[] getRprescribedId() {
		return rprescribedId;
	}
	public void setRprescribedId(String[] rprescribedId) {
		this.rprescribedId = rprescribedId;
	}
	public String[] getRtestId() {
		return rtestId;
	}
	public void setRtestId(String[] rtestId) {
		this.rtestId = rtestId;
	}
	public String[] getTestid() {
		return testid;
	}
	public void setTestid(String[] testid) {
		this.testid = testid;
	}
	public String[] getTestname() {
		return testname;
	}
	public void setTestname(String[] testname) {
		this.testname = testname;
	}
	public String[] getUnits() {
		return units;
	}
	public void setUnits(String[] units) {
		this.units = units;
	}
	public String[] getWithinNormal() {
		return withinNormal;
	}
	public void setWithinNormal(String[] withinNormal) {
		this.withinNormal = withinNormal;
	}

	public String getEndPage() {
		return endPage;
	}
	public void setEndPage(String endPage) {
		this.endPage = endPage;
	}
	public String getStartPage() {
		return startPage;
	}
	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getPageNum() {
		return pageNum;
	}
	public void setPageNum(String pageNum) {
		this.pageNum = pageNum;
	}
	public String[] getFilterDepartment() {
		return filterDepartment;
	}
	public void setFilterDepartment(String[] filterDepartment) {
		this.filterDepartment = filterDepartment;
	}
	public String getFilterTestname() {
		return filterTestname;
	}
	public void setFilterTestname(String filterTestname) {
		this.filterTestname = filterTestname;
	}
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getMrno() {
		return mrno;
	}
	public void setMrno(String mrno) {
		this.mrno = mrno;
	}
	public boolean isPatientAll() {
		return patientAll;
	}
	public void setPatientAll(boolean patientAll) {
		this.patientAll = patientAll;
	}
	public boolean isPatientDiag() {
		return patientDiag;
	}
	public void setPatientDiag(boolean patientDiag) {
		this.patientDiag = patientDiag;
	}
	public boolean isPatientIp() {
		return patientIp;
	}
	public void setPatientIp(boolean patientIp) {
		this.patientIp = patientIp;
	}
	public boolean isPatientOp() {
		return patientOp;
	}
	public void setPatientOp(boolean patientOp) {
		this.patientOp = patientOp;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public String getVisitId() {
		return visitId;
	}
	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}


	public String getPresMrno() {
		return presMrno;
	}
	public void setPresMrno(String presMrno) {
		this.presMrno = presMrno;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPatientAge() {
		return patientAge;
	}
	public void setPatientAge(String patientAge) {
		this.patientAge = patientAge;
	}
	public String getPatVisitId() {
		return patVisitId;
	}
	public void setPatVisitId(String patVisitId) {
		this.patVisitId = patVisitId;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getReferredBy() {
		return referredBy;
	}
	public void setReferredBy(String referredBy) {
		this.referredBy = referredBy;
	}
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public String getCancelledBy() {
		return cancelledBy;
	}
	public void setCancelledBy(String cancelledBy) {
		this.cancelledBy = cancelledBy;
	}
	public String getPresId() {
		return presId;
	}
	public void setPresId(String presId) {
		this.presId = presId;
	}
	public String[] getReportId() {
		return reportId;
	}
	public void setReportId(String[] reportId) {
		this.reportId = reportId;
	}
	public String getPrintreportId() {
		return printreportId;
	}
	public void setPrintreportId(String printreportId) {
		this.printreportId = printreportId;
	}

}

package com.insta.hms.diagnosticmodule.laboratory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;

public class LaboratoryForm extends ActionForm {

	/**
	 *
	 */

	public static final Boolean REPORT_CONDUCTION_REQUIRED = true;
	public static final Boolean REPORT_CONDUCTION_NOT_REQUIRED = false;

	private static final long serialVersionUID = 1L;



	private String mrno;
	private String firstName;
	private String lastName;
	private String visitid;
	private String sampleNeeded;
	private String needPrint;
	private String doctor[];
	private String sampleno[];
	private String resultlabel[];
	private String resultvalue[];
	private String units[];
	private String referenceRanges[];
	private String conductedinreportformat[];
	private String prescribedid[];
	private String formatid[];
	private String remarks[];
	private String testid[];
	private String testname[];
	private String ddeptid[];
	private String completed[];
	private String reporttemplate[];
	private String dateOfInvestigation[];
	private String dateOfReport[];
	private String dateOfSample[];
	private String testRemarks[];
	private String closePrescriptions[];
	private String reportId[];
	private String signOf[];

	private String htestId[];
	private String hprescribedId[];
	private String hsampleTypeId[];
	private String sampleid[];
	private String hhouseType[];
	private String houtHouseId[];
	private String houtSourceId[];
	private String reqAutoGenId;
	private String isdeletedRow[];
	private String houtHouseName[];
	private String sampleStatus[];
	private String shortImpression;
	private String test_detail_status[];
	private String deleted[];
	private String manageReportName[];
	private String manageReportId[];
	private String methodId[];
	private String methodName[];
	private String revisionNumber[];
	private String patient_sponsor_type[];
    private String reference_docto_id;
	private String pres_doctor;



//	variables corresponding to RadiologyCancellation

	public String getReference_docto_id() {
		return reference_docto_id;
	}

	public void setReference_docto_id(String reference_docto_id) {
		this.reference_docto_id = reference_docto_id;
	}

	public String[] getRevisionNumber() {
		return revisionNumber;
	}

	public void setRevisionNumber(String[] revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
	private String testName;


	private String print[];
	private String dashboard;

	private String diagname;
	private String sampleNo;
	private String department;
	private String patienttype;


	private boolean patientAll;
	private boolean patientIp;
	private boolean patientOp;
	private boolean patientDiag;
	private boolean patientRetail;
	private boolean patientIn;

	private boolean conductionAll;
	private boolean conductionYes;
	private boolean conductionNo;



	private String fdate;
	private String tdate;

	private boolean testAll;
	private boolean testIncoming;
	private boolean testOutgoing;

	private String rfdate;
	private String rtdate;
	private String labno;
	private String patientName;
	private String phoneNo;
	private String inhouse;
	private String outhouse;
	private boolean pendingSamples;
	private boolean pendingReports;

	private String pageNum;
	private String sortOrder;
	private boolean sortReverse;
	private String saveandprint;
	private String startPage;
	private String endPage;
	private String templateContent;


	private String reportContent;
	private String reportName;
	private String reportid;

	private boolean statusAll;
	private boolean statusActive;
	private boolean statusInactive;
	private boolean signedOff;
	private String printreportId;
	private String from;
	private String reportdate;
	private String category;
	private FormFile formFile;
	private String imageTitle;
	private boolean showOnlyouthouseTests;
	private boolean showOnlyInhouseTests;

	private boolean testStatusAll; // using in audit logs
	private boolean testPrescribed;
	private boolean testPrescribedNoResults;
	private boolean testPartialConducted;
	private boolean testConducted;
	private boolean testConductedNoResults;
	private boolean testCancelled;

	private String handed_over;

	public boolean getShowOnlyInhouseTests() {
		return showOnlyInhouseTests;
	}

	public void setShowOnlyInhouseTests(boolean showOnlyInhouseTests) {
		this.showOnlyInhouseTests = showOnlyInhouseTests;
	}

	public boolean getShowOnlyouthouseTests() {
		return showOnlyouthouseTests;
	}

	public void setShowOnlyouthouseTests(boolean showOnlyouthouseTests) {
		this.showOnlyouthouseTests = showOnlyouthouseTests;
	}

	public String getImageTitle() {
		return imageTitle;
	}

	public void setImageTitle(String imageTitle) {
		this.imageTitle = imageTitle;
	}

	public FormFile getFormFile() {
		return formFile;
	}

	public void setFormFile(FormFile formFile) {
		this.formFile = formFile;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getReportdate() {
		return reportdate;
	}
	public void setReportdate(String reportdate) {
		this.reportdate = reportdate;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getPrintreportId() {
		return printreportId;
	}
	public void setPrintreportId(String printreportId) {
		this.printreportId = printreportId;
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
	public String getSaveandprint() {
		return saveandprint;
	}
	public void setSaveandprint(String saveandprint) {
		this.saveandprint = saveandprint;
	}
	public String getDashboard() {
		return dashboard;
	}
	public void setDashboard(String dashboard) {
		this.dashboard = dashboard;
	}

	public String[] getPrint() {
		return print;
	}
	public void setPrint(String[] print) {
		this.print = print;
	}
	@Override
	public void reset(ActionMapping arg0, HttpServletRequest arg1) {
		/*
		 * To allow extended ascii chars, we set the default encoding to UTF-8, which is
		 * what the browser sends us (since the page is encoded thus
		 */
		try {
			arg1.setCharacterEncoding("UTF-8");
		} catch (Exception e) {}

		super.reset(arg0, arg1);
		this.mrno = null;
		this.testid=null;
		this.doctor = null;
		this.sampleno=null;
		this.resultlabel=null;
		this.resultvalue=null;
		this.units=null;
		this.referenceRanges=null;
		this.remarks=null;
		this.conductedinreportformat=null;
		this.prescribedid = null;
		this.formatid = null;
		this.completed = null;
		this.ddeptid = null;
		this.reporttemplate=null;
		this.rprescribedId=null;
		this.rtestId=null;
		this.testname=null;
	}
	//used to for test_details table
	private String rtestId[];
	private String rprescribedId[];
	private String withinNormal[];
	private String rrevisionNumber[];

	public String[] getRrevisionNumber() {
		return rrevisionNumber;
	}

	public void setRrevisionNumber(String[] rrevisionNumber) {
		this.rrevisionNumber = rrevisionNumber;
	}

	public String[] getWithinNormal() {
		return withinNormal;
	}
	public void setWithinNormal(String[] withinNormal) {
		this.withinNormal = withinNormal;
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
	public String[] getDdeptid() {
		return ddeptid;
	}
	public void setDdeptid(String[] ddeptid) {
		this.ddeptid = ddeptid;
	}
	public String[] getTestname() {
		return testname;
	}
	public void setTestname(String[] testname) {
		this.testname = testname;
	}
	public String[] getTestid() {
		return testid;
	}
	public void setTestid(String[] testid) {
		this.testid = testid;
	}
	public String[] getRemarks() {
		return remarks;
	}
	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
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
	public String getMrno() {
		return mrno;
	}
	public void setMrno(String mrno) {
		this.mrno = mrno;
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
	public String[] getResultlabel() {
		return resultlabel;
	}
	public void setResultlabel(String[] resultlabel) {
		this.resultlabel = resultlabel;
	}
	public String[] getResultvalue() {
		return resultvalue;
	}
	public void setResultvalue(String[] resultvalue) {
		this.resultvalue = resultvalue;
	}
	public String[] getSampleno() {
		return sampleno;
	}
	public void setSampleno(String[] sampleno) {
		this.sampleno = sampleno;
	}
	public String[] getUnits() {
		return units;
	}
	public void setUnits(String[] units) {
		this.units = units;
	}
	public String getVisitid() {
		return visitid;
	}
	public void setVisitid(String visitid) {
		this.visitid = visitid;
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
	public String[] getReporttemplate() {
		return reporttemplate;
	}
	public void setReporttemplate(String[] reporttemplate) {
		this.reporttemplate = reporttemplate;
	}

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDiagname() {
		return diagname;
	}
	public void setDiagname(String diagname) {
		this.diagname = diagname;
	}
	public String getFdate() {
		return fdate;
	}
	public void setFdate(String fdate) {
		this.fdate = fdate;
	}
	public String getPageNum() {
		return pageNum;
	}
	public void setPageNum(String pageNum) {
		this.pageNum = pageNum;
	}
	public boolean getPatientAll() {
		return patientAll;
	}
	public void setPatientAll(boolean patientAll) {
		this.patientAll = patientAll;
	}
	public boolean getPatientDiag() {
		return patientDiag;
	}
	public void setPatientDiag(boolean patientDiag) {
		this.patientDiag = patientDiag;
	}
	public boolean getPatientIp() {
		return patientIp;
	}
	public void setPatientIp(boolean patientIp) {
		this.patientIp = patientIp;
	}
	public boolean getPatientOp() {
		return patientOp;
	}
	public void setPatientOp(boolean patientOp) {
		this.patientOp = patientOp;
	}
	public boolean getPatientRetail() {
		return patientRetail;
	}
	public void setPatientRetail(boolean patientRetail) {
		this.patientRetail = patientRetail;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public boolean getSortReverse() {
		return sortReverse;
	}
	public void setSortReverse(boolean sortReverse) {
		this.sortReverse = sortReverse;
	}
	public String getTdate() {
		return tdate;
	}
	public void setTdate(String tdate) {
		this.tdate = tdate;
	}
	public String getPatienttype() {
		return patienttype;
	}
	public void setPatienttype(String patienttype) {
		this.patienttype = patienttype;
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
	public String[] getDateOfSample() {
		return dateOfSample;
	}
	public void setDateOfSample(String[] dateOfSample) {
		this.dateOfSample = dateOfSample;
	}
	public String[] getTestRemarks() {
		return testRemarks;
	}
	public void setTestRemarks(String[] testRemarks) {
		this.testRemarks = testRemarks;
	}
	public String[] getClosePrescriptions() {
		return closePrescriptions;
	}
	public void setClosePrescriptions(String[] closePrescriptions) {
		this.closePrescriptions = closePrescriptions;
	}
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String[] getReportId() {
		return reportId;
	}
	public void setReportId(String[] reportId) {
		this.reportId = reportId;
	}
	public boolean getStatusActive() {
		return statusActive;
	}
	public void setStatusActive(boolean statusActive) {
		this.statusActive = statusActive;
	}
	public boolean getStatusAll() {
		return statusAll;
	}
	public void setStatusAll(boolean statusAll) {
		this.statusAll = statusAll;
	}
	public boolean getStatusInactive() {
		return statusInactive;
	}
	public void setStatusInactive(boolean statusInactive) {
		this.statusInactive = statusInactive;
	}
	public String getReportContent() {
		return reportContent;
	}
	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}
	public String getReportid() {
		return reportid;
	}
	public void setReportid(String reportid) {
		this.reportid = reportid;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getTemplateContent() {
		return templateContent;
	}
	public void setTemplateContent(String templateContent) {
		this.templateContent = templateContent;
	}
	public String[] getSignOf() {
		return signOf;
	}
	public void setSignOf(String[] signOf) {
		this.signOf = signOf;
	}

	public boolean getSignedOff() {
		return signedOff;
	}
	public void setSignedOff(boolean signedOff) {
		this.signedOff = signedOff;
	}

	public boolean getTestStatusAll() {
		return testStatusAll;
	}

	public void setTestStatusAll(boolean testStatusAll) {
		this.testStatusAll = testStatusAll;
	}

	public boolean getTestCancelled() {
		return testCancelled;
	}

	public void setTestCancelled(boolean testCancelled) {
		this.testCancelled = testCancelled;
	}

	public boolean getTestConducted() {
		return testConducted;
	}

	public void setTestConducted(boolean testConducted) {
		this.testConducted = testConducted;
	}

	public boolean getTestPrescribed() {
		return testPrescribed;
	}

	public void setTestPrescribed(boolean testPrescribed) {
		this.testPrescribed = testPrescribed;
	}

	public boolean getTestPartialConducted() {
		return testPartialConducted;
	}

	public void setTestPartialConducted(boolean testPartialConducted) {
		this.testPartialConducted = testPartialConducted;
	}

	public boolean isTestAll() {
		return testAll;
	}

	public void setTestAll(boolean testAll) {
		this.testAll = testAll;
	}

	public boolean isTestIncoming() {
		return testIncoming;
	}

	public void setTestIncoming(boolean testIncoming) {
		this.testIncoming = testIncoming;
	}

	public boolean isTestOutgoing() {
		return testOutgoing;
	}

	public void setTestOutgoing(boolean testOutgoing) {
		this.testOutgoing = testOutgoing;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String[] getHoutHouseId() {
		return houtHouseId;
	}

	public void setHoutHouseId(String[] houtHouseId) {
		this.houtHouseId = houtHouseId;
	}

	public String[] getHprescribedId() {
		return hprescribedId;
	}

	public void setHprescribedId(String[] hprescribedId) {
		this.hprescribedId = hprescribedId;
	}

	public String[] getHhouseType() {
		return hhouseType;
	}

	public void setHhouseType(String[] hhouseType) {
		this.hhouseType = hhouseType;
	}

	public String[] getHsampleTypeId() {
		return hsampleTypeId;
	}

	public void setHsampleTypeId(String[] hsampleTypeId) {
		this.hsampleTypeId = hsampleTypeId;
	}

	public String[] getHtestId() {
		return htestId;
	}

	public void setHtestId(String[] htestId) {
		this.htestId = htestId;
	}

	public String getReqAutoGenId() {
		return reqAutoGenId;
	}

	public void setReqAutoGenId(String reqAutoGenId) {
		this.reqAutoGenId = reqAutoGenId;
	}

	public String[] getSampleid() {
		return sampleid;
	}

	public void setSampleid(String[] sampleid) {
		this.sampleid = sampleid;
	}

	public String[] getIsdeletedRow() {
		return isdeletedRow;
	}

	public void setIsdeletedRow(String[] isdeletedRow) {
		this.isdeletedRow = isdeletedRow;
	}

	public String getSampleNeeded() {
		return sampleNeeded;
	}

	public void setSampleNeeded(String sampleNeeded) {
		this.sampleNeeded = sampleNeeded;
	}

	public String getNeedPrint() {
		return needPrint;
	}

	public void setNeedPrint(String needPrint) {
		this.needPrint = needPrint;
	}

	public String getInhouse() {
		return inhouse;
	}

	public void setInhouse(String inhouse) {
		this.inhouse = inhouse;
	}

	public String getLabno() {
		return labno;
	}

	public void setLabno(String labno) {
		this.labno = labno;
	}

	public String getOuthouse() {
		return outhouse;
	}

	public void setOuthouse(String outhouse) {
		this.outhouse = outhouse;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public boolean getPendingReports() {
		return pendingReports;
	}

	public void setPendingReports(boolean pendingReports) {
		this.pendingReports = pendingReports;
	}

	public boolean getPendingSamples() {
		return pendingSamples;
	}

	public void setPendingSamples(boolean pendingSamples) {
		this.pendingSamples = pendingSamples;
	}

	public String getRfdate() {
		return rfdate;
	}

	public void setRfdate(String rfdate) {
		this.rfdate = rfdate;
	}

	public String getRtdate() {
		return rtdate;
	}

	public void setRtdate(String rtdate) {
		this.rtdate = rtdate;
	}

	public boolean getPatientIn() {
		return patientIn;
	}

	public void setPatientIn(boolean patientIn) {
		this.patientIn = patientIn;
	}

	public boolean getConductionAll() {
		return conductionAll;
	}

	public void setConductionAll(boolean conductionAll) {
		this.conductionAll = conductionAll;
	}

	public boolean getConductionNo() {
		return conductionNo;
	}

	public void setConductionNo(boolean conductionNo) {
		this.conductionNo = conductionNo;
	}

	public boolean getConductionYes() {
		return conductionYes;
	}

	public void setConductionYes(boolean conductionYes) {
		this.conductionYes = conductionYes;
	}

	public String[] getHoutHouseName() {
		return houtHouseName;
	}

	public void setHoutHouseName(String[] houtHouseName) {
		this.houtHouseName = houtHouseName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String[] getSampleStatus() {
		return sampleStatus;
	}

	public void setSampleStatus(String[] sampleStatus) {
		this.sampleStatus = sampleStatus;
	}

	public String getHanded_over() {
		return handed_over;
	}

	public void setHanded_over(String handed_over) {
		this.handed_over = handed_over;
	}

	public String getSampleNo() {
		return sampleNo;
	}

	public void setSampleNo(String sampleNo) {
		this.sampleNo = sampleNo;
	}

	public String getShortImpression() {
		return shortImpression;
	}

	public void setShortImpression(String shortImpression) {
		this.shortImpression = shortImpression;
	}

	public String[] getTest_detail_status() {
		return test_detail_status;
	}

	public void setTest_detail_status(String[] test_detail_status) {
		this.test_detail_status = test_detail_status;
	}

	public String[] getDeleted() {
		return deleted;
	}

	public void setDeleted(String[] deleted) {
		this.deleted = deleted;
	}

	public String[] getManageReportId() {
		return manageReportId;
	}

	public void setManageReportId(String[] manageReportId) {
		this.manageReportId = manageReportId;
	}

	public String[] getManageReportName() {
		return manageReportName;
	}

	public void setManageReportName(String[] manageReportName) {
		this.manageReportName = manageReportName;
	}

	public boolean isTestPrescribedNoResults() {
		return testPrescribedNoResults;
	}

	public void setTestPrescribedNoResults(boolean testPrescribedNoResults) {
		this.testPrescribedNoResults = testPrescribedNoResults;
	}

	public boolean isTestConductedNoResults() {
		return testConductedNoResults;
	}

	public void setTestConductedNoResults(boolean testConductedNoResults) {
		this.testConductedNoResults = testConductedNoResults;
	}

	public String[] getHoutSourceId() {
		return houtSourceId;
	}

	public void setHoutSourceId(String[] houtSourceId) {
		this.houtSourceId = houtSourceId;
	}

	public String[] getMethodId() {
		return methodId;
	}

	public void setMethodId(String[] methodId) {
		this.methodId = methodId;
	}

	public String[] getMethodName() {
		return methodName;
	}

	public void setMethodName(String[] methodName) {
		this.methodName = methodName;
	}

	public String[] getPatient_sponsor_type() {
		return patient_sponsor_type;
	}

	public void setPatient_sponsor_type(String[] patient_sponsor_type) {
		this.patient_sponsor_type = patient_sponsor_type;
	}

	public String getPres_doctor() {
		return pres_doctor;
	}

	public void setPres_doctor(String pres_doctor) {
		this.pres_doctor = pres_doctor;
	}
}

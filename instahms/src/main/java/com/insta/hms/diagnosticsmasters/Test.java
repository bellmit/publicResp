package com.insta.hms.diagnosticsmasters;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Test {

	private String testId;
	private String testName;
	private Integer specimen;
	private String testConduct;

	private String ddeptId;
	private String ddeptName;
	private String diagCode;
	private String sampleNeed;
	private String sampleDate;
	private String sampleTime;
	private String sampleNo;
	private String origSampelNo;
	private String sampleType;
	private String reportGroup;
	private String order[];
	private String testStatus;
	private String testDate;
	private String labno;
	private String remarks;
	private String conductedDoctor;
	private String prescriptionType;
	private String mandate_additional_info;
	private String test_additional_info;
	
	private boolean conduction_applicable;
	private boolean results_entry_applicable;
	private String billType;
	private String billStatus;
	private String dependent_test_id;

	//for printing purpose
	private String reportId;
	private String reportName;
	private String sampleSource;

	// the following are for general/general only
	private BigDecimal routineCharge;
	private BigDecimal statCharge;
	private BigDecimal scheduleCharge;
	private BigDecimal discount;
	private String userName;
	private String codeType;
	private int serviceSubGroupId;
	private String conducting_doc_mandatory;
	private String[] hl7ExportInterface;
	private String hl7ExportCode;

	private String sampleCollectionInstructions;
	private String conductionInstructions;
	private int insurance_category_id;
	private String paymentStatus;
	private String preAuthReq;
	private String resultsValidation;
	private String testTime;
	private String specimenCondition;
	private String orderRemarks;
	private boolean allow_rate_increase;
	private boolean allow_rate_decrease;
	private String technician;
	private String[] conductingRoleIds;
	private String outSourceDestPresId;
	private String[] item_type;
	private String[] interface_name;
	private String[] hl7_mapping_deleted;
	private boolean isconfidential;
	private String incoming_source_type;
	
	public String[] getHl7_mapping_deleted() {
		return hl7_mapping_deleted;
	}
	public void setHl7_mapping_deleted(String[] hl7_mapping_deleted) {
		this.hl7_mapping_deleted = hl7_mapping_deleted;
	}
	public String[] getItem_type() {
		return item_type;
	}
	public void setItem_type(String[] item_type) {
		this.item_type = item_type;
	}
	public String[] getInterface_name() {
		return interface_name;
	}
	public void setInterface_name(String[] interface_name) {
		this.interface_name = interface_name;
	}
	public String getTest_additional_info() {
		return test_additional_info;
	}
	public void setTest_additional_info(String test_additional_info) {
		this.test_additional_info = test_additional_info;
	}
	
	public String[] getConductingRoleIds() {
		return conductingRoleIds;
	}
	public String getOutSourceDestPresId() {
		return outSourceDestPresId;
	}
	public void setOutSourceDestPresId(String outSourceDestPresId) {
		this.outSourceDestPresId = outSourceDestPresId;
	}
	public void setConductingRoleIds(String[] conductingRoleIds) {
		this.conductingRoleIds = conductingRoleIds;
	}
	public boolean isAllow_rate_decrease() {
		return allow_rate_decrease;
	}
	public void setAllow_rate_decrease(boolean allow_rate_decrease) {
		this.allow_rate_decrease = allow_rate_decrease;
	}
	public boolean isAllow_rate_increase() {
		return allow_rate_increase;
	}
	public void setAllow_rate_increase(boolean allow_rate_increase) {
		this.allow_rate_increase = allow_rate_increase;
	}
	public String getSpecimenCondition() {
		return specimenCondition;
	}
	public void setSpecimenCondition(String specimenCondition) {
		this.specimenCondition = specimenCondition;
	}
	public String getTestTime() {
		return testTime;
	}
	public void setTestTime(String testTime) {
		this.testTime = testTime;
	}
	public String getResultsValidation() {
		return resultsValidation;
	}
	public void setResultsValidation(String resultsValidation) {
		this.resultsValidation = resultsValidation;
	}
	public String getPreAuthReq() {
		return preAuthReq;
	}
	public void setPreAuthReq(String preAuthReq) {
		this.preAuthReq = preAuthReq;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public int getInsurance_category_id() {
		return insurance_category_id;
	}
	public void setInsurance_category_id(int insurance_category_id) {
		this.insurance_category_id = insurance_category_id;
	}
	public String getConductionInstructions() {
		return conductionInstructions;
	}
	public void setConductionInstructions(String conductionInstructions) {
		this.conductionInstructions = conductionInstructions;
	}
	public String getSampleCollectionInstructions() {
		return sampleCollectionInstructions;
	}
	public void setSampleCollectionInstructions(String sampleCollectionInstructions) {
		this.sampleCollectionInstructions = sampleCollectionInstructions;
	}
	public int getServiceSubGroupId() {
		return serviceSubGroupId;
	}
	public void setServiceSubGroupId(int serviceSubGroupId) {
		this.serviceSubGroupId = serviceSubGroupId;
	}
	public String[] getHl7ExportInterface() { return hl7ExportInterface; }
	public void setHl7ExportInterface(String[] v) { hl7ExportInterface = v; }

	public String getHl7ExportCode() { return hl7ExportCode; }
	public void setHl7ExportCode(String v) { hl7ExportCode = v; }

	public String getDdeptName(){return ddeptName;}
	public void setDdeptName(String v){ddeptName = v; }

	public String getTestId() { return testId; }
	public void setTestId(String v) { testId = v; }

	public String getTestName() { return testName; }
	public void setTestName(String v) { testName = v; }

	public Integer getSpecimen() { return specimen; }
	public void setSpecimen(Integer v) { specimen = v; }

	public String getTestConduct() { return testConduct; }
	public void setTestConduct(String v) { testConduct = v; }


	public String getDdeptId() { return ddeptId; }
	public void setDdeptId(String v) { ddeptId = v; }

	public String getDiagCode() { return diagCode; }
	public void setDiagCode(String v) { diagCode = v; }

	public String getSampleNeed() { return sampleNeed; }
	public void setSampleNeed(String v) { sampleNeed = v; }

	public String getReportGroup() { return reportGroup; }
	public void setReportGroup(String v) { reportGroup = v; }

	public BigDecimal getStatCharge() { return statCharge; }
	public void setStatCharge(BigDecimal v) { statCharge = v; }

	public BigDecimal getRoutineCharge() { return routineCharge; }
	public void setRoutineCharge(BigDecimal v) { routineCharge = v; }

	public BigDecimal getScheduleCharge() { return scheduleCharge; }
	public void setScheduleCharge(BigDecimal v) { scheduleCharge = v; }

	public BigDecimal getDiscount() { return discount; }
	public void setDiscount(BigDecimal v) { discount = v; }

	public void setOrder(String[] order) {
		this.order = order;
	}
	public String getTestStatus() {
		return testStatus;
	}
	public void setTestStatus(String testStatus) {
		this.testStatus = testStatus;
	}
	public String getLabno() {
		return labno;
	}
	public void setLabno(String labno) {
		this.labno = labno;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public String getConductedDoctor() {
		return conductedDoctor;
	}
	public void setConductedDoctor(String conductedDoctor) {
		this.conductedDoctor = conductedDoctor;
	}
	public String getSampleDate() { return sampleDate; }
	public void setSampleDate(String v) { sampleDate = v; }
	public String getTestDate() { return testDate; }
	public void setTestDate(String v) { testDate = v; }

	public String getSampleNo() {return sampleNo;}
	public void setSampleNo(String sampleNo) {this.sampleNo = sampleNo;}
	public String getPrescriptionType() {
		return prescriptionType;
	}
	public void setPrescriptionType(String prescriptionType) {
		this.prescriptionType = prescriptionType;
	}
	public boolean isConduction_applicable() {return conduction_applicable;}
	public void setConduction_applicable(boolean conduction_applicable) {this.conduction_applicable = conduction_applicable;}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBillType() { return billType; }
	public void setBillType(String v) { billType = v; }

	public String getBillStatus() { return billStatus; }
	public void setBillStatus(String v) { billStatus = v; }

	public String getCodeType() { return codeType; }
	public void setCodeType(String v) { codeType = v; }
	public String getConducting_doc_mandatory() {
		return conducting_doc_mandatory;
	}
	public void setConducting_doc_mandatory(String conducting_doc_mandatory) {
		this.conducting_doc_mandatory = conducting_doc_mandatory;
	}
	public String getSampleSource() {
		return sampleSource;
	}
	public void setSampleSource(String sampleSource) {
		this.sampleSource = sampleSource;
	}
	public String getOrderRemarks() {
		return orderRemarks;
	}
	public void setOrderRemarks(String orderRemarks) {
		this.orderRemarks = orderRemarks;
	}
	public String getSampleType() {
		return sampleType;
	}
	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}
	public String getDependent_test_id() {
		return dependent_test_id;
	}
	public void setDependent_test_id(String dependent_test_id) {
		this.dependent_test_id = dependent_test_id;
	}
	public String getTechnician() {
		return technician;
	}
	public void setTechnician(String technician) {
		this.technician = technician;
	}
	public boolean isResults_entry_applicable() {
		return results_entry_applicable;
	}
	public void setResults_entry_applicable(boolean results_entry_applicable) {
		this.results_entry_applicable = results_entry_applicable;
	}
	public String getOrigSampelNo() {
		return origSampelNo;
	}
	public void setOrigSampelNo(String origSampelNo) {
		this.origSampelNo = origSampelNo;
	}
	public String getSampleTime() {
		return sampleTime;
	}
	public void setSampleTime(String sampleTime) {
		this.sampleTime = sampleTime;
	}
	public String getMandate_additional_info() {
		return mandate_additional_info;
	}
	public void setMandate_additional_info(String mandate_additional_info) {
		this.mandate_additional_info = mandate_additional_info;
	}
	public boolean isIsconfidential() {
		return isconfidential;
	}
	public void setIsconfidential(boolean isconfidential) {
		this.isconfidential = isconfidential;
	}

	public String getIncoming_source_type() {
		return incoming_source_type;
	}
	public void setIncoming_source_type(String incoming_source_type) {
		this.incoming_source_type = incoming_source_type;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("discount", this.discount);
		map.put("routineCharge", this.routineCharge);
		map.put("scheduleCharge", this.scheduleCharge);
		map.put("statCharge", this.statCharge);
		map.put("allow_rate_decrease", this.allow_rate_decrease);
		map.put("allow_rate_increase", this.allow_rate_increase);
		map.put("conduction_applicable", this.conduction_applicable);
		map.put("isconfidential", this.isconfidential);
		map.put("results_entry_applicable", this.results_entry_applicable);
		map.put("insurance_category_id", this.insurance_category_id);
		map.put("serviceSubGroupId", this.serviceSubGroupId);
		map.put("specimen", this.specimen);
		map.put("billStatus", this.billStatus);
		map.put("billType", this.billType);
		map.put("codeType", this.codeType);
		map.put("conductedDoctor", this.conductedDoctor);
		map.put("conducting_doc_mandatory", this.conducting_doc_mandatory);
		map.put("conductionInstructions", this.conductionInstructions);
		map.put("ddeptId", this.ddeptId);
		map.put("ddeptName", this.ddeptName);
		map.put("dependent_test_id", this.dependent_test_id);
		map.put("diagCode", this.diagCode);
		map.put("hl7ExportCode", this.hl7ExportCode);
		map.put("incoming_source_type", this.incoming_source_type);
		map.put("labno", this.labno);
		map.put("mandate_additional_info", this.mandate_additional_info);
		map.put("order", this.order);
		map.put("orderRemarks", this.orderRemarks);
		map.put("origSampelNo", this.origSampelNo);
		map.put("outSourceDestPresId", this.outSourceDestPresId);
		map.put("paymentStatus", this.paymentStatus);
		map.put("preAuthReq", this.preAuthReq);
		map.put("prescriptionType", this.prescriptionType);
		map.put("remarks", this.remarks);
		map.put("reportGroup", this.reportGroup);
		map.put("reportId", this.reportId);
		map.put("reportName", this.reportName);
		map.put("resultsValidation", this.resultsValidation);
		map.put("sampleCollectionInstructions", this.sampleCollectionInstructions);
		map.put("sampleDate", this.sampleDate);
		map.put("sampleNeed", this.sampleNeed);
		map.put("sampleNo", this.sampleNo);
		map.put("sampleSource", this.sampleSource);
		map.put("sampleTime", this.sampleTime);
		map.put("sampleType", this.sampleType);
		map.put("specimenCondition", this.specimenCondition);
		map.put("technician", this.technician);
		map.put("test_additional_info", this.test_additional_info);
		map.put("testConduct", this.testConduct);
		map.put("testDate", this.testDate);
		map.put("testId", this.testId);
		map.put("testName", this.testName);
		map.put("testStatus", this.testStatus);
		map.put("testTime", this.testTime);
		map.put("userName", this.userName);
		map.put("conductingRoleIds", this.conductingRoleIds);
		map.put("hl7_mapping_deleted", this.hl7_mapping_deleted);
		map.put("hl7ExportInterface", this.hl7ExportInterface);
		map.put("interface_name", this.interface_name);
		map.put("item_type", this.item_type);
		return map;
	}
}


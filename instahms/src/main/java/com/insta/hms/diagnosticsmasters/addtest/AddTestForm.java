package com.insta.hms.diagnosticsmasters.addtest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

public class AddTestForm extends ActionForm {

  static Logger logger = LoggerFactory.getLogger(AddTestForm.class);

  private static final long serialVersionUID = 1L;

  // the following are used in add/edit test
  private String addOrEdit;
  private String ddeptId;
  private String testName;
  private String sampleNeed;
  private Integer specimen;
  private String testConduct;
  private String testStatus;
  private String routineCharge;
  private String reportGroup;
  private String formatName[];
  private boolean conduction_applicable;
  private boolean results_entry_applicable;
  private String conducting_doc_mandatory;
  private String remarks;

  private String resultOp[];
  private String resultLabel[];
  private String resultLabelShort[];
  private String order[];
  private String units[];
  private String hl7_interface[];
  private String refRange[];
  private String resultlabel_id[];

  private String testId;
  private String dep_test_name;
  private String dependent_test_id;
  private String mandate_additional_info;

  // the following are used in edit charges
  private String diagCode;
  private String orgItemCode;
  private boolean applicable;
  private String chargeType;
  private String orgName;
  private String orgId;
  private String[] bedTypes;
  private Double[] regularCharges;// routine
  private Double[] discount;// discount
  private String updateTable;

  // the following are used in group udpate/import
  private String allBedTypes;
  private String allTests;
  private String[] selectTest;
  private String[] selectBedType;
  private String incType;
  private String amtType;
  private BigDecimal amount;
  private BigDecimal roundOff;

  private FormFile xlsChargeFile;
  // This is for test defination details upload
  private FormFile xlsTestFile;
  private String codeType;

  public String[] getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String[] defaultValue) {
    this.defaultValue = defaultValue;
  }

  private int serviceSubGroupId;
  private String[] hl7ExportInterface;

  private String hl7ExportCode;
  private String test_additional_info;

  private String sampleCollectionInstructions;
  private String expression[];
  private String conductionInstructions;

  private int insurance_category_id;
  private String preAuthReq;
  private String resultsValidation;
  private boolean allow_rate_increase;
  private boolean allow_rate_decrease;

  private String[] notApplicableRatePlans;
  private String[] conductingRoleIds;
  private String[] defaultValue;
  private String[] item_type;
  private String[] interface_name;
  private String[] hl7_mapping_deleted;

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

  public void setConductingRoleIds(String[] conductingRoleIds) {
    this.conductingRoleIds = conductingRoleIds;
  }

  public String[] getNotApplicableRatePlans() {
    return notApplicableRatePlans;
  }

  public void setNotApplicableRatePlans(String[] notApplicableRatePlans) {
    this.notApplicableRatePlans = notApplicableRatePlans;
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

  public String[] getExpression() {
    return expression;
  }

  public void setExpression(String[] expression) {
    this.expression = expression;
  }

  public String getSampleCollectionInstructions() {
    return sampleCollectionInstructions;
  }

  public void setSampleCollectionInstructions(String sampleCollectionInstructions) {
    this.sampleCollectionInstructions = sampleCollectionInstructions;
  }

  // accessors
  public int getServiceSubGroupId() {
    return serviceSubGroupId;
  }

  public void setServiceSubGroupId(int serviceSubGroupId) {
    this.serviceSubGroupId = serviceSubGroupId;
  }

  public String[] getHl7ExportInterface() {
    return hl7ExportInterface;
  }

  public void setHl7ExportInterface(String[] v) {
    hl7ExportInterface = v;
  }

  public String[] getHl7_interface() {
    return hl7_interface;
  }

  public void setHl7_interface(String[] hl7_interface) {
    this.hl7_interface = hl7_interface;
  }

  public String getHl7ExportCode() {
    return hl7ExportCode;
  }

  public void setHl7ExportCode(String v) {
    hl7ExportCode = v;
  }

  public boolean isApplicable() {
    return applicable;
  }

  public void setApplicable(boolean applicable) {
    this.applicable = applicable;
  }

  public String getOrgItemCode() {
    return orgItemCode;
  }

  public void setOrgItemCode(String orgItemCode) {
    this.orgItemCode = orgItemCode;
  }

  public Double[] getRegularCharges() {
    return regularCharges;
  }

  public void setRegularCharges(Double[] regularCharges) {
    this.regularCharges = regularCharges;
  }

  public Double[] getDiscount() {
    return discount;
  }

  public void setDiscount(Double[] discount) {
    this.discount = discount;
  }

  public String[] getBedTypes() {
    return bedTypes;
  }

  public void setBedTypes(String[] bedTypes) {
    this.bedTypes = bedTypes;
  }

  public String getUpdateTable() {
    return updateTable;
  }

  public void setUpdateTable(String v) {
    updateTable = v;
  }

  public Integer getSpecimen() {
    return specimen;
  }

  public void setSpecimen(Integer v) {
    specimen = v;
  }

  public String getTestConduct() {
    return testConduct;
  }

  public void setTestConduct(String v) {
    testConduct = v;
  }

  public String[] getFormatName() {
    return formatName;
  }

  public void setFormatName(String[] v) {
    formatName = v;
  }

  public String getDdeptId() {
    return ddeptId;
  }

  public void setDdeptId(String v) {
    ddeptId = v;
  }

  public String getTestName() {
    return testName;
  }

  public void setTestName(String v) {
    testName = v;
  }

  public String getDiagCode() {
    return diagCode;
  }

  public void setDiagCode(String v) {
    diagCode = v;
  }

  public String getSampleNeed() {
    return sampleNeed;
  }

  public void setSampleNeed(String v) {
    sampleNeed = v;
  }

  public String getReportGroup() {
    return reportGroup;
  }

  public void setReportGroup(String v) {
    reportGroup = v;
  }

  public String getAddOrEdit() {
    return addOrEdit;
  }

  public void setAddOrEdit(String v) {
    addOrEdit = v;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String v) {
    testId = v;
  }

  public String getRoutineCharge() {
    return routineCharge;
  }

  public void setRoutineCharge(String v) {
    routineCharge = v;
  }

  public String[] getResultOp() {
    return resultOp;
  }

  public void setResultOp(String[] v) {
    resultOp = v;
  }

  public String[] getResultLabel() {
    return resultLabel;
  }

  public void setResultLabel(String[] v) {
    resultLabel = v;
  }

  public String[] getUnits() {
    return units;
  }

  public void setUnits(String[] v) {
    units = v;
  }

  public String[] getRefRange() {
    return refRange;
  }

  public void setRefRange(String[] v) {
    refRange = v;
  }

  public String[] getOrder() {
    return order;
  }

  public void setOrder(String[] order) {
    this.order = order;
  }

  public String getChargeType() {
    return chargeType;
  }

  public void setChargeType(String chargeType) {
    this.chargeType = chargeType;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getTestStatus() {
    return testStatus;
  }

  public void setTestStatus(String testStatus) {
    this.testStatus = testStatus;
  }

  public String[] getResultlabel_id() {
    return resultlabel_id;
  }

  public void setResultlabel_id(String[] resultlabel_id) {
    this.resultlabel_id = resultlabel_id;
  }

  public String getAllBedTypes() {
    return allBedTypes;
  }

  public void setAllBedTypes(String v) {
    allBedTypes = v;
  }

  public String getAllTests() {
    return allTests;
  }

  public void setAllTests(String v) {
    allTests = v;
  }

  public String[] getSelectTest() {
    return selectTest;
  }

  public void setSelectTest(String[] v) {
    selectTest = v;
  }

  public String[] getSelectBedType() {
    return selectBedType;
  }

  public void setSelectBedType(String[] v) {
    selectBedType = v;
  }

  public String getIncType() {
    return incType;
  }

  public void setIncType(String v) {
    incType = v;
  }

  public String getAmtType() {
    return amtType;
  }

  public void setAmtType(String v) {
    amtType = v;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal v) {
    amount = v;
  }

  public BigDecimal getRoundOff() {
    return roundOff;
  }

  public void setRoundOff(BigDecimal v) {
    roundOff = v;
  }

  public FormFile getXlsChargeFile() {
    return xlsChargeFile;
  }

  public void setXlsChargeFile(FormFile v) {
    xlsChargeFile = v;
  }

  public FormFile getXlsTestFile() {
    return xlsTestFile;
  }

  public void setXlsTestFile(FormFile v) {
    this.xlsTestFile = v;
  }

  @Override
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {
    try {
      arg1.setCharacterEncoding("UTF-8");
    } catch (Exception e) {
    }

    super.reset(arg0, arg1);
    this.specimen = null;
    this.testConduct = null;
    this.formatName = null;
    this.ddeptId = null;
    this.testName = null;
    this.diagCode = null;
    this.sampleNeed = null;
    this.reportGroup = null;
    this.testStatus = null;

    this.resultOp = null;
    this.resultLabel = null;
    this.units = null;
    this.refRange = null;

    this.routineCharge = null;
    this.discount = null;
    this.updateTable = null;
    this.allBedTypes = "no";

    logger.debug("Resetting inside AddTestForm form " + this);
  }

  @Override
  public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
    logger.debug("in side validate method=========++>");
    return super.validate(arg0, arg1);
  }

  public boolean isConduction_applicable() {
    return conduction_applicable;
  }

  public void setConduction_applicable(boolean conduction_applicable) {
    this.conduction_applicable = conduction_applicable;
  }

  public String getCodeType() {
    return codeType;
  }

  public void setCodeType(String v) {
    codeType = v;
  }

  public String getConducting_doc_mandatory() {
    return conducting_doc_mandatory;
  }

  public void setConducting_doc_mandatory(String conducting_doc_mandatory) {
    this.conducting_doc_mandatory = conducting_doc_mandatory;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String[] getResultLabelShort() {
    return resultLabelShort;
  }

  public void setResultLabelShort(String[] resultLabelShort) {
    this.resultLabelShort = resultLabelShort;
  }

  public String getDep_test_name() {
    return dep_test_name;
  }

  public void setDep_test_name(String dep_test_name) {
    this.dep_test_name = dep_test_name;
  }

  public String getDependent_test_id() {
    return dependent_test_id;
  }

  public void setDependent_test_id(String dependent_test_id) {
    this.dependent_test_id = dependent_test_id;
  }

  public boolean isResults_entry_applicable() {
    return results_entry_applicable;
  }

  public void setResults_entry_applicable(boolean results_entry_applicable) {
    this.results_entry_applicable = results_entry_applicable;
  }

  public String getMandate_additional_info() {
    return mandate_additional_info;
  }

  public void setMandate_additional_info(String mandate_additional_info) {
    this.mandate_additional_info = mandate_additional_info;
  }

}

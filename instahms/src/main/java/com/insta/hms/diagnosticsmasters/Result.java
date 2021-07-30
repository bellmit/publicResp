package com.insta.hms.diagnosticsmasters;

import org.apache.commons.beanutils.BasicDynaBean;

public class Result {

	private String testId;
	private String resultLabel;
	private String resultLabelShort;
	private String units;
	private String referenceRanges;
	private String order;
	private String resultlabel_id;
	private String expression;
	private String hl7_interface;
	private String code_type;
	private String result_code;
	private BasicDynaBean resultrange;
	private Integer methodId;
	private Integer prevMethodId;
	private String methodName;

	//the following or for transcation tables
	private String withinNormal;
	private String resultvalue;
	private String remarks;
	private int countOfRanges;
	private String result_disclaimer;
	private int test_details_id;
	private int revised_test_details_id;
	private int original_test_details_id;
	private String amendment_reason;
	private String test_detail_status;
	private String test_details_status;

	private String dataAllowed;
	private String sourceIfList;
	private String defaultValue;
	private String calculated;


	public String getCalculated() {
		return calculated;
	}

	public void setCalculated(String calculated) {
		this.calculated = calculated;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDataAllowed() {
		return dataAllowed;
	}

	public void setDataAllowed(String dataAllowed) {
		this.dataAllowed = dataAllowed;
	}

	public String getSourceIfList() {
		return sourceIfList;
	}

	public String getAmendment_reason() {
		return amendment_reason;
	}
	public void setSourceIfList(String sourceIfList) {
		this.sourceIfList = sourceIfList;
	}
	public void setAmendment_reason(String amendment_reason) {
		this.amendment_reason = amendment_reason;
	}

	public int getOriginal_test_details_id() {
		return original_test_details_id;
	}

	public void setOriginal_test_details_id(int original_test_details_id) {
		this.original_test_details_id = original_test_details_id;
	}

	public int getRevised_test_details_id() {
		return revised_test_details_id;
	}

	public void setRevised_test_details_id(int revised_test_details_id) {
		this.revised_test_details_id = revised_test_details_id;
	}

	public String getTest_detail_status() {
		return test_detail_status;
	}

	public void setTest_detail_status(String test_detail_status) {
		this.test_detail_status = test_detail_status;
	}

	public int getTest_details_id() {
		return test_details_id;
	}

	public void setTest_details_id(int test_details_id) {
		this.test_details_id = test_details_id;
	}


	public String getResult_disclaimer() {
		return result_disclaimer;
	}

	public void setResult_disclaimer(String result_disclaimer) {
		this.result_disclaimer = result_disclaimer;
	}

	public int getCountOfRanges() {
		return countOfRanges;
	}

	public void setCountOfRanges(int countOfRanges) {
		this.countOfRanges = countOfRanges;
	}

	public String getRemarks() {return remarks;}

	public void setRemarks(String remarks) {this.remarks = remarks;}

	public Result(String testId, String resultLabel,String resultLabelShort, String units, String order,
				String resultlabel_id,String expression, String hl7_interface) {
		this.testId = testId;
		this.resultLabel = resultLabel;
		this.resultLabelShort = resultLabelShort;
		this.units = units;
		this.order = order;
		this.resultlabel_id = resultlabel_id;
		this.expression = expression;
		this.hl7_interface = hl7_interface;
	}

	public Result(String testId, String resultLabel,String resultLabelShort, String units, String order,
				String resultlabel_id,String expression) {
		this.testId = testId;
		this.resultLabel = resultLabel;
		this.resultLabelShort = resultLabelShort;
		this.units = units;
		this.order = order;
		this.resultlabel_id = resultlabel_id;
		this.expression = expression;
	}

	public Result(String testId, String resultLabel, String units, String referenceRanges, String order) {
		this.testId = testId;
		this.resultLabel = resultLabel;
		this.units = units;
		this.referenceRanges = referenceRanges;
		this.order = order;
	}

	public Result(String testId, String resultLabel, String units,  String order) {
		this.testId = testId;
		this.resultLabel = resultLabel;
		this.units = units;
		this.order = order;
	}

	public String getTestId() { return testId; }
	public void setTestId(String v) { testId = v; }

	public String getResultLabel() { return resultLabel; }
	public void setResultLabel(String v) { resultLabel = v; }

	public String getUnits() { return units; }
	public void setUnits(String v) { units = v; }

	public String getReferenceRanges() { return referenceRanges; }
	public void setReferenceRanges(String v) { referenceRanges = v; }

	public String getResultvalue() {
		return resultvalue;
	}

	public void setResultvalue(String resultvalue) {
		this.resultvalue = resultvalue;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getWithinNormal() {
		return withinNormal;
	}

	public void setWithinNormal(String withinNormal) {
		this.withinNormal = withinNormal;
	}

	public String getResultlabel_id() {
		return resultlabel_id;
	}

	public void setResultlabel_id(String resultlabel_id) {
		this.resultlabel_id = resultlabel_id;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getCode_type() {
		return code_type;
	}

	public void setCode_type(String code_type) {
		this.code_type = code_type;
	}

	public String getResult_code() {
		return result_code;
	}

	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}

	public BasicDynaBean getResultrange() {
		return resultrange;
	}

	public void setResultrange(BasicDynaBean resultrange) {
		this.resultrange = resultrange;
	}

	//Result Seviarity Ranges

	public static final String NORMAL = "Y";
	public static final String ABNORMAL_LOW = "*";
	public static final String ABNORMAL_HIGH = "#";
	public static final String CRITICAL_LOW = "**";
	public static final String CRITICAL_HIGH = "##";
	public static final String IMPROBABLE_LOW = "***";
	public static final String IMPROPABLE_HIGH = "###";


	public String getTest_details_status() {
		return test_details_status;
	}

	public void setTest_details_status(String test_details_status) {
		this.test_details_status = test_details_status;
	}

	public String getResultLabelShort() {
		return resultLabelShort;
	}

	public void setResultLabelShort(String resultLabelShort) {
		this.resultLabelShort = resultLabelShort;
	}

	public String getHl7_interface() {
		return hl7_interface;
	}

	public void setHl7_interface(String hl7_interface) {
		this.hl7_interface = hl7_interface;
	}

	public Integer getMethodId() {
		return methodId;
	}

	public void setMethodId(Integer methodId) {
		this.methodId = methodId;
	}

	public Integer getPrevMethodId() {
		return prevMethodId;
	}

	public void setPrevMethodId(Integer prevMethodId) {
		this.prevMethodId = prevMethodId;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}

package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestDetailsDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);
		Map<String, String> severityMap = new LinkedHashMap<String, String>();

		severityMap.put("Y", "Normal");
		severityMap.put("#", "Abnormal Low");
		severityMap.put("##", "Critical Low");
		severityMap.put("*", "Abnormal High");
		severityMap.put("**", "Critical High");

		desc.addField("patient_id", "Visit ID", false);
		desc.addField("test_id", "Test Name", false);
		desc.addField("conducted_in_reportformat", "Template Report");
		desc.addField("format_name", "Format Name");
		desc.addField("patient_report_file", "Patient Report File");
		desc.addField("resultlabel", "Test");
		desc.addField("report_value", "Result");
		desc.addField("units", "Units");
		desc.addField("reference_range", "Reference Range", false);
		desc.addField("comments", "Comments", false);
		desc.addField("withinnormal", "Severity", false);

		desc.addFieldValue("withinnormal", severityMap);
		desc.addFieldValue("test_id", "diagnostics", "test_id", "test_name");

		return desc;
	}

}

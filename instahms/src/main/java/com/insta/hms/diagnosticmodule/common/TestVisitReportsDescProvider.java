package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestVisitReportsDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);
		Map<String, String> categoryMap = new LinkedHashMap<String, String>();

		categoryMap.put("DEP_LAB", "Laboratory");
		categoryMap.put("DEP_RAD", "Radiology");

		desc.addField("report_id", "Report Name", false);
		desc.addField("patient_id", "Visit ID", false);
		desc.addField("report_name", "Report Name", false);
		desc.addField("category", "Category");
		desc.addField("report_date", "Report Date");
		desc.addField("signed_off", "Signed Off");
		desc.addField("report_mode", "Report Type");

		desc.addFieldValue("report_id", "test_visit_reports", "report_id", "report_name");
		desc.addFieldValue("category", categoryMap);
		// TODO: setup the lookup value for report_mode
		return desc;
	}

}

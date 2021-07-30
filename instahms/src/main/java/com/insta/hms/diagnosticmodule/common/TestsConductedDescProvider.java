package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class TestsConductedDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("patient_id", "Patient Visit", false);
		desc.addField("test_id", "Test", false);
		desc.addField("conducted_date", "Conducted Date", true);
		desc.addField("conducted_by", "Conducted By", true);
		desc.addField("issued_by", "Issued By", true);
		desc.addField("designation", "Designation", false);
		desc.addField("conducted_time", "Conducted Time", false);
		desc.addField("satisfactory_status", "Satisfactory Status", true);
		desc.addField("remarks", "Remarks", false);

		desc.addFieldValue("test_id", "diagnostics", "test_id", "test_name");
		desc.addFieldValue("conducted_by", "doctors", "doctor_id", "doctor_name");

		return desc;
	}
}

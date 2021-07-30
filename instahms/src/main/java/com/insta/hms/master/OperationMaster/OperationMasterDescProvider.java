package com.insta.hms.master.OperationMaster;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class OperationMasterDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> statusMap = new LinkedHashMap<String,String>();
		statusMap.put("A", "Active");
		statusMap.put("I", "InActive");

		desc.addField("operation_name", "Operation Name");
		desc.addField("dept_id", "Department");
		desc.addField("operation_code", "Operation Code");
		desc.addField("status", "Status");
		desc.addField("conduction_applicable", "Conduction Applicable");

		desc.addFieldValue("status", statusMap);

		desc.addFieldValue("dept_id", "department", "dept_id", "dept_name");

		return desc;
	}

}
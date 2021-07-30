package com.insta.hms.master.ServiceMaster;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceMasterDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> statusMap = new LinkedHashMap<String,String>();
		statusMap.put("A", "Active");
		statusMap.put("I", "InActive");

		desc.addField("service_name", "Service Name");
		desc.addField("units", "Units");
		desc.addField("service_tax", "Service Tax");
		desc.addField("service_code", "Service Code");
		desc.addField("status", "Status");
		desc.addField("conduction_applicable", "Conduction Applicable");
		desc.addField("serv_dept_id", "Service Department");

		desc.addFieldValue("status", statusMap);

		desc.addFieldValue("serv_dept_id", "services_departments", "serv_dept_id", "department");

		return desc;
	}

}
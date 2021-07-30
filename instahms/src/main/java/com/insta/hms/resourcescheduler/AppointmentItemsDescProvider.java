package com.insta.hms.resourcescheduler;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppointmentItemsDescProvider implements AuditLogDescProvider{

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> resourceDescription = new LinkedHashMap<String,String>();
		resourceDescription.put("OPDOC", "Doctor");
		resourceDescription.put("LABTECH", "Technician/Radiologist");
		resourceDescription.put("SUDOC", "Surgeon");
		resourceDescription.put("ANEDOC", "Anesthetist");
		resourceDescription.put("EQID", "Equipment");
		resourceDescription.put("SRID", "Service Resource");
		resourceDescription.put("THID", "Operation Theatre");
		resourceDescription.put("DOC", "Doctor");

		desc.addField("resource_type","Resource Type");
        desc.addFieldValue("resource_type", resourceDescription);

		desc.addField("resource_id", "Resource", true, "resource_type");

		desc.addFieldValue("resource_id", "EQID", "test_equipment_master", "eq_id", "equipment_name");
		desc.addFieldValue("resource_id", "LABTECH", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("resource_id", "OPDOC", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("resource_id", "SUDOC", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("resource_id", "ANEDOC", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("resource_id", "DOC", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("resource_id", "THID", "theatre_master", "theatre_id", "theatre_name");
		desc.addFieldValue("resource_id", "SRID", "service_resource_master", "serv_res_id", "serv_resource_name");

		return desc;
	}
}

package com.insta.hms.master.ServiceMaster;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class ServiceChargeDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("service_name", "Service Name");
		desc.addField("bed_type", "Bed Type");
		desc.addField("org_id", "Organization");
		desc.addField("unit_charge", "Unit Charge");
		desc.addField("discount", "Discount");

		desc.addFieldValue("org_id", "organization_details", "org_id", "org_name");

		return desc;
	}

}
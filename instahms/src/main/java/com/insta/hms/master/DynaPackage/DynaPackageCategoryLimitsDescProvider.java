package com.insta.hms.master.DynaPackage;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class DynaPackageCategoryLimitsDescProvider implements AuditLogDescProvider{

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("dyna_package_name", "Dyna Package Name");
		desc.addField("bed_type", "Bed Type");
		desc.addField("org_id", "Organization");
		desc.addField("charge", "Charge");

		desc.addFieldValue("org_id", "organization_details", "org_id", "org_name");
		return desc;
	}

}
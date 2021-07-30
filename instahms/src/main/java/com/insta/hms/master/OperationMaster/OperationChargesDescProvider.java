package com.insta.hms.master.OperationMaster;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class OperationChargesDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);


		desc.addField("operation_name", "Operation Name");
		desc.addField("org_id", "Organization", false);
		desc.addField("bed_type", "Bed Type", false);
		desc.addField("surg_asstance_charge", "Surgical Assistance Charge");
		desc.addField("surg_asst_discount", "Surgical Assistance Discount");
		desc.addField("surgeon_charge", "Surgeon Charge");
		desc.addField("surg_discount", "Surgeon Discount");
		desc.addField("anesthetist_charge", "Anesthetist Charge");
		desc.addField("anest_discount", "Anesthetist Discount");

		desc.addFieldValue("org_id", "organization_details", "org_id", "org_name");

		return desc;
	}
}
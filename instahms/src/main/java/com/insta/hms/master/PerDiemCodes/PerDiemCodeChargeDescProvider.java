/**
 *
 */
package com.insta.hms.master.PerDiemCodes;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

/**
 * @author lakshmi
 *
 */
public class PerDiemCodeChargeDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("per_diem_code", "Per diem code");
		desc.addField("per_diem_description", "Per diem code desc.");
		desc.addField("service_groups_names", "Included Service Groups");
		desc.addField("bed_type", "Bed Type");
		desc.addField("org_id", "Organization");
		desc.addField("charge", "Charge");

		desc.addFieldValue("org_id", "organization_details", "org_id", "org_name");
		return desc;
	}

}

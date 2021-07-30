package com.insta.hms.OTServices.OtRecord;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

public class OTAuditViewDescProvider extends InstaSectionAuditViewDescProvider{

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {

		AuditLogDesc desc = getCommonFeilds(tableName);
		desc.addField("section_item_id", "Operation Proc. Id");
		return desc;
	}
}

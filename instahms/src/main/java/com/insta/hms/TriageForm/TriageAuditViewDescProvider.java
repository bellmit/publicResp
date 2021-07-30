package com.insta.hms.TriageForm;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;


public class TriageAuditViewDescProvider extends InstaSectionAuditViewDescProvider{

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {

		AuditLogDesc desc = getCommonFeilds(tableName);
		desc.addField("section_item_id", "Cons. Id");
		return desc;
	}
}
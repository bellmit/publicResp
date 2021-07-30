package com.insta.hms.GenericForms;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

public class GenericFormAuditViewDescProvider extends InstaSectionAuditViewDescProvider{

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = getCommonFeilds(tableName);
		desc.addField("generic_form_id", "Generic Form Id");
		return desc;
	}
}

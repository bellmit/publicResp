package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.auditlog.AuditLogAction;
import com.insta.hms.auditlog.AuditLogDao;

public class TestAuditLogAction extends AuditLogAction {

	protected AuditLogDao getAuditLogDao(String auditType, String tableName) throws Exception {
		return new TestAuditLogDAO(auditType, tableName);
	}

}

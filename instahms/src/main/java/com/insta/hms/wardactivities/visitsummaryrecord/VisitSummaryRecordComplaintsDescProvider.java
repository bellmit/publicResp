package com.insta.hms.wardactivities.visitsummaryrecord;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class VisitSummaryRecordComplaintsDescProvider implements AuditLogDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("complaint", "Complaint");
    desc.addField("secondary_complaint", "Secondary Complaint");
    desc.addField("visit_id", "Visit Id");
    desc.addField("description", "Description");

    return desc;
  }
}

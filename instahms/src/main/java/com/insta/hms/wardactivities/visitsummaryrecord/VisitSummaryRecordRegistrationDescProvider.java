package com.insta.hms.wardactivities.visitsummaryrecord;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class VisitSummaryRecordRegistrationDescProvider implements AuditLogDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("complaint", "Complaint");
    desc.addField("description", "Description");
    desc.addField("secondary_complaint", "Secondary Complaint");
    desc.addField("mr_no", "MR no");
    desc.addField("patient_id", "Patient Id");
    desc.addField("mod_time", "Modification Time");

    return desc;
  }
}

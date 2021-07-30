package com.insta.hms.diagnosticsmasters.addtest;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class DiagnosticTestChargesDescProvider implements AuditLogDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("test_name", "Test Name");
    desc.addField("org_name", "Organization");
    desc.addField("bed_type", "Bed Type");
    desc.addField("charge", "Charge");
    desc.addField("priority", "Priority");
    desc.addField("discount", "Discount");

    desc.addFieldValue("org_name", "organization_details", "org_id", "org_name");

    return desc;
  }

}
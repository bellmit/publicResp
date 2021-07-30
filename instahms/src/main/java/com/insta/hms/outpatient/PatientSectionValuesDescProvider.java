package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

/**
 * The Class PatientSectionValuesDescProvider.
 */
public class PatientSectionValuesDescProvider implements AuditLogDescProvider {

  public PatientSectionValuesDescProvider() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("section_detail_id", "Section Details Id");
    desc.addField("field_id", "Field Id");
    desc.addField("option_id", "Option Id");
    desc.addField("option_remarks", "Option Remarks");
    desc.addField("user_name", "Username");
    desc.addField("mod_time", "Modification Time");
    desc.addField("date_time", "Date Time");
    desc.addField("available", "Available");

    return desc;
  }

}

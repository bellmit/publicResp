package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

/**
 * The Class SectionDetailsDescProvider.
 */
public class SectionDetailsDescProvider implements AuditLogDescProvider {

  public SectionDetailsDescProvider() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("mr_no", "MR No.");
    desc.addField("patient_id", "Patient Id");
    desc.addField("section_item_id", "Section Item Id");
    desc.addField("section_id", "Section Id");

    desc.addField("item_type", "Item Type");
    desc.addField("section_detail_id", "Section Details Id");
    desc.addField("section_status", "Section Status");
    desc.addField("user_name", "Username");
    desc.addField("mod_time", "Modification Time");
    desc.addField("generic_form_id", "Generic Form Id");

    return desc;
  }

}

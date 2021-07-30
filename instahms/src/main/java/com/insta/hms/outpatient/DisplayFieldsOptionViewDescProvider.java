package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

/**
 * The Class DisplayFieldsOptionViewDescProvider.
 */

public class DisplayFieldsOptionViewDescProvider implements AuditLogDescProvider {

  public DisplayFieldsOptionViewDescProvider() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("section_id", "Section Name");
    desc.addField("section_title", "Section Title");

    desc.addField("section_detail_id", "Section Detail Id");
    desc.addField("psv_section_detail_id", "Section Detail Id");
    desc.addField("field_id", "Field Name");
    desc.addField("option_id", "Option Id");
    desc.addField("option_value", "Option", false, true);
    desc.addField("section_field_name", "Field Title", false, true);
    desc.addField("option_remarks", "Values");
    desc.addField("field_remarks", "Values");
    desc.addField("available", "Available");
    desc.addField("date", "Date");
    desc.addField("date_time", "Date & Time");

    // Add all lookup values from a db table
    desc.addFieldValue("section_id", "section_master", "section_id", "section_title");
    desc.addFieldValue("field_id", "section_field_desc", "field_id", "field_name");
    desc.addFieldValue("option_id", "section_field_options", "option_id", "option_value");

    return desc;
  }

}

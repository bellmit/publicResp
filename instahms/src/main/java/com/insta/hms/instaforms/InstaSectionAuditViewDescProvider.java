package com.insta.hms.instaforms;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class InstaSectionAuditViewDescProvider.
 */
public class InstaSectionAuditViewDescProvider extends MultiAuditLogDescProvider {

  /** The Constant tables. */
  private static final String[] tables = {"patient_section_details_audit_log",
      "patient_section_fields_audit_log", "patient_section_options_audit_log"};

  /**
   * Instantiates a new insta section audit view desc provider.
   */
  public InstaSectionAuditViewDescProvider() {
    super(tables);
  }

  /**
   * Gets the common feilds.
   *
   * @param tableName the table name
   * @return the common feilds
   */
  public AuditLogDesc getCommonFeilds(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("mr_no", "MR No.");
    desc.addField("patient_id", "Patient Id");
    desc.addField("section_title", "Section Title");
    desc.addField("section_detail_id", "Section Detail Id", false, false, "integer");
    desc.addField("psd_section_id", "Section Name");
    desc.addField("section_id", "Section Name", false, true, "String");
    desc.addField("psv_section_detail_id", "Section Detail Id");
    desc.addField("psd_finalized", "Finalized");

    // Add all lookup values from a db table
    desc.addFieldValue("psd_section_id", "section_master", "section_id", "section_title");
    desc.addFieldValue("section_id", "section_master", "section_id", "section_title");
    return desc;
  }
}

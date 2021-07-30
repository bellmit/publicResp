package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

/**
 * The Class AllergiesDescProvider.
 */
public class AllergiesDescProvider extends MultiAuditLogDescProvider {

  private static final String[] ALLERGY_AUDIT_VIEW_TABLES = new String[] {
      "patient_section_detail_audit_log", "patient_allergies_audit_log" };

  public AllergiesDescProvider() {
    super(ALLERGY_AUDIT_VIEW_TABLES);
  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("mr_no", "Mr No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("generic_form_id", "Generic Form Id");

    desc.addField("allergy_id", "Allergy Id");
    desc.addField("section_detail_id", "Section Detail Id");
    desc.addField("section_id", "Section Id");
    desc.addField("finalized", "Finalized");
    desc.addField("allergy_type_id", "Allergy Type");
    desc.addField("allergen_code_id", "Allergen Description");
    
    desc.addFieldValue("allergy_type_id", "allergy_type_master",
        "allergy_type_id","allergy_type_name");
    desc.addFieldValue("allergen_code_id", "allergen_master",
        "allergen_code_id","allergen_description");
    
    return desc;
  }

}

package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class PatientAllergiesDescProvider.
 */
public class PatientAllergiesDescProvider implements AuditLogDescProvider {

  public PatientAllergiesDescProvider() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("paal_allergy_id", "Allergy Id");
    desc.addField("allergy_id", "Allergy Id");

    Map<String, String> statusMap = new LinkedHashMap<String, String>();
    statusMap.put("A", "Active");
    statusMap.put("I", "Inactive");

    desc.addField("status", "Status");
    desc.addFieldValue("status", statusMap);
    desc.addField("reaction", "Reaction");
    desc.addField("severity", "Severity");
    desc.addField("onset_date", "Onset");
    
    desc.addField("allergy_type_id", "Allergy Type");
    desc.addField("allergen_code_id", "Allergen Description");
    desc.addFieldValue("allergy_type_id", "allergy_type_master",
        "allergy_type_id","allergy_type_name");
    desc.addFieldValue("allergen_code_id","(SELECT allergen_code_id,allergen_description "
        + " FROM allergen_master "
        + " UNION select allergen_code_id, generic_name from generic_name) as allergen_mas_comb",
        "allergen_code_id", "allergen_description");

    return desc;
  }
}
package com.insta.hms.insurance;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class InsurancePlanDetailsDescProvider.
 */
public class InsurancePlanDetailsDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the table name
   * @return the audit log desc
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    Map<String, String> patientTypeMap = new LinkedHashMap<String, String>();
    patientTypeMap.put("i", "InPatient");
    patientTypeMap.put("o", "OutPatient");

    desc.addField("insurance_category_id", "Plan Item Category Name", false);
    /*
     * desc.addField("patient_amount", "Patient Co-pay Fixed Amount(Per Item)",false);
     * desc.addField("patient_percent", "Patient Co-pay Percent",false);
     * desc.addField("patient_amount_cap", "Patient Amount Cap",false);
     */
    desc.addField("patient_type", "patient Type", false);
    // desc.addField("patient_amount_per_category", "Patient Co-pay Fixed Amount(Per
    // Category)",false);
    // desc.addField("per_treatment_limit", "Treatment Limit",false);

    desc.addField("patient_amount_per_category", "Deductible (Cat)", false);
    desc.addField("patient_amount", "Deductible (Item)", false);
    desc.addField("patient_percent", "Copay %", false);
    desc.addField("patient_amount_cap", "Max Copay", false);
    desc.addField("per_treatment_limit", "Sponsor Limit", false);

    desc.addFieldValue("patient_type", patientTypeMap);
    desc.addFieldValue("insurance_category_id", "item_insurance_categories",
        "insurance_category_id", "insurance_category_name");

    return desc;
  }

}

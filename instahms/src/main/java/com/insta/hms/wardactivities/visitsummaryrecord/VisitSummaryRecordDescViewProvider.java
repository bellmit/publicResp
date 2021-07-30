package com.insta.hms.wardactivities.visitsummaryrecord;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class VisitSummaryRecordDescViewProvider extends MultiAuditLogDescProvider {

  private static final String[] COMPLAINTS_AUDIT_VIEW_TABLES = new String[] {
      "patient_registration_audit_log", "secondary_complaints_audit_log" };

  public VisitSummaryRecordDescViewProvider() {
    super(COMPLAINTS_AUDIT_VIEW_TABLES);
  }
  
  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = super.getAuditLogDesc(tableName);

    Map<String, String> diagTypeMap = new LinkedHashMap<String, String>();
    diagTypeMap.put("S", "Secondary");
    diagTypeMap.put("P", "Primary");

    desc.addField("complaint", "Chief Complaint");
    desc.addField("secondary_complaint", "Other Complaint");
    desc.addField("icd_code", "ICD CODE");
    desc.addField("description", "Description");
    desc.addField("mod_time", "Modification Time");
    desc.addField("code_type", "Code Type");
    desc.addField("diag_type", "Diagnosis Type");
    desc.addFieldValue("diag_type", diagTypeMap);
    desc.addField("mr_no", "MR No");
    desc.addField("patient_id", "Patient Id");

    return desc;
  }

}

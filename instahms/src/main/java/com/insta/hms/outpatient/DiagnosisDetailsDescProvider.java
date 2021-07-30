package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

/**
 * The Class DiagnosisDetailsDescProvider.
 */
public class DiagnosisDetailsDescProvider extends MultiAuditLogDescProvider {

  private static final String[] DIAGNOSIS_DETAILS_AUDIT_VIEW_TABLES = new String[] {
      "patient_registration_audit_log", "mrd_diagnosis_audit_log" };

  public DiagnosisDetailsDescProvider() {
    super(DIAGNOSIS_DETAILS_AUDIT_VIEW_TABLES);
  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc auditLogDesc = new AuditLogDesc(tableName);
    auditLogDesc.addField("mr_no", "Mr No");

    auditLogDesc.addField("id", "Diagnosis Details Id");
    auditLogDesc.addField("patient_id", "Patient Id");

    return auditLogDesc;
  }
}

package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

/**
 * The Class AdmissionRequestDiagnosisDescProvider.
 */
public class AdmissionRequestDiagnosisDescProvider extends MultiAuditLogDescProvider {

  private static final String[] DIAGNOSIS_DETAILS_AUDIT_VIEW_TABLES = new String[] {
      "patient_admission_request_audit_log", "mrd_diagnosis_audit_log" };

  public AdmissionRequestDiagnosisDescProvider() {
    super(DIAGNOSIS_DETAILS_AUDIT_VIEW_TABLES);
  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc auditLogDesc = new AuditLogDesc(tableName);
    auditLogDesc.addField("mr_no", "Mr No");

    auditLogDesc.addField("id", "Diagnosis Details Id");
    auditLogDesc.addField("adm_request_id", "Admission Request Id");
    auditLogDesc.addField("request_date", "Request Date");
    auditLogDesc.addField("chief_complaint", "Chief Complaint");
    auditLogDesc.addField("admission_date", "Admission Date");
    auditLogDesc.addField("requesting_doc", "Doctor");
    auditLogDesc.addField("center_id", "Center Id");
    auditLogDesc.addField("status", "Status");
    auditLogDesc.addFieldValue("requesting_doc", "doctors", "doctor_id", "doctor_name");
    return auditLogDesc;
  }

}

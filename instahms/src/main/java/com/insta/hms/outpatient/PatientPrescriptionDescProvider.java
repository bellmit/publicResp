package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

/**
 * The Class PatientPrescriptionDescProvider.
 */
public class PatientPrescriptionDescProvider extends MultiAuditLogDescProvider {

  private static final String[] PRESCRIPTION_AUDIT_VIEW_TABLES = new String[] {
      "patient_prescriptions_details_audit_log_view" };

  public PatientPrescriptionDescProvider() {
    super(PRESCRIPTION_AUDIT_VIEW_TABLES);
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
    desc.addField("pres_id", "Prescription Id");
    desc.addField("type", "Prescription Type");
    desc.addField("section_detail_id", "Section Detail Id");
    desc.addField("section_id", "Section Id");
    desc.addField("op_medicine_pres_id", "Prescription Id");
    desc.addField("op_test_pres_id", "Prescription Id");
    desc.addField("op_service_pres_id", "Prescription Id");
    desc.addField("prescription_id", "Prescription Id");
    desc.addField("finalized", "Finalized");
    return desc;
  }

}

package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DoctorConsultationTriageFormDescProvider.
 */
public class DoctorConsultationTriageFormDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("consultation_id", "Consultation Id");
    desc.addField("mr_no", "MR No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("triage_done", "Triage Status");
    desc.addField("reopen_remarks_triage", "Triage reopen remarks");
    desc.addField("triage_complete_time", "Triage closing time");
    Map<String, String> triageStatus = new HashMap<>();
    triageStatus.put("N", "Open");
    triageStatus.put("P", "Partial");
    triageStatus.put("Y", "Closed");
    desc.addFieldValue("triage_done", triageStatus);
    return desc;
  }
}

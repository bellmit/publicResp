package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DoctorConsultationDescProvider.
 */
public class DoctorConsultationDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("consultation_id", "Consultation Id");
    desc.addField("mr_no", "Mr No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("status", "Status");
    desc.addField("reopen_remarks", "Reopen Remarks");
    desc.addField("consultation_complete_time", "Consultation Closure Time");

    Map<String, String> consultationStatus = new HashMap<>();
    consultationStatus.put("A", "Open");
    consultationStatus.put("P", "Partial");
    consultationStatus.put("C", "Closed");

    desc.addFieldValue("status", consultationStatus);
    return desc;
  }

}

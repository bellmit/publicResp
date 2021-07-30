package com.insta.hms.wardactivities.doctorsnotes;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class DoctorNotesDescProvider.
 */
public class DoctorNotesDescProvider implements AuditLogDescProvider {
  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    Map<String, String> billableConsultationMap = new LinkedHashMap<String, String>();
    billableConsultationMap.put("Y", "Yes");
    billableConsultationMap.put("N", "No");

    desc.addField("notes", "Doctor Notes");
    desc.addField("charge_id", "Charge ID");
    desc.addField("doctor_id", "Doctor Name");

    desc.addFieldValue("doctor_id", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("billable_consultation", billableConsultationMap);
    desc.addFieldValue("finalized", billableConsultationMap);
    desc.addFieldValue("highlighted", billableConsultationMap);
    desc.addFieldValue("consultation_type_id", "consultation_types", "consultation_type_id",
        "consultation_type");

    return desc;
  }

}

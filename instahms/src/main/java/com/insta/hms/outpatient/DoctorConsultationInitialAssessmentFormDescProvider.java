package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DoctorConsultationInitialAssessmentFormDescProvider.
 */
public class DoctorConsultationInitialAssessmentFormDescProvider implements AuditLogDescProvider {
  
  /* (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("consultation_id", "Consultation Id");
    desc.addField("mr_no", "MR No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("initial_assessment_status", "Initial Assessment Status");
    desc.addField("reopen_remarks_ia", "Initial Assessment reopen remarks");
    desc.addField("ia_complete_time", "Initial Assessment closing time");
    Map<String, String> iaStatus = new HashMap<>();
    iaStatus.put("N", "Open");
    iaStatus.put("P", "Partial");
    iaStatus.put("Y", "Closed");
    desc.addFieldValue("initial_assessment_status", iaStatus);
    return desc;
  }

}

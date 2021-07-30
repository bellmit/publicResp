package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class PatientDiagnosisDetailsDescProvider.
 */
public class PatientDiagnosisDetailsDescProvider implements AuditLogDescProvider {

  public PatientDiagnosisDetailsDescProvider() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    Map<String, String> diagnosisTypeMap = new LinkedHashMap<String, String>();
    diagnosisTypeMap.put("P", "Principal");
    diagnosisTypeMap.put("S", "Secondary");
    diagnosisTypeMap.put("V", "Reason for Visit");

    AuditLogDesc auditLogDesc = new AuditLogDesc(tableName);
    auditLogDesc.addField("id", "Diagnosis Details Id");
    auditLogDesc.addField("description", "Description");
    auditLogDesc.addField("icd_code", "Code");
    auditLogDesc.addField("code_type", "Code Type");
    auditLogDesc.addField("diag_type", "Diagnosis Type");
    auditLogDesc.addFieldValue("diag_type", diagnosisTypeMap);
    auditLogDesc.addField("diagnosis_status_id", "Status");
    auditLogDesc.addField("remarks", "Remarks");
    auditLogDesc.addField("doctor_id", "Doctor");
    auditLogDesc.addField("diagnosis_datetime", "Date");
    auditLogDesc.addField("adm_request_id", "Admission Request Id");
    auditLogDesc.addFieldValue("doctor_id", "doctors", "doctor_id", "doctor_name");
    auditLogDesc.addFieldValue("diagnosis_status_id", "diagnosis_statuses", "diagnosis_status_id",
        "diagnosis_status_name");

    return auditLogDesc;
  }
}

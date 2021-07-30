package com.insta.hms.GenericForms;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class GenericFormsAuditViewDescProvider.
 */
public class PatientFormDetailsAuditViewDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("form_detail_id", "Form Id");
    desc.addField("mr_no", "MR No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("reopen_remarks", "Reopen remarks");
    Map<String, String> formStatus = new HashMap<>();
    formStatus.put("N", "Open");
    formStatus.put("P", "Partial");
    formStatus.put("F", "Finalised");
    desc.addFieldValue("form_status", formStatus);
    return desc;
  }
}
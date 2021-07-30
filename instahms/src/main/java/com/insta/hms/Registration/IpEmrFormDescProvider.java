package com.insta.hms.Registration;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;
import java.util.HashMap;
import java.util.Map;

public class IpEmrFormDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("mr_no", "MR No");
    desc.addField("patient_id", "Patient Id");
    desc.addField("ipemr_status", "Status");
    desc.addField("ipemr_reopen_remarks", "Reopen remarks");
    desc.addField("ipemr_complete_time", "IP EMR Closure Time");
    Map<String, String> ipEmrStatus = new HashMap<>();
    ipEmrStatus.put("N", "Open");
    ipEmrStatus.put("P", "Partial");
    ipEmrStatus.put("C", "Closed");
    desc.addFieldValue("ipemr_status", ipEmrStatus);
    return desc;
  }
  
}

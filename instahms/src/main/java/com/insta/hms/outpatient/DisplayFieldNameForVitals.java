package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

/**
 * The Class DisplayFieldNameForVitals.
 *
 * @author krishna
 *
 */
public class DisplayFieldNameForVitals implements AuditLogDescProvider {

  public DisplayFieldNameForVitals() {

  }

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("vital_reading_id", "Vital Reading Id");
    desc.addField("patient_id", "Patient Id");
    desc.addField("mr_no", "MR No.");
    desc.addField("date_time", "Date and Time");
    desc.addField("user_name", "User Name");
    desc.addField("vital_reading_id", "Vital Reading Id");
    desc.addField("param_value", "Parameter Value", true);
    desc.addField("param_id", "Field Name");
    desc.addField("param_label", "Field Title", false, true);
    desc.addFieldValue("param_id", "vital_parameter_master", "param_id", "param_label");

    return desc;
  }

}

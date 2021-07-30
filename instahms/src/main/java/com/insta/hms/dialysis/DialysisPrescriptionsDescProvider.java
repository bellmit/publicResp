package com.insta.hms.dialysis;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class DialysisPrescriptionsDescProvider implements AuditLogDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    Map<String, String> statusMap = new LinkedHashMap<>();
    statusMap.put("A", "Active");
    statusMap.put("I", "InActive");
    statusMap.put("P", "Pending");
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("end_date", "End Date");
    desc.addField("target_weight", "Dry Weight");
    desc.addField("status", "Status");
    desc.addField("mr_no", "Mr Number");
    desc.addField("patient_name", "Patient Name");
    desc.addField("presc_doctor", "Doctor");
    desc.addField("dialysate_type_id", "Dialysate Type");
    desc.addField("dialyzer_type_id", "Dialyzer Type");
    desc.addField("access_type_id", "Access Type");
    desc.addField("access_site_id", "Access Site");
    desc.addField("access_type_id_p", "Permanent Access Type (Permanent Access)");
    desc.addField("date_of_intiation_p", "Initiation Date (Permanent Access)");
    desc.addField("access_site_p", "Access Site (Permanent Access)");
    desc.addField("date_of_removal_p", "Removal Date (Permanent Access)");
    desc.addField("reason_p", "Reason (Permanent Access)");
    desc.addField("doctor_name_p", "Doctor Name (Permanent Access)");
    desc.addField("access_type_id_t", "Access Type (Temporary Access)");
    desc.addField("date_of_intiation_t", "Initiation Date (Temporary Access)");
    desc.addField("access_site_t", "Access Site (Temporary Access)");
    desc.addField("date_of_failure_t", "Removal Date (Temporary Access)");
    desc.addField("reason_t", "Reason (Temporary Access)");
    desc.addField("doctor_name_t", "Doctor Name (Temporary Access)");

    desc.addFieldValue("status", statusMap);

    desc.addFieldValue("presc_doctor", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("dialysate_type_id", "dialysate_type", "dialysate_type_id",
        "dialysate_type_name");
    desc.addFieldValue("dialyzer_type_id", "dialyzer_types", "dialyzer_type_id",
        "dialyzer_type_name");
    desc.addFieldValue("access_type_id", "dialysis_access_types", "access_type_id", "access_type");
    desc.addFieldValue("access_site_id", "dialysis_access_sites", "access_site_id", "access_site");
    desc.addFieldValue("access_type_id_p", "dialysis_access_types", "access_type_id",
        "access_type");
    desc.addFieldValue("access_type_id_t", "dialysis_access_types", "access_type_id",
        "access_type");
    desc.addFieldValue("doctor_name_t", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("doctor_name_p", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("access_site_t", "dialysis_access_sites", "access_site_id", "access_site");
    desc.addFieldValue("access_site_p", "dialysis_access_sites", "access_site_id", "access_site");

    return desc;
  }
}
package com.insta.hms.wardactivities.prescription;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class DoctorOrderAuditDescProvider.
 *
 * @author krishna
 *
 */
public class DoctorOrderAuditDescProvider implements AuditLogDescProvider {
  
  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("patient_id", "Patient Id.", true);
    desc.addField("entered_by", "Entered By");
    desc.addField("entered_datetime", "Entered Time", false);
    desc.addField("mod_time", "Mod. Time", false);
    desc.addField("username", "User Name", true);

    desc.addField("med_dosage", "Med. Dosage", false);
    desc.addField("med_strength", "Med. Strength", false);
    desc.addField("prior_med", "Prior Medication", false);
    desc.addField("remarks", "Remarks", false);
    desc.addField("freq_type", "Frequency Type", false);
    desc.addField("repeat_interval", "Repeat Interval", false);
    desc.addField("start_datetime", "Start Time");
    desc.addField("end_datetime", "End Time", false);
    desc.addField("no_of_occurrences", "No. of occurrences", false);
    desc.addField("end_on_discontinue", "End on Discontinue", false);
    desc.addField("discontinued", "Discontinued", true);
    desc.addField("repeat_interval_units", "Repeat Interval Units", false);

    desc.addField("med_route", "Medicine Route", false);
    desc.addField("med_form_id", "Medicine Form", false);
    desc.addField("recurrence_daily_id", "Frequency", false);
    desc.addField("doctor_id", "Prescribed By");

    desc.addField("item_name", "Other Item Name");

    Map<String, String> itemTypeMap = new LinkedHashMap<String, String>();
    itemTypeMap.put("M", "Medicine");
    itemTypeMap.put("I", "Investigation");
    itemTypeMap.put("S", "Service");
    itemTypeMap.put("C", "Consultation");
    itemTypeMap.put("O", "Others");

    desc.addField("presc_type", "Prescription Type", false);
    desc.addFieldValue("presc_type", itemTypeMap);
    desc.addField("item_id", "Item Name", true, "presc_type");

    desc.addFieldValue("item_id", "M", "store_item_details", "medicine_id", "medicine_name");
    desc.addFieldValue("item_id", "I", "all_tests_pkgs_view", "test_id", "test_name");
    desc.addFieldValue("item_id", "S", "services", "service_id", "service_name");
    desc.addFieldValue("item_id", "C", "doctors", "doctor_id", "doctor_name");

    desc.addFieldValue("doctor_id", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("med_route", "medicine_route", "route_id", "route_name");
    desc.addFieldValue("med_form_id", "item_form_master", "item_form_id", "item_form_name");
    desc.addFieldValue("recurrence_daily_id", "recurrence_daily_master", "recurrence_daily_id",
        "display_name");

    return desc;
  }

}

package com.insta.hms.wardactivities;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class PatientActivitiesAuditDescProvider.
 * 
 * @author krishna
 *
 */
public class PatientActivitiesAuditDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName          the tableName
   * @return the audit log desc
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("patient_id", "Patient Id");
    desc.addField("due_date", "Due Date");
    desc.addField("med_batch", "Med. Batch", false);
    desc.addField("med_exp_date", "Med. Expiry Date", false);

    Map<String, String> activityTypeMap = new LinkedHashMap<String, String>();
    activityTypeMap.put("P", "Prescription");
    activityTypeMap.put("G", "General Activity");

    desc.addField("activity_type", "Activity Type", false);
    desc.addFieldValue("activity_type", activityTypeMap);

    Map<String, String> activityStatusMap = new LinkedHashMap<String, String>();
    activityStatusMap.put("S", "Set-Up");
    activityStatusMap.put("P", "In-Progress");
    activityStatusMap.put("D", "Done");
    activityStatusMap.put("X", "Cancelled");

    desc.addField("activity_status", "Activity Status");
    desc.addFieldValue("activity_status", activityStatusMap);

    Map<String, Object> ivStatusMap = new HashMap<>();
    ivStatusMap.put("S", "Started");
    ivStatusMap.put("R", "Re Started");
    ivStatusMap.put("P", "Paused");
    ivStatusMap.put("X", "Stopped");

    desc.addField("iv_status", "IV Status");
    desc.addFieldValue("iv_status", ivStatusMap);

    desc.addField("infusion_site", "Infusion Site", false);
    desc.addField("order_no", "Prescription Id.", false);
    desc.addField("activity_remarks", "Activity Remarks", false);
    desc.addField("added_by", "Added By", false);
    desc.addField("mod_time", "Mod. Time", false);
    desc.addField("username", "User Name", false);
    desc.addField("completed_date", "Completed Date", true);
    desc.addField("completed_by", "Completed By");
    desc.addField("ordered_datetime", "Ordered Time", false);
    desc.addField("gen_activity_details", "Item Name", false);
    desc.addField("ordered_by", "Ordered By", false);
    desc.addField("activity_id", "Activity Id", false);
    desc.addField("presc_doctor_id", "Presc. Doctor", false);

    Map<String, String> itemTypeMap = new LinkedHashMap<String, String>();
    itemTypeMap.put("M", "Medicine");
    itemTypeMap.put("I", "Investigation");
    itemTypeMap.put("S", "Service");
    itemTypeMap.put("C", "Consultation");
    itemTypeMap.put("O", "Others");

    desc.addField("prescription_type", "Prescription Type", false);
    desc.addFieldValue("prescription_type", itemTypeMap);
    desc.addField("prescription_id", "Item Name", true);

    desc.addFieldValue("presc_doctor_id", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("prescription_id", "doctor_order_item_names_view", "prescription_id",
        "item_name");
    desc.addFieldValue("infusion_site", "iv_infusionsites", "id", "site_name");

    return desc;
  }

}

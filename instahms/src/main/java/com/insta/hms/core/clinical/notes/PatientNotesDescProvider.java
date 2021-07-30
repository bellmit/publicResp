package com.insta.hms.core.clinical.notes;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

public class PatientNotesDescProvider implements AuditLogDescProvider {

  protected static final String CONSULTATION_TYPE_ID = "consultation_type_id";
  protected static final String NOTE_TYPE_ID = "note_type_id";

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("note_id", "Note Id");
    desc.addField("patient_id", "Patient Id");
    desc.addField("billable_consultation", "Billable Consultation");
    desc.addField("charge_id", "Charge Id");
    desc.addField("save_status", "Save Status");
    desc.addField("original_note_id", "Original Note Id");
    desc.addField("new_note_id", "New Note Id");
    desc.addField("on_behalf_user", "On Behalf of User");
    desc.addField("created_by", "Created User");
    desc.addField("created_time", "Created Time");
    desc.addField("mod_user", "Modified User");
    desc.addField("mod_time", "Modified Time");
    desc.addField("on_behalf_doctor_id", "Doctor Name");
    desc.addField(NOTE_TYPE_ID, "Note Type Name");
    desc.addField(CONSULTATION_TYPE_ID, "Consultation Type");

    desc.addFieldValue(CONSULTATION_TYPE_ID, "consultation_types", CONSULTATION_TYPE_ID,
        "consultation_type");
    desc.addFieldValue("on_behalf_doctor_id", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue(NOTE_TYPE_ID, "note_type_master", NOTE_TYPE_ID, "note_type_name");

    Map<String, String> yesNoStatus = new HashMap<>();
    yesNoStatus.put("Y", "Yes");
    yesNoStatus.put("N", "No");

    Map<String, String> saveStatus = new HashMap<>();
    saveStatus.put("D", "Draft");
    saveStatus.put("F", "Final");

    desc.addFieldValue("billable_consultation", yesNoStatus);
    desc.addFieldValue("save_status", saveStatus);
    return desc;
  }

}

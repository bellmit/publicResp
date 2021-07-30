package com.insta.hms.common.preferences.clinicalpreferences;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ClinicalPreferencesDescProvider.
 */
public class ClinicalPreferencesDescProvider implements AuditLogDescProvider {

  /* (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("op_prescription_format", "OP Prescription Format");
    desc.addField("allow_op_prescription_format_override", "Allow Override (OP Prescription)");
    desc.addField("op_prescription_validity", "OP Prescription Validity");
    desc.addField("op_prescription_validity_period", "Validity Period(hour)");
    
    desc.addField("op_allow_template", "OP Consultation: Allow Templates");
    desc.addField("op_allow_template_save_with_data", 
        "OP Consultation: Allow Template to save with data");
    desc.addField("triage_allow_template", "Triage/Nurse: Allow Templates");
    desc.addField("triage_allow_template_save_with_data", 
        "Triage/Nurse: Allow Template to save with data");
    desc.addField("op_consultation_auto_closure", "Autoclosure of consultation validity");
    desc.addField("op_consultation_auto_closure_period", 
        "Autoclosure of consultation validity in hours");
    desc.addField("nurse_staff_ward_assignments_applicable", "Apply Nurse Rules");
    desc.addField("op_consultation_edit_across_doctors", "Consultation edit across doctors (OP)");
    desc.addField("consultation_reopen_time_limit", "Consultation reopen time limit in hours");
    desc.addField("consultation_validity_units", "Consultation Validity Units");
    
    desc.addField("historic_vitals_period", "Historical vitals display period");
    desc.addField("historic_vitals_period_unit", "Historical vitals display period unit");
    
    desc.addField("ip_prescription_format", "IP Prescription Format");
    desc.addField("allow_ip_prescription_format_override", "Allow Override (IP Prescription)");
    desc.addField("ip_allow_template", "IP EMR: Allow Templates");
    desc.addField("ip_allow_template_save_with_data", "IP EMR: Allow Template to save with data");
    desc.addField("ip_cases_across_doctors", "IP Cases Across Doctors");
    
    Map<String, String> yesNoStatus = new HashMap<>();
    yesNoStatus.put("Y", "Yes");
    yesNoStatus.put("N", "No");

    Map<String, String> basicAdvanceStatus = new HashMap<>();
    basicAdvanceStatus.put("B", "Basic");
    basicAdvanceStatus.put("A", "Advance");
    
    Map<String, String> validityUnitTypeStatus = new HashMap<>();
    validityUnitTypeStatus.put("D", "Days");
    validityUnitTypeStatus.put("T", "Time Based");
    
    Map<String, String> displayPeriodStatus = new HashMap<>();
    displayPeriodStatus.put("D", "Days");
    displayPeriodStatus.put("M", "Months");

    desc.addFieldValue("op_prescription_format", basicAdvanceStatus);
    desc.addFieldValue("op_prescription_validity", yesNoStatus);
    desc.addFieldValue("op_consultation_auto_closure", yesNoStatus);
    desc.addFieldValue("op_prescription_allow_override", yesNoStatus);
    desc.addFieldValue("op_allow_template", yesNoStatus);
    desc.addFieldValue("op_allow_template_save_with_data", yesNoStatus);
    desc.addFieldValue("triage_allow_template", yesNoStatus);
    desc.addFieldValue("triage_allow_template_save_with_data", yesNoStatus);
    desc.addFieldValue("op_consultation_edit_across_doctors", yesNoStatus);
    desc.addFieldValue("nurse_staff_ward_assignments_applicable", yesNoStatus);
    desc.addFieldValue("consultation_validity_units", validityUnitTypeStatus);
    desc.addFieldValue("historic_vitals_period_unit", displayPeriodStatus);
    desc.addFieldValue("ip_prescription_format", basicAdvanceStatus);
    desc.addFieldValue("ip_allow_template", yesNoStatus);
    desc.addFieldValue("ip_allow_template_save_with_data", yesNoStatus);
    desc.addFieldValue("ip_cases_across_doctors", yesNoStatus);
    
    return desc;
  }

}

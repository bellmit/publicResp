package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class PrescriptionDetailsDescProvider.
 */
public class PrescriptionDetailsDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    // Prescription Ids
    desc.addField("prescription_id", "Prescription Id");
    desc.addField("pbm_presc_id", "Prescription Id");
    desc.addField("op_test_pres_id", "Prescription Id");
    desc.addField("op_service_pres_id", "Prescription Id");
    desc.addField("patient_presc_id", "Prescription Id");
    desc.addField("type", "Prescription Type");

    // Prescription Fields
    desc.addField("consultation_id", "Consultation Id");
    desc.addField("prescribed_date", "Prescribed Date");
    desc.addField("cancelled_datetime", "Cancelled Date");
    desc.addField("cancelled_by", "Cancelled By");
    desc.addField("external_order_no", "External Order No");
    desc.addField("special_instr", "Special Instructions");
    desc.addField("store_item", "Store Item");

    // Common
    desc.addField("activity_due_date", "Activity Due Date");
    desc.addField("admin_strength", "Admin Strength");
    desc.addField("preauth_required", "Pre Auth Required");
    desc.addField("frequency", "Frequency");
    desc.addField("medicine_quantity", "Medicine Quantity");
    desc.addField("item_strength", "Strength");
    desc.addField("item_form_id", "Item Form");
    desc.addField("duration", "Duration");
    desc.addField("duration_units", "Duration Units");
    desc.addField("item_strength_units", "Item Strength Units");
    desc.addField("consumption_uom", "Consumption UOM");
    desc.addField("medicine_remarks", "Medicine Instructions");
    desc.addField("status", "Status");
    desc.addField("refills", "Refills");
    desc.addField("time_of_intake", "Time of Intake Instructions");
    desc.addField("start_date", "Start Date");
    desc.addField("end_date", "End Date");
    desc.addField("priority", "Priority");
    desc.addField("doctor_id", "Prescribing Doctor");
    desc.addField("prior_med", "Prior Medication");
    desc.addField("freq_type", "Frequency Type");
    desc.addField("recurrence_daily_id", "Frequency");
    desc.addField("repeat_interval", "Interval");
    desc.addField("start_datetime", "Start Date & Time");
    desc.addField("end_datetime", "End Date & Time");
    desc.addField("no_of_occurrences", "Occurrences");
    desc.addField("end_on_discontinue", "Till Discontinued");
    desc.addField("discontinued", "Discontinued");
    desc.addField("repeat_interval_units", "Interval Units");
    desc.addField("adm_request_id", "Admission Request Id");
    desc.addField("visit_id", "Visit Id");

    // Medicine
    desc.addField("issued_qty", "Issued Qty");
    desc.addField("medicine_id", "Medicine Name");
    desc.addField("strength", "Dosage");
    desc.addField("generic_code", "Generic Name");
    desc.addField("erx_status", "ERX Status");
    desc.addField("erx_denial_code", "ERX Denial Code");
    desc.addField("erx_denial_remarks", "ERX Denial Remarks");
    desc.addField("erx_approved_quantity", "ERX Approved Quantity");
    desc.addField("op_medicine_pres_id", "Medical Prescription Id");
    desc.addField("send_for_erx", "Send for ERX");
    desc.addField("final_sale_id", "Final Sale Id");
    desc.addField("initial_sale_id", "Initial Sale Id");

    // Test
    desc.addField("test_remarks", "Test Instructions");
    desc.addField("ispackage", "Package");
    desc.addField("test_id", "Inv. Name");

    // Service
    desc.addField("service_remarks", "Service Instructions");
    desc.addField("service_id", "Service Name");
    desc.addField("tooth_unv_number", "Tooth UNV Number");
    desc.addField("tooth_fdi_number", "Tooth FDI Number");
    desc.addField("qty", "Quantity");

    // Consultation
    desc.addField("cons_remarks", "Consultation Instructions");
    desc.addField("doctor_id", "Doctor Name");

    // Operation
    desc.addField("remarks", "Operation Instructions");
    desc.addField("operation_id", "Operation Name");

    // Other
    desc.addField("item_name", "Item Name");
    desc.addField("item_remarks", "Item Instructions");
    desc.addField("non_hosp_medicine", "Not Hospital Medicine");

    // Other Medicine
    desc.addField("medicine_name", "Medicine Name");
    desc.addField("route_of_admin", "Route");

    // ERX Status Values
    Map<String, String> erxStatusMap = new HashMap<String, String>();
    erxStatusMap.put("O", "Open");
    erxStatusMap.put("D", "Denied");
    erxStatusMap.put("C", "Closed");

    // Duration Units
    Map<String, String> durationUnitsMap = new HashMap<String, String>();
    durationUnitsMap.put("D", "Days");
    durationUnitsMap.put("W", "Weeks");
    durationUnitsMap.put("M", "Months");

    // Prescription Status
    Map<String, String> prescriptionStatusMap = new HashMap<String, String>();
    prescriptionStatusMap.put("PA", "Partially ordered");
    prescriptionStatusMap.put("O", "Ordered");
    prescriptionStatusMap.put("P", "Pending");

    // Boolean Status
    Map<String, String> booleanStatusMap = new HashMap<String, String>();
    booleanStatusMap.put("t", "True");
    booleanStatusMap.put("f", "False");

    // Yes No Status
    Map<String, String> yesNoStatusMap = new HashMap<String, String>();
    yesNoStatusMap.put("Y", "Yes");
    yesNoStatusMap.put("N", "No");

    // Priority Status
    Map<String, String> priorityStatusMap = new HashMap<>();
    priorityStatusMap.put("N", "Regular");
    priorityStatusMap.put("P", "PRN/SOS");
    priorityStatusMap.put("S", "Stat");
    priorityStatusMap.put("U", "Urgent");

    // Time of Intake Values
    Map<String, String> timeofintakeMap = new HashMap<>();
    timeofintakeMap.put("N", "None");
    timeofintakeMap.put("W", "With Food");
    timeofintakeMap.put("B", "Before Food");
    timeofintakeMap.put("A", "After Food");

    // Interval Units
    Map<String, String> intervalUnitsMap = new HashMap<String, String>();
    intervalUnitsMap.put("D", "Days");
    intervalUnitsMap.put("H", "Hours");
    intervalUnitsMap.put("M", "Minutes");

    // Freq Types
    Map<String, String> freqTypesMap = new HashMap<String, String>();
    freqTypesMap.put("F", "Frequency");
    freqTypesMap.put("R", "Repeat At Interval");

    desc.addFieldValue("erx_status", erxStatusMap);
    desc.addFieldValue("duration_units", durationUnitsMap);
    desc.addFieldValue("status", prescriptionStatusMap);
    desc.addFieldValue("store_item", booleanStatusMap);
    desc.addFieldValue("non_hosp_medicine", booleanStatusMap);
    desc.addFieldValue("ispackage", booleanStatusMap);
    desc.addFieldValue("preauth_required", yesNoStatusMap);
    desc.addFieldValue("send_for_erx", yesNoStatusMap);
    desc.addFieldValue("priority", priorityStatusMap);
    desc.addFieldValue("time_of_intake", timeofintakeMap);
    desc.addFieldValue("repeat_interval_units", intervalUnitsMap);
    desc.addFieldValue("freq_type", freqTypesMap);
    desc.addFieldValue("discontinued", yesNoStatusMap);
    desc.addFieldValue("end_on_discontinue", yesNoStatusMap);

    // Foreign key values
    desc.addFieldValue("medicine_id", "store_item_details", "medicine_id", "medicine_name");
    desc.addFieldValue("doctor_id", "doctors", "doctor_id", "doctor_name");
    desc.addFieldValue("test_id", "diagnostics", "test_id", "test_name");
    desc.addFieldValue("operation_id", "operation_master", "op_id", "operation_name");
    desc.addFieldValue("service_id", "services", "service_id", "service_name");
    desc.addFieldValue("item_form_id", "item_form_master", "item_form_id", "item_form_name");
    desc.addFieldValue("generic_code", "generic_name", "generic_code", "generic_name");
    desc.addFieldValue("item_strength_units", "strength_units", "unit_id", "unit_name");
    desc.addFieldValue("route_of_admin", "medicine_route", "route_id", "route_name");
    desc.addFieldValue("recurrence_daily_id", "recurrence_daily_master", "recurrence_daily_id",
        "display_name");

    return desc;
  }

}

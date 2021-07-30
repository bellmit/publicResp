/**
 *
 */

package com.insta.hms.auditlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuditLogHelper {
  static final Logger logger = LoggerFactory.getLogger(AuditLogHelper.class);
  public static final String DEFAULT_AUDITLOG_DESC_PROVIDER =
      "com.insta.hms.auditlog.BasicAuditLogDescProvider";
  private static Map<String, Map<String, String>> tableMap =
      new HashMap<String, Map<String, String>>();
  private static Map<String, String> providerMap = new HashMap<String, String>();

  static {

    addAuditLogTable("billing", "bill_audit_log", "Bill", "com.insta.hms.billing.BillDescProvider");
    addAuditLogTable("billing", "bill_charge_audit_log", "Bill Charge",
        "com.insta.hms.billing.BillChargeDescProvider");
    addAuditLogTable("billing", "bill_audit_view", "Bill",
        "com.insta.hms.billing.BillAuditViewDescProvider");

    addAuditLogTable("registration", "patient_details_audit_log", "Patient Details",
        "com.insta.hms.Registration.PatientDetailsDescProvider");
    addAuditLogTable("registration", "patient_registration_audit_log", "Patient Visits",
        "com.insta.hms.Registration.PatientVisitDescProvider");

    addAuditLogTable("stores", "store_item_batch_details_audit_log", "Store Item Batch",
        "com.insta.hms.stores.StoresItemBatchDescProvider");

    addAuditLogTable("laboratory", "tests_prescribed_audit_log", "Tests Prescribed",
        "com.insta.hms.diagnosticmodule.common.TestsPrescribedDescProvider");
    addAuditLogTable("laboratory", "tests_conducted_audit_log", "Tests Conducted",
        "com.insta.hms.diagnosticmodule.common.TestsConductedDescProvider");
    addAuditLogTable("laboratory", "test_details_audit_log", "Test Details",
        "com.insta.hms.diagnosticmodule.common.TestDetailsDescProvider");
    addAuditLogTable("laboratory", "test_visit_reports_audit_log", "Test Reports",
        "com.insta.hms.diagnosticmodule.common.TestVisitReportsDescProvider");
    addAuditLogTable("laboratory", "sample_collection_audit_log", "Sample Collection",
        "com.insta.hms.diagnosticmodule.common.SampleCollectionDescProvider");

    addAuditLogTable("radiology", "tests_prescribed_audit_log", "Tests Prescribed",
        "com.insta.hms.diagnosticmodule.common.TestsPrescribedDescProvider");
    addAuditLogTable("radiology", "tests_conducted_audit_log", "Tests Conducted",
        "com.insta.hms.diagnosticmodule.common.TestsConductedDescProvider");
    addAuditLogTable("radiology", "test_details_audit_log", "Test Details",
        "com.insta.hms.diagnosticmodule.common.TestDetailsDescProvider");
    addAuditLogTable("radiology", "test_visit_reports_audit_log", "Test Reports",
        "com.insta.hms.diagnosticmodule.common.TestVisitReportsDescProvider");

    addAuditLogTable("payments", "payments_audit_log", "Payments",
        "com.insta.hms.payments.PaymentsDescProvider");
    addAuditLogTable("payments", "payments_details_audit_log", "Payment Details",
        "com.insta.hms.payments.PaymentDetailsDescProvider");

    addAuditLogTable("paymentrule", "payment_rules_audit_log", "Payment Rules",
        "com.insta.hms.master.PaymentRule.PaymentRuleDescProvider");

    addAuditLogTable("operations", "operation_master_audit_log_view", "Operation Master",
        "com.insta.hms.master.OperationMaster.OperationMasterDescProvider");
    addAuditLogTable("operations", "operation_charges_audit_log_view", "Operation Charges",
        "com.insta.hms.master.OperationMaster.OperationChargesDescProvider");

    addAuditLogTable("services", "services_audit_log_view", "Service Master",
        "com.insta.hms.master.ServiceMaster.ServiceMasterDescProvider");
    addAuditLogTable("services", "service_master_charges_audit_log_view", "Service Charges",
        "com.insta.hms.master.ServiceMaster.ServiceChargeDescProvider");

    addAuditLogTable("dynapackagescategorylimit", "dyna_package_category_limits_audit_log_view",
        "Dyna Package Category Limit",
        "com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDescProvider");
    addAuditLogTable("dynapackagescharge", "dyna_package_charges_audit_log_view",
        "Dyna Package Charge", "com.insta.hms.master.DynaPackage.DynaPackageChargeDescProvider");

    addAuditLogTable("diagnosticTests", "diagnostics_audit_log_view", "Diagnostic Tests",
        "com.insta.hms.diagnosticsmasters.addtest.DiagnosticTestMasterDescProvider");
    addAuditLogTable("diagnosticTests", "diagnostic_charges_audit_log_view", "Diagnostic Charges",
        "com.insta.hms.diagnosticsmasters.addtest.DiagnosticTestChargesDescProvider");

    addAuditLogTable("schedulerAppointments", "scheduler_appointments_audit_log",
        "Scheduler Appointments",
        "com.insta.hms.resourcescheduler.SchedulerAppointmentsDescProvider");
    addAuditLogTable("schedulerAppointments", "scheduler_appointment_items_audit_log",
        "Scheduler Appointment Resources",
        "com.insta.hms.resourcescheduler.AppointmentItemsDescProvider");

    addAuditLogTable("perdiemcodecharges", "per_diem_codes_charges_audit_log_view",
        "Per Diem Charges", "com.insta.hms.master.PerDiemCodes.PerDiemCodeChargeDescProvider");

    addAuditLogTable("allReceipts", "all_receipts_audit_view", "All Receipts",
        "com.insta.hms.billing.AllReceiptDetailsDescProvider");

    addAuditLogTable("dialysis", "dialysis_prescriptions_audit_log", "Dialysis Prescriptions",
        "com.insta.hms.dialysis.DialysisPrescriptionsDescProvider");

    addAuditLogTable("patientdocs", "patient_general_docs_audit_log", "Patient Documents",
        "com.insta.hms.genericdocuments.GenericDocsDescProvider");
    addAuditLogTable("patientdocs", "patient_pdf_form_doc_values_audit_log",
        "Patient Docs PDF values",
        "com.insta.hms.genericdocuments.GenericDocsPDFValuesDescProvider");
    addAuditLogTable("patientdocs", "patient_general_docs_audit_view", "Patient Documents",
        "com.insta.hms.genericdocuments.GenericDocumentsAuditDescProvider");
    addAuditLogTable("patientactivities", "patient_activities_audit_log", "Patient Activities",
        "com.insta.hms.wardactivities.PatientActivitiesAuditDescProvider");
    addAuditLogTable("doctororder", "doctor_order_audit_log", "Doctor Order",
        "com.insta.hms.wardactivities.prescription.DoctorOrderAuditDescProvider");
    addAuditLogTable("doctor_notes", "doctor_notes_audit_log", "Doctor Notes",
        "com.insta.hms.wardactivities.doctorsnotes.DoctorNotesDescProvider");
    addAuditLogTable("nurse_notes", "nurse_notes_audit_log", "Nurse Notes",
        "com.insta.hms.wardactivities.nursenotes.NurseNotesDescProvider");
    addAuditLogTable("intakeoutputaudit", "intake_output_audit_log_view", "Intake Output",
        "com.insta.hms.vitalForm.IntakeOutputDescProvider");

    addAuditLogTable("vitalformaudit", "vital_reading_fields_options_view", "Vital Reading Form",
        "com.insta.hms.outpatient.DisplayFieldNameForVitals");
    addAuditLogTable("vitalformaudit", "visit_vitals_audit_log", "Visit Vitals Form",
        "com.insta.hms.vitalForm.VisitVitalFormDescProvider");
    addAuditLogTable("vitalformaudit", "vital_reading_audit_log", "Vital Reading Form",
        "com.insta.hms.vitalForm.VitalReadingFormDescProvider");
    addAuditLogTable("vitalformaudit", "patient_vitals_audit_log_view", "Vital Form",
        "com.insta.hms.vitalForm.VitalFormDescProvider");

    addAuditLogTable("insuranceMainPlan", "insurance_plan_main_audit_log", "Insurance Main Plan",
        "com.insta.hms.insurance.InsuranceMainPlanDescProvider");
    addAuditLogTable("insuranceDetailsPlan", "insurance_plan_details_audit_log",
        "Plan Item Category Details", "com.insta.hms.insurance.InsurancePlanDetailsDescProvider");

    addAuditLogTable("stores", "store_po_main_audit_log", "PO",
        "com.insta.hms.stores.PODescProvider");

    addAuditLogTable("instaformaudit", "patient_section_details_audit_log", "Insta Sections",
        "com.insta.hms.outpatient.SectionDetailsDescProvider");
    addAuditLogTable("instaformaudit", "patient_section_values_audit_log", "Insta Sections",
        "com.insta.hms.outpatient.PatientSectionValuesDescProvider");
    addAuditLogTable("instaformaudit", "display_fields_options_view", "Insta Sections",
        "com.insta.hms.outpatient.DisplayFieldsOptionViewDescProvider");
    addAuditLogTable("instaformaudit", "cons_audit_view", "Consultation Sections",
        "com.insta.hms.outpatient.ConsultationAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "triage_audit_view", "Triage Sections",
        "com.insta.hms.TriageForm.TriageAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "ia_audit_view", "Initial Assessment Sections",
        "com.insta.hms.initialassessment.InitialAssessmentAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "ip_audit_view", "IP Record Sections",
        "com.insta.hms.wardactivities.visitsummaryrecord.IPAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "ot_audit_view", "OT Record Sections",
        "com.insta.hms.OTServices.OtRecord.OTAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "serv_audit_view", "Service Sections",
        "com.insta.hms.services.ServicesAuditViewDescProvider");
    addAuditLogTable("instaformaudit", "gen_audit_view", "Generic Sections",
        "com.insta.hms.GenericForms.GenericFormAuditViewDescProvider");
    addAuditLogTable("genericform", "patient_form_details_audit_log", "Generic Form ",
        "com.insta.hms.GenericForms.PatientFormDetailsAuditViewDescProvider");

    addAuditLogTable("allergy", "patient_section_details_audit_log", "Allergy",
        "com.insta.hms.outpatient.SectionDetailsDescProvider");
    addAuditLogTable("allergy", "patient_allergies_audit_log", "Allergy",
        "com.insta.hms.outpatient.PatientAllergiesDescProvider");
    addAuditLogTable("allergy", "patient_allergies_audit_log_triage_view", "Triage Form Allergy",
        "com.insta.hms.outpatient.AllergiesDescProvider");
    addAuditLogTable("allergy", "patient_allergies_audit_log_cons_view",
        "Consultation Form Allergy", "com.insta.hms.outpatient.AllergiesDescProvider");
    addAuditLogTable("allergy", "patient_allergies_audit_log_ipf_view", "IP Form Allergy",
        "com.insta.hms.outpatient.AllergiesDescProvider");
    addAuditLogTable("allergy", "patient_allergies_audit_log_genf_view", "Generic Form Allergy",
        "com.insta.hms.outpatient.AllergiesDescProvider");

    addAuditLogTable("complaintsaudit", "patient_complaints_audit_log_view", "Complaints",
        "com.insta.hms.wardactivities.visitsummaryrecord.VisitSummaryRecordDescViewProvider");
    addAuditLogTable("complaintsaudit", "patient_registration_audit_log", "Registration Details",
        "com.insta.hms.wardactivities.visitsummaryrecord."
            + "VisitSummaryRecordRegistrationDescProvider");
    addAuditLogTable("complaintsaudit", "secondary_complaints_audit_log", "Secondary Complaints",
        "com.insta.hms.wardactivities.visitsummaryrecord.VisitSummaryRecordComplaintsDescProvider");

    addAuditLogTable("diagnosis", "patient_registration_audit_log", "Diagnosis Details",
        "com.insta.hms.Registration.PatientVisitDescProvider");
    addAuditLogTable("diagnosis", "mrd_diagnosis_audit_log", "Diagnosis Details",
        "com.insta.hms.outpatient.PatientDiagnosisDetailsDescProvider");
    addAuditLogTable("diagnosis", "patient_diagnosis_details_audit_log_view", "Diagnosis Details",
        "com.insta.hms.outpatient.DiagnosisDetailsDescProvider");

    addAuditLogTable("diagnosis", "patient_admission_request_audit_log", "Diagnosis Details",
        "com.insta.hms.Registration.PatientAdmissionRequestDescProvider");
    addAuditLogTable("diagnosis", "admission_request_diagnosis_audit_log_view", "Diagnosis Details",
        "com.insta.hms.outpatient.AdmissionRequestDiagnosisDescProvider");

    addAuditLogTable("management", "patient_prescription_audit_log_view", "Management",
        "com.insta.hms.outpatient.PatientPrescriptionDescProvider");
    addAuditLogTable("management", "patient_prescriptions_details_audit_log_view", "Prescription",
        "com.insta.hms.outpatient.PrescriptionDetailsDescProvider");
    
    addAuditLogTable("dischargemedication",
        "patient_discharge_medication_cons_audit_log_view",
        "Discharge Medication ",
        "com.insta.hms.outpatient.PatientPrescriptionDescProvider");
    addAuditLogTable("dischargemedication", "patient_prescriptions_details_audit_log_view",
        "Prescription ", "com.insta.hms.outpatient.PrescriptionDetailsDescProvider");

    addAuditLogTable("physicianorder", "patient_prescription_ip_audit_log_view", "Physician Order",
        "com.insta.hms.outpatient.PatientPrescriptionDescProvider");
    addAuditLogTable("physicianorder", "patient_prescriptions_details_audit_log_view",
        "Prescription", "com.insta.hms.outpatient.PrescriptionDetailsDescProvider");

    addAuditLogTable("dischargemedication",
        "patient_discharge_medication_ip_audit_log_view", "Discharge Medication ",
        "com.insta.hms.outpatient.PatientPrescriptionDescProvider");
    addAuditLogTable("dischargemedication", "patient_prescriptions_details_audit_log_view",
        "Prescription ", "com.insta.hms.outpatient.PrescriptionDetailsDescProvider");

    addAuditLogTable("consultation", "doctor_consultation_audit_log", "Consultation",
        "com.insta.hms.outpatient.DoctorConsultationDescProvider");

    addAuditLogTable("clinicalpreferences", "clinical_preferences_audit_log",
        "Clinical Preferences",
        "com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesDescProvider");

    addAuditLogTable("patientnotes", "patient_notes_audit_log", "Patient Notes",
        "com.insta.hms.core.clinical.notes.PatientNotesDescProvider");

    addAuditLogTable("triage", "doctor_consultation_triage_audit_log", "Triage",
        "com.insta.hms.outpatient.DoctorConsultationTriageFormDescProvider");

    addAuditLogTable("stores", "physical_stock_take_audit_log", "Stock Take");
    addAuditLogTable("stores", "physical_stock_take_detail_audit_log",
        "Stock Take Items");
    addAuditLogTable("stores", "stock_take_audit_view", "Stock Take & Items",
        "com.insta.hms.stores.StockTakeAuditViewDescProvider");

    addAuditLogTable("ipemr", "ipemr_form_audit_log_view", "IP EMR",
        "com.insta.hms.Registration.IpEmrFormDescProvider");

    addAuditLogTable("initialassessment", "doctor_consultation_ia_audit_log", "Initial Assesssment",
        "com.insta.hms.outpatient.DoctorConsultationInitialAssessmentFormDescProvider");

    addAuditLogTable("salucrolocation", "salucro_location_mapping_audit_log",
        "Salucro Location Mapping",
        "com.insta.hms.integration.salucro.SalucroLocationDescProvider");
    addAuditLogTable("salucrorole", "salucro_role_mapping_audit_log", "Salucro Role Mapping",
        "com.insta.hms.integration.salucro.SalucroRoleDescProvider");
  }

  private static void addAuditLogTable(String auditType, String tableName, String tableDisplayName,
      String descProviderClass) {
    Map<String, String> theMap = tableMap.get(auditType);
    if (null == theMap) {
      theMap = new LinkedHashMap<String, String>();
      tableMap.put(auditType, theMap);
    }
    theMap.put(tableName, tableDisplayName);
    providerMap.put(tableName, descProviderClass);
  }

  private static void addAuditLogTable(String auditType, String tableName,
      String tableDisplayName) {
    addAuditLogTable(auditType, tableName, tableDisplayName, DEFAULT_AUDITLOG_DESC_PROVIDER);
  }

  /**
   * Gets the audit log tables.
   *
   * @param auditType the audit type
   * @return the audit log tables
   */
  public static Map<String, String> getAuditLogTables(String auditType) {
    Map<String, String> tables = new LinkedHashMap<String, String>();
    if (null != auditType && null != tableMap.get(auditType)) {
      tables.putAll(tableMap.get(auditType));
    }
    return tables;
  }

  /**
   * Gets the audit log desc providers.
   * @return the audit log desc providers
   */
  public static Map<String, String> getAuditLogDescProviders() {
    Map<String, String> providers = new HashMap<String, String>();
    providers.putAll(providerMap);
    return providers;
  }
}

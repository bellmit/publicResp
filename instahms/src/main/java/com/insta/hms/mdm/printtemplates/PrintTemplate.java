package com.insta.hms.mdm.printtemplates;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

/**
 * Print Template class.
 * @author anup vishwas
 */
public enum PrintTemplate {

  /**
   * list of all customizable print templates available in hms. when adding new customizable print
   * template to this list do not forget to insert the default row in db with template_type. then
   * only newly added template will be available for user to customize it.
   */
  Lab("L", "DiagTemplateFormat", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
  Rad("R", "RadiologyTemplateFormat", PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD),
  CLab("CL", "ClinicalLabPrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Dis("D", "DischargeHVFTemplate", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
  RetPhar("RP", "PharmacyRetailCreditPaymentPrint", PrintConfigurationsDAO.PRINT_TYPE_PHARMACY),
  RetPharBill("RPB", "PharmacyRetailCreditBillPrint", PrintConfigurationsDAO.PRINT_TYPE_PHARMACY),
  Ser("S", "ServiceConductionReport", PrintConfigurationsDAO.PRINT_TYPE_SERVICE),
  EST_PHAR("PE", "PharmacyEstimatePrint", PrintConfigurationsDAO.PRINT_TYPE_PHARMACY),
  RegBarCode(
      "REGBARCODE", "RegistrationBarCodeTextTemplate", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  SamBarCode(
      "SAMBARCODE",
      "SampleCollectionBarCodeTextTemplate",
      PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  SampleWorkSheet("WORKSHEET", "SampleWorkSheetPrint", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
  ItemBarCode("ITMBARCODE", "StoreItemBarcodePrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Order("ORD", "IPPrescription", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
  Po_print("PO", "PurchaseOrderPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Grn_print("GRN", "GrnPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Gate_pass_print("GTPASS", "GatePassPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Patient_Ward_Activities(
      "PWACT", "PatientWardActivities", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  View_Raise_Indent_print(
      "INDENT", "VieworRaiseIndentPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Stock_Transfer_print("TRANSFER", "StockTransferPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  approve_indent_print("APPINDENT", "ApproveIndentPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  TreatmentSheet("TSheet", "Treatment", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
  ClinicalInfo("CI", "ClinicalInformation", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Items_Return_Note_Print(
      "RETURNNOTE", "ItemsReturnNotePrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Appointment("APP_PRINT", "AppointmentPrint", PrintConfigurationsDAO.PRINT_TYPE_APPOINTMENT),
  Triage("Triage", "TriagePrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Voucher("Voucher", "PaymentVoucherPrint", PrintConfigurationsDAO.PRINT_TYPE_BILL),
  Initial_Assessment(
      "Assessment", "InitialAssessmentPrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Vital_Measurements("Vital", "VitalFormDetailsReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Progress_Notes("PrgNotes", "PatientProgressNotes", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  PATIENT_SURVEY_RESPONSE(
      "PATIENT_RESPONSE_PRINT",
      "PatientResponsePrint",
      PrintConfigurationsDAO.PRINT_TYPE_PATIENT_SURVEY_RESPONSE),
  Sample(
      "PAPERPRINT",
      "SampleCollectionPaperPrint",
      PrintConfigurationsDAO.PRINT_TYPE_SAMPLE_COLLECTION),
  TRMT_QUOTATION(
      "TRMT_QUOTATION", "DentalTreatmentQuotationPrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  DENTAL_SUPPLIER_PRINT(
      "DENTAL_SUPPLIER_PRINT", "DentalSupplierPrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  WebLab("WEB_LAB", "webBasedLabTemplate", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
  WebRad("WEB_RAD", "webBasedRadTemplate", PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD),
  DoctorOrder("DoctorOrder", "DoctorOrderPrint", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  DoctorNotes("DoctorNotes", "DoctorNotesReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  NurseNotes("NurseNotes", "NurseNotesReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  VisitSummaryRecord(
      "VisitSummaryRecord", "VisitSummaryRecordsReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  ConsultationDetails(
      "ConsultationDetails", "ConsultationDetails", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  OTDetails("OTDetails", "OTDetails", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  PATIENT_INDENT("PATIENT_INDENT", "PatientIndentPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  PENDING_PRESCRIPTION(
      "PENDING_PRESC", "PendingPrescriptions", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  Medication_Chart(
      "Medication_Chart", "MedicationChartReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  PatientIssuePrintTemplate(
      "PatientIssuePrintTemplate",
      "PatientIssuePrintTemplate",
      PrintConfigurationsDAO.PRINT_TYPE_STORE),
  PatientIssueReturnPrintTemplate(
      "PatientIssueReturnPrintTemplate",
      "PatientIssueReturnPrintTemplate",
      PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Process_indent("Process_indent", "ProcessedIndentPrint", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  APILAB("API_LAB", "APIBasedLabTemplate", PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG),
  APIRAD("API_RAD", "APIBasedRadTemplate", PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG),
  PriorAuthPrescription(
      "PriorAuth", "EAuthPrescriptionPrint", PrintConfigurationsDAO.PRINT_TYPE_BILL),
  UserIssuePrintTemplate(
      "UserIssuePrintTemplate", "UserIssuePrintTemplate", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  UserIssueReturnPrintTemplate(
      "UserIssueReturnPrintTemplate",
      "UserIssueReturnPrintTemplate",
      PrintConfigurationsDAO.PRINT_TYPE_STORE),
  WorkOrderPrintTemplate(
      "WorkOrderPrintTemplate", "WorkOrderPrintTemplate", PrintConfigurationsDAO.PRINT_TYPE_STORE),
  Discharge_Medication(
      "Discharge_Medication", "DischargeMedication", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
  Investigation(
      "Investigation", "InvestigationTemplate", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
  Vaccination("Vaccination", "VaccinationChart", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  IpEmrSummaryRecord(
      "IpEmrSummaryRecord", "IpEmrSummaryRecordReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  PatientNotes(
          "PatientNotes", "PatientNotesReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
  VitalsChart(
           "VitalsChart", "VitalsChartReport", PrintConfigurationsDAO.PRINT_TYPE_PATIENT);

  String type = null;
  String ftlName = null;
  String printType = null;
  String title = null;

  private PrintTemplate(String type, String ftlName, String printType) {
    this.type = type;
    this.ftlName = ftlName;
    this.printType = printType;
  }

  public String getType() {
    return type;
  }

  public String getFtlName() {
    return ftlName;
  }

  public String getPrintType() {
    return printType;
  }
}

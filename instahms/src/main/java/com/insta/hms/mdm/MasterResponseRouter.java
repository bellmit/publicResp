package com.insta.hms.mdm;

/**
 * 
 * @author yashwant
 * 
 *         This classes is used for defining a path for jsp resources and used it in controller for
 *         sending response.
 *
 */

public class MasterResponseRouter extends ResponseRouter {

  public static final String PAGE_PATH = "master";

  protected MasterResponseRouter(String pathElement) {
    super(PAGE_PATH, pathElement);
  }

  // TODO : This is an example router only. The parameter passed to the
  // constructor
  // should be the path of the JSP files under master

  public static final MasterResponseRouter EXAMPLE_MASTER_ROUTER = new MasterResponseRouter(
      "example");

  public static final MasterResponseRouter SAMPLE_SOURCE_ROUTER = new MasterResponseRouter(
      "samplesources");
  public static final MasterResponseRouter REGION_MASTER_ROUTER = new MasterResponseRouter(
      "regions");
  public static final MasterResponseRouter SPONSOR_PROCEDURE_ROUTER = new MasterResponseRouter(
      "SponsorProcedureMaster");
  public static final MasterResponseRouter DEPARTMENT_ROUTER = new MasterResponseRouter(
      "departments");
  public static final MasterResponseRouter STORE_ROUTER = new MasterResponseRouter("store");
  public static final MasterResponseRouter RECURRENCE_DAILY_MASTER = new MasterResponseRouter(
      "dailyrecurrences");
  public static final MasterResponseRouter STRENGTH_UNIT_MASTER = new MasterResponseRouter(
      "StrengthUnit");
  public static final MasterResponseRouter AREA_MASTER_ROUTER = new MasterResponseRouter("areas");
  public static final MasterResponseRouter CITY_MASTER_ROUTER = new MasterResponseRouter("cities");
  public static final MasterResponseRouter DISTRICT_MASTER_ROUTER = new MasterResponseRouter(
      "districts");
  public static final MasterResponseRouter DENTAL_SUPPLIER_MASTER_ROUTER = new MasterResponseRouter(
      "dentalsuppliers");
  public static final MasterResponseRouter DENTAL_SUPPLIES_MASTER_ROUTER = new MasterResponseRouter(
      "dentalsupplies");
  public static final MasterResponseRouter REJECTION_REASON_ROUTER = new MasterResponseRouter(
      "RejectionReason");
  public static final MasterResponseRouter TRANSFER_HOSPITALS_ROUTER = new MasterResponseRouter(
      "TransferHospitals");
  public static final MasterResponseRouter GOVT_IDENTIFIER_MASTER_ROUTER = new MasterResponseRouter(
      "govtidentifiers");
  public static final MasterResponseRouter OTHER_IDENTIFIER_MASTER_ROUTER =
      new MasterResponseRouter("otheridentifiers");
  public static final MasterResponseRouter SALUTATION_MASTER_ROUTER = new MasterResponseRouter(
      "salutations");
  public static final MasterResponseRouter SUPPLIER_CATEGORY_ROUTER = new MasterResponseRouter(
      "suppliercategories");
  public static final MasterResponseRouter COUNTER_ROUTER = new MasterResponseRouter("counters");
  public static final MasterResponseRouter PAYMENT_TERMS_MASTER_ROUTER = new MasterResponseRouter(
      "paymentterms");
  public static final MasterResponseRouter INSURANCE_ITEM_CATEGORY_ROUTER =
      new MasterResponseRouter("iteminsurancecategories");
  public static final MasterResponseRouter BILL_LABEL_ROUTER = new MasterResponseRouter(
      "billlabels");
  public static final MasterResponseRouter ACCOUNTING_GROUP_ROUTER = new MasterResponseRouter(
      "accountinggroup");
  public static final MasterResponseRouter DIALYZER_RATINGS_ROUTER = new MasterResponseRouter(
      "dialyzerratings");
  public static final MasterResponseRouter DIALYSIS_ACCESS_TYPES_ROUTER = new MasterResponseRouter(
      "dialysisaccesstypes");
  public static final MasterResponseRouter DOCUMENT_TYPE_ROUTER = new MasterResponseRouter(
      "documenttypes");
  public static final MasterResponseRouter DIALYSIS_MASTER_ROUTER = new MasterResponseRouter(
      "dialyzertypes");
  public static final MasterResponseRouter CHARGE_HEADS_ROUTER = new MasterResponseRouter(
      "chargeheads");
  public static final MasterResponseRouter ACCOUNTING_HEADS_ROUTER = new MasterResponseRouter(
      "accountingheads");
  public static final MasterResponseRouter STORE_ITEM_CONTROL_TYPE_ROUTER =
      new MasterResponseRouter("storeitemcontroltypes");
  public static final MasterResponseRouter INCOMING_HOSPITALS_ROUTER = new MasterResponseRouter(
      "InComingHospitals");
  public static final MasterResponseRouter DIAG_METHODOLOGY_MASTER_ROUTER = 
      new MasterResponseRouter("diagmethodologies");
  public static final MasterResponseRouter PHRASE_SUGGESTIONS_CATEGORY_MASTER_ROUTER =
      new MasterResponseRouter("phrasesuggestionscategories");
  public static final MasterResponseRouter SAMPLE_COLLECTION_CENTER_ROUTER = 
      new MasterResponseRouter("samplecollectioncenters");

  public static final MasterResponseRouter CONTRACT_TYPE_ROUTER = new MasterResponseRouter(
      "contracttypes");
  public static final MasterResponseRouter SPONSOR_TPYE_ROUTER = new MasterResponseRouter(
      "sponsors");
  public static final MasterResponseRouter STATE_MASTER_ROUTER = new MasterResponseRouter("states");
  public static final MasterResponseRouter CENTER_ROUTER = new MasterResponseRouter("centers");
  public static final MasterResponseRouter STORE_TYPE_ROUTER = new MasterResponseRouter(
      "storetypes");
  public static final MasterResponseRouter COUNTRY_MASTER_ROUTER = new MasterResponseRouter(
      "countries");
  public static final MasterResponseRouter HEALTH_AUTH_PREFERENCES_ROUTER = 
      new MasterResponseRouter("healthauthpreferences");
  public static final MasterResponseRouter APPT_SOURCE_MASTER_ROUTER = new MasterResponseRouter(
      "appointmentsources");
  public static final MasterResponseRouter ENCOUNTER_TYPE_ROUTER = new MasterResponseRouter(
      "encountertype");
  public static final MasterResponseRouter DEATH_REASON_ROUTER = new MasterResponseRouter(
      "deathreasons");

  public static final MasterResponseRouter SECTIONS_ROUTER = new MasterResponseRouter("Sections");
  public static final MasterResponseRouter SECTION_FIELDS_ROUTER = new MasterResponseRouter(
      "SectionFields");
  public static final MasterResponseRouter IMAGE_MARKERS_ROUTER = new MasterResponseRouter(
      "ImageMarkers");
  public static final MasterResponseRouter REGULAR_EXPRESSION_ROUTER = new MasterResponseRouter(
      "regularexpressions");
  public static final MasterResponseRouter PHRASE_SUGGESTIONS_ROUTER = new MasterResponseRouter(
      "phrasesuggestions");
  public static final ResponseRouter SYS_GEN_SEC_PATH = new MasterResponseRouter("SystemSections");
  public static final ResponseRouter CARD_TYPE_ROUTER = new MasterResponseRouter("cardtypes");
  public static final ResponseRouter PAYMENT_MODE_ROUTER = new MasterResponseRouter("paymentmodes");
  public static final ResponseRouter PAYMENT_CATEGORY_ROUTER = new MasterResponseRouter(
      "paymentcategories");

  public static final MasterResponseRouter CONTRACTOR_MASTER_ROUTER = new MasterResponseRouter(
      "storescontractors");
  public static final ResponseRouter CENTER_GROUP_ROUTER = new MasterResponseRouter("centergroup");

  public static final MasterResponseRouter SECTION_ROLE_RIGHTS_ROUTER = new MasterResponseRouter(
      "sectionrolerights");

  public static final MasterResponseRouter ICD_CODES_ROUTER = new MasterResponseRouter("icdcodes");

  public static final MasterResponseRouter DISCOUNT_PLAN_ROUTER = new MasterResponseRouter(
      "discountplans");
  public static final MasterResponseRouter OP_VISIT_TYPE_RULES_ROUTER = new MasterResponseRouter(
      "visittyperules");
  public static final MasterResponseRouter ROUTE_OF_ADMINISTRATOIN_ROUTER = 
      new MasterResponseRouter("MedicineRoute");
  public static final MasterResponseRouter ICD_SUPPORTED_CODES_ROUTER = new MasterResponseRouter(
      "ICDSupportedCodes");

  public static final MasterResponseRouter HOSPITAL_ID_PATTERNS_ROUTER = new MasterResponseRouter(
      "hospitalidpatterns");
  public static final MasterResponseRouter BILL_SEQUENCE_ROUTER = new MasterResponseRouter(
      "billsequences");
  public static final MasterResponseRouter CLAIM_ID_SEQUENCE_ROUTER = new MasterResponseRouter(
      "claimidsequences");
  public static final MasterResponseRouter GRN_PRINT_TEMPLATES_ROUTER = new MasterResponseRouter(
      "grnprinttemplates");
  public static final MasterResponseRouter VISIT_ID_SEQUENCE_ROUTER = new MasterResponseRouter(
      "visitidsequences");
  public static final MasterResponseRouter VITAL_PARAMETER_ROUTER = new MasterResponseRouter(
      "vitalparameter");
  public static final MasterResponseRouter VITAL_REFERENCE_RANGE_ROUTER = new MasterResponseRouter(
      "vitalparameter/vitalreferencerange");
  public static final MasterResponseRouter DIAG_TEST_ROUTER = new MasterResponseRouter(
      "diagnostics");

  public static final MasterResponseRouter ITEM_FORM_ROUTER = new MasterResponseRouter("itemforms");
  public static final MasterResponseRouter GENERIC_CLASSIFICATION_ROUTER = new MasterResponseRouter(
      "genericclassifications");
  public static final MasterResponseRouter GENERIC_SUB_CLASSIFICATION_ROUTER = 
      new MasterResponseRouter("genericsubclassifications");

  public static final MasterResponseRouter ISSUE_USER_ROUTER = 
      new MasterResponseRouter("issueusers");
  public static final MasterResponseRouter STOCK_ADJUSTMENT_REASON_ROUTER =
      new MasterResponseRouter("stockadjustmentreason");

  public static final MasterResponseRouter FORM_COMPONENTS_ROUTER = new MasterResponseRouter(
      "FormComponents");

  public static final MasterResponseRouter FORM_COMPONENTS_CENTER_APPLICABILITY_ROUTER =
      new MasterResponseRouter("FormComponents/CenterApplicability");

  public static final MasterResponseRouter RESOURCE_AVAILABILITY_OVERRIDE_ROUTER = 
      new MasterResponseRouter("resourceoverrides");

  public static final MasterResponseRouter RESOURCE_AVAILABILITY_ROUTER = new MasterResponseRouter(
      "resourceschedulers");

  public static final MasterResponseRouter EQUIPMENT_ROUTER = new MasterResponseRouter("equipment");

  public static final MasterResponseRouter ITEM_GROUP_ROUTER = 
      new MasterResponseRouter("taxgroups");

  public static final MasterResponseRouter ITEM_SUB_GROUP_ROUTER = new MasterResponseRouter(
      "taxsubgroups");

  public static final MasterResponseRouter INSURANCE_PLANS_ROUTER = new MasterResponseRouter(
      "insuranceplans");

  public static final MasterResponseRouter CENTER_PREFERENCES_ROUTER = 
      new MasterResponseRouter("CenterPreferences");

  public static final MasterResponseRouter TEST_EQUIPMENT_ROUTER = 
      new MasterResponseRouter("testequipments");

  public static final MasterResponseRouter PHARMACY_BILL_SEQUENCE_ROUTER = 
      new MasterResponseRouter("pharmacybillsequences");
  
  public static final MasterResponseRouter BILL_AUDIT_NUMBER_SEQUENCE_ROUTER = 
      new MasterResponseRouter("billauditnumbersequences");

  public static final MasterResponseRouter GRN_NUMBER_SEQUENCE_ROUTER =
      new MasterResponseRouter("grnnumbersequences");
  
  public static final MasterResponseRouter PURCHASE_ORDER_SEQUENCE_ROUTER = 
      new MasterResponseRouter("purchaseordersequences");
  
  public static final MasterResponseRouter VOUCHER_NUMBER_SEQUENCE_ROUTER = 
      new MasterResponseRouter("vouchernosequences");

  public static final MasterResponseRouter RECEIPT_NUMBER_SEQUENCE_ROUTER = 
      new MasterResponseRouter("receiptnumbersequences");

  public static final MasterResponseRouter EDC_MACHINES_ROUTER = new MasterResponseRouter(
      "edcmachines");

  public static final MasterResponseRouter CONFIDENTIALITY_GROUPS_ROUTER = new MasterResponseRouter(
      "confidentialitygroups");

  public static final MasterResponseRouter STOCK_UPLOAD_ROUTER = new MasterResponseRouter(
      "stockuploads");

  public static final MasterResponseRouter GENERIC_NAMES = new MasterResponseRouter("genericnames");

  public static final MasterResponseRouter LAB_NUMBER_SEQUENCE_ROUTER = new 
      MasterResponseRouter("labnumbersequences");
  public static final MasterResponseRouter RADIOLOGY_NUMBER_SEQUENCE_ROUTER =
      new MasterResponseRouter("radiologynumbersequences");

  public static final MasterResponseRouter DIET_CONSTITUTENTS_ROUTER = new MasterResponseRouter(
      "dietconstituents");

  public static final MasterResponseRouter MESSAGE_DISPATCHER_CONFIG_ROUTER = 
      new MasterResponseRouter("messagedispatcherconfig");
  
  public static final MasterResponseRouter RACE_ROUTER = 
      new MasterResponseRouter("race");
  
  public static final MasterResponseRouter SYSTEM_MESSAGE_ROUTER = 
      new MasterResponseRouter("systemmessages");
  
  public static final MasterResponseRouter BLOOD_GROUP_ROUTER = 
      new MasterResponseRouter("bloodgroup");

  public static final MasterResponseRouter MARTIAL_STATUS_ROUTER =
      new MasterResponseRouter("maritalstatus");

  public static final MasterResponseRouter RELIGION_ROUTER =
      new MasterResponseRouter("religion");

  public static final MasterResponseRouter GENDER_ROUTER =
      new MasterResponseRouter("gender");
  
  public static final MasterResponseRouter COUNTER_CENTER_MAPPING_ROUTER =
      new MasterResponseRouter("usercentercounters");
  
  public static final MasterResponseRouter PATIENT_PROBLEMS_ROUTER =
      new MasterResponseRouter("patientproblems");
  
  public static final MasterResponseRouter CONSUMPTION_UOM_ROUTER =
      new MasterResponseRouter("consumptionUom");
  
  public static final MasterResponseRouter CODE_SETS_ROUTER =
      new MasterResponseRouter("codesets");

  public static final MasterResponseRouter ALLERGEN_ROUTER = new MasterResponseRouter(
      "allergenMaster");
}

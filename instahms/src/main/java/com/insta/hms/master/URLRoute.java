package com.insta.hms.master;

/**
 * 
 * @author yashwant
 * 
 *         This classes is used for defining a path for jsp resources and used
 *         in controller for sending response.
 *         
 *            Use parent path in jsp anchor link or c:url tag or c:var tag like below
 *         
 *         <c:set var="pagePath" value="<%=URLRoute.STORE_PATH %>"/>
 *         
 *         In edit dialog :     ${pagePath}/show.htm?_method=show
 *         
 *         For add button
 *         
 *         <c:url var="Url" value="${pagePath}/add.htm">
 *        	 <c:param name="_method" value="add"/>
 *         </c:url>
 *         
 *         
 *         For Anchor Tag :  <a href="${cpath}/master/store/add.htm?_method=add">Add</a>
 *
 */

public class URLRoute {
	
	/*
	 * Master Index page
	 */
	public static final String MASTER_INDEX_URL 						= "/index";
	
	/**
	 * Region Master Jsp Sources
	 */
	public static final String REGION_MASTER                    = "/master/regions";
	public static final String REGION_MASTER_LIST               = "/pages/master/regions/list";
	public static final String REGION_MASTER_SHOW               = "/pages/master/regions/show";
	public static final String REGION_MASTER_ADD                = "/pages/master/regions/add";
	public static final String REGION_MASTER_REDIRECT_TO_SHOW   = "redirect:show";
	public static final String REGION_MASTER_REDIRECT_TO_LIST   = "redirect:list";

    
    /**
     * Recurrence Daily Master
     */
    public static final String RECURRENCE_DAILY_MASTER_PATH = "/master/dailyrecurrences";

    /**
     * Recurrence Daily Master
     */
    public static final String PRACTIONER_TYPE_MASTER_PATH = "/master/practionertype";

	/**
	 *  For common insta Exception page
	 *
	 */
	public static final String HMS_EXCEPTION                    = "/pages/ExceptionPage";

	/**
	 *  Sponsor Procedure Master
	 */

	public static final String SPONSOR_PROCEDURE_PATH 	= "/master/sponsorprocedure";

	/**
	 * Store 
	 */
	public static final String STORE_PATH                		= "/master/stores";
	public static final String STORE_MASTER_LIST                = "/pages/master/stores/list";
	public static final String STORE_MASTER_REDIRECT_TO_LIST    = "redirect:list";

	/**
	 *  Search Master
	 */
	public static final String SAVED_SEARCHES_PATH = "/master/SavedSearches";
	public static final String SAVED_SEARCHES_LIST = "/pages/master/SavedSearches/SavedSearchesList";
	public static final String SAVED_SEARCHES_ADDSHOW = "/pages/master/SavedSearches/SavedSearchesAddShow";
	public static final String SAVED_SEARCHES_REDIRECT_TO_LIST = "redirect:list";
	public static final String SAVED_SEARCHES_REDIRECT_TO_SHOW = "redirect:show";

	/**
	 *  Area Master
	 */

	public static final String AREA_MASTER_PATH     = "/master/areas";

	/**
	 *  City Master
	 */

	public static final String CITY_MASTER_PATH     = "/master/cities";

  /**
   * District Master
   */
   public static final String DISTRICT_MASTER_PATH = "/master/districts";

	/**
	 *  finger print verification purposes Master
	 */
	
	public static final String FP_VERIFICATION_PURPOSE_MASTER_PATH     = "/master/fpVerificationPurpose";

	/**
	 * Dental Master
	 */
	public static final String DENTAL_SUPPLIER_MASTER = "/master/dentalsuppliers";
	public static final String DENTAL_SUPPLIES_MASTER = "/master/dentalsupplies";
	/**
	 * Rejection Reason
	 */    
	public static final String REJECTION_REASON_PATH 	= "/master/rejectionreason";


	/*TransferHospitals*/
	public static final String TRANSFER_HOSPITALS_PATH = "/master/transferhospitals";

	/**
	 * DocumentTypeMaster 
	 */
	public static final String DOCUMENT_TYPE_PATH = "/master/documenttypes";
	 
	/**
	 *  Government Identifier Master
	 */

	public static final String GOVT_IDENTIFIER_MASTER_PATH 	= "/master/govtidentifiers";

	/**
	 *  Other Identifier Master
	 */
	
	public static final String OTHER_IDENTIFIER_MASTER_PATH 	= "/master/otheridentifiers";

	/**
	 *  Salutation Master
	 */

	public static final String SALUTATION_MASTER_PATH 	= "/master/salutations";

	/**
	 * Supplier Category Mater 
	 */
	public static final String SUPPLIER_CATEGORY_PATH   = "/master/suppliercategories";
	public static final String SUPPLIER_CATEGORY_REDIRECT_TO_LIST    = "redirect:list";

	/**
	 * Payment Terms Master
	 */
	public static final String PAYMENT_TERMS_MASTER_PATH    = "/master/paymentterms";

	/**
	 * Insurance Item Category
	 */
	public static final String INSURANCE_ITEM_CATEGORY_MASTER_PATH  = "/master/iteminsurancecategories";

	/**
	 * Sample Source Master
	 */
	public static final String SAMPLE_SOURCE_MASTER = "/master/samplesources";


    /**
     * Department Mater 
     */
    public static final String DEPARTMENT_PATH   = "/master/departments";

/**
     * Appointment Source Mater 
     */
    public static final String APPT_SOURCE_MASTER_PATH                = "/master/appointmentsources";
    
    /**
     * Complaint Type Master 
     */
    public static final String COMPLAINT_TYPE_MASTER_PATH   = "/master/complainttypes";
    
    
    /**
     * Strength Unit Master
     */
    public static final String STRENGTH_UNIT_MASTER = "/master/strengthunits";
    
    /**
     *  Counter Mater 
     */
    public static final String COUNTER_PATH   = "/master/counters";
    
    /**
     *  Bill Label Master
     */
    public static final String BILL_LABEL_PATH = "/master/billlabels";
    
    /**
     *  Accounting Group Master
     */
    public static final String ACCOUNTING_GROUP_PATH = "/master/accountinggroup";
    
    /**
     *  Dialyzer Ratings Master
     */
    public static final String DIALYZER_RATINGS_PATH 	= "/master/dialyzerratings";
    /**
     *  Dialysis Access Types  Master
     */
    public static final String DIALYSIS_ACCESSS_TYPES_PATH 	= "/master/dialysisaccesstypes";
    
    /**
      *  Dialyzer Type Master
      */
    public static final String DIALYSIS_MASTER_PATH 	= "/master/dialyses";
       
    /**
     * Store Item Control Type Master
     */
    public static final String STORE_ITEM_CONTROL_TYPE_PATH         = "master/storeitemcontroltypes";

    /**
     *  Charge Head Mater 
     */
    public static final String CHARGE_HEAD_PATH   = "/master/chargeheads";
    
    /**
     *  Accounting Head Mater 
     */
    public static final String ACCOUNTING_HEAD_PATH   = "/master/accountingheads";
    
    /**
     *  Incoming Hospitals Master
     */
    public static final String INCOMING_HOSPITALS 		 = "/master/InComingHospitals";
    
    /**
     *  Diagnostics Methodology Master
     */    
    public static final String DIAG_METHODOLOGY_MASTER_PATH 	= "/master/diagmethodologies";
    
    /**
     *  Phrase Suggestions Categories
     */
    public static final String PHRASE_SUGGESTIONS_CATEGORY_PATH   = "/master/phrasesuggestionscategories";

    /**
     *  Contract Type Master
     */
    public static final String CONTRACT_TYPE_PATH 		 = "/master/contracttypes";
    
    /**
     *  Doctor Master
     */
    public static final String DOCTOR_MASTER_PATH = "/master/doctors";
    
    /**
     *  Referal Doctor Master
     */
    public static final String REFERRAL_DOCTOR_MASTER_PATH = "/master/referraldoctors";
    
    /**
     *  Sample Collection Center
     */
    public static final String SAMPLE_COLLECTION_CENTER 		 = "/master/samplecollectioncenters";
    
    /** 
     * Sections Master
    */
   public static final String SECTIONS_PATH = "master/sections";
   /**
    * section fields Master
    */
   public static final String SECTION_FIELDS_PATH 		 = "master/sectionfields";
   /**
    * Image Markers Master
    */
   public static final String IMAGE_MARKERS_PATH = "master/imagemarkers";
   /**
    * Regular Expression Master
    */
   public static final String REGULAR_EXPRESSION_PATH = "master/regularexpression";
    
    /**
     *  Sopnsor Type Master
     */
    public static final String SPONSOR_TYPE_PATH = "/master/sponsors";
    
    /**
     *  State Master
     */
    public static final String STATE_MASTER_PATH = "/master/states";
   
    /**
     * System Generated Sections Master
     */
    public static final String SYS_GEN_SEC_PATH = "master/systemsections";
    public static final String PHRASE_SUGGESTIONS_PATH   = "/master/phrasesuggestions";
    public static final String SYSTEM_PREFERENCES_PAGE = "/pages/systempreferences/show";
    public static final String SYSTEM_PREFERENCES = "/systempreferences";
    public static final String SYSTEM_PREFERNCES_REDIRECT_SHOW = "redirect:show";
    
    /**  Center Master
     */
    public static final String CENTER_PATH = "/master/centers";
    
    /**
     *  Store Type
     */
    public static final String STORE_TYPE_PATH = "/master/storetypes";
    
    /**
	 *  Country Master
	 */
	public static final String COUNTRY_MASTER_PATH     = "/master/countries";
	public static final String Country_MASTER_REDIRECT_TO_SHOW   = "redirect:show";
	 
	/**
     *  Health Authority Preferences Master
     */
    public static final String HEALTH_AUTH_PREFERENCES_PATH 		 = "/master/healthauthoritypreferences";
    
    /**
     *  Encounter Type Master
     */
    public static final String ENCOUNTER_TYPE_PATH 		 = "/master/encountertype";
    public static final String ENCOUNTER_TYPE_REDIRECT_TO_SHOW    = "redirect:show";
    
    /**
     *  Encounter Type Master
     */
    public static final String DEATH_REASON_PATH 		 = "/master/deathreasons";
    public static final String DEATH_REASON_REDIRECT_TO_SHOW    = "redirect:show";
    
    /**
     *  Card Type Master
     */ 
    public static final String CARD_TYPE_PATH 		 = "/master/cardtypes";
    
    /**
     *  Payment Mode Master
     */ 
    public static final String PAYMENT_MODE_PATH 		 = "/master/paymentmodes";
    public static final String PAYMENT_MODE_REDIRECT_TO_SHOW   = "redirect:show";
    
    /**
     *  Payment Category Master
     */ 
    public static final String PAYMENT_CATEGORY_PATH 		 = "/master/paymentcategory";
    
    /** 
     *Contractor Master
     */
    public static final String CONTRACTOR_MASTER_PATH = "/master/storescontractors";
    /**
     *  Center Group Master
     */ 
    public static final String CENTER_GROUP 		 = "/master/centergroup";
    public static final String CENTER_GROUP_REDIRECT_TO_SHOW   = "redirect:show";

    /**
     *  Doctor Master
     */
    public static final String INSURANCE_COMPANY_MASTER_PATH = "/master/insurancecompanies";
   
    /**
     * Discount Plan
     */
    public static final String DISCOUNT_PLAN_PATH = "/master/discountplans";
    
    /**
     * Op Visit Type rules
     */
    public static final String OP_VISIT_TYPE_RULES = "/master/visittyperules";
    
    public static final String TPA_MASTER_PATH = "/master/tpas";
   /**
     *  Route of Administration
     */
    public static final String ROUTE_OF_ADMINISTRATION_PATH 		 = "/master/medicineroute";

    
    /**
     *  Cron Job
     */
	public static final String CRON_JOB_PATH 			= "/master/cronjobs";
	public static final String CRON_JOB_SHOW 			= "/pages/master/cronjobs/show";
	public static final String CRON_JOB_SHOW_REDIRECT 	= "redirect:show";
    
    /**
     * ICD Codes Master
     */
    public static final String ICD_CODES_PATH = "/master/icdcodes";
    
    /**
     *  Coder claim review
     */
    public static final String CODER_CLAIM_REVIEW = "/";
    
    /**
     * Review types master
     */
    public static final String REVIEW_TYPES_PATH     = "/master/reviewtypes";
    
    /**
     * Roles master
     */
    public static final String ROLES_PATH = "/master/roles";
    
    /**
     * Review types master
     */
    public static final String REVIEW_CATEGORY_PATH     = "/master/reviewcategory";


    /**
     *  Supported Codes
     */
    public static final String ICD_SUPPORTED_CODES_MASTER_PATH = "master/icdsupportedcodes";
    /** 
	 * Vital parameter Master
	 */
	public static final String VITALS_MASTER_PATH = "/master/vitals";
	public static final String VITAL_PARAMETER_PATH = "/master/vitalparameter";
	public static final String VITAL_PARAMETER_RANGE = "/master/vitalparameterrange";

    /** 
     * Hospital Id Pattern Master
     */
    public static final String HOSPITAL_ID_PATTERNS_PATH = "/sequences/hospitalidpatterns";
    
    /** 
     * Billing Sequence Master
     */
    public static final String BILL_SEQUENCE_PATH = "/sequences/billsequences";
    
    /** 
     * Claim Id Sequence Master
     */
    public static final String CLAIM_ID_SEQUENCE_PATH = "/sequences/claimidsequences";

	/**
	 * Store GRN print templates Master 
	 */
	public static final String GRN_PRINT_TEMPLATE_PATH   = "/master/grnprinttemplates";
	public static final String GRN_PRINT_TEMPLATE_REDIRECT_TO_LIST    = "redirect:list";
	public static final String GRN_PRINT_TEMPLATE_REDIRECT_TO_SHOW    = "/pages/master/grnprinttemplates/show";
    
    
    /** 
     * Visit Id Sequence Master
     */
    public static final String VISIT_ID_SEQUENCE_PATH = "/sequences/visitidsequences";
    
    /**
     *  Diagnostics Test Master
     */
    public static final String DIAG_TEST_DETAILS_PATH = "/master/diagnostics";
    
    public static final String DIAG_TEST_DETAILS_PATH_REDIRECT = "redirect:list";
    
    public static final String ADD_EDIT_DIAG_TEST_DETAILS_PATH = "/master/addeditdiagnostics";
    public static final String ADD_EDIT_TEST_DETAILS_SHOW = "/master/addeditdiagnostics/show";
    public static final String ADD_EDIT_TEST_REDIRECT_TO_ADD = "redirect:add";
	public static final String ADD_EDIT_TEST_REDIRECT_TO_SHOW   = "redirect:show";
	public static final String EDIT_CHARGE_PATH = "/pages/master/diagnostics/editcharge";
	public static final String EDIT_TEST_CHARGE_REDIRECT_TO_SHOW ="redirect:editcharge";
	
	/**
	 * Item Form Master
	 */
	public static final String ITEM_FORM_PATH   = "/master/itemforms";
	
	/**
	 * Generic Classification Master
	 */
	public static final String GENERIC_CLASSIFICATION_PATH   = "/master/genericclassifications";
	public static final String GENERIC_CLASSIFICATION_REDIRECT_TO_LIST    = "redirect:list";
	
	/**
	 * Generic Sub Classification Master
	 */
	public static final String GENERIC_SUB_CLASSIFICATION_PATH   = "/master/genericsubclassifications";
	
	/** 
     *Issue User Master
     */
    public static final String ISSUE_USER_MASTER_PATH = "/master/issueuser";
    
    /** 
     *STOCK ADJUSTMENT REASON Master
     */
    public static final String STOCK_ADJUSTMENT_REASON_MASTER_PATH = "/master/stockadjustmentreason";

	
	/**
     *  Form Components
     */
    public static final String FORM_COMPONENTS_MASTER_PATH = "/master/formcomponents";
    public static final String FORM_COMPONENTS_LIST = "/pages/master/FormComponents/list";
    public static final String FORM_COMPONENTS_ADD = "/pages/master/FormComponents/add";
    public static final String FORM_COMPONENTS_SHOW = "/pages/master/FormComponents/show";
    
    /**
     *  Followup rules applicability
     */
    public static final String FOLLOWUP_RULES_APPLICABILITY = "/master/followuprulesapplicability";
    public static final String FOLLOWUP_RULES_APPLICABILITY_LIST = "/pages/master/visittyperules/applicability";

    /**
     *  Form Components Center Applicability
     */
    public static final String FORM_COMPONENTS_CENTER_APPLICABILITY_MASTER_PATH = "/master/formcomponents/centerapplicability";
    public static final String FORM_CENTER_SHOWFORM = "/pages/master/FormComponents/CenterApplicability/formcomponentscenterapplicability";

    /**
     * Insurance Aggregator Centers Mapping
     */
    public static final String INS_AGGREGATOR_CENTERS_PATH    ="/master/insaggregatorcenters";
    public static final String INS_AGGREGATOR_TPA_INSCO_PATH  ="/master/insaggregatortpainsco";
    public static final String INS_AGGREGATOR_DOCTORS_PATH    ="/master/insaggregatordoctors";
    public static final String INS_AGGREGATOR_PHARMACIES_PATH    ="/master/insaggregatorpharmacies";
    
    /** 
     * Order Sets Master
     */
    public static final String ORDER_SETS_PATH     = "/master/ordersets";
	public static final String PACKAGES_PATH     = "/master/packages";
	public static final String HOSPITAL_BILLING_MASTERS_INDEX_PATH = "/pages/master/hospitalbillingmasters/index";
    
    /**
	 *  Resource Availability
	 */
	public static final String RESOURCE_AVAILABILITY_PATH     = "/master/resourceschedulers";
	public static final String RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW   = "redirect:show";
	public static final String RESOURCE_AVAILABILITY_REDIRECT_DEF_TIMING  = "redirect:/master/resourceschedulers/show";

	
	/**
	 *  Resource Availability Override
	 */
	public static final String RESOURCE_AVAILABILITY_OVERRIDE_PATH    = "/master/resourceoverrides";
	public static final String RESOURCE_AVAILABILITY_OVERRIDE_PATH_REDIRECT    = "redirect:list";
	public static final String RESOURCE_AVAILABILITY_OVERRIDE_REDIRECT   = "redirect:show";
	public static final String RESOURCE_AVAILABILITY_DEFAULTTIMINGS_REDIRECT   = "redirect:defaultTimingsRedirect";
	public static final String SHOW_BULK_OVERRIDE = "/pages/master/resourceoverrides/addbulk";
	
	/**
     * Equipment master
     */
    public static final String EQUIPMENT_MASTER    ="/master/equipment";
	public static final String EQUIPMENT_TO_SHOW   = "redirect:show";
	public static final String ADD_EDIT_EQUIPMENT_SHOW = "/master/equipment/show";
    public static final String ADD_EDIT_EQUIPMENT_REDIRECT_TO_ADD = "redirect:add";
    public static final String EQUIPMENT_EDIT_CHARGE_PATH = "/pages/master/equipment/editcharge";
    public static final String EDIT_EQUIPMENT_CHARGE_REDIRECT_TO_SHOW ="redirect:editcharge";

	/**
	 * Insurance Plan Master
	 */
	public static final String INSURANCE_PLANS_PATH = "/master/insuranceplans";
	public static final String PLAN_LIST = "/planList";
	public static final String PLAN_LIST_BY_INSCO_CAT = "/plansByInscoAndCat";
	
	/**
	 * Center Preferences Master
	 */
	public static final String CENTER_PREFERENCES_PATH = "/master/centerpreferences";
	public static final String CENTER_PREFERENCES_REDIRECT_TO_SHOW = "redirect:/master/centerpreferences/show";

	/**
	 * Test Equipment Master
	 */
	public static final String TEST_EQUIPMENT_PATH = "/master/testequipments";
	public static final String TEST_EQUIPMENT_REDIRECT_TO_ADD = "redirect:add";
	public static final String TEST_EQUIPMENT_REDIRECT_TO_SHOW   = "redirect:show";
	
	 /**
     * Tax Item Group
     */
    public static final String ITEM_GROUP_PATH                                      = "/master/taxgroups";
    
    /**
     * Tax Item Sub Group
     */
    public static final String ITEM_SUB_GROUP_PATH                                    = "/master/taxsubgroups";
    public static final String ITEM_SUB_GROUP_SHOW               				 	  = "/pages/master/taxsubgroups/show";
    public static final String ITEM_SUB_GROUP_REDIRECT_TO_SHOW                        = "redirect:show";
    
    /** 
     * Pharmacy Bill Id Sequence Master
     */
    public static final String PHARMACY_BILL_ID_SEQUENCE_PATH = "/sequences/pharmacybillsequences";
    
    /** 
     * Bill Audit Number Sequence Master
     */
    public static final String BILL_AUDIT_NUMBER_SEQUENCE_PATH = "/sequences/billauditnumbersequences";
    
    /** 
     * GRN Number Sequence Master
     */
    public static final String GRN_NUMBER_SEQUENCE_PATH = "/sequences/grnnumbersequences";
    
    /** 
     * Purchase Order Sequence Master
     */
    public static final String PURCHASE_ORDER_SEQUENCE_PATH = "/sequences/purchaseordersequences";

    /**
	 * Tax Upload Download Group
	 */
	  public static final String TAX_UPLOAD_DOWNLOAD_PATH = "/master/itemtaxuploaddownloads";
	  public static final String TAX_UPLOAD_DOWNLOAD_PAGE = "/pages/master/itemtaxuploaddownloads/itemTaxUploadDownload";
	  public static final String TAX_UPLOAD_DOWNLOAD_REDIRECT = "redirect:UploadDownload";

    /** 
     * Voucher Number Sequence Master
     */
    public static final String VOUCHER_NUMBER_SEQUENCE_PATH = "/sequences/vouchernosequences";


    /** 
     * Receipt Number Sequence Master
     */
    public static final String RECEIPT_NUMBER_SEQUENCE_PATH = "/sequences/receiptnumbersequences";
    
    /** 
     * EDC Machines Master
     */
    public static final String EDC_MACHINES_PATH = "/master/edcmachines";

    /** 
     * Stock Upload
     */
    public static final String STOCK_UPLOAD_PATH = "/master/stockupload";

    /**
     * 	Store Item Generic Names end point
     */
    public static final String GENERIC_NAMES_PATH = "/master/genericnames";

	/** 
     * Lab Number Sequence Master
     */
    public static final String LAB_NUMBER_SEQUENCE_PATH = "/sequences/labnumbersequences";

    /** 
     * Radiology Number Sequence Master
     */
    public static final String RADIOLOGY_NUMBER_SEQUENCE_PATH = "/sequences/radiologynumbersequences";

    /** 
     * Note Type Master
     */
    public static final String NOTE_TYPES_PATH ="/master/notetypes";
    
    /** 
     * Manage Interface Master
     */
    public static final String INTERFACE_MASTER_PATH ="/master/interfaces";
    
    /** 
     * Data Backload Master
     */
    public static final String DATA_BACKLOAD_PATH ="/master/backload";

    /** 
     * Practitioner Type Consultation Mapping Master
     */
    public static final String PRACTITIONER_CONSULTATION_MAPPING_PATH = "/master/practitionerconsultationmapping";

    /**
     * Billing Group Master
     */
    public static final String BILLING_GROUP_MASTER = "/master/billinggroups";

    /**
     * Diet Constituents Master
     */
    public static final String DIET_CONSTITUTENTS_PATH = "/master/dietconstituents";


    /**
     * Confidentiality Groups Master
     */
    public static final String CONFIDENTIALITY_GROUPS_PATH = "/master/confidentialitygroups";

    /**
     * Case Rate Master
     */
    public static final String CASE_RATE_MASTER = "/master/caserate";
    public static final String FIND_BY_FILTER = "/findByFilter";
    public static final String CATEGORY_LIST = "/categoryList";
    
    /**
     * Code Type Master
     */
    public static final String CODE_TYPE_MASTER = "/master/codeType";


    /**
     *  Message Dispatcher Config Master
     */
    public static final String MESSAGE_DISPATCHER_CONFIG_LIST = "/master/messagedispatcherconfig/list";
    public static final String MESSAGE_DISPATCHER_CONFIG_PATH = "/master/messagedispatcherconfig";
    public static final String MESSAGE_DISPATCHER_CONFIG_SHOW = "/master/messagedispatcherconfig/show";
    
    public static final String CENTER_AVAILABILITY_PATH     = "/master/centeravailability";

    /**
     *  System Message Master
     */    
    public static final String SYSTEM_MESSAGE = "/master/systemmessages";

    /**
     * Hospital Role Master
     */
    public static final String HOSPITAL_ROLE_MASTER = "/master/hospitalroles";
    
    public static final String RACE_MASTER_PATH = "master/race";

    /**
     * pending prescription decline reasons master
     */
    public static final String PRESCRIPTION_DECLINE_REASON_MATER = "/master/prescriptiondeclinereason";
    
    /**
     * Bank master
     */
    public static final String BANK_MASTER = "/master/banks";
    
    public static final String Blood_GROUP_MASTER_PATH = "master/bloodgroup";

    public static final String MARITAL_STATUS_MASTER = "/master/maritalstatus";

  public static final String RELIGION_MASTER = "/master/religions";

  public static final String GENDER_MASTER = "/master/genders";

    /**
     * Indication For Caesarean Section Master
     */
    public static final String INDICATION_FOR_CAESAREAN_SECTION_MASTER = "/master/indicationforcaesareansection";

    /**
     * Reason for Referral Master
     */
    public static final String REASON_FOR_REFERRAL_MASTER = "/master/reasonforreferral";
    
    /**
     *  Billing counter mapping to user login center
     */
    public static final String BILLING_COUNTER_MAPPING_TO_CENTER = "/master/usercentercounters";
    
    /**
     * Consumption UOM master for store items
     */
    public static final String CONSUMPTION_UOM_PATH = "master/consumptionuom";
    
    /**
     * Code Sets master
     */
    public static final String CODE_SETS_PATH = "master/codesets";
    public static final String DEFAULT_CODE_SETS_FILE = "/pages/master/codesets/defaultCodeSets";

    /**
     * Allergen Master for allergies
     */
    public static final String ALLERGEN_PATH = "/master/allergen";

}



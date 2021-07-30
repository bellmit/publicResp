package com.bob.hms.common;

/**
 * The Class Constants.
 */
public final class Constants {

  /** The constant. */
  private static String constant;

  /** The Constant LOGFILEPATH. */
  public static final String LOGFILEPATH = "logfilepath";

  /** The Constant EXCEPTIONLOGFILEPATH. */
  public static final String EXCEPTIONLOGFILEPATH = "exceptionlogfilepath";

  /** The Constant PRINT_ON_CONSOLE. */
  public static final String PRINT_ON_CONSOLE = "printOnConsole";

  /** The Constant VISIT_TYPE_IP. */
  public static final String VISIT_TYPE_IP = "i";

  /** The Constant VISIT_TYPE_OP. */
  public static final String VISIT_TYPE_OP = "o";

  /** The Constant STRING_Y. */
  public static final String STRING_Y = "Y";

  /** The Constant STRING_N. */
  public static final String STRING_N = "N";

  /** The Constant VISIT_TYPE_DIAG. */
  public static final String VISIT_TYPE_DIAG = "d";

  /** The Constant VISIT_TYPE_INCOMING. */
  public static final String VISIT_TYPE_INCOMING = "t";

  // Insurance Timers Starts Here
  public static final String PRE_AUTH_TIMER = "4";

  /** The Constant CLAIM_TIMER. */
  public static final String CLAIM_TIMER = "6";

  /** The Constant FINAL_CLAIM_TIMER. */
  public static final String FINAL_CLAIM_TIMER = "8";
  // Insurance Timers Ends Here

  /** The Constant ROLEID. */
  public static final String ROLEID = "roleId"; // used in all insta masters

  /** The Constant INSTAADMIN. */
  public static final int INSTAADMIN = 1; // used in all insta masters jsps to
  // check
  // whether it is Insta admin role or not

  /** The Constant admin. */
  public static final String ADMIN = "admin";

  /** The Constant LAB. */
  public static final String LAB = "LABTESTS";

  /** The Constant PROFILE. */
  public static final String PROFILE = "PROFILE TESTS";

  /** The Constant LAB_DEPARTMENT. */
  public static final String LAB_DEPARTMENT = "LABORATORY";

  /** The Constant GENERAL_DEPARTMENT. */
  public static final String GENERAL_DEPARTMENT = "GENERAL";

  /** The Constant ANAESTHEST_DEPARTMENT. */
  public static final String ANAESTHEST_DEPARTMENT = "ANAESTHESIOLOGY";

  /** The Constant ERROR. */
  public static final String ERROR = "error";

  /** The Constant FILE_ID. */
  public static final String FILE_ID = "file_id";

  /** The Constant FILE_NAME. */
  public static final String FILE_NAME = "file_name";

  /** The Constant REPORT_BASE_DIR. */
  public static final String REPORT_BASE_DIR = "/reports";

  /** The Constant REPORT_ID. */
  public static final String REPORT_ID = "report_id";

  /** The Constant REPORT_ID. */
  public static final String PARENT_ID = "parent_id";

  /** The Constant REPORT_METADATA. */
  public static final String REPORT_METADATA = "report_metadata";

  /** The Constant REPORT_NAME. */
  public static final String REPORT_NAME = "report_name";

  /** The Constant REPORT_VAR. */
  public static final String REPORT_VAR = "report_var";

  /** The Constant REPORT_VAR_LABEL. */
  public static final String REPORT_VAR_LABEL = "report_var_label";

  /** The Constant REPORT_VAR_TYPE. */
  public static final String REPORT_VAR_TYPE = "report_var_type";

  /** The Constant DEFAULT_SECONDS_60. */
  public static final Integer DEFAULT_SECONDS_60 = 60;

  /** The Constant DEFAULT_PAGE_SIZE_100. */
  public static final Integer DEFAULT_PAGE_SIZE_100 = 100;

  /** The Constant STRING_CLASS. */
  public static final String STRING_CLASS = "class";

  /** The Constant SRJS_FILE. */
  public static final String SRJS_FILE = "srjsFile";

  /** The Constant REPT_DESC_FILE. */
  public static final String REPT_DESC_FILE = "reptDescFile";

  /** The Constant CUSTOM_REPORT_ID. */
  public static final String CUSTOM_REPORT_ID = "custom_report_id";

  /** The Constant CONTENT_TYPE_PDF. */
  public static final String CONTENT_TYPE_PDF = "application/pdf";

  /** The Constant VITALS. */
  public static final String VITALS = "vitals";

  /** The Constant SECONDARY_COMPLAINTS. */
  public static final String SECONDARY_COMPLAINTS = "secondary_complaints";

  /** The Constant MR_NO. */
  public static final String MR_NO = "mr_no";

  /** The Constant REPEAT_PATIENT_INFO. */
  public static final String REPEAT_PATIENT_INFO = "repeat_patient_info";

  /** The Constant FIELD_ID. */
  public static final String FIELD_ID = "field_id";

  /** The Constant VITAL_PARAMS. */
  public static final String VITAL_PARAMS = "vital_params";

  /** The Constant ORG_ID. */
  public static final String ORG_ID = "org_id";

  /** The Constant CENTER_ID. */
  public static final String CENTER_ID = "center_id";

  /** The Constant PATIENT_ID. */
  public static final String PATIENT_ID = "patient_id";
  /** The Constant VISIT_ID. */
  public static final String VISIT_ID = "visit_id";
  /** API username. */
  public static final String API_USERNAME = "InstaAPI";

  public static final String CONSULTATION_ID = "consultation_id";
  
  public static final String PATIENT_VACCINATION_ID = "pat_vacc_id";

  public static final String DISCHARGE_PRESCRIPTION_NOTES = "discharge_prescription_notes";
  public static final String NOTE_GROUPING_PREFERENCE_NOTE_TYPE = "NT";
  
  public static final String NOTE_GROUPING_PREFERENCE_HOSPITAL_ROLE = "HR";
  
  public static final String NOTE_GROUPING_PREFERENCE_NO_GROUPING = "NG";

  /**
   * Gets the constant value.
   *
   * @param type the type
   * @return the constant value
   */
  public static String getConstantValue(String type) {

    if (type.equalsIgnoreCase("ORG")) {
      constant = "GENERAL";
    } else if (type.equalsIgnoreCase("BEDTYPE")) {
      constant = "GENERAL";
    } else if (type.equalsIgnoreCase("DISCHARGE")) {
      constant = "Discharge";
    } else if (type.equalsIgnoreCase("DISCHARGEREPORT")) {
      constant = "Discharge Report";
    } else if (type.equalsIgnoreCase("BEDPRIORITY")) {
      constant = "R";
    } else if (type.equalsIgnoreCase("DAYCARE")) {
      constant = "DAYCARE";
    } else if (type.equalsIgnoreCase("ANAESTHESIOLOGY")) {
      constant = "ANAESTHESIOLOGY";
    } else if (type.equalsIgnoreCase("BABYBEDTYPE")) {
      constant = "BABYCRADLE";
    } else if (type.equalsIgnoreCase("DIAGNOSTIC")) {
      constant = "DIAGNOSTICS";
    } else if (type.equalsIgnoreCase("SERVICE")) {
      constant = "SERVICE";
    } else if (type.equalsIgnoreCase("OPERATION")) {
      constant = "OPERATION";
    } else if (type.equalsIgnoreCase("VISITING DOCTOR")) {
      constant = "VISITING DOCTOR";
    } else if (type.equalsIgnoreCase("BED")) {
      constant = "BED";
    } else if (type.equalsIgnoreCase("CONSULTING DOCTOR")) {
      constant = "CONSULTING DOCTOR";
    } else if (type.equalsIgnoreCase("PACKAGE")) {
      constant = "NO PACKAGE";
    } else if (type.equalsIgnoreCase("BABY")) {
      constant = "BABY";
    } else if (type.equalsIgnoreCase("PHARMACY")) {
      constant = "PHARMACY SALES";
    } else if (type.equalsIgnoreCase("PACKAGE CHARGE")) {
      constant = "PACKAGE CHARGES";
    } else {
      constant = "";
    }
    return constant;

  }

}

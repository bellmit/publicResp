package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesRepository;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodeTypesRepository;
import com.insta.hms.mdm.services.ServicesRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ClaimValidator that handles all claim level validations for XML generation.
 */
@Scope("prototype")
@Component
public class ClaimValidator {

  /** The claim sub repo. */
  @LazyAutowired
  private InsuranceSubmissionRepository claimSubRepo;

  /** The insurance claim service. */
  @LazyAutowired
  private InsuranceClaimService insuranceClaimService;

  /** The services repository. */
  @LazyAutowired
  private ServicesRepository servicesRepository;

  /** The icd supported code types repository. */
  @LazyAutowired
  private IcdSupportedCodeTypesRepository icdSupportedCodeTypesRepository;

  /** The health authority preferences repository. */
  @LazyAutowired
  private HealthAuthorityPreferencesRepository healthAuthorityPreferencesRepository;

  /** The mrd diagnosis service. */
  @LazyAutowired
  private MRDDiagnosisService mrdDiagnosisService;

  /** The bill claim service. */
  @LazyAutowired
  private BillClaimService billClaimService;

  /** The claim validation helper. */
  @LazyAutowired
  private ClaimValidationHelper claimValidationHelper;

  /** The claim submission service. */
  @LazyAutowired
  private ClaimSubmissionService claimSubmissionService;

  // To:do error messages can be moved to a common file and accessed by selfpay, 
  // preauth and selfpay claim processing
  
  /** The Constant DIAGNOSIS_CODE_ERR. */
  public static final String DIAGNOSIS_CODE_ERR = "<br/> DIAGNOSIS ERROR: "
      + "Claims without diagnosis codes. <br/>"
      + "Please enter diagnosis codes for Patients : <br/>";

  /** The Constant PRIMARY_DIAGNOSIS_CODE_ERR. */
  public static final String PRIMARY_DIAGNOSIS_CODE_ERR = "<br/> PRIMARY DIAGNOSIS ERROR: "
      + "Claims without Primary diagnosis codes. <br/>"
      + "Please enter Primary diagnosis codes for Patients : <br/>";

  /** The Constant PRESENT_ON_ADMISSION_ERROR. */
  public static final String PRESENT_ON_ADMISSION_ERROR = "<br/> PRESENT ON ADMISSION ERROR: "
      + "Diagnosis must contain Present on Admission value. <br/>";

  /** The Constant TOOTH_CODES_ERROR. */
  public static final String TOOTH_CODES_ERROR = "<br/> TOOTH NUMBER ERROR: "
      + "Claims found without tooth number observation(s).<br/> "
      + "Please check the Service(s) for for Patients : <br/>";

  /** The Constant TOOTH_NO_CODES_ERR. */
  public static final String TOOTH_NO_CODES_ERR = "<br/> TOOTH NUMBER CODES ERROR: "
      + "Claims found without tooth number observation code(s).<br/> "
      + "Please check the Observation(s) for Service(s) : <br/>";

  /** The Constant TOOTH_NO_UNIVERSAL_CODE_ERR. */
  public static final String TOOTH_NO_UNIVERSAL_CODE_ERR = "<br/> TOOTH NUMBER NO UNIVERSAL "
      + "CODE ERROR: Claims found without universal code.<br/> "
      + "Please check the Observation(s) for Service(s) : <br/>";

  /** The Constant ATTACHMENT_ERROR. */
  public static final String ATTACHMENT_ERROR = "<br/> ATTACHMENT ERROR: "
      + "Claims which have attachment could not be attached in XML. <br/>"
      + "Please check the attachments for Claims : <br/> ";

  /** The Constant BILL_STATUS_ERROR. */
  public static final String BILL_STATUS_ERROR = "<br/> BILLS OPEN ERROR: "
      + "Claims which have Open bills. <br/>"
      + "Please check the claims with Open Bills: <br/>";

  /** The Constant ENCOUNTERS_ERROR. */
  public static final String ENCOUNTERS_ERROR = "<br/> ENCOUNTERS ERROR: "
      + "Claims without encounter types. <br/>"
      + "Please enter encounter types for Patients : <br/>";

  /** The Constant NO_CLINICIANS_ERROR_KEY. */
  public static final String NO_CLINICIANS_ERROR_KEY = "NO CLINICIANS ERROR:";
  
  /** The Constant NO_CLINICIANS_ERROR. */
  public static final String NO_CLINICIANS_ERROR = "<br/> NO CLINICIANS ERROR: "
      + "Claims without clinician. "
      + "Please enter clinician for Patients : <br/>";

  /** The Constant CLINICIAN_ERROR_KEY. */
  public static final String CLINICIAN_ERROR_KEY = "CLINICIAN ERROR:";
  
  /** The Constant CLINICIAN_ID_ERROR. */
  public static final String CLINICIAN_ID_ERROR = "<br/> CLINICIAN ERROR: "
      + "Claims without clinician Id. "
      + "Please enter clinician id for Doctors : <br/>";

  /** The Constant ORDERING_CLINICIAN_ERROR_KEY. */
  public static final String ORDERING_CLINICIAN_ERROR_KEY = "ORDERING CLINICIAN ERROR:";
  
  /** The Constant ORDERING_CLINICIAN_ERROR. */
  public static final String ORDERING_CLINICIAN_ERROR = "<br/> ORDERING CLINICIAN ERROR: "
      + "Claims without ordering clinician. "
      + "Please enter ordering clinician for Doctors : <br/>";

  /** The Constant CODES_ERROR_KEY. */
  public static final String CODES_ERROR_KEY = "CODES ERROR:";
  
  /** The Constant CODES_ERROR. */
  public static final String CODES_ERROR = "<br/> CODES ERROR: "
      + "Claims found without activity codes. <br/>"
      + "Please check the Bills : <br/>";

  /** The Constant DRUG_CODES_ERROR_KEY. */
  public static final String DRUG_CODES_ERROR_KEY = "DRUG CODES ERROR:";
  
  /** The Constant DRUG_CODES_ERROR. */
  public static final String DRUG_CODES_ERROR = "<br/> DRUG CODES ERROR: "
      + "Claims found without activity codes(Drugs). <br/>"
      + "Please check the Drugs : <br/>";

  /** The Constant ENCOUNTERS_START_ERROR_KEY. */
  public static final String ENCOUNTERS_START_ERROR_KEY = "ENCOUNTERS START ERROR:";
  
  /** The Constant ENCOUNTERS_START_ERROR. */
  public static final String ENCOUNTERS_START_ERROR = "<br/> ENCOUNTERS START ERROR: "
      + "Claims without encounter start types. <br/>"
      + "Please enter encounter start types for Patients : <br/>";

  /** The Constant ENCOUNTERS_END_ERROR. */
  public static final String ENCOUNTERS_END_ERROR = "<br/> ENCOUNTERS END ERROR: "
      + "Claims without encounter end types. <br/>"
      + "Please enter encounter end types for Patients : <br/>";

  /** The Constant ENCOUNTERS_END_DATE_ERROR. */
  public static final String ENCOUNTERS_END_DATE_ERROR = "<br/> ENCOUNTERS END DATE ERROR: "
      + "Claims without encounter end date. <br/>"
      + "Please enter encounter end date for Patients : <br/>";

  /** The Constant ZERO_ACTIVITIES_ERROR_KEY. */
  public static final String ZERO_ACTIVITIES_ERROR_KEY = "ZERO ACTIVITIES ERROR:";
  
  /** The Constant NO_ACTIVITIES_ERROR. */
  public static final String NO_ACTIVITIES_ERROR = "<br/> ZERO ACTIVITIES ERROR: "
      + "Submission has claims with no activities.<br/> "
      + "Please check the claims : <br/>";

  /** The Constant ZERO_BILLS_ERROR_KEY. */
  public static final String ZERO_BILLS_ERROR_KEY = "ZERO BILLS ERROR:";
  
  /** The Constant NO_BILLS_ERROR. */
  public static final String NO_BILLS_ERROR = "<br/> ZERO BILLS ERROR: "
      + "Claims found without bills. <br/>"
      + "Please check the Claims without finalized bills: <br/>";

  /** The Constant CONS_CODES_ERROR_KEY. */
  public static final String CONS_CODES_ERROR_KEY = "CONS CODES ERROR:";
  
  /** The Constant CONS_CODES_ERROR. */
  public static final String CONS_CODES_ERROR = "<br/> CONS CODES ERROR: "
      + "Claims found without observation codes (or) values.<br/> "
      + "Please check the Consultation(s) for Patients : <br/>";

  /** The Constant CONS_COMPLAINT_CODES_ERROR_KEY. */
  public static final String CONS_COMPLAINT_CODES_ERROR_KEY = "CONS COMPLAINT CODES ERROR:";
  
  /** The Constant CONS_COMPLAINT_CODES_ERROR. */
  public static final String CONS_COMPLAINT_CODES_ERROR = "<br/> CONS COMPLAINT CODES ERROR: "
      + "Claims found without presenting-complaint observation codes (or) values.<br/> "
      + "Please check the Consultation(s) for Patients : <br/>";

  /** The Constant DIAG_CODES_ERROR_KEY. */
  public static final String DIAG_CODES_ERROR_KEY = "DIAG CODES ERROR:";
  
  /** The Constant DIAG_CODES_ERROR. */
  public static final String DIAG_CODES_ERROR = "<br/> DIAG CODES ERROR: "
      + "Claims found without observation codes (or) values.<br/> "
      + "Please check the Test(s) for Patients : <br/>";

  /** The errors map. */
  private Map<String, StringBuilder> errorsMap;

  /**
   * Gets the url action map.
   *
   * @return the url action map
   */
  public HashMap getUrlActionMap() {
    return claimValidationHelper.getUrlActionMap();
  }

  /**
   * Sets the url action map.
   *
   * @param urlActionMap the new url action map
   */
  public void setUrlActionMap(HashMap urlActionMap) {
    claimValidationHelper.setUrlActionMap(urlActionMap);
  }

  /**
   * Gets the errors map.
   *
   * @return the errors map
   */
  public Map<String, StringBuilder> getErrorsMap() {
    return errorsMap;
  }

  /**
   * Sets the errors map.
   *
   * @param errorsMap the errors map
   */
  public void setErrorsMap(Map<String, StringBuilder> errorsMap) {
    this.errorsMap = errorsMap;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return claimValidationHelper.getPath();
  }

  /**
   * Sets the path.
   *
   * @param path the new path
   */
  public void setPath(String path) {
    claimValidationHelper.setPath(path);
  }

  /**
   * Validate attachment.
   *
   * @param claim          the claim
   * @param isResubmission the is resubmission
   * @param isAccumed the is accumed
   * @return the string
   */
  public String validateAttachment(BasicDynaBean claim, Boolean isResubmission,
      boolean isAccumed) {
    String claimId = (String) claim.get("claim_id");
    StringBuilder attachmentErr = new StringBuilder(ATTACHMENT_ERROR);

    if (isResubmission || isAccumed) {
      BasicDynaBean attachmentBean = insuranceClaimService.getAttachment(claimId);
      InputStream file = (InputStream) attachmentBean.get("attachment");
      if (file != null) {
        String attachment = claimSubRepo.convertToBase64Binary(file);
        if (attachment == null) {
          if (errorsMap.containsKey("ATTACHMENT ERROR:")) {
            attachmentErr = errorsMap.get("ATTACHMENT ERROR:");
            attachmentErr.append("   ,  ");
          }
          errorsMap.put("ATTACHMENT ERROR:",
              attachmentErr.append(claimValidationHelper.urlString("attachment", claimId, null)));
          return "";
        } else {
          return attachment;
        }
      }
    }
    return "";
  }

  /**
   * Find all diagnosis for a given claim for a specific accountGroup. For pharmacy claims,
   * diagnoses will be brought from mrd_diagnosis. For Hospital claims, diagnoses will be
   * brought from hospital_claim_diagnosis.
   *
   * @param claimPatientId the claim patient id
   * @param accountGroupId the account group id (hospital = 1)
   * @return the list of diagnoses, if accountGroupId is null returns empty list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> findAllDiagnosis(String claimPatientId, Integer accountGroupId)
      throws SQLException {
    // if pharmacy claim, get the diagnosis from mrd_diagnosis table
    if (accountGroupId != 1) {
      return mrdDiagnosisService.findAllDiagnosis(claimPatientId);
    } else {
      // bring the diagnoses from hospital_claim_diagnosis table (Coder edited diagnosis)
      return mrdDiagnosisService.findAllCoderDiagnosis(claimPatientId);
    }
  }

  /**
   * Validate diagnosis and returns the List of diagnosis.
   *
   * @param claim          the claim
   * @param submissionBean the submission bean
   * @return the list of diagnosis associated to the claim
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  // returns list of valid diagnosis after updating any errors
  public List<BasicDynaBean> validateDiagnosis(BasicDynaBean claim,
      BasicDynaBean submissionBean) throws ParseException, SQLException {
    Integer accountGroupId = (Integer) claim.get("claim_account_group");
    String claimPatientId = (String) claim.get("claim_patient_id");
    List<BasicDynaBean> diagnosis = findAllDiagnosis(claimPatientId, accountGroupId);

    StringBuilder diagnosisCodesErr = new StringBuilder(DIAGNOSIS_CODE_ERR);
    StringBuilder primaryDiagnosisCodesErr = new StringBuilder(PRIMARY_DIAGNOSIS_CODE_ERR);
    if (diagnosis == null || diagnosis.isEmpty()) {
      if (errorsMap.containsKey("DIAGNOSIS ERROR:")) {
        diagnosisCodesErr = errorsMap.get("DIAGNOSIS ERROR:");
        diagnosisCodesErr.append("  ,  ");
      }
      errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
          .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
    } else {
      boolean primaryDiagnosisExists = false;
      for (BasicDynaBean diag : diagnosis) {
        String icdCode = (String) diag.get("icd_code");
        String diagType = (String) diag.get("diag_type");
        if (diagType.equals("Principal")) {
          primaryDiagnosisExists = true;
        }
        if (icdCode == null || icdCode.equals("")) {
          if (errorsMap.containsKey("DIAGNOSIS ERROR:")) {
            diagnosisCodesErr = errorsMap.get("DIAGNOSIS ERROR:");
            diagnosisCodesErr.append("   ,  ");
          }
          errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
              .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
        }
        validatePOADiagnosis(claim, errorsMap, diag);
      }
      /*
       * Validation for Primary diagnosis While generating/uploading E-claim xml.
       */
      if (!primaryDiagnosisExists) {
        if (errorsMap.containsKey("PRIMARY DIAGNOSIS ERROR:")) {
          diagnosisCodesErr = errorsMap.get("PRIMARY DIAGNOSIS ERROR:");
          diagnosisCodesErr.append("   ,  ");
        }
        errorsMap.put("PRIMARY DIAGNOSIS ERROR:", primaryDiagnosisCodesErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
      }
    }

    return diagnosis;

  }

  /**
   * Validate Present on admission diagnosis if encounter type = 3 or 4. Internally validates
   * only for claims with encounter date > 7-31-2018. As this is the date for the validation
   * Go-Live. Adds errors to errorsMap if found.
   *
   * @param claim     the claim
   * @param errorsMap the errors map
   * @param diag      the diag
   * @throws ParseException the parse exception
   */
  private void validatePOADiagnosis(BasicDynaBean claim, Map<String, StringBuilder> errorsMap,
      BasicDynaBean diag) throws ParseException {
    Date encounterStartDateObj = new Date();
    StringBuilder presentOnAdmissionError = new StringBuilder(PRESENT_ON_ADMISSION_ERROR);
    String presentOnAdmission = (String) diag.get("present_on_admission");
    String claimPatientId = (String) claim.get("claim_patient_id");
    String visitType = (String) claim.get("visit_type");
    String encounterType = claim.get("encounter_type") != null
        ? ((Integer) claim.get("encounter_type")).toString() : null;
    String encounterStartDateStr = (String) claim.get("start_date"); // This is the patient
    // Registration Date
    Calendar cal = Calendar.getInstance();
    if (StringUtils.isNotEmpty(encounterStartDateStr)) {
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
      encounterStartDateObj = formatter.parse(encounterStartDateStr);
      /*
       * This is date HAAD regulations for Present on Admission go Live. All claims with
       * encounter type 3/4 generated after this date will require the additional present on
       * admission check.
       */
      cal.set(2018, 7, 31);
    }

    if (("3".equals(encounterType) || "4".equals(encounterType))
        && !StringUtils.isNotEmpty(presentOnAdmission)
        && (encounterStartDateObj.compareTo(cal.getTime()) > 0) && visitType.equals("i")) {
      if (errorsMap.containsKey("PRESENT ON ADMISSION ERROR:")) {
        presentOnAdmissionError = errorsMap.get("PRESENT ON ADMISSION ERROR:");
        presentOnAdmissionError.append("   ,  ");
      }
      errorsMap.put("PRESENT ON ADMISSION ERROR:", presentOnAdmissionError
          .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
    }
  }

  /**
   * Validate emirates ID.
   *
   * @param claim    the claim
   * @param regPrefs the reg prefs
   */
  public void validateEmiratesID(BasicDynaBean claim, BasicDynaBean regPrefs) {

    String govenmtIdLabel = regPrefs.get("government_identifier_label") != null
        ? (String) regPrefs.get("government_identifier_label") : "Emirates ID";
    String govenmtIdTypeLabel = regPrefs.get("government_identifier_type_label") != null
        ? (String) regPrefs.get("government_identifier_type_label") : "Emirates ID Type";
    String emiratesIdNumber = claim.get("emirates_id_number") != null
        ? (String) claim.get("emirates_id_number") : null;
    String patientId = (String) claim.get("patient_id");

    StringBuilder govtIdNoErr = new StringBuilder("<br/> EMIRATES ID ERROR: Claims without "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + ".<br/> " + "Please check the "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + " for Patients: <br/>");
    if (emiratesIdNumber == null || emiratesIdNumber.equals("")) {
      if (errorsMap.containsKey("EMIRATES ID ERROR:")) {
        govtIdNoErr = errorsMap.get("EMIRATES ID ERROR:");
        govtIdNoErr.append("   ,  ");
      }
      errorsMap.put("EMIRATES ID ERROR:",
          govtIdNoErr.append(claimValidationHelper.urlString("pre-registration", patientId, null)));
    }
  }

  /**
   * Validate encounters.
   *
   * @param claim    the claim
   * @param regPrefs the reg prefs
   */
  public void validateEncounters(BasicDynaBean claim, BasicDynaBean regPrefs) {
    String visitType = (String) claim.get("visit_type");
    String patientId = (String) claim.get("patient_id");
    String encTypePref = regPrefs.get("encntr_type_reqd") != null
        ? (String) regPrefs.get("encntr_type_reqd") : "RQ";
    String encStartEndTypePref = regPrefs.get("encntr_start_and_end_reqd") != null
        ? (String) regPrefs.get("encntr_start_and_end_reqd") : "RQ";
    String encounterType = claim.get("encounter_type") != null
        ? ((Integer) claim.get("encounter_type")).toString() : null;
    String claimPatientId = (String) claim.get("claim_patient_id");

    String encounterStartType = claim.get("encounter_start_type") != null
        ? ((Integer) claim.get("encounter_start_type")).toString() : null;
    String encounterEndType = claim.get("encounter_end_type") != null
        ? ((Integer) claim.get("encounter_end_type")).toString() : null;
    String endDate = claim.get("end_date") != null ? ((String) claim.get("end_date")) : null;

    StringBuilder encountersErr = new StringBuilder(ENCOUNTERS_ERROR);
    if (((visitType.equals("i") && encTypePref.equals("IP"))
        || (visitType.equals("o") && encTypePref.equals("OP")) || encTypePref.equals("RQ"))
        && (encounterType == null || encounterType.equals("0"))) {
      if (errorsMap.containsKey("ENCOUNTERS ERROR:")) {
        encountersErr = errorsMap.get("ENCOUNTERS ERROR:");
        encountersErr.append("   ,  ");
      }
      errorsMap.put("ENCOUNTERS ERROR:",
          encountersErr.append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
    }

    if ((visitType.equals("i") && encStartEndTypePref.equals("IP"))
        || (visitType.equals("o") && encStartEndTypePref.equals("OP"))
        || encStartEndTypePref.equals("RQ")) {

      if (encounterStartType == null || encounterStartType.equals("0")) {
        StringBuilder encountersStartErr = new StringBuilder(ENCOUNTERS_START_ERROR);
        if (errorsMap.containsKey(ENCOUNTERS_START_ERROR_KEY)) {
          encountersStartErr = errorsMap.get(ENCOUNTERS_START_ERROR_KEY);
          encountersStartErr.append("   ,  ");
        }
        errorsMap.put(ENCOUNTERS_START_ERROR_KEY, encountersStartErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
      }

      if (encounterEndType == null || encounterEndType.equals("0")) {
        StringBuilder encountersEndErr = new StringBuilder(ENCOUNTERS_END_ERROR);
        if (errorsMap.containsKey("ENCOUNTERS END ERROR:")) {
          encountersEndErr = errorsMap.get("ENCOUNTERS END ERROR:");
          encountersEndErr.append("   ,  ");
        }
        errorsMap.put("ENCOUNTERS END ERROR:", encountersEndErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
      }
    }

    if (visitType != null && visitType.equals("i")
        && (endDate == null || endDate.trim().equals(""))) {
      StringBuilder encountersEndDateErr = new StringBuilder(ENCOUNTERS_END_DATE_ERROR);
      if (errorsMap.containsKey("ENCOUNTERS END DATE ERROR:")) {
        encountersEndDateErr = errorsMap.get("ENCOUNTERS END DATE ERROR:");
        encountersEndDateErr.append("  ,  ");
      }
      errorsMap.put("ENCOUNTERS END DATE ERROR:",
          encountersEndDateErr.append(claimValidationHelper.urlString("adt", patientId, null)));
    }
  }

  /**
   * Validate empty claim. Returns false if claims is empty and updates errorsMap.
   *
   * @param claims            the claims
   * @param submissionBatchId the submission batch id
   * @return boolean indicating if claims are not empty
   */
  public boolean isNotEmpty(List<BasicDynaBean> claims, String submissionBatchId) {
    if (claims != null && claims.isEmpty()) {
      StringBuilder noActivitiesErr = new StringBuilder(NO_ACTIVITIES_ERROR);
      if (errorsMap.containsKey(ZERO_ACTIVITIES_ERROR_KEY)) {
        noActivitiesErr = errorsMap.get(ZERO_ACTIVITIES_ERROR_KEY);
        noActivitiesErr.append("   ,  ");
      }
      errorsMap.put(ZERO_ACTIVITIES_ERROR_KEY, noActivitiesErr
          .append(claimValidationHelper.urlString("submission", submissionBatchId, null)));
      return false;
    }
    return true;

  }

  /**
   * Checks if is valid claim.
   *
   * @return the boolean
   */
  // returns true if claim is valid
  public Boolean isValidClaim() {
    return (errorsMap == null || errorsMap.isEmpty());
  }

  /**
   * Validate empty bills.
   *
   * @param bills   the bills
   * @param claimId the claim id
   * @return the boolean
   */
  // returns true if no error exists, ie. bills is not empty
  public Boolean isNotEmptyBills(List<BasicDynaBean> bills, String claimId) {

    if (bills.isEmpty()) {
      StringBuilder noBillsErr = new StringBuilder(NO_BILLS_ERROR);
      if (errorsMap.containsKey(ZERO_BILLS_ERROR_KEY)) {
        noBillsErr = errorsMap.get(ZERO_BILLS_ERROR_KEY);
        noBillsErr.append("   ,  ");
      }
      errorsMap.put(ZERO_BILLS_ERROR_KEY,
          noBillsErr.append(claimValidationHelper.urlString("claim", claimId, null)));
      return false;
    }
    return true;
  }

  /**
   * Validate open bill.
   *
   * @param bill  the bill
   * @param claim the claim
   */
  public void validateOpenBill(BasicDynaBean bill, BasicDynaBean claim) {
    StringBuilder billStatusErr = new StringBuilder(BILL_STATUS_ERROR);
    String claimId = (String) claim.get("claim_id");
    String status = (String) bill.get("status");
    if (status.equals("A")) {
      if (errorsMap.containsKey("BILLS OPEN ERROR:")) {
        billStatusErr = errorsMap.get("BILLS OPEN ERROR:");
        billStatusErr.append(" , ");
      }
      errorsMap.put("BILLS OPEN ERROR:",
          billStatusErr.append(claimValidationHelper.urlString("claim", claimId, null)));
      
    }
  }

  /**
   * Validate the observations for a dental charge.
   *
   * @param billNo       the bill no
   * @param observations the observations
   * @param charge       the charge
   * @param claim        the claim
   */
  public void validateDentalService(String billNo, List<Map> observations,
      BasicDynaBean charge, BasicDynaBean claim) {

    StringBuilder toothCodesErr = new StringBuilder(TOOTH_CODES_ERROR);
    StringBuilder toothNoCodesErr = new StringBuilder(TOOTH_NO_CODES_ERR);
    StringBuilder toothNoUniversalCodeErr = new StringBuilder(TOOTH_NO_UNIVERSAL_CODE_ERR);
    String actDescriptionId = (String) charge.get("act_description_id");
    String claimPatientId = (String) claim.get("claim_patient_id");
    String actDescription = (String) charge.get("act_description");
    String postedDate = (String) charge.get("posted_date");

    BasicDynaBean service = servicesRepository.findByKey("service_id", actDescriptionId);
    if (service != null && service.get("tooth_num_required").equals("Y")) {
      if (observations == null || observations.isEmpty()) {
        if (errorsMap.containsKey("TOOTH NUMBER ERROR:")) {
          toothCodesErr = errorsMap.get("TOOTH NUMBER ERROR:");
          toothCodesErr.append("   ,  ");
        }
        errorsMap.put("TOOTH NUMBER ERROR:", toothCodesErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
        toothCodesErr.append("<br/>Service :" + billNo + "( " + actDescription
            + ", Posted Date: " + postedDate + " ), <br/> ");
      }
      int numOfDentalCodes = 0;
      if (observations != null && !observations.isEmpty()) {
        for (Map observation : observations) {
          String resultCode = (String) observation.get("code");
          String resultType = (String) observation.get("type");
          BasicDynaBean haadBean =
              icdSupportedCodeTypesRepository.findByKey("code_type", resultType);
          Integer haadCode = haadBean == null ? null : (Integer) haadBean.get("haad_code");
          if (haadCode != null && haadCode.intValue() == 16) {
            ++numOfDentalCodes;
          }
          if (resultCode == null || resultCode.equals("")) {
            if (errorsMap.containsKey("TOOTH NUMBER CODES ERROR:")) {
              toothNoCodesErr = errorsMap.get("TOOTH NUMBER CODES ERROR:");
              toothNoCodesErr.append("   ,  ");
            }
            errorsMap.put("TOOTH NUMBER CODES ERROR:", toothNoCodesErr
                .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
            toothNoCodesErr.append("<br/>Service :" + billNo + "( " + actDescription
                + ", Posted Date: " + postedDate + " ), <br/> ");
          }
        }
      }
      if (numOfDentalCodes == 0) {
        if (errorsMap.containsKey("TOOTH NUMBER NO UNIVERSAL CODE ERROR:")) {
          toothNoUniversalCodeErr = errorsMap.get("TOOTH NUMBER NO UNIVERSAL CODE ERROR:");
          toothNoUniversalCodeErr.append("   ,  ");
        }
        errorsMap.put("TOOTH NUMBER NO UNIVERSAL CODE ERROR:", toothNoUniversalCodeErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
        toothNoUniversalCodeErr.append("<br/>Service :" + billNo + "( " + actDescription
            + ", Posted Date: " + postedDate + " ), <br/> ");
      }
    }
  }

  /**
   * Validate lab codes.
   *
   * @param observations the observations
   * @param charge       the charge
   * @param claim        the claim
   * @param billNo       the bill no
   */
  public void validateLabCodes(List<Map> observations, BasicDynaBean charge,
      BasicDynaBean claim, String billNo) {

    String claimPatientId = (String) claim.get("claim_patient_id");
    String postedDate = (String) charge.get("posted_date");
    String actDesc = (String) charge.get("act_description");
    String chargeHead = (String) charge.get("charge_head");
    if (observations != null && !observations.isEmpty() && chargeHead.equals("LTDIA")) {

      /* Check for codes for each observation if lab test i.e LTDIA */
      for (Map observation : observations) {
        String resultCode = (String) observation.get("code");
        String resultValue = (String) observation.get("value");
        String obsType = (String) observation.get("type");
        // observations of type file can have null value as it will
        // be base64 encoded file (from patient_documents) when xml
        // is generated
        if ((resultCode != null && !resultCode.equals(""))
            && ((resultValue == null || resultValue.equals(""))
                && (!"File".equalsIgnoreCase(obsType)))) {

          StringBuilder diagCodesErr = new StringBuilder(DIAG_CODES_ERROR);
          if (errorsMap.containsKey(DIAG_CODES_ERROR_KEY)) {
            diagCodesErr = errorsMap.get(DIAG_CODES_ERROR_KEY);
            diagCodesErr.append("   ,  ");
          }
          errorsMap.put(DIAG_CODES_ERROR_KEY, diagCodesErr
              .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
          diagCodesErr.append("<br/>Test :" + billNo + "( " + actDesc + ", Posted Date: "
              + postedDate + " ), <br/> ");
        }
      }
    } // Observations

  }

  /**
   * Validate consultation codes.
   *
   * @param healthAuthority the health authority
   * @param observations    the observations
   * @param charge          the charge
   * @param claim           the claim
   * @param billNo          the bill no
   */
  public void validateConsultationCodes(String healthAuthority, List<Map> observations,
      BasicDynaBean charge, BasicDynaBean claim, String billNo) {

    String visitType = (String) claim.get("visit_type");
    String chargeGroup = (String) charge.get("charge_group");
    String claimPatientId = (String) claim.get("claim_patient_id");
    String postedDate = (String) charge.get("posted_date");
    String actDesc = (String) charge.get("act_description");

    /* Check for complaint observation for DHA claim if consultation i.e DOC */
    if (!visitType.equals("i") && healthAuthority.equals("DHA") && chargeGroup.equals("DOC")
        && (observations == null || observations.isEmpty())) {
      StringBuilder consComplaintCodesErr = new StringBuilder(CONS_COMPLAINT_CODES_ERROR);
      if (errorsMap.containsKey(CONS_COMPLAINT_CODES_ERROR_KEY)) {
        consComplaintCodesErr = errorsMap.get(CONS_COMPLAINT_CODES_ERROR_KEY);
        consComplaintCodesErr.append("   ,  ");
      }
      errorsMap.put(CONS_COMPLAINT_CODES_ERROR_KEY, consComplaintCodesErr
          .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
      consComplaintCodesErr.append("<br/>Consultation :" + billNo + "( " + actDesc
          + ", Posted Date: " + postedDate + " ), <br/> ");
    }

    if (observations != null && !observations.isEmpty() && chargeGroup.equals("DOC")) {

      /* Check for value for each observation if consultation i.e DOC */

      boolean hasPresentingComplaint = false;
      for (Map observation : observations) {
        String resultCode = (String) observation.get("code");
        String resultValue = (String) observation.get("value");
        if (resultCode != null && resultCode.equals("Presenting-Complaint")) {
          hasPresentingComplaint = true;
        }
        if ((resultCode != null && !resultCode.equals(""))
            && (resultValue == null || resultValue.equals(""))) {
          StringBuilder consCodesErr = new StringBuilder(CONS_CODES_ERROR);
          if (errorsMap.containsKey(CONS_CODES_ERROR_KEY)) {
            consCodesErr = errorsMap.get(CONS_CODES_ERROR_KEY);
            consCodesErr.append("   ,  ");
          }
          errorsMap.put(CONS_CODES_ERROR_KEY, consCodesErr
              .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
          consCodesErr.append("<br/>Consultation :" + billNo + "( " + actDesc
              + ", Posted Date: " + postedDate + " ), <br/> ");
        }
      }
      if (!visitType.equals("i") && healthAuthority.equals("DHA") && !hasPresentingComplaint) {
        StringBuilder consComplaintCodesErr = new StringBuilder(CONS_COMPLAINT_CODES_ERROR);
        if (errorsMap.containsKey(CONS_COMPLAINT_CODES_ERROR_KEY)) {
          consComplaintCodesErr = errorsMap.get(CONS_COMPLAINT_CODES_ERROR_KEY);
          consComplaintCodesErr.append("   ,  ");
        }
        errorsMap.put(CONS_COMPLAINT_CODES_ERROR_KEY, consComplaintCodesErr
            .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
        consComplaintCodesErr.append("<br/>Consultation :" + billNo + "( " + actDesc
            + ", Posted Date: " + postedDate + " ), <br/> ");
      }
    }

  }

  /**
   * Validate codes.
   *
   * @param billNo          the bill no
   * @param chHeadMap       the ch head map
   * @param charge          the charge
   * @param claim           the claim
   * @param healthAuthority the health authority
   */
  public void validateCodes(String billNo, Map<String, BasicDynaBean> chHeadMap,
      BasicDynaBean charge, BasicDynaBean claim, String healthAuthority) {
    String chargeHead = (String) charge.get("charge_head");
    String itemCode = (String) charge.get("item_code");
    String actDesc = (String) charge.get("act_description");
    String postedDate = (String) charge.get("posted_date");
    String claimPatientId = (String) claim.get("claim_patient_id");

    String activityChargeId = (String) charge.get("activity_charge_id");

    BasicDynaBean chrgBean = chHeadMap.get(chargeHead);
    if (chrgBean != null && !chargeHead.equals("ADJDRG")) {
      String codificationSupported = chrgBean.get("codification_supported") != null
          && !chrgBean.get("codification_supported").equals("")
              ? (String) chrgBean.get("codification_supported") : "N";

      if (itemCode == null || itemCode.trim().equals("")) {
        if (activityChargeId.startsWith("A")) {
          StringBuilder codesErr = new StringBuilder(CODES_ERROR);
          if (codificationSupported != null && codificationSupported.equals("Y")) {
            if (errorsMap.containsKey(CODES_ERROR_KEY)) {
              codesErr = errorsMap.get(CODES_ERROR_KEY);
              codesErr.append("   ,  ");
            }
            errorsMap.put(CODES_ERROR_KEY, codesErr
                .append(claimValidationHelper.urlString("diagnosis", claimPatientId, null)));
            codesErr.append("( " + actDesc + ", Posted Date: " + postedDate + " ), <br/> ");
          } else {
            if (errorsMap.containsKey(CODES_ERROR_KEY)) {
              codesErr = errorsMap.get(CODES_ERROR_KEY);
            }
            errorsMap.put(CODES_ERROR_KEY,
                codesErr.append(claimValidationHelper.urlString("bill", billNo, null)));
            codesErr.append("( " + actDesc + ", Posted Date: " + postedDate + " ), <br/> ");
            
          }
        } else if (activityChargeId.startsWith("P")) {
          Map<String, String> idMap = new HashMap<>();
          idMap.put("chargeId", (String) charge.get("charge_id"));
          idMap.put("billNo", (String) charge.get("bill_no"));
          idMap.put("saleItemId", ((String) charge.get("activity_charge_id")).split("-")[2]);
          StringBuilder drugCodesErr = new StringBuilder(DRUG_CODES_ERROR);
          if (errorsMap.containsKey(DRUG_CODES_ERROR_KEY)) {
            drugCodesErr = errorsMap.get(DRUG_CODES_ERROR_KEY);
          }
          errorsMap.put(DRUG_CODES_ERROR_KEY,
              drugCodesErr.append(claimValidationHelper.urlString("drug",
                  ((String) charge.get("activity_charge_id")).split("-")[2], actDesc)));
          drugCodesErr.append(" , ");
        }
      }
    }

  }

  /**
   * Validate clinician related information.
   *
   * @param charge          the charge
   * @param claim           the claim
   * @param healthAuthority the health authority
   */
  public void validateClinician(BasicDynaBean charge, BasicDynaBean claim, String healthAuthority) {
    String claimPatientId = (String) claim.get("claim_patient_id");
    validateEmptyClinician(charge, claimPatientId);
    validateClinicianId(charge);
    validateOrderingClinician(charge, healthAuthority);
  }

  /**
   * Validate ordering clinician.
   *
   * @param charge          the charge
   * @param healthAuthority the health authority
   */
  private void validateOrderingClinician(BasicDynaBean charge, String healthAuthority) {
    String prescribingDoctorName = (String) charge.get("prescribing_doctor_name");
    String prescribingDoctorId = (String) charge.get("prescribing_doctor_id");
    String prescribingDoctorLicenseNumber = (String) charge
        .get("prescribing_doctor_license_number");
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("health_authority", healthAuthority);
    BasicDynaBean healthAuthorityPreference = healthAuthorityPreferencesRepository
        .findByKey(filterMap);

    if (healthAuthorityPreference.get("presc_doctor_as_ordering_clinician").equals("Y")
        && prescribingDoctorName != null && (prescribingDoctorLicenseNumber == null
            || prescribingDoctorLicenseNumber.trim().equals(""))) {

      StringBuilder orderingClinicianErr = new StringBuilder(ORDERING_CLINICIAN_ERROR);
      if (errorsMap.containsKey(ORDERING_CLINICIAN_ERROR_KEY)) {
        orderingClinicianErr = errorsMap.get(ORDERING_CLINICIAN_ERROR_KEY);
        orderingClinicianErr.append("   ,  ");
      }
      if (!orderingClinicianErr.toString().contains(prescribingDoctorName)) {
        errorsMap.put(ORDERING_CLINICIAN_ERROR_KEY, orderingClinicianErr.append(
            claimValidationHelper.urlString("doctor", prescribingDoctorId, prescribingDoctorName)));
      }
    }

  }

  /**
   * Validate clinician id.
   *
   * @param charge the charge
   */
  private void validateClinicianId(BasicDynaBean charge) {
    String doctorLicenseNumber = (String) charge.get("doctor_license_number");
    String doctorType = (String) charge.get("doctor_type");
    String doctorName = (String) charge.get("doctor_name");
    String doctorId = (String) charge.get("doctor_id");
    if (doctorLicenseNumber == null || doctorLicenseNumber.trim().equals("")) {

      StringBuilder clinicianIdErr = new StringBuilder(CLINICIAN_ID_ERROR);
      if ("Doctor".equals(doctorType)) {
        if (errorsMap.containsKey(CLINICIAN_ERROR_KEY)) {
          clinicianIdErr = errorsMap.get(CLINICIAN_ERROR_KEY);
          clinicianIdErr.append("   ,  ");
        }
        if (StringUtils.isNotBlank(doctorName) && !clinicianIdErr.toString().contains(doctorName)) {
          errorsMap.put(CLINICIAN_ERROR_KEY, clinicianIdErr
              .append(claimValidationHelper.urlString("doctor", doctorId, doctorName)));
        }

      } else {
        if (errorsMap.containsKey(CLINICIAN_ERROR_KEY)) {
          clinicianIdErr = errorsMap.get(CLINICIAN_ERROR_KEY);
          clinicianIdErr.append("   ,  ");
        }
        if (StringUtils.isNotBlank(doctorName)) {
          if (!clinicianIdErr.toString().contains(doctorName)) {
            errorsMap.put(CLINICIAN_ERROR_KEY, clinicianIdErr
                .append(claimValidationHelper.urlString("referral", doctorId, doctorName)));
          }
        }

      }
    }
  }

  /**
   * Validate empty clinician.
   *
   * @param charge         the charge
   * @param claimPatientId the claim patient id
   */
  private void validateEmptyClinician(BasicDynaBean charge, String claimPatientId) {
    String doctorName = (String) charge.get("doctor_name");
    if (doctorName == null || doctorName.trim().equals("")) {
      StringBuilder noCliniciansErr = new StringBuilder(NO_CLINICIANS_ERROR);
      if (errorsMap.containsKey(NO_CLINICIANS_ERROR_KEY)) {
        noCliniciansErr = errorsMap.get(NO_CLINICIANS_ERROR_KEY);
        noCliniciansErr.append("   ,  ");
      }
      if (!noCliniciansErr.toString().contains(claimPatientId)) {
        errorsMap.put(NO_CLINICIANS_ERROR_KEY, noCliniciansErr
            .append(claimValidationHelper.urlString("patient", claimPatientId, null)));
      }
    }
  }

  /**
   * Validate empty charges.
   *
   * @param charges           the charges
   * @param submissionBatchId the submission batch id
   */
  public void validateEmptyCharges(List<BasicDynaBean> charges, String submissionBatchId) {
    if (charges.isEmpty()) {
      StringBuilder noActivitiesErr = new StringBuilder(NO_ACTIVITIES_ERROR);
      if (errorsMap.containsKey(ZERO_ACTIVITIES_ERROR_KEY)) {
        noActivitiesErr = errorsMap.get(ZERO_ACTIVITIES_ERROR_KEY);
        noActivitiesErr.append("   ,  ");
      }
      errorsMap.put(ZERO_ACTIVITIES_ERROR_KEY, noActivitiesErr
          .append(claimValidationHelper.urlString("submission", submissionBatchId, null)));
    }
  }
  
  /**
  * Validate empty activity.
  *
  * @param charges  the charges
  * @param claimId  the claim id
  */
  public void validateEmptyActivity(List<BasicDynaBean> charges, String claimId) {
    if (charges.isEmpty()) {
      StringBuilder noActivitiesErr = new StringBuilder(NO_ACTIVITIES_ERROR);
      if (errorsMap.containsKey(ZERO_ACTIVITIES_ERROR_KEY)) {
        noActivitiesErr = errorsMap.get(ZERO_ACTIVITIES_ERROR_KEY);
        noActivitiesErr.append("   ,  ");
      }
      errorsMap.put(ZERO_ACTIVITIES_ERROR_KEY, noActivitiesErr
          .append(claimValidationHelper.urlString("claim", claimId, null)));
    }
  }

}

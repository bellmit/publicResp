package com.insta.hms.core.insurance;

import com.insta.hms.billing.Eclaim;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesRepository;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.integration.insurance.accumed.AccumedClaimHelper;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsRepository;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesRepository;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodeTypesRepository;
import com.insta.hms.mdm.services.ServicesRepository;
import com.insta.hms.mdm.tpas.TpaService;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class ClaimProcessor that handles claim XML generation.
 */
@Component
public class ClaimProcessor {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(ClaimProcessor.class);

  /** The insurance submission repository. */
  @LazyAutowired
  private InsuranceSubmissionRepository insuranceSubmissionRepository;

  /** The patient registration service. */
  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The internal code processor. */
  @LazyAutowired
  private RegistrationPreferencesService registrationPreferenceService;

  /** The registration preferences repository. */
  @LazyAutowired
  private RegistrationPreferencesRepository registrationPreferencesRepository;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The health authority preferences repository. */
  @LazyAutowired
  private HealthAuthorityPreferencesRepository healthAuthorityPreferencesRepository;

  /** The patient details repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository;

  /** The insurance claim service. */
  @LazyAutowired
  private InsuranceClaimService insuranceClaimService;

  /** The icd supported code types repository. */
  @LazyAutowired
  private IcdSupportedCodeTypesRepository icdSupportedCodeTypesRepository;

  /** The helper. */
  @LazyAutowired
  private ClaimGeneratorHelper helper;

  /** The accumed helper. */
  @LazyAutowired
  private AccumedClaimHelper accumedHelper;

  /** The services repository. */
  @LazyAutowired
  private ServicesRepository servicesRepository;

  /** The charge heads repository. */
  @LazyAutowired
  private ChargeHeadsRepository chargeHeadsRepository;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The claim submission service. */
  @LazyAutowired
  private ClaimSubmissionService claimSubmissionService;

  /** The claim validation helper. */
  @LazyAutowired
  private ClaimValidationHelper claimValidationHelper;

  /** The accounting group service. */
  @LazyAutowired
  private AccountingGroupService accountingGroupService;

  /** The claim validator. */
  @LazyAutowired
  private BeanFactory beanFactory;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /**
   * Process.
   *
   * @param submissionBatchId the submission batch id
   * @param healthAuthority the health authority
   * @param urlActionMap the url action map
   * @param rightsUrlMap the rights url map
   * @param path the path
   * @param isTesting the is testing
   * @param isAccumed the is accumed
   * @param centerid the centerid
   * @param isNewClaim the is new claim
   * @throws SQLException the exception
   */
  public void process(String submissionBatchId, String healthAuthority, HashMap urlActionMap,
      HashMap rightsUrlMap, String path, Boolean isTesting, boolean isAccumed,
      Integer centerid, boolean isNewClaim) throws SQLException {

    BasicDynaBean submissionBean =
        insuranceSubmissionRepository.findByKey("submission_batch_id", submissionBatchId);
    ClaimValidator claimValidator = beanFactory.getBean(ClaimValidator.class);
    claimValidator.setPath(path);
    claimValidator.setUrlActionMap(urlActionMap);
    claimValidator.setErrorsMap(new HashMap<String, StringBuilder>());
    List<Map> allEclaimMap = new ArrayList<>();
    List<BasicDynaBean> claims = claimSubmissionService.getClaims(submissionBatchId);
    if (claimValidator.isNotEmpty(claims, submissionBatchId)) {
      for (BasicDynaBean claim : claims) {
        try {
          Map claimBodyMap =
              processClaim(claim, healthAuthority, submissionBean, claimValidator, isAccumed);
          if (!claimBodyMap.isEmpty()) {
            allEclaimMap.add(claimBodyMap);
          }
        } catch (SQLException | ParseException ex) {
          logger.error("ERROR while validating claims for XML generation : " + ex);
          claimFailure(submissionBean, claimValidator);
        }
      }
    }

    Map<String, StringBuilder> errorsMap = claimValidator.getErrorsMap();
    if (errorsMap != null && !errorsMap.isEmpty()) {
      claimFailure(submissionBean, claimValidator);
      return;
    } else {
      // write the claims to file
      try {
        writeClaimsToFile(allEclaimMap, healthAuthority, submissionBean, isTesting, isAccumed,
            centerid, isNewClaim);
      } catch (IOException | TemplateException | SQLException ex) {
        logger.error("ERROR while writing claims to XML : " + ex);
        claimFailure(submissionBean, claimValidator);
        return;
      }
      // set processing status to completed
      setBatchSuccess(submissionBean);
    }
  }

  /**
   * Sets the batch success.
   *
   * @param submissionBean the new batch success
   */
  private void setBatchSuccess(BasicDynaBean submissionBean) {
    // set claim generation procession status to Success.
    submissionBean.set("processing_status", "C");
    submissionBean.set("processing_error", "");
    Map<String, Object> keys = new HashMap<>();
    keys.put("submission_batch_id", submissionBean.get("submission_batch_id"));
    claimSubmissionService.update(submissionBean, keys);

  }

  /**
   * Sets the batch failure.
   *
   * @param submissionBean the submission bean
   * @param errString the err string
   */
  private void setBatchFailure(BasicDynaBean submissionBean, String errString) {
    // set claim generation procession status to Failure.
    submissionBean.set("processing_status", "F");
    submissionBean.set("processing_error", errString);
    Map<String, Object> keys = new HashMap<>();
    keys.put("submission_batch_id", submissionBean.get("submission_batch_id"));
    claimSubmissionService.update(submissionBean, keys);

  }

  /**
   * Sets failed status and error message to submission batch.
   *
   * @param submissionBean the submission bean
   * @param claimValidator the claim validator
   */
  private void claimFailure(BasicDynaBean submissionBean, ClaimValidator claimValidator) {
    StringBuilder errString = new StringBuilder(
        "Error(s) while XML data check. Claim Submission XML could not be generated.<br/>"
            + "Please correct (or) update the following and generate e-claim again.<br/>");

    Map<String, StringBuilder> errorsMap = claimValidator.getErrorsMap();
    Iterator keys = errorsMap.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      StringBuilder errorString = errorsMap.get(key);
      errString.append("<br/>" + errorString);
    }
    // set claim generation procession status to Failed.
    setBatchFailure(submissionBean, errString.toString());
  }

  /**
   * Create an file and write the xml header + claim objects + xml footer.
   *
   * @param allEclaimMap the all eclaim map
   * @param healthAuthority the health authority
   * @param submissionBean the submission bean
   * @param isTesting the is testing
   * @param isAccumed the is accumed
   * @param centerId the center id
   * @param isNewClaim the is new claim
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException the SQL exception
   */
  private void writeClaimsToFile(List<Map> allEclaimMap, String healthAuthority,
      BasicDynaBean submissionBean, Boolean isTesting, boolean isAccumed, Integer centerId,
      boolean isNewClaim) throws IOException, TemplateException, SQLException {

    // first write header
    String fileName = submissionBean.get("file_name") != null
        ? (String) submissionBean.get("file_name") + ".xml" : "eClaim.xml";
    // check if directory exists
    String dirPath = "/var/log/insta/insta-ia-sync/";
    Path path = Paths.get(dirPath);
    if (!Files.exists(path)) {
      Files.createDirectory(path);
      logger.info("insta-ia-sync Directory created at /var/log/insta/insta-ia-sync/");
    }
    File claimXmlFile = new File("/var/log/insta/insta-ia-sync/" + fileName);
    claimXmlFile.createNewFile();
    try (FileOutputStream claimXmlStream = new FileOutputStream(claimXmlFile)) {
      BasicDynaBean headerbean =
          insuranceSubmissionRepository.getXmlHeaderFields(submissionBean, healthAuthority);
      headerbean.set("disposition_flag",
          getHeaderDispositionFlag((Integer) submissionBean.get("center_id"), isTesting));
      headerbean.set("claims_count", allEclaimMap.size());
      Map headerMap = new HashMap<>();
      Map headerBeanMap = headerbean.getMap();
      if (MapUtils.isNotEmpty(headerBeanMap)) {
        headerMap.putAll(headerBeanMap);
      }
      if (isAccumed) {
        headerMap.putAll(accumedHelper.getAccumedHeaders(allEclaimMap, isNewClaim));
      }
      helper.addClaimHeader(claimXmlStream, headerMap, healthAuthority, isAccumed);

      // then write claims
      String claimId = "";
      Integer priority = null;
      for (Map bodyMap : allEclaimMap) {
        claimId = (String) bodyMap.get("claim_id");
        priority = (Integer) bodyMap.get("priority");
        String claimBodyTemplate = "";
        if (isAccumed) {
          claimBodyTemplate = "/Accumed/AccumedEclaimBody.ftl";
        } else if ("HAAD".equalsIgnoreCase(healthAuthority)
            || "DHA".equalsIgnoreCase(healthAuthority)) {
          claimBodyTemplate = "/Eclaim/" + healthAuthority.toLowerCase() + "/EclaimBody.ftl";
          if (billChargeService.getDRGMarginExist(claimId, priority)) {
            claimBodyTemplate =
                "/Eclaim/" + healthAuthority.toLowerCase() + "/DrgEclaimBody.ftl";
          }
        } else {
          claimBodyTemplate = "/Eclaim/EclaimBody.ftl";
          if (billChargeService.getDRGMarginExist(claimId, priority)) {
            claimBodyTemplate = "/Eclaim/DrgEclaimBody.ftl";
          }
        }
        helper.addClaimBody(claimXmlStream, bodyMap, claimBodyTemplate);
      }

      // then write footer
      helper.addClaimFooter(claimXmlStream, new HashMap(), isAccumed);
      claimXmlStream.flush();
      logger.debug("XML processing ends. File written to filesystem successfully.");
    }
    if (isAccumed && centerId != null) {
      try {
        accumedHelper.sendFileThroughFTP(claimXmlFile, fileName, centerId);
      } catch (Exception exception) {
        logger.error("Unable to send Accumed XML through FTP", exception);
      }
    }
  }

  /**
   * Gets the header disposition flag.
   *
   * @param centerId the center id
   * @param isTesting the is testing
   * @return the header disposition flag
   */
  private String getHeaderDispositionFlag(Integer centerId, Boolean isTesting) {
    BasicDynaBean centerBean = centerService.findByKey(centerId);
    String healthAuthority = (String) centerBean.get("health_authority");
    Boolean isEclaimActive = "Y".equals(centerBean.get("eclaim_active"));
    String dispositionFlag = "";
    if ("HAAD".equalsIgnoreCase(healthAuthority)) {
      if (isEclaimActive && !isTesting) { // Active // Mode
        dispositionFlag = "PRODUCTION";
      } else if (isEclaimActive) {
        dispositionFlag = "TEST";
      } else {
        dispositionFlag = "PTE_SUBMIT";
      }
    } else if ("DHA".equalsIgnoreCase(healthAuthority)) {
      if (isTesting) {
        dispositionFlag = "TEST";
      } else {
        dispositionFlag = "PRODUCTION";
      }
    }
    return dispositionFlag;
  }

  /**
   * Validates claim errors and returns a map containing claim information to be consumed by
   * the FTL (to write claims to the file), if no errors exist.
   *
   * @param claim the claim
   * @param healthAuthority the health authority
   * @param submissionBean the submission bean
   * @param claimValidator the claim validator
   * @param isAccumed the is accumed
   * @return the map
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private Map processClaim(BasicDynaBean claim, String healthAuthority,
      BasicDynaBean submissionBean, ClaimValidator claimValidator, boolean isAccumed)
      throws SQLException, ParseException {

    Boolean isResubmission = submissionBean.get("is_resubmission") != null
        ? ((String) submissionBean.get("is_resubmission")).equals("Y") : false;

    // object to be sent to the ftl to be rendered
    Eclaim eclaim = new Eclaim();
    eclaim.setIsResubmission(isResubmission);
    // validate the claim

    BasicDynaBean regPrefs = registrationPreferenceService.getRegistrationPreferences();

    claimValidator.validateEmiratesID(claim, regPrefs);
    claimValidator.validateEncounters(claim, regPrefs);
    eclaim.setDiagnosis(claimValidator.validateDiagnosis(claim, submissionBean));
    eclaim.setAttachment(claimValidator.validateAttachment(claim, isResubmission, isAccumed));

    String claimId = (String) claim.get("claim_id");
    String sponsorId = (String) claim.get("sponsor_id");
    eclaim.setClaim(claim);

    List<BasicDynaBean> bills = insuranceSubmissionRepository.findAllBills(claimId);
    Boolean hasBills = claimValidator.isNotEmptyBills(bills, claimId);
    if (isAccumed) {
      accumedHelper.setAccumedClaimDetails(claim, eclaim, hasBills);
    }
    if (hasBills) {
      Map<String, List<Map>> obBeanMap =
          findAllObservationsMap(claimId, sponsorId, healthAuthority);
      Map<String, List<Map>> observationsMapForFTL = new HashMap<>();

      List<BasicDynaBean> allCharges = new ArrayList<>();
      // validate all the bills in the claim
      for (BasicDynaBean bill : bills) {
        validateAndProcessBill(bill, submissionBean, healthAuthority, obBeanMap, claim,
            observationsMapForFTL, claimValidator, allCharges);
      }

      claimValidator.validateEmptyActivity(allCharges, claimId);
      Map<String, StringBuilder> errorsMap = claimValidator.getErrorsMap();
      if (errorsMap == null || errorsMap.isEmpty()) {
        String healthAuthorityPreference = (String) healthAuthorityPreferencesRepository
            .findByKey("health_authority", healthAuthority)
            .get("presc_doctor_as_ordering_clinician");
        return getClaimMap(submissionBean, claim, observationsMapForFTL, eclaim,
            healthAuthorityPreference, isAccumed, healthAuthority);
      }

    }

    // if errored out return empty map
    return new HashMap();
  }



  /**
   * Gets the claim map.
   *
   * @param submissionBean the submission bean
   * @param claim the claim
   * @param observationsMapForFTL the observations map for FTL
   * @param eclaim the eclaim
   * @param healthAuthorityPreference the health authority preference
   * @param isAccumed the is accumed
   * @return the claim map
   * @throws SQLException the SQL exception
   */
  private Map getClaimMap(BasicDynaBean submissionBean, BasicDynaBean claim,
      Map<String, List<Map>> observationsMapForFTL, Eclaim eclaim,
      String healthAuthorityPreference, boolean isAccumed, String healthAuthority)
      throws SQLException {

    Boolean isResubmission = submissionBean.get("is_resubmission") != null
        ? ((String) submissionBean.get("is_resubmission")).equals("Y") : false;
    String claimId = (String) claim.get("claim_id");
    String resubmissionType = claim.get("resubmission_type") != null
        ? ((String) claim.get("resubmission_type")) : null;
    List<BasicDynaBean> allBillCharges;
    if (isResubmission && resubmissionType != null
        && (resubmissionType.equalsIgnoreCase("internal complaint")
        || resubmissionType.equalsIgnoreCase("reconciliation"))) {
      allBillCharges = insuranceClaimService.findAllChargesForXML(claimId,
          (Boolean) submissionBean.get("is_external_pbm_batch"), true, isAccumed);
    } else {
      allBillCharges = insuranceClaimService.findAllChargesForXML(claimId,
          (Boolean) submissionBean.get("is_external_pbm_batch"), false, isAccumed);
    }

    List<String> contractNames = null;
    if ("DHA".equals(healthAuthority)) {
      contractNames = findAllContractNames(allBillCharges);
    }

    if (!allBillCharges.isEmpty()) {
      eclaim.setCharges(allBillCharges);
      eclaim.setContractNames(contractNames);

      // if (isAccumed) {
      // eclaim.setObservationsMap(
      // accumedHelper.generateObservationsForCharges(allBillCharges));
      // } else {
      eclaim.setObservationsMap(observationsMapForFTL);
      // }
      if (isResubmission && ("internal complaint".equalsIgnoreCase(resubmissionType)
          || ("reconciliation").equalsIgnoreCase(resubmissionType))) {
        eclaim.setDrgAdjustmentAmt(BigDecimal.ZERO);
      } else {
        Integer priority = (Integer) claim.get("priority");
        eclaim.setDrgAdjustmentAmt(billChargeService.getDRGAdjustmentAmt(claimId, priority));
      }

      Map bodyMap = new HashMap();
      bodyMap.put("eclaim", eclaim);
      bodyMap.put("accGrpList", accountingGroupService.listAll());
      bodyMap.put("healthHAADPref", healthAuthorityPreference);
      return bodyMap;
    }
    return observationsMapForFTL;

  }

  /**
   * Validate and process bill.
   *
   * @param bill the bill
   * @param submissionBean the submission bean
   * @param healthAuthority the health authority
   * @param obBeanMap the ob bean map
   * @param claim the claim
   * @param observationsMapForFTL the observations map for FTL
   * @param claimValidator the claim validator
   * @throws SQLException the SQL exception
   */
  private void validateAndProcessBill(BasicDynaBean bill, BasicDynaBean submissionBean,
      String healthAuthority, Map<String, List<Map>> obBeanMap, BasicDynaBean claim,
      Map<String, List<Map>> observationsMapForFTL, ClaimValidator claimValidator,
      List<BasicDynaBean> allCharges) throws SQLException {

    Boolean isResubmission = submissionBean.get("is_resubmission") != null
        ? ((String) submissionBean.get("is_resubmission")).equals("Y") : false;
    String submissionBatchId = (String) submissionBean.get("submission_batch_id");
    claimValidator.validateOpenBill(bill, claim);
    String status = (String) bill.get("status");
    String billNo = (String) bill.get("bill_no");
    String claimId = (String) claim.get("claim_id");
    String resubmissionType = claim.get("resubmission_type") != null
        ? ((String) claim.get("resubmission_type")) : null;
    List<BasicDynaBean> charges;
    if (isResubmission && resubmissionType != null
        && (resubmissionType.equalsIgnoreCase("internal complaint")
        || resubmissionType.equalsIgnoreCase("reconciliation"))) {
      charges = findAllCharges(claimId, billNo, true);
    } else {
      charges = findAllCharges(claimId, billNo, false);
    }
    allCharges.addAll(charges);
    if (CollectionUtils.isNotEmpty(charges) && (status.equals("F") || status.equals("C"))) {
      List<String> chHeadList;
      chHeadList = findAllChargeHead(charges);
      Map<String, BasicDynaBean> chHeadMap = getChargeHeadMap(chHeadList);

      /* Check for codes for each charge */
      for (BasicDynaBean charge : charges) {
        String chargeId = (String) charge.get("charge_id");
        List<Map> observations = null;
        validateAndProcessCharge(charge, healthAuthority, chHeadMap, billNo, obBeanMap, claim,
            claimValidator);

        // Here we retrieve a map of charge_id to observations in order to do validation
        // against
        // each observation.
        observations = obBeanMap.get(chargeId);
        /*
         * All observation validations completed. Create the claim_activity_id -> observation
         * map that will be sent to the FTL for final XML generation.
         */
        setObservationMapForFTL(charge, observationsMapForFTL, observations, healthAuthority,
            claim);
      }
    } // Charges
  }

  /**
   * Find all charges.
   *
   * @param claimId the claim id
   * @param billNo the bill no
   * @param isResubmission the is resubmission
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllCharges(String claimId, String billNo,
      Boolean isResubmission) throws SQLException {

    BasicDynaBean billBean = billService.findByKey(billNo);
    if (billBean != null) {
      BasicDynaBean visitbean = patientRegistrationService.findByKey("patient_id",
          (String) billBean.get("visit_id"));
      boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
      boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
      boolean checkDrgOrPerDiem = (hasDRG || hasPerdiem);
      return insuranceClaimService.findAllCharges(claimId, billNo, isResubmission,
          checkDrgOrPerDiem);
    }
    return Collections.emptyList();
  }

  /**
   * Create/Updates the observationMapForFTL that is sent to FTL for final XML Generation
   * Maintains a mapping between claim_activity_id -> observations.
   *
   * @param charge the charge
   * @param observationsMapForFTL the observations map for FTL
   * @param observations the observations
   * @param healthAuthority the health authority
   * @param claim the claim
   */
  @SuppressWarnings({"unchecked"})
  private void setObservationMapForFTL(BasicDynaBean charge,
      Map<String, List<Map>> observationsMapForFTL, List<Map> observations,
      String healthAuthority, BasicDynaBean claim) {

    String claimActivityId = (String) charge.get("claim_activity_id");
    List<Map> observationList = null;
    Map patientShareObservationMap = null;
    if (observationsMapForFTL.containsKey(claimActivityId)) {
      observationList = observationsMapForFTL.get((String) charge.get("claim_activity_id"));
      // Need this map to set patient share for combined activities
      patientShareObservationMap = getPatientShareObservations(observationList);
    } else {
      observationList = new ArrayList<>();
    }
    // if hospital charge the add to observationsMapForFTL
    // as claim_activity_id -> observations
    if (((String) charge.get("activity_group")).equalsIgnoreCase("Hospital")) {
      setHospitalObsMapForFTL(observations, healthAuthority, charge, observationList,
          observationsMapForFTL, claim);
    } else if (((String) charge.get("activity_group")).equalsIgnoreCase("Pharmacy")) {
      setPharmacyObsMapForFTL(charge, observationList, observationsMapForFTL);
    }

    /**
     * Sending patient share as an observations for items which are having haad code as
     * 5(Drug/Drug HAAD).
     * 
     */

    setPatientShareObservations(healthAuthority, charge, claim, patientShareObservationMap,
        observationList, observationsMapForFTL);
  }

  /**
   * Method to get the patient observation map.
   *
   * @param observationList the observation list
   * @return the patient share observations
   */
  private Map getPatientShareObservations(List<Map> observationList) {
    for (Map obs : observationList) {
      String code = (String) obs.get("code");
      if (code.equals("Drug patient share")) {
        return obs;
      }
    }
    return null;
  }

  /**
   * Method to send patient share as observation in claim xml for activities which are having
   * haad code as Drug/Drug HAAD.
   *
   * @param healthAuthority the health authority
   * @param charge the charge
   * @param claim the claim
   * @param patientShareObservationMap the patient share observation map
   * @param observationList the observation list
   * @param observationsMapForFTL the observations map for FTL
   */
  private void setPatientShareObservations(String healthAuthority, BasicDynaBean charge,
      BasicDynaBean claim, Map patientShareObservationMap, List<Map> observationList,
      Map<String, List<Map>> observationsMapForFTL) {

    BigDecimal amount = (BigDecimal) charge.get("amount");
    BigDecimal claimAmt = (BigDecimal) charge.get("insurance_claim_amt");
    String claimActivityId = (String) charge.get("claim_activity_id");

    String useDRG = (String) claim.get("use_drg");
    String usePerdiem = (String) claim.get("use_perdiem");

    Integer actType = null != charge.get("act_type") ? (Integer) charge.get("act_type") : 0;

    // Patient share observation should go as 0 for drg /perdiem claims.
    if (actType != null && healthAuthority.equals("HAAD") && actType == 5
        && !usePerdiem.equals("Y")) {
      if (null == patientShareObservationMap) {
        HashMap chargeMap = new HashMap<>();
        chargeMap.put("type", "Text");
        BigDecimal actValue = useDRG.equals("Y") ? BigDecimal.ZERO : amount.subtract(claimAmt);
        chargeMap.put("value", actValue);
        chargeMap.put("code", "Drug patient share");
        chargeMap.put("value_type", "AED");
        observationList.add(chargeMap);

        observationsMapForFTL.put(claimActivityId, observationList);
      } else {
        /*
         * For combined activities modifying patient share observation value as sum of all
         * activities patient share
         */
        BigDecimal patientShare = (BigDecimal) patientShareObservationMap.get("value");
        BigDecimal actValue = useDRG.equals("Y") ? BigDecimal.ZERO
            : patientShare.add(amount.subtract(claimAmt));
        patientShareObservationMap.put("value", actValue);
      }
    }
  }

  /**
   * Creates eligibility_reference_number as observation. Checks for
   * enable_eligibility_auth_in_xml=='O' in ha_tpa_code mapping table.
   * 
   * @param charge the charge
   * @param healthAuthority the health authority
   * @param claim the claim
   * @return the patient share observations
   * 
   */
  private HashMap setEligibilityAuthObsMapForFTL(BasicDynaBean charge, String healthAuthority,
      BasicDynaBean claim) {

    String sponsorId = (String) claim.get("sponsor_id");
    String chargeGroup = (String) charge.get("charge_group");
    if (healthAuthority.equals("HAAD")) {
      // bring the health authority to tpa preferences
      Map<String, String> keys = new HashMap<>();
      keys.put("tpa_id", sponsorId);
      keys.put("health_authority", healthAuthority);
      BasicDynaBean healthAuthTpaPrefBean = tpaService.getTpaHealthAuthorityDetails(sponsorId,
          (Integer) claim.get("batch_center_id"));
      if (healthAuthTpaPrefBean != null) {
        String enableEligibilityAuthInXml =
            (String) healthAuthTpaPrefBean.get("enable_eligibility_auth_in_xml");
        Boolean enableEligibilityAuth =
            (Boolean) healthAuthTpaPrefBean.get("enable_eligibility_authorization");
        if (chargeGroup.equals("DOC") && enableEligibilityAuth
            && enableEligibilityAuthInXml.equals("O")) {
          // set it as observation
          HashMap obsMap = new HashMap<>();
          obsMap.put("type", "Text");
          obsMap.put("value", (String) claim.get("eligibility_reference_number"));
          obsMap.put("code", "Observation Note");
          obsMap.put("value_type", "TEXT");

          // remove eligibility reference number so it doesn't populate in the xml
          claim.set("eligibility_reference_number", null);
          return obsMap;
        } else if (enableEligibilityAuthInXml.equals("N")) {
          // remove eligibility reference number so it doesn't populate in the xml
          claim.set("eligibility_reference_number", null);
        }
      }
    }
    return null;
  }

  /**
   * Sets the pharmacy obs map for FTL.
   *
   * @param charge the charge
   * @param observationList the observation list
   * @param observationsMapForFTL the observations map for FTL
   */
  @SuppressWarnings("unchecked")
  private void setPharmacyObsMapForFTL(BasicDynaBean charge, List<Map> observationList,
      Map<String, List<Map>> observationsMapForFTL) {
    List<String> activatedModules = securityService.getActivatedModules();
    Boolean modEclaimErx = activatedModules.contains("mod_eclaim_erx");

    if (modEclaimErx && StringUtils.isNotEmpty((String) charge.get("erx_activity_id"))
        && StringUtils.isNotEmpty((String) charge.get("erx_reference_no"))) {

      /**
       * Adds the erx activity related observations to existing observationMap. Incase of
       * pharmacy claims with Erx we need to add erx observations Refer
       * https://practo.atlassian.net/wiki/spaces/HIMS/pages/895483954/EmirateClinics+Pharmacy+
       * Claim+Management
       */
      HashMap chargeMap = new HashMap<>();
      chargeMap.put("type", "ERX");
      chargeMap.put("value", charge.get("erx_activity_id"));
      chargeMap.put("code", charge.get("erx_reference_no"));
      chargeMap.put("value_type", "Reference");
      observationList.add(chargeMap);
      String claimActivityId = (String) charge.get("claim_activity_id");
      observationsMapForFTL.put(claimActivityId, observationList);
    }

  }

  /**
   * Sets the hospital obs map for FTL.
   *
   * @param observations the observations
   * @param healthAuthority the health authority
   * @param charge the charge
   * @param observationList the observation list
   * @param observationsMapForFTL the observations map for FTL
   * @param claim the claim
   */
  private void setHospitalObsMapForFTL(List<Map> observations, String healthAuthority,
      BasicDynaBean charge, List<Map> observationList,
      Map<String, List<Map>> observationsMapForFTL, BasicDynaBean claim) {

    // set eligibilty auth as observation based on preference
    Map eligObs = setEligibilityAuthObsMapForFTL(charge, healthAuthority, claim);
    if (eligObs != null) {
      observationList.add(eligObs);
    }
    String claimActivityId = (String) charge.get("claim_activity_id");
    // call dha specific observations code
    if (observations != null) {
      for (Map observation : observations) {
        // only make the following Observation changes after date mentioned in
        // calendar object
        if ("DHA".equals(healthAuthority)) {
          String chargeGroup = (String) charge.get("charge_group");
          String chargeHead = (String) charge.get("charge_head");
          if (chargeGroup.equalsIgnoreCase("SNP")) {
            setDhaDentalObservations(observation, charge);
          } else if (chargeHead.equalsIgnoreCase("LTDIA")) {
            setDhaLabObservations(observation, charge);
          } else if (chargeGroup.equalsIgnoreCase("DOC")) {
            setDhaVitalObservations(observation, charge);
          }
        }
      }

      observationList.addAll(observations);
      observationsMapForFTL.put(claimActivityId, observationList);
    }
    if (observationList != null) {
      observationsMapForFTL.put(claimActivityId, observationList);
    }
  }

  /**
   * Sets the dha dental observations.
   *
   * @param observation the observation
   * @param charge the charge
   */
  private void setDhaDentalObservations(Map observation, BasicDynaBean charge) {
    // Refer https://practo.atlassian.net/browse/HMS-24306
    if (observation.get("type").equals("Universal Dental")) {
      observation.put("value", observation.get("code"));
      observation.put("value_type", "ToothNumber");
    } else if (observation.get("type").equals("Result") && charge.get("conducted_date") != null) {
      // Refer https://practo.atlassian.net/browse/HMS-24304
      observation.put("value_type", charge.get("conducted_date"));
    }
  }

  /**
   * Sets the value type in an observation as the item posted date from the corresponding
   * charge.
   *
   * @param observation the observation
   * @param charge the charge
   */
  private void setDhaVitalObservations(Map observation, BasicDynaBean charge) {
    if (observation.get("type").equals("Result") && charge.get("item_posted_date") != null) {
      // Refer https://practo.atlassian.net/browse/HMS-24304
      observation.put("value_type",
          new SimpleDateFormat("dd/MM/yyyy").format(charge.get("item_posted_date")));
    }
  }

  /**
   * Sets the dha lab observations.
   *
   * @param observation the observation
   * @param charge the charge
   */
  private void setDhaLabObservations(Map observation, BasicDynaBean charge) {
    if (observation.get("type").equals("Result") && charge.get("conducted_date") != null) {
      // Refer https://practo.atlassian.net/browse/HMS-24304
      observation.put("value_type",
          new SimpleDateFormat("dd/MM/yyyy").format(charge.get("conducted_date")));
    }

  }

  /**
   * Validate and process charge.
   *
   * @param charge the charge
   * @param healthAuthority the health authority
   * @param chHeadMap the ch head map
   * @param billNo the bill no
   * @param obBeanMap the ob bean map
   * @param claim the claim
   * @param claimValidator the claim validator
   */
  private void validateAndProcessCharge(BasicDynaBean charge, String healthAuthority,
      Map<String, BasicDynaBean> chHeadMap, String billNo, Map<String, List<Map>> obBeanMap,
      BasicDynaBean claim, ClaimValidator claimValidator) {

    List<Map> observations = null;
    String chargeId = (String) charge.get("charge_id");
    // validate clinician related stuff
    claimValidator.validateClinician(charge, claim, healthAuthority);

    claimValidator.validateCodes(billNo, chHeadMap, charge, claim, healthAuthority);

    observations = obBeanMap.get(chargeId);

    /* Check for tooth number if required if dental service i.e SNP */
    String chargeGroup = (String) charge.get("charge_group");
    if (chargeGroup.equalsIgnoreCase("SNP")) {
      claimValidator.validateDentalService(billNo, observations, charge, claim);
    }

    claimValidator.validateConsultationCodes(healthAuthority, observations, charge, claim,
        billNo);

    claimValidator.validateLabCodes(observations, charge, claim, billNo);

  }

  /**
   * Find all observations map.
   *
   * @param claimId the claim id
   * @param sponsorId the sponsor id
   * @param eclaimXMLSchema the eclaim XML schema
   * @return the map
   */
  private Map<String, List<Map>> findAllObservationsMap(String claimId, String sponsorId,
      String eclaimXMLSchema) {
    Map<String, List<Map>> obBeanMap = new HashMap<>();
    List<BasicDynaBean> observationsBean =
        insuranceClaimService.findAllClaimObservations(claimId, sponsorId, eclaimXMLSchema);
    List<Map> nw = null;

    for (BasicDynaBean chbean : observationsBean) {
      String chargeId = (String) chbean.get("charge_id");
      // set value as base64 encoded attached document
      if (chbean.get("document_id") != null && chbean.get("file_bytes") != null
          && "File".equalsIgnoreCase((String) (chbean.get("type")))) {
        String fileBase64Str;
        try {
          fileBase64Str = Base64.encodeBase64String(
              IOUtils.toByteArray((ByteArrayInputStream) chbean.get("file_bytes")));
          chbean.set("value", fileBase64Str);
        } catch (IOException ex) {
          logger.error("Unable to read file data for charge Id %s" + chargeId + ex);
        }
      }
      if (obBeanMap.containsKey(chargeId)) {
        nw = obBeanMap.get((String) chbean.get("charge_id"));
        // creates a non read only map
        nw.add(new DynaBeanMapDecorator(chbean, false));
        obBeanMap.put(chargeId, nw);
      } else {
        nw = new ArrayList<>();
        // creates a non read only map
        nw.add(new DynaBeanMapDecorator(chbean, false));
        obBeanMap.put(chargeId, nw);
      }
    }
    return obBeanMap;
  }

  /**
   * Gets the charge head map.
   *
   * @param chHeadList the ch head list
   * @return the charge head map
   */
  private Map<String, BasicDynaBean> getChargeHeadMap(List<String> chHeadList) {
    Map<String, BasicDynaBean> chBeanMap = new HashMap<>();
    List<BasicDynaBean> chHeadBean = chargeHeadsRepository.getChargeHeadBean(chHeadList);
    for (BasicDynaBean chbean : chHeadBean) {
      chBeanMap.put((String) chbean.get("chargehead_id"), chbean);
    }
    return chBeanMap;
  }

  /**
   * Find all charge head.
   *
   * @param charges the charges
   * @return the list
   */
  private List<String> findAllChargeHead(List<BasicDynaBean> charges) {
    List<String> ls = new ArrayList<>();
    for (BasicDynaBean charge : charges) {
      ls.add((String) charge.get("charge_head"));
    }
    return ls;
  }

  /**
   * To find special service contract names from all charges.
   * @param charges list of charges
   * @return list of contract names
   */
  private List<String> findAllContractNames(List<BasicDynaBean> charges) {
    List<String> contractNames = new ArrayList<>();
    for (BasicDynaBean charge : charges) {
      if (StringUtils.isNotBlank((String) charge.get("special_service_contract_name"))) {
        contractNames.add((String) charge.get("special_service_contract_name"));
      }
    }
    return contractNames;
  }
}

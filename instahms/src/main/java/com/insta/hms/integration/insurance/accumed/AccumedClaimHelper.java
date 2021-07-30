package com.insta.hms.integration.insurance.accumed;

import com.insta.hms.billing.ClaimSubmissionDAO;
import com.insta.hms.billing.Eclaim;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.SectionDetailsRepository;
import com.insta.hms.core.insurance.InsuranceClaimService;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsRepository;
import com.insta.hms.documents.PlanDocsDetailsRepository;
import com.insta.hms.integration.connectors.FtpUtility;
import com.insta.hms.integration.insurance.accumed.AccumedXmlValues.AccumedXmlTagName;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AccumedClaimHelper.
 */

@Component
public class AccumedClaimHelper {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AccumedClaimHelper.class);

  /** The section details repository. */
  @LazyAutowired
  SectionDetailsRepository sectionDetailsRepository;

  /** The vital parameter repository. */
  @LazyAutowired
  VitalParameterRepository vitalParameterRepository;

  /** The plan docs details repository. */
  @LazyAutowired
  PlanDocsDetailsRepository planDocsDetailsRepository;

  /** The center preferences service. */
  @LazyAutowired
  CenterPreferencesService centerPreferencesService;

  /** The insurance claim service. */
  @LazyAutowired
  InsuranceClaimService insuranceClaimService;

  /** The mrd observations repository. */
  @LazyAutowired
  MRDObservationsRepository mrdObservationsRepository;


  /**
   * Generate clinical data.
   *
   * @param patientId the patient id
   * @param claimId the claim id
   * @param sponsorId the sponsor id
   * @param hasBills the has bills
   * @return the map
   */
  private Map<AccumedXmlTagName, String> generateClinicalData(String patientId, String claimId,
      String sponsorId, Boolean hasBills) {
    HashMap<AccumedXmlTagName, String> clinicalDataMap = new HashMap<>();
    setClinicalDataInMap(clinicalDataMap, patientId, claimId, sponsorId, hasBills);
    setVitalDetailsInMap(clinicalDataMap, patientId);
    return clinicalDataMap;
  }

  /**
   * Sets the vital details in map.
   *
   * @param clinicalDataMap the clinical data map
   * @param patientId the patient id
   */
  private void setVitalDetailsInMap(HashMap<AccumedXmlTagName, String> clinicalDataMap,
      String patientId) {
    List<BasicDynaBean> vitalDetails =
        vitalParameterRepository.getVitalDetailsByPatientId(patientId);
    if (CollectionUtils.isNotEmpty(vitalDetails)) {
      StringBuilder patientVitalDetails = new StringBuilder();
      for (BasicDynaBean vitalDetail : vitalDetails) {
        patientVitalDetails
            .append(StringUtils.trimToEmpty((String) vitalDetail.get("vital_details")));
        patientVitalDetails.append(" , ");
      }
      clinicalDataMap.put(AccumedXmlTagName.VITAL_SIGNS, patientVitalDetails.toString());
    }
  }

  /**
   * Sets the clinical data in map.
   *
   * @param clinicalDataMap the clinical data map
   * @param patientId the patient id
   * @param claimId the claim id
   * @param sponsorId the sponsor id
   * @param hasBills the has bills
   */
  private void setClinicalDataInMap(HashMap<AccumedXmlTagName, String> clinicalDataMap,
      String patientId, String claimId, String sponsorId, Boolean hasBills) {
    List<BasicDynaBean> clinicalData =
        sectionDetailsRepository.getClinicalDataByPatientId(patientId);
    if (CollectionUtils.isNotEmpty(clinicalData)) {
      for (BasicDynaBean clinicalDatum : clinicalData) {
        String fieldNameValue = (String) clinicalDatum.get("field_name_value");
        String sectionType = (String) clinicalDatum.get("section_type");
        if (StringUtils.isNotBlank(fieldNameValue)) {
          if (StringUtils.equals(sectionType, "Main Symptoms")) {
            clinicalDataMap.put(AccumedXmlTagName.MAIN_SYMPTOMS, fieldNameValue);
          } else if (StringUtils.equals(sectionType, "Physical Exam")) {
            clinicalDataMap.put(AccumedXmlTagName.PHYSICAL_EXAM, fieldNameValue);
          } else if (StringUtils.equals(sectionType, "Past History")) {
            clinicalDataMap.put(AccumedXmlTagName.PAST_HISTORY, fieldNameValue);
          }
        }
      }
    }

    if (hasBills) {
      List<BasicDynaBean> observationsBean =
          insuranceClaimService.findAllClaimObservations(claimId, sponsorId, "DHA");
      StringBuilder chiefComplaint = new StringBuilder();
      if (CollectionUtils.isNotEmpty(observationsBean)) {
        for (BasicDynaBean observation : observationsBean) {
          String observationType = (String) observation.get("type");
          if (StringUtils.equalsIgnoreCase("text", observationType)) {
            chiefComplaint.append(StringUtils.trimToEmpty((String) observation.get("value")));
          }
        }
      }
      clinicalDataMap.put(AccumedXmlTagName.CHIEF_COMPLAINT,
          StringUtils.trimToEmpty(chiefComplaint.toString()));
    }
  }

  /**
   * Sets the accumed claim details.
   *
   * @param claim the claim
   * @param eclaim the eclaim
   * @param hasBills the has bills
   */
  public void setAccumedClaimDetails(BasicDynaBean claim, Eclaim eclaim, Boolean hasBills) {
    String patientId = (String) claim.get("claim_patient_id");
    String claimId = (String) claim.get("claim_id");
    String sponsorId = (String) claim.get("sponsor_id");
    eclaim.setClinicalData(generateClinicalData(patientId, claimId, sponsorId, hasBills));
    eclaim.setCardAttachment(getInsuranceCardDetails(claim));
    normalizeTypeValues(claim);
  }

  /**
   * Normalize type values.
   *
   * @param claim the claim
   */
  private void normalizeTypeValues(BasicDynaBean claim) {
    String insuranceCardType = (String) claim.get("card_type");
    claim.set("card_type",
        AccumedXmlValues.AttachmentType.getAttachmentTypeXmlValue(insuranceCardType));
  }

  /**
   * Gets the insurance card details.
   *
   * @param claim the claim
   * @return the insurance card details
   */
  private String getInsuranceCardDetails(BasicDynaBean claim) {
    String patientId = (String) claim.get("claim_patient_id");
    Integer priority = (Integer) claim.get("priority");
    if (priority == null) {
      // Populating primary docs if priority is not set.
      priority = 1;
    }
    if (StringUtils.isNotBlank(patientId)) {
      List<BasicDynaBean> insurancePlanDocuments =
          planDocsDetailsRepository.getPlanDocumentsFromVisitID(patientId, priority);
      if (CollectionUtils.isNotEmpty(insurancePlanDocuments)) {
        try {
          ClaimSubmissionDAO claimSubmissionDAO = new ClaimSubmissionDAO();
          return claimSubmissionDAO.convertToBase64Binary(
              (ByteArrayInputStream) insurancePlanDocuments.get(0).get("doc_content_bytea"));
        } catch (IOException ioException) {
          logger.error("Error while adding Insurance card Photo ", ioException);
        }
      }
    }
    return null;
  }

  /**
   * Send file through FTP.
   *
   * @param claimXmlFile the claim xml file
   * @param fileName the file name
   * @param centerId the center id
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean sendFileThroughFTP(File claimXmlFile, String fileName, Integer centerId)
      throws Exception {
    if (centerId != null) {
      BasicDynaBean centerPreferences =
          centerPreferencesService.getCenterPreferences(centerId);
      if (centerPreferences != null) {
        String accumedFtpUrl = (String) centerPreferences.get("accumed_ftp_url");
        String accumedFtpUserName = (String) centerPreferences.get("accumed_ftp_username");
        String accumedFtpPassword = (String) centerPreferences.get("accumed_ftp_password");
        if (StringUtils.isNoneBlank(accumedFtpPassword, accumedFtpUrl, accumedFtpUserName)) {
          return FtpUtility.copyFileToFTPServer(claimXmlFile, fileName, accumedFtpUrl,
              accumedFtpUserName, accumedFtpPassword);
        }
      }
    }
    return true;
  }

  /**
   * Generate observations for charges.
   *
   * @param billCharges the bill charges
   * @return the map
   */
  public Map<String, List<Map<String, String>>> generateObservationsForCharges(
      List<BasicDynaBean> billCharges) {
    Map<String, List<Map<String, String>>> observationsMap = new HashMap<>();
    try {
      if (CollectionUtils.isNotEmpty(billCharges)) {
        for (BasicDynaBean billCharge : billCharges) {
          String chargeId = (String) billCharge.get("charge_id");
          List<BasicDynaBean> observations =
              mrdObservationsRepository.findAllObservations(chargeId);
          if (CollectionUtils.isNotEmpty(observations)) {
            populateMrdObservationsMap(observationsMap, chargeId, observations);
          }
          BasicDynaBean presentingComplaint =
              mrdObservationsRepository.getPresentingComplaint(chargeId);
          if (presentingComplaint != null) {
            populatePresentingComplaintInObservationsMap(observationsMap, presentingComplaint,
                chargeId);
          }
        }
      }
    } catch (Exception exception) {
      logger.error("Exception occurred while generating observations for charges in Accumed ",
          exception);
    }
    return observationsMap;
  }

  /**
   * Populate presenting complaint in observations map.
   *
   * @param observationsMap the observations map
   * @param presentingComplaint the presenting complaint
   * @param chargeId the charge id
   */
  private void populatePresentingComplaintInObservationsMap(
      Map<String, List<Map<String, String>>> observationsMap,
      BasicDynaBean presentingComplaint, String chargeId) {
    Map<String, String> observationDetailsMap = new HashMap<>();
    observationDetailsMap.put("Type",
        StringUtils.trimToEmpty((String) presentingComplaint.get("observation_type")));
    observationDetailsMap.put("ValueType",
        StringUtils.trimToEmpty((String) presentingComplaint.get("code")));
    observationDetailsMap.put("Value",
        StringUtils.trimToEmpty((String) presentingComplaint.get("value")));
    observationDetailsMap.put("Code",
        StringUtils.trimToEmpty((String) presentingComplaint.get("code")));
    addToObservationsMap(observationsMap, chargeId, observationDetailsMap);
  }

  /**
   * Populate mrd observations map.
   *
   * @param observationsMap the observations map
   * @param chargeId the charge id
   * @param observations the observations
   */
  private void populateMrdObservationsMap(
      Map<String, List<Map<String, String>>> observationsMap, String chargeId,
      List<BasicDynaBean> observations) {
    for (BasicDynaBean observation : observations) {
      Map<String, String> observationDetailsMap = new HashMap<>();
      observationDetailsMap.put("Type",
          StringUtils.trimToEmpty((String) observation.get("TYPE")));
      observationDetailsMap.put("ValueType",
          StringUtils.trimToEmpty((String) observation.get("value_type")));
      observationDetailsMap.put("Value",
          StringUtils.trimToEmpty((String) observation.get("value")));
      observationDetailsMap.put("Code",
          StringUtils.trimToEmpty((String) observation.get("code")));
      addToObservationsMap(observationsMap, chargeId, observationDetailsMap);
    }
  }

  /**
   * Adds the to observations map.
   *
   * @param observationsMap the observations map
   * @param chargeId the charge id
   * @param observationDetailsMap the observation details map
   */
  private void addToObservationsMap(Map<String, List<Map<String, String>>> observationsMap,
      String chargeId, Map<String, String> observationDetailsMap) {
    List<Map<String, String>> observationsList = observationsMap.get(chargeId);
    if (observationsList == null) {
      observationsList = new ArrayList<>();
      observationsList.add(observationDetailsMap);
      observationsMap.put(chargeId, observationsList);
    } else {
      observationsList.add(observationDetailsMap);
    }
  }

  /**
   * Gets the accumed headers.
   *
   * @param allEclaimMap the all eclaim map
   * @param isNewClaim the is new claim
   * @return the accumed headers
   */
  public Map<String, String> getAccumedHeaders(List<Map> allEclaimMap, boolean isNewClaim) {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("operation", isNewClaim ? "New" : "Update");
    List<Date> encounterStartDates = getStartDateListFromClaims(allEclaimMap);
    if (CollectionUtils.isNotEmpty(encounterStartDates)) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      headerMap.put("from_date", dateFormat.format(Collections.min(encounterStartDates)));
      headerMap.put("to_date", dateFormat.format(Collections.max(encounterStartDates)));
    }
    return headerMap;
  }

  /**
   * Gets the start date list from claims.
   *
   * @param allEclaimMap the all eclaim map
   * @return the start date list from claims
   */
  @SuppressWarnings("unchecked")
  private List<Date> getStartDateListFromClaims(List<Map> allEclaimMap) {
    List<Date> startDateList = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    if (CollectionUtils.isNotEmpty(allEclaimMap)) {
      for (Map eclaim : allEclaimMap) {
        Eclaim eclaimObject = (Eclaim) eclaim.get("eclaim");
        if (eclaimObject != null) {
          BasicDynaBean claimBean = eclaimObject.getClaim();
          if (claimBean != null) {
            String startDate = (String) claimBean.get("start_date");
            try {
              if (StringUtils.isNotBlank(startDate)) {
                startDateList.add(dateFormat.parse(startDate));
              }
            } catch (ParseException parseException) {
              logger.warn(
                  "Unable to parse date " + parseException.getMessage() + " for claim Id: "
                      + StringUtils.trimToEmpty((String) claimBean.get("claim_id")));
            }
          }
        }
      }
    }
    return startDateList;
  }
}

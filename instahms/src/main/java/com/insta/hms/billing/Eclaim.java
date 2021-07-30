package com.insta.hms.billing;

import com.insta.hms.integration.insurance.accumed.AccumedXmlValues.AccumedXmlTagName;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.MapUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class Eclaim.
 */
public class Eclaim {

  /** The claim. */
  BasicDynaBean claim;
  
  /** The diagnosis. */
  List diagnosis;
  
  /** The bills. */
  List bills;
  
  /** The charges. */
  List charges;
  
  /** The observations map. */
  Map observationsMap;
  
  /** The attachment. */
  String attachment;
  
  /** The presc doctor as ordering clinician. */
  String prescDoctorAsOrderingClinician;
  
  /** The is resubmission. */
  Boolean isResubmission;

  /** The plan co pay list. */
  // For Accumed XML
  List planCoPayList;
  
  /** The card attachment. */
  String cardAttachment;
  
  /** The supported attachments. */
  List supportedAttachments;
  
  /** The drg adjustment amt. */
  BigDecimal drgAdjustmentAmt;
  
  /** The clinical data. */
  Map<AccumedXmlTagName, String> clinicalData;

  List<String> contractNames;


  /**
   * Gets the clinical data.
   *
   * @return the clinical data
   */
  public Map<String, String> getClinicalData() {
    Map<String, String> clinicalDataMap = new HashMap<>();
    if (MapUtils.isNotEmpty(clinicalData)) {
      for (Map.Entry<AccumedXmlTagName, String> entry : clinicalData.entrySet()) {
        clinicalDataMap.put(entry.getKey().getTagName(), entry.getValue());
      }
    }
    return clinicalDataMap;
  }

  /**
   * Sets the clinical data.
   *
   * @param clinicalData the clinical data
   */
  public void setClinicalData(Map<AccumedXmlTagName, String> clinicalData) {
    this.clinicalData = clinicalData;
  }

  /**
   * Gets the drg adjustment amt.
   *
   * @return the drg adjustment amt
   */
  public BigDecimal getDrgAdjustmentAmt() {
    return drgAdjustmentAmt;
  }

  /**
   * Sets the drg adjustment amt.
   *
   * @param drgAdjustmentAmt the new drg adjustment amt
   */
  public void setDrgAdjustmentAmt(BigDecimal drgAdjustmentAmt) {
    this.drgAdjustmentAmt = drgAdjustmentAmt;
  }

  /**
   * Gets the claim.
   *
   * @return the claim
   */
  public BasicDynaBean getClaim() {
    return claim;
  }

  /**
   * Sets the claim.
   *
   * @param claim the new claim
   */
  public void setClaim(BasicDynaBean claim) {
    this.claim = claim;
  }

  /**
   * Gets the diagnosis.
   *
   * @return the diagnosis
   */
  public List getDiagnosis() {
    return diagnosis;
  }

  /**
   * Sets the diagnosis.
   *
   * @param diagnosis the new diagnosis
   */
  public void setDiagnosis(List diagnosis) {
    this.diagnosis = diagnosis;
  }

  /**
   * Gets the bills.
   *
   * @return the bills
   */
  public List getBills() {
    return bills;
  }

  /**
   * Sets the bills.
   *
   * @param bills the new bills
   */
  public void setBills(List bills) {
    this.bills = bills;
  }

  /**
   * Gets the charges.
   *
   * @return the charges
   */
  public List getCharges() {
    return charges;
  }

  /**
   * Sets the charges.
   *
   * @param charges the new charges
   */
  public void setCharges(List charges) {
    this.charges = charges;
  }

  /**
   * Gets the observations map.
   *
   * @return the observations map
   */
  public Map getObservationsMap() {
    return observationsMap;
  }

  /**
   * Sets the observations map.
   *
   * @param observationsMap the new observations map
   */
  public void setObservationsMap(Map observationsMap) {
    this.observationsMap = observationsMap;
  }

  /**
   * Gets the attachment.
   *
   * @return the attachment
   */
  public String getAttachment() {
    return attachment;
  }

  /**
   * Sets the attachment.
   *
   * @param attachment the new attachment
   */
  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }

  /**
   * Gets the checks if is resubmission.
   *
   * @return the checks if is resubmission
   */
  public Boolean getIsResubmission() {
    return isResubmission;
  }

  /**
   * Sets the checks if is resubmission.
   *
   * @param isResubmission the new checks if is resubmission
   */
  public void setIsResubmission(Boolean isResubmission) {
    this.isResubmission = isResubmission;
  }

  /**
   * Gets the card attachment.
   *
   * @return the card attachment
   */
  // For Accumed XML
  public String getCardAttachment() {
    return cardAttachment;
  }

  /**
   * Sets the card attachment.
   *
   * @param cardAttachment the new card attachment
   */
  public void setCardAttachment(String cardAttachment) {
    this.cardAttachment = cardAttachment;
  }

  /**
   * Gets the plan co pay list.
   *
   * @return the plan co pay list
   */
  public List getPlanCoPayList() {
    return planCoPayList;
  }

  /**
   * Sets the plan co pay list.
   *
   * @param planCoPayList the new plan co pay list
   */
  public void setPlanCoPayList(List planCoPayList) {
    this.planCoPayList = planCoPayList;
  }

  /**
   * Gets the supported attachments.
   *
   * @return the supported attachments
   */
  public List getSupportedAttachments() {
    return supportedAttachments;
  }

  /**
   * Sets the supported attachments.
   *
   * @param supportedAttachments the new supported attachments
   */
  public void setSupportedAttachments(List supportedAttachments) {
    this.supportedAttachments = supportedAttachments;
  }

  /**
   * Gets the presc doctor as ordering clinician.
   *
   * @return the presc doctor as ordering clinician
   */
  public String getPrescDoctorAsOrderingClinician() {
    return prescDoctorAsOrderingClinician;
  }

  /**
   * Sets the presc doctor as ordering clinician.
   *
   * @param prescDoctorAsOrderingClinician the new presc doctor as ordering clinician
   */
  public void setPrescDoctorAsOrderingClinician(String prescDoctorAsOrderingClinician) {
    this.prescDoctorAsOrderingClinician = prescDoctorAsOrderingClinician;
  }

  public List<String> getContractNames() {
    return contractNames;
  }

  public void setContractNames(List<String> contractNames) {
    this.contractNames = contractNames;
  }
}

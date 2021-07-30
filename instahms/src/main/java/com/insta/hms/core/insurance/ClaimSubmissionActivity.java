package com.insta.hms.core.insurance;

import java.math.BigDecimal;

/**
 * The Class ClaimAdviceActivity.
 */
public class ClaimSubmissionActivity {

  /** The activity ID. */
  private String activityID;

  /** The start. */
  private String start;

  /** The type. */
  private String type;

  /** The code. */
  private String code;

  /** The quantity. */
  private BigDecimal quantity;

  /** The net. */
  private BigDecimal net;

  /** The list. */
  private BigDecimal list;

  /** The clinician. */
  private String clinician;

  /** The prior authorization ID. */
  private String priorAuthorizationID;

  /** The gross. */
  private BigDecimal gross;

  /** The patient share. */
  private BigDecimal patientShare;

  /** The vat. */
  private BigDecimal vat;

  /** The vat percent. */
  private BigDecimal vatPercent;

  /** The ordering clinician. */
  private String orderingClinician;

  /**
   * Gets the activity ID.
   *
   * @return the activity ID
   */
  public String getActivityID() {
    return activityID;
  }

  /**
   * Sets the activity ID.
   *
   * @param activityID the new activity ID
   */
  public void setActivityID(String activityID) {
    this.activityID = activityID;
  }

  /**
   * Gets the clinician.
   *
   * @return the clinician
   */
  public String getClinician() {
    return clinician;
  }

  /**
   * Sets the clinician.
   *
   * @param clinician the new clinician
   */
  public void setClinician(String clinician) {
    this.clinician = clinician;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Sets the code.
   *
   * @param code the new code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Gets the gross.
   *
   * @return the gross
   */
  public BigDecimal getGross() {
    return gross;
  }

  /**
   * Sets the gross.
   *
   * @param gross the new gross
   */
  public void setGross(BigDecimal gross) {
    this.gross = gross;
  }

  /**
   * Gets the list.
   *
   * @return the list
   */
  public BigDecimal getList() {
    return list;
  }

  /**
   * Sets the list.
   *
   * @param list the new list
   */
  public void setList(BigDecimal list) {
    this.list = list;
  }

  /**
   * Gets the net.
   *
   * @return the net
   */
  public BigDecimal getNet() {
    return net;
  }

  /**
   * Sets the net.
   *
   * @param net the new net
   */
  public void setNet(BigDecimal net) {
    this.net = net;
  }

  /**
   * Gets the patient share.
   *
   * @return the patient share
   */
  public BigDecimal getPatientShare() {
    return patientShare;
  }

  /**
   * Sets the patient share.
   *
   * @param patientShare the new patient share
   */
  public void setPatientShare(BigDecimal patientShare) {
    this.patientShare = patientShare;
  }

  /**
   * Gets the prior authorization ID.
   *
   * @return the prior authorization ID
   */
  public String getPriorAuthorizationID() {
    return priorAuthorizationID;
  }

  /**
   * Sets the prior authorization ID.
   *
   * @param priorAuthorizationID the new prior authorization ID
   */
  public void setPriorAuthorizationID(String priorAuthorizationID) {
    this.priorAuthorizationID = priorAuthorizationID;
  }

  /**
   * Gets the quantity.
   *
   * @return the quantity
   */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /**
   * Sets the quantity.
   *
   * @param quantity the new quantity
   */
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  /**
   * Gets the start.
   *
   * @return the start
   */
  public String getStart() {
    return start;
  }

  /**
   * Sets the start.
   *
   * @param start the new start
   */
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the ordering clinician.
   *
   * @return the ordering clinician
   */
  public String getOrderingClinician() {
    return orderingClinician;
  }

  /**
   * Sets the ordering clinician.
   *
   * @param orderingClinician the new ordering clinician
   */
  public void setOrderingClinician(String orderingClinician) {
    this.orderingClinician = orderingClinician;
  }

  /**
   * Gets the vat.
   *
   * @return the vat
   */
  public BigDecimal getVat() {
    return vat;
  }

  /**
   * Sets the vat.
   *
   * @param vat the new vat
   */
  public void setVat(BigDecimal vat) {
    this.vat = vat;
  }

  /**
   * Gets the vat percent.
   *
   * @return the vat percent
   */
  public BigDecimal getVatPercent() {
    return vatPercent;
  }

  /**
   * Sets the vat percent.
   *
   * @param vatPercent the new vat percent
   */
  public void setVatPercent(BigDecimal vatPercent) {
    this.vatPercent = vatPercent;
  }
}

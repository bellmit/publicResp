package com.insta.hms.insurance;

import java.math.BigDecimal;

/**
 * The Class RemittanceAdviceActivity.
 *
 * @author deepasri.prasad
 */
public class RemittanceAdviceActivity {

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
  private BigDecimal list; // minocc:0

  /** The clinician. */
  private String clinician;

  /** The prior authorization ID. */
  private String priorAuthorizationID; // minocc:0

  /** The gross. */
  private BigDecimal gross; // minocc:0

  /** The patient share. */
  private BigDecimal patientShare; // minocc:0

  /** The payment amount. */
  private BigDecimal paymentAmount;

  /** The activity denial code. */
  private String activityDenialCode; // minocc:0

  /** The denial remarks. */
  private String denialRemarks; // minocc:0

  /**
   * Instantiates a new remittance advice activity.
   */
  public RemittanceAdviceActivity() {

  }

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
   * Gets the activity denial code.
   *
   * @return the activity denial code
   */
  public String getActivityDenialCode() {
    return activityDenialCode;
  }

  /**
   * Sets the activity denial code.
   *
   * @param activityDenialCode the new activity denial code
   */
  public void setActivityDenialCode(String activityDenialCode) {
    // if we receive and empty <denialCode></denialCode> tag, we save it as
    // null and not ''
    // to prevent additional checks in the processing queries
    if ("".equals(activityDenialCode)) {
      this.activityDenialCode = null;
    } else {
      this.activityDenialCode = activityDenialCode;
    }

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
   * Gets the payment amount.
   *
   * @return the payment amount
   */
  public BigDecimal getPaymentAmount() {
    return paymentAmount;
  }

  /**
   * Sets the payment amount.
   *
   * @param paymentAmount the new payment amount
   */
  public void setPaymentAmount(BigDecimal paymentAmount) {
    this.paymentAmount = paymentAmount;
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
   * Gets the denial remarks.
   *
   * @return the denial remarks
   */
  public String getDenialRemarks() {
    return denialRemarks;
  }

  /**
   * Sets the denial remarks.
   *
   * @param denialRemarks the new denial remarks
   */
  public void setDenialRemarks(String denialRemarks) {
    this.denialRemarks = denialRemarks;
  }

}

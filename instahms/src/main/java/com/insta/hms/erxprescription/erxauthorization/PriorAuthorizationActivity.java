/**
 *
 */

package com.insta.hms.erxprescription.erxauthorization;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * The Class PriorAuthorizationActivity.
 *
 * @author lakshmi
 */
public class PriorAuthorizationActivity {

  /**
   * The activity ID.
   */
  private String activityID;

  /**
   * The activity type.
   */
  private String activityType;

  /**
   * The activity code.
   */
  private String activityCode;

  /**
   * The quantity.
   */
  private BigDecimal quantity;

  /**
   * The net.
   */
  private BigDecimal net;

  /**
   * The list.
   */
  private BigDecimal list;

  /**
   * The patient share.
   */
  private BigDecimal patientShare;

  /**
   * The payment amount.
   */
  private BigDecimal paymentAmount;

  /**
   * The activity denial code.
   */
  private String activityDenialCode;

  /**
   * The denial remarks.
   */
  private String denialRemarks;

  /**
   * The observations.
   */
  public ArrayList<PriorActivityObservation> observations;

  /**
   * Instantiates a new prior authorization activity.
   */
  public PriorAuthorizationActivity() {
    observations = new ArrayList<PriorActivityObservation>();
  }

  /**
   * Gets the observations.
   *
   * @return the observations
   */
  public ArrayList<PriorActivityObservation> getObservations() {
    return observations;
  }

  /**
   * Sets the observations.
   *
   * @param observations the new observations
   */
  public void setObservations(
      ArrayList<PriorActivityObservation> observations) {
    this.observations = observations;
  }

  /**
   * Adds the observation.
   *
   * @param observation the observation
   */
  public void addObservation(PriorActivityObservation observation) {
    observations.add(observation);
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
   * Gets the activity code.
   *
   * @return the activity code
   */
  public String getActivityCode() {
    return activityCode;
  }

  /**
   * Sets the activity code.
   *
   * @param activityCode the new activity code
   */
  public void setActivityCode(String activityCode) {
    this.activityCode = activityCode;
  }

  /**
   * Gets the activity type.
   *
   * @return the activity type
   */
  public String getActivityType() {
    return activityType;
  }

  /**
   * Sets the activity type.
   *
   * @param activityType the new activity type
   */
  public void setActivityType(String activityType) {
    this.activityType = activityType;
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
    this.activityDenialCode = activityDenialCode;
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

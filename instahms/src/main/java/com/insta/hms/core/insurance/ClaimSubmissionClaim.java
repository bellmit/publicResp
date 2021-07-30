package com.insta.hms.core.insurance;

import java.math.BigDecimal;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimSubmissionClaim.
 */
public class ClaimSubmissionClaim {

  /** The claim ID. */
  private String claimID;

  /** The id payer. */
  private String idPayer;

  /** The provider ID. */
  private String providerID;

  /** The gross. */
  private BigDecimal gross;

  /** The net. */
  private BigDecimal net;

  /** The vat. */
  private BigDecimal vat;

  /** The patient share. */
  private BigDecimal patientShare;

  /** The member id. */
  private String memberId;

  /** The encounters. */
  public ArrayList<ClaimSubmissionEncounter> encounters;

  /** The activities. */
  public ArrayList<ClaimSubmissionActivity> activities;

  /** The bills. */
  public ArrayList<ClaimSubmissionBill> bills;

  /** The resubmission. */
  public ClaimSubmissionResubmission resubmission;

  private String receiverID;
  private String payerID;
  private String submissionBatchId;

  /**
   * Instantiates a new claim submission claim.
   */
  public ClaimSubmissionClaim() {
    encounters = new ArrayList<ClaimSubmissionEncounter>();
    activities = new ArrayList<ClaimSubmissionActivity>();
    bills = new ArrayList<ClaimSubmissionBill>();
  }

  /**
   * Gets the encounter.
   *
   * @return the encounter
   */
  public ArrayList getEncounter() {
    return encounters;
  }

  /**
   * Sets the encounter.
   *
   * @param encounter the new encounter
   */
  public void setEncounter(ArrayList encounter) {
    this.encounters = encounter;
  }

  /**
   * Gets the provider ID.
   *
   * @return the provider ID
   */
  public String getProviderID() {
    return providerID;
  }

  /**
   * Sets the provider ID.
   *
   * @param providerID the new provider ID
   */
  public void setProviderID(String providerID) {
    this.providerID = providerID;
  }

  /**
   * Adds the encounter.
   *
   * @param encou the encou
   */
  public void addEncounter(ClaimSubmissionEncounter encou) {
    encounters.add(encou);
  }

  /**
   * Adds the resubmission.
   *
   * @param resubmission the resubmission
   */
  public void addResubmission(ClaimSubmissionResubmission resubmission) {
    this.resubmission = resubmission;
  }

  /**
   * Gets the resubmission.
   *
   * @return the resubmission
   */
  public ClaimSubmissionResubmission getResubmission() {
    return resubmission;
  }

  /**
   * Sets the resubmission.
   *
   * @param resubmission the new resubmission
   */
  public void setResubmission(ClaimSubmissionResubmission resubmission) {
    this.resubmission = resubmission;
  }

  /**
   * Adds the activity.
   *
   * @param activity the activity
   */
  public void addActivity(ClaimSubmissionActivity activity) {
    activities.add(activity);
  }

  /**
   * Gets the activities.
   *
   * @return the activities
   */
  public ArrayList<ClaimSubmissionActivity> getActivities() {
    return activities;
  }

  /**
   * Sets the activities.
   *
   * @param activities the new activities
   */
  public void setActivities(ArrayList<ClaimSubmissionActivity> activities) {
    this.activities = activities;
  }

  /**
   * Gets the claim ID.
   *
   * @return the claim ID
   */
  public String getClaimID() {
    return claimID;
  }

  /**
   * Sets the claim ID.
   *
   * @param claimID the new claim ID
   */
  public void setClaimID(String claimID) {
    this.claimID = claimID;
  }

  /**
   * Gets the encounters.
   *
   * @return the encounters
   */
  public ArrayList getEncounters() {
    return encounters;
  }

  /**
   * Sets the encounters.
   *
   * @param encounters the new encounters
   */
  public void setEncounters(ArrayList<ClaimSubmissionEncounter> encounters) {
    this.encounters = encounters;
  }

  /**
   * Gets the id payer.
   *
   * @return the id payer
   */
  public String getIdPayer() {
    return idPayer;
  }

  /**
   * Sets the id payer.
   *
   * @param idPayer the new id payer
   */
  public void setIdPayer(String idPayer) {
    this.idPayer = idPayer;
  }

  /**
   * Adds the bill.
   *
   * @param bill the bill
   */
  public void addBill(ClaimSubmissionBill bill) {
    bills.add(bill);
  }

  /**
   * Gets the bills.
   *
   * @return the bills
   */
  public ArrayList<ClaimSubmissionBill> getBills() {
    return bills;
  }

  /**
   * Sets the bills.
   *
   * @param bills the new bills
   */
  public void setBills(ArrayList<ClaimSubmissionBill> bills) {
    this.bills = bills;
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
   * Gets the member id.
   *
   * @return the member id
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * Sets the member id.
   *
   * @param memberId the new member id
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public String getReceiverID() {
    return receiverID;
  }

  public void setReceiverID(String receiverID) {
    this.receiverID = receiverID;
  }

  public String getPayerID() {
    return payerID;
  }

  public void setPayerID(String payerID) {
    this.payerID = payerID;
  }

  public String getSubmissionBatchId() {
    return submissionBatchId;
  }

  public void setSubmissionBatchId(String submissionBatchId) {
    this.submissionBatchId = submissionBatchId;
  }
}

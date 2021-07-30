package com.insta.hms.insurance;

import java.util.ArrayList;

/**
 * The Class RemittanceAdviceClaim.
 *
 * @author deepasri.prasad
 */
public class RemittanceAdviceClaim {

  /** The claim ID. */
  private String claimID;

  /** The id payer. */
  private String idPayer;

  /** The provider ID. */
  private String providerID;

  /** The denial code. */
  private String denialCode; // minocc:0

  /** The payment reference. */
  private String paymentReference;

  /** The date settlement. */
  private String dateSettlement; // minocc:0

  /** The encounters. */
  private ArrayList<RemittanceAdviceEncounter> encounters;

  /** The activities. */
  private ArrayList<RemittanceAdviceActivity> activities;

  /** The bills. */
  private ArrayList<RemittanceAdviceBill> bills;

  /** The resubmission. */
  private RemittanceAdviceResubmission resubmission;

  /**
   * Instantiates a new remittance advice claim.
   */
  public RemittanceAdviceClaim() {
    encounters = new ArrayList<RemittanceAdviceEncounter>();
    activities = new ArrayList<RemittanceAdviceActivity>();
    bills = new ArrayList<RemittanceAdviceBill>();
  }

  /*
   * Accessors
   */

  /**
   * Gets the date settlement.
   *
   * @return the date settlement
   */
  public String getDateSettlement() {
    return dateSettlement;
  }

  /**
   * Sets the date settlement.
   *
   * @param dateSettlement the new date settlement
   */
  public void setDateSettlement(String dateSettlement) {
    this.dateSettlement = dateSettlement;
  }

  /**
   * Gets the denial code.
   *
   * @return the denial code
   */
  public String getDenialCode() {
    return denialCode;
  }

  /**
   * Sets the denial code.
   *
   * @param denialCode the new denial code
   */
  public void setDenialCode(String denialCode) {
    this.denialCode = denialCode;
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
   * Gets the payment reference.
   *
   * @return the payment reference
   */
  public String getPaymentReference() {
    return paymentReference;
  }

  /**
   * Sets the payment reference.
   *
   * @param paymentReference the new payment reference
   */
  public void setPaymentReference(String paymentReference) {
    this.paymentReference = paymentReference;
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
   * @param remittanceAdviceEncounter the e
   */
  public void addEncounter(RemittanceAdviceEncounter remittanceAdviceEncounter) {
    encounters.add(remittanceAdviceEncounter);
  }

  /**
   * Adds the resubmission.
   *
   * @param resubmission the resubmission
   */
  public void addResubmission(RemittanceAdviceResubmission resubmission) {
    this.resubmission = resubmission;
  }

  /**
   * Gets the resubmission.
   *
   * @return the resubmission
   */
  public RemittanceAdviceResubmission getResubmission() {
    return resubmission;
  }

  /**
   * Sets the resubmission.
   *
   * @param resubmission the new resubmission
   */
  public void setResubmission(RemittanceAdviceResubmission resubmission) {
    this.resubmission = resubmission;
  }

  /**
   * Adds the activity.
   *
   * @param remittanceAdviceActivity the a
   */
  public void addActivity(RemittanceAdviceActivity remittanceAdviceActivity) {
    activities.add(remittanceAdviceActivity);
  }

  /**
   * Gets the activities.
   *
   * @return the activities
   */
  public ArrayList<RemittanceAdviceActivity> getActivities() {
    return activities;
  }

  /**
   * Sets the activities.
   *
   * @param activities the new activities
   */
  public void setActivities(ArrayList<RemittanceAdviceActivity> activities) {
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
  public void setEncounters(ArrayList<RemittanceAdviceEncounter> encounters) {
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
   * @param remittanceAdviceBill the b
   */
  public void addBill(RemittanceAdviceBill remittanceAdviceBill) {
    bills.add(remittanceAdviceBill);
  }

  /**
   * Gets the bills.
   *
   * @return the bills
   */
  public ArrayList<RemittanceAdviceBill> getBills() {
    return bills;
  }

  /**
   * Sets the bills.
   *
   * @param bills the new bills
   */
  public void setBills(ArrayList<RemittanceAdviceBill> bills) {
    this.bills = bills;
  }
}

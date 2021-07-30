package com.bob.hms.adminmasters.organization;

import java.io.Serializable;

/**
 * The Class Organization.
 */
public class Organization implements Serializable {
  // this class represents a organization_details table

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The org id. */
  private String orgId;

  /** The org name. */
  private String orgName;

  /** The status. */
  private String status;

  /** The contact person. */
  private String contactPerson;

  /** The phone. */
  private String phone;

  /** The email. */
  private String email;

  /** The address. */
  private String address;

  /** The opconsvisitcode. */
  private String opconsvisitcode;

  /** The opconsrevisitcode. */
  private String opconsrevisitcode;

  /** The privateconsvisitcode. */
  private String privateconsvisitcode;

  /** The privateconsrevisitcode. */
  private String privateconsrevisitcode;

  /** The dutyconsvisitcode. */
  private String dutyconsvisitcode;

  /** The dutyconsrevisitcode. */
  private String dutyconsrevisitcode;

  /** The splconsvisitcode. */
  private String splconsvisitcode;

  /** The splconsrevisitcode. */
  private String splconsrevisitcode;

  /** The discperc. */
  private Double discperc;

  /** The disc type. */
  private String discType;

  /** The has date validity. */
  private boolean hasDateValidity;

  /** The from date. */
  private String fromDate;

  /** The to date. */
  private String toDate;

  /** The rate variation. */
  private String rateVariation;

  /** The store rate plan id. */
  private String storeRatePlanId;

  /** The eligible to earn points. */
  private String eligibleToEarnPoints;

  /**
   * Gets the store rate plan id.
   *
   * @return the store rate plan id
   */
  public String getStore_rate_plan_id() {
    return storeRatePlanId;
  }

  /**
   * Sets the store rate plan id.
   *
   * @param storeRatePlanId the new store rate plan id
   */
  public void setStore_rate_plan_id(String storeRatePlanId) {
    this.storeRatePlanId = storeRatePlanId;
  }

  /**
   * Gets the from date.
   *
   * @return the from date
   */
  public String getFromDate() {
    return fromDate;
  }

  /**
   * Sets the from date.
   *
   * @param fromDate the new from date
   */
  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  /**
   * Gets the to date.
   *
   * @return the to date
   */
  public String getToDate() {
    return toDate;
  }

  /**
   * Sets the to date.
   *
   * @param toDate the new to date
   */
  public void setToDate(String toDate) {
    this.toDate = toDate;
  }

  /**
   * Gets the checks for date validity.
   *
   * @return the checks for date validity
   */
  public boolean getHasDateValidity() {
    return hasDateValidity;
  }

  /**
   * Sets the checks for date validity.
   *
   * @param hasDateValidity the new checks for date validity
   */
  public void setHasDateValidity(boolean hasDateValidity) {
    this.hasDateValidity = hasDateValidity;
  }

  /**
   * Gets the discperc.
   *
   * @return the discperc
   */
  public Double getDiscperc() {
    return discperc;
  }

  /**
   * Sets the discperc.
   *
   * @param discperc the new discperc
   */
  public void setDiscperc(Double discperc) {
    this.discperc = discperc;
  }

  /**
   * Gets the disc type.
   *
   * @return the disc type
   */
  public String getDiscType() {
    return this.discType;
  }

  /**
   * Sets the disc type.
   *
   * @param discType the new disc type
   */
  public void setDiscType(String discType) {
    this.discType = discType;
  }

  /**
   * Gets the address.
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets the address.
   *
   * @param address the new address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Gets the contact person.
   *
   * @return the contact person
   */
  public String getContactPerson() {
    return contactPerson;
  }

  /**
   * Sets the contact person.
   *
   * @param contactPerson the new contact person
   */
  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  /**
   * Gets the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email.
   *
   * @param email the new email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the new org id
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the org name.
   *
   * @return the org name
   */
  public String getOrgName() {
    return orgName;
  }

  /**
   * Sets the org name.
   *
   * @param orgName the new org name
   */
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  /**
   * Gets the phone.
   *
   * @return the phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone.
   *
   * @param phone the new phone
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets the dutyconsrevisitcode.
   *
   * @return the dutyconsrevisitcode
   */
  public String getDutyconsrevisitcode() {
    return dutyconsrevisitcode;
  }

  /**
   * Sets the dutyconsrevisitcode.
   *
   * @param dutyconsrevisitcode the new dutyconsrevisitcode
   */
  public void setDutyconsrevisitcode(String dutyconsrevisitcode) {
    this.dutyconsrevisitcode = dutyconsrevisitcode;
  }

  /**
   * Gets the dutyconsvisitcode.
   *
   * @return the dutyconsvisitcode
   */
  public String getDutyconsvisitcode() {
    return dutyconsvisitcode;
  }

  /**
   * Sets the dutyconsvisitcode.
   *
   * @param dutyconsvisitcode the new dutyconsvisitcode
   */
  public void setDutyconsvisitcode(String dutyconsvisitcode) {
    this.dutyconsvisitcode = dutyconsvisitcode;
  }

  /**
   * Gets the opconsrevisitcode.
   *
   * @return the opconsrevisitcode
   */
  public String getOpconsrevisitcode() {
    return opconsrevisitcode;
  }

  /**
   * Sets the opconsrevisitcode.
   *
   * @param opconsrevisitcode the new opconsrevisitcode
   */
  public void setOpconsrevisitcode(String opconsrevisitcode) {
    this.opconsrevisitcode = opconsrevisitcode;
  }

  /**
   * Gets the opconsvisitcode.
   *
   * @return the opconsvisitcode
   */
  public String getOpconsvisitcode() {
    return opconsvisitcode;
  }

  /**
   * Sets the opconsvisitcode.
   *
   * @param opconsvisitcode the new opconsvisitcode
   */
  public void setOpconsvisitcode(String opconsvisitcode) {
    this.opconsvisitcode = opconsvisitcode;
  }

  /**
   * Gets the privateconsrevisitcode.
   *
   * @return the privateconsrevisitcode
   */
  public String getPrivateconsrevisitcode() {
    return privateconsrevisitcode;
  }

  /**
   * Sets the privateconsrevisitcode.
   *
   * @param privateconsrevisitcode the new privateconsrevisitcode
   */
  public void setPrivateconsrevisitcode(String privateconsrevisitcode) {
    this.privateconsrevisitcode = privateconsrevisitcode;
  }

  /**
   * Gets the privateconsvisitcode.
   *
   * @return the privateconsvisitcode
   */
  public String getPrivateconsvisitcode() {
    return privateconsvisitcode;
  }

  /**
   * Sets the privateconsvisitcode.
   *
   * @param privateconsvisitcode the new privateconsvisitcode
   */
  public void setPrivateconsvisitcode(String privateconsvisitcode) {
    this.privateconsvisitcode = privateconsvisitcode;
  }

  /**
   * Gets the splconsrevisitcode.
   *
   * @return the splconsrevisitcode
   */
  public String getSplconsrevisitcode() {
    return splconsrevisitcode;
  }

  /**
   * Sets the splconsrevisitcode.
   *
   * @param splconsrevisitcode the new splconsrevisitcode
   */
  public void setSplconsrevisitcode(String splconsrevisitcode) {
    this.splconsrevisitcode = splconsrevisitcode;
  }

  /**
   * Gets the splconsvisitcode.
   *
   * @return the splconsvisitcode
   */
  public String getSplconsvisitcode() {
    return splconsvisitcode;
  }

  /**
   * Sets the splconsvisitcode.
   *
   * @param splconsvisitcode the new splconsvisitcode
   */
  public void setSplconsvisitcode(String splconsvisitcode) {
    this.splconsvisitcode = splconsvisitcode;
  }

  /**
   * Gets the rate variation.
   *
   * @return the rate variation
   */
  public String getRateVariation() {
    return this.rateVariation;
  }

  /**
   * Sets the rate variation.
   *
   * @param rateVariation the new rate variation
   */
  public void setRateVariation(String rateVariation) {
    this.rateVariation = rateVariation;
  }

  /**
   * Gets the eligible to earn points.
   *
   * @return the eligible to earn points
   */
  public String getEligible_to_earn_points() {
    return eligibleToEarnPoints;
  }

  /**
   * Sets the eligible to earn points.
   *
   * @param eligibleToEarnPoints the new eligible to earn points
   */
  public void setEligible_to_earn_points(String eligibleToEarnPoints) {
    this.eligibleToEarnPoints = eligibleToEarnPoints;
  }
}

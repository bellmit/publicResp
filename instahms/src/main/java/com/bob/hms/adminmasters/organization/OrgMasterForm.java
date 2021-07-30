package com.bob.hms.adminmasters.organization;

import org.apache.struts.action.ActionForm;

public class OrgMasterForm extends ActionForm {

  private static final long serialVersionUID = 1L;

  private String orgName;
  private String status;
  private String contactPerson;
  private String phone;
  private String email;
  private String address;
  private String baseOrgName;
  private String baseOrgId;
  private String variaceType;
  private Double varianceBy;
  private Double varianceValue;
  private String editOrgId;
  private Double nearsetRoundofValue;
  private String storeRatePlanId;

  private String opconsvisitcode;
  private String opconsrevisitcode;

  private String privateconsvisitcode;
  private String privateconsrevisitcode;

  private String dutyconsvisitcode;
  private String dutyconsrevisitcode;

  private String splconsvisitcode;
  private String splconsrevisitcode;
  private Double discperc;
  private String discType;
  private boolean hasDateValidity;
  private String fromDate;
  private String toDate;

  private String rateVariation;
  private String eligibleToEarnPoints;

  public Double getDiscperc() {
    return discperc;
  }

  public void setDiscperc(Double discperc) {
    this.discperc = discperc;
  }

  public String getDiscType() {
    return this.discType;
  }

  public void setDiscType(String discType) {
    this.discType = discType;
  }

  public Double getNearsetRoundofValue() {
    return nearsetRoundofValue;
  }

  public void setNearsetRoundofValue(Double nearsetRoundofValue) {
    this.nearsetRoundofValue = nearsetRoundofValue;
  }

  public String getEditOrgId() {
    return editOrgId;
  }

  public void setEditOrgId(String editOrgId) {
    this.editOrgId = editOrgId;
  }

  public Double getVarianceBy() {
    return varianceBy;
  }

  public void setVarianceBy(Double varianceBy) {
    this.varianceBy = varianceBy;
  }

  public Double getVarianceValue() {
    return varianceValue;
  }

  public void setVarianceValue(Double varianceValue) {
    this.varianceValue = varianceValue;
  }

  public String getVariaceType() {
    return variaceType;
  }

  public void setVariaceType(String variaceType) {
    this.variaceType = variaceType;
  }

  public String getBaseOrgName() {
    return baseOrgName;
  }

  public void setBaseOrgName(String baseOrgName) {
    this.baseOrgName = baseOrgName;
  }

  public String getBaseOrgId() {
    return baseOrgId;
  }

  public void setBaseOrgId(String baseOrgId) {
    this.baseOrgId = baseOrgId;
  }

  public static long getSerialVersionUid() {
    return serialVersionUID;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOpconsrevisitcode() {
    return opconsrevisitcode;
  }

  public void setOpconsrevisitcode(String opconsrevisitcode) {
    this.opconsrevisitcode = opconsrevisitcode;
  }

  public String getOpconsvisitcode() {
    return opconsvisitcode;
  }

  public void setOpconsvisitcode(String opconsvisitcode) {
    this.opconsvisitcode = opconsvisitcode;
  }

  public String getPrivateconsrevisitcode() {
    return privateconsrevisitcode;
  }

  public void setPrivateconsrevisitcode(String privateconsrevisitcode) {
    this.privateconsrevisitcode = privateconsrevisitcode;
  }

  public String getPrivateconsvisitcode() {
    return privateconsvisitcode;
  }

  public void setPrivateconsvisitcode(String privateconsvisitcode) {
    this.privateconsvisitcode = privateconsvisitcode;
  }

  public String getDutyconsrevisitcode() {
    return dutyconsrevisitcode;
  }

  public void setDutyconsrevisitcode(String dutyconsrevisitcode) {
    this.dutyconsrevisitcode = dutyconsrevisitcode;
  }

  public String getDutyconsvisitcode() {
    return dutyconsvisitcode;
  }

  public void setDutyconsvisitcode(String dutyconsvisitcode) {
    this.dutyconsvisitcode = dutyconsvisitcode;
  }

  public String getSplconsrevisitcode() {
    return splconsrevisitcode;
  }

  public void setSplconsrevisitcode(String splconsrevisitcode) {
    this.splconsrevisitcode = splconsrevisitcode;
  }

  public String getSplconsvisitcode() {
    return splconsvisitcode;
  }

  public void setSplconsvisitcode(String splconsvisitcode) {
    this.splconsvisitcode = splconsvisitcode;
  }

  public boolean getHasDateValidity() {
    return hasDateValidity;
  }

  public void setHasDateValidity(boolean hasDateValidity) {
    this.hasDateValidity = hasDateValidity;
  }

  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  public String getToDate() {
    return toDate;
  }

  public void setToDate(String toDate) {
    this.toDate = toDate;
  }

  public String getRateVariation() {
    return this.rateVariation;
  }

  public void setRateVariation(String rateVariation) {
    this.rateVariation = rateVariation;
  }

  public String getStore_rate_plan_id() {
    return storeRatePlanId;
  }

  public void setStore_rate_plan_id(String storeRatePlanId) {
    this.storeRatePlanId = storeRatePlanId;
  }

  public String getEligible_to_earn_points() {
    return eligibleToEarnPoints;
  }

  public void setEligible_to_earn_points(String eligibleToEarnPoints) {
    this.eligibleToEarnPoints = eligibleToEarnPoints;
  }
}

package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * @author manika.singh
 * @since 16/04/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageChargesDTO {

  private int packageId;
  private String orgId;
  private String orgName;
  private String ratePlanCode;
  private List<PackageChargesVO> packageCharges;
  private List<PackageContentChargesVO> packageContentCharges;
  private List<DerivedRatePlansDTO> derivedRatePlans;
  private String codeType;
  private Boolean isDerivedRatePlan;
  private List<String> ratePlanApplicability;

  public List<PackageChargesVO> getPackageCharges() {
    return packageCharges;
  }

  public void setPackageCharges(List<PackageChargesVO> packageCharges) {
    this.packageCharges = packageCharges;
  }

  public int getPackageId() {
    return packageId;
  }

  public void setPackageId(int packageId) {
    this.packageId = packageId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public List<DerivedRatePlansDTO> getDerivedRatePlans() {
    return derivedRatePlans;
  }

  public void setDerivedRatePlans(List<DerivedRatePlansDTO> derivedRatePlans) {
    this.derivedRatePlans = derivedRatePlans;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getRatePlanCode() {
    return ratePlanCode;
  }

  public void setRatePlanCode(String ratePlanCode) {
    this.ratePlanCode = ratePlanCode;
  }

  public List<PackageContentChargesVO> getPackageContentCharges() {
    return packageContentCharges;
  }

  public void setPackageContentCharges(List<PackageContentChargesVO> packageContentCharges) {
    this.packageContentCharges = packageContentCharges;
  }

  public String getCodeType() {
    return codeType;
  }

  public void setCodeType(String codeType) {
    this.codeType = codeType;
  }

  public Boolean getIsDerivedRatePlan() {
    return isDerivedRatePlan;
  }

  public void setIsDerivedRatePlan(Boolean isDerivedRatePlan) {
    this.isDerivedRatePlan = isDerivedRatePlan;
  }

  public List<String> getRatePlanApplicability() {
    return ratePlanApplicability;
  }

  public void setRatePlanApplicability(List<String> ratePlanApplicability) {
    this.ratePlanApplicability = ratePlanApplicability;
  }
}

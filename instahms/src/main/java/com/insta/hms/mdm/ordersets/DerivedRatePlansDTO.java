package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author manika.singh
 * @since 09/05/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DerivedRatePlansDTO {

  private String orgId;
  private String orgName;
  private String discormarkup;
  private Integer rateVariationPercent;
  private Integer roundOffAmount;
  private Boolean applicable;
  private Integer packageId;
  private String baseRateSheetId;
  private String isOverride;

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getDiscormarkup() {
    return discormarkup;
  }

  public void setDiscormarkup(String discormarkup) {
    this.discormarkup = discormarkup;
  }

  public Integer getRateVariationPercent() {
    return rateVariationPercent;
  }

  public void setRateVariationPercent(Integer rateVariationPercent) {
    this.rateVariationPercent = rateVariationPercent;
  }

  public Integer getRoundOffAmount() {
    return roundOffAmount;
  }

  public void setRoundOffAmount(Integer roundOffAmount) {
    this.roundOffAmount = roundOffAmount;
  }

  public Boolean getApplicable() {
    return applicable;
  }

  public void setApplicable(Boolean applicable) {
    this.applicable = applicable;
  }

  public Integer getPackageId() {
    return packageId;
  }

  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }

  public String getBaseRateSheetId() {
    return baseRateSheetId;
  }

  public void setBaseRateSheetId(String baseRateSheetId) {
    this.baseRateSheetId = baseRateSheetId;
  }

  public String getIsOverride() {
    return isOverride;
  }

  public void setIsOverride(String isOverride) {
    this.isOverride = isOverride;
  }
}

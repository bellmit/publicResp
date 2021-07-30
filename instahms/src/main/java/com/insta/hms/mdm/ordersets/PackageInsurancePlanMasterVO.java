package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * The Class PackageInsurancePlanMasterVO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageInsurancePlanMasterVO {

  /** The plan id. */
  private Integer planId;

  /** The plan name. */
  private String planName;

  /** The plan type. */
  private String insuranceCategoryName;

  /** The insurance company. */
  private String insuranceCompanyName;

  /**
   * Gets the plan id.
   *
   * @return the plan id
   */
  public Integer getPlanId() {
    return planId;
  }

  /**
   * Sets the plan id.
   *
   * @param planId the new plan id
   */
  public void setPlanId(Integer planId) {
    this.planId = planId;
  }

  /**
   * Gets the plan name.
   *
   * @return the plan name
   */
  public String getPlanName() {
    return planName;
  }

  /**
   * Sets the plan name.
   *
   * @param planName the new plan name
   */
  public void setPlanName(String planName) {
    this.planName = planName;
  }

  /**
   * Gets the insurance category name.
   *
   * @return the insurance category name
   */
  public String getInsuranceCategoryName() {
    return insuranceCategoryName;
  }

  /**
   * Sets the insurance category name.
   *
   * @param insuranceCategoryName the new insurance category name
   */
  public void setInsuranceCategoryName(String insuranceCategoryName) {
    this.insuranceCategoryName = insuranceCategoryName;
  }

  /**
   * Gets the insurance company name.
   *
   * @return the insurance company name
   */
  public String getInsuranceCompanyName() {
    return insuranceCompanyName;
  }

  /**
   * Sets the insurance company name.
   *
   * @param insuranceCompanyName the new insurance company name
   */
  public void setInsuranceCompanyName(String insuranceCompanyName) {
    this.insuranceCompanyName = insuranceCompanyName;
  }

}

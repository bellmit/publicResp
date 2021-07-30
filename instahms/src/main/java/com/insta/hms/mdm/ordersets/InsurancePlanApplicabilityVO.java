package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.insta.hms.mdm.ordersets.SponsorPackageApplicabilityVO.SponsorApplicabilityType;

import java.util.List;

/**
 * The Class InsurancePlanApplicabilityVO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InsurancePlanApplicabilityVO {

  public enum PlanApplicabilityType {

    NO_PLANS("N"), ALL_PLANS("A"), SOME_PLANS("S");

    /** The applicability type. */
    private String applicabilityType;

    private PlanApplicabilityType(String applicabilityType) {
      this.applicabilityType = applicabilityType;
    }

    /**
     * Gets the applicability type.
     *
     * @return the applicability type
     */
    public String getApplicabilityType() {
      return applicabilityType;
    }

    /**
     * Gets the applicability type from status.
     *
     * @param applicabilityStatus the applicability status
     * @return the applicability type from status
     */
    public static PlanApplicabilityType getApplicabilityTypeFromStatus(
        String applicabilityStatus) {
      for (PlanApplicabilityType planApplicabilityType : PlanApplicabilityType.values()) {
        if (planApplicabilityType.getApplicabilityType().equals(applicabilityStatus)) {
          return planApplicabilityType;
        }
      }
      return null;
    }
  }

  /** The sponsor applicability type. */
  private String planApplicabilityType;

  /** The sponsor list. */
  private List<PackageInsurancePlanMasterVO> planList;

  /**
   * Gets the plan applicability type.
   *
   * @return the plan applicability type
   */
  public String getPlanApplicabilityType() {
    return planApplicabilityType;
  }

  /**
   * Sets the plan applicability type.
   *
   * @param planApplicabilityType the new plan applicability type
   */
  public void setPlanApplicabilityType(String planApplicabilityType) {
    this.planApplicabilityType = planApplicabilityType;
  }

  /**
   * Gets the plan list.
   *
   * @return the plan list
   */
  public List<PackageInsurancePlanMasterVO> getPlanList() {
    return planList;
  }

  /**
   * Sets the plan list.
   *
   * @param planList the new plan list
   */
  public void setPlanList(List<PackageInsurancePlanMasterVO> planList) {
    this.planList = planList;
  }

}

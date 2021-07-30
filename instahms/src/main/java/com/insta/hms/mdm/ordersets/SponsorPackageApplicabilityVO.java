package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * The Class SponsorPackageApplicabilityVO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SponsorPackageApplicabilityVO {

  /**
   * The Enum SponsorApplicabilityType.
   */
  public enum SponsorApplicabilityType {

    /** The no sponsors. */
    NO_SPONSORS("N"),
    /** The all sponsors. */
    // No Sponsors
    ALL_SPONSORS("A"),
    /** The some sponsors. */
    // All Sponsors
    SOME_SPONSORS("S"); // Some Sponsors

    /** The applicability type. */
    private String applicabilityType;

    /**
     * Instantiates a new sponsor applicability type.
     *
     * @param applicabilityType the applicability type
     */
    private SponsorApplicabilityType(String applicabilityType) {
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
    public static SponsorApplicabilityType getApplicabilityTypeFromStatus(
        String applicabilityStatus) {
      for (SponsorApplicabilityType sponsorApplicabilityType : SponsorApplicabilityType
          .values()) {
        if (sponsorApplicabilityType.getApplicabilityType().equals(applicabilityStatus)) {
          return sponsorApplicabilityType;
        }
      }
      return null;
    }
  }

  /** The sponsor applicability type. */
  private String sponsorApplicabilityType;

  /** The sponsor list. */
  private List<PackageSponsorMasterVO> sponsorList;

  /**
   * Gets the sponsor applicability type.
   *
   * @return the sponsor applicability type
   */
  public String getSponsorApplicabilityType() {
    return sponsorApplicabilityType;
  }

  /**
   * Sets the sponsor applicability type.
   *
   * @param sponsorApplicabilityType the new sponsor applicability type
   */
  public void setSponsorApplicabilityType(String sponsorApplicabilityType) {
    this.sponsorApplicabilityType = sponsorApplicabilityType;
  }

  /**
   * Gets the sponsor list.
   *
   * @return the sponsor list
   */
  public List<PackageSponsorMasterVO> getSponsorList() {
    return sponsorList;
  }

  /**
   * Sets the sponsor list.
   *
   * @param sponsorList the new sponsor list
   */
  public void setSponsorList(List<PackageSponsorMasterVO> sponsorList) {
    this.sponsorList = sponsorList;
  }

}

package com.insta.hms.core.insurance;

/**
 * The Class ClaimAdviceEncounter.
 */
public class ClaimSubmissionEncounter {

  /** The facility ID. */
  private String facilityID;

  /**
   * Instantiates a new claim advice encounter.
   */
  public ClaimSubmissionEncounter() {

  }

  /**
   * Instantiates a new claim advice encounter.
   *
   * @param facilityID the facility ID
   */
  public ClaimSubmissionEncounter(String facilityID) {
    this.facilityID = facilityID;
  }

  /**
   * Gets the facility ID.
   *
   * @return the facility ID
   */
  public String getFacilityID() {
    return facilityID;
  }

  /**
   * Sets the facility ID.
   *
   * @param facilityID the new facility ID
   */
  public void setFacilityID(String facilityID) {
    this.facilityID = facilityID;
  }

}

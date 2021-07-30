package com.insta.hms.insurance;

/**
 * The Class RemittanceAdviceEncounter.
 *
 * @author deepasri.prasad
 */
public class RemittanceAdviceEncounter {

  /** The facility ID. */
  private String facilityID;// minocc:0

  /**
   * Instantiates a new remittance advice encounter.
   */
  public RemittanceAdviceEncounter() {

  }

  /**
   * Instantiates a new remittance advice encounter.
   *
   * @param facilityID the facility ID
   */
  public RemittanceAdviceEncounter(String facilityID) {
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

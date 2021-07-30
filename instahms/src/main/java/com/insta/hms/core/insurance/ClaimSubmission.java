package com.insta.hms.core.insurance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimSubmission.
 */
public class ClaimSubmission {

  /** The header. */
  private ArrayList<ClaimSubmissionHeader> header;

  /** The claim. */
  private ArrayList<ClaimSubmissionClaim> claim;

  /**
   * Gets the claim.
   *
   * @return the claim
   */
  public ArrayList getClaim() {
    return claim;
  }

  /**
   * Sets the claim.
   *
   * @param claim the new claim
   */
  public void setClaim(ArrayList claim) {
    this.claim = claim;
  }

  /**
   * Gets the header.
   *
   * @return the header
   */
  public ArrayList getHeader() {
    return header;
  }

  /**
   * Sets the header.
   *
   * @param header the new header
   */
  public void setHeader(ArrayList header) {
    this.header = header;
  }

  /**
   * Instantiates a new claim submission.
   */
  public ClaimSubmission() {
    header = new ArrayList();
    claim = new ArrayList();
  }

  /**
   * Adds the header.
   *
   * @param rhs the rhs
   */
  public void addHeader(ClaimSubmissionHeader rhs) {
    header.add(rhs);
  }

  /**
   * Adds the claim.
   *
   * @param rhs the rhs
   */
  public void addClaim(ClaimSubmissionClaim rhs) {
    claim.add(rhs);
  }

  /**
   * Gets the claim submission map.
   *
   * @return the claim submission map
   */
  public Map getClaimSubmissionMap() {
    HashMap map = new HashMap();
    map.put("Header", header);
    map.put("Claim", claim);
    return map;
  }
}

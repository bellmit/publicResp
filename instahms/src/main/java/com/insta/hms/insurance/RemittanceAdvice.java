package com.insta.hms.insurance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class RemittanceAdvice.
 *
 * @author deepasri.prasad
 */
public class RemittanceAdvice {

  /** The header. */
  private ArrayList<RemittanceAdviceHeader> header;

  /** The claim. */
  private ArrayList<RemittanceAdviceClaim> claim;

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
   * Instantiates a new remittance advice.
   */
  public RemittanceAdvice() {
    header = new ArrayList();
    claim = new ArrayList();
  }

  /**
   * Adds the header.
   *
   * @param rhs the rhs
   */
  public void addHeader(RemittanceAdviceHeader rhs) {
    header.add(rhs);
  }

  /**
   * Adds the claim.
   *
   * @param rhs the rhs
   */
  public void addClaim(RemittanceAdviceClaim rhs) {
    claim.add(rhs);
  }

  /**
   * Gets the remittance advice map.
   *
   * @return the remittance advice map
   */
  public Map getRemittanceAdviceMap() {
    HashMap map = new HashMap();
    map.put("Header", header);
    map.put("Claim", claim);
    return map;
  }
}

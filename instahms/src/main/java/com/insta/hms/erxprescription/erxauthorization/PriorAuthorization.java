/**
 *
 */

package com.insta.hms.erxprescription.erxauthorization;

import com.insta.hms.erxprescription.ERxRequest;
import com.insta.hms.eservice.EResult;

/**
 * The Class PriorAuthorization.
 *
 * @author lakshmi
 */
public class PriorAuthorization extends ERxRequest implements EResult {

  /**
   * The header.
   */
  private PriorAuthorizationHeader header;

  /**
   * The authorization.
   */
  private PriorAuthAuthorization authorization;

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.erxprescription.ERxRequest#getHeader()
   */
  public PriorAuthorizationHeader getHeader() {
    return header;
  }

  /**
   * Sets the header.
   *
   * @param header the new header
   */
  public void setHeader(PriorAuthorizationHeader header) {
    this.header = header;
  }

  /**
   * Gets the authorization.
   *
   * @return the authorization
   */
  public PriorAuthAuthorization getAuthorization() {
    return authorization;
  }

  /**
   * Sets the authorization.
   *
   * @param authorization the new authorization
   */
  public void setAuthorization(PriorAuthAuthorization authorization) {
    this.authorization = authorization;
  }
}

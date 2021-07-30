/**
 *
 */

package com.insta.hms.eauthorization;

import com.insta.hms.eservice.EResponse;

/**
 * The Class EAuthResponse.
 *
 * @author lakshmi
 */
public class EAuthResponse extends EResponse {

  /**
   * Instantiates a new e auth response.
   *
   * @param responseCode the response code
   * @param errorMessage the error message
   * @param content      the content
   */
  public EAuthResponse(int responseCode, String errorMessage,
                       Object content) {
    super(responseCode, errorMessage, content);
  }
}

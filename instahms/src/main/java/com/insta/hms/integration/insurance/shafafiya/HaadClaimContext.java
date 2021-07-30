package com.insta.hms.integration.insurance.shafafiya;

import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ServiceCredential;

/**
 * The Class HaadClaimContext.
 */
public class HaadClaimContext extends ClaimContext {

  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Gets the service credentials.
   *
   * @return the service credentials
   */
  public ServiceCredential getServiceCredentials() {
    Integer centerId = (Integer) get("center_id");
    String eclaimUserId = (String) get("eclaim_user_id");
    String eclaimPassword = (String) get("eclaim_password");
    String serviceRegisrationNumber = (String) get("service_registration_number");
    // get the user name / password for that center id and health authority
    return new ServiceCredential(eclaimUserId, eclaimPassword, serviceRegisrationNumber);
    // return new ServiceCredential("","");
  }

}

package com.insta.hms.integration.insurance.dha;

import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ServiceCredential;

/**
 * The Class DhaClaimContext.
 */
public class DhaClaimContext extends ClaimContext {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Gets the service credentials.
   *
   * @return the service credentials
   */
  public ServiceCredential getServiceCredentials() {
    String serviceRegistrationNumber = (String) get("service_registration_number");
    String eclaimUserId = (String) get("eclaim_user_id");
    String eclaimPassword = (String) get("eclaim_password");
    // get the user name / password for that center id and health authority
    return new ServiceCredential(eclaimUserId, eclaimPassword, serviceRegistrationNumber);
  }

}

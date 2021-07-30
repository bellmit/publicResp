package com.insta.hms.fpmodule;

import com.insta.hms.mdm.ResponseRouter;

public class PurposeFpVerificationRouter extends ResponseRouter {

  public static final String PAGE_PATH = "master";

  private PurposeFpVerificationRouter(String pathElement) {
    super(PAGE_PATH, pathElement);
  }

  /**
   * Fp verification purpose.
   */
  public static final PurposeFpVerificationRouter PURPOSEFP_VERIFICATION = 
      new PurposeFpVerificationRouter("PurposeFPVerification");

}

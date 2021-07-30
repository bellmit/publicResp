package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.mdm.ResponseRouter;

/**
 * The Class CoderResponseRouter.
 *
 * @author allabakash
 * 
 *         This classes is used for defining a
 *         path for jsp resources and used it in controller for
 *         sending response.
 */

public class CoderResponseRouter extends ResponseRouter {

  /** The Constant PAGE_PATH. */
  public static final String PAGE_PATH = "medicalrecorddepartment";

  /**
   * Instantiates a new coder response router.
   *
   * @param pathElement
   *          the path element
   */
  CoderResponseRouter(String pathElement) {
    super(PAGE_PATH, pathElement);
  }

  /** Coder claim review. */
  public static final CoderResponseRouter CODER_CLAIM_REVIEW =
      new CoderResponseRouter("CoderClaimReview");

}

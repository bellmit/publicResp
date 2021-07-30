package com.insta.hms.core.clinical.multiuser;

/**
 * The Class URLRoute.
 *
 * @author sonam
 */
public class URLRoute {

  /**
   * Instantiates a new URL route.
   */
  private URLRoute() {
    throw new IllegalStateException("Utility class");
  }

  /** The Constant SEAVE_CARE_TEAM_URL. */
  static final String SAVE_CARE_TEAM_URL = "/{id}/saveCareTeam";
  
  /** The Constant FORM_SAVE. */
  static final String FORM_SAVE = "/{id}/save";
  
  /** The Constant AUTO_SAVE_SECTION_URL. */
  static final String AUTO_SAVE_SECTION_URL = "/{id}/section/{sectionId}";

}

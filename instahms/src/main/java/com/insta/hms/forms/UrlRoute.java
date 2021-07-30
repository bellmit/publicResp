package com.insta.hms.forms;

/**
 * Instantiates a new URL route.
 */
public class UrlRoute {

  /**
   * Instantiates a new URL route.
   */
  private UrlRoute() {
    throw new IllegalStateException("Utility class");
  }

  /** The Constant SHOW_URL. */
  static final String SHOW_URL = "/{id}/show";

  /** The Constant GET_SECTION_URL. */
  static final String GET_SECTION_URL = "/{id}/section/{sectionId}";

  /** The Constant AUTO_SAVE_SECTION_URL. */
  static final String AUTO_SAVE_SECTION_URL = "/{id}/section/{sectionId}";

  /** The Constant FORM_SAVE. */
  static final String FORM_SAVE = "/{id}/save";

  /** The Constant FINALIZE_IP_EMR_FORM. */
  static final String FINALIZE_FORM = "/{id}/finalize";

  /** The Constant FORM_REOPEN. */
  static final String FORM_REOPEN = "/{id}/reopen";

  /** The Constant DEPENDENT_META_DATA_URL. */
  static final String DEPENDENT_META_DATA_URL = "/{id}/metadata";

  /** The Constant INDEPENDENT_META_DATA_URL. */
  static final String INDEPENDENT_META_DATA_URL = "/metadata";

  /** The Constant SAVE_CUSTOM_FORM. */
  static final String SAVE_CUSTOM_FORM = "/customform";

  /** The Constant CHANGE_FORM. */
  static final String CHANGE_FORM = "/{id}/form/{formId}/changeform";

  /** The Constant VIEW_INDEX_URL. */
  static final String VIEW_INDEX_URL = "/index";

}

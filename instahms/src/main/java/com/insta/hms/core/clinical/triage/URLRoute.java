package com.insta.hms.core.clinical.triage;

/**
 * The Class URLRoute.
 */
public class URLRoute {

  /** The Constant TRIAGE_URL. */
  static final String TRIAGE_URL = "/triage";

  /** The Constant TRIAGE_LIST. */
  static final String TRIAGE_LIST = "/patient/{mrNo}/list";

  /** The Constant TRIAGE_SHOW_URL. */
  static final String TRIAGE_SHOW_URL = "/{consultationId}/show";

  /** The Constant TRIAGE_GET_SECTION_URL. */
  static final String TRIAGE_GET_SECTION_URL = "/{consultationId}/section/{id}";

  /** The Constant TRIAGE_AUTO_SAVE_SECTION_URL. */
  static final String TRIAGE_AUTO_SAVE_SECTION_URL = "/{consultationId}/section/{id}";

  /** The Constant TRIAGE_FORM_SAVE. */
  static final String TRIAGE_FORM_SAVE = "/{consultationId}/save";

  /** The Constant TRIAGE_DEPENDENT_META_DATA_URL. */
  static final String TRIAGE_DEPENDENT_META_DATA_URL = "/{consultationId}/metadata";

  /** The Constant TRIAGE_INDEPENDENT_META_DATA_URL. */
  static final String TRIAGE_INDEPENDENT_META_DATA_URL = "/commonTriageDetails";

  /** The Constant VIEW_INDEX_URL. */
  static final String VIEW_INDEX_URL = "/index";

}

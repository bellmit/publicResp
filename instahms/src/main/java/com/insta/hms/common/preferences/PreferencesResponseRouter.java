package com.insta.hms.common.preferences;

import com.insta.hms.mdm.ResponseRouter;

/**
 * @author aditya define paths for preferences jsp pages.
 *
 */
public class PreferencesResponseRouter extends ResponseRouter {

  public static final String PAGE_PATH = "preferences";

  protected PreferencesResponseRouter(String pathElement) {
    super(PAGE_PATH, pathElement);
  }

  public static final PreferencesResponseRouter SYSTEM_PREFERENCES_ROUTER = 
      new PreferencesResponseRouter("systempreferences");

  public static final PreferencesResponseRouter GENERIC_PREFERENCES_ROUTER = 
      new PreferencesResponseRouter("genericpreferences");
}

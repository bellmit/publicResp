package com.insta.hms.core.patient.registration;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.GenericPreferences.GenericPreferencesCache;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class RegistrationPreferencesService.
 */
@Service
public class RegistrationPreferencesService {

  /** The reg pref repo. */
  @LazyAutowired
  private RegistrationPreferencesRepository regPrefRepo;

  /**
   * Gets the registration preferences.
   *
   * @return the registration preferences
   */
  public BasicDynaBean getRegistrationPreferences() {
    String schema = RequestContext.getSchema();
    BasicDynaBean prefs = null;
    if (schema != null) {
      prefs = GenericPreferencesCache.REGCACHEDPREFERENCESBEAN.get(schema);
      if (prefs != null) {
        return prefs;
      }
    }

    prefs = getRegistrationPreferencesFromDB();
    if (schema != null) {
      GenericPreferencesCache.REGCACHEDPREFERENCESBEAN.put(schema, prefs);
    }
    return prefs;
  }

  /**
   * Gets the registration preferences from DB.
   *
   * @return the registration preferences from DB
   */
  public BasicDynaBean getRegistrationPreferencesFromDB() {
    return regPrefRepo.getRegistrationPreferencesFromDB();
  }
}

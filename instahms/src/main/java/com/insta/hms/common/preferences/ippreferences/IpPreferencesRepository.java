package com.insta.hms.common.preferences.ippreferences;

import com.insta.hms.common.preferences.PreferencesRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/** The Class IpPreferencesRepository. */
@Repository
public class IpPreferencesRepository extends PreferencesRepository {

  /** Instantiates a new ip preferences repository. */
  public IpPreferencesRepository() {
    super("ip_preferences");
  }
}

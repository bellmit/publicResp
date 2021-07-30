package com.insta.hms.common.preferences.userpreferences;

import com.insta.hms.common.preferences.PreferencesRepository;

import org.springframework.stereotype.Repository;

@Repository("userPreferencesRepository")
public class UserPreferencesRepository extends PreferencesRepository {

  UserPreferencesRepository() {
    super("user_preferences");
  }

}

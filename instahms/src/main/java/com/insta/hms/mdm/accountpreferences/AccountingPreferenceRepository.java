package com.insta.hms.mdm.accountpreferences;

import com.insta.hms.common.preferences.PreferencesRepository;

import org.springframework.stereotype.Repository;

/**
 * The AccountingPreference Repository.
 * 
 * @author deepak
 */
@Repository
public class AccountingPreferenceRepository extends PreferencesRepository {

  public AccountingPreferenceRepository() {
    super("hosp_accounting_prefs");
  }

}

package com.insta.hms.mdm.accountpreferences;

import com.insta.hms.common.preferences.PreferencesService;

import org.springframework.stereotype.Service;

/**
 * The AccountingPreference Service.
 * 
 * @author deepak
 */
@Service
public class AccountingPreferenceService extends PreferencesService {

  public AccountingPreferenceService(AccountingPreferenceRepository repository,
      AccountPreferencesValidator validator) {
    super(repository, validator);
  }
}

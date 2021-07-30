package com.insta.hms.common.preferences.ippreferences;

import com.insta.hms.common.preferences.PreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesValidator;

import org.springframework.stereotype.Service;

// TODO: Auto-generated Javadoc
/** The Class IpPreferencesService. */
@Service
public class IpPreferencesService extends PreferencesService {

  /** The ip preferences repository. */
  private IpPreferencesRepository ipPreferencesRepository;

  /**
   * Instantiates a new ip preferences service.
   *
   * @param ipPreferencesRepository the ip preferences repository
   * @param genericPreferencesValidator the generic preferences validator
   */
  public IpPreferencesService(
      IpPreferencesRepository ipPreferencesRepository,
      GenericPreferencesValidator genericPreferencesValidator) {
    super(ipPreferencesRepository, genericPreferencesValidator);
    this.ipPreferencesRepository = ipPreferencesRepository;
  }
}

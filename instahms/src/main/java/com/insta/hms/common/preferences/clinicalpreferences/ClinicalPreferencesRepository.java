package com.insta.hms.common.preferences.clinicalpreferences;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ClinicalPreferencesRepository extends GenericRepository {

  public ClinicalPreferencesRepository() {
    super("clinical_preferences");
  }

}

package com.insta.hms.mdm.patientheaderpreferences;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This is service for patient header preferences.
 *
 * @author sainathbatthala
 */
@Service
public class PatientHeaderPreferencesService {

  /** The repository. */
  @Autowired
  PatientHeaderPreferencesRepository repository;

  /**
   * This method gets preferences to display.
   *
   * @param dataLevel
   *          the data level
   * @param visitType
   *          the visit type
   * @param dataCategory
   *          the data category
   * @return preferences to display
   */
  public List<BasicDynaBean> getPreferencesToDisplay(String dataLevel, String[] visitType,
      String[] dataCategory) {
    return repository.getPreferencesToDisplay(dataLevel, visitType, dataCategory);
  }

}

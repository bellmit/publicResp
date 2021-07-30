package com.insta.hms.common.preferences.clinicalpreferences;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ConversionException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class ClinicalPreferencesService.
 */
@Service
public class ClinicalPreferencesService {

  /** The repo. */
  @LazyAutowired
  private ClinicalPreferencesRepository repo;
  
  /** The validator. */
  @LazyAutowired
  private ClinicalPreferencesValidator validator;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** Returns the clinical preferences bean.
   *
   * @return the clinical preferences
   **/
  public BasicDynaBean getClinicalPreferences() {
    return repo.getRecord();
  }

  /**
   * Updates clinical preferences.
   *
   * @param requestBody the request body
   * @return the clinical preferences
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> update(Map<String, Object> requestBody) {
    BasicDynaBean bean = repo.getRecord();
    List<String> errorFields = new ArrayList<>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);

    if (!errorFields.isEmpty()) {
      throw new ConversionException(errorFields);
    }
    validator.validateClinicalPreferences(bean);
    bean.set("username", sessionService.getSessionAttributes().get("userId"));
    repo.update(bean, null);
    return bean.getMap();
  }
}

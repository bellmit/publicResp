package com.insta.hms.common.validation;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

public class CenterCheckRule extends NotNullRule {
  private static final Integer DEFAULT_CENTER = 0;

  public CenterCheckRule() {
  }

  public CenterCheckRule(String key) {
    super(key);
  }

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    GenericPreferencesService genericPreferencesService = ApplicationContextProvider
        .getBean(GenericPreferencesService.class);

    Integer maximumCentersApplicable = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    boolean isMultiCenter = null != maximumCentersApplicable && maximumCentersApplicable > 2;

    if (isMultiCenter) {
      return super.apply(bean, fields, errorMap);
    } else {
      // Not clean.
      if (null == bean.get(fields[0])) {
        bean.set(fields[0], DEFAULT_CENTER);
      }
      return true;
    }

  }

}

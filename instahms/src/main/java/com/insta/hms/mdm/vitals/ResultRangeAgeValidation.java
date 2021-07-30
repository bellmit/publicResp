package com.insta.hms.mdm.vitals;

import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

/**
 * The Class ResultRangeAgeValidation.
 * @author yaskumar
 */
public class ResultRangeAgeValidation extends ValidationRule {

  /** The age message. */
  private String ageMessage = "ui.error.patient.min.age.cannot.exeed.patent.max.age";

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    Boolean ok = true;
    if (null != bean && (bean.get("min_patient_age") != null && bean.get("max_patient_age") != null)
        && ((Integer) bean.get("min_patient_age") >= (Integer) bean.get("max_patient_age")
            && null != errorMap.getErrorMap())) {
      errorMap.addError("min_patient_age", ageMessage);
      ok = false;
    }

    return ok;
  }
}

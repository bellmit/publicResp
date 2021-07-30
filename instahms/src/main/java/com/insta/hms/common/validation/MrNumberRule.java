package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * This rule checks if given mr number is valid or not.
 * 
 * @author sainathbatthala
 *
 */
@Component
public class MrNumberRule extends ValidationRule {

  @Autowired
  PatientDetailsService patientDetailsService;

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        if (bean.get(field) != null && !((String) bean.get(field)).isEmpty()
            && !patientDetailsService.isMrNumberValid((String) bean.get(field))) {
          if (null != errorMap.getErrorMap()) {
            errorMap.addError(field, "exception.invalid.value",
                Arrays.asList(StringUtil.prettyName(field)));
          }
          ok = false;
        }
      }
    }
    return ok;
  }

}

package com.insta.hms.mdm.breaktheglass;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class BreakTheGlassCheck extends ValidationRule {

  @Autowired
  PatientDetailsService patientDetailsService;

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean result = false;
    if (null != bean) {
      result = true;
      for (String field : fields) {
        if (bean.get(field) != null && !((String) bean.get(field)).isEmpty()
            && !patientDetailsService.isBreakTheGlassAllowed((String) bean.get(field))) {
          if (null != errorMap.getErrorMap()) {
            errorMap.addError(field, "exception.break.the.glass.not.allowed",
                Arrays.asList(StringUtil.prettyName(field)));
          }
        }
      }
    }
    return result;

  }

}

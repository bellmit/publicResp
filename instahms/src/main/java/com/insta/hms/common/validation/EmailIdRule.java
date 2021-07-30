package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.validator.EmailValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EmailIdRule extends ValidationRule {

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        if (bean.get(field) != null && !((String) bean.get(field)).isEmpty()
            && !(EmailValidator.getInstance().isValid((String) bean.get(field)))) {
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

package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Arrays;

public class NonNegativeNumberRule extends PropertyValidationRule {

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        Object value = bean.get(field);
        if (value instanceof BigDecimal) {
          if (BigDecimal.ZERO.compareTo((BigDecimal) value) > 0) {
            if (null != errorMap.getErrorMap()) {
              errorMap.addError(field, "exception.not.negative",
                  Arrays.asList(StringUtil.prettyName(field)));
            }
            ok = ok && false;
          }
        } else if (value instanceof Integer) {
          if (new Integer(0).compareTo((Integer) value) > 0) {
            if (null != errorMap.getErrorMap()) {
              errorMap.addError(field, "exception.not.negative",
                  Arrays.asList(StringUtil.prettyName(field)));
            }
            ok = ok && false;
          }
        }
      }
    }
    return ok;
  }
}
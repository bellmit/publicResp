package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;

public class DoctorConsultationTypeRule extends NotNullRule {

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      if ("Doctor".equals(bean.get("activity_id")) && (bean.get("consultation_type_id") == null
          || "".equals(bean.get("consultation_type_id")))) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("consultation_type_id", "exception.notnull.value",
              Arrays.asList(StringUtil.prettyName("consultation_type_id")));
        }
        ok = false;
      }
    }
    return ok;
  }

}
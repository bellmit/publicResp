package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.RunJavaScript;
import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.script.ScriptException;

/**
 * The Class SectionFieldOptionsValidator.
 *
 * @author krishnat
 */
@Component
public class SectionFieldOptionsValidator {

  /** The not null list. */
  private String[] notNullList = new String[] {"field_detail_id", "option_id"};

  /** The errmsgs. */
  // Error messages for corresponding field in above list
  private String[] errmsgs = new String[] {"exception.instasection.option.notnull.field_detail_id",
      "exception.instasection.option.notnull.option_id"};

  /**
   * Common validation.
   *
   * @param bean the bean
   * @param errMap the err map
   * @param regExpPattern the reg exp pattern
   * @param optionbean the optionbean
   * @return true, if successful
   */
  public boolean commonValidation(BasicDynaBean bean, ValidationErrorMap errMap, Map regExpPattern,
      BasicDynaBean optionbean) {
    boolean ok = true;
    int index = 0;
    for (String field : this.notNullList) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        ok = false;
        errMap.addError(field, errmsgs[index]);
      }
      index++;
    }
    int optionId = (Integer) bean.get("option_id");
    if (optionId == 0 || optionId == -1) {
      // no validation for normal and others.
    } else {
      String optionRemarks = (String) bean.get("option_remarks");
      if ((optionRemarks != null && !optionRemarks.equals(""))) {
        BasicDynaBean regExpBean =
            (BasicDynaBean) regExpPattern.get((Integer) optionbean.get("option_pattern_id"));
        if (regExpBean != null && regExpBean.get("regexp_pattern") != null
            && !regExpBean.get("regexp_pattern").equals("")) {
          RunJavaScript script = new RunJavaScript();
          try {
            if (!script.validateRegExp(optionRemarks, (String) regExpBean.get("regexp_pattern"))) {
              ok = false;
              errMap.addError("option_remarks", "exception.not.matched.regular.expression");
            }
          } catch (ScriptException se) {
            ok = false;
            errMap.addError("option_remarks", se.getMessage());
          }

        }
      }
    }
    return ok;
  }

  /**
   * Validate.
   *
   * @param bean the bean
   * @param errMap the err map
   * @param regExpPatterns the reg exp patterns
   * @param optionBean the option bean
   * @return true, if successful
   */
  public boolean validate(BasicDynaBean bean, ValidationErrorMap errMap, Map regExpPatterns,
      BasicDynaBean optionBean) {
    return commonValidation(bean, errMap, regExpPatterns, optionBean);

  }

}

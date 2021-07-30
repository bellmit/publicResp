package com.insta.hms.core.clinical.forms;

import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class SectionImageValidator.
 *
 * @author krishnat
 */
@Component
public class SectionImageValidator {

  /** The not null list. */
  private String[] notNullList = new String[] {"field_detail_id", "marker_detail_id", "marker_id",
      "coordinate_x", "coordinate_y"};

  /** The errmsgs. */
  // Error messages for corresponding field in above list
  private String[] errmsgs = new String[] {"exception.instasection.image.notnull.field_detail_id",
      "exception.instasection.image.notnull.marker_detail_id",
      "exception.instasection.image.notnull.marker_id",
      "exception.instasection.image.notnull.coordinate_x",
      "exception.instasection.image.notnull.coordinate_y"};

  /**
   * Common validation.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean commonValidation(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = true;
    int index = 0;
    for (String field : this.notNullList) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        ok = false;
        errMap.addError(field, errmsgs[index]);
      }
      index++;
    }
    return ok;
  }

  /**
   * Validate.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validate(BasicDynaBean bean, ValidationErrorMap errMap) {
    return commonValidation(bean, errMap);
  }


}

package com.insta.hms.core.clinical.forms;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterValidator;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class InstaSectionImageValidator.
 *
 * @author krishnat
 */
@Component
public class InstaSectionImageValidator extends MasterValidator {

  /** The not null list. */
  private String[] notNullList = new String[] {"file_content"};

  /** The errmsgs. */
  // Error messages for corresponding field in above list
  private String[] errmsgs = new String[] {"exception.instasection.image.notnull.content"};

  /**
   * Common validation.
   *
   * @param bean the bean
   * @param errMap the err map
   * @param notNullList the not null list
   * @param errmsgs the errmsgs
   * @return true, if successful
   */
  public boolean commonValidation(BasicDynaBean bean, ValidationErrorMap errMap,
      String[] notNullList, String[] errmsgs) {
    boolean ok = true;
    int index = 0;
    for (String field : notNullList) {
      if (bean.get(field) == null) {
        ok = false;
        errMap.addError(field, errmsgs[index]);
      }
      index++;
    }

    return ok;
  }

  /**
   * Validate insert.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = true;
    if (!commonValidation(bean, errMap, notNullList, errmsgs)) {
      ok = false;
    }
    return ok;
  }

  /**
   * Validate update.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = true;
    if (!commonValidation(bean, errMap, notNullList, errmsgs)) {
      ok = false;
    }
    if (!ValidationUtils.isKeyValid("patient_section_images", (Integer) bean.get("image_id"),
        "image_id")) {
      errMap.addError("image_id", "exception.instasection.image.notvalid.image_id");
      ok = false;
    }

    return ok;
  }

}

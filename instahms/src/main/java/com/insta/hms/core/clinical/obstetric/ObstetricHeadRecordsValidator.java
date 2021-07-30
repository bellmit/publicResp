package com.insta.hms.core.clinical.obstetric;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class ObstetricHeadRecordsValidator.
 *
 * @author anupvishwas
 */

@Component
public class ObstetricHeadRecordsValidator {

  /**
   * Validate obs head records update.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHeadRecordsUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    if (bean.get("obstetric_record_id") == null
        || bean.get("obstetric_record_id").toString().isEmpty()) {
      errMap.addError("obstetric_record_id", "exception.obsheadrecord.notnull.obsrecordid");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("obstetric_headrecords",
        ((Integer) bean.get("obstetric_record_id")), "obstetric_record_id")) {
      errMap.addError("obstetric_record_id", "exception.obsheadrecord.notvalid.obsrecordid");
      valid = false;
    }
    return valid;
  }

}

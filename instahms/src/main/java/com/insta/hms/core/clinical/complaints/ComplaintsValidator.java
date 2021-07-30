package com.insta.hms.core.clinical.complaints;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * Complaints validator.
 * 
 * @author anupvishwas
 *
 */

@Component
public class ComplaintsValidator {

  /**
   * Validate sec complaints.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateSecComplaints(BasicDynaBean bean, ValidationErrorMap errMap) {

    if (bean.get("row_id") == null || bean.get("row_id").toString().isEmpty()) {
      errMap.addError("row_id", "exception.seccomplaint.notnull.rowid");
      return false;
    }
    if (!ValidationUtils.isKeyValid("secondary_complaints", (Integer) bean.get("row_id"),
        "row_id")) {
      errMap.addError("row_id", "exception.seccomplaint.notvalid.rowid");
      return false;
    }
    return true;
  }

  /**
   * Validate sec complaints update.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateSecComplaintsUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {

    return validateSecComplaints(bean, errMap);
  }

  /**
   * Validate sec complaints delete.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateSecComplaintsDelete(BasicDynaBean bean, ValidationErrorMap errMap) {

    return validateSecComplaints(bean, errMap);
  }

}

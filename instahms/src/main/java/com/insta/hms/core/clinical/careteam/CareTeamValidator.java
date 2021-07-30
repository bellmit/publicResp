package com.insta.hms.core.clinical.careteam;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Care team validator.
 * 
 * @author anupvishwas
 *
 */

@Component
public class CareTeamValidator {

  /** The not null list. */
  private String[] notNullList = new String[] {"care_doctor_id", "patient_id"};

  /** The errmsgs. */
  // Error messages for corresponding field in above list
  private String[] errmsgs = new String[] {"exception.pac.notnull.doctor",
      "exception.pac.notnull.status"};

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
    if (!ValidationUtils.isKeyValid("doctors", (String) bean.get("care_doctor_id"), "doctor_id")) {
      errMap.addError("doctor_id", "exception.pac.notvalid.doctor");
      ok = false;
    }

    return ok;
  }

  /**
   * Validate care team on delete.
   *
   * @param bean the bean
   * @param addmitingDoctor the addmiting doctor
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateCareTeamOnDelete(BasicDynaBean bean, String addmitingDoctor,
      ValidationErrorMap errMap) {
    boolean valid = commonValidation(bean, errMap);

    if (addmitingDoctor.equals(bean.get("care_doctor_id"))) {
      errMap.addError("admitting_doctor", "exception.cannot.delete.admitting.doctorid");
      valid = false;
    }
    return valid;

  }

  /**
   * Checks if is duplicate care team doctor.
   *
   * @param bean the bean
   * @param exsitingCareTeamList the exsiting care team list
   * @return true, if is duplicate care team doctor
   */
  public boolean isDuplicateCareTeamDoctor(BasicDynaBean bean,
      List<BasicDynaBean> exsitingCareTeamList) {
    boolean isDuplicate = false;
    for (BasicDynaBean existingDocBean : exsitingCareTeamList) {
      if (existingDocBean.get("care_doctor_id").equals(bean.get("care_doctor_id"))) {
        isDuplicate = true;
      }

    }
    return isDuplicate;
  }
}

package com.insta.hms.core.clinical.pac;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.SectionRightsValidator;
import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * The Class PreAnaesthestheticValidator.
 *
 * @author teja
 */
@Component
public class PreAnaesthestheticValidator {

  /** The stn rights. */
  @LazyAutowired
  private SectionRightsValidator stnRights;

  /** The not null list. */
  private String[] notNullList =
      new String[] {"doctor_id", "status", "pac_date", "pac_validity", "patient_pac_remarks"};

  /** The errmsgs. */
  // Error messages for corresponding field in above list
  private String[] errmsgs = new String[] {"exception.pac.notnull.doctor",
      "exception.pac.notnull.status", "exception.pac.notnull.conduction.date",
      "exception.pac.notnull.validity", "exception.pac.notnull.remarks"};

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
    if (!ValidationUtils.isKeyValid("doctors", (String) bean.get("doctor_id"), "doctor_id")) {
      errMap.addError("doctor_id", "exception.pac.notvalid.doctor");
      ok = false;
    }
    if (bean.get("pac_date") != null && bean.get("pac_validity") != null
        && ((Timestamp) bean.get("pac_date")).after((Timestamp) bean.get("pac_validity"))) {
      errMap.addError("pac_validity", "exception.pac.notvalid.pac.validity");
      ok = false;
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
    return commonValidation(bean, errMap);
  }

  /**
   * Validate update.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = commonValidation(bean, errMap);
    if (bean.get("patient_pac_id") == null || bean.get("patient_pac_id").toString().isEmpty()) {
      errMap.addError("patient_pac_id", "exception.pac.notnull.pacid");
      ok = false;
    }
    if (!ValidationUtils.isKeyValid("patient_pac", (Integer) bean.get("patient_pac_id"),
        "patient_pac_id")) {
      errMap.addError("patient_pac_id", "exception.pac.notvalid.pacid");
      ok = false;
    }
    return ok;
  }

  /**
   * Validate delete.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateDelete(BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get("patient_pac_id") == null || bean.get("patient_pac_id").toString().isEmpty()) {
      errMap.addError("patient_pac_id", "exception.pac.notnull.pacid");
      return false;
    }
    return true;
  }

}

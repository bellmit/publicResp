package com.insta.hms.core.clinical.healthmaintenance;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.SectionRightsValidator;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class HealthMaintenanceValidation.
 *
 * @author sonam
 */
@Component
public class HealthMaintenanceValidation {

  /** The section rights. */
  @LazyAutowired
  private SectionRightsValidator sectionRights;
  
  /** The Constant DOCTOR_ID. */
  private static final String DOCTOR_ID = "doctor_id";
  
  /** The Constant ACTIVITY. */
  private static final String ACTIVITY = "activity";
  
  /** The Constant RECORDED_DATE. */
  private static final String RECORDED_DATE = "recorded_date";
  
  /** The Constant HEALTH_MAINT_ID. */
  private static final String HEALTH_MAINT_ID = "health_maint_id";
 
  /** The not null fields. */
  private String[] notNullFields = new String[] { ACTIVITY, DOCTOR_ID, RECORDED_DATE };

  /** The errormsg. */
  private String[] errormsg = new String[] { "exception.notnull.health.maint.activity",
      "exception.notnull.health.maint.doctor", "exception.notnull.health.maint.recordeddate" };

  /**
   * Health maint validation.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean healthMaintValidation(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    int index = 0;
    for (String field : this.notNullFields) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        valid = false;
        errMap.addError(field, errormsg[index]);
      }
      index++;
    }
    if (!ValidationUtils.isKeyValid("doctors", (String) bean.get(DOCTOR_ID), DOCTOR_ID)) {
      errMap.addError(DOCTOR_ID, "exception.health.notvalid.doctor");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate health insert.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateHealthInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    return healthMaintValidation(bean, errMap);
  }

  /**
   * Validate health update.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateHealthUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = healthMaintValidation(bean, errMap);
    if (bean.get(HEALTH_MAINT_ID) == null || bean.get(HEALTH_MAINT_ID).toString().isEmpty()) {
      errMap.addError(HEALTH_MAINT_ID, "exception.health.maintid.notnull");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("patient_health_maintenance",
        (Integer) bean.get(HEALTH_MAINT_ID), HEALTH_MAINT_ID)) {
      errMap.addError(HEALTH_MAINT_ID, "exception.health.maintid.notvalid.");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate health delete.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateHealthDelete(BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get(HEALTH_MAINT_ID) == null || bean.get(HEALTH_MAINT_ID).toString().isEmpty()) {
      errMap.addError(HEALTH_MAINT_ID, "exception.health.maintid.notnull");
      return false;
    }
    return true;
  }

  /**
   * Checks for edit section rights.
   *
   * @param roleId
   *          the role id
   * @param sectionId
   *          the section id
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean hasEditSectionRights(Integer roleId, Integer sectionId,
      ValidationErrorMap errMap) {
    boolean valid = sectionRights.validate(roleId, sectionId);
    if (!valid) {
      errMap.addError("section", "exception.section.allergy.noEditRights");
    }
    return valid;
  }
}

package com.insta.hms.mdm.departments;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

@Component
public class DepartmentValidator extends MasterValidator {

  @LazyAutowired
  private RegistrationPreferencesService registrationPreferencesService;

  @Override
  public boolean validateUpdate(BasicDynaBean bean) {
    super.validateUpdate(bean);
    String emergencyPatientDepartmentId = (String) registrationPreferencesService
        .getRegistrationPreferences().get("emergency_patient_department_id");
    String departmentId = (String) bean.get("dept_id");
    String departmentStatus = (String) bean.get("status");
    if (departmentStatus.equals("I") && departmentId.equals(emergencyPatientDepartmentId)) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      String errorKey = "exception.cannot.make.department.inactive.because.it.is.mapped.as."
          + "the.default.emergency.patient.department.in.registration.preferences";
      errorMap.addError("status", errorKey);
      throw new ValidationException(errorMap);
    }
    return true;
  }

}

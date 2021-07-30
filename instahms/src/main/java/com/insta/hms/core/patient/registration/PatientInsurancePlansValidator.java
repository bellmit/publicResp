package com.insta.hms.core.patient.registration;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.springframework.stereotype.Component;

@Component
public class PatientInsurancePlansValidator {

  /**
   * Validate patient id paramaeter.
   *
   * @param patientId
   *          the patient id
   * @return true, if successful
   */
  public boolean validatePatientIdParamaeter(String patientId) {
    ValidationErrorMap errMap = new ValidationErrorMap();

    if (!ValidationUtils.isKeyValid("patient_registration", patientId, "patient_id")) {
      errMap.addError("patient_id", "exception.insurance.plan.notvalid.patientid");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

}

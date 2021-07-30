package com.insta.hms.core.medicalrecords;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.springframework.stereotype.Component;

/**
 * The Class MRDDiagnosisValidator.
 *
 * @author anup vishwas
 */

@Component
public class MRDDiagnosisValidator {

  /** The Constant PATIENT_REGISTRATION. */
  private static final String PATIENT_REGISTRATION = "patient_registration";
  
  /** The Constant MR_NO. */
  private static final String MR_NO = "mr_no";

  /**
   * Validate prev diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @return true, if successful
   */
  public boolean validatePrevDiagnosisDetails(String patientId, String mrNo) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!ValidationUtils.isKeyValid(PATIENT_REGISTRATION, patientId,
        "patient_id")) {
      errMap.addError("patient_id",
          "exception.prevdiagdetail.notvalid.patientid");
    }
    if (!ValidationUtils.isKeyValid(PATIENT_REGISTRATION, mrNo, MR_NO)) {
      errMap.addError("mr_no", "exception.prevdiagdetail.notvalid.mrno");
    }

    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }

    return true;
  }

  /**
   * Validate diagnosis code and patient.
   *
   * @param mrNo
   *          the mr no
   * @param diagCode
   *          the diag code
   */
  public void validateDiagnosisCodeAndPatient(String mrNo, String diagCode) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!ValidationUtils.isKeyValid(PATIENT_REGISTRATION, mrNo, MR_NO)) {
      errMap.addError("mr_no", "exception.getonsetyear.notvalid.mrno");
    }
    if (!ValidationUtils.isKeyValid("mrd_codes_master", diagCode, "code")) {
      errMap.addError("icd_code", "exception.getonsetyear.notvalid.icd_code");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
  }

}

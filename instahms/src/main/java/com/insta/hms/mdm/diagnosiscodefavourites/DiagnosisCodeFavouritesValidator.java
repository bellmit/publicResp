/**
 * 
 */

package com.insta.hms.mdm.diagnosiscodefavourites;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DiagnosisCodeFavouritesValidator.
 *
 * @author anup vishwas
 */

@Component
public class DiagnosisCodeFavouritesValidator extends MasterValidator {
  Logger logger = LoggerFactory.getLogger(DiagnosisCodeFavouritesValidator.class);

  private static final String DOCTOR_ID = "doctor_id";
  private static final String CODE_TYPE = "code_type"; 
  
  /**
   * Validate diag code fav parameters.
   *
   * @param searchInput
   *          the search input
   * @param doctorId
   *          the doctor id
   * @param codeType
   *          the code type
   * @return true, if successful
   */
  public boolean validateDiagCodeFavParameters(String searchInput, String doctorId,
      String codeType) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!ValidationUtils.isKeyValid("doctors", doctorId, DOCTOR_ID)) {
      errMap.addError(DOCTOR_ID, "exception.favdoagnosiscode.notvalid.doctorid");
    }
    if (!codeType.isEmpty()
        && !ValidationUtils.isKeyValid("mrd_codes_doctor_master", codeType, CODE_TYPE)) {
      errMap.addError("diag_code_type", "exception.favdoagnosiscode.notvalid.diagcodetype");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }

    return true;
  }

  /**
   * Validate code fav insert.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateCodeFavInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    Map<Integer, String> colMap = new HashMap<>();
    boolean success = true;
    colMap.put(0, CODE_TYPE);
    colMap.put(1, DOCTOR_ID);
    colMap.put(2, "code");
    if (ValidationUtils.isDuplicateData("mrd_codes_doctor_master", colMap,
        new Object[] {bean.get(CODE_TYPE), bean.get(DOCTOR_ID), bean.get("code")})) {
      success = false;
    }
    if (!success) {
      logger.info("exception.favdoagnosiscode.duplicate.diagcode.");
    }
    return success;
  }
}

package com.insta.hms.mdm.icdsupportedcodes;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class IcdSupportedCodeTypesValidator.
 *
 * @author anup vishwas
 */
@Component
public class IcdSupportedCodeTypesValidator extends MasterValidator {

  /**
   * Validate diag code parameters.
   *
   * @param searchInput the search input
   * @param codeType the code type
   * @return true, if successful
   */
  public boolean validateDiagCodeParameters(String searchInput, String codeType) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!codeType.isEmpty()
        && !ValidationUtils.isKeyValid("mrd_supported_code_types", codeType, "code_type")) {
      errMap.addError("diag_code_type", "exception.favdoagnosiscode.notvalid.diagcodetype");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }

    return true;
  }
}

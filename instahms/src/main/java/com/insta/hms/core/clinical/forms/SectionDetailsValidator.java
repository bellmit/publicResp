package com.insta.hms.core.clinical.forms;

import com.bob.hms.common.RequestContext;
import com.insta.hms.exception.ValidationErrorMap;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class SectionDetailsValidator.
 *
 * @author krishnat
 */
@Component
public class SectionDetailsValidator {

  /**
   * Instantiates a new section details validator.
   */
  public SectionDetailsValidator() {

  }

  /**
   * Allow section undo.
   *
   * @param roleId the role id
   * @param finalized the finalized
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean allowSectionUndo(int roleId, String finalized, ValidationErrorMap errMap) {
    boolean valid = true;
    Map actionRightsMap = (Map) RequestContext.getSession().getAttribute("actionRightsMap");
    boolean isSectionUndo = actionRightsMap.get("undo_section_finalization").equals("A");
    if (roleId != 1 && roleId != 2) {
      if (!isSectionUndo && finalized.equals("Y")) {
        errMap.addError("section_id", "exception.form.section.finalized");
        valid = false;
      }
    }
    return valid;

  }
}

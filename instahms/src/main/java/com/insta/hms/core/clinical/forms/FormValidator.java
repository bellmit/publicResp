package com.insta.hms.core.clinical.forms;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The Class FormValidator.
 *
 * @author teja
 */
@Component
public class FormValidator {

  /** The form service. */
  @LazyAutowired
  ClinicalFormService clinicalFormService;

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
    if (roleId != 1 && roleId != 2) {
      boolean isSectionUndo = actionRightsMap.get("undo_section_finalization").equals("A");
      if (!isSectionUndo && finalized.equals("Y")) {
        errMap.addError("section_id", "exception.form.section.finalized");
        valid = false;
      }
    }
    return valid;

  }

  /**
   * Validate section parameters.
   *
   * @param sectionItemId the section item id
   * @param genericItemId the generic item id
   * @param itemIdName the item id name
   * @param formtype the formtype
   * @return true, if successful
   */
  public boolean validateSectionParameters(Integer sectionItemId, Integer genericItemId,
      String itemIdName, String formtype) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (formtype.equals("Form_CONS")
        && !ValidationUtils.isKeyValid("doctor_consultation", sectionItemId, "consultation_id")) {
      errMap.addError(itemIdName, "exception.form.notvalid.sectionitemid");
    }

    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

  /**
   * Validate form parameters.
   *
   * @param bean the bean
   * @return true, if successful
   */
  public boolean validateFormParameters(BasicDynaBean bean) {
    ValidationErrorMap errorMap = new ValidationErrorMap();
    Object filterBy = bean.get((String) bean.get("form_field_name"));
    if (filterBy == null) {
      errorMap.addError((String) bean.get("form_field_name"), "");
    } else {
      BasicDynaBean consultBean = clinicalFormService.getRecord(bean);
      if (consultBean == null) {
        errorMap.addError((String) bean.get("form_field_name"), "");
      }
    }
    if (errorMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorMap);
    }
    return true;
  }

  /**
   * Validate section id.
   *
   * @param sectionId the section id
   * @return true, if successful
   */
  public boolean validateSectionId(Integer sectionId) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (sectionId < 0
        && !ValidationUtils.isKeyValid("system_generated_sections", sectionId, "section_id")) {
      errMap.addError("section_id", "exception.form.notvalid.sectionid");
    }

    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

  /**
   * Validate section details.
   *
   * @param sectionId the section id
   * @param requestParam the request param
   * @return true, if successful
   */
  public boolean validateSectionDetails(Integer sectionId, Map<String, Object> requestParam) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if ((sectionId < 0 && !ValidationUtils.isKeyValid("system_generated_sections", sectionId,
        "section_id"))
        || (sectionId > 0 && !ValidationUtils
            .isKeyValid("section_master", sectionId, "section_id"))) {
      errMap.addError("section_id", "exception.form.notvalid.sectionid");
    }
    Integer secId = (Integer) requestParam.get("section_id");
    if (secId != null && !sectionId.equals(secId)) {
      errMap.addError("section_id", "exception.sectionid.notvalid");
    }
    Integer sectionDetailId = (Integer) requestParam.get("section_detail_id");
    if (sectionDetailId == null || sectionDetailId == 0) {
      errMap.addError("section_detail_id",
          "exception.form.notnull.section.detailid");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }


  /**
   * Validate custom form.
   *
   * @param data the data
   * @return true, if successful
   */
  public boolean validateCustomForm(Map<String, Object> data) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!(data.get("section_ids") instanceof List<?>)) {
      errMap.addError("section_ids", "exception.notValid.custom.form.sections");
    } else if (data.get("section_ids") == null || ((List<?>) data.get("section_ids")).isEmpty()) {
      errMap.addError("section_ids", "exception.required.custom.form.sections");
    }
    if (!ValidationUtils.isKeyValid("department", data.get("dept_id"), "dept_id")) {
      errMap.addError("dept_id", "exception.notvalid.custom.form.dept.id");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }
}

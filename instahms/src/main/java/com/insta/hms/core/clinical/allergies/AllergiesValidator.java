package com.insta.hms.core.clinical.allergies;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.SectionRightsValidator;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.allergy.AllergyTypeService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Allergies Validator.
 * 
 * @author krishnat
 *
 */
@Component
public class AllergiesValidator {

  /** The section rights. */
  @LazyAutowired
  private SectionRightsValidator sectionRights;
  /** Allergy type master. */
  @LazyAutowired
  private AllergyTypeService allergyTypeService;

  /**
   * Allergies validation.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean allergiesValidation(BasicDynaBean bean, ValidationErrorMap errMap) {

    boolean valid = true;
    boolean flag = false;

    if (StringUtil.isNullOrEmpty((String) bean.get("status"))) {
      valid = false;
      errMap.addError("status", "exception.notnull.patient.allergy.status");
    }

    if (bean.get("severity") != null && !bean.get("severity").equals("")) {
      flag = false;
      if (bean.get("severity").equals("Mild") || bean.get("severity").equals("Moderate") || bean
          .get("severity").equals("Severe") || bean.get("severity").equals("Unknown")) {
        flag = true;
      }
      if (!flag) {
        errMap.addError("severity", "exception.severity.value.not.valid");
        valid = false;
      }
    }
    return valid;
  }

  /**
   * Validate allergy insert.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateAllergyInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    return allergiesValidation(bean, errMap);
  }

  /**
   * Validate allergy type.
   *
   * @param allergydata the allergydata
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateAllergyType(List<BasicDynaBean> allergydata, ValidationErrorMap errMap) {
    List<Object> type = new ArrayList<Object>();
    Set<Integer> allergenCodeSet = new HashSet<>();
    boolean valid = true;
    for (BasicDynaBean b : allergydata) {

      type.add(b.get(AllergiesConstants.ALLERGY_TYPE_ID));
      Integer allergenCode = (Integer) b.get(AllergiesConstants.ALLERGEN_CODE_ID);

      if (b.get(AllergiesConstants.ALLERGY_TYPE_ID) == null && allergenCode != null) {
        errMap.addError(AllergiesConstants.ALLERGY_TYPE,
            "exception.patient.invalid.allergy.mapping");
        valid = false;
        break;
      }

      if (allergenCodeSet.contains(allergenCode)) {
        if (allergenCode == null) {
          errMap.addError(AllergiesConstants.ALLERGY_TYPE,
              "exception.patient.duplicate.no.allergy");
          valid = false;
          break;
        }

        errMap.addError(AllergiesConstants.ALLERGY_TYPE, "exception.patient.duplicate.allergy");
        valid = false;
        break;
      }
      allergenCodeSet.add(allergenCode);

      if (type.size() > 1 && type.contains(null)) {
        errMap.addError(AllergiesConstants.ALLERGY_TYPE,
            "exception.patient.invalid.allergy.mapping");
        valid = false;
        break;
      }
    }
    return valid;
  }

  /**
   * Validate allergy update.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateAllergyUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = allergiesValidation(bean, errMap);
    if (bean.get(AllergiesConstants.ALLERGY_ID) == null || bean.get(AllergiesConstants.ALLERGY_ID)
        .toString()
        .isEmpty()) {
      errMap.addError(AllergiesConstants.ALLERGY_ID, "exception.allergy.notnull.allergyid");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("patient_allergies", (Integer) bean.get(
        AllergiesConstants.ALLERGY_ID),
        AllergiesConstants.ALLERGY_ID)) {
      errMap.addError(AllergiesConstants.ALLERGY_ID, "exception.allergy.notvalid.allergyid");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate allergy delete.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateAllergyDelete(BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get(AllergiesConstants.ALLERGY_ID) == null || bean.get("allergy_id").toString()
        .isEmpty()) {
      errMap.addError(AllergiesConstants.ALLERGY_ID, "exception.allergy.notnull.allergyid");
      return false;
    }
    return true;
  }

  /**
   * Checks for edit section rights.
   *
   * @param roleId the role id
   * @param sectionId the section id
   * @param errMap the err map
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

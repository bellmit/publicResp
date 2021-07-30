package com.insta.hms.core.clinical.patientproblems;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patient Problem List Validator.
 * 
 * @author VinayKumarJavalkar
 *
 */
@Component
public class PatientProblemListValidator {
  private String[] notNullList = new String[] {"patient_problem_id", "onset"};

  private String[] errorMsgs =
      new String[] {"exception.patientProblemList.notnull.patientproblemid",
          "exception.patientProblemList.notnull.onset"};

  /**
   * Validate Patient Problem List.
   * 
   * @param bean the bean
   * @param errMap the map
   * @return boolean value
   */
  public boolean validatePatientProblemList(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    int count = 0;

    for (String field : this.notNullList) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        valid = false;
        errMap.addError(field, errorMsgs[count]);
      }
      count++;
    }
    return valid;
  }

  /**
   * Validate Patient Problem List insert.
   * 
   * @param bean the bean
   * @param errMap the map
   * @return boolean value
   */
  public boolean validatePatientProblemListInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validatePatientProblemList(bean, errMap);
  }

  /**
   * Validate Patient Problem List update.
   * 
   * @param bean the bean
   * @param errMap the map
   * @return boolean value
   */
  public boolean validatePatientProblemListUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validatePatientProblemList(bean, errMap);
  }

  /**
   * Validate Patien Problem List Delete.
   * 
   * @param insertBeanList bean list
   * @param updateBeanList bean list
   * @param bean the bean
   * @param errMap the map
   * @return boolean value
   */
  public boolean validatePatientProblemListDelete(List<BasicDynaBean> insertBeanList,
      List<BasicDynaBean> updateBeanList, BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get("id") == null || bean.get("ppl_id").toString().isEmpty()) {
      errMap.addError("id", "exception.patientProblemList.notnull.patientproblemid");
      return false;
    }

    if (!ValidationUtils.isKeyValid("mrd_codes_master",
        ((BigDecimal) bean.get("patient_problem_id")), "mrd_code_id")) {
      errMap.addError("id", "exception.patientProblemMaster.notvalid.id");
      return false;
    }
    return true;
  }
}

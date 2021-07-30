package com.insta.hms.core.clinical.patientproblems;

import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * Patient Problem List Details Validator.
 * 
 * @author VinayKumarJavalkar
 *
 */
@Component
public class PatientProblemListDetailsValidator {
  private String[] notNullList = new String[] {"ppl_id", "problem_status", "visit_id"};

  private String[] errorMsgs =
      new String[] {"exception.patientProblemListDetail.notnull.patientproblemlistid",
          "exception.patientProblemListDetail.notnull.problemstatus",
          "exception.patientProblemListDetail.notnull.visitid"};

  /**
   * Validate Patient Problem List insert.
   * 
   * @param bean the bean
   * @param errMap the map
   * @return boolean value
   */
  public boolean validatePatientProblemListInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
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
}

package com.insta.hms.core.clinical.diagnosisdetails;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat.
 *
 */

@Component
public class DiagnosisDetailsValidator {

  @LazyAutowired
  private DiagnosisDetailsRepository repo;

  private String[] notNullList = new String[] { "icd_code", "code_type", "diag_type" };

  private String[] errorMsgs = new String[] { "exception.diagdetail.notnull.idccode",
      "exception.diagdetail.notnull.codetype", "exception.diagdetail.notnull.diagtype" };

  /**
   * Validate diag details.
   * @param bean the BasicDynaBean
   * @param errMap the ValidationErrorMap
   * @return boolean value
   */
  public boolean validateDiagnosisDetails(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    int count = 0;

    for (String field : this.notNullList) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        valid = false;
        errMap.addError(field, errorMsgs[count]);
      }
      count++;
    }
    String doctorId = (String) bean.get("doctor_id");
    if (doctorId != null && !doctorId.equals("")
        && !ValidationUtils.isKeyValid("doctors", (String) bean.get("doctor_id"), "doctor_id")) {
      errMap.addError("doctor_id", "exception.diagdetail.notvalid.doctorid");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("mrd_codes_master", (String) bean.get("icd_code"), "code")) {
      errMap.addError("icd_code", "exception.diagdetail.notvalid.icdcode");
      valid = false;
    }
    if (bean.get("diagnosis_status_id") != null && !bean.get("diagnosis_status_id").equals("")) {
      if (!ValidationUtils.isKeyValid("diagnosis_statuses",
          (Integer) bean.get("diagnosis_status_id"), "diagnosis_status_id")) {
        errMap.addError("diagnosis_status_id", "exception.diagdetail.notvalid.diagstatus");
        valid = false;
      }
    }
    return valid;
  }

  /**
   * Validate diag details.
   * @param bean the BasicDynaBean
   * @param errMap the ValidationErrorMap
   * @return boolean value
   */
  public boolean validateDiagDetailsInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    Map<Integer, String> mrdColMap = new HashMap<Integer, String>();
    mrdColMap.put(0, "icd_code");
    mrdColMap.put(1, "visit_id");

    valid = validateDiagnosisDetails(bean, errMap);
    return valid;
  }

  /**
   * Validate diag details.
   * @param bean the BasicDynaBean
   * @param errMap the ValidationErrorMap
   * @return boolean value
   */
  public boolean validateDiagDetailsUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    Map<Integer, String> mrdColMap = new HashMap<Integer, String>();
    mrdColMap.put(0, "icd_code");
    mrdColMap.put(1, "visit_id");
    boolean valid = validateDiagnosisDetails(bean, errMap);
    if (bean.get("id") == null || bean.get("id").toString().isEmpty()) {
      errMap.addError("id", "exception.diagdetail.notnull.id");
      valid = false;
    }

    if (!ValidationUtils.isKeyValid("mrd_diagnosis", ((BigDecimal) bean.get("id")), "id")) {
      errMap.addError("id", "exception.diagdetail.notvalid.id");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate diag details.
   * @param insertBeanList the list of BasicDynaBean
   * @param updateBeanList the list of BasicDynaBean
   * @param bean the BasicDynaBean
   * @param errMap ValidationErrorMap
   * @return boolean value
   */
  public boolean validateDiagDetailsDelete(List<BasicDynaBean> insertBeanList,
      List<BasicDynaBean> updateBeanList, BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get("id") == null || bean.get("id").toString().isEmpty()) {
      errMap.addError("id", "exception.diagdetail.notnull.id");
      return false;
    }

    if (!ValidationUtils.isKeyValid("mrd_diagnosis", ((BigDecimal) bean.get("id")), "id")) {
      errMap.addError("id", "exception.diagdetail.notvalid.id");
      return false;
    }

    return true;
  }

}

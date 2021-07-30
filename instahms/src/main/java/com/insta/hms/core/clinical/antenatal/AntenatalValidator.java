package com.insta.hms.core.clinical.antenatal;

import com.insta.hms.common.DateHelper;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * The Class AntenatalValidator.
 *
 * @author anupvishwas
 */

@Component
public class AntenatalValidator {

  /**
   * Validate antenatal details id.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateAntenatalDetailsId(BasicDynaBean bean, ValidationErrorMap errMap) {
    if (bean.get("antenatal_id") == null || bean.get("antenatal_id").toString().isEmpty()) {
      errMap.addError("antenatal_id", "exception.antenatal.notnull.antenatalid");
      return false;
    }
    if (!ValidationUtils.isKeyValid("antenatal", ((Integer) bean.get("antenatal_id")),
        "antenatal_id")) {
      errMap.addError("antenatal_id", "exception.antenatal.notvalid.antenatalid");
      return false;
    }
    return true;
  }

  /**
   * Validate antenatal details.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateAntenatalDetails(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean isValid = true;
    if (bean.get("visit_date") == null || bean.get("visit_date").toString().isEmpty()) {
      errMap.addError("visit_date", "exception.antenatal.notnull.visitdate");
      isValid = false;
    }
    if (bean.get("doctor_id") != null && !bean.get("doctor_id").toString().isEmpty()) {
      if (!ValidationUtils.isKeyValid("doctors", ((String) bean.get("doctor_id")), "doctor_id")) {
        errMap.addError("doctor_id", "exception.antenatal.notvalid.doctorid");
        isValid = false;
      }
    }
    if (bean.get("height_fundus") != null && !bean.get("height_fundus").toString().isEmpty()) {
      if (((BigDecimal) bean.get("height_fundus")).intValue() < 0
          || ((BigDecimal) bean.get("height_fundus")).intValue() > 100) {
        errMap.addError("height_fundus", "exception.antenatal.notvalid.heightfundus");
        isValid = false;
      }
    }
    if (bean.get("systolic_bp") != null && !bean.get("systolic_bp").toString().isEmpty()) {
      if ((Integer) bean.get("systolic_bp") < 10 || (Integer) bean.get("systolic_bp") > 300) {
        errMap.addError("systolic_bp", "exception.antenatal.notvalid.systolicbp");
        isValid = false;
      }
    }
    if (bean.get("diastolic_bp") != null && !bean.get("diastolic_bp").toString().isEmpty()) {
      if ((Integer) bean.get("diastolic_bp") < 10 || (Integer) bean.get("diastolic_bp") > 300) {
        errMap.addError("diastolic_bp", "exception.antenatal.notvalid.diastolicbp");
        isValid = false;
      }
    }
    if (bean.get("weight") != null && !bean.get("weight").toString().isEmpty()) {
      if (((BigDecimal) bean.get("weight")).intValue() < 0
          || ((BigDecimal) bean.get("weight")).intValue() > 200) {
        errMap.addError("weight", "exception.antenatal.notvalid.weight");
        isValid = false;
      }
    }
    if (bean.get("visit_date") != null && !bean.get("visit_date").toString().isEmpty()
        && bean.get("next_visit_date") != null
        && !bean.get("next_visit_date").toString().isEmpty()) {
      LocalDate nextVisitDate =
          DateHelper.parseDate(bean.get("next_visit_date").toString(), "yyyy-MM-dd");
      LocalDate visitDate = DateHelper.parseDate(bean.get("visit_date").toString(), "yyyy-MM-dd");
      if (nextVisitDate.compareTo(new LocalDate()) < 0 && nextVisitDate.compareTo(visitDate) < 0) {
        errMap.addError("next_visit_date", "exception.antenatal.notvalid.nextvisitdate");
        isValid = false;
      }
    }
    // at least one record should be filled in the grid apart from visit
    // date
    if ((bean.get("gestation_age") == null || bean.get("gestation_age").toString().isEmpty())
        && (bean.get("height_fundus") == null || bean.get("height_fundus").toString().isEmpty())
        && (bean.get("presentation") == null || bean.get("presentation").toString().isEmpty())
        && (bean.get("rel_pp_brim") == null || bean.get("rel_pp_brim").toString().isEmpty())
        && (bean.get("foetal_heart") == null || bean.get("foetal_heart").toString().isEmpty())
        && (bean.get("urine") == null || bean.get("urine").toString().isEmpty())
        && (bean.get("systolic_bp") == null || bean.get("systolic_bp").toString().isEmpty())
        && (bean.get("diastolic_bp") == null || bean.get("diastolic_bp").toString().isEmpty())
        && (bean.get("weight") == null || bean.get("weight").toString().isEmpty())
        && (bean.get("prescription_summary") == null
            || bean.get("prescription_summary").toString().isEmpty())
        && (bean.get("doctor_id") == null || bean.get("doctor_id").toString().isEmpty())
        && (bean.get("next_visit_date") == null || bean.get("next_visit_date").toString().isEmpty())
        && (bean.get("lmp") == null || bean.get("lmp").toString().isEmpty())
        && (bean.get("edd") == null || bean.get("edd").toString().isEmpty())
        && (bean.get("final_edd") == null || bean.get("final_edd").toString().isEmpty())) {
      errMap.addError("grid_validation", "exception.antenatal.notvalid.requireatleastone");
      isValid = false;
    }

    return isValid;
  }

  /**
   * Validate antenatal insert.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateAntenatalInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validateAntenatalDetails(bean, errMap);
  }

  /**
   * Validate antenatal update.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateAntenatalUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean isValid = validateAntenatalDetailsId(bean, errMap);
    isValid = validateAntenatalDetails(bean, errMap);
    return isValid;
  }

  /**
   * Validate antenatal delete.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateAntenatalDelete(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validateAntenatalDetailsId(bean, errMap);
  }

}

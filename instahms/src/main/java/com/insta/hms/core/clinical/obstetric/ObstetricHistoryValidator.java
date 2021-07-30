package com.insta.hms.core.clinical.obstetric;

import com.insta.hms.common.DateHelper;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * The Class ObstetricHistoryValidator.
 *
 * @author anupvishwas
 */

@Component
public class ObstetricHistoryValidator {

  /**
   * Validate obs history id.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHistoryId(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    if (bean.get("pregnancy_history_id") == null
        || bean.get("pregnancy_history_id").toString().isEmpty()) {
      errMap.addError("pregnancy_history_id", "exception.obshistory.notnull.pregnancyhistoryid");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("pregnancy_history",
        ((Integer) bean.get("pregnancy_history_id")), "pregnancy_history_id")) {
      errMap.addError("pregnancy_history_id", "exception.obshistory.notvalid.pregnancyhistoryid");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate obs history insert.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHistoryInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validateObsHisInsertOrUpdate(bean, errMap);
  }

  /**
   * Validate obs history update.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHistoryUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = validateObsHistoryId(bean, errMap);
    valid = validateObsHisInsertOrUpdate(bean, errMap);
    return valid;
  }

  /**
   * Validate obs history delete.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHistoryDelete(BasicDynaBean bean, ValidationErrorMap errMap) {
    return validateObsHistoryId(bean, errMap);
  }

  /**
   * Validate obs his insert or update.
   *
   * @param bean
   *          the bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateObsHisInsertOrUpdate(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean valid = true;
    if (bean.get("date") == null || bean.get("date").toString().isEmpty()) {
      errMap.addError("date", "exception.obshistory.notnull.date");
      valid = false;
    }
    if (bean.get("weeks") != null && !bean.get("weeks").toString().isEmpty()) {
      if ((Integer) bean.get("weeks") < 1 || (Integer) bean.get("weeks") > 50) {
        errMap.addError("weeks", "exception.obshistory.notvalid.weeks");
        valid = false;
      }
    }
    if (bean.get("weight") != null && !bean.get("weight").toString().isEmpty()) {
      if (((BigDecimal) bean.get("weight")).intValue() < 0
          || ((BigDecimal) bean.get("weight")).intValue() > 10) {
        errMap.addError("weight", "exception.obshistory.notvalid.weight");
        valid = false;
      }
    }
    if (bean.get("sex") != null && !bean.get("sex").toString().isEmpty()) {
      if (!(bean.get("sex").equals("M") || bean.get("sex").equals("F") || bean.get("sex").equals(
          "O"))) {
        errMap.addError("sex", "exception.obshistory.notvalid.sex");
        valid = false;
      }
    }
    if (bean.get("date") != null && !bean.get("date").toString().isEmpty()) {
      LocalDate obsDate = DateHelper.parseDate(bean.get("date").toString(), "yyyy-MM-dd");
      if (obsDate.compareTo(new LocalDate()) > 0) {
        errMap.addError("date", "exception.obshistory.notvalid.obsdate");
        valid = false;
      }
    }
    // at least one record should be filled in the grid apart from visit date
    if ((bean.get("weeks") == null || bean.get("weeks").toString().isEmpty())
        && (bean.get("place") == null || bean.get("place").toString().isEmpty())
        && (bean.get("method") == null || bean.get("method").toString().isEmpty())
        && (bean.get("weight") == null || bean.get("weight").toString().isEmpty())
        && (bean.get("sex") == null || bean.get("sex").toString().isEmpty())
        && (bean.get("complications") == null || bean.get("complications").toString().isEmpty())
        && (bean.get("feeding") == null || bean.get("feeding").toString().isEmpty())
        && (bean.get("outcome") == null || bean.get("outcome").toString().isEmpty())) {
      errMap.addError("grid_validation", "exception.obshistory.notvalid.requireatleastone");
      valid = false;
    }

    return valid;
  }

}

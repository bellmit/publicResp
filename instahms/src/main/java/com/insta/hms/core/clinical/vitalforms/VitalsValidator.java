package com.insta.hms.core.clinical.vitalforms;

import com.bob.hms.common.DateUtil;
import com.insta.hms.exception.ValidationErrorMap;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Map;

/**
 * The Class VitalsValidator.
 *
 * @author krishnat
 */
@Component
public class VitalsValidator {

  /** The vv not null list. */
  private String[] vvNotNullList = new String[] {"date_time"};

  /** The vverrmsgs. */
  // Error messages for corresponding field in above list
  private String[] vverrmsgs = new String[] {"exception.vital.notnull.datetime"};

  /** The vr not null list. */
  private String[] vrNotNullList = new String[] {"vital_reading_id", "param_id"};

  /** The vrerrmsgs. */
  private String[] vrerrmsgs =
      new String[] {"exception.vital.notnull.vital_reading_id", "exception.vital.notnull.param_id"};

  /**
   * Common validation.
   *
   * @param bean the bean
   * @param errMap the err map
   * @param notNullList the not null list
   * @param errmsgs the errmsgs
   * @return true, if successful
   */
  public boolean commonValidation(BasicDynaBean bean, ValidationErrorMap errMap,
      String[] notNullList, String[] errmsgs) {
    boolean ok = true;
    int index = 0;
    for (String field : notNullList) {
      if (bean.get(field) == null || bean.get(field).toString().isEmpty()) {
        ok = false;
        errMap.addError(field, errmsgs[index]);
      }
      index++;
    }

    return ok;
  }

  /**
   * Validate visit vitals insert.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateVisitVitalsInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = true;

    if (!commonValidation(bean, errMap, vvNotNullList, vverrmsgs)) {
      ok = false;
    }
    if (((Timestamp) bean.get("date_time")).after(DateUtil.getCurrentTimestamp())) {
      errMap.addError("date_time", "exception.vital.datetime.cannot.be.in.future");
      ok = false;
    }

    return ok;
  }

  /**
   * Validate vital reading insert.
   *
   * @param bean the bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateVitalReadingInsert(BasicDynaBean bean, ValidationErrorMap errMap) {
    boolean ok = true;
    if (!commonValidation(bean, errMap, vrNotNullList, vrerrmsgs)) {
      ok = false;
    }
    return ok;
  }


  /**
   * Validate visit vitals update.
   *
   * @param visitBean the visit bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateVisitVitalsUpdate(BasicDynaBean visitBean, ValidationErrorMap errMap) {
    boolean ok = true;
    if (!commonValidation(visitBean, errMap, vvNotNullList, vverrmsgs)) {
      ok = false;
    }
    if (((Timestamp) visitBean.get("date_time")).after(DateUtil.getCurrentTimestamp())) {
      errMap.addError("date_time", "exception.vital.datetime.cannot.be.in.future");
      ok = false;
    }
    return ok;
  }

  /**
   * Validate vital reading update.
   *
   * @param readingBean the reading bean
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateVitalReadingUpdate(BasicDynaBean readingBean, ValidationErrorMap errMap) {
    boolean ok = true;
    if (!commonValidation(readingBean, errMap, vrNotNullList, vrerrmsgs)) {
      ok = false;
    }
    return ok;
  }

  /**
   * Validate delete.
   *
   * @param map the map
   * @param errMap the err map
   * @return true, if successful
   */
  public boolean validateDelete(Map map, ValidationErrorMap errMap) {
    if (map.get("vital_reading_id") == null || (Integer) map.get("vital_reading_id") == 0) {
      errMap.addError("vital_reading_id", "exception.vital.notnull.vital_reading_id");
      return false;
    }
    return true;
  }

}

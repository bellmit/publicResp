package com.insta.hms.mdm.vitals;

import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;

/**
 * The Class ResultRangeValidation.
 * @author yashwant
 */
public class ResultRangeValidation extends ValidationRule {

  /** The message 1. */
  private String message1 = "ui.error.min.critical.can.not.be.greater.than.min.normal";
  
  /** The message 11. */
  private String message11 = "ui.error.max.critical.can.not.be.less.than.max.normal";
  
  /** The message 2. */
  private String message2 = "ui.error.min.abnormal.can.not.be.greater.than.min.critical";
  
  /** The message 21. */
  private String message21 = "ui.error.max.abnormal.can.not.be.less.than.max.critical";
  
  /** The message 4. */
  private String message4 = "ui.error.min.normal.can.not.be.greater.than.max.normal";
  
  /** The message 5. */
  private String message5 = "ui.error.min.abnormal.can.not.be.greater.than.max.abnormal";
  
  /** The message 6. */
  private String message6 = "ui.error.min.critical.can.not.be.greater.than.max.critical";

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = true;
    if (null != bean) {
      BigDecimal minNormal = (BigDecimal) bean.get("min_normal_value");
      BigDecimal maxNormal = (BigDecimal) bean.get("max_normal_value");
      BigDecimal minCritical = (BigDecimal) bean.get("min_critical_value");
      BigDecimal maxCritical = (BigDecimal) bean.get("max_critical_value");
      BigDecimal minImprobable = (BigDecimal) bean.get("min_improbable_value");
      BigDecimal maxImprobabl = (BigDecimal) bean.get("max_improbable_value");
      if (((minImprobable != null && minNormal != null) 
          && minImprobable.compareTo(minNormal) > 0)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("min_improbable_value", message1);
        }
        ok = false;
      } else if (((maxImprobabl != null && maxNormal != null)
          && maxImprobabl.compareTo(maxNormal) < 0)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("max_improbable_value", message11);
        }
        ok = false;
      } else if (((minImprobable != null && minCritical != null)
          && minCritical.compareTo(minImprobable) > 0)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("min_critical_value", message2);
        }
        ok = false;
      } else if (((maxImprobabl != null && maxCritical != null)
          && maxCritical.compareTo(maxImprobabl) < 0)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("max_critical_value", message21);
        }
        ok = false;
      }
      if (((minNormal != null && maxNormal != null) && minNormal.compareTo(maxNormal) > 0)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("min_normal_value", message4);
        }
        ok = false;
      } else if ((minCritical != null && maxCritical != null)
          && minCritical.compareTo(maxCritical) > 0) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("min_critical_value", message6);
          ok = false;
        }
      } else if ((minImprobable != null && maxImprobabl != null)
          && minImprobable.compareTo(maxImprobabl) > 0) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("min_improbable_value", message5);
        }
        ok = false;
      }
    }
    return ok;
  }

}

package com.insta.hms.mdm.vitals;

import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;

/**
 * The Class ResultRangeNullValidation.
 * @author yashwant
 */
public class ResultRangeNullValidation extends ValidationRule {

  /** The message 3. */
  private String message3 = "ui.error.please.enter.values.in.the.following.order";

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
      if ((minNormal == null && minImprobable != null) 
          || (maxNormal == null && maxImprobabl != null)
          || ((minNormal == null || minImprobable == null) && minCritical != null)
          || ((maxNormal == null || maxImprobabl == null) && maxCritical != null)) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("refrence_range_order", message3);
          ok = false;
        }
      }
    }
    return ok;
  }

}

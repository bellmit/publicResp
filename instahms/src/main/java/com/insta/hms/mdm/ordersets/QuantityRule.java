package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.validation.PropertyValidationRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;
import java.util.List;

public class QuantityRule extends PropertyValidationRule {

  private String messageKey1 = "exception.value.not.greater.than.one";
  private String messageKey2 = "exception.value.not.less.than.one";

  private List<String> quantityEnabledTypes = Arrays.asList("Consumable", "Implant", "Medicine",
      "Other Charge", "Service", "Equipment");

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      Integer quantity = (Integer) bean.get("activity_qty");
      if (quantity <= 0) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("activity_qty", this.messageKey2,
              Arrays.asList(StringUtil.prettyName("quantity")));
        }
        ok = false;
      } else if (((!quantityEnabledTypes.contains(bean.get("activity_type")))
          && (quantity != null && quantity > 1))) {
        if (null != errorMap.getErrorMap()) {
          errorMap.addError("activity_qty", this.messageKey1,
              Arrays.asList(StringUtil.prettyName("quantity")));
        }
        ok = false;
      }
    }
    return ok;
  }

}

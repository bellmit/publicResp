package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;

public class NotUpdatableRule extends PropertyValidationRule {

  BulkDataIntegrationRepository<?> repository;
  String messageKey = "exception.notupdatable.value";

  public NotUpdatableRule(BulkDataIntegrationRepository<?> repository) {
    this.repository = repository;
  }

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {

    boolean ok = false;
    if (null != bean) {
      BasicDynaBean oldBean = repository.findByKey(repository.getIntegrationKeyColumn(),
          bean.get(repository.getIntegrationKeyColumn()));
      if (oldBean == null) {
        return true;
      }
      ok = true;
      for (String field : fields) {
        if (null == bean.get(field) || !bean.get(field).equals(oldBean.get(field))) {
          if (null != errorMap.getErrorMap()) {
            errorMap.addError(field, this.messageKey, Arrays.asList(StringUtil.prettyName(field)));
          }
          ok = false;
        }
      }
    }
    return ok;
  }

}

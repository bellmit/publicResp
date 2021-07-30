package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueNameRule extends PropertyValidationRule {

  @SuppressWarnings("rawtypes")
  private MasterRepository repository;

  @SuppressWarnings("rawtypes")
  public UniqueNameRule(MasterRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        boolean fieldOk = true;
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put(field, bean.get(field));
        List<BasicDynaBean> matches = repository.findByCriteria(filterMap);
        Object beanKey = bean.get(repository.getKeyColumn());
        if (!matches.isEmpty()) {
          if (null == beanKey) {
            fieldOk = false;
          } else if (!matches.get(0).get(repository.getKeyColumn()).equals(beanKey)) {
            fieldOk = false;
          }
        }
        if (!fieldOk) {
          errorMap.addError(field, "exception.must.be.unique", 
              Arrays.asList(StringUtil.prettyName(field)));
        }
        ok = ok && fieldOk;
      }
    }
    return ok;
  }

}
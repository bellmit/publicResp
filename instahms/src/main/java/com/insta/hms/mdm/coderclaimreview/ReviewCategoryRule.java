package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.PropertyValidationRule;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReviewCategoryRule extends PropertyValidationRule {

  private static Map<String, Object> filterMap = new HashMap<>();

  @LazyAutowired
  ReviewCategoryService reviewCategoryService;

  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    filterMap.put("category_id", bean.get("review_category_id"));
    if (reviewCategoryService.lookup(true, filterMap).isEmpty()) {
      errorMap.addError("review_category_id", "exception.master.review.types.inactive.category");
      return false;
    }
    return true;
  }

}

package com.insta.hms.mdm.insuranceplans;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.UniqueNameRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsurancePlanValidator.
 */
@Component
public class InsurancePlanValidator extends MasterValidator {
  
  @LazyAutowired
  private InsurancePlanRepository insurancePlanRepo;
  
  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final  String[] NOT_NULL_FIELDS_INSERT = new String[] { "plan_name", "username",
      "status" };
  
  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final  ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final  String[] NOT_NULL_FIELDS_UPDATE = new String[] { "plan_id", "plan_name",
      "username", "status" };
  
  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final  ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule(); 

  /**
   * Instantiates a new insurance plan validator.
   */
  public InsurancePlanValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);    
  }
  
  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    boolean result = applyRuleSet(INSERT_RULESET_NAME, bean);
    if (!result) {
      throwErrors();
    }
    
    String planName = (String)bean.get("plan_name");
    Integer planType = (Integer)bean.get("category_id");
    
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("plan_name", planName);
    filterMap.put("category_id", planType);
    List<BasicDynaBean> matches = insurancePlanRepo.findByCriteria(filterMap);
    if (!matches.isEmpty()) {
      errorMap.addError("plan_name", "exception.must.be.unique", 
          Arrays.asList(StringUtil.prettyName("plan_name_and_network_type")));
      throwErrors();
    }
    return true;
  }

}

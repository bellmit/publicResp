package com.insta.hms.mdm.areas;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class AreaValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"area_name", "city_id", "status"};
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"area_id", "area_name", "city_id", "status"};
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /** All the rules Defined in here. */
  public AreaValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}

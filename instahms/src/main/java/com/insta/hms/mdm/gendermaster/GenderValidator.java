package com.insta.hms.mdm.gendermaster;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;
import org.springframework.stereotype.Component;

@Component
public class GenderValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "gender_name", "gender_id",
      "status" };
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "gender_name", "gender_id",
      "status" };
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new city validator.
   */
  public GenderValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}

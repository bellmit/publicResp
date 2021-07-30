package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.UniqueNameRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterDetailsValidator;

import org.springframework.stereotype.Component;

@Component
public class ConfidentialityGroupValidator extends MasterDetailsValidator {

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();
  private static final MaximumLengthRule MAX_LENGTH_RULE_100 = new MaximumLengthRule(100);
  private static final MaximumLengthRule MAX_LENGTH_RULE_4 = new MaximumLengthRule(4);
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "name", "abbreviation" };
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "confidentiality_grp_id",
      "name", "abbreviation" };
  private static final String[] USER_CONFIDENTIALITY_ASSOCIATION_NOT_NULL_FIELDS_INSERT =
      new String[] { "emp_username", "confidentiality_grp_id", "status" };
  private static final String[] USER_CONFIDENTIALITY_ASSOCIATION_NOT_NULL_FIELDS_UPDATE =
      new String[] { "id", "emp_username", "confidentiality_grp_id", "status" };

  /**
   * Not Null rule for name and abbreviation.
   * Name cannot be more than 100 characters.
   * Abbreviation cannot be more than 4 characters.
   */
  public ConfidentialityGroupValidator(ConfidentialityGroupRepository repository) {
    UniqueNameRule uniqueNameRule = new UniqueNameRule(repository);
    addInsertRule(uniqueNameRule, new String[] { "abbreviation", "name" });
    addUpdateRule(uniqueNameRule, new String[] { "abbreviation", "name" });
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule("user_confidentiality_association", NOT_NULL_RULE,
        USER_CONFIDENTIALITY_ASSOCIATION_NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("user_confidentiality_association", NOT_NULL_RULE,
        USER_CONFIDENTIALITY_ASSOCIATION_NOT_NULL_FIELDS_UPDATE);
    addDefaultRule(MAX_LENGTH_RULE_100, new String[] { "name" });
    addDefaultRule(MAX_LENGTH_RULE_4, new String[] { "abbreviation" });
  }
}

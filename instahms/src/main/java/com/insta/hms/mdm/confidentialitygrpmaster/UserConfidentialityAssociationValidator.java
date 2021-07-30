package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class UserConfidentialityAssociationValidator extends MasterValidator {

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "emp_username",
      "confidentiality_grp_id", "status" };
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "id", "emp_username",
      "confidentiality_grp_id", "status" };

  public UserConfidentialityAssociationValidator() {
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}

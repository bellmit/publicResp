package com.insta.hms.mdm.breaktheglass;

import com.insta.hms.common.validation.MrNumberRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class UserMrNoAssociationValidator extends MasterValidator {

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();
  private static final BreakTheGlassCheck BREAK_THE_GLASS_RULE = new BreakTheGlassCheck();
  private static final MrNumberRule MR_NUMBER_RULE = new MrNumberRule();
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "emp_username", "mr_no" };
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "id", "emp_username",
      "mr_no" };
  private static final String[] MR_NO = new String[] { "mr_no" };

  /**
   * Instantiates a new user mr no association validator.
   * Validation checks:
   * 1. Check if mrno is valid for update and delete.
   * 2. Check if break the glass is allowed for mrno
   * 3. Check if username and mrno are null for inserts.
   * 4. Check if id,username and mrno are null for updates.
   * 
   */
  public UserMrNoAssociationValidator() {
    addDefaultRule(MR_NUMBER_RULE, MR_NO);
    addDefaultRule(BREAK_THE_GLASS_RULE, MR_NO);
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }

}

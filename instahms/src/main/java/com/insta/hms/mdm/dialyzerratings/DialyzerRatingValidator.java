package com.insta.hms.mdm.dialyzerratings;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class DialyzerRatingValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "dialyzer_rating" };
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "dialyzer_rating_id",
      "dialyzer_rating" };

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  public DialyzerRatingValidator() {
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
  }
}
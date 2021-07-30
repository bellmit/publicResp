package com.insta.hms.mdm.phrasesuggestions;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/** 
 * @author Sonam.
 * */
@Component
public class PhraseSuggestionsValidator extends MasterValidator {
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"phrase_suggestions_desc"};
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"phrase_suggestions_id", "phrase_suggestions_desc"};
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * validator constructor.
   */
  public PhraseSuggestionsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}

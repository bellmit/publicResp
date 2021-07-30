/** */

package com.insta.hms.mdm.phrasesuggestionscategories;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/** 
 * @author sonam.
 *  */
@Component
public class PhraseSuggestionsCategoryValidator extends MasterValidator {
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"phrase_suggestions_category"};
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"phrase_suggestions_category_id", "phrase_suggestions_category"};
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Constructor.
   */
  public PhraseSuggestionsCategoryValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}

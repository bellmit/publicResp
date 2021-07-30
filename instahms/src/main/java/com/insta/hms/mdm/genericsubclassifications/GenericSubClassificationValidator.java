package com.insta.hms.mdm.genericsubclassifications;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * 
 * @author irshadmohammed.
 *
 */
@Component
public class GenericSubClassificationValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS = new String[] { "sub_classification_name" };

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  public GenericSubClassificationValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }
}

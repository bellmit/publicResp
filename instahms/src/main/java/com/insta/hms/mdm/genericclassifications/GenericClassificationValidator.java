package com.insta.hms.mdm.genericclassifications;

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
public class GenericClassificationValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS = new String[] { "classification_name" };

  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  public GenericClassificationValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }
}

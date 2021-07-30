package com.insta.hms.mdm.samplecollectioncenters;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SampleCollectionCenterValidator extends MasterValidator {

  @LazyAutowired
  private SampleCollectionCenterService sampleCollectionCenterService;

  @LazyAutowired
  private MessageUtil messageUtil;

  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "collection_center" };
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "collection_center_id",
      "collection_center" };

  /** The errors key. */
  private static final String ERRORS_KEY = "Sample Collection Center";

  /** The failed validation message key. */
  private static final String FAILED_VALIDATION_MESSAGE_KEY = 
      "exception.samplecollectioncenter.dependents.validation.failed";

  /**
   * Instantiates a new sample collection center validator.
   */
  public SampleCollectionCenterValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }

  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    if (!super.validateInsert(bean)) {
      ValidationErrorMap errorsMap = new ValidationErrorMap();
      Integer collectionCenterCount = sampleCollectionCenterService.getMaxCollectionCenters();
      Integer maxCollectionCenterCount = sampleCollectionCenterService
          .getLimitedCollectionCenters();
      if (null != maxCollectionCenterCount && collectionCenterCount >= maxCollectionCenterCount) {
        errorsMap.addError(ERRORS_KEY + "", FAILED_VALIDATION_MESSAGE_KEY,
            Arrays.asList(new String[] { "Sample Collection Center" }));
      }
      if (!errorsMap.getErrorMap().isEmpty()) {
        throw new ValidationException(errorsMap);
      }
    }
    return false;
  }
}

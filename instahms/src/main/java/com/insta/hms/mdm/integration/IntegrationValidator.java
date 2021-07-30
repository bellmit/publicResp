package com.insta.hms.mdm.integration;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.NotUpdatableRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

public abstract class IntegrationValidator extends MasterValidator {
  protected abstract String[] getMandatoryFields();

  protected abstract String[] getNonUpdatableFields();

  BulkDataIntegrationRepository<?> repository;

  /**
   * Instantiates a new integration validator.
   *
   * @param repository the repository
   */
  public IntegrationValidator(BulkDataIntegrationRepository<?> repository) {
    this.repository = repository;
    ValidationRule notNullRule = new NotNullRule();
    ValidationRule notUpdatableRule = new NotUpdatableRule(repository);
    if (getMandatoryFields() != null && getMandatoryFields().length > 0) {
      addInsertRule(notNullRule, getMandatoryFields());
      addUpdateRule(notNullRule, getMandatoryFields());
    }
    if (getNonUpdatableFields() != null && getNonUpdatableFields().length > 0) {
      addUpdateRule(notUpdatableRule, getNonUpdatableFields());
    }
  }

  public BulkDataIntegrationRepository<?> getRepository() {
    return repository;
  }

}

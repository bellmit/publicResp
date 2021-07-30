package com.insta.hms.mdm.integration.iteminsurancecategory;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ItemInsuranceCategoriesIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "insurance_category_name",
      "insurance_payable", "integration_insurance_category_id" };

  public ItemInsuranceCategoriesIntegrationValidator(
      ItemInsuranceCategoriesIntegrationRepository repository) {
    super(repository);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return null;
  }

}

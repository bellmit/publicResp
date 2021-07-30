package com.insta.hms.mdm.integration.itemforms;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ItemFormIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_form_id",
      "item_form_name", "status", "granular_units" };

  public ItemFormIntegrationValidator(ItemFormIntegrationRepository repository) {
    super(repository);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return new String[] { "item_form_name" };
  }

}

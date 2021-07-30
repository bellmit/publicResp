package com.insta.hms.mdm.integration.taxgroups;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class TaxGroupsIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "item_group_name", "group_code",
      "status", "integration_group_id" };

  public TaxGroupsIntegrationValidator(TaxGroupsIntegrationRepository repository) {
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

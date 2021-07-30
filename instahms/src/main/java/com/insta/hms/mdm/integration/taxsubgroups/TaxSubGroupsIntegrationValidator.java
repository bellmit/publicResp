package com.insta.hms.mdm.integration.taxsubgroups;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class TaxSubGroupsIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_subgroup_id",
      "item_subgroup_name", "item_group_id", "status" };

  public TaxSubGroupsIntegrationValidator(TaxSubGroupsIntegrationRepository repository) {
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

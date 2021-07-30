package com.insta.hms.mdm.integration.stores;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ManufacturerIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_manf_id",
      "manf_name", "manf_mnemonic", "status", "pharmacy", "inventory" };

  public ManufacturerIntegrationValidator(ManufacturerIntegrationRepository repository) {
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
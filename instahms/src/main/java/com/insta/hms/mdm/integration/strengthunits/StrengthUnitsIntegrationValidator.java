package com.insta.hms.mdm.integration.strengthunits;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class StrengthUnitsIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_strength_unit_id",
      "unit_id", "status" };

  public StrengthUnitsIntegrationValidator(StrengthUnitsIntegrationRepository repository) {
    super(repository);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return new String[] { "" };
  }

}
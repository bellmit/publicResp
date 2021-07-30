package com.insta.hms.mdm.integration.controltype;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ControlTypeIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "control_type_name",
      "integration_control_type_id" };

  public ControlTypeIntegrationValidator(ControlTypeIntegrationRepository repository) {
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

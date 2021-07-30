package com.insta.hms.mdm.integration.servicegroup;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ServiceGroupIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_service_group_id",
      "status", "service_group_name", "display_order" };

  public ServiceGroupIntegrationValidator(ServiceGroupIntegrationRepository repository) {
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

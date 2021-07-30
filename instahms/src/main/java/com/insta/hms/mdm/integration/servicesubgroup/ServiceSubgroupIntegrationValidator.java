package com.insta.hms.mdm.integration.servicesubgroup;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class ServiceSubgroupIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] {
      "integration_service_sub_group_id", "status", "service_group_id", "service_sub_group_name",
      "display_order", "eligible_to_earn_points", "eligible_to_redeem_points" };

  public ServiceSubgroupIntegrationValidator(ServiceSubgroupIntegrationRepository repository) {
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

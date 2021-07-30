package com.insta.hms.mdm.integration.medicineroute;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class MedicineRouteIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_medicine_route_id",
      "route_id", "status" };

  public MedicineRouteIntegrationValidator(MedicineRouteIntegrationRepository repository) {
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
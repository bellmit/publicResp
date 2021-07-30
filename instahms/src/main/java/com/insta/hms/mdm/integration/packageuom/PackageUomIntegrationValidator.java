package com.insta.hms.mdm.integration.packageuom;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class PackageUomIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "package_uom", "issue_uom",
      "package_size" };
  private static final String[] NON_UPDATABLE_FIELDS = new String[] { "package_uom", "issue_uom",
      "package_size" };

  public PackageUomIntegrationValidator(PackageUomIntegrationRepository repository) {
    super(repository);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return NON_UPDATABLE_FIELDS;
  }

}

package com.insta.hms.mdm.integration.packageuom;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class PackageUomIntegrationEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_uom_id" };
  private static final String[] FIELDS = new String[] { "package_uom", "issue_uom",
      "package_size" };

  public PackageUomIntegrationEntity() {
    super(KEYS, FIELDS, null, null);
  }

}

package com.insta.hms.mdm.integration.packageuom;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PackageUomIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public PackageUomIntegrationRepository() {
    super("package_issue_uom", "uom_id", "integration_uom_id");
  }

}

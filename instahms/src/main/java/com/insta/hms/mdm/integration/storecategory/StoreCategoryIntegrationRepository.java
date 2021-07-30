package com.insta.hms.mdm.integration.storecategory;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreCategoryIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public StoreCategoryIntegrationRepository() {
    super("store_category_master", "category_id", "integration_category_id",
        "category");
  }
}

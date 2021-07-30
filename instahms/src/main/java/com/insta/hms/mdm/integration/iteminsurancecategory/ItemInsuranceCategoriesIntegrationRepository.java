package com.insta.hms.mdm.integration.iteminsurancecategory;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ItemInsuranceCategoriesIntegrationRepository
    extends BulkDataIntegrationRepository<Integer> {

  public ItemInsuranceCategoriesIntegrationRepository() {
    super("item_insurance_categories", "insurance_category_id",
        "integration_insurance_category_id");
  }

}

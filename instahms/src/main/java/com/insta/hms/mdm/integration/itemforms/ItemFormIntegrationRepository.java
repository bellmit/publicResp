package com.insta.hms.mdm.integration.itemforms;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ItemFormIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public ItemFormIntegrationRepository() {
    super("item_form_master", "item_form_id", "integration_form_id");
  }
  
}

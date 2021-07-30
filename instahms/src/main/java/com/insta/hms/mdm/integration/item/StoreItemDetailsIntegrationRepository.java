package com.insta.hms.mdm.integration.item;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreItemDetailsIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public StoreItemDetailsIntegrationRepository() {
    super("store_item_details", "medicine_id", "cust_item_code", "medicine_name");
  }
  
  @Override
  public Object getNextId() {
    return DatabaseHelper.getNextSequence("item_id");
  }

}

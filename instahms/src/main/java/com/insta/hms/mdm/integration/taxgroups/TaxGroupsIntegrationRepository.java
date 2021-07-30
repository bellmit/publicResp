package com.insta.hms.mdm.integration.taxgroups;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TaxGroupsIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public TaxGroupsIntegrationRepository() {
    super("item_groups", "item_group_id", "integration_group_id", "item_group_name",
        new String[] { "integration_group_id", "item_group_name", "item_group_id" });
  }

}

package com.insta.hms.mdm.integration.taxsubgroups;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TaxSubGroupsIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public TaxSubGroupsIntegrationRepository() {
    super("item_sub_groups", "item_subgroup_id", "integration_subgroup_id");
  }

}

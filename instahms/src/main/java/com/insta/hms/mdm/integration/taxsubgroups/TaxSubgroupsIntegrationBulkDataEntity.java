package com.insta.hms.mdm.integration.taxsubgroups;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class TaxSubgroupsIntegrationBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_subgroup_id" };
  private static final String[] FIELDS = new String[] { "item_subgroup_name",
      "item_subgroup_display_order", "subgroup_code", "item_group_id", "status" };
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("item_group_id", "item_groups", "item_group_id",
          "integration_group_id") };

  public TaxSubgroupsIntegrationBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }

}

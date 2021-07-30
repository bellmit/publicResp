package com.insta.hms.mdm.integration.taxgroups;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class TaxGroupsIntegrationBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_group_id" };
  private static final String[] FIELDS = new String[] { "item_group_name", "group_code",
      "item_group_display_order", "status" };

  public TaxGroupsIntegrationBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}

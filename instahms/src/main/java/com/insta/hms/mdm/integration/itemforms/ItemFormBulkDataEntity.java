package com.insta.hms.mdm.integration.itemforms;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ItemFormBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_form_id" };
  private static final String[] FIELDS = new String[] { "item_form_id", "item_form_name", "status",
      "granular_units" };

  public ItemFormBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}

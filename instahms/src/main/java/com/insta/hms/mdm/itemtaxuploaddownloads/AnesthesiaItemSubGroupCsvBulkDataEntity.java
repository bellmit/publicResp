package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class AnesthesiaItemSubGroupCsvBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] {};

  private static final String[] FIELDS = new String[] { "anesthesia_type_name", "item_group_id",
      "item_subgroup_id" };

  public AnesthesiaItemSubGroupCsvBulkDataEntity() {
    super(KEYS, FIELDS, null);
  }

}

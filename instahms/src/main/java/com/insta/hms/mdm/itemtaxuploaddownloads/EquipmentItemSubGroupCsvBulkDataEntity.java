package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class EquipmentItemSubGroupCsvBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "" };

  private static final String[] FIELDS = new String[] {};

  public EquipmentItemSubGroupCsvBulkDataEntity() {
    super(KEYS, FIELDS, null);
  }

}

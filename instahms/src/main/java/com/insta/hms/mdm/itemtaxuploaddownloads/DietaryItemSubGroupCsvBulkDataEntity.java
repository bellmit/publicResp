package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class DietaryItemSubGroupCsvBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] {};

  private static final String[] FIELDS = new String[] {};

  public DietaryItemSubGroupCsvBulkDataEntity() {
    super(KEYS, FIELDS, null);
  }

}

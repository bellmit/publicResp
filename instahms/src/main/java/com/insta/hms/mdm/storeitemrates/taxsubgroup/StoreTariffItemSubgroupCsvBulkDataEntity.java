package com.insta.hms.mdm.storeitemrates.taxsubgroup;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class StoreTariffItemSubgroupCsvBulkDataEntity extends CsVBulkDataEntity {
  private static final String[] KEYS = new String[] { "" };

  private static final String[] FIELDS = new String[] { "" };

  public StoreTariffItemSubgroupCsvBulkDataEntity() {
    super(KEYS, FIELDS, null);
  }
}

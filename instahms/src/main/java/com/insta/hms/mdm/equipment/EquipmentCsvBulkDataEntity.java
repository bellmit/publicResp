package com.insta.hms.mdm.equipment;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class EquipmentCsvBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] {"equip_id"};

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] {};

  /**
   * The Constant MASTERS with fields referencedField, referencedTable, referencedTablePK and.
   * referencedTableNameField
   */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {};

  /** Instantiates a new stores CSV bulk data entity. */
  public EquipmentCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }
}

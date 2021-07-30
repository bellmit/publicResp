package com.insta.hms.mdm.diagtatcenter;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

/**
 * The Class DiagTatCenterCsvBulkDataEntity.
 *
 * @author anil.n
 */
@Component("diagTatCSVEntity")
public class DiagTatCenterCsvBulkDataEntity extends CsVBulkDataEntity {

  /** The Constant KEYS. */
  private static final String[] KEYS = new String[] { "tat_center_id" };

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] {};

  /**
   * The Constant MASTERS with fields referencedField, referencedTable, referencedTablePK and
   * referencedTableNameField.
   */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {};

  /**
   * Instantiates a new stores CSV bulk data entity.
   */
  public DiagTatCenterCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }
}

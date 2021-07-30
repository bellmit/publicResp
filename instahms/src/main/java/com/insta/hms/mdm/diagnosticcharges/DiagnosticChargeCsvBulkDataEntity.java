package com.insta.hms.mdm.diagnosticcharges;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

/**
 * The Class DiagnosticChargeCSVBulkDataEntity.
 *
 * @author anil.n
 */
@Component("diagTestChargeCSVEntity")
public class DiagnosticChargeCsvBulkDataEntity extends CsVBulkDataEntity {

  /** The Constant KEYS. */
  private static final String[] KEYS = new String[] { "test_id", "bed_type",
    "org_name" };

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] {};

  /** The Constant MASTERS. */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {};

  /**
   * Instantiates a new diagnostic charge CSV bulk data entity.
  */
  public DiagnosticChargeCsvBulkDataEntity() {
   super(KEYS, FIELDS, null, MASTERS);
  }

}

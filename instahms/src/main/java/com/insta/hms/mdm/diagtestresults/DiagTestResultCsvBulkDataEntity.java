package com.insta.hms.mdm.diagtestresults;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

/**
 * The Class DiagTestResultCsvBulkDataEntity.
 *
 * @author anil.n
 */
@Component("diagTestResultCSVEntity")
public class DiagTestResultCsvBulkDataEntity extends CsVBulkDataEntity {

  /** The Constant KEYS. */
  private static final String[] KEYS = new String[] { "resultlabel_id" };

  /**
   * Instantiates a new stores CSV bulk data entity.
   */
  private static final String[] FIELDS = new String[] {};

  /** The Constant MASTERS. */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {};

  /**
   * Instantiates a new diag test result CSV bulk data entity.
   */
  public DiagTestResultCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }
}

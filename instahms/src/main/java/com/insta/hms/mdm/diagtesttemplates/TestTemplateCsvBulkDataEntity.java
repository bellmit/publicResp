package com.insta.hms.mdm.diagtesttemplates;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component("testTemplateCSVEntity")
public class TestTemplateCsvBulkDataEntity extends CsVBulkDataEntity {
  private static final String[] KEYS = new String[] { "test_id" };

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] { "test_name", "ddept_name", "format_name" };

  /**
   * The Constant MASTERS with fields referencedField, referencedTable, referencedTablePK and
   * referencedTableNameField.
   */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("test_name", "diagnostics", "test_id", "test_id"),
      new BulkDataMasterEntity("ddept_name", "diagnostics_departments", "ddept_id", "ddept_name"),
      new BulkDataMasterEntity("format_name", "test_format", "testformat_id", "format_name"), };

  /**
   * Instantiates a new stores CSV bulk data entity.
   */
  public TestTemplateCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }
  /*
   * private static final String TEST_TEMPLATES =
   * "SELECT d.test_name, dd.ddept_name, tf.format_name " + "FROM test_template_master trm " +
   * "JOIN diagnostics d using(test_id) " + "JOIN diagnostics_departments dd using(ddept_id) " +
   * "JOIN test_format tf on(trm.format_name = tf.testformat_id) " + "order by test_id";
   */
}

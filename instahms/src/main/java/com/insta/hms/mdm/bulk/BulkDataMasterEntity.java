package com.insta.hms.mdm.bulk;

/**
 * The Class BulkDataMasterEntity for maintaining foreign key references for import and export
 * functionality of BulkDate.
 * 
 * @author tanmay.k
 */
public class BulkDataMasterEntity {

  /** The displayed field. */
  private String displayedField;

  /** The referenced table. */
  private String referencedTable;

  /** The referenced table primary key field. */
  private String referencedTablePrimaryKeyField;

  /** The referenced table name field. */
  private String referencedTableNameField;

  /**
   * Instantiates a new bulk data master entity.
   *
   * @param displayedField
   *          the displayed field
   * @param referencedTable
   *          the referenced table
   * @param referencedTablePrimaryKeyField
   *          the referenced table primary key field
   * @param referencedTableNameField
   *          the referenced table name field
   */
  public BulkDataMasterEntity(String displayedField, String referencedTable,
      String referencedTablePrimaryKeyField, String referencedTableNameField) {
    this.displayedField = displayedField;
    this.referencedTable = referencedTable;
    this.referencedTablePrimaryKeyField = referencedTablePrimaryKeyField;
    this.referencedTableNameField = referencedTableNameField;
  }

  /**
   * Gets the displayed field.
   *
   * @return the displayed field
   */
  public String getDisplayedField() {
    return displayedField;
  }

  /**
   * Gets the referenced table.
   *
   * @return the referenced table
   */
  public String getReferencedTable() {
    return referencedTable;
  }

  /**
   * Gets the referenced table primary key field.
   *
   * @return the referenced table primary key field
   */
  public String getReferencedTablePrimaryKeyField() {
    return referencedTablePrimaryKeyField;
  }

  /**
   * Gets the referenced table name field.
   *
   * @return the referenced table name field
   */
  public String getReferencedTableNameField() {
    return referencedTableNameField;
  }

}

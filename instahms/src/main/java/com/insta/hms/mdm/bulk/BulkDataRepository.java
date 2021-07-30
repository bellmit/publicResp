package com.insta.hms.mdm.bulk;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import java.util.List;
import java.util.Map;

/**
 * The Class BulkDataRepository for all bulk database operations.
 *
 * @author tanmay.k
 * @param <PKT>
 *          the generic type
 */
public class BulkDataRepository<PKT> extends MasterRepository<PKT> {

  /**
   * Instantiates a new bulk data repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   */
  public BulkDataRepository(String table, String keyColumn) {
    super(table, keyColumn);
  }

  /**
   * Instantiates a new bulk data repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   * @param uniqueNameColumn
   *          the unique name column
   */
  public BulkDataRepository(String table, String keyColumn, String uniqueNameColumn) {
    super(table, keyColumn, uniqueNameColumn);
  }

  /**
   * Instantiates a new bulk data repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   * @param uniqueNameColumn
   *          the unique name column
   * @param lookupFields
   *          the lookup fields
   */
  public BulkDataRepository(String table, String keyColumn, String uniqueNameColumn,
      String[] lookupFields) {
    super(table, keyColumn, uniqueNameColumn, lookupFields);
  }

  /**
   * Export data.
   *
   * @param csvEntity
   *          the csv entity
   * @return the list
   */
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    String exportQuery = getExportQuery(csvEntity);
    return DatabaseHelper.queryWithCustomMapper(exportQuery, new CsVEntityMapper());

  }
  
  /**
   * Export data with filter value.
   *
   * @param csvEntity
   *          the csv entity
   * @param values
   *          the values of some condition
   * @return the list
   */
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity, Object[] values) {
    String exportQuery = getExportQuery(csvEntity);
    return DatabaseHelper.queryWithCustomMapper(exportQuery, values, new CsVEntityMapper());

  }

  /**
   * Import data.
   *
   * @param csvEntity
   *          the csv entity
   * @return the int[]
   */
  public int[] importData(CsVBulkDataEntity csvEntity) {
    return null;
  }

  /**
   * Gets the export query.
   *
   * @param csvEntity
   *          the csv entity
   * @return the export query
   */
  private String getExportQuery(CsVBulkDataEntity csvEntity) {
    Map<String, BulkDataMasterEntity> mastersWithFieldMap = csvEntity
        .getReferencedMastersWithFieldAsKeyMap();
    List<String> filters = csvEntity.getFilters();
    String tableName = super.getTable();

    StringBuilder query = new StringBuilder();
    query.append("SELECT");

    if (csvEntity.getFields().isEmpty()) {
      query.append(" *");
    } else {
      boolean first = true;
      for (String field : csvEntity.getAllFields()) {
        query.append(first ? " " : ",");

        BulkDataMasterEntity master = mastersWithFieldMap.get(field);
        if (null != master) {
          query.append(master.getReferencedTable()).append(".")
              .append(master.getReferencedTableNameField());
        } else {
          query.append(tableName).append(".").append(field);
        }

        Map<String, String> nameToAliasMap = csvEntity.getNameToAlias();
        String alias = nameToAliasMap.get(field);
        if (null != alias) {
          query.append(" AS ").append(alias);
        }
        first = false;
      }
    }

    query.append("\n FROM ").append(tableName);
    for (BulkDataMasterEntity master : csvEntity.getMasters()) {
      query.append("\n LEFT JOIN ").append(master.getReferencedTable()).append(" ON ")
          .append(tableName).append(".").append(master.getDisplayedField()).append(" = ")
          .append(master.getReferencedTable()).append(".")
          .append(master.getReferencedTablePrimaryKeyField());
    }

    if (!csvEntity.getFilters().isEmpty()) {
      boolean first = true;
      for (String filter : filters) {
        query.append(first ? "\n WHERE " : " AND ");
        query.append(filter);
        first = false;
      }
    }

    List<String> keys = csvEntity.getKeys();
    if (!keys.isEmpty()) {
      query.append("\n ORDER BY");
      boolean first = true;
      for (String key : keys) {
        query.append(first ? " " : ", ");
        query.append(tableName).append(".").append(key);
        first = false;
      }
    }

    return query.toString();
  }
}

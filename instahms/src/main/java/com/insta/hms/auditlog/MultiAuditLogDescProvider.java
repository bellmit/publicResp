/**
 *
 */

package com.insta.hms.auditlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The Class MultiAuditLogDescProvider. A Generic Class that provides field description given any
 * table.
 * 
 * @author anupama
 * 
 */
public class MultiAuditLogDescProvider extends BasicAuditLogDescProvider {

  static Logger logger = LoggerFactory.getLogger(MultiAuditLogDescProvider.class);

  private String[] auditTables = null;

  public MultiAuditLogDescProvider(String[] tableNames) {
    super(false);
    this.auditTables = tableNames;
  }

  protected String getFieldName(Map columnMap) {
    String tableName = "";
    String columnName = "";

    if (columnMap.containsKey("COLUMN_NAME")) {
      columnName = (String) columnMap.get("COLUMN_NAME");
      if (columnMap.containsKey("TABLE_NAME")) {
        tableName = (String) columnMap.get("TABLE_NAME");
      }
      return (null != tableName) ? (tableName + "_" + columnName) : columnName;
    }
    return null;
  }

  protected String getBaseTableList(String tableName) {

    StringBuilder tableListBuilder = new StringBuilder();
    for (int i = 0; i < auditTables.length; i++) {
      if (i > 0) {
        tableListBuilder.append(", ");
      }
      tableListBuilder.append("'").append(guessBaseTableName(auditTables[i])).append("'");
    }
    return tableListBuilder.toString();
  }
}

/**
 *
 */

package com.insta.hms.auditlog;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The Class BasicAuditLogDescProvider.
 * A Generic Class that provides field description given any table
 * @author anupama
 */
public class BasicAuditLogDescProvider implements AuditLogDescProvider {

  static Logger logger = LoggerFactory.getLogger(BasicAuditLogDescProvider.class);

  private boolean defaultSearchability = true;

  public BasicAuditLogDescProvider() {
    this(true);
  }

  public BasicAuditLogDescProvider(boolean defaultSearchability) {
    this.defaultSearchability = defaultSearchability;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    if (null == tableName || tableName.trim().length() <= 0) {
      return null;
    }

    String baseTableList = getBaseTableList(tableName);

    List<Map> columnNames = getTableColumnMap(baseTableList);

    // Create the AuditLogDescriptor
    AuditLogDesc desc = new AuditLogDesc(tableName);

    // Load the descriptor with all the fields, by default none are searchable.
    // Format the field names for pretty display names.
    for (Map columnEntry : columnNames) {
      String fieldName = getFieldName(columnEntry);
      String displayName = getDisplayName(columnEntry);
      desc.addField(fieldName, displayName, defaultSearchability);
    }
    return desc;

  }

  protected String getFieldName(Map columnMap) {
    if (null != columnMap && columnMap.containsKey("COLUMN_NAME")) {
      return (String) columnMap.get("COLUMN_NAME");
    }
    return null;
  }

  protected String getDisplayName(Map columnMap) {

    if (null != columnMap && columnMap.containsKey("COLUMN_NAME")) {
      return formatFieldName((String) columnMap.get("COLUMN_NAME"));
    }
    return null;
  }

  protected String getBaseTableList(String tableName) {
    if (null != tableName && tableName.trim().length() > 0) {
      // Guess the name of the base table from the audit log table.
      String baseTableName = new StringBuilder().append("'").append(guessBaseTableName(tableName))
          .append("'").toString();
      return baseTableName;
    }
    return null;
  }

  protected List<Map> getTableColumnMap(String baseTableList) {
    if (null != baseTableList && baseTableList.trim().length() > 0) {
      // Get all the columns from the base table except for user name and modification time
      String query = "SELECT table_name, column_name from information_schema.columns "
          + "WHERE table_schema = (select current_schema()) " + "AND table_name IN ("
          + baseTableList + ")"
          + "AND column_name not in ('username', 'user_name', 'mod_time', 'modtime')";

      List<Map> columnMapList = DataBaseUtil.queryToArrayList(query);
      return columnMapList;
    }
    return null;

  }

  protected String guessBaseTableName(String auditLogTable) {
    String baseTableName = null;
    if (null != auditLogTable && auditLogTable.endsWith("_audit_log")) {
      baseTableName = auditLogTable.substring(0, auditLogTable.lastIndexOf("_audit_log"));
    }
    return baseTableName;
  }

  protected String formatFieldName(String fieldName) {
    if (null == fieldName) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    String[] words = fieldName.split("_");
    // Look for all _ and replace it with space and capatalize the following character.
    for (int i = 0; i < words.length; i++) {
      if (i != 0) {
        sb.append(" ");
      }
      sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
    }
    return sb.toString();
  }
}

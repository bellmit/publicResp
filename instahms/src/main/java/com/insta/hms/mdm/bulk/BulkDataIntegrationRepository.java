package com.insta.hms.mdm.bulk;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class BulkDataIntegrationRepository<PKT> extends BulkDataRepository<PKT> {

  private String integrationKeyColumn;

  /**
   * Instantiates a new bulk data integration repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   * @param integrationKeyColumn
   *          the integration key column
   */
  public BulkDataIntegrationRepository(String table, String keyColumn,
      String integrationKeyColumn) {
    super(table, keyColumn);
    this.integrationKeyColumn = integrationKeyColumn;
    if (this.lookupFields != null && this.lookupFields.length > 0) {
      this.lookupFields = (String[]) ArrayUtils.addAll(this.lookupFields,
          new String[] { integrationKeyColumn });
    }
  }

  /**
   * Instantiates a new bulk data integration repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   * @param integrationKeyColumn
   *          the integration key column
   * @param uniqueNameColumn
   *          the unique name column
   */
  public BulkDataIntegrationRepository(String table, String keyColumn, String integrationKeyColumn,
      String uniqueNameColumn) {
    super(table, keyColumn, uniqueNameColumn);
    this.integrationKeyColumn = integrationKeyColumn;
    if (this.lookupFields != null && this.lookupFields.length > 0) {
      this.lookupFields = (String[]) ArrayUtils.addAll(this.lookupFields,
          new String[] { integrationKeyColumn });
    }
  }

  /**
   * Instantiates a new bulk data integration repository.
   *
   * @param table
   *          the table
   * @param keyColumn
   *          the key column
   * @param integrationKeyColumn
   *          the integration key column
   * @param uniqueNameColumn
   *          the unique name column
   * @param lookupFields
   *          the lookup fields
   */
  public BulkDataIntegrationRepository(String table, String keyColumn, String integrationKeyColumn,
      String uniqueNameColumn, String[] lookupFields) {
    super(table, keyColumn, uniqueNameColumn, lookupFields);
    this.integrationKeyColumn = integrationKeyColumn;
    if (this.lookupFields != null && this.lookupFields.length > 0
        && (lookupFields == null || lookupFields.length == 0)) {
      this.lookupFields = (String[]) ArrayUtils.addAll(this.lookupFields,
          new String[] { integrationKeyColumn });
    }
  }

  public String getIntegrationKeyColumn() {
    return integrationKeyColumn;
  }

  public BasicDynaBean findByIntegrationId(Object integrationId) {
    return findByKey(getIntegrationKeyColumn(), integrationId);
  }

  @Override
  public boolean isDuplicate(BasicDynaBean bean) {

    String[] nameColumn = getUniqueNameColumn();
    String keyColumn = getIntegrationKeyColumn();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    boolean duplicate = false;
    for (String name : nameColumn) {
      filterMap.put(name, bean.get(name));
    }
    if (null != nameColumn && null != keyColumn) {
      BasicDynaBean found = findByKey(filterMap);
      if (null != found && null != found.get(keyColumn)
          && !found.get(keyColumn).equals(bean.get(keyColumn))) {
        duplicate = true;
      }
    }

    return duplicate;
  }

}

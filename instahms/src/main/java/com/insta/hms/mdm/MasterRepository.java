package com.insta.hms.mdm;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.DuplicateEntityException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.ParameterizedType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterRepository.
 *
 * @param <PrimaryKeyT> the generic type
 */
public abstract class MasterRepository<PrimaryKeyT> extends GenericRepository {

  /** The key column. */
  private String keyColumn;
  
  /** The unique name column. */
  private String[] uniqueNameColumn = null;
  
  /** The lookup fields. */
  protected String[] lookupFields;
  
  /** The status field. */
  private String statusField = "status";
  
  /** The active status. */
  private String activeStatus = "A";
  
  /** The inactive status. */
  private String inactiveStatus = "I";
  
  /** The pk type. */
  private Class<PrimaryKeyT> pkType;

  /** The sqlType of the primary key. */
  private int pkSqltype = Types.INTEGER;

  /** The default search query. */
  private String defaultSearchQuery = "FROM (SELECT * FROM $$table_name$$) AS FOO";

  /**
   * Instantiates a new master repository.
   *
   * @param table the table
   * @param keyColumn the key column
   */
  public MasterRepository(String table, String keyColumn) {
    this(table, keyColumn, keyColumn);
  }

  /**
   * Instantiates a new master repository.
   *
   * @param table the table
   * @param keyColumn the key column
   * @param uniqueNameColumn the unique name column
   */
  public MasterRepository(String table, String keyColumn, String uniqueNameColumn) {
    this(table, keyColumn, uniqueNameColumn, new String[] { keyColumn, uniqueNameColumn });
  }

  /**
   * Instantiates a new master repository.
   *
   * @param table the table
   * @param keyColumn the key column
   * @param uniqueNameColumn the unique name column
   * @param lookupFields the lookup fields
   */
  public MasterRepository(String table, String keyColumn, String uniqueNameColumn,
      String[] lookupFields) {
    this((null != uniqueNameColumn ? new String[] { uniqueNameColumn } : null), lookupFields, table,
        keyColumn);
  }

  /**
   * Instantiates a new master repository.
   *
   * @param uniqueNameColumn the unique name column
   * @param lookupFields the lookup fields
   * @param table the table
   * @param keyColumn the key column
   */
  public MasterRepository(String[] uniqueNameColumn, String[] lookupFields, String table,
      String keyColumn) {
    super(table);
    this.keyColumn = keyColumn;
    this.uniqueNameColumn = (null == uniqueNameColumn) ? new String[] { keyColumn }
        : uniqueNameColumn;
    this.lookupFields = (null == lookupFields) ? merge(this.keyColumn, this.uniqueNameColumn)
        : lookupFields;
    pkType = (Class<PrimaryKeyT>) ((ParameterizedType) this.getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  /**
   * Merge.
   *
   * @param keyColumn2 the key column 2
   * @param uniqueNameColumn2 the unique name column 2
   * @return the string[]
   */
  private String[] merge(String keyColumn2, String[] uniqueNameColumn2) {
    List<String> mergedList = new ArrayList<String>(Arrays.asList(uniqueNameColumn2));
    mergedList.add(keyColumn2);
    return objectArrayToStringArray(mergedList.toArray());
  }

  /**
   * Throw duplicate entity exception.
   *
   * @param bean
   *          the bean
   */
  protected void throwDuplicateEntityException(BasicDynaBean bean) {
    List<String> duplicateValue = new ArrayList<String>();
    List<String> prettyUniqueName = new ArrayList<String>();
    String[] uniqueNameColumn = getUniqueNameColumn();
    for (String name : uniqueNameColumn) {
      if (null != bean && null != bean.get(name)) {
        duplicateValue.add(String.valueOf(bean.get(name)));
        prettyUniqueName.add(StringUtil.prettyName(name));
      }
    }
    String nameColumns = StringUtil
        .join((String[]) objectArrayToStringArray(prettyUniqueName.toArray()), ",");
    String duplicateValues = StringUtil
        .join((String[]) objectArrayToStringArray(duplicateValue.toArray()), ",");
    throw new DuplicateEntityException(new String[] { nameColumns, duplicateValues });
  }

  /**
   * Object array to string array.
   *
   * @param objectArray the object array
   * @return the string[]
   */
  private String[] objectArrayToStringArray(Object[] objectArray) {
    return Arrays.copyOf(objectArray, objectArray.length, String[].class);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#getNextId()
   */
  @Override
  public Object getNextId() {
    PrimaryKeyT id = null;
    Object pk = null;
    pk = super.getNextId();
    if (null == pk || !(pk instanceof String)) {
      pk = getNextSequence();
    }
    if (null != pk) {
      id = (PrimaryKeyT) ConvertUtils.convert(pk, pkType);
    }
    return id;
  }

  /**
   * Gets the status field.
   *
   * @return the status field
   */
  public String getStatusField() {
    return statusField;
  }

  /**
   * Sets the status field.
   *
   * @param statusField the new status field
   */
  public void setStatusField(String statusField) {
    this.statusField = statusField;
  }

  /**
   * Sets the active status.
   *
   * @param activeStatus the new active status
   */
  public void setActiveStatus(String activeStatus) {
    this.activeStatus = activeStatus;
  }

  /**
   * Sets the inactive status.
   *
   * @param inactiveStatus the new inactive status
   */
  public void setInactiveStatus(String inactiveStatus) {
    this.inactiveStatus = inactiveStatus;
  }

  /**
   * Sets the sql type.
   *
   * @param sqlType the new sql type
   */
  public void setSqlType(int sqlType) {
    this.pkSqltype = sqlType;
  }

  /**
   * Sets the lookup fields.
   *
   * @param lookupFields the new lookup fields
   */
  public void setLookupFields(String[] lookupFields) {
    this.lookupFields = lookupFields;
  }

  /**
   * Gets the active status.
   *
   * @return the active status
   */
  public String getActiveStatus() {
    return activeStatus;
  }

  /**
   * Gets the inactive status.
   *
   * @return the inactive status
   */
  public String getInactiveStatus() {
    return inactiveStatus;
  }

  /**
   * Gets the key column.
   *
   * @return the key column
   */
  public String getKeyColumn() {
    return this.keyColumn;
  }

  /**
   * Gets the unique name column.
   *
   * @return the unique name column
   */
  public String[] getUniqueNameColumn() {
    return this.uniqueNameColumn;
  }

  /**
   * Gets the search query.
   *
   * @return the search query
   */
  public SearchQuery getSearchQuery() {
    return new SearchQuery(defaultSearchQuery.replace("$$table_name$$", this.getTable()));
  }

  /**
   * Gets the view query.
   *
   * @return the view query
   */
  public String getViewQuery() {
    return null;
  }

  /**
   * Gets the sql type.
   *
   * @return the sql type
   */
  public int getSqlType() {
    return pkSqltype;
  }

  /**
   * Gets the lookup fields.
   *
   * @return the lookup fields
   */
  public String[] getLookupFields() {
    return lookupFields;
  }

  /**
   * Find by pk.
   *
   * @param params the params
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params) {
    return findByPk(params, false);
  }

  /**
   * Find by pk.
   *
   * @param params the params
   * @param includeDetails the include details
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params, boolean includeDetails) {
    return findBean(params, includeDetails);
  }

  /**
   * Find bean.
   *
   * @param params the params
   * @param includeDetails the include details
   * @return the basic dyna bean
   */
  private BasicDynaBean findBean(Map params, boolean includeDetails) {

    String column = null;
    Object keyValue = null;
    BasicDynaBean bean = null;

    column = getKeyColumn();
    if (column != null && null != params && params.containsKey(column)) {
      keyValue = (Object) params.get(column);
    }

    PrimaryKeyT id = (PrimaryKeyT) ConvertUtils.convert(keyValue, pkType);

    if (includeDetails) {
      String query = getViewQuery();
      if (null != query) {
        return DatabaseHelper.queryToDynaBean(query, id);
      }
    }
    // If we reach here, it either means include details was false or there is no separate view
    // query
    // In either case, we return the bean from the table.
    return findByKey(column, id);
  }

  /**
   * Checks if is duplicate.
   *
   * @param bean the bean
   * @return true, if is duplicate
   */
  public boolean isDuplicate(BasicDynaBean bean) {

    String[] nameColumn = getUniqueNameColumn();
    String keyColumn = getKeyColumn();
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

  @Override
  public Integer update(BasicDynaBean bean, Map keys) {
    if (!allowsDuplicates() && isDuplicate(bean)) {
      throwDuplicateEntityException(bean);
    }
    return super.update(bean, keys);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#delete(java.lang.String, java.lang.Object)
   */
  @Override
  public Integer delete(String key, Object value) {
    return super.delete(key, value);
  }

  /**
   * Batch soft delete.
   *
   * @param ids the ids
   * @return the int[]
   */
  public int[] batchSoftDelete(String[] ids) {
    if (null != getStatusField() && !getStatusField().isEmpty()) {
      StringBuilder query = new StringBuilder();
      query.append("UPDATE ").append(getTable()).append(" SET ").append(getStatusField());
      query.append("=?");
      query.append(" WHERE ").append(getKeyColumn()).append("=?");
      List<Object[]> listIds = new ArrayList<>();
      int index = 0;
      for (String id : ids) {
        listIds.add(index, new Object[] { getInactiveStatus(), id });
        index++;
      }
      return DatabaseHelper.batchUpdate(query.toString(), listIds,
          new int[] { Types.VARCHAR, getSqlType() });
    }
    return null;// if no status field or hardDelete

  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#insert(org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Integer insert(BasicDynaBean bean) {
    if (!allowsDuplicates() && isDuplicate(bean)) {
      throwDuplicateEntityException(bean);
    }
    return super.insert(bean);
  }

  /**
   * Allows duplicates if uniqueNameColumn array is null or one of the uniqueNameColumns is a
   * primary key.
   *
   * @return true, if successful
   */
  protected boolean allowsDuplicates() {
    List<String> uniqueNameColumnList = new ArrayList<>();
    uniqueNameColumnList = Arrays.asList(getUniqueNameColumn());
    return (null == getUniqueNameColumn()) || uniqueNameColumnList.contains(getKeyColumn());
  }

  /**
   * Gets the lookup query.
   *
   * @return the lookup query
   */
  public String getLookupQuery() {
    String[] columns = (null != lookupFields) ? lookupFields
        : merge(getKeyColumn(), getUniqueNameColumn());
    return "SELECT " + StringUtil.join(columns, ",") + " FROM " + getTable();

  }

  // ====================================CACHED METHOD=============================================
  /**
   * Lookup.
   *
   * @param activeOnly the active only
   * @return the list
   */
  // ======================IF YOU ARE EXTENDING MASTER CACHING REPOSITORY==========================
  public List<BasicDynaBean> lookup(boolean activeOnly) {
    return lookup(activeOnly, null);
  }
  // =============================================================================================
  // =============================================================================================

  /**
   * Lookup.
   *
   * @param activeOnly the active only
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> lookup(boolean activeOnly, Map<String, Object> filterMap) {
    if (activeOnly) {
      String statusField = getStatusField();
      String activeStatus = getActiveStatus();
      activeStatus = (null != activeStatus) ? activeStatus : null;
      statusField = (null != activeStatus) && (null != statusField) ? statusField : null;
      if (null == filterMap) {
        filterMap = new HashMap<>();
      }

      if (null != statusField && null != activeStatus) {
        filterMap.put(statusField, activeStatus);
      }
    }
    return lookup(filterMap, getUniqueNameColumn()[0]);// sort by the first
    // uniqueNameColumn
  }

  /**
   * Lookup.
   *
   * @param filterMap the filter map
   * @param sortField the sort field
   * @return the list
   */
  public List<BasicDynaBean> lookup(Map<String, Object> filterMap, String sortField) {
    List<String> columns = null;
    if (null == lookupFields) {
      columns = Arrays.asList(merge(getKeyColumn(), getUniqueNameColumn()));
    } else {
      columns = Arrays.asList(lookupFields);
    }
    return listAll(columns, filterMap, sortField);
  }

  /**
   * Supports auto id.
   *
   * @return true, if successful
   */
  public boolean supportsAutoId() {
    return true;
  }

  /**
   * Gets the bean name.
   *
   * @return the bean name
   */
  public String getBeanName() {
    return this.getTable();
  }

  /**
   * Get the key column type.
   *
   * @return the pk type
   */
  public Class<PrimaryKeyT> getPkType() {
    return pkType;
  }

  /**
   * Gets the sort column.
   *
   * @return the sort column
   */
  public String getSortColumn() {
    return this.keyColumn;
  }

}

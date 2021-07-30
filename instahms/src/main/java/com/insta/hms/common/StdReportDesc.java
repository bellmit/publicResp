
package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * The Class StdReportDesc.
 */
/*
 * A descriptor class that describes a standard report: - The table/view name, query, or how the
 * query is formed to be used with query params - Fields and their display names, data types etc.
 */
public class StdReportDesc implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportDesc.class);

  /** The title. */
  private String title;

  /** The description. */
  private String description;

  /** The table name. */
  private List<String> tableName; // same as queries, if multiple is there formed as union all.

  /** The query. */
  private List<String> query; // to be used only if tableName is null

  /** The use query as is. */
  // don't add a "SELECT * FROM" wrapper for performance reasons.
  private boolean useQueryAsIs = false;

  /** The skip date filter. */
  // don't add a date filter (when using ${fromDate}) for perf
  private boolean skipDateFilter = false;

  /** The query units. */
  private List<QueryUnit> queryUnits;

  /** The default order. */
  private String defaultOrder;

  /** The date fields. */
  private List<String> dateFields;

  /** The report group. */
  private String reportGroup;

  /** The allow no date. */
  private boolean allowNoDate = false;

  /** The default date. */
  private String defaultDate;

  /** The includes. */
  private List<String> includes;

  /** The fields. */
  private Map<String, Field> fields = new HashMap<String, Field>();

  /** The default show fields. */
  private List<String> defaultShowFields = new ArrayList<String>();

  /** The filter only fields. */
  private Map<String, Field> filterOnlyFields = new HashMap<String, Field>();

  /** The query params. */
  /*
   * Query params can contain the initial set of queryparams if the query has a static where clause
   * which has a ? in it. Otherwise, this is just a temp variable
   */
  private List<String> queryParams = new ArrayList<String>();

  private List<Map<String, String>> fieldsWithExpression = new ArrayList<>();

  /**
   * Instantiates a new std report desc.
   */
  /*
   * Constructors
   */
  public StdReportDesc() {
  }

  /**
   * Instantiates a new std report desc.
   *
   * @param title       the title
   * @param query       the query
   * @param queryParams the query params
   * @param dateField   the date field
   * @param reportGroup the report group
   * @param alowNoDate  the alow no date
   * @param defaultDate the default date
   * @param includes    the includes
   */
  public StdReportDesc(String title, List<String> query, List queryParams, List<String> dateField,
      String reportGroup, boolean alowNoDate, String defaultDate, List<String> includes) {
    this.title = title;
    this.query = query;
    this.queryParams = queryParams;
    setAllowNoDate(alowNoDate);
    this.defaultDate = defaultDate;
    setDateFields(dateField);
    this.reportGroup = reportGroup;
    this.includes = includes;
  }

  /*
   * Accessors
   */

  /**
   * Gets the includes.
   *
   * @return the includes
   */
  public List<String> getIncludes() {
    return includes;
  }

  /**
   * Sets the includes.
   *
   * @param includes the new includes
   */
  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  /**
   * Gets the default show fields.
   *
   * @return the default show fields
   */
  public List<String> getDefaultShowFields() {
    return defaultShowFields;
  }

  /**
   * Sets the default show fields.
   *
   * @param val the new default show fields
   */
  public void setDefaultShowFields(List<String> val) {
    this.defaultShowFields = val;
  }

  /**
   * Adds the default show field.
   *
   * @param val the v
   */
  public void addDefaultShowField(String val) {
    if (defaultShowFields == null) {
      defaultShowFields = new ArrayList<String>();
    }
    defaultShowFields.add(val);
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param val the new description
   */
  public void setDescription(String val) {
    description = val;
  }

  /**
   * Gets the table name.
   *
   * @return the table name
   */
  public List<String> getTableName() {
    return tableName;
  }

  /**
   * Sets the table name.
   *
   * @param val the new table name
   */
  public void setTableName(List<String> val) {
    tableName = val;
  }
  
  /**
   * Gets the fields with expression.
   *
   * @return the fields with expression
   */
  public List<Map<String, String>> getFieldsWithExpression() {
    return fieldsWithExpression;
  }
  
  /**
   * Sets the fields with expression.
   *
   * @param val the value
   */
  public void setFieldsWithExpression(List<Map<String, String>> val) {
    fieldsWithExpression.addAll(val);
  }
  
  /**
   * Gets the query.
   *
   * @return the query
   */
  public List getQuery() {
    return query;
  }

  /**
   * Sets the query.
   *
   * @param val the new query
   */
  public void setQuery(List<String> val) {
    query = val;
  }

  /**
   * Gets the query units.
   *
   * @return the query units
   */
  public List<QueryUnit> getQueryUnits() {
    return this.queryUnits;
  }

  /**
   * Sets the query units.
   *
   * @param qu the new query units
   */
  public void setQueryUnits(List<QueryUnit> qu) {
    this.queryUnits = qu;
  }

  /**
   * Gets the use query as is.
   *
   * @return the use query as is
   */
  public boolean getUseQueryAsIs() {
    return useQueryAsIs;
  }

  /**
   * Sets the use query as is.
   *
   * @param val the new use query as is
   */
  public void setUseQueryAsIs(boolean val) {
    useQueryAsIs = val;
  }

  /**
   * Gets the skip date filter.
   *
   * @return the skip date filter
   */
  public boolean getSkipDateFilter() {
    return skipDateFilter;
  }

  /**
   * Sets the skip date filter.
   *
   * @param val the new skip date filter
   */
  public void setSkipDateFilter(boolean val) {
    skipDateFilter = val;
  }

  /**
   * Gets the filter only fields.
   *
   * @return the filter only fields
   */
  public Map<String, Field> getFilterOnlyFields() {
    return filterOnlyFields;
  }

  /**
   * Sets the filter only fields.
   *
   * @param val the v
   */
  public void setFilterOnlyFields(Map<String, Field> val) {
    this.filterOnlyFields = val;
  }

  /**
   * Gets the query params.
   *
   * @return the query params
   */
  public List getQueryParams() {
    return queryParams;
  }

  /**
   * Sets the query params.
   *
   * @param val the new query params
   */
  public void setQueryParams(List val) {
    queryParams = val;
  }

  /**
   * Gets the fields.
   *
   * @return the fields
   */
  public Map<String, Field> getFields() {
    return fields;
  }

  /**
   * Sets the fields.
   *
   * @param val the v
   */
  public void setFields(Map<String, Field> val) {
    fields = val;
  }

  /**
   * Sets the date fields.
   *
   * @param val the new date fields
   */
  public void setDateFields(List<String> val) {
    dateFields = new ArrayList<String>();
    dateFields.addAll(val);
    // todo: do something about None: doesn't belong here.
    if (dateFields.size() > 0 && allowNoDate) {
      dateFields.add("None");
    }
    if (defaultDate != null && dateFields.indexOf(defaultDate) != -1) {
      int index = dateFields.indexOf(defaultDate);
      dateFields.remove(index);
      dateFields.add(0, defaultDate);
    }
  }

  /**
   * Gets the date fields.
   *
   * @return the date fields
   */
  public List<String> getDateFields() {
    return dateFields;
  }

  /**
   * Gets the default order.
   *
   * @return the default order
   */
  public String getDefaultOrder() {
    return defaultOrder;
  }

  /**
   * Sets the default order.
   *
   * @param val the new default order
   */
  public void setDefaultOrder(String val) {
    defaultOrder = val;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   *
   * @param val the new title
   */
  public void setTitle(String val) {
    title = val;
  }

  /**
   * Gets the report group.
   *
   * @return the report group
   */
  public String getReportGroup() {
    return reportGroup;
  }

  /**
   * Sets the report group.
   *
   * @param val the new report group
   */
  public void setReportGroup(String val) {
    reportGroup = val;
  }

  /**
   * Gets the allow no date.
   *
   * @return the allow no date
   */
  public boolean getAllowNoDate() {
    return allowNoDate;
  }

  /**
   * Sets the allow no date.
   *
   * @param val the new allow no date
   */
  public void setAllowNoDate(boolean val) {
    allowNoDate = val;
  }

  /**
   * Gets the default date.
   *
   * @return the default date
   */
  public String getDefaultDate() {
    return defaultDate;
  }

  /**
   * Sets the default date.
   *
   * @param val the new default date
   */
  public void setDefaultDate(String val) {
    this.defaultDate = val;
  }

  /**
   * Sets the query lines.
   *
   * @param queryLines the new query lines
   */
  public void setQueryLines(List<List<String>> queryLines) {
    query = new ArrayList<String>();
    for (List<String> subQuery : queryLines) {
      StringBuilder queryStrBuf = new StringBuilder(" ");
      for (String queryLine : subQuery) {
        queryStrBuf.append(queryLine).append("\n");
      }
      query.add(queryStrBuf.toString());
    }
  }

  /**
   * Checks if is using query units.
   *
   * @return true, if is using query units
   */
  public boolean isUsingQueryUnits() {
    if (queryUnits != null && !queryUnits.isEmpty()) {
      return true;
    }
    return false;
  }

  /**
   * Adds the field.
   *
   * @param fieldName the field name
   * @param field     the f
   */
  public void addField(String fieldName, Field field) {
    fields.put(fieldName, field);
  }

  /**
   * Adds the field. Useful functions for directly accessing the fields
   *
   * @param fieldName       the field name
   * @param displayName     the display name
   * @param width           the width
   * @param dataType        the data type
   * @param groupable       the groupable
   * @param filterable      the filterable
   * @param allowHorizGroup the allow horiz group
   * @return the std report desc
   */
  public StdReportDesc addField(String fieldName, String displayName, int width, String dataType,
      boolean groupable, boolean filterable, boolean allowHorizGroup) {
    Field field = new Field(displayName, width, dataType, groupable, filterable, allowHorizGroup);
    fields.put(fieldName, field);
    return this;
  }

  /**
   * Adds the field.
   *
   * @param fieldName   the field name
   * @param displayName the display name
   * @param width       the width
   * @param dataType    the data type
   * @param aggFunction the agg function
   * @param decimalType the decimal type
   * @return the std report desc
   */
  public StdReportDesc addField(String fieldName, String displayName, int width, String dataType,
      String aggFunction, String decimalType) {
    Field field = new Field(displayName, width, dataType, aggFunction, decimalType);
    fields.put(fieldName, field);
    return this;
  }

  /**
   * Adds the field.
   *
   * @param fieldName the field name
   * @return the std report desc
   */
  public StdReportDesc addField(String fieldName) {
    Field field = new Field();
    fields.put(fieldName, field);
    return this;
  }

  /**
   * Removes the field.
   *
   * @param key the key
   */
  public void removeField(String key) {
    if (fields.containsKey(key)) {
      fields.remove(key);
    }
  }

  /**
   * Sets the field allowed values.
   *
   * @param field the field
   * @param av    the av
   * @return the std report desc
   */
  public StdReportDesc setFieldAllowedValues(String field, List<String> av) {
    Field flld = fields.get(field);
    flld.setAllowedValues(av);
    return this;
  }

  /**
   * Sets the field allowed values query.
   *
   * @param field the field
   * @param avq   the avq
   * @return the std report desc
   */
  public StdReportDesc setFieldAllowedValuesQuery(String field, String avq) {
    Field fld = fields.get(field);
    fld.setAllowedValuesQuery(avq);
    return this;
  }

  /**
   * Gets the field.
   *
   * @param fieldName the field name
   * @return the field
   */
  public Field getField(String fieldName) {
    Field field = fields.get(fieldName);
    if (field == null) {
      field = filterOnlyFields.get(fieldName);
    }
    return field;
  }

  /**
   * Gets the field entries.
   *
   * @return the field entries
   */
  public Set<Map.Entry<String, Field>> getFieldEntries() {
    return fields.entrySet();
  }

  /**
   * Gets the field names.
   *
   * @return the field names
   */
  public List<String> getFieldNames() {
    return new ArrayList(fields.keySet());
  }

  /**
   * Gets the fields sorted.
   *
   * @return the fields sorted
   */
  public List<Field> getFieldsSorted() {
    List list = new ArrayList(fields.values());
    Collections.sort(list, fieldDisplayNameComparator);
    return list;
  }

  /**
   * Gets the field names sorted.
   *
   * @return the field names sorted
   */
  public List<String> getFieldNamesSorted() {
    List<String> list = getFieldNames();
    Collections.sort(list, displayNameComparator);
    return list;
  }

  /**
   * Gets the all fields.
   *
   * @return the all fields
   */
  public Collection<Field> getAllFields() {
    return fields.values();
  }

  /**
   * Gets the field display name.
   *
   * @param fieldName the field name
   * @return the field display name
   */
  public String getFieldDisplayName(String fieldName) {
    if (fieldName.equals("_count")) {
      return "Count";
    } else {
      return fields.get(fieldName).getDisplayName();
    }
  }

  /**
   * Gets the filter field names.
   *
   * @return the filter field names
   */
  public List<String> getFilterFieldNames() {
    List<String> list = new ArrayList<String>();

    for (Map.Entry<String, Field> f : fields.entrySet()) {
      if (f.getValue().getFilterable()) {
        list.add(f.getKey());
      }
    }
    for (Map.Entry<String, Field> f : filterOnlyFields.entrySet()) {
      if (f.getValue().getFilterable()) {
        list.add(f.getKey());
      }
    }
    return list;
  }

  /**
   * Gets the filter field names sorted.
   *
   * @return the filter field names sorted
   */
  public List<String> getFilterFieldNamesSorted() {
    MultiValueMap mvmap = new MultiValueMap();
    for (Map.Entry<String, Field> f : fields.entrySet()) {
      if (f.getValue().getFilterable()) {
        if (f.getValue().getDisplayName() != null) {
          mvmap.put(f.getValue().getDisplayName(), f.getKey());
        }
      }
    }
    for (Map.Entry<String, Field> f : filterOnlyFields.entrySet()) {
      if (f.getValue().getFilterable()) {
        if (f.getValue().getDisplayName() != null) {
          mvmap.put(f.getValue().getDisplayName(), f.getKey());
        }
      }
    }

    Map<String, String> sortedMap = new TreeMap<String, String>(mvmap);
    ArrayList<List> list = new ArrayList(sortedMap.values());
    List<String> flattenedList = new ArrayList();
    for (List sub : list) {
      if (!sub.isEmpty()) {
        for (int i = 0; i < sub.size(); i++) {
          flattenedList.add((String) sub.get(i));
        }
      }
    }
    return flattenedList;
  }

  /**
   * Gets the group field names.
   *
   * @return the group field names
   */
  public List<String> getGroupFieldNames() {
    List<String> list = new ArrayList<String>();
    for (Map.Entry<String, Field> f : fields.entrySet()) {
      if (f.getValue().getGroupable()) {
        list.add(f.getKey());
      }
    }
    return list;
  }

  /**
   * Gets the aggregate field names.
   *
   * @return the aggregate field names
   */
  public List<String> getAggregateFieldNames() {
    List<String> list = new ArrayList<String>();
    for (Map.Entry<String, Field> f : fields.entrySet()) {
      if (f.getValue().getAggFunction() != null) {
        list.add(f.getKey());
      }
    }
    return list;
  }

  /**
   * Gets the fixed group field names. List of groupable field names that have a fixed set of values
   * that can be determined upfront: only these are allowe to go on the horizontal axis for summary
   * reports
   *
   * @return the fixed group field names
   */
  public List<String> getFixedGroupFieldNames() {
    List<String> list = new ArrayList<String>();
    for (Map.Entry<String, Field> e : fields.entrySet()) {
      Field field = e.getValue();
      if (field.getGroupable() && field.getAllowHorizGroup()) {
        if (field.getAllowedValues().isEmpty() && field.getAllowedValuesQuery() == null) {
          continue;
        } else {
          list.add(e.getKey());
        }
      }
    }
    return list;
  }

  /** The field display name comparator. */
  public Comparator<Field> fieldDisplayNameComparator = new Comparator<Field>() {
    public int compare(Field fielda, Field fieldb) {
      if (fielda == null && fieldb == null) {
        return 0;
      }

      if (fielda != null && fieldb != null && fielda.getDisplayName() == null
          && fieldb.getDisplayName() == null) {
        return 0;
      }

      if (fielda == null || fielda.getDisplayName() == null) {
        return -1;
      }
      if (fieldb == null || fieldb.getDisplayName() == null) {
        return 1;
      }
      return (fielda.getDisplayName().compareTo(fieldb.getDisplayName()));
    }
  };

  /** The display name comparator. */
  public Comparator<String> displayNameComparator = new Comparator<String>() {
    public int compare(String strA, String strB) {
      if (strA == null && strB == null) {
        return 0;
      }

      if (strA != null && strB != null && getFieldDisplayName(strA) == null
          && getFieldDisplayName(strB) == null) {
        return 0;
      }

      if (strA == null || getFieldDisplayName(strA) == null) {
        return -1;
      }
      if (strB == null || getFieldDisplayName(strB) == null) {
        return 1;
      }
      return (getFieldDisplayName(strA).compareToIgnoreCase(getFieldDisplayName(strB)));
    }
  };

  /**
   * Validate.
   */
  public void validate() {
    if (title == null) {
      return; // we are an included desc. No need to validate here.
    }

    for (Map.Entry<String, Field> e : fields.entrySet()) {
      e.getValue().validate(e.getKey());
    }

    if (tableName != null && queryUnits != null) {
      throw new IllegalArgumentException("Cannot use both tableName and queryUnit");
    }
    if (query != null && queryUnits != null) {
      throw new IllegalArgumentException("Cannot use both query and queryUnit");
    }
    if (tableName != null && query != null) {
      throw new IllegalArgumentException("Cannot use both tableName and query");
    }

    if (queryUnits == null) {
      return;
    }

    processFieldExpressions();
    List<Map<String, String>> fieldsWithMap = new ArrayList<>();
    for (QueryUnit qu : queryUnits) {
      qu.validate();
      fieldsWithMap.add(qu.getFieldsWithExpression());
    }
    setFieldsWithExpression(fieldsWithMap);

  }

  /**
   * Sets the report includes. Report Includes Processing- to handle includes, fields overrides,
   * query desciptors etc.
   *
   * @param incDescs the inc descs
   * @param incNames the inc names
   * @throws Exception the exception
   */
  public void setReportIncludes(List<StdReportDesc> incDescs, List<String> incNames)
      throws Exception {
    /*
     * Merge the fields first: this is common for queryUnit or table/query usage.
     */
    Map<String, Field> mergedFields = new HashMap<String, Field>();
    // add all fields from includes to the desc. User reverse order since we want the first
    // include (eg BillDetailFields) to override the later includes (eg PatientVisitFields)
    for (int i = incDescs.size() - 1; i >= 0; i--) {
      StdReportDesc desc = incDescs.get(i);
      mergedFields.putAll(desc.getFields());
    }

    // now add our own fields, overriding any included fields.
    for (Map.Entry<String, StdReportDesc.Field> e : this.getFields().entrySet()) {
      StdReportDesc.Field field = e.getValue();
      if (field.getDisplayName() != null && !field.getDisplayName().equals("")) {
        // the field desc in the main srjs can override/remove the one in the includes
        mergedFields.put(e.getKey(), field);
      } else {
        mergedFields.remove(e.getKey());
      }
    }

    // use the merged set as the new set of fields.
    this.setFields(mergedFields);
    log.debug("Total number of fields after include: " + mergedFields.size());

    if (!isUsingQueryUnits()) {
      return;
    }

    for (QueryUnit qu : this.queryUnits) {
      List<String> remainingNames = new ArrayList<String>(incNames);
      List<StdReportDesc> remainingDescs = new ArrayList<StdReportDesc>(incDescs);

      List<JoinTable> mergedJts = new ArrayList<JoinTable>();
      for (JoinTable jt : qu.getJoinTables()) {
        if (jt.getIncludeName() == null) {
          mergedJts.add(jt);
        } else {
          // this is a placeholder for the JoinTables from the include file
          int index = remainingNames.indexOf(jt.getIncludeName());
          StdReportDesc desc = remainingDescs.remove(index);
          mergedJts.addAll(desc.getQueryUnits().get(0).getJoinTables());
          remainingNames.remove(index);
        }
      }

      // append the remaining ones at the end.
      for (StdReportDesc desc : remainingDescs) {
        mergedJts.addAll(desc.getQueryUnits().get(0).getJoinTables());
      }
      qu.setJoinTables(mergedJts);
      log.debug("Total number of JoinTables after include: " + mergedJts.size());
    }
  }

  /**
   * Create a separate set of field descriptors for each query unit: in case there are no field
   * overrides, this is based on the regular fields themselves (most often used case when number of
   * query units is 1). When there are field overrides, we let the overrides in each query unit
   * override the global field descriptors.
   * This must be called before generating any queries.
   */
  public void processFieldExpressions() {
    if (this.queryUnits == null) {
      return;
    }
    for (QueryUnit qu : this.queryUnits) {
      qu.processFieldExpressions(this.getFields());
    }
  }

  /**
   * Form a query for list view in reports. This depends on the methodology.
   *
   * @param params the params
   * @return the list result query
   * @throws Exception the exception
   */
  public String getListResultQuery(StdReportParams params) throws Exception {

    StringBuilder query = new StringBuilder();

    String selectFieldsStr;
    List<String> queries;
    boolean useAlias = true;
    Boolean hasDynamicFields = false;
    if (this.query != null && !this.query.isEmpty()) {
      /*
       * SELECT f1,f2,.. FROM (<SELECT query>) as q0 WHERE f1=a [UNION ALL SELECT f1,f2,.. FROM
       * (<SELECT query>) as q1 WHERE f1=a]... (where, the innner SELECTs are the queries
       * themselves)
       */
      queries = this.query;
      selectFieldsStr = getSelectFieldsStr(params);

    } else if (this.tableName != null && !this.tableName.isEmpty()) {
      /*
       * SELECT f1,f2,.. FROM t WHERE f1=a [ UNION ALL SELECT f1,f2,.. FROM t WHERE f1=a ] ...
       * (where each t is one table name)
       */
      queries = this.tableName;
      useAlias = false;
      selectFieldsStr = getSelectFieldsStr(params);

    } else {
      /*
       * SELECT * FROM (SELECT x1 as f1, x2 as f2,.. FROM ...) as q0 WHERE f1=a [UNION ALL SELECT *
       * FROM (SELECT x1 as f1, x2 as f2,.. FROM ...) as q1 WHERE f1=a ] ... (where the inner
       * SELECTs are formed based on the queryUnit and selected fields. Since inner SELECTs are
       * already selective in fields, the second level can be *)
       */
      queries = new ArrayList<String>();
      Set<String> selectFields = getSelectFields(params);
      for (String fieldName : selectFields) {
        hasDynamicFields = hasDynamicFields || this.getField(fieldName).getFieldUsesPartition();
      }
      for (QueryUnit qu : queryUnits) {
        queries.add(qu.formQuery(selectFields, hasDynamicFields));
      }
      selectFieldsStr = " * ";
    }
    List<String> innerWhereClause = params.getInnerFilterClause();
    for (int i = 0; i < queries.size(); i++) {
      if (i > 0) {
        query.append("\nUNION ALL\n");
      }
      int start = query.length();
      if (this.useQueryAsIs) {
        query.append(queries.get(i));
      } else if (useAlias) {
        query.append("SELECT ").append(selectFieldsStr).append(" FROM (\n").append(queries.get(i));
        if (!innerWhereClause.isEmpty() && !innerWhereClause.get(i).isEmpty()) {
          if (queries.get(i).toLowerCase().contains("where")) {
            query.append(" AND "); 
          } else {
            query.append(" WHERE");
          }
          query.append(innerWhereClause.get(i));
        }
        query.append("\n) AS q" + i);
      } else {
        query.append("SELECT ").append(selectFieldsStr).append(" FROM ").append(queries.get(i));
      }
      replacePreFilter(query, queryParams, params, start); // has to happen BEFORE the main filter
      appendFilter(query, queryParams, params);
      if (Boolean.TRUE.equals(hasDynamicFields) 
          && query.indexOf("row_number_for_partition") != -1) {
        if (!params.getFilterValues().isEmpty()) {
          query.append(" AND ");
        } else {
          query.append(" WHERE ");
        }
        query.append("row_number_for_partition = 1");
      }
    }

    // append any order-by for the groups, and include the defaultOrder if any
    List<String> orderList = new ArrayList<String>(params.getListGroups());
    List<String> sortList = new ArrayList<String>();
    for (String ol : orderList) {
      sortList.add("ASC");
    }

    log.debug("Custom Order1: " + params.getCustomOrder1());

    if (params.getCustomOrder1() != null) {
      orderList.add(params.getCustomOrder1());
      if (params.getSort1() != null && params.getSort1().equals("DESC")) {
        sortList.add("DESC");
      } else {
        sortList.add("ASC");
      }
    }
    if (params.getCustomOrder2() != null) {
      orderList.add(params.getCustomOrder2());
      if (params.getSort2() != null && params.getSort2().equals("DESC")) {
        sortList.add("DESC");
      } else {
        sortList.add("ASC");
      }
    }

    if (orderList.isEmpty() && this.getDefaultOrder() != null) {
      orderList.add(this.getDefaultOrder());
      sortList.add("ASC");
    }

    boolean hasOrderExpressions = false;
    for (String fieldName : orderList) {
      if (!getField(fieldName).getAllowedValues().isEmpty()) {
        hasOrderExpressions = true;
      }
    }

    if (hasOrderExpressions && queries.size() > 1) {
      // Cannot use expresions of fields in ORDER BY after UNION. Need an outer select * from.
      query.insert(0, "SELECT * FROM (\n").append("\n) as query");
    }
    appendOrderBy(orderList, sortList, query);
    return query.toString();
  }

  /**
   * Form a query for summary/trend view in reports. This depends on the methodology.
   *
   * @param params the params
   * @return the summary result query
   * @throws Exception the exception
   */
  public String getSummaryResultQuery(StdReportParams params) throws Exception {
    List<String> groupFieldNames = new ArrayList<String>();
    List<String> groupFields = new ArrayList<String>();
    List<String> dataFields = new ArrayList<String>();
    List<String> orderFields = new ArrayList<String>();
    List<String> sortList = new ArrayList<String>();

    /*
     * Collect all field names that we need to group by
     */
    groupFieldNames.add(params.getSumGroupVert());
    if ((params.getSumGroupVertSub() != null) && !params.getSumGroupVertSub().equals("_data")) {
      groupFieldNames.add(params.getSumGroupVertSub());
    }
    if ((params.getSumGroupHoriz() != null) && !params.getSumGroupHoriz().equals("_data")) {
      groupFieldNames.add(params.getSumGroupHoriz());
    }

    for (String fieldName : groupFieldNames) {
      if (fieldName.equals("_period")) {
        String periodField = getPeriodField(params.getSelectedDateField(), params.getTrendType());
        groupFields.add(periodField);
        orderFields.add(periodField);
        sortList.add("ASC");
      } else {
        groupFields.add(fieldName);
        orderFields.add(fieldName);
        sortList.add("ASC");
      }
    }

    for (String fieldName : params.getDisplayFields()) {
      dataFields.add(fieldName);
    }

    boolean append = false;

    StringBuilder query = new StringBuilder();
    /*
     * SELECT: all group fields, and then, all data fields
     */
    query.append("SELECT ");
    for (String fieldName : groupFieldNames) {
      if (append) {
        query.append(", ");
      }
      if (fieldName.equals("_period")) {
        // to_char(date_trunc('day', open_date), 'dd-MM-yyyy') AS _period
        String format = params.getTrendType().equals("month") ? "'Mon yyyy'" : "'dd-MM-yyyy'";
        query.append("to_char(")
            .append(getPeriodField(params.getSelectedDateField(), params.getTrendType()))
            .append(", ").append(format).append(") AS _period");
      } else {
        query.append(fieldName);
      }
      append = true;
    }

    for (String fieldName : dataFields) {
      if (fieldName.equals("_count")) {
        query.append(", ").append("count(*) AS _count");
      } else {
        String func = this.getField(fieldName).getAggFunction();
        String field = DataBaseUtil.quoteIdent(fieldName);
        if (func.equals("wavg")) {
          String weight = this.getField(fieldName).getAggWeight();
          /*
           * Weighted average requires special calculation. Also, it requires the sum of the weight
           * field so that row/grand totals can aggregate this properly. The query should look like:
           * sum(field*weight)/sum(weight) as field, sum(weight) as weight (append weight even if
           * weight is already there in the query: simplifies)
           */
          query.append(", ").append("sum(").append(field).append("*").append(weight).append(")")
              .append("/sum(").append(weight).append(") as ").append(field);

          query.append(", sum(").append(weight).append(") as ").append(weight);

        } else if (func.equals("count distinct")) {
          query.append(", ").append("count(distinct ").append(field).append(") as ").append(field);

        } else {
          query.append(", ").append(func).append("(").append(field).append(") as ").append(field);
        }
      }
    }

    /*
     * FROM : table or query or queryUnit depending on methodology
     */
    List<String> queries;
    boolean useAlias = true;
    Boolean hasDynamicFields = false;

    if (this.query != null && !this.query.isEmpty()) {
      queries = this.query;

    } else if (this.tableName != null && !this.tableName.isEmpty()) {
      queries = this.tableName;
      useAlias = false;

    } else {
      queries = new ArrayList<String>();
      Set<String> selectFields = getSelectFields(params);
      for (String fieldName : selectFields) {
        hasDynamicFields = hasDynamicFields || this.getField(fieldName).getFieldUsesPartition();
      }
      for (QueryUnit qu : this.queryUnits) {
        queries.add(qu.formQuery(selectFields, hasDynamicFields));
      }
    }
    List<String> innerWhereClause = params.getInnerFilterClause();
    if (queries.size() == 1) {
      if (useAlias) {
        /*
         * SELECT g1, sum(f1) FROM (<SELECT f from t...>) AS query WHERE f2=a GROUP BY g1
         */
        query.append(" FROM (\n").append(queries.get(0));
        if (!innerWhereClause.isEmpty() && !innerWhereClause.get(0).isEmpty()) {
          if (queries.get(0).toLowerCase().contains("where")) {
            query.append(" AND "); 
          } else {
            query.append(" WHERE");
          }
          query.append(innerWhereClause.get(0));
        }
        query.append("\n) AS query");
      } else {
        /*
         * SELECT g1, sum(f1) FROM <table> WHERE f2=a GROUP BY g1
         */
        query.append(" FROM ").append(queries.get(0));
      }
      replacePreFilter(query, queryParams, params, 0);
      appendFilter(query, queryParams, params);
    } else {
      // append multiple queries using UNION ALL and then select the required fields
      query.append(" FROM (\n");
      for (int i = 0; i < queries.size(); i++) {
        if (i > 0) {
          query.append("\nUNION ALL\n");
        }
        int start = query.length();
        
        if (useAlias) {
          /*
           * SELECT g1, sum(f1) FROM ( SELECT * FROM (<query>) AS q0 WHERE f2=a [UNION ALL SELECT *
           * FROM (<query>) AS q1 WHERE f2=a] ) as query GROUP BY g1
           */
          query.append("SELECT * FROM (\n").append(queries.get(i));
          if (!innerWhereClause.isEmpty() && !innerWhereClause.get(i).isEmpty()) {
            if (queries.get(i).toLowerCase().contains("where")) {
              query.append(" AND "); 
            } else {
              query.append(" WHERE");
            }
            query.append(innerWhereClause.get(i));
          }
          query.append("\n) as q" + i);
        } else {
          /*
           * SELECT g1, sum(f1) FROM ( SELECT * FROM <table> WHERE f2=a [UNION ALL SELECT * FROM
           * <table> WHERE f2=a] ... ) AS query GROUP BY g1
           */
          query.append("SELECT * FROM ").append(queries.get(i));
        }
        replacePreFilter(query, queryParams, params, start);
        appendFilter(query, queryParams, params);
      }
      query.append("\n) AS query");
    }
    if (Boolean.TRUE.equals(hasDynamicFields) 
        && query.indexOf("row_number_for_partition") != -1) {
      if (!params.getFilterValues().isEmpty()) {
        query.append(" AND ");
      } else {
        query.append(" WHERE ");
      }
      query.append("row_number_for_partition = 1");
    }
    /*
     * GROUP BY: add all group fields
     */
    appendGroupBy(groupFields, query);

    /*
     * ORDER BY: add all group fields
     */
    appendOrderBy(orderFields, sortList, query);
    return query.toString();
  }

  /**
   * Gets the select fields.
   *
   * @param params the params
   * @return the select fields
   */
  /*
   * Return a list of fields that must be there in the SELECT clause. This will include all display
   * fields chosen by the user, and any other that required to be added for calculations such as
   * weighted average and group fields not part of the selected fields.
   */
  private Set<String> getSelectFields(StdReportParams params) {
    Set<String> selectFields = new HashSet<String>();

    // start with all the displayFields that don't start with _
    for (String fieldName : params.getDisplayFields()) {
      if (fieldName.startsWith("_")) {
        continue; // skip _count, _slno etc.
      }

      selectFields.add(fieldName);

      // if it is a weighted average field, add its denominator also
      Field field = getField(fieldName);
      if (field == null) {
        throw new IllegalArgumentException("Invalid field selected: " + fieldName);
      }
      if (field.getAggFunction() != null && field.getAggFunction().equals("wavg")) {
        String weightField = field.getAggWeight();
        selectFields.add(weightField);
      }
    }

    // Add all group fields which are not already added
    for (String grpField : params.getListGroups()) {
      if (grpField.startsWith("_")) {
        continue; // skip _period etc.
      }
      selectFields.add(grpField);
    }

    // The following are really required only for queryUnit method, but for simplicity, add anyway
    List<String> otherFields = new ArrayList<String>();
    otherFields.addAll(params.getFilterFields());
    otherFields.addAll(params.getSumGroupFields());
    otherFields.addAll(params.getOrderFields());

    for (String otherField : otherFields) {
      if (otherField.startsWith("_")) {
        continue;
      }
      if (fields.get(otherField) == null) {
        continue; // it could be a filter-only field
      }
      selectFields.add(otherField);
    }

    if (params.getOrderFields().isEmpty() && (this.defaultOrder != null)) {
      selectFields.add(this.defaultOrder);
    }

    return selectFields;
  }

  // Note: works only for query/table methodology. For queryUnit, use queryUnit.getQuery()
  /**
   * Gets the select fields str.
   *
   * @param params the params
   * @return the select fields str
   */
  // to get proper results.
  private String getSelectFieldsStr(StdReportParams params) {
    StringBuilder selectFieldsStr = new StringBuilder();
    Set<String> selectFieldsList = getSelectFields(params);

    if (selectFieldsList.size() == 0) {
      throw new IllegalArgumentException("No fields selected for query");
    }

    for (String fieldName : selectFieldsList) {
      if (selectFieldsStr.length() > 0) {
        selectFieldsStr.append(", ");
      }
      selectFieldsStr.append(fieldName);
    }
    return selectFieldsStr.toString();
  }

  /**
   * Gets the period field.
   *
   * @param dateField the date field
   * @param trendType the trend type
   * @return the period field
   */
  public static String getPeriodField(String dateField, String trendType) {
    StringBuilder var = new StringBuilder();
    var.append("date_trunc('").append(trendType).append("', ").append(dateField).append(")");
    return var.toString();
  }

  /** The Constant TOKEN_START. */
  public static final String TOKEN_START = "${";

  /** The Constant TOKEN_END. */
  public static final String TOKEN_END = "}";

  /**
   * Replace pre filter. Replace the pre-filter (fully) or fromDate and toDate tokens. We only
   * support these two as these are fairly standard, and a filter on these can be imposed by setting
   * allowNoDate to false.
   *
   * @param query       the query
   * @param queryParams the query params
   * @param params      the params
   * @param start       the start
   */
  public static void replacePreFilter(StringBuilder query, List queryParams, StdReportParams params,
      int start) {

    int tokenStartIndex;
    while ((tokenStartIndex = query.indexOf(TOKEN_START, start)) != -1) {
      int tokenEndIndex = query.indexOf(TOKEN_END, tokenStartIndex + 2);
      if (tokenEndIndex == -1) {
        // no "}" -- invalid situation. Bail out.
        log.error("Token end } not found in query: "
            + query.substring(tokenStartIndex - 5, tokenStartIndex + 5));
        return;
      }
      String token = query.substring(tokenStartIndex, tokenEndIndex + 1);
      log.debug("Found prefilter token: " + token);

      int replaceLen = 0;
      if (token.equalsIgnoreCase("${prefilter}")) {
        query.replace(tokenStartIndex, tokenEndIndex + 1, params.getPreFilter().toString());
        replaceLen = params.getPreFilter().length();
        queryParams.addAll(params.getPreFilterValues());

      } else if (token.equalsIgnoreCase("${fromDate}")) {
        query.replace(tokenStartIndex, tokenEndIndex + 1, "?");
        replaceLen = 1;
        queryParams.add(params.getFromDate());

      } else if (token.equalsIgnoreCase("${toDate}")) {
        query.replace(tokenStartIndex, tokenEndIndex + 1, "?");
        replaceLen = 1;
        queryParams.add(params.getToDate());
      }
      start = tokenStartIndex + replaceLen;
    }
  }

  /**
   * Append filter.
   *
   * @param query       the query
   * @param queryParams the query params
   * @param params      the params
   */
  public static void appendFilter(StringBuilder query, List queryParams, StdReportParams params) {
    if (params.getFilterClause() != null && !params.getFilterClause().equals("")) {
      query.append("\n").append(params.getFilterClause());
      queryParams.addAll(params.getFilterValues());
    }
  }

  /**
   * Append order by.
   *
   * @param fields    the fields
   * @param sortOrder the sort order
   * @param query     the query
   * @return true, if successful
   */
  public boolean appendOrderBy(List<String> fields, List<String> sortOrder, StringBuilder query) {
    List<String> orderFields = new ArrayList<String>();
    int fieldCount = 0;
    for (String fieldName : fields) {
      StringBuilder orderBy = new StringBuilder();
      // need null check since the caller may pass expressions for _period
      if (getField(fieldName) != null && getField(fieldName).getAllowedValues().size() > 0) {
        orderBy.append("CASE");
        int index = 0;
        for (String value : this.getField(fieldName).getAllowedValues()) {
          orderBy.append(" WHEN ").append(fieldName).append("=").append("'").append(value)
              .append("'").append(" THEN ").append(index++);
        }
        if (this.getField(fieldName).getAllowNull()) {
          orderBy.append(" WHEN ").append(fieldName).append(" IS NULL ").append(" THEN ")
              .append(index++);
        }
        orderBy.append(" ELSE ").append(index++);
        orderBy.append(" END ");
      } else {
        orderBy.append(fieldName);
      }
      orderBy.append(" ").append(sortOrder.get(fieldCount++));
      orderFields.add(orderBy.toString());
    }
    return appendBy(orderFields, query, "ORDER BY");
  }

  /**
   * Append by.
   *
   * @param fields   the fields
   * @param query    the query
   * @param byString the by string
   * @return true, if successful
   */
  public static boolean appendBy(List<String> fields, StringBuilder query, String byString) {
    boolean append = false;
    if (fields != null) {
      for (String field : fields) {
        if (!append) {
          query.append("\n").append(byString).append(" ");
        } else {
          query.append(", ");
        }
        query.append(field);
        append = true;
      }
    }
    return append;
  }

  /**
   * Append group by.
   *
   * @param fields the fields
   * @param query  the query
   * @return true, if successful
   */
  public static boolean appendGroupBy(List<String> fields, StringBuilder query) {
    return appendBy(fields, query, "GROUP BY");
  }

  /**
   * Update custom fields.
   *
   * @param customNamesMap the custom names map
   * @return the std report desc
   */
  public StdReportDesc updateCustomFields(Map customNamesMap) {
    List allFieldNames = this.getFieldNames();
    for (int i = 0; i < allFieldNames.size(); i++) {
      String fieldName = (String) allFieldNames.get(i).toString();
      StdReportDesc.Field field = this.getField(fieldName);
      if (customNamesMap.containsKey(fieldName)) {
        if (customNamesMap.get(fieldName) == null || customNamesMap.get(fieldName).equals("")) {
          this.removeField(fieldName);
        } else {
          field.setDisplayName(customNamesMap.get(fieldName).toString());
        }
      }
    }
    return this;
  }

  /*
   **************************************************************************
   * StdReportDesc fields and methods ends here. All inner classes follows.
   **************************************************************************
   */

  /**
   * FieldExpression, used to override a field's table and expression details when multiple query
   * units are present.
   */
  public static class FieldExpression {

    /** The expression. */
    private String expression;

    /** The tables. */
    private ArrayList<String> tables = new ArrayList<String>();

    /**
     * Gets the expression.
     *
     * @return the expression
     */
    public String getExpression() {
      return expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the new expression
     */
    public void setExpression(String expression) {
      this.expression = expression;
    }

    /**
     * Gets the tables.
     *
     * @return the tables
     */
    public ArrayList<String> getTables() {
      return tables;
    }

    /**
     * Sets the tables.
     *
     * @param val the new tables
     */
    public void setTables(ArrayList<String> val) {
      tables = val;
    }

    /**
     * Sets the table.
     *
     * @param table the new table
     */
    public void setTable(String table) {
      StringTokenizer st = new StringTokenizer(table, ",");
      while (st.hasMoreTokens()) {
        this.tables.add(st.nextToken());
      }
    }

    /**
     * Sets the expression lines.
     *
     * @param expressionLines the new expression lines
     */
    public void setExpressionLines(List<String> expressionLines) {
      StringBuilder expStringBuff = new StringBuilder(" ");
      for (String line : expressionLines) {
        expStringBuff.append(line);
      }
      expression = expStringBuff.toString();
    }

    /**
     * Gets the single table.
     *
     * @return the single table
     */
    public String getSingleTable() {
      if (tables.size() == 1) {
        return tables.get(0);
      }
      return null;
    }

    /**
     * Instantiates a new field expression.
     */
    public FieldExpression() {
    }

    /**
     * Instantiates a new field expression.
     *
     * @param tables     the tables
     * @param expression the expression
     */
    public FieldExpression(ArrayList<String> tables, String expression) {
      this.tables = tables;
      this.expression = expression;
    }
  }

  /**
   * Join Table represents the various join hierarchies that composite a query.
   */
  public static class JoinTable {

    /** The name. */
    private String name;

    /** The alias. */
    private String alias;

    /** The type. */
    private String type = "left";

    /** The expression. */
    private String expression;

    /** The include name. */
    private String includeName;

    /** The depends on list. */
    private List<String> dependsOnList = new ArrayList<String>();

    /**
     * Gets the depends on list.
     *
     * @return the depends on list
     */
    public List<String> getDependsOnList() {
      return dependsOnList;
    }

    /**
     * Sets the depends on list.
     *
     * @param dependsOnList the new depends on list
     */
    public void setDependsOnList(List<String> dependsOnList) {
      this.dependsOnList = dependsOnList;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName() {
      return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    void setName(String name) {
      this.name = name;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    String getAlias() {
      return alias;
    }

    /**
     * Sets the alias.
     *
     * @param alias the new alias
     */
    void setAlias(String alias) {
      this.alias = alias;
    }

    /**
     * Gets the expression.
     *
     * @return the expression
     */
    String getExpression() {
      return expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the new expression
     */
    void setExpression(String expression) {
      this.expression = expression;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    String getType() {
      return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    void setType(String type) {
      this.type = type.trim();
    }

    /**
     * Gets the include name.
     *
     * @return the include name
     */
    String getIncludeName() {
      return includeName;
    }

    /**
     * Sets the include name.
     *
     * @param includeName the new include name
     */
    void setIncludeName(String includeName) {
      this.includeName = includeName;
    }

    /**
     * Gets the checks if is include.
     *
     * @return the checks if is include
     */
    boolean getIsInclude() {
      return includeName != null;
    }

    /**
     * Instantiates a new join table.
     */
    public JoinTable() {
    }

    /**
     * Sets the depends on.
     *
     * @param dependsOnStr the new depends on
     */
    void setDependsOn(String dependsOnStr) {
      StringTokenizer st = new StringTokenizer(dependsOnStr, ",");
      while (st.hasMoreTokens()) {
        String depTable = st.nextToken();
        dependsOnList.add(depTable);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return this.getAlias();
    }

    /**
     * Gets the join type string.
     *
     * @return the join type string
     */
    public String getJoinTypeString() {
      if (type == null || type.equals("") || type.equalsIgnoreCase("left")) {
        return "LEFT JOIN";
      } else if (type.equalsIgnoreCase("join") || type.equalsIgnoreCase("inner")) {
        return "JOIN";
      } else if (type.equalsIgnoreCase("right")) {
        return "RIGHT JOIN";
      } else if (type.equalsIgnoreCase("cross")) {
        return "CROSS JOIN";
      } else if (type.equalsIgnoreCase("leftlateral")) {
        return "LEFT JOIN LATERAL";
      } else if (type.equalsIgnoreCase("joinlateral")) {
        return "JOIN LATERAL";
      }
      throw new IllegalArgumentException("Invalid join type: " + type);
    }

  }

  /**
   * The Class Field.
   */
  /*
   * Inner class: Field
   */
  public static class Field extends FieldExpression {

    /** The display name. */
    public String displayName;

    /** The description. */
    public String description;

    /** The width. */
    public int width = 60; // in points

    /** The data type. */
    public String dataType = "string";

    /** The decimal type. */
    public String decimalType = null;

    /** The groupable. */
    public boolean groupable = false;

    /** The filterable. */
    public boolean filterable = false;

    /** The agg function. */
    public String aggFunction;

    /** The agg weight. */
    public String aggWeight;

    /** The allowed values. */
    public List<String> allowedValues = new ArrayList<String>();

    /** The allowed values query. */
    public String allowedValuesQuery;

    /** The allow null. */
    public boolean allowNull = false;

    /** The allow horiz group. */
    public boolean allowHorizGroup = true;

    /** The field uses partition. */
    public boolean fieldUsesPartition = false;

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    /*
     * Accessors
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param val the new display name
     */
    public void setDisplayName(String val) {
      displayName = val;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the description.
     *
     * @param val the new description
     */
    public void setDescription(String val) {
      description = val;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public int getWidth() {
      return width;
    }

    /**
     * Sets the width.
     *
     * @param val the new width
     */
    public void setWidth(int val) {
      width = val;
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public String getDataType() {
      return dataType;
    }

    /**
     * Sets the data type.
     *
     * @param val the new data type
     */
    public void setDataType(String val) {
      if (val.equalsIgnoreCase("integer")) {
        dataType = "numeric";
        decimalType = "integer";
      } else {
        dataType = val;
      }
    }

    /**
     * Gets the decimal type.
     *
     * @return the decimal type
     */
    public String getDecimalType() {
      return decimalType;
    }

    /**
     * Sets the decimal type.
     *
     * @param val the new decimal type
     */
    public void setDecimalType(String val) {
      decimalType = val;
    }

    /**
     * Gets the allowed values.
     *
     * @return the allowed values
     */
    public List<String> getAllowedValues() {
      return allowedValues;
    }

    /**
     * Sets the allowed values.
     *
     * @param val the new allowed values
     */
    public void setAllowedValues(List<String> val) {
      allowedValues = val;
    }

    /**
     * Gets the agg function.
     *
     * @return the agg function
     */
    public String getAggFunction() {
      return aggFunction;
    }

    /**
     * Sets the agg function.
     *
     * @param val the new agg function
     */
    public void setAggFunction(String val) {
      aggFunction = val;
    }

    /**
     * Gets the agg weight.
     *
     * @return the agg weight
     */
    public String getAggWeight() {
      return aggWeight;
    }

    /**
     * Sets the agg weight.
     *
     * @param val the new agg weight
     */
    public void setAggWeight(String val) {
      aggWeight = val;
    }

    /**
     * Gets the allowed values query.
     *
     * @return the allowed values query
     */
    public String getAllowedValuesQuery() {
      return allowedValuesQuery;
    }

    /**
     * Sets the allowed values query.
     *
     * @param val the new allowed values query
     */
    public void setAllowedValuesQuery(String val) {
      allowedValuesQuery = val;
    }

    /**
     * Gets the filterable.
     *
     * @return the filterable
     */
    public boolean getFilterable() {
      return filterable;
    }

    /**
     * Sets the filterable.
     *
     * @param val the new filterable
     */
    public void setFilterable(boolean val) {
      this.filterable = val;
    }

    /**
     * Gets the groupable.
     *
     * @return the groupable
     */
    public boolean getGroupable() {
      return groupable;
    }

    /**
     * Sets the groupable.
     *
     * @param val the new groupable
     */
    public void setGroupable(boolean val) {
      this.groupable = val;
    }

    /**
     * Gets the allow horiz group.
     *
     * @return the allow horiz group
     */
    public boolean getAllowHorizGroup() {
      return allowHorizGroup;
    }

    /**
     * Sets the allow horiz group.
     *
     * @param val the new allow horiz group
     */
    public void setAllowHorizGroup(boolean val) {
      allowHorizGroup = val;
    }

    /**
     * Gets the allow null.
     *
     * @return the allow null
     */
    public boolean getAllowNull() {
      return allowNull;
    }

    /**
     * Sets the allow null.
     *
     * @param val the new allow null
     */
    public void setAllowNull(boolean val) {
      allowNull = val;
    }

    /**
     * Gets the field uses partition.
     *
     * @return the field uses partition
     */
    public boolean getFieldUsesPartition() {
      return fieldUsesPartition;
    }

    /**
     * Sets the field uses partition.
     *
     * @param val the new field uses partition
     */
    public void setFieldUsesPartition(boolean val) {
      fieldUsesPartition = val;
    }

    /**
     * Instantiates a new field.
     */
    /*
     * Constructors
     */
    public Field() {
    }

    /**
     * Instantiates a new field.
     *
     * @param displayName the display name
     * @param width       the width
     * @param dataType    the data type
     * @param aggFunction the agg function
     * @param decimalType the decimal type
     */
    public Field(String displayName, int width, String dataType, String aggFunction,
        String decimalType) {
      this.displayName = displayName;
      this.width = width;
      this.dataType = dataType;
      this.aggFunction = aggFunction;
      this.decimalType = decimalType;
    }

    /**
     * Instantiates a new field.
     *
     * @param displayName     the display name
     * @param width           the width
     * @param dataType        the data type
     * @param groupable       the groupable
     * @param filterable      the filterable
     * @param allowHorizGroup the allow horiz group
     */
    public Field(String displayName, int width, String dataType, boolean groupable,
        boolean filterable, boolean allowHorizGroup) {
      this.displayName = displayName;
      this.width = width;
      this.dataType = dataType;
      this.groupable = groupable;
      this.filterable = filterable;
      this.allowHorizGroup = allowHorizGroup;
    }

    /**
     * Adds the value.
     *
     * @param value the value
     */
    public void addValue(String value) {
      allowedValues.add(value);
    }

    /**
     * Validate.
     *
     * @param fieldName the field name
     */
    public void validate(String fieldName) {
      if (aggFunction != null) {
        if (groupable) {
          throw new IllegalArgumentException(
              "Field " + fieldName + ": Cannot have groupable + aggFunction");
        }
        if (aggFunction.equals("wavg") && aggWeight == null) {
          throw new IllegalArgumentException(
              "Field " + fieldName + ": aggFunction = wavg requires aggWeight");
        }
      }

      if (!dataType.equalsIgnoreCase("string") && !dataType.equalsIgnoreCase("numeric")
          && !dataType.equalsIgnoreCase("date") && !dataType.equalsIgnoreCase("timestamp")
          && !dataType.equalsIgnoreCase("timestampnosecs") 
          && !dataType.equalsIgnoreCase("datewithoutdays")) {
        throw new IllegalArgumentException(
            "Field " + fieldName + ": Invalid data type (" + dataType + ")");
      }
    }
  }

  /**
   * Inner class: QueryUnit Represents the query object and its various constituent clauses.
   *
   */
  public static class QueryUnit {

    /** The join tables. */
    private List<JoinTable> joinTables = new ArrayList<JoinTable>();

    /** The main table name. */
    private String mainTableName;

    /** The main table name lines. */
    private ArrayList<String> mainTableNameLines;

    /** The main table alias. */
    private String mainTableAlias;

    /** The where expression. */
    private String whereExpression;

    /** The where tables. */
    private ArrayList<String> whereTables;

    /** The field expressions. */
    private Map<String, FieldExpression> fieldExpressions = new HashMap<String, FieldExpression>();
    
    /** The fields with expression. */
    private Map<String, String> fieldsWithExpression = new HashMap<String, String>();
    
    /** The dynamic fields partitioned on. */
    private String dynamicFieldsPartitionedOn;

    /** The partition grouping field name. */
    private String partitionGroupingFieldName = "row_number_for_partition";

    /** The join table alias map. */
    // for quick lookup based on the alias: built when joinTables is set.
    private Map<String, JoinTable> joinTableAliasMap = new HashMap<String, JoinTable>();

    /**
     * Gets the main table alias.
     *
     * @return the main table alias
     */
    public String getMainTableAlias() {
      return mainTableAlias;
    }

    /**
     * Sets the main table alias.
     *
     * @param val the new main table alias
     */
    public void setMainTableAlias(String val) {
      this.mainTableAlias = val;
    }

    /**
     * Gets the main table name.
     *
     * @return the main table name
     */
    public String getMainTableName() {
      return mainTableName;
    }

    /**
     * Sets the main table name.
     *
     * @param val the new main table name
     */
    public void setMainTableName(String val) {
      this.mainTableName = val;
    }

    /**
     * Gets the dynamic fields partitioned on.
     *
     * @return the dynamic fields partitioned on
     */
    public String getDynamicFieldsPartitionedOn() {
      return dynamicFieldsPartitionedOn != null && !dynamicFieldsPartitionedOn.isEmpty()
          ? dynamicFieldsPartitionedOn
          : "1";
    }

    /**
     * Sets the dynamic fields partitioned on.
     *
     * @param dynamicFieldsPartitionedOn the new dynamic fields partitioned on
     */
    public void setDynamicFieldsPartitionedOn(String dynamicFieldsPartitionedOn) {
      this.dynamicFieldsPartitionedOn = dynamicFieldsPartitionedOn;
    }

    /**
     * Gets the partition grouping field name.
     *
     * @return the partition grouping field name
     */
    public String getPartitionGroupingFieldName() {
      return partitionGroupingFieldName;
    }

    /**
     * Sets the main table name lines.
     *
     * @param lines the new main table name lines
     */
    public void setMainTableNameLines(List<String> lines) {
      StringBuilder buf = new StringBuilder("");
      for (String line : lines) {
        buf.append(line);
      }
      mainTableName = buf.toString();
    }

    /**
     * Gets the where expression.
     *
     * @return the where expression
     */
    public String getWhereExpression() {
      return whereExpression;
    }

    /**
     * Sets the where expression.
     *
     * @param whereExpression the new where expression
     */
    public void setWhereExpression(String whereExpression) {
      this.whereExpression = whereExpression;
    }

    /**
     * Gets the where tables.
     *
     * @return the where tables
     */
    public ArrayList getWhereTables() {
      return whereTables;
    }

    /**
     * Sets the where tables.
     *
     * @param val the new where tables
     */
    public void setWhereTables(ArrayList val) {
      whereTables = val;
    }

    /**
     * Sets the where table.
     *
     * @param whereTable the new where table
     */
    public void setWhereTable(String whereTable) {
      whereTables = new ArrayList<String>();
      StringTokenizer st = new StringTokenizer(whereTable, ",");
      while (st.hasMoreTokens()) {
        whereTables.add(st.nextToken());
      }
    }

    /**
     * Gets the join tables.
     *
     * @return the join tables
     */
    public List<JoinTable> getJoinTables() {
      return joinTables;
    }

    /**
     * Sets the join tables.
     *
     * @param val the new join tables
     */
    public void setJoinTables(List<JoinTable> val) {
      joinTables = val;
      for (JoinTable jt : joinTables) {
        joinTableAliasMap.put(jt.getAlias(), jt);
      }
    }

    /**
     * Gets the fields with expression.
     *
     * @return the fields with expression
     */
    public Map<String, String> getFieldsWithExpression() {
      return fieldsWithExpression;
    }

    /**
     * Sets the fields with expression.
     *
     * @param val the v
     */
    public void setFieldsWithExpression(Map<String, String> val) {
      fieldsWithExpression = val;
    }


    /**
     * Gets the field expressions.
     *
     * @return the field expressions
     */
    public Map<String, FieldExpression> getFieldExpressions() {
      return fieldExpressions;
    }

    /**
     * Sets the field expressions.
     *
     * @param val the v
     */
    public void setFieldExpressions(Map<String, FieldExpression> val) {
      fieldExpressions = val;
    }

    /**
     * Gets the field expression.
     *
     * @param fieldName the field name
     * @return the field expression
     */
    public FieldExpression getFieldExpression(String fieldName) {
      return fieldExpressions.get(fieldName);
    }

    /**
     * Gets the join table.
     *
     * @param alias the alias
     * @return the join table
     */
    public JoinTable getJoinTable(String alias) {
      return joinTableAliasMap.get(alias);
    }

    /**
     * Update the map of field expressions using the desc's field list. This is only for the field
     * expressions that are not given within the queryUnit, which will override the one given in the
     * field descriptor.
     *
     * @param fields the fields
     */
    public void processFieldExpressions(Map<String, Field> fields) {
      Map<String, String> fieldWithExpMap = new HashMap<>();
      for (Map.Entry<String, Field> e : fields.entrySet()) {
        if (!fieldExpressions.containsKey(e.getKey())) {
          Field field = e.getValue();
          FieldExpression fieldExpr = new FieldExpression(field.getTables(), field.getExpression());
          fieldExpressions.put(e.getKey(), fieldExpr);
          // prepare the fields with expression.
          if (!fieldWithExpMap.containsKey(e.getKey()) 
              && fieldExpr.getExpression() != null 
              && !fieldExpr.getExpression().isEmpty()) {
            fieldWithExpMap.put(e.getKey(), fieldExpr.getExpression());
          }
        } else {
          fieldWithExpMap.put(e.getKey(), fieldExpressions.get(e.getKey()).getExpression());
        }
      }
      setFieldsWithExpression(fieldWithExpMap);
    }

    /**
     * Validate.
     */
    public void validate() {
      for (Map.Entry<String, FieldExpression> e : fieldExpressions.entrySet()) {
        FieldExpression fe = e.getValue();
        // multiple tables requires expression
        if (fe.getTables().size() > 1 && fe.getExpression() == null) {
          throw new IllegalArgumentException(
              "Field " + e.getKey() + ": Multiple table requires expression");
        }

        for (String table : fe.getTables()) {
          // tables referenced by field must be there in jointables
          if (!table.equals(mainTableAlias) && getJoinTable(table) == null) {
            throw new IllegalArgumentException("Field " + e.getKey() + ": Invalid table (" + table
                + ") referenced for " + mainTableAlias);
          }
        }
      }
      for (JoinTable jt : joinTables) {
        // validate that joinTable dependsOn is a valid joinTable
        for (String table : jt.getDependsOnList()) {
          if (!table.equals(mainTableAlias) && getJoinTable(table) == null) {
            throw new IllegalArgumentException("Join " + jt.getName() + ": Invalid table (" + table
                + ") referenced for " + mainTableAlias);
          }
        }
      }
    }

    /**
     * Process the Query Unit to get the resultant SQL Query, which can then be used like a "query"
     * as in the StdReportDesc. In order to construct the query, we need a list of select fields on
     * which the inclusion of join tables will depend.
     *
     * @param selectFields the select fields
     * @return the string
     */
    public String formQuery(Set<String> selectFields) {
      return formQuery(selectFields, false);
    }

    /**
     * Form query.
     *
     * @param selectFields     the select fields
     * @param hasDynamicFields the has dynamic fields
     * @return the string
     */
    public String formQuery(Set<String> selectFields, Boolean hasDynamicFields) {
      StringBuilder query = new StringBuilder();
      StringBuilder selectFieldsStr = getSelectFieldsStr(selectFields);
      if (hasDynamicFields) {
        if (selectFieldsStr.length() > 0) {
          selectFieldsStr.append(", ");
        }
        /*
         * query.append("SELECT ") .append(StringUtils.collectionToDelimitedString(selectFields,
         * ", ")) .append("\nFROM (");
         */
        selectFieldsStr.append("row_number() OVER (PARTITION BY " + getDynamicFieldsPartitionedOn()
            + ") as " + getPartitionGroupingFieldName());
      }
      query.append("SELECT ").append(selectFieldsStr).append("\nFROM ").append(mainTableName)
          .append(" ").append(mainTableAlias);

      HashSet<String> reqdTables = new HashSet<String>();

      for (String fieldName : selectFields) {
        FieldExpression fieldExpr = getFieldExpression(fieldName);
        List<String> fieldReqdTables = fieldExpr.getTables();

        for (String table : fieldReqdTables) {
          addToRequiredTables(reqdTables, table);
        }
      }

      // if there is a static where clause, it can have some tables, which are always reqd.
      if (whereTables != null) {
        for (String table : whereTables) {
          addToRequiredTables(reqdTables, table);
        }
      }

      // Add all required tables' joins to the query. The original order of joins is maintained
      for (JoinTable jt : getJoinTables()) {
        if (!reqdTables.contains(jt.getAlias())) {
          continue; // not required, skip
        }
        query.append("\n  ").append(jt.getJoinTypeString()); // LEFT JOIN
        query.append(" ").append(jt.getName()); // bill
        query.append(" ").append(jt.getAlias()); // b
        if (!jt.getType().equalsIgnoreCase("cross")) {
          query.append(" ");
          query.append(jt.getExpression()); // ON (...)
        }
        query.append(" ");
      }

      if (getWhereExpression() != null) {
        query.append("\nWHERE ");
        query.append(getWhereExpression());
      }
      /*
       * if (hasDynamicFields) { query.append(") partition_foo WHERE " +
       * getPartitionGroupingFieldName() + " = 1"); }
       */

      return query.toString();
    }

    /**
     * Form the string of select expressions given the list of select fields. This is different from
     * StdReportDesc's getSelectFieldsStr because here we depend on the actual expression in each
     * query Unit.
     *
     * @param selectFields the select fields
     * @return the select fields str
     */
    private StringBuilder getSelectFieldsStr(Set<String> selectFields) {
      StringBuilder str = new StringBuilder();

      for (String fieldName : selectFields) {
        if (str.length() > 0) {
          str.append(", ");
        }

        FieldExpression fieldExpr = getFieldExpression(fieldName);
        if (fieldExpr == null) {
          throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }

        if (fieldExpr.getExpression() != null) {
          str.append(fieldExpr.getExpression()).append(" AS ")
              .append(DataBaseUtil.quoteIdent(fieldName));
        } else if (fieldExpr.getSingleTable() != null) {
          str.append(DataBaseUtil.quoteIdent(fieldExpr.getSingleTable() + "." + fieldName));
        } else {
          str.append(DataBaseUtil.quoteIdent(fieldName));
        }
      }
      return str;
    }

    /**
     * Add to the required tables, this table and all its dependencies. Cyclic dependencies are
     * allowed, the entire cycle will be added to the map.
     *
     * @param requiredTables the required tables
     * @param table          the table
     */
    public void addToRequiredTables(Set requiredTables, String table) {

      if (mainTableAlias != null && table.equals(mainTableAlias)) {
        return; // we don't have a joinTable entry, also nothing to do here.
      }

      if (requiredTables.contains(table)) {
        return; // already added, nothing to do. This also means dependents have been added.
      }

      JoinTable jt = this.getJoinTable(table);
      if (jt == null) {
        throw new IllegalArgumentException("Unknown join table alias: '" + table + "'");
      }

      requiredTables.add(table); // add self

      for (String depTable : jt.getDependsOnList()) {
        // add dependents
        addToRequiredTables(requiredTables, depTable);
      }
    }
  }

}
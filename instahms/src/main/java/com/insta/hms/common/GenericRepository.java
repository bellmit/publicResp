package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// TODO: Auto-generated Javadoc
// TODO: Caching
/**
 * The Class GenericRepository.
 *
 * @author tanmay.k
 */
public class GenericRepository {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenericRepository.class);

  /** The table. */
  private String table;
  
  /** The audit log table. */
  private String auditLogTable;

  /** The Constant GENERATE_NEXT_PREFIXED_ID_QUERY. */
  private static final String GENERATE_NEXT_PREFIXED_ID_QUERY = "SELECT ? || nextval(?)";

  /** The default page size. */
  private static Integer DEFAULT_PAGE_SIZE = 25;

  /** The default page number. */
  private static Integer DEFAULT_PAGE_NUM = 1;

  /** The Constant PREFIX_FIELD. */
  private static final String PREFIX_FIELD = "prefix";

  /** The Constant PATTERN_FIELD. */
  private static final String PATTERN_FIELD = "pattern";

  /** The Constant NEXTVAL_FIELD. */
  private static final String NEXTVAL_FIELD = "nextval";

  /** The Constant QUERY_CONTEXT_QUERY. */
  private static final String QUERY_CONTEXT_QUERY = "query";

  /** The Constant QUERY_CONTEXT_PARAMETERS. */
  private static final String QUERY_CONTEXT_PARAMETERS = "parameters";

  /** The cache. */
  private static Map<String, DynaBeanBuilder> CACHE = new HashMap<String, DynaBeanBuilder>();

  /**
   * Instantiates a new GenericRepository.
   *
   * @param tableName the table name
   */
  public GenericRepository(String tableName) {
    this.table = DatabaseHelper.quoteIdent(tableName);
    this.auditLogTable = DatabaseHelper.quoteIdent(tableName + "_audit");
  }

  /**
   * Gets the table.
   *
   * @return the table
   */
  protected String getTable() {
    return table;
  }
  
  /**
   * Gets audit log table.
   * 
   * @return the audit log table
   */
  protected String getAuditLogTable() {
    return auditLogTable;
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public Integer getNextSequence() {
    return DatabaseHelper.getNextSequence(table);
  }

  /**
   * Gets the next prefixed id.
   *
   * @param prefix the prefix
   * @return the next prefixed id
   */
  public String getNextPrefixedId(String prefix) {
    return DatabaseHelper.getString(GENERATE_NEXT_PREFIXED_ID_QUERY, prefix,
        this.table.concat("_seq"));
  }

  /**
   * Gets the next prefixed id.
   *
   * @return the next prefixed id
   */
  protected final Object getNextPrefixedId() {
    BasicDynaBean details = getPrefixPatternAndNextVal();

    if (null == details) {
      return null;
    }

    String prefix = details.get(PREFIX_FIELD) != null ? (String) details.get(PREFIX_FIELD) : table;
    String pattern = details.get(PATTERN_FIELD) != null ? (String) details.get(PATTERN_FIELD)
        : "000000";
    Long sequence = details.get(NEXTVAL_FIELD) != null ? (Long) details.get(NEXTVAL_FIELD) : 0;
    return prependPrefix(prefix, pattern, sequence);
  }

  /**
   * Gets the next id.
   *
   * @return the next id
   */
  public Object getNextId() {
    return getNextPrefixedId();
  }

  /** The get sequence id query. */
  protected static final String GET_SEQUENCE_ID_QUERY = "SELECT nextval(?), prefix, pattern FROM "
      + " unique_number WHERE type_number = ?";

  /**
   * Gets the prefix pattern and next val.
   *
   * @return the prefix pattern and next val
   */
  protected BasicDynaBean getPrefixPatternAndNextVal() {
    return DatabaseHelper.queryToDynaBean(GET_SEQUENCE_ID_QUERY, table + "_seq", table);
  }

  /**
   * Gets the prefix pattern and next val.
   *
   * @param typeNumber the type number
   * @return the prefix pattern and next val
   */
  protected final BasicDynaBean getPrefixPatternAndNextVal(String typeNumber) {
    return DatabaseHelper.queryToDynaBean(GET_SEQUENCE_ID_QUERY, table + "_seq", typeNumber);
  }

  /**
   * Prepend prefix.
   *
   * @param prefix  the prefix
   * @param pattern the pattern
   * @param number  the number
   * @return the string
   */
  public static String prependPrefix(String prefix, String pattern, Long number) {
    DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
    format.applyPattern(prefix + pattern);
    return format.format(number);
  }

  /**
   * Insert a row in database.
   *
   * @param bean the bean
   * @return the number of rows affected
   */
  public Integer insert(BasicDynaBean bean) {
    Map<String, Object> beanMap = bean.getMap();

    Map<String, Object> queryContext = getNamedParametersInsertQueryContext(beanMap);
    MapSqlParameterSource parameters = (MapSqlParameterSource) queryContext
        .get(QUERY_CONTEXT_PARAMETERS);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);

    if (null != query) {
      /*
       * For usage with LinkedHashMap - Collection<Object> values = beanMap.values();
       */
      return DatabaseHelper.insert(query, parameters);
    }

    return 0;
  }
  
  /**
   * Insert a audit log row in database.
   *
   * @param bean the bean
   * @return the number of rows affected
   */
  public Integer insertAuditLog(BasicDynaBean bean) {
    Map<String, Object> beanMap = bean.getMap();
    
    Map<String, Object> queryContext = getNamedParametersInsertAuditLogQueryContext(beanMap);
    MapSqlParameterSource parameters = (MapSqlParameterSource) queryContext
        .get(QUERY_CONTEXT_PARAMETERS);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);

    if (null != query) {
      /*
       * For usage with LinkedHashMap - Collection<Object> values = beanMap.values();
       */
      return DatabaseHelper.insert(query, parameters);
    }

    return 0;
  }

  /**
   * Batch insert in the database.
   *
   * @param beans the beans
   * @return the int[]
   */
  public int[] batchInsert(List<BasicDynaBean> beans) {
    if (beans.isEmpty()) {
      return null;
    }
    SqlParameterSource[] batchParameters = new SqlParameterSource[beans.size()];
    BasicDynaBean prototype = beans.get(0);
    Map<String, Object> queryContext = getNamedParametersInsertQueryContext(prototype.getMap());
    batchParameters[0] = (SqlParameterSource) queryContext.get(QUERY_CONTEXT_PARAMETERS);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);

    if (query != null) {

      for (Integer beanIndex = 1; beanIndex < beans.size(); beanIndex++) {
        BasicDynaBean bean = beans.get(beanIndex);
        batchParameters[beanIndex] = (SqlParameterSource) getParametersSourceMap(bean.getMap());
      }

      return DatabaseHelper.batchInsert(query, batchParameters);
    }

    return new int[0];
  }
  
  /**
   * Batch insert audit log in the database.
   *
   * @param beans the beans
   * @return the int[]
   */
  public int[] batchInsertAuditLogs(List<BasicDynaBean> beans) {
    if (beans.isEmpty()) {
      return null;
    }
    SqlParameterSource[] batchParameters = new SqlParameterSource[beans.size()];
    BasicDynaBean prototype = beans.get(0);
    Map<String, Object> queryContext =
        getNamedParametersInsertAuditLogQueryContext(prototype.getMap());
    batchParameters[0] = (SqlParameterSource) queryContext.get(QUERY_CONTEXT_PARAMETERS);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);

    if (query != null) {
      for (Integer beanIndex = 1; beanIndex < beans.size(); beanIndex++) {
        BasicDynaBean bean = beans.get(beanIndex);
        batchParameters[beanIndex] = (SqlParameterSource) getParametersSourceMap(bean.getMap());
      }

      return DatabaseHelper.batchInsert(query, batchParameters);
    }
    return new int[0];
  }

  /**
   * Gets the insert query.
   *
   * @param beanMap the bean map
   * @return the insert query
   */
  public String getInsertQuery(Map<String, Object> beanMap) {
    StringBuilder query = new StringBuilder();
    Set<String> columns = beanMap.keySet();
    if (columns.isEmpty()) {
      query.append("INSERT INTO ").append(table).append(" default values");
      return query.toString();
    }
    query.append("INSERT INTO ").append(table).append(" (");

    StringBuilder placeholders = new StringBuilder();

    boolean first = true;
    for (String column : columns) {
      if (!first) {
        query.append(", ");
        placeholders.append(", ");
      }
      first = false;
      query.append(DatabaseHelper.quoteIdent(column));
      placeholders.append("?");
    }

    if (first) {
      // No fields supplied, error out
      return null;
    }

    query.append(") VALUES (");
    query.append(placeholders.toString());
    query.append(")");
    return query.toString();
  }

  /**
   * Gets the parameters source map.
   *
   * @param beanMap the bean map
   * @return the parameters source map
   */
  public MapSqlParameterSource getParametersSourceMap(Map<String, Object> beanMap) {
    Set<String> columns = beanMap.keySet();
    MapSqlParameterSource parameters = new MapSqlParameterSource();

    for (String column : columns) {

      parameters.addValue(column, getParameterValue(beanMap.get(column), null));
    }
    return parameters;
  }

  /**
   * Gets the named parameters insert query.
   *
   * @param beanMap the bean map
   * @return the named parameters insert query
   */
  public Map<String, Object> getNamedParametersInsertQueryContext(Map<String, Object> beanMap) {
    return getNamedParametersInsertQueryContext(beanMap, table);
  }
  
  /**
   * Gets the named parameters insert query.
   *
   * @param beanMap the bean map
   * @return the named parameters insert query
   */
  public Map<String, Object> getNamedParametersInsertQueryContext(Map<String, Object> beanMap,
      String table) {
    Map<String, Object> queryContext = new HashMap<String, Object>();
    StringBuilder query = new StringBuilder();
    Set<String> columns = beanMap.keySet();

    if (columns.isEmpty()) {
      query.append("INSERT INTO ").append(table).append(" default values");
      queryContext.put(QUERY_CONTEXT_QUERY, query.toString());
      return queryContext;
    }
    query.append("INSERT INTO ").append(table).append(" (");

    StringBuilder placeholders = new StringBuilder();
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    boolean first = true;

    for (String column : columns) {
      if (!first) {
        query.append(", ");
        placeholders.append(", ");
      }

      first = false;
      placeholders.append(":" + column);

      parameters.addValue(column, getParameterValue(beanMap.get(column), null));
      query.append(DatabaseHelper.quoteIdent(column));
    }

    if (first) {
      // No fields supplied, error out
      return null;
    }

    query.append(") VALUES (");
    query.append(placeholders.toString());
    query.append(")");
    queryContext.put(QUERY_CONTEXT_QUERY, query.toString());

    queryContext.put(QUERY_CONTEXT_PARAMETERS, parameters);
    return queryContext;
  }

  /**
   * Gets the named parameters insert query.
   *
   * @param beanMap the bean map
   * @return the named parameters insert query
   */
  public Map<String, Object> getNamedParametersInsertAuditLogQueryContext(
      Map<String, Object> beanMap) {
    return getNamedParametersInsertQueryContext(beanMap, table + "_audit");
  }
  
  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the number of rows affected
   */
  public Integer update(BasicDynaBean bean, Map<String, Object> keys) {
    Map<String, Object> queryContext = getUpdateQueryContext(bean.getMap(), keys);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);
    if (query != null) {
      return DatabaseHelper.update(query,
          ((List<Object>) queryContext.get(QUERY_CONTEXT_PARAMETERS)).toArray());
    }

    return 0;
  }
  
  /**
   * Update audit log.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the number of rows affected
   */
  public Integer updateAuditLog(BasicDynaBean bean, Map<String, Object> keys) {
    if (bean == null) {
      return 0;
    }
    BasicDynaBean origBean = findByKey(keys);
    ConversionUtils.copyBeanToBean(bean, origBean);
    return insertAuditLog(origBean);
  }
  
  /**
   * Batch update.
   *
   * @param beans the beans
   * @param keys  the keys
   * @return the int[]
   */
  public int[] batchUpdate(List<BasicDynaBean> beans, Map<String, Object> keys) {
    if (beans.isEmpty()) {
      return new int[0];
    }

    SqlParameterSource[] batchParameters = new SqlParameterSource[beans.size()];
    BasicDynaBean prototype = beans.get(0);
    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    String updateQuery = getNamedParametersUpdateQuery(prototype.getMap(), keys, paramSource);

    if (updateQuery == null) {
      return new int[0];
    }

    for (Integer beanIndex = 0; beanIndex < beans.size(); beanIndex++) {
      MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
      Iterator<Map.Entry<String, Object>> keyIterator = keys.entrySet().iterator();

      BasicDynaBean bean = beans.get(beanIndex);
      Iterable<String> columns = bean.getMap().keySet();
      for (String field : columns) {
        String sqlPram = "set_" + field;
        sqlParameterSource.addValue(sqlPram, bean.getMap().get(field));
      }

      while (keyIterator.hasNext()) {
        Map.Entry<String, Object> entry = (Entry<String, Object>) keyIterator.next();
        String fieldName = entry.getKey();
        String queryParameter = "key_" + fieldName;
        ArrayList keyValue = (ArrayList) entry.getValue();
        sqlParameterSource.addValue(queryParameter, keyValue.get(beanIndex));
      }

      batchParameters[beanIndex] = sqlParameterSource;
    }

    return DatabaseHelper.batchUpdate(updateQuery, batchParameters);
  }
  
  /**
   * Batch update.
   *
   * @param query  the query
   * @param values the values
   * @param type   the array of sqltypes
   * @return the int[]
   */
  public int[] batchUpdate(String query, List<Object[]> values, int[] type) {
    if (values.get(0).length == type.length) {
      return DatabaseHelper.batchUpdate(query, values, type);
    } else {
      int[] batchTypes = new int[values.size()];
      Arrays.fill(batchTypes, type[0]);
      return DatabaseHelper.batchDelete(query, values, batchTypes);
    }
  }
  
  /**
   * Batch update audit logs.
   *
   * @param beans the beans
   * @param keys  the keys
   * @return the int[]
   */
  public int[] batchUpdateAuditLog(List<BasicDynaBean> beans, Map<String, Object> keys) {
    if (beans.isEmpty()) {
      return new int[0];
    }

    List<BasicDynaBean> beanList = findByCriteria(keys);

    for (Integer beanIndex = 0; beanIndex < beans.size(); beanIndex++) {
      ConversionUtils.copyBeanToBean(beans.get(beanIndex), beanList.get(beanIndex));
    }
    return batchInsertAuditLogs(beanList);
  }

  /**
   * Gets the update query.
   *
   * @param columnMap the column map
   * @param keys      the keys
   * @return the update query
   */
  public Map<String, Object> getUpdateQueryContext(Map<String, Object> columnMap,
      Map<String, Object> keys) {
    StringBuilder stmt = new StringBuilder();

    stmt.append("UPDATE ").append(table).append(" SET ");

    /*
     * Append a field=?[,...] statement for every field
     */
    boolean first = true;
    List<Object> parameters = new ArrayList<Object>();
    for (Map.Entry<String, Object> entry : columnMap.entrySet()) {
      String fieldName = DatabaseHelper.quoteIdent(entry.getKey());
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append(fieldName).append("=?");

      parameters.add(getParameterValue(entry.getValue(), null));
    }
    if (first) {
      // No fields supplied, error out
      return null;
    }

    /*
     * Append a WHERE clause based on the key, if any
     */
    if (keys != null) {
      stmt.append(" WHERE ");
      boolean firstKey = true;
      for (Map.Entry<String, Object> entry : keys.entrySet()) {
        String fieldName = DatabaseHelper.quoteIdent(entry.getKey());
        if (!firstKey) {
          stmt.append(" AND ");
        } else {
          firstKey = false;
        }
        if (entry.getValue() == null) {
          stmt.append(fieldName).append(" is NULL");
        } else {
          stmt.append(fieldName).append("=?");
          parameters.add(entry.getValue());
        }
      }
    }

    Map<String, Object> queryContext = new HashMap<String, Object>();
    queryContext.put(QUERY_CONTEXT_QUERY, stmt.toString());
    queryContext.put(QUERY_CONTEXT_PARAMETERS, parameters);
    return queryContext;
  }

  /**
   * Gets the named parameters update query.
   *
   * @param columnMap  the column map
   * @param keys       the keys
   * @param parameters the parameters
   * @return the named parameters update query
   */
  public String getNamedParametersUpdateQuery(Map columnMap, Map keys,
      MapSqlParameterSource parameters) {
    Iterable<String> columns = columnMap.keySet();

    StringBuilder stmt = new StringBuilder();
    stmt.append("UPDATE ").append(table).append(" SET ");

    /*
     * Append a field=?[,...] statement for every field
     */
    boolean first = true;
    for (String fieldName : columns) {
      String queryParameter = "set_" + fieldName;
      parameters.addValue(queryParameter, columnMap.get(fieldName));

      fieldName = DatabaseHelper.quoteIdent(fieldName);
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append(fieldName).append("= :" + queryParameter);

    }
    if (first) {
      // No fields supplied, error out
      return null;
    }

    /*
     * Append a WHERE clause based on the key, if any
     */
    if (keys != null) {
      stmt.append(" WHERE ");
      Iterator keyIt = keys.keySet().iterator();
      boolean keyFirst = true;
      while (keyIt.hasNext()) {
        String fieldName = (String) keyIt.next();
        // parameters.addValue(queryParameter, keys.get(fieldName));
        fieldName = DatabaseHelper.quoteIdent(fieldName);
        if (!keyFirst) {
          stmt.append(" AND ");
        }
        keyFirst = false;
        String queryParameter = "key_" + fieldName;
        stmt.append(fieldName).append("= :" + queryParameter);
      }
    }

    return stmt.toString();
  }

  /**
   * Gets the parameter value.
   *
   * @param fieldValue the field value
   * @param type       the type
   * @return the parameter value
   */
  public Object getParameterValue(Object fieldValue, Integer type) {
    if (fieldValue instanceof InputStream) {
      InputStream fieldValueStream = (InputStream) fieldValue;
      try {
        return new SqlParameterValue(Types.BLOB,
            new SqlLobValue(fieldValueStream, fieldValueStream.available()));
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
    return null != type ? new SqlParameterValue(type, fieldValue) : fieldValue;
  }

  /** The delete query. */
  private String deleteQuery = "DELETE FROM %s WHERE %s='%s';";

  /**
   * Delete.
   *
   * @param key   the key
   * @param value the value
   * @return the integer
   */
  public Integer delete(String key, Object value) {
    String query = String.format(deleteQuery, table, DatabaseHelper.quoteIdent(key), value);
    return DatabaseHelper.delete(query);
  }
  
  /**
   * Delete.
   *
   * @param parameters the parameters
   * @return the integer
   */
  public Integer delete(Map<String, Object> parameters) {
    List<Object> parameterValues = new ArrayList<Object>();
    String query = getDeleteQuery(parameters, parameterValues);
    return DatabaseHelper.delete(query, parameterValues.toArray());
  }
  
  /**
   * Delete with support for IN and NOT IN clauses.
   *
   * @param parameters the parameters
   * @param isNotIN    the is not IN
   * @return the integer
   */
  public Integer delete(Map<String, Object> parameters, boolean isNotIN) {
    Map<String, Object> queryContext = getNamedParametersDeleteQuery(parameters, isNotIN);
    return DatabaseHelper.update((String) queryContext.get("query"),
        (MapSqlParameterSource) queryContext.get("paramters"));
  }
  
  /**
   * Delete audit log.
   *
   * @param key   the key
   * @param value the value
   * @return the integer
   */
  public Integer deleteAuditLog(String key, Object value) {
    BasicDynaBean bean = findByKey(key, value);
    return insertAuditLog(bean);
  }

  /** The delete all records query. */
  private String deleteAllQuery = "DELETE FROM %s";

  /** The get number of records query. */
  private String getNumberOfRecordsQuery = "SELECT count(*) FROM %s";

  /**
   * Delete all records.
   *
   * @return the integer
   */
  public Integer deleteAllRecords() {
    String deleteAllQueryWithValues = String.format(deleteAllQuery, table);
    return DatabaseHelper.delete(deleteAllQueryWithValues);
  }

  /** The delete query with placeholder. */
  private String deleteQueryWithPlaceholder = "DELETE FROM %s WHERE %s=?;";

  /**
   * Batch delete.
   *
   * @param key    the key
   * @param values the values
   * @return the int[]
   */
  public int[] batchDelete(String key, List<Object> values) {
    return batchDelete(key, values, null);
  }
  
  /**
   * Batch delete.
   *
   * @param key    the key
   * @param values the values
   * @param type   the type
   * @return the int[]
   */
  public int[] batchDelete(String key, List<Object> values, Integer type) {
    if (values.isEmpty() || key == null || key.isEmpty()) {
      return null;
    }

    String batchDeleteQuery = String.format(deleteQueryWithPlaceholder, table, key, '?');
    List<Object[]> batchParameters = new ArrayList<Object[]>();
    for (Object value : values) {
      batchParameters.add(new Object[] { value });
    }

    if (type == null) {
      return DatabaseHelper.batchDelete(batchDeleteQuery, batchParameters);
    } else {
      int[] batchTypes = new int[values.size()];
      Arrays.fill(batchTypes, type);
      return DatabaseHelper.batchDelete(batchDeleteQuery, batchParameters, batchTypes);
    }

  }
  
  /**
   * Batch log audit log delete.
   *
   * @param key    the key
   * @param values the values
   * @return the int[]
   */
  public int[] batchDeleteAuditLog(String key, List<Object> values) {
    Map<String,Object> map = new HashMap<>();
    map.put(key, values);
    List<BasicDynaBean> beansList = findByCriteria(map);
    return batchInsertAuditLogs(beansList);
  }

  /**
   * Gets the delete query.
   *
   * @param parameters      the parameters
   * @param parameterValues the parameter values
   * @return the delete query
   */
  private String getDeleteQuery(Map<String, Object> parameters, List<Object> parameterValues) {
    StringBuilder query = new StringBuilder();
    query.append("DELETE FROM ").append(table).append(" WHERE ");

    Iterator mapIterator = parameters.entrySet().iterator();
    while (mapIterator.hasNext()) {
      Map.Entry<String, Object> entry = (Entry<String, Object>) mapIterator.next();
      query.append(DatabaseHelper.quoteIdent(entry.getKey())).append("=?");
      parameterValues.add(entry.getValue());
      if (mapIterator.hasNext()) {
        query.append(" AND ");
      }
    }
    return query.toString();
  }

  /**
   * Gets the named parameters delete query.
   *
   * @param parameters the parameters
   * @param isNotIN    the is not IN
   * @return the named parameters delete query
   */
  public Map<String, Object> getNamedParametersDeleteQuery(Map<String, Object> parameters,
      boolean isNotIN) {
    Map<String, Object> queryContext = new HashMap<String, Object>();
    StringBuilder query = new StringBuilder();
    MapSqlParameterSource namedParameters = new MapSqlParameterSource();

    query.append("DELETE FROM ").append(table).append(" WHERE ");
    Iterator<Entry<String, Object>> parametersIterator = parameters.entrySet().iterator();

    while (parametersIterator.hasNext()) {
      Map.Entry<String, Object> parameter = parametersIterator.next();

      String parameterName = parameter.getKey();
      Object parameterValue = parameter.getValue();

      query.append(parameterName);

      if (parameterValue instanceof List) {
        query.append(isNotIN ? " NOT " : " ").append("IN (").append(":").append(parameterName)
            .append(")");
      } else {
        query.append("=:").append(parameterName);
      }

      if (parametersIterator.hasNext()) {
        query.append(" AND ");
      }

      namedParameters.addValue(parameterName, parameterValue);

    }

    queryContext.put("query", query.toString());
    queryContext.put("paramters", namedParameters);
    return queryContext;
  }

  /**
   * Gets the audit log bean.
   *
   * @return the bean
   */
  public BasicDynaBean getAuditLogBean() {
    return getBean(auditLogTable);
  }
  
  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return getBean(table);
  }
  
  private BasicDynaBean getBean(String table) {
    DynaBeanBuilder builder = CACHE.get(table);

    if (builder == null) {
      List<BasicDynaBean> columns = DatabaseHelper.getColumns(table);
      builder = new DynaBeanBuilder();
      for (BasicDynaBean column : columns) {
        String type = (String) column.get("data_type");
        Class typeClass;

        if (type.equalsIgnoreCase("integer")) {
          typeClass = Integer.class;
        } else if (type.equalsIgnoreCase("date")) {
          typeClass = java.sql.Date.class;
        } else if (type.equalsIgnoreCase("smallint")) {
          typeClass = Short.class;
        } else if (type.equalsIgnoreCase("bigint")) {
          typeClass = Long.class;
        } else if (type.equalsIgnoreCase("numeric")) {
          typeClass = BigDecimal.class;
        } else if (type.equalsIgnoreCase("timestamp without time zone")) {
          typeClass = java.sql.Timestamp.class;
        } else if (type.equalsIgnoreCase("timestamp with time zone")) {
          typeClass = java.sql.Timestamp.class;
        } else if (type.equalsIgnoreCase("time without time zone")) {
          typeClass = java.sql.Time.class;
        } else if (type.equalsIgnoreCase("boolean")) {
          typeClass = Boolean.class;
        } else if (type.equalsIgnoreCase("bytea")) {
          typeClass = InputStream.class;
        } else {
          typeClass = String.class;
        }
        builder.add((String) column.get("column_name"), typeClass);
      }
      CACHE.put(table, builder);
    }
    return builder.build(table);
  }

  /**
   * Gets the column type.
   *
   * @param column the column
   * @return the column type
   */
  private Class getColumnType(String column) {

    /*
     * populating cache(if the bean not existing). because this method can be called just after
     * intialization of this class without calling the getBean().
     *
     * ex: GenericDAO dao = new GenericDAO("foo"); dao.getColumnType("column_name");
     *
     */
    BasicDynaBean bean = getBean();
    DynaProperty prop = bean.getDynaClass().getDynaProperty(column);
    return prop.getType();
  }

  /**
   * Check if row exist.
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return true, if successful
   */
  public boolean exist(String keycolumn, Object identifier) {
    return exist(keycolumn, identifier, false);
  }

  /**
   * Check if row exist.
   *
   * @param keycolumn       the keycolumn
   * @param identifier      the identifier
   * @param caseInsensitive the case insensitive
   * @return true, if successful
   */
  public boolean exist(String keycolumn, Object identifier, boolean caseInsensitive) {
    StringBuilder query = new StringBuilder();
    keycolumn = DatabaseHelper.quoteIdent(keycolumn);
    Class columnType = getColumnType(keycolumn);
    List<BasicDynaBean> resultList = new ArrayList<BasicDynaBean>();

    query.append("SELECT " + keycolumn + " FROM ").append(table).append(" WHERE ");
    if (columnType == String.class) {
      query.append(caseInsensitive ? "upper(" : "").append(keycolumn)
          .append(caseInsensitive ? ")" : "").append("=?;");
      resultList = DatabaseHelper.queryToDynaList(query.toString(),
          caseInsensitive ? ((String) identifier).toUpperCase() : identifier);
    } else {
      query.append(keycolumn).append("=?;");
      resultList = DatabaseHelper.queryToDynaList(query.toString(), identifier);
    }

    return !resultList.isEmpty();
  }

  /**
   * Find by key.
   *
   * @param keyColumn  the key column
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put(keyColumn, identifier);
    return findByKey(filterMap);
  }

  /**
   * Find by key.
   *
   * @param filterMap the filter map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    StringBuilder query = new StringBuilder();
    query.append("SELECT * FROM ").append(table).append(" WHERE ");
    Integer filterMapLength = filterMap.size();
    List<Object> parameters = new ArrayList<>();
    Integer parameterCount = 0;
    for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
      if (entry.getValue() != null) {
        query.append(DatabaseHelper.quoteIdent(entry.getKey())).append(" = ? ");
        parameters.add(entry.getValue());
        parameterCount++;
      } else {
        query.append(DatabaseHelper.quoteIdent(entry.getKey())).append(" IS NULL ");
        parameterCount++;
      }

      if (parameterCount < (filterMapLength)) {
        query.append(" AND ");
      }
    }
    query.append(";");

    return DatabaseHelper.queryToDynaBean(query.toString(), parameters.toArray());
  }

  /**
   * Find by criteria.
   *
   * @param filterMap  the filter map
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> findByCriteria(Map<String, Object> filterMap, String sortColumn) {
    StringBuilder query = new StringBuilder();
    query.append("SELECT * FROM ").append(table).append(" WHERE ");
    Integer filterMapLength = filterMap.size();
    // Object[] parameters = new Object[filterMapLength];
    List<Object> parameters = new ArrayList();
    Integer filterIndex = 0;

    for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
      if (entry.getValue() instanceof List && !((List) entry.getValue()).isEmpty()) {
        Boolean first = true;
        query.append(DatabaseHelper.quoteIdent(entry.getKey())).append(" IN (");

        for (Object object : (List) entry.getValue()) {
          if (first) {
            query.append("?");
            first = false;
          } else {
            query.append(",?");
          }
          parameters.add(object);
        }
        query.append(") ");
      } else {
        query.append(DatabaseHelper.quoteIdent(entry.getKey())).append("=?");
        parameters.add(entry.getValue());
      }
      // parameters[parameterIndex] = entry.getValue();
      if (filterIndex < (filterMapLength - 1)) {
        query.append(" AND ");
      }
      filterIndex++;
    }

    if (sortColumn != null && !sortColumn.isEmpty()) {
      query.append(" ORDER BY ").append(sortColumn);
    }

    query.append(";");

    return DatabaseHelper.queryToDynaList(query.toString(), parameters.toArray());
  }

  /**
   * Find by criteria.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> findByCriteria(Map<String, Object> filterMap) {
    return findByCriteria(filterMap, null);
  }

  /**
   * List.
   *
   * @return the paged list
   */
  public PagedList list() {
    return list(null, false, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM);
  }

  /**
   * List.
   *
   * @param sortField   the sort field
   * @param sortReverse the sort reverse
   * @param pageSize    the page size
   * @param pageNum     the page num
   * @return the paged list
   */
  public PagedList list(String sortField, boolean sortReverse, Integer pageSize, Integer pageNum) {
    String selectField = "SELECT *";
    String selectCount = "SELECT count(*)";
    String fromTable = " FROM " + table;

    SearchQueryAssembler qb = new SearchQueryAssembler(selectField, selectCount, fromTable, null,
        sortField, sortReverse, pageSize, pageNum);

    qb.build();
    PagedList list = qb.getMappedPagedList();

    return list;

  }

  /**
   * List.
   *
   * @param parameters the parameters
   * @return the paged list
   */
  public PagedList list(Map<LISTING, Object> parameters) {
    String sortField = (String) parameters.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) parameters.get(LISTING.SORTASC);
    Integer pageSize = (Integer) parameters.get(LISTING.PAGESIZE);
    Integer pageNum = (Integer) parameters.get(LISTING.PAGENUM);

    return list(sortField, sortReverse, pageSize, pageNum);
  }

  /**
   * Search.
   *
   * @param requestParams the request params
   * @return the paged list
   */
  public PagedList search(Map requestParams) {
    return search(requestParams, null, false, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM, null);
  }

  /**
   * Search.
   *
   * @param requestParams the request params
   * @param sortField     the sort field
   * @param sortReverse   the sort reverse
   * @param pageSize      the page size
   * @param pageNum       the page num
   * @param secondarySort the secondary sort
   * @return the paged list
   */
  public PagedList search(Map requestParams, String sortField, boolean sortReverse,
      Integer pageSize, Integer pageNum, String secondarySort) {
    String selectField = "SELECT *";
    String selectCount = "SELECT count(*)";
    String fromTable = " FROM " + table;

    SearchQueryAssembler qb = new SearchQueryAssembler(selectField, selectCount, fromTable, null,
        sortField, sortReverse, pageSize, pageNum);

    if (requestParams != null) {
      qb.addFilterFromParamMap(requestParams);
    }
    if (secondarySort != null && !secondarySort.equals("")) {
      qb.addSecondarySort(secondarySort);
    }
    qb.build();
    PagedList list = qb.getMappedPagedList();
    return list;
  }

  /**
   * Search.
   *
   * @param requestParams the request params
   * @param params        the params
   * @param secondarySort the secondary sort
   * @return the paged list
   * @throws ParseException the parse exception
   */
  public PagedList search(Map requestParams, Map<LISTING, Object> params, String secondarySort)
      throws ParseException {
    String sortField = (String) params.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) params.get(LISTING.SORTASC);
    Integer pageSize = (Integer) params.get(LISTING.PAGESIZE);
    Integer pageNum = (Integer) params.get(LISTING.PAGENUM);

    return search(requestParams, sortField, sortReverse, pageSize, pageNum, secondarySort);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return listAll(Collections.EMPTY_LIST);
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns) {
    return listAll(columns, null, null, null);
  }

  /**
   * List all.
   *
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(String sortColumn) {
    return listAll(Collections.EMPTY_LIST, null, null, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns    the columns
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String sortColumn) {
    return listAll(columns, null, null, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns     the columns
   * @param filterBy    the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return listAll(columns, filterBy, filterValue, null);
  }

  /**
   * List all.
   *
   * @param columns     the columns
   * @param filterBy    the filter by
   * @param filterValue the filter value
   * @param sortColumn  the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (filterBy == null && filterValue == null) {
      filterMap = null;
    } else {
      filterMap.put(filterBy, filterValue);
    }
    return listAll(columns, filterMap, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns    the columns
   * @param filterMap  the filter map
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    boolean shouldFilter = filterMap != null && !filterMap.isEmpty();
    StringBuilder query = new StringBuilder("SELECT ");
    List<Object> filterValues = new ArrayList<Object>();
    if (columns == null || columns.isEmpty()) {
      query.append("*");
    } else {
      boolean first = true;
      for (String column : columns) {
        column = DatabaseHelper.quoteIdent(column);
        if (!first) {
          query.append(", ");
        }
        first = false;
        query.append(column);
      }
    }
    query.append(" FROM ").append(table);

    if (shouldFilter) {

      query.append(" WHERE ");
      Integer filterIndex = 1;
      for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
        if (filterIndex == filterMap.size()) {
          query.append(DatabaseHelper.quoteIdent((String) entry.getKey())).append("=?");
        } else {
          query.append(DatabaseHelper.quoteIdent((String) entry.getKey())).append("=?")
              .append(" AND ");
        }
        filterIndex++;
        filterValues.add(entry.getValue());
      }
    }

    if ((sortColumn != null) && !sortColumn.equals("")) {
      query.append(" ORDER BY " + DatabaseHelper.quoteIdent(sortColumn));
    }

    if (shouldFilter) {
      return DatabaseHelper.queryToDynaList(query.toString(), filterValues.toArray());
    } else {
      return DatabaseHelper.queryToDynaList(query.toString());
    }
  }

  /**
   * Enable trigger.
   *
   * @param triggerName the trigger name
   * @return true, if successful
   */
  public boolean enableTrigger(String triggerName) {
    try {
      DatabaseHelper.enableTrigger(triggerName, this.getTable());
    } catch (DataAccessException exception) {
      logger.error("Trigger enabling failed: ", exception);
      return false;
    }
    return true;
  }

  /**
   * Disable trigger.
   *
   * @param triggerName the trigger name
   * @return true, if successful
   */
  public boolean disableTrigger(String triggerName) {
    try {
      DatabaseHelper.disableTrigger(triggerName, this.getTable());
    } catch (DataAccessException exception) {
      logger.error("Trigger enabling failed: ", exception);
      return false;
    }
    return true;
  }

  /**
   * This is a utility method to return the first row from the table. Very handy where table has
   * only one row of data. [Example generic_preferences]
   * 
   * @return BasicDynaBean
   */
  public BasicDynaBean getRecord() {
    String query = "SELECT * FROM " + table;
    BasicDynaBean resultBean = null;
    resultBean = DatabaseHelper.queryToDynaBean(query);
    return resultBean;
  }

  /**
   * This method to return the query string from ArrayList. It appends all parameters and return
   * string query.
   *
   * @param table   the table
   * @param fields  the fields
   * @param filters the filters
   * @return String
   */
  public String getQueryFromList(String table, List<String> fields, List<String> filters) {
    List<String> queryParts = new ArrayList<String>(Arrays.asList("SELECT"));
    queryParts.add(StringUtils.collectionToDelimitedString(fields, " "));
    queryParts.add("FROM " + table);
    if (!filters.isEmpty()) {
      queryParts.add("WHERE " + StringUtils.collectionToDelimitedString(filters, " "));
    }
    return StringUtils.collectionToDelimitedString(queryParts, " ");
  }

  /**
   * This method checks either batch operation is success or not.
   *
   * @param batch the batch
   * @return success
   */
  public boolean isBatchSuccess(int[] batch) {
    if (batch == null) {
      return false;
    }
    boolean success = true;
    for (int i : batch) {
      success &= (i >= 1);
    }
    return success;
  }

  /** The Constant GET_DATE_AND_TIME. */
  private static final String GET_DATE_AND_TIME =
      "SELECT localtimestamp(0) as timestamp FROM LOCALTIMESTAMP(0)";

  /**
   * Gets the date and time from DB.
   *
   * @return the date and time
   */
  public Timestamp getDateAndTime() {
    return (Timestamp) DatabaseHelper.queryToDynaBean(GET_DATE_AND_TIME).get("timestamp");
  }
  
  /** The Constant GET_VISITS_FOR_MR_NO. */
  private static final String GET_VISITS_FOR_MR_NO = 
      "Select doctor_name, patient_id, reg_date from patient_registration pr "
      + "left join doctors d on (pr.doctor = d.doctor_id) where mr_no = ?";

  /** The Constant GET_VISITS_FOR_MR_NO_FOR_CENTER. */
  private static final String GET_VISITS_FOR_MR_NO_FOR_CENTER = 
      "Select doctor_name, patient_id, reg_date from patient_registration pr "
      + "left join doctors d on (pr.doctor = d.doctor_id) where mr_no = ? AND center_id = ?";

  /**
   * Gets the visits for mr no.
   *
   * @param mrNo the mr no
   * @param visitType the visit type
   * @return the visits for mr no
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Map> getVisitsForMrNo(String mrNo, String visitType) {
   
    /*
    if (visitType.equals("OP")) {
      return ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
          GET_VISITS_FOR_MR_NO + " AND visit_type = 'o' ", new Object[] { mrNo }));
    }
    if (visitType.equals("IP")) {
      return ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
          GET_VISITS_FOR_MR_NO + " AND visit_type = 'i' ", new Object[] { mrNo }));
    }
    if (visitType.equals("OSP")) {
      return ConversionUtils
          .copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(GET_VISITS_FOR_MR_NO
              + " AND visit_type = 'o' AND op_type = 'O' ", new Object[] { mrNo }));
    }
    */
    int centerId =  RequestContext.getCenterId();
    if (centerId == 0) {
      return ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
          GET_VISITS_FOR_MR_NO, new Object[] { mrNo }));
    } else {
      return ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
          GET_VISITS_FOR_MR_NO_FOR_CENTER, new Object[] { mrNo, RequestContext.getCenterId() }));
    }
  }

}
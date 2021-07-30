package com.insta.hms.common;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a quick and easy access to a single table. Using this class you can do a)
 * quick listing of all records in the table, with support for searching, pagination, sorting b)
 * fetch a record by id c) insert a record d) insert a batch of records e) update records f) delete
 * a record.
 * The standard way to use this class would be, say we have a table named footable with two fields
 * foo & bar.
 * GenericDAO dao = new GenericDAO("footable"); //now lets insert some records. Connection con =
 * DataBaseUtil.getConnection(); con.setAutoCommit(false);
 * DynaBean bean = ... //create and populate the bean
 * boolean success = dao.insert(con, bean); if (success) { con.commit(); } else { con.rollback(); }
 * Great!!! No SQL required for insert, updates or listing.
 *
 */
public class GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(GenericDAO.class);

  /** Cache of bean builders. */
  private static Map<String, DynaBeanBuilder> CACHE = new HashMap<String, DynaBeanBuilder>();

  /** The table. */
  private String table;

  /**
   * Instantiates a new generic DAO.
   *
   * @param tablename the tablename
   */
  public GenericDAO(String tablename) {
    this.table = DataBaseUtil.quoteIdent(tablename);
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
   * Gets the next sequence. Get next ID: assumes that the sequence associated with the table is
   * called table_seq
   *
   * @return the next sequence
   * @throws SQLException the SQL exception
   */
  public int getNextSequence() throws SQLException {
    String query = "SELECT nextval(?)";
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      ps.setString(1, table + "_seq");
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the next formatted id. Use the following when there is an entry in unique_numbers for the
   * table. This also assumes that the table name is type_number. prefix and pattern are as per
   * normal conventions. For example, unique_number should have type_number='bill'; prefix='BL';
   * pattern='000000' and this DAO should have been constructed with "bill" as the table name, then,
   * this method returns, for example, BL000012.
   *
   * @return the next formatted id
   * @throws SQLException the SQL exception
   */
  public String getNextFormattedId() throws SQLException {
    return AutoIncrementId.getSequenceId(table + "_seq", table);
  }

  /*
   * almost same as above, but uses a hard-coded prefix instead of using a customizable one from
   * unique_number
   */
  /**
   * Gets the next prefixed id.
   *
   * @param prefix the prefix
   * @return the next prefixed id
   * @throws SQLException the SQL exception
   */
  public String getNextPrefixedId(String prefix) throws SQLException {
    // TODO: use prepared statement.
    String query = "SELECT '" + prefix + "' || nextval('" + table + "_seq')";
    return DataBaseUtil.getStringValueFromDb(query);
  }

  /**
   * Insert a new record into the table as specified by the bean.
   *
   * @param con  the con
   * @param bean the bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insert(Connection con, BasicDynaBean bean) throws SQLException, IOException {
    Set<String> columns = bean.getMap().keySet();
    PreparedStatement ps = null;
    int rows = 0;

    try {
      ps = getInsertStatement(con, columns);
      setParameterValues(ps, bean, null);
      rows = ps.executeUpdate();

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

    return (rows == 1);
  }

  /**
   * Inserts a list of records into the database table.
   *
   * @param con     The connection to be used to insert into the table
   * @param records A list of records maps.
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertAll(Connection con, List<BasicDynaBean> records)
      throws SQLException, IOException {
    if (records.isEmpty()) {
      return false;
    }

    BasicDynaBean prototype = records.get(0);
    Set<String> columns = prototype.getMap().keySet();

    PreparedStatement ps = null;
    int[] rows = new int[0];

    try {
      ps = getInsertStatement(con, columns);

      for (DynaBean bean : records) {
        int pos = 1;
        for (String column : columns) {
          Object value = bean.get(column);
          if (value instanceof java.io.InputStream) {
            java.io.InputStream stream = (java.io.InputStream) value;
            ps.setBinaryStream(pos++, stream, stream.available());
          } else {
            ps.setObject(pos++, value);
          }
        }
        ps.addBatch();
      }

      rows = ps.executeBatch();
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

    return (rows.length == records.size());
  }

  /**
   * Return a list of records as pagedlist. The paged list provides the records as a DynaBean. Two
   * kinds of functions are there: 1. list: lists ALL rows in the table 2. search: Uses a
   * requestParam map to add filters to the selection.
   *
   * @return the paged list
   * @throws SQLException the SQL exception
   */
  public PagedList list() throws SQLException {
    return list(null, false, 25, 1);
  }

  /**
   * Return a list of records as pagedlist. The paged list provides the records as a DynaBean.
   *
   * @param params the params
   * @return the paged list
   * @throws SQLException the SQL exception
   */
  public PagedList list(Map<LISTING, Object> params) throws SQLException {
    String sortField = (String) params.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) params.get(LISTING.SORTASC);
    int pageSize = (Integer) params.get(LISTING.PAGESIZE);
    int pageNum = (Integer) params.get(LISTING.PAGENUM);

    return list(sortField, sortReverse, pageSize, pageNum);
  }

  /**
   * Get a list of records as specified by search initWhere, sorted on sortField, limit the record
   * count to pageSize and show the page as specified by pageNum.
   *
   * @param sortField   the sort field
   * @param sortReverse the sort reverse
   * @param pageSize    the page size
   * @param pageNum     the page num
   * @return the paged list
   * @throws SQLException the SQL exception
   */
  public PagedList list(String sortField, boolean sortReverse, int pageSize, int pageNum)
      throws SQLException {
    String selectField = "SELECT *";
    String selectCount = "SELECT count(*)";
    String fromTable = " FROM " + table;

    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, selectField, selectCount, fromTable, null,
        sortField, sortReverse, pageSize, pageNum);

    qb.build();
    PagedList list = qb.getDynaPagedList();
    qb.close();
    DataBaseUtil.closeConnections(con, null);
    return list;
  }

  /**
   * Search.
   *
   * @param requestParams the request params
   * @return the paged list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList search(Map requestParams) throws SQLException, ParseException {
    return search(requestParams, null, false, 25, 1, null);
  }

  /**
   * Search.
   *
   * @param requestParams the request params
   * @param params        the params
   * @param secondarySort the secondary sort
   * @return the paged list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList search(Map requestParams, Map<LISTING, Object> params, String secondarySort)
      throws SQLException, ParseException {
    String sortField = (String) params.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) params.get(LISTING.SORTASC);
    int pageSize = (Integer) params.get(LISTING.PAGESIZE);
    int pageNum = (Integer) params.get(LISTING.PAGENUM);

    return search(requestParams, sortField, sortReverse, pageSize, pageNum, secondarySort);
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
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList search(Map requestParams, String sortField, boolean sortReverse, int pageSize,
      int pageNum, String secondarySort) throws SQLException, ParseException {

    String selectField = "SELECT *";
    String selectCount = "SELECT count(*)";
    String fromTable = " FROM " + table;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, selectField, selectCount, fromTable, null,
          sortField, sortReverse, pageSize, pageNum);

      if (requestParams != null) {
        qb.addFilterFromParamMap(requestParams);
      }
      if (secondarySort != null && !secondarySort.equals("")) {
        qb.addSecondarySort(secondarySort);
      }
      qb.build();
      PagedList list = qb.getMappedPagedList();
      qb.close();
      return list;

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Update table records with the data specified in the columndata and row identified by keys.
   *
   * @param con        the con
   * @param columndata the columndata
   * @param keys       the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int update(Connection con, Map columndata, Map keys) throws SQLException, IOException {
    return update(con, table, columndata, keys);
  }

  /**
   * Update.
   *
   * @param con        the con
   * @param columns    the columns
   * @param columndata the columndata
   * @param keys       the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int update(Connection con, String[] columns, Map columndata, Map keys)
      throws SQLException, IOException {
    return update(con, table, Arrays.asList(columns), columndata, keys);
  }

  /**
   * Update.
   *
   * @param con        the con
   * @param columns    the columns
   * @param columndata the columndata
   * @param keys       the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int update(Connection con, List<String> columns, Map columndata, Map keys)
      throws SQLException, IOException {
    return update(con, table, columns, columndata, keys);
  }

  /**
   * Update.
   *
   * @param con        the con
   * @param columndata the columndata
   * @param keyName    the key name
   * @param keyValue   the key value
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  public int update(Connection con, Map columndata, String keyName, Object keyValue)
      throws SQLException, IOException {
    Map map = new HashMap();
    map.put(keyName, keyValue);
    return update(con, table, columndata, map);
  }

  /**
   * Update.
   *
   * @param con        the con
   * @param table      the table
   * @param columndata the columndata
   * @param keys       the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected int update(Connection con, String table, Map columndata, Map keys)
      throws SQLException, IOException {
    return update(con, table, columndata.keySet(), columndata, keys);
  }

  /**
   * Update.
   *
   * @param con        the con
   * @param table      the table
   * @param columns    the columns
   * @param columndata the columndata
   * @param keys       the keys
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected int update(Connection con, String table, Iterable<String> columns, Map columndata,
      Map keys) throws SQLException, IOException {

    StringBuilder stmt = new StringBuilder();
    stmt.append("UPDATE ").append(table).append(" SET ");

    /*
     * Append a field=?[,...] statement for every field
     */
    boolean first = true;
    for (String fieldName : columns) {
      fieldName = DataBaseUtil.quoteIdent(fieldName);
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append(fieldName).append("=?");
    }
    if (first) {
      // No fields supplied, error out
      return 0;
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
        fieldName = DataBaseUtil.quoteIdent(fieldName);
        if (!keyFirst) {
          stmt.append(" AND ");
        }
        keyFirst = false;
        stmt.append(fieldName).append("=?");
      }
    }

    PreparedStatement ps = null;
    int rows = 0;

    try {
      ps = con.prepareStatement(stmt.toString());
      int pos = 1;
      /*
       * Do a setXXX for every field in fields as well as every field in keys
       */
      for (String fieldName : columns) {
        Object value = columndata.get(fieldName);
        if (value instanceof java.io.InputStream) {
          java.io.InputStream stream = (java.io.InputStream) value;
          ps.setBinaryStream(pos++, stream, stream.available());
        } else {
          ps.setObject(pos++, value);
        }
      }

      if (keys != null) {
        Iterator keyIt = keys.keySet().iterator();
        while (keyIt.hasNext()) {
          String fieldName = (String) keyIt.next();
          Object value = keys.get(fieldName);
          ps.setObject(pos++, value);
        }
      }
      rows = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return rows;
  }

  /**
   * Update with names. Update with a set of key names, with the value already part of the map
   *
   * @param con        the con
   * @param columndata the columndata
   * @param keyNames   the key names
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int updateWithNames(Connection con, Map columndata, String[] keyNames)
      throws SQLException, IOException {
    Map map = new HashMap();
    for (String key : keyNames) {
      map.put(key, columndata.get(key));
    }
    return update(con, table, columndata, map);
  }

  /**
   * Update with names.
   *
   * @param con        the con
   * @param columns    the columns
   * @param columndata the columndata
   * @param keyNames   the key names
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int updateWithNames(Connection con, String[] columns, Map columndata, String[] keyNames)
      throws SQLException, IOException {
    Map map = new HashMap();
    for (String key : keyNames) {
      map.put(key, columndata.get(key));
    }
    return update(con, table, Arrays.asList(columns), columndata, map);
  }

  /**
   * Update with name.
   *
   * @param con            the con
   * @param columnData     the column data
   * @param primaryKeyName the primary key name
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int updateWithName(Connection con, Map columnData, String primaryKeyName)
      throws SQLException, IOException {
    Map map = new HashMap();
    map.put(primaryKeyName, columnData.get(primaryKeyName));
    return update(con, table, columnData, map);
  }

  /**
   * Update with name.
   *
   * @param con            the con
   * @param columns        the columns
   * @param columnData     the column data
   * @param primaryKeyName the primary key name
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int updateWithName(Connection con, String[] columns, Map columnData, String primaryKeyName)
      throws SQLException, IOException {
    Map map = new HashMap();
    map.put(primaryKeyName, columnData.get(primaryKeyName));
    return update(con, table, Arrays.asList(columns), columnData, map);
  }

  /**
   * Various variations of listAll().
   *
   * @return the list
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> listAll() throws SQLException {
    return listAll(Collections.EMPTY_LIST);
  }

  /**
   * List all.
   *
   * @param sortColumn the sort column
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(String sortColumn) throws SQLException {
    return listAll(Collections.EMPTY_LIST, null, null, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns    the columns
   * @param sortColumn the sort column
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(List<String> columns, String sortColumn) throws SQLException {
    return listAll(columns, null, null, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(List<String> columns) throws SQLException {
    return listAll(columns, null, null, null);
  }

  /**
   * List all.
   *
   * @param columns     the columns
   * @param filterBy    the filter by
   * @param filterValue the filter value
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue)
      throws SQLException {
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
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (filterBy == null && filterValue == null) {
      filterMap = null;
    } else {
      filterMap.put(filterBy, filterValue);
    }
    try {
      return listAll(con, columns, filterMap, sortColumn);
    } finally {
      con.close();
    }
  }

  /**
   * List all.
   *
   * @param con         the con
   * @param columns     the columns
   * @param filterBy    the filter by
   * @param filterValue the filter value
   * @param sortColumn  the sort column
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(Connection con, List<String> columns, String filterBy,
      Object filterValue, String sortColumn) throws SQLException {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (filterBy == null && filterValue == null) {
      filterMap = null;
    } else {
      filterMap.put(filterBy, filterValue);
    }
    return listAll(con, columns, filterMap, sortColumn);
  }

  /**
   * List all.
   *
   * @param columns    the columns
   * @param filterMap  the filter map
   * @param sortColumn the sort column
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map filterMap, String sortColumn)
      throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      return listAll(con, columns, filterMap, sortColumn);
    } finally {
      con.close();
    }
  }

  /**
   * List all the data filtered using the filterBy column.
   *
   * @param conn       the conn
   * @param columns    the columns
   * @param filterMap  the filter map
   * @param sortColumn the sort column
   * @return the list
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> listAll(Connection conn, List<String> columns, Map filterMap,
      String sortColumn) throws SQLException {
    boolean shouldFilter = filterMap != null && !filterMap.isEmpty();
    StringBuilder query = new StringBuilder("SELECT ");
    if (columns == null || columns.isEmpty()) {
      query.append("*");
    } else {
      boolean first = true;
      for (String column : columns) {
        column = DataBaseUtil.quoteIdent(column);
        if (!first) {
          query.append(", ");
        }
        first = false;
        query.append(column);
      }
    }
    query.append(" FROM ").append(table);

    if (shouldFilter) {
      Iterator it = filterMap.entrySet().iterator();
      query.append(" WHERE ");
      int index = 1;
      while (it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();

        query.append(DataBaseUtil.quoteIdent((String) entry.getKey())).append("=?");
        if (index != filterMap.size()) {
          query.append(" AND ");
        }
        index++;
      }
    }

    if ((sortColumn != null) && !sortColumn.equals("")) {
      query.append(" ORDER BY " + DataBaseUtil.quoteIdent(sortColumn));
    }

    Connection con = (conn == null) ? DataBaseUtil.getReadOnlyConnection() : conn;
    PreparedStatement ps = con.prepareStatement(query.toString());

    try {
      if (shouldFilter) {
        Iterator it1 = filterMap.entrySet().iterator();
        int index = 1;
        while (it1.hasNext()) {
          Map.Entry entry = (Map.Entry) it1.next();
          query.append(" WHERE ");
          ps.setObject(index, entry.getValue());
          index++;
        }
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (conn == null) {
        DataBaseUtil.closeConnections(con, ps);
      } else {
        DataBaseUtil.closeConnections(null, ps);
      }
    }
  }

  /**
   * Gets a record using map of key identifiers.
   *
   * @param identifiers the identifiers
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(Map<String, Object> identifiers) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      return findByKey(con, (List) null, identifiers);
    } finally {
      con.close();
    }
  }

  /**
   * Find by key.
   *
   * @param columns     the columns
   * @param identifiers the identifiers
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(List<String> columns, Map<String, Object> identifiers)
      throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      return findByKey(con, columns, identifiers);
    } finally {
      con.close();
    }
  }

  /**
   * Find by key.
   *
   * @param con         the con
   * @param identifiers the identifiers
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(Connection con, Map<String, Object> identifiers)
      throws SQLException {
    return findByKey(con, (List<String>) null, identifiers);
  }

  /**
   * Find by key.
   *
   * @param con         the con
   * @param columns     the columns
   * @param identifiers the identifiers
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(Connection con, List<String> columns,
      Map<String, Object> identifiers) throws SQLException {

    StringBuilder query = new StringBuilder("SELECT ");
    if (columns == null || columns.isEmpty()) {
      query.append("*");
    } else {
      boolean first = true;
      for (String column : columns) {
        column = DataBaseUtil.quoteIdent(column);
        if (!first) {
          query.append(", ");
        }
        first = false;
        query.append(column);
      }
    }
    query.append(" FROM ").append(table);

    Iterator it = identifiers.entrySet().iterator();
    query.append(" WHERE ");
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      query.append(DataBaseUtil.quoteIdent((String) entry.getKey())).append("=?");

      if (it.hasNext()) {
        query.append(" AND ");
      }
    }

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      int index = 1;
      it = identifiers.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();
        ps.setObject(index++, entry.getValue());
      }
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      ps.close();
    }
  }

  /**
   * Get the record as a DynaBean as identified by key.
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(String keycolumn, Object identifier) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      return findByKey(con, keycolumn, identifier);
    } finally {
      con.close();
    }
  }

  /**
   * Find by key. Pass in a connection if within a transaction. Useful when you have just inserted
   * something, and within the same transaction, you want to find that row. If you don't pass the
   * connection, (as in the method above), the key will not be found since it is not committed yet.
   * Whereas, for the connection that you pass, even though not committed, it will find the key.
   *
   * @param con        the con
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findByKey(Connection con, String keycolumn, Object identifier)
      throws SQLException {

    StringBuilder query = new StringBuilder();
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    query.append("SELECT * FROM ").append(table).append(" WHERE ").append(keycolumn).append("=?;");

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      ps.close();
    }
  }

  /**
   * Check if row exist.
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean exist(String keycolumn, Object identifier) throws SQLException {
    return exist(keycolumn, identifier, true);
  }

  /**
   * checks whether row exists or not based on identifier and keycolumn.
   * if keycolumn type is String.class(i.e., character varying or char or character or text)
   *  it will check equality with case insensitive. returns true if record exists with column
   * value(identifier) otherwise false.
   *
   * @param keycolumn       the keycolumn
   * @param identifier      the identifier
   * @param caseInsensitive the case insensitive
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean exist(String keycolumn, Object identifier, boolean caseInsensitive)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    Class columnType = getColumnType(keycolumn);
    query.append("SELECT " + keycolumn + " FROM ").append(table).append(" WHERE ");
    if (columnType == String.class) {
      query.append(caseInsensitive ? "upper(" : "").append(keycolumn)
          .append(caseInsensitive ? ")" : "").append("=?;");

    } else {
      query.append(keycolumn).append("=?;");
    }

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(query.toString());
      if (columnType == String.class) {
        ps.setObject(1, caseInsensitive ? identifier.toString().toUpperCase() : identifier);
      } else {
        ps.setObject(1, identifier);
      }
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return false;
  }

  /**
   * Find exists by key.
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findExistsByKey(String keycolumn, Object identifier) throws SQLException {
    return findExistsByKey(keycolumn, identifier, null);
  }

  /**
   * This method has the same result of exist method, which returns the row if found (based on
   * identifier and keycolumn).
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @param columns    the columns
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean findExistsByKey(String keycolumn, Object identifier, List<String> columns)
      throws SQLException {
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);

    StringBuilder query = new StringBuilder("SELECT ");
    if (columns == null || columns.isEmpty()) {
      query.append("*");
    } else {
      boolean first = true;
      for (String column : columns) {
        column = DataBaseUtil.quoteIdent(column);
        if (!first) {
          query.append(", ");
        }
        first = false;
        query.append(column);
      }
    }
    query.append(" FROM ").append(table);
    query.append(" WHERE ");

    Class columnType = getColumnType(keycolumn);
    if (columnType == String.class) {
      query.append("upper(").append(keycolumn).append(")").append("=upper(?);");

    } else {
      query.append(keycolumn).append("=?;");
    }

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier);
      rs = ps.executeQuery();
      RowSetDynaClass rsd = new RowSetDynaClass(rs);
      List<BasicDynaBean> list = rsd.getRows();
      if ((list != null) && (list.size() > 0)) {
        return list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /**
   * Find all by key.
   *
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllByKey(String keycolumn, Object identifier) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      return findAllByKey(con, keycolumn, identifier);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Find all by key.
   *
   * @param con        the con
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return the list
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> findAllByKey(Connection con, String keycolumn, Object identifier)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    query.append("SELECT * FROM ").append(table).append(" WHERE ").append(keycolumn).append("=?;");

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * This is a utility method to return the first row from the table. Very handy where table has
   * only one row of data. [Example generic_preferences]
   *
   * @param con the con
   * @return the record
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getRecord(Connection con) throws SQLException {
    String query = "SELECT * FROM " + table;

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      List list = DataBaseUtil.queryToDynaList(ps);
      return (BasicDynaBean) list.get(0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the record.
   *
   * @return the record
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getRecord() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    String query = "SELECT * FROM " + table;
    try {
      ps = con.prepareStatement(query);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.isEmpty()) {
        return null;
      } else {
        return (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * This is a utility to return one column from the table as a list.
   *
   * @param columnName the column name
   * @return the column list
   * @throws SQLException the SQL exception
   */
  public List<String> getColumnList(String columnName) throws SQLException {
    String query = "SELECT " + columnName + " FROM " + table;

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    List returnList = null;
    try {
      ps = con.prepareStatement(query);
      List<BasicDynaBean> beanList = (List<BasicDynaBean>) DataBaseUtil.queryToDynaList(ps);
      returnList = new ArrayList();
      for (BasicDynaBean b : beanList) {
        returnList.add(b.get(columnName));
      }
      return returnList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Delete the records as specified by the key.
   *
   * @param con        the con
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return true if any rows were deleted, otherwise false.
   * @throws SQLException the SQL exception
   */
  public boolean delete(Connection con, String keycolumn, Object identifier) throws SQLException {

    StringBuilder query = new StringBuilder();
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    query.append("DELETE FROM ").append(table).append(" WHERE ").append(keycolumn).append("=?;");

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete.
   *
   * @param con         the con
   * @param keycolumn1  the keycolumn 1
   * @param identifier1 the identifier 1
   * @param keycolumn2  the keycolumn 2
   * @param identifier2 the identifier 2
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean delete(Connection con, String keycolumn1, Object identifier1, String keycolumn2,
      Object identifier2) throws SQLException {

    StringBuilder query = new StringBuilder();
    keycolumn1 = DataBaseUtil.quoteIdent(keycolumn1);
    keycolumn2 = DataBaseUtil.quoteIdent(keycolumn2);
    query.append("DELETE FROM ").append(table).append(" WHERE ").append(keycolumn1).append("=?")
        .append(" and ").append(keycolumn2).append("=?;");

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier1);
      ps.setObject(2, identifier2);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete.
   *
   * @param con         the con
   * @param identifiers the identifiers
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean delete(Connection con, LinkedHashMap<String, Object> identifiers)
      throws SQLException {

    StringBuilder query = new StringBuilder();
    query.append("DELETE FROM ").append(table);

    Iterator it = identifiers.entrySet().iterator();
    query.append(" WHERE ");
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      query.append(DataBaseUtil.quoteIdent((String) entry.getKey())).append("=?");

      if (it.hasNext()) {
        query.append(" AND ");
      }
    }

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      int index = 1;
      it = identifiers.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();
        ps.setObject(index++, entry.getValue());
      }
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete all records.
   *
   * @param con the con
   * @return true if any rows were deleted, otherwise false.
   * @throws SQLException the SQL exception
   */

  public boolean deleteAll(Connection con) throws SQLException {

    StringBuilder query = new StringBuilder();
    StringBuilder countQuery = new StringBuilder();
    query.append("DELETE FROM ").append(table);
    countQuery.append("SELECT count(*)  FROM ").append(table);
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(countQuery.toString());
      rs = ps.executeQuery();
      if (rs.next()) {
        int noRowsCount = rs.getInt("count");
        if (noRowsCount == 0) {
          return true;
        }
      }
      ps = con.prepareStatement(query.toString());
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
  }

  /**
   * Gets the insert statement.
   *
   * @param con     the con
   * @param columns the columns
   * @return the insert statement
   * @throws SQLException the SQL exception
   */
  private PreparedStatement getInsertStatement(Connection con, Set<String> columns)
      throws SQLException {
    return con.prepareStatement(getInsertQuery(columns));
  }

  /**
   * Gets the insert query.
   *
   * @param columns the columns
   * @return the insert query
   */
  protected String getInsertQuery(Set<String> columns) {
    StringBuilder stmt = new StringBuilder();

    if (columns.isEmpty()) {
      stmt.append("INSERT INTO ").append(table).append(" default values");
      return stmt.toString();
    }
    stmt.append("INSERT INTO ").append(table).append(" (");

    StringBuilder tmp = new StringBuilder();

    boolean first = true;
    for (String column : columns) {
      if (!first) {
        stmt.append(", ");
        tmp.append(", ");
      }
      first = false;
      stmt.append(DataBaseUtil.quoteIdent(column));
      tmp.append("?");
    }

    if (first) {
      // No fields supplied, error out
      return null;
    }

    stmt.append(") VALUES (");
    stmt.append(tmp.toString());
    stmt.append(")");
    return stmt.toString();
  }

  /**
   * Sets the parameter values in a prepared statement.
   *
   * @param statement                 the statement
   * @param bean                      the bean
   * @param additionalParameterValues the additional parameter values
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected void setParameterValues(PreparedStatement statement, BasicDynaBean bean,
      Object[] additionalParameterValues) throws SQLException, IOException {
    int placeholderIndex = 1;
    for (Object column : bean.getMap().keySet()) {
      Object object = bean.get((String) column);
      if (object instanceof java.io.InputStream) {
        java.io.InputStream stream = (java.io.InputStream) object;
        statement.setBinaryStream(placeholderIndex++, stream, stream.available());
      } else {
        statement.setObject(placeholderIndex++, object);
      }
    }

    if (null != additionalParameterValues) {
      for (Object parameterValue : additionalParameterValues) {
        statement.setObject(placeholderIndex, parameterValue);
        placeholderIndex = placeholderIndex + 1;
      }
    }
  }

  /** The get columns. */
  private static String GET_COLUMNS = "SELECT column_name,data_type FROM "
      + " information_schema.columns WHERE table_schema=(select current_schema()) "
      + " AND table_name = ? ";

  /**
   * Gets the bean.
   *
   * @return the bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getBean() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return getBean(con);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the bean.
   *
   * @param con the con
   * @return the bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getBean(Connection con) throws SQLException {
    DynaBeanBuilder builder = GenericDAO.CACHE.get(table);
    if (builder == null) {

      Class typeClass = String.class;
      PreparedStatement ps = null;
      try {
        builder = new DynaBeanBuilder();
        ps = con.prepareStatement(GET_COLUMNS);
        ps.setString(1, table);

        ArrayList<DynaBean> list = (ArrayList<DynaBean>) DataBaseUtil.queryToDynaList(ps);
        for (DynaBean columns : list) {
          String type = (String) columns.get("data_type");
          if (type.equalsIgnoreCase("integer")) {
            typeClass = Integer.class;
          } else if (type.equalsIgnoreCase("date")) {
            typeClass = java.sql.Date.class;
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
            typeClass = java.io.InputStream.class;
          } else {
            typeClass = String.class;
          }
          builder.add((String) columns.get("column_name"), typeClass);
        }
        GenericDAO.CACHE.put(table, builder);

      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }
    }
    return builder.build(table);
  }

  /**
   * Load bytea records.
   *
   * @param bean       the bean
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean loadByteaRecords(BasicDynaBean bean, String keycolumn, Object identifier)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return loadByteaRecords(con, bean, keycolumn, identifier);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * provided to load the bytea type records (b'cause DataBaseUtil.queryToDynaList(ps) cannot be
   * used to load the bytea type records).
   *
   * @param con        the con
   * @param bean       the bean
   * @param keycolumn  the keycolumn
   * @param identifier the identifier
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean loadByteaRecords(Connection con, BasicDynaBean bean, String keycolumn,
      Object identifier) throws SQLException {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();
    List<DynaProperty> fields = Arrays.asList(dynaProperties);
    StringBuilder sb = new StringBuilder("select ");
    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    boolean loaded = false;
    boolean first = true;
    for (DynaProperty property : fields) {
      if (!first) {
        sb.append(", ");
        // tmp.append(", ");
      }
      first = false;
      sb.append(property.getName());
      // tmp.append("?");
    }
    sb.append(" FROM " + table).append(" where " + keycolumn).append("=?");

    if (first) {
      // No fields supplied, error out
      return false;
    }

    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = con.prepareStatement(sb.toString());
      ps.setObject(1, identifier);
      rs = ps.executeQuery();
      if (rs.next()) {
        loaded = true;
        for (DynaProperty property : fields) {
          try {
            if (property.getType().equals(java.io.InputStream.class)) {
              if (rs.getBinaryStream(property.getName()) == null) {
                bean.set(property.getName(), null);
              } else {
                bean.set(property.getName(), ConvertUtils
                    .convert(rs.getBinaryStream(property.getName()), property.getType()));
              }
            } else {
              if (rs.getObject(property.getName()) == null) {
                bean.set(property.getName(), null);
              } else {
                bean.set(property.getName(),
                    ConvertUtils.convert(rs.getObject(property.getName()), property.getType()));
              }
            }
          } catch (ConversionException exception) {
            log.error(
                "Conversion error. " + property.getName() + "=" + rs.getObject(property.getName())
                    + " could not be converted to " + property.getType(),
                exception);
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    return loaded;
  }

  /**
   * returns the column type.
   *
   * @param column the column
   * @return the column type
   * @throws SQLException the SQL exception
   */
  private Class getColumnType(String column) throws SQLException {

    /*
     * populating cache(if the bean not existing). because this method can be called just after
     * intialization of this class without calling the getBean().
     *
     * ex: GenericDAO dao = new GenericDAO("foo"); dao.getColumnType("column_name");
     *
     */
    BasicDynaBean bean = getBean();
    DynaProperty prop = getBean().getDynaClass().getDynaProperty(column);
    return prop.getType();
  }

  /**
   * Alter trigger.
   *
   * @param con         the con
   * @param operation   the operation
   * @param tableName   the table name
   * @param triggerName the trigger name
   * @throws SQLException the SQL exception
   */
  public static void alterTrigger(Connection con, String operation, String tableName,
      String triggerName) throws SQLException {
    Statement statement = null;
    try {
      statement = con.createStatement();
      statement.executeUpdate(
          "ALTER TABLE " + tableName + " " + operation + " TRIGGER " + triggerName + " ");
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Alter trigger.
   *
   * @param operation   the operation
   * @param tableName   the table name
   * @param triggerName the trigger name
   * @throws SQLException the SQL exception
   */
  public static void alterTrigger(String operation, String tableName, String triggerName)
      throws SQLException {

    Statement statement = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      statement = con.createStatement();
      statement.executeUpdate(
          "ALTER TABLE " + tableName + " " + operation + " TRIGGER " + triggerName + " ");
    } finally {

      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }

  }

  /**
   * Lock table. this method is used for lock entire table with exclusive lock, so no
   * read,write,update and delete operations allows on table until transaction finished.
   *
   * @param con       the con
   * @param tableName the table name
   * @throws SQLException the SQL exception
   */
  public static void lockTable(Connection con, String tableName) throws SQLException {
    Statement st = null;
    try {
      st = con.createStatement();
      st.executeUpdate("LOCK TABLE " + tableName + " IN ACCESS EXCLUSIVE MODE ");
    } finally {
      if (st != null) {
        st.close();
      }
    }
  }

  private static final String GET_DATE_AND_TIME =
      "SELECT localtimestamp(0) as timestamp FROM LOCALTIMESTAMP(0)";

  /**
   * Gets the date and time from DB.
   *
   * @return the date and time
   */
  public static Timestamp getDateAndTime() {
    return (Timestamp) DatabaseHelper.queryToDynaBean(GET_DATE_AND_TIME).get("timestamp");
  }

}

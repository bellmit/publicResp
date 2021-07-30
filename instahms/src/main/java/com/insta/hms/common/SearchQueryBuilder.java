package com.insta.hms.common;
/*
 * Utility class to build an SQL search kind of query. A search query has the following
 * characteristics:
 *  - Many fields are presented to the user
 *  - User enters some of the fields, and leaves some others blank (same as bugzilla)
 *  - All non-empty fields are used for searching a table, with different operators,
 *    like =, <=, like and IN
 *  - The result is a PagedList (see PagedList.java).
 *
 * This class helps build a query dynamically. Dynamic query building is required
 * because, for the fields that the user has left empty, there shouldn't be any "where"
 * statement, but for other fields, there needs to be. The steps for building and using
 * the query builder are:
 *
 * 1. Construct the builder using query statements, tables, etc. Eg:
 *   Connection con = DataBaseUtil.getConnection();
 *   SearchQueryBuilder qb = new SearchQueryBuilder(con, QUERY_FIELDS, QUERY_COUNT, QUERY_TABLES,
 *    INIT_WHERE_CLAUSE, GROUP_BYFIELDS, sortFieldName, isReverseSort, pageSize, pageNum);
 *
 * 2. Add filters to the builder, either one by one, or by passing the parameterMap.
 * (a) one by one:
 *   qb.addFilter(qb.STRING, "field1", "=", field1Value);
 *   qb.addFilter(qb.NUMERIC, "field2", "=", field2Value);
 *   qb.addFilter(qb.STRING, "field3", "IN", field3ValueList);
 *
 * (b) Automatic, from the parameterMap:
 *   addFilterFromParamMap(req.getParameterMap());
 *
 * The automatic filter addition works on the basis of the UI param names being same as the db
 * field names. In addition to the field name, the data type and operator are also required.
 *
 * Data type: it is guessed from the field name (eg, open_date must be date field). Defaults to
 * "string". If you need to specify (eg, if open_date happens to be an integer), you should add
 * a hidden field like this:
 *   <input type="hidden" name="open_date@type" value="integer">
 * Supported data types are: text/string, numeric, integer, date, time, timestamp, boolean
 *
 * Operator(s): Defaults to "eq" if we find a single value, or "in" if we find more values. To
 * explicitly specify the operator, add a hidden field like this:
 *   <input type"hidden" name="open_date@op" value="ge">
 *
 * If the same field has more than one comparison (eg, fromDate and toDate), then, use the same
 * name for the field, and add two hidden operators like this:
 *   <input type"hidden" name="open_date@op" value="ge">
 *   <input type"hidden" name="open_date@op" value="le">
 * (this means, the first value of open_date is compared using ge and second using le).
 *
 * You could also combine multiple operators into one using a comma separated list like this:
 *   <input type"hidden" name="open_date@op" value="ge,le">
 *
 * Supported operators are:
 *  eq, neq, lt, gt, le, ge, co (contains), ico (ignore-case contains), sw (starts-with), isw,
 *  ew (ends with), iew, like, (put the % in the value yourself), betw, in, nin, null (=Y/N),
 *  period (=pd/td/pm/tm/pw/tw/py/ty/pf/tf).
 *
 * Note that the betw operator expects two values, so you cannot use it in a date range if it is
 * allowed to give one date and not the other. Use ge,le if the from/to dates are independent.
 *
 * 3. Build the query:
 *   qb.build();
 *
 * 4. Run the query and get the results. Various methods are available to do this:
 *
 * (a) Use getDynaPagedList() to get a PagedList containing DynaBeans
 * (b) Use getMappedPagedList() to get a PagedList containing HashMaps
 * (c) Use getDataStatement() and getCountStatement() to get the statements, execute and iterate
 *
 * 5. Cleanup
 *   qb.close();  // close stmts
 *   con.close();
 *
 */

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;

import com.insta.hms.exception.HMSException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



// TODO: Auto-generated Javadoc
/**
 * The Class SearchQueryBuilder.
 */
public class SearchQueryBuilder extends QueryBuilder {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SearchQueryBuilder.class);

  /** The con. */
  protected Connection con;

  /** The select fields. */
  protected String selectFields;

  /** The select count. */
  protected String selectCount;

  /** The from tables. */
  protected String fromTables;

  /** The sort field. */
  protected String sortField;

  /** The group by fields. */
  protected String groupByFields;

  /** The sort reverse. */
  protected boolean sortReverse;

  /** The page size. */
  protected int pageSize;

  /** The page num. */
  protected int pageNum;
  
  /** The valid columns. */
  protected List<String> validColumns;

  /** The data query. */
  protected StringBuilder dataQuery;

  /** The count query. */
  protected StringBuilder countQuery;

  /** The built. */
  protected boolean built = false;

  /** The data stmt. */
  protected PreparedStatement dataStmt;

  /** The count stmt. */
  protected PreparedStatement countStmt;

  /** The secondary sort fields. */
  protected ArrayList<String> secondarySortFields;

  /** The secondary sort reverse. */
  protected ArrayList<Boolean> secondarySortReverse;

  
  /**
   * Instantiates a new search query builder.
   *
   * @param con
   *          the con
   * @param selectFields
   *          the select fields
   * @param selectCount
   *          the select count
   * @param fromTables
   *          the from tables
   * @param initWhere
   *          the init where
   * @param groupByFields
   *          the group by fields
   * @param sortField
   *          the sort field
   * @param sortReverse
   *          the sort reverse
   * @param pageSize
   *          the page size
   * @param pageNum
   *          the page num
   * @param validColumns
   *          the valid columns
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupByFields, String sortField,
      boolean sortReverse, int pageSize, int pageNum, List<String> validColumns) {

    super(initWhere);
    this.con = con;
    this.selectFields = selectFields;
    this.selectCount = selectCount;
    this.fromTables = fromTables;
    this.groupByFields = groupByFields;
    this.sortField = sortField;
    this.sortReverse = sortReverse;
    this.pageSize = pageSize;
    this.pageNum = pageNum;
    this.validColumns = validColumns;

    dataQuery = new StringBuilder();
    if (selectCount != null) {
      countQuery = new StringBuilder();
    }

    secondarySortFields = new ArrayList();
    secondarySortReverse = new ArrayList();
  }
  
  /**
   * Instantiates a new search query builder. Constructor: self explanatory, except for initWhere,
   * which is an optional initial filter to append to the query. If non-null, we assume that it
   * already contains a WHERE clause, so that addFilter will only append AND to the end.
   *
   * @param con           the con
   * @param selectFields  the select fields
   * @param selectCount   the select count
   * @param fromTables    the from tables
   * @param initWhere     the init where
   * @param groupByFields the group by fields
   * @param sortField     the sort field
   * @param sortReverse   the sort reverse
   * @param pageSize      the page size
   * @param pageNum       the page num
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupByFields, String sortField,
      boolean sortReverse, int pageSize, int pageNum) {
    
    this(con,selectFields,selectCount,fromTables,initWhere,groupByFields,sortField,
      sortReverse,pageSize,pageNum,null);
  }


  /**
   * Instantiates a new search query builder.
   *
   * @param con          the con
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param sortField    the sort field
   * @param sortReverse  the sort reverse
   * @param pageSize     the page size
   * @param pageNum      the page num
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String sortField, boolean sortReverse, int pageSize,
      int pageNum) {

    this(con, selectFields, selectCount, fromTables, initWhere, null, sortField, sortReverse,
        pageSize, pageNum);
  }

  /**
   * Instantiates a new search query builder.
   *
   * @param con          the con
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param listing      the listing
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, Map listing) {

    this(con, selectFields, selectCount, fromTables, null, null,
        (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
        (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
  }

  /**
   * Instantiates a new search query builder.
   *
   * @param con          the con
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param listing      the listing
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, Map listing) {

    this(con, selectFields, selectCount, fromTables, initWhere, null,
        (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
        (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
  }

  /**
   * Instantiates a new search query builder.
   *
   * @param con          the con
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param groupBy      the group by
   * @param listing      the listing
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupBy, Map listing) {

    this(con, selectFields, selectCount, fromTables, initWhere, groupBy,
        (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
        (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
  }
  

  /**
   * Instantiates a new search query builder.
   *
   * @param con
   *          the con
   * @param selectFields
   *          the select fields
   * @param selectCount
   *          the select count
   * @param fromTables
   *          the from tables
   * @param initWhere
   *          the init where
   * @param groupBy
   *          the group by
   * @param listing
   *          the listing
   * @param validColumns
   *          the valid columns
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupBy, Map listing, List<String> validColumns) {

    this(con, selectFields, selectCount, fromTables, initWhere, groupBy, (String) listing
        .get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC), (Integer) listing
        .get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM), validColumns);
  }

  
  /**
   * Instantiates a new search query builder.
   *
   * @param con          the con
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   */
  public SearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables) {
    this(con, selectFields, selectCount, fromTables, null, null, null, false, 0, 0);
  }

  /**
   * Adds the filter from param map. Finds the types and operators acting on parameters from request
   * map and adds filters for all of them at one shot.
   *
   * @param map the map
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public void addFilterFromParamMap(Map map) throws ParseException, SQLException {
    if (map == null || map.isEmpty() || map.entrySet() == null || map.keySet() == null) {
      return;
    }

    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      String key = pairs.getKey().toString();// get the "key"
      
      if (pairs.getValue() == null) {
        continue;
      }
      if (key.startsWith("_")) { // explicitly required to ignore
        continue;
      }
      if (key.endsWith("@type") || key.endsWith("@op") || key.endsWith("@cast")) {
        // type and opparameters
        continue;
      }
      if (key.equals("method")) { // method parameter is used for DispatchAction
        continue;
      }
      if (key.equals("sortOrder") || key.equals("sortReverse") || key.equals("pageSize")
          || key.equals("pageNum")) { // listing params
        continue;
      }
      if (key.equals("title")) { // ?? todo.
        continue;
      }
      if (key.startsWith("exclude_in_qb")) {
        continue;
      }
      if (key.equals("prgkey")) {
        continue;
      }

      String keytype = key + "@type";
      String[] typeStrs = (String[]) map.get(keytype);
      String typeStr = ((typeStrs == null) || typeStrs[0].equals("")) ? guessType(key)
          : typeStrs[0];

      String keyop = key + "@op";
      String[] opTok = (String[]) map.get(keyop);
      String[] value = (String[]) pairs.getValue();
      String[] ops = getOperatorTokens(opTok, value.length);

      String keycast = key + "@cast";
      String[] castStrs = (String[]) map.get(keycast);
      String castStr = ((castStrs == null) || castStrs[0].equals("")) ? "N" : castStrs[0];
      boolean doCast = castStr.equalsIgnoreCase("y");
      if (ops[0].equals("in") || ops[0].equals("nin") || ops[0].equals("between")) {
        // we are expecting multiple values, so ignore any operator arrays.
        addFilterFromString(typeStr, DataBaseUtil.quoteIdent(key), ops[0], value, doCast);
      } else {
        // add a filter for every operator
        for (int i = 0; i < ops.length; ++i) {
          addFilterFromString(typeStr, DataBaseUtil.quoteIdent(key), ops[i], value[i], doCast);
        }
      }
    }
  }

  /**
   * Adds the secondary sort. Provision to add a secondary sort in addition to the given sort field.
   * A secondary sort on the primary key is usually required for proper pagination, since the order
   * of results must be predictable when we use OFFSET and LIMIT.
   *
   * @param sortField the sort field
   */
  public void addSecondarySort(String sortField) {
    addSecondarySort(sortField, false);
  }

  /**
   * Adds the secondary sort.
   *
   * @param sortField   the sort field
   * @param sortReverse the sort reverse
   */
  public void addSecondarySort(String sortField, boolean sortReverse) {
    secondarySortFields.add(sortField);
    secondarySortReverse.add(sortReverse);
  }

  /**
   * Build the query. If startWithWhere is true, then we start with WHERE in the filter, else we
   * start with AND assuming WHERE is already added to the query.
   *
   * @throws SQLException the SQL exception
   */
  public void build() throws SQLException {
    if (built) {
      return;
    }

    /*
     * Build the query to get the data
     */
    dataQuery.append(selectFields);
    if (null != fromTables) {
      dataQuery.append(fromTables);
    }

    if (initWhere != null) {
      dataQuery.append(" " + initWhere + " ");
    }

    if (null != whereClause) {
      dataQuery.append(whereClause);
    }

    if (groupByFields != null) {
      dataQuery.append(" GROUP BY ").append(groupByFields);
    }
    if (this.validColumns != null && !validateColumns()) {
      throw new HMSException("Invalid Column");
    }

    appendSort(dataQuery);

    if (pageSize != 0) {
      dataQuery.append(" LIMIT ?");
    }

    if (pageNum != 0) {
      dataQuery.append(" OFFSET ?");
    }

    /*
     * Build the query to get the count
     */
    if (countQuery != null) {
      countQuery.append(selectCount);
      if (null != fromTables) {
        countQuery.append(fromTables);
      }
      if (initWhere != null) {
        countQuery.append(initWhere);
      }
      countQuery.append(whereClause);
      // count query should not append the group by
    }

    dataStmt = con.prepareStatement(dataQuery.toString());
    logger.debug("Data query: " + dataQuery.toString());
    if (null != countQuery) {
      countStmt = con.prepareStatement(countQuery.toString());
      logger.debug("Count query: " + countQuery.toString());
    } else {
      logger.debug("Count query is null");
    }

    // now add the values to the statement
    int stmtIndex = 1;
    int numValues = fieldTypes.size();
    for (int i = 0; i < numValues; i++) {

      int type = (Integer) fieldTypes.get(i);
      Object value = fieldValues.get(i);
      if (value == null) {
        continue;
      }

      setTypeInStatement(dataStmt, stmtIndex, type, value);
      if (countStmt != null) {
        setTypeInStatement(countStmt, stmtIndex, type, value);
      }
      stmtIndex++;
    }

    if (pageSize != 0) {
      dataStmt.setInt(stmtIndex++, pageSize);
    }

    if (pageNum != 0) {
      dataStmt.setInt(stmtIndex++, (pageNum - 1) * pageSize);
    }

    built = true;
  }

  /**
   * Append sort.
   *
   * @param dataQuery the data query
   */
  protected void appendSort(StringBuilder dataQuery) {
    boolean sortAppend = false;
    /*
     * if ( (sortField != null) && !sortField.equals("") ) { dataQuery.append(" ORDER BY "
     * ).append(sortField); if (sortReverse) dataQuery.append(" DESC "); sortAppend = true; }
     * 
     * for (int i=0; i<secondarySortFields.size(); i++) { if (sortAppend) dataQuery.append(", ");
     * else dataQuery.append(" ORDER BY "); dataQuery.append(secondarySortFields.get(i)); if
     * (secondarySortReverse.get(i)) dataQuery.append(" DESC "); sortAppend = true; }
     */

    if ((sortField != null) && !sortField.equals("")) {
      dataQuery.append(" ORDER BY ").append(DataBaseUtil.quoteIdent(sortField.trim()));
      if (sortReverse) {
        dataQuery.append(" DESC ");
      }
      sortAppend = true;
    }

    for (int i = 0; i < secondarySortFields.size(); i++) {
      if (sortAppend) {
        dataQuery.append(", ");
      } else {
        dataQuery.append(" ORDER BY ");
      }
      dataQuery.append(DataBaseUtil.quoteIdent(secondarySortFields.get(i).trim()));
      if (secondarySortReverse.get(i)) {
        dataQuery.append(" DESC ");
      }
      sortAppend = true;
    }

  }

  /**
   * Gets the dyna paged list. Instead of a DTO list, if you prefer a BasicDynaBean list, this is an
   * easy way to get a PagedList containing a BasicDynaBean list
   *
   * @return the dyna paged list
   * @throws SQLException the SQL exception
   */
  public PagedList getDynaPagedList() throws SQLException {
    return getDynaPagedList(false);
  }

  /**
   * Gets the dyna paged list.
   *
   * @param returnRequestParamsForEmptyPage the return request params for empty page
   * @return the dyna paged list
   * @throws SQLException the SQL exception
   */
  public PagedList getDynaPagedList(boolean returnRequestParamsForEmptyPage) throws SQLException {

    PreparedStatement psData = getDataStatement();

    ResultSet rsData = psData.executeQuery();

    RowSetDynaClass rsd = new RowSetDynaClass(rsData);
    List dataList = rsd.getRows();
    rsData.close();
    psData.close();

    if (!returnRequestParamsForEmptyPage && (null == dataList || dataList.isEmpty())) {
      // just so that you dont fail the caller who does not check for null value;
      return new PagedList();
    }
    int totalCount = 0;
    Map countBeanMap = null;
    PreparedStatement psCount = getCountStatement();
    if (null != psCount) {
      ResultSet rsCount = psCount.executeQuery();
      RowSetDynaClass rsc = new RowSetDynaClass(rsCount);
      BasicDynaBean countBean = (BasicDynaBean) rsc.getRows().get(0);
      countBeanMap = countBean.getMap();
      rsCount.close();
      psCount.close();
      totalCount = ((Long) countBean.get("count")).intValue();
    }
    if (returnRequestParamsForEmptyPage && null == dataList) {
      dataList = new ArrayList();
    }
    logger.debug(Integer.toString(pageNum));
    return new PagedList(dataList, totalCount, pageSize, pageNum, countBeanMap);
  }

  /**
   * Gets the mapped paged list. Same as above, but converts the list into a list of Maps from the
   * BasicDynaBean list. This is so that in your JSP, you don't have to say listitem.map.field_name,
   * instead, you can use listitem.field_name directly
   *
   * @return the mapped paged list
   * @throws SQLException the SQL exception
   */
  public PagedList getMappedPagedList() throws SQLException {
    PagedList pl = getDynaPagedList();
    if ((pl != null) && (pl.getDtoList() != null)) {
      List mapList = ConversionUtils.copyListDynaBeansToMap(pl.getDtoList());
      pl.setDtoList(mapList);
    }
    return pl;
  }

  /**
   * Gets the data query string.
   *
   * @return the data query string
   */
  public String getDataQueryString() {
    return dataQuery.toString();
  }

  /**
   * Gets the count query string.
   *
   * @return the count query string
   */
  public String getCountQueryString() {
    return countQuery.toString();
  }

  /**
   * Gets the data statement.
   *
   * @return the data statement
   */
  public PreparedStatement getDataStatement() {
    if (!built) {
      return null;
    }
    return dataStmt;
  }

  /**
   * Gets the count statement.
   *
   * @return the count statement
   */
  public PreparedStatement getCountStatement() {
    if (!built) {
      return null;
    }
    return countStmt;
  }

  /**
   * Close.
   *
   * @throws SQLException the SQL exception
   */
  public void close() throws SQLException {
    if (dataStmt != null) {
      dataStmt.close();
    }
    if (countStmt != null) {
      countStmt.close();
    }
  }
  

  /**
   * Validate column.
   *
   * @return the boolean
   */
  public Boolean validateColumns() {
    Boolean result = true;
    if (groupByFields != null && this.validColumns != null) {
      List<String> groupFields = Arrays.asList(groupByFields.split(","));
      for (String field : groupFields) {
        result = result & this.validColumns.contains(field.trim());
      }
    }
    if (this.sortField != null && !this.sortField.equals("") && this.validColumns != null) {
      result = this.validColumns.contains(this.sortField) & result;
    }
    return result;
  }

}

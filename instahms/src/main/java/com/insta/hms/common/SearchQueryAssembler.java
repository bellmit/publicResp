package com.insta.hms.common;

import com.insta.hms.common.ConversionUtils.LISTING;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class SearchQueryAssembler.
 */
public class SearchQueryAssembler extends QueryAssembler {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SearchQueryAssembler.class);

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

  /** The data query. */
  protected StringBuilder dataQuery;

  /** The count query. */
  protected StringBuilder countQuery;

  /** The built. */
  protected boolean built = false;

  /** The secondary sort fields. */
  protected ArrayList<String> secondarySortFields;

  /** The secondary sort reverse. */
  protected ArrayList<Boolean> secondarySortReverse;

  /**
   * Instantiates a new search query assembler. Constructor: self explanatory, except for initWhere,
   * which is an optional initial filter to append to the query. If non-null, we assume that it
   * already contains a WHERE clause, so that addFilter will only append AND to the end.
   *
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
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables,
      String initWhere, String groupByFields, String sortField, boolean sortReverse, int pageSize,
      int pageNum) {

    super(initWhere);
    this.selectFields = selectFields;
    this.selectCount = selectCount;
    this.fromTables = fromTables;
    this.groupByFields = groupByFields;
    this.sortField = sortField;
    this.sortReverse = sortReverse;
    this.pageSize = pageSize;
    this.pageNum = pageNum;

    dataQuery = new StringBuilder();
    if (selectCount != null) {
      countQuery = new StringBuilder();
    }

    secondarySortFields = new ArrayList();
    secondarySortReverse = new ArrayList();
  }

  /**
   * Instantiates a new search query assembler.
   *
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param sortField    the sort field
   * @param sortReverse  the sort reverse
   * @param pageSize     the page size
   * @param pageNum      the page num
   */
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables,
      String initWhere, String sortField, boolean sortReverse, int pageSize, int pageNum) {

    this(selectFields, selectCount, fromTables, initWhere, null, sortField, sortReverse, pageSize,
        pageNum);
  }

  /**
   * Instantiates a new search query assembler.
   *
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param listing      the listing
   */
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables,
      Map listing) {

    this(selectFields, selectCount, fromTables, null, null, (String) listing.get(LISTING.SORTCOL),
        (Boolean) listing.get(LISTING.SORTASC), (Integer) listing.get(LISTING.PAGESIZE),
        (Integer) listing.get(LISTING.PAGENUM));
  }

  /**
   * Instantiates a new search query assembler.
   *
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param listing      the listing
   */
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables,
      String initWhere, Map listing) {

    this(selectFields, selectCount, fromTables, initWhere, null,
        (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
        (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
  }

  /**
   * Instantiates a new search query assembler.
   *
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   * @param initWhere    the init where
   * @param groupBy      the group by
   * @param listing      the listing
   */
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables,
      String initWhere, String groupBy, Map listing) {

    this(selectFields, selectCount, fromTables, initWhere, groupBy,
        (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
        (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
  }

  /**
   * Instantiates a new search query assembler.
   *
   * @param selectFields the select fields
   * @param selectCount  the select count
   * @param fromTables   the from tables
   */
  public SearchQueryAssembler(String selectFields, String selectCount, String fromTables) {
    this(selectFields, selectCount, fromTables, null, null, null, false, 0, 0);
  }

  /**
   * Adds the filter from param map. Finds the types and operators acting on parameters from request
   * map and adds filters for all of them at one shot.
   *
   * @param map the map
   */
  public void addFilterFromParamMap(Map map) {
    if (map == null || map.isEmpty() || map.entrySet() == null || map.keySet() == null) {
      return;
    }

    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      String key = pairs.getKey().toString();// get the "key"

      if (key.startsWith("_")) { // explicitly required to ignore
        continue;
      }
      if (key.endsWith("@type") || key.endsWith("@op") || key.endsWith("@cast")) {
        // type and op parameters
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
        // we are expecting multiple values, so ignore any operator
        // arrays.
        addFilterFromString(typeStr, DatabaseHelper.quoteIdent(key), ops[0], value, doCast);
      } else {
        // add a filter for every operator
        for (int i = 0; i < ops.length; ++i) {
          addFilterFromString(typeStr, DatabaseHelper.quoteIdent(key), ops[i], value[i], doCast);
        }
      }
    }
  }

  /**
   * Adds the secondary sort.
   *
   * @param sortField the sort field
   */
  /*
   * Provision to add a secondary sort in addition to the given sort field. A secondary sort on the
   * primary key is usually required for proper pagination, since the order of results must be
   * predictable when we use OFFSET and LIMIT.
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

  /*
   * Build the query. If startWithWhere is true, then we start with WHERE in the filter, else we
   * start with AND assuming WHERE is already added to the query.
   */

  /**
   * Builds the.
   */
  public void build() {
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

    logger.debug("Data query: " + dataQuery.toString());
    if (null != countQuery) {
      logger.debug("Count query: " + countQuery.toString());
    } else {
      logger.debug("Count query is null");
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
    if ((sortField != null) && !sortField.equals("")) {
      String field = sortField.trim();
      dataQuery.append(" ORDER BY ")
          .append(field.matches("[\\d]+") ? field : DatabaseHelper.quoteIdent(sortField.trim()));
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
      String field = secondarySortFields.get(i).trim();

      dataQuery.append(field.matches("[\\d]+") ? field : DatabaseHelper.quoteIdent(field));
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
   */
  public PagedList getDynaPagedList() {

    String count = null;
    if (countQuery != null) {
      count = getCountQueryString();
    }

    Collection<Object> parameterValues = new ArrayList();
    parameterValues.addAll(fieldValues);
    if (pageSize != 0) {
      parameterValues.add(pageSize);
    }

    if (pageNum != 0) {
      parameterValues.add((pageNum - 1) * pageSize);
    }

    String dataQuery = getDataQueryString();
    List<BasicDynaBean> dataList = DatabaseHelper.queryToDynaList(dataQuery,
        parameterValues.toArray());

    // if (null == dataList || dataList.isEmpty()) {
    // return new PagedList(); // just so that you dont fail the caller who
    // // does not check for null value;
    // }

    Integer totalCount = 0;
    BasicDynaBean countBean = null;
    if (countQuery != null) {
      countBean = DatabaseHelper.queryToDynaBean(count, fieldValues.toArray());
      totalCount = ((Long) countBean.get("count")).intValue();
    }

    if (countBean == null) {
      return new PagedList(dataList, totalCount, pageSize, pageNum);
    }

    return new PagedList(dataList, totalCount, pageSize, pageNum, countBean.getMap());
  }

  /**
   * Gets the mapped paged list. Same as above, but converts the list into a list of Maps from the
   * BasicDynaBean list. This is so that in your JSP, you don't have to say listitem.map.field_name,
   * instead, you can use listitem.field_name directly
   *
   * @return the mapped paged list
   */
  public PagedList getMappedPagedList() {
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
}

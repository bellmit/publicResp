package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class SplitSearchQueryBuilder. Variation of SearchQueryBuilder, where we know that the
 * filterable field set is not the same as the display field set. The two are thus different
 * queries. This will do a two-step querying process: first it will run the search query, and for
 * the result set, it will run the fields query based on the primary key name as a primaryKey IN
 * (?,?,?) (where ? are got from the search query results).
 * The steps for building and using SplitSearchQueryBuilder are:
 * 1. Construct the builder using two query statements, tables, etc. Eg: Connection con =
 * DataBaseUtil.getConnection(); SplitSearchQueryBuilder qb = new SplitSearchQueryBuilder(con,
 * SEARCH_QUERY, FIELDS_QUERY, INIT_WHERE_CLAUSE, primaryKeyFieldName, listingParams);
 * Note: remember to include all searchable fields, sortable fields, all fields in INIT_WHERE and
 * the primary key itself in the SEARCH_QUERY.
 * 2. Rest of the steps (add filters, build query, get result ...) are same as for
 * SearchQueryBuilder.
 * Restrictions: does not work with GROUP BY kind of searches.
 */
public class SplitSearchQueryBuilder extends SearchQueryBuilder {

  /** The search query. */
  protected String searchQuery;

  /** The fields query. */
  protected String fieldsQuery;

  /** The primary key name. */
  protected String primaryKeyName;

  /** The ext data stmt. */
  protected PreparedStatement extDataStmt;

  /**
   * Instantiates a new split search query builder.
   *
   * @param con            the con
   * @param searchQuery    the search query
   * @param fieldsQuery    the fields query
   * @param initWhere      the init where
   * @param primaryKeyName the primary key name
   * @param listing        the listing
   */
  public SplitSearchQueryBuilder(Connection con, String searchQuery, String fieldsQuery,
      String initWhere, String primaryKeyName, Map listing) {

    super(con, "SELECT * ", "SELECT COUNT(*) ", "FROM (" + searchQuery + ") as squery ", initWhere,
        listing);

    this.searchQuery = searchQuery;
    this.fieldsQuery = fieldsQuery;
    this.primaryKeyName = primaryKeyName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.SearchQueryBuilder#getDynaPagedList()
   */
  public PagedList getDynaPagedList() throws SQLException {
    return getDynaPagedList(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.SearchQueryBuilder#getDynaPagedList(boolean)
   */
  @Override
  public PagedList getDynaPagedList(boolean append) throws SQLException {

    PagedList searchResults = super.getDynaPagedList(false);

    // empty: return as is
    if ((searchResults.getDtoList().isEmpty()) || (searchResults.getTotalRecords() == 0)) {
      return searchResults;
    }

    // get the actual result from fields query for primary key IN values
    StringBuilder dataQuery = new StringBuilder(fieldsQuery);
    addWhereFieldOpValue(append, dataQuery, primaryKeyName, "IN", searchResults.getDtoList());

    // maintain main sort order
    appendSort(dataQuery);

    // execute the query and get new results dyna list
    PreparedStatement ps = con.prepareStatement(dataQuery.toString());
    int index = 1;
    String propertyKey = primaryKeyName;
    if (propertyKey.contains(".")) {
      propertyKey = propertyKey.replaceAll(".*\\.", "");
    }
    for (BasicDynaBean b : (List<BasicDynaBean>) searchResults.getDtoList()) {
      ps.setObject(index++, b.get(propertyKey));
    }

    List dataList = DataBaseUtil.queryToDynaList(ps);
    ps.close();

    return new PagedList(dataList, searchResults.getTotalRecords(), searchResults.getPageSize(),
        searchResults.getPageNumber(), searchResults.getCountInfo());
  }
}

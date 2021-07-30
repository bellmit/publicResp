package com.insta.hms.mdm;

/**
 * The Class SearchQuery.
 */
public class SearchQuery {

  /** The count query. */
  private String countQuery = null;
  
  /** The field list. */
  private String fieldList = null;
  
  /** The select tables. */
  private String selectTables = null;
  
  /** The secondary sort column. */
  private String secondarySortColumn = null;

  /** The Constant DEFAULT_FIELD_LIST. */
  private static final String DEFAULT_FIELD_LIST = "SELECT * ";
  
  /** The Constant DEFAULT_COUNT_QUERY. */
  private static final String DEFAULT_COUNT_QUERY = "SELECT COUNT(*) ";

  /**
   * Instantiates a new search query.
   *
   * @param selectTables the select tables
   */
  public SearchQuery(String selectTables) {
    this(DEFAULT_FIELD_LIST, DEFAULT_COUNT_QUERY, selectTables);
  }

  /**
   * Instantiates a new search query.
   *
   * @param fieldList the field list
   * @param countQuery the count query
   * @param selectTables the select tables
   */
  public SearchQuery(String fieldList, String countQuery, String selectTables) {
    this(fieldList, countQuery, selectTables, null);

  }

  /**
   * Instantiates a new search query.
   *
   * @param fieldList the field list
   * @param countQuery the count query
   * @param selectTables the select tables
   * @param secondarySortColumn the secondary sort column
   */
  public SearchQuery(String fieldList, String countQuery, String selectTables,
      String secondarySortColumn) {
    this.fieldList = fieldList;
    this.countQuery = countQuery;
    this.selectTables = selectTables;
    this.secondarySortColumn = secondarySortColumn;
  }

  /**
   * Gets the count query.
   *
   * @return the count query
   */
  public String getCountQuery() {
    return countQuery;
  }

  /**
   * Gets the field list.
   *
   * @return the field list
   */
  public String getFieldList() {
    return fieldList;
  }

  /**
   * Gets the select tables.
   *
   * @return the select tables
   */
  public String getSelectTables() {
    return selectTables;
  }

  /**
   * Gets the secondary sort column.
   *
   * @return the secondary sort column
   */
  public String getSecondarySortColumn() {
    return secondarySortColumn;
  }

  /**
   * Sets the secondary sort column.
   *
   * @param secondarySortColumn the new secondary sort column
   */
  public void setSecondarySortColumn(String secondarySortColumn) {
    this.secondarySortColumn = secondarySortColumn;
  }

}

package com.insta.hms.messaging.providers;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class QueryDataProvider.
 */
public class QueryDataProvider extends GenericDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(QueryDataProvider.class);

  /** The Constant DEFAULT_PAGE_SIZE. */
  public static final int DEFAULT_PAGE_SIZE = 25;

  /** The Constant DEFAULT_PAGE_NUM. */
  public static final int DEFAULT_PAGE_NUM = 0;

  /** The name. */
  private String name = null;

  /** The select fields. */
  private String selectFields = null;

  /** The select count. */
  private String selectCount = null;

  /** The from tables. */
  private String fromTables = null;

  /** The init where. */
  private String initWhere = null;

  /** The sort field. */
  private String sortField = null;

  /** The sort reverse. */
  private boolean sortReverse = false;

  /** The Constant STOCK_FIELDS. */
  private static final String[] STOCK_FIELDS = new String[] { "receipient_name", "recipient_name",
      "recipient_email", "receipient_mobile", "recipient_mobile" };

  /** The Constant KEY_FIELD_NAME. */
  private static final String KEY_FIELD_NAME = "key";

  /**
   * Instantiates a new query data provider.
   *
   * @param providerName
   *          the provider name
   */
  public QueryDataProvider(String providerName) {
    this.name = providerName;
  }

  /**
   * Sets the query params.
   *
   * @param selectFields
   *          the select fields
   * @param selectCount
   *          the select count
   * @param fromTables
   *          the from tables
   * @param initWhere
   *          the init where
   */
  public void setQueryParams(String selectFields, String selectCount, String fromTables,
      String initWhere) {
    setQueryParams(selectFields, selectCount, fromTables, initWhere, null, false);
  }

  /**
   * Sets the query params.
   *
   * @param selectFields
   *          the select fields
   * @param selectCount
   *          the select count
   * @param fromTables
   *          the from tables
   * @param initWhere
   *          the init where
   * @param sortField
   *          the sort field
   * @param sortReverse
   *          the sort reverse
   */
  public void setQueryParams(String selectFields, String selectCount, String fromTables,
      String initWhere, String sortField, boolean sortReverse) {
    this.selectFields = selectFields;
    this.selectCount = selectCount;
    this.fromTables = fromTables;
    this.initWhere = initWhere;
    this.sortField = sortField;
    this.sortReverse = sortReverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.GenericDataProvider
   * #getMessageDataList(com.insta.hms.messaging.MessageContext)
   */
  @Override
  public List<Map> getMessageDataList(MessageContext thisCtx) throws SQLException, ParseException {

    Map displayParams = null;
    if (null != thisCtx) {
      displayParams = thisCtx.getDisplayParams();
    }

    List<Map> resultList = null;
    Connection con = null;
    SearchQueryBuilder builder = null;
    try {
      con = DataBaseUtil.getConnection();
      if (null != displayParams) {
        builder = new SearchQueryBuilder(con, selectFields, selectCount, fromTables, initWhere,
            displayParams);
      } else {
        builder = new SearchQueryBuilder(con, selectFields, selectCount, fromTables, initWhere,
            null, false, 0, 0);
      }

      if (null != criteriaFilter && criteriaFilter.size() > 0) {
        builder.addFilterFromParamMap(criteriaFilter);
      }

      if (null != secSortColList && secSortColList.size() > 0) {
        for (String sortField : secSortColList) {
          builder.addSecondarySort(sortField);
        }
      }

      builder.build();
      PagedList pagedList = builder.getMappedPagedList();

      logger.debug("Query " + builder.getDataQueryString());
      if (null != thisCtx) {
        thisCtx.put("totalRecords", pagedList.getTotalRecords());
        thisCtx.put("pageNumber", pagedList.getPageNumber());
        thisCtx.put("numPages", pagedList.getPageSize() > 0 ? pagedList.getNumPages() : 1);
      }
      resultList = pagedList.getDtoList();
    } finally {
      if (null != builder) {
        builder.close();
      }
      if (null != con) {
        con.close();
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Exiting getMessageDataList, no. of records: " + resultList.size());
    }
    return resultList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MessageDataProvider#getTokens()
   */
  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = new ArrayList();
    Connection con = null;
    SearchQueryBuilder builder = null;
    PreparedStatement psData = null;
    ResultSet rsData = null;
    try {
      con = DataBaseUtil.getConnection();
      builder = new SearchQueryBuilder(con, selectFields, "", fromTables, initWhere, null,
          sortField, sortReverse, 1, 1);
      builder.build();

      // SearchQueryBuilder.getMappedPagedList functionality here since we dont need count statement
      // to execute
      psData = builder.getDataStatement();

      rsData = psData.executeQuery();

      RowSetDynaClass rsd = new RowSetDynaClass(rsData);
      List dataList = rsd.getRows();

      List<Map> list = dataList != null ? ConversionUtils.copyListDynaBeansToMap(dataList) : null;
      ;
      if (null != list && list.size() > 0) {
        List customFieldList = new ArrayList();
        Map bean = list.get(0);
        Set keySet = bean.keySet();
        if (null != keySet) {
          for (String field : STOCK_FIELDS) {
            // Put the pre-defined keys first so that there is some order.
            if (keySet.contains(field)) {
              tokenList.add(field);
            }
          }

          for (String key : (Set<String>) bean.keySet()) {
            if (key.equalsIgnoreCase(KEY_FIELD_NAME) || // This is always excluded in the token map
                tokenList.contains(key) || // We have already added this
                key.endsWith("__")) { // __ at the end indicates that the field is used in
              // searching but should not be available in the token map
              continue;
            }
            customFieldList.add(key);
          }

          // sort the custom fields and add them after the stock fields
          Collections.sort(customFieldList); 
          tokenList.addAll(customFieldList);
        }
      }
    } finally {
      if (null != builder) {
        builder.close();
      }
      if (null != con) {
        con.close();
      }
      if (null != rsData) {
        rsData.close();
      }
    }

    logger.debug("Exiting getTokens()...token list "
        + ((null != tokenList) ? "size is " + tokenList.size() : "is null"));
    return tokenList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MessageDataProvider#getName()
   */
  public String getName() {
    return name;
  }
}

package com.insta.hms.mdm.stores;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.mdm.bulk.BulkDataRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** The Class StoreRepository. */
@Repository
public class StoreRepository extends BulkDataRepository<Integer> {

  /** Instantiates a new store repository. */
  public StoreRepository() {
    super("stores", "dept_id", "dept_name");
  }

  /**
   * Get the search query.
   *
   * @return the search query
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(STORE_TABLES);
  }

  /** The store fields. */
  private static final String STORE_FIELDS = " select *";

  /** The store count. */
  private static final String STORE_COUNT = " SELECT count(dept_name) ";

  /** The store tables. */
  private static final String STORE_TABLES =
      " from ( select gd.dept_id, gd.dept_name, "
          + "gd.counter_id, gd.status, gd.purchases_store_vat_account_prefix, "
          + "gd.purchases_store_cst_account_prefix, gd.sales_store_vat_account_prefix, "
          + "c.counter_no, gd.center_id, hcm.center_name,gd.allow_auto_po_generation "
          + "FROM stores gd LEFT JOIN hospital_center_master hcm ON (hcm.center_id = gd.center_id) "
          + "LEFT OUTER JOIN counters c ON (gd.counter_id = c.counter_id AND "
          + "gd.center_id = c.center_id)) AS dept ";

  /**
   * Gets the center user store details.
   *
   * @param requestParams the request params
   * @param listingParams the listing params
   * @param centerId the center id
   * @return the center user store details
   * @throws ParseException the parse exception
   */
  public PagedList getCenterUserStoreDetails(Map requestParams, Map listingParams, int centerId)
      throws ParseException {

    SearchQueryAssembler qa = new SearchQueryAssembler(STORE_FIELDS, STORE_COUNT, STORE_TABLES);
    qa.addFilterFromParamMap(requestParams);
    if (centerId != 0) {
      qa.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", new Integer(centerId));
    }
    qa.addSecondarySort("dept_id");
    qa.build();
    return qa.getMappedPagedList();
  }

  /** The Constant STORE_DETAILS. */
  private static final String STORE_DETAILS =
      "SELECT s.*, hcm.center_name, hcm.status, "
          + "c.counter_no"
          + " FROM stores s "
          + "JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id) "
          + " LEFT JOIN counters c ON "
          + "(s.counter_id = c.counter_id AND s.center_id = c.center_id) WHERE s.dept_id=?";

  /**
   * Get the store view query.
   *
   * @return the view query
   * @see com.insta.hms.mdm.MasterRepository#getViewQuery()
   */
  @Override
  public String getViewQuery() {
    return STORE_DETAILS;
  }

  /**
   * Gets the store details.
   *
   * @param storeId the store id
   * @return the store details
   */
  public BasicDynaBean getStoreDetails(Integer[] storeId) {

    return DatabaseHelper.queryToDynaBean(STORE_DETAILS, Arrays.asList(storeId).toArray());
  }

  /** The Constant STORES_NAMESAND_iDS. */
  // TODO - Replace with Lookup Query.
  private static final String STORES_NAMESAND_iDS = "SELECT dept_name,dept_id FROM  stores";

  /**
   * Gets the stores names and ids.
   *
   * @return the stores names and ids
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getStoresNamesAndIds() {

    return ConversionUtils.copyListDynaBeansToMap(
        DatabaseHelper.queryToDynaList(STORES_NAMESAND_iDS));
  }

  /** The account group details for counter. */
  private static final String ACCOUNT_GROUP_DETAILS_FOR_COUNTER =
      " SELECT COUNT(account_group), "
          + "account_group, account_group_name FROM stores "
          + "JOIN account_group_master gm ON gm.account_group_id=account_group "
          + "WHERE counter_id=? AND account_group!=? and dept_id!=? GROUP BY account_group, "
          + "account_group_name ";

  /**
   * Gets the account group details.
   *
   * @param counterId the counter id
   * @param accountGroup the account group
   * @param departmenttId the departmentt id
   * @return the account group details
   */
  public List<BasicDynaBean> getAccountGroupDetails(
      String counterId, Integer accountGroup, Integer departmenttId) {
    if (counterId == null || counterId.isEmpty() || departmenttId == null) {
      return null;
    }

    return DatabaseHelper.queryToDynaList(
        ACCOUNT_GROUP_DETAILS_FOR_COUNTER, new Object[] {counterId, accountGroup, departmenttId});
  }

  /** The Constant GET_DIAGNOSTICS_STORE_DEPENDENTS. */
  private static final String GET_DIAGNOSTICS_STORE_DEPENDENTS =
      " SELECT * FROM diagnostics_departments d "
          + " JOIN diagnostic_department_stores ds ON (d.ddept_id = ds.ddept_id) "
          + " WHERE ds.store_id = ?  AND d.status = 'A' ";

  /**
   * Gets the diagnostics store dependents.
   *
   * @param storeId the store id
   * @return the diagnostics store dependents
   */
  public List<BasicDynaBean> getDiagnosticsStoreDependents(Integer storeId) {
    return DatabaseHelper.queryToDynaList(GET_DIAGNOSTICS_STORE_DEPENDENTS, new Object[] {storeId});
  }

  /** The Constant GET_USER_DEFAULT_STORE_DEPENDENTS. */
  private static final String GET_USER_DEFAULT_STORE_DEPENDENTS =
      " SELECT * FROM u_user " + "WHERE pharmacy_store_id = ? ";

  /**
   * Gets the user default store dependents.
   *
   * @param storeId the store id
   * @return the user default store dependents
   */
  public List<BasicDynaBean> getUserDefaultStoreDependents(String storeId) {
    return DatabaseHelper.queryToDynaList(
        GET_USER_DEFAULT_STORE_DEPENDENTS, new Object[] {storeId});
  }

  /** The Constant GET_USER_MULTI_STORE_DEPENDENTS. */
  private static final String GET_USER_MULTI_STORE_DEPENDENTS =
      "SELECT multi_store from u_user where multi_store IS NOT NULL AND multi_store != ''"
      + "AND ? = any(string_to_array(replace(multi_store, ' ', ''), ','));";

  /**
   * Gets the user multi store dependents.
   *
   * @param storeId the store id
   * @return the user multi store dependents
   */
  public List<BasicDynaBean> getUserMultiStoreDependents(String storeId) {
    return DatabaseHelper.queryToDynaList(GET_USER_MULTI_STORE_DEPENDENTS, new Object[] {storeId});
  }

  /** The Constant GET_CENTER_STORES. */
  private static final String GET_CENTER_STORES =
      " SELECT user_stores.dept_id, s.* "
          + "  FROM (SELECT regexp_split_to_table(multi_store, E'\\,')::integer as dept_id "
          + "    FROM u_user "
          + "     WHERE emp_username=?) as user_stores "
          + "  JOIN stores s ON (s.dept_id=user_stores.dept_id) "
          + "  JOIN hospital_center_master hcm ON (s.center_id=hcm.center_id) ";

  /**
   * Find by user.
   *
   * @param userName the user name
   * @return the list
   */
  public List<BasicDynaBean> findByUser(String userName) {
    return DatabaseHelper.queryToDynaList(GET_CENTER_STORES, new Object[] {userName});
  }
}

package com.insta.hms.mdm.counters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CounterRepository.
 *
 * @author yashwant
 */
@Repository
class CounterRepository extends MasterRepository<String> {

  /**
   * Instantiates a new counter repository.
   */
  public CounterRepository() {
    super("counters", "counter_id", "counter_no");
  }

  /** The Constant COUNTERS_SEARCH_TABLES. */
  private static final String COUNTERS_SEARCH_TABLES = " FROM ( SELECT c.*, hcm.center_name "
      + " FROM counters c " + " JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) "
      + " ) AS foo ";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(COUNTERS_SEARCH_TABLES);
  }

  /** The Constant PHARMACY_ACTIVE_COUNTERS. */
  private static final String PHARMACY_ACTIVE_COUNTERS = "SELECT c.counter_id, counter_no,"
      + " counter_type, c.center_id, hcm.center_name, s.account_group from counters c  "
      + " JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id and hcm.status = 'A') "
      + " Left JOIN stores s ON (s.counter_id = c.counter_id and s.center_id = c.center_id) "
      + " where counter_type='P' and c.status='A' "
      + " Group by c.counter_id, counter_no, counter_type, c.center_id,"
      + " hcm.center_name, s.account_group";

  /**
   * Gets the pharmacy active counters.
   *
   * @return the pharmacy active counters
   */
  public List<BasicDynaBean> getPharmacyActiveCounters() {
    return DatabaseHelper.queryToDynaList(PHARMACY_ACTIVE_COUNTERS);
  }

  /** The Constant GET_ALL_COUNTERS. */
  private static final String GET_ALL_COUNTERS = "select counter_id,counter_no from counters";

  /**
   * Gets the all counters.
   *
   * @return the all counters
   */
  public List<BasicDynaBean> getAllCounters() {
    return DatabaseHelper.queryToDynaList(GET_ALL_COUNTERS);
  }

  /** The Constant GET_COUNTER. */
  private static final String GET_COUNTER = " SELECT c.*, hcm.center_name " + " FROM counters c "
      + " JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) "
      + " WHERE c.counter_id = ? ";
  
  @Override
  public String getViewQuery() {
    return GET_COUNTER;
  }
  
  /**
  * Gets the lookup query.
  *
  * @return the lookup query
  */
  @Override
  public String getLookupQuery() {
    return "SELECT * " + COUNTERS_SEARCH_TABLES;
  }
  
  private static final String GET_ACTIVE_BILLING_COUNTERS = "SELECT counter_id,counter_no "
      + " FROM counters WHERE center_id = ? AND counter_type = 'B' AND status = 'A' ";

  public List<BasicDynaBean> getActiveBillingCounters(Integer centerId) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_BILLING_COUNTERS, (Object) centerId);
  }
  
  private static final String GET_ALL_ACTIVE_BILLING_COUNTERS = "SELECT counter_id,counter_no, "
      + " center_id FROM counters WHERE counter_type = 'B' AND status = 'A' ";

  public List<BasicDynaBean> getAllActiveBillingCounters() {
    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_BILLING_COUNTERS);
  }
  
  
  /** The Constant PHARMACY_ACTIVE_COUNTERS. */
  private static final String ALL_ACTIVE_COUNTERS = "SELECT c.counter_id, counter_no, "
      + " counter_type, c.center_id, hcm.center_name from counters c  "
      + " JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id and hcm.status = 'A') "
      + " where c.status='A' ";
  
  /**
   * Gets the all counters.
   *
   * @return the all counters
   */
  public List<BasicDynaBean> getAllActiveCounters(Integer centerId) {
    if (centerId != 0) {
      String query = ALL_ACTIVE_COUNTERS + " AND c.center_id = ?";
      return DatabaseHelper.queryToDynaList(query, new Object[] {centerId});
    }
    return DatabaseHelper.queryToDynaList(ALL_ACTIVE_COUNTERS);
  }
  
}

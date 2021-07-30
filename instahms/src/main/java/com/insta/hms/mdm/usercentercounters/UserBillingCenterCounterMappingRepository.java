package com.insta.hms.mdm.usercentercounters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserBillingCenterCounterMappingRepository extends MasterRepository<Integer> {

  public UserBillingCenterCounterMappingRepository() {
    super(new String[] { "emp_username", "center_id", "counter_id" }, null,
        "user_center_billing_counters", "center_counter_id");
  }

  private static final String GET_COUNTER_CENTER_MAPPED_LIST = " SELECT ucbc.center_counter_id, "
      + " ucbc.center_id,ucbc.counter_id,ucbc.default_counter, emp_username, "
      + " hcm.center_name,counter.counter_no, created_by " 
      + " FROM user_center_billing_counters  ucbc "
      + " LEFT JOIN hospital_center_master hcm ON (ucbc.center_id = hcm.center_id) "
      + " LEFT JOIN counters counter ON (ucbc.counter_id = counter.counter_id "
      + " AND counter.counter_type = 'B') " 
      + " WHERE ucbc.emp_username = ? ";

  public List<BasicDynaBean> getMappedCounterList(String userName) {
    return DatabaseHelper.queryToDynaList(GET_COUNTER_CENTER_MAPPED_LIST,userName);
  }
  
  private static final String COUNTER_QUERY = "SELECT ucbc.counter_id, counter_no "
      + " FROM user_center_billing_counters ucbc "
      + " JOIN counters c ON (ucbc.counter_id = c.counter_id) "
      + " WHERE emp_username = ? AND ucbc.center_id = ?";

  public BasicDynaBean getMappedCounterForCenter(String userName, Integer centerId) {
    return DatabaseHelper.queryToDynaBean(COUNTER_QUERY, userName, centerId);
  }

}
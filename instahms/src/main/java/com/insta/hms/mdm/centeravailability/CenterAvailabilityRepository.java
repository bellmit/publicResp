package com.insta.hms.mdm.centeravailability;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class CenterAvailabilityRepository extends MasterRepository<Integer> {

  public CenterAvailabilityRepository() {
    super("center_availability", "center_availability_id");
  }

  private static final String GET_WEEK_TIMING = "select min(from_time) as from_time, max(to_time)"
      + " as to_time from center_availability where center_id = ? and availability_status='Y'";

  public BasicDynaBean getWeekTimings(Integer centerId) {
    return DatabaseHelper.queryToDynaBean(GET_WEEK_TIMING, new Object[] { centerId });
  }
}
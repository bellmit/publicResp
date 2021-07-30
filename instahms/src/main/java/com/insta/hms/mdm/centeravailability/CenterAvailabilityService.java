package com.insta.hms.mdm.centeravailability;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CenterAvailabilityService.
 */
@Service
public class CenterAvailabilityService extends MasterService {

  /** The repository. */
  @LazyAutowired
  private CenterAvailabilityRepository repository;

  /**
   * Instantiates a new center availability service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public CenterAvailabilityService(CenterAvailabilityRepository repository,
      CenterAvailabilityValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the center timings.
   *
   * @return the center timings
   */
  public Map<String, Object> getCenterTimings() {
    Map<String, Object> timingDataMap = new HashMap<>();
    Map<String, Object> filterMap = new HashMap<>();
    Integer centerId = RequestContext.getCenterId();
    filterMap.put("center_id", centerId);
    filterMap.put("availability_status", "Y");
    List<BasicDynaBean> dayWiseTimingList = repository.findByCriteria(filterMap);
    if (dayWiseTimingList.isEmpty()) {
      centerId = 0;
      filterMap.put("center_id", centerId);
      dayWiseTimingList = repository.findByCriteria(filterMap);
    }
    for (BasicDynaBean dayTimings : dayWiseTimingList) {
      timingDataMap.put(String.valueOf(dayTimings.get("day_of_week")), dayTimings.getMap());
    }
    timingDataMap.put("week", getCenterWeekTimings(centerId));
    return timingDataMap;
  }

  /**
   * Gets the center week timings.
   *
   * @param centerId the center id
   * @return the center week timings
   */
  public Map getCenterWeekTimings(Integer centerId) {
    return repository.getWeekTimings(centerId).getMap();
  }
}
package com.insta.hms.mdm.servingfrequency;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class ServingFrequencyService.
 */
@Service
public class ServingFrequencyService {

  /** The repo. */
  @LazyAutowired
  private ServingFrequencyRepository repo;

  /**
   * Find by recurrence daily id.
   *
   * @param recurrenceDailyId the recurrence daily id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByRecurrenceDailyId(Integer recurrenceDailyId) {
    return repo.findByKey("recurrence_daily_id", recurrenceDailyId);
  }

  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap ) {
    return repo.listAll(columns, filterMap, null);
  }
}

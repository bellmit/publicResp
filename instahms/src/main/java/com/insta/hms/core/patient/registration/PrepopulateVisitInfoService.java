package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The Class PrepopulateVisitInfoService.
 */
@Service
public class PrepopulateVisitInfoService {

  /** The prepopulate visit info repository. */
  @LazyAutowired
  private PrepopulateVisitInfoRepository prepopulateVisitInfoRepository;

  /**
   * Insert.
   *
   * @param activityBean
   *          the activity bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean activityBean) {
    return prepopulateVisitInfoRepository.insert(activityBean);
  }

  /**
   * Update.
   *
   * @param activityBean
   *          the activity bean
   * @param keys
   *          the keys
   * @return the integer
   */
  public Integer update(BasicDynaBean activityBean, Map<String, Object> keys) {
    return prepopulateVisitInfoRepository.update(activityBean, keys);
  }

}

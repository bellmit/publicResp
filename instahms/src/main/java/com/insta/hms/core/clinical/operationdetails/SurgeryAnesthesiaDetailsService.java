package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class SurgeryAnesthesiaDetailsService.
 */
@Service
public class SurgeryAnesthesiaDetailsService {

  /** The repository. */
  @LazyAutowired
  private SurgeryAnesthesiaDetailsRepository repository;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return repository.getBean();
  }

  /**
   * Batch insert.
   *
   * @param beans
   *          the beans
   * @return the int[]
   */
  public int[] batchInsert(List<BasicDynaBean> beans) {
    return repository.batchInsert(beans);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return repository.getNextSequence();
  }

  /**
   * Batch update.
   *
   * @param beans
   *          the beans
   * @param keys
   *          the keys
   * @return the int[]
   */
  public int[] batchUpdate(List<BasicDynaBean> beans, Map<String, Object> keys) {
    return repository.batchUpdate(beans, keys);
  }

}

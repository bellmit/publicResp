package com.insta.hms.extension.clinical.ivf;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

// TODO: Auto-generated Javadoc
/**
 * The Class IVFCycleService.
 */
@Service
public class IVFCycleService {

  /** The ivf cycle repo. */
  @LazyAutowired
  private IVFCycleRepository ivfCycleRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ivfCycleRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return ivfCycleRepo.insert(bean);
  }
  
  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return ivfCycleRepo.getNextSequence();
  }

}

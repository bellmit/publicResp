package com.insta.hms.core.clinical.operationbillableresources;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class OperationBillableResourcesService.
 */
@Service
public class OperationBillableResourcesService {

  /** The op billable resource repo. */
  @LazyAutowired
  private OperationBillableResourcesRepository opBillableResourceRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return opBillableResourceRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param bean
   *          the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return opBillableResourceRepo.insert(bean);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return opBillableResourceRepo.getNextSequence();
  }

  /**
   * Batch insert.
   *
   * @param beans
   *          the beans
   * @return the int[]
   */
  public int[] batchInsert(List<BasicDynaBean> beans) {
    return opBillableResourceRepo.batchInsert(beans);
  }

}

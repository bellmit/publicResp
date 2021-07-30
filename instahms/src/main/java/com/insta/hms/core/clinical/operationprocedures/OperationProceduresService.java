package com.insta.hms.core.clinical.operationprocedures;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class OperationProceduresService.
 */
@Service
public class OperationProceduresService {

  /** The op procedures repo. */
  @LazyAutowired
  private OperationProceduresRepository opProceduresRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return opProceduresRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param bean
   *          the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return opProceduresRepo.insert(bean);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return opProceduresRepo.getNextSequence();
  }

  /**
   * Find by key.
   *
   * @param keyField
   *          the key field
   * @param value
   *          the value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyField, Integer value) {
    return opProceduresRepo.findByKey(keyField, value);
  }

}

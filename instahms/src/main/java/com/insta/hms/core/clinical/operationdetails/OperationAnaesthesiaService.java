package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class OperationAnaesthesiaService.
 *
 * @author anup vishwas
 */

@Service
public class OperationAnaesthesiaService {

  /** The operation anaesthesia repo. */
  @LazyAutowired
  private OperationAnaesthesiaRepository operationAnaesthesiaRepo;

  /**
   * Gets the operation anaesthesia details.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation anaesthesia details
   */
  public List<BasicDynaBean> getOperationAnaesthesiaDetails(Integer opDetailsId) {
    return operationAnaesthesiaRepo.getOperationAnaesthesiaDetails(opDetailsId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return operationAnaesthesiaRepo.getBean();
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return operationAnaesthesiaRepo.getNextSequence();
  }

  /**
   * Batch insert.
   *
   * @param beans
   *          the beans
   * @return the int[]
   */
  public int[] batchInsert(List<BasicDynaBean> beans) {
    return operationAnaesthesiaRepo.batchInsert(beans);
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
    return operationAnaesthesiaRepo.batchUpdate(beans, keys);
  }

}

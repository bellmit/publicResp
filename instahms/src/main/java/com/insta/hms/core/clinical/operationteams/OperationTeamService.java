package com.insta.hms.core.clinical.operationteams;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class OperationTeamService.
 */
@Service
public class OperationTeamService {

  /** The operation teams repo. */
  @LazyAutowired
  private OperationTeamRepository operationTeamsRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return operationTeamsRepo.getBean();
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return operationTeamsRepo.getNextSequence();
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return operationTeamsRepo.insert(bean);
  }

}

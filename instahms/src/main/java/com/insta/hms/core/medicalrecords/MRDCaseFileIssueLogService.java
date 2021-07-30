package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class MRDCaseFileIssueLogService.
 */
@Service
public class MRDCaseFileIssueLogService {

  /** The mrd case file issue log repo. */
  @LazyAutowired
  private MRDCaseFileIssueLogRepository mrdCaseFileIssueLogRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return mrdCaseFileIssueLogRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param bean
   *          the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return mrdCaseFileIssueLogRepo.insert(bean);
  }

}

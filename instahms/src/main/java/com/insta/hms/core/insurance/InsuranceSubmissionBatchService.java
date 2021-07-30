package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class InsuranceSubmissionBatchService.
 */
@Service
public class InsuranceSubmissionBatchService {

  /** The ins subm batch repo. */
  @LazyAutowired
  private InsuranceSubmissionBatchRepository insSubmBatchRepo;

  /**
   * Find by key.
   *
   * @param submissionBatchID the submission batch ID
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String submissionBatchID) {
    return insSubmBatchRepo.findByKey("submission_batch_id", submissionBatchID);
  }

  /**
   * Find submission batch.
   *
   * @param submissionBatchId the submission batch id
   * @return the basic dyna bean
   */
  public BasicDynaBean findSubmissionBatch(String submissionBatchId) {
    return insSubmBatchRepo.findSubmissionBatch(submissionBatchId);
  }

  /**
   * Update.
   *
   * @param subBatchBean      the sub batch bean
   * @param submissionBatchId the submission batch id
   * @return the boolean
   */
  public Boolean update(BasicDynaBean subBatchBean, String submissionBatchId) {
    Map<String, Object> keys = new HashMap<>();
    keys.put("submission_batch_id", submissionBatchId);

    return insSubmBatchRepo.update(subBatchBean, keys) >= 0;
  }
}

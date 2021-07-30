package com.insta.hms.core.insurance;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.insurance.submission.ClaimSubmissionsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ClaimSubmissionService.
 */
@Service
public class ClaimSubmissionService {

  /** The claim sub repo. */
  @LazyAutowired
  private ClaimSubmissionsRepository claimSubRepo;

  /** The ins sub batch repo. */
  @LazyAutowired
  private InsuranceSubmissionRepository insuranceSubmissionRepository;

  @LazyAutowired
  private ClaimProcessor claimProcessor;

  /**
   * Mark claim as sent.
   *
   * @param submissionBatchID the submission batch ID
   */
  public void markClaimAsSent(String submissionBatchID) {
    BasicDynaBean subBatchBean = insuranceSubmissionRepository.findByKey("submission_batch_id",
        submissionBatchID);

    Map keys = new HashMap();
    keys.put("submission_batch_id", submissionBatchID);

    subBatchBean.set("status", "S");
    subBatchBean.set("submission_date", new Timestamp(new java.util.Date().getTime()));
    subBatchBean.set("is_manual_sent_batch", true);

    insuranceSubmissionRepository.update(subBatchBean, keys);

    claimSubRepo.updateClaimBatchStatusToSent(submissionBatchID);
  }

  /**
   * Gets the claims in submission batch.
   *
   * @param submissionBatchID the submission batch ID
   * @return the claims in submission batch
   */
  public List<BasicDynaBean> getClaimsInSubmissionBatch(String submissionBatchID) {
    return claimSubRepo.getClaimsInSubmissionBatch(submissionBatchID);
  }

  public List<BasicDynaBean> getClaimSubmissionCountList(String submissionBatchID) {
    return claimSubRepo.getClaimSubmissionCountList(submissionBatchID);
  }

  public Integer update(BasicDynaBean bean, Map<String, Object> keys) {
    return insuranceSubmissionRepository.update(bean, keys);
  }

  public List<BasicDynaBean> getClaims(String submissionBatchId) {
    return insuranceSubmissionRepository.getClaimsBySubmissionBatchId(submissionBatchId);
  }


  public List<BasicDynaBean> getAccountGroupAndCenterType(String query, Integer centerId) {
    return claimSubRepo.getAccountingGroups(centerId , query);
  }
}

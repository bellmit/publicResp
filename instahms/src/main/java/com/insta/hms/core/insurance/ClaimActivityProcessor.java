package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Component;

/**
 * The Class ClaimActivityProcessor.
 */
@Component
public class ClaimActivityProcessor {

  @LazyAutowired
  private InsuranceSubmissionRepository insuranceSubmissionRepository;

  /**
   * Process.
   *
   * @param submissionBatchId the submission batch id
   * @param isResubmission    the is resubmission
   * @param healthAuthority   the health authority
   */
  public void process(String submissionBatchId, String isResubmission, String healthAuthority) {

    insuranceSubmissionRepository.updateClaimActivityId(submissionBatchId, isResubmission);

    insuranceSubmissionRepository.insertObservationForUnlistedItems(submissionBatchId,
        healthAuthority);
  }
}
package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.insurance.claimhistory.ClaimSubmissionHistoryService;

import org.springframework.stereotype.Service;

/**
 * The Class RemittanceHistoryService.
 */
@Service
public class RemittanceHistoryService {

  /** The insurance remittance details repo. */
  @LazyAutowired
  private InsuranceRemittanceDetailsRepository insuranceRemittanceDetailsRepo;

  /** The insurance remittance activity details repo. */
  @LazyAutowired
  private InsuranceRemittanceActivityDetailsRepository insuranceRemittanceActivityDetailsRepo;

  /** The claim submission history service. */
  @LazyAutowired
  private ClaimSubmissionHistoryService claimSubmissionHistoryService;

  /**
   * Post remittance history.
   *
   * @param remitId the remit id
   */
  public void postRemittanceHistory(int remitId) {

    claimSubmissionHistoryService.updateClaimReceivedAmount(remitId);

    claimSubmissionHistoryService.updateClaimRecoveryAmount(remitId);

    claimSubmissionHistoryService.updateCombinedActivityReceivedAmount(remitId);

    claimSubmissionHistoryService.updateCombinedActivityRecoveryAmount(remitId);
    
    claimSubmissionHistoryService.updateActivityStatusForHospital(remitId);
    
    claimSubmissionHistoryService.updateActivityStatusForPharmacy(remitId);
    
    claimSubmissionHistoryService.updateRecievedAmtAndRemittanceId(remitId);
    
    claimSubmissionHistoryService.updateRecoveryAmtAndRemittanceId(remitId);

  }

}

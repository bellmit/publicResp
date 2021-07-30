package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

/**
 * The Class InsuranceService.
 */
@Service
public class InsuranceService {

  /** The ic repository. */
  @LazyAutowired
  InsuranceClaimRepository icRepository;

  /**
   * update status of bill_charge_claim items based on insurance_recd_amounts.
   *
   * @param remittanceId the remittance id
   */
  public void updateRemittanceStatus(Integer remittanceId) {
    icRepository.updateClosedHospClaims(remittanceId);
    icRepository.updateClosedPharClaims(remittanceId);
    // Update status to closed according to auto_close_claims_with_difference in generic
    // preferences
    icRepository.updateAutoClosed(remittanceId);
    // updates payers_ref_no from insurance_remittance_details
    icRepository.updatePayersRef(remittanceId);
  }

}

package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The Class ClaimSubmissionsService.
 */
@Service
public class ClaimSubmissionsService {

  /** The claim submissions repository. */
  @LazyAutowired
  ClaimSubmissionsRepository claimSubmissionsRepository;

  /**
   * Update received.
   *
   * @param remittanceId the remittance id
   */
  public void updateReceived(Integer remittanceId) {
    claimSubmissionsRepository.updateStatusRecvd(remittanceId);
  }

  /**
   * Update denied.
   *
   * @param remittanceId the remittance id
   */
  public void updateDenied(Integer remittanceId) {
    claimSubmissionsRepository.updateStatusDenied(remittanceId);
  }

  public BasicDynaBean findByKey(Map<String, Object> identifiers) {
    // TODO Auto-generated method stub
    return claimSubmissionsRepository.findByKey(identifiers);
  }
}

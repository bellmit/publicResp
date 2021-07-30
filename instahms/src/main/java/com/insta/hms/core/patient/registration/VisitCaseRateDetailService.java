package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

/**
 * The Class VisitCaseRateDetailService.
 */
@Service
public class VisitCaseRateDetailService {
  
  /** The visit case rate detail repo. */
  @LazyAutowired
  private VisitCaseRateDetailRepository visitCaseRateDetailRepo;

  /**
   * Delete case rate details.
   *
   * @param visitId the visit id
   * @return the boolean
   */
  public Boolean deleteCaseRateDetails(String visitId) {
    return visitCaseRateDetailRepo.delete("visit_id", visitId) >= 0;
  }

}

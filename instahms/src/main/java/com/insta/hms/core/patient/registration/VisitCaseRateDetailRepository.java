package com.insta.hms.core.patient.registration;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class VisitCaseRateDetailRepository.
 */
@Repository
public class VisitCaseRateDetailRepository extends GenericRepository {

  /**
   * Instantiates a new visit case rate detail repository.
   */
  public VisitCaseRateDetailRepository() {
    super("visit_case_rate_detail");
  }

}

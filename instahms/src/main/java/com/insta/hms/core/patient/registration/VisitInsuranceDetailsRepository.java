package com.insta.hms.core.patient.registration;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class VisitInsuranceDetailsRepository.
 */
@Repository
public class VisitInsuranceDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new visit insurance details repository.
   */
  public VisitInsuranceDetailsRepository() {
    super("visit_insurance_details_view");
  }

}

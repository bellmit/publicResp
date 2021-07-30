package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PatientInsurancePolicyDetailsRepository.
 */
@Repository
public class PatientInsurancePolicyDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new patient insurance policy details repository.
   */
  public PatientInsurancePolicyDetailsRepository() {
    super("patient_policy_details");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getNextSequence("patient_policy_details_patient_policy_id");
  }

}

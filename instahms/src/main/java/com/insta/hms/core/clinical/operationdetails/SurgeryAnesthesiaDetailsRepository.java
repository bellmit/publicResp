package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class SurgeryAnesthesiaDetailsRepository.
 */
@Repository
public class SurgeryAnesthesiaDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new surgery anesthesia details repository.
   */
  public SurgeryAnesthesiaDetailsRepository() {
    super("surgery_anesthesia_details");
  }

}

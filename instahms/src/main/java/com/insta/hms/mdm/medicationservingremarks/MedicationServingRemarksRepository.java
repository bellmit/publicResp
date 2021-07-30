package com.insta.hms.mdm.medicationservingremarks;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class MedicationServingRemarksRepository.
 */
@Repository
public class MedicationServingRemarksRepository extends GenericRepository {

  /**
   * Instantiates a new medication serving remarks repository.
   */
  public MedicationServingRemarksRepository() {
    super("medication_serving_remarks");
  }

}

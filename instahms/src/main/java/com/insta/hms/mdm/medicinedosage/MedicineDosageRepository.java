package com.insta.hms.mdm.medicinedosage;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class MedicineDosageRepository.
 */
@Repository
public class MedicineDosageRepository extends MasterRepository<String> {

  /**
   * Instantiates a new medicine dosage repository.
   */
  public MedicineDosageRepository() {
    super("medicine_dosage_master", "dosage_id", "dosage_name");
  }

}

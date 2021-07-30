package com.insta.hms.mdm.incominghospitals;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class IncomingHospitalsRepository.
 */
@Repository
public class IncomingHospitalsRepository extends MasterRepository<String> {

  /**
   * Instantiates a new incoming hospitals repository.
   */
  public IncomingHospitalsRepository() {
    super("incoming_hospitals", "hospital_id", "hospital_name");
    // TODO Auto-generated constructor stub
  }
}

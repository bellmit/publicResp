package com.insta.hms.mdm.doctors;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DoctorCenterMasterRepository.
 */
@Repository
public class DoctorCenterMasterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new doctor center master repository.
   */
  DoctorCenterMasterRepository() {
    super(new String[] {"doctor_id","center_id"}, 
        new String[] {"doctor_id","center_id","status","status_on_practo"}, 
        "doctor_center_master", 
        "doc_center_id");
  }
  
}

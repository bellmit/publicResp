package com.insta.hms.mdm.hospitalcenters;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class HospitalCenterRepository.
 */
@Repository
public class HospitalCenterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new hospital center repository.
   */
  public HospitalCenterRepository() {
    super("hospital_center_master", "center_id");
  }
}

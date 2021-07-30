package com.insta.hms.mdm.bloodgroup;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BloodGroupRepository extends MasterRepository<String> {

  /**
   * Instantiates a new blood group repository.
   */
  public BloodGroupRepository() {
    super("blood_group_master", "blood_group_id", "blood_group_name",
        new String[] { "blood_group_id", "blood_group_name" });
  }
}

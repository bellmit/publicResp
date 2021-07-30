package com.insta.hms.mdm.practitionertypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PractitionerTypeRepository extends MasterRepository<Integer> {

  /**
   * constructor.
   */
  public PractitionerTypeRepository() {
    super("practitioner_types", "practitioner_id");
    // TODO Auto-generated constructor stub
  }
}

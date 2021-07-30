package com.insta.hms.mdm.complainttypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ComplaintTypesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new complaint types repository.
   */
  public ComplaintTypesRepository() {
    super(
        "complaint_type_master",
        "complaint_type_id",
        "complaint_type",
        new String[] {"duration", "complaint_type_id", "complaint_type"});
  }
}

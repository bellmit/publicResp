package com.insta.hms.mdm.dialysisaccesstypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DialysisAccessTypeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new dialysis access type repository.
   */
  public DialysisAccessTypeRepository() {
    super("dialysis_access_types", "access_type_id", "access_type");
  }

}

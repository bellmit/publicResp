package com.insta.hms.mdm.allergy;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class AllergyTypeRepository extends MasterRepository<Integer> {

  public AllergyTypeRepository() {
    super("allergy_type_master", "allergy_type_id", "allergy_type_code");
  }
}

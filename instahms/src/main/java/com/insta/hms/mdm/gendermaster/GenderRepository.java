package com.insta.hms.mdm.gendermaster;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

@Repository
public class GenderRepository extends MasterRepository<String> {

  public GenderRepository() {
    super("gender_master", "gender_id", "gender_name",
        new String[] {"gender_id", "gender_name"});
  }
}

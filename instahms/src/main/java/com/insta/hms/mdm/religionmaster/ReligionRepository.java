package com.insta.hms.mdm.religionmaster;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReligionRepository extends MasterRepository<Integer> {


  public ReligionRepository() {
    super("religion_master", "religion_id", "religion_name",
        new String[] {"religion_id", "religion_name"});
  }
}

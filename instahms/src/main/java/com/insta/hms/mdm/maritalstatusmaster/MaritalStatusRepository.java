package com.insta.hms.mdm.maritalstatusmaster;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MaritalStatusRepository extends MasterRepository<Integer> {

  public MaritalStatusRepository() {
    super("marital_status_master", "marital_status_id", "marital_status_name",
        new String[]{"marital_status_id", "marital_status_name"});
  }
}

package com.insta.hms.mdm.rateplan;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;


@Repository
public class RatePlanRepository extends MasterRepository<String> {

  public RatePlanRepository() {
    super("organization_details", "org_id", "org_name");
    // TODO Auto-generated constructor stub
  }

}

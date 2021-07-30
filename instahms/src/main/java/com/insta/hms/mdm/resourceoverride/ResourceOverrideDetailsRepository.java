package com.insta.hms.mdm.resourceoverride;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ResourceOverrideDetailsRepository extends MasterRepository<Integer> {

  public ResourceOverrideDetailsRepository() {
    super("sch_resource_availability_details", "res_avail_details_id");
  }

}

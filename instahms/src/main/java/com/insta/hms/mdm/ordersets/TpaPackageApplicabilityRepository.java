package com.insta.hms.mdm.ordersets;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TpaPackageApplicabilityRepository extends MasterRepository<Integer> {

  public TpaPackageApplicabilityRepository() {
    super("tpa_package_applicability", "tpa_package_id");
  }

}

package com.insta.hms.mdm.packages;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PatientCustomisedPackageDetailsRepository extends GenericRepository {

  public PatientCustomisedPackageDetailsRepository() {
    super("patient_customised_package_details");
  }
}

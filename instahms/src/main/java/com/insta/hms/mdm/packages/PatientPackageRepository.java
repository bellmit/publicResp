package com.insta.hms.mdm.packages;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PatientPackageRepository extends MasterRepository<Integer> {

  public PatientPackageRepository() {
    super("patient_package", "patient_package_id");
  }
}

package com.insta.hms.mdm.packages;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PatientPackageContentsRepository extends GenericRepository {

  public PatientPackageContentsRepository() {
    super("patient_package_contents");
  }
}

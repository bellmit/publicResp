package com.insta.hms.mdm.packages;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PatientPackageContentChargesRepository extends GenericRepository {

  public PatientPackageContentChargesRepository() {
    super("patient_package_content_charges");
  }
}

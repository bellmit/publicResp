package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class NonHospitalPrescriptionsRepository extends GenericRepository {

  public NonHospitalPrescriptionsRepository() {
    super("patient_other_prescriptions");
  }

}

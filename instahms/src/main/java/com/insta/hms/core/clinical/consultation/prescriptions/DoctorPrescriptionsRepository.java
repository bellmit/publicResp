package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DoctorPrescriptionsRepository extends GenericRepository {

  public DoctorPrescriptionsRepository() {
    super("patient_consultation_prescriptions");
  }

}

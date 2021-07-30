package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ServicePrescriptionsRepository extends GenericRepository {

  public ServicePrescriptionsRepository() {
    super("patient_service_prescriptions");
  }

}

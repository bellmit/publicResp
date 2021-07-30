package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class OperationPrescriptionsRepository extends GenericRepository {

  public OperationPrescriptionsRepository() {
    super("patient_operation_prescriptions");
  }

}

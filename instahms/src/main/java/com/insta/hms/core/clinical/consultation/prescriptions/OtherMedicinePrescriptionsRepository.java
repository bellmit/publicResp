package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class OtherMedicinePrescriptionsRepository extends GenericRepository {

  public OtherMedicinePrescriptionsRepository() {
    super("patient_other_medicine_prescriptions");
  }

}

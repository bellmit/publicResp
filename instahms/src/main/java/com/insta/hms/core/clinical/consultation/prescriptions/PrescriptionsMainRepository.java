package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionsMainRepository extends GenericRepository {

  public PrescriptionsMainRepository() {
    super("patient_prescriptions_main");
  }

}

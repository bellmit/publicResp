package com.insta.hms.core.clinical.discharge;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PatientDischargeRepository extends GenericRepository {

  public PatientDischargeRepository() {
    super("patient_discharge");
  }

}

package com.insta.hms.core.clinical.dischargemedication;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DischargeMedicationDetailsRepository extends GenericRepository {

  public DischargeMedicationDetailsRepository() {
    super("discharge_medication_details");
  }
}

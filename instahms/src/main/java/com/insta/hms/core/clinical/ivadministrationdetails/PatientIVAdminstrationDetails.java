package com.insta.hms.core.clinical.ivadministrationdetails;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PatientIVAdminstrationDetails extends GenericRepository {
  
  public PatientIVAdminstrationDetails() {
    super("patient_iv_administered_details");
  }

}

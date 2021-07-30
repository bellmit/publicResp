package com.insta.hms.mdm.prescriptionsdeclinedreasonmaster;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;


@Repository
public class PrescDeclinedReasonRepository extends MasterRepository<Integer> {

  public PrescDeclinedReasonRepository() {
    super("pending_prescription_declined_reasons", "declined_reason_id", "reason", 
        new String[] { "declined_reason_id", "reason"});
  }

}

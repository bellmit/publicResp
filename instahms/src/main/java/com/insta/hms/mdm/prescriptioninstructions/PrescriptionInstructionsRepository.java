package com.insta.hms.mdm.prescriptioninstructions;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Prescription Instruction Repository.
 */
@Repository
public class PrescriptionInstructionsRepository extends MasterRepository<Integer> {

  public PrescriptionInstructionsRepository() {
    super("presc_instr_master", "instruction_id");
  }
}

package com.insta.hms.mdm.prescriptioninstructions;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prescription Instruction Service.
 */
@Service
public class PrescriptionInstructionsService extends MasterService {
  public PrescriptionInstructionsService(
      PrescriptionInstructionsRepository presInstructionsRepository,
      PrescriptionInstructionsValidator presInstructionsValidator) {
    super(presInstructionsRepository, presInstructionsValidator);
  }

  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }
}

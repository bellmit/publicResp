package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientOtherMedicinePrescriptionsService.
 *
 * @author anup vishwas
 */

@Service
public class PatientOtherMedicinePrescriptionsService {

  /** The other medicine repo. */
  @LazyAutowired
  private PatientOtherMedicinePrescriptionsRepository otherMedicineRepo;

  /**
   * Gets the other prescribed medicines.
   *
   * @param consultationId the consultation id
   * @return the other prescribed medicines
   */
  public List<BasicDynaBean> getOtherPrescribedMedicines(int consultationId) {
    return otherMedicineRepo.getOtherPrescribedMedicines(consultationId);
  }
}

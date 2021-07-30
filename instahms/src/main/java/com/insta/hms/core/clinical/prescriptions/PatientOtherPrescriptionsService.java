package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientOtherPrescriptionsService.
 *
 * @author anup vishwas
 */

@Service
public class PatientOtherPrescriptionsService {

  /** The Patient other prescriptions repo. */
  @LazyAutowired
  private PatientOtherPrescriptionsRepository patientOtherPrescriptionsRepo;

  /**
   * Gets the presc others for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc others for consultation
   */
  public List<BasicDynaBean> getPrescOthersForConsultation(int consultationId) {

    return patientOtherPrescriptionsRepo.getPrescOthersForConsultation(consultationId);
  }

  /**
   * Gets the presc other medicines for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc other medicines for consultation
   */
  public List<BasicDynaBean> getPrescOtherMedicinesForConsultation(int consultationId) {

    return patientOtherPrescriptionsRepo.getPrescOtherMedicinesForConsultation(consultationId);
  }

}

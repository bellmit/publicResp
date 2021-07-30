package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientOperationPrescriptionsService.
 *
 * @author anup vishwas
 */
@Service
public class PatientOperationPrescriptionsService {

  /** The patient operation presc repo. */
  @LazyAutowired
  private PatientOperationPrescriptionsRepository patientOperationPrescRepo;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * Gets the presc operations for consultation.
   *
   * @param consultationId the consultation id
   * @param orgId the org id
   * @param bedType the bed type
   * @return the presc operations for consultation
   */
  public List<BasicDynaBean> getPrescOperationsForConsultation(int consultationId, String orgId,
      String bedType) {

    return patientOperationPrescRepo.getPrescOperationsForConsultation(consultationId, orgId,
        bedType);
  }

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    return preAuthItemsService
        .filterByValidity(patientOperationPrescRepo.getPrescriptions(patientId));
  }

  /**
   * Gets the prescriptions.
   *
   * @param consultationId the consultation id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptionsByConsultation(Integer consultationId) {
    return preAuthItemsService
        .filterByValidity(patientOperationPrescRepo
            .getPrescriptionsByConsultationId(consultationId));
  }
}

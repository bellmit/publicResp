package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientConsultationPrescriptionsService.
 */
@Service
public class PatientConsultationPrescriptionsService {

  /** The consultation presc repo. */
  @LazyAutowired
  private PatientConsultationPrescriptionsRepository consultationPrescRepo;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    return preAuthItemsService
        .filterByValidity(consultationPrescRepo.getPrescriptions(patientId));
  } 

  /**
   * Gets the prescriptions.
   *
   * @param consultationId the consultation id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptionsByConsultation(Integer consultationId) {
    return preAuthItemsService
        .filterByValidity(consultationPrescRepo.getPrescriptionsByConsultationId(consultationId));
  } 

  /**
   * Gets the presc consultations for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc consultations for consultation
   */
  public List<BasicDynaBean> getPrescConsultationsForConsultation(int consultationId) {
    return consultationPrescRepo.getPrescConsultationsForConsultation(consultationId);
  }

  /**
   * Gets the presc consultation for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the presc consultation for treatment sheet
   */
  public List<BasicDynaBean> getPrescConsultationForTreatmentSheet(int consultationId) {
    return consultationPrescRepo.getPrescConsultationForTreatmentSheet(consultationId);
  }

}

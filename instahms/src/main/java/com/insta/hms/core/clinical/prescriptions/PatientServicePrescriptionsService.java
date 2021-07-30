package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientServicePrescriptionsService.
 */
@Service
public class PatientServicePrescriptionsService {

  /** The service presc repo. */
  @LazyAutowired
  private PatientServicePrescriptionsRepository servicePrescRepo;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * Gets the prescriptions.
   *
   * @param consultationId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptionsByConsultation(Integer consultationId) {
    return preAuthItemsService.filterByValidity(servicePrescRepo
        .getPrescriptionsByConsultationId(consultationId));
  }

  /**
   * Gets the pres services for consultation.
   *
   * @param orgId the org id
   * @param bedType the bed type
   * @param consultationId the consultation id
   * @return the pres services for consultation
   */
  public List<BasicDynaBean> getPresServicesForConsultation(final String orgId,
      final String bedType, final int consultationId) {
    return servicePrescRepo.getPresServicesForConsultation(orgId, bedType, consultationId);
  }

  /**
   * Gets the pres services for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the pres services for treatment sheet
   */
  public List<BasicDynaBean> getPresServicesForTreatmentSheet(final int consultationId) {
    return servicePrescRepo.getPresServicesForTreatmentSheet(consultationId);
  }
  
  public BasicDynaBean findByPrescriptionId(Integer prescriptionId) {
    return servicePrescRepo.findByKey("op_service_pres_id", prescriptionId);
  }

}

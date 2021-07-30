package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientTestPrescriptionsService.
 */
@Service
public class PatientTestPrescriptionsService {

  /** The test presc repo. */
  @LazyAutowired
  private PatientTestPrescriptionsRepository testPrescRepo;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * Gets the prescriptions.
   *
   * @param consultationId the consultation id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptionsByConsultation(Integer consultationId) {
    return preAuthItemsService.filterByValidity(testPrescRepo
        .getPrescriptionsByConsultationId(consultationId));
  }

  /**
   * Gets the presc tests for consultation.
   *
   * @param orgId the org id
   * @param bedType the bed type
   * @param consultationId the consultation id
   * @return the presc tests for consultation
   */
  public List<BasicDynaBean> getPrescTestsForConsultation(String orgId, String bedType,
      int consultationId) {
    return testPrescRepo.getPrescTestsForConsultation(orgId, bedType, consultationId);
  }

  /**
   * Gets the presc tests for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the presc tests for treatment sheet
   */
  public List<BasicDynaBean> getPrescTestsForTreatmentSheet(int consultationId) {
    return testPrescRepo.getPrescTestsForTreatmentSheet(consultationId);
  }

}

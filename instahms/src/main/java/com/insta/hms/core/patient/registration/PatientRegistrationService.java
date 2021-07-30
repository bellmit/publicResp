package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientRegistrationService.
 */
@Service
public class PatientRegistrationService {

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /**
   * Find by key.
   *
   * @param keyColumn
   *          the key column
   * @param keyValue
   *          the key value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, String keyValue) {
    return patientRegistrationRepository.findByKey(keyColumn, keyValue);
  }

  /**
   * Gets the patient visit details map.
   *
   * @param visitId
   *          the visit id
   * @return the patient visit details map
   */
  public Map getPatientVisitDetailsMap(String visitId) {
    return patientRegistrationRepository.getPatientVisitDetailsMap(visitId);
  }
  
  /**
   * Gets the patient visit insurance details map.
   *
   * @param visitId
   *          the visit id
   * @return the patient visit insurance details map
   */
  public Map getPatientVisitInsuranceDetailsMap(String visitId) {
    return patientRegistrationRepository.getPatientVisitInsuranceDetailsMap(visitId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientRegistrationRepository.getBean();
  }

  /**
   * Update.
   *
   * @param visitDetailsBean
   *          the visit details bean
   * @param visitId
   *          the visit id
   * @return the boolean
   */
  public Boolean update(BasicDynaBean visitDetailsBean, String visitId) {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    return patientRegistrationRepository.update(visitDetailsBean, keys) >= 0;
  }

  /**
   * Checks if is visit id valid.
   *
   * @param visitId
   *          the visit id
   * @return the boolean
   */
  public Boolean isVisitIdValid(String visitId) {
    return patientRegistrationRepository.exist("patient_id", visitId, false);
  }

  /**
   * Gets the visits for mr no.
   *
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @return the visits for mr no
   */
  public List getVisitsForMrNo(String mrNo, String visitType) {
    return patientRegistrationRepository.getVisitsForMrNo(mrNo, visitType);
  }
}

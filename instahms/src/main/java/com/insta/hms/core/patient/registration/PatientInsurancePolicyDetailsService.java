package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class PatientInsurancePolicyDetailsService.
 */
@Service
public class PatientInsurancePolicyDetailsService {

  /** The patient insurance policy details repo. */
  @LazyAutowired
  PatientInsurancePolicyDetailsRepository patientInsurancePolicyDetailsRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientInsurancePolicyDetailsRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param patInsPolicyDetailsBean
   *          the pat ins policy details bean
   * @return the int
   */
  public int insert(BasicDynaBean patInsPolicyDetailsBean) {
    patInsPolicyDetailsBean.set("patient_policy_id",
        patientInsurancePolicyDetailsRepo.getNextSequence());
    return patientInsurancePolicyDetailsRepo.insert(patInsPolicyDetailsBean);
  }

  /**
   * Delete.
   *
   * @param visitId
   *          the visit id
   * @return the int
   */
  public int delete(String visitId) {
    return patientInsurancePolicyDetailsRepo.delete("visit_id", visitId);

  }

  /**
   * Delete.
   *
   * @param keys
   *          the keys
   * @return the int
   */
  public int delete(Map<String, Object> keys) {
    // TODO Auto-generated method stub
    return patientInsurancePolicyDetailsRepo.delete(keys);
  }
  
  /**
   * Find by key.
   *
   * @param keys
   *          the keys
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> keys) {
    // TODO Auto-generated method stub
    return patientInsurancePolicyDetailsRepo.findByKey(keys);
  }

  /**
   * Update.
   *
   * @param patientPolicyBean
   *          the patient policy bean
   * @return 0 if not updated, else returns the number of rows affected.
   */
  public int update(BasicDynaBean patientPolicyBean) {
    if (patientPolicyBean.get("patient_policy_id") != null) {
      int patientPolicyId = (Integer) patientPolicyBean.get("patient_policy_id");
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("patient_policy_id", patientPolicyId);
      return patientInsurancePolicyDetailsRepo.update(patientPolicyBean, keys);
    } else {
      return 0;
    }
  }

}

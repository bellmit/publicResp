package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientDietPrescriptionsService.
 *
 * @author ritolia
 */
@Service
public class PatientDietPrescriptionsService {

  /** The patient diet prescriptions repository. */
  @LazyAutowired
  private PatientDietPrescriptionsRepository patientDietPrescriptionsRepository;

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    return patientDietPrescriptionsRepository.getPrescriptions(patientId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientDietPrescriptionsRepository.getBean();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return patientDietPrescriptionsRepository.update(bean, keys);
  }
}

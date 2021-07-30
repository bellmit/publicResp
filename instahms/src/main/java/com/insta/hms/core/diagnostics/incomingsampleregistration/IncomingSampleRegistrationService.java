package com.insta.hms.core.diagnostics.incomingsampleregistration;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IncomingSampleRegistrationService.
 */
@Service
public class IncomingSampleRegistrationService {

  /** The incoming sample reg repo. */
  @LazyAutowired
  private IncomingSampleRegistrationRepository incomingSampleRegRepo;

  /** The Patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  @LazyAutowired
  private IncomingSampleRegistrationValidation validator;

  @LazyAutowired
  private CenterService centerService;

  /**
   * Update modified patient details for incoming patient.
   *
   * @param mrNo the mr no
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateModifiedPatientDetailsForIncomingPatient(String mrNo) {
    boolean status = true;
    if (exist("mr_no", mrNo)) {
      BasicDynaBean incomingPatientBean = incomingSampleRegRepo.getBean();
      BasicDynaBean patientBean = patientDetailsService.getPatientDetailsDisplayBean(mrNo);
      if (patientBean == null) {
        throw new EntityNotFoundException(new String[] {"patient", "MR NO", mrNo});
      }
      incomingPatientBean.set("patient_name", patientBean.get("full_name"));
      incomingPatientBean.set("patient_age", patientBean.get("age"));
      incomingPatientBean.set("age_unit", patientBean.get("agein"));
      incomingPatientBean.set("patient_gender", patientBean.get("patient_gender"));
      incomingPatientBean.set("phone_no", patientBean.get("patient_phone"));
      incomingPatientBean.set("referring_doctor", patientBean.get("reference_docto_id"));

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("mr_no", mrNo);
      status = incomingSampleRegRepo.update(incomingPatientBean, keys) > 0;
    }
    return status;
  }

  /**
   * Exist.
   *
   * @param keyColumn the key column
   * @param mrNo the mr no
   * @return true, if successful
   */
  public boolean exist(String keyColumn, String mrNo) {
    return incomingSampleRegRepo.exist(keyColumn, mrNo, false);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return incomingSampleRegRepo.getBean();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return incomingSampleRegRepo.update(bean, keys);
  }

  /**
   * List visit.
   *
   * @param visitId the visit id
   * @return the list
   */
  public List<BasicDynaBean> listVisit(List<String> visitId) {
    return incomingSampleRegRepo.listISRDetails(visitId);
  }

  /**
   * Gets the patient details.
   *
   * @param incomingVisitId
   *          the incoming visit id
   * @return the patient details
   */
  public BasicDynaBean getPatientDetails(String incomingVisitId) {
    return incomingSampleRegRepo.findByKey("incoming_visit_id", incomingVisitId);
  }
  
  /**
   * Gets country code by center id.
   *
   * @param centerId
   *          center id
   * @return country code
   */
  public String getCountryCode(int centerId) {
    return centerService.getCountryCode(centerId);
  }

  /**
   * To bean.
   *
   * @param requestBody
   *          the request body
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(ModelMap requestBody) {
    BasicDynaBean bean = incomingSampleRegRepo.getBean();
    List<String> errorFields = new ArrayList<>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);
    return bean;
  }
}

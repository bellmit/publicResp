package com.insta.hms.core.patient.registration;

import com.insta.hms.mdm.insuranceplans.InsurancePlanRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientInsurancePlansService.
 */
@Service
public class PatientInsurancePlansService {

  /** The pat ins plans repo. */
  @Autowired
  private PatientInsurancePlansRepository patInsPlansRepo;

  /** The insu plans repo. */
  @Autowired
  private InsurancePlanRepository insuPlansRepo;

  /** The insu plans validator. */
  @Autowired
  private PatientInsurancePlansValidator insuPlansValidator;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patInsPlansRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param patInsPlanBean
   *          the pat ins plan bean
   * @return the int
   */
  public int insert(BasicDynaBean patInsPlanBean) {
    patInsPlanBean.set("patient_insurance_plans_id", patInsPlansRepo.getNextSequence());
    return patInsPlansRepo.insert(patInsPlanBean);
  }

  /**
   * Delete.
   *
   * @param visitId
   *          the visit id
   * @return the int
   */
  public int delete(String visitId) {
    return patInsPlansRepo.delete("patient_id", visitId);
  }
  
  /**
   * Delete.
   *
   * @param keys
   *          the keys
   * @return the int
   */
  public int delete(Map<String, Object> keys) {
    return patInsPlansRepo.delete(keys);
  }

  /**
   * Gets the visit primary plan.
   *
   * @param visitId
   *          the visit id
   * @return the visit primary plan
   */
  public BasicDynaBean getVisitPrimaryPlan(String visitId) {
    int[] visitplans = getPlanIds(visitId);
    return (visitplans != null ? insuPlansRepo.findByKey("plan_id", visitplans[0]) : null);
  }

  /**
   * Gets the plan ids.
   *
   * @param patientId
   *          the patient id
   * @return the plan ids
   */
  public int[] getPlanIds(String patientId) {
    List<BasicDynaBean> planList = patInsPlansRepo.getPlanIds(patientId);
    int[] planIds = null != planList && planList.size() > 0 ? new int[planList.size()] : null;
    int planIdx = 0;
    for (BasicDynaBean bean : planList) {
      planIds[planIdx++] = (Integer) bean.get("plan_id");
    }
    return planIds;
  }

  /**
   * Gets the patient insurance details.
   *
   * @param patientId the patient id
   * @return the patient insurance details
   */
  public List<BasicDynaBean> getPatientInsuranceDetails(String patientId) {
    List insBean = new ArrayList();
    insBean = patInsPlansRepo.getDetails(patientId);
    return insBean;
  }

  /**
   * List.
   *
   * @param keys
   *          the keys
   * @return the basic dyna bean
   */
  public BasicDynaBean list(Map<String, Object> keys) {
    return findByKey(keys);
  }

  /**
   * Find by key.
   *
   * @param prKeys the pr keys
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map prKeys) {
    List<BasicDynaBean> beans = patInsPlansRepo.listAll(null, prKeys, null);
    BasicDynaBean plan = null;
    if (null != beans && beans.size() > 0) {
      List<BasicDynaBean> plans = patInsPlansRepo.listAll(null, prKeys, null);
      plan = plans.get(0);
    }
    return plan;
  }

  /**
   * Update.
   *
   * @param patInsPlanBean
   *          the pat ins plan bean
   * @param keys
   *          the keys
   * @return the int
   */
  public int update(BasicDynaBean patInsPlanBean, Map keys) {
    return patInsPlansRepo.update(patInsPlanBean, keys);
  }

  /**
   * Gets the insurance document.
   *
   * @param object
   *          the object
   * @return the insurance document
   */
  public BasicDynaBean getInsuranceDocument(Object[] object) {
    return patInsPlansRepo.getInsuranceDocument(object);
  }

  /**
   * Gets the plan details.
   *
   * @param visitId
   *          the visit id
   * @return the plan details
   */
  public List<BasicDynaBean> getPlanDetails(String visitId) {
    return patInsPlansRepo.getPlanDetails(visitId);
  }

  /**
   * Gets the pat insurance info.
   *
   * @param visitId
   *          the visit id
   * @return the pat insurance info
   */
  public BasicDynaBean getPatInsuranceInfo(String visitId) {
    insuPlansValidator.validatePatientIdParamaeter(visitId);
    return patInsPlansRepo.getPatInsuranceInfo(visitId);
  }

  /**
   * Gets the policy details.
   *
   * @param mrNo
   *          the mr no
   * @param centerId
   *          the center id
   * @return the policy details
   */
  public List<BasicDynaBean> getPolicyDetails(String mrNo, Integer centerId) {
    return patInsPlansRepo.getPolicyDetails(mrNo, centerId);
  }

  /**
   * Gets the insurance details.
   *
   * @param visitId
   *          the visit id
   * @param visitType
   *          the visit type
   * @return the insurance details
   */
  public List getInsuranceDetails(String visitId, String visitType) {
    return patInsPlansRepo.getInsuranceDetails(visitId, visitType);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public Integer getNextSequence() {
    return patInsPlansRepo.getNextSequence();
  }

  /**
   * List all.
   *
   * @param keys
   *          the keys
   * @return the list
   */
  public List<BasicDynaBean> listAll(Map<String, Object> keys) {
    return patInsPlansRepo.listAll(null, keys, null);
  }

  /**
   * List all.
   *
   * @param keys
   *          the keys
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(Map<String, Object> keys, String sortColumn) {
    return patInsPlansRepo.listAll(null, keys, sortColumn);
  }

  /**
   * Gets the all patient plan details using mr no.
   *
   * @param mrNo
   *          the mr no
   * @return the all patient plan details using mr no
   */
  public List<BasicDynaBean> getAllPatientPlanDetailsUsingMrNo(String mrNo) {
    return patInsPlansRepo.getAllPatientPlanDetails(mrNo);
  }

  /**
   * Find by keys.
   *
   * @param keys
   *          the keys
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKeys(Map keys) {
    return patInsPlansRepo.findByKey(keys);
  }

  /**
   * Update case rate limits.
   *
   * @param priCaseRateId
   *          the pri case rate id
   * @param visitId
   *          the visit id
   * @param ind
   *          the ind
   */
  public void updateCaseRateLimits(Integer priCaseRateId, String visitId, int ind) {
    patInsPlansRepo.updateCaseRateLimits(priCaseRateId, visitId, ind);
  }

  /**
   * Gets the insurance category payable status.
   *
   * @param visitId
   *          the visit id
   * @param insuranceCategoryId
   *          the insurance category id
   * @param patientType
   *          the patient type
   * @return the insurance category payable status
   */
  public BasicDynaBean getInsuranceCategoryPayableStatus(String visitId,
      Integer insuranceCategoryId, String patientType) {
    return patInsPlansRepo.getInsuranceCategoryPayableStatus(visitId, insuranceCategoryId,
        patientType);
  }

}

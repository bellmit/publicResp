package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientInsurancePlanDetailsService.
 */
@Service
public class PatientInsurancePlanDetailsService {

  /** The patient insurance plan details repo. */
  @LazyAutowired
  private PatientInsurancePlanDetailsRepository patientInsurancePlanDetailsRepo;

  /** The insurance plan details service. */
  @LazyAutowired
  private InsurancePlanDetailsService insurancePlanDetailsService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientInsurancePlanDetailsRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param patInsPlanDetailBean
   *          the pat ins plan detail bean
   * @return the int
   */
  public int insert(BasicDynaBean patInsPlanDetailBean) {
    return patientInsurancePlanDetailsRepo.insert(patInsPlanDetailBean);
  }

  /**
   * Delete.
   *
   * @param visitId
   *          the visit id
   * @return the int
   */
  public int delete(String visitId) {
    return patientInsurancePlanDetailsRepo.delete("visit_id", visitId);
  }

  /**
   * Delete.
   *
   * @param patientInsurancePlanId
   *          the patient insurance plan id
   * @return the int
   */
  public int delete(Integer patientInsurancePlanId) {
    // TODO Auto-generated method stub
    return patientInsurancePlanDetailsRepo.delete("patient_insurance_plans_id",
        patientInsurancePlanId);
  }

  /**
   * Gets the plan details.
   *
   * @param planId
   *          the plan id
   * @param visitType
   *          the visit type
   * @param visitId
   *          the visit id
   * @return the plan details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getPlanDetails(int planId, String visitType, String visitId) {
    List<BasicDynaBean> planDetailsList = patientInsurancePlanDetailsRepo.getPlanDetails(planId,
        visitType, visitId);
    if (null == planDetailsList || (null != planDetailsList && planDetailsList.isEmpty())) {
      Map filterMap = new HashMap();
      filterMap.put("plan_id", planId);
      filterMap.put("patient_type", visitType);
      planDetailsList = insurancePlanDetailsService.listAll(filterMap);
    }
    return planDetailsList;
  }


  /**
   * Gets the plan details.
   *
   * @param planId
   *          the plan id
   * @param visitType
   *          the visit type
   * @param visitId
   *          the visit id
   * @return the plan details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getPreviousPlanDetails(int planId, String visitType, String visitId) {
    List<BasicDynaBean> planDetailsList = patientInsurancePlanDetailsRepo
        .getPreviousPlanDetails(planId,visitType, visitId);
    if (null == planDetailsList || (null != planDetailsList && planDetailsList.isEmpty())) {
      Map filterMap = new HashMap();
      filterMap.put("plan_id", planId);
      filterMap.put("patient_type", visitType);
      planDetailsList = insurancePlanDetailsService.listAll(null,filterMap,"display_order");
    }
    return planDetailsList;
  }


  /**
   * Gets the visit ins details.
   *
   * @param visitId
   *          the visit id
   * @return the visit ins details
   */
  public List<BasicDynaBean> getVisitInsDetails(String visitId) {
    List<BasicDynaBean> patPlanListBean = new ArrayList<BasicDynaBean>();
    patPlanListBean = 
        patientInsurancePlanDetailsRepo.getVisitInsDetails(new Object[] { visitId });
    if (patPlanListBean == null || patPlanListBean.size() == 0) {
      String visitType = regService.getVisitType(visitId);
      patPlanListBean = 
          patientInsurancePlanDetailsRepo.getInsuranceDetailsFromMaster(
              new Object[] {visitId, visitType });
    }
    return patPlanListBean;
  }

  /**
   * Gets the patient insurance plan details.
   *
   * @param visitId
   *          the visit id
   * @return the patient insurance plan details
   */
  public List<BasicDynaBean> getPatientInsurancePlanDetails(String visitId) {
    // TODO Auto-generated method stub
    return patientInsurancePlanDetailsRepo.listAll(null, "visit_id", visitId);
  }

  /**
   * Insert all.
   *
   * @param patInsPlanDetailsList
   *          the pat ins plan details list
   * @return the int[]
   */
  public int[] insertAll(List<BasicDynaBean> patInsPlanDetailsList) {
    // TODO Auto-generated method stub
    return patientInsurancePlanDetailsRepo.batchInsert(patInsPlanDetailsList);
  }

}

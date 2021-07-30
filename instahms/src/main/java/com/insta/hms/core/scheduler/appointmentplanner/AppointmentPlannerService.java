package com.insta.hms.core.scheduler.appointmentplanner;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.AppointmentValidator;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.JobService;
import com.insta.hms.messaging.MessageManager;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Class AppointmentPlannerService.
 */
@Service
public class AppointmentPlannerService {

  /** The appointment planner repository. */
  @LazyAutowired
  private AppointmentPlannerRepository appointmentPlannerRepository;

  /** The appointment planner details repository. */
  @LazyAutowired
  private AppointmentPlannerDetailsRepository appointmentPlannerDetailsRepository;

  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;

  /** The appointment planner ACL. */
  @LazyAutowired
  private AppointmentPlannerACL appointmentPlannerACL;

  /** The appointment validator. */
  @LazyAutowired
  private AppointmentValidator appointmentValidator;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The appointment repository. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;

  /** The patient details repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AppointmentPlannerService.class);

  /**
   * Creates the new plan.
   *
   * @param params the params
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> createNewPlan(Map<String, Object> params) {
    if (params.get("newPlan") == null) {
      appointmentPlannerACL.checkAllowAddAppointmentPlan();
    }
    validateInputParams(params);
    boolean result = false;
    BasicDynaBean appointmentPlannerRepoBean = appointmentPlannerRepository.getBean();
    int planId = 0;
    int plan = 0;
    if (params.get("center_id") == null) {
      params.put("center_id", RequestContext.getCenterId());
    }
    if (params.get("plan_id") != null) {
      appointmentPlannerRepoBean = appointmentPlannerRepository.findByKey("plan_id",
          params.get("plan_id"));
      planId = (int) appointmentPlannerRepoBean.get("plan_id");
      Map<String, Object> keys = new HashMap<>();
      appointmentPlannerRepoBean.set("modified_by",
          sessionService.getSessionAttributes().get("userId"));
      appointmentPlannerRepoBean.set("mod_time", DateUtil.getCurrentTimestamp());
      keys.put("plan_id", planId);
      plan = appointmentPlannerRepository.update(appointmentPlannerRepoBean, keys);
    } else {
      planId = appointmentPlannerRepository.getNextSequence();
      appointmentPlannerRepoBean.set("plan_id", planId);
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      params.put("created_by", userName);
      ConversionUtils.copyToDynaBean(params, appointmentPlannerRepoBean);
      plan = appointmentPlannerRepository.insert(appointmentPlannerRepoBean);
    }

    Map<String, Object> apptsResponse = null;
    List<Map<String, Object>> apptsBooked = null;
    if (params.get("patient_appointment_plan_details") != null) {
      Map<String, Object> apptsWithCategories = constructAppointmentsData(params);
      if (apptsWithCategories.get("appointments") != null
          && ((List) apptsWithCategories.get("appointments")).size() > 0) {
        Map<String, String> patientData = new HashMap<>();

        patientData.put("mr_no", (String) params.get("mr_no"));
        patientData.put("patient_name", (String) params.get("patient_name"));
        patientData.put("patient_contact", (String) params.get("patient_phone"));
        patientData.put("scheduler_visit_type", "F");
        patientData.put("vip_status", (String) params.get("vip_status"));

        Map<String, Object> appointmentSchedulerParams = new HashMap<>();
        appointmentSchedulerParams.put("patient", patientData);
        appointmentSchedulerParams.put("appointment_plan_id", planId);
        appointmentSchedulerParams.put("appointments", apptsWithCategories.get("appointments"));
        apptsResponse = appointmentService.createBulkAppointments(
            (List<AppointmentCategory>) apptsWithCategories.get("apptCategories"),
            appointmentSchedulerParams);
        apptsBooked = (List<Map<String, Object>>) apptsResponse.get("appointments");
      }
    }
    for (Object patientAppointmentPlanDetail : (ArrayList) params
        .get("patient_appointment_plan_details")) {
      Map patientAppointmentDetailParams = (HashMap) patientAppointmentPlanDetail;
      patientAppointmentDetailParams.put("plan_id", planId);
      if (patientAppointmentDetailParams.get("book_appointment") != null
          && parseBool((String) patientAppointmentDetailParams.get("book_appointment"))) {
        for (Map<String, Object> apptBooked : apptsBooked) {
          if (((String) apptBooked.get("secondary_resource_id"))
              .equals((String) patientAppointmentDetailParams.get("secondary_resource_id"))
              && ((String) apptBooked.get("date")).equals((String) patientAppointmentDetailParams
                  .get("plan_visit_date"))
              && ((String) apptBooked.get("slot_time"))
                  .equals((String) patientAppointmentDetailParams.get("plan_visit_time"))) {
            List<Integer> apptIds = (List<Integer>) apptBooked.get("appointment_ids_list");
            if (apptIds != null && apptIds.size() > 0) {
              patientAppointmentDetailParams.put("appointment_id", apptIds.get(0));
            }
            break;
          }
        }
      }
    }
    for (Object patientAppointmentPlanDetail : (ArrayList) params
        .get("patient_appointment_plan_details")) {
      Map patientAppointmentDetailParams = (HashMap) patientAppointmentPlanDetail;
      if (patientAppointmentDetailParams.get("book_appointment") != null
          && parseBool((String) patientAppointmentDetailParams.get("book_appointment"))) {
        if (params.get("plan_appointment_status") != null) {
          patientAppointmentDetailParams.put("plan_details_status",
              PlanDetailsStatus.PROGRESS.planDetailsStatus());
        }
      } else {
        patientAppointmentDetailParams.put("plan_details_status",
            PlanDetailsStatus.NEW.planDetailsStatus());
      }
      BasicDynaBean schedulerPlanDetailBean = appointmentPlannerDetailsRepository.getBean();
      if (patientAppointmentDetailParams.get("plan_details_id") == null) {
        schedulerPlanDetailBean = appointmentPlannerDetailsRepository.getBean();
        schedulerPlanDetailBean.set("plan_details_id",
            appointmentPlannerDetailsRepository.getNextSequence());
        ConversionUtils.copyToDynaBean(patientAppointmentDetailParams, schedulerPlanDetailBean);
        appointmentPlannerDetailsRepository.insert(schedulerPlanDetailBean);
        result = true;
      } else {
        schedulerPlanDetailBean = appointmentPlannerDetailsRepository.findByKey("plan_details_id",
            patientAppointmentDetailParams.get("plan_details_id"));
        schedulerPlanDetailBean.set("appointment_id",
            patientAppointmentDetailParams.get("appointment_id"));
        schedulerPlanDetailBean.set("plan_details_status",
            patientAppointmentDetailParams.get("plan_details_status"));
        schedulerPlanDetailBean.set("doc_dept_id",
            patientAppointmentDetailParams.get("doc_dept_id"));
        Timestamp planVisitDateTimeStamp = null;
        try {
          planVisitDateTimeStamp = DateUtil.parseTimestamp(
              (String) patientAppointmentDetailParams.get("plan_visit_date"), null);
        } catch (ParseException exception) {
          ValidationErrorMap validationErrors = new ValidationErrorMap();
          ValidationException ex = new ValidationException(validationErrors);
          Map<String, Object> nestedException = new HashMap<>();
          nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
        schedulerPlanDetailBean.set(
            "plan_visit_date",
            DateUtil.getDatePart(planVisitDateTimeStamp) != null ? DateUtil
                .getDatePart(planVisitDateTimeStamp) : null);
        schedulerPlanDetailBean.set("secondary_resource_id",
            patientAppointmentDetailParams.get("secondary_resource_id"));
        schedulerPlanDetailBean.set("doc_dept_id",
            patientAppointmentDetailParams.get("doc_dept_id"));
        Map<String, Object> keys = new HashMap<>();
        keys.put("plan_details_id", patientAppointmentDetailParams.get("plan_details_id"));
        appointmentPlannerDetailsRepository.update(schedulerPlanDetailBean, keys);
      }
    }
    if (result && params.get("newPlan") == null) {
      sendCreatePlanSMS((Integer) planId);
    }

    return params;
  }

  /**
   *  Get all the plans created for the patient.
   *
   * @param mrNo the mr no
   * @param centerId the center id
   * @return the plans for patient
   */
  public Map<String, List<Map<String, Object>>> getPlansForPatient(String mrNo,
      Integer centerId) {
    List<BasicDynaBean> basicDynaBeans = appointmentPlannerRepository.getPlanNamesByPatient(mrNo,
        centerId);
    Map<String, List<Map<String, Object>>> plansMap = new HashMap<>();
    List<Map<String, Object>> plansArray = new ArrayList<>();
    for (BasicDynaBean basicDynaBean : basicDynaBeans) {
      Map<String, Object> plan = new HashMap<>();
      plan.put("plan_name", basicDynaBean.get("plan_name"));
      plan.put("plan_id", basicDynaBean.get("plan_id"));
      plansArray.add(plan);
    }
    plansMap.put("plans", plansArray);
    return plansMap;
  }

  /**
   * Gets the patient plan details.
   *
   * @param planId the plan id
   * @return the patient plan details
   */
  public Map<String, Object> getPatientPlanDetails(Integer planId) {
    List<BasicDynaBean> basicDynaBeans = appointmentPlannerDetailsRepository
        .getPlanDetails(planId);
    Map<String, Object> patientPlanDetailsMap = new HashMap<>();
    patientPlanDetailsMap.put("plan_id", basicDynaBeans.get(0).get("plan_id"));
    patientPlanDetailsMap.put("plan_name", basicDynaBeans.get(0).get("plan_name"));
    patientPlanDetailsMap.put("presc_doc_id", basicDynaBeans.get(0).get("presc_doc_id"));
    patientPlanDetailsMap.put("presc_doc_name", basicDynaBeans.get(0).get("presc_doc_name"));
    patientPlanDetailsMap.put("mr_no", basicDynaBeans.get(0).get("mr_no"));
    BasicDynaBean patientDetailsDisplayBean = patientDetailsRepository
        .getPatientDetailsDisplayBean((String) basicDynaBeans.get(0).get("mr_no"));
    patientPlanDetailsMap.put("patient_name", patientDetailsDisplayBean.get("full_name"));
    patientPlanDetailsMap.put("patient_phone", patientDetailsDisplayBean.get("patient_phone"));

    List<Map<String, Object>> planDetailsList = new ArrayList<>();
    Set<Integer> listOfTraversedPlanDetails = new HashSet<>();
    for (BasicDynaBean basicDynaBean : basicDynaBeans) {
      Map<String, Object> planDetailMap = null;
      List<Map<String, Object>> additionalResources = null;
      if (basicDynaBean.get("plan_details_id") != null
          && !listOfTraversedPlanDetails.contains(basicDynaBean.get("plan_details_id"))) {
        planDetailMap = new HashMap<>();
        planDetailMap.put("plan_details_id", basicDynaBean.get("plan_details_id"));
        if (basicDynaBean.get("appointment_date") != null) {
          planDetailMap.put("plan_visit_date", basicDynaBean.get("appointment_date"));
        } else {
          planDetailMap.put("plan_visit_date", basicDynaBean.get("plan_visit_date"));
        }
        planDetailMap.put("plan_visit_time", basicDynaBean.get("plan_visit_time"));
        planDetailMap.put("appointment_category", basicDynaBean.get("appointment_category"));
        planDetailMap.put("consultation_reason_id", basicDynaBean.get("consultation_reason_id"));
        planDetailMap
            .put("consultation_reason_name", basicDynaBean.get("consultation_reason_name"));
        planDetailMap.put("secondary_resource_id", basicDynaBean.get("secondary_resource_id"));
        planDetailMap.put("secondary_resource_name", basicDynaBean.get("secondary_resource_name"));
        planDetailMap.put("primary_resource_id", basicDynaBean.get("primary_resource_id"));
        planDetailMap.put("appointment_id", basicDynaBean.get("appointment_id"));
        planDetailMap.put("status", basicDynaBean.get("appointment_status"));
        planDetailMap.put("plan_details_status", basicDynaBean.get("plan_details_status"));
        planDetailMap.put("scheduler_visit_type", basicDynaBean.get("scheduler_visit_type"));
        planDetailMap.put("vip_status", basicDynaBean.get("vip_status"));
        planDetailMap.put("duration", basicDynaBean.get("complaint_type_duration"));
        planDetailMap.put("doc_dept_id", basicDynaBean.get("doc_dept_id"));
        if (basicDynaBean.get("duration") == null) {
          planDetailMap.put("duration", basicDynaBean.get("complaint_type_duration"));
        } else {
          planDetailMap.put("duration", basicDynaBean.get("duration"));
        }
        planDetailMap.put("additional_resources_insert", new ArrayList<>());
        listOfTraversedPlanDetails.add((Integer) basicDynaBean.get("plan_details_id"));
        planDetailsList.add(planDetailMap);
      }

      if (basicDynaBean.get("appointment_id") != null) {
        if (basicDynaBean.get("resource_id") != null) {
          Map<String, Object> additionalResourceMap = new HashMap<>();
          additionalResourceMap.put("resource_id", basicDynaBean.get("resource_id"));
          additionalResourceMap.put("resource_type", basicDynaBean.get("resource_type"));
          additionalResourceMap.put("resource_name", basicDynaBean.get("resource_name"));
          for (Map planDetails : planDetailsList) {
            if (planDetails.get("appointment_id") != null
                && basicDynaBean.get("appointment_id") != null
                && ((Integer) basicDynaBean.get("appointment_id"))
                .intValue() == ((Integer) planDetails.get("appointment_id")).intValue()) {
              planDetailMap = planDetails;
              additionalResources = (List<Map<String, Object>>) planDetailMap
                  .get("additional_resources_insert");
              additionalResources.add(additionalResourceMap);
              break;
            }
          }
        }
      }
    }
    patientPlanDetailsMap.put("patient_appointment_plan_details", planDetailsList);
    return patientPlanDetailsMap;
  }

  /**
   * Modify plan.
   *
   * @param params the params
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> modifyPlan(Map<String, Object> params) {
    appointmentPlannerACL.checkAllowEditAppointmentPlan();
    List deleteAppointmentList = null;
    List<Map> planDetailsDeleteMapList = null;

    Map<String, Object> newPlanParams = null;
    List newPlaList = null;

    List<Map> rescheduleAppointmentsMapList = null;
    List<Map> modifyPlanDetailsMapList = null;

    boolean modifyResult = false;

    for (Map patientAppointmentPlanDetail : (List<Map>) params
        .get("patient_appointment_plan_details")) {
      if (patientAppointmentPlanDetail.get("action") != null
          && patientAppointmentPlanDetail.get("action").equals("D")) {
        if (patientAppointmentPlanDetail.get("appointment_id") != null) {
          if (deleteAppointmentList == null) {
            deleteAppointmentList = new ArrayList();
          }
          Map<String, Object> deleteAppointmentMap = new HashMap<>();
          deleteAppointmentMap.put("appointment_id",
              patientAppointmentPlanDetail.get("appointment_id"));
          deleteAppointmentMap.put("category",
              patientAppointmentPlanDetail.get("appointment_category"));
          deleteAppointmentMap.put("appointment_status", "Cancel");
          deleteAppointmentMap.put("cancel_reason", "Removed from plan by care team");
          deleteAppointmentMap.put("cancel_type", "Other");
          deleteAppointmentList.add(deleteAppointmentMap);
        }
        if (planDetailsDeleteMapList == null) {
          planDetailsDeleteMapList = new ArrayList<>();
        }
        Map<String, Object> planDetailsDelete = new HashMap<>();
        planDetailsDelete.put("plan_details_id",
            patientAppointmentPlanDetail.get("plan_details_id"));
        planDetailsDelete.put("plan_details_status", PlanDetailsStatus.DELETED.planDetailsStatus());
        planDetailsDeleteMapList.add(planDetailsDelete);
      } else if (patientAppointmentPlanDetail.get("action") != null
          && patientAppointmentPlanDetail.get("action").equals("M")
          && patientAppointmentPlanDetail.get("appointment_id") != null
          && (patientAppointmentPlanDetail.get("book_appointment") != null
          && parseBool((String) patientAppointmentPlanDetail
              .get("book_appointment")))) {
        Map<String, Object> patientMap = new HashMap<>();
        patientMap = new HashMap<>();
        patientMap.put("mr_no", params.get("mr_no"));
        patientMap.put("patient_name", params.get("patient_name"));
        patientMap.put("patient_contact", params.get("patient_phone"));
        patientMap.put("vip_status", params.get("vip_status"));
        patientMap.put("scheduler_visit_type",
            patientAppointmentPlanDetail.get("scheduler_visit_type"));
        Map<String, Object> modifyPlanMap = new HashMap<>();
        modifyPlanMap.put("patient", patientMap);
        modifyPlanMap.put("appointment_plan_id", params.get("plan_id"));
        Map<String, Object> appointmentsMap = new HashMap<>();
        appointmentsMap.put("presc_doc_id", params.get("presc_doc_id"));
        appointmentsMap.put("presc_doc_name", params.get("presc_doc_name"));
        appointmentsMap.put("duration", patientAppointmentPlanDetail.get("duration"));
        appointmentsMap.put("category", patientAppointmentPlanDetail.get("appointment_category"));
        appointmentsMap.put("secondary_resource_id",
            patientAppointmentPlanDetail.get("secondary_resource_id"));
        appointmentsMap.put("appointment_id", patientAppointmentPlanDetail.get("appointment_id"));
        appointmentsMap.put("slot_time", patientAppointmentPlanDetail.get("plan_visit_time"));
        appointmentsMap.put("date", patientAppointmentPlanDetail.get("plan_visit_date"));
        appointmentsMap.put("center_id", RequestContext.getCenterId());
        appointmentsMap.put("complaint",
            patientAppointmentPlanDetail.get("consultation_reason_name"));
        appointmentsMap.put("status", patientAppointmentPlanDetail.get("status"));
        appointmentsMap.put("row_id", patientAppointmentPlanDetail.get("row_id"));

        List additionalResInsertList = null;
        List additionalResDeleteList = null;
        if (patientAppointmentPlanDetail.get("additional_resources_insert") != null) {
          additionalResInsertList = new ArrayList();
          for (Map additionalRes : (List<Map>) patientAppointmentPlanDetail
              .get("additional_resources_insert")) {
            Map<String, Object> additionalResInsMap = new HashMap<>();
            additionalResInsMap.put("resource_type", additionalRes.get("resource_type"));
            additionalResInsMap.put("resource_id", additionalRes.get("resource_id"));
            additionalResInsertList.add(additionalResInsMap);
          }
        }
        if (patientAppointmentPlanDetail.get("additional_resources_delete") != null) {
          additionalResDeleteList = new ArrayList();
          for (Map additionalRes : (List<Map>) patientAppointmentPlanDetail
              .get("additional_resources_delete")) {
            Map<String, Object> additionalResDeleteMap = new HashMap<>();
            additionalResDeleteMap.put("resource_type", additionalRes.get("resource_type"));
            additionalResDeleteMap.put("resource_id", additionalRes.get("resource_id"));
            additionalResDeleteList.add(additionalResDeleteMap);
          }
        }
        appointmentsMap.put("additional_resources_insert", additionalResInsertList);
        appointmentsMap.put("additional_resources_delete", additionalResDeleteList);
        modifyPlanMap.put("appointment", appointmentsMap);
        if (rescheduleAppointmentsMapList == null) {
          rescheduleAppointmentsMapList = new ArrayList<>();
        }
        rescheduleAppointmentsMapList.add(modifyPlanMap);
        if (modifyPlanDetailsMapList == null) {
          modifyPlanDetailsMapList = new ArrayList<>();
        }
        Map<String, Object> modifyPlanDetailsMap = new HashMap<>();
        modifyPlanDetailsMap.put("plan_details_id",
            patientAppointmentPlanDetail.get("plan_details_id"));
        modifyPlanDetailsMap.put("plan_visit_date",
            patientAppointmentPlanDetail.get("plan_visit_date"));
        modifyPlanDetailsMap.put("appointment_category",
            patientAppointmentPlanDetail.get("appointment_category"));
        modifyPlanDetailsMap.put("consultation_reason_id",
            patientAppointmentPlanDetail.get("consultation_reason_id"));
        modifyPlanDetailsMap.put("secondary_resource_id",
            patientAppointmentPlanDetail.get("secondary_resource_id"));
        modifyPlanDetailsMap.put("plan_details_status",
            PlanDetailsStatus.PROGRESS.planDetailsStatus());
        modifyPlanDetailsMap.put("plan_visit_time",
            patientAppointmentPlanDetail.get("plan_visit_time"));
        modifyPlanDetailsMap.put("doc_dept_id", patientAppointmentPlanDetail.get("doc_dept_id"));
        modifyPlanDetailsMapList.add(modifyPlanDetailsMap);

      } else if ((patientAppointmentPlanDetail.get("action") != null && patientAppointmentPlanDetail
          .get("action").equals("C"))
          || (patientAppointmentPlanDetail.get("action") != null
              && patientAppointmentPlanDetail.get("action").equals("M") 
              && patientAppointmentPlanDetail
              .get("appointment_id") == null)) {
        if (newPlaList == null) {
          newPlaList = new ArrayList();
        }
        if (newPlanParams == null) {
          newPlanParams = new HashMap<>();
          newPlanParams.put("mr_no", params.get("mr_no"));
          newPlanParams.put("patient_name", params.get("patient_name"));
          newPlanParams.put("patient_phone", params.get("patient_phone"));
          newPlanParams.put("vip_status", params.get("vip_status"));
          newPlanParams.put("presc_doc_id", params.get("presc_doc_id"));
          newPlanParams.put("presc_doc_name", params.get("presc_doc_name"));
          newPlanParams.put("plan_name", params.get("plan_name"));
          newPlanParams.put("plan_id", params.get("plan_id"));
          newPlanParams.put("newPlan", "true");
          newPlanParams.put("plan_appointment_status", params.get("plan_appointment_status"));
          newPlanParams.put("center_id", RequestContext.getCenterId());
        }
        if (patientAppointmentPlanDetail.get("book_appointment") != null
            && parseBool((String) patientAppointmentPlanDetail.get("book_appointment"))) {
          Map<String, Object> newPlan = new HashMap<>();
          newPlan.put("row_id", patientAppointmentPlanDetail.get("row_id"));
          newPlan.put("plan_visit_date", patientAppointmentPlanDetail.get("plan_visit_date"));
          newPlan.put("plan_visit_time", patientAppointmentPlanDetail.get("plan_visit_time"));
          newPlan.put("appointment_category",
              patientAppointmentPlanDetail.get("appointment_category"));
          newPlan.put("consultation_reason_id",
              patientAppointmentPlanDetail.get("consultation_reason_id"));
          newPlan.put("secondary_resource_id",
              patientAppointmentPlanDetail.get("secondary_resource_id"));
          newPlan.put("primary_resource_id", "");
          newPlan.put("book_appointment", patientAppointmentPlanDetail.get("book_appointment"));
          newPlan.put("duration", patientAppointmentPlanDetail.get("duration"));
          newPlan.put("plan_details_id", patientAppointmentPlanDetail.get("plan_details_id"));
          newPlan.put("doc_dept_id", patientAppointmentPlanDetail.get("doc_dept_id"));
          List additionalResInsertList = null;
          if (patientAppointmentPlanDetail.get("additional_resources_insert") != null
              && ((List) patientAppointmentPlanDetail
                  .get("additional_resources_insert")).size() > 0) {
            additionalResInsertList = new ArrayList();
            for (Map additionalResource : (List<Map>) patientAppointmentPlanDetail
                .get("additional_resources_insert")) {
              Map<String, Object> additionalResInsertMap = new HashMap<>();
              additionalResInsertMap.put("resource_type", additionalResource.get("resource_type"));
              additionalResInsertMap.put("resource_id", additionalResource.get("resource_id"));
              additionalResInsertList.add(additionalResInsertMap);
            }
          }
          newPlan.put("additional_resources_insert", additionalResInsertList);
          newPlaList.add(newPlan);
          newPlanParams.put("patient_appointment_plan_details", newPlaList);
        } else {
          Map<String, Object> newPlan = new HashMap<>();
          newPlan.put("row_id", patientAppointmentPlanDetail.get("row_id"));
          newPlan.put("plan_visit_date", patientAppointmentPlanDetail.get("plan_visit_date"));
          newPlan.put("plan_visit_time", patientAppointmentPlanDetail.get("plan_visit_time"));
          newPlan.put("appointment_category",
              patientAppointmentPlanDetail.get("appointment_category"));
          newPlan.put("consultation_reason_id",
              patientAppointmentPlanDetail.get("consultation_reason_id"));
          newPlan.put("secondary_resource_id",
              patientAppointmentPlanDetail.get("secondary_resource_id"));
          newPlan.put("primary_resource_id", "");
          newPlan.put("book_appointment", "N");
          newPlan.put("duration", patientAppointmentPlanDetail.get("duration"));
          newPlan.put("plan_details_id", patientAppointmentPlanDetail.get("plan_details_id"));
          newPlan.put("doc_dept_id", patientAppointmentPlanDetail.get("doc_dept_id"));
          newPlaList.add(newPlan);
          newPlanParams.put("patient_appointment_plan_details", newPlaList);
        }
      }
    }
    if (params.get("presc_doc_id") != null) {
      BasicDynaBean planBean = appointmentPlannerRepository.findByKey("plan_id",
          params.get("plan_id"));
      if (((String) planBean.get("presc_doc_id") == null)) {
        planBean.set("presc_doc_id", params.get("presc_doc_id"));
        Map keys = new HashMap();
        keys.put("plan_id", params.get("plan_id"));
        appointmentPlannerRepository.update(planBean, keys);
      }
    }
    if (deleteAppointmentList != null && deleteAppointmentList.size() > 0) {
      HashMap<String, Object> deleteAppointmentParams = new HashMap<>();
      deleteAppointmentParams.put("update_app_status", deleteAppointmentList);
      modifyResult = true;
      try {
        appointmentService.updateAppointmentsStatus(deleteAppointmentParams);
      } catch (Exception exception) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("plan_appointment_status", "exception.scheduler.status.updation.failed");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient_appointment_plan_details", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    if (planDetailsDeleteMapList != null && planDetailsDeleteMapList.size() > 0) {
      for (Map planDetailsMap : planDetailsDeleteMapList) {
        BasicDynaBean appointmentPlannerDetailsRepositoryBean = appointmentPlannerDetailsRepository
            .findByKey("plan_details_id", planDetailsMap.get("plan_details_id"));
        appointmentPlannerDetailsRepositoryBean.set("plan_details_status",
            PlanDetailsStatus.DELETED.planDetailsStatus());
        Map keys = new HashMap();
        keys.put("plan_details_id", planDetailsMap.get("plan_details_id"));
        appointmentPlannerDetailsRepository.update(appointmentPlannerDetailsRepositoryBean, keys);
        modifyResult = true;
      }
    }
    if (newPlanParams != null) {
      createNewPlan(newPlanParams);
      modifyResult = true;
    }
    List<Map> appointmentInfoMapList = new ArrayList<>();
    if (rescheduleAppointmentsMapList != null && rescheduleAppointmentsMapList.size() > 0) {
      for (Map resAppointmentMap : rescheduleAppointmentsMapList) {
        String category = resAppointmentMap.get("category") != null ? (String) resAppointmentMap
            .get("category") : "DOC";
        AppointmentCategory apptCategory = appointmentCategoryFactory.getInstance(category
            .toUpperCase(Locale.ENGLISH));
        validate(params, Arrays.asList((Map<String, Object>) resAppointmentMap.get("appointment")),
            Arrays.asList(apptCategory));
        Map<String, Object> result = appointmentService.editAppointment(apptCategory,
            resAppointmentMap);
        Integer appointmentId = null;
        if (result.get("appointment") != null) {
          Map map = (Map) result.get("appointment");
          appointmentId = (Integer) map.get("appointment_id");
        } else if (result.get("appointments") != null) {
          Map apptsBooked = ((List<Map<String, Object>>) result.get("appointments")).get(0);
          List<Integer> apptIds = (List<Integer>) apptsBooked.get("appointment_ids_list");
          if (apptIds != null && apptIds.size() > 0) {
            appointmentId = apptIds.get(0);
          }
        }
        Map appointmentInfoMap = new HashMap();
        Map appointmentsMap = null;
        if (((List<Map>) resAppointmentMap.get("appointments")) != null) {
          appointmentsMap = ((List<Map>) resAppointmentMap.get("appointments")).get(0);
        } else {
          appointmentsMap = (Map) resAppointmentMap.get("appointment");
        }
        appointmentInfoMap.put("appointment_id", appointmentId);
        appointmentInfoMap.put("plan_visit_time", appointmentsMap.get("slot_time"));
        appointmentInfoMap.put("plan_visit_date", appointmentsMap.get("date"));
        appointmentInfoMap.put("secondary_resource_id",
            appointmentsMap.get("secondary_resource_id"));
        appointmentInfoMapList.add(appointmentInfoMap);
        modifyResult = true;
      }
    }
    Map<String, Object> nestedException = new HashMap<>();
    if (modifyPlanDetailsMapList != null && modifyPlanDetailsMapList.size() > 0) {
      for (Map modifyPlanDetail : modifyPlanDetailsMapList) {
        BasicDynaBean appointmentPlannerDetailsRepositoryBean = appointmentPlannerDetailsRepository
            .findByKey("plan_details_id", modifyPlanDetail.get("plan_details_id"));
        Timestamp timestamp = null;
        try {
          timestamp = DateUtil.parseTimestamp((String) modifyPlanDetail.get("plan_visit_date"),
              null);
        } catch (ParseException exception) {
          ValidationErrorMap validationErrors = new ValidationErrorMap();
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
        appointmentPlannerDetailsRepositoryBean.set("plan_visit_date",
            DateUtil.getDatePart(timestamp));
        appointmentPlannerDetailsRepositoryBean.set("appointment_category",
            modifyPlanDetail.get("appointment_category"));
        appointmentPlannerDetailsRepositoryBean.set("consultation_reason_id",
            modifyPlanDetail.get("consultation_reason_id"));
        appointmentPlannerDetailsRepositoryBean.set("secondary_resource_id",
            modifyPlanDetail.get("secondary_resource_id"));
        appointmentPlannerDetailsRepositoryBean.set("doc_dept_id",
            modifyPlanDetail.get("doc_dept_id"));
        for (Map appointmentInfo : (List<Map>) appointmentInfoMapList) {
          if (appointmentInfo.get("plan_visit_date")
              .equals(modifyPlanDetail.get("plan_visit_date"))
              && appointmentInfo.get("plan_visit_time").equals(
                  modifyPlanDetail.get("plan_visit_time"))
              && appointmentInfo.get("secondary_resource_id").equals(
                  modifyPlanDetail.get("secondary_resource_id"))) {
            appointmentPlannerDetailsRepositoryBean.set("appointment_id",
                appointmentInfo.get("appointment_id"));
          }
        }
        Map keys = new HashMap();
        keys.put("plan_details_id", modifyPlanDetail.get("plan_details_id"));
        appointmentPlannerDetailsRepository.update(appointmentPlannerDetailsRepositoryBean, keys);
      }
      modifyResult = true;
    }

    if (modifyResult) {
      sendCreatePlanSMS((Integer) params.get("plan_id"));
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    return params;
  }

  /**
   * Parses the bool.
   *
   * @param values the values
   * @return true, if successful
   */
  private boolean parseBool(String values) {
    return values != null && Arrays.asList("true", "y", "yes", "1").contains(values.toLowerCase());
  }

  /**
   * Construct appointments data.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> constructAppointmentsData(Map<String, Object> params) {
    Map<String, Object> resultMap = new HashMap();
    ArrayList apptslist = new ArrayList();
    List<AppointmentCategory> appointmentCategories = new ArrayList<>();
    for (Map patientAppointmentPlanDetail : (List<Map>) params
        .get("patient_appointment_plan_details")) {
      if (patientAppointmentPlanDetail.get("book_appointment") != null
          && parseBool((String) patientAppointmentPlanDetail.get("book_appointment"))) {
        Map<String, Object> apptData = new HashMap<String, Object>();
        apptData.put("category", patientAppointmentPlanDetail.get("appointment_category"));
        appointmentCategories.add(appointmentCategoryFactory
            .getInstance(((String) patientAppointmentPlanDetail.get("appointment_category"))
                .toUpperCase(Locale.ENGLISH)));
        apptData.put("secondary_resource_id",
            patientAppointmentPlanDetail.get("secondary_resource_id"));
        apptData.put("date", patientAppointmentPlanDetail.get("plan_visit_date"));
        apptData.put("slot_time", patientAppointmentPlanDetail.get("plan_visit_time"));
        apptData.put("duration", patientAppointmentPlanDetail.get("duration"));
        apptData.put("presc_doc_id", params.get("presc_doc_id"));
        apptData.put("center_id", params.get("center_id"));
        apptData.put("status", params.get("plan_appointment_status"));
        apptData.put("row_id", patientAppointmentPlanDetail.get("row_id"));
        ArrayList additionalResourceList = new ArrayList();
        if (patientAppointmentPlanDetail.get("additional_resources_insert") != null) {
          for (Object additionalResource : (ArrayList) patientAppointmentPlanDetail
              .get("additional_resources_insert")) {
            Map additionalDetailsParams = (HashMap) additionalResource;
            additionalResourceList.add(additionalDetailsParams);
          }
        }
        apptData.put("additional_resources_insert", additionalResourceList);
        apptslist.add(apptData);
      }
    }
    validate(params, apptslist, appointmentCategories);
    resultMap.put("appointments", apptslist);
    resultMap.put("apptCategories", appointmentCategories);
    return resultMap;
  }

  /**
   * Validate.
   *
   * @param params the params
   * @param apptsList the appts list
   * @param appointmentCategories the appointment categories
   */
  public void validate(Map<String, Object> params, List<Map<String, Object>> apptsList,
      List<AppointmentCategory> appointmentCategories) {
    ValidationErrorMap validationErrors;
    Map<String, Object> nestedException = new HashMap<String, Object>();
    int categoryIdx = 0;
    Map<Integer, Object> apptsMap = null;
    Map<String, Object> apptMap = null;
    for (Map<String, Object> appointmentInfo : apptsList) {
      Integer duration = 0;
      if (appointmentInfo.get("duration") != null) {
        duration = (Integer) appointmentInfo.get("duration");
      } // TODO: get scheduler_master bean and get the info
      /*
       * else if (scheduleBean != null) { duration =
       * ((Integer)scheduleBean.get("default_duration")).intValue(); }
       */
      String apptDate = (String) appointmentInfo.get("date");
      String slotTime = (String) appointmentInfo.get("slot_time");
      String timestampStr = apptDate + " " + slotTime;
      Timestamp apptTime = null;
      try {
        apptTime = DateUtil.parseTimestamp(timestampStr);
      } catch (ParseException pe) {
        validationErrors = new ValidationErrorMap();
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      long appointmentTimeLong = apptTime.getTime();
      appointmentTimeLong = appointmentTimeLong + (duration * 60 * 1000);
      Timestamp endTimestamp = new java.sql.Timestamp(appointmentTimeLong);
      String resId = (String) appointmentInfo.get("secondary_resource_id");
      /* Validate if appointment slot is available */
      validationErrors = new ValidationErrorMap();
      AppointmentCategory category = appointmentCategories.get(categoryIdx);
      String resType = category.getPrimaryResourceType();
      Integer appointmentId = appointmentInfo.get("appointment_id") != null 
          ? (Integer) appointmentInfo.get("appointment_id") : null;

      String appId = null;
      if (appointmentId != null) {
        appId = appointmentId.toString();
      }

      // check secondary resource's overbook limit for the appt time
      if (!appointmentValidator.validateIfSlotOverbooked(category, resId, resType, apptTime,
          endTimestamp, validationErrors, "plan_visit_time", appId)) {
        ValidationException ex = new ValidationException(validationErrors);
        if (nestedException.get("patient_appointment_plan_details") != null
            && ((Map<String, Object>) nestedException.get("patient_appointment_plan_details"))
                .size() > 0) {
          apptsMap = (Map<Integer, Object>) nestedException.get("patient_appointment_plan_details");
        } else {
          apptsMap = new HashMap<Integer, Object>();
          nestedException.put("patient_appointment_plan_details", apptsMap);
        }
        int apptIndx = (Integer) appointmentInfo.get("row_id");
        if (apptsMap.get(apptIndx) != null
            && ((Map<String, Object>) apptsMap.get(apptIndx)).size() > 0) {
          apptMap = (Map<String, Object>) apptsMap.get(apptIndx);
        } else {
          apptMap = new HashMap<String, Object>();
          apptsMap.put(apptIndx, apptMap);
        }
        apptMap.putAll(ex.getErrors());
      }
      validationErrors = new ValidationErrorMap();
      if (!appointmentValidator.validateIfAppointmentExistsForPatient(apptTime, endTimestamp,
          appId, (String) params.get("mr_no"), (String) params.get("patient_name"),
          (String) params.get("patient_phone"), validationErrors, null)) {
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.putAll(ex.getErrors());
      }

      // check secondary resource's availability for the appt time
      validationErrors = new ValidationErrorMap();
      Integer centerId = params.get("center_id") != null ? (Integer) params.get("center_id") : 0;
      if (!appointmentValidator.validateResourcesAvailability(category, apptTime, endTimestamp,
          apptDate, category.getSecondaryResourceType(), resId, centerId, validationErrors,
          "secondary_resource_id", null)) {
        ValidationException ex = new ValidationException(validationErrors);
        if (nestedException.get("patient_appointment_plan_details") != null
            && ((Map<String, Object>) nestedException.get("patient_appointment_plan_details"))
                .size() > 0) {
          apptsMap = (Map<Integer, Object>) nestedException.get("patient_appointment_plan_details");
        } else {
          apptsMap = new HashMap<Integer, Object>();
          nestedException.put("patient_appointment_plan_details", apptsMap);
        }
        int apptIndx = (Integer) appointmentInfo.get("row_id");
        if (apptsMap.get(apptIndx) != null
            && ((Map<String, Object>) apptsMap.get(apptIndx)).size() > 0) {
          apptMap = (Map<String, Object>) apptsMap.get(apptIndx);
        } else {
          apptMap = new HashMap<String, Object>();
          apptsMap.put(apptIndx, apptMap);
        }
        apptMap.putAll(ex.getErrors());
      }

      // check additional resources availability for the appt time
      validationErrors = new ValidationErrorMap();
      List resourceTypes = new ArrayList<>();
      List resourceValues = new ArrayList<>();
      List<Map> insertList = (appointmentInfo.get("additional_resources_insert") != null
          ? (List<Map>) appointmentInfo.get("additional_resources_insert") : null);
      if (insertList != null) {
        for (Map row : insertList) {
          resourceTypes.add(row.get("resource_type"));
          resourceValues.add(row.get("resource_id"));
        }

      }

      if (resourceTypes != null) {
        for (int i = 0; i < resourceTypes.size(); i++) {
          if (!resourceTypes.get(i).equals("") && !resourceValues.get(i).equals("")) {
            if (!appointmentValidator.validateResourcesAvailability(category, apptTime,
                endTimestamp, apptDate, (String) resourceTypes.get(i),
                (String) resourceValues.get(i), centerId, validationErrors, null, null)) {
              ValidationException ex = new ValidationException(validationErrors);
              Map<String, Object> resourceIdsMap = new HashMap<String, Object>();
              if (resourceIdsMap.get("resources_id") != null) {
                Map<String, Object> temp = (Map<String, Object>) resourceIdsMap.get("resources_id");
                temp.putAll(ex.getErrors());
              } else {
                resourceIdsMap.put("additional_resources_insert", ex.getErrors());
              }
              if (nestedException.get("patient_appointment_plan_details") != null
                  && ((Map<String, Object>) nestedException.get("patient_appointment_plan_details"))
                      .size() > 0) {
                apptsMap = (Map<Integer, Object>) nestedException
                    .get("patient_appointment_plan_details");
              } else {
                apptsMap = new HashMap<Integer, Object>();
                nestedException.put("patient_appointment_plan_details", apptsMap);
              }
              int apptIndx = (Integer) appointmentInfo.get("row_id");
              if (apptsMap.get(apptIndx) != null
                  && ((Map<String, Object>) apptsMap.get(apptIndx)).size() > 0) {
                apptMap = (Map<String, Object>) apptsMap.get(apptIndx);
              } else {
                apptMap = new HashMap<String, Object>();
                apptsMap.put(apptIndx, apptMap);
              }
              apptMap.putAll(resourceIdsMap);
            }
          }
        }
      }

      // check additional resources overbook setting
      if (resourceTypes != null) {
        validationErrors = new ValidationErrorMap();
        for (int i = 0; i < resourceTypes.size(); i++) {
          if (!resourceTypes.get(i).equals("") && !resourceValues.get(i).equals("")) {
            if (!appointmentValidator.validateIfSlotOverbooked(category,
                (String) resourceValues.get(i), (String) resourceTypes.get(i), apptTime,
                endTimestamp, validationErrors, null, appId)) {
              ValidationException ex = new ValidationException(validationErrors);
              Map<String, Object> resourceIdsMap = new HashMap<String, Object>();
              if (resourceIdsMap.get("resources_id") != null) {
                Map<String, Object> temp = (Map<String, Object>) resourceIdsMap.get("resources_id");
                temp.putAll(ex.getErrors());
              } else {
                resourceIdsMap.put("additional_resources_insert", ex.getErrors());
              }
              if (nestedException.get("patient_appointment_plan_details") != null
                  && ((Map<String, Object>) nestedException.get("patient_appointment_plan_details"))
                      .size() > 0) {
                apptsMap = (Map<Integer, Object>) nestedException
                    .get("patient_appointment_plan_details");
              } else {
                apptsMap = new HashMap<Integer, Object>();
                nestedException.put("patient_appointment_plan_details", apptsMap);
              }
              int apptIndx = (Integer) appointmentInfo.get("row_id");
              if (apptsMap.get(apptIndx) != null
                  && ((Map<String, Object>) apptsMap.get(apptIndx)).size() > 0) {
                apptMap = (Map<String, Object>) apptsMap.get(apptIndx);
              } else {
                apptMap = new HashMap<String, Object>();
                apptsMap.put(apptIndx, apptMap);
              }
              apptMap.putAll(resourceIdsMap);
            }
          }
        }
      }
      categoryIdx++;
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * Send create plan SMS.
   *
   * @param planId the plan id
   */
  public void sendCreatePlanSMS(Integer planId) {

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("plan_id", planId);
    MessageManager mgr = new MessageManager();
    try {
      mgr.processEvent("appointment_planner", jobData);
    } catch (SQLException | ParseException | IOException exception) {
      logger.error("Exception caused while triggering appointment_planner ", exception);
      throw new HMSException("exception.unable.send.message");
    }
  }

  /**
   * Validate input params.
   *
   * @param params the params
   */
  public void validateInputParams(Map<String, Object> params) {
    ValidationErrorMap validationErrors = null;
    Map<String, Object> nestedException = new HashMap<String, Object>();
    Map<Integer, Object> planDetailsMap = null;
    Map<String, Object> planDetailMap = null;
    Map<Object, List> planVisitDateTime = new HashMap();

    for (Map patientAppointmentPlanDetail : (List<Map>) params
        .get("patient_appointment_plan_details")) {
      if (patientAppointmentPlanDetail.get("book_appointment") != null
          && parseBool((String) patientAppointmentPlanDetail.get("book_appointment"))) {
        List<Timestamp> planVisitTimeOnThisDate = null;
        Timestamp startTime = null;
        Timestamp endTime = null;
        try {
          startTime = DateUtil.parseTimestamp(
              (String) patientAppointmentPlanDetail.get("plan_visit_date"),
              (String) patientAppointmentPlanDetail.get("plan_visit_time"));
          long startTimeLong = startTime.getTime();
          startTimeLong = startTimeLong + ((Integer) patientAppointmentPlanDetail.get("duration")
              * 60 * 1000);
          endTime = new java.sql.Timestamp(startTimeLong);
        } catch (ParseException exception) {
          validationErrors = new ValidationErrorMap();
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
        }
        if (planVisitDateTime.containsKey(patientAppointmentPlanDetail.get("row_id"))) {
          planVisitTimeOnThisDate = planVisitDateTime.get(patientAppointmentPlanDetail
              .get("row_id"));

        } else {
          planVisitTimeOnThisDate = new ArrayList();
        }
        planVisitTimeOnThisDate.add(startTime);
        planVisitTimeOnThisDate.add(endTime);
        planVisitDateTime.put(patientAppointmentPlanDetail.get("row_id"), planVisitTimeOnThisDate);
      }
    }

    if (params.get("mr_no") == null || ((String) params.get("mr_no")).isEmpty()) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("mr_no", "exception.appointmentplanner.mr_no.empty");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.putAll(ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (params.get("plan_name") == null || ((String) params.get("plan_name")).trim().isEmpty()) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("plan_name", "exception.appointmentplanner.plan_name.empty");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.putAll(ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    if ((params.get("plan_name") != null || !((String) params.get("plan_name")).trim().isEmpty())
        && params.get("newPlan") == null) {
      List<BasicDynaBean> plans = appointmentPlannerRepository.getPlanNamesByPatient(
          (String) params.get("mr_no"), (Integer) params.get("center_id"));
      boolean planNameExists = false;
      for (BasicDynaBean basicDynaBean : plans) {
        if (((String) basicDynaBean.get("plan_name")).trim().equalsIgnoreCase(
            ((String) params.get("plan_name")).trim())) {
          planNameExists = true;
          break;
        }
      }
      if (planNameExists) {
        validationErrors = new ValidationErrorMap();
        validationErrors.addError("plan_name", "exception.appointmentplanner.plan_name.exists");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.putAll(ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    if (params.get("patient_appointment_plan_details") == null
        || ((List) params.get("patient_appointment_plan_details")).size() <= 0) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("patient_appointment_plan_details",
          "exception.appointmentplanner.plan_details_empty");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.putAll(ex.getErrors());
      throw new NestableValidationException(nestedException);
    } else {
      if (params.get("presc_doc_id") == null || ((String) params.get("presc_doc_id")).isEmpty()) {
        for (Map patientAppointmentPlanDetail : (List<Map>) params
            .get("patient_appointment_plan_details")) {
          if (patientAppointmentPlanDetail.get("book_appointment") != null
              && parseBool((String) patientAppointmentPlanDetail.get("book_appointment"))) {
            String presDocPref = (String) genericPreferencesService.getPreferences().get(
                "prescribing_doctor_required");
            if (presDocPref.equalsIgnoreCase("Y")) {
              validationErrors = new ValidationErrorMap();
              validationErrors.addError("presc_doc_id",
                  "exception.appointmentplanner.pres_doc.empty");
              ValidationException ex = new ValidationException(validationErrors);
              nestedException.putAll(ex.getErrors());
              throw new NestableValidationException(nestedException);
            }
          }
        }
      }
    }
    HashSet<Timestamp> appointmentDateTime = new HashSet<>();
    for (Map patientAppointmentPlanDetail : (List<Map>) params
        .get("patient_appointment_plan_details")) {

      if (nestedException.get("patient_appointment_plan_details") != null
          && ((Map<String, Object>) 
              nestedException.get("patient_appointment_plan_details")).size() > 0) {
        planDetailsMap = (Map<Integer, Object>) nestedException
            .get("patient_appointment_plan_details");
      } else {
        planDetailsMap = new HashMap<Integer, Object>();
      }
      int planIndx = (Integer) patientAppointmentPlanDetail.get("row_id");
      if (planDetailsMap.get(planIndx) != null
          && ((Map<String, Object>) planDetailsMap.get(planIndx)).size() > 0) {
        planDetailMap = (Map<String, Object>) planDetailsMap.get(planIndx);
      } else {
        planDetailMap = new HashMap<String, Object>();
        planDetailsMap.put(planIndx, planDetailMap);
      }

      if (patientAppointmentPlanDetail.get("plan_visit_date") == null
          || ((String) patientAppointmentPlanDetail.get("plan_visit_date")).isEmpty()) {
        validationErrors = new ValidationErrorMap();
        validationErrors.addError("plan_visit_date",
            "exception.appointment.planner.plan_visit_date.empty");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("patient_appointment_plan_details", planDetailsMap);
        planDetailMap.putAll(ex.getErrors());
      }

      if (patientAppointmentPlanDetail.get("consultation_reason_id") == null
          || patientAppointmentPlanDetail.get("consultation_reason_id").equals("")) {
        validationErrors = new ValidationErrorMap();
        validationErrors.addError("consultation_reason_id",
            "exception.appointment.planner.plan_visit_purpouse.empty");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("patient_appointment_plan_details", planDetailsMap);
        planDetailMap.putAll(ex.getErrors());
      }

      String apptDate = (String) patientAppointmentPlanDetail.get("plan_visit_date");
      String slotTime = (String) patientAppointmentPlanDetail.get("plan_visit_time");
      String timestampStr = slotTime != null ? apptDate + " " + slotTime : apptDate;
      Timestamp apptTime = null;
      Date appointmentDate = null;
      try {
        apptTime = DateUtil.parseTimestamp(apptDate, slotTime);
        appointmentDate = DateUtil.parseDate(apptDate);
      } catch (ParseException exception) {
        validationErrors = new ValidationErrorMap();
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
      }

      if (patientAppointmentPlanDetail.get("book_appointment") != null
          && parseBool((String) patientAppointmentPlanDetail.get("book_appointment"))) {
        if (patientAppointmentPlanDetail.get("secondary_resource_id") == null
            || ((String) patientAppointmentPlanDetail.get("secondary_resource_id")).isEmpty()) {
          validationErrors = new ValidationErrorMap();
          validationErrors.addError("secondary_resource_id",
              "exception.appointment.planner.plan_visit_resource.empty");
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("patient_appointment_plan_details", planDetailsMap);
          planDetailMap.putAll(ex.getErrors());
        }
        if (patientAppointmentPlanDetail.get("plan_visit_time") == null
            || ((String) patientAppointmentPlanDetail.get("plan_visit_time")).isEmpty()) {
          validationErrors = new ValidationErrorMap();
          validationErrors.addError("plan_visit_time",
              "exception.appointment.planner.plan_visit_time.empty");
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("patient_appointment_plan_details", planDetailsMap);
          planDetailMap.putAll(ex.getErrors());
        }
        if (apptDate != null && slotTime != null && !apptDate.equals("") && !slotTime.equals("")) {
          if (!appointmentDateTime.contains(apptTime)) {
            appointmentDateTime.add(apptTime);
          } else {
            validationErrors = new ValidationErrorMap();
            validationErrors.addError("plan_visit_time",
                "exception.appointment.planner.duplicate.date.time_slot");
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("patient_appointment_plan_details", planDetailsMap);
            planDetailMap.putAll(ex.getErrors());
          }
        }
        if (appointmentDate != null) {
          if (apptTime.before(DateUtil.getCurrentTimestamp())) {
            validationErrors = new ValidationErrorMap();
            validationErrors.addError("plan_visit_time",
                "exception.appointment.planner.past.date.time");
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("patient_appointment_plan_details", planDetailsMap);
            planDetailMap.putAll(ex.getErrors());
          }
        }

        if (apptTime != null) {
          List<Timestamp> startEndTime = planVisitDateTime.get(patientAppointmentPlanDetail
              .get("row_id"));
          boolean isOverlapping = false;
          Iterator it = planVisitDateTime.entrySet().iterator();
          while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            List<Timestamp> oftimes = (List) pair.getValue();
            if ((Integer) pair.getKey() != (Integer) patientAppointmentPlanDetail.get("row_id")
                && isOverlapping(startEndTime.get(0), startEndTime.get(1), oftimes.get(0),
                    oftimes.get(1))) {
              isOverlapping = true;
              break;
            }
          }
          if (isOverlapping) {
            validationErrors = new ValidationErrorMap();
            validationErrors.addError("plan_visit_time",
                "exception.appointment.planner.overlapping.time.slots");
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("patient_appointment_plan_details", planDetailsMap);
            planDetailMap.putAll(ex.getErrors());
          }
        }

        if (patientAppointmentPlanDetail.get("additional_resources_insert") != null) {
          for (Map additionalResource : (List<Map>) patientAppointmentPlanDetail
              .get("additional_resources_insert")) {
            if (additionalResource.get("resource_type") == null
                || additionalResource.get("resource_id") == null
                || ((String) additionalResource.get("resource_id")).isEmpty()) {
              validationErrors = new ValidationErrorMap();
              validationErrors.addError("additional_resources_insert",
                  "exception.appointment.planner.resource_id.empty");
              ValidationException ex = new ValidationException(validationErrors);
              nestedException.put("patient_appointment_plan_details", planDetailsMap);
              planDetailMap.putAll(ex.getErrors());
            }
          }
        }
      }
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * Checks if is overlapping.
   *
   * @param startTime1 the start time 1
   * @param endTime1 the end time 1
   * @param startTime2 the start time 2
   * @param endTime2 the end time 2
   * @return true, if is overlapping
   */
  public static boolean isOverlapping(Timestamp startTime1, Timestamp endTime1,
      Timestamp startTime2, Timestamp endTime2) {
    return startTime1.before(endTime2) && startTime2.before(endTime1);
  }
}

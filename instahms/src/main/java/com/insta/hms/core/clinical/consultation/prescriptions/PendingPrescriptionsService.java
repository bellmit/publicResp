package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.departments.DepartmentRepository;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentRepository;
import com.insta.hms.mdm.diagnostics.DiagnosticTestRepository;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.hospitalroles.HospitalRoleRepository;
import com.insta.hms.mdm.prescriptionsdeclinedreasonmaster.PrescDeclinedReasonRepository;
import com.insta.hms.mdm.servicedepartments.ServiceDepartmentsRepository;
import com.insta.hms.mdm.services.ServicesRepository;
import com.insta.hms.mdm.tpas.TpaRepository;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class PendingPrescriptionsService.
 */
@Service
public class PendingPrescriptionsService {

  /** Pending presc repo. */
  @LazyAutowired
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;

  /** Inv presc repo. */
  @LazyAutowired
  private InvestigationPrescriptionsRepository investigationPrescriptionsRepository;

  /** Diagnostics repo. */
  @LazyAutowired
  private DiagnosticTestRepository diagnosticTestRepository;

  /** Doctor master repo. */
  @LazyAutowired
  private DoctorRepository doctorRepository;

  /** Service master repo. */
  @LazyAutowired
  private ServicesRepository servicesRepository;

  /** Tpa master repo. */
  @LazyAutowired
  private TpaRepository tpaRepository;

  /** Generic pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The pending prescriptions details repository. */
  @LazyAutowired
  private PendingPrescriptionsRemarksRepository pendingPrescriptionsRemarksRepository;

  @LazyAutowired
  private UserService userService;

  @LazyAutowired
  private HospitalRoleRepository hospitalRoleRepository;

  @LazyAutowired
  private DepartmentRepository departmentRepository;

  @LazyAutowired
  private DiagDepartmentRepository diagDepartmentRepository;

  @LazyAutowired
  private ServiceDepartmentsRepository serviceDepartmentsRepository;

  @LazyAutowired
  private AppointmentRepository appointmentRepository;

  @LazyAutowired
  private PrescDeclinedReasonRepository prescDeclinedReasonRepository;

  @LazyAutowired
  private DoctorConsultationRepository doctorConsultationRepository;

  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository eauthPrescriptionActivitiesRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The Constant USER_ID_SESSION. */
  private static final String USER_ID_SESSION = "userId";

  /** The Constant DEFAULT_PORTAL_ID. */
  private static final String DEFAULT_PORTAL_ID = "N";

  /** The Constant SYS_LOG_COL. */
  private static final String SYS_LOG_COL = "system_log";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PendingPrescriptionsService.class);

  /**
   * Gets the pending prescriptions.
   *
   * @param requestMap the request map
   * @return the pending prescriptions
   * @throws ParseException the parse exception
   */
  public Map<String, Object> getPendingPrescriptions(Map<String, String[]> requestMap)
      throws ParseException {

    List<Map<String, Object>> mapList = new ArrayList<>();
    PagedList pagedList = pendingPrescriptionsRepository.getPendingPrescriptions(requestMap);
    List<Map<String, Object>> prescBeanList = pagedList.getDtoList();
    Map<String, Object> map = null;

    List<Long> prescIdsList = new ArrayList<>();
    for (Map<String, Object> pendPrescIds : prescBeanList) {
      prescIdsList.add((Long) pendPrescIds.get("pending_prescription_id"));
    }

    List<Map<String, Object>> schedularDetailMapList = null;
    if (!prescIdsList.isEmpty()) {
      schedularDetailMapList = appointmentRepository.getScheduleDetails(prescIdsList);
    }

    // on fetched beans, adds additional information to the beans
    for (Map<String, Object> pendingPrescBean : prescBeanList) {
      map = new HashMap<>();
      map.putAll(pendingPrescBean);

      BasicDynaBean masterBean = null;
      BasicDynaBean masterDeptBean = null;

      // prescribed by
      if (pendingPrescBean.get("prescribed_doctor_id") != null) {
        masterBean =
            doctorRepository.findByKey("doctor_id", pendingPrescBean.get("prescribed_doctor_id"));
        if (masterBean != null) {
          map.put("prescribed_by", masterBean.get("doctor_name").toString());
        }
      }

      // prescription type and item name
      String prescName = null;
      String presctype = pendingPrescBean.get("prescription_type").toString();
      String prescTypeFullName = null;
      String prescitemid = (pendingPrescBean.get("presc_item_id") != null)
          ? pendingPrescBean.get("presc_item_id").toString()
          : null;
      String prescDeptName = null;

      if (presctype.equalsIgnoreCase("Lab") || presctype.equalsIgnoreCase("Rad")) {
        masterBean = diagnosticTestRepository.findByKey("test_id", prescitemid);
        prescName = (masterBean != null) ? masterBean.get("test_name").toString() : "";
        masterDeptBean = diagDepartmentRepository.findByKey("ddept_id",
            pendingPrescBean.get("presc_item_dept_id"));
        prescDeptName = (masterDeptBean != null) ? masterDeptBean.get("ddept_name").toString() : "";
        if (presctype.equalsIgnoreCase("Lab")) {
          prescTypeFullName = "Laboratory";
        } else {
          prescTypeFullName = "Radiology";
        }
      } else if (presctype.equalsIgnoreCase("Ref") || presctype.equalsIgnoreCase("Followup")) {
        masterBean = doctorRepository.findByKey("doctor_id", prescitemid);
        prescName = (masterBean != null) ? masterBean.get("doctor_name").toString() : "";
        masterDeptBean =
            departmentRepository.findByKey("dept_id", pendingPrescBean.get("presc_item_dept_id"));
        prescDeptName = (masterDeptBean != null) ? masterDeptBean.get("dept_name").toString() : "";
        prescTypeFullName = "Doctor";
      } else if (presctype.equalsIgnoreCase("Ser")) {
        masterBean = servicesRepository.findByKey("service_id", prescitemid);
        prescName = (masterBean != null) ? masterBean.get("service_name").toString() : "";
        masterDeptBean = serviceDepartmentsRepository.findByKey("serv_dept_id",
            Integer.parseInt(pendingPrescBean.get("presc_item_dept_id").toString()));
        prescDeptName = (masterDeptBean != null) ? masterDeptBean.get("department").toString() : "";
        prescTypeFullName = "Service";
      } else {
        if (!prescitemid.equals("")) {
          masterBean = doctorRepository.findByKey("doctor_id", prescitemid);
          prescName = (masterBean != null) ? masterBean.get("doctor_name").toString() : "";
          masterDeptBean =
              departmentRepository.findByKey("dept_id", pendingPrescBean.get("presc_item_dept_id"));
          prescDeptName =
              (masterDeptBean != null) ? masterDeptBean.get("dept_name").toString() : "";
          prescTypeFullName = "Doctor";
        } else {
          masterDeptBean =
              departmentRepository.findByKey("dept_id", pendingPrescBean.get("presc_item_dept_id"));
          prescDeptName =
              (masterDeptBean != null) ? masterDeptBean.get("dept_name").toString() : "";
          prescName = prescDeptName;
        }
      }
      map.put("presc_type_full_name", prescTypeFullName);
      map.put("presc_item_dept_name", prescDeptName);
      map.put("prescription_description", prescName);

      // primary and secondary sponsor / tpa
      String priSponsorId = "pri_sponsor_id";
      if ((pendingPrescBean.get(priSponsorId) != null)
          && (!pendingPrescBean.get(priSponsorId).equals(""))) {
        map.put("primary_tpa_name",
            tpaRepository.findByKey("tpa_id", pendingPrescBean.get(priSponsorId)).get("tpa_name"));
        String secSponsorId = "sec_sponsor_id";
        if (pendingPrescBean.get(secSponsorId) != null
            && !pendingPrescBean.get(secSponsorId).equals("")) {
          map.put("secondary_tpa_name", tpaRepository
              .findByKey("tpa_id", pendingPrescBean.get(secSponsorId)).get("tpa_name"));
        }
      }

      // Assigned to can be role id or user id
      map.put("assigned_to", "");
      if (pendingPrescBean.get("assigned_to_user") != null
          && !pendingPrescBean.get("assigned_to_user").equals("")) {
        map.put("assigned_to", pendingPrescBean.get("assigned_to_user"));
      } else if (pendingPrescBean.get("assigned_role_name") != null
          && !pendingPrescBean.get("assigned_role_name").equals("")) {
        map.put("assigned_to", pendingPrescBean.get("assigned_role_name"));
      }
      map.put("assigned_at", pendingPrescBean.get("assigned_date"));

      // prescription remarks.
      BasicDynaBean prescriptionRemarksBean = pendingPrescriptionsRemarksRepository
          .getLatestRemarks((Long) pendingPrescBean.get("pending_prescription_id"));
      if (prescriptionRemarksBean != null) {
        map.put("last_remark", prescriptionRemarksBean.get("remark"));
        map.put("last_remark_by", prescriptionRemarksBean.get("remark_added_by"));
      } else {
        map.put("last_remark", "");
        map.put("last_remark_by", "");
      }

      // preauth end date
      if (pendingPrescBean.get("preauth_id") != null) {
        BasicDynaBean expBean =
            pendingPrescriptionsRepository.getPreauthEndDate(pendingPrescBean.get("preauth_id"));
        if (expBean != null) {
          map.put("preauth_end_date", expBean.get("end_date"));
        }
      }

      // Schedule details
      for (Map<String, Object> schMap : schedularDetailMapList) {
        if (schMap.get("patient_presc_id")
            .equals(pendingPrescBean.get("pending_prescription_id"))) {
          map.put("schedulerDetails", schMap);
        }
      }

      map.put("schedulable", true);
      mapList.add(map);
    }
    Map<String, Object> returnMap = new HashMap<>();
    returnMap.put("prescriptions", mapList);
    returnMap.put("record_count", pagedList.getTotalRecords());
    returnMap.put("num_pages", pagedList.getNumPages());
    return returnMap;
  }

  /**
   * Gets the pending prescriptions.
   *
   * @param request the request
   * @return the pending prescriptions
   */
  public List<Map<String, Object>> getPrescriptionRemarks(HttpServletRequest request) {
    List<Map<String, Object>> mapList = new ArrayList<>();
    if (request.getParameter("pending_prescription_id") == null
        || request.getParameter("pending_prescription_id").isEmpty()) {
      logger.error("pending_prescription_id is null/empty");
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    } else {
      for (BasicDynaBean pendingPrescriptionRemarks : pendingPrescriptionsRemarksRepository
          .getRemarks(
              Integer.parseInt(request.getParameter("pending_prescription_id").toString()))) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("remark", pendingPrescriptionRemarks.get("remark"));
        responseMap.put("assigned_to", pendingPrescriptionRemarks.get("assigned_to"));
        responseMap.put("remark_on", pendingPrescriptionRemarks.get("modified_date_time"));
        responseMap.put("remark_by", pendingPrescriptionRemarks.get("remark_added_by"));
        mapList.add(responseMap);
      }
    }
    return mapList;
  }

  /**
   * Adds the pending prescriptions and remark.
   *
   * @param requestBody the request body
   * @return true, if successful
   */
  public boolean addPendingPrescriptionsAndRemark(ModelMap requestBody) {
    Long pendPrescId = Long.valueOf(requestBody.get("pending_prescription_id").toString());

    BasicDynaBean pendingPrescriptionBean =
        pendingPrescriptionsRepository.findByKey("pat_pending_presc_id", pendPrescId);

    if (pendingPrescriptionBean != null) {
      // get current time
      Calendar calendar = Calendar.getInstance();
      java.util.Date now = calendar.getTime();
      java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
      String loggedInUserIdStr =
          sessionService.getSessionAttributes().get(USER_ID_SESSION).toString();
      if (requestBody.get("patient_declined") != null
          && requestBody.get("patient_declined").equals("Y")
          && requestBody.get("declined_reason_id") != null
          && !requestBody.get("declined_reason_id").equals("")
          && (pendingPrescriptionBean.get("declined_reason_id") != null || pendingPrescriptionBean
              .get("declined_reason_id") != requestBody.get("declined_reason_id"))) {
        pendingPrescriptionBean.set("declined_reason_id", requestBody.get("declined_reason_id"));
        pendingPrescriptionBean.set("declined_at", currentTime);
        pendingPrescriptionBean.set("declined_by", loggedInUserIdStr);
      } else {
        pendingPrescriptionBean.set("declined_reason_id", null);
        pendingPrescriptionBean.set("declined_at", null);
        pendingPrescriptionBean.set("declined_by", null);
      }
      if (pendingPrescriptionBean.get("presc_item_type").equals("Dep")) {
        if (requestBody.get("doctor_id") != null && !requestBody.get("doctor_id").equals("")) {
          pendingPrescriptionBean.set("presc_item_id",
              StringUtils.trimToEmpty(requestBody.get("doctor_id").toString()));
        } else {
          pendingPrescriptionBean.set("presc_item_id", "");
        }
      }

      BasicDynaBean prescriptionDetails = pendingPrescriptionsRemarksRepository.getBean();
      if (requestBody.get("remark") != null && !requestBody.get("remark").equals("")) {
        prescriptionDetails.set("remark",
            StringUtils.trimToEmpty(requestBody.get("remark").toString()));
      } else {
        prescriptionDetails.set("remark", null);
      }
      if (requestBody.get("assigned_to") != null && !requestBody.get("assigned_to").equals("")) {
        Map<String, Object> assignedMap = (Map<String, Object>) requestBody.get("assigned_to");
        if (assignedMap.get("type").equals("role")) {
          prescriptionDetails.set("assigned_to_role_id",
              Integer.parseInt(assignedMap.get("id").toString()));
          pendingPrescriptionBean.set("assigned_to_role_id",
              Integer.parseInt(assignedMap.get("id").toString()));
          // setting assigned_to_user as null
          prescriptionDetails.set("assigned_to_user_id", null);
          pendingPrescriptionBean.set("assigned_to_user_id", null);
        }
        if (assignedMap.get("type").equals("user")) {
          prescriptionDetails.set("assigned_to_user_id", assignedMap.get("id").toString());
          pendingPrescriptionBean.set("assigned_to_user_id", assignedMap.get("id").toString());
          // setting assigned_to_role as null
          prescriptionDetails.set("assigned_to_role_id", null);
          pendingPrescriptionBean.set("assigned_to_role_id", null);
        }
        pendingPrescriptionBean.set("modified_by", loggedInUserIdStr);
        pendingPrescriptionBean.set("modified_at", currentTime);
      } else {
        prescriptionDetails.set("assigned_to_role_id",
            pendingPrescriptionBean.get("assigned_to_role_id"));
        prescriptionDetails.set("assigned_to_user_id",
            pendingPrescriptionBean.get("assigned_to_user_id"));
      }
      prescriptionDetails.set("modified_by", loggedInUserIdStr);
      prescriptionDetails.set("pending_prescription_id", pendPrescId);

      prescriptionDetails.set(SYS_LOG_COL, false);

      List<BasicDynaBean> prescriptionDetailsLogList = new ArrayList<>();
      if (requestBody.get("remark") != null) {
        prescriptionDetailsLogList.add(prescriptionDetails);
      }

      // creates a bean to store audit log as remarks
      BasicDynaBean mainBeanBeforeUpdate =
          pendingPrescriptionsRepository.findByKey("pat_pending_presc_id", pendPrescId);

      // check for doctor if the type is department.
      if (pendingPrescriptionBean.get("presc_item_type").equals("Dep")) {
        BasicDynaBean prescLogItem = pendingPrescriptionsRemarksRepository.getBean();
        prescLogItem.set("modified_by", loggedInUserIdStr);
        prescLogItem.set("pending_prescription_id",
            pendingPrescriptionBean.get("pat_pending_presc_id"));
        prescLogItem.set(SYS_LOG_COL, true);
        prescLogItem.set("assigned_to_role_id", prescriptionDetails.get("assigned_to_role_id"));
        prescLogItem.set("assigned_to_user_id", prescriptionDetails.get("assigned_to_user_id"));
        if (!mainBeanBeforeUpdate.get("presc_item_id").toString().equals("")) {
          if (pendingPrescriptionBean.get("presc_item_id") != null) {
            if (!mainBeanBeforeUpdate.get("presc_item_id")
                .equals(pendingPrescriptionBean.get("presc_item_id"))) {
              String prevDoctor =
                  doctorRepository.findByKey("doctor_id", mainBeanBeforeUpdate.get("presc_item_id"))
                      .get("doctor_name").toString();
              String newDoctor = doctorRepository
                  .findByKey("doctor_id", pendingPrescriptionBean.get("presc_item_id"))
                  .get("doctor_name").toString();
              prescLogItem.set("remark",
                  "Updated referral doctor from " + prevDoctor + " to " + newDoctor);
              prescriptionDetailsLogList.add(prescLogItem);
            }
          } else {
            String prevDoctor =
                doctorRepository.findByKey("doctor_id", mainBeanBeforeUpdate.get("presc_item_id"))
                    .get("doctor_name").toString();
            prescLogItem.set("remark", "Removed referral doctor from " + prevDoctor);
            prescriptionDetailsLogList.add(prescLogItem);
          }
        } else {
          if (pendingPrescriptionBean.get("presc_item_id") != null) {
            BasicDynaBean docBean = doctorRepository.findByKey("doctor_id",
                pendingPrescriptionBean.get("presc_item_id"));
            if (docBean != null) {
              String newDoctor = docBean.get("doctor_name").toString();
              prescLogItem.set("remark", "Assigned referral doctor to " + newDoctor);
              prescriptionDetailsLogList.add(prescLogItem);
            }
          }
        }
      }

      // check for declined by
      BasicDynaBean prescLogDeclined = pendingPrescriptionsRemarksRepository.getBean();
      prescLogDeclined.set("modified_by", loggedInUserIdStr);
      prescLogDeclined.set("pending_prescription_id",
          pendingPrescriptionBean.get("pat_pending_presc_id"));
      prescLogDeclined.set(SYS_LOG_COL, true);
      prescLogDeclined.set("assigned_to_role_id", prescriptionDetails.get("assigned_to_role_id"));
      prescLogDeclined.set("assigned_to_user_id", prescriptionDetails.get("assigned_to_user_id"));
      if (mainBeanBeforeUpdate.get("declined_reason_id") != null) {
        if (pendingPrescriptionBean.get("declined_reason_id") != null) {
          if (!mainBeanBeforeUpdate.get("declined_reason_id")
              .equals(pendingPrescriptionBean.get("declined_reason_id"))) {
            String prevReason = prescDeclinedReasonRepository
                .findByKey("declined_reason_id", mainBeanBeforeUpdate.get("declined_reason_id"))
                .get("reason").toString();
            String newReason = prescDeclinedReasonRepository
                .findByKey("declined_reason_id", pendingPrescriptionBean.get("declined_reason_id"))
                .get("reason").toString();
            prescLogDeclined.set("remark",
                "Updated declined reason from " + prevReason + " to " + newReason);
            prescriptionDetailsLogList.add(prescLogDeclined);
          }
        } else {
          String prevReason = prescDeclinedReasonRepository
              .findByKey("declined_reason_id", mainBeanBeforeUpdate.get("declined_reason_id"))
              .get("reason").toString();
          prescLogDeclined.set("remark",
              "Removed declined for the prescription which has the reason " + prevReason);
          prescriptionDetailsLogList.add(prescLogDeclined);
        }
      } else {
        if (pendingPrescriptionBean.get("declined_reason_id") != null) {
          String newReason = prescDeclinedReasonRepository
              .findByKey("declined_reason_id", pendingPrescriptionBean.get("declined_reason_id"))
              .get("reason").toString();
          prescLogDeclined.set("remark", "Declined the prescription with reason " + newReason);
          prescriptionDetailsLogList.add(prescLogDeclined);
        }
      }

      // Check for assigned to user / role
      BasicDynaBean prescLogAssigned = pendingPrescriptionsRemarksRepository.getBean();
      prescLogAssigned.set("modified_by", loggedInUserIdStr);
      prescLogAssigned.set("pending_prescription_id",
          pendingPrescriptionBean.get("pat_pending_presc_id"));
      prescLogAssigned.set(SYS_LOG_COL, true);
      prescLogAssigned.set("assigned_to_role_id", prescriptionDetails.get("assigned_to_role_id"));
      prescLogAssigned.set("assigned_to_user_id", prescriptionDetails.get("assigned_to_user_id"));
      if (requestBody.get("assigned_to") != null) {
        if (pendingPrescriptionBean.get("assigned_to_role_id") != null) {
          if (mainBeanBeforeUpdate.get("assigned_to_role_id") != null) {
            if (!mainBeanBeforeUpdate.get("assigned_to_role_id")
                .equals(pendingPrescriptionBean.get("assigned_to_role_id"))) {
              String prevRole = hospitalRoleRepository
                  .findByKey("hosp_role_id", mainBeanBeforeUpdate.get("assigned_to_role_id"))
                  .get("hosp_role_name").toString();
              String newRole = hospitalRoleRepository
                  .findByKey("hosp_role_id", pendingPrescriptionBean.get("assigned_to_role_id"))
                  .get("hosp_role_name").toString();
              prescLogAssigned.set("remark",
                  "Updated assigned to role, from " + prevRole + " to " + newRole);
              prescriptionDetailsLogList.add(prescLogAssigned);
            }
          } else {
            if (mainBeanBeforeUpdate.get("assigned_to_user_id") != null) {
              String userBeforeUpdate = mainBeanBeforeUpdate.get("assigned_to_user_id").toString();
              String prevUser = userService.getUserDisplayName(userBeforeUpdate);
              String newRole = hospitalRoleRepository
                  .findByKey("hosp_role_id", pendingPrescriptionBean.get("assigned_to_role_id"))
                  .get("hosp_role_name").toString();
              prescLogAssigned.set("remark",
                  "Updated assigned to, from user " + prevUser + " to role " + newRole);
              prescriptionDetailsLogList.add(prescLogAssigned);
            } else {
              String newRole = hospitalRoleRepository
                  .findByKey("hosp_role_id", pendingPrescriptionBean.get("assigned_to_role_id"))
                  .get("hosp_role_name").toString();
              prescLogAssigned.set("remark", "Assigned to role " + newRole);
              prescriptionDetailsLogList.add(prescLogAssigned);
            }
          }
        } else {
          if (pendingPrescriptionBean.get("assigned_to_user_id") != null) {
            if (mainBeanBeforeUpdate.get("assigned_to_user_id") != null) {
              if (!mainBeanBeforeUpdate.get("assigned_to_user_id")
                  .equals(pendingPrescriptionBean.get("assigned_to_user_id"))) {
                String userBeforeUpdate =
                    mainBeanBeforeUpdate.get("assigned_to_user_id").toString();
                String prevUser = userService.getUserDisplayName(userBeforeUpdate);
                String userPendingPrescription =
                    pendingPrescriptionBean.get("assigned_to_user_id").toString();
                String newUser = userService.getUserDisplayName(userPendingPrescription);
                prescLogAssigned.set("remark",
                    "Updated assigned to user, from " + prevUser + " to " + newUser);
                prescriptionDetailsLogList.add(prescLogAssigned);
              }
            } else {
              if (mainBeanBeforeUpdate.get("assigned_to_role_id") != null) {
                String userPendingPrescription =
                    pendingPrescriptionBean.get("assigned_to_user_id").toString();
                String newUser = userService.getUserDisplayName(userPendingPrescription);
                String prevRole = hospitalRoleRepository
                    .findByKey("hosp_role_id", mainBeanBeforeUpdate.get("assigned_to_role_id"))
                    .get("hosp_role_name").toString();
                prescLogAssigned.set("remark",
                    "Updated assigned to, from role " + prevRole + " to user " + newUser);
                prescriptionDetailsLogList.add(prescLogAssigned);
              } else {
                String userPendingPrescription =
                    pendingPrescriptionBean.get("assigned_to_user_id").toString();
                String newUser = userService.getUserDisplayName(userPendingPrescription);
                prescLogAssigned.set("remark", "Assigned to user " + newUser);
                prescriptionDetailsLogList.add(prescLogAssigned);
              }
            }
          }
        }
      }
      if (!prescriptionDetailsLogList.isEmpty()) {
        pendingPrescriptionsRemarksRepository.batchInsert(prescriptionDetailsLogList);
      }
      Map<String, Object> updateKey = new HashMap<>();
      updateKey.put("pat_pending_presc_id", pendPrescId);
      pendingPrescriptionsRepository.update(pendingPrescriptionBean, updateKey);
    }
    return true;
  }

  /**
   * Inserts prescription details in table.
   * 
   * @param prescriptions the bean
   * @param request the request
   * @throws ParseException the Parse Exception
   */
  public void insertPrescriptions(BasicDynaBean prescriptions, Map<String, Object> request) {
    BasicDynaBean mainBean = pendingPrescriptionsRepository.getBean();
    mainBean.set("patient_presc_id",
        Long.valueOf((prescriptions.get("patient_presc_id").toString())));
    mainBean.set("presc_item_id", request.get("item_id"));
    String prescitemtype = "";
    String itemtype = request.get("item_type").toString();
    String depId = null;
    if (itemtype.equals("Inv.")) {
      // Don't save the Investigation, if it's a package.
      if (request.get("is_package").toString().equals("true")) {
        return;
      }
      BasicDynaBean prescDetails = investigationPrescriptionsRepository
          .getInvestigationType(request.get("item_id").toString());
      String ptype = prescDetails.get("patient_prescription_type").toString();
      depId = prescDetails.get("dept_id").toString();
      if (ptype.equalsIgnoreCase("DEP_LAB")) {
        prescitemtype = "Lab";
      } else if (ptype.equalsIgnoreCase("DEP_RAD")) {
        prescitemtype = "Rad";
      }
    } else if (itemtype.equals("Service")) {
      prescitemtype = "Ser";
      depId = servicesRepository.findByKey("service_id", request.get("item_id")).get("serv_dept_id")
          .toString();
    } else if (itemtype.equals("Doctor")) {
      if (StringUtils.isNotEmpty(request.get("presc_activity_type").toString())
          && request.get("presc_activity_type").equals("DEPT")) {
        prescitemtype = "Dep";
        depId = request.get("item_id").toString();
        mainBean.set("presc_item_id", "");
      } else {
        prescitemtype = "Ref";
        depId = doctorRepository.findByKey("doctor_id", request.get("item_id")).get("dept_id")
            .toString();
      }
    }
    mainBean.set("presc_item_dept_id", depId);
    mainBean.set("presc_item_type", prescitemtype);
    mainBean.set("presc_item_qty", request.get("prescribed_qty"));
    mainBean.set("visit_id", request.get("visit_id"));

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      mainBean.set("start_datetime",
          new Timestamp(sdf.parse(request.get("start_datetime").toString()).getTime()));
      mainBean.set("prescribed_date",
          new Timestamp(sdf.parse(request.get("prescribed_date").toString()).getTime()));
    } catch (ParseException exception) {
      mainBean.set("start_datetime", "");
      mainBean.set("prescribed_date", "");
    }
    mainBean.set("prescribed_by", request.get("doctor_id"));
    mainBean.set("status", prescriptions.get("status"));
    ModelMap remarksMap = new ModelMap();
    String prescriptionRemarks = (request.get("item_remarks") != null)
        ? StringUtils.trimToEmpty(request.get("item_remarks").toString())
        : "";

    if (prescitemtype.equals("Ser")) {
      for (int i = 0; i < (int) request.get("prescribed_qty"); i++) {
        if (pendingPrescriptionsRepository.insert(mainBean) > 0
            && !prescriptionRemarks.equals("")) {
          remarksMap.put("remark", prescriptionRemarks);
          remarksMap.put("pending_prescription_id",
              pendingPrescriptionsRepository.getCurrentSequence());
          this.addPendingPrescriptionsAndRemark(remarksMap);
        }
      }
    } else {
      if (pendingPrescriptionsRepository.insert(mainBean) > 0 && !prescriptionRemarks.equals("")) {
        remarksMap.put("remark", prescriptionRemarks);
        remarksMap.put("pending_prescription_id",
            pendingPrescriptionsRepository.getCurrentSequence());
        this.addPendingPrescriptionsAndRemark(remarksMap);
      }
    }
  }

  /**
   * Updates prescriptions in table.
   * 
   * @param prescription the prescription
   */
  public void updatePrescriptions(Map<String, Object> prescription) {
    List<BasicDynaBean> mainBeanList = null;
    Map<String, Object> filterMap = new HashMap<>();
    Long itemId = Long.valueOf(prescription.get("item_prescribed_id").toString());
    filterMap.put("patient_presc_id", itemId);
    mainBeanList = pendingPrescriptionsRepository.findByCriteria(filterMap);
    String itemtype = prescription.get("item_type").toString();

    if (itemtype.equals("Service")) {
      int noOfExistingServiceQty = mainBeanList.size();
      int noOfEditedServiceQty = (int) prescription.get("prescribed_qty");
      Map<String, Object> map = null;
      if (noOfExistingServiceQty < noOfEditedServiceQty) {
        BasicDynaBean bean = pendingPrescriptionsRepository.findByKey("patient_presc_id", itemId);
        if (bean != null) {
          for (int i = noOfExistingServiceQty; i < noOfEditedServiceQty; i++) {
            BasicDynaBean newBean = pendingPrescriptionsRepository.getBean();
            ConversionUtils.copyBeanToBean(bean, newBean);
            newBean.set("pat_pending_presc_id",
                Long.valueOf(pendingPrescriptionsRepository.getNextSequence()));
            pendingPrescriptionsRepository.insert(newBean);
          }
        }
      } else if (noOfExistingServiceQty > noOfEditedServiceQty) {
        map = new HashMap<>();
        map.put("columnName", "patient_presc_id");
        map.put("columnValue", itemId);
        map.put("limit", noOfExistingServiceQty - noOfEditedServiceQty);
        List<BasicDynaBean> beanListToDelete =
            pendingPrescriptionsRepository.getPendingServicePrescriptionId(map);
        if (!beanListToDelete.isEmpty()) {
          List<Object> deleteKeys = new ArrayList<>();
          for (BasicDynaBean bean : beanListToDelete) {
            deleteKeys.add(bean.get("pat_pending_presc_id"));
          }
          pendingPrescriptionsRemarksRepository.batchDelete("pending_prescription_id", deleteKeys);
          pendingPrescriptionsRepository.batchDelete("pat_pending_presc_id", deleteKeys);
        }
      }
    }
    Map<String, Object> pendPresc = new HashMap<>();
    mainBeanList = pendingPrescriptionsRepository.findByCriteria(filterMap);
    for (BasicDynaBean bean : mainBeanList) {
      pendPresc.put("pat_pending_presc_id", bean.get("pat_pending_presc_id"));
      try {
        bean.set("start_datetime", new Timestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            .parse(prescription.get("start_datetime").toString()).getTime()));
        bean.set("prescribed_date", new Timestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            .parse(prescription.get("prescribed_date").toString()).getTime()));
        if (itemtype.equals("Service")) {
          bean.set("presc_item_qty", (int) prescription.get("prescribed_qty"));
        }
      } catch (ParseException exception) {
        logger.error(exception.toString());
        throw new HMSException();
      }
      pendingPrescriptionsRepository.update(bean, pendPresc);
    }

    // if remarks has edited in the consultation,insert an entry in remarks log
    BasicDynaBean prescriptionRemarksBean =
        pendingPrescriptionsRepository.findByKey("patient_presc_id", itemId);
    ModelMap remarksMap = new ModelMap();
    String remarksEdited = (prescription.get("item_remarks") != null)
        ? StringUtils.trimToEmpty(prescription.get("item_remarks").toString())
        : "";

    if (prescriptionRemarksBean != null) {
      remarksMap.put("remark", remarksEdited);
      remarksMap.put("pending_prescription_id",
          prescriptionRemarksBean.get("pat_pending_presc_id"));
      this.addPendingPrescriptionsAndRemark(remarksMap);
    }

  }

  /**
   * Get department id for prescriptions.
   *
   * @param prescriptionsList the prescriptions list
   * @return the map
   */
  private Map<String, Map<String, String>> getDepartmentIdForPrescriptions(
      List<Map<String, Object>> prescriptionsList) {
    Map<String, Map<String, String>> listReturn = new HashMap<>();
    List<String> prescInvestigations = null;
    List<String> prescServices = null;
    List<String> prescRefferals = null;
    for (Map<String, Object> prescriptions : prescriptionsList) {
      if ("DIA".equals(prescriptions.get("item_type"))) {
        if (prescInvestigations == null) {
          prescInvestigations = new ArrayList<>();
        }
        prescInvestigations.add(prescriptions.get("item_id").toString());
      } else if ("SER".equals(prescriptions.get("item_type"))) {
        if (prescServices == null) {
          prescServices = new ArrayList<>();
        }
        prescServices.add(prescriptions.get("item_id").toString());
      } else if ("DOC".equals(prescriptions.get("item_type"))) {
        if (prescRefferals == null) {
          prescRefferals = new ArrayList<>();
        }
        prescRefferals.add(prescriptions.get("item_id").toString());
      }
    }
    if (prescInvestigations != null && !prescInvestigations.isEmpty()) {
      for (BasicDynaBean prescDetails : investigationPrescriptionsRepository
          .getInvestigationType(prescInvestigations)) {
        Map<String, String> returnMap = new HashMap<>();
        String ptype = prescDetails.get("patient_prescription_type").toString();
        if (ptype.equalsIgnoreCase("DEP_LAB")) {
          returnMap.put("prescription_type", "Lab");
        } else if (ptype.equalsIgnoreCase("DEP_RAD")) {
          returnMap.put("prescription_type", "Rad");
        }
        returnMap.put("department_id", prescDetails.get("dept_id").toString());
        listReturn.put(prescDetails.get("presc_item_id").toString(), returnMap);
      }
    }
    if (prescServices != null && !prescServices.isEmpty()) {
      for (BasicDynaBean prescDetails : servicesRepository.getServices(prescServices)) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("prescription_type", "Ser");
        returnMap.put("department_id", prescDetails.get("serv_dept_id").toString());
        listReturn.put(prescDetails.get("service_id").toString(), returnMap);
      }
    }
    if (prescRefferals != null && !prescRefferals.isEmpty()) {
      for (BasicDynaBean prescDetails : doctorRepository.getDoctors(prescRefferals)) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("prescription_type", "Ref");
        returnMap.put("department_id", prescDetails.get("dept_id").toString());
        listReturn.put(prescDetails.get("doctor_id").toString(), returnMap);
      }
    }
    return listReturn;
  }

  /**
   * Inserts or updates follow up prescription details in table.
   * 
   * @param action insert or update
   * @param followup the bean list
   */
  public void insertUpdateFollowUpPrescriptions(String action, List<BasicDynaBean> followup) {
    BasicDynaBean mainBean = pendingPrescriptionsRepository.getBean();
    if (action.equals("insert")) {
      for (BasicDynaBean followUpBean : followup) {
        mainBean.set("followup_id", followUpBean.get("followup_id"));
        mainBean.set("presc_item_id", followUpBean.get("followup_doctor_id"));
        mainBean.set("prescribed_by", followUpBean.get("followup_doctor_id"));
        mainBean.set("start_datetime",
            new Timestamp(((Date) followUpBean.get("followup_date")).getTime()));
        mainBean.set("presc_item_type", "Followup");
        mainBean.set("status", "P");
        mainBean.set("visit_id", followUpBean.get("patient_id"));
        ModelMap remarksMap = new ModelMap();
        String followUpRemark = (followUpBean.get("followup_remarks") != null)
            ? StringUtils.trimToEmpty(followUpBean.get("followup_remarks").toString())
            : "";
        if (pendingPrescriptionsRepository.insert(mainBean) > 0 && !followUpRemark.equals("")) {
          remarksMap.put("remark", followUpRemark);
          remarksMap.put("pending_prescription_id",
              pendingPrescriptionsRepository.getCurrentSequence());
          this.addPendingPrescriptionsAndRemark(remarksMap);
        }
      }
    } else if (action.equals("update")) {
      List<BasicDynaBean> updateBeansList = new ArrayList<>();
      Map<String, Object> updateKeysMap = new HashMap<>();
      List<Object> updateKeys = new ArrayList<>();
      for (BasicDynaBean followUpBean : followup) {
        mainBean.set("followup_id", followUpBean.get("followup_id"));
        mainBean.set("presc_item_id", followUpBean.get("followup_doctor_id"));
        mainBean.set("prescribed_by", followUpBean.get("followup_doctor_id"));
        mainBean.set("visit_id", followUpBean.get("patient_id"));
        mainBean.set("start_datetime",
            new Timestamp(((Date) followUpBean.get("followup_date")).getTime()));
        updateBeansList.add(mainBean);
        updateKeys.add(followUpBean.get("followup_id"));
      }
      updateKeysMap.put("followup_id", updateKeys);
      pendingPrescriptionsRepository.batchUpdate(updateBeansList, updateKeysMap);
    }
  }

  /**
   * Inserts or updates preauth prescription in PPD table.
   * 
   * @param consultationId cons id
   * @param preauthBeans the bean list
   */
  public void insertUpdatePreauthPrescriptions(List<Map<String, Object>> preauthBeans,
      Integer consultationId) {
    String consultingDoctor = null;
    List<Map<String, Object>> prescPriorAuthItems = new ArrayList<>();
    List<Map<String, Object>> prescNewItems = new ArrayList<>();
    List<Map<String, Object>> prescUpdatedItems = new ArrayList<>();
    List<Integer> preauthActDelIds = null;
    for (Map<String, Object> preauthBean : preauthBeans) {
      Map<String, Object> prescItems = new HashMap<>();
      prescItems.put("item_type", preauthBean.get("patient_prescription_type"));
      prescItems.put("item_id", preauthBean.get("preauth_act_item_id"));
      if (preauthBean.get("action").equals("insert")) {
        prescNewItems.add(preauthBean);
        prescPriorAuthItems.add(prescItems);
      } else if (preauthBean.get("action").equals("update")) {
        prescPriorAuthItems.add(prescItems);
        prescUpdatedItems.add(preauthBean);
      } else if (preauthBean.get("action").equals("delete")) {
        if (preauthActDelIds == null) {
          preauthActDelIds = new ArrayList<>();
        }
        preauthActDelIds.add((Integer) preauthBean.get("preauth_activity_id"));
      }
    }
    BasicDynaBean consultation =
        doctorConsultationRepository.findByKey("consultation_id", consultationId);
    consultingDoctor = (consultation != null) ? consultation.get("doctor_name").toString() : "";
    Map<String, Map<String, String>> prescItemDepts =
        getDepartmentIdForPrescriptions(prescPriorAuthItems);
    // new priorauth items
    for (Map<String, Object> itemMap : prescNewItems) {
      BasicDynaBean pendingPrescNewBean = pendingPrescriptionsRepository.getBean();

      pendingPrescNewBean.set("preauth_activity_id",
          Long.valueOf(itemMap.get("preauth_activity_id").toString()));
      if (null != itemMap.get("preauth_required") && "Y".equals(itemMap.get("preauth_required"))) {
        pendingPrescNewBean.set("preauth_activity_status", "O");
      }
      pendingPrescNewBean.set("presc_item_id", itemMap.get("preauth_act_item_id"));
      pendingPrescNewBean.set("prescribed_by", consultingDoctor);
      pendingPrescNewBean.set("start_datetime", itemMap.get("prescribed_date"));
      if (prescItemDepts.containsKey(itemMap.get("preauth_act_item_id"))) {
        Map<String, String> prescDepartment =
            prescItemDepts.get(itemMap.get("preauth_act_item_id"));
        pendingPrescNewBean.set("presc_item_dept_id", prescDepartment.get("department_id"));
        pendingPrescNewBean.set("presc_item_type", prescDepartment.get("prescription_type"));
      } else {
        pendingPrescNewBean.set("presc_item_dept_id", "");
        pendingPrescNewBean.set("presc_item_type", "");
      }
      pendingPrescNewBean.set("presc_item_qty", itemMap.get("quantity"));
      pendingPrescNewBean.set("status", "P");
      pendingPrescNewBean.set("visit_id", itemMap.get("visit_id"));
      ModelMap remarksMap = new ModelMap();
      String presRemark = (itemMap.get("preauth_act_item_remarks") != null)
          ? StringUtils.trimToEmpty(itemMap.get("preauth_act_item_remarks").toString())
          : "";
      if (pendingPrescNewBean.get("presc_item_type").equals("Ser")) {
        for (int i = 0; i < (int) itemMap.get("quantity"); i++) {
          if (pendingPrescriptionsRepository.insert(pendingPrescNewBean) > 0
              && !presRemark.equals("")) {
            remarksMap.put("remark", presRemark);
            remarksMap.put("pending_prescription_id",
                pendingPrescriptionsRepository.getCurrentSequence());
            this.addPendingPrescriptionsAndRemark(remarksMap);
          }
        }
      } else {
        if (pendingPrescriptionsRepository.insert(pendingPrescNewBean) > 0
            && !presRemark.equals("")) {
          remarksMap.put("remark", presRemark);
          remarksMap.put("pending_prescription_id",
              pendingPrescriptionsRepository.getCurrentSequence());
          this.addPendingPrescriptionsAndRemark(remarksMap);
        }
      }
      String preAuthStatus = (String) itemMap.get("preauth_act_status");
      if (itemMap.get("patient_prescription_type").equals("SER")) {
        int noOfServiceQty = (int) itemMap.get("quantity");
        int noOfApprovedQty = (int) itemMap.get("approved_qty");
        int noOfDenied = 0;
        int noOfAvbleQty = 0;
        // if status open or sent then update all to O or S.
        if ("O".equals(preAuthStatus) || "S".equals(preAuthStatus)) {
          updatePriorAuthStatus((int)itemMap.get("preauth_activity_id"), preAuthStatus, 
              noOfServiceQty);
        }
        // if not update appropriately.
        if ("C".equals(preAuthStatus)) {
          noOfDenied = noOfServiceQty - noOfApprovedQty;
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "C", noOfApprovedQty);
        }
        if ("D".equals(preAuthStatus)) {
          noOfDenied = noOfServiceQty - noOfApprovedQty;
          noOfAvbleQty = noOfServiceQty - noOfDenied;
        }
        if (noOfDenied > 0) {
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "D", noOfDenied);
        }
        if (noOfAvbleQty > 0) {
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "C", noOfAvbleQty);
        }
      } else {
        updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), preAuthStatus, 1);
      }
    }

    // Updated priorauth items
    List<Object> updateKeys = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<BasicDynaBean> updateBeansList = new ArrayList<>();
    for (Map<String, Object> itemMap : prescUpdatedItems) {
      Map<String, Object> preauthFilterMap = new HashMap<>();
      preauthFilterMap.put("preauth_activity_id", itemMap.get("preauth_activity_id"));
      String status = (String) itemMap.get("preauth_act_status");
      if (itemMap.get("patient_prescription_type").equals("SER")) {
        List<BasicDynaBean> mainBeanList =
            pendingPrescriptionsRepository.findByCriteria(preauthFilterMap);
        int noOfExistingServiceQty = mainBeanList.size();
        int noOfEditedServiceQty = (int) itemMap.get("quantity");
        Map<String, Object> map = null;
        if (noOfExistingServiceQty > 0 && noOfExistingServiceQty < noOfEditedServiceQty) {
          BasicDynaBean bean = mainBeanList.get(0);
          if (bean != null) {
            // change the status of prescription
            bean.set("status", "P");
            for (int i = noOfExistingServiceQty; i < noOfEditedServiceQty; i++) {
              BasicDynaBean newBean = pendingPrescriptionsRepository.getBean();
              ConversionUtils.copyBeanToBean(bean, newBean);
              newBean.set("pat_pending_presc_id",
                  Long.valueOf(pendingPrescriptionsRepository.getNextSequence()));
              pendingPrescriptionsRepository.insert(newBean);
            }
          }
        } else if (noOfExistingServiceQty > noOfEditedServiceQty) {
          map = new HashMap<>();
          map.put("columnName", "preauth_activity_id");
          map.put("columnValue", itemMap.get("preauth_activity_id"));
          map.put("limit", noOfExistingServiceQty - noOfEditedServiceQty);
          List<BasicDynaBean> beanListToDelete =
              pendingPrescriptionsRepository.getPendingServicePrescriptionId(map);
          if (!beanListToDelete.isEmpty()) {
            List<Object> deleteKeys = new ArrayList<>();
            for (BasicDynaBean bean : beanListToDelete) {
              bean.set("preauth_activity_id", null);
              deleteKeys.add(bean.get("pat_pending_presc_id"));
            }
            Map<String, Object> updateKeyMap = new HashMap<>();
            updateKeyMap.put("pat_pending_presc_id", deleteKeys);
            pendingPrescriptionsRepository.batchUpdate(beanListToDelete, updateKeyMap);
          }
        }
        int approvedQty = (int) itemMap.get("approved_qty");
        int totalDenied = 0;
        int avbleQty = 0;

        // if status open or sent then update all to O or S.
        if ("O".equals(status) || "S".equals(status)) {
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), status,
              noOfEditedServiceQty);
        }

        // if not update appropriately.
        if ("C".equals(status)) {
          totalDenied = noOfEditedServiceQty - approvedQty;
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "C", approvedQty);
        }

        if ("D".equals(status)) {
          totalDenied = noOfEditedServiceQty - approvedQty;
          avbleQty = noOfEditedServiceQty - totalDenied;
        }

        if (totalDenied > 0) {
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "D", totalDenied);
        }
        if (avbleQty > 0) {
          updatePriorAuthStatus((int) itemMap.get("preauth_activity_id"), "C", avbleQty);
        }
      } else {
        updatePriorAuthStatus((int)itemMap.get("preauth_activity_id"), status, 1);
      } 
      List<BasicDynaBean> mainBeanList = pendingPrescriptionsRepository
          .findByCriteria(preauthFilterMap);
      for (BasicDynaBean pendingPresUpdateBean : mainBeanList) {
        pendingPresUpdateBean.set("presc_item_qty", itemMap.get("quantity"));
        String presRemark = (itemMap.get("preauth_act_item_remarks") != null)
            ? StringUtils.trimToEmpty(itemMap.get("preauth_act_item_remarks").toString())
            : "";
        updateBeansList.add(pendingPresUpdateBean);
        updateKeys.add(pendingPresUpdateBean.get("pat_pending_presc_id"));
        // if remarks has edited in the consultation,insert an entry in remarks log
        if (!presRemark.isEmpty()) {
          ModelMap remarksMap = new ModelMap();
          remarksMap.put("remark", presRemark);
          remarksMap.put("pending_prescription_id",
              pendingPresUpdateBean.get("pat_pending_presc_id"));
          this.addPendingPrescriptionsAndRemark(remarksMap);
        }
      }
    }
    if (!updateBeansList.isEmpty()) {
      updateKeysMap.put("pat_pending_presc_id", updateKeys);
      pendingPrescriptionsRepository.batchUpdate(updateBeansList, updateKeysMap);
    }

    // Delete priorauth items
    List<Object> pendingPrescDeleteKeys = new ArrayList<>();
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("preauth_activity_id", preauthActDelIds);
    for (BasicDynaBean pendingprescBean : pendingPrescriptionsRepository
        .findByCriteria(filterMap)) {
      if (pendingprescBean.get("patient_presc_id") == null) {
        pendingPrescDeleteKeys.add(pendingprescBean.get("pat_pending_presc_id"));
      }
    }
    if (!pendingPrescDeleteKeys.isEmpty()) {
      pendingPrescriptionsRemarksRepository.batchDelete("pending_prescription_id",
          pendingPrescDeleteKeys);
      pendingPrescriptionsRepository.batchDelete("pat_pending_presc_id", pendingPrescDeleteKeys);
    }
  }

  /**
   * Gets roles and users based on centerid.
   * 
   * @param centerId the centerid
   * @return the combined list of users and roles
   */
  public List<Map<String, Object>> getUsersAndRoles(int centerId) {
    Map<String, Object> userAndRoleMap = null;
    List<Map<String, Object>> userAndRoleMapList = new ArrayList<>();

    for (BasicDynaBean roleBean : hospitalRoleRepository.listAll("hosp_role_name")) {
      userAndRoleMap = new HashMap<>();
      userAndRoleMap.put("id", roleBean.get("hosp_role_id"));
      userAndRoleMap.put("name", roleBean.get("hosp_role_name"));
      userAndRoleMap.put("type", "role");
      userAndRoleMapList.add(userAndRoleMap);
    }

    for (BasicDynaBean userBean : userService.getUsersWithDefaultCenter(centerId,
        DEFAULT_PORTAL_ID)) {
      userAndRoleMap = new HashMap<>();
      userAndRoleMap.put("id", userBean.get("emp_username"));
      userAndRoleMap.put("name", userBean.get("temp_username"));
      userAndRoleMap.put("type", "user");
      userAndRoleMapList.add(userAndRoleMap);
    }

    return userAndRoleMapList;
  }

  /**
   * Gets roles and users based on centerid.
   * 
   * @param centerId the centerid
   * @return the combined list of users and roles
   */
  public List<Map<String, Object>> getPrescriptionedUsersAndRoles(int centerId) {
    Map<String, Object> userAndRoleMap = null;
    List<Map<String, Object>> userAndRoleMapList = new ArrayList<>();
    List<Map<String, Object>> prescribedUsers =
        pendingPrescriptionsRepository.getDistinctUsersOrRoles("users");
    List<Map<String, Object>> prescribedRoles =
        pendingPrescriptionsRepository.getDistinctUsersOrRoles("roles");
    List<String> distinctPrescUsers = new ArrayList<>();
    List<Integer> distinctPrescRoles = new ArrayList<>();
    for (Map<String, Object> prescRow : prescribedUsers) {
      distinctPrescUsers.add(prescRow.get("distinct_row").toString());
    }

    for (Map<String, Object> prescRow : prescribedRoles) {
      distinctPrescRoles.add(Integer.parseInt(prescRow.get("distinct_row").toString()));
    }

    if (!distinctPrescRoles.isEmpty()) {
      for (BasicDynaBean roleBean : hospitalRoleRepository
          .getSelectedHospitalRoles(distinctPrescRoles)) {
        userAndRoleMap = new HashMap<>();
        userAndRoleMap.put("id", roleBean.get("hosp_role_id"));
        userAndRoleMap.put("name", roleBean.get("hosp_role_name"));
        userAndRoleMap.put("type", "role");
        userAndRoleMapList.add(userAndRoleMap);
      }
    }

    if (!distinctPrescUsers.isEmpty()) {
      for (BasicDynaBean userBean : userService.getUsersWithDefaultCenter(centerId,
          DEFAULT_PORTAL_ID, distinctPrescUsers)) {
        userAndRoleMap = new HashMap<>();
        userAndRoleMap.put("id", userBean.get("emp_username"));
        userAndRoleMap.put("name", userBean.get("temp_username"));
        userAndRoleMap.put("type", "user");
        userAndRoleMapList.add(userAndRoleMap);
      }
    }

    return userAndRoleMapList;
  }

  /**
   * Updates op visit id to ip visit id on op to ip convertion.
   *
   * @param billNo the bill number
   * @param ipVisitId the ip visit id
   * @throws SQLException the SQL exception
   */
  public void updateOpToIpPrescriptions(String billNo, String ipVisitId) throws SQLException {
    pendingPrescriptionsRepository.updateVisitId(billNo, ipVisitId);
  }

  /**
   * deletes follow up pending prescriptions along with remarks.
   * 
   * @param deleteKeys List of followup ids to be deleted.
   */
  public void deleteFollowUpPrescription(List<Object> deleteKeys) {
    List<Object> remarkKeys = new ArrayList<>();
    for (Object key : deleteKeys) {
      remarkKeys.add(
          pendingPrescriptionsRepository.findByKey("followup_id", key).get("pat_pending_presc_id"));
    }
    pendingPrescriptionsRemarksRepository.batchDelete("pending_prescription_id", remarkKeys);
    pendingPrescriptionsRepository.batchDelete("followup_id", deleteKeys);
  }

  /**
   * Get User prescription types.
   * 
   * @return the list of prescriptions.
   */
  public List<String> getUserPrescriptionTypes() {

    List<Integer> userHospitalRoles =
        (List<Integer>) sessionService.getSessionAttributes().get("hospital_role_ids");
    List<String> prescriptionTypes = new ArrayList<>();
    if (!userHospitalRoles.isEmpty()) {
      List<Map<String, String>> hospitalRoles =
          pendingPrescriptionsRepository.getUserPrescrptionTypes(userHospitalRoles);
      if (hospitalRoles != null && !hospitalRoles.isEmpty()) {
        for (Map<String, String> prescriptionTypesMap : hospitalRoles) {
          prescriptionTypes.add(prescriptionTypesMap.get("prescription_type"));
          if ("Ref".equals(prescriptionTypesMap.get("prescription_type"))) {
            prescriptionTypes.add("Dep");
          }
        }
      }
    }
    return prescriptionTypes;
  }

  /**
   * Gets the appointment id and appointment time for the passed PPD Id.
   *
   * @param ppdId the ppd id
   * @return the appointment id
   */
  public Map<String, String> getAppointmentId(Long ppdId) {
    BasicDynaBean appointmentBean = pendingPrescriptionsRepository.getAppointmentId(ppdId);
    if (null != appointmentBean) {
      Map<String, String> appointmentMap = new HashMap<>();
      appointmentMap.put("appointment_id", appointmentBean.get("appointment_id").toString());
      SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
      String appointmentTimestamp =
          dateFormatter.format((Timestamp) appointmentBean.get("appointment_time"));
      appointmentMap.put("appointment_time", appointmentTimestamp);
      return appointmentMap;
    }
    return Collections.EMPTY_MAP;
  }

  /**
   * Gets the pat_presc_id and pat_pending_presc_id of appointment.
   *
   * @param appointmentId the appointment id
   * @return the presc id map of appointment
   */
  public Map<String, Object> getPrescIdMapOfAppointment(String appointmentId) {
    BasicDynaBean prescriptionBean =
        pendingPrescriptionsRepository.getPrescIdOfAppointment(appointmentId);
    Map<String, Object> prescriptionMap = new HashMap<>();
    if (null != prescriptionBean) {
      prescriptionMap.put("patient_presc_id", prescriptionBean.get("patient_presc_id"));
      prescriptionMap.put("pat_pending_presc_id", prescriptionBean.get("pat_pending_presc_id"));
      prescriptionMap.put("preauth_act_id", prescriptionBean.get("preauth_activity_id"));
      prescriptionMap.put("preauth_activity_status",
          prescriptionBean.get("preauth_activity_status"));
    }
    return prescriptionMap;

  }

  /**
   * Update pending prescription status.
   *
   * @param patientPendingPrescId the patient pending presc id
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(String patientPendingPrescId, String status) {
    pendingPrescriptionsRepository.updatePendingPrescriptionStatus(patientPendingPrescId, status);
  }

  /**
   * Update pending prescription status.
   *
   * @param prescriptionId the prescription id
   * @param qty the qty
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(Integer prescriptionId, Integer qty, String status) {
    pendingPrescriptionsRepository.updatePendingPrescriptionStatus(prescriptionId, qty, status);

  }


  /**
   * Update pending prescription status.
   *
   * @param prescriptionId the prescription id
   * @param priorAuthStatus the prior auth status
   * @param qty the qty
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(Integer prescriptionId, String priorAuthStatus,
      Integer qty, String status) {
    pendingPrescriptionsRepository.updatePendingPrescriptionStatus(prescriptionId, qty,
        priorAuthStatus, status);

  }

  /**
   * Update pending prescription status.
   *
   * @param preAuthId the prescription id
   * @param status the status
   */
  public void updatePendingPrescriptionStatusWithPreauthId(Integer preAuthId, String status) {
    pendingPrescriptionsRepository.updatePriorAuthItemStatus(preAuthId, status);
  }

  /**
   * Update pending prescription status.
   *
   * @param preAuthId the prescription id
   * @param preAuthActStatus the prescription id
   * @param status the status
   */
  public void updatePendingPrescriptionStatusWithPreauthId(Integer preAuthId,
      String preAuthActStatus, String status) {
    pendingPrescriptionsRepository.updatePriorAuthItemStatus(preAuthId, preAuthActStatus, status);

  }


  /**
   * Update preauth activity id.
   *
   * @param patientPrescIds the patient presc ids
   */
  public void updatePreauthActivityId(List<Integer> patientPrescIds) {
    List<BasicDynaBean> pendingPrescBeans = new ArrayList<>();
    List<Long> patientPendingPrescriptionKeys = new ArrayList<>();
    List<Long> priorAuthRequiredList = new ArrayList<>();
    Map<Long, Long> pendingPrescIdsMap = new HashMap<>();
    List<Integer> patPrescIds = new ArrayList<>();
    // update preauth_activity_id for precription_id
    for (BasicDynaBean preauthPresActivity : eauthPrescriptionActivitiesRepository
        .getPreauthActIds(patientPrescIds)) {
      patPrescIds.add((Integer) preauthPresActivity.get("patient_pres_id"));
      pendingPrescIdsMap.put(Long.valueOf(preauthPresActivity.get("patient_pres_id").toString()),
          Long.valueOf(preauthPresActivity.get("preauth_act_id").toString()));
      if (null != preauthPresActivity.get("preauth_required")
          && "Y".equals(preauthPresActivity.get("preauth_required"))) {
        priorAuthRequiredList
            .add(Long.valueOf(preauthPresActivity.get("patient_pres_id").toString()));
      }
    }
    if (!patPrescIds.isEmpty()) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("patient_presc_id", patPrescIds);
      for (BasicDynaBean pendingPrescBean : pendingPrescriptionsRepository
          .findByCriteria(filterMap)) {
        Long patientPrescId = Long.valueOf(pendingPrescBean.get("patient_presc_id").toString());
        if (pendingPrescIdsMap.containsKey(patientPrescId)) {
          pendingPrescBean.set("preauth_activity_id", pendingPrescIdsMap.get(patientPrescId));
          if (priorAuthRequiredList.contains(patientPrescId)) {
            pendingPrescBean.set("preauth_activity_status", "O");
          }
          pendingPrescBeans.add(pendingPrescBean);
          patientPendingPrescriptionKeys
              .add(Long.valueOf(pendingPrescBean.get("pat_pending_presc_id").toString()));
        }
      }
    }
    if (!pendingPrescBeans.isEmpty()) {
      Map<String, Object> updateKeysMap = new HashMap<>();
      updateKeysMap.put("pat_pending_presc_id", patientPendingPrescriptionKeys);
      pendingPrescriptionsRepository.batchUpdate(pendingPrescBeans, updateKeysMap);
    }
  }

  /**
   * Get prior auth presc id.
   * 
   * @param pendingPrescriptionId the id
   * @return integer
   */
  public Integer getPriorAuthActIdFromPendingPrescId(Long pendingPrescriptionId) {
    BasicDynaBean bean =
        pendingPrescriptionsRepository.findByKey("pat_pending_presc_id", pendingPrescriptionId);
    if (null != bean) {
      return Integer.valueOf(((Long) bean.get("preauth_activity_id")).toString());
    }
    return null;
  }

  public int updatePriorAuthStatus(int preauthActId, String preAuthStatus, int quantityToUpdate) {
    return pendingPrescriptionsRepository.updatePriorAuthStatus(preauthActId, preAuthStatus,
        quantityToUpdate);
  }
}
